package engine.math;

import engine.math.AABB;
import engine.math.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AABBTest {
    AABBTest() {
    }

    @Test
    void ofPointsComputesBounds() {
        AABB b = AABB.ofPoints((Vec3[])new Vec3[]{new Vec3(-1.0f, 2.0f, 0.0f), new Vec3(3.0f, -4.0f, 5.0f)});
        Assertions.assertEquals((Object)new Vec3(-1.0f, -4.0f, 0.0f), (Object)b.min());
        Assertions.assertEquals((Object)new Vec3(3.0f, 2.0f, 5.0f), (Object)b.max());
        Assertions.assertEquals((Object)new Vec3(1.0f, -1.0f, 2.5f), (Object)b.center());
    }

    @Test
    void containsAndIntersects() {
        AABB a = AABB.ofPoints((Vec3[])new Vec3[]{Vec3.ZERO, Vec3.ONE});
        AABB b = AABB.ofPoints((Vec3[])new Vec3[]{new Vec3(0.5f, 0.5f, 0.5f), new Vec3(2.0f, 2.0f, 2.0f)});
        AABB c = AABB.ofPoints((Vec3[])new Vec3[]{new Vec3(5.0f, 5.0f, 5.0f), new Vec3(6.0f, 6.0f, 6.0f)});
        Assertions.assertTrue((boolean)a.contains(new Vec3(0.5f, 0.5f, 0.5f)));
        Assertions.assertFalse((boolean)a.contains(new Vec3(2.0f, 2.0f, 2.0f)));
        Assertions.assertTrue((boolean)a.intersects(b));
        Assertions.assertFalse((boolean)a.intersects(c));
    }

    @Test
    void merge() {
        AABB a = AABB.ofPoints((Vec3[])new Vec3[]{Vec3.ZERO, Vec3.ONE});
        AABB b = AABB.ofPoints((Vec3[])new Vec3[]{new Vec3(-2.0f, -2.0f, -2.0f), Vec3.ZERO});
        AABB m = a.merge(b);
        Assertions.assertEquals((Object)new Vec3(-2.0f, -2.0f, -2.0f), (Object)m.min());
        Assertions.assertEquals((Object)Vec3.ONE, (Object)m.max());
    }
}
