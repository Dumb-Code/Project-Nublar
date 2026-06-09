package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class AttributesInit {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Constants.MODID, Registries.ATTRIBUTE);

    public static final DeferredSupplier<Attribute> DINO_HUNGER_NEED = ATTRIBUTES.register("dinosaur_hunger", () -> new RangedAttribute(
            // The translation key to use.
            "attributes.projectnublar.dinosaur_hunger",
            // The default value.
            250,
            // Min and max values.
            0,
            100000
    ).setSyncable(true));

    public static final DeferredSupplier<Attribute> TRUST_SCORE = ATTRIBUTES.register("dinosaur_trust", () -> new RangedAttribute(
            // The translation key to use.
            "attributes.projectnublar.dinosaur_hunger",
            // The default value.
            250,
            // Min and max values.
            0,
            100000
    ).setSyncable(true));

    public static final DeferredSupplier<Attribute> DINO_SOCIAL_NEED = ATTRIBUTES.register("dinosaur_social", () -> new RangedAttribute(
            "attributes.projectnublar.dinosaur_social",250,0,10000).setSyncable(true));

    public static final DeferredSupplier<Attribute> DINO_THIRST_NEED = ATTRIBUTES.register("dinosaur_thirst", () -> new RangedAttribute(
            "attributes.projectnublar.dinosaur_thirst", 250, 0, 10000).setSyncable(true));

    public static final DeferredSupplier<Attribute> DINO_ENERGY_NEED = ATTRIBUTES.register("dinosaur_stamina", () -> new RangedAttribute(
            "attributes.projectnublar.dinosaur_stamina", 250, 0, 10000).setSyncable(true));

    public static final DeferredSupplier<Attribute> DINO_AGGRESSION = ATTRIBUTES.register("dinosaur_aggression", () -> new RangedAttribute(
            "attributes.projectnublar.dinosaur_aggression", 0, 0, 1000).setSyncable(true));

    public static final DeferredSupplier<Attribute> DINO_INTELLIGENCE = ATTRIBUTES.register("dinosaur_intelligence", () -> new RangedAttribute(
            "attributes.projectnublar.dinosaur_intelligence", 50, 0, 1000).setSyncable(true));

    public static final DeferredSupplier<Attribute> DINO_FERTILITY = ATTRIBUTES.register("dinosaur_fertility", () -> new RangedAttribute(
            "attributes.projectnublar.dinosaur_fertility", 100, 0, 1000).setSyncable(true));

    public static final DeferredSupplier<Attribute> DINO_VISION = ATTRIBUTES.register("dinosaur_vision", () -> new RangedAttribute(
            "attributes.projectnublar.dinosaur_vision", 50, 0, 1000).setSyncable(true));

    public static final DeferredSupplier<Attribute> DINO_IMMUNITY = ATTRIBUTES.register("dinosaur_immunity", () -> new RangedAttribute(
            "attributes.projectnublar.dinosaur_immunity", 0, 0, 1000).setSyncable(true));

    public static void loadClass(){ATTRIBUTES.register();}

}
