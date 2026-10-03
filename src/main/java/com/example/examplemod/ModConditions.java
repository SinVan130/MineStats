package com.example.examplemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModConditions {
    public static final DeferredRegister<LootItemConditionType> CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, ExampleMod.MODID);

    public static final Supplier<LootItemConditionType> PROC =
            CONDITIONS.register("proc", () -> new LootItemConditionType(ProcCondition.CODEC));

    public static void register(IEventBus bus) {
        CONDITIONS.register(bus);
    }
}