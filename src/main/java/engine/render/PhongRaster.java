package engine.render;

import engine.math.Vec3;
import engine.render.Framebuffer;
import engine.render.Light;
import engine.render.Lighting;
import engine.render.Material;
import engine.render.Texture;

public final class PhongRaster {
    private static volatile Vec3 camPos = new Vec3(0.0f, 0.0f, 10.0f);
    public static final int ATTR_COUNT = 8;

    private PhongRaster() {
    }

    public static void setCamera(Vec3 pos) {
        camPos = pos;
    }

    static void sortByY(float[] xs, float[] ys, float[] ws, float[] zs, float[][] as) {
        for (int i = 0; i < 2; ++i) {
            for (int j = i + 1; j < 3; ++j) {
                if (!(ys[j] < ys[i])) continue;
                float t = ys[i];
                ys[i] = ys[j];
                ys[j] = t;
                t = xs[i];
                xs[i] = xs[j];
                xs[j] = t;
                t = ws[i];
                ws[i] = ws[j];
                ws[j] = t;
                t = zs[i];
                zs[i] = zs[j];
                zs[j] = t;
                float[] ta = as[i];
                as[i] = as[j];
                as[j] = ta;
            }
        }
    }

    static float lerpA(float[][] aa, int i0, int i1, int k, float t) {
        return aa[i0][k] + (aa[i1][k] - aa[i0][k]) * t;
    }

    static float ambientOf(Material mat, float ambient) {
        Vec3 ka = mat.ambient();
        return ambient * (ka.x() + ka.y() + ka.z()) / 3.0f;
    }

    static int edge(float x0, float y0, float z0, float iw0, float[] aw0, float x1, float y1, float z1, float iw1, float[] aw1, float yc, float[] xa, float[] za, float[] wa, float[][] aa, int n, int cnt) {
        if (yc < Math.min(y0, y1) || yc > Math.max(y0, y1) || Math.abs(y1 - y0) < 1.0E-9f) {
            return cnt;
        }
        float t = (yc - y0) / (y1 - y0);
        xa[cnt] = x0 + (x1 - x0) * t;
        za[cnt] = z0 + (z1 - z0) * t;
        wa[cnt] = iw0 + (iw1 - iw0) * t;
        for (int k = 0; k < n; ++k) {
            aa[cnt][k] = aw0[k] + (aw1[k] - aw0[k]) * t;
        }
        return cnt + 1;
    }

