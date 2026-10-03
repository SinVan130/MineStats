package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class WeaponRarity {

    public static final int S_RANK = 5;
    private static final String[] LETTERS = {"E", "D", "C", "B", "A", "S"};
    private static final int[] COLORS = {
            0xA66CFF, 0x4D9DFF, 0x2DD4BF, 0x84E05A, 0xFF5A5F, 0xFFC83D
    };
    private static final int BEYOND_S_END = 0xFF4FD8;

    public static String name(int rank) {
        if (rank < 0) rank = 0;
        if (rank < LETTERS.length) return LETTERS[rank];
        int plus = rank - S_RANK;
        return plus <= 3 ? "S" + "+".repeat(plus) : "S+" + plus;
    }

    public static int rgb(int rank) {
        if (rank <= S_RANK) return COLORS[Math.max(rank, 0)];
        float t = Math.min(1f, (rank - S_RANK) / 8f);
        return lerp(COLORS[S_RANK], BEYOND_S_END, t);
    }

    private static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (r << 16) | (g << 8) | bl;
    }

    public static Style style(int rank) {
        Style s = Style.EMPTY.withColor(TextColor.fromRgb(rgb(rank)));
        return rank > S_RANK ? s.withBold(true) : s;
    }

    public static MutableComponent component(int rank) {
        return Component.literal(name(rank)).withStyle(style(rank));
    }

    /** Бонус к урону: до S по +1 за ранг, после S затухающая прибавка */
    public static double damageBonus(int rank) {
        double main = Math.min(rank, S_RANK) * 1.0;
        double extra = rank > S_RANK ? 0.6 * Math.sqrt(rank - S_RANK) : 0.0;
        return main + extra;
    }
}