package fr.astralnexus.astralpack.world;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Trajets par le portail : aller au Nexus en mémorisant l'origine, et en revenir par le portail permanent. */
public final class NexusTravel {
    private static final String ORIGIN = "astralpack_nexus_origin";
    /** Délai, en ticks, avant qu'un joueur puisse reprendre un portail (évite les allers-retours en boucle). */
    private static final int COOLDOWN = 100;
    /** Distance, en blocs, entre le plan du portail et le point d'arrivée au retour. */
    private static final double EXIT_OFFSET = 3.0D;

    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    private NexusTravel() {
    }

    public static void travel(ServerPlayer player, BlockPos portalPos, Direction.Axis axis) {
        long now = player.serverLevel().getGameTime();
        Long until = COOLDOWNS.get(player.getUUID());
        if (until != null && now < until) {
            return;
        }
        MinecraftServer server = player.server;
        if (NexusDimension.isNexus(player.level())) {
            back(player, server);
        } else {
            toNexus(player, portalPos, axis, server);
        }
        COOLDOWNS.put(player.getUUID(), now + COOLDOWN);
    }

    private static void toNexus(ServerPlayer player, BlockPos portalPos, Direction.Axis axis, MinecraftServer server) {
        ServerLevel nexus = server.getLevel(NexusDimension.NEXUS);
        if (nexus == null) {
            return;
        }
        // Le joueur repart du côté d'où il est entré : on le recule de quelques blocs par rapport au plan du portail.
        Vec3 motion = player.getDeltaMovement();
        double along = axis == Direction.Axis.X ? motion.z : motion.x;
        double side = axis == Direction.Axis.X ? player.getZ() - (portalPos.getZ() + 0.5D)
                : player.getX() - (portalPos.getX() + 0.5D);
        int travel = along > 0.01D ? 1 : along < -0.01D ? -1 : (side < 0.0D ? 1 : -1);
        double originX = player.getX();
        double originZ = player.getZ();
        if (axis == Direction.Axis.X) {
            originZ = portalPos.getZ() + 0.5D - travel * EXIT_OFFSET;
        } else {
            originX = portalPos.getX() + 0.5D - travel * EXIT_OFFSET;
        }
        CompoundTag origin = new CompoundTag();
        origin.putString("dim", player.level().dimension().location().toString());
        origin.putDouble("x", originX);
        origin.putDouble("y", player.getY());
        origin.putDouble("z", originZ);
        origin.putFloat("yaw", player.getYRot());
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(ORIGIN, origin);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
        NexusDimension.teleportToHub(player, nexus);
    }

    private static void back(ServerPlayer player, MinecraftServer server) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.contains(ORIGIN)) {
            CompoundTag origin = persisted.getCompound(ORIGIN);
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(origin.getString("dim")));
            ServerLevel target = server.getLevel(key);
            if (target != null) {
                player.teleportTo(target, origin.getDouble("x"), origin.getDouble("y"), origin.getDouble("z"),
                        origin.getFloat("yaw"), 0.0F);
                player.fallDistance = 0.0F;
                return;
            }
        }
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        player.teleportTo(overworld, spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, player.getYRot(), 0.0F);
        player.fallDistance = 0.0F;
    }
}
