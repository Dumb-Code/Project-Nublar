package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * The only record of who is in which group (plan B18). One instance per level, indexed by member UUID.
 * Nothing is stored on the entity, so there is no second copy to drift out of step.
 * <p>
 * A member leaves its group only when it dies, is discarded or changes dimension: {@code Dinosaur.remove}
 * calls {@link #leave}. An animal that unloads keeps its place (its graze spot and its place in the travel
 * order), and is in its group again when it loads.
 * Groups are not saved: after a restart every animal is groupless and the groups form again within
 * seconds. The leader is then the best of each group again, because {@link DinoGroup#add} promotes a
 * newcomer that outranks the leader and the rank order is total.
 * <p>
 * The instance methods work on UUIDs and a {@link DinoGroup.Roster}, so a unit test can drive them.
 * The static methods are the entity-facing entry points. Lookups never change state. Server thread only.
 */
public final class GroupManager {

    /**
     * Weak keys: a level that the server unloads is collected. A value must never reach its level
     * (no level field, no entity), or the key stays strongly reachable and the map leaks.
     */
    private static final Map<ServerLevel, GroupManager> BY_LEVEL = new WeakHashMap<>();

    private final Map<UUID, DinoGroup> groupByMember = new HashMap<>();

    GroupManager() {
    }

    /** The manager of a level. Fetch it once per scan, not once per animal. */
    static GroupManager of(ServerLevel level) {
        return BY_LEVEL.computeIfAbsent(level, key -> new GroupManager());
    }

    /** The animal's group, or {@code null}. Always {@code null} on the client. */
    public static @Nullable DinoGroup groupOf(Dinosaur dinosaur) {
        return dinosaur.level() instanceof ServerLevel level ? of(level).groupOf(dinosaur.getUUID()) : null;
    }

    /** {@code true} if the animal is the stored leader of its group. Always {@code false} on the client. */
    public static boolean isLeader(Dinosaur dinosaur) {
        DinoGroup group = groupOf(dinosaur);
        return group != null && group.isLeader(dinosaur.getUUID());
    }

    /** Takes the animal out of its group. Call it on death, discard, change of dimension and taming (B19). */
    public static void leave(Dinosaur dinosaur) {
        if (dinosaur.level() instanceof ServerLevel level) {
            of(level).remove(dinosaur.getUUID());
        }
    }

    /** The leader of {@code group} as an entity, or {@code null} if no member is present. */
    public static @Nullable Dinosaur leaderOf(DinoGroup group, ServerLevel level) {
        UUID leader = group.leader(roster(level));
        return leader == null ? null : resolve(level, leader);
    }

    /** The live roster of a level: present means alive and loaded. */
    static DinoGroup.Roster roster(ServerLevel level) {
        return new DinoGroup.Roster() {
            @Override
            public boolean isPresent(UUID member) {
                return resolve(level, member) != null;
            }

            @Override
            public float dominance(UUID member) {
                Dinosaur dinosaur = resolve(level, member);
                return dinosaur == null ? 0F : Dominance.of(dinosaur);
            }
        };
    }

    /**
     * The herd's member snapshot for this tick, filled by the first call of the tick. Velocities come from
     * the snapshot's own previous positions ({@link HerdBodies#add}), not from {@code x - xo}: the level
     * resets those right before each entity's tick, so the members that had not ticked yet (about half the
     * herd, the same half every tick) would all read 0 and count as standing.
     */
    static HerdBodies bodiesOf(DinoGroup group, ServerLevel level) {
        HerdBodies bodies = group.bodies();
        long now = level.getGameTime();
        if (bodies.tick() == now) {
            return bodies;
        }
        bodies.begin(now);
        for (UUID member : group.memberIds()) {
            Dinosaur dinosaur = resolve(level, member);
            if (dinosaur != null) {
                // Tight ground is the member's own move control's cached measure (HerdMoveControl.tightGround), not probed here.
                HerdMoveControl control = dinosaur.getMoveControl() instanceof HerdMoveControl herd ? herd : null;
                bodies.add(member, dinosaur.getX(), dinosaur.getZ(), dinosaur.getBbWidth(), dinosaur.isResting(),
                        control != null && control.tightGround(), control != null && control.wantsToMove(), group.rank(member));
            }
        }
        return bodies;
    }

    @Nullable DinoGroup groupOf(UUID member) {
        return this.groupByMember.get(member);
    }

    /** Starts a new group of two. The caller checks that both animals are groupless. */
    void create(UUID founder, UUID partner, int capacity, DinoGroup.Roster roster) {
        DinoGroup group = new DinoGroup(capacity);
        put(group, founder, roster);
        put(group, partner, roster);
    }

    /**
     * Adds a groupless animal if the group has room.
     *
     * @return {@code true} if the animal joined
     */
    boolean tryJoin(DinoGroup group, UUID member, DinoGroup.Roster roster) {
        if (group.size() + 1 > group.capacity()) {
            return false;
        }
        put(group, member, roster);
        return true;
    }

    /**
     * Moves every member of {@code from} into {@code into} if they all fit.
     *
     * @return {@code true} if the groups merged
     */
    boolean tryMerge(DinoGroup from, DinoGroup into, DinoGroup.Roster roster) {
        if (into.size() + from.size() > into.capacity()) {
            return false;
        }
        for (UUID member : List.copyOf(from.memberIds())) {
            from.remove(member);
            put(into, member, roster);
        }
        // Nobody settles the emptied group: its old leader's walk is reset with the merged herd's.
        into.takeDeposed(from);
        return true;
    }

    void remove(UUID member) {
        DinoGroup group = this.groupByMember.remove(member);
        if (group != null) {
            group.remove(member);
        }
    }

    private void put(DinoGroup group, UUID member, DinoGroup.Roster roster) {
        group.add(member, roster);
        this.groupByMember.put(member, group);
    }

    /** The living, loaded animal with this UUID, or {@code null}. */
    static @Nullable Dinosaur resolve(ServerLevel level, UUID id) {
        Entity entity = level.getEntity(id);
        return entity instanceof Dinosaur dinosaur && dinosaur.isAlive() ? dinosaur : null;
    }
}
