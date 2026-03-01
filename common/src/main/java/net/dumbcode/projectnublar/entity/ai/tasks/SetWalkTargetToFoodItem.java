package net.dumbcode.projectnublar.entity.ai.tasks;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class SetWalkTargetToFoodItem<E extends AbstractDinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT),
            Pair.of(MemoryModuleTypeInit.IS_HUNGRY.get(), MemoryStatus.VALUE_PRESENT));


    protected BiPredicate<E, ItemEntity> predicate = (entity, item) -> true;
    protected BiFunction<E, BlockPos, Float> speedMod = (owner, pos) -> 1f;
    protected BiFunction<E, BlockPos, Integer> closeEnoughDist = (entity, pos) -> 2;

    protected ItemEntity target = null;
    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    public SetWalkTargetToFoodItem<E> predicate(final BiPredicate<E, ItemEntity> predicate) {
        this.predicate = predicate;

        return this;
    }

    public SetWalkTargetToFoodItem<E> speedMod(BiFunction<E, BlockPos, Float> speedModifier) {
        this.speedMod = speedModifier;

        return this;
    }

    public SetWalkTargetToFoodItem<E> closeEnoughWhen(final BiFunction<E, BlockPos, Integer> function) {
        this.closeEnoughDist = function;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.target = BrainUtils.getMemory(entity, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
        return this.target != null;
    }

    @Override
    protected void start(E entity) {
        BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.target, this.speedMod.apply(entity, this.target.blockPosition()), this.closeEnoughDist.apply(entity, this.target.blockPosition())));
        BrainUtils.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.target.blockPosition()));
    }

}
