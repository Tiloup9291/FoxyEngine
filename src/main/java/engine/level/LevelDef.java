package engine.level;

import engine.math.Vec3;
import java.util.List;

public record LevelDef(String name, Vec3 spawn, float groundY, List<Entity> entities, List<StaticBox> statics) {
    public LevelDef {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("empty name");
        }
        if (spawn == null) {
            throw new IllegalArgumentException("null spawn");
        }
        entities = List.copyOf(entities);
        statics = List.copyOf(statics);
    }

    public record StaticBox(Vec3 center, Vec3 size) {
        public StaticBox {
            if (center == null || size == null) {
                throw new IllegalArgumentException("nulls");
            }
            if (size.x() <= 0.0f || size.y() <= 0.0f || size.z() <= 0.0f) {
                throw new IllegalArgumentException("size <= 0");
            }
        }
    }

    public record Entity(String model, Vec3 pos, float yawDeg, float scale, boolean collidable) {
        public Entity {
            if (model == null || model.isBlank()) {
                throw new IllegalArgumentException("empty model");
            }
            if (pos == null) {
                throw new IllegalArgumentException("null pos");
            }
            if (scale <= 0.0f) {
                throw new IllegalArgumentException("scale <= 0");
            }
        }
    }
}
