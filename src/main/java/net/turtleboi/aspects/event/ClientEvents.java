package net.turtleboi.aspects.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.client.renderer.ArcaneAuraRenderer;
import net.turtleboi.aspects.client.renderer.ColdAuraRenderer;
import net.turtleboi.aspects.client.renderer.FireAuraRenderer;
import net.turtleboi.aspects.util.AspectUtil;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Aspects.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        String aspect = AspectUtil.getAspect(stack);

        if (aspect != null) {
            Component aspectLine = Component.translatable("aspect.aspects." + aspect)
                    .withStyle(style -> style.withColor(AspectUtil.getAspectColor(aspect)));
            event.getToolTip().add(aspectLine);
            //Component aspectLineAlt = Component.translatable("aspectinfo.aspects.alt_info")
            //        .withStyle(ChatFormatting.BLUE);
//
            //if (isAltKeyDown()){
            //    aspectLineAlt = Component.translatable("aspectinfo.aspects." + aspect)
            //            .withStyle(ChatFormatting.YELLOW);
            //}
            //event.getToolTip().add(aspectLineAlt);

        }
    }

    @SubscribeEvent
    public static void onRenderEntity(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity livingEntity = event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();

        FireAuraRenderer.renderAuras(bufferSource, poseStack, livingEntity, event.getPartialTick());
        ColdAuraRenderer.renderAuras(bufferSource, poseStack, livingEntity, event.getPartialTick());
        ArcaneAuraRenderer.renderAuras(bufferSource, poseStack, livingEntity, event.getPartialTick());
    }
}
