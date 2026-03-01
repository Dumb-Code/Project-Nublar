package net.dumbcode.projectnublar.api.fossil.config;

import java.util.ArrayList;
import java.util.List;

public class FossilSets {
    private static List<FossilSet> SETS = new ArrayList<>();

    //Generic Sets
    public static FossilSet BIPED = registerSet("biped");
    public static FossilSet QUADRUPED = registerSet("quadruped");
    public static FossilSet FERN = registerSet("fern");

    //IDinosaur Sets
    public static FossilSet TYRANNOSAURUS_FOSSIL_SET = registerSet("tyrannosaurus_rex_fossils");

    public static FossilSet registerSet(String name) {
        FossilSet set = new FossilSet(name);
        SETS.add(set);
        return set;
    }
    public static List<FossilSet> getSets() {
        return SETS;
    }
}
