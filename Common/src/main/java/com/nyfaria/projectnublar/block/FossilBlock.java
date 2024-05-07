package com.nyfaria.projectnublar.block;

import com.nyfaria.projectnublar.api.FossilPiece;
import com.nyfaria.projectnublar.api.Quality;
import com.nyfaria.projectnublar.block.api.MultiBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;

public class FossilBlock extends DropExperienceBlock {
    final ResourceLocation entityType;
    final FossilPiece fossilPiece;
    final Quality quality;
    final Block base;
    public FossilBlock(Properties properties, ResourceLocation entityType, FossilPiece fossilPiece, Block base) {
        this(properties, entityType, fossilPiece, Quality.NONE, base);
    }
    public FossilBlock(Properties properties, ResourceLocation entityType, FossilPiece fossilPiece, Quality quality, Block base) {
        super(properties, ConstantInt.of(10));
        this.entityType = entityType;
        this.fossilPiece = fossilPiece;
        this.quality = quality;
        this.base = base;
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
