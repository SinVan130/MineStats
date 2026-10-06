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

public class EquipmentUpgradeRecipe implements SmithingRecipe {

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return EquipmentUtil.isArmor(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return EquipmentUpgrades.isUsable(stack);
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return input.template().isEmpty() && EquipmentUpgrades.plan(input.base(), input.addition()) != null;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        return EquipmentUpgrades.apply(input.base(), input.addition());
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
        return ModRecipes.EQUIPMENT_UPGRADE.get();
    }

    public static class Serializer implements RecipeSerializer<EquipmentUpgradeRecipe> {

        private static final MapCodec<EquipmentUpgradeRecipe> CODEC =
                MapCodec.unit(EquipmentUpgradeRecipe::new);

        private static final StreamCodec<RegistryFriendlyByteBuf, EquipmentUpgradeRecipe> STREAM =
                StreamCodec.of((buf, recipe) -> { }, buf -> new EquipmentUpgradeRecipe());

        @Override
        public MapCodec<EquipmentUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EquipmentUpgradeRecipe> streamCodec() {
            return STREAM;
        }
    }
}