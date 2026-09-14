package engine.input;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.Test;

/** InputState: held vs pressed edges, mouse/wheel drain, camera actions. */
class InputStateTest {

    @Test
    void heldAndPressedEdge() {
        InputState s = new InputState();
        ActionMapper m = new ActionMapper();
        s.keyDown(KeyEvent.VK_W);
        InputFrame f = s.pollFrame(m);
        assertTrue(f.held(InputAction.FWD));
        assertTrue(f.pressed(InputAction.FWD));
        InputFrame f2 = s.pollFrame(m);
        assertTrue(f2.held(InputAction.FWD));
        assertFalse(f2.pressed(InputAction.FWD));
        s.keyUp(KeyEvent.VK_W);
        assertFalse(s.pollFrame(m).held(InputAction.FWD));
    }

    @Test
    void mouseAndWheelDrain() {
        InputState s = new InputState();
        ActionMapper m = new ActionMapper();
        s.addMouseDelta(10, -5);
        s.addWheel(2);
        InputFrame f = s.pollFrame(m);
        assertEquals(10, f.mouseDX());
        assertEquals(-5, f.mouseDY());
        assertEquals(2, f.wheel());
        InputFrame f2 = s.pollFrame(m);
        assertEquals(0, f2.mouseDX());
        assertEquals(0, f2.wheel());
    }

    @Test
    void cameraActionsMap() {
        InputState s = new InputState();
        ActionMapper m = new ActionMapper();
        s.keyDown(KeyEvent.VK_W);
        s.keyDown(KeyEvent.VK_D);
        InputFrame f = s.pollFrame(m);
        var cam = f.cameraActions();
        assertTrue(cam.contains(engine.render.CameraRig.Action.FWD));
        assertTrue(cam.contains(engine.render.CameraRig.Action.RIGHT));
    }

    @Test
    void functionKeyEdgeOnce() {
        InputState s = new InputState();
        ActionMapper m = new ActionMapper();
        s.keyDown(KeyEvent.VK_V);
        assertTrue(s.pollFrame(m).pressed(InputAction.TOGGLE_WIREFRAME));
        assertFalse(s.pollFrame(m).pressed(InputAction.TOGGLE_WIREFRAME));
    }
}
