# Herd movement: redesign

Date: 2026-10-08. Updated: 2026-10-10 (the lead reviewed the proposed diff and its decisions 19 to 33). Status: built (P1 to P12); P13 is in progress. Replaces the follower model in `2026-10-08-herd-movement-design.md`. Root causes: `.superpowers/sdd/2026-10-08-herd-movement/movement-investigation.md`.

## Problem

The layered design failed two playtests (20 Gallimimus, body then 2 wide and 3 tall; the Gallimimus hitbox is now 1.2 x 2.6).

| Seen in game | Root cause |
| --- | --- |
| Followers crowd and shove the leader; the leader cannot move | Separation is a soft push. Walkers never yield or slow down. The leader walks into standing followers. |
| Stuck on trees, walks "through" trunks, no re-path | The step check only detects. On a fail, seek aims straight at the target, through the trunk. Fallback paths aim at moving slots. |
| Stuck in narrow spaces, or inside or under blocks | `StepCheck` tests ground under the centre only, misses the top block when feet are not on a block edge, and its cache key ignores the body's offset in the block. Probes run only on a new column. |
| Random turns | Targets jump every 5 ticks, the stall rule fires on moving targets, and the separation side term flips. |
| No wedge; one blob or a column | Travel is 28% of the time in bursts of about 60 ticks. The slot geometry is too dense and is clamped to 5 blocks. |
| Members stack (push off) or spread badly | Nothing in movement stops a body entering another body. |
| All move when the leader moves; no grazing | The herd state follows every leader wander step (P(walk > 8) = 0.92). The anchor follows the leader. |

Tuning will not fix this. The follower movement model changes.

## Behaviour targets (the lead checks these in game)

| # | Target | Pass when |
| --- | --- | --- |
| T1 | Grazing members stay put | While the herd grazes, at any moment at least 70% of members stand, eat or rest. A member keeps one spot for 30 to 90 s. |
| T2 | Grazing spreads over the herd area | Members cover the area. No two graze spots are closer than the graze spacing (4.2 blocks for Gallimimus). |
| T3 | Small leader moves do not move the herd | The leader moves inside the herd area. No follower changes its spot because of it. |
| T4 | Spacing holds | No two members come closer than the hard minimum by their own movement: 2.2 blocks in open ground (the personal space), 1.7 in tight ground or beside a resting body (body width + 0.5). Bodies never visibly overlap for more than 1 s. A member on a fallback path may pass through herd mates (decision 22). |
| T5 | No stacking | Members that do overlap (spawn, push, teleport) separate within 5 s. |
| T6 | Travel only on real journeys | The herd travels only when the leader starts a journey (156 to 314 blocks). This happens every 2 to 5 minutes. |
| T7 | Followers use the leader's route | During a journey, followers walk the leader's walked route, round trees, 1 to 3 abreast. No member presses into a trunk for more than 2 s. |
| T8 | Fitting | No member ever enters a space its clearance box does not fit (leaves, slabs, low roofs, 2-wide gaps for the 1.25-wide Gallimimus box). |

## Decisions

| Topic | Decision (proposed) |
| --- | --- |
| Model | Trail following for travel. Context steering with real body sampling as the one local mover for all follower moves. Budgeted vanilla paths to fixed points as fallback. |
| Flow field | Not now. Kept in reserve if dense forest still fails after this change. |
| Herd state | Set by the leader's journey, not inferred from path length. |
| Herd area | A fixed centre that recentres only after a journey or when the leader stays outside it. |
| Spacing | A hard minimum in movement, a soft graze spacing in spot choice, vanilla pushing on as a backstop. |
| Fitting | Vanilla collision query on the full clearance box, on every movement tick. |

Why this model, against both playtests:

- Trees and narrow gaps (playtests 1 and 2): the leader's trail is a route its same-size body walked. Followers on it need no search. Context steering picks another heading when the sampled box is blocked; the old code had no alternative heading.
- Clustering and stacking: candidate headings that bring a body inside the hard minimum are masked, so a walker goes round or waits. It never pushes.
- Random turns: targets move smoothly along the trail (arc length, not 5-tick jumps). Heading hysteresis stops flip-flops.
- A per-herd flow field (option C) is robust, but costs a 48 x 48 Dijkstra with 3 x 3 x 4 clearance every few ticks, needs rebuilds on terrain change, and does not help grazing. The trail gives the same result for travel at almost no cost.

## Architecture

### Leader

A new behaviour `LeaderJourney` replaces the leader's `SetRandomWalkTarget` in `Dinosaur.getIdleTasks`.

