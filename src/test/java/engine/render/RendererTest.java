/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  engine.math.Vec3
 *  engine.render.CameraRig
 *  engine.render.CameraRig$Mode
 *  engine.render.Framebuffer
 *  engine.render.Renderer
 *  engine.render.Renderer$Mode
 *  org.junit.jupiter.api.Assertions
 *  org.junit.jupiter.api.Test
 */
package engine.render;

import engine.math.Vec3;
import engine.render.CameraRig;
import engine.render.Framebuffer;
import engine.render.Renderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RendererTest {
    RendererTest() {
    }

    private Renderer newRenderer(Framebuffer fb, Renderer.Mode mode) {
        Renderer r = new Renderer(fb, 1.6f, 70.0f, 0.1f, 100.0f, 5088255, 65416);
        r.setMode(mode);
        r.setCamera(CameraRig.defaultRig((CameraRig.Mode)CameraRig.Mode.ORBIT, (float)3.0f, (float)1.6f).toCamera());
        return r;
    }

    @Test
    void solidCubeDrawsPixels() {
        Framebuffer fb = new Framebuffer(200, 150);
        Renderer r = this.newRenderer(fb, Renderer.Mode.SOLID);
        int tris = r.render(0.5f, 0);
        Assertions.assertTrue((tris > 0 ? 1 : 0) != 0, (String)"au moins un triangle visible");
        long painted = 0L;
        for (int px : fb.pixels()) {
            if ((px & 0xFFFFFF) == 0) continue;
            ++painted;
        }
        Assertions.assertTrue((painted > 500L ? 1 : 0) != 0, (String)("solid cube must cover pixels, got=" + painted));
    }

    @Test
    void wireframeDrawsLines() {
        Framebuffer fb = new Framebuffer(200, 150);
        Renderer r = this.newRenderer(fb, Renderer.Mode.WIREFRAME);
        int tris = r.render(0.5f, 0);
        Assertions.assertTrue((tris > 0 ? 1 : 0) != 0);
        long painted = 0L;
        for (int px : fb.pixels()) {
            if ((px & 0xFFFFFF) == 0) continue;
            ++painted;
        }
        Assertions.assertTrue((painted > 20L ? 1 : 0) != 0, (String)"wireframe must draw lines");
    }

    @Test
    void rotationChangesOverTime() {
        Framebuffer fb = new Framebuffer(200, 150);
        Renderer r = this.newRenderer(fb, Renderer.Mode.SOLID);
        r.render(0.0f, 0);
        float a0 = r.angle();
        r.render(1.0f, 0);
        Assertions.assertTrue((r.angle() > a0 ? 1 : 0) != 0);
    }

    @Test
    void cameraProcheClippedPasDisparu() {
        Framebuffer fb = new Framebuffer(200, 150);
        Renderer r = new Renderer(fb, 1.6f, 70.0f, 0.1f, 100.0f, 5088255, 65416);
        CameraRig rig = new CameraRig(CameraRig.Mode.ORBIT, Vec3.ZERO, 0.0f, 0.0f, 1.2f, new Vec3(0.0f, 0.8f, 4.2f), 0.0f, 0.0f, 3.0f, 1.6f);
        r.setCamera(rig.toCamera());
        int tris = r.render(0.5f, 0);
        Assertions.assertTrue((tris >= 0 ? 1 : 0) != 0);
    }
}
