package engine.level;

import engine.level.LevelDef;
import engine.level.LevelParseException;
import engine.math.Vec3;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class LevelParser {
    private LevelParser() {
    }

    public static LevelDef parse(Path file) throws IOException, LevelParseException {
        List<String> lines = Files.readAllLines(file);
        String name = file.getFileName().toString().replaceFirst("\\.[^.]*$", "");
        Vec3 spawn = new Vec3(0.0f, 2.0f, 2.0f);
        float ground = 0.0f;
        ArrayList<LevelDef.Entity> entities = new ArrayList<LevelDef.Entity>();
        ArrayList<LevelDef.StaticBox> statics = new ArrayList<LevelDef.StaticBox>();
        int ln = 0;
        for (String raw : lines) {
            ++ln;
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int eq = line.indexOf(61);
            if (eq < 0) {
                throw new LevelParseException(file.toString(), ln, "expected key=value");
            }
            String key = line.substring(0, eq).trim().toLowerCase();
            String val = line.substring(eq + 1).trim();
            try {
                switch (key) {
                    case "name": {
                        if (val.isBlank()) break;
                        name = val;
                        break;
                    }
                    case "spawn": {
                        spawn = LevelParser.vec3(val);
                        break;
                    }
                    case "ground": {
                        ground = Float.parseFloat(val);
                        break;
                    }
                    case "entity": {
                        entities.add(LevelParser.entity(val));
                        break;
                    }
                    case "static": {
                        statics.add(LevelParser.staticBox(val));
                        break;
                    }
                }
            }
            catch (IllegalArgumentException e) {
                throw new LevelParseException(file.toString(), ln, e.getMessage());
            }
        }
        return new LevelDef(name, spawn, ground, entities, statics);
    }

    private static Vec3 vec3(String v) {
        float[] f = LevelParser.floats(v, 3, "x,y,z");
        return new Vec3(f[0], f[1], f[2]);
    }

    private static LevelDef.Entity entity(String v) {
        String[] parts = v.split(",");
        if (parts.length < 6 || parts.length > 7) {
            throw new IllegalArgumentException("entity=model,x,y,z,yawDeg,scale[,collide]");
        }
        String model = parts[0].trim();
        float x = Float.parseFloat(parts[1].trim());
        float y = Float.parseFloat(parts[2].trim());
        float z = Float.parseFloat(parts[3].trim());
        float yaw = Float.parseFloat(parts[4].trim());
        float scale = Float.parseFloat(parts[5].trim());
        boolean collide = parts.length == 7 && parts[6].trim().equalsIgnoreCase("collide");
        return new LevelDef.Entity(model, new Vec3(x, y, z), yaw, scale, collide);
    }

    private static LevelDef.StaticBox staticBox(String v) {
        float[] f = LevelParser.floats(v, 6, "cx,cy,cz,sx,sy,sz");
        return new LevelDef.StaticBox(new Vec3(f[0], f[1], f[2]), new Vec3(f[3], f[4], f[5]));
    }

    private static float[] floats(String v, int n, String attendu) {
        String[] parts = v.split(",");
        if (parts.length != n) {
            throw new IllegalArgumentException("expected " + attendu);
        }
        float[] f = new float[n];
        for (int i = 0; i < n; ++i) {
            f[i] = Float.parseFloat(parts[i].trim());
        }
        return f;
    }
}
