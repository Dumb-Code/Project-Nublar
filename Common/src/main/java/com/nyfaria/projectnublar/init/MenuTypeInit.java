package com.nyfaria.projectnublar.init;

import com.nyfaria.projectnublar.Constants;
import com.nyfaria.projectnublar.menutypes.ProcessorMenu;
import com.nyfaria.projectnublar.registration.RegistrationProvider;
import com.nyfaria.projectnublar.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class MenuTypeInit {
    public static RegistrationProvider<MenuType<?>> MENU_TYPES = RegistrationProvider.get(Registries.MENU, Constants.MODID);
    public static RegistryObject<MenuType<ProcessorMenu>> PROCESSOR = MENU_TYPES.register("processor", () -> new MenuType<>(ProcessorMenu::new, FeatureFlags.VANILLA_SET));

    public static void loadClass() {

    }
}
