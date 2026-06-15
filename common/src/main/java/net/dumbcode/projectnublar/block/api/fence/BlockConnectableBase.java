package net.dumbcode.projectnublar.block.api.fence;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.block.api.geometry.DelegateBlockHitResult;
import net.dumbcode.projectnublar.block.api.geometry.DelegateVoxelShape;
import net.dumbcode.projectnublar.block.api.geometry.RotatedRayBox;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFenceBase;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.util.LineUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base block for anything fence wires can connect through. Owns the wire hit-testing
 * (delegating to {@link RotatedRayBox} raytraces through {@link DelegateVoxelShape}), the shock
 * collision response, and the wire-repair interactions. All raytrace and bounding-box math is
 * numerically frozen.
 */
public class BlockConnectableBase extends Block {

    // Mutable global flags used to temporarily disable wire collision while collecting shapes
    // (e.g. during fence-breaking raytraces). Set them at your own will, just remember to set
    // them back to true after collection. Static-state semantics should be changed.
    private static boolean collidableClient = true;
    private static boolean collidableServer = true;

    public BlockConnectableBase(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        AABB entityBox = entityIn.getBoundingBox();
        if (te instanceof ConnectableBlockEntity) {
            entityBox = entityBox.inflate(0.1D);
            for (ConnectionAxisAlignedBB box : ((ConnectableBlockEntity) te).getOrCreateCollisionBoxes(pos)) {
                if (entityBox.intersects(box.move(pos)) && box.getConnection().isPowered(worldIn)) {

                    Vec3 vec = new Vec3((entityBox.maxX + entityBox.minX) / 2, (entityBox.maxY + entityBox.minY) / 2, (entityBox.maxZ + entityBox.minZ) / 2);
                    vec = vec.subtract(box.getConnection().getCenter());
                    vec = vec.normalize();

                    if (worldIn instanceof ServerLevel) {
                        entityIn.hurt(new DamageSource(worldIn.registryAccess().lookup(Registries.DAMAGE_TYPE).get().get(DamageTypes.THORNS).get(), null, null), 1F);
                    }

                    if (!entityIn.onGround()) {
                        vec = vec.scale(0.4D);
                    }

                    entityIn.setDeltaMovement(new Vec3(vec.x, vec.y * 0.3D, vec.z));

                    break;
                }
            }
        }
    }


    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    protected VoxelShape getDefaultShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return this.createDelegateShape(this.estimateShape(world, pos), this.getDefaultShape(state, world, pos, context), world);
    }


    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (world instanceof ServerLevel ? collidableServer : collidableClient) {
            return this.estimateShape(world, pos);
        }
        return Shapes.empty();
    }

    protected VoxelShape estimateShape(BlockGetter world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ConnectableBlockEntity) {
            return ((ConnectableBlockEntity) te).getOrCreateCollision();
        }
        return Shapes.empty();
    }

    protected VoxelShape createDelegateShape(VoxelShape shape, VoxelShape interactionShape, BlockGetter world) {
        return new DelegateVoxelShape(shape, (from, to, offset, fallback) -> {
            DelegateBlockHitResult raytraceResult = DelegateBlockHitResult.of(getRaytraceResult(world, offset, from, to));
            DelegateBlockHitResult defaultResult = DelegateBlockHitResult.of(interactionShape.clip(from, to, offset));
            if(defaultResult == null) {
                return raytraceResult;
            }
            defaultResult.hitInfo = new DelegateVoxelShapeRender(interactionShape);
            if(raytraceResult == null) {
                return defaultResult;
            }
            return raytraceResult.getLocation().subtract(from).lengthSqr() < defaultResult.getLocation().subtract(from).lengthSqr() ? raytraceResult : defaultResult;
        });
    }

    @Nullable
    public static HitChunk getHitChunk(Player viewer) {
        Vec3 start = viewer.getEyePosition(1F);
        Vec3 vec = viewer.getViewVector(1F);
        Vec3 end = start.add(vec.x * 20, vec.y * 20, vec.z * 20);
        HitResult pick = viewer.pick(20, 1F, false);
        if (!(pick instanceof BlockHitResult)) {
            return null;
        }
        BlockHitResult result = getRaytraceResult(viewer.level(), ((BlockHitResult) pick).getBlockPos(), start, end);
        if(result instanceof DelegateBlockHitResult dbhr && dbhr.hitInfo instanceof HitChunk) {
            return (HitChunk) dbhr.hitInfo;
        }
        return null;
    }


    @Nullable
    public static DelegateBlockHitResult getRaytraceResult(BlockGetter world, BlockPos pos, Vec3 start, Vec3 end) {
        double hitDist = Double.MAX_VALUE;
        DelegateBlockHitResult resultOut = null;
        Set<BlockConnectableBase.ChunkedInfo> set = getOutlines(world, pos);

        for (BlockConnectableBase.ChunkedInfo chunk : set) {
            Connection connection = chunk.connection();
            boolean pb = connection.brokenSide(world, false);
            boolean nb = connection.brokenSide(world, true);

            List<RotatedRayBox.Result> results = Lists.newArrayList();
            if (nb || pb) {
                if (nb) {
                    results.add(connection.getNextCache().rotatedBox().rayTrace(start, end));
                    if (!pb) {
                        results.add(connection.getNextCache().fixedBox().rayTrace(start, end));
                    }
                }

                if (pb) {
                    results.add(connection.getPrevCache().rotatedBox().rayTrace(start, end));
                    if (!nb) {
                        results.add(connection.getPrevCache().fixedBox().rayTrace(start, end));
                    }
                }

            } else {
                results.add(connection.getRayBox().rayTrace(start, end));
            }

            if (!results.isEmpty()) {
                for (RotatedRayBox.Result result : results) {
                    if (result == null) {
                        continue;
                    }
                    double dist = result.distance();
                    if (dist < hitDist) {
                        resultOut = DelegateBlockHitResult.of(result.result().withPosition(pos));
                        resultOut.hitInfo = new BlockConnectableBase.HitChunk(chunk.aabb(), chunk.connection(), result.hitDir(), result);
                        hitDist = dist;
                    }
                }
            }
        }
        return resultOut;
    }

    public static Set<ChunkedInfo> getOutlines(BlockGetter world, BlockPos pos) {
        Set<ChunkedInfo> set = Sets.newLinkedHashSet();
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof ConnectableBlockEntity) {
            for (Connection connection : ((ConnectableBlockEntity) tileEntity).getConnections()) {
                if (!connection.isBroken()) {
                    double w = connection.getType().getCableWidth();
                    set.add(new ChunkedInfo(new AABB(0, -w, -w, -connection.getFullLen(), w, w), connection));

                }
            }
        }
        return set;
    }

    public static List<ConnectionAxisAlignedBB> createBoundingBox(Set<Connection> fenceConnections, BlockPos pos) {
        List<ConnectionAxisAlignedBB> out = Lists.newArrayList();
        for (Connection connection : fenceConnections) {
            if (connection.isBroken() || !connection.isValid()) {
                continue;
            }
            double[] intersect = connection.getIn();
            double amount = 8;

            double x = (intersect[1] - intersect[0]) / amount;
            double y = (intersect[5] - intersect[4]) / amount;
            double z = (intersect[3] - intersect[2]) / amount;

            for (int i = 0; i < amount; i++) {
                int next = i + 1;
                out.add(new ConnectionAxisAlignedBB(
                    new AABB(x * i, y * i, z * i, x * next, y * next, z * next)
                        .move(intersect[0] - pos.getX(), intersect[4] - pos.getY(), intersect[2] - pos.getZ())
                        .inflate(connection.getType().getCableWidth() / 2D), connection)
                );
            }
        }
        return out;
    }



    public static class ConnectionAxisAlignedBB extends AABB {

        private final Connection connection;

        public ConnectionAxisAlignedBB(AABB aabb, Connection connection) {
            super(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
            this.connection = connection;
        }

        public Connection getConnection() {
            return connection;
        }
    }

    /** Swing/consume feedback when a wire is placed. */
    public static void placeEffect(Player player, InteractionHand hand, Level worldIn, BlockPos pos) {
        if (player != null) {
            player.swing(hand);
            if (!player.isCreative()) {
                player.getItemInHand(hand).shrink(1);
            }
        }
    }

    public static void breakEffect(Level worldIn, BlockPos pos) {
        worldIn.levelEvent(2001, pos, Block.getId(worldIn.getBlockState(pos)));
    }

    public static int breakUnsupportedFloatingConnections(Level world, Iterable<Connection> boundaryConnections, boolean dropItems) {
        if (world.isClientSide) {
            return 0;
        }

        ArrayDeque<Connection> queue = new ArrayDeque<>();
        for (Connection boundary : boundaryConnections) {
            enqueueLiveNeighbor(world, boundary, queue, boundary.getPrevious());
            enqueueLiveNeighbor(world, boundary, queue, boundary.getNext());
        }

        Set<Connection> checked = new HashSet<>();
        int brokenCount = 0;
        while (!queue.isEmpty()) {
            Connection start = queue.removeFirst();
            if (start.isBroken() || checked.contains(start)) {
                continue;
            }

            List<Connection> component = collectLiveComponent(world, start);
            checked.addAll(component);
            if (component.isEmpty() || isAnchoredToFencePost(component)) {
                continue;
            }

            List<Connection> brokenConnections = new ArrayList<>();
            for (Connection connection : component) {
                if (connection.setBrokenSilently(true)) {
                    brokenConnections.add(connection);
                    if (dropItems) {
                        popResource(world, connection.getPosition(), new ItemStack(ItemInit.WIRE_SPOOL.get()));
                    }
                }
            }

            updateChangedConnectionBlocks(world, brokenConnections);
            brokenCount += brokenConnections.size();
            for (Connection brokenConnection : brokenConnections) {
                enqueueLiveNeighbor(world, brokenConnection, queue, brokenConnection.getPrevious());
                enqueueLiveNeighbor(world, brokenConnection, queue, brokenConnection.getNext());
            }
        }
        return brokenCount;
    }

    private static List<Connection> collectLiveComponent(Level world, Connection start) {
        List<Connection> component = new ArrayList<>();
        ArrayDeque<Connection> queue = new ArrayDeque<>();
        Set<Connection> visited = new HashSet<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Connection connection = queue.removeFirst();
            if (connection.isBroken() || !visited.add(connection)) {
                continue;
            }

            component.add(connection);
            enqueueLiveNeighbor(world, connection, queue, connection.getPrevious());
            enqueueLiveNeighbor(world, connection, queue, connection.getNext());
        }

        return component;
    }

    private static void enqueueLiveNeighbor(Level world, Connection reference, ArrayDeque<Connection> queue, BlockPos neighborPos) {
        if (neighborPos.equals(reference.getPosition())) {
            return;
        }

        Connection neighbor = findMatchingConnection(world, neighborPos, reference);
        if (neighbor != null && !neighbor.isBroken()) {
            queue.add(neighbor);
        }
    }

    @Nullable
    private static Connection findMatchingConnection(Level world, BlockPos pos, Connection reference) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ConnectableBlockEntity connectable)) {
            return null;
        }

        for (Connection connection : connectable.getConnections()) {
            if (reference.lazyEquals(connection)) {
                return connection;
            }
        }
        return null;
    }

    private static boolean isAnchoredToFencePost(List<Connection> component) {
        for (Connection connection : component) {
            if (connection.getPosition().equals(connection.getFrom()) || connection.getPosition().equals(connection.getTo())) {
                return true;
            }
        }
        return false;
    }

    private static void updateChangedConnectionBlocks(Level world, List<Connection> brokenConnections) {
        Set<BlockPos> changedPositions = new HashSet<>();
        for (Connection connection : brokenConnections) {
            changedPositions.add(connection.getPosition());
        }

        for (BlockPos changedPos : changedPositions) {
            BlockEntity blockEntity = world.getBlockEntity(changedPos);
            if (blockEntity instanceof BlockEntityElectricFenceBase fence) {
                fence.onConnectionChanged();
            }
            if (blockEntity != null) {
                blockEntity.setChanged();
            }

            BlockState state = world.getBlockState(changedPos);
            if (!hasLiveConnections(blockEntity) && state.is(BlockInit.ELECTRIC_FENCE.get())) {
                world.destroyBlock(changedPos, false);
            } else {
                world.sendBlockUpdated(changedPos, state, state, 3);
            }
        }
    }

    private static boolean hasLiveConnections(@Nullable BlockEntity blockEntity) {
        if (!(blockEntity instanceof ConnectableBlockEntity connectable)) {
            return false;
        }

        for (Connection connection : connectable.getConnections()) {
            if (!connection.isBroken()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (world.isClientSide) {
            return;
        }
        HitChunk chunk = getHitChunk(player);
        if (chunk != null) {
            Connection brokenConnection = chunk.connection();
            brokenConnection.setBroken(true);
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof ConnectableBlockEntity) {
                for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
                    if (!connection.isBroken()) {
                        breakEffect(world, pos);
                    }
                }
                te.setChanged();
            }
            breakUnsupportedFloatingConnections(world, List.of(brokenConnection), true);

        }
    }


    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        HitChunk chunk = getHitChunk(ray);
        if (chunk == null) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ConnectableBlockEntity connectable)) {
            return InteractionResult.SUCCESS;
        }

        if (tryRepairVerticalConnection(connectable, blockEntity, chunk, player, hand, world, pos)
            || tryRepairEndpointConnection(world, player, hand, pos, chunk)) {
            return InteractionResult.SUCCESS;
        }

        Connection connection = chunk.connection();
        if (player.getItemInHand(hand).getItem() == Item.byBlock(this)) {
            return InteractionResult.CONSUME;
        }
        connection.setSign(!connection.isSign());
        blockEntity.setChanged();
        return InteractionResult.SUCCESS;
    }

    @Nullable
    private static HitChunk getHitChunk(BlockHitResult ray) {
        if (ray instanceof DelegateBlockHitResult dbhr && dbhr.hitInfo instanceof HitChunk chunk) {
            return chunk;
        }
        return null;
    }

    private static boolean tryRepairVerticalConnection(
        ConnectableBlockEntity connectable,
        BlockEntity blockEntity,
        HitChunk chunk,
        Player player,
        InteractionHand hand,
        Level world,
        BlockPos pos
    ) {
        if (chunk.dir().getAxis() != Direction.Axis.Y) {
            return false;
        }

        Connection reference = findVerticalRepairTarget(connectable, chunk.connection(), chunk.dir());
        if (reference == null || !reference.isBroken()) {
            return false;
        }

        reference.setBroken(false);
        blockEntity.setChanged();
        placeEffect(player, hand, world, pos);
        return true;
    }

    @Nullable
    private static Connection findVerticalRepairTarget(ConnectableBlockEntity connectable, Connection hitConnection, Direction direction) {
        double hitY = hitConnection.getFrom().getY() + hitConnection.getOffset();
        double bestY = direction == Direction.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
        Connection bestConnection = null;

        for (Connection connection : connectable.getConnections()) {
            double connectionY = connection.getFrom().getY() + connection.getOffset();
            if (direction == Direction.DOWN) {
                if (connectionY < hitY && connectionY > bestY) {
                    bestY = connectionY;
                    bestConnection = connection;
                }
            } else if (connectionY > hitY && connectionY < bestY) {
                bestY = connectionY;
                bestConnection = connection;
            }
        }
        return bestConnection;
    }

    private boolean tryRepairEndpointConnection(Level world, Player player, InteractionHand hand, BlockPos pos, HitChunk chunk) {
        if (chunk.dir().getAxis() != Direction.Axis.X) {
            return false;
        }

        BlockPos repairPos = getEndpointRepairPosition(chunk);
        BlockEntity repairEntity = world.getBlockEntity(repairPos);
        if (!(repairEntity instanceof ConnectableBlockEntity)) {
            if (!world.getBlockState(repairPos).canBeReplaced(Fluids.EMPTY)) {
                return false;
            }

            world.setBlock(repairPos, this.defaultBlockState(), 3);
            repairEntity = world.getBlockEntity(repairPos);
            if (repairEntity instanceof ConnectableBlockEntity connectable
                && generateConnections(world, repairPos, connectable, chunk, null)) {
                placeEffect(player, hand, world, pos);
                return true;
            }
        }

        if (repairEntity instanceof ConnectableBlockEntity connectable) {
            EndpointRepairResult result = repairExistingEndpointConnection(connectable, repairEntity, chunk.connection());
            if (result.repaired()) {
                placeEffect(player, hand, world, pos);
            }
            if (result.handled()) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos getEndpointRepairPosition(HitChunk chunk) {
        boolean hitNegativeLocalEnd = chunk.dir() == Direction.WEST;
        boolean connectionWasReversed = chunk.connection().getCompared() < 0;
        return hitNegativeLocalEnd == connectionWasReversed
            ? chunk.connection().getNext()
            : chunk.connection().getPrevious();
    }

    private static EndpointRepairResult repairExistingEndpointConnection(
        ConnectableBlockEntity connectable,
        BlockEntity blockEntity,
        Connection reference
    ) {
        for (Connection connection : connectable.getConnections()) {
            if (!connection.lazyEquals(reference)) {
                continue;
            }
            if (!connection.isBroken()) {
                return EndpointRepairResult.HANDLED;
            }
            connection.setBroken(false);
            blockEntity.setChanged();
            return EndpointRepairResult.REPAIRED;
        }
        return EndpointRepairResult.MISSED;
    }

    public static boolean generateConnections(Level worldIn, BlockPos pos, ConnectableBlockEntity be, @Nullable HitChunk chunk, @Nullable Direction side) {
        Set<Connection> newConnections = collectGeneratedConnections(worldIn, pos, be);
        Connection ref = selectGeneratedReference(newConnections, chunk, side);
        if (ref == null) {
            return false;
        }
        for (Connection connection : newConnections) {
            connection.setBrokenSilently(!connection.lazyEquals(ref));
            be.addConnection(connection);
        }
        if (be instanceof BlockEntity) {
            ((BlockEntity) be).setChanged();
        }
        return true;
    }

    private static Set<Connection> collectGeneratedConnections(Level worldIn, BlockPos pos, ConnectableBlockEntity target) {
        Set<Connection> connections = Sets.newLinkedHashSet();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    BlockEntity neighborEntity = worldIn.getBlockEntity(pos.offset(x, y, z));
                    if (neighborEntity instanceof ConnectableBlockEntity neighbor) {
                        addGeneratedConnectionsFromNeighbor(connections, pos, target, neighborEntity, neighbor);
                    }
                }
            }
        }
        return connections;
    }

    private static void addGeneratedConnectionsFromNeighbor(
        Set<Connection> connections,
        BlockPos pos,
        ConnectableBlockEntity target,
        BlockEntity neighborEntity,
        ConnectableBlockEntity neighbor
    ) {
        for (Connection connection : neighbor.getConnections()) {
            if (connection.getPrevious().equals(pos) || connection.getNext().equals(pos)) {
                addGeneratedConnection(connections, pos, target, neighborEntity, connection);
            }
        }
    }

    private static void addGeneratedConnection(
        Set<Connection> connections,
        BlockPos pos,
        ConnectableBlockEntity target,
        BlockEntity neighborEntity,
        Connection source
    ) {
        List<BlockPos> positions = LineUtils.getBlocksInbetween(source.getFrom(), source.getTo(), source.getOffset());
        for (int index = 0; index < positions.size(); index++) {
            if (!positions.get(index).equals(pos)) {
                continue;
            }

            BlockEntity owner = target instanceof BlockEntity blockEntity ? blockEntity : neighborEntity;
            connections.add(new Connection(
                owner,
                source.getType(),
                source.getOffset(),
                source.getFrom(),
                source.getTo(),
                positions.get(Math.max(index - 1, 0)),
                positions.get(Math.min(index + 1, positions.size() - 1)),
                pos));
            return;
        }
    }

    @Nullable
    private static Connection selectGeneratedReference(Set<Connection> connections, @Nullable HitChunk chunk, @Nullable Direction side) {
        Connection reference = selectVerticalReference(connections, side);
        if (chunk == null) {
            return reference;
        }

        Direction face = chunk.dir();
        if (chunk.connection().getCompared() < 0) {
            face = face.getOpposite();
        }
        if (face.getAxis() != Direction.Axis.X) {
            return reference;
        }

        for (Connection connection : connections) {
            if (chunk.connection().lazyEquals(connection)) {
                return connection;
            }
        }
        return reference;
    }

    @Nullable
    private static Connection selectVerticalReference(Set<Connection> connections, @Nullable Direction side) {
        double yRef = side == Direction.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
        Connection reference = null;
        for (Connection connection : connections) {
            double y = connection.getCenter().y;
            if (isBetterVerticalReference(side, y, yRef)) {
                yRef = y;
                reference = connection;
            }
        }
        return reference;
    }

    private static boolean isBetterVerticalReference(@Nullable Direction side, double y, double yRef) {
        return side == Direction.DOWN ? y > yRef : y < yRef;
    }

    public static void setCollidableClient(boolean client) {
        collidableClient = client;
    }

    public static void setCollidableServer(boolean server) {
        collidableServer = server;
    }


    public record  HitChunk(AABB aabb, Connection connection, Direction dir, RotatedRayBox.Result result) {

    }

    public record ChunkedInfo(AABB aabb, Connection connection) {

    }

    private record EndpointRepairResult(boolean handled, boolean repaired) {
        private static final EndpointRepairResult MISSED = new EndpointRepairResult(false, false);
        private static final EndpointRepairResult HANDLED = new EndpointRepairResult(true, false);
        private static final EndpointRepairResult REPAIRED = new EndpointRepairResult(true, true);
    }


    public record DelegateVoxelShapeRender(VoxelShape toRender) {
        
    }
}
