package net.dumbcode.projectnublar.entity.ai;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class FenceAwareNodeEvaluator extends WalkNodeEvaluator {
    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z, Mob mob) {
        BlockPathTypes original = super.getBlockPathType(level, x, y, z);
        BlockPos pos = new BlockPos(x,y,z);
        BlockState ground = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());
        BlockState above2 = level.getBlockState(pos.above(2));

        if(isFenceOrWire(ground) || isFenceOrWire(above) || isFenceOrWire(above2)){
            if(mob instanceof Dinosaur dinosaur && DinoNeedsUtils.allNeedsAtZero(dinosaur)) {
                    return BlockPathTypes.OPEN;
            }

            return BlockPathTypes.FENCE;
        }

        return original;
    }

    private boolean isFenceOrWire(BlockState state){
        return state.is(BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get()) || state.is(BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get()) ||
                state.is(BlockInit.ELECTRIC_FENCE.get());
    }
}
