package fr.astralnexus.astralpack.network;

import fr.astralnexus.astralpack.client.ClientPortalFx;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Demande au client de jouer l'effet de traversée du portail : happé par la lumière, ou arrivée. */
public record PortalFxPacket(int mode, int ticks) {
    public static final int ABSORB = 1;
    public static final int ARRIVE = 2;

    public static void encode(PortalFxPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.mode);
        buffer.writeVarInt(packet.ticks);
    }

    public static PortalFxPacket decode(FriendlyByteBuf buffer) {
        return new PortalFxPacket(buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(PortalFxPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPortalFx.start(packet.mode, packet.ticks)));
        context.get().setPacketHandled(true);
    }
}
