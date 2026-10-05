package com.example.examplemod;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LuckUtil {

    public static final double BASE_PROC = 0.10;
    public static final int PLAYER_BASE_LUCK = 1;

    /** Бонус удачи от оружия в руке: постоянный плюс временная «Фортуна» */
    public static int swordBonus(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!WeaponUtil.isLevelable(stack)) return 0;
        return stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT)
                .effectiveLuck(player.level().getGameTime());
    }

    /** Полная удача игрока. Позже сюда добавится стат «Удача». */
    public static int getLuck(Player player) {
        int stat = player.getData(ModAttachments.PLAYER_STATS).luck() - PlayerStats.BASE;
        return Math.max(1, PLAYER_BASE_LUCK + stat + swordBonus(player) + TitleEvents.luckBonus(player));
    }
}