package com.example.examplemod.client;

import net.minecraft.client.gui.GuiGraphics;

public class Ui {

    public static final int TEXT = 0xFFF2F4F8;
    public static final int MUTED = 0xFF8E9AAF;
    public static final int DIM = 0xFF5B6680;
    public static final int GOOD = 0xFF6EE7B7;
    public static final int BAD = 0xFFFB7185;
    public static final int MANA = 0xFF60A5FA;
    public static final int ACCENT = 0xFFFFC83D;
    public static final int BORDER = 0xFF2B3556;

    public static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }

    private static int lighten(int argb, float t) {
        int r = (argb >> 16) & 255;
        int gr = (argb >> 8) & 255;
        int b = argb & 255;
        r += (255 - r) * t;
        gr += (255 - gr) * t;
        b += (255 - b) * t;
        return 0xFF000000 | (r << 16) | (gr << 8) | b;
    }

    /** Основное окно: рамка, градиент, блик сверху */
    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, BORDER);
        g.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF1A2038, 0xFF0E1222);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x22FFFFFF);
    }

    /** Вдавленная карточка внутри окна */
    public static void inset(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xFF222B48);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF0C1020);
    }

    public static void bar(GuiGraphics g, int x, int y, int w, int h, float frac, int color) {
        frac = Math.max(0f, Math.min(1f, frac));
        g.fill(x, y, x + w, y + h, 0xFF05070F);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF161B2E);
        int fw = Math.round((w - 2) * frac);
        if (fw > 0) {
            g.fillGradient(x + 1, y + 1, x + 1 + fw, y + h - 1, lighten(color, 0.35f), color);
        }
    }
}