package engine.assets;

import engine.math.Vec3;
import engine.render.Material;
import engine.render.Mesh;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ObjMeshBuilder {
    private ObjMeshBuilder() {
    }

    static List<Mesh> build(List<Vec3> positions, List<float[]> uvs, List<Vec3> normalsIn, List<int[]> facesV, List<int[]> facesVt, List<int[]> facesVn, List<String> faceMtls, Map<String, Material> materials) {
        ArrayList<Mesh> parts = new ArrayList<Mesh>();
        ArrayList<Integer> group = new ArrayList<Integer>();
        String groupMtl = faceMtls.get(0);
        for (int i = 0; i < facesV.size(); ++i) {
            if (!ObjMeshBuilder.eq(groupMtl, faceMtls.get(i)) && !group.isEmpty()) {
                parts.add(ObjMeshBuilder.oneMesh(positions, uvs, normalsIn, facesV, facesVt, facesVn, group, materials.get(groupMtl)));
                group = new ArrayList();
                groupMtl = faceMtls.get(i);
            }
            group.add(i);
        }
        if (!group.isEmpty()) {
            parts.add(ObjMeshBuilder.oneMesh(positions, uvs, normalsIn, facesV, facesVt, facesVn, group, materials.get(groupMtl)));
        }
        return parts;
    }

    private static Mesh oneMesh(List<Vec3> positions, List<float[]> uvs, List<Vec3> normalsIn, List<int[]> facesV, List<int[]> facesVt, List<int[]> facesVn, List<Integer> group, Material mat) {
        float[] norFinal;
        float[] uvFinal;
        ArrayList<int[]> tv = new ArrayList<int[]>();
        ArrayList<int[]> tt = new ArrayList<int[]>();
        ArrayList<int[]> tn = new ArrayList<int[]>();
        for (int fi : group) {
            int[] v = facesV.get(fi);
            int[] t = facesVt.get(fi);
            int[] n = facesVn.get(fi);
            int k = 1;
            while (k + 1 < v.length) {
                tv.add(new int[]{v[0], v[k], v[k + 1]});
                tt.add(new int[]{t[0], t[k], t[k + 1]});
                tn.add(new int[]{n[0], n[k], n[k + 1]});
                ++k;
            }
        }
        int tris = tv.size();
        float[] pos = new float[tris * 9];
        float[] uv = new float[tris * 6];
        float[] nor = new float[tris * 9];
        boolean hasUv = false;
        boolean hasN = false;
        int uvCount = 0;
        int norCount = 0;
        for (int t = 0; t < tris; ++t) {
            for (int k = 0; k < 3; ++k) {
                int ni;
                Vec3 p = positions.get(((int[])tv.get(t))[k]);
                pos[(t * 3 + k) * 3] = p.x();
                pos[(t * 3 + k) * 3 + 1] = p.y();
                pos[(t * 3 + k) * 3 + 2] = p.z();
                int ti = ((int[])tt.get(t))[k];
                if (ti >= 0 && ti < uvs.size()) {
                    uv[(t * 3 + k) * 2] = uvs.get(ti)[0];
                    uv[(t * 3 + k) * 2 + 1] = uvs.get(ti)[1];
                    hasUv = true;
                    ++uvCount;
                }
                if ((ni = ((int[])tn.get(t))[k]) < 0 || ni >= normalsIn.size()) continue;
                Vec3 nn = normalsIn.get(ni);
                nor[(t * 3 + k) * 3] = nn.x();
                nor[(t * 3 + k) * 3 + 1] = nn.y();
                nor[(t * 3 + k) * 3 + 2] = nn.z();
                hasN = true;
                ++norCount;
            }
        }
        if (!hasN) {
            ObjMeshBuilder.smoothNormals(pos, tris, nor);
            float[] uvFinal2 = hasUv && uvCount == tris * 3 ? uv : null;
            int[] idx = new int[tris * 3];
            for (int i = 0; i < idx.length; ++i) {
                idx[i] = i;
            }
            return new Mesh(pos, nor, uvFinal2, idx, mat != null ? mat : Material.DEFAULT);
        }
        float[] fArray = uvFinal = hasUv && uvCount == tris * 3 ? uv : null;
        if (norCount == tris * 3) {
            norFinal = nor;
        } else {
            ObjMeshBuilder.smoothNormals(pos, tris, nor);
            norFinal = nor;
        }
        int[] idx = new int[tris * 3];
        for (int i = 0; i < idx.length; ++i) {
            idx[i] = i;
        }
        return new Mesh(pos, norFinal, uvFinal, idx, mat != null ? mat : Material.DEFAULT);
    }

    static void smoothNormals(float[] pos, int tris, float[] out) {
        int i;
        int vc = tris * 3;
        for (int t = 0; t < tris; ++t) {
            float ax = pos[t * 9];
            float ay = pos[t * 9 + 1];
            float az = pos[t * 9 + 2];
            float bx = pos[t * 9 + 3];
            float by = pos[t * 9 + 4];
            float bz = pos[t * 9 + 5];
            float cx = pos[t * 9 + 6];
            float cy = pos[t * 9 + 7];
            float cz = pos[t * 9 + 8];
            float e1x = bx - ax;
            float e1y = by - ay;
            float e1z = bz - az;
            float e2x = cx - ax;
            float e2y = cy - ay;
            float e2z = cz - az;
            float nx = e1y * e2z - e1z * e2y;
            float ny = e1z * e2x - e1x * e2z;
            float nz = e1x * e2y - e1y * e2x;
            for (int k = 0; k < 3; ++k) {
                int o;
                int n = o = (t * 3 + k) * 3;
                out[n] = out[n] + nx;
                int n2 = o + 1;
                out[n2] = out[n2] + ny;
                int n3 = o + 2;
                out[n3] = out[n3] + nz;
            }
        }
        HashMap<String, Integer> first = new HashMap<String, Integer>();
        for (i = 0; i < vc; ++i) {
            String key = pos[i * 3] + "," + pos[i * 3 + 1] + "," + pos[i * 3 + 2];
            Integer f = first.putIfAbsent(key, i);
            if (f == null) continue;
            int n = f * 3;
            out[n] = out[n] + out[i * 3];
            int n4 = f * 3 + 1;
            out[n4] = out[n4] + out[i * 3 + 1];
            int n5 = f * 3 + 2;
            out[n5] = out[n5] + out[i * 3 + 2];
        }
        for (i = 0; i < vc; ++i) {
            float nz;
            float ny;
            String key = pos[i * 3] + "," + pos[i * 3 + 1] + "," + pos[i * 3 + 2];
            int f = (Integer)first.get(key);
            float nx = out[f * 3];
            float len = (float)Math.sqrt(nx * nx + (ny = out[f * 3 + 1]) * ny + (nz = out[f * 3 + 2]) * nz);
            if (len < 1.0E-9f) {
                out[i * 3] = 0.0f;
                out[i * 3 + 1] = 1.0f;
                out[i * 3 + 2] = 0.0f;
                continue;
            }
            out[i * 3] = nx / len;
            out[i * 3 + 1] = ny / len;
            out[i * 3 + 2] = nz / len;
        }
    }

    private static boolean eq(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
