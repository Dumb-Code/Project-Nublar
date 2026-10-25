# Movement and navigation for large, long-bodied mobs in the seven studied sources

Scope and method. This note re-reads the seven earlier-studied codebases for the movement side only. It covers navigation, move control, body rotation, stuck handling, clearance checks, group spacing and issue reports. Earlier notes (`research_notes/Multipart entity hitbox systems/*.md`) were read first.

Code was read from public shallow clones made by the earlier research session. On 2026-10-09 I confirmed with the GitHub API that each pinned commit is still the branch head:

- Citadel `1.21` @ `badebf65` (2026-07-18)
- Alex's Caves `1.21.1` @ `0ab3c214` (2026-04-27)
- Alex's Mobs `1.20` @ `09755dad` (2026-01-07)
- Ice and Fire `1.20` @ `0526aad9` (2025-09-05)
- Mowzie's Mobs `MowziesMobs-Public` `1.20` @ `64d15248` (2026-04-23)
- Fossils & Archeology Revival `1.20.1` @ `b400cf24` (2026-06-07)
- CustomHitboxLib `main` @ `17585f5d` (2026-10-06)

Vanilla 1.20.1 navigation classes come from the public third-party Mojang-mapped mirror `Blackjack200/minecraft_client_1_20_1` @ `c129e3e3`, the same mirror the baseline note used. No local game files were opened.

Issue trackers were searched with `gh search issues` on 2026-10-09. GitHub rate-limited part of the Mowzie's and Citadel searches, so those two lean on the earlier notes.

URL prefixes used in links (all pinned):
- CIT = https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/
- AC = https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/
- AM = https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/
- IAF = https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/
- MM = https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/
- FA = https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/
- CHL = https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/
- V = https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/

**Correction to the earlier report.** The earlier report says vanilla "aims each path node at block centre +0.5". That is wrong for 1.20.1. Vanilla `Path.getEntityPosAtNode` already aims at `node + floor(bbWidth+1) × 0.5`. The real vanilla defect is the **arrival test**: it still measures distance to `node + 0.5` (details in Q1). Mowzie's and Fossils' `PatchedPath` repeats the vanilla formula. Their real fix is that their own `isAt` measures arrival against the same centred point the mob aims at.

---

## Q1. Per source: how the navigator sizes its search, aims at nodes, shortcuts or smooths the path, and detects and recovers from being stuck

### Takeaway
There are three navigator families:

1. **Vanilla-derived ground navigators** (Mowzie's `MMPathNavigateGround`, Fossils `PrehistoricPathNavigation`, Alex's Mobs (AM) and Alex's Caves (AC) "Wide"/"NoSpin" navigators). These keep the vanilla A* and the vanilla `floor(w+1)` footprint. They fix the arrival test and add an AABB voxel-sweep shortcut (Mowzie's, Fossils) or loosen the arrival tolerance (AM, AC).
2. **The Minecolonies-derived async `AdvancedPathNavigate`** (Citadel, used by AC and AM; a near-copy in Ice and Fire). It plans on worker threads with a footprint **centred** on the node, 4-way neighbours only, look-ahead arrival over 4 nodes, and a tiered stuck handler.
3. **Custom move or flight code without a ground path** (Naga, Ender Dragon).

No navigator in any source uses body length or heading. Every footprint is a square derived from `getBbWidth()`.

### Cited Findings

