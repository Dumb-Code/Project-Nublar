package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.DinosaurPart;
import net.dumbcode.projectnublar.entity.dinosaur.carnivore.DilophosaurusEntity;
import net.dumbcode.projectnublar.entity.dinosaur.carnivore.TyrannosaurusRexEntity;
import net.dumbcode.projectnublar.entity.dinosaur.carnivore.VelociraptorEntity;
import net.dumbcode.projectnublar.entity.dinosaur.herbivore.BrachiosaurusEntity;
import net.dumbcode.projectnublar.entity.dinosaur.herbivore.TriceratopsEntity;
import net.dumbcode.projectnublar.entity.dinosaur.omnivore.GallimimusEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

/**
 * Registers every mod entity type and collects the attribute suppliers that the loader-specific
 * code (e.g. Forge's attribute-creation event) applies later.
 *
 * <p>The registered id strings are registry contracts and must never change.
 */
public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Constants.MODID, Registries.ENTITY_TYPE);

    /** Attribute builders queued during registration, consumed by the active mod loader. */
    public static final List<AttributesRegister<?>> attributeSuppliers = new ArrayList<>();

    // Carnivores.
    public static final DeferredSupplier<EntityType<TyrannosaurusRexEntity>> TYRANNOSAURUS_REX =
            registerEntity("tyrannosaurus_rex",
                    () -> EntityType.Builder.of(TyrannosaurusRexEntity::new, MobCategory.MONSTER).sized(1, 3),
                    Dinosaur::createAttributes);
    public static final DeferredSupplier<EntityType<VelociraptorEntity>> VELOCIRAPTOR =
            registerEntity("velociraptor",
                    () -> EntityType.Builder.of(VelociraptorEntity::new, MobCategory.MONSTER).sized(0.6F, 0.6F),
                    Dinosaur::createAttributes);
    public static final DeferredSupplier<EntityType<DilophosaurusEntity>> DILOPHOSAURUS =
            registerEntity("dilophosaurus",
                    () -> EntityType.Builder.of(DilophosaurusEntity::new, MobCategory.MONSTER).sized(1, 1),
                    Dinosaur::createAttributes);

    // Herbivores.
    public static final DeferredSupplier<EntityType<TriceratopsEntity>> TRICERATOPS =
            registerEntity("triceratops",
                    () -> EntityType.Builder.of(TriceratopsEntity::new, MobCategory.MONSTER).sized(2, 3),
                    Dinosaur::createAttributes);
    public static final DeferredSupplier<EntityType<BrachiosaurusEntity>> BRACHIOSAURUS =
            registerEntity("brachiosaurus",
                    () -> EntityType.Builder.of(BrachiosaurusEntity::new, MobCategory.MONSTER).sized(2, 3),
                    Dinosaur::createAttributes);

    // Omnivores.
    public static final DeferredSupplier<EntityType<GallimimusEntity>> GALLIMIMUS =
            registerEntity("gallimimus",
                    () -> EntityType.Builder.of(GallimimusEntity::new, MobCategory.MONSTER).sized(2, 3),
                    Dinosaur::createAttributes);

    /** Invisible helper entity used for multi-part dinosaur hitboxes (e.g. the T-Rex head). */
    public static final DeferredSupplier<EntityType<DinosaurPart>> DINOSAUR_PART =
            registerEntity("dinosaur_part_entity",
                    () -> EntityType.Builder.<DinosaurPart>of(DinosaurPart::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f));

    private static <T extends Entity> DeferredSupplier<EntityType<T>> registerEntity(
            String name, Supplier<EntityType.Builder<T>> supplier) {
        return ENTITIES.register(name, () -> supplier.get().build(Constants.MODID + ":" + name));
    }

    private static <T extends LivingEntity> DeferredSupplier<EntityType<T>> registerEntity(
            String name,
            Supplier<EntityType.Builder<T>> supplier,
            Supplier<AttributeSupplier.Builder> attributeSupplier) {
        DeferredSupplier<EntityType<T>> entityTypeSupplier = registerEntity(name, supplier);
        attributeSuppliers.add(new AttributesRegister<>(entityTypeSupplier, attributeSupplier));
        return entityTypeSupplier;
    }

    public static void loadClass() {
        ENTITIES.register();
    }

    /** Pairs an entity type with the attribute builder the loader must register for it. */
    public record AttributesRegister<E extends LivingEntity>(
            Supplier<EntityType<E>> entityTypeSupplier, Supplier<AttributeSupplier.Builder> factory) {}
}
