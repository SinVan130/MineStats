package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class ModNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(LevelUpPayload.TYPE, LevelUpPayload.CODEC, (payload, ctx) ->
                ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer p) StatActions.levelUp(p, payload.stat());
                }));

        registrar.playToServer(SelectTitlePayload.TYPE, SelectTitlePayload.CODEC, (payload, ctx) ->
                ctx.enqueueWork(() -> {
                    if (!(ctx.player() instanceof ServerPlayer p)) return;
                    String id = payload.id();
                    if (id.isEmpty()) {
                        TitleEvents.select(p, "");
                    } else if (Titles.get(id) != null && p.getData(ModAttachments.TITLES).has(id)) {
                        TitleEvents.select(p, id);
                    }
                }));
    }
}