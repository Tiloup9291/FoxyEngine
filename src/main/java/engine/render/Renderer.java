package engine.render;

import engine.math.Mat4;
import engine.math.Vec3;
import engine.math.Vec4;
import engine.render.Camera;
import engine.render.Clipper;
import engine.render.Framebuffer;
import engine.render.Frustum;
import engine.render.Light;
import engine.render.Material;
import engine.render.Mesh;
import engine.render.PhongRaster;
import engine.render.Rasterizer;
import engine.render.Texture;
import engine.render.TextureCache;
import engine.render.Transform;
import java.util.List;

public final class Renderer {
    private final Framebuffer fb;
    private final List<Instance> instances;
    private final float fovYRad;
    private final float near;
    private final float far;
    private final int fallbackColor;
    private final int wireColor;
    private Mode mode = Mode.SOLID;
    private float angle;
    private Camera camera = Camera.defaultCamera();
    private float spinSpeed = 0.9f;
    private Light light = Light.DEFAULT_SUN;
    private float ambient = 0.25f;
    private Texture.Filter filter = Texture.Filter.BILINEAR;
    private TextureCache textures;

    public Renderer(Framebuffer fb, List<Instance> instances, float fovYDeg, float near, float far, int wireColor, int fallbackColor) {
        if (instances == null || instances.isEmpty()) {
            throw new IllegalArgumentException("empty instances");
        }
        this.fb = fb;
        this.instances = List.copyOf(instances);
        this.fovYRad = (float)Math.toRadians(fovYDeg);
        this.near = near;
        this.far = far;
        this.wireColor = wireColor;
        this.fallbackColor = fallbackColor;
    }

