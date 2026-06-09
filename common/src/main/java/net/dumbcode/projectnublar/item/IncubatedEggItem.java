package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.dinosaur.DinoBehaviourData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.data.BehaviourDataReloadListener;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class IncubatedEggItem extends DNADataItem {
    public IncubatedEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if (pContext.getLevel().isClientSide()) {
            return InteractionResult.sidedSuccess(true);
        }
        if(pContext.getHand() == InteractionHand.OFF_HAND){
            return InteractionResult.FAIL;
        }
        DinoData dinoData = DinoData.fromStack(pContext.getItemInHand());
        if (dinoData != null) {
            EntityType<?> entityType = dinoData.getBaseDino();
            DinoBehaviourData behaviourData = BehaviourDataReloadListener.getBehaviourInfoForDino(entityType);
            Dinosaur dinosaur = (Dinosaur) entityType.spawn((ServerLevel) pContext.getLevel(), pContext.getClickedPos().above(), MobSpawnType.EVENT);
            dinosaur.setDinoData(dinoData);
            dinosaur.setDinoBehaviour(behaviourData.toNBT(behaviourData));
            DinoNeedsUtils.setDinoBaseNeeds(dinosaur,behaviourData);
            DinoNeedsUtils.setCurrentHunger(dinosaur,100.0F);
            DinoNeedsUtils.setCurrentStamina(dinosaur,(float) behaviourData.maxStamina());

            pContext.getItemInHand().shrink(1);
            return InteractionResult.CONSUME;
        }
        return super.useOn(pContext);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable(this.getDescriptionId(), DinoData.fromStack(pStack).getFormattedType());
    }
}
