package net.turtleboi.aspects;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.turtleboi.aspects.block.ModBlockEntities;
import net.turtleboi.aspects.block.ModBlocks;
import net.turtleboi.aspects.client.renderer.SingularityRenderer;
import net.turtleboi.aspects.effect.ModEffects;
import net.turtleboi.aspects.entity.ModEntities;
import net.turtleboi.aspects.item.ModCreativeModeTabs;
import net.turtleboi.aspects.item.ModItems;
import net.turtleboi.aspects.loot.ModLootModifiers;
import net.turtleboi.aspects.network.ModNetworking;
import net.turtleboi.aspects.particle.ChilledParticles;
import net.turtleboi.aspects.particle.ModParticles;
import net.turtleboi.aspects.particle.StunnedParticles;
import net.turtleboi.aspects.util.ModAttributes;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

@Mod(Aspects.MOD_ID)
public class Aspects {
    public static final String MOD_ID = "aspects";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Aspects() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModParticles.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModEffects.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModAttributes.REGISTRY.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {

                });
        ModNetworking.register();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.SINGULARITY.get(), SingularityRenderer::new);
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event){
            event.registerSpriteSet(ModParticles.NONE_PARTICLES.get(), StunnedParticles.Provider::new);
            event.registerSpriteSet(ModParticles.CHILLED_PARTICLES.get(), ChilledParticles.Provider::new);
            event.registerSpriteSet(ModParticles.STUNNED_PARTICLES.get(), StunnedParticles.Provider::new);
        }
    }
}
