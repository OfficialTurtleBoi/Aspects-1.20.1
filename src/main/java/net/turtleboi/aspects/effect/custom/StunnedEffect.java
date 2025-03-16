package net.turtleboi.aspects.effect.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.turtleboi.aspects.client.renderer.ColdAuraRenderer;
import net.turtleboi.aspects.effect.ModEffects;
import net.turtleboi.aspects.entity.ModEntities;
import net.turtleboi.aspects.entity.entities.SingularityEntity;
import net.turtleboi.aspects.network.ModNetworking;
import net.turtleboi.aspects.network.packets.SendParticlesS2C;
import net.turtleboi.aspects.particle.ModParticles;
import net.turtleboi.aspects.util.AspectUtil;
import net.turtleboi.aspects.util.AttributeModifierUtil;
import net.turtleboi.aspects.util.ModAttributes;

import java.util.*;

public class StunnedEffect extends MobEffect {
    private final String attributeModifierName = "stunned_movement_speed";
    public static final Map<UUID, StunPlayerData> stunnedPlayers = new HashMap<>();
    private final List<LivingEntity> storedEntities = new ArrayList<>();

    public StunnedEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        Player stunningPlayer = null;
        if (pLivingEntity.getPersistentData().hasUUID("StunnedBy")) {
            stunningPlayer = pLivingEntity.level().getPlayerByUUID(pLivingEntity.getPersistentData().getUUID("StunnedBy"));
        }

        if (pLivingEntity.hasEffect(ModEffects.STUNNED.get())) {
            MobEffectInstance originalInstance = pLivingEntity.getEffect(ModEffects.STUNNED.get());
            if (originalInstance.isVisible()) {
                originalInstance.update(
                        new MobEffectInstance(
                                ModEffects.STUNNED.get(),
                                originalInstance.getDuration(),
                                originalInstance.getAmplifier(),
                                originalInstance.isAmbient(),
                                false,
                                originalInstance.showIcon()));
            }
        }

        if (!pLivingEntity.level().isClientSide &&
                (pLivingEntity.tickCount % 20 == 0 || pLivingEntity.tickCount % 18 == 0 || pLivingEntity.tickCount % 16 == 0)){
            double offX = (pLivingEntity.level().random.nextDouble() - 0.5) * 0.5;
            double offZ = (pLivingEntity.level().random.nextDouble() - 0.5) * 0.5;
            ModNetworking.sendToNear(new SendParticlesS2C(
                    ModParticles.STUNNED_PARTICLES.get(),
                    pLivingEntity.getX() + offX,
                    pLivingEntity.getY() + pLivingEntity.getBbHeight(),
                    pLivingEntity.getZ() + offZ,
                    0,0,0), pLivingEntity);
        }

