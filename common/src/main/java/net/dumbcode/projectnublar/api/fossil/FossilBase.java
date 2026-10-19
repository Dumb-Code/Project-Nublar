package net.dumbcode.projectnublar.api.fossil;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** The host stones a fossil ore is made in; each fossil block copies its stone's properties. */
public enum FossilBase implements StringRepresentable {
    STONE("stone", Blocks.STONE),
    SANDSTONE("sandstone", Blocks.SANDSTONE),
    DEEPSLATE("deepslate", Blocks.DEEPSLATE);

    private final String name;
    private final Block block;

    FossilBase(String name, Block block) {
        this.name = name;
        this.block = block;
    }

    public String getName() {
        return name;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
