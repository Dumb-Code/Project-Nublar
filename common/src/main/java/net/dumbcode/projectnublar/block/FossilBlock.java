package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.api.fossil.FossilBase;
import net.dumbcode.projectnublar.api.fossil.FossilPiece;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * A fossil ore: one piece of one species in one host stone. The quality is a block state, so one
 * block covers all four qualities.
 */
public class FossilBlock extends DropExperienceBlock {
    public static final EnumProperty<Quality> QUALITY = EnumProperty.create("quality", Quality.class);

    private final ResourceLocation entityType;
    private final FossilPiece fossilPiece;
    private final FossilBase base;

    public FossilBlock(Properties properties, ResourceLocation entityType, FossilPiece piece, FossilBase base) {
        super(properties);
        this.entityType = entityType;
        this.fossilPiece = piece;
        this.base = base;
        registerDefaultState(stateDefinition.any().setValue(QUALITY, Quality.COMMON));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(QUALITY);
    }

    public ResourceLocation getEntityType() {
        return entityType;
    }

    public FossilPiece getFossilPiece() {
        return fossilPiece;
    }

    public FossilBase getBase() {
        return base;
    }
}
