package net.dumbcode.projectnublar.client.model;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;

import java.util.HashMap;
import java.util.Map;

public class SimpleGeoItemModel<T extends Item & GeoItem> extends GeoModel<T> {
    protected final Map<ResourceLocation, ResourceLocation> geoCache = new HashMap<>();
    protected final Map<ResourceLocation, ResourceLocation> textureCache = new HashMap<>();

    @Override
    public ResourceLocation getModelResource(T object) {
        return geoCache.computeIfAbsent(getRegistryName(object), k -> new ResourceLocation(k.getNamespace(), "geo/" + (object instanceof BlockItem ? "block/" : "item/") + k.getPath() + ".geo.json"));
    }

    @Override
    public ResourceLocation getTextureResource(T object) {
        return textureCache.computeIfAbsent(getRegistryName(object), k -> new ResourceLocation(k.getNamespace(), "textures/" + (object instanceof BlockItem ? "block/" : "item/") + k.getPath() + ".png"));
    }

    @Override
    public ResourceLocation getAnimationResource(T object) {
        return null;
    }

    private static ResourceLocation getRegistryName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }
}
