package net.dumbcode.projectnublar.block;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class AmberBlock extends Block {
    final Identifier entityType;
    final Block base;
    public AmberBlock(Properties properties, Identifier entityType, Block base) {
        super(properties);
        this.entityType = entityType;
        this.base = base;
    }


    public Identifier getEntityType() {
        return entityType;
    }

    public Block getBase() {
        return base;
    }

}
