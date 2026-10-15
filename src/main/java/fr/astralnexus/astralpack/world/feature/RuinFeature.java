package fr.astralnexus.astralpack.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Vestiges d'une ancienne civilisation : une arche à moitié effondrée, ou une petite salle aux murs éboulés. Pierre
 * stellaire, quelques blocs d'anneau astral comme clef de voûte ou chapiteau, et de la mousse lumineuse qui reprend ses droits.
 */
public class RuinFeature extends Feature<NexusFeatureConfig> {
    private static final int HALL_HALF = 3;
    private static final int ARCH_HALF = 4;

    public RuinFeature() {
        super(NexusFeatureConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NexusFeatureConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (context.config().tooCloseToCenter(origin) || !level.getBlockState(origin).isAir()
                || !FeatureUtil.isGround(level.getBlockState(origin.below()))) {
            return false;
        }
        if (random.nextBoolean()) {
            return arch(level, origin, random);
        }
        return hall(level, origin, random);
    }

    private boolean arch(WorldGenLevel level, BlockPos origin, RandomSource random) {
        boolean alongX = random.nextBoolean();
        int[] ground = new int[2];
        for (int side = 0; side < 2; side++) {
            int offset = side == 0 ? -ARCH_HALF : ARCH_HALF;
            int x = origin.getX() + (alongX ? offset : 0);
            int z = origin.getZ() + (alongX ? 0 : offset);
            if (!FeatureUtil.hasGround(level, x, z)) {
                return false;
            }
            ground[side] = FeatureUtil.groundY(level, x, z);
        }
        if (Math.abs(ground[0] - ground[1]) > 3) {
            return false;
        }
        int base = Math.min(ground[0], ground[1]);
        int height = 6 + random.nextInt(3);
        int broken = random.nextInt(3) == 0 ? -1 : random.nextInt(2);
        BlockState stone = FeatureUtil.stellar();
        for (int side = 0; side < 2; side++) {
            int offset = side == 0 ? -ARCH_HALF : ARCH_HALF;
            int pillar = side == broken ? height - 2 - random.nextInt(3) : height;
            for (int y = ground[side] + 1; y <= base + pillar; y++) {
                BlockPos pos = at(origin, alongX, offset, y);
                FeatureUtil.put(level, pos, stone);
                if (y <= base + 1) {
                    FeatureUtil.putIfAir(level, at(origin, alongX, offset + (side == 0 ? 1 : -1), y), stone);
                }
            }
            if (side != broken && random.nextFloat() < 0.5F) {
                FeatureUtil.put(level, at(origin, alongX, offset, base + pillar + 1), FeatureUtil.moss());
            }
        }
        int cutoff = broken < 0 ? ARCH_HALF + 1 : random.nextInt(3);
        for (int step = 0; step <= 180; step += 6) {
            double angle = Math.toRadians(step);
            int offset = (int) Math.round(Math.cos(angle) * ARCH_HALF);
            int y = base + height + (int) Math.round(Math.sin(angle) * 3.0D);
            boolean onBrokenSide = broken >= 0 && ((broken == 0 && offset < 0) || (broken == 1 && offset > 0));
            if (onBrokenSide && Math.abs(offset) < ARCH_HALF - cutoff) {
                continue;
            }
            BlockPos pos = at(origin, alongX, offset, y);
            FeatureUtil.putIfAir(level, pos, stone);
        }
        if (broken < 0) {
            FeatureUtil.put(level, at(origin, alongX, 0, base + height + 3), FeatureUtil.ring());
        }
        return true;
    }

    private BlockPos at(BlockPos origin, boolean alongX, int offset, int y) {
        return new BlockPos(origin.getX() + (alongX ? offset : 0), y, origin.getZ() + (alongX ? 0 : offset));
    }

    private boolean hall(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int[][] ground = new int[2 * HALL_HALF + 1][2 * HALL_HALF + 1];
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int dx = -HALL_HALF; dx <= HALL_HALF; dx++) {
            for (int dz = -HALL_HALF; dz <= HALL_HALF; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                if (!FeatureUtil.hasGround(level, x, z)) {
                    return false;
                }
                int y = FeatureUtil.groundY(level, x, z);
                ground[dx + HALL_HALF][dz + HALL_HALF] = y;
                min = Math.min(min, y);
                max = Math.max(max, y);
            }
        }
        if (max - min > 2) {
            return false;
        }
        BlockState stone = FeatureUtil.stellar();
        for (int dx = -HALL_HALF; dx <= HALL_HALF; dx++) {
            for (int dz = -HALL_HALF; dz <= HALL_HALF; dz++) {
                int y = ground[dx + HALL_HALF][dz + HALL_HALF];
                BlockPos floor = new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
                if (random.nextFloat() < 0.75F) {
                    FeatureUtil.put(level, floor, stone);
                }
                boolean edgeX = Math.abs(dx) == HALL_HALF;
                boolean edgeZ = Math.abs(dz) == HALL_HALF;
                if (!edgeX && !edgeZ) {
                    continue;
                }
                boolean corner = edgeX && edgeZ;
                boolean doorway = !corner && (edgeX ? Math.abs(dz) <= 1 : Math.abs(dx) <= 1);
                int wall = corner ? 4 + random.nextInt(3) : (doorway ? 0 : random.nextInt(5));
                for (int h = 1; h <= wall; h++) {
                    FeatureUtil.putIfAir(level, floor.above(h), stone);
                }
                if (corner && random.nextFloat() < 0.6F) {
                    FeatureUtil.putIfAir(level, floor.above(wall + 1), FeatureUtil.ring());
                } else if (wall > 0 && random.nextFloat() < 0.35F) {
                    FeatureUtil.putIfAir(level, floor.above(wall + 1), FeatureUtil.moss());
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            int x = origin.getX() + random.nextInt(2 * HALL_HALF + 5) - HALL_HALF - 2;
            int z = origin.getZ() + random.nextInt(2 * HALL_HALF + 5) - HALL_HALF - 2;
            if (FeatureUtil.hasGround(level, x, z)) {
                FeatureUtil.putIfAir(level, new BlockPos(x, FeatureUtil.groundY(level, x, z) + 1, z), stone);
            }
        }
        return true;
    }
}
