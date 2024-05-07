package com.nyfaria.projectnublar.item;

import com.nyfaria.projectnublar.block.ProcessorBlock;
import com.nyfaria.projectnublar.block.entity.ProcessorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class TankItem extends Item {
    private final int fluidAmount;
    public TankItem(Properties properties, int fluidAmount) {
        super(properties);
        this.fluidAmount = fluidAmount;
    }
    public int getFluidAmount() {
        return fluidAmount;
    }
}
