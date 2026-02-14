package net.dumbcode.projectnublar.api.fossil;

import java.util.List;

public record FossilSets(
        String configId,

        List<FossilPiece> bipedPieces,
        List<FossilPiece> quadrupedPieces,
        List<FossilPiece> fernPieces


) {
    public record FossilPiece(String piece, int weight){}
}
