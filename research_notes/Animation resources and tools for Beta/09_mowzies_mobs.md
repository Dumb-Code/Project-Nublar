# Item 9: Mowzie's Mobs, code-driven animation (clean-room study)

Date: 2026-11-03

## Scope

- Study how Mowzie's Mobs drives bones from code on GeckoLib.
- Write each mechanism as a short spec in words and numbered steps (S-M1 to S-M9).
- Map each spec to our layers 2 to 7 of the F2.3 plan.
- Rate per-frame cost and allocation.
- Clean-room rule: no code is copied or closely paraphrased. Numbers such as constants are facts, not code.

## Labels

- `[v]` checked in source at a named commit, with path and lines.
- `[doc]` from a README, changelog, store page or release note (with link).
- `[inf]` my inference. Not run, not profiled.

## Bottom line

- Mowzie's Mobs `main` runs on GeckoLib 4.8.3 (MC 1.21.1). The `1.20` branch uses a CurseForge GeckoLib file (exact version not checked). No GeckoLib 5 or MC 26.x branch exists. [v]
- It has no IK. It has a leg-height solver with a fixed-step settle. [v]
- Its tail and beard physics rebuild the bones from world-space positions. This needs GeckoLib 4 bone mutation and a matrix override. It does not port to GeckoLib 5 as written. [inf]
- Three ideas are worth taking: clip-driven weights ("controller bones"), a stiffness fall-off and a pose attraction for chain springs, and locator bones for effects.
- The code allocates heavily every frame. We must not copy that shape.

## 1. Source and version

- The brief gave `https://github.com/BobMowzie/MowziesMobs`. That URL returns HTTP 404. [v] (`gh api repos/BobMowzie/MowziesMobs`)
- The public copy is `https://github.com/BobMowzie/MowziesMobs-Public`. Its description is "A public copy of the Mowzie's Mobs source code." [doc]
- Branches: `main` and `1.20` only. [v] (`gh api .../branches`)
- `main` = commit `7aa17309337cbd4102efc418cba12a4983b1540c`, 2026-04-22, "1.8.2 update". [v]
  - MC 1.21.1, NeoForge 21.1.162, GeckoLib 4.8.3. `gradle.properties` lines 17, 20, 26. [v]
  - GeckoLib dependency: `build.gradle` line 61. [v]
- `1.20` = commit `64d15248c4421fdf8555165134b306e8cfc0a6c6`, 2026-04-23. [v]
  - Forge 1.20.1-47.2.32 (`build.gradle` line 109) and a CurseForge-maven GeckoLib file (line 110). [v]
  - The same chain classes exist there: `GeckoDynamicChain`, `MowzieGeoBone`, `LegArticulator`. [v] (tree of that commit)
- Modrinth lists Mowzie's Mobs 1.8.2 for 1.20.1 (Forge, 2026-04-02) and 1.21.1 (NeoForge, 2026-03-15). No 26.x build. [doc] https://modrinth.com/mod/mowzies-mobs
- The unofficial Fabric port `ZanahoriaRoja/MowziesMobs-Refabricated` is MC 1.21.1 with GeckoLib 4.8.3. [v] (`gradle.properties`)
- A GitHub search found no GeckoLib 5 or MC 26 port. [inf] (search is not exhaustive)

Path shorthand in this note:

- `MM` = `src/main/java/com/bobmowzie/mowziesmobs`
- `CH` = `MM/client/model/tools/dynamics`
- `GL` = `MM/client/model/tools/geckolib`
- All `[v]` facts below are at `7aa1730` (`main`) unless stated.

## 2. How Mowzie writes bones on GeckoLib

- Hook A: override `GeoModel#setCustomAnimations`. It runs after the clips. The code mutates `GeoBone` objects with `addRotX`, `setPosY` and similar calls. [v] `MM/client/model/entity/ModelElokosa.java:40-41, 111-115`; `GL/MowzieGeoBone.java:108-165`
- Hook B: override `GeoModel#applyMolangQueries` to reset every registered bone to its rest snapshot before animating. [v] `GL/MowzieGeoModel.java:40-44`
- Hook C: override `GeoEntityRenderer#actuallyRender` and `#renderRecursively` for the chains. [v] `MM/client/render/entity/MowzieGeoEntityRenderer.java:62-95, 98-182`
- Hook D: subclass `AnimationController` and override its protected `adjustTick`. [v] `GL/MowzieAnimationController.java:12-49`
- All four hooks use GeckoLib 4 internals. GeckoLib 5 has no in-place bone mutation. It has per-pass `BoneSnapshot`s and bone updaters. [inf] See `01b_geckolib_upstream.md`.

