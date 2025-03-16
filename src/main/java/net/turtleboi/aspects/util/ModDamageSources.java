package net.turtleboi.aspects.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.turtleboi.aspects.Aspects;
import org.jetbrains.annotations.Nullable;

public class ModDamageSources {
    public static final ResourceLocation FROZEN_DAMAGE_ID = new ResourceLocation(Aspects.MOD_ID, "frozen");

    public static DamageSource frozenDamage(Level level, @Nullable Entity attacker, @Nullable Entity directCause) {
        Holder<DamageType> holder = frozenDamageType(level);
        return new DamageSource(holder, attacker, directCause, attacker != null ? attacker.position() : null);
    }

    public static Holder<DamageType> frozenDamageType(Level level) {
        return level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, FROZEN_DAMAGE_ID));
    }
}
