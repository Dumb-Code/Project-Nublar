package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.registry.TagInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LeavesBlock;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/** The item, block and entity type tags of the mod. */
public class ModTagProvider {

    /** The item tags: sugar, bone matter, feeder meat and plant matter. */
    public static class ItemTag extends TagsProvider<Item>{

        public ItemTag(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, Registries.ITEM, lookupProvider, Constants.MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            populateTag(TagInit.SUGAR, Items.SUGAR);
            populateTag(TagInit.BONE_MATTER, Items.BONE_MEAL);
            populateTag(TagInit.FEEDER_MEAT, Items.PORKCHOP,Items.BEEF,Items.CHICKEN,Items.MUTTON,Items.COOKED_PORKCHOP,Items.COOKED_BEEF,Items.COOKED_MUTTON,Items.COOKED_CHICKEN);
            tag(TagInit.PLANT_MATTER).addTag(ItemTags.LEAVES);
            populateTag(TagInit.PLANT_MATTER, ComposterBlock.COMPOSTABLES.keySet().stream().filter(item -> !(item instanceof LeavesBlock)).toArray(ItemLike[]::new));

        }

        /** Adds the items to the tag. */
        public void populateTag(TagKey<Item> tag, ItemLike... items){
            for (ItemLike item : items) {
                tag(tag).add(BuiltInRegistries.ITEM.getResourceKey(item.asItem()).orElseThrow());
            }
        }
    }

    /** The block tags: sandstone ore replaceables, and every fossil block mineable with a pickaxe. */
    public static class BlockTag extends TagsProvider<Block>{

        public BlockTag(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, Registries.BLOCK, lookupProvider, Constants.MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            populateTag(TagInit.SANDSTONE_ORE_REPLACEABLES, () -> Blocks.SANDSTONE);
            populateTag(BlockTags.MINEABLE_WITH_PICKAXE, FossilCollection.COLLECTIONS.values().stream()
                    .flatMap(FossilCollection::blocks)
                    .toArray(Supplier<?>[]::new));
        }

        /** Adds the blocks the suppliers give to the tag. */
        public  <T extends Block>void populateTag(TagKey<Block> tag, Supplier<?>... items){
            for (Supplier<?> item : items) {
                tag(tag).add(BuiltInRegistries.BLOCK.getResourceKey((Block)item.get()).orElseThrow());
            }
        }
    }

    /** The entity type tags: the vanilla animals that give an embryo. */
    public static class EntityTypeTag extends TagsProvider<EntityType<?>>{

        public EntityTypeTag(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, Registries.ENTITY_TYPE, lookupProvider, Constants.MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            populateTag(TagInit.EMBRYO_ENTITY, EntityTypes.PARROT,EntityTypes.CHICKEN);

        }

        /** Adds the entity types the suppliers give to the tag. */
        public  <T extends EntityType>void populateTag(TagKey<EntityType<?>> tag, Supplier<EntityType<?>>... items){
            for (Supplier<?> item : items) {
                tag(tag).add(BuiltInRegistries.ENTITY_TYPE.getResourceKey((EntityType<?>)item.get()).orElseThrow());
            }
        }

        /** Adds the entity types to the tag. */
        public  <T extends EntityType>void populateTag(TagKey<EntityType<?>> tag, EntityType<?>... items){
            for (EntityType<?> item : items) {
                tag(tag).add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(item).orElseThrow());
            }
        }
    }
}
