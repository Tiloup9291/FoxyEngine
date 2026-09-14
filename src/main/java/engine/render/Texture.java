package engine.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public final class Texture {
    private final int width;
    private final int height;
    private final int[] pixels;

    public Texture(int width, int height, int[] argb) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("invalid size");
        }
        if (argb == null || argb.length != width * height) {
            throw new IllegalArgumentException("invalid pixels");
        }
        this.width = width;
        this.height = height;
        this.pixels = (int[])argb.clone();
    }

    public static Texture load(Path file) throws IOException {
        BufferedImage img = ImageIO.read(file.toFile());
        if (img == null) {
            throw new IOException("unsupported image format: " + file);
        }
        int w = img.getWidth();
        int h = img.getHeight();
        int[] px = new int[w * h];
        img.getRGB(0, 0, w, h, px, 0, w);
        return new Texture(w, h, px);
    }

    public static Texture checker(int cells, int cellSize, int colorA, int colorB) {
        int s = cells * cellSize;
        int[] px = new int[s * s];
        for (int y = 0; y < s; ++y) {
            for (int x = 0; x < s; ++x) {
                boolean even = (x / cellSize + y / cellSize) % 2 == 0;
                px[y * s + x] = even ? colorA : colorB;
            }
        }
        return new Texture(s, s, px);
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public int sample(float u, float v, Filter filter) {
        float uu = u - (float)Math.floor(u);
        float vv = v - (float)Math.floor(v);
        if (filter == Filter.BILINEAR) {
            return this.sampleBilinear(uu, vv);
        }
        return this.sampleNearest(uu, vv);
    }

    private int sampleNearest(float u, float v) {
        int x = Math.min(this.width - 1, (int)(u * (float)this.width));
        int y = Math.min(this.height - 1, (int)(v * (float)this.height));
        return this.pixels[y * this.width + x];
    }

    private int sampleBilinear(float u, float v) {
        float fx = u * (float)this.width - 0.5f;
        float fy = v * (float)this.height - 0.5f;
        int x0 = (int)Math.floor(fx);
        int y0 = (int)Math.floor(fy);
        float tx = fx - (float)x0;
        float ty = fy - (float)y0;
        int c00 = this.getWrapped(x0, y0);
        int c10 = this.getWrapped(x0 + 1, y0);
        int c01 = this.getWrapped(x0, y0 + 1);
        int c11 = this.getWrapped(x0 + 1, y0 + 1);
        return Texture.bilerp(c00, c10, c01, c11, tx, ty);
    }

    private int getWrapped(int x, int y) {
        int xx = (x % this.width + this.width) % this.width;
        int yy = (y % this.height + this.height) % this.height;
        return this.pixels[yy * this.width + xx];
    }

    static int bilerp(int c00, int c10, int c01, int c11, float tx, float ty) {
        float a = Texture.lerp(Texture.lerp(Texture.a(c00), Texture.a(c10), tx), Texture.lerp(Texture.a(c01), Texture.a(c11), tx), ty);
        float r = Texture.lerp(Texture.lerp(Texture.r(c00), Texture.r(c10), tx), Texture.lerp(Texture.r(c01), Texture.r(c11), tx), ty);
        float g = Texture.lerp(Texture.lerp(Texture.g(c00), Texture.g(c10), tx), Texture.lerp(Texture.g(c01), Texture.g(c11), tx), ty);
        float b = Texture.lerp(Texture.lerp(Texture.b(c00), Texture.b(c10), tx), Texture.lerp(Texture.b(c01), Texture.b(c11), tx), ty);
        return Texture.clamp8(a) << 24 | Texture.clamp8(r) << 16 | Texture.clamp8(g) << 8 | Texture.clamp8(b);
    }

    private static float lerp(float x, float y, float t) {
        return x + (y - x) * t;
    }

    private static float a(int c) {
        return c >>> 24 & 0xFF;
    }

    private static float r(int c) {
        return c >> 16 & 0xFF;
    }

    private static float g(int c) {
        return c >> 8 & 0xFF;
    }

    private static float b(int c) {
        return c & 0xFF;
    }

    private static int clamp8(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }

    public static enum Filter {
        NEAREST,
        BILINEAR;

    }
}
