package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.util.NublarMath;
import net.dumbcode.projectnublar.registry.TagInit;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SyringeItem extends DNADataItem {
    public SyringeItem(Properties $$0) {
        super($$0);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.getItemInHand(hand).is(this) && !hasSampleData(player.getItemInHand(hand))) {
            if (target.isAlive()) {
                if(target.is(TagInit.EMBRYO_ENTITY) && target instanceof Animal mob && mob.isInLove()){
                    ItemStack embryoStack = player.getItemInHand(hand).copyWithCount(1);
                    embryoStack.set(DataComponentInit.EMBRYO.get(), true);
                    player.getInventory().add(embryoStack);
                } else {
                    DNAData dnaData = new DNAData();
                    dnaData.setEntityType(target.getType());
                    dnaData.setDnaPercentage(NublarMath.round(RandomSource.create().nextDouble(), 2));
                    if (target instanceof Parrot parrot) {
                        dnaData.setVariant(parrot.getVariant().getSerializedName());
                    }
                    if (target instanceof Cat cat) {
                        dnaData.setVariant(cat.getVariant().getRegisteredName());
                    }
                    dnaData.setEmbryo(false);
                    ItemStack dnaSyringe = new ItemStack(this);
                    dnaData.saveTo(dnaSyringe);
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
        if (pStack.has(DataComponentInit.EMBRYO.get())) {
            return Component.literal("Embryo Filled Syringe");
        }
        return super.getName(pStack);
    }
}
