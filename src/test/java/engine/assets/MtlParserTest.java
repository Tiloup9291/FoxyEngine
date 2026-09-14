package engine.assets;

import engine.assets.MtlParser;
import engine.render.Material;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MtlParserTest {
    MtlParserTest() {
    }

    @Test
    void parsesKdAndOpacity(@TempDir Path tmp) throws Exception {
        Path mtl = tmp.resolve("test.mtl");
        Files.writeString(mtl, (CharSequence)"newmtl red\nKd 1 0 0\nd 0.5\n", new OpenOption[0]);
        Map mats = MtlParser.parse((Path)mtl);
        Assertions.assertEquals((int)0xFF0000, (int)((Material)mats.get("red")).diffuseRgb());
        Assertions.assertEquals((float)0.5f, (float)((Material)mats.get("red")).opacity(), (float)1.0E-5f);
    }

    @Test
    void parsesPhongAndMapKd(@TempDir Path tmp) throws Exception {
        Path mtl = tmp.resolve("test.mtl");
        Files.writeString(mtl, (CharSequence)"newmtl shiny\nKa 0.1 0.1 0.1\nKd 0.5 0.5 0.5\nKs 0.9 0.9 0.9\nNs 64\nmap_Kd tex.png\n", new OpenOption[0]);
        Map mats = MtlParser.parse((Path)mtl);
        Material m = (Material)mats.get("shiny");
        Assertions.assertNotNull((Object)m);
        Assertions.assertEquals((float)0.1f, (float)m.ambient().x(), (float)1.0E-5f);
        Assertions.assertEquals((float)0.9f, (float)m.specular().z(), (float)1.0E-5f);
        Assertions.assertEquals((float)64.0f, (float)m.shininess(), (float)1.0E-5f);
        Assertions.assertEquals((Object)"tex.png", (Object)m.mapKd());
        Assertions.assertTrue((boolean)m.hasTexture());
    }

    @Test
    void missingFileReturnsEmpty(@TempDir Path tmp) throws Exception {
        Assertions.assertTrue((boolean)MtlParser.parse((Path)tmp.resolve("absent.mtl")).isEmpty());
    }
}
