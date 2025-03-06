package net.turtleboi.aspects.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.turtleboi.aspects.Aspects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ColdAuraRenderer {
    private final long spawnTime;
    private final int totalAnimationTime;
    private final double amplifier;

    public static final Map<UUID, List<ColdAuraRenderer>> ENTITY_AURAS = new ConcurrentHashMap<>();
    private final SpikeData[] spikesSequence;

    public ColdAuraRenderer(long currentTime, int totalAnimationTime, double amplifier) {
        this.spawnTime = currentTime;
        this.totalAnimationTime = totalAnimationTime;
        this.amplifier = amplifier;

        Random random = new Random();
        List<SpikeData> list = new ArrayList<>();
        list.add(new SpikeData(5, 8, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike1.png"), random));
        list.add(new SpikeData(8, 11, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike2.png"), random));
        list.add(new SpikeData(8, 14, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike3.png"), random));
        list.add(new SpikeData(6, 11, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike4.png"), random));
        list.add(new SpikeData(6, 19, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike5.png"), random));
        list.add(new SpikeData(8, 8, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike6.png"), random));
        list.add(new SpikeData(5, 8, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike1.png"), random));
        list.add(new SpikeData(8, 11, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike2.png"), random));
        list.add(new SpikeData(8, 14, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike3.png"), random));
        list.add(new SpikeData(6, 11, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike4.png"), random));
        list.add(new SpikeData(6, 19, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike5.png"), random));
        list.add(new SpikeData(8, 8, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike6.png"), random));
        list.add(new SpikeData(5, 8, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike1.png"), random));
        list.add(new SpikeData(8, 11, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike2.png"), random));
        list.add(new SpikeData(8, 14, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike3.png"), random));
        list.add(new SpikeData(6, 11, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike4.png"), random));
        list.add(new SpikeData(6, 19, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike5.png"), random));
        list.add(new SpikeData(8, 8, ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/spike6.png"), random));

        Collections.shuffle(list, random);
        spikesSequence = list.toArray(new SpikeData[0]);
    }

    public static void addAuraForEntity(LivingEntity livingEntity, long currentTime, int totalAnimationTime, double amplifier) {
        UUID uuid = livingEntity.getUUID();
        List<ColdAuraRenderer> cubeList = ENTITY_AURAS.computeIfAbsent(uuid, key -> new CopyOnWriteArrayList<>());
        cubeList.add(new ColdAuraRenderer(currentTime, totalAnimationTime, amplifier));
    }

    public static void renderAuras(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        UUID uuid = livingEntity.getUUID();
        List<ColdAuraRenderer> spikeList = ENTITY_AURAS.get(uuid);
        if (spikeList != null) {
            spikeList.removeIf(ColdAuraRenderer::isExpired);
            for (ColdAuraRenderer cube : spikeList) {
                cube.renderIceSpikes(bufferSource, poseStack, livingEntity, partialTicks);
            }
        }
    }

    public void renderIceSpikes(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        poseStack.pushPose();
        float ticksElapsed = (System.currentTimeMillis() - spawnTime) / 50.0f;
        float tickCount = ticksElapsed + partialTicks;
        float initialTicks = 5.0f;
        float scale;

        if (tickCount < initialTicks) {
            double curve = 5;
            double normalized = (Math.exp(curve * (tickCount / initialTicks)) - 1) / (Math.exp(curve) - 1);
            scale = (float) normalized;
        } else {
            scale = 1f;
        }

        float alpha;
        if (tickCount < (totalAnimationTime * 0.9f)) {
            alpha = 1.0f;
        } else if (tickCount > totalAnimationTime) {
            alpha = 0.0f;
        } else {
            alpha = 1.0f - ((tickCount - (totalAnimationTime * 0.9f)) / (totalAnimationTime - (totalAnimationTime * 0.9f)));
        }
        int vertexAlpha = (int)(alpha * 255.0f);
        float adjustedScale = scale / 15;

        poseStack.translate(0, livingEntity.getBbHeight() * 0.5, 0);
        poseStack.scale(adjustedScale, adjustedScale, adjustedScale);
        poseStack.mulPose(Axis.YP.rotationDegrees(-livingEntity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));

        int spikeCount = (int) (4 + amplifier);
        for (int i = 0; i < spikeCount; i++) {
            SpikeData spike = spikesSequence[i % spikesSequence.length];
            float zTranslation = 10 * ((spike.zTranslationOffset * livingEntity.getBbHeight()) - (livingEntity.getBbHeight() * 0.375f));

            float baseAngle = (360F / spikeCount) * i;
            float xAngle = spike.xAngleOffset;
            float yAngle = spike.yAngleOffset;
            float zAngle = spike.zAngleOffset;

            zAngle = baseAngle + zAngle;
            renderSpike(poseStack, bufferSource, spike, zTranslation, xAngle, yAngle, zAngle, vertexAlpha);
        }

        poseStack.popPose();
    }

    private void renderSpike(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, SpikeData spike, float zTranslation, float xAngle, float yAngle, float zAngle, int vertexAlpha) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(spike.texture));
        poseStack.pushPose();
        poseStack.translate(0, 0, zTranslation);
        poseStack.scale(1, 1, (float) (1 * (amplifier / 4)));
        poseStack.mulPose(Axis.XP.rotationDegrees(xAngle));
        poseStack.mulPose(Axis.YP.rotationDegrees(yAngle));
        poseStack.mulPose(Axis.ZP.rotationDegrees(zAngle));
        renderSpikeQuad(poseStack, consumer, spike.width, spike.height, vertexAlpha);
        poseStack.popPose();
    }

    private void renderSpikeQuad(PoseStack postStack, VertexConsumer vertexConsumer, float width, float height, int vertexAlpha) {
        float halfWidth = width / 2.0f;
        vertex(postStack.last(), vertexConsumer, -halfWidth, 0, 0, 0, 0, vertexAlpha);
        vertex(postStack.last(), vertexConsumer, halfWidth, 0, 0, 1, 0, vertexAlpha);
        vertex(postStack.last(), vertexConsumer, halfWidth, height, 0, 1, 1, vertexAlpha);
        vertex(postStack.last(), vertexConsumer, -halfWidth, height, 0, 0, 1, vertexAlpha);

        vertex(postStack.last(), vertexConsumer, -halfWidth, height, 0, 0, 1, vertexAlpha);
        vertex(postStack.last(), vertexConsumer, halfWidth, height, 0, 1, 1, vertexAlpha);
        vertex(postStack.last(), vertexConsumer, halfWidth, 0, 0, 1, 0, vertexAlpha);
        vertex(postStack.last(), vertexConsumer, -halfWidth, 0, 0, 0, 0, vertexAlpha);
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

    private static class SpikeData {
        final float width;
        final float height;
        final ResourceLocation texture;
        final float zTranslationOffset;
        final float xAngleOffset;
        final float yAngleOffset;
        final float zAngleOffset;

        SpikeData(float width, float height, ResourceLocation texture, Random random) {
            this.width = width;
            this.height = height;
            this.texture = texture;
            this.zTranslationOffset = random.nextFloat();
            this.xAngleOffset = (random.nextFloat() * 60F) - 30F;
            this.yAngleOffset = (random.nextFloat() * 10F) - 5F;
            this.zAngleOffset = (random.nextFloat() * 30F) - 15F;
        }
    }
}
