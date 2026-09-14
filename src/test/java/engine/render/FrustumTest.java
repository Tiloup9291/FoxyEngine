package engine.render;

import engine.math.AABB;
import engine.math.Mat4;
import engine.math.Vec3;
import engine.render.Frustum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FrustumTest {
    FrustumTest() {
    }

    private static Frustum canonical() {
        Mat4 view = Mat4.lookAt((Vec3)new Vec3(0.0f, 0.8f, 4.2f), (Vec3)Vec3.ZERO, (Vec3)Vec3.UNIT_Y);
        Mat4 proj = Mat4.perspective((float)((float)Math.toRadians(70.0)), (float)1.3333334f, (float)0.1f, (float)100.0f);
        return Frustum.fromViewProjection((Mat4)proj.mul(view));
    }

    @Test
    void pointDevantCameraVisible() {
        Assertions.assertTrue((boolean)FrustumTest.canonical().isVisible(Vec3.ZERO));
    }

    @Test
    void pointDerriereCameraInvisible() {
        Assertions.assertFalse((boolean)FrustumTest.canonical().isVisible(new Vec3(0.0f, 0.8f, 10.0f)));
    }

    @Test
    void aabbCulledDerriereCamera() {
        AABB box = new AABB(new Vec3(-0.5f, -0.5f, 8.0f), new Vec3(0.5f, 0.5f, 9.0f));
        Assertions.assertFalse((boolean)FrustumTest.canonical().isVisible(box));
    }

    @Test
    void aabbVisibleDevantCamera() {
        AABB box = new AABB(new Vec3(-0.5f, -0.5f, -0.5f), new Vec3(0.5f, 0.5f, 0.5f));
        Assertions.assertTrue((boolean)FrustumTest.canonical().isVisible(box));
    }

    @Test
    void transformAABBTranslation() {
        AABB box = new AABB(new Vec3(-1.0f, -1.0f, -1.0f), new Vec3(1.0f, 1.0f, 1.0f));
        AABB moved = Frustum.transformAABB((AABB)box, (Mat4)Mat4.translation((Vec3)new Vec3(5.0f, 0.0f, 0.0f)));
        Assertions.assertEquals((float)4.0f, (float)moved.min().x(), (float)1.0E-5f);
        Assertions.assertEquals((float)6.0f, (float)moved.max().x(), (float)1.0E-5f);
        Assertions.assertEquals((float)-1.0f, (float)moved.min().y(), (float)1.0E-5f);
    }
}
