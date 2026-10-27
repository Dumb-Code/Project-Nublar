package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.api.fence.WireSpoolRepair;
import net.dumbcode.projectnublar.platform.NeoForgePlatformHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * NeoForge events of the <b>game bus</b> ({@code NeoForge.EVENT_BUS}), on both sides: the server
 * data reload listeners and the wire-spool fence repair.
 *
 * <p>Only game-bus events go here; mod-bus events go in {@code NeoForgeModEvents}.
 */
@EventBusSubscriber(modid = Constants.MODID)
public final class NeoForgeGameEvents {

    /** Adds the listeners that {@code NublarReloadListeners} registered, on every data pack load. */
    @SubscribeEvent
    public static void addReloadListeners(AddServerReloadListenersEvent event) {
        NeoForgePlatformHelper.serverReloadListeners().forEach(event::addListener);
    }

    /** Cancels the click when a wire spool repaired a fence wire (see {@link WireSpoolRepair}). */
    @SubscribeEvent
    public static void repairWire(PlayerInteractEvent.RightClickBlock event) {
        if (WireSpoolRepair.tryRepair(event.getLevel(), event.getEntity(), event.getHand(), event.getPos(),
                event.getFace(), event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    private NeoForgeGameEvents() {
    }
}
