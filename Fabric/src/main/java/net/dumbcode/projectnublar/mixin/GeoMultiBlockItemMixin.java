package net.dumbcode.projectnublar.mixin;

import net.dumbcode.projectnublar.client.renderer.SimpleGeoItemRenderer;
import net.dumbcode.projectnublar.item.GeoMultiBlockItem;
import net.dumbcode.projectnublar.item.api.MultiBlockItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(GeoMultiBlockItem.class)
public abstract class GeoMultiBlockItemMixin extends MultiBlockItem implements GeoItem {
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public GeoMultiBlockItemMixin(Block block, Properties properties, int rows, int columns, int depth) {
        super(block, properties, rows, columns, depth);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private SimpleGeoItemRenderer<?> renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new SimpleGeoItemRenderer<>();

                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }


}
