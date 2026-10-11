package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

/**
 * Spacing between herd members: the single switch for exceptions ({@link #applies}), the distances
 * ({@link #personalSpace}, {@link #hardMinimum}, {@link #herdRadius}), the pure tests behind the masks and
 * the guard ({@link #wouldCrowd}, {@link #stepCrowds}), and the step directions out of a stack
 * ({@link #away}, used to unstack) and out of the leader's line ({@link #sideStep}).
 */
public final class HerdSpacing {

    /** Gap between bodies, in blocks, that herd members keep. */
    static final double GAP = 1.0D;

    /** Gap kept to a resting body instead of {@link #GAP} (lead decision 4: resting members may be closer; tune). */
    static final double RESTING_GAP = 0.5D;

    /**
     * Gap kept instead of {@link #GAP} where either body stands in tight ground ({@link #tightGround}): the
     * hard minimum is then the body width plus this, 1.7 for Gallimimus (lead decision, playtest 2 fix P8b).
     * With the personal space (2.2) a standing member could not be passed in any corridor narrower than
     * 3.95 blocks (against a wall) to 6.15 (in the middle); at 1.7 those are 2.95 and 4.65. The same value as
     * {@link #RESTING_GAP} today, tuned on its own.
     */
    static final double TIGHT_GAP = 0.5D;

    /**
     * Hysteresis of the tight-ground measure: once tight, a body stays tight until its side room is this much
     * past {@link #tightThreshold} (one side-room step, so one more probe), so a member walking along a
     * corridor's uneven wall does not flip its minimum every refresh.
     */
    static final double TIGHT_HYSTERESIS = BodyFit.SIDE_ROOM_STEP;

    private HerdSpacing() {
    }

    /**
     * Whether spacing is on for this animal. Off for animals without a group and while fighting.
     * Add later exceptions here (mating, playing, ...), so every part of the spacing obeys them.
     */
    public static boolean applies(Dinosaur dinosaur) {
        return GroupManager.groupOf(dinosaur) != null && !BrainUtils.hasMemory(dinosaur, MemoryModuleType.ATTACK_TARGET);
    }

    /** Centre-to-centre distance two bodies keep: half of each width plus the gap. */
    static double personalSpace(double width, double otherWidth) {
        return (width + otherWidth) / 2D + GAP;
    }

    /**
     * Centre-to-centre distance no member enters by its own movement (lead decision 4): the personal space
     * while both move or stand in the open, a smaller gap when either body rests ({@link #RESTING_GAP}) or
     * stands in tight ground ({@link #TIGHT_GAP}, playtest 2 fix P8b); both: the smaller of the two. The one
     * rule behind the masks, the guard, the unstack, the side-step clearance and the passage test
     * ({@code HerdBodies.minimum}, {@code FollowFormation.roomBeside}).
     */
    static double hardMinimum(double width, double otherWidth, boolean eitherResting, boolean eitherTight) {
        double gap = GAP;
        if (eitherResting) {
            gap = Math.min(gap, RESTING_GAP);
        }
        if (eitherTight) {
            gap = Math.min(gap, TIGHT_GAP);
        }
        return (width + otherWidth) / 2D + gap;
    }

    /** {@link #hardMinimum} for two animals, with the tight ground each one's move control measured. Public for GameTests. */
    public static double hardMinimumOf(Dinosaur a, Dinosaur b) {
        return hardMinimum(a.getBbWidth(), b.getBbWidth(), a.isResting() || b.isResting(), tightGroundOf(a) || tightGroundOf(b));
    }

    /** Whether the animal's move control last measured tight ground under it ({@link HerdMoveControl#tightGround}); open without one. */
    static boolean tightGroundOf(Dinosaur dinosaur) {
        return dinosaur.getMoveControl() instanceof HerdMoveControl control && control.tightGround();
    }

    /**
     * Side room below which a member's ground is tight: the open hard minimum plus the clearance box width
     * (3.45 for Gallimimus: 2.2 + 1.25). A passer needs the open minimum of room beside a standing body on
     * one side; a box width on top of it is the slack that makes the place open ground, where the personal
     * space is kept. Side room comes in half-block steps ({@code BodyFit.sideRoom}), so 3.5 is the least
     * that counts as open for Gallimimus.
     */
    static double tightThreshold(double width, double boxWidth) {
        return personalSpace(width, width) + boxWidth;
    }

