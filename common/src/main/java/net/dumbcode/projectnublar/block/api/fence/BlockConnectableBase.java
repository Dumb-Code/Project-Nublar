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

public class BlockConnectableBase extends Block {

    //Set this at your own will, just remember to set it back to true after collection
    private static boolean collidableClient = true;
    private static boolean collidableServer = true;

    public BlockConnectableBase(Properties properties) {
        super(properties);
    }

//todo: when on ladder per loader
//    @Override
//    public boolean isLadder(BlockState state, BlockGetter world, BlockPos pos, LivingEntity entity) {
//        //todo: config
////        if (!ForgeConfig.SERVER.fullBoundingBoxLadders.get()) {
////            ForgeConfig.SERVER.fullBoundingBoxLadders.set(true);
////        }
//        BlockEntity te = world.getBlockEntity(pos);
//        AABB entityBox = entity.getBoundingBox();
//        boolean intersect = false;
//        if (te instanceof ConnectableBlockEntity) {
//            AABB enityxzbox = entityBox.inflate(0.025D, 0, 0.025D);
//            for (ConnectionAxisAlignedBB boxIn : this.createBoundingBox(((ConnectableBlockEntity) te).getConnections(), pos)) {
//                AABB box = boxIn.move(pos);
//                if (enityxzbox.intersects(box) && (!entityBox.inflate(0, 0.025D, 0).intersects(box) || !entityBox.inflate(0, -0.025D, 0).intersects(box))) {
//                    intersect = true;
//                    if (boxIn.getConnection().isPowered(world)) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return intersect;
//    }


    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        AABB entityBox = entityIn.getBoundingBox();
        if (te instanceof ConnectableBlockEntity) {
            entityBox = entityBox.inflate(0.1D);
            for (ConnectionAxisAlignedBB box : this.createBoundingBox(((ConnectableBlockEntity) te).getConnections(), pos)) {
                if (entityBox.intersects(box.move(pos)) && box.getConnection().isPowered(worldIn)) {

                    Vec3 vec = new Vec3((entityBox.maxX + entityBox.minX) / 2, (entityBox.maxY + entityBox.minY) / 2, (entityBox.maxZ + entityBox.minZ) / 2);
                    vec = vec.subtract(box.getConnection().getCenter());
                    vec = vec.normalize();

                    Vec3 center = this.center(box.move(pos));
                    Vec3 other = this.center(entityBox);
                    if (worldIn instanceof ServerLevel) {
                        //todo: damage
                        entityIn.hurt(new DamageSource(worldIn.registryAccess().lookup(Registries.DAMAGE_TYPE).get().get(DamageTypes.THORNS).get(),null,null), 1F);

                        int count = 30;
                        //todo: particles
//                        ((ServerWorld) worldIn).sendParticles(ProjectNublarParticles.SPARK.get(),
//                            center.x, center.y, center.z, count,
//                            (other.x - center.x) * 0.5F,
//                            (other.y - center.y) * 0.5F,
//                            (other.z - center.z) * 0.5F,
//                            1.5F
//                        );

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

    private Vec3 center(AABB box) {
        return new Vec3(box.minX + (box.maxX - box.minX) * 0.5D, box.minY + (box.maxY - box.minY) * 0.5D, box.minZ + (box.maxZ - box.minZ) * 0.5D);
    }

//    @SubscribeEvent
//    public static void onDrawBlock(DrawHighlightEvent.HighlightBlock event) {
//        IRenderTypeBuffer buffers = event.getBuffers();
//        MatrixStack stack = event.getMatrix();
//        IVertexBuilder buffer = buffers.getBuffer(RenderType.lines());
//        BlockHitResult target = event.getTarget();
//        ActiveRenderInfo info = event.getInfo();
//        Vector3d position = info.getPosition();
//        BlockPos pos = target.getBlockPos();
//        double px = -position.x();
//        double py = -position.y();
//        double pz = -position.z();
//
//        if(target.getType() == RayTraceResult.Type.BLOCK && target.hitInfo instanceof DelegateVoxelShapeRender) {
//            WorldRenderer.renderShape(stack, buffer, ((DelegateVoxelShapeRender) target.hitInfo).getToRender(), pos.getX()+px, pos.getY()+py, pos.getZ()+pz, 0F, 0F, 0F, 0.4F);
//            event.setCanceled(true);
//        }
//        if (target.getType() == RayTraceResult.Type.BLOCK && target.hitInfo instanceof BlockConnectableBase.HitChunk) {
//            World world = Minecraft.getInstance().level;
//            BlockState state = world.getBlockState(pos);
//            if (state.getBlock() instanceof BlockConnectableBase) {
//                BlockConnectableBase.HitChunk chunk = (BlockConnectableBase.HitChunk) target.hitInfo;
//                event.setCanceled(true);
//
//
//                Connection connection = chunk.getConnection();
//                double[] in = connection.getIn();
//
//                if (ProjectNublar.DEBUG) {
//                    chunk.getResult().debugRender(stack, buffers, px, py, pz);
//                }
//
//
//                double x = in[0] - position.x();
//                double y = in[4] - position.y();
//                double z = in[2] - position.z();
//
//
//                boolean pb = connection.brokenSide(world, false);
//                boolean nb = connection.brokenSide(world, true);
//
//                if (nb || pb) {
//                    Vector3d center = chunk.getAabb().getCenter();
//                    double ycent = (chunk.getAabb().maxY - chunk.getAabb().minY) / 2;
//                    double zcent = (chunk.getAabb().maxZ - chunk.getAabb().minZ) / 2;
//
//                    if (nb) {
//                        Vector3f[] bases = connection.getRayBox().points(new AABB(center.x, center.y - ycent, center.z - zcent, center.x, center.y + ycent, center.z + zcent), x, y, z);
//                        for (int i = 0; i < 4; i++) {
//                            bases[i + 4].add(connection.getNextCache().getPoint());
//                        }
//                        RenderUtils.renderBoxLines(stack, buffer, bases, Direction.SOUTH);
//                    }
//                    if (pb) {
//                        Vector3f[] bases = connection.getRayBox().points(new AABB(center.x, center.y - ycent, center.z - zcent, center.x, center.y + ycent, center.z + zcent), x, y, z);
//                        for (int i = 0; i < 4; i++) {
//                            bases[i + 4].add(connection.getPrevCache().getPoint());
//                        }
//                        RenderUtils.renderBoxLines(stack, buffer, bases, Direction.SOUTH);
//                    }
//                    if (nb != pb) {
//                        if (nb) {
//                            RenderUtils.renderBoxLines(stack, buffer, connection.getRayBox().points(new AABB(chunk.getAabb().minX, chunk.getAabb().minY, chunk.getAabb().minZ, center.x, center.y + ycent, center.z + zcent), x, y, z), Direction.NORTH);
//                        } else {
//                            RenderUtils.renderBoxLines(stack, buffer, connection.getRayBox().points(new AABB(center.x, center.y - ycent, center.z - zcent, chunk.getAabb().maxX, chunk.getAabb().maxY, chunk.getAabb().maxZ), x, y, z), Direction.SOUTH);
//                        }
//                    }
//                } else {
//                    RenderUtils.renderBoxLines(stack, buffer, connection.getRayBox().points(x, y, z));
//                }
//            }
//        }
//    }

    //    @Override
//    public AABB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
//        BlockEntity te = worldIn.getTileEntity(pos);
//        if(te instanceof ConnectableBlockEntity) {
//            double minX = Double.MAX_VALUE;
//            double minY = Double.MAX_VALUE;
//            double minZ = Double.MAX_VALUE;
//
//            double maxX = -Double.MAX_VALUE;
//            double maxY = -Double.MAX_VALUE;
//            double maxZ = -Double.MAX_VALUE;
//
//            boolean set = false;
//
//            for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
//                if(!connection.isBroken()) {
//                    double[] in = connection.getIn();
//                    set = true;
//
//                    minX = Math.min(minX, in[0]);
//                    maxX = Math.max(maxX, in[1]);
//
//                    minZ = Math.min(minZ, in[2]);
//                    maxZ = Math.max(maxZ, in[3]);
//
//                    minY = Math.min(minY, in[4]);
//                    maxY = Math.max(maxY, in[5]);
//                }
//            }
//            if(set) {
//                return new AABB(minX, minY, minZ, maxX, maxY, maxZ).grow(1/16F);
//            }
//        }
//        return super.getSelectedBoundingBox(state, worldIn, pos);
//    }

    protected VoxelShape getDefaultShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

//    @Override
//    public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {
//        return true;
//    }
    

//    @Override
//    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
//        return false;
//    }

    
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

    private final DiscreteVoxelShape discreteShape = new DiscreteVoxelShape(0,0,0) {
        @Override
        public boolean isFull(int i, int i1, int i2) {
            return false;
        }

        @Override
        public void fill(int i, int i1, int i2) {

        }

        @Override
        public int firstFull(Direction.Axis axis) {
            return 0;
        }

        @Override
        public int lastFull(Direction.Axis axis) {
            return 0;
        }
    };

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

    public static void placeEffect(Player player, InteractionHand hand, Level worldIn, BlockPos pos) {
        if (player != null) {
            player.swing(hand);
            if (!player.isCreative()) {
                player.getItemInHand(hand).shrink(1);
            }
        }
        //todo: sound
//        SoundType soundType = BlockHandler.ELECTRIC_FENCE.get().getSoundType(BlockHandler.ELECTRIC_FENCE.get().defaultBlockState());
//        worldIn.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
    }

    public static void breakEffect(Level worldIn, BlockPos pos) {
        worldIn.levelEvent(2001, pos, Block.getId(worldIn.getBlockState(pos)));
    }


//todo: on destroyed by player forge

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
//todo: on right click block forge

//    @SubscribeEvent
//    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
//        World world = event.getWorld();
//        Direction side = event.getFace();
//        if (side != null && !event.getItemStack().isEmpty() && event.getItemStack().getItem() == ItemHandler.WIRE_SPOOL.get()) {
//            BlockEntity tile = world.getBlockEntity(event.getPos().relative(side));
//            if (tile instanceof ConnectableBlockEntity) {
//                ConnectableBlockEntity cb = (ConnectableBlockEntity) tile;
//                if (side.getAxis() == Direction.Axis.Y) {
//                    double yRef = side == Direction.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
//                    Connection ref = null;
//                    for (Connection connection : cb.getConnections()) {
//                        if (connection.isBroken()) {
//                            double[] in = connection.getIn();
//                            double yin = (in[4] + in[5]) / 2D;
//                            if (side == Direction.DOWN == yin > yRef) {
//                                yRef = yin;
//                                ref = connection;
//                            }
//                        }
//                    }
//                    if (ref != null) {
//                        ref.setBroken(false);
//                        event.setCanceled(true);
//                        placeEffect(event.getPlayer(), event.getHand(), event.getWorld(), event.getPos());
//                    }
//                } else {
//                    for (Connection connection : cb.getConnections()) {
//                        if (connection.isBroken()) {
//                            connection.setBroken(false);
//                            event.setCanceled(true);
//                            placeEffect(event.getPlayer(), event.getHand(), event.getWorld(), event.getPos());
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//    }

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
