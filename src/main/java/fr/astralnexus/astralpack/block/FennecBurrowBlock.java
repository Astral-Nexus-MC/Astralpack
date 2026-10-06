package fr.astralnexus.astralpack.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Terrier de fennec : un petit abri de sable en dôme, ouvert à l'avant. L'intérieur fait 0,75 bloc de large et de haut
 * (le Fennec mesure 0,6 x 0,7) : il y entre et s'y couche le jour. Un Fennec sauvage le construit lui-même ; pour un
 * Fennec apprivoisé, c'est le joueur qui le fabrique. L'entrée regarde le joueur à la pose.
 */
public class FennecBurrowBlock extends HorizontalDirectionalBlock {
    /** Boîtes (x1, y1, z1, x2, y2, z2) pour l'orientation NORTH : entrée côté nord, fond côté sud. */
    private static final double[][] BOXES = {
            {0, 0, 0, 16, 1, 16},
            {0, 1, 2, 2, 13, 16},
            {14, 1, 2, 16, 13, 16},
            {2, 1, 14, 14, 13, 16},
            {0, 13, 2, 16, 15, 16},
            {3, 15, 5, 13, 16, 16},
            {5, 15, 3, 11, 16, 5}
    };

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        SHAPES.put(Direction.NORTH, buildShape(0));
        SHAPES.put(Direction.EAST, buildShape(1));
        SHAPES.put(Direction.SOUTH, buildShape(2));
        SHAPES.put(Direction.WEST, buildShape(3));
    }

    public FennecBurrowBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    private static VoxelShape buildShape(int quarterTurns) {
        VoxelShape shape = Shapes.empty();
        for (double[] b : BOXES) {
            double x1 = b[0];
            double x2 = b[3];
            double z1 = b[2];
            double z2 = b[5];
            for (int i = 0; i < quarterTurns; i++) {
                double nx1 = 16 - z2;
                double nx2 = 16 - z1;
                double nz1 = x1;
                double nz2 = x2;
                x1 = nx1;
                x2 = nx2;
                z1 = nz1;
                z2 = nz2;
            }
            shape = Shapes.or(shape, Block.box(x1, b[1], z1, x2, b[4], z2));
        }
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
