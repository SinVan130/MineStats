package com.example.examplemod.client;

import com.example.examplemod.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ClientGameEvents {

    private static final String[] ROMAN = {"I", "II", "III"};

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (ClientModEvents.OPEN_STATUS.consumeClick()) {
            if (mc.player != null && mc.screen == null) mc.setScreen(new StatusScreen());
        }
    }

    /** Панель маны справа от стола зачарований */

    }