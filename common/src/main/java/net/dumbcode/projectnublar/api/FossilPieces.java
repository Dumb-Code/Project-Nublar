package net.dumbcode.projectnublar.api;


import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.init.DinosaurInit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FossilPieces {
    public static final List<FossilPiece> FOSSIL_PIECES = new ArrayList<>();

    public static final FossilPiece BIPED_LEG = registerFossilPiece("bipedal_leg","common/biped/");
    public static final FossilPiece BIPED_CLAW_LEG = registerFossilPiece("bipedal_claw_leg","common/biped/");
    public static final FossilPiece BIPED_ARM = registerFossilPiece("bipedal_arm","common/biped/");

    public static final FossilPiece QUADRUPED_LEG = registerFossilPiece("quadruped_leg","common/quadruped/");

    public static final FossilPiece NECK =  registerFossilPiece("neck","common/generic/");
    public static final FossilPiece RIBCAGE =  registerFossilPiece("ribcage","common/generic/");
    public static final FossilPiece TAIL =  registerFossilPiece("tail","common/generic/");

    public static final FossilPiece SAUROPOD_NECK = registerFossilPiece("sauropod_neck","common/sauropod/");

    public static final FossilPiece TYRANNOSAURUS_FOOT = registerFossilPiece("tyrannosaurus_foot","tyrannosaurus_rex/");
    public static final FossilPiece TYRANNOSAURUS_HAND = registerFossilPiece("tyrannosaurus_hand","tyrannosaurus_rex/");
    public static final FossilPiece TYRANNOSAURUS_PELVIS = registerFossilPiece("tyrannosaurus_pelvis","tyrannosaurus_rex/");
    public static final FossilPiece TYRANNOSAURUS_SKULL = registerFossilPiece("tyrannosaurus_skull","tyrannosaurus_rex/");


    public static final List<FossilPiece> bipedPieces = List.of(BIPED_LEG,BIPED_ARM,NECK,RIBCAGE,TAIL);
    public static final List<FossilPiece> quadrupedPieces = List.of(QUADRUPED_LEG,NECK,RIBCAGE,TAIL);
    public static final List<FossilPiece> tyrannosaurPieces = List.of(TYRANNOSAURUS_HAND,TYRANNOSAURUS_FOOT,TYRANNOSAURUS_PELVIS,TYRANNOSAURUS_SKULL);

    public static final Map<String, List<FossilPiece>> FOSSIL_PIECES_BY_GROUP = Map.of(
            "biped",bipedPieces,
            "quadruped",quadrupedPieces,
            "tyrannosaurus_rex",tyrannosaurPieces);


    public static FossilPiece registerFossilPiece(String name, String path) {
        FossilPiece fossilPiece = new FossilPiece(name, path);
        FOSSIL_PIECES.add(fossilPiece);
        return fossilPiece;
    }

}
