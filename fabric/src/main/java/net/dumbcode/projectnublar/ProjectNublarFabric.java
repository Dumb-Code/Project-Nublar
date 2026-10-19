package net.dumbcode.projectnublar;


import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.registry.EntityInit;
import net.dumbcode.projectnublar.registry.SensorTypesInit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraftforge.fml.config.ModConfig;

public class ProjectNublarFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ForgeConfigRegistry.INSTANCE.register(Constants.MODID, ModConfig.Type.COMMON, FossilsConfig.CONFIG_SPEC, "projectnublar-fossils.toml");
        ProjectNublar.init();
        SensorTypesInit.init();
        EntityInit.attributeSuppliers.forEach(
                p -> FabricDefaultAttributeRegistry.register(p.entityTypeSupplier().get(), p.factory().get().build())
        );
    }
}
