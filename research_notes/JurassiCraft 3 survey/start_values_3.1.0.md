# JurassiCraft 3.1.0: start values for the planned work

**Scope:** the numbers and rules from JurassiCraft 3.1.0 (NeoForge 26.1.2, CAZ LLC, paid, proprietary) that the dev plan does not hold, but that task plans will need. The plan decisions are D380 to D393 in `docs/plan/engineering-backlog.md`. This note is raw reference, not a survey report. It covers the body pose and ground sampler (F2.1 parts B and C), wading and remembered spots (F2.1, F2.4), path fixes (F2.5), tree climbing and gliding (no Nublar species yet), and flight (Release 2 Pteranodon).

**Clean room.** The note describes behaviour, state, steps and numbers in my own words. It copies no code. It reads no licence, activation or integrity code.

**Labels:** `[v]` read in the decompiled 3.1.0 source or its data files. `[inf]` inference. Nothing was run. Every value is a start value for Nublar, to tune in game.

**Date of reading:** 2026-11-04, against the 3.0.14-hotfix jar for the diff. The jar is `jurassicraft-neoforge-26.1.2-3.1.0.jar`. The decompiled tree was in a session scratchpad and is not kept. Class paths below are obfuscated names under `llc/caz/jurassicraft/` in that decompile.

---

## 1. Body pose and ground sampler (F2.1 parts B and C)

Nublar takes the body pose (D380), the sampler (D381) and its own leg IK bands (D393). JC3 has no knee solve: a leg root bone moves up or down. Nublar keeps FABRIK, so the leg rows here are weights and limits only.

### Species data `[v]`

| Field | Meaning |
|---|---|
| `foot_anchors` | 0 to 4 entries: `slot` (rear_left, rear_right, front_left, front_right), `offset` [x, z] in blocks at scale 1, `half` [width, length], `bone` (leg root) |
| `leg_length`, `front_leg_length` | Blocks at scale 1 |
| `grounding` (optional) | `max_step_up`, `max_step_down`, `max_body_lift`, `max_body_drop` (0 to 16), `max_pitch`, `max_roll` (0 to 30°), `tilt_scale` (0.01 to 1) |

- Grounding is off for a species with no anchors, an adult leg length under 0.5 blocks, a layout that is not 2 rear or 2 rear plus 2 front, or a rear track width × scale under 0.25 `[v]`.
- Two sauropods set `tilt_scale` (0.88 and 0.89). No species overrides another cap `[v]`.
- Sample anchors `[v]`: T. rex leg 1.4176, rear feet at x ±0.396, z −0.288. Triceratops leg 1.2478, front 1.0681. Brachiosaurus leg 2.0099, front 2.4355. Velociraptor 1.2718. Dilophosaurus 1.453. Gallimimus 1.6387.

### Derived caps (L leg length, F front leg length, s scale) `[v]`

| Value | Rule |
|---|---|
| Step up | clamp(0.35 × L × s, 0.6, 1.5) |
| Step down (no ground inside = no support) | clamp(0.9 × L × s, 1.25, 4.0) |
| Body lift | 0.08 × L × s + the smallest per-leg raise (at least 0.25, at most L × s) |
| Body drop | = step down |
| Leg extend | rear 0.22 × L × s, front 0.16 × F × s |
| Leg raise | rear 0.15 × L × s, front 0.12 × F × s |
| Pitch (four legs only) | min(12°, degrees(3 ÷ (r × s))) × `tilt_scale`; r = largest hitbox corner distance from the origin |
| Roll | min(6° two legs or 8° four legs, 0.75 × degrees(3 ÷ (r × s))) × `tilt_scale` × clamp(track × s ÷ 0.5, 0, 1) |
| Plant tolerance | clamp(0.25 × max(half) × s, 0.05, 0.25) |
| Size factor (slows all rates) | clamp(L × s ÷ 3, 0.8, 1.3) |

