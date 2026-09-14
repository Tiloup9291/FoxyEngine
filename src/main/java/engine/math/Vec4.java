package engine.math;

import engine.math.Vec3;

public record Vec4(float x, float y, float z, float w) {
    public static final Vec4 ZERO = new Vec4(0.0f, 0.0f, 0.0f, 0.0f);
    public static final Vec4 UNIT_W = new Vec4(0.0f, 0.0f, 0.0f, 1.0f);

    public Vec4 add(Vec4 o) {
        return new Vec4(this.x + o.x, this.y + o.y, this.z + o.z, this.w + o.w);
    }

    public Vec4 sub(Vec4 o) {
        return new Vec4(this.x - o.x, this.y - o.y, this.z - o.z, this.w - o.w);
    }

    public Vec4 mul(float s) {
        return new Vec4(this.x * s, this.y * s, this.z * s, this.w * s);
    }

    public Vec4 div(float s) {
        return new Vec4(this.x / s, this.y / s, this.z / s, this.w / s);
    }

    public float dot(Vec4 o) {
        return this.x * o.x + this.y * o.y + this.z * o.z + this.w * o.w;
    }

    public float lengthSq() {
        return this.dot(this);
    }

    public float length() {
        return (float)Math.sqrt(this.lengthSq());
    }

    public Vec4 normalize() {
        float len = this.length();
        if (len < 1.0E-9f) {
            return ZERO;
        }
        return this.div(len);
    }

    public Vec3 perspectiveDivide() {
        if (Math.abs(this.w) < 1.0E-9f) {
            return new Vec3(this.x, this.y, this.z);
        }
        return new Vec3(this.x / this.w, this.y / this.w, this.z / this.w);
    }

    public float[] toArray() {
        return new float[]{this.x, this.y, this.z, this.w};
    }
}
