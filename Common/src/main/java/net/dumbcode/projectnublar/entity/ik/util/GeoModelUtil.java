package net.dumbcode.projectnublar.entity.ik.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

import java.util.Optional;

public class GeoModelUtil {
    public static<M extends GeoModel<? extends GeoAnimatable>> GeoBone getBoneOrNull(M model, String boneIdentifier) {
        Optional<GeoBone> bone = model.getBone(boneIdentifier);
        return bone.orElse(null);
    }

    public static Vec3 modelToWorldPos(GeoBone modelTransform) {
        return new Vec3(modelTransform.getPivotX() / 16, modelTransform.getPivotY() / 16, modelTransform.getPivotZ() / 16);
    }

    public static Vec3 modelToEntityPos(GeoBone modelTransform, PathfinderMob entity) {
        return modelToWorldPos(modelTransform).add(entity.position());
    }

    public static Vec3 modelTransformToVec3d(GeoBone modelTransform) {
        return new Vec3(modelTransform.getPivotX() / 18, modelTransform.getPivotY() / 18, -modelTransform.getPivotZ() / 18);
    }

    //public static void setTransformForModelPart(GeoBone modelPart, Vec3 pos, double yaw, double pitch) {
    //    // Apply rotation to the model part
    //    modelPart.setRotY((float) ((yaw - 90) * ((float) Math.PI / 180F)));
    //    modelPart.setRotX((float) ((pitch) * ((float) Math.PI / 180F)));
    //    // Position the model part at the specified global coordinates (converted to model space)
    //    setPivot(modelPart, pos);
    //}

    public static void setAngles(GeoBone bone, double yaw, double pitch, double roll) {
        bone.updateRotation((float) yaw, (float) pitch, (float) roll);
        //bone.setRotX((float) pitch);
        //bone.setRotY((float) yaw);
        //bone.setRotZ((float) roll);
    }

    public static void setBonePosFromWorldPos(GeoBone bone, Vec3 pos, Entity entity) {
        Vec3 localizedJoint = pos.subtract(entity.position()).scale(16);

        localizedJoint = localizedJoint.yRot((float) Math.toRadians(entity.getYRot()));

        localizedJoint = localizedJoint.add(GeoModelUtil.getPivot(bone));

        bone.setModelPosition(new Vector3d(localizedJoint.x, localizedJoint.y, -localizedJoint.z));
    }


    public static void setBonePosFromWorldPos(GeoBone bone, double x, double y, double z, Entity entity) {
        Vec3 bonePos = new Vec3(x, y, z);
        setBonePosFromWorldPos(bone, bonePos, entity);
    }

    public static Vec3 getPivot(GeoBone bone) {
        return new Vec3(bone.getPivotX(), -bone.getPivotY(), bone.getPivotZ());
    }
}
