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
    @SubscribeEvent
    public static void onEnchantScreen(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof EnchantmentScreen screen)) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return;

        GuiGraphics g = event.getGuiGraphics();
        PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);
        int x = screen.getGuiLeft() + screen.getXSize() + 6;
        int y = screen.getGuiTop();
        int w = 96;

        Ui.panel(g, x, y, w, 84);
        g.drawString(mc.font, Component.translatable("gui." + ExampleMod.MODID + ".enchant.title"),
                x + 8, y + 6, Ui.MANA, false);
        String mana = s.mana() + "/" + s.maxMana();
        g.drawString(mc.font, mana, x + w - 8 - mc.font.width(mana), y + 6, Ui.TEXT, false);
        Ui.bar(g, x + 8, y + 18, w - 16, 6, (float) s.mana() / Math.max(1, s.maxMana()), Ui.MANA);

        if (p.getAbilities().instabuild) {
            g.drawString(mc.font, Component.translatable("gui." + ExampleMod.MODID + ".enchant.free"),
                    x + 8, y + 32, Ui.GOOD, false);
            return;
        }

        for (int i = 0; i < 3; i++) {
            int req = screen.getMenu().costs[i];
            int ry = y + 32 + i * 13;
            if (req <= 0) {
                g.drawString(mc.font, ROMAN[i] + ": -", x + 8, ry, Ui.DIM, false);
                continue;
            }
            int price = (i + 1) * ManaUtil.SLOT_MANA;
            int need = Math.max(price, req * ManaUtil.LEVEL_MANA);
            boolean ok = s.mana() >= need;
            g.drawString(mc.font, ROMAN[i] + ": -" + price + " / " + need, x + 8, ry,
                    ok ? Ui.GOOD : Ui.BAD, false);
        }
        g.drawString(mc.font, Component.translatable("gui." + ExampleMod.MODID + ".enchant.legend"),
                x + 8, y + 72, Ui.DIM, false);
    }
}