package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.api.fence.BlockConnectableBase;
import net.dumbcode.projectnublar.block.api.fence.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.api.fence.ConnectionType;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFenceBase;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.util.LineUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A fence post column. Each post is {@code type.getHeight()} blocks tall; the
 * {@code indexProperty} value is the block's height within the column (0 = base). The
 * {@code powered} and index blockstate property names are frozen contracts.
 */
public class ElectricFencePostBlock extends BlockConnectableBase implements EntityBlock {

    public final ConnectionType type;
    public final IntegerProperty indexProperty;
    public static final BooleanProperty POWERED_PROPERTY = BooleanProperty.create("powered");

    /** Re-entrancy guard while a column tears itself down */
    private static boolean destroying = false;

    /** Maximum wire run length in blocks. */
    public static final int LIMIT = 15;

    /** NBT key on the wire spool item storing the first clicked post. */
    private static final String FENCE_POSITION_TAG = "fence_position";

    public ElectricFencePostBlock(Properties properties, ConnectionType type, IntegerProperty indexProperty) {
        super(properties);
        this.type = type;
        this.indexProperty = indexProperty;
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED_PROPERTY, false).setValue(indexProperty,0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED_PROPERTY);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getDefaultShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof BlockEntityElectricFencePole) {
            return ((BlockEntityElectricFencePole) entity).getCachedShape();
        }
        return Shapes.block();
    }


    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        boolean flag = true;
        for (int i = 0; i < this.type.getHeight(); i++) {
            flag &= world.getBlockState(pos.above(i)).getBlock().canBeReplaced(state, Fluids.EMPTY);
        }
        return flag;
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(this.indexProperty, 0);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState old, boolean p_220082_5_) {
        if (!world.isClientSide && !state.is(old.getBlock()) && state.getValue(indexProperty) == 0) {
            for (int i = 1; i < this.type.getHeight(); i++) {
                world.setBlock(pos.above(i), this.defaultBlockState().setValue(indexProperty, i), 3);
            }

        }
        super.onPlace(state, world, pos, old, p_220082_5_);
    }


    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        int index = state.getValue(indexProperty);
        if (index == 0) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.isEmpty()) {
                if (world.isClientSide) {
                    return InteractionResult.SUCCESS;
                }
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof BlockEntityElectricFencePole fencePole) {
                    flipPole(world, pos, fencePole);
                    return InteractionResult.SUCCESS;
                }
            } else if (stack.getItem() == ItemInit.WIRE_SPOOL.get()) {
                if (world.isClientSide) {
                    return InteractionResult.SUCCESS;
                }
                // Pre-existing note: this could live on the wire-spool item class instead.
                handleWireSpoolUse(world, pos, player, stack);
                return InteractionResult.SUCCESS;
            }
        } else if (world.getBlockState(pos.below(index)).getBlock() == this) {
            // Clicks on upper column blocks are redirected to the base block.
            return this.use(world.getBlockState(pos.below(index)), world, pos.below(index), player, hand, ray);
        }
        return super.use(state, world, pos, player, hand, ray);
    }

    /** Empty-hand interaction: flips the pole's facing and refreshes the column's models. */
    private void flipPole(Level world, BlockPos pos, BlockEntityElectricFencePole fencePole) {
        fencePole.setFlippedAround(!fencePole.isFlippedAround());
        fencePole.setChanged();
        for (int y = 0; y < this.type.getHeight(); y++) {
            BlockEntity t = world.getBlockEntity(pos.above(y));
            if (t instanceof BlockEntityElectricFencePole pole) {
                pole.triggerModelUpdate();
            }
        }
    }

    /**
     * Wire-spool interaction: the first click stores this post on the spool; the second click
     * strings wires from the stored post to this one (within {@link #LIMIT}), consuming spools.
     */
    private void handleWireSpoolUse(Level world, BlockPos pos, Player player, ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTagElement(Constants.MODID);
        if (nbt.contains(FENCE_POSITION_TAG, Tag.TAG_COMPOUND)) {
            BlockPos other = NbtUtils.readBlockPos(nbt.getCompound(FENCE_POSITION_TAG));
            double dist = Math.sqrt(other.distSqr(pos));
            if (dist > LIMIT) {
                player.displayClientMessage(Component.literal("Fence run is too long (" + Math.round(dist) + "/" + LIMIT + " blocks)"), true);
                nbt.put(FENCE_POSITION_TAG, NbtUtils.writeBlockPos(pos));
            } else if (world.getBlockState(other).getBlock() == this && !other.equals(pos)) {
                placeWireRun(world, pos, other, player, stack, dist);
                nbt.put(FENCE_POSITION_TAG, NbtUtils.writeBlockPos(pos));
            } else {
                nbt.remove(FENCE_POSITION_TAG);
            }
        } else {
            nbt.put(FENCE_POSITION_TAG, NbtUtils.writeBlockPos(pos));
        }
    }

    /** Collects enough spool items across the inventory, then strings every wire of the run. */
    private void placeWireRun(Level world, BlockPos pos, BlockPos other, Player player, ItemStack stack, double dist) {
        int required = Mth.ceil(dist / ElectricFenceBlock.ITEM_FOLD * this.type.getHeight());
        if (!hasWirePlacementWork(world, pos, other)) {
            return;
        }

        SpoolInventoryUse inventoryUse = collectWireSpools(player, stack, required);
        if (!inventoryUse.hasEnough()) {
            player.displayClientMessage(Component.translatable("projectnublar.fences.length.notenough", required, inventoryUse.totalFound()), true);
        } else {
            if (!player.isCreative()) {
                inventoryUse.consume();
            }
            for (double offset : this.type.getOffsets()) {
                List<BlockPos> positions = LineUtils.getBlocksInbetween(pos, other, offset);
                for (int i = 0; i < this.type.getHeight(); i++) {
                    BlockPos pos1 = pos.above(i);
                    BlockPos other1 = other.above(i);
                    for (int i1 = 0; i1 < positions.size(); i1++) {
                        BlockPos position = positions.get(i1).above(i);
                        BlockState targetState = world.getBlockState(position);
                        if ((targetState.isAir() || targetState.canBeReplaced(Fluids.EMPTY)) && !(targetState.getBlock() instanceof ElectricFencePostBlock)) {
                            world.setBlock(position, BlockInit.ELECTRIC_FENCE.get().defaultBlockState(), 3);
                        }
                        BlockEntity fencete = world.getBlockEntity(position);
                        if (fencete instanceof ConnectableBlockEntity) {
                            ((ConnectableBlockEntity) fencete).addConnection(new Connection(
                                fencete,
                                this.type,
                                offset,
                                pos1,
                                other1,
                                positions.get(Math.max(i1 - 1, 0)).above(i),
                                positions.get(Math.min(i1 + 1, positions.size() - 1)).above(i),
                                position));
                        }
                    }
                }
            }
        }
    }

    private boolean hasWirePlacementWork(Level world, BlockPos pos, BlockPos other) {
        for (double offset : this.type.getOffsets()) {
            List<BlockPos> positions = LineUtils.getBlocksInbetween(pos, other, offset);
            for (int y = 0; y < this.type.getHeight(); y++) {
                BlockPos from = pos.above(y);
                BlockPos to = other.above(y);
                for (BlockPos basePosition : positions) {
                    BlockPos position = basePosition.above(y);
                    BlockState targetState = world.getBlockState(position);
                    if ((targetState.isAir() || targetState.canBeReplaced(Fluids.EMPTY))
                        && !(targetState.getBlock() instanceof ElectricFencePostBlock)) {
                        return true;
                    }

                    BlockEntity blockEntity = world.getBlockEntity(position);
                    if (blockEntity instanceof ConnectableBlockEntity connectable) {
                        Connection existing = findMatchingConnection(connectable, from, to, offset);
                        if (existing == null || existing.isBroken()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    private static Connection findMatchingConnection(ConnectableBlockEntity connectable, BlockPos from, BlockPos to, double offset) {
        for (Connection connection : connectable.getConnections()) {
            if (matchesConnection(connection, from, to, offset)) {
                return connection;
            }
        }
        return null;
    }

    private static boolean matchesConnection(Connection connection, BlockPos from, BlockPos to, double offset) {
        return Double.compare(connection.getOffset(), offset) == 0
            && ((connection.getFrom().equals(from) && connection.getTo().equals(to))
                || (connection.getFrom().equals(to) && connection.getTo().equals(from)));
    }

    private static SpoolInventoryUse collectWireSpools(Player player, ItemStack heldStack, int required) {
        int remaining = required;
        int totalFound = 0;
        List<SpoolStackUse> stacks = new ArrayList<>();

        int fromHeld = Math.min(remaining, heldStack.getCount());
        if (fromHeld > 0) {
            stacks.add(new SpoolStackUse(heldStack, fromHeld));
            totalFound += fromHeld;
            remaining -= fromHeld;
        }

        for (ItemStack itemStack : player.getInventory().items) {
            if (remaining <= 0) {
                break;
            }
            if (itemStack != heldStack && itemStack.getItem() == ItemInit.WIRE_SPOOL.get()) {
                int fromStack = Math.min(remaining, itemStack.getCount());
                if (fromStack > 0) {
                    stacks.add(new SpoolStackUse(itemStack, fromStack));
                    totalFound += fromStack;
                    remaining -= fromStack;
                }
            }
        }

        return new SpoolInventoryUse(required, totalFound, stacks);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            return;
        }

        if (!level.isClientSide && !destroying) {
            BlockPos base = pos.below(state.getValue(this.indexProperty));
            breakRunsForRemovedColumn(level, base);
            removeColumnBlocks(level, pos, state);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void breakRunsForRemovedColumn(Level level, BlockPos base) {
        List<Connection> columnConnections = new ArrayList<>();
        for (int y = 0; y < this.type.getHeight(); y++) {
            BlockEntity blockEntity = level.getBlockEntity(base.above(y));
            if (blockEntity instanceof BlockEntityElectricFencePole fencePole) {
                columnConnections.addAll(fencePole.getConnections());
            }
        }
        for (Connection connection : columnConnections) {
            breakRunForRemovedEndpoint(level, connection);
        }
    }

    private void removeColumnBlocks(Level level, BlockPos pos, BlockState state) {
        if (!destroying) {
            destroying = true;
            try {
                int index = state.getValue(indexProperty);
                BlockPos base = pos.below(index);
                for (int i = 0; i < this.type.getHeight(); i++) {
                    BlockPos columnPos = base.above(i);
                    if (columnPos.equals(pos)) {
                        continue;
                    }
                    BlockState columnState = level.getBlockState(columnPos);
                    if (columnState.getBlock() == this && columnState.getValue(this.indexProperty) == i) {
                        level.setBlock(columnPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            } finally {
                destroying = false;
            }
        }
    }

    private static void breakRunForRemovedEndpoint(Level level, Connection removedConnection) {
        for (BlockPos blockPos : LineUtils.getBlocksInbetween(removedConnection.getFrom(), removedConnection.getTo(), removedConnection.getOffset())) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (!(blockEntity instanceof ConnectableBlockEntity connectableBlockEntity)) {
                continue;
            }

            boolean changed = false;
            boolean hasLiveConnection = false;
            for (Connection connection : connectableBlockEntity.getConnections()) {
                if (removedConnection.lazyEquals(connection)) {
                    changed |= connection.setBrokenSilently(true);
                }
                hasLiveConnection |= !connection.isBroken();
            }

            if (!changed) {
                continue;
            }

            if (blockEntity instanceof BlockEntityElectricFenceBase fence) {
                fence.onConnectionChanged();
            }
            blockEntity.setChanged();

            BlockState state = level.getBlockState(blockPos);
            if (!hasLiveConnection && state.is(BlockInit.ELECTRIC_FENCE.get())) {
                level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
            } else {
                level.sendBlockUpdated(blockPos, state, state, 3);
            }
        }
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
        super.destroy(world, pos, state);
    }


    @Override
    public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getValue(POWERED_PROPERTY) && state.getValue(indexProperty) == this.type.getHeight() - 1 ? this.type.getLightLevel() : 0;
    }


    public ConnectionType getType() {
        return type;
    }

    public IntegerProperty getIndexProperty() {
        return indexProperty;
    }

    public static boolean isDestroying() {
        return destroying;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityElectricFencePole(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlockInit.ELECTRIC_FENCE_POST_BLOCK_ENTITY.get(), (level, pos, state, be) -> be.tick(level, pos, state, be));
    }
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
        return pClientType == pServerType ? (BlockEntityTicker<A>)pTicker : null;
    }

    private record SpoolStackUse(ItemStack stack, int amount) {
        private void consume() {
            this.stack.shrink(this.amount);
        }
    }

    private record SpoolInventoryUse(int required, int totalFound, List<SpoolStackUse> stacks) {
        private boolean hasEnough() {
            return this.totalFound >= this.required;
        }

        private void consume() {
            for (SpoolStackUse stack : this.stacks) {
                stack.consume();
            }
        }
    }
}
