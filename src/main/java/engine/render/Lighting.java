package engine.render;

import engine.math.Vec3;
import engine.render.Light;
import engine.render.Material;

public final class Lighting {
    private Lighting() {
    }

    public static Vec3 phong(Vec3 normal, Vec3 viewDir, Vec3 lightDir, Vec3 albedo, Material mat, Light light, float ambient) {
        Vec3 n = normal.normalize();
        Vec3 l = lightDir.normalize();
        Vec3 v = viewDir.normalize();
        float ndl = Math.max(0.0f, n.dot(l));
        float rA = albedo.x() * ambient;
        float gA = albedo.y() * ambient;
        float bA = albedo.z() * ambient;
        float diff = ndl * light.intensity();
        float rD = albedo.x() * light.color().x() * diff;
        float gD = albedo.y() * light.color().y() * diff;
        float bD = albedo.z() * light.color().z() * diff;
        float rS = 0.0f;
        float gS = 0.0f;
        float bS = 0.0f;
        if (ndl > 0.0f && mat.shininess() > 0.0f) {
            Vec3 h = l.add(v).normalize();
            float ndh = Math.max(0.0f, n.dot(h));
            float spec = (float)Math.pow(ndh, mat.shininess()) * light.intensity();
            Vec3 ks = mat.specular();
            rS = ks.x() * light.color().x() * spec;
            gS = ks.y() * light.color().y() * spec;
            bS = ks.z() * light.color().z() * spec;
        }
        return new Vec3(rA + rD + rS, gA + gD + gS, bA + bD + bS);
    }

    public static Vec3 fromRgb(int rgb) {
        return new Vec3((float)(rgb >> 16 & 0xFF) / 255.0f, (float)(rgb >> 8 & 0xFF) / 255.0f, (float)(rgb & 0xFF) / 255.0f);
    }

    public static int toRgb(Vec3 c) {
        int r = Math.max(0, Math.min(255, Math.round(c.x() * 255.0f)));
        int g = Math.max(0, Math.min(255, Math.round(c.y() * 255.0f)));
        int b = Math.max(0, Math.min(255, Math.round(c.z() * 255.0f)));
        return r << 16 | g << 8 | b;
    }
}
