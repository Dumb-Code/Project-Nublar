package net.dumbcode.projectnublar.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.Dinosaur;
import net.dumbcode.projectnublar.api.Dinosaurs;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.DinosaurPart;
import net.dumbcode.projectnublar.entity.dinosaur.carnivore.TyrannosaurusRexEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Constants.MODID, Registries.ENTITY_TYPE);
    public static final List<AttributesRegister<?>> attributeSuppliers = new ArrayList<>();



    public static final DeferredSupplier<EntityType<TyrannosaurusRexEntity>> TYRANNOSAURUS_REX_ENTITY = registerTyrannosaurusRexEntity();


    public static final DeferredSupplier<EntityType<DinosaurPart>> DINOSAUR_PART = registerEntity("dinosaur_part_entity", () -> EntityType.Builder.<DinosaurPart>of(DinosaurPart::new, MobCategory.MISC).sized(0.5f,0.5f));

    public static <T extends Entity> DeferredSupplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> supplier) {
        return ENTITIES.register(name, () -> supplier.get().build(Constants.MODID + ":" + name));
    }
    public static <T extends LivingEntity> DeferredSupplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> supplier,
                                                                                         Supplier<AttributeSupplier.Builder> attributeSupplier) {
        DeferredSupplier<EntityType<T>> entityTypeSupplier = registerEntity(name, supplier);
        attributeSuppliers.add(new AttributesRegister<>(entityTypeSupplier, attributeSupplier));
        return entityTypeSupplier;
    }
    public static DeferredSupplier<EntityType<TyrannosaurusRexEntity>> registerTyrannosaurusRexEntity(){
       DeferredSupplier<EntityType<TyrannosaurusRexEntity>> tyrannosaurus = registerEntity(DinosaurInit.TYRANNOSAURUS_REX_ID, () -> EntityType.Builder.of(TyrannosaurusRexEntity::new, MobCategory.MONSTER).sized(2f,4f),TyrannosaurusRexEntity::createAttributes);
       return tyrannosaurus;
    }





    public static void loadClass() {
        ENTITIES.register();
    }


    public record AttributesRegister<E extends LivingEntity>(Supplier<EntityType<E>> entityTypeSupplier, Supplier<AttributeSupplier.Builder> factory) {}
}
