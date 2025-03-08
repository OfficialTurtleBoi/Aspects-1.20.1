package net.turtleboi.aspects.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.client.gui.AspectItemDecorator;
import net.turtleboi.aspects.client.renderer.FrozenRenderer;
import net.turtleboi.aspects.util.AspectUtil;

@EventBusSubscriber(modid = Aspects.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientModBusEvents {
    @SubscribeEvent
    public static void addFrozenLayer(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?> candidate = event.getRenderer(type);
            if (candidate instanceof LivingEntityRenderer<?, ?>) {
                @SuppressWarnings("unchecked")
                LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer =
                        (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) candidate;
                renderer.addLayer(new FrozenRenderer.FrozenLayer<>(renderer));
            }
        }

        for (PlayerSkin.Model skin : event.getSkins()) {
            EntityRenderer<?> candidate = event.getSkin(skin);
            if (candidate instanceof LivingEntityRenderer<?, ?>) {
                @SuppressWarnings("unchecked")
                LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer =
                        (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) candidate;
                renderer.addLayer(new FrozenRenderer.FrozenLayer<>(renderer));
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ArmorItem) {
                event.register(item, new AspectItemDecorator());
            }
        }
    }
}
