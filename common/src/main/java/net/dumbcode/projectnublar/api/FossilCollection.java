package net.dumbcode.projectnublar.api;

import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.api.fossil.FossilBase;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.DinosaurInit;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import java.util.HashMap;
import java.util.Map;

public record FossilCollection(Map<Block,Map<FossilPiece, DeferredSupplier<Block>>> fossilblocks, Map<Block,DeferredSupplier<Block>> amberBlocks) {
    //store collections for use
    public static Map<Dinosaur,FossilCollection> COLLECTIONS = new HashMap<>();
    //register a fossil collection based off an EntityType
    public static FossilCollection create(Dinosaur dinosaur) {

        Map<Block,Map<FossilPiece,DeferredSupplier<Block>>> fullFossilMap = new HashMap<>();
        Map<Block,DeferredSupplier<Block>> fullAmberMap = new HashMap<>();

        for (FossilBase stone : FossilBase.values()) {
            Map<FossilPiece,DeferredSupplier<Block>> partMap = new HashMap<>();
                for(FossilPiece piece : dinosaur.fossilCollection()) {
                  partMap.put(piece, BlockInit.registerBlock(stone.name().toLowerCase() + "_" + dinosaur.name() + "_" + piece.name(), () -> new FossilBlock(BlockBehaviour.Properties.copy(stone.getBlock()).noOcclusion(),stone.getBlock(),dinosaur,piece)));
                }
                fullFossilMap.put(stone.getBlock(),partMap);
            fullAmberMap.put(stone.getBlock(),BlockInit.registerBlock(stone.name().toLowerCase() + "_" + dinosaur.name() + "_amber", () -> new AmberBlock(BlockBehaviour.Properties.copy(stone.getBlock()).noOcclusion(), dinosaur,stone.getBlock())));
        }
        return COLLECTIONS.put(dinosaur,new FossilCollection(fullFossilMap, fullAmberMap));
    }

    public static Map<Dinosaur,FossilCollection> createAllFossilCollections() {
        Map<Dinosaur ,FossilCollection> collections = new HashMap<>();

        for(Dinosaur dinosaur: DinosaurInit.getList()) {
                collections.put(dinosaur, create(dinosaur));

        }
        return collections;
    }

    public static FossilCollection getFossilCollection(Dinosaur dinosaur) {
        return COLLECTIONS.get(dinosaur);
    }

}
