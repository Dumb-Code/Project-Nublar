package net.dumbcode.projectnublar.api.fossil;

/**
 * One kind of fossil bone.
 *
 * @param name the id part of the fossil blocks and the saved piece name
 * @param folder the overlay texture folder below {@code block/fossil_overlay/}
 * @param displayName the readable name the lang entry is made from
 */
public record FossilPiece(String name, String folder, String displayName) {

    /** The overlay texture of this piece at the given quality. */
    public String texturePath(Quality quality) {
        return "block/fossil_overlay/" + folder + "/" + quality.artFolder() + "/" + name;
    }
}
