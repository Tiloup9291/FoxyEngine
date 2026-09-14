package engine.render;

import engine.render.Framebuffer;
import engine.render.Rasterizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RasterizerTest {
    RasterizerTest() {
    }

    @Test
    void horizontalLineDrawsEveryPixel() {
        Framebuffer fb = new Framebuffer(10, 10);
        fb.clear(0);
        Rasterizer.drawLine((Framebuffer)fb, (int)1, (int)5, (float)0.5f, (int)8, (int)5, (float)0.5f, (int)0xFFFFFF);
        for (int x = 1; x <= 8; ++x) {
            Assertions.assertEquals((int)0xFFFFFF, (int)fb.getPixel(x, 5));
        }
        Assertions.assertEquals((int)0, (int)fb.getPixel(0, 5));
    }

    @Test
    void filledTriangleCoversItsCenter() {
        Framebuffer fb = new Framebuffer(20, 20);
        fb.clear(0);
        Rasterizer.drawTriangleFilled((Framebuffer)fb, (float)2.0f, (float)2.0f, (float)0.5f, (float)17.0f, (float)2.0f, (float)0.5f, (float)9.0f, (float)17.0f, (float)0.5f, (int)0xFF0000);
        Assertions.assertEquals((int)0xFF0000, (int)fb.getPixel(9, 7));
        Assertions.assertEquals((int)0, (int)fb.getPixel(0, 19));
    }

    @Test
    void depthWinsBetweenOverlappingTriangles() {
        Framebuffer fb = new Framebuffer(20, 20);
        fb.clear(0);
        Rasterizer.drawTriangleFilled((Framebuffer)fb, (float)2.0f, (float)2.0f, (float)0.9f, (float)17.0f, (float)2.0f, (float)0.9f, (float)9.0f, (float)17.0f, (float)0.9f, (int)0xFF0000);
        Rasterizer.drawTriangleFilled((Framebuffer)fb, (float)2.0f, (float)2.0f, (float)0.2f, (float)17.0f, (float)2.0f, (float)0.2f, (float)9.0f, (float)17.0f, (float)0.2f, (int)65280);
        Assertions.assertEquals((int)65280, (int)fb.getPixel(9, 7));
    }

    @Test
    void degenerateTriangleDoesNotCrash() {
        Framebuffer fb = new Framebuffer(10, 10);
        fb.clear(0);
        Assertions.assertDoesNotThrow(() -> Rasterizer.drawTriangleFilled((Framebuffer)fb, (float)5.0f, (float)5.0f, (float)0.5f, (float)5.0f, (float)5.0f, (float)0.5f, (float)5.0f, (float)5.0f, (float)0.5f, (int)0xFFFFFF));
    }
}
