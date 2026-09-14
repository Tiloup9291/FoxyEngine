package engine.input;

import engine.input.InputAction;
import java.util.HashMap;
import java.util.Map;

public final class ActionMapper {
    private final Map<Integer, InputAction> keymap = new HashMap<Integer, InputAction>();

    public ActionMapper() {
        this.resetDefaults();
    }

    public void resetDefaults() {
        this.keymap.clear();
        this.bind(87, InputAction.FWD);
        this.bind(90, InputAction.FWD);
        this.bind(83, InputAction.BACK);
        this.bind(65, InputAction.LEFT);
        this.bind(81, InputAction.LEFT);
        this.bind(68, InputAction.RIGHT);
        this.bind(69, InputAction.UP);
        this.bind(88, InputAction.DOWN);
        this.bind(17, InputAction.DOWN);
        this.bind(37, InputAction.TURN_L);
        this.bind(39, InputAction.TURN_R);
        this.bind(38, InputAction.LOOK_U);
        this.bind(40, InputAction.LOOK_D);
        this.bind(32, InputAction.JUMP);
        this.bind(86, InputAction.TOGGLE_WIREFRAME);
        this.bind(77, InputAction.NEXT_MODEL);
        this.bind(78, InputAction.NEXT_LEVEL);
        this.bind(71, InputAction.TRIGGER_FX);
        this.bind(67, InputAction.TOGGLE_CAMERA);
        this.bind(82, InputAction.RESET_CAMERA);
        this.bind(27, InputAction.PAUSE_TOGGLE);
        this.bind(80, InputAction.PAUSE_TOGGLE);
        this.bind(10, InputAction.MENU_CONFIRM);
    }

    public void bind(int keyCode, InputAction action) {
        this.keymap.put(keyCode, action);
    }

    public void unbind(int keyCode) {
        this.keymap.remove(keyCode);
    }

    public InputAction mapKey(int keyCode) {
        return this.keymap.get(keyCode);
    }

    public Map<Integer, InputAction> bindings() {
        return Map.copyOf(this.keymap);
    }
}
