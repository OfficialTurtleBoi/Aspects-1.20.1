package net.turtleboi.aspects.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.entity.entities.SingularityEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SingularityRenderer extends EntityRenderer<SingularityEntity> {
    public SingularityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(SingularityEntity singularityEntity) {
        int textureIndex = singularityEntity.getTextureIndex();
        return new ResourceLocation(Aspects.MOD_ID, "textures/entity/singularity/singularityentity_" + textureIndex + ".png");
    }

    @Override
    public void render(@NotNull SingularityEntity singularityEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            double playerX = player.getX();
            double playerZ = player.getZ();
            double entityX = singularityEntity.getX();
            double entityZ = singularityEntity.getZ();
            double dirX = playerX - entityX;
            double dirZ = playerZ - entityZ;

            float yaw = (float) (Math.atan2(dirZ, dirX) * (180 / Math.PI)) - 90.0F;

            float scale = 0.2f;
            poseStack.pushPose();
            poseStack.translate(0, singularityEntity.getBbHeight() * 0.5, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
            poseStack.scale(scale, scale, scale);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(getTextureLocation(singularityEntity)));
            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();
            Matrix3f normalMatrix = pose.normal();

            vertex(vertexConsumer, matrix, normalMatrix, -4, -4, 0, 0, 0, 255, 255, 255, 224);
            vertex(vertexConsumer, matrix, normalMatrix, 4, -4, 0, 1, 0, 255, 255, 255, 224);
            vertex(vertexConsumer, matrix, normalMatrix, 4, 4, 0, 1, 1, 255, 255, 255, 224);
            vertex(vertexConsumer, matrix, normalMatrix, -4, 4, 0, 0, 1, 255, 255, 255, 224);

            vertex(vertexConsumer, matrix, normalMatrix, -4, 4, 0, 0, 1, 255, 255, 255, 224);
            vertex(vertexConsumer, matrix, normalMatrix, 4, 4, 0, 1, 1, 255, 255, 255, 224);
            vertex(vertexConsumer, matrix, normalMatrix, 4, -4, 0, 1, 0, 255, 255, 255, 224);
            vertex(vertexConsumer, matrix, normalMatrix, -4, -4, 0, 0, 0, 255, 255, 255, 224);

            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
        super.render(singularityEntity, entityYaw, partialTicks, poseStack, bufferSource, light);
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix, Matrix3f normalMatrix,
                               int x, int y, int z, float u, float v, int red, int green, int blue, int vertexAlpha) {
        vertexConsumer.vertex(matrix, (float)x, (float)y, (float)z)
                .color(red, green, blue, vertexAlpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normalMatrix,0, 0, 1)
                .endVertex();;
    }
}
