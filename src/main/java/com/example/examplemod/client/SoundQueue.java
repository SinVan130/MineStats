package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

import java.util.ArrayDeque;
import java.util.Deque;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class SoundQueue {

    private static final int GAP_TICKS = 6;               // пауза между звуками (20 тиков = секунда)
    private static final int MAX_QUEUE = Integer.MAX_VALUE; // можно ограничить длину очереди

    private static final Deque<SoundEvent> QUEUE = new ArrayDeque<>();
    private static SimpleSoundInstance current = null;
    private static int age = 0;
    private static int gap = 0;

    @SubscribeEvent
    public static void onPlay(PlaySoundEvent event) {
        SoundInstance original = event.getOriginalSound();
        if (original == null || original == current) return; // свой звук из очереди пропускаем
        ResourceLocation id = original.getLocation();
        if (!ModSounds.isQueued(id)) return;
        event.setSound(null); // отменяем мгновенное проигрывание
        if (QUEUE.size() < MAX_QUEUE) QUEUE.add(SoundEvent.createVariableRangeEvent(id));
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) { // вышли из мира
            QUEUE.clear();
            current = null;
            return;
        }
        if (current != null) {
            age++;
            if (age >= 3 && !mc.getSoundManager().isActive(current)) {
                current = null;
                gap = GAP_TICKS;
            }
            return;
        }
        if (gap > 0) {
            gap--;
            return;
        }
        SoundEvent next = QUEUE.poll();
        if (next == null) return;
        current = SimpleSoundInstance.forUI(next, 1.0f, 1.0f);
        age = 0;
        mc.getSoundManager().play(current);
    }
}