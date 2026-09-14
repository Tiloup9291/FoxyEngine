package engine.math;

import engine.math.Vec2;
import engine.math.Vec3;
import engine.math.Vec4;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VecTest {
    VecTest() {
    }

    @Test
    void vec3AddSub() {
        Vec3 a = new Vec3(1.0f, 2.0f, 3.0f);
        Vec3 b = new Vec3(4.0f, -1.0f, 0.5f);
        Assertions.assertEquals((Object)new Vec3(5.0f, 1.0f, 3.5f), (Object)a.add(b));
        Assertions.assertEquals((Object)new Vec3(-3.0f, 3.0f, 2.5f), (Object)a.sub(b));
    }

    @Test
    void vec3DotCross() {
        Assertions.assertEquals((float)0.0f, (float)Vec3.UNIT_X.dot(Vec3.UNIT_Y));
        Assertions.assertEquals((Object)Vec3.UNIT_Z, (Object)Vec3.UNIT_X.cross(Vec3.UNIT_Y));
    }

    @Test
    void vec3Normalize() {
        Vec3 n = new Vec3(0.0f, 3.0f, 4.0f).normalize();
        Assertions.assertEquals((float)1.0f, (float)n.length(), (float)1.0E-5f);
        Assertions.assertEquals((Object)Vec3.ZERO, (Object)Vec3.ZERO.normalize());
    }

    @Test
    void vec2Ops() {
        Assertions.assertEquals((float)5.0f, (float)new Vec2(3.0f, 4.0f).length(), (float)1.0E-5f);
        Assertions.assertEquals((Object)new Vec2(4.0f, 6.0f), (Object)new Vec2(1.0f, 2.0f).add(new Vec2(3.0f, 4.0f)));
    }

    @Test
    void vec4PerspectiveDivide() {
        Vec3 p = new Vec4(2.0f, 4.0f, 6.0f, 2.0f).perspectiveDivide();
        Assertions.assertEquals((Object)new Vec3(1.0f, 2.0f, 3.0f), (Object)p);
    }
}
