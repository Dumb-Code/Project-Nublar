# Herd movement: layered design

Date: 2026-10-08. Status: draft for review. Scope: worktree `herd-leader-sample`, plan module B18 (with fixes that belong to B16 and B17 marked).

## Problem

In-game tests with a herd of 20 Gallimimus showed:

1. Followers crowd the leader. Every follower picks spots within a fixed distance of the leader's position, and that area does not grow with the herd.
2. Standing members never give way, so walkers push through them. Both animals get shoved.
3. A shove counts as movement, so it drains stamina (a bug in the earlier stamina fix).
4. Every follower plans its own path every 3 to 8 seconds. For 20 animals that is 3 to 6 wide-body paths per second.

## Decisions (from the lead, 2026-10-08)

| Topic | Decision |
| --- | --- |
| Approach | Layered: leader route, soft formation, flocking steering |
| Travel formation | Loose wedge |
| Grazing centre | Weighted anchor; the leader weighs as much as a third of the other members combined |
| Contact | Standing members step aside; vanilla pushing stays on as a backstop (Same-herd pushing was switched off on 2026-10-08 and reverted the same day: without it, members stacked into one spot.) |
| Side work | Client tracking range, frame-rate measurement, stamina push fix, profiling budget |

## Architecture

Three layers. Each does one job.

| Layer | Who | Job | Rate |
| --- | --- | --- | --- |
| Route | The leader only, with vanilla pathfinding | Find a way around terrain | Once per destination |
| Formation | Each follower holds a slot | Order and cohesion | On a state or membership change |
| Steering | Each follower, through `HerdMoveControl` | Smooth motion and spacing | Every tick |

All follower movement uses one mechanism: steer toward a target point. The target is a formation slot (travel), a grazing spot (graze) or the herd area (regroup). A follower uses vanilla pathfinding only as a fallback, when steering makes no progress.

### Herd states

The group holds one state. The leader's tick updates it.

| State | Enter when | Followers do |
| --- | --- | --- |
| Travel | The leader has an active path that ends more than 8 blocks away | Steer to their wedge slot along the leader's trail |
| Graze | The leader has had no active path for 40 ticks | Steer to free spots in the herd area around the anchor |

Regroup is per follower, not a herd state. A follower regroups when it is farther than 1.5 x the herd radius `R` (defined under Graze) from its target: the anchor in graze, its slot's target point in travel. A regrouping follower uses a fallback path to its target.

### Travel: loose wedge along the leader's trail

- **Trail.** The leader records its position every 5 ticks in a ring buffer (64 entries, about 16 seconds of walking). These are points the leader actually stood on, so they are walkable for a body of its size.
- **Slots.** Row `r` (1, 2, 3, ...) has `r + 1` slots, behind the leader. Rows are `personal space` apart along the trail. Slots in a row are `1.3 x personal space` apart, centred on the trail. Each follower has a fixed random jitter of up to 0.5 blocks on its slot, so the wedge does not look drilled.
- **Assignment.** On entering travel, or when a member joins or leaves, followers take slots nearest-first (greedy). Assignments stay stable otherwise.
- **Target point.** For a follower in row `r`, walk back along the trail from the leader by `r x row spacing`. Offset sideways from the trail direction at that point by the slot's lateral offset.
- **Squeeze.** At the target point, sample up to 6 blocks to each side of the trail for standable ground. The lateral offset is scaled down to fit the open width. In a narrow gap the wedge collapses to a column and opens again after. Width samples are cached per trail entry.

### Graze: anchor and herd area

