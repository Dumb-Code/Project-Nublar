package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.registry.SoundInit;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class ThreatDisplay<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));

    public ThreatDisplay(int delayTicks) {
        super(delayTicks);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        if(!BrainUtils.hasMemory(entity, MemoryModuleTypeInit.IS_ROARING.get())){
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
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.IS_ROARING.get());
    }
}
