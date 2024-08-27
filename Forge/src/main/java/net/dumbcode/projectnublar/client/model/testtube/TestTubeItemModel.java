package net.dumbcode.projectnublar.client.model.testtube;

import com.mojang.math.Transformation;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.client.model.api.IStackSensitive;
import net.dumbcode.projectnublar.client.model.api.StackSensitiveItemOverrides;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class TestTubeItemModel implements IDynamicBakedModel, IStackSensitive {
    private ItemStack stack;

    public TestTubeItemModel() {
    }
    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {

        ModelState modelState = new SimpleModelState(new Transformation(new Vector3f(0f,0,0), new Quaternionf(1,1,1,0), new Vector3f(0.1F, 0.1F, 0.1F), new Quaternionf(1,1,1,0)));
        DNAData data = stack.getOrCreateTag().contains("DNAData") ? DNAData.loadFromNBT(stack.getOrCreateTag().getCompound("DNAData")) : null;
        DinoData dinoData = stack.getOrCreateTag().contains("DinoData") ? DinoData.fromNBT(stack.getOrCreateTag().getCompound("DinoData")) : null;
        TextureAtlasSprite sprite = getTexture(data != null || dinoData != null ? "item/test_tube_full" : "item/test_tube");
        TextureAtlasSprite sprite2 = data != null ? getTexture(data.getNameSpace(), "item/dino_overlay/"+data.getPath()) : dinoData != null ? getTexture(dinoData.getNameSpace(), "item/dino_overlay/"+dinoData.getPath()) : null;
        List<BlockElement> unbaked = UnbakedGeometryHelper.createUnbakedItemElements(0,sprite.contents());
        if(sprite2 != null)
        {
            unbaked.addAll(UnbakedGeometryHelper.createUnbakedItemElements(1,sprite2.contents()));
        }
        return UnbakedGeometryHelper.bakeElements(unbaked, m->{
            if(!m.texture().getPath().equals("layer0")){
                return sprite2;
            }
            return sprite;
        }, modelState, Constants.modLoc( "item/fossil"));
    }
    private TextureAtlasSprite getTexture(String path) {
        return getTexture(Constants.MODID, path);
    }
    private TextureAtlasSprite getTexture(String namespace, String path) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(namespace, path));
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
    public ItemOverrides getOverrides() {
        return new StackSensitiveItemOverrides(this);
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return this.stack;
    }
}