## 3. Specs

### S-M1: Controller bones (clip-driven weights)

What it is: a dummy bone whose position X is a number between 0 and 1. The clip keys it. Code reads it and scales a code effect.

Steps:

1. Add a bone named for the effect (for example `tailPhysicsBlendController`) in the model.
2. Key its X position in each clip. 0 means code effect off. 1 means on.
3. After the clips run, read the bone's X position.
4. Multiply the code effect by that number.
5. Skip the effect when the number is 0 (Mowzie does not skip; see S-M2).

Facts:

- Reader: one method returns posX, one returns 1 minus posX. They return 0 or 1 when the model or bone is missing. [v] `GL/MowzieGeoModel.java:46-58`
- Uses:
  - Form switch, wall cling, locomotion weight, walk/run switch, tail-physics blend. [v] `ModelElokosa.java:47, 122, 150-151, 169-170`
  - Torso aim versus head aim. [v] `ModelSculptor.java:57-61`
  - Head look gate and arm pitch gate on the player model. [v] `ModelGeckoBiped.java:137-138`
- The tail chain's blend is "1 minus controller". So the clip decides how much of the physics shows. [v] `ModelElokosa.java:169-170`; `CH/GeckoDynamicChain.java:283-285`
- Nothing in code writes these bones. So the clip is the only source. [inf]

Cost: one bone lookup and one float read per effect per frame. Allocation: one `Optional` per lookup in GeckoLib 4. [inf]

### S-M2: Procedural locomotion on top of the idle clip

What it is: the Elokosa walk and run are sine waves written per bone in Java. Clips only blend them in and out (S-M1).

Steps:

1. Each tick, rotate the velocity into the body frame. Take its dot product with the four unit axes (forward, back, left, right). Clamp at 0. Do this only if speed is above a small limit. [v] `MM/server/entity/MowzieGeckoEntity.java:80-97`
2. At render, lerp each of the four weights by partial tick. Double the limb swing amount and cap it at 0.7. [v] `ModelElokosa.java:144-149`
3. Read the locomotion weight and the walk/run weight from controller bones. [v] `ModelElokosa.java:150-152`
4. Call seven pose functions every frame: four walk directions, run, and two day-form versions. Each gets its weight as the first argument. [v] `ModelElokosa.java:153-160`
5. Inside each function, add offsets to about 20 bones. Each offset is a cosine of (limb swing times speed plus a phase) times a size times the limb swing amount times the weight. [v] `ModelElokosa.java:176-262`
6. Use a "powered wave" for foot-down dwell: remap cosine to 0..1, then raise it to a power. [v] `GL/MowzieGeoModel.java:60-62`; use at `ModelElokosa.java:292`
7. Tail bones get the same wave with a phase that shifts by 0.5 rad per bone, so the wave travels down the tail. [v] `ModelElokosa.java:243-252`

Facts:

- The seven functions take lines 176 to 925 of `ModelElokosa.java` (about 750 lines for one animal). [v]
- The functions run even when the weight is 0. [v] `ModelElokosa.java:153-160` (no guard). [inf] That wastes work.

Cost: about 150 bone mutations per frame per animal, plus about 150 bone lookups. [inf, counted from the code]

### S-M3: Head look

What it is: add the entity's head yaw and pitch to a head bone. Variants split or gate the amount.

Steps (common):

1. Read `netHeadYaw` and `headPitch` from the GeckoLib 4 model data. [v] `ModelElokosa.java:97, 108-110`
2. Convert to radians.
3. Add to the head bone rotation. [v] `ModelElokosa.java:111-116`

Variants:

