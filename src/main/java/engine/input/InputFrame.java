package engine.input;

import engine.input.InputAction;
import engine.render.CameraRig;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public record InputFrame(Map<InputAction, Boolean> held, Set<InputAction> pressed, int mouseDX, int mouseDY, int wheel, int mouseX, int mouseY, boolean mouseCaptured, Set<Integer> rawPressedCodes) {

    public static InputFrame empty() {
        return new InputFrame(Map.of(), Set.of(), 0, 0, 0, 0, 0, false, Set.of());
    }

    public boolean held(InputAction a) {
        return this.held.getOrDefault((Object)a, Boolean.FALSE);
    }

    public boolean pressed(InputAction a) {
        return this.pressed.contains((Object)a);
    }

    public Set<CameraRig.Action> cameraActions() {
        EnumSet<CameraRig.Action> out = EnumSet.noneOf(CameraRig.Action.class);
        for (InputAction a : this.held.keySet()) {
            CameraRig.Action c = a.toCameraAction();
            if (c == null) continue;
            out.add(c);
        }
        return out;
    }

    public Set<Integer> rawPressedCodes() {
        return this.rawPressedCodes != null ? this.rawPressedCodes : Set.of();
    }
}
