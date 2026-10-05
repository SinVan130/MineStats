package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class StatEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) StatEffects.apply(p);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) StatEffects.apply(p);
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || p.tickCount % 20 != 0) return;
        StatEffects.apply(p);
        PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);
        if (s.mana() < s.maxMana()) {
            p.setData(ModAttachments.PLAYER_STATS, s.withMana(s.mana() + s.manaRegen()));
        }
    }
}