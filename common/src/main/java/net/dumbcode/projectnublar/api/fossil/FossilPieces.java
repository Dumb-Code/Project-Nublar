package net.dumbcode.projectnublar.api.fossil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

/** Every fossil piece, and the piece sets that fossil collections are built from. */
public final class FossilPieces {

    private static final List<FossilPiece> PIECES = new ArrayList<>();

    public static final FossilPiece BIPEDAL_LEG = register("bipedal_leg", "common/biped", "leg");
    public static final FossilPiece BIPEDAL_CLAW_LEG = register("bipedal_claw_leg", "common/biped", "claw_leg");
    public static final FossilPiece BIPEDAL_ARM = register("bipedal_arm", "common/biped", "arm");

    public static final FossilPiece QUADRUPED_LEG = register("quadruped_leg", "common/quadruped", "leg");

    public static final FossilPiece NECK = register("neck", "common/generic", "neck");
    public static final FossilPiece RIBCAGE = register("ribcage", "common/generic", "ribcage");
    public static final FossilPiece TAIL = register("tail", "common/generic", "tail");

    public static final FossilPiece SAUROPOD_NECK = register("sauropod_neck", "common/sauropod", "neck");

    public static final FossilPiece TYRANNOSAURUS_FOOT = register("tyrannosaurus_foot", "tyrannosaurus_rex", "foot");
    public static final FossilPiece TYRANNOSAURUS_HAND = register("tyrannosaurus_hand", "tyrannosaurus_rex", "hand");
    public static final FossilPiece TYRANNOSAURUS_PELVIS = register("tyrannosaurus_pelvis", "tyrannosaurus_rex", "pelvis");
    public static final FossilPiece TYRANNOSAURUS_SKULL = register("tyrannosaurus_skull", "tyrannosaurus_rex", "skull");

    /** The pieces every two-legged species shares. */
    public static final List<FossilPiece> BIPED_PIECES = List.of(BIPEDAL_LEG, BIPEDAL_ARM, NECK, RIBCAGE, TAIL);

    /** The Tyrannosaurus pieces: its own four, then the biped set. */
    public static final List<FossilPiece> TYRANNOSAURUS_PIECES = Stream.concat(
            Stream.of(TYRANNOSAURUS_HAND, TYRANNOSAURUS_FOOT, TYRANNOSAURUS_PELVIS, TYRANNOSAURUS_SKULL),
            BIPED_PIECES.stream()).toList();

    private FossilPieces() {
    }

    private static FossilPiece register(String name, String folder, String displayName) {
        FossilPiece piece = new FossilPiece(name, folder, displayName);
        PIECES.add(piece);
        return piece;
    }

    /** Every piece, in declaration order. */
    public static List<FossilPiece> all() {
        return List.copyOf(PIECES);
    }

    /** The piece with this name, or null (old saves can hold removed names such as {@code rex_skull}). */
    public static @Nullable FossilPiece byName(String name) {
        for (FossilPiece piece : PIECES) {
            if (piece.name().equals(name)) {
                return piece;
            }
        }
        return null;
    }
}
