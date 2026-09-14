package engine.render;

import engine.math.AABB;
import engine.math.Vec3;
import engine.render.Material;

public final class Mesh {
    private final float[] positions;
    private final float[] normals;
    private final float[] uvs;
    private final int[] indices;
    private final Material material;
    private final AABB bounds;

    public Mesh(float[] positions, float[] normals, float[] uvs, int[] indices, Material material) {
        if (positions == null || positions.length % 3 != 0 || positions.length == 0) {
            throw new IllegalArgumentException("positions must store x,y,z triplets");
        }
        if (indices == null || indices.length % 3 != 0 || indices.length == 0) {
            throw new IllegalArgumentException("indices must hold triangles");
        }
        if (normals != null && normals.length != positions.length) {
            throw new IllegalArgumentException("normals must align with positions");
        }
        if (uvs != null && uvs.length != positions.length / 3 * 2) {
            throw new IllegalArgumentException("uvs must hold 1 pair per vertex");
        }
        int vCount = positions.length / 3;
        for (int i : indices) {
            if (i >= 0 && i < vCount) continue;
            throw new IllegalArgumentException("index out of bounds: " + i);
        }
        this.positions = (float[])positions.clone();
        this.normals = normals != null ? (float[])normals.clone() : null;
        this.uvs = uvs != null ? (float[])uvs.clone() : null;
        this.indices = (int[])indices.clone();
        this.material = material != null ? material : Material.DEFAULT;
        this.bounds = AABB.ofFloatArray(this.positions);
    }

    public Mesh(float[] positions, int[] indices) {
        this(positions, null, null, indices, Material.DEFAULT);
    }

    public static Mesh createCube(float size, Material material) {
        Mesh base = Mesh.createCube(size);
        return new Mesh(base.positions, base.normals, base.uvs, base.indices, material);
    }

    public static Mesh createCube(float size) {
        float h = size / 2.0f;
        float[] p = new float[]{-h, -h, -h, h, -h, -h, h, h, -h, -h, h, -h, -h, -h, h, h, -h, h, h, h, h, -h, h, h};
        int[] idx = new int[]{4, 5, 6, 4, 6, 7, 1, 0, 3, 1, 3, 2, 5, 1, 2, 5, 2, 6, 0, 4, 7, 0, 7, 3, 7, 6, 2, 7, 2, 3, 0, 1, 5, 0, 5, 4};
        return new Mesh(p, idx);
    }

    public int vertexCount() {
        return this.positions.length / 3;
    }

    public int triangleCount() {
        return this.indices.length / 3;
    }

    public boolean hasNormals() {
        return this.normals != null;
    }

    public boolean hasUvs() {
        return this.uvs != null;
    }

    public Vec3 vertex(int i) {
        return new Vec3(this.positions[i * 3], this.positions[i * 3 + 1], this.positions[i * 3 + 2]);
    }

    public Vec3 normal(int i) {
        if (this.normals == null) {
            throw new IllegalStateException("no normals");
        }
        return new Vec3(this.normals[i * 3], this.normals[i * 3 + 1], this.normals[i * 3 + 2]);
    }

    public float[] positions() {
        return (float[])this.positions.clone();
    }

    public float[] normals() {
        return this.normals != null ? (float[])this.normals.clone() : null;
    }

    public float[] uvs() {
        return this.uvs != null ? (float[])this.uvs.clone() : null;
    }

    public int[] indices() {
        return (int[])this.indices.clone();
    }

    public Material material() {
        return this.material;
    }

    public AABB bounds() {
        return this.bounds;
    }

    public float[] positionsRaw() {
        return this.positions;
    }

    public float[] normalsRaw() {
        return this.normals;
    }

    public float[] uvsRaw() {
        return this.uvs;
    }

    public int[] indicesRaw() {
        return this.indices;
    }
}
