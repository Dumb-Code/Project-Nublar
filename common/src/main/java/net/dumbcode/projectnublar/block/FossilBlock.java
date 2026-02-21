package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.FossilStates;
import net.dumbcode.projectnublar.api.Quality;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class FossilBlock extends DropExperienceBlock {
/*
    public static final EnumProperty<Quality> QUALITY = EnumProperty.create("quality",Quality.class);
    public static final EnumProperty<FossilStates.StoneType> STONE_TYPE = EnumProperty.create("stone_type",FossilStates.StoneType.class);
    public static final EnumProperty<FossilStates.EntityType> ENTITY_TYPE = EnumProperty.create("entity", FossilStates.EntityType.class);
    public static final EnumProperty<FossilStates.Piece> PICE = EnumProperty.create("piece",FossilStates.Piece.class);


 */

    final ResourceLocation entityType;
    final FossilPiece fossilPiece;
    final Quality quality;
    final Block base;

    public FossilBlock(Properties properties, ResourceLocation pEntityType,FossilPiece pPiece, Quality pQuality, Block pBase) {
        super(properties);
        this.entityType = pEntityType;
        this.fossilPiece = pPiece;
        this.quality = pQuality;
        this.base = pBase;
        /*
        System.err.println(BuiltInRegistries.BLOCK.getKey(pBase).getPath());
        String stoneType = BuiltInRegistries.BLOCK.getKey(pBase).getPath();
        FossilStates.StoneType type = FossilStates.StoneType.valueOf(stoneType);
        FossilStates.EntityType entityType = FossilStates.EntityType.valueOf(pEntityType.getPath());
        FossilStates.Piece fossilPieceForState = FossilStates.Piece.valueOf(pPiece.name());
        this.defaultBlockState().setValue(QUALITY,pQuality).setValue(STONE_TYPE,type).setValue(ENTITY_TYPE,entityType).setValue(PICE,fossilPieceForState);
    */
    }

    public FossilPiece getFossilPiece() {
        return fossilPiece;
    }
    public ResourceLocation getEntityType() {
        return entityType;
    }
    public Quality getQuality() {
        return quality;
    }
    public Block getBase() {
        return base;
    }
}
