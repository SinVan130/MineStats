package com.example.examplemod;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;

public class StatEffects {

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, path);
    }

    private static final ResourceLocation VIT = id("stat_vitality");
    private static final ResourceLocation STR = id("stat_strength");
    private static final ResourceLocation DEX_SPEED = id("stat_dex_attack_speed");
    private static final ResourceLocation DEX_MOVE = id("stat_dex_move");
    private static final ResourceLocation INT_MANA = id("stat_intelligence_mana");
    private static final ResourceLocation INT_MANA_REGEN = id("stat_intelligence_mana_regen");

    public static void apply(ServerPlayer p) {
        PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);

        int vit = StatBonus.effective(p, s, Stat.VITALITY);
        int str = StatBonus.effective(p, s, Stat.STRENGTH);
        int dex = StatBonus.effective(p, s, Stat.DEXTERITY);
        int intel = StatBonus.effective(p, s, Stat.INTELLIGENCE);

        set(p, Attributes.MAX_HEALTH, VIT, (vit - PlayerStats.BASE) * 0.5, Operation.ADD_VALUE);
        set(p, Attributes.ATTACK_DAMAGE, STR, (str - PlayerStats.BASE) * 0.01, Operation.ADD_MULTIPLIED_TOTAL);
        set(p, Attributes.ATTACK_SPEED, DEX_SPEED, (dex - PlayerStats.BASE) * 0.01, Operation.ADD_VALUE);
        set(p, Attributes.MOVEMENT_SPEED, DEX_MOVE, (dex - PlayerStats.BASE) * 0.002, Operation.ADD_MULTIPLIED_BASE);
        set(p, AttributeRegistry.MAX_MANA, INT_MANA, (intel - PlayerStats.BASE) * 5.0, Operation.ADD_VALUE);
        set(p, AttributeRegistry.MANA_REGEN, INT_MANA_REGEN, (intel - PlayerStats.BASE) * 0.005, Operation.ADD_MULTIPLIED_TOTAL);

        double damage = p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (Math.abs(p.getData(ModAttachments.ATTACK_DAMAGE_SYNC) - damage) > 0.001) {
            p.setData(ModAttachments.ATTACK_DAMAGE_SYNC, damage);
        }

        String cls = ClassUtil.get(p);
        if (!cls.equals(p.getData(ModAttachments.PLAYER_CLASS))) {
            p.setData(ModAttachments.PLAYER_CLASS, cls);
        }
    }

    private static void set(ServerPlayer p, Holder<Attribute> attr, ResourceLocation id, double amount, Operation op) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        AttributeModifier existing = inst.getModifier(id);
        if (amount == 0) {
            if (existing != null) inst.removeModifier(id);
            return;
        }
        // Не трогаем, если ничего не изменилось: иначе атрибут рассылается клиенту каждую секунду
        if (existing != null && existing.amount() == amount && existing.operation() == op) return;
        inst.addOrUpdateTransientModifier(new AttributeModifier(id, amount, op));
    }
}