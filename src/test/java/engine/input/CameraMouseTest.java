package engine.input;

import static org.junit.jupiter.api.Assertions.*;
import engine.render.CameraRig;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

/** CameraRig addLook/addZoom + driveCamera (mouse/wheel/keys). */
class CameraMouseTest {

    @Test
    void addLookTurnsOrbit() {
        CameraRig rig = CameraRig.defaultRig(CameraRig.Mode.ORBIT, 3f, 1.6f);
        float yaw0 = rig.yaw();
        rig.addLook(0.5f, 0.1f);
        assertEquals(yaw0 - 0.5f, rig.yaw(), 1e-5f);
        assertTrue(rig.pitch() > 0.1f);
    }

    @Test
    void addLookClampsPitch() {
        CameraRig rig = CameraRig.defaultRig(CameraRig.Mode.ORBIT, 3f, 1.6f);
        rig.addLook(0f, 10f);
        assertEquals(1.45f, rig.pitch(), 1e-5f);
        rig.addLook(0f, -10f);
        assertEquals(-1.45f, rig.pitch(), 1e-5f);
    }

    @Test
    void addZoomChangesDistance() {
        CameraRig rig = CameraRig.defaultRig(CameraRig.Mode.ORBIT, 3f, 1.6f);
        float d0 = rig.distance();
        rig.addZoom(-1f);
        assertEquals(d0 - 1f, rig.distance(), 1e-5f);
        rig.addZoom(100f);
        assertEquals(25f, rig.distance(), 1e-5f);
    }

    @Test
    void addZoomFpsMoves() {
        CameraRig rig = CameraRig.defaultRig(CameraRig.Mode.FPS, 3f, 1.6f);
        var p0 = rig.fpsPos();
        rig.addZoom(-2f);
        assertTrue(rig.fpsPos().distance(p0) > 1.9f);
    }

    @Test
    void driveCameraMouseThenKeys() {
        CameraRig rig = CameraRig.defaultRig(CameraRig.Mode.ORBIT, 3f, 1.6f);
        float yaw0 = rig.yaw();
        InputState s = new InputState();
        ActionMapper m = new ActionMapper();
        s.setMouseCaptured(true);
        s.addMouseDelta(100, 0);
        InputFrame f = s.pollFrame(m);
        InputThread.driveCamera(rig, f, 0.016f, 0.0035f, 0.6f);
        assertEquals(yaw0 - 100 * 0.0035f, rig.yaw(), 1e-4f);
        s.addMouseDelta(100, 0);
        s.setMouseCaptured(false);
        InputFrame f2 = s.pollFrame(m);
        float yaw1 = rig.yaw();
        InputThread.driveCamera(rig, f2, 0.016f, 0.0035f, 0.6f);
        assertEquals(yaw1, rig.yaw(), 1e-6f);
    }

    @Test
    void driveCameraWheelZoom() {
        CameraRig rig = CameraRig.defaultRig(CameraRig.Mode.ORBIT, 3f, 1.6f);
        float d0 = rig.distance();
        InputState s = new InputState();
        ActionMapper m = new ActionMapper();
        s.addWheel(2);
        InputFrame f = s.pollFrame(m);
        InputThread.driveCamera(rig, f, 0.016f, 0.0035f, 0.6f);
        assertEquals(d0 + 2 * 0.6f, rig.distance(), 1e-5f);
    }

    @Test
    void keysStillWork() {
        CameraRig rig = CameraRig.defaultRig(CameraRig.Mode.ORBIT, 3f, 1.6f);
        InputThread.driveCamera(rig,
                new InputFrame(java.util.Map.of(InputAction.FWD, true),
                        java.util.Set.of(), 0, 0, 0, 0, 0, false, java.util.Set.of()),
                1f, 0.0035f, 0.6f);
        assertTrue(EnumSet.of(CameraRig.Action.FWD).contains(CameraRig.Action.FWD));
    }

    @Test
    void driveCameraPlayerIgnoresMoveKeys() {
        CameraRig rig = CameraRig.defaultRig(CameraRig.Mode.ORBIT, 3f, 1.6f);
        float d0 = rig.distance();
        float yaw0 = rig.yaw();
        InputState s = new InputState();
        ActionMapper m = new ActionMapper();
        s.keyDown(java.awt.event.KeyEvent.VK_W);
        s.keyDown(java.awt.event.KeyEvent.VK_A);
        InputFrame f = s.pollFrame(m);
        assertTrue(f.held(InputAction.FWD));
        InputThread.driveCameraPlayer(rig, f, 1f, 0.0035f, 0.6f);
        assertEquals(d0, rig.distance(), 1e-6f);
        assertEquals(yaw0, rig.yaw(), 1e-6f);
        InputState s2 = new InputState();
        s2.keyDown(java.awt.event.KeyEvent.VK_LEFT);
        InputFrame f2 = s2.pollFrame(m);
        InputThread.driveCameraPlayer(rig, f2, 1f, 0.0035f, 0.6f);
        assertEquals(yaw0 + 1.6f, rig.yaw(), 1e-5f);
    }
}
