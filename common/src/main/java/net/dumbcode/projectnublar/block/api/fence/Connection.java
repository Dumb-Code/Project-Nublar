package net.dumbcode.projectnublar.block.api.fence;

import net.dumbcode.projectnublar.block.api.geometry.RotatedRayBox;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFenceBase;
import net.dumbcode.projectnublar.util.LineUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;


/**
 * One wire segment of an electric fence, running between two posts ({@code from}/{@code to}) at a
 * vertical {@code offset}, passing through {@code position}. Combines three concerns that share
 * the same intersection data ({@code in}):
 *
 * <ul>
 *   <li><b>Geometry</b>: the rotated ray boxes and collision shape used for picking/collision -
 *       all math here is numerically frozen ({@code RotatedRayBox}, {@code LineUtils}).
 *   <li><b>Persistence</b>: {@link #writeToNBT}/{@link #fromNBT} with frozen tag names
 *       ({@code id}, {@code offset}, {@code from}, {@code to}, {@code sign}, {@code next},
 *       {@code previous}, {@code broken}).
 *   <li><b>Rendering</b>: precomputed vertex arrays ({@link RenderData}) consumed by
 *       {@code RenderUtils.drawSpacedCube}.
 * </ul>
 */
public class Connection {

    // NBT tag names (frozen save contracts).
    private static final String ID_TAG = "id";
    private static final String OFFSET_TAG = "offset";
    private static final String FROM_TAG = "from";
    private static final String TO_TAG = "to";
    private static final String SIGN_TAG = "sign";
    private static final String NEXT_TAG = "next";
    private static final String PREVIOUS_TAG = "previous";
    private static final String BROKEN_TAG = "broken";

    private static final int START_X = 0;
    private static final int END_X = 1;
    private static final int START_Z = 2;
    private static final int END_Z = 3;
    private static final int START_Y = 4;
    private static final int END_Y = 5;
    private static final int UV_COUNT = 12;
    private static final int RENDER_DATA_LENGTH = 39;
    private static final int VERTEX_DATA_LENGTH = 24;
    private static final double TEXTURE_PIXEL = 1D / 64D;
    // The loose wire end pivots at the straight half. Keep the bend visible without
    // letting the prism cut back into the intact segment near the shared center.
    private static final double BROKEN_WIRE_MAX_BEND_ANGLE = Math.PI / 7D;
    private static final double BROKEN_WIRE_MIN_BEND = 0.25D;
    private static final double BROKEN_WIRE_SIDE_BEND = 0.9D;

    private final Runnable reRenderCallback;
    private final ConnectionType type;

    private final double offset;
    /** Used to help compare Connections. */
    private final int toFromHash;
    private final BlockPos from;
    private final BlockPos to;

    private final BlockPos next;
    private final BlockPos previous;

    private boolean sign;

    private final BlockPos position;
    private final int compared;

    private RenderData renderData;
    private CompiledRenderData compiledRenderData;
    private int compiledRenderDataState = Integer.MIN_VALUE;

    private boolean broken;

    private final double[] in;
    private final boolean valid;

    private final double xzlen;
    private final double fullLen;

    private final Random random;

    private final Vec3 center;
    private final RotatedRayBox rayBox;

    private final SurroundingCache prevCache;
    private final SurroundingCache nextCache;

    private final VoxelShape collisionShape;

    public Connection(BlockEntity internalBlockEntity, ConnectionType type, double offset, BlockPos from, BlockPos to, BlockPos previous, BlockPos next, BlockPos position) {
        this(() -> {
            if (internalBlockEntity instanceof BlockEntityElectricFenceBase fence) {
                fence.onConnectionChanged();
            } else {
                Level level = internalBlockEntity.getLevel();
                if (level != null) {
                    level.sendBlockUpdated(position, Blocks.AIR.defaultBlockState(), internalBlockEntity.getBlockState(), 3);
                }
            }
        }, type, offset, from, to, previous, next, position);
    }