Adult T. rex by hand `[inf]`: step up 1.04, step down 2.68, extend 0.66, raise 0.45, roll at most 6°, no pitch. Nublar forbids a leg stretch, so the extend limit can serve only as a reach limit for the IK target `[inf]`.

### Support and fit `[v]`

| Feet on the ground | Free axes | Confidence |
|---|---|---|
| Two legs, both down | lift, roll | 1.0 |
| Four legs, 4 down | lift, pitch, roll | 1.0 |
| Four legs, 3 down | lift, pitch, roll | 0.7 |
| Four legs, 2 at the same end | lift, roll | 0.45 |
| Four legs, 2 on the same side | lift, pitch | 0.45 |
| Four legs, 2 diagonal | lift | 0.35 |
| Fewer than 2 | none | 0 |

- Plane fit: foot height = lift + z × sin(pitch) − x × cos(pitch) × sin(roll). All axes free: solve the 3 × 3 normal equations, with a fallback when the determinant is under 1e-6. One slope free: a fit pulled toward the stored slope. Lift only: the mean `[v]`.
- Clamp to the caps, then lift the body until the worst foot is within its raise limit plus the plant tolerance `[v]`.
- Leg offset = foot residual × gait gain, clamped to [−extend, +raise] `[v]`.

### Gait gain on leg reach `[v]`

| Band | Horizontal speed (blocks per tick) | Gain |
|---|---|---|
| Standing | under 0.0075 | 1.0 |
| Very slow | 0.0075 to 0.05 | 1.0 falling to 0.8 |
| Walking | 0.05 to 0.18 | 0.8 falling to 0.6 |
| Running | 0.18 and up | 0.3 |

The gain scales leg offsets only, never the body pose.

### Smoothing, hold and fade `[v]`

1. Move each free axis toward the fit by the confidence.
2. No support at all: hold the pose 0.75 s, then decay it with a 3 s time constant.
3. Slew limits: pitch and roll 300°/s ÷ size factor. Lift clamp(6 ÷ size factor, 4, 6) blocks/s plus the body's own vertical move. Rise is 2 × the fall limit.
4. Exponential smoothing is nearly instant at a 0.05 s step (lift 0.01 s up, 0.02 s down; pitch and roll 0.02 s), so the slew limits do the real smoothing `[inf]`.
5. Last pass: if smoothed legs still fall short, lift the body by at most max(0.25, 12 × dt) per step.
6. Fade to flat with exp(−dt ÷ 0.35) when ridden, on two synced flags (one is the carcass flag; the other is probably lying down `[inf]`), in a pose clip, or in fluid and off the ground.

### Ground sampler `[v]`

- An unloaded chunk (not FULL) counts as no support.
- The scan starts at y + step up, or at the chunk's `WORLD_SURFACE` height when that is lower (air skip). It stops below y − step down.
- Per block: the collision shape top at the exact fractional x, z. Slab 0.5, stairs by facing, carpet 0.0625, farmland 0.9375, snow by layer (1 layer has no collision), fence post 1.5. A gap between posts falls through. Water falls to its bed.
- An empty block: check the block below; a top of 1.0 or more there counts as top − 1.
- A top above the start is skipped (a tall post, a ceiling). A result below the floor is no support.
- Footprint: 5 points per foot, the centre and 4 corners at 0.9 of the half size, turned by yaw and scaled. Points in one column merge. The highest counts.

### Cache and re-solve `[v]`

- 20 slots per animal (4 feet × 5 points). A slot hits when the column, x and z (within 0.05) and y (within 0.05) match and it has not expired. A full-cube column from the heightmap shortcut hits on the column alone.
- Expiry: 10 ticks × a jitter of 0.8 to 1.0 from the entity id. No block-update invalidation: a broken block shows within about 0.5 s.
- Solve again only on the first tick, a move over max(4, 2 × leg reach) sideways or 8 up, a scale change over 5 %, or a dirty last pass. Skip when position is within 1e-4, yaw within 0.01° and the pose settled within 5e-4. dt = min((skipped ticks + 1) × 0.05, 0.1) s.
- The cost log reports, every 5 s: microseconds per creature frame for sample, solve and write; probes and reads per frame; cache hits split by full column, sub-block, shared and sampled.

