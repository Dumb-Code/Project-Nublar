package net.dumbcode.projectnublar.api.dinosaur;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Per-drive record of how much DNA has been synthesized per entity type, plus collected
 * tropical-fish colors. The tag-name strings are frozen save contracts.
 */
public class DiskStorage {
    private static final String SYNTHED_ENTITIES_KEY = "synthedEntities";
    private static final String TROPICAL_FISH_COLORS_KEY = "tropicalFishColors";

    private final Map<EntityType<?>, Double> synthedEntityMap = new HashMap<>();
    private final Set<DyeColor> tropicalFishColors = new HashSet<>();

    public void increaseSynthedEntity(EntityType<?> entityType, double amount) {
        synthedEntityMap.put(entityType, synthedEntityMap.getOrDefault(entityType, 0.0) + amount);
    }

    public void addTropicalFishColor(DyeColor color) {
        tropicalFishColors.add(color);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        CompoundTag synthedEntityTag = new CompoundTag();
        for (Map.Entry<EntityType<?>, Double> entry : synthedEntityMap.entrySet()) {
            synthedEntityTag.putDouble(
                    BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey()).toString(), entry.getValue());
        }
        tag.put(SYNTHED_ENTITIES_KEY, synthedEntityTag);
        List<Integer> colors = new ArrayList<>();
        for (DyeColor color : tropicalFishColors) {
            colors.add(color.getId());
        }
        tag.putIntArray(TROPICAL_FISH_COLORS_KEY, colors);
        return tag;
    }

    public void load(CompoundTag tag) {
        CompoundTag synthedEntityTag = tag.getCompound(SYNTHED_ENTITIES_KEY);
        for (String key : synthedEntityTag.getAllKeys()) {
            synthedEntityMap.put(
                    BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(key)),
                    synthedEntityTag.getDouble(key));
        }
        for (int color : tag.getIntArray(TROPICAL_FISH_COLORS_KEY)) {
            tropicalFishColors.add(DyeColor.byId(color));
        }
    }

    public static DiskStorage createFromTag(CompoundTag tag) {
        DiskStorage storage = new DiskStorage();
        storage.load(tag);
        return storage;
    }
}
