package com.example.examplemod;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public record TitleCounters(Map<String, Integer> values) {

    public static final TitleCounters EMPTY = new TitleCounters(Map.of());

    public static final Codec<TitleCounters> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT)
            .xmap(m -> new TitleCounters(Map.copyOf(m)), TitleCounters::values);

    public static String entityKey(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
    }

    public static int get(Player p, String key) {
        return p.getData(ModAttachments.COUNTERS).values().getOrDefault(key, 0);
    }

    /** Увеличивает на 1 сразу несколько счётчиков за одну запись данных */
    public static void add(Player p, String... keys) {
        Map<String, Integer> copy = new HashMap<>(p.getData(ModAttachments.COUNTERS).values());
        for (String k : keys) copy.merge(k, 1, Integer::sum);
        p.setData(ModAttachments.COUNTERS, new TitleCounters(Map.copyOf(copy)));
    }

    public static void addAll(Player p, Collection<String> keys) {
        add(p, keys.toArray(new String[0]));
    }
}