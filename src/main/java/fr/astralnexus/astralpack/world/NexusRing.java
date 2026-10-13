package fr.astralnexus.astralpack.world;

import fr.astralnexus.astralpack.block.NexusPortalBlock;
import fr.astralnexus.astralpack.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/** Anneau du portail posé dans le monde : détection, ouverture et fermeture de la surface. */
public final class NexusRing {
    /** Distance maximale, en blocs, entre le pupitre et le centre de l'anneau. */
    public static final int SEARCH_RADIUS = 10;

    public record Found(BlockPos center, Direction.Axis axis) {
    }

    private NexusRing() {
    }

    public static BlockPos cell(BlockPos center, Direction.Axis axis, int dx, int dy) {
        return axis == Direction.Axis.X ? center.offset(dx, dy, 0) : center.offset(0, dy, dx);
    }

    private static boolean isRing(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.ASTRAL_RING_BLOCK.get());
    }

    /** Anneau complet autour de ce centre. */
    public static boolean isComplete(Level level, BlockPos center, Direction.Axis axis) {
        for (int[] c : RingGeometry.ring()) {
            if (!isRing(level, cell(center, axis, c[0], c[1]))) {
                return false;
            }
        }
        return true;
    }

    /** La surface est libre : air, plante ou portail déjà ouvert. */
    private static boolean isInteriorFree(Level level, BlockPos center, Direction.Axis axis) {
        for (int[] c : RingGeometry.interior()) {
            BlockState state = level.getBlockState(cell(center, axis, c[0], c[1]));
            if (!state.isAir() && !state.canBeReplaced() && !state.is(ModBlocks.NEXUS_PORTAL.get())) {
                return false;
            }
        }
        return true;
    }

    /** Cherche l'anneau complet et dégagé le plus proche du pupitre. */
    public static Optional<Found> find(Level level, BlockPos near) {
        Found best = null;
        double bestDistance = Double.MAX_VALUE;
        int r = RingGeometry.RADIUS;
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
                for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
                    for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                        BlockPos center = near.offset(dx, dy, dz);
                        if (!isRing(level, cell(center, axis, r, 0)) || !isRing(level, cell(center, axis, -r, 0))
                                || !isRing(level, cell(center, axis, 0, r)) || !isRing(level, cell(center, axis, 0, -r))) {
                            continue;
                        }
                        double distance = center.distSqr(near);
                        if (distance < bestDistance && isComplete(level, center, axis)
                                && isInteriorFree(level, center, axis)) {
                            best = new Found(center.immutable(), axis);
                            bestDistance = distance;
                        }
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }

    /** Remplit la surface du portail. */
    public static void open(Level level, BlockPos center, Direction.Axis axis) {
        BlockState portal = ModBlocks.NEXUS_PORTAL.get().defaultBlockState().setValue(NexusPortalBlock.AXIS, axis);
        for (int[] c : RingGeometry.interior()) {
            level.setBlock(cell(center, axis, c[0], c[1]), portal, 3);
        }
    }

    /** Retire la surface du portail, et seulement elle. */
    public static void close(Level level, BlockPos center, Direction.Axis axis) {
        for (int[] c : RingGeometry.interior()) {
            BlockPos pos = cell(center, axis, c[0], c[1]);
            if (level.getBlockState(pos).is(ModBlocks.NEXUS_PORTAL.get())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}
