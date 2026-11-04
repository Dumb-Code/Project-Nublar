package net.dumbcode.projectnublar.block.api.multiblock;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dumbcode.projectnublar.client.ModShapes;
import net.dumbcode.projectnublar.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for the machine multiblocks. The {@code facing}/{@code rows}/{@code columns}/
 * {@code depth} blockstate property names and ranges are frozen contracts; the (0,0,0) block is
 * the core holding the block entity.
 *
 * <p>Note: {@code SHAPES} is a static, never-invalidated per-blockstate shape cache shared by all
 * multiblocks; its semantics are preserved.
 */
public abstract class MultiEntityBlock extends BaseEntityBlock implements MultiBlock {
    private final int rows;
    private final int columns;
    private final int depth;
    public static Map<BlockState, VoxelShape> SHAPES = new HashMap<>();

    public MultiEntityBlock(Properties properties, int rows, int columns, int depth) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ROWS, 0)
                .setValue(COLUMNS, 0)
                .setValue(FACING, Direction.NORTH)
                .setValue(DEPTH, 0));
        this.rows = rows;
        this.columns = columns;
        this.depth = depth;
    }

    /**
     * Builds the codec of a multiblock subclass: the block properties plus the rows, columns and
     * depth its constructor takes.
     */
    protected static <B extends MultiEntityBlock> MapCodec<B> multiBlockCodec(Function4<Properties, Integer, Integer, Integer, B> constructor) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                        propertiesCodec(),
                        Codec.INT.fieldOf("rows").forGetter(block -> ((MultiEntityBlock) block).rows),
                        Codec.INT.fieldOf("columns").forGetter(block -> ((MultiEntityBlock) block).columns),
                        Codec.INT.fieldOf("depth").forGetter(block -> ((MultiEntityBlock) block).depth))
                .apply(i, constructor));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            BlockPos corePos = MultiBlock.getCorePos(pState, pPos);
            this.openContainer(pLevel, corePos, pPlayer);
            return InteractionResult.CONSUME;
        }

    }

    protected void openContainer(Level pLevel, BlockPos pPos, Player pPlayer) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof BaseContainerBlockEntity) {
            pPlayer.openMenu((MenuProvider) blockentity);
            // TODO(DEAD): an open-container stat award was planned but never implemented.
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
        $$0.add(ROWS, COLUMNS, DEPTH, FACING);
    }

    /**
     * The full blocks the whole multiblock fills when its core stands at {@code corePos} in {@code coreState}:
     * {@code rows} blocks up, {@code columns} blocks counter-clockwise of the facing and {@code depth} blocks away
     * from it, the same blocks {@link #affectNeighborsAfterRemoval} breaks.
     */
    public AABB getFootprint(BlockPos corePos, BlockState coreState) {
        Direction facing = coreState.getValue(FACING);
        BlockPos farCorner = corePos.relative(facing.getOpposite(), depth - 1)
                .relative(facing.getCounterClockWise(), columns - 1)
                .above(rows - 1);
        return AABB.encapsulatingFullBlocks(corePos, farCorner);
    }

    /**
     * Finds the core of the machine that the block at {@code pos} belongs to. Only the core has the
     * block entity that stores the machine's energy, so the generator and the loader energy
     * lookups use this for any block of a machine.
     *
     * @param level the level of the block
     * @param pos   the position of the block
     * @param state the block's state at {@code pos}, a state of a {@code MultiEntityBlock}
     * @return {@code pos} itself for a core; the core's position for another block of a machine;
     *         null when the core's position is not loaded (a lookup never loads a chunk), or holds
     *         another block or a block of this machine that is not a core (a broken machine left
     *         by a command or a world edit, so two such blocks never send a lookup back and forth)
     */
    public static @Nullable BlockPos findCore(Level level, BlockPos pos, BlockState state) {
        BlockPos corePos = MultiBlock.getCorePos(state, pos);
        if (corePos.equals(pos)) {
            return pos;
        }
        if (!level.isLoaded(corePos)) {
            return null;
        }
        BlockState coreState = level.getBlockState(corePos);
        boolean isCore = coreState.is(state.getBlock())
                && MultiBlock.getCorePos(coreState, corePos).equals(corePos);
        return isCore ? corePos : null;
    }

    public abstract VoxelShape getShapeForDirection(Direction direction);
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        int columns = pState.getValue(COLUMNS);
        int rows = pState.getValue(ROWS);
        int depth = pState.getValue(DEPTH);
        Direction direction = pState.getValue(FACING);
        return SHAPES.computeIfAbsent(pState,(state)->switch (pState.getValue(FACING)) {
            case NORTH ->getShapeForDirection(direction).move(columns, -rows, -depth);
            case SOUTH -> getShapeForDirection(direction).move(-columns, -rows, depth);
            case EAST -> getShapeForDirection(direction).move(depth, -rows, columns);
            case WEST -> getShapeForDirection(direction).move(-depth, -rows, -columns);
            default -> getShapeForDirection(direction);
        });
    }

    /**
     * The block model has only a particle texture, so the chunk mesh draws nothing and the block
     * entity renderer draws the machine. 26.2 dropped {@code ENTITYBLOCK_ANIMATED}; vanilla chests
     * moved to {@code MODEL} with the same kind of particle-only model.
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Runs on the server after this block is set at {@code pos}: refreshes the loader's capability
     * caches there ({@code IPlatformHelper.invalidateCapabilities}). Every block of a machine
     * answers an energy lookup for the core, but a loader refreshes on its own only where a block
     * entity comes or goes, that is at the core. So a cable that looked at this space before the
     * machine stood here sees the machine now.
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState,
                           boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel) {
            Services.PLATFORM.invalidateCapabilities(serverLevel, pos);
        }
    }

    /**
     * Runs on the server after this block is replaced by a different block: refreshes the loader's
     * capability caches at {@code pPos} (see {@link #onPlace}), so no cable keeps the energy of a
     * removed machine, updates comparators at the core and breaks the other blocks of the
     * multiblock. The core's inventory and machine parts drop before this, in the block entity's
     * {@code preRemoveSideEffects}.
     */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState pState, ServerLevel pLevel, BlockPos pPos, boolean pMovedByPiston) {
        Services.PLATFORM.invalidateCapabilities(pLevel, pPos);
        if (pState.getValue(ROWS) == 0 && pState.getValue(COLUMNS) == 0 && pState.getValue(DEPTH) == 0) {
            Containers.updateNeighboursAfterDestroy(pState, pLevel, pPos);
        }
        //break the other blocks
        Direction direction = pState.getValue(FACING);
        BlockPos corePos = pPos.relative(direction, pState.getValue(DEPTH)).relative(direction.getClockWise(), pState.getValue(COLUMNS)).relative(Direction.UP, -pState.getValue(ROWS));
        //use r c d for loops
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                for (int d = 0; d < depth; d++) {
                    BlockPos blockPos = corePos.relative(direction.getOpposite(), d).relative(direction.getCounterClockWise(), c).relative(Direction.UP, r);
                    if (!pLevel.getBlockState(blockPos).isAir() && pLevel.getBlockState(blockPos).is(this)) {
                        pLevel.destroyBlock(blockPos, false);
                    }
                }
            }
        }


        super.affectNeighborsAfterRemoval(pState, pLevel, pPos, pMovedByPiston);
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(ROWS) == 0 && blockState.getValue(COLUMNS) == 0 && blockState.getValue(DEPTH) == 0)
            return createBlockEntity(blockPos,blockState);
        return null;
    }
    public abstract BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState);



}
