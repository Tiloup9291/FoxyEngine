package engine.fx;

import static org.junit.jupiter.api.Assertions.*;
import engine.math.Vec3;
import engine.render.Framebuffer;
import org.junit.jupiter.api.Test;

/** Post-process (vignette/flash/fade) + FXThread (transition). */
class PostProcessTest {

    @Test
    void vignetteDarkensCorners() {
        Framebuffer fb = new Framebuffer(100, 100);
        fb.clear(0x808080);
        PostProcess pp = new PostProcess();
        pp.setVignette(1f);
        pp.apply(fb);
        int center = fb.getPixel(50, 50);
        int corner = fb.getPixel(0, 0);
        assertTrue((corner & 0xFF) < (center & 0xFF), "corner=" + corner + " center=" + center);
    }

    @Test
    void flashDecays() {
        Framebuffer fb = new Framebuffer(32, 32);
        fb.clear(0x000000);
        PostProcess pp = new PostProcess();
        pp.flash(1f, 0xFFFFFF);
        pp.apply(fb);
        assertEquals(0xFFFFFF, fb.getPixel(16, 16));
        pp.update(1f);
        assertEquals(0f, pp.flash(), 1e-6f);
        pp.apply(fb);
        assertTrue(fb.getPixel(16, 16) != 0);
    }

    @Test
    void fadeToBlack() {
        Framebuffer fb = new Framebuffer(16, 16);
        fb.clear(0xFFFFFF);
        PostProcess pp = new PostProcess();
        pp.setFade(1f);
        pp.apply(fb);
        assertEquals(0, fb.getPixel(8, 8));
        pp.setFade(0.5f);
        fb.clear(0xFFFFFF);
        pp.apply(fb);
        int c = fb.getPixel(8, 8);
        int r = (c >> 16) & 0xFF;
        assertTrue(r > 50 && r < 200, "r=" + r);
    }

    @Test
    void transitionRunsMidAction() {
        FXThread fx = new FXThread(64, 7L);
        boolean[] mid = {false};
        fx.transition(() -> mid[0] = true);
        assertTrue(fx.fading());
        for (int i = 0; i < 70; i++) fx.update(1f / 60f);
        assertTrue(mid[0]);
        assertEquals(0f, fx.post().fade(), 1e-5f);
    }

    @Test
    void gameplayTriggers() {
        FXThread fx = new FXThread(256, 1L);
        Vec3 p = new Vec3(0, 1, 0);
        fx.explode(p);
        assertEquals(60, fx.pool().aliveCount());
        assertTrue(fx.post().flash() > 0.3f);
        fx.footstep(p);
        fx.jump(p);
        fx.land(p);
        assertEquals(60 + 4 + 12 + 10, fx.pool().aliveCount());
    }
}
