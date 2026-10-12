package dev.pulse.menu;

import net.minecraft.client.gui.DrawContext;

public final class Gfx {
    private Gfx() {}

    public static final int CUBE = 0, WIFI = 1, USER = 2, HEART = 3, LIST = 4, GEAR = 5,
            DIAMOND = 6, SHIELD = 7, CLOSE = 8, MOON = 9, SLIDERS = 10, WIND = 11,
            SPIRAL = 12, SUN = 13;

    public static int argb(int rgb, float a) {
        int al = Math.max(0, Math.min(255, (int) (a * 255f)));
        return (al << 24) | (rgb & 0xFFFFFF);
    }

    public static int mix(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (r << 16) | (g << 8) | b;
    }

    public static int pal(float v, int[] stops) {
        v = Math.max(0f, Math.min(0.9999f, v));
        float pos = v * (stops.length - 1);
        int i = (int) pos;
        return mix(stops[i], stops[i + 1], pos - i);
    }

    public static float clamp01(float x) {
        return Math.max(0f, Math.min(1f, x));
    }

    public static float ease(float x) {
        x = clamp01(x);
        float inv = 1f - x;
        return 1f - inv * inv * inv;
    }

    /** Rounded rectangle made of horizontal strips. */
    public static void rrect(DrawContext c, int x, int y, int w, int h, int r, int col) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            c.fill(x, y, x + w, y + h, col);
            return;
        }
        c.fill(x, y + r, x + w, y + h - r, col);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(r * (double) r - dy * dy));
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, col);
            c.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, col);
        }
    }

    private static boolean px(int k, int x, int y) {
        double d = Math.hypot(x, y);
        int ax = Math.abs(x), ay = Math.abs(y);
        switch (k) {
            case CUBE:
                return (ax == 4 && ay <= 4) || (ay == 4 && ax <= 4) || (x == 0 && ay <= 4)
                        || (y == 0 && ax <= 4 && ax >= 0 && (x % 2 == 0));
            case WIFI: {
                int yy = y - 3;
                double dd = Math.hypot(x, yy);
                if (dd < 0.9) return true;
                if (yy < 0 && ax <= -yy) {
                    return Math.abs(dd - 2.5) < 0.6 || Math.abs(dd - 4.7) < 0.6 || Math.abs(dd - 6.9) < 0.6;
                }
                return false;
            }
            case USER:
                return Math.abs(d - 6) < 0.7 || Math.hypot(x, y + 1.5) < 1.9
                        || (y >= 2 && d < 5.8 && Math.abs(Math.hypot(x, y - 5.5) - 3.6) < 0.8);
            case HEART: {
                double hx = x / 4.2, hy = -(y - 0.6) / 4.2;
                double a = hx * hx + hy * hy - 1;
                return a * a * a - hx * hx * hy * hy * hy <= 0;
            }
            case LIST:
                return (ay == 3 || y == 0) && ((x >= -1 && x <= 5) || x == -5);
            case GEAR:
                return Math.abs(d - 3.4) < 0.75
                        || (d > 4 && d < 6.2 && (ax <= 1 || ay <= 1 || ax == ay));
            case DIAMOND:
                return ax + ay == 4 || ax + ay <= 1;
            case SHIELD: {
                if (y < -5 || y > 5) return false;
                int hw = y <= 0 ? 4 : Math.max(0, 5 - y);
                return ax <= hw && !(ax <= 1 && ay <= 1);
            }
            case CLOSE:
                return ax == ay && ax <= 4;
            case MOON:
                return d <= 5.6 && Math.hypot(x - 2.6, y + 1.6) > 4.6;
            case SLIDERS:
                return ((ay == 3 || y == 0) && ax <= 5)
                        || Math.hypot(x + 2, y + 3) < 1.9 || Math.hypot(x - 2, y) < 1.9
                        || Math.hypot(x + 1, y - 3) < 1.9;
            case WIND:
                return (y == -3 && x >= -5 && x <= 3) || (y == 0 && ax <= 5) || (y == 3 && x >= -5 && x <= 1);
            case SPIRAL: {
                double th = Math.atan2(y, x);
                double q = (d - 0.5) / 1.8 - th / (2 * Math.PI);
                q -= Math.floor(q);
                return d <= 6.3 && q < 0.38;
            }
            case SUN:
                return d <= 2.9 || (d > 3.9 && d < 6.3 && (x == 0 || y == 0 || ax == ay));
            default:
                return false;
        }
    }

    public static void icon(DrawContext c, int kind, int cx, int cy, int col) {
        for (int y = -7; y <= 7; y++) {
            for (int x = -7; x <= 7; x++) {
                if (px(kind, x, y)) c.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, col);
            }
        }
    }
}