- **Anchor** = `(w x leader + sum of followers) / (w + n)`, where `n` is the number of present followers and `w = n / 3` (at least 1).
- **Herd radius** `R = s x sqrt(N / 2) + w / 2`, where `s` is the personal space, `N` the herd size and `w` the body width (the allowance lets a body's edge reach `R`). For Gallimimus (`s` = 3, `w` = 2): about 5.7 blocks for 5 animals, 10.5 for 20.
- **Far from the herd** (regroup check while grazing) is measured from the leader, not the anchor, so the check costs nothing per tick. The anchor is computed only when a follower picks a new spot.
- **Grazing moves.** Every 3 to 8 seconds, a follower samples 6 candidate spots inside `R` and takes the one farthest from its nearest member. Spots inside another member's personal space (current position or target) are skipped, as now.
- **Step aside.** A standing member with another member inside its personal space for more than 5 ticks takes one short step, 1 to 2 blocks, directly away, by steering only.

### Steering (per tick)

`HerdMoveControl` computes the step direction as the sum of:

- **seek**: toward the target point (weight 1),
- **separation**: the current `HerdSpacing.steer` push (weight 1.5),
- **alignment**: in travel only, toward the leader's heading (weight 0.3).

Then:

- **Turn limit**: at most 10 degrees per tick, the body's own clamp, so rotation does not jitter on clients.
- **Arrival**: the follower slows down within 3 blocks of its target, to at least 0.3 of its speed. At full speed it circles a near target instead of reaching it.
- **Step check**: when the body is about to enter a new block column, check that the blocks it would occupy are passable and that there is standable ground (no fluid, no fire or cactus, no drop of more than 3 blocks, the vanilla safe fall for mobs). This is a simple check, not the full path-node evaluation. Results are cached per herd per tick, because followers cross the same ground. If the check fails, the follower drops separation and alignment for that tick and heads for the nearest trail point (travel) or stays put (graze), and the failure counts toward the progress monitor.

### Fallback paths

- **Progress monitor**: if the follower has not moved for 40 ticks, or the step check fails 3 times in a row, the follower asks for a fallback path. "Not moved" means less than 0.05 blocks in a tick. Within 4 blocks of its target, only the step-check rule applies.
- **Budget**: at most 2 fallback path requests per herd per tick. Extra requests wait in a queue. Each follower has a 60-tick cooldown between requests. Fallback range is 16 blocks.
- While a fallback path runs, steering adds separation only; seek comes from the path.

### Exceptions

`HerdSpacing.applies` stays the single switch. It gates separation, step aside and formation following. Mating, playing and other exceptions are added there later. Following and grazing also wait while the animal is not idle (as now).

## Components

| Class (package `entity.ai.behaviour.social`) | Purpose | Pure, unit-testable |
| --- | --- | --- |
| `HerdState` | Enum: travel, graze | n/a |
| `LeaderTrail` | Ring buffer of leader positions; point at distance `d` behind the leader | Yes |
| `WedgeLayout` | Slot offsets for row and index; greedy assignment | Yes |
| `HerdArea` | Anchor and radius; best-of-k spot scoring | Yes |
| `HerdSteering` | Seek, separation, alignment, turn limit | Yes |
| `StepCheck` | Simple passability check with a per-tick cache | No (reads blocks) |
| `FallbackBudget` | Per-herd queue and per-follower cooldown | Yes |
| `FollowFormation` | Behaviour that replaces `FollowGroupLeader`: picks the target point per state and hands it to `HerdMoveControl` | No |
| `DinoGroup` | Gains state, trail, slot assignments | Partly |
| `HerdMoveControl` | Applies steering, step check, step aside | No |

| `HerdLeading` | Leader's tick: records the trail every 5 ticks and updates the herd state | No |
| `TickTimer`, `HerdTimings` | Per-tick timing of herd code: average and worst tick over the last 600 ticks, shown on the debug stick | `TickTimer` yes |

`HerdSpacing` stays and keeps personal space and separation; it gains `away` for stepping aside.

## Side work

| Item | Change | Module |
| --- | --- | --- |
| Tracking range | `clientTrackingRange(10)` for every dinosaur type in `EntityInit` (vanilla animals use 10). Large species may need more after B16 | B16 |
| Frame-rate measurement | A client-side debug toggle (sneak and use the debug stick in the air) that skips the colour-layer passes in `DinosaurRenderer`. Compare F3 frame rate with the herd in view, toggle on and off. Measurement only; any rendering fix is a separate task | B09 |
| Stamina push fix | "Moving" means self-propelled: an active navigation path, a wanted position in the move control, or forward input. Not velocity | B17 |
| Profiling budget | Measure with `/debug start` and `/debug stop` while a herd of 20 crosses open ground and forest, plus the herd timer on the debug stick (average and worst tick). Measure the current version first as the baseline | n/a |

## Performance budget

| Measure | Budget |
| --- | --- |
| Herd AI for 20 animals, average | under 1 ms per tick |
| Any single tick, herd code | under 5 ms |
| Fallback paths per herd per tick | at most 2 |

Known risks and their containment: fallback storms (budget and queue), step-check cost (simple check, new-column only, shared cache), rotation jitter (turn limit).

## Testing

- **Unit tests** (pure classes): trail lookup at distance, wedge offsets and assignment, anchor and radius, best-of-k scoring, steering sums and turn limit, fallback budget and cooldown.
- **GameTests**:
  - A herd of 20 travels 40 blocks across flat ground. Every member ends within the herd radius of the anchor. The smallest centre-to-centre distance between members stays above 2 blocks in at least 95% of sampled ticks.
  - The same herd crosses a forest course. Fallback requests never exceed 2 per tick.
  - A herd grazes for 60 seconds. No member stays inside another's personal space for more than 40 ticks.
- **In game, by the lead**: travel looks like a loose wedge, grazing spreads out, no shoving, no pile-up on the leader.
- **Profiling** against the budget above.

## Out of scope

Flow fields, separation between different herds, the actual mating and playing exceptions (hook only), Fabric, rendering fixes (measurement only).

## Tuning

All distances, weights and timings above are starting values, kept as named constants in one place per class, and tuned in playtest.