| Leader action | When | What |
| --- | --- | --- |
| Graze move | Every 20 to 40 s | Walk 3 to 8 blocks to a free spot inside the herd area, at speed 0.8. The herd state does not change. A leashed leader makes no graze moves of its own (P13, in progress). |
| Journey | Every 2 to 5 min, or when water or food is outside the herd area | Pick a heading and a distance of 156 to 314 blocks. The pick does not read the blocks of the target, and the chunk of the target need not be loaded. It reads one point only: the first leg point, 24 blocks ahead, in a loaded chunk that ticks entities. So a boxed-in leader does not start a journey that cannot move. Set state TRAVEL at the start. Speed 0.8. The leader walks in legs of at most 24 blocks toward the target. The ground of a leg point is resolved only when its chunk is loaded. A leg point that is not usable is tried again with the heading turned by 20, then 40 degrees, to each side. If no leg is usable, the journey ends where the leader stands. A vanilla walk reaches at most the follow range (35 blocks) and the walk behaviour runs 150 to 249 ticks, then erases the walk target: on a roaming journey the leader sets the next leg each time a walk ends while it has moved at least 0.5 blocks since the previous end. An urgent trip's walk target belongs to the need, so that journey ends with it. A try that finds no usable leg is repeated after 200 ticks. A leashed leader starts no roaming journey. |
| Journey start rule (P13, in progress) | When a roaming journey is due | The leader needs at least 75% of its maximum stamina (150 for Gallimimus). It starts no roaming journey from sunset (day time 12000) until morning. A blocked try counts as a failed try and is repeated after 200 ticks. |
| Dragged leader | In GRAZE, the leader moves out of the herd area (plus 2 blocks) with no walk of its own (a lead, a push, a short teleport) | Start an urgent journey at once, so the herd follows the leader's route. It has no fixed target. It ends when the leader has stood still for 100 ticks. |
| Arrive | Leader within 3 blocks of the journey target, or its walk ends twice in one place (or an urgent trip's walk ends) | Hold TRAVEL for 100 more ticks (settle), then set GRAZE and recentre the herd area on the leader. A rest of the leader is not a walk end (P13, in progress). |
| Wait for stragglers | During a roaming journey, fewer than 75% of the counted followers are close to their own slot (up to 5 blocks beyond the slot's distance from the leader) | Walk at the slow speed 0.6 for the whole wait, with no stop (P13, in progress). Before P13 the leader slowed to 0.5 for 40 ticks, then stopped. Wait at most 200 ticks per journey, then continue. Only followers whose slot is on the trail count. Members more than 48 blocks from the leader do not count. Urgent trips never wait. |
| Rest pause (P13, in progress) | A roaming journey, and the leader sits down, rests or gets up | Pause the journey. After the get-up, walk on to the same target. See "Journey pauses". |
| Fight chase (deferred) | The leader chases or fights, and the chase leaves the herd area | Deferred (lead, 2026-10-10): the mod has no flee, attack or attacker handling yet, so no fight chase happens in play. Decision 16 applies unchanged until then. |

The leader's core-task walk targets (water, feeder) count as a journey when they end outside the herd area. A planned change (the journey pauses for the need and goes on) is deferred; see "Journey pauses".

### Herd states

| State | Followers do |
| --- | --- |
| GRAZE | Hold a graze spot. Small idle moves only. |
| TRAVEL | Follow the trail in travel order. |

### Graze

- **Herd area.** Centre `C` and radius `R = s x (g x sqrt(N / pi) + w / 2)`. `g` is the graze spacing (personal space + 2 = 4.2 for Gallimimus), `N` the herd size, `w` the body width, `s` the area scale (`sqrt(6)`: the area is 6 times the area of the graze squares alone; decision 25). Gallimimus: 14.4 blocks for 5, 27.4 for 20.
- **Recentre.** `C` moves to the leader at the end of a journey, or when the leader has stood outside `R` for 100 ticks (not for a dragged leader: that is a journey). Nothing else moves `C`.
- **Spot choice.** On entering GRAZE (staggered over 0 to 60 ticks), a member samples 8 candidates inside `R` and within the fallback range (24 blocks) of itself. A candidate must: fit the clearance box (see Fitting), be farther than `g` from every other reserved spot, and be reachable in a straight line (the box sampled every block on the way). Take the roomiest. The spot is a reservation held in `DinoGroup`. A member walks to its spot at speed 0.6 inside the herd area and at 1.2 outside it (decision 20).
- **Stay.** A member holds its spot for 600 to 1800 ticks. Eat and Rest run as now. Then, with a 50% chance, it picks a new spot at most 6 blocks away. Otherwise it stays another period.
- **Invalid spot** (blocked, or outside `R` after a recentre): pick again. A spot farther than the fallback range (a straggler after a journey): drop it and walk back inside the herd area first. No spot found: stand still and try again after 100 ticks; if standing outside the herd area, walk inside it first (with the stuck rule and a fallback path to the centre, where the leader stood).

### Travel

- **Trail.** The leader records a point every 1 block walked (not every 5 ticks), 128 points. Each point keeps its arc length from the journey start and the open width beside it (measured once, with the clearance box).
- **Travel order.** At the journey start, followers are ordered by distance to the leader, nearest first. The order holds until the journey ends or a member joins or leaves.
- **Lanes.** The followers stand in rows behind the leader. Row `r` (0 is nearest the leader) holds at most `2 + r` followers, never more than 3, and never more than the ground allows. The ground allows 3 lanes where the side room is at least one spacing, 2 where it is at least half a spacing, else 1. A row is laid 4.2 blocks behind the row ahead where the ground there gives it all the lanes it wants (open spacing: personal space + 2), else 3.2 blocks behind (tight spacing: personal space + 1), as in decision 9. The spacing is also the gap between the lanes of the row. A partly filled row is centred. In a gap the lane count drops to 1 and the line gets longer. Every follower has its own target arc and lane offset, so no two share a target point. A row's lane count uses the tightest open width within 2 blocks of the row's arc, so it does not flicker between trail points. A sneak-use of the debug stick on a dinosaur sets the tight spacing everywhere, for comparison.
- **Following.** Each follower steers to a look-ahead point 3 blocks ahead of its own projection on the trail, plus its lane offset, clamped to the tightest open width from its projection to 4 blocks past the look-ahead point (about one turning radius), so an outer-lane follower moves in before a narrowing instead of hitting its edge. Speed comes from the arc gap to its target arc: 0 when 0.5 blocks or more ahead, 1.2 when 3 blocks or more behind, and between them the ground speed rises in a straight line (so the speed setting is 1.2 times the square root of the ramp; see "Speeds"). It never passes the member ahead in its lane. On a roaming journey a follower starts a rest only when its stamina is critical (below half the rage threshold); on an urgent trip it rests whenever tired (decision 17). A rest begun before the journey ends on its own.
- **Joining.** At the start, a follower steers to its projection on the trail. If it is more than 12 blocks from the trail, it asks for a fallback path to that fixed trail point. A follower whose target arc is not on the trail yet waits where it stands; if it stands in the leader's line toward the journey target (ahead of the leader, within 16 blocks, inside the hard minimum plus 0.5 of the line), it first steps out of that line once, perpendicular to it.

### Journey pauses (P13, in progress; lead rulings of 2026-10-10)

- **Rest pause.** The leader keeps resting at normal tiredness. On a roaming journey, a leader that sits down, rests or gets up pauses the journey. The leader sets no leg and records no walk end while it is down. After the get-up it walks on to the same target.
- **The herd during a rest pause.** In daytime, for a leader rest of up to 400 ticks (20 s), every member keeps its formation slot, and a tired member lies down in its slot. At night, or when the leader's rest runs past 400 ticks, members may leave their slots and spread out to rest, tired or not, as in grazing. When the leader is up, the wedge forms again.
- **Night.** A journey that night catches pauses until morning. No new roaming journey starts from sunset until morning.
- **Stamina.** Rest stops stay part of a journey. Walking drains 25% less stamina (running is unchanged). A roaming journey starts only with at least 75% stamina.
- **The leader's needs (deferred).** Deferred (lead, 2026-10-10): the herd work makes herds move and behave properly before hunger, thirst, stats, genes and combat are added. Hunger and thirst tick only on a daily schedule today. When needs are added: on a roaming journey the leader may go for water or food at a normal need. If the water or food is within 32 blocks of the leader or of the next one or two legs ahead (about 24 to 48 blocks of the route), the journey pauses, the herd goes along, members that need it drink or eat, and then the journey goes on to the same target. If it is farther, the leader waits until the need is critical, and then does the same. The leader's need walk is not a walk end of the journey. During such a pause the herd drinks and eats at a normal need and rests when tired; this exception to decisions 8 and 17 applies only while the journey is paused (lead, 2026-10-10).
- **Straggler wait.** The leader walks at the slow speed (0.6) for the whole wait. It does not stop.
- **Logs.** The server writes one log line per journey end: the reason, the distance left to the target and the journey length. While the debug visuals are on for a herd, it also writes a line at each start and end of a straggler slow walk and at each make-way ask.
- **Fight chase (deferred, lead 2026-10-10: the mod has no flee, attack or attacker handling yet).** When combat exists: a leader's fight chase that leaves the herd area is no longer an urgent trip. The herd moves a short way from the fight spot (12 blocks to start), waits there for the leader, and joins it when the fight ends. If the threat comes toward the herd, the herd flees. The design is still to be written (P13c); it changes decision 16 for fight chases.
- **Journey target height (planned with P12c).** A journey target is at most 8 blocks above or below the herd.

### Speeds

Vanilla ground speed is not linear in the speed setting. It is quadratic: `Mob.setSpeed(s)` also sets the forward input to `s`, and the input is scaled by the speed again. All herd code uses one pure helper (`GroundSpeed`). Ground speeds come from the speed settings that the lead chose (decision 19):

| Move | Speed setting | Ground speed (blocks per tick) |
| --- | --- | --- |
| Graze walk to a spot (inside the herd area), unstack, side step | 0.6 | 0.049 |
| Leader graze move, journey | 0.8 | 0.086 |
| Catch-up, regroup, walk back into the herd area | 1.2 | 0.19 |
| Leader slow walk for stragglers (P13) | 0.6 | 0.049 |

A slow-down factor acts on the ground speed, and the speed setting is converted back. A walking member is never below setting 0.45 (0.027 blocks per tick), the floor that keeps the walk animation on; below it the member stands.

### Context steering (per follower, every 2 ticks or more, on a fixed stagger by entity id)

At most 10 evaluations per herd per tick. A member is due when at least `max(2, ceil(herd size / 10))` ticks have passed since its last evaluation; a member the budget refused is due again the next tick, so the members that tick last are not starved. The first evaluation is staggered by entity id. The others keep their last heading. A stopped member re-evaluates on the same cadence, not every tick.

1. **Candidates.** 12 headings around the circle, plus "stop".
2. **Interest.** Cosine to the desired direction (look-ahead point or graze spot), minus a turn cost (the body turns 10 degrees per tick).
3. **Masks** (candidate removed): the clearance box at 1 block ahead does not fit (every heading); a hazard or a drop over 3 blocks; the body would come inside the hard minimum of a member within 10 ticks (members' current positions and velocities); the guard stopped the body at blocks on this heading since the last evaluation. Then only the previous heading and the 4 best open headings by interest are probed at 2 blocks (mask) and 3 blocks (danger); the other open headings are not probed and count as masked. At most 22 box tests per evaluation.
4. **Danger** (score penalty): the box does not fit at 3 blocks (0.5); a member within the graze spacing (0.3, while grazing).
5. **Pick.** The best candidate. Keep the previous heading unless the new one is better by 0.05 (hysteresis; 0.15 was more than an adjacent heading can gain, so a walker could not straighten up). Stopping scores -0.3: a sideways step round an obstacle beats it, a turn back does not. All masked: stop.
6. **Speed.** Wanted speed x arrival factor (slow within 3 blocks of the target, down to 0.3) x free-distance factor (slow as the nearest member on the heading gets within 1 block of the hard minimum, down to 0.4). A walker that closes on a moving member ahead matches its speed early and smoothly (within 5 blocks, settling 1 block past the hard minimum), so it does not run into the mask or the guard.

**Guard, every tick, all modes (steering, path, leader).** Before `zza` is set, test the clearance box 0.6 blocks ahead along the actual body yaw. If it does not fit, or it is inside the hard minimum of a member, speed is 0 for this tick and the horizontal velocity is cut to 20% (friction alone would let the body slide on about 0.7 blocks). The member test looks along the move direction while the yaw is within 45 degrees of it, so a turn does not brake the body. This makes T4 and T8 hold even between evaluations and on vanilla paths. A fallback path that the guard holds for 20 ticks is stopped, so the stuck rule can plan again. While a member walks a herd fallback path, herd mates do not stop it (the guard tests blocks only; decision 22).

### Spacing and stacking

| Rule | Value |
| --- | --- |
| Hard minimum (centre to centre) | The personal space, `w + 1` (2.2 for Gallimimus), in open ground. `w + 0.5` (1.7) where the ground is tight, and beside a resting body. Enforced by masks and the guard. Ground is tight where neither side across the heading has room for the personal space plus a box width (3.45 for Gallimimus). Decisions 4 and 24. |
| Graze spacing | `g` = personal space + 2 (4.2). Used for spot choice only. |
| Who yields | Rank: leader first, then travel order. When two walkers conflict, the lower rank waits. Walkers go round standing members. A walker that yields for 160 ticks in a row counts as blocked, and the stuck rule asks for a fallback path. |
| Make way | If the leader is masked by a standing member for 10 ticks, that member steps sideways. The same holds for any follower held for 20 ticks by a standing member of lower rank, or by a member that does not try to move (arrived, resting, eating). The leader is never asked. The member steps perpendicular to the walker's heading (never along it), on its own side of the line, until it is the hard minimum plus 0.5 beside the line (2.7 blocks for Gallimimus in open ground, 2.2 in tight ground; a fixed 2-block step would leave it inside the minimum). It crosses to the other side only if that side has more room. A step takes at most 40 ticks. A member asked is not asked again for 60 ticks. A resting member gets up for the leader's ask only (see Rest). |
| Unstack | A member closer than the hard minimum to another for 10 ticks: the lower rank steers away (interest = away, masks as usual) until clear, for at most 40 ticks. An eating member finishes its eat first (at most 69 ticks). A sitting or resting member stays down (see Rest). (10, not 20: a legal pair that becomes too close must be apart again inside T4's one second.) |
| Rest | A member that sits down stands still during the sit-down phase (about 69 ticks). Crowding and unstack by a follower never clear a rest; herd mates walk round the body. The leader's make-way ask gets a resting member up after its sit-down phase, because the leader has no way round. A member with a make-way side step in progress (the wait for its get-up included) does not start a new rest; after the step it may rest where it stands. Decision 7 is narrowed to this rule. |
| Ghosting | While a member walks a herd fallback path, herd mates do not stop it. Vanilla pushing keeps the bodies apart. No unstack starts during the path or for 20 ticks after it. |
| Vanilla pushing | On between all members (playtest 2: off made them stack). It acts only when movement rules failed. |

### Fitting

- **Clearance box** = the larger of the hitbox and the visible model, times the growth scale and the size gene, plus a margin (0 per side, 0.25 on top), rounded up to 0.25 (decisions 5, 11 and 24). Model length is not in the box. The size gene never raises the box above what the hitbox needs. Gallimimus (hitbox 1.2 x 2.6): 1.25 x 3.0.
- **Fit test** = the vanilla collision query on the box at the probe position (`noCollision` in 1.20.1 Mojang names), at feet height or raised by the step height (1.0). Real collision shapes: slabs, leaves, carpets, partial blocks count as they are.
- **Ground** = some collision under any column of the footprint within 3 blocks, no fluid, fire, cactus, fence or electric fence under the footprint.
- **Cache** per herd, keyed by the box position rounded to 0.25 block, the feet height and the box size. Stand results (fit, ground, hazards) are kept 20 ticks: members move 0.05 to 0.2 blocks per tick (see "Speeds"), so a per-tick cache would be empty for every probe, while the ground does not change faster than a second; followers in a column probe the same cells a few ticks apart and share them. The guard's fit test is cached per tick.
- **Paths:** vanilla `GroundPathNavigation` sizes nodes to 3 x 3 x 4. Keep it. Do not write a pathfinder. The mod's `FenceAwareNodeEvaluator` asks `BodyFit` (`pathProbe`) whether the clearance box stands at a node, so a path never leads a herd member where the guard would stop it. It loads no chunk. The guard still runs on path moves.

### Stuck and re-path

- **Progress** = arc progress on the trail (travel) or distance to the spot (graze). Waiting behind a member, or the leader waiting, is not stuck.
- **Stuck** = less than 0.5 blocks of progress in 40 ticks while not waiting, or every heading masked by blocks (not by members) for 10 ticks in a row, or a guard stop at blocks for 10 ticks in a row. A follower that cannot make 0.5 blocks of progress before its stop line behind a leader that waits or settles is waiting, not stuck.
- **Fallback goal** = a fixed point: in travel, the trail point 8 blocks ahead of the follower's projection (the leader stood there); in graze, its spot, or the herd centre for a member walking back inside the area. Never a moving slot.
- **Budget** (kept): 2 fallback paths per herd per tick, 60-tick cooldown per member, range 24 blocks. A stuck member asks every tick for 40 ticks (the budget queues only members that keep asking). Two failed fallbacks in graze: pick a new spot.

## Components

| Class (`entity.ai.behaviour.social`) | Decision | Notes |
| --- | --- | --- |
| `LeaderTrail` | Keep, change | Record by distance, 128 points, arc length, projection, point at arc, open width per point. Pure. |
| `WedgeLayout` | Delete | Replaced by `TravelOrder` (order, lanes, target arc). Pure. |
| `HerdArea` | Keep, change | New radius formula; keep `roomiest`. Delete the weighted `anchor`. Add a reachable filter (a predicate in). |
| `HerdSteering` | Replace | New `ContextSteering`: candidates, interest, masks as inputs, hysteresis, speed factor. Pure. Keep the turn limit constant. |
| `HerdSpacing` | Keep, shrink | Keep `applies` (the single switch) and personal space. Delete `steer`. Add `hardMinimum` (open, tight and resting) and `wouldCrowd`. Keep `away` for unstack. Add the tight-ground and make-way tests. |
| `StepCheck` | Replace | New `BodyFit`: clearance box, vanilla collision query, footprint ground, hazards, fixed cache key, `openWidth` with the box. The only class that reads blocks. |
| `ClearanceBox`, `HerdBodies`, `GroundSpeed`, `GrazeSchedule`, `ProgressMonitor`, `HerdNeeds` | Add | Box size per species. Per-tick snapshot of the herd bodies for the masks. The speed law. Spot timing. Stuck window. Journey rules for needs and rest. |
| `RunGait`, `TurnEasing` | Add | Walk or run choice from ground speed. Eased turning. See "Animation and smoothing". |
| `HerdDebugVisuals`, `DebugShapes` | Add | Debug-stick layers per herd: steering, travel slots and trail, graze spots and area, clearance box and path nodes. |
| `HerdMoveControl` | Keep, rewrite `tick` | Modes: stop, steer, path, unstack, side step. The guard. Delete step aside and `stepFailures`. |
| `FollowFormation` | Keep, shrink | Picks the target per state, progress monitor, fallback requests. Delete squeeze, drop-back, centre-line hold, `isTaken` discs, per-tick regroup target. |
| `FallbackBudget`, `TickTimer`, `HerdTimings`, `HerdState` | Keep | |
| `DinoGroup` | Keep, change | Add centre, spot reservations, travel order, explicit state. Delete slot assignments and the 40-tick path inference. |
| `HerdLeading` | Rewrite | Trail recording, wait rule, state from `LeaderJourney`. |
| `LeaderJourney` (new) | Add | Graze moves and journeys. The policy part (`JourneyPolicy`) is pure. |
| `Dinosaur` | Change | Replace the leader `SetRandomWalkTarget` with `LeaderJourney`. Keep `setYRot` clamp, step height 1, `HerdMoveControl`, pushing. Fix the comment on pushing. |

Portability: pure classes import nothing from Minecraft. Block access is only in `BodyFit` (the path node test in `FenceAwareNodeEvaluator` calls it), so the 26.2 port changes one place. Only SmartBrainLib behaviours, vanilla navigation and the vanilla collision query are used; no Forge events.

## Performance budget

| Measure | Budget | Estimate |
| --- | --- | --- |
| Herd AI, 20 animals, average | under 1 ms per tick | An uncached box test (collision query, hazard and ground reads) is about 8 microseconds. Grazing: about 3 evaluations per tick (most stand), about 0.2 ms. Travel: 10 evaluations x at most 22 box tests, about half of them hits in the 20-tick cache along a column: about 0.5 to 1 ms. |
| Any single tick | under 5 ms | At most 10 context evaluations per herd per tick; others keep their last heading (the guard still runs). A tick with two spot picks (8 straight-line reaches each) stays under 3 ms. |
| Fallback paths | at most 2 per herd per tick | Kept from `FallbackBudget`. |

## Testing

**Unit tests** (pure classes): trail arc, projection and tightest room over a range; `TravelOrder` gives distinct targets in every lane count and clamps the lane offset by the room ahead; `ContextSteering` masks, hysteresis, all-masked stop; `HerdSpacing.wouldCrowd`; `HerdArea` radius, roomiest, reachable filter; `JourneyPolicy` timing; `DinoGroup` journeys, incl. a walk that ends twice in one place; `FallbackBudget` (existing).

**GameTests** (class `HerdGameTests`). Each test first asserts that every animal stands on the arena floor and fails at once with "spawned below the floor" if not (that bug is under separate investigation).

| Test | Arena | Command | Pass |
| --- | --- | --- | --- |
| `grazingHerdHoldsHardMinimum` | `herd_track` | `/test run herdgametests.grazingherdholdshardminimum` | Pinned GRAZE, 1200 ticks: no pair under its hard minimum (2.2 in the open, 1.7 in tight ground) for more than 20 ticks in a row. |
| `grazingMembersStayPut` | `herd_track` | `/test run herdgametests.grazingmembersstayput` | Pinned GRAZE, 1200 ticks, after 700 ticks of settling: in 90% of sampled ticks, at least 70% of members move under 0.01 b/t. |
| `smallLeaderMovesKeepGraze` | `herd_track` | `/test run herdgametests.smallleadermoveskeepgraze` | After 300 pinned grazing ticks the leader walks 5 blocks inside the area every 100 ticks: state stays GRAZE, no follower that holds a spot changes it before its minimum stay (followers without a spot are not counted). |
| `herdTravelsAlongTrail` | `herd_track` | `/test run herdgametests.herdtravelsalongtrail` | Journey of 45 blocks (beyond one vanilla walk): back in GRAZE with the new centre within 6 blocks of the goal and all within `R` of it in 1700 ticks; no pair under the hard minimum for more than 20 ticks. |
| `herdCrossesForest` | `herd_forest` | `/test run herdgametests.herdcrossesforest` | Back in GRAZE with the centre within 6 blocks of the goal and all within `R` of it in 2400 ticks; no member without progress for 100 ticks; at most 2 fallbacks in one tick. |
| `herdPassesCorridor` | `herd_corridor` (new, 3 wide, 20 long) | `/test run herdgametests.herdpassescorridor` | All pass within 1900 ticks; no two followers share a target point (two lanes in one row share an arc). |
| `memberNeverEntersLowGap` | `herd_lowgap` (new; the tested gap is 2.8125 high under a closed top trapdoor: taller than the 2.6 hitbox, lower than the 3.0 box; the 2.5 gap under a top slab is a control) | `/test run herdgametests.memberneverenterslowgap` | No member's position is ever inside the gap, during the journey and 300 ticks of grazing after it. |
| `stackedMembersSeparate` | `herd_arena` | `/test run herdgametests.stackedmembersseparate` | 3 spawned on one block: all pairs at the hard minimum or more within 100 ticks. |
| `herdSkirtsWater` (existing) | `herd_shore` | `/test run herdgametests.herdskirtswater` | A journey along the channel: back in GRAZE with the centre within 6 blocks of the goal and all within `R` of it in 2000 ticks; no member ever in water. |

Test names are lower case in commands (Forge `@GameTestHolder`, no template prefix). The arenas are small, so every test herd has an area scale of 1.0, not `sqrt(6)`; the tests check movement, and the lead checks the big area in game. The time limits follow the real ground speeds (lead decision after playtest 1, group 4); they replace decision 18's 1200 and 1500 ticks.

**In game, by the lead** (20 Gallimimus, plains with scattered trees, 10 minutes, debug stick for state and timings):

1. Most animals stand, eat or rest; a given animal keeps its place for half a minute or more (T1).
2. The herd covers a wide patch, not a knot round the leader (T2).
3. The leader shuffles a few blocks; nobody follows it (T3).
4. No two bodies overlap for more than a second; no shoving (T4, T5).
5. Every few minutes the leader sets off; the debug stick shows TRAVEL from the first step (T6).
6. The herd strings out behind the leader along its route, 1 to 3 abreast, and goes round trees (T7).
7. No animal stands under leaves or in a gap its body does not fit (T8).
8. Debug stick timer: average under 1 ms, worst under 5 ms.
9. (P13, in progress) A journey goes on after the leader rests, to the same target. The leader does not stop for stragglers; it walks slowly. A leader that is low on stamina or caught by night does not start a roaming journey.

## Animation and smoothing

These parts make herd movement look right. They are not part of the movement model. The Gallimimus is the species that the lead tests.

- **Animation per state (P10, decision 26).** Every animation of a species plays in a fitting state. Each state has an ordered list of names; the first name that the species has plays. Calling plays when the leader starts a roaming journey; the first walk waits until it ends (at most 60 ticks). Sentry plays when the leader stands on watch while the herd grazes (a check every 200 to 400 ticks, 30% chance). Startled plays when a threat comes within 6 blocks (cooldown 400 ticks). A dead animal with no `dead` animation plays nothing. A species without an eat, drink or get-up animation plays idle.
- **Gait (P12a).** Walk, run and idle follow a smoothed ground speed with a minimum hold, so one late position packet does not flip the gait. The run animation starts above setting 1.05 and ends below 0.95. The leg cycle rate follows the ground speed, so feet do not slide. Animation controllers blend over 4 to 5 ticks.
- **Idle (P12a, decision 27).** Every species adds a slow breathing sway in code to the bones hips, neck1, head, tail1, tail2 and tail3 (those that exist), at the Gallimimus `misc.idle` amplitudes. It plays always, except on a dead animal. Idle variants play for at least one full cycle. For the Gallimimus, `misc.idle` has weight 6 and each variant weight 1 (a variant about every 16 s per animal). Its `rest` and `restidle` are copies of `misc.idle`, and it has no `getup` (art, not code).
- **Motion (P12b).** Turning ramps in and out (the turn rate grows 3 degrees per tick up to 10, and falls as the turn ends). The body yaw follows the head yaw by half the gap each tick, at most 15 degrees. A follower that catches up matches the speed of the member ahead early. A follower stands still while it sits down.
- **Softer defaults (P13, in progress; ruling 8).** A slower turn ramp, a gentler body-yaw ease and earlier speed matching, about 1.5 times softer. The lead judges them in the next playtest.
- **Calmer stops (P13, in progress; ruling 3).** No idle variants while the herd travels: only the main idle with the sway. A get-up with no get-up animation plays the main idle.

## Planned: cliff edges (P12c, after P13)

The drop limit stays 3 blocks. Ground must be under the centre column of the body for every herd placement check. For all dinosaurs, path nodes with ground under one column only are blocked, and rim nodes cost more. The guard then checks the ground ahead. Side steps stay on their own side's room.

## Out of scope

Flow fields (reserve), separation between herds, mating and play exceptions (the `applies` hook only), the 26.2 port itself, rendering (apart from the animation hooks above).

## Tuning

All numbers are starting values. They are named constants, one place per class, tuned in playtest.

## Lead decisions (2026-10-08)

The lead answered the open questions below. Where an answer changes a value above, the answer wins.

| # | Topic | Decision |
| --- | --- | --- |
| 1 | Travel shape | A wedge in open ground. It narrows to 1 to 3 abreast, then single file, where the ground is narrow. |
| 2 | Waiting | On roaming journeys, the leader waits up to 10 s for stragglers. On urgent trips (thirst, hunger, fleeing) it never waits. |
| 3 | Journeys | Roaming journeys every 2 to 5 minutes, 32 to 64 blocks. Needs (water, food) also drive journeys. |
| 4 | Hard minimum | The personal space (3 blocks for Gallimimus) while moving. Resting members may be closer (value to tune). |
| 5 | Clearance box | A per-species box taken from the model size. Also fix species hitboxes that do not match their models (separate task). |
| 6 | Grazing | Keep a spot for 30 to 90 s, then a 50% chance of a short move. During the stay, eat, rest or idle with animations. |
| 7 | Stacking | A resting member may get up to separate from a stacked neighbour. |
| 8 | Needs on a journey | A follower leaves the line to drink or eat only when the need is critical. |

## Lead decisions on the plan review (2026-10-09)

The lead answered the gaps found while writing the plan. These answers also override values above.

| # | Topic | Decision |
| --- | --- | --- |
| 9 | Travel spacing | Spacing scales with body size (it comes from the personal space). In open ground the slots are personal space + 2 apart (5 for Gallimimus). In tight ground they are personal space + 1 apart (4). A row is "open" when the ground there gives it all the lanes it wants at the open spacing. A debug-stick toggle sets 4 everywhere, so the lead can compare the two in a playtest. |
| 10 | Row width | At most 3 abreast. Fewer where the ground is narrow, and single file in gaps. |
| 11 | Fit box | The larger of the hitbox and the visible model's width and height (times the growth scale), plus a margin. Model length is not in the box. |
| 12 | Neighbours' destinations | Avoided while grazing only, not while travelling. |
| 13 | Outside the herd area | A member without a spot that stands outside the herd area walks back inside it. |
| 14 | Stuck or waiting | Blocked by blocks = stuck (fallback path). Blocked by members = waiting (not stuck). |
| 15 | Follow speed | Speed ramps with the gap to the follower's slot. |
| 16 | Unplanned trips | A walk target the herd did not plan that leaves the herd area counts as an urgent journey. |
| 17 | Rest on a journey | On a roaming journey, a follower starts a rest only when its stamina is critical. On an urgent trip, it rests whenever it is tired. A rest begun before the journey ends on its own. |
| 18 | GameTest limits | Keep the travel limit at 1200 ticks and the corridor limit at 1500 ticks. |

## Lead decisions after the plan review (2026-10-09 to 2026-10-10)

The lead decided these during the playtests. They override values above. Entries marked P13 are being built now.

| # | Topic | Decision |
| --- | --- | --- |
| 19 | Speeds | The old build's speed settings: graze walk 0.6, leader graze move 0.8, journey 0.8, regroup and catch-up 1.2. The speed law is quadratic (see "Speeds"). |
| 20 | Hitbox and walk-in | The Gallimimus hitbox is 1.2 x 2.6; other species do not change. A member outside the herd area walks to its graze spot at the catch-up speed (1.2). |
| 21 | Stragglers and paths | The leader counts only followers whose slot is on the trail. Herd animals use box-aware path nodes. |
| 22 | Ghosting | Members may pass through herd mates on fallback paths only. Vanilla pushing still keeps bodies apart. |
| 23 | Dragged leader | A leader that is dragged beyond the herd area starts an urgent journey, so the herd follows its route. |
| 24 | Box margin and tight ground | The box margin per side is 0 (Gallimimus box 1.25 wide). The hard minimum is `w + 0.5` (1.7) in tight ground and 2.2 in the open. |
| 25 | Area and journeys | The herd area is 6 times the area of the graze squares (radius x `sqrt(6)`). Journeys are 156 to 314 blocks, walked toward a far target in loaded legs. |
| 26 | Animations | Hook up all animations per state. The three proposed triggers are accepted: calling at a roaming journey start, sentry on watch, startled at a threat within 6 blocks. |
| 27 | Smoothing | Idle blend by an always-on sway in code, for all species. Fix animation smoothing, catch-up stop-start, turning and cliff edges. The drop limit stays 3. A follower stands still while it sits down. |
| 28 | Journey pauses (P13) | A leader's rest pauses a roaming journey and it goes on to the same target. Daytime pause up to 400 ticks: members keep their slots and tired ones lie down in their slots. At night, or longer: members may leave their slots, spread out and lie down, tired or not; the wedge forms again when the leader is up. Followers' normal-need resting applies only while the journey is paused. A journey caught by night pauses until morning; no roaming journey starts from sunset. |
| 29 | Stamina (P13) | Walking drains 25% less (walking only; running is unchanged). A roaming journey starts only with at least 75% stamina (150 for Gallimimus). |
| 30 | Stops and needs (P13) | The straggler wait is a slow walk, not a stop. No idle variants while the herd travels. A get-up with no animation plays plain idle. The straggler slow speed is 0.6. The leader's water and food detour on a journey is deferred until needs are part of herd testing. |
| 31 | Logs and leash (P13) | One server log line per journey end (reason, distance left, length). While the debug visuals are on: log lines for straggler slow walks and make-way asks. A leashed leader makes no graze moves. A walk that ends from a rest is not an arrival. |
| 32 | Fight chase (deferred) | A leader's fight chase is no longer an urgent trip. The herd moves a short way away, waits for the leader, and flees if the threat comes toward it. Deferred until the mod has flee, attack and attacker handling; decision 16 applies unchanged until then. |
| 33 | Journey target height and easing | A journey target is at most 8 blocks above or below the herd. Softer easing defaults now; the next playtest judges them. No slow-down near walls (tune in playtest). |

Decisions 3, 4, 7 and 16 above are changed by these: the journey length (3, now 156 to 314 blocks), the hard minimum (4, now 2.2 and 1.7), the rest rule (7, narrowed in "Spacing and stacking") and unplanned trips (16, fight chases excepted).

## Open questions for the lead (answered above)

1. **Travel shape.** Accept 1 to 3 abreast along the trail (a loose column that widens in open ground) instead of a true wedge? A wedge needs more width than most terrain gives.
2. **Waiting.** Should the leader wait for stragglers (up to 10 s when fewer than 75% are close)? Yes or no.
3. **Journeys.** Every 2 to 5 minutes, 32 to 64 blocks. Confirm or give other values.
4. **Hard minimum.** Body width + 0.5 (2.5 for Gallimimus), or the personal space (3)?
5. **Clearance box.** If the model is larger than the 2 x 3 hitbox, animals look "inside" blocks while legal. Use the hitbox plus 0.25, or a per-species box from the model size?
6. **Grazing stay.** 30 to 90 s per spot, then a 50% chance of a short move. Confirm.
7. **Rest and stacking.** May a resting member get up to unstack, or does only the walker move?
8. **Follower needs on a journey.** May a follower leave the line to drink or eat during a journey, or only when the need is critical?
