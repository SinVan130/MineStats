package com.example.examplemod;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LuckUtil {

    public static final double BASE_PROC = 0.10;
    public static final int PLAYER_BASE_LUCK = 1;

    /** Бонус удачи от меча (0 по умолчанию) */
    public static int swordBonus(ItemStack stack) {
        if (!WeaponUtil.isLevelable(stack)) return 0;
        return stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT).luckBonus();
    }

    /** Полная удача игрока. Позже сюда добавится стат «Удача». */
    public static int getLuck(Player player) {
        return PLAYER_BASE_LUCK + swordBonus(player.getMainHandItem());
    }
}