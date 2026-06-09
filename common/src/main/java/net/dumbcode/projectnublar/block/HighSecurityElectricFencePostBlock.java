package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.block.api.fence.ConnectionType;
import net.dumbcode.projectnublar.block.api.fence.EnumConnectionType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class HighSecurityElectricFencePostBlock extends ElectricFencePostBlock{

    public static final IntegerProperty INDEX = IntegerProperty.create("index", 0, EnumConnectionType.HIGH_SECURITY.getHeight() - 1);

    public HighSecurityElectricFencePostBlock(Properties properties, ConnectionType type) {
        super(properties, type, INDEX);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(INDEX);
    }
}
