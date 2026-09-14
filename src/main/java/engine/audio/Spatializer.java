package engine.audio;

public final class Spatializer {
    private Spatializer() {
    }

    public static SpatialGain spatialize(float dx, float dz, float maxDist) {
        float d = (float)Math.sqrt(dx * dx + dz * dz);
        float gain = Math.max(0.0f, 1.0f - d / Math.max(1.0f, maxDist));
        float pan = Math.max(-1.0f, Math.min(1.0f, dx / Math.max(1.0f, maxDist * 0.5f)));
        return new SpatialGain(gain, pan);
    }

    public static float[] constantPower(float pan) {
        float p = Math.max(-1.0f, Math.min(1.0f, pan));
        float angle = (p + 1.0f) * (float)Math.PI / 4.0f;
        return new float[]{(float)Math.cos(angle), (float)Math.sin(angle)};
    }

    public record SpatialGain(float gain, float pan) {
    }
}