    public Renderer(Framebuffer fb, float cubeSize, float fovYDeg, float near, float far, int cubeColor, int wireColor) {
        this(fb, List.of(new Instance(Mesh.createCube(cubeSize), Transform.of(Vec3.ZERO, cubeSize / 1.6f))), fovYDeg, near, far, wireColor, cubeColor);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode mode() {
        return this.mode;
    }

    public void toggleMode() {
        this.mode = this.mode == Mode.SOLID ? Mode.WIREFRAME : Mode.SOLID;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void setSpinSpeed(float radPerSec) {
        this.spinSpeed = radPerSec;
    }

    public float angle() {
        return this.angle;
    }

    public void setLight(Light light) {
        this.light = light;
    }

    public Light light() {
        return this.light;
    }

    public void setAmbient(float ambient) {
        this.ambient = ambient;
    }

    public void setTextureFilter(Texture.Filter filter) {
        this.filter = filter;
    }

    public void setTextures(TextureCache textures) {
        this.textures = textures;
    }

    public int render(float dtSeconds, int clearColor) {
        this.angle += dtSeconds * this.spinSpeed;
        this.fb.clear(clearColor);
        float aspect = (float)this.fb.width() / (float)this.fb.height();
        Mat4 spin = Mat4.rotationY(this.angle).mul(Mat4.rotationX(this.angle * 0.6f));
        Mat4 view = this.camera.viewMatrix();
        Mat4 proj = Mat4.perspective(this.fovYRad, aspect, this.near, this.far);
        Frustum frustum = Frustum.fromViewProjection(proj.mul(view));
        Vec3 camPos = this.camera.position();
        int drawn = 0;
        for (Instance inst : this.instances) {
            Mat4 model = inst.transform().modelMatrix().mul(spin);
            Mat4 rotOnly = inst.transform().rotationMatrix().mul(spin);
            if (!frustum.isVisible(Frustum.transformAABB(inst.mesh().bounds(), model))) continue;
            drawn += this.renderMesh(inst.mesh(), model, rotOnly, view, proj, camPos);
        }
        return drawn;
    }

    private int renderMesh(Mesh mesh, Mat4 model, Mat4 rotOnly, Mat4 view, Mat4 proj, Vec3 camPos) {
        float[] pos = mesh.positions();
        float[] nrm = mesh.normals();
        float[] tuv = mesh.uvs();
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
        }
        Material mat = mesh.material() != null ? mesh.material() : Material.DEFAULT;
        Texture tex = this.textures != null ? this.textures.get(mat.mapKd()) : null;
        boolean hasUv = tuv != null && tex != null;
        int drawn = 0;
        int[] idx = mesh.indices();
        for (int t = 0; t < idx.length; t += 3) {
            int i0 = idx[t];
            int i1 = idx[t + 1];
            int i2 = idx[t + 2];
            float[] c0 = Renderer.toCamera(view, wx[i0], wy[i0], wz[i0]);
            float[] c1 = Renderer.toCamera(view, wx[i1], wy[i1], wz[i1]);
            float[] c2 = Renderer.toCamera(view, wx[i2], wy[i2], wz[i2]);
            float[] n0 = Renderer.faceOrVertexNormal(nrm, wnx, wny, wnz, i0, wx[i0], wy[i0], wz[i0], wx[i1], wy[i1], wz[i1], wx[i2], wy[i2], wz[i2]);
            float[] n1 = Renderer.faceOrVertexNormal(nrm, wnx, wny, wnz, i1, wx[i0], wy[i0], wz[i0], wx[i1], wy[i1], wz[i1], wx[i2], wy[i2], wz[i2]);
            float[] n2 = Renderer.faceOrVertexNormal(nrm, wnx, wny, wnz, i2, wx[i0], wy[i0], wz[i0], wx[i1], wy[i1], wz[i1], wx[i2], wy[i2], wz[i2]);
            List<Clipper.Vertex> poly0 = List.of(Renderer.attrVertex(c0, n0, wx[i0], wy[i0], wz[i0], tuv, i0, hasUv), Renderer.attrVertex(c1, n1, wx[i1], wy[i1], wz[i1], tuv, i1, hasUv), Renderer.attrVertex(c2, n2, wx[i2], wy[i2], wz[i2], tuv, i2, hasUv));
            List<Clipper.Vertex[]> clipped = Clipper.clipTriangleAttr(poly0.get(0), poly0.get(1), poly0.get(2), this.near);
            for (Clipper.Vertex[] tri : clipped) {
                if (Renderer.isBackfaceAttr(tri) || !this.projectAndDrawAttr(tri, proj, mat, tex, camPos)) continue;
                ++drawn;
            }
        }
        return drawn;
    }

    private static Clipper.Vertex attrVertex(float[] c, float[] n, float wx, float wy, float wz, float[] tuv, int i, boolean hasUv) {
        float[] fArray;
        if (hasUv) {
            float[] fArray2 = new float[8];
            fArray2[0] = n[0];
            fArray2[1] = n[1];
            fArray2[2] = n[2];
            fArray2[3] = wx;
            fArray2[4] = wy;
            fArray2[5] = wz;
            fArray2[6] = tuv[i * 2];
            fArray = fArray2;
            fArray2[7] = tuv[i * 2 + 1];
        } else {
            float[] fArray3 = new float[6];
            fArray3[0] = n[0];
            fArray3[1] = n[1];
            fArray3[2] = n[2];
            fArray3[3] = wx;
            fArray3[4] = wy;
            fArray = fArray3;
            fArray3[5] = wz;
        }
        float[] attr = fArray;
        return new Clipper.Vertex(c[0], c[1], c[2], attr);
    }

    private static float[] faceOrVertexNormal(float[] nrm, float[] wnx, float[] wny, float[] wnz, int i, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2) {
        if (nrm != null) {
            return new float[]{wnx[i], wny[i], wnz[i]};
        }
        float e1y = y1 - y0;
        float e2z = z2 - z0;
        float e1z = z1 - z0;
        float e2y = y2 - y0;
        float nx = e1y * e2z - e1z * e2y;
        float e2x = x2 - x0;
        float e1x = x1 - x0;
        float ny = e1z * e2x - e1x * e2z;
        float nz = e1x * e2y - e1y * e2x;
        float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0E-9f) {
            return new float[]{0.0f, 1.0f, 0.0f};
        }
        return new float[]{nx / len, ny / len, nz / len};
    }

