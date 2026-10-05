package com.example.examplemod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class StatsCommands {

    private static final int LABEL = 0x8E9AAF;
    private static final int VALUE = 0xF2F4F8;
    private static final int GOLD = 0xFFC83D;
    private static final int MANA = 0x60A5FA;

    private static Style color(int rgb) {
        return Style.EMPTY.withColor(TextColor.fromRgb(rgb));
    }

    private static String key(String name) {
        return "stats." + ExampleMod.MODID + "." + name;
    }

    private static CompletableFuture<Suggestions> suggestStats(CommandContext<CommandSourceStack> c, SuggestionsBuilder b) {
        return SharedSuggestionProvider.suggest(Arrays.stream(Stat.values()).map(s -> s.id), b);
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("stats")
                .executes(ctx -> show(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("levelup")
                        .then(Commands.argument("stat", StringArgumentType.word())
                                .suggests(StatsCommands::suggestStats)
                                .executes(ctx -> levelUp(ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "stat")))))
                .then(Commands.literal("set")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("stat", StringArgumentType.word())
                                .suggests(StatsCommands::suggestStats)
                                .then(Commands.argument("value", IntegerArgumentType.integer(1, PlayerStats.MAX_STAT))
                                        .executes(ctx -> set(ctx.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(ctx, "stat"),
                                                IntegerArgumentType.getInteger(ctx, "value")))))));
    }

    private static int show(ServerPlayer p) {
        PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);
        p.sendSystemMessage(Component.translatable(key("header"),
                        Component.literal(String.valueOf(s.level())).withStyle(color(GOLD)),
                        Component.literal(s.mana() + "/" + s.maxMana()).withStyle(color(MANA)),
                        Component.literal(String.valueOf(s.levelUpCost())).withStyle(color(VALUE)))
                .withStyle(color(LABEL)));
        for (Stat stat : Stat.values()) {
            p.sendSystemMessage(stat.displayName()
                    .append(Component.literal("  " + s.get(stat)).withStyle(color(VALUE))));
        }
        return 1;
    }

    private static int levelUp(ServerPlayer p, String id) {
        Stat stat = Stat.byId(id);
        if (stat == null) {
            p.sendSystemMessage(Component.translatable(key("unknown"), id).withStyle(color(LABEL)));
            return 0;
        }
        PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);
        PlayerStats next = s.levelUp(stat);
        if (next == null) {
            p.sendSystemMessage(Component.translatable(key("maxed"), stat.displayName()).withStyle(color(LABEL)));
            return 0;
        }
        int cost = s.levelUpCost();
        int have = XpUtil.total(p);
        if (have < cost) {
            p.sendSystemMessage(Component.translatable(key("not_enough_xp"), cost, have).withStyle(color(LABEL)));
            return 0;
        }
        p.giveExperiencePoints(-cost);
        p.setData(ModAttachments.PLAYER_STATS, next);
        StatEffects.apply(p);
        p.sendSystemMessage(Component.translatable(key("levelup"),
                        Component.literal(String.valueOf(next.level())).withStyle(color(GOLD)),
                        stat.displayName(),
                        Component.literal(String.valueOf(next.get(stat))).withStyle(color(VALUE)))
                .withStyle(color(LABEL)));
        return 1;
    }

    private static int set(ServerPlayer p, String id, int value) {
        Stat stat = Stat.byId(id);
        if (stat == null) {
            p.sendSystemMessage(Component.translatable(key("unknown"), id).withStyle(color(LABEL)));
            return 0;
        }
        PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);
        p.setData(ModAttachments.PLAYER_STATS, s.with(stat, value));
        StatEffects.apply(p);
        return show(p);
    }
}