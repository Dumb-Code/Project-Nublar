package net.dumbcode.projectnublar.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.dumbcode.projectnublar.init.DinosaurInit;
import net.dumbcode.projectnublar.init.EntityInit;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class Dinosaurs {

   // public static Codec<Dinosaur> CODEC = Codec.STRING.xmap(Dinosaurs::byName, Dinosaur::name);
    public static Map<Dinosaur, EntityType<? extends AbstractDinosaur>> DINOSAUR_STORAGE = new HashMap<>();

    public static EntityType<? extends AbstractDinosaur> getDinosaurEntity(Dinosaur dinosaur) {
       return DINOSAUR_STORAGE.get(dinosaur);
    }


}
