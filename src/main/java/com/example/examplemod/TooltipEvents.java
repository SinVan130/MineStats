package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class TooltipEvents {

    private static final int LABEL = 0x8E9AAF;  // серо-голубой для подписей
    private static final int VALUE = 0xF2F4F8;  // светлый для значений
    private static final int LUCK = 0xF472B6;   // нежно-розовый для удачи

    private static Style color(int rgb) {
        return Style.EMPTY.withColor(TextColor.fromRgb(rgb));
    }

    private static Component line(String key, Component value) {
        return Component.translatable("tooltip." + ExampleMod.MODID + "." + key, value)
                .withStyle(color(LABEL));
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!WeaponUtil.isLevelable(stack)) return;

        WeaponData d = stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);
        var tip = event.getToolTip();

        // Название окрашиваем в цвет редкости
        if (!tip.isEmpty()) {
            tip.set(0, tip.get(0).copy().withStyle(WeaponRarity.style(d.rank())));
        }

        // Редкость показываем всегда
        tip.add(line("rarity", WeaponRarity.component(d.rank())));

        // Остальное только если есть что показывать
        if (d.kills() > 0) {
            tip.add(line("kills", Component.literal(String.valueOf(d.kills())).withStyle(color(VALUE))));
        }
        if (d.luckBonus() > 0) {
            tip.add(line("luck", Component.literal(String.valueOf(d.luckBonus())).withStyle(color(LUCK))));
        }
    }
}