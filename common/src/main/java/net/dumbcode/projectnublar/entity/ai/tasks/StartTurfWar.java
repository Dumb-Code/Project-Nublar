package net.dumbcode.projectnublar.entity.ai.tasks;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;


public class StartTurfWar<E extends AbstractDinosaur> extends ExtendedBehaviour<E> {

    private Random random = new Random();

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of(Pair.of(MemoryModuleTypeInit.INITIATED_TURF_WAR.get(), MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
    }

    protected Predicate<LivingEntity> canAttackPredicate = ( target) -> target.isAlive();
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
        this.toTarget = BrainUtils.getMemory(carnivore, this.priorityTargetMemory);

        if (this.toTarget == null) {
            this.toTarget = BrainUtils.getMemory(carnivore, MemoryModuleType.HURT_BY_ENTITY);

            if (this.toTarget == null) {
                NearestVisibleLivingEntities nearbyEntities = BrainUtils.getMemory(carnivore, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

                if (nearbyEntities != null)
                    this.toTarget = nearbyEntities.findClosest(this.canAttackPredicate).orElse(null);

                if (this.toTarget == null)
                    return false;
            }
        }
        if(BrainUtils.hasMemory(carnivore, MemoryModuleTypeInit.INITIATED_TURF_WAR.get())){
            return false;
        }
        return this.canAttackPredicate.test(this.toTarget) && carnivore.distanceTo(toTarget) < 100.0F;
    }


    @Override
    protected void start(E entity)
    {
        int encounterOutcome = random.nextInt(3);

        if(BrainUtils.hasMemory(entity, MemoryModuleTypeInit.INITIATED_TURF_WAR.get())){
            this.stop(entity);
            return;
        }
        //UPDATE BRAIN TO TRIGGER TURF WAR
        BrainUtils.setMemory(toTarget, MemoryModuleTypeInit.INITIATED_TURF_WAR.get(), (byte) 1);
        BrainUtils.setMemory(entity, MemoryModuleTypeInit.INITIATED_TURF_WAR.get(), (byte) 1);
        BrainUtils.setMemory(entity, MemoryModuleTypeInit.TURF_WAR_MEMBER.get(),1);
        BrainUtils.setMemory(toTarget, MemoryModuleTypeInit.TURF_WAR_MEMBER.get(),2);
        BrainUtils.setMemory(entity, MemoryModuleTypeInit.TURF_WAR_OUTCOME.get(),encounterOutcome);
        BrainUtils.setMemory(entity, MemoryModuleTypeInit.SOCIAL_TARGET.get(),(AbstractDinosaur) toTarget);

        //END OF BRAIN TO DO
        this.toTarget = null;
    }
}
