package engine.core;

import engine.anim.AnimStateMachine;
import engine.anim.Animator;
import engine.anim.Clips;
import engine.anim.Humanoid;
import engine.anim.Rig;
import engine.assets.AssetManager;
import engine.assets.ModelRepository;
import engine.assets.ObjParseException;
import engine.audio.AudioClip;
import engine.audio.AudioThread;
import engine.audio.SoundBank;
import engine.core.EngineConfig;
import engine.core.FrameLimiter;
import engine.core.Profiler;
import engine.core.Splash;
import engine.fx.FXThread;
import engine.fx.ParticleRenderer;
import engine.input.ActionMapper;
import engine.input.InputAction;
import engine.input.InputFrame;
import engine.input.InputState;
import engine.input.InputThread;
import engine.level.LevelDef;
import engine.level.LevelParseException;
import engine.level.LevelThread;
import engine.math.Quaternion;
import engine.math.Vec3;
import engine.physics.Body;
import engine.physics.Collider;
import engine.physics.PhysicsWorld;
import engine.physics.PlayerController;
import engine.pipeline.RenderPipeline;
import engine.pipeline.SceneSnapshot;
import engine.render.Camera;
import engine.render.CameraRig;
import engine.render.Framebuffer;
import engine.render.Light;
import engine.render.Lighting;
import engine.render.LodMesh;
import engine.render.Mesh;
import engine.render.Renderer;
import engine.render.Texture;
import engine.render.TextureCache;
import engine.render.Transform;
import engine.ui.Hud;
import engine.ui.MenuThread;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class EngineKernel {
    private final EngineConfig config;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong fpsValue = new AtomicLong(0L);
    private final AtomicLong trianglesValue = new AtomicLong(0L);
    private Thread loopThread;
    private JFrame frame;
    private Canvas canvas;
    private volatile Renderer renderer;
    private volatile Framebuffer framebuffer;
    private volatile ModelRepository repository;
    private volatile String currentModel = "cube";
    private volatile String modelError;
    private volatile String lastBuiltModel;
    private volatile CameraRig rig;
    private volatile TextureCache textures;
    private volatile RenderPipeline pipeline;
    private volatile List<Renderer.Instance> sceneInstances = List.of();
    private volatile float spinAngle;
    private volatile InputThread input;
    private volatile InputState inputState;
    private volatile ActionMapper actionMapper;
    private volatile PhysicsWorld physics;
    private volatile Body playerBody;
    private volatile PlayerController player;
    private volatile Body crateBody;
    private volatile Rig playerRig;
    private volatile Animator playerAnimator;
    private volatile AnimStateMachine animFsm;
    private volatile AudioThread audio;
    private volatile SoundBank sounds;
    private volatile AudioClip sndJump;
    private volatile AudioClip sndStep;
    private volatile AudioClip sndLand;
    private volatile boolean wasGrounded = true;
    private volatile float stepAccum;
    private volatile MenuThread menus;
    private volatile Hud hud;
    private volatile boolean soundOn = true;
    private volatile boolean quitRequested;
    private volatile LevelThread levels;
    private volatile String currentLevel = "demo";
    private volatile String levelError;
    private volatile FXThread fx;
    private volatile int particleCount;
    private volatile AssetManager assets;
    private volatile Profiler profiler;
    private volatile boolean showProfiler;
    private volatile int lodDrawn;
    private volatile long bootNs = System.nanoTime();
    private long logicAccumNs;
    private long tickPeriodNs;
    private long droppedSimNs;
    private long lastHudCacheNs;
    private String cachedStatus = "";
    private String cachedPhys = "";
    private final ExecutorService bandPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "RenderBand");
        t.setDaemon(true);
        return t;
    });
    private final Set<Integer> keysDown = Collections.synchronizedSet(new HashSet());
    private volatile InputFrame lastInputFrame = InputFrame.empty();
    private volatile boolean menuOpen;
    private volatile int bandsOverride = -1;

    public EngineKernel(EngineConfig config) {
        this.config = config;
    }

    public void start() {
        if (!this.running.compareAndSet(false, true)) {
            return;
        }
        Splash.drawBanner("JAVA 3D ENGINE", "v1.0");
        try {
            SwingUtilities.invokeAndWait(this::createWindow);
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to create window", e);
        }
        this.loopThread = new Thread(this::loop, "RenderThread");
        this.loopThread.setDaemon(true);
        this.loopThread.start();
    }

    private void createWindow() {
        this.frame = new JFrame(this.config.windowTitle());
        this.frame.setDefaultCloseOperation(3);
        this.canvas = new Canvas();
        this.canvas.setPreferredSize(new Dimension(this.config.windowWidth(), this.config.windowHeight()));
        this.canvas.setFocusable(true);
        this.frame.add(this.canvas);
        this.frame.pack();
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
        this.canvas.createBufferStrategy(2);
        this.framebuffer = new Framebuffer(this.config.windowWidth(), this.config.windowHeight());
        this.repository = new ModelRepository(Path.of("assets", "models"));
        this.textures = new TextureCache(Path.of("assets", "textures"));
        this.currentModel = this.config.modelId();
        CameraRig.Mode m = this.config.cameraMode().equals("fps") ? CameraRig.Mode.FPS : CameraRig.Mode.ORBIT;
        this.rig = CameraRig.defaultRig(m, this.config.cameraMoveSpeed(), this.config.cameraRotSpeed());
        this.rebuildRenderer();
        this.inputState = new InputState();
        this.actionMapper = new ActionMapper();
        this.input = new InputThread(this.canvas, this.inputState, this.actionMapper);
        this.input.start();
        this.input.setCaptured(this.config.mouseCaptured());
        this.physics = new PhysicsWorld();
        this.physics.setGravity(new Vec3(0.0f, this.config.gravityY(), 0.0f));
        this.physics.setGround(this.config.groundY(), true);
        this.physics.setFixedDt(1.0f / (float)Math.max(1, this.config.tickRate()));
        this.physics.addStatic(Collider.Box.centered(new Vec3(2.5f, this.config.groundY() + 0.25f, -2.5f), new Vec3(2.0f, 0.5f, 2.0f)));
        this.playerBody = Body.player(new Vec3(0.0f, this.config.groundY() + 2.0f, 2.0f));
        this.crateBody = Body.cube(new Vec3(-2.0f, this.config.groundY() + 3.0f, -1.0f), 0.7f, 0.5f);
        this.physics.addBody(this.playerBody);
        this.physics.addBody(this.crateBody);
        this.player = new PlayerController(this.config.playerSpeed(), this.config.playerJump());
        this.playerRig = Humanoid.create();
        this.playerAnimator = new Animator(this.playerRig, Map.of(Animator.State.IDLE, Clips.idle(), Animator.State.WALK, Clips.walk(), Animator.State.RUN, Clips.run(), Animator.State.JUMP, Clips.jump()));
        this.playerAnimator.setFadeDuration(0.15f);
        this.animFsm = new AnimStateMachine(this.playerAnimator, 2.0f, 5.0f);
        this.levels = new LevelThread(Path.of("assets", "levels"), this.repository);
        try {
            this.loadLevel(this.currentLevel);
        }
        catch (Exception e) {
            this.levelError = e.getMessage();
        }
        this.fx = new FXThread(512, 1337L);
        this.fx.pool().setWorldGravity(this.config.gravityY());
        this.profiler = new Profiler("input", "phys", "anim", "pipe", "fx");
        this.showProfiler = this.config.profilerEnabled();
        this.sounds = new SoundBank(Path.of("assets", "sounds"));
        this.audio = new AudioThread();
        this.audio.setSpatialMaxDist(this.config.audioMaxDist());
        try {
            this.sndJump = this.sounds.acquire("jump");
            this.sndStep = this.sounds.acquire("step");
            this.sndLand = this.sounds.acquire("land");
        }
        catch (Exception e) {
            this.modelError = "audio: " + e.getMessage();
        }
        if (this.config.audioEnabled()) {
            this.audio.setMaster(this.config.audioMaster());
            this.audio.start();
        }
        this.soundOn = this.config.audioEnabled();
        this.assets = new AssetManager(this.repository, this.textures, this.sounds);
        this.hud = new Hud();
        this.menus = new MenuThread(() -> {
            this.quitRequested = true;
        }, this::toggleWireframe, this::toggleSound, this::cycleBands);
        this.menus.syncOptions(this.currentMode() == Renderer.Mode.WIREFRAME, this.soundOn, this.config.renderBands());
    }

    private void rebuildRenderer() {
        try {
            List<Mesh> parts = this.repository.acquire(this.currentModel);
            ArrayList<Renderer.Instance> insts = new ArrayList<Renderer.Instance>();
            for (Mesh m : parts) {
                insts.add(new Renderer.Instance(m, Transform.of(Vec3.ZERO, this.config.modelScale())));
            }
            this.sceneInstances = List.copyOf(insts);
            if (this.pipeline == null) {
                this.pipeline = new RenderPipeline(this.textures, this.config.renderBands());
            }
            if (this.renderer != null && !this.currentModel.equals(this.lastBuiltModel)) {
                this.repository.release(this.lastBuiltModel);
            }
            this.lastBuiltModel = this.currentModel;
            this.modelError = null;
        }
        catch (Exception e) {
            this.modelError = this.currentModel + ": " + e.getMessage();
        }
    }

    private Renderer.Mode currentMode() {
        if (this.renderer != null) {
            return this.renderer.mode();
        }
        return this.config.renderMode().equals("wireframe") ? Renderer.Mode.WIREFRAME : Renderer.Mode.SOLID;
    }

    private void cycleModel() {
        List<String> ids;
        try {
            ids = this.repository.availableModels();
        }
        catch (Exception e) {
            return;
        }
        if (ids.isEmpty()) {
            ids = List.of("cube", "pyramid", "plane");
        }
        int i = ids.indexOf(this.currentModel);
        this.currentModel = ids.get((i + 1 + ids.size()) % ids.size());
        this.rebuildRenderer();
    }

    private static int lastKeyCode(InputFrame frame) {
        Set<Integer> pressed = frame.rawPressedCodes();
        for (int c : pressed) {
            if (!InputAction.isProfilerToggle(c)) continue;
            return c;
        }
        return -1;
    }

    private void respawnPlayer() {
        if (this.playerBody == null) {
            return;
        }
        Vec3 spawn = null;
        try {
            if (this.levels != null && this.levels.current() != null) {
                spawn = this.levels.current().spawn();
            }
        } catch (Exception ignored) {
        }
        if (spawn == null) {
            spawn = new Vec3(0.0f, this.config.groundY() + 2.0f, 2.0f);
        }
        this.playerBody.setPosition(spawn);
        this.playerBody.setVelocity(Vec3.ZERO);
        if (this.crateBody != null) {
            this.crateBody.setPosition(new Vec3(-2.0f, this.config.groundY() + 3.0f, -1.0f));
            this.crateBody.setVelocity(Vec3.ZERO);
        }
    }

    public void loadLevel(String id) throws IOException, LevelParseException, ObjParseException {
        LevelDef def = this.levels.load(id);
        this.currentLevel = id;
        this.levelError = null;
        this.levels.applyToWorld(this.physics, this.playerBody, new Vec3(2.5f, this.config.groundY() + 0.25f, -2.5f));
        if (this.crateBody != null) {
            this.crateBody.setPosition(new Vec3(def.spawn().x() - 2.0f, def.spawn().y() + 1.0f, def.spawn().z() - 3.0f));
            this.crateBody.setVelocity(Vec3.ZERO);
        }
        this.sceneInstances = this.levels.instances();
        this.currentModel = "level:" + id;
        // Level decor already set above: do NOT call rebuildRenderer() here,
        // it would try to acquire "level:demo" as a model file ("level:demo.obj").
        // Release the previous showcase model ("cube") if one was acquired.
        if (this.lastBuiltModel != null) {
            try {
                this.repository.release(this.lastBuiltModel);
            } catch (Exception ignored) {
            }
            this.lastBuiltModel = null;
        }
        this.modelError = null;
        if (this.hud != null) {
            this.hud.pushMessage("Level: " + def.name());
        }
    }

    public void cycleLevel() {
        List<String> ids;
        try {
            ids = this.levels.availableLevels();
        }
        catch (Exception e) {
            return;
        }
        if (ids.isEmpty()) {
            return;
        }
        int i = ids.indexOf(this.currentLevel);
        String next = ids.get((i + 1 + ids.size()) % ids.size());
        if (this.fx != null) {
            this.fx.transition(() -> {
                try {
                    this.loadLevel(next);
                }
                catch (Exception e) {
                    this.levelError = e.getMessage();
                }
            });
        } else {
            try {
                this.loadLevel(next);
            }
            catch (Exception e) {
                this.levelError = e.getMessage();
            }
        }
    }

    public void toggleWireframe() {
        Renderer.Mode next = this.currentMode() == Renderer.Mode.SOLID ? Renderer.Mode.WIREFRAME : Renderer.Mode.SOLID;
        this.renderer = new Renderer(this.framebuffer, this.sceneInstances.isEmpty() ? List.of(new Renderer.Instance(Mesh.createCube(1.6f), Transform.of(Vec3.ZERO, 1.0f))) : this.sceneInstances, this.config.fovDeg(), this.config.near(), this.config.far(), this.config.wireColor(), this.config.cubeColor());
        this.renderer.setMode(next);
        if (this.menus != null) {
            this.menus.syncOptions(next == Renderer.Mode.WIREFRAME, this.soundOn, this.config.renderBands());
        }
        if (this.hud != null) {
            this.hud.pushMessage(next == Renderer.Mode.WIREFRAME ? "Wireframe" : "Solid");
        }
    }

    public void toggleSound() {
        boolean bl = this.soundOn = !this.soundOn;
        if (this.audio != null) {
            if (this.soundOn) {
                this.audio.setMaster(this.config.audioMaster());
                if (!this.config.audioEnabled()) {
                    this.audio.start();
                }
            } else {
                this.audio.setMaster(0.0f);
            }
        }
        if (this.menus != null) {
            this.menus.syncOptions(this.currentMode() == Renderer.Mode.WIREFRAME, this.soundOn, this.config.renderBands());
        }
        if (this.hud != null) {
            this.hud.pushMessage(this.soundOn ? "Sound ON" : "Sound MUTED");
        }
    }

    public void cycleBands() {
        int next;
        int cur = this.bandsOverride > 0 ? this.bandsOverride : this.config.renderBands();
        this.bandsOverride = next = cur >= 8 ? 1 : cur * 2;
        if (this.pipeline != null) {
            this.pipeline.shutdown();
            this.pipeline = null;
        }
        if (this.menus != null) {
            this.menus.syncOptions(this.currentMode() == Renderer.Mode.WIREFRAME, this.soundOn, next);
        }
        if (this.hud != null) {
            this.hud.pushMessage("Bands: " + next);
        }
    }

    public int effectiveBands() {
        return this.bandsOverride > 0 ? this.bandsOverride : this.config.renderBands();
    }

    private void handleFunctionKeys(InputFrame frame) {
        if (frame.pressed(InputAction.TOGGLE_WIREFRAME)) {
            this.toggleWireframe();
        }
        if (frame.pressed(InputAction.NEXT_MODEL)) {
            this.cycleModel();
        }
        if (frame.pressed(InputAction.NEXT_LEVEL)) {
            this.cycleLevel();
        }
        if (frame.pressed(InputAction.TRIGGER_FX) && this.fx != null && this.playerBody != null) {
            this.fx.explode(this.playerBody.position().add(new Vec3(0.0f, 1.0f, 0.0f)));
            if (this.hud != null) {
                this.hud.pushMessage("BOOM!");
            }
        }
        if (frame.pressed(InputAction.TOGGLE_CAMERA) && this.rig != null) {
            this.rig.toggleMode();
        }
        if (frame.pressed(InputAction.RESET_CAMERA) && this.rig != null) {
            this.rig.reset();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Set<CameraRig.Action> pollActions() {
        EnumSet<CameraRig.Action> a = EnumSet.noneOf(CameraRig.Action.class);
        Set<Integer> set = this.keysDown;
        synchronized (set) {
            for (int k : this.keysDown) {
                if (k == 87 || k == 90) {
                    a.add(CameraRig.Action.FWD);
                    continue;
                }
                if (k == 83) {
                    a.add(CameraRig.Action.BACK);
                    continue;
                }
                if (k == 65 || k == 81) {
                    a.add(CameraRig.Action.LEFT);
                    continue;
                }
                if (k == 68) {
                    a.add(CameraRig.Action.RIGHT);
                    continue;
                }
                if (k == 69) {
                    a.add(CameraRig.Action.UP);
                    continue;
                }
                if (k == 88) {
                    a.add(CameraRig.Action.DOWN);
                    continue;
                }
                if (k == 17) {
                    a.add(CameraRig.Action.DOWN);
                    continue;
                }
                if (k == 37) {
                    a.add(CameraRig.Action.TURN_L);
                    continue;
                }
                if (k == 39) {
                    a.add(CameraRig.Action.TURN_R);
                    continue;
                }
                if (k == 38) {
                    a.add(CameraRig.Action.LOOK_U);
                    continue;
                }
                if (k != 40) continue;
                a.add(CameraRig.Action.LOOK_D);
            }
        }
        return a;
    }

    private void loop() {
        FrameLimiter limiter = new FrameLimiter(this.config.fpsTarget());
        int tickRate = EngineConfig.clampTick(this.config.tickRate());
        this.tickPeriodNs = tickRate > 0 ? 1000000000L / (long) tickRate : 0L;
        this.logicAccumNs = 0L;
        this.droppedSimNs = 0L;
        long lastFpsTime = System.nanoTime();
        long frames = 0L;
        long lastNs = System.nanoTime();
        while (this.running.get()) {
            int iPipe;
            limiter.beginFrame();
            long now = System.nanoTime();
            float dt = Math.min(0.1f, (float)(now - lastNs) / 1.0E9f);
            lastNs = now;
            if (this.rig != null) {
                boolean nowPaused;
                int iInput;
                int n = iInput = this.profiler != null ? this.profiler.indexOf("input") : -1;
                if (iInput >= 0) {
                    this.profiler.begin(iInput);
                }
                InputFrame frame = this.inputState.pollFrame(this.actionMapper);
                boolean bl = nowPaused = this.menus != null && this.menus.handleFrame(frame);
                if (nowPaused != this.menuOpen) {
                    this.menuOpen = nowPaused;
                    if (this.input != null) {
                        this.input.setCaptured(!nowPaused && this.config.mouseCaptured());
                    }
                    if (this.hud != null) {
                        this.hud.pushMessage(nowPaused ? "Paused" : "Resumed!");
                    }
                }
                if (this.quitRequested) {
                    this.stop();
                    return;
                }
                if (!nowPaused) {
                    int iFx;
                    if (iInput >= 0) {
                        this.profiler.end(iInput);
                    }
                    int iPhys = this.profiler.indexOf("phys");
                    int iAnim = this.profiler.indexOf("anim");
                    if (iPhys >= 0) {
                        this.profiler.begin(iPhys);
                    }
                    this.handleFunctionKeys(frame);
                    if (InputAction.isProfilerToggle(EngineKernel.lastKeyCode(frame))) {
                        boolean bl2 = this.showProfiler = !this.showProfiler;
                        if (this.hud != null) {
                            this.hud.pushMessage(this.showProfiler ? "Profiler ON" : "Profiler OFF");
                        }
                    }
                    if (frame.pressed(InputAction.RESET_CAMERA)) {
                        this.respawnPlayer();
                    }
                    if (this.playerBody != null && this.player != null) {
                        boolean jumpPressed = frame.pressed(InputAction.JUMP);
                        this.player.drive(this.playerBody, frame, PlayerController.cameraYaw(this.rig));
                        int tickHz = EngineConfig.clampTick(this.config.tickRate());
                        if (tickHz <= 0) {
                            this.physics.setFixedDt(Math.max(0.0005f, dt));
                        } else {
                            this.physics.setFixedDt(1.0f / (float) tickHz);
                        }
                        this.physics.update(dt);
                        if (this.audio != null && this.sndJump != null) {
                            this.audio.setListener(this.rig.toCamera().position(), PlayerController.cameraYaw(this.rig));
                            if (jumpPressed && this.playerBody.velocity().y() > 0.5f) {
                                this.audio.playAt(this.sndJump, this.playerBody.position());
                                if (this.fx != null) {
                                    this.fx.jump(this.playerBody.position());
                                }
                            }
                            boolean grounded = this.playerBody.onGround();
                            if (!this.wasGrounded && grounded) {
                                this.audio.playAt(this.sndLand, this.playerBody.position());
                                if (this.fx != null) {
                                    this.fx.land(this.playerBody.position());
                                }
                            }
                            this.wasGrounded = grounded;
                            Vec3 pv2 = this.playerBody.velocity();
                            float hs = (float)Math.sqrt(pv2.x() * pv2.x() + pv2.z() * pv2.z());
                            if (grounded && hs > 1.0f) {
                                this.stepAccum += hs * dt;
                                if (this.stepAccum > 1.6f) {
                                    this.stepAccum = 0.0f;
                                    this.audio.playAt(this.sndStep, this.playerBody.position());
                                    if (this.fx != null) {
                                        this.fx.footstep(this.playerBody.position());
                                    }
                                }
                            } else {
                                this.stepAccum = 0.0f;
                            }
                        }
                        if (iPhys >= 0) {
                            this.profiler.end(iPhys);
                        }
                        if (iAnim >= 0) {
                            this.profiler.begin(iAnim);
                        }
                        if (this.animFsm != null && this.playerAnimator != null) {
                            Vec3 pv = this.playerBody.velocity();
                            float hSpeed = (float)Math.sqrt(pv.x() * pv.x() + pv.z() * pv.z());
                            this.animFsm.update(hSpeed, this.playerBody.onGround(), pv.y() > 1.0f);
                            this.playerAnimator.update(dt);
                        }
                        if (iAnim >= 0) {
                            this.profiler.end(iAnim);
                        }
                        this.rig.setTarget(this.playerBody.position().add(new Vec3(0.0f, 0.6f, 0.0f)));
                        if (this.rig.mode() == CameraRig.Mode.FPS) {
                            this.rig.setFpsPos(this.playerBody.position().add(new Vec3(0.0f, 0.5f, 0.0f)));
                        }
                    }
                    if ((iFx = this.profiler.indexOf("fx")) >= 0) {
                        this.profiler.begin(iFx);
                    }
                    if (this.fx != null) {
                        this.fx.update(dt);
                        this.particleCount = this.fx.pool().aliveCount();
                    }
                    if (iFx >= 0) {
                        this.profiler.end(iFx);
                    }
                    InputThread.driveCameraPlayer(this.rig, frame, dt, this.config.mouseSensitivity(), this.config.wheelZoom());
                    this.lastInputFrame = frame;
                } else {
                    this.lastInputFrame = InputFrame.empty();
                }
            }
            this.spinAngle += dt * 0.9f;
            int n = iPipe = this.profiler != null ? this.profiler.indexOf("pipe") : -1;
            if (iPipe >= 0) {
                this.profiler.begin(iPipe);
            }
            int tris = this.renderViaPipeline(dt);
            if (iPipe >= 0) {
                this.profiler.end(iPipe);
            }
            if (this.profiler != null) {
                this.profiler.nextFrame();
            }
            this.trianglesValue.set(tris);
            this.present();
            ++frames;
            long t = System.nanoTime();
            if (t - lastFpsTime >= 1000000000L) {
                this.fpsValue.set(frames);
                frames = 0L;
                lastFpsTime = t;
            }
            limiter.endFrame();
        }
    }

    private SceneSnapshot snapshot(float dtUnused) {
        List all;
        ArrayList<Renderer.Instance> dyn = new ArrayList<Renderer.Instance>(this.sceneInstances.size() + 12);
        Vec3 camPos = this.rig != null ? this.rig.toCamera().position() : Vec3.ZERO;
        this.lodDrawn = 0;
        for (Renderer.Instance inst : this.sceneInstances) {
            if (this.config.lodEnabled()) {
                float d = inst.transform().position().distance(camPos);
                Mesh lod = LodMesh.forDistance(inst.mesh(), d);
                if (lod != inst.mesh()) {
                    ++this.lodDrawn;
                    dyn.add(new Renderer.Instance(lod, inst.transform()));
                    continue;
                }
            }
            dyn.add(inst);
        }
        if (this.playerBody != null && this.playerRig != null) {
            Vec3 rootPos = this.playerBody.position().sub(new Vec3(0.0f, 0.05f, 0.0f));
            dyn.addAll(Humanoid.toInstances(this.playerRig, new Transform(rootPos, Quaternion.IDENTITY, Vec3.ONE)));
            if (this.crateBody != null) {
                dyn.add(new Renderer.Instance(Mesh.createCube(0.7f), new Transform(this.crateBody.position(), Quaternion.IDENTITY, Vec3.ONE)));
            }
        }
        return new SceneSnapshot((all = List.copyOf(dyn)).isEmpty() ? List.of(new Renderer.Instance(Mesh.createCube(1.6f), Transform.of(Vec3.ZERO, 1.0f))) : all, this.rig != null ? this.rig.toCamera() : Camera.defaultCamera(), this.spinAngle, new Light(new Vec3(this.config.lightDirX(), this.config.lightDirY(), this.config.lightDirZ()).normalize(), Lighting.fromRgb(this.config.lightColor()), this.config.lightIntensity()), this.config.ambient(), this.config.textureFilter().equals("nearest") ? Texture.Filter.NEAREST : Texture.Filter.BILINEAR, this.currentMode(), this.config.clearColor(), this.config.wireColor(), (float)Math.toRadians(this.config.fovDeg()), this.config.near(), this.config.far(), this.config.windowWidth(), this.config.windowHeight());
    }

    private int renderViaPipeline(float dtUnused) {
        try {
            if (this.pipeline == null) {
                this.pipeline = new RenderPipeline(this.textures, this.effectiveBands());
            }
            SceneSnapshot snap = this.snapshot(dtUnused);
            int tris;
            // Synchronous fast path for small scenes: run the three stages
            // directly on the loop thread instead of rendezvousing the stage
            // threads through two phasers + two latches + band invokeAll.
            // Same pixels out, no thread wake/sleep jitter.
            if (RenderPipeline.shouldRunDirect(snap)) {
                tris = RenderPipeline.renderDirect(snap, this.framebuffer, this.textures, this.effectiveBands(), this.bandPool);
            } else {
                tris = this.pipeline.renderFrame(snap, this.framebuffer);
            }
            if (this.fx != null && this.rig != null) {
                tris += ParticleRenderer.render(this.fx.pool(), this.framebuffer, this.rig.toCamera(), (float)Math.toRadians(this.config.fovDeg()), this.config.clearColor());
                this.fx.post().apply(this.framebuffer);
            }
            return tris;
        }
        catch (Exception e) {
            this.modelError = "pipeline: " + e.getMessage();
            return 0;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void present() {
        BufferStrategy bs = this.canvas.getBufferStrategy();
        if (bs == null) {
            this.canvas.createBufferStrategy(2);
            return;
        }
        Graphics g = null;
        try {
            g = bs.getDrawGraphics();
            int w = this.canvas.getWidth();
            int h = this.canvas.getHeight();
            g.drawImage(this.framebuffer.image(), 0, 0, w, h, null);
            long ageMs = (System.nanoTime() - this.bootNs) / 1000000L;
            if (ageMs < Splash.durationMs()) {
                Splash.draw(g, w, h, "JAVA 3D ENGINE", "v1.0 - playable demo", new String[]{"ZQSD move - SPACE jump - G explosion", "N level - ESC menu - T profiler"});
            }
            if (this.hud != null && this.menus != null) {
                long nowHud = System.nanoTime();
                if (nowHud - this.lastHudCacheNs > 250000000L) {
                    this.lastHudCacheNs = nowHud;
                    String camInfo = this.rig != null ? this.rig.mode().name().toLowerCase() : "?";
                String status = this.modelError != null ? "ERROR " + this.modelError : "v1.0 - " + this.currentModel + " (" + this.currentMode().name().toLowerCase() + ", cam " + camInfo + ", " + this.effectiveBands() + " bands) SPACE=jump G=explosion N=level M=model C=cam R=respawn ESC=menu | FPS: " + this.fpsValue.get() + " / target " + (this.config.fpsTarget() == 0 ? "unlimited" : Integer.valueOf(this.config.fpsTarget())) + " | tris: " + this.trianglesValue.get() + " | sound: " + (this.soundOn && this.audio != null && this.audio.lineAvailable() ? "on" : "muted");
                String physInfo = this.playerBody != null ? new StringBuilder(96).append("player (").append((double) ((int) (this.playerBody.position().x() * 10.0f)) / 10.0).append(", ").append((double) ((int) (this.playerBody.position().y() * 10.0f)) / 10.0).append(", ").append((double) ((int) (this.playerBody.position().z() * 10.0f)) / 10.0).append(')').append(this.playerBody.onGround() ? " GROUND" : "").append(" [").append(this.playerAnimator != null ? this.playerAnimator.state().name().toLowerCase() : "?").append("] | V=wire/solid | ").append(this.physics.lastContactCount()).append(" contacts").toString() : "no physics";
                if (this.levelError != null) {
                    physInfo = "LEVEL: " + this.levelError + " | " + physInfo;
                }
                physInfo = physInfo + " PART " + this.particleCount + " LOD " + this.lodDrawn + " DROP " + this.droppedSimNs / 1000000L + "ms";
                this.cachedStatus = status;
                this.cachedPhys = physInfo;
                }
                this.hud.setInfo(this.cachedPhys);
                this.hud.setStatus(this.cachedStatus);
                this.hud.setSub("ZQSD/WASD player + SPACE jump + mouse, F capture T=prof");
                this.hud.setFps(this.showProfiler && this.profiler != null ? this.profiler.hudLine() : "assets=" + this.assets.totalCached());
                this.hud.drawHud(g, w, h);
                if (this.menus.paused() && this.menus.activeMenu() != null) {
                    this.hud.drawMenu(g, w, h, this.menus.activeMenu());
                }
            }
        }
        finally {
            if (g != null) {
                g.dispose();
            }
        }
        if (!bs.contentsLost()) {
            bs.show();
        }
    }

    public void stop() {
        this.running.set(false);
        if (this.loopThread != null) {
            try {
                this.loopThread.join(2000L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (this.input != null) {
            this.input.stop();
        }
        if (this.audio != null) {
            this.audio.stop();
        }
        if (this.pipeline != null) {
            this.pipeline.shutdown();
        }
        this.bandPool.shutdownNow();
        if (this.frame != null) {
            SwingUtilities.invokeLater(this.frame::dispose);
        }
    }

    public long fps() {
        return this.fpsValue.get();
    }

    public long triangles() {
        return this.trianglesValue.get();
    }

    public boolean isRunning() {
        return this.running.get();
    }
}
