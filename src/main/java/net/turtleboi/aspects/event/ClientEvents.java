package net.turtleboi.aspects.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
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
            Component aspectLine = Component.translatable("aspect.aspects." + aspect)
                    .withStyle(ChatFormatting.GOLD);
            event.getToolTip().add(aspectLine);
            Component aspectLineAlt = Component.translatable("aspectinfo.aspects.alt_info")
                    .withStyle(ChatFormatting.BLUE);

            if (isAltKeyDown()){
                aspectLineAlt = Component.translatable("aspectinfo.aspects." + aspect)
                        .withStyle(ChatFormatting.YELLOW);
            }
            event.getToolTip().add(aspectLineAlt);

        }
    }

    private static boolean isAltKeyDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LALT) ||
                InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_RALT);
    }
}
