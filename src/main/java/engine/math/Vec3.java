package engine.math;

public record Vec3(float x, float y, float z) {
    public static final Vec3 ZERO = new Vec3(0.0f, 0.0f, 0.0f);
    public static final Vec3 ONE = new Vec3(1.0f, 1.0f, 1.0f);
    public static final Vec3 UNIT_X = new Vec3(1.0f, 0.0f, 0.0f);
    public static final Vec3 UNIT_Y = new Vec3(0.0f, 1.0f, 0.0f);
    public static final Vec3 UNIT_Z = new Vec3(0.0f, 0.0f, 1.0f);

    public Vec3 add(Vec3 o) {
        return new Vec3(this.x + o.x, this.y + o.y, this.z + o.z);
    }

    public Vec3 sub(Vec3 o) {
        return new Vec3(this.x - o.x, this.y - o.y, this.z - o.z);
    }

    public Vec3 mul(float s) {
        return new Vec3(this.x * s, this.y * s, this.z * s);
    }

    public Vec3 div(float s) {
        return new Vec3(this.x / s, this.y / s, this.z / s);
    }

    public Vec3 neg() {
        return new Vec3(-this.x, -this.y, -this.z);
    }

    public float dot(Vec3 o) {
        return this.x * o.x + this.y * o.y + this.z * o.z;
    }

    public Vec3 cross(Vec3 o) {
        return new Vec3(this.y * o.z - this.z * o.y, this.z * o.x - this.x * o.z, this.x * o.y - this.y * o.x);
    }

    public float lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public float length() {
        return (float)Math.sqrt(this.lengthSq());
    }

    public Vec3 normalize() {
        float len = this.length();
        if (len < 1.0E-9f) {
            return ZERO;
        }
        return this.div(len);
    }

    public float distance(Vec3 o) {
        return this.sub(o).length();
    }

    public float[] toArray() {
        return new float[]{this.x, this.y, this.z};
    }
}
