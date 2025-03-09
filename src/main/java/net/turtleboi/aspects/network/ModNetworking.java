package net.turtleboi.aspects.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.network.payloads.FrozenData;
import net.turtleboi.aspects.network.payloads.ParticleData;
import net.turtleboi.aspects.network.payloads.SoundData;

@EventBusSubscriber(modid = Aspects.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar("1");
        registrar.playToClient(
                ParticleData.TYPE,
                ParticleData.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ParticleData::handleParticleData,
                        (data, context) -> {

                        }
                )
        );

        registrar.playToClient(
                SoundData.TYPE,
                SoundData.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        SoundData::handleSoundData,
                        (data, context) -> {

                        }
                )
        );

        registrar.playBidirectional(
                FrozenData.TYPE,
                FrozenData.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        FrozenData::handleFrozenData,
                        FrozenData::handleFrozenSync
                )
        );
    }
}