- Even split between neck and head: half each. [v] `ModelUmvuthana.java:62-72`
- Blend between head and chest by a controller value. [v] `ModelSculptor.java:57-61`
- Player model: a controller bone gates the look amount. A second pair gates arm pitch. [v] `ModelGeckoBiped.java:137-157`
- Legacy models: `faceTarget` divides yaw and pitch by (divisor times bone count) and adds the share to each bone. [v] `src/main/java/com/ilexiconn/llibrary/client/model/tools/AdvancedModelBase.java:56-65`; use `ModelNaga.java:712`
- One model clamps yaw to plus or minus 30 degrees. [v] `ModelGrottol.java:291-293`

Gaps: no per-joint bend budget. No smoothing. No target in world space. [v] (no such code in the files above)

Cost: a few float adds per frame. No allocation beyond bone lookups. [inf]

### S-M4: `GeckoDynamicChain` (world-space Verlet chain on GeckoLib bones)

What it is: a rope simulation in world space. It follows the animated pose and lags behind it. Used for the Elokosa tail (6 bones) and the Sculptor beard (4 bones). [v] `ModelElokosa.java:164`; `ModelSculptor.java:86`

Setup (once, or when the bone count changes or a value is NaN):

1. Record each chain bone's world position. [v] `CH/GeckoDynamicChain.java:185-210`
2. Set every node mass to 1. Set each rest segment length to the distance between neighbours in the animated pose. [v] `GeckoDynamicChain.java:210-219`
3. Mark the original bones as "dynamic joints". Create one copy bone per chain bone. The copy keeps the original's cubes and its non-chain children. [v] `GeckoDynamicChain.java:222-229`; `GL/MowzieGeoBone.java:33-47`
4. Turn on matrix tracking for the originals, so their world matrices exist after drawing. [v] `GeckoDynamicChain.java:172-178`

Each render frame:

1. Hide the original chain bones. Draw the whole model. The bones are still tracked but not drawn. [v] `MowzieGeoEntityRenderer.java:62-73`
2. Read the animated world position of every chain bone. Recompute segment lengths. [v] `GeckoDynamicChain.java:244-254`
3. If the game is not paused and game time changed, run the sim with the elapsed time in ticks. [v] `GeckoDynamicChain.java:238, 257-259`
4. In the sim, repeat the sub-step N times (default 10). [v] `GeckoDynamicChain.java:46, 124-125`
   1. Slide node 0 in a straight line toward the new animated root. [v] line 126
   2. If more than one tick passed since the last update, snap every other node to the animated pose and clear acceleration. This stops popping after culling. [v] lines 127-133
   3. Otherwise, build a force: constant gravity (default 0.1) plus, if enabled, a pull to the animated node. The pull is divided by (1 plus index squared times a fall-off). [v] lines 138-144
   4. Take one Verlet step. Velocity is (position minus previous position) times (1 minus damping). Add acceleration times the sub-step time squared. [v] line 147
   5. Pull the node back to the exact rest distance from the node before it. One pass, root to tip. [v] lines 149-154
5. For each node, blend the render position: lerp(alpha, animated, simulated). Alpha comes from a controller bone (S-M1). [v] lines 283-285
6. Build each copy bone's rotation as the shortest-arc turn from a fixed "chain axis" (rotated by the entity yaw) to the direction toward the next node. The last node gets no rotation. [v] lines 289-310
7. Copy the animated bone's scale. [v] lines 312-315
8. Write the matrix to the copy bone. [v] line 319
9. Draw the copies. The renderer replaces the parent chain with the copy's world matrix. [v] `MowzieGeoEntityRenderer.java:80-87, 101-119`

Config used by Elokosa: attraction on, strength 2.0, fall-off 0.25, damping 0.04, axis (0, 0, 1). [v] `MM/server/entity/elokosa/EntityElokosa.java:228-233`

Config for the Sculptor beard: defaults. The sim pauses (`setSimulating`) while a pillar rises or falls, and the nodes then follow the animated pose. [v] `EntitySculptor.java:199-202, 469`; `GeckoDynamicChain.java:161-168`

Facts that matter:

- The sim steps every render frame, not every tick. [v] `GeckoDynamicChain.java:233-259`
- Damping is a fixed fraction per sub-step. It is not scaled by time. [v] line 147
  - So the damping per second grows with frame rate. At 60 fps and 10 sub-steps, the velocity left after one frame is about 0.66 (0.96 to the 10th, for damping 0.04). [inf, arithmetic from line 147]
  - Results will differ between 30 fps and 144 fps. [inf]