---

## 2. Wading and remembered spots (F2.1, F2.4)

Nublar sets its own wade share (D382, 0.55 of body height, long necks deeper). These are the parts the plan leaves to the task plan.

### Wading `[v]`

- Depth = water surface − top of the solid collision under it. A full source block is about 0.89 deep.
- JC3's own rule (for reference): wade at depth up to 0.4, or 0 when eye height is under 0.4; or in flowing, not falling water with air above and depth under min(1, eye height).
- Path retry costs: WATER 1, WATER_BORDER 0 for the one retry, then restore. Use the retry result only if it reaches.
- While wading, vanilla travel runs with the touching-water flag cleared: land speed, no drag, no current push. Nublar adds a small slowdown (D383).
- Climb-out goal: scan 12 wide × 4 tall for a dry walkable cell with at most 6 path tests. Swim straight at speed 1.2. Within 0.6 blocks sideways, with room above, kick up at 0.3 or more. Fall back to the path. End after 600 ticks. Runs every tick, only in deep water.
- Thirst under 0.2 while in water: every 40 ticks the thirst refills to full and the drink clip plays.

### Remembered spots and treks `[v]`

- Saved keys `WaterAt` and `FoodAt` (X, Y, Z), written on each meal and drink. JC3 keeps one spot per need; Nublar keeps 1 to 3 by intelligence (D386).
- Trek hop when no path: a random hop in a 20 × 7 box, scored by negative squared distance to the target, then a wade-aware hop.
- Foraging drift score for a wander candidate: squared distance to the nearest of the last 16 failed search origins of each flagged need (capped at 1,000,000), plus squared distance from the animal. The highest wins. Wander runs every 20 ticks in a 20 × 7 box while a flagged need is low.
- Arrival at a target block that is not walkable (a plant) counts at an edge distance of 0.85 or less. An animal on top of its target block has not arrived.
- Drink spots: a dry cell with a floor top 0.5 to 1 high (slabs and stairs count) and water beside it or one below. A cliff-top ledge also counts: the next cell and the one below it are open and the water is two below. Pick by lowest squared distance plus a normalised turn angle.

---

## 3. Path fixes (F2.5 part A)

Nublar takes these as D387 and D388.

- Start cell for a body wider than 1 cell: floor(x − half footprint + 0.5) on x and z. Use it if the body fits; else the nearest of the 3 × 3 same-height cells; else the vanilla start `[v]`.
- Target jitter for wander and herd targets: −half footprint + a random 0 to 1 on x and z `[v]`.
- Node nudge (Nublar: test item only): after vanilla path following, if the next node did not advance, the animal is within 0.25 blocks sideways and under 1 block vertically, advance by hand. Final node: 0.35 sideways, 1.0 vertically `[v]`.
- Hazard escape: same-level DAMAGING or POWDER_SNOW nodes at cost at least 8, only when the search starts in that type and the body box is clear `[v]`.
- Stuck rung: a bit mask walks each of the 8 directions once; equal runs are picked by reservoir sampling; a lane is skipped if any footprint path type has a negative cost (open cells use the type below, so powder snow and trapdoors count) `[v]`.

---

## 4. Tree climbing and gliding (no Nublar species yet)

JC3 gives this to the Microraptor only. It works for any species with the data block. A Nublar species needs climb and glide clips first.

### Species data `[v]`

`abilities.tree_climb`: `search_radius` 7 (1 to 64), `maximum_height` 14 (1 to 128), `climb_speed` 0.3 (0.01 to 2.0), `interval` 1,200 ticks (1 to 24,000), `glide_range` 22 (1 to 128), `glide_descent_rate` 0.05 (0 to 1).

