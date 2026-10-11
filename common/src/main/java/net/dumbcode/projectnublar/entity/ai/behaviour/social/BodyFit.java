package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.GeneInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The only herd class that reads blocks (spec "Fitting"; the 26.2 port changes this one place). Every test
 * uses a member's {@link ClearanceBox}:
 * <ul>
 *   <li><b>Fit</b>: the vanilla collision query ({@code noCollision}) on the whole box at feet height, or
 *       raised by {@link #STEP_HEIGHT}. Real collision shapes: leaves, slabs, carpets, glass and barrier
 *       blocks count as they are; nothing is special-cased.</li>
 *   <li><b>Ground</b>: a collision shape within {@link #MAX_DROP} blocks under the column that holds the box
 *       centre (fix P12c, {@link GroundSupport#standTop}: under at least half the columns when the centre lies on
 *       a column boundary; before P12c any footprint column was enough, so a body 0.6 past a rim "stood"), with
 *       no hazard (fluid, fire, cactus, fences, walls, electric fences ...) under any column on the way down
 *       and none in the feet layer. The stand height is the highest ground top under the box.</li>
 * </ul>
 * Results are cached per herd, keyed by {@link ClearanceBox#probeKey}; probe positions are rounded to 0.25
 * first so a cached answer is exact. Stand results live {@link #STAND_CACHE_TICKS}: the ground does not
 * change faster than that, and followers in a column probe the same cells a few ticks apart, so they share
 * them (a per-tick cache would miss on every probe). The guard's fit results live one tick. One instance
 * per herd. Server thread only.
 * <p>
 * Public for one caller outside the package, {@code FenceAwareNodeEvaluator}: the path node test
 * ({@link #pathProbe}, {@link PathProbe}), so a vanilla path never leads a herd member where the guard stops it,
 * and the rim rules of every dinosaur's paths ({@link #nodeSupport}, {@link NodeSupport}).
 */
public final class BodyFit {

    /** Largest drop a herd member steps down, the vanilla safe fall for mobs. */
    static final int MAX_DROP = 3;
    /** The dinosaurs' step height ({@code Dinosaur} sets {@code setMaxUpStep(1.0F)}). */
    static final double STEP_HEIGHT = 1.0D;
    static final double SIDE_ROOM_STEP = 0.5D;
    /** Side room is measured up to this far: 3 lanes 4.2 apart in open ground (Gallimimus) need 4.2 on each side, a wider species more (plan review decision 9). */
    static final double MAX_SIDE_ROOM = 5.5D;
    /** A straight-line reach is sampled every this many blocks (spec: every block). */
    static final double REACH_STEP = 1.0D;
    /** Stand results are kept this many ticks; a block placed or broken is seen within a second. */
    static final int STAND_CACHE_TICKS = 20;
    /**
     * Rows a path node's rim rule reads per column (fix P12c, {@link NodeSupport}): the node cell and the two
     * rows under it. Ground one step ({@link #STEP_HEIGHT}) below the node floor is the top of a full block in
     * the second row down ({@link GroundSupport#columnGrounds}); nothing lower can ground the node.
     */
    static final int NODE_GROUND_ROWS = 3;
    private static final double EPSILON = 1.0E-7D;
    /** {@link #standCache} value for a key not cached (stand results are a height or NaN, never this). */
    private static final double STAND_ABSENT = Double.NEGATIVE_INFINITY;
    /** {@link #fitCache} values: one lookup per probe, with 0 for a key not cached. */
    private static final byte FIT_ABSENT = 0;
    private static final byte FIT_NO = 1;
    private static final byte FIT_YES = 2;

    private final Long2DoubleOpenHashMap standCache = new Long2DoubleOpenHashMap();
    private final Long2ByteOpenHashMap fitCache = new Long2ByteOpenHashMap();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private long fitTick = Long.MIN_VALUE;
    private long standTick = -STAND_CACHE_TICKS;
    /** One column scan of {@link #groundBelow} ({@link #scanColumn}), made once so a probe allocates no lambda. */
    private final GroundSupport.ColumnTop columnScan = this::scanColumn;
    /** The level, collision context and top row {@link #scanColumn} reads; set by {@link #groundBelow} for its scan only. */
    private @Nullable ServerLevel scanLevel;
    private @Nullable CollisionContext scanContext;
    private int scanStart;

    BodyFit() {
        this.standCache.defaultReturnValue(STAND_ABSENT);
        this.fitCache.defaultReturnValue(FIT_ABSENT);
    }

    /** The clearance box of a herd member, from its species, hitbox, growth stage and size gene. */
    static ClearanceBox boxOf(Dinosaur dinosaur) {
        return ClearanceBox.of(BuiltInRegistries.ENTITY_TYPE.getKey(dinosaur.getType()).getPath(),
                dinosaur.getBbWidth(), dinosaur.getBbHeight(), dinosaur.getGrowthStage(),
                dinosaur.getDinoData().getGeneValue(GeneInit.SIZE.get()));
    }

    /**
     * The feet height at which the box stands with its bottom centre at (x, z), starting from {@code feetY}
     * (stepping up at most {@link #STEP_HEIGHT}, down at most {@link #MAX_DROP}), or NaN if it cannot stand
     * there (no fit, no ground, or a hazard). Cached for {@link #STAND_CACHE_TICKS}.
     */
    double standY(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z) {
        refresh(level);
        double qx = ClearanceBox.quantise(x);
        double qy = ClearanceBox.quantise(feetY);
        double qz = ClearanceBox.quantise(z);
        long key = box.probeKey(qx, qy, qz);
        double cached = this.standCache.get(key);
        if (cached != STAND_ABSENT) {
            return cached;
        }
        double result = standUncached(level, walker, box, qx, qy, qz);
        this.standCache.put(key, result);
        return result;
    }

    boolean canStand(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z) {
        return !Double.isNaN(standY(level, walker, box, x, feetY, z));
    }

    /**
     * The feet height at which the box stands on the surface at (x, z), or NaN (journey targets, playtest fix
     * P9). Loads no chunk: NaN unless the column's chunk is loaded and entity-ticking, which is checked before
     * any block read. An entity-ticking chunk has its neighbours loaded, so the box's footprint reads only
     * loaded chunks. The start height is the chunk's heightmap ({@code MOTION_BLOCKING_NO_LEAVES}: just above
     * the ground, or the water, under any leaves); {@link #standY} then rejects water, hazards and places where
     * the box does not fit (under low leaves, on a trunk among leaves).
     */
    double surfaceStandY(ServerLevel level, Entity walker, ClearanceBox box, double x, double z) {
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        if (!level.isPositionEntityTicking(this.pos.set(blockX, walker.getBlockY(), blockZ))) {
            return Double.NaN;
        }
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);
        return standY(level, walker, box, x, surface, z);
    }

    /**
     * The path node test of one herd member for one path computation (playtest 1 fix P6). Made by
     * {@link #pathProbe} in {@code FenceAwareNodeEvaluator.prepare} and dropped in its {@code done()}, so
     * the group, box and cache are looked up once per path, not per node. Vanilla's own node check accepts
     * gaps between the hitbox height and the box height (a top-half trapdoor is "TRAPDOOR", malus 0, with
     * no collision test; a carpet under a 3-high roof is "OPEN"), and the guard then stops the body there.
     */
    public static final class PathProbe {

        private final BodyFit fit;
        private final ServerLevel level;
        private final Dinosaur dinosaur;
        private final ClearanceBox box;
        /** The body does not move while its path is computed, so one context serves every node. */
        private final CollisionContext context;
        private final double width;

        private PathProbe(BodyFit fit, ServerLevel level, Dinosaur dinosaur, ClearanceBox box) {
            this.fit = fit;
            this.level = level;
            this.dinosaur = dinosaur;
            this.box = box;
            this.context = CollisionContext.of(dinosaur);
            this.width = dinosaur.getBbWidth();
        }

        /**
         * Does the box fit where the body stands at the node (nodeX, nodeY, nodeZ)? The guard's test
         * ({@link #fits}: same box, same herd cache, same 0.25 rounding) at the position vanilla walks the
         * body to ({@link ClearanceBox#nodeCentre}), with the feet where they rest there
         * ({@link #feetAt}: on a slab under the node, on a carpet in it). Blocks are read through
         * {@code region}, the path's own chunk view, which never loads a chunk; an unloaded chunk reads as
         * air there, as it does for vanilla's typing, so the answer is "fits" and vanilla's type stands.
         * Cells without a collision shape settle it without the collision query ({@link #boxCellsEmpty}).
         */
        public boolean fitsAt(BlockGetter region, int nodeX, int nodeY, int nodeZ) {
            double x = ClearanceBox.nodeCentre(nodeX, this.width);
            double z = ClearanceBox.nodeCentre(nodeZ, this.width);
            double feetY = ClearanceBox.quantise(this.fit.feetAt(region, this.context, this.width, x, nodeY, z));
            return this.fit.boxCellsEmpty(region, this.context, this.box, x, feetY, z)
                    || this.fit.fits(this.level, this.dinosaur, this.box, x, feetY, z);
        }
    }

    /**
     * The path node test for this animal's next path computation, or {@code null} when the guard does not
     * run for it ({@link HerdSpacing#applies}: no group, or fighting), so vanilla pathing stays as it was: a
     * solo animal's box can be far taller than its hitbox (the model top sets it), and nothing stops its
     * body at a gap. Server thread only; paths are computed synchronously in the mob's tick.
     */
    public static @Nullable PathProbe pathProbe(Dinosaur dinosaur) {
        if (!(dinosaur.level() instanceof ServerLevel level) || !HerdSpacing.applies(dinosaur)) {
            return null;
        }
        DinoGroup group = GroupManager.groupOf(dinosaur);
        if (group == null) {
            return null;
        }
        ClearanceBox box = dinosaur.getMoveControl() instanceof HerdMoveControl control ? control.box() : boxOf(dinosaur);
        return new PathProbe(group.fit(), level, dinosaur, box);
    }

    /**
     * The rim rules of one dinosaur's path computation (fix P12c, cliff causes 1 and 5; every dinosaur, herd or
     * not: the lead saw "entities in general" fall). Vanilla types a node for a body wider than one block by its
     * whole footprint and takes {walkable, open} as walkable, so a Gallimimus node with ground under 1 of its 4
     * columns was a walkable node, and A*, with no cost on a rim, ran routes along rims and round pit corners.
     * {@link #typeAt} keeps vanilla's type on a {@link GroundSupport.Edge#FULL} node, makes a
     * {@link GroundSupport.Edge#RIM} node {@code DANGER_OTHER} (malus 8: the route moves one block in where it
     * can, and vanilla does not cut corners on it) and a {@link GroundSupport.Edge#HANG} node
     * {@code BLOCKED}. Made once per path computation ({@code FenceAwareNodeEvaluator.prepare}); reads blocks
     * only through the path's region (no chunk loads), at most {@link #NODE_GROUND_ROWS} per footprint column,
     * and vanilla caches the type per node for the path.
     */
    public static final class NodeSupport implements GroundSupport.ColumnGround {

        private final Mob mob;
        /** The body does not move while its path is computed, so one context serves every node. */
        private final CollisionContext context;
        private final int footprint;
        private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        /** The region and node row {@link #grounded} reads; set by {@link #typeAt} for its count only. */
        private @Nullable BlockGetter region;
        private int nodeY;

        private NodeSupport(Mob mob, int footprint) {
            this.mob = mob;
            this.context = CollisionContext.of(mob);
            this.footprint = footprint;
        }

        /**
         * The type of the node (x, y, z) given vanilla's {@code vanilla} type: {@code BLOCKED} on a hang,
         * {@code DANGER_OTHER} on a rim (only when that costs more than vanilla's type), vanilla's otherwise. An
         * {@code OPEN} node (no ground under any column at the node's own floor) keeps its type: vanilla tests
         * the fall from it itself, and planned drops of up to 3 blocks stay allowed (lead decision).
         */
        public BlockPathTypes typeAt(BlockGetter region, int x, int y, int z, BlockPathTypes vanilla) {
            if (vanilla == BlockPathTypes.OPEN) {
                return vanilla;
            }
            this.region = region;
            this.nodeY = y;
            GroundSupport.Edge edge = GroundSupport.nodeEdge(x, z, this.footprint, this);
            this.region = null;
            if (edge == GroundSupport.Edge.HANG) {
                return BlockPathTypes.BLOCKED;
            }
            if (edge == GroundSupport.Edge.RIM
                    && this.mob.getPathfindingMalus(BlockPathTypes.DANGER_OTHER) > this.mob.getPathfindingMalus(vanilla)) {
                return BlockPathTypes.DANGER_OTHER;
            }
            return vanilla;
        }

        /**
         * Whether the column (x, z) grounds the node ({@link GroundSupport#columnGrounds}): the first collision
         * shape from the node cell down {@link #NODE_GROUND_ROWS} rows tops at most one step below the node floor.
         * A fluid counts as ground: water and lava are vanilla's own types and costs, and a body that goes over a
         * shore steps into the fluid, it does not fall.
         */
        @Override
        public boolean grounded(int x, int z) {
            for (int by = this.nodeY; by > this.nodeY - NODE_GROUND_ROWS; by--) {
                BlockState state = this.region.getBlockState(this.pos.set(x, by, z));
                if (!state.getFluidState().isEmpty()) {
                    return true;
                }
                VoxelShape shape = state.getCollisionShape(this.region, this.pos, this.context);
                if (!shape.isEmpty()) {
                    return GroundSupport.columnGrounds(this.nodeY, by + shape.max(Direction.Axis.Y));
                }
            }
            return false;
        }
    }

    /**
     * The rim rules for this dinosaur's next path computation, or {@code null} for a body one column wide
     * (floor(width + 1) = 1, vanilla's footprint): it lies inside its column, so it has no rim to hang over.
     */
    public static @Nullable NodeSupport nodeSupport(Dinosaur dinosaur) {
        int footprint = Mth.floor(dinosaur.getBbWidth() + 1.0F);
        return footprint < 2 ? null : new NodeSupport(dinosaur, footprint);
    }

    /** Collision only: does the box fit at feet height or one step up? The guard's test. Cached for this tick. */
    boolean fits(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z) {
        refresh(level);
        double qx = ClearanceBox.quantise(x);
        double qy = ClearanceBox.quantise(feetY);
        double qz = ClearanceBox.quantise(z);
        long key = box.probeKey(qx, qy, qz);
        byte cached = this.fitCache.get(key);
        if (cached != FIT_ABSENT) {
            return cached == FIT_YES;
        }
        boolean result = boxClear(level, walker, box, qx, qy, qz) || boxClear(level, walker, box, qx, qy + STEP_HEIGHT, qz);
        this.fitCache.put(key, result ? FIT_YES : FIT_NO);
        return result;
    }

    /**
     * Walks a straight line from (fromX, fromY, fromZ) to (toX, toZ), standing every {@link #REACH_STEP}
     * blocks, each step starting from the height found at the previous one.
     *
     * @return the feet height at the destination, or NaN if any step cannot stand
     */
    double reachY(ServerLevel level, Entity walker, ClearanceBox box, double fromX, double fromY, double fromZ, double toX, double toZ) {
        double dx = toX - fromX;
        double dz = toZ - fromZ;
        int steps = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dz * dz) / REACH_STEP));
        double y = fromY;
        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;
            y = standY(level, walker, box, fromX + dx * t, y, fromZ + dz * t);
            if (Double.isNaN(y)) {
                return Double.NaN;
            }
        }
        return y;
    }

    /**
     * How far the box can stand sideways from (x, z) along the unit direction (sideX, sideZ), in steps of
     * {@link #SIDE_ROOM_STEP}, at most {@link #MAX_SIDE_ROOM}.
     */
    float sideRoom(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z, double sideX, double sideZ) {
        return sideRoom(level, walker, box, x, feetY, z, sideX, sideZ, MAX_SIDE_ROOM);
    }

    /**
     * {@link #sideRoom} measured only as far as {@code enough}: the first step at or past it ends the measure,
     * so a caller that needs to know "at least this much" (the passage rule, {@code FollowFormation.roomBeside})
     * does not probe the whole {@link #MAX_SIDE_ROOM} in open ground. Returns at least {@code enough} when the
     * room reaches it, the room found otherwise.
     */
    float sideRoom(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z,
                   double sideX, double sideZ, double enough) {
        double limit = Math.min(enough, MAX_SIDE_ROOM);
        for (double shift = SIDE_ROOM_STEP; ; shift += SIDE_ROOM_STEP) {
            if (!canStand(level, walker, box, x + sideX * shift, feetY, z + sideZ * shift)) {
                return (float) (shift - SIDE_ROOM_STEP);
            }
            if (shift >= limit - EPSILON) {
                return (float) shift;
            }
        }
    }

    private void refresh(ServerLevel level) {
        long now = level.getGameTime();
        if (now != this.fitTick) {
            this.fitCache.clear();
            this.fitTick = now;
        }
        if (now - this.standTick >= STAND_CACHE_TICKS || now < this.standTick) {
            this.standCache.clear();
            this.standTick = now;
        }
    }

    private double standUncached(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z) {
        if (boxClear(level, walker, box, x, feetY, z)) {
            // The step-up scans a subset of the same columns, so it cannot succeed when this one fits.
            return feetHazard(level, box, x, feetY, z) ? Double.NaN : groundBelow(level, walker, box, x, feetY, z);
        }
        double up = feetY + STEP_HEIGHT;
        if (boxClear(level, walker, box, x, up, z) && !feetHazard(level, box, x, up, z)) {
            return groundBelow(level, walker, box, x, up, z);
        }
        return Double.NaN;
    }

    /** The vanilla collision query on the whole box, a hair smaller so touching faces do not count. */
    private boolean boxClear(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z) {
        double half = box.halfWidth();
        AABB aabb = new AABB(x - half + EPSILON, feetY + EPSILON, z - half + EPSILON,
                x + half - EPSILON, feetY + box.height() - EPSILON, z + half - EPSILON);
        return level.noCollision(walker, aabb);
    }

    /** A hazard in the feet layer (water has no collision shape, so the fit test does not see it). */
    private boolean feetHazard(ServerLevel level, ClearanceBox box, double x, double feetY, double z) {
        double half = box.halfWidth();
        int y = Mth.floor(feetY + EPSILON);
        for (int bx = Mth.floor(x - half + EPSILON); bx <= Mth.floor(x + half - EPSILON); bx++) {
            for (int bz = Mth.floor(z - half + EPSILON); bz <= Mth.floor(z + half - EPSILON); bz++) {
                if (isHazard(level.getBlockState(this.pos.set(bx, y, bz)))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The ground under the box within {@link #MAX_DROP} blocks below the feet: the highest ground top under its
     * columns, or NaN if any column meets a hazard first or the box centre is not over ground (fix P12c, the
     * centre column rule, {@link GroundSupport#standTop}). Every herd placement test goes through it
     * ({@link #standY}): steering masks, lane room ({@link #sideRoom}), graze spots and leader graze moves
     * ({@link #reachY}), journey targets and leg points ({@link #surfaceStandY}), side steps, tight ground and the
     * guard.
     */
    private double groundBelow(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z) {
        this.scanLevel = level;
        this.scanContext = CollisionContext.of(walker);
        this.scanStart = Mth.floor(feetY - EPSILON);
        double top = GroundSupport.standTop(x, z, box.halfWidth(), this.columnScan);
        this.scanLevel = null;
        this.scanContext = null;
        return top;
    }

    /**
     * One column of {@link #groundBelow}: the top of the first collision shape from the row under the feet down
     * {@link #MAX_DROP} more rows, NaN if none, {@link GroundSupport#HAZARD} if a hazard comes first.
     */
    private double scanColumn(int bx, int bz) {
        for (int by = this.scanStart; by >= this.scanStart - MAX_DROP; by--) {
            BlockState state = this.scanLevel.getBlockState(this.pos.set(bx, by, bz));
            if (isHazard(state)) {
                return GroundSupport.HAZARD;
            }
            VoxelShape shape = state.getCollisionShape(this.scanLevel, this.pos, this.scanContext);
            if (!shape.isEmpty()) {
                return by + shape.max(Direction.Axis.Y);
            }
        }
        return Double.NaN;
    }

    /**
     * Where the feet rest at a path node: the highest {@link ClearanceBox#columnFeet} over the hitbox
     * columns (vanilla's footprint, floor(width + 1) square: half the hitbox width each side of the node
     * centre lands on exactly those columns), read from the node cell row and the row below, or the node
     * floor when every column is a hole (a cell vanilla only falls through). The clearance box's wider
     * columns are not used here: a block in an overhang column (a wall beside a velociraptor or a
     * triceratops) is not under the body and must not lift the feet; the fit test looks at it instead.
     * Reads through the path's region (no chunk loads).
     */
    private double feetAt(BlockGetter region, CollisionContext context, double hitboxWidth, double x, int nodeY, double z) {
        double half = hitboxWidth / 2D;
        double feet = Double.NaN;
        for (int bx = Mth.floor(x - half + EPSILON); bx <= Mth.floor(x + half - EPSILON); bx++) {
            for (int bz = Mth.floor(z - half + EPSILON); bz <= Mth.floor(z + half - EPSILON); bz++) {
                double column = ClearanceBox.columnFeet(nodeY, shapeTop(region, context, bx, nodeY, bz), shapeTop(region, context, bx, nodeY - 1, bz));
                if (!Double.isNaN(column) && !(column <= feet)) {
                    feet = column;
                }
            }
        }
        return Double.isNaN(feet) ? nodeY : feet;
    }

    /** The top of the collision shape in one cell, 0 when it has none. */
    private double shapeTop(BlockGetter region, CollisionContext context, int bx, int by, int bz) {
        VoxelShape shape = region.getBlockState(this.pos.set(bx, by, bz)).getCollisionShape(region, this.pos, context);
        return shape.isEmpty() ? 0D : shape.max(Direction.Axis.Y);
    }

    /**
     * The path test's pre-filter: no collision shape in the box's columns, from the row under the feet up
     * to the box top, reaches above the feet, so the collision query could find no block and the box fits.
     * The row under the feet counts because a fence, wall or fence gate there is 1.5 high and reaches into
     * the box (the feet rule caps the feet at the node floor). It can only say "fits", never "does not",
     * and it does not look at entities (boats) or the world border; vanilla's typing ignores them too, and
     * the guard still sees them. Reads through the path's region (no chunk loads).
     */
    private boolean boxCellsEmpty(BlockGetter region, CollisionContext context, ClearanceBox box, double x, double feetY, double z) {
        double half = box.halfWidth();
        int top = Mth.floor(feetY + box.height() - EPSILON);
        for (int by = Mth.floor(feetY + EPSILON) - 1; by <= top; by++) {
            for (int bx = Mth.floor(x - half + EPSILON); bx <= Mth.floor(x + half - EPSILON); bx++) {
                for (int bz = Mth.floor(z - half + EPSILON); bz <= Mth.floor(z + half - EPSILON); bz++) {
                    double shapeTop = shapeTop(region, context, bx, by, bz);
                    if (shapeTop > 0D && by + shapeTop > feetY + EPSILON) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isHazard(BlockState state) {
        return !state.getFluidState().isEmpty()
                || state.is(BlockTags.FIRE) || state.is(Blocks.CACTUS) || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.POWDER_SNOW)
                || state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS) || state.is(BlockTags.FENCE_GATES)
                || state.is(BlockInit.ELECTRIC_FENCE.get())
                || state.is(BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get())
                || state.is(BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get());
    }
}
