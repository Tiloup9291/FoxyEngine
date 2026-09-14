package engine.audio;

import engine.audio.AudioClip;
import engine.audio.Spatializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AudioMixer {
    public static final int MAX_VOICES = 16;
    private final List<Voice> voices = new ArrayList<Voice>();
    private float master = 1.0f;

    public void setMaster(float m) {
        this.master = Math.max(0.0f, Math.min(1.0f, m));
    }

    public float master() {
        return this.master;
    }

    public int voiceCount() {
        return this.voices.size();
    }

    public void clear() {
        this.voices.clear();
    }

    public void play(AudioClip clip, float gain, float pan, double pitchRate, boolean loop) {
        float[] lr = Spatializer.constantPower(pan);
        float l = gain * lr[0];
        float r = gain * lr[1];
        if (this.voices.size() >= 16) {
            for (int i = 0; i < this.voices.size(); ++i) {
                if (this.voices.get((int)i).loop) continue;
                this.voices.remove(i);
                break;
            }
            if (this.voices.size() >= 16) {
                this.voices.remove(0);
            }
        }
        this.voices.add(new Voice(clip, Math.max(0.01, pitchRate), l, r, loop));
    }

    public void play(AudioClip clip, float gain) {
        this.play(clip, gain, 0.0f, 1.0, false);
    }

    public void stopLoop(AudioClip clip) {
        this.voices.removeIf(v -> v.loop && v.clip == clip);
    }

    public void mix(float[] out, int frames, boolean clear) {
        if (clear) {
            Arrays.fill(out, 0, frames * 2, 0.0f);
        }
        for (int vi = this.voices.size() - 1; vi >= 0; --vi) {
            Voice v = this.voices.get(vi);
            AudioClip c = v.clip;
            int ch = c.channels();
            float[] s = c.samples();
            int total = c.frameCount();
            for (int f = 0; f < frames; ++f) {
                int idx = (int)v.pos;
                if (idx >= total) {
                    if (v.loop && total > 0) {
                        v.pos %= (double)total;
                        idx = (int)v.pos;
                    } else {
                        v.done = true;
                        break;
                    }
                }
                float m = ch == 1 ? s[idx] : (s[idx * 2] + s[idx * 2 + 1]) * 0.5f;
                int n = f * 2;
                out[n] = out[n] + m * v.left;
                int n2 = f * 2 + 1;
                out[n2] = out[n2] + m * v.right;
                v.pos += v.step;
            }
            if (!v.done) continue;
            this.voices.remove(vi);
        }
        for (int i = 0; i < frames * 2; ++i) {
            float x = out[i] * this.master;
            out[i] = (float)Math.tanh(x);
        }
    }

    public static final class Voice {
        final AudioClip clip;
        double pos;
        final double step;
        final float left;
        final float right;
        final boolean loop;
        boolean done;

        Voice(AudioClip clip, double step, float left, float right, boolean loop) {
            this.clip = clip;
            this.step = step;
            this.left = left;
            this.right = right;
            this.loop = loop;
        }
    }
}
