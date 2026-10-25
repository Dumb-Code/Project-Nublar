package net.dumbcode.projectnublar.util;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.dinosaur.animation.AnimationChoice;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.loading.object.BakedAnimations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tells whether a species' animation file defines a given animation.
 *
 * <p>Why: the animation predicates ({@code DinoAnimator}) ask for "dead", "getup", "eat1", "drink" and "roar" on
 * every frame, but not every species file defines them. When GeckoLib cannot find a name it prints "Unable to find animation"
 * to stdout (no logger level silences it) and retries on the next frame, so the log grows by hundreds of thousands
 * of lines. The predicate now asks this class first and does not request a missing animation.
 *
 * <p>Render path (fix P10): each action state has an {@link AnimationChoice} candidate list. {@link #available}
 * and {@link #first} give the candidate indices the species has, cached per species and state, and one shared
 * {@link RawAnimation} per candidate ({@link #raw}). They allocate nothing after the first lookup for a
 * (species, state) pair; that lookup, and a one-time WARN when the species has none of the candidates, are the
 * only slow steps.
 *
 * <p>Client safety: {@link GeckoLibCache#getBakedAnimations()} is empty on a dedicated server, and it is empty on the
 * client until the first resource load ends. In both cases the answer is "available" and is not cached, so the
 * behaviour matches the old code until real data exists. A resource reload (F3+T) replaces the {@link BakedAnimations}
 * instance, which invalidates the cached answers for that species.
 *
 * <p>Client only: {@link #available} and {@link #first} run on the client (render) thread. Server code must not
 * use this class: it reads the client's GeckoLib cache. The server reads animation lengths with
 * {@link DinoAnimationLengths}.
 *
 * <p>The animation file is found by the same rule {@code DefaultedEntityGeoModel} uses:
 * {@code <namespace>:animations/entity/<registry path>.animation.json}. All species are registered this way
 * (see {@code ClientRegistrationHolder}). A species that does not follow it is treated as "available".
 */
public final class DinoAnimationAvailability {
    private static final Map<EntityType<?>, Species> SPECIES = new HashMap<>();
    private static final AnimationChoice[] CHOICES = AnimationChoice.values();
    /** One shared animation per candidate: {@code RAW[choice][candidate]}. */
    private static final RawAnimation[][] RAW = new RawAnimation[CHOICES.length][];
    /** Every candidate index, per choice: the answer while the file is not loaded. */
    private static final int[][] ALL = new int[CHOICES.length][];

    static {
        for (AnimationChoice choice : CHOICES) {
            RawAnimation[] raws = new RawAnimation[choice.size()];
            int[] all = new int[choice.size()];
            for (int i = 0; i < raws.length; i++) {
                raws[i] = build(choice.loop(), choice.name(i));
                all[i] = i;
            }
            RAW[choice.ordinal()] = raws;
            ALL[choice.ordinal()] = all;
        }
    }

    private DinoAnimationAvailability() {}

    /** Per-species cache. The cached answers are only valid while {@code baked} is the current file instance. */
    private static final class Species {
        final ResourceLocation file;
        final String name;
        BakedAnimations baked;
        /** Client: available candidate indices per choice, {@code null} until looked up. */
        final int[][] indices = new int[CHOICES.length][];

        Species(ResourceLocation file, String name) {
            this.file = file;
            this.name = name;
        }
    }

    /**
     * The candidate indices of {@code choice} the species has, in candidate order (every index while the file is
     * not loaded). Client thread. The returned array is shared: do not change it.
     */
    public static int[] available(EntityType<?> type, AnimationChoice choice) {
        Species species = speciesOf(SPECIES, type);
        BakedAnimations baked = current(species);
        if (baked == null) {
            return ALL[choice.ordinal()];
        }
        int[] known = species.indices[choice.ordinal()];
        if (known == null) {
            known = choice.availableIndices(name -> baked.getAnimation(name) != null);
            species.indices[choice.ordinal()] = known;
            if (known.length == 0) {
                Constants.LOG.warn("No {} animation in {}; {} will not play one", choice, species.file, species.name);
            }
        }
        return known;
    }

    /** The first candidate of {@code choice} the species has, or {@code null} when it has none. Client thread. */
    public static @Nullable RawAnimation first(EntityType<?> type, AnimationChoice choice) {
        int[] available = available(type, choice);
        return available.length == 0 ? null : RAW[choice.ordinal()][available[0]];
    }

    /** The shared animation for one candidate of {@code choice}. */
    public static RawAnimation raw(AnimationChoice choice, int candidate) {
        return RAW[choice.ordinal()][candidate];
    }

    private static Species speciesOf(Map<EntityType<?>, Species> cache, EntityType<?> type) {
        Species species = cache.get(type);
        if (species == null) {
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            species = new Species(new ResourceLocation(key.getNamespace(), "animations/entity/" + key.getPath() + ".animation.json"), key.getPath());
            cache.put(type, species);
        }
        return species;
    }

    /** The species' loaded file, or {@code null}; a new file instance (a reload) clears the cached answers. */
    private static @Nullable BakedAnimations current(Species species) {
        BakedAnimations baked = GeckoLibCache.getBakedAnimations().get(species.file);
        if (baked != null && baked != species.baked) {
            species.baked = baked;
            Arrays.fill(species.indices, null);
        }
        return baked;
    }

    private static RawAnimation build(AnimationChoice.Loop loop, String name) {
        return switch (loop) {
            case LOOP -> RawAnimation.begin().thenLoop(name);
            case FILE -> RawAnimation.begin().thenPlay(name);
            case HOLD -> RawAnimation.begin().thenPlayAndHold(name);
        };
    }
}
