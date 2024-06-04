package net.dumbcode.projectnublar.api;

import net.minecraft.world.entity.EntityType;

public class EntityColorStore {




    public static Color register(EntityType<?> entityType, int color) {
        return new Color(entityType, "", color);
    }
    public static Color register(EntityType<?> entityType, String variant, int color) {
        return new Color(entityType, variant, color);
    }


    public record Color(EntityType<?> entityType, String variant, int... color) {
    }
}
