package net.dumbcode.projectnublar.entity.dinosaur.ai.task;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;


// TODO(DEAD): not wired into any activity group; turf wars never start. Left unwired on purpose.
public class StartTurfWar<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private Random random = new Random();

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MemoryTest.builder(2).noMemory(MemoryModuleTypeInit.INITIATED_TURF_WAR.get())
                .hasMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
    }

    protected Predicate<LivingEntity> canAttackPredicate = LivingEntity::isAlive;
    protected LivingEntity toTarget = null;
    protected MemoryModuleType<? extends LivingEntity> priorityTargetMemory = MemoryModuleType.NEAREST_ATTACKABLE;

    public StartTurfWar<E> attackablePredicate(Predicate<LivingEntity> predicate){
        this.canAttackPredicate = predicate;

        return this;
    }

    public StartTurfWar<E> useMemory(MemoryModuleType<? extends LivingEntity> memory) {
        this.priorityTargetMemory = memory;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E carnivore) {
        this.toTarget = BrainUtil.getMemory(carnivore, this.priorityTargetMemory);

        if (this.toTarget == null) {
            this.toTarget = BrainUtil.getMemory(carnivore, MemoryModuleType.HURT_BY_ENTITY);

            if (this.toTarget == null) {
                NearestVisibleLivingEntities nearbyEntities = BrainUtil.getMemory(carnivore, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

                if (nearbyEntities != null)
                    this.toTarget = nearbyEntities.findClosest(this.canAttackPredicate).orElse(null);

                if (this.toTarget == null)
                    return false;
            }
        }
        if(BrainUtil.hasMemory(carnivore, MemoryModuleTypeInit.INITIATED_TURF_WAR.get())){
            return false;
        }
        return this.canAttackPredicate.test(this.toTarget) && carnivore.distanceTo(toTarget) < 100.0F;
    }


    @Override
    protected void start(E entity)
    {
        int encounterOutcome = random.nextInt(3);

        if(BrainUtil.hasMemory(entity, MemoryModuleTypeInit.INITIATED_TURF_WAR.get())){
            this.stop(entity);
            return;
        }
        //UPDATE BRAIN TO TRIGGER TURF WAR
        BrainUtil.setMemory(toTarget, MemoryModuleTypeInit.INITIATED_TURF_WAR.get(), (byte) 1);
        BrainUtil.setMemory(entity, MemoryModuleTypeInit.INITIATED_TURF_WAR.get(), (byte) 1);
        BrainUtil.setMemory(entity, MemoryModuleTypeInit.TURF_WAR_MEMBER.get(),1);
        BrainUtil.setMemory(toTarget, MemoryModuleTypeInit.TURF_WAR_MEMBER.get(),2);
        BrainUtil.setMemory(entity, MemoryModuleTypeInit.TURF_WAR_OUTCOME.get(),encounterOutcome);
        BrainUtil.setMemory(entity, MemoryModuleTypeInit.SOCIAL_TARGET.get(),(Dinosaur) toTarget);

        //END OF BRAIN TO DO
        this.toTarget = null;
    }
}
