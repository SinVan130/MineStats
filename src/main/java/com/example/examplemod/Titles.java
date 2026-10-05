package com.example.examplemod;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Titles {

    public static final int E = 0, D = 1, C = 2, B = 3, A = 4, S = 5;
    private static final String[] RANK_LETTERS = {"E", "D", "C", "B", "A", "S"};

    public record Def(String id, int rank, Cond condition, List<Fx> effects) {
        public MutableComponent name() {
            return Component.translatable("title." + ExampleMod.MODID + "." + id);
        }

        public MutableComponent description() {
            return Component.translatable("title." + ExampleMod.MODID + "." + id + ".desc");
        }

        public MutableComponent styledName() {
            return name().withStyle(WeaponRarity.style(rank));
        }

        /** Название, на которое можно нажать, чтобы выбрать титул */
        public MutableComponent clickableName(Component hover) {
            return name().withStyle(WeaponRarity.style(rank)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/titles set " + id))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
        }
    }

    private static final Map<String, Def> ALL = new LinkedHashMap<>();
    private static final List<Def> WITH_ATTRIBUTES = new ArrayList<>();

    public static void add(String id, int rank, Cond condition, Fx... effects) {
        if (!id.matches("[a-z0-9_]+")) {
            ExampleMod.LOGGER.error("Bad title id '{}': use only a-z, 0-9 and _", id);
            return;
        }
        if (rank < E || rank > S) {
            ExampleMod.LOGGER.error("Bad rank for title '{}'", id);
            return;
        }
        if (ALL.containsKey(id)) {
            ExampleMod.LOGGER.error("Duplicate title id '{}'", id);
            return;
        }
        Def def = new Def(id, rank, condition, List.of(effects));
        ALL.put(id, def);
        for (Fx fx : effects) {
            if (fx instanceof Fx.Attr) {
                WITH_ATTRIBUTES.add(def);
                break;
            }
        }
    }

    public static Def get(String id) {
        return ALL.get(id);
    }

    public static Collection<Def> all() {
        return ALL.values();
    }

    public static List<Def> withAttributes() {
        return WITH_ATTRIBUTES;
    }

    public static boolean isEmpty() {
        return ALL.isEmpty();
    }

    public static int rankOf(String letter) {
        for (int i = 0; i < RANK_LETTERS.length; i++) {
            if (RANK_LETTERS[i].equalsIgnoreCase(letter)) return i;
        }
        return -1;
    }
}