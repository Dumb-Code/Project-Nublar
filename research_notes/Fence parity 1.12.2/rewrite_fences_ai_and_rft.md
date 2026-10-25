# Electric fence in the 1.20.1 rewrite: inventory of two code lines

Scope: read-only static review. Nothing was run in game. "Works" means the code path exists and reads correctly; it is not a play test. Items marked [inf] are my inference from reading code.

Code lines:
- **ai** = working tree on branch `kash's-slop` (based on `1.20.1-ai` 4244f62), read from disk. Paths are relative to `common/src/main/java/net/dumbcode/projectnublar/` ("P/") unless they start with `forge/`.
- **rft** = `origin/rft/adversing` (tip 97023cf), read with `git show`. Merge base with ai is 55e1968. Packages differ: `block/api/fence/*` (Connection, BlockConnectableBase, EnumConnectionType, FencePowerService), `block/api/geometry/*` (RotatedRayBox, MathUtils, RenderUtils), `registry/*` instead of `init/*`, `entity/dinosaur/ai/*` instead of `entity/ai/*`.
- Fence files on ai have NOT changed since 55e1968 (git diff is empty for Fence*, Connection, BlockConnectableBase, LineUtils, BreakFenceBehaviour). So "rft vs merge base" equals "rft vs ai" for the fence, except the dinosaur pathing files, which ai has changed in its working tree (section 6).
- Line numbers: for ai they are in the named file; for rft they are in the rft version of that file.

## 0. Inventory of files

| Item | ai | rft |
| --- | --- | --- |
| Post blocks | `P/block/ElectricFencePostBlock.java` (307 lines), `LowSecurityElectricFencePostBlock`, `HighSecurityElectricFencePostBlock` (index property 0..h-1) | same, rewritten (473 lines) |
| Wire block | `P/block/ElectricFenceBlock.java` | same, logic unchanged |
| Base class | `P/block/api/BlockConnectableBase.java` (657) | `P/block/api/fence/BlockConnectableBase.java` (724) |
| Segment data | `P/block/api/Connection.java` (584), `ConnectionType`, `EnumConnectionType` (75) | `fence/Connection.java` (898), `EnumConnectionType` (110), new `FencePowerService.java` (100) |
| Geometry | `P/block/api/RotatedRayBox.java`, `MathUtils`, `DelegateVoxelShape`, `DelegateBlockHitResult`, `RenderUtils`, `P/util/LineUtils.java` | moved to `geometry/` (RotatedRayBox changed by 2 lines only) |
| Block entities | `P/block/entity/BlockEntityElectricFence`, `...Base`, `...Pole` | same names, rewritten |
| Renderers | `P/client/renderer/ElectricFenceRenderer.java` (post, Geo), `ElectricWireRenderer.java` (wire block); model `P/client/model/FencePostModel.java`; registered at `P/client/ClientRegistrationHolder.java:95-96` | same classes; renderers changed |
| Forge-only | `forge/src/main/java/net/dumbcode/projectnublar/event/CommonForgeEvents.java:38-76` (spool repair); mixins `BlockEntityElectricFenceMixin`, `BlockEntityElectricFencePoleMixin`, `BlockElectricFencePoleMixin` (empty) | repair kept; empty mixin deleted; others only repackaged |
| Item | `ItemInit.WIRE_SPOOL` = plain `new Item(...)` (`P/init/ItemInit.java:60`) | same |
| Dinosaur side | `P/entity/ai/FenceAwareNavigation`, `FenceAwareNodeEvaluator`, `behaviour/actions/BreakFenceBehaviour`, `P/util/DinoNeedsUtils`, `Dinosaur.java` sensors | same names under `entity/dinosaur/ai/**`; BreakFence and NodeEvaluator differ |
| Power source | `P/block/GeneratorBlock.java`, `P/block/entity/GeneratorBlockEntity.java` (coal and creative), Botarium energy | same; refactored, not fence-specific |

Registered (`P/init/BlockInit.java:35-38,51-54`): blocks `electric_fence` (`noLootTable().noOcclusion()`), `low_security_electric_fence_post`, `high_security_electric_fence_post`; block entity types `electric_fence` and `electric_fence_pole` (the pole BE serves both post blocks). Creative tab lists both posts and the spool (`P/init/CreativeTabInit.java:100-102`). Post items are plain `BlockItem`.

