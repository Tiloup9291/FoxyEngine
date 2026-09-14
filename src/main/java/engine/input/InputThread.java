package engine.input;

import engine.input.ActionMapper;
import engine.input.InputFrame;
import engine.input.InputState;
import engine.render.CameraRig;
import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;

public final class InputThread {
    private final Canvas canvas;
    private final InputState state;
    private final ActionMapper mapper;
    private Robot robot;
    private volatile boolean captureRequested = true;
    private volatile boolean running;
    private Thread pumpThread;
    private final KeyAdapter keyAdapter = new KeyAdapter(){

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 70) {
                InputThread.this.toggleCapture();
                return;
            }
            InputThread.this.state.keyDown(e.getKeyCode());
        }

        @Override
        public void keyReleased(KeyEvent e) {
            InputThread.this.state.keyUp(e.getKeyCode());
        }
    };
    private final MouseAdapter mouseAdapter = new MouseAdapter(){

        @Override
        public void mouseMoved(MouseEvent e) {
            InputThread.this.state.setMousePos(e.getX(), e.getY());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            InputThread.this.state.setMousePos(e.getX(), e.getY());
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            InputThread.this.state.addWheel(e.getWheelRotation());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            InputThread.this.canvas.requestFocusInWindow();
            if (!InputThread.this.state.isMouseCaptured()) {
                InputThread.this.toggleCapture();
            }
        }
    };

    public InputThread(Canvas canvas, InputState state, ActionMapper mapper) {
        this.canvas = canvas;
        this.state = state;
        this.mapper = mapper;
        try {
            this.robot = new Robot();
        }
        catch (AWTException e) {
            this.robot = null;
        }
    }

    public InputState state() {
        return this.state;
    }

    public ActionMapper mapper() {
        return this.mapper;
    }

    public void start() {
        if (this.running) {
            return;
        }
        this.running = true;
        this.canvas.setFocusable(true);
        this.canvas.addKeyListener(this.keyAdapter);
        this.canvas.addMouseListener(this.mouseAdapter);
        this.canvas.addMouseMotionListener(this.mouseAdapter);
        this.canvas.addMouseWheelListener(this.mouseAdapter);
        this.setCaptured(true);
        this.pumpThread = new Thread(this::pump, "InputThread");
        this.pumpThread.setDaemon(true);
        this.pumpThread.start();
        this.canvas.requestFocusInWindow();
    }

    public void stop() {
        this.running = false;
        if (this.pumpThread != null) {
            try {
                this.pumpThread.join(1000L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.setCaptured(false);
        try {
            this.canvas.removeKeyListener(this.keyAdapter);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.canvas.removeMouseListener(this.mouseAdapter);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.canvas.removeMouseMotionListener(this.mouseAdapter);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.canvas.removeMouseWheelListener(this.mouseAdapter);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void toggleCapture() {
        this.setCaptured(!this.state.isMouseCaptured());
    }

    public void setCaptured(boolean captured) {
        this.captureRequested = captured;
        this.state.setMouseCaptured(captured);
        this.canvas.setCursor(captured ? InputThread.hiddenCursor() : Cursor.getDefaultCursor());
    }

    private static Cursor hiddenCursor() {
        BufferedImage img = new BufferedImage(1, 1, 2);
        return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(0, 0), "hidden");
    }

    private void pump() {
        while (this.running) {
            Point cur;
            Point center;
            if (this.captureRequested && this.canvas.isShowing() && this.robot != null && (center = this.canvasCenterOnScreen()) != null && (cur = InputThread.mouseOnScreen()) != null && (cur.x != center.x || cur.y != center.y)) {
                this.state.addMouseDelta(cur.x - center.x, cur.y - center.y);
                this.robot.mouseMove(center.x, center.y);
            }
            try {
                Thread.sleep(8L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private Point canvasCenterOnScreen() {
        try {
            Point p = this.canvas.getLocationOnScreen();
            return new Point(p.x + this.canvas.getWidth() / 2, p.y + this.canvas.getHeight() / 2);
        }
        catch (Exception e) {
            return null;
        }
    }

    private static Point mouseOnScreen() {
        try {
            return MouseInfo.getPointerInfo().getLocation();
        }
        catch (Exception e) {
            return null;
        }
    }

    public static void driveCamera(CameraRig rig, InputFrame frame, float dt, float mouseSensitivity, float wheelZoom) {
        HashSet<CameraRig.Action> actions = new HashSet<CameraRig.Action>(frame.cameraActions());
        if (frame.mouseCaptured() && mouseSensitivity > 0.0f && (frame.mouseDX() != 0 || frame.mouseDY() != 0)) {
            rig.addLook((float)frame.mouseDX() * mouseSensitivity, (float)(-frame.mouseDY()) * mouseSensitivity);
        }
        if (frame.wheel() != 0 && wheelZoom > 0.0f) {
            rig.addZoom((float)frame.wheel() * wheelZoom);
        }
        rig.update(dt, actions);
    }

    public static void driveCameraPlayer(CameraRig rig, InputFrame frame, float dt, float mouseSensitivity, float wheelZoom) {
        HashSet<CameraRig.Action> actions = new HashSet<CameraRig.Action>(frame.cameraActions());
        actions.remove((Object)CameraRig.Action.FWD);
        actions.remove((Object)CameraRig.Action.BACK);
        actions.remove((Object)CameraRig.Action.LEFT);
        actions.remove((Object)CameraRig.Action.RIGHT);
        if (frame.mouseCaptured() && mouseSensitivity > 0.0f && (frame.mouseDX() != 0 || frame.mouseDY() != 0)) {
            rig.addLook((float)frame.mouseDX() * mouseSensitivity, (float)(-frame.mouseDY()) * mouseSensitivity);
        }
        if (frame.wheel() != 0 && wheelZoom > 0.0f) {
            rig.addZoom((float)frame.wheel() * wheelZoom);
        }
        rig.update(dt, actions);
    }
}
