package net.turtleboi.aspects.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ColdAuraRenderer {
    private final long spawnTime;
    private final int totalAnimationTime;
    private final double amplifier;
    public static final ResourceLocation SNOW_CUBE_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/snow.png");

    public static final Map<UUID, List<ColdAuraRenderer>> ENTITY_AURAS = new ConcurrentHashMap<>();

    public ColdAuraRenderer(long currentTime, int totalAnimationTime, double amplifier) {
        this.spawnTime = currentTime;
        this.totalAnimationTime = totalAnimationTime;
        this.amplifier = amplifier;
    }

    public static void addAuraForEntity(LivingEntity livingEntity, long currentTime, int totalAnimationTime, double amplifier) {
        UUID uuid = livingEntity.getUUID();
        List<ColdAuraRenderer> cubeList = ENTITY_AURAS.computeIfAbsent(uuid, key -> new CopyOnWriteArrayList<>());
        cubeList.add(new ColdAuraRenderer(currentTime, totalAnimationTime, amplifier));
    }

    public static void renderAuras(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        UUID uuid = livingEntity.getUUID();
        List<ColdAuraRenderer> cubeList = ENTITY_AURAS.get(uuid);
        if (cubeList != null) {
            cubeList.removeIf(ColdAuraRenderer::isExpired);
            for (ColdAuraRenderer cube : cubeList) {
                cube.renderCube(bufferSource, poseStack, livingEntity, partialTicks);
            }
        }
    }

    public void renderCube(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        poseStack.pushPose();
        float ticksElapsed = (System.currentTimeMillis() - spawnTime) / 50.0f;
        float tickCount = ticksElapsed + partialTicks;
        float initialTicks = 5.0f;
        float scale;

        if (tickCount < initialTicks) {
            scale = 0.25f * (tickCount / initialTicks);
        } else {
            scale = (float) (0.25f + ((tickCount - initialTicks) / (totalAnimationTime - initialTicks)) * (0.3f * amplifier));
        }

        float alpha;
        if (tickCount < (totalAnimationTime * 0.75f)) {
            alpha = 1.0f;
        } else if (tickCount > totalAnimationTime) {
            alpha = 0.0f;
        } else {
            alpha = 1.0f - ((tickCount - (totalAnimationTime * 0.75f)) / (totalAnimationTime - (totalAnimationTime * 0.75f)));
        }
        int vertexAlpha = (int)(alpha * 255.0f);

        poseStack.translate(0, livingEntity.getBbHeight() * 0.5, 0);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(-livingEntity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(SNOW_CUBE_TEXTURE));

        //Idk how to do this

        poseStack.popPose();
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, float u, float v, int vertexAlpha) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, vertexAlpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(pose, 0, 0, 1);
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - spawnTime) > (totalAnimationTime * 50L);
    }
}