## 1. Constants

| Value | Low security | High security | Evidence (same on both lines unless noted) |
| --- | --- | --- | --- |
| Post height (blocks) | 3 | 8 | ai `block/api/EnumConnectionType.java:8-9`; rft `fence/EnumConnectionType.java:30-44` |
| Wires per height layer | 2, at 0.25 and 0.75 of a block | 1, at 0.5 | offsets built in the enum constructor |
| Total wire strands per run | 6 | 8 | derived |
| Cable half-width | 0.75/32 = 0.0234 block | 2/32 = 0.0625 block | field `cableWidth = x/32`; the code comment says it is half width |
| Pole radius from block centre | 6/16 | 1/2 | `radius` |
| Pole hitbox half-size | 1/8 | 2/8 | `halfSize` |
| Default yaw offset (degrees) | 90 | 0 | `rotationOffset` |
| "Light level" | 10 | 15 | `lightLevel` (see 2.8: not emitted) |
| Max run length | 15 blocks, 3-D distance | same | `ElectricFencePostBlock.LIMIT`, ai :57, rft :64 |
| Spool cost | `ceil(dist / 20 * height)` | same | `ElectricFenceBlock.ITEM_FOLD = 20` (ai file line 30); formula ai PostBlock :145, rft :191. At 15 blocks: low 3 spools, high 6. Cost ignores wire count per layer. |
| Pole energy buffer | 350 FE, insert only, 350 per transfer | same | ai Pole BE :225; rft consts :42-47 |
| Pole drain | 10 FE per tick while stored > 0 | same | ai Pole BE :111; rft :137 |
| Pole to pole sharing | when stored > 300, shares 300 FE per tick among linked poles | same | ai :124-141; rft :141-201 |
| Shock damage | 1.0, THORNS type, no attacker | same | ai `BlockConnectableBase.java:91`; rft :80 |
| Collision boxes per wire per block | 8 (the analysis doc says 16) | 8 | `double amount = 8;` ai :396, rft :229; each box inflated by cableWidth/2 |

## 2. Features

Status words: works / partial / stub / commented out / missing.

### 2.1 Placement of posts
- Column: placing the base block creates the upper blocks with `index = 1..h-1` in `onPlace`. ai PostBlock :104-112; rft :114-122.
  - ai: runs on client and server, and again on any state change of the same block (for example a `powered` toggle) [inf: can overwrite the upper blocks]. Status: works, fragile.
  - rft: server only, and skipped when the old state is the same block. Status: works.
- Survival: all `height` blocks from the base up must be replaceable (ai :88-94, rft :98-104). Both. Nothing requires a solid floor.
- Blockstate: `powered` (boolean) plus `index`. State names are frozen in rft's javadoc.

### 2.2 Pole yaw and flip
- Auto yaw from the first one or two distinct runs of the base pole.
  - ai `BlockEntityElectricFencePole.computeRotation` (ai :156-210 of that file): `atan` of the intersection numbers of the first connection (single run), or the mean of two `MathUtils.horizontalDegree` angles; adds `rotationOffset`, +180 if flipped; no wrap to 0..360.
  - rft `computeRotation` :228-293: `atan2` on base positions, runs averaged into 0..180, offset and flip added, normalised to 0..360. Formula differs, so some poles may face differently [inf].
- Flip: empty-hand right click on the base post toggles `flippedAround` (+180 degrees). ai PostBlock :118-132 (the loop calls `fencePole.triggerModelUpdate()` on the base entity for each height, not on each block `t`); rft :130-137 and `flipPole` :155-164 (updates each column pole; server only).
- Clicks on upper blocks go to the base block (ai :209-211, rft :147-150).
- Render: Geo model `geo/block/<post>.geo.json`, only the index 0 block draws it, rotated about the block centre by `getCachedRotation()`. ai `ElectricFenceRenderer.actuallyRender` :46-59 (an if/else with two identical branches); rft :36-48. `FencePostModel.getAnimationResource` returns null, so there is no post animation.
- Pole selection box: `cachedShape` = a box at `radius` from the centre in the yaw direction, set in `forge/.../mixin/BlockEntityElectricFencePoleMixin.java:44-58`. Collision falls back to a fixed, unrotated `DEFAULT_SHAPE` when the pole has no wires (ai Pole BE :91,:147-153; rft :111,:219-225) [inf: does not match the drawn pole].
- `triggerModelUpdate` is empty in common code; it works only through the Forge mixins (Fabric risk). Both lines.

