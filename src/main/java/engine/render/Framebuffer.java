package engine.render;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public final class Framebuffer {
    private final int width;
    private final int height;
    private final BufferedImage image;
    private final int[] pixels;
    private final float[] depth;

    public Framebuffer(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("invalid size");
        }
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, 1);
        this.pixels = ((DataBufferInt)this.image.getRaster().getDataBuffer()).getData();
        this.depth = new float[width * height];
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public BufferedImage image() {
        return this.image;
    }

    public void clear(int rgb) {
        Arrays.fill(this.pixels, rgb & 0xFFFFFF);
        Arrays.fill(this.depth, Float.POSITIVE_INFINITY);
    }

    public boolean setPixel(int x, int y, float z, int rgb) {
        if ((x | y) < 0 || x >= this.width || y >= this.height) {
            return false;
        }
        int i = y * this.width + x;
        if (z < this.depth[i]) {
            this.depth[i] = z;
            this.pixels[i] = rgb & 0xFFFFFF;
            return true;
        }
        return false;
    }

    public int getPixel(int x, int y) {
        return this.pixels[y * this.width + x] & 0xFFFFFF;
    }

    public float getDepth(int x, int y) {
        return this.depth[y * this.width + x];
    }

    public int[] pixels() {
        return this.pixels;
    }

    public float[] depth() {
        return this.depth;
    }
}
