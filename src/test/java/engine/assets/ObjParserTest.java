package engine.assets;

import engine.assets.ObjParseException;
import engine.assets.ObjParser;
import engine.render.Mesh;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ObjParserTest {
    private static final String QUAD = "v 0 0 0\nv 1 0 0\nv 1 1 0\nv 0 1 0\nf 1 2 3 4\n";

    ObjParserTest() {
    }

    @Test
    void quadTriangulatesToTwo() throws Exception {
        List<Mesh> parts = ObjParser.parseText((String)QUAD, null, null);
        Assertions.assertEquals((int)1, (int)parts.size());
        Assertions.assertEquals((int)2, (int)((Mesh)parts.get(0)).triangleCount());
        Assertions.assertTrue((boolean)((Mesh)parts.get(0)).hasNormals(), (String)"recomputed normals");
    }

    @Test
    void allFaceFormats() throws Exception {
        String obj = "v 0 0 0\nv 1 0 0\nv 0 1 0\nvt 0 0\nvt 1 0\nvt 0 1\nvn 0 0 1\nf 1 2 3\nf 1/1 2/2 3/3\nf 1//1 2//1 3//1\nf 1/1/1 2/2/1 3/3/1\n";
        List<Mesh> parts = ObjParser.parseText((String)obj, null, null);
        int tris = parts.stream().mapToInt(m -> m.triangleCount()).sum();
        Assertions.assertEquals((int)4, (int)tris);
    }

    @Test
    void negativeIndices() throws Exception {
        String obj = "v 0 0 0\nv 1 0 0\nv 0 1 0\nf -3 -2 -1\n";
        List<Mesh> parts = ObjParser.parseText((String)obj, null, null);
        Assertions.assertEquals((int)1, (int)((Mesh)parts.get(0)).triangleCount());
    }

    @Test
    void commentsAndBlankLines() throws Exception {
        String obj = "# cube\n\nv 0 0 0\n\nv 1 0 0\nv 0 1 0\n# face\nf 1 2 3\n";
        Assertions.assertEquals((int)1, (int)((Mesh)ObjParser.parseText((String)obj, null, null).get(0)).triangleCount());
    }

    @Test
    void explicitNormalsKept() throws Exception {
        String obj = "v 0 0 0\nv 1 0 0\nv 0 1 0\nvn 0 0 1\nf 1//1 2//1 3//1\n";
        Mesh m = (Mesh)ObjParser.parseText((String)obj, null, null).get(0);
        Assertions.assertTrue((boolean)m.hasNormals());
        Assertions.assertEquals((float)0.0f, (float)m.normal(0).x(), (float)1.0E-5f);
        Assertions.assertEquals((float)0.0f, (float)m.normal(0).y(), (float)1.0E-5f);
        Assertions.assertEquals((float)1.0f, (float)m.normal(0).z(), (float)1.0E-5f);
    }

    @Test
    void errorsReportFileAndLine() {
        ObjParseException e = (ObjParseException)Assertions.assertThrows(ObjParseException.class, () -> ObjParser.parseText((String)"v 0 0\n", null, null));
        Assertions.assertTrue((boolean)e.getMessage().contains(":1:"));
    }

    @Test
    void emptyModelRejected() {
        Assertions.assertThrows(ObjParseException.class, () -> ObjParser.parseText((String)"# empty\n", null, null));
    }

    @Test
    void demoAssetsLoad() throws Exception {
        List<Mesh> cube = ObjParser.parseFile((Path)Path.of("assets", "models", "cube.obj"));
        int tris = cube.stream().mapToInt(m -> m.triangleCount()).sum();
        Assertions.assertEquals((int)12, (int)tris, (String)"cube.obj: 6 quads -> 12 triangles");
        List<Mesh> pyramid = ObjParser.parseFile((Path)Path.of("assets", "models", "pyramid.obj"));
        int trisP = pyramid.stream().mapToInt(m -> m.triangleCount()).sum();
        Assertions.assertEquals((int)6, (int)trisP, (String)"pyramid.obj: 1 quad + 4 tris -> 6 triangles");
        Assertions.assertTrue((boolean)pyramid.stream().anyMatch(m -> m.hasUvs()));
    }
}
