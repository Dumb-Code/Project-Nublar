package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class TagInit {
    public static TagKey<Item> BONE_MATTER = TagKey.create(BuiltInRegistries.ITEM.key(), Constants.modLoc("bone_matter"));
    public static TagKey<Item> FEEDER_MEAT = TagKey.create(BuiltInRegistries.ITEM.key(), Constants.modLoc("feeder_meat"));
    public static TagKey<Item> PLANT_MATTER = TagKey.create(BuiltInRegistries.ITEM.key(), Constants.modLoc("plant_matter"));
    public static TagKey<Item> SUGAR = TagKey.create(BuiltInRegistries.ITEM.key(), Constants.modLoc("sugar"));
    public static TagKey<EntityType<?>> EMBRYO_ENTITY = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Constants.modLoc("embryo_entity"));
    public static TagKey<EntityType<?>> LAND_FOOD_SOURCE = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Constants.modLoc("land_food_source"));
    public static TagKey<EntityType<?>> AQUATIC_FOOD_SOURCE = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Constants.modLoc("aquatic_food_source"));
}
