package fr.astralnexus.astralpack.world;

import com.mojang.logging.LogUtils;
import fr.astralnexus.astralpack.network.ModNetwork;
import fr.astralnexus.astralpack.network.Notify;
import fr.astralnexus.astralpack.network.PortalFxPacket;
import fr.astralnexus.astralpack.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Trajets par le portail, façon Stargate : le joueur est happé par la surface (écran qui se remplit de lumière, champ de
 * vision qui s'élargit, gerbe de particules), puis arrive dans une gerbe de lumière. L'aller se fait vers le hub, le
 * retour par le portail permanent du hub, devant le pupitre d'où le joueur est parti.
 */
public final class NexusTravel {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ORIGIN = "astralpack_nexus_origin";
    /** Délai, en ticks, avant qu'un joueur puisse reprendre un portail (évite les allers-retours en boucle). */
    private static final int COOLDOWN = 160;
    /** Durée, en ticks, pendant laquelle le joueur est happé par le portail avant d'être téléporté. */
    private static final int ABSORB_TICKS = 24;
    /** Durée, en ticks, du fondu à l'arrivée. */
    private static final int ARRIVAL_TICKS = 30;
    /** Distances, en blocs, devant le plan du portail pour l'ancien calcul du point de retour (secours). */
    private static final double[] EXIT_DISTANCES = {3.0D, 2.0D, 4.0D};

    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    /** Trajets en cours : exécutés par {@link #tick()} à la fin de chaque tick, jamais pendant un paquet du joueur. */
    private static final List<Pending> PENDING = new ArrayList<>();

    private static final class Pending {
        private final ServerPlayer player;
        private final BlockPos portalPos;
        private final Direction.Axis axis;
        private int ticksLeft = ABSORB_TICKS;
        private boolean started;

        private Pending(ServerPlayer player, BlockPos portalPos, Direction.Axis axis) {
            this.player = player;
            this.portalPos = portalPos;
            this.axis = axis;
        }
    }

    private NexusTravel() {
    }

    public static void travel(ServerPlayer player, BlockPos portalPos, Direction.Axis axis) {
        long now = player.serverLevel().getGameTime();
        Long until = COOLDOWNS.get(player.getUUID());
        if (until != null && now < until) {
            return;
        }
        COOLDOWNS.put(player.getUUID(), now + COOLDOWN);
        // Ne jamais rien faire d'autre ici que mettre le trajet en file : cette méthode est appelée pendant le
        // traitement du paquet de déplacement du joueur (Entity.move -> checkInsideBlocks). Une téléportation faite
        // maintenant serait écrasée par le jeu, qui remettrait le joueur aux coordonnées de son client dans la nouvelle
        // dimension (arrivée dans des blocs, retour loin du portail).
        PENDING.add(new Pending(player, portalPos.immutable(), axis));
    }

    /** À appeler à la fin de chaque tick du serveur : fait avancer les trajets en cours. */
    public static void tick() {
        if (PENDING.isEmpty()) {
            return;
        }
        for (Pending trip : new ArrayList<>(PENDING)) {
            try {
                if (trip.player.isRemoved() || trip.player.hasDisconnected()) {
                    PENDING.remove(trip);
                    continue;
                }
                if (!trip.started) {
                    trip.started = true;
                    absorb(trip.player);
                }
                if (--trip.ticksLeft <= 0) {
                    PENDING.remove(trip);
                    run(trip);
                }
            } catch (RuntimeException e) {
                PENDING.remove(trip);
                LOGGER.error("[Nexus] trajet en erreur", e);
            }
        }
    }

    /** Début de la traversée : le joueur est immobilisé dans la lumière du portail. */
    private static void absorb(ServerPlayer player) {
        ModNetwork.portalFx(player, PortalFxPacket.ABSORB, ABSORB_TICKS);
        int duration = ABSORB_TICKS + 8;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 7, true, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, 200, true, false, false));
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.5F, 1.5F);
        burst(level, player.position().add(0.0D, 1.0D, 0.0D), 26);
    }

    private static void burst(ServerLevel level, Vec3 at, int count) {
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, count, 0.4D, 0.7D, 0.4D, 0.06D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, at.x, at.y, at.z, count, 0.4D, 0.7D, 0.4D, 0.4D);
    }

    private static void run(Pending trip) {
        ServerPlayer player = trip.player;
        MinecraftServer server = player.server;
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.JUMP);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
                0.9F, 0.6F);
        boolean arrived = NexusDimension.isNexus(player.level()) ? back(player, server)
                : toNexus(player, trip.portalPos, trip.axis, server);
        if (arrived) {
            ServerLevel level = player.serverLevel();
            ModNetwork.portalFx(player, PortalFxPacket.ARRIVE, ARRIVAL_TICKS);
            level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9F, 0.8F);
            burst(level, player.position().add(0.0D, 1.0D, 0.0D), 40);
        } else {
            ModNetwork.portalFx(player, PortalFxPacket.ARRIVE, 8);
        }
    }

    private static boolean toNexus(ServerPlayer player, BlockPos portalPos, Direction.Axis axis, MinecraftServer server) {
        ServerLevel nexus = server.getLevel(NexusDimension.NEXUS);
        if (nexus == null) {
            LOGGER.warn("[Nexus] dimension introuvable : trajet annulé pour {}", player.getGameProfile().getName());
            return false;
        }
        String dimension = player.level().dimension().location().toString();
        CompoundTag origin = new CompoundTag();
        origin.putString("dim", dimension);
        origin.putInt("px", portalPos.getX());
        origin.putInt("py", portalPos.getY());
        origin.putInt("pz", portalPos.getZ());
        origin.putBoolean("axisX", axis == Direction.Axis.X);
        origin.putDouble("feetY", player.getY());
        origin.putDouble("ox", player.getX());
        origin.putDouble("oz", player.getZ());
        origin.putFloat("yaw", player.getYRot());
        // Côté d'où le joueur arrive, déduit de sa position : secours si le pupitre a disparu entre-temps.
        double offset = axis == Direction.Axis.X ? player.getZ() - (portalPos.getZ() + 0.5D)
                : player.getX() - (portalPos.getX() + 0.5D);
        origin.putInt("side", offset < 0.0D ? -1 : 1);
        // Le retour se fait devant le pupitre où la Death Scythe est plantée : c'est là que le joueur est parti.
        NexusData data = NexusData.get(server);
        if (data.hasActivePortal() && data.getPortalDimension().equals(dimension)
                && data.getPortalCenter().distSqr(portalPos) <= 100.0D) {
            origin.putBoolean("hasConsole", true);
            origin.putLong("console", data.getPortalConsole().asLong());
            origin.putInt("consoleFacing", data.getPortalConsoleFacing());
        }
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(ORIGIN, origin);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
        LOGGER.info("[Nexus] {} traverse le portail en {} (pupitre : {}) et part pour le hub",
                player.getGameProfile().getName(), portalPos, origin.getBoolean("hasConsole"));
        NexusDimension.teleportToHub(player, nexus);
        LOGGER.info("[Nexus] {} arrive au hub en {}", player.getGameProfile().getName(), player.blockPosition());
        return true;
    }

    private static boolean back(ServerPlayer player, MinecraftServer server) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.contains(ORIGIN)) {
            CompoundTag origin = persisted.getCompound(ORIGIN);
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(origin.getString("dim")));
            ServerLevel target = server.getLevel(key);
            if (target != null) {
                Vec3 spot = returnSpot(target, origin);
                float yaw = lookAt(origin, spot, origin.getFloat("yaw"));
                player.teleportTo(target, spot.x, spot.y, spot.z, yaw, 0.0F);
                player.fallDistance = 0.0F;
                LOGGER.info("[Nexus] {} revient en {} {}", player.getGameProfile().getName(), key.location(),
                        player.blockPosition());
                return true;
            }
        }
        LOGGER.info("[Nexus] {} revient sans origine connue : point d'apparition du monde principal",
                player.getGameProfile().getName());
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        Vec3 spot = SafeSpot.find(overworld, spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
        player.teleportTo(overworld, spot.x, spot.y, spot.z, player.getYRot(), 0.0F);
        player.fallDistance = 0.0F;
        Notify.send(player, "message.astralpack.nexus.no_origin", Notify.Kind.WARNING);
        return true;
    }

    /** Orientation du joueur à son retour : vers le pupitre s'il y en a un, sinon comme au départ. */
    private static float lookAt(CompoundTag origin, Vec3 spot, float fallback) {
        if (!origin.getBoolean("hasConsole")) {
            return fallback;
        }
        BlockPos console = BlockPos.of(origin.getLong("console"));
        double dx = console.getX() + 0.5D - spot.x;
        double dz = console.getZ() + 0.5D - spot.z;
        if (dx * dx + dz * dz < 1.0E-4D) {
            return fallback;
        }
        return (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
    }

    /** Un emplacement où l'on peut se tenir, et qui n'est pas dans la surface d'un portail (on le retraverserait). */
    private static boolean usable(ServerLevel level, Vec3 spot) {
        BlockPos feet = BlockPos.containing(spot);
        return SafeSpot.standable(level, feet) && !level.getBlockState(feet).is(ModBlocks.NEXUS_PORTAL.get())
                && !level.getBlockState(feet.above()).is(ModBlocks.NEXUS_PORTAL.get());
    }

    /**
     * Point de retour : devant le pupitre (son côté visible), puis sur ses autres côtés, à un puis deux blocs. Si le
     * pupitre n'existe plus, devant le plan du portail du côté d'où le joueur est entré, puis de l'autre côté, puis à
     * l'endroit exact d'où il est parti.
     */
    private static Vec3 returnSpot(ServerLevel level, CompoundTag origin) {
        double y = origin.getDouble("feetY");
        if (origin.getBoolean("hasConsole")) {
            BlockPos console = BlockPos.of(origin.getLong("console"));
            level.getChunk(console.getX() >> 4, console.getZ() >> 4);
            if (level.getBlockState(console).is(ModBlocks.NEXUS_CONSOLE.get())) {
                Direction front = Direction.from2DDataValue(origin.getInt("consoleFacing"));
                Direction[] sides = {front, front.getClockWise(), front.getCounterClockWise(), front.getOpposite()};
                Vec3 first = null;
                for (int distance = 1; distance <= 2; distance++) {
                    for (Direction side : sides) {
                        BlockPos base = console.relative(side, distance);
                        Vec3 spot = SafeSpot.find(level, base.getX() + 0.5D, console.getY(), base.getZ() + 0.5D);
                        if (first == null) {
                            first = spot;
                        }
                        if (usable(level, spot)) {
                            return spot;
                        }
                    }
                }
                if (first != null) {
                    return first;
                }
            }
        }
        boolean axisX = origin.getBoolean("axisX");
        int side = origin.getInt("side") == 0 ? -1 : origin.getInt("side");
        double cx = origin.getInt("px") + 0.5D;
        double cz = origin.getInt("pz") + 0.5D;
        Vec3 first = null;
        for (int s : new int[]{side, -side}) {
            for (double distance : EXIT_DISTANCES) {
                double x = axisX ? origin.getDouble("ox") : cx + s * distance;
                double z = axisX ? cz + s * distance : origin.getDouble("oz");
                Vec3 spot = SafeSpot.find(level, x, y, z);
                if (first == null) {
                    first = spot;
                }
                if (usable(level, spot)) {
                    return spot;
                }
            }
        }
        Vec3 exact = SafeSpot.find(level, origin.getDouble("ox"), y, origin.getDouble("oz"));
        return usable(level, exact) ? exact : first;
    }
}
