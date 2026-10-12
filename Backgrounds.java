package dev.pulse.menu;

import net.minecraft.client.gui.DrawContext;

import java.util.Random;

/** Procedural animated backgrounds drawn with plain fills (no shaders needed). */
public final class Backgrounds {
    private Backgrounds() {}

    public static final int SILK = 0, STARS = 1, TOPO = 2, SMOKE = 3, NEBULA = 4;

    private static final long START = System.nanoTime();
    private static int mode = SILK;
    private static int prev = SILK;
    private static float fadeStart = -10f;

    private static final int[] PAL_SILK = {0x0E0104, 0x4A0510, 0xB80C1E, 0xFF3B2E, 0xFF8A55};
    private static final int[] PAL_SMOKE = {0x050302, 0x2A1204, 0x7A3A0A, 0xE08A2E, 0xFFD9A0};
    private static final int[] PAL_NEB = {0x05030A, 0x1B0A2E, 0x5A1250, 0xB4203C, 0xFF7A66};

    private static final int STAR_COUNT = 260;
    private static final float[] SX = new float[STAR_COUNT];
    private static final float[] SY = new float[STAR_COUNT];
    private static final float[] SP = new float[STAR_COUNT];
    private static final float[] SS = new float[STAR_COUNT];

    static {
        Random r = new Random(7);
        for (int i = 0; i < STAR_COUNT; i++) {
            SX[i] = r.nextFloat();
            SY[i] = r.nextFloat();
            SP[i] = r.nextFloat() * 6.28f;
            SS[i] = 0.3f + r.nextFloat() * 1.2f;
        }
    }

    public static float time() {
        return (System.nanoTime() - START) / 1_000_000_000f;
    }

    public static int getMode() {
        return mode;
    }

    public static void setMode(int m) {
        if (m == mode) return;
        prev = mode;
        mode = m;
        fadeStart = time();
    }

    public static void draw(DrawContext c, int w, int h, float alpha) {
        float t = time();
        float f = Gfx.clamp01((t - fadeStart) / 0.7f);
        if (f < 1f) {
            layer(c, prev, w, h, t, alpha);
            layer(c, mode, w, h, t, alpha * Gfx.ease(f));
        } else {
            layer(c, mode, w, h, t, alpha);
        }
        // soft vignette
        c.fillGradient(0, 0, w, h / 3, Gfx.argb(0x000000, 0.45f * alpha), 0x00000000);
        c.fillGradient(0, h - h / 3, w, h, 0x00000000, Gfx.argb(0x000000, 0.55f * alpha));
    }

    private static void layer(DrawContext c, int m, int w, int h, float t, float a) {
        if (a <= 0.003f) return;
        switch (m) {
            case SILK -> flow(c, w, h, t, a, 0, PAL_SILK);
            case SMOKE -> flow(c, w, h, t, a, 1, PAL_SMOKE);
            case TOPO -> topo(c, w, h, t, a);
            case STARS -> {
                c.fill(0, 0, w, h, Gfx.argb(0x030204, a));
                flow(c, w, h, t * 0.6f, a * 0.32f, 1, PAL_SMOKE);
                stars(c, w, h, t, a);
            }
            case NEBULA -> {
                c.fill(0, 0, w, h, Gfx.argb(0x040208, a));
                flow(c, w, h, t * 0.7f, a * 0.75f, 1, PAL_NEB);
                stars(c, w, h, t, a * 0.8f);
            }
            default -> {}
        }
    }

    private static float silk(float x, float y, float t) {
        float a = (float) Math.sin(x * 2.2f + Math.sin(y * 2.1f + t * 0.30f) * 1.5f + t * 0.20f);
        float b = (float) Math.sin(x * 3.9f - y * 2.7f + t * 0.33f + a * 1.4f);
        float v = 0.5f + 0.5f * (0.65f * a + 0.35f * b);
        return (float) Math.pow(v, 1.6);
    }

    private static float smoke(float x, float y, float t) {
        float wx = x * 3f + 0.8f * (float) Math.sin(y * 4.3f + t * 0.35f);
        float wy = y * 3f + 0.8f * (float) Math.sin(x * 3.7f - t * 0.28f);
        float n = (float) (Math.sin(wx * 1.7f + wy * 1.1f + t * 0.2f)
                + Math.sin(wy * 2.3f - wx * 0.9f - t * 0.25f) * 0.7f
                + Math.sin((wx + wy) * 3.1f + t * 0.4f) * 0.35f) / 2.05f;
        float v = 1f - Math.abs(n);
        return (float) Math.pow(Math.max(0f, v), 4.0);
    }

    private static void flow(DrawContext c, int w, int h, float t, float a, int style, int[] pal) {
        final int colW = 3;
        final int rows = 22;
        float aspect = w / (float) h;
        for (int x = 0; x < w; x += colW) {
            float nx = ((x + colW * 0.5f) / w) * aspect;
            int prevCol = 0;
            int prevY = 0;
            for (int k = 0; k <= rows; k++) {
                int y = k * h / rows;
                float ny = (float) y / h;
                float v = style == 0 ? silk(nx, ny, t) : smoke(nx, ny, t);
                int col = Gfx.argb(Gfx.pal(v, pal), a);
                if (k > 0) c.fillGradient(x, prevY, Math.min(x + colW, w), y, prevCol, col);
                prevCol = col;
                prevY = y;
            }
        }
    }

    private static float height(float x, float y, float t) {
        return (float) (Math.sin(x * 1.3f + Math.sin(y * 0.9f + t * 0.2f) * 1.7f)
                + Math.sin(y * 1.7f - x * 0.6f + t * 0.15f) * 0.8f
                + Math.sin((x + y) * 0.8f + Math.sin(x * 0.5f) * 2f) * 0.6f);
    }

    private static void topo(DrawContext c, int w, int h, float t, float a) {
        c.fill(0, 0, w, h, Gfx.argb(0x0B0604, a));
        final int cs = 2;
        float aspect = w / (float) h;
        for (int y = 0; y < h; y += cs) {
            float ny = (float) y / h;
            for (int x = 0; x < w; x += cs) {
                float nx = ((float) x / w) * aspect;
                float hv = height(nx * 4.2f, ny * 4.2f, t) * 3.3f;
                float fr = Math.abs(hv) % 1f;
                if (fr < 0.06f) {
                    float glow = 0.45f + 0.55f * Gfx.clamp01((hv + 8f) / 16f);
                    c.fill(x, y, x + cs, y + cs, Gfx.argb(0xFF8A2A, a * glow));
                }
            }
        }
    }

    private static void stars(DrawContext c, int w, int h, float t, float a) {
        for (int i = 0; i < STAR_COUNT; i++) {
            float x = (SX[i] - t * 0.004f * SS[i]) % 1f;
            if (x < 0) x += 1f;
            float y = (SY[i] - t * 0.002f * SS[i]) % 1f;
            if (y < 0) y += 1f;
            float tw = 0.45f + 0.55f * (float) Math.sin(t * (0.8f + SS[i]) + SP[i]);
            int px = (int) (x * w);
            int py = (int) (y * h);
            int size = SS[i] > 1.2f ? 2 : 1;
            c.fill(px, py, px + size, py + size, Gfx.argb(0xFFE6C8, a * tw * 0.85f));
        }
    }
}
