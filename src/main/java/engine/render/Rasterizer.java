package engine.render;

import engine.render.Framebuffer;

public final class Rasterizer {
    private Rasterizer() {
    }

    public static void drawLine(Framebuffer fb, int x0, int y0, float z0, int x1, int y1, float z1, int rgb) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int steps = Math.max(dx, dy) + 1;
        int x = x0;
        int y = y0;
        for (int i = 0; i <= steps; ++i) {
            float t = steps <= 1 ? 0.0f : (float)i / (float)(steps - 1);
            fb.setPixel(x, y, z0 + (z1 - z0) * t, rgb);
            if (x == x1 && y == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 >= dx) continue;
            err += dx;
            y += sy;
        }
    }

    public static void drawTriangleWireframe(Framebuffer fb, int x0, int y0, float z0, int x1, int y1, float z1, int x2, int y2, float z2, int rgb) {
        Rasterizer.drawLine(fb, x0, y0, z0, x1, y1, z1, rgb);
        Rasterizer.drawLine(fb, x1, y1, z1, x2, y2, z2, rgb);
        Rasterizer.drawLine(fb, x2, y2, z2, x0, y0, z0, rgb);
    }

    public static void drawTriangleFilled(Framebuffer fb, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, int rgb) {
        int yEnd;
        int yStart;
        float t;
        if (y1 < y0) {
            t = x0;
            x0 = x1;
            x1 = t;
            t = y0;
            y0 = y1;
            y1 = t;
            t = z0;
            z0 = z1;
            z1 = t;
        }
        if (y2 < y0) {
            t = x0;
            x0 = x2;
            x2 = t;
            t = y0;
            y0 = y2;
            y2 = t;
            t = z0;
            z0 = z2;
            z2 = t;
        }
        if (y2 < y1) {
            t = x1;
            x1 = x2;
            x2 = t;
            t = y1;
            y1 = y2;
            y2 = t;
            t = z1;
            z1 = z2;
            z2 = t;
        }
        if ((yStart = Math.max(0, (int)Math.ceil(y0 - 0.5f))) > (yEnd = Math.min(fb.height() - 1, (int)Math.floor(y2 - 0.5f)))) {
            return;
        }
        for (int y = yStart; y <= yEnd; ++y) {
            float zb;
            float xb;
            float za;
            float xa;
            float yc = (float)y + 0.5f;
            float[] xs = new float[3];
            float[] zs = new float[3];
            int n = 0;
            n = Rasterizer.intersectEdge(x0, y0, z0, x1, y1, z1, yc, xs, zs, n);
            n = Rasterizer.intersectEdge(x1, y1, z1, x2, y2, z2, yc, xs, zs, n);
            if ((n = Rasterizer.intersectEdge(x2, y2, z2, x0, y0, z0, yc, xs, zs, n)) < 2) continue;
            if (n == 2) {
                xa = xs[0];
                za = zs[0];
                xb = xs[1];
                zb = zs[1];
            } else {
                int iMin = 0;
                int iMax = 0;
                for (int i = 1; i < 3; ++i) {
                    if (xs[i] < xs[iMin]) {
                        iMin = i;
                    }
                    if (!(xs[i] > xs[iMax])) continue;
                    iMax = i;
                }
                xa = xs[iMin];
                za = zs[iMin];
                xb = xs[iMax];
                zb = zs[iMax];
            }
            if (xa > xb) {
                float t2 = xa;
                xa = xb;
                xb = t2;
                t2 = za;
                za = zb;
                zb = t2;
            }
            int xStart = Math.max(0, (int)Math.ceil(xa - 0.5f));
            int xEnd = Math.min(fb.width() - 1, (int)Math.floor(xb - 0.5f));
            float span = xb - xa;
            for (int x = xStart; x <= xEnd; ++x) {
                float xc = (float)x + 0.5f;
                float t3 = span < 1.0E-6f ? 0.0f : (xc - xa) / span;
                float z = za + (zb - za) * t3;
                fb.setPixel(x, y, z, rgb);
            }
        }
    }

    private static int intersectEdge(float x0, float y0, float z0, float x1, float y1, float z1, float yc, float[] xs, float[] zs, int n) {
        if (yc < Math.min(y0, y1) || yc > Math.max(y0, y1) || Math.abs(y1 - y0) < 1.0E-9f) {
            return n;
        }
        float t = (yc - y0) / (y1 - y0);
        xs[n] = x0 + (x1 - x0) * t;
        zs[n] = z0 + (z1 - z0) * t;
        return n + 1;
    }

    public static int rgb(int r, int g, int b) {
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    public static int shade(int baseRgb, float ndl) {
        float k = 0.25f + 0.75f * Math.max(0.0f, Math.min(1.0f, ndl));
        int r = (int)((float)(baseRgb >> 16 & 0xFF) * k);
        int g = (int)((float)(baseRgb >> 8 & 0xFF) * k);
        int b = (int)((float)(baseRgb & 0xFF) * k);
        return Rasterizer.rgb(r, g, b);
    }
}
