package com.example.examplemod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class WeaponUtil {

    private static final ResourceLocation BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "level_bonus");

    public static boolean isLevelable(ItemStack stack) {
        return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES);
    }

    /** Пересчитывает бонус к урону по уровню меча. Родные характеристики меча сохраняются. */
    public static void updateAttributes(ItemStack stack) {
        WeaponData data = stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);
        double bonus = WeaponRarity.damageBonus(data.rank());

        ItemAttributeModifiers current = stack.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry e : current.modifiers()) {
            if (!e.modifier().id().equals(BONUS_ID)) {
                builder.add(e.attribute(), e.modifier(), e.slot());
            }
        }
        if (bonus > 0) {
            builder.add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(BONUS_ID, bonus, AttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }
}