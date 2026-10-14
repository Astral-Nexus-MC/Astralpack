package fr.astralnexus.astralpack.world;

import com.mojang.logging.LogUtils;
import fr.astralnexus.astralpack.network.Notify;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Trajets par le portail : aller au Nexus en mémorisant l'origine, et en revenir par le portail permanent. */
public final class NexusTravel {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ORIGIN = "astralpack_nexus_origin";
    /** Délai, en ticks, avant qu'un joueur puisse reprendre un portail (évite les allers-retours en boucle). */
    private static final int COOLDOWN = 100;
    /** Distances, en blocs, entre le plan du portail et le point de retour : on essaie de la plus à la moins loin. */
    private static final double[] EXIT_DISTANCES = {3.0D, 2.0D, 4.0D};

    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    /** Trajets en attente : exécutés à la fin du tick, une fois tous les paquets du joueur traités. */
    private static final List<Runnable> PENDING = new ArrayList<>();

    private NexusTravel() {
    }

    public static void travel(ServerPlayer player, BlockPos portalPos, Direction.Axis axis) {
        long now = player.serverLevel().getGameTime();
        Long until = COOLDOWNS.get(player.getUUID());
        if (until != null && now < until) {
            return;
        }
        COOLDOWNS.put(player.getUUID(), now + COOLDOWN);
        // Ne jamais téléporter ici : cette méthode est appelée pendant le traitement du paquet de déplacement du joueur
        // (Entity.move -> checkInsideBlocks). Le jeu, en finissant ce traitement, remettrait le joueur aux coordonnées
        // envoyées par son client, mais dans la nouvelle dimension : arrivée dans le vide ou dans des blocs, et retour
        // loin du portail. Le trajet part à la fin du tick.
        PENDING.add(() -> {
            if (player.isRemoved() || player.hasDisconnected()) {
                return;
            }
            MinecraftServer server = player.server;
            if (NexusDimension.isNexus(player.level())) {
                back(player, server);
            } else {
                toNexus(player, portalPos, axis, server);
            }
        });
    }

    /** Exécute les trajets demandés pendant ce tick. À appeler à la fin de chaque tick du serveur. */
    public static void runPending() {
        if (PENDING.isEmpty()) {
            return;
        }
        List<Runnable> batch = new ArrayList<>(PENDING);
        PENDING.clear();
        for (Runnable travel : batch) {
            try {
                travel.run();
            } catch (RuntimeException e) {
                LOGGER.error("[Nexus] trajet en erreur", e);
            }
        }
    }

    private static void toNexus(ServerPlayer player, BlockPos portalPos, Direction.Axis axis, MinecraftServer server) {
        ServerLevel nexus = server.getLevel(NexusDimension.NEXUS);
        if (nexus == null) {
            LOGGER.warn("[Nexus] dimension introuvable : trajet annulé pour {}", player.getGameProfile().getName());
            return;
        }
        // Le côté d'où le joueur arrive : celui où il se trouve par rapport au plan du portail. Côté serveur, la vitesse
        // d'un joueur est presque toujours nulle (c'est son client qui le déplace) : seule la position est fiable.
        double offset = axis == Direction.Axis.X ? player.getZ() - (portalPos.getZ() + 0.5D)
                : player.getX() - (portalPos.getX() + 0.5D);
        int side = offset < 0.0D ? -1 : 1;
        CompoundTag origin = new CompoundTag();
        origin.putString("dim", player.level().dimension().location().toString());
        origin.putInt("px", portalPos.getX());
        origin.putInt("py", portalPos.getY());
        origin.putInt("pz", portalPos.getZ());
        origin.putBoolean("axisX", axis == Direction.Axis.X);
        origin.putInt("side", side);
        origin.putDouble("feetY", player.getY());
        origin.putDouble("ox", player.getX());
        origin.putDouble("oz", player.getZ());
        origin.putFloat("yaw", player.getYRot());
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(ORIGIN, origin);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
        LOGGER.info("[Nexus] {} entre dans le portail en {} (axe {}, côté {}) et part pour le hub",
                player.getGameProfile().getName(), portalPos, axis, side);
        NexusDimension.teleportToHub(player, nexus);
        LOGGER.info("[Nexus] {} arrive au hub en {}", player.getGameProfile().getName(), player.blockPosition());
    }

    private static void back(ServerPlayer player, MinecraftServer server) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.contains(ORIGIN)) {
            CompoundTag origin = persisted.getCompound(ORIGIN);
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(origin.getString("dim")));
            ServerLevel target = server.getLevel(key);
            if (target != null) {
                Vec3 spot = returnSpot(target, origin);
                player.teleportTo(target, spot.x, spot.y, spot.z, origin.getFloat("yaw"), 0.0F);
                player.fallDistance = 0.0F;
                LOGGER.info("[Nexus] {} revient en {} {}", player.getGameProfile().getName(), key.location(),
                        player.blockPosition());
                return;
            }
        }
        LOGGER.info("[Nexus] {} revient sans origine connue : point d'apparition du monde principal",
                player.getGameProfile().getName());
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        Vec3 spot = SafeSpot.find(overworld, spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
        player.teleportTo(overworld, spot.x, spot.y, spot.z, player.getYRot(), 0.0F);
        player.fallDistance = 0.0F;
        Notify.send(player, "message.astralpack.nexus.no_origin");
    }

    /**
     * Point de retour : devant le portail, du côté d'où le joueur est entré, à l'endroit debout le plus proche de sa
     * hauteur d'origine. Si ce côté est obstrué (pente, mur), on essaie d'autres distances, puis l'autre côté du
     * portail, puis l'endroit exact où il se tenait.
     */
    private static Vec3 returnSpot(ServerLevel level, CompoundTag origin) {
        boolean axisX = origin.getBoolean("axisX");
        int side = origin.getInt("side") == 0 ? -1 : origin.getInt("side");
        double cx = origin.getInt("px") + 0.5D;
        double cz = origin.getInt("pz") + 0.5D;
        double y = origin.getDouble("feetY");
        Vec3 first = null;
        for (int s : new int[]{side, -side}) {
            for (double distance : EXIT_DISTANCES) {
                double x = axisX ? origin.getDouble("ox") : cx + s * distance;
                double z = axisX ? cz + s * distance : origin.getDouble("oz");
                Vec3 spot = SafeSpot.find(level, x, y, z);
                if (first == null) {
                    first = spot;
                }
                if (SafeSpot.standable(level, BlockPos.containing(spot))) {
                    return spot;
                }
            }
        }
        Vec3 exact = SafeSpot.find(level, origin.getDouble("ox"), y, origin.getDouble("oz"));
        return SafeSpot.standable(level, BlockPos.containing(exact)) ? exact : first;
    }
}
