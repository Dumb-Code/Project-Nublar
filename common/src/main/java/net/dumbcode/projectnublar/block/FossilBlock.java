package net.dumbcode.projectnublar.block;


import net.dumbcode.projectnublar.api.Dinosaur;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;


public class FossilBlock extends DropExperienceBlock {

    public static final EnumProperty<Quality> QUALITY_PROPERTY = EnumProperty.create("quality", Quality.class);

    private final Block block;
    private final Dinosaur dinosaur;
    private final FossilPiece fossilPiece;


    public FossilBlock(Properties properties,Block baseBlock, Dinosaur pDinosaur, FossilPiece pFossilPiece) {
        super(properties);
        block = baseBlock;
        dinosaur = pDinosaur;
        this.fossilPiece = pFossilPiece;
        this.registerDefaultState(this.defaultBlockState().setValue(QUALITY_PROPERTY,Quality.COMMON));
    }

    public Block getBlock() {
        return block;
    }
    public Dinosaur getDinosaur() {return dinosaur;}
    public FossilPiece getFossilPiece() {return fossilPiece;}
    public Quality getQuality() {return this.defaultBlockState().getValue(QUALITY_PROPERTY);}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(QUALITY_PROPERTY);
    }
}
