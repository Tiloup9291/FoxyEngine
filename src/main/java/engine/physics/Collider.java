package engine.physics;

import engine.math.AABB;
import engine.math.Vec3;

/*
 * Uses 'sealed' constructs - enablewith --sealed true
 */
public interface Collider {
    public static boolean overlaps(Box a, Box b) {
        return a.aabb().intersects(b.aabb());
    }

    public static boolean overlaps(Sphere a, Sphere b) {
        float r = a.radius() + b.radius();
        return a.center().distance(b.center()) <= r;
    }

    public static boolean overlaps(Sphere s, Box b) {
        float dz;
        float dy;
        Vec3 min = b.aabb().min();
        Vec3 max = b.aabb().max();
        float cx = Collider.clamp(s.center().x(), min.x(), max.x());
        float cy = Collider.clamp(s.center().y(), min.y(), max.y());
        float cz = Collider.clamp(s.center().z(), min.z(), max.z());
        float dx = s.center().x() - cx;
        return dx * dx + (dy = s.center().y() - cy) * dy + (dz = s.center().z() - cz) * dz <= s.radius() * s.radius();
    }

    public static boolean overlaps(Box b, Sphere s) {
        return Collider.overlaps(s, b);
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public record Box(AABB aabb) implements Collider
    {
        public Box {
            if (aabb == null) {
                throw new IllegalArgumentException("null aabb");
            }
        }

        public static Box centered(Vec3 center, Vec3 size) {
            Vec3 h = size.mul(0.5f);
            return new Box(new AABB(center.sub(h), center.add(h)));
        }

        public Box moved(Vec3 offset) {
            return new Box(new AABB(this.aabb.min().add(offset), this.aabb.max().add(offset)));
        }
    }

    public record Sphere(Vec3 center, float radius) implements Collider
    {
        public Sphere {
            if (center == null) {
                throw new IllegalArgumentException("null center");
            }
            if (radius <= 0.0f) {
                throw new IllegalArgumentException("radius <= 0");
            }
        }

        public Sphere moved(Vec3 offset) {
            return new Sphere(this.center.add(offset), this.radius);
        }
    }
}
