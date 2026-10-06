package com.example.examplemod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PlayerStats(int vitality, int strength, int dexterity,
                          int intelligence, int luck) {

    public static final int BASE = 10;
    public static final int MAX_STAT = 99;
    public static final PlayerStats DEFAULT = new PlayerStats(BASE, BASE, BASE, BASE, BASE);

    public static final Codec<PlayerStats> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("vitality", BASE).forGetter(PlayerStats::vitality),
            Codec.INT.optionalFieldOf("strength", BASE).forGetter(PlayerStats::strength),
            Codec.INT.optionalFieldOf("dexterity", BASE).forGetter(PlayerStats::dexterity),
            Codec.INT.optionalFieldOf("intelligence", BASE).forGetter(PlayerStats::intelligence),
            Codec.INT.optionalFieldOf("luck", BASE).forGetter(PlayerStats::luck)
    ).apply(i, PlayerStats::new));
    public static final StreamCodec<ByteBuf, PlayerStats> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlayerStats::vitality,
            ByteBufCodecs.VAR_INT, PlayerStats::strength,
            ByteBufCodecs.VAR_INT, PlayerStats::dexterity,
            ByteBufCodecs.VAR_INT, PlayerStats::intelligence,
            ByteBufCodecs.VAR_INT, PlayerStats::luck,
            PlayerStats::new);



    public int get(Stat stat) {
        return switch (stat) {
            case VITALITY -> vitality;
            case STRENGTH -> strength;
            case DEXTERITY -> dexterity;
            case INTELLIGENCE -> intelligence;
            case LUCK -> luck;
        };
    }

    public PlayerStats with(Stat stat, int value) {
        int v = Math.max(1, Math.min(MAX_STAT, value));

        return switch (stat) {
            case VITALITY -> new PlayerStats(v, strength, dexterity, intelligence, luck);
            case STRENGTH -> new PlayerStats(vitality, v, dexterity, intelligence, luck);
            case DEXTERITY -> new PlayerStats(vitality, strength, v, intelligence, luck);
            case INTELLIGENCE -> new PlayerStats(vitality, strength, dexterity, v, luck);
            case LUCK -> new PlayerStats(vitality, strength, dexterity, intelligence, v);
        };
    }

    /** Уровень персонажа: 1 + сумма очков выше базы */
    public int level() {
        return Math.max(1, 1 + vitality + strength + dexterity + intelligence + luck - 5 * BASE);
    }

    /** Стоимость следующего уровня в очках ванильного опыта */
    public int levelUpCost() {
        return (int) Math.round(30 * Math.pow(1.12, Math.min(level() - 1, 120)));
    }

    public int maxMana() {
        return Math.max(10, 10 + 5 * (intelligence - BASE));
    }

    public int manaRegen() {
        return Math.max(1, 1 + (intelligence - BASE) / 10);
    }


    /** Повышает стат на 1. null, если он уже на максимуме. */
    public PlayerStats levelUp(Stat stat) {
        if (get(stat) >= MAX_STAT) return null;
        return with(stat, get(stat) + 1);
    }
}