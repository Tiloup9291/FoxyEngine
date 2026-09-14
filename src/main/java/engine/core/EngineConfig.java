package engine.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Properties;

public final class EngineConfig {
    private final int windowWidth;
    private final int windowHeight;
    private final String windowTitle;
    private final int fpsTarget;
    private final int tickRate;
    private final String renderMode;
    private final float fovDeg;
    private final float near;
    private final float far;
    private final int clearColor;
    private final int cubeColor;
    private final int wireColor;
    private final String modelId;
    private final float modelScale;
    private final String cameraMode;
    private final float cameraMoveSpeed;
    private final float cameraRotSpeed;
    private final int renderBands;
    private final float mouseSensitivity;
    private final float wheelZoom;
    private final boolean mouseCaptured;
    private final float gravityY;
    private final float groundY;
    private final float playerSpeed;
    private final float playerJump;
    private final boolean audioEnabled;
    private final float audioMaster;
    private final float audioMaxDist;
    private final boolean lodEnabled;
    private final boolean profilerEnabled;
    private final float lightDirX;
    private final float lightDirY;
    private final float lightDirZ;
    private final float lightIntensity;
    private final int lightColor;
    private final float ambient;
    private final String textureFilter;

    private EngineConfig(int w, int h, String title, int fps, int tick, String renderMode, float fovDeg, float near, float far, int clearColor, int cubeColor, int wireColor, String modelId, float modelScale, String cameraMode, float cameraMoveSpeed, float cameraRotSpeed, float lightDirX, float lightDirY, float lightDirZ, float lightIntensity, int lightColor, float ambient, String textureFilter, int renderBands, float mouseSensitivity, float wheelZoom, boolean mouseCaptured, float gravityY, float groundY, float playerSpeed, float playerJump, boolean audioEnabled, float audioMaster, float audioMaxDist, boolean lodEnabled, boolean profilerEnabled) {
        this.windowWidth = w;
        this.windowHeight = h;
        this.windowTitle = title;
        this.fpsTarget = clampFps(fps);
        this.tickRate = clampTick(tick);
        this.renderMode = renderMode;
        this.fovDeg = fovDeg;
        this.near = near;
        this.far = far;
        this.clearColor = clearColor;
        this.cubeColor = cubeColor;
        this.wireColor = wireColor;
        this.modelId = modelId;
        this.modelScale = modelScale;
        this.cameraMode = cameraMode;
        this.cameraMoveSpeed = cameraMoveSpeed;
        this.cameraRotSpeed = cameraRotSpeed;
        this.lightDirX = lightDirX;
        this.lightDirY = lightDirY;
        this.lightDirZ = lightDirZ;
        this.lightIntensity = lightIntensity;
        this.lightColor = lightColor;
        this.ambient = ambient;
        this.textureFilter = textureFilter;
        this.renderBands = Math.max(1, renderBands);
        this.mouseSensitivity = Math.max(0.0f, mouseSensitivity);
        this.wheelZoom = Math.max(0.0f, wheelZoom);
        this.mouseCaptured = mouseCaptured;
        this.gravityY = gravityY;
        this.groundY = groundY;
        this.playerSpeed = playerSpeed;
        this.playerJump = playerJump;
        this.audioEnabled = audioEnabled;
        this.audioMaster = Math.max(0.0f, Math.min(1.0f, audioMaster));
        this.audioMaxDist = Math.max(1.0f, audioMaxDist);
        this.lodEnabled = lodEnabled;
        this.profilerEnabled = profilerEnabled;
    }

    public static EngineConfig load(Path file) throws IOException {
        Properties p = new Properties();
        if (Files.exists(file, new LinkOption[0])) {
            try (InputStream in = Files.newInputStream(file, new OpenOption[0]);){
                p.load(in);
            }
        }
        return EngineConfig.fromProperties(p);
    }

