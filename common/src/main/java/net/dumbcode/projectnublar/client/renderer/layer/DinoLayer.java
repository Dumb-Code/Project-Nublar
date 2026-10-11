package net.dumbcode.projectnublar.client.renderer.layer;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Predicate;

public class DinoLayer {
    private final String layerName;
    private final int basicLayer;
    private final Function<Dinosaur, Boolean> renderRequirement;
    /**
     * Per-entity visibility, checked on every frame. {@link #renderRequirement} cannot do this job: it is read once,
     * when the renderer builds its layer list, so it can only describe things that never change for the entity.
     * Layers that follow a changing state (for example eyelids that close while the animal rests) use this instead.
     */
    private Predicate<Dinosaur> visibleWhen = dino -> true;
    private static final String MALE = "male";
    private static final String FEMALE = "female";
    /** Entity type the cached texture locations belong to. */
    private @Nullable EntityType<?> cachedType;
    private @Nullable ResourceLocation cachedMale;
    private @Nullable ResourceLocation cachedFemale;

    public DinoLayer(String layerName, int basicLayer, Function<Dinosaur, Boolean> renderRequirement) {
        this.layerName = layerName;
        this.basicLayer = basicLayer;
        this.renderRequirement = renderRequirement;
    }

    public DinoLayer(String layerName, int basicLayer) {
        this(layerName,basicLayer,(b) -> true);
    }

    /** Sets the per-frame visibility test of this layer. Call it once, while the layer is built. */
    public DinoLayer visibleWhen(Predicate<Dinosaur> visibleWhen) {
        this.visibleWhen = visibleWhen;
        return this;
    }

    public boolean isVisibleFor(Dinosaur dino) {
        return visibleWhen.test(dino);
    }

    public int getBasicLayer() {
        return basicLayer;
    }

    /**
     * Returns the texture of this layer for the given dinosaur. The texture depends only on the entity type and the
     * gender, so the two possible locations are built once per entity type and then reused. This keeps the per-frame
     * render path free of string and {@link ResourceLocation} allocation. Vanilla memoizes
     * {@code RenderType.entityTranslucent} per {@link ResourceLocation}, so a stable instance here is enough to avoid
     * a render type lookup that allocates.
     * Called on the render thread only, so the cache needs no locking.
     */
    public ResourceLocation getTextureLocation(Dinosaur dino) {
        EntityType<?> type = dino.getType();
        if (type != cachedType) {
            cachedType = type;
            cachedMale = null;
            cachedFemale = null;
        }
        boolean male = MALE.equals(dino.getStringDinoGender());
        ResourceLocation cached = male ? cachedMale : cachedFemale;
        if (cached == null) {
            ResourceLocation dinoLoc = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            cached = new ResourceLocation(dinoLoc.getNamespace(), "textures/entity/" + dinoLoc.getPath() + "/" + (male ? MALE : FEMALE) + "/" + layerName + ".png");
            if (male) {
                cachedMale = cached;
            } else {
                cachedFemale = cached;
            }
        }
        return cached;
    }

    public String getLayerName() {
        return layerName;
    }
    public Function<Dinosaur, Boolean> getRenderRequirement() {return renderRequirement;}

}
