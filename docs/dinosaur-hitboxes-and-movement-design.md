# Dinosaur hitboxes and movement design

> **Location:** this is the authoritative copy, moved from the herd-leader worktree into `docs/` on 2026-10-10 so the project plan (modules B26 and B27) does not depend on the worktree. The research notes, the superseded reports and the uncommitted herd code it cites still live in the worktree (`.claude/worktrees/herd-leader-sample`). Links to them work while the worktree exists.

> **Status:** Consolidated 2026-10-10. This page is the one current design for dinosaur hitboxes, movement, anti-clipping and IK. The target is Minecraft 26.2 on NeoForge and Fabric, with GeckoLib 5.
>
> **This page supersedes these documents.** They stay in this folder as history. Nobody edits them.
>
> - [Multipart entity hitbox systems.md](../.claude/worktrees/herd-leader-sample/reports/Multipart%20entity%20hitbox%20systems.md) (round 1 survey, 2026-10-09)
> - [Multipart entity hitbox systems (STE-MC).md](../.claude/worktrees/herd-leader-sample/reports/Multipart%20entity%20hitbox%20systems%20(STE-MC).md) (style copy of round 1)
> - [Multipart hitbox hybrid design.md](../.claude/worktrees/herd-leader-sample/reports/Multipart%20hitbox%20hybrid%20design.md) (round 2 design, with the lead's decisions)
> - [Multipart hitbox hybrid design review.md](../.claude/worktrees/herd-leader-sample/reports/Multipart%20hitbox%20hybrid%20design%20review.md) (adversarial review, 22 findings)
> - [Gallimimus hitbox mockup.html](../.claude/worktrees/herd-leader-sample/reports/Gallimimus%20hitbox%20mockup.html) (replaced by the layouts page)
>
> **Companion page:** [Dinosaur hitbox layouts.html](dinosaur-hitbox-layouts.html) draws the part layouts of all six species. It stays current.
>
> **Labels:** every value has a source. "Inference" marks a conclusion from facts. "Estimate" marks a number that nobody measured.

## Summary

**In game, the design gives these results:**

- A dinosaur moves and finds paths with a box the size of its torso and legs. At tight corners, its neck and tail can overlap blocks. Then they bend away within their joint limits.
- A wide dinosaur walks from node to node. It does not circle a node or spin on the spot.
- On open ground, a dinosaur walks straight lines, not staircases. It does not cut corners into trunks.
- In tight ground, a dinosaur stops, turns, then walks. In open ground, it slows while it turns.
- Herd mates keep space along their full snout-to-tail length. In corridors, the herd walks in single file.
- A stuck dinosaur tries cheap recoveries first. As the last step, it snaps to a nearby clear spot.
- With the default config, leaves never stop a dinosaur. Small species pass through leaves. Large species break leaves with the torso. Trunks stop calm animals, and a charging large animal breaks them.
- An adult Brachiosaurus stays in open ground.
- Hits land on the visible head, neck, torso, legs and tail. A head hit hurts most. The attacker keeps kill credit.
- Hit parts follow the real animations. An arrow aimed at a raised head hits the head.
- A dinosaur grows smoothly. Its box, its parts and its model grow together.
- Feet sit on uneven ground and steps. This is visual only.
- With 100 dinosaurs loaded, the server cost of this design is about 9 to 15 ms per tick (estimate).

**The idea in one paragraph.** Two owners split the work. The movement box decides where the body can go. The hit parts decide what a hit touches. One per-species spine, scaled by one growth value, places the parts and drives herd spacing, the corner test and the neck and tail bend. Every studied mod except CustomHitboxLib keeps collision and paths on one vanilla box. No studied mod ships a long animated mob with both server-truth parts and box-aware navigation.

### At a glance

| Area | In-game effect | Design | Precedent |
|---|---|---|---|
| Movement box | The dinosaur fits any gap that its torso and legs fit | Width from the scaled torso and legs | Field standard (Alex's Caves, Ice and Fire, vanilla dragon) |
| Arrival fix | No circling at path nodes | Test arrival at the aim point | Mowzie's Mobs, Fossils |
| Sweep shortcut | Straight lines, no corner cuts | Sweep the movement box toward later nodes | Mowzie's Mobs, Fossils, AzureLib |
| Footprint | Gaps sized to the real width | Symmetric, ceil-based, centred | New for Minecraft (Harabor research) |
| Turn gates | Turn in place when tight, curve when open | Hard gate in tight ground, soft gate elsewhere, anti-orbit guard | Alex's Caves, Fossils. The combination is new. |
| Corner test | The leader prefers corners where its body can turn | Soft path cost, plus a spine danger term for followers | New |
| Spacing | No nose-to-tail overlap in the herd | Asymmetric capsules, single file in corridors | Capsules: robotics only. Single file: Alex's Mobs, Alex's Caves. |
| Stuck ladder | Cheap recoveries first, snap last | Five rungs | Inference from several sources |
| Block filter and trees | Leaves and trunks act per species | One filter, five code paths | Citadel, Alex's Caves |
| Hit parts | Hits land on visible sections | Unspawned square parts, aligned IDs | Vanilla dragon, Alex's Caves, Ice and Fire CE |
| Server truth | Parts follow the animation | Baked poses and a synced pose state | New |
| Neck and tail bend | Hit parts and model agree near blocks | Server IK, synced bend side | New |
| Leg IK | Feet on the ground | Client only, with distance detail | CHAOS branch code, reworked |
| Growth | Box, parts and model grow together | 26.2 `scale` attribute, growth guard | Vanilla 26.2, Fossils |

## Terms

| Term | Meaning |
|---|---|
| Movement box | The dinosaur's one vanilla axis-aligned bounding box (AABB). It moves, collides and finds paths. It is not pickable. |
| Part | An unspawned hit box that belongs to the dinosaur. It never collides with blocks. |
| Section | The body region that a part covers: head, neck, torso, leg or tail. |
| Spine | A per-species line of points from snout tip to tail tip. Body yaw rotates it and growth scales it. |
| Capsule | A line segment with a radius. Herd spacing uses one capsule per animal. |
| Footprint | The square of block columns that the pathfinder tests for each path node. |
| Aim point | The point that path following sends to the move control. |
| Arrival test | The test that decides that the dinosaur reached a path node. |
| Pose state | The synced animation id, start tick and loop flag. The server owns it. Earlier documents call it the pose clock. |
| Bake | The build step that samples the animation files into position tables for each section. |
| Bend side | The side to which a neck or tail chain bends around a block. The server picks it. |
| Block filter | The per-species rule that answers pass, break or solid for a block state. |
| Render scale | The species scale times the growth scale. The `scale` attribute carries it on 26.2. |
| Large species | Brachiosaurus, Triceratops and T. rex |
| Charging | Running, chasing or stampeding |
| Tight ground | The herd code's flag for a narrow space (`tightGround`) |
| Server truth | The server knows where a section is during an animation |

## Decisions register

The lead made these decisions after the research and the review. Each row gives the latest ruling. "Replaces" names an earlier ruling or recommendation that this ruling overrides.

### Hitbox decisions

| Topic | Ruling | Replaces |
|---|---|---|
| Hit-detection approach | Own parts, with the design copied from MoreHitboxes and other mods. No MoreHitboxes jar. Research recommendation, accepted with the design. | Round 1 option A: MoreHitboxes as a dependency |
| Torso | Two square torso parts take torso hits. The movement box is not pickable. Parts pass right-click interactions to the dinosaur. | Round 2 body text: parent pickable, torso part optional |
| Part shape | Every part has a square footprint. Long sections are chains of squares. | — |
| Legs | One part per leg, ×1.0. Gallimimus legs are 0.6 × 0.6. | — |
| Tail tip | Two square parts cover the thin tail tip | — |
| Arms and frill | No arm parts. No Dilophosaurus frill part. Hits there pass through. | — |
| Client refinement | None. Parts stay on the deterministic positions. Approximate placement is acceptable. | — |
| Multipliers | Head ×1.5, neck ×1.25, torso ×1.0, legs ×1.0, tail ×0.5 | — |
| Area damage, untyped | Explosions find the part, and the part forwards the damage | — |
| Area damage, typed | A typed `LivingEntity` query that touches a part returns the dinosaur once, through an identity set | Round 2 port rule: "pick one rule for typed queries" |
| Area damage range | The range test measures to the nearest part, not to the torso | Review M3 proposal: torso within the effect's own range |
| Sizes | From the render-scaled posed model, not raw pivots. The movement box width comes from the scaled torso and legs. | Raw-pivot spine table. Gallimimus torso 0.70. |
| Server truth | Baked poses from the real animation files are the default. Hand-tuned angles cover only what the bake cannot cover, such as a look-at-target head turn. No server GeckoLib posing. | Round 1: rest pose plus a procedural estimate. Round 2 body: procedural spine only. |
| Walking | Yaw history, plus a baked walk and run cycle on top | — |
| Pose state | The server owns a synced pose state: animation id, start tick, loop flag | — |
| Bake location | The tables live inside the mod jar. Both sides load them directly, not through data packs or resource packs. A Gradle step builds them. | Round 2 Decisions, first version: tables in `data/`. Review H6: tables under `assets/`. |
| Bake safety | A hash check on join blocks a client whose tables differ. A golden test guards the bake. | — |
| Part scan cost | Chunk-section part buckets on both loaders, including a mixin into NeoForge's patched `Level.getEntities`. Distance-based part counts with the same objects and IDs. Direct block reads in herd and IK code, never `noCollision`. | — |

### IK and anti-clipping decisions

| Topic | Ruling | Replaces |
|---|---|---|
| Anti-clipping | Both methods. The planner avoids clipping spots, and the server bends the neck and tail within joint limits. The lead accepts small remaining mismatches. | — |
| Neck and tail IK | Runs on the server tick with deterministic code. The client runs the same code. | CHAOS branch: client solve per render frame |
| Bend side | The server picks the bend side per chain and syncs one byte | — |
| Leg IK | Client only, visual only. Once per tick, lerped per frame. Full within 16 blocks, none past 32 blocks. | CHAOS branch: per-frame solve and server step planner |
| CHAOS branch IK code | Part of the mod. The team uses it together with its sources: Mowzie's Mobs, the spider repo and others that apply. Credit is given as the Mowzie's Mobs terms require. The lead handles licence questions. | — |
| Head contact | Eating and drinking count only when the head touches the source. The animal must be able to reach it. | — |
| Neck reach IK | Deferred until eating and drinking animations exist | — |

### Movement decisions

| Topic | Ruling | Replaces |
|---|---|---|
| Node aim | Keep vanilla's aim point. Fix the arrival test. Research recommendation, accepted with the design. | Round 1: re-centre node targets |
| Clearance split | Paths and gaps use the scaled torso-and-legs width. Herd spacing uses the whole scaled model. A per-species config value can change the choice. | — |
| Corner test | A soft cost in the planner, never a hard rule | — |
| Follower clipping | A spine danger term in context steering | — |
| Corridors | The herd switches from the wedge to single file when the path narrows | — |
| Single-file gap | Measured from the follower's snout to the tail tip ahead. A neck may overlap the tail ahead. | Round 2 piece 5: 1.0-block capsule gap in line (problem raised by review H2) |
| In-line pitch | Chest rule by default: the follower's chest stays behind the tail tip ahead. Today's tight spacing applies in single file in a corridor, and for herds whose chest-rule column would outgrow the base trail. Values are per-species config, tuned in playtest (2026-10-10). | No value |
| Trail guard | Trail capacity grows with the herd's single-file column. Animals past the trail tail follow the animal ahead (2026-10-10). | Fixed 128-block trail |
| Last-resort snap | Allowed as the last rung of the stuck ladder, also when a player is near | Round 2 body text: no snap near players |
| Brachiosaurus | Movement box 3.68 × 8.87. It stays out of forests. Playtests measure it in open ground. | — |
| Large-species paths | At most one large-species path per herd per tick | — |

### Tree decisions

| Topic | Ruling | Replaces |
|---|---|---|
| Leaves | Per-species config: pass, break or solid. Default: Brachiosaurus, Triceratops and adult T. rex break leaves. Gallimimus, Velociraptor and Dilophosaurus pass through. | Round 2 open question: passable leaves, block breaking, or neither |
| What breaks leaves | Only the movement box, and only above a per-species growth cut-off. Head and neck pass through canopy leaves. | — |
| Logs | Large species break logs only when charging. Per-species config. | — |
| Breakable blocks | A mod tag lists them. Player-placed leaves break too. | — |
| `mobGriefing` off | A leaf-breaking species passes through leaves. Logs stay solid for every species, also when charging. Players design their parks with that in mind. | — |
| Block filter | One per-species filter, used by five code paths | — |

### Growth decisions

| Topic | Ruling | Replaces |
|---|---|---|
| Growth | Smooth. The 26.2 `scale` attribute is the one scale source for box, parts and renderer. | Four growth stages in code today (a code gap, not a design choice) |
| Growth safety | Full `getDimensions` override. Part refresh after load and on scale change. `noPhysics` on parts. Fossils growth guard. | — |

### Sources and process decisions

| Topic | Ruling | Replaces |
|---|---|---|
| Vanilla source | Read vanilla source through the modding toolchain: ModDevGradle and Loom with Parchment for 1.20.1, and the NeoForge and Fabric 26.2 toolchains for the port | Third-party mirrors as the main source |
| Mirrors | Third-party mirrors stay a labelled secondary source | — |
| Other commercial games | Public sources only. Standing project rule. | — |
| Assets | Design only for animations and models that exist | — |
| Tests | GameTests in common code, with a shim per loader | GameTests in the Forge module |

## Hitboxes

**In game:** an arrow aimed at a raised neck hits the neck. A sword hit on the tail counts. A creeper next to the tail hurts the dinosaur. The dinosaur retaliates against the attacker, and the attacker gets kill credit.

### Part rules

| Rule | In-game effect | Detail |
|---|---|---|
| Square footprint | A part keeps its size when the body turns | Vanilla `EntityDimensions` has one width. Long sections become chains of squares, as on the Ender Dragon and in Alex's Caves. |
| Two torso parts | Every hit lands on a named section | The movement box is not pickable, as on the Ender Dragon |
| One part per leg | A sword hit or arrow on a leg counts | ×1.0 multiplier |
| Chained neck and tail | Hits on a long neck or tail count | Each part chains from the end of the previous part, not from one anchor |
| Two tail-tip parts | A hit on the thin tail end counts | The last two tail parts |
| No arm or frill part | Hits on a Velociraptor arm or a spread Dilophosaurus frill pass through | Decided |
| Interactions | A right-click on any part reaches the dinosaur | The part forwards the interaction to the dinosaur |
| No collision | Parts never stop the body | `noPhysics = true` on every part |
| Sizes | Hitboxes match the dinosaur as the player sees it | Sizes come from the render-scaled posed model |

### Layouts for the six species

Measured from each geo file in the rest pose, with bone rotations applied, at gene 0, adult size and render scale. The data is the `D` object in [Dinosaur hitbox layouts.html](dinosaur-hitbox-layouts.html). Each `parts` row is `[section, x, z, width, depth, y0, y1]` in blocks.

**Part counts and boxes:**

| Species | Render scale | Parts | Head | Neck | Torso | Tail | Legs | Movement box (W × H) | Today's box |
|---|---|---|---|---|---|---|---|---|---|
| Gallimimus | 1.0 | 12 | 1 | 2 | 2 | 5 | 2 | 0.80 × 2.39 | 1.2 × 2.6 |
| T. rex | 1.0 | 13 | 1 | 2 | 2 | 6 | 2 | 1.56 × 3.53 | 1 × 3 |
| Triceratops | 2.0 | 12 | 1 | 1 | 2 | 4 | 4 | 2.19 × 4.25 | 2 × 3 |
| Brachiosaurus | 2.5 | 17 | 1 | 4 | 2 | 6 | 4 | 3.68 × 8.87 | 2 × 3 |
| Velociraptor | 0.6 | 12 | 1 | 2 | 2 | 5 | 2 | 0.67 × 1.53 | 0.6 × 0.6 |
| Dilophosaurus | 0.5 | 11 | 1 | 2 | 2 | 4 | 2 | 0.56 × 1.44 | 1 × 1 |

**Part widths (blocks):**

| Species | Head | Neck, first to last part | Torso (each) | Tail, first to last part | Leg (each) |
|---|---|---|---|---|---|
| Gallimimus | 0.45 | 0.40, 0.50 | 0.75 | 0.55 to 0.50 | 0.60 |
| T. rex | 1.61 | 0.87, 0.89 | 1.27 | 1.10 to 0.79 (range 0.75 to 1.10) | 0.90 |
| Triceratops | 3.80 | 0.90 | 2.51 | 1.10 to 0.88 (range 0.86 to 1.10) | 1.00 |
| Brachiosaurus | 1.51 | 2.13, 1.90, 1.35, 1.60 | 4.285 | 1.78 to 0.81 | 1.70 front, 1.80 rear |
| Velociraptor | 0.64 | 0.35, 0.40 | 0.57 | 0.53 to 0.55 | 0.45 |
| Dilophosaurus | 0.44 | 0.37, 0.38 | 0.63 | 0.56 to 0.56 (range 0.55 to 0.56) | 0.40 |

**Spine (posed rest model, adult, gene 0, render scale):**

| Species | Length | Ahead of origin | Behind origin | Torso length | Length ÷ movement box width | Turn sweep radius |
|---|---|---|---|---|---|---|
| Gallimimus | 5.15 | 2.20 | 2.95 | 1.64 | 6.4 | about 2.95 |
| T. rex | 10.13 | 3.94 | 6.19 | 2.54 | 6.5 | about 6.19 |
| Triceratops | 11.16 | 5.28 | 5.88 | 5.02 | 5.1 | about 5.88 |
| Brachiosaurus | 16.97 | 5.31 | 11.66 | 8.57 | 4.6 | about 11.66 |
| Velociraptor | 4.62 | 1.52 | 3.10 | 1.14 | 6.9 | about 3.10 |
| Dilophosaurus | 4.39 | 1.93 | 2.46 | 1.26 | 7.8 | about 2.46 |

The turn sweep radius is the larger of the two reaches (inference). Growth scales every value through the `scale` attribute.

**Notes on the layouts:**

- **Gallimimus.** The lead reviewed this layout. The torso and legs span 0.80 blocks, so the movement box is 0.80 wide. Leg parts extend 0.11 blocks past the box on each side. The lead accepts this, because parts do not collide. The model torso is 1.64 blocks long. The two torso parts span 1.52 blocks (z −1.195 to 0.325). The neck and tail parts cover the 0.05-block front edge and the 0.07-block rear edge, so no part of the torso stays uncovered.
- **Brachiosaurus.** The legs spread 3.68 blocks, so the movement box is 3.68 wide. The neck stands almost vertical, so its four parts stack by height. A tall part keeps its square footprint, so height costs nothing when the body turns.
- **Triceratops.** The single head part covers the frill, so it is 3.8 blocks wide. The one neck part sits mostly inside the frill and the shoulders.
- **Arms.** Velociraptor arms hang about 0.4 blocks below its torso parts. T. rex and Dilophosaurus arms sit mostly inside the torso parts.
- **Dilophosaurus frill.** The head part covers the head without the frill. In the rest pose, the frill spreads 1.4 blocks wide.

### Placement layers

**In game:** the tail trails the route of the body and sways with the walk. A calling Gallimimus raises its head, and the head part rises with it. Near a trunk, the tail part bends away with the visible tail.

One deterministic placement function runs on both sides. No part packets cross the network. It runs at the end of every `tick()`, whether or not the AI runs. It also runs in the constructor, in `recreateFromPacket` and after NBT load. So a `NoAI` exhibit dinosaur can be hit, and parts never sit at world 0, 0, 0 (review M5, [MC-252478](https://mojira.dev/MC-252478)).

The function stacks four layers:

| Layer | In-game effect | Input | Source |
|---|---|---|---|
| 1. Rest pose | Parts sit on the model at rest | The geo file, posed with bone rotations, times render scale | Layouts page |
| 2. Yaw history | The neck and tail trail the body through turns | A 128-entry ring buffer of `yBodyRot`. The neck reads 10 ticks back, the tail 20 ticks back. | [AC SauropodBaseEntity.java L247-305](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L247-L305) |
| 3. Baked poses | Parts follow the real animations | The bake tables, read at the synced pose state | This design |
| 4. Server IK bend | Neck and tail parts bend away from blocks | The deterministic bend, with the synced bend side | See [Anti-clipping and IK](#anti-clipping-and-ik) |

Hand-tuned angles cover only what the bake cannot cover, such as a head that turns to look at a target. They approach their target at a capped rate, as `approachDegrees` does in Alex's Caves.

**Pose state.** The server owns a synced pose state on `Dinosaur`: an animation id (byte), a start game tick (int) and a loop flag. `DinoAnimator` on the client reads it. So the GeckoLib controllers and the bake lookup share one source. Today, the client picks animations in `DinoAnimator.clientTick`, and the server runs only `DinoOneShots` ([Dinosaur.java](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java), L669-673).

**Lookup rules:**

- The walk and run cycle phase comes from the distance walked. Both sides see that distance within one lerp step. The bake stores the cycle by phase, not by tick.
- The bake samples `query.anim_time` Molang terms at 1-tick resolution.
- `query.life_time` terms bake as the rest pose. Each side counts `life_time` from its own start, so it is not a synced value. The error is about 3° on the Gallimimus idle tail, under 0.2 blocks (review estimate).
- A transition blends linearly over the controller's transition ticks (`MOVEMENT_TRANSITION_TICKS`, `MAIN_TRANSITION_TICKS`), from the previous pose state. Both sides store the previous state.

**Yaw history rules:**

- Store the buffer as a `float[]`, not as one record per tick.
- Record `yBodyRot`, not `getYRot()`.
- Record the same way on both sides.
- Refill the buffer after a teleport or a large position jump (dragon note, C2).

**Accepted differences.** The client lerps its position, so the client parts trail the server parts by the lerp error. A side that has not loaded a part's chunk keeps the last pose. After a load, the tail starts straight and bends again within a few ticks (review L3, accepted). One full placement runs before the first render.

### Identity and lookup

**In game:** a sword hit on any part counts, on NeoForge and on Fabric. The game never sends a hit to the wrong animal.

| Rule | Detail |
|---|---|
| Aligned IDs | Part `i` gets ID `parentId + i + 1`, set in a `setId` override in common code. Vanilla uses this rule for the dragon since 24w44a (1.21.4), per a NeoForge maintainer statement and mirrors. The exact diff is not public. |
| Contiguous block | The constructor reserves one ID block: `setId(ENTITY_COUNTER.getAndAdd(n + 1) + 1)`. No other entity takes an ID between the dinosaur and its parts. |
| Stable objects | Create parts once. Keep the same objects and IDs through growth and part-count changes. |
| Tracking order | Set part IDs before the dinosaur starts tracking. NeoForge registers parts by their current ID at tracking start. |
| No relay | No custom client-to-server hit packet. Vanilla resolves the part ID with `getEntityOrPart` and tests reach against the part's server box with a 3-block buffer. |
| Pick order | Crosshair and projectile picking keep the nearest box along the ray, parts included |
| Level-wide list | The game finds parts from a level-wide part list, so a long body that spans chunk sections stays hittable. Large single boxes still miss there ([MC-261638](https://mojira.dev/MC-261638), open). |
| Culling | The culling box covers all parts. The placement function caches the union once per tick. |

Sources: [V262 EnderDragon.java L851-859](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L851-L859), [V262 ProjectileUtil.java L109-182](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/projectile/ProjectileUtil.java#L109-L182), [V262 ServerGamePacketListenerImpl.java L1785-1836](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/server/network/ServerGamePacketListenerImpl.java#L1785-L1836) (third-party mirror), [IAFCE MultipartPartEntity.java](https://github.com/IAFEnvoy/IceAndFire-CE/blob/63a3bba7960ad991c67fe1d1f4ea1c32e6523741/src/main/java/com/iafenvoy/iceandfire/entity/MultipartPartEntity.java), [Twilight Forest #2579](https://github.com/TeamTwilight/twilightforest/issues/2579) (culling bug). The per-loader mechanics are in [Cross-loader implementation](#cross-loader-implementation-neoforge-and-fabric-262).

### Damage

**In game:** a head hit hurts more than a tail hit. A tail in lava does not burn the dinosaur. A creeper, a sweep or a splash potion near any part reaches the dinosaur, once.

**Multipliers.** The part forwards the same `DamageSource`, times the section multiplier, so the attacker keeps credit.

| Section | Multiplier |
|---|---|
| Head | ×1.5 |
| Neck | ×1.25 |
| Torso | ×1.0 |
| Legs | ×1.0 |
| Tail | ×0.5 |

**Environmental filter.** Part `hurtServer` drops these damage types: fall, drown, in-wall, falling block, lava and fire. The dinosaur's own movement box takes them as usual. Source: [IAFCE MultipartPartEntity.java](https://github.com/IAFEnvoy/IceAndFire-CE/blob/63a3bba7960ad991c67fe1d1f4ea1c32e6523741/src/main/java/com/iafenvoy/iceandfire/entity/MultipartPartEntity.java).

**Area damage:**

| Query | In-game effect | Rule |
|---|---|---|
| Untyped (explosions) | A creeper next to the tail hurts the dinosaur | The query finds the part. The part forwards the damage. |
| Typed `LivingEntity` (sweeps, splash potions) | A sweep or potion on the tail reaches the dinosaur | When the query touches a part, it returns the dinosaur once. An identity set stops a second count. |
| Range test | A sweep at a Brachiosaurus tail 11 blocks behind the torso still counts | The test measures to the nearest part, not to the torso |

Vanilla sweeps test 3 blocks and potions test 4 blocks from the target's centre (review M3, inference from vanilla code). Both loaders need a hook for the typed query and the range test.

**Several parts in one hit.** While `invulnerableTime` is above 10, a new hit applies only the amount above the last hit. So an explosion across several parts counts once, at the highest amount ([V262 LivingEntity.java L1194-1208](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/LivingEntity.java#L1194-L1208), third-party mirror).

**Projectile effects (review L1).** The part forwards fire ticks, so a flaming arrow on the neck sets the dinosaur on fire. `AbstractArrow.onHitEntity` applies effects only to a `LivingEntity`. Tipped-arrow effects and Punch on a part therefore stay lost, unless the team adds a mixin on `onHitEntity` (inference, not checked on 26.2).

### Part buckets and part counts

**In game:** the server keeps its tick time with many dinosaurs loaded. Zombies, items and arrows far from any herd pay nothing extra.

**Why.** `noCollision` calls `getEntityCollisions`, which calls `getEntities`. On NeoForge 26.2 and vanilla 26.2, the untyped `getEntities` loops over every part in the level on each call. Every moving entity calls it once per tick through `Entity.move`. So the cost is parts × queries ([NF262 Level.java.patch L133-187](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/level/Level.java.patch#L133-L187), review C1).

| Measure | Rule | Effect |
|---|---|---|
| Chunk-section buckets | A per-level map from chunk-section key to the parts inside it. The placement function updates it when a part crosses a section. A query reads only the sections that its box covers. | Under 24 parts per query (estimate) |
| NeoForge hook | A mixin redirects the `dragonParts()` call inside the three `Level` methods to the bucketed view | Accepted by the lead |
| Fabric hook | The mod's own `getEntities` inject reads the buckets | — |
| Own parts only | Inject only this mod's parts. Re-test the NeoForge hook on each NeoForge release. | Other multipart mods keep working (review L2) |
| Distance-based part count | Full layout within 64 blocks of a player. Past that, 4 parts: head, torso, torso, tail. Only sizes and positions change. The objects and IDs stay. | Fewer parts in far scans |
| Direct block reads | Herd and IK code read block collision shapes directly, never through `noCollision`. Entity collisions matter to the guard only for boats and shulkers, so the guard drops them. | No part scan per block test |

### Growth

**In game:** a hatchling grows smoothly into an adult. Its box, parts and model match at every age. A growing dinosaur inside a fence lane does not grow into the fence.

| Rule | Detail |
|---|---|
| One scale source | On 26.2, growth drives the vanilla `scale` attribute. Vanilla then scales the box, the eye height and the attachments. Parts and renderer read the same value. |
| No double scaling | Check that GeckoLib 5 does not scale the model a second time |
| Full `getDimensions` override | Species torso-and-legs size × the shared scale. Vanilla baby scaling never applies. |
| Part refresh | After `readAdditionalSaveData`, in `onSyncedDataUpdated`, and on each scale change. The part objects stay. |
| Refresh step | Refresh the box when the scale moves by 0.05 (review value, tune in playtest) |
| `noPhysics` on parts | A growing part does not collide. `Entity.refreshDimensions` skips its fix-up move for `noPhysics`. |
| Growth guard | Vanilla moves a growing entity out of blocks only when the new width and height are both 4.0 blocks or less. Fossils removes that limit and keeps the old dimensions when no free position exists. Call it with small growth steps. |

Sources: [FA Prehistoric.java L419-449](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/Prehistoric.java#L419-L449). Vanilla 1.20.1 `Entity.refreshDimensions`, L2843-2861 in the toolchain sources.

**Code gap today.** Growth has four stages in code, with render factors 0.25, 0.5, 0.75 and 1. `getGrowthStage()` returns 4 when no stage flag exists, so a part built in the constructor reads adult size before NBT loads ([Dinosaur.java](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java), L684-703 and L981-992). `Dinosaur` does not override `getDimensions`. Code reading suggests that a juvenile keeps a half-size vanilla baby box until adulthood (inference). The `ClearanceBox` growth formula also differs from the renderer scale: 2.75 against 2.6 for Brachiosaurus at gene +10 %. The design replaces all of these with the one `scale` value.

### The bake step

**In game:** an animator changes a tail keyframe and rebuilds. Arrows then hit the new tail position. A client with different tables cannot join a server.

| Part of the step | Detail |
|---|---|
| Build | A Gradle step reads the `.animation.json` and geo files and writes the tables |
| Sampling | Bedrock keyframe sampling, Molang terms and forward kinematics with the Bedrock pivot rule. The Gallimimus neck chains nine bones deep. |
| Molang | Every species except Dilophosaurus uses Molang in keyframes, for example `math.sin(query.anim_time*180/0.5)*-5` |
| Format | One flat `float[]` per animation, indexed by tick (or phase) and section. No maps, no boxing. |
| Location | Inside the mod jar. Both sides load the tables directly. No data pack and no resource pack can override them. |
| Hash check | A hash of the tables goes into the mod's network version check. A mismatched client cannot join. |
| Staleness | The build fails when an animation file is newer than its bake |
| Golden test | A dev run records bone world positions with GeckoLib 5 `addBonePositionListener`. A fixture stores them. The test compares the bake against the fixture within 0.1 blocks. |
| T. rex source | Bake the original T. rex animation file. The CHAOS branch removed the leg and tail tracks from its copy. |

### What stays uncovered

**In game:** a few hits pass through on purpose, and a few hit parts sit slightly off the model.

| Gap | In-game effect | Why |
|---|---|---|
| Arms | A hit on a Velociraptor arm passes through. T. rex and Dilophosaurus arms sit mostly inside the torso parts. | Decided. No arm parts. |
| Dilophosaurus frill | A hit on the spread frill passes through | Decided. No frill part. |
| Coarse envelope | During a roar, parts cover the sections only approximately | Square parts on a chain. No client refinement (decided). |
| Leg overhang | Gallimimus leg parts extend 0.11 blocks past the movement box | Accepted (review L4) |
| Tipped arrows and Punch | No effect and no knockback from a part hit | Vanilla applies them to a `LivingEntity` only (review L1) |
| Lerp gap | Client and server parts differ by the lerp error, under 0.5 blocks for Gallimimus and under 2 blocks for Brachiosaurus | Accepted (review H1 estimate) |
| Unloaded chunk | A part in a chunk that a side has not loaded keeps its last pose | Accepted (review H1) |
| Far animals | Past 64 blocks from a player, 4 parts cover the body | Part-count optimization |

## Movement

**In game:** herds of up to 20 animals cross open ground, pass trees and file through gaps and corridors. They do not cluster, circle or stand stuck.

### Movement box and clearance split

**In game:** a dinosaur fits through any gap that its torso and legs fit. Its neck and tail do not stop it. A Brachiosaurus keeps room for its whole body next to herd mates.

The movement box is the field standard. Alex's Caves sauropods move on 5 × 8 and 6 × 8.5 boxes. Their necks and tails are non-colliding parts, and they are immune to `IN_WALL` damage ([AC SauropodBaseEntity.java L624-626](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L624-L626)). A 1.21.1 modder reports that a 3-block box "is too big for the vanilla pathfinding" ([Forge forum 159517](https://forums.minecraftforge.net/topic/159517-bad-pathfinding-for-big-mobs-1211/)).

**Clearance split by job:**

| Job | Size used | Default |
|---|---|---|
| Paths and gaps | Scaled torso-and-legs width (the movement box) | Yes |
| Herd spacing | Whole scaled model (the capsule) | Yes |
| Per-species change | A config value can change either choice | — |

Today's herd `ClearanceBox` takes the larger of the hitbox and the model width. For Brachiosaurus and Triceratops, that is about 3.5 blocks, against a 2 × 3 hitbox. It refreshes every 100 ticks (5 s) ([ClearanceBox.java](../.claude/worktrees/herd-leader-sample/common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/ClearanceBox.java)). The split replaces that single box.

A box narrower than 1.0 plans with a one-column footprint. That gives the Gallimimus more slack in 2-wide gaps (inference).

### Arrival fix

**In game:** a wide dinosaur reaches a path node and walks to the next one. It does not circle the node or spin on the spot.

**Mechanics (1.20.1, toolchain sources, checked 2026-10-10):**

- `Path.getEntityPosAtNode` aims at `node + floor(bbWidth + 1) × 0.5`. Vanilla already centres the aim point on the footprint.
- `PathNavigation.followThePath` sets the tolerance to `bbWidth / 2` for widths above 0.75. It advances only when the dinosaur is closer than that to `node + 0.5` on X and on Z, with a strict `<`. Y must be within 1 block.
- The aim point sits `floor(w) / 2` from `node + 0.5`. So the arrival margin is `(w − floor(w)) / 2`.
- At w = 2.1, the margin is 0.05 blocks. At every whole-number width, the margin is zero. A dinosaur on its aim point then fails the test on the axis across its travel. Only corner cutting can advance the path.

This fits the "spinning on the spot" reports across big-mob mods. That link is an inference from code.

**Adult margins with the new boxes (vanilla test, inference from the formula):**

| Species | Width | Margin |
|---|---|---|
| Gallimimus | 0.80 | 0.40 |
| T. rex | 1.56 | 0.28 |
| Triceratops | 2.19 | 0.095 |
| Brachiosaurus | 3.68 | 0.34 |
| Velociraptor | 0.67 | 0.42 (vanilla uses `0.75 − w/2` at or below 0.75) |
| Dilophosaurus | 0.56 | 0.47 |

Smooth growth passes through every whole-number width below the adult width. A herd holds many widths at once. So the fix matters even where an adult margin looks safe.

**The fix.** Override path following in `FenceAwareNavigation`. Test arrival at the aim point, as Mowzie's `isAt` does: tolerance 0.5, or `w/2` at elevation changes ([MM MMPathNavigateGround.java L30-140](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathNavigateGround.java#L30-L140)). Fossils uses 0.5, or 0.75·w at elevation changes ([FA NavUtil.java L144-149](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/NavUtil.java#L144-L149)). Also count a node as reached when the dinosaur is within tolerance and faces the next node. Otherwise a turning dinosaur that overshoots turns again, and the spin loop starts.

Other proven fixes exist. Alex's Mobs and Alex's Caves raise the tolerance to 0.75·w. Cataclysm and Naturalist add a "passed the node" dot-product test. Citadel centres the footprint on the node.

`FenceAwareNavigation` overrides only `createPathFinder` today. It has no arrival, shortcut or stuck override ([FenceAwareNavigation.java](../common/src/main/java/net/dumbcode/projectnublar/entity/ai/FenceAwareNavigation.java)).

### Sweep shortcut

**In game:** on open ground, the dinosaur walks a straight line to a later node. It does not cut a corner into a trunk.

The navigation sweeps the movement box toward later nodes on the same Y level, farthest first. It takes the first clear node. The sweep rejects water, lava, open floor and damaging blocks. It reads blocks through the block filter. Sources: [FA PrehistoricPathNavigation.java L48-97](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/PrehistoricPathNavigation.java#L48-L97), [AzureNavigation 101](https://moddedmc.wiki/en/project/azurelib/latest/docs/misc/azurenavigation_101).

- Run the sweep every 5 ticks, or when the next node changes. Cache the result per node index.
- The sweep removes most staircase micro-turns. So the hard turn gate fires mainly at real corners.

**Warning case.** In Cataclysm, a comment disables the whole sweep body. Its shortcut always jumps to the farthest same-Y node ([CMPathNavigateGround L67-144](https://github.com/lender544/new1.20.1/blob/819937203e22b4fa72fdeccc6c56ad415d65eeef/src/main/java/com/github/L_Ender/cataclysm/entity/etc/path/CMPathNavigateGround.java#L67-L144)). A shortcut with no width sweep lets a wide mob cut corners into walls.

### Footprint

**In game:** the pathfinder sends a dinosaur only into gaps that its box can enter. It does not refuse gaps that the box fits.

| Footprint rule | Effect | Source |
|---|---|---|
| Vanilla `floor(w + 1)`, anchored toward +X and +Z | Over-sizes whole widths: w = 2.0 needs a 3-wide gap | Toolchain `NodeEvaluator.prepare` L27-29. Third-party mirror: [V NodeEvaluator.java L27-29](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/level/pathfinder/NodeEvaluator.java#L27-L29) |
| Citadel `2·floor(w/2) + 1`, centred | Under-sizes 1 < w < 2 as 1 block. A juvenile gets sent into a gap that its box cannot enter. | [CIT AbstractPathJob.java L458-476](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/pathjobs/AbstractPathJob.java#L458-L476) |
| **Design: symmetric, ceil-based, centred** | Avoids both errors and the +X/+Z skew of [MC-169105](https://bugs.mojang.com/browse/MC-169105) (community report) | Inference, after Harabor's "true clearance" ([Harabor 2009](https://users.cecs.anu.edu.au/~dharabor/data/papers/harabor-aigamedev09.pdf)) |

True clearance (the largest free square at a tile) finds a path for any agent size if one exists. An on-demand test costs about w² block reads per node and layer (estimate). On 26.2, `WalkNodeEvaluator` uses `PathType` and `PathfindingContext`, so `FenceAwareNodeEvaluator` needs a rewrite.

### Turn gates

**In game:** in a tight space, the dinosaur stops, turns to face its next node, then walks. In open ground, it walks a curve and slows while it turns.

| Gate | When | Rule | Source |
|---|---|---|---|
| Hard gate | `tightGround` is true | Forward speed stays 0 until the yaw error is small. Alex's Caves walks only within 15°. | [AC SauropodBaseEntity.java L645-686](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L645-L686) |
| Soft gate | Open ground, and followers in open ground | Turn rate `clamp(90 − 35·w, 5, 90)` °/tick. Speed falls to as little as 0.1× in proportion to the remaining turn. | [FA SmoothTurningMoveControl.java L19-75](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/control/SmoothTurningMoveControl.java#L19-L75) |
| Anti-orbit guard | Always | Skip turning when the horizontal distance² to the target is 0.12 or less (about 0.35 blocks) | Same Fossils file |

**Rules:**

- Gate forward speed in `HerdMoveControl.tick`, after `super.tick()`. The split by ground type is an inference.
- Do not count turning ticks as stuck ticks.
- Never cancel a rotation in `setYRot`. CustomHitboxLib does this, and its own FAQ warns that it confuses the pathfinder ([CHL FAQ](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/wiki/FAQ.md)).
- A follower that stops to turn loses its place in its slot. Add hysteresis to slot assignment, or use the soft gate for followers in open ground.

**Today.** `Dinosaur.setYRot` clamps every yaw change to 10°/tick (200°/s). Vanilla `MoveControl` sets forward speed in the same tick that it turns. The `HerdMoveControl` guard sets speed 0 when the box 0.6 blocks ahead does not fit. At journey speed, a 90° turn covers 0.77 blocks of arc that no probe tests. The guard fires on that arc at trunks ([HerdMoveControl.java](../.claude/worktrees/herd-leader-sample/common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdMoveControl.java)).

**Limit.** Turning in place removes the forward chord, not the tail sweep. An adult Gallimimus that turns about its centre sweeps a radius of about 2.95 blocks. An adult Brachiosaurus sweeps about 11.7 blocks with its tail. The corner test covers that sweep.

### Corner test and the follower danger term

**In game:** the herd leader prefers routes where its full length can turn at each corner. Where no such route exists, it still goes, and its neck or tail bends or overlaps blocks. Followers also prefer headings where their head and tail tip stay clear.

**Corner test (leader, in the planner):**

- Find path nodes where the heading changes by more than 45°. Test only species whose spine reach exceeds 2 blocks.
- At each such node, test a disc of radius `max(nose reach, tail reach)` around the pivot. A capsule swept over the turn is the alternative.
- Sample the spine with direct block reads through the block filter. Do not use `noCollision`.
- Add the result as a path cost or as a "turn in place here" flag. Never use it as a veto in `MoveControl` or `setYRot`.
- If no route passes, accept the clipping. The neck and tail bend handles what it can.
- For sauropod lengths only, try a small local search in a 16 × 16 block window with 8 headings. A three-point reverse turn is the alternative.

Place the test in `FenceAwareNodeEvaluator` neighbour generation, or in a post-pass over the `Path` in `FenceAwareNavigation.createPath`. Read blocks through `BodyFit`, the one herd block reader in the [herd-movement redesign spec](../.claude/worktrees/herd-leader-sample/docs/superpowers/specs/2026-10-08-herd-movement-redesign.md). The cost is O(r²) column reads per sharp node. Few nodes are sharp, so this is cheap next to A* (estimate).

**Follower spine danger term.** Followers use context steering, not the planner. `HerdMoveControl.evaluate` probes only the clearance box at 1, 2 and 3 blocks ahead ([HerdMoveControl.java](../.claude/worktrees/herd-leader-sample/common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdMoveControl.java), L742-828). So the design adds one danger term:

1. Take the previous heading and the 4 deep-probed headings.
2. For each heading, compute the head cell and the tail-tip cell after a turn to that heading.
3. Read the two cells through the block filter.
4. A solid cell adds `DANGER_BLOCKED_FAR` to that heading.

That is about 10 cell reads per evaluation. The follower then prefers headings where its ends stay clear, and the bend handles the rest.

### Capsule spacing and single file

**In game:** herd mates keep a gap from snout to tail. Side by side, they stand closer than today. In a corridor, 20 Gallimimus file through one after another.

**Why circles fail.** Today, the herd spaces mates by centre distance, `(w1 + w2)/2 + 1.0`. That is 2.2 blocks for two Gallimimus. In single file, two posed models at 2.2 blocks overlap by about 2.95 blocks. With a 1.0 gap, capsules space them about 6.15 blocks apart in line and 1.8 blocks side by side. This is arithmetic on the posed sizes. The single-file decision then shortens the in-line gap. Robotics research shows the same failure. An inflated circle closes narrow passages that a capsule fits ([EPFL RDS](https://infoscience.epfl.ch/record/284026/files/09385856.pdf)).

**Capsule rules:**

| Rule | Detail |
|---|---|
| Asymmetric spine | Each capsule runs from the snout tip to the tail tip, offset as the spine table gives. A Brachiosaurus reaches 5.31 blocks ahead of its origin and 11.66 behind. |
| Size | The whole scaled model (clearance split) |
| One choke point | The capsule test replaces `HerdSpacing.hardMinimum` and `HerdBodies.minimum`. `HerdBodies` stores each member's yaw and spine. |
| Broad phase | Per evaluation, one circle pass finds members within the reach sum plus 10 ticks of travel. Capsule tests run only on those. About 60 capsule tests per evaluation (estimate), against 228 per evaluation (2,280 per herd tick) with today's masks. |
| Slot reassignment | When the formation turns, give each follower the nearest free slot. Fixed slot IDs make followers cross through each other ([Pottinger 1999](https://gamedeveloper.com/programming/implementing-coordinated-movement)). |
| Rank priority | A lower-rank animal yields to a higher-rank capsule ahead. Entity ID breaks ties. Inference from Pottinger and the ORCA deadlock research. |
| Valid slot targets | Test each slot target against path clearance before a follower walks to it. A slot inside a trunk causes a tree snag. |
| Herd jam recovery | If over a quarter of the members fail to path in one tick, reverse the herd heading ([Jurassic Reborn Herd.java L196-206](https://github.com/Interactive-Modding/Jurassic-Reborn/blob/4a9a21b6ad7cea718534abd31913cccca2a6d98e/src/main/java/mod/reborn/server/entity/ai/Herd.java#L196-L206)) |
| No pushing | Do not rely on vanilla pushing. Fossils ignores pushes on bodies 3 blocks or larger. Separation comes from steering and slots. |
| No ORCA by default | ORCA and RVO2 assume discs. Use them only if capsule separation oscillates head-on ([RRVO](https://parasollab.web.illinois.edu/research/rrvo/)). |
| New tuning | Slot spacing, herd radius, graze spacing and the tight-ground threshold all need new values. `HerdSpacingTest` (22 tests) encodes circle results and needs a rewrite. |

**Single-file rules:**

- The herd switches from the wedge to single file when the path narrows. No source makes this switch, so it is new (inference).
- In single file, the gap runs from the follower's snout to the tail tip ahead.
- A follower's neck may overlap the tail ahead.
- The capsule still governs side-by-side spacing.

**In-line pitch (lead decision, 2026-10-10):**

| Situation | Pitch rule | Gallimimus (adult) |
|---|---|---|
| Default (rows and lanes in line) | Chest rule: tail reach of the animal ahead plus chest reach of the follower, both scaled by growth. Bodies never overlap. Only the neck and head reach over the tail ahead. | 2.95 + 1.24 = 4.19 blocks |
| Single file in a corridor | Today's tight spacing: personal space plus 1.0 (`TravelOrder.tightSpacing`) | 3.2 blocks with today's 1.2 box, 2.8 with the 0.80 box |
| Big herd | Today's tight spacing when the chest-rule column would outgrow the base 128-block trail | As above |

All pitch values are per-species config values, tuned in playtest.

**Chest-rule pitch per species (adult, posed, inference from the spine table):**

| Species | Herd size | Pitch | Column at the chest rule |
|---|---|---|---|
| Gallimimus | 20 | 4.19 | 80 blocks |
| T. rex | 5 | 7.88 | 32 blocks |
| Triceratops | 5 | 8.86 | 35 blocks |
| Brachiosaurus | 5 | 16.41 | 66 blocks |
| Velociraptor | 5 | 3.81 | 15 blocks |
| Dilophosaurus | 5 | 3.48 | 14 blocks |

In a corridor the Gallimimus column is shorter. Today it is 19 × 3.2 = 61 blocks. With the 0.80 box it is 19 × 2.8 = 53 blocks.

**Trail guard (lead decision, 2026-10-10):**

1. Trail capacity is the herd's single-file column length plus 16 blocks, never below 128. The column length comes from `group_size` and the pitch.
2. A follower whose slot falls behind the trail tail follows the animal ahead of it, not the trail.

The leader never stalls for a long column. Its gather rule is relative to each slot (slot distance plus 5 blocks). Followers past 48 blocks do not count. Since P13, the straggler wait is a slow walk of at most 200 ticks (10 s), never a stop.

Precedents for single file: Alex's Mobs elephants follow with a 4-block gap ([AM ElephantAIFollowCaravan.java](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/ElephantAIFollowCaravan.java#L21-L140)). Alex's Caves Underzealots keep a 1-block gap ([AC UnderzealotProcessionGoal.java](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/UnderzealotProcessionGoal.java#L42-L54)).

### Stuck ladder

**In game:** a stuck dinosaur tries cheap recoveries first. If it stays stuck for a long time, it snaps to a nearby clear spot, also when a player watches.

**Detector.** Count a dinosaur as stuck only when its position change is small and its commanded speed is near zero. Then apply a cooldown ([arXiv 2605.01783](https://arxiv.org/pdf/2605.01783)). Turning ticks do not count.

**Ladder, cheapest first (inference):**

1. Skip to the next path node or corner.
2. Repath with a cost on the blocking node, so the new path differs.
3. Reverse about half a body length along recent positions. Then turn in place.
4. Followers only: leave the slot and path along the leader's trail.
5. Snap to the nearest position with valid path clearance within a few blocks. Use a cooldown. The lead allows this rung also when a player is near.

Repaths stay inside the spec's budget: 2 fallback paths per herd per tick and a 60-tick cooldown per member.

**Evidence.** Alex's Caves dinosaurs teleported through the Citadel stuck handler and suffocated in walls, "more prevalent in bigger mobs" ([AlexsCaves #852](https://github.com/AlexModGuy/AlexsCaves/issues/852)). So the snap must test clearance of the movement box before it moves the dinosaur. The no-teleport Citadel handler waits 1200 ticks (60 s) before its first action. A Roblox developer teleported a stuck AI to its start, and it took the same failing path again. So the repath rung must change the path.

**Today.** The guard sets speed 0, so vanilla stuck detection never fires on a held path. `holdPath` stops a herd fallback path after 20 held ticks (1 s). The wait limit is 160 ticks (8 s).

### Per-species block filter

**In game:** a Gallimimus walks through leaves and keeps walking. It does not walk into a canopy and then freeze there.

One per-species `BlockFilter` answers pass, break or solid for each block state. Every block reader asks it. If one path disagrees, the herd sticks on trees again. Collision lets the animal into leaves. The guard then sees the leaves and sets speed 0 (review H4).

| # | Code path | Use |
|---|---|---|
| 1 | `Entity.collide` | One mixin on both loaders |
| 2 | `BodyFit` | Fit, stand, ground and side-room tests ([BodyFit.java](../.claude/worktrees/herd-leader-sample/common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/BodyFit.java)) |
| 3 | Node evaluator | Block typing for paths |
| 4 | Sweep shortcut | The box sweep |
| 5 | IK bend | The neck and tail bend test |

Build the filter before the movement box work. One GameTest under a 3-high canopy tests all five paths. Today, `BodyFit` reads blocks through `noCollision`, and its Javadoc says that leaves "count as they are".

### Trees, leaves and logs

**In game:** leaves never stop a dinosaur. Small species pass through leaves. Large species break leaves with the torso, while the head and neck pass through the canopy. Trunks stop calm animals. A charging large dinosaur breaks trunks.

| Rule | Detail |
|---|---|
| Leaf mode | Per-species config: pass, break or solid. Tune in playtest. |
| Default | Brachiosaurus, Triceratops and adult T. rex break leaves. Gallimimus, Velociraptor and Dilophosaurus pass through. |
| What breaks | Only the movement box, and only above a per-species growth cut-off. Head and neck parts never break blocks. |
| Logs | Large species break logs only when charging. Per-species config. |
| Breakable set | A mod tag lists the breakable blocks. Player-placed leaves break too. |
| `mobGriefing` off | A leaf-breaking species passes through leaves. Logs stay solid for every species (lead decision, 2026-10-10). Players design their parks with that in mind. |
| Trigger | Breaking runs only on `horizontalCollision`, as in Fossils. A 128-cell scan runs on contact. |

One predicate decides both "break" and "pass through". On NeoForge, it asks `CommonHooks.canEntityDestroy`, as the dragon's `checkWalls` does. That hook can allow breaking with `mobGriefing` off. On Fabric, the predicate reads the `mobGriefing` game rule (inference from the dragon note). Precedents: Citadel passable leaves for the Alex's Mobs tiger ([CIT ICustomCollisions.java](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/collision/ICustomCollisions.java#L22-L48)), and tagged block breaking for the Alex's Caves Luxtructosaurus ([AC LuxtructosaurusEntity.java L394-410](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/LuxtructosaurusEntity.java#L394-L410)). Horizon Zero Dawn lets its largest machines smash trees only when angry or chasing ([Game Developer, HZD Part 2](https://www.gamedeveloper.com/design/behind-the-ai-of-horizon-zero-dawn-part-2-)).

### Brachiosaurus in open ground

**In game:** an adult Brachiosaurus walks open plains and clearings. It does not enter forests. It needs gaps about 4 blocks wide.

- The movement box is 3.68 × 8.87 blocks. Every trunk stops it, because logs hold for calm animals.
- Its node volume is `floor(w + 1)` × `floor(h + 1)` × `floor(w + 1)`. That is 4 × 9 × 4 = 144 cell reads per node (inference from the vanilla formula).
- One path costs about 5 ms (review estimate). So a herd plans at most one large-species path per tick.
- Playtests measure the Brachiosaurus in open ground only.

### Tuning values

| Value | Today | Source values |
|---|---|---|
| Turn rate | 10°/tick (200°/s), all yaw changes | Alex's Caves sauropod body: 2°/tick (40°/s). Fossils: `clamp(90 − 35·w, 5, 90)` °/tick. |
| Turn gate | None | Alex's Caves: 15°. Cataclysm Leviathan: 4°. |
| Anti-orbit distance² | None | Fossils: 0.12 |
| Arrival tolerance | Vanilla `w/2` from `node + 0.5` | Fossils: 0.5 from the aim point. Alex's Mobs and Alex's Caves: 0.75·w. |
| Stuck window | Path hold exit after 20 ticks (1 s). Wait limit 160 ticks (8 s). | Vanilla: 100 ticks (5 s). Citadel: first action after 1200 ticks (60 s). |
| Herd gap | 1.0 block (0.5 when resting or in tight ground) | Alex's Mobs caravan: 4 blocks. Underzealots: 1 block. |
| Growth fix-up limit | Vanilla, 4.0 blocks | Fossils: no limit, keep the old box if blocked |
| `ClearanceBox` refresh | 100 ticks (5 s) | Replaced by the scale-change refresh |

## Anti-clipping and IK

**In game:** necks and tails do not sink into trunks, walls or stone. Where a small overlap remains, the hit parts and the visible model still agree. Feet stand on steps and slopes.

### Plan-around

The planner avoids spots where the body clips. Two pieces do this:

- the leader's corner test, as a soft path cost
- the followers' spine danger term in context steering

Both are in [Movement](#corner-test-and-the-follower-danger-term). They reduce how often the bend must work.

### Server neck and tail bend

**In game:** a Brachiosaurus walks past a trunk. Its tail bends away from the trunk on the server and on every client, to the same side. An arrow at the visible tail hits.

| Rule | Detail |
|---|---|
| Where | The server tick, with deterministic code. The client runs the same code. |
| Base | The placed spine from layers 1–3 (rest pose, yaw history, baked pose), not rendered bones |
| Method | Per joint, rotate until clear, within joint limits. Test block collision shapes on the cells that each part covers. |
| Block reads | Direct reads through one cached chunk-section view, through the block filter. Never `noCollision`. |
| Bend side | The server picks the side per chain (neck, tail) and syncs one byte, with hysteresis. The client runs the magnitude search with that side fixed. |
| Unloaded chunks | Skip parts in non-ticking chunks on the server. A side that has not loaded a part's chunk keeps the last pose. |
| Step cap | At most 6 bend steps per chain per tick |
| Early out | Skip the search when the part cells and their neighbours are air. Most open-ground ticks cost about 8 reads (estimate). |
| Cadence | Every tick within 32 blocks of a player. Every 4 ticks past that, with the last pose held. |

**Why a synced side.** The bend search takes the first clear direction. Near a symmetric case, a 0.1-block input difference flips the choice. The inputs differ per side: lerped position, local `yBodyRot` and loaded chunks. Without the byte, the visible tail and the server tail can be up to 20 blocks apart (review H1, inference). With it, the two sides differ by the lerp error only.

**Networking.** The pose state and the bend sides cost about 6 bytes per animal, sent on change only.

### Client leg IK

**In game:** feet sit on uneven ground and steps. Leg IK is visual only. Leg parts do not move with it.

| Rule | Detail |
|---|---|
| Side | Client only |
| Rate | Once per tick, lerped per frame |
| Detail by distance | Full IK within 16 blocks of the player. None past 32 blocks. |
| Raycasts | Pass `CollisionContext.empty()`, never an entity. No `Arrow` allocation per raycast. |

The frame rate matters here. With 20 Gallimimus on screen, colour layers already cost about 80 % of the frame, at about 26 fps ([herd-movement handoff](../.claude/worktrees/herd-leader-sample/docs/superpowers/handoffs/2026-10-09-herd-movement-handoff.md)).

### GeckoLib 5 delta updater

**In game:** the visible tail follows the bent spine. It does not sway twice, and it does not step 20 times a second.

- A GeckoLib 5 `BoneUpdater` adds only the delta between the bent spine and the unbent spine.
- It lerps the delta by partial tick.
- It applies the delta on top of the animation snapshot. `RenderPassInfo.create` registers `applyAnimationControllers` as the first updater ([GL262 RenderPassInfo.java](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/renderer/base/RenderPassInfo.java)).
- The renderer copies the delta into the render state in `addRenderData`, because 26.2 renderers work from an extracted render state.

An absolute bone write would fight the controller. A per-tick delta with no partial tick would repeat the CHAOS jitter. Nobody tested whether GeckoLib 5 bone updaters fight the animation controller. That is a test item.

### CHAOS branch code status

The CHAOS branch IK code is part of the mod. The team uses it together with its sources: Mowzie's Mobs, the Cymaera `minecraft-spider` repo and others that apply. Credit is given as the Mowzie's Mobs terms require. The lead handles licence questions. The audit is [chaos_ik_audit.md](../.claude/worktrees/herd-leader-sample/research_notes/CHAOS%20branch%20IK%20audit/chaos_ik_audit.md).

| Item | Fact from the audit | Effect on this design |
|---|---|---|
| Branch | `ShadowTekStudio/Project-Nublar-Behaviour` `1.20.1-CHAOS`, head `483c598` (2025-06-08), by HerrChaos. GeckoLib 4.4.4. | — |
| Size | 30 files, 2,698 lines in `entity/ik` | — |
| Where chains solve | Every chain solves in `tickClient`, each render frame. `IKTailComponent.tickServer` is empty. | Neck and tail need a new deterministic solve path on the server tick |
| Server step planner | `ServerLimb` runs on both sides. Nobody reads its server results. | Leg IK moves to the client only |
| Sync | No packet, no synced data, no NBT for IK state | The bend-side byte is new |
| Reusable core | `IKChain`, `AngleConstraintIKChain` and `MathUtil` use no client classes | They can run on the server if their bases come from the spine |
| World collision | `WorldCollidingSegment` stops joints, tests joint points only and depends on frame history | Replace it with the deterministic per-joint test |
| Bone writes | `MowzieGeoBone` forced world matrices, through `MowzieModelFactory` | GeckoLib 5 removes the bone matrices and the model factory. The delta updater replaces them. |
| Animation file | The branch removed the leg and tail tracks from the T. rex animation | Bake the original file |
| Jitter | Mixed interpolation, a yaw mismatch, one-frame lag and tick-rate foot targets | The delta updater lerps by partial tick and uses one yaw source |
| Defaults | `ChainCommonClass.isDev = true` makes production builds throw `BoneNotFoundException` for a missing bone | Set it to false for releases |
| Scale | Some passes scale segment lengths and some do not | Use the shared render scale everywhere |
| Cost | Per frame, up to about 144 tail raycasts, each with an `Arrow` allocation | Once-per-tick leg IK with `CollisionContext.empty()` |
| Reach chain | `AngleConstraintIKChain` (FABRIK with planar hinge limits) fits a future neck reach chain. It needs fixes for unclamped `acos` and sign flips at full reach. | Waits for neck reach IK |

## Cross-loader implementation (NeoForge and Fabric 26.2)

**In game:** the same dinosaur behaves the same on a NeoForge server and on a Fabric server.

### Dependencies

| Dependency | 26.2 status | Action |
|---|---|---|
| GeckoLib | 5.5.6 on NeoForge, 5.5.5 newest on Fabric. Package `com.geckolib`. Render-state renderers. ([Modrinth](https://api.modrinth.com/v2/project/geckolib/version?game_versions=%5B%2226.2%22%5D)) | Pin one version on both loaders. Remove all renderer writes to synced entity data. |
| SmartBrainLib | 2.0.x on NeoForge, Forge and Fabric ([Modrinth](https://api.modrinth.com/v2/project/smartbrainlib/version?game_versions=[%2226.2%22])) | Port |
| Vanilla pathfinding | `PathType` and `PathfindingContext` replace `BlockPathTypes` | Rewrite `FenceAwareNodeEvaluator`. Keep the evaluator override on both loaders, because Fabric `LandPathTypeRegistry` has no mob parameter. |
| Step height | An attribute since the 1.20.5 era | Change `setMaxUpStep(1.0F)` |
| NeoForge branch | The default branch is now `26.3.x` | Pin `26.2.x` |

Keep all spine, capsule, footprint, corner and bend geometry in pure common classes. `BodyFit` stays the one herd block reader, "so the 26.2 port changes one place" (spec).

### Parts per loader

| Need | NeoForge 26.2 | Fabric 26.2 |
|---|---|---|
| Unspawned part class | Native `PartEntity` ([NF262 PartEntity.java](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/src/main/java/net/neoforged/neoforge/entity/PartEntity.java)) | Own class that extends `Entity` and throws on `getAddEntityPacket` |
| Level-wide part list, filled at tracking start and end | Native ([NF262 ServerLevel.java.patch](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/server/level/ServerLevel.java.patch#L268-L291)) | Mixins on `ServerLevel`, `ClientLevel` and their entity callbacks |
| Parts in `getEntities` and `hasEntities` | Native loop, redirected to the buckets by a mixin | `RETURN` injects on both `getEntities` variants and `hasEntities`, reading the buckets |
| Typed `LivingEntity` queries | Hook: return the dinosaur once when a part touches the box | Same rule in the typed `getEntities` inject |
| Range test to the nearest part | Hook on sweep and potion code | Same hook, common code |
| Attack packets resolve parts | Native `getEntityOrPart` | `TAIL` inject on `ServerLevel.getEntityOrPart` |
| Parts never tracked | Native `ChunkMap.addEntity` skip | Cancel `ChunkMap.addEntity` for parts |
| Item effects and camera go to the dinosaur | Native | Mixins on `Player` and `ServerPlayer` |
| Aligned IDs | Own `setId` override, common code | Same common code, plus an `ENTITY_COUNTER` accessor |

NeoForge's native typed loop uses `tryCast`, so a part fails a `LivingEntity` test. That is why both loaders need the typed hook ([NF262 Level.java.patch L144-187](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/level/Level.java.patch#L144-L187)).

Fabric API has no multipart API, and no issue proposes one. Two public mods show the Fabric mixin set:

- MoreHitboxes' Fabric module, 1.21.1 ([MH21 FabricLevelMixin.java](https://github.com/DarkPred/MoreHitboxes/blob/88899b3e1c4ed881b1ea65b35104453fc6df592a/fabric/src/main/java/com/github/darkpred/morehitboxes/mixin/FabricLevelMixin.java#L22-L61)). Its typed inject already returns the dinosaur, not the part.
- Aquamirae, with about four common mixins for both loaders ([Aquamirae MixinLevel.java](https://github.com/ObscuriaLithium/aquamirae/blob/a468d77cc443a932d56a7aa34303f4e435eca3b6/common/src/main/java/dev/obscuria/aquamirae/integration/mixin/MixinLevel.java)). Its targets on 26.x are not checked.

### Port rules

- Set part IDs before the dinosaur starts tracking. Never swap part objects or IDs while the game tracks the dinosaur.
- Do not copy Biome Makeover. It adds parts only for parents that the section query found, which keeps the MC-261638 miss.
- Do not copy the MoreHitboxes `ServerPlayerMixin` camera redirect as it is. Code reading suggests that a second `setCamera` call can leave the old camera (untested).
- Register GameTests in common code, with a shim per loader.

### Blockers in the worktree today

The [herd-movement handoff](../.claude/worktrees/herd-leader-sample/docs/superpowers/handoffs/2026-10-09-herd-movement-handoff.md) records three blockers:

- The GameTests live in the Forge module.
- The Fabric build fails.
- The mod crashes on a dedicated server.

## Performance budget

**In game:** the server keeps 20 ticks per second with 100 dinosaurs loaded. The client keeps its frame rate with a herd on screen.

All values are per server tick and are estimates, not measurements. Profile each row after it ships.

**Assumptions (review, inference):**

- 15 ns per part in a linear scan, 30 ns per direct block-state read, 3 µs per small `noCollision` without the part scan.
- Load A: 20 Gallimimus in one herd, 1 player, 300 other entities.
- Load B: 100 dinosaurs in five herds, 10 of them large species, 3 players, 600 other entities.
- 12 parts per animal. The real layouts have 11 to 17 parts (Brachiosaurus 17), so the load B figures are slightly low.

### Planned state

| Subsystem | Load A (20) | Load B (100) | Notes |
|---|---|---|---|
| Part placement | 25 µs | 125 µs | `setPos` allocates one AABB per part: 240 and 1,200 allocations per tick |
| Baked pose lookups | 5 µs | 25 µs | Array index per section per tick |
| Server neck and tail bend, direct reads | 0.15 ms | 0.8 ms | 8 parts, 1 µs when clear, 20 µs in a bend, 30 % of animals bending |
| Level-wide part scans, bucketed | 0.2 ms | 0.6 ms | Under 24 parts per query |
| Typed-query hook | 30 µs | 0.4 ms | 15 and 35 typed queries per tick |
| Capsule tests with broad phase | 40 µs | 0.2 ms | About 60 capsule tests per evaluation |
| Corner clearance | 60 µs | 0.5 to 3 ms | 3 sharp nodes per path. 6 samples × 3 headings (Gallimimus), 18 × 3 (Brachiosaurus). 2 and 10 paths per tick. |
| Destination clearance | 0.1 ms | 0.5 ms | One cached `standY` per slot change |
| Sweep shortcut | 30 µs | 0.75 ms | Figure for a sweep every tick. The 5-tick cadence lowers it. |
| Stuck detector | 2 µs | 10 µs | One compare per animal |
| Stuck repaths | up to 4 ms spike | up to 20 ms spike | Vanilla A*: 1 to 2 ms (Gallimimus), about 5 ms (Brachiosaurus). The repath budget caps it. |
| Leaf breaking | 0 | 0.6 ms | Gallimimus pass through. 10 large animals, 128-cell scan on contact, 5 breaks per tick at 100 µs. |
| Networking | 0 B | 0 B | No part packets. Pose state and bend side: about 6 bytes per animal on change. |
| Herd AI today (spec budget) | under 1 ms | about 5 ms | Unchanged by this design |
| **Total server** | **2 to 3 ms** | **9 to 15 ms** | A* spikes on top |

**Client:**

| Subsystem | Load A | Load B | Notes |
|---|---|---|---|
| Leg IK, once per tick with lerp | 1.1 ms | 5.5 ms if all visible | 2 legs, 5 raycasts at 5 µs, 20-iteration solve. Distance detail cuts load B to about 1 ms. |
| Part scans | Same as the server rows | Same | `ClientLevel` ticks every entity and keeps its own part list |
| Bone updaters | 20 µs per frame | 100 µs per frame | One delta per bone |

### Paths the design rejects, for comparison

| Path | Load A | Load B | Replaced by |
|---|---|---|---|
| Level-wide part scans, linear | 2 ms | 36 ms | Chunk-section buckets |
| Server bend through `noCollision` | 0.8 ms | 12 ms | Direct block reads |
| Capsule tests with today's masks | 0.2 to 0.5 ms | 1 to 2.5 ms | Broad phase |
| **Total server with linear scans** | **4 to 8 ms** | **45 to 70 ms** | Over the 50 ms tick at load B |

### Planned optimizations

| # | Optimization | Section |
|---|---|---|
| 1 | Chunk-section part buckets on both loaders. The largest win. | [Part buckets](#part-buckets-and-part-counts) |
| 2 | One `LevelChunkSection` cache per herd per tick for direct block reads | [Server bend](#server-neck-and-tail-bend) |
| 3 | Bend early out in air | [Server bend](#server-neck-and-tail-bend) |
| 4 | Bend cadence by distance | [Server bend](#server-neck-and-tail-bend) |
| 5 | Capsule broad phase | [Capsule spacing](#capsule-spacing-and-single-file) |
| 6 | Part counts by distance | [Part buckets](#part-buckets-and-part-counts) |
| 7 | Sweep every 5 ticks, cached per node index | [Sweep shortcut](#sweep-shortcut) |
| 8 | Corner test only on heading changes over 45° and spine reach over 2 blocks | [Corner test](#corner-test-and-the-follower-danger-term) |
| 9 | Baked tables as flat `float[]` | [Bake step](#the-bake-step) |
| 10 | Yaw history as `float[]` | [Placement layers](#placement-layers) |
| 11 | Culling box once per tick | [Identity and lookup](#identity-and-lookup) |
| 12 | Leaf breaking only on `horizontalCollision` | [Trees](#trees-leaves-and-logs) |
| 13 | Leg IK once per tick with `CollisionContext.empty()` | [Client leg IK](#client-leg-ik) |
| 14 | Spread part refresh over the herd: one animal per tick when many cross the refresh step together | [Growth](#growth) |

## Build order

This order is an inference, updated after the review. Each step targets the playtest failures first.

1. **Check 26.2 navigation.** Read 26.2 `PathNavigation.followThePath`, `Path.getEntityPosAtNode` and `WalkNodeEvaluator` from the NeoForge 26.2 or Fabric 26.2 toolchain sources.
2. **Build the per-species block filter** (pass, break, solid). All five block readers use it. Test it under a 3-high canopy.
3. **Fix arrival and add the sweep shortcut.** Override path following in `FenceAwareNavigation`. Test arrival at the aim point. Add the box sweep. Use the symmetric footprint.
4. **Settle the box and growth.** Drive growth through the `scale` attribute. Override `getDimensions` fully. Add the growth guard. Split the clearance by job.
5. **Add the turn gates.** Hard gate in tight ground, soft gate elsewhere, anti-orbit guard.
6. **Add stuck recovery and the tree rules.** Use the stuck ladder. The last rung snaps to a clear spot.
7. **Build the shared spine and the pose state.** Define a per-species spine from the posed geo data. Add the synced pose state.
8. **Add capsule spacing.** Add these pieces:
   - the circle broad phase
   - single file with the snout-to-tail gap
   - slot reassignment and rank priority
   - valid slot targets and herd jam recovery

   Retune the constants and rewrite the circle tests.
9. **Add the corner test** as a path cost. Add the follower spine danger term.
10. **Add the hit parts.** Remove the dead T. rex part code first. Add the part buckets and the distance-based part counts in the same step. Place parts in every tick and on load. Start on NeoForge, then add the Fabric mixins.
11. **Add the bake step** with its golden test and hash check.
12. **Add the neck and tail bend** with the synced bend side and the GeckoLib 5 delta updater.
13. **Add client leg IK** once per tick, with detail by distance.

Measure each step against the playtest stuck cases. Playtest 3 has no recorded result, so nobody knows which causes remain after the P8 fixes.

## Risks and tests

### Risks

| ID | Risk | In-game effect | Design answer | Status |
|---|---|---|---|---|
| C1 | Every collision query scans every part | The server slows as dinosaurs load. Every mob, item and arrow pays. | Part buckets, direct block reads, part counts by distance | Designed |
| C2 | The server has no animation clock | An arrow passes through a raised head | Synced pose state, phase by distance walked | Designed |
| H1 | Server and client bend to different sides | Arrows at the visible tail miss | Synced bend-side byte | Designed |
| H2 | A single-file column outgrows the herd rules | The last animals leave the trail and the leader waits | Chest-rule pitch, tight spacing in corridors and big herds, trail guard | Designed. R5 is resolved. |
| H3 | Only the leader plans around trunks | Followers swing tails through trunks | Spine danger term | Designed |
| H4 | One code path disagrees about leaves | The herd sticks in a canopy | One block filter, five paths | Designed |
| H5 | Growth clips or double-scales | Boxes grow into fences. Parts show adult size on a hatchling. | `scale` attribute, full `getDimensions`, part refresh, `noPhysics`, growth guard | Designed |
| H6 | The bake drifts from the assets | Arrows hit an old tail position | Gradle bake, staleness failure, golden test, hash check | Designed |
| M1 | Capsule tests cost 15 times the first estimate | Herd ticks slow | Broad phase | Designed |
| M2 | The bend costs too much under a canopy | Server ticks slow | Direct reads, step cap, skip non-ticking chunks | Designed |
| M3 | Sweeps and potions miss far parts | A sweep on a long tail does nothing | Range test to the nearest part | Designed |
| M5 | Parts skip placement with `NoAI` or before the first tick | An exhibit cannot be hit. Parts sit at 0, 0, 0 for one tick. | Placement in every tick, constructor, packet and load | Designed |
| M6 | The client tail sways twice or steps | Visible jitter | Delta updater, lerped | Designed. Needs a test. |
| M8 | Leg IK costs too much on the client | Frame rate drops | Once per tick, distance detail | Designed |
| M9 | Brachiosaurus paths cost about 5 ms and forests stop it | A Brachiosaurus looks stuck at forest edges | Open ground only, one large path per herd per tick | Accepted |
| M10 | Part tests cannot run on Fabric | Fabric bugs ship | GameTests in common code | Designed |
| L1 | Arrow effects need a `LivingEntity` | Tipped arrows and Punch do nothing on a part | Fire ticks forwarded | Accepted |
| L2 | Mixins clash with other multipart mods | Parts added twice | Inject own parts only, re-test per NeoForge release | Designed |
| L3 | IK state and yaw history are not saved | A loaded tail straightens, then bends again | One full placement before the first render | Accepted |
| L4 | Leg parts overhang the movement box | None in practice | Parts do not collide | Accepted |
| R1 | Nobody read vanilla 26.2 path-following code | The arrival fix may need changes on 26.2 | Build step 1 | Open item |
| R2 | Fabric has no part API | Fabric parts need a mixin set | Copy the MoreHitboxes Fabric design | Designed |
| R3 | GeckoLib 5 updaters may fight the controller | Tail sways twice | Test in step 12 | Untested |
| R4 | Capsule tuning and the corner test in forests are new | Herds jam in new ways | Playtests per build step | Untested |
| R5 | The single-file column may outgrow the herd rules | The rear animals lose the trail | Resolved 2026-10-10. The gather rule is slot-relative, followers past 48 blocks do not count, and the straggler wait never stops the leader (P13). The pitch rules and the trail guard keep every column on the trail. See [Capsule spacing and single file](#capsule-spacing-and-single-file). | Resolved. Measure in playtest. |
| R6 | Worktree blockers | No Fabric or dedicated-server test today | Fix before the port | Open in the worktree |

### Tests

| # | Test | Type | Build step |
|---|---|---|---|
| 1 | All five block-filter paths under a 3-high canopy | GameTest | 2 |
| 2 | A dinosaur at each whole-number width walks a straight path with no circling | GameTest | 3 |
| 3 | The sweep never cuts a corner into a trunk | GameTest | 3 |
| 4 | Box size through growth, with F3+B. GeckoLib 5 does not double-scale the model. | Playtest | 4 |
| 5 | Growth guard: a growing dinosaur in a 2-wide fence lane keeps its old box | GameTest | 4 |
| 6 | Hard gate in tight ground, soft gate in open ground, no orbit at a node | GameTest | 5 |
| 7 | The snap never lands the movement box inside blocks. The cooldown holds. | GameTest | 6 |
| 8 | Leaf and log rules per species, with `mobGriefing` on and off | GameTest | 6 |
| 9 | `HerdSpacingTest` rewritten for capsules (22 circle tests today) | Unit | 8 |
| 10 | 20 Gallimimus file through a corridor. Count stragglers. | Playtest | 8 |
| 11 | Melee and arrows on every part resolve on a dedicated server, on both loaders | GameTest | 10 |
| 12 | A `NoAI` exhibit can be hit. No part sits at 0, 0, 0 after load. | GameTest | 10 |
| 13 | Area damage: creeper, sweep and splash potion at the tail tip count once | GameTest | 10 |
| 14 | Environmental filter: a tail in lava does not burn the dinosaur | GameTest | 10 |
| 15 | Bake golden test within 0.1 blocks | Unit | 11 |
| 16 | A client with a different bake hash cannot join | GameTest | 11 |
| 17 | The build fails when an animation file is newer than its bake | Build | 11 |
| 18 | Server and client tails bend to the same side at a trunk | Playtest with a debug overlay | 12 |
| 19 | The delta updater gives no double sway and no 20 Hz steps | Playtest | 12 |
| 20 | Frame rate with 20 animals on screen, leg IK on | Playtest | 13 |
| 21 | Profile every budget row at load A and load B | Profiler | Each step |
| 22 | Brachiosaurus herd in open ground | Playtest | 4 onward |
| 23 | The NeoForge typed hook still works on each NeoForge release | GameTest | 10 onward |

**Debug aids.** A client-only overlay draws the error between the spine and the GeckoLib bones. The 26.2 debug hitbox renderer draws the client entity even in its local-server path. Do not use it to compare server and client parts (dragon note, B4).

## Open items

Only these two design decisions stay open:

| Item | Waits for | Note |
|---|---|---|
| Neck reach IK | Eating and drinking animations. Gallimimus has no `eat` or `drink` animation. | The lead decided head contact. `AngleConstraintIKChain` is the closest CHAOS fit. |
| 26.2 path-following check | The 26.2 toolchain | Read `followThePath`, `getEntityPosAtNode` and `WalkNodeEvaluator` on 26.2. The arrival defect is a fact for 1.20.1 only. |

## Background: why this design

### Survey, rounds 1 and 2

**The field standard.** Every studied system except CustomHitboxLib keeps block collision and pathfinding on one vanilla box. Each one puts body length into hit parts or steering. Parts are hit-detection ghosts. This holds for these systems:

- the vanilla Ender Dragon
- Forge and NeoForge `PartEntity`
- Ice and Fire
- Alex's Mobs and Alex's Caves
- Fossils with MoreHitboxes
- Countered's Accurate Hitboxes
- the original Project Nublar

Mowzie's Mobs has no parts at all. Bedrock Edition makes the same split in its official components ([collision_box](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/entityreference/examples/entitycomponents/minecraftcomponent_collision_box), [custom_hit_test](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/entityreference/examples/entitycomponents/minecraftcomponent_custom_hit_test)). No source gives a mob an oriented movement box that vanilla pathfinding accepts.

**Four part designs:**

| Design | Example | Result |
|---|---|---|
| Unspawned parts in the parent's array | Vanilla dragon, Alex's Caves, MoreHitboxes | The most common design. This design uses it. |
| Every part a real, tracked, saved entity | Ice and Fire, Alex's Mobs segments, original Project Nublar | The longest bug record, for example UUID log spam past 200 MB ([Ice and Fire #5412](https://github.com/AlexModGuy/Ice_and_Fire/issues/5412)) |
| No parts, region rules in `hurt()` | Mowzie's Mobs | No section hits |
| Client render-time geometry | Countered's Accurate Hitboxes 1.0.1 | Only the client crosshair benefits |

**Where part bugs live.** Part IDs and damage routing cause most bugs. Every custom client-to-server relay in the studied mods weakened a vanilla test. Alex's Mobs trusted a client damage amount. Alex's Caves accepted hits from 16 blocks. Older Alex's Caves parts return false from `hurt`, so a neck-only arrow deals nothing (code reading).

**Why server truth.** The 26.2 reach test and all arrows use the server part box. MoreHitboxes moves parts with bones on the client only, so the two sides disagree whenever an animation moves a hitbox bone. Only the original DumbLibrary-era Project Nublar posed models on the server. Its team disabled that in March 2022 and enabled it again, at a lower rate, in April 2022. GeckoLib 5 gives bones a position only at render time ([GeckoLib 5 Bone Snapshots](https://wiki.geckolib.com/docs/geckolib5/concepts/geobones/bone-snapshots)). The bake gives server truth from the real animations, without a server animation engine.

**Alternatives rejected:**

| Alternative | Why it loses |
|---|---|
| MoreHitboxes as a dependency | Stuck on 1.21.1 and GeckoLib 4 since 2026-03-31. Its renderer mixin targets a method that GeckoLib 5.5.6 no longer has ([Modrinth versions](https://api.modrinth.com/v2/project/BOVAW87Z/version)). The design copies its ideas, not its jar. |
| Fork of MoreHitboxes with server bones | A full server animation evaluator, plus drift from upstream |
| Server GeckoLib posing (DumbLibrary pattern) | Exact, but a whole animation evaluator to maintain on two loaders |
| Client bone capture with a rest-pose server | Server and client disagree during every animation |
| Hand-tuned procedural spine only | Targets drift from the keyframes |
| Client-reported bones | Trusts a client. Better Combat finds targets on the client ([TargetFinder.java](https://raw.githubusercontent.com/ZsoltMolnarrr/BetterCombat/1.20.1/common/src/main/java/net/bettercombat/client/collision/TargetFinder.java)). Its server disables the range test with `if (true \|\| ...)` ([ServerNetwork.java](https://raw.githubusercontent.com/ZsoltMolnarrr/BetterCombat/1.20.1/common/src/main/java/net/bettercombat/network/ServerNetwork.java)). |
| Oriented or compound movement box | No source does it. It would break path sizing, `Entity.collide`, step-up, pushing, suffocation tests, spawn placement and movement sync (inference from absence). |
| Colliding parts (CustomHitboxLib) | It cancels turns, and the planner sends the mob where it cannot turn. Its own FAQ warns of this. |
| Full Hybrid A* | 50 to 300 ms per replan on a 2008 PC with 72 headings ([Dolgov et al. 2008](https://cdn.aaai.org/Workshops/2008/WS-08-10/WS08-10-006.pdf)). One tick is 50 ms. The corner test keeps the cheap half. |
| ORCA or RVO2 | Assumes discs and conflicts with vanilla pushing |
| HAA* hierarchy | Described for static maps. Minecraft terrain changes. |
| Compound oriented box as the entity box (Bosses of Mass Destruction) | It overrides the box that vanilla uses for collision, lookups and culling. It needs `AABB` mixins. |
| Countered's Accurate Hitboxes 2.x | No public source, no API, no part identity |

**Precedent for each piece:**

| Piece | Status | Where it shipped |
|---|---|---|
| Torso movement box, non-colliding neck and tail | Proven | Alex's Caves, Ice and Fire, vanilla dragon |
| Arrival test at the aim point | Proven | Mowzie's Mobs, Fossils |
| Box-sweep shortcut | Proven | Mowzie's Mobs, Fossils, AzureLib |
| Hard turn gate | Proven | Alex's Caves |
| Soft turn gate, anti-orbit guard, growth guard | Proven | Fossils |
| Symmetric ceil-based footprint | New for Minecraft | Harabor (research) |
| Corner test, wedge-to-column switch, shared spine | New | None |
| Capsule spacing | New in mods | Robotics research only |
| Single-file following | Proven | Alex's Mobs, Alex's Caves |
| Slot reassignment and rank priority | New in Minecraft | Age of Empires (Pottinger) |
| Herd jam recovery | Proven | Jurassic Reborn |
| Passable leaves | Proven | Citadel, Alex's Mobs tiger |
| Procedural parts on both sides | Proven | Vanilla dragon, Alex's Caves |
| Aligned part IDs | Proven | Vanilla 24w44a and later, MoreHitboxes, Ice and Fire CE |
| Contiguous ID block | New, two sources | Ice and Fire CE (since 2026-08), old Forge dragon patch |
| Baked poses with a synced pose state | New | None |

No-teleport stuck handling is background evidence, not a precedent. Alex's Caves disabled teleport after its dinosaurs suffocated in walls ([AlexsCaves #852](https://github.com/AlexModGuy/AlexsCaves/issues/852)). This design's last rung snaps, but only after a clearance test.

### Project Nublar today

**Boxes and herd code:**

- Today's boxes are in the [layouts table](#layouts-for-the-six-species). The Gallimimus box is 1.2 × 2.6 ([EntityInit.java](../common/src/main/java/net/dumbcode/projectnublar/init/EntityInit.java)).
- The herd `ClearanceBox` uses the larger of the hitbox and the model width.
- `FenceAwareNavigation` overrides only `createPathFinder`.
- The `HerdMoveControl` guard stops the animal when the box 0.6 blocks ahead does not fit.
- `HerdSpacing` and `HerdBodies` space mates by centre distance ([HerdSpacing.java](../.claude/worktrees/herd-leader-sample/common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdSpacing.java), [HerdBodies.java](../.claude/worktrees/herd-leader-sample/common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdBodies.java)).
- `BodyFit` reads blocks through `noCollision`.
- The worktree investigation recorded these values for the Gallimimus at width 1.2 ([playtest2-geometry-investigation.md](../.claude/worktrees/herd-leader-sample/.superpowers/sdd/2026-10-08-herd-movement-redesign/playtest2-geometry-investigation.md)):
  - tolerance 0.6 from `node + 0.5`
  - aim point `node + 1.0`
  - up to 1.1 blocks of slop off the node line
  - an inner-corner column on 90° turns, often the trunk that the path turns around

**Dead T. rex head part.** The current part code is not a base for this design. A check of the worktree at `4244f62` found these defects:

| # | Defect | Effect |
|---|---|---|
| 1 | `resetParts` has no caller, so the head part never exists ([TyrannosaurusRexEntity.java](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/carnivore/TyrannosaurusRexEntity.java)) | The T. rex has only its movement box |
| 2 | Only client renderers write `DINOSAUR_HEAD_POS` ([TyrannosaurusRenderer.java](../common/src/main/java/net/dumbcode/projectnublar/client/renderer/dinosaurs/TyrannosaurusRenderer.java)) | If enabled, the server head part sits at world 0, 0, 0 |
| 3 | The position maths uses a raw pivot in model pixels, with no yaw, parents or growth | The client head box would be in the wrong place |
| 4 | The client branch of `hurt` is an empty stub ([PartEntity.java](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/PartEntity.java)) | Dead client path |
| 5 | The lifecycle copies Ice and Fire: `addFreshEntity`, a UUID re-roll, no `shouldBeSaved` | Orphan-part and UUID-clash risk |
| 6 | `Dinosaur.hurtFromPart` replaces the source with `generic()` | The attacker loses credit |
| 7 | Two `DinosaurPart` classes. The Forge one has no references ([DinosaurPart.java](../forge/src/main/java/net/dumbcode/projectnublar/entity/DinosaurPart.java)). | Confusion with Forge's `PartEntity` name |
| 8 | `updateParts` allocates a new array every tick | Minor waste |

Build step 10 removes all of this code before it adds the new parts.

**Animation and growth.** The client picks animations, and the server runs only one-shots. Gallimimus idle and rest animations use `query.life_time`. Growth has four stages (see [Growth](#growth)).

### The 1.20.1 toolchain check

On 2026-10-10, the team checked the vanilla claims against the project's own toolchain sources. The file is `vanilla-1.20.1-sources.jar`, which ModDevGradle generates with Mojang mappings and Parchment.

| Method | Lines | Fact |
|---|---|---|
| `PathNavigation.followThePath` | L261-274 | Tolerance `bbWidth / 2` above 0.75, strict `<` against `node + 0.5`, Y within 1 |
| `Path.getEntityPosAtNode` | L82-88 | Aim point `node + (int)(bbWidth + 1) × 0.5` |
| `NodeEvaluator.prepare` | L27-29 | Footprint `floor(bbWidth + 1)` |
| `Entity.refreshDimensions` | L2843-2861 | Fix-up move only up to 4.0 blocks, skipped for `noPhysics` |

The same sources match these rules for w = 1.2: the corner-cut rules (`canCutCorner`, `shouldTargetNextNodeInDirection`), the `MoveControl.tick` turn with `rotlerp(…, 90)` and the diagonal-step rule (`isDiagonalValid`). The earlier `javap` figures in [progress.md](../.claude/worktrees/herd-leader-sample/.superpowers/sdd/2026-10-08-herd-movement-redesign/progress.md) also hold. The third-party 1.20.1 mirror agrees, with different line numbers.

## Corrections log

This log resolves review M7 (body text that conflicted with the Decisions).

| # | Earlier claim | Where | Current value |
|---|---|---|---|
| 1 | Vanilla aims each path node at block centre +0.5. The fix is node centring. | Round 1 | Vanilla 1.20.1 aims at `node + floor(w + 1) × 0.5`. The defect is the arrival test in `followThePath`: tolerance w/2, strict `<`, from `node + 0.5`. |
| 2 | Mowzie's `PatchedPath` and Fossils re-centre node targets | Round 1 | `PatchedPath` repeats the vanilla formula. Their real fix is `isAt`, which tests arrival at the aim point. |
| 3 | Recommend MoreHitboxes 1.20.1 as a dependency (option A) | Round 1 | Rejected. It stalled at 1.21.1 and GeckoLib 4. Copy its design, especially the Fabric mixin set. |
| 4 | Forge does not align part IDs | Round 1 | True for generic `PartEntity` mods. Forge patched dragon part IDs from 2022 (PR #8576), and NeoForge kept that through 26.1. NeoForge removed it in 26.2. Vanilla uses `parentId + i + 1` since 24w44a (1.21.4). |
| 5 | Server truth is a rest pose plus a procedural estimate | Round 1 | Baked poses from the animation files, keyed by the synced pose state |
| 6 | The procedural spine is the server truth | Round 2 body | Same as row 5 |
| 7 | Bake tables go into `data/` | Round 2 Decisions, first version | Inside the mod jar, loaded directly, with a hash check |
| 8 | Bake tables go under `assets/` | Review H6 | Same as row 7 |
| 9 | Keep the parent pickable, torso part optional | Round 2 body | Two torso parts, movement box not pickable |
| 10 | No teleport or snap near players | Round 2 body | The lead allows the snap as the last rung, also near players |
| 11 | "The hybrid has no tree-specific measure" | Round 2 body | Leaf and log rules decided |
| 12 | Gallimimus snout 3.18 blocks ahead | Round 2 spine table (raw pivots) | 2.20 blocks (posed model) |
| 13 | Gallimimus length 6.13 blocks, in-line pitch 7.1 | Review H2 | Length 5.15 blocks. The capsule-only in-line pitch is 6.15 with a 1.0 gap. In single file, the snout-to-tail-tip rule replaces it. |
| 14 | Gallimimus torso width 0.70 | Earlier draft | Movement box width 0.80 (torso-and-legs span). Torso parts 0.75. |
| 15 | 190 capsule pair tests per herd tick | Round 2 body | 228 per evaluation (2,280 per herd tick) with today's masks. About 60 per evaluation with the broad phase (estimate). |
| 16 | Brachiosaurus torso box about 3.4 × 9.5, 160 reads per node | Review M9 | 3.68 × 8.87, 144 reads per node (inference from the vanilla formula) |
| 17 | Growth scales by four factors | Round 2 spine table | Smooth growth through the `scale` attribute |
| 18 | The corner test samples with `noCollision` | Round 2 body | Direct block reads through the block filter |
| 19 | A sweep or potion reaches the dinosaur only within the torso range | Review M3 proposal | Range test to the nearest part |
| 20 | 13 open decisions | Round 2 | Two open items |
| 21 | Gallimimus movement box 0.8 × 2.4, tail tip 2.94 | Gallimimus mockup | 0.80 × 2.39, tail tip 2.95 (layouts page) |
| 22 | Line numbers from the third-party mirror: Path L76-82, PathNavigation L228-268, Entity L2656-2672 | Round 2 | Toolchain sources: Path L82-88, PathNavigation L261-274, Entity L2843-2861. The facts agree. |

## Sources

**Research notes** (from this folder):

- [Multipart entity hitbox systems](../.claude/worktrees/herd-leader-sample/research_notes/Multipart%20entity%20hitbox%20systems/): round 1 notes on each studied mod
- [Multipart hitbox hybrid design](../.claude/worktrees/herd-leader-sample/research_notes/Multipart%20hitbox%20hybrid%20design/): round 2 notes on sources, movement, hit detection and project fit
- [Ender dragon part placement](../.claude/worktrees/herd-leader-sample/research_notes/Ender%20dragon%20part%20placement/placement_26_2.md): part IDs, the level-wide list and placement on 26.2
- [CHAOS branch IK audit](../.claude/worktrees/herd-leader-sample/research_notes/CHAOS%20branch%20IK%20audit/chaos_ik_audit.md): the IK code on `1.20.1-CHAOS`

**Pages in this folder:**

- [Dinosaur hitbox layouts.html](dinosaur-hitbox-layouts.html): the part layouts of all six species
- [Multipart hitbox hybrid design review.md](../.claude/worktrees/herd-leader-sample/reports/Multipart%20hitbox%20hybrid%20design%20review.md): the review, with full evidence per finding

**Project documents:**

- [Herd-movement redesign spec](../.claude/worktrees/herd-leader-sample/docs/superpowers/specs/2026-10-08-herd-movement-redesign.md)
- [Herd-movement handoff](../.claude/worktrees/herd-leader-sample/docs/superpowers/handoffs/2026-10-09-herd-movement-handoff.md)

**Key external citations:**

| Topic | Source |
|---|---|
| NeoForge 26.2 parts | [PartEntity.java](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/src/main/java/net/neoforged/neoforge/entity/PartEntity.java), [Level.java.patch](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/level/Level.java.patch#L133-L187), [ServerLevel.java.patch](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/server/level/ServerLevel.java.patch#L268-L291) |
| Part ID history | [Forge PR #8576](https://github.com/MinecraftForge/MinecraftForge/pull/8576), [NeoForge issue #3211](https://github.com/neoforged/NeoForge/issues/3211), [NeoForge commit 2459e75f](https://github.com/neoforged/NeoForge/commit/2459e75fe4fdda7e3d92124b1e7fe9990983009d), [MC-158205](https://mojira.dev/MC-158205) |
| Vanilla 26.2 (third-party mirror, secondary) | [Renekovski/26.2-mcp at 51f3128b](https://github.com/Renekovski/26.2-mcp/tree/51f3128ba7265299dce20b9abd2b37d7dd27d096) |
| Vanilla 1.20.1 (third-party mirror, secondary) | [Blackjack200/minecraft_client_1_20_1 at c129e3e3](https://github.com/Blackjack200/minecraft_client_1_20_1/tree/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c) |
| GeckoLib 5 | [RenderPassInfo.java](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/renderer/base/RenderPassInfo.java), [Bone Snapshots](https://wiki.geckolib.com/docs/geckolib5/concepts/geobones/bone-snapshots) |
| Alex's Caves sauropods | [SauropodBaseEntity.java](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java) |
| Fossils navigation and growth | [PrehistoricPathNavigation.java](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/PrehistoricPathNavigation.java#L48-L97), [Prehistoric.java](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/Prehistoric.java#L419-L449) |
| Mowzie's Mobs navigation | [MMPathNavigateGround.java](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathNavigateGround.java#L30-L140) |
| Ice and Fire CE parts | [MultipartPartEntity.java](https://github.com/IAFEnvoy/IceAndFire-CE/blob/63a3bba7960ad991c67fe1d1f4ea1c32e6523741/src/main/java/com/iafenvoy/iceandfire/entity/MultipartPartEntity.java) |
| MoreHitboxes Fabric module | [FabricLevelMixin.java](https://github.com/DarkPred/MoreHitboxes/blob/88899b3e1c4ed881b1ea65b35104453fc6df592a/fabric/src/main/java/com/github/darkpred/morehitboxes/mixin/FabricLevelMixin.java#L22-L61) |
| Clearance and crowds | [Harabor 2009](https://users.cecs.anu.edu.au/~dharabor/data/papers/harabor-aigamedev09.pdf), [Pottinger 1999](https://gamedeveloper.com/programming/implementing-coordinated-movement), [EPFL RDS](https://infoscience.epfl.ch/record/284026/files/09385856.pdf) |

The two Better Combat links point to a branch head as read on 2026-10-09, not to a commit SHA. Pin them before anyone quotes them as stable references.
