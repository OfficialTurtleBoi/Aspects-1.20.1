package net.turtleboi.aspects.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.item.ModItems;
import net.turtleboi.aspects.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_,
                              CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, Aspects.MOD_ID, existingFileHelper);
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
