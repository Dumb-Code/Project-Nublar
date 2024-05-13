package net.dumbcode.projectnublar.api;

import net.minecraft.ChatFormatting;

public enum Quality {
    NONE("none", 0, ChatFormatting.GRAY),
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
}
