package net.turtleboi.aspects.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.aspects.Aspects;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Aspects.MOD_ID);

    public static final RegistryObject<Item> INFERNUM_RUNE = ITEMS.register("infernum_rune",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GLACIUS_RUNE = ITEMS.register("glacius_rune",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TERRA_RUNE = ITEMS.register("terra_rune",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TEMPESTAS_RUNE = ITEMS.register("tempestas_rune",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ARCANI_RUNE = ITEMS.register("arcani_rune",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> UMBRE_RUNE = ITEMS.register("umbre_rune",
            () -> new Item(new Item.Properties()));


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
