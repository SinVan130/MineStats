package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class WeaponUpgradeRecipe implements SmithingRecipe {

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.isEmpty(); // шаблон не нужен
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return WeaponUtil.isLevelable(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return WeaponUpgrades.isAddition(stack);
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return input.template().isEmpty() && WeaponUpgrades.plan(input.base(), input.addition()) != null;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        return WeaponUpgrades.apply(input.base(), input.addition());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.WEAPON_UPGRADE.get();
    }

    public static class Serializer implements RecipeSerializer<WeaponUpgradeRecipe> {
        private static final MapCodec<WeaponUpgradeRecipe> CODEC = MapCodec.unit(WeaponUpgradeRecipe::new);
        private static final StreamCodec<RegistryFriendlyByteBuf, WeaponUpgradeRecipe> STREAM =
                StreamCodec.of((buf, recipe) -> { }, buf -> new WeaponUpgradeRecipe());

        @Override
        public MapCodec<WeaponUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WeaponUpgradeRecipe> streamCodec() {
            return STREAM;
        }
    }
}