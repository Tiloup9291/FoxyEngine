package engine.physics;

import engine.math.Vec3;

public record RayHit(float distance, Vec3 point, Vec3 normal, int colliderIndex) {
    public static final RayHit MISS = new RayHit(Float.MAX_VALUE, Vec3.ZERO, Vec3.UNIT_Y, -1);

    public boolean hit() {
        return this.colliderIndex >= 0;
    }
}
