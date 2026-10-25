# Original Project Nublar electric fences (Minecraft 1.12.2): functional inventory

Sources (read-only research, 2026-10-10)
- 1.12.2 code: repo Dumb-Code/ProjectNublar, branch `master`, commit `e525ed7b69aaaa9de5234de9875f50d3822a25af` (2021-04-30). Raw base: https://raw.githubusercontent.com/Dumb-Code/ProjectNublar/e525ed7b69aaaa9de5234de9875f50d3822a25af/
- 1.16 code: branch `1.16` (repo default branch), commit `32f3a45caac7197e7a7a45d04c77d121ea4c74b0` (2023-09-21).
- Wiki: https://github.com/Dumb-Code/ProjectNublar/wiki/Fences (raw: https://raw.githubusercontent.com/wiki/Dumb-Code/ProjectNublar/Fences.md). Written by Wyn Price (dev blog style). It describes maths, rendering and break visuals. It has NO numbers for damage, power, span or recipes.
- Paths below are under `src/main/java/net/dumbcode/projectnublar/` unless shown. `M@` = master SHA above. Labels: [v] read in code, [doc] wiki only, [inf] inferred.
- Line numbers are per-file at `M@`.

## 1. Class map (all [v])
| Class | Role |
|---|---|
| `server/block/BlockElectricFencePole` | Pole block (stacked column), wire placement logic, power light, break logic |
| `server/block/BlockElectricFence` | Invisible "wire block" (`electric_fence`); holds wires; `ITEM_FOLD=20`; spark display tick |
| `server/block/BlockConnectableBase` | Shared base: collision, ray trace, selection outline, break/repair/sign, damage, ladder |
| `server/block/entity/BlockEntityElectricFence`, `...FencePole`, `ConnectableBlockEntity` | Tile entities; the pole one stores energy |
| `server/utils/Connection`, `ConnectionType`, `EnumConnectionType`, `LineUtils`, `RotatedRayBox` | Wire data, per-type constants, block line walk, rotated ray box |
| `server/item/WireSpoolItem`, `ItemFenceRemover`, `CreativeFenceRemovers` | Spool (ItemBlock of `electric_fence`), two removers |
| `server/entity/ai/EntityAttackFenceAI`, `...component/impl/ai/AttackFenceComponent` | Dinosaur fence-breaking AI |
| `client/render/blockentity/BlockEntityElectricFenceRenderer`, `...PoleRenderer`, `client/render/FenceStateMapper` | Wire and sign render, pole state mapping |
| `server/particles/SparkParticle`, `server/entity/DamageSourceHandler` | Sparks, damage source |
| `server/command/RegenFenceCacheCommand` | `/projectnublar regenfence` (op level 2) rebuilds wire caches |

Pylons (`BlockPylonHead`, `BlockPylonPole`, `ItemPylonPole`) reuse the wire spool but are a separate power-network feature, not fences. Not covered.