### 2.3 Wire spool placement
Flow (both): right-click post A with a spool stores A in item NBT `projectnublar.fence_position`. Right-click post B (same block type, not the same block) checks distance, counts spools, strings all wires, then stores B as the new first post (so a player can chain).
- **Span**: 3-D distance up to 15. Above 15: ai shows translatable `projectnublar.fences.length.toolong` (ai :138-142); rft shows the literal English "Fence run is too long (n/15 blocks)" (rft :176). The stored post becomes the new click either way.
- **Directions and incline**: any direction. The run follows `LineUtils.getBlocksInbetween(from, to, offset)` (`P/util/LineUtils.java:42-158`), a voxel walk from block centre to block centre at the wire offset, so runs between different base heights work. Each height layer `i` is its own run from `from.above(i)` to `to.above(i)`. Works (both). Low and high posts do not link to each other (`getBlock() == this`).
- **Auto-filled wire blocks**: for each offset, layer and cell on the line, an `ElectricFenceBlock` is set if the cell is air or replaceable and not a post (ai :189-191; rft `placeWireConnection` :253-271), then a `Connection(be, type, offset, from, to, previous, next, position)` is added to that cell's block entity.
- **Obstruction**: a solid block on the line is not an error. No wire block is placed in that cell, no connection is made there, and the spools are already spent. The run has a gap; neighbours show broken ends, and the run is unpowered. No message. Both [inf from code]. rft only adds a guard that stops before spending spools when every cell already has a live matching wire (`hasWirePlacementWork`, :193 and :214-230).
- **Consumption**: held stack first, then other stacks in the main inventory (`getInventory().items`; not offhand). Creative does not consume. If short, the player sees `projectnublar.fences.length.notenough` with needed and found counts (ai :174-177; rft :199).
- **Duplicates**: ai: stringing the same pair twice adds a second identical connection to every cell (identity equality on a `LinkedHashSet`) [inf]. rft: `Connection.equals/hashCode` (offset, position, from, to) plus `addConnection` merge; a repeat run un-breaks a broken connection (`BlockEntityElectricFenceBase.addConnection` :43-57).
- **Lang**: neither `projectnublar.fences.length.toolong` nor `.notenough` exists in `common/src/generated/resources/assets/projectnublar/lang/en_us.json`. The player sees the raw key (ai both messages; rft the not-enough message). Partial.
- **Spool item**: plain `Item`, no recipe (the generated recipes folder and `ModRecipeProvider` only have `unincubated_egg`); creative tab only, plus rft's floating-wire drops. Item animation (demo caption): missing on both.

### 2.4 Collision, selection and per-wire ray trace
- Collision shape = union of each connection's 8 boxes. ai `BlockEntityElectricFence.getOrCreateCollision` :76-86 includes broken connections [inf: invisible solid boxes]. rft `BlockEntityElectricFenceBase.getOrCreateCollision` :85-97 skips broken ones and caches boxes (`getOrCreateCollisionBoxes` :100-110).
- `setCollidableClient/Server` flags exist (ai :636-642) but no fence code uses them. `isPathfindable` is false (both).
- Selection: `getShape` returns a `DelegateVoxelShape` whose `clip` runs `getRaytraceResult`, a per-wire ray trace against a `RotatedRayBox` aligned to the wire (length `fullLen`, half-thickness = cable width). ai BlockConnectableBase :254-256, :297-310, :330-375, `getOutlines` :377-390; rft :163, :207. Each wire is targetable individually; nearest hit wins; the pole shape is a second candidate. Broken connections are skipped; a broken side adds a rotated and a fixed stub box so the bent stub can be targeted (ai :341-355). Works (both).
- Outline drawing: the custom highlight (`onDrawBlock`, draws only the hit wire and stubs) is commented out in ai (`BlockConnectableBase.java:127-199`) and absent in rft. Players get the default delegate-shape outline [inf].
- Ladder climbing on wires: commented out in ai (:48-71), absent in rft.

