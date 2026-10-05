package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ManaUtil {

    public static final int LEVEL_MANA = 4;  // маны на один «уровень требования» слота
    public static final int SLOT_MANA = 20;  // маны за ступень лазурита (слот 1/2/3 = 10/20/30)
    public static final int MANA_COLOR = 0x60A5FA;
    public static final int WARN_COLOR = 0xFB7185;
    public static final int ANVIL_MANA = 5;  // маны за один уровень стоимости наковальни
    public static final int ANVIL_COST_MULTIPLIER = 5;  // во сколько раз наковальня дороже ванили

    public static int mana(Player p) {
        return p.getData(ModAttachments.PLAYER_STATS).mana();
    }

    public static boolean spend(ServerPlayer p, int amount) {
        PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);
        if (s.mana() < amount) return false;
        p.setData(ModAttachments.PLAYER_STATS, s.withMana(s.mana() - amount));
        return true;
    }

    public static void add(ServerPlayer p, int amount) {
        PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);
        p.setData(ModAttachments.PLAYER_STATS, s.withMana(s.mana() + amount));
    }

    public static MutableComponent message(String key, int rgb, Object... args) {
        return Component.translatable("message." + ExampleMod.MODID + "." + key, args)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }
}