    private Connection(Runnable reRenderCallback, ConnectionType type, double offset, BlockPos from, BlockPos to, BlockPos previous, BlockPos next, BlockPos position) {
        this.reRenderCallback = reRenderCallback;
        this.type = type;
        this.offset = offset;
        this.position = position;

        if ((this.compared = (from.getX() == to.getX() ? to.getZ() - from.getZ() : from.getX() - to.getX())) < 0) {
            BlockPos ref = from;
            from = to;
            to = ref;

            ref = previous;
            previous = next;
            next = ref;

        }

        this.from = from;
        this.to = to;
        this.next = next;
        this.previous = previous;

        this.toFromHash = (this.compared < 0 ? this.from : this.to).hashCode() + (this.compared < 0 ? this.to : this.from).hashCode() * 31;

        double[] intercept = LineUtils.intersect(this.position, this.from, this.to, this.offset);
        if (intercept == null) {
            intercept = new double[6]; //ew
            this.valid = false;
        } else {
            this.valid = true;
        }

        this.random = new Random(this.getPosition().asLong() * (long) (this.getOffset() * 1000));
        this.in = intercept;
        double w = this.type.getCableWidth();
        this.xzlen = Math.sqrt(this.xDelta() * this.xDelta() + this.zDelta() * this.zDelta());
        this.fullLen = Math.sqrt(this.xzlen * this.xzlen + this.yDelta() * this.yDelta());

        this.center = new Vec3(
            (this.in[START_X] + this.in[END_X]) / 2,
            (this.in[START_Y] + this.in[END_Y]) / 2,
            (this.in[START_Z] + this.in[END_Z]) / 2);
        this.rayBox = new RotatedRayBox.Builder(new AABB(0, -w, -w, -this.fullLen, w, w))
            .origin(this.in[START_X], this.in[START_Y], this.in[START_Z])
            .rotate(this.verticalRotation(), 0, 0, 1)
            .rotate(this.horizontalRotation(), 0, 1, 0)
            .build();

        this.prevCache = this.genCache(false);
        this.nextCache = this.genCache(true);

        this.collisionShape = this.createCollisionShape();
    }

    public Connection setBroken(boolean broken) {
        if (!this.setBrokenSilently(broken)) {
            return this;
        }
        this.reRenderCallback.run();
        return this;
    }

    private Connection silentlySetBroken(boolean broken) {
        this.setBrokenSilently(broken);
        return this;
    }

    public boolean setBrokenSilently(boolean broken) {
        if (this.broken == broken) {
            return false;
        }
        this.broken = broken;
        this.invalidateRenderData();
        return true;
    }

    private Connection silentlySetSign(boolean sign) {
        this.sign = sign;
        this.invalidateRenderData();
        return this;
    }

    private SurroundingCache genCache(boolean next) {
        double w = this.type.getCableWidth();
        RotatedRayBox fixedBox = new RotatedRayBox.Builder(new AABB(0, -w, -w, -this.fullLen / 2, w, w))
            .origin(next ? this.center.x : this.in[START_X], next ? this.center.y : this.in[START_Y], next ? this.center.z : this.in[START_Z])
            .rotate(this.verticalRotation(), 0, 0, 1)
            .rotate(this.horizontalRotation(), 0, 1, 0)
            .build();
        Vec3 point = this.clipBrokenPointToBlock(this.buildBrokenPoint(next));
        RotatedRayBox rotatedBox = this.genRotatedBox(point, next);
        return new SurroundingCache(new Vector3f((float)point.x, (float)point.y, (float)point.z), fixedBox, rotatedBox);
    }

    public Connection copy() {
        return new Connection(this.reRenderCallback, this.type, this.offset, this.from, this.to, this.previous, this.next, this.position)
            .silentlySetBroken(this.broken)
            .silentlySetSign(this.sign);
    }

    private RotatedRayBox genRotatedBox(Vec3 point, boolean next) {
        double w = this.type.getCableWidth();
        Vec3 rotationVector = next ? point : point.scale(-1D);
        double length = point.length() * (next ? 1D : -1D);
        return new RotatedRayBox.Builder(new AABB(0, -w, -w, length, w, w))
            .origin(this.center.x, this.center.y, this.center.z)
            .rotate(horizontalRotation(rotationVector), 0, 1, 0)
            .rotate(verticalRotation(rotationVector), 0, 0, 1)
            .build();
    }

