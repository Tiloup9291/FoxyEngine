package engine.pipeline;

import engine.pipeline.ProjectionStage;
import engine.pipeline.RenderStage;
import engine.pipeline.SceneSnapshot;
import engine.pipeline.TransformStage;
import engine.render.Framebuffer;
import engine.render.Frustum;
import engine.render.Renderer;
import engine.render.TextureCache;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class RenderPipeline {
    private final ExecutorService[] stages = new ExecutorService[3];
    private final ExecutorService bandPool;
    private final Phaser phaserIn = new Phaser(4);
    private final Phaser phaserOut = new Phaser(4);
    private final AtomicReference<Slot> slot = new AtomicReference<Slot>(new Slot());
    private volatile boolean running = true;
    private final int bands;
    private final TextureCache textures;
    private volatile int lastCulled;
    private volatile int lastBackfaced;

    public RenderPipeline(TextureCache textures, int bands) {
        this.textures = textures;
        this.bands = Math.max(1, bands);
        String[] names = new String[]{"Transform", "Projection", "Render"};
        for (int i = 0; i < 3; ++i) {
            String name = "Pipeline-" + names[i];
            this.stages[i] = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, name);
                t.setDaemon(true);
                return t;
            });
        }
        this.bandPool = Executors.newFixedThreadPool(Math.max(2, this.bands), r -> {
            Thread t = new Thread(r, "RenderBand");
            t.setDaemon(true);
            return t;
        });
        this.stages[0].submit(this::transformLoop);
        this.stages[1].submit(this::projectionLoop);
        this.stages[2].submit(this::renderLoop);
    }

    public int renderFrame(SceneSnapshot snap, Framebuffer fb) throws Exception {
        Slot s = new Slot();
        s.snap = snap;
        s.fb = fb;
        this.slot.set(s);
        this.phaserIn.arriveAndAwaitAdvance();
        this.phaserOut.arriveAndAwaitAdvance();
        if (s.error != null) {
            Throwable throwable = s.error;
            if (throwable instanceof Exception) {
                Exception e = (Exception)throwable;
                throw e;
            }
            throw new RuntimeException(s.error);
        }
        this.lastCulled = s.culled;
        this.lastBackfaced = s.backfaced;
        return s.drawn;
    }

    public int lastCulled() {
        return this.lastCulled;
    }

    public int lastBackfaced() {
        return this.lastBackfaced;
    }

    public List<String> stageNames() {
        return List.of("Transform", "Projection", "Render");
    }

    private void transformLoop() {
        while (this.running) {
            this.phaserIn.arriveAndAwaitAdvance();
            if (!this.running) {
                return;
            }
            try {
                Slot s = this.slot.get();
                s.tr = TransformStage.run(s.snap, TransformStage.frustumOf(s.snap));
                s.culled = s.tr.culled();
            }
            catch (Throwable t) {
                this.slot.get().error = t;
            }
            finally {
                this.slot.get().transformDone.countDown();
            }
            this.phaserOut.arriveAndAwaitAdvance();
        }
    }

    private void projectionLoop() {
        while (this.running) {
            this.phaserIn.arriveAndAwaitAdvance();
            if (!this.running) {
                return;
            }
            try {
                Slot s = this.slot.get();
                if (!s.transformDone.await(10L, TimeUnit.SECONDS)) {
                    s.error = new IllegalStateException("timeout Transform");
                } else if (s.error == null) {
                    s.pr = ProjectionStage.run(s.snap, s.tr);
                    s.backfaced = s.pr.backfaceCulled();
                }
            }
            catch (Throwable t) {
                this.slot.get().error = t;
            }
            finally {
                this.slot.get().projectionDone.countDown();
            }
            this.phaserOut.arriveAndAwaitAdvance();
        }
    }

    private void renderLoop() {
        while (this.running) {
            this.phaserIn.arriveAndAwaitAdvance();
            if (!this.running) {
                return;
            }
            try {
                Slot s = this.slot.get();
                if (!s.projectionDone.await(10L, TimeUnit.SECONDS)) {
                    s.error = new IllegalStateException("timeout Projection");
                } else if (s.error == null) {
                    RenderStage.RenderStats stats = RenderStage.run(s.snap, s.pr, s.fb, this.textures, this.bandPool, this.bands);
                    s.drawn = stats.triangles();
                }
            }
            catch (Throwable t) {
                this.slot.get().error = t;
            }
            this.phaserOut.arriveAndAwaitAdvance();
        }
    }

    public void shutdown() {
        this.running = false;
        try {
            this.phaserIn.arriveAndDeregister();
            this.phaserOut.arriveAndDeregister();
        }
        catch (IllegalStateException illegalStateException) {
            // empty catch block
        }
        for (ExecutorService e : this.stages) {
            e.shutdownNow();
        }
        this.bandPool.shutdownNow();
    }

    /** Max triangles for the synchronous path (no stage-thread rendezvous). */
    public static final int DIRECT_TRIANGLE_LIMIT = 4096;

    /**
     * True when the snapshot is small enough that the stage-thread pipeline
     * costs more than the work itself. The direct path produces identical
     * pixels via the same stages on the calling thread.
     */
    public static boolean shouldRunDirect(SceneSnapshot snap) {
        if (snap == null || snap.instances() == null) {
            return true;
        }
        long tris = 0L;
        for (Renderer.Instance inst : snap.instances()) {
            if (inst == null || inst.mesh() == null) {
                continue;
            }
            tris += inst.mesh().triangleCount();
            if (tris > DIRECT_TRIANGLE_LIMIT) {
                return false;
            }
        }
        return true;
    }

    public static int renderDirect(SceneSnapshot snap, Framebuffer fb, TextureCache textures, int bands, ExecutorService bandPool) throws Exception {
        Frustum frustum = TransformStage.frustumOf(snap);
        TransformStage.TransformResult tr = TransformStage.run(snap, frustum);
        ProjectionStage.ProjectionResult pr = ProjectionStage.run(snap, tr);
        RenderStage.RenderStats stats = RenderStage.run(snap, pr, fb, textures, bandPool, bands);
        return stats.triangles();
    }

    static final class Slot {
        volatile SceneSnapshot snap;
        volatile TransformStage.TransformResult tr;
        volatile ProjectionStage.ProjectionResult pr;
        volatile Framebuffer fb;
        volatile Throwable error;
        volatile int drawn;
        volatile int culled;
        volatile int backfaced;
        final CountDownLatch transformDone = new CountDownLatch(1);
        final CountDownLatch projectionDone = new CountDownLatch(1);

        Slot() {
        }
    }
}