### 2.5 Breaking one wire versus a post
- **Player breaks a wire block**: `playerWillDestroy` marks the hit connection broken and plays the break effect (ai :449-465; rft :415-436, server only, then the cascade). Partial: vanilla then removes the whole block after this call, so the other connection(s) in the same block entity go too [inf, needs a play test]. The wire block has `noLootTable()`, so a direct break drops nothing.
- **Neighbours**: a missing cell or flagged connection makes `Connection.brokenSide` true for adjoining segments, so they render and select as bent stubs (rft only for rendering).
- **Break a post**: any block of the column removes the whole column and breaks the wires.
  - ai `onRemove` :216-266: for each connection on the removed block entity, walks the run, marks end connections broken, flags wire cells, sets cells with no live connection to air. Condition `... != this || true` (forced true, :224). Also runs on the client.
  - rft `onRemove` :324-337, `breakRunsForRemovedColumn` :339-350, `breakRunForRemovedEndpoint` :374-406: gathers connections from every column block, flags matching connections silently, removes empty wire cells, syncs once, then removes the column. Server only.
- **Cascade (rft only)**: `breakUnsupportedFloatingConnections` (rft :277-366). After a break it walks live neighbour cells; a live component with no cell at a post position (`position == from || to`) is marked broken and drops one `wire_spool` per cell (`popResource`, :307). On ai a cut-off wire stays as a straight wire (see 2.6).
- Dinosaur break: section 6.

### 2.6 Broken-wire visuals
- **ai**: both renderers always draw `connection.getRenderData().data()` (full straight wire), ignoring `broken` (`ElectricFenceRenderer.java:25-44`; `ElectricWireRenderer.java` `render`; the `isPowered` conditions are commented out). `Connection.compileRenderData` (bent and half variants, `Connection.java:296-320`) is only used by the Forge mixin `getModelData()`, and nothing consumes `ProjectNublarModelData.CONNECTIONS` (grep) [inf]. Broken wires and bent stubs are invisible in game. Partial: data exists, not drawn.
- **rft**: renderers draw `compileRenderData(level)` arrays (`ElectricWireRenderer.drawWire`). A broken connection returns an empty list; both sides broken, nothing; one side broken draws the rotated (bent) half plus the fixed half (`Connection.collectRenderData` :450-473). Results are cached by a hash of (broken, sign, prevBroken, nextBroken) (:435-448). Works.
- **Bend**: ai: random yaw and pitch of +-30 degrees, seeded by position and offset (`genCache` :159-191). rft: up to PI/7 (about 25.7 degrees), at least 25% of the maximum, biased away from the other strands, plus random sideways part (`buildBrokenPoint` :234-260, constants :67-69). Both clip the stub to the block.
- **Sparks at break**: `ElectricFenceBlock.animateTick` (file lines 52-80, same on both) spawns 8 `ELECTRIC_SPARK` particles at the bent end but first needs `connection.isPowered(world)`. `isPowered` is false when any cell of the run has a broken matching connection (ai `Connection.isPowered` :258-293; rft `FencePowerService.isRunComplete` :31-45). A neighbour with a broken connection is in the same run, so the connection is unpowered whenever `brokenSide` is true, and the sparks never fire [inf from logic]. Operator slip too: `pb || (nb && rand < 0.02)`. Stub / dead on both.
- **Sign flag**: `Connection.sign` toggles on right-click with a non-wire item and is stored, but nothing renders a sign. Stub.

### 2.7 Repair
1. **Right-click a wire stub** (`BlockConnectableBase.use`; ai :469-532, rft :439-474 with helpers).
   - Hit on a Y face: finds the nearest broken wire above or below (by `from.y + offset`) and un-breaks it; `placeEffect` swings and shrinks the held stack by 1 in survival (ai :430-440; sound TODO :437). The item type is not checked [inf].
   - Hit on an X end: rebuilds the missing neighbour cell (places a wire block, `generateConnections` copies connections from live neighbours, marks all but one broken) or un-breaks the matching connection there. rft adds `EndpointRepairResult` so an already-live connection is handled without using the item, and picks the reference by `getCenter().y` (rft :521-693). rft also makes the new block entity the owner of generated connections; ai passes the neighbour entity.
   - Otherwise: block item in hand returns CONSUME; anything else toggles `sign`.
