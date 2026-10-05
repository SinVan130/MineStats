package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class StatActions {

    public static void levelUp(ServerPlayer p, String id) {
        Stat stat = Stat.byId(id);
        if (stat == null) return;
        String base = "stats." + ExampleMod.MODID + ".";

        PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);
        PlayerStats next = s.levelUp(stat);
        if (next == null) {
            p.displayClientMessage(Component.translatable(base + "maxed", stat.displayName()), true);
            return;
        }
        int cost = s.levelUpCost();
        int have = XpUtil.total(p);
        if (have < cost) {
            p.displayClientMessage(Component.translatable(base + "not_enough_xp", cost, have), true);
            return;
        }
        p.giveExperiencePoints(-cost);
        p.setData(ModAttachments.PLAYER_STATS, next);
        StatEffects.apply(p);
        Fancy.burst(p, stat.rgb);
        p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 0.5f, 1.2f);
        p.displayClientMessage(Component.translatable(base + "levelup",
                next.level(), stat.displayName(), next.get(stat)), true);
    }
}