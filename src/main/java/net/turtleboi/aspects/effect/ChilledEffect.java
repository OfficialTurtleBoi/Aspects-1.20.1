package net.turtleboi.aspects.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.network.payloads.ParticleData;
import net.turtleboi.aspects.particle.ModParticles;
import net.turtleboi.aspects.util.AttributeModifierUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChilledEffect extends MobEffect {
    private final String attributeModifierName = "chilled_movement_speed";
    public ChilledEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity.tickCount % 5 == 0){
            for (int i = 0; i < (pAmplifier + 1); i++) {
                double offX = (pLivingEntity.level().random.nextDouble() - 0.5) * 0.5;
                double offY = pLivingEntity.getBbHeight() * pLivingEntity.level().random.nextDouble();
                double offZ = (pLivingEntity.level().random.nextDouble() - 0.5) * 0.5;
                ParticleData.spawnParticle(
                        ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "chilled_particles"),
                        pLivingEntity.getX() + offX,
                        pLivingEntity.getY() + offY,
                        pLivingEntity.getZ() + offZ);
            }
        }

        if (!pLivingEntity.level().isClientSide()) {
            if(pLivingEntity instanceof Mob mob) {
                AttributeModifierUtil.applyPermanentModifier(
                        mob,
                        Attributes.MOVEMENT_SPEED,
                        attributeModifierName,
                        -0.125 * (1 + pAmplifier),
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            }

            if(pLivingEntity instanceof Player player) {
                AttributeModifierUtil.applyPermanentModifier(
                        player,
                        Attributes.MOVEMENT_SPEED,
                        attributeModifierName,
                        -0.2 * (1 + pAmplifier),
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            }

            if (pAmplifier > 3){
                pLivingEntity.addEffect(new MobEffectInstance(ModEffects.FROZEN, 100, 0));
                pLivingEntity.removeEffect(ModEffects.CHILLED);
            }
        }
        return super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(@NotNull AttributeMap attributeMap) {
        super.removeAttributeModifiers(attributeMap);
        AttributeInstance instance = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (instance != null) {
            instance.getModifiers().stream()
                    .map(AttributeModifier::id)
                    .filter(id -> id.getPath().equals(attributeModifierName))
                    .forEach(instance::removeModifier);
        }
    }

    @Override
    public ParticleOptions createParticleOptions(MobEffectInstance effect) {
        return ModParticles.CHILLED_PARTICLES.get();
    }
}
