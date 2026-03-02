package net.dumbcode.projectnublar.block;


import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.Dinosaur;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.fossil.FossilBase;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.init.DinosaurInit;
import net.dumbcode.projectnublar.init.EntityInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.item.FossilItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FossilBlock extends DropExperienceBlock {

    public static final EnumProperty<Quality> QUALITY_PROPERTY = EnumProperty.create("quality", Quality.class);

    private final FossilBase block;
    private final Dinosaur dinosaur;
    private final FossilPiece fossilPiece;

    private Item item;

    public FossilBlock(Properties properties, FossilBase baseBlock, Dinosaur pDinosaur, FossilPiece pFossilPiece) {
        super(properties);
        block = baseBlock;
        dinosaur = pDinosaur;
        this.fossilPiece = pFossilPiece;
        // dont register a default state, makes all blocks common quality when placing
      //  this.registerDefaultState(this.defaultBlockState().setValue(QUALITY_PROPERTY,Quality.COMMON));
    }
    public FossilBlock(Properties properties, FossilBase baseBlock, Dinosaur pDinosaur, FossilPiece pFossilPiece,Quality quality) {
        super(properties);
        block = baseBlock;
        dinosaur = pDinosaur;
        this.fossilPiece = pFossilPiece;
        this.registerDefaultState(this.defaultBlockState().setValue(QUALITY_PROPERTY,quality));
    }
/// TODO: find a better way than overriding getdrops() like this
/// TODO: make enchantments affect drops

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
            ItemStack itemStack = new ItemStack(ItemInit.FOSSIL_ITEM.get());
            DNAData dnaData = new DNAData();
            dnaData.setEntityType(EntityInit.TYRANNOSAURUS_REX_ENTITY.get());
            dnaData.setQuality(state.getValue(QUALITY_PROPERTY));
            dnaData.setFossilPiece(getFossilPiece());
            itemStack.getOrCreateTag().put("DNAData", dnaData.saveToNBT(new CompoundTag()));
        drops.add(itemStack);
        return drops;
    }


    //needed as fossilblockitems don't hook in by default
    @Override
    public Item asItem() {
        if (this.item == null) {
            ItemInit.TYRANNOSAURUS_FOSSIL_ORE.forEach(blockItem -> {
                if((this.getDescriptionId()).equals(blockItem.get().getDescriptionId())){
                    this.item = blockItem.get();
                }
            });
        }

        return this.item;
    }

    public FossilBase getBase() {
        return block;
    }
    public Dinosaur getDinosaur() {return dinosaur;}
    public FossilPiece getFossilPiece() {return fossilPiece;}
    public Quality getQuality() {return this.defaultBlockState().getValue(QUALITY_PROPERTY);}
    public static void setQuality(Quality quality,FossilBlock block) {
        block.defaultBlockState().setValue(QUALITY_PROPERTY,quality);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(QUALITY_PROPERTY);
    }
}
