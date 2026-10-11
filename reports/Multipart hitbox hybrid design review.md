# Review: the hybrid design works, after six additions

> **Superseded on 2026-10-10** by [Dinosaur hitboxes and movement design.md](Dinosaur%20hitboxes%20and%20movement%20design.md). That page holds the current design and the lead's decisions. This page stays as history. Some of its claims are wrong or replaced. The corrections log in the new page lists them.

> **Scope:** an adversarial review of [Multipart hitbox hybrid design.md](Multipart%20hitbox%20hybrid%20design.md) as of 2026-10-09, with the lead's Decisions as the binding text. Read only. No file in the worktree was changed, no build ran, no game file was opened. Facts come from the report, the research notes, the worktree code, and public sources. Each claim is marked **[F]** (fact from a cited source) or **[I]** (inference from facts).
>
> **Short names:** `REPORT` = the design page. `NOTE/x` = `research_notes/Multipart hitbox hybrid design/x.md`. `DRAGON` = `research_notes/Ender dragon part placement/placement_26_2.md`. `CHAOS` = `research_notes/CHAOS branch IK audit/chaos_ik_audit.md`. `SOC/` = `common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/`. `V1201` and `V262` = third-party decompiled-source mirrors of 1.20.1 and 26.2 on GitHub, pinned as in the notes. `NF262` = NeoForge branch `26.2.x` at `f40fb568`.

## Verdict

The design solves the ask for one herd of 20 Gallimimus, with the fixes below. The torso box, the arrival fix, the sweep, the turn gate, the capsule spacing and the single file cover every playtest failure. The section parts cover the visible body, and vanilla 26.2 routes hits to them safely. Two things stop it from working as written. First, every collision query in the level scans every part, so a server with 100 dinosaurs loses most of its tick to that scan (C1). Second, the baked poses have no server-side animation clock, so the server cannot know which pose to look up (C2). Both have cheap resolutions that keep the decided design. Six High findings need sequencing or one more piece each.

## Findings

Ranked by severity. Costs are per server tick unless the row says otherwise. 20 ticks = 1 s.

