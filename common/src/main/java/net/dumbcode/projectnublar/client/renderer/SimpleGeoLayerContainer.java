package net.dumbcode.projectnublar.client.renderer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.List;

public class SimpleGeoLayerContainer<T extends GeoAnimatable> {
    private final GeoRenderer<T> renderer;
    private final List<SimpleGeoLayerRenderer<T>> layers = new ObjectArrayList<>();
    private boolean compiledLayers = false;

    public SimpleGeoLayerContainer(GeoRenderer<T> renderer) {
        this.renderer = renderer;
    }
    public List<SimpleGeoLayerRenderer<T>> getRenderLayers() {
        if (!this.compiledLayers)
            fireCompileRenderLayersEvent();

        return this.layers;
    }

    public void addLayer(SimpleGeoLayerRenderer<T> layer) {
        this.layers.add(layer);
    }

    public void fireCompileRenderLayersEvent() {
        this.compiledLayers = true;

        this.renderer.fireCompileRenderLayersEvent();
    }
}
