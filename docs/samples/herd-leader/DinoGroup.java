package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * One herd or pack: a capacity, the members and the leader.
 * <p>
 * {@link GroupManager} owns membership: only it adds or removes members, so the member set and its
 * index always agree. This class owns leadership. It works on UUIDs only and asks a {@link Roster}
 * which members are present and how dominant they are, so it holds no entity and no level, and a
 * unit test can drive it with a fake roster.
 * <p>
 * An unloaded member stays in the set, but it cannot lead: the leader is always a present member.
 */
public final class DinoGroup {

    /** What the group needs to know about its members. {@link GroupManager#roster} gives the live one. */
    interface Roster {

        /** {@code true} if the member is alive and loaded. */
        boolean isPresent(UUID member);

        /** The member's dominance. Only called for a present member. */
        float dominance(UUID member);
    }

    private final int capacity;
    private final Set<UUID> members = new HashSet<>();
    private @Nullable UUID leader;

    DinoGroup(int capacity) {
        this.capacity = capacity;
    }

    int capacity() {
        return this.capacity;
    }

    int size() {
        return this.members.size();
    }

    /** A read-only view of the member UUIDs. */
    Set<UUID> memberIds() {
        return Collections.unmodifiableSet(this.members);
    }

    /** {@code true} if {@code member} is the stored leader. Does not check that the leader is present. */
    boolean isLeader(UUID member) {
        return member.equals(this.leader);
    }

    /**
     * Adds a member. A newcomer that outranks the present leader leads at once, so the leader is always
     * the best of the group, whatever the order of joining. If there is no present leader, the best
     * present member is elected now, so a leader exists from the moment the group forms. The contest
     * margin applies only to changes through growth ({@link #challenge}).
     */
    void add(UUID member, Roster roster) {
        this.members.add(member);
        if (this.leader == null || !roster.isPresent(this.leader)) {
            electLeader(roster);
        } else if (roster.isPresent(member)
                && Dominance.outranks(roster.dominance(member), member, roster.dominance(this.leader), this.leader)) {
            this.leader = member;
        }
    }

    /** Removes the member. If it was the leader, the next {@link #leader} call elects a new one. */
    void remove(UUID member) {
        this.members.remove(member);
        if (member.equals(this.leader)) {
            this.leader = null;
        }
    }

    /**
     * The leader. If the stored leader is not present, the highest-ranked present member takes its place
     * first, with no contest.
     *
     * @return the leader, or {@code null} if no member is present
     */
    @Nullable UUID leader(Roster roster) {
        if (this.leader == null || !roster.isPresent(this.leader)) {
            electLeader(roster);
        }
        return this.leader;
    }

    /**
     * Gives the lead to {@code challenger} if its dominance is more than {@code margin} above the leader's.
     * The margin stops two near-equal animals from swapping the lead back and forth as they grow.
     *
     * @param margin at least 0; a negative margin would let the lead swap at every contest
     * @return {@code true} if the challenger is now the leader
     */
    boolean challenge(UUID challenger, float margin, Roster roster) {
        UUID current = leader(roster);
        if (current == null || current.equals(challenger) || !this.members.contains(challenger)
                || !roster.isPresent(challenger)
                || roster.dominance(challenger) <= roster.dominance(current) + margin) {
            return false;
        }
        this.leader = challenger;
        return true;
    }

    private void electLeader(Roster roster) {
        UUID best = null;
        float bestDominance = 0F;
        for (UUID member : this.members) {
            if (!roster.isPresent(member)) {
                continue;
            }
            float dominance = roster.dominance(member);
            if (best == null || Dominance.outranks(dominance, member, bestDominance, best)) {
                best = member;
                bestDominance = dominance;
            }
        }
        this.leader = best;
    }
}
