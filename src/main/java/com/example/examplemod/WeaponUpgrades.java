package com.example.examplemod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.Map;

public class WeaponUpgrades {

    public record Material(String id, int unit, int maxLevel) {}
    public record Plan(WeaponData data, int cost, long points) {}

    public static final int MAX_LUCK_BONUS = 10;
    public static final int MAX_TEMPS = 5;
    private static final long PASSIVE_POINTS = 30;
    private static final long POTION_POINTS = 15;

    private static final Map<Item, Material> MATERIALS = Map.of(
            Items.REDSTONE, new Material("attack_speed", 8, 5),
            Items.DIAMOND, new Material("crit", 8, 5),
            Items.IRON_INGOT, new Material("reach", 8, 5),
            Items.GOLD_INGOT, new Material("lifesteal", 8, 5),
            Items.NETHERITE_SCRAP, new Material("might", 2, 5),
            Items.EMERALD, new Material("luck", 8, MAX_LUCK_BONUS)
    );

    public static Material materialFor(ItemStack stack) {
        return MATERIALS.get(stack.getItem());
    }

    private static boolean isPotionItem(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

    /** Зелье с хотя бы одним эффектом, который умеет оружие */
    public static boolean isUsablePotion(ItemStack stack) {
        if (!isPotionItem(stack)) return false;
        PotionContents pc = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        for (MobEffectInstance inst : pc.getAllEffects()) {
            if (tempId(inst) != null) return true;
        }
        return false;
    }

    public static boolean isAddition(ItemStack stack) {
        return materialFor(stack) != null || isUsablePotion(stack);
    }

    private static String tempId(MobEffectInstance inst) {
        var key = inst.getEffect().unwrapKey();
        if (key.isEmpty() || !key.get().location().getNamespace().equals("minecraft")) return null;
        return switch (key.get().location().getPath()) {
            case "fire_resistance" -> "ignite";
            case "slow_falling" -> "levitation";
            case "poison" -> "poison";
            case "weakness" -> "weakness";
            case "slowness" -> "slowness";
            case "night_vision" -> "glowing";
            case "strength" -> "strength";
            case "regeneration" -> "regeneration";
            case "luck" -> "luck";
            default -> null;
        };
    }

    /** Что получится, если положить addition на base. null, если улучшение невозможно. */
    public static Plan plan(ItemStack base, ItemStack addition) {
        if (!WeaponUtil.isLevelable(base) || addition.isEmpty()) return null;
        WeaponData d = base.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);

        Material m = materialFor(addition);
        if (m != null) {
            if (m.id().equals("luck")) {
                if (d.luckBonus() >= m.maxLevel() || addition.getCount() < m.unit()) return null;
                if (d.luckBonus() == 0 && d.usedSlots() >= d.maxSlots()) return null;
                return new Plan(d.withLuckBonus(d.luckBonus() + 1), m.unit(), PASSIVE_POINTS);
            }
            int level = d.passiveLevel(m.id());
            if (level == 0 && d.usedSlots() >= d.maxSlots()) return null;
            if (level >= m.maxLevel()) return null;
            int next = level + 1;
            int cost = m.unit() * next;
            if (addition.getCount() < cost) return null;
            return new Plan(d.withPassive(m.id(), next), cost, PASSIVE_POINTS * next);
        }

        if (isPotionItem(addition)) {
            PotionContents pc = addition.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            WeaponData cur = d;
            int count = 0;
            for (MobEffectInstance inst : pc.getAllEffects()) {
                String id = tempId(inst);
                if (id == null) continue;
                WeaponData next = cur.withTemp(id, inst.getAmplifier() + 1, (long) inst.getDuration() * 2);
                if (next == null) return null;
                cur = next;
                count++;
            }
            if (count == 0) return null;
            return new Plan(cur, 1, POTION_POINTS * count);
        }
        return null;
    }

    public static ItemStack apply(ItemStack base, ItemStack addition) {
        Plan p = plan(base, addition);
        if (p == null) return ItemStack.EMPTY;
        ItemStack result = base.copy();
        result.setCount(1);
        result.set(ModDataComponents.WEAPON_DATA.get(), p.data().addPoints(p.points()));
        WeaponUtil.updateAttributes(result);
        return result;
    }

    public static int totalCost(ItemStack base, ItemStack addition) {
        Plan p = plan(base, addition);
        return p == null ? 0 : p.cost();
    }
}