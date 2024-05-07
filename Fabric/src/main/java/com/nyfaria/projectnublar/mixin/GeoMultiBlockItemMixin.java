package com.nyfaria.projectnublar.mixin;

import com.nyfaria.projectnublar.client.renderer.SimpleGeoItemRenderer;
import com.nyfaria.projectnublar.item.GeoMultiBlockItem;
import com.nyfaria.projectnublar.item.api.MultiBlockItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import software.bernie.example.item.GeckoHabitatItem;
import software.bernie.geckolib.GeckoLib;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

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
