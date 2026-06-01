package net.dumbcode.projectnublar.entity.ai.tasks;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.BiPredicate;

public class EatFromMeatFeeder<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS =
            ObjectArrayList.of(Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT),
                    Pair.of(MemoryModuleTypeInit.IS_HUNGRY.get(), MemoryStatus.VALUE_PRESENT));

    protected BiPredicate<E,? extends DinosaurFeederBlockEntity> targetPredicate = (dinosaur, feeder) -> true ;

    private DinosaurFeederBlockEntity feeder;

    public EatFromMeatFeeder(int delayTicks) {
        super(delayTicks);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E dinosaur) {

        if(level.getBlockEntity(BrainUtils.getMemory(dinosaur,MemoryModuleType.WALK_TARGET).getTarget().currentBlockPosition()) instanceof DinosaurFeederBlockEntity fbe) {
            feeder = fbe;
            return dinosaur.distanceToSqr(feeder.getBlockPos().getCenter()) <=2;
        } else return false;
    }

    @Override
    protected void doDelayedAction(E dinosaur) {
       if(feeder != null) {
           if((dinosaur.getDinoDiet().foodMap().containsKey(feeder.getItem(0)))
                   || (dinosaur.getDinoDiet().foodMap().containsKey(feeder.getItem(1)))
                   || (dinosaur.getDinoDiet().foodMap().containsKey(feeder.getItem(2)))) {
               double foodIncreasePerUnit = 0;

               if(!feeder.getItem(0).isEmpty()) {
                   foodIncreasePerUnit = dinosaur.getDinoDiet().foodMap().get(feeder.getItem(0));
                   for (double i = DinoNeedsUtils.getCurrentHunger(dinosaur); i < DinoNeedsUtils.getMaxHunger(dinosaur); i += foodIncreasePerUnit) {
                       feeder.removeItem(0, 1);
                       if(feeder.getItem(0).isEmpty()) {return;}
                       if (DinoNeedsUtils.getCurrentHunger(dinosaur) >= DinoNeedsUtils.getMaxHunger(dinosaur)) {
                           DinoNeedsUtils.setCurrentHunger(dinosaur, DinoNeedsUtils.getMaxHunger(dinosaur));
                           return;
                       }

                   }
               } else if(!feeder.getItem(1).isEmpty()) {
                   foodIncreasePerUnit = dinosaur.getDinoDiet().foodMap().get(feeder.getItem(1));
                   for (double i = DinoNeedsUtils.getCurrentHunger(dinosaur); i < DinoNeedsUtils.getMaxHunger(dinosaur); i += foodIncreasePerUnit) {
                       feeder.removeItem(1, 1);
                       if(feeder.getItem(1).isEmpty()) {return;}
                       if (DinoNeedsUtils.getCurrentHunger(dinosaur) >= DinoNeedsUtils.getMaxHunger(dinosaur)) {
                           DinoNeedsUtils.setCurrentHunger(dinosaur, DinoNeedsUtils.getMaxHunger(dinosaur));
                           return;
                       }
                   }
               } else if(!feeder.getItem(2).isEmpty()) {
                   foodIncreasePerUnit = dinosaur.getDinoDiet().foodMap().get(feeder.getItem(2));
                   for (double i = DinoNeedsUtils.getCurrentHunger(dinosaur); i < DinoNeedsUtils.getMaxHunger(dinosaur); i += foodIncreasePerUnit) {
                       feeder.removeItem(2, 1);
                       if(feeder.getItem(2).isEmpty()) {return;}
                       if (DinoNeedsUtils.getCurrentHunger(dinosaur) >= DinoNeedsUtils.getMaxHunger(dinosaur)) {
                           DinoNeedsUtils.setCurrentHunger(dinosaur, DinoNeedsUtils.getMaxHunger(dinosaur));
                           return;
                       }
                   }
               } else return;
           }
       }
    }

    @Override
    protected void start(E dinosaur) {
        BrainUtils.clearMemory(dinosaur,MemoryModuleTypeInit.HAS_FOUND_FEEDER.get());
      BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.IS_EATING.get(), true);
    }

    @Override
    protected void stop(E dinosaur) {

        BrainUtils.clearMemory(dinosaur,MemoryModuleTypeInit.HAS_FOUND_FEEDER.get());
        BrainUtils.clearMemory(dinosaur,MemoryModuleTypeInit.IS_EATING.get());
        BrainUtils.clearMemory(dinosaur, MemoryModuleType.WALK_TARGET);
        BrainUtils.clearMemory(dinosaur, MemoryModuleType.LOOK_TARGET);
    }

}
