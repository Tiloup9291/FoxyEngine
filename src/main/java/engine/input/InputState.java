package engine.input;

import engine.input.ActionMapper;
import engine.input.InputAction;
import engine.input.InputFrame;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InputState {
    private final Set<Integer> keysDown = ConcurrentHashMap.newKeySet();
    private volatile int mouseDX;
    private volatile int mouseDY;
    private volatile int wheelAccum;
    private volatile int mouseX;
    private volatile int mouseY;
    private volatile boolean mouseCaptured;
    private volatile long lastEventNs = System.nanoTime();
    private final Set<Integer> pressedSincePoll = ConcurrentHashMap.newKeySet();

    public void keyDown(int keyCode) {
        if (this.keysDown.add(keyCode)) {
            this.pressedSincePoll.add(keyCode);
        }
        this.lastEventNs = System.nanoTime();
    }

    public void keyUp(int keyCode) {
        this.keysDown.remove(keyCode);
        this.lastEventNs = System.nanoTime();
    }

    public boolean isDown(int keyCode) {
        return this.keysDown.contains(keyCode);
    }

    public void addMouseDelta(int dx, int dy) {
        this.mouseDX += dx;
        this.mouseDY += dy;
        this.lastEventNs = System.nanoTime();
    }

    public void addWheel(int notches) {
        this.wheelAccum += notches;
        this.lastEventNs = System.nanoTime();
    }

    public void setMousePos(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    public void setMouseCaptured(boolean captured) {
        this.mouseCaptured = captured;
    }

    public boolean isMouseCaptured() {
        return this.mouseCaptured;
    }

    public long lastEventNs() {
        return this.lastEventNs;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public InputFrame pollFrame(ActionMapper mapper) {
        InputAction a;
        int wheel;
        int dy;
        int dx;
        Set<Integer> keys = Set.copyOf(this.keysDown);
        Set<Integer> pressed = Set.copyOf(this.pressedSincePoll);
        this.pressedSincePoll.removeAll(pressed);
        InputState inputState = this;
        synchronized (inputState) {
            dx = this.mouseDX;
            dy = this.mouseDY;
            wheel = this.wheelAccum;
            this.mouseDX = 0;
            this.mouseDY = 0;
            this.wheelAccum = 0;
        }
        EnumMap<InputAction, Boolean> held = new EnumMap<InputAction, Boolean>(InputAction.class);
        EnumSet<InputAction> pressedActions = EnumSet.noneOf(InputAction.class);
        for (int code : keys) {
            a = mapper.mapKey(code);
            if (a == null) continue;
            held.put(a, Boolean.TRUE);
        }
        for (int code : pressed) {
            a = mapper.mapKey(code);
            if (a == null) continue;
            pressedActions.add(a);
        }
        return new InputFrame(held, pressedActions, dx, dy, wheel, this.mouseX, this.mouseY, this.mouseCaptured, Set.copyOf(pressed));
    }
}
