package net.dumbcode.projectnublar.api.loot.functions;

import net.dumbcode.projectnublar.api.*;


import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.init.DinosaurInit;
import net.dumbcode.projectnublar.init.EntityInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.init.LootFunctionInit;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;


public class FossilItemFunction extends LootItemConditionalFunction {

    public FossilItemFunction(LootItemCondition[] $$0) {
        super($$0);
    }

    public static LootItemConditionalFunction.Builder<?> fossilItem() {
        return simpleBuilder(FossilItemFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        FossilBlock block = (FossilBlock) ((BlockItem) itemStack.getItem()).getBlock();
        EntityType<?> entityType = EntityInit.TYRANNOSAURUS_REX_ENTITY.get();
        FossilPiece piece = block.getFossilPiece();
        Quality quality = block.getQuality();
        ItemStack toolStack = lootContext.getParamOrNull(LootContextParams.TOOL);
        if (toolStack != null) {
            int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, toolStack);
            boolean hasSilkTouch = EnchantmentHelper.hasSilkTouch(toolStack);
            if (!hasSilkTouch) {
                itemStack = new ItemStack(ItemInit.FOSSIL_ITEM.get());
                DNAData dnaData = new DNAData();
                dnaData.setEntityType(entityType);
                dnaData.setQuality(quality);
                dnaData.setFossilPiece(piece);
                itemStack.getOrCreateTag().put("DNAData", dnaData.saveToNBT(new CompoundTag()));
            } else {
               itemStack = new ItemStack(FossilCollection.COLLECTIONS.get(DinosaurInit.TYRANNOSAURUS_REX).fossilblocks().get(block.getBase().getBlock()).get(piece).get().defaultBlockState()
                       .setValue(FossilBlock.QUALITY_PROPERTY,quality).getBlock());
            }

        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootFunctionInit.FOSSIL_PART_FUNCTION.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<FossilItemFunction> {
        public Serializer() {
        }

        public void serialize(JsonObject $$0, FossilItemFunction $$1, JsonSerializationContext $$2) {
            super.serialize($$0, $$1, $$2);
        }

        public FossilItemFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new FossilItemFunction(conditions);
        }
    }
}


