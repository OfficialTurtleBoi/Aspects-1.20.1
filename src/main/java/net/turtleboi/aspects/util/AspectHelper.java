package net.turtleboi.aspects.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.component.ModDataComponents;
import net.turtleboi.aspects.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public class AspectHelper {
    public static final String INFERNUM_ASPECT = "infernum";
    public static final String GLACIUS_ASPECT = "glacius";
    public static final String TERRA_ASPECT = "terra";
    public static final String TEMPESTAS_ASPECT = "tempestas";
    public static final String ARCANI_ASPECT = "arcani";
    public static final String UMBRE_ASPECT = "umbre";

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

    public static void applyAspectAttributes(LivingEntity entity) {
        for (ItemStack armor : entity.getArmorSlots()) {
            String aspect = AspectHelper.getAspect(armor);
            if (aspect != null && !aspect.isEmpty()) {
                switch (aspect) {
                    case INFERNUM_ASPECT:
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.INFERNUM_ASPECT,
                                armor.getDescriptionId() + "inferum_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        break;
                    case GLACIUS_ASPECT:
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.INFERNUM_ASPECT,
                                armor.getDescriptionId() + "glacius_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                Attributes.MAX_HEALTH,
                                armor.getDescriptionId() + "glacius_health_infusion",
                                2.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        break;
                    case TERRA_ASPECT:
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.TERRA_ASPECT,
                                armor.getDescriptionId() + "terra_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                Attributes.ARMOR,
                                armor.getDescriptionId() + "terra_armor_infusion",
                                3.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        break;
                    case TEMPESTAS_ASPECT:
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.TEMPESTUS_ASPECT,
                                armor.getDescriptionId() + "tempestas_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                Attributes.MINING_EFFICIENCY,
                                armor.getDescriptionId() + "tempestas_mining_efficiency_infusion",
                                0.5,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                        break;
                    case ARCANI_ASPECT:
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.ARCANI_ASPECT,
                                armor.getDescriptionId() + "arcani_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        break;
                    case UMBRE_ASPECT:
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                ModAttributes.UMBRE_ASPECT,
                                armor.getDescriptionId() + "umbre_infusion",
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE);
                        AttributeModifierUtil.applyPermanentModifier(
                                entity,
                                Attributes.ATTACK_SPEED,
                                armor.getDescriptionId() + "umbre_attack_speed_infusion",
                                0.2,
                                AttributeModifier.Operation.ADD_VALUE);
                        AttributeModifierUtil.applyPermanentModifier(entity,
                                Attributes.MOVEMENT_SPEED,
                                armor + "umbre_speed_infusion",
                                0.05,
                                AttributeModifier.Operation.ADD_VALUE);
                        break;
                }
            }
        }
    }

    public static void clearAspectAttributes(LivingEntity entity) {
        for (AttributeInstance instance : entity.getAttributes().getSyncableAttributes()) {
            List<ResourceLocation> toRemove = new ArrayList<>();
            for (AttributeModifier modifier : instance.getModifiers()) {
                if (modifier.id().getNamespace().equals(Aspects.MODID)
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
}
