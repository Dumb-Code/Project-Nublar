package net.dumbcode.projectnublar.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.dumbcode.projectnublar.TestBootstrap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** The block-entity and entity side of the {@code version} key: the same meaning as the item codecs (P7). */
class SaveVersionTest {

    @BeforeAll
    static void bootstrap() {
        TestBootstrap.ensure();
    }

    @Test
    void writeThenReadGivesCurrent() {
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        SaveVersion.write(output);
        CompoundTag saved = output.buildResult();
        assertEquals(SaveVersion.CURRENT, SaveVersion.read(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, saved)));
    }

    @Test
    void missingKeyReadsAsZero() {
        assertEquals(0, SaveVersion.read(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, new CompoundTag())));
    }
}
