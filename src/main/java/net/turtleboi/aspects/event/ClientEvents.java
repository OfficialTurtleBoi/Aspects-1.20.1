package net.turtleboi.aspects.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.client.renderer.ArcaneAuraRenderer;
import net.turtleboi.aspects.client.renderer.ColdAuraRenderer;
import net.turtleboi.aspects.client.renderer.FireAuraRenderer;
import net.turtleboi.aspects.client.renderer.ShockwaveRenderer;
import net.turtleboi.aspects.client.renderer.util.ParticleSpawnQueue;
import net.turtleboi.aspects.effect.ModEffects;
import net.turtleboi.aspects.effect.StunnedEffect;
import net.turtleboi.aspects.util.AspectUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = Aspects.MOD_ID, value = Dist.CLIENT)
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

    private static boolean isAltKeyDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LALT) ||
                InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_RALT);
    }

    @SubscribeEvent
    public static void onRenderEntity(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity livingEntity = event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = (MultiBufferSource.BufferSource) event.getMultiBufferSource();
        FireAuraRenderer.renderAuras(bufferSource, poseStack, livingEntity, event.getPartialTick());
        ColdAuraRenderer.renderAuras(bufferSource, poseStack, livingEntity, event.getPartialTick());
        ArcaneAuraRenderer.renderAuras(bufferSource, poseStack, livingEntity, event.getPartialTick());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null) {
            UUID uuid = clientPlayer.getUUID();
            if (StunnedEffect.stunnedPlayers.containsKey(uuid)) {
                StunnedEffect.StunPlayerData data = StunnedEffect.stunnedPlayers.get(uuid);
                clientPlayer.setYRot(data.savedYaw);
                clientPlayer.setXRot(data.savedPitch);
                clientPlayer.yRotO = data.savedYaw;
                clientPlayer.xRotO = data.savedPitch;
            }
        }

        ParticleSpawnQueue.tick();
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && (player.hasEffect(ModEffects.STUNNED) || player.hasEffect(ModEffects.FROZEN))) {
            event.getInput().forwardImpulse = 0f;
            event.getInput().leftImpulse = 0f;
            event.getInput().jumping = false;
            event.getInput().shiftKeyDown = false;
        }
    }
}
