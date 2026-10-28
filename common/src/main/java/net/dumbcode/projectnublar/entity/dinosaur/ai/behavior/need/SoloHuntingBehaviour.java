package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.need;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

public class SoloHuntingBehaviour<E extends Dinosaur> extends ExtendedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(5).noMemory(MemoryModuleType.ATTACK_TARGET)
            .usesMemory(MemoryModuleType.NEAREST_ATTACKABLE).usesMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            .hasMemory(MemoryModuleTypeInit.HUNTING.get())
            .hasMemory(MemoryModuleTypeInit.IS_HUNGRY.get());

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
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {return MEMORY_REQUIREMENTS;}

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E carnivore) {
        // A baby does not hunt, nor an animal that rests, eats, drinks or sees meat on the ground (isHuntingBlocked).
        if (carnivore.isChild() || carnivore.isHuntingBlocked()) {
            return false;
        }
        Brain<?> brain = carnivore.getBrain();
        this.toTarget = null;
        this.toTarget = BrainUtil.getMemory(brain, this.priorityTargetMemory);

            if (this.toTarget == null) {
                NearestVisibleLivingEntities nearbyEntities = BrainUtil.getMemory(brain, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

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
                        BrainUtil.setMemory(carnivore, MemoryModuleTypeInit.WANTS_TO_BREAK_FENCE.get(),true);
                        this.toTarget = bestTarget;
                    }
                }

                if (this.toTarget == null)
                    return false;

            } else return false;



        // Last, so the level is searched only when a hunt would start.
        return this.canAttackPredicate.test(this.toTarget) && !foodOnTheGround(level, carnivore);
    }

    /**
     * Whether food of the hunter's diet lies on the ground within its food-item range, so it eats that first (backlog:
     * "a raptor with dropped meat in range eats the meat first"). Read from the level, not from the food-item memory:
     * {@code Eat} clears that memory after each item, and the sensor sets it again only at its next scan, up to 20
     * ticks later, long enough for a hunt to start between two items of the same kill.
     */
    private static boolean foodOnTheGround(ServerLevel level, Dinosaur hunter) {
        AABB range = hunter.getBoundingBox().inflate(Dinosaur.FOOD_ITEM_RANGE);
        return !level.getEntitiesOfClass(ItemEntity.class, range, hunter::canTargetFoodItem).isEmpty();
    }

    @Override
    protected void start(E entity) {
        BrainUtil.setTargetOfEntity(entity, this.toTarget);
        BrainUtil.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }
}
