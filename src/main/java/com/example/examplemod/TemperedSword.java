package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TemperedSword extends SwordItem {
    public TemperedSword(Item.Properties props) {
        super(Tiers.IRON, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        WeaponData d = stack.getOrDefault(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT);
        String id = ExampleMod.MODID;
        tooltip.add(Component.translatable("tooltip." + id + ".level", d.level()));
        tooltip.add(Component.translatable("tooltip." + id + ".kills", d.kills()));
    }
}