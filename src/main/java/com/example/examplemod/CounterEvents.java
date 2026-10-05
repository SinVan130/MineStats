package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class CounterEvents {

    /** Убийства: каждое засчитывается во все подходящие «способы» */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player) return;
        if (victim.getPersistentData().getBoolean("minestats_no_count")) return; // спавнеры, яйца, команды
        DamageSource src = event.getSource();
        if (!(src.getEntity() instanceof ServerPlayer killer)) return;

        List<String> hows = new ArrayList<>();
        Entity direct = src.getDirectEntity();
        if (direct == killer) {
            ItemStack held = killer.getMainHandItem();
            if (held.isEmpty()) hows.add("unarmed");
            if (held.is(ItemTags.SWORDS)) hows.add("sword");
            if (held.is(ItemTags.AXES)) hows.add("axe");
            if (held.is(ItemTags.PICKAXES)) hows.add("pickaxe");
            if (held.is(ItemTags.SHOVELS)) hows.add("shovel");
            if (held.is(ItemTags.HOES)) hows.add("hoe");
            if (held.is(Items.TRIDENT)) hows.add("trident");
            if (held.is(Items.MACE)) hows.add("mace");
        } else if (direct instanceof Projectile) {
            hows.add("ranged");
        }
        if (killer.isFallFlying()) hows.add("glide");
        if (killer.getHealth() <= 4.0f) hows.add("lowhp");
        if (killer.level().isThundering()) hows.add("storm");
        long t = killer.level().getDayTime() % 24000L;
        if (t >= 13000L && t <= 23000L) hows.add("night");

        String mob = TitleCounters.entityKey(victim.getType());
        List<String> keys = new ArrayList<>();
        for (String how : hows) {
            keys.add("kill_" + how + ":" + mob);
            keys.add("kill_" + how + ":all");
        }
        if (!keys.isEmpty()) TitleCounters.addAll(killer, keys);
    }

    /** Кормление: считается, только если животное реально приняло еду */
    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof Animal animal)) return;
        if (!animal.isFood(event.getItemStack())) return;
        boolean accepts = animal.getAge() < 0 || (animal.getAge() == 0 && animal.canFallInLove());
        if (!accepts) return;
        TitleCounters.add(player, "fed:" + TitleCounters.entityKey(animal.getType()), "fed:all");
    }

    @SubscribeEvent
    public static void onBaby(BabyEntitySpawnEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getCausedByPlayer() instanceof ServerPlayer player)) return;
        Mob parent = event.getParentA();
        TitleCounters.add(player, "bred:" + TitleCounters.entityKey(parent.getType()), "bred:all");
    }

    @SubscribeEvent
    public static void onTame(AnimalTameEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getTamer() instanceof ServerPlayer player)) return;
        TitleCounters.add(player, "tamed:" + TitleCounters.entityKey(event.getAnimal().getType()), "tamed:all");
    }

    /** «На волоске»: здоровье упало с выше 1 сердца до 1 сердца и меньше, но игрок жив */
    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !p.isAlive()) return;
        if (event.getNewDamage() > 0 && p.getHealth() <= 2.0f && p.getHealth() + event.getNewDamage() > 2.0f) {
            TitleCounters.add(p, "close_call");
        }
    }
}