**Vanilla 1.20.1 baseline (what every source inherits)**
- `Path.getEntityPosAtNode` aims at `node.x + (int)(bbWidth + 1) × 0.5` (the same on Z). The aim point is the centre of the footprint that starts at the node block and extends in +X/+Z. — [V Path.java L76-82](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/level/pathfinder/Path.java#L76-L82)
- `PathNavigation.tick` sends that centred point to `MoveControl.setWantedPosition`. — [V PathNavigation.java L198-218](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/ai/navigation/PathNavigation.java#L198-L218)
- `PathNavigation.followThePath` sets `maxDistanceToWaypoint = bbWidth/2` (for w > 0.75). It then advances only when `|x − (node.x + 0.5)| < w/2`, `|z − (node.z + 0.5)| < w/2` and `|dy| < 1`. The test uses **node + 0.5, not the centred aim point**. Corner cutting needs the mob to be within 2.0 of `atBottomCenterOf(node)`. — [V PathNavigation.java L228-268](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/ai/navigation/PathNavigation.java#L228-L268)
- Base `PathNavigation.canMoveDirectly` returns false, so vanilla ground navigation never shortcuts. `isClearForMovementBetween` is a single-ray `clip`, not a box sweep. — [V PathNavigation.java L355-366](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/ai/navigation/PathNavigation.java#L355-L366)
- Footprint sizing: `NodeEvaluator.prepare` sets `entityWidth = entityDepth = floor(bbWidth + 1)` and `entityHeight = floor(bbHeight + 1)`. — [V NodeEvaluator.java L27-29](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/level/pathfinder/NodeEvaluator.java#L27-L29)
- `WalkNodeEvaluator` samples block path types over that whole cuboid (L356-358). A true AABB collision test of the candidate node is done only when `bbWidth < 1.0` (L241-245). — [V WalkNodeEvaluator.java L241-245, L356-358](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/level/pathfinder/WalkNodeEvaluator.java#L241-L358)
- Vanilla stuck detection works at two levels:
  - Every 100 ticks, if the mob moved less than `speed × 25` (`speed²×25` if speed < 1), it sets `isStuck` and stops the path.
  - Per node, if the time on the same next node exceeds 3 × (distance ÷ speed × 20) ticks, it times out the path.

  — [V PathNavigation.java L271-310](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/ai/navigation/PathNavigation.java#L271-L310)

**Citadel 1.21 `AdvancedPathNavigate` (used by AC and AM; Ice and Fire ships a near-copy)**
- Search sizing (`AbstractPathJob.setEntitySizes`):
  - An `ICustomSizeNavigator` sets XZ half-extent `(int)getXZNavSize()` and height `getYNavSize()`.
  - Otherwise, w ≤ 1 gives range [0,1]. w > 1 gives [−floor(w/2), +floor(w/2)], a footprint **centred on the node**, plus height `ceil(bbHeight)`.

  — [CIT pathfinding/raycoms/pathjobs/AbstractPathJob.java L458-476](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/pathjobs/AbstractPathJob.java#L458-L476)
- Passability checks:
  - `isPassableBBFull` checks the full (2·half+1)² × height cube.
  - `isPassableBB` checks only a **cross-section slice** at the new node, perpendicular to the step direction (`pos.relative(facing.getClockWise(), i).above(j)`).

  — [CIT AbstractPathJob.java L1348-1396](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/pathjobs/AbstractPathJob.java#L1348-L1396)
- Neighbours are N/E/S/W plus up and down only. There are no diagonal moves (L644-693). Cost is Euclidean step length times jump, drop, door, path, rail, vine and swim multipliers. There is no turning cost (L490-535). Search stops after `PathfindingConstants.maxPathingNodes` (5000) or `maxRange²` nodes (L582). — [CIT AbstractPathJob.java L490-693](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/pathjobs/AbstractPathJob.java#L490-L693); [CIT PathfindingConstants.java L52-54](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/PathfindingConstants.java#L52-L54)
- Jobs run on a `ThreadPoolExecutor` (core 1, max `pathfindingThreads`, default 1). — [CIT Pathfinding.java L38](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/Pathfinding.java#L38)
- `tick()` writes `nodeEvaluator.entityWidth/Height/Depth = floor(width + 1)` each tick, from the live `getBbWidth()` unless `overrideDefaultDimensions` is set (L248-258). It then replaces vanilla's `super.tick()` and sends `getEntityPosAtNode(next)` to the move control (L291-313). — [CIT AdvancedPathNavigate.java L248-313](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/AdvancedPathNavigate.java#L248-L313)
- Aiming: `getEntityPosAtNode` returns `node + 0.5` on X/Z (L870-878). Because the planner's footprint is centred on the node, this is the footprint centre. Aim and arrival use the same point.
  - Arrival (`followThePath`, L770-865) scans the **next 4 nodes**. It advances past any node within `maxDistanceToWaypoint − 0.1·|dy|` on X and Z.
  - The vertical tolerance is `min(1, ceil(h/2))` or `ceil(w/2) × 3`, so a wider mob may "arrive" at a node up to 3·ceil(w/2) blocks below.
  - It also rewinds to an earlier node if the mob fell behind.

  — [CIT AdvancedPathNavigate.java L770-878](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/AdvancedPathNavigate.java#L770-L878)
- No shortcutting: `canMoveDirectly` just calls super (always false for this class). It carries the comment "TODO improve road walking". — [CIT AdvancedPathNavigate.java L512-515](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/AdvancedPathNavigate.java#L512-L515)
- Stuck handling:
  - Vanilla `doStuckDetection` is overridden to do nothing (L896-898). `PathingStuckHandler.checkStuck` runs every tick instead.
  - "Not progressing" means the next-node index did not change. Progress over more than 5 nodes resets the timers.
  - A global timeout of `max(2400, 100 × max(10, distanceToGoal))` ticks triggers `completeStuckAction`.
  - `tryUnstuck` escalates after an initial `delayBeforeActions` of 1200 ticks: level 0 clears the path; level 1 moves away 10 blocks; level 2 teleports N nodes ahead if `teleportRange > 0`; levels 3-5 place ladders or leaves (if enabled); levels 6-8 break blocks (if enabled); level 9 completes the stuck action (teleport to goal, damage, or a block-break line). There is a random chance to drop back 2 levels.
  - The default constructor uses `withTeleportSteps(6).withTeleportOnFullStuck()` (L103-104).

  — [CIT PathingStuckHandler.java L42-53, L111, L139-322](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/PathingStuckHandler.java#L139-L322); [CIT AdvancedPathNavigate.java L103-104, L896-898](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/AdvancedPathNavigate.java#L103-L104)
- Extension hooks:
  - `IPassabilityNavigator`: per-entity `maxSearchNodes` and "explicitly passable / not passable" blocks.
  - `ITallWalker.getMaxNavigableDistanceToGround`: AC sauropods return 3.

  — [CIT IPassabilityNavigator.java](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/IPassabilityNavigator.java); [CIT AbstractPathJob.java L181-189](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/pathjobs/AbstractPathJob.java#L181-L189); [AC living/SauropodBaseEntity.java L640-643](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L640-L643)

**Alex's Caves 1.21.1**
- The sauropods, Tremorzilla, Grottoceratops, Tremorsaurus, Relicheirus and Subterranodon all use `AdvancedPathNavigateNoTeleport`. It is the Citadel navigator sized from `getBbWidth()`/`getBbHeight()`, built with a plain `PathingStuckHandler.createStuckHandler()` (teleport off), and with `canUpdatePath()` always true. — [AC ai/AdvancedPathNavigateNoTeleport.java L8-22](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/AdvancedPathNavigateNoTeleport.java#L8-L22)
- That class was added in commit `51a08da4` (2024-02-24), "fix #852 fix #849 fix #848". Issue #852 reported: "Dinosaurs have started to occasionally teleport when moving, causing them to clip into walls and die of suffocation… more prevalent in bigger mobs (Grotto, Relich, Tremorzilla/saurus and Atlatitan)". — [commit 51a08da4](https://github.com/AlexModGuy/AlexsCaves/commit/51a08da4); [AlexsCaves #852](https://github.com/AlexModGuy/AlexsCaves/issues/852)
- Vallumraptor uses `GroundPathNavigatorNoSpin`, a vanilla subclass whose arrival tolerance is `bbWidth × 0.75` (default). — [AC ai/GroundPathNavigatorNoSpin.java L9-32](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/GroundPathNavigatorNoSpin.java#L9-L32); [AC living/VallumraptorEntity.java L109-110](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/VallumraptorEntity.java#L109-L110)
- Sauropod main boxes are 5.0×8.0 (Atlatitan) and 6.0×8.5 (Luxtructosaurus), with step height 3.2. — earlier note [citadel_alexs_caves.md Q4]; [AC living/SauropodBaseEntity.java L539-541](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L539-L541)

**Alex's Mobs 1.20**
- Elephant, bison, rhinoceros and tusklin use AM's own `AdvancedPathNavigateNoTeleport(this, level, true)`. This is a Citadel navigator with no teleport and, when `wide`, `calculateMaxDistanceToWaypoint = bbWidth × 0.75`. — [AM ai/AdvancedPathNavigateNoTeleport.java L8-30](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/AdvancedPathNavigateNoTeleport.java#L8-L30); [AM EntityElephant.java L186-187](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/EntityElephant.java#L186-L187)
- `GroundPathNavigatorWide` is the vanilla follower with `maxDistanceToWaypoint = bbWidth × distancemodifier` (default 0.75). About 20 AM mobs use it, for example the tiger with modifier 1.2. — [AM ai/GroundPathNavigatorWide.java L23](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/GroundPathNavigatorWide.java#L23); [AM EntityTiger.java L582-591](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/EntityTiger.java#L582-L591)

**Ice and Fire 1.20**
- The dragon uses an in-tree copy of the same Minecolonies navigator. Its `ICustomSizeNavigator` gives XZ = `max(1.4, bbWidth/2)` (truncated to an int half-extent) and Y = `ceil(bbHeight)`. — [IAF entity/EntityDragonBase.java L2912-2924](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L2912-L2924)
- The ground navigator gets `createStuckHandler().withTeleportSteps(5)`. — [IAF EntityDragonBase.java L447-476](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L447-L476)
- The same width-scaled drop tolerance (`ceil(bbWidth/2) × maxDropHeight`, `maxDropHeight = 3`) is in IaF's `followThePath`. — [IAF pathfinding/raycoms/AdvancedPathNavigate.java L731-740](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/pathfinding/raycoms/AdvancedPathNavigate.java#L731-L740)
- Two further techniques: blocks are broken around the main box (stage 3+, griefing on), and with `dragonDigWhenStuck` the dragon digs when stuck. — earlier note [ice_and_fire.md Q3]

**Mowzie's Mobs 1.20**
- `MMPathNavigateGround` (Frostmaw, Grottol, Wroughtnaut) works as follows:
  1. It caps the shortcut horizon at the first node whose Y differs from the mob's floor Y.
  2. It **sweeps the mob's own AABB** (voxel-AABB sweep after andyhall/voxel-aabb-sweep) from the mob toward each later centred node, farthest first, and jumps to the first clear one.
  3. The sweep rejects non-pathfindable blocks, water, lava or open floor below, malus < 0 or ≥ 8, and fire/damage types.
  4. If no shortcut is taken, it advances when `isAt(path, 0.5)`, measured against `getNextEntityPos` (the **centred** aim point). At elevation changes within `ceil(w/2)+1` nodes it uses `isAt(path, w/2)`.

  — [MM ai/MMPathNavigateGround.java L30-140](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathNavigateGround.java#L30-L140)
- `MMPathFinder.PatchedPath.getEntityPosAtNode` uses `node + Mth.floor(bbWidth + 1) × 0.5`. Numerically this is identical to vanilla's `(int)(bbWidth + 1) × 0.5` for positive widths. `MMWalkNodeProcessor`'s overrides are all commented out. — [MM ai/MMPathFinder.java L31-43](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathFinder.java#L31-L43); [V Path.java L76-82](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/level/pathfinder/Path.java#L76-L82); earlier note [mowzies_mobs.md Q4]
- Stuck handling is vanilla `doStuckDetection` (called at the end of `followThePath`). — [MM ai/MMPathNavigateGround.java L30-48](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathNavigateGround.java#L30-L48)
- The Naga (flying) accepts a flight target only if its own AABB moved there passes `noCollision` and a block `clip` from current position to target misses. — [MM entity/naga/EntityNaga.java L1015-1027](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/entity/naga/EntityNaga.java#L1015-L1027)

**Fossils & Archeology Revival 1.20.1**
- `PrehistoricPathNavigation` says it is "based off of Mowzie's Mobs". It uses the same same-Y horizon, the AABB sweep shortcut (`NavUtil.isNoCollisionOnPath`) and the centred `isAt`: tolerance 0.5, or 0.75·w at elevation changes, with vertical ≤ 1. It also points the look control at the next node when advancing. — [FA entity/ai/navigation/PrehistoricPathNavigation.java L48-97](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/PrehistoricPathNavigation.java#L48-L97); [FA NavUtil.java L14-161](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/NavUtil.java#L14-L161)
- `PrehistoricPathFinder` is its own A*:
  - The heuristic is **inflated ×1.5** (`FUDGING`, weighted A*; L22, L108).
  - It appends the exact target node for mobs narrower than 1 (L124).
  - `PatchedPath` uses the same centring formula as vanilla (L136-140).
  - `PrehistoricNodeEvaluator` rejects a step-up if the floor rise exceeds both the entity height and 1.125.

  — [FA PrehistoricPathFinder.java L22-140](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/PrehistoricPathFinder.java#L22-L140); [FA PrehistoricNodeEvaluator.java L15](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/PrehistoricNodeEvaluator.java#L15)
- `CenteredNodeEvaluator` and the flight navigator's `FlightNodeEvaluator` check a **symmetric** footprint around the node. They loop i, k in `[0, ceil(max(0, entityWidth−2)/2)+1)` and test `(x+i, z+k)` and `(x−i, z−k)`. `CenteredPath` aims at `node + 0.5`. `FlightPathNavigation` wraps its paths in `CenteredPath`; the ground navigator does not use this evaluator. — [FA CenteredNodeEvaluator.java L83-101](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/CenteredNodeEvaluator.java#L83-L101); [FA CenteredPath.java](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/CenteredPath.java); [FA FlightPathNavigation.java](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/FlightPathNavigation.java)
- Ground mobs get `PrehistoricPathNavigation`; arthropod climbers get `PrehistoricWallClimberNavigation`. — [FA entity/prehistoric/base/Prehistoric.java L1396-1398](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/Prehistoric.java#L1396-L1398)
- Recovery: adult, hungry, angry (mood < 0) dinos whose species `breaksBlocks()`, with mob griefing and the config on, break blocks of hardness ≤ config inside their AABB inflated 0.1 when `horizontalCollision` is set, and slow to 0.6×. — [FA Prehistoric.java L755-757, L880-897](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/Prehistoric.java#L880-L897)

**CustomHitboxLib**
- It overwrites `followThePath` and patches `WalkNodeEvaluator.getStart`/`getSurfaceY` to use the lowest colliding part's `minY`. Waypoint tolerance still uses `getBbWidth()`. The node evaluator plans with the main width. — earlier note [custom_hitbox_lib.md Q6]

**MoreHitboxes and Countered's Accurate Hitboxes**
- They contain no navigation or movement code. MoreHitboxes parts are `noPhysics`. Countered's only gameplay mixin is a client raycast. — earlier notes [morehitboxes.md Q4], [countereds_accurate_hitboxes.md Q3]

### Inferences
- **Vanilla arrival bug for wide mobs.** The aim point sits `floor(w)/2` from `node + 0.5`, and the arrival tolerance is `w/2`.
  - For non-integer w the margin is only `(w − floor(w))/2`. For example, w = 2.1 leaves 0.05 blocks.
  - For **integer w** (2.0, 3.0, 5.0) the margin is zero. A mob that sits exactly on its aim point fails the strict `<` on the axis perpendicular to travel, and only corner-cutting can advance the path.
  - This fits the "spinning on the spot" reports across sources (see Q6).
  - Dinosaurs that grow smoothly pass through every integer width, and herds contain many widths at once. Any Nublar navigator that keeps vanilla `followThePath` with vanilla `getEntityPosAtNode` has this defect.
  - Proven fixes: (a) measure arrival against the same centred point (Mowzie's, Fossils `isAt`), or (b) loosen tolerance to ≥ 0.75·w (AM, AC "Wide"/"NoSpin"), or (c) centre the footprint on the node and aim at `node + 0.5` (Citadel).
- **Footprint quantisation differs.**
  - Vanilla `floor(w+1)` over-sizes integer widths: w = 2.0 needs a 3-wide gap.
  - Citadel's `2·floor(w/2)+1` **under-sizes** many widths: 1 < w < 2 plans as 1 block wide, and 3 ≤ w < 4 plans as 3 blocks wide. The planner can therefore route a w = 1.5 mob through a 1-wide gap that the collision box cannot enter. This is a plausible jam generator for growth-scaled widths.
  - IaF's dragon floor of 1.4 forces a minimum 3-wide plan.
- **Citadel paths are 4-connected staircases.** No diagonals and no turning cost mean many 90° node-to-node direction changes on diagonal routes. Look-ahead over 4 nodes with tolerance w/2 smooths this only partly. The effect grows when turning is gated (Q2).
- **Shortcuts.** Only Mowzie's and Fossils shortcut, and they sweep the **square AABB** along a straight segment on one Y level. That is the cheapest proven way to remove staircase corners for big mobs. Cost is O(cells crossed × box cross-section) per candidate node, tried farthest-first, every tick while following.
- **Stuck handling.** Citadel's tiered handler is the richest. Its teleport tier is demonstrably harmful for big mobs (AC #852), and both AC and AM ship "NoTeleport" variants. Vanilla's 100-tick displacement test is crude but cheap.

### Gaps
- I did not check how vanilla 26.x (the port target) changed `PathNavigation.followThePath`, `Path.getEntityPosAtNode` or `WalkNodeEvaluator`. Public Mojang-mapped mirrors for 26.x were not consulted. The arrival-mismatch finding is verified for 1.20.1 only.
- No source publishes profiler data for Citadel async jobs or the AABB sweep.

---

## Q2. Which sources make turning a gated step (turn-before-move, maximum turn rate, body-yaw lag), and how is it implemented?

### Takeaway
There are four levels of turn control:

1. **A hard gate**: AC sauropods walk only when the body yaw is within 15° of the move direction.
2. **A soft gate**: Fossils `SmoothTurningMoveControl` slows the mob to as little as 0.1× speed in proportion to the remaining turn, and caps the turn rate from body width.
3. **A turn-rate cap only**: IaF dragons (5-15°/tick when large), Mowzie's Frostmaw (7°/tick), Grottol (45°/tick). The mob keeps full speed, so it walks an arc.
4. **Visual body-yaw lag only**: AC Tremorzilla, Grottoceratops and Tremorsaurus, plus the AC sauropods' yaw ring buffer for neck and tail.

CustomHitboxLib is the negative case. It vetoes whole rotation steps when colliding parts would hit blocks.

### Cited Findings
- **Vanilla baseline.**
  - `MoveControl.MOVE_TO` turns `yRot` up to 90°/tick (`rotlerp(…, 90)`) and sets full speed in the same tick.
  - `BodyRotationControl` snaps `yBodyRot = yRot` whenever the mob moves more than about 5e-4 blocks per tick. When still, it lets the body follow the head after 10 ticks.

  — [V MoveControl.java L85-105](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/ai/control/MoveControl.java#L85-L105); [V BodyRotationControl.java L18-58](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/ai/control/BodyRotationControl.java#L18-L58)
- **AC `SauropodMoveHelper` (hard gate).**
  - On `MOVE_TO` it computes the angle to the wanted point and sets `flag = degreesDifferenceAbs(yBodyRot, moveToRot) < 15`. The flag is forced true during tail-whip animations.
  - Speed is `(legSlam − 0.65)/(1 − 0.65)` only if `legSlam ≥ 0.65 && flag` (halved for babies), else 0. Here `legSlam = |cos(walkAnimPos·0.05·2 − π·0.66)|`.
  - It then calls `super.tick()`, so vanilla still rotates `yRot` 90°/tick toward the target.
  - `WAIT` sets `WALKING = false`.

  — [AC living/SauropodBaseEntity.java L645-686](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L645-L686); [L530-533 getLegSlamAmount](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L530-L533)
- **AC sauropod body-yaw lag.**
  - In `tick()`, `yBodyRot = approachDegrees(yBodyRotO, getYRot(), turningFast ? 10 : 2)`, so the body turns **2°/tick** normally. A 90° re-heading therefore takes about 45 ticks, during which the gate keeps speed at 0.
  - `turningFast` is set only inside `AtlatitanMeleeGoal`/`LuxtructosaurusMeleeGoal`. Whip animations drive the body yaw directly.
  - Head rotation is limited by `getHeadRotSpeed() = 3` and `getMaxHeadYRot() = 90`.

  — [AC SauropodBaseEntity.java L100, L150-165, L384-390](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L150-L165); [AC ai/AtlatitanMeleeGoal.java L31-46](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/AtlatitanMeleeGoal.java#L31-L46)
- **AC other dinosaurs (lag only, no gate).**
  - Tremorzilla: `yBodyRot = approachDegrees(yBodyRotO, getYRot(), 4)`.
  - Grottoceratops and Tremorsaurus: `yBodyRot = approachDegrees(yBodyRotO, yBodyRot, getHeadRotSpeed())`.
  - They use the vanilla `MoveControl`, so movement direction follows `yRot` and is not gated.

  — [AC TremorzillaEntity.java L270-295](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/TremorzillaEntity.java#L270-L295); [AC GrottoceratopsEntity.java L112](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/GrottoceratopsEntity.java#L112); [AC TremorsaurusEntity.java L136](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/TremorsaurusEntity.java#L136)
- **AC neck and tail lag.** A 128-entry `yBodyRot` ring buffer is sampled at lag 10 (neck) and lag 20 (tail). Neck and tail angles approach their targets at 10°/tick (+30 when `turningFast`). — earlier note [citadel_alexs_caves.md Q2]; [AC SauropodBaseEntity.java L258-270](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L258-L270)
- **Fossils `SmoothTurningMoveControl` (soft gate).** It is installed on every `Prehistoric` (Prehistoric L162).
  - Turning is skipped if the horizontal distance² is ≤ 0.12. The comment says this "prevents spinning if mob overshoots".
  - `maxTurn = getMaxTurnDistancePerTick() = clamp(90 − 35·bbWidth, 5, 90)` degrees per tick. So w = 1 gives 55°, w = 2 gives 20°, and w ≥ 2.43 gives 5°.
  - `turnDiff = max(0, |Δyaw| − maxTurn)` and `turnMod = lerp(turnDiff/180, 1, 0.1)`.
  - Speed is `turnMod × speedModifier × MOVEMENT_SPEED`. A 180° re-heading at 5°/tick starts at about 0.125× speed.
  - Its jump rule also fires early when a step-up is within a speed-scaled "jump distance".

  — [FA entity/ai/control/SmoothTurningMoveControl.java L19-75](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/control/SmoothTurningMoveControl.java#L19-L75); [FA Prehistoric.java L162, L1511-1513](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/Prehistoric.java#L1511-L1513)
- **IaF dragon `GroundMoveHelper` (rate cap).**
  - The turn range is 70°/tick. If `ceil(bbWidth) > 2` it becomes `5 + 10·(1 − min(ageDays,125)/125)`, so 5°/tick at full age.
  - Speed is not reduced while turning.
  - The jump test uses `bbWidth/2` instead of vanilla's `bbWidth`.

  — [IAF entity/IafDragonFlightManager.java L159-230](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/IafDragonFlightManager.java#L159-L230)
- **Mowzie's `MMEntityMoveHelper` (rate cap).** It is vanilla logic with a `maxRotate` parameter: Frostmaw uses 7°/tick and Grottol 45°/tick. Speed is not reduced. — [MM ai/MMEntityMoveHelper.java L15-77](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMEntityMoveHelper.java#L15-L77); [MM entity/frostmaw/EntityFrostmaw.java L128](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/entity/frostmaw/EntityFrostmaw.java#L128); [MM entity/grottol/EntityGrottol.java L100](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/entity/grottol/EntityGrottol.java#L100)
- **CustomHitboxLib (rotation veto).**
  - A HEAD inject on `Entity.setYRot` (and `setXRot`) tests whether any `collision:true` part box (at its positioner position for the new yaw) overlaps block collision shapes.
  - If the old pose was clear and the new one collides, the **whole rotation call is cancelled**. There is no partial turn.
  - Its replacement `MoveControl` still calls `rotlerp(…, 90)` and sets full speed in the same tick.

  — [CHL src/main/java/dev/customhitboxlib/mixin/EntityRotationMixin.java L30-133](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/src/main/java/dev/customhitboxlib/mixin/EntityRotationMixin.java#L30-L133); [CHL MoveControlMixin.java L95-130](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/src/main/java/dev/customhitboxlib/mixin/MoveControlMixin.java#L95-L130)
- **Alex's Mobs.** Tiger and others use `MovementControllerCustomCollisions` (vanilla 90°/tick turn), and `FlightMoveController` sets `yBodyRot = yRot` directly. No AM ground mob studied gates movement on heading. — [AM ai/MovementControllerCustomCollisions.java](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/MovementControllerCustomCollisions.java); [AM ai/FlightMoveController.java L32-56](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/FlightMoveController.java#L32-L56)

### Inferences
- **AC's gate is two-layer.** `yRot` (logical heading) snaps quickly through vanilla, but the gate reads the slow visual `yBodyRot`. Movement resumes only when the visible body faces the target, which gives true turn-in-place. The cost is long standstills: 2°/tick body speed and a path that changes direction often. Combined with Citadel's 4-connected staircase paths, a diagonal route can trigger repeated small re-headings. Most stay under 15°, because the look-ahead targets nodes up to w/2 away, but this is unverified.
- **Soft gating vs a hard gate.** Fossils' slowdown never stops completely, so a mob in a corridor still moves 0.1× forward while turning. For a long body in a tight space this produces an arc, which is the failure turn-in-place avoids. A hybrid could use Fossils' formula in open terrain and AC's hard gate when a clearance check (Q3) reports tight space.
- **Rate caps without slowdown** (IaF, Mowzie's) give a minimum turning radius of roughly `speed / (rate in radians per tick)`. With a 5°/tick cap and about 0.3 blocks/tick, that is a radius of about 3.4 blocks. A mob that cannot make a corner inside its arrival tolerance overshoots and orbits the node: the "spinning" symptom.
- **Anti-spin guard.** Fossils' `horizontalDist > 0.12` guard and the Citadel and AM loose tolerances are the only explicit anti-orbit measures found.

### Gaps
- I did not measure in game how long AC sauropods stand still when making 90° path turns, or whether players notice.
- I did not read Luxtructosaurus- or Atlatitan-specific overrides of the move helper beyond the shared base.

---

## Q3. Does any source check clearance for body length or heading, beyond the square box (at corners, on spawn, on growth, on path acceptance)?

### Takeaway
No source checks clearance for the **body length along the heading** during pathfinding. The closest things are:

- **CustomHitboxLib's rotation veto**, a per-rotation-step check of colliding part boxes. It works against the planner and its own FAQ warns it confuses pathfinding.
- **Mowzie's and Fossils' AABB sweep**, which accepts a straight path shortcut only if the square box clears it.
- **Citadel's cross-section slice**, which checks the full width perpendicular to each step but not length.
- **Fossils' growth guard**, which refuses a larger box that would end up inside blocks. Vanilla 1.20.1 skips its own growth fix-up for boxes over 4 blocks.

Spawn checks everywhere are vanilla AABB `noCollision` / `isUnobstructed` tests on the main box.

### Cited Findings
- **Vanilla growth.** `Entity.refreshDimensions` tries `level.findFreePosition` to nudge a growing entity out of blocks, but **only if the new width and height are both ≤ 4.0**, and only for non-players on the server. If no free position exists, the bigger box stays in place. — [V world/entity/Entity.java L2656-2672](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/Entity.java#L2656-L2672)
- **Fossils growth.** `Prehistoric.refreshDimensions` drops the 4.0 limit and always tries `findFreePosition` when growing. If none is found, it **reverts** to the old dimensions and eye height, writes the old width and height into the synced `DIMENSION_VER` tag and calls `reapplyPosition()`. The code comment says the free-position search is "Quite slow for very large mobs and borderline unusable if wDiff and hDiff are also very large". — [FA Prehistoric.java L419-449](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/Prehistoric.java#L419-L449)
- **Mowzie's and Fossils path acceptance.** A shortcut is taken only if the mob's **square AABB** sweeps clear to the candidate node. — [MM MMPathNavigateGround.java L69-140](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathNavigateGround.java#L69-L140); [FA NavUtil.java L14-130](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/NavUtil.java#L14-L130)
- **Citadel per-step slice.** `isPassableBB` checks a line of the full footprint width perpendicular to the step at each new node. `isPassableBBFull` checks the full cube on drops. — [CIT AbstractPathJob.java L1348-1396](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/pathjobs/AbstractPathJob.java#L1348-L1396)
- **CustomHitboxLib rotation veto.** For each yaw or pitch change, part boxes are rebuilt at the new pose and tested with `getBlockCollisions`. Turns that would make a clear part collide are cancelled. The FAQ says: "Custom parts with `collision: true` affect mob navigation. If the part is large or poorly positioned, it can confuse the pathfinder." — [CHL EntityRotationMixin.java L30-133](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/src/main/java/dev/customhitboxlib/mixin/EntityRotationMixin.java#L30-L133); [CHL wiki/FAQ.md](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/wiki/FAQ.md)
- **CustomHitboxLib step-up.** The `MoveControl` replacement raises the wanted Y by 1 if colliding parts would hit blocks at the wanted Y but not at Y + 1. — [CHL MoveControlMixin.java L95-106](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/src/main/java/dev/customhitboxlib/mixin/MoveControlMixin.java#L95-L106)
- **Alex's Mobs segment terrain probing.** Anaconda segments probe up to 3 blocks down and up for suffocating shapes to choose a pitch (≤ 10°/tick) and then `moveTo` without collision. The segment adapts, but this does not check clearance. — earlier note [fossils_alexsmobs_baseline.md Q4]; [AM EntityAnacondaPart.java L152-226](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/EntityAnacondaPart.java#L152-L226)
- **Teleport and target validation by main box.**
  - AC Tremorzilla and Subterranodon follow-owner goals, AM flying follow-owner goals and the Fossils `DinoFollowOwnerGoal` accept a teleport spot only if the main box moved there passes `noCollision`.
  - The Mowzie's Naga accepts a flight target only if the moved box is clear and a block ray misses.

  — [AC ai/TremorzillaFollowOwnerGoal.java L144](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/TremorzillaFollowOwnerGoal.java#L144); [FA ai/DinoFollowOwnerGoal.java L134-142](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/DinoFollowOwnerGoal.java#L134-L142); [MM EntityNaga.java L1015-1027](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/entity/naga/EntityNaga.java#L1015-L1027)
- **Spawn.** AC's `NaturalSpawnerMixin` tests `noCollision(type.getDimensions().makeBoundingBox(...))` and `checkSpawnObstruction`. AM, IaF and Fossils override `checkSpawnObstruction` with `isUnobstructed` (entity-overlap) checks. All of these use the main box only. — [AC mixin/NaturalSpawnerMixin.java L67-84](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/mixin/NaturalSpawnerMixin.java#L67-L84); [IAF entity/EntityIceDragon.java L299-300](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityIceDragon.java#L299-L300)
- **AC's alternative to clearance.** Sauropods are made immune to `IN_WALL` damage. — [AC SauropodBaseEntity.java L624-626](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L624-L626)
- **Block-removal alternatives.**
  - Luxtructosaurus destroys blocks tagged `alexscaves:luxtructosaurus_breaks` (`#minecraft:leaves`, `#minecraft:logs`, `alexscaves:pewen_branch`) in its inflated box when `horizontalCollision`, in water, or when griefing is allowed.
  - IaF dragons and the Ender Dragon break blocks around their boxes.

  — [AC LuxtructosaurusEntity.java L394-410](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/LuxtructosaurusEntity.java#L394-L410); [AC luxtructosaurus_breaks.json](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/resources/data/alexscaves/tags/block/luxtructosaurus_breaks.json); earlier notes [ice_and_fire.md Q3], [fossils_alexsmobs_baseline.md Q1]

### Inferences
- **CHL is evidence against a hard heading check in move control.** The rotation veto is effectively "body-length clearance at every turn" enforced in `setYRot`. Because the planner does not know about it, the mob arrives at a corner, cannot rotate, still receives full speed, and pushes forward along the old heading. A candidate "clearance at sharp-turn nodes" check must live in the **planner or path post-processor** (change the route, or mark the node as a turn-in-place point), not in `MoveControl` or `setYRot`.
- **Required clearance for turning in place.** For a body pivoting about its torso centre, the swept area of a full turn is a disc of radius `max(noseReach, tailReach)` from the pivot. A 90° turn sweeps a quarter-annulus on each end. Checking a disc of block columns at sharp-turn nodes (only where the heading change exceeds a threshold) costs O(r²) column lookups per such node. That is cheap compared with A* itself, because few nodes are sharp turns. This is new; no source does it.
- **Hard or soft constraint.** In forests a hard length-clearance rule will often find no path. AC's practice (non-colliding neck and tail plus `IN_WALL` immunity) shows the gameplay-safe fallback is to allow clipping. The new check is best used as a **cost** or **turn-mode trigger**, not a hard constraint.
- **Growth.** Nublar grows smoothly, so the box grows nearly every tick for a long time. Vanilla's free-position fix-up stops above 4 blocks. Fossils' revert-on-blocked approach is the only source pattern that prevents adults from growing into walls. Its noted cost suggests calling it with small width deltas (growth step) rather than large ones.

### Gaps
- I found no source that validates path acceptance against the swept area of an oriented rectangle or capsule.
- I did not check whether Fossils' reverted dimensions are re-attempted later. The code reverts on each call, and grow attempts come from `refreshDimensions` triggers. Retry frequency was not traced.

---

## Q4. How do group and herd mobs in these sources avoid each other and avoid jams? Is any spacing non-circular?

### Takeaway
No source implements separation steering, slot spacing or any non-circular (capsule or spine) spacing for ground herds. Group movement uses one of three patterns:

1. **Linked-list follow** (llama-style caravan or procession): AM elephants keep a 4-block centre gap, and AC Underzealots keep a 1-block gap. This is single file.
2. **Leader-relative random targets** (Fossils flocks, AC Vallumraptor packs, IaF Stymphalian birds).
3. **Indexed V-formation slots for fliers**: AC Subterranodon, whose wedge offsets grow with rank.

Entity pushing is the only anti-overlap mechanism. Fossils turns pushing **off** for big bodies (≥ 3 blocks), which removes even that.

### Cited Findings
- **AM elephant caravan.**
  - A non-tusked elephant joins the nearest caravan tail within 32 blocks, or a tusked adult. Each animal follows the one ahead (`caravanHead`).
  - In `tick`, when its navigation is done, it moves to a point along the line to its head at distance `max(d − 4, 0)`.
  - If more than 26 blocks behind (d² > 676) it speeds up ×1.2 for 40 ticks, then gives up.

  — [AM ai/ElephantAIFollowCaravan.java L21-140](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/ElephantAIFollowCaravan.java#L21-L140)
- **AM gazelle herd panic.** `AnimalAIHerdPanic` copies the attacker to every same-type `IHerdPanic` animal within 15 blocks. Each flees to its own `LandRandomPos.getPosAway(16, 7)`. There is no spacing. — [AM ai/AnimalAIHerdPanic.java L55-80](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/AnimalAIHerdPanic.java#L55-L80)
- **AM bison.** It has no herd goal. It uses `FollowParentGoal`, `AnimalAIWanderRanged(70, 1.0, 18, 7)` and a charge goal. — [AM EntityBison.java registerGoals](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/EntityBison.java)
- **AC packs** (`PackAnimal`: Vallumraptor up to 8, Subterranodon up to 5, Underzealot up to 11).
  - Joining builds a doubly linked list (`priorPackMember`/`afterPackMember`).
  - A Vallumraptor follower wanders to `DefaultRandomPos.getPosTowards(10, 7, leaderPos, π/2)`.
  - An Underzealot procession follows its prior member to `max(dist − 1, 0)`.

  — [AC ai/AnimalJoinPackGoal.java](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/AnimalJoinPackGoal.java); [AC ai/VallumraptorWanderGoal.java L21-27](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/VallumraptorWanderGoal.java#L21-L27); [AC ai/UnderzealotProcessionGoal.java L42-54](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/UnderzealotProcessionGoal.java#L42-L54)
- **AC Subterranodon formation.**
  - `index` is the follower's depth in the linked list (capped at 16), and `halfIndex = ceil(index/2)`.
  - The slot is `leader.lastFlightTargetPos + ((index%2 − 0.5)·xOffset·halfIndex, 0, halfIndex·zOffset)`, rotated by `180 − leader.yBodyRot`.
  - `xOffset` is randomised in 6-8 and `zOffset` in 4-7 on every call. This is an alternating-side V whose lateral and longitudinal spacing grow linearly with rank.
  - The leader's flight target is validated with a block `clip`.

  — [AC ai/SubterranodonFlightGoal.java L143-176](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/SubterranodonFlightGoal.java#L143-L176)
- **Fossils flocks** (`PrehistoricFlocking`: Gallimimus max 10, Mammoth).
  - The leader is chosen preferring adults, then teens, then babies.
  - Every 25 ticks a follower paths to `LandRandomPos.getPos(this, 5, 7)` if within 7 blocks of the leader, else to `LandRandomPos.getPos(groupLeader, 5, 7)`.
  - It leaves the group if out of range (d² > 151) or, with chance 1/200 per tick, if the leader is not within the 32-block flock distance.

  — [FA entity/prehistoric/base/PrehistoricFlocking.java L68-118](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/PrehistoricFlocking.java#L68-L118); [FA ai/FlockWanderGoal.java L52-60](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/FlockWanderGoal.java#L52-L60); [FA ai/FlockBuildGoal.java](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/FlockBuildGoal.java)
- **Fossils push mass scaling.** `Prehistoric.push(x, y, z)` takes `max = max(width, height)` of the standing dimensions. If max ≥ 3 it **ignores the push entirely**. Otherwise it scales horizontal push by `lerp((max(max,1) − 1)/2, 1, 0)`. — [FA Prehistoric.java L522-529](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/Prehistoric.java#L522-L529)
- **Fossils branch `1.20.1-gallimimus-fix`.** It is 2 commits ahead and 151 behind `1.20.1`. Its only flock change is "Fixed the bug where the game crashes with gallimimus flocks" (`98934e3d`, 2025-10-10, +20 lines in Gallimimus and +2/−1 in PrehistoricFlocking). It is a crash fix, not a movement fix. — [GitHub compare 1.20.1...1.20.1-gallimimus-fix](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/compare/1.20.1...1.20.1-gallimimus-fix)
- **AC Tremorzilla.** `isPushable() = false`, and a custom `push` nudges others by 0.05/tick. — earlier note [citadel_alexs_caves.md Q4]
- **IaF Stymphalian bird flock.** Followers get `getNearbyAirTarget` random targets when the leader's target changes. The leader is re-chosen at random if it dies. — [IAF entity/util/StymphalianBirdFlock.java L60-104](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/util/StymphalianBirdFlock.java#L60-L104)
- **IaF part pushing.** Non-dragon multipart parts push nearby entities away from the parent (`collideWithNearbyEntities`); dragon parts do not. — earlier note [ice_and_fire.md Q3]
- **Mowzie's.** `repelEntities` sets nearby velocities radially away (a circle). Umvuthana circle a target on a parametric circle with random direction flips. — [MM entity/MowzieEntity.java L510-524](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/entity/MowzieEntity.java#L510-L524)

### Inferences
- **Nublar's spacing is already more elaborate than any source.** Soft wedge slots, flocking steering and circle spacing go beyond everything here. The sources offer no tested non-circular spacing to copy. A snout-to-tail spine or capsule spacing would be new.
- **Single file is the proven low-jam pattern for confined terrain.** The elephant caravan and the Underzealot procession show it, because each animal paths only to a point behind one other animal. A hybrid could switch a herd from wedge slots to a column when the leader's path has a narrow cross-section. Citadel's slice check or a corridor width probe could detect that. This is an inference; no source switches formations.
- **Pushing is unreliable for separating big herd members.** Fossils disables push for ≥ 3-block bodies, and AC Tremorzilla is not pushable. Vanilla push is pairwise AABB overlap and weak (vanilla constants not re-read here). Separation must come from steering or slot assignment.
- **Rank-scaled V spacing gives slack to rear members.** AC's Subterranodon wedge spacing grows linearly with rank. That reduces rear-member collisions but widens the formation's footprint, which is bad in corridors.

### Gaps
- I found no source with herd sizes near 20 ground animals. The largest is 11 (Underzealot procession, single file).
- No issue reports were found about herd members jamming each other in these mods. The closest is Fossils #96 (big dinos clustering in one enclosure corner, Q6).

---

## Q5. How does Alex's Caves let necks and tails clip through walls without breaking gameplay, and what do players report?

### Takeaway
AC makes clipping harmless by construction:

- Neck and tail parts are `setPos`-placed and non-colliding. Only the torso box (5×8 or 6×8.5) moves and paths.
- Sauropods are immune to `IN_WALL` damage, and their navigator does not teleport.
- Luxtructosaurus also destroys leaves, logs and pewen branches it bumps into.
- The model reads its neck and tail bone angles from the part positions, so the visible neck goes where the parts go, including into walls.

I found **no player report about neck or tail clipping** in the AC tracker. The movement complaints are about whole-body jams, spinning and teleports.

### Cited Findings
- Parts never call `move()`, are not solid (`canBeCollidedWith` delegates to a non-colliding parent), and only `blocksBuilding`. The main box alone uses Citadel pathing. — earlier note [citadel_alexs_caves.md Q4]; [AC util/ACMultipartEntity.java L18-58](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/util/ACMultipartEntity.java#L18-L58)
- `IN_WALL` immunity: [AC SauropodBaseEntity.java L624-626](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L624-L626)
- Neck and tail target angles are hard-coded per animation, not terrain-aware. A search of `SauropodBaseEntity` for block queries finds only the stomp-crush ring (L318-345). — [AC SauropodBaseEntity.java L442-521](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L442-L521)
- Luxtructosaurus block clearing (tag: leaves, logs, pewen_branch): [AC LuxtructosaurusEntity.java L394-410](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/LuxtructosaurusEntity.java#L394-L410)
- Teleport removal after #852 (wall-clipping suffocation): [commit 51a08da4](https://github.com/AlexModGuy/AlexsCaves/commit/51a08da4)
- **Tracker searches.** On 2026-10-09 I searched the AC tracker for "neck", "tail through", "through walls", "atlatitan cave", "luxtructosaurus stuck", "suffocat" and "clip wall". None returned an issue about neck or tail clipping. "neck" returned only #139 (magnetic controls), and "through walls" only #581 (particles). — [AlexsCaves issues](https://github.com/AlexModGuy/AlexsCaves/issues)
- A web search for Atlatitan neck-clipping reports returned nothing relevant. CurseForge comments were not individually read. — (search, 2026-10-09)

### Inferences
- The absence of complaints is weak evidence that players accept neck and tail clipping from very large creatures, at least in cave biomes. Sauropods there are rare and boss-adjacent. Herd animals seen often in open terrain (Nublar's Gallimimus) may draw more attention to clipping. This is unverified.
- AC's model-follows-parts design means any hybrid that clamps neck or tail parts to avoid walls would also visibly bend the neck away from walls. That is a cheap way to make clipping less visible without touching movement. No source does this.

### Gaps
- CurseForge and Modrinth comment threads and the AC Discord were not searched. Player sentiment on clipping is unknown.

---

## Q6. What do the issue trackers show about which techniques failed in practice (stuck, pathfinding, large mob, hitbox)?

### Takeaway
The dominant failure across big-mob sources is **spinning or orbiting near a node**: AC Grottoceratops, AM gazelle and elephant, IaF baby dragons, dragons on stairs, and death worms. That is consistent with the arrival-tolerance problems in Q1 and with rate-limited turning in Q2.

The second failure is **stuck on corners or slopes**: AC Grottoceratops and Subterranodon on corners, and Grottoceratops unable to descend 1-block steps. The third is **teleport-based unsticking dropping big mobs into walls** (AC #852, fixed by disabling teleport).

Fossils reports big dinosaurs clustering in one enclosure corner (#96) and dinosaurs passing through thin walls (#120).

### Cited Findings
- **AC #1666 (open, 2025-10-06), "Dinosaurs stuck in place".** Grounded Subterranodons and Grottoceratops are "stuck on a corner" with erratic head movement. "Removing the blocks in front of them allows their AI to return to normal". — [AlexsCaves #1666](https://github.com/AlexModGuy/AlexsCaves/issues/1666)
- **AC #1313 (open, 2024-10-21), "Grottoceratops spinning on block edge"** (video only). **AC #1187** (open): Grottoceratops "start to almost uncontrolably spin around like the spider bug". — [#1313](https://github.com/AlexModGuy/AlexsCaves/issues/1313); [#1187](https://github.com/AlexModGuy/AlexsCaves/issues/1187)
- **AC #1579 (open, 2025-05-06), "Grottoceratops can't pathfind off of slopes".** On 1-block steps it follows the player "perpendicular to the slope, but not be able to climb down"; it is better at climbing up than down. — [#1579](https://github.com/AlexModGuy/AlexsCaves/issues/1579)
- **AC #852 (closed 2024-02-24).** Teleporting dinosaurs clip into walls and suffocate. It was fixed by the `NoTeleport` navigator. — [#852](https://github.com/AlexModGuy/AlexsCaves/issues/852); [commit 51a08da4](https://github.com/AlexModGuy/AlexsCaves/commit/51a08da4)
- **AC #1482 (open).** A ridden Tremorzilla slows and gets stuck going up or down hills. — [#1482](https://github.com/AlexModGuy/AlexsCaves/issues/1482)
- **IaF #4766 (closed, 2023-03-04), "Dragons have problem walking down hills".** Large dragons "keep spinning around on halfway", because `followThePath` "failed to reach the next path node… as the dragon grows it might not be able to drop down to somewhere that is only 1 block in width". The reporter proposed widening the Y tolerance in the multi-node look-ahead. The current IaF and Citadel code has a width-scaled drop tolerance (`ceil(w/2) × 3`). — [IaF #4766](https://github.com/AlexModGuy/Ice_and_Fire/issues/4766); [IAF AdvancedPathNavigate.java L731-740](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/pathfinding/raycoms/AdvancedPathNavigate.java#L731-L740)
- **IaF #1449 (closed 2019), "Myrmex and Dragon Pathfinding Megaissue".** A commenter reports workers walking "in circles" and sentinels walking "straight into walls and suffocate". The maintainer "removed experimental pathfinding. new pathfinding will be a tweaked vanilla one… less spinning and less janky movements". (That was before the Minecolonies port now in the code.) — [IaF #1449](https://github.com/AlexModGuy/Ice_and_Fire/issues/1449)
- **Other IaF reports.** #2963 (closed): baby dragons "start spinning randomly". #4730 (open, 1.18.2): death worms "constantly spinning". #2532 (closed): Myrmex colony "stuck swarming in one corner". #4175 (closed): baby dragons randomly teleporting 10-30 blocks, through walls. — [#2963](https://github.com/AlexModGuy/Ice_and_Fire/issues/2963); [#4730](https://github.com/AlexModGuy/Ice_and_Fire/issues/4730); [#2532](https://github.com/AlexModGuy/Ice_and_Fire/issues/2532); [#4175](https://github.com/AlexModGuy/Ice_and_Fire/issues/4175)
- **AM #64 (open, 2020), "Gazelle spinning".** Gazelles "doing random spins when moving". A later comment says it appears fixed but they "continuously jump" at 1-2-block rises. — [AM #64](https://github.com/AlexModGuy/AlexsMobs/issues/64)
- **AM #480 (closed 2021).** A wandering-trader elephant "keeps spinning weirdly". — [AM #480](https://github.com/AlexModGuy/AlexsMobs/issues/480)
- **AM #1422 (closed 2022).** Bison, moose and non-ridden elephants "still cannot step over 1 block heights", unlike rhinos and tusklins. — [AM #1422](https://github.com/AlexModGuy/AlexsMobs/issues/1422)
- **AM #1511 (open 2023).** Mobs walk "against a block which is in their way… endlessly until broken". — [AM #1511](https://github.com/AlexModGuy/AlexsMobs/issues/1511)
- **Fossils #96 (open, 2025-11-29).** Diplodocus, triceratops, ankylosaurus, pachyrhinosaurus and T. rex gather in one corner of a 25×25+ enclosure, "until everyone is in the corner". The reporter notes "only… the bigger animals do this". — [FA #96](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/issues/96)
- **Fossils #120 (open, 2026-04-06), "Dinos phase through thin walls".** The reporter attributes it to "hitboxs not matching model". Their reproduction involves unloading the area. — [FA #120](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/issues/120)
- **Fossils #58 (closed 2025, 1.18.2).** Spinosaurus cannot leave water and gets stuck at the shore. — [FA #58](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/issues/58)
- **Mowzie's public tracker.** No "hitbox" or "pathfinding" issues as of the earlier search. Known issues: Wroughtnaut slowed by flowing water (#19), Sculptor stuck in its pillar (#41). — earlier note [mowzies_mobs.md Q4-Q5]
- **CustomHitboxLib.** Two issues only, neither about movement. — earlier note [custom_hitbox_lib.md Q7]
- **Citadel.** Searches for "pathfinding" and "stuck" returned no pathfinding-behaviour issues; #207 is a ticking-entity crash. — [Citadel issues](https://github.com/AlexModGuy/Citadel/issues?q=pathfinding)

### Inferences
- **Spinning reports cluster on wide, rate-limited or vanilla-follower mobs.** Many predate the "Wide/NoSpin" navigators. AM's `GroundPathNavigatorWide` and AC's `GroundPathNavigatorNoSpin` (both 0.75·w tolerance, the second named for the symptom) look like the practical fix the Alex's-mods authors settled on. AC #1313 (2024) still reports spinning on a Citadel-navigated mob (Grottoceratops), so Citadel's tolerance (w/2 against a centred node, with look-ahead) does not eliminate it at block edges.
- **Fossils #96** does not by itself show a navigation defect. Random wander targets plus failures to path out of a corner would produce the same result. It is a warning that big-mob wander with the Mowzie-derived navigator can degrade into clustering, which is Nublar's symptom.
- **Corner jams (AC #1666) fit the square-footprint plus 4-connected planner model.** The planner believes a 1-block corner cut is fine for the torso footprint while the actual box grazes the corner. AC removed the teleport escape, and the no-teleport stuck handler only clears the path or moves away after 60 s, so the animal stays stuck for a long time. This is an inference; the reporter did not diagnose it.

### Gaps
- Most spinning reports have no developer diagnosis. The link to the arrival tolerance is my inference from code.
- I did not search CurseForge or Modrinth comments.

---

## Q7. Which of the six hybrid pieces are proven by a source, which are new, which conflict, and is any source technique better than the hybrid?

### Takeaway
Pieces 1, 3 and 6 are directly proven (AC sauropods). Piece 2 is proven, but it is already vanilla behaviour in 1.20.1. The proven part that matters is making the **arrival test** agree with the centred aim (Mowzie's/Fossils `isAt`, or the AM/AC 0.75·w tolerance), together with an AABB-sweep shortcut. Pieces 4 and 5 are new.

Piece 4 conflicts with piece 1 only if it becomes a hard constraint enforced in move control, which is the CustomHitboxLib failure. Piece 3 interacts badly with staircase paths and tight arrival tolerances unless path smoothing (sweep shortcut) and anti-spin guards come with it.

Source techniques the hybrid lacks:
- the async, width-centred Citadel planner with a tiered, no-teleport stuck handler;
- Fossils' growth revert;
- the Citadel/AM custom-collision stack that makes leaves passable for both collision and pathing (tiger);
- AC's tag-based leaf and log breaking;
- single-file caravan following for narrow terrain.

### Cited Findings
- **Piece 1 (torso box for collision and pathing): proven.**
  - AC sauropods: 5×8 and 6×8.5 main boxes, with neck and tail as non-colliding parts and Citadel pathing on the main box.
  - IaF dragon: main box 0.78×1.2 scaled to at most ×7, while render size goes to 30.
  - Vanilla Ender Dragon: `noPhysics`.

  — earlier notes [citadel_alexs_caves.md Q4], [ice_and_fire.md Q3], [fossils_alexsmobs_baseline.md Q1]
- **Piece 2 (centred node targets).**
  - Vanilla 1.20.1 already centres the aim ([V Path.java L76-82](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/level/pathfinder/Path.java#L76-L82)) but tests arrival at node + 0.5 ([V PathNavigation.java L228-241](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/ai/navigation/PathNavigation.java#L228-L241)).
  - Proven consistent fixes: Mowzie's `isAt` against `getNextEntityPos` ([MM L50-55](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathNavigateGround.java#L50-L55)) and Fossils `NavUtil.isAt` ([FA NavUtil.java L144-149](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/navigation/NavUtil.java#L144-L149)).
  - Citadel uses a centred footprint and centred aim ([CIT AbstractPathJob.java L458-476](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/pathjobs/AbstractPathJob.java#L458-L476)).
  - AM and AC loosen the tolerance to 0.75·w ([AM GroundPathNavigatorWide.java L23](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/GroundPathNavigatorWide.java#L23)).
- **Piece 3 (turn in place): proven** by the AC `SauropodMoveHelper` 15° gate with 2°/tick body yaw ([AC L150-152, L645-686](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L645-L686)). A softer, proven variant is Fossils `SmoothTurningMoveControl` ([FA L19-75](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/control/SmoothTurningMoveControl.java#L19-L75)).
- **Piece 4 (body-length clearance at sharp turns): not found in any source.** The nearest relative, CHL's per-rotation part-collision veto, is documented by its own FAQ as confusing the pathfinder. — [CHL EntityRotationMixin.java L30-133](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/src/main/java/dev/customhitboxlib/mixin/EntityRotationMixin.java#L30-L133); [CHL FAQ](https://github.com/gawrmonster/CustomHitboxLib/blob/17585f5d8ca9180e98f10485b6d94363d1c4d196/wiki/FAQ.md)
- **Piece 5 (spine or capsule herd spacing): not found.** Sources use caravans, random leader-relative targets or V slots (Q4).
- **Piece 6 (hit parts on a per-species spine): proven** by the AC procedural chain. Each segment is a fixed local offset rotated by smoothed neck and tail angles plus yaw-buffer lag, chained from the body centre and scaled by `getScale()`. Vanilla Ender Dragon and AM whale use the same latency-buffer pattern. AM anaconda and centipede segments follow the parent's back point, a positional chain. — earlier notes [citadel_alexs_caves.md Q2], [fossils_alexsmobs_baseline.md Q1, Q4]
- **Collision-and-pathing pass-through stack (Citadel `ICustomCollisions`, used by the AM tiger).** All four layers agree on the same blocks:
  - The entity overrides `collide(Vec3)` with `ICustomCollisions.getAllowedMovementForEntity`, which builds block collisions through `CustomCollisionsBlockCollisions` and skips blocks where `canPassThrough` is true.
  - The tiger returns true for bamboo and `#leaves`, and `isColliding` agrees.
  - Its node evaluator maps `LEAVES` and bamboo to `OPEN`.
  - Its navigator is `GroundPathNavigatorWide` with tolerance 1.2·w.
  - Citadel also ships `CustomCollisionsNavigator` (whose `canMoveDirectly` accepts a clip hit if the hit block is pass-through), `CustomCollisionsNodeProcessor` and `MovementControllerCustomCollisions` (no jump for pass-through blocks).

  — [CIT collision/ICustomCollisions.java L22-48](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/collision/ICustomCollisions.java#L22-L48); [CIT collision/CustomCollisionsNavigator.java L15-38](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/collision/CustomCollisionsNavigator.java#L15-L38); [AM EntityTiger.java L303-308, L485-487, L576-591](https://github.com/AlexModGuy/AlexsMobs/blob/09755dade2cfbdf14839e026d3af446f9d3ff843/src/main/java/com/github/alexthe666/alexsmobs/entity/EntityTiger.java#L576-L591)
- **Block clearing as an escape.** AC Luxtructosaurus (leaves, logs, pewen branches), Fossils angry hungry adults (hardness-limited), IaF dragons (main box +1, hardness by stage), and Citadel stuck-handler block breaking at levels 6-8 when enabled. — Q3 citations; [CIT PathingStuckHandler.java L290-295](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/PathingStuckHandler.java#L290-L295)

### Inferences
**Composition:**
- **Pieces 1 + 2 + Mowzie/Fossils sweep shortcut + consistent arrival.** These are fully compatible and proven together in Fossils. They need one Nublar-specific fix: choose either vanilla's corner-anchored `floor(w+1)` footprint with centred aim, or Citadel's centred footprint, and make arrival use the same point. Avoid Citadel's `2·floor(w/2)+1` under-sizing for 1 < w < 2, which matters for juveniles. A ceil-based symmetric footprint would avoid it. Cost: one AABB sweep per candidate node per tick, bounded by same-Y nodes.
- **Pieces 3 + 2.** Turn-in-place needs a generous arrival tolerance or a "reached when within tolerance and heading-aligned" rule. A standing, turning mob that overshoots by a fraction re-targets and turns again: the spin loop. Fossils' "skip turning when within 0.35 blocks" guard (horizontal distance² ≤ 0.12) is the proven anti-orbit measure to copy.
- **Pieces 3 + shortcut.** Sweep shortcuts remove most staircase micro-turns, so the hard gate fires mainly at real corners. Without shortcuts, a hard gate on 4-connected Citadel paths risks stop-start motion.
- **Piece 4 vs piece 1.** If parts never collide (piece 1), the length check is only a preference against visual clipping. Implement it in the planner or post-processor as a cost, or as a "turn here in place" flag, not as a veto in `setYRot` or `MoveControl` (CHL failure). A disc of radius equal to half the body length around the pivot at nodes with large heading change is the minimal check. Fallback when no clear route exists: accept clipping (AC practice).
- **Piece 5 vs piece 3.** Followers that stop to turn in place fall behind their slots, and slot targets then move further away. Slot assignment needs hysteresis, or followers should use the soft gate (Fossils) in open terrain. Capsule (segment-to-segment) spacing for 20 animals is 190 pair tests per herd tick, which is cheap. No source data exists on its behaviour.
- **Piece 6 + piece 5.** If spacing uses the same spine that places the parts, a single source of truth exists. AC computes the chain on both sides from synced state, so the spine is available on the server at no network cost.

**Techniques arguably better than parts of the hybrid for Nublar's stated symptoms (trees, walls, gaps, corridors):**
- **Trees.** The tiger pattern (leaves non-colliding for collision **and** pathing **and** move control) removes canopy snags without any length logic. AC's tag-driven leaf and log destruction is a griefing-dependent alternative. Both are proven in shipped mods. The hybrid has no tree-specific measure.
- **Corridors.** Switching from wedge to single-file caravan following (AM elephant, AC procession) avoids side-by-side jams that no spacing shape fixes when the corridor is narrower than two bodies.
- **Stuck recovery.** Citadel's tiered handler, with teleport disabled (AC and AM practice after #852), gives escalating recovery: clear path, move away, optionally break blocks. Vanilla's 100-tick test is the only fallback in the Mowzie/Fossils family.
- **Growth.** Fossils' revert-if-blocked `refreshDimensions` is needed because vanilla's fix-up stops above 4 blocks and Nublar dinosaurs grow continuously.

### Gaps
- No source evaluates capsule or spine spacing, oriented-footprint planning, or sharp-turn clearance. Pieces 4 and 5 have no field evidence and need prototyping.
- Performance of Citadel async jobs, AABB sweeps and Fossils' `findFreePosition` for large boxes is unmeasured in public sources. The only data point is a code comment.
- 26.x vanilla navigation internals were not checked. Whether the arrival mismatch still exists in the port target is unknown.
