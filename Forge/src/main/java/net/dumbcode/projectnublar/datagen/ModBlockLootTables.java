package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.api.loot.functions.AmberItemFunction;
import net.dumbcode.projectnublar.api.loot.functions.FossilItemFunction;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.registration.RegistryObject;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ModBlockLootTables extends BlockLootSubProvider {
    protected ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {

        this.getBlockStream().filter(this::shouldDropSelf).filter(b -> b instanceof FossilBlock).map(b -> (FossilBlock) b).forEach(this::fossilDrops);
        this.getBlockStream().filter(this::shouldDropSelf).filter(b -> b instanceof AmberBlock).map(b -> (AmberBlock) b).forEach(this::amberDrops);
        List.of(
                        BlockInit.PROCESSOR,
                        BlockInit.SEQUENCER
                )
                .stream()
                .map(RegistryObject::get)
                .forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return this.getBlockStream().filter(this::shouldGenerateLoot).toList();
    }

    protected void fossilDrops(FossilBlock block) {
        ItemLike pItem = block.asItem();
        this.add(block, LootTable.lootTable().withPool(
                this.applyExplosionCondition(pItem, LootPool.lootPool()
                        .apply(FossilItemFunction.fossilItem())
                        .setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pItem)))));
    }

    protected void amberDrops(AmberBlock block) {
        ItemLike pItem = block.asItem();
        this.add(block, LootTable.lootTable().withPool(
                this.applyExplosionCondition(pItem, LootPool.lootPool()
                        .apply(AmberItemFunction.amberItem())
                        .setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pItem)))));
    }

    protected Stream<Block> getBlockStream() {
        return BlockInit.BLOCKS.getEntries().stream().map(RegistryObject::get);
    }

    protected boolean shouldDropSelf(Block block) {
        return shouldGenerateLoot(block);
    }

    protected boolean shouldGenerateLoot(Block block) {
        return block.asItem() != Items.AIR;
    }

}
