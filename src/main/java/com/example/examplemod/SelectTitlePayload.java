package com.example.examplemod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectTitlePayload(String id) implements CustomPacketPayload {

    public static final Type<SelectTitlePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "select_title"));

    public static final StreamCodec<ByteBuf, SelectTitlePayload> CODEC =
            ByteBufCodecs.STRING_UTF8.map(SelectTitlePayload::new, SelectTitlePayload::id);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}