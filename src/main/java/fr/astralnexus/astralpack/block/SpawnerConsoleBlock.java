package fr.astralnexus.astralpack.block;

import fr.astralnexus.astralpack.block.entity.SpawnerConsoleBlockEntity;
import fr.astralnexus.astralpack.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Console de contrôle des spawners : une seule par chunk, elle règle les spawners de ce chunk contre des objets
 * précis (voir {@link fr.astralnexus.astralpack.spawner.SpawnerStat}). Clic droit main vide : état ; avec un objet : bonus ou malus.
 */
public class SpawnerConsoleBlock extends HorizontalDirectionalBlock implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public SpawnerConsoleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** Une seule console par chunk : une deuxième ne se pose pas. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (BlockEntity entity : context.getLevel().getChunkAt(context.getClickedPos()).getBlockEntities().values()) {
            if (entity instanceof SpawnerConsoleBlockEntity) {
                return null;
            }
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpawnerConsoleBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.SPAWNER_CONSOLE.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<SpawnerConsoleBlockEntity>) SpawnerConsoleBlockEntity::serverTick;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !(level.getBlockEntity(pos) instanceof SpawnerConsoleBlockEntity console)) {
            return InteractionResult.PASS;
        }
        if (held.isEmpty()) {
            if (!level.isClientSide) {
                console.sendStatus(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) {
            return fr.astralnexus.astralpack.spawner.SpawnerStat.forItem(held, new boolean[1]) != null
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return console.tryUpgrade(player, held) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    /** Console cassée : les spawners du chunk retrouvent leurs réglages d'origine. */
    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof SpawnerConsoleBlockEntity console) {
            console.restoreAll();
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
