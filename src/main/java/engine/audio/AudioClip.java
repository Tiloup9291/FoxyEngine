package engine.audio;

import java.util.Arrays;

public record AudioClip(int sampleRate, int channels, float[] samples) {
    public AudioClip {
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("sampleRate <= 0");
        }
        if (channels != 1 && channels != 2) {
            throw new IllegalArgumentException("channels != 1/2");
        }
        if (samples == null || samples.length == 0 || samples.length % channels != 0) {
            throw new IllegalArgumentException("invalid samples");
        }
        samples = (float[])samples.clone();
    }

    public int frameCount() {
        return this.samples.length / this.channels;
    }

    public double durationSec() {
        return (double)this.frameCount() / (double)this.sampleRate;
    }

    public float monoAt(int frame) {
        int i = frame * this.channels;
        if (this.channels == 1) {
            return this.samples[i];
        }
        return (this.samples[i] + this.samples[i + 1]) * 0.5f;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AudioClip)) {
            return false;
        }
        AudioClip c = (AudioClip)o;
        return this.sampleRate == c.sampleRate && this.channels == c.channels && Arrays.equals(this.samples, c.samples);
    }

    @Override
    public int hashCode() {
        int h = this.sampleRate * 31 + this.channels;
        return h * 31 + Arrays.hashCode(this.samples);
    }
}
