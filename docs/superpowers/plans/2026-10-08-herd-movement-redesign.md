# Herd Movement Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the failed follower model with leader journeys, fixed graze spots, trail following in a wedge that narrows to single file, context steering with real clearance-box sampling, a per-tick movement guard and explicit unstacking, so that the eight behaviour targets T1-T8 of the redesign spec hold.

**Architecture:** Pure, unit-tested parts (`ContextSteering`, `HerdBodies`, `HerdSpacing` math, `HerdArea`, `GrazeSchedule`, `ProgressMonitor`, `JourneyPolicy`, `LeaderTrail`, `TravelOrder`, `ClearanceBox`) carry every rule and number. One class reads blocks (`BodyFit`). `DinoGroup` holds the herd's explicit state, journey, centre, spot reservations, travel order and per-tick caches. Three game-facing parts use them: `HerdMoveControl` (steering, guard, unstack, side step), `FollowFormation` (a follower's target per herd state, progress and fallback paths) and the leader's `LeaderJourney` behaviour plus `HerdLeading` tick (journeys, trail, waiting, recentre).

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.2.30 through ModDevGradle, SmartBrainLib 1.15, GeckoLib 4.7.3, Architectury-style layout (`common/` + `forge/`). Python 3 for the GameTest arena generator.

**Spec:** `docs/superpowers/specs/2026-10-08-herd-movement-redesign.md` (binding). Its "Lead decisions (2026-10-08)" and "Lead decisions on the plan review (2026-10-09)" tables override values elsewhere in the spec. Root causes: `.superpowers/sdd/2026-10-08-herd-movement/movement-investigation.md`. Previous plan (style and constraints source): `docs/superpowers/plans/2026-10-08-herd-movement.md`.

## Global Constraints

- Work only in the worktree `C:\Users\kgumm\Documents\Minecraft Modding\Project-Nublar\.claude\worktrees\herd-leader-sample` (branch `herd-leader-sample`). Never edit the main checkout.
- **Do not commit.** The lead chose "no commits" on 2026-10-08. Never `git add`, `git commit`, `git stash`, `git checkout --` or `git reset`. The worktree holds a lot of earlier uncommitted work that must not be touched outside your task's files. Every task ends with a `git status --short` checkpoint instead of a commit.
- All new herd classes go in package `net.dumbcode.projectnublar.entity.ai.behaviour.social`.
- `HerdSpacing.applies(Dinosaur)` stays the single switch for spacing exceptions; every spacing, steering, guard, unstack, side-step and formation path must call it (directly, or by getting the group only through it).
- Every distance, weight and timing is a named `static final` constant in the class that uses it (spec: "All numbers are starting values. They are named constants, one place per class, tuned in playtest").
- Performance budget (spec): herd AI for 20 animals under 1 ms per tick on average; no single tick over 5 ms from herd code; at most 10 context-steering evaluations per herd per tick; at most 2 fallback paths per herd per tick.
- No new dependencies. The project has no JUnit; unit tests run on the stand-in runner: `./gradlew :common:compileJava` first, then `sh tools/junit-standin/run.sh`. The runner cannot load classes that extend SmartBrainLib behaviours (Forge access transformer missing), so every rule that needs a unit test lives in a pure class.
- Pure classes (`ContextSteering`, `HerdBodies`, `HerdArea`, `GrazeSchedule`, `ProgressMonitor`, `JourneyPolicy`, `LeaderTrail`, `TravelOrder`, `ClearanceBox`) import nothing from Minecraft. `ContextSteering` reads the compile-time constant `Dinosaur.MAX_TURN_PER_TICK` (inlined by javac). `HerdSpacing` keeps its existing `Vec3` and entity imports.
- `BodyFit` is the only class that reads blocks. It uses the vanilla collision query (`noCollision`) with real collision shapes. Barrier blocks, leaves, slabs, glass and every other block count by their collision shape; nothing is special-cased (lead feedback after the forest-arena playtest).
- No route point, graze spot, fallback goal or journey target the herd steers to may be one the clearance box cannot reach: graze spots must pass the straight-line reach check, travel targets are points the leader walked, fallback goals are trail points or reserved spots.
- In-game checks: implementers never launch the game and never run GameTests. In Task 21 the controller launches the headed client (`./gradlew :forge:runClient` from the worktree root) and the lead tests. Never run `runGameTestServer` (the mod cannot load on a dedicated server today).
- GameTests are run by the lead one at a time in a creative world: `/test run herdgametests.<method name in lower case>` (Forge `@GameTestHolder`, no template prefix). Every GameTest task states the exact command for each test. Spawns and goals use the `FLOOR_Y` constant (relative y=2, the first walkable layer; the template floor is relative y=1).
- Arenas are generated, never hand-edited: `python tools/gametest-templates/make_templates.py` from the worktree root writes `forge/src/main/resources/data/projectnublar/structures/*.nbt`. The generator uses fixed seeds and stops with an error if an arena has no route for a 2 x 3 body.
- Lead decisions used throughout (they override the spec body): hard minimum = personal space (3 for Gallimimus) while moving, resting bodies may be closer (`RESTING_GAP` 0.5, so 2.5); the leader waits up to 200 ticks on roaming journeys and never on urgent ones; roaming journeys every 2400-6000 ticks, 32-64 blocks, needs also drive journeys; a per-species clearance box from the model size; graze stay 600-1800 ticks then a 50% chance of a short move; a wedge in open ground narrowing to 1-3 abreast, then single file; travel slots personal space + 2 apart in open ground and + 1 in tight ground, with a debug-stick toggle for 4 everywhere (plan review decision 9); a resting member may get up to unstack; a follower leaves the line on a journey only for a critical need.
- Code style: match the surrounding code; Javadoc on every class and on non-obvious methods; no per-tick collection allocations unless a neighbour is actually in range. Small value records per tick (`TrailPoint`, `Target`, `Vec3`) are accepted (ledger ruling of the previous plan).
- Shell: Git Bash on Windows. If plan code does not compile against the real code, make the smallest fix and report it as a concern; do not redesign.

## Review Focus

1. **More members than graze spots** (a crowded herd area, or arena walls cutting the circle; random best-of-8 sampling cannot place 20 spots 5 apart in a 13.6-block circle every time). A member without a spot that stands outside the herd area walks inside it and stops; it never stands outside or pushes into others. Pinned by `herdTravelsAlongTrail` (Task 20: all members within R of the new centre in a 22-wide arena smaller than 2R) and the no-spot branch in Task 13.
2. **A target behind a solid wall** (barriers, rock, trunks; the forest-arena playtest showed a line of animals pressing into the barrier wall). A walker turns along the wall or stops and re-paths; it never presses into it for more than 40 ticks. Pinned by `wallAheadTurnsAlongTheWall` and `deadEndStops` in Task 2, the `wallBlockedTicks` assertion in every travel GameTest of Task 20, and the straight-line reach check for spots in Task 13.
3. **The leader changes mid-journey** (death, unload, contest). The journey ends, the herd grazes, and nobody reads the old trail. Pinned by `leaderChangeEndsTheJourney` in Task 11.
4. **A member joins or leaves mid-journey.** The travel order is rebuilt, no follower reads a layout index past the order, and a leaver's spot and order place are freed. Pinned by `joinDuringTravelMarksTheOrderDirty`, `leaveDropsSpotAndOrderPlace` and `layoutGivesEachFollowerItsOwnTarget` in Task 11.
5. **A herd of two, and a trail that is still too short.** The single follower walks directly on the trail (offset 0); a follower whose target arc lies before the trail start waits where it stands instead of converging on the start point. Pinned by `singleFollowerWalksOnTheTrail` and `followWaitsWhileItsTargetIsNotOnTheTrailYet` in Task 8.
6. **A journey that outlives one vanilla walk.** SmartBrainLib's `MoveToWalkTarget` runs 150 to 249 ticks and then erases the walk target, and a vanilla path reaches at most the follow range (35 blocks for every dinosaur), so a 32-64 block journey needs several walks. The leader re-sets its walk target each time a walk ends while it still gets on; a walk that ends twice in one place is the arrival. Pinned by `thePathEndingTwiceInOnePlaceEndsTheJourney` in Task 11, the re-issue in Task 15 and the goal checks of every travel GameTest in Task 20.
7. **Standing followers in the leader's line at a journey start.** Followers whose slot is not on the trail yet stand still, and the leader (on a vanilla path) cannot steer round them. A follower standing in the leader's line steps out of it once at the start (Task 17), and a member the leader's guard stops at for 10 ticks steps until it is a hard minimum plus a margin beside the line (never 2 blocks, which is inside the 3-block minimum). Pinned by `memberOnTheLeftStepsFurtherLeft` and the lead's item 5 in Task 21.

## Ambiguities resolved in this plan

| Spec text | Resolution |
| --- | --- |
| Lanes `w + 1` apart, rows `w + 1` apart | Decision 4 raised the hard minimum to 3 = `w + 1`, so slots that far apart sit exactly on the mask limit. Plan review decision 9: slot spacing is personal space + 2 (5 for Gallimimus) in open ground and personal space + 1 (4) in tight ground. Each row is tried at the open spacing first; if the ground there gives it fewer lanes than it wants, it is laid at the tight spacing. The spacing is both the row step and the lane gap. `DebugFlags.fixedTravelSpacing` (debug stick) uses the tight spacing everywhere. |
| Lane count = open width / (w + 1) | Lanes are measured per trail point as side room on the tighter side: 3 lanes if it has room for an offset of one spacing, 2 if it has half a spacing, else 1 (`TravelOrder.lanes`). Side room is measured up to 5.5 blocks (`BodyFit.MAX_SIDE_ROOM`), so open ground can qualify at the open spacing. |
| Decision 1 "a wedge in open ground" | Row r (0 = nearest the leader) holds at most `2 + r` followers and never more than 3 or the lanes the ground allows: leader, 2, 3, 3, ... in open ground; 1 per row in a gap. |
| Decision 5 "per-species box from the model size" | `ClearanceBox.MODEL_SIZES` holds each adult model's cross width and top (render scale applied, measured by `tools/clearance/measure_models.py`). Box = max(hitbox, model x growth scale) + 0.25 per side, + 0.25 on top, rounded up to 0.25. Model length is not in the box (an axis-aligned box cannot follow the yaw). Hitbox changes are the separate Task 22. |
| "Danger: a member within the graze spacing" | Applied in GRAZE only (plan review decision 12). In TRAVEL the slots are 4 to 5 apart, so every heading toward the member ahead would carry it and the column would weave. |
| Speed "1.2 behind, 1.0 near, 0 ahead" | Linear in the arc gap: 0 at 0.5 blocks ahead, 1.2 at 3 blocks behind (`TravelOrder.followSpeed`); it settles about 1.5 blocks behind its slot at the leader's 0.7, with no overshoot cycle. |
| "No spot found: stand still" | Also: if the member stands outside the herd area, it first walks inside it (Review Focus 1). |
| Fallback range 24 | In graze, only spots within 24 blocks of the member are picked; a spot farther away (after a journey) is dropped and the member first walks back inside the herd area, then picks again. In travel the goal is a trail point and is not clipped (clipping could put it inside a trunk). |
| Stuck = no progress while not waiting | "Waiting" means stopped by the speed rule or by members (yielding). A stop because every heading is blocked by blocks is not waiting, so a dead end leads to a fallback path; after `BLOCKED_FALLBACK_TICKS` (10) of that the member is stuck at once (lead decision 14), without waiting out the 40-tick window. Once stuck, it asks the herd budget every tick for one window, because `FallbackBudget` queues only members that keep asking. |
| "Arrive ... or its path ends" | A vanilla path reaches at most the follow range (35 blocks) and `MoveToWalkTarget` runs 150-249 ticks, so on a roaming journey the leader re-sets its walk target each time its path ends while it has moved at least 0.5 blocks since the previous end; a path that ends twice in one place is the arrival. An urgent trip's walk target belongs to the need, so it ends with it. |
| "Slow to 0.3, then stop" | The stop is speed 0 on the navigation; the walk target and path stay, because restarting `MoveToWalkTarget` costs its cooldown of up to 39 ticks. |
| "That member steps 2 blocks sideways" | 2 blocks is inside the 3-block hard minimum of lead decision 4, so the leader would stop at it again. The member steps until it is the hard minimum plus `SIDE_STEP_MARGIN` (0.5) beside the leader's line, after `LEADER_BLOCKED_TICKS` (10, not 20). |
| "Closer than the hard minimum ... for 20 ticks" (unstack) | 10 ticks (`UNSTACK_AFTER_TICKS`), so a legal pair that becomes too close (a resting body gets up) is apart again inside T4's one second. |
| "Cache per herd per tick" | The stand results (fit, ground, hazards) are kept for `STAND_CACHE_TICKS` (20): the ground does not change faster than that, and a per-tick cache is empty for every probe (members move 0.3-0.7 blocks per tick, so no key repeats). The guard's fit test is cached per tick. |
| "Every 2 ticks, staggered" and "at most 10 evaluations per herd per tick" | A fixed stagger by entity id; a member the budget refused is due again the next tick, so the members that tick last are not starved. A stopped member re-evaluates on the same cadence, not every tick. Each evaluation probes the box 1 block ahead on all 12 headings, and 2 and 3 blocks ahead only on the previous heading and the `DEEP_PROBES` (4) best open ones; the other open headings count as masked. |
| Lane offset "kept inside the side room" | Inside the tightest room from the follower's projection to `ROOM_AHEAD` (4 blocks, about one turning radius) past its look-ahead point, so a follower in an outer lane moves in before a narrowing instead of hitting its edge. Lanes per row use the tightest room within `ROW_ROOM_WINDOW` (2 blocks) of the row's arc, so they do not flicker between trail points. |
| Rest during a journey | Plan review decision 17: on a roaming journey a travelling follower starts a rest only when its stamina is critical (below half the rage threshold, `HerdNeeds.CRITICAL_STAMINA_FACTOR`; a normal rest starts at the rage threshold); on an urgent trip it rests whenever tired. A rest begun before the journey ends on its own. |
| Rank for unstacking in GRAZE | Leader 0, then the last travel order, then members not in it (rank tie broken by UUID). |
| Leader-blocked rule for a resting member | It gets up first (as decision 7 allows for stacking), then steps aside. |
| "Fleeing" urgent trips (decision 2) | No flee behaviour exists today; any walk target the herd did not plan that leaves the herd area counts as urgent (water, food, feeder). |

## File map

`.../social` is `common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social`; `test/.../social` is `common/src/test/java/net/dumbcode/projectnublar/entity/ai/behaviour/social`.

| File | Decision (spec Components) | Responsibility | Task |
| --- | --- | --- | --- |
| `.../social/FollowFormation.java` | Keep, shrink (deleted in Task 1, rewritten in 13-14) | A follower's target per herd state, progress monitor, fallback requests, making way at a journey start | 1, 13, 14, 17 |
| `.../social/WedgeLayout.java`, `test/.../social/WedgeLayoutTest.java` | Delete | Replaced by `TravelOrder` | 1 |
| `.../social/HerdSteering.java`, `test/.../social/HerdSteeringTest.java` | Replace | Replaced by `ContextSteering` | 1 |
| `.../social/StepCheck.java` | Replace | Replaced by `BodyFit` | 1 |
| `.../social/ContextSteering.java` (new) | Add | 12 headings, interest, hysteresis, stop, speed factor. Pure | 2 |
| `.../social/HerdSpacing.java` | Keep, shrink | `applies`, personal space, `hardMinimum`, `wouldCrowd`, `stepCrowds`, `away`, `sideStep`, `herdRadius`; `steer` and `crowding` deleted | 1, 3, 11, 17 |
| `.../social/HerdBodies.java` (new) | Add (spacing support) | Per-tick member snapshot; masks, guard blocker, free distance, unstack queries. Pure | 3, 17 |
| `.../social/HerdArea.java` | Keep, change | New radius, graze spacing, sampling, `pickSpot` with the reach predicate; `anchor` deleted | 4 |
| `.../social/GrazeSchedule.java`, `.../social/ProgressMonitor.java` (new) | Add | Graze timers; stuck detection. Pure | 5 |
| `.../social/JourneyPolicy.java` (new) | Add (policy part of `LeaderJourney`) | Journey and graze-move timing, arrival, waiting, recentre. Pure | 6 |
| `.../social/LeaderTrail.java` | Keep, change | Record by distance, 128 points, arc, projection, point at arc, side room per point. Pure | 7 |
| `.../social/TravelOrder.java` (new) | Add | Order by distance, wedge and lane layout, follow target and speed. Pure | 8 |
| `.../social/ClearanceBox.java` (new), `tools/clearance/measure_models.py` (new), `tools/junit-standin/stub/org/junit/jupiter/api/Assertions.java` | Add | Per-species clearance box, cache key; model measurement; `assertNotEquals` for the stand-in | 9 |
| `.../social/BodyFit.java` (new) | Add | The only block reader: stand height, fit, reach, side room, per-tick cache | 10 |
| `.../social/DinoGroup.java` | Keep, change | Explicit state, journey, centre, spots, travel order, layout cache, evaluation cap, ranks; slots and the 40-tick inference deleted | 1, 11, 15 |
| `.../social/GroupManager.java` | Keep, add | `bodiesOf` snapshot | 11 |
| `.../social/HerdMoveControl.java` | Keep, rewrite `tick` | Modes stop, steer, path, unstack, side step; the guard; step aside and `stepFailures` deleted | 1, 12, 17 |
| `.../social/LeaderJourney.java` (new) | Add | Leader graze moves and roaming journeys | 15 |
| `.../social/HerdLeading.java` | Rewrite | Trail recording with side room, arrival and settle, waiting rule, recentre, urgent journeys | 7, 11, 15 |
| `.../social/HerdNeeds.java` (new) | Add | Decision 8 gate for needs during journeys; rest on a roaming journey only at critical stamina (decision 17) | 16 |
| `.../social/FallbackBudget.java`, `TickTimer.java`, `HerdTimings.java`, `HerdState.java` | Keep | Unchanged | - |
| `.../social/ChallengeGroupLeader.java` | Keep | Javadoc link fix only | 1 |
| `common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java` | Change | Core and idle task wiring, pushing comment | 1, 12, 13, 15, 16 |
| `common/src/main/java/net/dumbcode/projectnublar/item/DebugStick.java` | Change | Herd state line; sneak-use on a dinosaur switches the travel spacing | 14, 15 |
| `common/src/main/java/net/dumbcode/projectnublar/util/DebugFlags.java` | Change | `fixedTravelSpacing` flag (plan review decision 9) | 14 |
| `test/.../social/*Test.java` | Change / add | Unit tests | 1-9, 11, 16, 17 |
| `tools/gametest-templates/make_templates.py`, `forge/src/main/resources/data/projectnublar/structures/*.nbt` | Change | Natural forest, corridor, low gaps; route check | 18 |
| `forge/src/main/java/net/dumbcode/projectnublar/gametest/HerdGameTests.java` | Rewrite | GameTests | 19, 20 |
| `common/src/main/java/net/dumbcode/projectnublar/init/EntityInit.java` | Change (separable) | Hitboxes that do not match their models | 22 |

**Verification commands used throughout** (run from the worktree root in Git Bash):

```bash
./gradlew :common:compileJava
```

```bash
sh tools/junit-standin/run.sh
```

```bash
./gradlew :forge:compileJava
```

The runner prints `N run, M failed` on its last line. Before Task 1 it reports `76 run, 0 failed`.

## Behaviour targets and decisions mapped to tests

| Target (spec) | Unit tests | GameTests | Lead checklist (Task 21) |
| --- | --- | --- | --- |
| T1 grazing members stay put | `GrazeScheduleTest` (Task 5) | `grazingMembersStayPut` (Task 19) | item 1 |
| T2 grazing spreads over the area | `HerdAreaTest.pickSpot*` (Task 4) | `grazingMembersStayPut` spot spacing assertion (Task 19) | item 2 |
| T3 small leader moves do not move the herd | `DinoGroupTest.pinnedHerdRefusesJourneys` (Task 11) | `smallLeaderMovesKeepGraze` (Task 19) | item 3 |
| T4 spacing holds | `HerdSpacingTest`, `HerdBodiesTest` (Task 3) | `grazingHerdHoldsHardMinimum` (Task 19), `herdTravelsAlongTrail` (Task 20) | item 4 |
| T5 no stacking | `HerdBodiesTest.crowdedBy*` (Task 17) | `stackedMembersSeparate` (Task 19) | item 4 |
| T6 travel only on real journeys | `JourneyPolicyTest` (Task 6), `DinoGroupTest` journey tests and `thePathEndingTwiceInOnePlaceEndsTheJourney` (Task 11) | `smallLeaderMovesKeepGraze` (state stays GRAZE), `herdTravelsAlongTrail` (centre at the goal, Task 20) | item 5 |
| T7 followers use the leader's route | `LeaderTrailTest` (Task 7), `TravelOrderTest` incl. `followKeepsTheLaneOffsetInsideTheRoomAhead` (Task 8), `ContextSteeringTest.wallAheadTurnsAlongTheWall` (Task 2) | `herdCrossesForest`, `herdPassesCorridor` (Task 20) | item 6 |
| T8 fitting | `ClearanceBoxTest` (Task 9) | `memberNeverEntersLowGap` (Task 20) | item 7 |
| Performance budget | `DinoGroupTest.tenEvaluationsPerTickAtMost` (Task 11), `FallbackBudgetTest` (existing) | `herdCrossesForest` fallback cap (Task 20) | item 8 |
| Decision 1 wedge narrowing | `TravelOrderTest.openGroundMakesAWideWedge`, `aGapNarrowsOnlyTheRowsInsideIt` | `herdPassesCorridor` | item 6 |
| Decision 2 waiting | `JourneyPolicyTest.urgentTripsNeverWait`, `roamingSlowsThenStops`, `waitingEndsAfterTenSeconds` | `herdTravelsAlongTrail` | item 5 |
| Decision 3 journeys | `JourneyPolicyTest.journeysComeEvery2To5Minutes`, `journeyDistanceIs32To64Blocks` | - | item 5 |
| Decision 4 hard minimum | `HerdSpacingTest.hardMinimumIsThePersonalSpaceWhileMoving`, `aRestingBodyMayBeCloser` | spacing assertions | item 4 |
| Decision 5 clearance box | `ClearanceBoxTest` | `memberNeverEntersLowGap` | item 7; Task 22 |
| Decision 6 grazing | `GrazeScheduleTest` | `grazingMembersStayPut` | item 1 |
| Decision 7 stacking | `HerdBodiesTest.crowdedBy*` | `stackedMembersSeparate` | item 4 |
| Decision 8 needs on a journey | `HerdNeedsTest` (Task 16) | - | item 9 |
| Journeys outlive one vanilla walk (Review Focus 6) | `DinoGroupTest.thePathEndingTwiceInOnePlaceEndsTheJourney` (Task 11) | goal check in `herdTravelsAlongTrail`, `herdCrossesForest`, `herdSkirtsWater` (Task 20) | item 5 |
| Lead feedback: walls | `ContextSteeringTest.wallAheadTurnsAlongTheWall`, `deadEndStops` | `wallBlockedTicks` check in all Task 20 tests | item 6 |

## Tasks needing the most capable model

Tasks 12 (move control), 13 and 14 (follower behaviour), 15 (leader journeys) and 17 (unstack and make way) combine several parts and vanilla movement details; give them the most capable model. Tasks 18 and 20 (arenas and travel GameTests) need care with coordinates. Tasks 1-11 and 16 are transcription of complete code.

---
### Task 1: Remove the old follower model

The old follower model (wedge slots, seek plus separation, the detect-only step check) is removed first, so the pure parts after it can change their interfaces freely. After this task the code compiles, followers stand still (no follower behaviour), and the leader still wanders with `SetRandomWalkTarget`. Nobody runs the game between tasks.

**Files:**
- Delete: `.../social/FollowFormation.java`, `.../social/WedgeLayout.java`, `.../social/HerdSteering.java`, `.../social/StepCheck.java`
- Delete: `test/.../social/WedgeLayoutTest.java`, `test/.../social/HerdSteeringTest.java`
- Modify: `.../social/HerdSpacing.java` (whole file), `.../social/DinoGroup.java` (whole file), `.../social/HerdMoveControl.java` (whole file), `.../social/ChallengeGroupLeader.java:25-27` (Javadoc), `common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java:57,581`
- Test: `test/.../social/HerdSpacingTest.java` (whole file), `test/.../social/DinoGroupTest.java` (whole file)

**Interfaces:**
- Consumes: nothing new.
- Produces: `HerdSpacing` keeps `public static boolean applies(Dinosaur)`, `static final double GAP`, `static double personalSpace(double width, double otherWidth)`, `record Neighbour(double dx, double dz, double space)`, `static Vec3 away(List<Neighbour>)`. `DinoGroup` keeps membership, leadership, `HerdState state()`, `void updateState(boolean leaderTravelling, long gameTime)` (removed in Task 11), `LeaderTrail trail()`, `FallbackBudget fallbacks()`, `public int mostFallbacksInOneTick()`. `HerdMoveControl` keeps `public HerdMoveControl(Dinosaur)` and `public boolean isSteering()`.

- [ ] **Step 1: Delete the old classes and their tests**

```bash
rm common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/FollowFormation.java
rm common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/WedgeLayout.java
rm common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdSteering.java
rm common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/StepCheck.java
rm common/src/test/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/WedgeLayoutTest.java
rm common/src/test/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdSteeringTest.java
```

- [ ] **Step 2: Shrink `HerdSpacing` (delete `steer`, `crowding` and their constants)**

Replace the whole file `.../social/HerdSpacing.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

/**
 * Spacing between herd members: the single switch for exceptions ({@link #applies}), the personal space,
 * and the step straight away from members that are too close ({@link #away}, used to unstack).
 */
public final class HerdSpacing {

    /** Gap between bodies, in blocks, that herd members keep. */
    static final double GAP = 1.0D;

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
}
```

- [ ] **Step 3: Remove slots and the step check from `DinoGroup`**

Replace the whole file `.../social/DinoGroup.java` with:

```java
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
    private final Set<UUID> memberView = Collections.unmodifiableSet(this.members);
    private @Nullable UUID leader;

    /** After this many ticks without travelling, a herd goes back to grazing. Removed in the redesign's Task 11. */
    static final int GRAZE_AFTER_TICKS = 40;

    private HerdState state = HerdState.GRAZE;
    private long lastTravelTick = Long.MIN_VALUE;
    private final LeaderTrail trail = new LeaderTrail();
    private final FallbackBudget fallbacks = new FallbackBudget();

    DinoGroup(int capacity) {
        this.capacity = capacity;
    }

    int capacity() {
        return this.capacity;
    }

    public int size() {
        return this.members.size();
    }

    /** A read-only view of the member UUIDs. */
    Set<UUID> memberIds() {
        return this.memberView;
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
            this.trail.clear();
        }
    }

    /** Removes the member. If it was the leader, the next {@link #leader} call elects a new one. */
    void remove(UUID member) {
        this.members.remove(member);
        this.fallbacks.forget(member);
        if (member.equals(this.leader)) {
            this.leader = null;
            this.trail.clear();
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
        this.trail.clear();
        return true;
    }

    HerdState state() {
        return this.state;
    }

    /**
     * Called from the leader's tick: travel starts at once, grazing after {@link #GRAZE_AFTER_TICKS} without
     * travel. When travel starts, the trail is cleared. Replaced by explicit journeys in Task 11.
     */
    void updateState(boolean leaderTravelling, long gameTime) {
        if (leaderTravelling) {
            this.lastTravelTick = gameTime;
            if (this.state != HerdState.TRAVEL) {
                this.state = HerdState.TRAVEL;
                this.trail.clear();
            }
        } else if (this.state == HerdState.TRAVEL && gameTime - this.lastTravelTick > GRAZE_AFTER_TICKS) {
            this.state = HerdState.GRAZE;
        }
    }

    LeaderTrail trail() {
        return this.trail;
    }

    FallbackBudget fallbacks() {
        return this.fallbacks;
    }

    /** The most fallback paths this herd started in one tick. For GameTests. */
    public int mostFallbacksInOneTick() {
        return this.fallbacks.mostGrantedInOneTick();
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
        if (best == null ? this.leader != null : !best.equals(this.leader)) {
            this.trail.clear();
        }
        this.leader = best;
    }
}
```

- [ ] **Step 4: Reduce `HerdMoveControl` to vanilla movement plus timing**

Replace the whole file `.../social/HerdMoveControl.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.control.MoveControl;

/**
 * Vanilla movement for herd animals. The herd layer (context steering and the guard) is rebuilt in the
 * redesign's Task 12; until then no member steers. Herd work is timed for the budget.
 */
public class HerdMoveControl extends MoveControl {

    public HerdMoveControl(Dinosaur dinosaur) {
        super(dinosaur);
    }

    /** {@code true} while herd steering moves the animal. Read by the stamina rule in {@code Dinosaur}. */
    public boolean isSteering() {
        return false;
    }

    @Override
    public void tick() {
        ServerLevel level = (ServerLevel) this.mob.level();
        long start = System.nanoTime();
        super.tick();
        HerdTimings.record(level.getGameTime(), System.nanoTime() - start);
    }
}
```

- [ ] **Step 5: Unwire the follower behaviour in `Dinosaur`**

In `common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java`, delete the import line:

```java
import net.dumbcode.projectnublar.entity.ai.behaviour.social.FollowFormation;
```

In `getCoreTasks()`, replace:

```java
                new ThreatDisplay<>(34),
                new FollowFormation<>()
        );
```

with:

```java
                new ThreatDisplay<>(34)
        );
```

- [ ] **Step 6: Fix the Javadoc links to the deleted class**

In `.../social/ChallengeGroupLeader.java`, replace:

```java
 * The face-off clears both walk targets. The wiring keeps them clear: {@code ThreatDisplay} runs before
 * {@link FollowFormation} in the core tasks, and while it plays, {@code isIdle()} is false, which
 * stops {@link FollowFormation} and the gated wander lines.
```

with:

```java
 * The face-off clears both walk targets. The wiring keeps them clear: {@code ThreatDisplay} runs before
 * {@code FollowFormation} in the core tasks, and while it plays, {@code isIdle()} is false, which
 * stops {@code FollowFormation} and the gated wander lines.
```

- [ ] **Step 7: Remove the deleted methods' tests**

Replace the whole file `test/.../social/HerdSpacingTest.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Unit tests for the herd spacing arithmetic. Runs on the JUnit stand-in runner (tools/junit-standin). */
class HerdSpacingTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void personalSpaceIsHalfOfEachWidthPlusTheGap() {
        assertEquals(2D + HerdSpacing.GAP, HerdSpacing.personalSpace(2D, 2D), EPSILON);
        assertEquals(1.3D + HerdSpacing.GAP, HerdSpacing.personalSpace(0.6D, 2D), EPSILON);
    }

    @Test
    void awayFromOneNeighbourIsStraightBack() {
        Vec3 away = HerdSpacing.away(List.of(new HerdSpacing.Neighbour(1D, 0D, 3D)));
        assertEquals(-1D, away.x, EPSILON);
        assertEquals(0D, away.z, EPSILON);
    }

    @Test
    void awayFromBalancedNeighboursIsSideways() {
        Vec3 away = HerdSpacing.away(List.of(new HerdSpacing.Neighbour(1D, 0D, 3D), new HerdSpacing.Neighbour(-1D, 0D, 3D)));
        assertEquals(0D, away.x, EPSILON);
        assertEquals(1D, Math.abs(away.z), EPSILON);
    }

    @Test
    void awayFromNobodyIsZero() {
        assertEquals(Vec3.ZERO, HerdSpacing.away(List.of()));
    }

    @Test
    void awayFromANeighbourOnTheSameSpotIsAUnitStep() {
        Vec3 away = HerdSpacing.away(List.of(new HerdSpacing.Neighbour(0D, 0D, 3D)));
        assertEquals(1D, away.length(), EPSILON);
    }
}
```

Replace the whole file `test/.../social/DinoGroupTest.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DinoGroupTest {

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

    private UUID animal(long id, float dominance) {
        UUID uuid = new UUID(0L, id);
        this.present.put(uuid, dominance);
        return uuid;
    }

    @Test
    void newGroupGrazes() {
        assertSame(HerdState.GRAZE, new DinoGroup(5).state());
    }

    @Test
    void travelStartsAtOnceAndGrazingWaits() {
        DinoGroup group = new DinoGroup(5);
        group.updateState(true, 100L);
        assertSame(HerdState.TRAVEL, group.state());
        group.updateState(false, 100L + DinoGroup.GRAZE_AFTER_TICKS);
        assertSame(HerdState.TRAVEL, group.state(), "not yet: exactly the wait");
        group.updateState(false, 101L + DinoGroup.GRAZE_AFTER_TICKS);
        assertSame(HerdState.GRAZE, group.state());
    }

    @Test
    void enteringTravelClearsTheTrailButTravellingOnKeepsIt() {
        DinoGroup group = new DinoGroup(5);
        group.trail().record(0, 64, 0);
        group.trail().record(5, 64, 0);
        group.updateState(true, 1L);
        assertEquals(0, group.trail().size());
        group.trail().record(0, 64, 0);
        group.trail().record(5, 64, 0);
        group.updateState(true, 6L);
        assertEquals(2, group.trail().size());
    }

    @Test
    void leaderChangeClearsTheTrail() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID next = animal(2, 0.8F);
        group.add(leader, this.roster);
        group.add(next, this.roster);
        group.trail().record(0, 64, 0);
        group.trail().record(5, 64, 0);
        this.present.remove(leader);
        group.leader(this.roster);
        assertEquals(0, group.trail().size());
    }
}
```

- [ ] **Step 8: Check that nothing else uses the deleted classes**

Run: `grep -rn "HerdSteering\|WedgeLayout\|StepCheck\|FollowFormation\|slotOf\|stepCheck()\|HerdSpacing.steer\|HerdSpacing.crowding" common/src forge/src`
Expected: no code references. Only two text matches remain: the Javadoc link `{@link StepCheck#openWidth}` in `LeaderTrail.java` (the file is rewritten in Task 7) and the comment `// Animals without a group wander on their own, as before. Followers move with FollowFormation.` in `Dinosaur.java`.

- [ ] **Step 9: Compile and run the unit tests**

Run: `./gradlew :common:compileJava`
Expected: `BUILD SUCCESSFUL`.

Run: `sh tools/junit-standin/run.sh`
Expected: last line `50 run, 0 failed` (76 minus 7 wedge, 9 steering, 7 `steer` and 3 slot tests).

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 10: Checkpoint (no commit)**

Run: `git status --short`
Expected: the four deleted classes and two deleted tests show as gone (`D` or no longer listed under `??`), the modified files are listed; nothing is staged.

### Task 2: Context steering scorer

The one local mover's decision rule (spec "Context steering"): 12 headings, interest toward the desired direction minus a turn cost, masks and danger supplied by the caller, hysteresis, a stop candidate, and the speed factor. It answers the lead's wall feedback: a heading into a wall is masked, so a walker turns along the wall, and a dead end gives "stop" (which the follower's progress monitor turns into a fallback path).

**Files:**
- Create: `.../social/ContextSteering.java`
- Test: `test/.../social/ContextSteeringTest.java`

**Interfaces:**
- Consumes: `Dinosaur.MAX_TURN_PER_TICK` (float constant 10).
- Produces (all `static`, package-private): `int HEADINGS = 12`, `int STOP = -1`, `double STEP_DEGREES`, `double TURN_COST_PER_DEGREE`, `double MAX_TURN_DEGREES`, `double HYSTERESIS`, `double STOP_SCORE`, `double DANGER_BLOCKED_FAR`, `double DANGER_NEAR_MEMBER`, `double SLOW_RADIUS`, `double MIN_ARRIVAL_FACTOR`, `double FREE_SLOW_DISTANCE`, `double MIN_FREE_FACTOR`; `double dirX(int heading)`, `double dirZ(int heading)`, `float yawOf(int heading)`, `double wrapDegrees(double degrees)`, `double interest(int heading, double desiredX, double desiredZ, float currentYaw)`, `int pick(double desiredX, double desiredZ, float currentYaw, int previous, boolean[] masked, double[] danger)`, `double speedFactor(double distanceToTarget, double freeDistance)`, `int nearestHeading(float yaw)`. Heading `i` faces Minecraft yaw `i x 30` degrees, direction `(-sin yaw, cos yaw)`.

- [ ] **Step 1: Write the failing test**

Create `test/.../social/ContextSteeringTest.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextSteeringTest {

    private static final double EPSILON = 1.0E-9D;

    private static boolean[] open() {
        return new boolean[ContextSteering.HEADINGS];
    }

    private static double[] calm() {
        return new double[ContextSteering.HEADINGS];
    }

    private static double dx(double yaw) {
        return -Math.sin(Math.toRadians(yaw));
    }

    private static double dz(double yaw) {
        return Math.cos(Math.toRadians(yaw));
    }

    @Test
    void headingsFollowMinecraftYaw() {
        assertEquals(0D, ContextSteering.dirX(0), EPSILON);
        assertEquals(1D, ContextSteering.dirZ(0), EPSILON);
        assertEquals(-1D, ContextSteering.dirX(3), EPSILON);
        assertEquals(0D, ContextSteering.dirZ(3), EPSILON);
        assertEquals(90F, ContextSteering.yawOf(3), 0F);
    }

    @Test
    void freeWayPicksTheHeadingTowardTheTarget() {
        assertEquals(0, ContextSteering.pick(0D, 1D, 0F, ContextSteering.STOP, open(), calm()));
    }

    @Test
    void maskedHeadingIsNeverPicked() {
        boolean[] masked = open();
        masked[0] = true;
        int heading = ContextSteering.pick(0D, 1D, 0F, ContextSteering.STOP, masked, calm());
        assertTrue(heading == 1 || heading == 11, "should take a neighbouring heading, was " + heading);
    }

    @Test
    void wallAheadTurnsAlongTheWall() {
        // A wall across the way: every heading within 60 degrees of the target is blocked.
        boolean[] masked = open();
        for (int heading : new int[]{10, 11, 0, 1, 2}) {
            masked[heading] = true;
        }
        int heading = ContextSteering.pick(0D, 1D, 0F, ContextSteering.STOP, masked, calm());
        assertTrue(heading == 3 || heading == 9, "should walk along the wall, was " + heading);
    }

    @Test
    void allMaskedStops() {
        boolean[] masked = open();
        java.util.Arrays.fill(masked, true);
        assertEquals(ContextSteering.STOP, ContextSteering.pick(0D, 1D, 0F, 0, masked, calm()));
    }

    @Test
    void deadEndStops() {
        // Only the headings back the way it came are open: standing beats turning round.
        boolean[] masked = open();
        java.util.Arrays.fill(masked, true);
        masked[5] = false;
        masked[6] = false;
        masked[7] = false;
        assertEquals(ContextSteering.STOP, ContextSteering.pick(0D, 1D, 0F, ContextSteering.STOP, masked, calm()));
    }

    @Test
    void hysteresisKeepsThePreviousHeading() {
        // The target is at yaw 20: heading 1 (yaw 30) scores a little better than heading 0, but not by HYSTERESIS.
        assertEquals(1, ContextSteering.pick(dx(20D), dz(20D), 0F, ContextSteering.STOP, open(), calm()));
        assertEquals(0, ContextSteering.pick(dx(20D), dz(20D), 0F, 0, open(), calm()));
    }

    @Test
    void clearlyBetterHeadingWins() {
        assertEquals(3, ContextSteering.pick(dx(90D), dz(90D), 0F, 0, open(), calm()));
    }

    @Test
    void dangerSteersToTheSafeSide() {
        double[] danger = calm();
        danger[0] = ContextSteering.DANGER_BLOCKED_FAR;
        assertEquals(1, ContextSteering.pick(0D, 1D, 0F, ContextSteering.STOP, open(), danger));
    }

    @Test
    void speedSlowsOnArrivalAndNearBlocks() {
        assertEquals(1D, ContextSteering.speedFactor(10D, 10D), EPSILON);
        assertEquals(0.5D, ContextSteering.speedFactor(1.5D, 10D), EPSILON);
        assertEquals(ContextSteering.MIN_ARRIVAL_FACTOR, ContextSteering.speedFactor(0D, 10D), EPSILON);
        assertEquals(0.5D, ContextSteering.speedFactor(10D, 1.5D), EPSILON);
        assertEquals(ContextSteering.MIN_FREE_FACTOR, ContextSteering.speedFactor(10D, 0D), EPSILON);
    }

    @Test
    void nearestHeadingRoundsYaw() {
        assertEquals(9, ContextSteering.nearestHeading(-90F));
        assertEquals(3, ContextSteering.nearestHeading(95F));
        assertEquals(0, ContextSteering.nearestHeading(359F));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors, `cannot find symbol ... ContextSteering`.

- [ ] **Step 3: Write the implementation**

Create `.../social/ContextSteering.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;

/**
 * Context steering for one herd member (spec: redesign, "Context steering"). The caller scores 12 headings
 * round the circle: it marks headings that are blocked (masks: the clearance box does not fit 1 or 2 blocks
 * ahead, a hazard or a drop, or a member's hard minimum within 10 ticks) and adds penalties (danger). This
 * class adds interest (how well a heading points at the desired direction, minus a turn cost), keeps the
 * previous heading unless another is clearly better (hysteresis), and stops when nothing beats standing
 * still. Heading {@code i} faces Minecraft yaw {@code i x 30} degrees, direction {@code (-sin yaw, cos yaw)}.
 * Pure arithmetic; {@link Dinosaur#MAX_TURN_PER_TICK} is a compile-time constant.
 */
final class ContextSteering {

    static final int HEADINGS = 12;
    /** {@link #pick} result for "stand still". */
    static final int STOP = -1;
    static final double STEP_DEGREES = 360D / HEADINGS;
    /** Interest lost per degree of turn beyond one tick's turn (the body turns 10 degrees per tick). */
    static final double TURN_COST_PER_DEGREE = 0.002D;
    static final double MAX_TURN_DEGREES = Dinosaur.MAX_TURN_PER_TICK;
    /** A new heading must beat the previous one by this much (spec: 0.15). */
    static final double HYSTERESIS = 0.15D;
    /**
     * Score of standing still. Walking sideways round an obstacle (interest about -0.16) beats it; turning
     * back (interest below -0.5) does not, so a dead end stops the walker instead of turning it round.
     */
    static final double STOP_SCORE = -0.3D;
    /** Danger when the clearance box does not fit 3 blocks ahead. */
    static final double DANGER_BLOCKED_FAR = 0.5D;
    /** Danger when a member stands within the graze spacing of the first step. */
    static final double DANGER_NEAR_MEMBER = 0.3D;
    /** Inside this distance from its target a walker slows down: about the turning radius at 10 degrees per tick. */
    static final double SLOW_RADIUS = 3.0D;
    static final double MIN_ARRIVAL_FACTOR = 0.3D;
    /** A walker slows when the nearest obstacle or member on its heading is closer than this. */
    static final double FREE_SLOW_DISTANCE = 3.0D;
    static final double MIN_FREE_FACTOR = 0.4D;

    private static final double[] DIR_X = new double[HEADINGS];
    private static final double[] DIR_Z = new double[HEADINGS];

    static {
        for (int heading = 0; heading < HEADINGS; heading++) {
            double yaw = Math.toRadians(heading * STEP_DEGREES);
            DIR_X[heading] = -Math.sin(yaw);
            DIR_Z[heading] = Math.cos(yaw);
        }
    }

    private ContextSteering() {
    }

    static double dirX(int heading) {
        return DIR_X[heading];
    }

    static double dirZ(int heading) {
        return DIR_Z[heading];
    }

    static float yawOf(int heading) {
        return (float) (heading * STEP_DEGREES);
    }

    /** An angle in degrees, wrapped into [-180, 180). */
    static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360D;
        if (wrapped >= 180D) {
            wrapped -= 360D;
        }
        if (wrapped < -180D) {
            wrapped += 360D;
        }
        return wrapped;
    }

    /** Cosine between the heading and the desired direction, minus the turn cost beyond one tick's turn. */
    static double interest(int heading, double desiredX, double desiredZ, float currentYaw) {
        double length = Math.sqrt(desiredX * desiredX + desiredZ * desiredZ);
        if (length < 1.0E-9D) {
            return 0D;
        }
        double cosine = (DIR_X[heading] * desiredX + DIR_Z[heading] * desiredZ) / length;
        double turn = Math.abs(wrapDegrees(heading * STEP_DEGREES - currentYaw));
        return cosine - TURN_COST_PER_DEGREE * Math.max(0D, turn - MAX_TURN_DEGREES);
    }

    /**
     * The heading to walk, or {@link #STOP}.
     *
     * @param previous the heading picked last time, or {@link #STOP}
     * @param masked   per heading: removed from the choice
     * @param danger   per heading: subtracted from the interest
     */
    static int pick(double desiredX, double desiredZ, float currentYaw, int previous, boolean[] masked, double[] danger) {
        int best = STOP;
        double bestScore = STOP_SCORE;
        for (int heading = 0; heading < HEADINGS; heading++) {
            if (masked[heading]) {
                continue;
            }
            double score = interest(heading, desiredX, desiredZ, currentYaw) - danger[heading];
            if (score > bestScore) {
                bestScore = score;
                best = heading;
            }
        }
        if (best != STOP && previous >= 0 && previous < HEADINGS && previous != best && !masked[previous]) {
            double keep = interest(previous, desiredX, desiredZ, currentYaw) - danger[previous];
            if (keep > STOP_SCORE && keep >= bestScore - HYSTERESIS) {
                return previous;
            }
        }
        return best;
    }

    /**
     * Fraction of the wanted speed: slower within {@link #SLOW_RADIUS} of the target (never below
     * {@link #MIN_ARRIVAL_FACTOR}) and slower as the nearest obstacle on the heading gets close (never below
     * {@link #MIN_FREE_FACTOR}).
     */
    static double speedFactor(double distanceToTarget, double freeDistance) {
        double arrival = distanceToTarget >= SLOW_RADIUS ? 1D : Math.max(MIN_ARRIVAL_FACTOR, distanceToTarget / SLOW_RADIUS);
        double free = freeDistance >= FREE_SLOW_DISTANCE ? 1D : Math.max(MIN_FREE_FACTOR, freeDistance / FREE_SLOW_DISTANCE);
        return arrival * free;
    }

    /** The heading closest to a yaw. */
    static int nearestHeading(float yaw) {
        return Math.floorMod((int) Math.round(yaw / STEP_DEGREES), HEADINGS);
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: every `ContextSteeringTest` line prints `PASS`; last line `61 run, 0 failed`.

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`
Expected: `ContextSteering.java` and `ContextSteeringTest.java` are new; nothing is staged.

### Task 3: Hard minimum, crowding prediction and the member snapshot

The spacing rules that keep bodies apart by movement (spec "Spacing and stacking", lead decision 4) and the per-tick snapshot every member's steering reads (`HerdBodies`). "Who yields": walking members that outrank the walker are predicted along their velocity; everyone else counts as standing, so the lower rank waits and walkers go round standing members.

**Files:**
- Modify: `.../social/HerdSpacing.java` (add constants and methods after `personalSpace`)
- Create: `.../social/HerdBodies.java`
- Test: `test/.../social/HerdSpacingTest.java` (add 7 tests), `test/.../social/HerdBodiesTest.java` (new)

**Interfaces:**
- Consumes: `HerdSpacing.personalSpace(double, double)`.
- Produces:
  - `HerdSpacing`: `static final double RESTING_GAP = 0.5D`; `static double hardMinimum(double width, double otherWidth, boolean eitherResting)`; `public static double hardMinimumOf(Dinosaur a, Dinosaur b)`; `static boolean wouldCrowd(double x, double z, double vx, double vz, double otherX, double otherZ, double otherVx, double otherVz, double minimum, int ticks)`; `static boolean stepCrowds(double x, double z, double aheadX, double aheadZ, double otherX, double otherZ, double minimum)`.
  - `HerdBodies` (package-private, pure): `static final int PREDICT_TICKS = 10`; `long tick()`; `int count()`; `void begin(long tick)`; `void add(UUID id, double x, double z, double vx, double vz, double width, boolean resting, boolean walking, int rank)`; `int indexOf(UUID id)`; `UUID id(int i)`; `double x(int i)`; `double z(int i)`; `boolean walking(int i)`; `boolean resting(int i)`; `double minimum(int a, int b)`; `boolean outranks(int a, int b)`; `boolean wouldCrowd(int self, double vx, double vz)`; `int blockerAt(int self, double aheadX, double aheadZ)`; `double freeDistance(int self, double dirX, double dirZ, double cap)`; `boolean anyWithin(int self, double x, double z, double spacing)`.

- [ ] **Step 1: Write the failing tests**

Add these tests inside `HerdSpacingTest` (after `personalSpaceIsHalfOfEachWidthPlusTheGap`):

```java
    @Test
    void hardMinimumIsThePersonalSpaceWhileMoving() {
        assertEquals(3D, HerdSpacing.hardMinimum(2D, 2D, false), EPSILON);
    }

    @Test
    void aRestingBodyMayBeCloser() {
        assertEquals(2D + HerdSpacing.RESTING_GAP, HerdSpacing.hardMinimum(2D, 2D, true), EPSILON);
    }

    @Test
    void headingStraightAtAStandingBodyCrowds() {
        assertTrue(HerdSpacing.wouldCrowd(0D, 0D, 0D, 0.5D, 0D, 6D, 0D, 0D, 3D, 10));
    }

    @Test
    void passingWideDoesNotCrowd() {
        assertFalse(HerdSpacing.wouldCrowd(0D, 0D, 0D, 0.5D, 4D, 3D, 0D, 0D, 3D, 10));
    }

    @Test
    void steppingAwayWhenAlreadyTooCloseIsAllowed() {
        assertFalse(HerdSpacing.wouldCrowd(0D, 0D, -0.3D, 0D, 1D, 0D, 0D, 0D, 3D, 10));
        assertTrue(HerdSpacing.wouldCrowd(0D, 0D, 0.3D, 0D, 1D, 0D, 0D, 0D, 3D, 10));
    }

    @Test
    void standingStillNeverCrowds() {
        assertFalse(HerdSpacing.wouldCrowd(0D, 0D, 0D, 0D, 1D, 0D, 0D, 0D, 3D, 10));
    }

    @Test
    void stepCrowdsOnlyWhenGettingCloser() {
        assertTrue(HerdSpacing.stepCrowds(0D, 0D, 0D, 0.6D, 0D, 2.5D, 3D));
        assertFalse(HerdSpacing.stepCrowds(0D, 0D, 0D, -0.6D, 0D, 2.5D, 3D));
        assertFalse(HerdSpacing.stepCrowds(0D, 0D, 0D, 0.6D, 0D, 10D, 3D));
    }
```

and change the imports at the top of `HerdSpacingTest` to:

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
```

Create `test/.../social/HerdBodiesTest.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdBodiesTest {

    private static final double EPSILON = 1.0E-9D;
    private static final double WIDTH = 2D;

    private static UUID id(long n) {
        return new UUID(0L, n);
    }

    private static HerdBodies bodies() {
        HerdBodies bodies = new HerdBodies();
        bodies.begin(1L);
        return bodies;
    }

    @Test
    void beginEmptiesTheSnapshot() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, 0D, 0D, WIDTH, false, false, 0);
        assertEquals(1, bodies.count());
        bodies.begin(2L);
        assertEquals(0, bodies.count());
        assertEquals(-1, bodies.indexOf(id(1)));
        assertEquals(2L, bodies.tick());
    }

    @Test
    void growsPastItsFirstSize() {
        HerdBodies bodies = bodies();
        for (int i = 0; i < 20; i++) {
            bodies.add(id(i), i, 0D, 0D, 0D, WIDTH, false, false, i);
        }
        assertEquals(19, bodies.indexOf(id(19)));
        assertEquals(19D, bodies.x(19), EPSILON);
    }

    @Test
    void outranksByRankThenId() {
        HerdBodies bodies = bodies();
        bodies.add(id(5), 0D, 0D, 0D, 0D, WIDTH, false, false, 0);
        bodies.add(id(1), 0D, 0D, 0D, 0D, WIDTH, false, false, 1);
        bodies.add(id(2), 0D, 0D, 0D, 0D, WIDTH, false, false, 1);
        assertTrue(bodies.outranks(0, 1));
        assertFalse(bodies.outranks(1, 0));
        assertTrue(bodies.outranks(1, 2));
    }

    @Test
    void standingMemberAheadMasksTheWalker() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, 0D, 0D, WIDTH, false, false, 1);
        bodies.add(id(2), 0D, 4D, 0D, 0D, WIDTH, false, false, 2);
        assertTrue(bodies.wouldCrowd(0, 0D, 0.5D));
        assertFalse(bodies.wouldCrowd(0, 0D, -0.5D));
    }

    @Test
    void walkerYieldsToAHigherRankWalkerCrossingItsPath() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, 0D, 0.2D, WIDTH, false, true, 2);
        bodies.add(id(2), -3.5D, 2D, 0.4D, 0D, WIDTH, false, true, 1);
        assertTrue(bodies.wouldCrowd(0, 0D, 0.2D), "the higher rank's path must be kept clear");
    }

    @Test
    void lowerRankWalkerCountsAsStanding() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, 0D, 0.2D, WIDTH, false, true, 1);
        bodies.add(id(2), -3.5D, 2D, 0.4D, 0D, WIDTH, false, true, 2);
        assertFalse(bodies.wouldCrowd(0, 0D, 0.2D), "the lower rank yields, so it counts as standing");
    }

    @Test
    void movingAwayFromAStackedMemberIsAllowed() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, 0D, 0D, WIDTH, false, false, 2);
        bodies.add(id(2), 0.5D, 0D, 0D, 0D, WIDTH, false, false, 1);
        assertFalse(bodies.wouldCrowd(0, -0.3D, 0D));
        assertTrue(bodies.wouldCrowd(0, 0.3D, 0D));
    }

    @Test
    void blockerAtFindsTheMemberAhead() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, 0D, 0D, WIDTH, false, false, 0);
        bodies.add(id(2), 0D, 2.8D, 0D, 0D, WIDTH, false, false, 1);
        assertEquals(1, bodies.blockerAt(0, 0D, 0.6D));
        assertEquals(-1, bodies.blockerAt(0, 0D, -0.6D));
    }

    @Test
    void freeDistanceMeasuresToTheMemberAhead() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, 0D, 0D, WIDTH, false, false, 0);
        bodies.add(id(2), 0D, 5D, 0D, 0D, WIDTH, false, false, 1);
        assertEquals(2D, bodies.freeDistance(0, 0D, 1D, 10D), EPSILON);
        assertEquals(10D, bodies.freeDistance(0, 1D, 0D, 10D), EPSILON);
    }

    @Test
    void anyWithinSeesMembersInsideTheSpacing() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, 0D, 0D, WIDTH, false, false, 0);
        bodies.add(id(2), 0D, 6D, 0D, 0D, WIDTH, false, false, 1);
        assertTrue(bodies.anyWithin(0, 0D, 2D, 5D));
        assertFalse(bodies.anyWithin(0, 0D, 0D, 5D));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors, `cannot find symbol ... hardMinimum`, `... HerdBodies`.

- [ ] **Step 3: Add the spacing rules to `HerdSpacing`**

In `.../social/HerdSpacing.java`, add after the `GAP` constant:

```java
    /** Gap kept to a resting body instead of {@link #GAP} (lead decision 4: resting members may be closer; tune). */
    static final double RESTING_GAP = 0.5D;
```

and add after `personalSpace`:

```java
    /**
     * Centre-to-centre distance no member enters by its own movement (lead decision 4): the personal space
     * while both move or stand, a smaller gap ({@link #RESTING_GAP}) when either body rests.
     */
    static double hardMinimum(double width, double otherWidth, boolean eitherResting) {
        return eitherResting ? (width + otherWidth) / 2D + RESTING_GAP : personalSpace(width, otherWidth);
    }

    /** {@link #hardMinimum} for two animals. Public for GameTests. */
    public static double hardMinimumOf(Dinosaur a, Dinosaur b) {
        return hardMinimum(a.getBbWidth(), b.getBbWidth(), a.isResting() || b.isResting());
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
```

- [ ] **Step 4: Create `HerdBodies`**

Create `.../social/HerdBodies.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.Arrays;
import java.util.UUID;

/**
 * The present members of one herd as plain numbers: position, velocity per tick, body width, resting,
 * walking and rank (0 = the leader). {@code GroupManager.bodiesOf} refills it once per game tick, so the
 * steering of 20 members reads 20 positions instead of resolving 20 x 20 entities. Pure: no Minecraft
 * classes. Server thread only.
 * <p>
 * Spec "Who yields": walking members that outrank the asker are predicted along their velocity; every
 * other member counts as standing. So the lower rank waits for the higher, and walkers go round standing
 * members.
 */
final class HerdBodies {

    /** Masks look this many ticks ahead (spec: "within 10 ticks"). */
    static final int PREDICT_TICKS = 10;
    private static final int INITIAL_SIZE = 8;

    private long tick = Long.MIN_VALUE;
    private int count;
    private UUID[] ids = new UUID[INITIAL_SIZE];
    private double[] xs = new double[INITIAL_SIZE];
    private double[] zs = new double[INITIAL_SIZE];
    private double[] vxs = new double[INITIAL_SIZE];
    private double[] vzs = new double[INITIAL_SIZE];
    private double[] widths = new double[INITIAL_SIZE];
    private boolean[] resting = new boolean[INITIAL_SIZE];
    private boolean[] walking = new boolean[INITIAL_SIZE];
    private int[] ranks = new int[INITIAL_SIZE];

    long tick() {
        return this.tick;
    }

    int count() {
        return this.count;
    }

    /** Starts a new snapshot for {@code tick}; the previous members are dropped. */
    void begin(long tick) {
        this.tick = tick;
        this.count = 0;
    }

    void add(UUID id, double x, double z, double vx, double vz, double width, boolean resting, boolean walking, int rank) {
        if (this.count == this.ids.length) {
            grow();
        }
        int i = this.count++;
        this.ids[i] = id;
        this.xs[i] = x;
        this.zs[i] = z;
        this.vxs[i] = vx;
        this.vzs[i] = vz;
        this.widths[i] = width;
        this.resting[i] = resting;
        this.walking[i] = walking;
        this.ranks[i] = rank;
    }

    /** Index of the member, or -1. */
    int indexOf(UUID id) {
        for (int i = 0; i < this.count; i++) {
            if (this.ids[i].equals(id)) {
                return i;
            }
        }
        return -1;
    }

    UUID id(int i) {
        return this.ids[i];
    }

    double x(int i) {
        return this.xs[i];
    }

    double z(int i) {
        return this.zs[i];
    }

    boolean walking(int i) {
        return this.walking[i];
    }

    boolean resting(int i) {
        return this.resting[i];
    }

    /** The hard minimum between two members. */
    double minimum(int a, int b) {
        return HerdSpacing.hardMinimum(this.widths[a], this.widths[b], this.resting[a] || this.resting[b]);
    }

    /** {@code true} if member a goes first: lower rank number, then the lower UUID. */
    boolean outranks(int a, int b) {
        return this.ranks[a] < this.ranks[b] || this.ranks[a] == this.ranks[b] && this.ids[a].compareTo(this.ids[b]) < 0;
    }

    /**
     * Mask test: would {@code self}, moving (vx, vz) per tick, come inside any member's hard minimum within
     * {@link #PREDICT_TICKS}? Walking members that outrank {@code self} move along their velocity; all
     * others count as standing.
     */
    boolean wouldCrowd(int self, double vx, double vz) {
        for (int i = 0; i < this.count; i++) {
            if (i == self) {
                continue;
            }
            boolean moving = this.walking[i] && outranks(i, self);
            if (HerdSpacing.wouldCrowd(this.xs[self], this.zs[self], vx, vz, this.xs[i], this.zs[i],
                    moving ? this.vxs[i] : 0D, moving ? this.vzs[i] : 0D, minimum(self, i), PREDICT_TICKS)) {
                return true;
            }
        }
        return false;
    }

    /** Guard test: the member whose hard minimum the point (aheadX, aheadZ) enters while getting closer, or -1. */
    int blockerAt(int self, double aheadX, double aheadZ) {
        for (int i = 0; i < this.count; i++) {
            if (i != self && HerdSpacing.stepCrowds(this.xs[self], this.zs[self], aheadX, aheadZ, this.xs[i], this.zs[i], minimum(self, i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * How far {@code self} can walk along the unit direction (dirX, dirZ) before it reaches a member's hard
     * minimum, at most {@code cap}. Members behind or beside the line are ignored.
     */
    double freeDistance(int self, double dirX, double dirZ, double cap) {
        double free = cap;
        for (int i = 0; i < this.count; i++) {
            if (i == self) {
                continue;
            }
            double rx = this.xs[i] - this.xs[self];
            double rz = this.zs[i] - this.zs[self];
            double along = rx * dirX + rz * dirZ;
            if (along <= 0D) {
                continue;
            }
            double minimum = minimum(self, i);
            double lateralSqr = rx * rx + rz * rz - along * along;
            if (lateralSqr >= minimum * minimum) {
                continue;
            }
            free = Math.min(free, Math.max(0D, along - Math.sqrt(minimum * minimum - lateralSqr)));
        }
        return free;
    }

    /** {@code true} if any member other than {@code self} stands within {@code spacing} of (x, z). */
    boolean anyWithin(int self, double x, double z, double spacing) {
        for (int i = 0; i < this.count; i++) {
            if (i == self) {
                continue;
            }
            double dx = this.xs[i] - x;
            double dz = this.zs[i] - z;
            if (dx * dx + dz * dz < spacing * spacing) {
                return true;
            }
        }
        return false;
    }

    private void grow() {
        int size = this.ids.length * 2;
        this.ids = Arrays.copyOf(this.ids, size);
        this.xs = Arrays.copyOf(this.xs, size);
        this.zs = Arrays.copyOf(this.zs, size);
        this.vxs = Arrays.copyOf(this.vxs, size);
        this.vzs = Arrays.copyOf(this.vzs, size);
        this.widths = Arrays.copyOf(this.widths, size);
        this.resting = Arrays.copyOf(this.resting, size);
        this.walking = Arrays.copyOf(this.walking, size);
        this.ranks = Arrays.copyOf(this.ranks, size);
    }
}
```

- [ ] **Step 5: Run the tests to verify they pass**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: all `HerdSpacingTest` and `HerdBodiesTest` lines `PASS`; last line `78 run, 0 failed` (61 + 7 + 10).

- [ ] **Step 6: Checkpoint (no commit)**

Run: `git status --short`
Expected: `HerdBodies.java` and `HerdBodiesTest.java` new; `HerdSpacing.java` and its test modified; nothing staged.

### Task 4: Herd area and spot choice

Spec "Graze": the new radius `R = g x sqrt(N / pi) + w / 2` with graze spacing `g` = personal space + 2, uniform sampling inside the area, and spot choice: the roomiest candidate that is farther than `g` from every reserved spot and passes a reach predicate (the block checks of Task 10 plug in there). The weighted `anchor` is deleted (spec: "Delete the weighted anchor").

**Files:**
- Modify: `.../social/HerdArea.java` (whole file)
- Test: `test/.../social/HerdAreaTest.java` (whole file)

**Interfaces:**
- Consumes: nothing.
- Produces (package-private, pure): `int SPOT_CANDIDATES = 8`; `double GRAZE_EXTRA = 2.0`; `record Point(double x, double z)`; `double grazeSpacing(double personalSpace)`; `double radius(int herdSize, double grazeSpacing, double bodyWidth)`; `boolean inside(double centreX, double centreZ, double radius, double x, double z)`; `Point sample(double centreX, double centreZ, double radius, double angleUnit, double distanceUnit)`; `Point sampleRing(double fromX, double fromZ, double minDistance, double maxDistance, double angleUnit, double distanceUnit)`; `double roominess(Point point, List<Point> occupied)`; `int roomiest(List<Point> candidates, List<Point> occupied)`; `int pickSpot(List<Point> candidates, List<Point> reserved, double spacing, Predicate<Point> usable)`.

- [ ] **Step 1: Write the failing test**

Replace the whole file `test/.../social/HerdAreaTest.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdAreaTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void radiusGivesEveryMemberAGrazeSpacingSquare() {
        assertEquals(5D * Math.sqrt(20D / Math.PI) + 1D, HerdArea.radius(20, 5D, 2D), EPSILON);
        assertEquals(13.6D, HerdArea.radius(20, 5D, 2D), 0.05D);
        assertEquals(7.3D, HerdArea.radius(5, 5D, 2D), 0.05D);
    }

    @Test
    void grazeSpacingIsPersonalSpacePlusTwo() {
        assertEquals(5D, HerdArea.grazeSpacing(3D), EPSILON);
    }

    @Test
    void insideIncludesTheEdge() {
        assertTrue(HerdArea.inside(0D, 0D, 5D, 3D, 4D));
        assertFalse(HerdArea.inside(0D, 0D, 5D, 3D, 4.01D));
    }

    @Test
    void sampleStaysInsideTheRadius() {
        for (double angle = 0D; angle <= 1D; angle += 0.1D) {
            for (double distance = 0D; distance <= 1D; distance += 0.1D) {
                HerdArea.Point point = HerdArea.sample(10D, -4D, 7D, angle, distance);
                assertTrue(HerdArea.inside(10D, -4D, 7D + EPSILON, point.x(), point.z()), "outside: " + point);
            }
        }
    }

    @Test
    void sampleRingKeepsTheDistanceBand() {
        for (double angle = 0D; angle <= 1D; angle += 0.1D) {
            for (double distance = 0D; distance <= 1D; distance += 0.1D) {
                HerdArea.Point point = HerdArea.sampleRing(0D, 0D, 3D, 8D, angle, distance);
                double length = Math.sqrt(point.x() * point.x() + point.z() * point.z());
                assertTrue(length >= 3D - EPSILON && length <= 8D + EPSILON, "out of the band: " + length);
            }
        }
    }

    @Test
    void roomiestPicksTheCandidateFarthestFromEveryone() {
        List<HerdArea.Point> occupied = List.of(new HerdArea.Point(0D, 0D), new HerdArea.Point(10D, 0D));
        List<HerdArea.Point> candidates = List.of(new HerdArea.Point(1D, 0D), new HerdArea.Point(5D, 0D), new HerdArea.Point(9D, 0D));
        assertEquals(1, HerdArea.roomiest(candidates, occupied));
    }

    @Test
    void roomiestOfNoCandidatesIsMinusOne() {
        assertEquals(-1, HerdArea.roomiest(List.of(), List.of(new HerdArea.Point(0D, 0D))));
    }

    @Test
    void withNobodyElseTheFirstCandidateWins() {
        assertEquals(0, HerdArea.roomiest(List.of(new HerdArea.Point(1D, 1D), new HerdArea.Point(2D, 2D)), List.of()));
    }

    @Test
    void pickSpotSkipsCandidatesTooCloseToAReservation() {
        List<HerdArea.Point> candidates = List.of(new HerdArea.Point(3D, 0D), new HerdArea.Point(6D, 0D));
        assertEquals(1, HerdArea.pickSpot(candidates, List.of(new HerdArea.Point(0D, 0D)), 5D, point -> true));
    }

    @Test
    void pickSpotTriesTheRoomiestFirstAndStopsAtTheFirstUsable() {
        List<HerdArea.Point> candidates = List.of(new HerdArea.Point(6D, 0D), new HerdArea.Point(10D, 0D), new HerdArea.Point(20D, 0D));
        int[] calls = new int[1];
        int picked = HerdArea.pickSpot(candidates, List.of(new HerdArea.Point(0D, 0D)), 5D, point -> {
            calls[0]++;
            return point.x() < 15D;
        });
        assertEquals(1, picked);
        assertEquals(2, calls[0], "the 20-block candidate is tried first and refused, then the 10-block one");
    }

    @Test
    void pickSpotWithNothingUsableIsMinusOne() {
        List<HerdArea.Point> candidates = List.of(new HerdArea.Point(6D, 0D), new HerdArea.Point(10D, 0D));
        assertEquals(-1, HerdArea.pickSpot(candidates, List.of(), 5D, point -> false));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors, `cannot find symbol ... grazeSpacing`, `... pickSpot`.

- [ ] **Step 3: Write the implementation**

Replace the whole file `.../social/HerdArea.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.List;
import java.util.function.Predicate;

/**
 * The grazing area of a herd (spec: redesign, "Graze"): a fixed centre held by {@link DinoGroup}, a radius
 * that gives every member a graze-spacing square, random sampling, and the spot choice. Pure arithmetic on
 * horizontal points; the block checks come in as a predicate.
 */
final class HerdArea {

    /** A member samples this many candidates when it picks a graze spot (spec: 8). */
    static final int SPOT_CANDIDATES = 8;
    /** Graze spacing = personal space + this (spec: 5 for Gallimimus). */
    static final double GRAZE_EXTRA = 2.0D;

    /** A horizontal position. */
    record Point(double x, double z) {
    }

    private HerdArea() {
    }

    static double grazeSpacing(double personalSpace) {
        return personalSpace + GRAZE_EXTRA;
    }

    /** Herd radius: {@code herdSize} squares of side {@code grazeSpacing}, plus half a body (spec: 7.3 for 5, 13.6 for 20). */
    static double radius(int herdSize, double grazeSpacing, double bodyWidth) {
        return grazeSpacing * Math.sqrt(herdSize / Math.PI) + bodyWidth / 2D;
    }

    static boolean inside(double centreX, double centreZ, double radius, double x, double z) {
        double dx = x - centreX;
        double dz = z - centreZ;
        return dx * dx + dz * dz <= radius * radius;
    }

    /** A point uniform over the disc; both units in [0, 1]. */
    static Point sample(double centreX, double centreZ, double radius, double angleUnit, double distanceUnit) {
        double angle = angleUnit * 2D * Math.PI;
        double distance = radius * Math.sqrt(distanceUnit);
        return new Point(centreX + Math.cos(angle) * distance, centreZ + Math.sin(angle) * distance);
    }

    /** A point uniform over the ring between {@code minDistance} and {@code maxDistance}; both units in [0, 1]. */
    static Point sampleRing(double fromX, double fromZ, double minDistance, double maxDistance, double angleUnit, double distanceUnit) {
        double angle = angleUnit * 2D * Math.PI;
        double distance = Math.sqrt(minDistance * minDistance + (maxDistance * maxDistance - minDistance * minDistance) * distanceUnit);
        return new Point(fromX + Math.cos(angle) * distance, fromZ + Math.sin(angle) * distance);
    }

    /** Distance from the point to the nearest occupied point; {@link Double#MAX_VALUE} if none. */
    static double roominess(Point point, List<Point> occupied) {
        double nearest = Double.MAX_VALUE;
        for (Point other : occupied) {
            double dx = point.x() - other.x();
            double dz = point.z() - other.z();
            nearest = Math.min(nearest, dx * dx + dz * dz);
        }
        return nearest == Double.MAX_VALUE ? nearest : Math.sqrt(nearest);
    }

    /** Index of the roomiest candidate, the first on a tie, or {@code -1} if there is none. */
    static int roomiest(List<Point> candidates, List<Point> occupied) {
        int best = -1;
        double bestRoom = -1D;
        for (int i = 0; i < candidates.size(); i++) {
            double room = roominess(candidates.get(i), occupied);
            if (room > bestRoom) {
                bestRoom = room;
                best = i;
            }
        }
        return best;
    }

    /**
     * Spec spot choice: the roomiest candidate that is farther than {@code spacing} from every reserved spot
     * and passes {@code usable} (fit, ground, straight-line reach). Candidates are tried roomiest first, so
     * the costly predicate runs only until one passes. {@code -1} if none qualifies.
     */
    static int pickSpot(List<Point> candidates, List<Point> reserved, double spacing, Predicate<Point> usable) {
        int count = candidates.size();
        double[] room = new double[count];
        for (int i = 0; i < count; i++) {
            room[i] = roominess(candidates.get(i), reserved);
        }
        boolean[] tried = new boolean[count];
        for (int round = 0; round < count; round++) {
            int best = -1;
            for (int i = 0; i < count; i++) {
                if (!tried[i] && (best < 0 || room[i] > room[best])) {
                    best = i;
                }
            }
            tried[best] = true;
            if (room[best] <= spacing) {
                return -1;
            }
            if (usable.test(candidates.get(best))) {
                return best;
            }
        }
        return -1;
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: every `HerdAreaTest` line `PASS`; last line `83 run, 0 failed` (78 - 6 old + 11).

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`
Expected: `HerdArea.java` and `HerdAreaTest.java` modified; nothing staged.

### Task 5: Graze schedule and progress monitor

Two per-follower timers. `GrazeSchedule` is lead decision 6 and the spec's "Spot choice / Stay / Invalid spot": stagger 0-60 ticks on entering GRAZE, hold a spot 600-1800 ticks, then a 50% chance of a short move, retry after 100 ticks when no spot is found. `ProgressMonitor` is the spec's "Stuck": less than 0.5 blocks of progress in 40 ticks while not waiting.

**Files:**
- Create: `.../social/GrazeSchedule.java`, `.../social/ProgressMonitor.java`
- Test: `test/.../social/GrazeScheduleTest.java`, `test/.../social/ProgressMonitorTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces (package-private, pure):
  - `GrazeSchedule`: constants `ENTER_STAGGER_TICKS = 60`, `STAY_MIN_TICKS = 600`, `STAY_SPREAD_TICKS = 1200`, `MOVE_CHANCE = 0.5`, `RETRY_TICKS = 100`, `SHORT_MOVE_RANGE = 6.0`; `enum Action { HOLD, PICK_ANY, PICK_NEAR }`; `void enter(long now, double unit)`; `Action decide(long now, boolean hasSpot, boolean spotValid, DoubleSupplier random)`; `void spotTaken(long now, DoubleSupplier random)`; `void noSpot(long now)`; `void repickNow(long now)`; `long nextDecision()`.
  - `ProgressMonitor`: constants `WINDOW_TICKS = 40`, `MIN_PROGRESS = 0.5`; `void reset(double measure, long now)` (NaN measure: the next update sets the baseline); `boolean update(double measure, boolean waiting, long now)` (true once per stuck window); `long lastProgressTick()`.

- [ ] **Step 1: Write the failing tests**

Create `test/.../social/GrazeScheduleTest.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class GrazeScheduleTest {

    /** Returns the given values in order, then repeats the last one. */
    private static DoubleSupplier fixed(double... values) {
        int[] next = new int[1];
        return () -> values[Math.min(next[0]++, values.length - 1)];
    }

    @Test
    void enteringStaggersTheFirstPick() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.enter(100L, 0.5D);
        assertSame(GrazeSchedule.Action.HOLD, schedule.decide(129L, false, false, fixed(0D)));
        assertSame(GrazeSchedule.Action.PICK_ANY, schedule.decide(130L, false, false, fixed(0D)));
    }

    @Test
    void aSpotIsKeptThirtyToNinetySeconds() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.spotTaken(0L, fixed(0D));
        assertEquals(600L, schedule.nextDecision());
        schedule.spotTaken(0L, fixed(0.999D));
        assertEquals(1798L, schedule.nextDecision());
    }

    @Test
    void afterTheStayHalfTheMembersMoveNearby() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.spotTaken(0L, fixed(0D));
        assertSame(GrazeSchedule.Action.HOLD, schedule.decide(599L, true, true, fixed(0.2D)));
        assertSame(GrazeSchedule.Action.PICK_NEAR, schedule.decide(600L, true, true, fixed(0.2D)));
    }

    @Test
    void theOtherHalfStayAnotherPeriod() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.spotTaken(0L, fixed(0D));
        assertSame(GrazeSchedule.Action.HOLD, schedule.decide(600L, true, true, fixed(0.7D, 0D)));
        assertEquals(1200L, schedule.nextDecision());
    }

    @Test
    void anInvalidSpotIsReplacedAtOnce() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.spotTaken(0L, fixed(0D));
        assertSame(GrazeSchedule.Action.PICK_ANY, schedule.decide(10L, true, false, fixed(0.9D)));
    }

    @Test
    void noSpotTriesAgainAfter100Ticks() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.noSpot(50L);
        assertSame(GrazeSchedule.Action.HOLD, schedule.decide(149L, false, false, fixed(0D)));
        assertSame(GrazeSchedule.Action.PICK_ANY, schedule.decide(150L, false, false, fixed(0D)));
    }

    @Test
    void repickNowPicksOnTheNextDecision() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.spotTaken(0L, fixed(0D));
        schedule.repickNow(10L);
        assertSame(GrazeSchedule.Action.PICK_ANY, schedule.decide(10L, false, false, fixed(0.9D)));
    }
}
```

Create `test/.../social/ProgressMonitorTest.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressMonitorTest {

    @Test
    void firstUpdateOnlySetsTheBaseline() {
        ProgressMonitor monitor = new ProgressMonitor();
        monitor.reset(Double.NaN, 0L);
        assertFalse(monitor.update(0D, false, 7L));
        assertEquals(7L, monitor.lastProgressTick());
    }

    @Test
    void steadyProgressNeverStalls() {
        ProgressMonitor monitor = new ProgressMonitor();
        monitor.reset(Double.NaN, 0L);
        for (long tick = 0L; tick <= 200L; tick++) {
            assertFalse(monitor.update(tick * 0.1D, false, tick), "stalled at tick " + tick);
        }
    }

    @Test
    void lessThanHalfABlockIn40TicksIsStuck() {
        ProgressMonitor monitor = new ProgressMonitor();
        monitor.reset(Double.NaN, 0L);
        monitor.update(0D, false, 0L);
        for (long tick = 1L; tick < ProgressMonitor.WINDOW_TICKS; tick++) {
            assertFalse(monitor.update(0.4D, false, tick));
        }
        assertTrue(monitor.update(0.4D, false, ProgressMonitor.WINDOW_TICKS));
    }

    @Test
    void waitingIsNotStuck() {
        ProgressMonitor monitor = new ProgressMonitor();
        monitor.reset(Double.NaN, 0L);
        monitor.update(0D, false, 0L);
        for (long tick = 1L; tick <= 100L; tick++) {
            assertFalse(monitor.update(0D, true, tick));
        }
    }

    @Test
    void stuckFiresOnceThenStartsANewWindow() {
        ProgressMonitor monitor = new ProgressMonitor();
        monitor.reset(Double.NaN, 0L);
        monitor.update(0D, false, 0L);
        assertTrue(monitor.update(0D, false, 40L));
        assertFalse(monitor.update(0D, false, 41L));
        assertTrue(monitor.update(0D, false, 80L));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors, `cannot find symbol ... GrazeSchedule`, `... ProgressMonitor`.

- [ ] **Step 3: Write the implementations**

Create `.../social/GrazeSchedule.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.function.DoubleSupplier;

/**
 * When a grazing follower picks a spot (spec "Graze", lead decision 6). On entering GRAZE the first pick
 * is staggered over {@link #ENTER_STAGGER_TICKS}; a spot is held for 600 to 1800 ticks; then, with a 50%
 * chance, the member moves to a spot at most {@link #SHORT_MOVE_RANGE} away, otherwise it stays another
 * period. An invalid spot is replaced at once; with no spot found it tries again after {@link #RETRY_TICKS}.
 * During the stay the member eats, rests or idles with its animations (the behaviours that already exist).
 * Pure; one instance per follower.
 */
final class GrazeSchedule {

    static final int ENTER_STAGGER_TICKS = 60;
    static final int STAY_MIN_TICKS = 600;
    static final int STAY_SPREAD_TICKS = 1200;
    static final double MOVE_CHANCE = 0.5D;
    static final int RETRY_TICKS = 100;
    static final double SHORT_MOVE_RANGE = 6.0D;

    /** What the follower does about its spot this tick. */
    enum Action {
        /** Keep the spot (or keep waiting for the next try). */
        HOLD,
        /** Pick a spot anywhere in the herd area. */
        PICK_ANY,
        /** Pick a spot near the current one; keep the current one if none is found. */
        PICK_NEAR
    }

    private long nextDecision;

    /** The herd entered GRAZE: the first pick comes after {@code unit x ENTER_STAGGER_TICKS} ticks. */
    void enter(long now, double unit) {
        this.nextDecision = now + (long) (unit * ENTER_STAGGER_TICKS);
    }

    Action decide(long now, boolean hasSpot, boolean spotValid, DoubleSupplier random) {
        if (hasSpot && !spotValid) {
            return Action.PICK_ANY;
        }
        if (now < this.nextDecision) {
            return Action.HOLD;
        }
        if (!hasSpot) {
            return Action.PICK_ANY;
        }
        if (random.getAsDouble() < MOVE_CHANCE) {
            return Action.PICK_NEAR;
        }
        spotTaken(now, random);
        return Action.HOLD;
    }

    /** A spot was taken or kept: hold it for 600 to 1800 ticks. */
    void spotTaken(long now, DoubleSupplier random) {
        this.nextDecision = now + STAY_MIN_TICKS + (long) (random.getAsDouble() * STAY_SPREAD_TICKS);
    }

    /** No spot was found: try again after {@link #RETRY_TICKS}. */
    void noSpot(long now) {
        this.nextDecision = now + RETRY_TICKS;
    }

    /** The spot failed (two failed fallback paths): decide again now. */
    void repickNow(long now) {
        this.nextDecision = now;
    }

    long nextDecision() {
        return this.nextDecision;
    }
}
```

Create `.../social/ProgressMonitor.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/**
 * Stuck detection (spec "Stuck and re-path"): stuck means less than {@link #MIN_PROGRESS} blocks of
 * progress in {@link #WINDOW_TICKS} ticks while not waiting. Progress is a number that grows as the member
 * gets on: the arc on the trail in travel, minus the distance to the spot in graze. Waiting (held back on
 * purpose, or yielding to members) restarts the window. Reports once per window. Pure; one per follower.
 */
final class ProgressMonitor {

    static final int WINDOW_TICKS = 40;
    static final double MIN_PROGRESS = 0.5D;

    private double baseline = Double.NaN;
    private long baselineTick;

    /** Starts a new window at {@code measure}; with NaN the next {@link #update} sets the baseline. */
    void reset(double measure, long now) {
        this.baseline = measure;
        this.baselineTick = now;
    }

    /** @return {@code true} if the member is stuck; the window then restarts */
    boolean update(double measure, boolean waiting, long now) {
        if (Double.isNaN(this.baseline) || waiting || measure - this.baseline >= MIN_PROGRESS) {
            reset(measure, now);
            return false;
        }
        if (now - this.baselineTick >= WINDOW_TICKS) {
            reset(measure, now);
            return true;
        }
        return false;
    }

    /** The tick of the last progress, wait or reset. */
    long lastProgressTick() {
        return this.baselineTick;
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: every new line `PASS`; last line `95 run, 0 failed` (83 + 7 + 5).

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`
Expected: the two classes and two tests are new; nothing staged.

### Task 6: Journey policy

The pure part of `LeaderJourney` (spec "Leader" table, lead decisions 2 and 3): graze moves every 400-800 ticks, roaming journeys every 2400-6000 ticks of 32-64 blocks, arrival within 3 blocks or when the path ends without progress (a vanilla walk reaches at most the follow range of 35 blocks and runs 150-249 ticks, so the leader re-sets its walk target while it gets on: Review Focus 6), settle 100 ticks, the gathering rule (75% of counted members within 2R; members farther than 48 do not count), slow then stop (speed 0, the walk target stays), wait at most 200 ticks on roaming journeys and never on urgent ones, recentre after 100 ticks outside.

**Files:**
- Create: `.../social/JourneyPolicy.java`
- Test: `test/.../social/JourneyPolicyTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces (package-private, pure): `enum Kind { ROAM, URGENT }`; `enum Gathering { GO, SLOW, STOP }`; constants `GRAZE_MOVE_MIN_TICKS = 400`, `GRAZE_MOVE_SPREAD_TICKS = 400`, `GRAZE_MOVE_MIN = 3.0`, `GRAZE_MOVE_MAX = 8.0`, `float GRAZE_MOVE_SPEED = 0.5F`, `JOURNEY_MIN_TICKS = 2400`, `JOURNEY_SPREAD_TICKS = 3600`, `JOURNEY_MIN = 32.0`, `JOURNEY_MAX = 64.0`, `float JOURNEY_SPEED = 0.7F`, `JOURNEY_ATTEMPTS = 10`, `ARRIVE_DISTANCE = 3.0`, `REISSUE_PROGRESS = 0.5`, `SETTLE_TICKS = 100`, `GATHERED_FRACTION = 0.75`, `STRAGGLER_IGNORE_DISTANCE = 48.0`, `GATHER_RADIUS_FACTOR = 2.0`, `SLOW_TICKS = 40`, `float WAIT_SLOW_SPEED = 0.3F`, `float WAIT_STOP_SPEED = 0F`, `MAX_WAIT_TICKS = 200`, `OUTSIDE_RECENTRE_TICKS = 100`; `long nextGrazeMove(long now, double unit)`; `long nextJourney(long now, double unit)`; `boolean isJourneyDistance(double distance)`; `boolean arrived(double distanceToTarget, boolean pathEndedWithoutProgress)`; `boolean isGathered(int close, int counted)`; `Gathering gathering(Kind kind, boolean gathered, int waitedTicks)`; `boolean shouldRecentre(int outsideTicks)`.

- [ ] **Step 1: Write the failing test**

Create `test/.../social/JourneyPolicyTest.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JourneyPolicyTest {

    @Test
    void grazeMovesComeEvery20To40Seconds() {
        assertEquals(1400L, JourneyPolicy.nextGrazeMove(1000L, 0D));
        long latest = JourneyPolicy.nextGrazeMove(1000L, 0.9999D);
        assertTrue(latest >= 1799L && latest <= 1800L, "was " + latest);
    }

    @Test
    void journeysComeEvery2To5Minutes() {
        assertEquals(2400L, JourneyPolicy.nextJourney(0L, 0D));
        long latest = JourneyPolicy.nextJourney(0L, 0.9999D);
        assertTrue(latest >= 5999L && latest <= 6000L, "was " + latest);
    }

    @Test
    void journeyDistanceIs32To64Blocks() {
        assertFalse(JourneyPolicy.isJourneyDistance(31.9D));
        assertTrue(JourneyPolicy.isJourneyDistance(32D));
        assertTrue(JourneyPolicy.isJourneyDistance(64D));
        assertFalse(JourneyPolicy.isJourneyDistance(64.1D));
    }

    @Test
    void arrivesWithin3BlocksOrWhenThePathEndsWithoutProgress() {
        assertTrue(JourneyPolicy.arrived(3D, false));
        assertFalse(JourneyPolicy.arrived(3.1D, false));
        assertTrue(JourneyPolicy.arrived(10D, true));
    }

    @Test
    void urgentTripsNeverWait() {
        assertSame(JourneyPolicy.Gathering.GO, JourneyPolicy.gathering(JourneyPolicy.Kind.URGENT, false, 0));
    }

    @Test
    void roamingSlowsThenStops() {
        assertSame(JourneyPolicy.Gathering.SLOW, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, false, 0));
        assertSame(JourneyPolicy.Gathering.SLOW, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, false, JourneyPolicy.SLOW_TICKS - 1));
        assertSame(JourneyPolicy.Gathering.STOP, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, false, JourneyPolicy.SLOW_TICKS));
    }

    @Test
    void gatheredHerdGoes() {
        assertSame(JourneyPolicy.Gathering.GO, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, true, 50));
    }

    @Test
    void waitingEndsAfterTenSeconds() {
        assertSame(JourneyPolicy.Gathering.STOP, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, false, 199));
        assertSame(JourneyPolicy.Gathering.GO, JourneyPolicy.gathering(JourneyPolicy.Kind.ROAM, false, 200));
    }

    @Test
    void gatheredMeansThreeQuarters() {
        assertTrue(JourneyPolicy.isGathered(3, 4));
        assertFalse(JourneyPolicy.isGathered(2, 4));
        assertTrue(JourneyPolicy.isGathered(0, 0), "nobody to wait for");
    }

    @Test
    void recentreAfterFiveSecondsOutside() {
        assertFalse(JourneyPolicy.shouldRecentre(99));
        assertTrue(JourneyPolicy.shouldRecentre(100));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors, `cannot find symbol ... JourneyPolicy`.

- [ ] **Step 3: Write the implementation**

Create `.../social/JourneyPolicy.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/**
 * The leader's timing and rules (spec "Leader", lead decisions 2 and 3). Pure: {@link LeaderJourney} and
 * {@link HerdLeading} apply it.
 * <ul>
 *   <li>Graze move every 400-800 ticks: walk 3-8 blocks to a free spot inside the herd area at speed 0.5.</li>
 *   <li>Roaming journey every 2400-6000 ticks: 32-64 blocks at speed 0.7. Needs (water, food) also start
 *       journeys: those are urgent.</li>
 *   <li>Arrival: within 3 blocks of the target, or when the path ends without progress. A vanilla walk
 *       reaches at most the follow range (35 blocks) and runs 150-249 ticks, so on a roaming journey the
 *       leader re-sets its walk target each time a walk ends while it has moved at least
 *       {@link #REISSUE_PROGRESS} since the previous end ({@code DinoGroup.pathEnded}); a walk that ends
 *       twice in one place is the arrival. Then {@link #SETTLE_TICKS} more in TRAVEL before GRAZE.</li>
 *   <li>Waiting (roaming only): fewer than 75% of the counted members within 2R of the leader: slow to 0.3
 *       for {@link #SLOW_TICKS}, then stop (speed {@link #WAIT_STOP_SPEED}; the walk target stays); at most
 *       {@link #MAX_WAIT_TICKS} per journey. Members farther than 48 blocks do not count. Urgent trips
 *       never wait.</li>
 *   <li>Recentre the herd area when the leader has stood outside it for 100 ticks.</li>
 * </ul>
 */
final class JourneyPolicy {

    /** Why the herd travels. */
    enum Kind {
        /** A roaming journey the leader chose; the leader waits for stragglers. */
        ROAM,
        /** A need (water, food) outside the herd area; the leader never waits. */
        URGENT
    }

    /** What the leader does about stragglers this tick. */
    enum Gathering {
        GO,
        SLOW,
        STOP
    }

    static final int GRAZE_MOVE_MIN_TICKS = 400;
    static final int GRAZE_MOVE_SPREAD_TICKS = 400;
    static final double GRAZE_MOVE_MIN = 3.0D;
    static final double GRAZE_MOVE_MAX = 8.0D;
    static final float GRAZE_MOVE_SPEED = 0.5F;
    static final int JOURNEY_MIN_TICKS = 2400;
    static final int JOURNEY_SPREAD_TICKS = 3600;
    static final double JOURNEY_MIN = 32.0D;
    static final double JOURNEY_MAX = 64.0D;
    static final float JOURNEY_SPEED = 0.7F;
    /** Random positions tried for a journey target before the journey is put off. */
    static final int JOURNEY_ATTEMPTS = 10;
    static final double ARRIVE_DISTANCE = 3.0D;
    /** A walk that ends at least this far from the previous end counts as progress: the walk target is set again. */
    static final double REISSUE_PROGRESS = 0.5D;
    static final int SETTLE_TICKS = 100;
    static final double GATHERED_FRACTION = 0.75D;
    static final double STRAGGLER_IGNORE_DISTANCE = 48.0D;
    /** Members within this many herd radii of the leader count as close. */
    static final double GATHER_RADIUS_FACTOR = 2.0D;
    static final int SLOW_TICKS = 40;
    static final float WAIT_SLOW_SPEED = 0.3F;
    /** Standing for stragglers: speed 0 on the navigation, so the walk behaviour keeps running (no restart cooldown). */
    static final float WAIT_STOP_SPEED = 0F;
    static final int MAX_WAIT_TICKS = 200;
    static final int OUTSIDE_RECENTRE_TICKS = 100;

    private JourneyPolicy() {
    }

    static long nextGrazeMove(long now, double unit) {
        return now + GRAZE_MOVE_MIN_TICKS + (long) (unit * GRAZE_MOVE_SPREAD_TICKS);
    }

    static long nextJourney(long now, double unit) {
        return now + JOURNEY_MIN_TICKS + (long) (unit * JOURNEY_SPREAD_TICKS);
    }

    static boolean isJourneyDistance(double distance) {
        return distance >= JOURNEY_MIN && distance <= JOURNEY_MAX;
    }

    /** @param pathEndedWithoutProgress the walk ended and the leader had not moved since the previous end */
    static boolean arrived(double distanceToTarget, boolean pathEndedWithoutProgress) {
        return distanceToTarget <= ARRIVE_DISTANCE || pathEndedWithoutProgress;
    }

    /** {@code true} if at least 75% of the counted members are close (or none are counted). */
    static boolean isGathered(int close, int counted) {
        return counted == 0 || close >= GATHERED_FRACTION * counted;
    }

    /** @param waitedTicks ticks this journey has already spent slowing or standing for stragglers */
    static Gathering gathering(Kind kind, boolean gathered, int waitedTicks) {
        if (kind == Kind.URGENT || gathered || waitedTicks >= MAX_WAIT_TICKS) {
            return Gathering.GO;
        }
        return waitedTicks < SLOW_TICKS ? Gathering.SLOW : Gathering.STOP;
    }

    static boolean shouldRecentre(int outsideTicks) {
        return outsideTicks >= OUTSIDE_RECENTRE_TICKS;
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: every `JourneyPolicyTest` line `PASS`; last line `105 run, 0 failed`.

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`
Expected: `JourneyPolicy.java` and its test new; nothing staged.

### Task 7: Leader trail by distance, with arc length and projection

Spec "Trail": the leader records a point every 1 block walked (not every 5 ticks), 128 points; each point keeps its arc length from the journey start and the side room beside it (measured once, with the clearance box, by `HerdLeading` in Task 15). Followers project themselves on the trail and read points by arc, so targets move smoothly instead of jumping every 5 ticks.

**Files:**
- Modify: `.../social/LeaderTrail.java` (whole file), `.../social/HerdLeading.java` (whole file, interim)
- Test: `test/.../social/LeaderTrailTest.java` (whole file)

**Interfaces:**
- Consumes: nothing.
- Produces (package-private, pure): `int CAPACITY = 128`; `double SPACING = 1.0`; `record TrailPoint(double x, double y, double z, double dirX, double dirZ, double arc, float roomPos, float roomNeg)` (`roomPos` is the side room toward `(-dirZ, dirX)`, `roomNeg` toward `(dirZ, -dirX)`; NaN = not measured); `int size()`; `void clear()`; `int record(double x, double y, double z)` (buffer index of the new point, or -1 if closer than `SPACING` to the newest); `double headArc()`; `double tailArc()` (both NaN when empty); `@Nullable TrailPoint newest()`; `@Nullable TrailPoint pointAt(double arc)` (clamped to the trail; one point gives direction 0); `double project(double x, double z, double hintArc, double window)` (NaN hint = whole trail); `void setRoom(int index, float roomPos, float roomNeg)`; `float minRoom(double fromArc, double toArc, boolean positive)` (the tightest room on one side over the points of every segment the arc range touches; NaN if any is unmeasured).

- [ ] **Step 1: Write the failing test**

Replace the whole file `test/.../social/LeaderTrailTest.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaderTrailTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void emptyTrailHasNoPoint() {
        LeaderTrail trail = new LeaderTrail();
        assertNull(trail.pointAt(0D));
        assertTrue(Double.isNaN(trail.project(0D, 0D, Double.NaN, 5D)));
        assertTrue(Double.isNaN(trail.headArc()));
    }

    @Test
    void recordsOnlyEveryBlockWalked() {
        LeaderTrail trail = new LeaderTrail();
        assertTrue(trail.record(0D, 64D, 0D) >= 0);
        assertEquals(-1, trail.record(0.5D, 64D, 0D));
        assertTrue(trail.record(1D, 64D, 0D) >= 0);
        assertEquals(2, trail.size());
    }

    @Test
    void arcGrowsWithTheDistanceWalked() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(3D, 64D, 0D);
        trail.record(3D, 64D, 4D);
        assertEquals(7D, trail.headArc(), EPSILON);
        assertEquals(0D, trail.tailArc(), EPSILON);
    }

    @Test
    void pointAtInterpolatesAndGivesTheDirection() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(3D, 64D, 0D);
        trail.record(3D, 66D, 4D);
        LeaderTrail.TrailPoint point = trail.pointAt(5D);
        assertEquals(3D, point.x(), EPSILON);
        assertEquals(2D, point.z(), EPSILON);
        assertEquals(65D, point.y(), EPSILON);
        assertEquals(0D, point.dirX(), EPSILON);
        assertEquals(1D, point.dirZ(), EPSILON);
        assertEquals(5D, point.arc(), EPSILON);
    }

    @Test
    void pointAtClampsToTheTrail() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(3D, 64D, 0D);
        assertEquals(3D, trail.pointAt(100D).x(), EPSILON);
        assertEquals(0D, trail.pointAt(-5D).x(), EPSILON);
    }

    @Test
    void projectFindsTheNearestArc() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(3D, 64D, 0D);
        trail.record(3D, 64D, 4D);
        assertEquals(4D, trail.project(4D, 1D, Double.NaN, 5D), EPSILON);
    }

    @Test
    void projectWithAHintIgnoresFarSegments() {
        // A U-turn: the return leg passes close to the first one.
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(10D, 64D, 0D);
        trail.record(10D, 64D, 2D);
        trail.record(0D, 64D, 2D);
        assertEquals(20D, trail.project(2D, 1.1D, Double.NaN, 5D), EPSILON);
        assertEquals(2D, trail.project(2D, 1.1D, 2D, 5D), EPSILON);
    }

    @Test
    void ringBufferKeepsTheNewest128() {
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x < 200; x++) {
            trail.record(x, 64D, 0D);
        }
        assertEquals(LeaderTrail.CAPACITY, trail.size());
        assertEquals(199D, trail.headArc(), EPSILON);
        assertEquals(72D, trail.tailArc(), EPSILON);
        assertEquals(72D, trail.pointAt(0D).x(), EPSILON);
    }

    @Test
    void sideRoomIsUnknownUntilMeasuredAndUsesTheTighterEnd() {
        LeaderTrail trail = new LeaderTrail();
        int first = trail.record(0D, 64D, 0D);
        int second = trail.record(2D, 64D, 0D);
        trail.setRoom(first, 3F, 1F);
        assertTrue(Float.isNaN(trail.pointAt(1D).roomPos()));
        trail.setRoom(second, 2F, 4F);
        assertEquals(2F, trail.pointAt(1D).roomPos(), 0F);
        assertEquals(1F, trail.pointAt(1D).roomNeg(), 0F);
    }

    @Test
    void minRoomTakesTheTightestPointInTheRange() {
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x <= 10; x++) {
            trail.setRoom(trail.record(x, 64D, 0D), x == 7 ? 1F : 5F, 5F);
        }
        assertEquals(5F, trail.minRoom(2D, 5D, true), 0F);
        assertEquals(1F, trail.minRoom(2D, 6.5D, true), 0F, "the range touches the segment that ends at the tight point");
        assertEquals(5F, trail.minRoom(8D, 10D, true), 0F);
        assertEquals(5F, trail.minRoom(2D, 6.5D, false), 0F);
        assertEquals(5F, trail.minRoom(-3D, 1D, true), 0F, "clamped to the trail");
        trail.setRoom(trail.record(11D, 64D, 0D), 5F, Float.NaN);
        assertTrue(Float.isNaN(trail.minRoom(9D, 11D, false)), "an unmeasured point makes the range unknown");
    }

    @Test
    void clearEmptiesTheTrail() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(2D, 64D, 0D);
        trail.clear();
        assertEquals(0, trail.size());
        assertNull(trail.newest());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors, `cannot find symbol ... headArc`, `... project`, `... setRoom`, `... minRoom`.

- [ ] **Step 3: Write the implementation**

Replace the whole file `.../social/LeaderTrail.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.jetbrains.annotations.Nullable;

/**
 * The points the herd leader actually walked on the current journey, one per {@link #SPACING} block, in a
 * ring buffer of {@link #CAPACITY} (spec "Trail"). Each point keeps its arc length from the journey start
 * and the side room beside it on both sides, measured once with the clearance box ({@code HerdLeading}).
 * Followers project themselves on the trail and steer to points by arc, so they inherit a route the
 * leader's same-size body has walked and their targets move smoothly. Pure.
 */
final class LeaderTrail {

    static final int CAPACITY = 128;
    /** The leader records a point every this many blocks walked. */
    static final double SPACING = 1.0D;

    /**
     * A point on the trail: position, the unit travel direction there, its arc length, and the side room
     * toward {@code (-dirZ, dirX)} ({@code roomPos}) and toward {@code (dirZ, -dirX)} ({@code roomNeg}).
     * Room is NaN until measured. A single-point trail has direction (0, 0).
     */
    record TrailPoint(double x, double y, double z, double dirX, double dirZ, double arc, float roomPos, float roomNeg) {
    }

    private final double[] xs = new double[CAPACITY];
    private final double[] ys = new double[CAPACITY];
    private final double[] zs = new double[CAPACITY];
    private final double[] arcs = new double[CAPACITY];
    private final float[] roomPos = new float[CAPACITY];
    private final float[] roomNeg = new float[CAPACITY];
    private int newest = -1;
    private int size;

    int size() {
        return this.size;
    }

    void clear() {
        this.newest = -1;
        this.size = 0;
    }

    /**
     * Appends the position if it is at least {@link #SPACING} from the newest point.
     *
     * @return the buffer index of the new point (for {@link #setRoom}), or -1 if nothing was recorded
     */
    int record(double x, double y, double z) {
        double arc = 0D;
        if (this.size > 0) {
            double dx = x - this.xs[this.newest];
            double dz = z - this.zs[this.newest];
            double step = Math.sqrt(dx * dx + dz * dz);
            if (step < SPACING) {
                return -1;
            }
            arc = this.arcs[this.newest] + step;
        }
        this.newest = (this.newest + 1) % CAPACITY;
        this.xs[this.newest] = x;
        this.ys[this.newest] = y;
        this.zs[this.newest] = z;
        this.arcs[this.newest] = arc;
        this.roomPos[this.newest] = Float.NaN;
        this.roomNeg[this.newest] = Float.NaN;
        if (this.size < CAPACITY) {
            this.size++;
        }
        return this.newest;
    }

    /** Arc length of the newest point; NaN if empty. */
    double headArc() {
        return this.size == 0 ? Double.NaN : this.arcs[this.newest];
    }

    /** Arc length of the oldest kept point; NaN if empty. */
    double tailArc() {
        return this.size == 0 ? Double.NaN : this.arcs[at(0)];
    }

    /** The newest point, with the direction of the last segment; {@code null} if empty. */
    @Nullable TrailPoint newest() {
        return this.size == 0 ? null : pointAt(headArc());
    }

    /** The point at {@code arc}, clamped to the trail and interpolated; side room is the tighter of the two ends. */
    @Nullable TrailPoint pointAt(double arc) {
        if (this.size == 0) {
            return null;
        }
        if (this.size == 1) {
            int i = this.newest;
            return new TrailPoint(this.xs[i], this.ys[i], this.zs[i], 0D, 0D, this.arcs[i], this.roomPos[i], this.roomNeg[i]);
        }
        double clamped = Math.max(tailArc(), Math.min(headArc(), arc));
        int segment = segmentAt(clamped);
        int a = at(segment);
        int b = at(segment + 1);
        double length = this.arcs[b] - this.arcs[a];
        double t = (clamped - this.arcs[a]) / length;
        return new TrailPoint(this.xs[a] + (this.xs[b] - this.xs[a]) * t,
                this.ys[a] + (this.ys[b] - this.ys[a]) * t,
                this.zs[a] + (this.zs[b] - this.zs[a]) * t,
                (this.xs[b] - this.xs[a]) / length, (this.zs[b] - this.zs[a]) / length, clamped,
                Math.min(this.roomPos[a], this.roomPos[b]), Math.min(this.roomNeg[a], this.roomNeg[b]));
    }

    /**
     * Arc of the trail point closest to (x, z). With a hint (not NaN), only segments within {@code window}
     * of the hint are searched, so a follower does not jump to a nearby later leg after a U-turn; if none
     * is in the window, the whole trail is searched. NaN if empty.
     */
    double project(double x, double z, double hintArc, double window) {
        if (this.size == 0) {
            return Double.NaN;
        }
        if (this.size == 1) {
            return this.arcs[this.newest];
        }
        double arc = Double.isNaN(hintArc) ? Double.NaN : nearest(x, z, hintArc - window, hintArc + window);
        return Double.isNaN(arc) ? nearest(x, z, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) : arc;
    }

    void setRoom(int index, float roomPos, float roomNeg) {
        this.roomPos[index] = roomPos;
        this.roomNeg[index] = roomNeg;
    }

    /**
     * The tightest side room on one side ({@code positive}: toward {@code (-dirZ, dirX)}) over the trail from
     * {@code fromArc} to {@code toArc}, both clamped to the trail: the points of every segment the range
     * touches. NaN if any of them is unmeasured, or the trail is empty. Followers clamp their lane offset
     * with it, so they move in before a narrowing instead of hitting its edge.
     */
    float minRoom(double fromArc, double toArc, boolean positive) {
        if (this.size == 0) {
            return Float.NaN;
        }
        float[] room = positive ? this.roomPos : this.roomNeg;
        if (this.size == 1) {
            return room[this.newest];
        }
        double from = Math.max(tailArc(), Math.min(headArc(), fromArc));
        double to = Math.max(from, Math.min(headArc(), toArc));
        int last = Math.min(this.size - 1, segmentAt(to) + 1);
        float min = Float.POSITIVE_INFINITY;
        for (int k = segmentAt(from); k <= last; k++) {
            min = Math.min(min, room[at(k)]);
        }
        return min;
    }

    private double nearest(double x, double z, double fromArc, double toArc) {
        double bestArc = Double.NaN;
        double bestDistance = Double.MAX_VALUE;
        for (int k = 0; k < this.size - 1; k++) {
            int a = at(k);
            int b = at(k + 1);
            if (this.arcs[b] < fromArc || this.arcs[a] > toArc) {
                continue;
            }
            double sx = this.xs[b] - this.xs[a];
            double sz = this.zs[b] - this.zs[a];
            double lengthSqr = sx * sx + sz * sz;
            double t = Math.max(0D, Math.min(1D, ((x - this.xs[a]) * sx + (z - this.zs[a]) * sz) / lengthSqr));
            double px = this.xs[a] + sx * t - x;
            double pz = this.zs[a] + sz * t - z;
            double distance = px * px + pz * pz;
            if (distance < bestDistance) {
                bestDistance = distance;
                bestArc = this.arcs[a] + (this.arcs[b] - this.arcs[a]) * t;
            }
        }
        return bestArc;
    }

    /** Logical index (0 = oldest) of the segment start whose arc is the largest not above {@code arc}. */
    private int segmentAt(double arc) {
        int low = 0;
        int high = this.size - 2;
        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            if (this.arcs[at(mid)] <= arc) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    /** Buffer index of logical index {@code k} (0 = oldest). */
    private int at(int k) {
        return Math.floorMod(this.newest - (this.size - 1) + k, CAPACITY);
    }
}
```

- [ ] **Step 4: Update `HerdLeading` to the new trail (interim, replaced in Task 15)**

Replace the whole file `.../social/HerdLeading.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.world.level.pathfinder.Path;

/**
 * The leader's part of herd movement, called from every dinosaur's server tick: it returns at once for
 * non-leaders. Interim version: travel is still inferred from the leader's path (until Task 11), and the
 * trail is recorded by distance. Task 15 replaces this class.
 */
public final class HerdLeading {

    /** The herd travels when the leader's path ends at least this far away. */
    static final double TRAVEL_DISTANCE = 8D;
    /** Travel is checked every this many ticks. */
    static final int STATE_CHECK_INTERVAL = 5;

    private HerdLeading() {
    }

    public static void tick(Dinosaur dinosaur) {
        DinoGroup group = GroupManager.groupOf(dinosaur);
        if (group == null || !group.isLeader(dinosaur.getUUID())) {
            return;
        }
        if (dinosaur.tickCount % STATE_CHECK_INTERVAL == 0) {
            Path path = dinosaur.getNavigation().getPath();
            boolean travelling = path != null && !path.isDone()
                    && path.getTarget().distToCenterSqr(dinosaur.position()) > TRAVEL_DISTANCE * TRAVEL_DISTANCE;
            group.updateState(travelling, dinosaur.level().getGameTime());
        }
        group.trail().record(dinosaur.getX(), dinosaur.getY(), dinosaur.getZ());
    }
}
```

- [ ] **Step 5: Run the tests to verify they pass**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: every `LeaderTrailTest` line `PASS`, `DinoGroupTest` still passes; last line `109 run, 0 failed` (105 - 7 old + 11).

- [ ] **Step 6: Checkpoint (no commit)**

Run: `git status --short`
Expected: `LeaderTrail.java`, `HerdLeading.java`, `LeaderTrailTest.java` modified; nothing staged.

### Task 8: Travel order, wedge layout and trail following

Spec "Travel order", "Lanes", "Following", lead decision 1 and plan review decision 9: followers ordered nearest-first at the journey start; rows behind the leader 5 blocks apart in open ground (personal space + 2) and 4 in tight ground (personal space + 1), each row tried at the open spacing first; row r holds at most `2 + r` followers, never more than 3 and never more than the ground allows (a wedge in open ground, 1 to 3 abreast, single file in gaps); every follower gets its own target (arc, lane offset). A follower steers to a look-ahead point 3 blocks ahead of its projection, offset by its lane but never beyond the tightest side room from its projection to 4 blocks past the look-ahead point (so it moves in before a narrowing instead of hitting its edge), at a speed from its arc gap; if its target arc is not on the trail yet, it waits where it stands.

**Files:**
- Create: `.../social/TravelOrder.java`
- Test: `test/.../social/TravelOrderTest.java`

**Interfaces:**
- Consumes: `LeaderTrail` (`size`, `headArc`, `tailArc`, `pointAt`, `minRoom`, `TrailPoint`) from Task 7.
- Produces (package-private, pure): constants `FIRST_ROW = 2`, `MAX_LANES = 3`, `OPEN_MARGIN = 2.0`, `TIGHT_MARGIN = 1.0`, `LOOK_AHEAD = 3.0`, `ROOM_AHEAD = 4.0`, `AHEAD_GAP = -0.5`, `BEHIND_GAP = 3.0`, `CATCH_UP_SPEED = 1.2`; `record Target(double x, double y, double z, double speed)`; `double openSpacing(double personalSpace)`; `double tightSpacing(double personalSpace)`; `int lanes(double room, double spacing)` (room = side room on the tighter side, NaN unknown); `List<UUID> byDistance(double leaderX, double leaderZ, List<UUID> ids, double[] xs, double[] zs)`; `void layout(double leaderArc, int followers, double openSpacing, double tightSpacing, DoubleUnaryOperator roomAtArc, double[] arcs, double[] offsets)`; `double followSpeed(double gap)`; `@Nullable Target follow(LeaderTrail trail, double projectedArc, double targetArc, double offset)`.

- [ ] **Step 1: Write the failing test**

Create `test/.../social/TravelOrderTest.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TravelOrderTest {

    private static final double EPSILON = 1.0E-9D;
    /** Gallimimus: personal space 3, so 5 in open ground and 4 in tight ground. */
    private static final double OPEN = 5D;
    private static final double TIGHT = 4D;

    private static LeaderTrail straightTrail(int length) {
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x <= length; x++) {
            trail.record(x, 64D, 0D);
        }
        return trail;
    }

    @Test
    void spacingIsPersonalSpacePlusTwoInTheOpenAndPlusOneWhenTight() {
        assertEquals(OPEN, TravelOrder.openSpacing(3D), EPSILON);
        assertEquals(TIGHT, TravelOrder.tightSpacing(3D), EPSILON);
    }

    @Test
    void lanesFollowTheSideRoom() {
        assertEquals(3, TravelOrder.lanes(5D, OPEN));
        assertEquals(2, TravelOrder.lanes(2.5D, OPEN));
        assertEquals(1, TravelOrder.lanes(2.4D, OPEN));
        assertEquals(1, TravelOrder.lanes(Double.NaN, OPEN));
        assertEquals(3, TravelOrder.lanes(4D, TIGHT));
    }

    @Test
    void byDistanceIsNearestFirst() {
        UUID a = new UUID(0L, 1L);
        UUID b = new UUID(0L, 2L);
        UUID c = new UUID(0L, 3L);
        List<UUID> order = TravelOrder.byDistance(0D, 0D, List.of(a, b, c), new double[]{9D, 1D, 4D}, new double[]{0D, 0D, 0D});
        assertEquals(List.of(b, c, a), order);
    }

    @Test
    void openGroundMakesAWideWedge() {
        double[] arcs = new double[9];
        double[] offsets = new double[9];
        TravelOrder.layout(100D, 9, OPEN, TIGHT, arc -> 6D, arcs, offsets);
        double[] expectedArcs = {95D, 95D, 90D, 90D, 90D, 85D, 85D, 85D, 80D};
        double[] expectedOffsets = {-2.5D, 2.5D, -5D, 0D, 5D, -5D, 0D, 5D, 0D};
        for (int i = 0; i < 9; i++) {
            assertEquals(expectedArcs[i], arcs[i], EPSILON, "arc of follower " + i);
            assertEquals(expectedOffsets[i], offsets[i], EPSILON, "offset of follower " + i);
        }
    }

    @Test
    void tightGroundUsesTheTightSpacing() {
        // Room 4: the first row (2 lanes wanted) still fits 5 apart; rows of 3 fit only 4 apart.
        double[] arcs = new double[7];
        double[] offsets = new double[7];
        TravelOrder.layout(100D, 7, OPEN, TIGHT, arc -> 4D, arcs, offsets);
        double[] expectedArcs = {95D, 95D, 91D, 91D, 91D, 87D, 87D};
        double[] expectedOffsets = {-2.5D, 2.5D, -4D, 0D, 4D, -2D, 2D};
        for (int i = 0; i < 7; i++) {
            assertEquals(expectedArcs[i], arcs[i], EPSILON, "arc of follower " + i);
            assertEquals(expectedOffsets[i], offsets[i], EPSILON, "offset of follower " + i);
        }
    }

    @Test
    void narrowGroundIsSingleFileAtTheTightSpacing() {
        double[] arcs = new double[5];
        double[] offsets = new double[5];
        TravelOrder.layout(100D, 5, OPEN, TIGHT, arc -> 1D, arcs, offsets);
        for (int i = 0; i < 5; i++) {
            assertEquals(100D - (i + 1) * TIGHT, arcs[i], EPSILON);
            assertEquals(0D, offsets[i], EPSILON);
        }
    }

    @Test
    void fixedSpacingDebugUsesOneSpacingEverywhere() {
        double[] arcs = new double[3];
        double[] offsets = new double[3];
        TravelOrder.layout(100D, 3, TIGHT, TIGHT, arc -> 6D, arcs, offsets);
        double[] expectedArcs = {96D, 96D, 92D};
        double[] expectedOffsets = {-2D, 2D, 0D};
        for (int i = 0; i < 3; i++) {
            assertEquals(expectedArcs[i], arcs[i], EPSILON, "arc of follower " + i);
            assertEquals(expectedOffsets[i], offsets[i], EPSILON, "offset of follower " + i);
        }
    }

    @Test
    void everyFollowerGetsItsOwnTargetInEveryKindOfGround() {
        for (double room : new double[]{Double.NaN, 1D, 2.5D, 4D, 6D}) {
            for (int followers = 1; followers <= 20; followers++) {
                double[] arcs = new double[followers];
                double[] offsets = new double[followers];
                TravelOrder.layout(200D, followers, OPEN, TIGHT, arc -> room, arcs, offsets);
                for (int i = 0; i < followers; i++) {
                    for (int j = i + 1; j < followers; j++) {
                        assertFalse(Math.abs(arcs[i] - arcs[j]) < EPSILON && Math.abs(offsets[i] - offsets[j]) < EPSILON,
                                "followers " + i + " and " + j + " share a target with side room " + room);
                    }
                }
            }
        }
    }

    @Test
    void aGapNarrowsOnlyTheRowsInsideIt() {
        // Rows that land in the gap (arc 86 to 91) go single file at the tight spacing; the open rows around it stay 5 apart.
        double[] arcs = new double[8];
        double[] offsets = new double[8];
        TravelOrder.layout(100D, 8, OPEN, TIGHT, arc -> arc >= 86D && arc <= 91D ? 1D : 6D, arcs, offsets);
        double[] expectedArcs = {95D, 95D, 91D, 87D, 82D, 82D, 82D, 77D};
        for (int i = 0; i < 8; i++) {
            assertEquals(expectedArcs[i], arcs[i], EPSILON, "arc of follower " + i);
        }
    }

    @Test
    void singleFollowerWalksOnTheTrail() {
        double[] arcs = new double[1];
        double[] offsets = new double[1];
        TravelOrder.layout(50D, 1, OPEN, TIGHT, arc -> 6D, arcs, offsets);
        assertEquals(45D, arcs[0], EPSILON);
        assertEquals(0D, offsets[0], EPSILON);
    }

    @Test
    void followSpeedIsZeroAheadAndCatchesUpBehind() {
        assertEquals(0D, TravelOrder.followSpeed(-1D), EPSILON);
        assertEquals(0D, TravelOrder.followSpeed(TravelOrder.AHEAD_GAP), EPSILON);
        assertEquals(0.6D, TravelOrder.followSpeed(1.25D), EPSILON);
        assertEquals(TravelOrder.CATCH_UP_SPEED, TravelOrder.followSpeed(3D), EPSILON);
        assertEquals(TravelOrder.CATCH_UP_SPEED, TravelOrder.followSpeed(30D), EPSILON);
    }

    @Test
    void followAimsAheadOfItsProjectionButNeverPastTheLeader() {
        LeaderTrail trail = straightTrail(20);
        assertEquals(13D, TravelOrder.follow(trail, 10D, 6D, 0D).x(), EPSILON);
        assertEquals(20D, TravelOrder.follow(trail, 19D, 15D, 0D).x(), EPSILON);
    }

    @Test
    void followKeepsTheLaneOffsetInsideTheSideRoom() {
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x <= 20; x++) {
            trail.setRoom(trail.record(x, 64D, 0D), 1F, 5F);
        }
        // Travel along +x: positive offsets lie toward (-dirZ, dirX) = +z, negative toward -z.
        TravelOrder.Target left = TravelOrder.follow(trail, 10D, 6D, 4D);
        assertEquals(13D, left.x(), EPSILON);
        assertEquals(1D, left.z(), EPSILON);
        assertEquals(-4D, TravelOrder.follow(trail, 10D, 6D, -4D).z(), EPSILON);
    }

    @Test
    void followKeepsTheLaneOffsetInsideTheRoomAhead() {
        // Side room 5 up to x 15, then 1. A follower at arc 10 (look-ahead 13) already sees the narrowing
        // ROOM_AHEAD past its look-ahead point; one at arc 5 does not.
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x <= 20; x++) {
            float room = x >= 16 ? 1F : 5F;
            trail.setRoom(trail.record(x, 64D, 0D), room, room);
        }
        assertEquals(1D, TravelOrder.follow(trail, 10D, 6D, 4D).z(), EPSILON);
        assertEquals(4D, TravelOrder.follow(trail, 5D, 1D, 4D).z(), EPSILON);
    }

    @Test
    void followWaitsWhileItsTargetIsNotOnTheTrailYet() {
        LeaderTrail trail = straightTrail(5);
        assertEquals(0D, TravelOrder.follow(trail, 0D, -4D, 0D).speed(), EPSILON);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors, `cannot find symbol ... TravelOrder`.

- [ ] **Step 3: Write the implementation**

Create `.../social/TravelOrder.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

/**
 * Travel formation (spec "Travel", lead decision 1, plan review decision 9). Followers are ordered
 * nearest-first at the journey start. Rows lie along the trail behind the leader, {@link #openSpacing}
 * apart in open ground and {@link #tightSpacing} apart where the ground is tight; row r (0 = nearest)
 * holds at most {@code FIRST_ROW + r} followers, never more than {@link #MAX_LANES} and never more than
 * the ground allows there: a wedge in open ground that narrows to 1-3 abreast and to single file in gaps.
 * Every follower has its own (arc, lane offset). A follower steers to a look-ahead point on the trail at
 * a speed from its arc gap. Pure.
 */
final class TravelOrder {

    /** Followers in the row nearest the leader; each row behind holds one more, up to {@link #MAX_LANES}. */
    static final int FIRST_ROW = 2;
    static final int MAX_LANES = 3;
    /** In open ground, slots are this much farther apart than the hard minimum (the personal space): 5 for Gallimimus. */
    static final double OPEN_MARGIN = 2.0D;
    /** In tight ground, slots are this much farther apart than the personal space (4 for Gallimimus), so neighbours are not on the mask limit. */
    static final double TIGHT_MARGIN = 1.0D;
    /** A follower steers to the trail point this far ahead of its own projection (spec: 3). */
    static final double LOOK_AHEAD = 3.0D;
    /** The lane offset is clamped by the side room up to this far past the look-ahead point: about one turning radius. */
    static final double ROOM_AHEAD = 4.0D;
    /** At this arc gap (target minus own arc; negative = ahead of the target) a follower stands. */
    static final double AHEAD_GAP = -0.5D;
    /** At this arc gap and beyond a follower walks at {@link #CATCH_UP_SPEED}. */
    static final double BEHIND_GAP = 3.0D;
    static final double CATCH_UP_SPEED = 1.2D;

    /** A follower's steering target and wanted speed modifier. */
    record Target(double x, double y, double z, double speed) {
    }

    private TravelOrder() {
    }

    static double openSpacing(double personalSpace) {
        return personalSpace + OPEN_MARGIN;
    }

    static double tightSpacing(double personalSpace) {
        return personalSpace + TIGHT_MARGIN;
    }

    /**
     * Lanes the ground allows at a trail point, from the side room on its tighter side: 3 if there is room
     * for an offset of one spacing, 2 if there is room for half a spacing, else 1. Unknown room (NaN) counts as 1.
     */
    static int lanes(double room, double spacing) {
        if (Double.isNaN(room)) {
            return 1;
        }
        if (room >= spacing) {
            return MAX_LANES;
        }
        return room >= spacing / 2D ? 2 : 1;
    }

    /** The followers sorted by distance to the leader, nearest first. {@code xs} and {@code zs} parallel {@code ids}. */
    static List<UUID> byDistance(double leaderX, double leaderZ, List<UUID> ids, double[] xs, double[] zs) {
        Integer[] order = new Integer[ids.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingDouble(i -> (xs[i] - leaderX) * (xs[i] - leaderX) + (zs[i] - leaderZ) * (zs[i] - leaderZ)));
        List<UUID> sorted = new ArrayList<>(order.length);
        for (int i : order) {
            sorted.add(ids.get(i));
        }
        return sorted;
    }

    /**
     * Writes each follower's target arc and lane offset (follower index = travel order). Each row is tried
     * at the open spacing behind the row ahead; if the ground there allows fewer lanes than the row wants,
     * the row is laid at the tight spacing instead. A row's spacing is both its step from the row ahead and
     * the gap between its lanes. A partly filled row is centred. Offsets are positive toward
     * {@code (-dirZ, dirX)} of the trail direction.
     *
     * @param roomAtArc side room on the tighter side of the trail point at an arc; NaN when unknown
     */
    static void layout(double leaderArc, int followers, double openSpacing, double tightSpacing,
                       DoubleUnaryOperator roomAtArc, double[] arcs, double[] offsets) {
        int placed = 0;
        double rowAhead = leaderArc;
        for (int row = 0; placed < followers; row++) {
            int wanted = Math.min(FIRST_ROW + row, MAX_LANES);
            double spacing = openSpacing;
            int lanes = lanes(roomAtArc.applyAsDouble(rowAhead - spacing), spacing);
            if (lanes < wanted) {
                spacing = tightSpacing;
                lanes = lanes(roomAtArc.applyAsDouble(rowAhead - spacing), spacing);
            }
            double arc = rowAhead - spacing;
            int inRow = Math.min(Math.min(wanted, lanes), followers - placed);
            for (int j = 0; j < inRow; j++) {
                arcs[placed] = arc;
                offsets[placed] = (j - (inRow - 1) / 2D) * spacing;
                placed++;
            }
            rowAhead = arc;
        }
    }

    /** Speed modifier from the arc gap: 0 at {@link #AHEAD_GAP} or more ahead, rising to {@link #CATCH_UP_SPEED} at {@link #BEHIND_GAP} behind. */
    static double followSpeed(double gap) {
        if (gap <= AHEAD_GAP) {
            return 0D;
        }
        if (gap >= BEHIND_GAP) {
            return CATCH_UP_SPEED;
        }
        return CATCH_UP_SPEED * (gap - AHEAD_GAP) / (BEHIND_GAP - AHEAD_GAP);
    }

    /**
     * The look-ahead target: the trail point {@link #LOOK_AHEAD} past the follower's projection (never past
     * the leader), moved sideways by its lane offset but never farther than the tightest side room from the
     * projection to {@link #ROOM_AHEAD} past the look-ahead point. Speed 0 if its target arc is not on the
     * trail yet (it waits where it stands). {@code null} for an empty trail.
     */
    static @Nullable Target follow(LeaderTrail trail, double projectedArc, double targetArc, double offset) {
        if (trail.size() == 0) {
            return null;
        }
        double look = Math.max(trail.tailArc(), Math.min(projectedArc + LOOK_AHEAD, trail.headArc()));
        LeaderTrail.TrailPoint point = trail.pointAt(look);
        float room = trail.minRoom(projectedArc, look + ROOM_AHEAD, offset >= 0D);
        double lateral = Math.copySign(Float.isNaN(room) ? 0D : Math.min(Math.abs(offset), room), offset);
        double speed = targetArc < trail.tailArc() ? 0D : followSpeed(targetArc - projectedArc);
        return new Target(point.x() - point.dirZ() * lateral, point.y(), point.z() + point.dirX() * lateral, speed);
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: every `TravelOrderTest` line `PASS`; last line `124 run, 0 failed` (109 + 15).

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`
Expected: `TravelOrder.java` and its test new; nothing staged.

### Task 9: Per-species clearance box

Lead decision 5: the clearance box comes from the model size, per species. The values live in `ClearanceBox.MODEL_SIZES` (Java, one entry per species: the adult model's cross width and top in blocks, render scale applied, size gene 0). They are obtained with `tools/clearance/measure_models.py`, which reads each `geo/entity/*.geo.json` and the adult render scale of each renderer. The box is `max(hitbox, model x growth scale)` plus 0.25 per side and 0.25 on top, rounded up to 0.25; the `max` keeps it right after the hitbox fix of Task 22. The class also builds the `BodyFit` cache key (spec "Cache": box position rounded to 0.25, feet height, box size).

**Files:**
- Create: `.../social/ClearanceBox.java`, `tools/clearance/measure_models.py`
- Modify: `tools/junit-standin/stub/org/junit/jupiter/api/Assertions.java` (add `assertNotEquals`; the stand-in has no such overload and the test below needs it)
- Test: `test/.../social/ClearanceBoxTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces (package-private, pure): constants `SIDE_MARGIN = 0.25`, `TOP_MARGIN = 0.25`, `QUANTUM = 0.25`, `MAX_WIDTH = 7.75`, `MAX_HEIGHT = 31.75`; `record ModelSize(double width, double top)`; `Map<String, ModelSize> MODEL_SIZES`; constructor `ClearanceBox(double width, double height)`; `static ClearanceBox of(String species, double hitboxWidth, double hitboxHeight, int growthStage, double sizeGenePercent)` (species = entity id path, for example `gallimimus`; growth stage as `Dinosaur.getGrowthStage()`, 1 baby to 4 adult); `static double growthScale(int growthStage, double sizeGenePercent)`; `static double quantise(double value)`; `double width()`; `double height()`; `double halfWidth()`; `long probeKey(double x, double feetY, double z)`.

- [ ] **Step 1: Write the measurement script**

Create `tools/clearance/measure_models.py`:

```python
"""Prints each dinosaur model's adult size for ClearanceBox.MODEL_SIZES (herd movement redesign, Task 9).

Usage, from the worktree root:  python tools/clearance/measure_models.py

Width is the model's extent across the body (x), top its highest point above the feet (y), both in blocks,
scaled by the adult render scale with the size gene at 0 (the adultScale lines in
common/src/main/java/net/dumbcode/projectnublar/client/renderer/dinosaurs/*Renderer.java). Length (z) is
printed for the hitbox review of Task 22 only. Cube rotations and bone pivots are ignored, so the numbers
are estimates; check them in game with F3+B.
"""
import glob
import json
import os

GEO = "common/src/main/resources/assets/projectnublar/geo/entity"
# Adult render scale per species: the constant added to the size gene in each renderer's scaleModelForRender.
ADULT_SCALE = {
    "brachiosaurus": 2.5,
    "dilophosaurus": 0.5,
    "gallimimus": 1.0,
    "triceratops": 2.0,
    "tyrannosaurus_rex": 1.0,
    "velociraptor": 0.6,
}


def main():
    for path in sorted(glob.glob(os.path.join(GEO, "*.geo.json"))):
        name = os.path.basename(path)[: -len(".geo.json")]
        if name not in ADULT_SCALE:
            print("skip", name, "(no adult scale in this script)")
            continue
        with open(path, encoding="utf-8") as handle:
            geometry = json.load(handle)["minecraft:geometry"][0]
        xs, tops, zs = [], [], []
        for bone in geometry.get("bones", []):
            for cube in bone.get("cubes", []):
                origin, size, inflate = cube["origin"], cube["size"], cube.get("inflate", 0)
                xs += [origin[0] - inflate, origin[0] + size[0] + inflate]
                tops.append(origin[1] + size[1] + inflate)
                zs += [origin[2] - inflate, origin[2] + size[2] + inflate]
        scale = ADULT_SCALE[name] / 16.0
        print('Map.entry("%s", new ModelSize(%.2fD, %.2fD)),  // length %.2f'
              % (name, (max(xs) - min(xs)) * scale, max(tops) * scale, (max(zs) - min(zs)) * scale))


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Run the script and compare with the table below**

Run: `python tools/clearance/measure_models.py`
Expected output:

```
Map.entry("brachiosaurus", new ModelSize(3.38D, 19.10D)),  // length 17.41
Map.entry("dilophosaurus", new ModelSize(0.49D, 1.73D)),  // length 4.74
Map.entry("gallimimus", new ModelSize(0.69D, 2.53D)),  // length 6.12
Map.entry("triceratops", new ModelSize(3.40D, 5.80D)),  // length 13.11
Map.entry("tyrannosaurus_rex", new ModelSize(1.38D, 3.42D)),  // length 10.87
Map.entry("velociraptor", new ModelSize(0.53D, 1.48D)),  // length 5.18
```

If a renderer's adult scale or a model changed since this plan was written, use the printed values in Step 5 instead of the ones shown there.

- [ ] **Step 3: Give the stand-in runner `assertNotEquals`**

In `tools/junit-standin/stub/org/junit/jupiter/api/Assertions.java`, replace:

```java
    public static void assertEquals(long expected, long actual, String message) { if (expected != actual) fail(message + ": expected " + expected + ", got " + actual); }
```

with:

```java
    public static void assertEquals(long expected, long actual, String message) { if (expected != actual) fail(message + ": expected " + expected + ", got " + actual); }
    public static void assertNotEquals(long unexpected, long actual) { assertNotEquals(unexpected, actual, "unexpectedly equal"); }
    public static void assertNotEquals(long unexpected, long actual, String message) { if (unexpected == actual) fail(message + ": both " + actual); }
```

(The runner compiles every test in one javac call, so a missing overload would stop all tests, not just this class.)

- [ ] **Step 4: Write the failing test**

Create `test/.../social/ClearanceBoxTest.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ClearanceBoxTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void growthScaleMatchesTheRenderers() {
        assertEquals(0.25D, ClearanceBox.growthScale(1, 0D), EPSILON);
        assertEquals(0.75D, ClearanceBox.growthScale(3, 0D), EPSILON);
        assertEquals(1D, ClearanceBox.growthScale(4, 0D), EPSILON);
        assertEquals(1.5D, ClearanceBox.growthScale(4, 50D), EPSILON);
    }

    @Test
    void gallimimusBoxIsTheHitboxPlusAQuarter() {
        ClearanceBox box = ClearanceBox.of("gallimimus", 2D, 3D, 4, 0D);
        assertEquals(2.5D, box.width(), EPSILON);
        assertEquals(3.25D, box.height(), EPSILON);
    }

    @Test
    void aModelLargerThanItsHitboxSetsTheBox() {
        ClearanceBox box = ClearanceBox.of("triceratops", 2D, 3D, 4, 0D);
        assertEquals(4.0D, box.width(), EPSILON);
        assertEquals(6.25D, box.height(), EPSILON);
    }

    @Test
    void unknownSpeciesUsesTheHitbox() {
        ClearanceBox box = ClearanceBox.of("unknown", 1D, 1D, 4, 0D);
        assertEquals(1.5D, box.width(), EPSILON);
        assertEquals(1.25D, box.height(), EPSILON);
    }

    @Test
    void sizesRoundUpToAQuarter() {
        ClearanceBox box = new ClearanceBox(1.1D, 1.3D);
        assertEquals(1.25D, box.width(), EPSILON);
        assertEquals(1.5D, box.height(), EPSILON);
    }

    @Test
    void probeKeyTellsQuartersApart() {
        ClearanceBox box = new ClearanceBox(2.5D, 3.25D);
        assertNotEquals(box.probeKey(10.0D, 64D, 5D), box.probeKey(10.25D, 64D, 5D));
    }

    @Test
    void probeKeyIsTheSameInsideOneQuarter() {
        ClearanceBox box = new ClearanceBox(2.5D, 3.25D);
        assertEquals(box.probeKey(10.01D, 64D, 5D), box.probeKey(10.1D, 64D, 5D));
    }

    @Test
    void probeKeyDependsOnFeetHeightAndBoxSize() {
        ClearanceBox box = new ClearanceBox(2.5D, 3.25D);
        assertNotEquals(box.probeKey(10D, 64D, 5D), box.probeKey(10D, 64.5D, 5D));
        assertNotEquals(box.probeKey(10D, 64D, 5D), new ClearanceBox(2.5D, 3.5D).probeKey(10D, 64D, 5D));
    }
}
```

- [ ] **Step 5: Run the test to verify it fails**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors, `cannot find symbol ... ClearanceBox` (and none about `assertNotEquals`).

- [ ] **Step 6: Write the implementation**

Create `.../social/ClearanceBox.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.Map;

/**
 * The box a herd member's movement checks are made with (spec "Fitting", lead decision 5): per species, from
 * the model size. Width = max(hitbox width, model cross width x growth scale) + {@link #SIDE_MARGIN} per side;
 * height = max(hitbox height, model top x growth scale) + {@link #TOP_MARGIN}; both rounded up to
 * {@link #QUANTUM}. The model's length is not in the box: an axis-aligned box cannot follow the yaw.
 * <p>
 * {@link #MODEL_SIZES} comes from {@code tools/clearance/measure_models.py} (adult models, render scale
 * applied, size gene 0). {@link #growthScale} repeats the renderers' growth-stage scale (0.25, 0.5, 0.75, 1)
 * and size gene. Pure.
 */
final class ClearanceBox {

    static final double SIDE_MARGIN = 0.25D;
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

    /** The box of an animal of {@code species} (entity id path) with this hitbox, growth stage (1-4) and size gene (%). */
    static ClearanceBox of(String species, double hitboxWidth, double hitboxHeight, int growthStage, double sizeGenePercent) {
        ModelSize model = MODEL_SIZES.get(species);
        double scale = growthScale(growthStage, sizeGenePercent);
        double bodyWidth = model == null ? hitboxWidth : Math.max(hitboxWidth, model.width() * scale);
        double bodyTop = model == null ? hitboxHeight : Math.max(hitboxHeight, model.top() * scale);
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
```

- [ ] **Step 7: Run the test to verify it passes**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: every `ClearanceBoxTest` line `PASS`; last line `132 run, 0 failed` (124 + 8).

- [ ] **Step 8: Checkpoint (no commit)**

Run: `git status --short`
Expected: `ClearanceBox.java`, its test and `tools/clearance/` new; `Assertions.java` modified; nothing staged.

### Task 10: Body fit, the only block reader

Spec "Fitting": the vanilla collision query on the full clearance box at the probe position, at feet height or raised by the step height; ground under any footprint column within 3 blocks, no fluid, fire, cactus, fence or electric fence; a per-herd cache keyed by `ClearanceBox.probeKey`. Stand results are kept for `STAND_CACHE_TICKS` (20 ticks): members move 0.3-0.7 blocks per tick, so a per-tick cache would be empty for every probe, while the ground does not change faster than a second; the guard's fit test is cached per tick. It also gives a straight-line reach test (graze spots, leader graze moves) and the side room beside a trail point (lanes). Barriers and every other block count by their real collision shape (lead feedback). It cannot be unit-tested without a level; the GameTests `memberNeverEntersLowGap`, `herdCrossesForest` and `herdSkirtsWater` (Task 20) exercise it.

**Files:**
- Create: `.../social/BodyFit.java`

**Interfaces:**
- Consumes: `ClearanceBox` (`of`, `quantise`, `probeKey`, `halfWidth`, `height`) from Task 9.
- Produces (package-private): `static final int MAX_DROP = 3`, `double STEP_HEIGHT = 1.0`, `double SIDE_ROOM_STEP = 0.5`, `double MAX_SIDE_ROOM = 5.5`, `double REACH_STEP = 1.0`, `int STAND_CACHE_TICKS = 20`; `static ClearanceBox boxOf(Dinosaur dinosaur)`; `double standY(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z)` (the feet height the body would stand at, NaN if it cannot stand there); `boolean canStand(ServerLevel, Entity, ClearanceBox, double x, double feetY, double z)`; `boolean fits(ServerLevel, Entity, ClearanceBox, double x, double feetY, double z)` (collision only, for the guard); `double reachY(ServerLevel level, Entity walker, ClearanceBox box, double fromX, double fromY, double fromZ, double toX, double toZ)` (feet height at the destination after walking a straight line sampled every block, NaN if blocked); `float sideRoom(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z, double sideX, double sideZ)`.

- [ ] **Step 1: Write the class**

Create `.../social/BodyFit.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.GeneInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The only herd class that reads blocks (spec "Fitting"; the 26.2 port changes this one place). Every test
 * uses a member's {@link ClearanceBox}:
 * <ul>
 *   <li><b>Fit</b>: the vanilla collision query ({@code noCollision}) on the whole box at feet height, or
 *       raised by {@link #STEP_HEIGHT}. Real collision shapes: leaves, slabs, carpets, glass and barrier
 *       blocks count as they are; nothing is special-cased.</li>
 *   <li><b>Ground</b>: a collision shape under some footprint column within {@link #MAX_DROP} blocks, with
 *       no hazard (fluid, fire, cactus, fences, walls, electric fences ...) under any column on the way down
 *       and none in the feet layer.</li>
 * </ul>
 * Results are cached per herd, keyed by {@link ClearanceBox#probeKey}; probe positions are rounded to 0.25
 * first so a cached answer is exact. Stand results live {@link #STAND_CACHE_TICKS}: the ground does not
 * change faster than that, and followers in a column probe the same cells a few ticks apart, so they share
 * them (a per-tick cache would miss on every probe). The guard's fit results live one tick. One instance
 * per herd. Server thread only.
 */
final class BodyFit {

    /** Largest drop a herd member steps down, the vanilla safe fall for mobs. */
    static final int MAX_DROP = 3;
    /** The dinosaurs' step height ({@code Dinosaur} sets {@code setMaxUpStep(1.0F)}). */
    static final double STEP_HEIGHT = 1.0D;
    static final double SIDE_ROOM_STEP = 0.5D;
    /** Side room is measured up to this far: 3 lanes 5 apart in open ground need 5 on each side (plan review decision 9). */
    static final double MAX_SIDE_ROOM = 5.5D;
    /** A straight-line reach is sampled every this many blocks (spec: every block). */
    static final double REACH_STEP = 1.0D;
    /** Stand results are kept this many ticks; a block placed or broken is seen within a second. */
    static final int STAND_CACHE_TICKS = 20;
    private static final double EPSILON = 1.0E-7D;

    private final Long2DoubleOpenHashMap standCache = new Long2DoubleOpenHashMap();
    private final Long2BooleanOpenHashMap fitCache = new Long2BooleanOpenHashMap();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private long fitTick = Long.MIN_VALUE;
    private long standTick = Long.MIN_VALUE;

    /** The clearance box of a herd member, from its species, hitbox, growth stage and size gene. */
    static ClearanceBox boxOf(Dinosaur dinosaur) {
        return ClearanceBox.of(BuiltInRegistries.ENTITY_TYPE.getKey(dinosaur.getType()).getPath(),
                dinosaur.getBbWidth(), dinosaur.getBbHeight(), dinosaur.getGrowthStage(),
                dinosaur.getDinoData().getGeneValue(GeneInit.SIZE.get()));
    }

    /**
     * The feet height at which the box stands with its bottom centre at (x, z), starting from {@code feetY}
     * (stepping up at most {@link #STEP_HEIGHT}, down at most {@link #MAX_DROP}), or NaN if it cannot stand
     * there (no fit, no ground, or a hazard). Cached for {@link #STAND_CACHE_TICKS}.
     */
    double standY(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z) {
        refresh(level);
        double qx = ClearanceBox.quantise(x);
        double qy = ClearanceBox.quantise(feetY);
        double qz = ClearanceBox.quantise(z);
        long key = box.probeKey(qx, qy, qz);
        if (this.standCache.containsKey(key)) {
            return this.standCache.get(key);
        }
        double result = standUncached(level, walker, box, qx, qy, qz);
        this.standCache.put(key, result);
        return result;
    }

    boolean canStand(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z) {
        return !Double.isNaN(standY(level, walker, box, x, feetY, z));
    }

    /** Collision only: does the box fit at feet height or one step up? The guard's test. Cached for this tick. */
    boolean fits(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z) {
        refresh(level);
        double qx = ClearanceBox.quantise(x);
        double qy = ClearanceBox.quantise(feetY);
        double qz = ClearanceBox.quantise(z);
        long key = box.probeKey(qx, qy, qz);
        if (this.fitCache.containsKey(key)) {
            return this.fitCache.get(key);
        }
        boolean result = boxClear(level, walker, box, qx, qy, qz) || boxClear(level, walker, box, qx, qy + STEP_HEIGHT, qz);
        this.fitCache.put(key, result);
        return result;
    }

    /**
     * Walks a straight line from (fromX, fromY, fromZ) to (toX, toZ), standing every {@link #REACH_STEP}
     * blocks, each step starting from the height found at the previous one.
     *
     * @return the feet height at the destination, or NaN if any step cannot stand
     */
    double reachY(ServerLevel level, Entity walker, ClearanceBox box, double fromX, double fromY, double fromZ, double toX, double toZ) {
        double dx = toX - fromX;
        double dz = toZ - fromZ;
        int steps = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dz * dz) / REACH_STEP));
        double y = fromY;
        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;
            y = standY(level, walker, box, fromX + dx * t, y, fromZ + dz * t);
            if (Double.isNaN(y)) {
                return Double.NaN;
            }
        }
        return y;
    }

    /**
     * How far the box can stand sideways from (x, z) along the unit direction (sideX, sideZ), in steps of
     * {@link #SIDE_ROOM_STEP}, at most {@link #MAX_SIDE_ROOM}.
     */
    float sideRoom(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z, double sideX, double sideZ) {
        for (double shift = SIDE_ROOM_STEP; shift <= MAX_SIDE_ROOM + EPSILON; shift += SIDE_ROOM_STEP) {
            if (!canStand(level, walker, box, x + sideX * shift, feetY, z + sideZ * shift)) {
                return (float) (shift - SIDE_ROOM_STEP);
            }
        }
        return (float) MAX_SIDE_ROOM;
    }

    private void refresh(ServerLevel level) {
        long now = level.getGameTime();
        if (now != this.fitTick) {
            this.fitCache.clear();
            this.fitTick = now;
        }
        if (now - this.standTick >= STAND_CACHE_TICKS || now < this.standTick) {
            this.standCache.clear();
            this.standTick = now;
        }
    }

    private double standUncached(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z) {
        if (boxClear(level, walker, box, x, feetY, z) && !feetHazard(level, box, x, feetY, z)) {
            double ground = groundBelow(level, box, x, feetY, z);
            if (!Double.isNaN(ground)) {
                return ground;
            }
        }
        double up = feetY + STEP_HEIGHT;
        if (boxClear(level, walker, box, x, up, z) && !feetHazard(level, box, x, up, z)) {
            return groundBelow(level, box, x, up, z);
        }
        return Double.NaN;
    }

    /** The vanilla collision query on the whole box, a hair smaller so touching faces do not count. */
    private boolean boxClear(ServerLevel level, Entity walker, ClearanceBox box, double x, double feetY, double z) {
        double half = box.halfWidth();
        AABB aabb = new AABB(x - half + EPSILON, feetY + EPSILON, z - half + EPSILON,
                x + half - EPSILON, feetY + box.height() - EPSILON, z + half - EPSILON);
        return level.noCollision(walker, aabb);
    }

    /** A hazard in the feet layer (water has no collision shape, so the fit test does not see it). */
    private boolean feetHazard(ServerLevel level, ClearanceBox box, double x, double feetY, double z) {
        double half = box.halfWidth();
        int y = Mth.floor(feetY + EPSILON);
        for (int bx = Mth.floor(x - half + EPSILON); bx <= Mth.floor(x + half - EPSILON); bx++) {
            for (int bz = Mth.floor(z - half + EPSILON); bz <= Mth.floor(z + half - EPSILON); bz++) {
                if (isHazard(level.getBlockState(this.pos.set(bx, y, bz)))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The highest ground top under the footprint within {@link #MAX_DROP} blocks below the feet, or NaN if
     * no column has ground or any column meets a hazard first.
     */
    private double groundBelow(ServerLevel level, ClearanceBox box, double x, double feetY, double z) {
        double half = box.halfWidth();
        int start = Mth.floor(feetY - EPSILON);
        double top = Double.NaN;
        for (int bx = Mth.floor(x - half + EPSILON); bx <= Mth.floor(x + half - EPSILON); bx++) {
            for (int bz = Mth.floor(z - half + EPSILON); bz <= Mth.floor(z + half - EPSILON); bz++) {
                for (int by = start; by >= start - MAX_DROP; by--) {
                    BlockState state = level.getBlockState(this.pos.set(bx, by, bz));
                    if (isHazard(state)) {
                        return Double.NaN;
                    }
                    VoxelShape shape = state.getCollisionShape(level, this.pos);
                    if (!shape.isEmpty()) {
                        double columnTop = by + shape.max(Direction.Axis.Y);
                        if (Double.isNaN(top) || columnTop > top) {
                            top = columnTop;
                        }
                        break;
                    }
                }
            }
        }
        return top;
    }

    private static boolean isHazard(BlockState state) {
        return !state.getFluidState().isEmpty()
                || state.is(BlockTags.FIRE) || state.is(Blocks.CACTUS) || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.POWDER_SNOW)
                || state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS) || state.is(BlockTags.FENCE_GATES)
                || state.is(BlockInit.ELECTRIC_FENCE.get())
                || state.is(BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get())
                || state.is(BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get());
    }
}
```

- [ ] **Step 2: Compile and run the unit tests**

Run: `./gradlew :common:compileJava`
Expected: `BUILD SUCCESSFUL`.

Run: `sh tools/junit-standin/run.sh`
Expected: last line `132 run, 0 failed` (no new unit tests: this class needs a level).

- [ ] **Step 3: Self-check against the spec**

Confirm by reading the code: the fit test uses `level.noCollision(walker, aabb)` only (no block list, no barrier case); the step height is tried once; ground is searched under every footprint column; a hazard under any column rejects the probe (so bodies keep off water edges: `herdSkirtsWater`); the fit cache is cleared every tick and the stand cache every `STAND_CACHE_TICKS`.

- [ ] **Step 4: Checkpoint (no commit)**

Run: `git status --short`
Expected: `BodyFit.java` new; nothing staged.

### Task 11: Herd state, journey, centre, spots and order in `DinoGroup`

Spec Components `DinoGroup`: "Add centre, spot reservations, travel order, explicit state. Delete slot assignments and the 40-tick path inference." The herd state is now set by journeys, not inferred from path length. The group also holds the per-tick caches every member shares: the `BodyFit` cache, the `HerdBodies` snapshot, the travel layout and the evaluation cap (at most 10 context evaluations per herd per tick). A leader change ends any journey. `GroupManager.bodiesOf` fills the snapshot once per tick.

**Files:**
- Modify: `.../social/DinoGroup.java` (whole file), `.../social/GroupManager.java` (add `bodiesOf`), `.../social/HerdSpacing.java` (add `herdRadius`), `.../social/HerdLeading.java` (whole file, interim)
- Test: `test/.../social/DinoGroupTest.java` (whole file)

**Interfaces:**
- Consumes: `LeaderTrail` incl. `minRoom` (Task 7), `TravelOrder.layout` (Task 8), `JourneyPolicy.Kind`, `JourneyPolicy.SETTLE_TICKS`, `JourneyPolicy.REISSUE_PROGRESS` (Task 6), `HerdArea.Point`, `HerdArea.radius`, `HerdArea.grazeSpacing` (Task 4), `BodyFit` (Task 10), `HerdBodies` (Task 3), `FallbackBudget` (existing).
- Produces, on `DinoGroup`:
  - `static final long UNSET = Long.MIN_VALUE`; `static final int MAX_EVALUATIONS_PER_TICK = 10`; `static final double ROW_ROOM_WINDOW = 2.0`.
  - State and journey: `public HerdState state()`; `boolean startJourney(JourneyPolicy.Kind kind, double x, double y, double z, List<UUID> followerOrder, long now)` (false when pinned); `boolean journeyActive()`; `JourneyPolicy.Kind journeyKind()`; `double journeyX()`, `journeyY()`, `journeyZ()`; `boolean pathEnded(double leaderX, double leaderZ)` (true = re-set the walk target, false = the walk ended twice in one place); `void arrive(long now)`; `boolean settling()`; `boolean settleDone(long now)`; `void endJourney(double leaderX, double leaderZ)`; `void addWait()`; `int waitedTicks()`; `boolean leaderWaiting()`; `void setLeaderWaiting(boolean waiting)`; `public void pinGraze(boolean pinned)`; `boolean grazePinned()`; `long nextJourneyTick()`, `void setNextJourneyTick(long)`, `long nextGrazeMoveTick()`, `void setNextGrazeMoveTick(long)`.
  - Herd area: `boolean hasCentre()`; `public double centreX()`; `public double centreZ()`; `void recentre(double x, double z)`; `int countOutside(boolean outside)`; `public double radius(double personalSpace, double bodyWidth)`.
  - Spots: `void reserveSpot(UUID member, double x, double z)`; `void releaseSpot(UUID member)`; `@Nullable HerdArea.Point spotOf(UUID member)`; `List<HerdArea.Point> reservedSpotsExcept(UUID member)`; `public double spotX(UUID member)`, `public double spotZ(UUID member)` (NaN without a spot).
  - Order, rank, layout: `List<UUID> travelOrder()`; `boolean orderDirty()`; `void setTravelOrder(List<UUID> followerOrder)`; `int rank(UUID member)` (leader 0, travel order 1.., others `Integer.MAX_VALUE`); `void refreshLayout(long now, double openSpacing, double tightSpacing)`; `public double targetArcOf(UUID member)`, `public double laneOffsetOf(UUID member)` (NaN when not laid out).
  - Shared caches: `boolean tryEvaluate(long now)`; `LeaderTrail trail()`; `FallbackBudget fallbacks()`; `BodyFit fit()`; `HerdBodies bodies()`; `public int mostFallbacksInOneTick()`.
  - Removed: `updateState`, `GRAZE_AFTER_TICKS`.
- Produces, elsewhere: `GroupManager.bodiesOf(DinoGroup group, ServerLevel level)` (package-private static, returns the refreshed `HerdBodies`); `GroupManager.WALKING_SPEED = 0.01`; `HerdSpacing.herdRadius(DinoGroup group, Dinosaur member)` (public static).

- [ ] **Step 1: Write the failing test**

Replace the whole file `test/.../social/DinoGroupTest.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DinoGroupTest {

    private static final double EPSILON = 1.0E-9D;

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

    private UUID animal(long id, float dominance) {
        UUID uuid = new UUID(0L, id);
        this.present.put(uuid, dominance);
        return uuid;
    }

    @Test
    void newGroupGrazes() {
        assertSame(HerdState.GRAZE, new DinoGroup(5).state());
    }

    @Test
    void leaderChangeClearsTheTrail() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID next = animal(2, 0.8F);
        group.add(leader, this.roster);
        group.add(next, this.roster);
        group.trail().record(0, 64, 0);
        group.trail().record(5, 64, 0);
        this.present.remove(leader);
        group.leader(this.roster);
        assertEquals(0, group.trail().size());
    }

    @Test
    void startJourneyEntersTravelAndClearsTrailAndSpots() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID follower = animal(2, 0.5F);
        group.add(leader, this.roster);
        group.add(follower, this.roster);
        group.trail().record(0, 64, 0);
        group.trail().record(5, 64, 0);
        group.reserveSpot(follower, 3D, 3D);
        assertTrue(group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(follower), 10L));
        assertSame(HerdState.TRAVEL, group.state());
        assertTrue(group.journeyActive());
        assertEquals(0, group.trail().size());
        assertNull(group.spotOf(follower));
        assertEquals(List.of(follower), group.travelOrder());
    }

    @Test
    void pinnedHerdRefusesJourneys() {
        DinoGroup group = new DinoGroup(5);
        group.pinGraze(true);
        assertFalse(group.startJourney(JourneyPolicy.Kind.URGENT, 40D, 64D, 0D, List.of(), 10L));
        assertSame(HerdState.GRAZE, group.state());
    }

    @Test
    void arriveSettlesThenEndJourneyGrazesAndRecentres() {
        DinoGroup group = new DinoGroup(5);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 0L);
        group.arrive(100L);
        assertTrue(group.settling());
        assertFalse(group.settleDone(100L + JourneyPolicy.SETTLE_TICKS - 1));
        assertTrue(group.settleDone(100L + JourneyPolicy.SETTLE_TICKS));
        group.endJourney(5D, 7D);
        assertSame(HerdState.GRAZE, group.state());
        assertFalse(group.journeyActive());
        assertEquals(5D, group.centreX(), EPSILON);
        assertEquals(7D, group.centreZ(), EPSILON);
    }

    @Test
    void leaderChangeEndsTheJourney() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID follower = animal(2, 0.5F);
        group.add(leader, this.roster);
        group.add(follower, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(follower), 0L);
        this.present.remove(leader);
        group.remove(leader);
        assertSame(HerdState.GRAZE, group.state());
        assertFalse(group.journeyActive());
    }

    @Test
    void thePathEndingTwiceInOnePlaceEndsTheJourney() {
        DinoGroup group = new DinoGroup(5);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 0L);
        assertTrue(group.pathEnded(0D, 0D), "the first end re-sets the walk target");
        assertTrue(group.pathEnded(5D, 0D), "the leader got on: again");
        assertFalse(group.pathEnded(5.2D, 0D), "no progress since the last end: arrival");
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 10L);
        assertTrue(group.pathEnded(5.2D, 0D), "a new journey starts afresh");
    }

    @Test
    void joinDuringTravelMarksTheOrderDirty() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        group.add(leader, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(), 0L);
        assertFalse(group.orderDirty());
        group.add(animal(2, 0.5F), this.roster);
        assertTrue(group.orderDirty());
    }

    @Test
    void leaveDropsSpotAndOrderPlace() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID b = animal(2, 0.5F);
        UUID c = animal(3, 0.4F);
        group.add(leader, this.roster);
        group.add(b, this.roster);
        group.add(c, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(b, c), 0L);
        group.reserveSpot(b, 1D, 1D);
        group.remove(b);
        assertNull(group.spotOf(b));
        assertEquals(List.of(c), group.travelOrder());
        assertTrue(group.orderDirty());
    }

    @Test
    void rankIsLeaderThenTravelOrder() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID b = animal(2, 0.5F);
        UUID c = animal(3, 0.4F);
        group.add(leader, this.roster);
        group.add(b, this.roster);
        group.add(c, this.roster);
        group.setTravelOrder(List.of(c, b));
        assertEquals(0, group.rank(leader));
        assertEquals(1, group.rank(c));
        assertEquals(2, group.rank(b));
        assertEquals(Integer.MAX_VALUE, group.rank(new UUID(0L, 99L)));
    }

    @Test
    void layoutGivesEachFollowerItsOwnTarget() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID b = animal(2, 0.5F);
        UUID c = animal(3, 0.4F);
        UUID d = animal(4, 0.3F);
        group.add(leader, this.roster);
        group.add(b, this.roster);
        group.add(c, this.roster);
        group.add(d, this.roster);
        group.startJourney(JourneyPolicy.Kind.ROAM, 40D, 64D, 0D, List.of(b, c, d), 0L);
        for (int x = 0; x <= 20; x++) {
            group.trail().record(x, 64D, 0D);
        }
        group.refreshLayout(1L, 5D, 4D);
        UUID[] followers = {b, c, d};
        for (int i = 0; i < followers.length; i++) {
            assertFalse(Double.isNaN(group.targetArcOf(followers[i])));
            for (int j = i + 1; j < followers.length; j++) {
                assertFalse(group.targetArcOf(followers[i]) == group.targetArcOf(followers[j])
                        && group.laneOffsetOf(followers[i]) == group.laneOffsetOf(followers[j]), "two followers share a target");
            }
        }
        assertTrue(Double.isNaN(group.targetArcOf(leader)), "the leader has no slot");
    }

    @Test
    void tenEvaluationsPerTickAtMost() {
        DinoGroup group = new DinoGroup(5);
        for (int i = 0; i < DinoGroup.MAX_EVALUATIONS_PER_TICK; i++) {
            assertTrue(group.tryEvaluate(7L));
        }
        assertFalse(group.tryEvaluate(7L));
        assertTrue(group.tryEvaluate(8L));
    }

    @Test
    void spotAccessorsAreNaNWithoutASpot() {
        DinoGroup group = new DinoGroup(5);
        UUID member = animal(1, 0.9F);
        group.add(member, this.roster);
        assertTrue(Double.isNaN(group.spotX(member)));
        group.reserveSpot(member, 3D, 4D);
        assertEquals(3D, group.spotX(member), EPSILON);
        assertEquals(4D, group.spotZ(member), EPSILON);
        group.releaseSpot(member);
        assertTrue(Double.isNaN(group.spotZ(member)));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors, `cannot find symbol ... startJourney`, `... reserveSpot`, `... refreshLayout`, `... pathEnded`.

- [ ] **Step 3: Write `DinoGroup`**

Replace the whole file `.../social/DinoGroup.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * One herd or pack: a capacity, the members and the leader, and the herd's movement state (spec: redesign).
 * <p>
 * {@link GroupManager} owns membership: only it adds or removes members, so the member set and its
 * index always agree. This class owns leadership. It works on UUIDs only and asks a {@link Roster}
 * which members are present and how dominant they are, so it holds no entity and no level, and a
 * unit test can drive it with a fake roster.
 * <p>
 * Movement state: GRAZE or TRAVEL, set by journeys ({@link #startJourney}, {@link #endJourney}), never
 * inferred from a path. The herd area has a fixed centre that moves only at the end of a journey or when
 * the leader has stood outside it ({@link #recentre}). Followers reserve graze spots here. A journey has a
 * travel order (nearest-first at the start, rebuilt when a member joins or leaves) and a per-tick layout of
 * target arcs and lane offsets. A leader change ends any journey. The group also holds the caches its
 * members share each tick: {@link BodyFit}, {@link HerdBodies}, the layout and the evaluation cap.
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

    /** A tick or time not set yet. */
    static final long UNSET = Long.MIN_VALUE;
    /** At most this many context-steering evaluations per herd per tick (spec: performance budget). */
    static final int MAX_EVALUATIONS_PER_TICK = 10;
    /** Lanes per row come from the tightest side room within this many blocks of the row's arc, so they do not flicker between trail points. */
    static final double ROW_ROOM_WINDOW = 2.0D;

    private final int capacity;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> memberView = Collections.unmodifiableSet(this.members);
    private @Nullable UUID leader;

    private HerdState state = HerdState.GRAZE;
    private final LeaderTrail trail = new LeaderTrail();
    private final FallbackBudget fallbacks = new FallbackBudget();
    private final BodyFit fit = new BodyFit();
    private final HerdBodies bodies = new HerdBodies();

    private boolean hasCentre;
    private double centreX;
    private double centreZ;
    private int outsideTicks;
    private final Map<UUID, HerdArea.Point> spots = new HashMap<>();

    private boolean journeyActive;
    private JourneyPolicy.Kind journeyKind = JourneyPolicy.Kind.ROAM;
    private double journeyX;
    private double journeyY;
    private double journeyZ;
    private long settleUntil = UNSET;
    private int waitedTicks;
    private boolean leaderWaiting;
    private boolean hasPathEnd;
    private double pathEndX;
    private double pathEndZ;
    private long nextJourneyTick = UNSET;
    private long nextGrazeMoveTick = UNSET;
    private boolean grazePinned;

    private final List<UUID> travelOrder = new ArrayList<>();
    private final List<UUID> travelOrderView = Collections.unmodifiableList(this.travelOrder);
    private boolean orderDirty;
    private double[] targetArcs = new double[0];
    private double[] laneOffsets = new double[0];
    private long layoutTick = UNSET;

    private long evaluationTick = UNSET;
    private int evaluations;

    DinoGroup(int capacity) {
        this.capacity = capacity;
    }

    int capacity() {
        return this.capacity;
    }

    public int size() {
        return this.members.size();
    }

    /** A read-only view of the member UUIDs. */
    Set<UUID> memberIds() {
        return this.memberView;
    }

    /** {@code true} if {@code member} is the stored leader. Does not check that the leader is present. */
    boolean isLeader(UUID member) {
        return member.equals(this.leader);
    }

    /**
     * Adds a member. A newcomer that outranks the present leader leads at once, so the leader is always
     * the best of the group, whatever the order of joining. If there is no present leader, the best
     * present member is elected now, so a leader exists from the moment the group forms. The contest
     * margin applies only to changes through growth ({@link #challenge}). A join during travel rebuilds the
     * travel order.
     */
    void add(UUID member, Roster roster) {
        this.members.add(member);
        if (this.state == HerdState.TRAVEL) {
            this.orderDirty = true;
        }
        if (this.leader == null || !roster.isPresent(this.leader)) {
            electLeader(roster);
        } else if (roster.isPresent(member)
                && Dominance.outranks(roster.dominance(member), member, roster.dominance(this.leader), this.leader)) {
            this.leader = member;
            onLeaderChange();
        }
    }

    /** Removes the member, its spot and its place in the order. If it led, the next {@link #leader} call elects a new one. */
    void remove(UUID member) {
        this.members.remove(member);
        this.fallbacks.forget(member);
        this.spots.remove(member);
        if (this.travelOrder.remove(member)) {
            this.layoutTick = UNSET;
        }
        if (this.state == HerdState.TRAVEL) {
            this.orderDirty = true;
        }
        if (member.equals(this.leader)) {
            this.leader = null;
            onLeaderChange();
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
        onLeaderChange();
        return true;
    }

    public HerdState state() {
        return this.state;
    }

    /**
     * Starts a journey: TRAVEL from this tick, a fresh trail, no graze spots, and the given travel order
     * (followers nearest-first). Refused while the herd is pinned to grazing.
     */
    boolean startJourney(JourneyPolicy.Kind kind, double x, double y, double z, List<UUID> followerOrder, long now) {
        if (this.grazePinned) {
            return false;
        }
        this.state = HerdState.TRAVEL;
        this.journeyActive = true;
        this.journeyKind = kind;
        this.journeyX = x;
        this.journeyY = y;
        this.journeyZ = z;
        this.settleUntil = UNSET;
        this.waitedTicks = 0;
        this.leaderWaiting = false;
        this.hasPathEnd = false;
        this.trail.clear();
        this.spots.clear();
        setTravelOrder(followerOrder);
        return true;
    }

    boolean journeyActive() {
        return this.journeyActive;
    }

    JourneyPolicy.Kind journeyKind() {
        return this.journeyKind;
    }

    double journeyX() {
        return this.journeyX;
    }

    double journeyY() {
        return this.journeyY;
    }

    double journeyZ() {
        return this.journeyZ;
    }

    /**
     * The leader's walk ended at (x, z) before the journey target (Review Focus 6: a vanilla walk reaches at
     * most the follow range and runs 150-249 ticks). {@code true} if the leader has moved at least
     * {@link JourneyPolicy#REISSUE_PROGRESS} since the previous end, or this is the first end, so the walk
     * target is set again; {@code false} when the walk ended twice in one place, which counts as arrival.
     */
    boolean pathEnded(double x, double z) {
        double dx = x - this.pathEndX;
        double dz = z - this.pathEndZ;
        boolean progressed = !this.hasPathEnd
                || dx * dx + dz * dz >= JourneyPolicy.REISSUE_PROGRESS * JourneyPolicy.REISSUE_PROGRESS;
        this.hasPathEnd = true;
        this.pathEndX = x;
        this.pathEndZ = z;
        return progressed;
    }

    /** The leader arrived: the herd stays in TRAVEL for {@link JourneyPolicy#SETTLE_TICKS} more ticks. */
    void arrive(long now) {
        if (this.journeyActive && this.settleUntil == UNSET) {
            this.settleUntil = now + JourneyPolicy.SETTLE_TICKS;
        }
        this.leaderWaiting = false;
    }

    boolean settling() {
        return this.settleUntil != UNSET;
    }

    boolean settleDone(long now) {
        return settling() && now >= this.settleUntil;
    }

    /** Ends the journey: GRAZE, and the herd area recentres on the leader. */
    void endJourney(double leaderX, double leaderZ) {
        this.state = HerdState.GRAZE;
        this.journeyActive = false;
        this.settleUntil = UNSET;
        this.leaderWaiting = false;
        this.waitedTicks = 0;
        recentre(leaderX, leaderZ);
    }

    void addWait() {
        this.waitedTicks++;
    }

    int waitedTicks() {
        return this.waitedTicks;
    }

    boolean leaderWaiting() {
        return this.leaderWaiting;
    }

    void setLeaderWaiting(boolean waiting) {
        this.leaderWaiting = waiting;
    }

    /** While pinned, no journey starts (roaming or urgent). For GameTests that need a grazing herd. */
    public void pinGraze(boolean pinned) {
        this.grazePinned = pinned;
    }

    boolean grazePinned() {
        return this.grazePinned;
    }

    long nextJourneyTick() {
        return this.nextJourneyTick;
    }

    void setNextJourneyTick(long tick) {
        this.nextJourneyTick = tick;
    }

    long nextGrazeMoveTick() {
        return this.nextGrazeMoveTick;
    }

    void setNextGrazeMoveTick(long tick) {
        this.nextGrazeMoveTick = tick;
    }

    boolean hasCentre() {
        return this.hasCentre;
    }

    public double centreX() {
        return this.centreX;
    }

    public double centreZ() {
        return this.centreZ;
    }

    /** Moves the herd area's centre (spec: only after a journey, or when the leader stood outside it). */
    void recentre(double x, double z) {
        this.hasCentre = true;
        this.centreX = x;
        this.centreZ = z;
        this.outsideTicks = 0;
    }

    /** Counts ticks the leader stands outside the herd area in a row; returns the count. */
    int countOutside(boolean outside) {
        this.outsideTicks = outside ? this.outsideTicks + 1 : 0;
        return this.outsideTicks;
    }

    /** Herd area radius for members of this personal space and body width. */
    public double radius(double personalSpace, double bodyWidth) {
        return HerdArea.radius(size(), HerdArea.grazeSpacing(personalSpace), bodyWidth);
    }

    void reserveSpot(UUID member, double x, double z) {
        this.spots.put(member, new HerdArea.Point(x, z));
    }

    void releaseSpot(UUID member) {
        this.spots.remove(member);
    }

    @Nullable HerdArea.Point spotOf(UUID member) {
        return this.spots.get(member);
    }

    /** The other members' reserved spots, as a new list (spot picks only). */
    List<HerdArea.Point> reservedSpotsExcept(UUID member) {
        List<HerdArea.Point> reserved = new ArrayList<>(this.spots.size());
        for (Map.Entry<UUID, HerdArea.Point> entry : this.spots.entrySet()) {
            if (!entry.getKey().equals(member)) {
                reserved.add(entry.getValue());
            }
        }
        return reserved;
    }

    /** x of the member's graze spot, NaN without one. Public for GameTests. */
    public double spotX(UUID member) {
        HerdArea.Point spot = this.spots.get(member);
        return spot == null ? Double.NaN : spot.x();
    }

    /** z of the member's graze spot, NaN without one. Public for GameTests. */
    public double spotZ(UUID member) {
        HerdArea.Point spot = this.spots.get(member);
        return spot == null ? Double.NaN : spot.z();
    }

    /** The followers in travel order, read-only. */
    List<UUID> travelOrder() {
        return this.travelOrderView;
    }

    boolean orderDirty() {
        return this.orderDirty;
    }

    /** Sets the travel order (followers nearest-first); the leader and non-members are skipped. */
    void setTravelOrder(List<UUID> followerOrder) {
        this.travelOrder.clear();
        for (UUID member : followerOrder) {
            if (this.members.contains(member) && !member.equals(this.leader) && !this.travelOrder.contains(member)) {
                this.travelOrder.add(member);
            }
        }
        this.orderDirty = false;
        this.layoutTick = UNSET;
    }

    /** Who goes first (spec "Who yields"): the leader 0, then the travel order from 1, others last. */
    int rank(UUID member) {
        if (member.equals(this.leader)) {
            return 0;
        }
        int index = this.travelOrder.indexOf(member);
        return index < 0 ? Integer.MAX_VALUE : index + 1;
    }

    /** Lays out the travel targets once per tick, from the trail's head (the leader) back (see {@link TravelOrder#layout}). */
    void refreshLayout(long now, double openSpacing, double tightSpacing) {
        if (this.layoutTick == now) {
            return;
        }
        this.layoutTick = now;
        int followers = this.travelOrder.size();
        if (this.targetArcs.length < followers) {
            this.targetArcs = new double[followers];
            this.laneOffsets = new double[followers];
        }
        double head = this.trail.headArc();
        if (Double.isNaN(head)) {
            Arrays.fill(this.targetArcs, 0, followers, Double.NaN);
            Arrays.fill(this.laneOffsets, 0, followers, 0D);
            return;
        }
        TravelOrder.layout(head, followers, openSpacing, tightSpacing, this::roomAt, this.targetArcs, this.laneOffsets);
    }

    /** The member's target arc from the latest layout; NaN if it has none. Public for GameTests. */
    public double targetArcOf(UUID member) {
        int index = this.travelOrder.indexOf(member);
        return index < 0 || this.layoutTick == UNSET ? Double.NaN : this.targetArcs[index];
    }

    /** The member's lane offset from the latest layout; NaN if it has none. Public for GameTests. */
    public double laneOffsetOf(UUID member) {
        int index = this.travelOrder.indexOf(member);
        return index < 0 || this.layoutTick == UNSET ? Double.NaN : this.laneOffsets[index];
    }

    /** {@code true} if one more context-steering evaluation fits this herd's budget for this tick. */
    boolean tryEvaluate(long now) {
        if (now != this.evaluationTick) {
            this.evaluationTick = now;
            this.evaluations = 0;
        }
        if (this.evaluations >= MAX_EVALUATIONS_PER_TICK) {
            return false;
        }
        this.evaluations++;
        return true;
    }

    LeaderTrail trail() {
        return this.trail;
    }

    FallbackBudget fallbacks() {
        return this.fallbacks;
    }

    BodyFit fit() {
        return this.fit;
    }

    HerdBodies bodies() {
        return this.bodies;
    }

    /** The most fallback paths this herd started in one tick. For GameTests. */
    public int mostFallbacksInOneTick() {
        return this.fallbacks.mostGrantedInOneTick();
    }

    /**
     * Side room on the tighter side near an arc: the tightest point within {@link #ROW_ROOM_WINDOW} either
     * way, so lanes per row do not flicker between trail points. NaN when not measured (no point, or either
     * side unknown anywhere in the window).
     */
    private double roomAt(double arc) {
        if (this.trail.size() == 0) {
            return Double.NaN;
        }
        return Math.min(this.trail.minRoom(arc - ROW_ROOM_WINDOW, arc + ROW_ROOM_WINDOW, true),
                this.trail.minRoom(arc - ROW_ROOM_WINDOW, arc + ROW_ROOM_WINDOW, false));
    }

    /** A new leader walks a new route: the trail goes, and a journey in progress ends (the herd grazes). */
    private void onLeaderChange() {
        this.trail.clear();
        this.layoutTick = UNSET;
        if (this.state == HerdState.TRAVEL || this.journeyActive) {
            this.state = HerdState.GRAZE;
            this.journeyActive = false;
            this.settleUntil = UNSET;
            this.leaderWaiting = false;
        }
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
        boolean changed = best == null ? this.leader != null : !best.equals(this.leader);
        this.leader = best;
        if (changed) {
            onLeaderChange();
        }
    }
}
```

- [ ] **Step 4: Add the snapshot to `GroupManager` and the radius helper to `HerdSpacing`**

In `.../social/GroupManager.java`, add after the `roster` method:

```java
    /** A member moving at least this many blocks per tick counts as walking in {@link HerdBodies}. */
    static final double WALKING_SPEED = 0.01D;

    /**
     * The herd's member snapshot for this tick, filled by the first call of the tick. Velocity is the move
     * since the previous tick ({@code x - xo}); the one member whose own tick is running reads 0 until it
     * moves, which only weakens others' prediction of it for one tick.
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
            if (dinosaur == null) {
                continue;
            }
            double vx = dinosaur.getX() - dinosaur.xo;
            double vz = dinosaur.getZ() - dinosaur.zo;
            bodies.add(member, dinosaur.getX(), dinosaur.getZ(), vx, vz, dinosaur.getBbWidth(), dinosaur.isResting(),
                    vx * vx + vz * vz > WALKING_SPEED * WALKING_SPEED, group.rank(member));
        }
        return bodies;
    }
```

In `.../social/HerdSpacing.java`, add after `hardMinimumOf`:

```java
    /** The herd area radius for a member of this herd. Public for GameTests. */
    public static double herdRadius(DinoGroup group, Dinosaur member) {
        return group.radius(personalSpace(member.getBbWidth(), member.getBbWidth()), member.getBbWidth());
    }
```

- [ ] **Step 5: Update `HerdLeading` (interim: explicit state, so only record the trail while travelling)**

Replace the whole file `.../social/HerdLeading.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;

/**
 * The leader's part of herd movement, called from every dinosaur's server tick: it returns at once for
 * non-leaders. Interim version: the herd state is explicit now (journeys start it, Task 15), so this only
 * records the trail while travelling. Task 15 replaces this class.
 */
public final class HerdLeading {

    private HerdLeading() {
    }

    public static void tick(Dinosaur dinosaur) {
        DinoGroup group = GroupManager.groupOf(dinosaur);
        if (group == null || !group.isLeader(dinosaur.getUUID()) || group.state() != HerdState.TRAVEL) {
            return;
        }
        group.trail().record(dinosaur.getX(), dinosaur.getY(), dinosaur.getZ());
    }
}
```

- [ ] **Step 6: Run the tests to verify they pass**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: every `DinoGroupTest` line `PASS`; `GroupManagerTest` still passes; last line `141 run, 0 failed` (132 - 4 old + 13).

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL` (the GameTests use only `GroupManager.groupOf`, `DinoGroup.size` and `mostFallbacksInOneTick` so far).

- [ ] **Step 7: Checkpoint (no commit)**

Run: `git status --short`
Expected: `DinoGroup.java`, `GroupManager.java`, `HerdSpacing.java`, `HerdLeading.java`, `DinoGroupTest.java` modified; nothing staged.

### Task 12: Move control: context steering and the guard

**Most capable model.** Spec "Context steering" and "Guard": the one local mover for all follower moves, and a per-tick guard in every mode (steering, path, leader). A follower's steering re-evaluates every 2 ticks on a fixed stagger by entity id, at most 10 evaluations per herd per tick; a member the budget refused is due again the next tick (so the members that tick last are not starved), and the others keep their last heading. Each evaluation probes the clearance box 1 block ahead on all 12 headings and predicts members 10 ticks ahead (`HerdBodies.wouldCrowd`); then only the previous heading and the `DEEP_PROBES` (4) best open headings get the 2-block probe (mask) and the 3-block probe (danger), and the other open headings count as masked. That is at most 22 `BodyFit.canStand` calls per evaluation instead of 36, and the stand cache of Task 10 keeps results 20 ticks, so a column of followers shares them. After vanilla `MoveControl.tick` has turned the body (10 degrees per tick) and set the forward input, the guard tests the box 0.6 blocks ahead along the real yaw (`BodyFit.fits`, collision only) and the members' hard minimum (`HerdBodies.blockerAt`); on a hit the forward input is 0 for this tick and the horizontal velocity is cut to `GUARD_VELOCITY_FACTOR` of itself, so the body does not slide on into the wall or the member. A stop because every heading is blocked by blocks is not "waiting"; `blockedTicks` counts such ticks, so a dead end feeds the follower's stuck rule and leads to a fallback path (lead wall feedback, decision 14). The constructor comment about pushing is fixed (spec Components `Dinosaur`).

Cost estimate (spec budget: under 1 ms average, 5 ms worst): an uncached stand probe is one `noCollision` on the box, 9 hazard reads and 9 to 36 ground reads, about 8 microseconds; an evaluation is at most 22 probes, so at most 0.18 ms uncached. With 10 evaluations per tick and about half of the probes hitting the 20-tick cache in a column, TRAVEL costs about 0.5 to 1 ms per tick, GRAZE (3 evaluations) about 0.2 ms; a tick with two spot picks (8 straight-line reaches each) stays under 3 ms. Task 21 item 8 measures it.

**Files:**
- Modify: `.../social/HerdMoveControl.java` (whole file), `common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java:141` (comment)

**Interfaces:**
- Consumes: `ContextSteering` (Task 2), `HerdBodies` (Task 3), `HerdSpacing.applies`, `HerdSpacing.personalSpace`, `HerdArea.grazeSpacing` (Task 4), `BodyFit.boxOf`, `BodyFit.canStand`, `BodyFit.fits` (Task 10), `DinoGroup.tryEvaluate`, `DinoGroup.fit`, `DinoGroup.state` (Task 11), `GroupManager.groupOf`, `GroupManager.bodiesOf` (Task 11), `HerdTimings.record`.
- Produces (on `HerdMoveControl`, public class): constants `ARRIVED = 1.0`, `GUARD_AHEAD = 0.6`, `GUARD_VELOCITY_FACTOR = 0.2`, `PROBE_NEAR = 1.0`, `PROBE_MID = 2.0`, `PROBE_FAR = 3.0`, `DEEP_PROBES = 4`, `EVALUATE_EVERY_TICKS = 2`, `WANTED_AHEAD = 2.0`, `GROUND_SPEED_FACTOR = 2.2`, `BOX_REFRESH_TICKS = 100`, `PATH_PROGRESS_SPEED = 0.05`; `public void steerTo(double x, double y, double z, double speed)` (speed 0 = wait in place); `public void stopSteering()`; `public boolean isSteering()`; `public boolean isWaiting()`; `public int blockedTicks()` (ticks in a row every heading was masked by blocks while steering); `public long lastProgressTick()`; `void markProgress(long gameTime)`; `public int wallBlockedTicks()`; `ClearanceBox box()`; private `void moveToward(ServerLevel, DinoGroup, HerdBodies, int self, double targetX, double targetZ, double speed)` and private `int guardBlocker` (both extended in Task 17).

- [ ] **Step 1: Write the move control**

Replace the whole file `.../social/HerdMoveControl.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Vanilla movement plus the herd's per-tick layer (spec: redesign, "Context steering" and "Guard"):
 * <ul>
 *   <li><b>Stop</b>: no wanted position; vanilla sets no forward input.</li>
 *   <li><b>Steer</b> ({@link #steerTo}): every {@link #EVALUATE_EVERY_TICKS} ticks on a fixed stagger by
 *       entity id (at most {@link DinoGroup#MAX_EVALUATIONS_PER_TICK} per herd per tick; a member the budget
 *       refused is due again the next tick) the 12 headings of {@link ContextSteering} are scored. A heading
 *       is masked when the clearance box cannot stand {@link #PROBE_NEAR} blocks ahead ({@link BodyFit}: fit,
 *       ground, hazards) or when it would bring the body inside a member's hard minimum within 10 ticks
 *       ({@link HerdBodies}). The previous heading and the {@link #DEEP_PROBES} best open headings are also
 *       probed {@link #PROBE_MID} (mask) and {@link #PROBE_FAR} (danger) blocks ahead; the other open
 *       headings are not probed and count as masked. Between evaluations the last heading is kept.</li>
 *   <li><b>Path</b>: navigation sets the wanted position (the leader, fallback paths, needs). Navigation
 *       always wins over steering.</li>
 * </ul>
 * After vanilla has turned the body (at most 10 degrees per tick) and set the forward input, the
 * <b>guard</b> tests the clearance box {@link #GUARD_AHEAD} blocks ahead along the real body yaw, in every
 * mode: if it does not fit, or it enters a member's hard minimum, the forward input is 0 for this tick and
 * the horizontal velocity is cut to {@link #GUARD_VELOCITY_FACTOR} of itself (vanilla friction alone would
 * let the body slide on about 0.7 blocks). So spacing (T4) and fitting (T8) hold between evaluations and on
 * vanilla paths too. Herd work is timed.
 */
public class HerdMoveControl extends MoveControl {

    /** Steering stops this close to its target. */
    static final double ARRIVED = 1.0D;
    /** The guard tests the box this far ahead along the body yaw (spec: 0.6). */
    static final double GUARD_AHEAD = 0.6D;
    /** When the guard fires, the body keeps this fraction of its horizontal velocity. */
    static final double GUARD_VELOCITY_FACTOR = 0.2D;
    static final double PROBE_NEAR = 1.0D;
    static final double PROBE_MID = 2.0D;
    static final double PROBE_FAR = 3.0D;
    /** Open headings probed 2 and 3 blocks ahead, besides the previous heading: the best by interest. */
    static final int DEEP_PROBES = 4;
    static final int EVALUATE_EVERY_TICKS = 2;
    /** The wanted position lies this far ahead along the chosen heading. */
    static final double WANTED_AHEAD = 2.0D;
    /** Blocks per tick per unit of {@code setSpeed} on ground (vanilla friction 0.6: about 1 / (1 - 0.546)). */
    static final double GROUND_SPEED_FACTOR = 2.2D;
    /** The clearance box is rebuilt this often (growth changes it slowly). */
    static final int BOX_REFRESH_TICKS = 100;
    /** On a path, moving at least this many blocks per tick counts as progress. */
    static final double PATH_PROGRESS_SPEED = 0.05D;

    private final Dinosaur dinosaur;
    private final boolean[] masked = new boolean[ContextSteering.HEADINGS];
    private final double[] danger = new double[ContextSteering.HEADINGS];
    /** Headings probed 2 and 3 blocks ahead in the current evaluation. */
    private final boolean[] deep = new boolean[ContextSteering.HEADINGS];
    private boolean steering;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double targetSpeed;
    private int heading = ContextSteering.STOP;
    private double freeDistance = PROBE_FAR;
    /** The last evaluation found a heading free of blocks that only members masked. */
    private boolean yieldingOnly;
    /** The herd's evaluation budget refused this animal last time it was due: it is due again now. */
    private boolean overdue;
    private boolean waiting;
    private int blockedTicks;
    private long lastProgressTick;
    private int wallBlockedTicks;
    /** Index in this tick's {@link HerdBodies} of the member the guard stopped for, or -1. */
    private int guardBlocker = -1;
    private @Nullable ClearanceBox box;
    private int boxAge;

    public HerdMoveControl(Dinosaur dinosaur) {
        super(dinosaur);
        this.dinosaur = dinosaur;
    }

    /**
     * Steer toward (x, z) at the speed modifier {@code speed} until {@link #stopSteering}. Called every tick
     * by the follower behaviour. Speed 0 means "wait where you stand" (counts as waiting).
     */
    public void steerTo(double x, double y, double z, double speed) {
        this.steering = true;
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.targetSpeed = speed;
    }

    public void stopSteering() {
        if (this.steering) {
            this.steering = false;
            this.operation = Operation.WAIT;
        }
        this.heading = ContextSteering.STOP;
        this.waiting = false;
        this.blockedTicks = 0;
    }

    /** {@code true} while herd steering moves the animal. Read by the stamina rule in {@code Dinosaur}. */
    public boolean isSteering() {
        return this.steering;
    }

    /** {@code true} if the animal stood this tick on purpose: held back (speed 0) or yielding to members. */
    public boolean isWaiting() {
        return this.waiting;
    }

    /** Ticks in a row that steering stood because every heading was masked by blocks (not by members). */
    public int blockedTicks() {
        return this.blockedTicks;
    }

    /** Game time of the last progress, wait or path move. Public for GameTests. */
    public long lastProgressTick() {
        return this.lastProgressTick;
    }

    void markProgress(long gameTime) {
        this.lastProgressTick = gameTime;
    }

    /** Ticks in a row the guard stopped the animal at blocks (a wall or trunk). Public for GameTests. */
    public int wallBlockedTicks() {
        return this.wallBlockedTicks;
    }

    /** This animal's clearance box, rebuilt every {@link #BOX_REFRESH_TICKS} ticks. */
    ClearanceBox box() {
        if (this.box == null || ++this.boxAge >= BOX_REFRESH_TICKS) {
            this.box = BodyFit.boxOf(this.dinosaur);
            this.boxAge = 0;
        }
        return this.box;
    }

    @Override
    public void tick() {
        ServerLevel level = (ServerLevel) this.mob.level();
        long start = System.nanoTime();
        level.getProfiler().push("herdMove");
        if (this.mob.getNavigation().isInProgress()) {
            this.steering = false;
            this.waiting = false;
        }
        DinoGroup group = HerdSpacing.applies(this.dinosaur) ? GroupManager.groupOf(this.dinosaur) : null;
        HerdBodies bodies = group == null ? null : GroupManager.bodiesOf(group, level);
        int self = bodies == null ? -1 : bodies.indexOf(this.dinosaur.getUUID());
        if (self >= 0 && this.steering) {
            moveToward(level, group, bodies, self, this.targetX, this.targetZ, this.targetSpeed);
        }
        super.tick();
        if (self >= 0) {
            guard(level, group, bodies, self);
        } else {
            this.wallBlockedTicks = 0;
            this.guardBlocker = -1;
        }
        if (this.mob.getNavigation().isInProgress()
                && this.mob.getDeltaMovement().horizontalDistanceSqr() > PATH_PROGRESS_SPEED * PATH_PROGRESS_SPEED) {
            markProgress(level.getGameTime());
        }
        level.getProfiler().pop();
        HerdTimings.record(level.getGameTime(), System.nanoTime() - start);
    }

    /** One tick of context steering toward (targetX, targetZ): evaluate when due, then walk the heading. */
    private void moveToward(ServerLevel level, DinoGroup group, HerdBodies bodies, int self,
                            double targetX, double targetZ, double speed) {
        double dx = targetX - this.mob.getX();
        double dz = targetZ - this.mob.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < ARRIVED) {
            this.heading = ContextSteering.STOP;
            this.waiting = false;
            this.blockedTicks = 0;
            this.operation = Operation.WAIT;
            return;
        }
        if (speed <= 0D) {
            this.waiting = true;
            this.blockedTicks = 0;
            this.operation = Operation.WAIT;
            return;
        }
        // A fixed stagger; a refused member is due again next tick, so the members that tick last are not
        // starved by the herd's cap. A stopped member re-evaluates on the same cadence, not every tick.
        boolean due = this.overdue || (this.dinosaur.tickCount + this.dinosaur.getId()) % EVALUATE_EVERY_TICKS == 0;
        if (due) {
            if (group.tryEvaluate(level.getGameTime())) {
                this.overdue = false;
                this.heading = evaluate(level, group, bodies, self, dx, dz, speed);
            } else {
                this.overdue = true;
            }
        }
        if (this.heading == ContextSteering.STOP) {
            // Stopped by members only: waiting (not stuck). Stopped by blocks: not waiting, and counted,
            // so the follower asks for a fallback path (decision 14: blocked by blocks is stuck).
            this.waiting = this.yieldingOnly;
            this.blockedTicks = this.yieldingOnly ? 0 : this.blockedTicks + 1;
            this.operation = Operation.WAIT;
            return;
        }
        this.waiting = false;
        this.blockedTicks = 0;
        double wanted = speed * ContextSteering.speedFactor(distance, this.freeDistance);
        this.setWantedPosition(this.mob.getX() + ContextSteering.dirX(this.heading) * WANTED_AHEAD, this.mob.getY(),
                this.mob.getZ() + ContextSteering.dirZ(this.heading) * WANTED_AHEAD, wanted);
    }

    /**
     * Scores the 12 headings (spec steps 1-5) and returns the pick; also sets the free distance for the speed.
     * Stage 1 probes every heading one block ahead and the members. Stage 2 probes only the previous heading
     * (if open) and the {@link #DEEP_PROBES} best open headings two blocks ahead (mask) and three blocks ahead
     * (danger); the other open headings are not probed and count as masked. So a wall ahead still leaves the
     * headings along the wall, and a dead end still gives "stop", at a third of the probes.
     */
    private int evaluate(ServerLevel level, DinoGroup group, HerdBodies bodies, int self, double dx, double dz, double speed) {
        ClearanceBox box = box();
        BodyFit fit = group.fit();
        double x = this.mob.getX();
        double y = this.mob.getY();
        double z = this.mob.getZ();
        float yaw = this.mob.getYRot();
        double perTick = speed * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * GROUND_SPEED_FACTOR;
        double width = this.mob.getBbWidth();
        double spacing = HerdArea.grazeSpacing(HerdSpacing.personalSpace(width, width));
        // Spec danger "a member within the graze spacing" applies while grazing; travel slots are 4 to 5 apart,
        // so in TRAVEL it would push every follower off the line behind the member ahead.
        boolean memberDanger = group.state() == HerdState.GRAZE;
        this.yieldingOnly = false;
        for (int h = 0; h < ContextSteering.HEADINGS; h++) {
            double hx = ContextSteering.dirX(h);
            double hz = ContextSteering.dirZ(h);
            boolean open = fit.canStand(level, this.mob, box, x + hx * PROBE_NEAR, y, z + hz * PROBE_NEAR);
            boolean crowded = bodies.wouldCrowd(self, hx * perTick, hz * perTick);
            this.masked[h] = !open || crowded;
            if (open && crowded) {
                this.yieldingOnly = true;
            }
            this.danger[h] = !this.masked[h] && memberDanger
                    && bodies.anyWithin(self, x + hx * PROBE_NEAR, z + hz * PROBE_NEAR, spacing)
                    ? ContextSteering.DANGER_NEAR_MEMBER : 0D;
            this.deep[h] = false;
        }
        int next = this.heading >= 0 && !this.masked[this.heading] ? this.heading : -1;
        for (int n = 0; n <= DEEP_PROBES; n++) {
            if (next < 0) {
                double bestScore = Double.NEGATIVE_INFINITY;
                for (int h = 0; h < ContextSteering.HEADINGS; h++) {
                    if (!this.masked[h] && !this.deep[h]) {
                        double score = ContextSteering.interest(h, dx, dz, yaw) - this.danger[h];
                        if (score > bestScore) {
                            bestScore = score;
                            next = h;
                        }
                    }
                }
                if (next < 0) {
                    break;
                }
            }
            this.deep[next] = true;
            double hx = ContextSteering.dirX(next);
            double hz = ContextSteering.dirZ(next);
            if (!fit.canStand(level, this.mob, box, x + hx * PROBE_MID, y, z + hz * PROBE_MID)) {
                this.masked[next] = true;
            } else if (!fit.canStand(level, this.mob, box, x + hx * PROBE_FAR, y, z + hz * PROBE_FAR)) {
                this.danger[next] += ContextSteering.DANGER_BLOCKED_FAR;
            }
            next = -1;
        }
        for (int h = 0; h < ContextSteering.HEADINGS; h++) {
            if (!this.deep[h]) {
                this.masked[h] = true;
            }
        }
        int pick = ContextSteering.pick(dx, dz, yaw, this.heading, this.masked, this.danger);
        if (pick != ContextSteering.STOP) {
            double blocks = this.danger[pick] >= ContextSteering.DANGER_BLOCKED_FAR ? PROBE_MID : PROBE_FAR;
            this.freeDistance = Math.min(blocks, bodies.freeDistance(self, ContextSteering.dirX(pick), ContextSteering.dirZ(pick), PROBE_FAR));
        }
        return pick;
    }

    /**
     * The guard (spec): no forward input this tick if the box 0.6 ahead along the real yaw does not fit or
     * enters a hard minimum. The horizontal velocity is cut too: with friction alone a body at 0.65 blocks
     * per tick slides on about 0.7 blocks, past the probe.
     */
    private void guard(ServerLevel level, DinoGroup group, HerdBodies bodies, int self) {
        this.guardBlocker = -1;
        if (this.mob.zza <= 0F) {
            this.wallBlockedTicks = 0;
            return;
        }
        double yaw = Math.toRadians(this.mob.getYRot());
        double aheadX = this.mob.getX() - Math.sin(yaw) * GUARD_AHEAD;
        double aheadZ = this.mob.getZ() + Math.cos(yaw) * GUARD_AHEAD;
        boolean blockFree = group.fit().fits(level, this.mob, box(), aheadX, this.mob.getY(), aheadZ);
        int blocker = bodies.blockerAt(self, aheadX, aheadZ);
        if (!blockFree || blocker >= 0) {
            this.mob.setSpeed(0F);
            Vec3 velocity = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(velocity.x * GUARD_VELOCITY_FACTOR, velocity.y, velocity.z * GUARD_VELOCITY_FACTOR);
        }
        this.wallBlockedTicks = blockFree ? 0 : this.wallBlockedTicks + 1;
        this.guardBlocker = blocker;
    }
}
```

- [ ] **Step 2: Fix the pushing comment in `Dinosaur`**

In `Dinosaur.java`, replace:

```java
        // Herd members bend their steps around each other (HerdSpacing); vanilla pushing stays on as a backstop.
```

with:

```java
        // Herd members steer round each other and never step inside a hard minimum (HerdMoveControl: context
        // steering masks and the guard). Vanilla pushing stays on as a backstop for overlaps from spawning,
        // shoves and teleports (turning it off made members stack in playtest 2).
```

- [ ] **Step 3: Compile and run the unit tests**

Run: `./gradlew :common:compileJava`
Expected: `BUILD SUCCESSFUL`.

Run: `sh tools/junit-standin/run.sh`
Expected: last line `141 run, 0 failed`.

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Self-check against the spec**

Read the code once more and confirm: every herd branch goes through `HerdSpacing.applies` (the group is fetched only when it holds); the guard runs after `super.tick()` in every mode and cuts the velocity when it fires; a heading masked by blocks and by members both count as masks, and an open heading that was not probed 2 blocks ahead counts as masked; the per-herd cap (`tryEvaluate`) is asked before each evaluation and a refused member is due the next tick; `blockedTicks` grows only on stops that members did not cause; no collection is allocated per tick.

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`
Expected: `HerdMoveControl.java` and `Dinosaur.java` modified; nothing staged.

### Task 13: Follower behaviour, grazing

**Most capable model.** The follower behaviour comes back (spec Components `FollowFormation`: "Picks the target per state, progress monitor, fallback requests"), first with GRAZE. A grazing follower holds a reserved spot (spec "Graze", lead decision 6): on entering GRAZE it picks after a 0-60 tick stagger; it samples 8 candidates inside the herd area and takes the roomiest that is farther than the graze spacing from every reserved spot (the leader's position counts as reserved) and that its clearance box reaches in a straight line; it holds the spot 600-1800 ticks, then moves near it with a 50% chance. Within 1.5 blocks of its spot it stands (eat, rest and idle animations run as before). Only spots within the fallback range (24 blocks) are picked; a spot farther away (after a journey) is dropped and the member first walks back inside the herd area. Progress is measured as minus the distance to the spot; stuck (the 40-tick window, or every heading masked by blocks for 10 ticks: decision 14) leads to a budgeted fallback path to the spot, asked for every tick for one window because `FallbackBudget` queues only members that keep asking; two failed fallbacks pick a new spot. A member without a spot that stands outside the herd area walks inside it (Review Focus 1), with the same stuck rule and a fallback path to the centre (the leader stood there). TRAVEL is added in Task 14; until then a travelling follower stands.

**Files:**
- Create: `.../social/FollowFormation.java`
- Modify: `common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java` (import and core task line)

**Interfaces:**
- Consumes: `GrazeSchedule`, `ProgressMonitor` (Task 5), `HerdArea` (Task 4), `BodyFit.reachY` (Task 10), `DinoGroup` spots, centre, radius, fallbacks, state (Task 11), `HerdMoveControl.steerTo`, `stopSteering`, `isWaiting`, `blockedTicks`, `markProgress`, `box` (Task 12), `HerdSpacing.applies`, `HerdSpacing.personalSpace`, `GroupManager.groupOf`, `GroupManager.leaderOf`, `FallbackBudget.tryAcquire`, `HerdTimings.record`.
- Produces: `public class FollowFormation<E extends Dinosaur> extends ExtendedBehaviour<E>` with constants `GRAZE_SPEED = 0.5`, `SPOT_TOLERANCE = 1.5`, `FAILED_FALLBACKS_BEFORE_REPICK = 2`, `FALLBACK_RANGE = 24`, `FALLBACK_MIN_DISTANCE = 4.0`, `FALLBACK_CLOSE_ENOUGH = 2`, `EDGE_MARGIN = 1.0`, `BLOCKED_FALLBACK_TICKS = 10`; private `void travel(ServerLevel level, E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control, long now)` (filled in Task 14); private `void onStateChange(E entity, long now)` (extended in Task 14); private `boolean isStuck(HerdMoveControl control, double measure, long now)` (true while the member should ask for a fallback path); private `void walkTo(ServerLevel, E, DinoGroup, HerdMoveControl, double x, double y, double z, double speed, long now)`; private `boolean requestFallback(ServerLevel, E, DinoGroup, HerdMoveControl, double x, double y, double z, double speed)`; private `static double horizontalDistance(Dinosaur, double x, double z)`.

- [ ] **Step 1: Write the behaviour (graze part)**

Create `.../social/FollowFormation.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * How a follower moves, per herd state (spec: redesign). Each tick it picks a target and hands it to
 * {@link HerdMoveControl}, which steers round blocks and members:
 * <ul>
 *   <li><b>Graze</b>: hold a reserved spot ({@link GrazeSchedule}): the roomiest of
 *       {@link HerdArea#SPOT_CANDIDATES} candidates inside the herd area that is farther than the graze
 *       spacing from every reserved spot and reachable in a straight line by the clearance box.</li>
 *   <li><b>Travel</b>: see {@link #travel}.</li>
 *   <li><b>Stuck</b> ({@link ProgressMonitor}, or every heading masked by blocks for
 *       {@link #BLOCKED_FALLBACK_TICKS}): a fallback vanilla path to a fixed point, within the herd's
 *       {@link FallbackBudget}, asked for every tick for one window. Setting the walk target ends this
 *       behaviour until the path is done.</li>
 * </ul>
 * Followers only: the leader moves by its own path. Each brain gets its own instance (task lists are
 * built per entity), so the fields below are per animal.
 */
public class FollowFormation<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));

    static final double GRAZE_SPEED = 0.5D;
    /** A grazing follower within this distance of its spot stands. */
    static final double SPOT_TOLERANCE = 1.5D;
    /** Spec: two failed fallback paths in graze pick a new spot. */
    static final int FAILED_FALLBACKS_BEFORE_REPICK = 2;
    /**
     * Spec: fallback range. Only spots this close are picked; a spot farther away (after a journey) is
     * dropped, and the member walks back inside the herd area before it picks again.
     */
    static final double FALLBACK_RANGE = 24D;
    /**
     * No fallback this close to the target: beyond the path's close-enough ({@link #FALLBACK_CLOSE_ENOUGH},
     * Manhattan between block positions), so a granted path is never "already reached" when it starts.
     */
    static final double FALLBACK_MIN_DISTANCE = 4.0D;
    static final int FALLBACK_CLOSE_ENOUGH = 2;
    /** A member without a spot standing outside the herd area walks in until it is this far inside the edge. */
    static final double EDGE_MARGIN = 1.0D;
    /** Lead decision 14: every heading masked by blocks for this many ticks is stuck at once, without the 40-tick window. */
    static final int BLOCKED_FALLBACK_TICKS = 10;
    private static final long UNSET = Long.MIN_VALUE;

    private final GrazeSchedule schedule = new GrazeSchedule();
    private final ProgressMonitor progress = new ProgressMonitor();
    private @Nullable HerdState lastState;
    private double spotY;
    private int fallbacksForSpot;
    /** Until this tick the member asks the herd budget for a fallback path every tick (the budget queues members that keep asking). */
    private long askingUntil = UNSET;

    public FollowFormation() {
        noTimeout();
        cooldownFor(entity -> 10);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return canFollow(entity);
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return canFollow(entity) && !BrainUtils.hasMemory(entity, MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void start(E entity) {
        this.progress.reset(Double.NaN, entity.level().getGameTime());
    }

    @Override
    protected void stop(E entity) {
        HerdMoveControl control = control(entity);
        if (control != null) {
            control.stopSteering();
        }
    }

    @Override
    protected void tick(E entity) {
        ServerLevel level = (ServerLevel) entity.level();
        long start = System.nanoTime();
        level.getProfiler().push("herdFollow");
        follow(level, entity);
        level.getProfiler().pop();
        HerdTimings.record(level.getGameTime(), System.nanoTime() - start);
    }

    private static boolean canFollow(Dinosaur dinosaur) {
        DinoGroup group = GroupManager.groupOf(dinosaur);
        return group != null && !group.isLeader(dinosaur.getUUID()) && dinosaur.isIdle() && HerdSpacing.applies(dinosaur);
    }

    private static @Nullable HerdMoveControl control(Dinosaur dinosaur) {
        return dinosaur.getMoveControl() instanceof HerdMoveControl control ? control : null;
    }

    private void follow(ServerLevel level, E entity) {
        DinoGroup group = GroupManager.groupOf(entity);
        HerdMoveControl control = control(entity);
        if (group == null || control == null) {
            return;
        }
        Dinosaur leader = GroupManager.leaderOf(group, level);
        if (leader == null || leader == entity) {
            control.stopSteering();
            return;
        }
        long now = level.getGameTime();
        if (group.state() != this.lastState) {
            this.lastState = group.state();
            onStateChange(entity, now);
        }
        if (group.state() == HerdState.TRAVEL) {
            travel(level, entity, group, leader, control, now);
        } else {
            graze(level, entity, group, leader, control, now);
        }
    }

    private void onStateChange(E entity, long now) {
        this.progress.reset(Double.NaN, now);
        this.fallbacksForSpot = 0;
        this.askingUntil = UNSET;
        if (this.lastState == HerdState.GRAZE) {
            this.schedule.enter(now, entity.getRandom().nextDouble());
        }
    }

    /** Travel is added in Task 14; until then a travelling follower stands. */
    private void travel(ServerLevel level, E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control, long now) {
        control.stopSteering();
    }

    private void graze(ServerLevel level, E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control, long now) {
        if (!group.hasCentre()) {
            control.stopSteering();
            return;
        }
        double width = entity.getBbWidth();
        double personalSpace = HerdSpacing.personalSpace(width, width);
        double spacing = HerdArea.grazeSpacing(personalSpace);
        double radius = group.radius(personalSpace, width);
        double centreX = group.centreX();
        double centreZ = group.centreZ();
        RandomSource random = entity.getRandom();

        HerdArea.Point spot = group.spotOf(entity.getUUID());
        if (spot != null && horizontalDistance(entity, spot.x(), spot.z()) > FALLBACK_RANGE) {
            // Too far for a fallback path (a straggler after a journey): drop it. The no-spot branch below
            // walks back inside the herd area, and the next pick takes only spots within range.
            group.releaseSpot(entity.getUUID());
            this.schedule.noSpot(now);
            spot = null;
        }
        boolean valid = spot != null && HerdArea.inside(centreX, centreZ, radius, spot.x(), spot.z());
        GrazeSchedule.Action action = this.schedule.decide(now, spot != null, valid, random::nextDouble);
        if (action != GrazeSchedule.Action.HOLD) {
            HerdArea.Point near = action == GrazeSchedule.Action.PICK_NEAR ? spot : null;
            Vec3 picked = pickSpot(level, entity, group, leader, control, centreX, centreZ, radius, spacing, near);
            if (picked != null) {
                group.reserveSpot(entity.getUUID(), picked.x, picked.z);
                this.spotY = picked.y;
                this.schedule.spotTaken(now, random::nextDouble);
                this.fallbacksForSpot = 0;
                this.progress.reset(Double.NaN, now);
            } else if (action == GrazeSchedule.Action.PICK_NEAR) {
                this.schedule.spotTaken(now, random::nextDouble);
            } else {
                group.releaseSpot(entity.getUUID());
                this.schedule.noSpot(now);
            }
            spot = group.spotOf(entity.getUUID());
        }

        if (spot == null) {
            // No spot: stand still and try again later (spec), but first walk into the herd area if outside
            // it (Review Focus 1). The centre is where the leader stood, so a fallback path can go there.
            if (!HerdArea.inside(centreX, centreZ, radius - EDGE_MARGIN, entity.getX(), entity.getZ())) {
                walkTo(level, entity, group, control, centreX, leader.getY(), centreZ, GRAZE_SPEED, now);
            } else {
                control.stopSteering();
                control.markProgress(now);
                this.progress.reset(Double.NaN, now);
                this.askingUntil = UNSET;
            }
            return;
        }
        double distance = horizontalDistance(entity, spot.x(), spot.z());
        if (distance <= SPOT_TOLERANCE) {
            control.stopSteering();
            this.progress.reset(-distance, now);
            control.markProgress(now);
            this.askingUntil = UNSET;
            return;
        }
        if (isStuck(control, -distance, now)) {
            if (distance <= FALLBACK_MIN_DISTANCE) {
                // Stuck close by: this place is the spot now (stay put).
                group.reserveSpot(entity.getUUID(), entity.getX(), entity.getZ());
                this.spotY = entity.getY();
                this.askingUntil = UNSET;
                control.stopSteering();
                return;
            }
            if (this.fallbacksForSpot >= FAILED_FALLBACKS_BEFORE_REPICK) {
                group.releaseSpot(entity.getUUID());
                this.schedule.repickNow(now);
                this.fallbacksForSpot = 0;
                this.askingUntil = UNSET;
                control.stopSteering();
                return;
            }
            if (requestFallback(level, entity, group, control, spot.x(), this.spotY, spot.z(), GRAZE_SPEED)) {
                this.fallbacksForSpot++;
                this.askingUntil = UNSET;
            }
            return;
        }
        control.steerTo(spot.x(), this.spotY, spot.z(), GRAZE_SPEED);
    }

    /**
     * Spec "Stuck": less than 0.5 blocks of progress in 40 ticks while not waiting, or every heading masked
     * by blocks for {@link #BLOCKED_FALLBACK_TICKS} (lead decision 14). Once stuck, the member asks the herd
     * budget for a fallback path every tick for one window, because {@link FallbackBudget} queues only
     * members that keep asking. {@code measure} grows as the member gets on.
     *
     * @return {@code true} while the member should ask for a fallback path this tick
     */
    private boolean isStuck(HerdMoveControl control, double measure, long now) {
        boolean stuck = this.progress.update(measure, control.isWaiting(), now)
                || control.blockedTicks() >= BLOCKED_FALLBACK_TICKS;
        control.markProgress(this.progress.lastProgressTick());
        if (stuck) {
            this.askingUntil = now + ProgressMonitor.WINDOW_TICKS;
        }
        return now < this.askingUntil;
    }

    /** Steer to a fixed point the leader stood at; when stuck, a fallback path there. */
    private void walkTo(ServerLevel level, E entity, DinoGroup group, HerdMoveControl control,
                        double x, double y, double z, double speed, long now) {
        if (isStuck(control, -horizontalDistance(entity, x, z), now)) {
            if (requestFallback(level, entity, group, control, x, y, z, speed)) {
                this.askingUntil = UNSET;
            }
            return;
        }
        control.steerTo(x, y, z, speed);
    }

    /**
     * A graze spot: {@link HerdArea#SPOT_CANDIDATES} random points inside the herd area (or within
     * {@link GrazeSchedule#SHORT_MOVE_RANGE} of {@code near}) and within {@link #FALLBACK_RANGE} of the
     * member, the roomiest that is farther than the graze spacing from every reserved spot and from the
     * leader, and that the clearance box reaches in a straight line from here (so never behind a wall or
     * outside an arena). Returns the spot with its feet height.
     */
    private static @Nullable Vec3 pickSpot(ServerLevel level, Dinosaur entity, DinoGroup group, Dinosaur leader,
                                           HerdMoveControl control, double centreX, double centreZ, double radius,
                                           double spacing, @Nullable HerdArea.Point near) {
        RandomSource random = entity.getRandom();
        List<HerdArea.Point> candidates = new ArrayList<>(HerdArea.SPOT_CANDIDATES);
        for (int attempt = 0; attempt < HerdArea.SPOT_CANDIDATES; attempt++) {
            HerdArea.Point point = near == null
                    ? HerdArea.sample(centreX, centreZ, radius, random.nextDouble(), random.nextDouble())
                    : HerdArea.sampleRing(near.x(), near.z(), 0D, GrazeSchedule.SHORT_MOVE_RANGE, random.nextDouble(), random.nextDouble());
            if (HerdArea.inside(centreX, centreZ, radius, point.x(), point.z())
                    && horizontalDistance(entity, point.x(), point.z()) <= FALLBACK_RANGE) {
                candidates.add(point);
            }
        }
        List<HerdArea.Point> reserved = group.reservedSpotsExcept(entity.getUUID());
        reserved.add(new HerdArea.Point(leader.getX(), leader.getZ()));
        ClearanceBox box = control.box();
        BodyFit fit = group.fit();
        double[] reachY = new double[candidates.size()];
        int best = HerdArea.pickSpot(candidates, reserved, spacing, point -> {
            int index = candidates.indexOf(point);
            reachY[index] = fit.reachY(level, entity, box, entity.getX(), entity.getY(), entity.getZ(), point.x(), point.z());
            return !Double.isNaN(reachY[index]);
        });
        if (best < 0) {
            return null;
        }
        HerdArea.Point point = candidates.get(best);
        return new Vec3(point.x(), reachY[best], point.z());
    }

    /**
     * Within the herd's budget, hand over to vanilla pathfinding toward the fixed point (x, y, z): a trail
     * point in travel, the spot in graze; never a moving slot. While queued, keep steering there.
     *
     * @return {@code true} if the path was granted
     */
    private boolean requestFallback(ServerLevel level, E entity, DinoGroup group, HerdMoveControl control,
                                    double x, double y, double z, double speed) {
        if (!group.fallbacks().tryAcquire(entity.getUUID(), level.getGameTime())) {
            control.steerTo(x, y, z, speed);
            return false;
        }
        control.stopSteering();
        BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET,
                new WalkTarget(BlockPos.containing(x, y, z), (float) speed, FALLBACK_CLOSE_ENOUGH));
        this.progress.reset(Double.NaN, level.getGameTime());
        return true;
    }

    private static double horizontalDistance(Dinosaur entity, double x, double z) {
        double dx = x - entity.getX();
        double dz = z - entity.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
```

- [ ] **Step 2: Wire the behaviour back into the core tasks**

In `Dinosaur.java`, add the import (next to the other `social` imports):

```java
import net.dumbcode.projectnublar.entity.ai.behaviour.social.FollowFormation;
```

In `getCoreTasks()`, replace:

```java
                new ThreatDisplay<>(34)
        );
```

with:

```java
                new ThreatDisplay<>(34),
                new FollowFormation<>()
        );
```

- [ ] **Step 3: Compile and run the unit tests**

Run: `./gradlew :common:compileJava`
Expected: `BUILD SUCCESSFUL`.

Run: `sh tools/junit-standin/run.sh`
Expected: last line `141 run, 0 failed` (the runner does not load `FollowFormation`).

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Self-check against the spec**

Confirm by reading: every spot candidate passes `inside`, the fallback range, the reserved-spot spacing (strictly farther than the graze spacing), and `reachY` from the member (so spots behind walls or barriers are never picked); fallback goals are the reserved spot or the herd centre only; the predicate in `pickSpot` runs only for candidates that already pass the spacing test; a spot farther than the fallback range is released, never re-picked every tick; a member walking back inside the area goes through `isStuck` like any other walk.

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`
Expected: `FollowFormation.java` new; `Dinosaur.java` modified; nothing staged.

### Task 14: Follower behaviour, travel

**Most capable model.** Spec "Travel", "Joining" and "Stuck and re-path": in TRAVEL a follower reads its target arc and lane offset from the herd's per-tick layout, projects itself on the trail (with a hint from the previous tick, so a U-turn does not make it jump), and steers to `TravelOrder.follow` (3 blocks ahead of its projection, lane offset kept inside the side room). Speed comes from its arc gap; 0 means it waits in place (ahead of its slot, or its slot is not on the trail yet). A follower more than 12 blocks from the trail asks for a fallback path to its projection point (a fixed trail point). Stuck (less than 0.5 blocks of arc progress in 40 ticks while not waiting, or every heading masked by blocks for 10 ticks) asks for a fallback path to the trail point 8 blocks ahead of its projection, never past the leader, every tick for one window. A follower whose slot is not on the trail yet waits, and (from Task 17) first steps out of the leader's line if it stands in it. Plan review decision 9: the layout uses the open and tight spacings from the follower's personal space; sneak-using the debug stick on a dinosaur switches to the tight spacing everywhere, so the lead can compare the two in a playtest.

**Files:**
- Modify: `.../social/FollowFormation.java` (constants, a field, `onStateChange`, `travel`), `common/src/main/java/net/dumbcode/projectnublar/util/DebugFlags.java` (one flag), `common/src/main/java/net/dumbcode/projectnublar/item/DebugStick.java` (sneak-use on a dinosaur)

**Interfaces:**
- Consumes: `LeaderTrail.project`, `pointAt`, `headArc`, `size` (Task 7), `TravelOrder.openSpacing`, `tightSpacing`, `TravelOrder.follow`, `TravelOrder.CATCH_UP_SPEED`, `TravelOrder.Target` (Task 8), `DinoGroup.refreshLayout`, `targetArcOf`, `laneOffsetOf`, `trail` (Task 11), `DebugFlags.fixedTravelSpacing` (this task), Task 13's `requestFallback`, `isStuck`, `horizontalDistance`, `progress`, `askingUntil`.
- Produces: constants `JOIN_PATH_DISTANCE = 12`, `PROJECT_WINDOW = 12`, `FALLBACK_AHEAD = 8` on `FollowFormation`; private `void makeWay(E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control)` (empty here, filled in Task 17) and the field `boolean madeWay`; `public static volatile boolean fixedTravelSpacing` on `DebugFlags`.

- [ ] **Step 1: Add the constants and the projection hint**

In `FollowFormation`, add after `EDGE_MARGIN`:

```java
    /** Spec "Joining": a follower farther than this from the trail asks for a fallback path to its projection. */
    static final double JOIN_PATH_DISTANCE = 12D;
    /** The projection searches the trail this far either side of last tick's arc. */
    static final double PROJECT_WINDOW = 12D;
    /** Spec "Fallback goal": the trail point this far ahead of the follower's projection (the leader stood there). */
    static final double FALLBACK_AHEAD = 8D;
```

and add after the field `private int fallbacksForSpot;`:

```java
    /** Last tick's arc on the trail, the hint for the next projection; NaN after a state change. */
    private double lastArc = Double.NaN;
    /** Set once the member has checked whether it stands in the leader's line at a journey start (Task 17). */
    private boolean madeWay;
```

- [ ] **Step 2: Reset the hint on a state change**

Replace the method `onStateChange` with:

```java
    private void onStateChange(E entity, long now) {
        this.progress.reset(Double.NaN, now);
        this.fallbacksForSpot = 0;
        this.askingUntil = UNSET;
        this.lastArc = Double.NaN;
        this.madeWay = false;
        if (this.lastState == HerdState.GRAZE) {
            this.schedule.enter(now, entity.getRandom().nextDouble());
        }
    }
```

- [ ] **Step 3: Write `travel`**

Replace the method `travel` (and its Javadoc) with:

```java
    /**
     * Travel (spec "Following"): steer to a look-ahead point on the leader's trail, offset by the lane, at a
     * speed from the arc gap to the follower's own target arc. Wait in place while ahead of the slot or while
     * the slot is not on the trail yet (stepping out of the leader's line first: {@link #makeWay}). Far from
     * the trail: a fallback path to the projection point. Stuck: a fallback path to the trail point
     * {@link #FALLBACK_AHEAD} ahead.
     */
    private void travel(ServerLevel level, E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control, long now) {
        LeaderTrail trail = group.trail();
        if (trail.size() == 0) {
            control.stopSteering();
            control.markProgress(now);
            return;
        }
        double width = entity.getBbWidth();
        double personalSpace = HerdSpacing.personalSpace(width, width);
        double tight = TravelOrder.tightSpacing(personalSpace);
        group.refreshLayout(now, DebugFlags.fixedTravelSpacing ? tight : TravelOrder.openSpacing(personalSpace), tight);
        double targetArc = group.targetArcOf(entity.getUUID());
        if (Double.isNaN(targetArc)) {
            // Joined this tick: the leader rebuilds the order next tick.
            control.stopSteering();
            control.markProgress(now);
            return;
        }
        double arc = trail.project(entity.getX(), entity.getZ(), this.lastArc, PROJECT_WINDOW);
        this.lastArc = arc;
        TravelOrder.Target target = TravelOrder.follow(trail, arc, targetArc, group.laneOffsetOf(entity.getUUID()));
        if (target == null) {
            control.stopSteering();
            return;
        }
        if (target.speed() <= 0D) {
            makeWay(entity, group, leader, control);
            control.steerTo(target.x(), target.y(), target.z(), 0D);
            this.progress.reset(arc, now);
            control.markProgress(now);
            this.askingUntil = UNSET;
            return;
        }
        LeaderTrail.TrailPoint onTrail = trail.pointAt(arc);
        if (horizontalDistance(entity, onTrail.x(), onTrail.z()) > JOIN_PATH_DISTANCE) {
            requestFallback(level, entity, group, control, onTrail.x(), onTrail.y(), onTrail.z(), TravelOrder.CATCH_UP_SPEED);
            control.markProgress(now);
            return;
        }
        if (isStuck(control, arc, now)) {
            LeaderTrail.TrailPoint ahead = trail.pointAt(Math.min(arc + FALLBACK_AHEAD, trail.headArc()));
            if (requestFallback(level, entity, group, control, ahead.x(), ahead.y(), ahead.z(), TravelOrder.CATCH_UP_SPEED)) {
                this.askingUntil = UNSET;
            }
            return;
        }
        control.steerTo(target.x(), target.y(), target.z(), target.speed());
    }

    /**
     * Review Focus 7: a follower that stands in the leader's line at a journey start steps out of it once.
     * Empty here; Task 17 fills it (it needs {@code HerdMoveControl.requestSideStep}).
     */
    private void makeWay(E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control) {
    }
```

Add `import net.dumbcode.projectnublar.util.DebugFlags;` to `FollowFormation` if it is missing.

- [ ] **Step 4: Add the travel-spacing debug toggle**

In `common/src/main/java/net/dumbcode/projectnublar/util/DebugFlags.java`, add after the field `skipDinoColourLayers`:

```java

    /**
     * When true, herd travel rows use the tight spacing everywhere (4 for Gallimimus) instead of the open
     * spacing in open ground (5), so the lead can compare the two (plan review decision 9). Server side.
     */
    public static volatile boolean fixedTravelSpacing = false;
```

In `common/src/main/java/net/dumbcode/projectnublar/item/DebugStick.java`, replace:

```java
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if(interactionTarget instanceof Dinosaur dinosaur) {
```

with:

```java
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (interactionTarget instanceof Dinosaur dinosaur && player.isShiftKeyDown()) {
            // Sneak-use on a dinosaur: switch the herd travel spacing. Consumed on both sides, so the
            // sneak-use in the air (colour layers) does not also fire.
            if (!dinosaur.level().isClientSide()) {
                DebugFlags.fixedTravelSpacing = !DebugFlags.fixedTravelSpacing;
                player.displayClientMessage(Component.literal("Herd travel spacing: "
                        + (DebugFlags.fixedTravelSpacing ? "tight everywhere (comparing)" : "open in the open, tight when tight")), true);
            }
            return InteractionResult.sidedSuccess(dinosaur.level().isClientSide());
        }
        if(interactionTarget instanceof Dinosaur dinosaur) {
```

- [ ] **Step 5: Compile and run the unit tests**

Run: `./gradlew :common:compileJava`
Expected: `BUILD SUCCESSFUL`.

Run: `sh tools/junit-standin/run.sh`
Expected: last line `141 run, 0 failed`.

- [ ] **Step 6: Self-check against the spec**

Confirm by reading: every steering target and fallback goal in travel is a point on the leader's trail (or the lane offset beside it, clamped to the measured side room ahead); the follower never targets past the leader (`follow` clamps to `headArc`); the stuck rule ignores ticks where the follower waits on purpose or yields, and counts blocked ticks (decision 14); a stuck follower keeps asking the budget for one window; the spacing flag only changes which spacing the layout gets, and the default is the open and tight pair.

- [ ] **Step 7: Checkpoint (no commit)**

Run: `git status --short`
Expected: `FollowFormation.java`, `DebugFlags.java` and `DebugStick.java` modified; nothing staged.

### Task 15: Leader journeys, waiting and the herd area

**Most capable model.** Spec "Leader" and "Herd area", lead decisions 2 and 3. A new idle behaviour `LeaderJourney` replaces the leader's `SetRandomWalkTarget`: every 400-800 ticks a graze move of 3-8 blocks to a free, reachable spot inside the herd area at speed 0.5 (the herd state does not change); every 2400-6000 ticks a roaming journey to a vanilla `LandRandomPos` target 32-64 blocks away at speed 0.7, which sets TRAVEL from the first step. `HerdLeading` (rewritten) runs every tick for the leader: it records the trail by distance and measures the side room of each new point with the clearance box; it rebuilds the travel order when a member joins or leaves; on a roaming journey it re-sets the walk target each time the leader's walk ends while it still gets on (Review Focus 6: SmartBrainLib's `MoveToWalkTarget` runs 150-249 ticks and then erases the walk target, and a vanilla path reaches at most the follow range of 35 blocks); it detects arrival (within 3 blocks, or the walk ended twice in one place, or an urgent trip's walk ended), holds TRAVEL 100 more ticks, then sets GRAZE and recentres; on roaming journeys it applies the waiting rule (slow to 0.3, then stop at speed 0 with the walk target kept, at most 200 ticks; never on urgent trips); in GRAZE it recentres after 100 ticks outside the area and turns any walk target the herd did not plan that leaves the area (water, food, feeder) into an urgent journey. The debug stick shows the herd state from the first step.

**Files:**
- Create: `.../social/LeaderJourney.java`
- Modify: `.../social/HerdLeading.java` (whole file), `.../social/DinoGroup.java` (add `describe`), `common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java` (idle tasks, import), `common/src/main/java/net/dumbcode/projectnublar/item/DebugStick.java` (one line)

**Interfaces:**
- Consumes: `JourneyPolicy` incl. `WAIT_STOP_SPEED` (Task 6), `HerdArea.sampleRing`, `inside`, `roominess`, `grazeSpacing`, `SPOT_CANDIDATES` (Task 4), `LeaderTrail.record`, `newest`, `setRoom` (Task 7), `TravelOrder.byDistance` (Task 8), `BodyFit.boxOf`, `reachY`, `sideRoom` (Task 10), every journey, centre and order method of `DinoGroup` incl. `pathEnded` (Task 11), `GroupManager.bodiesOf`, `HerdBodies.count`, `id`, `x`, `z` (Tasks 3, 11), `HerdSpacing.personalSpace`, `HerdTimings.record`.
- Produces: `public class LeaderJourney<E extends Dinosaur> extends ExtendedBehaviour<E>` with `static final int JOURNEY_Y_RANGE = 7`, `CLOSE_ENOUGH = 2`, `COOLDOWN_TICKS = 20`, `public static boolean beginRoam(Dinosaur leader, Vec3 target)` (public: GameTests start journeys with it), `static boolean begin(Dinosaur leader, Vec3 target, JourneyPolicy.Kind kind)`; `HerdLeading.tick(Dinosaur)` (public, unchanged signature), `static List<UUID> followersByDistance(ServerLevel level, DinoGroup group, Dinosaur leader)`; `public String DinoGroup.describe(UUID member)`.

- [ ] **Step 1: Write `LeaderJourney`**

Create `.../social/LeaderJourney.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The herd leader's own moves while the herd grazes (spec "Leader", lead decision 3), in place of the old
 * random wander:
 * <ul>
 *   <li><b>Graze move</b> every 400-800 ticks: walk 3-8 blocks to a free spot inside the herd area that the
 *       clearance box reaches in a straight line, at speed 0.5. The herd state does not change, so no
 *       follower moves because of it (T3).</li>
 *   <li><b>Journey</b> every 2400-6000 ticks: a vanilla {@link LandRandomPos} target 32-64 blocks away, at
 *       speed 0.7. TRAVEL starts at once ({@link DinoGroup#startJourney}).</li>
 * </ul>
 * {@link HerdLeading} runs the journey after it starts. Needs (water, food) start urgent journeys there.
 * Leader only; it runs only while the herd grazes and the leader is idle and has no walk target.
 */
public class LeaderJourney<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));

    /** Vertical range of the journey target search. */
    static final int JOURNEY_Y_RANGE = 7;
    /** A journey's walk target counts as reached this close (Manhattan, block positions). */
    static final int CLOSE_ENOUGH = 2;
    static final int COOLDOWN_TICKS = 20;

    public LeaderJourney() {
        cooldownFor(entity -> COOLDOWN_TICKS);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        DinoGroup group = GroupManager.groupOf(entity);
        if (group == null || !group.isLeader(entity.getUUID()) || !entity.isIdle()
                || BrainUtils.hasMemory(entity, MemoryModuleTypeInit.BRAIN_OVERRIDE.get())
                || group.state() != HerdState.GRAZE || !group.hasCentre()) {
            return false;
        }
        long now = level.getGameTime();
        schedule(group, entity, now);
        return !group.grazePinned() && now >= group.nextJourneyTick() || now >= group.nextGrazeMoveTick();
    }

    @Override
    protected void start(E entity) {
        ServerLevel level = (ServerLevel) entity.level();
        DinoGroup group = GroupManager.groupOf(entity);
        if (group == null) {
            return;
        }
        long now = level.getGameTime();
        RandomSource random = entity.getRandom();
        if (!group.grazePinned() && now >= group.nextJourneyTick()) {
            group.setNextJourneyTick(JourneyPolicy.nextJourney(now, random.nextDouble()));
            Vec3 target = journeyTarget(entity);
            if (target != null && begin(entity, target, JourneyPolicy.Kind.ROAM)) {
                return;
            }
        }
        if (now >= group.nextGrazeMoveTick()) {
            group.setNextGrazeMoveTick(JourneyPolicy.nextGrazeMove(now, random.nextDouble()));
            Vec3 spot = grazeMove(level, entity, group);
            if (spot != null) {
                group.reserveSpot(entity.getUUID(), spot.x, spot.z);
                BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(spot, JourneyPolicy.GRAZE_MOVE_SPEED, 0));
            }
        }
    }

    /** Starts a roaming journey of the leader's herd toward {@code target}. Public for GameTests. */
    public static boolean beginRoam(Dinosaur leader, Vec3 target) {
        return begin(leader, target, JourneyPolicy.Kind.ROAM);
    }

    /**
     * Starts a journey: TRAVEL from this tick with the followers ordered nearest-first, and (for a roaming
     * journey) the leader's walk target. An urgent journey keeps the leader's own walk target (the need's).
     *
     * @return {@code false} if the animal does not lead a herd or the herd is pinned to grazing
     */
    static boolean begin(Dinosaur leader, Vec3 target, JourneyPolicy.Kind kind) {
        if (!(leader.level() instanceof ServerLevel level)) {
            return false;
        }
        DinoGroup group = GroupManager.groupOf(leader);
        if (group == null || !group.isLeader(leader.getUUID())
                || !group.startJourney(kind, target.x, target.y, target.z, HerdLeading.followersByDistance(level, group, leader), level.getGameTime())) {
            return false;
        }
        if (kind == JourneyPolicy.Kind.ROAM) {
            BrainUtils.setMemory(leader, MemoryModuleType.WALK_TARGET, new WalkTarget(target, JourneyPolicy.JOURNEY_SPEED, CLOSE_ENOUGH));
        }
        return true;
    }

    private static void schedule(DinoGroup group, Dinosaur leader, long now) {
        if (group.nextJourneyTick() == DinoGroup.UNSET) {
            group.setNextJourneyTick(JourneyPolicy.nextJourney(now, leader.getRandom().nextDouble()));
        }
        if (group.nextGrazeMoveTick() == DinoGroup.UNSET) {
            group.setNextGrazeMoveTick(JourneyPolicy.nextGrazeMove(now, leader.getRandom().nextDouble()));
        }
    }

    /** A vanilla random land position 32-64 blocks away, or {@code null} after {@link JourneyPolicy#JOURNEY_ATTEMPTS} tries. */
    private static @Nullable Vec3 journeyTarget(Dinosaur leader) {
        for (int attempt = 0; attempt < JourneyPolicy.JOURNEY_ATTEMPTS; attempt++) {
            Vec3 position = LandRandomPos.getPos(leader, (int) JourneyPolicy.JOURNEY_MAX, JOURNEY_Y_RANGE);
            if (position != null) {
                double dx = position.x - leader.getX();
                double dz = position.z - leader.getZ();
                if (JourneyPolicy.isJourneyDistance(Math.sqrt(dx * dx + dz * dz))) {
                    return position;
                }
            }
        }
        return null;
    }

    /** A free spot 3-8 blocks away inside the herd area, reachable in a straight line, or {@code null}. */
    private static @Nullable Vec3 grazeMove(ServerLevel level, Dinosaur leader, DinoGroup group) {
        double width = leader.getBbWidth();
        double personalSpace = HerdSpacing.personalSpace(width, width);
        double spacing = HerdArea.grazeSpacing(personalSpace);
        double radius = group.radius(personalSpace, width);
        List<HerdArea.Point> reserved = group.reservedSpotsExcept(leader.getUUID());
        ClearanceBox box = BodyFit.boxOf(leader);
        BodyFit fit = group.fit();
        RandomSource random = leader.getRandom();
        for (int attempt = 0; attempt < HerdArea.SPOT_CANDIDATES; attempt++) {
            HerdArea.Point point = HerdArea.sampleRing(leader.getX(), leader.getZ(), JourneyPolicy.GRAZE_MOVE_MIN,
                    JourneyPolicy.GRAZE_MOVE_MAX, random.nextDouble(), random.nextDouble());
            if (!HerdArea.inside(group.centreX(), group.centreZ(), radius, point.x(), point.z())
                    || HerdArea.roominess(point, reserved) <= spacing) {
                continue;
            }
            double y = fit.reachY(level, leader, box, leader.getX(), leader.getY(), leader.getZ(), point.x(), point.z());
            if (!Double.isNaN(y)) {
                return new Vec3(point.x(), y, point.z());
            }
        }
        return null;
    }
}
```

- [ ] **Step 2: Rewrite `HerdLeading`**

Replace the whole file `.../social/HerdLeading.java` with:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The leader's per-tick part of herd movement (spec "Leader", "Herd area"), called from every dinosaur's
 * server tick; it returns at once for non-leaders.
 * <ul>
 *   <li><b>TRAVEL</b>: record the trail every block walked and measure the side room of each new point with
 *       the clearance box; rebuild the travel order after a join or leave; on a roaming journey re-set the
 *       walk target each time the walk ends while the leader still gets on (a vanilla walk reaches at most
 *       the follow range and runs 150-249 ticks); arrival (within 3 blocks, or the walk ended twice in one
 *       place, or an urgent trip's walk ended) holds TRAVEL 100 more ticks, then GRAZE and recentre; roaming
 *       journeys wait for stragglers (slow, then stop at speed 0 with the walk target kept, at most 200
 *       ticks); urgent trips never wait.</li>
 *   <li><b>GRAZE</b>: recentre after 100 ticks outside the herd area; a walk target the herd did not plan
 *       (water, food, a feeder) that leaves the area starts an urgent journey.</li>
 * </ul>
 */
public final class HerdLeading {

    private HerdLeading() {
    }

    public static void tick(Dinosaur dinosaur) {
        if (!(dinosaur.level() instanceof ServerLevel level)) {
            return;
        }
        DinoGroup group = GroupManager.groupOf(dinosaur);
        if (group == null || !group.isLeader(dinosaur.getUUID())) {
            return;
        }
        long start = System.nanoTime();
        level.getProfiler().push("herdLead");
        long now = level.getGameTime();
        double width = dinosaur.getBbWidth();
        double radius = group.radius(HerdSpacing.personalSpace(width, width), width);
        if (!group.hasCentre()) {
            group.recentre(dinosaur.getX(), dinosaur.getZ());
        }
        if (group.state() == HerdState.TRAVEL) {
            travel(level, dinosaur, group, radius, now);
        } else {
            graze(level, dinosaur, group, radius, now);
        }
        level.getProfiler().pop();
        HerdTimings.record(now, System.nanoTime() - start);
    }

    /** The present followers, nearest to the leader first (spec "Travel order"). */
    static List<UUID> followersByDistance(ServerLevel level, DinoGroup group, Dinosaur leader) {
        HerdBodies bodies = GroupManager.bodiesOf(group, level);
        List<UUID> ids = new ArrayList<>(bodies.count());
        double[] xs = new double[bodies.count()];
        double[] zs = new double[bodies.count()];
        for (int i = 0; i < bodies.count(); i++) {
            if (bodies.id(i).equals(leader.getUUID())) {
                continue;
            }
            xs[ids.size()] = bodies.x(i);
            zs[ids.size()] = bodies.z(i);
            ids.add(bodies.id(i));
        }
        return TravelOrder.byDistance(leader.getX(), leader.getZ(), ids, xs, zs);
    }

    private static void travel(ServerLevel level, Dinosaur leader, DinoGroup group, double radius, long now) {
        int index = group.trail().record(leader.getX(), leader.getY(), leader.getZ());
        if (index >= 0) {
            measureRoom(level, leader, group, index);
        }
        if (group.orderDirty()) {
            group.setTravelOrder(followersByDistance(level, group, leader));
        }
        if (group.settling()) {
            if (group.settleDone(now)) {
                group.endJourney(leader.getX(), leader.getZ());
            }
            return;
        }
        double dx = group.journeyX() - leader.getX();
        double dz = group.journeyZ() - leader.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        boolean pathEnded = !group.leaderWaiting() && !BrainUtils.hasMemory(leader, MemoryModuleType.WALK_TARGET)
                && !leader.getNavigation().isInProgress();
        if (pathEnded && distance > JourneyPolicy.ARRIVE_DISTANCE && group.journeyKind() == JourneyPolicy.Kind.ROAM
                && group.pathEnded(leader.getX(), leader.getZ())) {
            // Review Focus 6: a vanilla walk reaches at most the follow range (35 blocks) and MoveToWalkTarget
            // runs 150-249 ticks, then erases the walk target. The leader got on since the last end: walk again.
            // (An urgent trip's walk target belongs to the need, so that journey ends with it.)
            setJourneyWalkTarget(leader, group);
            return;
        }
        if (JourneyPolicy.arrived(distance, pathEnded)) {
            if (group.leaderWaiting() && leader.getNavigation().isInProgress()) {
                // Arrived while standing for stragglers: let the walk finish at its own speed.
                leader.getNavigation().setSpeedModifier(JourneyPolicy.JOURNEY_SPEED);
            }
            group.arrive(now);
            return;
        }
        if (group.journeyKind() == JourneyPolicy.Kind.ROAM) {
            gather(level, leader, group, radius);
        }
    }

    /** The roaming journey's walk target, as {@link LeaderJourney#begin} sets it. */
    private static void setJourneyWalkTarget(Dinosaur leader, DinoGroup group) {
        BrainUtils.setMemory(leader, MemoryModuleType.WALK_TARGET, new WalkTarget(
                new Vec3(group.journeyX(), group.journeyY(), group.journeyZ()), JourneyPolicy.JOURNEY_SPEED, LeaderJourney.CLOSE_ENOUGH));
    }

    /**
     * Spec "Wait for stragglers" (roaming journeys only). The stop is speed 0 on the navigation; the walk
     * target and path stay, because restarting the walk behaviour would cost its cooldown of up to 39 ticks.
     */
    private static void gather(ServerLevel level, Dinosaur leader, DinoGroup group, double radius) {
        HerdBodies bodies = GroupManager.bodiesOf(group, level);
        int close = 0;
        int counted = 0;
        double closeDistance = JourneyPolicy.GATHER_RADIUS_FACTOR * radius;
        for (int i = 0; i < bodies.count(); i++) {
            if (bodies.id(i).equals(leader.getUUID())) {
                continue;
            }
            double dx = bodies.x(i) - leader.getX();
            double dz = bodies.z(i) - leader.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > JourneyPolicy.STRAGGLER_IGNORE_DISTANCE) {
                continue;
            }
            counted++;
            if (distance <= closeDistance) {
                close++;
            }
        }
        JourneyPolicy.Gathering gathering = JourneyPolicy.gathering(group.journeyKind(),
                JourneyPolicy.isGathered(close, counted), group.waitedTicks());
        switch (gathering) {
            case GO -> {
                // If the walk ended while standing, the next tick's pathEnded check walks again.
                group.setLeaderWaiting(false);
                if (leader.getNavigation().isInProgress()) {
                    leader.getNavigation().setSpeedModifier(JourneyPolicy.JOURNEY_SPEED);
                }
            }
            case SLOW -> {
                group.addWait();
                if (leader.getNavigation().isInProgress()) {
                    leader.getNavigation().setSpeedModifier(JourneyPolicy.WAIT_SLOW_SPEED);
                }
            }
            case STOP -> {
                group.addWait();
                group.setLeaderWaiting(true);
                if (leader.getNavigation().isInProgress()) {
                    leader.getNavigation().setSpeedModifier(JourneyPolicy.WAIT_STOP_SPEED);
                }
            }
        }
    }

    private static void graze(ServerLevel level, Dinosaur leader, DinoGroup group, double radius, long now) {
        boolean outside = !HerdArea.inside(group.centreX(), group.centreZ(), radius, leader.getX(), leader.getZ());
        if (JourneyPolicy.shouldRecentre(group.countOutside(outside))) {
            group.recentre(leader.getX(), leader.getZ());
        }
        WalkTarget walkTarget = BrainUtils.getMemory(leader, MemoryModuleType.WALK_TARGET);
        if (walkTarget == null || group.grazePinned()) {
            return;
        }
        Vec3 target = walkTarget.getTarget().currentPosition();
        if (!HerdArea.inside(group.centreX(), group.centreZ(), radius, target.x, target.z)) {
            // A need's walk target (water, food, a feeder) that leaves the herd area: an urgent journey.
            group.startJourney(JourneyPolicy.Kind.URGENT, target.x, target.y, target.z,
                    followersByDistance(level, group, leader), now);
        }
    }

    /** Side room of a new trail point on both sides, with the leader's clearance box (spec: "measured once"). */
    private static void measureRoom(ServerLevel level, Dinosaur leader, DinoGroup group, int index) {
        LeaderTrail.TrailPoint point = group.trail().newest();
        if (point == null || point.dirX() == 0D && point.dirZ() == 0D) {
            return;
        }
        ClearanceBox box = BodyFit.boxOf(leader);
        BodyFit fit = group.fit();
        float roomPos = fit.sideRoom(level, leader, box, point.x(), point.y(), point.z(), -point.dirZ(), point.dirX());
        float roomNeg = fit.sideRoom(level, leader, box, point.x(), point.y(), point.z(), point.dirZ(), -point.dirX());
        group.trail().setRoom(index, roomPos, roomNeg);
    }
}
```

- [ ] **Step 3: Add the debug summary to `DinoGroup`**

In `.../social/DinoGroup.java`, add before the Javadoc of `private double roomAt`:

```java
    /** One line for the debug stick: herd state from the first step of a journey, centre, spot, travel target, rank. */
    public String describe(UUID member) {
        StringBuilder text = new StringBuilder("Herd: ").append(this.state);
        if (this.journeyActive) {
            text.append(", journey ").append(this.journeyKind)
                    .append(String.format(" to %.0f %.0f", this.journeyX, this.journeyZ))
                    .append(settling() ? " (settling)" : "")
                    .append(this.leaderWaiting ? " (leader waiting)" : "");
        }
        if (this.hasCentre) {
            text.append(String.format(", centre %.1f %.1f", this.centreX, this.centreZ));
        }
        HerdArea.Point spot = this.spots.get(member);
        text.append(spot == null ? ", spot: none" : String.format(", spot %.1f %.1f", spot.x(), spot.z()));
        double arc = targetArcOf(member);
        if (!Double.isNaN(arc)) {
            text.append(String.format(", target arc %.1f lane %.1f", arc, laneOffsetOf(member)));
        }
        int rank = rank(member);
        return text.append(", rank ").append(rank == Integer.MAX_VALUE ? "-" : String.valueOf(rank)).toString();
    }
```

- [ ] **Step 4: Replace the leader's wander in `Dinosaur`**

In `Dinosaur.java`, add the import next to the other `social` imports:

```java
import net.dumbcode.projectnublar.entity.ai.behaviour.social.LeaderJourney;
```

In `getIdleTasks()`, replace:

```java
                new ChallengeGroupLeader<>(),
                // The leader picks the herd's next destination, then waits 6 to 16 seconds before the next one.
                new SetRandomWalkTarget<Dinosaur>().setRadius(24, 6).speedModifier(0.8F)
                        .walkTargetPredicate((dinosaur, pos) -> !BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.BRAIN_OVERRIDE.get()))
                        .startCondition(dinosaur -> dinosaur.isIdle() && dinosaur.isGroupLeader())
                        .cooldownFor(dinosaur -> 120 + dinosaur.getRandom().nextInt(200)),
```

with:

```java
                new ChallengeGroupLeader<>(),
                // The leader grazes in short moves and sets off on journeys every few minutes (LeaderJourney);
                // HerdLeading runs the journey and sets the herd state.
                new LeaderJourney<>(),
```

- [ ] **Step 5: Show the herd line on the debug stick**

In `common/src/main/java/net/dumbcode/projectnublar/item/DebugStick.java`, replace:

```java
                            + " at " + String.format("%.1f", dinosaur.distanceTo(leader)) + " blocks"));
                }
```

with:

```java
                            + " at " + String.format("%.1f", dinosaur.distanceTo(leader)) + " blocks"));
                    lines.add(group.describe(dinosaur.getUUID()));
                }
```

- [ ] **Step 6: Compile and run the unit tests**

Run: `./gradlew :common:compileJava`
Expected: `BUILD SUCCESSFUL`.

Run: `sh tools/junit-standin/run.sh`
Expected: last line `141 run, 0 failed`.

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Self-check against the spec**

Confirm by reading: TRAVEL is set only by `startJourney` (roaming in `LeaderJourney.begin`, urgent in `HerdLeading.graze`); graze moves never change the state; the waiting rule is skipped for urgent trips; `pathEnded` is false while the leader waits, so waiting never counts as arrival; a roaming walk that ends short of the target with progress since the last end is re-issued, one that ends twice in one place is the arrival; the stop for stragglers keeps the walk target (speed 0); the leader's graze-move destination is reserved so followers avoid it.

- [ ] **Step 8: Checkpoint (no commit)**

Run: `git status --short`
Expected: `LeaderJourney.java` new; `HerdLeading.java`, `DinoGroup.java`, `Dinosaur.java`, `DebugStick.java` modified; nothing staged.

### Task 16: Needs during journeys

Lead decision 8: during a journey a follower leaves the line to drink or eat only when the need is critical. "Critical" is below the species' rage threshold (`rage_threshold` in the config, `DinoBehaviourData.rageThreshold()`, 0.4 for Gallimimus) of the maximum, for thirst or hunger. The three core behaviours that set need walk targets get a start condition. Plan review decision 17: on a roaming journey a travelling follower starts a rest only when its stamina is critical, below half the rage threshold of its maximum (`CRITICAL_STAMINA_FACTOR` 0.5, so 0.2 for Gallimimus; a normal rest starts below the rage threshold itself); on an urgent trip it rests whenever it is tired, as in GRAZE. `Rest` gets a start condition; a rest begun before the journey ends on its own. The leader is not gated (its needs drive urgent journeys, Task 15), and in GRAZE everything works as before.

**Files:**
- Create: `.../social/HerdNeeds.java`
- Modify: `common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java` (`getCoreTasks`, `getIdleTasks`, import)
- Test: `test/.../social/HerdNeedsTest.java`

**Interfaces:**
- Consumes: `DinoGroup.state`, `DinoGroup.isLeader`, `DinoGroup.journeyKind` (Task 11), `JourneyPolicy.Kind` (Task 6), `GroupManager.groupOf`, `DinoNeedsUtils.getCurrentThirst`, `getMaxThirst`, `getCurrentHunger`, `getMaxHunger`, `getCurrentStamina`, `getMaxStamina`, `Dinosaur.getDinoBehaviour().rageThreshold()`.
- Produces: `public final class HerdNeeds` with constant `CRITICAL_STAMINA_FACTOR = 0.5`, `static boolean isCritical(float current, float max, double threshold)`, `public static boolean mayLeaveForNeeds(Dinosaur dinosaur)` and `public static boolean mayRest(Dinosaur dinosaur)`.

- [ ] **Step 1: Write the failing test**

Create `test/.../social/HerdNeedsTest.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdNeedsTest {

    @Test
    void criticalBelowTheThreshold() {
        assertTrue(HerdNeeds.isCritical(39F, 100F, 0.4D));
    }

    @Test
    void notCriticalAtOrAboveTheThreshold() {
        assertFalse(HerdNeeds.isCritical(40F, 100F, 0.4D));
        assertFalse(HerdNeeds.isCritical(70F, 100F, 0.4D));
    }

    @Test
    void staminaIsCriticalBelowHalfTheRageThreshold() {
        double threshold = 0.4D * HerdNeeds.CRITICAL_STAMINA_FACTOR;
        assertTrue(HerdNeeds.isCritical(19F, 100F, threshold));
        assertFalse(HerdNeeds.isCritical(20F, 100F, threshold));
        assertFalse(HerdNeeds.isCritical(39F, 100F, threshold), "tired but not critical: keeps walking on a roaming journey");
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors, `cannot find symbol ... HerdNeeds`.

- [ ] **Step 3: Write the class**

Create `.../social/HerdNeeds.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Lead decision 8: during a journey a follower leaves the line to drink or eat only when the need is
 * critical, that is below the species' rage threshold of its maximum (0.4 for Gallimimus). Plan review
 * decision 17: on a roaming journey it starts a rest only when its stamina is critical (below
 * {@link #CRITICAL_STAMINA_FACTOR} of the rage threshold; a normal rest starts at the rage threshold); on an
 * urgent trip it rests whenever it is tired. The leader and every grazing or groupless animal follow their
 * needs as before.
 */
public final class HerdNeeds {

    /** Critical stamina on a roaming journey: below this fraction of the rage threshold (0.2 of the maximum for Gallimimus). */
    static final double CRITICAL_STAMINA_FACTOR = 0.5D;

    private HerdNeeds() {
    }

    static boolean isCritical(float current, float max, double threshold) {
        return current < max * threshold;
    }

    /** Start condition for the need walk-target behaviours in {@code Dinosaur.getCoreTasks}. */
    public static boolean mayLeaveForNeeds(Dinosaur dinosaur) {
        if (!isTravellingFollower(dinosaur, GroupManager.groupOf(dinosaur))) {
            return true;
        }
        double threshold = dinosaur.getDinoBehaviour().rageThreshold();
        return isCritical(DinoNeedsUtils.getCurrentThirst(dinosaur), DinoNeedsUtils.getMaxThirst(dinosaur), threshold)
                || isCritical(DinoNeedsUtils.getCurrentHunger(dinosaur), DinoNeedsUtils.getMaxHunger(dinosaur), threshold);
    }

    /** Start condition for {@code Rest} in {@code Dinosaur.getIdleTasks}: a rest begun before a journey ends on its own. */
    public static boolean mayRest(Dinosaur dinosaur) {
        DinoGroup group = GroupManager.groupOf(dinosaur);
        if (!isTravellingFollower(dinosaur, group) || group.journeyKind() == JourneyPolicy.Kind.URGENT) {
            return true;
        }
        double threshold = dinosaur.getDinoBehaviour().rageThreshold() * CRITICAL_STAMINA_FACTOR;
        return isCritical(DinoNeedsUtils.getCurrentStamina(dinosaur), DinoNeedsUtils.getMaxStamina(dinosaur), threshold);
    }

    private static boolean isTravellingFollower(Dinosaur dinosaur, @Nullable DinoGroup group) {
        return group != null && group.state() == HerdState.TRAVEL && !group.isLeader(dinosaur.getUUID());
    }
}
```

- [ ] **Step 4: Gate the need behaviours**

In `Dinosaur.java`, add the import next to the other `social` imports:

```java
import net.dumbcode.projectnublar.entity.ai.behaviour.social.HerdNeeds;
```

In `getCoreTasks()`, replace:

```java
                new SetWalkTargetToWaterSource<>().closeEnoughWhen((entity, pos)-> 3),
                new WalkToNearestFeeder<>().closeEnoughWhen((entity, pos) -> 3) ,
                new SetWalkTargetToFoodItem<>().predicate(Dinosaur::canTargetFoodItem),
```

with:

```java
                // Lead decision 8: a travelling follower leaves the line only for a critical need (HerdNeeds).
                new SetWalkTargetToWaterSource<>().closeEnoughWhen((entity, pos)-> 3).startCondition(HerdNeeds::mayLeaveForNeeds),
                new WalkToNearestFeeder<>().closeEnoughWhen((entity, pos) -> 3).startCondition(HerdNeeds::mayLeaveForNeeds),
                new SetWalkTargetToFoodItem<>().predicate(Dinosaur::canTargetFoodItem).startCondition(HerdNeeds::mayLeaveForNeeds),
```

In `getIdleTasks()`, replace:

```java
                        new Rest<>(69)
                                .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"sit",true))
```

with:

```java
                        // Plan review decision 17: on a roaming journey a follower lies down only when stamina is critical (HerdNeeds).
                        new Rest<>(69).startCondition(HerdNeeds::mayRest)
                                .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"sit",true))
```

- [ ] **Step 5: Run the tests to verify they pass**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: the three `HerdNeedsTest` lines `PASS`; last line `144 run, 0 failed`.

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Checkpoint (no commit)**

Run: `git status --short`
Expected: `HerdNeeds.java` and its test new; `Dinosaur.java` modified; nothing staged.

### Task 17: Unstack and make way for the leader

**Most capable model.** Spec "Spacing and stacking" and lead decision 7. **Unstack:** a member closer than the hard minimum to a higher-ranked member for 10 ticks (`UNSTACK_AFTER_TICKS`; 20 would leave a legal pair that becomes too close, a resting body getting up, inside the minimum for longer than T4's one second) steers away (interest = away from the members inside its hard minimum, masks as usual) until clear; a resting member first gets up (its `IS_RESTING` memory is cleared, so `Rest` stops and `GettingUpFromRestBehaviour` plays); an eating member finishes eating first (`Eat` ends on its own). **Leader blocked:** if the guard stops the leader at the same standing member for 10 ticks, that member steps sideways, perpendicular to the leader's heading on its own side (never along the heading), until it is the hard minimum plus `SIDE_STEP_MARGIN` (0.5) beside the leader's line (3.5 blocks from the line for Gallimimus; the spec's 2 blocks lie inside the 3-block minimum, so the leader would stop at it again); a resting member gets up first. **Making way at a journey start** (Review Focus 7): a follower whose slot is not on the trail yet stands still, so one that stands in the leader's line toward the journey target steps out of it once, the same way, before the leader reaches it. All three run inside `HerdMoveControl` and take priority over follower steering for their duration; navigation (a path) still wins.

**Files:**
- Modify: `.../social/HerdSpacing.java` (add `sideStep`), `.../social/HerdBodies.java` (add `crowdedBy`, `inside`), `.../social/HerdMoveControl.java` (constants, fields, `tick`, new methods), `.../social/FollowFormation.java` (`makeWay`)
- Test: `test/.../social/HerdSpacingTest.java` (add 4), `test/.../social/HerdBodiesTest.java` (add 3)

**Interfaces:**
- Consumes: `HerdSpacing.away`, `HerdSpacing.Neighbour` (Task 1), `HerdSpacing.hardMinimumOf` (Task 3), `HerdBodies.minimum`, `outranks`, `walking`, `id` (Task 3), Task 12's `moveToward`, `guardBlocker` and `ARRIVED`, Task 14's `makeWay` and `madeWay`, `DinoGroup.journeyX`, `journeyZ` (Task 11), `GroupManager.resolve`, `MemoryModuleTypeInit.IS_RESTING`.
- Produces: `static Vec3 HerdSpacing.sideStep(double headingX, double headingZ, double memberDx, double memberDz)`; `int HerdBodies.crowdedBy(int self)`; `List<HerdSpacing.Neighbour> HerdBodies.inside(int self)`; on `HerdMoveControl`: constants `UNSTACK_AFTER_TICKS = 10`, `UNSTACK_REACH = 3.0`, `UNSTACK_SPEED = 0.6`, `LEADER_BLOCKED_TICKS = 10`, `SIDE_STEP_MARGIN = 0.5`, `SIDE_STEP_SPEED = 0.6`, `SIDE_STEP_MAX_TICKS = 40`; `public void requestSideStep(double x, double z)`; `public boolean isUnstacking()`; on `FollowFormation`: constants `MAKE_WAY_RANGE = 16`, `MAKE_WAY_MARGIN = 0.5` and the filled `makeWay`.

- [ ] **Step 1: Write the failing tests**

Add inside `HerdSpacingTest`:

```java
    @Test
    void memberOnTheLeftStepsFurtherLeft() {
        Vec3 side = HerdSpacing.sideStep(1D, 0D, 2D, 1D);
        assertEquals(0D, side.x, EPSILON);
        assertEquals(1D, side.z, EPSILON);
    }

    @Test
    void memberOnTheRightStepsFurtherRight() {
        Vec3 side = HerdSpacing.sideStep(1D, 0D, 2D, -1D);
        assertEquals(0D, side.x, EPSILON);
        assertEquals(-1D, side.z, EPSILON);
    }

    @Test
    void memberDeadAheadStepsToAFixedSide() {
        Vec3 side = HerdSpacing.sideStep(1D, 0D, 2D, 0D);
        assertEquals(1D, side.length(), EPSILON);
        assertEquals(0D, side.x, EPSILON);
    }

    @Test
    void sideStepIsNeverAlongTheHeading() {
        Vec3 side = HerdSpacing.sideStep(3D, 4D, 5D, 1D);
        assertEquals(0D, side.x * 3D + side.z * 4D, EPSILON);
        assertEquals(1D, side.length(), EPSILON);
    }
```

Add inside `HerdBodiesTest`:

```java
    @Test
    void crowdedByFindsTheHigherRankInside() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, 0D, 0D, WIDTH, false, false, 2);
        bodies.add(id(2), 1D, 0D, 0D, 0D, WIDTH, false, false, 1);
        assertEquals(1, bodies.crowdedBy(0));
        assertEquals(-1, bodies.crowdedBy(1), "the higher rank stays put");
    }

    @Test
    void crowdedByIgnoresMembersOutsideTheMinimum() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, 0D, 0D, WIDTH, false, false, 2);
        bodies.add(id(2), 3.5D, 0D, 0D, 0D, WIDTH, false, false, 1);
        assertEquals(-1, bodies.crowdedBy(0));
    }

    @Test
    void insideListsOnlyCloseMembers() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, 0D, 0D, WIDTH, false, false, 2);
        bodies.add(id(2), 1D, 0D, 0D, 0D, WIDTH, false, false, 1);
        bodies.add(id(3), 9D, 0D, 0D, 0D, WIDTH, false, false, 3);
        assertEquals(1, bodies.inside(0).size());
        assertEquals(1D, bodies.inside(0).get(0).dx(), EPSILON);
        assertTrue(bodies.inside(2).isEmpty());
    }
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors, `cannot find symbol ... sideStep`, `... crowdedBy`, `... inside`.

- [ ] **Step 3: Add `sideStep` and the unstack queries**

In `.../social/HerdSpacing.java`, add after `away`:

```java
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
```

In `.../social/HerdBodies.java`, add these imports:

```java
import java.util.ArrayList;
import java.util.List;
```

and add after `anyWithin`:

```java
    /** The highest-ranked member inside {@code self}'s hard minimum that outranks {@code self}, or -1 (spec "Unstack"). */
    int crowdedBy(int self) {
        int best = -1;
        for (int i = 0; i < this.count; i++) {
            if (i == self || !outranks(i, self)) {
                continue;
            }
            double dx = this.xs[i] - this.xs[self];
            double dz = this.zs[i] - this.zs[self];
            double minimum = minimum(self, i);
            if (dx * dx + dz * dz < minimum * minimum && (best < 0 || outranks(i, best))) {
                best = i;
            }
        }
        return best;
    }

    /** The members inside {@code self}'s hard minimum, for {@link HerdSpacing#away}; no allocation if there are none. */
    List<HerdSpacing.Neighbour> inside(int self) {
        List<HerdSpacing.Neighbour> found = List.of();
        for (int i = 0; i < this.count; i++) {
            if (i == self) {
                continue;
            }
            double dx = this.xs[i] - this.xs[self];
            double dz = this.zs[i] - this.zs[self];
            double minimum = minimum(self, i);
            if (dx * dx + dz * dz < minimum * minimum) {
                if (found.isEmpty()) {
                    found = new ArrayList<>(2);
                }
                found.add(new HerdSpacing.Neighbour(dx, dz, minimum));
            }
        }
        return found;
    }
```

- [ ] **Step 4: Add unstack and side step to `HerdMoveControl`**

In `.../social/HerdMoveControl.java`, add these imports (`Vec3` is already imported):

```java
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.UUID;
```

Add after the constant `PATH_PROGRESS_SPEED`:

```java
    /**
     * Spec "Unstack": closer than the hard minimum to a higher rank for this many ticks, the lower rank
     * steers away. 10, not the spec's 20: a legal pair that becomes too close (a resting body gets up) must
     * be apart again inside T4's one second.
     */
    static final int UNSTACK_AFTER_TICKS = 10;
    /** An unstacking member steers toward a point this far away from the members inside its hard minimum. */
    static final double UNSTACK_REACH = 3.0D;
    static final double UNSTACK_SPEED = 0.6D;
    /** Spec "Leader blocked": the guard stops the leader at the same standing member for this many ticks. */
    static final int LEADER_BLOCKED_TICKS = 10;
    /**
     * The blocking member steps sideways until it is the hard minimum plus this margin beside the leader's
     * line (3.5 for Gallimimus). The spec's fixed 2 blocks lie inside the 3-block minimum of lead decision 4,
     * so the leader would stop at the member again.
     */
    static final double SIDE_STEP_MARGIN = 0.5D;
    static final double SIDE_STEP_SPEED = 0.6D;
    /** A side step gives up after this many ticks. */
    static final int SIDE_STEP_MAX_TICKS = 40;
```

Add after the field `private int boxAge;`:

```java
    private int crowdedTicks;
    private boolean unstacking;
    private boolean sideStepping;
    private double sideX;
    private double sideZ;
    private int sideTicks;
    private int leaderBlockedTicks;
    private @Nullable UUID leaderBlocker;
```

Add after `wallBlockedTicks()`:

```java
    /** {@code true} while the animal steers out of a stack. */
    public boolean isUnstacking() {
        return this.unstacking;
    }

    /**
     * Spec "Leader blocked" and Review Focus 7: step to (x, z), beside the leader's line. A resting animal
     * gets up first (lead decision 7).
     */
    public void requestSideStep(double x, double z) {
        this.sideStepping = true;
        this.sideX = x;
        this.sideZ = z;
        this.sideTicks = 0;
        if (this.dinosaur.isResting()) {
            BrainUtils.clearMemory(this.dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
        }
    }
```

Replace the whole method `tick()` with:

```java
    @Override
    public void tick() {
        ServerLevel level = (ServerLevel) this.mob.level();
        long start = System.nanoTime();
        level.getProfiler().push("herdMove");
        if (this.mob.getNavigation().isInProgress()) {
            this.steering = false;
            this.waiting = false;
            this.sideStepping = false;
            this.unstacking = false;
        }
        DinoGroup group = HerdSpacing.applies(this.dinosaur) ? GroupManager.groupOf(this.dinosaur) : null;
        HerdBodies bodies = group == null ? null : GroupManager.bodiesOf(group, level);
        int self = bodies == null ? -1 : bodies.indexOf(this.dinosaur.getUUID());
        if (self >= 0) {
            updateUnstack(bodies, self);
            if (this.sideStepping) {
                continueSideStep(level, group, bodies, self);
            } else if (this.unstacking) {
                unstack(level, group, bodies, self);
            } else if (this.steering) {
                moveToward(level, group, bodies, self, this.targetX, this.targetZ, this.targetSpeed);
            }
        }
        super.tick();
        if (self >= 0) {
            guard(level, group, bodies, self);
            leaderBlocked(level, group, bodies);
        } else {
            this.wallBlockedTicks = 0;
            this.guardBlocker = -1;
        }
        if (this.mob.getNavigation().isInProgress()
                && this.mob.getDeltaMovement().horizontalDistanceSqr() > PATH_PROGRESS_SPEED * PATH_PROGRESS_SPEED) {
            markProgress(level.getGameTime());
        }
        level.getProfiler().pop();
        HerdTimings.record(level.getGameTime(), System.nanoTime() - start);
    }
```

Add these methods after `guard`:

```java
    /**
     * Spec "Unstack" with lead decision 7: after {@link #UNSTACK_AFTER_TICKS} inside a higher rank's hard
     * minimum, a resting member gets up, an eating member finishes, and an idle member steers away.
     */
    private void updateUnstack(HerdBodies bodies, int self) {
        if (bodies.crowdedBy(self) < 0) {
            this.crowdedTicks = 0;
            this.unstacking = false;
            return;
        }
        if (++this.crowdedTicks < UNSTACK_AFTER_TICKS) {
            return;
        }
        if (this.dinosaur.isResting()) {
            BrainUtils.clearMemory(this.dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
            return;
        }
        this.unstacking = this.dinosaur.isIdle() && !this.mob.getNavigation().isInProgress();
    }

    /** Steer away from the members inside the hard minimum (masks as usual: moving apart is never masked). */
    private void unstack(ServerLevel level, DinoGroup group, HerdBodies bodies, int self) {
        List<HerdSpacing.Neighbour> inside = bodies.inside(self);
        Vec3 away = HerdSpacing.away(inside);
        if (away.lengthSqr() < 1.0E-12D) {
            this.unstacking = false;
            this.operation = Operation.WAIT;
            return;
        }
        moveToward(level, group, bodies, self, this.mob.getX() + away.x * UNSTACK_REACH,
                this.mob.getZ() + away.z * UNSTACK_REACH, UNSTACK_SPEED);
    }

    private void continueSideStep(ServerLevel level, DinoGroup group, HerdBodies bodies, int self) {
        double dx = this.sideX - this.mob.getX();
        double dz = this.sideZ - this.mob.getZ();
        if (++this.sideTicks > SIDE_STEP_MAX_TICKS || dx * dx + dz * dz < ARRIVED * ARRIVED) {
            this.sideStepping = false;
            this.operation = Operation.WAIT;
            return;
        }
        if (!this.dinosaur.isIdle()) {
            this.operation = Operation.WAIT;
            return;
        }
        moveToward(level, group, bodies, self, this.sideX, this.sideZ, SIDE_STEP_SPEED);
    }

    /**
     * Spec "Leader blocked": the leader's guard stopped at the same standing member for
     * {@link #LEADER_BLOCKED_TICKS}; that member steps aside until it is the hard minimum plus
     * {@link #SIDE_STEP_MARGIN} beside the leader's line, so one step clears the way.
     */
    private void leaderBlocked(ServerLevel level, DinoGroup group, HerdBodies bodies) {
        if (!group.isLeader(this.dinosaur.getUUID()) || this.guardBlocker < 0) {
            this.leaderBlockedTicks = 0;
            this.leaderBlocker = null;
            return;
        }
        UUID blocker = bodies.id(this.guardBlocker);
        this.leaderBlockedTicks = blocker.equals(this.leaderBlocker) ? this.leaderBlockedTicks + 1 : 1;
        this.leaderBlocker = blocker;
        if (this.leaderBlockedTicks < LEADER_BLOCKED_TICKS || bodies.walking(this.guardBlocker)) {
            return;
        }
        this.leaderBlockedTicks = 0;
        Dinosaur member = GroupManager.resolve(level, blocker);
        if (member != null && member.getMoveControl() instanceof HerdMoveControl control) {
            double yaw = Math.toRadians(this.mob.getYRot());
            double headingX = -Math.sin(yaw);
            double headingZ = Math.cos(yaw);
            double dx = member.getX() - this.mob.getX();
            double dz = member.getZ() - this.mob.getZ();
            Vec3 side = HerdSpacing.sideStep(headingX, headingZ, dx, dz);
            // How far the member already stands beside the line; aim one ARRIVED beyond the step, because
            // steering stops ARRIVED short of its target.
            double lateral = Math.abs(dz * headingX - dx * headingZ);
            double reach = Math.max(0D, HerdSpacing.hardMinimumOf(this.dinosaur, member) + SIDE_STEP_MARGIN - lateral) + ARRIVED;
            control.requestSideStep(member.getX() + side.x * reach, member.getZ() + side.z * reach);
        }
    }
```

- [ ] **Step 5: Make way at a journey start (`FollowFormation`)**

In `.../social/FollowFormation.java`, add after the constant `BLOCKED_FALLBACK_TICKS`:

```java
    /** Review Focus 7: a follower this far ahead of the leader at most checks whether it stands in its line. */
    static final double MAKE_WAY_RANGE = 16D;
    /** It steps out until it is the hard minimum plus this margin beside the leader's line toward the journey target. */
    static final double MAKE_WAY_MARGIN = 0.5D;
```

Replace the method `makeWay` (and its Javadoc) with:

```java
    /**
     * Review Focus 7: followers whose slot is not on the trail yet stand still, and the leader walks a
     * vanilla path that cannot go round them. So a follower standing ahead of the leader, within
     * {@link #MAKE_WAY_RANGE}, inside the hard minimum plus {@link #MAKE_WAY_MARGIN} of the leader's line
     * toward the journey target, steps out of that line once, perpendicular to it on its own side.
     */
    private void makeWay(E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control) {
        if (this.madeWay) {
            return;
        }
        this.madeWay = true;
        double dirX = group.journeyX() - leader.getX();
        double dirZ = group.journeyZ() - leader.getZ();
        double length = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (length < 1.0E-6D) {
            return;
        }
        dirX /= length;
        dirZ /= length;
        double dx = entity.getX() - leader.getX();
        double dz = entity.getZ() - leader.getZ();
        double along = dx * dirX + dz * dirZ;
        double lateral = Math.abs(dz * dirX - dx * dirZ);
        double clear = HerdSpacing.hardMinimumOf(leader, entity) + MAKE_WAY_MARGIN;
        if (along <= 0D || along > MAKE_WAY_RANGE || lateral >= clear) {
            return;
        }
        Vec3 side = HerdSpacing.sideStep(dirX, dirZ, dx, dz);
        // Aim one ARRIVED beyond the step, because steering stops ARRIVED short of its target.
        double reach = clear - lateral + HerdMoveControl.ARRIVED;
        control.requestSideStep(entity.getX() + side.x * reach, entity.getZ() + side.z * reach);
    }
```

- [ ] **Step 6: Run the tests to verify they pass**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: the 7 new lines `PASS`; last line `151 run, 0 failed`.

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Self-check against the spec**

Confirm by reading: only the lower rank of a stacked pair moves (`crowdedBy` requires the other to outrank it); unstack, side step and making way go through `moveToward`, so blocks and other members still mask; the side step is perpendicular to the leader's heading and long enough to clear the hard minimum in one go; making way happens once per journey (`madeWay`) and only for a follower ahead of the leader in its line; a path in progress cancels all three.

- [ ] **Step 8: Checkpoint (no commit)**

Run: `git status --short`
Expected: `HerdSpacing.java`, `HerdBodies.java`, `HerdMoveControl.java`, `FollowFormation.java` and the two tests modified; nothing staged.

### Task 18: GameTest arenas: a natural forest, a corridor and low gaps

Lead feedback 1: the forest arena must look like an overworld forest, not a grid. The generator builds it from a fixed seed: oak and birch trees with trunks 4-7 high at irregular spacing, some 2 x 2 trunks, leaf canopies of different radii starting 3-5 blocks above the ground, bushes (leaf blocks on the ground), fallen logs, saplings and one-block rises. It checks that a 2 x 3 body can cross (vanilla sizes its path nodes 3 x 3 columns with 4 blocks of headroom; the 2.5 x 3.25 clearance box fits inside) and stops with an error if not. Two new arenas: `herd_corridor` (a 3-wide, 20-long passage through rock, spec `herdPassesCorridor`) and `herd_lowgap` (a wall with a 3-high leaf gap, a 2.5-high gap under top slabs and one open passage, spec `memberNeverEntersLowGap`; the leaf gap is the real T8 test, since a 3-tall hitbox fits it physically and only the 3.25-high box forbids it, while the slab gap is a vanilla-collision control); they are plain because realism would not serve those tests. Every arena keeps its barrier walls: they are ordinary solid blocks for `BodyFit` (lead feedback 2). The spawn area (x 3-19) and the goal area (x 54-62) stay flat and open in every arena, so `FLOOR_Y` spawns stand on the floor.

**Files:**
- Modify: `tools/gametest-templates/make_templates.py` (whole file)
- Create (generated): `forge/src/main/resources/data/projectnublar/structures/herd_corridor.nbt`, `.../structures/herd_lowgap.nbt`
- Modify (generated): `.../structures/herd_forest.nbt`, `.../structures/herd_track.nbt`, `.../structures/herd_shore.nbt` (track and shore are written unchanged)

**Interfaces:**
- Consumes: nothing.
- Produces: templates `herd_track` (64 x 6 x 24), `herd_forest` (64 x 11 x 24), `herd_shore` (64 x 6 x 24), `herd_corridor` (64 x 7 x 24; corridor at x 24-43, z 10-12), `herd_lowgap` (64 x 8 x 24; wall at x 30-31; leaf gap z 3-7, template y 1-3 open under leaves at y 4-5, so 3 blocks of headroom; slab gap z 10-14, y 1-2 open under top slabs at y 3, so 2.5 blocks of headroom; open passage z 17-21). Template x and z equal relative x and z in a test; template y = relative y - 1. Goal columns: forest and track (56, 8), corridor (56, 11), low gaps (56, 12), shore (56, 6). `herd_arena` is not generated (it already exists and is unchanged).

- [ ] **Step 1: Replace the generator**

Replace the whole file `tools/gametest-templates/make_templates.py` with:

```python
"""Writes the herd GameTest arenas as structure NBT files (Minecraft 1.20.1, DataVersion 3465).

Usage, from the worktree root:  python tools/gametest-templates/make_templates.py

Template y=0 is the floor layer. In a test it sits at relative y=1, so animals and goals use relative
y=2 (HerdGameTests.FLOOR_Y). Template x and z equal relative x and z.
"""
import gzip
import os
import random
import struct
from collections import deque

OUT = "forge/src/main/resources/data/projectnublar/structures"
DATA_VERSION = 3465
# Fixed seed: the forest is the same on every run. If the route check fails after an edit, change the seed.
FOREST_SEED = 20261012
# The herd GameTests' goal column and the middle of their spawn grid (HerdGameTests.GOAL, hatchTwenty).
GOAL = (56, 8)
SPAWN_CELL = (11, 12)


def _name(text):
    data = text.encode("utf-8")
    return struct.pack(">H", len(data)) + data


def _int(name, value):
    return b"\x03" + _name(name) + struct.pack(">i", value)


def _string(name, value):
    return b"\x08" + _name(name) + _name(value)


def _int_list(name, values):
    return b"\x09" + _name(name) + b"\x03" + struct.pack(">i", len(values)) + b"".join(struct.pack(">i", v) for v in values)


def _compound_list(name, items):
    kind = b"\x0a" if items else b"\x00"
    return b"\x09" + _name(name) + kind + struct.pack(">i", len(items)) + b"".join(item + b"\x00" for item in items)


def _state(block):
    """A palette entry. block is an id, optionally with properties: "minecraft:oak_leaves[persistent=true]"."""
    if "[" not in block:
        return _string("Name", block)
    name, properties = block[:-1].split("[", 1)
    pairs = [pair.split("=", 1) for pair in properties.split(",")]
    return (_string("Name", name) + b"\x0a" + _name("Properties")
            + b"".join(_string(key, value) for key, value in pairs) + b"\x00")


def write(name, size, blocks):
    """blocks: dict (x, y, z) -> block id. Positions not listed are left as they are (structure void)."""
    palette = sorted(set(blocks.values()))
    index = {block: i for i, block in enumerate(palette)}
    entries = [_int_list("pos", list(pos)) + _int("state", index[block]) for pos, block in sorted(blocks.items())]
    root = (b"\x0a" + _name("") + _int("DataVersion", DATA_VERSION) + _int_list("size", list(size))
            + _compound_list("palette", [_state(block) for block in palette])
            + _compound_list("blocks", entries) + _compound_list("entities", []) + b"\x00")
    os.makedirs(OUT, exist_ok=True)
    with open(os.path.join(OUT, name + ".nbt"), "wb") as handle:
        handle.write(gzip.compress(root))


# Persistent, so leaves never decay (the default state is persistent=false, distance=7).
OAK_LEAVES = "minecraft:oak_leaves[persistent=true]"
BIRCH_LEAVES = "minecraft:birch_leaves[persistent=true]"
OAK_LOG = "minecraft:oak_log"
BIRCH_LOG = "minecraft:birch_log"
LOG_ALONG_X = "minecraft:oak_log[axis=x]"
LOG_ALONG_Z = "minecraft:oak_log[axis=z]"
SAPLING = "minecraft:oak_sapling"
GRASS = "minecraft:grass_block"
STONE = "minecraft:stone"
SLAB = "minecraft:smooth_stone_slab[type=top]"
BARRIER = "minecraft:barrier"
# Blocks without a collision shape: they never block a body.
NO_COLLISION = {SAPLING, "minecraft:water"}

SYMBOLS = {BARRIER: "#", OAK_LOG: "L", BIRCH_LOG: "B", LOG_ALONG_X: "=", LOG_ALONG_Z: "|",
           OAK_LEAVES: "%", BIRCH_LEAVES: "%", "minecraft:water": "~", SAPLING: "s", GRASS: "^",
           STONE: "S", SLAB: "_"}


def print_map(blocks, size_x, size_z, y, title, route=()):
    """Top-down map of one layer: * the checked route, G the goal, . open, ^ a one-block rise."""
    on_route = set(route)
    print(title + ", x to the right, z down")
    for z in range(size_z):
        row = ""
        for x in range(size_x):
            block = blocks.get((x, y, z))
            if (x, z) == GOAL and block is None:
                row += "G"
            elif (x, z) in on_route and block is None:
                row += "*"
            else:
                row += SYMBOLS.get(block, "?" if block else ".")
        print(row)


def walled_floor(size_x, size_y, size_z, wall_height):
    blocks = {}
    for x in range(size_x):
        for z in range(size_z):
            blocks[(x, 0, z)] = GRASS
            if x in (0, size_x - 1) or z in (0, size_z - 1):
                for y in range(1, wall_height + 1):
                    blocks[(x, y, z)] = BARRIER
    return blocks


def solid(block):
    return block is not None and block not in NO_COLLISION


def ground_height(blocks, x, z):
    """Template y of the ground: 1 on a one-block rise (grass at y=1), else 0."""
    return 1 if blocks.get((x, 1, z)) == GRASS else 0


def body_fits(blocks, cx, cz, size_y, foot=3, head=4):
    """Feet height (template y) if a body fits on the foot x foot columns centred on (cx, cz), else None.

    Vanilla pathfinding sizes a 2 x 3 mob as 3 x 3 columns with 4 blocks of headroom (floor(width + 1),
    floor(height + 1)); the herd's clearance box (2.5 x 3.25) fits inside that. Columns may differ by one block.
    """
    half = foot // 2
    columns = [(cx + dx, cz + dz) for dx in range(-half, half + 1) for dz in range(-half, half + 1)]
    heights = [ground_height(blocks, x, z) for x, z in columns]
    if max(heights) - min(heights) > 1:
        return None
    feet = max(heights) + 1
    for x, z in columns:
        for y in range(feet, feet + head):
            if y >= size_y or solid(blocks.get((x, y, z))):
                return None
    return feet


def find_route(blocks, size_x, size_y, size_z, start, goal):
    """Breadth-first search over body centres, one block per step, rises of at most one. The route, or []."""
    def fits(cell):
        return body_fits(blocks, cell[0], cell[1], size_y)
    if fits(start) is None:
        return []
    previous = {start: None}
    queue = deque([start])
    while queue:
        cell = queue.popleft()
        if abs(cell[0] - goal[0]) <= 1 and abs(cell[1] - goal[1]) <= 1:
            route = []
            while cell is not None:
                route.append(cell)
                cell = previous[cell]
            return route[::-1]
        feet = fits(cell)
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nxt = (cell[0] + dx, cell[1] + dz)
            if nxt in previous or not (2 <= nxt[0] <= size_x - 3 and 2 <= nxt[1] <= size_z - 3):
                continue
            next_feet = fits(nxt)
            if next_feet is not None and abs(next_feet - feet) <= 1:
                previous[nxt] = cell
                queue.append(nxt)
    return []


def require_route(name, blocks, size, start=SPAWN_CELL, goal=GOAL):
    """Stops generation unless a 2 x 3 body can walk from start to goal."""
    route = find_route(blocks, size[0], size[1], size[2], start, goal)
    if not route:
        raise SystemExit(name + ": no route for a 2 x 3 body from " + str(start) + " to " + str(goal)
                         + "; change the layout or FOREST_SEED")
    return route


def canopy(blocks, rng, cx, cz, bottom, top, radius, leaves, size_x, size_z):
    """A rough leaf ball from bottom to top (template y): full radius in the middle, smaller at the ends,
    ragged at the edge. Never replaces a block already there (trunks stay)."""
    for y in range(bottom, top + 1):
        layer = radius if (bottom < y < top or top == bottom) else max(1, radius - 1)
        for x in range(cx - layer - 1, cx + layer + 2):
            for z in range(cz - layer - 1, cz + layer + 2):
                if 1 <= x <= size_x - 2 and 1 <= z <= size_z - 2 \
                        and (x - cx) ** 2 + (z - cz) ** 2 <= layer * layer + rng.uniform(-0.8, 0.8):
                    blocks.setdefault((x, y, z), leaves)


def natural_forest():
    """A forest like the overworld's: oak and birch of mixed heights, some 2 x 2 trunks, canopies of
    different sizes starting at different heights, bushes, fallen logs, saplings and one-block rises,
    at irregular spacing from a fixed seed. The spawn area (x < 23) and the goal area (x > 53) stay flat
    and open. Generation stops if a 2 x 3 body cannot cross."""
    rng = random.Random(FOREST_SEED)
    size_x, size_y, size_z = 64, 11, 24
    blocks = walled_floor(size_x, size_y, size_z, size_y - 1)
    x_min, x_max = 24, 52

    # One-block rises: a few ragged patches of an extra grass layer.
    for _ in range(6):
        cx, cz, r = rng.randint(x_min + 1, x_max - 1), rng.randint(3, 20), rng.uniform(1.5, 3.0)
        for x in range(int(cx - r) - 1, int(cx + r) + 2):
            for z in range(int(cz - r) - 1, int(cz + r) + 2):
                if x_min - 1 <= x <= x_max + 1 and 1 <= z <= 22 \
                        and (x - cx) ** 2 + (z - cz) ** 2 <= r * r + rng.uniform(-1, 1):
                    blocks[(x, 1, z)] = GRASS

    def base(x, z):
        return ground_height(blocks, x, z) + 1

    # Trees at irregular spacing: random points, each kept only if far enough from those already kept.
    trees = []
    for _ in range(600):
        if len(trees) >= 18:
            break
        x, z = rng.randint(x_min, x_max), rng.randint(2, 21)
        roll = rng.random()
        kind = "big" if roll < 0.15 else ("oak" if roll < 0.6 else "birch")
        gap = 5.0 if kind == "big" else rng.uniform(3.0, 4.5)
        if all((x - tx) ** 2 + (z - tz) ** 2 >= max(gap, tgap) ** 2 for tx, tz, _, tgap in trees):
            trees.append((x, z, kind, gap))
    for x, z, kind, _ in trees:
        if kind == "big":
            footprint = [(x, z), (x + 1, z), (x, z + 1), (x + 1, z + 1)]
            height, log, leaves, radius = rng.randint(6, 7), OAK_LOG, OAK_LEAVES, rng.randint(2, 3)
        else:
            footprint = [(x, z)]
            height, radius = rng.randint(4, 7), rng.randint(1, 3)
            log, leaves = (OAK_LOG, OAK_LEAVES) if kind == "oak" else (BIRCH_LOG, BIRCH_LEAVES)
        footprint = [(fx, fz) for fx, fz in footprint if 1 <= fz <= 22]
        floor = max(base(fx, fz) for fx, fz in footprint)
        top = min(floor + height - 1, size_y - 3)
        for fx, fz in footprint:
            for y in range(base(fx, fz), top + 1):
                blocks[(fx, y, fz)] = log
        bottom = min(floor + rng.randint(3, 5), top)
        canopy(blocks, rng, x, z, bottom, top + 1, radius, leaves, size_x, size_z)

    # Bushes: one or two leaf blocks on the ground.
    for _ in range(10):
        x, z = rng.randint(x_min, x_max), rng.randint(2, 21)
        for bx, bz in [(x, z)] + ([(x + 1, z)] if rng.random() < 0.5 else []):
            blocks.setdefault((bx, base(bx, bz), bz), OAK_LEAVES)
    # Fallen logs, three or four long, along x or z, only where nothing stands yet.
    for _ in range(3):
        x, z = rng.randint(x_min, x_max - 4), rng.randint(3, 19)
        along_x = rng.random() < 0.5
        cells = [(x + i, z) if along_x else (x, z + i) for i in range(rng.randint(3, 4))]
        if all((cx, base(cx, cz), cz) not in blocks for cx, cz in cells):
            for cx, cz in cells:
                blocks[(cx, base(cx, cz), cz)] = LOG_ALONG_X if along_x else LOG_ALONG_Z
    # Saplings: no collision, only looks.
    for _ in range(6):
        x, z = rng.randint(x_min, x_max), rng.randint(2, 21)
        blocks.setdefault((x, base(x, z), z), SAPLING)

    route = require_route("herd_forest", blocks, (size_x, size_y, size_z))
    write("herd_forest", (size_x, size_y, size_z), blocks)
    print_map(blocks, size_x, size_z, 1, "herd_forest y=1 (walking level)", route)
    print_map(blocks, size_x, size_z, 3, "herd_forest y=3 (head level of a 3-high animal)")
    print_map(blocks, size_x, size_z, 4, "herd_forest y=4 (top of the clearance box)")
    print("herd_forest:", len(trees), "trees, route of", len(route), "cells")


def corridor():
    """A 3-wide, 20-long passage through rock (x 24-43, z 10-12), 4 high, open to the sky."""
    size = (64, 7, 24)
    blocks = walled_floor(size[0], size[1], size[2], size[1] - 1)
    for x in range(24, 44):
        for z in range(1, 23):
            if not 10 <= z <= 12:
                for y in range(1, 5):
                    blocks[(x, y, z)] = STONE
    route = require_route("herd_corridor", blocks, size, goal=(56, 11))
    write("herd_corridor", size, blocks)
    print_map(blocks, size[0], size[2], 1, "herd_corridor y=1", route)


def low_gaps():
    """A stone wall across the track (x 30-31) with three 5-wide openings: under leaves, 3 high (z 3-7);
    under top slabs, 2.5 high (z 10-14: the floor top is y=1, the slab starts at y=3.5); open to the sky
    (z 17-21). Only the last fits the clearance box of a Gallimimus (2.5 x 3.25). The leaf gap is the real
    test: a 3-tall hitbox fits it physically and only the box forbids it. The slab gap is lower than the
    hitbox, so vanilla collision alone keeps a body out; it is a control."""
    size = (64, 8, 24)
    blocks = walled_floor(size[0], size[1], size[2], size[1] - 1)
    for x in (30, 31):
        for z in range(1, 23):
            for y in range(1, 6):
                if 3 <= z <= 7:
                    block = None if y <= 3 else OAK_LEAVES
                elif 10 <= z <= 14:
                    block = None if y <= 2 else (SLAB if y == 3 else STONE)
                elif 17 <= z <= 21:
                    block = None
                else:
                    block = STONE
                if block is not None:
                    blocks[(x, y, z)] = block
    route = require_route("herd_lowgap", blocks, size, goal=(56, 12))
    if any(29 <= x <= 32 and z <= 15 for x, z in route):
        raise SystemExit("herd_lowgap: the checked route goes through a low gap")
    write("herd_lowgap", size, blocks)
    print_map(blocks, size[0], size[2], 1, "herd_lowgap y=1", route)
    print_map(blocks, size[0], size[2], 3, "herd_lowgap y=3")


def main():
    # Open track: 64 x 24, for travel and spacing tests.
    write("herd_track", (64, 6, 24), walled_floor(64, 6, 24, 5))
    natural_forest()
    # Shore: the track with a 3-wide water channel at z 12-14 from x 24 to 60, level with the floor.
    shore = walled_floor(64, 6, 24, 5)
    for x in range(24, 61):
        for z in range(12, 15):
            shore[(x, 0, z)] = "minecraft:water"
    write("herd_shore", (64, 6, 24), shore)
    corridor()
    low_gaps()
    print("ok")


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Generate the arenas**

Run: `python tools/gametest-templates/make_templates.py`
Expected: three top-down maps of `herd_forest` (irregular trees `L`/`B`, leaves `%`, rises `^`, fallen logs `=`/`|`, saplings `s`, and a `*` route from the spawn area to `G`), the line `herd_forest: 18 trees, route of 62 cells`, maps of `herd_corridor` and `herd_lowgap` with their routes (the low-gap route goes through the open passage at z 17-21), and `ok`. No `SystemExit` message.

- [ ] **Step 3: Check the files**

Run: `ls -l forge/src/main/resources/data/projectnublar/structures/`
Expected: `herd_arena.nbt`, `herd_corridor.nbt`, `herd_forest.nbt`, `herd_lowgap.nbt`, `herd_shore.nbt`, `herd_track.nbt`.

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL` (resources only; no Java change).

- [ ] **Step 4: Checkpoint (no commit)**

Run: `git status --short`
Expected: the script and the structure files modified or new (the folder is untracked, so it may show as one `??` line); nothing staged.

### Task 19: GameTests for grazing, spacing and stacking

The class is rewritten: every test first asserts at tick 5 that every animal stands on the arena floor and fails at once with "spawned below the floor" if not (spec "GameTests"); the four formation tests are kept; the old travel and personal-space tests are removed (Task 20 adds the new travel tests). New tests pin T1-T5. The lead runs each test alone in a creative world started from `runClient`.

| Test | Arena | Command | Pass |
| --- | --- | --- | --- |
| `grazingHerdHoldsHardMinimum` | `herd_track` | `/test run herdgametests.grazingherdholdshardminimum` | Pinned GRAZE, 300 settle ticks, then 1200 ticks: no pair under the hard minimum for more than 20 ticks in a row (T4). |
| `grazingMembersStayPut` | `herd_track` | `/test run herdgametests.grazingmembersstayput` | Pinned GRAZE, 300 settle ticks, then 1200 ticks: in 90% of the ticks at least 70% of members move under 0.05 b/t (T1); at the end no two graze spots are closer than 5 (T2). |
| `smallLeaderMovesKeepGraze` | `herd_track` | `/test run herdgametests.smallleadermoveskeepgraze` | After 300 pinned grazing ticks, the leader walks 5 blocks toward the centre every 100 ticks, 4 times: the state stays GRAZE and no follower that holds a spot changes or loses it before it has held it 600 ticks (T3, T6). Followers without a spot are not counted: random sampling cannot always place 19 spots 5 apart in the 22-wide arena (Review Focus 1). |
| `stackedMembersSeparate` | `herd_arena` | `/test run herdgametests.stackedmembersseparate` | 3 spawned on one block: all pairs at the hard minimum (less 0.05) within 100 ticks of forming a herd (T5). |
| Formation tests (unchanged) | `herd_arena` | `/test run herdgametests.fivegallimimusformoneherdledbythestrongest`, `/test run herdgametests.twentygallimimusformoneherdoftwenty`, `/test run herdgametests.strongernewcomerleadsatonce`, `/test run herdgametests.deadmemberleavesitsherd` | As before, plus the floor check. |

**Files:**
- Modify: `forge/src/main/java/net/dumbcode/projectnublar/gametest/HerdGameTests.java` (whole file)

**Interfaces:**
- Consumes: `DinoGroup.pinGraze`, `state`, `size`, `centreX`, `centreZ`, `spotX`, `spotZ` (Task 11), `GroupManager.groupOf`, `GroupManager.leaderOf`, `HerdSpacing.hardMinimumOf` (Task 3), `HerdState`, `Dinosaur.isGroupLeader`, `GameTestHelper.relativeVec`, `onEachTick`, `startSequence().thenExecuteAfter`.
- Produces (private helpers Task 20 uses): `FLOOR_Y`, `FLOOR_CHECK_TICK`, `TRACK`, `ARENA`, `FOREST`, `SHORE` constants; `List<Dinosaur> hatchTwenty(GameTestHelper, int firstZ)`; `Dinosaur hatch(GameTestHelper, EntityType<? extends Dinosaur>, BlockPos, double aggressionGene)`; `@Nullable DinoGroup herdOf(List<Dinosaur>)`; `void assertOnFloor(GameTestHelper, List<Dinosaur>)`; `void checkSpacing(GameTestHelper, List<Dinosaur>, int[][] closeTicks)`; `double horizontalDistance(Dinosaur, Dinosaur)`.

- [ ] **Step 1: Rewrite the test class**

Replace the whole file `forge/src/main/java/net/dumbcode/projectnublar/gametest/HerdGameTests.java` with:

```java
package net.dumbcode.projectnublar.gametest;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.data.BehaviourDataReloadListener;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.DinoGroup;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.GroupManager;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.HerdSpacing;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.HerdState;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.EntityInit;
import net.dumbcode.projectnublar.init.GeneInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * GameTests for herds (plan B18 and the herd movement redesign). The lead runs them one at a time in a
 * creative world started from {@code runClient} (never on a dedicated GameTest server):
 * {@code /test run herdgametests.<method name in lower case>}.
 * <ul>
 * <li>{@code /test run herdgametests.fivegallimimusformoneherdledbythestrongest}</li>
 * <li>{@code /test run herdgametests.twentygallimimusformoneherdoftwenty}</li>
 * <li>{@code /test run herdgametests.strongernewcomerleadsatonce}</li>
 * <li>{@code /test run herdgametests.deadmemberleavesitsherd}</li>
 * <li>{@code /test run herdgametests.grazingherdholdshardminimum}</li>
 * <li>{@code /test run herdgametests.grazingmembersstayput}</li>
 * <li>{@code /test run herdgametests.smallleadermoveskeepgraze}</li>
 * <li>{@code /test run herdgametests.stackedmembersseparate}</li>
 * </ul>
 * Arenas come from {@code tools/gametest-templates/make_templates.py}. Every test first checks that its
 * animals stand on the arena floor.
 */
@GameTestHolder(Constants.MODID)
@PrefixGameTestTemplate(false)
public final class HerdGameTests {

    /**
     * First walkable layer. The template sits one block above the test origin, so its floor (template y=0)
     * is relative y=1; animals and goals at y=2 stand on it instead of falling through.
     */
    private static final int FLOOR_Y = 2;
    /** Tick at which every test checks that its animals stand on the floor. */
    private static final int FLOOR_CHECK_TICK = 5;
    /** An animal more than this below {@link #FLOOR_Y} fell through the floor. */
    private static final double FLOOR_TOLERANCE = 0.5D;
    private static final String ARENA = "herd_arena";
    private static final String TRACK = "herd_track";
    private static final String FOREST = "herd_forest";
    private static final String SHORE = "herd_shore";
    /** Pinned grazing ticks before counting: spots are picked within 60 ticks and walked to. */
    private static final int GRAZE_SETTLE_TICKS = 300;
    private static final int COUNTED_TICKS = 1200;
    /** Spec T4: bodies may not stay closer than the hard minimum for more than 1 s. */
    private static final int CLOSE_TICKS_LIMIT = 20;
    /** Spec T1: a member moving slower than this (blocks per tick) stands. */
    private static final double STILL_SPEED = 0.05D;
    private static final double STILL_FRACTION = 0.7D;
    private static final double STILL_TICK_FRACTION = 0.9D;
    /** Graze spacing of a Gallimimus: personal space 3 + 2 (spec T2). */
    private static final double GRAZE_SPACING = 5D;
    /** A follower may change its spot only after holding it this long (GrazeSchedule.STAY_MIN_TICKS). */
    private static final int SPOT_STAY_MIN_TICKS = 600;
    private static final int LEADER_MOVES = 4;
    private static final int LEADER_MOVE_INTERVAL = 100;
    private static final double LEADER_MOVE_DISTANCE = 5D;
    private static final float LEADER_MOVE_SPEED = 0.5F;
    private static final BlockPos STACK_POS = new BlockPos(12, FLOOR_Y, 12);
    /** Spec T5: stacked members separate within this many ticks. */
    private static final int SEPARATE_TICKS = 100;
    private static final double APART_TOLERANCE = 0.05D;

    private HerdGameTests() {
    }

    @GameTest(template = ARENA, timeoutTicks = 600)
    public static void fiveGallimimusFormOneHerdLedByTheStrongest(GameTestHelper helper) {
        List<Dinosaur> animals = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            animals.add(hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(4 + 4 * i, FLOOR_Y, 5), 0D));
        }
        Dinosaur strongest = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(12, FLOOR_Y, 18), 50D);
        animals.add(strongest);

        helper.startSequence()
                .thenExecuteAfter(FLOOR_CHECK_TICK, () -> assertOnFloor(helper, animals))
                .thenWaitUntil(() -> {
                    DinoGroup group = GroupManager.groupOf(strongest);
                    helper.assertTrue(group != null, "the strongest animal has no group");
                    for (Dinosaur animal : animals) {
                        helper.assertTrue(GroupManager.groupOf(animal) == group, "an animal is not in the strongest animal's herd");
                    }
                    helper.assertTrue(strongest.isGroupLeader(), "the strongest animal does not lead");
                    helper.assertTrue(animals.stream().filter(Dinosaur::isGroupLeader).count() == 1, "the herd has more than one leader");
                })
                .thenSucceed();
    }

    /**
     * A full herd: the Gallimimus config sets {@code group_size} to 20. Twenty animals end in one herd
     * whatever pairs form first, because any two partial herds fit together and merge.
     */
    @GameTest(template = ARENA, timeoutTicks = 1200)
    public static void twentyGallimimusFormOneHerdOfTwenty(GameTestHelper helper) {
        List<Dinosaur> animals = new ArrayList<>();
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 5; column++) {
                animals.add(hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(3 + 4 * column, FLOOR_Y, 4 + 5 * row), 0D));
            }
        }

        helper.startSequence()
                .thenExecuteAfter(FLOOR_CHECK_TICK, () -> assertOnFloor(helper, animals))
                .thenWaitUntil(() -> {
                    helper.assertTrue(herdOf(animals) != null, "the 20 animals are not one herd of 20");
                    helper.assertTrue(animals.stream().filter(Dinosaur::isGroupLeader).count() == 1, "the herd does not have exactly one leader");
                })
                .thenSucceed();
    }

    @GameTest(template = ARENA, timeoutTicks = 600)
    public static void strongerNewcomerLeadsAtOnce(GameTestHelper helper) {
        Dinosaur first = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(8, FLOOR_Y, 12), 0D);
        Dinosaur second = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(14, FLOOR_Y, 12), 0D);
        Dinosaur[] newcomer = new Dinosaur[1];

        helper.startSequence()
                .thenExecuteAfter(FLOOR_CHECK_TICK, () -> assertOnFloor(helper, List.of(first, second)))
                .thenWaitUntil(() -> helper.assertTrue(GroupManager.groupOf(first) != null
                        && GroupManager.groupOf(first) == GroupManager.groupOf(second), "the first two have not paired"))
                .thenExecute(() -> newcomer[0] = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(11, FLOOR_Y, 18), 50D))
                .thenWaitUntil(() -> {
                    helper.assertTrue(GroupManager.groupOf(newcomer[0]) == GroupManager.groupOf(first), "the newcomer has not joined");
                    helper.assertTrue(newcomer[0].isGroupLeader(), "the stronger newcomer does not lead");
                })
                .thenSucceed();
    }

    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void deadMemberLeavesItsHerd(GameTestHelper helper) {
        Dinosaur doomed = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(8, FLOOR_Y, 12), 0D);
        Dinosaur survivor = hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(14, FLOOR_Y, 12), 0D);

        helper.startSequence()
                .thenExecuteAfter(FLOOR_CHECK_TICK, () -> assertOnFloor(helper, List.of(doomed, survivor)))
                .thenWaitUntil(() -> helper.assertTrue(GroupManager.groupOf(doomed) != null
                        && GroupManager.groupOf(doomed) == GroupManager.groupOf(survivor), "the two have not paired"))
                .thenExecute(doomed::kill)
                .thenWaitUntil(() -> {
                    helper.assertTrue(GroupManager.groupOf(doomed) == null, "the dead animal is still in the herd");
                    DinoGroup group = GroupManager.groupOf(survivor);
                    helper.assertTrue(group != null && group.size() == 1, "the survivor's herd does not have 1 member");
                })
                .thenSucceed();
    }

    /** Spec T4: a pinned grazing herd of 20; no pair stays closer than the hard minimum for more than 20 ticks. */
    @GameTest(template = TRACK, batch = "grazing_hard_minimum", timeoutTicks = 3000)
    public static void grazingHerdHoldsHardMinimum(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        int[][] close = new int[animals.size()][animals.size()];
        long[] clock = new long[3]; // ticks, pinned ticks, counted ticks
        helper.onEachTick(() -> {
            if (++clock[0] == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            DinoGroup group = herdOf(animals);
            if (group == null) {
                return;
            }
            group.pinGraze(true);
            if (clock[1]++ < GRAZE_SETTLE_TICKS) {
                return;
            }
            checkSpacing(helper, animals, close);
            if (++clock[2] >= COUNTED_TICKS) {
                helper.succeed();
            }
        });
    }

    /**
     * Spec T1 and T2: a pinned grazing herd of 20 mostly stands: in 90% of the counted ticks at least 70% of
     * the members move under 0.05 blocks per tick. At the end no two graze spots are closer than 5.
     */
    @GameTest(template = TRACK, batch = "grazing_stay_put", timeoutTicks = 3000)
    public static void grazingMembersStayPut(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        double[][] last = new double[animals.size()][2];
        long[] clock = new long[4]; // ticks, pinned ticks, counted ticks, ticks with enough members standing
        helper.onEachTick(() -> {
            if (++clock[0] == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            DinoGroup group = herdOf(animals);
            if (group == null) {
                return;
            }
            group.pinGraze(true);
            int still = 0;
            for (int i = 0; i < animals.size(); i++) {
                Dinosaur animal = animals.get(i);
                double dx = animal.getX() - last[i][0];
                double dz = animal.getZ() - last[i][1];
                if (dx * dx + dz * dz < STILL_SPEED * STILL_SPEED) {
                    still++;
                }
                last[i][0] = animal.getX();
                last[i][1] = animal.getZ();
            }
            if (clock[1]++ < GRAZE_SETTLE_TICKS) {
                return;
            }
            clock[2]++;
            if (still >= STILL_FRACTION * animals.size()) {
                clock[3]++;
            }
            if (clock[2] < COUNTED_TICKS) {
                return;
            }
            if (clock[3] < STILL_TICK_FRACTION * clock[2]) {
                helper.fail("enough members stood in only " + clock[3] + " of " + clock[2] + " ticks");
            }
            for (int i = 0; i < animals.size(); i++) {
                for (int j = i + 1; j < animals.size(); j++) {
                    double ax = group.spotX(animals.get(i).getUUID());
                    double bx = group.spotX(animals.get(j).getUUID());
                    if (Double.isNaN(ax) || Double.isNaN(bx)) {
                        continue;
                    }
                    double dx = ax - bx;
                    double dz = group.spotZ(animals.get(i).getUUID()) - group.spotZ(animals.get(j).getUUID());
                    if (dx * dx + dz * dz < GRAZE_SPACING * GRAZE_SPACING - 1.0E-6D) {
                        helper.fail("two graze spots are closer than the graze spacing", animals.get(i));
                    }
                }
            }
            helper.succeed();
        });
    }

    /**
     * Spec T3 and T6: after {@link #GRAZE_SETTLE_TICKS} of pinned grazing, the leader walks 5 blocks toward
     * the herd centre every 100 ticks, 4 times. The state stays GRAZE, and no follower that holds a spot
     * changes or loses it before holding it {@link #SPOT_STAY_MIN_TICKS} (the schedule's own minimum stay).
     * Followers without a spot are not counted: random best-of-8 sampling cannot always place 19 spots 5
     * apart inside the 22-wide arena (Review Focus 1), and such a member stands still anyway.
     */
    @GameTest(template = TRACK, batch = "small_leader_moves", timeoutTicks = 2400)
    public static void smallLeaderMovesKeepGraze(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        double[][] spots = new double[animals.size()][];
        long[] clock = {0L, 0L, -1L, 0L}; // ticks, pinned ticks, tick the leader moves began, leader moves made
        helper.onEachTick(() -> {
            long tick = ++clock[0];
            if (tick == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            DinoGroup group = herdOf(animals);
            if (group == null) {
                return;
            }
            group.pinGraze(true);
            Dinosaur leader = GroupManager.leaderOf(group, helper.getLevel());
            if (leader == null) {
                return;
            }
            for (int i = 0; i < animals.size(); i++) {
                Dinosaur animal = animals.get(i);
                if (animal == leader) {
                    continue;
                }
                double x = group.spotX(animal.getUUID());
                double z = group.spotZ(animal.getUUID());
                boolean changed = spots[i] != null && (Double.isNaN(x) || spots[i][0] != x || spots[i][1] != z);
                if (changed && clock[2] >= 0 && tick - spots[i][2] < SPOT_STAY_MIN_TICKS) {
                    helper.fail("a follower changed its graze spot while the leader made small moves", animal);
                }
                if (Double.isNaN(x)) {
                    spots[i] = null;
                } else if (spots[i] == null || changed) {
                    spots[i] = new double[]{x, z, tick};
                }
            }
            if (clock[1]++ < GRAZE_SETTLE_TICKS) {
                return;
            }
            if (clock[2] < 0) {
                clock[2] = tick;
            }
            if (group.state() != HerdState.GRAZE) {
                helper.fail("a small leader move started a journey", leader);
            }
            long since = tick - clock[2];
            if (since % LEADER_MOVE_INTERVAL == 0 && clock[3] < LEADER_MOVES) {
                double direction = leader.getX() > group.centreX() ? -1D : 1D;
                BrainUtils.setMemory(leader, MemoryModuleType.WALK_TARGET, new WalkTarget(
                        new Vec3(leader.getX() + direction * LEADER_MOVE_DISTANCE, leader.getY(), leader.getZ()), LEADER_MOVE_SPEED, 0));
                clock[3]++;
            }
            if (clock[3] >= LEADER_MOVES && since >= (LEADER_MOVES + 1L) * LEADER_MOVE_INTERVAL) {
                helper.succeed();
            }
        });
    }

    /** Spec T5: three animals spawned on one block are all at the hard minimum within 100 ticks of forming a herd. */
    @GameTest(template = ARENA, batch = "stacked_members", timeoutTicks = 600)
    public static void stackedMembersSeparate(GameTestHelper helper) {
        List<Dinosaur> animals = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            animals.add(hatch(helper, EntityInit.GALLIMIMUS.get(), STACK_POS, 0D));
        }
        long[] clock = {0L, -1L}; // ticks, tick the herd formed
        helper.onEachTick(() -> {
            long tick = ++clock[0];
            if (tick == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            if (clock[1] < 0) {
                if (herdOf(animals) != null) {
                    clock[1] = tick;
                }
                return;
            }
            if (allApart(animals)) {
                helper.succeed();
            } else if (tick - clock[1] > SEPARATE_TICKS) {
                helper.fail("stacked members did not reach the hard minimum within " + SEPARATE_TICKS + " ticks of forming a herd");
            }
        });
    }

    /** Twenty adult Gallimimus on a 5 x 4 grid at the start of a track (x 3-19, z firstZ to firstZ + 15). */
    private static List<Dinosaur> hatchTwenty(GameTestHelper helper, int firstZ) {
        List<Dinosaur> animals = new ArrayList<>();
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 5; column++) {
                animals.add(hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(3 + 4 * column, FLOOR_Y, firstZ + 5 * row), 0D));
            }
        }
        return animals;
    }

    /** The herd of all the animals if they form exactly one herd of their number, else {@code null}. */
    private static @Nullable DinoGroup herdOf(List<Dinosaur> animals) {
        DinoGroup group = GroupManager.groupOf(animals.get(0));
        if (group == null || group.size() != animals.size()) {
            return null;
        }
        for (Dinosaur animal : animals) {
            if (GroupManager.groupOf(animal) != group) {
                return null;
            }
        }
        return group;
    }

    /** Spec: each test first checks that every animal stands on the arena floor. */
    private static void assertOnFloor(GameTestHelper helper, List<Dinosaur> animals) {
        for (Dinosaur animal : animals) {
            if (helper.relativeVec(animal.position()).y < FLOOR_Y - FLOOR_TOLERANCE) {
                helper.fail("spawned below the floor", animal);
            }
        }
    }

    /** Counts, per pair, the ticks in a row closer than the hard minimum; fails past {@link #CLOSE_TICKS_LIMIT}. */
    private static void checkSpacing(GameTestHelper helper, List<Dinosaur> animals, int[][] close) {
        for (int i = 0; i < animals.size(); i++) {
            for (int j = i + 1; j < animals.size(); j++) {
                Dinosaur a = animals.get(i);
                Dinosaur b = animals.get(j);
                close[i][j] = horizontalDistance(a, b) < HerdSpacing.hardMinimumOf(a, b) ? close[i][j] + 1 : 0;
                if (close[i][j] > CLOSE_TICKS_LIMIT) {
                    helper.fail("two herd members stayed closer than the hard minimum for over " + CLOSE_TICKS_LIMIT + " ticks", a);
                }
            }
        }
    }

    private static boolean allApart(List<Dinosaur> animals) {
        for (int i = 0; i < animals.size(); i++) {
            for (int j = i + 1; j < animals.size(); j++) {
                Dinosaur a = animals.get(i);
                Dinosaur b = animals.get(j);
                if (horizontalDistance(a, b) < HerdSpacing.hardMinimumOf(a, b) - APART_TOLERANCE) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double horizontalDistance(Dinosaur a, Dinosaur b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Spawns an adult the way {@code IncubatedEggItem} hatches one, so it has its species data. */
    private static Dinosaur hatch(GameTestHelper helper, EntityType<? extends Dinosaur> type, BlockPos pos,
                                  double aggressionGene) {
        Dinosaur dinosaur = helper.spawn(type, pos);
        DinoData data = new DinoData();
        data.setBaseDino(type);
        data.setBasePercentage(100D);
        data.setGeneValue(GeneInit.GENDER.get(), 1D);
        data.setGeneValue(GeneInit.AGGRESSION.get(), aggressionGene);
        dinosaur.setDinoData(data);
        DinoBehaviourData behaviour = BehaviourDataReloadListener.getBehaviourInfoForDino(type);
        dinosaur.setDinoBehaviour(behaviour.toNBT(behaviour));
        DinoNeedsUtils.setDinoBaseNeeds(dinosaur, behaviour);
        DinoNeedsUtils.setCurrentHunger(dinosaur, 100F);
        DinoNeedsUtils.setCurrentStamina(dinosaur, (float) behaviour.maxStamina());
        return dinosaur;
    }
}
```

The constants `FOREST` and `SHORE` are used by Task 20; until then javac may report them unused only as a warning, not an error.

- [ ] **Step 2: Compile**

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`.

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: last line `151 run, 0 failed` (GameTests are not unit tests).

- [ ] **Step 3: Hand the GameTests to the lead (do not run them)**

The lead runs these one at a time in a creative world started from `runClient` (Task 21 launches it):

```
/test run herdgametests.grazingherdholdshardminimum
/test run herdgametests.grazingmembersstayput
/test run herdgametests.smallleadermoveskeepgraze
/test run herdgametests.stackedmembersseparate
/test run herdgametests.fivegallimimusformoneherdledbythestrongest
/test run herdgametests.twentygallimimusformoneherdoftwenty
/test run herdgametests.strongernewcomerleadsatonce
/test run herdgametests.deadmemberleavesitsherd
```

Expected: each prints a green pass. A "spawned below the floor" failure is the separate floor bug, not this feature.

- [ ] **Step 4: Checkpoint (no commit)**

Run: `git status --short`
Expected: `HerdGameTests.java` modified (the folder is untracked, so it may show as one `??` line); nothing staged.

### Task 20: GameTests for journeys, trees, gaps and walls

Travel tests (T4, T6, T7, T8) and the lead's wall feedback. Each test starts a roaming journey with the public hook `LeaderJourney.beginRoam` once the 20 animals form one herd, and checks every tick that no member presses into a wall or trunk for more than 40 ticks (`HerdMoveControl.wallBlockedTicks`; spec T7 allows 2 s). The three tests that end in GRAZE also require the new herd centre within 6 blocks of the goal, so a journey that ends early (a partial path, a walk that was not re-issued) cannot pass; the goals are 45 blocks from the spawn grid, beyond the 35-block follow range, so they also exercise the re-issue of Review Focus 6. The forest test also checks progress (no idle follower without progress for 100 ticks during TRAVEL) and the fallback cap. Goals lie inside the arena on open ground (Task 18).

| Test | Arena | Command | Pass |
| --- | --- | --- | --- |
| `herdTravelsAlongTrail` | `herd_track` | `/test run herdgametests.herdtravelsalongtrail` | Journey to (56, 8): within 1200 ticks of the start the herd is back in GRAZE with the centre within 6 blocks of the goal and every member within R of it; no pair under the hard minimum for more than 20 ticks; no wall press over 40 ticks. |
| `herdCrossesForest` | `herd_forest` | `/test run herdgametests.herdcrossesforest` | Journey across the natural forest to (56, 8): back in GRAZE with the centre within 6 blocks of the goal and all within R of it within 2400 ticks; no idle follower without progress for 100 ticks while travelling; no wall press over 40 ticks; at most 2 fallbacks in one tick. |
| `herdPassesCorridor` | `herd_corridor` | `/test run herdgametests.herdpassescorridor` | Journey to (56, 11) through the 3-wide corridor: every member has been past x 44 within 1500 ticks (19 followers file through one at a time, each needing about 10 blocked ticks and one fallback path at the mouth); no two followers ever share a target point; no wall press over 40 ticks. |
| `memberNeverEntersLowGap` | `herd_lowgap` | `/test run herdgametests.memberneverenterslowgap` | Journey to (56, 12) past the wall: no member's hitbox ever overlaps the leaf gap or the slab gap; every member has been past x 33 within 2000 ticks; no wall press over 40 ticks. |
| `herdSkirtsWater` (existing, rewritten) | `herd_shore` | `/test run herdgametests.herdskirtswater` | Journey to (56, 6) along the channel: back in GRAZE with the centre within 6 blocks of the goal and all within R of it within 1200 ticks; no member ever in water; no wall press over 40 ticks. |

**Files:**
- Modify: `forge/src/main/java/net/dumbcode/projectnublar/gametest/HerdGameTests.java` (imports, Javadoc list, constants, five tests, helpers)

**Interfaces:**
- Consumes: Task 19's helpers and constants; `LeaderJourney.beginRoam(Dinosaur, Vec3)` (Task 15); `HerdMoveControl.wallBlockedTicks()`, `lastProgressTick()` (Task 12); `HerdSpacing.herdRadius(DinoGroup, Dinosaur)` (Task 11); `DinoGroup.targetArcOf`, `laneOffsetOf`, `state`, `centreX`, `centreZ`, `mostFallbacksInOneTick` (Task 11); `GameTestHelper.relativeVec`.
- Produces: the helper `centreNearGoal(GameTestHelper, DinoGroup, BlockPos)` and `GOAL_TOLERANCE`; nothing used by later tasks.

- [ ] **Step 1: Add the imports**

In `HerdGameTests.java`, add next to the other `social` imports:

```java
import net.dumbcode.projectnublar.entity.ai.behaviour.social.HerdMoveControl;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.LeaderJourney;
```

and next to the other `net.minecraft.world.phys` import:

```java
import net.minecraft.world.phys.AABB;
```

- [ ] **Step 2: Extend the command list in the class Javadoc**

Replace:

```java
 * <li>{@code /test run herdgametests.stackedmembersseparate}</li>
 * </ul>
```

with:

```java
 * <li>{@code /test run herdgametests.stackedmembersseparate}</li>
 * <li>{@code /test run herdgametests.herdtravelsalongtrail}</li>
 * <li>{@code /test run herdgametests.herdcrossesforest}</li>
 * <li>{@code /test run herdgametests.herdpassescorridor}</li>
 * <li>{@code /test run herdgametests.memberneverenterslowgap}</li>
 * <li>{@code /test run herdgametests.herdskirtswater}</li>
 * </ul>
```

- [ ] **Step 3: Add the constants**

Add after the constant `APART_TOLERANCE`:

```java
    private static final String CORRIDOR = "herd_corridor";
    private static final String LOWGAP = "herd_lowgap";
    /** Journey goals: open ground inside each arena (tools/gametest-templates/make_templates.py). */
    private static final BlockPos GOAL = new BlockPos(56, FLOOR_Y, 8);
    private static final BlockPos CORRIDOR_GOAL = new BlockPos(56, FLOOR_Y, 11);
    private static final BlockPos LOWGAP_GOAL = new BlockPos(56, FLOOR_Y, 12);
    private static final BlockPos SHORE_GOAL = new BlockPos(56, FLOOR_Y, 6);
    /** Spec herdTravelsAlongTrail: all within R of the new centre this many ticks after the start (45 blocks, two walks, waits, settle, regroup). The lead kept 1200 (2026-10-09). */
    private static final int TRAVEL_TICKS = 1200;
    private static final int FOREST_TRAVEL_TICKS = 2400;
    /** Spec herdPassesCorridor: all pass within this many ticks (19 followers file through one at a time). The lead kept 1500 (2026-10-09). */
    private static final int CORRIDOR_TICKS = 1500;
    private static final int LOWGAP_TICKS = 2000;
    /** A journey has reached its goal when the new herd centre is within this many blocks of it. */
    private static final double GOAL_TOLERANCE = 6D;
    /** Spec herdCrossesForest: no member without progress for this many ticks. */
    private static final int NO_PROGRESS_TICKS = 100;
    /** Spec T7 (2 s) and the lead's wall feedback: no member presses into a wall or trunk longer than this. */
    private static final int WALL_PRESS_TICKS = 40;
    /** Spec herdCrossesForest: at most this many fallback paths in one tick. */
    private static final int MAX_FALLBACKS_PER_TICK = 2;
    /** The corridor ends at x 43; a member beyond this has passed it. */
    private static final double CORRIDOR_END_X = 44D;
    /** The low-gap wall ends at x 31; a member beyond this has passed it. */
    private static final double WALL_END_X = 33D;
    private static final double SAME_TARGET = 1.0E-6D;
```

- [ ] **Step 4: Add the five tests**

Add after `stackedMembersSeparate`:

```java
    /** Spec T4, T6: a journey along the open track; the herd regathers within R of the new centre, which lies at the goal. */
    @GameTest(template = TRACK, batch = "travel_trail", timeoutTicks = 3000)
    public static void herdTravelsAlongTrail(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        int[][] close = new int[animals.size()][animals.size()];
        long[] clock = {0L, -1L}; // ticks, tick the journey started
        boolean[] travelled = new boolean[1];
        helper.onEachTick(() -> {
            long tick = ++clock[0];
            if (tick == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            DinoGroup group = herdOf(animals);
            if (group == null) {
                return;
            }
            if (clock[1] < 0) {
                startJourney(helper, group, GOAL);
                clock[1] = tick;
                return;
            }
            checkSpacing(helper, animals, close);
            checkWalls(helper, animals);
            travelled[0] |= group.state() == HerdState.TRAVEL;
            if (travelled[0] && group.state() == HerdState.GRAZE && centreNearGoal(helper, group, GOAL) && allWithinHerdArea(group, animals)) {
                helper.succeed();
            } else if (tick - clock[1] > TRAVEL_TICKS) {
                helper.fail("the herd was not together at the goal " + TRAVEL_TICKS + " ticks after the journey started");
            }
        });
    }

    /** Spec T7: a journey through the natural forest to the goal beyond it. */
    @GameTest(template = FOREST, batch = "travel_forest", timeoutTicks = 4000)
    public static void herdCrossesForest(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        long[] clock = {0L, -1L};
        boolean[] travelled = new boolean[1];
        helper.onEachTick(() -> {
            long tick = ++clock[0];
            if (tick == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            DinoGroup group = herdOf(animals);
            if (group == null) {
                return;
            }
            if (clock[1] < 0) {
                startJourney(helper, group, GOAL);
                clock[1] = tick;
                return;
            }
            checkWalls(helper, animals);
            travelled[0] |= group.state() == HerdState.TRAVEL;
            if (group.state() == HerdState.TRAVEL && tick - clock[1] > NO_PROGRESS_TICKS) {
                checkProgress(helper, group, animals);
            }
            if (group.mostFallbacksInOneTick() > MAX_FALLBACKS_PER_TICK) {
                helper.fail("more than " + MAX_FALLBACKS_PER_TICK + " fallback paths in one tick");
            }
            if (travelled[0] && group.state() == HerdState.GRAZE && centreNearGoal(helper, group, GOAL) && allWithinHerdArea(group, animals)) {
                helper.succeed();
            } else if (tick - clock[1] > FOREST_TRAVEL_TICKS) {
                helper.fail("the herd did not cross the forest to the goal within " + FOREST_TRAVEL_TICKS + " ticks");
            }
        });
    }

    /** Spec herdPassesCorridor (decision 1: single file where the ground is narrow). */
    @GameTest(template = CORRIDOR, batch = "travel_corridor", timeoutTicks = 3000)
    public static void herdPassesCorridor(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        long[] clock = {0L, -1L};
        boolean[] passed = new boolean[animals.size()];
        helper.onEachTick(() -> {
            long tick = ++clock[0];
            if (tick == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            DinoGroup group = herdOf(animals);
            if (group == null) {
                return;
            }
            if (clock[1] < 0) {
                startJourney(helper, group, CORRIDOR_GOAL);
                clock[1] = tick;
                return;
            }
            checkWalls(helper, animals);
            if (group.state() == HerdState.TRAVEL) {
                checkDistinctTargets(helper, group, animals);
            }
            boolean all = true;
            for (int i = 0; i < animals.size(); i++) {
                passed[i] |= helper.relativeVec(animals.get(i).position()).x > CORRIDOR_END_X;
                all &= passed[i];
            }
            if (all) {
                helper.succeed();
            } else if (tick - clock[1] > CORRIDOR_TICKS) {
                helper.fail("not every member passed the corridor within " + CORRIDOR_TICKS + " ticks");
            }
        });
    }

    /** Spec T8: no member ever enters a gap its clearance box does not fit (3 high under leaves, 2.5 under slabs). */
    @GameTest(template = LOWGAP, batch = "travel_lowgap", timeoutTicks = 3000)
    public static void memberNeverEntersLowGap(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        long[] clock = {0L, -1L};
        boolean[] passed = new boolean[animals.size()];
        AABB leafGap = new AABB(helper.absoluteVec(new Vec3(30D, FLOOR_Y, 3D)), helper.absoluteVec(new Vec3(32D, FLOOR_Y + 3D, 8D)));
        AABB slabGap = new AABB(helper.absoluteVec(new Vec3(30D, FLOOR_Y, 10D)), helper.absoluteVec(new Vec3(32D, FLOOR_Y + 2.5D, 15D)));
        helper.onEachTick(() -> {
            long tick = ++clock[0];
            if (tick == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            for (Dinosaur animal : animals) {
                if (animal.getBoundingBox().intersects(leafGap) || animal.getBoundingBox().intersects(slabGap)) {
                    helper.fail("a herd member entered a gap lower than its clearance box", animal);
                }
            }
            DinoGroup group = herdOf(animals);
            if (group == null) {
                return;
            }
            if (clock[1] < 0) {
                startJourney(helper, group, LOWGAP_GOAL);
                clock[1] = tick;
                return;
            }
            checkWalls(helper, animals);
            boolean all = true;
            for (int i = 0; i < animals.size(); i++) {
                passed[i] |= helper.relativeVec(animals.get(i).position()).x > WALL_END_X;
                all &= passed[i];
            }
            if (all) {
                helper.succeed();
            } else if (tick - clock[1] > LOWGAP_TICKS) {
                helper.fail("not every member passed the wall within " + LOWGAP_TICKS + " ticks");
            }
        });
    }

    /** Review Focus 1 of the previous plan: travelling beside water, nobody walks into it. */
    @GameTest(template = SHORE, batch = "travel_shore", timeoutTicks = 3000)
    public static void herdSkirtsWater(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        long[] clock = {0L, -1L};
        boolean[] travelled = new boolean[1];
        helper.onEachTick(() -> {
            long tick = ++clock[0];
            if (tick == FLOOR_CHECK_TICK) {
                assertOnFloor(helper, animals);
            }
            for (Dinosaur animal : animals) {
                if (animal.isInWater()) {
                    helper.fail("a herd member walked into the water", animal);
                }
            }
            DinoGroup group = herdOf(animals);
            if (group == null) {
                return;
            }
            if (clock[1] < 0) {
                startJourney(helper, group, SHORE_GOAL);
                clock[1] = tick;
                return;
            }
            checkWalls(helper, animals);
            travelled[0] |= group.state() == HerdState.TRAVEL;
            if (travelled[0] && group.state() == HerdState.GRAZE && centreNearGoal(helper, group, SHORE_GOAL) && allWithinHerdArea(group, animals)) {
                helper.succeed();
            } else if (tick - clock[1] > TRAVEL_TICKS) {
                helper.fail("the herd was not together at the goal " + TRAVEL_TICKS + " ticks after the journey started");
            }
        });
    }
```

- [ ] **Step 5: Add the helpers**

Add after `allApart`:

```java
    /** Starts a roaming journey of the herd's leader to the goal block's bottom centre. */
    private static void startJourney(GameTestHelper helper, DinoGroup group, BlockPos goal) {
        Dinosaur leader = GroupManager.leaderOf(group, helper.getLevel());
        if (leader == null || !LeaderJourney.beginRoam(leader, helper.absoluteVec(Vec3.atBottomCenterOf(goal)))) {
            helper.fail("the herd leader could not start a journey");
        }
    }

    /** Lead feedback: no member presses into a wall or trunk for more than {@link #WALL_PRESS_TICKS}. */
    private static void checkWalls(GameTestHelper helper, List<Dinosaur> animals) {
        for (Dinosaur animal : animals) {
            if (animal.getMoveControl() instanceof HerdMoveControl control && control.wallBlockedTicks() > WALL_PRESS_TICKS) {
                helper.fail("a herd member pressed into a wall or trunk for over " + WALL_PRESS_TICKS + " ticks", animal);
            }
        }
    }

    /** Spec herdCrossesForest: every idle follower made progress (or waited on purpose) within the last 100 ticks. */
    private static void checkProgress(GameTestHelper helper, DinoGroup group, List<Dinosaur> animals) {
        long now = helper.getLevel().getGameTime();
        for (Dinosaur animal : animals) {
            if (animal.isGroupLeader() || !animal.isIdle() || !(animal.getMoveControl() instanceof HerdMoveControl control)) {
                continue;
            }
            if (now - control.lastProgressTick() > NO_PROGRESS_TICKS) {
                helper.fail("a herd member made no progress for " + NO_PROGRESS_TICKS + " ticks", animal);
            }
        }
    }

    /** Spec herdPassesCorridor: no two followers share a target point (arc and lane offset). */
    private static void checkDistinctTargets(GameTestHelper helper, DinoGroup group, List<Dinosaur> animals) {
        for (int i = 0; i < animals.size(); i++) {
            double arc = group.targetArcOf(animals.get(i).getUUID());
            if (Double.isNaN(arc)) {
                continue;
            }
            double offset = group.laneOffsetOf(animals.get(i).getUUID());
            for (int j = i + 1; j < animals.size(); j++) {
                double otherArc = group.targetArcOf(animals.get(j).getUUID());
                if (!Double.isNaN(otherArc) && Math.abs(arc - otherArc) < SAME_TARGET
                        && Math.abs(offset - group.laneOffsetOf(animals.get(j).getUUID())) < SAME_TARGET) {
                    helper.fail("two followers share a target point", animals.get(i));
                }
            }
        }
    }

    /** The herd centre lies within {@link #GOAL_TOLERANCE} of the goal: the leader arrived there and the area recentred on it. */
    private static boolean centreNearGoal(GameTestHelper helper, DinoGroup group, BlockPos goal) {
        Vec3 centre = helper.relativeVec(new Vec3(group.centreX(), 0D, group.centreZ()));
        double dx = centre.x - (goal.getX() + 0.5D);
        double dz = centre.z - (goal.getZ() + 0.5D);
        return dx * dx + dz * dz <= GOAL_TOLERANCE * GOAL_TOLERANCE;
    }

    /** Every member within the herd radius of the herd's centre. */
    private static boolean allWithinHerdArea(DinoGroup group, List<Dinosaur> animals) {
        double radius = HerdSpacing.herdRadius(group, animals.get(0));
        for (Dinosaur animal : animals) {
            double dx = animal.getX() - group.centreX();
            double dz = animal.getZ() - group.centreZ();
            if (dx * dx + dz * dz > radius * radius) {
                return false;
            }
        }
        return true;
    }
```

- [ ] **Step 6: Compile**

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`.

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: last line `151 run, 0 failed`.

- [ ] **Step 7: Hand the GameTests to the lead (do not run them)**

The lead runs these one at a time in a creative world started from `runClient` (Task 21):

```
/test run herdgametests.herdtravelsalongtrail
/test run herdgametests.herdcrossesforest
/test run herdgametests.herdpassescorridor
/test run herdgametests.memberneverenterslowgap
/test run herdgametests.herdskirtswater
```

Expected: each prints a green pass.

- [ ] **Step 8: Checkpoint (no commit)**

Run: `git status --short`
Expected: `HerdGameTests.java` modified (or the untracked folder line); nothing staged.

### Task 21: Lead in-game check

Not for an implementer subagent. The controller launches the headed client and the lead tests; nobody runs `runGameTestServer`. The lead's results go into the ledger (`.superpowers/sdd/2026-10-08-herd-movement/progress.md`).

**Files:**
- None changed. Results recorded in the ledger by the controller.

**Interfaces:**
- Consumes: everything above; the debug stick line `Herd: ...` (Task 15), its timing line `Herd AI: average ... worst ...` and the travel-spacing toggle (Task 14).
- Produces: the lead's pass or fail per item below.

- [ ] **Step 1: Final build checks (controller)**

Run: `./gradlew :common:compileJava`, then `sh tools/junit-standin/run.sh`, then `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`, `151 run, 0 failed`, `BUILD SUCCESSFUL`.

- [ ] **Step 2: Launch the headed client (controller)**

Run (in the background, from the worktree root): `./gradlew :forge:runClient`
Expected: the game window opens. Tell the lead it is ready.

- [ ] **Step 3: GameTests (lead, one at a time, creative world)**

```
/test run herdgametests.fivegallimimusformoneherdledbythestrongest
/test run herdgametests.twentygallimimusformoneherdoftwenty
/test run herdgametests.strongernewcomerleadsatonce
/test run herdgametests.deadmemberleavesitsherd
/test run herdgametests.grazingherdholdshardminimum
/test run herdgametests.grazingmembersstayput
/test run herdgametests.smallleadermoveskeepgraze
/test run herdgametests.stackedmembersseparate
/test run herdgametests.herdtravelsalongtrail
/test run herdgametests.herdcrossesforest
/test run herdgametests.herdpassescorridor
/test run herdgametests.memberneverenterslowgap
/test run herdgametests.herdskirtswater
```

Expected: all pass. While `herdcrossesforest` runs, the lead also looks at the forest: irregular oak and birch, bushes, rises; the herd never lines up against the barrier wall.

- [ ] **Step 4: Observable checklist (lead, 20 Gallimimus, plains with scattered trees, 10 minutes, debug stick)**

| # | Watch for | Target |
| --- | --- | --- |
| 1 | Most animals stand, eat or rest; a given animal keeps its place for half a minute or more, then sometimes moves a few blocks. | T1, decision 6 |
| 2 | The herd covers a wide patch (about 27 blocks across for 20), not a knot round the leader. | T2 |
| 3 | The leader shuffles a few blocks every 20-40 s; nobody follows it; the debug stick on a follower shows the same spot before and after. | T3 |
| 4 | No two bodies overlap for more than a second; no shoving; animals spawned or pushed into each other come apart within 5 s; a resting animal that is stacked gets up and steps away. | T4, T5, decisions 4 and 7 |
| 5 | Every 2-5 minutes the leader sets off 32-64 blocks and gets there (a journey longer than 35 blocks pauses for up to 2 s at about 35 blocks, where the vanilla walk ends and is set again); the debug stick shows `Herd: TRAVEL` from the first step; on a roaming journey the leader slows and waits (at most 10 s) when the herd strings out; when thirsty it goes to water without waiting. At a journey start, followers standing in the leader's way step out of it and the leader is not held up for more than a few seconds. | T6, decisions 2 and 3, Review Focus 6 and 7 |
| 6 | The herd strings out behind the leader along its route, a wedge (2 then 3 abreast) in the open and single file between trees; outer-lane followers move in before a narrowing rather than hitting its edge; it goes round trees and never pushes into a trunk or wall for more than 2 s. | T7, decision 1, wall feedback |
| 7 | No animal stands under leaves or in a gap its body does not fit; no animal looks "inside" a block. | T8, decision 5 |
| 8 | Debug stick timer: `Herd AI: average` under 1 ms, `worst` under 5 ms. | Performance budget |
| 9 | During a journey, a follower that is only a little thirsty stays in line; one that is very thirsty (below 40%) leaves to drink. | Decision 8 |
| 10 | Watch a journey with the default spacing (rows 5 apart in the open, 4 in tight ground), then sneak-use the debug stick on a dinosaur (`Herd travel spacing: tight everywhere`) and watch another journey with 4 everywhere. The lead picks one, or keeps both. | Plan review decision 9 |
| 11 | Grazers walking to a spot keep a steady line; if they veer 30 degrees off it whenever another member stands within 5 blocks of their next step, lower `ContextSteering.DANGER_NEAR_MEMBER` (0.3) in a follow-up. | Tuning (spec "Danger") |

- [ ] **Step 5: Record and checkpoint (controller, no commit)**

Write the lead's result per item into the ledger. Run: `git status --short`
Expected: no change from Task 20 apart from the ledger (which lives under `.superpowers/`); nothing staged.

### Task 22 (separable, optional): Hitboxes that do not match their models

Lead decision 5, second half: "Also fix species hitboxes that do not match their models (separate task)." This changes collision and vanilla pathing for whole species, so it is gated on the lead's approval of each value and can be done or skipped on its own; Tasks 1-21 do not depend on it (the clearance box already uses `max(hitbox, model)`). Gallimimus is not changed (its model, 0.69 wide and 2.53 high, fits inside its 2 x 3 hitbox), so no GameTest changes.

**Files:**
- Modify: `common/src/main/java/net/dumbcode/projectnublar/init/EntityInit.java:30-39` (the `sized(...)` arguments of the approved species only)

**Interfaces:**
- Consumes: `tools/clearance/measure_models.py` (Task 9).
- Produces: nothing other tasks use.

- [ ] **Step 1: Measure and propose (controller)**

Run: `python tools/clearance/measure_models.py`
Propose to the lead, for each species whose model is more than 0.5 block wider or higher than its hitbox, width = the model width and height = the model top, both rounded up to 0.1 (an axis-aligned hitbox cannot cover the length):

| Species | Hitbox now | Model (width, top, length) | Proposed `sized(...)` |
| --- | --- | --- | --- |
| triceratops | 2 x 3 | 3.40, 5.80, 13.11 | `sized(3.4F, 5.8F)` |
| brachiosaurus | 2 x 3 | 3.38, 19.10, 17.41 | `sized(3.4F, 19.1F)` |
| velociraptor | 0.6 x 0.6 | 0.53, 1.48, 5.18 | `sized(0.6F, 1.5F)` |
| dilophosaurus | 1 x 1 | 0.49, 1.73, 4.74 | `sized(1F, 1.8F)` |
| tyrannosaurus_rex | 1 x 3 | 1.38, 3.42, 10.87 | no change (within 0.5) |
| gallimimus | 2 x 3 | 0.69, 2.53, 6.12 | no change |

- [ ] **Step 2: Wait for the lead's approval**

Do not edit until the lead approves or changes each value. Species the lead does not approve stay as they are.

- [ ] **Step 3: Apply the approved values**

For each approved species, in `EntityInit.java`, change only the `sized(...)` call on its line. For example, for triceratops replace:

```java
    public static final DeferredSupplier<EntityType<TriceratopsEntity>> TRICERATOPS = registerEntity("triceratops", ()-> EntityType.Builder.of(TriceratopsEntity::new, MobCategory.MONSTER).sized(2,3).clientTrackingRange(10),Dinosaur::createAttributes);
```

with:

```java
    public static final DeferredSupplier<EntityType<TriceratopsEntity>> TRICERATOPS = registerEntity("triceratops", ()-> EntityType.Builder.of(TriceratopsEntity::new, MobCategory.MONSTER).sized(3.4F, 5.8F).clientTrackingRange(10),Dinosaur::createAttributes);
```

The other species follow the same pattern with the values from Step 1's table as approved.

- [ ] **Step 4: Compile**

Run: `./gradlew :common:compileJava`, then `sh tools/junit-standin/run.sh`, then `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`, `151 run, 0 failed`, `BUILD SUCCESSFUL`.

- [ ] **Step 5: Lead check in game**

The controller launches `./gradlew :forge:runClient`; the lead spawns each changed species, presses F3+B, and confirms the box matches the model and the animal still walks through 1-block steps and its usual gaps.

- [ ] **Step 6: Checkpoint (no commit)**

Run: `git status --short`
Expected: `EntityInit.java` modified; nothing staged.

---

## Self-review (done while writing; kept for the executor)

- **Spec coverage.** Leader table (graze move, journey, arrive and settle, wait, re-issued walks): Tasks 6, 11 and 15. Herd states and explicit state: Task 11. Graze (area, recentre, spot choice, stay, invalid spot): Tasks 4, 5, 13, 15. Travel (trail by distance, travel order, lanes, following, joining): Tasks 7, 8, 14, 15. Context steering (candidates, interest, masks, danger, pick, speed) and the guard: Tasks 2, 3, 12. Spacing and stacking (hard minimum, graze spacing, who yields, leader blocked, unstack, vanilla pushing kept): Tasks 3, 12, 17. Fitting (clearance box, fit test, ground, cache, vanilla paths kept): Tasks 9, 10, 12. Stuck and re-path (progress, stuck, fixed fallback goals, budget, two failed fallbacks): Tasks 5, 13, 14. Components table: every row in the File map. Performance budget: evaluation cap (Task 11), fallback cap (existing, checked in Task 20), timings (Tasks 12, 13, 15), lead item 8. Testing: every unit test the spec lists is in Tasks 2-9, 11; every GameTest the spec lists is in Tasks 19-20 with its command; the lead checklist is Task 21. Lead decisions 1-8: see the mapping table near the top. Lead feedback (natural forest, walls): Tasks 2, 10, 13, 18, 20.
- **Placeholder scan.** No TBD or "similar to"; every code step has complete code. The `travel` method of Task 13 is an explicit interim (stand still) replaced in full in Task 14.
- **Type consistency.** Names and signatures were checked across tasks: `HerdMoveControl.steerTo(x, y, z, speed)` (4 arguments, Tasks 12-14), `HerdMoveControl.blockedTicks()` and `requestSideStep(x, z)` (Tasks 12, 13, 17), `BodyFit.reachY(level, walker, box, fromX, fromY, fromZ, toX, toZ)` (Tasks 10, 13, 15), `DinoGroup.startJourney(kind, x, y, z, order, now)` and `pathEnded(x, z)` (Tasks 11, 15), `LeaderTrail.minRoom(from, to, positive)` (Tasks 7, 8, 11), `TravelOrder.follow(trail, projectedArc, targetArc, offset)` (Tasks 8, 14), `FollowFormation.travel(level, entity, group, leader, control, now)` and `makeWay(entity, group, leader, control)` (Tasks 13, 14, 17), `HerdSpacing.hardMinimumOf` and `herdRadius` (Tasks 3, 11, 17, 19, 20), `LeaderJourney.beginRoam` (Tasks 15, 20), `HerdNeeds.mayLeaveForNeeds` and `mayRest` (Task 16).
- **Unit test counts.** 76 before; after each task: 1: 50, 2: 61, 3: 78, 4: 83, 5: 95, 6: 105, 7: 109, 8: 124, 9: 132, 10: 132, 11: 141, 12-15: 141, 16: 144, 17: 151, 18-22: 151.
- **Review fixes (2026-10-09).** The plan review found 19 points; all are folded in: journeys re-issue the walk target (Tasks 6, 11, 15, 20), `assertNotEquals` in the stand-in (Task 9), a side step that clears the hard minimum and making way at a journey start (Tasks 14, 17), far spots dropped instead of re-picked every tick (Task 13), stuck handling for the walk back inside the area (Task 13), `smallLeaderMovesKeepGraze` without the all-spots precondition (Task 19), a 20-tick stand cache and staged probes (Tasks 10, 12), fair evaluation turns (Task 12), lane offsets clamped by the room ahead and lanes smoothed per row (Tasks 7, 8, 11), blocked ticks count as stuck and blocked members ask for a path sooner (Tasks 12, 13, 14; the lead kept the GameTest limits at 1200 travel and 1500 corridor), goal checks in the travel GameTests (Task 20), unstack after 10 ticks (Task 17), a 2.5-high slab gap (Task 18), counts and names here, the guard cuts velocity (Task 12), the stragglers stop keeps the walk target (Task 15), rest on a roaming journey only at critical stamina, any time on an urgent trip (Task 16, plan review decision 17), stuck members keep asking the budget (Tasks 13, 14), and checklist item 11 for the graze danger weight (Task 21).