    private Vec3 buildBrokenPoint(boolean next) {
        Vec3 outward = this.halfWireVector(next);
        double outwardLength = outward.length();
        if (outwardLength <= 1.0E-5D) {
            return outward;
        }

        Vec3 axis = outward.scale(1D / outwardLength);
        Vec3 away = this.awayFromOtherWires(axis);
        Vec3 side = normalize(cross(axis, away));

        double maximumLateral = outwardLength * Math.sin(BROKEN_WIRE_MAX_BEND_ANGLE);
        double awayAmount = maximumLateral * (BROKEN_WIRE_MIN_BEND + this.random.nextFloat() * (1D - BROKEN_WIRE_MIN_BEND));
        double sideAmount = maximumLateral * (this.random.nextFloat() * 2D - 1D) * BROKEN_WIRE_SIDE_BEND;
        Vec3 lateral = away.scale(awayAmount).add(side.scale(sideAmount));

        double lateralLength = lateral.length();
        if (lateralLength > maximumLateral && lateralLength > 1.0E-5D) {
            lateral = lateral.scale(maximumLateral / lateralLength);
            lateralLength = maximumLateral;
        }
        double axial = Math.sqrt(Math.max(0D, outwardLength * outwardLength - lateralLength * lateralLength));
        return axis.scale(axial).add(lateral);
    }

    private Vec3 awayFromOtherWires(Vec3 axis) {
        double verticalDirection = this.verticalBendDirection();
        if (verticalDirection == 0D) {
            return this.randomPerpendicular(axis);
        }

        Vec3 away = new Vec3(0D, verticalDirection, 0D);
        away = away.subtract(axis.scale(away.dot(axis)));
        if (away.length() <= 1.0E-5D) {
            return this.randomPerpendicular(axis);
        }
        return normalize(away);
    }

    private Vec3 randomPerpendicular(Vec3 axis) {
        Vec3 first = this.perpendicularTo(axis);
        Vec3 second = normalize(cross(axis, first));
        double angle = this.random.nextDouble() * Math.PI * 2D;
        return first.scale(Math.cos(angle)).add(second.scale(Math.sin(angle)));
    }

    private Vec3 perpendicularTo(Vec3 axis) {
        Vec3 fallback = Math.abs(axis.y) < 0.9D ? new Vec3(0D, 1D, 0D) : new Vec3(1D, 0D, 0D);
        fallback = fallback.subtract(axis.scale(fallback.dot(axis)));
        if (fallback.length() <= 1.0E-5D) {
            return new Vec3(0D, 0D, 1D);
        }
        return normalize(fallback);
    }

    private Vec3 clipBrokenPointToBlock(Vec3 point) {
        AABB aabb = new AABB(this.position);
        Vec3 centerVec = new Vec3(this.center.x, this.center.y, this.center.z);
        Vec3 end = point.add(centerVec);
        if (aabb.contains(end)) {
            return point;
        }
        Optional<Vec3> clip = aabb.clip(centerVec, end);
        return clip.map(vec3 -> vec3.subtract(centerVec)).orElse(point);
    }

    private double verticalBendDirection() {
        double[] offsets = this.type.getOffsets();
        if (offsets.length <= 1) {
            return 0D;
        }

        double centerOffset = 0D;
        for (double typeOffset : offsets) {
            centerOffset += typeOffset;
        }
        centerOffset /= offsets.length;

        if (Math.abs(this.offset - centerOffset) <= 1.0E-5D) {
            return 0D;
        }
        if (this.offset > centerOffset) {
            return 1D;
        }
        return -1D;
    }

    private Vec3 halfWireVector(boolean next) {
        return new Vec3(this.xDelta(), this.yDelta(), this.zDelta()).scale(next ? 0.5D : -0.5D);
    }

    private static Vec3 normalize(Vec3 vector) {
        double length = vector.length();
        if (length <= 1.0E-5D) {
            return Vec3.ZERO;
        }
        return vector.scale(1D / length);
    }

    private static Vec3 cross(Vec3 first, Vec3 second) {
        return new Vec3(
            first.y * second.z - first.z * second.y,
            first.z * second.x - first.x * second.z,
            first.x * second.y - first.y * second.x);
    }

