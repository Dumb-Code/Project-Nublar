package net.dumbcode.projectnublar.util;


import net.dumbcode.projectnublar.api.Dinosaur;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.minecraft.util.random.SimpleWeightedRandomList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FossilUtils {


    public static FossilsConfig.Fossil getFossilForDino(Dinosaur dinoName){
        return FossilsConfig.getFossils().get(dinoName);
    }
    public static String getDinoFossilSet(FossilsConfig.Fossil fossil){
        return fossil.getPieces().get();
    }
    public static Map<String,Map<String,Integer>> getSpecialPieces(FossilsConfig.Fossil fossil){
        List<String> specialPieces = fossil.getSpecial_pieces().get();
        List<Integer> weights = fossil.getSpecial_weights().get();
        Map<String, Integer> pieceWeights = new HashMap<>();
        Map<String, Map<String,Integer>> setMap = new HashMap<>();
        for(int i = 0; i < specialPieces.size(); i++){
            pieceWeights.put(specialPieces.get(i), weights.get(i));
        }
        setMap.put(fossil.toString(), pieceWeights);
        return setMap;
    }

    public static Map<String,Integer> getPiecesForDino(FossilsConfig.Fossil fossil){
       Map<String,Map<String,Integer>> setMap = getFossilSet(getDinoFossilSet(fossil));
       Map<String, Map<String,Integer>> specialPieces = getSpecialPieces(fossil);
       Map<String, Integer> piecesToAdd = new HashMap<>();

       for(String key : setMap.keySet()){
           Map<String,Integer> pieceWeights = setMap.get(key);
           piecesToAdd.putAll(pieceWeights);
       }
       for(String key : specialPieces.keySet()){
           Map<String,Integer> pieceWeights = specialPieces.get(key);
           piecesToAdd.putAll(pieceWeights);
       }
       return piecesToAdd;
    }

    public static Map<String,Map<String,SimpleWeightedRandomList.Builder<String>>> getPeriodBiomeWeightMap(FossilsConfig.Fossil fossil){
        List<String> periods = fossil.getPeriods().get();
        List<String> biomes = fossil.getBiomes().get();
        Map<String, Map<String, SimpleWeightedRandomList.Builder<String>>> WEIGHTED_PERIOD_BIOME_FOSSIL_MAP = new HashMap<>();


        for (String period : periods) {
            for (String biome : biomes) {
                    WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.put(period, new HashMap<>());
                    WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.get(period).put(biome, new SimpleWeightedRandomList.Builder<>());

                WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.get(period).get(biome).add(fossil.toString(), fossil.getWeight().get());
            }
        }
        return WEIGHTED_PERIOD_BIOME_FOSSIL_MAP;
    }

    public static Map<String, Map<String,Integer>> getFossilSet(String fossilSet){
        FossilsConfig.Set set = FossilsConfig.getSet(fossilSet);
        List<? extends String> pieces = set.pieces().get();
        List<? extends Integer> weightsint = set.weights().get();
        Map<String, Integer> pieceWeights = new HashMap<>();
        Map<String, Map<String,Integer>> setMap = new HashMap<>();

        for (int i10 = 0; i10 < pieces.size(); i10++) {
            String piece = pieces.get(i10);
            int weight = weightsint.get(i10);
            pieceWeights.put(piece, weight);
        }

        setMap.put(fossilSet,pieceWeights);

        return setMap;
    }





}
