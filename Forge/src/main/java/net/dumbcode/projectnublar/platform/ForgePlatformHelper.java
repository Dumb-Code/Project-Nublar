package net.dumbcode.projectnublar.platform;

import com.ibm.icu.impl.Trie;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.dumbcode.projectnublar.platform.services.IPlatformHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Consumer;

public class ForgePlatformHelper<T extends AbstractContainerMenu> implements IPlatformHelper<T> {

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public MenuType<SequencerMenu> registerSequenceMenu() {
        return IForgeMenuType.create(SequencerMenu::new);
    }
    @Override
    public MenuType<IncubatorMenu> registerIncubatorMenu() {
        return IForgeMenuType.create(IncubatorMenu::new);
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> buf) {
        NetworkHooks.openScreen(player, provider, buf);
    }
}