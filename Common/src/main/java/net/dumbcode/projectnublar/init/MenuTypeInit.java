package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.menutypes.ProcessorMenu;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.dumbcode.projectnublar.registration.RegistrationProvider;
import net.dumbcode.projectnublar.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class MenuTypeInit {
    public static RegistrationProvider<MenuType<?>> MENU_TYPES = RegistrationProvider.get(Registries.MENU, Constants.MODID);
    public static RegistryObject<MenuType<ProcessorMenu>> PROCESSOR = MENU_TYPES.register("processor", () -> new MenuType<>(ProcessorMenu::new, FeatureFlags.VANILLA_SET));
    public static RegistryObject<MenuType<SequencerMenu>> SEQUENCER = MENU_TYPES.register("sequencer", () -> new MenuType<>(SequencerMenu::new, FeatureFlags.VANILLA_SET));

    public static void loadClass() {

    }
}