2. **Spool on a block next to a wire** (Forge only): `CommonForgeEvents.onRightClick` (`forge/.../event/CommonForgeEvents.java:38-76`; rft :44-107). With a spool in hand, takes the tile at `pos.relative(face)`; on a Y face repairs the broken wire nearest in Y, otherwise the first broken wire in that cell; cancels the event. rft adds a server-only guard and `setChanged`. Fabric has no handler.
- Repair cost is 1 item per repair, not tied to wire length.
- Post repair: place the post again and restring. rft's dedupe un-breaks old connections.

### 2.8 Power source and propagation
- **Source**: coal generator (256 FE, 16 FE per tick output, `BlockInit.java:39`) and creative generator (inserts 999999 per tick and calls `EnergyApi.distributeEnergyNearby(this, 256)`, ai `GeneratorBlockEntity.tick` :76-90). The pole is an insert-only Botarium `BotariumEnergyBlock`. [inf] Botarium moves energy between adjacent energy blocks, so a generator beside a pole feeds it; there is no cable or fuse item. The coal generator gains 4 FE per 20 ticks per fuel item (any item in the slot, no fuel check) against a pole drain of 10 FE per tick, so only the creative generator keeps a pole powered [inf].
- **Pole tick**:
  - ai (`BlockEntityElectricFencePole.tick`, file lines 94-144): runs on client and server. If stored > 0: extracts 10 FE and sets `powered=true` on every column block (only when it differs, only in the powered branch). Nothing ever sets `powered=false`. Base block only: when stored > 300, shares 300 FE per tick evenly among poles at the far end of its layer-0 connections, including incomplete runs.
  - rft (`tickEnergy` :129-144): server only, base block only. Extracts 10 FE if stored > 0; `syncPoweredBlockstates` sets `powered` both ways and clears connected power caches. One store per column (`getEnergyStorage` forwards upper blocks to the base, :382-412); a `NotifyingInsertOnlyEnergyContainer` triggers the sync on change. Sharing only over complete runs (`isRunComplete`).
- **Is a wire powered?** The run must be intact (every cell has a live matching connection and is loaded) and one end post must hold energy > 0.
  - ai `Connection.isPowered` :258-279: recomputed on every call (collision and display ticks); reads end block entities directly.
  - rft `FencePowerService.isPowered` :18-24: cached per block entity per game tick (`isConnectionPowered` :112-122); also checks the base pole of a column end (`endpointHasEnergy` :87-95).
- **Unpowered behaviour**: an unpowered wire is a solid collision that does not shock. There is no visual difference (the `isPowered` conditions in both renderers are commented out). Demo caption "Unpowering": partial.
- **Powered light**: `getLightBlock` returns `type.lightLevel` (10/15) on the top post block when powered (ai PostBlock :275-277; rft :415-417). In 1.20.1 `getLightBlock` is light absorption, not emission, and the Properties set no `lightLevel(...)`. The light is not emitted [inf, strong]. Stub / bug, both.
- **Config**: no config for any fence value. Missing.

### 2.9 Damage, knockback
- `BlockConnectableBase.entityInside` (ai :75-115; rft :67-98): for each collision box in the cell (box inflated by 0.1 around the entity), if the connection is powered: server side `hurt(THORNS, 1.0)`; both sides set the entity's delta movement to the unit vector from the wire centre to the entity centre with y scaled by 0.3, whole vector scaled by 0.4 when airborne. Then `break` (one connection per call). Works.
- Shock particles: commented-out `sendParticles(SPARK...)` (ai :93-101; rft deleted it). Sound: none. Missing.
- Dinosaurs take the same shock as any entity.

### 2.10 High security and gates
- High security = separate block type with the same code and different constants (section 1). Works.
- Gates, doors, extra high-security parts (demo caption "High security fences"): missing on both.

### 2.11 Sounds, particles, recipes, config, sync
- Sounds: none for fences (`sounds.json` has entity sounds only); `breakEffect` uses vanilla `levelEvent(2001)`.
- Recipes: none for posts, spool or generators. Loot: posts have generated loot tables; wire block has none; rft drops spools for floating wire only.
- Config: none.
- Sync: `SyncingBlockEntity` update tags plus `sendBlockUpdated`. NBT per connection: `id`, `offset`, `from`, `to`, `sign`, `next`, `previous`, `broken` (ai `Connection.writeToNBT` :206-216); rft keeps the names and skips invalid saved geometry on load (catches `RuntimeException` silently).

