package net.dumbcode.projectnublar.client.renderer;

import net.dumbcode.projectnublar.client.model.SimpleGeoItemModel;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SimpleGeoItemRenderer<T extends Item & GeoItem> extends GeoItemRenderer<T> {
    public SimpleGeoItemRenderer() {
        super(new SimpleGeoItemModel<>());
    }
}
