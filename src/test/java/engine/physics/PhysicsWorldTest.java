package engine.physics;

import static org.junit.jupiter.api.Assertions.*;
import engine.math.Vec3;
import org.junit.jupiter.api.Test;

/** PhysicsWorld: gravity, ground, bounce, determinism. */
class PhysicsWorldTest {

    private static PhysicsWorld world() {
        PhysicsWorld w = new PhysicsWorld();
        w.setGravity(new Vec3(0, -9.81f, 0));
        w.setGround(0f, true);
        return w;
    }

    @Test
    void fallsOntoGround() {
        PhysicsWorld w = world();
        Body b = Body.cube(new Vec3(0, 5, 0), 1f, 0f);
        w.addBody(b);
        for (int i = 0; i < 600; i++) w.step(1f / 60f);
        assertEquals(0.5f, b.position().y(), 1e-3f);
        assertTrue(b.onGround());
    }

    @Test
    void bounceLosesEnergy() {
        PhysicsWorld w = world();
        Body b = Body.cube(new Vec3(0, 5, 0), 1f, 0.8f);
        w.addBody(b);
        boolean bounced = false;
        float peakAfter = 0f;
        for (int i = 0; i < 400; i++) {
            w.step(1f / 60f);
            if (b.onGround()) bounced = true;
            if (bounced && !b.onGround()) peakAfter = Math.max(peakAfter, b.position().y());
        }
        assertTrue(bounced);
        assertTrue(peakAfter < 4.5f, "peak=" + peakAfter);
    }

    @Test
    void wallStopsBody() {
        PhysicsWorld w = world();
        w.setGravity(Vec3.ZERO);
        w.setGround(0f, false);
        w.addStatic(Collider.Box.centered(new Vec3(2, 1, 0), new Vec3(1, 4, 4)));
        Body b = Body.cube(new Vec3(0, 1, 0), 1f, 0f);
        b.setVelocity(new Vec3(10, 0, 0));
        w.addBody(b);
        for (int i = 0; i < 120; i++) w.step(1f / 60f);
        assertTrue(b.position().x() < 1.6f, "x=" + b.position().x());
    }

    @Test
    void deterministic() {
        PhysicsWorld a = world(), b = world();
        Body ba = Body.cube(new Vec3(0, 3, 0), 1f, 0.3f);
        Body bb = Body.cube(new Vec3(0, 3, 0), 1f, 0.3f);
        a.addBody(ba); b.addBody(bb);
        for (int i = 0; i < 120; i++) { a.step(1f / 60f); b.step(1f / 60f); }
        assertEquals(ba.position().x(), bb.position().x(), 1e-6f);
        assertEquals(ba.position().y(), bb.position().y(), 1e-6f);
    }
}
