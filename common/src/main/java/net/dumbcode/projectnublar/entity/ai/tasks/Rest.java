package net.dumbcode.projectnublar.entity.ai.tasks;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class Rest<E extends AbstractDinosaur> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleTypeInit.IS_TIRED.get(), MemoryStatus.VALUE_PRESENT));

    public Rest(int delayTicks) {
        super(delayTicks);
    }

    boolean isNightTime;
    boolean isNocturnal;

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E dinosaur) {
        if (BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.IS_DEHYDRATED.get()) || BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.IS_STARVING.get())){
            return BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.IS_EXHAUSTED.get());
        }
        return BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.IS_TIRED.get()) && !BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
    }

    @Override
    protected void tick(E dinosaur) {
        super.tick(dinosaur);

        isNightTime = dinosaur.level().getDayTime() % 24000 > 12000;
        isNocturnal = dinosaur.getDinoBehaviour().isNocturnal();

        if((isNightTime && isNocturnal) || (!isNightTime && !isNocturnal)){
            if (DinoNeedsUtils.getCurrentStamina(dinosaur) >= DinoNeedsUtils.getMaxStamina(dinosaur)) {
                BrainUtils.clearMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
            }
            if (DinoNeedsUtils.isDehydratedOrStarving(dinosaur)) {
                BrainUtils.clearMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
                BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.GETTING_UP.get(), true);
                DinoAnimationUtils.setAnimationState(dinosaur, "rest", false);
            }
        }
    }


    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void start(E dinosaur) {
        BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get(), true);
        BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.IS_SITTING.get(), true);
    }

    @Override
    protected void doDelayedAction(E entity) {
        super.doDelayedAction(entity);
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.IS_SITTING.get());
        DinoAnimationUtils.setAnimationState(entity,"sit",false);
        DinoAnimationUtils.setAnimationState(entity,"rest",true);
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return BrainUtils.hasMemory(entity, MemoryModuleTypeInit.IS_RESTING.get());
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.IS_TIRED.get());
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.IS_RESTING.get());
        BrainUtils.setMemory(entity, MemoryModuleTypeInit.GETTING_UP.get(), true);
        DinoAnimationUtils.setAnimationState(entity, "rest", false);
    }
}
