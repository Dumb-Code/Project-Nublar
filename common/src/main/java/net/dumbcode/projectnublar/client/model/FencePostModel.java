package net.dumbcode.projectnublar.client.model;

import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FencePostModel extends GeoModel<BlockEntityElectricFencePole> {
    private static final Map<Identifier, Identifier> modelCache = new HashMap<>();
    private static final Map<Identifier, Identifier> textureCache = new HashMap<>();
    private static final Map<Identifier, Identifier> animationCache = new HashMap<>();
    @Override
    public Identifier getModelResource(BlockEntityElectricFencePole animatable) {
        return modelCache.computeIfAbsent(BuiltInRegistries.BLOCK.getKey(animatable.getBlockState().getBlock()), k -> Identifier.fromNamespaceAndPath(k.getNamespace(),"geo/block/" + k.getPath() + ".geo.json"));
    }

    @Override
    public Identifier getTextureResource(BlockEntityElectricFencePole animatable) {
        return textureCache.computeIfAbsent(BuiltInRegistries.BLOCK.getKey(animatable.getBlockState().getBlock()), k -> Identifier.fromNamespaceAndPath(k.getNamespace(),"textures/block/" + k.getPath() + ".png"));
    }

    @Override
    public Identifier getAnimationResource(BlockEntityElectricFencePole animatable) {
        return null;
    }
}
