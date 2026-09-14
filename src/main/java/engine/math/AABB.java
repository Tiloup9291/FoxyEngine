package engine.math;

import engine.math.Vec3;

public record AABB(Vec3 min, Vec3 max) {
    public static AABB ofPoints(Vec3 ... points) {
        if (points.length == 0) {
            throw new IllegalArgumentException("empty AABB");
        }
        float minX = points[0].x();
        float minY = points[0].y();
        float minZ = points[0].z();
        float maxX = minX;
        float maxY = minY;
        float maxZ = minZ;
        for (int i = 1; i < points.length; ++i) {
            Vec3 p = points[i];
            minX = Math.min(minX, p.x());
            minY = Math.min(minY, p.y());
            minZ = Math.min(minZ, p.z());
            maxX = Math.max(maxX, p.x());
            maxY = Math.max(maxY, p.y());
            maxZ = Math.max(maxZ, p.z());
        }
        return new AABB(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ));
    }

    public static AABB ofFloatArray(float[] positions) {
        if (positions.length % 3 != 0 || positions.length == 0) {
            throw new IllegalArgumentException("positions must hold x,y,z triplets");
        }
        float minX = positions[0];
        float minY = positions[1];
        float minZ = positions[2];
        float maxX = minX;
        float maxY = minY;
        float maxZ = minZ;
        for (int i = 3; i < positions.length; i += 3) {
            minX = Math.min(minX, positions[i]);
            maxX = Math.max(maxX, positions[i]);
            minY = Math.min(minY, positions[i + 1]);
            maxY = Math.max(maxY, positions[i + 1]);
            minZ = Math.min(minZ, positions[i + 2]);
            maxZ = Math.max(maxZ, positions[i + 2]);
        }
        return new AABB(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ));
    }

    public Vec3 center() {
        return this.min.add(this.max).div(2.0f);
    }

    public Vec3 extents() {
        return this.max.sub(this.min).div(2.0f);
    }

    public Vec3 size() {
        return this.max.sub(this.min);
    }

    public boolean contains(Vec3 p) {
        return p.x() >= this.min.x() && p.x() <= this.max.x() && p.y() >= this.min.y() && p.y() <= this.max.y() && p.z() >= this.min.z() && p.z() <= this.max.z();
    }

    public boolean intersects(AABB o) {
        return this.min.x() <= o.max.x() && this.max.x() >= o.min.x() && this.min.y() <= o.max.y() && this.max.y() >= o.min.y() && this.min.z() <= o.max.z() && this.max.z() >= o.min.z();
    }

    public AABB merge(AABB o) {
        return new AABB(new Vec3(Math.min(this.min.x(), o.min.x()), Math.min(this.min.y(), o.min.y()), Math.min(this.min.z(), o.min.z())), new Vec3(Math.max(this.max.x(), o.max.x()), Math.max(this.max.y(), o.max.y()), Math.max(this.max.z(), o.max.z())));
    }
}
