package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    public static final KeyMapping OPEN_STATUS = new KeyMapping(
            "key.minestats.status", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.minestats");

    @SubscribeEvent
    public static void onKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STATUS);
    }
}