### Climb goal `[v]` (`dnui/fnutlq.java`)

- Starts only when the animal is on the ground, has no attack target, and is not a carcass. A second synced flag also blocks it, probably lying down `[inf]`. After a failed try it waits 40 ticks; after a climb it waits `interval`. A debug command can force one climb, which then takes the nearest tree.
- **Find a tree:** 20 random columns within `search_radius` of the animal. In each, go 1 below the surface (the heightmap top `[inf]`) and scan down up to 6 blocks for a log, then down to the lowest log (the trunk base). Count logs up from the base, at most `maximum_height`; at least 2 needed. Then test the 4 sides of the base: the side cell and the cell above must be open (a level helper, probably "passable" `[inf]`), and the side column up to the trunk height must hold only air or leaves. The first good side is the climb spot.
- **Approach:** path to the climb spot. Start the climb within 2 blocks of the trunk base. A path that ends short drops the tree. Each phase has a 200-tick limit.
- **Climb:** play `start_climbing`, then `climbing`. Each tick the animal is placed at the centre of the climb spot column, `climb_speed` higher, with zero velocity and fall distance reset. The top is the first block above the base that is not a log or leaves, at most `maximum_height` up. It climbs beside the trunk, through leaves, with no collision test.
- **At the top, pick a landing spot:** 40 random points within `glide_range`, at the animal's height, dropped to the surface. A point must be at least half of `glide_range` away and pass a level helper on the block under it (probably "solid to stand on" `[inf]`). A point on leaves (another canopy) wins at once; else the first good ground point. No point found: the goal ends at the top.
- **Launch:** sideways 2 % of the offset, up 0.2. Play `gliding`.
- **Glide:** horizontal speed = min(move speed × 0.5, horizontal distance × 0.25). Descent = clamp(drop needed × speed ÷ distance, `glide_descent_rate`, 0.3). While the launch hop still rises, keep it; then descend at that rate. Face the travel direction, pitch from descent over speed. Fall distance resets each tick. It ends on the ground, in water, within 1.5 blocks of the spot, or after 200 ticks.
- **Stop:** set the wait, level the pitch, stop the path, stop the climb or glide clip.
- It never climbs down. With no landing spot it stays at the top, and the soft-fall goal below brings it down `[inf]`.

### Soft fall `[v]` (`dnui/yfasw.java`)

- For any species with `tree_climb`: when not on the ground, not in water, not ridden, not a carcass or lying down, not in a leap, and fallen more than 4 blocks, it caps the fall speed at `glide_descent_rate` (0.05 per tick), resets fall distance and plays `gliding`. A cheap way to give a light animal no fall damage.

### Test from the GameTests `[v]`

A Microraptor with wings out glides from 12 blocks, and a wingless Velociraptor dropped at the same height lands long before it. The test logs the tick the wings open, the glide length, and the mean and worst descent per tick.

---

## 5. Flight model (Release 2 Pteranodon)

Nublar keeps only a pointer (D392). These are the values behind it.

### States and timers `[v]`

- States: GROUNDED, TAKING_OFF, FLYING, GLIDING (a visual sub-state of FLYING), SWOOPING, LANDING, FALLING.
- Ground and air timers each roll once between the species minimum and maximum (Ludodactylus ground 400 to 1,200 ticks, air 300 to 1,800). The maximum forces the change.
- Landing reasons: an order, max air time, sleep, thirst or hunger after at least min(100, min air) ticks, a schedule, a mate, a meal. LANDING starts only within 10 blocks of the spot. A 35-tick lock follows take-off.

### Take-off `[v]`

- Checks: alive, awake, growth at least 0.5, no roof overhead, enough room (4 rays of 32 blocks plus a roof scan), not busy. About 9 blocks of clear radius and a 4-block gap for Ludodactylus `[inf]`.
- The 34-tick take-off clip launches at tick 9: up 0.5, forward 0.3. Ground contact more than 5 ticks after launch fails the take-off.

