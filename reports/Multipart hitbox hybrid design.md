# Hybrid design holds for long dinosaurs

> **Superseded on 2026-10-10** by [Dinosaur hitboxes and movement design.md](Dinosaur%20hitboxes%20and%20movement%20design.md). That page holds the current design and the lead's decisions. This page stays as history. Some of its claims are wrong or replaced. The corrections log in the new page lists them.

> **Question from the lead:** Large, long-bodied dinosaurs grow smoothly and walk in herds of up to 20. They cluster and get stuck on trees, walls, gaps and corridors. What reliable design handles their movement and their section hitboxes on Minecraft 26.2, NeoForge and Fabric, with GeckoLib models?
>
> **Answer:** Keep the candidate hybrid, with the fixes on this page. The research found no wholly better design. Four pieces copy shipped precedents. Two pieces are new and need prototypes.

## Summary

**In game, the recommended design gives these results:**

- A dinosaur moves and finds paths with a torso-sized box. At tight corners, its neck and tail can overlap blocks.
- A wide dinosaur stops circling path nodes, because the arrival test uses the same point that the dinosaur aims at.
- In tight ground, a dinosaur turns in place before it walks. In open ground, it slows while it turns.
- Herd mates keep space along their full snout-to-tail length, not only around their centres.
- In corridors, the herd walks in single file.
- Arrows and melee hits land on the visible head, neck and tail sections. The attacker keeps kill credit.

Every studied mod keeps block collision and pathfinding on one vanilla box. Every studied mod puts body length into hit parts or steering. Bedrock Edition makes the same split in its official components ([Microsoft Learn: collision_box](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/entityreference/examples/entitycomponents/minecraftcomponent_collision_box), [custom_hit_test](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/entityreference/examples/entitycomponents/minecraftcomponent_custom_hit_test)). No source gives a mob an oriented movement box that vanilla pathfinding accepts.

The most important fix is not in the candidate list. Vanilla 1.20.1 already aims each path node at the footprint centre. Its arrival test still measures from the block centre. For a dinosaur of whole-number width, the arrival margin is zero. This fits the "spinning on the spot" reports across big-mob mods. That link is an inference from code.

The research supports these additions to the hybrid:

