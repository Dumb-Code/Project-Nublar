package net.dumbcode.projectnublar.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class StackSensitiveItemOverrides<T extends IStackSensitive & BakedModel> extends ItemOverrides{
    private final T model;

    public StackSensitiveItemOverrides(T model) {
        this.model = model;
    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
        model.setStack(pStack);
        return model;
    }
}
