package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput generator, ExistingFileHelper existingFileHelper) {
        super(generator, Constants.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

        FossilCollection.COLLECTIONS.values().forEach(fossilCollection -> {
            fossilCollection.blocks().forEach(block -> fossilBlock((FossilBlock) block.get()));
            fossilCollection.amberBlocks().forEach((base, block) -> amberBlock(base.getBlock(), block.get()));
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
    /**
     * One model per quality, named {@code block/<block id>_<quality>}: the host stone with the
     * piece's overlay for that quality on top. The block state picks the model by quality.
     */
    protected void fossilBlock(FossilBlock block) {
        getVariantBuilder(block).forAllStates(state -> {
            Quality quality = state.getValue(FossilBlock.QUALITY);
            return ConfiguredModel.builder()
                    .modelFile(models()
                            .withExistingParent(getName(block) + "_" + quality.getName(), modLoc("block/fossil_base"))
                            .texture("0", blockTexture(block.getBase().getBlock()))
                            .texture("1", modLoc(block.getFossilPiece().texturePath(quality)))
                            .renderType("cutout"))
                    .build();
        });
    }

    /** The host stone with the amber overlay on top. */
    protected void amberBlock(Block base, Block block) {
        simpleBlock(block, models()
                .withExistingParent(getName(block), modLoc("block/fossil_base"))
                .texture("0", blockTexture(base))
                .texture("1", modLoc("block/fossil_overlay/amber/amber"))
                .renderType("cutout"));
    }

    protected String getName(Block item) {
        return ForgeRegistries.BLOCKS.getKey(item).getPath();
    }

}
