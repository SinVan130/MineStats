package com.example.examplemod;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class EffectEvents {

    private static final String TICK_TAG = "minestats_last_proc_tick";

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().getDirectEntity() != player) return;

        ItemStack stack = player.getMainHandItem();
        if (!WeaponUtil.isLevelable(stack)) return;

        LivingEntity target = event.getEntity();
        if (target == player) return;

        // Один бросок на один удар (защита от повторов на каждую цель размаха)
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        if (data.getLong(TICK_TAG) == now) return;
        data.putLong(TICK_TAG, now);

        trySweep(player, target, stack);
    }

    private static void trySweep(ServerPlayer player, LivingEntity target, ItemStack stack) {
        Level level = player.level();
        Holder<Enchantment> sweeping = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SWEEPING_EDGE);

        int lvl = EnchantmentHelper.getItemEnchantmentLevel(sweeping, stack);
        if (lvl <= 0) return;
        if (!ProcSystem.roll(player, "sweeping_edge", LuckUtil.BASE_PROC)) return;

        double radius = 1.0 + 0.5 * lvl;
        float damage = 1.0f + (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * lvl / (lvl + 1f);
        AABB box = target.getBoundingBox().inflate(radius, 0.25, radius);

        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == player || e == target || !e.isAlive() || player.isAlliedTo(e)) continue;
            if (e instanceof ArmorStand a && a.isMarker()) continue;
            e.hurt(player.damageSources().playerAttack(player), damage);
        }

        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    target.getX(), target.getY(0.5), target.getZ(), 1, 0, 0, 0, 0);
        }
    }
}