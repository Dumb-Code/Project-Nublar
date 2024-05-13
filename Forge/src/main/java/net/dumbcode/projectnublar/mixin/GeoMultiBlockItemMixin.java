package net.dumbcode.projectnublar.mixin;

import net.dumbcode.projectnublar.client.renderer.SimpleGeoItemRenderer;
import net.dumbcode.projectnublar.item.GeoMultiBlockItem;
import net.dumbcode.projectnublar.item.api.MultiBlockItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(GeoMultiBlockItem.class)
public abstract class GeoMultiBlockItemMixin extends MultiBlockItem {
    public GeoMultiBlockItemMixin(Block block, Properties properties, int rows, int columns, int depth) {
        super(block, properties, rows, columns, depth);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new SimpleGeoItemRenderer<>();
            }
        });
    }
}
