package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class TitleEvents {

    private static final Set<String> BROKEN = ConcurrentHashMap.newKeySet();

    /** Выбранный титул игрока или null */
    public static Titles.Def selected(Player p) {
        String id = p.getData(ModAttachments.SELECTED_TITLE);
        return id.isEmpty() ? null : Titles.get(id);
    }

    public static int luckBonus(Player p) {
        Titles.Def def = selected(p);
        if (def == null) return 0;
        int sum = 0;
        for (Fx fx : def.effects()) sum += fx.luckBonus();
        return sum;
    }

    public static void select(ServerPlayer p, String id) {
        p.setData(ModAttachments.SELECTED_TITLE, id);
        applyAttributes(p);
    }

    public static boolean unlock(ServerPlayer p, Titles.Def def, boolean announce) {
        TitleData data = p.getData(ModAttachments.TITLES);
        if (data.has(def.id())) return false;
        p.setData(ModAttachments.TITLES, data.with(def.id()));
        if (p.getData(ModAttachments.SELECTED_TITLE).isEmpty()) select(p, def.id());
        if (announce) {
            Component msg = Component.translatable("message." + ExampleMod.MODID + ".title_unlocked",
                    def.clickableName(def.description()), WeaponRarity.component(def.rank()));
            p.sendSystemMessage(msg);
            p.displayClientMessage(msg, true);
            ModSounds.play(p, ModSounds.TITLE);
            Fancy.burst(p, WeaponRarity.rgb(def.rank()));
        }
        return true;
    }

    public static void revoke(ServerPlayer p, String id) {
        TitleData data = p.getData(ModAttachments.TITLES);
        p.setData(ModAttachments.TITLES, data.without(id));
        if (id.equals(p.getData(ModAttachments.SELECTED_TITLE))) select(p, "");
    }

    private static ResourceLocation modifierId(Titles.Def def, int index) {
        return ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "title/" + def.id() + "/" + index);
    }

    /** Убирает атрибуты всех титулов и вешает атрибуты выбранного */
    public static void applyAttributes(ServerPlayer p) {
        for (Titles.Def def : Titles.withAttributes()) {
            List<Fx> fx = def.effects();
            for (int i = 0; i < fx.size(); i++) {
                if (fx.get(i) instanceof Fx.Attr a) {
                    AttributeInstance inst = p.getAttribute(a.attribute());
                    if (inst != null) inst.removeModifier(modifierId(def, i));
                }
            }
        }
        Titles.Def active = selected(p);
        if (active == null) return;
        List<Fx> fx = active.effects();
        for (int i = 0; i < fx.size(); i++) {
            if (fx.get(i) instanceof Fx.Attr a) {
                AttributeInstance inst = p.getAttribute(a.attribute());
                if (inst != null) {
                    inst.addOrUpdateTransientModifier(
                            new AttributeModifier(modifierId(active, i), a.amount(), a.operation()));
                }
            }
        }
    }

    private static void checkUnlocks(ServerPlayer p) {
        TitleData data = p.getData(ModAttachments.TITLES);
        for (Titles.Def def : Titles.all()) {
            if (data.has(def.id()) || BROKEN.contains(def.id())) continue;
            boolean ok;
            try {
                ok = def.condition().test(p);
            } catch (RuntimeException e) {
                BROKEN.add(def.id());
                ExampleMod.LOGGER.error("Title condition failed for '{}'", def.id(), e);
                continue;
            }
            if (ok) {
                unlock(p, def, true);
                data = p.getData(ModAttachments.TITLES);
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) applyAttributes(p);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) applyAttributes(p);
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        if (p.tickCount % 20 == 0) {
            Titles.Def def = selected(p);
            if (def != null) {
                for (Fx fx : def.effects()) fx.onSecond(p);
            }
        }
        if (p.tickCount % 40 == 7) checkUnlocks(p);
    }

    @SubscribeEvent
    public static void onIncoming(LivingIncomingDamageEvent event) {
        float amount = event.getAmount();

        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != event.getEntity()) {
            Titles.Def def = selected(attacker);
            if (def != null) {
                for (Fx fx : def.effects()) {
                    amount = fx.dealt(attacker, event.getEntity(), event.getSource(), amount);
                }
            }
        }
        if (event.getEntity() instanceof ServerPlayer victim) {
            Titles.Def def = selected(victim);
            if (def != null) {
                for (Fx fx : def.effects()) amount = fx.taken(victim, event.getSource(), amount);
            }
        }
        if (amount != event.getAmount()) event.setAmount(amount);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (event.getEntity().getPersistentData().getBoolean("minestats_no_count")) return; // спавнеры и яйца
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        Titles.Def def = selected(killer);
        if (def == null) return;
        for (Fx fx : def.effects()) fx.onKill(killer, event.getEntity());
    }
}