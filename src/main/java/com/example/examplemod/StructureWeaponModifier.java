package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class StructureWeaponModifier extends LootModifier {

    public static final MapCodec<StructureWeaponModifier> CODEC =
            com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst ->
                    LootModifier.codecStart(inst).apply(inst, StructureWeaponModifier::new));

    public StructureWeaponModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        ResourceLocation table = context.getQueriedLootTableId();
        if (!table.getPath().startsWith("chests/")) return loot;

        // Верхний и Нижний миры: до A (ранг 4), Энд: до S (ранг 5)
        int maxRank = context.getLevel().dimension() == Level.END ? WeaponRarity.S_RANK : 4;
        RandomSource random = context.getRandom();

        for (ItemStack stack : loot) {
            if (!WeaponUtil.isLevelable(stack)) continue;
            if (stack.has(ModDataComponents.WEAPON_DATA.get())) continue;

            // Перекос к низким рангам: высокие выпадают редко
            double r = random.nextDouble();
            int rank = Math.min(maxRank, (int) Math.floor((maxRank + 1) * r * r));
            long points = RankPoints.threshold(rank)
                    + (long) (random.nextDouble() * RankPoints.cost(rank));

            // Очки за зачарования уже входят в эту сумму, чтобы не засчитались второй раз
            ItemEnchantments ench = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            long enchLevels = 0;
            for (var e : ench.entrySet()) enchLevels += e.getIntValue();
            long enchantPoints = enchLevels * RankPoints.ENCHANT_POINTS_PER_LEVEL;

            stack.set(ModDataComponents.WEAPON_DATA.get(), new WeaponData(points, 0, 0, enchantPoints));
            WeaponUtil.updateAttributes(stack);
        }
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}