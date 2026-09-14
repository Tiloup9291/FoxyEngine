package engine.render;

import engine.math.Vec3;
import java.util.Objects;

public record Material(String name, int diffuseRgb, float opacity, Vec3 ambient, Vec3 specular, float shininess, String mapKd) {
    public static final Material DEFAULT = new Material("default", 5088255, 1.0f, new Vec3(0.2f, 0.2f, 0.2f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f, null);

    public Material(String name, int diffuseRgb, float opacity) {
        this(name, diffuseRgb, opacity, new Vec3(0.2f, 0.2f, 0.2f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f, null);
    }

    public Material {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(ambient, "ambient");
        Objects.requireNonNull(specular, "specular");
        diffuseRgb &= 0xFFFFFF;
        opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        if (shininess < 0.0f) {
            throw new IllegalArgumentException("shininess negative");
        }
    }

    public Vec3 albedo() {
        return new Vec3((float)(this.diffuseRgb >> 16 & 0xFF) / 255.0f, (float)(this.diffuseRgb >> 8 & 0xFF) / 255.0f, (float)(this.diffuseRgb & 0xFF) / 255.0f);
    }

    public boolean hasTexture() {
        return this.mapKd != null && !this.mapKd.isBlank();
    }

    public static Material withColor(String name, float r, float g, float b) {
        int rgb = Material.clamp8(r * 255.0f) << 16 | Material.clamp8(g * 255.0f) << 8 | Material.clamp8(b * 255.0f);
        return new Material(name, rgb, 1.0f);
    }

    private static int clamp8(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }
}
