package engine.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/** Profiler: sections, HUD line, reset. */
class ProfilerTest {

    @Test
    void unknownSections() {
        Profiler p = new Profiler("a", "b");
        assertEquals(0, p.indexOf("a"));
        assertEquals(-1, p.indexOf("zzz"));
        assertEquals(0.0, p.avgMs(0), 1e-9);
    }

    @Test
    void beginEndMeasures() {
        Profiler p = new Profiler("x");
        p.begin(0);
        p.end(0);
        p.nextFrame();
        assertEquals(1, p.frameCount());
        assertTrue(p.lastMs(0) >= 0f);
        assertTrue(p.avgMs(0) >= 0.0);
    }

    @Test
    void hudLineListsSections() {
        Profiler p = new Profiler("input", "pipe");
        String line = p.hudLine();
        assertTrue(line.contains("input="));
        assertTrue(line.contains("pipe="));
        assertTrue(line.endsWith("ms"));
    }

    @Test
    void endWithoutBeginIgnored() {
        Profiler p = new Profiler("x");
        p.end(0);
        assertEquals(0.0, p.avgMs(0), 1e-9);
        p.begin(0);
        p.end(0);
        p.end(0);
        p.nextFrame();
        assertEquals(1, p.frameCount());
    }

    @Test
    void resetClears() {
        Profiler p = new Profiler("x");
        p.begin(0);
        p.end(0);
        p.reset();
        assertEquals(0, p.frameCount());
        assertEquals(0.0, p.avgMs(0), 1e-9);
        assertEquals(1, p.snapshotAvgMs().size());
        assertTrue(p.snapshotAvgMs().containsKey("x"));
    }
}
