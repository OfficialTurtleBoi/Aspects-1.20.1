package net.turtleboi.aspects.event;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.block.ModBlockEntities;
import net.turtleboi.aspects.client.model.PedestalModel;
import net.turtleboi.aspects.client.renderer.block.PedestalRenderer;

@Mod.EventBusSubscriber(modid = Aspects.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
