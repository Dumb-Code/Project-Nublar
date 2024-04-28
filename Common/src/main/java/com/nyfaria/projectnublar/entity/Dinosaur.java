package com.nyfaria.projectnublar.entity;

import com.nyfaria.projectnublar.entity.api.FossilRevived;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class Dinosaur extends PathfinderMob implements FossilRevived {
    public Dinosaur(EntityType<? extends PathfinderMob> $$0, Level $$1) {
        super($$0, $$1);
    }
}
