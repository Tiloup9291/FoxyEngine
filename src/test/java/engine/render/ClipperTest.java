package engine.render;

import engine.render.Clipper;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClipperTest {
    ClipperTest() {
    }

    private static float[] v(float x, float y, float z) {
        return new float[]{x, y, z};
    }

    @Test
    void triangleDevantIntact() {
        List<float[]> out = Clipper.clipTriangleNear((float[])ClipperTest.v(-1.0f, 0.0f, -5.0f), (float[])ClipperTest.v(1.0f, 0.0f, -5.0f), (float[])ClipperTest.v(0.0f, 1.0f, -5.0f), (float)0.1f);
        Assertions.assertEquals((int)1, (int)out.size());
    }

    @Test
    void triangleDerriereRejete() {
        List<float[]> out = Clipper.clipTriangleNear((float[])ClipperTest.v(-1.0f, 0.0f, 1.0f), (float[])ClipperTest.v(1.0f, 0.0f, 1.0f), (float[])ClipperTest.v(0.0f, 1.0f, 1.0f), (float)0.1f);
        Assertions.assertTrue((boolean)out.isEmpty());
    }

    @Test
    void triangleTraversantDecoupe() {
        List<float[]> out = Clipper.clipTriangleNear((float[])ClipperTest.v(-1.0f, 0.0f, -5.0f), (float[])ClipperTest.v(1.0f, 0.0f, -5.0f), (float[])ClipperTest.v(0.0f, 0.0f, 1.0f), (float)0.1f);
        Assertions.assertEquals((int)2, (int)out.size());
        for (float[] tri : out) {
            Assertions.assertTrue((tri[2] <= -0.09999f ? 1 : 0) != 0);
            Assertions.assertTrue((tri[5] <= -0.09999f ? 1 : 0) != 0);
            Assertions.assertTrue((tri[8] <= -0.09999f ? 1 : 0) != 0);
        }
    }

    @Test
    void unSommetDevantUnTriangle() {
        List<float[]> out = Clipper.clipTriangleNear((float[])ClipperTest.v(0.0f, 0.0f, -5.0f), (float[])ClipperTest.v(1.0f, 0.0f, 1.0f), (float[])ClipperTest.v(-1.0f, 0.0f, 1.0f), (float)0.1f);
        Assertions.assertEquals((int)1, (int)out.size());
    }
}
