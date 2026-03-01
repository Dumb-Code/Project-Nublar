package net.dumbcode.projectnublar.api.fossil;

import net.dumbcode.projectnublar.block.FossilBlock;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum FossilBase implements StringRepresentable {
    STONE("stone", Blocks.STONE),
    SANDSTONE("sandstone", Blocks.SANDSTONE),
    DEEPSLATE("deepslate", Blocks.DEEPSLATE),;

    private final String name;
    private final Block block;


    FossilBase(String name, Block block) {
        this.name = name;
        this.block = block;
    }

    public String getName() {
        return this.name;
    }
    public Block getBlock() {
        return this.block;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
