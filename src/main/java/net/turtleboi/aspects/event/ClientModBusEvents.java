package net.turtleboi.aspects.event;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.client.gui.AspectItemDecorator;

@Mod.EventBusSubscriber(modid = Aspects.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModBusEvents {

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        for (Item item : ForgeRegistries.ITEMS) {
            if (item instanceof ArmorItem) {
                event.register(item, new AspectItemDecorator());
            }
        }
    }
}
