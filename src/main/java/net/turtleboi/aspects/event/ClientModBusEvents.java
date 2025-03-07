package net.turtleboi.aspects.event;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.client.renderer.FrozenRenderer;

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
}
