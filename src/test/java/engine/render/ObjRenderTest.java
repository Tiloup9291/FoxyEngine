/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  engine.assets.ObjParser
 *  engine.math.Vec3
 *  engine.render.Camera
 *  engine.render.Framebuffer
 *  engine.render.Mesh
 *  engine.render.Renderer
 *  engine.render.Renderer$Instance
 *  org.junit.jupiter.api.Assertions
 *  org.junit.jupiter.api.Test
 */
package engine.render;

import engine.assets.ObjParser;
import engine.math.Vec3;
import engine.render.Camera;
import engine.render.Framebuffer;
import engine.render.Mesh;
import engine.render.Renderer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ObjRenderTest {
    ObjRenderTest() {
    }

    private static Renderer rendererFor(String model, Framebuffer fb) throws Exception {
        List<Mesh> parts = ObjParser.parseFile((Path)Path.of("assets", "models", model + ".obj"));
        ArrayList<Renderer.Instance> insts = new ArrayList<Renderer.Instance>();
        for (Mesh m : parts) {
            insts.add(new Renderer.Instance(m));
        }
        Renderer r = new Renderer(fb, insts, 70.0f, 0.1f, 100.0f, 65416, 5088255);
        r.setCamera(new Camera(new Vec3(0.0f, 0.8f, 4.2f), Vec3.ZERO, Vec3.UNIT_Y));
        return r;
    }

    @Test
    void objCubeRenders() throws Exception {
        Framebuffer fb = new Framebuffer(200, 150);
        Renderer r = ObjRenderTest.rendererFor("cube", fb);
        int tris = r.render(0.5f, 0);
        Assertions.assertTrue((tris > 0 ? 1 : 0) != 0);
        long painted = 0L;
        for (int px : fb.pixels()) {
            if ((px & 0xFFFFFF) == 0) continue;
            ++painted;
        }
        Assertions.assertTrue((painted > 500L ? 1 : 0) != 0, (String)"cube.obj must cover pixels");
    }

    @Test
    void texturedCubeUsesUv() throws Exception {
        List<Mesh> parts = ObjParser.parseFile((Path)Path.of("assets", "models", "cube.obj"));
        Assertions.assertTrue((boolean)parts.stream().anyMatch(Mesh::hasUvs), (String)"cube.obj must have UVs");
        Assertions.assertTrue((boolean)parts.stream().anyMatch(m -> m.material().hasTexture()), (String)"map_Kd expected");
    }

    @Test
    void objPyramidRenders() throws Exception {
        Framebuffer fb = new Framebuffer(200, 150);
        Renderer r = ObjRenderTest.rendererFor("pyramid", fb);
        Assertions.assertTrue((r.render(0.5f, 0) > 0 ? 1 : 0) != 0);
    }

    @Test
    void materialColorIsUsed() throws Exception {
        List<Mesh> parts = ObjParser.parseFile((Path)Path.of("assets", "models", "cube.obj"));
        Assertions.assertTrue((boolean)parts.stream().anyMatch(m -> m.material().diffuseRgb() != 0xFFFFFF));
    }
}