- `floorCollision` and `stiffness` are set by the API but never read in this class. There is no ground collision in the GeckoLib chain. [v] `GeckoDynamicChain.java:42, 47, 95-104` (only fields and setters)
- The chain needs this frame's animated positions. It reads them after drawing the first pass. [v] `MowzieGeoEntityRenderer.java:73-79`

Allocation (my count from the code, not profiled):

- One `Vec3` per arithmetic step. About 19 per node per sub-step. [inf] lines 136-154
- A 6-bone tail with 10 sub-steps gives about 1,000 short-lived objects per frame. [inf]
- Matrix, quaternion and vector objects add about 12 per node per frame. [inf] lines 280-315

### S-M5: `DynamicChain` (older chain: joint-angle spring, tick-stepped)

What it is: a chain simulated in joint angles with a spring per joint. Used by the Naga tail on a non-GeckoLib model. [v] `MM/client/model/entity/ModelNaga.java:1234`

Steps:

1. Once per entity tick (guard on tick count), read the animated world positions of the chain bones. [v] `CH/DynamicChain.java:181-185`
2. Repeat N sub-steps (Naga uses 30). For each joint: [v] `DynamicChain.java:48-95`; `ModelNaga.java:1234`
   1. Compute the target direction angles from the animated positions. Compute the current angles from the simulated positions.
   2. Torque = minus stiffness divided by (index plus 1) to the power of a fall-off, times the wrapped angle difference. [v] line 73
   3. Add a gravity torque that scales with node mass, segment length and the sine of the pitch. [v] lines 74-78
   4. Optionally add an upward torque when the entity is on the ground and the node is below its feet. [v] lines 79-82
   5. Angular acceleration = torque divided by (mass times length squared). Add to angular velocity (divided by sub-steps), then multiply by damping. [v] lines 84-86
   6. Add velocity to the angle. Wrap angles to plus or minus pi. [v] lines 87-88
   7. Rebuild the next node position from the angle and the segment length. [v] line 90
3. At render, place each node at position plus velocity times partial tick plus half acceleration times partial tick squared. [v] line 195
4. Turn node positions into bone yaw and pitch. [v] lines 196-206

Naga settings: gravity 0.5, stiffness 0.5, fall-off 0.5, damping 0.97, 30 sub-steps, floor on. [v] `ModelNaga.java:1234`. Mass per node is 0.5 plus 0.5 divided by (index plus 1). [v] `DynamicChain.java:152`

Facts:

- Tick-stepped, so it does not depend on frame rate. [v] `DynamicChain.java:181-189`
- Allocates many `Vec3` per joint per sub-step (every helper returns a new vector). 30 sub-steps make this large. [inf] lines 48-95, 332-351
- The `EntityDynamicsTester` entity uses the same class as a debug rig. [v] `MM/server/entity/EntityDynamicsTester.java:43-45`

### S-M6: Leg height solver and articulator (not IK)

What it is: a per-leg height value that settles toward the ground under that leg. A second class turns the heights into thigh and calf offsets.

Steps (solver, each tick, each leg):

1. Take a probe point: entity position plus the leg's side offset and forward offset, turned by the body yaw. [v] `MM/server/entity/LegSolver.java:21-31, 51-54`
2. Read the support-shape top of the block at the point. Convert it to a distance from the feet. If the column is empty, look one block lower. [v] lines 56-63, 72-81
3. If the entity is on the ground and the leg height is at or below the target, raise the height by at most 0.3 per tick toward it. [v] line 64-65
4. Otherwise, if the height is above 0, lower it by at most 0.4 per tick, not below the target. [v] lines 66-67
5. At render, lerp the previous and current height by partial tick. [v] lines 47-49

Steps (articulator, at render):

1. Biped: raise the body by the average leg height. [v] `MM/client/model/tools/LegArticulator.java:12-21`
2. For each leg pair: raise each thigh by max(own height, average). Rotate thigh back and calf forward by a fixed amount times the height gap to the other leg. [v] lines 54-63
3. Quadruped: tilt the body by the angle from the back average to the front average over the body length. Counter-rotate the front thighs and the neck. [v] lines 24-52

