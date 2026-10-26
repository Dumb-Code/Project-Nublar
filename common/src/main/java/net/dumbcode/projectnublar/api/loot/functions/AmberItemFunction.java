package net.dumbcode.projectnublar.api.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.util.NublarMath;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function ({@code projectnublar:amber}) turning a mined amber block into an amber item with
 * a random DNA percentage; silk touch keeps the block drop untouched.
 */
public class AmberItemFunction extends LootItemConditionalFunction {

    /** The function has no fields of its own, only the shared {@code conditions} list. */
    public static final MapCodec<AmberItemFunction> MAP_CODEC =
            RecordCodecBuilder.mapCodec(i -> commonFields(i).apply(i, AmberItemFunction::new));

    /** Exponent skewing the random DNA roll toward higher percentages. */
    private static final double DNA_ROLL_EXPONENT = 0.8d;
    private static final int DNA_ROLL_DECIMALS = 2;

    public AmberItemFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    public static Builder<?> amberItem() {
        return simpleBuilder(AmberItemFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        AmberBlock block = (AmberBlock) ((BlockItem) itemStack.getItem()).getBlock();
        Identifier dino = block.getEntityType();
        ItemInstance toolStack = lootContext.getOptionalParameter(LootContextParams.TOOL);
        if (toolStack != null) {
            Holder<Enchantment> silkTouch = lootContext.getLevel().registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
            boolean hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(silkTouch, toolStack) > 0;
            if (!hasSilkTouch) {
                itemStack = new ItemStack(ItemInit.AMBER_ITEM.get());
                DNAData dnaData = new DNAData();
                dnaData.setEntityType(BuiltInRegistries.ENTITY_TYPE.getValue(dino));
                dnaData.setDnaPercentage(NublarMath.round(
                        Math.pow(lootContext.getRandom().nextDouble(), DNA_ROLL_EXPONENT),
                        DNA_ROLL_DECIMALS));
                dnaData.saveTo(itemStack);
            }
        }
        return itemStack;
    }

    @Override
    public MapCodec<AmberItemFunction> codec() {
        return MAP_CODEC;
    }
}
