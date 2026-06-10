package net.dumbcode.projectnublar.api.fossil;

import java.util.List;

/** Named sets of weighted fossil pieces per body plan (datapack model). */
public record FossilSets(
        String configId,
        List<FossilPiece> bipedPieces,
        List<FossilPiece> quadrupedPieces,
        List<FossilPiece> fernPieces
) {
    public record FossilPiece(String piece, int weight) {}
}