Facts:

- Only the Frostmaw uses it, and the Frostmaw is not a GeckoLib model. [v] `MM/server/entity/frostmaw/EntityFrostmaw.java:113, 118, 289`; `MM/client/model/entity/ModelFrostmaw.java:13, 537`
- A search of `src/main/java` for IK, inverse kinematics, two-bone, FABRIK and CCD found nothing. [v]
- No foot planting. Feet follow the clip. Only the height offsets change.

Cost: 2 to 4 block-state reads per animal per tick. Each read builds a `BlockPos` and a bounds box. [inf]

### S-M7: Bone read-back for secondary parts (Sculptor skirt and beads)

What it is: read bone positions after drawing. Use them next frame to bend cloth bones.

Steps:

1. After the first draw in a new game-time step, read model-space positions of thigh, calf and skirt-end bones. Store them on the entity. [v] `MM/client/render/entity/RenderSculptor.java:50-80`; `MowzieGeoEntityRenderer.java:48-55`
2. Next frame, in `setCustomAnimations`, compute unit directions thigh-to-knee and thigh-to-skirt-end. [v] `ModelSculptor.java:120-169`
3. Raise the dot product with "down" to the power 3. This is the leg-forward amount. [v] lines 170-173
4. Build the shortest-arc quaternion between the two directions and use it as a matrix override on a skirt bone. [v] `ModelSculptor.java:179-184`; `MM/client/model/tools/RigUtils.java:257-263`
5. Move, rotate and scale other cloth bones from the same numbers. [v] `ModelSculptor.java:185-212`

Facts:

- The data is one frame old. [inf]
- `beadsCorrections` builds a `HashMap` every frame. [v] `ModelSculptor.java:92-93`

### S-M8: Locator bones for effects

What it is: empty bones whose world position the code reads once per game-time step to spawn particles.

Steps:

1. Model has bones named like `droolPos1`. [v] `MM/client/render/entity/RenderElokosa.java:44-49`
2. After drawing, if game time changed since the last update, run `renderUpdates`. [v] `MowzieGeoEntityRenderer.java:48-55`
3. Read the bone's world position. Store it on the entity. [v] `RenderElokosa.java:46-49`
4. Entity code spawns particles at the stored positions.

Our plan already does this with `RenderPassInfo#addBonePositionListener`. This confirms the approach.

### S-M9: Animation timing tweaks

- `MowzieAnimationController#playAnimation` starts the clip at once even if the mob is off-screen. It resets the tick offset and sets transition length to 0. [v] `GL/MowzieAnimationController.java:23-30`
- `adjustTick` adds a fixed `timingOffset` to the clip time and wraps loops by modulo. The comment on line 48 says the author does not know why it works. [v] lines 32-49
- It is built on GeckoLib 4 protected fields (`tickOffset`, `shouldResetTick`). [v] lines 34-44

### S-M10: Unused or minor code

- `RigUtils.BlendShape3D`: blend several stored pose offsets by the dot product of a look direction with each pose's direction. Defined, never called. [v] `MM/client/model/tools/RigUtils.java:85-191`; no caller found by grep of `src/main/java`
- `LLibrary ChainBuffer`: copy of a tick-based tail-lag helper. Not used in this repo. [v] `src/main/java/com/ilexiconn/llibrary/client/model/tools/ChainBuffer.java`; no other file references it. See `10_other_finds.md`.
- `SmartBodyHelper`: body yaw follows the movement direction over a 10-sample position history. [v] `MM/server/entity/SmartBodyHelper.java:12-38`

## 4. Map to our layers

