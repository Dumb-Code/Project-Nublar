package net.dumbcode.projectnublar.entity.dinosaur.ai.task;

import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;
import java.util.function.BiPredicate;

public class EatFromMeatFeeder<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS =
            MemoryTest.builder(2).hasMemory(MemoryModuleType.WALK_TARGET)
                    .hasMemory(MemoryModuleTypeInit.IS_HUNGRY.get());

    protected BiPredicate<E,? extends DinosaurFeederBlockEntity> targetPredicate = (dinosaur, feeder) -> true ;

    private DinosaurFeederBlockEntity feeder;

    public EatFromMeatFeeder(int delayTicks) {
        super(delayTicks);
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E dinosaur) {

        if(level.getBlockEntity(BrainUtil.getMemory(dinosaur,MemoryModuleType.WALK_TARGET).getTarget().currentBlockPosition()) instanceof DinosaurFeederBlockEntity fbe) {
            feeder = fbe;
            return dinosaur.distanceToSqr(Vec3.atCenterOf(feeder.getBlockPos())) <=2;
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
        BrainUtil.clearMemory(dinosaur,MemoryModuleTypeInit.HAS_FOUND_FEEDER.get());
      BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.IS_EATING.get(), true);
    }

    @Override
    protected void stop(E dinosaur) {

        BrainUtil.clearMemory(dinosaur,MemoryModuleTypeInit.HAS_FOUND_FEEDER.get());
        BrainUtil.clearMemory(dinosaur,MemoryModuleTypeInit.IS_EATING.get());
        BrainUtil.clearMemory(dinosaur, MemoryModuleType.WALK_TARGET);
        BrainUtil.clearMemory(dinosaur, MemoryModuleType.LOOK_TARGET);
    }

}
