package net.dumbcode.projectnublar.entity.ai.behaviour.actions;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class GettingUpFromRestBehaviour <E extends AbstractDinosaur> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleTypeInit.GETTING_UP.get(), MemoryStatus.VALUE_PRESENT));

    public GettingUpFromRestBehaviour(int delayTicks) {
        super(delayTicks);
    }


    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void doDelayedAction(E entity) {
        super.doDelayedAction(entity);
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.GETTING_UP.get());
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.GETTING_UP.get());
    }
}