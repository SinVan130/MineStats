package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class TooltipEvents {

    private static final int LABEL = 0x8E9AAF;   // серо-голубой для подписей
    private static final int VALUE = 0xF2F4F8;   // светлый для значений
    private static final int LUCK = 0xF472B6;    // нежно-розовый для удачи
    private static final int PASSIVE = 0x6EE7B7; // мятный для пассивок
    private static final int TEMP = 0xFDBA74;    // янтарный для временных эффектов
    private static final int TIME = 0x94A3B8;    // приглушённый для таймеров

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private static Style color(int rgb) {
        return Style.EMPTY.withColor(TextColor.fromRgb(rgb));
    }

    private static String roman(int n) {
        return n >= 1 && n < ROMAN.length ? ROMAN[n] : String.valueOf(n);
    }

    private static String time(long ticks) {
        long s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private static Component line(String key, Component value) {
        return Component.translatable("tooltip." + ExampleMod.MODID + "." + key, value)
                .withStyle(color(LABEL));
    }

    /** Название в цвет редкости + разделитель под ним */
    private static void decorateHeader(List<Component> tip, int rank) {
        if (tip.isEmpty()) return;
        tip.set(0, tip.get(0).copy().withStyle(WeaponRarity.style(rank)));
        tip.add(1, Component.literal("              ")
                .withStyle(WeaponRarity.style(rank).withStrikethrough(true)));
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        var tip = event.getToolTip();
        String id = ExampleMod.MODID;

        // Подсказки для материалов и зелий (оружие)
        WeaponUpgrades.Material m = WeaponUpgrades.materialFor(stack);
        if (m != null) {
            tip.add(Component.translatable("tooltip." + id + ".hint_material",
                    Component.translatable("passive." + id + "." + m.id()), m.unit()).withStyle(color(PASSIVE)));
        } else if (WeaponUpgrades.isUsablePotion(stack)) {
            tip.add(Component.translatable("tooltip." + id + ".hint_potion").withStyle(color(TEMP)));
        }

        // Подсказка для материалов брони
        EquipmentUpgrades.Material em = EquipmentUpgrades.materialFor(stack);
        if (em != null) {
            tip.add(Component.translatable("tooltip." + id + ".hint_armor_material",
                    Component.translatable("stat." + id + "." + em.id()), em.unit()).withStyle(color(PASSIVE)));
        }

        if (WeaponUtil.isLevelable(stack)) {
            weaponTooltip(stack, tip);
        } else if (EquipmentUtil.isArmor(stack)) {
            armorTooltip(stack, tip);
        } else if (JewelryRarity.has(stack)) {
            int rank = JewelryRarity.rank(stack);
            decorateHeader(tip, rank);
            tip.add(line("rarity", WeaponRarity.component(rank)));
        }
    }

    private static void weaponTooltip(ItemStack stack, List<Component> tip) {
        String id = ExampleMod.MODID;
        WeaponData d = stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);
        var level = Minecraft.getInstance().level;
        long now = level == null ? 0 : level.getGameTime();

        decorateHeader(tip, d.rank());

        tip.add(line("rarity", WeaponRarity.component(d.rank())));
        tip.add(line("slots", Component.literal(d.usedSlots() + "/" + d.maxSlots()).withStyle(color(VALUE))));

        if (d.kills() > 0) {
            tip.add(line("kills", Component.literal(String.valueOf(d.kills())).withStyle(color(VALUE))));
        }
        int luck = d.effectiveLuck(now);
        if (luck > 0) {
            tip.add(line("luck", Component.literal(String.valueOf(luck)).withStyle(color(LUCK))));
        }

        for (WeaponData.Passive p : d.passives()) {
            tip.add(Component.translatable("passive." + id + "." + p.id()).withStyle(color(PASSIVE))
                    .append(Component.literal(" " + roman(p.level())).withStyle(color(PASSIVE))));
        }
        for (WeaponData.TempEffect t : d.temps()) {
            long rem = WeaponData.remaining(t, now);
            if (rem <= 0) continue;
            tip.add(Component.translatable("temp." + id + "." + t.id()).withStyle(color(TEMP))
                    .append(Component.literal(" " + roman(t.level())).withStyle(color(TEMP)))
                    .append(Component.literal("  " + time(rem)).withStyle(color(TIME))));
        }
    }

    private static void armorTooltip(ItemStack stack, List<Component> tip) {
        String id = ExampleMod.MODID;
        EquipmentData d = stack.getOrDefault(
                ModDataComponents.EQUIPMENT_DATA.get(), EquipmentData.DEFAULT);

        decorateHeader(tip, d.rank());

        tip.add(line("rarity", WeaponRarity.component(d.rank())));
        tip.add(line("slots", Component.literal(d.usedSlots() + "/" + d.maxSlots()).withStyle(color(VALUE))));

        for (Stat stat : Stat.values()) {
            int lvl = d.get(stat);
            if (lvl <= 0) continue;
            int rgb = stat == Stat.LUCK ? LUCK : PASSIVE;
            tip.add(Component.translatable("stat." + id + "." + stat.name().toLowerCase())
                    .withStyle(color(rgb))
                    .append(Component.literal(" " + roman(lvl)).withStyle(color(rgb))));
        }
    }
}