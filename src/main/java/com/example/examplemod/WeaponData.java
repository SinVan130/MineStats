package com.example.examplemod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record WeaponData(long points, long kills, int luckBonus, long enchantPoints,
                         List<Passive> passives, List<TempEffect> temps) {

    public static final long MAX_TEMP_TICKS = 72000; // не больше часа

    public record Passive(String id, int level) {
        public static final Codec<Passive> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("id").forGetter(Passive::id),
                Codec.INT.fieldOf("level").forGetter(Passive::level)
        ).apply(i, Passive::new));
        public static final StreamCodec<ByteBuf, Passive> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Passive::id,
                ByteBufCodecs.VAR_INT, Passive::level,
                Passive::new);
    }

    /** pending: тики, ещё не вшитые в таймер; expiresAt: игровое время окончания */
    public record TempEffect(String id, int level, long pending, long expiresAt) {
        public static final Codec<TempEffect> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("id").forGetter(TempEffect::id),
                Codec.INT.fieldOf("level").forGetter(TempEffect::level),
                Codec.LONG.optionalFieldOf("pending", 0L).forGetter(TempEffect::pending),
                Codec.LONG.optionalFieldOf("expires_at", 0L).forGetter(TempEffect::expiresAt)
        ).apply(i, TempEffect::new));
        public static final StreamCodec<ByteBuf, TempEffect> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, TempEffect::id,
                ByteBufCodecs.VAR_INT, TempEffect::level,
                ByteBufCodecs.VAR_LONG, TempEffect::pending,
                ByteBufCodecs.VAR_LONG, TempEffect::expiresAt,
                TempEffect::new);
    }

    public static final WeaponData DEFAULT = new WeaponData(0, 0, 0, 0);

    public static final Codec<WeaponData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.optionalFieldOf("points", 0L).forGetter(WeaponData::points),
            Codec.LONG.optionalFieldOf("kills", 0L).forGetter(WeaponData::kills),
            Codec.INT.optionalFieldOf("luck_bonus", 0).forGetter(WeaponData::luckBonus),
            Codec.LONG.optionalFieldOf("enchant_points", 0L).forGetter(WeaponData::enchantPoints),
            Passive.CODEC.listOf().optionalFieldOf("passives", List.of()).forGetter(WeaponData::passives),
            TempEffect.CODEC.listOf().optionalFieldOf("temps", List.of()).forGetter(WeaponData::temps)
    ).apply(i, WeaponData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, WeaponData::points,
            ByteBufCodecs.VAR_LONG, WeaponData::kills,
            ByteBufCodecs.VAR_INT, WeaponData::luckBonus,
            ByteBufCodecs.VAR_LONG, WeaponData::enchantPoints,
            Passive.STREAM_CODEC.apply(ByteBufCodecs.list()), WeaponData::passives,
            TempEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), WeaponData::temps,
            WeaponData::new
    );

    /** Короткий конструктор без эффектов (его использует лут структур) */
    public WeaponData(long points, long kills, int luckBonus, long enchantPoints) {
        this(points, kills, luckBonus, enchantPoints, List.of(), List.of());
    }

    public int rank() {
        return RankPoints.rankFor(points);
    }
    /** Сколько видов постоянных улучшений уже занято */
    public int usedSlots() {
        return passives.size() + (luckBonus > 0 ? 1 : 0);
    }

    /** Сколько видов улучшений позволяет ранг: F = 0, E = 1, D = 2, ... */
    public int maxSlots() {
        return Math.max(0, rank() + 1);
    }

    public WeaponData addPoints(long amount) {
        return new WeaponData(Math.max(RankPoints.MIN_POINTS, points + amount), kills, luckBonus, enchantPoints, passives, temps);
    }

    public WeaponData addKill(boolean isBoss) {
        long gain = isBoss ? RankPoints.BOSS_POINTS : RankPoints.KILL_POINTS;
        return new WeaponData(points + gain, kills + 1, luckBonus, enchantPoints, passives, temps);
    }

    public WeaponData withRank(int rank) {
        return new WeaponData(RankPoints.threshold(Math.max(-1, rank)), kills, luckBonus, enchantPoints, passives, temps);
    }

    public WeaponData withLuckBonus(int bonus) {
        return new WeaponData(points, kills, Math.max(0, bonus), enchantPoints, passives, temps);
    }

    /** Меняет засчитанные очки за чары и сдвигает общие очки на разницу */
    public WeaponData withEnchantPoints(long newValue) {
        long delta = newValue - enchantPoints;
        return new WeaponData(Math.max(RankPoints.MIN_POINTS, points + delta), kills, luckBonus, newValue, passives, temps);
    }

    // ---------- пассивные эффекты ----------

    public int passiveLevel(String id) {
        for (Passive p : passives) if (p.id().equals(id)) return p.level();
        return 0;
    }

    public WeaponData withPassive(String id, int level) {
        List<Passive> out = new ArrayList<>();
        boolean found = false;
        for (Passive p : passives) {
            if (p.id().equals(id)) { out.add(new Passive(id, level)); found = true; }
            else out.add(p);
        }
        if (!found) out.add(new Passive(id, level));
        return new WeaponData(points, kills, luckBonus, enchantPoints, List.copyOf(out), temps);
    }

    // ---------- временные эффекты ----------

    public TempEffect temp(String id) {
        for (TempEffect t : temps) if (t.id().equals(id)) return t;
        return null;
    }

    /** Добавляет или продлевает эффект. Возвращает null, если слотов нет. */
    public WeaponData withTemp(String id, int level, long ticks) {
        List<TempEffect> out = new ArrayList<>();
        boolean found = false;
        for (TempEffect t : temps) {
            if (t.id().equals(id)) {
                out.add(new TempEffect(id, Math.max(t.level(), level),
                        Math.min(t.pending() + ticks, MAX_TEMP_TICKS), t.expiresAt()));
                found = true;
            } else out.add(t);
        }
        if (!found) {
            if (out.size() >= WeaponUpgrades.MAX_TEMPS) return null;
            out.add(new TempEffect(id, level, Math.min(ticks, MAX_TEMP_TICKS), 0L));
        }
        return new WeaponData(points, kills, luckBonus, enchantPoints, passives, List.copyOf(out));
    }

    public static long remaining(TempEffect t, long now) {
        return t.pending() + Math.max(0, t.expiresAt() - now);
    }

    /** Вшивает отложенное время в таймеры и убирает истёкшие */
    public WeaponData normalizeTemps(long now) {
        List<TempEffect> out = new ArrayList<>();
        for (TempEffect t : temps) {
            long exp = t.expiresAt();
            if (t.pending() > 0) {
                exp = Math.min(Math.max(now, exp) + t.pending(), now + MAX_TEMP_TICKS);
            }
            if (exp > now) out.add(new TempEffect(t.id(), t.level(), 0L, exp));
        }
        if (out.equals(temps)) return this;
        return new WeaponData(points, kills, luckBonus, enchantPoints, passives, List.copyOf(out));
    }

    /** Удача оружия: постоянный бонус плюс временная «Фортуна» */
    public int effectiveLuck(long now) {
        int total = luckBonus;
        for (TempEffect t : temps) {
            if (t.id().equals("luck") && remaining(t, now) > 0) total += 2 * t.level();
        }
        return total;
    }
}