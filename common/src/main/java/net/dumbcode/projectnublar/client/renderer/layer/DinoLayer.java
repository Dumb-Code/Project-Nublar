package net.dumbcode.projectnublar.client.renderer.layer;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
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
    /**
     * The layer this one is drawn directly below, or {@code null} to draw it at its place in the list. A layer's
     * place in the list picks its colour, so a layer whose art lies under an earlier layer moves with this, not by
     * its place.
     */
    private @Nullable String drawnBelow;
    private static final String MALE = "male";
    private static final String FEMALE = "female";
    /** Entity type the cached texture locations belong to. */
    private @Nullable EntityType<?> cachedType;
    private @Nullable Identifier cachedMale;
    private @Nullable Identifier cachedFemale;

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

    /**
     * Draws this layer directly below the layer named {@code layerName} of the same list, as the art stacks them.
     * The layer keeps its place in the list, and with it its colour. Call it once, while the layer is built.
     */
    public DinoLayer below(String layerName) {
        this.drawnBelow = layerName;
        return this;
    }

    /**
     * Returns the draw rank of each layer of {@code layers}, by its place in the list: rank 0 is drawn first, at
     * the bottom. Layers are drawn in list order, except that a layer set {@link #below} another is drawn directly
     * below it; layers set below the same layer keep their list order.
     *
     * @throws IllegalArgumentException if a layer is set below a name that is not in the list, or below a layer
     *                                  that is itself set below another
     */
    public static int[] drawOrder(List<DinoLayer> layers) {
        List<DinoLayer> sequence = new ArrayList<>(layers.size());
        for (DinoLayer layer : layers) {
            if (layer.drawnBelow == null) {
                sequence.add(layer);
            }
        }
        for (DinoLayer layer : layers) {
            if (layer.drawnBelow != null) {
                sequence.add(indexOfTarget(sequence, layer), layer);
            }
        }
        int[] ranks = new int[layers.size()];
        for (int i = 0; i < ranks.length; i++) {
            ranks[i] = sequence.indexOf(layers.get(i));
        }
        return ranks;
    }

    /** Returns where the layer that {@code layer} is drawn below stands in {@code sequence}. */
    private static int indexOfTarget(List<DinoLayer> sequence, DinoLayer layer) {
        for (int i = 0; i < sequence.size(); i++) {
            DinoLayer target = sequence.get(i);
            if (target.drawnBelow == null && target.layerName.equals(layer.drawnBelow)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Layer " + layer.layerName + " is drawn below " + layer.drawnBelow
                + ", which is not a layer of the list drawn at its own place");
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
     * render path free of string and {@link Identifier} allocation. Vanilla memoizes
     * {@code RenderType.entityTranslucent} per {@link Identifier}, so a stable instance here is enough to avoid
     * a render type lookup that allocates.
     * Called on the render thread only, so the cache needs no locking.
     */
    public Identifier getTextureLocation(Dinosaur dino) {
        EntityType<?> type = dino.getType();
        if (type != cachedType) {
            cachedType = type;
            cachedMale = null;
            cachedFemale = null;
        }
        boolean male = MALE.equals(dino.getStringDinoGender());
        Identifier cached = male ? cachedMale : cachedFemale;
        if (cached == null) {
            Identifier dinoLoc = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            cached = Identifier.fromNamespaceAndPath(dinoLoc.getNamespace(),"textures/entity/" + dinoLoc.getPath() + "/" + (male ? MALE : FEMALE) + "/" + layerName + ".png");
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
