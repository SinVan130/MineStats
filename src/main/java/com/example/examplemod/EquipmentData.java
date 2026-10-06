package com.example.examplemod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import io.netty.buffer.ByteBuf;

public record EquipmentData(
        long points,
        long enchantPoints,
        int vitality,
        int strength,
        int dexterity,
        int intelligence,
        int luck
) {

    public static final EquipmentData DEFAULT =
            new EquipmentData(0, 0, 0, 0, 0, 0, 0);

    public static final Codec<EquipmentData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.optionalFieldOf("points", 0L).forGetter(EquipmentData::points),
            Codec.LONG.optionalFieldOf("enchant_points", 0L).forGetter(EquipmentData::enchantPoints),
            Codec.INT.optionalFieldOf("vitality", 0).forGetter(EquipmentData::vitality),
            Codec.INT.optionalFieldOf("strength", 0).forGetter(EquipmentData::strength),
            Codec.INT.optionalFieldOf("dexterity", 0).forGetter(EquipmentData::dexterity),
            Codec.INT.optionalFieldOf("intelligence", 0).forGetter(EquipmentData::intelligence),
            Codec.INT.optionalFieldOf("luck", 0).forGetter(EquipmentData::luck)
    ).apply(i, EquipmentData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeVarLong(data.points());
                buf.writeVarLong(data.enchantPoints());
                buf.writeVarInt(data.vitality());
                buf.writeVarInt(data.strength());
                buf.writeVarInt(data.dexterity());
                buf.writeVarInt(data.intelligence());
                buf.writeVarInt(data.luck());
            },
            buf -> new EquipmentData(
                    buf.readVarLong(),
                    buf.readVarLong(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt()
            )
    );

    public int rank() {
        return RankPoints.rankFor(points);
    }
    public int usedSlots() {
        int slots = 0;
        if (vitality > 0) slots++;
        if (strength > 0) slots++;
        if (dexterity > 0) slots++;
        if (intelligence > 0) slots++;
        if (luck > 0) slots++;
        return slots;
    }

    public int maxSlots() {
        return Math.max(0, rank() + 1);
    }

    public EquipmentData addPoints(long amount) {
        return new EquipmentData(
                Math.max(RankPoints.MIN_POINTS, points + amount),
                enchantPoints,
                vitality,
                strength,
                dexterity,
                intelligence,
                luck
        );
    }

    public EquipmentData withEnchantPoints(long value) {
        long delta = value - enchantPoints;

        return new EquipmentData(
                Math.max(RankPoints.MIN_POINTS, points + delta),
                value,
                vitality,
                strength,
                dexterity,
                intelligence,
                luck
        );
    }

    public EquipmentData withStat(Stat stat, int amount) {
        return switch (stat) {
            case VITALITY -> new EquipmentData(points, enchantPoints, Math.max(0, amount), strength, dexterity, intelligence, luck);
            case STRENGTH -> new EquipmentData(points, enchantPoints, vitality, Math.max(0, amount), dexterity, intelligence, luck);
            case DEXTERITY -> new EquipmentData(points, enchantPoints, vitality, strength, Math.max(0, amount), intelligence, luck);
            case INTELLIGENCE -> new EquipmentData(points, enchantPoints, vitality, strength, dexterity, Math.max(0, amount), luck);
            case LUCK -> new EquipmentData(points, enchantPoints, vitality, strength, dexterity, intelligence, Math.max(0, amount));
        };
    }

    public int get(Stat stat) {
        return switch (stat) {
            case VITALITY -> vitality;
            case STRENGTH -> strength;
            case DEXTERITY -> dexterity;
            case INTELLIGENCE -> intelligence;
            case LUCK -> luck;
        };
    }
}