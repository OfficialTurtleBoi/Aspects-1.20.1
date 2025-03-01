package net.turtleboi.aspects.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.block.ModBlocks;
import net.turtleboi.aspects.item.ModItems;
import net.turtleboi.aspects.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Aspects.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Items.RUNE_ITEMS)
                .add(ModItems.INFERNUM_RUNE.get())
                .add(ModItems.GLACIUS_RUNE.get())
                .add(ModItems.TERRA_RUNE.get())
                .add(ModItems.TEMPESTAS_RUNE.get())
                .add(ModItems.ARCANI_RUNE.get())
                .add(ModItems.UMBRE_RUNE.get());
    }
}
