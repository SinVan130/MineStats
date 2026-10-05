package com.example.examplemod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class TitleCommands {

    private static final int PAGE_SIZE = 8;
    private static final int LABEL = 0x8E9AAF;
    private static final int VALUE = 0xF2F4F8;
    private static final int OK = 0x6EE7B7;
    private static final int DIM = 0x64748B;
    private static final int WARN = 0xFB7185;

    private static Style color(int rgb) {
        return Style.EMPTY.withColor(TextColor.fromRgb(rgb));
    }

    private static String key(String name) {
        return "titles." + ExampleMod.MODID + "." + name;
    }

    private static List<String> allIds() {
        List<String> ids = new ArrayList<>();
        for (Titles.Def d : Titles.all()) ids.add(d.id());
        Collections.sort(ids);
        return ids;
    }

    private static List<String> unlockedIds(ServerPlayer p) {
        List<String> ids = new ArrayList<>(p.getData(ModAttachments.TITLES).unlocked());
        Collections.sort(ids);
        return ids;
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("titles")
                .executes(ctx -> overview(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("list")
                        .then(Commands.argument("rank", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        List.of("E", "D", "C", "B", "A", "S"), b))
                                .executes(ctx -> list(ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "rank"), 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> list(ctx.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(ctx, "rank"),
                                                IntegerArgumentType.getInteger(ctx, "page"))))))
                .then(Commands.literal("set")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        unlockedIds(c.getSource().getPlayerOrException()), b))
                                .executes(ctx -> set(ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "id")))))
                .then(Commands.literal("clear")
                        .executes(ctx -> clear(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("info")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(allIds(), b))
                                .executes(ctx -> info(ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "id")))))
                .then(Commands.literal("counters")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            p.getData(ModAttachments.COUNTERS).values().forEach((k, v) ->
                                    p.sendSystemMessage(Component.literal(k + " = " + v)));
                            return 1;
                        }))
                .then(Commands.literal("grant")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(allIds(), b))
                                .executes(ctx -> grant(ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "id")))))
                .then(Commands.literal("revoke")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(allIds(), b))
                                .executes(ctx -> revoke(ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "id"))))));
    }

    private static int overview(ServerPlayer p) {
        Titles.Def active = TitleEvents.selected(p);
        Component name = active == null
                ? Component.translatable(key("none")).withStyle(color(DIM))
                : active.clickableName(active.description());
        p.sendSystemMessage(Component.translatable(key("active"), name).withStyle(color(LABEL)));

        TitleData data = p.getData(ModAttachments.TITLES);
        MutableComponent line = Component.empty();
        for (int r = Titles.E; r <= Titles.S; r++) {
            int total = 0;
            int have = 0;
            for (Titles.Def d : Titles.all()) {
                if (d.rank() == r) {
                    total++;
                    if (data.has(d.id())) have++;
                }
            }
            line.append(WeaponRarity.component(r));
            line.append(Component.literal(" " + have + "/" + total + "   ").withStyle(color(VALUE)));
        }
        p.sendSystemMessage(line);
        p.sendSystemMessage(Component.translatable(key("hint")).withStyle(color(DIM)));
        return 1;
    }

    private static int list(ServerPlayer p, String letter, int page) {
        int rank = Titles.rankOf(letter);
        if (rank < 0) {
            p.sendSystemMessage(Component.translatable(key("bad_rank"), letter).withStyle(color(WARN)));
            return 0;
        }
        TitleData data = p.getData(ModAttachments.TITLES);
        List<Titles.Def> defs = Titles.all().stream().filter(d -> d.rank() == rank).toList();
        int have = 0;
        for (Titles.Def d : defs) {
            if (data.has(d.id())) have++;
        }
        int pages = Math.max(1, (defs.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int pg = Math.min(page, pages);

        p.sendSystemMessage(Component.translatable(key("list_header"),
                WeaponRarity.component(rank), pg, pages, have, defs.size()).withStyle(color(LABEL)));

        int from = (pg - 1) * PAGE_SIZE;
        int to = Math.min(defs.size(), from + PAGE_SIZE);
        for (int i = from; i < to; i++) {
            Titles.Def def = defs.get(i);
            boolean has = data.has(def.id());
            Component detail = has ? def.description() : def.condition().hint();
            MutableComponent line = Component.literal(has ? "● " : "○ ").withStyle(color(has ? OK : DIM));
            line.append(def.clickableName(detail));
            line.append(Component.literal(" - ").withStyle(color(DIM)));
            line.append(detail.copy().withStyle(color(has ? LABEL : DIM)));
            p.sendSystemMessage(line);
        }
        return 1;
    }

    private static int set(ServerPlayer p, String id) {
        Titles.Def def = Titles.get(id);
        if (def == null) {
            p.sendSystemMessage(Component.translatable(key("unknown"), id).withStyle(color(WARN)));
            return 0;
        }
        if (!p.getData(ModAttachments.TITLES).has(id)) {
            p.sendSystemMessage(Component.translatable(key("locked")).withStyle(color(WARN)));
            return 0;
        }
        TitleEvents.select(p, id);
        p.sendSystemMessage(Component.translatable(key("selected"), def.styledName()).withStyle(color(LABEL)));
        return 1;
    }

    private static int clear(ServerPlayer p) {
        TitleEvents.select(p, "");
        p.sendSystemMessage(Component.translatable(key("cleared")).withStyle(color(LABEL)));
        return 1;
    }

    private static int info(ServerPlayer p, String id) {
        Titles.Def def = Titles.get(id);
        if (def == null) {
            p.sendSystemMessage(Component.translatable(key("unknown"), id).withStyle(color(WARN)));
            return 0;
        }
        boolean has = p.getData(ModAttachments.TITLES).has(id);
        p.sendSystemMessage(def.styledName().append(Component.literal("  "))
                .append(WeaponRarity.component(def.rank())));
        p.sendSystemMessage(def.description().withStyle(color(LABEL)));
        p.sendSystemMessage(has
                ? Component.translatable(key("state_unlocked")).withStyle(color(OK))
                : Component.translatable(key("state_locked"), def.condition().hint()).withStyle(color(DIM)));
        return 1;
    }

    private static int grant(ServerPlayer p, String id) {
        Titles.Def def = Titles.get(id);
        if (def == null) {
            p.sendSystemMessage(Component.translatable(key("unknown"), id).withStyle(color(WARN)));
            return 0;
        }
        TitleEvents.unlock(p, def, true);
        return 1;
    }

    private static int revoke(ServerPlayer p, String id) {
        Titles.Def def = Titles.get(id);
        if (def == null) {
            p.sendSystemMessage(Component.translatable(key("unknown"), id).withStyle(color(WARN)));
            return 0;
        }
        TitleEvents.revoke(p, id);
        p.sendSystemMessage(Component.translatable(key("revoked"), def.styledName()).withStyle(color(LABEL)));
        return 1;
    }
}