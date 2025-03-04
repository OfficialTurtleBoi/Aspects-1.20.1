package net.turtleboi.aspects.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.block.ModBlockEntities;
import net.turtleboi.aspects.client.model.PedestalModel;
import net.turtleboi.aspects.client.renderer.block.PedestalRenderer;

@EventBusSubscriber(modid = Aspects.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerEntityLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PedestalModel.PEDESTAL_LAYER, PedestalModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL_BE.get(), PedestalRenderer::new);
    }
}
