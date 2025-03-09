package net.turtleboi.aspects.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.item.ModItems;
import net.turtleboi.aspects.loot.AddItemModifier;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Aspects.MOD_ID);
    }

    @Override
    protected void start() {
        this.add("infernum_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/blaze")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.INFERNUM_RUNE.get()));

        this.add("glacius_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/polar_bear")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.GLACIUS_RUNE.get()));

        this.add("terra_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/husk")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.TERRA_RUNE.get()));

        this.add("tempestas_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/breeze")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.TEMPESTAS_RUNE.get()));

        this.add("arcani_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/enderman")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.ARCANI_RUNE.get()));

        this.add("umbre_rune_from_mobs",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/phantom")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                }, ModItems.UMBRE_RUNE.get()));
    }
}
