package fr.astralnexus.astralpack.world;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.block.NexusPortalBlock;
import fr.astralnexus.astralpack.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/** Dimension hub « Nexus Astral » : clé, point d'apparition sûr, portail de retour et téléportation. */
public final class NexusDimension {
    public static final ResourceKey<Level> NEXUS =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(Astralpack.MOD_ID, "nexus"));

    /** Hauteur de la plateforme de secours si le centre est vide. */
    private static final int PLATFORM_Y = 64;
    /** Distance, en blocs, entre le point d'arrivée et le portail de retour (vers le sud). */
    private static final int GATE_Z = 14;

    private NexusDimension() {
    }

    public static boolean isNexus(Level level) {
        return level.dimension().equals(NEXUS);
    }

    /** Point d'apparition du hub : au-dessus de l'île centrale, sinon sur une petite plateforme créée à la volée. */
    public static Vec3 hubSpawn(ServerLevel level) {
        level.getChunk(0, 0);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
        if (y <= level.getMinBuildHeight() + 1) {
            BlockPos center = new BlockPos(0, PLATFORM_Y - 1, 0);
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, 0, -3), center.offset(3, 0, 3))) {
                level.setBlockAndUpdate(pos, Blocks.DEEPSLATE_TILES.defaultBlockState());
            }
            y = PLATFORM_Y;
        }
        ensureReturnGate(level);
        return new Vec3(0.5, y, 0.5);
    }

    public static void teleportToHub(ServerPlayer player, ServerLevel level) {
        Vec3 spawn = hubSpawn(level);
        player.teleportTo(level, spawn.x, spawn.y, spawn.z, player.getYRot(), 0.0F);
        player.fallDistance = 0.0F;
    }

    /**
     * Portail de retour permanent : un anneau de même taille que celui du joueur, toujours ouvert, posé une seule fois
     * près de l'arrivée. S'il a été abîmé, il est reconstruit à la même place.
     */
    public static void ensureReturnGate(ServerLevel level) {
        NexusData data = NexusData.get(level.getServer());
        BlockPos center = data.getReturnGate();
        int r = RingGeometry.RADIUS;
        level.getChunk(-1, 0);
        level.getChunk(0, 0);
        if (center == null) {
            int ground = level.getMinBuildHeight();
            for (int x = -r; x <= r; x++) {
                ground = Math.max(ground, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, GATE_Z));
            }
            if (ground <= level.getMinBuildHeight() + 1) {
                ground = PLATFORM_Y;
            }
            center = new BlockPos(0, ground + r, GATE_Z);
            data.setReturnGate(center);
        } else if (NexusRing.isComplete(level, center, Direction.Axis.X)) {
            return;
        }
        buildReturnGate(level, center);
    }

    private static void buildReturnGate(ServerLevel level, BlockPos center) {
        int r = RingGeometry.RADIUS;
        BlockState ring = ModBlocks.ASTRAL_RING_BLOCK.get().defaultBlockState();
        BlockState portal = ModBlocks.NEXUS_PORTAL.get().defaultBlockState()
                .setValue(NexusPortalBlock.AXIS, Direction.Axis.X);
        BlockState base = Blocks.DEEPSLATE_TILES.defaultBlockState();
        for (int x = -r + 2; x <= r - 2; x++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = center.offset(x, -r - 1, dz);
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.canBeReplaced()) {
                    level.setBlock(pos, base, 3);
                }
            }
        }
        for (int[] c : RingGeometry.ring()) {
            level.setBlock(NexusRing.cell(center, Direction.Axis.X, c[0], c[1]), ring, 3);
        }
        for (int[] c : RingGeometry.interior()) {
            level.setBlock(NexusRing.cell(center, Direction.Axis.X, c[0], c[1]), portal, 3);
        }
    }
}
