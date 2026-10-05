package com.example.examplemod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LevelUpPayload(String stat) implements CustomPacketPayload {

    public static final Type<LevelUpPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "level_up"));

    public static final StreamCodec<ByteBuf, LevelUpPayload> CODEC =
            ByteBufCodecs.STRING_UTF8.map(LevelUpPayload::new, LevelUpPayload::stat);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}