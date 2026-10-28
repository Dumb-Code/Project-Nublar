package net.dumbcode.projectnublar.entity.dinosaur.ai.task;

import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * A hungry animal walks to the feeder its sensor found, if the feeder holds food it eats (meat for a meat-eater).
 * The walk target is set once per walk, to the block beside the feeder on the animal's side (a path to the feeder block
 * itself ends on its top), and the walk ends when the animal is in eating reach ({@link EatFromMeatFeeder#inReach}).
 * Before Task 41 every animal that had seen any feeder walked to it, the walk target was set again every tick, and the
 * walk ended 3 blocks out, where eating failed, so animals stood at the feeder and held their herd there.
 */
public class WalkToNearestFeeder<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(3)
            .hasMemory(MemoryModuleTypeInit.IS_HUNGRY.get())
            .noMemory(MemoryModuleTypeInit.HUNTING.get())
            .hasMemory(MemoryModuleTypeInit.HAS_FOUND_FEEDER.get());


    protected BiPredicate<E, BlockPos> predicate = (entity, block) -> true;
    protected BiFunction<E, BlockPos, Float> speedMod = (owner, pos) -> 1f;
    /** The walk's own close-enough distance: 0, so the walk goes on until the animal is in eating reach. */
    protected BiFunction<E, BlockPos, Integer> closeEnoughDist = (entity, pos) -> 0;

    protected BlockPos target = null;
    /** The block this walk heads for, beside {@link #target}. */
    protected BlockPos spot = null;

    public WalkToNearestFeeder() {
        // It runs for the whole walk and ends by shouldKeepRunning, however long the walk takes.
        noTimeout();
    }
    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }
    public WalkToNearestFeeder<E> predicate(final BiPredicate<E, BlockPos> predicate) {
        this.predicate = predicate;

        return this;
    }

    public WalkToNearestFeeder<E> speedMod(BiFunction<E, BlockPos, Float> speedModifier) {
        this.speedMod = speedModifier;

        return this;
    }

    public WalkToNearestFeeder<E> closeEnoughWhen(final BiFunction<E, BlockPos, Integer> function) {
        this.closeEnoughDist = function;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.target = BrainUtil.getMemory(entity, MemoryModuleTypeInit.HAS_FOUND_FEEDER.get());
        // No walk to a feeder without food this animal eats, and none when it is in reach already (it eats).
        return this.target != null && level.getBlockEntity(this.target) instanceof DinosaurFeederBlockEntity feeder
                && feeder.holdsFoodFor(entity) && !EatFromMeatFeeder.inReach(entity, this.target);
    }

    @Override
    protected void start(E entity) {
        this.spot = standingSpot(this.target, entity);
        BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.spot, this.speedMod.apply(entity, this.target), this.closeEnoughDist.apply(entity, this.target)));
        BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.target));
    }

    /** The walk goes on while the animal still wants food, is not yet in reach, and the walk target is still this walk's. */
    @Override
    protected boolean shouldKeepRunning(E entity) {
        WalkTarget walkTarget = BrainUtil.getMemory(entity, MemoryModuleType.WALK_TARGET);
        return walkTarget != null && walkTarget.getTarget().currentBlockPosition().equals(this.spot)
                && DinoNeedsUtils.isHungry(entity) && !EatFromMeatFeeder.inReach(entity, this.target);
    }

    /**
     * In reach, the walk target goes, so the animal stops beside the feeder and eats. A walk cut short (another walk
     * took over, the path ended away from the feeder, or the animal is no longer hungry) forgets the feeder too, so a
     * new walk waits for the sensor's next scan instead of starting again on the next tick.
     */
    @Override
    protected void stop(E entity) {
        WalkTarget walkTarget = BrainUtil.getMemory(entity, MemoryModuleType.WALK_TARGET);
        if (walkTarget != null && walkTarget.getTarget().currentBlockPosition().equals(this.spot)) {
            BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
        }
        if (!EatFromMeatFeeder.inReach(entity, this.target)) {
            BrainUtil.clearMemory(entity, MemoryModuleTypeInit.HAS_FOUND_FEEDER.get());
        }
        this.target = null;
        this.spot = null;
    }

    /** The block beside the feeder on the animal's side. */
    private static BlockPos standingSpot(BlockPos feeder, Dinosaur dinosaur) {
        Direction side = Direction.getApproximateNearest(dinosaur.getX() - (feeder.getX() + 0.5D), 0D,
                dinosaur.getZ() - (feeder.getZ() + 0.5D));
        return feeder.relative(side);
    }

}
