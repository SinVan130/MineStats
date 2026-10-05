package com.example.examplemod.mixin;

import com.example.examplemod.ManaUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "onEnchantmentPerformed", at = @At("HEAD"), cancellable = true)
    private void minestats$payWithMana(ItemStack stack, int levelCost, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        ci.cancel(); // уровни опыта не списываем
        if (self.getAbilities().instabuild) return;
        if (self instanceof ServerPlayer sp) {
            int cost = levelCost * ManaUtil.SLOT_MANA;
            ManaUtil.spend(sp, cost);
            sp.displayClientMessage(ManaUtil.message("mana_spent", ManaUtil.MANA_COLOR, cost), true);
        }
    }
}