package fr.astralnexus.astralpack.world.feature;

import fr.astralnexus.astralpack.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/** Aiguille de cristal : une grande pointe de cristal de récif, entourée de quelques petites, qui s'effile vers le haut. */
public class CrystalSpireFeature extends Feature<NexusFeatureConfig> {
    public CrystalSpireFeature() {
        super(NexusFeatureConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NexusFeatureConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        NexusFeatureConfig config = context.config();
        if (config.tooCloseToCenter(origin) || !level.getBlockState(origin).isAir()
                || !FeatureUtil.isGround(level.getBlockState(origin.below()))) {
            return false;
        }
        int height = 6 + random.nextInt(7) + (config.size() - 1) * 6;
        spire(level, origin, height, random);
        int satellites = 2 + random.nextInt(3);
        for (int i = 0; i < satellites; i++) {
            int x = origin.getX() + random.nextInt(9) - 4;
            int z = origin.getZ() + random.nextInt(9) - 4;
            int groundY = FeatureUtil.groundY(level, x, z);
            if (!FeatureUtil.hasGround(level, x, z) || Math.abs(groundY - (origin.getY() - 1)) > 3) {
                continue;
            }
            spire(level, new BlockPos(x, groundY + 1, z), 3 + random.nextInt(4), random);
        }
        return true;
    }

    private void spire(WorldGenLevel level, BlockPos base, int height, RandomSource random) {
        BlockState crystal = FeatureUtil.crystal();
        BlockState tip = ModBlocks.ASTRALITE_BLOCK.get().defaultBlockState();
        float leanX = (random.nextFloat() - 0.5F) * 0.4F;
        float leanZ = (random.nextFloat() - 0.5F) * 0.4F;
        for (int y = 0; y < height; y++) {
            BlockPos center = base.offset(Math.round(leanX * y), y, Math.round(leanZ * y));
            float t = 1.0F - y / (float) height;
            FeatureUtil.putIfAir(level, center, y >= height - 1 ? tip : crystal);
            if (t > 0.55F) {
                for (BlockPos arm : new BlockPos[]{center.north(), center.south(), center.east(), center.west()}) {
                    if (y > 0 || FeatureUtil.isGround(level.getBlockState(arm.below()))) {
                        FeatureUtil.putIfAir(level, arm, crystal);
                    }
                }
            }
            if (t > 0.85F && height > 8) {
                for (BlockPos corner : new BlockPos[]{center.north().east(), center.north().west(),
                        center.south().east(), center.south().west()}) {
                    if (y > 0 || FeatureUtil.isGround(level.getBlockState(corner.below()))) {
                        FeatureUtil.putIfAir(level, corner, crystal);
                    }
                }
            }
        }
    }
}
