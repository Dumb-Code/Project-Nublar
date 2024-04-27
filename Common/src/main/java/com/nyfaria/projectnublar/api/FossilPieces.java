package com.nyfaria.projectnublar.api;

import java.util.ArrayList;
import java.util.List;
//pseudo-registry of fossil pieces.
public class FossilPieces {
    private static List<FossilPiece> PIECES = new ArrayList<>();
    public static FossilPiece RIBCAGE = registerPiece("ribcage");
    public static FossilPiece NECK = registerPiece("neck");
    public static FossilPiece FOOT = registerPiece("foot");
    public static FossilPiece ARM = registerPiece("arm");
    public static FossilPiece LEG = registerPiece("leg");
    public static FossilPiece WING = registerPiece("wing");
    public static FossilPiece TAIL = registerPiece("tail");
    public static FossilPiece SPINE = registerPiece("spine");
    public static FossilPiece LEAF = registerPiece("leaf");
    public static FossilPiece REX_SKULL = registerPiece("rex_skull", "tyrannosaurus_rex");

    public static List<FossilPiece> getPieces() {
        return PIECES;
    }
    //overload to register common piece
    public static FossilPiece registerPiece(String name) {
        return registerPiece(name, "common");
    }

    //register a piece
    public static FossilPiece registerPiece(String name, String folder) {
        FossilPiece piece = new FossilPiece(name, folder);
        PIECES.add(piece);
        return piece;
    }
    //get a piece by name
    public static FossilPiece getPieceByName(String name) {
        return PIECES.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
    }
}
