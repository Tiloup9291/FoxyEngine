package engine.render;

import engine.math.Vec3;
import engine.render.Camera;
import java.util.Set;

public final class CameraRig {
    private Mode mode;
    private final float moveSpeed;
    private final float rotSpeed;
    private Vec3 target = Vec3.ZERO;
    private float yaw;
    private float pitch;
    private float distance;
    private Vec3 fpsPos;
    private float fpsYaw;
    private float fpsPitch;
    private final Vec3 homeTarget;
    private final float homeYaw;
    private final float homePitch;
    private final float homeDistance;
    private final Vec3 homeFpsPos;
    private final float homeFpsYaw;
    private final float homeFpsPitch;

    public CameraRig(Mode mode, Vec3 target, float yaw, float pitch, float distance, Vec3 fpsPos, float fpsYaw, float fpsPitch, float moveSpeed, float rotSpeed) {
        this.mode = mode;
        this.target = target;
        this.yaw = yaw;
        this.pitch = pitch;
        this.distance = distance;
        this.fpsPos = fpsPos;
        this.fpsYaw = fpsYaw;
        this.fpsPitch = fpsPitch;
        this.moveSpeed = moveSpeed;
        this.rotSpeed = rotSpeed;
        this.homeTarget = target;
        this.homeYaw = yaw;
        this.homePitch = pitch;
        this.homeDistance = distance;
        this.homeFpsPos = fpsPos;
        this.homeFpsYaw = fpsYaw;
        this.homeFpsPitch = fpsPitch;
    }

