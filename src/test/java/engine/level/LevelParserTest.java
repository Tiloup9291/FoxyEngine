package engine.level;

import static java.nio.file.Files.writeString;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Parser: format, line errors, unknown keys ignored. */
class LevelParserTest {

    private static Path level(Path tmp, String name, String body) throws Exception {
        Path f = tmp.resolve(name + ".lvl");
        writeString(f, body);
        return f;
    }

    @Test
    void fullParse(@TempDir Path tmp) throws Exception {
        Path f = level(tmp, "t", "name=hello\nspawn=1,2,3\nground=0.5\n"
                + "entity=cube,1,2,3,45,1.5,collide\nstatic=0,1,0,2,2,2\nunknown=ignored\n");
        LevelDef d = LevelParser.parse(f);
        assertEquals("hello", d.name());
        assertEquals(1f, d.spawn().x(), 1e-6f);
        assertEquals(0.5f, d.groundY(), 1e-6f);
        assertEquals(1, d.entities().size());
        assertEquals("cube", d.entities().get(0).model());
        assertTrue(d.entities().get(0).collidable());
        assertEquals(1, d.statics().size());
    }

    @Test
    void defaultFileName(@TempDir Path tmp) throws Exception {
        Path f = tmp.resolve("mydungeon.lvl");
        writeString(f, "spawn=0,1,0\n");
        assertEquals("mydungeon", LevelParser.parse(f).name());
    }

    @Test
    void malformedLine(@TempDir Path tmp) throws Exception {
        Path f = tmp.resolve("bad.lvl");
        writeString(f, "spawn=0,1,0\nentity=cube,1,2\n");
        var e = assertThrows(LevelParseException.class, () -> LevelParser.parse(f));
        assertEquals(2, e.line());
        assertTrue(e.getMessage().contains("bad.lvl"));
    }
}
