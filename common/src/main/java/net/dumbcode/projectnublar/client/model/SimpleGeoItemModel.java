package net.dumbcode.projectnublar.client.model;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;

import java.util.HashMap;
import java.util.Map;

public class SimpleGeoItemModel<T extends Item & GeoItem> extends GeoModel<T> {
    protected final Map<Identifier, Identifier> geoCache = new HashMap<>();
    protected final Map<Identifier, Identifier> textureCache = new HashMap<>();

    @Override
    public Identifier getModelResource(T object) {
        return geoCache.computeIfAbsent(getRegistryName(object), k -> Identifier.fromNamespaceAndPath(k.getNamespace(),"geo/" + (object instanceof BlockItem ? "block/" : "item/") + k.getPath() + ".geo.json"));
    }

    @Override
    public Identifier getTextureResource(T object) {
        return textureCache.computeIfAbsent(getRegistryName(object), k -> Identifier.fromNamespaceAndPath(k.getNamespace(),"textures/" + (object instanceof BlockItem ? "block/" : "item/") + k.getPath() + ".png"));
    }

    @Override
    public Identifier getAnimationResource(T object) {
        return null;
    }

    private static Identifier getRegistryName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }
}
