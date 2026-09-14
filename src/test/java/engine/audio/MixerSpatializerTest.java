package engine.audio;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/** Mixer and spatializer: silence, voices, pan, distance. */
class MixerSpatializerTest {

    private static AudioClip tone(double freq, double seconds, int rate) {
        int n = (int) (seconds * rate);
        float[] s = new float[n];
        for (int i = 0; i < n; i++) s[i] = (float) Math.sin(2 * Math.PI * freq * i / rate);
        return new AudioClip(rate, 1, s);
    }

    @Test
    void silenceWhenEmpty() {
        AudioMixer m = new AudioMixer();
        float[] out = new float[64 * 2];
        m.mix(out, 64, true);
        for (float v : out) assertEquals(0f, v, 1e-6f);
    }

    @Test
    void voiceReleasedAfterEnd() {
        AudioMixer m = new AudioMixer();
        AudioClip c = tone(440, 0.01, 22050);
        m.play(c, 1f);
        assertEquals(1, m.voiceCount());
        float[] out = new float[512 * 2];
        m.mix(out, 512, true);
        assertEquals(0, m.voiceCount());
        float peak = 0f;
        for (float v : out) peak = Math.max(peak, Math.abs(v));
        assertTrue(peak > 0.05f, "silent mix");
    }

    @Test
    void loopNeverEnds() {
        AudioMixer m = new AudioMixer();
        m.play(tone(440, 0.01, 22050), 1f, 0f, 1.0, true);
        float[] out = new float[256 * 2];
        m.mix(out, 256, true);
        m.mix(out, 256, true);
        assertEquals(1, m.voiceCount());
    }

    @Test
    void voiceStealOnOverflow() {
        AudioMixer m = new AudioMixer();
        AudioClip c = tone(440, 1f, 22050);
        for (int i = 0; i < AudioMixer.MAX_VOICES + 4; i++) m.play(c, 0.5f);
        assertTrue(m.voiceCount() <= AudioMixer.MAX_VOICES);
    }

    @Test
    void constantPowerPan() {
        float[] center = Spatializer.constantPower(0f);
        assertEquals(center[0], center[1], 1e-5f);
        float[] left = Spatializer.constantPower(-1f);
        assertTrue(left[0] > left[1]);
        float[] right = Spatializer.constantPower(1f);
        assertTrue(right[1] > right[0]);
    }

    @Test
    void spatializeFallsWithDistance() {
        var near = Spatializer.spatialize(1f, 0f, 18f);
        var far = Spatializer.spatialize(17f, 0f, 18f);
        assertTrue(near.gain() > far.gain());
        assertTrue(near.pan() < far.pan() || near.pan() >= -1f);
    }

    @Test
    void audioThreadDegradedMixAhead() {
        AudioThread t = new AudioThread();
        AudioClip c = tone(440, 0.05, 22050);
        t.play(c, 0.8f);
        float[] out = new float[512 * 2];
        t.mixAhead(out, 512);
        float peak = 0f;
        for (float v : out) peak = Math.max(peak, Math.abs(v));
        assertTrue(peak > 0.05f, "mixAhead silent");
        assertTrue(t.mixedFrames() >= 512);
    }
}