    private VoxelShape createCollisionShape() {
        VoxelShape shape = Shapes.empty();
        for (BlockConnectableBase.ConnectionAxisAlignedBB bb : BlockConnectableBase.createBoundingBox(Collections.singleton(this), this.position)) {
            shape = Shapes.or(shape, Shapes.create(bb));
        }
        return shape;
    }

    private double horizontalRotation() {
        return this.xDelta() == 0D ? Math.PI * 1.5D : Math.atan(this.zDelta() / this.xDelta());
    }

    private static double horizontalRotation(Vec3 vector) {
        return vector.x == 0D ? Math.PI * 1.5D : Math.atan(vector.z / vector.x);
    }

    private double verticalRotation() {
        return this.xzlen == 0D ? 0D : Math.atan(this.yDelta() / this.xzlen);
    }

    private static double verticalRotation(Vec3 vector) {
        double horizontalLength = Math.sqrt(vector.x * vector.x + vector.z * vector.z);
        return horizontalLength == 0D ? 0D : Math.atan(vector.y / horizontalLength);
    }

    private double xDelta() {
        return this.in[END_X] - this.in[START_X];
    }

    private double yDelta() {
        return this.in[END_Y] - this.in[START_Y];
    }

    private double zDelta() {
        return this.in[END_Z] - this.in[START_Z];
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putString(ID_TAG, this.type.getRegistryName().toString());
        nbt.putDouble(OFFSET_TAG, this.offset);
        nbt.put(FROM_TAG, NbtUtils.writeBlockPos(this.getFrom()));
        nbt.put(TO_TAG, NbtUtils.writeBlockPos(this.getTo()));
        nbt.putBoolean(SIGN_TAG, this.sign);
        nbt.put(NEXT_TAG, NbtUtils.writeBlockPos(this.next));
        nbt.put(PREVIOUS_TAG, NbtUtils.writeBlockPos(this.previous));
        nbt.putBoolean(BROKEN_TAG, this.broken);
        return nbt;
    }

    public static Connection fromNBT(CompoundTag nbt, BlockEntity tileEntity) {
        return new Connection(
            tileEntity,
            ConnectionType.getType(new ResourceLocation(nbt.getString(ID_TAG))),
            nbt.getDouble(OFFSET_TAG),
            NbtUtils.readBlockPos(nbt.getCompound(FROM_TAG)),
            NbtUtils.readBlockPos(nbt.getCompound(TO_TAG)),
            NbtUtils.readBlockPos(nbt.getCompound(PREVIOUS_TAG)),
            NbtUtils.readBlockPos(nbt.getCompound(NEXT_TAG)),
            tileEntity.getBlockPos()
        ).silentlySetBroken(nbt.getBoolean(BROKEN_TAG)).silentlySetSign(nbt.getBoolean(SIGN_TAG));
    }

    public boolean lazyEquals(Connection con) {
        return this.getFrom().equals(con.getFrom())
            && this.getTo().equals(con.getTo())
            && Double.compare(this.offset, con.offset) == 0;
    }

    public BlockPos getMin() {
        return this.compared < 0 ? this.to : this.from;
    }

    public BlockPos getMax() {
        return this.compared >= 0 ? this.to : this.from;
    }

