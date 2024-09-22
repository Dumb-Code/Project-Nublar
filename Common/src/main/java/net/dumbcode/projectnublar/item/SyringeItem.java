package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.NublarMath;
import net.dumbcode.projectnublar.init.TagInit;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SyringeItem extends DNADataItem {
    public SyringeItem(Properties $$0) {
        super($$0);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.getItemInHand(hand).is(this) && !player.getItemInHand(hand).hasTag()) {
            if (target.isAlive()) {
                if(target.getType().is(TagInit.EMBRYO_ENTITY) && target instanceof Animal mob && mob.isInLove()){
                    ItemStack embryoStack = player.getItemInHand(hand).copyWithCount(1);
                    embryoStack.getOrCreateTag().putBoolean("Embryo", true);
                    player.getInventory().add(embryoStack);
                } else {
                    DNAData dnaData = new DNAData();
                    dnaData.setEntityType(target.getType());
                    dnaData.setDnaPercentage(NublarMath.round(RandomSource.create().nextDouble(), 2));
                    if (target instanceof VariantHolder<?>) {
                        if (target instanceof Parrot parrot) {
                            dnaData.setVariant(parrot.getVariant().getSerializedName());
                        }
                        if (target instanceof Cat cat) {
                            dnaData.setVariant(BuiltInRegistries.CAT_VARIANT.getKey(cat.getVariant()).toString());
                        }
                    }
                    dnaData.setEmbryo(false);
                    ItemStack dnaSyringe = new ItemStack(this);
                    dnaSyringe.getOrCreateTag().put("DNAData", dnaData.saveToNBT(new CompoundTag()));
                    player.getInventory().add(dnaSyringe);
                    player.getItemInHand(hand).shrink(1);
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getName(ItemStack pStack) {
        if(pStack.hasTag() && pStack.getTag().contains("Embryo")){
            return Component.literal("Embryo Filled Syringe");
        }
        return super.getName(pStack);
    }
}
