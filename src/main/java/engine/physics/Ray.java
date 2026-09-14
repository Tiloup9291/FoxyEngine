package engine.physics;

import engine.math.Vec3;

public record Ray(Vec3 origin, Vec3 direction, float maxDist) {
    public Ray {
        if (origin == null || direction == null) {
            throw new IllegalArgumentException("null ray");
        }
        if (maxDist <= 0.0f) {
            throw new IllegalArgumentException("maxDist <= 0");
        }
    }

    public static Ray of(Vec3 origin, Vec3 direction) {
        return new Ray(origin, direction.normalize(), Float.MAX_VALUE);
    }

    public Vec3 pointAt(float t) {
        return this.origin.add(this.direction.mul(t));
    }
}