| Our layer | Mowzie spec | Verdict | Why |
|---|---|---|---|
| 1. Clips and additive overlay | S-M1 controller bones | Use the idea | Lets each clip switch layers 2 to 7 on or off without per-clip code. |
| 1. Clips | S-M9 timing tweaks | Keep for reference | GeckoLib 4 internals. We should not need them. |
| 2. Leg IK (client) | S-M6 leg solver and articulator | Keep for reference | We already have a two-bone solver. Take only the rate limits (0.3 up, 0.4 down per tick) and the body drop by average foot height. Check whether the CHAOS foot planner has both. |
| 3. Neck and tail bend away from blocks | none | Nothing to take | No block avoidance found in the model code. [inf] (I searched for ray casts and block reads in `client/model` only.) |
| 4. Head look chain with budgets | S-M3 head look | Keep for reference. Use the controller gate. | Our budgets are richer. A clip-driven gate (S-M1) lets attack clips lower the look. |
| 4. Head look (alternative) | S-M10 `BlendShape3D` | Keep for reference | Needs authored look poses per species. Extra art. |
| 5. Neck reach to water or food | none | Nothing to take | No reach code found. |
| 6. Tail lag spring | S-M4 `GeckoDynamicChain` | Keep for reference. Use three ideas. | Ideas: pose attraction that weakens along the chain; snap when stale; blend alpha from the clip. Do not use: world-space nodes, bone copies, matrix override, per-frame sim, per-sub-step damping. |
| 6. Tail lag spring | S-M5 `DynamicChain` | Use the idea | Closest to our design: one angular spring per joint, stiffness falling along the chain, damping, fixed tick step, partial-tick extrapolation. Our spring takes turn rate and speed as input, so it needs no world positions. |
| 7. Breathing sway | S-M2 powered wave and phase lag | Keep for reference | Phase shift per bone and the powered wave are cheap and give a travelling wave. |
| Effects at bone positions | S-M8 locator bones | Use (confirms plan) | Same pattern as `addBonePositionListener`. |
| Locomotion by code | S-M2 | Drop | Our six species use keyed clips. 750 lines per animal is a poor trade. |
| Cloth read-back | S-M7 | Drop | No cloth in our six species. |

### Why S-M4 does not port directly

- It reads this frame's animated world positions after drawing. In GeckoLib 5 the bone positions come from listeners during the draw. A bone updater runs before the draw. So a world-space sim in an updater is one frame late, or needs a second pass. [inf] (see `01b_geckolib_upstream.md`, `RenderPassInfo` and `RenderUtil`)
- It draws copies of bones with a world matrix. GeckoLib 5 has no such override in the places I read. [inf]
- An angle-space spring (our layer 6) needs no positions. It has no latency and no copies.

## 5. Per-frame cost and allocation, summary

| Item | Runs | Work | Allocation (my count) |
|---|---|---|---|
| `GeckoDynamicChain` | every render frame per chain | 10 sub-steps times nodes | about 1,000 objects per 6-bone tail per frame |
| `DynamicChain` | every tick, extrapolated at render | 30 sub-steps times joints | tens of `Vec3` per joint per sub-step |
| Elokosa walk and run | every render frame | 7 functions, about 150 bone writes | one `Optional` per bone lookup |
| Head look | every render frame | 2 to 4 adds | none beyond lookups |
| `LegSolver` | every tick per leg | block reads | `BlockPos` and bounds box per read |
| Sculptor cloth | every render frame | about 40 vector ops | `HashMap` and many `Vec3` |

- None of the Mowzie code has a distance cut-off or a low-detail path. [inf] I searched the chain, renderer and model files only.
- The only skips are: paused game, and no time change since the last update. [v] `GeckoDynamicChain.java:257`; `MowzieGeoEntityRenderer.java:51`
- For low and mid PCs: copy none of the allocation pattern. Use arrays of floats, preallocated per animal. [inf]

## 6. Not checked

- Mowzie's animation JSON and the controller-bone keyframes. I read only Java.
- The 1.20 branch line by line. I checked that the same classes exist.
- Frame time. Nothing was run or profiled.

## Verdicts

- S-M1 controller bones: use the idea.
- S-M2 procedural locomotion: drop. Keep the powered wave for layer 7 as reference.
- S-M3 head look: reference. Use the clip-driven gate.
- S-M4 `GeckoDynamicChain`: reference. Take pose attraction with fall-off, stale snap and clip-driven blend. Do not port the structure.
- S-M5 `DynamicChain`: use the idea (angular spring with stiffness fall-off, fixed step).
- S-M6 leg solver: reference.
- S-M7 cloth read-back: drop.
- S-M8 locator bones: use (matches the plan).
- S-M9 timing tweaks: drop.
- S-M10 `BlendShape3D`: reference.
