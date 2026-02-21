package net.dumbcode.projectnublar.api.fossil;

import net.dumbcode.projectnublar.api.FossilPiece;

import java.util.List;
import java.util.Map;

public record FossilSets(
        String configId,

        Map<net.dumbcode.projectnublar.api.FossilPiece,Integer> bipedPieces,
        Map<net.dumbcode.projectnublar.api.FossilPiece,Integer> quadrupedPieces,
        Map<net.dumbcode.projectnublar.api.FossilPiece,Integer> fernPieces


) {
    public record FossilPiece(String piece, int weight){}
}
