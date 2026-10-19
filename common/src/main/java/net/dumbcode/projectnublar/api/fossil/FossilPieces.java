package net.dumbcode.projectnublar.api.fossil;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/** Pseudo-registry of fossil pieces; species-specific piece sets are assembled here. */
public class FossilPieces {

    private static final List<FossilPiece> PIECES = new ArrayList<>();

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
        List<FossilPiece> tyrannosaurPieces = new ArrayList<>();
        tyrannosaurPieces.add(REX_SKULL);
        tyrannosaurPieces.add(RIBCAGE);
        tyrannosaurPieces.add(ARM);
        tyrannosaurPieces.add(LEG);
        tyrannosaurPieces.add(FOOT);
        tyrannosaurPieces.add(TAIL);
        tyrannosaurPieces.add(SPINE);
        return tyrannosaurPieces;
    }

    public static List<FossilPiece> getPieces() {
        return PIECES;
    }

    public static List<FossilPiece> getTriceratopsPieces() {
        List<FossilPiece> triceratopsPieces = new ArrayList<>();
        triceratopsPieces.add(TRICERATOPS_SKULL);
        triceratopsPieces.add(RIBCAGE);
        triceratopsPieces.add(ARM);
        triceratopsPieces.add(LEG);
        triceratopsPieces.add(FOOT);
        triceratopsPieces.add(TAIL);
        triceratopsPieces.add(SPINE);
        return triceratopsPieces;
    }

    /** Piece set for a species; empty for species without a hardcoded set. */
    public static List<FossilPiece> getPiecesByEntityType(ResourceLocation entityType) {
        if (entityType.getPath().equals("tyrannosaurus_rex")) {
            return getTyrannosaurPieces();
        } else if (entityType.getPath().equals("triceratops")) {
            return getTriceratopsPieces();
        } else {
            return new ArrayList<>();
        }
    }

    /** Registers a piece in the "common" folder. */
    public static FossilPiece registerPiece(String name) {
        return registerPiece(name, "common");
    }

    public static FossilPiece registerPiece(String name, String folder) {
        FossilPiece piece = new FossilPiece(name, folder);
        PIECES.add(piece);
        return piece;
    }

    public static FossilPiece getPieceByName(String name) {
        return PIECES.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
    }
}
