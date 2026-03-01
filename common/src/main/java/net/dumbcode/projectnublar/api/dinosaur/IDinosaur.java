package net.dumbcode.projectnublar.api.dinosaur;

import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.api.Dinosaur;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IDinosaur {

    /// This is the Interface for all [net.dumbcode.projectnublar.api.Dinosaur] objects in Project Nublar

    /// You may add as many dinosaurs as you want
    ///
    void registerDinosaur(net.dumbcode.projectnublar.api.Dinosaur dinosaur);

    default Dinosaur getDinosaur() {return null;}
    /// Each Instance of IDinosaur that is making use of the IDinosaur Brain must return an instance of [net.dumbcode.projectnublar.api.DinoBehaviourData]
    /// This will call on the dinosaurs default AI settings from the Behaviour DataPack.
    @ApiStatus.OverrideOnly
    default @Nullable DinoBehaviourData getDinoBehaviourData(){
        return null;
    }


    @ApiStatus.Internal
    default @Nullable List<String> getFossilPieces(){
        return null;
    }



}
