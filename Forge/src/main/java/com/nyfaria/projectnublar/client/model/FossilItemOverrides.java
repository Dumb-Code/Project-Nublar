package com.nyfaria.projectnublar.client.model;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FossilItemOverrides extends ItemOverrides {
    private final FossilItemModel model;

    FossilItemOverrides(FossilItemModel model) {
        this.model = model;
    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
        model.setStack(pStack);
        return model;
    }
}
