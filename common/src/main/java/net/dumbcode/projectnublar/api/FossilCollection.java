package net.dumbcode.projectnublar.api;

import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.init.BlockInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record FossilCollection(Map<Block,Map<Quality,Map<FossilPiece, DeferredSupplier<Block>>>> fossilblocks, Map<Block,DeferredSupplier<Block>> amberBlocks) {
    //store collections for use
    public static Map<String,FossilCollection> COLLECTIONS = new HashMap<>();
    public static Map<Block,Map<Quality,Map<FossilPiece,DeferredSupplier<Block>>>> fullFossilMap = new HashMap<>();
    public static Map<Block,DeferredSupplier<Block>> fullAmberMap = new HashMap<>();
    public static List<Block> stonelist = List.of(
            Blocks.STONE,
            Blocks.ANDESITE, Blocks.GRANITE, Blocks.DIORITE, Blocks.SANDSTONE, Blocks.DEEPSLATE,
            Blocks.TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.ORANGE_TERRACOTTA,
            Blocks.YELLOW_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.WHITE_TERRACOTTA,
            Blocks.LIGHT_GRAY_TERRACOTTA
    );
    //overload for PN Entities
    public static FossilCollection create(String fossilName) {
        return create(Constants.modLoc(fossilName));
   }
    //register a fossil collection based off an EntityType
    public static FossilCollection create(ResourceLocation entityType) {
        for (Block stone : stonelist) {
            String stoneName = BuiltInRegistries.BLOCK.getKey(stone).getPath();
            Map<Quality,Map<FossilPiece,DeferredSupplier<Block>>> qualityMap2 = new HashMap<>();
            for (Quality quality : Quality.values()) {
                String qualityName = quality == Quality.NONE ? "" : quality.getName().toLowerCase() + "_";
                Map<FossilPiece,DeferredSupplier<Block>> stoneMap2 = new HashMap<>();
                for (FossilPiece piece : FossilPieces.getPiecesByEntityType(entityType)) {
                    stoneMap2.put(piece, BlockInit.registerBlock(qualityName + stoneName + "_" + entityType.getPath() + "_" + piece.name().toLowerCase() +"_fossil", () -> new FossilBlock(BlockBehaviour.Properties.copy(stone).noOcclusion(), entityType, piece, quality,stone)));
                }
                qualityMap2.put(quality,stoneMap2);
            }
            fullFossilMap.put(stone,qualityMap2);
            fullAmberMap.put(stone,BlockInit.registerBlock(stoneName + "_" + entityType.getPath() + "_amber", () -> new AmberBlock(BlockBehaviour.Properties.copy(stone).noOcclusion(), entityType,stone)));
        }
        return COLLECTIONS.put(entityType.toString(),new FossilCollection(fullFossilMap, fullAmberMap));
    }

}
