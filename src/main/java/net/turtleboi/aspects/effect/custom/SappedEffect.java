package net.turtleboi.aspects.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.turtleboi.aspects.util.AttributeModifierUtil;

public class SappedEffect extends MobEffect {
    private final String attributeModifierName = "sapped_max_health";
    public SappedEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide()) {
            AttributeModifierUtil.applyPermanentModifier(
                    pLivingEntity,
                    Attributes.MAX_HEALTH,
                    attributeModifierName,
                    -2 * (pAmplifier + 1),
                    AttributeModifier.Operation.ADDITION);
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
                Attributes.MAX_HEALTH,
                attributeModifierName);
    }
}
