package engine.pipeline;

import engine.math.Mat4;
import engine.math.Vec3;
import engine.math.Vec4;
import engine.pipeline.SceneSnapshot;
import engine.pipeline.TransformStage;
import engine.render.Clipper;
import engine.render.Material;
import engine.render.Renderer;
import java.util.ArrayList;
import java.util.List;

public final class ProjectionStage {
    private ProjectionStage() {
    }

    public static ProjectionResult run(SceneSnapshot snap, TransformStage.TransformResult tr) {
        Mat4 view = snap.camera().viewMatrix();
        float aspect = (float)snap.fbWidth() / (float)snap.fbHeight();
        Mat4 proj = Mat4.perspective(snap.fovYRad(), aspect, snap.near(), snap.far());
        ArrayList<DrawCommand> solids = new ArrayList<DrawCommand>();
        ArrayList<DrawCommand> wires = new ArrayList<DrawCommand>();
        int drawn = 0;
        int backfaced = 0;
        boolean wire = snap.mode() == Renderer.Mode.WIREFRAME;
        for (TransformStage.TransformedInstance inst : tr.visible()) {
            int[] idx = inst.mesh().indicesRaw();
            for (int t = 0; t < idx.length; t += 3) {
                int i0 = idx[t];
                int i1 = idx[t + 1];
                int i2 = idx[t + 2];
                float[] c0 = ProjectionStage.toCamera(view, inst, i0);
                float[] c1 = ProjectionStage.toCamera(view, inst, i1);
                float[] c2 = ProjectionStage.toCamera(view, inst, i2);
                boolean hasUv = inst.hasUv();
                Clipper.Vertex v0 = ProjectionStage.attr(c0, inst, i0, hasUv);
                Clipper.Vertex v1 = ProjectionStage.attr(c1, inst, i1, hasUv);
                Clipper.Vertex v2 = ProjectionStage.attr(c2, inst, i2, hasUv);
                List<Clipper.Vertex[]> clipped = Clipper.clipTriangleAttr(v0, v1, v2, snap.near());
                for (Clipper.Vertex[] tri : clipped) {
                    if (ProjectionStage.isBackface(tri)) {
                        ++backfaced;
                        continue;
                    }
                    DrawCommand cmd = ProjectionStage.project(tri, proj, inst, snap);
                    if (cmd == null) continue;
                    (wire ? wires : solids).add(cmd);
                    ++drawn;
                }
            }
        }
        return new ProjectionResult(solids, wires, drawn, backfaced);
    }

    private static float[] toCamera(Mat4 view, TransformStage.TransformedInstance inst, int i) {
        Vec4 c = view.transform(new Vec4(inst.wx()[i], inst.wy()[i], inst.wz()[i], 1.0f));
        return new float[]{c.x(), c.y(), c.z()};
    }

    private static Clipper.Vertex attr(float[] c, TransformStage.TransformedInstance inst, int i, boolean hasUv) {
        float[] fArray;
        float[] tuv = inst.tuv();
        if (hasUv && tuv != null) {
            float[] fArray2 = new float[8];
            fArray2[0] = inst.wnx()[i];
            fArray2[1] = inst.wny()[i];
            fArray2[2] = inst.wnz()[i];
            fArray2[3] = inst.wx()[i];
            fArray2[4] = inst.wy()[i];
            fArray2[5] = inst.wz()[i];
            fArray2[6] = tuv[i * 2];
            fArray = fArray2;
            fArray2[7] = tuv[i * 2 + 1];
        } else {
            float[] fArray3 = new float[6];
            fArray3[0] = inst.wnx()[i];
            fArray3[1] = inst.wny()[i];
            fArray3[2] = inst.wnz()[i];
            fArray3[3] = inst.wx()[i];
            fArray3[4] = inst.wy()[i];
            fArray = fArray3;
            fArray3[5] = inst.wz()[i];
        }
        float[] attr = fArray;
        return new Clipper.Vertex(c[0], c[1], c[2], attr);
    }

    private static boolean isBackface(Clipper.Vertex[] tri) {
        float e1y = tri[1].y - tri[0].y;
        float e2z = tri[2].z - tri[0].z;
        float e1z = tri[1].z - tri[0].z;
        float e2y = tri[2].y - tri[0].y;
        float nx = e1y * e2z - e1z * e2y;
        float e2x = tri[2].x - tri[0].x;
        float e1x = tri[1].x - tri[0].x;
        float ny = e1z * e2x - e1x * e2z;
        float nz = e1x * e2y - e1y * e2x;
        return nx * tri[0].x + ny * tri[0].y + nz * tri[0].z >= 0.0f;
    }

    private static DrawCommand project(Clipper.Vertex[] tri, Mat4 proj, TransformStage.TransformedInstance inst, SceneSnapshot snap) {
        Vec4 p0 = proj.transform(new Vec4(tri[0].x, tri[0].y, tri[0].z, 1.0f));
        Vec4 p1 = proj.transform(new Vec4(tri[1].x, tri[1].y, tri[1].z, 1.0f));
        Vec4 p2 = proj.transform(new Vec4(tri[2].x, tri[2].y, tri[2].z, 1.0f));
        if (p0.w() <= 0.0f || p1.w() <= 0.0f || p2.w() <= 0.0f) {
            return null;
        }
        Vec3 n0 = p0.perspectiveDivide();
        Vec3 n1 = p1.perspectiveDivide();
        Vec3 n2 = p2.perspectiveDivide();
        float w0 = (n0.x() * 0.5f + 0.5f) * (float)snap.fbWidth();
        float h0 = (1.0f - (n0.y() * 0.5f + 0.5f)) * (float)snap.fbHeight();
        float w1 = (n1.x() * 0.5f + 0.5f) * (float)snap.fbWidth();
        float h1 = (1.0f - (n1.y() * 0.5f + 0.5f)) * (float)snap.fbHeight();
        float w2 = (n2.x() * 0.5f + 0.5f) * (float)snap.fbWidth();
        float h2 = (1.0f - (n2.y() * 0.5f + 0.5f)) * (float)snap.fbHeight();
        float d0 = n0.z() * 0.5f + 0.5f;
        float d1 = n1.z() * 0.5f + 0.5f;
        float d2 = n2.z() * 0.5f + 0.5f;
        float cw0 = Math.max(1.0E-6f, -tri[0].z);
        float cw1 = Math.max(1.0E-6f, -tri[1].z);
        float cw2 = Math.max(1.0E-6f, -tri[2].z);
        return new DrawCommand(w0, h0, cw0, d0, tri[0].attr, w1, h1, cw1, d1, tri[1].attr, w2, h2, cw2, d2, tri[2].attr, inst.material());
    }

    public record DrawCommand(float sx0, float sy0, float w0, float z0, float[] a0, float sx1, float sy1, float w1, float z1, float[] a1, float sx2, float sy2, float w2, float z2, float[] a2, Material material) {
    }

    public record ProjectionResult(List<DrawCommand> solids, List<DrawCommand> wires, int drawn, int backfaceCulled) {
    }
}
