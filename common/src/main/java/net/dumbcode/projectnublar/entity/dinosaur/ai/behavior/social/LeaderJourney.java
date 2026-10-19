package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The herd leader's own moves while the herd grazes (spec "Leader", lead decision 3), in place of the old
 * random wander:
 * <ul>
 *   <li><b>Graze move</b> every 400-800 ticks: walk 3-8 blocks to a free spot inside the herd area that the
 *       clearance box reaches in a straight line, at {@link JourneyPolicy#GRAZE_MOVE_SPEED}. The herd state
 *       does not change, so no follower moves because of it (T3).</li>
 *   <li><b>Journey</b> every 2400-6000 ticks: a heading and a distance of 156-314 blocks ({@link #journeyTarget};
 *       the target's chunk need not be loaded), walked in legs ({@link #nextLeg}) at
 *       {@link JourneyPolicy#JOURNEY_SPEED}. TRAVEL starts at once ({@link DinoGroup#startJourney}). A try whose
 *       first leg is not usable is tried again after {@link JourneyPolicy#JOURNEY_RETRY_TICKS}, as is a try in the
 *       rest hours (from sunset for a day animal) or with less than 75% stamina ({@link JourneyPolicy#roamMayStart},
 *       P13a rulings Q2 and Q3) or on a lead.</li>
 * </ul>
 * A leashed leader picks no graze moves (P13a ruling 9): the lead decides where it goes.
 * {@link HerdLeading} runs the journey after it starts. Needs (water, food) start urgent journeys there.
 * Leader only; it runs only while the herd grazes and the leader is idle and has no walk target.
 */
public class LeaderJourney<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));

    /** A journey's walk target counts as reached this close (Manhattan, block positions). */
    static final int CLOSE_ENOUGH = 2;
    /** A graze move ends only in the spot's own block: the spot is reserved, so the leader should stand on it. */
    static final int GRAZE_MOVE_CLOSE_ENOUGH = 0;
    static final int COOLDOWN_TICKS = 20;

    public LeaderJourney() {
        cooldownFor(entity -> COOLDOWN_TICKS);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        DinoGroup group = GroupManager.groupOf(entity);
        if (group == null || !group.isLeader(entity.getUUID()) || !entity.isIdle()
                || BrainUtils.hasMemory(entity, MemoryModuleTypeInit.BRAIN_OVERRIDE.get())
                || group.state() != HerdState.GRAZE || !group.hasCentre()) {
            return false;
        }
        long now = level.getGameTime();
        schedule(group, entity, now);
        return !group.grazePinned() && now >= group.nextJourneyTick() || now >= group.nextGrazeMoveTick();
    }

    @Override
    protected void start(E entity) {
        ServerLevel level = (ServerLevel) entity.level();
        DinoGroup group = GroupManager.groupOf(entity);
        if (group == null) {
            return;
        }
        long now = level.getGameTime();
        RandomSource random = entity.getRandom();
        if (!group.grazePinned() && now >= group.nextJourneyTick()) {
            // A leashed leader's ROAM journey would only become a drag (P8c), and P13a rulings Q2 and Q3 start
            // none in the rest hours or below 75% stamina: each a failed try, retried as usual (no probes).
            boolean mayStart = !entity.isLeashed() && JourneyPolicy.roamMayStart(
                    JourneyPolicy.restHours(level.getDayTime(), entity.getDinoBehaviour().isNocturnal()),
                    DinoNeedsUtils.getCurrentStamina(entity), DinoNeedsUtils.getMaxStamina(entity));
            Vec3 target = mayStart ? journeyTarget(level, entity, group) : null;
            boolean started = target != null && begin(entity, target, JourneyPolicy.Kind.ROAM);
            // A try that found no target is retried soon, not a whole interval later (playtest 1, R3).
            group.setNextJourneyTick(JourneyPolicy.nextJourneyAfterTry(now, random.nextDouble(), started));
            if (started) {
                return;
            }
        }
        if (now >= group.nextGrazeMoveTick()) {
            group.setNextGrazeMoveTick(JourneyPolicy.nextGrazeMove(now, random.nextDouble()));
            // P13a ruling 9: a leashed leader picks no graze move of its own (it would pull against the lead).
            Vec3 spot = entity.isLeashed() ? null : grazeMove(level, entity, group);
            if (spot != null) {
                group.reserveSpot(entity.getUUID(), spot.x, spot.z);
                BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET,
                        new WalkTarget(spot, JourneyPolicy.GRAZE_MOVE_SPEED, GRAZE_MOVE_CLOSE_ENOUGH));
            }
        }
    }

    /** Starts a roaming journey of the leader's herd toward {@code target}. Public for GameTests. */
    public static boolean beginRoam(Dinosaur leader, Vec3 target) {
        return begin(leader, target, JourneyPolicy.Kind.ROAM);
    }

    /**
     * Starts a journey: TRAVEL from this tick with the followers ordered nearest-first, and (for a roaming
     * journey) the leader's call and the first leg's walk target ({@link HerdLeading#walkNextLeg}); the walk
     * waits for the call (fix P10). An urgent journey keeps the leader's own walk target (the need's).
     * {@code target.y} is NaN for a far target with no ground height yet (fix P11).
     *
     * @return {@code false} if the animal does not lead a herd or the herd is pinned to grazing
     */
    static boolean begin(Dinosaur leader, Vec3 target, JourneyPolicy.Kind kind) {
        if (!(leader.level() instanceof ServerLevel level)) {
            return false;
        }
        DinoGroup group = GroupManager.groupOf(leader);
        if (group == null) {
            return false;
        }
        // A change of lead not settled yet would erase the walk target set below (GameTests start journeys
        // from outside the animals' ticks).
        HerdLeading.settleLeaderChange(level, group);
        if (!group.isLeader(leader.getUUID())
                || !group.startJourney(kind, target.x, target.y, target.z, HerdLeading.followersByDistance(level, group, leader), level.getGameTime())) {
            return false;
        }
        // Fix P10: on a roaming journey the leader calls first; the first walk waits for the call (at most 60
        // ticks) and HerdLeading.travel sets it when the wait ends. A species without a call sets off at once.
        // No usable first leg here leaves the walk ended: HerdLeading.travel ends the journey as an arrival.
        if (kind == JourneyPolicy.Kind.ROAM && leader.oneShots().beginJourneyCall() == 0) {
            HerdLeading.walkNextLeg(level, leader, group);
        }
        return true;
    }

    private static void schedule(DinoGroup group, Dinosaur leader, long now) {
        if (group.nextJourneyTick() == DinoGroup.UNSET) {
            group.setNextJourneyTick(JourneyPolicy.nextJourney(now, leader.getRandom().nextDouble()));
        }
        if (group.nextGrazeMoveTick() == DinoGroup.UNSET) {
            group.setNextGrazeMoveTick(JourneyPolicy.nextGrazeMove(now, leader.getRandom().nextDouble()));
        }
    }

    /**
     * A journey target in a random direction, {@link JourneyPolicy#JOURNEY_MIN}-{@link JourneyPolicy#JOURNEY_MAX}
     * blocks away (playtest fix P11): a heading and a horizontal distance only, with no height (NaN).
     * <p>
     * What the pick reads: only the first leg's points, never the target. The target is not read, and its chunk
     * need not be loaded. A heading counts only if its first leg is usable, by the same choice as every later
     * leg ({@link JourneyPolicy#chooseLeg}): the point {@link JourneyPolicy#LEG_LENGTH} (24) blocks straight
     * ahead, then the same distance at +20, -20, +40 and -40 degrees, so a tree or a pond straight ahead does not
     * throw the heading away. Each point goes through {@link BodyFit#surfaceStandY}, which first checks that the
     * point's chunk is loaded and entity-ticking and reads no block if it is not, and a point more than
     * {@link JourneyPolicy#JOURNEY_MAX_HEIGHT_DIFF} above or below the leader does not count (lead ruling 14). So
     * a leader with water, walls or cliffs on every side does not start a journey that would end at once.
     * <p>
     * Cost: at most {@link JourneyPolicy#JOURNEY_ATTEMPTS} x {@link JourneyPolicy#LEG_TRIES} = 50 surface probes,
     * once per try (a try comes every 2400-6000 ticks, or {@link JourneyPolicy#JOURNEY_RETRY_TICKS} after a
     * failed one); usually 1. The probes are cached in the herd's {@link BodyFit}, so a first walk set at once
     * ({@link #begin}, a species with no journey call) finds the same point in the cache. {@code null} after all
     * tries fail.
     */
    private static @Nullable Vec3 journeyTarget(ServerLevel level, Dinosaur leader, DinoGroup group) {
        ClearanceBox box = BodyFit.boxOf(leader);
        BodyFit fit = group.fit();
        RandomSource random = leader.getRandom();
        JourneyPolicy.StandHeight stand = (x, z) -> fit.surfaceStandY(level, leader, box, x, z);
        for (int attempt = 0; attempt < JourneyPolicy.JOURNEY_ATTEMPTS; attempt++) {
            HerdArea.Point target = HerdArea.sampleRing(leader.getX(), leader.getZ(), JourneyPolicy.JOURNEY_MIN,
                    JourneyPolicy.JOURNEY_MAX, random.nextDouble(), random.nextDouble());
            // The target is at least JOURNEY_MIN away, so this probes first-leg points only (never the target).
            if (JourneyPolicy.chooseLeg(leader.getX(), leader.getY(), leader.getZ(), target.x(), target.z(), Double.NaN, stand) != null) {
                return new Vec3(target.x(), Double.NaN, target.z());
            }
        }
        return null;
    }

    /**
     * The walk target of the roaming journey's next leg (playtest fix P11, {@link JourneyPolicy#chooseLeg}), from
     * where the leader stands now, or {@code null} if no leg point is usable. Each point is resolved with the
     * leader's clearance box on the surface ({@link BodyFit#surfaceStandY}: nothing is read unless the point's
     * chunk is loaded and entity-ticking; ground height, hazards and water; at most
     * {@link JourneyPolicy#JOURNEY_MAX_HEIGHT_DIFF} above or below the leader, lead ruling 14). The last leg, once the target is
     * within {@link JourneyPolicy#LEG_LENGTH}, resolves the target: the spot it found becomes the journey target
     * ({@link DinoGroup#retarget}), so arrival is measured to a spot the leader can stand on.
     */
    static @Nullable Vec3 nextLeg(ServerLevel level, Dinosaur leader, DinoGroup group) {
        ClearanceBox box = BodyFit.boxOf(leader);
        BodyFit fit = group.fit();
        JourneyPolicy.Leg leg = JourneyPolicy.chooseLeg(leader.getX(), leader.getY(), leader.getZ(), group.journeyX(), group.journeyZ(),
                group.journeyY(), (x, z) -> fit.surfaceStandY(level, leader, box, x, z));
        if (leg == null) {
            return null;
        }
        if (leg.last()) {
            group.retarget(leg.x(), leg.y(), leg.z());
        }
        return new Vec3(leg.x(), leg.y(), leg.z());
    }

    /** A free spot 3-8 blocks away inside the herd area, reachable in a straight line, or {@code null}. */
    private static @Nullable Vec3 grazeMove(ServerLevel level, Dinosaur leader, DinoGroup group) {
        double width = leader.getBbWidth();
        double personalSpace = HerdSpacing.personalSpace(width, width);
        double spacing = HerdArea.grazeSpacing(personalSpace);
        double radius = group.radius(personalSpace, width);
        List<HerdArea.Point> reserved = group.reservedSpotsExcept(leader.getUUID());
        ClearanceBox box = BodyFit.boxOf(leader);
        BodyFit fit = group.fit();
        RandomSource random = leader.getRandom();
        for (int attempt = 0; attempt < HerdArea.SPOT_CANDIDATES; attempt++) {
            HerdArea.Point point = HerdArea.sampleRing(leader.getX(), leader.getZ(), JourneyPolicy.GRAZE_MOVE_MIN,
                    JourneyPolicy.GRAZE_MOVE_MAX, random.nextDouble(), random.nextDouble());
            if (!HerdArea.inside(group.centreX(), group.centreZ(), radius, point.x(), point.z())
                    || HerdArea.roominess(point, reserved) <= spacing) {
                continue;
            }
            double y = fit.reachY(level, leader, box, leader.getX(), leader.getY(), leader.getZ(), point.x(), point.z());
            if (!Double.isNaN(y)) {
                return new Vec3(point.x(), y, point.z());
            }
        }
        return null;
    }
}
