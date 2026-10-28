package net.dumbcode.projectnublar.client;

import java.util.List;
import java.util.UUID;

/**
 * The horn set and horn colour one Triceratops shows. The model carries four horn sets on the same bases, and each
 * horn texture paints exactly one of them, so an animal shows one set by drawing only that set's texture. The other
 * three sets stay unpainted and draw nothing, as on 1.20.1.
 * <p>
 * Stand-in: the horn-set and horn-colour genes (F2.2) replace this pick, so that a player can choose the horns in the
 * Sequencer and a young takes them from a parent.
 *
 * @param set    the horn set as the art numbers it, 1 to 4: the texture {@code horn<set>} paints it. The art number
 *               is not the bone number in the model (Horn1 paints the bones of set 2); the texture picks the bones.
 * @param colour the horn colour, an index into {@link #COLOURS}
 */
record TriceratopsHorns(int set, int colour) {

    /** The number of horn sets in the model, and of horn textures in each colour. */
    static final int SETS = 4;
    /**
     * The art's horn colours, in the order of {@link #colour()}, as the texture name suffix after {@code horn<set>}:
     * Brown ({@code horn1.png}), Grey ({@code horn1_grey.png}) and Black ({@code horn1_black.png}).
     */
    static final List<String> COLOURS = List.of("", "_grey", "_black");

    /**
     * The horns of the animal with this UUID. The UUID is random at spawn, saved with the animal and known on the
     * client, so the pick is random per animal, stays the same after a save and reload, and needs no saved value.
     */
    static TriceratopsHorns of(UUID id) {
        int hash = id.hashCode();
        // The set takes the two low bits of the hash; the colour takes the bits above them.
        return new TriceratopsHorns(1 + Math.floorMod(hash, SETS), Math.floorMod(hash >>> 2, COLOURS.size()));
    }

    /** The name of the colour layer that draws these horns: its texture is {@code <gender>/<name>.png}. */
    String layerName() {
        return "horn" + set + COLOURS.get(colour);
    }
}
