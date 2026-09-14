package engine.render;

import engine.render.Lod;
import engine.render.Mesh;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LodMesh {
    public static final float NEAR = 8.0f;
    public static final float FAR = 20.0f;
    private static final Map<Mesh, Mesh[]> CACHE = new ConcurrentHashMap<Mesh, Mesh[]>();

    private LodMesh() {
    }

    public static Mesh forDistance(Mesh src, float dist) {
        int level = Lod.level(dist, 8.0f, 20.0f);
        if (level == 0) {
            return src;
        }
        Mesh[] lods = CACHE.computeIfAbsent(src, LodMesh::build);
        return lods[level];
    }

    private static Mesh[] build(Mesh src) {
        Mesh[] lods = new Mesh[]{src, Lod.decimate(src, 2), Lod.decimate(src, 4)};
        return lods;
    }

    public static int cachedCount() {
        return CACHE.size();
    }

    public static void clear() {
        CACHE.clear();
    }
}
