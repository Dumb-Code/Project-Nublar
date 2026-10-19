package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Lead decision 8: during a journey a follower leaves the line to drink or eat only when the need is
 * critical, that is below the species' rage threshold of its maximum (0.4 for Gallimimus). Plan review
 * decision 17: on a roaming journey a follower starts a rest only when its stamina is critical (below
 * {@link #CRITICAL_STAMINA_FACTOR} of the rage threshold; a normal rest starts at the rage threshold), except
 * while the leader rests (P13a ruling Q1: a tired member may lie down then); on an urgent trip it rests
 * whenever it is tired. The leader rests at normal tiredness (P13a ruling 2: no critical gate for the leader;
 * its rest pauses the journey, {@link #isDown}). The leader and every grazing or groupless animal follow
 * their needs as before. In every state, a member does not start a rest while it steps aside for a make-way
 * ask ({@link #mayStartRest}).
 */
public final class HerdNeeds {

    /** Critical stamina on a roaming journey: below this fraction of the rage threshold (0.2 of the maximum for Gallimimus). */
    static final double CRITICAL_STAMINA_FACTOR = 0.5D;

    private HerdNeeds() {
    }

    static boolean isCritical(float current, float max, double threshold) {
        return current < max * threshold;
    }

    /**
     * P13a item 1: an animal is down while it sits down, rests or gets up. The memories cover the ticks the
     * animation states do not: {@code Rest} sets its resting memory before the sit state shows, and sets the
     * getting-up memory one tick before the get-up behaviour starts. {@code HerdLeading} pauses a roaming
     * journey while the leader is down; {@code FollowFormation} sees the pause.
     */
    static boolean isDown(boolean sitting, boolean resting, boolean rising, boolean restingMemory, boolean gettingUpMemory) {
        return sitting || resting || rising || restingMemory || gettingUpMemory;
    }

    /** {@link #isDown} for an animal. */
    static boolean isDown(Dinosaur dinosaur) {
        return isDown(dinosaur.isSitting(), dinosaur.isResting(), dinosaur.isRising(),
                BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get()),
                BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.GETTING_UP.get()));
    }

    /** The animal gets up from a rest: the get-up state, or the getting-up memory (fix round 1, item 2). */
    static boolean isRising(Dinosaur dinosaur) {
        return dinosaur.isRising() || BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.GETTING_UP.get());
    }

    /**
     * Makes the animal lie down: {@code Rest} starts on a later brain tick when the needs rule allows it
     * ({@link #mayRest}). In a day animal's night the rest lasts until morning, whatever its stamina; in the
     * daytime it ends at full stamina. P13a rulings Q1 and Q2: the leader on a roaming journey at night, and a
     * member at its rest spot while the herd rests. The group remembers an animal this marked tired (fix round
     * 1, item 5), so the mark is dropped again if it never rests ({@link #dropUnusedTired}).
     */
    static void lieDown(Dinosaur dinosaur, DinoGroup group) {
        if (!BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.IS_TIRED.get())) {
            BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.IS_TIRED.get(), true);
            group.markTired(dinosaur.getUUID());
        }
    }

    /**
     * Fix round 1, item 5: clears the tired memory that {@link #lieDown} set on an animal that never started
     * {@code Rest} (it is not down) and is not really tired, so it does not sit down later for nothing.
     */
    static void dropUnusedTired(Dinosaur dinosaur) {
        if (!isDown(dinosaur) && !DinoNeedsUtils.isTired(dinosaur)) {
            BrainUtils.clearMemory(dinosaur, MemoryModuleTypeInit.IS_TIRED.get());
        }
    }

    /** Start condition for the need walk-target behaviours in {@code Dinosaur.getCoreTasks}. */
    public static boolean mayLeaveForNeeds(Dinosaur dinosaur) {
        if (!isTravellingFollower(dinosaur, GroupManager.groupOf(dinosaur))) {
            return true;
        }
        double threshold = dinosaur.getDinoBehaviour().rageThreshold();
        return isCritical(DinoNeedsUtils.getCurrentThirst(dinosaur), DinoNeedsUtils.getMaxThirst(dinosaur), threshold)
                || isCritical(DinoNeedsUtils.getCurrentHunger(dinosaur), DinoNeedsUtils.getMaxHunger(dinosaur), threshold);
    }

    /**
     * Playtest 4 fix P12b, fix round 2: a rest may start only when no make-way side step is in progress
     * ({@code HerdMoveControl.sideStepping}, the wait through its get-up included) and the needs allow it.
     * The leader's ask gets a resting member up ({@link HerdBodies#answersMakeWay}); a member that is still
     * tired would otherwise start {@code Rest} again as its get-up ends and lie down in the leader's line
     * before it steps aside, and the leader would wait again and again. Once the step ends, it may rest
     * where it stands, out of the line.
     *
     * @param sideStepping a make-way side step is in progress
     * @param needsAllow   the needs rule allows a rest ({@link #mayRest})
     */
    static boolean mayStartRest(boolean sideStepping, boolean needsAllow) {
        return !sideStepping && needsAllow;
    }

    /**
     * Start condition for {@code Rest} in {@code Dinosaur.getIdleTasks}: a rest begun before a journey ends on
     * its own. Never while a make-way side step is in progress ({@link #mayStartRest}).
     */
    public static boolean mayRest(Dinosaur dinosaur) {
        boolean sideStepping = dinosaur.getMoveControl() instanceof HerdMoveControl control && control.sideStepping();
        return mayStartRest(sideStepping, needsAllowRest(dinosaur));
    }

    /**
     * The needs part of {@link #mayRest}: a travelling follower rests only when its stamina is critical, except
     * on an urgent trip and while the leader rests on a roaming journey (P13a ruling Q1: a tired member lies
     * down in its slot, or at its rest spot once the herd spreads out).
     */
    private static boolean needsAllowRest(Dinosaur dinosaur) {
        DinoGroup group = GroupManager.groupOf(dinosaur);
        // A drag is urgent but has no need behind it: followers rest only when critical, as on a roaming journey.
        if (!isTravellingFollower(dinosaur, group) || group.journeyKind() == JourneyPolicy.Kind.URGENT && !group.dragged()
                || group.pause() == JourneyPolicy.Pause.REST) {
            return true;
        }
        double threshold = dinosaur.getDinoBehaviour().rageThreshold() * CRITICAL_STAMINA_FACTOR;
        return isCritical(DinoNeedsUtils.getCurrentStamina(dinosaur), DinoNeedsUtils.getMaxStamina(dinosaur), threshold);
    }

    private static boolean isTravellingFollower(Dinosaur dinosaur, @Nullable DinoGroup group) {
        return group != null && group.state() == HerdState.TRAVEL && !group.isLeader(dinosaur.getUUID());
    }
}
