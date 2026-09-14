package engine.render;

import engine.math.Mat4;
import engine.math.Quaternion;
import engine.math.Vec3;

public record Transform(Vec3 position, Quaternion rotation, Vec3 scale) {
    public static final Transform IDENTITY = new Transform(Vec3.ZERO, Quaternion.IDENTITY, new Vec3(1.0f, 1.0f, 1.0f));

    public Transform {
        if (position == null || rotation == null || scale == null) {
            throw new IllegalArgumentException("null components");
        }
    }

    public static Transform of(Vec3 offset, float uniformScale) {
        return new Transform(offset, Quaternion.IDENTITY, new Vec3(uniformScale, uniformScale, uniformScale));
    }

    public static Transform of(Vec3 position, Quaternion rotation, float uniformScale) {
        return new Transform(position, rotation, new Vec3(uniformScale, uniformScale, uniformScale));
    }

    public Mat4 modelMatrix() {
        return Mat4.translation(this.position).mul(this.rotation.toMatrix()).mul(Mat4.scaling(this.scale));
    }

    public Mat4 rotationMatrix() {
        return this.rotation.toMatrix();
    }
}
