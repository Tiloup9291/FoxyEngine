package engine.physics;

import engine.input.InputAction;
import engine.input.InputFrame;
import engine.math.Vec3;
import engine.physics.Body;
import engine.render.CameraRig;

public final class PlayerController {
    private final float speed;
    private final float jumpVelocity;

    public PlayerController(float speed, float jumpVelocity) {
        if (speed <= 0.0f) {
            throw new IllegalArgumentException("speed <= 0");
        }
        this.speed = speed;
        this.jumpVelocity = jumpVelocity;
    }

    public float speed() {
        return this.speed;
    }

    public float jumpVelocity() {
        return this.jumpVelocity;
    }

    public static float cameraYaw(CameraRig rig) {
        return rig.mode() == CameraRig.Mode.ORBIT ? rig.yaw() : rig.fpsYaw();
    }

    public void drive(Body body, InputFrame frame, float yaw) {
        float fwd = 0.0f;
        float strafe = 0.0f;
        if (frame.held(InputAction.FWD)) {
            fwd += 1.0f;
        }
        if (frame.held(InputAction.BACK)) {
            fwd -= 1.0f;
        }
        if (frame.held(InputAction.RIGHT)) {
            strafe += 1.0f;
        }
        if (frame.held(InputAction.LEFT)) {
            strafe -= 1.0f;
        }
        float len = (float)Math.sqrt(fwd * fwd + strafe * strafe);
        Vec3 vel = body.velocity();
        if (len > 1.0E-6f) {
            float sy = (float)Math.sin(yaw);
            float cy = (float)Math.cos(yaw);
            Vec3 wish = new Vec3(-sy * (fwd /= len) + cy * (strafe /= len), 0.0f, -cy * fwd - sy * strafe);
            body.setVelocity(new Vec3(wish.x() * this.speed, vel.y(), wish.z() * this.speed));
        } else {
            body.setVelocity(new Vec3(0.0f, vel.y(), 0.0f));
        }
        if (frame.pressed(InputAction.JUMP) && body.onGround()) {
            body.setVelocity(new Vec3(body.velocity().x(), this.jumpVelocity, body.velocity().z()));
            body.setOnGround(false);
        }
    }
}
