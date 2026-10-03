package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ExampleMod.MODID);

    public static final Supplier<MapCodec<StructureWeaponModifier>> STRUCTURE_WEAPONS =
            MODIFIERS.register("structure_weapons", () -> StructureWeaponModifier.CODEC);

    public static void register(IEventBus bus) {
        MODIFIERS.register(bus);
    }
}