package engine.render;

import engine.render.Texture;
import engine.render.TextureCache;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TextureTest {
    TextureTest() {
    }

    @Test
    void nearestCoinSuperieurGauche() {
        Texture t = new Texture(2, 2, new int[]{-65536, -16711936, -16776961, -1});
        Assertions.assertEquals((int)-65536, (int)t.sample(0.1f, 0.1f, Texture.Filter.NEAREST));
        Assertions.assertEquals((int)-16711936, (int)t.sample(0.9f, 0.1f, Texture.Filter.NEAREST));
        Assertions.assertEquals((int)-16776961, (int)t.sample(0.1f, 0.9f, Texture.Filter.NEAREST));
        Assertions.assertEquals((int)-1, (int)t.sample(0.9f, 0.9f, Texture.Filter.NEAREST));
    }

    @Test
    void wrapRepeat() {
        Texture t = new Texture(2, 1, new int[]{-65536, -16776961});
        Assertions.assertEquals((int)-65536, (int)t.sample(1.1f, 0.5f, Texture.Filter.NEAREST));
        Assertions.assertEquals((int)-65536, (int)t.sample(-0.9f, 0.5f, Texture.Filter.NEAREST));
    }

    @Test
    void bilinearCenterBlends4Texels() {
        Texture t = new Texture(2, 2, new int[]{-16777216, -65536, -16711936, -16776961});
        int c = t.sample(0.5f, 0.5f, Texture.Filter.BILINEAR);
        Assertions.assertEquals((int)-12566464, (int)c, () -> String.format("attendu 0xFF404040, recu 0x%08X", c));
    }

    @Test
    void checkerProcedural() {
        Texture t = Texture.checker((int)2, (int)4, (int)-1, (int)-16777216);
        Assertions.assertEquals((int)8, (int)t.width());
        Assertions.assertEquals((int)-1, (int)t.sample(0.1f, 0.1f, Texture.Filter.NEAREST));
        Assertions.assertEquals((int)-16777216, (int)t.sample(0.6f, 0.1f, Texture.Filter.NEAREST));
    }

    @Test
    void chargePngDisque() throws Exception {
        Texture t = Texture.load((Path)Path.of("assets", "textures", "checker.png"));
        Assertions.assertEquals((int)128, (int)t.width());
        Assertions.assertEquals((int)128, (int)t.height());
    }

    @Test
    void cacheFallbackDamier() throws Exception {
        Texture b;
        TextureCache cache = new TextureCache(Path.of("assets", "textures"));
        Texture a = cache.get("checker.png");
        Assertions.assertTrue((a == (b = cache.get("checker.png")) ? 1 : 0) != 0, (String)"same instance (cache)");
        Assertions.assertTrue((cache.get("inexistant.png") != null ? 1 : 0) != 0, (String)"fallback damier");
    }
}
