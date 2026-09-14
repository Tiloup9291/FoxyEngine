package engine.math;

import engine.math.Mat4;
import engine.math.Vec3;

public record Quaternion(float x, float y, float z, float w) {
    public static final Quaternion IDENTITY = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);

    public static Quaternion fromAxisAngle(Vec3 axis, float radians) {
        Vec3 n = axis.normalize();
        float half = radians / 2.0f;
        float s = (float)Math.sin(half);
        return new Quaternion(n.x() * s, n.y() * s, n.z() * s, (float)Math.cos(half)).normalize();
    }

    public static Quaternion fromEuler(float pitchX, float yawY, float rollZ) {
        Quaternion qx = Quaternion.fromAxisAngle(Vec3.UNIT_X, pitchX);
        Quaternion qy = Quaternion.fromAxisAngle(Vec3.UNIT_Y, yawY);
        Quaternion qz = Quaternion.fromAxisAngle(Vec3.UNIT_Z, rollZ);
        return qy.mul(qx).mul(qz).normalize();
    }

    public Quaternion mul(Quaternion o) {
        return new Quaternion(this.w * o.x + this.x * o.w + this.y * o.z - this.z * o.y, this.w * o.y - this.x * o.z + this.y * o.w + this.z * o.x, this.w * o.z + this.x * o.y - this.y * o.x + this.z * o.w, this.w * o.w - this.x * o.x - this.y * o.y - this.z * o.z);
    }

    public Quaternion conjugate() {
        return new Quaternion(-this.x, -this.y, -this.z, this.w);
    }

    public float lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public Quaternion normalize() {
        float len = (float)Math.sqrt(this.lengthSq());
        if (len < 1.0E-9f) {
            return IDENTITY;
        }
        return new Quaternion(this.x / len, this.y / len, this.z / len, this.w / len);
    }

    public Vec3 rotate(Vec3 v) {
        Quaternion qv = new Quaternion(v.x(), v.y(), v.z(), 0.0f);
        Quaternion r = this.mul(qv).mul(this.conjugate());
        return new Vec3(r.x(), r.y(), r.z());
    }

    public Mat4 toMatrix() {
        float xx = this.x * this.x;
        float yy = this.y * this.y;
        float zz = this.z * this.z;
        float xy = this.x * this.y;
        float xz = this.x * this.z;
        float yz = this.y * this.z;
        float wx = this.w * this.x;
        float wy = this.w * this.y;
        float wz = this.w * this.z;
        return new Mat4(new float[]{1.0f - 2.0f * (yy + zz), 2.0f * (xy + wz), 2.0f * (xz - wy), 0.0f, 2.0f * (xy - wz), 1.0f - 2.0f * (xx + zz), 2.0f * (yz + wx), 0.0f, 2.0f * (xz + wy), 2.0f * (yz - wx), 1.0f - 2.0f * (xx + yy), 0.0f, 0.0f, 0.0f, 0.0f, 1.0f});
    }
}
