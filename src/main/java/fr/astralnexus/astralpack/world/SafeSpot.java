package fr.astralnexus.astralpack.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/** Cherche un endroit où un joueur peut se tenir debout : sol plein dessous, deux blocs libres au-dessus. */
public final class SafeSpot {
    /** Écart vertical maximal exploré autour de la hauteur demandée. */
    private static final int SEARCH_RANGE = 16;

    private SafeSpot() {
    }

    private static boolean passable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty();
    }

    /** Vrai si l'on peut se tenir sur ce bloc : sol plein dessous, pieds et tête libres. */
    public static boolean standable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        return !level.getBlockState(below).getCollisionShape(level, below).isEmpty()
                && level.getBlockState(below).getFluidState().isEmpty()
                && passable(level, feet) && passable(level, feet.above());
    }

    /**
     * Cherche, dans la colonne (x, z), l'emplacement debout le plus proche de la hauteur demandée ; à défaut, le
     * dessus de la colonne ; à défaut, la position demandée telle quelle.
     */
    public static Vec3 find(ServerLevel level, double x, double y, double z) {
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);
        level.getChunk(bx >> 4, bz >> 4);
        int min = level.getMinBuildHeight() + 1;
        int max = level.getMaxBuildHeight() - 2;
        int start = Math.max(min, Math.min(max, (int) Math.floor(y)));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int d = 0; d <= SEARCH_RANGE; d++) {
            for (int sign = 1; sign >= -1; sign -= 2) {
                int yy = start + sign * d;
                if (yy < min || yy > max) {
                    continue;
                }
                pos.set(bx, yy, bz);
                if (standable(level, pos)) {
                    return new Vec3(x, yy, z);
                }
                if (d == 0) {
                    break;
                }
            }
        }
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        if (top > min && top <= max) {
            return new Vec3(x, top, z);
        }
        return new Vec3(x, y, z);
    }
}
