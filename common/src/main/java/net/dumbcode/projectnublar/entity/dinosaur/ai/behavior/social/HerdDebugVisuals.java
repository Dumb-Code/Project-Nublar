package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

/**
 * Why a herd member stands where it stands, drawn in the world (playtest 1 fix P7): dust particles the
 * server sends to the players near the herd, nothing on the client and no packet of its own. Switched on
 * per herd with the debug stick ({@link DinoGroup#setDebugVisuals}); the leader's tick calls {@link #tick}
 * once per herd per tick, which costs one check while the herd is off. Every {@link #DRAW_EVERY_TICKS}, if
 * a player is within {@link #VIEW_RANGE} of the leader, four layers are drawn, each in its own colour:
 * <ol>
 *   <li><b>Steering</b> (every member the herd layer moves: follower steering, unstack, side step): a column
 *       of {@link #TARGET_COLUMN_POINTS} dots at its target and a {@link #HEADING_LENGTH}-block line along
 *       the heading it walks, <b>green</b> while walking (a column alone: it stands at its target),
 *       <b>yellow</b> while waiting or yielding to members, <b>teal</b> while held by a standing herd mate it
 *       asks to make way, <b>red</b> while stopped by blocks or the guard
 *       ({@link HerdMoveControl#debugState}). A member on a vanilla path (the leader, a fallback) is not
 *       steering: see layer 4.</li>
 *   <li><b>Travel</b> (TRAVEL only): every {@link #TRAIL_STRIDE}nd trail point in <b>cyan</b>, and each
 *       follower's slot ({@link TravelOrder#slot}: its target arc on the trail moved out by its lane offset,
 *       clamped by the side room as the follower's own target is) as a column of {@link #SLOT_COLUMN_POINTS}
 *       <b>magenta</b> dots; no column while its slot is not on the trail yet.</li>
 *   <li><b>Graze</b> (GRAZE only): each reserved spot, the leader's included, as a column of
 *       {@link #SLOT_COLUMN_POINTS} <b>orange</b> dots; the herd area's centre as a column of
 *       {@link #CENTRE_COLUMN_POINTS} <b>blue</b> dots and its edge as a ring of {@link #RING_POINTS} blue
 *       dots at radius R. These points have no height of their own: they are drawn on the heightmap
 *       (motion-blocking, no leaves), so under a roof they sit on the roof.</li>
 *   <li><b>Clearance</b>: the clearance box's outline ({@link DebugShapes#boxEdges}) in <b>purple</b> at the
 *       guard's test point of a member the guard stopped at blocks within the last draw interval; and the
 *       remaining nodes (at most {@link #MAX_PATH_NODES}) of each member's active vanilla path in
 *       <b>light grey</b>, or <b>pink</b> while the member ghosts through herd mates on its fallback path
 *       ({@link HerdMoveControl#ghosting}), where the body stands at each node ({@link ClearanceBox#nodeCentre}).</li>
 * </ol>
 * Budget: at most {@link #MAX_PARTICLES_PER_DRAW} particles per herd per draw, whatever the herd's size: one
 * counter shared by all four layers, so once it is spent the shapes drawn later in the order above are cut,
 * layer 4 first; a very large herd could also cut into layers 2-3. While a herd is on, each draw allocates a
 * little (this drawer, a few lambdas, collection iterators); while every herd is off nothing is allocated.
 * Nothing here changes herd state: only read-only accessors.
 */
final class HerdDebugVisuals {

    /** A draw every this many game ticks (dust particles live about 8-40 ticks, so the shapes stay visible). */
    static final int DRAW_EVERY_TICKS = 5;
    /** Nothing is drawn unless a player is within this many blocks of the leader. */
    static final double VIEW_RANGE = 128D;
    /** The hard cap per herd per draw. */
    static final int MAX_PARTICLES_PER_DRAW = 600;
    /** Every this many trail points is drawn. */
    static final int TRAIL_STRIDE = 2;
    static final int RING_POINTS = 24;
    /** Points inside each box edge, besides the 8 corners: 8 + 12 x this per box. */
    static final int BOX_EDGE_INTERIOR_POINTS = 2;
    static final double HEADING_LENGTH = 2D;
    static final int HEADING_POINTS = 5;
    static final int TARGET_COLUMN_POINTS = 3;
    static final int SLOT_COLUMN_POINTS = 2;
    static final int CENTRE_COLUMN_POINTS = 6;
    /** Dots in a column are this far apart. */
    static final double COLUMN_STEP = 0.5D;
    /** Dots sit this far above the feet or the ground, so grass does not hide them. */
    static final double LIFT = 0.5D;
    /** Remaining path nodes drawn per member. */
    static final int MAX_PATH_NODES = 24;
    static final float DOT_SIZE = 0.6F;

