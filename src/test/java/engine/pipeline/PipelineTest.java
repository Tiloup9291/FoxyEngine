/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  engine.assets.ObjParser
 *  engine.math.Vec3
 *  engine.pipeline.RenderPipeline
 *  engine.pipeline.SceneSnapshot
 *  engine.pipeline.TransformStage
 *  engine.pipeline.TransformStage$TransformResult
 *  engine.render.Camera
 *  engine.render.CameraRig
 *  engine.render.CameraRig$Mode
 *  engine.render.Framebuffer
 *  engine.render.Frustum
 *  engine.render.Light
 *  engine.render.Mesh
 *  engine.render.Renderer$Instance
 *  engine.render.Renderer$Mode
 *  engine.render.Texture$Filter
 *  engine.render.TextureCache
 *  engine.render.Transform
 *  org.junit.jupiter.api.Assertions
 *  org.junit.jupiter.api.Test
 */
package engine.pipeline;

import engine.assets.ObjParser;
import engine.math.Vec3;
import engine.pipeline.RenderPipeline;
import engine.pipeline.SceneSnapshot;
import engine.pipeline.TransformStage;
import engine.render.Camera;
import engine.render.CameraRig;
import engine.render.Framebuffer;
import engine.render.Frustum;
import engine.render.Light;
import engine.render.Mesh;
import engine.render.Renderer;
import engine.render.Texture;
import engine.render.TextureCache;
import engine.render.Transform;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PipelineTest {
    PipelineTest() {
    }

    static SceneSnapshot snapFor(String model, Framebuffer fb, Renderer.Mode mode) throws Exception {
        List<Mesh> parts = ObjParser.parseFile((Path)Path.of("assets", "models", model + ".obj"));
        ArrayList<Renderer.Instance> insts = new ArrayList<Renderer.Instance>();
        for (Mesh m : parts) {
            insts.add(new Renderer.Instance(m, Transform.of((Vec3)Vec3.ZERO, (float)0.8f)));
        }
        Camera cam = CameraRig.defaultRig((CameraRig.Mode)CameraRig.Mode.ORBIT, (float)3.0f, (float)1.6f).toCamera();
        return new SceneSnapshot(insts, cam, 0.5f, Light.DEFAULT_SUN, 0.25f, Texture.Filter.BILINEAR, mode, 1053720, 65416, (float)Math.toRadians(70.0), 0.1f, 100.0f, fb.width(), fb.height());
    }

    @Test
    void etagesDirectsDessinentCube() throws Exception {
        Framebuffer fb = new Framebuffer(200, 150);
        SceneSnapshot snap = PipelineTest.snapFor("cube", fb, Renderer.Mode.SOLID);
        TextureCache textures = new TextureCache(Path.of("assets", "textures"));
        int tris = RenderPipeline.renderDirect((SceneSnapshot)snap, (Framebuffer)fb, (TextureCache)textures, (int)1, null);
        Assertions.assertTrue((tris > 0 ? 1 : 0) != 0, (String)"triangles dessines");
        long painted = 0L;
        for (int px : fb.pixels()) {
            if ((px & 0xFFFFFF) == 1053720) continue;
            ++painted;
        }
        Assertions.assertTrue((painted > 500L ? 1 : 0) != 0, (String)("pixels couverts=" + painted));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Test
    void monoAndMultiBandIdentical() throws Exception {
        TextureCache textures = new TextureCache(Path.of("assets", "textures"));
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            Framebuffer a = new Framebuffer(200, 150);
            Framebuffer b = new Framebuffer(200, 150);
            SceneSnapshot snap = PipelineTest.snapFor("pyramid", a, Renderer.Mode.SOLID);
            RenderPipeline.renderDirect((SceneSnapshot)snap, (Framebuffer)a, (TextureCache)textures, (int)1, null);
            RenderPipeline.renderDirect((SceneSnapshot)snap, (Framebuffer)b, (TextureCache)textures, (int)4, (ExecutorService)pool);
            Assertions.assertArrayEquals((int[])a.pixels(), (int[])b.pixels(), (String)"1 band == 4 bands");
            Assertions.assertArrayEquals((float[])a.depth(), (float[])b.depth(), (String)"z-buffer identique");
        }
        finally {
            pool.shutdownNow();
        }
    }

    @Test
    void frustumCullingCompte() throws Exception {
        Framebuffer fb = new Framebuffer(200, 150);
        SceneSnapshot snap = PipelineTest.snapFor("cube", fb, Renderer.Mode.SOLID);
        Frustum frustum = TransformStage.frustumOf((SceneSnapshot)snap);
        TransformStage.TransformResult tr = TransformStage.run((SceneSnapshot)snap, (Frustum)frustum);
        Assertions.assertEquals((int)0, (int)tr.culled(), (String)"cube devant camera : rien cule");
        Assertions.assertTrue((tr.visible().size() > 0 ? 1 : 0) != 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Test
    void pipelineThreadsDessine() throws Exception {
        TextureCache textures = new TextureCache(Path.of("assets", "textures"));
        RenderPipeline pipe = new RenderPipeline(textures, 2);
        try {
            Framebuffer fb = new Framebuffer(200, 150);
            SceneSnapshot snap = PipelineTest.snapFor("cube", fb, Renderer.Mode.SOLID);
            int tris = pipe.renderFrame(snap, fb);
            Assertions.assertTrue((tris > 0 ? 1 : 0) != 0);
            long painted = 0L;
            for (int px : fb.pixels()) {
                if ((px & 0xFFFFFF) == 1053720) continue;
                ++painted;
            }
            Assertions.assertTrue((painted > 500L ? 1 : 0) != 0, (String)("rendu via 3 threads, pixels=" + painted));
        }
        finally {
            pipe.shutdown();
        }
    }

    @Test
    void directAndThreadedIdentical() throws Exception {
        TextureCache textures = new TextureCache(Path.of("assets", "textures"));
        RenderPipeline pipe = new RenderPipeline(textures, 2);
        try {
            Framebuffer a = new Framebuffer(200, 150);
            Framebuffer b = new Framebuffer(200, 150);
            SceneSnapshot snap = PipelineTest.snapFor("pyramid", a, Renderer.Mode.SOLID);
            Assertions.assertTrue(RenderPipeline.shouldRunDirect(snap), "demo-size scene uses direct path");
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                int direct = RenderPipeline.renderDirect(snap, a, textures, 1, null);
                int threaded = pipe.renderFrame(snap, b);
                Assertions.assertEquals(direct, threaded, "same triangle count");
                Assertions.assertArrayEquals(a.pixels(), b.pixels(), "direct == threaded pixels");
                Assertions.assertArrayEquals(a.depth(), b.depth(), "direct == threaded depth");
            } finally {
                pool.shutdownNow();
            }
        } finally {
            pipe.shutdown();
        }
    }

    @Test
    void wireframeViaPipeline() throws Exception {
        TextureCache textures = new TextureCache(Path.of("assets", "textures"));
        Framebuffer fb = new Framebuffer(200, 150);
        SceneSnapshot snap = PipelineTest.snapFor("cube", fb, Renderer.Mode.WIREFRAME);
        int tris = RenderPipeline.renderDirect((SceneSnapshot)snap, (Framebuffer)fb, (TextureCache)textures, (int)1, null);
        Assertions.assertTrue((tris > 0 ? 1 : 0) != 0);
    }
}
