package engine.math;

import engine.math.Mat4;
import engine.math.Vec3;
import engine.math.Vec4;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Mat4Test {
    Mat4Test() {
    }

    @Test
    void identityTransformsPoint() {
        Vec3 p = new Vec3(1.0f, 2.0f, 3.0f);
        Assertions.assertEquals((Object)p, (Object)Mat4.identity().transformPoint(p));
    }

    @Test
    void translation() {
        Vec3 p = Mat4.translation((Vec3)new Vec3(1.0f, 2.0f, 3.0f)).transformPoint(Vec3.ZERO);
        Assertions.assertEquals((Object)new Vec3(1.0f, 2.0f, 3.0f), (Object)p);
    }

    @Test
    void scaling() {
        Vec3 p = Mat4.scaling((Vec3)new Vec3(2.0f, 3.0f, 4.0f)).transformPoint(new Vec3(1.0f, 1.0f, 1.0f));
        Assertions.assertEquals((Object)new Vec3(2.0f, 3.0f, 4.0f), (Object)p);
    }

    @Test
    void rotationZ90() {
        Vec3 p = Mat4.rotationZ((float)1.5707964f).transformPoint(Vec3.UNIT_X);
        Assertions.assertEquals((float)0.0f, (float)p.x(), (float)1.0E-5f);
        Assertions.assertEquals((float)1.0f, (float)p.y(), (float)1.0E-5f);
    }

    @Test
    void composeTranslationAfterScaling() {
        Mat4 m = Mat4.translation((Vec3)new Vec3(10.0f, 0.0f, 0.0f)).mul(Mat4.scaling((Vec3)new Vec3(2.0f, 2.0f, 2.0f)));
        Assertions.assertEquals((Object)new Vec3(12.0f, 0.0f, 0.0f), (Object)m.transformPoint(new Vec3(1.0f, 0.0f, 0.0f)));
    }

    @Test
    void perspectiveDividesW() {
        Mat4 proj = Mat4.perspective((float)((float)Math.toRadians(90.0)), (float)1.0f, (float)0.1f, (float)100.0f);
        Vec4 clip = proj.transform(new Vec4(0.0f, 0.0f, -1.0f, 1.0f));
        Assertions.assertNotEquals((float)0.0f, (float)clip.w());
        Vec3 ndc = clip.perspectiveDivide();
        Assertions.assertTrue((ndc.z() > -1.01f && ndc.z() < 1.01f ? 1 : 0) != 0);
    }

    @Test
    void lookAtMovesWorldToCamera() {
        Mat4 view = Mat4.lookAt((Vec3)new Vec3(0.0f, 0.0f, 5.0f), (Vec3)Vec3.ZERO, (Vec3)Vec3.UNIT_Y);
        Vec3 p = view.transformPoint(Vec3.ZERO);
        Assertions.assertEquals((Object)new Vec3(0.0f, 0.0f, -5.0f), (Object)p);
    }
}
