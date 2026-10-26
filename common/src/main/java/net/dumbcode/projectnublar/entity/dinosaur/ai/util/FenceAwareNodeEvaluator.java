package net.dumbcode.projectnublar.entity.dinosaur.ai.util;

import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.BodyFit;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.Nullable;

/**
 * The dinosaurs' node evaluator: vanilla walking paths with two changes.
 * <ul>
 *   <li>Electric fences and wire are {@link PathType#FENCE} (impassable) unless every need of the
 *       animal is at zero, when they are open and the animal walks up to break them. The exemption covers
 *       a node whose own footprint (the columns vanilla types, floor(hitbox width + 1) square) holds the
 *       fence; a species whose clearance box is wider than that footprint still gets the box test on the
 *       cells beside it.</li>
 *   <li>A node a herd member's clearance box does not fit at is {@link PathType#BLOCKED}
 *       ({@link BodyFit.PathProbe#fitsAt}). Vanilla sizes the body check in whole blocks (3 high for a 2.6
 *       hitbox) and takes a trapdoor or carpet as passable without a collision test, so it routes a
 *       Gallimimus through gaps 2.6-3.0 high that its 3.0 box forbids; the herd guard then stops the body
 *       there and the journey stalls (playtest 1 fix P6). The probe is made once per path computation
 *       ({@link #prepare}: group, box and cache lookups) and dropped in {@link #done}; the box test runs
 *       only on nodes vanilla accepted, reads blocks through the path's own region (no chunk loads), and
 *       vanilla caches the result per node for the path. The start node is used whatever its type
 *       (vanilla), so a body already in a tight spot can still path out to a node that fits.</li>
 *   <li>Cliff edges, for every dinosaur wider than one block (fix P12c, {@link BodyFit.NodeSupport}): a node
 *       whose centre has no ground under it (for a Gallimimus: ground under only 1 of its 4 columns) is
 *       {@link PathType#BLOCKED}; a rim node (ground under the centre, a drop of more than one step
 *       beside it) is {@link PathType#DAMAGING_IN_NEIGHBOR} (malus 8; {@code DANGER_OTHER} before the 26.1
 *       rename), so routes keep one block in from a rim where they can and do not cut corners along it.
 *       Planned drops of up to 3 blocks onto solid ground stay allowed (lead decision). Before P12c a 2-wide
 *       body's node counted as walkable with ground under any one column, so paths ran on rims and round pit
 *       corners, and turns and corner cutting dropped the body.</li>
 * </ul>
 * <p>
 * How vanilla 26.2 calls the two overrides ({@code WalkNodeEvaluator}): every node type comes from
 * {@code getCachedPathType}, which keeps {@link #getPathTypeOfMob} results per position for one path
 * computation and clears them in {@link #done}. {@code getPathTypeOfMob} types every block of the body
 * ({@code getPathTypeWithinMobBB}) through {@link #getPathType}, so a fence anywhere in the body reaches the
 * one-block check, and {@code fenceOpened} is set before the node's type is stored. {@link PathfindingContext}
 * caches only vanilla's block-state type ({@code getPathTypeFromState}, the server level's shared
 * {@code PathTypeCache}); {@link PathfindingContext#getBlockState} is not cached, so the fence test runs on
 * every call and one animal's open fence never enters a cache another animal reads.
 */
public class FenceAwareNodeEvaluator extends WalkNodeEvaluator {

    /** The herd member's box test for the path being computed; {@code null} when the guard does not run for it. */
    private @Nullable BodyFit.PathProbe probe;
    /** The dinosaur's rim rules for the path being computed; {@code null} for a mob that is not a dinosaur or is one column wide. */
    private @Nullable BodyFit.NodeSupport edges;
    /** Set while a node's blocks are typed when a fence in them was opened for a desperate animal. */
    private boolean fenceOpened;

    @Override
    public void prepare(PathNavigationRegion region, Mob mob) {
        super.prepare(region, mob);
        this.probe = mob instanceof Dinosaur dinosaur ? BodyFit.pathProbe(dinosaur) : null;
        this.edges = mob instanceof Dinosaur dinosaur ? BodyFit.nodeSupport(dinosaur) : null;
        this.fenceOpened = false;
    }

    @Override
    public void done() {
        this.probe = null;
        this.edges = null;
        super.done();
    }

    /**
     * The node check: vanilla's whole-body typing, then the cliff-edge rules (fix P12c), then the clearance
     * box. A node whose fence was opened keeps its vanilla type, so the walk up to the fence stays as it was;
     * a node vanilla refused is not read again.
     */
    @Override
    public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
        this.fenceOpened = false;
        PathType type = super.getPathTypeOfMob(context, x, y, z, mob);
        if (this.fenceOpened || mob.getPathfindingMalus(type) < 0F) {
            return type;
        }
        if (this.edges != null) {
            type = this.edges.typeAt(context.level(), x, y, z, type);
            if (mob.getPathfindingMalus(type) < 0F) {
                return type;
            }
        }
        if (this.probe != null && !this.probe.fitsAt(context.level(), x, y, z)) {
            return PathType.BLOCKED;
        }
        return type;
    }

    /**
     * The one-block check. Vanilla's mob-aware check calls it for every block the mob's body covers,
     * so a large dinosaur only plans steps its whole body fits through, and a fence anywhere in that
     * space blocks the step.
     */
    @Override
    public PathType getPathType(PathfindingContext context, int x, int y, int z) {
        if (isFenceOrWire(context.getBlockState(new BlockPos(x, y, z)))) {
            if (this.mob instanceof Dinosaur dinosaur && DinoNeedsUtils.allNeedsAtZero(dinosaur)) {
                this.fenceOpened = true;
                return PathType.OPEN;
            }
            return PathType.FENCE;
        }
        return super.getPathType(context, x, y, z);
    }

    private boolean isFenceOrWire(BlockState state){
        return state.is(BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get()) || state.is(BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get()) ||
                state.is(BlockInit.ELECTRIC_FENCE.get());
    }
}
