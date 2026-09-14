package engine.pipeline;

import engine.render.Camera;
import engine.render.Light;
import engine.render.Renderer;
import engine.render.Texture;
import java.util.List;

public record SceneSnapshot(List<Renderer.Instance> instances, Camera camera, float spinAngle, Light light, float ambient, Texture.Filter filter, Renderer.Mode mode, int clearColor, int wireColor, float fovYRad, float near, float far, int fbWidth, int fbHeight) {
    public SceneSnapshot {
        instances = List.copyOf(instances);
    }
}
