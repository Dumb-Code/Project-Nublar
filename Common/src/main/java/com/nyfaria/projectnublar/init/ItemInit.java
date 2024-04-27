package com.nyfaria.projectnublar.init;

import com.nyfaria.projectnublar.Constants;
import com.nyfaria.projectnublar.api.FossilCollection;
import com.nyfaria.projectnublar.api.FossilPieces;
import com.nyfaria.projectnublar.api.Quality;
import com.nyfaria.projectnublar.config.FossilsConfig;
import com.nyfaria.projectnublar.item.AmberItem;
import com.nyfaria.projectnublar.item.FossilItem;
import com.nyfaria.projectnublar.registration.RegistrationProvider;
import com.nyfaria.projectnublar.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
    public static final RegistryObject<Item> FOSSIL_ITEM = ITEMS.register("fossil", () -> new FossilItem(getItemProperties()));
    public static final RegistryObject<Item> AMBER_ITEM = ITEMS.register("amber", () -> new AmberItem(getItemProperties()));
    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    public static void loadClass() {
    }
}