### Movement `[v]`

- A kinematic model: no path navigator, no node evaluator. Each tick the goal writes one wanted point and a profile.
- Turn: g = 0.0245 blocks per tick², yaw rate = g × tan(bank) ÷ speed. Bank changes at most `roll_rate` (4) per tick, up to `max_bank` 35 (`hunt_bank` 50).
- Pitch is clamped by `climb_angle` 15, `takeoff_climb_angle` 25, `descent_angle` 12 and `approach_angle` 10, with a pitch rate limit. Speed changes by `acceleration` 0.01 per tick; `cruise_speed` 0.45, `swoop_speed` 0.8, `min_speed_factor` 0.6.
- Profiles: cruise, hunt, take-off, climb-out, landing, flare, dive, avoid, steep.
- Loop-out: when the height change cannot fit the angle, the bird circles until it can.

### Terrain and waypoints `[v]`

- Floor = ground + 3. Ceiling = roof − 1.5. Altitude band `min_altitude` 8 to `max_altitude` 24.
- A forward ray every 5 ticks, offset by entity id, starts a 10-tick avoid mode with a climb target 64 up.
- Blocked and nearly stopped for 40 ticks = wedged: forced to the ground.
- Waypoints 20 to 40 blocks ahead, within ±60°, inside `home_radius` 48 of the roost.

### Hunting and fishing `[v]`

- Strike windows are glide slopes. Land prey at 0.466 to 1.0 of the horizontal distance, fish at 0.364 to 0.577. `hunt_radius` 20, `swoop_launch` 0.6.
- Fish rules: water category, at the surface, health at most 0.9 × own.
- Shore fishing is a ground goal: a 52-tick clip with the strike at tick 24.

### Landing `[v]`

- 4 landing-spot samples every 10 ticks.
- A ring of 8 failed spots, 2-tile tolerance, 2,400-tick expiry.
- An approach scan: 7 distances on 8 headings.
- Phases: line up, join, descend, final, flare.

### Save and sync `[v]`

- NBT: `FlightState`, `FlightGroundTicks`, `FlightAirTicks`, `DiedInAir`, `Roost`.
- One synced byte: the state. The client works out bank and pitch from position history and blends the flap, hover and steering clips.
- Every ground one-shot clip has an `airborne_` twin.

### Clips Ludodactylus ships `[v]`

`takeoff`, `airborne_flapping`, `airborne_hovering`, `airborne_idle`, `airborne_swooping`, `airborne_landing`, `airborne_falling`, `airborne_steering_left`, `airborne_steering_right`, `airborne_death`, plus airborne attack, call, eat, injured, look and speak clips, and `fishing`. A Nublar Pteranodon needs at least idle, walk, takeoff, flap, soar, land, dive, fall and air death (D392). Steering clips are optional if bank is procedural.

### Not verified

The stand-off constants 8.19 and 10.63, and the counter behind the soft fall's "more than 4".

---

## Sources

- `jurassicraft-neoforge-26.1.2-3.1.0.jar` and `jurassicraft-neoforge-26.1.2-3.0.14-hotfix.jar` in the user's Downloads folder, decompiled with Vineflower 1.12.0 for this reading.
- Species data: `data/jurassicraft/jurassicraft/species/*.json` in the 3.1.0 jar (`ludodactylus.json`, `microraptor.json`, `tyrannosaurus.json` and others).
- Config comments: the 3.1.0 config class (`cjhzvuor.java`), keys `footGrounding` and `creaturesBreakLeaves`.
- The earlier study: [ai_and_behaviour.md](ai_and_behaviour.md), [performance_and_rendering.md](performance_and_rendering.md), and [JurassiCraft 3 survey for Beta](../../reports/JurassiCraft%203%20survey%20for%20Beta.md).
