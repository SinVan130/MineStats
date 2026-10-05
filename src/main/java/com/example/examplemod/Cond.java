package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.function.Predicate;

/** Условие получения титула. Готовые варианты ниже, свои делаются через Cond.custom(...) */
public interface Cond {

    boolean test(ServerPlayer player);

    Component hint();

    static Cond of(Predicate<ServerPlayer> predicate, Component hint) {
        return new Cond() {
            @Override
            public boolean test(ServerPlayer player) {
                return predicate.test(player);
            }

            @Override
            public Component hint() {
                return hint;
            }
        };
    }

    private static Component text(String key, Object... args) {
        return Component.translatable("title." + ExampleMod.MODID + ".cond." + key, args);
    }

    /** Убить n мобов указанного типа */
    static Cond kills(EntityType<?> type, int n) {
        return of(p -> p.getStats().getValue(Stats.ENTITY_KILLED.get(type)) >= n,
                text("kills", type.getDescription(), n));
    }

    /** Убить n мобов всего */
    static Cond totalKills(int n) {
        return stat(Stats.MOB_KILLS, n);
    }

    /** Любая ванильная статистика: Stats.JUMP, Stats.DEATHS, Stats.DAMAGE_DEALT... */
    static Cond stat(ResourceLocation stat, int n) {
        return of(p -> p.getStats().getValue(Stats.CUSTOM.get(stat)) >= n,
                text("stat", Component.translatable("stat." + stat.toLanguageKey()), n));
    }

    /** Пройти расстояние в блоках: Stats.WALK_ONE_CM, Stats.SWIM_ONE_CM, Stats.FLY_ONE_CM... */
    static Cond distance(ResourceLocation stat, int blocks) {
        long cm = blocks * 100L;
        return of(p -> p.getStats().getValue(Stats.CUSTOM.get(stat)) >= cm,
                text("distance", Component.translatable("stat." + stat.toLanguageKey()), blocks));
    }

    static Cond playMinutes(int minutes) {
        return of(p -> p.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) >= minutes * 1200,
                text("play_minutes", minutes));
    }

    static Cond mined(Block block, int n) {
        return of(p -> p.getStats().getValue(Stats.BLOCK_MINED.get(block)) >= n,
                text("mined", block.getName(), n));
    }

    static Cond crafted(Item item, int n) {
        return of(p -> p.getStats().getValue(Stats.ITEM_CRAFTED.get(item)) >= n,
                text("crafted", item.getDescription(), n));
    }

    /** Уровень персонажа (из статов) */
    static Cond level(int n) {
        return of(p -> p.getData(ModAttachments.PLAYER_STATS).level() >= n, text("level", n));
    }

    /** Оружие такого ранга в руке в момент проверки (0 = E, 5 = S) */
    static Cond weaponRank(int rank) {
        return of(p -> {
            ItemStack stack = p.getMainHandItem();
            return WeaponUtil.isLevelable(stack)
                    && stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT).rank() >= rank;
        }, text("weapon_rank", WeaponRarity.component(rank)));
    }

    /** Своё условие: Cond.custom(p -> p.getY() > 300, "Подняться выше 300 блоков") */
    static Cond custom(Predicate<ServerPlayer> predicate, String hint) {
        return of(predicate, Component.literal(hint));
    }

    /** Все условия сразу */
    static Cond all(Cond... conditions) {
        return new Cond() {
            @Override
            public boolean test(ServerPlayer player) {
                for (Cond c : conditions) {
                    if (!c.test(player)) return false;
                }
                return true;
            }

            @Override
            public Component hint() {
                MutableComponent out = Component.empty();
                for (int i = 0; i < conditions.length; i++) {
                    if (i > 0) out.append(Component.literal("; "));
                    out.append(conditions[i].hint());
                }
                return out;
            }
        };
    }

    /** Не открывается сам: только командой /titles grant или из твоего кода */
    static Cond special() {
        return of(p -> false, text("special"));
    }
    /** Своё условие с переводом: подсказка берётся из lang по ключу title.<modid>.cond.<key> */
    static Cond when(String key, Predicate<ServerPlayer> predicate) {
        return of(predicate, text(key));
    }

    /** Счётчик из CounterEvents достиг n */
    static Cond counter(String key, int n, Component hint) {
        return of(p -> TitleCounters.get(p, key) >= n, hint);
    }

    /** Покормить животных этого вида n раз */
    static Cond fed(EntityType<?> type, int n) {
        return counter("fed:" + TitleCounters.entityKey(type), n, text("fed", type.getDescription(), n));
    }

    /** Развести животных этого вида n раз */
    static Cond bred(EntityType<?> type, int n) {
        return counter("bred:" + TitleCounters.entityKey(type), n, text("bred", type.getDescription(), n));
    }

    /** Приручить n животных */
    static Cond tamed(int n) {
        return counter("tamed:all", n, text("tamed", n));
    }

    /** Убить моба определённым способом: axe, sword, pickaxe, shovel, hoe, trident, mace,
     *  unarmed, ranged, glide, lowhp, storm, night */
    static Cond killedWith(String how, EntityType<?> type, int n) {
        return counter("kill_" + how + ":" + TitleCounters.entityKey(type), n,
                text("kill_how", type.getDescription(),
                        Component.translatable("title." + ExampleMod.MODID + ".how." + how), n));
    }

    /** Убить n любых мобов определённым способом */
    static Cond killedWithAny(String how, int n) {
        return counter("kill_" + how + ":all", n,
                text("kill_how_any", Component.translatable("title." + ExampleMod.MODID + ".how." + how), n));
    }

    /** Выжить n раз на волоске от смерти (здоровье упало до 1 сердца или меньше) */
    static Cond closeCalls(int n) {
        return counter("close_call", n, text("close_calls", n));
    }

    /** Добыть n блоков любых из перечисленных (например обычная и глубинная алмазная руда) */
    static Cond minedAny(int n, Block first, Block... rest) {
        return of(p -> {
            long sum = p.getStats().getValue(Stats.BLOCK_MINED.get(first));
            for (Block b : rest) sum += p.getStats().getValue(Stats.BLOCK_MINED.get(b));
            return sum >= n;
        }, text("mined", first.getName(), n));
    }

    /** Использовать предмет n раз */
    static Cond used(Item item, int n) {
        return of(p -> p.getStats().getValue(Stats.ITEM_USED.get(item)) >= n,
                text("used", item.getDescription(), n));
    }

    /** Сломать n штук предмета */
    static Cond broke(Item item, int n) {
        return of(p -> p.getStats().getValue(Stats.ITEM_BROKEN.get(item)) >= n,
                text("broke", item.getDescription(), n));
    }

    /** Погибнуть от этого моба n раз */
    static Cond diedTo(EntityType<?> type, int n) {
        return of(p -> p.getStats().getValue(Stats.ENTITY_KILLED_BY.get(type)) >= n,
                text("died_to", type.getDescription(), n));
    }

    /** Нанести hp урона всего (ванильная статистика хранит десятые доли, поэтому умножаем на 10) */
    static Cond dealt(int hp) {
        return of(p -> p.getStats().getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT)) >= hp * 10L,
                text("dealt", hp));
    }

    /** Получить достижение Minecraft, например "minecraft:story/enter_the_nether" */
    static Cond advancement(String id) {
        ResourceLocation rl = ResourceLocation.parse(id);
        Component name = Component.translatable("advancements." + rl.getPath().replace('/', '.') + ".title");
        return of(p -> {
            var holder = p.server.getAdvancements().get(rl);
            return holder != null && p.getAdvancements().getOrStartProgress(holder).isDone();
        }, text("advancement", name));
    }
}