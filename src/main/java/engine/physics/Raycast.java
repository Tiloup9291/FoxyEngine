package engine.physics;

import engine.math.Vec3;
import engine.physics.Body;
import engine.physics.Collider;
import engine.physics.Ray;
import engine.physics.RayHit;
import java.util.List;

public final class Raycast {
    private Raycast() {
    }

    public static RayHit cast(Ray ray, List<Collider.Box> statics, List<Body> dynamics) {
        int i;
        RayHit best = RayHit.MISS;
        if (statics != null) {
            for (i = 0; i < statics.size(); ++i) {
                float t = Raycast.hitBox(ray, statics.get(i));
                if (!(t >= 0.0f) || !(t < best.distance()) || !(t <= ray.maxDist())) continue;
                Vec3 p = ray.pointAt(t);
                best = new RayHit(t, p, Raycast.boxNormal(ray, statics.get(i), p), i);
            }
        }
        if (dynamics != null) {
            for (i = 0; i < dynamics.size(); ++i) {
                float r;
                Body b = dynamics.get(i);
                Vec3 c = b.position();
                float t = Raycast.hitSphere(ray, c, r = Math.max(b.size().x(), Math.max(b.size().y(), b.size().z())) * 0.5f);
                if (!(t >= 0.0f) || !(t < best.distance()) || !(t <= ray.maxDist())) continue;
                Vec3 p = ray.pointAt(t);
                Vec3 n = p.sub(c).normalize();
                best = new RayHit(t, p, n, 100000 + i);
            }
        }
        return best;
    }

    static float hitBox(Ray ray, Collider.Box box) {
        Vec3 o = ray.origin();
        Vec3 d = ray.direction();
        Vec3 min = box.aabb().min();
        Vec3 max = box.aabb().max();
        float tmin = 0.0f;
        float tmax = ray.maxDist();
        float[] oo = new float[]{o.x(), o.y(), o.z()};
        float[] dd = new float[]{d.x(), d.y(), d.z()};
        float[] lo = new float[]{min.x(), min.y(), min.z()};
        float[] hi = new float[]{max.x(), max.y(), max.z()};
        for (int i = 0; i < 3; ++i) {
            if (Math.abs(dd[i]) < 1.0E-9f) {
                if (!(oo[i] < lo[i]) && !(oo[i] > hi[i])) continue;
                return -1.0f;
            }
            float inv = 1.0f / dd[i];
            float t0 = (lo[i] - oo[i]) * inv;
            float t1 = (hi[i] - oo[i]) * inv;
            if (t0 > t1) {
                float tmp = t0;
                t0 = t1;
                t1 = tmp;
            }
            if (!((tmin = Math.max(tmin, t0)) > (tmax = Math.min(tmax, t1)))) continue;
            return -1.0f;
        }
        return tmin;
    }

    static Vec3 boxNormal(Ray ray, Collider.Box box, Vec3 p) {
        Vec3 min = box.aabb().min();
        Vec3 max = box.aabb().max();
        float eps = 1.0E-4f;
        if (Math.abs(p.x() - min.x()) < eps) {
            return Vec3.UNIT_X.neg();
        }
        if (Math.abs(p.x() - max.x()) < eps) {
            return Vec3.UNIT_X;
        }
        if (Math.abs(p.y() - min.y()) < eps) {
            return Vec3.UNIT_Y.neg();
        }
        if (Math.abs(p.y() - max.y()) < eps) {
            return Vec3.UNIT_Y;
        }
        if (Math.abs(p.z() - min.z()) < eps) {
            return Vec3.UNIT_Z.neg();
        }
        if (Math.abs(p.z() - max.z()) < eps) {
            return Vec3.UNIT_Z;
        }
        return ray.direction().neg();
    }

    static float hitSphere(Ray ray, Vec3 c, float r) {
        float cc;
        Vec3 oc = ray.origin().sub(c);
        float b = oc.dot(ray.direction());
        float disc = b * b - (cc = oc.lengthSq() - r * r);
        if (disc < 0.0f) {
            return -1.0f;
        }
        float t = -b - (float)Math.sqrt(disc);
        return t >= 0.0f ? t : -1.0f;
    }
}
