package com.example.examplemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ExampleMod.MODID);

    public static final Supplier<RecipeSerializer<WeaponUpgradeRecipe>> WEAPON_UPGRADE =
            SERIALIZERS.register("weapon_upgrade", WeaponUpgradeRecipe.Serializer::new);

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}