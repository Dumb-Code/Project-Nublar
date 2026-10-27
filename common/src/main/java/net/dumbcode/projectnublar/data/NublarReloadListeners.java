package net.dumbcode.projectnublar.data;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.platform.Services;

/**
 * Registers the mod's four server data reload listeners with the loader, in the order the 1.20.1
 * Forge event added them: gene data, behaviour data, diets, fossil config. Each listener reads its
 * own data pack folder on every load and reload.
 */
public final class NublarReloadListeners {

    /** Registers the four listeners; {@code ProjectNublar.init} calls it once on each loader. */
    public static void register() {
        Services.PLATFORM.registerServerReloadListener(Constants.modLoc("gene_data"), new GeneDataReloadListener());
        Services.PLATFORM.registerServerReloadListener(Constants.modLoc("behaviour_data"), new BehaviourDataReloadListener());
        Services.PLATFORM.registerServerReloadListener(Constants.modLoc("diet"), new DietReloadListener());
        Services.PLATFORM.registerServerReloadListener(Constants.modLoc("fossil_config"), new FossilConfigReloadListener());
    }

    private NublarReloadListeners() {
    }
}
