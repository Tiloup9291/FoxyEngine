package engine.physics;

import static org.junit.jupiter.api.Assertions.*;
import engine.math.Vec3;
import org.junit.jupiter.api.Test;

/** Narrow check: box/box, sphere/sphere, sphere/box + contacts. */
class ColliderTest {

    @Test
    void boxBoxOverlap() {
        var a = Collider.Box.centered(Vec3.ZERO, new Vec3(2, 2, 2));
        var b = Collider.Box.centered(new Vec3(1, 0, 0), new Vec3(2, 2, 2));
        assertTrue(Collider.overlaps(a, b));
        assertFalse(Collider.overlaps(a, Collider.Box.centered(new Vec3(5, 0, 0), Vec3.ONE)));
    }

    @Test
    void sphereBoxOverlap() {
        var box = Collider.Box.centered(Vec3.ZERO, new Vec3(2, 2, 2));
        assertTrue(Collider.overlaps(new Collider.Sphere(new Vec3(1.5f, 0, 0), 1f), box));
        assertFalse(Collider.overlaps(new Collider.Sphere(new Vec3(5f, 0, 0), 1f), box));
        assertTrue(Collider.overlaps(new Collider.Sphere(Vec3.ZERO, 0.1f), box));
    }

    @Test
    void boxVsBoxNormalMinPenetration() {
        var a = Collider.Box.centered(new Vec3(0.9f, 0, 0), new Vec3(2, 2, 2));
        var b = Collider.Box.centered(Vec3.ZERO, new Vec3(2, 2, 2));
        Contact c = new Contact();
        assertTrue(Contact.boxVsBox(a, b, c));
        assertEquals(1f, c.normal.x(), 1e-5f);
        assertEquals(1.1f, c.penetration, 1e-5f);
        assertFalse(Contact.boxVsBox(
                Collider.Box.centered(new Vec3(10, 0, 0), Vec3.ONE), b, new Contact()));
    }

    @Test
    void sphereVsBoxNormalAndPenetration() {
        var box = Collider.Box.centered(Vec3.ZERO, new Vec3(2, 2, 2));
        Contact c = new Contact();
        assertTrue(Contact.sphereVsBox(new Collider.Sphere(new Vec3(1.5f, 0, 0), 1f), box, c));
        assertEquals(1f, c.normal.x(), 1e-5f);
        assertEquals(0.5f, c.penetration, 1e-5f);
    }

    @Test
    void sphereVsBoxCenterInside() {
        var box = Collider.Box.centered(Vec3.ZERO, new Vec3(2, 2, 2));
        Contact c = new Contact();
        assertTrue(Contact.sphereVsBox(new Collider.Sphere(Vec3.ZERO, 0.5f), box, c));
        assertTrue(c.penetration > 0.5f);
        assertEquals(1f, Math.abs(c.normal.x() + c.normal.y() + c.normal.z()), 1e-5f);
    }
}
