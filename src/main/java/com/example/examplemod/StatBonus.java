package com.example.examplemod;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Скрытые бонусы к статам из всех источников: броня, бижутерия, титулы. */
public class StatBonus {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    /** Суммарный бонус к стату. Работает и на клиенте, и на сервере. */
    public static int get(Player p, Stat stat) {
        if (p == null) return 0;
        return fromArmor(p, stat)
                + fromJewelry(p, stat)
                + fromTitle(p, stat);
    }

    /** Основной стат + бонус */
    public static int effective(Player p, PlayerStats base, Stat stat) {
        return base.get(stat) + get(p, stat);
    }

    private static int fromArmor(Player p, Stat stat) {
        int sum = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack s = p.getItemBySlot(slot);
            if (!EquipmentUtil.isArmor(s)) continue;
            EquipmentData d = s.getOrDefault(
                    ModDataComponents.EQUIPMENT_DATA.get(), EquipmentData.DEFAULT);
            sum += d.get(stat);
        }
        return sum;
    }

    private static int fromJewelry(Player p, Stat stat) {
        return 0; // TODO: когда появится бижутерия
    }

    private static int fromTitle(Player p, Stat stat) {
        return 0; // TODO: когда титулы начнут давать статы
    }
}