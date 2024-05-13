package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.init.BlockInit;
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

        // Stream.of(
        //
        //         )
        //         .map(Supplier::get)
        //         .forEach(this::simpleCubeBottomTopBlockState);
        //
        // Stream.of(
        //
        // ).map(Supplier::get)
        //         .forEach(this::simpleBlock);
        FossilCollection.COLLECTIONS.forEach((s, fossilCollection) -> {
            fossilCollection.fossilblocks().forEach((block, qualityMap) -> {
                qualityMap.forEach((quality, fossilPieceRegistryObjectMap) -> {
                    fossilPieceRegistryObjectMap.forEach((fossilPiece, blockRegistryObject) -> {
                        fossilBlock(block, (FossilBlock) blockRegistryObject.get());
                    });
                });
            });
        });
        FossilCollection.COLLECTIONS.forEach((s, fossilCollection) -> {
            fossilCollection.amberBlocks().forEach((block, blockRegistryObject) -> {
                    amberBlock(block, (AmberBlock) blockRegistryObject.get());
            });
        });
        simpleBlock(BlockInit.PROCESSOR.get(), models().getBuilder("block/processor").texture("particle", modLoc("block/processor")));
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
                    .texture("1", modLoc("block/fossil_overlay/" + block.getFossilPiece().folder() + "/" + block.getFossilPiece().name()))
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
