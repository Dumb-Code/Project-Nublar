package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import java.util.List;
import java.util.function.Supplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.recipe.UnincubatedEggRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Registers the mod's recipe serializers and lists the recipe types that the server sends to
 * clients. The id strings are frozen contracts.
 */
public class RecipeInit {
    public static DeferredRegister<RecipeSerializer<?>> RECIPE_TYPE = DeferredRegister.create(Constants.MODID, Registries.RECIPE_SERIALIZER);
    public static DeferredSupplier<RecipeSerializer<UnincubatedEggRecipe>> UNINCUBATED_EGG = RECIPE_TYPE.register("unincubated_egg",
            () -> new RecipeSerializer<>(UnincubatedEggRecipe.MAP_CODEC, UnincubatedEggRecipe.STREAM_CODEC));

    /**
     * The recipe types that the server sends to clients for {@code ClientRecipeStore}. NeoForge
     * sends every recipe of these types; Fabric sends only the recipes of the serializers that
     * {@code ProjectNublarFabric} registers for sync. Each machine package adds its recipe type
     * here.
     */
    public static final List<Supplier<RecipeType<?>>> SYNCED_TYPES = List.of(() -> RecipeType.CRAFTING);

    public static void loadClass() {
        RECIPE_TYPE.register();
    }
}
