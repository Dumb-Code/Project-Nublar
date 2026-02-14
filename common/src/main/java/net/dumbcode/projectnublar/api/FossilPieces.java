package net.dumbcode.projectnublar.api;

import net.minecraft.resources.ResourceLocation;

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
    public static FossilPiece TRICERATOPS_SKULL = registerPiece("triceratops_skull", "triceratops");



    public static List<FossilPiece> getTyrannosaurPieces() {
        List<FossilPiece> TYRANNOSAUR_PIECES = new ArrayList<>();

        TYRANNOSAUR_PIECES.add(REX_SKULL);
        TYRANNOSAUR_PIECES.add(RIBCAGE);
        TYRANNOSAUR_PIECES.add(ARM);
        TYRANNOSAUR_PIECES.add(LEG);
        TYRANNOSAUR_PIECES.add(FOOT);
        TYRANNOSAUR_PIECES.add(TAIL);
        TYRANNOSAUR_PIECES.add(SPINE);


        return TYRANNOSAUR_PIECES;
    }

    public static List<FossilPiece> getPieces() {
        return PIECES;
    }

    public static List<FossilPiece> getTriceratopsPieces() {
        List<FossilPiece> TRICERATOPS_PIECES = new ArrayList<>();

        TRICERATOPS_PIECES.add(TRICERATOPS_SKULL);
        TRICERATOPS_PIECES.add(RIBCAGE);
        TRICERATOPS_PIECES.add(ARM);
        TRICERATOPS_PIECES.add(LEG);
        TRICERATOPS_PIECES.add(FOOT);
        TRICERATOPS_PIECES.add(TAIL);
        TRICERATOPS_PIECES.add(SPINE);


        return TRICERATOPS_PIECES;
    }
    //overload to register common piece
    public static List<FossilPiece> getPiecesByEntityType(ResourceLocation pType) {
        if (pType.getPath().equals("tyrannosaurus_rex")) {
            return getTyrannosaurPieces();
        }
        else if (pType.getPath().equals("triceratops"))
            return getTriceratopsPieces();

        else return new ArrayList<>();
    }

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