    private static boolean isBackfaceAttr(Clipper.Vertex[] tri) {
        float e1y = tri[1].y - tri[0].y;
        float e2z = tri[2].z - tri[0].z;
        float e1z = tri[1].z - tri[0].z;
        float e2y = tri[2].y - tri[0].y;
        float nx = e1y * e2z - e1z * e2y;
        float e2x = tri[2].x - tri[0].x;
        float e1x = tri[1].x - tri[0].x;
        float ny = e1z * e2x - e1x * e2z;
        float nz = e1x * e2y - e1y * e2x;
        float dot = nx * tri[0].x + ny * tri[0].y + nz * tri[0].z;
        return dot >= 0.0f;
    }

    private boolean projectAndDrawAttr(Clipper.Vertex[] tri, Mat4 proj, Material mat, Texture tex, Vec3 camPos) {
        Vec4 p0 = proj.transform(new Vec4(tri[0].x, tri[0].y, tri[0].z, 1.0f));
        Vec4 p1 = proj.transform(new Vec4(tri[1].x, tri[1].y, tri[1].z, 1.0f));
        Vec4 p2 = proj.transform(new Vec4(tri[2].x, tri[2].y, tri[2].z, 1.0f));
        if (p0.w() <= 0.0f || p1.w() <= 0.0f || p2.w() <= 0.0f) {
            return false;
        }
        Vec3 n0 = p0.perspectiveDivide();
        Vec3 n1 = p1.perspectiveDivide();
        Vec3 n2 = p2.perspectiveDivide();
        float w0 = Math.max(1.0E-6f, -tri[0].z);
        float w1 = Math.max(1.0E-6f, -tri[1].z);
        float w2 = Math.max(1.0E-6f, -tri[2].z);
        if (this.mode == Mode.WIREFRAME) {
            float sx0 = (n0.x() * 0.5f + 0.5f) * (float)this.fb.width();
            float sy0 = (1.0f - (n0.y() * 0.5f + 0.5f)) * (float)this.fb.height();
            float sx1 = (n1.x() * 0.5f + 0.5f) * (float)this.fb.width();
            float sy1 = (1.0f - (n1.y() * 0.5f + 0.5f)) * (float)this.fb.height();
            float sx2 = (n2.x() * 0.5f + 0.5f) * (float)this.fb.width();
            float sy2 = (1.0f - (n2.y() * 0.5f + 0.5f)) * (float)this.fb.height();
            float d0 = n0.z() * 0.5f + 0.5f;
            float d1 = n1.z() * 0.5f + 0.5f;
            float d2 = n2.z() * 0.5f + 0.5f;
            Rasterizer.drawTriangleWireframe(this.fb, Math.round(sx0), Math.round(sy0), d0, Math.round(sx1), Math.round(sy1), d1, Math.round(sx2), Math.round(sy2), d2, this.wireColor);
            return true;
        }
        float sx0 = (n0.x() * 0.5f + 0.5f) * (float)this.fb.width();
        float sy0 = (1.0f - (n0.y() * 0.5f + 0.5f)) * (float)this.fb.height();
        float sx1 = (n1.x() * 0.5f + 0.5f) * (float)this.fb.width();
        float sy1 = (1.0f - (n1.y() * 0.5f + 0.5f)) * (float)this.fb.height();
        float sx2 = (n2.x() * 0.5f + 0.5f) * (float)this.fb.width();
        float sy2 = (1.0f - (n2.y() * 0.5f + 0.5f)) * (float)this.fb.height();
        float d0 = n0.z() * 0.5f + 0.5f;
        float d1 = n1.z() * 0.5f + 0.5f;
        float d2 = n2.z() * 0.5f + 0.5f;
        PhongRaster.drawTriangle(this.fb, sx0, sy0, w0, d0, tri[0].attr, sx1, sy1, w1, d1, tri[1].attr, sx2, sy2, w2, d2, tri[2].attr, mat, tex, this.filter, this.light, this.ambient, camPos.x(), camPos.y(), camPos.z());
        return true;
    }

    private static float[] toCamera(Mat4 view, float x, float y, float z) {
        Vec4 c = view.transform(new Vec4(x, y, z, 1.0f));
        return new float[]{c.x(), c.y(), c.z()};
    }

