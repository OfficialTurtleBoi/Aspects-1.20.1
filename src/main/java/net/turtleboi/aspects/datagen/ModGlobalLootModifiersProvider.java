package net.turtleboi.aspects.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.item.ModItems;
import net.turtleboi.aspects.loot.AddItemModifier;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, Aspects.MOD_ID);
    }

    @Override
    protected void start() {
        add("infernum_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(new ResourceLocation("entities/blaze")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.INFERNUM_RUNE.get()));

        add("glacius_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(new ResourceLocation("entities/stray")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.GLACIUS_RUNE.get()));

        add("terra_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(new ResourceLocation("entities/husk")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.TERRA_RUNE.get()));

        add("tempestas_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(new ResourceLocation("entities/creeper")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.TEMPESTAS_RUNE.get()));

        add("arcani_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(new ResourceLocation("entities/enderman")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.ARCANI_RUNE.get()));

        add("umbre_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(new ResourceLocation("entities/phantom")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.UMBRE_RUNE.get()));
    }
}
