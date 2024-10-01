package net.dumbcode.projectnublar.entity.ik.model;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface BoneAccessor {
    Vec3 getPivotPointOffset(Entity entity);

    void moveTo(Vec3 to, Vec3 facing, Entity entity);
}
