package net.dumbcode.projectnublar.api.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.api.fossil.FossilPiece;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
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
 * Loot function ({@code projectnublar:fossil_part}) turning a mined fossil ore into a fossil item
 * with DNA data, or with Silk Touch into the ore item of the mined quality. The quality comes from
 * the block state.
 */
public class FossilItemFunction extends LootItemConditionalFunction {

    /** The function has no fields of its own, only the shared {@code conditions} list. */
    public static final MapCodec<FossilItemFunction> MAP_CODEC =
            RecordCodecBuilder.mapCodec(i -> commonFields(i).apply(i, FossilItemFunction::new));

    public FossilItemFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    public static LootItemConditionalFunction.Builder<?> fossilItem() {
        return simpleBuilder(FossilItemFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        FossilBlock block = (FossilBlock) ((BlockItem) itemStack.getItem()).getBlock();
        Quality quality = lootContext.getParameter(LootContextParams.BLOCK_STATE).getValue(FossilBlock.QUALITY);
        ItemInstance toolStack = lootContext.getOptionalParameter(LootContextParams.TOOL);
        if (toolStack != null) {
            Holder<Enchantment> silkTouch = lootContext.getLevel().registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
            if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, toolStack) > 0) {
                return new ItemStack(FossilCollection.COLLECTIONS.get(block.getEntityType())
                        .item(block.getBase(), block.getFossilPiece(), quality).get());
            }
        }
        return createFossilItem(block.getEntityType(), block.getFossilPiece(), quality);
    }

    private static ItemStack createFossilItem(Identifier dino, FossilPiece piece, Quality quality) {
        ItemStack itemStack = new ItemStack(ItemInit.FOSSIL_ITEM.get());
        DNAData dnaData = new DNAData();
        dnaData.setEntityType(BuiltInRegistries.ENTITY_TYPE.getValue(dino));
        dnaData.setQuality(quality);
        dnaData.setFossilPiece(piece);
        dnaData.saveTo(itemStack);
        return itemStack;
    }

    @Override
    public MapCodec<FossilItemFunction> codec() {
        return MAP_CODEC;
    }
}
