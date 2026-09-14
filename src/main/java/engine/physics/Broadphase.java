package engine.physics;

import engine.math.AABB;
import engine.math.Vec3;
import engine.physics.Body;
import engine.physics.Collider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Broadphase {
    private final float cellSize;
    private final Map<Long, List<Integer>> cells = new HashMap<Long, List<Integer>>();

    public Broadphase(float cellSize) {
        if (cellSize <= 0.0f) {
            throw new IllegalArgumentException("cellSize <= 0");
        }
        this.cellSize = cellSize;
    }

    public void rebuild(List<Collider.Box> statics) {
        this.cells.clear();
        for (int i = 0; i < statics.size(); ++i) {
            AABB b = statics.get(i).aabb();
            int x0 = this.cellOf(b.min().x());
            int y0 = this.cellOf(b.min().y());
            int z0 = this.cellOf(b.min().z());
            int x1 = this.cellOf(b.max().x());
            int y1 = this.cellOf(b.max().y());
            int z1 = this.cellOf(b.max().z());
            for (int x = x0; x <= x1; ++x) {
                for (int y = y0; y <= y1; ++y) {
                    for (int z = z0; z <= z1; ++z) {
                        this.cells.computeIfAbsent(Broadphase.key(x, y, z), k -> new ArrayList()).add(i);
                    }
                }
            }
        }
    }

    public void query(AABB box, Set<Integer> out) {
        out.clear();
        int x0 = this.cellOf(box.min().x());
        int y0 = this.cellOf(box.min().y());
        int z0 = this.cellOf(box.min().z());
        int x1 = this.cellOf(box.max().x());
        int y1 = this.cellOf(box.max().y());
        int z1 = this.cellOf(box.max().z());
        HashSet<Integer> seen = new HashSet<Integer>();
        for (int x = x0; x <= x1; ++x) {
            for (int y = y0; y <= y1; ++y) {
                for (int z = z0; z <= z1; ++z) {
                    List<Integer> l = this.cells.get(Broadphase.key(x, y, z));
                    if (l == null) continue;
                    for (int i : l) {
                        if (!seen.add(i)) continue;
                        out.add(i);
                    }
                }
            }
        }
    }

    public int cellCount() {
        return this.cells.size();
    }

    private int cellOf(float v) {
        return (int)Math.floor(v / this.cellSize);
    }

    private static long key(int x, int y, int z) {
        return (long)(x + 512) << 20 | (long)(y + 512) << 10 | (long)(z + 512);
    }

    public static Vec3 aabbOf(Body b) {
        return b.position();
    }
}
