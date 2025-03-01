package net.turtleboi.aspects.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Aspects.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.INFERNUM_RUNE.get());
        basicItem(ModItems.GLACIUS_RUNE.get());
        basicItem(ModItems.TERRA_RUNE.get());
        basicItem(ModItems.TEMPESTAS_RUNE.get());
        basicItem(ModItems.ARCANI_RUNE.get());
        basicItem(ModItems.UMBRE_RUNE.get());
    }
}
