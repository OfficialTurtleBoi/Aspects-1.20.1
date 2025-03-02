package net.turtleboi.aspects.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.turtleboi.aspects.Aspects;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Aspects.MOD_ID);

    public static final DeferredItem<Item> INFERNUM_RUNE = ITEMS.register("infernum_rune",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> GLACIUS_RUNE = ITEMS.register("glacius_rune",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TERRA_RUNE = ITEMS.register("terra_rune",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TEMPESTAS_RUNE = ITEMS.register("tempestas_rune",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ARCANI_RUNE = ITEMS.register("arcani_rune",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> UMBRE_RUNE = ITEMS.register("umbre_rune",
            () -> new Item(new Item.Properties()));


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