| ID | Severity | Area | In-game effect | Issue | Evidence | Resolution |
|---|---|---|---|---|---|---|
| C1 | Critical | Performance | The server slows as dinosaurs are added. Every mob, item and arrow in the level pays, not only the herds. | `noCollision` runs an entity query. On NeoForge and vanilla 26.2 that query loops over every loaded part in the level. Cost is parts × queries. | [F] `noCollision` calls `getEntityCollisions`, which calls `getEntities` ([V1201 CollisionGetter L47-62](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/level/CollisionGetter.java), [V1201 EntityGetter](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/level/EntityGetter.java), [V262 CollisionGetter](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/level/CollisionGetter.java)). [F] NF262 loops all parts on every call ([Level.java.patch](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/level/Level.java.patch#L133-L187)). [F] `BodyFit.boxClear` calls `level.noCollision` ([SOC/BodyFit.java:277-282](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/BodyFit.java)). | Bucket parts by chunk section on both loaders. Make every herd and IK block test read blocks directly, never through `noCollision`. See the detail section. |
| C2 | Critical | Baked poses | A Gallimimus calls. The player shoots the raised head. The arrow passes through, because the server head part still sits at the walk pose. | The server has no animation clock. The client picks animations in `DinoAnimator.clientTick`. The server runs only `DinoOneShots`. A baked table needs the animation name and its tick on the server. Gallimimus idle and rest also use `query.life_time`, which differs per side. | [F] `Dinosaur.tick` ([Dinosaur.java:669-673](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java)). [F] `gallimimus.animation.json` uses `math.cos(query.life_time * 100+90)*3` in `misc.idle`, `rest`, `restidle` (grep of the file). [F] REPORT "Action poses" decision. | Add a synced pose state: animation id, start tick, loop flag. The server sets it. Both sides look up the bake from it. Treat `life_time` terms as rest pose. See the detail section. |
| H1 | High | Determinism | A Brachiosaurus tail bends round a trunk. The server bends it left, the client right. Arrows at the visible tail miss by up to 20 blocks. | The IK bend runs on both sides from slightly different inputs (lerped position, body yaw, loaded chunks). A near-symmetric case flips the bend sign per side. | [F] client position is lerped (DRAGON B4, C1). [F] REPORT "Neck and tail IK" decision: deterministic code on both sides. [I] sign flip. | Server picks the bend side per chain and syncs one byte. The client reproduces the magnitude only. Skip the bend when the part's chunk is not loaded on that side. |
| H2 | High | Herd spacing | Twenty Gallimimus in single file stretch 140 blocks. The last ones leave the trail and the follow range. The leader waits every few steps. | Capsule spacing with a 1.0 gap gives a 7.1-block pitch in line. The trail holds 128 points at 1 block. Stragglers past 48 blocks do not count. The leader waits for 75% within 2R. | [F] REPORT piece 5: "in-line spacing to about 7 blocks". [F] spec: trail 128 points, 48-block rule, 75% rule ([redesign spec L63, L84](../docs/superpowers/specs/2026-10-08-herd-movement-redesign.md)). | Scale the trail length, the follow range and the straggler distance by `N × pitch`. Or measure the in-line gap from snout to tail tip with a 0.5 gap, as a lead decision. See the detail section. |
| H3 | High | Anti-clipping | Nineteen of twenty animals still swing their tails through trunks at corners. Only the leader plans round them. | Followers use context steering, not the path planner. The corner clearance test (piece 4) lives in the planner. Followers get IK only, and IK cannot clear two trunks at once. | [F] `HerdMoveControl.evaluate` probes the clearance box only ([SOC/HerdMoveControl.java:742-828](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdMoveControl.java)). [F] REPORT "Anti-clipping" decision: "both methods". | Add a spine danger term to context steering: for the best 4 headings, test the tail-tip and head cells after the turn. Cost: 8 cell reads per heading. See the detail section. |
| H4 | High | Trees | A Gallimimus walks into leaves and the guard stops it there. The herd is stuck on trees again. | Passable leaves must hold in five code paths, not four. `BodyFit.boxClear` uses vanilla `noCollision`, which counts leaves. The IK block filter and the sweep must skip them too. | [F] `BodyFit` Javadoc: "leaves ... count as they are" ([SOC/BodyFit.java:24-31](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/BodyFit.java)). [F] REPORT Trees table lists collision, node typing, shortcuts and move control. | One per-species block filter, used by: `Entity.collide` (mixin), `BodyFit`, the node evaluator, the sweep, and the IK. Build it once, before piece 1. |
| H5 | High | Growth | A Brachiosaurus grows one stage. Its box jumps from 2 wide to 2.5 wide inside a fence line. Its parts keep adult size while the model is a hatchling. | Growth is four steps in code, not smooth. Parts built in the constructor read `getGrowthStage()` before NBT or synced data arrive, so they get stage 4. Vanilla halves the box for `isBaby()`, so a stage scale on top double-scales. A torso box over 4 blocks skips vanilla's fix-up. | [F] `getGrowthStage` returns 4 when no flag is set ([Dinosaur.java:981-992](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java)). [F] renderers use 0.25/0.5/0.75/1 (NOTE/project_nublar_fit). [F] REPORT piece 1 growth additions. | Refresh parts after NBT load, after synced data, and on each stage change. Override `getDimensions` fully. Set `noPhysics` on parts. On 26.2 use the `scale` attribute as the one scale source. Ask the lead about smooth growth (Q1). |
| H6 | High | Bake step | Animators change a tail keyframe. The game shows the new tail. Arrows still hit the old place until someone rebuilds. On a server with a data pack override, server and client disagree. | The bake must reimplement Bedrock keyframe sampling, Molang `math.sin(query.anim_time …)` terms and bone forward kinematics. `data/` files are server data packs. The client does not get data packs. | [F] all species except Dilophosaurus use Molang in keyframes (grep). [F] REPORT "Baking" decision: Gradle writes into `data/`. [I] sync risk. | Put the bake under `assets/` or a non-reloadable resource in the jar. Hash it and compare on join. Add a golden test against recorded bone positions. See the detail section. |
| M1 | Medium | Performance | Capsule spacing costs 15 times the report's figure, about 0.2 to 0.5 ms per herd. | The masks call the pair test once per heading per member, 12 × 19 per evaluation, 10 evaluations per tick. That is 2,280 pair tests, not 190. | [F] `HerdBodies.crowder` loops all members per heading ([SOC/HerdBodies.java:173-193](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdBodies.java), `evaluate` L766). [F] REPORT: "190 pair tests per herd tick". | Add a circle broad phase per evaluation: one pass finds members within reach sum plus 10 ticks of travel. Run capsule tests only on those. |
| M2 | Medium | Performance | Under a canopy, 20 animals bending necks cost 1 ms. With `noCollision` they cost 10 ms. | Each IK bend step tests several boxes against blocks. Through `noCollision` each test also scans parts (C1). An unloaded chunk read on the server loads it. | [F] `ServerLevel.getBlockState` loads chunks. [F] `BodyFit.surfaceStandY` guards with `isPositionEntityTicking` ([SOC/BodyFit.java:116-124](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/BodyFit.java)). | IK reads block collision shapes directly on the cells each part covers, through one cached chunk-section view. Skip parts in non-ticking chunks. Cap at 6 bend steps per chain per tick. |
| M3 | Medium | Area damage | A player sweeps at a Brachiosaurus tail 11 blocks behind its torso. Nothing happens. A splash potion on the tail does nothing. | Sweeps and potions find the parent through the typed hook, then test distance to the parent's centre: 3 blocks for sweeps, 4 for potions. The Decision's in-game effect only holds within those ranges. The hook also needs dedupe when torso and part both intersect. | [F] NF262 typed loop uses `tryCast`, so a part fails `LivingEntity` ([Level.java.patch L144-187](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/level/Level.java.patch#L144-L187)). [I] vanilla distance tests. | Keep the hook. Restate the effect: "reaches the dinosaur when the torso is within the effect's own range". Dedupe with an identity set. Ask the lead if that is enough (Q6). |
| M4 | Medium | Riding | A player rides a Gallimimus and clicks. The neck part is in front of the camera. The rider hits its own mount. | Parts are pickable entities. The vehicle check compares against the parent only. | [F] 26.2 attack path: `getEntityOrPart` then `player.attack(target)` (NOTE/sources_hit_detection Q6). [I] passenger case. | Part `hurtServer` ignores a source whose entity is a passenger of the parent. Client picking skips parts of the camera entity's vehicle. |
| M5 | Medium | NoAI | A posed exhibit dinosaur with `NoAI` cannot be hit. For one tick after spawn or chunk load, its parts sit at world 0, 0, 0. | The dragon places parts inside the AI tick only. The recipe copies the dragon and does not say where placement runs. | [F] MC-252478, DRAGON A6 and C2. | Place parts at the end of `tick()` regardless of AI, in the constructor, in `recreateFromPacket`, and after NBT load. |
| M6 | Medium | GeckoLib 5 | The visible tail sways twice, or steps 20 times a second while the model is smooth. | The client must move bones to follow the bent spine. Keyframes already sway the tail. An absolute bone write fights the controller. A per-tick delta with no partial tick repeats the CHAOS jitter. | [F] `RenderPassInfo.create` registers `applyAnimationControllers` as the first updater ([GL262 RenderPassInfo](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/renderer/base/RenderPassInfo.java)). [F] CHAOS jitter cause 1 and 4. | Updater adds only the delta between the bent and the unbent spine, lerped by partial tick, on top of the snapshot. Copy the delta into the render state in `addRenderData`. |
| M7 | Medium | Report text | Implementers read two rules for teleport, two rules for the torso, and two spine lengths. | The Decisions and the body text conflict in seven places. | See the detail section. | Edit the body text to match the Decisions. Regenerate the spine table from the posed rest model. |
| M8 | Medium | Client cost | With 20 Gallimimus on screen the frame rate drops further. It is already 26 fps from colour layers. | Leg IK per frame: 10 ground raycasts and a 20-iteration solve per animal. The CHAOS code allocates an `Arrow` per raycast. | [F] handoff: colour layers cost 80% of the frame. [F] CHAOS section 7. | Run leg IK once per tick, lerp per frame. Full IK within 16 blocks, none past 32. Pass `CollisionContext.empty()`, never an entity. |
| M9 | Medium | Large species | A Brachiosaurus plans paths 5 ms each. It cannot enter any forest. The lead may read that as stuck. | A torso box about 3.4 × 9.5 gives a 4 × 10 × 4 node volume, 160 cell reads per node. Every trunk stops the box. Leaves break but logs hold. | [F] model top 19.1, torso 9.05 long (REPORT data values). [I] torso height from the geo file. [F] vanilla node volume `floor(w+1)` × `floor(h+1)` (NOTE/sources_movement Q1). | State the expectation in the design: sauropods path in open ground only. Cap large-species paths at 1 per herd per tick. |
| M10 | Medium | Tests | Nobody can run the part tests on Fabric. Nobody sees server parts in F3+B. | GameTests live in the Forge module. The report has no test plan. The 26.2 debug renderer shows the client entity, not the server one. | [F] handoff L47. [F] DRAGON B4 debug aid. | Add the test list in the detail section. Register GameTests in common with a shim per loader. |
| L1 | Low | Projectiles | A tipped arrow on the tail deals damage but no effect. A flaming arrow on the neck does not ignite the dinosaur. Punch gives no knockback. | `AbstractArrow.onHitEntity` applies effects only to a `LivingEntity`. A part is not one. | [I] from vanilla arrow code, unverified on 26.2. | Forward fire ticks in the part. Accept the potion and Punch loss, or add one mixin on `onHitEntity`. |
| L2 | Low | Mixins | Another multipart mod on Fabric adds its parts twice, or the `ENTITY_COUNTER` accessor clashes. | The Fabric set adds `RETURN` injects on `getEntities`, a `ChunkMap.addEntity` cancel and a counter accessor. The NeoForge typed hook injects into patched code. | [F] NOTE/sources_hit_detection Q3. | Inject only this mod's parts. Pin NeoForge `26.2.x` and re-test the typed hook per NeoForge release. |
| L3 | Low | Save and load | A dinosaur saved with its tail bent round a trunk loads with a straight tail, then bends over 10 ticks. | IK state and the yaw buffer are not saved. | [F] DRAGON B2: the dragon saves no history. | Accept. Run one full placement before the first render. |
| L4 | Low | Part layout | Leg parts stick out 0.11 blocks past the 0.8 movement box on each side. | Legs are 0.6 wide at x = ±0.21. | [F] mock-up `P` rows 11 and 12. | Accept, or narrow legs to 0.5. Parts do not collide. |

## C1. Every collision query scans every part

**In game:** with 100 dinosaurs loaded, the server tick rises by 20 to 40 ms. Zombies, items and arrows far from any herd pay the same. The client pays too, for every entity it ticks.

**Mechanics [F]:**

1. `CollisionGetter.noCollision(entity, aabb)` tests block shapes, then calls `getEntityCollisions`, then the world border. The 26.2 mirror keeps that order ([V262 CollisionGetter](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/level/CollisionGetter.java)).
2. `getEntityCollisions` calls `getEntities(entity, aabb, predicate)` unless the box is smaller than 1e-7 ([V1201 EntityGetter](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/level/EntityGetter.java)).
3. On NeoForge 26.2, that untyped `getEntities` loops every element of `dragonParts()` on every call. Per part it tests `except`, the parent, the selector, then the box ([NF262 Level.java.patch](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/level/Level.java.patch#L133-L143)). Vanilla 26.2 does the same for dragon parts (NOTE/sources_hit_detection Q2).
4. `Entity.move` calls `collide`, which calls `getEntityCollisions`. Every moving entity pays once per tick.
5. `BodyFit.boxClear` calls `level.noCollision` on the `ServerLevel` ([SOC/BodyFit.java:277-282](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/BodyFit.java)). The guard, the steering probes, the side-room measures and `PathProbe.fitsAt` all end there.

**Scale [I]:** with 12 parts per animal, 100 dinosaurs give 1,200 parts. At about 15 ns per part, one query costs 18 µs. A level with 700 moving entities, 1,250 herd probes and 50 typed queries runs 2,000 queries per tick. That is 36 ms. With 20 dinosaurs (240 parts) the same arithmetic gives about 2 ms. The vanilla pathfinder does not pay: `PathNavigationRegion.getEntityCollisions` returns an empty list.

**Resolution (keeps 12 parts):**

1. **Spatial buckets.** Keep a per-level map from chunk-section key to the parts inside it. Update it in the placement function when a part crosses a section. Replace the linear loop with a lookup of the sections the query box covers. On Fabric this is the mod's own `getEntities` inject. On NeoForge, redirect the `dragonParts()` call inside the three `Level` methods to the bucketed view. Cost per query falls to the parts near the box, usually under 24.
2. **No `noCollision` in herd code.** `BodyFit`, the sweep and the IK read block collision shapes directly, cell by cell. One chunk-section cache per herd per tick serves them. Entity collisions matter to the guard only for boats and shulkers. Drop them.
3. **Part count by distance.** Keep 12 parts for animals within 64 blocks of a player. Collapse to 4 (head, torso, torso, tail) past that. Parts must keep their objects and IDs, so only sizes and positions change.

Ask the lead about item 1 (Q2). It is a mixin into NeoForge-patched code.

## C2. The server has no animation clock for the baked poses

**In game:** the Decision says head and neck parts follow the real animation. Today the server never knows which animation plays. A `calling` Gallimimus raises its head on every client. The server part stays at the walk pose. Arrows at the visible head miss. Melee works, because the client sends the part ID and the reach buffer is 3 blocks.

**Mechanics:**

- [F] `Dinosaur.tick` runs `animator.clientTick()` on the client and `oneShots.serverTick()` on the server ([Dinosaur.java:669-673](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java)). The `movement` controller picks walk or run on the client from ground speed.
- [F] GeckoLib blends controllers over `MOVEMENT_TRANSITION_TICKS` and `MAIN_TRANSITION_TICKS` ([Dinosaur.java:164-171](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java)). A bake keyed by animation and tick does not know the blend.
- [F] Gallimimus `misc.idle`, `rest` and `restidle` drive `tail1` to `tail3` and `neck1` with `math.cos(query.life_time * 100+90)*3`. `life_time` is the entity's own tick count on each side. The client count starts at 0 when the entity is added to the client level. That is not a synced value.
- [F] The Alex's Caves pattern keys targets on a synced animation ID and tick (NOTE/sources_hit_detection Q1). The dragon syncs one phase int (DRAGON B4).

**Resolution (keeps the bake as default):**

1. Add a synced pose state on `Dinosaur`: animation id (byte), start game tick (int), loop flag. The server owns it. `DinoAnimator` on the client reads it, so the GeckoLib controllers and the bake lookup share one source.
2. The walk and run cycle phase comes from distance walked, which both sides see within one lerp step. Bake the cycle by phase, not by tick.
3. Bake `anim_time` Molang terms at 1-tick resolution. Treat `life_time` terms as 0, which is the rest pose. The error is 3° on the Gallimimus idle tail, under 0.2 blocks.
4. Bake the transition as a linear blend over the controller's transition ticks, from the previous pose state. Store the previous state on both sides.
5. Measure the remaining error with the debug overlay the report already plans.

## H1. The IK bend can differ between server and client

**In game:** a Brachiosaurus walks past a trunk. Its 11.7-block tail must bend. The server bends it left, the client bends it right. The visible tail and the server tail are up to 20 blocks apart. Every arrow at the tail misses. Melee fails the reach test.

**Why [I]:** the bend search takes the first clear direction within joint limits. Near a symmetric case, a 0.1-block input difference flips the choice. The inputs differ per side: the client position is lerped over 3 steps, the client `yBodyRot` is computed locally, and chunk edges can differ. Each side alone is stable. The pair is not.

**Resolution:** the server chooses the bend side per chain (neck, tail) and syncs it as one byte with hysteresis. The client runs the same magnitude search with the sign fixed. When a part's chunk is not loaded on a side, that side skips the bend and keeps the last pose. Then the two sides differ by the lerp error only, under 0.5 blocks for Gallimimus and under 2 blocks for Brachiosaurus.

## H2. Capsule spacing makes the column longer than the herd machinery

**In game:** in a corridor, 20 Gallimimus walk nose to tail with a 1-block gap. The column is 140 blocks long. The trail holds 128 blocks, so the last four animals have no trail point. Animals past 48 blocks do not count as stragglers. The leader stops every 10 s for the 75% rule and the herd never arrives.

**Arithmetic [F]:** Gallimimus length 6.13 plus a 1.0 gap gives a 7.1-block pitch. Twenty animals in one lane give 19 × 7.1 = 135 blocks. In three lanes the column is 45 blocks, which still exceeds the 35-block follow range of one walk.

**Resolution (pick one, Q3):**

1. Scale the trail (points), the straggler distance and the wait rule by `N × pitch`. Trail 256 points for 20 Gallimimus. Straggler distance `N × pitch / 2`.
2. Measure the in-line gap from the follower's snout to the leader's tail tip, and allow a 0.5 gap. The pitch falls to 6.6. The column is still 125 blocks.
3. Accept in-line overlap of the neck over the tail ahead by up to 2 blocks. Animals do this in life. The pitch falls to about 4.6 and the column to 87 blocks.

Option 3 keeps the capsule for side-by-side spacing and only relaxes the in-line term. It needs the asymmetric spine the report already asks for.

## H3. Anti-clipping planning covers the leader only

**In game:** the leader turns a corner where its whole body fits. Nineteen followers steer behind it with a 12-heading probe of the torso box. Each follower cuts the corner. Each tail sweeps the trunk. The IK bends one tail round one trunk, but not round two.

**Why [F]:** `HerdMoveControl.evaluate` probes the clearance box at 1, 2 and 3 blocks ahead and nothing else ([SOC/HerdMoveControl.java:742-828](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdMoveControl.java)). Piece 4 lives in the planner. Followers path only as a fallback.

**Resolution:** add one danger term to context steering. For the previous heading and the 4 deep-probed headings, compute the head cell and the tail-tip cell after a turn to that heading. Read the two cells. A solid cell adds `DANGER_BLOCKED_FAR`. That is 10 cell reads per evaluation. The follower then prefers headings where its ends stay clear, and the IK handles the rest.

## H4. Passable leaves must hold in five code paths

**In game:** a Gallimimus walks into leaves because collision lets it. The guard tests the box with vanilla `noCollision`, sees leaves, sets speed 0. The animal stands in the canopy. The herd is stuck on trees, as in playtest 2.

**Why [F]:** the report's Trees table lists collision, node typing, shortcuts and move control. `BodyFit` reads blocks through vanilla queries and says so ([SOC/BodyFit.java:24-31](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/BodyFit.java)). The IK block filter is a fifth path. For a leaf-breaking species with `mobGriefing` off, the same pass-through applies.

**Resolution:** one per-species `BlockFilter` with three answers per block state: pass, break, solid. Every block reader asks it:

1. `Entity.collide` through one mixin on both loaders.
2. `BodyFit` fit, stand, ground and side-room tests.
3. The node evaluator's block typing.
4. The sweep shortcut.
5. The IK bend test for neck and tail.

Build the filter before piece 1 and test all five with one GameTest under a 3-high canopy.

## H5. Growth has four edge cases the design does not cover

**In game:** a Brachiosaurus hits stage 3 inside a 2-wide fence lane. Its box grows and clips the fence. A loaded hatchling shows adult-size parts for one tick. A baby box is a quarter of the model, not half.

**Facts:**

- [F] Growth is four stages, from synced flags set one tick after `age` crosses a threshold ([Dinosaur.java:684-703, 981-992](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java)). The lead's ask says "grow smoothly". The report's spine table uses the four factors.
- [F] `getGrowthStage()` returns 4 when no flag is set. Parts built in the constructor read it before NBT load.
- [F] Vanilla moves a growing entity out of blocks only up to 4.0 blocks (REPORT piece 1). A Brachiosaurus torso box is taller.
- [I] Vanilla 1.20.1 halves a baby's dimensions. A stage factor on top gives 0.125.
- [I] `refreshDimensions` on a growing part calls `move`, which collides unless `noPhysics` is set.

**Resolution:**

1. Override `getDimensions(pose)` fully from the species torso size × the shared scale. Never let vanilla baby scaling apply.
2. On 26.2 drive the shared scale through the `scale` attribute. Vanilla then scales the box, the eye height and the attachments. Check that GeckoLib 5 does not scale the model a second time.
3. Refresh parts after `readAdditionalSaveData`, in `onSyncedDataUpdated` for the stage flags, and on each stage change. Keep the part objects.
4. Set `noPhysics = true` on parts.
5. Apply the Fossils growth guard to the parent at each stage change.
6. Ask the lead whether growth becomes smooth on 26.2 (Q1). If yes, refresh the box only when the scale moves by 0.05.

## H6. The bake step is a second animation engine that can drift from the assets

**In game:** an animator moves the neck in `calling`. The model shows it at once. The server head part keeps the old position until the next build. On a server where an operator overrides the data pack, the client and the server disagree.

**Facts:**

- [F] Every species except Dilophosaurus uses Molang in keyframes, for example `math.sin(query.anim_time*180/0.5)*-5` (grep of the animation files). The bake must evaluate these.
- [F] Bone pivots and parent rotations chain nine deep on the Gallimimus neck (geo file). The bake needs forward kinematics with the Bedrock pivot rule.
- [F] The Decision writes the tables into `data/`. Data packs load on the server. A dedicated server does not send them to clients.
- [F] The mock-up and the report's spine table disagree on the Gallimimus snout reach. The posed model gives 2.2 blocks, the raw pivots 3.18 (mock-up `C` rows, REPORT data values). That shows the posed bake and the raw pivots differ.

**Resolution:**

1. Store the bake as a plain resource in the jar under `assets/`, loaded by a shared loader on both sides. Do not make it a reloadable data pack.
2. Write a hash of the bake into the mod's network version check, so a mismatched client cannot join.
3. Add a golden test. Record bone world positions once from GeckoLib 5 `addBonePositionListener` in a dev run and store them as a fixture. Compare the bake against the fixture within 0.1 blocks.
4. Fail the build when an animation file is newer than its bake.

## M7. Text that conflicts with the Decisions

| Place | Says | Decision says | Fix |
|---|---|---|---|
| Summary item 6, stuck ladder rung 5, build order step 5 | No teleport or snap near players | Snap allowed also when a player is near | Edit the three places |
| Piece 6 "Parent box or torso part" | Keep the parent pickable, torso part optional | Two torso parts, parent not pickable | Remove the paragraph |
| "Server truth of the animated pose" recommendation | Procedural spine only, per-animation targets | Baked data is the default | Mark as superseded |
| Trees section | "The hybrid has no tree-specific measure" | Leaf and log rules decided | Mark as superseded |
| Data values spine table | Gallimimus 3.18 ahead | Sizes from the posed, render-scaled model (mock-up: 2.2) | Regenerate from the posed model |
| Piece 5 | "190 pair tests per herd tick" | n/a | 2,280 with today's masks (M1) |
| Data values | Growth scales by four factors | Lead's ask: smooth growth | Resolve with Q1 |
| Open decisions list | 13 open items | Decisions answer 12 | Collapse to item 1 |

## Performance budget

Per server tick, estimated. **Assumptions [I]:** 15 ns per part in a linear scan. 30 ns per direct block-state read. 3 µs per small `noCollision` without the part scan. Load A: 20 Gallimimus in one herd, 1 player, 300 other entities. Load B: 100 dinosaurs in five herds, 10 of them large species, 3 players, 600 other entities. None of these numbers is a measurement. Profile each row after it ships.

| Subsystem | Load A (20) | Load B (100) | Notes and assumptions |
|---|---|---|---|
| Part placement | 25 µs | 125 µs | 12 parts, `setPos` allocates one AABB each. 240 and 1,200 allocations per tick. |
| Baked pose lookups | 5 µs | 25 µs | Array index per section per tick. Needs C2. |
| Server neck and tail IK, direct cell reads | 0.15 ms | 0.8 ms | 8 parts, 1 µs when clear, 20 µs in a bend, 30% of animals bending. |
| Server IK through `noCollision` (do not do this) | 0.8 ms | 12 ms | Adds one part scan per test (C1). |
| Level-wide part scans, linear | 2 ms | 36 ms | 570 and 2,000 queries per tick. The Critical row. |
| Level-wide part scans, bucketed | 0.2 ms | 0.6 ms | Under 24 parts per query. |
| Typed-query parent hook | 30 µs | 0.4 ms | 15 and 35 typed queries per tick (sensors every 20 ticks, combat). Same loop as native. |
| Capsule pair tests, today's masks | 0.2 to 0.5 ms | 1 to 2.5 ms | 2,280 tests per herd, 60 ns each, 3 samples for the 10-tick prediction. |
| Capsule pair tests, with broad phase | 40 µs | 0.2 ms | About 60 capsule tests per evaluation. |
| Corner clearance, sampled capsule | 60 µs | 0.5 to 3 ms | 3 sharp nodes per path, 6 samples × 3 headings (Gallimimus), 18 × 3 (Brachiosaurus). 2 and 10 paths per tick. |
| Destination clearance | 0.1 ms | 0.5 ms | One cached `standY` per slot change. |
| Box-sweep shortcut, every tick | 30 µs | 0.75 ms | 1 × 3 cells × 10 blocks (Gallimimus), 4 × 9 × 10 (Brachiosaurus), 3 attempts. |
| Stuck recovery, detector | 2 µs | 10 µs | One compare per animal. |
| Stuck recovery, repaths | up to 4 ms spike | up to 20 ms spike | Vanilla A*: 1 to 2 ms (Gallimimus), 5 ms (Brachiosaurus, 160 reads per node). Capped by the 2-per-herd budget. |
| Leaf breaking | 0 | 0.6 ms | Gallimimus pass. 10 large animals, 128-cell scan while colliding, 5 breaks per tick at 100 µs. |
| Networking | 0 B | 0 B | Parts never sent. Pose state and bend side: 6 bytes per animal on change. |
| Herd AI today (spec budget) | under 1 ms | about 5 ms | Spec table. Unchanged by this design. |
| **Total server, linear scans** | **4 to 8 ms** | **45 to 70 ms** | Over the 50 ms tick at load B. |
| **Total server, with C1, M1, M2 fixed** | **2 to 3 ms** | **9 to 15 ms** | Spikes from A* on top. |
| Client leg IK, per tick with lerp | 1.1 ms | 5.5 ms if all visible | 2 legs, 5 raycasts at 5 µs, 20-iteration solve. LOD cuts load B to about 1 ms. |
| Client part scans | same as server rows | same | `ClientLevel` ticks every entity and keeps its own part list. |
| Client bone updaters | 20 µs per frame | 100 µs per frame | Delta per bone, trivial. |

## Optimizations

Each keeps the decided design.

1. **Bucket parts by chunk section** on both loaders (C1). Largest win.
2. **Direct block reads in herd and IK code.** One `LevelChunkSection` cache per herd per tick, keyed by section. No `noCollision`, no `getBlockState` through the level for cells in the same section.
3. **IK early out.** Skip the bend search when the 8 part cells and their neighbours are air. Most ticks in open ground cost 8 reads.
4. **IK cadence by distance.** Every tick within 32 blocks of a player. Every 4 ticks past that, with the last pose held. Hits past 32 blocks are rare.
5. **Capsule broad phase** (M1). Circle test first, capsule only on the few that pass.
6. **Part count LOD.** 12 parts near players, 4 far away, same objects and IDs.
7. **Sweep shortcut every 5 ticks**, or when the next node changes. Cache the result per node index.
8. **Corner test only on heading changes over 45°**, and only where the spine reach exceeds 2 blocks. Gallimimus stages 1 and 2 skip it.
9. **Baked tables as flat `float[]`** per animation, indexed by tick and section. No maps, no boxing.
10. **Yaw history as `float[]`**, not records (DRAGON C2 item 3).
11. **Culling box once per tick** in the placement function. `getBoundingBoxForCulling` returns the cached union.
12. **Leaf breaking gated on `horizontalCollision`**, as Fossils does, so the cell scan runs only on contact.
13. **Client leg IK once per tick**, lerped per frame, with `CollisionContext.empty()` (M8).
14. **Spread part refresh on growth** over the herd: one animal per tick at a stage change.

## Questions for the lead

1. **Growth.** Does growth become smooth on 26.2, or stay four stages? Do we drive it through the `scale` attribute? (H5)
2. **Part scan fix.** Do you accept a mixin into NeoForge's patched `Level.getEntities` for spatial buckets? The alternative is part-count LOD only, which still leaves 480 parts for 100 near animals. (C1)
3. **Column length.** Which in-line rule: scaled trail and straggler constants, snout-to-tail-tip gap, or allowed neck-over-tail overlap? (H2)
4. **Bend side sync.** Do you accept one synced byte per chain so both sides bend the same way? (H1)
5. **Riders.** Should a rider's attacks and right-clicks ignore the mount's parts? (M4)
6. **Area damage range.** Sweeps and potions reach the dinosaur only when the torso is within their own range. Is that enough, or do you want a custom range test? (M3)
7. **Sauropods in forests.** Do you accept that a Brachiosaurus never enters a forest, and that playtests measure it in open ground only? (M9)
