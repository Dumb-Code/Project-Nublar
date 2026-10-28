package net.dumbcode.projectnublar.entity.dinosaur.ai.task;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.HerdNeeds;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;

/**
 * The animal sits down, then rests. It starts when the animal is tired, or when its sleep window has begun
 * ({@link HerdNeeds#sleepsNow}, Task 42). In its sleep window it sleeps on until the window ends; outside it, it
 * gets up at full stamina. A starving or dehydrated animal gets up at once, in its window too, to eat or drink.
 *
 * @param <E> the dinosaur that rests
 */
public class Rest<E extends Dinosaur> extends DelayedBehaviour<E> {

    /** @param delayTicks how long the animal sits before it rests */
    public Rest(int delayTicks) {
        super(delayTicks);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E dinosaur) {
        boolean tired = BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_TIRED.get());
        if (BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_DEHYDRATED.get()) || BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_STARVING.get())){
            return tired && BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_EXHAUSTED.get());
        }
        return (tired || HerdNeeds.sleepsNow(dinosaur)) && !BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
    }

    @Override
    protected void tick(E dinosaur) {
        super.tick(dinosaur);

        // Hunger or thirst at zero gets the animal up, in its sleep window too (Task 42), so it goes to eat or drink.
        if (DinoNeedsUtils.isDehydratedOrStarving(dinosaur)) {
            BrainUtil.clearMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
            BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.GETTING_UP.get(), true);
            DinoAnimationUtils.setAnimationState(dinosaur, "rest", false);
        }
        // In its sleep window (backlog F2.0) the animal sleeps on; outside it, the rest ends once it is rested.
        if (!dinosaur.getDinoBehaviour().inSleepWindow(dinosaur.level().getOverworldClockTime())
                && DinoNeedsUtils.getCurrentStamina(dinosaur) >= DinoNeedsUtils.getMaxStamina(dinosaur)) {
            BrainUtil.clearMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
        }
    }


    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        // No tired memory needed: the sleep window starts a rest too (checkExtraStartConditions).
        return Set.of();
    }

    @Override
    protected void start(E dinosaur) {
        BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get(), true);
        BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.IS_SITTING.get(), true);
    }

    @Override
    protected void doDelayedAction(E entity) {
        super.doDelayedAction(entity);
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.IS_SITTING.get());
        DinoAnimationUtils.setAnimationState(entity,"sit",false);
        DinoAnimationUtils.setAnimationState(entity,"rest",true);
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return BrainUtil.hasMemory(entity, MemoryModuleTypeInit.IS_RESTING.get());
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.IS_TIRED.get());
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.IS_RESTING.get());
        BrainUtil.setMemory(entity, MemoryModuleTypeInit.GETTING_UP.get(), true);
        DinoAnimationUtils.setAnimationState(entity, "rest", false);
    }
}
