package net.turtleboi.aspects.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.turtleboi.aspects.util.ModAttributes;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ChilledEffect extends MobEffect {
    private final String attributeModifierName = "chilled_movement_speed";
    public ChilledEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity.tickCount % 5 == 0){
            for (int i = 0; i < ((pAmplifier + 1) * 2); i++) {
                double offX = (pLivingEntity.level().random.nextDouble() - 0.5) * 0.5;
                double offY = pLivingEntity.getBbHeight() * pLivingEntity.level().random.nextDouble();
                double offZ = (pLivingEntity.level().random.nextDouble() - 0.5) * 0.5;
                ParticleData.spawnParticle(
                        ModParticles.CHILLED_PARTICLES.get(),
                        pLivingEntity.getX() + offX,
                        pLivingEntity.getY() + offY,
                        pLivingEntity.getZ() + offZ,
                        0,0,0);
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

            int freezeDuration = 100;
            if (pLivingEntity.getPersistentData().hasUUID("ChilledBy")) {
                UUID chilledByUUID = pLivingEntity.getPersistentData().getUUID("ChilledBy");
                if (pLivingEntity.level() instanceof ServerLevel serverLevel) {
                    Entity chilledByEntity = serverLevel.getEntity(chilledByUUID);
                    if (chilledByEntity instanceof LivingEntity chillerEntity) {
                        double arcaniAmplifier;
                        double arcaniFactor = 1;
                        if (chillerEntity.getAttribute(ModAttributes.ARCANI_ASPECT) != null) {
                            arcaniAmplifier = chillerEntity.getAttribute(ModAttributes.ARCANI_ASPECT).getValue();
                            arcaniFactor = 1 + (arcaniAmplifier / 4.0);
                        }
                        freezeDuration = (int) (100 * arcaniFactor);

                        if (chillerEntity.getAttribute(ModAttributes.TERRA_ASPECT) != null) {
                            double playerTerraAmplifier = chillerEntity.getAttributeValue(ModAttributes.TERRA_ASPECT);
                            if (!chillerEntity.hasEffect(MobEffects.DAMAGE_RESISTANCE)){
                                chillerEntity.addEffect(
                                        new MobEffectInstance(
                                                MobEffects.DAMAGE_RESISTANCE,
                                                (int) (600 * playerTerraAmplifier * arcaniFactor),
                                                pAmplifier,
                                                false,
                                                true,
                                                true
                                        ));
                            } else if (chillerEntity.hasEffect(MobEffects.DAMAGE_RESISTANCE) &&
                                    chillerEntity.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() <= pAmplifier) {
                                chillerEntity.addEffect(
                                        new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                                                (int) (600 * playerTerraAmplifier * arcaniFactor),
                                                pAmplifier,
                                                false,
                                                true,
                                                true));
                            }
                        }
                    }
                }
            }

            if (pAmplifier > 3) {
                pLivingEntity.removeEffect(ModEffects.CHILLED);
                pLivingEntity.addEffect(new MobEffectInstance(ModEffects.FROZEN, freezeDuration, 0));
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
