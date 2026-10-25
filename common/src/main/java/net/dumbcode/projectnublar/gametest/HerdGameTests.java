package net.dumbcode.projectnublar.gametest;

import net.dumbcode.projectnublar.api.dinosaur.DinoBehaviourData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.data.BehaviourDataReloadListener;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.DinoGroup;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.GroupManager;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.HerdMoveControl;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.HerdSpacing;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.HerdState;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.LeaderJourney;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.EntityInit;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * GameTests for herds (plan B18 and the herd movement redesign). The lead runs them one at a time in a
 * creative world started from {@code runClient} (never on a dedicated GameTest server). On the GameTest server
 * each test is its own batch, so they run one at a time there too. The tests have no loader code; the holders
 * {@code ForgeGameTests} (Forge) and {@code FabricGameTests} (Fabric) register them.
 * Run a test with {@code /test run <name>} on Forge and {@code /test run fabricgametests.<name>} on Fabric,
 * where the name is the method name in lower case:
 * <ul>
 * <li>{@code fivegallimimusformoneherdledbythestrongest}</li>
 * <li>{@code twentygallimimusformoneherdoftwenty}</li>
 * <li>{@code strongernewcomerleadsatonce}</li>
 * <li>{@code deadmemberleavesitsherd}</li>
 * <li>{@code grazingherdholdshardminimum}</li>
 * <li>{@code grazingmembersstayput}</li>
 * <li>{@code smallleadermoveskeepgraze}</li>
 * <li>{@code stackedmembersseparate}</li>
 * <li>{@code herdtravelsalongtrail}</li>
 * <li>{@code herdcrossesforest}</li>
 * <li>{@code herdpassescorridor}</li>
 * <li>{@code memberneverenterslowgap}</li>
 * <li>{@code herdskirtswater}</li>
 * </ul>
 * The arenas {@code herd_track}, {@code herd_forest}, {@code herd_shore}, {@code herd_corridor} and
 * {@code herd_lowgap} come from {@code tools/gametest-templates/make_templates.py}. {@code herd_arena} (the
 * 24 x 24 formation arena) does not come from that script. Every test first checks that its animals stand
 * on the arena floor.
 * <p>
 * <b>Travel time limits</b> (playtest 1, fix P4; they replace lead decision 18's 1200, 1500 and 2400 and the
 * plan's 2000, which were sized for speeds 3 to 8 times too high). Each travel test's limit is the realistic
 * time from the journey start, times 1.3 or more, rounded up to 100 ticks. The real ground speed is 0.135 x
 * modifier^2 blocks per tick ({@code GroundSpeed}): leader journey 0.8 = 0.086 b/t, catch-up and walk-back 1.2
 * = 0.194 b/t, graze 0.6 = 0.049 b/t. The realistic time adds up, for the leader spawned in the farthest grid
 * cell:
 * <ul>
 * <li>start 90: the first path, and members in the leader's line stepping aside (up to 40 ticks each);</li>
 * <li>walk: the leader's route length / 0.086, stopping 3 short of the goal (arrival distance);</li>
 * <li>re-issues: a vanilla walk runs 150-249 ticks and each restart costs up to 40 ticks: 40 x (walk / 150,
 * rounded down);</li>
 * <li>wait: only followers whose slot is on the trail count (JourneyPolicy.Tally), so a correct herd still
 * waits at the start for front-row followers from the spawn grid (up to 22 blocks from their slots) to catch
 * up at 0.194 b/t: assumed 100 ticks in open ground, with the limit at 1.3 x or more even for the full 200. Where the herd files through a narrowing
 * (shore, forest, corridor), laggards in the file are assumed to use the whole cap: 200, the most a roaming
 * journey waits;</li>
 * <li>settle 100: TRAVEL after the arrival;</li>
 * <li>walk-in: the tail stands at the wedge depth plus 1.1 blocks of lag behind the leader (open spacing 4.2,
 * tight 3.2; 19 followers fill 7 open rows, 29.4 deep, or 60.8 in single file) and walks back inside R = 11.2
 * (20 Gallimimus: 4.2 x sqrt(20 / pi) + 0.6, at the tests' area scale 1, {@link #herdOf}) at 0.194 b/t.
 * Outside R a member walks at that speed with or without a graze spot (FollowFormation.spotWalkSpeed); only
 * the last part to its spot, inside R, is at 0.049 b/t, and the pass conditions need it inside R (or past a
 * line), not on its spot;</li>
 * <li>stagger 60: the first spot decision comes 0-60 ticks into GRAZE (GrazeSchedule.ENTER_STAGGER_TICKS) and
 * can switch the walk target and restart a path.</li>
 * </ul>
 */
public final class HerdGameTests {

    /**
     * First walkable layer. The template sits one block above the test origin, so its floor (template y=0)
     * is relative y=1; animals and goals at y=2 stand on it instead of falling through.
     */
    private static final int FLOOR_Y = 2;
    /** Tick at which every test checks that its animals stand on the floor. */
    private static final int FLOOR_CHECK_TICK = 5;
    /** An animal more than this below {@link #FLOOR_Y} fell through the floor. */
    private static final double FLOOR_TOLERANCE = 0.5D;
    // Template names (data/projectnublar/structures/<name>.nbt), public for the loader holders' @GameTest annotations.
    public static final String ARENA = "herd_arena";
    public static final String TRACK = "herd_track";
    public static final String FOREST = "herd_forest";
    public static final String SHORE = "herd_shore";
    public static final String CORRIDOR = "herd_corridor";
    public static final String LOWGAP = "herd_lowgap";
    /**
     * Pinned grazing ticks before counting: spots are picked within 60 ticks (GrazeSchedule.ENTER_STAGGER_TICKS).
     * At the graze speed (0.049 b/t) a member walks only up to 11.8 blocks toward its spot in the other 240,
     * so the tests that use this do not need the walks finished; grazingMembersStayPut waits
     * {@link #STAND_SETTLE_TICKS}.
     */
    private static final int GRAZE_SETTLE_TICKS = 300;
    /**
     * grazingMembersStayPut: pinned grazing ticks before counting standing members, so the first walks to the
     * spots are over. The longest crosses the herd area, 2R = 22.4 blocks at 0.049 b/t: 60 + 457 = 517 ticks,
     * x 1.3 = 672, so 700 ({@link #GRAZE_SETTLE_TICKS} was sized for 0.275 b/t).
     */
    private static final int STAND_SETTLE_TICKS = 700;
    private static final int COUNTED_TICKS = 1200;
    /** Spec T4: bodies may not stay closer than the hard minimum for more than 1 s. */
    private static final int CLOSE_TICKS_LIMIT = 20;
    /**
     * Spec T1: a member moving slower than this (blocks per tick) stands: the herd's own walking threshold
     * ({@code HerdBodies.WALKING_SPEED}). It must lie under every walking speed: the walk floor (modifier 0.45)
     * moves 0.027 b/t and the graze walk (0.6) 0.049 b/t. The old 0.05 counted every graze walk as standing.
     */
    private static final double STILL_SPEED = 0.01D;
    private static final double STILL_FRACTION = 0.7D;
    private static final double STILL_TICK_FRACTION = 0.9D;
    /** Graze spacing of a Gallimimus: personal space 2.2 + 2 (spec T2). */
    private static final double GRAZE_SPACING = 4.2D;
    /** A follower may change its spot only after holding it this long (GrazeSchedule.STAY_MIN_TICKS). */
    private static final int SPOT_STAY_MIN_TICKS = 600;
    private static final int LEADER_MOVES = 4;
    private static final int LEADER_MOVE_INTERVAL = 100;
    private static final double LEADER_MOVE_DISTANCE = 5D;
    /**
     * The leader's own graze-move modifier ({@code JourneyPolicy.GRAZE_MOVE_SPEED}): 0.086 b/t, so the 3 blocks
     * {@link #LEADER_MOVE_MIN_FRACTION} asks for take 35 ticks of the 100. The old 0.5 moves 0.034 b/t and
     * needs 88 ticks for them, too close to the interval.
     */
    private static final float LEADER_MOVE_SPEED = 0.8F;
    /** The leader must get at least this fraction of {@link #LEADER_MOVE_DISTANCE} away from its start before the next order. */
    private static final double LEADER_MOVE_MIN_FRACTION = 0.6D;
    /** At least this fraction of the followers must hold a graze spot on every checked tick. */
    private static final double MIN_SPOT_HOLDER_FRACTION = 0.5D;
    private static final BlockPos STACK_POS = new BlockPos(12, FLOOR_Y, 12);
    /** Spec T5: stacked members separate within this many ticks. */
    private static final int SEPARATE_TICKS = 100;
    private static final double APART_TOLERANCE = 0.05D;
    /** Journey goals: open ground inside each arena (tools/gametest-templates/make_templates.py). */
    private static final BlockPos GOAL = new BlockPos(56, FLOOR_Y, 8);
    /**
     * 16 blocks past the corridor exit (x 44): a herd member without a graze spot stands once inside R - 1 =
     * 10.2 of the centre, and the centre (the leader, within 3 of the goal) is at least x 57.5, so that line
     * (x 47.3 or more) lies outside the corridor. At x 56 it lay inside it and the file could not pass
     * (playtest 1, finding C).
     */
    private static final BlockPos CORRIDOR_GOAL = new BlockPos(60, FLOOR_Y, 11);
    /** Just east of the low-gap wall, behind the trapdoor gap (z 3-7): the herd area then covers the gap. */
    private static final BlockPos LOWGAP_GOAL = new BlockPos(40, FLOOR_Y, 5);
    private static final BlockPos SHORE_GOAL = new BlockPos(56, FLOOR_Y, 6);
    /** Spec herdTravelsAlongTrail: all within R of the new centre this many ticks after the start (arithmetic: the test's Javadoc). */
    private static final int TRAVEL_TICKS = 1700;
    /** Spec herdCrossesForest: back in GRAZE at the goal, all within R, this many ticks after the start (arithmetic: the test's Javadoc). */
    private static final int FOREST_TRAVEL_TICKS = 2400;
    /** Spec herdPassesCorridor: all pass within this many ticks (arithmetic: the test's Javadoc). */
    private static final int CORRIDOR_TICKS = 1900;
    /**
     * memberNeverEntersLowGap (the plan's own limit, not a spec number): all past the wall and together at the
     * goal this many ticks after the start (arithmetic: the test's Javadoc). The test then watches
     * {@link #GRAZE_SETTLE_TICKS} of grazing.
     */
    private static final int LOWGAP_TICKS = 1700;
    /** herdSkirtsWater: back in GRAZE at the goal, all within R, this many ticks after the start (arithmetic: the test's Javadoc). */
    private static final int SHORE_TICKS = 2000;
    /** A journey has reached its goal when the new herd centre is within this many blocks of it. */
    private static final double GOAL_TOLERANCE = 6D;
    /** Spec herdCrossesForest: no member without progress for this many ticks. */
    private static final int NO_PROGRESS_TICKS = 100;
    /**
     * The progress check skips a follower idle for fewer ticks in a row (this tick and the one before): one
     * that just finished eating or resting keeps a stale progress tick until FollowFormation runs again.
     */
    private static final int PROGRESS_IDLE_TICKS = 2;
    /** Spec T7 (2 s) and the lead's wall feedback: no member presses into a wall or trunk longer than this. */
    private static final int WALL_PRESS_TICKS = 40;
    /** Spec herdCrossesForest: at most this many followers start a path (a fallback) in one tick. */
    private static final int MAX_FALLBACKS_PER_TICK = 2;
    /** The corridor ends at x 43; a member beyond this has passed it. */
    private static final double CORRIDOR_END_X = 44D;
    /** The low-gap wall ends at x 31; a member beyond this has passed it. */
    private static final double WALL_END_X = 33D;
    /**
     * Clear height of the tested low gap (make_templates.py): floor top to the bottom of a closed top-half
     * trapdoor, 2 + 13/16. Taller than the Gallimimus hitbox (2.6), lower than its clearance box (3.0).
     */
    private static final double TRAPDOOR_GAP_HEIGHT = 2.8125D;
    /** Clear height of the control gap: floor top to the bottom of a top slab, lower than the hitbox. */
    private static final double SLAB_GAP_HEIGHT = 2.5D;
    private static final double SAME_TARGET = 1.0E-6D;
    /**
     * Herd area scale of every test herd ({@link #herdOf}; playtest fix P9): the arenas were sized for the
     * unscaled radius, so the R values in these Javadocs (11.2 for 20 Gallimimus) stay true.
     */
    private static final double ARENA_AREA_SCALE = 1.0D;

    private HerdGameTests() {
    }

    /**
     * Five animals form one herd, and the strongest leads it.
     */
    public static void fiveGallimimusFormOneHerdLedByTheStrongest(GameTestHelper helper) {
        List<Dinosaur> animals = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            animals.add(hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(4 + 4 * i, FLOOR_Y, 5), 0D));
        }
        Dinosaur strongest = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(12, FLOOR_Y, 18), 50D);
        animals.add(strongest);

        helper.startSequence()
                .thenExecuteAfter(FLOOR_CHECK_TICK, () -> assertOnFloor(helper, animals))
                .thenWaitUntil(() -> {
                    DinoGroup group = GroupManager.groupOf(strongest);
                    helper.assertTrue(group != null, "the strongest animal has no group");
                    for (Dinosaur animal : animals) {
                        helper.assertTrue(GroupManager.groupOf(animal) == group, "an animal is not in the strongest animal's herd");
                    }
                    helper.assertTrue(strongest.isGroupLeader(), "the strongest animal does not lead");
                    helper.assertTrue(animals.stream().filter(Dinosaur::isGroupLeader).count() == 1, "the herd has more than one leader");
                })
                .thenSucceed();
    }

    /**
     * A full herd: the Gallimimus config sets {@code group_size} to 20. Twenty animals end in one herd
     * whatever pairs form first, because any two partial herds fit together and merge.
     */
    public static void twentyGallimimusFormOneHerdOfTwenty(GameTestHelper helper) {
        List<Dinosaur> animals = new ArrayList<>();
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 5; column++) {
                animals.add(hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(3 + 4 * column, FLOOR_Y, 4 + 5 * row), 0D));
            }
        }

        helper.startSequence()
                .thenExecuteAfter(FLOOR_CHECK_TICK, () -> assertOnFloor(helper, animals))
                .thenWaitUntil(() -> {
                    DinoGroup first = GroupManager.groupOf(animals.get(0));
                    helper.assertTrue(herdOf(animals) != null, "the 20 animals are not one herd of 20 (the first animal's herd has "
                            + (first == null ? 0 : first.size()) + " members)");
                    helper.assertTrue(animals.stream().filter(Dinosaur::isGroupLeader).count() == 1, "the herd does not have exactly one leader");
                })
                .thenSucceed();
    }

    /**
     * A stronger animal that joins a pair leads the herd at once.
     */
    public static void strongerNewcomerLeadsAtOnce(GameTestHelper helper) {
        Dinosaur first = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(8, FLOOR_Y, 12), 0D);
        Dinosaur second = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(14, FLOOR_Y, 12), 0D);
        Dinosaur[] newcomer = new Dinosaur[1];

        helper.startSequence()
                .thenExecuteAfter(FLOOR_CHECK_TICK, () -> assertOnFloor(helper, List.of(first, second)))
                .thenWaitUntil(() -> helper.assertTrue(GroupManager.groupOf(first) != null
                        && GroupManager.groupOf(first) == GroupManager.groupOf(second), "the first two have not paired"))
                .thenExecute(() -> newcomer[0] = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(11, FLOOR_Y, 18), 50D))
                .thenWaitUntil(() -> {
                    helper.assertTrue(GroupManager.groupOf(newcomer[0]) == GroupManager.groupOf(first), "the newcomer has not joined");
                    helper.assertTrue(newcomer[0].isGroupLeader(), "the stronger newcomer does not lead");
                })
                .thenSucceed();
    }

    /**
     * A member that dies leaves its herd; the survivor stays in a herd of 1.
     */
    public static void deadMemberLeavesItsHerd(GameTestHelper helper) {
        Dinosaur doomed = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(8, FLOOR_Y, 12), 0D);
        Dinosaur survivor = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(14, FLOOR_Y, 12), 0D);

        helper.startSequence()
                .thenExecuteAfter(FLOOR_CHECK_TICK, () -> assertOnFloor(helper, List.of(doomed, survivor)))
                .thenWaitUntil(() -> helper.assertTrue(GroupManager.groupOf(doomed) != null
                        && GroupManager.groupOf(doomed) == GroupManager.groupOf(survivor), "the two have not paired"))
                .thenExecute(doomed::kill)
                .thenWaitUntil(() -> {
                    helper.assertTrue(GroupManager.groupOf(doomed) == null, "the dead animal is still in the herd");
                    DinoGroup group = GroupManager.groupOf(survivor);
                    helper.assertTrue(group != null && group.size() == 1, "the survivor's herd does not have 1 member");
                })
                .thenSucceed();
    }

    /**
     * Spec T4: a pinned grazing herd of 20; no pair stays closer than the hard minimum for more than 20 ticks.
     */
    public static void grazingHerdHoldsHardMinimum(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        int[][] close = new int[animals.size()][animals.size()];
        long[] clock = new long[3]; // ticks, pinned ticks, counted ticks
        helper.onEachTick(() -> {
            if (++clock[0] == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            DinoGroup group = herdOf(animals);
            if (group == null) {
                return;
            }
            group.pinGraze(true);
            if (clock[1]++ < GRAZE_SETTLE_TICKS) {
                return;
            }
            checkSpacing(helper, animals, close);
            if (++clock[2] >= COUNTED_TICKS) {
                helper.succeed();
            }
        });
    }

    /**
     * Spec T1 and T2: a pinned grazing herd of 20 mostly stands: in 90% of the counted ticks at least 70% of
     * the members move under {@link #STILL_SPEED} (0.01) blocks per tick. On every counted tick no two graze
     * spots are closer than {@link #GRAZE_SPACING} (4.2), and at least half of the followers hold a spot (so
     * the spot check cannot pass with no spots). Counting starts after {@link #STAND_SETTLE_TICKS} of pinned
     * grazing.
     */
    public static void grazingMembersStayPut(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        double[][] last = new double[animals.size()][2];
        long[] clock = new long[4]; // ticks, pinned ticks, counted ticks, ticks with enough members standing
        helper.onEachTick(() -> {
            if (++clock[0] == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            DinoGroup group = herdOf(animals);
            if (group == null) {
                return;
            }
            group.pinGraze(true);
            int still = 0;
            for (int i = 0; i < animals.size(); i++) {
                Dinosaur animal = animals.get(i);
                double dx = animal.getX() - last[i][0];
                double dz = animal.getZ() - last[i][1];
                if (dx * dx + dz * dz < STILL_SPEED * STILL_SPEED) {
                    still++;
                }
                last[i][0] = animal.getX();
                last[i][1] = animal.getZ();
            }
            if (clock[1]++ < STAND_SETTLE_TICKS) {
                return;
            }
            clock[2]++;
            if (still >= STILL_FRACTION * animals.size()) {
                clock[3]++;
            }
            int holders = checkSpotSpacing(helper, group, animals, GroupManager.leaderOf(group, helper.getLevel()));
            if (holders < MIN_SPOT_HOLDER_FRACTION * (animals.size() - 1)) {
                helper.fail("only " + holders + " of " + (animals.size() - 1) + " followers hold a graze spot");
            }
            if (clock[2] < COUNTED_TICKS) {
                return;
            }
            if (clock[3] < STILL_TICK_FRACTION * clock[2]) {
                helper.fail("enough members stood in only " + clock[3] + " of " + clock[2] + " ticks");
            }
            helper.succeed();
        });
    }

    /**
     * Spec T3 and T6: after {@link #GRAZE_SETTLE_TICKS} of pinned grazing the herd is unpinned, and the leader
     * is ordered to walk 5 blocks toward the herd centre every 100 ticks, 4 times. Unpinned, a leader move that
     * left the herd area would start a journey, so the test fails if the state leaves GRAZE. A real roaming
     * journey cannot start inside the test: the first one is due at least {@code JourneyPolicy.JOURNEY_MIN_TICKS}
     * (2400) ticks after the leader first schedules. Each order must take the leader at least
     * {@link #LEADER_MOVE_MIN_FRACTION} of the 5 blocks away from where it got the order before the next order
     * (the farthest it got, so a graze move back toward the start after the walk does not count against it).
     * <p>
     * No follower that holds a spot changes or loses it before holding it {@link #SPOT_STAY_MIN_TICKS} (the
     * schedule's own minimum stay). Followers without a spot are not checked for changes: random best-of-8
     * sampling cannot always place 19 spots 4.2 apart inside the 22-wide arena (Review Focus 1). But at least
     * {@link #MIN_SPOT_HOLDER_FRACTION} of the followers must hold a spot on every tick after the moves begin,
     * so the spot check cannot pass with no spots.
     */
    public static void smallLeaderMovesKeepGraze(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        double[][] spots = new double[animals.size()][];
        Dinosaur[] ordered = new Dinosaur[1]; // the leader that got the last order
        double[] orderedFrom = new double[3]; // its x and z when it got the order, the farthest it got from there since
        long[] clock = {0L, 0L, -1L, 0L}; // ticks, pinned ticks, tick the leader moves began, leader moves made
        helper.onEachTick(() -> {
            long tick = ++clock[0];
            if (tick == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            DinoGroup group = herdOf(animals);
            if (group == null) {
                return;
            }
            boolean movesBegan = clock[2] >= 0;
            group.pinGraze(!movesBegan);
            Dinosaur leader = GroupManager.leaderOf(group, helper.getLevel());
            if (leader == null) {
                return;
            }
            int holders = 0;
            for (int i = 0; i < animals.size(); i++) {
                Dinosaur animal = animals.get(i);
                if (animal == leader) {
                    continue;
                }
                double x = group.spotX(animal.getUUID());
                double z = group.spotZ(animal.getUUID());
                boolean changed = spots[i] != null && (Double.isNaN(x) || spots[i][0] != x || spots[i][1] != z);
                if (changed && movesBegan && tick - spots[i][2] < SPOT_STAY_MIN_TICKS) {
                    helper.fail("a follower changed its graze spot while the leader made small moves", animal);
                }
                if (Double.isNaN(x)) {
                    spots[i] = null;
                } else {
                    holders++;
                    if (spots[i] == null || changed) {
                        spots[i] = new double[]{x, z, tick};
                    }
                }
            }
            if (movesBegan && holders < MIN_SPOT_HOLDER_FRACTION * (animals.size() - 1)) {
                helper.fail("only " + holders + " of " + (animals.size() - 1) + " followers hold a graze spot");
            }
            if (clock[1]++ < GRAZE_SETTLE_TICKS) {
                return;
            }
            if (clock[2] < 0) {
                clock[2] = tick;
            }
            if (group.state() != HerdState.GRAZE) {
                helper.fail(clock[3] == 0 ? "the herd was not grazing when the leader moves were due"
                        : "a small leader move started a journey", leader);
            }
            if (clock[3] > 0) {
                trackLeaderWalk(ordered[0], orderedFrom);
            }
            long since = tick - clock[2];
            if (since % LEADER_MOVE_INTERVAL == 0 && clock[3] < LEADER_MOVES) {
                if (clock[3] > 0) {
                    checkLeaderMoved(helper, ordered[0], orderedFrom);
                }
                double direction = leader.getX() > group.centreX() ? -1D : 1D;
                BrainUtils.setMemory(leader, MemoryModuleType.WALK_TARGET, new WalkTarget(
                        new Vec3(leader.getX() + direction * LEADER_MOVE_DISTANCE, leader.getY(), leader.getZ()), LEADER_MOVE_SPEED, 0));
                ordered[0] = leader;
                orderedFrom[0] = leader.getX();
                orderedFrom[1] = leader.getZ();
                orderedFrom[2] = 0D;
                clock[3]++;
            }
            if (clock[3] >= LEADER_MOVES && since >= (LEADER_MOVES + 1L) * LEADER_MOVE_INTERVAL) {
                checkLeaderMoved(helper, ordered[0], orderedFrom);
                helper.succeed();
            }
        });
    }

    /**
     * Spec T5: three animals spawned on one block are all at the hard minimum within 100 ticks of forming a herd.
     * <p>
     * Two deliberate deviations from the spec table ("separate within 5 s"):
     * <ul>
     * <li>The 100 ticks count from the tick the herd forms, not from the spawn. Spacing applies only to herd
     * members ({@code HerdSpacing.applies}), so nothing unstacks the animals before the herd exists, and the
     * time the herd takes to form is not part of T5.</li>
     * <li>A pair counts as apart at {@link #APART_TOLERANCE} under the hard minimum. A separating body stops
     * when it reaches the minimum, and movement and collision can leave the stop a little short of it.</li>
     * </ul>
     */
    public static void stackedMembersSeparate(GameTestHelper helper) {
        List<Dinosaur> animals = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            animals.add(hatch(helper, EntityInit.GALLIMIMUS.get(), STACK_POS, 0D));
        }
        long[] clock = {0L, -1L}; // ticks, tick the herd formed
        helper.onEachTick(() -> {
            long tick = ++clock[0];
            if (tick == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            if (clock[1] < 0) {
                if (herdOf(animals) != null) {
                    clock[1] = tick;
                }
                return;
            }
            if (allApart(animals)) {
                helper.succeed();
            } else if (tick - clock[1] > SEPARATE_TICKS) {
                helper.fail("stacked members did not reach the hard minimum within " + SEPARATE_TICKS + " ticks of forming a herd");
            }
        });
    }

    /**
     * Spec T4, T6: a journey along the open track to {@link #GOAL}. Passes when the herd grazes again with its
     * centre near the goal and every member within R of it, within {@link #TRAVEL_TICKS} of the start. No pair
     * stays under the hard minimum for more than 20 ticks; no member presses into a wall for over 40 ticks.
     * <p>
     * Limit (class Javadoc): route 51 blocks (cell 3.5, 18.5 to 3 short of the goal); start 90 + walk
     * 51 / 0.086 = 593 + re-issues 3 x 40 = 120 + wait 100 + settle 100 + walk-in (open wedge, tail 30.5
     * behind) (30.5 - 11.2) / 0.194 = 100 + stagger 60; total 1163, x 1.3 = 1512. A full 200-tick wait makes
     * 1263, x 1.3 = 1642, so 1700 (1.46 x the realistic total, 1.35 x with the full wait). The timeout leaves
     * 1500 ticks to form the herd.
     */
    public static void herdTravelsAlongTrail(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        int[][] close = new int[animals.size()][animals.size()];
        boolean[] travelled = new boolean[1];
        runJourney(helper, animals, GOAL, TRAVEL_TICKS,
                "the herd was not together at the goal " + TRAVEL_TICKS + " ticks after the journey started", () -> { },
                (group, sinceStart) -> {
                    checkSpacing(helper, animals, close);
                    return arrivedTogether(helper, group, animals, GOAL, travelled);
                });
    }

    /**
     * Spec T7: a journey through the natural forest to {@link #GOAL} beyond it. Passes when the herd grazes
     * again with its centre near the goal and every member within R of it, within {@link #FOREST_TRAVEL_TICKS}
     * of the start. While the herd travels, no idle follower goes {@link #NO_PROGRESS_TICKS} without progress.
     * On every tick after the start, at most {@link #MAX_FALLBACKS_PER_TICK} followers start a path (seen on
     * their navigation, not on the fallback budget's own count, which the budget caps); no wall press over 40
     * ticks.
     * <p>
     * Limit (class Javadoc): route 51 blocks straight (cell 3.5, 18.5 to 3 short of the goal), x 1.3 for the
     * turns between trunks = 66; start 90 + walk 66 / 0.086 = 767 + re-issues 5 x 40 = 200 + wait 200 + settle
     * 100 + walk-in (single file through the trees, tail 62 behind, x 1.3 for the turns) (62 - 11.2) x 1.3 /
     * 0.194 = 340 + stagger 60; total 1757, x 1.3 = 2284, so 2400 (1.37 x; the forest is the least certain
     * estimate, and the limit is not cut below lead decision 18's 2400). The timeout leaves 1600 ticks to form
     * the herd.
     */
    public static void herdCrossesForest(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        boolean[] travelled = new boolean[1];
        boolean[] pathing = new boolean[animals.size()];
        int[] idleTicks = new int[animals.size()];
        int[] pathStarts = new int[1]; // followers that started a path this tick
        runJourney(helper, animals, GOAL, FOREST_TRAVEL_TICKS,
                "the herd did not cross the forest to the goal within " + FOREST_TRAVEL_TICKS + " ticks",
                () -> {
                    pathStarts[0] = countPathStarts(animals, pathing);
                    countIdleTicks(animals, idleTicks);
                },
                (group, sinceStart) -> {
                    if (group.state() == HerdState.TRAVEL && sinceStart > NO_PROGRESS_TICKS) {
                        checkProgress(helper, animals, idleTicks);
                    }
                    if (pathStarts[0] > MAX_FALLBACKS_PER_TICK) {
                        helper.fail(pathStarts[0] + " followers started a path in one tick (at most " + MAX_FALLBACKS_PER_TICK + ")");
                    }
                    return arrivedTogether(helper, group, animals, GOAL, travelled);
                });
    }

    /**
     * Spec herdPassesCorridor (decision 1: single file where the ground is narrow): a journey to
     * {@link #CORRIDOR_GOAL} through the 3-wide corridor (x 24-43). Passes when every member has been past
     * x {@link #CORRIDOR_END_X} within {@link #CORRIDOR_TICKS} of the start. While the herd travels, no two
     * followers share a target point; no wall press over 40 ticks.
     * <p>
     * Limit (class Javadoc): route 55 blocks (cell 3.5, 3.5 to the corridor mouth: 21.5; corridor: 20; exit to x 57.5:
     * 13.5); start 90 + walk 55 / 0.086 = 640 + re-issues 4 x 40 = 160 + wait 200 + settle 100. At the arrival
     * the tail is 43.5 behind the leader (5 followers in 2 open rows east of the exit, 8 in single file at 3.2
     * through the corridor and 2 blocks either side of it, 6 in 2 open rows west of it, + 1.1 lag), 30 blocks
     * short of x 44: 30 / 0.194 = 155, so 160 with the file ahead of it, + stagger 60; total 1410, x 1.3 =
     * 1833, so 1900 (1.35 x). The timeout leaves 1500 ticks to form the herd.
     */
    public static void herdPassesCorridor(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        boolean[] passed = new boolean[animals.size()];
        runJourney(helper, animals, CORRIDOR_GOAL, CORRIDOR_TICKS,
                "not every member passed the corridor within " + CORRIDOR_TICKS + " ticks", () -> { },
                (group, sinceStart) -> {
                    if (group.state() == HerdState.TRAVEL) {
                        checkDistinctTargets(helper, group, animals);
                    }
                    return allPassed(helper, animals, passed, CORRIDOR_END_X);
                });
    }

    /**
     * Spec T8: no member ever enters a gap its clearance box does not fit. The wall at x 30-31 has a trapdoor
     * gap (z 3-7, {@link #TRAPDOOR_GAP_HEIGHT} = 2.8125 high under closed top-half trapdoors), a slab gap
     * (z 10-14, {@link #SLAB_GAP_HEIGHT} = 2.5 high) and an open passage (z 17-21). The trapdoor gap is the
     * tested case: the Gallimimus hitbox (2.6 tall) fits it, and only the clearance box (3.0 tall) forbids it.
     * The slab gap is a control: it is lower than the hitbox, so collision alone keeps a body out. The test
     * checks both gaps on every tick. Vanilla pathing checks 3 blocks of height for a 2.6-tall mob and may
     * count a closed trapdoor as passable, so a vanilla path (the leader's journey walk, a fallback path) can
     * plan through the trapdoor gap; the guard must then stop the body at it.
     * <p>
     * The journey goes through the open passage to {@link #LOWGAP_GOAL}, just east of the wall behind the
     * trapdoor gap. The herd area (R about 11.2 for 20) then covers the gap and ground west of the wall, which
     * a graze spot can reach only through the trapdoor gap. So the graze reach check must reject such spots,
     * and steering near the gap must refuse it. Passes when every member has been past x {@link #WALL_END_X}
     * and the herd grazes together at the goal ({@link #arrivedTogether}) within {@link #LOWGAP_TICKS} of the
     * start, and then stays in GRAZE for {@link #GRAZE_SETTLE_TICKS} (pinned from the arrival, so a roaming
     * journey cannot start in the meantime); no wall press over 40 ticks.
     * <p>
     * Limit (class Javadoc): route 45 blocks (cell 3.5, 3.5 to the passage 30.5, 19: 31.1; on to 3 short of
     * the goal: 13.8); start 90 + walk 45 / 0.086 = 523 + re-issues 3 x 40 = 120 + wait 100 (open ground; the
     * passage is 2 blocks long) + settle 100 + walk-in (wedge about 32 deep, tail 33 behind along the trail,
     * back through the passage) (33 - 11.2) / 0.194 = 112, + 60 to find it stuck at the wall and get a fallback
     * path = 175 + stagger 60; total 1168, x 1.3 = 1518. A full 200-tick wait makes 1268, x 1.3 = 1648, so
     * 1700 (1.46 x the realistic total, 1.34 x with the full wait). The timeout leaves 1500 ticks to form the
     * herd (after the limit and the 300 ticks of grazing).
     */
    public static void memberNeverEntersLowGap(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        boolean[] passed = new boolean[animals.size()];
        boolean[] travelled = new boolean[1];
        long[] arrived = {-1L}; // ticks after the start when all were past the wall and together at the goal
        AABB trapdoorGap = new AABB(helper.absoluteVec(new Vec3(30D, FLOOR_Y, 3D)),
                helper.absoluteVec(new Vec3(32D, FLOOR_Y + TRAPDOOR_GAP_HEIGHT, 8D)));
        AABB slabGap = new AABB(helper.absoluteVec(new Vec3(30D, FLOOR_Y, 10D)),
                helper.absoluteVec(new Vec3(32D, FLOOR_Y + SLAB_GAP_HEIGHT, 15D)));
        int limitTicks = LOWGAP_TICKS + GRAZE_SETTLE_TICKS;
        runJourney(helper, animals, LOWGAP_GOAL, limitTicks,
                "the herd did not pass the wall and graze " + GRAZE_SETTLE_TICKS + " ticks at the goal within " + limitTicks + " ticks",
                () -> {
                    for (Dinosaur animal : animals) {
                        if (animal.getBoundingBox().intersects(trapdoorGap) || animal.getBoundingBox().intersects(slabGap)) {
                            helper.fail("a herd member entered a gap lower than its clearance box", animal);
                        }
                    }
                },
                (group, sinceStart) -> {
                    if (arrived[0] < 0) {
                        boolean allPast = allPassed(helper, animals, passed, WALL_END_X);
                        if (arrivedTogether(helper, group, animals, LOWGAP_GOAL, travelled) && allPast) {
                            arrived[0] = sinceStart;
                            // Pinned from here: a spontaneous roaming journey must not break the GRAZE hold.
                            group.pinGraze(true);
                        } else if (sinceStart > LOWGAP_TICKS) {
                            helper.fail("the herd was not past the wall and together at the goal within " + LOWGAP_TICKS + " ticks");
                        }
                        return false;
                    }
                    if (group.state() != HerdState.GRAZE) {
                        helper.fail("the herd left GRAZE next to the trapdoor gap");
                    }
                    return sinceStart - arrived[0] >= GRAZE_SETTLE_TICKS;
                });
    }

    /**
     * Review Focus 1 of the previous plan: travelling beside water, nobody walks into it. A journey along the
     * channel (z 12-14) to {@link #SHORE_GOAL}. Passes when the herd grazes again with its centre near the goal
     * and every member within R of it, within {@link #SHORE_TICKS} of the start. No member is ever in water;
     * no wall press over 40 ticks.
     * <p>
     * Limit (class Javadoc): route 52 blocks (cell 3.5, 18.5 round the channel's west end at 23.5, 11: 21.4;
     * on to 3 short of the goal: 30.3); start 90 + walk 52 / 0.086 = 605 + re-issues 4 x 40 = 160 + wait 200 +
     * settle 100 + walk-in (the deepest wedge, single file beside the channel, tail 62 behind) (62 - 11.2) /
     * 0.194 = 262 + stagger 60; total 1477, x 1.3 = 1920, so 2000 (1.35 x). The timeout leaves 1400 ticks to
     * form the herd.
     */
    public static void herdSkirtsWater(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        boolean[] travelled = new boolean[1];
        runJourney(helper, animals, SHORE_GOAL, SHORE_TICKS,
                "the herd was not together at the goal " + SHORE_TICKS + " ticks after the journey started",
                () -> {
                    for (Dinosaur animal : animals) {
                        if (animal.isInWater()) {
                            helper.fail("a herd member walked into the water", animal);
                        }
                    }
                },
                (group, sinceStart) -> arrivedTogether(helper, group, animals, SHORE_GOAL, travelled));
    }

    /** Twenty adult Gallimimus on a 5 x 4 grid at the start of a track (x 3-19, z firstZ to firstZ + 15). */
    private static List<Dinosaur> hatchTwenty(GameTestHelper helper, int firstZ) {
        List<Dinosaur> animals = new ArrayList<>();
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 5; column++) {
                animals.add(hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(3 + 4 * column, FLOOR_Y, firstZ + 5 * row), 0D));
            }
        }
        return animals;
    }

    /**
     * The herd of all the animals if they form exactly one herd of their number, else {@code null}. The herd
     * found gets area scale 1 ({@link DinoGroup#setAreaScale}; playtest fix P9): the 64 x 24 arenas and every
     * derived number in these tests (R = 11.2 for 20, R - 1, R + 2, the limits) were sized for that radius,
     * and the default scale (R 27.4 for 20 Gallimimus, 54.9 across; fix P11) does not fit a 22-wide track. Every test
     * gets the herd through here before it pins, moves or starts a journey, so the scale is set first.
     */
    private static @Nullable DinoGroup herdOf(List<Dinosaur> animals) {
        DinoGroup group = GroupManager.groupOf(animals.get(0));
        if (group == null || group.size() != animals.size()) {
            return null;
        }
        for (Dinosaur animal : animals) {
            if (GroupManager.groupOf(animal) != group) {
                return null;
            }
        }
        group.setAreaScale(ARENA_AREA_SCALE);
        return group;
    }

    /** Spec: each test first checks that every animal stands on the arena floor. */
    private static void assertOnFloor(GameTestHelper helper, List<Dinosaur> animals) {
        for (Dinosaur animal : animals) {
            if (helper.relativeVec(animal.position()).y < FLOOR_Y - FLOOR_TOLERANCE) {
                helper.fail("spawned below the floor", animal);
            }
        }
    }

    /**
     * Counts, per pair, the ticks in a row closer than the hard minimum; fails past {@link #CLOSE_TICKS_LIMIT}.
     * A pair with a member that ghosts through its herd mates on a fallback path, or just did
     * ({@link HerdMoveControl#ghostExempt}), is allowed inside the minimum (lead decision, playtest 2 fix P8b)
     * and its count starts over.
     */
    private static void checkSpacing(GameTestHelper helper, List<Dinosaur> animals, int[][] close) {
        for (int i = 0; i < animals.size(); i++) {
            for (int j = i + 1; j < animals.size(); j++) {
                Dinosaur a = animals.get(i);
                Dinosaur b = animals.get(j);
                if (ghostExempt(a) || ghostExempt(b)) {
                    close[i][j] = 0;
                    continue;
                }
                close[i][j] = horizontalDistance(a, b) < HerdSpacing.hardMinimumOf(a, b) ? close[i][j] + 1 : 0;
                if (close[i][j] > CLOSE_TICKS_LIMIT) {
                    helper.fail("two herd members stayed closer than the hard minimum for over " + CLOSE_TICKS_LIMIT + " ticks", a);
                }
            }
        }
    }

    /**
     * Fails if two reserved graze spots (the leader's included) are closer than {@link #GRAZE_SPACING} (spec T2).
     *
     * @return the number of followers (animals other than {@code leader}) that hold a spot
     */
    private static int checkSpotSpacing(GameTestHelper helper, DinoGroup group, List<Dinosaur> animals,
                                        @Nullable Dinosaur leader) {
        int holders = 0;
        for (int i = 0; i < animals.size(); i++) {
            double ax = group.spotX(animals.get(i).getUUID());
            if (Double.isNaN(ax)) {
                continue;
            }
            if (animals.get(i) != leader) {
                holders++;
            }
            double az = group.spotZ(animals.get(i).getUUID());
            for (int j = i + 1; j < animals.size(); j++) {
                double bx = group.spotX(animals.get(j).getUUID());
                if (Double.isNaN(bx)) {
                    continue;
                }
                double dx = ax - bx;
                double dz = az - group.spotZ(animals.get(j).getUUID());
                if (dx * dx + dz * dz < GRAZE_SPACING * GRAZE_SPACING - 1.0E-6D) {
                    helper.fail("two graze spots are closer than the graze spacing", animals.get(i));
                }
            }
        }
        return holders;
    }

    /**
     * Records in {@code orderedFrom[2]} the farthest {@code leader} has been from where it got its order
     * ({@code orderedFrom[0]}, {@code orderedFrom[1]}). A graze move back toward the start after the ordered
     * walk then does not undo the walk.
     */
    private static void trackLeaderWalk(Dinosaur leader, double[] orderedFrom) {
        double dx = leader.getX() - orderedFrom[0];
        double dz = leader.getZ() - orderedFrom[1];
        orderedFrom[2] = Math.max(orderedFrom[2], Math.sqrt(dx * dx + dz * dz));
    }

    /**
     * Fails if {@code leader} never got {@link #LEADER_MOVE_MIN_FRACTION} of its ordered move away from where
     * it got the order (the farthest distance that {@link #trackLeaderWalk} recorded).
     */
    private static void checkLeaderMoved(GameTestHelper helper, Dinosaur leader, double[] orderedFrom) {
        double walked = orderedFrom[2];
        if (walked < LEADER_MOVE_MIN_FRACTION * LEADER_MOVE_DISTANCE) {
            helper.fail("the leader walked only " + walked + " of its ordered " + LEADER_MOVE_DISTANCE + " blocks", leader);
        }
    }

    /** Every pair is at the hard minimum or more (within {@link #APART_TOLERANCE}); ghosting pairs ({@link #ghostExempt}) are skipped. */
    private static boolean allApart(List<Dinosaur> animals) {
        for (int i = 0; i < animals.size(); i++) {
            for (int j = i + 1; j < animals.size(); j++) {
                Dinosaur a = animals.get(i);
                Dinosaur b = animals.get(j);
                if (ghostExempt(a) || ghostExempt(b)) {
                    continue;
                }
                if (horizontalDistance(a, b) < HerdSpacing.hardMinimumOf(a, b) - APART_TOLERANCE) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Whether the animal ghosts through herd mates on a fallback path, or did within the grace ({@link HerdMoveControl#ghostExempt}). */
    private static boolean ghostExempt(Dinosaur animal) {
        return animal.getMoveControl() instanceof HerdMoveControl control && control.ghostExempt();
    }

    /**
     * Morning day time for the travel tests: every travel test runs at most 4000 ticks, so it ends before sunset
     * (day time 12000), when a roaming leader lies down for the night (P13a ruling Q2).
     */
    private static final long TRAVEL_DAY_TIME = 1000L;

    /** The part of a travel test that differs per test: its own checks and its pass condition. */
    @FunctionalInterface
    private interface JourneyCheck {
        /**
         * Runs on every tick after the journey started, after the shared wall check. Fails the test through
         * {@code helper.fail} if one of the test's rules breaks.
         *
         * @param sinceStart ticks since the journey started
         * @return {@code true} when the test's pass condition holds
         */
        boolean passed(DinoGroup group, long sinceStart);
    }

    /**
     * The shared run of a travel test. On every tick: checks the floor at {@link #FLOOR_CHECK_TICK} and runs
     * {@code everyTick}. Once the animals form one herd, starts a roaming journey to {@code goal}. On each
     * later tick: checks the walls ({@link #checkWalls}), also while the herd is split; then, while the
     * animals are one herd, runs {@code check}. Succeeds when {@code check} passes; fails with {@code failure}
     * if it has not passed {@code limitTicks} after the journey started (a split herd included). The day time is
     * set to {@link #TRAVEL_DAY_TIME} at the start (P13a fix round 1): at night the leader lies down until
     * morning and the journey pauses (ruling Q2), which would time the test out.
     */
    private static void runJourney(GameTestHelper helper, List<Dinosaur> animals, BlockPos goal, int limitTicks,
                                   String failure, Runnable everyTick, JourneyCheck check) {
        helper.getLevel().setDayTime(TRAVEL_DAY_TIME);
        long[] clock = {0L, -1L}; // ticks, tick the journey started
        helper.onEachTick(() -> {
            long tick = ++clock[0];
            if (tick == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            everyTick.run();
            DinoGroup group = herdOf(animals);
            if (clock[1] < 0) {
                if (group != null) {
                    startJourney(helper, group, goal);
                    clock[1] = tick;
                }
                return;
            }
            checkWalls(helper, animals);
            if (group != null && check.passed(group, tick - clock[1])) {
                helper.succeed();
            } else if (tick - clock[1] > limitTicks) {
                helper.fail(failure);
            }
        });
    }

    /**
     * Starts a roaming journey of the herd's leader to the goal block's bottom centre. The goal has a height, so
     * the leader walks to it directly, as before playtest fix P11; legs ({@code JourneyPolicy.chooseLeg}) are
     * only for far targets with no height, which these tests do not use.
     */
    private static void startJourney(GameTestHelper helper, DinoGroup group, BlockPos goal) {
        Dinosaur leader = GroupManager.leaderOf(group, helper.getLevel());
        if (leader == null || !LeaderJourney.beginRoam(leader, helper.absoluteVec(Vec3.atBottomCenterOf(goal)))) {
            helper.fail("the herd leader could not start a journey");
        }
    }

    /** Lead feedback: no member presses into a wall or trunk for more than {@link #WALL_PRESS_TICKS}. */
    private static void checkWalls(GameTestHelper helper, List<Dinosaur> animals) {
        for (Dinosaur animal : animals) {
            if (animal.getMoveControl() instanceof HerdMoveControl control && control.wallBlockedTicks() > WALL_PRESS_TICKS) {
                helper.fail("a herd member pressed into a wall or trunk for over " + WALL_PRESS_TICKS + " ticks", animal);
            }
        }
    }

    /**
     * Spec herdCrossesForest: every idle follower made progress (or waited on purpose) within the last 100
     * ticks. Checks only a follower idle for at least {@link #PROGRESS_IDLE_TICKS} ticks in a row
     * ({@link #countIdleTicks}): one that just finished eating or resting keeps a stale progress tick until
     * FollowFormation runs again.
     */
    private static void checkProgress(GameTestHelper helper, List<Dinosaur> animals, int[] idleTicks) {
        long now = helper.getLevel().getGameTime();
        for (int i = 0; i < animals.size(); i++) {
            Dinosaur animal = animals.get(i);
            if (animal.isGroupLeader() || idleTicks[i] < PROGRESS_IDLE_TICKS
                    || !(animal.getMoveControl() instanceof HerdMoveControl control)) {
                continue;
            }
            if (now - control.lastProgressTick() > NO_PROGRESS_TICKS) {
                helper.fail("a herd member made no progress for " + NO_PROGRESS_TICKS + " ticks", animal);
            }
        }
    }

    /** Counts, per animal, the ticks in a row it has been idle ({@code Dinosaur.isIdle}). Call once per tick. */
    private static void countIdleTicks(List<Dinosaur> animals, int[] idleTicks) {
        for (int i = 0; i < animals.size(); i++) {
            idleTicks[i] = animals.get(i).isIdle() ? idleTicks[i] + 1 : 0;
        }
    }

    /**
     * Spec herdCrossesForest: the followers (not the leader) whose navigation started a path since the last
     * call, seen as {@code isInProgress} going from false to true. Call once per tick; updates {@code pathing}.
     */
    private static int countPathStarts(List<Dinosaur> animals, boolean[] pathing) {
        int starts = 0;
        for (int i = 0; i < animals.size(); i++) {
            Dinosaur animal = animals.get(i);
            boolean now = animal.getNavigation().isInProgress();
            if (now && !pathing[i] && !animal.isGroupLeader()) {
                starts++;
            }
            pathing[i] = now;
        }
        return starts;
    }

    /** Spec herdPassesCorridor: no two followers share a target point (arc and lane offset). */
    private static void checkDistinctTargets(GameTestHelper helper, DinoGroup group, List<Dinosaur> animals) {
        for (int i = 0; i < animals.size(); i++) {
            double arc = group.targetArcOf(animals.get(i).getUUID());
            if (Double.isNaN(arc)) {
                continue;
            }
            double offset = group.laneOffsetOf(animals.get(i).getUUID());
            for (int j = i + 1; j < animals.size(); j++) {
                double otherArc = group.targetArcOf(animals.get(j).getUUID());
                if (!Double.isNaN(otherArc) && Math.abs(arc - otherArc) < SAME_TARGET
                        && Math.abs(offset - group.laneOffsetOf(animals.get(j).getUUID())) < SAME_TARGET) {
                    helper.fail("two followers share a target point", animals.get(i));
                }
            }
        }
    }

    /**
     * Pass condition of a journey that ends in GRAZE: the herd travelled, grazes again, its centre lies near
     * {@code goal} ({@link #centreNearGoal}) and every member is within R of the centre. A journey that ends
     * early (a partial path, a walk that was not re-issued) cannot pass.
     *
     * @param travelled set here once the herd is seen in TRAVEL
     */
    private static boolean arrivedTogether(GameTestHelper helper, DinoGroup group, List<Dinosaur> animals,
                                           BlockPos goal, boolean[] travelled) {
        travelled[0] |= group.state() == HerdState.TRAVEL;
        return travelled[0] && group.state() == HerdState.GRAZE && centreNearGoal(helper, group, goal)
                && allWithinHerdArea(group, animals);
    }

    /** Marks each member that is beyond relative x {@code endX}; {@code true} once every member has been. */
    private static boolean allPassed(GameTestHelper helper, List<Dinosaur> animals, boolean[] passed, double endX) {
        boolean all = true;
        for (int i = 0; i < animals.size(); i++) {
            passed[i] |= helper.relativeVec(animals.get(i).position()).x > endX;
            all &= passed[i];
        }
        return all;
    }

    /** The herd centre lies within {@link #GOAL_TOLERANCE} of the goal: the leader arrived there and the area recentred on it. */
    private static boolean centreNearGoal(GameTestHelper helper, DinoGroup group, BlockPos goal) {
        Vec3 centre = helper.relativeVec(new Vec3(group.centreX(), 0D, group.centreZ()));
        double dx = centre.x - (goal.getX() + 0.5D);
        double dz = centre.z - (goal.getZ() + 0.5D);
        return dx * dx + dz * dz <= GOAL_TOLERANCE * GOAL_TOLERANCE;
    }

    /** Every member within the herd radius of the herd's centre. */
    private static boolean allWithinHerdArea(DinoGroup group, List<Dinosaur> animals) {
        double radius = HerdSpacing.herdRadius(group, animals.get(0));
        for (Dinosaur animal : animals) {
            double dx = animal.getX() - group.centreX();
            double dz = animal.getZ() - group.centreZ();
            if (dx * dx + dz * dz > radius * radius) {
                return false;
            }
        }
        return true;
    }

    private static double horizontalDistance(Dinosaur a, Dinosaur b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Spawns an adult the way {@code IncubatedEggItem} hatches one, so it has its species data. */
    private static Dinosaur hatch(GameTestHelper helper, EntityType<? extends Dinosaur> type, BlockPos pos,
                                  double aggressionGene) {
        Dinosaur dinosaur = helper.spawn(type, pos);
        DinoData data = new DinoData();
        data.setBaseDino(type);
        data.setBasePercentage(100D);
        data.setGeneValue(GeneInit.GENDER.get(), 1D);
        data.setGeneValue(GeneInit.AGGRESSION.get(), aggressionGene);
        dinosaur.setDinoData(data);
        DinoBehaviourData behaviour = BehaviourDataReloadListener.getBehaviourInfoForDino(type);
        dinosaur.setDinoBehaviour(behaviour.toNBT(behaviour));
        DinoNeedsUtils.setDinoBaseNeeds(dinosaur, behaviour);
        DinoNeedsUtils.setCurrentHunger(dinosaur, 100F);
        DinoNeedsUtils.setCurrentStamina(dinosaur, (float) behaviour.maxStamina());
        return dinosaur;
    }
}
