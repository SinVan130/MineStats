package com.example.examplemod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WeaponData(long points, long kills, int luckBonus, long enchantPoints) {

    public static final WeaponData DEFAULT = new WeaponData(0, 0, 0, 0);

    public static final Codec<WeaponData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.optionalFieldOf("points", 0L).forGetter(WeaponData::points),
            Codec.LONG.optionalFieldOf("kills", 0L).forGetter(WeaponData::kills),
            Codec.INT.optionalFieldOf("luck_bonus", 0).forGetter(WeaponData::luckBonus),
            Codec.LONG.optionalFieldOf("enchant_points", 0L).forGetter(WeaponData::enchantPoints)
    ).apply(i, WeaponData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, WeaponData::points,
            ByteBufCodecs.VAR_LONG, WeaponData::kills,
            ByteBufCodecs.VAR_INT, WeaponData::luckBonus,
            ByteBufCodecs.VAR_LONG, WeaponData::enchantPoints,
            WeaponData::new
    );

    public int rank() {
        return RankPoints.rankFor(points);
    }

    public WeaponData addPoints(long amount) {
        return new WeaponData(Math.max(0, points + amount), kills, luckBonus, enchantPoints);
    }

    public WeaponData addKill(boolean isBoss) {
        long gain = isBoss ? RankPoints.BOSS_POINTS : RankPoints.KILL_POINTS;
        return new WeaponData(points + gain, kills + 1, luckBonus, enchantPoints);
    }

    public WeaponData withRank(int rank) {
        return new WeaponData(RankPoints.threshold(Math.max(0, rank)), kills, luckBonus, enchantPoints);
    }

    public WeaponData withLuckBonus(int bonus) {
        return new WeaponData(points, kills, Math.max(0, bonus), enchantPoints);
    }

    /** Меняет засчитанные очки за чары и сдвигает общие очки на разницу */
    public WeaponData withEnchantPoints(long newValue) {
        long delta = newValue - enchantPoints;
        return new WeaponData(Math.max(0, points + delta), kills, luckBonus, newValue);
    }
}