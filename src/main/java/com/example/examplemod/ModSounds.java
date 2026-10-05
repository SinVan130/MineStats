package com.example.examplemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ExampleMod.MODID);

    public static final Supplier<SoundEvent> TITLE = register("title");
    public static final Supplier<SoundEvent> RANK_UP = register("rank_up");
    public static final Supplier<SoundEvent> LVL_UP = register("lvl_up");

    private static final Set<String> QUEUED = Set.of("title", "rank_up", "lvl_up");

    private static Supplier<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, name)));
    }

    /** Эти звуки клиент ставит в очередь, а не играет сразу */
    public static boolean isQueued(ResourceLocation id) {
        return id.getNamespace().equals(ExampleMod.MODID) && QUEUED.contains(id.getPath());
    }

    /** Звук слышит только сам игрок */
    public static void play(ServerPlayer player, Supplier<SoundEvent> sound) {
        player.playNotifySound(sound.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}