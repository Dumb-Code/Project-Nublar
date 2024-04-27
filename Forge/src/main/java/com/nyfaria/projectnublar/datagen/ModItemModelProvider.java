package com.nyfaria.projectnublar.datagen;

import com.google.gson.JsonObject;
import com.nyfaria.projectnublar.Constants;
import com.nyfaria.projectnublar.api.FossilCollection;
import com.nyfaria.projectnublar.client.model.FossilModelLoader;
import com.nyfaria.projectnublar.init.ItemInit;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput generator, ExistingFileHelper existingFileHelper) {
        super(generator, Constants.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Stream.of()
        //         .map(Supplier::get)
        //         .forEach(this::simpleHandHeldModel);

        // Stream.of()
        //         .map(Supplier::get)
        //         .forEach(this::simpleGeneratedModel);
        FossilCollection.COLLECTIONS.forEach((s, fossilCollection) -> {
            fossilCollection.fossilblocks().forEach((block, qualityMap) -> {
                qualityMap.forEach((quality, fossilPieceRegistryObjectMap) -> {
                    fossilPieceRegistryObjectMap.forEach((fossilPiece, blockRegistryObject) -> {
                        try {
                            simpleBlockItemModel(blockRegistryObject.get());
                        } catch (Exception ignored) {
                        }
                    });
                });
            });
            fossilCollection.amberBlocks().forEach((block, blockRegistryObject) -> {
                try {
                    simpleBlockItemModel(blockRegistryObject.get());
                } catch (Exception ignored) {
                }
            });
        });
        withExistingParent(getName(ItemInit.FOSSIL_ITEM.get()), "item/generated")
                .texture("layer0", "block/fossil_overlay/common/tail")
                .customLoader((builder, helper) -> new FossilLoaderBuilder(FossilModelLoader.GENERATOR, builder, helper));
        simpleGeneratedSpecialTexture(ItemInit.AMBER_ITEM.get(), "block/fossil_overlay/amber/amber");
        // Stream.of()
        //         .map(Supplier::get)
        //         .forEach(this::simpleBlockItemModel);
    }

    protected ItemModelBuilder simpleBlockItemModel(Block block) {
        String name = getName(block);
        return withExistingParent(name, modLoc("block/" + name));
    }

    protected ItemModelBuilder simpleGeneratedModel(Item item) {
        return simpleModel(item, mcLoc("item/generated"));
    }
    protected ItemModelBuilder simpleGeneratedSpecialTexture(Item item, String texture) {
        return singleTexture(getName(item), mcLoc("item/generated"),"layer0", modLoc(texture));
    }

    protected ItemModelBuilder simpleHandHeldModel(Item item) {
        return simpleModel(item, mcLoc("item/handheld"));
    }

    protected ItemModelBuilder simpleModel(Item item, ResourceLocation parent) {
        String name = getName(item);
        return singleTexture(name, parent, "layer0", modLoc("item/"+name));
    }

    protected String getName(Item item) {
        return ForgeRegistries.ITEMS.getKey(item).getPath();
    }

    protected String getName(Block item) {
        return ForgeRegistries.BLOCKS.getKey(item).getPath();
    }

    public static class FossilLoaderBuilder extends CustomLoaderBuilder<ItemModelBuilder> {

        public FossilLoaderBuilder(ResourceLocation loader, ItemModelBuilder parent, ExistingFileHelper existingFileHelper) {
            super(loader, parent, existingFileHelper);

        }

        @Override
        public JsonObject toJson(JsonObject json) {
            JsonObject obj = super.toJson(json);
            return obj;
        }
    }
}
