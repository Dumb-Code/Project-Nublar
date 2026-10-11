# Server tick, entity AI and pathfinding mods: what they teach Project Nublar

Scope: six public repositories about server-side tick cost. Lithium, AI-Improvements, Immersive Optimization (IO), Very Many Players (VMP), Rail Optimization and Async Locator. This note extends the earlier survey ("Reference mod survey for Beta.md"). It does not repeat that survey's SmartBrainLib (SBL) sensor-gating result. Section 9 adds only new facts about SBL.

Labels: `[v]` verified in source (path and lines at the commit), `[doc]` README, wiki, changelog or store page, `[inf]` my inference. Every performance number that is not `[v]` is an estimate.

Paths below start at each clone's repository root. Lithium paths are shortened: `LI/` means `common/src/main/java/net/caffeinemc/mods/lithium/`.

---

## 1. Repositories read

| Repo | Branch read | Commit | Commit date | Licence (repo fact) | Loaders | MC versions | Size | Status |
|---|---|---|---|---|---|---|---|---|
| CaffeineMC/lithium | `26.2` (line numbers). `develop` also read | `59c9db1` (`26.2`); `416f68d` (`develop`, targets 26.3) | 2026-09-15; 2026-10-05 | LGPL-3.0 | Fabric, NeoForge (Quilt on Modrinth) | 26.1.x to 26.3 on Modrinth `[doc]`. `build.gradle.kts` line 12: compile target 26.2 on `26.2`, 26.3 on `develop` `[v]` | About 760 tracked files. About 135 million downloads `[doc]` | Active. Release `mc26.3-0.26.2` on 2026-09-28 `[doc]` |
| BuiltBrokenModding/AI-Improvements | `26.3` (the default branch `1.18` is stale) | `a51cab7` | 2026-10-03 | MIT, "applies to code only" (`LICENSE` line 3) | NeoForge and Forge (Modrinth) | 26.1.1 to 26.3 on Modrinth `[doc]`. Default branch: 1.18.2 `[v]` | 19 Java files | Maintained for 26.x (port commit 2026-10-03). The code is the 2019 to 2022 code, ported |
| Luke100000/ImmersiveOptimization | `26.2` (line numbers). `1.20.1` also read | `559386e` (`26.2`); `3aab8b1` (`1.20.1`) | 2026-07-14; 2026-07-13 | GPL-3.0 | Fabric, Forge, NeoForge | 1.20.1, 1.21.1, 1.21.11, 26.1.2, 26.2 (branches) | 48 tracked files, about 560 lines of mixin and scheduler code | Active. Wiki: https://github.com/Luke100000/ImmersiveOptimization/wiki (one page) |
| RelativityMC/VMP-fabric | `ver/26.1.1` (default). Branch `ver/26.2.0` also exists and was not read | `7e89476` | 2026-04-01 | MIT | Fabric only | 26.1 to 26.3 on Modrinth `[doc]` | 100 tracked files | Active (pushed 2026-10-09). README: "still in early development" |
| fxmorin/RailOptimization | `master` | `52ca530` | 2023-01-06 | No licence file. README: modpack use allowed, merging into clients needs permission | Fabric, Forge, Quilt | 1.19.3 to 1.19.4 only | One logic class, 330 lines | Dormant since 2023-05 |
| thebrightspark/AsyncLocator | `1.20.x` | `c79885f` | 2023-09-07 | MIT | Fabric, Forge | 1.19.x to 1.20.2 | 63 tracked files, 2,211 lines of Java | Dormant. Modrinth lists 1.20.2 as newest. No 26.x port found `[doc]` |

I did not build or run any code. All clones are read-only.

---

## 2. What to take from this note (top findings)

1. **Lithium does not speed up Nublar's brains.** SBL 2 `SmartBrain.tick` replaces the vanilla `Brain.tick` path. It never calls the method Lithium rewrites, and `ExtendedBehaviour` overrides the method Lithium caches `[v]` (section 3.7). Nublar's own sensor gating and registries carry all of the AI savings.
2. **Lithium's hopper and block-entity sleep work only for vanilla block entities.** Nublar machines that extend `BaseContainerBlockEntity` still get listeners, but a hopper next to them can never sleep `[v]` (section 3.2). Nothing in Lithium's public API changes that. The public API has only five small interfaces. The honest rule for Nublar: keep machine ticks near zero cost when idle, and expose the platform item API.
3. **Lithium reads Nublar's entity classes by reflection.** If a dinosaur or vehicle class overrides `canCollideWith`, `canBeCollidedWith` or `isPushable`, Lithium puts it in a different collision or cramming path `[v]` (section 3.3). One override on the dinosaur base class can slow every collision query nearby. This is a code-review rule for B26 and B27.
4. **IO slows far entities by skipping the whole `tickNonPassenger` call.** The entity's `tickCount` does not advance and its brain does not tick `[v]` (section 5). Distant dinosaurs "move in slow motion" `[doc]`. At default settings the tick interval reaches 17 to 20 ticks beyond the tracking range. Three Nublar patterns break under this: per-tick request queues (`FallbackBudget`), `tickCount` windows (`turnTick`, `% 20`) and tick-counted need decay. All three are fixable with small changes (section 5.5).
5. **Nublar should not build a global tick throttle.** It should do three things instead: (a) be tolerant of IO, (b) opt out the few entities that must tick every tick, using the `isAlwaysTicking()` hook that the 26.2 branch of IO reads `[v]`, (c) keep its own park-aware LOD inside its own sensors and behaviours (plan item B25.9). Section 5.7 gives the split.
6. **VMP gives Nublar large-park wins on Fabric only.** Its area maps remove the "every tracker times every player" loop and speed up `getNearestPlayer` for radii up to 128 blocks `[v]`. Nublar keeps detection radii at or below 128 blocks and loses nothing else (section 6).
7. **Digsite maps can freeze a server.** Plan B21 sells a map "to the nearest digsite" and requires `/locate` within 2,000 blocks. Vanilla `ExplorationMapFunction` and trade code call `findNearestMapStructure` on the server thread. Async Locator moves that call to a worker thread but is dormant at 1.20.2 `[doc]`. Nublar must either reuse the vanilla map function (so any async mod covers it) or copy the 60-line placeholder-map pattern (section 8). The `/locate` default radius is 100 chunks (1,600 blocks) `[v]` in Async Locator's command logic, which is less than the 2,000 blocks in the B21 acceptance line (open question Q1).
8. **Rail Optimization teaches one thing for the tour track:** change many track blocks in one pass with `UPDATE_KNOWN_SHAPE` and notify only the lane ends `[v]` (section 7). A tour vehicle needs no power signal on the track.

---

## 3. Lithium

### 3.1 Overview

**In game.** Mob AI, hoppers, collisions, chunk access and block-entity ticks use less CPU. Players see no change except a higher TPS and lower MSPT in large builds. Lithium states that it keeps vanilla behaviour, and lists the few deviations in its config file `[doc]` (`lithium-neoforge-mixin-config.md`, section `mixin.minimal_nonvanilla`).

**Mechanism.** About 100 mixin groups. Each group is a Java properties key (for example `mixin.ai.pathing`). The config file names requirements between groups. For example `mixin.block.hopper` needs `mixin.util.entity_movement_tracking`, `mixin.util.block_entity_retrieval`, `mixin.util.inventory_change_listening` and `mixin.util.item_component_and_count_tracking` `[doc]`.

**Platform overlap.** None of this exists in NeoForge or Fabric API. Lithium is a vanilla optimizer, not a modding API.

**Verdict.** Compat (be a good neighbour). Do not copy code. Adopt a few patterns (listed per area below).

### 3.2 Block-entity sleeping, inventory change tracking and hoppers

**In game.** A furnace, brewing stand, campfire, crafter, shulker box, hopper, sculk sensor, sculk catalyst or chest lid that has nothing to do is removed from the tick list until something wakes it. A hopper that cannot move anything sleeps until its neighbour inventory changes. In a hopper-heavy base this removes most hopper cost `[doc]`.

