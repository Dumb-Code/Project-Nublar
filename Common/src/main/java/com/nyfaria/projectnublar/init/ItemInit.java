package com.nyfaria.projectnublar.init;

import com.nyfaria.projectnublar.Constants;
import com.nyfaria.projectnublar.api.FossilCollection;
import com.nyfaria.projectnublar.api.FossilPieces;
import com.nyfaria.projectnublar.api.Quality;
import com.nyfaria.projectnublar.config.FossilsConfig;
import com.nyfaria.projectnublar.item.AmberItem;
import com.nyfaria.projectnublar.item.ComputerChipItem;
import com.nyfaria.projectnublar.item.FilterItem;
import com.nyfaria.projectnublar.item.FossilItem;
import com.nyfaria.projectnublar.item.TankItem;
import com.nyfaria.projectnublar.item.TestTubeItem;
import com.nyfaria.projectnublar.registration.RegistrationProvider;
import com.nyfaria.projectnublar.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ItemInit {
    public static final RegistrationProvider<Item> ITEMS = RegistrationProvider.get(Registries.ITEM, Constants.MODID);
    public static final RegistrationProvider<CreativeModeTab> CREATIVE_MODE_TABS = RegistrationProvider.get(Registries.CREATIVE_MODE_TAB, Constants.MODID);
    public static final RegistryObject<CreativeModeTab> FOSSIL_ITEMS_TAB = CREATIVE_MODE_TABS.register(Constants.MODID +"_fossil_items", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MODID + ".fossil_items"))
            .icon(() -> {
                ItemStack stack = new ItemStack(ItemInit.FOSSIL_ITEM.get());
                stack.getOrCreateTag().putString("piece", "rex_skull");
                stack.getTag().putString("quality", "pristine");
                stack.getTag().putString("dino", "projectnublar:tyrannosaurus_rex");
                return stack;
            })
            .displayItems(
                    (itemDisplayParameters, output) -> {
                        EntityInit.ENTITIES.getEntries().forEach((entityRegistryObject) -> {
                            FossilPieces.getPieces().forEach((fossilPiece) -> {
                                FossilsConfig.Fossil fossil = FossilsConfig.getFossils().get(entityRegistryObject.getId().toString());
                                if (FossilsConfig.getSet(fossil.getPieces().get()).pieces.get().contains(fossilPiece.name()) || fossil.getSpecial_pieces().get().contains(fossilPiece.name())) {
                                    for (Quality value : Quality.values()) {
                                        if (value == Quality.NONE) continue;
                                        ItemStack stack = new ItemStack(ItemInit.FOSSIL_ITEM.get());
                                        stack.getOrCreateTag().putString("piece", fossilPiece.name());
                                        stack.getTag().putString("quality", value.getName());
                                        stack.getTag().putString("dino", entityRegistryObject.getId().toString());
                                        output.accept(stack);
                                    }
                                }
                            });
                            ItemStack amberStack = new ItemStack(ItemInit.AMBER_ITEM.get());
                            amberStack.getOrCreateTag().putFloat("dna_percentage", 0.8f);
                            amberStack.getTag().putString("dino", entityRegistryObject.getId().toString());
                            output.accept(amberStack);
                        });
                    })
            .build());
    public static final RegistryObject<CreativeModeTab> FOSSIL_ORES_TAB = CREATIVE_MODE_TABS.register(Constants.MODID + "_fossil_ores", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MODID + ".fossil_ores"))
            .icon(() -> {
                Block block = FossilCollection.COLLECTIONS.get("projectnublar:tyrannosaurus_rex").fossilblocks().get(Blocks.STONE).get(Quality.PRISTINE).get(FossilPieces.getPieceByName("rex_skull")).get();
                ItemStack stack = new ItemStack(block);
                return stack;
            })
            .displayItems(
                    (itemDisplayParameters, output) -> {
                        FossilCollection.COLLECTIONS.forEach((entity, fossilCollection) -> {
                            fossilCollection.fossilblocks().forEach((block, qualityMap) -> {
                                qualityMap.forEach((quality, stoneMap) -> {
                                    stoneMap.forEach((piece, blockRegistryObject) -> {
                                        FossilsConfig.Fossil fossil = FossilsConfig.getFossils().get(entity);
                                        if (FossilsConfig.getSet(fossil.getPieces().get()).pieces.get().contains(piece.name()) || fossil.getSpecial_pieces().get().contains(piece.name())) {
                                            ItemStack stack = new ItemStack(blockRegistryObject.get());
                                            stack.getOrCreateTag().putString("quality", quality.getName());
                                            output.accept(stack);
                                        }
                                    });
                                });
                            });
                            fossilCollection.amberBlocks().forEach((block, blockRegistryObject) -> output.accept(blockRegistryObject.get()));
                        });
                    })
            .build());

    public static final RegistryObject<CreativeModeTab> MACHINES_TAB = CREATIVE_MODE_TABS.register(Constants.MODID + "_machines", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MODID + ".machines"))
            .icon(() -> new ItemStack(BlockInit.PROCESSOR.get()))
            .displayItems(
                    (itemDisplayParameters, output) -> {
                        output.accept(BlockInit.PROCESSOR.get());
                    })
            .build());
    public static final RegistryObject<CreativeModeTab> MISC_TAB = CREATIVE_MODE_TABS.register(Constants.MODID + "_misc", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MODID + ".misc"))
            .icon(() -> new ItemStack(ItemInit.IRON_FILTER.get()))
            .displayItems(
                    (itemDisplayParameters, output) -> {
                        output.accept(ItemInit.IRON_FILTER.get());
                        output.accept(ItemInit.GOLD_FILTER.get());
                        output.accept(ItemInit.DIAMOND_FILTER.get());
                        output.accept(ItemInit.IRON_TANK_UPGRADE.get());
                        output.accept(ItemInit.GOLD_TANK_UPGRADE.get());
                        output.accept(ItemInit.DIAMOND_TANK_UPGRADE.get());
                        output.accept(ItemInit.IRON_COMPUTER_CHIP.get());
                        output.accept(ItemInit.GOLD_COMPUTER_CHIP.get());
                        output.accept(ItemInit.DIAMOND_COMPUTER_CHIP.get());
                    })
            .build());

    public static final RegistryObject<CreativeModeTab> DNA_TAB = CREATIVE_MODE_TABS.register(Constants.MODID + "_dna", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MODID + ".dna"))
            .icon(() -> new ItemStack(ItemInit.TEST_TUBE_ITEM.get()))
            .displayItems(
                    (itemDisplayParameters, output) -> {
                        output.accept(ItemInit.TEST_TUBE_ITEM.get());
                        for (RegistryObject<EntityType<?>> entry : EntityInit.ENTITIES.getEntries()) {
                            ItemStack stack = new ItemStack(ItemInit.TEST_TUBE_ITEM.get());
                            stack.getOrCreateTag().putString("dino", entry.getId().toString());
                            stack.getOrCreateTag().putDouble("dna_percentage", 0.5);
                            output.accept(stack);
                        }
                    })
            .build());

    public static final RegistryObject<Item> FOSSIL_ITEM = ITEMS.register("fossil", () -> new FossilItem(getItemProperties()));
    public static final RegistryObject<Item> AMBER_ITEM = ITEMS.register("amber", () -> new AmberItem(getItemProperties()));

    public static final RegistryObject<Item> TEST_TUBE_ITEM = ITEMS.register("test_tube", () -> new TestTubeItem(getItemProperties()));

    public static final RegistryObject<Item> IRON_FILTER = ITEMS.register("iron_filter", () -> new FilterItem(getItemProperties().durability(100), 0.25));
    public static final RegistryObject<Item> GOLD_FILTER = ITEMS.register("gold_filter", () -> new FilterItem(getItemProperties().durability(100),0.5));
    public static final RegistryObject<Item> DIAMOND_FILTER = ITEMS.register("diamond_filter", () -> new FilterItem(getItemProperties().durability(100),1));

    public static final RegistryObject<Item> IRON_TANK_UPGRADE = ITEMS.register("iron_tank_upgrade", () -> new TankItem(getItemProperties(), 3000));
    public static final RegistryObject<Item> GOLD_TANK_UPGRADE = ITEMS.register("gold_tank_upgrade", () -> new TankItem(getItemProperties(), 4000));
    public static final RegistryObject<Item> DIAMOND_TANK_UPGRADE = ITEMS.register("diamond_tank_upgrade", () -> new TankItem(getItemProperties(),8000));

    public static final RegistryObject<Item> IRON_COMPUTER_CHIP = ITEMS.register("iron_computer_chip", () -> new ComputerChipItem(getItemProperties(), 3*20*60));
    public static final RegistryObject<Item> GOLD_COMPUTER_CHIP = ITEMS.register("gold_computer_chip", () -> new ComputerChipItem(getItemProperties(),2*20*60));
    public static final RegistryObject<Item> DIAMOND_COMPUTER_CHIP = ITEMS.register("diamond_computer_chip", () -> new ComputerChipItem(getItemProperties(),20*60));


    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    public static void loadClass() {
    }
}
