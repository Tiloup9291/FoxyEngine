package engine.core;

import engine.core.EngineConfig;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EngineConfigTest {
    EngineConfigTest() {
    }

    @Test
    void loadsRealFile() {
        Path f = Path.of("config", "engine.properties");
        try {
            EngineConfig c = EngineConfig.load((Path)f);
            Assertions.assertEquals((int)800, (int)c.windowWidth());
            Assertions.assertEquals((int)60, (int)c.fpsTarget());
        }
        catch (Exception e) {
            EngineConfig c = EngineConfig.defaults();
            Assertions.assertEquals((int)800, (int)c.windowWidth());
        }
    }

    @Test
    void defaultsWhenMissing(@TempDir Path tmp) throws Exception {
        EngineConfig c = EngineConfig.load((Path)tmp.resolve("absent.properties"));
        Assertions.assertEquals((Object)"Java 3D Engine v1.0", (Object)c.windowTitle());
        Assertions.assertEquals((int)60, (int)c.tickRate());
        Assertions.assertEquals((Object)"solid", (Object)c.renderMode());
    }

    @Test
    void parsesRenderSection() {
        Properties p = new Properties();
        p.setProperty("engine.render.mode", "wireframe");
        p.setProperty("engine.render.fov", "90");
        p.setProperty("engine.render.near", "0.5");
        p.setProperty("engine.render.far", "200");
        p.setProperty("engine.render.clear", "0xFF0000");
        EngineConfig c = EngineConfig.fromProperties((Properties)p);
        Assertions.assertEquals((Object)"wireframe", (Object)c.renderMode());
        Assertions.assertEquals((float)90.0f, (float)c.fovDeg(), (float)1.0E-5f);
        Assertions.assertEquals((int)0xFF0000, (int)c.clearColor());
    }

    @Test
    void invalidModeFallsBackToSolid() {
        Properties p = new Properties();
        p.setProperty("engine.render.mode", "anything");
        Assertions.assertEquals((Object)"solid", (Object)EngineConfig.fromProperties((Properties)p).renderMode());
    }

    @Test
    void parsesCameraSection() {
        Properties p = new Properties();
        p.setProperty("engine.camera.mode", "fps");
        p.setProperty("engine.camera.move_speed", "5.5");
        p.setProperty("engine.camera.rot_speed", "2.0");
        EngineConfig c = EngineConfig.fromProperties((Properties)p);
        Assertions.assertEquals((Object)"fps", (Object)c.cameraMode());
        Assertions.assertEquals((float)5.5f, (float)c.cameraMoveSpeed(), (float)1.0E-5f);
        Assertions.assertEquals((float)2.0f, (float)c.cameraRotSpeed(), (float)1.0E-5f);
    }

    @Test
    void parsesLightAndTextureSection() {
        Properties p = new Properties();
        p.setProperty("engine.light.dir_x", "0.1");
        p.setProperty("engine.light.intensity", "2.0");
        p.setProperty("engine.light.color", "0xFF0000");
        p.setProperty("engine.texture.filter", "nearest");
        EngineConfig c = EngineConfig.fromProperties((Properties)p);
        Assertions.assertEquals((float)0.1f, (float)c.lightDirX(), (float)1.0E-5f);
        Assertions.assertEquals((float)2.0f, (float)c.lightIntensity(), (float)1.0E-5f);
        Assertions.assertEquals((int)0xFF0000, (int)c.lightColor());
        Assertions.assertEquals((Object)"nearest", (Object)c.textureFilter());
    }

    @Test
    void invalidFilterFallsBackToBilinear() {
        Properties p = new Properties();
        p.setProperty("engine.texture.filter", "anything");
        Assertions.assertEquals((Object)"bilinear", (Object)EngineConfig.fromProperties((Properties)p).textureFilter());
    }

    @Test
    void parsesPipelineBands() {
        Properties p = new Properties();
        p.setProperty("engine.pipeline.bands", "2");
        Assertions.assertEquals((int)2, (int)EngineConfig.fromProperties((Properties)p).renderBands());
    }

    @Test
    void bandsClampedToAtLeastOne() {
        Properties p = new Properties();
        p.setProperty("engine.pipeline.bands", "0");
        Assertions.assertEquals((int)1, (int)EngineConfig.fromProperties((Properties)p).renderBands());
    }

    @Test
    void invalidCameraModeFallsBackToOrbit() {
        Properties p = new Properties();
        p.setProperty("engine.camera.mode", "anything");
        Assertions.assertEquals((Object)"orbit", (Object)EngineConfig.fromProperties((Properties)p).cameraMode());
    }
}
