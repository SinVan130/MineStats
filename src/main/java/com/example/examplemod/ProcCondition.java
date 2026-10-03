package com.example.examplemod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record ProcCondition(String effectId, double chance) implements LootItemCondition {

    public static final MapCodec<ProcCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("effect").forGetter(ProcCondition::effectId),
            Codec.DOUBLE.fieldOf("chance").forGetter(ProcCondition::chance)
    ).apply(i, ProcCondition::new));

    @Override
    public LootItemConditionType getType() {
        return ModConditions.PROC.get();
    }

    @Override
    public boolean test(LootContext ctx) {
        Entity attacker = ctx.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
        if (attacker == null) attacker = ctx.getParamOrNull(LootContextParams.THIS_ENTITY);
        // Не игрок (мобы и т.д.): ванильное поведение без шансов
        if (!(attacker instanceof Player player)) return true;
        return ProcSystem.roll(player, effectId, chance);
    }
}