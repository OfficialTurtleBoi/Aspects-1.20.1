package net.turtleboi.aspects;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.turtleboi.aspects.block.ModBlockEntities;
import net.turtleboi.aspects.block.ModBlocks;
import net.turtleboi.aspects.client.renderer.SingularityRenderer;
import net.turtleboi.aspects.component.ModDataComponents;
import net.turtleboi.aspects.effect.ModEffects;
import net.turtleboi.aspects.entity.ModEntities;
import net.turtleboi.aspects.item.ModCreativeModeTabs;
import net.turtleboi.aspects.item.ModItems;
import net.turtleboi.aspects.loot.ModLootModifiers;
import net.turtleboi.aspects.particle.ChilledParticles;
import net.turtleboi.aspects.particle.ModParticles;
import net.turtleboi.aspects.particle.StunnedParticles;
import net.turtleboi.aspects.potion.ModPotions;
import net.turtleboi.aspects.util.ModAttributes;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(Aspects.MOD_ID)
public class Aspects {
    public static final String MOD_ID = "aspects";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Aspects(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        ModDataComponents.register(modEventBus);
        ModAttributes.REGISTRY.register(modEventBus);

        ModEntities.register(modEventBus);

        ModEffects.register(modEventBus);
        ModPotions.register(modEventBus);
        ModParticles.register(modEventBus);

        ModLootModifiers.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS){
            event.accept(ModItems.INFERNUM_RUNE);
            event.accept(ModItems.GLACIUS_RUNE);
            event.accept(ModItems.TERRA_RUNE);
            event.accept(ModItems.TEMPESTAS_RUNE);
            event.accept(ModItems.ARCANI_RUNE);
            event.accept(ModItems.UMBRE_RUNE);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
