package com.example.examplemod;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class ClassCommands {

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("class")
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    String cls = ClassUtil.get(p);
                    PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);
                    Component name = cls.isEmpty()
                            ? Component.translatable("class." + ExampleMod.MODID + ".none")
                            : ClassUtil.displayName(cls);
                    p.sendSystemMessage(Component.translatable("class." + ExampleMod.MODID + ".info",
                            name, s.level(), ClassUtil.skillPoints(s), ClassUtil.MAX_POINTS));
                    // отладочная строка: убрать, когда проверка пройдёт
                    p.sendSystemMessage(Component.literal("debug oxygen_bonus = "
                            + p.getAttributeValue(Attributes.OXYGEN_BONUS)));
                    return 1;
                }));
    }
}