package engine.fx;

import engine.render.Framebuffer;

public final class PostProcess {
    private boolean enabled = true;
    private float flash = 0.0f;
    private int flashRgb = 0xFFFFFF;
    private float fade = 0.0f;
    private float vignette = 0.35f;
    private int lastW = -1;
    private int lastH = -1;
    private float[] vigLut;

    public void setVignette(float v) {
        this.vignette = Math.max(0.0f, Math.min(1.0f, v));
    }

    public float vignette() {
        return this.vignette;
    }

    public void flash(float intensity, int rgb) {
        this.flash = Math.max(this.flash, Math.max(0.0f, Math.min(1.0f, intensity)));
        this.flashRgb = rgb & 0xFFFFFF;
    }

    public float flash() {
        return this.flash;
    }

    public void setFade(float f) {
        this.fade = Math.max(0.0f, Math.min(1.0f, f));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public float fade() {
        return this.fade;
    }

    public void update(float dt) {
        if (this.flash > 0.0f) {
            this.flash = Math.max(0.0f, this.flash - dt * 2.5f);
        }
    }

    public void apply(Framebuffer fb) {
        if (!this.enabled) {
            return;
        }
        if (this.vignette <= 0.0f && this.flash <= 0.0f && this.fade <= 0.0f) {
            return;
        }
        int w = fb.width();
        int h = fb.height();
        if (w != this.lastW || h != this.lastH) {
            this.buildLut(w, h);
        }
        int[] px = fb.pixels();
        float fr = this.flashRgb >> 16 & 0xFF;
        float fg = this.flashRgb >> 8 & 0xFF;
        float fb2 = this.flashRgb & 0xFF;
        float inv = 1.0f - this.fade;
        for (int i = 0; i < px.length; ++i) {
            int c = px[i];
            float r = c >> 16 & 0xFF;
            float g = c >> 8 & 0xFF;
            float b = c & 0xFF;
            float v = this.vigLut[i];
            r *= v;
            g *= v;
            b *= v;
            if (this.flash > 0.0f) {
                r += (fr - r) * this.flash;
                g += (fg - g) * this.flash;
                b += (fb2 - b) * this.flash;
            }
            px[i] = PostProcess.clamp8(r *= inv) << 16 | PostProcess.clamp8(g *= inv) << 8 | PostProcess.clamp8(b *= inv);
        }
    }

    private void buildLut(int w, int h) {
        this.lastW = w;
        this.lastH = h;
        this.vigLut = new float[w * h];
        float cx = (float)w * 0.5f;
        float cy = (float)h * 0.5f;
        float maxR2 = cx * cx + cy * cy;
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                float dx = (float)x - cx;
                float dy = (float)y - cy;
                float r2 = (dx * dx + dy * dy) / maxR2;
                this.vigLut[y * w + x] = 1.0f - this.vignette * r2 * r2;
            }
        }
    }

    private static int clamp8(float v) {
        int i = (int)v;
        return i < 0 ? 0 : (i > 255 ? 255 : i);
    }
}
