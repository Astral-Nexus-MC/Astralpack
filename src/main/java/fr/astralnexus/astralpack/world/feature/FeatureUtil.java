package fr.astralnexus.astralpack.world.feature;

import fr.astralnexus.astralpack.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Outils communs aux éléments de décor du Nexus. */
final class FeatureUtil {
    private FeatureUtil() {
    }

    static BlockState regolith() {
        return ModBlocks.ASTRAL_REGOLITH.get().defaultBlockState();
    }

    static BlockState stellar() {
        return ModBlocks.STELLAR_STONE.get().defaultBlockState();
    }

    static BlockState abyssal() {
        return ModBlocks.ABYSSAL_STONE.get().defaultBlockState();
    }

    static BlockState crystal() {
        return ModBlocks.REEF_CRYSTAL.get().defaultBlockState();
    }

    static BlockState moss() {
        return ModBlocks.LUMEN_MOSS.get().defaultBlockState();
    }

    static BlockState ring() {
        return ModBlocks.ASTRAL_RING_BLOCK.get().defaultBlockState();
    }

    /** Sol naturel du Nexus : le décor ne pousse que dessus. */
    static boolean isGround(BlockState state) {
        return state.is(ModBlocks.ASTRAL_REGOLITH.get()) || state.is(ModBlocks.STELLAR_STONE.get())
                || state.is(ModBlocks.REEF_CRYSTAL.get()) || state.is(ModBlocks.LUMEN_MOSS.get())
                || state.is(ModBlocks.ABYSSAL_STONE.get());
    }

    static boolean isSolid(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    static void put(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }

    /** Pose le bloc seulement s'il n'y a rien à cet endroit. */
    static boolean putIfAir(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, state, 2);
            return true;
        }
        return false;
    }

    /** Hauteur du dernier bloc plein de la colonne ; le minimum du monde s'il n'y en a pas (vide). */
    static int groundY(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
    }

    static boolean hasGround(WorldGenLevel level, int x, int z) {
        int y = groundY(level, x, z);
        return y > level.getMinBuildHeight() + 1 && isGround(level.getBlockState(new BlockPos(x, y, z)));
    }
}
