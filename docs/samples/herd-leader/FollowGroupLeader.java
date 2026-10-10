package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

/**
 * Group cohesion: a follower that strays farther than the cohesion radius from its leader walks back.
 * <p>
 * It runs only when the animal has no walk target, so food, water and fight targets always win.
 * Inside the radius the follower is left alone and wanders on its own, which keeps the herd loose.
 * It also waits while the animal is not idle (eating, resting, roaring in a contest).
 * The radius grows with body width, so it measures roughly the same gap between bodies for every species.
 * <p>
 * The lookup is in {@link #start}, not in the start check: SmartBrainLib applies the cooldown only
 * after a behaviour runs, so a failing check would repeat every tick.
 */
public class FollowGroupLeader<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));

    private float cohesionRadius = 12F;
    private float speedModifier = 1F;

    public FollowGroupLeader() {
        cooldownFor(entity -> 20);
    }

    /** How far, in blocks, a follower may stray before it walks back, before the body-width allowance. Default 12. */
    public FollowGroupLeader<E> cohesionRadius(float radius) {
        this.cohesionRadius = radius;
        return this;
    }

    public FollowGroupLeader<E> speedModifier(float modifier) {
        this.speedModifier = modifier;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void start(E entity) {
        DinoGroup group = GroupManager.groupOf(entity);
        if (group == null || !entity.isIdle()) {
            return;
        }
        Dinosaur leader = GroupManager.leaderOf(group, (ServerLevel) entity.level());
        double radius = this.cohesionRadius + 2D * entity.getBbWidth();
        if (leader == null || leader == entity || entity.distanceToSqr(leader) <= radius * radius) {
            return;
        }
        // Stop at half the radius, so the follower rejoins the herd and does not crowd the leader.
        BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET,
                new WalkTarget(leader, this.speedModifier, (int) (radius / 2D)));
    }
}
