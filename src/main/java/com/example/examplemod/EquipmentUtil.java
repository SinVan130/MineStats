package com.example.examplemod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class EquipmentUtil {

    private static final ResourceLocation ARMOR_RANK_ID =
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "armor_rank");

    public static boolean isArmor(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    public static void init(ItemStack stack) {
        if (!isArmor(stack)) return;

        if (!stack.has(ModDataComponents.EQUIPMENT_DATA.get())) {
            stack.set(ModDataComponents.EQUIPMENT_DATA.get(), EquipmentData.DEFAULT);
        }

        updateAttributes(stack);
    }

    public static void updateAttributes(ItemStack stack) {
        if (!isArmor(stack)) return;

        EquipmentData data = stack.getOrDefault(
                ModDataComponents.EQUIPMENT_DATA.get(), EquipmentData.DEFAULT);

        int armorBonus = Math.max(0, data.rank());

        ItemAttributeModifiers current = stack.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        // Есть ли в компоненте что-то кроме нашего модификатора (родная защита и т.д.)
        boolean hasNative = false;
        double existing = 0;
        for (ItemAttributeModifiers.Entry e : current.modifiers()) {
            if (e.modifier().id().equals(ARMOR_RANK_ID)) existing = e.modifier().amount();
            else hasNative = true;
        }

        // Ничего не менялось: выходим, чтобы не пересобирать компонент каждый тик
        if (existing == armorBonus && (hasNative || armorBonus == 0)) return;

        // Если родных модификаторов в компоненте нет, берём их у самого предмета
        ItemAttributeModifiers base = hasNative
                ? current
                : stack.getItem().getDefaultAttributeModifiers();

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        for (ItemAttributeModifiers.Entry e : base.modifiers()) {
            if (!e.modifier().id().equals(ARMOR_RANK_ID)) {
                builder.add(e.attribute(), e.modifier(), e.slot());
            }
        }

        if (armorBonus > 0) {
            builder.add(
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ARMOR_RANK_ID,
                            armorBonus,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    slotGroup(stack)
            );
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static EquipmentSlotGroup slotGroup(ItemStack stack) {
        EquipmentSlot slot = ((ArmorItem) stack.getItem()).getEquipmentSlot();

        return switch (slot) {
            case HEAD -> EquipmentSlotGroup.HEAD;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case FEET -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.ANY;
        };
    }
}