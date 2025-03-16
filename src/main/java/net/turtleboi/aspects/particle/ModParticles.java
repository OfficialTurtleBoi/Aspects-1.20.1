package net.turtleboi.aspects.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.aspects.Aspects;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Aspects.MOD_ID);

    public static final Supplier<SimpleParticleType> NONE_PARTICLES =
            PARTICLE_TYPES.register("none_particles", () -> new SimpleParticleType(true));

    public static final Supplier<SimpleParticleType> CHILLED_PARTICLES =
            PARTICLE_TYPES.register("chilled_particles", () -> new SimpleParticleType(true));

    public static final Supplier<SimpleParticleType> STUNNED_PARTICLES =
            PARTICLE_TYPES.register("stunned_particles", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus){
        PARTICLE_TYPES.register(eventBus);
    }
}
