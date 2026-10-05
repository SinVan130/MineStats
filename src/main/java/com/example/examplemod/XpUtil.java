package com.example.examplemod;

import net.minecraft.world.entity.player.Player;

public class XpUtil {

    private static int atLevel(int l) {
        if (l <= 16) return l * l + 6 * l;
        if (l <= 31) return (int) (2.5 * l * l - 40.5 * l + 360);
        return (int) (4.5 * l * l - 162.5 * l + 2220);
    }

    /** Сколько очков опыта у игрока сейчас */
    public static int total(Player p) {
        return atLevel(p.experienceLevel) + Math.round(p.experienceProgress * p.getXpNeededForNextLevel());
    }
}