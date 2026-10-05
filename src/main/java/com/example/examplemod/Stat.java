package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public enum Stat {
    VITALITY("vitality", 0xFB7185),     // коралловый
    STRENGTH("strength", 0xFDBA74),     // янтарный
    DEXTERITY("dexterity", 0x6EE7B7),   // мятный
    INTELLIGENCE("intelligence", 0x60A5FA), // лазурный
    LUCK("luck", 0xF472B6);             // нежно-розовый

    public final String id;
    public final int rgb;

    Stat(String id, int rgb) {
        this.id = id;
        this.rgb = rgb;
    }

    public MutableComponent displayName() {
        return Component.translatable("stat." + ExampleMod.MODID + "." + id)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }

    public static Stat byId(String id) {
        for (Stat s : values()) if (s.id.equals(id)) return s;
        return null;
    }
}