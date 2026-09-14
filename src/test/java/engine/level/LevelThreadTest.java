package engine.level;

import static java.nio.file.Files.writeString;
import static org.junit.jupiter.api.Assertions.*;
import engine.assets.ModelRepository;
import engine.math.Vec3;
import engine.physics.Body;
import engine.physics.PhysicsWorld;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** LevelThread: load/unload, ref counts, world apply. */
class LevelThreadTest {

    private static ModelRepository repo() {
        return new ModelRepository(Path.of("assets", "models"));
    }

    private static void level(Path tmp, String name, String body) throws Exception {
        writeString(tmp.resolve(name + ".lvl"), body);
    }

    @Test
    void loadRealDemo() throws Exception {
        ModelRepository r = repo();
        LevelThread lt = new LevelThread(Path.of("assets", "levels"), r);
        LevelDef def = lt.load("demo");
        assertEquals("demo", def.name());
        assertFalse(lt.instances().isEmpty());
        assertFalse(lt.levelStatics().isEmpty());
        assertEquals("demo", lt.currentId());
        lt.unload();
        assertEquals(java.util.List.of(), lt.instances());
        assertEquals(java.util.List.of(), lt.levelStatics());
    }

    @Test
    void refCountsAcrossLevels(@TempDir Path tmp) throws Exception {
        ModelRepository r = repo();
        level(tmp, "one", "entity=cube,0,1,0,0,1.0\n");
        level(tmp, "two", "entity=pyramid,0,1,0,0,1.0\n");
        LevelThread lt = new LevelThread(tmp, r);
        lt.load("one");
        assertEquals(1, r.refCount("cube"));
        lt.load("two");
        assertEquals(0, r.refCount("cube"));
        assertEquals(1, r.refCount("pyramid"));
        assertFalse(r.isCached("cube"));
        lt.unload();
        assertEquals(0, r.refCount("pyramid"));
    }

    @Test
    void unknownLevel(@TempDir Path tmp) {
        LevelThread lt = new LevelThread(tmp, repo());
        assertThrows(java.io.IOException.class, () -> lt.load("ghost"));
    }

    @Test
    void applyWorldAndSpawn(@TempDir Path tmp) throws Exception {
        level(tmp, "t", "spawn=7,3,7\nground=1.5\nstatic=0,2,0,2,4,2\n");
        PhysicsWorld w = new PhysicsWorld();
        Body player = Body.player(new Vec3(0, 5, 0));
        LevelThread lt = new LevelThread(tmp, repo());
        lt.load("t");
        lt.applyToWorld(w, player, new Vec3(0, 0, 0));
        assertEquals(1.5f, w.groundY(), 1e-6f);
        assertEquals(7f, player.position().x(), 1e-6f);
        assertEquals(1, w.statics().size());
    }

    @Test
    void emptyLevelKeepsCompatPlatform(@TempDir Path tmp) throws Exception {
        PhysicsWorld w = new PhysicsWorld();
        LevelThread lt = new LevelThread(tmp, repo());
        level(tmp, "empty", "spawn=0,1,0\n");
        lt.load("empty");
        lt.applyToWorld(w, null, new Vec3(9, 9, 9));
        assertEquals(1, w.statics().size());
    }

    @Test
    void collidableEntityBox(@TempDir Path tmp) throws Exception {
        level(tmp, "e", "entity=cube,3,0.5,-2,0,2.0,collide\n");
        LevelThread lt = new LevelThread(tmp, repo());
        lt.load("e");
        assertEquals(1, lt.levelStatics().size());
        var box = lt.levelStatics().get(0);
        assertEquals(3f, box.aabb().center().x(), 0.6f);
    }
}
