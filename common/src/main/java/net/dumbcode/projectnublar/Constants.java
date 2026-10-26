package net.dumbcode.projectnublar;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Mod-wide constants and {@link Identifier} helpers. */
public class Constants {

    public static final String MODID = "projectnublar";
    public static final String MOD_NAME = "Project Nublar";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final int BORDER_COLOR = 0xFF577694;

    /** An identifier in this mod's namespace. */
    public static Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    /** An identifier parsed from {@code path}; without a namespace it is in {@code minecraft}. */
    public static Identifier mcLoc(String path) {
        return Identifier.parse(path);
    }
}
