package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * The leader's per-tick part of herd movement (spec "Leader", "Herd area"), called from every dinosaur's
 * server tick; for non-leaders it only settles a change of lead ({@link #settleLeaderChange}) and returns.
 * <ul>
 *   <li><b>TRAVEL</b>: record the trail every block walked and measure the side room of each new point with
 *       the clearance box; rebuild the travel order after a join or leave; on a roaming journey set the first
 *       walk target when the leader's journey-start call ends (fix P10, {@code DinoOneShots}), and set the next
 *       leg each time the walk ends while the leader still gets on (a vanilla walk reaches at most the follow
 *       range and runs 150-249 ticks; legs toward a far target, fix P11, {@link #walkNextLeg}); arrival (within
 *       3 blocks, or the walk ended twice in one place, or no leg is usable, or an urgent trip's walk ended)
 *       holds TRAVEL 100 more ticks, then GRAZE and recentre; roaming
 *       journeys wait for stragglers (a slow walk with the walk target kept, never a stop, P13a ruling 3a; at
 *       most 200 ticks; each follower measured against its own slot, and only followers whose slot is on the
 *       trail count); urgent trips never wait. A roaming journey pauses while the leader is down
 *       ({@link #paused}, P13a): no path end, no leg, no wait; then it walks on to the same target. In the
 *       species' rest hours the leader lies down, so a journey caught by night pauses until morning. Each
 *       journey end gives one server log line ({@link #logEnd}). A drag journey (below) ends when the leader has stood still
 *       for {@link JourneyPolicy#DRAG_SETTLE_TICKS}, or becomes an ordinary urgent trip when the leader (not
 *       leashed) takes a walk target. A leashed leader turns any journey into a drag (trail and slots kept).</li>
 *   <li><b>GRAZE</b>: a leader that moves out of the herd area with no walk of its own (dragged on a lead,
 *       pushed, knocked back, a short teleport) starts a drag journey at once, so the herd follows its trail
 *       (playtest 2 fix P8c); a walk target the herd did not plan (water, food, a feeder) that leaves the area
 *       starts an urgent journey (decision 16); otherwise recentre after 100 ticks outside the herd area (a
 *       leader standing outside it, or one whose own walk inside the area took it out).</li>
 * </ul>
 */
public final class HerdLeading {

    private HerdLeading() {
    }

    public static void tick(Dinosaur dinosaur) {
        if (!(dinosaur.level() instanceof ServerLevel level)) {
            return;
        }
        DinoGroup group = GroupManager.groupOf(dinosaur);
        if (group == null) {
            return;
        }
        // A brain this tick may have changed the lead (a join, a challenge): settle it before the leader's
        // graze sees a leftover follower walk as a need outside the area (L2).
        settleLeaderChange(level, group);
        // A leader change ended the journey (any member's tick logs it).
        logEnd(group);
        if (!group.isLeader(dinosaur.getUUID())) {
            return;
        }
        long start = System.nanoTime();
        level.getProfiler().push("herdLead");
        long now = level.getGameTime();
        double width = dinosaur.getBbWidth();
        double radius = group.radius(HerdSpacing.personalSpace(width, width), width);
        if (!group.hasCentre()) {
            // At the start, and after a leader change ended a journey: the area forms on the current leader.
            group.recentre(dinosaur.getX(), dinosaur.getY(), dinosaur.getZ());
        }
        // Every leader tick, so the step is always one tick's move (a drag shows in it, P8c).
        double step = group.leaderStep(dinosaur.getX(), dinosaur.getZ(), now);
        if (group.state() == HerdState.TRAVEL) {
            travel(level, dinosaur, group, now);
        } else {
            graze(level, dinosaur, group, radius, step, now);
        }
        if (!group.journeyActive()) {
            // A journey that ended outside a pause's end (arrival, drag, leader change): no tired mark stays.
            dropUnusedTiredMarks(level, group);
        }
        level.getProfiler().pop();
        HerdTimings.record(now, System.nanoTime() - start);
        logEnd(group);
        // After the timing: the debug particles (lead's toggle) must not count as herd AI cost.
        HerdDebugVisuals.tick(level, dinosaur, group, now);
    }

    /**
     * Settles a change of lead in the animal's group ({@link #settleLeaderChange(ServerLevel, DinoGroup)}).
     * {@code Dinosaur} calls it before its brain ticks, so no brain runs on a stale walk.
     */
    public static void settleLeaderChange(Dinosaur dinosaur) {
        if (dinosaur.level() instanceof ServerLevel level) {
            DinoGroup group = GroupManager.groupOf(dinosaur);
            if (group != null) {
                settleLeaderChange(level, group);
            }
        }
    }

    /**
     * After a change of lead (playtest 1, L1 and L2), every deposed leader and the new leader drop their walk
     * target and stop their navigation: a deposed leader kept the journey walk, slowed to 0 or 0.3 while it
     * waited for stragglers, for up to 249 ticks; a new leader kept a follower's fallback walk, which could
     * start an urgent trip back to the old trail. A won challenge does this too ({@link ChallengeGroupLeader}).
     * Nothing to do (one check) unless the lead changed. An animal that is not loaded is skipped.
     */
    static void settleLeaderChange(ServerLevel level, DinoGroup group) {
        if (!group.leaderChanged() && group.deposedLeaders().isEmpty()) {
            return;
        }
        // First: the lookup may elect a new leader, which adds to the deposed list.
        Dinosaur leader = group.leaderChanged() ? GroupManager.leaderOf(group, level) : null;
        for (UUID id : group.deposedLeaders()) {
            Dinosaur deposed = GroupManager.resolve(level, id);
            if (deposed != null) {
                dropWalk(deposed);
            }
        }
        if (leader != null) {
            dropWalk(leader);
        }
        group.leaderChangeSettled();
    }

    private static void dropWalk(Dinosaur dinosaur) {
        BrainUtils.clearMemory(dinosaur, MemoryModuleType.WALK_TARGET);
        dinosaur.getNavigation().stop();
    }

    /** The present followers, nearest to the leader first (spec "Travel order"). */
    static List<UUID> followersByDistance(ServerLevel level, DinoGroup group, Dinosaur leader) {
        HerdBodies bodies = GroupManager.bodiesOf(group, level);
        List<UUID> ids = new ArrayList<>(bodies.count());
        double[] xs = new double[bodies.count()];
        double[] zs = new double[bodies.count()];
        for (int i = 0; i < bodies.count(); i++) {
            if (bodies.id(i).equals(leader.getUUID())) {
                continue;
            }
            xs[ids.size()] = bodies.x(i);
            zs[ids.size()] = bodies.z(i);
            ids.add(bodies.id(i));
        }
        return TravelOrder.byDistance(leader.getX(), leader.getZ(), ids, xs, zs);
    }

    private static void travel(ServerLevel level, Dinosaur leader, DinoGroup group, long now) {
        LeaderTrail trail = group.trail();
        int index = trail.record(leader.getX(), leader.getY(), leader.getZ());
        if (index >= 0) {
            if (trail.size() == 2) {
                // The first point had no direction when it was recorded, so its room could not be measured
                // then; it gets the first segment's direction now. It sits one slot behind in the ring buffer.
                measureRoom(level, leader, group, Math.floorMod(index - 1, LeaderTrail.CAPACITY), trail.pointAt(trail.tailArc()));
            }
            measureRoom(level, leader, group, index, trail.newest());
        }
        if (group.orderDirty()) {
            group.setTravelOrder(followersByDistance(level, group, leader));
        }
        if (group.settling()) {
            if (group.settleDone(now)) {
                group.endJourney(leader.getX(), leader.getY(), leader.getZ());
            }
            return;
        }
        if (!HerdSpacing.applies(leader)) {
            // A fighting leader's walk is the fight's (the chase at its own speed): no re-issue, no arrival by
            // a walk's end and no gathering until spacing applies again. The trail and order above still run.
            return;
        }
        if (leader.isLeashed() && !group.dragged()) {
            // P8c fix round 1: the lead decides where a leashed leader goes, so any journey becomes a drag; the
            // trail, order and slots stay. Undo a straggler slow-down first: the walk it slowed is dropped.
            if (group.leaderSlowed() && leader.getNavigation().isInProgress()) {
                leader.getNavigation().setSpeedModifier(JourneyPolicy.JOURNEY_SPEED);
            }
            BrainUtils.clearMemory(leader, MemoryModuleType.WALK_TARGET);
            group.becomeDrag(leader.getX(), leader.getY(), leader.getZ());
        }
        if (group.dragged()) {
            WalkTarget walkTarget = BrainUtils.getMemory(leader, MemoryModuleType.WALK_TARGET);
            if (walkTarget == null || leader.isLeashed()) {
                if (walkTarget != null) {
                    // A leashed leader stays dragged: a need's walk set during the drag would fight the lead.
                    BrainUtils.clearMemory(leader, MemoryModuleType.WALK_TARGET);
                }
                followDrag(leader, group);
                return;
            }
            // Free, with a walk of its own (a need): from now on the usual urgent arrival rules.
            Vec3 target = walkTarget.getTarget().currentPosition();
            group.adoptTarget(target.x, target.y, target.z);
        }
        if (leader.oneShots().journeyCallWaiting()) {
            // Fix P10: a roaming journey's first walk waits for the leader's call (LeaderJourney.begin); no path
            // end or arrival is counted meanwhile. Any other journey never waits.
            if (group.journeyKind() != JourneyPolicy.Kind.ROAM) {
                leader.oneShots().cancelJourneyCall();
            } else {
                if (leader.oneShots().countDownJourneyCall() && !walkNextLeg(level, leader, group)) {
                    // Fix P11: no usable first leg: the journey ends where the leader is, as an arrival.
                    group.recordEnd(JourneyPolicy.End.NO_FIRST_LEG, leader.getUUID(), leader.getX(), leader.getZ(), now);
                    group.arrive(now);
                }
                return;
            }
        }
        if (group.journeyKind() == JourneyPolicy.Kind.ROAM && paused(level, leader, group, now)) {
            return;
        }
        double dx = group.journeyX() - leader.getX();
        double dz = group.journeyZ() - leader.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        // P13a ruling 3a: the leader never stands for stragglers (a slow walk), so every walk end is a path end.
        boolean pathEnded = !BrainUtils.hasMemory(leader, MemoryModuleType.WALK_TARGET) && !leader.getNavigation().isInProgress();
        boolean noLeg = false;
        if (pathEnded && distance > JourneyPolicy.ARRIVE_DISTANCE && group.journeyKind() == JourneyPolicy.Kind.ROAM
                && group.pathEnded(leader.getX(), leader.getZ())) {
            // Review Focus 6: a vanilla walk reaches at most the follow range (35 blocks) and MoveToWalkTarget
            // runs 150-249 ticks, then erases the walk target. The leader got on since the last end: walk the
            // next leg (fix P11). (An urgent trip's walk target belongs to the need, so that journey ends with it.)
            if (walkNextLeg(level, leader, group)) {
                return;
            }
            // Fix P11: no usable leg: the journey ends where the leader is. pathEnded is true, so the arrival
            // check below takes it.
            noLeg = true;
        }
        if (JourneyPolicy.arrived(distance, pathEnded)) {
            if (group.leaderSlowed() && leader.getNavigation().isInProgress()) {
                // Arrived while slowed for stragglers: let the walk finish at its own speed.
                leader.getNavigation().setSpeedModifier(JourneyPolicy.JOURNEY_SPEED);
            }
            group.recordEnd(JourneyPolicy.endReason(distance, group.journeyKind(), noLeg), leader.getUUID(),
                    leader.getX(), leader.getZ(), now);
            group.arrive(now);
            return;
        }
        if (group.journeyKind() == JourneyPolicy.Kind.ROAM) {
            gather(level, leader, group);
        }
    }

    /**
     * P13a items 1 and 3: the pause of a roaming journey ({@link JourneyPolicy#pause}) while the leader is down
     * (sits down, rests, gets up: {@link HerdNeeds#isDown}). While paused: no path end, no leg, no wait ticks.
     * In the species' rest hours (night for a day animal) the leader lies down ({@link HerdNeeds#lieDown}) once
     * it is on dry ground (fix round 1: it walks on out of water first), so a journey caught by night pauses
     * until morning: {@code Rest} ends then, at full stamina. A rest pause at night or longer than
     * {@link JourneyPolicy#SHORT_REST_PAUSE_TICKS} lets the herd spread out to rest ({@link DinoGroup#spreadHerd}),
     * but no spread starts while the leader gets up. The first tick the leader is up ends the pause
     * ({@link #resume}); the usual walk-end handling below then walks the next leg toward the same target.
     *
     * @return {@code true} while the journey is paused this tick
     */
    private static boolean paused(ServerLevel level, Dinosaur leader, DinoGroup group, long now) {
        boolean restHours = JourneyPolicy.restHours(level.getDayTime(), leader.getDinoBehaviour().isNocturnal());
        boolean down = HerdNeeds.isDown(leader);
        if (restHours && !down && leader.onGround() && !leader.isInWater()) {
            // Ruling Q2: the leader lies down for the night; Rest starts on a later brain tick.
            HerdNeeds.lieDown(leader, group);
        }
        JourneyPolicy.Pause pause = JourneyPolicy.pause(down);
        if (pause == JourneyPolicy.Pause.NONE) {
            if (group.journeyPaused()) {
                resume(level, leader, group, now);
            }
            return false;
        }
        if (group.startPause(pause, now)) {
            if (group.leaderSlowed()) {
                group.setLeaderSlowed(false);
                if (leader.getNavigation().isInProgress()) {
                    // The leg's walk (held while the leader sits down) goes on at journey speed if it outlasts the pause.
                    leader.getNavigation().setSpeedModifier(JourneyPolicy.JOURNEY_SPEED);
                }
            }
            group.setStragglerSlow(false);
            if (group.debugVisuals()) {
                Constants.LOG.info("[herd debug] leader {}: journey paused ({}), {} blocks from the target", leader.getUUID(), pause,
                        String.format(Locale.ROOT, "%.1f", Math.hypot(group.journeyX() - leader.getX(), group.journeyZ() - leader.getZ())));
            }
        }
        // Fix round 1, item 2: members at a rest spot stop lying down once the leader gets up.
        boolean rising = HerdNeeds.isRising(leader);
        group.setLeaderRising(rising);
        if (!group.herdSpread() && JourneyPolicy.herdSpreads(group.pausedTicks(now), restHours, rising)) {
            group.spreadHerd();
            if (group.debugVisuals()) {
                Constants.LOG.info("[herd debug] leader {}: the herd spreads out to rest after {} paused ticks{}", leader.getUUID(),
                        group.pausedTicks(now), restHours ? " (rest hours)" : "");
            }
        }
        return true;
    }

    /**
     * The leader is up and done: the pause ends and the previous path end is forgotten ({@link DinoGroup#endPause}).
     * A herd that spread out gets a new travel order, nearest first, so the wedge re-forms from where the members
     * are; members still lying down follow when they are up (the straggler wait covers them).
     */
    private static void resume(ServerLevel level, Dinosaur leader, DinoGroup group, long now) {
        JourneyPolicy.Pause pause = group.pause();
        int ticks = group.pausedTicks(now);
        if (group.endPause()) {
            group.setTravelOrder(followersByDistance(level, group, leader));
        }
        dropUnusedTiredMarks(level, group);
        if (group.debugVisuals()) {
            Constants.LOG.info("[herd debug] leader {}: journey goes on after a {} pause of {} ticks", leader.getUUID(), pause, ticks);
        }
    }

    /**
     * Fix round 1, item 5: members (and the leader) that {@link HerdNeeds#lieDown} marked tired but that never
     * started {@code Rest} lose the tired memory again, so they do not sit down later for nothing. Called when a
     * pause ends and once a journey has ended in any other way ({@link #tick}). A marked animal that rests keeps
     * it ({@code Rest.stop} clears it), as does one whose stamina really is low.
     */
    private static void dropUnusedTiredMarks(ServerLevel level, DinoGroup group) {
        if (!group.hasTiredMarks()) {
            return;
        }
        for (UUID id : group.takeTiredMarks()) {
            Dinosaur member = GroupManager.resolve(level, id);
            if (member != null) {
                HerdNeeds.dropUnusedTired(member);
            }
        }
    }

    /**
     * Logs the journey end the group recorded ({@link DinoGroup#recordEnd}), once: one INFO line per journey
     * (P13a item 8) with the herd size, the leader, the kind, the reason, the distance left to the target, the
     * straight distance from the start, the trail length walked and the journey's ticks.
     */
    private static void logEnd(DinoGroup group) {
        JourneyPolicy.JourneyEnd end = group.takeEnd();
        if (end != null) {
            Constants.LOG.info("[herd] Herd of {} led by {}: {}", group.size(), end.leader(), end.describe());
        }
    }

    /**
     * A drag journey (playtest 2 fix P8c): its target follows the leader ({@link DinoGroup#followDrag}), and it ends (GRAZE, recentre on the
     * leader) once the leader has stood still for {@link JourneyPolicy#DRAG_SETTLE_TICKS}. Unleashing does
     * not end it; standing still does. The still time doubles as the settle: the herd closes up while the
     * leader stands.
     */
    private static void followDrag(Dinosaur leader, DinoGroup group) {
        group.followDrag(leader.getX(), leader.getY(), leader.getZ());
        if (JourneyPolicy.dragSettled(group.countStill(leader.getX(), leader.getZ()))) {
            group.recordEnd(JourneyPolicy.End.DRAG_SETTLED, leader.getUUID(), leader.getX(), leader.getZ(),
                    leader.level().getGameTime());
            group.endJourney(leader.getX(), leader.getY(), leader.getZ());
        }
    }

    /**
     * Sets the roaming journey's next leg ({@link LeaderJourney#nextLeg}) as the leader's walk target (playtest
     * fix P11). Every walk of a roaming journey starts here: the first one ({@link LeaderJourney#begin}, or when
     * the journey-start call ends) and each re-issue. {@code false} if no leg is usable; the walk then stays
     * ended, and the journey ends where the leader is, as an arrival ({@link #travel}: in the same tick for a
     * re-issue, the call's end or the end of a pause).
     */
    static boolean walkNextLeg(ServerLevel level, Dinosaur leader, DinoGroup group) {
        Vec3 leg = LeaderJourney.nextLeg(level, leader, group);
        if (leg == null) {
            return false;
        }
        BrainUtils.setMemory(leader, MemoryModuleType.WALK_TARGET, new WalkTarget(leg, JourneyPolicy.JOURNEY_SPEED, LeaderJourney.CLOSE_ENOUGH));
        return true;
    }

    /**
     * Spec "Wait for stragglers" (roaming journeys only). The count is {@link JourneyPolicy.Tally}, from the
     * latest layout: only followers whose slot is on the trail count, each close when it is within its own
     * slot's distance of the leader plus a margin. P13a ruling 3a: the wait is a slow walk for the whole wait
     * (at most {@link JourneyPolicy#MAX_WAIT_TICKS} per journey), never a stop. Only the navigation speed
     * changes; the walk target and path stay, so no new path is computed. (SmartBrainLib's walk cooldown of up
     * to 39 ticks applies only after a stuck stop; a time-out or a reached leg restarts the walk on the next
     * tick.) The speed is restored only after the slow walk changed it, so another task's walk is never
     * touched. While the herd's debug visuals are on, the start and end of each slow walk give a log line.
     */
    private static void gather(ServerLevel level, Dinosaur leader, DinoGroup group) {
        JourneyPolicy.Tally tally = new JourneyPolicy.Tally();
        double trailTail = group.trail().tailArc();
        HerdBodies bodies = GroupManager.bodiesOf(group, level);
        for (int i = 0; i < bodies.count(); i++) {
            UUID id = bodies.id(i);
            if (id.equals(leader.getUUID())) {
                continue;
            }
            double dx = bodies.x(i) - leader.getX();
            double dz = bodies.z(i) - leader.getZ();
            tally.add(Math.sqrt(dx * dx + dz * dz), group.slotDistanceOf(id), group.targetArcOf(id), trailTail);
        }
        JourneyPolicy.Gathering gathering = JourneyPolicy.gathering(group.journeyKind(), tally.gathered(),
                group.waitedTicks());
        switch (gathering) {
            case GO -> {
                if (group.stragglerSlow()) {
                    group.setStragglerSlow(false);
                    // Only the end of a slow walk whose start was logged (DinoGroup.STRAGGLER_LOG_MIN_TICKS).
                    if (group.takeStragglerStartLogged() && group.debugVisuals()) {
                        Constants.LOG.info("[herd debug] leader {}: straggler slow walk ends ({}), waited {} of {} ticks",
                                leader.getUUID(), tally.gathered() ? "gathered" : "wait used up", group.waitedTicks(),
                                JourneyPolicy.MAX_WAIT_TICKS);
                    }
                }
                if (group.leaderSlowed()) {
                    group.setLeaderSlowed(false);
                    if (leader.getNavigation().isInProgress()) {
                        leader.getNavigation().setSpeedModifier(JourneyPolicy.JOURNEY_SPEED);
                    }
                }
            }
            case SLOW -> {
                group.addWait();
                if (!group.stragglerSlow()) {
                    group.setStragglerSlow(true);
                    if (group.debugVisuals() && group.mayLogStragglerStart(level.getGameTime())) {
                        Constants.LOG.info("[herd debug] leader {}: straggler slow walk starts ({} of {} counted followers close), waited {} of {} ticks",
                                leader.getUUID(), tally.close(), tally.counted(), group.waitedTicks(), JourneyPolicy.MAX_WAIT_TICKS);
                    }
                }
                if (leader.getNavigation().isInProgress()) {
                    group.setLeaderSlowed(true);
                    leader.getNavigation().setSpeedModifier(JourneyPolicy.WAIT_SLOW_SPEED);
                }
            }
        }
    }

    /**
     * The leader's graze checks, in this order (playtest 2 fix P8c): a drag ({@link JourneyPolicy#dragStarts},
     * not while fighting or riding) starts an urgent journey and nothing else runs; then the recentre after 100
     * ticks outside (only a leader that is not dragged gets that far); then decision 16, a walk target of its
     * own (not leashed) that leaves the area.
     *
     * @param step the leader's horizontal move since its previous tick ({@link DinoGroup#leaderStep})
     */
    private static void graze(ServerLevel level, Dinosaur leader, DinoGroup group, double radius, double step, long now) {
        double fromCentreX = leader.getX() - group.centreX();
        double fromCentreZ = leader.getZ() - group.centreZ();
        double fromCentre = Math.sqrt(fromCentreX * fromCentreX + fromCentreZ * fromCentreZ);
        boolean outside = !HerdArea.inside(group.centreX(), group.centreZ(), radius, leader.getX(), leader.getZ());
        WalkTarget walkTarget = BrainUtils.getMemory(leader, MemoryModuleType.WALK_TARGET);
        // A leashed leader has no walk of its own: the lead moves it, whatever walk target it still holds.
        boolean ownWalk = walkTarget != null && !leader.isLeashed();
        // Not a drag: a fight's knockback (the fight owns the leader's moves), or a ride (the vehicle moves it).
        boolean free = HerdSpacing.applies(leader) && !leader.isPassenger();
        if (!group.grazePinned() && free && JourneyPolicy.dragStarts(fromCentre, radius, ownWalk, step)) {
            if (walkTarget != null) {
                // A graze move (or a need's walk) would pull against the lead.
                BrainUtils.clearMemory(leader, MemoryModuleType.WALK_TARGET);
            }
            group.startDrag(leader.getX(), leader.getY(), leader.getZ(), followersByDistance(level, group, leader), now);
            return;
        }
        if (JourneyPolicy.shouldRecentre(group.countOutside(outside))) {
            group.recentre(leader.getX(), leader.getY(), leader.getZ());
        }
        if (!ownWalk || group.grazePinned()) {
            // A leashed leader's need walk starts no urgent trip against the lead (P8c fix round 1).
            return;
        }
        Vec3 target = walkTarget.getTarget().currentPosition();
        if (!HerdArea.inside(group.centreX(), group.centreZ(), radius, target.x, target.z)) {
            // A need's walk target (water, food, a feeder) that leaves the herd area: an urgent journey.
            group.startJourney(JourneyPolicy.Kind.URGENT, target.x, target.y, target.z,
                    followersByDistance(level, group, leader), now);
        }
    }

    /**
     * Side room of a trail point on both sides, with the leader's clearance box (spec: "measured once").
     * Nothing is measured for a point without a direction (a one-point trail).
     */
    private static void measureRoom(ServerLevel level, Dinosaur leader, DinoGroup group, int index, @Nullable LeaderTrail.TrailPoint point) {
        if (point == null || point.dirX() == 0D && point.dirZ() == 0D) {
            return;
        }
        // The leader's cached box (rebuilt every BOX_REFRESH_TICKS), not a new one per trail point.
        ClearanceBox box = leader.getMoveControl() instanceof HerdMoveControl control ? control.box() : BodyFit.boxOf(leader);
        BodyFit fit = group.fit();
        float roomPos = fit.sideRoom(level, leader, box, point.x(), point.y(), point.z(), -point.dirZ(), point.dirX());
        float roomNeg = fit.sideRoom(level, leader, box, point.x(), point.y(), point.z(), point.dirZ(), -point.dirX());
        group.trail().setRoom(index, roomPos, roomNeg);
    }
}
