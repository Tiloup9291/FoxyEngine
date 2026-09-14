package engine.anim;

import engine.anim.Channel;
import engine.anim.Keyframe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Track {
    private final String nodeName;
    private final Channel channel;
    private final List<Keyframe> keys;

    public Track(String nodeName, Channel channel, List<Keyframe> keys) {
        if (nodeName == null || nodeName.isBlank()) {
            throw new IllegalArgumentException("empty node");
        }
        if (channel == null) {
            throw new IllegalArgumentException("null channel");
        }
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("empty track");
        }
        ArrayList<Keyframe> sorted = new ArrayList<Keyframe>(keys);
        sorted.sort((a, b) -> Float.compare(a.time(), b.time()));
        this.nodeName = nodeName;
        this.channel = channel;
        this.keys = Collections.unmodifiableList(sorted);
    }

    public String nodeName() {
        return this.nodeName;
    }

    public Channel channel() {
        return this.channel;
    }

    public List<Keyframe> keys() {
        return this.keys;
    }

    public float duration() {
        return this.keys.get(this.keys.size() - 1).time();
    }

    public float sample(float t) {
        if (t <= this.keys.get(0).time()) {
            return this.keys.get(0).value();
        }
        Keyframe last = this.keys.get(this.keys.size() - 1);
        if (t >= last.time()) {
            return last.value();
        }
        for (int i = 0; i < this.keys.size() - 1; ++i) {
            Keyframe a = this.keys.get(i);
            Keyframe b = this.keys.get(i + 1);
            if (!(t >= a.time()) || !(t <= b.time())) continue;
            float span = b.time() - a.time();
            if (span < 1.0E-9f) {
                return b.value();
            }
            float u = (t - a.time()) / span;
            return a.value() + (b.value() - a.value()) * u;
        }
        return last.value();
    }
}
