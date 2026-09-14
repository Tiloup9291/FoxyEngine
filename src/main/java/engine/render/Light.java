package engine.render;

import engine.math.Vec3;

public record Light(Vec3 direction, Vec3 color, float intensity) {
    public static final Light DEFAULT_SUN = new Light(new Vec3(0.4f, 0.8f, 0.6f).normalize(), new Vec3(1.0f, 1.0f, 1.0f), 1.0f);

    public Light {
        if (direction == null || color == null) {
            throw new IllegalArgumentException("null direction/color");
        }
        if (intensity < 0.0f) {
            throw new IllegalArgumentException("intensite negative");
        }
    }

    public Vec3 dir() {
        return this.direction.normalize();
    }
}
