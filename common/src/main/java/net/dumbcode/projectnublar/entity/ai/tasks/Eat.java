package net.dumbcode.projectnublar.entity.ai.tasks;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.BiPredicate;

public class Eat<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS =
            ObjectArrayList.of(Pair.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT),
                    Pair.of(MemoryModuleTypeInit.IS_HUNGRY.get(), MemoryStatus.VALUE_PRESENT));

    protected BiPredicate<E,? extends ItemEntity> targetPredicate = (dinosaur, foodItem) -> true ;

    private ItemEntity foodItem;

    public Eat(int delayTicks) {
        super(delayTicks);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E dinosaur) {
        foodItem = BrainUtils.getMemory(dinosaur, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);

        if(dinosaur.getDinoDiet().foodMap().containsKey(foodItem.getItem().getDescriptionId())){
            return dinosaur.distanceToSqr(foodItem) <= 5;
        } else return false;
    }

    @Override
    protected void doDelayedAction(E dinosaur) {
       if(foodItem != null) {
           ItemStack foodItemstack = foodItem.getItem();
           //Check to prevent bug where air sometimes ends up in the feed method.
           if(dinosaur.getDinoDiet().foodMap().containsKey(foodItemstack.getDescriptionId())) {
               DinoNeedsUtils.feed(dinosaur, foodItemstack.getDescriptionId());
           }
           foodItemstack.shrink(1);
       }
    }

    @Override
    protected void start(E dinosaur) {
      BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.IS_EATING.get(), true);
    }

    @Override
    protected void stop(E dinosaur) {
        BrainUtils.clearMemory(dinosaur, MemoryModuleTypeInit.IS_EATING.get());
        BrainUtils.clearMemory(dinosaur, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
    }

}
