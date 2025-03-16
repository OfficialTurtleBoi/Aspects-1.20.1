package net.turtleboi.aspects.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.turtleboi.aspects.Aspects;

public class ModTags {
    public static class Blocks {
        private static TagKey<Block> createTag(String name){
            return BlockTags.create(new ResourceLocation(Aspects.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> RUNE_ITEMS = createTag("rune_items");

        private static TagKey<Item> createTag(String name){
            return ItemTags.create(new ResourceLocation(Aspects.MOD_ID, name));
        }
    }
}
