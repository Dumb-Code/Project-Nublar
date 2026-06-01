package net.dumbcode.projectnublar.client.renderer;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.DinosaurFeederBlock;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.client.model.CarnivoreFeederModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CarnivoreFeederRenderer extends GeoBlockRenderer<DinosaurFeederBlockEntity> {
    public CarnivoreFeederRenderer() {
        super(new CarnivoreFeederModel());
    }

    @Override
    public ResourceLocation getTextureLocation(DinosaurFeederBlockEntity animatable) {
        DinosaurFeederBlock block = (DinosaurFeederBlock) animatable.getBlockState().getBlock();
        String path = block.path;
        if(animatable.shouldDisplayFood && animatable.shouldDispenseFood) {
            return Constants.modLoc("textures/block/"+ path +"/full.png");
        }
        return Constants.modLoc("textures/block/"+ path +"/empty.png");
    }

}
