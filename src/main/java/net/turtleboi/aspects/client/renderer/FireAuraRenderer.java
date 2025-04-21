package net.turtleboi.aspects.client.renderer;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.turtlecore.client.renderer.FireCircleRenderer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FireAuraRenderer {
    private final long spawnTime;
    private final int totalAnimationTime;
    private final double amplifier;

    public static final Map<UUID, List<FireAuraRenderer>> ENTITY_AURAS = new ConcurrentHashMap<>();

    public FireAuraRenderer(long currentTime, int totalAnimationTime, double amplifier) {
        this.spawnTime = currentTime;
        this.totalAnimationTime = totalAnimationTime;
        this.amplifier = amplifier;
    }

    public static void addAuraForEntity(LivingEntity livingEntity, long currentTime, int totalAnimationTime, double amplifier) {
        UUID uuid = livingEntity.getUUID();
        List<FireAuraRenderer> auraList = ENTITY_AURAS.computeIfAbsent(uuid, key -> new CopyOnWriteArrayList<>());
        auraList.add(new FireAuraRenderer(currentTime, totalAnimationTime, amplifier));
    }

    public static void renderAuras(MultiBufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        UUID uuid = livingEntity.getUUID();
        List<FireAuraRenderer> auraList = ENTITY_AURAS.get(uuid);
        if (auraList != null) {
            auraList.removeIf(FireAuraRenderer::isExpired);

            for (FireAuraRenderer aura : auraList) {
                aura.renderAura(bufferSource, poseStack, livingEntity, partialTicks);
            }
        }
    }

    public void renderAura(MultiBufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        poseStack.pushPose();
        float ticksElapsed = (System.currentTimeMillis() % spawnTime) / 50.0f;
        float tickCount = ticksElapsed + partialTicks;
        float initialTicks = 5.0f;
        float scale;

        float rotationSpeed;
        if (tickCount < initialTicks) {
            rotationSpeed = 3.0f;
        } else {
            rotationSpeed = (float) (5.0f * amplifier);
        }

        float rotationAngle = tickCount * rotationSpeed;

        if (tickCount < initialTicks) {
            scale = 0.25f * (tickCount / initialTicks);
        } else {
            scale = (float) (0.25f + ((tickCount - initialTicks) / (totalAnimationTime - initialTicks)) * (0.3f * amplifier));
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

        poseStack.translate(0, livingEntity.getBbHeight() * 0.5, 0);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-livingEntity.getYRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotationAngle));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.scale(scale, scale, scale);

        FireCircleRenderer.renderFireCircle(bufferSource, poseStack, vertexAlpha, amplifier > 3, false);

        poseStack.popPose();
    }



    public boolean isExpired() {
        return (System.currentTimeMillis() - spawnTime) > (totalAnimationTime * 50L);
    }
}
