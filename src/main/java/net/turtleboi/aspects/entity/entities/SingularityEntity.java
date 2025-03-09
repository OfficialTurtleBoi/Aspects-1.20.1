package net.turtleboi.aspects.entity.entities;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.aspects.network.payloads.ParticleData;
import net.turtleboi.aspects.util.ModAttributes;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class SingularityEntity extends Entity {
    private static final EntityDataAccessor<Integer> TEXTURE_INDEX = SynchedEntityData.defineId(SingularityEntity.class, EntityDataSerializers.INT);
    private LivingEntity owner;
    private int textureTickCounter = 0;
    protected int age;

    private final List<LivingEntity> storedEntities = new ArrayList<>();

    public SingularityEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TEXTURE_INDEX, 0);
    }

    @Override
    public void tick() {
        super.tick();
        age = tickCount;

        if (this.level().isClientSide) {
            if (age % 20 == 0) {
                spawnPortalParticles();
            }
        }

        if (!this.level().isClientSide) {
            textureTickCounter++;
            int ticksPerFrame = 2;
            if (textureTickCounter >= ticksPerFrame) {
                int newIndex = (getTextureIndex() + 1) % getMaxTextureIndex();
                setTextureIndex(newIndex);
                textureTickCounter = 0;
            }

            if (age == 1){
                playPortalSpawnSound();
                for (int i = 0; i < 120; i++) {
                    double theta = random.nextDouble() * Math.PI;
                    double phi = random.nextDouble() * 2 * Math.PI;
                    double speed = 1 + random.nextDouble() * 1;
                    double xSpeed = speed * Math.sin(theta) * Math.cos(phi);
                    double ySpeed = speed * Math.cos(theta);
                    double zSpeed = speed * Math.sin(theta) * Math.sin(phi);
                    double offX = (random.nextDouble() - 0.5) * 0.2;
                    double offY = getBbHeight() / 2;
                    double offZ = (random.nextDouble() - 0.5) * 0.2;

                    level().addParticle(
                            new DustParticleOptions(new Vector3f(0.404f, 0.310f, 0.349f), 1.0F),
                            getX() + offX,
                            getY() + offY,
                            getZ() + offZ,
                            xSpeed,ySpeed,zSpeed);
                }
            }

            if (age % 20 == 0) {
                spawnPortalParticles();
            }

            if (owner != null) {
                Player stunningPlayer;
                if (getOwner().getPersistentData().hasUUID("StunnedBy")) {
                    stunningPlayer = getOwner().level().getPlayerByUUID(getOwner().getPersistentData().getUUID("StunnedBy"));
                } else {
                    stunningPlayer = null;
                }

                if (stunningPlayer != null && stunningPlayer.getAttribute(ModAttributes.UMBRE_ASPECT) != null && stunningPlayer.getAttribute(ModAttributes.UMBRE_ASPECT).getValue() > 0) {
                    double umbreAmplifier = stunningPlayer.getAttributeValue(ModAttributes.UMBRE_ASPECT);

                    int randomInt = level().random.nextInt(100);
                    if (randomInt < (50 * (1 + (umbreAmplifier / 4)))) {
                        AABB area = new AABB(getX() - (4 * (1 + umbreAmplifier)), getY() - (4 * (1 + umbreAmplifier)), getZ() - (4 * (1 + umbreAmplifier)),
                                getX() + (4 * (1 + umbreAmplifier)), getY() + (4 * (1 + umbreAmplifier)), getZ() + (4 * (1 + umbreAmplifier)));
                        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, area, e -> e != stunningPlayer);
                        if (!entities.isEmpty()) {
                            if (randomInt < (25 * (1 + (umbreAmplifier / 4)))) {
                                LivingEntity chosen = entities.get(level().getRandom().nextInt(entities.size()));
                                if (!storedEntities.contains(chosen)) {
                                    storedEntities.add(chosen);
                                }
                            }
                        }
                    }

                    if (age <= 96) {
                        for (LivingEntity target : storedEntities) {
                            Vec3 direction = new Vec3(getX() - target.getX(), getY() - (target.getY() + (target.getBbHeight() / 2)), getZ() - target.getZ());
                            direction = direction.normalize().scale(0.75);
                            target.setDeltaMovement(direction);
                            target.hurtMarked = true;
                        }
                    }

                    if (age > 96) {
                        for (LivingEntity target : storedEntities) {
                            double throwAngle = (level().getRandom().nextDouble() * (1 + (umbreAmplifier / 4)));
                            Vec3 direction = new Vec3(getX() - target.getX(), getY() - (target.getY() + throwAngle), getZ() - target.getZ());
                            direction = direction.normalize().scale(-(2 + (umbreAmplifier / 4)) * level().getRandom().nextDouble());
                            target.setDeltaMovement(direction);
                            target.hurtMarked = true;
                        }
                    }
                }
            }

            if (age >= 100) {
                discard();
            }
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    private void spawnPortalParticles() {
        RandomSource random = level().getRandom();
        double portalX = getX();
        double portalZ = getZ();
        int numParticles = 8;

        for (int i = 0; i < numParticles; i++) {
            double offsetY = getBbHeight() * level().random.nextDouble();
            double randomX = portalX + (random.nextDouble() - 0.5) * 1.25;
            double randomZ = portalZ + (random.nextDouble() - 0.5) * 1.25;
            double velocityX = (random.nextDouble() - 0.5) * 0.05;
            double velocityY = (random.nextDouble() - 0.5) * 0.05;
            double velocityZ = (random.nextDouble() - 0.5) * 0.05;
            level().addParticle(
                    new DustParticleOptions(new Vector3f(0.404f, 0.310f, 0.349f), 1.0F),
                    randomX,
                    getY() + offsetY,
                    randomZ,
                    velocityX,
                    velocityY,
                    velocityZ);
        }
    }

    private void playPortalSpawnSound() {
        level().playLocalSound(
                getX(),
                getY(),
                getZ(),
                SoundEvents.ILLUSIONER_CAST_SPELL,
                SoundSource.BLOCKS,
                2.0F,
                level().random.nextFloat() * 0.2F + 0.5F,
                true
        );
    }

    public void setOwner(LivingEntity livingEntity) {
            owner = livingEntity;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    public int getTextureIndex() {
        return this.entityData.get(TEXTURE_INDEX);
    }

    public void setTextureIndex(int index) {
        this.entityData.set(TEXTURE_INDEX, index);
    }

    public int getMaxTextureIndex() {
        return 13;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(@NotNull ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }
}
