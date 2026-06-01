package net.dumbcode.projectnublar.entity.ai.tasks;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.Predicate;

public class SetHunting<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleTypeInit.IS_HUNGRY.get(), MemoryStatus.VALUE_PRESENT),
            Pair.of(MemoryModuleTypeInit.IS_DEHYDRATED.get(),MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleTypeInit.HUNTING.get(), MemoryStatus.VALUE_ABSENT));

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    protected Predicate<E> canStartHuntingPredicate = (dinosaur) -> dinosaur instanceof CarnivoreDinosaur && DinoNeedsUtils.isHungry(dinosaur);

    public SetHunting<E> canStartHuntingPredicate(final Predicate<E> predicate){
        this.canStartHuntingPredicate = predicate;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {

        if(entity.hasGroup()){
            return false;
        }

        //Check for feeder first before starting hunt
        if(BrainUtils.hasMemory(entity, MemoryModuleTypeInit.HAS_FOUND_FEEDER.get())){
            BlockEntity block = level.getBlockEntity(BrainUtils.getMemory(entity,MemoryModuleTypeInit.HAS_FOUND_FEEDER.get()));
            if(block instanceof DinosaurFeederBlockEntity fbe){
                return !fbe.shouldDisplayFood;
            }
        }

        if(BrainUtils.hasMemory(entity, MemoryModuleTypeInit.HUNTING.get())){
            return false;
        }

        return !entity.isChild() && !entity.isHuntingBlocked();
    }

    @Override
    protected void start(E entity) {
        BrainUtils.setMemory(entity, MemoryModuleTypeInit.HUNTING.get(), true);
    }
}