    public static CameraRig defaultRig(Mode mode, float moveSpeed, float rotSpeed) {
        float dist = (float)Math.sqrt(18.279996871948242);
        float pitch = (float)Math.asin(0.8f / dist);
        return new CameraRig(mode, Vec3.ZERO, 0.0f, pitch, dist, new Vec3(0.0f, 0.8f, 4.2f), 0.0f, -pitch, moveSpeed, rotSpeed);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode mode() {
        return this.mode;
    }

    public void toggleMode() {
        this.mode = this.mode == Mode.ORBIT ? Mode.FPS : Mode.ORBIT;
    }

    public void setTarget(Vec3 target) {
        this.target = target;
    }

    public void setFpsPos(Vec3 pos) {
        this.fpsPos = pos;
    }

    public void reset() {
        if (this.mode == Mode.ORBIT) {
            this.target = this.homeTarget;
            this.yaw = this.homeYaw;
            this.pitch = this.homePitch;
            this.distance = this.homeDistance;
        } else {
            this.fpsPos = this.homeFpsPos;
            this.fpsYaw = this.homeFpsYaw;
            this.fpsPitch = this.homeFpsPitch;
        }
    }

    public void update(float dt, Set<Action> actions) {
        if (this.mode == Mode.ORBIT) {
            if (actions.contains((Object)Action.TURN_L) || actions.contains((Object)Action.LEFT)) {
                this.yaw += this.rotSpeed * dt;
            }
            if (actions.contains((Object)Action.TURN_R) || actions.contains((Object)Action.RIGHT)) {
                this.yaw -= this.rotSpeed * dt;
            }
            if (actions.contains((Object)Action.LOOK_U) || actions.contains((Object)Action.UP)) {
                this.pitch += this.rotSpeed * 0.7f * dt;
            }
            if (actions.contains((Object)Action.LOOK_D) || actions.contains((Object)Action.DOWN)) {
                this.pitch -= this.rotSpeed * 0.7f * dt;
            }
            this.pitch = Math.max(-1.45f, Math.min(1.45f, this.pitch));
            if (actions.contains((Object)Action.FWD)) {
                this.distance -= this.moveSpeed * dt;
            }
            if (actions.contains((Object)Action.BACK)) {
                this.distance += this.moveSpeed * dt;
            }
            this.distance = Math.max(1.2f, Math.min(25.0f, this.distance));
        } else {
            if (actions.contains((Object)Action.TURN_L)) {
                this.fpsYaw += this.rotSpeed * dt;
            }
            if (actions.contains((Object)Action.TURN_R)) {
                this.fpsYaw -= this.rotSpeed * dt;
            }
            if (actions.contains((Object)Action.LOOK_U)) {
                this.fpsPitch += this.rotSpeed * 0.7f * dt;
            }
            if (actions.contains((Object)Action.LOOK_D)) {
                this.fpsPitch -= this.rotSpeed * 0.7f * dt;
            }
            this.fpsPitch = Math.max(-1.55f, Math.min(1.55f, this.fpsPitch));
            Vec3 dir = this.fpsDirection();
            Vec3 right = dir.cross(Vec3.UNIT_Y).normalize();
            Vec3 move = Vec3.ZERO;
            if (actions.contains((Object)Action.FWD)) {
                move = move.add(dir);
            }
            if (actions.contains((Object)Action.BACK)) {
                move = move.sub(dir);
            }
            if (actions.contains((Object)Action.RIGHT)) {
                move = move.add(right);
            }
            if (actions.contains((Object)Action.LEFT)) {
                move = move.sub(right);
            }
            if (actions.contains((Object)Action.UP)) {
                move = move.add(Vec3.UNIT_Y);
            }
            if (actions.contains((Object)Action.DOWN)) {
                move = move.sub(Vec3.UNIT_Y);
            }
            if (move.lengthSq() > 1.0E-9f) {
                this.fpsPos = this.fpsPos.add(move.normalize().mul(this.moveSpeed * dt));
            }
        }
    }

    private Vec3 fpsDirection() {
        float cp = (float)Math.cos(this.fpsPitch);
        return new Vec3((float)Math.sin(this.fpsYaw) * cp, (float)Math.sin(this.fpsPitch), (float)(-Math.cos(this.fpsYaw)) * cp).normalize();
    }

    public Camera toCamera() {
        if (this.mode == Mode.ORBIT) {
            float cp = (float)Math.cos(this.pitch);
            Vec3 pos = new Vec3(this.target.x() + this.distance * cp * (float)Math.sin(this.yaw), this.target.y() + this.distance * (float)Math.sin(this.pitch), this.target.z() + this.distance * cp * (float)Math.cos(this.yaw));
            return new Camera(pos, this.target, Vec3.UNIT_Y);
        }
        return new Camera(this.fpsPos, this.fpsPos.add(this.fpsDirection()), Vec3.UNIT_Y);
    }

    public void addLook(float dxRad, float dyRad) {
        if (this.mode == Mode.ORBIT) {
            this.yaw -= dxRad;
            this.pitch += dyRad;
            this.pitch = Math.max(-1.45f, Math.min(1.45f, this.pitch));
        } else {
            this.fpsYaw -= dxRad;
            this.fpsPitch += dyRad;
            this.fpsPitch = Math.max(-1.55f, Math.min(1.55f, this.fpsPitch));
        }
    }

    public void addZoom(float amount) {
        if (this.mode == Mode.ORBIT) {
            this.distance = Math.max(1.2f, Math.min(25.0f, this.distance + amount));
        } else {
            this.fpsPos = this.fpsPos.add(this.fpsDirection().mul(-amount));
        }
    }

    public Vec3 target() {
        return this.target;
    }

    public float yaw() {
        return this.yaw;
    }

    public float pitch() {
        return this.pitch;
    }

    public float distance() {
        return this.distance;
    }

    public Vec3 fpsPos() {
        return this.fpsPos;
    }

    public float fpsYaw() {
        return this.fpsYaw;
    }

    public float fpsPitch() {
        return this.fpsPitch;
    }

    public static enum Mode {
        ORBIT,
        FPS;

    }

    public static enum Action {
        FWD,
        BACK,
        LEFT,
        RIGHT,
        UP,
        DOWN,
        TURN_L,
        TURN_R,
        LOOK_U,
        LOOK_D;

    }
}
