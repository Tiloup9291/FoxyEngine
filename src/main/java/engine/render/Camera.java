package engine.render;

import engine.math.Mat4;
import engine.math.Vec3;

public final class Camera {
    private final Vec3 position;
    private final Vec3 target;
    private final Vec3 up;

    public Camera(Vec3 position, Vec3 target, Vec3 up) {
        this.position = position;
        this.target = target;
        this.up = up;
    }

    public static Camera defaultCamera() {
        return new Camera(new Vec3(0.0f, 0.8f, 4.2f), Vec3.ZERO, Vec3.UNIT_Y);
    }

    public Mat4 viewMatrix() {
        return Mat4.lookAt(this.position, this.target, this.up);
    }

    public Vec3 position() {
        return this.position;
    }

    public Vec3 target() {
        return this.target;
    }
}
