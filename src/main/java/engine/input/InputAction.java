package engine.input;

import engine.render.CameraRig;

public enum InputAction {
    FWD,
    BACK,
    LEFT,
    RIGHT,
    UP,
    DOWN,
    TURN_L,
    TURN_R,
    LOOK_U,
    LOOK_D,
    TOGGLE_WIREFRAME,
    NEXT_MODEL,
    NEXT_LEVEL,
    TRIGGER_FX,
    TOGGLE_CAMERA,
    RESET_CAMERA,
    JUMP,
    PAUSE_TOGGLE,
    MENU_CONFIRM;


    public CameraRig.Action toCameraAction() {
        return switch (this) {
            case TOGGLE_WIREFRAME, NEXT_MODEL, NEXT_LEVEL, TRIGGER_FX, TOGGLE_CAMERA, RESET_CAMERA, JUMP, PAUSE_TOGGLE, MENU_CONFIRM -> null;
            case FWD -> CameraRig.Action.FWD;
            case BACK -> CameraRig.Action.BACK;
            case LEFT -> CameraRig.Action.LEFT;
            case RIGHT -> CameraRig.Action.RIGHT;
            case UP -> CameraRig.Action.UP;
            case DOWN -> CameraRig.Action.DOWN;
            case TURN_L -> CameraRig.Action.TURN_L;
            case TURN_R -> CameraRig.Action.TURN_R;
            case LOOK_U -> CameraRig.Action.LOOK_U;
            case LOOK_D -> CameraRig.Action.LOOK_D;
        };
    }

    public boolean isCameraAction() {
        return this.toCameraAction() != null;
    }

    public static boolean isProfilerToggle(int keyCode) {
        return keyCode == 84;
    }
}
