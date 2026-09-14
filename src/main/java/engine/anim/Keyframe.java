package engine.anim;

public record Keyframe(float time, float value) {
    public Keyframe {
        if (time < 0.0f) {
            throw new IllegalArgumentException("negative time");
        }
    }
}
