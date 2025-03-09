package net.turtleboi.aspects.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.entity.entities.SingularityEntity;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Aspects.MOD_ID);

    public static final Supplier<EntityType<SingularityEntity>> SINGULARITY =
            ENTITY_TYPES.register("singularity", () -> EntityType.Builder.of(SingularityEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).build("singularity"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
