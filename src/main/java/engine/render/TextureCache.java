package engine.render;

import engine.render.Texture;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public final class TextureCache {
    private final Path texturesDir;
    private final ConcurrentHashMap<String, Texture> cache = new ConcurrentHashMap();

    public TextureCache(Path texturesDir) {
        this.texturesDir = texturesDir;
    }

    public Texture get(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return this.cache.computeIfAbsent(name, this::loadOrFallback);
    }

    private Texture loadOrFallback(String name) {
        try {
            Path f = this.texturesDir.resolve(name).normalize();
            return Texture.load(f);
        }
        catch (IOException | RuntimeException e) {
            return Texture.checker(2, 8, -65281, -16777216);
        }
    }

    public int cachedCount() {
        return this.cache.size();
    }

    public void clear() {
        this.cache.clear();
    }
}
