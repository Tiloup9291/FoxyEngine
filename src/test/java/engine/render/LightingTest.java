package engine.render;

import engine.math.Vec3;
import engine.render.Light;
import engine.render.Lighting;
import engine.render.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LightingTest {
    private static final Material MAT = new Material("m", 0xFFFFFF, 1.0f, new Vec3(0.2f, 0.2f, 0.2f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f, null);
    private static final Light SUN = new Light(Vec3.UNIT_Y, new Vec3(1.0f, 1.0f, 1.0f), 1.0f);

    LightingTest() {
    }

    @Test
    void faceEclaireePleine() {
        Vec3 c = Lighting.phong((Vec3)Vec3.UNIT_Y, (Vec3)Vec3.UNIT_Y, (Vec3)Vec3.UNIT_Y, (Vec3)new Vec3(1.0f, 1.0f, 1.0f), (Material)MAT, (Light)SUN, (float)0.2f);
        Assertions.assertTrue((c.x() > 1.0f ? 1 : 0) != 0, (String)("diffus + speculaire : " + c));
    }

    @Test
    void faceOposeeAmbiantSeul() {
        Vec3 c = Lighting.phong((Vec3)new Vec3(0.0f, -1.0f, 0.0f), (Vec3)Vec3.UNIT_Y, (Vec3)Vec3.UNIT_Y, (Vec3)new Vec3(1.0f, 1.0f, 1.0f), (Material)MAT, (Light)SUN, (float)0.2f);
        Assertions.assertEquals((float)0.2f, (float)c.x(), (float)1.0E-4f);
        Assertions.assertEquals((float)0.2f, (float)c.y(), (float)1.0E-4f);
    }

    @Test
    void speculaireDependsBrillance() {
        Material mat = new Material("m", 0xFFFFFF, 1.0f, new Vec3(0.0f, 0.0f, 0.0f), new Vec3(1.0f, 1.0f, 1.0f), 64.0f, null);
        Vec3 n = new Vec3(0.0f, 1.0f, 0.0f);
        Vec3 l = new Vec3(0.0f, 1.0f, 0.0f);
        Vec3 c = Lighting.phong((Vec3)n, (Vec3)new Vec3(0.0f, 1.0f, 0.0f), (Vec3)l, (Vec3)new Vec3(0.0f, 0.0f, 0.0f), (Material)mat, (Light)SUN, (float)0.0f);
        Assertions.assertEquals((float)1.0f, (float)c.x(), (float)1.0E-4f);
        Vec3 c2 = Lighting.phong((Vec3)n, (Vec3)new Vec3(1.0f, 0.0f, 0.0f), (Vec3)l, (Vec3)new Vec3(0.0f, 0.0f, 0.0f), (Material)mat, (Light)SUN, (float)0.0f);
        Assertions.assertTrue((c2.x() < 0.01f ? 1 : 0) != 0, (String)("spec rasant : " + c2));
    }

    @Test
    void rgbRoundTrip() {
        Assertions.assertEquals((int)16744512, (int)Lighting.toRgb((Vec3)Lighting.fromRgb((int)16744512)));
    }
}
