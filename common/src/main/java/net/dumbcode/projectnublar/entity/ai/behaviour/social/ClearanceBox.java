package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.Map;

/**
 * The box a herd member's movement checks are made with (spec "Fitting", lead decision 5): per species, from
 * the model size. Width = max(hitbox width, model cross width x growth scale) + {@link #SIDE_MARGIN} per side;
 * height = max(hitbox height, model top x growth scale) + {@link #TOP_MARGIN}, where the size gene never
 * raises the model top above what the hitbox needs ({@link #of}); both rounded up to {@link #QUANTUM}. The
 * model's length is not in the box: an axis-aligned box cannot follow the yaw. Gallimimus (hitbox 1.2 x 2.6,
 * model 0.69 x 2.53): 1.25 x 3.0.
 * <p>
 * {@link #MODEL_SIZES} comes from {@code tools/clearance/measure_models.py} (adult models, render scale
 * applied, size gene 0). {@link #growthScale} repeats the renderers' growth-stage scale (0.25, 0.5, 0.75, 1)
 * and size gene. Pure.
 */
final class ClearanceBox {

    /**
     * Margin beside the body: none (lead decision, playtest 2 fix P8a). The hitbox is the physical body and
     * vanilla pushes an overlapping body out; the old 0.25 made the Gallimimus box 1.75, which spans three
     * block columns at three grid positions in four and leaves 0.125 of slack either side in a 2-wide gap,
     * so neither steering nor vanilla path following could line the body up with such a gap. At 1.25 the
     * slack is 0.375 and the box spans two columns at three positions in four.
     */
    static final double SIDE_MARGIN = 0D;
    static final double TOP_MARGIN = 0.25D;
    static final double QUANTUM = 0.25D;
    /** Largest width the cache key holds (5 bits of quarters). */
    static final double MAX_WIDTH = 7.75D;
    /** Largest height the cache key holds (7 bits of quarters). */
    static final double MAX_HEIGHT = 31.75D;
    private static final double[] GROWTH_FACTORS = {0.25D, 0.5D, 0.75D, 1.0D};

    /** An adult model's size in blocks: extent across the body (x) and highest point above the feet. */
    record ModelSize(double width, double top) {
    }

    /** Per species (entity id path): adult model size. Regenerate with tools/clearance/measure_models.py. */
    static final Map<String, ModelSize> MODEL_SIZES = Map.ofEntries(
            Map.entry("brachiosaurus", new ModelSize(3.38D, 19.10D)),
            Map.entry("dilophosaurus", new ModelSize(0.49D, 1.73D)),
            Map.entry("gallimimus", new ModelSize(0.69D, 2.53D)),
            Map.entry("triceratops", new ModelSize(3.40D, 5.80D)),
            Map.entry("tyrannosaurus_rex", new ModelSize(1.38D, 3.42D)),
            Map.entry("velociraptor", new ModelSize(0.53D, 1.48D)));

    private final double width;
    private final double height;
    private final long sizeBits;

    ClearanceBox(double width, double height) {
        this.width = Math.min(MAX_WIDTH, roundUp(width));
        this.height = Math.min(MAX_HEIGHT, roundUp(height));
        this.sizeBits = (Math.round(this.width / QUANTUM) << 7) | Math.round(this.height / QUANTUM);
    }

    /**
     * The box of an animal of {@code species} (entity id path) with this hitbox, growth stage (1-4) and size
     * gene (%). The gene scales the rendered model but not the hitbox, so a positive gene never raises the
     * model top above the hitbox height or the gene-0 model top of the stage, whichever is taller (playtest 2
     * fix P8a: a Gallimimus with a gene of +8.7 % or more got a 3.25 box and refused every 3-high canopy its
     * 2.6 hitbox walks under). A negative gene still shrinks the model, down to the hitbox.
     */
    static ClearanceBox of(String species, double hitboxWidth, double hitboxHeight, int growthStage, double sizeGenePercent) {
        ModelSize model = MODEL_SIZES.get(species);
        double scale = growthScale(growthStage, sizeGenePercent);
        double bodyWidth = model == null ? hitboxWidth : Math.max(hitboxWidth, model.width() * scale);
        double bodyTop = hitboxHeight;
        if (model != null) {
            double topCap = Math.max(hitboxHeight, model.top() * growthScale(growthStage, 0D));
            bodyTop = Math.max(hitboxHeight, Math.min(model.top() * scale, topCap));
        }
        return new ClearanceBox(bodyWidth + 2D * SIDE_MARGIN, bodyTop + TOP_MARGIN);
    }

    /** The renderers' scale: 0.25, 0.5, 0.75 or 1 by growth stage (1 baby .. 4 adult), times 1 + size gene / 100. */
    static double growthScale(int growthStage, double sizeGenePercent) {
        int index = Math.max(1, Math.min(GROWTH_FACTORS.length, growthStage)) - 1;
        return GROWTH_FACTORS[index] * (1D + sizeGenePercent / 100D);
    }

    /** The nearest multiple of {@link #QUANTUM}: probe positions are rounded to it, so cached results match. */
    static double quantise(double value) {
        return Math.round(value / QUANTUM) * QUANTUM;
    }

    /**
     * Where a body with this hitbox width stands, on one axis, when vanilla walks it to a path node: the node
     * plus half its footprint in whole blocks ({@code Path.getEntityPosAtNode}: node + floor(width + 1) / 2,
     * the footprint {@code NodeEvaluator.prepare} sizes). A 2-column body (Gallimimus, 1.2 wide) stands a
     * whole block past the node, so its box lies inside the columns the path checked.
     */
    static double nodeCentre(int node, double hitboxWidth) {
        return node + Math.floor(hitboxWidth + 1.0D) * 0.5D;
    }

    /**
     * Where a body's feet rest in one footprint column at a path node, from the top of the collision shape
     * in the node cell ({@code cellTop}, 0 when it has none) and in the row below ({@code belowTop}, 0 when
     * none). Vanilla's node is the lowest cell the body occupies, not its floor: slabs and stairs are not
     * pathfindable, so the node sits above them and the feet rest half a block (or less) under the node
     * floor; a carpet or thin snow in the cell raises the feet, at most {@link BodyFit#STEP_HEIGHT} (the
     * body steps onto it). The row below never lifts the feet above the node floor. NaN when both are
     * empty: a hole under this column.
     */
    static double columnFeet(int nodeY, double cellTop, double belowTop) {
        if (cellTop > 0D) {
            return nodeY + Math.min(BodyFit.STEP_HEIGHT, cellTop);
        }
        if (belowTop > 0D) {
            return nodeY - 1 + Math.min(1D, belowTop);
        }
        return Double.NaN;
    }

    double width() {
        return this.width;
    }

    double height() {
        return this.height;
    }

    double halfWidth() {
        return this.width / 2D;
    }

    /**
     * Cache key of a probe with this box at (x, feetY, z), each rounded to {@link #QUANTUM}: 20 bits each of
     * x and z (wrapping every 262144 blocks, far beyond one herd's probes in a tick), 12 bits of feet height
     * and 12 bits of box size.
     */
    long probeKey(double x, double feetY, double z) {
        long qx = Math.round(x / QUANTUM) & 0xFFFFFL;
        long qz = Math.round(z / QUANTUM) & 0xFFFFFL;
        long qy = Math.round(feetY / QUANTUM) & 0xFFFL;
        return qx << 44 | qz << 24 | qy << 12 | this.sizeBits;
    }

    private static double roundUp(double value) {
        return Math.ceil(value / QUANTUM - 1.0E-9D) * QUANTUM;
    }
}
