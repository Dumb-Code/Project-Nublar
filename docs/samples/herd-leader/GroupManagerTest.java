package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for group membership and leadership, driven by a fake roster instead of a level.
 * Needs JUnit 5 in common/build.gradle; the project has no tests yet.
 */
class GroupManagerTest {

    /** Present animals and their dominance. An animal missing from the map is unloaded or dead. */
    private final Map<UUID, Float> present = new HashMap<>();
    private final DinoGroup.Roster roster = new DinoGroup.Roster() {
        @Override
        public boolean isPresent(UUID member) {
            return present.containsKey(member);
        }

        @Override
        public float dominance(UUID member) {
            return present.get(member);
        }
    };
    private final GroupManager manager = new GroupManager();
    private final List<UUID> animals = new ArrayList<>();

    private UUID animal(long id, float dominance) {
        UUID uuid = new UUID(0L, id);
        this.present.put(uuid, dominance);
        this.animals.add(uuid);
        return uuid;
    }

    /** Every animal is in at most one member set, and the index points at exactly that group. */
    private void assertIndexMatchesGroups(DinoGroup... groups) {
        for (UUID animal : this.animals) {
            DinoGroup holder = null;
            for (DinoGroup group : groups) {
                if (group.memberIds().contains(animal)) {
                    assertNull(holder, "animal in two groups: " + animal);
                    holder = group;
                }
            }
            assertSame(holder, this.manager.groupOf(animal), "index disagrees for " + animal);
        }
    }

    @Test
    void createPutsBothInOneGroupLedByTheStronger() {
        UUID weak = animal(1, 0.7F);
        UUID strong = animal(2, 0.8F);
        this.manager.create(weak, strong, 5, this.roster);

        DinoGroup group = this.manager.groupOf(weak);
        assertSame(group, this.manager.groupOf(strong));
        assertEquals(strong, group.leader(this.roster));
        assertIndexMatchesGroups(group);
    }

    @Test
    void newcomerThatOutranksTheLeaderLeadsAtOnceEvenOnATie() {
        UUID a = animal(2, 0.75F);
        UUID b = animal(3, 0.75F);
        this.manager.create(a, b, 5, this.roster);
        DinoGroup group = this.manager.groupOf(a);
        assertEquals(a, group.leader(this.roster));

        UUID lowerId = animal(1, 0.75F);
        assertTrue(this.manager.tryJoin(group, lowerId, this.roster));
        assertEquals(lowerId, group.leader(this.roster));
    }

    @Test
    void leaderIsTheBestWhateverTheOrderOfJoining() {
        UUID[] ids = {animal(5, 0.75F), animal(3, 0.75F), animal(4, 0.6F), animal(1, 0.75F), animal(2, 0.7F)};
        UUID expected = new UUID(0L, 1L);

        for (int start = 0; start < ids.length - 1; start++) {
            GroupManager fresh = new GroupManager();
            fresh.create(ids[start], ids[start + 1], 10, this.roster);
            DinoGroup group = fresh.groupOf(ids[start]);
            for (UUID id : ids) {
                if (fresh.groupOf(id) == null) {
                    assertTrue(fresh.tryJoin(group, id, this.roster));
                }
            }
            assertEquals(expected, group.leader(this.roster), "join order starting at " + start);
        }
    }

    @Test
    void joinStopsAtCapacity() {
        UUID a = animal(1, 0.5F);
        UUID b = animal(2, 0.5F);
        UUID c = animal(3, 0.5F);
        this.manager.create(a, b, 2, this.roster);
        DinoGroup group = this.manager.groupOf(a);

        assertFalse(this.manager.tryJoin(group, c, this.roster));
        assertNull(this.manager.groupOf(c));
        assertEquals(2, group.size());
        assertIndexMatchesGroups(group);
    }