    private static final DustParticleOptions WALKING = colour(0.2F, 1F, 0.2F);
    private static final DustParticleOptions WAITING = colour(1F, 1F, 0.2F);
    private static final DustParticleOptions ASKING = colour(0F, 0.75F, 0.6F);
    private static final DustParticleOptions BLOCKED = colour(1F, 0.2F, 0.2F);
    private static final DustParticleOptions TRAIL = colour(0.2F, 0.9F, 1F);
    private static final DustParticleOptions SLOT = colour(1F, 0.3F, 1F);
    private static final DustParticleOptions SPOT = colour(1F, 0.6F, 0.1F);
    private static final DustParticleOptions AREA = colour(0.3F, 0.4F, 1F);
    private static final DustParticleOptions BOX = colour(0.7F, 0.1F, 0.9F);
    private static final DustParticleOptions PATH = colour(0.85F, 0.85F, 0.85F);
    private static final DustParticleOptions GHOST_PATH = colour(1F, 0.5F, 0.7F);

    private final ServerLevel level;
    private final List<ServerPlayer> players;
    private final long now;
    private final double leaderX;
    private final double leaderY;
    private final double leaderZ;
    private int sent;

    private HerdDebugVisuals(ServerLevel level, Dinosaur leader, long now) {
        this.level = level;
        this.players = level.players();
        this.now = now;
        this.leaderX = leader.getX();
        this.leaderY = leader.getY();
        this.leaderZ = leader.getZ();
    }

    /**
     * The leader's per-tick call. One check while the herd's visuals are off; otherwise a draw every
     * {@link #DRAW_EVERY_TICKS} when a player is within {@link #VIEW_RANGE} of the leader.
     */
    static void tick(ServerLevel level, Dinosaur leader, DinoGroup group, long now) {
        if (!group.debugVisuals() || now % DRAW_EVERY_TICKS != 0) {
            return;
        }
        HerdDebugVisuals draw = new HerdDebugVisuals(level, leader, now);
        if (draw.anyPlayerNear()) {
            draw.herd(leader, group);
        }
    }

    private static DustParticleOptions colour(float red, float green, float blue) {
        return new DustParticleOptions(new Vector3f(red, green, blue), DOT_SIZE);
    }

