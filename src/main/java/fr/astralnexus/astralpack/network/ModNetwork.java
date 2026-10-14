package fr.astralnexus.astralpack.network;

import fr.astralnexus.astralpack.Astralpack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Canal réseau du mod : pour l'instant, les notifications envoyées du serveur vers un joueur. */
public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Astralpack.MOD_ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, ToastPacket.class, ToastPacket::encode, ToastPacket::decode, ToastPacket::handle);
    }

    /** Affiche une notification en haut à droite de l'écran du joueur, avec les autres notifications du jeu. */
    public static void toast(ServerPlayer player, Component message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ToastPacket(message));
    }
}