1. A box-sweep shortcut along the path (Mowzie's Mobs, Fossils).
2. An anti-orbit guard and a soft turn gate in open ground (Fossils).
3. A growth guard that keeps the old box when the bigger box would be inside blocks (Fossils).
4. Single-file following in corridors (Alex's Mobs elephants, Alex's Caves Underzealots).
5. Formation slot reassignment and herd-rank priority at bottlenecks (RTS formation research).
6. A stuck detector with an escalation ladder that changes the path. Its last rung snaps the dinosaur to a clear spot.
7. A tree rule: passable leaves, tagged block breaking, or neither.

The biggest risk is the 26.2 port. Nobody has read vanilla 26.2 path-following code from a public source. Fabric has no part API, so section parts on Fabric need a set of mixins.

### At a glance

| Piece | In-game effect | Verdict | Status |
|---|---|---|---|
| 1. Torso-sized main box | The dinosaur fits any gap its torso fits. Neck and tail can overlap blocks. | Keep | Proven |
| 2. Node targets re-centred for box width | The dinosaur advances from node to node without circling | Change: fix the arrival test, not the aim point | Aim point: already vanilla. Arrival fix: proven. |
| 3. Turn in place in tight spaces | The dinosaur stops, turns, then walks | Keep. Add a soft gate in open ground and an anti-orbit guard. | Proven |
| 4. Body-length clearance at sharp corners | The leader prefers routes where the whole body can turn | Keep, as a path cost or a "turn here" flag. Never as a turn veto. | New |
| 5. Snout-to-tail spine or capsule spacing | Herd mates stop overlapping nose to tail | Keep. Make it asymmetric. Add single file and slot reassignment. | New in mods |
| 6. Section hit parts on the same spine | Hits land on the visible neck and tail | Keep | Proven |
| Server truth of the animated pose | Arrows hit a lowered head | Baked poses from the animation files, keyed by a synced pose state. No server GeckoLib posing. | Decided |

### Terms

- **Main box:** the dinosaur's one vanilla axis-aligned bounding box (AABB).
- **Footprint:** the square of block columns that the pathfinder tests for each path node.
- **Aim point:** the point that path following sends to the move control.
- **Arrival test:** the test that decides that the dinosaur reached a path node.
- **Spine:** a per-species line of points from snout tip to tail tip, rotated by body yaw and scaled by growth.
- **Capsule:** a line segment with a radius.
- **Section part:** an unspawned hit box that belongs to the dinosaur.
- **Server truth:** the server knows where a section is during an animation.

## Decisions

> The lead made these decisions on 2026-10-09, after the research on this page. They replace the matching items in [Open decisions for the lead](#open-decisions-for-the-lead).

### Section hitboxes

| Decision | In-game effect | Ruling |
|---|---|---|
| Torso | Every hit lands on a named section | Two torso parts take torso hits. The parent box is not pickable, as on the Ender Dragon. Parts pass right-click interactions to the parent. |
| Section multipliers | A head hit hurts more than a tail hit | Head ×1.5, neck ×1.25, torso ×1.0, legs ×1.0, tail ×0.5 |
| Area damage | A creeper next to the tail hurts the dinosaur | Untyped queries (explosions) find the part. The part forwards the damage. |
| Sweeps and splash potions | A sweep or a potion on the tail reaches the dinosaur | When a typed `LivingEntity` query touches a part, it returns the dinosaur once. The range test measures to the nearest part, not to the torso. Both loaders need a hook for this. |
| Action poses | Head and neck parts follow the real animation | Data baked from the `.animation.json` files is the default. Hand-tuned angles cover only what the build cannot bake, such as a head that turns to look at a target. |
| Walking | Neck and tail parts trail the body and sway with the walk | Yaw history, plus a baked walk and run cycle on top |
| Baking | Animators rebuild to see changes | A Gradle build step writes the baked tables into the mod jar. Server and client load them directly, not through data packs or resource packs. A hash check on join blocks a player whose tables differ (review H6). |
| Pose clock | The server knows which animation plays, so baked parts follow it | The server owns a synced pose state: animation id, start tick and loop flag. Both GeckoLib and the bake lookup read it (review C2). |
| Legs | A sword hit or an arrow on a leg counts | Each leg gets its own hit part, with a ×1.0 multiplier. |
| Tail tip | A hit on the thin end of the tail counts | Two square parts cover the tail tip |
| Part shape | Parts keep their size as the dinosaur turns | Every part has a square footprint, as vanilla `EntityDimensions` allows. Long sections become chains of squares, as on the Ender Dragon and in Alex's Caves. Legs use 0.6 × 0.6 squares. |
| Client refinement | A part can sit slightly off the rendered model cube | No client refinement. Parts stay on the deterministic positions. Approximate placement is acceptable. |
| Arms and frill | Hits on a Velociraptor arm or a spread Dilophosaurus frill pass through | No arm parts and no frill part |
| Part scan cost | The server keeps its tick time with many dinosaurs loaded | Parts sit in buckets by chunk section on both loaders, with a mixin into NeoForge's patched `Level.getEntities`. Dinosaurs far from players use fewer parts with the same objects and IDs. Herd and IK code read block shapes directly, not through `noCollision` (review C1). |
| Growth | Box, parts and model grow together | Growth stays smooth. On 26.2 it drives the vanilla `scale` attribute, the one scale source for box, parts and renderer (review H5). |
| Part and box sizes | Hitboxes match the dinosaur as the player sees it | Take sizes from the render-scaled model, not the raw model file. The torso box width comes from the scaled torso and legs, even when that is narrower than 1 block. Example: in its posed rest model, the adult Gallimimus torso and legs span 0.80 blocks at render scale 1.0, against today's 1.2-block box. |

### Anti-clipping and IK

| Decision | In-game effect | Ruling |
|---|---|---|
| Anti-clipping | Necks and tails do not sink into trunks, walls or stone | Both methods. The planner avoids spots where the body clips. Neck and tail bend away within joint limits where clipping remains. |
| Neck and tail IK | Hit parts and the model agree | Runs on the server tick, with deterministic code. The client runs the same code. |
| Bend side | Server and client bend a neck or tail the same way | The server picks the bend side per chain and syncs one byte. Small remaining differences between the sides are accepted (review H1). |
| Leg IK (foot placement) | Feet sit on uneven ground and steps | Runs on the client only. Visual only. |
| Neck reach IK | — | Not decided. The lead said not to design for animations that do not exist yet. Gallimimus has no `drink` or `eat` animation. |
| CHAOS branch IK code | — | Part of the mod. Use it together with its sources (Mowzie's Mobs, the spider repo, others that apply). Give credit as the Mowzie's Mobs licence requires. The lead handles the licence question. |

The CHAOS audit is in [chaos_ik_audit.md](../research_notes/CHAOS%20branch%20IK%20audit/chaos_ik_audit.md). It found that every CHAOS chain solves on the client, each render frame. So the server-side neck and tail IK needs a new deterministic solve path. GeckoLib 5 on 26.2 also removes the bone matrices and the model factory that the CHAOS render integration uses.

### Movement

| Decision | In-game effect | Ruling |
|---|---|---|
| Clearance box | A Brachiosaurus fits through gaps its torso fits through, but keeps room for its whole body next to herd mates | Split by job. Paths and gaps use the scaled torso width by default. Herd spacing uses the whole scaled model. Each species config can change the choice. |
| Corner test | The leader prefers corners where its body can turn, but still takes a tight corner when no other route exists | Soft cost in the planner, not a hard rule |
| Corridors | Twenty Gallimimus file through a narrow gap one after another | The herd switches from the wedge to single file when the path narrows |
| Last-resort snap | A dinosaur stuck for a long time jumps to a nearby clear spot | Allowed as the last rung of the stuck ladder, also when a player is near |
| Single-file gap | A 20-animal column stays short enough for the herd rules | In single file, the gap runs from the follower's snout to the tail tip ahead, and a neck may overlap the tail ahead (review H2) |
| Brachiosaurus in forests | An adult Brachiosaurus stays in open ground | Accepted. Its 3.68-block movement box keeps it out of forests. Playtests measure it in open ground. |

The mod has no mounts, so rider cases do not apply.

### Trees and leaves

**In game:** leaves never stop a dinosaur. Small species pass through leaves, and large species break them with their torso. Trunks stop calm animals. A charging large dinosaur smashes through trunks.

| Decision | Ruling |
|---|---|
| Leaves | A per-species config value: pass, break or solid. Default: Brachiosaurus, Triceratops and adult T. rex break leaves. Gallimimus, Velociraptor and Dilophosaurus pass through. Tune in playtest. |
| What breaks leaves | Only the main box, and only above a growth stage set per species. Head and neck pass through canopy leaves. |
| Logs | Large species break logs only when charging (running, chasing or stampeding). A per-species config value. |
| Breakable blocks | A mod tag lists them |
| Player-placed leaves | They break too |
| `mobGriefing` off | A leaf-breaking species passes through leaves instead |

### Head contact

Eating and drinking count only when the head touches the food, the water or the feeder. The animal must be able to reach the source.

### Sources

The team reads vanilla Minecraft source through the modding toolchain, as modders do. For 1.20.1 that is the project's own Gradle setup (ModDevGradle and Fabric Loom with Parchment). For the port it is the NeoForge 26.2 and Fabric 26.2 toolchains. Third-party mirrors stay allowed as a labelled secondary source.

### Still open

1. Neck reach IK. It waits until the eating and drinking animations exist.

## Changes to the earlier findings

**In game:** a wide dinosaur reaches a path node but does not advance. It circles the node or spins on the spot.

The earlier report, `Multipart entity hitbox systems.md`, said that vanilla aims each path node at block centre +0.5. It named node centring as the fix. That statement is wrong for 1.20.1.

| | Earlier report | This report |
|---|---|---|
| Vanilla aim point | `node + 0.5` | `node + floor(bbWidth + 1) × 0.5`, in `Path.getEntityPosAtNode` ([V Path.java L76-82](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/level/pathfinder/Path.java#L76-L82)) |
| Real defect | The aim point | The arrival test in `PathNavigation.followThePath` measures from `node + 0.5` |
| What Mowzie's and Fossils `PatchedPath` fix | A new aim formula | Nothing new. `PatchedPath` repeats the vanilla formula. Their real fix is `isAt`, which tests arrival at the aim point. |

**Mechanics.** `followThePath` sets `maxDistanceToWaypoint = bbWidth / 2` for widths above 0.75. It advances only when the dinosaur is within that distance of `node + 0.5` on X and on Z, and within 1 block on Y ([V PathNavigation.java L228-268](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/ai/navigation/PathNavigation.java#L228-L268)).

- The aim point sits `floor(w) / 2` from `node + 0.5`. The arrival margin is therefore `(w − floor(w)) / 2`.
- For w = 2.1, the margin is 0.05 blocks.
- For whole-number widths (2.0, 3.0, 5.0), the margin is zero. A dinosaur on its aim point fails the strict `<` test on the axis across its travel. Only corner cutting can advance the path.
- A smoothly growing dinosaur passes through every whole-number width. A herd holds many widths at once.

**Checked on 2026-10-10** against the project's own toolchain sources. The file is `vanilla-1.20.1-sources.jar`, which ModDevGradle generates with Mojang mappings and Parchment. The relevant code is listed below:

- `PathNavigation.followThePath`, L261-274: tolerance `bbWidth / 2` above 0.75, a strict `<` test against `node + 0.5`, and Y within 1.
- `Path.getEntityPosAtNode`, L82-88: aim point `node + (int)(bbWidth + 1) × 0.5`.
- `NodeEvaluator.prepare`, L27-29: footprint `floor(bbWidth + 1)`.

The mirror agrees. The arrival mismatch is a fact for 1.20.1. On 26.2 it is not checked yet. The link to spinning reports is an inference from code.

## Movement

### Piece 1: torso-sized main box

**In game:** the dinosaur fits through any gap its torso fits. Its neck and tail do not stop it. At tight corners, the neck and tail can overlap trunks and walls.

**Verdict:** keep. This is the field standard.

**Evidence:**

- Alex's Caves sauropods move on 5×8 and 6×8.5 main boxes. Their necks and tails are non-colliding parts. The sauropods are immune to `IN_WALL` damage ([AC SauropodBaseEntity.java L624-626](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L624-L626)).
- A search of the Alex's Caves tracker found no player report about neck or tail clipping ([AlexsCaves issues](https://github.com/AlexModGuy/AlexsCaves/issues)). That absence is weak evidence. Nublar herds in open terrain can draw more attention to clipping. This is unverified.
- A 1.21.1 modder reports that a 3-block box "is too big for the vanilla pathfinding to work well" ([Forge forum 159517](https://forums.minecraftforge.net/topic/159517-bad-pathfinding-for-big-mobs-1211/)).

**Project Nublar today:**

- The Gallimimus main box is already 1.2 × 2.6 ([EntityInit.java](../common/src/main/java/net/dumbcode/projectnublar/init/EntityInit.java)).
- The real oversize box is the herd `ClearanceBox`. Its width is the larger of the hitbox and the model width. For Brachiosaurus and Triceratops it is about 3.5 blocks wide, against a 2 × 3 hitbox ([ClearanceBox.java](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/ClearanceBox.java)).
- For those species, piece 1 is a decision to drop or shrink the model-derived clearance box. It is not an `EntityInit` change. This is an inference.
- A box narrower than 1.0 plans with a one-column footprint. That gives the Gallimimus more slack in 2-wide gaps. This is an inference.

**Growth additions:**

- Vanilla `Entity.refreshDimensions` moves a growing entity out of blocks only when the new width and height are both 4.0 blocks or less ([V Entity.java L2656-2672](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/Entity.java#L2656-L2672)).
- Fossils removes the 4.0 limit. If no free position exists, it keeps the old dimensions ([FA Prehistoric.java L419-449](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/Prehistoric.java#L419-L449)). Its comment calls the search slow for very large mobs. Call it with small growth steps.
- Nublar's `Dinosaur` does not override `getDimensions`. Code reading suggests that juveniles can keep a half-size vanilla baby box until adulthood. Check this in game with F3+B before the spine scales with growth.
- The `ClearanceBox` growth formula differs from the renderer scale for Brachiosaurus (2.75 against 2.6 at gene +10%). Use one shared render-scale function for the box, the spine and the parts.

### Piece 2: arrival test, footprint and shortcuts

**In game:** a wide dinosaur walks from node to node without circling. It does not cut tree corners. On open ground, it walks straight lines, not staircases.

**Verdict:** change the piece. The aim point is already centred. Fix the arrival test, and add a box-sweep shortcut.

**Proven arrival fixes (use one):**

1. Test arrival at the aim point. Mowzie's `isAt` uses tolerance 0.5, or `w/2` at elevation changes ([MM MMPathNavigateGround.java L30-140](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathNavigateGround.java#L30-L140)). Fossils uses 0.5, or 0.75·w at elevation changes ([FA NavUtil.java L144-149](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/NavUtil.java#L144-L149)).
2. Raise the tolerance to 0.75·w. Alex's Mobs `GroundPathNavigatorWide` and Alex's Caves `GroundPathNavigatorNoSpin` do this ([AM GroundPathNavigatorWide.java L23](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/GroundPathNavigatorWide.java#L23), [AC GroundPathNavigatorNoSpin.java](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/GroundPathNavigatorNoSpin.java#L9-L32)). Cataclysm and Naturalist add a "passed the node" dot-product test.
3. Centre the footprint on the node and aim at `node + 0.5`. Citadel does this ([CIT AbstractPathJob.java L458-476](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/pathjobs/AbstractPathJob.java#L458-L476)).

**Box-sweep shortcut.** Mowzie's sweeps the dinosaur's own AABB toward later nodes on the same Y level, farthest first. It takes the first clear node. The sweep rejects water, lava, open floor and damaging blocks. Fossils copies it ([FA PrehistoricPathNavigation.java L48-97](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/PrehistoricPathNavigation.java#L48-L97)). AzureLib's `AzureNavigation` credits Mowzie's for the same idea ([AzureNavigation 101](https://moddedmc.wiki/en/project/azurelib/latest/docs/misc/azurenavigation_101)).

Cataclysm is the warning case. A comment disables its whole sweep body, so the shortcut always jumps to the farthest same-Y node ([CMPathNavigateGround L67-144](https://github.com/lender544/new1.20.1/blob/819937203e22b4fa72fdeccc6c56ad415d65eeef/src/main/java/com/github/L_Ender/cataclysm/entity/etc/path/CMPathNavigateGround.java#L67-L144)). A shortcut with no width sweep lets a wide mob cut corners into walls.

**Footprint size:**

| Footprint rule | Effect | Source |
|---|---|---|
| Vanilla `floor(w + 1)`, anchored at the node toward +X and +Z | Over-sizes whole widths: w = 2.0 needs a 3-wide gap | [V NodeEvaluator.java L27-29](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/level/pathfinder/NodeEvaluator.java#L27-L29) |
| Citadel `2·floor(w/2) + 1`, centred | Under-sizes 1 < w < 2 as 1 block. The planner can send a juvenile into a gap its box cannot enter. | [CIT AbstractPathJob.java L458-476](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/pathjobs/AbstractPathJob.java#L458-L476) |
| Symmetric, ceil-based, centre-anchored | Avoids both errors. Also avoids the +X/+Z asymmetry in [MC-169105](https://bugs.mojang.com/browse/MC-169105) (community report). | Inference, after Harabor's "true clearance" ([Harabor 2009](https://users.cecs.anu.edu.au/~dharabor/data/papers/harabor-aigamedev09.pdf)) |

Harabor shows that "true clearance" (the largest free square at a tile) finds a path for any agent size if one exists. Distance-to-obstacle clearance closes bottlenecks that a 2×2 agent can pass. An on-demand test costs about w² block reads per node and layer. That is an estimate, not a measurement.

**Project Nublar today:** `FenceAwareNavigation` overrides only `createPathFinder` ([FenceAwareNavigation.java](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/FenceAwareNavigation.java)). It has no arrival, shortcut or stuck override. The worktree investigation recorded these values for the Gallimimus:

- tolerance 0.6 from `node + 0.5`,
- aim point `node + 1.0`,
- up to 1.1 blocks of slop off the node line,
- an inner-corner column on 90° turns, "typically the trunk the path is turning around".

Source: [playtest2-geometry-investigation.md](../.superpowers/sdd/2026-10-08-herd-movement-redesign/playtest2-geometry-investigation.md). For w = 1.2, the toolchain sources give the same aim point, tolerance and slop. They also match the corner-cut rules (`canCutCorner`, `shouldTargetNextNodeInDirection`), `MoveControl.tick` turning with `rotlerp(…, 90)`, and the diagonal-step rule (`isDiagonalValid`) (checked 2026-10-10).

**26.2:** nobody verified `followThePath`, `getEntityPosAtNode` or `WalkNodeEvaluator` on 26.x from a public source. On 1.21 and later, `WalkNodeEvaluator` uses `PathType` and `PathfindingContext` ([NeoForge WalkNodeEvaluator.java.patch, 26.2.x](https://github.com/neoforged/NeoForge/blob/26.2.x/patches/net/minecraft/world/level/pathfinder/WalkNodeEvaluator.java.patch)). `FenceAwareNodeEvaluator` therefore needs a rewrite.

### Piece 3: turn in place

**In game:** in a tight space, the dinosaur stops, turns to face its next node, and then walks. In open ground, it walks a curve and slows while it turns.

**Verdict:** keep. Use a hard gate in tight ground and a soft gate in open ground.

| Turn control | In-game effect | Source |
|---|---|---|
| Hard gate | The sauropod walks only when its body yaw is within 15° of the move direction. The body turns 2°/tick (40°/s), so a 90° turn takes about 45 ticks (2.25 s) of standing. | [AC SauropodBaseEntity.java L645-686](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L645-L686) |
| Soft gate | Turn rate is `clamp(90 − 35·w, 5, 90)` degrees per tick. Speed falls to as little as 0.1× in proportion to the remaining turn. | [FA SmoothTurningMoveControl.java L19-75](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/control/SmoothTurningMoveControl.java#L19-L75) |
| Rate cap only | Full speed while turning. At 5°/tick and 0.3 blocks/tick (6 blocks/s), the turn radius is about 3.4 blocks (inference). | Ice and Fire dragons, Mowzie's Frostmaw |
| Attack-goal gate | Yaw changes at most 2°/tick. Thrust starts only under 4° of error. | [Cataclysm Leviathan L1906-1925](https://github.com/lender544/new1.20.1/blob/819937203e22b4fa72fdeccc6c56ad415d65eeef/src/main/java/com/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/The_Leviathan/The_Leviathan_Entity.java#L1906-L1925) |

**Project Nublar today:**

- `Dinosaur.setYRot` clamps every yaw change to 10°/tick (200°/s) ([Dinosaur.java](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java)).
- Vanilla `MoveControl` sets forward speed in the same tick that it turns.
- The `HerdMoveControl` guard sets speed 0 when the box 0.6 blocks ahead along the yaw does not fit. That is a hard stop, not a planned turn ([HerdMoveControl.java](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdMoveControl.java)).
- At journey speed, a 90° turn covers 0.77 blocks of arc that no probe tested. The guard fires on that arc at trunks.

**Fixes the research supports:**

1. Gate forward speed in `HerdMoveControl.tick`, after `super.tick()`, when `tightGround` is true and the yaw error is large. Use the soft gate elsewhere. This split is an inference.
2. Copy the Fossils anti-orbit guard: skip turning when the horizontal distance² to the target is 0.12 or less (about 0.35 blocks). Its comment says this "prevents spinning if mob overshoots".
3. Make arrival generous, or count a node as reached when the dinosaur is within tolerance and faces the next node. A turning dinosaur that overshoots otherwise turns again, and the spin loop starts.
4. Add the box-sweep shortcut first. It removes most staircase micro-turns, so the hard gate fires mainly at real corners.
5. Do not count turning ticks as stuck ticks.
6. Never cancel rotation in `setYRot`. CustomHitboxLib does this, and its FAQ warns that it confuses the pathfinder ([CHL EntityRotationMixin.java](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/src/main/java/dev/customhitboxlib/mixin/EntityRotationMixin.java#L30-L133), [CHL FAQ](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/wiki/FAQ.md)).

**Limit:** turning in place removes the forward chord, not the tail sweep. An adult Gallimimus turning about its centre sweeps a radius of about 2.95 blocks. An adult Brachiosaurus sweeps about 11.7 blocks with its tail. Piece 4 must test that sweep.

### Piece 4: body-length clearance at sharp corners

**In game:** the herd leader prefers routes where its full length can turn at each corner. Where no such route exists, the leader still goes, and its neck or tail overlaps blocks.

**Verdict:** keep, as a soft rule in the planner. This piece is new. No studied mod tests body length against terrain during pathfinding.

**Design (inference):**

- Find path nodes where the heading changes more than a threshold.
- At each such node, test a disc of radius `max(nose reach, tail reach)` around the pivot. As an alternative, test a capsule swept over the turn.
- Run the test as a few sampled AABB `noCollision` queries along the spine, not as an exact oriented test.
- Use the result as a path cost or as a "turn in place here" flag. Do not use it as a veto in `MoveControl` or `setYRot`.
- If no route passes, accept the clipping. Alex's Caves does this with non-colliding parts and `IN_WALL` immunity.
- For sauropod lengths only, try a small local search in a 16 × 16 block window with 8 headings. A three-point reverse turn is the alternative.

The cost is O(r²) column reads per sharp node. Few nodes are sharp, so this is cheap next to A*. This is an estimate.

**Why not full heading search:** Hybrid A* searches (x, y, heading). It took 50 to 300 ms per replan on a 2008 PC with 72 headings ([Dolgov et al. 2008](https://cdn.aaai.org/Workshops/2008/WS-08-10/WS08-10-006.pdf)). One server tick is 50 ms for everything. The corner-only test keeps the cheap half: a 2D search finds the corridor, and the heading test runs only where heading matters.

**Where it goes in Project Nublar:** `FenceAwareNodeEvaluator` neighbour generation, or a post-pass over the `Path` in `FenceAwareNavigation.createPath`. Read blocks through `BodyFit`, the only herd block reader in the spec ([herd-movement-redesign spec](../docs/superpowers/specs/2026-10-08-herd-movement-redesign.md)).

### Piece 5: spine or capsule herd spacing

**In game:** herd mates in single file keep a gap from snout to tail. Side by side, they stand closer than today.

**Verdict:** keep. Make the spine asymmetric. Add slot rules and single file. No studied mod uses non-circular spacing, so this piece is new.

**Why circles fail:** Nublar spaces herd mates by centre distance, `(w1 + w2)/2 + 1.0`. That is 2.2 blocks for two Gallimimus. At 2.2 blocks in single file, the two posed models overlap by about 2.95 blocks. A capsule with a 1.0 gap moves in-line spacing to about 6.15 blocks and side-by-side spacing to about 1.8 blocks. These figures are arithmetic on the posed model sizes. The single-file decision then shortens the in-line gap.

The robotics literature shows the same failure. Inflating a circle to cover a long robot "reduce[s] the capability to plan through narrow passages". A capsule fits gaps that a same-width circle cannot ([EPFL RDS](https://infoscience.epfl.ch/record/284026/files/09385856.pdf)). A capsule-capsule test is a segment-to-segment distance. Today's steering masks call the pair test once per heading per member, about 2,280 tests per herd evaluation (review M1). A circle broad phase first cuts that to about 60 capsule tests.

**Fixes and additions:**

1. **Asymmetric spine.** A Brachiosaurus reaches 5.31 blocks ahead of its origin and 11.66 behind. A symmetric capsule of half-length L/2 is badly off-centre.
2. **One choke point.** Replace `HerdSpacing.hardMinimum` and `HerdBodies.minimum` with the capsule test. `HerdBodies` must also store each member's yaw and spine ([HerdSpacing.java](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdSpacing.java), [HerdBodies.java](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdBodies.java)).
3. **New tuning.** Slot spacing, herd radius, graze spacing and the tight-ground threshold were all tuned on circles. `HerdSpacingTest` (22 tests) encodes circle results. All of these need new values.
4. **Single file in corridors.** Alex's Mobs elephants follow the animal ahead with a 4-block gap ([AM ElephantAIFollowCaravan.java](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/ElephantAIFollowCaravan.java#L21-L140)). Alex's Caves Underzealots keep a 1-block gap ([AC UnderzealotProcessionGoal.java](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/UnderzealotProcessionGoal.java#L42-L54)). No source switches from a wedge to a column. That switch is an inference.
5. **Slot reassignment.** When the formation turns, give each follower the nearest free slot. Fixed slot IDs make followers cross through each other ([Pottinger 1999](https://gamedeveloper.com/programming/implementing-coordinated-movement)).
6. **Priority at bottlenecks.** Give each animal a herd rank. A lower-rank animal yields to a higher-rank capsule ahead. Break ties by entity ID. This is an inference from Pottinger and the ORCA deadlock research.
7. **Valid slot targets.** Test each slot target against clearance before a follower walks to it. Slots inside trunks cause the tree snags left after piece 1.
8. **Herd jam recovery.** If over a quarter of the members fail to path in one tick, Jurassic Reborn reverses the herd heading ([Herd.java L196-206](https://github.com/Interactive-Modding/Jurassic-Reborn/blob/4a9a21b6ad7cea718534abd31913cccca2a6d98e/src/main/java/mod/reborn/server/entity/ai/Herd.java#L196-L206)).

**Do not rely on vanilla pushing.** Fossils ignores pushes on bodies 3 blocks or larger ([FA Prehistoric.java L522-529](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/Prehistoric.java#L522-L529)). Separation must come from steering or slots.

**Do not adopt ORCA or RVO2 by default.** They assume disc agents. The RRVO project reports that plain ORCA "performs very poorly" for non-circular shapes ([RRVO](https://parasollab.web.illinois.edu/research/rrvo/), via a search summary). Use them only if capsule separation oscillates head-on.

**Interaction with piece 3:** a follower that stops to turn falls back from its slot. Add hysteresis to slot assignment, or use the soft gate for followers in open ground.

### Stuck recovery

**In game:** a stuck dinosaur tries cheap fixes first. It never teleports into a wall in front of a player.

**Evidence:**

- Alex's Caves dinosaurs teleported through the Citadel stuck handler and suffocated in walls, "more prevalent in bigger mobs" ([AlexsCaves #852](https://github.com/AlexModGuy/AlexsCaves/issues/852)). The fix turned teleport off ([commit 51a08da4](https://github.com/AlexModGuy/AlexsCaves/commit/51a08da4)).
- The no-teleport Citadel handler waits 1200 ticks (60 s) before its first action ([CIT PathingStuckHandler.java](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/PathingStuckHandler.java#L139-L322)). Alex's Caves still has an open "stuck on a corner" report ([#1666](https://github.com/AlexModGuy/AlexsCaves/issues/1666)).
- One paper counts an agent as stuck only when its position change is small and its commanded velocity is near zero. It then applies a cooldown ([arXiv 2605.01783](https://arxiv.org/pdf/2605.01783)).
- A Roblox developer teleported a stuck AI to its start. It took the same failing path again ([Roblox DevForum](https://devforum.roblox.com/t/horror-pathfinding-ai-stuck/1733583)).

**Escalation ladder (inference), cheapest first:**

1. Skip to the next path node or corner.
2. Repath with a cost on the blocking node, so the new path differs.
3. Reverse about half a body length along recent positions. Then turn in place.
4. Followers only: leave the slot and path along the leader's trail.
5. Last resort: snap to the nearest position with valid clearance within a few blocks. Use a cooldown. The lead allows this also when a player is near.

**Project Nublar today:** the guard sets speed 0, so vanilla stuck detection never fires on a held path. `holdPath` stops a herd fallback path after 20 held ticks (1 s).

### Trees

**In game:** canopy leaves and trunks stop herds under low trees.

The lead's leaf and log rules (see [Decisions](#trees-and-leaves)) replace this section's open question. Shipped mods use two measures:

| Measure | In-game effect | Source |
|---|---|---|
| Passable leaves | The dinosaur walks through leaves and bamboo. Collision, node typing, path shortcuts and move control all agree. | Citadel `ICustomCollisions`, used by the Alex's Mobs tiger ([CIT ICustomCollisions.java](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/collision/ICustomCollisions.java#L22-L48), [AM EntityTiger.java](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/EntityTiger.java#L576-L591)) |
| Tagged block breaking | The dinosaur breaks leaves, logs and pewen branches that it bumps into. Needs mob griefing. | Alex's Caves Luxtructosaurus ([AC LuxtructosaurusEntity.java L394-410](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/LuxtructosaurusEntity.java#L394-L410)) |

Horizon Zero Dawn lets its largest machines smash rocks and trees only when angry or chasing ([Game Developer, HZD Part 2](https://www.gamedeveloper.com/design/behind-the-ai-of-horizon-zero-dawn-part-2-)).

## Hit detection: piece 6

**In game:** an arrow aimed at a raised neck hits the neck. A sword hit on the tail counts. The dinosaur turns on the attacker, and the attacker gets kill credit. Sections can take different damage.

**Verdict:** keep. Place section parts and herd spacing from the same per-species spine.

### Recipe

Every step in this table shipped in vanilla or a studied mod, except where the table says otherwise.

| Step | In-game effect | Source | Status |
|---|---|---|---|
| Place parts with one deterministic function on both sides | Server and client agree with no part packets | Vanilla dragon, [AC SauropodBaseEntity.java L247-305](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L247-L305) | Proven |
| Body-yaw ring buffer of 128 entries. Neck reads 10 ticks back, tail 20 ticks back. | The tail follows the route of the body | AC (same file) | Proven |
| Per-animation target angles, approached at a capped rate (`approachDegrees`) | The neck rises during a roar | [AC SauropodBaseEntity.java L442-525](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L442-L525) | AC only |
| Offsets in model pixels × render scale × growth. Part sizes scale too. | Hatchlings and adults get matching parts | [Iron's Spells IceSpiderPartEntity.java L35-39](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/entity/mobs/ice_spider/IceSpiderPartEntity.java#L35-L39) | Proven |
| Part IDs `parentId + i + 1`, set in a `setId` override | Client attack packets resolve to the right part | [V262 EnderDragon.java L851-859](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L851-L859) (third-party mirror) | Proven |
| Reserve a contiguous ID block in the constructor | No other entity takes an ID between the parent and its parts | [IAFCE MultipartPartEntity.java](https://github.com/IAFEnvoy/IceAndFire-CE/blob/63a3bba7960ad991c67fe1d1f4ea1c32e6523741/src/main/java/com/iafenvoy/iceandfire/entity/MultipartPartEntity.java) | New, Ice and Fire CE only, since 2026-08 |
| Create parts once. Keep the same objects through growth. | The part map stays valid | AC, Ice and Fire CE | Proven |
| Part `hurtServer` filters fall, drown, in-wall, falling block, lava and fire. It forwards the same `DamageSource` × a section multiplier. | A tail in lava does not burn the dinosaur. The attacker keeps credit. | [IAFCE MultipartPartEntity.java](https://github.com/IAFEnvoy/IceAndFire-CE/blob/63a3bba7960ad991c67fe1d1f4ea1c32e6523741/src/main/java/com/iafenvoy/iceandfire/entity/MultipartPartEntity.java) | Proven |
| Culling bounds cover all parts | Culling mods do not hide sections | [Twilight Forest #2579](https://github.com/TeamTwilight/twilightforest/issues/2579) shows the bug | Proven |

**Vanilla 26.2 already routes hits safely** when part IDs align (third-party mirror):

1. Crosshair and projectile picking keep the nearest box along the ray, parts included ([V262 ProjectileUtil.java L109-182](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/projectile/ProjectileUtil.java#L109-L182)).
2. The server resolves the ID with `getEntityOrPart`. It tests reach against the part's server box with a 3-block buffer ([V262 ServerGamePacketListenerImpl.java L1785-1836](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/server/network/ServerGamePacketListenerImpl.java#L1785-L1836)).
3. While `invulnerableTime` is above 10, a new hit applies only the amount above the last hit. An explosion across several parts counts once, at the highest amount ([V262 LivingEntity.java L1194-1208](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/LivingEntity.java#L1194-L1208)).

Every custom client-to-server relay in the studied mods weakened one of these tests. Alex's Mobs trusted a client damage amount. Alex's Caves accepted hits from 16 blocks. Do not write a relay.

**Long boxes miss hits.** [MC-261638](https://mojira.dev/MC-261638) is open. It covers large boxes that span chunk sections. It notes "you can hit a mob, but an arrow can't hit it at the same point". The game finds parts in the level-wide part list from every section. This favours parts even for the torso of very long dinosaurs. That is an inference.

**Torso.** Two torso parts take torso hits, and the parent is not pickable, as the vanilla dragon does (Decisions).

**Fidelity limit.** Alex's Caves bends the neck as a straight ramp of equal pitch steps. During a roar, the parts are a coarse envelope of the model. Alex's Caves hides the gap by posing the model from the parts ([AC SauropodBaseModel.java L432-461](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/client/model/SauropodBaseModel.java#L432-L461)). A GeckoLib model can do the same only through a bone updater on top of keyframes. Nobody tested whether GeckoLib 5 bone updaters fight the animation controller.

**Project Nublar today:** the T. rex head part is dead code. Nothing calls `resetParts`. Only client renderers write `DINOSAUR_HEAD_POS`, so the server head position is never set. `hurtFromPart` drops the attacker ([progress.md:299](../.superpowers/sdd/2026-10-08-herd-movement-redesign/progress.md)). Piece 6 replaces this code. Piece 6 fixes no stuck case.

### Server truth of the animated pose

**In game:** a Brachiosaurus lowers its head to drink. A player shoots at the visible head. The arrow hits only if the server part is near the visible head.

- The 26.2 reach test uses the server part box. So the server needs at least an approximate animated pose.
- GeckoLib 5 says bones have a position only at render time. It discards bone snapshots at the end of each render pass ([GeckoLib 5 Bone Snapshots](https://wiki.geckolib.com/docs/geckolib5/concepts/geobones/bone-snapshots)). Server GeckoLib posing therefore needs a separately written evaluator.
- The compute cost is small. 25 mobs × 40 bones × 20 ticks is 20,000 bone evaluations per second, well under 1 ms per tick. That is arithmetic, not a benchmark. The real cost is a server copy of every animation, Molang query and controller transition.
- Epic Fight poses joints on the server only during attack windows ([Epic Fight Collider.java](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/collider/Collider.java)). A bite can copy this: one server collider on a jaw point that the spine computes.
- Client-reported bones trust a client. Better Combat's server attacks client-sent entity IDs with its range test disabled by `if (true || ...)` ([BetterCombat ServerNetwork.java](https://raw.githubusercontent.com/ZsoltMolnarrr/BetterCombat/1.20.1/common/src/main/java/net/bettercombat/network/ServerNetwork.java)). MultiHitboxLib elects one tracking client to send bones.

**Superseded:** the lead chose baked poses as the server truth (Decisions). The original recommendation follows. The procedural spine is the server truth. Use GeckoLib 5 `addBonePositionListener` on the client only, for a debug overlay of the error between spine and bones ([GL262 RenderPassInfo.java L186-273](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/renderer/base/RenderPassInfo.java#L186-L273)). A later option clamps a client bone offset to about 1 block from the procedural position.

## Designs that are not better

No surveyed technique replaces the hybrid at better cost. The table lists the alternatives and the reason each one loses.

| Alternative | Why it is not better |
|---|---|
| Oriented or compound movement box for the mob | No source does it. It would break path sizing, `Entity.collide`, step-up, pushing, suffocation tests, spawn placement and movement sync (inference from absence). Create and Valkyrien Skies rotate rigid block grids, not mobs. |
| Colliding parts (CustomHitboxLib) | The planner sends the mob where it cannot turn. Its own FAQ warns of this. |
| Full Hybrid A* | 50 to 300 ms per replan on a 2008 PC. Use only the corner-only heading test. |
| ORCA or RVO2 | Assumes discs. Conflicts with vanilla pushing. Use capsule separation and priorities. |
| HAA* hierarchy | Described for static maps. Minecraft terrain changes. |
| One navmesh per size class (Recast, Horizon Zero Dawn) | Good idea, already native: use one clearance threshold per size class in the node evaluator. |
| Compound oriented box as the entity box (Bosses of Mass Destruction) | Credible for hits only. It overrides the box that vanilla uses for collision, lookups and culling. It needs `AABB` mixins. BOMD itself disables it with Better Combat. Unverified on 26.x. |
| Server GeckoLib posing (old DumbLibrary pattern) | Exact truth, but a whole animation evaluator to maintain on two loaders |
| MoreHitboxes as a dependency | Stuck on 1.21.1 and GeckoLib 4 since 2026-03-31. Its renderer mixin targets a method that GeckoLib 5.5.6 no longer has ([Modrinth versions](https://api.modrinth.com/v2/project/BOVAW87Z/version)). Copy its design, not its jar. |
| Countered's Accurate Hitboxes 2.x | No public source, no API, no part identity ([Modrinth](https://modrinth.com/mod/countereds-accurate-hitboxes)) |

## Proven versus new

| Element | Status | Where it shipped |
|---|---|---|
| Torso main box, non-colliding neck and tail | Proven | Alex's Caves, Ice and Fire, vanilla dragon |
| Arrival test at the aim point | Proven | Mowzie's Mobs, Fossils |
| Box-sweep shortcut | Proven | Mowzie's Mobs, Fossils, AzureLib |
| Hard turn gate | Proven | Alex's Caves sauropods |
| Soft turn gate and anti-orbit guard | Proven | Fossils |
| Hard gate in tight ground plus soft gate in open ground | New combination | None |
| Growth revert when blocked | Proven | Fossils |
| Symmetric ceil-based footprint | New for Minecraft | Harabor (research) |
| Corner clearance test | New | None |
| Capsule spacing | New in mods | Robotics research only |
| Single-file following | Proven | Alex's Mobs, Alex's Caves |
| Wedge-to-column switch | New | None |
| Slot reassignment and rank priority | New in Minecraft | Age of Empires (Pottinger) |
| Herd jam recovery by heading reversal | Proven | Jurassic Reborn |
| No-teleport stuck handling | Proven | Alex's Caves, Alex's Mobs |
| Passable leaves stack | Proven | Citadel, Alex's Mobs tiger |
| Procedural parts on both sides | Proven | Vanilla dragon, Alex's Caves |
| Aligned part IDs | Proven | Vanilla 26.x, MoreHitboxes, Ice and Fire CE |
| Contiguous ID reservation | New, one source | Ice and Fire CE |
| GeckoLib 5 bone listeners for hitboxes | New | API exists. No studied mod uses it for hitboxes. |
| One spine shared by spacing and parts | New | None |

No studied mod ships a long-bodied animated mob with both server-truth section parts and box-aware navigation. The full hybrid has no direct precedent.

## Cross-loader path (NeoForge and Fabric 26.2)

**In game:** the same dinosaur behaves the same on a NeoForge server and a Fabric server.

### Dependencies

| Dependency | 26.2 status | Action |
|---|---|---|
| GeckoLib | 5.5.6 on NeoForge, 5.5.5 newest on Fabric. Package `com.geckolib`. Render-state renderers. ([Modrinth](https://api.modrinth.com/v2/project/geckolib/version?game_versions=%5B%2226.2%22%5D)) | Pin one version on both loaders. Remove all renderer writes to synced entity data. |
| SmartBrainLib | 2.0.x on NeoForge, Forge and Fabric ([Modrinth](https://api.modrinth.com/v2/project/smartbrainlib/version?game_versions=[%2226.2%22])) | Port |
| Vanilla pathfinding | `PathType` and `PathfindingContext` replace `BlockPathTypes` | Rewrite `FenceAwareNodeEvaluator`. Fabric `LandPathTypeRegistry` has no mob parameter ([Fabric API javadoc](https://maven.fabricmc.net/docs/fabric-api-0.150.1%2B26.2/net/fabricmc/fabric/api/registry/LandPathTypeRegistry.html)), so keep the evaluator override on both loaders. |
| Step height | An attribute since the 1.20.5 era | Change `setMaxUpStep(1.0F)` |

Keep all spine, capsule, footprint and corner geometry in pure common classes. The spec already makes `BodyFit` the only block reader, "so the 26.2 port changes one place".

### Section parts per loader

| Need | NeoForge 26.2 | Fabric 26.2 |
|---|---|---|
| Unspawned part class | Native `PartEntity` ([NF262 PartEntity.java](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/src/main/java/net/neoforged/neoforge/entity/PartEntity.java)) | Own class that extends `Entity` and throws on `getAddEntityPacket` |
| Level-wide part list, filled at tracking start and end | Native ([NF262 ServerLevel.java.patch](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/server/level/ServerLevel.java.patch#L268-L291)) | Mixin on `ServerLevel`, `ClientLevel` and their entity callbacks |
| Parts in `Level.getEntities` and `hasEntities` | Native ([NF262 Level.java.patch](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/level/Level.java.patch#L133-L187)) | `RETURN` injects on both `getEntities` variants and `hasEntities` |
| Attack packets resolve parts | Native `getEntityOrPart` | `TAIL` inject on `ServerLevel.getEntityOrPart` |
| Parts never tracked | Native `ChunkMap.addEntity` skip | Cancel `ChunkMap.addEntity` for parts |
| Item effects and camera go to the parent | Native | Mixins on `Player` and `ServerPlayer` |
| Aligned IDs | Own `setId` override, common code | Same common code |

Fabric API has no multipart API, and no issue proposes one ([fabric-api issues](https://github.com/FabricMC/fabric-api/issues?q=PartEntity)). Two public mods show the Fabric mixin set:

- MoreHitboxes' Fabric module, 1.21.1 ([MH21 FabricLevelMixin.java](https://github.com/DarkPred/MoreHitboxes/blob/88899b3e1c4ed881b1ea65b35104453fc6df592a/fabric/src/main/java/com/github/darkpred/morehitboxes/mixin/FabricLevelMixin.java#L22-L61)). MIT.
- Aquamirae, with about four common mixins for both loaders ([Aquamirae MixinLevel.java](https://github.com/ObscuriaLithium/aquamirae/blob/a468d77cc443a932d56a7aa34303f4e435eca3b6/common/src/main/java/dev/obscuria/aquamirae/integration/mixin/MixinLevel.java)). Its targets on 26.x are unverified.

**Rules for the port:**

- Set part IDs before the parent starts tracking. Never swap part objects or IDs while the game tracks the parent. NeoForge registers parts by ID at tracking start.
- Pick one rule for typed queries such as `getEntitiesOfClass(LivingEntity.class, …)`. NeoForge returns parts only to queries that accept `PartEntity`. MoreHitboxes Fabric returns the parent. Apply the same rule on both loaders.
- Do not copy Biome Makeover. It adds parts only for parents that the section query found, which keeps the MC-261638 miss.
- Do not copy the MoreHitboxes `ServerPlayerMixin` camera redirect as it is. Code reading suggests that a second `setCamera` call can leave the old camera. This is untested.
- Pin NeoForge to the `26.2.x` branch. The default branch is now `26.3.x`.

The worktree records three current blockers. The GameTests live in the Forge module. The Fabric build fails. The mod crashes on a dedicated server ([herd-movement handoff](../docs/superpowers/handoffs/2026-10-09-herd-movement-handoff.md)).

## Data values

### Spine lengths (adult, gene 0)

Measured from each geo file in the posed rest model, with bone rotations applied, at render scale. These values replace the earlier raw-pivot table.

| Species | Length (blocks) | Ahead of origin | Behind origin | Torso | Length ÷ movement box width | Turn sweep radius |
|---|---|---|---|---|---|---|
| Gallimimus | 5.15 | 2.20 | 2.95 | 1.64 | 6.4 | about 2.95 |
| T. rex | 10.13 | 3.94 | 6.19 | 2.54 | 6.5 | about 6.19 |
| Triceratops | 11.16 | 5.28 | 5.88 | 5.02 | 5.1 | about 5.88 |
| Brachiosaurus | 16.97 | 5.31 | 11.66 | 8.57 | 4.6 | about 11.66 |
| Velociraptor | 4.62 | 1.52 | 3.10 | 1.14 | 6.9 | about 3.10 |
| Dilophosaurus | 4.39 | 1.93 | 2.46 | 1.26 | 7.8 | about 2.46 |

Growth scales these smoothly through the 26.2 `scale` attribute. The turn sweep radius is the larger reach, an inference.

### Proposed part layouts (all six species)

These layouts are proposals. They follow the decided rules:

- square footprints
- a torso of two parts
- one part per leg
- chained necks and tails
- sizes from the render-scaled model

The Gallimimus layout is the one the lead reviewed. The interactive page is [Dinosaur hitbox layouts.html](Dinosaur%20hitbox%20layouts.html).

Measured from each geo file in the rest pose, with bone rotations applied, at gene 0 and adult size.

| Species | Render scale | Parts | Head | Neck | Torso | Tail | Legs | Movement box (W × H) | Today's box |
|---|---|---|---|---|---|---|---|---|---|
| Gallimimus | 1.0 | 12 | 1 | 2 | 2 | 5 | 2 | 0.80 × 2.39 | 1.2 × 2.6 |
| T. rex | 1.0 | 13 | 1 | 2 | 2 | 6 | 2 | 1.56 × 3.53 | 1 × 3 |
| Triceratops | 2.0 | 12 | 1 | 1 | 2 | 4 | 4 | 2.19 × 4.25 | 2 × 3 |
| Brachiosaurus | 2.5 | 17 | 1 | 4 | 2 | 6 | 4 | 3.68 × 8.87 | 2 × 3 |
| Velociraptor | 0.6 | 12 | 1 | 2 | 2 | 5 | 2 | 0.67 × 1.53 | 0.6 × 0.6 |
| Dilophosaurus | 0.5 | 11 | 1 | 2 | 2 | 4 | 2 | 0.56 × 1.44 | 1 × 1 |

**Notes:**

- **Brachiosaurus movement box.** Its legs spread 3.68 blocks wide, so the torso-and-legs rule gives a 3.68-block movement box. In game, an adult Brachiosaurus then needs gaps about 4 blocks wide and stays out of forests (decided).
- **Brachiosaurus neck.** The neck stands almost vertical, so its four parts stack by height. A tall part keeps its square footprint, so height costs nothing when the body turns.
- **Triceratops head.** The single head part covers the frill, so it is 3.8 blocks wide. The one neck part sits mostly inside the frill and the shoulders.
- **Arms.** No species has arm parts (decided). The Velociraptor arms hang about 0.4 blocks below its torso parts, and hits there pass through. T. rex and Dilophosaurus arms sit mostly inside the torso parts.
- **Dilophosaurus frill.** The head part covers the head without the frill (decided). In the rest pose, the frill spreads 1.4 blocks wide.

### Timing and tuning constants

| Value | Current Nublar | Source value |
|---|---|---|
| Turn rate | 10°/tick (200°/s), all yaw changes | AC sauropod body: 2°/tick (40°/s). Fossils: `clamp(90 − 35·w, 5, 90)` °/tick. |
| Turn gate | None | AC: 15°. Cataclysm Leviathan: 4°. |
| Anti-orbit distance² | None | Fossils: 0.12 |
| Arrival tolerance | Vanilla `w/2` from `node + 0.5` | Fossils: 0.5 from the aim point. AM and AC: 0.75·w. |
| Stuck window | Path hold exit after 20 ticks (1 s). Wait limit 160 ticks (8 s). | Vanilla: 100 ticks (5 s). Citadel: first action after 1200 ticks (60 s). |
| Herd gap | 1.0 block (0.5 resting or in tight ground) | Alex's Mobs caravan: 4 blocks. Underzealots: 1 block. |
| Growth fix-up limit | Vanilla, 4.0 blocks | Fossils: no limit, keep old box if blocked |
| Clearance box refresh | 100 ticks (5 s) | n/a |

## Recommended build order

This order is an inference, updated after the adversarial review. Each step targets the playtest failures first.

1. **Check 26.2 navigation.** Read 26.2 `PathNavigation.followThePath`, `Path.getEntityPosAtNode` and `WalkNodeEvaluator` from the NeoForge 26.2 or Fabric 26.2 toolchain sources.
2. **Build the per-species block filter** (pass, break, solid). Every block reader uses it: `Entity.collide`, `BodyFit`, the node evaluator, the sweep and the IK (review H4). Test it under a 3-high canopy.
3. **Fix arrival and add the sweep shortcut (piece 2).** Override path following in `FenceAwareNavigation`. Test arrival at the aim point. Add the Mowzie's-style box sweep. Use a symmetric footprint.
4. **Settle the box and growth (piece 1).** Drive growth through the `scale` attribute. Override `getDimensions` fully. Add the Fossils growth guard. Split the clearance box by job.
5. **Add the turn gate (piece 3).** Hard gate in tight ground, soft gate elsewhere, anti-orbit guard.
6. **Add stuck recovery and the tree rules.** Use the escalation ladder. The last rung snaps to a clear spot.
7. **Build the shared spine and the pose clock.** Define a per-species spine from the posed geo data. Add the synced pose state (review C2).
8. **Add capsule spacing (piece 5).** Add these parts:
   - the circle broad phase
   - single file with the snout-to-tail gap
   - slot reassignment and rank priority
   - valid slot targets and herd jam recovery

   Retune and rewrite the circle tests.
9. **Add the corner clearance test (piece 4)** as a path cost. Add the follower spine danger term (review H3).
10. **Add section parts (piece 6).** Remove the dead T. rex part code first. Add the chunk-section part buckets and distance-based part counts in the same step (review C1). Place parts in every tick and on load (review M5). Start on NeoForge, then add the Fabric mixins.
11. **Add the bake step** with its golden test and hash check (review H6).
12. **Add neck and tail IK** with the synced bend side (review H1) and the GeckoLib 5 delta updater (review M6).
13. **Add client leg IK** once per tick, with distance-based detail (review M8).

Measure each step against the playtest stuck cases. Playtest 3 has no recorded result, so nobody knows which causes remain after the P8 fixes. Register GameTests in common code, so both loaders run them (review M10).

## Changes from the adversarial review

The review is [Multipart hitbox hybrid design review.md](Multipart%20hitbox%20hybrid%20design%20review.md). Its fixes are part of this design. The lead answered its questions on 2026-10-09.

| ID | In-game effect | Change to the design |
|---|---|---|
| C1 | The server keeps its tick time with 100 dinosaurs | Chunk-section part buckets on both loaders. Distance-based part counts. Herd and IK code read block shapes directly. |
| C2 | Server parts follow the animation that plays | A synced pose state (animation id, start tick, loop flag). Walk and run phase by distance walked. `life_time` Molang terms bake as the rest pose. Transitions blend over the controller's transition ticks. |
| H1 | Bends match on both sides | The server syncs one bend-side byte per chain, with hysteresis. A side that has not loaded a part's chunk keeps the last pose. |
| H2 | A 20-animal column stays manageable | Snout-to-tail gap in single file, with neck-over-tail overlap allowed |
| H3 | Followers also keep their ends clear at corners | A spine danger term in context steering: head and tail-tip cells for the best headings, about 10 cell reads per evaluation |
| H4 | Passable leaves work everywhere | One per-species block filter used by five code paths |
| H5 | Growth never clips or double-scales | `scale` attribute, full `getDimensions` override, part refresh after load and on scale change, `noPhysics` on parts, Fossils growth guard |
| H6 | Server parts match the shipped animations | A golden test against recorded bone positions. A bake hash in the network version check. A build failure when an animation is newer than its bake. The tables live in the mod jar (see Decisions). |
| M1 | Spacing stays cheap | Circle broad phase before capsule tests |
| M2 | Bending stays cheap | Direct, cached block reads. Skip parts in non-ticking chunks. At most 6 bend steps per chain per tick. |
| M3 | A sweep or potion on any part reaches the dinosaur | Custom range test to the nearest part, with an identity set so each dinosaur counts once |
| M4 | — | Not applicable. The mod has no mounts. |
| M5 | Exhibit dinosaurs with `NoAI` can be hit | Place parts at the end of every `tick()`, in the constructor, on packet recreation and after load |
| M6 | The visible tail does not sway twice or step | The GeckoLib 5 updater adds only the bent-minus-unbent delta, lerped by partial tick |
| M7 | Implementers read one rule per topic | Stale text fixed in this page |
| M8 | The frame rate holds with herds on screen | Leg IK once per tick, lerped per frame. Full IK within 16 blocks, none past 32. |
| M9 | Brachiosaurus paths stay affordable | Open ground only (decided). At most one large-species path per herd per tick. |
| M10 | Tests run on both loaders | GameTests in common code with a shim per loader |
| L1 | Fire from a flaming arrow on the neck reaches the dinosaur | The part forwards fire ticks. Tipped-arrow effects and Punch on a part stay lost unless a mixin on `onHitEntity` is added. |
| L2 | Other multipart mods keep working | Inject only this mod's parts. Re-test the NeoForge hook per NeoForge release. |
| L3 | A loaded dinosaur's tail straightens, then bends again | Accepted. One full placement runs before the first render. |
| L4 | Leg parts stick out 0.11 blocks past the movement box | Accepted. Parts do not collide. |

## Open decisions for the lead

The lead answered every item that this list held. The answers are in [Decisions](#decisions). One item remains:

1. **Neck reach IK.** It waits until the eating and drinking animations exist.

## Items flagged for the lead

1. **The `javap` figures hold.** The ledger at [progress.md:287](../.superpowers/sdd/2026-10-08-herd-movement-redesign/progress.md) records an earlier investigation that read the Minecraft jar with `javap`. On 2026-10-10, a check of the project's own 1.20.1 toolchain sources matched every vanilla claim marked [javap] in that investigation.
2. **Links not pinned to commits.** The links in `oriented_shapes_and_server_animation.md` point to branch heads as read on 2026-10-09, not to commit SHAs. This affects the Epic Fight, Better Combat, Create and Valkyrien Skies citations on this page. Pin them before anyone quotes them as stable references.

## Conclusion

The research moves the problem away from hitboxes. Vanilla already centres the aim point. The defect that best fits "spinning" and "stuck at gaps" sits in one arrival test, and three mod families fixed it in a few lines. Fix that, add a box sweep, and gate turns. Then the torso box carries movement with no new geometry.

The new work is the spine. One per-species spine, scaled by one growth function, can drive herd spacing, corner clearance and section parts. No shipped mod has that single source of truth. It is also where the remaining risk lives: tuning the capsule herd and proving the corner test in forests. The lead answered the main question. The server knows the animated pose through baked data and a synced pose clock. No GeckoLib evaluator runs on the server.
