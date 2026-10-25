package net.dumbcode.projectnublar.api.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.util.NublarMath;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.registry.LootFunctionInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
 * Loot function ({@code projectnublar:amber}) turning a mined amber block into an amber item with
 * a random DNA percentage; silk touch keeps the block drop untouched.
 */
public class AmberItemFunction extends LootItemConditionalFunction {

    /** Exponent skewing the random DNA roll toward higher percentages. */
    private static final double DNA_ROLL_EXPONENT = 0.8d;
    private static final int DNA_ROLL_DECIMALS = 2;

    public AmberItemFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static Builder<?> amberItem() {
        return simpleBuilder(AmberItemFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        AmberBlock block = (AmberBlock) ((BlockItem) itemStack.getItem()).getBlock();
        ResourceLocation dino = block.getEntityType();
        ItemStack toolStack = lootContext.getParamOrNull(LootContextParams.TOOL);
        if (toolStack != null) {
            // here the fortune level is computed but unused
            int fortuneLevel =
                    EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, toolStack);
            boolean hasSilkTouch = EnchantmentHelper.hasSilkTouch(toolStack);
            if (!hasSilkTouch) {
                itemStack = new ItemStack(ItemInit.AMBER_ITEM.get());
                DNAData dnaData = new DNAData();
                dnaData.setEntityType(BuiltInRegistries.ENTITY_TYPE.get(dino));
                dnaData.setDnaPercentage(NublarMath.round(
                        Math.pow(lootContext.getRandom().nextDouble(), DNA_ROLL_EXPONENT),
                        DNA_ROLL_DECIMALS));
                itemStack.getOrCreateTag().put("DNAData", dnaData.saveToNBT(new CompoundTag()));
            }
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootFunctionInit.AMBER_FUNCTION.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<AmberItemFunction> {
        public Serializer() {
        }

        @Override
        public void serialize(
                JsonObject json, AmberItemFunction function, JsonSerializationContext context) {
            super.serialize(json, function, context);
        }

        @Override
        public AmberItemFunction deserialize(
                JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new AmberItemFunction(conditions);
        }
    }
}
