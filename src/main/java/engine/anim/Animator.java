package engine.anim;

import engine.anim.Channel;
import engine.anim.Clip;
import engine.anim.Node;
import engine.anim.Rig;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class Animator {
    private final Rig rig;
    private final Map<State, Clip> clips;
    private State state = State.IDLE;
    private Clip current;
    private Clip fadingFrom;
    private float fadeDuration = 0.15f;
    private float fadeTime;
    private float time;
    private float speedScale = 1.0f;
    private final Map<String, float[]> fadePose = new HashMap<String, float[]>();

    public Animator(Rig rig, Map<State, Clip> clips) {
        if (rig == null) {
            throw new IllegalArgumentException("null rig");
        }
        if (clips == null || !clips.containsKey((Object)State.IDLE)) {
            throw new IllegalArgumentException("IDLE clip required");
        }
        this.rig = rig;
        this.clips = new EnumMap<State, Clip>(clips);
        this.current = clips.get((Object)State.IDLE);
    }

    public State state() {
        return this.state;
    }

    public Clip current() {
        return this.current;
    }

    public float time() {
        return this.time;
    }

    public float fadeDuration() {
        return this.fadeDuration;
    }

    public void setFadeDuration(float d) {
        this.fadeDuration = Math.max(0.0f, d);
    }

    public void setSpeedScale(float s) {
        this.speedScale = Math.max(0.0f, s);
    }

    public float speedScale() {
        return this.speedScale;
    }

    public boolean fading() {
        return this.fadingFrom != null;
    }

    public void play(State next) {
        if (next == this.state || !this.clips.containsKey((Object)next)) {
            return;
        }
        this.snapshotPose();
        this.fadingFrom = this.current;
        this.state = next;
        this.current = this.clips.get((Object)next);
        this.time = 0.0f;
        this.fadeTime = 0.0f;
    }

    private void snapshotPose() {
        this.fadePose.clear();
        for (Node n : this.rig.nodes()) {
            float[] v = new float[Channel.values().length];
            for (Channel c : Channel.values()) {
                v[c.ordinal()] = n.getChannel(c);
            }
            this.fadePose.put(n.name(), v);
        }
    }

    public void update(float dt) {
        this.time += dt * this.speedScale;
        this.current.apply(this.rig, this.time);
        if (this.fadingFrom != null) {
            this.fadeTime += dt;
            float u = this.fadeDuration < 1.0E-9f ? 1.0f : Math.min(1.0f, this.fadeTime / this.fadeDuration);
            for (Node n : this.rig.nodes()) {
                float[] old = this.fadePose.get(n.name());
                if (old == null) continue;
                for (Channel c : Channel.values()) {
                    float now = n.getChannel(c);
                    n.setChannel(c, old[c.ordinal()] + (now - old[c.ordinal()]) * u);
                }
            }
            if (u >= 1.0f) {
                this.fadingFrom = null;
                this.fadePose.clear();
            }
        }
    }

    public static enum State {
        IDLE,
        WALK,
        RUN,
        JUMP;

    }
}
