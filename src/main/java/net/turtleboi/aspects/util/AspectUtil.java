package net.turtleboi.aspects.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.component.ModDataComponents;
import net.turtleboi.aspects.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AspectUtil {
    public static final String INFERNUM_ASPECT = "infernum";
    public static final String GLACIUS_ASPECT = "glacius";
    public static final String TERRA_ASPECT = "terra";
    public static final String TEMPESTAS_ASPECT = "tempestas";
    public static final String ARCANI_ASPECT = "arcani";
    public static final String UMBRE_ASPECT = "umbre";

    public static boolean hasAspect(ItemStack stack) {
        return stack.get(ModDataComponents.ASPECT_STRING) != null;
    }

    public static void setAspect(ItemStack stack, String aspectName) {
        stack.set(ModDataComponents.ASPECT_STRING, aspectName);
    }

    public static String getAspect(ItemStack stack) {
        if (stack.get(ModDataComponents.ASPECT_STRING) != null){
            return stack.get(ModDataComponents.ASPECT_STRING);
        } else {
            return null;
        }
    }

    public static void removeAspect(ItemStack stack){
        stack.set(ModDataComponents.ASPECT_STRING, null);
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

    public static void setIgnitor(LivingEntity livingEntity, Entity ignitor){
        if (!livingEntity.getPersistentData().contains("IgnitedBy")) {
            //System.out.println(livingEntity + " ignited by " + ignitor + "!");
            livingEntity.getPersistentData().putUUID("IgnitedBy", ignitor.getUUID());
        }
    }

    public static void setChiller(LivingEntity livingEntity, Entity chiller){
        if (!livingEntity.getPersistentData().contains("ChilledBy")) {
            //System.out.println(livingEntity + " chilled by " + chiller + "!");
            livingEntity.getPersistentData().putUUID("ChilledBy", chiller.getUUID());
        }
    }

    public static void setFrozen(LivingEntity livingEntity, UUID uuid){
        if (!livingEntity.getPersistentData().contains("FrozenBy")) {
            //System.out.println(livingEntity + " was frozen by" + uuid.toString() + "!");
            livingEntity.getPersistentData().putUUID("FrozenBy", uuid);
        }
    }

    public static void setStunner(LivingEntity livingEntity, Entity stunner){
        if (!livingEntity.getPersistentData().contains("StunnedBy")) {
            //System.out.println(livingEntity + " stunned by " + stunner + "!");
            livingEntity.getPersistentData().putUUID("StunnedBy", stunner.getUUID());
        }
    }

    public static void applyAspectAttributes(LivingEntity entity) {
        for (ItemStack armor : entity.getArmorSlots()) {
            String aspect = AspectUtil.getAspect(armor);
            if (aspect != null && !aspect.isEmpty()) {
                switch (aspect) {
                    case INFERNUM_ASPECT:
                        //System.out.println("Applying Infernum from " + armor.getDescriptionId());
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.INFERNUM_ASPECT,
                                armor.getDescriptionId() + INFERNUM_ASPECT + "inferum_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        break;
                    case GLACIUS_ASPECT:
                        //System.out.println("Applying Glacius from " + armor.getDescriptionId());
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.GLACIUS_ASPECT,
                                armor.getDescriptionId() + GLACIUS_ASPECT +"_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                Attributes.MAX_HEALTH,
                                armor.getDescriptionId() + GLACIUS_ASPECT +"_health_infusion",
                                2.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        break;
                    case TERRA_ASPECT:
                        //System.out.println("Applying Terra from " + armor.getDescriptionId());
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.TERRA_ASPECT,
                                armor.getDescriptionId() + TERRA_ASPECT + "_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        double arcaniAmplifier;
                        double arcaniFactor = 1;
                        if (entity.getAttribute(ModAttributes.ARCANI_ASPECT) != null) {
                            arcaniAmplifier = entity.getAttribute(ModAttributes.ARCANI_ASPECT).getValue();
                            arcaniFactor = 1 + (arcaniAmplifier / 4.0);
                        }
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                Attributes.ARMOR,
                                armor.getDescriptionId() + TERRA_ASPECT + "_armor_infusion",
                                2.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                Attributes.ARMOR,
                                armor.getDescriptionId() + TERRA_ASPECT + "_armor_percent_infusion",
                                0.25 * arcaniFactor,
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                        break;
                    case TEMPESTAS_ASPECT:
                        //System.out.println("Applying Glacius from " + armor.getDescriptionId());
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.TEMPESTUS_ASPECT,
                                armor.getDescriptionId() + TEMPESTAS_ASPECT + "_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                Attributes.ATTACK_KNOCKBACK,
                                armor.getDescriptionId() + TEMPESTAS_ASPECT + "_knockback_infusion",
                                0.25,
                                AttributeModifier.Operation.ADD_VALUE);
                        break;
                    case ARCANI_ASPECT:
                        //System.out.println("Applying Arcani from " + armor.getDescriptionId());
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.ARCANI_ASPECT,
                                armor.getDescriptionId() + ARCANI_ASPECT + "_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        break;
                    case UMBRE_ASPECT:
                        //System.out.println("Applying Umbre from " + armor.getDescriptionId());
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.UMBRE_ASPECT,
                                armor.getDescriptionId() + UMBRE_ASPECT + "_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        if (getAverageSurroundingLight(entity) <= 0 + entity.getAttribute(ModAttributes.UMBRE_ASPECT).getValue()) {
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
                            AttributeModifierUtil.applyPermanentModifier(
                                    entity,
                                    Attributes.ATTACK_SPEED,
                                    armor.getDescriptionId() + UMBRE_ASPECT + "_attack_speed_infusion",
                                    0.2,
                                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                            AttributeModifierUtil.applyPermanentModifier(
                                    entity,
                                    Attributes.MOVEMENT_SPEED,
                                    armor.getDescriptionId() + UMBRE_ASPECT + "_speed_infusion",
                                    0.1,
                                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                            AttributeModifierUtil.applyPermanentModifier(
                                    entity,
                                    Attributes.MINING_EFFICIENCY,
                                    armor.getDescriptionId() + UMBRE_ASPECT + "_mining_speed_infusion",
                                    8,
                                    AttributeModifier.Operation.ADD_VALUE);
                        } else {
                            AttributeModifierUtil.removeModifier(
                                    entity,
                                    Attributes.ATTACK_SPEED,
                                    armor.getDescriptionId() + UMBRE_ASPECT + "_attack_speed_infusion"
                            );
                            AttributeModifierUtil.removeModifier(
                                    entity,
                                    Attributes.MOVEMENT_SPEED,
                                    armor.getDescriptionId() + UMBRE_ASPECT + "_speed_infusion"
                            );
                            AttributeModifierUtil.removeModifier(
                                    entity,
                                    Attributes.MOVEMENT_SPEED,
                                    armor.getDescriptionId() + UMBRE_ASPECT + "_mining_speed_infusion"
                            );
                        }
                        break;
                }
            }
        }
    }

    public static void clearAspectAttributes(LivingEntity entity) {
        for (AttributeInstance instance : entity.getAttributes().getSyncableAttributes()) {
            List<ResourceLocation> toRemove = new ArrayList<>();
            for (AttributeModifier modifier : instance.getModifiers()) {
                if (modifier.id().getNamespace().equals(Aspects.MOD_ID)
                        && modifier.id().getPath().endsWith("_infusion")) {
                    toRemove.add(modifier.id());
                }
            }
            for (ResourceLocation id : toRemove) {
                instance.removeModifier(id);
            }
        }
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
