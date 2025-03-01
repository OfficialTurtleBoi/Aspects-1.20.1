package net.turtleboi.aspects.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.turtleboi.aspects.Aspects;

@EventBusSubscriber(modid = Aspects.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModAttributes {
    public static final DeferredRegister<Attribute> REGISTRY =
            DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, Aspects.MODID);


    public static final DeferredHolder<Attribute, Attribute> INFERNUM_ASPECT =
            create("infernum_aspect", 0D, 0D);

    public static final DeferredHolder<Attribute, Attribute> GLACIUS_ASPECT =
            create("glacius_aspect", 0D, 0D);

    public static final DeferredHolder<Attribute, Attribute> TERRA_ASPECT =
            create("terra_aspect", 0D, 0D);

    public static final DeferredHolder<Attribute, Attribute> TEMPESTUS_ASPECT =
            create("tempestus_aspect", 0D, 0D);

    public static final DeferredHolder<Attribute, Attribute> ARCANI_ASPECT =
            create("arcani_aspect", 0D, 0D);

    public static final DeferredHolder<Attribute, Attribute> UMBRE_ASPECT =
            create("umbre_aspect", 0D, 0D);

    private static DeferredHolder<Attribute, Attribute> create(String name, double defaultValue, double minValue) {
        String descriptionId = "attribute.name." + Aspects.MODID + "." + name;
        return REGISTRY.register(name, () -> new RangedAttribute(descriptionId, defaultValue, minValue, 1024.0D)
                .setSyncable(true));
    }

    @SubscribeEvent
    public static void attachAttributes(EntityAttributeModificationEvent event) {
        REGISTRY.getEntries().forEach(entry -> {
            ResourceLocation id = entry.getId();
            BuiltInRegistries.ATTRIBUTE.getHolder(id).ifPresent(holder ->
                    event.add(EntityType.PLAYER, holder)
            );
        });
    }
}
