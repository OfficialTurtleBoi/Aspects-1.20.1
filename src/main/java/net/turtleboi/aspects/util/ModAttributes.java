package net.turtleboi.aspects.util;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.aspects.Aspects;

@Mod.EventBusSubscriber(modid = Aspects.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAttributes {
    public static final DeferredRegister<Attribute> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Aspects.MOD_ID);

    public static final RegistryObject<Attribute> INFERNUM_ASPECT =
            create("infernum_aspect", 0D, 0D);

    public static final RegistryObject<Attribute> GLACIUS_ASPECT =
            create("glacius_aspect", 0D, 0D);

    public static final RegistryObject<Attribute> TERRA_ASPECT =
            create("terra_aspect", 0D, 0D);

    public static final RegistryObject<Attribute> TEMPESTUS_ASPECT =
            create("tempestus_aspect", 0D, 0D);

    public static final RegistryObject<Attribute> ARCANI_ASPECT =
            create("arcani_aspect", 0D, 0D);

    public static final RegistryObject<Attribute> UMBRE_ASPECT =
            create("umbre_aspect", 0D, 0D);

    private static RegistryObject<Attribute> create(String name, double defaultValue, double minValue) {
        String descriptionId = "attribute.name." + Aspects.MOD_ID + "." + name;
        return REGISTRY.register(name, () -> new RangedAttribute(descriptionId, defaultValue, minValue, 1024.0D)
                .setSyncable(true));
    }

    @SubscribeEvent
    public static void attachAttributes(EntityAttributeModificationEvent event) {
        REGISTRY.getEntries().stream()
                .map(RegistryObject::get)
                .forEach(attribute -> event.add(EntityType.PLAYER, attribute));
    }
}
