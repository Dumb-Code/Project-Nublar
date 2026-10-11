# Multipart Hitboxes

> **Superseded on 2026-10-10** by [Dinosaur hitboxes and movement design.md](Dinosaur%20hitboxes%20and%20movement%20design.md). That page holds the current design and the lead's decisions. This page stays as history. Some of its claims are wrong or replaced. The corrections log in the new page lists them.

> **Question from the lead:** Would a T. rex-style multipart hitbox system, synced to animation and rotation, stop herd members from getting stuck in gaps and corridors?
>
> **Answer:** No, not largely. Multipart hitboxes change what a hit touches. In every studied system except one, they do not change where a dinosaur can walk.
>
> *This page is an STE-MC rewrite of `Multipart entity hitbox systems.md` in the same folder.*

## Summary

**In game, a multipart hitbox system affects these things:**

- Player and arrow hits on the visible head and tail.
- Bite reach.
- Render culling.
- Rider seats.

**In game, a multipart hitbox system would not largely stop herd members from getting stuck in gaps and corridors.**

Every studied system uses parts as **hit-detection ghosts**. Parts never collide with blocks. Parts never enter pathfinding. Movement and A* use the single main box. This is true for these systems:

- The vanilla Ender Dragon
- Forge and NeoForge `PartEntity`
- Ice and Fire
- Alex's Mobs
- Alex's Caves
- Fossils with MoreHitboxes
- Countered's Accurate Hitboxes
- The original Project Nublar

Mowzie's Mobs has no parts at all.

CustomHitboxLib is the only system whose parts collide with blocks. Its pathfinder still plans paths with the main box width. It also cancels any turn that would push a part into a block. Its own FAQ warns that colliding parts confuse the pathfinder.

The fix for stuck herds belongs in the navigator and in herd-mate spacing. Three changes help:

1. Width-aware path following, with path nodes centred for the body's width.
2. Size-aware passability checks.
3. An oriented capsule along the body axis for spacing, in place of an axis-aligned box.

For hit detection, MoreHitboxes 1.20.1 fits Project Nublar's Forge 1.20.1 and GeckoLib stack best. It is MIT-licensed, and Fossils uses it. It follows bones only on the client, though. The server always uses the rest pose. Of the studied mods, only the original DumbLibrary-era Project Nublar poses models on the server.

Project Nublar's current T. rex head part is dead code. Nothing ever creates it. If a developer enables it, the part sits at world coordinates (0, 0, 0) on the server. Remove it before any option on this page goes in.

### At a glance

| | |
|---|---|
| Stops herd members getting stuck | No, not largely |
| Makes hits land on the visible head and tail | Yes |
| Best option for hit detection | A: MoreHitboxes 1.20.1 |
| Best options for stuck herds | E: capsule spacing. F: navigation fixes. |
| Server poses the model | Only the original DumbLibrary-era Project Nublar. Procedural systems such as Alex's Caves estimate part positions on both sides. |
| Current Project Nublar state | The T. rex head part is dead code. Remove it first. |

### Terms

- **Part:** an extra hitbox that belongs to a parent mob.
- **Main box:** the parent's single vanilla AABB (axis-aligned bounding box).
- **Rest pose:** the model with no animation applied.
- **Server truth:** the server knows where a part is during an animation.

## Part designs

In game, the four designs differ in what the player can hit:

- Designs 1 and 2 add hit areas to the body.
- Design 3 adds no hit areas. It applies body-region rules inside `hurt()`.
- Design 4 helps only the client crosshair.

### Design 1: Unspawned parts in the parent's array

This is the most common design. The vanilla Ender Dragon owns 8 `EnderDragonPart` objects. The game never adds them to the world, never saves them and never sends them as entities. Each tick, both sides place the parts from the dragon's position and a 64-entry history of yaw and Y ([V EnderDragon.java](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L90-L105)).

Forge makes this design general with `PartEntity`, `isMultipartEntity()` and `getParts()`. Forge keeps a part map for each level, so `Level.getEntities` finds parts. Forge does **not** do these things:

- Sync part IDs or positions.
- Save parts.
- Give parts block collision.

