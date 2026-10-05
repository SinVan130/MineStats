package com.example.examplemod;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class EnchantPointsEvents {

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;
        // Пока добыча временно снята (см. LootEvents), не пересчитываем
        if (LootEvents.isStripped(player.getUUID())) return;

        ItemStack stack = player.getMainHandItem();
        if (!WeaponUtil.isLevelable(stack)) return;

        ItemEnchantments ench = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        long totalLevels = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> e : ench.entrySet()) {
            totalLevels += e.getIntValue();
        }
        long target = totalLevels * RankPoints.ENCHANT_POINTS_PER_LEVEL;

        WeaponData data = stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);
        if (data.enchantPoints() == target) return;

        WeaponData next = data.withEnchantPoints(target);
        stack.set(ModDataComponents.WEAPON_DATA.get(), next);
        WeaponUtil.updateAttributes(stack);

        long diff = target - data.enchantPoints();
        if (ProcSystem.DEBUG) {
            player.displayClientMessage(Component.literal(
                    "Зачарования: " + (diff > 0 ? "+" : "") + diff + " очков"), true);
        }
        if (next.rank() > data.rank()) {
            player.displayClientMessage(Component.translatable(
                    "message." + ExampleMod.MODID + ".rankup", WeaponRarity.component(next.rank())), true);
            Fancy.rankUp(player, next.rank());
        }
    }
}