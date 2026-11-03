package net.dumbcode.projectnublar;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import java.util.List;
import net.dumbcode.projectnublar.block.api.fence.WireSpoolRepair;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.energy.TeamRebornEnergySwitch;
import net.dumbcode.projectnublar.network.NublarPayloads;
import net.dumbcode.projectnublar.registry.EntityInit;
import net.dumbcode.projectnublar.registry.RecipeInit;
import net.dumbcode.projectnublar.registry.SensorTypesInit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.fml.config.ModConfig;

/**
 * The Fabric main entry point (the {@code main} entrypoint in {@code fabric.mod.json}). It runs on
 * both sides and registers what NeoForge registers through its events: the fossils config, entity
 * attributes, the fossil veins, the serverbound payloads, the recipe sync and the wire-spool
 * repair, plus the Fabric-only Team Reborn Energy switch. The client entry point is
 * {@code ProjectNublarClientFabric}.
 */
public class ProjectNublarFabric implements ModInitializer {

    /**
     * The fossil vein placed features of {@code worldgen/placed_feature} in the generated
     * resources, one per host stone; NeoForge adds the same three through its biome modifiers.
     */
    private static final List<String> FOSSIL_VEINS = List.of(
            "stone_cretaceous_fossils_placed",
            "sandstone_cretaceous_fossils_placed",
            "deepslate_cretaceous_fossils_placed");

    /** Creates the entry point; Fabric Loader calls it before {@link #onInitialize()}. */
    public ProjectNublarFabric() {
    }

    @Override
    public void onInitialize() {
        ProjectNublar.init();
        SensorTypesInit.init();
        // Before the config: Forge Config API Port fires the loading event inside register.
        TeamRebornEnergySwitch.init();
        ConfigRegistry.INSTANCE.register(Constants.MODID, ModConfig.Type.COMMON, FossilsConfig.CONFIG_SPEC, "projectnublar-fossils.toml");
        EntityInit.attributeSuppliers.forEach(
                p -> FabricDefaultAttributeRegistry.register(p.entityTypeSupplier().get(), p.factory().get().build()));
        FOSSIL_VEINS.forEach(vein -> BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, Constants.modLoc(vein))));
        NublarPayloads.SERVERBOUND.forEach(ProjectNublarFabric::registerServerbound);
        // Fabric API syncs recipes by serializer, so only Nublar's crafting recipes travel to clients.
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeInit.UNINCUBATED_EGG.get());
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
                WireSpoolRepair.tryRepair(level, player, hand, hit.getBlockPos(), hit.getDirection(), player.getItemInHand(hand))
                        // FAIL stops the click and, like NeoForge's cancelled event (PASS), consumes nothing and swings nothing.
                        ? InteractionResult.FAIL
                        : InteractionResult.PASS);
    }

    /**
     * Registers one serverbound payload type and its receiver. The handler runs on the server
     * thread with the sending player.
     */
    private static <T extends CustomPacketPayload> void registerServerbound(NublarPayloads.Entry<T> entry) {
        PayloadTypeRegistry.serverboundPlay().register(entry.type(), entry.codec());
        ServerPlayNetworking.registerGlobalReceiver(entry.type(), (payload, context) ->
                context.server().execute(() -> entry.handler().accept(payload, context.player())));
    }
}
