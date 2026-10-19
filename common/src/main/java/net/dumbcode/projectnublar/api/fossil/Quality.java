package net.dumbcode.projectnublar.api.fossil;

import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

/**
 * How well a fossil is preserved. It is the {@code quality} block state of a fossil ore and it
 * sets the DNA yield of the mined fossil.
 *
 * <p>The overlay art has three folders for the four qualities: Fragmented and Poor share the
 * {@code fragmented} art.
 */
public enum Quality implements StringRepresentable {
    FRAGMENTED("fragmented", 1, ChatFormatting.GRAY, "fragmented"),
    POOR("poor", 2, ChatFormatting.WHITE, "fragmented"),
    COMMON("common", 3, ChatFormatting.GREEN, "fossilized"),
    PRISTINE("pristine", 4, ChatFormatting.DARK_PURPLE, "fresh");

    private final String name;
    private final int value;
    private final ChatFormatting color;
    private final String artFolder;

    Quality(String name, int value, ChatFormatting color, String artFolder) {
        this.name = name;
        this.value = value;
        this.color = color;
        this.artFolder = artFolder;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public ChatFormatting getColor() {
        return color;
    }

    /** The overlay texture folder of this quality. */
    public String artFolder() {
        return artFolder;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** The quality with this name, or null (old saves can hold the removed {@code none}). */
    public static @Nullable Quality byName(String name) {
        for (Quality quality : values()) {
            if (quality.name.equals(name)) {
                return quality;
            }
        }
        return null;
    }
}
