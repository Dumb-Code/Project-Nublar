package net.dumbcode.projectnublar.util;

/**
 * Switches for measuring, toggled in game and never saved. They live in common code so items can flip
 * them without loading client classes.
 */
public final class DebugFlags {

    /** When true, {@code DinosaurRenderer} skips the colour-layer passes, to measure what they cost. */
    public static volatile boolean skipDinoColourLayers = false;

    /**
     * When true, herd travel rows use the tight spacing everywhere (3.2 for Gallimimus) instead of the open
     * spacing in open ground (4.2), so the lead can compare the two (plan review decision 9). Server side.
     */
    public static volatile boolean fixedTravelSpacing = false;

    private DebugFlags() {
    }
}
