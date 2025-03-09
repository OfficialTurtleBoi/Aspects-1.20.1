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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArcaneAuraRenderer {
    private final long spawnTime;
    private final int totalAnimationTime;
    private final double amplifier;
    private final int delayTicks;

    public static final ResourceLocation ARCANE_AURA_TEXTURE = ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "textures/gui/arcane_aura.png");
    public static final Map<UUID, List<ArcaneAuraRenderer>> ENTITY_AURAS = new ConcurrentHashMap<>();

    public ArcaneAuraRenderer(long currentTime, int totalAnimationTime, double amplifier, int delayTicks) {
        this.spawnTime = currentTime;
        this.totalAnimationTime = totalAnimationTime;
        this.amplifier = amplifier;
        this.delayTicks = delayTicks;
    }

    public static void addAuraForEntity(LivingEntity livingEntity, long currentTime, int totalAnimationTime, double amplifier) {
        UUID uuid = livingEntity.getUUID();
        List<ArcaneAuraRenderer> auraList = ENTITY_AURAS.computeIfAbsent(uuid, key -> new CopyOnWriteArrayList<>());
        int delay = auraList.size() * 10;
        auraList.add(new ArcaneAuraRenderer(currentTime, totalAnimationTime, amplifier, delay));
    }

    public static void renderAuras(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        UUID uuid = livingEntity.getUUID();
        List<ArcaneAuraRenderer> auraList = ENTITY_AURAS.get(uuid);
        if (auraList != null) {
            auraList.removeIf(ArcaneAuraRenderer::isExpired);

            for (ArcaneAuraRenderer aura : auraList) {
                aura.renderAura(bufferSource, poseStack, livingEntity, partialTicks);
            }
        }
    }

    public void renderAura(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        poseStack.pushPose();
        float ticksElapsed = (System.currentTimeMillis() % spawnTime) / 50.0f;
        float tickCount = ticksElapsed + partialTicks - delayTicks;

        if (tickCount < 0) {
            poseStack.popPose();
            return;
        }

        float initialTicks = 5.0f;
        float scale;

        float rotationSpeed;
        if (tickCount < initialTicks) {
            rotationSpeed = 3.0f;
        } else {
            rotationSpeed = (float) (10.0f * amplifier);
        }

        float rotationAngle = tickCount * rotationSpeed;

        if (tickCount < initialTicks) {
            scale = 0.25f * (tickCount / initialTicks);
        } else {
            scale = 0.25f;
        }

        float alpha;
        if (tickCount < (totalAnimationTime * 0.75f)) {
            alpha = 1.0f;
        } else if (tickCount > totalAnimationTime){
            alpha = 0.0f;
        } else {
            alpha = 1.0f - ((tickCount - (totalAnimationTime * 0.75f)) / (totalAnimationTime - (totalAnimationTime * 0.75f)));
        }

        int vertexAlpha = (int)(alpha * 255.0f);

        float yPosition;
        if (tickCount < initialTicks) {
            yPosition = (float) (livingEntity.getBbHeight() * 0.01);
        } else {
            yPosition = (float) Math.min(livingEntity.getBbHeight() * 1.1, ((livingEntity.getBbHeight() * 0.01) + ((tickCount - initialTicks) / ((totalAnimationTime * 0.75f) - initialTicks)) * (livingEntity.getBbHeight())));
        }

        poseStack.translate(0, yPosition, 0);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-livingEntity.getYRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotationAngle));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.scale(scale, scale, scale);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(ARCANE_AURA_TEXTURE));

        vertex(poseStack.last(), vertexConsumer, -6, -6, 0, 0, 0, 255, 255, 255, vertexAlpha);
        vertex(poseStack.last(), vertexConsumer, 6, -6, 0, 1, 0, 255, 255, 255, vertexAlpha);
        vertex(poseStack.last(), vertexConsumer, 6, 6, 0, 1, 1, 255, 255, 255, vertexAlpha);
        vertex(poseStack.last(), vertexConsumer, -6, 6, 0, 0, 1, 255, 255, 255, vertexAlpha);

        vertex(poseStack.last(), vertexConsumer, -6, 6, 0, 0, 1, 255, 255, 255, vertexAlpha);
        vertex(poseStack.last(), vertexConsumer, 6, 6, 0, 1, 1, 255, 255, 255, vertexAlpha);
        vertex(poseStack.last(), vertexConsumer, 6, -6, 0, 1, 0, 255, 255, 255, vertexAlpha);
        vertex(poseStack.last(), vertexConsumer, -6, -6, 0, 0, 0, 255, 255, 255, vertexAlpha);

        poseStack.popPose();
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, int x, int y, int z, float u, float v, int red, int green, int blue, int vertexAlpha) {
        consumer.addVertex(pose, (float)x, (float)y, (float)z)
                .setColor(red, green, blue, vertexAlpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(pose, 0, 0, 1);
    }

    public boolean isExpired() {
        long effectiveSpawnTime = spawnTime + delayTicks * 50L;
        return (System.currentTimeMillis() - effectiveSpawnTime) > (totalAnimationTime * 50L);
    }
}
