package engine.render;

import java.util.ArrayList;
import java.util.List;

public final class Clipper {
    private Clipper() {
    }

    public static List<float[]> clipTriangleNear(float[] v0, float[] v1, float[] v2, float near) {
        ArrayList<float[]> poly = new ArrayList<float[]>(4);
        poly.add(v0);
        poly.add(v1);
        poly.add(v2);
        ArrayList<float[]> out = new ArrayList<float[]>(4);
        float limit = -near;
        for (int i = 0; i < poly.size(); ++i) {
            boolean inB;
            float[] a = (float[])poly.get(i);
            float[] b = (float[])poly.get((i + 1) % poly.size());
            boolean inA = a[2] <= limit;
            boolean bl = inB = b[2] <= limit;
            if (inA && inB) {
                out.add(b);
                continue;
            }
            if (inA) {
                out.add(Clipper.intersect(a, b, limit));
                continue;
            }
            if (!inB) continue;
            out.add(Clipper.intersect(a, b, limit));
            out.add(b);
        }
        ArrayList<float[]> tris = new ArrayList<float[]>(2);
        if (out.size() < 3) {
            return tris;
        }
        float[] p0 = (float[])out.get(0);
        int i = 1;
        while (i + 1 < out.size()) {
            float[] p1 = (float[])out.get(i);
            float[] p2 = (float[])out.get(i + 1);
            tris.add(new float[]{p0[0], p0[1], p0[2], p1[0], p1[1], p1[2], p2[0], p2[1], p2[2]});
            ++i;
        }
        return tris;
    }

    private static float[] intersect(float[] a, float[] b, float limit) {
        float denom = b[2] - a[2];
        float t = Math.abs(denom) < 1.0E-9f ? 0.0f : (limit - a[2]) / denom;
        t = Math.max(0.0f, Math.min(1.0f, t));
        return new float[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, limit};
    }

    public static List<Vertex[]> clipTriangleAttr(Vertex v0, Vertex v1, Vertex v2, float near) {
        float limit = -near;
        boolean in0 = v0.z <= limit;
        boolean in1 = v1.z <= limit;
        boolean in2 = v2.z <= limit;
        int inside = (in0 ? 1 : 0) + (in1 ? 1 : 0) + (in2 ? 1 : 0);
        ArrayList<Vertex[]> tris = new ArrayList<Vertex[]>(2);
        if (inside == 3) {
            tris.add(new Vertex[]{v0, v1, v2});
            return tris;
        }
        if (inside == 0) {
            return tris;
        }
        if (inside == 1) {
            Vertex v = in0 ? v0 : (in1 ? v1 : v2);
            Vertex o1 = in0 ? v1 : (in1 ? v2 : v0);
            Vertex o2 = in0 ? v2 : (in1 ? v0 : v1);
            Vertex a = Clipper.intersectAttr(v, o1, limit);
            Vertex b = Clipper.intersectAttr(v, o2, limit);
            tris.add(new Vertex[]{v, a, b});
            return tris;
        }
        Vertex o = !in0 ? v0 : (!in1 ? v1 : v2);
        Vertex k1 = !in0 ? v1 : (!in1 ? v2 : v0);
        Vertex k2 = !in0 ? v2 : (!in1 ? v0 : v1);
        Vertex a = Clipper.intersectAttr(o, k1, limit);
        Vertex b = Clipper.intersectAttr(o, k2, limit);
        tris.add(new Vertex[]{k1, k2, a});
        tris.add(new Vertex[]{k2, b, a});
        return tris;
    }

    private static Vertex intersectAttr(Vertex a, Vertex b, float limit) {
        float denom = b.z - a.z;
        float t = Math.abs(denom) < 1.0E-9f ? 0.0f : (limit - a.z) / denom;
        t = Math.max(0.0f, Math.min(1.0f, t));
        float[] attr = new float[a.attr.length];
        for (int i = 0; i < attr.length; ++i) {
            attr[i] = a.attr[i] + (b.attr[i] - a.attr[i]) * t;
        }
        return new Vertex(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, limit, attr);
    }

    public static final class Vertex {
        public float x;
        public float y;
        public float z;
        public final float[] attr;

        public Vertex(float x, float y, float z, float[] attr) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.attr = attr;
        }
    }
}
