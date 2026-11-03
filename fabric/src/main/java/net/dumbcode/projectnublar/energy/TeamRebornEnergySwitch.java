package net.dumbcode.projectnublar.energy;

import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.registry.BlockInit;
import team.reborn.energy.api.EnergyStorage;

/**
 * The Fabric switch for {@code compat.team_reborn_energy_bridge}. The value is read once, when the
 * fossils config loads at game start.
 *
 * <p>When the key is {@code true}, this class registers nothing: Balm's Team Reborn Energy
 * fallback offers the energy of every Nublar storage block to other mods. When it is
 * {@code false}, the seven Nublar storage block entities get an explicit
 * {@code EnergyStorage.SIDED} provider that returns {@link EnergyStorage#EMPTY}. Fabric API's
 * {@code BlockApiLookupImpl.find} asks the block's own provider first and goes on to the fallback
 * providers when that one returns null, so a null provider would let Balm's fallback serve every
 * Nublar storage. A non-null {@code EMPTY} stops the lookup there. A Team Reborn Energy cable can
 * still draw a connection to a Nublar block, but no energy moves: {@code EMPTY} accepts and gives
 * nothing.
 *
 * <p>The switch never touches Nublar-to-Nublar power: the generator fills Nublar neighbours
 * through the direct {@code BalmEnergyStorageProvider} path, not through this lookup.
 * {@code FabricPlatformHelper.insertEnergy} reads {@link #bridgeEnabled()}, so the bridge is off
 * in both directions. NeoForge ignores the key.
 */
public final class TeamRebornEnergySwitch {

    private static boolean loaded;
    private static boolean bridgeEnabled = true;

    /**
     * Subscribes to the loading of the fossils config. Call it before the config is registered:
     * Forge Config API Port loads a common config, and fires its loading event, inside
     * {@code ConfigRegistry.register}. The block entity types must be registered already.
     */
    public static void init() {
        ModConfigEvents.loading(Constants.MODID).register(config -> {
            if (loaded || config.getSpec() != FossilsConfig.CONFIG_SPEC) {
                return;
            }
            loaded = true;
            bridgeEnabled = FossilsConfig.teamRebornEnergyBridge();
            if (!bridgeEnabled) {
                EnergyStorage.SIDED.registerForBlockEntities((blockEntity, side) -> EnergyStorage.EMPTY,
                        BlockInit.PROCESSOR_BLOCK_ENTITY.get(),
                        BlockInit.SEQUENCER_BLOCK_ENTITY.get(),
                        BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(),
                        BlockInit.INCUBATOR_BLOCK_ENTITY.get(),
                        BlockInit.GENERATOR.get(),
                        BlockInit.ELECTRIC_FENCE_POST_BLOCK_ENTITY.get(),
                        BlockInit.FEEDER_BLOCK_ENTITY.get());
            }
        });
    }

    /**
     * Whether Nublar blocks exchange energy with Team Reborn Energy, as read at game start.
     *
     * @return the value of {@code compat.team_reborn_energy_bridge} at the first config load;
     *         {@code true} before that load
     */
    public static boolean bridgeEnabled() {
        return bridgeEnabled;
    }

    private TeamRebornEnergySwitch() {
    }
}
