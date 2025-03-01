package net.turtleboi.aspects.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.util.AspectHelper;

@EventBusSubscriber(modid = Aspects.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        String aspect = AspectHelper.getAspect(stack);

        if (aspect != null) {
            Component aspectLine = Component.literal("Aspect: " + aspect)
                    .withStyle(ChatFormatting.GOLD);
            event.getToolTip().add(aspectLine);
        }
    }
}
