package net.turtleboi.aspects.effect.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.aspects.effect.ModEffects;
import net.turtleboi.aspects.network.ModNetworking;
import net.turtleboi.aspects.network.packets.SendParticlesS2C;
import net.turtleboi.aspects.particle.ModParticles;
import net.turtleboi.aspects.util.AttributeModifierUtil;
import net.turtleboi.aspects.util.ModAttributes;

import java.util.UUID;

public class ChilledEffect extends MobEffect {
    private final String attributeModifierName = "chilled_movement_speed";
    public ChilledEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.hasEffect(ModEffects.CHILLED.get())) {
            MobEffectInstance originalInstance = pLivingEntity.getEffect(ModEffects.CHILLED.get());
            if (originalInstance.isVisible()) {
                originalInstance.update(
                        new MobEffectInstance(
                                ModEffects.CHILLED.get(),
                                originalInstance.getDuration(),
                                originalInstance.getAmplifier(),
                                originalInstance.isAmbient(),
                                false,
                                originalInstance.showIcon()));
            }
        }

        if (!pLivingEntity.level().isClientSide && pLivingEntity.tickCount % 5 == 0){
            for (int i = 0; i < ((pAmplifier + 1) * 2); i++) {
                double offX = (pLivingEntity.level().random.nextDouble() - 0.5) * 0.5;
                double offY = pLivingEntity.getBbHeight() * pLivingEntity.level().random.nextDouble();
                double offZ = (pLivingEntity.level().random.nextDouble() - 0.5) * 0.5;
                ModNetworking.sendToNear(new SendParticlesS2C(
                        ModParticles.CHILLED_PARTICLES.get(),
                        pLivingEntity.getX() + offX,
                        pLivingEntity.getY() + offY,
                        pLivingEntity.getZ() + offZ,
                        0,0,0), pLivingEntity);
            }
        }

        if (!pLivingEntity.level().isClientSide()) {
            if(pLivingEntity instanceof Mob mob) {
                AttributeModifierUtil.applyPermanentModifier(
                        mob,
                        Attributes.MOVEMENT_SPEED,
                        attributeModifierName,
                        -0.125 * (1 + pAmplifier),
                        AttributeModifier.Operation.MULTIPLY_TOTAL);
            }

            if(pLivingEntity instanceof Player player) {
                AttributeModifierUtil.applyPermanentModifier(
                        player,
                        Attributes.MOVEMENT_SPEED,
                        attributeModifierName,
                        -0.2 * (1 + pAmplifier),
                        AttributeModifier.Operation.MULTIPLY_TOTAL);
            }

            int freezeDuration = 100;
            if (pLivingEntity.getPersistentData().hasUUID("ChilledBy")) {
                UUID chilledByUUID = pLivingEntity.getPersistentData().getUUID("ChilledBy");
                if (pLivingEntity.level() instanceof ServerLevel serverLevel) {
                    Entity chilledByEntity = serverLevel.getEntity(chilledByUUID);
                    if (chilledByEntity instanceof LivingEntity chillerEntity) {
                        double arcaniAmplifier;
                        double arcaniFactor = 1;
                        if (chillerEntity.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                            arcaniAmplifier = chillerEntity.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
                            arcaniFactor = 1 + (arcaniAmplifier / 4.0);
                        }
                        freezeDuration = (int) (100 * arcaniFactor);

                        if (chillerEntity.getAttribute(ModAttributes.TERRA_ASPECT.get()) != null) {
                            double playerTerraAmplifier = chillerEntity.getAttributeValue(ModAttributes.TERRA_ASPECT.get());
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
                pLivingEntity.removeEffect(ModEffects.CHILLED.get());
                pLivingEntity.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), freezeDuration, 0));
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        AttributeModifierUtil.removeModifier(
                pLivingEntity,
                Attributes.MOVEMENT_SPEED,
                attributeModifierName);
    }
}
