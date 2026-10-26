package net.dumbcode.projectnublar.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dumbcode.projectnublar.block.api.multiblock.MultiBlock;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.block.entity.GeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class GeneratorBlock extends BaseEntityBlock {
    /** Codec of this block type: the properties plus the three energy values. */
    public static final MapCodec<GeneratorBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    propertiesCodec(),
                    Codec.INT.fieldOf("max_energy").forGetter(GeneratorBlock::getMaxEnergy),
                    Codec.INT.fieldOf("energy_output").forGetter(GeneratorBlock::getEnergyOutput),
                    Codec.INT.fieldOf("energy_input").forGetter(GeneratorBlock::getEnergyInput))
            .apply(i, GeneratorBlock::new));

    private final int maxEnergy;
    private final int energyOutput;
    private final int energyInput;

    public GeneratorBlock(Properties pProperties, int maxEnergy, int energyOutput, int energyInput) {
        super(pProperties);
        this.maxEnergy = maxEnergy;
        this.energyInput = energyInput;
        this.energyOutput = energyOutput;
    }

    @Override
    protected MapCodec<GeneratorBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            this.openContainer(pLevel, pPos, pPlayer);
            return InteractionResult.CONSUME;
        }

    }
    protected void openContainer(Level pLevel, BlockPos pPos, Player pPlayer) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof BaseContainerBlockEntity) {
            pPlayer.openMenu((MenuProvider) blockentity);
            //todo: add stat
//            pPlayer.awardStat(getOpenState());
        }
    }
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GeneratorBlockEntity(pPos, pState);
    }
    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, pBlockEntityType, (level, pos, state, be) -> ((GeneratorBlockEntity)be).tick(level, pos, state, (GeneratorBlockEntity)be));
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public int getEnergyOutput() {
        return energyOutput;
    }

    public int getEnergyInput() {
        return energyInput;
    }
}
