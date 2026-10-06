package com.example.examplemod;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public class EquipmentUpgrades {

    public record Material(String id, Stat stat, int unit, int maxLevel) {}
    public record Plan(EquipmentData data, int cost, long points) {}

    public static final int MAX_STAT = 5;
    private static final long STAT_POINTS = 50;

    private static final Map<Item, Material> MATERIALS = Map.of(
            Items.LAPIS_LAZULI, new Material("intelligence", Stat.INTELLIGENCE, 8, MAX_STAT),
            Items.IRON_INGOT, new Material("strength", Stat.STRENGTH, 8, MAX_STAT),
            Items.EMERALD, new Material("luck", Stat.LUCK, 8, MAX_STAT),
            Items.GOLD_INGOT, new Material("dexterity", Stat.DEXTERITY, 8, MAX_STAT),
            Items.NETHERITE_SCRAP, new Material("vitality", Stat.VITALITY, 2, MAX_STAT)
    );

    public static Material materialFor(ItemStack stack) {
        return MATERIALS.get(stack.getItem());
    }

    public static boolean isUsable(ItemStack stack) {
        return materialFor(stack) != null;
    }

    public static Plan plan(ItemStack base, ItemStack addition) {
        if (!EquipmentUtil.isArmor(base) || addition.isEmpty()) return null;

        Material m = materialFor(addition);
        if (m == null) return null;

        EquipmentData d = base.getOrDefault(
                ModDataComponents.EQUIPMENT_DATA.get(), EquipmentData.DEFAULT);

        int level = d.get(m.stat());

        if (level >= m.maxLevel()) return null;
        if (level == 0 && d.usedSlots() >= d.maxSlots()) return null;

        int next = level + 1;
        int cost = m.unit() * next;

        if (addition.getCount() < cost) return null;

        return new Plan(
                d.withStat(m.stat(), next),
                cost,
                STAT_POINTS * next
        );
    }


    public static ItemStack apply(ItemStack base, ItemStack addition) {
        Plan p = plan(base, addition);
        if (p == null) return ItemStack.EMPTY;

        ItemStack result = base.copy();
        result.setCount(1);
        result.set(ModDataComponents.EQUIPMENT_DATA.get(), p.data().addPoints(p.points()));
        EquipmentUtil.updateAttributes(result);

        return result; // ничего не списываем, это делает SmithingMenuMixin.onTake
    }

    public static int totalCost(ItemStack base, ItemStack addition) {
        Plan p = plan(base, addition);
        return p == null ? 0 : p.cost();
    }
}