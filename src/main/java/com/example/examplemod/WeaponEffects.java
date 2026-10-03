package com.example.examplemod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class WeaponEffects {

    public static final double POTION_BASE_CHANCE = 0.10;
    private static final String DMG_TICK = "minestats_effects_dmg_tick";
    private static final String HIT_TICK = "minestats_effects_hit_tick";

    private static boolean firstThisTick(ServerPlayer player, String tag) {
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        if (data.getLong(tag) == now) return false;
        data.putLong(tag, now);
        return true;
    }

    /** Применяет отложенное время зелий и убирает истёкшие эффекты */
    public static WeaponData normalize(Level level, ItemStack stack) {
        WeaponData d = stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);
        WeaponData n = d.normalizeTemps(level.getGameTime());
        if (!n.equals(d)) stack.set(ModDataComponents.WEAPON_DATA.get(), n);
        return n;
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
        ItemStack stack = player.getMainHandItem();
        if (WeaponUtil.isLevelable(stack)) normalize(player.level(), stack);
    }

    /** До нанесения урона: крит и усиление от зелья силы */
    @SubscribeEvent
    public static void onIncoming(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().getDirectEntity() != player) return;
        if (event.getEntity() == player) return;

        ItemStack stack = player.getMainHandItem();
        if (!WeaponUtil.isLevelable(stack)) return;
        if (!firstThisTick(player, DMG_TICK)) return;

        WeaponData d = normalize(player.level(), stack);
        float amount = event.getAmount();

        int crit = d.passiveLevel("crit");
        if (crit > 0 && ProcSystem.roll(player, "crit", 0.05 * crit)) {
            amount *= 1.5f;
            if (player.level() instanceof ServerLevel sl) {
                LivingEntity t = event.getEntity();
                sl.sendParticles(ParticleTypes.CRIT, t.getX(), t.getY(0.5), t.getZ(), 12, 0.3, 0.3, 0.3, 0.2);
            }
        }

        WeaponData.TempEffect strength = d.temp("strength");
        if (strength != null && ProcSystem.roll(player, "potion_strength", POTION_BASE_CHANCE)) {
            amount += 3.0f * strength.level();
        }
        event.setAmount(amount);
    }

    /** После удара: вампиризм и эффекты зелий */
    @SubscribeEvent
    public static void onPost(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().getDirectEntity() != player) return;
        if (event.getEntity() == player) return;

        ItemStack stack = player.getMainHandItem();
        if (!WeaponUtil.isLevelable(stack)) return;
        if (!firstThisTick(player, HIT_TICK)) return;

        LivingEntity target = event.getEntity();
        WeaponData d = normalize(player.level(), stack);

        int vamp = d.passiveLevel("lifesteal");
        if (vamp > 0) player.heal(event.getNewDamage() * 0.03f * vamp);

        for (WeaponData.TempEffect t : d.temps()) {
            if (t.id().equals("luck") || t.id().equals("strength")) continue;
            if (!ProcSystem.roll(player, "potion_" + t.id(), POTION_BASE_CHANCE)) continue;
            applyTemp(player, target, t);
        }
    }

    private static void applyTemp(ServerPlayer player, LivingEntity target, WeaponData.TempEffect t) {
        int lvl = t.level();
        switch (t.id()) {
            case "ignite" -> target.setRemainingFireTicks(
                    Math.max(target.getRemainingFireTicks(), 20 * (3 + 2 * lvl)));
            case "levitation" -> addEffect(player, target, "levitation", 40 + 20 * lvl, 0);
            case "poison" -> addEffect(player, target, "poison", 80 + 40 * lvl, Math.min(lvl - 1, 2));
            case "weakness" -> addEffect(player, target, "weakness", 160 + 40 * lvl, Math.min(lvl - 1, 2));
            case "slowness" -> addEffect(player, target, "slowness", 80 + 40 * lvl, Math.min(lvl - 1, 3));
            case "glowing" -> addEffect(player, target, "glowing", 200 + 100 * lvl, 0);
            case "regeneration" -> player.heal(1.0f + lvl);
            default -> { }
        }
    }

    private static void addEffect(ServerPlayer player, LivingEntity target, String name, int ticks, int amplifier) {
        BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.withDefaultNamespace(name))
                .ifPresent(h -> target.addEffect(new MobEffectInstance(h, ticks, amplifier), player));
    }
}