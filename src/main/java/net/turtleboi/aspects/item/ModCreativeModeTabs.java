package net.turtleboi.aspects.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.block.ModBlocks;
import org.checkerframework.checker.units.qual.C;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Aspects.MODID);

    public static final Supplier<CreativeModeTab> ASPECTS_TAB = CREATIVE_MODE_TAB.register("aspects_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.INFERNUM_RUNE.get()))
                    .title(Component.translatable("creativetab.aspects.aspects_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.INFERNUM_RUNE);
                        output.accept(ModItems.GLACIUS_RUNE);
                        output.accept(ModItems.TERRA_RUNE);
                        output.accept(ModItems.TEMPESTAS_RUNE);
                        output.accept(ModItems.ARCANI_RUNE);
                        output.accept(ModItems.UMBRE_RUNE);

                        output.accept(ModBlocks.RUNE_BLOCK);
                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
