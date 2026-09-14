package engine.fx;

import engine.fx.Emitters;
import engine.fx.ParticlePool;
import engine.fx.PostProcess;
import engine.math.Vec3;
import java.util.Random;

public final class FXThread {
    private final ParticlePool pool;
    private final PostProcess post;
    private final Random rng;
    private float fadeDir;
    private Runnable fadeMid;
    private boolean fadeOutDone;

    public FXThread(int capacity, long seed) {
        this.pool = new ParticlePool(capacity);
        this.post = new PostProcess();
        this.rng = new Random(seed);
    }

    public ParticlePool pool() {
        return this.pool;
    }

    public PostProcess post() {
        return this.post;
    }

    public boolean fading() {
        return this.fadeDir != 0.0f;
    }

    public void explode(Vec3 center) {
        Emitters.explosion(this.pool, center, this.rng);
        this.post.flash(0.55f, 0xFFDD88);
    }

    public void footstep(Vec3 feet) {
        Emitters.footstep(this.pool, feet, this.rng);
    }

    public void jump(Vec3 feet) {
        Emitters.jumpBurst(this.pool, feet, this.rng);
    }

    public void land(Vec3 feet) {
        Emitters.landPoof(this.pool, feet, this.rng);
        this.post.flash(0.12f, 0xFFFFFF);
    }

    public void transition(Runnable midAction) {
        if (this.fadeDir != 0.0f) {
            return;
        }
        this.fadeDir = -1.0f;
        this.fadeMid = midAction;
        this.fadeOutDone = false;
        this.post.setFade(0.0f);
    }

    public void update(float dt) {
        this.pool.update(dt);
        this.post.update(dt);
        if (this.fadeDir < 0.0f) {
            float f = this.post.fade() + dt * 2.0f;
            if (f >= 1.0f) {
                this.post.setFade(1.0f);
                if (!this.fadeOutDone) {
                    this.fadeOutDone = true;
                    if (this.fadeMid != null) {
                        Runnable r = this.fadeMid;
                        this.fadeMid = null;
                        r.run();
                    }
                }
                this.fadeDir = 1.0f;
            } else {
                this.post.setFade(f);
            }
        } else if (this.fadeDir > 0.0f) {
            float f = this.post.fade() - dt * 2.0f;
            if (f <= 0.0f) {
                this.post.setFade(0.0f);
                this.fadeDir = 0.0f;
            } else {
                this.post.setFade(f);
            }
        }
    }
}
