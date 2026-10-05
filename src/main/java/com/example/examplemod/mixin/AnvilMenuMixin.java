package com.example.examplemod.mixin;

import com.example.examplemod.ManaUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @Shadow @Final private DataSlot cost;

    // Лимит «Слишком дорого»: порог 40 становится недостижимым
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40))
    private int minestats$noLimit(int original) {
        return Integer.MAX_VALUE;
    }

    // Удваиваем итоговую стоимость: теперь она равна цене в мане
    @Inject(method = "createResult", at = @At("TAIL"))
    private void minestats$doubleCost(CallbackInfo ci) {
        int c = cost.get();
        if (c > 0) cost.set(c * ManaUtil.ANVIL_COST_MULTIPLIER);
    }

    // Можно ли забрать результат: хватает ли маны вместо уровней опыта
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void minestats$manaPickup(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
        int c = cost.get();
        boolean affordable = player.getAbilities().instabuild
                || ManaUtil.mana(player) >= c * ManaUtil.ANVIL_MANA;
        cir.setReturnValue(affordable && c > 0);
    }

    // Списание: вместо уровней опыта списываем ману
    @Redirect(method = "onTake",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"))
    private void minestats$payMana(Player player, int levels) {
        if (player instanceof ServerPlayer sp) {
            int spent = -levels * ManaUtil.ANVIL_MANA;
            ManaUtil.spend(sp, spent);
            sp.displayClientMessage(ManaUtil.message("mana_spent", ManaUtil.MANA_COLOR, spent), true);
        }
    }
}