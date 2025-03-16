package net.turtleboi.aspects.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.turtleboi.aspects.block.entity.PedestalBlockEntity;
import net.turtleboi.aspects.client.model.PedestalModel;

public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity> {
    private final PedestalModel model;
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/block/stone.png");

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new PedestalModel(context.bakeLayer(PedestalModel.PEDESTAL_LAYER));
    }

    @Override
    public void render(PedestalBlockEntity pedestalBlockEntity, float v, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.XP.rotationDegrees(180));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
        VertexConsumer baseVertexConsumer = pBuffer.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
        model.renderToBuffer(pPoseStack, baseVertexConsumer, pPackedLight, pPackedOverlay, 1, 1, 1, 1);
        pPoseStack.popPose();
    }
}
