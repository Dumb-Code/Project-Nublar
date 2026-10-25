package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.menutypes.EggPrinterMenu;
import net.dumbcode.projectnublar.menutypes.GeneratorMenu;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.menutypes.ProcessorMenu;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.dumbcode.projectnublar.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/**
 * Registers the machine menu types. {@code sequencer} and {@code incubator} are created through
 * the platform helper because their factories differ per loader. The id strings are frozen.
 */
public class MenuTypeInit {
    public static DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Constants.MODID, Registries.MENU);
    public static DeferredSupplier<MenuType<ProcessorMenu>> PROCESSOR = MENU_TYPES.register("processor", () -> new MenuType<>(ProcessorMenu::new, FeatureFlags.VANILLA_SET));
    public static DeferredSupplier<MenuType<SequencerMenu>> SEQUENCER = MENU_TYPES.register("sequencer", Services.PLATFORM::registerSequenceMenu);
    public static DeferredSupplier<MenuType<EggPrinterMenu>> EGG_PRINTER = MENU_TYPES.register("egg_printer", () -> new MenuType<>(EggPrinterMenu::new, FeatureFlags.VANILLA_SET));
    public static DeferredSupplier<MenuType<IncubatorMenu>> INCUBATOR = MENU_TYPES.register("incubator", Services.PLATFORM::registerIncubatorMenu);
    public static DeferredSupplier<MenuType<GeneratorMenu>> GENERATOR_MENU = MENU_TYPES.register("generator", ()-> new MenuType<>(GeneratorMenu::new, FeatureFlags.VANILLA_SET));

    public static void loadClass() {
        MENU_TYPES.register();
    }
}
