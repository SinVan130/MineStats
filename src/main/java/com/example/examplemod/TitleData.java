package com.example.examplemod;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record TitleData(Set<String> unlocked) {

    public static final TitleData EMPTY = new TitleData(Set.of());

    public static final Codec<TitleData> CODEC = Codec.STRING.listOf().xmap(
            list -> new TitleData(Set.copyOf(list)),
            data -> List.copyOf(data.unlocked()));

    public static final StreamCodec<ByteBuf, TitleData> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())
                    .map(list -> new TitleData(Set.copyOf(list)), data -> List.copyOf(data.unlocked()));

    public boolean has(String id) {
        return unlocked.contains(id);
    }

    public TitleData with(String id) {
        Set<String> copy = new HashSet<>(unlocked);
        copy.add(id);
        return new TitleData(Set.copyOf(copy));
    }

    public TitleData without(String id) {
        Set<String> copy = new HashSet<>(unlocked);
        copy.remove(id);
        return new TitleData(Set.copyOf(copy));
    }
}