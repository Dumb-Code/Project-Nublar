package net.dumbcode.projectnublar.entity.dinosaur.ai.task;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.Predicate;


public class Drink<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleTypeInit.HAS_FOUND_WATER.get(), MemoryStatus.VALUE_PRESENT),Pair.of(MemoryModuleTypeInit.IS_THIRSTY.get(), MemoryStatus.VALUE_PRESENT));

    protected Predicate<? extends BlockState> targetPredicate = (blockState) -> true;
    protected Predicate<E> canTargetPredicate = (dinosaur) -> true;

    public Drink(int delayTicks) {
        super(delayTicks);
    }


    public Drink<E> targetPredicate(final Predicate<BlockState> predicate) {
        this.targetPredicate = predicate; return this;
    }
    public Drink<E> canTargetPredicate(final Predicate<E> predicate) {
        this.canTargetPredicate = predicate; return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E dinosaur) {
        BlockPos nearestWaterSource = BrainUtils.getMemory(dinosaur, MemoryModuleTypeInit.HAS_FOUND_WATER.get());

        if(dinosaur.distanceToSqr(nearestWaterSource.getCenter()) > 5){
            return false;
        }

        return !BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.IS_DRINKING.get());

    }

    @Override
    protected void start(E dinosaur) {
        BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.IS_DRINKING.get(), true);
    }

    @Override
    protected void doDelayedAction(E dinosaur) {
        BrainUtils.clearMemory(dinosaur, MemoryModuleTypeInit.IS_DRINKING.get());
        DinoNeedsUtils.drink(dinosaur);
    }

    @Override
    protected void stop(E entity) {
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.IS_DRINKING.get());
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.IS_THIRSTY.get());
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.HAS_FOUND_WATER.get());
    }
}
