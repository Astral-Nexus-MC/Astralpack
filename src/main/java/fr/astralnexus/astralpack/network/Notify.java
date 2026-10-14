package fr.astralnexus.astralpack.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Messages destinés à un joueur : affichés comme les autres notifications du jeu (en haut à droite). */
public final class Notify {
    private Notify() {
    }

    public static void send(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.toast(serverPlayer, message);
        }
    }

    public static void send(Player player, String translationKey) {
        send(player, Component.translatable(translationKey));
    }
}
