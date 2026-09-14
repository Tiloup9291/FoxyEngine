package engine.pipeline;

import engine.pipeline.ProjectionStage;
import engine.pipeline.SceneSnapshot;
import engine.render.Framebuffer;
import engine.render.Material;
import engine.render.PhongRaster;
import engine.render.Rasterizer;
import engine.render.Texture;
import engine.render.TextureCache;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public final class RenderStage {
    private RenderStage() {
    }

    private static final ThreadLocal<Framebuffer> SCRATCH = new ThreadLocal<Framebuffer>();

    private static Framebuffer scratchFor(int w, int h) {
        Framebuffer fb = SCRATCH.get();
        if (fb == null || fb.width() != w || fb.height() != h) {
            fb = new Framebuffer(w, h);
            SCRATCH.set(fb);
        }
        return fb;
    }

    private static boolean overlapsBand(float sy0, float sy1, float sy2, int y0, int y1) {
        float mn = Math.min(sy0, Math.min(sy1, sy2));
        float mx = Math.max(sy0, Math.max(sy1, sy2));
        return mx >= (float) y0 - 1.0f && mn < (float) y1 + 1.0f;
    }

    public static RenderStats run(SceneSnapshot snap, ProjectionStage.ProjectionResult pr, Framebuffer fb, TextureCache textures, ExecutorService pool, int bands) throws Exception {
        List<ProjectionStage.DrawCommand> solids = pr.solids();
        List<ProjectionStage.DrawCommand> wires = pr.wires();
        if (bands <= 1 || pool == null) {
            fb.clear(snap.clearColor());
            PhongRaster.setCamera(snap.camera().position());
            for (ProjectionStage.DrawCommand c : wires) {
                RenderStage.drawWire(fb, c, snap);
            }
            for (ProjectionStage.DrawCommand c : solids) {
                RenderStage.drawSolid(fb, c, snap, textures);
            }
            return new RenderStats(pr.drawn(), RenderStage.countNonClear(fb, snap.clearColor()));
        }
        int h = fb.height();
        int n = Math.max(1, Math.min(bands, h));
        ArrayList<Callable<BandResult>> tasks = new ArrayList<Callable<BandResult>>(n);
        for (int i = 0; i < n; ++i) {
            int y0 = h * i / n;
            int y1 = h * (i + 1) / n;
            tasks.add(() -> RenderStage.renderBand(snap, solids, wires, textures, y0, y1));
        }
        List futures = pool.invokeAll(tasks);
        int pixels = 0;
        for (int i = 0; i < n; ++i) {
            BandResult b = (BandResult) ((java.util.concurrent.Future) futures.get(i)).get();
            RenderStage.blit(fb, b);
            pixels += b.painted();
        }
        return new RenderStats(pr.drawn(), pixels);
    }

    private static BandResult renderBand(SceneSnapshot snap, List<ProjectionStage.DrawCommand> solids, List<ProjectionStage.DrawCommand> wires, TextureCache textures, int y0, int y1) {
        Framebuffer local = RenderStage.scratchFor(snap.fbWidth(), snap.fbHeight());
        local.clear(snap.clearColor());
        PhongRaster.setCamera(snap.camera().position());
        for (ProjectionStage.DrawCommand c : wires) {
            if (!overlapsBand(c.sy0(), c.sy1(), c.sy2(), y0, y1)) {
                continue;
            }
            RenderStage.drawWire(local, c, snap);
        }
        for (ProjectionStage.DrawCommand c : solids) {
            if (!overlapsBand(c.sy0(), c.sy1(), c.sy2(), y0, y1)) {
                continue;
            }
            RenderStage.drawSolid(local, c, snap, textures);
        }
        int w = snap.fbWidth();
        int[] src = local.pixels();
        int[] bandPx = new int[(y1 - y0) * w];
        float[] srcD = local.depth();
        float[] bandD = new float[(y1 - y0) * w];
        int painted = 0;
        int clear = snap.clearColor() & 0xFFFFFF;
        for (int y = y0; y < y1; ++y) {
            System.arraycopy(src, y * w, bandPx, (y - y0) * w, w);
            System.arraycopy(srcD, y * w, bandD, (y - y0) * w, w);
            for (int x = 0; x < w; ++x) {
                if ((src[y * w + x] & 0xFFFFFF) == clear) continue;
                ++painted;
            }
        }
        return new BandResult(y0, y1, w, bandPx, bandD, painted);
    }

    private static void blit(Framebuffer fb, BandResult b) {
        System.arraycopy(b.pixels(), 0, fb.pixels(), b.y0() * b.width(), b.pixels().length);
        System.arraycopy(b.depth(), 0, fb.depth(), b.y0() * b.width(), b.depth().length);
    }

    private static int countNonClear(Framebuffer fb, int clear) {
        int c = clear & 0xFFFFFF;
        int n = 0;
        for (int px : fb.pixels()) {
            if ((px & 0xFFFFFF) == c) continue;
            ++n;
        }
        return n;
    }

    private static void drawWire(Framebuffer fb, ProjectionStage.DrawCommand c, SceneSnapshot snap) {
        Rasterizer.drawTriangleWireframe(fb, Math.round(c.sx0()), Math.round(c.sy0()), c.z0(), Math.round(c.sx1()), Math.round(c.sy1()), c.z1(), Math.round(c.sx2()), Math.round(c.sy2()), c.z2(), snap.wireColor());
    }

    private static void drawSolid(Framebuffer fb, ProjectionStage.DrawCommand c, SceneSnapshot snap, TextureCache textures) {
        Material mat = c.material() != null ? c.material() : Material.DEFAULT;
        Texture tex = textures != null && mat.hasTexture() ? textures.get(mat.mapKd()) : null;
        PhongRaster.drawTriangle(fb, c.sx0(), c.sy0(), c.w0(), c.z0(), c.a0(), c.sx1(), c.sy1(), c.w1(), c.z1(), c.a1(), c.sx2(), c.sy2(), c.w2(), c.z2(), c.a2(), mat, tex, snap.filter(), snap.light(), snap.ambient(), 0.0f, 0.0f, 10.0f);
    }

    public record RenderStats(int triangles, int pixels) {
    }

    private record BandResult(int y0, int y1, int width, int[] pixels, float[] depth, int painted) {
    }
}
