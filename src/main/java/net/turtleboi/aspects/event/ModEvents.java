package net.turtleboi.aspects.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
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
        String name = event.getName();
        int i = 5;


        if ((isRune(right)) && (left.getItem() instanceof ArmorItem)) {
            ItemStack output = left.copy();

            if (name != null && !StringUtil.isBlank(name)) {
                if (!name.equals(left.getHoverName().getString())) {
                    i += 1;

                    output.set(DataComponents.CUSTOM_NAME, Component.literal(name));
                }
            }else if (left.has(DataComponents.CUSTOM_NAME)) {

                i += 1;
                output.remove(DataComponents.CUSTOM_NAME);

            }

            String aspectData = AspectHelper.getAspect(right);
            if (aspectData == null) {
                String runeAspect = AspectHelper.getAspectFromRune(right);
                AspectHelper.setAspect(output, runeAspect);

                event.setMaterialCost(1);
                event.setOutput(output);
                event.setCost(i);
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
