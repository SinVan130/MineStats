package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

public class ProcSystem {

    public static final double PITY_STEP = 0.0025; // +0.25% за каждую неудачу подряд
    public static final double PITY_CAP = 0.05;    // максимум +5%
    public static final double MAX_CHANCE = 0.95;  // гарантированного срабатывания нет
    public static final boolean DEBUG = true;      // показывать броски над хотбаром

    /** Шанс с учётом удачи: при удаче 1 равен базовому */
    public static double luckChance(Player player, double base) {
        return 1.0 - Math.pow(1.0 - base, LuckUtil.getLuck(player));
    }

    public static boolean roll(Player player, String effectId, double base) {
        if (player.level().isClientSide) return false;

        PityData pity = player.getData(ModAttachments.PITY);
        int fails = pity.fails.getOrDefault(effectId, 0);
        double pityBonus = Math.min(PITY_CAP, fails * PITY_STEP);
        double chance = Math.min(MAX_CHANCE, luckChance(player, base) + pityBonus);

        boolean success = player.getRandom().nextDouble() < chance;
        if (success) {
            pity.fails.remove(effectId);
        } else {
            pity.fails.put(effectId, fails + 1);
        }

        if (DEBUG) {
            player.displayClientMessage(Component.literal(String.format(Locale.ROOT,
                    "%s: %.1f%% (неудач подряд %d, +%.2f%%) -> %s",
                    effectId, chance * 100, fails, pityBonus * 100, success ? "СРАБОТАЛО" : "нет")), true);
        }
        return success;
    }
}