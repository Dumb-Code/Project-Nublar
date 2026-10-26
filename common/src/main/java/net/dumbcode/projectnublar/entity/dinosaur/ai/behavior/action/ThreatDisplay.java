package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.registry.SoundInit;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;

// TODO(DEAD): commented out of Dinosaur.getCoreTasks() ("needs to be made more situational");
// not wired into any activity group. Left unwired on purpose.
public class ThreatDisplay<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);

    public ThreatDisplay(int delayTicks) {
        super(delayTicks);
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        if(!BrainUtil.hasMemory(entity, MemoryModuleTypeInit.IS_ROARING.get())){
            return false;
        }
        return !entity.isRoaring();
    }

    @Override
    protected void start(E entity) {
        DinoAnimationUtils.setAnimationState(entity, "roar", true);
        entity.playSound(SoundInit.TYRANNOSAUR_ROAR.get(), 10,1);
    }

    @Override
    protected void doDelayedAction(E entity) {
        DinoAnimationUtils.setAnimationState(entity, "roar", false);
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.IS_ROARING.get());
    }
}
