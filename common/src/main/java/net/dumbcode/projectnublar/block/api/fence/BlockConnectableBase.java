package net.dumbcode.projectnublar.block.api.fence;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.block.api.geometry.DelegateBlockHitResult;
import net.dumbcode.projectnublar.block.api.geometry.DelegateVoxelShape;
import net.dumbcode.projectnublar.block.api.geometry.RotatedRayBox;
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
import java.util.List;
import java.util.Set;

/**
 * Base block for anything fence wires can connect through. Owns the wire hit-testing
 * (delegating to {@link RotatedRayBox} raytraces through {@link DelegateVoxelShape}), the shock
 * collision response, and the wire-repair interactions. All raytrace and bounding-box math is
 * numerically frozen.
 *
 * <p>TODO(DEAD): several loader-specific hooks from the 1.12/Forge original were never ported
 * and their commented-out bodies were removed: ladder climbing ({@code isLadder}), the custom
 * block-highlight renderer ({@code DrawHighlightEvent}), selected-bounding-box logic, hit/destroy
 * particle suppression, and the wire-spool right-click handler (a Forge-only version lives in
 * {@code CommonForgeEvents}).
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
            for (ConnectionAxisAlignedBB box : createBoundingBox(((ConnectableBlockEntity) te).getConnections(), pos)) {
                if (entityBox.intersects(box.move(pos)) && box.getConnection().isPowered(worldIn)) {

                    Vec3 vec = new Vec3((entityBox.maxX + entityBox.minX) / 2, (entityBox.maxY + entityBox.minY) / 2, (entityBox.maxZ + entityBox.minZ) / 2);
                    vec = vec.subtract(box.getConnection().getCenter());
                    vec = vec.normalize();

                    if (worldIn instanceof ServerLevel) {
                        // TODO(DEAD): a dedicated fence damage source and spark particles were
                        // planned; currently THORNS damage with no particles (preserved).
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

    /** Swing/consume feedback when a wire is placed. TODO(DEAD): place sound never ported. */
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

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        HitChunk chunk = getHitChunk(player);
        if (chunk != null) {
            chunk.connection().setBroken(true);
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof ConnectableBlockEntity) {
                for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
                    if (!connection.isBroken()) {
                        breakEffect(world, pos);
                    }
                }
                te.setChanged();
            }

        }
    }


    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        if (ray instanceof DelegateBlockHitResult dbhr && dbhr.hitInfo instanceof HitChunk) {
            HitChunk chunk = (HitChunk) dbhr.hitInfo;
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof ConnectableBlockEntity) {
                ConnectableBlockEntity be = (ConnectableBlockEntity) te;
                Connection con = chunk.connection();
                double off = chunk.connection().getFrom().getY() + chunk.connection().getOffset();
                if (chunk.dir().getAxis() == Direction.Axis.Y) {
                    Connection ref = null;
                    double yref = chunk.dir() == Direction.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
                    for (Connection connection : be.getConnections()) {
                        double yoff = connection.getOffset() + connection.getFrom().getY();
                        if (chunk.dir() == Direction.DOWN) {
                            if (yoff < off && yoff > yref) {
                                yref = yoff;
                                ref = connection;
                            }
                        } else {
                            if (yoff > off && yoff < yref) {
                                yref = yoff;
                                ref = connection;
                            }
                        }
                    }
                    if (ref != null && ref.isBroken()) {
                        ref.setBroken(false);
                        te.setChanged();
                        placeEffect(player, hand, world, pos);
                        return InteractionResult.SUCCESS;
                    }
                } else if (chunk.dir().getAxis() == Direction.Axis.X) {
                    BlockPos nextPos = chunk.dir() == Direction.WEST == chunk.connection.getCompared() < 0 ? con.getNext() : con.getPrevious();
                    BlockEntity nextTe = world.getBlockEntity(nextPos);
                    if (!(nextTe instanceof ConnectableBlockEntity)) {
                        if (world.getBlockState(nextPos).canBeReplaced(Fluids.EMPTY)) {
                            world.setBlock(nextPos, this.defaultBlockState(), 3);
                            nextTe = world.getBlockEntity(nextPos);
                            if (nextTe instanceof ConnectableBlockEntity && generateConnections(world, nextPos, (ConnectableBlockEntity) nextTe, chunk, null)) {
                                placeEffect(player, hand, world, pos);
                            }

                        }
                    }
                    if (nextTe instanceof ConnectableBlockEntity) {
                        for (Connection connection : ((ConnectableBlockEntity) nextTe).getConnections()) {
                            if (connection.lazyEquals(chunk.connection())) {
                                connection.setBroken(false);
                                placeEffect(player, hand, world, pos);
                                nextTe.setChanged();
                                return InteractionResult.SUCCESS;
                            }
                        }
                    }
                }
                if (player.getItemInHand(hand).getItem() == Item.byBlock(this)) {
                    return InteractionResult.CONSUME;
                }
                con.setSign(!con.isSign());
                te.setChanged();
            }
        }
        return InteractionResult.SUCCESS;
    }


    public static boolean generateConnections(Level worldIn, BlockPos pos, ConnectableBlockEntity be, @Nullable HitChunk chunk, @Nullable Direction side) {
        Set<Connection> newConnections = Sets.newLinkedHashSet();
        double yRef = side == Direction.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
        Connection ref = null;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    BlockEntity tileentity = worldIn.getBlockEntity(pos.offset(x, y, z));
                    if (tileentity instanceof ConnectableBlockEntity) {
                        ConnectableBlockEntity cbe = (ConnectableBlockEntity) tileentity;
                        for (Connection connection : cbe.getConnections()) {
                            if (connection.getPrevious().equals(pos) || connection.getNext().equals(pos)) {
                                List<BlockPos> positions = LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset());
                                for (int i = 0; i < positions.size(); i++) {
                                    if (positions.get(i).equals(pos)) {
                                        Connection con = new Connection(tileentity, connection.getType(), connection.getOffset(), connection.getFrom(), connection.getTo(), positions.get(Math.min(i + 1, positions.size() - 1)), positions.get(Math.max(i - 1, 0)), pos);
                                        double[] in = con.getIn();
                                        double yin = (in[4] + in[5]) / 2D;
                                        if (side == Direction.DOWN == yin > yRef) {
                                            yRef = yin;
                                            ref = con;
                                        }
                                        newConnections.add(con);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (chunk != null) {
            Direction face = chunk.dir();
            if (chunk.connection().getCompared() < 0) {
                face = face.getOpposite();
            }
            if (face.getAxis() == Direction.Axis.X) {
                for (Connection connection : newConnections) {
                    if (chunk.connection().lazyEquals(connection)) {
                        ref = connection;
                    }
                }
            }

        }
        for (Connection connection : newConnections) {
            connection.setBroken(!connection.lazyEquals(ref));
            be.addConnection(connection);
        }
        if (be instanceof BlockEntity) {
            ((BlockEntity) be).setChanged();
        }
        return ref != null;
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


    public record DelegateVoxelShapeRender(VoxelShape toRender) {
        
    }
}
