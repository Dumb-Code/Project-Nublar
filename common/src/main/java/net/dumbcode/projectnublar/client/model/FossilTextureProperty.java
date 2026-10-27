package net.dumbcode.projectnublar.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.fossil.FossilPiece;
import net.dumbcode.projectnublar.api.fossil.FossilPieces;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The texture of a fossil item, as {@code <piece>_<quality>}: the piece name and the quality name
 * of the stack's DNA data, for example {@code tyrannosaurus_skull_common}. A fossil without a
 * piece or a quality (no DNA data, or a piece or quality that an old save holds and the mod no
 * longer has) gives {@code tyrannosaurus_skull_common}, as the 1.20.1 fossil model did.
 *
 * <p>Why a {@code select} property: there is one fossil item, and its texture comes from the data
 * of each stack. On 26.2 an item definition picks a model by such a value with
 * {@code minecraft:select}. {@code items/fossil.json} has one case per piece and quality, and each
 * case is the flat model {@code item/fossil/<piece>_<quality>} with the texture
 * {@link FossilPiece#texturePath}. This replaces the 1.20.1 Forge model that made its quads for
 * each stack.
 */
public record FossilTextureProperty() implements SelectItemModelProperty<String> {

    /** The id that {@code items/fossil.json} names in {@code property}. */
    public static final Identifier ID = Constants.modLoc("fossil_texture");
    /** The property type to register under {@link #ID}: no extra fields, string cases. */
    public static final SelectItemModelProperty.Type<FossilTextureProperty, String> TYPE =
            SelectItemModelProperty.Type.create(MapCodec.unit(new FossilTextureProperty()), Codec.STRING);

    @Override
    public String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed,
                      ItemDisplayContext displayContext) {
        // read only: component values are shared between stack copies
        DNAData data = stack.get(DataComponentInit.DNA_DATA.get());
        FossilPiece piece = data == null || data.getFossilPiece() == null ? FossilPieces.TYRANNOSAURUS_SKULL : data.getFossilPiece();
        Quality quality = data == null || data.getQuality() == null ? Quality.COMMON : data.getQuality();
        return piece.name() + "_" + quality.getName();
    }

    @Override
    public Codec<String> valueCodec() {
        return Codec.STRING;
    }

    @Override
    public SelectItemModelProperty.Type<FossilTextureProperty, String> type() {
        return TYPE;
    }
}
