package engine.fx;

import static org.junit.jupiter.api.Assertions.*;
import engine.math.Vec3;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Particle pool: spawn, recycle, gravity, emitters. */
class ParticleTest {

    @Test
    void spawnAndRecycle() {
        ParticlePool pool = new ParticlePool(4);
        assertTrue(pool.isEmpty());
        for (int i = 0; i < 4; i++)
            pool.spawn(Vec3.ZERO, Vec3.ZERO, 5f, 0.1f, 0xFFFFFF, 0f, 0f);
        assertEquals(4, pool.aliveCount());
        pool.spawn(Vec3.ZERO, Vec3.ZERO, 5f, 0.1f, 0xFFFFFF, 0f, 0f);
        assertEquals(4, pool.aliveCount());
    }

    @Test
    void gravityAndExpiry() {
        ParticlePool pool = new ParticlePool(4);
        pool.setWorldGravity(-10f);
        pool.spawn(new Vec3(0, 5, 0), Vec3.ZERO, 5f, 0.1f, 0xFFFFFF, 1f, 0f);
        pool.update(0.5f);
        assertTrue(pool.get(0).pos.y() < 5f);
        pool.update(5f);
        assertTrue(pool.get(0).pos.y() >= 0.019f || pool.isEmpty());
    }

    @Test
    void emitterCounts() {
        ParticlePool pool = new ParticlePool(256);
        Random rng = new Random(42);
        assertEquals(60, Emitters.explosion(pool, new Vec3(0, 1, 0), rng));
        assertEquals(60, pool.aliveCount());
        assertEquals(4, Emitters.footstep(pool, Vec3.ZERO, rng));
        assertEquals(12, Emitters.jumpBurst(pool, Vec3.ZERO, rng));
        assertEquals(10, Emitters.landPoof(pool, Vec3.ZERO, rng));
        assertEquals(86, pool.aliveCount());
    }
}
