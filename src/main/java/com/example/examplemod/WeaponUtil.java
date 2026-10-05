package com.example.examplemod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class WeaponUtil {

    private static final ResourceLocation BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "level_bonus");
    private static final ResourceLocation SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "passive_speed");
    private static final ResourceLocation REACH_ID =
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "passive_reach");

    public static boolean isLevelable(ItemStack stack) {
        return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES);
    }

    /** Пересчитывает наши модификаторы. Родные характеристики оружия сохраняются. */
    public static void updateAttributes(ItemStack stack) {
        WeaponData data = stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);
        double damage = WeaponRarity.damageBonus(data.rank()) + 0.5 * data.passiveLevel("might");
        double speed = 0.025 * data.passiveLevel("attack_speed");
        double reach = 0.25 * data.passiveLevel("reach");
        if (data.rank() >= WeaponRarity.S_RANK) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }

        ItemAttributeModifiers current = stack.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry e : current.modifiers()) {
            ResourceLocation id = e.modifier().id();
            if (!id.equals(BONUS_ID) && !id.equals(SPEED_ID) && !id.equals(REACH_ID)) {
                builder.add(e.attribute(), e.modifier(), e.slot());
            }
        }
        if (damage > 0) {
            builder.add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(BONUS_ID, damage, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        if (speed > 0) {
            builder.add(Attributes.ATTACK_SPEED,
                    new AttributeModifier(SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        if (reach > 0) {
            builder.add(Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(REACH_ID, reach, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }
}