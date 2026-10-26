package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action;

import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class BreakFenceBehaviour<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(MemoryModuleTypeInit.WANTS_TO_BREAK_FENCE.get());

    @Nullable
    private BlockPos targetFencePos;

    public BreakFenceBehaviour(int delayTicks) {
        super(delayTicks);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.targetFencePos = null;
        double closestDistance = Double.MAX_VALUE;
        if(BrainUtil.hasMemory(entity, SBLMemoryTypes.NEARBY_BLOCKS.get())){
            List<BlockInWorld> nearby_blocks = BrainUtil.getMemory(entity, SBLMemoryTypes.NEARBY_BLOCKS.get());

            if(nearby_blocks == null) {return false;}

            for(BlockInWorld blockToTest: nearby_blocks){
                if (blockToTest.getState().is(BlockInit.ELECTRIC_FENCE.get())){
                    BlockPos testPos = blockToTest.getPos();
                    BlockEntity blockEntity = level.getBlockEntity(testPos);
                    double distance = entity.distanceToSqr(Vec3.atCenterOf(testPos));
                    if(blockEntity instanceof BlockEntityElectricFence && distance < 2 && distance < closestDistance) {
                        closestDistance = distance;
                        this.targetFencePos = testPos;
                    }
                }
            }

        }
        return this.targetFencePos != null;
    }

    @Override
    protected void start(E entity) {
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.WANTS_TO_BREAK_FENCE.get());
        BrainUtil.clearMemory(entity,SBLMemoryTypes.NEARBY_BLOCKS.get());
        DinoAnimationUtils.setAnimationState(entity,"attack", true);

    }

    @Override
    protected void doDelayedAction(E entity) {
        DinoAnimationUtils.setAnimationState(entity, "attack",false);
        if (entity.level() instanceof ServerLevel level && this.targetFencePos != null) {
            BlockEntity blockEntity = level.getBlockEntity(this.targetFencePos);
            if (blockEntity instanceof BlockEntityElectricFence fence
                    && entity.distanceToSqr(Vec3.atCenterOf(this.targetFencePos)) < 2) {
                fence.breakFence(10);
                entity.level().players().forEach(p ->
                        p.sendSystemMessage(Component.literal("WARNING: Fence Destroyed at: " + entity.position() + ", by a: " + entity)));
            }
        }
    }

    @Override
    protected void stop(E entity) {
        this.targetFencePos = null;
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }
}
