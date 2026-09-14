package engine.fx;

import engine.fx.Particle;
import engine.fx.ParticlePool;
import engine.math.Mat4;
import engine.math.Vec3;
import engine.math.Vec4;
import engine.render.Camera;
import engine.render.Framebuffer;

public final class ParticleRenderer {
    private ParticleRenderer() {
    }

    public static int render(ParticlePool pool, Framebuffer fb, Camera camera, float fovYRad, int clearColor) {
        if (pool.isEmpty()) {
            return 0;
        }
        Mat4 view = camera.viewMatrix();
        float aspect = (float)fb.width() / (float)fb.height();
        Mat4 proj = Mat4.perspective(fovYRad, aspect, 0.1f, 100.0f);
        float focal = (float)fb.height() / (2.0f * (float)Math.tan(fovYRad / 2.0f));
        Vec3 camPos = camera.position();
        int drawn = 0;
        for (int i = 0; i < pool.aliveCount(); ++i) {
            Particle p = pool.get(i);
            Vec4 vc = view.transform(new Vec4(p.pos.x(), p.pos.y(), p.pos.z(), 1.0f));
            float cx = vc.x();
            float cy = vc.y();
            float cz = vc.z();
            if (cz > -0.15f) continue;
            float depth = -cz;
            Vec4 clip = proj.transform(new Vec4(cx, cy, cz, 1.0f));
            if (clip.w() < 1.0E-6f) continue;
            float ndcX = clip.x() / clip.w();
            float ndcY = clip.y() / clip.w();
            if (ndcX < -1.2f || ndcX > 1.2f || ndcY < -1.2f || ndcY > 1.2f) continue;
            int sx = (int)((ndcX * 0.5f + 0.5f) * (float)fb.width());
            int sy = (int)((1.0f - (ndcY * 0.5f + 0.5f)) * (float)fb.height());
            float shrink = 0.4f + 0.6f * p.lifeFrac();
            int r = Math.max(1, (int)(p.size * shrink * focal / depth));
            r = Math.min(r, 64);
            int col = ParticleRenderer.fade(p.colorRgb, clearColor, 1.0f - p.lifeFrac() * 0.85f);
            boolean any = false;
            for (int y = sy - r; y <= sy + r; ++y) {
                for (int x = sx - r; x <= sx + r; ++x) {
                    if (!fb.setPixel(x, y, depth, col)) continue;
                    any = true;
                }
            }
            if (!any) continue;
            ++drawn;
        }
        return drawn;
    }

    static int fade(int src, int dst, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int sr = src >> 16 & 0xFF;
        int sg = src >> 8 & 0xFF;
        int sb = src & 0xFF;
        int dr = dst >> 16 & 0xFF;
        int dg = dst >> 8 & 0xFF;
        int db = dst & 0xFF;
        int r = (int)((float)sr + (float)(dr - sr) * t);
        int g = (int)((float)sg + (float)(dg - sg) * t);
        int b = (int)((float)sb + (float)(db - sb) * t);
        return r << 16 | g << 8 | b;
    }
}
