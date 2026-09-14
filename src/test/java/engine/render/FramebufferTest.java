package engine.render;

import engine.render.Framebuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FramebufferTest {
    FramebufferTest() {
    }

    @Test
    void clearFillsColorAndResetsDepth() {
        Framebuffer fb = new Framebuffer(4, 4);
        fb.setPixel(1, 1, 0.1f, 0xFF0000);
        fb.clear(0x112233);
        Assertions.assertEquals((int)0x112233, (int)fb.getPixel(1, 1));
        Assertions.assertEquals((float)Float.POSITIVE_INFINITY, (float)fb.getDepth(0, 0), (float)0.0f);
    }

    @Test
    void zBufferKeepsNearest() {
        Framebuffer fb = new Framebuffer(4, 4);
        fb.clear(0);
        Assertions.assertTrue((boolean)fb.setPixel(2, 2, 0.8f, 0xFF0000));
        Assertions.assertFalse((boolean)fb.setPixel(2, 2, 0.9f, 65280));
        Assertions.assertTrue((boolean)fb.setPixel(2, 2, 0.2f, 255));
        Assertions.assertEquals((int)255, (int)fb.getPixel(2, 2));
    }

    @Test
    void outOfBoundsIsIgnored() {
        Framebuffer fb = new Framebuffer(4, 4);
        fb.clear(0);
        Assertions.assertFalse((boolean)fb.setPixel(-1, 0, 0.1f, 0xFFFFFF));
        Assertions.assertFalse((boolean)fb.setPixel(4, 0, 0.1f, 0xFFFFFF));
        Assertions.assertFalse((boolean)fb.setPixel(0, 99, 0.1f, 0xFFFFFF));
    }
}
