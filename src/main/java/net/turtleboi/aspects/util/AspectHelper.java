package net.turtleboi.aspects.util;

import net.minecraft.world.item.ItemStack;
import net.turtleboi.aspects.component.ModDataComponents;
import net.turtleboi.aspects.item.ModItems;

public class AspectHelper {
    public static String INFERNUM_ASPECT = "Infernum";
    public static String GLACIUS_ASPECT = "Glacius";
    public static String TERRA_ASPECT = "Terra";
    public static String TEMPESTAS_ASPECT = "Tempestas";
    public static String ARCANI_ASPECT = "Arcani";
    public static String UMBRE_ASPECT = "Umbre";

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
}
