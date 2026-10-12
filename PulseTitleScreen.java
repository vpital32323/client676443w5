package dev.pulse.menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class PulseTitleScreen extends Screen {
    public static final String NAME = "Delta Pulse";
    public static final String VERSION = "2026.10.12 \u00b7 1.21";
    private static final int ACCENT = 0xFF2D3F;

    private static final int PW = 156;
    private static final int PH = 274;

    private static final int[] ROW_Y = {60, 84, 124, 148, 188};
    private static final String[] ROW_TEXT = {"\u041e\u0434\u0438\u043d\u043e\u0447\u043d\u0430\u044f \u0438\u0433\u0440\u0430", "\u0421\u0435\u0442\u0435\u0432\u0430\u044f \u0438\u0433\u0440\u0430", "Account", "Friends", "Mods"};
    private static final int[] ROW_ICON = {Gfx.CUBE, Gfx.WIFI, Gfx.USER, Gfx.HEART, Gfx.LIST};
    private static final int[] BTN_ICON = {Gfx.GEAR, Gfx.USER, Gfx.DIAMOND, Gfx.SHIELD, Gfx.CLOSE};
    private static final int BTN_Y = 218;

    // top bar: left to right -> background mode
    private static final int[] TOP_ICON = {Gfx.MOON, Gfx.SLIDERS, Gfx.WIND, Gfx.SPIRAL, Gfx.SUN};
    private static final int[] TOP_MODE = {Backgrounds.STARS, Backgrounds.TOPO, Backgrounds.SMOKE,
            Backgrounds.NEBULA, Backgrounds.SILK};

    private final long open = System.nanoTime();
    private final float[] hov = new float[16];
    private String toast = null;
    private long toastAt = 0;

    public PulseTitleScreen() {
        super(Text.literal(NAME));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private float panelScale() {
        return Math.min(1f, (height - 24) / (float) PH);
    }

    private float originX() {
        return (width - PW * panelScale()) / 2f;
    }

    private float originY() {
        return (height - PH * panelScale()) / 2f;
    }

    private float intro() {
        return Gfx.ease(((System.nanoTime() - open) / 1_000_000_000f) / 0.8f);
    }

    private int topX(int k) {
        return width - 10 - (5 - k) * 18 - (4 - k) * 4;
    }

    private void text(DrawContext c, String s, float x, float y, float sc, int col) {
        MatrixStack m = c.getMatrices();
        m.push();
        m.translate(x, y, 0f);
        m.scale(sc, sc, 1f);
        c.drawText(this.textRenderer, s, 0, 0, col, false);
        m.pop();
    }

    private void lerpHover(int i, boolean over) {
        hov[i] += ((over ? 1f : 0f) - hov[i]) * 0.22f;
    }

    private boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private double localX(double mx) {
        return (mx - originX()) / panelScale();
    }

    private double localY(double my) {
        return (my - originY() - (1f - intro()) * 16f * panelScale()) / panelScale();
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        float now = (System.nanoTime() - open) / 1_000_000_000f;
        float t = Backgrounds.time();
        float intro = intro();

        Backgrounds.draw(c, width, height, 1f);

        // ---- top bar (screen coordinates) ----
        for (int k = 0; k < 5; k++) {
            int x = topX(k);
            int y = 9;
            boolean active = Backgrounds.getMode() == TOP_MODE[k];
            lerpHover(10 + k, inRect(mouseX, mouseY, x, y, 18, 18));
            float h = hov[10 + k];
            float ap = Gfx.ease((now - 0.1f - 0.07f * k) / 0.5f);
            if (active) {
                Gfx.rrect(c, x - 2, y - 2, 22, 22, 8, Gfx.argb(ACCENT, (0.16f + 0.06f * (float) Math.sin(t * 3f)) * ap));
                Gfx.rrect(c, x, y, 18, 18, 6, Gfx.argb(ACCENT, 0.92f * ap));
                Gfx.icon(c, TOP_ICON[k], x + 9, y + 9, Gfx.argb(0xFFFFFF, ap));
            } else {
                Gfx.rrect(c, x, y, 18, 18, 6, Gfx.argb(0x181416, (0.55f + 0.2f * h) * ap));
                Gfx.icon(c, TOP_ICON[k], x + 9, y + 9, Gfx.argb(ACCENT, (0.75f + 0.25f * h) * ap));
            }
        }

        // ---- panel (scaled local coordinates) ----
        float s = panelScale();
        float ox = originX();
        float oy = originY() + (1f - intro) * 16f * s;
        double lx = (mouseX - ox) / s;
        double ly = (mouseY - oy) / s;

        MatrixStack m = c.getMatrices();
        m.push();
        m.translate(ox, oy, 0f);
        m.scale(s, s, 1f);

        float pulse = 0.09f + 0.05f * (float) Math.sin(t * 2f);
        Gfx.rrect(c, -5, -5, PW + 10, PH + 10, 16, Gfx.argb(ACCENT, pulse * 0.6f * intro));
        Gfx.rrect(c, -2, -2, PW + 4, PH + 4, 13, Gfx.argb(ACCENT, pulse * intro));
        Gfx.rrect(c, -1, -1, PW + 2, PH + 2, 12, Gfx.argb(ACCENT, 0.5f * intro));
        Gfx.rrect(c, 0, 0, PW, PH, 11, Gfx.argb(0x141416, 0.94f * intro));

        // header
        float hp = Gfx.ease((now - 0.15f) / 0.5f);
        Gfx.rrect(c, 10, 10, 28, 28, 8, Gfx.argb(0xFFFFFF, 0.06f * hp));
        m.push();
        m.translate(24f, 24f, 0f);
        m.scale(1.7f, 1.7f, 1f);
        Gfx.icon(c, Gfx.SPIRAL, 0, 0, Gfx.argb(0xB46CFF, hp));
        m.pop();
        text(c, NAME, 46, 13, 1f, Gfx.argb(0xFFFFFF, hp));
        text(c, VERSION, 46, 26, 0.62f, Gfx.argb(0x8A8A90, hp));

        sectionLabel(c, "PLAY", 12, 48, now, 0.2f);
        sectionLabel(c, "CLIENT", 12, 112, now, 0.35f);
        sectionLabel(c, "MODS", 12, 176, now, 0.5f);

        // rows
        for (int i = 0; i < 5; i++) {
            int y = ROW_Y[i];
            float ap = Gfx.ease((now - 0.25f - 0.07f * i) / 0.45f) * intro;
            lerpHover(i, inRect(lx, ly, 8, y, PW - 16, 20));
            float h = hov[i];
            Gfx.rrect(c, 8, y, PW - 16, 20, 7, Gfx.argb(0xFFFFFF, (0.04f + 0.07f * h) * ap));
            Gfx.rrect(c, 8, y + 4, 2, 12, 1, Gfx.argb(ACCENT, h * ap));
            Gfx.icon(c, ROW_ICON[i], 20, y + 10, Gfx.argb(ACCENT, ap));
            text(c, ROW_TEXT[i], 33 + h * 2.5f, y + 6, 0.9f, Gfx.argb(0xE8E8EC, ap));
        }

        // bottom icon buttons
        for (int i = 0; i < 5; i++) {
            int x = 11 + i * 28;
            float ap = Gfx.ease((now - 0.7f - 0.06f * i) / 0.4f) * intro;
            lerpHover(5 + i, inRect(lx, ly, x, BTN_Y, 22, 22));
            float h = hov[5 + i];
            Gfx.rrect(c, x, BTN_Y, 22, 22, 7, Gfx.argb(ACCENT, (0.10f + 0.20f * h) * ap));
            Gfx.icon(c, BTN_ICON[i], x + 11, BTN_Y + 11, Gfx.argb(ACCENT, ap));
        }

        // user card
        float up = Gfx.ease((now - 1.0f) / 0.45f) * intro;
        Gfx.rrect(c, 8, 246, PW - 16, 20, 7, Gfx.argb(ACCENT, 0.14f * up));
        Gfx.rrect(c, 12, 249, 14, 14, 4, Gfx.argb(0x7A5A3A, up));
        String user = (this.client != null && this.client.getSession() != null)
                ? this.client.getSession().getUsername() : "Player";
        text(c, user, 31, 250, 0.8f, Gfx.argb(0xFFFFFF, up));
        text(c, "User", 31, 258, 0.55f, Gfx.argb(0xFF8A94, up));

        m.pop();

        // footer
        text(c, NAME + " \u00b7 " + VERSION, width - textRenderer.getWidth(NAME + " \u00b7 " + VERSION) * 0.6f - 8,
                height - 12, 0.6f, Gfx.argb(0xFFFFFF, 0.35f * intro));

        // toast
        if (toast != null) {
            float age = (System.nanoTime() - toastAt) / 1_000_000_000f;
            if (age > 2.2f) {
                toast = null;
            } else {
                float a = Gfx.ease(age / 0.25f) * (1f - Gfx.clamp01((age - 1.8f) / 0.4f));
                int tw = (int) (textRenderer.getWidth(toast) * 0.9f) + 16;
                int tx = (width - tw) / 2;
                int ty = height - 34 - (int) ((1f - Gfx.ease(age / 0.25f)) * 8);
                Gfx.rrect(c, tx, ty, tw, 16, 7, Gfx.argb(0x141416, 0.92f * a));
                Gfx.rrect(c, tx, ty + 3, 2, 10, 1, Gfx.argb(ACCENT, a));
                text(c, toast, tx + 9, ty + 4, 0.9f, Gfx.argb(0xFFFFFF, a));
            }
        }
    }

    private void sectionLabel(DrawContext c, String s, int x, int y, float now, float delay) {
        float a = Gfx.ease((now - delay) / 0.4f);
        text(c, s, x, y, 0.65f, Gfx.argb(0x8A8A90, a));
    }

    private void showToast(String s) {
        toast = s;
        toastAt = System.nanoTime();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || this.client == null) return super.mouseClicked(mouseX, mouseY, button);

        for (int k = 0; k < 5; k++) {
            if (inRect(mouseX, mouseY, topX(k), 9, 18, 18)) {
                Backgrounds.setMode(TOP_MODE[k]);
                return true;
            }
        }

        double lx = localX(mouseX);
        double ly = localY(mouseY);

        for (int i = 0; i < 5; i++) {
            if (inRect(lx, ly, 8, ROW_Y[i], PW - 16, 20)) {
                switch (i) {
                    case 0 -> this.client.setScreen(new SelectWorldScreen(this));
                    case 1 -> this.client.setScreen(new MultiplayerScreen(this));
                    case 2 -> showToast("Account: \u0441\u043a\u043e\u0440\u043e");
                    case 3 -> showToast("Friends: \u0441\u043a\u043e\u0440\u043e");
                    default -> showToast("Mods: \u0441\u043a\u043e\u0440\u043e");
                }
                return true;
            }
        }
        for (int i = 0; i < 5; i++) {
            if (inRect(lx, ly, 11 + i * 28, BTN_Y, 22, 22)) {
                switch (i) {
                    case 0 -> this.client.setScreen(new OptionsScreen(this, this.client.options));
                    case 1 -> showToast("Profile: \u0441\u043a\u043e\u0440\u043e");
                    case 2 -> showToast("Cosmetics: \u0441\u043a\u043e\u0440\u043e");
                    case 3 -> showToast("Security: \u0441\u043a\u043e\u0440\u043e");
                    default -> this.client.scheduleStop();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
