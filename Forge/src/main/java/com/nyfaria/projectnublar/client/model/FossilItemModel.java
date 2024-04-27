package com.nyfaria.projectnublar.client.model;

import com.mojang.math.Transformation;
import com.nyfaria.projectnublar.Constants;
import com.nyfaria.projectnublar.api.FossilPiece;
import com.nyfaria.projectnublar.api.FossilPieces;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
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

import static com.nyfaria.projectnublar.client.model.BakedModelHelper.quad;

import java.util.List;

public class FossilItemModel implements IDynamicBakedModel {
    private ItemStack stack;
    private BakedModel model;

    public FossilItemModel() {
        ModelResourceLocation key = new ModelResourceLocation(new ResourceLocation(Constants.MODID,"item/fossil.json"), "inventory");
        model = Minecraft.getInstance().getModelManager().getModel(key);
    }
    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {

        ModelState modelState = new SimpleModelState(new Transformation(new Vector3f(0f,0,0), new Quaternionf(1,1,1,0), new Vector3f(0.1F, 0.1F, 0.1F), new Quaternionf(1,1,1,0)));
        FossilPiece piece = FossilPieces.getPieceByName(stack.getOrCreateTag().getString("piece"));
        TextureAtlasSprite sprite = getTexture("block/fossil_overlay/" + piece.folder() + "/"+piece.name());
        List<BlockElement> unbaked = UnbakedGeometryHelper.createUnbakedItemElements(0,sprite.contents());
        unbaked.forEach(e ->
        {
            for(Direction d : Direction.values())
            {
                if(d != side)
                {
                    e.faces.remove(d);
                }
            }

            float z = (e.from.z() + e.to.z()) * 0.5F;
            e.from.z =(z + 0.5f * 0.1f);
            e.to.z = (z + 0.5f * 0.1F);
        });
        return UnbakedGeometryHelper.bakeElements(unbaked, m->sprite, modelState, new ResourceLocation(Constants.MODID, "item/fossil"));
    }
    private TextureAtlasSprite getTexture(String path) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(Constants.MODID, path));
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
        return new FossilItemOverrides(this);
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }
}
