/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  engine.math.Mat4
 *  engine.math.Quaternion
 *  engine.math.Vec3
 *  engine.render.Camera
 *  engine.render.CameraRig
 *  engine.render.CameraRig$Action
 *  engine.render.CameraRig$Mode
 *  engine.render.Transform
 *  org.junit.jupiter.api.Assertions
 *  org.junit.jupiter.api.Test
 */
package engine.render;

import engine.math.Mat4;
import engine.math.Quaternion;
import engine.math.Vec3;
import engine.render.Camera;
import engine.render.CameraRig;
import engine.render.Transform;
import java.util.EnumSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TransformCameraTest {
    TransformCameraTest() {
    }

    @Test
    void identityNeBougeRien() {
        Vec3 p = new Vec3(1.0f, 2.0f, 3.0f);
        Assertions.assertEquals((Object)p, (Object)Transform.IDENTITY.modelMatrix().transformPoint(p));
    }

    @Test
    void translationPuisEchelle() {
        Transform t = new Transform(new Vec3(1.0f, 0.0f, 0.0f), Quaternion.IDENTITY, new Vec3(2.0f, 2.0f, 2.0f));
        Vec3 r = t.modelMatrix().transformPoint(new Vec3(1.0f, 0.0f, 0.0f));
        Assertions.assertEquals((float)3.0f, (float)r.x(), (float)1.0E-5f);
    }

    @Test
    void rotation90DegY() {
        Transform t = new Transform(Vec3.ZERO, Quaternion.fromAxisAngle((Vec3)Vec3.UNIT_Y, (float)1.5707964f), new Vec3(1.0f, 1.0f, 1.0f));
        Vec3 r = t.modelMatrix().transformPoint(Vec3.UNIT_Z);
        Assertions.assertEquals((float)1.0f, (float)r.x(), (float)1.0E-5f);
        Assertions.assertEquals((float)0.0f, (float)r.z(), (float)1.0E-4f);
    }

    @Test
    void orbitDefautViseOrigine() {
        CameraRig rig = CameraRig.defaultRig((CameraRig.Mode)CameraRig.Mode.ORBIT, (float)3.0f, (float)1.6f);
        Camera cam = rig.toCamera();
        Assertions.assertEquals((Object)Vec3.ZERO, (Object)cam.target());
        Assertions.assertTrue((cam.position().distance(Vec3.ZERO) > 1.0f ? 1 : 0) != 0);
    }

    @Test
    void orbitTourneAvecFleches() {
        CameraRig rig = CameraRig.defaultRig((CameraRig.Mode)CameraRig.Mode.ORBIT, (float)3.0f, (float)1.6f);
        float yaw0 = rig.yaw();
        rig.update(1.0f, EnumSet.of(CameraRig.Action.TURN_L));
        Assertions.assertEquals((float)(yaw0 + 1.6f), (float)rig.yaw(), (float)1.0E-5f);
    }

    @Test
    void fpsAvance() {
        CameraRig rig = CameraRig.defaultRig((CameraRig.Mode)CameraRig.Mode.FPS, (float)3.0f, (float)1.6f);
        Vec3 p0 = rig.fpsPos();
        rig.update(1.0f, EnumSet.of(CameraRig.Action.FWD));
        Assertions.assertTrue((rig.fpsPos().z() < p0.z() ? 1 : 0) != 0);
    }

    @Test
    void toggleEtReset() {
        CameraRig rig = CameraRig.defaultRig((CameraRig.Mode)CameraRig.Mode.ORBIT, (float)3.0f, (float)1.6f);
        rig.toggleMode();
        Assertions.assertEquals((Object)CameraRig.Mode.FPS, (Object)rig.mode());
        rig.update(1.0f, EnumSet.of(CameraRig.Action.FWD));
        rig.reset();
        rig.setMode(CameraRig.Mode.ORBIT);
        Assertions.assertEquals((float)0.0f, (float)rig.yaw(), (float)1.0E-5f);
    }

    @Test
    void matriceVueRegardeCible() {
        Camera cam = new Camera(new Vec3(0.0f, 0.0f, 5.0f), Vec3.ZERO, Vec3.UNIT_Y);
        Vec3 atOrigin = cam.viewMatrix().transformPoint(Vec3.ZERO);
        Assertions.assertEquals((float)0.0f, (float)atOrigin.x(), (float)1.0E-4f);
        Assertions.assertEquals((float)0.0f, (float)atOrigin.y(), (float)1.0E-4f);
        Assertions.assertTrue((atOrigin.z() < 0.0f ? 1 : 0) != 0);
        Assertions.assertEquals((float)-5.0f, (float)atOrigin.z(), (float)1.0E-4f);
    }

    @Test
    void modelMatrixComposee() {
        Mat4 m = Mat4.translation((Vec3)new Vec3(0.0f, 1.0f, 0.0f)).mul(Mat4.rotationY((float)((float)Math.PI)));
        Vec3 r = m.transformPoint(new Vec3(1.0f, 0.0f, 0.0f));
        Assertions.assertEquals((float)-1.0f, (float)r.x(), (float)1.0E-4f);
        Assertions.assertEquals((float)1.0f, (float)r.y(), (float)1.0E-4f);
    }
}
