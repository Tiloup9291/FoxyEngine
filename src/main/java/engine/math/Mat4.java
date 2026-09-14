package engine.math;

import engine.math.Vec3;
import engine.math.Vec4;
import java.util.Arrays;

public final class Mat4 {
    private final float[] m;

    public Mat4() {
        this.m = new float[16];
    }

    public Mat4(float[] valuesColMajor) {
        if (valuesColMajor.length != 16) {
            throw new IllegalArgumentException("Mat4 needs 16 floats");
        }
        this.m = Arrays.copyOf(valuesColMajor, 16);
    }

    public static Mat4 identity() {
        Mat4 r = new Mat4();
        r.m[0] = 1.0f;
        r.m[5] = 1.0f;
        r.m[10] = 1.0f;
        r.m[15] = 1.0f;
        return r;
    }

    public static Mat4 translation(Vec3 t) {
        Mat4 r = Mat4.identity();
        r.m[12] = t.x();
        r.m[13] = t.y();
        r.m[14] = t.z();
        return r;
    }

    public static Mat4 scaling(Vec3 s) {
        Mat4 r = new Mat4();
        r.m[0] = s.x();
        r.m[5] = s.y();
        r.m[10] = s.z();
        r.m[15] = 1.0f;
        return r;
    }

    public static Mat4 rotationX(float radians) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        Mat4 r = Mat4.identity();
        r.m[5] = c;
        r.m[6] = s;
        r.m[9] = -s;
        r.m[10] = c;
        return r;
    }

    public static Mat4 rotationY(float radians) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        Mat4 r = Mat4.identity();
        r.m[0] = c;
        r.m[2] = -s;
        r.m[8] = s;
        r.m[10] = c;
        return r;
    }

    public static Mat4 rotationZ(float radians) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        Mat4 r = Mat4.identity();
        r.m[0] = c;
        r.m[1] = s;
        r.m[4] = -s;
        r.m[5] = c;
        return r;
    }

    public static Mat4 perspective(float fovYRad, float aspect, float near, float far) {
        if (near <= 0.0f || far <= near) {
            throw new IllegalArgumentException("invalid near/far");
        }
        float f = 1.0f / (float)Math.tan(fovYRad / 2.0f);
        Mat4 r = new Mat4();
        r.m[0] = f / aspect;
        r.m[5] = f;
        r.m[10] = (far + near) / (near - far);
        r.m[11] = -1.0f;
        r.m[14] = 2.0f * far * near / (near - far);
        return r;
    }

    public static Mat4 orthographic(float left, float right, float bottom, float top, float near, float far) {
        Mat4 r = Mat4.identity();
        r.m[0] = 2.0f / (right - left);
        r.m[5] = 2.0f / (top - bottom);
        r.m[10] = -2.0f / (far - near);
        r.m[12] = -(right + left) / (right - left);
        r.m[13] = -(top + bottom) / (top - bottom);
        r.m[14] = -(far + near) / (far - near);
        return r;
    }

    public static Mat4 lookAt(Vec3 eye, Vec3 center, Vec3 up) {
        Vec3 f = center.sub(eye).normalize();
        Vec3 s = f.cross(up.normalize()).normalize();
        Vec3 u = s.cross(f);
        Mat4 r = Mat4.identity();
        r.m[0] = s.x();
        r.m[1] = u.x();
        r.m[2] = -f.x();
        r.m[4] = s.y();
        r.m[5] = u.y();
        r.m[6] = -f.y();
        r.m[8] = s.z();
        r.m[9] = u.z();
        r.m[10] = -f.z();
        r.m[12] = -s.dot(eye);
        r.m[13] = -u.dot(eye);
        r.m[14] = f.dot(eye);
        return r;
    }

    public Mat4 mul(Mat4 other) {
        Mat4 r = new Mat4();
        float[] a = this.m;
        float[] b = other.m;
        float[] c = r.m;
        for (int col = 0; col < 4; ++col) {
            for (int row = 0; row < 4; ++row) {
                float sum = 0.0f;
                for (int k = 0; k < 4; ++k) {
                    sum += a[k * 4 + row] * b[col * 4 + k];
                }
                c[col * 4 + row] = sum;
            }
        }
        return r;
    }

    public Vec4 transform(Vec4 v) {
        return new Vec4(this.m[0] * v.x() + this.m[4] * v.y() + this.m[8] * v.z() + this.m[12] * v.w(), this.m[1] * v.x() + this.m[5] * v.y() + this.m[9] * v.z() + this.m[13] * v.w(), this.m[2] * v.x() + this.m[6] * v.y() + this.m[10] * v.z() + this.m[14] * v.w(), this.m[3] * v.x() + this.m[7] * v.y() + this.m[11] * v.z() + this.m[15] * v.w());
    }

    public Vec3 transformPoint(Vec3 p) {
        Vec4 r = this.transform(new Vec4(p.x(), p.y(), p.z(), 1.0f));
        return r.perspectiveDivide();
    }

    public Vec3 transformDirection(Vec3 d) {
        float x = this.m[0] * d.x() + this.m[4] * d.y() + this.m[8] * d.z();
        float y = this.m[1] * d.x() + this.m[5] * d.y() + this.m[9] * d.z();
        float z = this.m[2] * d.x() + this.m[6] * d.y() + this.m[10] * d.z();
        return new Vec3(x, y, z);
    }

    public Mat4 transpose() {
        Mat4 r = new Mat4();
        for (int c = 0; c < 4; ++c) {
            for (int l = 0; l < 4; ++l) {
                r.m[c * 4 + l] = this.m[l * 4 + c];
            }
        }
        return r;
    }

    public float get(int row, int col) {
        return this.m[col * 4 + row];
    }

    public float[] toArrayColMajor() {
        return Arrays.copyOf(this.m, 16);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Mat4[\n");
        for (int row = 0; row < 4; ++row) {
            sb.append("  ");
            for (int col = 0; col < 4; ++col) {
                sb.append(String.format("%9.4f", Float.valueOf(this.get(row, col))));
                if (col >= 3) continue;
                sb.append(", ");
            }
            sb.append("\n");
        }
        return sb.append("]").toString();
    }
}
