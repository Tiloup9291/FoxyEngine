package engine.assets;

import static org.junit.jupiter.api.Assertions.*;
import engine.render.TextureCache;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** AssetManager: facade, snapshot, purge. */
class AssetManagerTest {

    private static AssetManager manager() {
        return new AssetManager(
                new ModelRepository(Path.of("assets", "models")),
                new TextureCache(Path.of("assets", "textures")),
                new engine.audio.SoundBank(Path.of("assets", "sounds")));
    }

    @Test
    void snapshotStructure() {
        AssetManager m = manager();
        var snap = m.snapshotRefs();
        assertTrue(snap.containsKey("models"));
        assertTrue(snap.containsKey("textures"));
        assertTrue(snap.containsKey("sounds"));
        assertEquals(0, m.totalCached());
    }

    @Test
    void acquireReleasePassthrough() throws Exception {
        AssetManager m = manager();
        m.models().acquire("cube");
        assertEquals(1, m.models().refCount("cube"));
        assertEquals(1, m.totalCached() - m.textures().cachedCount() - m.sounds().cachedCount());
        assertEquals(1, m.snapshotRefs().get("models").get("cube"));
        m.models().release("cube");
        assertEquals(0, m.models().refCount("cube"));
    }
}
