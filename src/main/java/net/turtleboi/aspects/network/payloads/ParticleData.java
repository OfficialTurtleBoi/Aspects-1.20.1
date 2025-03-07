package net.turtleboi.aspects.network.payloads;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.turtleboi.aspects.Aspects;
import org.jetbrains.annotations.NotNull;

public record ParticleData(ResourceLocation particleType, double x, double y, double z, Speed speed)
        implements CustomPacketPayload {

    public record Speed(double x, double y, double z) { }

    public static final StreamCodec<FriendlyByteBuf, Speed> SPEED_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, Speed::x,
                    ByteBufCodecs.DOUBLE, Speed::y,
                    ByteBufCodecs.DOUBLE, Speed::z,
                    Speed::new
            );

    public static final CustomPacketPayload.Type<ParticleData> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "particle_data"));

    public static final StreamCodec<FriendlyByteBuf, ParticleData> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, ParticleData::particleType,
                    ByteBufCodecs.DOUBLE, ParticleData::x,
                    ByteBufCodecs.DOUBLE, ParticleData::y,
                    ByteBufCodecs.DOUBLE, ParticleData::z,
                    SPEED_CODEC, ParticleData::speed,
                    ParticleData::new
            );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleParticleData(final ParticleData data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel world = Minecraft.getInstance().level;
            if (world != null) {
                ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.get(data.particleType());
                ParticleOptions particle;
                if (type != null) {
                    if (type instanceof ParticleOptions options) {
                        particle = options;
                    } else if (type.equals(ParticleTypes.BLOCK)) {
                        particle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState());
                    } else {
                        particle = ParticleTypes.EXPLOSION;
                    }
                    Speed speed = data.speed();
                    world.addParticle(particle, data.x(), data.y(), data.z(), speed.x(), speed.y(), speed.z());
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("aspects.networking.failed", e.getMessage()));
            return null;
        });
    }

    public static void spawnParticle(ParticleOptions particleOptions, double x, double y, double z,double xSpeed,
                                     double ySpeed, double zSpeed) {
        ResourceLocation particleLocation = BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType());
        if (particleLocation == null) {
            System.out.println("Particle event null!");
            particleLocation = ResourceLocation.fromNamespaceAndPath("minecraft", "default_particle");
        }
        Speed speed = new Speed(xSpeed, ySpeed, zSpeed);
        ParticleData packet = new ParticleData(particleLocation, x, y, z, speed);
        PacketDistributor.sendToAllPlayers(packet);
    }


}
