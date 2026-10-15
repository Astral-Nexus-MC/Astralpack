package fr.astralnexus.astralpack.world;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * Nettoie les mondes créés avec des versions plus anciennes du mod, au démarrage d'un serveur dédié, avant que le jeu
 * ne lise le monde. Un monde enregistre ses dimensions dans level.dat : si l'une d'elles n'existe plus dans le mod, le
 * serveur refuse de charger le monde. La dimension {@code astralpack:nexus_b} (variante de lumière de la 0.4.0,
 * retirée en 0.4.2) est donc retirée de level.dat, après une sauvegarde du fichier.
 */
public final class LegacyWorldMigration {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String REMOVED_DIMENSION = "astralpack:nexus_b";
    private static final String BACKUP_SUFFIX = ".avant-retrait-nexus_b";

    private LegacyWorldMigration() {
    }

    /** À appeler au démarrage d'un serveur dédié, depuis le dossier du serveur. Ne lève jamais d'exception. */
    public static void run() {
        removeOldDimension();
        regenerateNexusIfOutdated();
    }

    /**
     * Génération du terrain du Nexus. À augmenter chaque fois que les blocs ou le relief changent au point de vouloir
     * régénérer le Nexus : au prochain démarrage du serveur, ses chunks sont mis de côté (jamais supprimés) et le jeu
     * les régénère au prochain trajet. 1 = blocs vanilla (0.4.x) ; 2 = blocs du mod (ressources du Nexus).
     */
    private static final int NEXUS_GENERATION = 2;
    private static final String GENERATION_FILE = "astralpack_nexus_generation.txt";

    /**
     * Le fichier de génération, à la racine du monde, dit avec quelle version du terrain les chunks du Nexus ont été
     * créés. Absent ou plus ancien : le dossier du Nexus est déplacé en {@code nexus.avant-regeneration-<date>}. Dans
     * tous les cas le fichier est ensuite mis à jour, donc cela ne se produit qu'une fois par génération.
     */
    private static void regenerateNexusIfOutdated() {
        try {
            Path world = Path.of(levelName());
            if (!Files.isDirectory(world)) {
                return;
            }
            Path marker = world.resolve(GENERATION_FILE);
            int current = 0;
            if (Files.isRegularFile(marker)) {
                try {
                    current = Integer.parseInt(Files.readString(marker).trim());
                } catch (NumberFormatException e) {
                    current = 0;
                }
            }
            if (current >= NEXUS_GENERATION) {
                return;
            }
            Path nexus = world.resolve("dimensions").resolve("astralpack").resolve("nexus");
            if (Files.isDirectory(nexus)) {
                String stamp = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
                Path backup = nexus.resolveSibling("nexus.avant-regeneration-" + stamp);
                Files.move(nexus, backup);
                LOGGER.info("[Astralpack] Nexus régénéré (génération {} -> {}) : anciens chunks mis de côté dans {}.",
                        current, NEXUS_GENERATION, backup);
            }
            Files.writeString(marker, Integer.toString(NEXUS_GENERATION));
        } catch (IOException | RuntimeException e) {
            LOGGER.error("[Astralpack] Régénération du Nexus impossible ; utiliser tools/regenerer_nexus.py", e);
        }
    }

    private static void removeOldDimension() {
        try {
            Path levelDat = Path.of(levelName()).resolve("level.dat");
            if (!Files.isRegularFile(levelDat)) {
                return;
            }
            CompoundTag root = NbtIo.readCompressed(levelDat.toFile());
            CompoundTag dimensions = root.getCompound("Data").getCompound("WorldGenSettings").getCompound("dimensions");
            if (!dimensions.contains(REMOVED_DIMENSION)) {
                return;
            }
            Path backup = Path.of(levelDat + BACKUP_SUFFIX);
            if (!Files.exists(backup)) {
                Files.copy(levelDat, backup, StandardCopyOption.COPY_ATTRIBUTES);
            }
            dimensions.remove(REMOVED_DIMENSION);
            NbtIo.writeCompressed(root, levelDat.toFile());
            LOGGER.info("[Astralpack] Dimension {} retirée de {} (sauvegarde : {}). Ses chunks restent sur le disque.",
                    REMOVED_DIMENSION, levelDat, backup.getFileName());
        } catch (IOException | RuntimeException e) {
            LOGGER.error("[Astralpack] Nettoyage de level.dat impossible ; si le serveur refuse de démarrer, "
                    + "utiliser tools/retirer_nexus_b.py", e);
        }
    }

    private static String levelName() {
        Path properties = Path.of("server.properties");
        if (Files.isRegularFile(properties)) {
            try (Reader reader = Files.newBufferedReader(properties)) {
                Properties props = new Properties();
                props.load(reader);
                String name = props.getProperty("level-name", "world").trim();
                return name.isEmpty() ? "world" : name;
            } catch (IOException e) {
                LOGGER.warn("[Astralpack] server.properties illisible, dossier de monde par défaut", e);
            }
        }
        return "world";
    }
}
