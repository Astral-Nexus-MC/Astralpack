package fr.astralnexus.astralpack.network;

import fr.astralnexus.astralpack.client.ClientToasts;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Texte d'une notification envoyée au client ; le texte est traduit côté client, dans sa langue. */
public record ToastPacket(Component message) {
    public static void encode(ToastPacket packet, FriendlyByteBuf buffer) {
        buffer.writeComponent(packet.message);
    }

    public static ToastPacket decode(FriendlyByteBuf buffer) {
        return new ToastPacket(buffer.readComponent());
    }

    public static void handle(ToastPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientToasts.show(packet.message)));
        context.get().setPacketHandled(true);
    }
}
