package net.dumbcode.projectnublar.entity.dinosaur;

import net.dumbcode.projectnublar.init.EntityInit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class DinosaurPart extends PartEntity{
    private AbstractDinosaur dinosaur;

    public DinosaurPart(EntityType<?> t, Level world) {
        super(t, world);
    }

    public DinosaurPart(EntityType<?> type, AbstractDinosaur dinosaur, String name, float radius, float angleYaw, float offsetY,
                        float sizeX, float sizeY, float damageMultiplier) {
        super(type, dinosaur,name, radius, angleYaw, offsetY, sizeX, sizeY, damageMultiplier);
        this.dinosaur = dinosaur;
    }

    public DinosaurPart(AbstractDinosaur parent, String name, float radius, float angleYaw, float offsetY, float sizeX, float sizeY, float damageMultiplier) {
        super(EntityInit.DINOSAUR_PART.get(), parent,name, radius, angleYaw, offsetY, sizeX, sizeY,
                damageMultiplier);
        this.dinosaur = parent;
    }

    @Override
    public void collideWithNearbyEntities() {
    }
}