## 3. Differences: what rft adds, removes, changes

| Area | ai | rft |
| --- | --- | --- |
| Package layout | `block/api/*`, `init/*` | `block/api/fence|geometry|multiblock|sync`, `registry/*` |
| Power | per-segment buffers; `powered` never cleared; client also ticks | one store per column, server only, `powered` both ways, `FencePowerService` with per-tick cache |
| Placement | re-runs on any state change, client too | server only, skips same-block state change |
| Dedupe | none | `equals/hashCode`, `addConnection` merge, no-op restring spends nothing |
| Spool messages | translatable keys, missing from lang | too-long is literal English; not-enough key still missing |
| Remove column | per block, forced-true branch, client and server | server only, whole column first, silent marks, one sync |
| Cascade | none | `breakUnsupportedFloatingConnections`; drops 1 spool per cell |
| Collision | includes broken wires; boxes rebuilt each `entityInside` | excludes broken; cached |
| Rendering | always the full wire | `compileRenderData`; broken vanish; bent halves drawn |
| Bend | +-30 degrees random | about 25.7 degrees max, biased away from other strands |
| Pole rotation | `atan` on intersection data | `atan2` on base positions, averaged, normalised |
| Pole save | field dereferenced directly in `saveData` (ai Pole BE :65-68), NPE risk before first tick [inf] | uses getter |
| Dinosaur break | `destroyBlock` on the middle cells | marks segments broken, then cascade |
| `previous`/`next` at creation | passes `positions[i+1]` as `previous` (ai PostBlock :194) | passes `positions[i-1]` as `previous` (rft :244-245); the Connection constructor also swaps when `compared < 0`, so old worlds may load mirrored [inf] |
| Removed | | `ExampleMixin`, empty `BlockElectricFencePoleMixin` |
| Unchanged | spool formula, limit 15, ITEM_FOLD 20, 1.0 THORNS shock, knockback, light flag, sound and particle gaps, `ElectricFenceBlock.animateTick` | |

## 4. Checks of `docs/codebase-analysis.md` (2.3, 5.1, 5.2)
- 5.1 heights and wire counts: confirmed (`EnumConnectionType` :8-9).
- 5.1 spool up to 15 blocks, `ElectricFencePostBlock.java:133-200`: confirmed.
- 5.1 shock 1 THORNS plus knockback, `BlockConnectableBase.java:75-111`: confirmed; knockback is a velocity override.
- 5.1 `|| true` at `ElectricFencePostBlock.java:224`: confirmed.
- 5.1 "Only Forge can restore a wire with a right-click": half true. The spool-on-adjacent-face repair is Forge only. Right-click on a broken stub (`BlockConnectableBase.use`) is common code.
- 5.1 TODO table lines match.
- 5.2 "16 small collision boxes in each block": wrong for the rewrite; it uses 8.
- 5.2 ray trace rotated into the wire axis, `RotatedRayBox`: confirmed.
- 5.2 "A player can break one wire in a block and leave the other wires": not delivered as written (2.5) [inf].
- 5.2 bend and sparks: bend exists, visible only on rft; sparks never fire on either line.
- 5.2 pole yaw from first two connections, right-click 180: confirmed. Powered pole light: not delivered (2.8).
- 2.3 table (power, placement, connections, collision, rendering, dinosaur break): confirmed. Risk "59d9518 swaps previous and next": confirmed. Risk "triggerModelUpdate does nothing outside the Forge mixin": confirmed.

## 5. Status table

| Feature | ai | rft |
| --- | --- | --- |
| Low and high posts (3 and 8 high) | works | works |
| 2 wires per layer low, 1 high | works | works |
| Spool two-click placement, span 15, incline, any direction | works | works |
| Auto-placed wire blocks, cost `ceil(d/20*h)` | works | works |
| Obstruction in the run | silent gap, spools spent | same, except repeat-run pre-check |
| Duplicate restring | duplicates [inf] | deduped |
| Pole auto yaw and flip | works | works (formula differs) |
| Pole hitbox | works (Forge mixin) | works |
| Wire collision | includes broken | excludes broken |
| Per-wire ray trace | works | works |
| Wire outline highlight | commented out | missing |
| Break single wire | partial [inf] | partial plus cascade |
| Break post | works, forced-true branch | works |
| Broken wire visual and bend | data only, not drawn | works |
| Sparks at break | dead | dead |
| Repair by clicking stub | works (common) | works (common) |
| Repair with spool on neighbour face | Forge only | Forge only |
| Power from generator | creative generator adjacent works | same, tidier |
| Pole to pole propagation | works, also over incomplete runs | complete runs only |
| Unpowering | `powered` never cleared | cleared both ways |
| Powered/unpowered visual | none | none |
| Powered light | stub | stub |
| Damage 1.0 THORNS and knockback | works | works |
| Shock particles and sounds | commented out / missing | missing |
| Item animation | missing | missing |
| Gates, doors | missing | missing |
| Recipes | missing | missing |
| Config | missing | missing |
| Spool lang keys | missing | missing (one) |
| Fabric spool repair | missing | missing |

