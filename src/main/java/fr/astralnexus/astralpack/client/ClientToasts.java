package fr.astralnexus.astralpack.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

/** Notifications du mod : le système de toasts du jeu, en haut à droite, au-dessus du reste de l'interface. */
public final class ClientToasts {
    private ClientToasts() {
    }

    public static void show(Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        SystemToast.addOrUpdate(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                Component.translatable("toast.astralpack.title"), message);
    }
}
