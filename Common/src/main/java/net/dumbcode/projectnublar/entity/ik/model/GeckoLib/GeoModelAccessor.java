package net.dumbcode.projectnublar.entity.ik.model.GeckoLib;

import net.dumbcode.projectnublar.entity.ik.model.BoneAccessor;
import net.dumbcode.projectnublar.entity.ik.model.ModelAccessor;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

import java.util.Optional;

public class GeoModelAccessor implements ModelAccessor {
    private final GeoModel<? extends GeoAnimatable> model;

    public GeoModelAccessor(GeoModel<? extends GeoAnimatable> model) {
        this.model = model;
    }

    @Override
    public BoneAccessor getBone(String boneName) {
        Optional<GeoBone> optionalGeoBone = this.model.getBone(boneName);

        if (optionalGeoBone.isEmpty()) {
            throw new IllegalArgumentException("Bone not found: " + boneName);
        }

        GeoBone bone = optionalGeoBone.get();

        return (BoneAccessor) bone;
    }
}
