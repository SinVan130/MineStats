package com.example.examplemod.mixin;

import com.example.examplemod.ManaUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

    // Проверка «уровень >= ступень лазурита» теперь проверяет ману: мана / 10
    @Redirect(method = "clickMenuButton",
            at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, ordinal = 0,
                    target = "Lnet/minecraft/world/entity/player/Player;experienceLevel:I"))
    private int minestats$tierMana(Player player) {
        return ManaUtil.mana(player) / ManaUtil.SLOT_MANA;
    }

    // Проверка «уровень >= требование слота» теперь проверяет ману: мана / 2
    @Redirect(method = "clickMenuButton",
            at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, ordinal = 1,
                    target = "Lnet/minecraft/world/entity/player/Player;experienceLevel:I"))
    private int minestats$requirementMana(Player player) {
        return ManaUtil.mana(player) / ManaUtil.LEVEL_MANA;
    }

    // Сообщение над хотбаром, если не хватает маны (только на клиенте)
    @Inject(method = "clickMenuButton", at = @At("HEAD"))
    private void minestats$notifyNoMana(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!player.level().isClientSide || player.getAbilities().instabuild) return;
        EnchantmentMenu self = (EnchantmentMenu) (Object) this;
        if (id < 0 || id > 2 || self.costs[id] <= 0 || self.getSlot(0).getItem().isEmpty()) return;
        int need = Math.max(self.costs[id] * ManaUtil.LEVEL_MANA, (id + 1) * ManaUtil.SLOT_MANA);
        int have = ManaUtil.mana(player);
        if (have < need) {
            player.displayClientMessage(
                    ManaUtil.message("not_enough_mana", ManaUtil.WARN_COLOR, need, have), true);
        }
    }
}