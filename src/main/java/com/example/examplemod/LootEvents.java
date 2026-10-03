package com.example.examplemod;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class LootEvents {

    private static final double LOOTING_CHANCE = 0.25;

    private record Stripped(ItemStack stack, ItemEnchantments original) {}

    private static final Map<UUID, Stripped> STRIPPED = new HashMap<>();

    private static Holder<Enchantment> lootingHolder(ServerPlayer player) {
        return player.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.LOOTING);
    }

    private static void restore(UUID id) {
        Stripped s = STRIPPED.remove(id);
        if (s != null) {
            s.stack().set(DataComponents.ENCHANTMENTS, s.original());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        if (source.getDirectEntity() != player) return; // только ближний бой

        ItemStack stack = player.getMainHandItem();
        if (!WeaponUtil.isLevelable(stack)) return;

        Holder<Enchantment> looting = lootingHolder(player);
        if (EnchantmentHelper.getItemEnchantmentLevel(looting, stack) <= 0) return;

        if (ProcSystem.roll(player, "looting", LOOTING_CHANCE)) return; // повезло: добыча работает

        restore(player.getUUID()); // на всякий случай, если что-то осталось
        ItemEnchantments original = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable changed = new ItemEnchantments.Mutable(original);
        changed.set(looting, 0);
        stack.set(DataComponents.ENCHANTMENTS, changed.toImmutable());
        STRIPPED.put(player.getUUID(), new Stripped(stack, original));
    }

    // Дроп посчитан: возвращаем зачарование
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrops(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            restore(player.getUUID());
        }
    }

    // Страховка: если дроп не случился, возвращаем на следующем тике
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && STRIPPED.containsKey(player.getUUID())) {
            restore(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        restore(event.getEntity().getUUID());
    }
    public static boolean isStripped(UUID id) {
        return STRIPPED.containsKey(id);
    }
}