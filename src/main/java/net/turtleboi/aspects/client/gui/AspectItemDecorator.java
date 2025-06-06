package net.turtleboi.aspects.client.gui;


import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.util.AspectUtil;

public class AspectItemDecorator implements IItemDecorator {

    private static ResourceLocation getAspectOverlay(ItemStack itemStack) {
        return new ResourceLocation(Aspects.MOD_ID, "textures/gui/" + AspectUtil.getAspect(itemStack) + "_overlay.png");
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int i, int i1) {
        if (!itemStack.isEmpty() && AspectUtil.hasAspect(itemStack)) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            guiGraphics.blit(getAspectOverlay(itemStack), i, i1, 0, 0, 16, 16, 16, 16);
            guiGraphics.pose().popPose();
            return true;
        }
        return false;
    }
}
