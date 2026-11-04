package net.dumbcode.projectnublar.energy;

import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.api.multiblock.MultiBlock;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/**
 * The Fabric switch for {@code compat.team_reborn_energy_bridge}, and the Team Reborn Energy
 * provider at every block of a machine. The value is read once, when the fossils config loads at
 * game start.
 *
 * <p>When the key is {@code true}, Balm's Team Reborn Energy fallback offers the energy of every
 * Nublar storage block to other mods. Balm finds the storage through the block entity, and of a
 * machine (Processor, Sequencer, Egg Printer, Incubator) only the core block has one, so this class
 * gives the four machine blocks a provider of their own: at a non-core block it answers with the
 * lookup at the machine's core, so a cable that touches any face of a machine reaches the core's
 * storage through Balm's fallback, the single source of that storage. At the core it returns null
 * and Balm's fallback answers, as before.
 *
 * <p>When the key is {@code false}, every block of the four machines and the other three Nublar
 * storage block entities get an explicit {@code EnergyStorage.SIDED} provider that returns
 * {@link EnergyStorage#EMPTY}. Fabric API's {@code BlockApiLookupImpl.find} asks the block's own
 * provider first and goes on to the fallback providers when that one returns null, so a null
 * provider would let Balm's fallback serve every Nublar storage. A non-null {@code EMPTY} stops the
 * lookup there. A Team Reborn Energy cable can still draw a connection to a Nublar block, but no
 * energy moves: {@code EMPTY} accepts and gives nothing. A block has one provider at most: Fabric
 * API keeps the first registration for a block and ignores a later one with a warning, so the
 * machine blocks are registered once, by block, in either case.
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
     * {@code ConfigRegistry.register}. The blocks and block entity types must be registered
     * already.
     */
    public static void init() {
        ModConfigEvents.loading(Constants.MODID).register(config -> {
            if (loaded || config.getSpec() != FossilsConfig.CONFIG_SPEC) {
                return;
            }
            loaded = true;
            bridgeEnabled = FossilsConfig.teamRebornEnergyBridge();
            Block[] machines = {
                    BlockInit.PROCESSOR.get(),
                    BlockInit.SEQUENCER.get(),
                    BlockInit.EGG_PRINTER.get(),
                    BlockInit.INCUBATOR.get()};
            if (bridgeEnabled) {
                EnergyStorage.SIDED.registerForBlocks(TeamRebornEnergySwitch::coreStorage,
                        machines);
            } else {
                EnergyStorage.SIDED.registerForBlocks(
                        (level, pos, state, blockEntity, side) -> EnergyStorage.EMPTY, machines);
                EnergyStorage.SIDED.registerForBlockEntities(
                        (blockEntity, side) -> EnergyStorage.EMPTY,
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

    /**
     * The storage of the machine that the block at {@code pos} belongs to: the lookup at its core.
     * Null at the core itself, so the fallback providers (Balm's) answer there, and null when the
     * core is not loaded or the block there is not this machine's core, so a broken machine never
     * loads a chunk or loops between two blocks.
     */
    private static @Nullable EnergyStorage coreStorage(Level level, BlockPos pos, BlockState state,
                                                       @Nullable BlockEntity blockEntity,
                                                       @Nullable Direction side) {
        BlockPos corePos = MultiBlock.getCorePos(state, pos);
        if (corePos.equals(pos) || !level.isLoaded(corePos)) {
            return null;
        }
        BlockState coreState = level.getBlockState(corePos);
        if (!coreState.is(state.getBlock())
                || !MultiBlock.getCorePos(coreState, corePos).equals(corePos)) {
            return null;
        }
        return EnergyStorage.SIDED.find(level, corePos, coreState, null, side);
    }

    private TeamRebornEnergySwitch() {
    }
}
