package net.dumbcode.projectnublar.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

/**
 * The species overlay of a test tube item, as the path of an entity id, for example
 * {@code tyrannosaurus_rex}. The id is the one of the stack's DNA data; without DNA data it is the
 * base dinosaur of the stack's dino data; without either the value is {@link #NONE}. Dino data
 * without a base dinosaur gives no value (null), so the definition's fallback applies.
 *
 * <p>Why a {@code select} property: there is one test tube item, and its look comes from the data
 * of each stack. On 26.2 an item definition picks a model by such a value with
 * {@code minecraft:select}. {@code items/test_tube.json} shows the empty tube for {@code none},
 * and the full tube with the species overlay ({@code item/dino_overlay/<species>}) for each species
 * that has an overlay texture. Any other species gets the fallback: the full tube without an
 * overlay. On 1.20.1 that tube showed the missing-texture sprite as its overlay; a missing texture
 * is a defect, not a look, so the port leaves the overlay out (ruling P111). This replaces the
 * 1.20.1 Forge model that made its quads for each stack.
 */
public record TestTubeOverlayProperty() implements SelectItemModelProperty<String> {

    /** The id that {@code items/test_tube.json} names in {@code property}. */
    public static final Identifier ID = Constants.modLoc("test_tube_overlay");
    /** The value of a tube with no sample: the definition shows the empty tube. */
    public static final String NONE = "none";
    /** The property type to register under {@link #ID}: no extra fields, string cases. */
    public static final SelectItemModelProperty.Type<TestTubeOverlayProperty, String> TYPE =
            SelectItemModelProperty.Type.create(MapCodec.unit(new TestTubeOverlayProperty()), Codec.STRING);

    @Override
    public @Nullable String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
                                int seed, ItemDisplayContext displayContext) {
        CompoundTag customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (customData.contains("DNAData")) {
            return DNAData.fromStack(stack).getPath();
        }
        if (customData.contains("DinoData")) {
            return DinoData.fromStack(stack).getPath();
        }
        return NONE;
    }

    @Override
    public Codec<String> valueCodec() {
        return Codec.STRING;
    }

    @Override
    public SelectItemModelProperty.Type<TestTubeOverlayProperty, String> type() {
        return TYPE;
    }
}
