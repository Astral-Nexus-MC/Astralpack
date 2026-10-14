package fr.astralnexus.astralpack.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.Method;

/**
 * Passerelle vers les notifications de Fordix (interface Voidframe 2 du serveur : verre sombre, en haut à droite, au-dessus
 * de tout). Fordix n'est pas une dépendance obligatoire : on l'appelle par réflexion, et en son absence (ou si son API
 * change) l'appelant se rabat sur la notification du jeu.
 */
final class FordixBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MOD_ID = "fordix";
    private static final String API = "com.omneria.fordix.notification.FordixNotifications";
    private static final String STYLE = "com.omneria.fordix.notification.NotificationStyle";

    private static boolean resolved;
    private static boolean usable;
    private static Method sendMethod;
    private static Class<?> styleClass;

    private FordixBridge() {
    }

    /** Envoie la notification par Fordix ; faux si Fordix n'est pas utilisable (l'appelant prend alors le relais). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static boolean send(ServerPlayer player, String style, Component title, Component message, int seconds) {
        if (!resolve()) {
            return false;
        }
        try {
            Object fordixStyle = Enum.valueOf((Class<? extends Enum>) styleClass, style);
            sendMethod.invoke(null, player, fordixStyle, title, message, seconds);
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            usable = false;
            LOGGER.warn("[Astralpack] Notification par Fordix impossible, retour aux notifications du jeu", e);
            return false;
        }
    }

    private static boolean resolve() {
        if (!resolved) {
            resolved = true;
            if (ModList.get().isLoaded(MOD_ID)) {
                try {
                    styleClass = Class.forName(STYLE);
                    sendMethod = Class.forName(API).getMethod("send", ServerPlayer.class, styleClass, Component.class,
                            Component.class, int.class);
                    usable = true;
                    LOGGER.info("[Astralpack] Notifications par Fordix activées");
                } catch (ReflectiveOperationException | LinkageError e) {
                    LOGGER.warn("[Astralpack] Fordix est présent mais son API de notifications est introuvable : "
                            + "notifications du jeu utilisées", e);
                }
            }
        }
        return usable;
    }
}
