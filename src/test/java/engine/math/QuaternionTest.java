package engine.math;

import engine.math.Quaternion;
import engine.math.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QuaternionTest {
    QuaternionTest() {
    }

    @Test
    void identityLeavesVectorUnchanged() {
        Assertions.assertEquals((Object)Vec3.UNIT_X, (Object)Quaternion.IDENTITY.rotate(Vec3.UNIT_X));
    }

    @Test
    void rotateZ90() {
        Quaternion q = Quaternion.fromAxisAngle((Vec3)Vec3.UNIT_Z, (float)1.5707964f);
        Vec3 p = q.rotate(Vec3.UNIT_X);
        Assertions.assertEquals((float)0.0f, (float)p.x(), (float)1.0E-5f);
        Assertions.assertEquals((float)1.0f, (float)p.y(), (float)1.0E-5f);
        Assertions.assertEquals((float)0.0f, (float)p.z(), (float)1.0E-5f);
    }

    @Test
    void quaternionMatchesMatrixRotation() {
        Quaternion q = Quaternion.fromAxisAngle((Vec3)Vec3.UNIT_Y, (float)((float)Math.PI));
        Vec3 viaQuat = q.rotate(Vec3.UNIT_X);
        Vec3 viaMat = q.toMatrix().transformDirection(Vec3.UNIT_X);
        Assertions.assertEquals((float)viaMat.x(), (float)viaQuat.x(), (float)1.0E-5f);
        Assertions.assertEquals((float)viaMat.y(), (float)viaQuat.y(), (float)1.0E-5f);
        Assertions.assertEquals((float)viaMat.z(), (float)viaQuat.z(), (float)1.0E-5f);
        Assertions.assertEquals((float)-1.0f, (float)viaQuat.x(), (float)1.0E-5f);
    }
}
