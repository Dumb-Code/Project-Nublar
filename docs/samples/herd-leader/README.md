# Sample: herd leader behaviour (B18)

This folder is sample code for the group and leader part of plan module B18. It is not in the build. No file under `common/` or `forge/` changed.

The package is `net.dumbcode.projectnublar.entity.ai.behaviour.social`, next to `LargeCarnivoreFight`. The six classes, the wiring snippet and the `Dinosaur` changes below compile against `1.20.1-ai` at 4244f62. The compile check ran in a temporary worktree that is now deleted. `DominanceTest` and `GroupManagerTest` pass. The project has no JUnit setup, so they ran against minimal stand-ins for the JUnit annotations and assertions.

## What it does

| Rule (plan B18, decision of 2026-10-06) | Where |
| --- | --- |
| Animals of one species form a group, up to `group_size` scaled by the herd or pack size gene | `JoinGroup` |
| Small groups merge into a nearby group with a higher-ranked leader, if all members fit | `JoinGroup`, `GroupManager.tryMerge` |
| A dominance value from age, size gene and aggression gene picks the leader. A tie goes to the lower UUID. A newcomer that outranks the leader leads at once, so the leader is the best of the group whatever the order of joining | `Dominance`, `DinoGroup.add`, `DinoGroup.leader` |
| If the leader dies or unloads, the highest-ranked loaded member takes its place, with no contest | `DinoGroup.leader` |
| An animal leaves its group only when it dies, is discarded or changes dimension. An unloaded animal keeps its place | `GroupManager.leave`, change 2 below |
| A rank contest does no damage. It starts only when growth lets the challenger beat the leader by the margin. Then both stop, face each other and play `ThreatDisplay` | `ChallengeGroupLeader`, `DinoGroup.challenge` |
| Distances (scan, cohesion, contest) grow with body width, so they fit a raptor and a sauropod alike | the three behaviours |
| The leader chooses where the group goes. Followers that stray too far walk back to it | wiring below, `FollowGroupLeader` |

## Files

- `Dominance.java`: the pure dominance rule, in 0..1 (age 50%, size gene 30%, aggression gene 20%), and the tie-break.
- `GroupManager.java`: the only record of who is in which group, one per level, indexed by member UUID. It is not saved. Groups form again after a restart, and the best animal of each group leads again.
- `DinoGroup.java`: one group: capacity, members and leader. It works on UUIDs and asks a `Roster` who is present and how dominant, so tests can use a fake roster.
- `JoinGroup.java`, `FollowGroupLeader.java`, `ChallengeGroupLeader.java`: SmartBrainLib behaviours.
- `DominanceTest.java`, `GroupManagerTest.java`: JUnit 5 tests for the rule, the tie-break, and the group invariants (index and member sets agree, merges, leader choice, capacity).

## Wiring into `Dinosaur`

No sensor is needed. `JoinGroup` scans for itself, and only for animals without a group and for leaders.

```java
// getCoreTasks(): after the needs tasks, so food and water targets win.
// ThreatDisplay comes before FollowGroupLeader, so a contest's roar starts before the follower can walk off.
new JoinGroup<>(),
new ThreatDisplay<>(34), // already in the file, commented out
new FollowGroupLeader<>().cohesionRadius(12),

// getIdleTasks(): in the FirstApplicableBehaviour, in place of the OneRandomBehaviour.
new ChallengeGroupLeader<>(),
new SetRandomWalkTarget<Dinosaur>().setRadius(24, 6).speedModifier(0.8F)  // the leader walks far and slowly
        .walkTargetPredicate((dinosaur, pos) -> !BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.BRAIN_OVERRIDE.get()))
        .startCondition(dinosaur -> dinosaur.isIdle() && dinosaur.isGroupLeader()),
new SetRandomWalkTarget<Dinosaur>().dontAvoidWater().setRadius(10, 4)     // all others wander near by, as today
        .walkTargetPredicate((dinosaur, pos) -> !BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.BRAIN_OVERRIDE.get()))
        .startCondition(Dinosaur::isIdle)
```

Write the type argument in `new SetRandomWalkTarget<Dinosaur>()`. With `<>` at the start of a call chain, Java takes `PathfinderMob` and `Dinosaur::isGroupLeader` does not compile. Call `startCondition` last: it returns the base type, so the `SetRandomWalkTarget` methods are no longer in reach after it. The `BRAIN_OVERRIDE` predicate is the one on today's wander line; keep it until B17 deletes that memory. The `isIdle()` conditions stop the wander while the animal roars in a contest, so the two rivals stay facing each other.

## Changes to existing code before this can merge

1. In `Dinosaur`, make the group accessors ask `GroupManager`, and delete `getGroupId()` (it has no caller):

   ```java
   public boolean hasGroup() {
       return GroupManager.groupOf(this) != null;
   }

   public boolean isGroupLeader() {
       return GroupManager.isLeader(this);
   }
   ```

2. In `Dinosaur`, take the animal out of its group when it leaves the level for good. An unloaded animal keeps its place:

   ```java
   @Override
   public void remove(RemovalReason reason) {
       if (reason.shouldDestroy() || reason == RemovalReason.CHANGED_DIMENSION) {
           GroupManager.leave(this);
       }
       super.remove(reason);
   }
   ```

3. In `SetHunting.checkExtraStartConditions`, remove or replace `if(entity.hasGroup()) return false;`. When `hasGroup()` starts to return `true`, that line stops every grouped carnivore from hunting. Every species config has `can_form_group: true`. B18 pack hunting replaces the line.
4. In `MemoryModuleTypeInit`, the group memories `GROUP_UUID`, `GROUP_LEADER_UUID`, `IS_GROUP_LEADER` and `HAS_GROUP` are no longer used for groups. Delete them with the other unused types (plan B17). This also removes the duplicate id `is_pack_leader`.
5. When an animal is tamed (B19), call `GroupManager.leave(dinosaur)`.
6. `Dominance` sets a hatchling's age to -24000 ticks. B16 replaces growth, so this constant must agree with B16. Today a hatched animal spawns as an adult, so all animals get the full age score.
7. `ThreatDisplay` plays the T. rex roar for each species. B24 adds a sound for each species.

## For the lead

The B18 acceptance says "six Gallimimus form one herd with one leader". The Gallimimus config has `group_size` 5, so six unedited Gallimimus form a herd of five and leave one animal alone. Either `group_size` goes to 6 or more, or the acceptance count changes.

## Not in this sample

- Calls (warn, gather, threaten) and raptor pack hunting. They use the same group and leader.
- Weights, margins and base distances as config values (B00).
