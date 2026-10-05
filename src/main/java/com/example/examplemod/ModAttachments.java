package com.example.examplemod;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ExampleMod.MODID);

    public static final Supplier<AttachmentType<PityData>> PITY =
            ATTACHMENTS.register("pity", () -> AttachmentType.builder(() -> new PityData()).build());

    public static final Supplier<AttachmentType<PlayerStats>> PLAYER_STATS =
            ATTACHMENTS.register("player_stats", () ->
                    AttachmentType.builder(() -> PlayerStats.DEFAULT)
                            .serialize(PlayerStats.CODEC)
                            .sync(PlayerStats.STREAM_CODEC)
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<TitleData>> TITLES =
            ATTACHMENTS.register("titles", () ->
                    AttachmentType.builder(() -> TitleData.EMPTY)
                            .serialize(TitleData.CODEC)
                            .sync(TitleData.STREAM_CODEC)
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<String>> SELECTED_TITLE =
            ATTACHMENTS.register("selected_title", () ->
                    AttachmentType.builder(() -> "")
                            .serialize(Codec.STRING)
                            .sync(ByteBufCodecs.STRING_UTF8)
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<TitleCounters>> COUNTERS =
            ATTACHMENTS.register("title_counters", () ->
                    AttachmentType.builder(() -> TitleCounters.EMPTY)
                            .serialize(TitleCounters.CODEC)
                            .copyOnDeath()
                            .build());

    /** Урон игрока с сервера: ванильный атрибут урона клиенту не передаётся */
    public static final Supplier<AttachmentType<Double>> ATTACK_DAMAGE_SYNC =
            ATTACHMENTS.register("attack_damage_sync", () ->
                    AttachmentType.builder(() -> 1.0)
                            .sync(ByteBufCodecs.DOUBLE)
                            .build());
    /** Класс игрока: клиент метку атрибута не видит, поэтому сервер присылает его сам */
    public static final Supplier<AttachmentType<String>> PLAYER_CLASS =
            ATTACHMENTS.register("player_class", () ->
                    AttachmentType.builder(() -> "")
                            .sync(ByteBufCodecs.STRING_UTF8)
                            .build());

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}