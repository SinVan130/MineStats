package com.example.examplemod;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExampleMod.MODID);

    public static final DeferredItem<TemperedSword> TEMPERED_SWORD = ITEMS.register("tempered_sword",
            () -> new TemperedSword(new Item.Properties()
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4f))
                    .component(ModDataComponents.WEAPON_DATA.get(), WeaponData.DEFAULT)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
