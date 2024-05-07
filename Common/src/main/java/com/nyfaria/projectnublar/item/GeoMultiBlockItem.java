package com.nyfaria.projectnublar.item;

import com.nyfaria.projectnublar.client.renderer.SimpleGeoItemRenderer;
import com.nyfaria.projectnublar.item.api.MultiBlockItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import software.bernie.example.item.GeckoHabitatItem;
import software.bernie.geckolib.GeckoLib;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

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

    public void createRenderer(Consumer<Object> consumer) {

    }


    public Supplier<Object> getRenderProvider() {
        return null;
    }
}
