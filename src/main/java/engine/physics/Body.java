package engine.physics;

import engine.math.Vec3;
import engine.physics.Collider;
import engine.physics.Contact;

public final class Body {
    private Vec3 position;
    private Vec3 velocity = Vec3.ZERO;
    private final Vec3 size;
    private final float restitution;
    private final float friction;
    private boolean onGround;
    private final Contact scratch = new Contact();

    public Body(Vec3 position, Vec3 size, float restitution, float friction) {
        if (position == null || size == null) {
            throw new IllegalArgumentException("nulls");
        }
        this.position = position;
        this.size = size;
        this.restitution = restitution;
        this.friction = friction;
    }

    public static Body player(Vec3 spawn) {
        return new Body(spawn, new Vec3(0.6f, 1.2f, 0.6f), 0.0f, 0.9f);
    }

    public static Body cube(Vec3 pos, float edge, float restitution) {
        return new Body(pos, new Vec3(edge, edge, edge), restitution, 0.4f);
    }

    public Vec3 position() {
        return this.position;
    }

    public Vec3 velocity() {
        return this.velocity;
    }

    public Vec3 size() {
        return this.size;
    }

    public boolean onGround() {
        return this.onGround;
    }

    public Contact scratch() {
        return this.scratch;
    }

    public void setPosition(Vec3 p) {
        this.position = p;
    }

    public void setVelocity(Vec3 v) {
        this.velocity = v;
    }

    public void setOnGround(boolean g) {
        this.onGround = g;
    }

    public Collider.Box collider() {
        return Collider.Box.centered(this.position, this.size);
    }

    public float restitution() {
        return this.restitution;
    }

    public float friction() {
        return this.friction;
    }
}