    @Test
    void mergeMovesEveryMemberAndEmptiesTheSource() {
        UUID a = animal(1, 0.9F);
        UUID b = animal(2, 0.5F);
        UUID c = animal(3, 0.6F);
        UUID d = animal(4, 0.5F);
        this.manager.create(a, b, 5, this.roster);
        this.manager.create(c, d, 5, this.roster);
        DinoGroup into = this.manager.groupOf(a);
        DinoGroup from = this.manager.groupOf(c);

        assertTrue(this.manager.tryMerge(from, into, this.roster));
        assertEquals(0, from.size());
        assertEquals(4, into.size());
        assertEquals(a, into.leader(this.roster));
        assertIndexMatchesGroups(into, from);
    }

    @Test
    void mergeIsRefusedWhenItWouldOverfill() {
        UUID a = animal(1, 0.9F);
        UUID b = animal(2, 0.5F);
        UUID c = animal(3, 0.6F);
        UUID d = animal(4, 0.5F);
        this.manager.create(a, b, 3, this.roster);
        this.manager.create(c, d, 3, this.roster);
        DinoGroup into = this.manager.groupOf(a);
        DinoGroup from = this.manager.groupOf(c);

        assertFalse(this.manager.tryMerge(from, into, this.roster));
        assertEquals(2, from.size());
        assertEquals(2, into.size());
        assertIndexMatchesGroups(into, from);
    }

    @Test
    void unloadedLeaderIsReplacedButKeepsItsPlace() {
        UUID leader = animal(1, 0.9F);
        UUID next = animal(2, 0.8F);
        UUID last = animal(3, 0.7F);
        this.manager.create(leader, next, 5, this.roster);
        DinoGroup group = this.manager.groupOf(leader);
        this.manager.tryJoin(group, last, this.roster);
        assertEquals(leader, group.leader(this.roster));

        float dominance = this.present.remove(leader);
        assertEquals(next, group.leader(this.roster));
        assertSame(group, this.manager.groupOf(leader));

        this.present.put(leader, dominance);
        assertEquals(next, group.leader(this.roster), "a returning animal does not take the lead without a contest");
        assertIndexMatchesGroups(group);
    }

    @Test
    void weakNewcomerDoesNotLeadWhenTheLeaderIsUnloaded() {
        UUID leader = animal(1, 0.9F);
        UUID strong = animal(2, 0.8F);
        this.manager.create(leader, strong, 5, this.roster);
        DinoGroup group = this.manager.groupOf(leader);
        this.present.remove(leader);

        UUID weak = animal(3, 0.5F);
        assertTrue(this.manager.tryJoin(group, weak, this.roster));
        assertEquals(strong, group.leader(this.roster));
    }

    @Test
    void removedLeaderLeavesTheIndexAndTheNextBestLeads() {
        UUID leader = animal(1, 0.9F);
        UUID next = animal(2, 0.8F);
        this.manager.create(leader, next, 5, this.roster);
        DinoGroup group = this.manager.groupOf(leader);

        this.manager.remove(leader);
        assertNull(this.manager.groupOf(leader));
        assertEquals(next, group.leader(this.roster));
        assertIndexMatchesGroups(group);
    }

    @Test
    void challengeNeedsMoreThanTheMargin() {
        UUID leader = animal(1, 0.75F);
        UUID rival = animal(2, 0.7F);
        this.manager.create(leader, rival, 5, this.roster);
        DinoGroup group = this.manager.groupOf(leader);

        this.present.put(rival, 0.85F);
        assertFalse(group.challenge(rival, 0.1F, this.roster));
        assertEquals(leader, group.leader(this.roster));

        this.present.put(rival, 0.86F);
        assertTrue(group.challenge(rival, 0.1F, this.roster));
        assertEquals(rival, group.leader(this.roster));
    }

    @Test
    void groupWithNoPresentMemberHasNoLeader() {
        UUID a = animal(1, 0.5F);
        UUID b = animal(2, 0.5F);
        this.manager.create(a, b, 5, this.roster);
        DinoGroup group = this.manager.groupOf(a);

        this.present.clear();
        assertNull(group.leader(this.roster));
    }
}
