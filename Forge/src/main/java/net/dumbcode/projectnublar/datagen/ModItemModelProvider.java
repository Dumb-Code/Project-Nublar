package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.client.model.fossil.FossilModelLoader;
import net.dumbcode.projectnublar.client.model.testtube.TestTubeModelLoader;
import com.google.gson.JsonObject;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput generator, ExistingFileHelper existingFileHelper) {
        super(generator, Constants.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Stream.of()
        //         .map(Supplier::get)
        //         .forEach(this::simpleHandHeldModel);

        Stream.of(
                        ItemInit.IRON_FILTER,
                        ItemInit.IRON_COMPUTER_CHIP,
                        ItemInit.IRON_TANK_UPGRADE,
                        ItemInit.GOLD_FILTER,
                        ItemInit.GOLD_COMPUTER_CHIP,
                        ItemInit.GOLD_TANK_UPGRADE,
                        ItemInit.DIAMOND_FILTER,
                        ItemInit.DIAMOND_COMPUTER_CHIP,
                        ItemInit.DIAMOND_TANK_UPGRADE,
                        ItemInit.HARD_DRIVE,
                        ItemInit.SSD,
                ItemInit.SEQUENCER_COMPUTER,
                ItemInit.SEQUENCER_DOOR,
                ItemInit.SEQUENCER_SCREEN
                )
                .map(Supplier::get)
                .forEach(this::simpleGeneratedModel);
        simpleGeneratedModel(BlockInit.SEQUENCER.get().asItem());
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
                .texture("layer0", modLoc("block/fossil_overlay/common/spine"))
                .customLoader((builder, helper) -> new FossilLoaderBuilder(FossilModelLoader.GENERATOR, builder, helper));
        withExistingParent(getName(ItemInit.TEST_TUBE_ITEM.get()), "item/generated")
                .texture("layer0", modLoc("item/test_tube"))
                .customLoader((builder, helper) -> new FossilLoaderBuilder(TestTubeModelLoader.GENERATOR, builder, helper));

        simpleGeneratedSpecialTexture(ItemInit.AMBER_ITEM.get(), "block/fossil_overlay/amber/amber");
        // Stream.of()
        //         .map(Supplier::get)
        //         .forEach(this::simpleBlockItemModel);
        withExistingParent(getName(ItemInit.SYRINGE.get()), "item/generated")
                .texture("layer0", modLoc("item/syringe")).override()
                .predicate(new ResourceLocation("filled"), 0.5f)
                .model(withExistingParent(getName(ItemInit.SYRINGE.get()) + "_dna", "item/generated")
                        .texture("layer0", modLoc("item/syringe_blood")))
                .predicate(new ResourceLocation("filled"), 1.0f)
                .model(withExistingParent(getName(ItemInit.SYRINGE.get()) + "_embryo", "item/generated")
                        .texture("layer0", modLoc("item/syringe_embryo"))).end();
        ;
    }

    protected ItemModelBuilder simpleBlockItemModel(Block block) {
        String name = getName(block);
        return withExistingParent(name, modLoc("block/" + name));
    }

    protected ItemModelBuilder simpleGeneratedModel(Item item) {
        return simpleModel(item, mcLoc("item/generated"));
    }

    protected ItemModelBuilder simpleGeneratedSpecialTexture(Item item, String texture) {
        return singleTexture(getName(item), mcLoc("item/generated"), "layer0", modLoc(texture));
    }

    protected ItemModelBuilder simpleHandHeldModel(Item item) {
        return simpleModel(item, mcLoc("item/handheld"));
    }

    protected ItemModelBuilder simpleModel(Item item, ResourceLocation parent) {
        String name = getName(item);
        return singleTexture(name, parent, "layer0", modLoc("item/" + name));
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
