package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import static com.example.examplemod.Titles.*;

public class TitleList {

    public static void registerAll() {
        if (!Titles.isEmpty()) return;

        // ===== E: узкие ситуативные бонусы =====
        add("zombie_slayer", E,
                Cond.kills(EntityType.ZOMBIE, 50),
                Fx.damageVs(EntityTypeTags.UNDEAD, 0.20));

        add("night_stalker", E,
                Cond.playMinutes(60),
                Fx.damageWhen(p -> {
                    long t = p.level().getDayTime() % 24000L;
                    return t >= 13000L && t <= 23000L;
                }, 0.15));

        add("diver", E,
                Cond.distance(Stats.SWIM_ONE_CM, 500),
                Fx.damageWhen(p -> p.isInWater(), 0.20));

        // ===== D =====
        add("spider_bane", D,
                Cond.kills(EntityType.SPIDER, 100),
                Fx.damageVs(EntityTypeTags.ARTHROPOD, 0.25));

        add("springheel", D,
                Cond.stat(Stats.JUMP, 2000),
                Fx.resist(DamageTypes.FALL, 0.5));

        // ===== C =====
        add("fireproof", C,
                Cond.kills(EntityType.BLAZE, 50),
                Fx.resist(DamageTypeTags.IS_FIRE, 0.4));

        // ===== B =====
        add("blast_ward", B,
                Cond.kills(EntityType.CREEPER, 100),
                Fx.resist(DamageTypeTags.IS_EXPLOSION, 0.5));

        add("wanderer", B,
                Cond.distance(Stats.WALK_ONE_CM, 50000),
                Fx.attribute(Attributes.MOVEMENT_SPEED, 0.08, Operation.ADD_MULTIPLIED_BASE));

        // ===== A =====
        add("witherbane", A,
                Cond.kills(EntityType.WITHER, 1),
                Fx.damageVs(Tags.EntityTypes.BOSSES, 0.25),
                Fx.luck(2));

        // ===== S: универсальные и уникальные =====
        add("dragonslayer", S,
                Cond.all(Cond.kills(EntityType.ENDER_DRAGON, 1), Cond.level(20)),
                Fx.damage(0.05),
                Fx.luck(2),
                Fx.onKillProc("dragonslayer_surge", 0.15, p -> {
                    p.heal(4.0f);
                    ManaUtil.add(p, 10);
                }));

        add("legend", S,
                Cond.all(Cond.level(40), Cond.totalKills(5000)),
                Fx.damage(0.08),
                Fx.attribute(Attributes.MAX_HEALTH, 4.0, Operation.ADD_VALUE),
                Fx.luck(3),
                Fx.manaRegen(1));

        // ===== Свои титулы добавляй сюда =====
        // add("cow_friend", E, Cond.kills(EntityType.COW, 1), Fx.luck(1));
        add("shepherd", E,
                Cond.kills(EntityType.SHEEP, 50),
                Fx.luck(1));

        add("slime_hunter", E,
                Cond.kills(EntityType.SLIME, 50),
                Fx.damage(0.03));

        add("chicken_chaser", E,
                Cond.kills(EntityType.CHICKEN, 100),
                Fx.luck(1));

        add("fish_friend", E,
                Cond.kills(EntityType.COD, 50),
                Fx.luck(1));

        add("phantom_bane", D,
                Cond.kills(EntityType.PHANTOM, 30),
                Fx.damage(0.05));

        add("skeleton_hunter", D,
                Cond.kills(EntityType.SKELETON, 150),
                Fx.resist(DamageTypeTags.IS_PROJECTILE, 0.15));

        add("creeper_survivor", D,
                Cond.kills(EntityType.CREEPER, 50),
                Fx.resist(DamageTypeTags.IS_EXPLOSION, 0.25));

        add("enderman_hunter", D,
                Cond.kills(EntityType.ENDERMAN, 50),
                Fx.damage(0.05));

        add("fire_walker", D,
                Cond.kills(EntityType.BLAZE, 25),
                Fx.resist(DamageTypeTags.IS_FIRE, 0.20));

        add("traveler", D,
                Cond.distance(Stats.WALK_ONE_CM, 25000),
                Fx.attribute(Attributes.MOVEMENT_SPEED, 0.04,
                        Operation.ADD_MULTIPLIED_BASE));

        add("sprinter", D,
                Cond.distance(Stats.SPRINT_ONE_CM, 15000),
                Fx.attribute(Attributes.MOVEMENT_SPEED, 0.06,
                        Operation.ADD_MULTIPLIED_BASE));

        add("guardian_slayer", C,
                Cond.kills(EntityType.GUARDIAN, 50),
                Fx.damage(0.08));

        add("piglin_enemy", C,
                Cond.kills(EntityType.PIGLIN, 100),
                Fx.damage(0.06));

        add("nether_walker", C,
                Cond.distance(Stats.WALK_ONE_CM, 100000),
                Fx.resist(DamageTypeTags.IS_FIRE, 0.15),
                Fx.attribute(Attributes.MOVEMENT_SPEED, 0.05,
                        Operation.ADD_MULTIPLIED_BASE));

        add("veteran", B,
                Cond.level(25),
                Fx.attribute(Attributes.MAX_HEALTH, 4.0,
                        Operation.ADD_VALUE),
                Fx.damage(0.03));

        add("monster_slayer", B,
                Cond.totalKills(2500),
                Fx.damage(0.05),
                Fx.luck(2));

        add("blood_hunter", A,
                Cond.totalKills(7500),
                Fx.damage(0.05),
                Fx.onKillProc("blood_hunter", 0.20, p -> {
                    p.heal(2.0f);
                }));

        add("mana_hunter", A,
                Cond.totalKills(3000),
                Fx.onKillProc("mana_hunter", 0.25, p -> {
                    ManaUtil.add(p, 5);
                }));

        add("world_walker", A,
                Cond.distance(Stats.WALK_ONE_CM, 500000),
                Fx.attribute(Attributes.MOVEMENT_SPEED, 0.10,
                        Operation.ADD_MULTIPLIED_BASE),
                Fx.luck(3));

        add("calamity", S,
                Cond.totalKills(25000),
                Fx.damage(0.12),
                Fx.attribute(Attributes.MAX_HEALTH, 6.0,
                        Operation.ADD_VALUE));

        add("apex_predator", S,
                Cond.all(
                        Cond.level(50),
                        Cond.totalKills(15000)
                ),
                Fx.damage(0.10),
                Fx.attribute(Attributes.MOVEMENT_SPEED, 0.08,
                        Operation.ADD_MULTIPLIED_BASE),
                Fx.luck(4));

        add("immortal_veteran", S,
                Cond.playMinutes(10000),
                Fx.attribute(Attributes.MAX_HEALTH, 8.0,
                        Operation.ADD_VALUE),
                Fx.manaRegen(2),
                Fx.luck(3));
        add("night_predator", C,
                Cond.playMinutes(180),
                Fx.damageWhen(p -> {
                    long t = p.level().getDayTime() % 24000L;
                    return t >= 13000L && t <= 23000L;
                }, 0.25));

        add("iron_will", B,
                Cond.level(30),
                Fx.resist(DamageTypeTags.IS_PROJECTILE, 0.20),
                Fx.resist(DamageTypeTags.IS_EXPLOSION, 0.20));

        add("last_stand", A,
                Cond.level(40),
                Fx.damageWhen(p -> p.getHealth() <= 6.0f, 0.30));

        add("executioner", A,
                Cond.totalKills(10000),
                Fx.onKillProc("executioner", 0.15, p -> {
                    p.heal(3.0f);
                    ManaUtil.add(p, 3);
                }));

        add("world_ender", S,
                Cond.all(
                        Cond.level(50),
                        Cond.totalKills(30000)
                ),
                Fx.damage(0.15),
                Fx.attribute(Attributes.MAX_HEALTH, 10.0,
                        Operation.ADD_VALUE),
                Fx.luck(5));
        // ---------- E ----------
        add("cow_friend", E,
                Cond.fed(EntityType.COW, 1),
                Fx.damageWhen(p -> p.level().getBlockState(p.blockPosition().below()).is(Blocks.GRASS_BLOCK), 0.10));

        add("farmhand", E,
                Cond.stat(Stats.ANIMALS_BRED, 10),
                Fx.manaOnKill(1));

        add("fisherman", E,
                Cond.stat(Stats.FISH_CAUGHT, 50),
                Fx.damageWhen(p -> p.isInWaterOrRain(), 0.15));

        add("cave_dweller", E,
                Cond.when("deep", p -> p.getY() < -20),
                Fx.damageWhen(p -> p.getY() < 0, 0.15));

        add("bare_knuckle", E,
                Cond.killedWithAny("unarmed", 10),
                Fx.damageWhen(p -> p.getMainHandItem().isEmpty(), 1.0));

        // ---------- D ----------
        add("gravedigger", D,
                Cond.killedWith("shovel", EntityType.ZOMBIE, 25),
                Fx.damageWhen(p -> p.getMainHandItem().is(ItemTags.SHOVELS), 0.40));

        add("skyborne", D,
                Cond.when("sky", p -> p.getY() > 280),
                Fx.resist(DamageTypes.FALL, 0.25));

        add("storm_caller", D,
                Cond.killedWithAny("storm", 25),
                Fx.damageWhen(p -> p.level().isThundering(), 0.20),
                Fx.resist(DamageTypes.LIGHTNING_BOLT, 0.8));

        add("close_shave", D,
                Cond.closeCalls(5),
                Fx.damageWhen(p -> p.getHealth() <= 6.0f, 0.20));

        add("sharpshooter", D,
                Cond.killedWithAny("ranged", 100),
                new Fx() {
                    @Override
                    public float dealt(ServerPlayer player, LivingEntity target, DamageSource source, float amount) {
                        boolean projectile = source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
                        return projectile ? amount * 1.2f : amount;
                    }
                });

        add("nether_visitor", D,
                Cond.advancement("minecraft:story/enter_the_nether"),
                Fx.damageWhen(p -> p.level().dimension() == Level.NETHER, 0.10));

        // ---------- C ----------
        add("diamond_digger", C,
                Cond.minedAny(500, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
                Fx.attribute(Attributes.BLOCK_BREAK_SPEED, 0.15, Operation.ADD_MULTIPLIED_BASE),
                Fx.luck(1));

        add("brute_breaker", C,
                Cond.killedWith("axe", EntityType.PIGLIN_BRUTE, 1),
                Fx.damageWhen(p -> p.getMainHandItem().is(ItemTags.AXES), 0.15));

        add("beastmaster", C,
                Cond.tamed(5),
                Fx.healOnKill(1.0f));

        add("merchant", C,
                Cond.stat(Stats.TRADED_WITH_VILLAGER, 100),
                Fx.luck(1));

        add("trident_lord", C,
                Cond.killedWith("trident", EntityType.DROWNED, 50),
                Fx.damageWhen(p -> p.getMainHandItem().is(Items.TRIDENT), 0.25));

        // ---------- B ----------
        add("phantom_hunter", B,
                Cond.killedWith("glide", EntityType.PHANTOM, 1),
                Fx.damageWhen(p -> p.isFallFlying(), 0.30),
                Fx.resist(DamageTypes.FLY_INTO_WALL, 0.5));

        add("end_walker", B,
                Cond.advancement("minecraft:story/enter_the_end"),
                Fx.damageWhen(p -> p.level().dimension() == Level.END, 0.12),
                Fx.attribute(Attributes.MAX_HEALTH, 2.0, Operation.ADD_VALUE));

        add("mace_crusher", B,
                Cond.killedWithAny("mace", 25),
                Fx.damageWhen(p -> p.getMainHandItem().is(Items.MACE), 0.20));

        add("ancient_hoarder", B,
                Cond.mined(Blocks.ANCIENT_DEBRIS, 100),
                Fx.attribute(Attributes.BLOCK_BREAK_SPEED, 0.25, Operation.ADD_MULTIPLIED_BASE),
                Fx.resist(DamageTypeTags.IS_EXPLOSION, 0.2));

        // ---------- A ----------
        add("monster_hunter", A,
                Cond.advancement("minecraft:adventure/kill_all_mobs"),
                Fx.damage(0.05),
                Fx.manaOnKill(2));

        add("beacon_keeper", A,
                Cond.advancement("minecraft:nether/create_full_beacon"),
                Fx.luck(1),
                Fx.attribute(Attributes.ARMOR, 3.0, Operation.ADD_VALUE));

        add("warden_slayer", A,
                Cond.kills(EntityType.WARDEN, 1),
                Fx.resist(DamageTypes.SONIC_BOOM, 0.7),
                Fx.attribute(Attributes.KNOCKBACK_RESISTANCE, 0.3, Operation.ADD_VALUE));

        add("village_hero", A,
                Cond.advancement("minecraft:adventure/hero_of_the_village"),
                Fx.luck(2));

        // ---------- S ----------
        add("sky_sovereign", S,
                Cond.all(Cond.killedWithAny("glide", 200), Cond.distance(Stats.AVIATE_ONE_CM, 20000)),
                Fx.damageWhen(p -> p.isFallFlying(), 0.08),
                Fx.luck(2),
                new Fx() {
                    @Override
                    public void onKill(ServerPlayer player, LivingEntity victim) {
                        if (player.isFallFlying() && ProcSystem.roll(player, "sky_sovereign_boost", 0.25)) {
                            player.heal(3.0f);
                            player.setDeltaMovement(player.getDeltaMovement().add(player.getLookAngle().scale(0.8)));
                            player.hurtMarked = true;
                        }
                    }
                });

        add("undying", S,
                Cond.all(Cond.closeCalls(100), Cond.level(30)),
                Fx.luck(2),
                new Fx() {
                    @Override
                    public float taken(ServerPlayer player, DamageSource source, float amount) {
                        if (amount >= player.getHealth() && ProcSystem.roll(player, "undying_last_stand", 0.20)) {
                            return Math.max(0.0f, player.getHealth() - 1.0f);
                        }
                        return amount;
                    }
                });
    }
}