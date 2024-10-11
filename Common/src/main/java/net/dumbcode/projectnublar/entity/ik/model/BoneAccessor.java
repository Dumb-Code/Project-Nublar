package net.dumbcode.projectnublar.entity.ik.model;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface BoneAccessor {
    /**
     * @param entity the entity the model of the bone belongs to
     * @return the position of the bone in world space
     */
    Vec3 getPosition(Entity entity);

    /**
     * @param to the point to move to
     * @param facing at wha the bone should face, if null, the bone will not rotate
     * @param entity the entity the model of the bone belongs to
     */
    void moveTo(Vec3 to, @Nullable Vec3 facing, Entity entity);
}
