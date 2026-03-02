package net.dumbcode.projectnublar.api.fossil;

import net.dumbcode.projectnublar.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.List;

public enum Quality implements StringRepresentable {
    FRAGMENTED("fragmented", 0.0F, ChatFormatting.GRAY),
    POOR("poor", 1F, ChatFormatting.WHITE),
    COMMON("common", 2F, ChatFormatting.GREEN),
    PRISTINE("pristine", 3F, ChatFormatting.DARK_PURPLE);

    private final String name;
    private final float value;
    private final ChatFormatting color;


    Quality(String name, float value, ChatFormatting color) {
        this.name = name;
        this.value = value;
        this.color = color;
    }

    public float getValue() {
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
    public static Quality byValue(float value) {
        for (Quality quality : values()) {
            if (quality.getValue() == value) {
                return quality;
            }
        }
        Constants.LOG.error("Invalid quality value detected: " + value + ", [defaulting to common]");
        return Quality.COMMON;
    }

    public ChatFormatting getColor() {
        return color;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
