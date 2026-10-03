package com.example.examplemod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class SwordCommands {

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("swordluck")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("value", IntegerArgumentType.integer(0, 1000))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            ItemStack stack = p.getMainHandItem();
                            if (!WeaponUtil.isLevelable(stack)) return 0;
                            WeaponData d = stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);
                            stack.set(ModDataComponents.WEAPON_DATA.get(),
                                    d.withLuckBonus(IntegerArgumentType.getInteger(ctx, "value")));
                            return 1;
                        })));

        event.getDispatcher().register(Commands.literal("swordrank")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("value", IntegerArgumentType.integer(-1, 200))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            ItemStack stack = p.getMainHandItem();
                            if (!WeaponUtil.isLevelable(stack)) return 0;
                            WeaponData d = stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);
                            stack.set(ModDataComponents.WEAPON_DATA.get(),
                                    d.withRank(IntegerArgumentType.getInteger(ctx, "value")));
                            WeaponUtil.updateAttributes(stack);
                            return 1;
                        })));
        event.getDispatcher().register(Commands.literal("swordpoints")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("value", com.mojang.brigadier.arguments.LongArgumentType.longArg(0, 1000000000L))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            ItemStack stack = p.getMainHandItem();
                            if (!WeaponUtil.isLevelable(stack)) return 0;
                            WeaponData d = stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);
                            WeaponData next = d.addPoints(com.mojang.brigadier.arguments.LongArgumentType.getLong(ctx, "value"));
                            stack.set(ModDataComponents.WEAPON_DATA.get(), next);
                            WeaponUtil.updateAttributes(stack);
                            return 1;
                        })));
    }
}