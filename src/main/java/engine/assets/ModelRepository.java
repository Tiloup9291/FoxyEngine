package engine.assets;

import engine.assets.ObjParseException;
import engine.assets.ObjParser;
import engine.render.Mesh;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class ModelRepository {
    private final Path modelsDir;
    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap();

    public ModelRepository(Path modelsDir) {
        this.modelsDir = modelsDir;
    }

    public List<Mesh> acquire(String id) throws IOException, ObjParseException {
        List<Mesh> loaded;
        Entry existing = this.cache.get(id);
        if (existing != null) {
            existing.refs.incrementAndGet();
            return existing.meshes;
        }
        try {
            loaded = this.load(id);
        }
        catch (ObjParseException | IOException | RuntimeException e) {
            this.cache.remove(id);
            throw e;
        }
        Entry fresh = new Entry(loaded);
        Entry prev = this.cache.putIfAbsent(id, fresh);
        Entry winner = prev != null ? prev : fresh;
        winner.refs.incrementAndGet();
        return winner.meshes;
    }

    public void release(String id) {
        Entry e = this.cache.get(id);
        if (e == null) {
            return;
        }
        if (e.refs.decrementAndGet() <= 0) {
            this.cache.remove(id, e);
        }
    }

    public int refCount(String id) {
        Entry e = this.cache.get(id);
        return e == null ? 0 : e.refs.get();
    }

    public boolean isCached(String id) {
        return this.cache.containsKey(id);
    }

    public int cachedCount() {
        return this.cache.size();
    }

    public static int triangleCount(List<Mesh> meshes) {
        int n = 0;
        for (Mesh m : meshes) {
            n += m.triangleCount();
        }
        return n;
    }

    private List<Mesh> load(String id) throws IOException, ObjParseException {
        Path file = this.modelsDir.resolve(id + ".obj").normalize();
        if (!Files.exists(file, new LinkOption[0])) {
            throw new IOException("model not found: " + file);
        }
        List<Mesh> parts = ObjParser.parseFile(file);
        return List.copyOf(parts);
    }

    public List<Mesh> acquireChecked(String id) throws IOException, ObjParseException {
        return this.acquire(id);
    }

    public List<String> availableModels() throws IOException {
        ArrayList<String> out = new ArrayList<String>();
        if (!Files.isDirectory(this.modelsDir, new LinkOption[0])) {
            return out;
        }
        try (Stream<Path> stream = Files.list(this.modelsDir);){
            for (Path p : (Iterable<Path>) stream::iterator) {
                String n = p.getFileName().toString();
                if (!n.toLowerCase().endsWith(".obj")) continue;
                out.add(n.substring(0, n.length() - 4));
            }
        }
        out.sort(String::compareTo);
        return out;
    }

    public Map<String, Integer> snapshotRefs() {
        HashMap<String, Integer> m = new HashMap<String, Integer>();
        this.cache.forEach((k, v) -> m.put((String)k, v.refs.get()));
        return m;
    }

    private static final class Entry {
        final List<Mesh> meshes;
        final AtomicInteger refs = new AtomicInteger(0);

        Entry(List<Mesh> meshes) {
            this.meshes = meshes;
        }
    }
}
