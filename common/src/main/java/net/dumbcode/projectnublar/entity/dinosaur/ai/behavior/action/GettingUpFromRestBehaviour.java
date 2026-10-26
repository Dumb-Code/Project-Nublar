package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;

public class GettingUpFromRestBehaviour <E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(MemoryModuleTypeInit.GETTING_UP.get());

    public GettingUpFromRestBehaviour(int delayTicks) {
        super(delayTicks);
    }


    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void doDelayedAction(E entity) {
        super.doDelayedAction(entity);
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.GETTING_UP.get());
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.GETTING_UP.get());
    }
}