package fr.astralnexus.astralpack.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/** Stalactite : pointe de roche (parfois de cristal) accrochée sous une île, plus épaisse en haut. */
public class StalactiteFeature extends Feature<NexusFeatureConfig> {
    public StalactiteFeature() {
        super(NexusFeatureConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NexusFeatureConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (!level.getBlockState(origin).isAir() || !FeatureUtil.isSolid(level.getBlockState(origin.above()))) {
            return false;
        }
        int length = 3 + random.nextInt(8) * context.config().size();
        boolean crystalline = random.nextFloat() < 0.3F;
        BlockState rock = random.nextBoolean() ? FeatureUtil.stellar() : FeatureUtil.abyssal();
        for (int i = 0; i < length; i++) {
            BlockPos pos = origin.below(i);
            if (!level.getBlockState(pos).isAir()) {
                break;
            }
            FeatureUtil.put(level, pos, crystalline && i >= length - 3 ? FeatureUtil.crystal() : rock);
            if (i < length * 0.35F) {
                for (BlockPos arm : new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west()}) {
                    FeatureUtil.putIfAir(level, arm, rock);
                }
            }
        }
        return true;
    }
}
