package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;


public class SoundInit {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Constants.MODID, Registries.SOUND_EVENT);

    public static final DeferredSupplier<SoundEvent> TYRANNOSAUR_ROAR = registerSoundEvents("entity.tyrannosaurus_rex.vocals.roar");
    public static final DeferredSupplier<SoundEvent> TYRANNOSAUR_GROWL = registerSoundEvents("entity.tyrannosaurus_rex.vocals.growl");
    public static final DeferredSupplier<SoundEvent> TYRANNOSAUR_BREATH = registerSoundEvents("entity.tyrannosaurus_rex.ambient");
    public static final DeferredSupplier<SoundEvent> TYRANNOSAUR_SNARL = registerSoundEvents("entity.tyrannosaurus_rex.attack.snarl");
    public static final DeferredSupplier<SoundEvent> TYRANNOSAUR_BITE = registerSoundEvents("entity.tyrannosaurus_rex.attack.bite");
    public static final DeferredSupplier<SoundEvent> TYRANNOSAUR_HURT = registerSoundEvents("entity.tyrannosaurus_rex.hurt");
    public static final DeferredSupplier<SoundEvent> TYRANNOSAUR_DEATH = registerSoundEvents("entity.tyrannosaurus_rex.death");


    public static void loadClass() {
        SOUND_EVENTS.register();
    }

    private static RegistrySupplier<SoundEvent> registerSoundEvents(String name) {
        ResourceLocation id = new ResourceLocation(Constants.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

}
