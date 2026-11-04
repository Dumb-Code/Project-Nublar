package net.dumbcode.projectnublar;

import net.dumbcode.projectnublar.block.api.multiblock.MultiEntityBlock;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge energy at every block of a machine, so a cable from another mod connects at any face
 * of the Processor, Sequencer, Egg Printer and Incubator.
 *
 * <p>Each machine fills several block spaces, and only the core space (rows, columns and depth all
 * 0) has the block entity that stores the energy. Balm answers {@code Capabilities.Energy.BLOCK}
 * through the block entity, so the other spaces had no energy handler. This class gives the four
 * machine blocks a provider of their own: at a non-core space it answers with the lookup at the
 * machine's core, so Balm's provider stays the single source of the handler. At the core it
 * returns null, and Balm's provider answers, as before: NeoForge asks each provider of a block in
 * turn until one answers.
 *
 * <p>A mod-bus listener; {@link EventBusSubscriber} registers it.
 */
@EventBusSubscriber(modid = Constants.MODID)
public final class NeoForgeMachineEnergy {

    /**
     * Registers the core lookup for the four machine blocks.
     *
     * @param event the capability registration event
     */
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.Energy.BLOCK, NeoForgeMachineEnergy::coreEnergy,
                BlockInit.PROCESSOR.get(),
                BlockInit.SEQUENCER.get(),
                BlockInit.EGG_PRINTER.get(),
                BlockInit.INCUBATOR.get());
    }

    /**
     * The energy handler of the machine that the block at {@code pos} belongs to: the lookup at
     * its core ({@link MultiEntityBlock#findCore}). Null at the core itself, so Balm's provider
     * answers there, and null when {@code findCore} finds no core.
     */
    private static @Nullable EnergyHandler coreEnergy(Level level, BlockPos pos, BlockState state,
                                                      @Nullable BlockEntity blockEntity,
                                                      @Nullable Direction side) {
        BlockPos corePos = MultiEntityBlock.findCore(level, pos, state);
        if (corePos == null || corePos.equals(pos)) {
            return null;
        }
        return level.getCapability(Capabilities.Energy.BLOCK, corePos, side);
    }

    private NeoForgeMachineEnergy() {
    }
}