## 6. Dinosaur interaction
- **Pathing** (ai working tree `P/entity/ai/FenceAwareNodeEvaluator.java:118-132`): posts and wire block count as `BlockPathTypes.FENCE` (impassable) for any block the body overlaps; if `DinoNeedsUtils.allNeedsAtZero(dinosaur)` the node is `OPEN` so a desperate animal walks up to the fence. This file is modified and uncommitted in the working tree (clearance box, cliff-edge rules, path probe from `BodyFit`). `FenceAwareNavigation` is used by `Dinosaur.createNavigation` (`P/entity/dinosaur/Dinosaur.java:476-477`). rft has the older version: checks the node and the two blocks above; FENCE, or OPEN when all needs are zero.
- **Sensor**: `Dinosaur.getSensors` (ai :602-604): `NearbyBlocksSensor`, radius 10, matches only `ELECTRIC_FENCE` (wire block, not posts) and only while `DinoNeedsUtils.starving`.
- **Wants to break**: `SoloHuntingBehaviour` (ai :66-70): if the best hunt target is outside the enclosure (`DinoNeedsUtils.isTargetInsideEnclosure` walks a straight line and returns false on a post or wire block) and the carnivore is starving, it sets `WANTS_TO_BREAK_FENCE`. Bug: the line `entity.equals(BlockInit.ELECTRIC_FENCE_BLOCK_ENTITY.get())` (`DinoNeedsUtils.java` about :93-95) compares a block entity with a `BlockEntityType`, always false.
- **Action**: `BreakFenceBehaviour` (ai `P/entity/ai/behaviour/actions/BreakFenceBehaviour.java:34-73`, registered in `Dinosaur` at :691 with delay 20): needs the memory; scans `NEARBY_BLOCKS` for wire blocks with `distanceToSqr < 2` and keeps the last match (not the nearest); sets the "attack" animation; after the delay calls `breakFence(10)` and sends "WARNING: Fence Destroyed at ..." to all players (hard-coded English).
  - ai `breakFence` (`BlockEntityElectricFence.java:114-130`): for each connection on the hit block and each offset, destroys with drops (`destroyBlock(pos, true)`) the middle cell for `i < intensity` height and the two cells beside it for `i < intensity/2+1`. With intensity 10 and posts 3 or 8 high, the middle cells go for the full height. Connection flags are not touched. No loot, so nothing drops.
  - rft `breakFence` (`BlockEntityElectricFence.java:101-197`): collects each run's base endpoints; marks segments in the central cells broken (distance <= 0.5 cell: up to `intensity` layers; <= 1.5: `intensity/2+1`); `destroyBlock` for cells left with no live connection; then the floating-wire cascade (drops spools). rft `BreakFenceBehaviour` picks the closest wire block within `distanceSq < 2` and rechecks the distance when acting.
- **Contact**: dinosaurs take the same shock and knockback as other entities. No avoidance of powered wire and no learning from shocks [inf from missing code].
- Multi-part entity hitboxes: not checked.

## 7. Open risks seen while reading (no edits made)
- ai wire collision keeps boxes for broken wires.
- `getLightBlock` misuse: light never emitted.
- Missing lang keys for the two spool messages.
- Spark code unreachable.
- ai `BlockEntityElectricFencePole.saveData` uses the `energyContainer` field before the getter creates it [inf].
- `previous`/`next` order differs between ai and rft at creation; saved fences may mirror when moving between lines.
- rft `loadData` swallows `RuntimeException` with no log line.
- Nothing here was tested in a world with save and load.
