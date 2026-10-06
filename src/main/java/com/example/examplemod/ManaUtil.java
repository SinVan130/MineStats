package com.example.examplemod;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ManaUtil {

    public static final int MANA_COLOR = 0x60A5FA;
    public static final int WARN_COLOR = 0xFB7185;

    /**
     * Получить текущую ману Iron's Spellbooks.
     */
    public static float mana(Player p) {
        return MagicData.getPlayerMagicData(p).getMana();
    }

    /**
     * Получить максимальную ману Iron's Spellbooks.
     */
    public static float maxMana(Player p) {
        return (float) p.getAttributeValue(AttributeRegistry.MAX_MANA);
    }

    /**
     * Установить ману.
     */
    public static void set(ServerPlayer p, float amount) {
        MagicData data = MagicData.getPlayerMagicData(p);

        float max = maxMana(p);

        data.setMana(Math.max(0.0f, Math.min(amount, max)));
    }

    /**
     * Добавить ману.
     */
    public static void add(ServerPlayer p, float amount) {
        MagicData data = MagicData.getPlayerMagicData(p);

        float max = maxMana(p);

        data.setMana(Math.min(data.getMana() + amount, max));
    }

    /**
     * Потратить ману.
     *
     * @return true, если маны хватило.
     */
    public static boolean spend(ServerPlayer p, float amount) {
        MagicData data = MagicData.getPlayerMagicData(p);

        if (data.getMana() < amount) {
            return false;
        }

        data.setMana(data.getMana() - amount);
        return true;
    }

    /**
     * Проверить, хватает ли маны.
     */
    public static boolean has(ServerPlayer p, float amount) {
        return mana(p) >= amount;
    }

    public static MutableComponent message(String key, int rgb, Object... args) {
        return Component.translatable(
                "message." + ExampleMod.MODID + "." + key,
                args
        ).withStyle(
                Style.EMPTY.withColor(TextColor.fromRgb(rgb))
        );
    }
}