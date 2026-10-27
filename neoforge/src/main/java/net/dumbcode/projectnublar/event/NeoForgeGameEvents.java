package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.api.fence.WireSpoolRepair;
import net.dumbcode.projectnublar.platform.NeoForgePlatformHelper;
import net.dumbcode.projectnublar.registry.RecipeInit;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * NeoForge events of the <b>game bus</b> ({@code NeoForge.EVENT_BUS}), on both sides: the server
 * data reload listeners, the recipe sync to clients and the wire-spool fence repair.
 *
 * <p>The recipe sync has an accepted cost: NeoForge 26.2.0 sends whole recipe types only, so every
 * crafting recipe of the pack, not only Nublar's, travels to each client once per login and once
 * per {@code /reload}. That is about the size of the vanilla recipe book.
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

    /**
     * Sends every recipe of the {@link RecipeInit#SYNCED_TYPES} types to the joining player, or to
     * all players after {@code /reload}. The client stores them in {@code ClientRecipeStore}.
     */
    @SubscribeEvent
    public static void syncRecipes(OnDatapackSyncEvent event) {
        RecipeInit.SYNCED_TYPES.forEach(type -> event.sendRecipes(type.get()));
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
