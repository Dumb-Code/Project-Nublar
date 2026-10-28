package net.dumbcode.projectnublar.client;

import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MathParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Every dinosaur animation file loads in GeckoLib 5 with all of its clips, and has no runaway rotation key.
 * GeckoLib 5 drops the whole file when one key has the wrong shape (the Triceratops {@code relative_to} object), and
 * drops a clip when one Molang value does not parse (the Brachiosaurus {@code getup}). In game this is silent apart
 * from a log line: the animal stands in its base pose or skips the clip. A rotation key of millions of degrees (an
 * exported {@code e-7} that lost its minus sign) loads, but turns the bone to a random angle every frame. The test
 * runs GeckoLib's own JSON loader, so it fails where the game would drop something.
 */
class DinosaurAnimationFilesTest {

    private static final String FOLDER = "/assets/projectnublar/geckolib/animations/entity";
    /** Two full turns. A larger rotation key is an export defect, not an intended spin. */
    private static final double MAX_ROTATION_DEGREES = 720;

    @Test
    void everyClipLoadsInGeckoLib5() throws IOException {
        List<String> problems = new ArrayList<>();
        for (Path file : animationFiles()) {
            JsonObject json = read(file);
            String name = file.getFileName().toString();
            BakedAnimations baked;
            try {
                baked = new GeckoLibGsonLoader().bakeGeckoLibAnimationsFile(
                        Identifier.fromNamespaceAndPath("projectnublar", "entity/" + name), json,
                        MathParser.createWithDeduplication());
            } catch (Throwable e) {
                problems.add(name + ": the whole file is dropped: " + e);
                continue;
            }
            for (String clip : json.getAsJsonObject("animations").keySet()) {
                if (baked.getAnimation(clip) == null) {
                    problems.add(name + ": clip '" + clip + "' is dropped (see the GeckoLib error above)");
                }
            }
        }
        assertEquals(List.of(), problems);
    }

    @Test
    void noRotationKeyIsOverTwoTurns() throws IOException {
        List<String> problems = new ArrayList<>();
        for (Path file : animationFiles()) {
            JsonObject clips = read(file).getAsJsonObject("animations");
            for (Map.Entry<String, JsonElement> clip : clips.entrySet()) {
                JsonObject bones = clip.getValue().getAsJsonObject().getAsJsonObject("bones");
                if (bones == null) {
                    continue;
                }
                for (Map.Entry<String, JsonElement> bone : bones.entrySet()) {
                    JsonElement rotation = bone.getValue().getAsJsonObject().get("rotation");
                    String where = file.getFileName() + " " + clip.getKey() + " " + bone.getKey() + " rotation";
                    if (rotation != null) {
                        findLargeNumbers(rotation, where, problems);
                    }
                }
            }
        }
        assertEquals(List.of(), problems);
    }

    /** Adds every number under {@code element} (any key shape: vector, time map, pre and post) over the limit. */
    private static void findLargeNumbers(JsonElement element, String where, List<String> problems) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> findLargeNumbers(child, where, problems));
        } else if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> child : element.getAsJsonObject().entrySet()) {
                findLargeNumbers(child.getValue(), where + " " + child.getKey(), problems);
            }
        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
                && Math.abs(element.getAsDouble()) > MAX_ROTATION_DEGREES) {
            problems.add(where + ": " + element.getAsString());
        }
    }

    private static List<Path> animationFiles() throws IOException {
        URL folder = DinosaurAnimationFilesTest.class.getResource(FOLDER);
        assertNotNull(folder, FOLDER + " is not on the test class path");
        List<Path> files;
        try (Stream<Path> list = Files.list(Path.of(folder.toURI()))) {
            files = list.filter(path -> path.toString().endsWith(".animation.json")).sorted().toList();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        assertFalse(files.isEmpty(), "no animation file in " + FOLDER);
        return files;
    }

    /** Reads the file the way GeckoLib's loader does ({@code GsonHelper.parse}). */
    private static JsonObject read(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GsonHelper.parse(reader);
        }
    }
}
