package engine.fx;

import engine.fx.Particle;
import engine.math.Vec3;

public final class ParticlePool {
    private final Particle[] all;
    private int aliveCount;
    private float worldGravityY = -9.81f;

    public ParticlePool(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity <= 0");
        }
        this.all = new Particle[capacity];
        for (int i = 0; i < capacity; ++i) {
            this.all[i] = new Particle();
        }
        this.aliveCount = 0;
    }

    public int capacity() {
        return this.all.length;
    }

    public int aliveCount() {
        return this.aliveCount;
    }

    public boolean isEmpty() {
        return this.aliveCount == 0;
    }

    public void setWorldGravity(float g) {
        this.worldGravityY = g;
    }

    public boolean spawn(Vec3 pos, Vec3 vel, float life, float size, int colorRgb, float gravity, float drag) {
        Particle p;
        boolean recycled = false;
        if (this.aliveCount < this.all.length) {
            p = this.all[this.aliveCount++];
        } else {
            p = this.all[0];
            System.arraycopy(this.all, 1, this.all, 0, this.all.length - 1);
            this.all[this.all.length - 1] = p;
            recycled = true;
        }
        p.spawn(pos, vel, life, size, colorRgb, gravity, drag);
        return !recycled;
    }

    public void update(float dt) {
        for (int i = this.aliveCount - 1; i >= 0; --i) {
            Particle p = this.all[i];
            p.update(dt, this.worldGravityY);
            if (p.alive()) continue;
            --this.aliveCount;
            Particle tmp = this.all[i];
            this.all[i] = this.all[this.aliveCount];
            this.all[this.aliveCount] = tmp;
        }
    }

    public void clear() {
        this.aliveCount = 0;
    }

    public Particle get(int i) {
        return this.all[i];
    }
}
