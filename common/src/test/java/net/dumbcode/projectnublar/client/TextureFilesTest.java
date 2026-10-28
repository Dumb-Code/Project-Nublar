package net.dumbcode.projectnublar.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Every texture file reads as an image. The pelvis fossil overlay shipped as an empty file for a year: the game only
 * logs "PNG header missing" and draws the missing texture, so nothing else catches a file that is empty or is not an
 * image.
 */
class TextureFilesTest {

    private static final String FOLDER = "/assets/projectnublar/textures";

    @Test
    void everyTextureReadsAsAnImage() throws IOException {
        Path root = textureFolder();
        List<Path> files;
        try (Stream<Path> walk = Files.walk(root)) {
            files = walk.filter(path -> path.toString().endsWith(".png")).sorted().toList();
        }
        assertFalse(files.isEmpty(), "no texture in " + FOLDER);
        List<String> problems = new ArrayList<>();
        for (Path file : files) {
            String name = root.relativize(file).toString();
            try {
                BufferedImage image = ImageIO.read(file.toFile());
                if (image == null) {
                    problems.add(name + ": not an image (" + Files.size(file) + " bytes)");
                }
            } catch (IOException e) {
                problems.add(name + ": " + e.getMessage());
            }
        }
        assertEquals(List.of(), problems);
    }

    private static Path textureFolder() {
        URL folder = TextureFilesTest.class.getResource(FOLDER);
        assertNotNull(folder, FOLDER + " is not on the test class path");
        try {
            return Path.of(folder.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
