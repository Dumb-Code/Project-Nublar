package net.dumbcode.projectnublar.entity.ai.behaviour.needs;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class SoloHuntingBehaviour<E extends AbstractDinosaur> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.NEAREST_ATTACKABLE, MemoryStatus.REGISTERED), Pair.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.REGISTERED)
            ,Pair.of(MemoryModuleTypeInit.HUNTING.get(), MemoryStatus.VALUE_PRESENT),
            Pair.of(MemoryModuleTypeInit.IS_HUNGRY.get(), MemoryStatus.VALUE_PRESENT));

    protected Predicate<LivingEntity> canAttackPredicate = entity -> entity.isAlive() && (!(entity instanceof Player player)|| !player.getAbilities().invulnerable);
    protected LivingEntity toTarget = null;
    protected MemoryModuleType<? extends LivingEntity> priorityTargetMemory = MemoryModuleType.NEAREST_ATTACKABLE;

    public SoloHuntingBehaviour<E> attackablePredicate(Predicate<LivingEntity> predicate){
        this.canAttackPredicate = predicate;

        return this;
    }
    public SoloHuntingBehaviour<E> useMemory(MemoryModuleType<? extends LivingEntity> memory) {
        this.priorityTargetMemory = memory;

        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {return MEMORY_REQUIREMENTS;}

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E carnivore) {
        Brain<?> brain = carnivore.getBrain();
        this.toTarget = null;
        this.toTarget = BrainUtils.getMemory(brain, this.priorityTargetMemory);

            if (this.toTarget == null) {
                NearestVisibleLivingEntities nearbyEntities = BrainUtils.getMemory(brain, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

                if(nearbyEntities != null) {
                    double currentHuntScore = 0;
                    double bestHuntScore = 0;
                    @Nullable LivingEntity bestTarget = null;
                    for (LivingEntity targetTest : nearbyEntities.findAll(this.canAttackPredicate)) { //Finds all targets with low enough threat score
                        currentHuntScore = DinoNeedsUtils.getHuntTargetValue(targetTest); //Chooses the target with best Food risk/reward ratio
                        if(currentHuntScore > bestHuntScore){
                            bestHuntScore = currentHuntScore;
                            bestTarget = targetTest;
                        }
                    }
                    if(bestTarget != null && DinoNeedsUtils.isTargetInsideEnclosure(bestTarget, carnivore)){
                        this.toTarget = bestTarget;
                    } else if (bestTarget != null && !DinoNeedsUtils.isTargetInsideEnclosure(bestTarget,carnivore) && DinoNeedsUtils.starving(carnivore)) {
                        BrainUtils.setMemory(carnivore, MemoryModuleTypeInit.WANTS_TO_BREAK_FENCE.get(),true);
                        this.toTarget = bestTarget;
                    }
                }

                if (this.toTarget == null)
                    return false;

            } else return false;



        return this.canAttackPredicate.test(this.toTarget);
    }

    @Override
    protected void start(E entity) {
        BrainUtils.setTargetOfEntity(entity, this.toTarget);
        BrainUtils.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        System.out.println("Entity: " + entity + ", has found target: "+ this.toTarget + ", with hunt score of: " + DinoNeedsUtils.getHuntTargetValue(this.toTarget));
    }
}
