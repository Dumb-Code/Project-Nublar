package net.dumbcode.projectnublar.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.dinosaur.animation.AnimationChoice;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Animation lengths for the server's timed animations (fix P10): one-shots (call, sentry, startled) and the
 * flinch state. The client also reads idle and leg cycle lengths here (fix P12a).
 * <p>
 * Common code only, no GeckoLib and no client class: the server must not touch the client's resource cache
 * ({@code GeckoLibCache} belongs to the client's resource reload). It reads the species' animation file
 * ({@code assets/<namespace>/animations/entity/<registry path>.animation.json}, the rule
 * {@code DinoAnimationAvailability} uses) from the mod's own classpath, once per species, and keeps each
 * animation's {@code animation_length} in seconds. A resource pack that replaces the file on a client is not seen.
 * When the file cannot be read, every answer is {@link AnimationChoice#LENGTH_UNKNOWN} and callers use their
 * fallback lengths.
 * <p>
 * Thread-safe (a lock); called rarely: when a one-shot starts, when a flinch is counted, and on the client when an
 * idle is picked or the leg cycle lengths are first needed.
 */
public final class DinoAnimationLengths {
    /** Species to (animation name to seconds, NaN without a length); an empty map when the file is unreadable. */
    private static final Map<EntityType<?>, Map<String, Double>> SECONDS = new HashMap<>();

    private DinoAnimationLengths() {}

    /**
     * The length in ticks of the first candidate of {@code choice} the species has
     * ({@link AnimationChoice#lengthTicks}), {@link AnimationChoice#LENGTH_NOT_DEFINED} when it has none, or
     * {@link AnimationChoice#LENGTH_UNKNOWN} when the file cannot be read.
     */
    public static int lengthTicks(EntityType<?> type, AnimationChoice choice) {
        Map<String, Double> seconds = secondsOf(type);
        return seconds.isEmpty() ? AnimationChoice.LENGTH_UNKNOWN : choice.lengthTicks(seconds);
    }

    /**
     * The length in ticks of one candidate of {@code choice} ({@link AnimationChoice#lengthTicks(Map, int)}), or
     * {@link AnimationChoice#LENGTH_UNKNOWN} when the file cannot be read. Fix P12a: the client's idle pick and leg
     * cycles use it too.
     */
    public static int lengthTicks(EntityType<?> type, AnimationChoice choice, int candidate) {
        Map<String, Double> seconds = secondsOf(type);
        return seconds.isEmpty() ? AnimationChoice.LENGTH_UNKNOWN : choice.lengthTicks(seconds, candidate);
    }

    private static Map<String, Double> secondsOf(EntityType<?> type) {
        synchronized (SECONDS) {
            return SECONDS.computeIfAbsent(type, DinoAnimationLengths::read);
        }
    }

    private static Map<String, Double> read(EntityType<?> type) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        String path = "/assets/" + key.getNamespace() + "/animations/entity/" + key.getPath() + ".animation.json";
        Map<String, Double> seconds = new HashMap<>();
        try (InputStream stream = DinoAnimationLengths.class.getResourceAsStream(path)) {
            if (stream == null) {
                Constants.LOG.warn("No animation file {} on the classpath; {} uses fallback animation lengths", path, key);
                return seconds;
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject animations = GsonHelper.parse(reader).getAsJsonObject("animations");
                for (Map.Entry<String, JsonElement> entry : animations.entrySet()) {
                    JsonElement length = entry.getValue().getAsJsonObject().get("animation_length");
                    seconds.put(entry.getKey(), length != null && length.isJsonPrimitive() ? length.getAsDouble() : Double.NaN);
                }
            }
        } catch (Exception e) {
            Constants.LOG.warn("Could not read animation lengths from {}; {} uses fallback animation lengths", path, key, e);
            seconds.clear();
        }
        return seconds;
    }
}
