package net.dumbcode.projectnublar.entity.dinosaur.ai.task;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.minecraft.core.BlockPos;
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

public class WalkToNearestFeeder<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(3)
            .hasMemory(MemoryModuleTypeInit.IS_HUNGRY.get())
            .noMemory(MemoryModuleTypeInit.HUNTING.get())
            .hasMemory(MemoryModuleTypeInit.HAS_FOUND_FEEDER.get());


    protected BiPredicate<E, BlockPos> predicate = (entity, block) -> true;
    protected BiFunction<E, BlockPos, Float> speedMod = (owner, pos) -> 1f;
    protected BiFunction<E, BlockPos, Integer> closeEnoughDist = (entity, pos) -> 4;

    protected BlockPos target = null;
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
        return this.target != null;
    }

    @Override
    protected void start(E entity) {
        BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.target, this.speedMod.apply(entity, this.target), this.closeEnoughDist.apply(entity, this.target)));
        BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.target));
    }

    @Override
    protected void stop(E entity) {
        this.target = null;
    }

}
