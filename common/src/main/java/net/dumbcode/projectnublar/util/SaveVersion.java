package net.dumbcode.projectnublar.util;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The root {@code version} key of this mod's saved data.
 *
 * <p>Every block entity and {@link net.dumbcode.projectnublar.entity.dinosaur.Dinosaur} writes the
 * key first in its save method, so that a later change to the saved layout can tell old data from
 * new data and migrate it. {@code 0} means "written before this port": data from 1.20.1 has no key,
 * and a missing key reads as {@code 0} everywhere, in block entities and in the item codecs alike.
 */
public final class SaveVersion {
    /** The key name at the root of the saved data. */
    public static final String KEY = "version";
    /** The version that this build writes. */
    public static final int CURRENT = 1;

    private SaveVersion() {
    }

    /** Writes {@link #CURRENT} under {@link #KEY}; call it first in a save method. */
    public static void write(ValueOutput out) {
        out.putInt(KEY, CURRENT);
    }

    /** Reads the saved version; a missing key gives {@code 0}, which means "written before this port". */
    public static int read(ValueInput in) {
        return in.getIntOr(KEY, 0);
    }
}
