package net.dumbcode.projectnublar.datagen;

import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/**
 * The {@code sounds.json} generator. Its body is disabled, so it writes nothing: the mod ships the
 * hand-written {@code sounds.json}.
 */
public class ModSoundProvider extends SoundDefinitionsProvider {
    public ModSoundProvider(PackOutput generator) {
        super(generator, Constants.MODID);
    }

    @Override
    public void registerSounds() {
        // SoundInit.SOUND_EVENTS.forEach(this::addSound);
    }

    /** One definition for the event, playing the sound file of the same name. */
    public void addSound(DeferredSupplier<SoundEvent> entry) {
        add(entry.get(), SoundDefinition.definition().with(sound(entry.getId())));
    }
}