    public static EngineConfig fromProperties(Properties p) {
        int w = EngineConfig.parseInt(p, "engine.window.width", 800);
        int h = EngineConfig.parseInt(p, "engine.window.height", 600);
        String title = p.getProperty("engine.window.title", "Java 3D Engine v1.0");
        int fps = EngineConfig.parseInt(p, "engine.fps.target", 60);
        int tick = EngineConfig.parseInt(p, "engine.tick.rate", 60);
        String mode = p.getProperty("engine.render.mode", "solid").trim().toLowerCase();
        if (!mode.equals("solid") && !mode.equals("wireframe")) {
            mode = "solid";
        }
        float fov = EngineConfig.parseFloat(p, "engine.render.fov", 70.0f);
        float near = EngineConfig.parseFloat(p, "engine.render.near", 0.1f);
        float far = EngineConfig.parseFloat(p, "engine.render.far", 100.0f);
        int clear = EngineConfig.parseColor(p.getProperty("engine.render.clear", "0x101418"));
        int cube = EngineConfig.parseColor(p.getProperty("engine.render.cube", "0x4DA3FF"));
        int wire = EngineConfig.parseColor(p.getProperty("engine.render.wire", "0x00FF88"));
        String modelId = p.getProperty("engine.model.id", "cube").trim();
        if (modelId.isEmpty()) {
            modelId = "cube";
        }
        float modelScale = EngineConfig.parseFloat(p, "engine.model.scale", 0.8f);
        String camMode = p.getProperty("engine.camera.mode", "orbit").trim().toLowerCase();
        if (!camMode.equals("orbit") && !camMode.equals("fps")) {
            camMode = "orbit";
        }
        float camMove = EngineConfig.parseFloat(p, "engine.camera.move_speed", 3.0f);
        float camRot = EngineConfig.parseFloat(p, "engine.camera.rot_speed", 1.6f);
        float ldx = EngineConfig.parseFloat(p, "engine.light.dir_x", 0.4f);
        float ldy = EngineConfig.parseFloat(p, "engine.light.dir_y", 0.8f);
        float ldz = EngineConfig.parseFloat(p, "engine.light.dir_z", 0.6f);
        float li = Math.max(0.0f, EngineConfig.parseFloat(p, "engine.light.intensity", 1.0f));
        int lc = EngineConfig.parseColor(p.getProperty("engine.light.color", "0xFFFFFF"));
        float amb = Math.max(0.0f, EngineConfig.parseFloat(p, "engine.light.ambient", 0.25f));
        String tf = p.getProperty("engine.texture.filter", "bilinear").trim().toLowerCase();
        if (!tf.equals("nearest") && !tf.equals("bilinear")) {
            tf = "bilinear";
        }
        int bands = EngineConfig.parseInt(p, "engine.pipeline.bands", 4);
        float sens = EngineConfig.parseFloat(p, "engine.input.mouse_sensitivity", 0.0035f);
        float zoom = EngineConfig.parseFloat(p, "engine.input.wheel_zoom", 0.6f);
        boolean captured = EngineConfig.parseBool(p, "engine.input.mouse_captured", true);
        float gravY = EngineConfig.parseFloat(p, "engine.physics.gravity_y", -9.81f);
        float gndY = EngineConfig.parseFloat(p, "engine.physics.ground_y", 0.0f);
        float pSpeed = EngineConfig.parseFloat(p, "engine.player.speed", 4.0f);
        float pJump = EngineConfig.parseFloat(p, "engine.player.jump", 5.0f);
        boolean aOn = EngineConfig.parseBool(p, "engine.audio.enabled", true);
        float aMaster = EngineConfig.parseFloat(p, "engine.audio.master", 0.8f);
        float aDist = EngineConfig.parseFloat(p, "engine.audio.max_dist", 18.0f);
        boolean lod = EngineConfig.parseBool(p, "engine.lod.enabled", true);
        boolean prof = EngineConfig.parseBool(p, "engine.profiler.enabled", true);
        return new EngineConfig(w, h, title, fps, tick, mode, fov, near, far, clear, cube, wire, modelId, modelScale, camMode, camMove, camRot, ldx, ldy, ldz, li, lc, amb, tf, bands, sens, zoom, captured, gravY, gndY, pSpeed, pJump, aOn, aMaster, aDist, lod, prof);
    }

    private static int parseInt(Properties p, String key, int def) {
        try {
            return Integer.parseInt(p.getProperty(key, String.valueOf(def)).trim());
        }
        catch (NumberFormatException e) {
            return def;
        }
    }

