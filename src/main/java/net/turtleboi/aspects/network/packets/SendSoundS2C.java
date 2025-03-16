package net.turtleboi.aspects.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class SendSoundS2C {
    private final double x, y, z;
    private final float volume, pitch;
    private final boolean distanceDelay;
    private final SoundEvent soundEvent;

    public SendSoundS2C(SoundEvent soundEvent, double x, double y, double z, float volume, float pitch, boolean distanceDelay) {
        this.soundEvent = soundEvent;
        this.x = x;
        this.y = y;
        this.z = z;
        this.volume = volume;
        this.pitch = pitch;
        this.distanceDelay = distanceDelay;
    }

    public SendSoundS2C(FriendlyByteBuf buf) {
        this.soundEvent = buf.readRegistryId();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
        this.distanceDelay = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeRegistryId(ForgeRegistries.SOUND_EVENTS, this.soundEvent);
        this.soundEvent.writeToNetwork(buf);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
        buf.writeBoolean(distanceDelay);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            //System.out.println(Component.literal("Level: " + Minecraft.getInstance().level)); //debug code
            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().level.playLocalSound(
                        x,
                        y,
                        z,
                        soundEvent,
                        SoundSource.AMBIENT,
                        volume,
                        pitch,
                        distanceDelay);
                //System.out.println(Component.literal("Sending particles to the client at: " + x + ", " + y + ", " + z)); //debug code
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
