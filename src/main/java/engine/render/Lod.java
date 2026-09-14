package engine.render;

import engine.render.Mesh;

public final class Lod {
    private Lod() {
    }

    public static int level(float dist, float near, float far) {
        if (dist < near) {
            return 0;
        }
        if (dist < far) {
            return 1;
        }
        return 2;
    }

    public static Mesh decimate(Mesh src, int stride) {
        if (stride <= 1) {
            return src;
        }
        int[] idx = src.indicesRaw();
        int kept = idx.length / 3 / stride * 3;
        if (kept < 3) {
            return src;
        }
        int[] out = new int[kept];
        int o = 0;
        int t = 0;
        while (t + 2 < idx.length && o < kept) {
            out[o++] = idx[t];
            out[o++] = idx[t + 1];
            out[o++] = idx[t + 2];
            t += 3 * stride;
        }
        return new Mesh(src.positionsRaw(), src.normalsRaw(), src.uvsRaw(), out, src.material());
    }
}