    /** The side room a body needs to count as open: {@code threshold}, plus {@link #TIGHT_HYSTERESIS} while it is tight. */
    static double tightLimit(double threshold, boolean wasTight) {
        return wasTight ? threshold + TIGHT_HYSTERESIS : threshold;
    }

    /**
     * Whether the ground at a body is tight (lead decision, playtest 2 fix P8b): neither side across its
     * heading has {@link #tightLimit} ({@code threshold}, one step more while it was tight) of room for its
     * own clearance box.
     */
    static boolean tightGround(float roomOneSide, float roomOtherSide, double threshold, boolean wasTight) {
        return Math.max(roomOneSide, roomOtherSide) < tightLimit(threshold, wasTight);
    }

    /**
     * Whether a side step should cross the walker's line to the other side (the side with room, playtest 2
     * fix P8b). The member stands {@code lateral} beside the line on its own side: staying ends at most
     * {@code lateral + ownRoom} from the line, crossing at most {@code otherRoom - lateral}. It crosses only
     * when its own side falls short of the {@code needed} room (how far it must get to be clear) and crossing
     * ends clearer of the line than staying; so a member next to a wall with a little room beyond the line
     * stays, instead of walking back toward the walker (fix round 1).
     */
    static boolean crossesLine(float ownRoom, float otherRoom, double lateral, double needed) {
        return ownRoom < needed && otherRoom - lateral > ownRoom + lateral;
    }

    /**
     * How far a side step on the member's own side aims (fix P12c, cliff cause 6): the {@code needed} distance,
     * but no farther than the {@code ownRoom} measured there ({@code BodyFit.sideRoom}: it ends at a wall or at
     * a rim), plus {@code arrived} because steering stops that short of its target. Before P12c the target lay
     * past the room, and steering pressed the body into the rim until the step timed out.
     */
    static double ownSideReach(double needed, float ownRoom, double arrived) {
        return Math.min(needed, ownRoom) + arrived;
    }

    /**
     * Whether a walker held by a standing member asks it to make way ({@code HerdMoveControl.makeWay}, fix
     * round 1): never the herd leader (spec "Who yields: leader first": followers go round a standing leader
     * or wait); never a walking blocker (it is making way already); a blocker that is not trying to move
     * (arrived, resting, eating, stopped by blocks: {@link HerdMoveControl#wantsToMove} false) whatever the
     * ranks; between two members that both want to move, only the one that outranks the other. A blocker
     * that does not want to move is never itself held, so it never asks back: no ping-pong.
     */
    static boolean asksToMakeWay(boolean blockerIsLeader, boolean blockerWalking, boolean blockerWantsToMove, boolean walkerOutranksBlocker) {
        return !blockerIsLeader && !blockerWalking && (walkerOutranksBlocker || !blockerWantsToMove);
    }

    /** The herd area radius for a member of this herd. Public for GameTests. */
    public static double herdRadius(DinoGroup group, Dinosaur member) {
        return group.radius(personalSpace(member.getBbWidth(), member.getBbWidth()), member.getBbWidth());
    }

    /**
     * Would a body at (x, z) moving (vx, vz) per tick come inside {@code minimum} of a body at
     * (otherX, otherZ) moving (otherVx, otherVz), within {@code ticks}? Uses the closest approach of the
     * straight-line motions. A move that never brings the bodies closer than they are now does not count,
     * so a member that is already too close can always step away.
     */
    static boolean wouldCrowd(double x, double z, double vx, double vz, double otherX, double otherZ,
                              double otherVx, double otherVz, double minimum, int ticks) {
        double px = otherX - x;
        double pz = otherZ - z;
        double rvx = otherVx - vx;
        double rvz = otherVz - vz;
        double speedSqr = rvx * rvx + rvz * rvz;
        double t = speedSqr < 1.0E-12D ? 0D : Math.max(0D, Math.min(ticks, -(px * rvx + pz * rvz) / speedSqr));
        double cx = px + rvx * t;
        double cz = pz + rvz * t;
        double closestSqr = cx * cx + cz * cz;
        return closestSqr < minimum * minimum && closestSqr < px * px + pz * pz - 1.0E-9D;
    }

