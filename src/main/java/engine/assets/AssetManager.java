package engine.assets;

import engine.assets.ModelRepository;
import engine.audio.SoundBank;
import engine.render.TextureCache;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AssetManager {
    private final ModelRepository models;
    private final TextureCache textures;
    private final SoundBank sounds;

    public AssetManager(ModelRepository models, TextureCache textures, SoundBank sounds) {
        if (models == null || textures == null || sounds == null) {
            throw new IllegalArgumentException("null stores");
        }
        this.models = models;
        this.textures = textures;
        this.sounds = sounds;
    }

    public ModelRepository models() {
        return this.models;
    }

    public TextureCache textures() {
        return this.textures;
    }

    public SoundBank sounds() {
        return this.sounds;
    }

    public Map<String, Map<String, Integer>> snapshotRefs() {
        LinkedHashMap out = new LinkedHashMap();
        out.put("models", Collections.unmodifiableMap(this.models.snapshotRefs()));
        LinkedHashMap<String, Integer> tex = new LinkedHashMap<String, Integer>();
        tex.put("cached", this.textures.cachedCount());
        out.put("textures", Collections.unmodifiableMap(tex));
        out.put("sounds", Collections.unmodifiableMap(this.sounds.snapshotRefs()));
        return Collections.unmodifiableMap(out);
    }

    public int totalCached() {
        return this.models.cachedCount() + this.textures.cachedCount() + this.sounds.cachedCount();
    }

    public void purgeTextures() {
        this.textures.clear();
    }
}
