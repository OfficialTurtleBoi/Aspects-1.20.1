package net.turtleboi.aspects.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.turtleboi.turtlecore.TurtleCore;
import net.turtleboi.turtlecore.client.data.SpikeData;
import net.turtleboi.turtlecore.client.renderer.IceSpikeRenderer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ColdAuraRenderer {
    private final long spawnTime;
    private final int totalAnimationTime;
    private final int amplifier;

    public static final Map<UUID, List<ColdAuraRenderer>> ENTITY_AURAS = new ConcurrentHashMap<>();
    private final SpikeData[] spikesSequence;

    public ColdAuraRenderer(long currentTime, int totalAnimationTime, int amplifier) {
        this.spawnTime = currentTime;
        this.totalAnimationTime = totalAnimationTime;
        this.amplifier = amplifier;

        Random random = new Random();
        SpikeData[] subset = SpikeData.getPremadeSpikesByIds(random, 1, 2, 3, 4, 5, 6);
        List<SpikeData> pool = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            pool.addAll(Arrays.asList(subset));
        }

        Collections.shuffle(pool, random);
        spikesSequence = pool.toArray(new SpikeData[0]);
    }

    public static void addAuraForEntity(LivingEntity livingEntity, long currentTime, int totalAnimationTime, int amplifier) {
        UUID uuid = livingEntity.getUUID();
        List<ColdAuraRenderer> cubeList = ENTITY_AURAS.computeIfAbsent(uuid, key -> new CopyOnWriteArrayList<>());
        cubeList.add(new ColdAuraRenderer(currentTime, totalAnimationTime, amplifier));
    }

    public static void renderAuras(MultiBufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        UUID uuid = livingEntity.getUUID();
        List<ColdAuraRenderer> spikeList = ENTITY_AURAS.get(uuid);
        if (spikeList != null) {
            spikeList.removeIf(ColdAuraRenderer::isExpired);
            for (ColdAuraRenderer cube : spikeList) {
                cube.renderIceSpikes(bufferSource, poseStack, livingEntity, partialTicks);
            }
        }
    }

    public void renderIceSpikes(MultiBufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
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

        int spikeCount = 4 + amplifier;
        for (int i = 0; i < spikeCount; i++) {
            SpikeData spike = spikesSequence[i % spikesSequence.length];
            float zTranslation = 10 * ((spike.zTranslationOffset * livingEntity.getBbHeight()) - (livingEntity.getBbHeight() * 0.375f));

            float baseAngle = (360F / spikeCount) * i;
            float xAngle = spike.xAngleOffset;
            float yAngle = spike.yAngleOffset;
            float zAngle = spike.zAngleOffset;

            zAngle = baseAngle + zAngle;
            IceSpikeRenderer.renderSpike(poseStack, bufferSource, spike, amplifier, zTranslation, xAngle, yAngle, zAngle, vertexAlpha);
        }

        poseStack.popPose();
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - spawnTime) > (totalAnimationTime * 50L);
    }
}
