package engine.physics;

import engine.math.Vec3;
import engine.physics.Collider;

public final class Contact {
    public Vec3 normal = Vec3.UNIT_Y;
    public float penetration;
    public Vec3 point = Vec3.ZERO;
    public boolean hit;

    public void clear() {
        this.hit = false;
        this.penetration = 0.0f;
    }

    public static boolean sphereVsBox(Collider.Sphere s, Collider.Box b, Contact out) {
        out.clear();
        Vec3 min = b.aabb().min();
        Vec3 max = b.aabb().max();
        float cx = Contact.clamp(s.center().x(), min.x(), max.x());
        float cy = Contact.clamp(s.center().y(), min.y(), max.y());
        float cz = Contact.clamp(s.center().z(), min.z(), max.z());
        Vec3 closest = new Vec3(cx, cy, cz);
        Vec3 delta = s.center().sub(closest);
        float d2 = delta.lengthSq();
        if (d2 > s.radius() * s.radius()) {
            return false;
        }
        float d = (float)Math.sqrt(d2);
        if (d > 1.0E-6f) {
            out.normal = delta.div(d);
            out.penetration = s.radius() - d;
        } else {
            float[] dists = new float[]{s.center().x() - min.x(), max.x() - s.center().x(), s.center().y() - min.y(), max.y() - s.center().y(), s.center().z() - min.z(), max.z() - s.center().z()};
            int axis = 0;
            for (int i = 1; i < 6; ++i) {
                if (!(dists[i] < dists[axis])) continue;
                axis = i;
            }
            out.normal = switch (axis) {
                case 0 -> Vec3.UNIT_X.neg();
                case 1 -> Vec3.UNIT_X;
                case 2 -> Vec3.UNIT_Y.neg();
                case 3 -> Vec3.UNIT_Y;
                case 4 -> Vec3.UNIT_Z.neg();
                default -> Vec3.UNIT_Z;
            };
            out.penetration = dists[axis] + s.radius();
        }
        out.point = closest;
        out.hit = true;
        return true;
    }

    public static boolean sphereVsSphere(Collider.Sphere a, Collider.Sphere b, Contact out) {
        out.clear();
        Vec3 delta = a.center().sub(b.center());
        float r = a.radius() + b.radius();
        float d2 = delta.lengthSq();
        if (d2 > r * r) {
            return false;
        }
        float d = (float)Math.sqrt(d2);
        out.normal = d > 1.0E-6f ? delta.div(d) : Vec3.UNIT_Y;
        out.penetration = r - d;
        out.point = b.center().add(out.normal.mul(b.radius()));
        out.hit = true;
        return true;
    }

    public static boolean boxVsBox(Collider.Box a, Collider.Box b, Contact out) {
        out.clear();
        Vec3 amin = a.aabb().min();
        Vec3 amax = a.aabb().max();
        Vec3 bmin = b.aabb().min();
        Vec3 bmax = b.aabb().max();
        float ox = Math.min(amax.x(), bmax.x()) - Math.max(amin.x(), bmin.x());
        float oy = Math.min(amax.y(), bmax.y()) - Math.max(amin.y(), bmin.y());
        float oz = Math.min(amax.z(), bmax.z()) - Math.max(amin.z(), bmin.z());
        if (ox <= 0.0f || oy <= 0.0f || oz <= 0.0f) {
            return false;
        }
        Vec3 ca = a.aabb().center();
        Vec3 cb = b.aabb().center();
        if (ox <= oy && ox <= oz) {
            out.normal = ca.x() >= cb.x() ? Vec3.UNIT_X : Vec3.UNIT_X.neg();
            out.penetration = ox;
        } else if (oy <= oz) {
            out.normal = ca.y() >= cb.y() ? Vec3.UNIT_Y : Vec3.UNIT_Y.neg();
            out.penetration = oy;
        } else {
            out.normal = ca.z() >= cb.z() ? Vec3.UNIT_Z : Vec3.UNIT_Z.neg();
            out.penetration = oz;
        }
        out.point = ca.add(cb).div(2.0f);
        out.hit = true;
        return true;
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
