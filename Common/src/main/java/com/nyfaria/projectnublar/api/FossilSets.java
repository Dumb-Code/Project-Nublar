package com.nyfaria.projectnublar.api;

import java.util.ArrayList;
import java.util.List;

public class FossilSets {
    private static List<FossilSet> SETS = new ArrayList<>();
    public static FossilSet BIPED = registerSet("biped");
    public static FossilSet QUADRUPED = registerSet("quadruped");
    public static FossilSet FERN = registerSet("fern");

    public static FossilSet registerSet(String name) {
        FossilSet set = new FossilSet(name);
        SETS.add(set);
        return set;
    }
    public static List<FossilSet> getSets() {
        return SETS;
    }
}
