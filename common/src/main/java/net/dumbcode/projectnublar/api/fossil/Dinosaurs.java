package net.dumbcode.projectnublar.api.fossil;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.EntityInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dinosaurs {
    public static List<ResourceLocation> DINOSAURS_LIST = List.of(
            EntityInit.TYRANNOSAURUS_REX.getId(),
            EntityInit.TRICERATOPS.getId()
    );
    public static Map<ResourceLocation, EntityType<?>> DINOSAURS_MAP = new HashMap<>();

    public static Map<ResourceLocation, EntityType<?>> getDinosaurMap(){
        DINOSAURS_MAP.clear();
        DINOSAURS_MAP.put(EntityInit.TYRANNOSAURUS_REX.getId(),EntityInit.TYRANNOSAURUS_REX.get());
        DINOSAURS_MAP.put(EntityInit.TRICERATOPS.getId(),EntityInit.TRICERATOPS.get());

        return DINOSAURS_MAP;
    }


    public static EntityType<?>  getEntityType(ResourceLocation type){
        return getDinosaurMap().get(type);
    }

}
