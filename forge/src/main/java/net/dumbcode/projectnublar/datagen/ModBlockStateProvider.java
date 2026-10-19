package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput generator, ExistingFileHelper existingFileHelper) {
        super(generator, Constants.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

        FossilCollection.COLLECTIONS.values().forEach(fossilCollection -> {
            fossilCollection.fossilBlocks().forEach((base, pieces) ->
                    pieces.values().forEach(block -> fossilBlock(base.getBlock(), (FossilBlock) block.get())));
            fossilCollection.amberBlocks().forEach((base, block) ->
                    amberBlock(base.getBlock(), (AmberBlock) block.get()));
        });
        simpleBlock(BlockInit.CARNIVORE_FEEDER_ONE.get(), models().getBuilder("block/carnivore_feeder_one").texture("particle",modLoc("block/feeder/meat/feeder_one/empty")));
        simpleBlock(BlockInit.PROCESSOR.get(), models().getBuilder("block/processor").texture("particle", modLoc("block/processor")));
        simpleBlock(BlockInit.SEQUENCER.get(), models().getBuilder("block/sequencer").texture("particle", modLoc("block/sequencer")));
        simpleBlock(BlockInit.EGG_PRINTER.get(), models().getBuilder("block/egg_printer").texture("particle", modLoc("block/egg_printer")));
        simpleBlock(BlockInit.INCUBATOR.get(), models().getBuilder("block/incubator").texture("particle", modLoc("block/incubator")));
    }

    protected void simpleCubeBottomTopBlockState(Block block) {
        simpleBlock(block, blockCubeTopModel(block));
    }

    protected BlockModelBuilder blockCubeTopModel(Block block) {
        String name = getName(block);
        return models().cubeBottomTop(name, modLoc("block/" + name + "_side"), modLoc("block/" + name + "_bottom"), modLoc("block/" + name + "_top"));
    }
    protected void fossilBlock(Block base, FossilBlock block) {
        try {
            simpleBlock(block, models()
                    .withExistingParent(getName(block), modLoc("block/fossil_base"))
                    .texture("0", blockTexture(base))
                    .texture("1", modLoc(block.getFossilPiece().texturePath(Quality.COMMON)))
                    .renderType("cutout")
            );
        } catch (Exception ignored) {
        }
    }
    protected void amberBlock(Block base, AmberBlock block) {
        try {
            simpleBlock(block, models()
                    .withExistingParent(getName(block), modLoc("block/fossil_base"))
                    .texture("0", blockTexture(base))
                    .texture("1", modLoc("block/fossil_overlay/amber/amber"))
                    .renderType("cutout")
            );
        } catch (Exception ignored) {
        }
    }
    protected String getName(Block item) {
        return ForgeRegistries.BLOCKS.getKey(item).getPath();
    }

}
