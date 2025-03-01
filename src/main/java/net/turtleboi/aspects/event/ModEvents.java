package net.turtleboi.aspects.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.item.ModItems;
import net.turtleboi.aspects.util.AspectHelper;

@EventBusSubscriber(modid = Aspects.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (isRune(right)) {
            String aspectData = AspectHelper.getAspect(right);
            if (aspectData == null) {
                ItemStack output = left.copy();
                String runeAspect = AspectHelper.getAspectFromRune(right);
                AspectHelper.setAspect(output, runeAspect);
                event.setOutput(output);
                event.setCost(1);
            }
        }
    }

    private static boolean isRune(ItemStack stack) {
        return stack.getItem() == ModItems.INFERNUM_RUNE.get() ||
                stack.getItem() == ModItems.GLACIUS_RUNE.get() ||
                stack.getItem() == ModItems.TERRA_RUNE.get() ||
                stack.getItem() == ModItems.TEMPESTAS_RUNE.get() ||
                stack.getItem() == ModItems.ARCANI_RUNE.get() ||
                stack.getItem() == ModItems.UMBRE_RUNE.get();
    }
}
