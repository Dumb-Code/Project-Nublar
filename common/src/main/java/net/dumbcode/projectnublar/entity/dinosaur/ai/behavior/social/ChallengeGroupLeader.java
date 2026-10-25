package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

/**
 * A rank contest with no damage (plan B18). A follower near its leader takes the lead if its dominance
 * is more than the margin above the leader's. Only then do the two face each other, stop and roar
 * ({@code IS_ROARING}, which {@code ThreatDisplay} plays). A contest the challenger would lose never
 * starts, so equal animals stay quiet.
 * <p>
 * The leader is already the highest-ranked member when a group forms, so contests happen only when
 * growth or an edit changes the order. The range grows with body width, so large animals can contest too.
 * <p>
 * The face-off clears both walk targets. The wiring keeps them clear: {@code ThreatDisplay} runs before
 * {@code FollowFormation} in the core tasks, and while it plays, {@code isIdle()} is false, which
 * stops {@code FollowFormation} and the gated wander lines.
 * <p>
 * The memory list declares {@code IS_ROARING} and {@code LOOK_TARGET}. A brain ignores {@code setMemory}
 * for a memory type that no behaviour or sensor declares, so keep both lines if you move this code.
 */
public class ChallengeGroupLeader<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleTypeInit.IS_ROARING.get(), MemoryStatus.REGISTERED),
            Pair.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));

    /** Contest range in blocks, before the body-width allowance. */
    private static final double CONTEST_RANGE = 8D;
    /** How long the rivals look at each other: the length of the {@code ThreatDisplay} roar plus a margin. */
    private static final int FACE_OFF_TICKS = 40;

    private float margin = 0.1F;

    public ChallengeGroupLeader() {
        // One attempt a minute at most, with a random spread so a herd does not check in step.
        cooldownFor(entity -> 1200 + entity.getRandom().nextInt(1200));
    }

    /**
     * Dominance lead (0..1) the challenger needs to win. Default 0.1.
     *
     * @throws IllegalArgumentException if {@code margin} is negative, which would let the lead swap at every contest
     */
    public ChallengeGroupLeader<E> margin(float margin) {
        if (margin < 0F) {
            throw new IllegalArgumentException("margin must be at least 0, was " + margin);
        }
        this.margin = margin;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void start(E entity) {
        DinoGroup group = GroupManager.groupOf(entity);
        if (group == null) {
            return;
        }
        ServerLevel level = (ServerLevel) entity.level();
        Dinosaur leader = GroupManager.leaderOf(group, level);
        double range = CONTEST_RANGE + 2D * entity.getBbWidth();
        if (leader == null || leader == entity || !entity.isIdle() || !leader.isIdle()
                || entity.distanceToSqr(leader) > range * range
                || !group.challenge(entity.getUUID(), this.margin, GroupManager.roster(level))) {
            return;
        }
        faceOff(entity, leader);
        faceOff(leader, entity);
        // The deposed leader drops its own route (faceOff erased its walk target), so it does not walk
        // off to its old destination while the herd re-forms on the new leader.
        leader.getNavigation().stop();
    }

    private static void faceOff(Dinosaur dinosaur, Dinosaur rival) {
        BrainUtils.clearMemory(dinosaur, MemoryModuleType.WALK_TARGET);
        BrainUtils.setForgettableMemory(dinosaur, MemoryModuleType.LOOK_TARGET,
                new EntityTracker(rival, true), FACE_OFF_TICKS);
        BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.IS_ROARING.get(), true);
    }
}