    private boolean anyPlayerNear() {
        for (int i = 0; i < this.players.size(); i++) {
            if (playerNear(this.players.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean playerNear(ServerPlayer player) {
        return player.distanceToSqr(this.leaderX, this.leaderY, this.leaderZ) <= VIEW_RANGE * VIEW_RANGE;
    }

    private void herd(Dinosaur leader, DinoGroup group) {
        HerdBodies bodies = GroupManager.bodiesOf(group, this.level);
        for (int i = 0; i < bodies.count(); i++) {
            Dinosaur member = GroupManager.resolve(this.level, bodies.id(i));
            if (member != null && member.getMoveControl() instanceof HerdMoveControl control) {
                steering(member, control);
            }
        }
        if (group.state() == HerdState.TRAVEL) {
            travel(group);
        } else {
            graze(leader, group);
        }
        for (int i = 0; i < bodies.count(); i++) {
            Dinosaur member = GroupManager.resolve(this.level, bodies.id(i));
            if (member != null && member.getMoveControl() instanceof HerdMoveControl control) {
                clearance(member, control);
            }
        }
    }

    /** Layer 1. */
    private void steering(Dinosaur member, HerdMoveControl control) {
        HerdMoveControl.DebugState state = control.debugState();
        if (state == null) {
            return;
        }
        DustParticleOptions options = switch (state) {
            case WALKING -> WALKING;
            case WAITING -> WAITING;
            case ASKING -> ASKING;
            case BLOCKED -> BLOCKED;
        };
        double y = member.getY() + LIFT;
        column(options, control.lastTargetX(), y, control.lastTargetZ(), TARGET_COLUMN_POINTS);
        int heading = control.heading();
        if (heading != ContextSteering.STOP) {
            double x = member.getX();
            double z = member.getZ();
            DebugShapes.line(x, y, z, x + ContextSteering.dirX(heading) * HEADING_LENGTH, y,
                    z + ContextSteering.dirZ(heading) * HEADING_LENGTH, HEADING_POINTS, (px, py, pz) -> dot(options, px, py, pz));
        }
    }

    /** Layer 2. */
    private void travel(DinoGroup group) {
        LeaderTrail trail = group.trail();
        for (int k = 0; k < trail.size(); k += TRAIL_STRIDE) {
            dot(TRAIL, trail.x(k), trail.y(k) + LIFT, trail.z(k));
        }
        List<UUID> order = group.travelOrder();
        for (int i = 0; i < order.size(); i++) {
            UUID follower = order.get(i);
            double arc = group.targetArcOf(follower);
            if (Double.isNaN(arc) || arc < trail.tailArc()) {
                continue;
            }
            // The lane offset clamped by the side room as the follower's own target is (TravelOrder.slot), so
            // the dot never sits inside a wall the real target keeps out of.
            TravelOrder.Target slot = TravelOrder.slot(trail, arc, group.laneOffsetOf(follower));
            if (slot != null) {
                column(SLOT, slot.x(), slot.y() + LIFT, slot.z(), SLOT_COLUMN_POINTS);
            }
        }
    }

    /** Layer 3. */
    private void graze(Dinosaur leader, DinoGroup group) {
        if (!group.hasCentre()) {
            return;
        }
        double width = leader.getBbWidth();
        double radius = group.radius(HerdSpacing.personalSpace(width, width), width);
        double centreX = group.centreX();
        double centreZ = group.centreZ();
        column(AREA, centreX, groundY(centreX, centreZ), centreZ, CENTRE_COLUMN_POINTS);
        DebugShapes.ring(centreX, centreZ, radius, RING_POINTS, (x, z) -> dot(AREA, x, groundY(x, z), z));
        for (HerdArea.Point spot : group.reservedSpots()) {
            column(SPOT, spot.x(), groundY(spot.x(), spot.z()), spot.z(), SLOT_COLUMN_POINTS);
        }
    }

    /** Layer 4. */
    private void clearance(Dinosaur member, HerdMoveControl control) {
        if (control.guardWallTick() != DinoGroup.UNSET && this.now - control.guardWallTick() < DRAW_EVERY_TICKS) {
            ClearanceBox box = control.box();
            DebugShapes.boxEdges(control.guardWallX(), control.guardWallY(), control.guardWallZ(), box.halfWidth(), box.height(),
                    BOX_EDGE_INTERIOR_POINTS, (x, y, z) -> dot(BOX, x, y, z));
        }
        Path path = member.getNavigation().getPath();
        if (path == null || member.getNavigation().isDone()) {
            return;
        }
        double width = member.getBbWidth();
        DustParticleOptions options = control.ghosting() ? GHOST_PATH : PATH;
        int end = Math.min(path.getNodeCount(), path.getNextNodeIndex() + MAX_PATH_NODES);
        for (int i = path.getNextNodeIndex(); i < end; i++) {
            Node node = path.getNode(i);
            dot(options, ClearanceBox.nodeCentre(node.x, width), node.y + LIFT, ClearanceBox.nodeCentre(node.z, width));
        }
    }

    /** The first air block above the highest motion-blocking block (leaves aside) at (x, z), plus {@link #LIFT}. */
    private double groundY(double x, double z) {
        return this.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z)) + LIFT;
    }

    /** {@code points} dots stacked {@link #COLUMN_STEP} apart from (x, y, z) upward. */
    private void column(DustParticleOptions options, double x, double y, double z, int points) {
        for (int i = 0; i < points; i++) {
            dot(options, x, y + i * COLUMN_STEP, z);
        }
    }

    /** One particle, sent to every player in range (forced: the 32-block limit of plain particles is too short for a herd). */
    private void dot(DustParticleOptions options, double x, double y, double z) {
        if (this.sent >= MAX_PARTICLES_PER_DRAW) {
            return;
        }
        this.sent++;
        for (int i = 0; i < this.players.size(); i++) {
            ServerPlayer player = this.players.get(i);
            if (playerNear(player)) {
                this.level.sendParticles(player, options, true, x, y, z, 1, 0D, 0D, 0D, 0D);
            }
        }
    }
}
