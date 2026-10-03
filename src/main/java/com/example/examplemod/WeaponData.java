package com.example.examplemod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WeaponData(int level, long kills, long progress, long boss, int luckBonus) {

    public static final WeaponData DEFAULT = new WeaponData(1, 0, 0, 0, 0);

    public static final Codec<WeaponData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("level").forGetter(WeaponData::level),
            Codec.LONG.fieldOf("kills").forGetter(WeaponData::kills),
            Codec.LONG.fieldOf("progress").forGetter(WeaponData::progress),
            Codec.LONG.fieldOf("boss").forGetter(WeaponData::boss),
            Codec.INT.optionalFieldOf("luck_bonus", 0).forGetter(WeaponData::luckBonus)
    ).apply(i, WeaponData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WeaponData::level,
            ByteBufCodecs.VAR_LONG, WeaponData::kills,
            ByteBufCodecs.VAR_LONG, WeaponData::progress,
            ByteBufCodecs.VAR_LONG, WeaponData::boss,
            ByteBufCodecs.VAR_INT, WeaponData::luckBonus,
            WeaponData::new
    );

    /** Сколько убийств любых мобов нужно, чтобы выйти с текущего уровня */
    public long requiredKills() {
        long mult = 1L << Math.min(level - 1, 40);
        return (100 + level) * mult;
    }

    /** Нужен ли босс: на каждом втором уровне */
    public long requiredBosses() {
        return level % 2 == 0 ? 1 : 0;
    }

    public WeaponData withLuckBonus(int bonus) {
        return new WeaponData(level, kills, progress, boss, Math.max(0, bonus));
    }

    public WeaponData addKill(boolean isBoss) {
        long p = Math.min(progress + 1, requiredKills());
        long b = isBoss ? Math.min(boss + 1, requiredBosses()) : boss;
        long total = kills + 1;
        if (p >= requiredKills() && b >= requiredBosses()) {
            return new WeaponData(level + 1, total, 0, 0, luckBonus);
        }
        return new WeaponData(level, total, p, b, luckBonus);
    }
}