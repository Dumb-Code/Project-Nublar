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
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function ({@code projectnublar:fossil_part}) turning a mined fossil block into either a
 * fossil item with DNA data (normal mining) or the matching fossil block (silk touch).
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
        ResourceLocation dino = block.getEntityType();
        FossilPiece piece = block.getFossilPiece();
        Quality quality = block.getQuality();
        ItemStack toolStack = lootContext.getParamOrNull(LootContextParams.TOOL);
        if (toolStack != null) {
            int fortuneLevel =
                    EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, toolStack);
            boolean hasSilkTouch = EnchantmentHelper.hasSilkTouch(toolStack);
            if (quality == Quality.NONE) {
                quality = rollQuality(lootContext, fortuneLevel);
            }
            if (!hasSilkTouch) {
                itemStack = createFossilItem(dino, piece, quality);
            } else {
                itemStack = new ItemStack(FossilCollection.COLLECTIONS
                        .get(dino.toString())
                        .fossilblocks()
                        .get(block.getBase())
                        .get(quality)
                        .get(piece)
                        .get());
            }
        }
        return itemStack;
    }

    /**
     * Rolls an upgraded quality once per fortune level (plus once at level 0).
     *
     * <p>TODO(BUG): the weighted list is built empty because its config source (the old
     * FossilsConfig weights) was commented out, so {@code getRandomValue(...)} yields an empty
     * Optional and {@code .get()} throws - fortune-based quality upgrades effectively never work.
     */
    private static Quality rollQuality(LootContext lootContext, int fortuneLevel) {
        Quality quality = Quality.FRAGMENTED;
        for (int roll = 0; roll <= fortuneLevel; ++roll) {
            SimpleWeightedRandomList.Builder<Quality> builder = new SimpleWeightedRandomList.Builder<>();
            SimpleWeightedRandomList<Quality> weightedrandomlist = builder.build();
            Quality newQuality = weightedrandomlist.getRandomValue(lootContext.getRandom()).get();
            if (newQuality.getValue() > quality.getValue()) {
                quality = newQuality;
            }
        }
        return quality;
    }

    private static ItemStack createFossilItem(
            ResourceLocation dino, FossilPiece piece, Quality quality) {
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