## 2. Feature table
| # | Feature | What player sees / does; exact values | Evidence | Label |
|---|---|---|---|---|
| 1 | Post types | Two poles: `low_security_electric_fence_pole`, `high_security_electric_fence_pole`. Creative tab only (see #37). | `server/block/BlockHandler.java:28-29,57-58` | [v] |
| 2 | Pole height | Low = 3 blocks tall, high = 8 blocks tall. Placing needs all `height` blocks above to be replaceable; the column is filled on placement (index property 0..h-1). | `server/utils/EnumConnectionType.java:33-34`; `BlockElectricFencePole.java:107-113,172-179` | [v] |
| 3 | Wires per type | Low: 2 wires per block-height x 3 = 6 wires (y offsets 0.25 and 0.75 in each block). High: 1 wire per block-height x 8 = 8 wires (y offset 0.5). Offsets = (2i+1)/(2*amount). | `EnumConnectionType.java:33-34,54-57`; `BlockElectricFencePole.java:246-264` | [v] |
| 4 | Wire thickness | Ray/selection half-thickness = cableWidth/32: low 0.75/32 (about 0.047 block full, under 1 px), high 2/32 (0.125 block, 2 px). | `EnumConnectionType.java:33-34,48`; `Connection.java:100-105` | [v] |
| 5 | Pole footprint (collision/selection) | Thin post offset from block centre along a rotated axis. Low: radius 6/16, half-size 1/8 (0.25 x 0.25). High: radius 1/2, half-size 2/8 (0.5 x 0.5; overhangs the block edge). 1 block high per column block. | `EnumConnectionType`; `BlockElectricFencePole.java:78-85` | [v] |
| 6 | Pole rotation / yaw | Yaw is computed, not chosen. 1 distinct connection: wire angle + 90. 2+: bisector of the first two distinct connections (distinct by from/to pair). Plus type offset (low 90, high 0), plus 180 if flipped. Stored as int 0-359 state `rotation`. | `BlockElectricFencePole.java:116-164`; `FenceStateMapper.java:19-29`; wiki "Pole Rotations" | [v][doc] |
| 7 | Flip pole 180 | Right-click the base pole with an EMPTY hand: toggles `rotatedAround` and refreshes all column blocks. Wiki: needed because the correct side is ambiguous. | `BlockElectricFencePole.java:182-196`; `BlockEntityElectricFencePole.java:34-35,46`; wiki | [v][doc] |
| 8 | Wire spool item | `wire_spool` is an `ItemBlock` of `electric_fence`. 12 sprite stages (`spool_00..11`) driven by predicate `projectnublar:distance` (thresholds 0, .083 ... .916). | `server/item/ItemHandler.java:46,91`; `WireSpoolItem.java:30-68`; `assets/.../models/item/wire_spool.json` | [v] |
| 9 | Spool item animation | While a first pole is stored (`fence_position`, main hand only), the spool shows how much of the stack the pending span would use: dist(player, pole1) x poleHeight / (20 x stackCount), capped at 1. Walks stacks in inventory order. | `WireSpoolItem.java:30-68` | [v] |
| 10 | Placement by spool | Right-click pole A with spool: stores A (any column block redirects to the base). Right-click pole B: if B is the same pole block type (low-to-low or high-to-high), B != A and in range, the fence is built, then B is stored (chain placing). Click A again, or a different pole type: stored pos is cleared. | `BlockElectricFencePole.java:197-274` (type check :207) | [v] |
| 11 | Max span | LIMIT = 15 blocks, 3D Euclidean distance between base pole positions. If more: action-bar message `projectnublar.fences.length.toolong` ("%sm is too long. The max distance is %sm") and B becomes the new stored pole. | `BlockElectricFencePole.java:50,200-206`; `assets/.../lang/en_us.lang:44` | [v] |
| 12 | Wire cost | Spools needed = ceil(dist / 20 x poleHeight) (operator precedence: (dist/20)*height). Low at 15 blocks = 3 spools; high at 15 = 6. NOT per wire. Taken from the held stack first, then other spool stacks in main inventory. Creative: free. Not enough: message `projectnublar.fences.length.notenough` ("Not enough cable. Need a total of %s cable. (You have %s)") and nothing is built. | `BlockElectricFencePole.java:209-245`; `BlockElectricFence.java:35`; lang :45 | [v] |
| 13 | Incline / any direction | Poles may differ in Y and in any XZ direction. Wire path = vanilla-style block ray walk from pole centre to pole centre (per wire offset) plus a per-block 3D intersection. | `server/utils/LineUtils.java:13-146`; wiki "Inclines" | [v][doc] |
| 14 | Auto-filled wire blocks | For every wire, layer and block on the line: if air or replaceable, place invisible `electric_fence`, then add a `Connection` to its tile entity. Non-replaceable blocks on the line get no wire (gap). No line-of-sight or obstruction check. | `BlockElectricFencePole.java:246-264` | [v] |
| 15 | Wire look | Wire block render type INVISIBLE; wires drawn by the tile-entity renderer from display lists; texture `textures/blocks/electric_fence.png`; unlimited render distance. | `BlockElectricFence.java:90-92`; `BlockEntityElectricFenceRenderer.java:19-30,76-102`; `BlockEntityElectricFence.java:48-56` | [v] |
| 16 | Wire collision | Solid to entities: 16 small AABBs per wire per block along the 3D segment, each grown by cableWidth/2 on Y only; uses vanilla collision. Wiki: earlier "custom box" idea was rejected as buggy. Broken wires keep their boxes (no broken filter) [v]. Collision can be toggled off (used by sparks). | `BlockConnectableBase.java:69-79,355-371,595-601`; wiki "Collision" | [v][doc] |
| 17 | Per-wire ray trace / selection | Each wire is a rotated box. `RotatedRayBox` rotates ray start/end into box space, then vanilla `calculateIntercept`. Nearest hit wins; hit stores a `HitChunk` (wire + side). Broken wires cannot be hit. Highlight outlines only the hovered wire (or half a box next to a break). Break/place logic uses `rayTraceEyes(player, 7)`. | `BlockConnectableBase.java:172-249,289-353,402-423`; `utils/RotatedRayBox.java:37-`; wiki "Raytracing Collision" | [v][doc] |
| 18 | Break one wire | Left-click a wire: only the hovered wire in that block gets `broken=true`, break effect (event 2001) plays, block stays. When no live wire is left, the block breaks normally. No `setHardness` call, so hardness is default 0 (instant) [inf]. | `BlockConnectableBase.java:402-419`; `BlockHandler.java:57-59` | [v][inf] |
| 19 | Broken-wire visuals | A wire whose neighbour segment is broken (or has no tile entity) renders half fixed and half rotated by random yaw and pitch within +-30 degrees (PI/3 x (rand-0.5)), so the cable hangs bent. Seed = block pos x offset x 1000 (stable). Wiki says the seed uses the y offset; code uses block position (differs per layer). | `Connection.java:98,116-141,184-196`; `BlockEntityElectricFenceRenderer.java:76-102`; wiki "Cable breaks" | [v][doc] |
| 20 | Break sparks | Only on powered, unbroken wires (client display tick). Previous side broken: 8 SPARKS EVERY tick. Next side broken: 2 % chance per tick (the expression `pb \|\| nb && rand<0.02` makes the previous side spam; likely bug). Spark: gravity 0.75, scale 0.25, lifetime/4, motion = dir x0.075 + rand(+-0.02), full-bright 0xF000F0, texture `particles/map.png`. Wiki: "random intervals", glow in dark. | `BlockElectricFence.java:42-67`; `particles/SparkParticle.java:17-60`; `ProjectNublar.java:207-215` | [v][doc] |
| 21 | Repair / replace wire | (a) Right-click a wire's top/bottom face: repairs the nearest broken wire above/below in that block. (b) Right-click a wire END (east/west of ray box): places or extends the neighbouring wire block and un-breaks the matching wire. (c) `RightClickBlock` event: using a spool on a block face next to a wire block un-breaks a broken wire there (Y faces choose highest/lowest, other faces the first broken). Each repair consumes 1 spool (`placeEffect` shrinks the held stack by 1 in survival) and plays the block place sound at pitch x0.8. | `BlockConnectableBase.java:385-394,422-487,555-593` | [v] |
| 22 | Spool placed as block | Placing the spool as a block beside wires copies the connections whose path crosses that cell (3x3x3 scan) and marks all broken except one reference wire; if none, the block is removed. Placing on a wire's east/west end uses the next/previous cell. | `WireSpoolItem.java:72-98`; `BlockConnectableBase.java:489-547` | [v] |
| 23 | Voltage warning sign | Right-click a wire with a non-spool item or empty hand: toggles `sign` for that wire in that block. Renders a double-sided `voltage_warning.png` quad (1.0 wide x 17/32 high) at the wire centre, tilted with the incline. Saved in NBT. | `BlockConnectableBase.java:478-482`; `BlockEntityElectricFenceRenderer.java:32-74`; `Connection.java:160` | [v] |
| 24 | Break a pole | Breaking any column block removes the whole column (static `destroying` guard). If the OTHER end pole no longer exists, all wires between are marked broken, and wire blocks with no live wire are removed (no drops). If the other pole still exists, wires stay untouched, now unpowered, with bent ends (missing tile counts as a broken side). So breaking the first pole leaves hanging wires; breaking the second clears them. | `BlockElectricFencePole.java:282-325`; `Connection.java:184-196` | [v] |
| 25 | Fence remover | `fence_remover`: right-click a wire: breaks the whole wire line (from to to, same offset) in every block, removes wire blocks left with no live wire; poles are skipped (`removedByFenceRemovers=false`). | `server/item/ItemFenceRemover.java:7-46`; `BlockEntityElectricFencePole.java:87-90` | [v] |
| 26 | Creative fence remover | `creative_fence_remover`: right-click a wire: gathers the whole connected fence network from that pole column and sets every block on all those lines to air (this includes the pole end blocks). | `server/item/CreativeFenceRemovers.java:19-52` | [v] |
| 27 | Power source and storage | The base pole tile (index 0) holds a Forge Energy storage: capacity 350, maxReceive 350, maxExtract 250. Every column block exposes the base storage as `CapabilityEnergy.ENERGY` (all faces). Any FE source can feed it. `BlockCreativePowerSource` pushes max energy to all 6 neighbours every tick. Other sources (coal generator) [inf]. | `block/entity/BlockEntityElectricFencePole.java:37,93-119`; `block/BlockCreativePowerSource.java:61-72` | [v][inf] |
| 28 | Power drain | Each pole tile loses 10 FE per tick (`extractRaw(10)`). A full 350 pole lasts 35 ticks (1.75 s) without resupply. | `BlockEntityElectricFencePole.java:130` | [v] |
| 29 | Power propagation | A base pole with more than 300 FE shares up to 300 FE per tick, split evenly over the poles at the other end of its level-0 wires, lowest stored first. Chains of poles get powered. | `BlockEntityElectricFencePole.java:143-158` | [v] |
| 30 | "Wire is live" rule | `Connection.isPowered`: EVERY block along the wire must exist with an unbroken matching wire AND at least one end pole must hold energy > 0. One broken or missing wire block makes that whole wire unpowered. | `server/utils/Connection.java:198-232` | [v] |
| 31 | Powered visuals | Pole `powered` state = energy > 0, set on all column blocks. The top block (index h-1) uses the `_upper_powered` model with a lit "PoleLight" cube and emits light: low 10, high 15; else 0. | `BlockEntityElectricFencePole.java:122-141`; `BlockElectricFencePole.java:328-330`; `assets/.../models/block/*_upper_powered.json`; `EnumConnectionType.java:33-34` | [v] |
| 32 | Damage | Any entity (player, mob, dinosaur) touching a powered wire (hitbox grown 0.1 on all axes) takes `attackEntityFrom(FENCE_ELECTRIC, 1F)` on the server. `FENCE_ELECTRIC` = `DamageSource("electric_fence").setDamageBypassesArmor()`. 1 HP = half a heart; vanilla invulnerability frames limit the rate (about 2 HP/s) [inf]. No death message in lang. | `BlockConnectableBase.java:108-158` (damage :127); `entity/DamageSourceHandler.java:6` | [v][inf] |
| 33 | Shock knockback / effect | Velocity is overwritten, not added: horizontal = unit vector from wire centre to entity centre; Y = vec.y x 0.3; if the entity is airborne the vector is scaled x0.4 first. Burst of 15 SPARKS (3 x 5, gaussian spread) at the contact point per hit. Only the first intersecting box applies. | `BlockConnectableBase.java:119-155` | [v] |
| 34 | Unpowered behaviour | Wires stay solid. No damage. Unpowered wires act as ladders (`isLadder` true when touching the side, false when powered), so players can climb them. Code force-sets `ForgeModContainer.fullBoundingBoxLadders=true` (TODO about a config). | `BlockConnectableBase.java:82-105` | [v] |
| 35 | Pathfinding | Wire blocks and poles return `PathNodeType.BLOCKED`, so mobs path around them. | `BlockConnectableBase.java:160-164` | [v] |
| 36 | Dinosaur fence AI | `EntityAttackFenceAI` (priority 1) is attached ONLY to Tyrannosaurus (`ComponentHandler.ATTACK_FENCE_AI`). Each check has a 50 % roll. Within a 10-block XZ scan it finds a wire block with an UNPOWERED wire, paths to it (speed + 0.1), and when within 1 block calls `breakFence(6)` once (`hasBrokenFence`). `breakFence`: per wire run, the middle block's column up to 6 high is destroyed WITH drops; the two neighbours up to 4 high. Powered fences are ignored. | `entity/ai/EntityAttackFenceAI.java:37-80`; `block/entity/BlockEntityElectricFence.java:73-89`; `dinosaur/Tyrannosaurus.java:54`; `entity/component/impl/ai/AttackFenceComponent.java:7-20` | [v] |
| 37 | Recipes | None found for poles, spool or removers in `assets/projectnublar/recipes/` (only `empty_filled_syringe.json`) or `VanillaCraftingHandler`. Items sit in the mod creative tab. | repo tree at `M@` | [v: absence] |
| 38 | Sounds | No custom fence sounds in `sounds.json`. Place sound = SoundType place sound of `electric_fence` (pitch x0.8). Break effect = vanilla break event 2001. | `BlockConnectableBase.java:385-398` | [v] |
| 39 | Particles | Only SPARKS (`ParticleType.SPARKS`), sent from server by packet `S21SpawnParticle`. | `particles/ParticleType.java:8`; `ProjectNublar.java:207-215` | [v] |
| 40 | Config | No fence config options. Hard-coded: LIMIT 15, ITEM_FOLD 20, damage 1, pole energy 350/350/250, drain 10, share 300. | repo tree | [v: absence] |
| 41 | Gates | No gate, door or open-able wire in the 1.12.2 code. | repo tree | [v: absence] |
| 42 | Lang | Only two fence strings in `en_us.lang` (the two messages in #11 and #12). No block or item names, no death message. | `assets/projectnublar/lang/en_us.lang:44-45` | [v] |
| 43 | Command | `/projectnublar regenfence` (permission 2): clones every loaded wire to rebuild caches and sends `S20RegenCache`. | `command/RegenFenceCacheCommand.java:33-50`; `CommandProjectNublar.java:11` | [v] |
| 44 | Debug | `ProjectNublar.DEBUG=false`. When true, the highlight shows the rotated ray box. | `ProjectNublar.java:71`; `BlockConnectableBase.java:199-201` | [v] |

## 3. Known bugs / TODOs in the 1.12.2 code [v]
- Spark spam: `pb || brokenSide(next) && rand<0.02` precedence (`BlockElectricFence.java:55`).
- Broken wires keep collision boxes (`BlockConnectableBase.java:355-371`).
- Breaking the first pole leaves orphan wires (#24). `TODO: verify if our block?` at `BlockElectricFencePole.java:318`.
- `RegenFenceCacheCommand.getUsage` returns "" with TODO (line 25).
- `breakFence` "TODO: Add more randomness" (`BlockEntityElectricFence.java:73`).
- `fullBoundingBoxLadders` forced on, "TODO: set a config" (`BlockConnectableBase.java:83`).
- "Expensive calls ahead. Maybe try and cache them?" in `getActualState` (rotation recomputed on every query, `BlockElectricFencePole.java:117`).
- Spool distance predicate works for the main hand only.

## 4. Differences in the 1.16 branch (one line each; `32f3a45`) [v]
1. Pole yaw is no longer a blockstate property: the pole tile computes it (`computeRotation`) and passes it to a baked model via model data; NBT key `rotation_flipped`; field `flippedAround` (`BlockEntityElectricFencePole.java:34-229`).
2. Pole collision shape is a cached `VoxelShape` from that rotation (`BlockEntityElectricFencePole.java:148-163`).
3. Wire collision uses 8 boxes per wire per block (was 16), inflated on all axes by cableWidth/2 (was Y only) (`BlockConnectableBase.java:388-408`).
4. Wire collision and selection go through a `DelegateVoxelShape`; `getHitChunk(viewer)` picks at range 20 (was `rayTraceEyes(.,7)`) (`BlockConnectableBase.java:293-322`).
5. Shock effect: 30 SPARK particles via `ServerWorld.sendParticles` (was 15 client-spawned). Damage still `FENCE_ELECTRIC` 1F; same velocity formula (`BlockConnectableBase.java:97-136`).
6. Break sparks: still 8 per tick; skipped if the direction vector is zero (`BlockElectricFence.java:26-55`).
7. Unchanged numbers: ITEM_FOLD 20, LIMIT 15, 3-high/2-wire and 8-high/1-wire types, energy 350/350/250, drain 10, share 300, light 10/15 (`BlockElectricFencePole.java:53`; `EnumConnectionType.java:9-10`; `BlockEntityElectricFencePole.java:46,113,126`).
8. Spool stored positions are NBT compounds (`fence_position` TAG_COMPOUND); item property registered via `ItemModelsProperties` (`WireSpoolItem.java:64-104`).
9. Pole tile now extends `BlockEntityElectricFence` (inherits wires and `breakFence`); energy exposed through `LazyOptional`.
10. Wire texture UVs random from `nextInt(65 - maximumTexSize)/64` (64-px texture; was `nextInt(16)/16`) (`Connection.java:385`).
11. Blocks use `Material.HEAVY_METAL` and `noOcclusion`; `isPathfindable` false; ladders via `ForgeConfig` (`BlockHandler.java:32-34`; `BlockConnectableBase.java:75-95,139`).
12. Lang gained names: "Fence Remover", "Creative Fence Remover", "Wire spool" (`item.projectnublar.electric_fence`) (`en_us.json`).
13. AI, removers, repair, sign and break logic are otherwise 1:1 ports (diff shows only API renames).
Not diffed in depth on 1.16: `Connection` render cache, `FenceBakedModel`, pylons.

## 5. Could not determine
- Whether any recipe exists outside repo resources (none in repo).
- Real in-game damage rate; whether broken wire/pole drops follow default block drops (no loot tables; [inf] 1 spool when the last wire of a block breaks).
- Exact block hardness (inferred 0 since no `setHardness`).
- Whether any dinosaur other than Tyrannosaurus attacks fences (only it adds the component).
- How the coal generator feeds poles (`MachineModuleBlockEntity.updateEnergyNetwork` not read in full).
- The wiki gives no numbers; all numbers above come from code.
