package net.turtleboi.aspects.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.item.ModItems;
import net.turtleboi.turtlecore.init.CoreAttributeModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AspectUtil {
    public static final String ASPECT_KEY = "aspect";

    public static final String INFERNUM_ASPECT = "infernum";
    public static final String GLACIUS_ASPECT = "glacius";
    public static final String TERRA_ASPECT = "terra";
    public static final String TEMPESTAS_ASPECT = "tempestas";
    public static final String ARCANI_ASPECT = "arcani";
    public static final String UMBRE_ASPECT = "umbre";


    public static boolean hasAspect(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(ASPECT_KEY, Tag.TAG_STRING);
    }

    public static void setAspect(ItemStack stack, String aspectName) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(ASPECT_KEY, aspectName);
    }

    public static String getAspect(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(ASPECT_KEY, Tag.TAG_STRING)
                ? tag.getString(ASPECT_KEY)
                : null;
    }

    public static void removeAspect(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null) {
            stack.getTag().remove(ASPECT_KEY);
        }
    }

    public static String getAspectFromRune(ItemStack runeStack) {
        if (runeStack.getItem() == ModItems.INFERNUM_RUNE.get()) {
            return INFERNUM_ASPECT;
        } else if (runeStack.getItem() == ModItems.GLACIUS_RUNE.get()) {
            return GLACIUS_ASPECT;
        } else if (runeStack.getItem() == ModItems.TERRA_RUNE.get()) {
            return TERRA_ASPECT;
        } else if (runeStack.getItem() == ModItems.TEMPESTAS_RUNE.get()) {
            return TEMPESTAS_ASPECT;
        } else if (runeStack.getItem() == ModItems.ARCANI_RUNE.get()) {
            return ARCANI_ASPECT;
        } else if (runeStack.getItem() == ModItems.UMBRE_RUNE.get()) {
            return UMBRE_ASPECT;
        } else {
            return null;
        }
    }

    public static TextColor getAspectColor(String aspect) {
        return switch (aspect) {
            case INFERNUM_ASPECT -> TextColor.fromRgb(0xFF5733 );
            case GLACIUS_ASPECT -> TextColor.fromRgb(0x96fff7);
            case TERRA_ASPECT -> TextColor.fromRgb(0x9f7b06);
            case TEMPESTAS_ASPECT -> TextColor.fromRgb(0xcbfb5d);
            case ARCANI_ASPECT -> TextColor.fromRgb(0xca6aee);
            case UMBRE_ASPECT -> TextColor.fromRgb(0xa7a1b0);
            default -> TextColor.fromRgb(0xFFFFFF);
        };
    }

    public static void applyAspectAttributes(LivingEntity entity) {
        for (ItemStack armor : entity.getArmorSlots()) {
            String aspect = AspectUtil.getAspect(armor);
            if (aspect != null && !aspect.isEmpty()) {
                switch (aspect) {
                    case INFERNUM_ASPECT:
                        //System.out.println("Applying Infernum from " + armor.getDescriptionId());
                        CoreAttributeModifiers.applyPermanentModifier(
                                entity,
                                ModAttributes.INFERNUM_ASPECT.get(),
                                armor.getDescriptionId() + INFERNUM_ASPECT + "inferum_infusion",
                                1.0,
                                AttributeModifier.Operation.ADDITION);
                        break;
                    case GLACIUS_ASPECT:
                        //System.out.println("Applying Glacius from " + armor.getDescriptionId());
                        CoreAttributeModifiers.applyPermanentModifier(
                                entity,
                                ModAttributes.GLACIUS_ASPECT.get(),
                                armor.getDescriptionId() + GLACIUS_ASPECT +"_infusion",
                                1.0,
                                AttributeModifier.Operation.ADDITION);
                        CoreAttributeModifiers.applyPermanentModifier(
                                entity,
                                Attributes.MAX_HEALTH,
                                armor.getDescriptionId() + GLACIUS_ASPECT +"_health_infusion",
                                2.0,
                                AttributeModifier.Operation.ADDITION);
                        break;
                    case TERRA_ASPECT:
                        //System.out.println("Applying Terra from " + armor.getDescriptionId());
                        CoreAttributeModifiers.applyPermanentModifier(
                                entity,
                                ModAttributes.TERRA_ASPECT.get(),
                                armor.getDescriptionId() + TERRA_ASPECT + "_infusion",
                                1.0,
                                AttributeModifier.Operation.ADDITION);
                        double arcaniAmplifier;
                        double arcaniFactor = 1;
                        if (entity.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                            arcaniAmplifier = entity.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
                            arcaniFactor = 1 + (arcaniAmplifier / 4.0);
                        }
                        CoreAttributeModifiers.applyPermanentModifier(
                                entity,
                                Attributes.ARMOR,
                                armor.getDescriptionId() + TERRA_ASPECT + "_armor_infusion",
                                2.0,
                                AttributeModifier.Operation.ADDITION);
                        CoreAttributeModifiers.applyPermanentModifier(
                                entity,
                                Attributes.ARMOR,
                                armor.getDescriptionId() + TERRA_ASPECT + "_armor_percent_infusion",
                                0.25 * arcaniFactor,
                                AttributeModifier.Operation.MULTIPLY_TOTAL);
                        break;
                    case TEMPESTAS_ASPECT:
                        //System.out.println("Applying Glacius from " + armor.getDescriptionId());
                        CoreAttributeModifiers.applyPermanentModifier(
                                entity,
                                ModAttributes.TEMPESTUS_ASPECT.get(),
                                armor.getDescriptionId() + TEMPESTAS_ASPECT + "_infusion",
                                1.0,
                                AttributeModifier.Operation.ADDITION);
                        CoreAttributeModifiers.applyPermanentModifier(
                                entity,
                                Attributes.ATTACK_KNOCKBACK,
                                armor.getDescriptionId() + TEMPESTAS_ASPECT + "_knockback_infusion",
                                0.25,
                                AttributeModifier.Operation.ADDITION);
                        break;
                    case ARCANI_ASPECT:
                        //System.out.println("Applying Arcani from " + armor.getDescriptionId());
                        CoreAttributeModifiers.applyPermanentModifier(
                                entity,
                                ModAttributes.ARCANI_ASPECT.get(),
                                armor.getDescriptionId() + ARCANI_ASPECT + "_infusion",
                                1.0,
                                AttributeModifier.Operation.ADDITION);
                        break;
                    case UMBRE_ASPECT:
                        //System.out.println("Applying Umbre from " + armor.getDescriptionId());
                        CoreAttributeModifiers.applyPermanentModifier(
                                entity,
                                ModAttributes.UMBRE_ASPECT.get(),
                                armor.getDescriptionId() + UMBRE_ASPECT + "_infusion",
                                1.0,
                                AttributeModifier.Operation.ADDITION);
                        if (getAverageSurroundingLight(entity) <= 0 + entity.getAttribute(ModAttributes.UMBRE_ASPECT.get()).getValue()) {
                            if(!entity.hasEffect(MobEffects.NIGHT_VISION) || (entity.hasEffect(MobEffects.NIGHT_VISION) && entity.getEffect(MobEffects.NIGHT_VISION).getDuration() < 300)){
                                entity.addEffect(new MobEffectInstance(
                                        MobEffects.NIGHT_VISION,
                                        600,
                                        0,
                                        true,
                                        true,
                                        true
                                ));
                            }
                            CoreAttributeModifiers.applyPermanentModifier(
                                    entity,
                                    Attributes.ATTACK_SPEED,
                                    armor.getDescriptionId() + UMBRE_ASPECT + "_attack_speed_infusion",
                                    0.2,
                                    AttributeModifier.Operation.MULTIPLY_TOTAL);
                            CoreAttributeModifiers.applyPermanentModifier(
                                    entity,
                                    Attributes.MOVEMENT_SPEED,
                                    armor.getDescriptionId() + UMBRE_ASPECT + "_speed_infusion",
                                    0.1,
                                    AttributeModifier.Operation.MULTIPLY_TOTAL);
                        } else {
                            CoreAttributeModifiers.removeModifier(
                                    entity,
                                    Attributes.ATTACK_SPEED,
                                    armor.getDescriptionId() + UMBRE_ASPECT + "_attack_speed_infusion"
                            );
                            CoreAttributeModifiers.removeModifier(
                                    entity,
                                    Attributes.MOVEMENT_SPEED,
                                    armor.getDescriptionId() + UMBRE_ASPECT + "_speed_infusion"
                            );
                        }
                        break;
                }
            }
        }
    }

    public static void clearAspectAttributes(LivingEntity entity) {
        //System.out.println("Removing player's attributes");
        entity.getAttributes().getSyncableAttributes().forEach(instance -> {
            Attribute attribute = instance.getAttribute();
            CoreAttributeModifiers.removeModifiersBySuffix(entity, attribute, "_infusion");
        });
    }

    public static void updateAspectAttributes(LivingEntity entity) {
        clearAspectAttributes(entity);
        applyAspectAttributes(entity);
    }

    public static float getAverageSurroundingLight(LivingEntity entity) {
        Level level = entity.level();
        BlockPos playerPos = new BlockPos((int) entity.getX(), (int) entity.getEyeY(), (int) entity.getZ());
        return level.getMaxLocalRawBrightness(playerPos);
    }
}