        int duration = Objects.requireNonNull(pLivingEntity.getEffect(ModEffects.STUNNED.get())).getDuration();
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
                        AttributeModifier.Operation.MULTIPLY_TOTAL);

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

            if(stunningPlayer != null) {
                double arcaniAmplifier = 0;
                if (stunningPlayer.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                    arcaniAmplifier = stunningPlayer.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
                }

                if (stunningPlayer.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null && stunningPlayer.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue() > 0) {
                    Level level = pLivingEntity.level();
                    if (level instanceof ServerLevel serverLevel && duration <= 1) {
                        for (int i = 0; i < (arcaniAmplifier / 2); i++) {
                            LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
                            lightning.setPos(pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ());
                            serverLevel.addFreshEntity(lightning);
                        }

                        if (stunningPlayer.getAttribute(ModAttributes.INFERNUM_ASPECT.get()) != null) {
                            AABB ignitionArea = new AABB(pLivingEntity.getX() - 2, pLivingEntity.getY() - 2, pLivingEntity.getZ() - 2,
                                    pLivingEntity.getX() + 2, pLivingEntity.getY() + 2, pLivingEntity.getZ() + 2);
                            List<LivingEntity> ignitedEntities = pLivingEntity.level().getEntitiesOfClass(LivingEntity.class, ignitionArea, e -> e != pLivingEntity && !(e instanceof Player));
                            for (LivingEntity ignitedEntity : ignitedEntities) {
                                AspectUtil.setIgnitor(ignitedEntity, stunningPlayer);
                            }
                        }
                    }
                }
            }
        }

        if(stunningPlayer != null) {
            double arcaniAmplifier = 0;
            if (stunningPlayer.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                arcaniAmplifier = stunningPlayer.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
            }
            double arcaneFactor = 1 + (arcaniAmplifier / 4.0);

            if (stunningPlayer.getAttribute(ModAttributes.GLACIUS_ASPECT.get()) != null && stunningPlayer.getAttribute(ModAttributes.GLACIUS_ASPECT.get()).getValue() > 0) {
                double glaciusAmplifier = stunningPlayer.getAttributeValue(ModAttributes.GLACIUS_ASPECT.get());
                if (pLivingEntity.tickCount % 20 == 0) {
                    ColdAuraRenderer.addAuraForEntity(pLivingEntity, System.currentTimeMillis(), 30, glaciusAmplifier);
                    pLivingEntity.level().playSound(
                            null,
                            pLivingEntity.getX(),
                            pLivingEntity.getY(),
                            pLivingEntity.getZ(),
                            SoundEvents.PLAYER_HURT_FREEZE,
                            SoundSource.HOSTILE,
                            1.0F,
                            0.4f / (pLivingEntity.level().getRandom().nextFloat() * 0.4f + 0.8f)
                    );
                    RandomSource random = pLivingEntity.level().getRandom();
                    int count = (int) ((glaciusAmplifier + 1) * 20);
                    for (int i = 0; i < count; i++) {
                        double theta = random.nextDouble() * Math.PI;
                        double phi = random.nextDouble() * 2 * Math.PI;
                        double speed = 0.2 + random.nextDouble() * 0.3;
                        double xSpeed = speed * Math.sin(theta) * Math.cos(phi);
                        double ySpeed = speed * Math.cos(theta);
                        double zSpeed = speed * Math.sin(theta) * Math.sin(phi);
                        double offX = (random.nextDouble() - 0.5) * 0.2;
                        double offY = pLivingEntity.getBbHeight() / 2;
                        double offZ = (random.nextDouble() - 0.5) * 0.2;

                        ModNetworking.sendToNear(new SendParticlesS2C(
                                ParticleTypes.SNOWFLAKE,
                                pLivingEntity.getX() + offX,
                                pLivingEntity.getY() + offY,
                                pLivingEntity.getZ() + offZ,
                                xSpeed,ySpeed,zSpeed), pLivingEntity);
                    }
                    AABB ignitionArea = new AABB(pLivingEntity.getX() - 2, pLivingEntity.getY() - 2, pLivingEntity.getZ() - 2,
                            pLivingEntity.getX() + 2, pLivingEntity.getY() + 2, pLivingEntity.getZ() + 2);
                    List<LivingEntity> chilledEntities = pLivingEntity.level().getEntitiesOfClass(LivingEntity.class, ignitionArea, e -> e != pLivingEntity && !(e instanceof Player));
                    for (LivingEntity chilledEntity : chilledEntities) {
                        AspectUtil.setChiller(chilledEntity, stunningPlayer);
                        int chillTicks = (int) ((40 * glaciusAmplifier) * arcaneFactor);
                        chilledEntity.addEffect(new MobEffectInstance(ModEffects.CHILLED.get(), chillTicks, (int) glaciusAmplifier - 1));
                    }
                }
            }

            if (stunningPlayer.getAttribute(ModAttributes.UMBRE_ASPECT.get()) != null && stunningPlayer.getAttribute(ModAttributes.UMBRE_ASPECT.get()).getValue() > 0) {
                if (duration == 1){
                    SingularityEntity blackHole = new SingularityEntity(ModEntities.SINGULARITY.get(), pLivingEntity.level());
                    blackHole.setOwner(pLivingEntity);
                    blackHole.setPos(pLivingEntity.getX(), pLivingEntity.getY() + pLivingEntity.getBbHeight(), pLivingEntity.getZ());
                    pLivingEntity.level().addFreshEntity(blackHole);
                }
            }
        }

        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        AttributeModifierUtil.removeModifier(
                pLivingEntity,
                Attributes.MOVEMENT_SPEED,
                attributeModifierName);
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
