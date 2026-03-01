package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.api.Dinosaur;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class AmberBlock extends Block {
    final Dinosaur entityType;
    final Block base;
    public AmberBlock(Properties properties, Dinosaur entityType, Block base) {
        super(properties);
        this.entityType = entityType;
        this.base = base;
    }


    public Dinosaur getEntityType() {
        return entityType;
    }
    public Block getBase() {
        return base;
    }

}
