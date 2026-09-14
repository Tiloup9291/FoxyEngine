package engine.physics;

import static org.junit.jupiter.api.Assertions.*;
import engine.math.Vec3;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Broadphase grid + raycast slab test. */
class BroadphaseRaycastTest {

    @Test
    void gridFiltersFarBoxes() {
        Broadphase bp = new Broadphase(2f);
        bp.rebuild(java.util.List.of(
                Collider.Box.centered(new Vec3(0, 0, 0), new Vec3(1, 1, 1)),
                Collider.Box.centered(new Vec3(50, 0, 0), new Vec3(1, 1, 1))));
        Set<Integer> out = new HashSet<>();
        bp.query(Collider.Box.centered(new Vec3(0.5f, 0, 0), Vec3.ONE).aabb(), out);
        assertTrue(out.contains(0));
        assertFalse(out.contains(1));
    }

    @Test
    void emptyGridAfterClear() {
        Broadphase bp = new Broadphase(2f);
        bp.rebuild(java.util.List.of(Collider.Box.centered(Vec3.ZERO, Vec3.ONE)));
        assertTrue(bp.cellCount() > 0);
        bp.rebuild(java.util.List.of());
        assertEquals(0, bp.cellCount());
    }

    @Test
    void rayHitsBox() {
        var boxes = java.util.List.of(Collider.Box.centered(new Vec3(5, 0, 0), new Vec3(2, 2, 2)));
        var hit = Raycast.cast(Ray.of(new Vec3(0, 0, 0), new Vec3(1, 0, 0)), boxes, java.util.List.of());
        assertTrue(hit.distance() > 3.5f && hit.distance() < 4.5f);
    }

    @Test
    void rayMisses() {
        var boxes = java.util.List.of(Collider.Box.centered(new Vec3(5, 0, 0), new Vec3(2, 2, 2)));
        var hit = Raycast.cast(Ray.of(new Vec3(0, 0, 0), new Vec3(0, 1, 0)), boxes, java.util.List.of());
        assertEquals(RayHit.MISS.distance(), hit.distance(), 1e-6f);
    }
}
