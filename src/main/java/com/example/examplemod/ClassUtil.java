package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class ClassUtil {

    /** Порядок совпадает с метками в аддоне: warrior = 0.001, tank = 0.002 и так далее */
    public static final String[] CLASSES = {
            "warrior", "tank", "archer", "assassin", "berserker", "znakhar", "hero", "samurai",
            "mage", "summoner"
    };

    public static final int POINT_EVERY = 5;   // очко умений каждые 5 уровней
    public static final int MAX_POINTS = 8;    // 4 перка + 4 навыка

    /** Id класса игрока или пустая строка, если класс не выбран */
    public static String get(Player player) {
        double v = player.getAttributeValue(Attributes.OXYGEN_BONUS);
        double fraction = v - Math.floor(v); // целая часть может прийти от зачарования «Подводное дыхание»
        int index = (int) Math.round(fraction * 1000.0);
        return index >= 1 && index <= CLASSES.length ? CLASSES[index - 1] : "";
    }

    public static MutableComponent displayName(String id) {
        return Component.translatable("class." + ExampleMod.MODID + "." + id);
    }

    /** Сколько очков умений уже получено при таком уровне персонажа */
    public static int skillPoints(PlayerStats stats) {
        return Math.min(MAX_POINTS, stats.level() / POINT_EVERY);
    }
    public static int color(String id) {
        return switch (id) {
            case "warrior" -> 0xFB7185;
            case "tank" -> 0x60A5FA;
            case "archer" -> 0x84E05A;
            case "assassin" -> 0xA66CFF;
            case "berserker" -> 0xFF5A5F;
            case "znakhar" -> 0x2DD4BF;
            case "hero" -> 0xFFC83D;
            case "samurai" -> 0xFDBA74;
            case "mage" -> 0x22D3EE;
            case "summoner" -> 0xE879F9;
            default -> 0x8E9AAF;
        };
    }
}