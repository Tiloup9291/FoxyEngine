package engine.audio;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** SoundBank: synthetic sounds, ref counts, missing file errors. */
class SoundBankTest {

    @Test
    void syntheticAcquire(@TempDir Path tmp) throws Exception {
        SoundBank bank = new SoundBank(tmp);
        var jump = bank.acquire("jump");
        assertNotNull(jump);
        assertEquals(1, bank.refCount("jump"));
        bank.acquire("jump");
        assertEquals(2, bank.refCount("jump"));
        bank.release("jump");
        assertEquals(1, bank.refCount("jump"));
        bank.release("jump");
        assertEquals(0, bank.refCount("jump"));
    }

    @Test
    void missingSoundExplicit(@TempDir Path tmp) {
        SoundBank bank = new SoundBank(tmp);
        var e = assertThrows(java.io.IOException.class, () -> bank.acquire("missing"));
        assertTrue(e.getMessage().contains("not found"));
    }

    @Test
    void oggMessageExplicit(@TempDir Path tmp) throws Exception {
        java.nio.file.Files.write(tmp.resolve("music.ogg"), new byte[]{1, 2, 3});
        SoundBank bank = new SoundBank(tmp);
        var e = assertThrows(java.io.IOException.class, () -> bank.acquire("music"));
        assertTrue(e.getMessage().contains("OGG"));
    }
}
