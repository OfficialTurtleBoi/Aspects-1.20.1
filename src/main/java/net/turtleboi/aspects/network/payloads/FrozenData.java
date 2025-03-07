package net.turtleboi.aspects.network.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.client.data.FrozenStatusCache;
import net.turtleboi.aspects.effect.ModEffects;

public record FrozenData(int entityId, boolean frozen) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FrozenData> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "frozen_data"));

    public static final StreamCodec<FriendlyByteBuf, FrozenData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, FrozenData::entityId,
                    ByteBufCodecs.BOOL, FrozenData::frozen,
                    FrozenData::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleFrozenData(final FrozenData data, final IPayloadContext context) {
        context.enqueueWork(() -> FrozenStatusCache.setStatus(data.entityId, data.frozen)).exceptionally(e -> {
            context.disconnect(Component.translatable("aspects.networking.failed", e.getMessage()));
            return null;
        });
    }

    public static void handleFrozenSync(final FrozenData data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            LivingEntity entity = (LivingEntity) context.player().level().getEntity(data.entityId());
            if (entity != null) {
                boolean serverFrozen = entity.hasEffect(ModEffects.FROZEN);
                boolean clientFrozen = data.frozen();
                if (serverFrozen != clientFrozen) {
                    //System.out.println("Frozen state mismatch for entity " + entity.getName().getString() +
                    //        ": server = " + serverFrozen + ", client = " + clientFrozen);
                    context.reply(new FrozenData(data.entityId(), false));
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("aspects.networking.failed", e.getMessage()));
            return null;
        });
    }

    public static void setFrozen(LivingEntity livingEntity, boolean frozen){
        FrozenData packet = new FrozenData(livingEntity.getId(), frozen);
        PacketDistributor.sendToAllPlayers(packet);
    }

    public static void sendFrozenSync(LivingEntity livingEntity) {
        int livingEntityId = livingEntity.getId();
        boolean clientFrozen = FrozenStatusCache.isFrozen(livingEntityId);
        FrozenData payload = new FrozenData(livingEntityId, clientFrozen);
        PacketDistributor.sendToServer(payload);
    }
}