    private static Vec3 worldFaceNormal(float[] nrm, Mat4 rotOnly, int i0, int i1, int i2, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2) {
        if (nrm != null) {
            Vec3 q0 = rotOnly.transformDirection(new Vec3(nrm[i0 * 3], nrm[i0 * 3 + 1], nrm[i0 * 3 + 2])).normalize();
            Vec3 q1 = rotOnly.transformDirection(new Vec3(nrm[i1 * 3], nrm[i1 * 3 + 1], nrm[i1 * 3 + 2])).normalize();
            Vec3 q2 = rotOnly.transformDirection(new Vec3(nrm[i2 * 3], nrm[i2 * 3 + 1], nrm[i2 * 3 + 2])).normalize();
            return q0.add(q1).add(q2).normalize();
        }
        Vec3 w0 = new Vec3(x0, y0, z0);
        Vec3 w1 = new Vec3(x1, y1, z1);
        Vec3 w2 = new Vec3(x2, y2, z2);
        return w1.sub(w0).cross(w2.sub(w0)).normalize();
    }

    private static boolean isBackface(float[] tri) {
        float e1y = tri[4] - tri[1];
        float e2z = tri[8] - tri[2];
        float e1z = tri[5] - tri[2];
        float e2y = tri[7] - tri[1];
        float nx = e1y * e2z - e1z * e2y;
        float e2x = tri[6] - tri[0];
        float e1x = tri[3] - tri[0];
        float ny = e1z * e2x - e1x * e2z;
        float nz = e1x * e2y - e1y * e2x;
        return nx * tri[0] + ny * tri[1] + nz * tri[2] >= 0.0f;
    }

    private boolean projectAndDraw(float[] tri, Mat4 proj, float ndl, int baseColor) {
        Vec4 p0 = proj.transform(new Vec4(tri[0], tri[1], tri[2], 1.0f));
        Vec4 p1 = proj.transform(new Vec4(tri[3], tri[4], tri[5], 1.0f));
        Vec4 p2 = proj.transform(new Vec4(tri[6], tri[7], tri[8], 1.0f));
        if (p0.w() <= 0.0f || p1.w() <= 0.0f || p2.w() <= 0.0f) {
            return false;
        }
        Vec3 n0 = p0.perspectiveDivide();
        Vec3 n1 = p1.perspectiveDivide();
        Vec3 n2 = p2.perspectiveDivide();
        float sx0 = (n0.x() * 0.5f + 0.5f) * (float)this.fb.width();
        float sy0 = (1.0f - (n0.y() * 0.5f + 0.5f)) * (float)this.fb.height();
        float sx1 = (n1.x() * 0.5f + 0.5f) * (float)this.fb.width();
        float sy1 = (1.0f - (n1.y() * 0.5f + 0.5f)) * (float)this.fb.height();
        float sx2 = (n2.x() * 0.5f + 0.5f) * (float)this.fb.width();
        float sy2 = (1.0f - (n2.y() * 0.5f + 0.5f)) * (float)this.fb.height();
        float d0 = n0.z() * 0.5f + 0.5f;
        float d1 = n1.z() * 0.5f + 0.5f;
        float d2 = n2.z() * 0.5f + 0.5f;
        if (this.mode == Mode.WIREFRAME) {
            Rasterizer.drawTriangleWireframe(this.fb, Math.round(sx0), Math.round(sy0), d0, Math.round(sx1), Math.round(sy1), d1, Math.round(sx2), Math.round(sy2), d2, this.wireColor);
        } else {
            int shaded = Rasterizer.shade(baseColor, ndl);
            Rasterizer.drawTriangleFilled(this.fb, sx0, sy0, d0, sx1, sy1, d1, sx2, sy2, d2, shaded);
        }
        return true;
    }

    public static enum Mode {
        WIREFRAME,
        SOLID;

    }

    public record Instance(Mesh mesh, Transform transform) {
        public Instance(Mesh mesh) {
            this(mesh, Transform.IDENTITY);
        }
    }
}
