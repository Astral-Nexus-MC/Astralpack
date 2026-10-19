package fr.astralnexus.astralpack.network;

import com.mojang.logging.LogUtils;
import fr.astralnexus.astralpack.block.entity.SpawnerConsoleBlockEntity;
import fr.astralnexus.astralpack.spawner.SpawnerStat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * Passerelle vers les consoles de Fordix (écran Voidframe 2 des réglages à niveaux). Fordix n'est pas une dépendance obligatoire :
 * on l'appelle par réflexion, et en son absence (ou si son API change) la console garde ses messages dans le chat.
 */
public final class ConsoleBridge {
    /** Le type de console, tel que Fordix le garde pour renvoyer les clics. */
    public static final String KIND = "astralpack:spawner_console";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String API = "com.omneria.fordix.console.FordixConsoles";

    private static boolean resolved;
    private static boolean usable;
    private static Method openMethod;

    private ConsoleBridge() {
    }

    /** Ouvre l'écran de la console chez le joueur ; faux si Fordix n'est pas utilisable (l'appelant écrit alors dans le chat). */
    public static boolean open(ServerPlayer player, SpawnerConsoleBlockEntity console) {
        if (!resolve()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(openMethod.invoke(null, player, console.screenData()));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            usable = false;
            LOGGER.warn("[Astralpack] Ecran de console par Fordix impossible, retour aux messages du chat", e);
            return false;
        }
    }

    private static boolean resolve() {
        if (!resolved) {
            resolved = true;
            if (ModList.get().isLoaded("fordix")) {
                try {
                    Class<?> api = Class.forName(API);
                    openMethod = api.getMethod("open", ServerPlayer.class, CompoundTag.class);
                    Method register = api.getMethod("register", String.class, BiFunction.class);
                    BiFunction<ServerPlayer, CompoundTag, CompoundTag> handler = ConsoleBridge::onAction;
                    register.invoke(null, KIND, handler);
                    usable = true;
                    LOGGER.info("[Astralpack] Ecran de la console des spawners par Fordix active");
                } catch (ReflectiveOperationException | LinkageError e) {
                    LOGGER.warn("[Astralpack] Fordix est present mais son API de consoles est introuvable : messages du chat utilises", e);
                }
            }
        }
        return usable;
    }

    /** Un clic sur un bouton de l'ecran : un cran de plus ou de moins sur un reglage ; renvoie les donnees a jour de l'ecran. */
    private static CompoundTag onAction(ServerPlayer player, CompoundTag action) {
        int[] at = action.getIntArray("pos");
        SpawnerStat stat = SpawnerStat.byId(action.getString("row"));
        if (at.length != 3 || stat == null) {
            return null;
        }
        BlockPos pos = new BlockPos(at[0], at[1], at[2]);
        if (!player.level().isLoaded(pos) || !(player.level().getBlockEntity(pos) instanceof SpawnerConsoleBlockEntity console)) {
            return null;
        }
        console.tryUpgradeFromScreen(player, stat, action.getBoolean("bonus"));
        return console.screenData();
    }
}
