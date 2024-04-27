package com.nyfaria.projectnublar.api.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.nyfaria.projectnublar.api.FossilCollection;
import com.nyfaria.projectnublar.api.FossilPiece;
import com.nyfaria.projectnublar.api.FossilPieces;
import com.nyfaria.projectnublar.api.Quality;
import com.nyfaria.projectnublar.block.FossilBlock;
import com.nyfaria.projectnublar.config.FossilsConfig;
import com.nyfaria.projectnublar.init.ItemInit;
import com.nyfaria.projectnublar.init.LootFunctionInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
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
        return simpleBuilder((conditions) -> {
            return new FossilItemFunction(conditions);
        });
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        FossilBlock block = (FossilBlock) ((BlockItem) itemStack.getItem()).getBlock();
        ResourceLocation dino = block.getEntityType();
        FossilPiece piece = block.getFossilPiece();
        Quality quality = block.getQuality();
        ItemStack toolStack = lootContext.getParamOrNull(LootContextParams.TOOL);
        if (toolStack != null) {
            int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, toolStack);
            boolean hasSilkTouch = EnchantmentHelper.hasSilkTouch(toolStack);
            if (quality == Quality.NONE) {
                quality = Quality.FRAGMENTED;
                for (int j = 0; j <= i; ++j) {
                    SimpleWeightedRandomList.Builder<Quality> builder = new SimpleWeightedRandomList.Builder<>();
                    builder.add(Quality.FRAGMENTED, FossilsConfig.INSTANCE.fragmented.weight().get());
                    builder.add(Quality.POOR, FossilsConfig.INSTANCE.poor.weight().get());
                    builder.add(Quality.COMMON, FossilsConfig.INSTANCE.common.weight().get());
                    builder.add(Quality.PRISTINE, FossilsConfig.INSTANCE.pristine.weight().get());
                    SimpleWeightedRandomList<Quality> weightedrandomlist = builder.build();
                    Quality newQuality = weightedrandomlist.getRandomValue(lootContext.getRandom()).get();
                    if (newQuality.getValue() > quality.getValue()) {
                        quality = newQuality;
                    }
                }
            }
            if (!hasSilkTouch) {
                itemStack = new ItemStack(ItemInit.FOSSIL_ITEM.get());

                itemStack.getOrCreateTag().putString("quality", quality.getName());
                itemStack.getOrCreateTag().putString("dino", dino.toString());
                itemStack.getOrCreateTag().putString("piece", piece.name());
            } else {
               itemStack = new ItemStack(FossilCollection.COLLECTIONS.get(dino.toString()).fossilblocks().get(block.getBase()).get(quality).get(piece).get());
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