**Mechanism.**
- Each sleeping type is a separate mixin group under `mixin.world.block_entity_ticking.sleeping.*` (brewing_stand, campfire, chest_animation, crafter, furnace, hopper, sculk_catalyst, sculk_sensor_shrieker, shulker_box) `[v]` (directory `LI/mixin/world/block_entity_ticking/sleeping/`).
- The mechanism is a ticker swap. `SleepingBlockEntity` (`LI/common/block/entity/SleepingBlockEntity.java`, 85 lines) replaces the delegate inside vanilla's `RebindableTickingBlockEntityWrapper` with an empty ticker (lines 10 to 33 and 35 to 49). `wakeUpNow` puts the real ticker back (lines 65 to 73) `[v]`.
- `LevelChunkMixin` (`LI/mixin/world/block_entity_ticking/sleeping/LevelChunkMixin.java`, lines 17 to 38) stores the wrapper only when `blockEntity instanceof SleepingBlockEntity`. That interface is added by a mixin to each named vanilla class (for example `AbstractFurnaceBlockEntityMixin implements SleepingBlockEntity`) `[v]`.
- A sleeping block entity wakes on `setChanged()` (the mixin in `sleeping/BlockEntityMixin.java`, lines 12 to 16) and on block-state changes `[v]`.
- Change listening on inventories: `BaseContainerBlockEntityMixin` (`LI/mixin/util/inventory_change_listening/BaseContainerBlockEntityMixin.java`) is applied to **every** `BaseContainerBlockEntity` subclass. It keeps listener sets and fires `lithium$emitContentModified`. But the `InventoryChangeTracker` interface, which the hopper needs for "listen and sleep", is added only to Furnace, BrewingStand, Barrel, Chest, Dispenser, Hopper and ShulkerBox by name (`LI/mixin/util/inventory_change_listening/StackListReplacementTracking.java`, lines 16 to 68) `[v]`.
- Hopper logic: `HopperBlockEntityMixin.checkSleepingConditions` (`LI/mixin/block/hopper/HopperBlockEntityMixin.java`, lines 756 to 870) lets the hopper sleep only when the neighbour is (a) a block entity in mode `REMOVAL_TRACKING_BLOCK_ENTITY`, meaning it implements `InventoryChangeTracker`, or (b) no block inventory at all. In every other case the method `return`s and the hopper keeps ticking `[v]` (lines 778 to 830). Mode (b) additionally checks the platform item API: `PlatformModCompat.canHopperInteractWithApiBlockInventory` (lines 799 and 823). On NeoForge this asks `getCapability(Capabilities.Item.BLOCK, …)` and, if a handler exists, the hopper does **not** sleep (`neoforge/src/main/java/net/caffeinemc/mods/lithium/neoforge/NeoForgeModCompat.java` line 23, using `net.neoforged.neoforge.transfer.ResourceHandler`). On Fabric it asks `ItemStorage.SIDED.find` (`fabric/src/main/java/net/caffeinemc/mods/lithium/fabric/FabricModCompat.java`) `[v]`.
- The hopper "modification counter shortcut": if neither inventory changed since the last failed transfer, the whole transfer is skipped and only the comparator side effects are replayed (`LI/mixin/block/hopper/README.md`, section "Modification Counter Shortcuts and Comparator Updates") `[doc]`. This needs a `LithiumInventory` (a replaceable stack list).

**What Lithium assumes about modded inventories (`LI/mixin/block/hopper/README.md`).** `[doc]`
- A hopper moves one item at a time. The insertion test does not depend on stack size, unless the inventory implements `lithium$itemInsertionTestRequiresStackSize1()` (only the Chiseled Bookshelf does in vanilla).
- Inventory contents change only through the stack list (so the counters stay right) and through `setChanged()`.
- The comparator update pattern of a failed extract is "guaranteed to be calculated like vanilla if mods do not add item types with a non-power-of-two maximum stack size".

**Public API (the whole surface).** `common/src/api/java/net/caffeinemc/mods/lithium/api/inventory/` holds five interfaces `[v]`:

| Interface | What a mod implements | When it matters for Nublar |
|---|---|---|
| `LithiumInventory` (lines 30 to 60) | `getInventoryLithium()`, `setInventoryLithium(NonNullList<ItemStack>)`, optional `generateLootLithium()` | Machines that hold items in a `NonNullList`. Hoppers then use counters and skip failed transfers. Javadoc line 12: an inventory with a supporting block that implements `InventoryProvider` must not also extend `BlockEntity` |
| `LithiumDefaultedList` | `changedInteractionConditions()` | Call it when `canPlaceItem`, `canPlaceItemThroughFace` or `canTakeItemThroughFace` change without a content change. Example: a machine that accepts DNA items only while powered |
| `LithiumTransferConditionInventory` | `lithium$itemInsertionTestRequiresStackSize1()` | Return true if an insertion test fails for stacks larger than one |
| `LithiumCooldownReceivingInventory` | `setTransferCooldown(long)`, `canReceiveTransferCooldown()` | Only for hopper-like block entities (none in Nublar) |
| `LithiumCacheableInventory` | `hopperCanReuseInventoryUntilBlockEntityInvalidatedOrBlockStateChangedLithium()` | Exists on `develop` (26.3) only. It is **absent** on the `26.2` branch `[v]` (directory listing). Return false to stop hoppers from caching the inventory |

There is **no** API for sleeping block entities, no API for the movement trackers and no opt-out annotation. The `SleepingBlockEntity` and `InventoryChangeTracker` interfaces are internal (`LI/common/...`, not `common/src/api`) `[v]`.

**Lesson for Nublar (B12, B14, B15, B20, B25).**
- Nublar machines (`SyncingContainerBlockEntity extends BaseContainerBlockEntity` in `block/api/SyncingContainerBlockEntity.java` line 14 `[v]`) already emit Lithium's change events. A hopper next to a Nublar machine caches the block entity (`BLOCK_ENTITY` mode, `HopperBlockEntityMixin.java` lines 435 to 450) but cannot sleep `[v]`. So every hopper that touches a machine or feeder ticks at the vanilla cadence (once per 8-tick cooldown, plus the failed-attempt checks). With 20 hopper-fed machines this is a small, fixed cost `[inf]`. It does not scale with idle machines.
- Implementing `LithiumInventory` is an option for the **feeder** (B20) and the incubator. It adds an optional compile-only dependency on the Lithium API source set. It gains the failed-transfer shortcut, not sleep. I rate the gain small `[inf]`. Verdict for B20: do not add it in Beta. Revisit if a profile shows hopper cost.
- Nublar's own idle cost matters more. A machine tick that returns in under 100 ns when idle is enough (B12 `isIdle()` already exists in the plan). Rules that keep hoppers and Lithium fast:
  1. Call `setChanged()` only when contents or state change. A `setChanged()` on every tick wakes neighbour listeners and forces comparator work.
  2. Send block updates and `sendBlockUpdated` only on edges, not every tick (the earlier survey's item 4).
  3. Expose sided slots through `WorldlyContainer` or the platform item API. Do not expose internal slots (DNA, chips, upgrades) to hoppers.
  4. Do not hand out one `ItemStack` object to two inventories. Lithium tracks stacks by reference (`README.md` "item shadowing").
- For the fence post block entities (hundreds of them, B15): Lithium cannot sleep them. Use the vanilla pattern instead. Return `null` from `getTicker` unless the post needs a tick (for example only while energised or damaged), and rebind through a block-state change. Cost to verify in a profile: the rebind runs `LevelChunk.updateBlockEntityTicker` `[inf]`. Better still, give posts no ticker and drive them from the power network event (B14).

**Compatibility duty.**
- Do not replace the item list with a custom `Container` that bypasses `setChanged()`.
- If a machine stores items in a field Lithium cannot see (for example a `ItemStackHandler`-like object), a hopper can see stale data only if the machine is also a `Container`. Expose one view only: either `Container` or the platform item API. Both views must read the same list.
- Do not change a slot's `ItemStack` in place without calling `setChanged()` (the counter updates happen in Lithium's `ItemStackMixin`, per the hopper README).

