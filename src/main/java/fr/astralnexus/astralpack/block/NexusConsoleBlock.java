package fr.astralnexus.astralpack.block;

import fr.astralnexus.astralpack.block.entity.NexusConsoleBlockEntity;
import fr.astralnexus.astralpack.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Pupitre du Nexus : on y plante la Death Scythe, qui donne l'énergie d'ouvrir le portail tant qu'elle y reste. */
public class NexusConsoleBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty PLANTED = BooleanProperty.create("planted");

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public NexusConsoleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PLANTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PLANTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NexusConsoleBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.NEXUS_CONSOLE.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<NexusConsoleBlockEntity>) NexusConsoleBlockEntity::serverTick;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (hand == InteractionHand.MAIN_HAND && level.getBlockEntity(pos) instanceof NexusConsoleBlockEntity console) {
            console.interact(player);
        }
        return InteractionResult.CONSUME;
    }

    /** Faux plantée : de petites étincelles montent de l'ouverture du plateau. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(PLANTED) && random.nextInt(3) == 0) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.4D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.4D;
            level.addParticle(random.nextInt(4) == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.END_ROD,
                    x, pos.getY() + 0.95D, z, 0.0D, 0.02D + random.nextDouble() * 0.03D, 0.0D);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof NexusConsoleBlockEntity console) {
            console.onBroken();
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
