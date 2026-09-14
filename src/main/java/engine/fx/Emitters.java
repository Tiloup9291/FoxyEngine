package engine.fx;

import engine.fx.ParticlePool;
import engine.math.Vec3;
import java.util.Random;

public final class Emitters {
    private Emitters() {
    }

    public static int explosion(ParticlePool pool, Vec3 center, Random rng) {
        int[] fire = new int[]{16765286, 16032353, 15167313, 15681391, 0xAAAAAA};
        int n = 0;
        for (int i = 0; i < 60; ++i) {
            Vec3 dir = Emitters.randomDir(rng);
            float speed = 2.0f + rng.nextFloat() * 5.0f;
            float life = 0.5f + rng.nextFloat() * 0.6f;
            pool.spawn(center.add(dir.mul(0.2f)), dir.mul(speed), life, 0.1f + rng.nextFloat() * 0.1f, fire[rng.nextInt(fire.length)], 0.5f, 1.5f);
            ++n;
        }
        return n;
    }

    public static int footstep(ParticlePool pool, Vec3 feet, Random rng) {
        for (int i = 0; i < 4; ++i) {
            Vec3 v = new Vec3((rng.nextFloat() - 0.5f) * 1.2f, 0.4f + rng.nextFloat() * 0.6f, (rng.nextFloat() - 0.5f) * 1.2f);
            pool.spawn(feet.add(new Vec3(0.0f, 0.05f, 0.0f)), v, 0.35f + rng.nextFloat() * 0.15f, 0.05f + rng.nextFloat() * 0.04f, 0xBBBBAA, -0.15f, 2.5f);
        }
        return 4;
    }

    public static int jumpBurst(ParticlePool pool, Vec3 feet, Random rng) {
        for (int i = 0; i < 12; ++i) {
            float a = (float)((double)i / 12.0 * Math.PI * 2.0);
            Vec3 v = new Vec3((float)Math.cos(a) * 2.2f, 0.6f, (float)Math.sin(a) * 2.2f);
            pool.spawn(feet, v, 0.4f, 0.06f, 7268279, 0.3f, 2.0f);
        }
        return 12;
    }

    public static int landPoof(ParticlePool pool, Vec3 feet, Random rng) {
        for (int i = 0; i < 10; ++i) {
            float a = rng.nextFloat() * ((float)Math.PI * 2);
            float r = 1.0f + rng.nextFloat() * 1.5f;
            Vec3 v = new Vec3((float)Math.cos(a) * r, 0.3f, (float)Math.sin(a) * r);
            pool.spawn(feet, v, 0.5f, 0.08f, 0xCCCCBB, 0.1f, 2.5f);
        }
        return 10;
    }

    private static Vec3 randomDir(Random rng) {
        float z = rng.nextFloat() * 2.0f - 1.0f;
        float a = rng.nextFloat() * ((float)Math.PI * 2);
        float r = (float)Math.sqrt(Math.max(0.0f, 1.0f - z * z));
        return new Vec3(r * (float)Math.cos(a), (float)Math.sqrt(rng.nextFloat()) * 0.9f + 0.1f, r * (float)Math.sin(a)).normalize();
    }
}
