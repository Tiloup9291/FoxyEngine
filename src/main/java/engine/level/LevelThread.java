package engine.level;

import engine.assets.ModelRepository;
import engine.assets.ObjParseException;
import engine.level.LevelDef;
import engine.level.LevelParseException;
import engine.level.LevelParser;
import engine.math.AABB;
import engine.math.Quaternion;
import engine.math.Vec3;
import engine.physics.Body;
import engine.physics.Collider;
import engine.physics.PhysicsWorld;
import engine.render.Mesh;
import engine.render.Renderer;
import engine.render.Transform;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class LevelThread {
    private final Path levelsDir;
    private final ModelRepository repository;
    private String currentId;
    private LevelDef current;
    private final List<String> acquiredModels = new ArrayList<String>();
    private List<Renderer.Instance> instances = List.of();
    private List<Collider.Box> levelStatics = List.of();

    public LevelThread(Path levelsDir, ModelRepository repository) {
        this.levelsDir = levelsDir;
        this.repository = repository;
    }

    public String currentId() {
        return this.currentId;
    }

    public LevelDef current() {
        return this.current;
    }

    public List<Renderer.Instance> instances() {
        return this.instances;
    }

    public List<Collider.Box> levelStatics() {
        return this.levelStatics;
    }

    public List<String> availableLevels() throws IOException {
        ArrayList<String> out = new ArrayList<String>();
        if (!Files.isDirectory(this.levelsDir, new LinkOption[0])) {
            return out;
        }
        try (Stream<Path> stream = Files.list(this.levelsDir);){
            for (Path p : (Iterable<Path>)stream::iterator) {
                String n = p.getFileName().toString();
                if (!n.toLowerCase().endsWith(".lvl")) continue;
                out.add(n.substring(0, n.length() - 4));
            }
        }
        out.sort(String::compareTo);
        return out;
    }

    public LevelDef load(String id) throws IOException, LevelParseException, ObjParseException {
        Path file = this.levelsDir.resolve(id + ".lvl").normalize();
        if (!Files.exists(file, new LinkOption[0])) {
            throw new IOException("level not found: " + file);
        }
        LevelDef def = LevelParser.parse(file);
        this.unload();
        ArrayList<Renderer.Instance> inst = new ArrayList<Renderer.Instance>();
        ArrayList<Collider.Box> boxes = new ArrayList<Collider.Box>();
        try {
            for (LevelDef.Entity e : def.entities()) {
                List<Mesh> parts = this.repository.acquire(e.model());
                this.acquiredModels.add(e.model());
                Quaternion rot = Quaternion.fromAxisAngle(Vec3.UNIT_Y, (float)Math.toRadians(e.yawDeg()));
                Vec3 s = new Vec3(e.scale(), e.scale(), e.scale());
                for (Mesh m : parts) {
                    inst.add(new Renderer.Instance(m, new Transform(e.pos(), rot, s)));
                }
                if (!e.collidable()) continue;
                boxes.add(LevelThread.entityBox(e, parts));
            }
            for (LevelDef.StaticBox s : def.statics()) {
                boxes.add(Collider.Box.centered(s.center(), s.size()));
            }
        }
        catch (IOException | RuntimeException ex) {
            for (String m : this.acquiredModels) {
                try {
                    this.repository.release(m);
                }
                catch (Exception exception) {}
            }
            this.acquiredModels.clear();
            if (ex instanceof IOException) {
                IOException io = (IOException)ex;
                throw io;
            }
            throw new IOException("loading level '" + id + "' : " + ex.getMessage(), ex);
        }
        catch (ObjParseException ex) {
            for (String m : this.acquiredModels) {
                try {
                    this.repository.release(m);
                }
                catch (Exception exception) {}
            }
            this.acquiredModels.clear();
            throw new IOException("level OBJ model '" + id + "' : " + ex.getMessage(), ex);
        }
        this.currentId = id;
        this.current = def;
        this.instances = List.copyOf(inst);
        this.levelStatics = List.copyOf(boxes);
        return def;
    }

    public void unload() {
        for (String m : this.acquiredModels) {
            try {
                this.repository.release(m);
            }
            catch (Exception exception) {}
        }
        this.acquiredModels.clear();
        this.currentId = null;
        this.current = null;
        this.instances = List.of();
        this.levelStatics = List.of();
    }

    public void applyToWorld(PhysicsWorld world, Body player, Vec3 platformCenter) {
        world.clearStatics();
        world.setGround(this.current != null ? this.current.groundY() : 0.0f, true);
        for (Collider.Box b : this.levelStatics) {
            world.addStatic(b);
        }
        if (this.levelStatics.isEmpty() && platformCenter != null) {
            world.addStatic(Collider.Box.centered(platformCenter, new Vec3(2.0f, 0.5f, 2.0f)));
        }
        if (player != null && this.current != null) {
            player.setPosition(this.current.spawn());
            player.setVelocity(Vec3.ZERO);
        }
    }

    static Collider.Box entityBox(LevelDef.Entity e, List<Mesh> parts) {
        AABB acc = null;
        for (Mesh m : parts) {
            acc = acc == null ? m.bounds() : acc.merge(m.bounds());
        }
        if (acc == null) {
            throw new IllegalArgumentException("empty model : " + e.model());
        }
        Vec3 center = acc.center().mul(e.scale()).add(e.pos());
        Vec3 size = acc.size().mul(e.scale());
        size = new Vec3(Math.max(0.1f, size.x()), Math.max(0.1f, size.y()), Math.max(0.1f, size.z()));
        return Collider.Box.centered(center, size);
    }
}
