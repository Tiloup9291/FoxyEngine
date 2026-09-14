package engine.render;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/** LOD: thresholds, deterministic decimation, shared cache. */
class LodTest {

    @Test
    void thresholds() {
        assertEquals(0, Lod.level(5f, 8f, 20f));
        assertEquals(1, Lod.level(10f, 8f, 20f));
        assertEquals(2, Lod.level(25f, 8f, 20f));
    }

    @Test
    void deterministicDecimation() {
        Mesh src = Mesh.createCube(1f);
        Mesh half = Lod.decimate(src, 2);
        assertEquals(6, half.triangleCount());
        Mesh quarter = Lod.decimate(src, 4);
        assertEquals(3, quarter.triangleCount());
        assertEquals(half.triangleCount(), Lod.decimate(src, 2).triangleCount());
        assertSame(src, Lod.decimate(src, 1));
    }

    @Test
    void rawWithoutCopy() {
        Mesh src = Mesh.createCube(1f);
        assertSame(src.positionsRaw(), src.positionsRaw());
        assertEquals(src.positions().length, src.positionsRaw().length);
        assertEquals(src.indices().length, src.indicesRaw().length);
        assertNull(src.normalsRaw());
        assertNull(src.uvsRaw());
    }

    @Test
    void tooSmallKeepsOriginal() {
        Mesh src = Mesh.createCube(1f);
        assertSame(src, Lod.decimate(src, 100));
    }

    @Test
    void sharedCache() {
        LodMesh.clear();
        Mesh src = Mesh.createCube(1f);
        assertSame(src, LodMesh.forDistance(src, 1f));
        Mesh lod1a = LodMesh.forDistance(src, 10f);
        Mesh lod1b = LodMesh.forDistance(src, 12f);
        assertSame(lod1a, lod1b);
        assertTrue(lod1a.triangleCount() < src.triangleCount());
        Mesh lod2 = LodMesh.forDistance(src, 50f);
        assertTrue(lod2.triangleCount() <= lod1a.triangleCount());
        assertEquals(1, LodMesh.cachedCount());
    }

    @Test
    void configParses() {
        Properties p = new Properties();
        p.setProperty("engine.lod.enabled", "false");
        p.setProperty("engine.profiler.enabled", "false");
        engine.core.EngineConfig c = engine.core.EngineConfig.fromProperties(p);
        assertFalse(c.lodEnabled());
        assertFalse(c.profilerEnabled());
        engine.core.EngineConfig d = engine.core.EngineConfig.fromProperties(new Properties());
        assertTrue(d.lodEnabled());
        assertTrue(d.profilerEnabled());
    }
}