    public boolean brokenSide(BlockGetter world, boolean next) {
        BlockPos adjacentPos = next == this.compared < 0 ? this.previous : this.next;
        BlockEntity te = world.getBlockEntity(adjacentPos);
        if (te instanceof ConnectableBlockEntity fe) {
            for (Connection fenceConnection : fe.getConnections()) {
                if (this.lazyEquals(fenceConnection) && fenceConnection.isBroken()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public boolean isPowered(BlockGetter world) {
        return FencePowerService.isPowered(world, this);
    }


    public CompiledRenderData compileRenderData(BlockGetter world) {
        boolean previousBroken = this.brokenSide(world, false);
        boolean nextBroken = this.brokenSide(world, true);
        int state = Objects.hash(this.broken, this.sign, previousBroken, nextBroken);
        if (this.compiledRenderData != null && this.compiledRenderDataState == state) {
            return this.compiledRenderData;
        }

        this.compiledRenderDataState = state;
        this.compiledRenderData = new CompiledRenderData(
            this.isSign(),
            this.collectRenderData(previousBroken, nextBroken));
        return this.compiledRenderData;
    }

    private List<float[]> collectRenderData(boolean previousBroken, boolean nextBroken) {
        if (this.isBroken() || previousBroken && nextBroken) {
            return List.of();
        }

        List<float[]> out = new ArrayList<>();
        RenderData data = this.getRenderData();
        if (nextBroken) {
            out.add(data.nextRotated());
            if (!previousBroken) {
                out.add(data.nextFixed());
            }
        }
        if (previousBroken) {
            out.add(data.prevRotated());
            if (!nextBroken) {
                out.add(data.prevFixed());
            }
        }
        if (!previousBroken && !nextBroken) {
            out.add(data.data());
        }
        return List.copyOf(out);
    }


    private RenderData buildRenderData() {
        RenderGeometry geometry = this.createRenderGeometry();
        SegmentRenderData previous = this.buildSegmentRenderData(geometry, SegmentEnd.PREVIOUS);
        SegmentRenderData next = this.buildSegmentRenderData(geometry, SegmentEnd.NEXT);
        return new RenderData(
            this.buildMainRenderData(geometry),
            previous.fixed(),
            next.fixed(),
            previous.rotated(),
            next.rotated());
    }

    private RenderGeometry createRenderGeometry() {
        double halfThickness = this.type.getCableWidth() / 2F;
        double postDistance = this.distance(this.from, this.to.getX() + 0.5F, this.to.getZ() + 0.5F);
        double yRange = postDistance == 0 ? 1 : (this.to.getY() - this.from.getY()) / postDistance;
        double horizontalAngle = this.in[END_X] == this.in[START_X]
            ? Math.PI / 2D
            : Math.atan((this.in[START_Z] - this.in[END_Z]) / (this.in[END_X] - this.in[START_X]));
        double xThickness = halfThickness * Math.sin(horizontalAngle);
        double zThickness = halfThickness * Math.cos(horizontalAngle);
        double verticalAngle = postDistance == 0 ? Math.PI / 2D : Math.atan((this.to.getY() - this.from.getY()) / postDistance);
        double verticalSkew = Math.sin(verticalAngle);

        PlanarQuad top = new PlanarQuad(
            this.in[START_X] - xThickness + verticalSkew * zThickness, this.in[START_Z] - zThickness - verticalSkew * xThickness,
            this.in[END_X] - xThickness + verticalSkew * zThickness, this.in[END_Z] - zThickness - verticalSkew * xThickness,
            this.in[END_X] + xThickness + verticalSkew * zThickness, this.in[END_Z] + zThickness - verticalSkew * xThickness,
            this.in[START_X] + xThickness + verticalSkew * zThickness, this.in[START_Z] + zThickness - verticalSkew * xThickness);
        PlanarQuad bottom = new PlanarQuad(
            this.in[START_X] - xThickness - verticalSkew * zThickness, this.in[START_Z] - zThickness + verticalSkew * xThickness,
            this.in[END_X] - xThickness - verticalSkew * zThickness, this.in[END_Z] - zThickness + verticalSkew * xThickness,
            this.in[END_X] + xThickness - verticalSkew * zThickness, this.in[END_Z] + zThickness + verticalSkew * xThickness,
            this.in[START_X] + xThickness - verticalSkew * zThickness, this.in[START_Z] + zThickness + verticalSkew * xThickness);

        double startY = yRange * this.distance(this.from, this.in[START_X], this.in[START_Z]) - this.position.getY() + this.from.getY();
        double endY = yRange * this.distance(this.from, this.in[END_X], this.in[END_Z]) - this.position.getY() + this.from.getY();
        double worldWidth = this.type.getCableWidth() * 32F;
        double uvLength = Math.sqrt(square(this.in[START_X] - this.in[END_X])
            + square(this.in[START_Z] - this.in[END_Z])
            + square(this.in[START_Y] - this.in[END_Y])) / worldWidth;

        return new RenderGeometry(
            top,
            bottom,
            startY,
            endY,
            halfThickness * Math.cos(verticalAngle),
            uvLength,
            worldWidth,
            -this.position.getX(),
            this.offset,
            -this.position.getZ());
    }

    private double distance(BlockPos from, double x, double z) {
        return Math.sqrt((from.getX() + 0.5F - x) * (from.getX() + 0.5F - x) + (from.getZ() + 0.5F - z) * (from.getZ() + 0.5F - z));
    }

    private SegmentRenderData buildSegmentRenderData(RenderGeometry geometry, SegmentEnd end) {
        PlanarQuad rotatedTop = getHalf(geometry.top(), end);
        PlanarQuad rotatedBottom = getHalf(geometry.bottom(), end);
        SegmentEnd fixedEnd = end.opposite();
        PlanarQuad fixedTop = getHalf(geometry.top(), fixedEnd);
        PlanarQuad fixedBottom = getHalf(geometry.bottom(), fixedEnd);
        double centerY = geometry.centerY();
        int textureSize = textureMaxSize(geometry.uvLength(), geometry.worldWidth());

        float[] rotated = this.buildRotatedSegmentRenderData(
            geometry,
            rotatedTop,
            rotatedBottom,
            end,
            this.randomUvs(textureSize));
        float[] fixed = packStraightRenderData(
            geometry,
            fixedTop,
            fixedBottom,
            fixedEnd == SegmentEnd.PREVIOUS ? geometry.startY() : centerY,
            fixedEnd == SegmentEnd.PREVIOUS ? centerY : geometry.endY(),
            this.randomUvs(textureSize),
            geometry.uvLength() / 4D);
        return new SegmentRenderData(fixed, rotated);
    }

    private static PlanarQuad getHalf(PlanarQuad quad, SegmentEnd end) {
        return end == SegmentEnd.NEXT ? quad.nextHalf() : quad.previousHalf();
    }

    private float[] buildMainRenderData(RenderGeometry geometry) {
        return packStraightRenderData(
            geometry,
            geometry.top(),
            geometry.bottom(),
            geometry.startY(),
            geometry.endY(),
            this.randomUvs(textureMaxSize(geometry.uvLength(), geometry.worldWidth())),
            geometry.uvLength() / 2D);
    }

    private float[] buildRotatedSegmentRenderData(RenderGeometry geometry, PlanarQuad top, PlanarQuad bottom, SegmentEnd end, double[] uvs) {
        Vector3f point = end == SegmentEnd.NEXT ? this.nextCache.point : this.prevCache.point;
        double centerY = geometry.centerY();
        return end == SegmentEnd.NEXT
            ? packNextRotatedRenderData(geometry, top, bottom, point, centerY, uvs)
            : packPreviousRotatedRenderData(geometry, top, bottom, point, centerY, uvs);
    }

    private static float[] packStraightRenderData(
        RenderGeometry geometry,
        PlanarQuad top,
        PlanarQuad bottom,
        double startY,
        double endY,
        double[] uvs,
        double textureLength
    ) {
        double x = geometry.xOffset();
        double y = geometry.yOffset();
        double z = geometry.zOffset();
        double yThickness = geometry.yThickness();
        return packRenderData(new double[] {
            top.startLeftX() + x, startY + yThickness + y, top.startLeftZ() + z,
            top.endLeftX() + x, endY + yThickness + y, top.endLeftZ() + z,
            top.startRightX() + x, startY + yThickness + y, top.startRightZ() + z,
            top.endRightX() + x, endY + yThickness + y, top.endRightZ() + z,
            bottom.startLeftX() + x, startY - yThickness + y, bottom.startLeftZ() + z,
            bottom.endLeftX() + x, endY - yThickness + y, bottom.endLeftZ() + z,
            bottom.startRightX() + x, startY - yThickness + y, bottom.startRightZ() + z,
            bottom.endRightX() + x, endY - yThickness + y, bottom.endRightZ() + z
        }, uvs, textureLength, TEXTURE_PIXEL, TEXTURE_PIXEL);
    }

    private static float[] packNextRotatedRenderData(
        RenderGeometry geometry,
        PlanarQuad top,
        PlanarQuad bottom,
        Vector3f point,
        double centerY,
        double[] uvs
    ) {
        double x = geometry.xOffset();
        double y = geometry.yOffset();
        double z = geometry.zOffset();
        double yThickness = geometry.yThickness();
        return packRenderData(new double[] {
            top.startLeftX() + x + point.x(), centerY + yThickness + y + point.y(), top.startLeftZ() + z + point.z(),
            top.startLeftX() + x, centerY + yThickness + y, top.startLeftZ() + z,
            top.startRightX() + x + point.x(), centerY + yThickness + y + point.y(), top.startRightZ() + z + point.z(),
            top.startRightX() + x, centerY + yThickness + y, top.startRightZ() + z,
            bottom.startLeftX() + x + point.x(), centerY - yThickness + y + point.y(), bottom.startLeftZ() + z + point.z(),
            bottom.startLeftX() + x, centerY - yThickness + y, bottom.startLeftZ() + z,
            bottom.startRightX() + x + point.x(), centerY - yThickness + y + point.y(), bottom.startRightZ() + z + point.z(),
            bottom.startRightX() + x, centerY - yThickness + y, bottom.startRightZ() + z
        }, uvs, geometry.uvLength() / 4D, TEXTURE_PIXEL, TEXTURE_PIXEL);
    }

    private static float[] packPreviousRotatedRenderData(
        RenderGeometry geometry,
        PlanarQuad top,
        PlanarQuad bottom,
        Vector3f point,
        double centerY,
        double[] uvs
    ) {
        double x = geometry.xOffset();
        double y = geometry.yOffset();
        double z = geometry.zOffset();
        double yThickness = geometry.yThickness();
        return packRenderData(new double[] {
            top.endLeftX() + x, centerY + yThickness + y, top.endLeftZ() + z,
            top.endLeftX() + x + point.x(), centerY + yThickness + y + point.y(), top.endLeftZ() + z + point.z(),
            top.endRightX() + x, centerY + yThickness + y, top.endRightZ() + z,
            top.endRightX() + x + point.x(), centerY + yThickness + y + point.y(), top.endRightZ() + z + point.z(),
            bottom.endLeftX() + x, centerY - yThickness + y, bottom.endLeftZ() + z,
            bottom.endLeftX() + x + point.x(), centerY - yThickness + y + point.y(), bottom.endLeftZ() + z + point.z(),
            bottom.endRightX() + x, centerY - yThickness + y, bottom.endRightZ() + z,
            bottom.endRightX() + x + point.x(), centerY - yThickness + y + point.y(), bottom.endRightZ() + z + point.z()
        }, uvs, geometry.uvLength() / 4D, TEXTURE_PIXEL, TEXTURE_PIXEL);
    }

    private static float[] packRenderData(double[] vertices, double[] uvs, double textureLength, double textureHeight, double textureDepth) {
        float[] data = new float[RENDER_DATA_LENGTH];
        for (int i = 0; i < VERTEX_DATA_LENGTH; i++) {
            data[i] = (float) vertices[i];
        }
        for (int i = 0; i < UV_COUNT; i++) {
            data[VERTEX_DATA_LENGTH + i] = (float) uvs[i];
        }
        data[VERTEX_DATA_LENGTH + UV_COUNT] = (float) textureLength;
        data[VERTEX_DATA_LENGTH + UV_COUNT + 1] = (float) textureHeight;
        data[VERTEX_DATA_LENGTH + UV_COUNT + 2] = (float) textureDepth;
        return data;
    }

    private double[] randomUvs(int maximumTextureSize) {
        double[] uvs = new double[UV_COUNT];
        int randomBound = 65 - maximumTextureSize;
        for (int i = 0; i < uvs.length; i++) {
            uvs[i] = this.random.nextInt(randomBound) / 64F;
        }
        return uvs;
    }

    private static int textureMaxSize(double uvLength, double worldWidth) {
        return (int) Math.min(64, Math.ceil(Math.max(uvLength * 16D, worldWidth) * 2D));
    }

    private static double square(double value) {
        return value * value;
    }

    public Runnable getReRenderCallback() {
        return reRenderCallback;
    }

    public ConnectionType getType() {
        return type;
    }

    public double getOffset() {
        return offset;
    }

    public int getToFromHash() {
        return toFromHash;
    }

    public BlockPos getFrom() {
        return from;
    }

    public BlockPos getTo() {
        return to;
    }

    public BlockPos getNext() {
        return next;
    }

    public BlockPos getPrevious() {
        return previous;
    }

    public boolean isSign() {
        return sign;
    }

    public Connection setSign(boolean sign) {
        if (this.sign == sign) {
            return this;
        }
        this.sign = sign;
        this.invalidateRenderData();
        this.reRenderCallback.run();
        return this;
    }

    public BlockPos getPosition() {
        return position;
    }

    public int getCompared() {
        return compared;
    }

    public RenderData getRenderData() {
        if (renderData == null) {
            renderData = this.buildRenderData();
        }
        return renderData;
    }

    public void setRenderData(RenderData renderData) {
        this.renderData = renderData;
        this.compiledRenderData = null;
        this.compiledRenderDataState = Integer.MIN_VALUE;
    }

    public boolean isBroken() {
        return broken;
    }

    public double[] getIn() {
        return in;
    }

    public boolean isValid() {
        return valid;
    }

    public double getXzlen() {
        return xzlen;
    }

    public double getFullLen() {
        return fullLen;
    }

    public Random getRandom() {
        return random;
    }

    public Vec3 getCenter() {
        return center;
    }

    public RotatedRayBox getRayBox() {
        return rayBox;
    }

    public SurroundingCache getPrevCache() {
        return prevCache;
    }

    public SurroundingCache getNextCache() {
        return nextCache;
    }

    public VoxelShape getCollisionShape() {
        return collisionShape;
    }

    public void invalidateRenderData() {
        this.compiledRenderData = null;
        this.compiledRenderDataState = Integer.MIN_VALUE;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Connection connection)) {
            return false;
        }
        return Double.compare(connection.offset, this.offset) == 0
            && this.position.equals(connection.position)
            && this.from.equals(connection.from)
            && this.to.equals(connection.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.position, this.from, this.to, this.offset);
    }

    private enum SegmentEnd {
        PREVIOUS,
        NEXT;

        private SegmentEnd opposite() {
            return this == NEXT ? PREVIOUS : NEXT;
        }
    }

    private record PlanarQuad(
        double startLeftX,
        double startLeftZ,
        double endLeftX,
        double endLeftZ,
        double endRightX,
        double endRightZ,
        double startRightX,
        double startRightZ
    ) {
        private PlanarQuad section(double startT, double endT) {
            return new PlanarQuad(
                lerp(this.startLeftX, this.endLeftX, startT),
                lerp(this.startLeftZ, this.endLeftZ, startT),
                lerp(this.startLeftX, this.endLeftX, endT),
                lerp(this.startLeftZ, this.endLeftZ, endT),
                lerp(this.startRightX, this.endRightX, endT),
                lerp(this.startRightZ, this.endRightZ, endT),
                lerp(this.startRightX, this.endRightX, startT),
                lerp(this.startRightZ, this.endRightZ, startT));
        }

        private static double lerp(double start, double end, double t) {
            return start + (end - start) * t;
        }

        private PlanarQuad previousHalf() {
            return this.section(0D, 0.5D);
        }

        private PlanarQuad nextHalf() {
            return this.section(0.5D, 1D);
        }
    }

    private record RenderGeometry(
        PlanarQuad top,
        PlanarQuad bottom,
        double startY,
        double endY,
        double yThickness,
        double uvLength,
        double worldWidth,
        double xOffset,
        double yOffset,
        double zOffset
    ) {
        private double centerY() {
            return this.endY + (this.startY - this.endY) / 2D;
        }
    }

    private record SegmentRenderData(float[] fixed, float[] rotated) {
    }

    public record SurroundingCache(Vector3f point, RotatedRayBox fixedBox, RotatedRayBox rotatedBox) {
    }

    //Each array is length 39, and should be passed to RenderUtils.drawSpacedCube

    public record RenderData(float[] data, float[] prevFixed, float[] nextFixed, float[] prevRotated, float[] nextRotated) {
    }


    public record CompiledRenderData(boolean renderSign, List<float[]> connectionData) {
    }
}
