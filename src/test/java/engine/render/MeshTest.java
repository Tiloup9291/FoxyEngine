package engine.render;

import engine.render.Material;
import engine.render.Mesh;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MeshTest {
    MeshTest() {
    }

    @Test
    void cubeHas8VerticesAnd12Triangles() {
        Mesh cube = Mesh.createCube((float)2.0f);
        Assertions.assertEquals((int)8, (int)cube.vertexCount());
        Assertions.assertEquals((int)12, (int)cube.triangleCount());
    }

    @Test
    void cubeBoundsMatchSize() {
        Mesh cube = Mesh.createCube((float)2.0f);
        Assertions.assertEquals((float)-1.0f, (float)cube.bounds().min().x(), (float)1.0E-5f);
        Assertions.assertEquals((float)1.0f, (float)cube.bounds().max().x(), (float)1.0E-5f);
        Assertions.assertEquals((float)-1.0f, (float)cube.bounds().min().y(), (float)1.0E-5f);
        Assertions.assertEquals((float)1.0f, (float)cube.bounds().max().z(), (float)1.0E-5f);
    }

    @Test
    void invalidMeshIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Mesh(new float[]{1.0f, 2.0f}, new int[]{0}));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Mesh(new float[]{0.0f, 0.0f, 0.0f}, new int[]{0, 1, 5}));
    }

    @Test
    void fullMeshCarriesNormalsUvsMaterial() {
        float[] p = new float[]{0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
        float[] n = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f};
        float[] t = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f};
        Mesh m = new Mesh(p, n, t, new int[]{0, 1, 2}, new Material("m", 0xFF0000, 1.0f));
        Assertions.assertTrue((boolean)m.hasNormals());
        Assertions.assertTrue((boolean)m.hasUvs());
        Assertions.assertEquals((int)0xFF0000, (int)m.material().diffuseRgb());
        Assertions.assertEquals((float)1.0f, (float)m.normal(0).z(), (float)1.0E-5f);
    }

    @Test
    void mismatchedArraysRejected() {
        float[] p = new float[]{0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Mesh(p, new float[]{0.0f, 0.0f, 1.0f}, null, new int[]{0, 1, 2}, null));
    }
}
