package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class KillEvents {

    private static final String NO_COUNT_TAG = "minestats_no_count";

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        MobSpawnType type = event.getSpawnType();
        if (type == MobSpawnType.SPAWNER
                || type == MobSpawnType.TRIAL_SPAWNER
                || type == MobSpawnType.SPAWN_EGG
                || type == MobSpawnType.COMMAND) {
            event.getEntity().getPersistentData().putBoolean(NO_COUNT_TAG, true);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (event.getEntity().getPersistentData().getBoolean(NO_COUNT_TAG)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = player.getMainHandItem();
        if (!WeaponUtil.isLevelable(stack)) return;

        boolean isBoss = event.getEntity().getType().is(Tags.EntityTypes.BOSSES);

        WeaponData data = stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);
        WeaponData next = data.addKill(isBoss);
        stack.set(ModDataComponents.WEAPON_DATA.get(), next);

        if (next.rank() > data.rank()) {
            WeaponUtil.updateAttributes(stack);
            player.displayClientMessage(
                    Component.translatable("message." + ExampleMod.MODID + ".rankup",
                            WeaponRarity.component(next.rank())), true);
            Fancy.rankUp(player, next.rank());
        }
    }
}