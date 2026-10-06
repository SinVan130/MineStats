package com.example.examplemod.mixin;

import com.example.examplemod.EquipmentUpgrades;
import com.example.examplemod.EquipmentUtil;
import com.example.examplemod.WeaponUpgrades;
import com.example.examplemod.WeaponUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void minestats$consumeExtra(Player player, ItemStack stack, CallbackInfo ci) {
        SmithingMenu self = (SmithingMenu) (Object) this;

        if (!self.getSlot(0).getItem().isEmpty()) return;

        ItemStack base = self.getSlot(1).getItem();
        ItemStack addition = self.getSlot(2).getItem();

        int extra;

        if (WeaponUtil.isLevelable(base) && WeaponUpgrades.isAddition(addition)) {
            extra = WeaponUpgrades.totalCost(base, addition) - 1;
        } else if (EquipmentUtil.isArmor(base) && EquipmentUpgrades.isUsable(addition)) {
            extra = EquipmentUpgrades.totalCost(base, addition) - 1;
        } else {
            return;
        }

        if (extra > 0) addition.shrink(extra);
    }
}