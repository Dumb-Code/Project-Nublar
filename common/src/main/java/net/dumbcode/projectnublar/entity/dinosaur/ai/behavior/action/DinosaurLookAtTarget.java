package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;

/**
 * Look at the look target for as long as it is present
 * @param <E> The entity
 */
public class DinosaurLookAtTarget<E extends Mob> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(MemoryModuleType.LOOK_TARGET);

	public DinosaurLookAtTarget() {
		noTimeout();
	}

	@Override
	public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return BrainUtil.hasMemory(entity, MemoryModuleType.LOOK_TARGET);
	}

	@Override
	protected void tick(E entity) {

			BrainUtil.withMemory(entity, MemoryModuleType.LOOK_TARGET, target -> entity.getLookControl().setLookAt(target.currentPosition()));

	}
}
