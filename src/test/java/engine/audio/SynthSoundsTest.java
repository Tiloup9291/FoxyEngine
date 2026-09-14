package engine.audio;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/** Synthetic sounds: non-empty, correct rate, bounded envelopes. */
class SynthSoundsTest {

    @Test
    void builtinsExist() {
        assertNotNull(SynthSounds.get("jump"));
        assertNotNull(SynthSounds.get("step"));
        assertNotNull(SynthSounds.get("land"));
        assertNull(SynthSounds.get("nope"));
    }

    @Test
    void rateAndBounds() {
        for (String id : new String[]{"jump", "step", "land"}) {
            AudioClip c = SynthSounds.get(id);
            assertEquals(SynthSounds.RATE, c.sampleRate());
            assertTrue(c.frameCount() > 100, id);
            float peak = 0f;
            for (float s : c.samples()) peak = Math.max(peak, Math.abs(s));
            assertTrue(peak > 0.05f && peak <= 1.0f, id + " peak=" + peak);
        }
    }
}
