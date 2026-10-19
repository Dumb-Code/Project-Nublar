package net.dumbcode.projectnublar.util;

import java.util.LinkedHashMap;
import java.util.List;
import net.dumbcode.projectnublar.config.FossilsConfig;

/** Reads the fossil config into the shapes the fossil worldgen needs. */
public final class FossilUtils {

    private FossilUtils() {
    }

    /**
     * The piece weights of a species: the pieces of its set in config order, then its special
     * pieces in config order. A piece named twice keeps the later weight.
     */
    public static LinkedHashMap<String, Integer> pieceWeights(FossilsConfig.SpeciesFossils species) {
        LinkedHashMap<String, Integer> weights = new LinkedHashMap<>();
        FossilsConfig.PieceSet set = FossilsConfig.getSet(species.getPieces().get());
        if (set != null) {
            put(weights, set.pieces().get(), set.weights().get());
        }
        put(weights, species.getSpecialPieces().get(), species.getSpecialWeights().get());
        return weights;
    }

    /** Pairs names with weights by index; extra entries in the longer list are ignored. */
    private static void put(LinkedHashMap<String, Integer> weights, List<? extends String> pieces,
                            List<? extends Integer> pieceWeights) {
        for (int i = 0; i < Math.min(pieces.size(), pieceWeights.size()); i++) {
            weights.put(pieces.get(i), pieceWeights.get(i));
        }
    }
}
