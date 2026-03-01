package net.dumbcode.projectnublar.api.fossil;

import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.List;

public enum Quality implements StringRepresentable {
    FRAGMENTED("fragmented", 1, ChatFormatting.GRAY),
    POOR("poor", 2, ChatFormatting.WHITE),
    COMMON("common", 3, ChatFormatting.GREEN),
    PRISTINE("pristine", 4, ChatFormatting.DARK_PURPLE);

    private final String name;
    private final int value;
    private final ChatFormatting color;


    Quality(String name, int value, ChatFormatting color) {
        this.name = name;
        this.value = value;
        this.color = color;
    }

    public int getValue() {
        return value;
    }

    public static List<Quality> getQualityList() {
        return Arrays.asList(values());
    }

    public String getName() {
        return name;
    }

    public static Quality byName(String name) {
        for (Quality quality : values()) {
            if (quality.getName().equals(name)) {
                return quality;
            }
        }
        return null;
    }

    public ChatFormatting getColor() {
        return color;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
