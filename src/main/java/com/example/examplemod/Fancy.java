package com.example.examplemod;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.joml.Vector3f;

public class Fancy {

    /** Всплеск частиц цвета rgb вокруг игрока */
    public static void burst(ServerPlayer p, int rgb) {
        if (!(p.level() instanceof ServerLevel sl)) return;
        Vector3f color = new Vector3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
        sl.sendParticles(new DustParticleOptions(color, 1.2f),
                p.getX(), p.getY(1.0), p.getZ(), 40, 0.5, 0.8, 0.5, 0.02);
        sl.sendParticles(ParticleTypes.END_ROD,
                p.getX(), p.getY(1.0), p.getZ(), 10, 0.4, 0.8, 0.4, 0.02);
    }

    /** Рост ранга оружия */
    public static void rankUp(ServerPlayer p, int rank) {
        burst(p, WeaponRarity.rgb(rank));
        p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 0.6f, 0.9f);
    }
}