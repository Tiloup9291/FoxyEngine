package engine.pipeline;

import engine.math.Mat4;
import engine.math.Vec3;
import engine.pipeline.SceneSnapshot;
import engine.render.Camera;
import engine.render.Clipper;
import engine.render.Frustum;
import engine.render.Material;
import engine.render.Mesh;
import engine.render.Renderer;
import engine.render.Transform;
import java.util.ArrayList;
import java.util.List;

public final class TransformStage {
    private TransformStage() {
    }

    public static TransformResult run(SceneSnapshot snap, Frustum frustum) {
        ArrayList<TransformedInstance> visible = new ArrayList<TransformedInstance>(snap.instances().size());
        int culled = 0;
        Mat4 spin = Mat4.rotationY(snap.spinAngle()).mul(Mat4.rotationX(snap.spinAngle() * 0.6f));
        for (Renderer.Instance inst : snap.instances()) {
            Mat4 model = inst.transform().modelMatrix().mul(spin);
            if (!frustum.isVisible(Frustum.transformAABB(inst.mesh().bounds(), model))) {
                ++culled;
                continue;
            }
            visible.add(TransformStage.transformOne(inst.mesh(), model, inst.transform().rotationMatrix().mul(spin)));
        }
        return new TransformResult(visible, culled);
    }

    static TransformedInstance transformOne(Mesh mesh, Mat4 model, Mat4 rotOnly) {
        float[] pos = mesh.positionsRaw();
        float[] nrm = mesh.normalsRaw();
        float[] tuv = mesh.uvsRaw();
        int vc = mesh.vertexCount();
        float[] wx = new float[vc];
        float[] wy = new float[vc];
        float[] wz = new float[vc];
        for (int i = 0; i < vc; ++i) {
            Vec3 w = model.transformPoint(new Vec3(pos[i * 3], pos[i * 3 + 1], pos[i * 3 + 2]));
            wx[i] = w.x();
            wy[i] = w.y();
            wz[i] = w.z();
        }
        float[] wnx = new float[vc];
        float[] wny = new float[vc];
        float[] wnz = new float[vc];
        if (nrm != null) {
            for (int i = 0; i < vc; ++i) {
                Vec3 q = rotOnly.transformDirection(new Vec3(nrm[i * 3], nrm[i * 3 + 1], nrm[i * 3 + 2])).normalize();
                wnx[i] = q.x();
                wny[i] = q.y();
                wnz[i] = q.z();
            }
        } else {
            int[] idx = mesh.indicesRaw();
            for (int t = 0; t < idx.length; t += 3) {
                int i1 = idx[t + 1];
                int i0 = idx[t];
                float e1y = wy[i1] - wy[i0];
                int i2 = idx[t + 2];
                float e2z = wz[i2] - wz[i0];
                float e1z = wz[i1] - wz[i0];
                float e2y = wy[i2] - wy[i0];
                float nx = e1y * e2z - e1z * e2y;
                float e2x = wx[i2] - wx[i0];
                float e1x = wx[i1] - wx[i0];
                float ny = e1z * e2x - e1x * e2z;
                float nz = e1x * e2y - e1y * e2x;
                float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (len < 1.0E-9f) {
                    nx = 0.0f;
                    ny = 1.0f;
                    nz = 0.0f;
                    len = 1.0f;
                }
                wnx[i0] = nx /= len;
                wny[i0] = ny /= len;
                wnz[i0] = nz /= len;
                wnx[i1] = nx;
                wny[i1] = ny;
                wnz[i1] = nz;
                wnx[i2] = nx;
                wny[i2] = ny;
                wnz[i2] = nz;
            }
        }
        return new TransformedInstance(mesh, mesh.material(), wx, wy, wz, wnx, wny, wnz, tuv, tuv != null);
    }

    public static Frustum frustumOf(SceneSnapshot snap) {
        Camera cam = snap.camera();
        Mat4 view = cam.viewMatrix();
        float aspect = (float)snap.fbWidth() / (float)snap.fbHeight();
        Mat4 proj = Mat4.perspective(snap.fovYRad(), aspect, snap.near(), snap.far());
        return Frustum.fromViewProjection(proj.mul(view));
    }

    public static Mat4 modelOf(Transform t, float spinAngle) {
        Mat4 spin = Mat4.rotationY(spinAngle).mul(Mat4.rotationX(spinAngle * 0.6f));
        return t.modelMatrix().mul(spin);
    }

    static List<float[]> clipNear(float[] c0, float[] c1, float[] c2, float near) {
        return Clipper.clipTriangleNear(c0, c1, c2, near);
    }

    public record TransformedInstance(Mesh mesh, Material material, float[] wx, float[] wy, float[] wz, float[] wnx, float[] wny, float[] wnz, float[] tuv, boolean hasUv) {
    }

    public record TransformResult(List<TransformedInstance> visible, int culled) {
    }
}
