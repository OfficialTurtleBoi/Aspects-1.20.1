package net.turtleboi.aspects.network.payloads;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.turtleboi.aspects.Aspects;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record SoundData(ResourceLocation sound, double x, double y, double z, SoundParams params)
        implements CustomPacketPayload {

    public record SoundParams(float volume, float pitch) { }

    public static final StreamCodec<FriendlyByteBuf, SoundParams> SOUND_PARAMS_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, SoundParams::volume,
                    ByteBufCodecs.FLOAT, SoundParams::pitch,
                    SoundParams::new
            );

    public static final CustomPacketPayload.Type<SoundData> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "sound_data"));

    public static final StreamCodec<FriendlyByteBuf, SoundData> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, SoundData::sound,
                    ByteBufCodecs.DOUBLE, SoundData::x,
                    ByteBufCodecs.DOUBLE, SoundData::y,
                    ByteBufCodecs.DOUBLE, SoundData::z,
                    SOUND_PARAMS_CODEC, SoundData::params,
                    SoundData::new
            );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleSoundData(final SoundData data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level world = context.player().level();
            SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(data.sound());
            if (soundEvent == null) {
                soundEvent = SoundEvents.UI_BUTTON_CLICK.getDelegate().value();
            }
            SoundParams params = data.params();
            world.playLocalSound(
                    data.x(), data.y(), data.z(),
                    soundEvent,
                    SoundSource.PLAYERS,
                    params.volume(),
                    params.pitch(),
                    true
            );
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("aspects.networking.failed", e.getMessage()));
            return null;
        });
    }

    public static void spawnSound(SoundEvent soundEvent, double x, double y, double z, float volume, float pitch) {
        ResourceLocation soundLocation = BuiltInRegistries.SOUND_EVENT.getKey(soundEvent);
        if (soundLocation == null) {
            soundLocation = Objects.requireNonNull(SoundEvents.UI_BUTTON_CLICK.getKey()).location();
        }
        SoundParams params = new SoundParams(volume, pitch);
        SoundData packet = new SoundData(soundLocation, x, y, z, params);
        PacketDistributor.sendToAllPlayers(packet);
    }
}
