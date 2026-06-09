package net.dumbcode.projectnublar;


import net.dumbcode.projectnublar.registry.EntityInit;
import net.dumbcode.projectnublar.registry.SensorTypesInit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ProjectNublarFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ProjectNublar.init();
        SensorTypesInit.init();
        EntityInit.attributeSuppliers.forEach(
                p -> FabricDefaultAttributeRegistry.register(p.entityTypeSupplier().get(), p.factory().get().build())
        );
    }
}
