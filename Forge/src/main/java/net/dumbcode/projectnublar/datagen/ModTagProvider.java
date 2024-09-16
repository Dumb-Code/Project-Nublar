package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.init.TagInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ModTagProvider {

    public static class ItemTag extends TagsProvider<Item>{

        public ItemTag(PackOutput p_256596_, CompletableFuture<HolderLookup.Provider> p_256513_, @Nullable ExistingFileHelper existingFileHelper) {
            super(p_256596_, Registries.ITEM, p_256513_, Constants.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            populateTag(TagInit.SUGAR, Items.SUGAR);
            populateTag(TagInit.BONE_MATTER, Items.BONE_MEAL);
            tag(TagInit.PLANT_MATTER).addTag(ItemTags.LEAVES);
            populateTag(TagInit.PLANT_MATTER, ComposterBlock.COMPOSTABLES.keySet().stream().filter(item -> !(item instanceof LeavesBlock)).toArray(ItemLike[]::new));

        }

        public void populateTag(TagKey<Item> tag, ItemLike... items){
            for (ItemLike item : items) {
                tag(tag).add(ForgeRegistries.ITEMS.getResourceKey(item.asItem()).get());
            }
        }
    }

    public static class BlockTag extends TagsProvider<Block>{

        public BlockTag(PackOutput pGenerator, CompletableFuture<HolderLookup.Provider> p_256513_, @Nullable ExistingFileHelper existingFileHelper) {
            super(pGenerator, Registries.BLOCK, p_256513_, Constants.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            FossilCollection.COLLECTIONS.forEach((name, collection) -> {
                collection.amberBlocks().forEach((block, map)->{
                    tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ForgeRegistries.BLOCKS.getResourceKey(map.get()).get());
                });
                collection.fossilblocks().forEach((block, qualityMap) -> {
                    qualityMap.forEach((quality, fossilPieceRegistryObjectMap) -> {
                        fossilPieceRegistryObjectMap.forEach((fossilPiece, registryObject) -> {
                            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ForgeRegistries.BLOCKS.getResourceKey(registryObject.get()).get());
                        });
                    });
                });
            });
        }
        public  <T extends Block>void populateTag(TagKey<Block> tag, Supplier<?>... items){
            for (Supplier<?> item : items) {
                tag(tag).add(ForgeRegistries.BLOCKS.getResourceKey((Block)item.get()).get());
            }
        }
    }
    public static class EntityTypeTag extends TagsProvider<EntityType<?>>{

        public EntityTypeTag(PackOutput pGenerator, CompletableFuture<HolderLookup.Provider> p_256513_, @Nullable ExistingFileHelper existingFileHelper) {
            super(pGenerator, Registries.ENTITY_TYPE, p_256513_, Constants.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            populateTag(TagInit.EMBRYO_ENTITY, EntityType.PARROT,EntityType.CHICKEN);

        }
        public  <T extends EntityType>void populateTag(TagKey<EntityType<?>> tag, Supplier<EntityType<?>>... items){
            for (Supplier<?> item : items) {
                tag(tag).add(ForgeRegistries.ENTITY_TYPES.getResourceKey((EntityType<?>)item.get()).get());
            }
        }
        public  <T extends EntityType>void populateTag(TagKey<EntityType<?>> tag, EntityType<?>... items){
            for (EntityType<?> item : items) {
                tag(tag).add(ForgeRegistries.ENTITY_TYPES.getResourceKey(item).get());
            }
        }
    }
}
