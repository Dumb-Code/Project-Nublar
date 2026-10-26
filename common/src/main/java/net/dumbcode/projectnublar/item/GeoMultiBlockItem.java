package net.dumbcode.projectnublar.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import net.dumbcode.projectnublar.client.renderer.SimpleGeoItemRenderer;
import net.dumbcode.projectnublar.item.api.MultiBlockItem;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/**
 * Item of a multiblock machine that draws the machine's GeckoLib block model when its item definition uses the
 * {@code geckolib:geckolib} special model (today only the Processor; see {@code assets/projectnublar/items}).
 */
public class GeoMultiBlockItem extends MultiBlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GeoMultiBlockItem(Block block, Properties properties, int rows, int columns, int depth) {
        super(block, properties, rows, columns, depth);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private SimpleGeoItemRenderer<GeoMultiBlockItem> renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SimpleGeoItemRenderer<>();
                }
                return this.renderer;
            }
        });
    }
}
