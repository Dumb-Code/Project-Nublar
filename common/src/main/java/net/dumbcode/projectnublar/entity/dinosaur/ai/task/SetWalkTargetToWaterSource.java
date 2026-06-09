package net.dumbcode.projectnublar.entity.dinosaur.ai.task;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;


import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class SetWalkTargetToWaterSource<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleTypeInit.HAS_FOUND_WATER.get(), MemoryStatus.VALUE_PRESENT),
            Pair.of(MemoryModuleTypeInit.IS_THIRSTY.get(), MemoryStatus.VALUE_PRESENT));


    protected BiPredicate<E, BlockPos> predicate = (entity, block) -> true;
    protected BiFunction<E, BlockPos, Float> speedMod = (owner, pos) -> 1f;
    protected BiFunction<E, BlockPos, Integer> closeEnoughDist = (entity, pos) -> 4;

    protected BlockPos target = null;
    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }
    public SetWalkTargetToWaterSource<E> predicate(final BiPredicate<E, BlockPos> predicate) {
        this.predicate = predicate;

        return this;
    }

    public SetWalkTargetToWaterSource<E> speedMod(BiFunction<E, BlockPos, Float> speedModifier) {
        this.speedMod = speedModifier;

        return this;
    }

    public SetWalkTargetToWaterSource<E> closeEnoughWhen(final BiFunction<E, BlockPos, Integer> function) {
        this.closeEnoughDist = function;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.target = BrainUtils.getMemory(entity, MemoryModuleTypeInit.HAS_FOUND_WATER.get());
        return this.target != null;
    }

    @Override
    protected void start(E entity) {
        BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.target, this.speedMod.apply(entity, this.target), this.closeEnoughDist.apply(entity, this.target)));
        BrainUtils.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.target));
    }

    @Override
    protected void stop(E entity) {
        this.target = null;
    }

}
