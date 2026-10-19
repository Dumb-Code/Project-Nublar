package net.dumbcode.projectnublar.api.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.api.fossil.FossilPiece;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.registry.LootFunctionInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function ({@code projectnublar:fossil_part}) turning a mined fossil ore into a fossil item
 * with DNA data, or with Silk Touch into the ore item of the mined quality. The quality comes from
 * the block state.
 */
public class FossilItemFunction extends LootItemConditionalFunction {

    public FossilItemFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static LootItemConditionalFunction.Builder<?> fossilItem() {
        return simpleBuilder(FossilItemFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        FossilBlock block = (FossilBlock) ((BlockItem) itemStack.getItem()).getBlock();
        Quality quality = lootContext.getParam(LootContextParams.BLOCK_STATE).getValue(FossilBlock.QUALITY);
        ItemStack toolStack = lootContext.getParamOrNull(LootContextParams.TOOL);
        if (toolStack != null && EnchantmentHelper.hasSilkTouch(toolStack)) {
            return new ItemStack(FossilCollection.COLLECTIONS.get(block.getEntityType())
                    .item(block.getBase(), block.getFossilPiece(), quality).get());
        }
        return createFossilItem(block.getEntityType(), block.getFossilPiece(), quality);
    }

    private static ItemStack createFossilItem(ResourceLocation dino, FossilPiece piece, Quality quality) {
        ItemStack itemStack = new ItemStack(ItemInit.FOSSIL_ITEM.get());
        DNAData dnaData = new DNAData();
        dnaData.setEntityType(BuiltInRegistries.ENTITY_TYPE.get(dino));
        dnaData.setQuality(quality);
        dnaData.setFossilPiece(piece);
        itemStack.getOrCreateTag().put("DNAData", dnaData.saveToNBT(new CompoundTag()));
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootFunctionInit.FOSSIL_PART_FUNCTION.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<FossilItemFunction> {
        public Serializer() {
        }

        @Override
        public void serialize(
                JsonObject json, FossilItemFunction function, JsonSerializationContext context) {
            super.serialize(json, function, context);
        }

        @Override
        public FossilItemFunction deserialize(
                JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new FossilItemFunction(conditions);
        }
    }
}
