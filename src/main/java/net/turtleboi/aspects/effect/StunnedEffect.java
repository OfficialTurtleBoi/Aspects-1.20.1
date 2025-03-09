package net.turtleboi.aspects.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.entity.ModEntities;
import net.turtleboi.aspects.entity.entities.SingularityEntity;
import net.turtleboi.aspects.event.ModEvents;
import net.turtleboi.aspects.network.payloads.ParticleData;
import net.turtleboi.aspects.particle.ModParticles;
import net.turtleboi.aspects.util.AspectUtil;
import net.turtleboi.aspects.util.AttributeModifierUtil;
import net.turtleboi.aspects.util.ModAttributes;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StunnedEffect extends MobEffect {
    private final String attributeModifierName = "stunned_movement_speed";
    public static final Map<UUID, StunPlayerData> stunnedPlayers = new HashMap<>();
    private final List<LivingEntity> storedEntities = new ArrayList<>();

    public StunnedEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        Player stunningPlayer = null;
        if (pLivingEntity.getPersistentData().hasUUID("StunnedBy")) {
            stunningPlayer = pLivingEntity.level().getPlayerByUUID(pLivingEntity.getPersistentData().getUUID("StunnedBy"));
        }

        if (!pLivingEntity.level().isClientSide &&
                (pLivingEntity.tickCount % 20 == 0 || pLivingEntity.tickCount % 18 == 0 || pLivingEntity.tickCount % 16 == 0)){
            double offX = (pLivingEntity.level().random.nextDouble() - 0.5) * 0.5;
            double offZ = (pLivingEntity.level().random.nextDouble() - 0.5) * 0.5;
            ParticleData.spawnParticle(
                    ModParticles.STUNNED_PARTICLES.get(),
                    pLivingEntity.getX() + offX,
                    pLivingEntity.getY() + pLivingEntity.getBbHeight(),
                    pLivingEntity.getZ() + offZ,
                    0,0,0);
        }

        int duration = Objects.requireNonNull(pLivingEntity.getEffect(ModEffects.STUNNED)).getDuration();
        if (!pLivingEntity.level().isClientSide() && duration > 2) {
            //System.out.println("Stunning: " + pLivingEntity);
            //System.out.println("Duration: " + duration);
            if (pLivingEntity instanceof Player player) {
                if (!stunnedPlayers.containsKey(player.getUUID())) {
                    StunPlayerData data = new StunPlayerData(
                            player.getYRot(), player.getXRot(),
                            player.getAbilities().getWalkingSpeed(), player.getAbilities().getFlyingSpeed(),
                            player.getX(), player.getY(), player.getZ());
                    stunnedPlayers.put(player.getUUID(), data);
                    player.getAbilities().mayBuild = false;
                    player.getAbilities().flying = false;
                    player.getAbilities().setWalkingSpeed(0.0f);
                    if (!player.isCreative()) {
                        player.getAbilities().setFlyingSpeed(0);
                        player.getAbilities().invulnerable = false;
                    }
                    player.getAbilities().setWalkingSpeed(data.savedWalkingSpeed);
                    player.getAbilities().setFlyingSpeed(data.savedFlyingSpeed);
                    player.onUpdateAbilities();
                    player.setSprinting(false);
                    player.setJumping(false);
                }
                StunPlayerData data = stunnedPlayers.get(player.getUUID());

                if (data != null) {
                    player.setYRot(data.savedYaw);
                    player.setXRot(data.savedPitch);
                }
                if (data != null) {
                    player.setYRot(data.savedYaw);
                    player.setXRot(data.savedPitch);
                    player.teleportTo(data.savedX, data.savedY, data.savedZ);
                }

                player.hurtMarked = true;
                AttributeModifierUtil.applyPermanentModifier(
                        pLivingEntity,
                        Attributes.MOVEMENT_SPEED,
                        attributeModifierName,
                        -1,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

            } else if (pLivingEntity instanceof Mob mob) {
                mob.setNoAi(true);
                mob.hurtMarked = true;
                mob.setSprinting(false);
                mob.setJumping(false);
                mob.getNavigation().stop();
            }
        } else if (duration <= 3){
            //System.out.println("Unstunning: " + pLivingEntity);
            if (pLivingEntity instanceof Player player) {
                stunnedPlayers.remove(player.getUUID());
                player.getAbilities().mayBuild = true;
                if (player.isCreative()) {
                    player.getAbilities().invulnerable = true;
                }
                player.hurtMarked = true;
            } else if (pLivingEntity instanceof Mob mob) {
                mob.setNoAi(false);
                mob.hurtMarked = true;
            }

            if (stunningPlayer != null && stunningPlayer.getAttribute(ModAttributes.ARCANI_ASPECT) != null && stunningPlayer.getAttribute(ModAttributes.ARCANI_ASPECT).getValue() > 0) {
                double arcaniAmplifier = stunningPlayer.getAttributeValue(ModAttributes.ARCANI_ASPECT);
                Level level = pLivingEntity.level();
                if (level instanceof ServerLevel serverLevel && duration <= 1) {
                    for (int i = 0; i < (arcaniAmplifier / 2); i++) {
                        LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
                        lightning.setPos(pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ());
                        serverLevel.addFreshEntity(lightning);
                    }
                }

                if (pLivingEntity.getPersistentData().hasUUID("StunnedBy")){
                    AABB ignitionArea = new AABB(pLivingEntity.getX() - 2, pLivingEntity.getY() - 2, pLivingEntity.getZ() - 2,
                            pLivingEntity.getX() + 2, pLivingEntity.getY() + 2, pLivingEntity.getZ() + 2);
                    List<LivingEntity> ignitedEntities = pLivingEntity.level().getEntitiesOfClass(LivingEntity.class, ignitionArea, e -> e != pLivingEntity && !(e instanceof Player));
                    for (LivingEntity ignitedEntity : ignitedEntities) {
                        AspectUtil.setIgnitor(ignitedEntity, pLivingEntity.level().getPlayerByUUID(pLivingEntity.getPersistentData().getUUID("StunnedBy")));
                    }
                }
            }
        }

        if (stunningPlayer != null && stunningPlayer.getAttribute(ModAttributes.UMBRE_ASPECT) != null && stunningPlayer.getAttribute(ModAttributes.UMBRE_ASPECT).getValue() > 0) {
            if (duration == 1){
                SingularityEntity blackHole = new SingularityEntity(ModEntities.SINGULARITY.get(), pLivingEntity.level());
                blackHole.setOwner(pLivingEntity);
                blackHole.setPos(pLivingEntity.getX(), pLivingEntity.getY() + pLivingEntity.getBbHeight(), pLivingEntity.getZ());
                pLivingEntity.level().addFreshEntity(blackHole);
            }
        }
        return super.applyEffectTick(pLivingEntity, pAmplifier);
    }


    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(@NotNull AttributeMap attributeMap) {
        super.removeAttributeModifiers(attributeMap);
        AttributeInstance instance = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (instance != null) {
            instance.getModifiers().stream()
                    .map(AttributeModifier::id)
                    .filter(id -> id.getPath().equals(attributeModifierName))
                    .forEach(instance::removeModifier);
        }
    }

    @Override
    public ParticleOptions createParticleOptions(MobEffectInstance effect) {
        return ModParticles.NONE_PARTICLES.get();
    }

    public static class StunPlayerData {
        public final float savedYaw;
        public final float savedPitch;
        public final float savedWalkingSpeed;
        public final float savedFlyingSpeed;
        public final double savedX;
        public final double savedY;
        public final double savedZ;
        public StunPlayerData(float yaw, float pitch, float walkingSpeed, float flyingSpeed, double x, double y, double z) {
            this.savedYaw = yaw;
            this.savedPitch = pitch;
            this.savedWalkingSpeed = walkingSpeed;
            this.savedFlyingSpeed = flyingSpeed;
            this.savedX = x;
            this.savedY = y;
            this.savedZ = z;
        }
    }



}
