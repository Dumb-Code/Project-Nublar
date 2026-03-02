package net.dumbcode.projectnublar.datagen;


import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.Dinosaur;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.FossilPieces;
import net.dumbcode.projectnublar.api.fossil.FossilBase;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.DinosaurInit;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput generator, ExistingFileHelper existingFileHelper) {
        super(generator, Constants.MODID, existingFileHelper);
    }
    @Override
    protected void registerStatesAndModels() {
        simpleBlock(BlockInit.PROCESSOR.get(), models().getBuilder("block/processor").texture("particle", modLoc("block/processor")));
        simpleBlock(BlockInit.SEQUENCER.get(), models().getBuilder("block/sequencer").texture("particle", modLoc("block/sequencer")));
        simpleBlock(BlockInit.EGG_PRINTER.get(), models().getBuilder("block/egg_printer").texture("particle", modLoc("block/egg_printer")));
        simpleBlock(BlockInit.INCUBATOR.get(), models().getBuilder("block/incubator").texture("particle", modLoc("block/incubator")));

        for(FossilBase base : FossilBase.values()) {
            for (FossilPiece piece : DinosaurInit.getTyrannosaurPieces()) {
              Block block = FossilCollection.getFossilCollection(DinosaurInit.TYRANNOSAURUS_REX).fossilblocks().get(base.getBlock()).get(piece).get();
                System.out.println(ForgeRegistries.BLOCKS.getKey(block));
              generateFossils(base.getBlock(), block,piece);
            }
        }
    }
    protected void simpleCubeBottomTopBlockState(Block block) {
        simpleBlock(block, blockCubeTopModel(block));
    }
    protected BlockModelBuilder blockCubeTopModel(Block block) {
        String name = getName(block);
        return models().cubeBottomTop(name, modLoc("block/" + name + "_side"), modLoc("block/" + name + "_bottom"), modLoc("block/" + name + "_top"));
    }

    public void generateFossils(Block base, Block block,FossilPiece piece) {
        getVariantBuilder(block).forAllStates(state -> {
            Quality quality = state.getValue(FossilBlock.QUALITY_PROPERTY);
              if(quality == Quality.POOR || quality == Quality.FRAGMENTED)
                return new ConfiguredModel[]{new ConfiguredModel(models().withExistingParent(quality.getName().toLowerCase() + "_" + getName(base) + "_" + piece.name() , modLoc("block/fossil_base"))
                        .texture("0", blockTexture(base))
                        .texture("1", modLoc("block/fossil_overlay/" + piece.getPath() + "fragmented/" + piece.getName()))
                        .renderType("cutout")
                )};
              if(quality == Quality.COMMON) {
                  return new ConfiguredModel[]{new ConfiguredModel(models().withExistingParent(quality.getName().toLowerCase() +"_" + getName(base) +"_"+ piece.name(), modLoc("block/fossil_base"))
                          .texture("0", blockTexture(base))
                          .texture("1", modLoc("block/fossil_overlay/" + piece.getPath() + "fossilized/" + piece.getName()))
                          .renderType("cutout")
                  )};
              }
              else  {
                  return new ConfiguredModel[]{new ConfiguredModel(models().withExistingParent(quality.getName().toLowerCase() + "_" + getName(base) +"_"+ piece.name(), modLoc("block/fossil_base"))
                          .texture("0", blockTexture(base))
                          .texture("1", modLoc("block/fossil_overlay/" + piece.getPath() + "fresh/" + piece.getName()))
                          .renderType("cutout")
                  )};
              }

        });
    }

    protected String getName(Block item) {
        return ForgeRegistries.BLOCKS.getKey(item).getPath();
    }
}
