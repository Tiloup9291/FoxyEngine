package engine.fx;

import engine.math.Vec3;

public final class Particle {
    public Vec3 pos = Vec3.ZERO;
    public Vec3 vel = Vec3.ZERO;
    public float life;
    public float maxLife = 1.0f;
    public float size = 0.08f;
    public int colorRgb = 0xFFFFFF;
    public float gravity = 1.0f;
    public float drag = 0.5f;

    public boolean alive() {
        return this.life > 0.0f;
    }

    public float lifeFrac() {
        return this.maxLife > 1.0E-9f ? Math.max(0.0f, this.life / this.maxLife) : 0.0f;
    }

    public void spawn(Vec3 pos, Vec3 vel, float life, float size, int colorRgb, float gravity, float drag) {
        this.pos = pos;
        this.vel = vel;
        this.life = life;
        this.maxLife = life;
        this.size = size;
        this.colorRgb = colorRgb & 0xFFFFFF;
        this.gravity = gravity;
        this.drag = drag;
    }

    public void update(float dt, float worldGravityY) {
        if (this.life <= 0.0f) {
            return;
        }
        this.life -= dt;
        if (this.life <= 0.0f) {
            this.life = 0.0f;
            return;
        }
        float damp = Math.max(0.0f, 1.0f - this.drag * dt);
        this.vel = new Vec3(this.vel.x() * damp, this.vel.y() * damp + worldGravityY * this.gravity * dt, this.vel.z() * damp);
        this.pos = this.pos.add(this.vel.mul(dt));
        if (this.pos.y() < 0.02f) {
            this.pos = new Vec3(this.pos.x(), 0.02f, this.pos.z());
            this.vel = new Vec3(this.vel.x() * 0.7f, -this.vel.y() * 0.3f, this.vel.z() * 0.7f);
        }
    }
}
