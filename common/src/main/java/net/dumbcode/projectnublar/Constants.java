package net.dumbcode.projectnublar;

import java.util.ServiceLoader;
import net.minecraft.resources.ResourceLocation;
import net.tslat.smartbrainlib.SBLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Mod-wide constants and {@link ResourceLocation} helpers. */
public class Constants {

    public static final String MODID = "projectnublar";
    public static final String MOD_NAME = "Project Nublar";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final SBLLoader PN_SBL_LOADER =
            ServiceLoader.load(SBLLoader.class).findFirst().get();
    public static final int BORDER_COLOR = 0xFF577694;

    public static ResourceLocation modLoc(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static ResourceLocation mcLoc(String path) {
        return new ResourceLocation(path);
    }
}