Sources: [F IForgeEntity.java](https://github.com/MinecraftForge/MinecraftForge/blob/0ec923d7307eb15bef70d6916329642b733a21ed/src/main/java/net/minecraftforge/common/extensions/IForgeEntity.java#L183-L207), [F Level.java.patch](https://github.com/MinecraftForge/MinecraftForge/blob/0ec923d7307eb15bef70d6916329642b733a21ed/patches/minecraft/net/minecraft/world/level/Level.java.patch#L161-L200).

These systems use this design as their base:

- Alex's Caves
- The cachalot in Alex's Mobs
- MoreHitboxes
- CustomHitboxLib

Citadel 1.21 adds no multipart API at all ([Citadel 1.21 tree](https://github.com/AlexModGuy/Citadel/tree/1.21/src/main/java/com/github/alexthe666/citadel)).

### Design 2: Each part is a real entity

In this design, the game tracks and saves every part as an entity. Ice and Fire uses `EntityMutlipartPart extends Entity`. It spawns each part with `addFreshEntity` and links it to its parent by UUID ([IaF EntityUtil.java](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/util/EntityUtil.java#L15-L38)).

Alex's Mobs uses the same design for the anaconda, centipede and void worm segments. The original Project Nublar used one real entity per model cube. That is 15 to 17 entities per adult dinosaur ([PN master Tyrannosaurus.java](https://github.com/Dumb-Code/ProjectNublar/blob/e525ed7b69aaaa9de5234de9875f50d3822a25af/src/main/java/net/dumbcode/projectnublar/server/dinosaur/Tyrannosaurus.java#L56-L62)). This design has the longest bug record. See [Real-entity part bugs](#real-entity-part-bugs).

### Design 3: No parts

Mowzie's Mobs gives each boss one vanilla box. Body regions are rules inside `hurt()`. For example, the Wroughtnaut has a 220° frontal armour arc ([MM EntityWroughtnaut.java](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/entity/wroughtnaut/EntityWroughtnaut.java#L183-L207)).

### Design 4: Virtual geometry at render time

Countered's Accurate Hitboxes 1.0.1 turns each vanilla `ModelPart` cuboid into world-space triangles on the client. It then replaces one raycast function. Only the client crosshair benefits ([Countered ModProjectileUtilMixin.java](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/mixin/server/ModProjectileUtilMixin.java)).

### Comparison

| Project | Part model | Positioning source | Animation on server | Networking | Damage routing | Block collision | Pathfinding | Notable bugs | License / maturity |
|---|---|---|---|---|---|---|---|---|---|
| Vanilla Ender Dragon 1.20.1 | 8 unspawned `EnderDragonPart`s in the parent's array | Yaw/Y ring buffer (64 ticks) plus trigonometry, on both sides | None needed. The same buffer drives the renderer. | None for parts. The client sets IDs in `recreateFromPacket`. | `hurt(part)`: the head takes full damage. Other parts take `amt/4 + min(amt,1)`. | None. `checkWalls` destroys blocks in the head, neck and body boxes. | None (flight is phase-driven) | Part ID off by one, [MC-158205](https://mojira.dev/MC-158205). Fixed only in 24w44a. | Platform code |
| Forge 1.20.1 / NeoForge 1.21.1 `PartEntity` | Abstract unspawned part. Each level keeps a part map. | The mod decides | The mod decides | None. No ID alignment. | None provided | None | None | `Level.getEntities` loops over every part in the level for each query | Platform API |
| Ice and Fire 1.20 | 4 registered real entity types. Spawned, tracked and saved. | Fixed polar offset around `yBodyRot`, server only | None | Vanilla tracking of each part | `parent.hurt × multiplier`. Melee uses `AttackEntityEvent`. | None. Adult dragons break blocks around the main box. | Async A* derived from Minecolonies, sized from the main box | UUID log spam ([#5412](https://github.com/AlexModGuy/Ice_and_Fire/issues/5412)). PR #5117 regressions. | License not checked. Maintainers want to move to Forge parts. |
| Alex's Mobs 1.20 | (A) `PartEntity` bodies. (B) Real `LivingEntity` segments. | (A) Ring buffer copied from the dragon, on both sides. (B) The head moves segments on the server and probes terrain. | None | (A) Client-to-server `MessageHurtMultipart`. (B) Entity tracking. | (A) `parent.hurt`. (B) Forwarded to the head. Exception: void worm segments have their own health. | None | Head or main box only | The relay trusts a damage amount sent by the client | License not checked |
| Alex's Caves 1.21.1 | NeoForge `PartEntity`, 1 to 10 per creature | Procedural chain: offsets rotated by smoothed neck and tail angles, 128-entry yaw buffer, on both sides | Hard-coded target angles per animation. The model reads the parts. | None for parts. Client-to-server relay by parent ID. | Newer parts: `parent.hurt`. Older parts: `hurt` returns false. | None | Citadel `AdvancedPathNavigate` with the main box | Projectiles on older parts deal no damage. The relay accepts hits from 16 blocks away. | License not checked |
| Mowzie's Mobs 1.20 | None. One box per mob. | Client socket bones, for particles only | None. Hit frames are fixed tick numbers. | Animation index packets | Bearing arcs inside `hurt` | Main box | `MMPathNavigateGround` with node centring and box sweep | No hitbox issues in the public tracker | License not checked |
| Fossils + MoreHitboxes 1.20.1 | Forge `PartEntity`, defined in data-pack JSON and bound to GeckoLib bones | Server: static offset rotated by yaw, × `getScale()`. Client: GeckoLib bone position, once per tick. | None (the server uses the rest pose) | JSON synced once. Part IDs aligned as `id + i + 1`. | `partHurt` (Fossils adds no multiplier). The client reports attack boxes. | None (`noPhysics`) | Fossils navigation from the main box, with centred nodes | Server and client boxes differ during animations. Off-screen client boxes become stale. | MIT. Forge 1.9.2 has about 181k downloads. One author. |
| MoreHitboxes 1.21.1 | Same API, NeoForge `PartEntity` | Same | Same | Code reading finds no NeoForge path that sends JSON to clients of a dedicated server | Same | Same | Same | Alpha builds | MIT |
| CustomHitboxLib | `PartEntity` carriers, never spawned. A mixin adds them to every `LivingEntity`. | `PartPositioner` lambda, yaw only, one set per `Pose` | None | Client-to-server player rotation only | Full damage to the parent. Ray hits resolve to the parent. | Optional compound collision. Cancels turns that would clip. | Main width. Replaces `MoveControl` for every mob. | Global mixins. Reflection every tick. | MIT. Repo created July 2026. 766 downloads. |
| Countered's 1.0.1 | No parts. Client cuboid triangles. | Client render pose of vanilla `ModelPart`s | None | None | Vanilla (entity only, part unknown) | None | None | Client-only authority. GeckoLib models not covered (inference). | Repo CC0 (Modrinth says MIT). 2.x has no public source. |
| Original Project Nublar + DumbLibrary | One real entity per model cube (15 to 17), managed by an ECS | Bounding box of the 8 corners of each animated cube | **Yes**: the server loads the model and poses it again every 3 ticks (0.15 s) in 1.12, or every 5 ticks (0.25 s) in 1.16 | Vanilla tracking (interval 20 ticks) plus a synced size | `parent.hurt`, unchanged | None | Main box at the 0.6 × 1.8 default | UUID byte-order swap. Young dinosaurs get no parts. `Tail3` name mismatch. | Unmaintained |
| Current Project Nublar | Real entity copied from Ice and Fire (T. rex head), plus an unused Forge `DinosaurPart` | Intended: the head bone pivot, which the client renderer writes into synced data | None | Effectively none (the client write never reaches the server) | `parent.hurt × 1.5` | None | Main box | Never spawned. See [Project Nublar head part](#project-nublar-head-part). | In-repo |

The table shows that the field agrees on one design: parts stay out of movement. The two mods that tried something richer show why.

- **Fossils** gets real bone-bound boxes from MoreHitboxes, but only for picking ([MH ForgeMultiPart.java](https://github.com/DarkPred/MoreHitboxes/blob/012edbb4258120161c96103e51dac6374b78cab8/forge/src/main/java/com/github/darkpred/morehitboxes/internal/ForgeMultiPart.java#L24-L129)). Its pathfinding still sizes everything from the main box ([FA PrehistoricPathFinder.java](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/PrehistoricPathFinder.java#L130-L140)).
- **The original Project Nublar** posed its model on the server. It still moved each dinosaur as a player-sized box of 0.6 × 1.8 blocks. Its pathfinding code never refers to parts ([PN 1.16 EntityHandler.java](https://github.com/Dumb-Code/ProjectNublar/blob/32f3a45caac7197e7a7a45d04c77d121ea4c74b0/src/main/java/net/dumbcode/projectnublar/server/entity/EntityHandler.java#L17-L23)).

## Mechanics

### Animation sync

**In game:** a player shoots an arrow at a dinosaur that lowers its head to drink, sleeps, or swings its tail. The player expects the arrow to hit where the body is on screen. Whether it hits depends on whether the server and the client agree on the part positions.

The way a mod places its parts decides this agreement. The studied mods use three approaches.

#### Procedural placement on both sides

Of the three approaches, this one keeps the server and the client in agreement most reliably. The dragon, the cachalot in Alex's Mobs and Alex's Caves all run the same deterministic function on each side. The inputs are values that vanilla already syncs:

- Position
- Yaw
- Head yaw
- Animation ID
- Tick

No part data crosses the network.

Alex's Caves takes this approach furthest. Its sauropod neck and tail parts follow a 128-entry yaw ring buffer and target angles for each animation. Examples are −140° neck pitch during ROAR and ±70° tail yaw during whips. The client model then reads its neck and tail bone rotations **from the part positions**. This is the reverse of the usual direction ([AC SauropodBaseEntity.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L247-L305), [AC SauropodBaseModel.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/client/model/SauropodBaseModel.java#L432-L461)).

The cost is fidelity. The hitboxes match the animation only as well as the hand-tuned targets copy the keyframes. Legs and jaw get no boxes.

#### Client bone capture

This approach gives the best visual match but no server truth. MoreHitboxes reads `bone.getLocalPosition()` inside GeckoLib's `renderRecursively`, once per game tick. It stores the result as a part "override". The server never renders. So on the server, MoreHitboxes uses the static JSON offset, rotated by `yBodyRot` and scaled by `getScale()` ([MH21 MultiPart.java](https://github.com/DarkPred/MoreHitboxes/blob/88899b3e1c4ed881b1ea65b35104453fc6df592a/common/src/main/java/com/github/darkpred/morehitboxes/api/MultiPart.java#L43-L64), [MH21 GeoEntityRendererMixin.java](https://github.com/DarkPred/MoreHitboxes/blob/88899b3e1c4ed881b1ea65b35104453fc6df592a/common/src/main/java/com/github/darkpred/morehitboxes/mixin/GeoEntityRendererMixin.java#L23-L52)).

The two sides therefore **disagree whenever an animation moves a hitbox bone**. Examples are a lowered drinking head, a sleeping pose and a tail swing. The in-game effects are:

- **Player melee** follows the animated boxes on the client, because the client sends the part ID.
- **Arrows, explosions and AI queries** on the server hit the rest-pose boxes.
- **Off-screen mobs** keep a stale pose on the client, because MoreHitboxes never clears the override.

Only position and scale come from the bone. MoreHitboxes ignores bone rotation, and each box stays axis-aligned. The author's own wiki says the rotation "has no effect on the actual hitbox in game" ([MoreHitboxes wiki](https://github.com/DarkPred/MoreHitboxes/wiki/Blockbench-plugin)).

Mowzie's Mobs captures bones the same way but uses them only for particles. Its gameplay uses hard-coded mouth offsets rotated by yaw ([MM EntityFrostmaw.java](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/entity/frostmaw/EntityFrostmaw.java#L366-L390)).

Countered's captures vanilla `ModelPart`s only. GeckoLib dinosaurs therefore use the vanilla box. This is an inference from GeckoLib's renderer hierarchy. The research did not test it.

#### Server-side posing

Only one studied codebase uses this approach: Project Nublar's own predecessor. DumbLibrary loaded the model on the server:

- 1.12: the Tabula `.tbl` model. The server posed it again every 3 ticks (0.15 s).
- 1.16: the DumbCode Studio `.dcm` model. The server posed it again every 5 ticks, with a 0.25 s step.

Each part became the bounding box of the 8 corners of one animated cube, rotated by yaw ([PN master MultipartSystem.java](https://github.com/Dumb-Code/ProjectNublar/blob/e525ed7b69aaaa9de5234de9875f50d3822a25af/src/main/java/net/dumbcode/projectnublar/server/entity/system/impl/MultipartSystem.java#L52-L133), [DL master AnimationComponent.java](https://github.com/Dumb-Code/DumbLibrary/blob/791ada6afea65e284e4f9e534dd64b15a1815afe/src/main/java/net/dumbcode/dumblibrary/server/ecs/component/impl/AnimationComponent.java#L144-L160)).

The team disabled the server animation in March 2022. They enabled it again at a lower rate in April 2022 ([commit 6d32dd27](https://github.com/Dumb-Code/ProjectNublar/commit/6d32dd27), [commit c24f8ce3](https://github.com/Dumb-Code/ProjectNublar/commit/c24f8ce3)). That churn is the only public evidence of what server posing costs in practice. GeckoLib has no server-side bone pose. To use this approach again, a developer must write the evaluator.

### Part IDs and damage routing

**In game:** a player hits a part with a sword, and the hit does not always count.

**Cause:** a Forge `PartEntity` takes a fresh ID from the entity counter on each side. Forge does not align the two IDs. So the client sends attack packets with part IDs that the server cannot resolve. Most part bugs live here.

Vanilla's own dragon has a similar but separate bug. It does not use Forge `PartEntity`, but its part IDs are off by one. [MC-158205](https://mojira.dev/MC-158205) made melee hits on the dragon unreliable from 1.14.4 until 24w44a. That fix came after 1.20.1.

Mods solve this in three ways:

1. **ID alignment.** MoreHitboxes uses a `setId` mixin. It sets each part's ID to `parentId + i + 1` on both sides. Vanilla packets then resolve to the right part ([MH EntityMixin.java](https://github.com/DarkPred/MoreHitboxes/blob/012edbb4258120161c96103e51dac6374b78cab8/forge/src/main/java/com/github/darkpred/morehitboxes/mixin/EntityMixin.java#L68-L76)).
2. **Relay with a client damage amount.** `MessageHurtMultipart` in Alex's Mobs accepts **a damage amount from the client**, with no reach check. This opens an exploit ([AM MessageHurtMultipart.java](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/message/MessageHurtMultipart.java#L59-L90)).
3. **Relay with a wide range.** The relay in Alex's Caves accepts an attack on any multipart parent within 16 blocks. This also opens an exploit ([AC MultipartEntityMessage.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/message/MultipartEntityMessage.java#L47-L63)).

The older part classes in Alex's Caves also return false from `hurt`. Code reading suggests that an arrow that hits only a sauropod's neck deals no damage ([AC SauropodPartEntity.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodPartEntity.java#L79-L120)).

Damage localisation is simple wherever it exists:

| System | Damage by body part |
|---|---|
| Vanilla Ender Dragon | Head takes full damage. All other parts take reduced damage. |
| Ice and Fire | Multiplier: head × 1.5, wings × 0.5 |
| MoreHitboxes | Passes the part to `partHurt`. No multiplier. |
| Fossils | Adds no multiplier |

### Real-entity part bugs

**In game:** the server log fills with errors. Users of Ice and Fire reported "UUID of added entity already exists: EntityDragonPart" from 1.18 to 1.20.1. One report describes logs larger than 200 MB ([#5412](https://github.com/AlexModGuy/Ice_and_Fire/issues/5412)). The reports continued after PR #5117.

PR #5117 also caused two regressions ([IaF EntityHydra.java](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydra.java#L263-L269), [PR #5117](https://github.com/AlexModGuy/Ice_and_Fire/pull/5117)):

- A hydra loop typo (`HEADS + 1`).
- A flat sea-serpent chain, because the code sets each segment's parent again every tick.

A maintainer there wrote that the system should ideally become Forge's own.

### Performance

Performance is modest everywhere the research examined it, with one scaling cost. Forge's `Level.getEntities` loops over **every part in the level for every AABB query**. The cost therefore grows with total loaded parts times queries. It does not grow with the number of parts near the query ([F Level.java.patch](https://github.com/MinecraftForge/MinecraftForge/blob/0ec923d7307eb15bef70d6916329642b733a21ed/patches/minecraft/net/minecraft/world/level/Level.java.patch#L161-L200)). None of the projects published profiling data.

## Stuck herds

**In game:** herd members wedge in gaps and corridors. The lead reported this symptom. The code that moves the animal causes it, and every studied system leaves that code to the main box.

### Peers keep parts out of movement

- **Alex's Caves:** sauropods carry long necks and tails as parts. They still navigate with Citadel's `AdvancedPathNavigate`, using `getBbWidth()` and `getBbHeight()` of a 5 × 8 main box ([AC AdvancedPathNavigateNoTeleport.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/AdvancedPathNavigateNoTeleport.java#L8-L22)). In game, necks and tails pass through walls. The research note on Alex's Caves reads this as a deliberate trade: no stuck AI, at the cost of visual clipping.
- **Ice and Fire:** the dragon's main box is at most 0.78 × 1.2, scaled by at most 7. Path checks use that box, and the dragon breaks blocks around it ([IaF EntityDragonBase.java](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L2911-L2924)).
- **MoreHitboxes:** sets `noPhysics = true` on every part and has no navigation code ([commit e772727d](https://github.com/DarkPred/MoreHitboxes/commit/e772727d)).

### CustomHitboxLib: colliding parts

CustomHitboxLib is the counter-example, and it shows the risk.

- It runs a real compound-box sweep inside `Entity.collide` for parts with the `collision` flag ([CHL EntityMixin.java](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/src/main/java/dev/customhitboxlib/mixin/EntityMixin.java#L69-L168)).
- Its rotation mixins **cancel any yaw change** that would push a clear part into a block ([CHL EntityRotationMixin.java](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/src/main/java/dev/customhitboxlib/mixin/EntityRotationMixin.java#L29-L133)).
- Its node evaluator still plans with the main width.
- Its FAQ warns that large colliding parts confuse the pathfinder ([CHL FAQ](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/wiki/FAQ.md)).

**In game, for a T. rex in a corridor:** if Project Nublar used colliding animated parts, the planner would send the animal into a space where it then cannot turn. The T. rex would get more stuck, not less.

A rotation-synced hitbox system therefore helps the herd only if the pathfinder plans in the same shape. No studied mod does that. The cost is an A* node evaluator with an oriented footprint, a large new subsystem.

### Navigation fixes in peer mods

The fixes that studied mods use for big bodies all live in navigation.

| Fix | In-game effect | Where it exists |
|---|---|---|
| Node centring | A wide mob steers the centre of its footprint onto each node, not a corner. Vanilla aims each path node at block centre +0.5. The fix re-centres node targets at `floor(bbWidth + 1) × 0.5`. | Mowzie's `PatchedPath` ([MM MMPathFinder.java](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathFinder.java#L19-L43)), Fossils' `PrehistoricPathFinder` ([FA PrehistoricPathFinder.java](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/PrehistoricPathFinder.java#L130-L140)) |
| Box sweep shortcut | The mob takes shortcuts along the path where a voxel sweep of its own box is clear | Mowzie's ([MM MMPathNavigateGround.java](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathNavigateGround.java#L17-L140)) |
| Size-aware passability | Each path node checks a volume sized from the entity (`ICustomSizeNavigator`). Path jobs run on worker threads. | Citadel, Ice and Fire ([Citadel AdvancedPathNavigate.java](https://github.com/AlexModGuy/Citadel/blob/1.21/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/AdvancedPathNavigate.java#L103-L129)) |
| Turn before move | The sauropod walks forward only when its body is within 15° of the move direction | Alex's Caves ([AC SauropodBaseEntity.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L539-L686)) |
| Growth check | A growing dinosaur keeps its old box if the bigger box would be inside blocks | Fossils ([FA Prehistoric.java](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/Prehistoric.java#L419-L454)) |

This research did not audit Project Nublar's current navigator. The next step is to check which of these fixes it already has.

### Herd-mate spacing

The lead's herd change, shrinking the clearance box to the hitbox, matches what every peer does for terrain. Herd-mate spacing is a separate question. For spacing, an axis-aligned box is the wrong shape.

Minecraft boxes never rotate with yaw. The dragon's parts, MoreHitboxes' parts and CustomHitboxLib's parts are all axis-aligned boxes placed by their centre ([V EnderDragon.java](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L351-L353)). For a long body, this gives a bad result:

- A square sized to the body's length reserves too much space at the sides.
- A square sized to the body's width reserves too little space in front and behind.

A capsule fixes this. A capsule is a line segment from tail to snout along `yBodyRot`, with a radius of half the body width. The distance between two capsules is a cheap segment-to-segment calculation. It gives the true gap at any heading.

This is a design recommendation, not a pattern found in the studied mods. It stays separate from hit detection and from terrain clearance.

## Project Nublar head part

**In game:** the T. rex has only its main box today. A player cannot hit a separate head.

The current code is not a working base for any option. The research notes found that the common `PartEntity` matches Ice and Fire's `EntityMutlipartPart` field for field and method for method. That includes the UUID re-roll and the `addFreshEntity` lifecycle, which has the longest bug record in the survey. A check of the worktree at `4244f62` found the same defects, plus defect 1.

| # | In-game effect | Defect | Evidence |
|---|---|---|---|
| 1 | The T. rex has only its main box. All other defects are latent. | The game never creates the head part. `resetParts` has no caller in common, forge or fabric. So `head` stays null, and `updatePart` returns at once. | [TyrannosaurusRexEntity.java](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/carnivore/TyrannosaurusRexEntity.java) L28-34, L54-57. [Dinosaur.java](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java) L183. |
| 2 | If a developer enables the part, the server-side head part sits at **world coordinates (0, 0, 0)**. | Only the client writes the head position. Six species renderers call `entityData.set(DINOSAUR_HEAD_POS, …)` inside `scaleModelForRender`. The setter `setHeadPositon` has no callers. Synced data flows from server to client only, so the server reads the default `(0,0,0)`. The renderer also writes server-owned synced data every frame. | [TyrannosaurusRenderer.java](../common/src/main/java/net/dumbcode/projectnublar/client/renderer/dinosaurs/TyrannosaurusRenderer.java) L34-39. Dinosaur.java L110, L282, L385-391. |
| 3 | Even on the client, the head box would be in the wrong place. | The position maths uses a raw GeckoLib pivot in model pixels. It does not divide by 16 or rotate by yaw. It ignores parent bones and the growth-stage scale. | TyrannosaurusRenderer.java L22-39. Compare MoreHitboxes, whose `getLocalPosition()` input is already rotated and scaled ([GL GeoEntityRenderer.java](https://github.com/bernie-g/geckolib/blob/7d6f32db9168a24e14b327c0ad9f8f87dfc204de/Forge/src/main/java/software/bernie/geckolib/renderer/GeoEntityRenderer.java#L288-L312)). |
| 4 | Hits would work only through the vanilla attack packet. | Only the server resolves the parent. The client branch of `hurt` is an empty `//send packet` stub. This is a dead client path, as in Ice and Fire. | [PartEntity.java](../common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/PartEntity.java) L229-235 |
| 5 | Risk of orphan parts and UUID clashes, as in Ice and Fire [#5412](https://github.com/AlexModGuy/Ice_and_Fire/issues/5412) and [#5413](https://github.com/AlexModGuy/Ice_and_Fire/issues/5413). | The lifecycle copies Ice and Fire: a real entity added with `addFreshEntity`, a UUID re-roll, and no `shouldBeSaved` override. | TyrannosaurusRexEntity.java L59-73 |
| 6 | A hit on the part would lose the attacker: no retaliation and no kill credit. Nothing calls it today. | `Dinosaur.hurtFromPart` replaces the damage source with `generic()`. | Dinosaur.java L429-431 |
| 7 | No effect today, because nothing references the classes. Confusing, and conflicts with MoreHitboxes. | There are two `DinosaurPart` classes. The Forge one extends Forge's `PartEntity`, returns false from `hurt` and has no references. `Dinosaur` declares `subEntities` but never overrides `getParts()` or `isMultipartEntity()`. The common class `PartEntity` has the same simple name as Forge's class. MoreHitboxes needs the mob **not** to override `getParts()`. | [forge DinosaurPart.java](../forge/src/main/java/net/dumbcode/projectnublar/entity/DinosaurPart.java). Dinosaur.java L136. [MH21 NeoForgeMobMixin.java](https://github.com/DarkPred/MoreHitboxes/blob/88899b3e1c4ed881b1ea65b35104453fc6df592a/neoforge/src/main/java/com/github/darkpred/morehitboxes/mixin/NeoForgeMobMixin.java#L21-L40) |
| 8 | Minor waste. | `tick()` calls `refreshDimensions()` and `updateParts()` every tick. `updateParts` allocates a new array each time. | TyrannosaurusRexEntity.java L47-51, L103-108 |

### Original DumbLibrary defects

The original DumbLibrary code has its own defects. They matter if anyone uses that code again.

- **UUID byte order.** The 1.12 component serialiser writes UUIDs in LSB-then-MSB order but reads them in MSB-then-LSB order ([PN master MultipartEntityComponent.java](https://github.com/Dumb-Code/ProjectNublar/blob/e525ed7b69aaaa9de5234de9875f50d3822a25af/src/main/java/net/dumbcode/projectnublar/server/entity/component/impl/MultipartEntityComponent.java#L91-L108)).
- **Hatchlings never get parts.** The code spawns parts only for the age at join time, and only adult part lists exist.
- **Name mismatch.** The T. rex list names `Tail3`, but the other species use `tail3` ([PN master Tyrannosaurus.java](https://github.com/Dumb-Code/ProjectNublar/blob/e525ed7b69aaaa9de5234de9875f50d3822a25af/src/main/java/net/dumbcode/projectnublar/server/dinosaur/Tyrannosaurus.java#L56-L62)).

## Options

| Option | Hit accuracy | Server truth during animation | Effect on stuck herds | Cost and risk |
|---|---|---|---|---|
| **A. Single box for movement + MoreHitboxes 1.20.1 bone-bound parts** | Client boxes follow GeckoLib bones. MoreHitboxes includes ID alignment, picking redirect, reach, culling bounds and rider anchors. | Rest pose only | None | Required dependency on both sides. One JSON file and empty `<name>_hitbox` bones per species. Growth must flow through `getScale()`. Boxes are axis-aligned. One author. The 1.21.1 line is alpha. |
| **B. Fork MoreHitboxes to evaluate bones on the server** | Full | Yes | None | GeckoLib has no server-side pose, so a developer must write the evaluator. The author's TODO already flags the GeckoLib coupling. The fork drifts from upstream. |
| **C. Use DumbLibrary-style server posing again** | Bounding box of each rotated cube (too large on diagonals) | Yes, at the chosen cadence | None | Rebuild a server model loader and animator for GeckoLib. The predecessor churned on cost. Never use one real entity per cube. |
| **D. Procedural chain (dragon / Alex's Caves style) on Forge `PartEntity`, no dependency** | Head, neck and tail follow yaw, head yaw, pitch and per-animation targets | Yes, deterministic on both sides | None | Hand-tuned targets drift from the keyframes. You must write ID alignment, picking and reach yourself. The homemade systems broke there. |
| **E. Oriented capsule spacing for herd mates** | n/a | n/a | Fixes jams between herd mates, not terrain jams | Small, self-contained steering change |
| **F. Navigation fixes (node centring, box sweep, size-aware passability, turn before move)** | n/a | n/a | Directly targets terrain jams | Moderate. Several peer implementations exist to study. |

### Why option A for hit detection

Option A is the best fit for hit detection. MoreHitboxes has these strengths:

- It is small (about 2.8k lines) and MIT-licensed.
- It compiles against GeckoLib 4.7.
- Its 1.20.1 Forge build reached about 181k downloads through Fossils.
- Its public API is byte-identical between 1.20.1 and 1.21.1, which makes a later port easier ([Modrinth versions](https://api.modrinth.com/v2/project/BOVAW87Z/version), [MH20 forge build.gradle](https://github.com/DarkPred/MoreHitboxes/blob/012edbb4258120161c96103e51dac6374b78cab8/forge/build.gradle#L66-L74)).
- It solves the vanilla plumbing that broke the homemade systems: part ID alignment, client section indexing and picking redirection.

Two caveats apply:

1. The research checked the `renderRecursively` descriptor against GeckoLib 4.8.4 only, not against Project Nublar's 4.7.3 tag. Project Nublar's renderers must also reach the base `renderRecursively`.
2. Integration needs a `getScale()` override that matches the growth-stage render scale. Static server offsets scale only through that method ([MH21 MultiPart.java](https://github.com/DarkPred/MoreHitboxes/blob/88899b3e1c4ed881b1ea65b35104453fc6df592a/common/src/main/java/com/github/darkpred/morehitboxes/api/MultiPart.java#L59-L60)).

### Closing the server and client gap without a fork

In option A, the server and the client disagree during animations. Two methods make the gap smaller without a fork:

1. **Author still hitbox bones.** Place the hitbox bones so that they barely move.
2. **Hybrid (untested).** `MultiPart.updatePosition()` honours any non-null override. Project Nublar could write a procedural head and tail estimate, as in option D, as the override on the server only. The client keeps the real bone.

Options B and C pay for themselves only if gameplay needs exact posed hitboxes on the server. Examples are arrows against a lowered head or a bite volume on the server. No studied mod outside Project Nublar's own past found that worth the cost.

### Recommended sequence

1. **Remove the dead head-part code.** Remove both `DinosaurPart` classes and the client renderer's write to `DINOSAUR_HEAD_POS`. Then nothing remains to collide with a future system.
2. **Treat the herd symptom with options E and F.** Measure them against the stuck cases that the lead reported.
3. **Adopt option A for hit detection** when player-facing hit accuracy becomes the priority. Start with the T. rex, the Brachiosaurus and the other long-bodied species, where the main box misses most.

## Conclusion

The survey changes the question. "Hitbox problems" combines two different jobs:

- Deciding **what a hit touches**.
- Deciding **where a body can go**.

The Minecraft mod ecosystem gives the two jobs separate owners. Parts do the first job. The main box and a size-aware navigator do the second job.

That split is not laziness. The one library that merged the two jobs ended up cancelling turns, and it confused its own pathfinder. The original Project Nublar kept server hitboxes in step with animation, and no other studied codebase did. It still moved a T. rex as a player-sized box.

For Project Nublar, the important decision is not which multipart library to use. The important decision is whether any gameplay rule needs the server to know the animated pose.

- **If no rule needs it:** client-animated parts over a server rest pose are the field standard. Put the effort into navigation and herd spacing, where the lead's actual symptom lives.
- **If a rule needs it:** the cheapest server truth is a procedural estimate fed into an existing hook. A GeckoLib evaluator on the server costs more.