**Platform overlap.** NeoForge 26.2 has the `Capabilities.Item.BLOCK` handler (`ResourceHandler<ItemResource>`) and Fabric has `ItemStorage.SIDED` `[v]` (Lithium's own compat classes call them). Use those for pipes and cross-mod logistics. Hoppers still use `Container` on vanilla blocks.

**Verdict.** Compat. Adapt the "early return plus edge-only update" rule (S).

### 3.3 Entity collision, cramming and class groups

**In game.** Walking dinosaurs, the player and vehicles collide with blocks and entities with fewer queries. Mob cramming checks skip entities that cannot be pushed. No visible change.

**Mechanism.**
- Lithium groups entities by **reflection on method overrides**. `EntityClassGroup` (`LI/common/entity/EntityClassGroup.java` lines 1 to 130) evaluates a class once and caches the answer in a per-class map `[v]`.
- Collision groups (`LI/common/entity/movement/CollisionEntityClassGroups.java`):
  - `CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE` = classes that override `Entity.canCollideWith(Entity)` (line 23). Entities in this group "attempt to collide with all other entities when moving" (comment line 17).
  - `BOAT_SHULKER_LIKE_COLLISION` = classes that override `Entity.canBeCollidedWith(Entity)` (line 39). Other entities treat them as block-like.
- The collision loop (`LI/common/entity/LithiumEntityCollisions.java`, comment at lines 125 to 128): if the **moving** entity's class overrides `canCollideWith`, "all types of entities may be (=> are assumed to be) required". Otherwise only entities in the block-like group are fetched `[v]`.
- Cramming groups (`LI/common/entity/pushable/PushableEntityClassGroup.java` lines 37 to 70). Two groups. `CACHABLE_UNPUSHABILITY` (lines 37 to 49) contains a non-player `LivingEntity` class only when it overrides neither `onClimbable` nor `isPushable`; for those classes Lithium may cache the "not pushable" answer `[v]`. `MAYBE_PUSHABLE` (lines 51 to 66) contains every class that overrides `Entity.isPushable` (and players); Lithium scans this list in the cramming test. So an `isPushable` override removes a class from the cacheable group and puts it into the scanned group `[v]`. The exact saving is not measured `[inf]`.
- Entity fetch by class: `ClassInstanceMultiMapMixin` (`LI/mixin/chunk/entity_class_groups/ClassInstanceMultiMapMixin.java` lines 17 to 85) adds a per-section set for each group, built lazily on first query and updated in `add` and `remove` `[v]`.
- Entity movement notification for hoppers and item merging uses `EntityMovementTrackerSection` and `SectionedEntityMovementTracker` (`LI/common/tracking/entity/`) `[v]`. These apply to item entities and `Container` entities. They do not apply to dinosaurs.

**Lesson for Nublar (B26, B27, B22).**
- Do **not** override `canCollideWith` on any dinosaur or vehicle unless the design needs it. One override moves the whole class into the "collide with everything" path `[v]`. The hitbox design does not need it (parts use vanilla collision).
- Override `canBeCollidedWith` only on entities that other entities must stand on or bump into like a boat: the tour vehicle and the gyrosphere, not dinosaurs. A dinosaur with this override becomes block-like for every other mover, so a herd of 40 adds 40 shapes to every neighbour's collision query `[inf]`.
- Override `isPushable` only with a reason. Dinosaurs in a herd are exactly the case where cramming and pushing run often. If the design wants "big dinosaurs do not push small ones", implement that inside one dinosaur-specific push method and leave `isPushable` alone `[inf]`.
- The reflection check is done once per class (cached), so there is no per-tick cost to Nublar. The cost is paid by every other mover that queries the area.

**Compatibility duty.** The three method names above must stay on the exact vanilla signatures. A renamed override (for example an override with an extra parameter) is invisible to Lithium and is harmless.

**Platform overlap.** None. Vanilla 26.2 collision stays as is.

**Verdict.** Adapt as a code-review rule. Effort S (a checklist item in B26 and B27).

### 3.4 Entity tracking, `getEntities` and the spatial structure

**In game.** Anything that asks "which entities are near this box" runs faster: sensors, item pickup, projectiles, explosions.

**Mechanism.**
- `mixin.entity.fast_retrieval` makes lookups over a small number of sections cheaper (`EntitySectionStorageMixin`) `[doc]`.
- `mixin.collections.entity_by_type` and `entity_filtering` replace maps with fastutil and avoid class-filter checks `[doc]`.
- `mixin.entity.replace_entitytype_predicates` rewrites four **named vanilla** call sites (ArmorStand, Golem stroll, Llama caravan, Old minecart behaviour) so they ask for the right class directly (directory `LI/mixin/entity/replace_entitytype_predicates/`) `[v]`.
- `mixin.entity.inactive_navigations` is a notification filter. `PathNavigationMixin` (`LI/mixin/entity/inactive_navigations/PathNavigationMixin.java` lines 33 to 61) marks a mob's navigation active only while it has a `path`. Block updates then notify only mobs with an active path `[v]`.

**Lesson for Nublar (B17, B18, B25).**
- The `inactive_navigations` idea is what Nublar needs for dinosaurs. A dinosaur that stands still must not be on the block-update notification list. Lithium does this inside `PathNavigation.recomputePath`, `moveTo` and `stop`. Nublar's `FenceAwareNodeEvaluator` and navigation subclasses call `stop()` through vanilla, so the Lithium mixin sees them `[inf]`. Nublar must not replace `PathNavigation` methods in a way that skips `stop()`.
- The earlier survey says Nublar queries entities with one `NearbyLivingEntitySensor` per scan. That already uses the fast path in `getEntities(EntityTypeTest, AABB, Predicate)` that Lithium accelerates. Always pass an `EntityTypeTest` or class, not `Entity.class` with an `instanceof` filter in the predicate `[inf]`.

**Compatibility duty.** None beyond the rules in 3.3.

**Platform overlap.** Vanilla already stores entities in sections. Lithium makes it cheaper. Nublar needs no own spatial index for dinosaurs (the earlier survey, S2).

**Verdict.** Adopt the pattern "no work for idle navigation" (already planned). Effort S.

### 3.5 POI search

**In game.** Villagers find beds and jobs faster. For Nublar the question is whether feeders should be POIs.

**Mechanism.** `PoiManagerMixin` (`LI/mixin/ai/poi/PoiManagerMixin.java`, `@Overwrite` at lines 54, 107, 148, 187) replaces `getRandom`, `findClosest`-style methods with one pass that collects `PoiRecord`s and sorts by a fixed ordering (`PoiOrdering.L2ThenInSquare`) `[v]`. It deviates from vanilla only in which of several equal points is chosen `[doc]`. `mixin.ai.poi.tasks` applies it to vanilla `AcquirePoi` and `LocateHidingPlace` `[v]` (directory listing).

**Lesson for Nublar (B17.1, B20).** The earlier survey chose a per-level feeder registry over POI. This stays correct. A POI type needs block-state registration, persists in region files and is queried through `PoiManager`; a registry is simpler and has no disk format. If Nublar later wants POIs (for example for a ranger station), Lithium speeds up the query without any Nublar code, as long as Nublar calls the vanilla `PoiManager` methods `[inf]`.

**Compatibility duty.** If Nublar adds a `PoiType`, use vanilla `PoiManager` queries only. Do not iterate `PoiSection` internals.

**Platform overlap.** Vanilla POI.

**Verdict.** Not relevant for Beta.

### 3.6 Pathfinding node caches

**In game.** Mobs plan paths with fewer block-state checks. Large herds benefit most.

**Mechanism.**
- `WalkNodeEvaluatorMixin` (`LI/mixin/ai/pathing/WalkNodeEvaluatorMixin.java` lines 24 to 90, priority 990) caches the `PathType` of a `BlockState` in the state object itself, because one state always maps to one type `[v]`. States that depend on neighbours (`null` in the cache) fall back to vanilla.
- `PathNodeCache.getNodeTypeFromNeighbors` (`LI/common/ai/pathing/PathNodeCache.java` lines 54 to 130) skips the 26-neighbour scan when the chunk section contains no "dangerous" states. It reads the section palette through `maybeHas` (line 17 to 20) or a block-counting listener (line 41 to 52) `[v]`.
- Also: `FlyNodeEvaluatorMixin`, `PathNavigationRegionMixin`, `PathfindingContextMixin` (cached chunk access) `[v]` (directory listing).

**Lesson for Nublar (B15, B17, B26).**
- Nublar has `FenceAwareNodeEvaluator` (modified in the working tree). If it extends `WalkNodeEvaluator` and overrides `getPathType`/`getPathTypeFromState` it will bypass or compose with the Lithium injection at the `getPathTypeFromState` call site. Keep calling `super` so Lithium's cache stays in effect `[inf]`.
- A fence post or wire block that returns `PathType.DANGER_*` or `BLOCKED` for pathing must do so as a function of its **block state only**. A cached type that depends on a block entity or on neighbours is wrong in Lithium's cache. Lithium's own comment (line 79 of `PathNodeCache.java`) lists the dynamic exceptions: shulker boxes and Fabric API dynamic definitions. A neighbour-dependent block must return `null` from the cache path (Lithium calls the vanilla neighbour code for it).
- The palette skip means open grassland sections with no fence or water pay almost nothing. A park with many fence blocks in every section loses this skip for those sections. Keep fences out of "path type" logic where possible: use the `UnreachableTargetSensor` route from the earlier survey (B15.3) instead of a custom path type for fences.

**Compatibility duty.** A custom path type from a block must come from `BlockState` alone. If it cannot, document it so Lithium's `mixin.ai.pathing` can be disabled for Nublar through the `lithium:options` override (section 3.9).

**Platform overlap.** Vanilla pathfinding. NeoForge adds `getPathType` hooks on `BlockState` (`IBlockStateExtension#getBlockPathType`) `[inf]`. Prefer that hook to a custom evaluator for single-block rules.

**Verdict.** Adopt (rule: state-only path types). Effort S.

### 3.7 Brain and goal ticking (and why SBL bypasses it)

**In game.** Villager and piglin brains start tasks faster. No visible change.

**Mechanism.**
- `mixin.ai.task.launch` (`LI/mixin/ai/task/launch/BrainMixin.java`): `@Overwrite startEachNonRunningBehavior` (lines 101 to 111) iterates a cached `possibleTasks` list. The list is rebuilt when `addActivity`, `removeAllBehaviors` and similar methods run (lines 56 to 76 and 190) `[v]`.
- `mixin.ai.task.memory_changes` (`LI/mixin/ai/task/memory_changes/BehaviorMixin.java`): `@Overwrite Behavior.hasRequiredMemories` (lines 40 to 58) caches the answer per behaviour until the brain's memory modification counter changes. The `Behavior` constructor replaces the `entryCondition` map with a `Reference2ObjectOpenHashMap` (lines 31 to 35) `[v]`.
- `mixin.collections.brain` uses fastutil maps for memories and sensors `[doc]`.
- Vanilla sensor streams are replaced with loops (`mixin.ai.sensor.replace_streams`) and "useless" sensors are removed (`mixin.ai.useless_sensors`: baby-only sensors, goat item sensor) `[doc]`. These touch vanilla sensor classes only.

**What happens with SBL 2 (verified against SBL `Main` at commit `deb0150`, 2026-10-05).**
- `SmartBrain<BO> extends Brain<BO>` (`common/src/main/java/net/tslat/smartbrainlib/api/internal/SmartBrain.java` line 44). It sets `this.availableBehaviorsByPriority = this.behaviours.asMapView()` (line 55) `[v]`.
- `SmartBrain.tick` (lines 409 to 425) is its own method: sort, `forgetOutdatedMemories`, sensors, `tryStartBehaviours`, `tickBehaviours`, activity update. It never calls `Brain.startEachNonRunningBehavior` `[v]`. So the `launch` mixin has no effect on SBL brains.
- `ExtendedBehaviour extends Behavior<BO>` (line 31) and **overrides** `hasRequiredMemories` (lines 335 to 343), looping over its own `getMemoryRequirements()` `[v]`. Lithium's `@Overwrite` of the base method is therefore bypassed for SBL behaviours.
- SBL swaps the Brain behaviours field. Lithium's cached `possibleTasks` would be stale for an SBL brain if the vanilla method were used. It is not used, so there is no stale-cache risk `[inf]`.
- What Lithium can still do for SBL brains: fastutil memory maps (`mixin.collections.brain`) and the `MemorySlot` listener (`MemorySlotMixin`), and nothing else `[inf]`.

**Lesson for Nublar (B17, B25).** The AI savings must come from Nublar. Section 9 lists what the SBL structure gives for free and what it does not.

**Compatibility duty.** None. Do not subclass vanilla `Behavior` with a custom `entryCondition` map type: `new Reference2ObjectOpenHashMap(map)` in Lithium's constructor hook copies the map `[v]`. SBL already does the right thing.

**Platform overlap.** SBL 2 owns the brain. NeoForge and Fabric add nothing.

**Verdict.** Not relevant for dinosaur brains. Document it so nobody expects a Lithium AI win for dinosaurs (B25).

### 3.8 Chunk access, game events and ticks

**In game.** Block reads, random ticks, scheduled ticks and raycasts are cheaper.

**Mechanism and Nublar relevance.**
- `mixin.util.chunk_access`, `world.chunk_access`, `chunk.no_locking`, `chunk.no_validation` shorten `Level.getBlockState` paths `[doc]`. Nublar gets this for free for every vanilla `getBlockState` call. Nublar must not read blocks through its own cache of chunk sections.
- `mixin.world.game_events.dispatch`: game-event dispatchers per chunk section are created "only when needed, i.e. when a listener is added to a section" `[doc]`. A dinosaur step event (`GameEvent.STEP`) in a section with no listener costs a map lookup. The earlier survey's item B15.5 (dinosaurs emit step events so sculk sensors react) is therefore cheap in empty areas `[inf]`.
- `mixin.world.tick_scheduler` packs scheduled ticks into integers `[doc]`. Nublar's `scheduleTick` calls (if any) go through it.
- `mixin.world.chunk_ticking.random_block_ticking` skips random-tick sections without tickable blocks using a block-count listener `[doc]`. Nublar blocks that use `isRandomlyTicking` (growing plants) lose this skip for their sections.

**Lesson.** Do not mark Nublar blocks as `randomTicks()` unless they need it. A single random-ticking block per section turns the skip off for that section `[inf]`.

**Verdict.** Adopt the rule (S).

### 3.9 Compat flags that a mod can set

**In game.** If a mod conflicts with one Lithium group, the mod can switch that group off, and the log prints who did it.

**Mechanism.**
- Both loaders read a custom metadata key `lithium:options` from each mod: `fabric.mod.json` `custom` object (FabricMixinOverrides, same key) and on NeoForge the mod's config element `lithium:options` in `neoforge.mods.toml` (`neoforge/src/main/java/net/caffeinemc/mods/lithium/neoforge/NeoForgeMixinOverrides.java` lines 13 to 35) `[v]`.
- Values must be booleans. Keys may be written with or without the `mixin.` prefix (`LI/common/config/LithiumConfig.java` lines 168 to 190). A mod may only **disable** (line 181: `!override.enabled() && option.isEnabled()` is the accepted case); an enable request is accepted only when the option has no defining mods (line 185) `[v]`.
- VMP ships an empty `"lithium:options": {}` (`VMP-fabric/src/main/resources/fabric.mod.json` lines 40 to 41) `[v]`. VMP has its own mirror: `vmp:incompatibleConfig` array in a mod's metadata forces a VMP option off, with a log line (`VMP-fabric/src/main/java/com/ishland/vmp/common/config/Config.java` lines 60 to 100) `[v]`.

**Lesson for Nublar (P2, B25).** Nublar needs neither at launch. Keep the mechanism in mind: if a playtest finds a Lithium group that corrupts a Nublar behaviour (candidates: `mixin.ai.pathing` with custom path types, `mixin.entity.collisions.*` with custom collision), the fix is a two-line entry in `neoforge.mods.toml` and `fabric.mod.json`. Do not ship a mixin plugin of our own.

**Verdict.** Compat (hold in reserve). Effort S.

### 3.10 Lithium summary table

| Area | Touches Nublar? | What Nublar does |
|---|---|---|
| Sleeping block entities | No (vanilla types only) | Own idle early-return; edge-only updates |
| Hopper caching and counters | Partly (any `Container` block entity is cached) | Sided slots, `setChanged` only on change; optional `LithiumInventory` later |
| Collision and pushable groups | Yes (by reflection) | No `canCollideWith`. Careful `canBeCollidedWith`, `isPushable` |
| Entity sections and movement trackers | Item entities and containers only | Use typed `getEntities` |
| Path node cache | Yes if Nublar path types are state-only | Keep path types state-only |
| Brain launch and memory cache | No (SBL bypasses) | Nublar-side gating |
| POI | No | Registry stays |
| Game events | Yes, cheaply | Keep step events |
| `lithium:options` | Reserve | Document |

---

## 4. AI-Improvements

**In game.** Passive animals and fish lose some idle goals if a server owner turns them off in config. Head turning of vanilla-style mobs uses a table-based `atan2`. A server owner can remove "look at player" and "random look around" goals for chosen mobs.

**Mechanism (branch `26.3`, commit `a51cab7`).**
- `ModifierSystem.onEntityJoinWorld(EntityJoinLevelEvent)` (`src/main/java/com/builtbroken/ai/improvements/modifier/ModifierSystem.java` lines 54 to 58) runs a filter tree on every entity that joins the level `[v]`.
- For every `Mob`: remove `LookAtPlayerGoal` and `RandomLookAroundGoal` if configured (lines 65 to 67), and replace the `LookControl` (lines 100 to 125) `[v]`. Both removals default to **false** (`ConfigMain.java` lines 83 to 84). The replacement defaults to **true** (line 85) `[v]`.
- Removal is `goalSelector.removeGoal` on the wrapped goals that match an `instanceof` predicate (`modifier/editor/ModifierLayer.java` lines 44 to 62) `[v]`. Other removals: fish swim/avoid/panic/follow leader, squid movement, cow/chicken/pig/sheep panic, breed, tempt, follow parent, stroll, float, eat block (all default false) `[v]` (`ModifierSystem.java` lines 71 to 138).
- `FixedLookControl` (`FixedLookControl.java`) overrides `getXRotD` and `getYRotD` with a 2D lookup table `FastTrig.atan2` (`FastTrig.java` lines 13 to 76). The replacement is applied only if the mob's look control class is **exactly** `LookControl` (line 106) `[v]`. Fields (`wantedX/Y/Z`, `lookAtCooldown`, `xMaxRotAngle`, `yMaxRotSpeed`) are copied (lines 36 to 44 of `FixedLookControl.java`) `[v]`.
- Why those goals cost: `LookAtPlayerGoal.canUse` runs a nearest-player or entity query for each mob on the goal tick; `RandomLookAroundGoal` rolls a random number every tick. Both are cheap per mob and noticeable at tens of thousands of mobs `[inf]`. The mod's own README says the tan cache gives a "small improvement 1.10+" `[doc]`.

**Lesson for Nublar (B09, B17, B26).**
- Nublar dinosaurs use an SBL brain. They have no `LookAtPlayerGoal` and no `RandomLookAroundGoal`. The goal-removal part never touches them `[inf]`. The mod is irrelevant to the cost of the look behaviour.
- The look cost in Nublar is `DinosaurLookAtTarget` (`common/.../ai/behaviour/actions/DinosaurLookAtTarget.java`), an `ExtendedBehaviour` with `noTimeout()`. The earlier survey already covers brain-start cost. The mod's real lesson is the **filter idea**: every idle behaviour needs a species or state switch so that servers can turn it off. A server owner with 400 sleeping dinosaurs wants the idle look behaviour gated by `isResting`. Nublar already does this with `stopIf(isResting || isDrinking || isDeadOrDying)` at `Dinosaur.java` line 622 `[v]`. Good.

**Compatibility duty.**
- Nublar's `Dinosaur` look control: if it stays the exact class `LookControl`, AI-Improvements swaps in `FixedLookControl` on join. That copies state and keeps `getWantedX/Z`, so `Dinosaur.java` lines 495 to 497 still work `[inf]`. Head angles become quantised by the table (a few hundredths of a degree) `[inf]`. If B26/B27 add a custom look control for neck IK, the exact-class check makes the mod skip it `[v]`. No action needed either way.
- If Nublar ever adds vanilla-style goals to a non-dinosaur mob (for example the live bait feeder's captured cow), the mod's global toggles can remove them for that mob type. Document that Nublar relies on none of the vanilla goals being present.

**Platform overlap.** Vanilla `LookControl` and `Mth.atan2` already use sine/atan tables (since 1.16) `[inf]`. SBL has `LookAtTarget` and `LookAtTarget`'s cooldown fields `[inf]`.

**Verdict.** Not relevant (compat only). The status "ported to 26.3 on 2026-10-03" means players may install it.

---

## 5. Immersive Optimization (IO)

### 5.1 In game

A dinosaur far from every player moves and thinks in slow motion. Close to the player nothing changes. A dedicated server with 400 dinosaurs spread over a big park runs fewer entity ticks per second. The wiki says: "Far away entities basically move in slow motion." `[doc]` (wiki page `Home`.)

### 5.2 Mechanism

Branch `26.2`, commit `559386e`.

- Two mixins. `ServerLevelMixin` (`common/.../mixin/ServerLevelMixin.java`) injects at the head of `ServerLevel.tick` (to update stress) and at the head of `ServerLevel.tickNonPassenger(Entity)` with `cancellable = true`: if `shouldTick(entity)` is false, the whole entity tick is cancelled (lines 20 to 25) `[v]`. `LevelMixin` injects at `Level.shouldTickBlocksAt(long)` for block entities, off by default (`LevelMixin.java` lines 12 to 17; `Config.enableBlockEntities = false`) `[v]`.
- Vanilla `tickNonPassenger` is the method that increments `tickCount` and then calls `entity.tick()` `[inf]` (my memory of vanilla; IO's cancel at the head skips both). Consequences: `tickCount` does not advance, and `baseTick`, `aiStep`, the brain, effects and animation state do not run `[inf]`.
- Priority: a worker thread (`Thread worker`, priority `MIN_PRIORITY`, daemon, sleeps 500 ms between passes; `TickScheduler.java` lines 29 and 39) recomputes a map `entityId -> level` for every active entity (`tickLevel`) `[v]`.
- `shouldTick` (`TickScheduler.java` lines 216 to 227):
  - an entity with `isAlwaysTicking()` ticks every tick (line 217; in the `1.20.1` branch this was `entity.noCulling`) `[v]`;
  - `priority <= 1` ticks every tick (line 224);
  - otherwise it ticks when `(data.tick + entity.getId()) % priority == 0` (line 226). The id offset spreads entities over ticks `[v]`.
- `getPriority` (lines 229 to 281):
  - type blacklist (`config.isBlacklisted`), projectiles when `cullProjectiles` is false, and entities in force-loaded chunks (unless `optimizeForceLoadedChunks`) return 0, which means "always tick" `[v]`;
  - `minDistance` = squared distance to the nearest player; `blocksPerLevel` starts at 64;
  - **render-distance and viewport culling apply only on an integrated server with one player** (line 246) `[v]`;
  - tracking-range culling: if the nearest player is farther than the entity type's scaled tracking distance, `blocksPerLevel` drops to 10 (`blocksPerLevelTrackingCulled`, lines 255 to 260) `[v]`;
  - stress: when the average tick time exceeds `stressedThreshold` (45 ms default), `stressedTicks` rises by 2 per tick (cap 600) and falls by 1 otherwise (lines 160 to 170); this scales `blocksPerLevel` down by up to `minDecreaseFactor` 0.25 (lines 274 to 276) `[v]`;
  - level = `floor((sqrt(minDistSq) - minDistance(6)) / max(2, blocksPerLevel)) + 1`, clamped to `1..maxLevel (20)` (line 280) `[v]`.
- Config (`config/Config.java`): `entities` map of rules, checked in this order: entity type, entity tag (`#namespace:tag`, smaller tags win), mod namespace, `*` `[doc]` (wiki "Blacklist"). Default rules block `minecraft:player`, `minecraft:ender_dragon`, `minecraft:ender_pearl`, `create`, `valkyrienskies`, `#minecraft:arrows` and a few more; `*` is true `[v]` (`Config.java` lines 84 to 102 on `1.20.1`).

Worked example for a Nublar park on a dedicated server (my arithmetic from the formulas above, values `[inf]` where marked):
- Dinosaur `clientTrackingRange(10)` (`EntityInit.java` lines 30 to 39 `[v]`) = 160 blocks, scaled down by the server's view distance `[inf]`.
- Nearest player 100 blocks away: `(100-6)/64 = 1.47` -> level 2. The dinosaur ticks every second tick.
- Nearest player 190 blocks away (beyond tracking range): `(190-6)/10 = 18` -> level 19. It ticks once per 0.95 s.
- Nearest player 400 blocks away: level 20 (cap). One tick per second. Its motion and need timers run at 5 % speed `[inf]`.

### 5.3 How IO treats modded entities

By default every entity not on the blacklist is throttled. A mod namespace can be blacklisted in config. There is no API, no event and no tag shipped for other mods. The only in-code opt-out is the entity-side flag `isAlwaysTicking()` (26.2 branch) or `noCulling` (1.20.1 branch) `[v]`. Whether `Entity.isAlwaysTicking()` is a vanilla method on 26.2 is `[inf]` from IO's call; the call compiles against the 26.2 mappings `[v]`, so the method exists.

### 5.4 What IO skips, and what it does not

| Skipped on a throttled tick | Not skipped |
|---|---|
| The whole `tickNonPassenger` call: `tickCount++`, `Entity.tick`, `aiStep`, movement, brain, sensors, goals `[inf]` | Entity tracker packets (the tracker runs from the chunk map, not the entity tick) `[inf]` |
| Passengers? `tickNonPassenger` is for non-passengers. A passenger ticks through its vehicle (`tickPassenger`) `[inf]` | Damage events and `hurt` calls from other entities `[inf]` |
| | Block-entity ticks (default off) `[v]` |
| | Data written by other entities' ticks |

### 5.5 What breaks in Nublar code today (concrete)

All locations are in the working tree (some files are uncommitted). `[v]` means I read the lines.

1. `FallbackBudget.tryAcquire` (`entity/ai/behaviour/social/FallbackBudget.java` lines 26 to 52). The class doc says "A member that wants a path asks every tick; a member that stops asking drops out of the queue." Line 30 removes queue entries whose last request is more than one game tick old `[v]`. A member ticked every 2nd, 5th or 17th tick is removed from the queue between its own asks. It then never moves up the queue `[inf]`. At level 1 (a player within 70 blocks) nothing changes.
2. `Dinosaur.setYRot` (`Dinosaur.java` lines 520 to 524) compares `turnTick` with `tickCount` and with `tickCount - 1` `[v]`. `tickCount` only advances when the entity ticks, so this stays correct under IO (both sides use the same counter). It would break if someone mixed it with game time.
3. `Dinosaur.java` line 581 (`tickCount % 20`) and `DinoOneShots.java` line 70 (`tickCount % CHECK_TICKS`) `[v]` measure entity ticks. Under IO a "second" lasts N real seconds. Harmless for a leader flag. Not harmless for the startle check if the design says "check every second" `[inf]`.
4. `staminaDrainTick` (`Dinosaur.java` lines 723 and 760 to 765) counts entity ticks `[v]`. Stamina recovery and need decay therefore slow down by the same factor as movement. The motion and the need clocks stay in sync (a feature), but the park "sleeps" when no player is near `[inf]`. The plan item B25.9 says "Needs keep ticking". A need system based on elapsed **game time** keeps ticking correctly; a counter-based one does not.
5. Herd code uses `level.getGameTime()` for deadlines (`ghostGraceUntil`, `lastProgressTick`, `GroupManager`) `[v]`. A throttled member sees `now - lastProgress` jump by N each tick. Any threshold smaller than N (for example a stall timeout in ticks) fires without a real stall `[inf]`. Read the stall constants in `HerdMoveControl` before B26 and keep every stall threshold above 20 ticks, or scale it by the elapsed time since the member's previous tick.
6. SBL sensors use game time (`ExtendedSensor.tick`: `nextTickTime <= level.getGameTime()` then `nextTickTime = gameTime + scanRate`, lines 96 to 104) `[v]`, so a throttled sensor scans at its next entity tick and is not stuck. Good. `ExtendedBehaviour` end times use game time too (`endTimestamp = gameTime + runtime`, line 247) `[v]`. Good.
7. Brain memory expiry runs once per brain tick (`SmartBrain.forgetOutdatedMemories` calls `MemorySlot::tick` for each expirable memory, `SmartBrain.java` lines 440 to 453) `[v]`. If `MemorySlot.tick` decrements a counter (vanilla's design) `[inf]`, a memory with a 100-tick lifetime lives 100 **brain** ticks, which is 100 x N game ticks for a throttled dinosaur. "Forget the threat after 5 s" then means 5 s x N. For short memories (alerts, flee) that is the wrong direction for gameplay and for compat.

### 5.6 Compatibility duty (checklist for P2 and B17/B25)

1. **Use game time for deadlines**, never `tickCount`, for anything that must mean "seconds of world time": need decay, cooldowns, grace periods, stall detection, memory lifetimes. Store the last-tick time and apply `elapsed = now - last` (clamped to a cap such as 40 ticks).
2. **Per-tick queues must tolerate gaps.** `FallbackBudget` should expire a request after `max(2, elapsed since the member's last tick) + 1` ticks, or expire by member state, not by game tick.
3. **Opt out only entities that cannot tolerate gaps:** the tour vehicle on track (a vehicle with no player in or near it would crawl and desync with its stops), any entity in an active cinematic or hunt with a player within the tracking range (IO's level 1 inside 70 blocks already covers the latter), and the gyrosphere. Implement `isAlwaysTicking()` (or the 1.20.1 `noCulling` field if the port is staged) in those classes only `[inf]`. A vehicle carrying a passenger ticks through the passenger's vehicle path and is distance-checked against the player in it, which is 0 `[inf]`.
4. **Do not opt out the dinosaurs.** The point of IO for big parks is dinosaurs. If a player wants a different policy, they edit IO's `entities` map. Nublar can document a suggested rule: leave `projectnublar:*` at the default, and blacklist `projectnublar:ford_explorer`.
5. **Chunk loaders:** IO's default does not throttle entities in force-loaded chunks (`optimizeForceLoadedChunks = false`, `TickScheduler.java` line 181 / `getPriority` line 235) `[v]`. If Nublar adds a park chunk loader, those dinosaurs run at full speed. That is expected and good for a park but it hides IO's saving.
6. **Multipart hitboxes:** IO throttles the parent only. Parts (B26/B27) follow the parent's position set by the parent tick `[inf]`. A part that moves by its own tick would be throttled separately and drift. The hitbox design already makes parts follow the parent; keep that.
7. **Server tick budget display:** IO's `/io report` prints per-dimension rates. Nublar's own `HerdTimings` logs should print game ticks, not entity ticks, so numbers compare `[inf]`.

### 5.7 Should Nublar build its own LOD, or rely on IO?

Decision proposal (open to the lead):

| Option | What it costs | What it gives | Verdict |
|---|---|---|---|
| A. Rely on IO | Nothing. Tolerance fixes from 5.6 (S each) | Entity-wide slow motion for players who install IO. Not available on stock installs | Compat only |
| B. Own global throttle (mixin on `tickNonPassenger`) | An L subsystem and a global hook. Violates the earlier survey's rule B25.5 "no tick mixin on a vanilla base class" | Same as IO | **Avoid** |
| C. Own park-aware LOD **inside** SBL (distance-scaled `scanRate`, gated activities) | M. One cached "nearest player distance" per dinosaur per second | Brain cost falls without slow motion. Movement and needs stay at full speed | **Adopt** (plan B25.9, extended) |

Design for C (all on SBL's public API, `[v]` for each hook):
- Each dinosaur keeps `nearestPlayerBand` in a field (0 = within 64 blocks, 1 = within 128, 2 = beyond). A cheap sensor recomputes it with `level.getNearestPlayer(x, y, z, 128, false)`. Keep the radius at 128 or below so VMP's area map answers it (section 6).
- Sensors use `scanRate(e -> baseRate + band(e) * baseRate + (e.getId() & 15))`. `scanRate` takes a function (`ExtendedSensor.scanRate(ToIntFunction)`, lines 34 to 43) `[v]`.
- Behaviours read the band in `startCondition`. Idle wandering and grazing run at band 2 only every 100 ticks (`cooldownFor` takes a function, `ExtendedBehaviour.cooldownFor`, lines 122 to 140) `[v]`.
- Needs use elapsed game time (rule 1 in 5.6), so a quiet park stays correct without observers.
- Entities with a target, a threat or a fence interaction stay at band 0 whatever the distance. A hunt nobody sees is still a hunt.
- Result: brain cost for a 400-dinosaur park falls roughly in proportion to the share of dinosaurs outside 64 blocks `[inf]`. Movement is vanilla speed everywhere. IO, if installed, stacks on top and Nublar tolerates it.

**Platform overlap.** NeoForge and Fabric API have no tick-throttle API. Vanilla 26.2 has simulation distance and entity-ticking chunk checks only `[inf]`. SBL gives `scanRate`, `onlyScanIf`, `cooldownFor` and `startCondition` `[v]`.

**Verdict.** Compat (tolerate) + Adapt (own SBL-level LOD). Effort for tolerance: S each, four items. Effort for the LOD band: M.

---

## 6. VMP (Very Many Players)

### 6.1 In game

A server with many players spread over a big map keeps a high TPS. For a park server with a few players and hundreds of dinosaurs, the entity tracker part still helps, because it removes a loop over "every tracked entity for every player". It is Fabric-only. A NeoForge server does not get it.

### 6.2 Mechanism (`ver/26.1.1`, commit `7e89476`)

- **Area maps.** `AreaMap<T>` (`src/main/java/com/ishland/vmp/common/maps/AreaMap.java`, 282 lines) maps a chunk key to the set of objects that cover that chunk, each with its own view distance. A player's chunk lookup returns the set directly: `getObjectsInRange(long)` (lines 55 to 58) `[v]`.
- **Entity tracking.** `NearbyEntityTracking` (`.../common/playerwatching/NearbyEntityTracking.java`, 281 lines). Long-lived trackers sit in an `AreaMap`; each tick, for every player, it compares the player's current tracker set with the area-map set and starts or stops tracking only for the differences (`tick0`, lines 160 to 205) `[v]`. New trackers (items, arrows, projectiles) wait in a "staging area" for 200 ticks (`STAGING_TRACKER_LIFETIME`, line 70) and are handled the vanilla way, then migrate to the area map (`tickStaging`, lines 207 to 240) `[v]`. The trick: short-lived entities never pay the area map's insert and remove cost.
- A tracker's chunk reach is `ceil(maxTrackDistance / 16) + 1` (`getChunkViewDistance`, lines 263 to 265) `[v]`, so a larger `clientTrackingRange` costs more map cells per tracker.
- **Position transformers** for entities that move in another coordinate frame: `EntityPositionTransformer`, with a Valkyrien Skies implementation loaded only if that mod is present (lines 23 to 38) `[v]`. This is the compat pattern for moving-frame entities.
- **Nearby players.** `MixinServerWorld.getClosestPlayer` (`.../mixins/playerwatching/optimize_nearby_player_lookups/MixinServerWorld.java` lines 22 to 51) answers from the player area map when `maxDistance` is at most `GENERAL_PLAYER_AREA_MAP_DISTANCE * 16` and non-negative; otherwise it calls vanilla (line 29) `[v]`. The constant is the largest `SpawnGroup` immediate-despawn range divided by 16, rounded up (`AreaPlayerChunkWatchingManager.java` lines 17 to 20) `[v]`. In vanilla that range is 128 blocks `[inf]`, so the fast path covers radii up to 128 blocks.
- **Other pieces** `[doc]` (README): own chunk sending with optional packet-level rate limit, packet-level per-player render distance, async chunk load on login, async portals, split Netty event loops, an optimized ticket propagator, async logging.
- `MixinEntity.move` (`.../mixins/entity/move_zero_velocity/MixinEntity.java` lines 10 to 25): after a bounding-box change, the next `Entity.move` call with a zero vector is cancelled once `[v]`. This removes a wasted zero move for entities whose box changed.

### 6.3 Lesson for Nublar

- Hundreds of dinosaurs are hundreds of trackers. Vanilla loops over trackers and players. VMP's area map shows the right structure if Nublar ever needs its own "dinosaurs near a player" index. It does not need one: vanilla already gives `getEntities` by section, and the earlier survey's S2 query is a single typed section query.
- The staging idea fits Nublar's short-lived entities: dropped meat, eggs in flight, tranquilizer darts. Nothing to build; the mod does it.
- The `getClosestPlayer` lookup is the one place Nublar code interacts with VMP. Keep every "is a player near" radius at or below 128 blocks to stay on the fast path. The earlier plan's detection range (sight, scent, noise) is far below that `[inf]`.
- `clientTrackingRange(10)` (160 blocks) for every species (`EntityInit.java` lines 30 to 39) is large for a Velociraptor (0.6 blocks wide) and small for a Brachiosaurus (12.5 blocks tall). A per-species range scaled by body size costs nothing and cuts tracker reach for small species `[inf]`. Vanilla values for reference: 10 chunks for animals, 8 for monsters `[inf]`. Decision for B16/B25 (open question Q4).
- Zero-move cancel: B27's anti-clipping ("deterministic constraint on the server tick") may rely on a zero move after a pose change to push a dinosaur out of a wall `[inf]`. On Fabric with VMP the first zero move after a box change is dropped. Test this on the Fabric playtest build with VMP installed.

### 6.4 Compatibility duty

- Dinosaur positions must come from `Entity.position()` (the tracker reads `getEntityPos` and `getPos`). Do not keep a private "true" position for a dinosaur that differs from the entity position `[inf]`.
- A tour vehicle that moves along a track is a normal entity. No position transformer is needed unless the track sits on a moving structure (Valkyrien Skies): out of scope.
- Do not raise the tracking range of small entities above 10 chunks. It enlarges both vanilla and VMP cost.

### 6.5 Platform overlap and verdict

VMP is Fabric only. NeoForge has no equivalent in this survey. Vanilla 26.2 tracking is unchanged `[inf]`. There is no platform API for tracking.

**Verdict.** Compat (Fabric players will install it). Adopt the rule "radius 128 or less" (S) and the per-species tracking range (S).

---

## 7. Rail Optimization

### 7.1 In game

Long lines of powered rails switch on and off up to four times faster `[doc]` (README: "up to 4x faster"). The player sees a whole line of rails light at once instead of a wave.

### 7.2 Mechanism (`master`, commit `52ca530`, MC 1.19.3)

- One logic class `common/src/main/java/ca/fxco/RailOptimization/RailLogic.java` (about 330 lines) plus an `@Overwrite` of `PoweredRailBlock.updateState` at priority 990 in the Fabric and Forge modules `[v]`.
- Vanilla recomputes the state of each rail separately, and each change sends a block update to its neighbours, which recompute again: a wave of O(n) updates, each of which scans up to 8 blocks `[inf]`.
- The mod computes the whole lane in one pass. `powerLane` (lines 162 to 178) sets the starting rail, then `setRailPositionsPower` (lines 198 to 214) walks both directions up to `RAIL_POWER_LIMIT` (8) rails, keeps a `HashMap<BlockPos, Boolean>` of positions already decided and sets each rail with the flag set `UPDATE_MOVE_BY_PISTON | UPDATE_KNOWN_SHAPE | UPDATE_CLIENTS` (line 23) `[v]`. Because the flags suppress neighbour and shape updates, no cascade starts.
- Afterwards it sends the suppressed updates **once**, to the blocks around the whole lane and to the two ends (`updateRails`, lines 280 to 330, `neighborUpdateEnd`, `shapeUpdateEnd`) `[v]`.
- De-powering mirrors this (`dePowerLane`, lines 180 to 196) `[v]`.

### 7.3 Lesson for Nublar (B22)

- The Ford Explorer follows a track the player lays. A track block with a "powered" or "connected" state that updates through neighbour updates would repeat vanilla's cascade for a 200-block tour. Nublar has no power on track (plan decision D07: no fuel), so the cascade is only about **shape** (which neighbours a track block connects to).
- Rule 1: set track blocks with `UPDATE_KNOWN_SHAPE` when Nublar itself changes many at once (a "lay 20 blocks" tool, a creative copy, a structure placement), then notify only the ends.
- Rule 2: the vehicle does not read blocks every tick. Keep one `TrackPath` per connected track (a list of positions or a spline) in a per-level map. Rebuild it when a track block is placed or removed (an edge event), clear it on level unload (B25.4). The vehicle then samples the path by distance along it. This is the same "registry on load, remove on removal" pattern as the feeder registry (B17.1) `[inf]`.
- Rule 3: stops and signs are data on the path, not blocks the vehicle searches for.
- Rule 4: do not copy the `@Overwrite` approach. A mixin on a vanilla block is fragile across versions; this mod is stuck at 1.19.4.

### 7.4 Compatibility duty

Vanilla rails and minecarts coexist with Nublar track blocks only if Nublar track is a separate block class. A track block that extends `BaseRailBlock` would be hit by this mod's and Lithium's minecart mixins (`LI/mixin/block/hopper/OldMinecartBehaviorMixin.java` and `LI/mixin/entity/replace_entitytype_predicates/OldMinecartBehaviorMixin.java` exist `[v]`). The plan already uses a separate `TrackBlock`.

### 7.5 Platform overlap and verdict

Vanilla 26.2 has `MinecartBehavior` classes (Lithium's file names show `OldMinecartBehavior`, so a "new" behaviour class also exists) `[inf]`. The new minecart behaviour interpolates a cart along rails with client-side lerp steps `[inf]`. The plan chose a separate track block and a vehicle entity. Whether to reuse the vanilla minecart path logic is a design choice for B22 (open question Q3).

**Verdict.** Adapt the "bulk change plus ends only" rule and the cached `TrackPath` (S to M). Avoid the code (dormant since 2023).

---

## 8. Async Locator

### 8.1 In game

A player who opens an explorer map chest, buys a cartographer map or runs `/locate` does not freeze the server for seconds. The map shows "Locating…" and fills in when the search finishes `[doc]`.

### 8.2 Mechanism (`1.20.x`, commit `c79885f`)

- `AsyncLocator` (`Common/src/main/java/brightspark/asynclocator/AsyncLocator.java`, 180 lines) owns a fixed thread pool (size from config, lines 25 to 43). `locate(...)` submits a task that calls vanilla `ServerLevel.findNearestMapStructure` (tag form, line 110) or `ChunkGenerator.findNearestMapStructure` (holder-set form, lines 132 to 133) on the worker thread, then completes a `CompletableFuture` `[v]`.
- The result goes back to the main thread with `server.submit(...)` (`LocateTask.thenOnServerThread`, lines 167 to 170) `[v]`. The class doc says to use it for any change to game data (lines 154 to 157).
- Map creation: `ExplorationMapFunctionLogic.updateMapAsync` (`logic/ExplorationMapFunctionLogic.java` lines 60 to 73) makes an empty `FILLED_MAP` item with a hover name "locating" and an NBT flag (`CommonLogic.createEmptyMap`, lines 29 to 34), starts the task and returns the item at once. When the task finishes, `updateMap` fills the map data, renders the biome preview, adds the target decoration and removes the flag (`CommonLogic.updateMap`, lines 98 to 114) `[v]`. A map name cache expires after 5 minutes (line 20) `[v]`.
- Hooks (mixins into vanilla): `ExplorationMapFunctionMixin` at the `findNearestMapStructure` call inside the loot function `run` (`mixins/ExplorationMapFunctionMixin.java` lines 33 to 56); `TreasureMapForEmeraldsMixin` at the same call inside the villager trade `getOffer` (lines 41 to 66); `LocateCommandMixin`; `EnderEyeItemMixin`/`EyeOfEnderMixin`; `DolphinSwimToTreasureGoalMixin` `[v]`.
- `/locate` uses a fixed search radius of 100 chunks (`LocateCommandLogic.java` line 25: `locate(..., 100, false)`) `[v]`.

### 8.3 Lesson for Nublar (B21)

- B21 plans paleontologist map trades and an explorer-map item "that leads to the nearest digsite, optionally naming a species". If Nublar builds those through the **vanilla** `minecraft:exploration_map` loot function and the vanilla `TreasureMapForEmeralds` trade, then any async-locate mod that hooks those two calls covers Nublar for free. Both are data-driven or vanilla classes `[inf]`. If Nublar instead calls `findNearestMapStructure` inside its own `ItemListing` or its own map item, no external mod covers it and the trade freezes the server.
- A server-thread structure search in a fresh world loads chunks up to the `structure_starts` stage in rings around the player `[inf]`. A rare structure means many rings. This is the "explorer-map trades freeze servers" case in the task statement.
- The mod is dormant (1.20.2 newest on Modrinth `[doc]`). Do not depend on it. Two safe routes:
  1. Use the vanilla map function and trade classes, and make the digsite frequent enough that the search ends in a few rings (open question Q1 and Q2). No async needed.
  2. Copy the pattern: placeholder item with a "locating" flag, a single-thread executor shared by Nublar, `server.submit` for the write-back, and a timeout that turns the map into "no digsite found". About 80 lines `[inf]`. Do not copy its code; it is MIT, but the pattern is short and the 26.2 map and component APIs differ from 1.20.
- `/locate structure projectnublar:digsite` is vanilla's command. It blocks. For the B21 acceptance line it is a one-off dev command, not a gameplay path.

### 8.4 Compatibility duty

- Keep the digsite as a normal registered `Structure` in a normal `StructureSet` with a `random_spread` placement. Async tools and `/locate` handle only structure sets `[inf]`.
- Do not write map data from a worker thread. Worker threads may only call the search method.
- The map item must survive being created empty and filled later (components in 26.x; the mod used NBT) `[inf]`.

### 8.5 Platform overlap and verdict

Vanilla 26.2 has no async locate that I could find; the 26.2 changelogs were not read `[inf]`. Neither NeoForge nor Fabric API provides one. The locate call is a vanilla server-thread call.

**Verdict.** Adapt (pattern only; use vanilla map classes). Effort S if vanilla classes are used; M if Nublar builds its own async path.

---

## 9. Extending the earlier SBL sensor-gating result

The earlier survey says: gate block-scanning sensors with `onlyScanIf`, add `scanRate`, jitter starts, and registry-based feeder search. It also flags that the SBL 2.0.3 changelog claim of random sensor offsets was unverified. This section adds only new facts (SBL `Main`, commit `deb0150`, 2026-10-05).

| New fact | Source | Effect on Nublar |
|---|---|---|
| `SmartBrain` constructor calls `sensor.randomlyDelayStart(random)` for every sensor | `SmartBrain.java` lines 60 to 62 `[v]` | The jitter exists in library code. Whether it moves `nextTickTime` depends on vanilla `Sensor.randomlyDelayStart` `[inf]`. Keep Nublar's own `(e.getId() & 15)` offset in `scanRate`; it is harmless and it is deterministic |
| Sensor scans compare game time: `if (nextTickTime <= gameTime) { nextTickTime = gameTime + scanRate… }` | `ExtendedSensor.java` lines 96 to 104 `[v]` | Safe under IO throttling (section 5): a late tick scans once and reschedules from now. No burst of catch-up scans |
| `scanRate` and `cooldownFor` accept functions of the entity | `ExtendedSensor.java` lines 34 to 43; `ExtendedBehaviour.java` lines 122 to 140 `[v]` | Distance-band LOD (section 5.7) needs no SBL change |
| `onlyScanIf` failing "will skip that scan tick and will not try again until the next scan tick" | `ExtendedSensor.java` doc, lines 53 to 58 `[v]` | A gated sensor costs one predicate per scan interval, not per tick |
| `SmartBrain.tick` runs `behaviours.sort()` every tick | `SmartBrain.java` line 414 `[v]` | A per-tick sort for every dinosaur. Cost depends on SBL's implementation of `sort()` (not read). Profile it with the SBL profiler sections `smartBrainPreTick` etc. (`SmartBrain.java` lines 411 to 425) `[v]` |
| Memory expiry decrements per brain tick (`forgetOutdatedMemories` -> `MemorySlot::tick`) | `SmartBrain.java` lines 440 to 453 `[v]` | Short memories stretch under throttling (section 5.5, item 7). Prefer expiry by stored game-time stamp for gameplay-critical memories |
| `hasRequiredMemories` is overridden and loops over a list each call | `ExtendedBehaviour.java` lines 335 to 343 `[v]` | Lithium's cache does not apply. With 11 idle behaviours per dinosaur (earlier survey S4) this loop runs 11 times per tick. Fewer memory requirements per behaviour, and `cooldownFor` on idle behaviours, are the lever |
| SBL ticks the brain from a `WrapOperation` around `Mob.customServerAiStep` inside `serverAiStep` | `common/src/main/java/net/tslat/smartbrainlib/mixin/common/MobMixin.java` lines 14 to 21 `[v]` | Confirms the earlier "double brain tick" warning. Also: a `Mob` that IO throttles skips `serverAiStep`, so the brain does not tick `[inf]` |

---

## 10. Cross-cutting compatibility rules (one page for B25)

1. Game time for meaning, entity ticks only for rendering and local timers (IO).
2. No `canCollideWith`/`canBeCollidedWith`/`isPushable` overrides on dinosaurs without a written reason (Lithium).
3. Path types from block state only (Lithium).
4. Machines: idle early return, `setChanged()` on change only, edge-only block updates, sided slots (Lithium).
5. `getNearestPlayer` radius 128 or less (VMP).
6. Track: bulk change plus end notification, cached path (Rail Optimization).
7. Digsite maps: vanilla classes, or placeholder-map pattern (Async Locator).
8. No global tick hook, no mixin on vanilla base classes (earlier survey B25.5). This keeps Nublar out of the mixin conflict space of every mod in this note.
9. Reserve: `lithium:options` entry in `neoforge.mods.toml` and `fabric.mod.json` if a playtest shows a conflict.

---

## 11. Technique table

Effort: S under 100 lines, M one or two classes, L new subsystem.

| Technique | Source | Nublar module | Verdict | Effort | Expected effect |
|---|---|---|---|---|---|
| Idle early return in machine ticks; `setChanged()` on change only | Lithium sleeping `[v]`, hopper README `[doc]` | B12, B14, B20 | Adapt | S | Machine tick cost near zero when idle `[inf]` |
| Optional `LithiumInventory` on feeder and incubator | Lithium API `[v]` | B20 | Compat (defer) | S | Small hopper gain `[inf]` |
| Fence posts with no ticker unless energised; drive by power-network event | Lithium cannot sleep modded BEs `[v]` | B14, B15 | Adapt | M | Hundreds of idle posts cost nothing per tick `[inf]` |
| No `canCollideWith` on dinosaurs; careful `canBeCollidedWith`, `isPushable` | Lithium class groups `[v]` | B26, B27, B22 | Adapt (review rule) | S | Keeps Lithium's fast collision path for all movers `[inf]` |
| Navigation off the update list when idle | Lithium `inactive_navigations` `[v]` | B17 | Adopt (keep calling `stop()`) | S | Fewer block-update notifications to idle dinosaurs `[inf]` |
| Path types from block state only | Lithium `PathNodeCache` `[v]` | B15, B17 | Adopt | S | Keeps palette skip `[inf]` |
| Typed `getEntities` only | Lithium entity sections `[doc]` | B17, B18 | Adopt | S | Cheaper sensor queries `[inf]` |
| Random-tick blocks only when needed | Lithium random ticking `[doc]` | B01, B21 | Adopt | S | Keeps section skip `[inf]` |
| Step game events stay on | Lithium game-event dispatch `[doc]` | B15, B19 | Adopt | none | Cheap when no listener `[inf]` |
| `lithium:options` override in mod metadata | Lithium `[v]` | P2, B25 | Compat (reserve) | S | Fix a conflict without a mixin `[inf]` |
| Goal removal by config filter | AI-Improvements `[v]` | B17 | Not relevant | none | None for SBL brains `[inf]` |
| Keep dinosaur look control exact class or fully custom | AI-Improvements `[v]` | B26, B27 | Compat | none | No conflict either way `[inf]` |
| Game-time deadlines for needs, cooldowns, stall tests | IO `[v]` | B17, B25, B26 | Adopt | M (four call sites) | Correct behaviour under any throttle |
| Per-tick request queues tolerate gaps (`FallbackBudget`) | IO `[v]` | B26 | Adapt | S | Herd fallback paths keep working under IO `[inf]` |
| `isAlwaysTicking()` on tour vehicle and gyrosphere | IO 26.2 `[v]` | B22 | Compat | S | Vehicle keeps full speed under IO `[inf]` |
| Own distance-band LOD inside SBL (`scanRate`, `cooldownFor`, gated activities) | IO idea, SBL API `[v]` | B25.9 | Adapt | M | Brain cost falls with distance; no slow motion `[inf]` |
| Own global entity tick throttle | IO `[v]` | none | Avoid | L | Not needed; breaks rule B25.5 |
| Nearest-player radius at or below 128 | VMP `[v]` | B19, B25 | Adopt | S | Uses area-map fast path on Fabric `[inf]` |
| Per-species `clientTrackingRange` by body size | VMP tracker reach `[v]` | B16, B25 | Adapt | S | Fewer tracker cells for small species `[inf]` |
| Test pose-change anti-clip on Fabric with VMP | VMP zero-move cancel `[v]` | B27 | Compat | S | Avoids a one-tick regression `[inf]` |
| Bulk track change with `UPDATE_KNOWN_SHAPE`, notify ends only | Rail Optimization `[v]` | B22 | Adapt | S | No cascade on long track `[inf]` |
| Cached `TrackPath` per connected track, rebuilt on edge events | Rail Optimization (idea), feeder registry (earlier survey) | B22, B25.4 | Adapt | M | Vehicle reads no blocks per tick `[inf]` |
| Vanilla map classes for digsite maps and trades | Async Locator hooks `[v]` | B21 | Adopt | S | Any async-locate mod covers Nublar; no own freeze `[inf]` |
| Placeholder map plus worker thread plus `server.submit` | Async Locator `[v]` | B21 | Adapt (only if own map path) | M | No server stall on map purchase `[inf]` |
| Frequent digsite placement so searches end in few rings | design, `[inf]` | B21 | Adapt | S | `/locate` and maps finish fast `[inf]` |

---

## 12. Open questions

Real design gaps only.

**Q1. Digsite search distance versus the `/locate` radius.** B21's acceptance line says `/locate structure projectnublar:digsite` must find a digsite "within 2000 blocks". Vanilla's `/locate` search radius is 100 chunks, which is 1,600 blocks (`LocateCommandLogic.java` line 25 shows 100 `[v]`; vanilla's own value `[inf]`). The default radius of an exploration-map loot function is smaller `[inf]`. Which distance does the lead want to guarantee, and for which of command, map and trade? The placement `spacing` of the digsite structure set follows from the answer.

**Q2. Map search cost.** Does the lead accept a short wait ("Locating…") on a fresh world, or must the map appear at once? If a wait is acceptable, one shared worker thread plus a placeholder map is enough. If not, the maps could point to pre-placed digsites kept in a per-level list of known digsites, which requires no search at all (a registry, like the feeder registry). This choice changes the B21 data model.

**Q3. Reuse vanilla minecart behaviour for the tour track?** B22 has its own `TrackBlock`. Vanilla 26.2 has a rail-following behaviour with client interpolation `[inf]`. Is reusing it for the Ford Explorer in scope, or is a separate path object (section 7.3) the plan? This decides whether the rail rules in section 7 apply or whether vanilla handles them.

**Q4. Tracking range per species.** All species use `clientTrackingRange(10)` today. Should B16 set the range from body size (small species lower, sauropods higher)? A larger range increases tracker cost in vanilla and in VMP. A smaller range makes small dinosaurs pop in sooner.

**Q5. Park observers and force-loading.** The earlier survey's B25.9 says needs keep ticking without observers. Vanilla only ticks entities inside the simulation distance of some player `[inf]`. Does the lead want a park chunk loader (a block that keeps the park's chunks entity-ticking)? If yes, IO and similar mods leave those chunks alone by default (section 5.6, item 5), and the cost of a fully ticking 400-dinosaur park is Nublar's own budget, not a mod's.

**Q6. Which entities may a server owner throttle?** Nublar can only document a recommended IO rule set. Should the docs recommend throttling dinosaurs (cheap park, slow far dinosaurs) or protecting them (full speed, higher cost)? This is a product statement for the mod page, not code.

---

## 13. Sources

Local clones (shallow, read-only), in the session scratchpad `clones/` folder: `lithium` (`26.2` at `59c9db1`, `develop` at `416f68d`), `AI-Improvements` (`26.3` at `a51cab7`), `ImmersiveOptimization` (`26.2` at `559386e`, `1.20.1` at `3aab8b1`), `VMP-fabric` (`ver/26.1.1` at `7e89476`), `RailOptimization` (`master` at `52ca530`), `AsyncLocator` (`1.20.x` at `c79885f`), plus the IO wiki (`Home.md`). SBL files were read through `gh api` at `Tslat/SmartBrainLib` `Main`, commit `deb0150`. Store data (versions, loaders, download counts, licence ids) came from the Modrinth API on 2026-10-10.

- https://github.com/CaffeineMC/lithium
- https://github.com/BuiltBrokenModding/AI-Improvements
- https://github.com/Luke100000/ImmersiveOptimization and its wiki
- https://github.com/RelativityMC/VMP-fabric
- https://github.com/fxmorin/RailOptimization
- https://github.com/thebrightspark/AsyncLocator
- https://github.com/Tslat/SmartBrainLib