    private static float parseFloat(Properties p, String key, float def) {
        try {
            return Float.parseFloat(p.getProperty(key, String.valueOf(def)).trim());
        }
        catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean parseBool(Properties p, String key, boolean def) {
        String v = p.getProperty(key, String.valueOf(def));
        if (v == null) {
            return def;
        }
        return (v = v.trim().toLowerCase()).equals("true") || v.equals("1") || v.equals("yes") || v.equals("on");
    }

    static int parseColor(String s) {
        s = s.trim().toLowerCase().replace("0x", "").replace("#", "");
        return (int)(Long.parseLong(s, 16) & 0xFFFFFFL);
    }

    public static int clampFps(int fps) {
        return Math.min(120, Math.max(0, fps));
    }

    public static int clampTick(int tick) {
        return Math.min(240, Math.max(0, tick));
    }

    public static EngineConfig defaults() {
        return new EngineConfig(800, 600, "Java 3D Engine v1.0", 60, 60, "solid", 70.0f, 0.1f, 100.0f, 1053720, 5088255, 65416, "cube", 0.8f, "orbit", 3.0f, 1.6f, 0.4f, 0.8f, 0.6f, 1.0f, 0xFFFFFF, 0.25f, "bilinear", 4, 0.0035f, 0.6f, true, -9.81f, 0.0f, 4.0f, 5.0f, true, 0.8f, 18.0f, true, true);
    }

    public int windowWidth() {
        return this.windowWidth;
    }

    public int windowHeight() {
        return this.windowHeight;
    }

    public String windowTitle() {
        return this.windowTitle;
    }

    public int fpsTarget() {
        return this.fpsTarget;
    }

    public int tickRate() {
        return this.tickRate;
    }

    public String renderMode() {
        return this.renderMode;
    }

    public float fovDeg() {
        return this.fovDeg;
    }

    public float near() {
        return this.near;
    }

    public float far() {
        return this.far;
    }

    public int clearColor() {
        return this.clearColor;
    }

    public int cubeColor() {
        return this.cubeColor;
    }

    public int wireColor() {
        return this.wireColor;
    }

    public String modelId() {
        return this.modelId;
    }

    public float modelScale() {
        return this.modelScale;
    }

    public String cameraMode() {
        return this.cameraMode;
    }

    public float cameraMoveSpeed() {
        return this.cameraMoveSpeed;
    }

    public float cameraRotSpeed() {
        return this.cameraRotSpeed;
    }

    public float lightDirX() {
        return this.lightDirX;
    }

    public float lightDirY() {
        return this.lightDirY;
    }

    public float lightDirZ() {
        return this.lightDirZ;
    }

    public float lightIntensity() {
        return this.lightIntensity;
    }

    public int lightColor() {
        return this.lightColor;
    }

    public float ambient() {
        return this.ambient;
    }

    public String textureFilter() {
        return this.textureFilter;
    }

    public int renderBands() {
        return this.renderBands;
    }

    public float mouseSensitivity() {
        return this.mouseSensitivity;
    }

    public float wheelZoom() {
        return this.wheelZoom;
    }

    public boolean mouseCaptured() {
        return this.mouseCaptured;
    }

    public float gravityY() {
        return this.gravityY;
    }

    public float groundY() {
        return this.groundY;
    }

    public float playerSpeed() {
        return this.playerSpeed;
    }

    public float playerJump() {
        return this.playerJump;
    }

    public boolean audioEnabled() {
        return this.audioEnabled;
    }

    public float audioMaster() {
        return this.audioMaster;
    }

    public float audioMaxDist() {
        return this.audioMaxDist;
    }

    public boolean lodEnabled() {
        return this.lodEnabled;
    }

    public boolean profilerEnabled() {
        return this.profilerEnabled;
    }

    public String toString() {
        return "EngineConfig[width=" + this.windowWidth + ", height=" + this.windowHeight + ", title=" + this.windowTitle + ", fpsTarget=" + this.fpsTarget + ", tickRate=" + this.tickRate + ", mode=" + this.renderMode + ", fov=" + this.fovDeg + "]";
    }
}
