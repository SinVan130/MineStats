package com.example.examplemod;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ExampleMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponData>> WEAPON_DATA =
            DATA_COMPONENTS.register("weapon_data", () -> DataComponentType.<WeaponData>builder()
                    .persistent(WeaponData.CODEC)
                    .networkSynchronized(WeaponData.STREAM_CODEC)
                    .build());

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
