package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DinoGroupTest {

    private static final double EPSILON = 1.0E-9D;

    private final Map<UUID, Float> present = new HashMap<>();
    private final DinoGroup.Roster roster = new DinoGroup.Roster() {
        @Override
        public boolean isPresent(UUID member) {
            return present.containsKey(member);
        }

        @Override
        public float dominance(UUID member) {
            return present.get(member);
        }
    };

    private UUID animal(long id, float dominance) {
        UUID uuid = new UUID(0L, id);
        this.present.put(uuid, dominance);
        return uuid;
    }

    @Test
    void newGroupGrazes() {
        assertSame(HerdState.GRAZE, new DinoGroup(5).state());
    }

    @Test
    void leaderChangeClearsTheTrail() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID next = animal(2, 0.8F);
        group.add(leader, this.roster);
        group.add(next, this.roster);
        group.trail().record(0, 64, 0);
        group.trail().record(5, 64, 0);
        this.present.remove(leader);
        group.leader(this.roster);
        assertEquals(0, group.trail().size());
    }

    @Test
    void startJourneyEntersTravelAndClearsTrailAndSpots() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID follower = animal(2, 0.5F);
        group.add(leader, this.roster);
        group.add(follower, this.roster);
        group.trail().record(0, 64, 0);
        group.trail().record(5, 64, 0);
        group.reserveSpot(follower, 3D, 3D);
        assertTrue(group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(follower), 10L));
        assertSame(HerdState.TRAVEL, group.state());
        assertTrue(group.journeyActive());
        assertEquals(0, group.trail().size());
        assertNull(group.spotOf(follower));
        assertEquals(List.of(follower), group.travelOrder());
    }

    @Test
    void eachStartedJourneyCountsOnceAndARefusedOneDoesNot() {
        DinoGroup group = new DinoGroup(5);
        assertEquals(0, group.journeys());
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 0L);
        group.endJourney(0D, 64D, 0D);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 10L);
        assertEquals(2, group.journeys());
        group.pinGraze(true);
        assertFalse(group.startJourney(JourneyPolicy.Kind.URGENT, 40D, 64D, 0D, List.of(), 20L));
        assertEquals(2, group.journeys());
    }

    @Test
    void pinnedHerdRefusesJourneys() {
        DinoGroup group = new DinoGroup(5);
        group.pinGraze(true);
        assertFalse(group.startJourney(JourneyPolicy.Kind.URGENT, 40D, 64D, 0D, List.of(), 10L));
        assertSame(HerdState.GRAZE, group.state());
    }

    @Test
    void arriveSettlesThenEndJourneyGrazesAndRecentres() {
        DinoGroup group = new DinoGroup(5);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 0L);
        group.arrive(100L);
        assertTrue(group.settling());
        assertFalse(group.settleDone(100L + JourneyPolicy.SETTLE_TICKS - 1));
        assertTrue(group.settleDone(100L + JourneyPolicy.SETTLE_TICKS));
        group.endJourney(5D, 64D, 7D);
        assertSame(HerdState.GRAZE, group.state());
        assertFalse(group.journeyActive());
        assertEquals(5D, group.centreX(), EPSILON);
        assertEquals(7D, group.centreZ(), EPSILON);
    }

    @Test
    void centreKeepsTheGroundHeightWhereTheLeaderStood() {
        // Playtest 2 fix P8a: a member walking back into the herd area paths to the centre at the centre's own
        // height, not at the leader's current one (a dragged leader may stand far above or below it).
        DinoGroup group = new DinoGroup(5);
        group.recentre(3D, 70D, 4D);
        assertEquals(70D, group.centreY(), EPSILON);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 0L);
        group.endJourney(40D, 61.5D, 2D);
        assertEquals(61.5D, group.centreY(), EPSILON);
    }

    @Test
    void leaderChangeEndsTheJourney() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID follower = animal(2, 0.5F);
        group.add(leader, this.roster);
        group.add(follower, this.roster);
        group.recentre(0D, 64D, 0D);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(follower), 0L);
        this.present.remove(leader);
        group.remove(leader);
        assertSame(HerdState.GRAZE, group.state());
        assertFalse(group.journeyActive());
        assertFalse(group.hasCentre(), "the old centre is dropped; the new leader recentres on itself");
    }

    @Test
    void formingAGroupIsNoLeaderChange() {
        DinoGroup group = new DinoGroup(5);
        group.add(animal(1, 0.9F), this.roster);
        group.add(animal(2, 0.5F), this.roster);
        assertFalse(group.leaderChanged(), "the first leader has no walk from an earlier role to drop");
        assertTrue(group.deposedLeaders().isEmpty());
    }

    @Test
    void aStrongerNewcomerDeposesTheLeaderUntilSettled() {
        DinoGroup group = new DinoGroup(5);
        UUID first = animal(1, 0.5F);
        group.add(first, this.roster);
        group.add(animal(2, 0.4F), this.roster);
        group.add(animal(3, 0.9F), this.roster);
        assertTrue(group.leaderChanged());
        assertEquals(List.of(first), group.deposedLeaders());
        group.leaderChangeSettled();
        assertFalse(group.leaderChanged());
        assertTrue(group.deposedLeaders().isEmpty());
    }

    @Test
    void aWonChallengeDeposesTheLeader() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID challenger = animal(2, 0.5F);
        group.add(leader, this.roster);
        group.add(challenger, this.roster);
        this.present.put(challenger, 1.5F);
        assertTrue(group.challenge(challenger, 0.1F, this.roster));
        assertTrue(group.leaderChanged());
        assertEquals(List.of(leader), group.deposedLeaders());
    }

    @Test
    void reElectionAfterTheLeaderUnloadsIsALeaderChange() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        group.add(leader, this.roster);
        group.add(animal(2, 0.5F), this.roster);
        this.present.remove(leader);
        group.leader(this.roster);
        assertTrue(group.leaderChanged());
        assertEquals(List.of(leader), group.deposedLeaders(), "the old leader's walk is dropped when it loads in range");
    }

    @Test
    void scheduleLineShowsTicksUntilTheLeadersNextMoves() {
        DinoGroup group = new DinoGroup(5);
        assertEquals("Leader schedule: next graze move not scheduled, next journey not scheduled", group.describeSchedule(100L));
        group.setNextGrazeMoveTick(400L);
        group.setNextJourneyTick(90L);
        assertEquals("Leader schedule: next graze move in 300 ticks, next journey in -10 ticks", group.describeSchedule(100L));
    }

    @Test
    void thePathEndingTwiceInOnePlaceEndsTheJourney() {
        DinoGroup group = new DinoGroup(5);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 0L);
        assertTrue(group.pathEnded(0D, 0D), "the first end re-sets the walk target");
        assertTrue(group.pathEnded(5D, 0D), "the leader got on: again");
        assertFalse(group.pathEnded(5.2D, 0D), "no progress since the last end: arrival");
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 10L);
        assertTrue(group.pathEnded(5.2D, 0D), "a new journey starts afresh");
    }

    @Test
    void joinDuringTravelMarksTheOrderDirty() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        group.add(leader, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 0L);
        assertFalse(group.orderDirty());
        group.add(animal(2, 0.5F), this.roster);
        assertTrue(group.orderDirty());
    }

    @Test
    void leaveDropsSpotAndOrderPlace() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID b = animal(2, 0.5F);
        UUID c = animal(3, 0.4F);
        group.add(leader, this.roster);
        group.add(b, this.roster);
        group.add(c, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(b, c), 0L);
        group.reserveSpot(b, 1D, 1D);
        group.remove(b);
        assertNull(group.spotOf(b));
        assertEquals(List.of(c), group.travelOrder());
        assertTrue(group.orderDirty());
    }

    @Test
    void rankIsLeaderThenTravelOrder() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID b = animal(2, 0.5F);
        UUID c = animal(3, 0.4F);
        group.add(leader, this.roster);
        group.add(b, this.roster);
        group.add(c, this.roster);
        group.setTravelOrder(List.of(c, b));
        assertEquals(0, group.rank(leader));
        assertEquals(1, group.rank(c));
        assertEquals(2, group.rank(b));
        assertEquals(Integer.MAX_VALUE, group.rank(new UUID(0L, 99L)));
    }

    @Test
    void layoutGivesEachFollowerItsOwnTarget() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID b = animal(2, 0.5F);
        UUID c = animal(3, 0.4F);
        UUID d = animal(4, 0.3F);
        group.add(leader, this.roster);
        group.add(b, this.roster);
        group.add(c, this.roster);
        group.add(d, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(b, c, d), 0L);
        for (int x = 0; x <= 20; x++) {
            group.trail().record(x, 64D, 0D);
        }
        group.refreshLayout(1L, 5D, 4D);
        UUID[] followers = {b, c, d};
        for (int i = 0; i < followers.length; i++) {
            assertFalse(Double.isNaN(group.targetArcOf(followers[i])));
            for (int j = i + 1; j < followers.length; j++) {
                assertFalse(group.targetArcOf(followers[i]) == group.targetArcOf(followers[j])
                        && group.laneOffsetOf(followers[i]) == group.laneOffsetOf(followers[j]), "two followers share a target");
            }
        }
        assertTrue(Double.isNaN(group.targetArcOf(leader)), "the leader has no slot");
    }

    @Test
    void slotDistanceComesFromTheLatestLayout() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID b = animal(2, 0.5F);
        UUID c = animal(3, 0.4F);
        UUID d = animal(4, 0.3F);
        group.add(leader, this.roster);
        group.add(b, this.roster);
        group.add(c, this.roster);
        group.add(d, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(b, c, d), 0L);
        assertTrue(Double.isNaN(group.slotDistanceOf(b)), "no layout yet");
        for (int x = 0; x <= 20; x++) {
            int index = group.trail().record(x, 64D, 0D);
            group.trail().setRoom(index, 10F, 10F);
        }
        // Open ground: the first row of 2 at arc 15, lanes 2.5 either side; the next row (d) at arc 10.
        group.refreshLayout(1L, 5D, 4D);
        assertEquals(Math.sqrt(5D * 5D + 2.5D * 2.5D), group.slotDistanceOf(b), EPSILON);
        assertEquals(Math.sqrt(5D * 5D + 2.5D * 2.5D), group.slotDistanceOf(c), EPSILON);
        assertEquals(10D, group.slotDistanceOf(d), EPSILON);
        assertTrue(Double.isNaN(group.slotDistanceOf(leader)), "the leader has no slot");
    }

    @Test
    void tenEvaluationsPerTickAtMost() {
        DinoGroup group = new DinoGroup(5);
        for (int i = 0; i < DinoGroup.MAX_EVALUATIONS_PER_TICK; i++) {
            assertTrue(group.tryEvaluate(7L));
        }
        assertFalse(group.tryEvaluate(7L));
        assertTrue(group.tryEvaluate(8L));
    }

    @Test
    void spotAccessorsAreNaNWithoutASpot() {
        DinoGroup group = new DinoGroup(5);
        UUID member = animal(1, 0.9F);
        group.add(member, this.roster);
        assertTrue(Double.isNaN(group.spotX(member)));
        group.reserveSpot(member, 3D, 4D);
        assertEquals(3D, group.spotX(member), EPSILON);
        assertEquals(4D, group.spotZ(member), EPSILON);
        group.releaseSpot(member);
        assertTrue(Double.isNaN(group.spotZ(member)));
    }

    @Test
    void aDragIsAnUrgentJourneyWhoseTargetFollowsTheLeader() {
        // Playtest 2 fix P8c: a dragged leader has no target; the journey's target follows the leader.
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID follower = animal(2, 0.5F);
        group.add(leader, this.roster);
        group.add(follower, this.roster);
        group.recentre(0D, 64D, 0D);
        group.reserveSpot(follower, 3D, 3D);
        group.leaderStep(11.8D, 0D, 9L);
        group.leaderStep(12D, 0D, 10L);
        assertTrue(group.startDrag(12D, 64D, 0D, List.of(follower), 10L));
        assertSame(HerdState.TRAVEL, group.state());
        assertTrue(group.journeyActive());
        assertTrue(group.dragged());
        assertSame(JourneyPolicy.Kind.URGENT, group.journeyKind());
        assertNull(group.spotOf(follower));
        assertEquals(List.of(follower), group.travelOrder());
        assertEquals(12D + JourneyPolicy.DRAG_AHEAD, group.journeyX(), EPSILON, "ahead of the leader, not a fixed goal");
        assertEquals(64D, group.journeyY(), EPSILON);
        assertEquals(0D, group.journeyZ(), EPSILON);
        group.leaderStep(12D, 1D, 11L);
        group.followDrag(12D, 65D, 1D);
        assertEquals(12D, group.journeyX(), EPSILON, "follows the leader as it is dragged");
        assertEquals(65D, group.journeyY(), EPSILON);
        assertEquals(1D + JourneyPolicy.DRAG_AHEAD, group.journeyZ(), EPSILON);
    }

    @Test
    void aLeashedLeaderTurnsARoamingJourneyIntoADragAndKeepsTheColumn() {
        // P8c fix round 1: the trail, the travel order and the journey count stay; followers keep their slots.
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID follower = animal(2, 0.5F);
        group.add(leader, this.roster);
        group.add(follower, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(follower), 0L);
        group.trail().record(0D, 64D, 0D);
        group.trail().record(5D, 64D, 0D);
        group.addWait();
        group.setLeaderSlowed(true);
        group.startPause(JourneyPolicy.Pause.REST, 8L);
        group.pathEnded(5D, 0D);
        group.leaderStep(4.8D, 0D, 9L);
        group.leaderStep(5D, 0D, 10L);
        group.becomeDrag(5D, 64D, 0D);
        assertTrue(group.dragged());
        assertSame(JourneyPolicy.Kind.URGENT, group.journeyKind());
        assertSame(HerdState.TRAVEL, group.state());
        assertEquals(1, group.journeys(), "no new journey");
        assertEquals(2, group.trail().size(), "the trail stays");
        assertEquals(List.of(follower), group.travelOrder());
        assertEquals(0, group.waitedTicks());
        assertFalse(group.leaderSlowed());
        assertFalse(group.journeyPaused(), "a drag has no rest pause");
        assertTrue(group.pathEnded(5D, 0D), "the old walk's path ends do not count");
        assertEquals(5D + JourneyPolicy.DRAG_AHEAD, group.journeyX(), EPSILON);
        assertEquals(1, group.countStill(5.2D, 0D), "still counted from where it was turned into a drag");
    }

    @Test
    void aDraggedLeaderStandsForTheHerdOnceItHasStoodStillATick() {
        // FollowFormation counts it like a waiting or settling leader (no stuck window while it stands).
        DinoGroup group = new DinoGroup(5);
        assertFalse(group.dragStanding());
        group.startDrag(12D, 64D, 0D, List.of(), 10L);
        assertFalse(group.dragStanding(), "it was moving when the drag started");
        group.countStill(12D, 0D);
        assertTrue(group.dragStanding());
        group.countStill(14D, 0D);
        assertFalse(group.dragStanding(), "moving again");
        group.countStill(14D, 0D);
        group.endJourney(14D, 64D, 0D);
        assertFalse(group.dragStanding());
    }

    @Test
    void aDragsTargetLiesAheadAlongTheLastDragDirection() {
        // The leader's line of travel for followers that step out of it (FollowFormation.makeWay): it must
        // point forward, not back to where the leader stood a tick ago.
        DinoGroup group = new DinoGroup(5);
        group.leaderStep(11.8D, 0D, 9L);
        group.leaderStep(12D, 0D, 10L);
        group.startDrag(12D, 64D, 0D, List.of(), 10L);
        assertEquals(12D + JourneyPolicy.DRAG_AHEAD, group.journeyX(), EPSILON, "ahead from the start");
        group.followDrag(12D, 64D, 0D);
        assertEquals(12D + JourneyPolicy.DRAG_AHEAD, group.journeyX(), EPSILON);
        assertEquals(0D, group.journeyZ(), EPSILON);
        group.leaderStep(12D, 0.3D, 11L);
        group.followDrag(12D, 65D, 0.3D);
        assertEquals(12D, group.journeyX(), EPSILON);
        assertEquals(65D, group.journeyY(), EPSILON);
        assertEquals(0.3D + JourneyPolicy.DRAG_AHEAD, group.journeyZ(), EPSILON);
        group.leaderStep(12D, 0.3D + JourneyPolicy.DRAG_MIN_STEP * 0.5D, 12L);
        group.followDrag(12D, 65D, 0.3D);
        assertEquals(0.3D + JourneyPolicy.DRAG_AHEAD, group.journeyZ(), EPSILON, "a jitter step keeps the last direction");
    }

    @Test
    void pinnedHerdRefusesADrag() {
        DinoGroup group = new DinoGroup(5);
        group.pinGraze(true);
        assertFalse(group.startDrag(12D, 64D, 0D, List.of(), 10L));
        assertFalse(group.dragged());
        assertSame(HerdState.GRAZE, group.state());
    }

    @Test
    void stillTicksCountWhileTheLeaderStaysNearWhereItStopped() {
        DinoGroup group = new DinoGroup(5);
        group.startDrag(12D, 64D, 0D, List.of(), 10L);
        assertEquals(1, group.countStill(12.3D, 0.2D));
        assertEquals(2, group.countStill(12D, 0D));
        assertEquals(0, group.countStill(14D, 0D), "moved more than the still radius: a new still spot");
        assertEquals(1, group.countStill(14.5D, 0D));
        assertEquals(2, group.countStill(14.9D, 0.3D), "slow creep stays measured from where it stopped");
        assertEquals(0, group.countStill(15.2D, 0D));
    }

    @Test
    void aWalkTargetTakesOverTheDrag() {
        // The usual urgent rules (within 3 blocks, or the walk ended) apply from then on.
        DinoGroup group = new DinoGroup(5);
        group.startDrag(12D, 64D, 0D, List.of(), 10L);
        group.pathEnded(12D, 0D);
        group.adoptTarget(30D, 63D, 5D);
        assertFalse(group.dragged());
        assertTrue(group.journeyActive());
        assertSame(JourneyPolicy.Kind.URGENT, group.journeyKind());
        assertEquals(30D, group.journeyX(), EPSILON);
        assertEquals(63D, group.journeyY(), EPSILON);
        assertEquals(5D, group.journeyZ(), EPSILON);
        assertTrue(group.pathEnded(12D, 0D), "the drag's path ends do not count against the new walk");
    }

    @Test
    void anEndedJourneyANewJourneyOrALeaderChangeIsNoDrag() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID next = animal(2, 0.5F);
        group.add(leader, this.roster);
        group.add(next, this.roster);
        group.startDrag(12D, 64D, 0D, List.of(next), 10L);
        group.endJourney(12D, 64D, 0D);
        assertFalse(group.dragged());
        group.startDrag(12D, 64D, 0D, List.of(next), 20L);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(next), 30L);
        assertFalse(group.dragged());
        group.endJourney(40D, 64D, 0D);
        group.startDrag(12D, 64D, 0D, List.of(next), 40L);
        this.present.remove(leader);
        group.remove(leader);
        assertFalse(group.dragged());
        assertFalse(group.journeyActive());
    }

    @Test
    void leaderStepIsTheDistanceSinceTheLeadersPreviousTick() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID next = animal(2, 0.5F);
        group.add(leader, this.roster);
        group.add(next, this.roster);
        assertEquals(0D, group.leaderStep(0D, 0D, 10L), EPSILON, "no previous tick");
        assertEquals(5D, group.leaderStep(3D, 4D, 11L), EPSILON);
        assertEquals(0D, group.leaderStep(30D, 40D, 13L), EPSILON, "a skipped tick (unloaded leader) is no step");
        assertEquals(1D, group.leaderStep(31D, 40D, 14L), EPSILON);
        this.present.remove(leader);
        group.remove(leader);
        assertEquals(0D, group.leaderStep(50D, 40D, 15L), EPSILON, "a new leader starts with no previous tick");
    }

    @Test
    void radiusUsesTheHerdsAreaScale() {
        // P9, P11: 20 Gallimimus (graze spacing 4.2, body 1.2) graze in R 27.4 by default; a GameTest herd whose
        // arena was sized for the old radius sets the scale to 1 and gets R 11.2.
        DinoGroup group = new DinoGroup(20);
        for (int i = 1; i <= 20; i++) {
            group.add(animal(i, 0.5F), this.roster);
        }
        assertEquals(HerdArea.HERD_AREA_SCALE, group.areaScale(), EPSILON);
        assertEquals(27.4D, group.radius(2.2D, 1.2D), 0.05D);
        group.setAreaScale(1D);
        assertEquals(11.2D, group.radius(2.2D, 1.2D), 0.05D);
    }

    @Test
    void aFarJourneyTargetHasNoHeightUntilItsLastLegResolvesIt() {
        // P11: a roaming target is a heading and a distance only (height NaN); the last leg gives it a ground height.
        DinoGroup group = new DinoGroup(5);
        group.startJourney(JourneyPolicy.Kind.ROAM, 200D, Double.NaN, 0D, List.of(), 0L);
        assertFalse(group.journeyTargetKnown());
        group.retarget(198D, 70D, 3D);
        assertTrue(group.journeyTargetKnown());
        assertEquals(198D, group.journeyX(), EPSILON);
        assertEquals(70D, group.journeyY(), EPSILON);
        assertEquals(3D, group.journeyZ(), EPSILON);
        // A GameTest goal has its height from the start.
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 10L);
        assertTrue(group.journeyTargetKnown());
    }

    // --- P13a: journey pause, legs, journey-end records ---

    @Test
    void aPauseStartsOnceAndCountsItsTicks() {
        DinoGroup group = new DinoGroup(5);
        group.startJourney(JourneyPolicy.Kind.ROAM, 200D, Double.NaN, 0D, List.of(), 0L);
        assertFalse(group.journeyPaused());
        assertTrue(group.startPause(JourneyPolicy.Pause.REST, 100L), "a new pause");
        assertFalse(group.startPause(JourneyPolicy.Pause.REST, 101L), "the same pause goes on");
        assertTrue(group.journeyPaused());
        assertSame(JourneyPolicy.Pause.REST, group.pause());
        assertEquals(50, group.pausedTicks(150L));
    }

    @Test
    void theEndOfAPauseForgetsThePreviousPathEnd() {
        // P13a item 1: the rest's walk ends must not make the next end look like "twice in one place".
        DinoGroup group = new DinoGroup(5);
        group.startJourney(JourneyPolicy.Kind.ROAM, 200D, Double.NaN, 0D, List.of(), 0L);
        assertTrue(group.pathEnded(10D, 0D));
        group.startPause(JourneyPolicy.Pause.REST, 100L);
        assertFalse(group.endPause(), "the herd kept its slots");
        assertFalse(group.journeyPaused());
        assertTrue(group.pathEnded(10D, 0D), "the first end after the pause walks the next leg");
    }

    @Test
    void aSpreadHerdDropsItsRestSpotsWhenThePauseEnds() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID follower = animal(2, 0.5F);
        group.add(leader, this.roster);
        group.add(follower, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 200D, Double.NaN, 0D, List.of(follower), 0L);
        group.startPause(JourneyPolicy.Pause.REST, 100L);
        assertFalse(group.herdSpread());
        group.spreadHerd();
        assertTrue(group.herdSpread());
        group.reserveSpot(follower, 3D, 4D);
        assertTrue(group.endPause(), "the wedge re-forms");
        assertFalse(group.herdSpread());
        assertNull(group.spotOf(follower));
    }

    @Test
    void arrivalAndANewJourneyEndThePause() {
        DinoGroup group = new DinoGroup(5);
        group.startJourney(JourneyPolicy.Kind.ROAM, 200D, Double.NaN, 0D, List.of(), 0L);
        group.startPause(JourneyPolicy.Pause.REST, 10L);
        group.arrive(20L);
        assertFalse(group.journeyPaused());
        group.startPause(JourneyPolicy.Pause.REST, 30L);
        group.startJourney(JourneyPolicy.Kind.ROAM, 200D, Double.NaN, 0D, List.of(), 40L);
        assertFalse(group.journeyPaused());
    }

    @Test
    void aLeaderChangeEndsThePause() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        group.add(leader, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 200D, Double.NaN, 0D, List.of(), 0L);
        group.startPause(JourneyPolicy.Pause.REST, 10L);
        group.add(animal(2, 0.95F), this.roster);
        assertFalse(group.journeyPaused());
    }

    @Test
    void eachJourneyEndIsRecordedOnce() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        group.add(leader, this.roster);
        group.leaderStep(0D, 0D, 99L);
        group.startJourney(JourneyPolicy.Kind.ROAM, 100D, Double.NaN, 0D, List.of(), 100L);
        group.trail().record(0D, 64D, 0D);
        group.trail().record(30D, 64D, 40D);
        assertNull(group.takeEnd());
        group.recordEnd(JourneyPolicy.End.NO_PROGRESS, leader, 30D, 40D, 400L);
        group.recordEnd(JourneyPolicy.End.ARRIVED, leader, 30D, 40D, 500L);
        JourneyPolicy.JourneyEnd end = group.takeEnd();
        assertSame(JourneyPolicy.End.NO_PROGRESS, end.reason(), "the first reason wins");
        assertSame(JourneyPolicy.Kind.ROAM, end.kind());
        assertEquals(leader, end.leader());
        assertEquals(Math.hypot(70D, 40D), end.distanceLeft(), EPSILON);
        assertEquals(50D, end.fromStart(), EPSILON);
        assertEquals(50D, end.walked(), EPSILON);
        assertEquals(300L, end.ticks());
        assertNull(group.takeEnd(), "logged once");
    }

    @Test
    void aLeaderChangeRecordsTheJourneyEnd() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        group.add(leader, this.roster);
        group.leaderStep(0D, 0D, 9L);
        group.startJourney(JourneyPolicy.Kind.ROAM, 100D, Double.NaN, 0D, List.of(), 10L);
        group.leaderStep(0.1D, 0D, 10L);
        group.leaderStep(0.2D, 0D, 11L);
        group.add(animal(2, 0.95F), this.roster);
        JourneyPolicy.JourneyEnd end = group.takeEnd();
        assertSame(JourneyPolicy.End.LEADER_CHANGE, end.reason());
        assertEquals(leader, end.leader(), "the leader that led the journey");
        assertEquals(99.8D, end.distanceLeft(), EPSILON);
        assertEquals(1L, end.ticks());
    }

    @Test
    void describeShowsTheRestPause() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        group.add(leader, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 100D, Double.NaN, 0D, List.of(), 10L);
        group.startPause(JourneyPolicy.Pause.REST, 20L);
        assertTrue(group.describe(leader).contains("(leader resting)"), group.describe(leader));
    }

    // --- P13a fix round 1 ---

    @Test
    void theEndOfAPauseRestoresTheStragglerWait() {
        // Item 1 (lead ruling Q1): late risers follow, and the straggler wait covers them again.
        DinoGroup group = new DinoGroup(5);
        group.startJourney(JourneyPolicy.Kind.ROAM, 200D, Double.NaN, 0D, List.of(), 0L);
        for (int i = 0; i < 150; i++) {
            group.addWait();
        }
        group.setStragglerSlow(true);
        group.startPause(JourneyPolicy.Pause.REST, 200L);
        group.endPause();
        assertEquals(0, group.waitedTicks());
        assertFalse(group.stragglerSlow());
    }

    @Test
    void theLeaderRisingFlagGoesWithThePause() {
        DinoGroup group = new DinoGroup(5);
        group.startJourney(JourneyPolicy.Kind.ROAM, 200D, Double.NaN, 0D, List.of(), 0L);
        group.startPause(JourneyPolicy.Pause.REST, 10L);
        group.setLeaderRising(true);
        assertTrue(group.leaderRising());
        group.endPause();
        assertFalse(group.leaderRising());
    }

    @Test
    void eachSpreadCountsOnceSoALateRiserJoinsTheNextOne() {
        // Item 6: a member still lying down from one spread must see the next one as new.
        DinoGroup group = new DinoGroup(5);
        group.startJourney(JourneyPolicy.Kind.ROAM, 200D, Double.NaN, 0D, List.of(), 0L);
        assertEquals(0, group.spreads());
        group.startPause(JourneyPolicy.Pause.REST, 10L);
        group.spreadHerd();
        group.spreadHerd();
        assertEquals(1, group.spreads(), "one spread per pause");
        group.endPause();
        group.startPause(JourneyPolicy.Pause.REST, 500L);
        group.spreadHerd();
        assertEquals(2, group.spreads());
    }

    @Test
    void tiredMarksAreKeptUntilTakenOnce() {
        // Item 5: members lieDown marked are cleared once the pause ends, if they never started Rest.
        DinoGroup group = new DinoGroup(5);
        UUID member = animal(3, 0.5F);
        assertFalse(group.hasTiredMarks());
        group.markTired(member);
        group.markTired(member);
        group.startPause(JourneyPolicy.Pause.REST, 10L);
        group.endPause();
        assertTrue(group.hasTiredMarks(), "the marks outlive the pause until the leader's tick clears them");
        assertEquals(List.of(member), group.takeTiredMarks());
        assertFalse(group.hasTiredMarks());
        assertTrue(group.takeTiredMarks().isEmpty());
    }

    @Test
    void stragglerLogLinesKeepAMinimumInterval() {
        // Item 7: a herd at the 75% edge does not flap a log line every few ticks.
        DinoGroup group = new DinoGroup(5);
        assertTrue(group.mayLogStragglerStart(100L));
        assertTrue(group.takeStragglerStartLogged(), "its end is logged too");
        assertFalse(group.mayLogStragglerStart(100L + DinoGroup.STRAGGLER_LOG_MIN_TICKS - 1));
        assertFalse(group.takeStragglerStartLogged(), "no end line for a start that was not logged");
        assertTrue(group.mayLogStragglerStart(100L + DinoGroup.STRAGGLER_LOG_MIN_TICKS));
    }
}
