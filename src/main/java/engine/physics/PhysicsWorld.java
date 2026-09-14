package engine.physics;

import engine.math.Vec3;
import engine.physics.Body;
import engine.physics.Broadphase;
import engine.physics.Collider;
import engine.physics.Contact;
import engine.physics.Ray;
import engine.physics.RayHit;
import engine.physics.Raycast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PhysicsWorld {
    private final List<Collider.Box> statics = new ArrayList<Collider.Box>();
    private final List<Body> bodies = new ArrayList<Body>();
    private final Broadphase broadphase;
    private final Set<Integer> candidates = new HashSet<Integer>();
    private Vec3 gravity = new Vec3(0.0f, -9.81f, 0.0f);
    private float groundY = 0.0f;
    private boolean groundEnabled = true;
    private float fixedDt = 0.016666668f;
    private float accumulator;
    private long stepCount;
    private int lastContactCount;

    public PhysicsWorld(float cellSize) {
        this.broadphase = new Broadphase(cellSize);
    }

    public PhysicsWorld() {
        this(2.0f);
    }

    public int addStatic(Collider.Box box) {
        this.statics.add(box);
        this.broadphase.rebuild(this.statics);
        return this.statics.size() - 1;
    }

    public void clearStatics() {
        this.statics.clear();
        this.broadphase.rebuild(this.statics);
    }

    public int addBody(Body b) {
        this.bodies.add(b);
        return this.bodies.size() - 1;
    }

    public List<Collider.Box> statics() {
        return this.statics;
    }

    public List<Body> bodies() {
        return this.bodies;
    }

    public Broadphase broadphase() {
        return this.broadphase;
    }

    public void setGravity(Vec3 g) {
        this.gravity = g;
    }

    public Vec3 gravity() {
        return this.gravity;
    }

    public void setGround(float y, boolean enabled) {
        this.groundY = y;
        this.groundEnabled = enabled;
    }

    public float groundY() {
        return this.groundY;
    }

    public void setFixedDt(float dt) {
        this.fixedDt = dt;
    }

    public long stepCount() {
        return this.stepCount;
    }

    public int lastContactCount() {
        return this.lastContactCount;
    }

    public void update(float dt) {
        int n;
        this.accumulator += Math.min(dt, 0.25f);
        for (n = 0; this.accumulator >= this.fixedDt && n < 16; ++n) {
            this.step(this.fixedDt);
            this.accumulator -= this.fixedDt;
        }
        if (n == 16) {
            this.accumulator = 0.0f;
        }
    }

    public void drain() {
        this.accumulator = 0.0f;
    }

    public void step(float dt) {
        ++this.stepCount;
        int contacts = 0;
        for (Body b : this.bodies) {
            Vec3 v;
            b.setVelocity(b.velocity().add(this.gravity.mul(dt)));
            if (b.onGround()) {
                float f = Math.max(0.0f, 1.0f - b.friction() * dt * 8.0f);
                v = b.velocity();
                b.setVelocity(new Vec3(v.x() * f, v.y(), v.z() * f));
            }
            b.setPosition(b.position().add(b.velocity().mul(dt)));
            b.setOnGround(false);
            float halfH = b.size().y() * 0.5f;
            if (this.groundEnabled && b.position().y() - halfH < this.groundY) {
                float vy;
                b.setPosition(new Vec3(b.position().x(), this.groundY + halfH, b.position().z()));
                v = b.velocity();
                float f = vy = v.y() < 0.0f ? -v.y() * b.restitution() : v.y();
                if (Math.abs(vy) < 0.05f) {
                    vy = 0.0f;
                }
                b.setVelocity(new Vec3(v.x(), vy, v.z()));
                b.setOnGround(true);
                ++contacts;
            }
            Collider.Box self = b.collider();
            this.broadphase.query(self.aabb(), this.candidates);
            for (int i : this.candidates) {
                Contact c = b.scratch();
                if (!Contact.boxVsBox(self, this.statics.get(i), c)) continue;
                ++contacts;
                b.setPosition(b.position().add(c.normal.mul(c.penetration + 1.0E-4f)));
                Vec3 v2 = b.velocity();
                float vn = v2.dot(c.normal);
                if (vn < 0.0f) {
                    Vec3 vt = v2.sub(c.normal.mul(vn));
                    Vec3 vn2 = c.normal.mul(-vn * b.restitution());
                    b.setVelocity(vt.add(vn2));
                    if (c.normal.y() > 0.7f) {
                        b.setOnGround(true);
                    }
                }
                self = b.collider();
            }
        }
        this.lastContactCount = contacts;
    }

    public RayHit raycast(Ray ray) {
        return Raycast.cast(ray, this.statics, this.bodies);
    }
}
