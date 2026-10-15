package fr.astralnexus.astralpack.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Débris flottant : un petit bloc de roche suspendu dans le vide, avec un dessus de régolithe et parfois un cristal. */
public class FloatingDebrisFeature extends Feature<NexusFeatureConfig> {
    public FloatingDebrisFeature() {
        super(NexusFeatureConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NexusFeatureConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (context.config().tooCloseToCenter(origin)) {
            return false;
        }
        int size = context.config().size();
        float rx = 1.5F + random.nextFloat() * 2.5F * size;
        float ry = Math.max(1.0F, rx * (0.45F + random.nextFloat() * 0.25F));
        float rz = 1.5F + random.nextFloat() * 2.5F * size;
        int ex = (int) Math.ceil(rx) + 1;
        int ey = (int) Math.ceil(ry) + 1;
        int ez = (int) Math.ceil(rz) + 1;
        for (int dx = -ex - 1; dx <= ex + 1; dx++) {
            for (int dy = -ey - 4; dy <= ey + 4; dy++) {
                for (int dz = -ez - 1; dz <= ez + 1; dz++) {
                    if (!level.getBlockState(origin.offset(dx, dy, dz)).isAir()) {
                        return false;
                    }
                }
            }
        }
        Set<BlockPos> cells = new HashSet<>();
        List<BlockPos> order = new ArrayList<>();
        for (int dx = -ex; dx <= ex; dx++) {
            for (int dy = -ey; dy <= ey; dy++) {
                for (int dz = -ez; dz <= ez; dz++) {
                    double d = (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) + (dz * dz) / (rz * rz);
                    if (d <= 1.0D - random.nextFloat() * 0.25F) {
                        BlockPos pos = origin.offset(dx, dy, dz);
                        cells.add(pos);
                        order.add(pos);
                    }
                }
            }
        }
        for (BlockPos pos : order) {
            BlockState state;
            if (!cells.contains(pos.above())) {
                state = random.nextFloat() < 0.2F ? FeatureUtil.moss() : FeatureUtil.regolith();
            } else if (!cells.contains(pos.below())) {
                state = FeatureUtil.abyssal();
            } else {
                state = FeatureUtil.stellar();
            }
            FeatureUtil.put(level, pos, state);
        }
        for (BlockPos pos : order) {
            if (!cells.contains(pos.above()) && random.nextFloat() < 0.10F) {
                int height = 1 + random.nextInt(3);
                for (int i = 1; i <= height; i++) {
                    FeatureUtil.putIfAir(level, pos.above(i), FeatureUtil.crystal());
                }
                break;
            }
        }
        for (BlockPos pos : order) {
            if (!cells.contains(pos.below()) && random.nextFloat() < 0.12F) {
                int length = 1 + random.nextInt(3);
                for (int i = 1; i <= length; i++) {
                    FeatureUtil.putIfAir(level, pos.below(i), FeatureUtil.abyssal());
                }
            }
        }
        return true;
    }
}
