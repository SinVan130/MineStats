package com.example.examplemod;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.function.Consumer;
import java.util.function.Predicate;

/** Бонус титула. Свой уникальный бонус можно написать так: new Fx() { переопределить нужные методы } */
public interface Fx {

    /** Игрок наносит урон */
    default float dealt(ServerPlayer player, LivingEntity target, DamageSource source, float amount) {
        return amount;
    }

    /** Игрок получает урон */
    default float taken(ServerPlayer player, DamageSource source, float amount) {
        return amount;
    }

    /** Игрок убил моба */
    default void onKill(ServerPlayer player, LivingEntity victim) {
    }

    /** Раз в секунду, пока титул выбран */
    default void onSecond(ServerPlayer player) {
    }

    /** Бонус к общей удаче */
    default int luckBonus() {
        return 0;
    }

    /** Атрибут (здоровье, скорость...). Обрабатывается отдельно при выборе титула */
    record Attr(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) implements Fx {
    }

    static Fx attribute(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        return new Attr(attribute, amount, operation);
    }

    /** +bonus к урону, когда выполняется условие (0.2 = +20%) */
    static Fx damageWhen(Predicate<ServerPlayer> condition, double bonus) {
        return new Fx() {
            @Override
            public float dealt(ServerPlayer player, LivingEntity target, DamageSource source, float amount) {
                return condition.test(player) ? (float) (amount * (1.0 + bonus)) : amount;
            }
        };
    }

    /** +bonus к урону всегда */
    static Fx damage(double bonus) {
        return damageWhen(p -> true, bonus);
    }

    /** +bonus к урону по мобам из тега */
    static Fx damageVs(TagKey<EntityType<?>> tag, double bonus) {
        return new Fx() {
            @Override
            public float dealt(ServerPlayer player, LivingEntity target, DamageSource source, float amount) {
                return target.getType().is(tag) ? (float) (amount * (1.0 + bonus)) : amount;
            }
        };
    }

    /** Меньше урона от конкретного типа (0.5 = на 50% меньше) */
    static Fx resist(ResourceKey<DamageType> type, double reduction) {
        return new Fx() {
            @Override
            public float taken(ServerPlayer player, DamageSource source, float amount) {
                return source.is(type) ? (float) (amount * (1.0 - reduction)) : amount;
            }
        };
    }

    /** Меньше урона от группы типов (огонь, взрывы, снаряды) */
    static Fx resist(TagKey<DamageType> tag, double reduction) {
        return new Fx() {
            @Override
            public float taken(ServerPlayer player, DamageSource source, float amount) {
                return source.is(tag) ? (float) (amount * (1.0 - reduction)) : amount;
            }
        };
    }

    static Fx luck(int amount) {
        return new Fx() {
            @Override
            public int luckBonus() {
                return amount;
            }
        };
    }

    static Fx healOnKill(float hp) {
        return new Fx() {
            @Override
            public void onKill(ServerPlayer player, LivingEntity victim) {
                player.heal(hp);
            }
        };
    }

    static Fx manaOnKill(int mana) {
        return new Fx() {
            @Override
            public void onKill(ServerPlayer player, LivingEntity victim) {
                ManaUtil.add(player, mana);
            }
        };
    }

    static Fx manaRegen(int perSecond) {
        return new Fx() {
            @Override
            public void onSecond(ServerPlayer player) {
                ManaUtil.add(player, perSecond);
            }
        };
    }

    /** При убийстве с шансом (через общую систему удачи и псевдоудачи) выполняет действие */
    static Fx onKillProc(String effectId, double chance, Consumer<ServerPlayer> action) {
        return new Fx() {
            @Override
            public void onKill(ServerPlayer player, LivingEntity victim) {
                if (ProcSystem.roll(player, effectId, chance)) action.accept(player);
            }
        };
    }
}