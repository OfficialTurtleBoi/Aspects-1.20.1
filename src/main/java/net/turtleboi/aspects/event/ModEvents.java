package net.turtleboi.aspects.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.item.ModItems;
import net.turtleboi.aspects.util.AspectHelper;
import net.turtleboi.aspects.util.ModTags;

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

            ItemStack aspectRune = right.copy();
            aspectRune.setCount(1);

            String aspectData = AspectHelper.getAspect(left);
            if (aspectData == null) {
                String runeAspect = AspectHelper.getAspectFromRune(aspectRune);
                AspectHelper.setAspect(output, runeAspect);

                event.setMaterialCost(1);
                event.setOutput(output);
                event.setCost(i);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            AspectHelper.updateAspectAttributes(player);
        }
    }

    private static boolean isRune(ItemStack itemStack) {
        return itemStack.is(ModTags.Items.RUNE_ITEMS);
    }
}
