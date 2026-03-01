package net.dumbcode.projectnublar.api;

import net.dumbcode.projectnublar.api.dinosaur.Diet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Dinosaur(String name, @Nullable Diet diet, @Nullable DinoBehaviourData behaviourData, @Nullable List<FossilPiece> fossilCollection, @Nullable String period) implements Comparable<Dinosaur> {



    /// register dinosaur but do not implement
    public Dinosaur(String pName) {
        this(pName,null,null,null, null);
    }

    public Dinosaur(String pName, Diet pDiet,DinoBehaviourData pBehaviourData) {
        this(pName,pDiet,pBehaviourData,null,null);
    }

    @Override
    public int compareTo(@NotNull Dinosaur o) {
        return compare(this,o);
    }

    public static int compare(Dinosaur a, Dinosaur b) {
        if(a.name().equals(b.name())) {
            return 0;
        }
        else return 1;
    }
}
