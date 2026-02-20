package net.dumbcode.projectnublar.client.renderer.layer;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public class DinoLayer {
    private final String layerName;
    private final int basicLayer;
    private final Function<Dinosaur, Boolean> renderRequirement;
    private @Nullable ResourceLocation textureLocation;

    public DinoLayer(String layerName, int basicLayer, Function<Dinosaur, Boolean> renderRequirement) {
        this.layerName = layerName;
        this.basicLayer = basicLayer;
        this.renderRequirement = renderRequirement;
    }

    public DinoLayer(String layerName, int basicLayer) {
        this(layerName,basicLayer,(b) -> true);
    }

    public int getBasicLayer() {
        return basicLayer;
    }

    public Optional<ResourceLocation> getTextureLocation(Dinosaur dino) {
        ResourceLocation dinoLoc = BuiltInRegistries.ENTITY_TYPE.getKey(dino.getType());
        textureLocation = new ResourceLocation(dinoLoc.getNamespace(), "textures/entity/" + dinoLoc.getPath() + "/" + dino.getStringDinoGender() + "/" + layerName + ".png");
        return Optional.of(textureLocation);
    }

    public String getLayerName() {
        return layerName;
    }
    public Function<Dinosaur, Boolean> getRenderRequirement() {return renderRequirement;}

}
