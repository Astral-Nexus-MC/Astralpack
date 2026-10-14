package fr.astralnexus.astralpack.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Messages destinés à un joueur, affichés comme des notifications : par Fordix (style Voidframe 2) quand il est installé,
 * sinon par les notifications du jeu. Toujours en haut à droite, jamais dans la barre d'action.
 */
public final class Notify {
    /** Nature de la notification : choisit la couleur du liseré et l'icône chez Fordix. */
    public enum Kind {
        INFO, SUCCESS, WARNING, ERROR
    }

    private static final int SECONDS = 5;

    private Notify() {
    }

    public static void send(Player player, Component message, Kind kind) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Component title = Component.translatable("toast.astralpack.title");
        if (!FordixBridge.send(serverPlayer, kind.name(), title, message, SECONDS)) {
            ModNetwork.toast(serverPlayer, message);
        }
    }

    public static void send(Player player, String translationKey, Kind kind) {
        send(player, Component.translatable(translationKey), kind);
    }

    public static void send(Player player, String translationKey) {
        send(player, translationKey, Kind.INFO);
    }
}
