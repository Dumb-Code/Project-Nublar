package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.Quality;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;

public class FossilBlock extends DropExperienceBlock {

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
