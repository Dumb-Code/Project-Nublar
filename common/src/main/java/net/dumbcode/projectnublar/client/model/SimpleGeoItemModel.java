package net.dumbcode.projectnublar.client.model;

import com.geckolib.animatable.GeoItem;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Item model that draws a block item with its block's GeckoLib model and texture
 * ({@code geckolib/models/block/<id>.geo.json}, {@code textures/block/<id>.png}), and any other item with
 * {@code geckolib/models/item/<id>.geo.json} and {@code textures/item/<id>.png}. No animations.
 *
 * @param <T> the item type
 */
public class SimpleGeoItemModel<T extends Item & GeoItem> extends GeoModel<T> {
    protected final Map<Identifier, Identifier> geoCache = new HashMap<>();
    protected final Map<Identifier, Identifier> textureCache = new HashMap<>();

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        Item item = renderState.getGeckolibData(GeoItemRenderer.CURRENT_ITEM);
        return geoCache.computeIfAbsent(getRegistryName(item), k -> Identifier.fromNamespaceAndPath(k.getNamespace(), (item instanceof BlockItem ? "block/" : "item/") + k.getPath()));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        Item item = renderState.getGeckolibData(GeoItemRenderer.CURRENT_ITEM);
        return textureCache.computeIfAbsent(getRegistryName(item), k -> Identifier.fromNamespaceAndPath(k.getNamespace(),"textures/" + (item instanceof BlockItem ? "block/" : "item/") + k.getPath() + ".png"));
    }

    @Override
    public Identifier getAnimationResource(T object) {
        return null;
    }

    private static Identifier getRegistryName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }
}
