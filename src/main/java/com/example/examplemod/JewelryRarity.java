package com.example.examplemod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Редкость бижутерии. Ранг только показывается в тултипе и ничего не даёт. */
public class JewelryRarity {

    private static final Map<ResourceLocation, Integer> RANKS = new HashMap<>();

    static {
        // Здесь раздаём ранги: 0=E, 1=D, 2=C, 3=B, 4=A, 5=S, 6=S+ ...
        set("minestats:example_ring", 3);
        set("minestats:example_amulet", 5);
        iron("wimpy_spell_book", 0);            // 0 слотов, тестовая
        iron("copper_spell_book", 0);           // 5 слотов
        iron("iron_spell_book", 1);             // 6 слотов
        iron("rotten_spell_book", 1);           // 8 слотов, +100 маны, но -15% сопротивления
        iron("gold_spell_book", 2);             // 8 слотов, -15% время каста, +50 маны
        iron("diamond_spell_book", 3);          // 10 слотов, +100 маны
        iron("evoker_spell_book", 3);           // уникальная, +200 маны, сила призыва
        iron("villager_spell_book", 3);         // 10 слотов, святая сила, -10% каста
        iron("druidic_spell_book", 3);          // 10 слотов, сила природы
        iron("blaze_spell_book", 3);            // 10 слотов, сила огня
        iron("netherite_spell_book", 4);        // 12 слотов, -20% перезарядки, +200 маны
        iron("ice_spell_book", 4);              // 12 слотов, сила льда
        iron("cursed_doll_spell_book", 4);      // вампирская, уникальная
        iron("dragonskin_spell_book", 5);       // 12 слотов, дроп с дракона
        iron("necronomicon_spell_book", 5);     // уникальная, босс
        iron("legendary_spell_book", 6);        // 12 слотов, творческая

        // ===== Кольца =====
        iron("silver_ring", 0);                 // +25 маны
        iron("emerald_stoneplate_ring", 1);     // +25% опыта
        iron("visibility_ring", 1);             // видеть невидимых
        iron("mana_ring", 2);                   // +100 маны
        iron("cooldown_ring", 2);               // -15% перезарядки
        iron("cast_time_ring", 2);              // -15% времени каста
        iron("fireward_ring", 4);               // иммунитет к огню
        iron("frostward_ring", 2);              // иммунитет к холоду
        iron("poisonward_ring", 2);             // иммунитет к яду
        iron("invisibility_ring", 2);
        iron("expulsion_ring", 2);              // отталкивающий взрыв
        iron("affinity_ring", 3);               // усиление школы
        iron("lurker_ring", 3);
        iron("wicked_bone_ring", 4);            // рикошет снарядов, дроп из ominous
        iron("betrayer_signet", 5);
        iron("heavy_chain_necklace", 2);
        iron("conjurers_talisman", 2);
        iron("amethyst_resonance_charm", 2);
        iron("concentration_amulet", 3);
        iron("greater_conjurers_talisman", 3);
        iron("teleportation_amulet", 4);// босс, сила Eldritch
        // ===== Jewelry =====
        jewelry("copper_ring", 0);
        jewelry("iron_ring", 0);
        jewelry("gold_ring", 0);

        jewelry("emerald_necklace", 1);
        jewelry("diamond_necklace", 1);

        jewelry("ruby_ring", 2);
        jewelry("topaz_ring", 2);
        jewelry("citrine_ring", 2);
        jewelry("jade_ring", 2);
        jewelry("sapphire_ring", 2);
        jewelry("tanzanite_ring", 2);

        jewelry("ruby_necklace", 2);
        jewelry("topaz_necklace", 2);
        jewelry("citrine_necklace", 2);
        jewelry("jade_necklace", 2);
        jewelry("sapphire_necklace", 2);
        jewelry("tanzanite_necklace", 2);

        jewelry("netherite_ruby_ring", 3);
        jewelry("netherite_topaz_ring", 3);
        jewelry("netherite_citrine_ring", 3);
        jewelry("netherite_jade_ring", 3);
        jewelry("netherite_sapphire_ring", 3);
        jewelry("netherite_tanzanite_ring", 3);

        jewelry("netherite_ruby_necklace", 3);
        jewelry("netherite_topaz_necklace", 3);
        jewelry("netherite_citrine_necklace", 3);
        jewelry("netherite_jade_necklace", 3);
        jewelry("netherite_sapphire_necklace", 3);
        jewelry("netherite_tanzanite_necklace", 3);

        jewelry("unique_attack_ring", 4);
        jewelry("unique_attack_necklace", 4);
        jewelry("unique_dex_ring", 4);
        jewelry("unique_dex_necklace", 4);
        jewelry("unique_tank_ring", 4);
        jewelry("unique_tank_necklace", 4);
        jewelry("unique_archer_ring", 4);
        jewelry("unique_archer_necklace", 4);
        jewelry("unique_arcane_ring", 4);
        jewelry("unique_arcane_necklace", 4);
        jewelry("unique_fire_ring", 4);
        jewelry("unique_fire_necklace", 4);
        jewelry("unique_frost_ring", 4);
        jewelry("unique_frost_necklace", 4);
        jewelry("unique_healing_ring", 4);
        jewelry("unique_healing_necklace", 4);

        // ===== Relics =====
        relic("infinity_ham", 0);
        relic("wool_mitten", 1);
        relic("leather_belt", 1);
        relic("aqua_walker", 1);
        relic("amphibian_boot", 1);
        relic("ice_skates", 2);
        relic("ice_breaker", 2);
        relic("magma_walker", 2);
        relic("roller_skates", 2);
        relic("springy_boot", 2);
        relic("blazing_flask", 2);
        relic("bastion_ring", 2);
        relic("drowned_belt", 2);
        relic("hunter_belt", 2);
        relic("jellyfish_necklace", 2);
        relic("spore_sack", 2);
        relic("spatial_sign", 2);

        relic("arrow_quiver", 3);
        relic("chorus_inhibitor", 3);
        relic("enders_hand", 3);
        relic("holy_locket", 3);
        relic("magic_mirror", 3);
        relic("reflection_necklace", 3);
        relic("shadow_glaive", 3);
        relic("space_dissector", 3);
        relic("elytra_booster", 3);
        relic("rage_glove", 3);

        relic("chef_hat", 3);
        relic("piglin_mask", 3);
        relic("cut_glass_boot", 4);
        relic("hunting_belt", 4);
        relic("kinetic_belt", 4);
        relic("leafy_mantle", 4);
        relic("midnight_mantle", 4);
        relic("rider_flute", 4);
        relic("chorus_staff", 4);

        relic("ring_of_the_seven_deadly_sins", 5);
        relic("clot_of_time", 5);
        relic("sphere_of_self_sacrifice", 5);
        relic("reflective_necklace", 5);
        // ===== More Relics =====
        moreRelic("axolotl_cream", 1);
        moreRelic("crown_of_the_legend", 4);
        moreRelic("eject_button", 3);
        moreRelic("guts_orb", 4);

        moreRelic("tyrant_mask", 4);
        moreRelic("king_crimson", 6);

        moreRelic("slumbering_amulet", 3);
        moreRelic("whispering_amulet", 4);
        moreRelic("made_in_heaven", 6);

        moreRelic("mass_gauntlet", 4);
        moreRelic("opal_necklace", 2);
        moreRelic("sentient_rust", 3);
        moreRelic("shieldweave_cape", 4);
        moreRelic("bionic_eye", 4);
        moreRelic("thermoseismic_heart", 4);
        moreRelic("biojoint", 5);
        moreRelic("whims_of_fate", 4);

        moreRelic("depleted_spool", 3);
        moreRelic("weavers_spool", 5);

        moreRelic("moodworm", 3);
        moreRelic("vertebrax", 5);
        moreRelic("gravitum_glove", 4);
        moreRelic("epoch_apple", 4);

        moreRelic("converging_orb", 4);
        moreRelic("wonder_of_u", 6);

        moreRelic("gravitum_strider", 5);
        moreRelic("twin_fangs", 5);
        moreRelic("swiftedge", 4);
        moreRelic("runic_plate", 4);

    }
    private static void iron(String path, int rank) {
        RANKS.put(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", path), rank);
    }
    private static void jewelry(String path, int rank) {
        RANKS.put(ResourceLocation.fromNamespaceAndPath("jewelry", path), rank);
    }

    private static void relic(String path, int rank) {
        RANKS.put(ResourceLocation.fromNamespaceAndPath("relics", path), rank);
    }
    private static void moreRelic(String path, int rank) {
        RANKS.put(ResourceLocation.fromNamespaceAndPath("morerelics", path), rank);
    }

    private static void set(String id, int rank) {
        RANKS.put(ResourceLocation.parse(id), rank);
    }

    private static ResourceLocation key(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static boolean has(ItemStack stack) {
        return RANKS.containsKey(key(stack));
    }

    public static int rank(ItemStack stack) {
        return RANKS.getOrDefault(key(stack), 0);
    }
}