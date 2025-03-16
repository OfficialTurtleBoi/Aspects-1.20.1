package net.turtleboi.aspects.event;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.client.gui.AspectItemDecorator;
import net.turtleboi.aspects.client.renderer.FrozenRenderer;

@Mod.EventBusSubscriber(modid = Aspects.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModBusEvents {

    @SubscribeEvent
    public static void addFrozenLayer(EntityRenderersEvent.AddLayers event) {
        ForgeRegistries.ENTITY_TYPES.getValues().stream()
                .filter(entityType -> DefaultAttributes.hasSupplier(entityType))
                .filter(entityType -> entityType != EntityType.ENDER_DRAGON)
                .map(entityType -> (EntityType<? extends LivingEntity>) entityType)
                .forEach(entityType -> {
                    try {
                        LivingEntityRenderer<?, ?> renderer = event.getRenderer(entityType);
                        if (renderer != null) {
                            renderer.addLayer(new FrozenRenderer.FrozenLayer(renderer));
                            System.out.println("Added FrozenLayer to renderer for entity type: " + ForgeRegistries.ENTITY_TYPES.getKey(entityType));
                        }
                    } catch (Exception e) {
                        System.out.println("Could not add FrozenLayer for entity type: " + ForgeRegistries.ENTITY_TYPES.getKey(entityType));
                    }
                });

        for (String skinType : event.getSkins()) {
            LivingEntityRenderer<?, ?> renderer = event.getSkin(skinType);
            if (renderer != null) {
                renderer.addLayer(new FrozenRenderer.FrozenLayer(renderer));
                System.out.println("Added FrozenLayer to skin renderer for skin type: " + skinType);
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        for (Item item : ForgeRegistries.ITEMS) {
            if (item instanceof ArmorItem) {
                event.register(item, new AspectItemDecorator());
            }
        }
    }
}