    /**
     * The guard's member test: does the point (aheadX, aheadZ) lie inside {@code minimum} of the other body
     * and closer to it than the walker is now?
     */
    static boolean stepCrowds(double x, double z, double aheadX, double aheadZ, double otherX, double otherZ, double minimum) {
        double aheadSqr = (otherX - aheadX) * (otherX - aheadX) + (otherZ - aheadZ) * (otherZ - aheadZ);
        double nowSqr = (otherX - x) * (otherX - x) + (otherZ - z) * (otherZ - z);
        return aheadSqr < minimum * minimum && aheadSqr < nowSqr;
    }

    /**
     * Can a walker pass a standing body where it stands? Only where the body's own clearance box can stand
     * {@code hardMinimum} beside it on at least one side ({@code BodyFit.sideRoom} on each side, in half-block
     * steps): a passer's centre must be that far from the standing body. A spotless member in a place with
     * less room does not stand there (playtest 2 fix P8a, traffic cause R2). The caller passes the tight
     * minimum: a place that is not open ground ({@link #tightGround}) is tight, and an open one is passable.
     */
    static boolean roomToPass(float roomOneSide, float roomOtherSide, double hardMinimum) {
        return Math.max(roomOneSide, roomOtherSide) >= hardMinimum;
    }

    /** One herd member near the walker: its offset from the walker (other minus self) and the distance to keep. */
    record Neighbour(double dx, double dz, double space) {
    }

    /**
     * Unit direction straight away from crowding neighbours, closer ones counting more. If they cancel
     * out (crowded evenly on two sides), a sideways step from the first one. If the first neighbour sits
     * on the same spot, a fixed step along +x. Empty list: {@link Vec3#ZERO}.
     */
    static Vec3 away(List<Neighbour> neighbours) {
        if (neighbours.isEmpty()) {
            return Vec3.ZERO;
        }
        double x = 0D;
        double z = 0D;
        for (Neighbour neighbour : neighbours) {
            double distance = Math.sqrt(neighbour.dx() * neighbour.dx() + neighbour.dz() * neighbour.dz());
            if (distance < 1.0E-6D || distance >= neighbour.space()) {
                continue;
            }
            double closeness = (neighbour.space() - distance) / neighbour.space();
            x -= neighbour.dx() / distance * closeness;
            z -= neighbour.dz() / distance * closeness;
        }
        double length = Math.sqrt(x * x + z * z);
        if (length > 1.0E-6D) {
            return new Vec3(x / length, 0D, z / length);
        }
        Neighbour first = neighbours.get(0);
        double firstLength = Math.sqrt(first.dx() * first.dx() + first.dz() * first.dz());
        if (firstLength < 1.0E-6D) {
            // Neighbour on the same spot: a fixed step along +x.
            return new Vec3(1D, 0D, 0D);
        }
        return new Vec3(-first.dz() / firstLength, 0D, first.dx() / firstLength);
    }

    /**
     * Spec "Leader blocked": the unit direction a member standing in the leader's way steps, perpendicular to
     * the leader's heading and on the member's own side of the leader's line, never along the heading. A
     * member exactly on the line steps toward {@code (-headingZ, headingX)}.
     *
     * @param memberDx member x minus leader x
     * @param memberDz member z minus leader z
     */
    static Vec3 sideStep(double headingX, double headingZ, double memberDx, double memberDz) {
        double length = Math.sqrt(headingX * headingX + headingZ * headingZ);
        if (length < 1.0E-9D) {
            return new Vec3(1D, 0D, 0D);
        }
        double sideX = -headingZ / length;
        double sideZ = headingX / length;
        return memberDx * sideX + memberDz * sideZ < 0D ? new Vec3(-sideX, 0D, -sideZ) : new Vec3(sideX, 0D, sideZ);
    }
}