    public static void drawTriangle(Framebuffer fb, float sx0, float sy0, float w0, float zw0, float[] attr0, float sx1, float sy1, float w1, float zw1, float[] attr1, float sx2, float sy2, float w2, float zw2, float[] attr2, Material mat, Texture tex, Texture.Filter filter, Light light, float ambient, float camX, float camY, float camZ) {
        int yEnd;
        int n = attr0.length;
        float[] xs = new float[]{sx0, sx1, sx2};
        float[] ys = new float[]{sy0, sy1, sy2};
        float[] ws = new float[]{w0, w1, w2};
        float[] zs = new float[]{zw0, zw1, zw2};
        float[][] as = new float[][]{attr0, attr1, attr2};
        PhongRaster.sortByY(xs, ys, ws, zs, as);
        float[] iw = new float[3];
        float[][] aw = new float[3][n];
        for (int i = 0; i < 3; ++i) {
            iw[i] = 1.0f / Math.max(1.0E-6f, ws[i]);
            for (int k = 0; k < n; ++k) {
                aw[i][k] = as[i][k] * iw[i];
            }
        }
        int yStart = Math.max(0, (int)Math.ceil(ys[0] - 0.5f));
        if (yStart > (yEnd = Math.min(fb.height() - 1, (int)Math.floor(ys[2] - 0.5f)))) {
            return;
        }
        float[] xa = new float[3];
        float[] za = new float[3];
        float[] wa = new float[3];
        float[][] aa = new float[3][n];
        boolean textured = tex != null && n > 7;
        Vec3 cam = camPos;
        float camPx = cam.x();
        float camPy = cam.y();
        float camPz = cam.z();
        Vec3 ld = light.dir();
        float llen = (float)Math.sqrt(ld.x() * ld.x() + ld.y() * ld.y() + ld.z() * ld.z());
        float lx = 0.0f;
        float ly = 1.0f;
        float lz = 0.0f;
        if (llen >= 1.0E-9f) {
            lx = ld.x() / llen;
            ly = ld.y() / llen;
            lz = ld.z() / llen;
        }
        Vec3 lc = light.color();
        float lcR = lc.x();
        float lcG = lc.y();
        float lcB = lc.z();
        float inten = light.intensity();
        Vec3 ks = mat.specular();
        float ksR = ks.x();
        float ksG = ks.y();
        float ksB = ks.z();
        float shine = mat.shininess();
        boolean useSpec = shine > 0.0f && (ksR != 0.0f || ksG != 0.0f || ksB != 0.0f);
        float amb = PhongRaster.ambientOf(mat, ambient);
        float albR = (float)(mat.diffuseRgb() >> 16 & 0xFF) / 255.0f;
        float albG = (float)(mat.diffuseRgb() >> 8 & 0xFF) / 255.0f;
        float albB = (float)(mat.diffuseRgb() & 0xFF) / 255.0f;
        for (int y = yStart; y <= yEnd; ++y) {
            float yc = (float)y + 0.5f;
            int cnt = 0;
            cnt = PhongRaster.edge(xs[0], ys[0], zs[0], iw[0], aw[0], xs[1], ys[1], zs[1], iw[1], aw[1], yc, xa, za, wa, aa, n, cnt);
            cnt = PhongRaster.edge(xs[1], ys[1], zs[1], iw[1], aw[1], xs[2], ys[2], zs[2], iw[2], aw[2], yc, xa, za, wa, aa, n, cnt);
            if ((cnt = PhongRaster.edge(xs[2], ys[2], zs[2], iw[2], aw[2], xs[0], ys[0], zs[0], iw[0], aw[0], yc, xa, za, wa, aa, n, cnt)) < 2) continue;
            int i0 = 0;
            int i1 = 1;
            if (cnt == 3) {
                i0 = 0;
                i1 = 0;
                for (int i = 1; i < 3; ++i) {
                    if (xa[i] < xa[i0]) {
                        i0 = i;
                    }
                    if (!(xa[i] > xa[i1])) continue;
                    i1 = i;
                }
            } else if (xa[0] > xa[1]) {
                i0 = 1;
                i1 = 0;
            }
            int xStart = Math.max(0, (int)Math.ceil(xa[i0] - 0.5f));
            int xEnd = Math.min(fb.width() - 1, (int)Math.floor(xa[i1] - 0.5f));
            float span = xa[i1] - xa[i0];
            float wa0 = wa[i0];
            float waD = wa[i1] - wa0;
            float za0 = za[i0];
            float zaD = za[i1] - za0;
            float[] row0 = aa[i0];
            float[] row1 = aa[i1];
            for (int x = xStart; x <= xEnd; ++x) {
                float xc = (float)x + 0.5f;
                float t = span < 1.0E-6f ? 0.0f : (xc - xa[i0]) / span;
                float invW = wa0 + waD * t;
                if (invW < 1.0E-9f) continue;
                float inv = 1.0f / invW;
                float nx = (row0[0] + (row1[0] - row0[0]) * t) * inv;
                float ny = (row0[1] + (row1[1] - row0[1]) * t) * inv;
                float nz = (row0[2] + (row1[2] - row0[2]) * t) * inv;
                float nl = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (nl < 1.0E-9f) continue;
                nx /= nl;
                ny /= nl;
                nz /= nl;
                float px = (row0[3] + (row1[3] - row0[3]) * t) * inv;
                float py = (row0[4] + (row1[4] - row0[4]) * t) * inv;
                float pz = (row0[5] + (row1[5] - row0[5]) * t) * inv;
                float tR = albR;
                float tG = albG;
                float tB = albB;
                if (textured) {
                    float u = (row0[6] + (row1[6] - row0[6]) * t) * inv;
                    float v = (row0[7] + (row1[7] - row0[7]) * t) * inv;
                    int s = tex.sample(u, v, filter);
                    tR *= (float)(s >> 16 & 0xFF) / 255.0f;
                    tG *= (float)(s >> 8 & 0xFF) / 255.0f;
                    tB *= (float)(s & 0xFF) / 255.0f;
                }
                float vx = camPx - px;
                float vy = camPy - py;
                float vz = camPz - pz;
                float vl = (float)Math.sqrt(vx * vx + vy * vy + vz * vz);
                if (vl < 1.0E-9f) continue;
                vx /= vl;
                vy /= vl;
                vz /= vl;
                float ndl = nx * lx + ny * ly + nz * lz;
                if (ndl < 0.0f) {
                    ndl = 0.0f;
                }
                float diff = ndl * inten;
                float r = tR * amb + tR * lcR * diff;
                float g = tG * amb + tG * lcG * diff;
                float b = tB * amb + tB * lcB * diff;
                if (ndl > 0.0f && useSpec) {
                    float hx = lx + vx;
                    float hy = ly + vy;
                    float hz = lz + vz;
                    float hl = (float)Math.sqrt(hx * hx + hy * hy + hz * hz);
                    if (hl >= 1.0E-9f) {
                        float ndh = (nx * hx + ny * hy + nz * hz) / hl;
                        if (ndh > 0.0f) {
                            float spec = (float)Math.pow(ndh, shine) * inten;
                            r += ksR * lcR * spec;
                            g += ksG * lcG * spec;
                            b += ksB * lcB * spec;
                        }
                    }
                }
                int ri = Math.max(0, Math.min(255, Math.round(r * 255.0f)));
                int gi = Math.max(0, Math.min(255, Math.round(g * 255.0f)));
                int bi = Math.max(0, Math.min(255, Math.round(b * 255.0f)));
                float z = za0 + zaD * t;
                fb.setPixel(x, y, z, ri << 16 | gi << 8 | bi);
            }
        }
    }
}
