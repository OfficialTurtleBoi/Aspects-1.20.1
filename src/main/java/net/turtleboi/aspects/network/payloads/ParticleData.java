package net.turtleboi.aspects.network.payloads;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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

public record ParticleData(ResourceLocation particleType, double x, double y, double z) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ParticleData> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "particle_data"));

    public static final StreamCodec<FriendlyByteBuf, ParticleData> STREAM_CODEC =
            StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ParticleData::particleType,
            ByteBufCodecs.DOUBLE, ParticleData::x,
            ByteBufCodecs.DOUBLE, ParticleData::y,
            ByteBufCodecs.DOUBLE, ParticleData::z,
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
                    world.addParticle(particle, data.x(), data.y(), data.z(), 0, 0, 0);
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("aspects.networking.failed", e.getMessage()));
            return null;
        });
    }

    public static void spawnParticle(ResourceLocation particleType, double x, double y, double z) {
        ParticleData packet = new ParticleData(particleType, x, y, z);
        PacketDistributor.sendToAllPlayers(packet);
    }
}
