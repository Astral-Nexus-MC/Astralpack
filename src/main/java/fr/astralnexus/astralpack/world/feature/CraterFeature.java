package fr.astralnexus.astralpack.world.feature;

import fr.astralnexus.astralpack.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/** Cratère : une cuvette creusée dans la surface, avec un peu de mousse et des minerais visibles au fond. */
public class CraterFeature extends Feature<NexusFeatureConfig> {
    public CraterFeature() {
        super(NexusFeatureConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NexusFeatureConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (context.config().tooCloseToCenter(origin) || !FeatureUtil.isGround(level.getBlockState(origin.below()))) {
            return false;
        }
        int radius = 4 + random.nextInt(5);
        int depthMax = 2 + random.nextInt(3);
        int centerGround = origin.getY() - 1;
        if (!FeatureUtil.isSolid(level.getBlockState(new BlockPos(origin.getX(), centerGround - depthMax - 3, origin.getZ())))) {
            return false;
        }
        BlockState surface = level.getBlockState(origin.below());
        BlockState ore = surface.is(ModBlocks.REEF_CRYSTAL.get()) ? ModBlocks.ASTRALITE_ORE.get().defaultBlockState()
                : ModBlocks.NEBULA_ORE.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius) {
                    continue;
                }
                double f = 1.0D - (distance / radius) * (distance / radius);
                int depth = (int) Math.round(f * depthMax);
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int top = FeatureUtil.groundY(level, x, z);
                if (depth <= 0 || !FeatureUtil.hasGround(level, x, z) || Math.abs(top - centerGround) > 2
                        || !FeatureUtil.isSolid(level.getBlockState(new BlockPos(x, top - depth - 1, z)))) {
                    continue;
                }
                for (int y = top; y > top - depth; y--) {
                    FeatureUtil.put(level, new BlockPos(x, y, z), air);
                }
                BlockPos floor = new BlockPos(x, top - depth, z);
                float roll = random.nextFloat();
                if (roll < 0.08F && distance > radius * 0.3D) {
                    FeatureUtil.put(level, floor, ore);
                } else if (roll < 0.30F) {
                    FeatureUtil.put(level, floor, FeatureUtil.moss());
                } else if (level.getBlockState(floor).is(ModBlocks.STELLAR_STONE.get())) {
                    FeatureUtil.put(level, floor, FeatureUtil.regolith());
                }
            }
        }
        return true;
    }
}
