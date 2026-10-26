package net.dumbcode.projectnublar.entity.dinosaur.ai.task;

import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;
import java.util.function.Predicate;

public class SetHunting<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(3)
            .hasMemory(MemoryModuleTypeInit.IS_HUNGRY.get())
            .noMemory(MemoryModuleTypeInit.IS_DEHYDRATED.get())
            .noMemory(MemoryModuleTypeInit.HUNTING.get());

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    protected Predicate<E> canStartHuntingPredicate = (dinosaur) -> dinosaur instanceof CarnivoreDinosaur && DinoNeedsUtils.isHungry(dinosaur);

    public SetHunting<E> canStartHuntingPredicate(final Predicate<E> predicate){
        this.canStartHuntingPredicate = predicate;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {

        //Check for feeder first before starting hunt
        if(BrainUtil.hasMemory(entity, MemoryModuleTypeInit.HAS_FOUND_FEEDER.get())){
            BlockEntity block = level.getBlockEntity(BrainUtil.getMemory(entity,MemoryModuleTypeInit.HAS_FOUND_FEEDER.get()));
            if(block instanceof DinosaurFeederBlockEntity fbe){
                return !fbe.shouldDisplayFood;
            }
        }

        if(BrainUtil.hasMemory(entity, MemoryModuleTypeInit.HUNTING.get())){
            return false;
        }

        return !entity.isChild() && !entity.isHuntingBlocked();
    }

    @Override
    protected void start(E entity) {
        BrainUtil.setMemory(entity, MemoryModuleTypeInit.HUNTING.get(), true);
    }
}
