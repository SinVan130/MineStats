package com.example.examplemod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class DurabilityEvents {

    private static final String SLOT_TAG = "minestats_f_slot";
    private static final String LAST_TAG = "minestats_f_last";

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData();
        ItemStack stack = player.getMainHandItem();

        boolean isF = WeaponUtil.isLevelable(stack) && stack.isDamageableItem()
                && stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT).rank() < 0;
        if (!isF) {
            data.putInt(LAST_TAG, -1);
            return;
        }

        int slot = player.getInventory().selected;
        int damage = stack.getDamageValue();
        int last = data.contains(LAST_TAG) ? data.getInt(LAST_TAG) : -1;

        if (last >= 0 && data.getInt(SLOT_TAG) == slot && damage > last) {
            stack.hurtAndBreak(damage - last, player, EquipmentSlot.MAINHAND);
            damage = stack.isEmpty() ? -1 : stack.getDamageValue();
        }
        data.putInt(SLOT_TAG, slot);
        data.putInt(LAST_TAG, damage);
    }
}