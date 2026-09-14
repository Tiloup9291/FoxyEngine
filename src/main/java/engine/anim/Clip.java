package engine.anim;

import engine.anim.Node;
import engine.anim.Rig;
import engine.anim.Track;
import java.util.Collections;
import java.util.List;

public final class Clip {
    private final String name;
    private final List<Track> tracks;
    private final float duration;
    private final boolean loop;

    public Clip(String name, List<Track> tracks, boolean loop) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("empty name");
        }
        if (tracks == null || tracks.isEmpty()) {
            throw new IllegalArgumentException("empty clip");
        }
        this.name = name;
        this.tracks = Collections.unmodifiableList(List.copyOf(tracks));
        float d = 0.0f;
        for (Track t : tracks) {
            d = Math.max(d, t.duration());
        }
        this.duration = d;
        this.loop = loop;
    }

    public String name() {
        return this.name;
    }

    public List<Track> tracks() {
        return this.tracks;
    }

    public float duration() {
        return this.duration;
    }

    public boolean loop() {
        return this.loop;
    }

    public void apply(Rig rig, float time) {
        float t = time;
        if (this.loop && this.duration > 1.0E-9f && (t = time % this.duration) < 0.0f) {
            t += this.duration;
        }
        rig.resetPose();
        for (Track tr : this.tracks) {
            Node n = rig.find(tr.nodeName());
            if (n == null) continue;
            n.setChannel(tr.channel(), tr.sample(t));
        }
    }
}
