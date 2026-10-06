package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class EquipmentEvents {

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;

        for (ItemStack stack : p.getInventory().items) {
            EquipmentUtil.init(stack);
        }

        for (ItemStack stack : p.getInventory().armor) {
            EquipmentUtil.init(stack);
        }

        EquipmentUtil.init(p.getMainHandItem());
        EquipmentUtil.init(p.getOffhandItem());
    }
}