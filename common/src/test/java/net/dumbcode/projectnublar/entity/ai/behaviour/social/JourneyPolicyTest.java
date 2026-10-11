package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JourneyPolicyTest {

    @Test
    void grazeMovesComeEvery20To40Seconds() {
        assertEquals(1400L, JourneyPolicy.nextGrazeMove(1000L, 0D));
        long latest = JourneyPolicy.nextGrazeMove(1000L, 0.9999D);
        assertTrue(latest >= 1799L && latest <= 1800L, "was " + latest);
    }

    @Test
    void journeysComeEvery2To5Minutes() {
        assertEquals(2400L, JourneyPolicy.nextJourney(0L, 0D));
        long latest = JourneyPolicy.nextJourney(0L, 0.9999D);
        assertTrue(latest >= 5999L && latest <= 6000L, "was " + latest);
    }

    @Test
    void aStartedJourneyWaitsAFullInterval() {
        assertEquals(JourneyPolicy.nextJourney(1000L, 0.5D), JourneyPolicy.nextJourneyAfterTry(1000L, 0.5D, true));
    }

    @Test
    void aFailedJourneyTryIsRetriedSoon() {
        // Playtest 1 (R3): a try that finds no target must not cost a whole 2-5 minute interval.
        assertEquals(1000L + JourneyPolicy.JOURNEY_RETRY_TICKS, JourneyPolicy.nextJourneyAfterTry(1000L, 0.9999D, false));
        assertTrue(JourneyPolicy.JOURNEY_RETRY_TICKS > 0 && JourneyPolicy.JOURNEY_RETRY_TICKS < JourneyPolicy.JOURNEY_MIN_TICKS,
                "retry " + JourneyPolicy.JOURNEY_RETRY_TICKS + " must be sooner than the shortest interval");
    }

    @Test
    void journeyDistanceIs156To314Blocks() {
        // Lead, playtest fix P11: the P9 band (78-157) doubled.
        assertEquals(2D * 78D, JourneyPolicy.JOURNEY_MIN, 1.0E-9D);
        assertEquals(2D * 157D, JourneyPolicy.JOURNEY_MAX, 1.0E-9D);
        assertFalse(JourneyPolicy.isJourneyDistance(155.9D));
        assertTrue(JourneyPolicy.isJourneyDistance(156D));
        assertTrue(JourneyPolicy.isJourneyDistance(314D));
        assertFalse(JourneyPolicy.isJourneyDistance(314.1D));
        // A journey always leaves the herd area: 156 is more than 2R (54.9 for 20 Gallimimus at sqrt(6)).
        assertTrue(JourneyPolicy.JOURNEY_MIN > 2D * HerdArea.radius(20, 4.2D, 1.2D, HerdArea.HERD_AREA_SCALE));
        // LeaderJourney samples its targets in this band (HerdArea.sampleRing).
        for (double unit = 0.01D; unit < 1D; unit += 0.07D) {
            HerdArea.Point point = HerdArea.sampleRing(10D, -5D, JourneyPolicy.JOURNEY_MIN, JourneyPolicy.JOURNEY_MAX, unit, unit);
            assertTrue(JourneyPolicy.isJourneyDistance(Math.hypot(point.x() - 10D, point.z() + 5D)), "outside the band: " + point);
        }
    }

    @Test
    void aLegIsWithinPathReachAndLongerThanOneWalk() {
        // P11: a vanilla path reaches at most the follow range (35 blocks); a leg stays well inside it, so the
        // path can bend round obstacles. One walk (at most 249 ticks at the journey speed) ends before the leg
        // point, so legs add no stops: the leader re-issues its walk as before.
        assertTrue(JourneyPolicy.LEG_LENGTH <= 35D - 8D, "inside the path reach with room to bend");
        double attribute = net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.BASE_MOVEMENT_SPEED;
        double longestWalk = 249D * GroundSpeed.groundSpeed(JourneyPolicy.JOURNEY_SPEED, attribute);
        assertTrue(JourneyPolicy.LEG_LENGTH > longestWalk, "one walk is " + longestWalk);
    }

    @Test
    void legPointLiesAlongTheHeadingAtLegLength() {
        HerdArea.Point east = JourneyPolicy.legPoint(10D, 20D, 210D, 20D, 0);
        assertEquals(10D + JourneyPolicy.LEG_LENGTH, east.x(), 1.0E-9D);
        assertEquals(20D, east.z(), 1.0E-9D);
        // A 3-4-5 heading.
        HerdArea.Point slanted = JourneyPolicy.legPoint(0D, 0D, 120D, 160D, 0);
        assertEquals(0.6D * JourneyPolicy.LEG_LENGTH, slanted.x(), 1.0E-9D);
        assertEquals(0.8D * JourneyPolicy.LEG_LENGTH, slanted.z(), 1.0E-9D);
        // A target nearer than one leg: the straight point is the target itself, exactly.
        HerdArea.Point near = JourneyPolicy.legPoint(0D, 0D, 6.3D, 8.7D, 0);
        assertEquals(6.3D, near.x(), 0D);
        assertEquals(8.7D, near.z(), 0D);
        // An offset try turns the heading, at the same distance: +20 degrees for try 1.
        double offset = Math.toRadians(JourneyPolicy.LEG_OFFSET_DEGREES);
        HerdArea.Point turned = JourneyPolicy.legPoint(0D, 0D, 100D, 0D, 1);
        assertEquals(Math.cos(offset) * JourneyPolicy.LEG_LENGTH, turned.x(), 1.0E-9D);
        assertEquals(Math.sin(offset) * JourneyPolicy.LEG_LENGTH, turned.z(), 1.0E-9D);
        // Near the target, an offset try turns at the target's distance (10), not onto the target.
        HerdArea.Point nearTurned = JourneyPolicy.legPoint(0D, 0D, 10D, 0D, 2);
        assertEquals(Math.cos(offset) * 10D, nearTurned.x(), 1.0E-9D);
        assertEquals(-Math.sin(offset) * 10D, nearTurned.z(), 1.0E-9D);
    }

    @Test
    void aPickedHeadingWithAnObstacleStraightAheadIsRescuedByAnOffset() {
        // Fix round 1: the journey pick uses the same leg choice as every leg. A tree or pond 24 blocks straight
        // ahead (z = 0) does not throw the heading away when the +20 degree point is usable.
        JourneyPolicy.Leg first = JourneyPolicy.chooseLeg(0D, 64D, 0D, JourneyPolicy.JOURNEY_MIN, 0D, Double.NaN,
                (x, z) -> Math.abs(z) < 1.0E-6D ? Double.NaN : 64D);
        assertNotNull(first, "an offset point is usable");
        assertFalse(first.last(), "a picked target is farther than one leg");
        assertTrue(first.z() > 0D);
    }

    @Test
    void legOffsetsAreSmallAndAlternateSides() {
        double step = Math.toRadians(JourneyPolicy.LEG_OFFSET_DEGREES);
        assertEquals(1 + 2 * JourneyPolicy.LEG_OFFSET_PAIRS, JourneyPolicy.LEG_TRIES);
        assertEquals(0, Double.compare(0D, JourneyPolicy.legOffset(0)), "straight ahead is +0.0, not -0.0");
        assertEquals(step, JourneyPolicy.legOffset(1), 1.0E-9D);
        assertEquals(-step, JourneyPolicy.legOffset(2), 1.0E-9D);
        assertEquals(2D * step, JourneyPolicy.legOffset(3), 1.0E-9D);
        assertEquals(-2D * step, JourneyPolicy.legOffset(4), 1.0E-9D);
        assertTrue(JourneyPolicy.LEG_OFFSET_PAIRS * JourneyPolicy.LEG_OFFSET_DEGREES <= 45D, "small: the leader still heads for the target");
    }

    @Test
    void chooseLegTakesThePointAlongTheHeadingWhenItIsUsable() {
        JourneyPolicy.Leg leg = JourneyPolicy.chooseLeg(0D, 64D, 0D, 200D, 0D, Double.NaN, (x, z) -> 70D);
        assertNotNull(leg);
        assertEquals(JourneyPolicy.LEG_LENGTH, leg.x(), 1.0E-9D);
        assertEquals(70D, leg.y(), 1.0E-9D);
        assertEquals(0D, leg.z(), 1.0E-9D);
        assertFalse(leg.last(), "the target is farther than one leg");
    }

    @Test
    void chooseLegTriesTheOffsetsWhenThePointAlongTheHeadingIsNotUsable() {
        // Water straight ahead (z = 0): the first offset (+20 degrees, z > 0) is used.
        JourneyPolicy.Leg leg = JourneyPolicy.chooseLeg(0D, 64D, 0D, 200D, 0D, Double.NaN,
                (x, z) -> Math.abs(z) < 1.0E-6D ? Double.NaN : 65D);
        assertNotNull(leg);
        double offset = JourneyPolicy.legOffset(1);
        assertEquals(Math.cos(offset) * JourneyPolicy.LEG_LENGTH, leg.x(), 1.0E-9D);
        assertEquals(Math.sin(offset) * JourneyPolicy.LEG_LENGTH, leg.z(), 1.0E-9D);
        // Only the far side of the last offset is usable.
        double last = JourneyPolicy.legOffset(JourneyPolicy.LEG_TRIES - 1);
        JourneyPolicy.Leg lastLeg = JourneyPolicy.chooseLeg(0D, 64D, 0D, 200D, 0D, Double.NaN,
                (x, z) -> z < Math.sin(last) * JourneyPolicy.LEG_LENGTH + 1.0E-6D ? 65D : Double.NaN);
        assertNotNull(lastLeg);
        assertTrue(lastLeg.z() < 0D);
    }

    @Test
    void noUsableLegGivesNoLeg() {
        // P11: the caller then ends the journey where the leader is, as an arrival.
        int[] probes = {0};
        assertNull(JourneyPolicy.chooseLeg(0D, 64D, 0D, 200D, 0D, Double.NaN, (x, z) -> {
            probes[0]++;
            return Double.NaN;
        }));
        assertEquals(JourneyPolicy.LEG_TRIES, probes[0], "every offset tried once");
    }

    @Test
    void theLastLegGoesToTheResolvedTarget() {
        // The target's chunk is loaded and usable: the last leg is the target, at its ground height.
        JourneyPolicy.Leg leg = JourneyPolicy.chooseLeg(0D, 64D, 0D, 12D, 5D, Double.NaN, (x, z) -> 66D);
        assertNotNull(leg);
        assertTrue(leg.last());
        assertEquals(12D, leg.x(), 1.0E-9D);
        assertEquals(66D, leg.y(), 1.0E-9D);
        assertEquals(5D, leg.z(), 1.0E-9D);
    }

    @Test
    void aKnownTargetIsWalkedToWithoutAProbe() {
        // A target whose height is known (a resolved last leg, or a GameTest goal) is the walk target as it is:
        // no block read, and no legs in between (the vanilla path walks toward it, as before P11).
        JourneyPolicy.StandHeight noProbe = (x, z) -> {
            throw new AssertionError("no probe for a known target");
        };
        JourneyPolicy.Leg leg = JourneyPolicy.chooseLeg(0D, 64D, 0D, 12D, 5D, 63D, noProbe);
        assertNotNull(leg);
        assertTrue(leg.last());
        assertEquals(12D, leg.x(), 1.0E-9D);
        assertEquals(63D, leg.y(), 1.0E-9D);
        assertEquals(5D, leg.z(), 1.0E-9D);
        JourneyPolicy.Leg far = JourneyPolicy.chooseLeg(0D, 64D, 0D, 50D, 0D, 64D, noProbe);
        assertNotNull(far);
        assertEquals(50D, far.x(), 1.0E-9D);
        assertEquals(64D, far.y(), 1.0E-9D);
    }

    @Test
    void anUnusableLastTargetUsesAnOffsetAsTheLastLeg() {
        // The target is in water: an offset point at the same distance is the last leg (it becomes the target).
        JourneyPolicy.Leg leg = JourneyPolicy.chooseLeg(0D, 64D, 0D, 10D, 0D, Double.NaN,
                (x, z) -> Math.abs(z) < 1.0E-6D ? Double.NaN : 64D);
        assertNotNull(leg);
        assertTrue(leg.last());
        assertEquals(10D, Math.hypot(leg.x(), leg.z()), 1.0E-9D);
    }

    @Test
    void aJourneyPointMayBeAtMost8BlocksAboveOrBelowTheLeader() {
        // Lead ruling 14 (P12c): cliff tops and roofs are not journey points.
        assertEquals(8D, JourneyPolicy.JOURNEY_MAX_HEIGHT_DIFF, 1.0E-9D);
        assertTrue(JourneyPolicy.withinJourneyHeight(72D, 64D));
        assertTrue(JourneyPolicy.withinJourneyHeight(56D, 64D));
        assertFalse(JourneyPolicy.withinJourneyHeight(72.25D, 64D));
        assertFalse(JourneyPolicy.withinJourneyHeight(55.75D, 64D));
    }

    @Test
    void aLegPointTooHighOrTooLowIsRescuedByAnOffsetLikeAnUnusablePoint() {
        // Straight ahead (z = 0) is a cliff top 9 blocks up: the +20 degree point, 8 up, is the leg.
        JourneyPolicy.Leg leg = JourneyPolicy.chooseLeg(0D, 64D, 0D, 200D, 0D, Double.NaN,
                (x, z) -> Math.abs(z) < 1.0E-6D ? 73D : 72D);
        assertNotNull(leg);
        assertEquals(72D, leg.y(), 1.0E-9D);
        assertTrue(leg.z() > 0D);
        // Every point is 9 blocks down: no leg, after one probe per try (the normal failure path).
        int[] probes = {0};
        assertNull(JourneyPolicy.chooseLeg(0D, 64D, 0D, 200D, 0D, Double.NaN, (x, z) -> {
            probes[0]++;
            return 55D;
        }));
        assertEquals(JourneyPolicy.LEG_TRIES, probes[0]);
        // The last leg resolves the target the same way: a target on a roof is not the journey's end point.
        JourneyPolicy.Leg last = JourneyPolicy.chooseLeg(0D, 64D, 0D, 10D, 0D, Double.NaN,
                (x, z) -> Math.abs(z) < 1.0E-6D ? 80D : 64D);
        assertNotNull(last);
        assertTrue(last.last());
        assertTrue(Math.abs(last.z()) > 1.0E-6D, "an offset point at the same distance");
    }

    @Test
    void arrivesWithin3BlocksOrWhenThePathEndsWithoutProgress() {
        assertTrue(JourneyPolicy.arrived(3D, false));
        assertFalse(JourneyPolicy.arrived(3.1D, false));
        assertTrue(JourneyPolicy.arrived(10D, true));
    }

    @Test
    void urgentTripsNeverWait() {
        assertSame(JourneyPolicy.Gathering.GO, JourneyPolicy.gathering(JourneyPolicy.Kind.URGENT, false, 0));
    }

    @Test
    void roamingSlowsForTheWholeWaitAndNeverStops() {
        // P13a item 6 (ruling 3a): the straggler wait is a slow walk for the whole wait, no full stop.
        assertSame(JourneyPolicy.Gathering.SLOW, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, false, 0));
        assertSame(JourneyPolicy.Gathering.SLOW, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, false, 40));
        assertSame(JourneyPolicy.Gathering.SLOW, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, false, JourneyPolicy.MAX_WAIT_TICKS - 1));
        assertEquals(2, JourneyPolicy.Gathering.values().length, "GO and SLOW only");
    }

    @Test
    void theSlowWalkIsTheLeadsSpeed() {
        // Lead ruling (P13a, mid-task): 0.6, so 0.135 x 0.6^2 = about 0.049 blocks per tick.
        assertEquals(0.6F, JourneyPolicy.WAIT_SLOW_SPEED, 1.0E-6F);
    }

    @Test
    void slowedLeaderStillPlaysTheWalkAnimation() {
        // Below GroundSpeed.MIN_WALK_MODIFIER the body glides in the idle pose.
        assertTrue(JourneyPolicy.WAIT_SLOW_SPEED >= GroundSpeed.MIN_WALK_MODIFIER,
                "WAIT_SLOW_SPEED " + JourneyPolicy.WAIT_SLOW_SPEED + " is under the walk floor " + GroundSpeed.MIN_WALK_MODIFIER);
        assertTrue(JourneyPolicy.WAIT_SLOW_SPEED < JourneyPolicy.JOURNEY_SPEED, "slow must be slower than the journey");
    }

    @Test
    void gatheredHerdGoes() {
        assertSame(JourneyPolicy.Gathering.GO, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, true, 50));
    }

    @Test
    void waitingEndsAfterTenSeconds() {
        assertSame(JourneyPolicy.Gathering.SLOW, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, false, 199));
        assertSame(JourneyPolicy.Gathering.GO, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, false, 200));
    }

    @Test
    void gatheredMeansThreeQuarters() {
        assertTrue(JourneyPolicy.isGathered(3, 4));
        assertFalse(JourneyPolicy.isGathered(2, 4));
        assertTrue(JourneyPolicy.isGathered(0, 0), "nobody to wait for");
    }

    /** The trail's first point: a journey's trail starts at arc 0. */
    private static final double TRAIL_TAIL = 0D;
    /** Open and tight travel spacing of a Gallimimus (personal space 2.2 + 2 and + 1). */
    private static final double OPEN = 4.2D;
    private static final double TIGHT = 3.2D;
    /** How far a follower in its slot stands behind it: 1.06 lag at journey speed + the leader 1 block past the trail head. */
    private static final double IN_SLOT_LAG = 2.06D;

    /**
     * Lays out {@code followers} behind a trail head at {@code head} in open ground and tallies them. Each
     * follower stands {@link #IN_SLOT_LAG} plus {@code extraBehind[i]} behind its slot (straight trail).
     */
    private static JourneyPolicy.Tally tallyOf(double head, int followers, double[] extraBehind) {
        double[] arcs = new double[followers];
        double[] offsets = new double[followers];
        TravelOrder.layout(head, followers, OPEN, TIGHT, arc -> 8D, arcs, offsets);
        JourneyPolicy.Tally tally = new JourneyPolicy.Tally();
        for (int i = 0; i < followers; i++) {
            double depth = head - arcs[i];
            double slot = Math.sqrt(depth * depth + offsets[i] * offsets[i]);
            double behind = depth + IN_SLOT_LAG + extraBehind[i];
            tally.add(Math.sqrt(behind * behind + offsets[i] * offsets[i]), slot, arcs[i], TRAIL_TAIL);
        }
        return tally;
    }

    @Test
    void followerAtItsSlotIsClose() {
        // Playtest 1 (D): the tail of a 20-animal column sits about 30 behind the leader, beyond the old 2R.
        assertTrue(JourneyPolicy.followerClose(29.4D, 29.4D));
        assertTrue(JourneyPolicy.followerClose(29.4D + JourneyPolicy.GATHER_MARGIN, 29.4D));
        assertFalse(JourneyPolicy.followerClose(29.4D + JourneyPolicy.GATHER_MARGIN + 0.1D, 29.4D));
    }

    @Test
    void followerAWholeRowBehindItsSlotIsAStraggler() {
        // A row behind (open spacing 4.2), plus the lag of a follower in its slot.
        double slot = 12.6D;
        assertFalse(JourneyPolicy.followerClose(slot + OPEN + IN_SLOT_LAG, slot));
    }

    @Test
    void aSlotIsOnTheTrailFromTheTrailsFirstPoint() {
        assertFalse(JourneyPolicy.slotOnTrail(-9.4D, TRAIL_TAIL), "journey start: the slot lies behind the trail");
        assertTrue(JourneyPolicy.slotOnTrail(TRAIL_TAIL, TRAIL_TAIL));
        assertTrue(JourneyPolicy.slotOnTrail(0.8D, TRAIL_TAIL));
        assertFalse(JourneyPolicy.slotOnTrail(Double.NaN, TRAIL_TAIL), "no slot");
        assertFalse(JourneyPolicy.slotOnTrail(5D, Double.NaN), "empty trail");
    }

    @Test
    void aHerdWithNoSlotOnTheTrailIsGathered() {
        // Journey start, nothing walked yet: every slot lies behind the trail, so nobody is counted and the
        // leader goes on (no deadlock: standing would stop the trail from growing).
        double[] extra = new double[19];
        java.util.Arrays.fill(extra, 30D);
        assertTrue(tallyOf(0D, 19, extra).gathered());
    }

    @Test
    void earlyFrontRowStragglersOfTwentyAreWaitedFor() {
        // The leader has walked 9 blocks: rows 0 (arc 4.8) and 1 (arc 0.6) are on the trail, 5 followers; the
        // other 14 slots are not. Two of the five are 20 blocks behind their slots: 3 of 5 close, under 75%.
        // Counting the 14 off-trail followers as close (fix round 1) gave 17 of 19 and no wait.
        double[] extra = new double[19];
        extra[1] = 20D;
        extra[3] = 20D;
        assertFalse(tallyOf(9D, 19, extra).gathered());
        assertTrue(tallyOf(9D, 19, new double[19]).gathered(), "all five in their slots");
        double[] farBehindOffTrail = new double[19];
        java.util.Arrays.fill(farBehindOffTrail, 5, 19, 30D);
        assertTrue(tallyOf(9D, 19, farBehindOffTrail).gathered(), "off-trail followers do not count");
    }

    @Test
    void earlyStragglerOfFiveIsWaitedFor() {
        // A herd of 5 after 6 blocks: the first row (2 followers, arc 1.8) is on the trail, the second (arc
        // -2.4) is not. One first-row follower 20 blocks behind its slot: 1 of 2 close.
        double[] extra = {0D, 20D, 0D, 0D};
        assertFalse(tallyOf(6D, 4, extra).gathered());
        assertTrue(tallyOf(6D, 4, new double[4]).gathered());
    }

    @Test
    void followersWithoutASlotOrFarAwayAreNotCounted() {
        JourneyPolicy.Tally tally = new JourneyPolicy.Tally();
        tally.add(40D, Double.NaN, Double.NaN, TRAIL_TAIL);
        tally.add(JourneyPolicy.STRAGGLER_IGNORE_DISTANCE + 1D, 4.2D, 10D, TRAIL_TAIL);
        assertTrue(tally.gathered(), "no slot, and lost beyond 48 blocks: nobody counted");
        tally.add(20D, 4.2D, 10D, TRAIL_TAIL);
        assertFalse(tally.gathered(), "one counted straggler");
    }

    @Test
    void aCorrectOpenWedgeOfTwentyIsGathered() {
        // 19 followers at the open spacing 4.2 (tight 3.2) in open ground: 7 rows, 29.4 deep, all on the trail,
        // each in its slot. The old rule (75% within 2R = 22.4) counted only 11 of 19 close.
        assertTrue(tallyOf(100D, 19, new double[19]).gathered());
    }

    @Test
    void recentreAfterFiveSecondsOutside() {
        assertFalse(JourneyPolicy.shouldRecentre(99));
        assertTrue(JourneyPolicy.shouldRecentre(100));
    }

    @Test
    void aLeaderMovedOutOfTheAreaWithoutAWalkOfItsOwnStartsADrag() {
        // Playtest 2 (R7): a leader dragged on a lead left R about every 156 ticks and the herd walked straight
        // at a jumping centre. Now the move itself starts an urgent journey, so the herd follows the trail.
        double radius = 27.4D; // 20 Gallimimus at the herd area scale (P11)
        double beyond = radius + JourneyPolicy.DRAG_EDGE_MARGIN + 0.1D;
        assertTrue(JourneyPolicy.dragStarts(beyond, radius, false, 0.2D), "dragged at a walking player's speed");
        assertTrue(JourneyPolicy.dragStarts(beyond, radius, false, JourneyPolicy.DRAG_MIN_STEP));
        assertTrue(JourneyPolicy.dragStarts(radius + 6D, radius, false, 6D), "a short teleport is one big step");
        assertFalse(JourneyPolicy.dragStarts(radius - 1D, radius, false, 0.2D), "inside the herd area");
        assertFalse(JourneyPolicy.dragStarts(beyond, radius, true, 0.2D), "its own walk: decision 16 or the recentre rule");
        assertFalse(JourneyPolicy.dragStarts(beyond, radius, false, 0D), "standing outside: the recentre rule");
        assertFalse(JourneyPolicy.dragStarts(beyond, radius, false, JourneyPolicy.DRAG_MIN_STEP * 0.5D), "jitter");
    }

    @Test
    void aGrazeMoveThatEndsJustOutsideTheAreaIsNoDrag() {
        // P8c fix round 1: the brain erases the walk target when a graze move ends, while the leader still has
        // a stride of momentum (0.086 blocks per tick); spots have no edge margin, so it can stop just outside R.
        double radius = 27.4D; // 20 Gallimimus at the herd area scale (P11)
        assertFalse(JourneyPolicy.dragStarts(radius + 0.5D, radius, false, 0.086D), "momentum after a graze move");
        assertFalse(JourneyPolicy.dragStarts(radius + JourneyPolicy.DRAG_EDGE_MARGIN, radius, false, 0.086D), "at the margin");
        assertTrue(JourneyPolicy.dragStarts(radius + JourneyPolicy.DRAG_EDGE_MARGIN + 0.01D, radius, false, 0.086D));
        assertTrue(JourneyPolicy.DRAG_EDGE_MARGIN > 0.086D + 0.05D + 0.03D, "more than a stride plus the momentum after it");
    }

    @Test
    void dragStepIsAboveTheWalkingThresholdAndBelowTheSlowestDrag() {
        // Within 6 blocks the lead moves the mob by its own navigation at followLeashSpeed (vanilla 1.0), so the
        // slowest drag is the leader's own walk on a lead: 0.135 blocks per tick at the base attribute. Farther
        // out the lead pulls by velocity, faster still.
        double attribute = net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.BASE_MOVEMENT_SPEED;
        double leashWalk = GroundSpeed.groundSpeed(1D, attribute);
        assertTrue(JourneyPolicy.DRAG_MIN_STEP > HerdBodies.WALKING_SPEED, "above the herd's own walking threshold");
        assertTrue(JourneyPolicy.DRAG_MIN_STEP < leashWalk, "below the leader's walk on a lead " + leashWalk);
    }

    @Test
    void aDragEndsWhenTheLeaderHasStoodStillForTheSettleTime() {
        assertFalse(JourneyPolicy.dragSettled(JourneyPolicy.DRAG_SETTLE_TICKS - 1));
        assertTrue(JourneyPolicy.dragSettled(JourneyPolicy.DRAG_SETTLE_TICKS));
    }

    @Test
    void standingStillAllowsSmallShovesButNotATrailStep() {
        assertFalse(JourneyPolicy.leftStillSpot(0.6D, 0.6D), "0.85 blocks: a shove from a herd mate");
        assertTrue(JourneyPolicy.leftStillSpot(1.0D, 0.5D), "1.1 blocks: a new trail point");
        assertTrue(JourneyPolicy.DRAG_STILL_RADIUS <= LeaderTrail.SPACING,
                "a still leader moves less than one trail spacing");
    }

    // --- P13a: journeys pause, night, start rule, need detours, logs ---

    @Test
    void nightIsTheSameTestAsRestFromSunsetUntilMorning() {
        // Rest: dayTime % 24000 > 12000 is night.
        assertFalse(JourneyPolicy.isNight(0L));
        assertFalse(JourneyPolicy.isNight(JourneyPolicy.SUNSET_DAY_TIME));
        assertTrue(JourneyPolicy.isNight(JourneyPolicy.SUNSET_DAY_TIME + 1));
        assertTrue(JourneyPolicy.isNight(23999L));
        assertFalse(JourneyPolicy.isNight(24000L), "morning of the next day");
        assertTrue(JourneyPolicy.isNight(24000L + 13000L));
    }

    @Test
    void restHoursAreTheNightForADayAnimalAndTheDayForANightAnimal() {
        assertTrue(JourneyPolicy.restHours(13000L, false));
        assertFalse(JourneyPolicy.restHours(1000L, false));
        assertTrue(JourneyPolicy.restHours(1000L, true));
        assertFalse(JourneyPolicy.restHours(13000L, true));
    }

    @Test
    void aRoamingJourneyStartsOnlyInTheDayWithThreeQuartersStamina() {
        // Ruling Q3: at least 75% of the maximum (150 of 200 for Gallimimus); ruling Q2: none from sunset.
        assertEquals(0.75D, JourneyPolicy.JOURNEY_MIN_STAMINA_FRACTION, 1.0E-9D);
        assertTrue(JourneyPolicy.roamMayStart(false, 150F, 200F));
        assertTrue(JourneyPolicy.roamMayStart(false, 200F, 200F));
        assertFalse(JourneyPolicy.roamMayStart(false, 149.9F, 200F), "a failed try, retried as usual");
        assertFalse(JourneyPolicy.roamMayStart(true, 200F, 200F), "not in the rest hours");
    }

    @Test
    void aDownLeaderPausesTheJourney() {
        assertSame(JourneyPolicy.Pause.NONE, JourneyPolicy.pause(false));
        assertSame(JourneyPolicy.Pause.REST, JourneyPolicy.pause(true));
        assertEquals(2, JourneyPolicy.Pause.values().length, "NONE and REST only (item 5 removed)");
    }

    @Test
    void theHerdSpreadsOutAtNightOrWhenTheRestPassesTwentySeconds() {
        // Ruling Q1: a daytime pause up to 400 ticks keeps every slot.
        assertEquals(400, JourneyPolicy.SHORT_REST_PAUSE_TICKS);
        assertFalse(JourneyPolicy.herdSpreads(0, false, false));
        assertFalse(JourneyPolicy.herdSpreads(JourneyPolicy.SHORT_REST_PAUSE_TICKS, false, false));
        assertTrue(JourneyPolicy.herdSpreads(JourneyPolicy.SHORT_REST_PAUSE_TICKS + 1, false, false));
        assertTrue(JourneyPolicy.herdSpreads(0, true, false), "at night at once");
    }

    @Test
    void noSpreadStartsWhileTheLeaderGetsUp() {
        // Fix round 1, item 2: a ~400-tick rest passes the short pause during the 69-tick get-up.
        assertFalse(JourneyPolicy.herdSpreads(JourneyPolicy.SHORT_REST_PAUSE_TICKS + 1, false, true));
        assertFalse(JourneyPolicy.herdSpreads(0, true, true), "not even in the rest hours");
    }

    @Test
    void aSpreadMemberLiesDownAtItsSpotAtNightOrWhenItHasStaminaToGetBack() {
        assertTrue(JourneyPolicy.sleepsAtSpot(true, false, 200F, 200F), "the herd sleeps at night, tired or not");
        assertTrue(JourneyPolicy.sleepsAtSpot(false, false, 150F, 200F));
        assertFalse(JourneyPolicy.sleepsAtSpot(false, false, 200F, 200F), "a daytime rest at full stamina would end at once");
    }

    @Test
    void aSpreadMemberDoesNotLieDownOnceTheLeaderGetsUp() {
        assertFalse(JourneyPolicy.sleepsAtSpot(true, true, 100F, 200F));
        assertFalse(JourneyPolicy.sleepsAtSpot(false, true, 100F, 200F));
        assertTrue(JourneyPolicy.sleepsAtSpot(false, false, 100F, 200F));
    }

    @Test
    void aJourneyEndDescribesItsReasonDistancesAndTime() {
        JourneyPolicy.JourneyEnd end = new JourneyPolicy.JourneyEnd(new java.util.UUID(0L, 7L), JourneyPolicy.End.NO_PROGRESS,
                JourneyPolicy.Kind.ROAM, 154.24D, 52.0D, 60.31D, 1308L);
        assertEquals("ROAM journey ended NO_PROGRESS: 154.2 blocks from the target, 52.0 from the start, walked 60.3, 1308 ticks",
                end.describe());
    }

    @Test
    void theEndReasonOfAnArrivalSaysWhyTheWalkStopped() {
        assertSame(JourneyPolicy.End.ARRIVED, JourneyPolicy.endReason(3D, JourneyPolicy.Kind.ROAM, false));
        assertSame(JourneyPolicy.End.ARRIVED, JourneyPolicy.endReason(1D, JourneyPolicy.Kind.URGENT, false));
        assertSame(JourneyPolicy.End.URGENT_WALK_END, JourneyPolicy.endReason(10D, JourneyPolicy.Kind.URGENT, false));
        assertSame(JourneyPolicy.End.NO_LEG, JourneyPolicy.endReason(10D, JourneyPolicy.Kind.ROAM, true));
        assertSame(JourneyPolicy.End.NO_PROGRESS, JourneyPolicy.endReason(10D, JourneyPolicy.Kind.ROAM, false));
    }

    @Test
    void theTallyCountsCountedAndCloseFollowers() {
        JourneyPolicy.Tally tally = new JourneyPolicy.Tally();
        tally.add(5D, 4D, 10D, 0D);
        tally.add(30D, 4D, 10D, 0D);
        tally.add(5D, 4D, Double.NaN, 0D);
        assertEquals(2, tally.counted());
        assertEquals(1, tally.close());
    }
}
