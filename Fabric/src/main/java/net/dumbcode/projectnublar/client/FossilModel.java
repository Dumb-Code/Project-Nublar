package net.dumbcode.projectnublar.client;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class FossilModel implements UnbakedModel, BakedModel, FabricBakedModel,IStackSensitive {
    private ItemStack stack = ItemStack.EMPTY;
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return null;
    }

    @Override
    public ItemTransforms getTransforms() {
        return null;
    }

    @Override
    public ItemOverrides getOverrides() {
        return new StackSensitiveItemOverrides<>(this);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return List.of();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {

    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, ResourceLocation location) {
        return null;
    }

    @Override
    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }
}
