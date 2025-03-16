package net.turtleboi.aspects.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.turtleboi.aspects.Aspects;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Aspects.MOD_ID);

    public static final Supplier<CreativeModeTab> ASPECTS_TAB = CREATIVE_MODE_TAB.register("aspects_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.INFERNUM_RUNE.get()))
                    .title(Component.translatable("creativetab.aspects.aspects_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.INFERNUM_RUNE.get());
                        output.accept(ModItems.GLACIUS_RUNE.get());
                        output.accept(ModItems.TERRA_RUNE.get());
                        output.accept(ModItems.TEMPESTAS_RUNE.get());
                        output.accept(ModItems.ARCANI_RUNE.get());
                        output.accept(ModItems.UMBRE_RUNE.get());

                        //output.accept(ModBlocks.RUNE_BLOCK);
                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
