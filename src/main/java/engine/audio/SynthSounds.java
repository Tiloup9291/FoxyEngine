package engine.audio;

import engine.audio.AudioClip;
import java.util.Random;

public final class SynthSounds {
    public static final int RATE = 22050;

    private SynthSounds() {
    }

    public static AudioClip get(String id) {
        return switch (id) {
            case "jump" -> SynthSounds.jump();
            case "step" -> SynthSounds.step();
            case "land" -> SynthSounds.land();
            case "click" -> SynthSounds.click();
            default -> null;
        };
    }

    public static AudioClip jump() {
        int n = 3969;
        float[] s = new float[n];
        double phase = 0.0;
        for (int i = 0; i < n; ++i) {
            double u = (double)i / (double)n;
            double f = 300.0 + 400.0 * u;
            float env = (float)Math.sin(Math.PI * u);
            s[i] = (float)(Math.sin(phase += Math.PI * 2 * f / 22050.0) * 0.6 * (double)env);
        }
        return new AudioClip(22050, 1, s);
    }

    public static AudioClip step() {
        int i;
        int n = 1543;
        float[] s = new float[n];
        Random rng = new Random(1234L);
        for (i = 0; i < n; ++i) {
            double u = (double)i / (double)n;
            float env = (float)Math.exp(-u * 8.0);
            s[i] = (rng.nextFloat() * 2.0f - 1.0f) * 0.35f * env;
        }
        for (i = 1; i < n; ++i) {
            s[i] = (s[i] + s[i - 1]) * 0.5f;
        }
        return new AudioClip(22050, 1, s);
    }

    public static AudioClip land() {
        int n = 4851;
        float[] s = new float[n];
        Random rng = new Random(777L);
        for (int i = 0; i < n; ++i) {
            double u = (double)i / (double)n;
            float env = (float)Math.exp(-u * 6.0);
            float tone = (float)Math.sin(565.4866776461628 * (double)i / 22050.0) * 0.6f;
            float noise = (rng.nextFloat() * 2.0f - 1.0f) * 0.15f;
            s[i] = (tone + noise) * env;
        }
        return new AudioClip(22050, 1, s);
    }

    public static AudioClip click() {
        int n = 1102;
        float[] s = new float[n];
        for (int i = 0; i < n; ++i) {
            double u = (double)i / (double)n;
            s[i] = (float)(Math.sin(7539.822368615503 * (double)i / 22050.0) * 0.4 * Math.exp(-u * 10.0));
        }
        return new AudioClip(22050, 1, s);
    }
}
