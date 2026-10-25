# Create add-ons survey for Project Nublar

Scope: what each Create add-on adds, how it joins another mod's systems, and what Nublar can learn for B12 (machines), B14 (power), B15 (fences), B20 (feeders), B22 (vehicles), B25 (performance) and P2 (port). Create core is covered by another researcher. This note reads the add-ons only.

Labels: `[v]` verified in source (path and lines at the commit named), `[doc]` README, wiki, changelog or store page, `[inf]` my inference. A performance number that is not `[v]` is an estimate.

## 1. Repos read

All clones are shallow (`--depth 1`, blob filter) in the scratchpad. Nothing was built or run. Line numbers refer to the commit in the table.

| Repo | Branch read | Commit | Date | Licence (repo fact) | Loaders and MC in the branch | Size | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Layers-of-Railways/Railway (Steam 'n' Rails) | `1.20/dev` | 7441960c | 2026-08-27 | LGPL-3.0 | Forge, Fabric, NeoForge; 1.20.1 only (`gradle.properties`: `minecraft_version = 1.20.1`, mod 1.7.3-beta) | about 100 MB | Active. Branch `1.20/create-v6` exists. No 1.21 branch `[v]` |
| mrh0/createaddition (Crafts & Additions, CCA) | `1.21.1` | 1c81fab | 2026-10-01 | MIT | NeoForge, MC 1.21.1, Create 6.0.10 (`gradle.properties`). Fabric branches stop at 1.20.1 | 6 MB | Active |
| hlysine/create_connected | `main` | be43db7 | 2026-09-04 | AGPL-3.0 | NeoForge, MC 1.21.1, Create 6.0.10 | 12 MB | Active |
| talrey/CreateDeco | `1.21-neo` | 996780b | 2026-04-14 | CC0-1.0 | NeoForge, MC 1.21.1 | 6 MB | Slow. Last push 2026-04-15 |
| george8188625/Create-Diesel-Generators (CDG) | `1.21.1` | bed7a1c | 2026-09-25 | MIT | NeoForge, MC 1.21.1 | 4 MB | Active |
| DragonsPlusMinecraft/CreateEnchantmentIndustry (CEI) | `1.21.1/6.0.0-dev` | 7cb2e9f | 2026-10-09 | LGPL-3.0 | NeoForge, MC 1.21.1. Store page also lists Fabric 26.1.2 builds (see section 2) | 4 MB | Active |
| DragonsPlusMinecraft/CreateCentralKitchen (CCK) | `1.21.1/6.0.0-dev` | df9cc5c | 2026-10-09 | LGPL-3.0 | NeoForge, MC 1.21.1 | 3 MB | Active |
| antarcticgardens/create-new-age (CNA, GitLab) | `1.20.1` (plus `1.21.1` branch at 57f66eb, 2026-08-26) | 9ff732f | 2026-06-08 | `LICENSE` file covers code only; assets all rights reserved (README) | Forge and Fabric, MC 1.20.1 (`minecraftVersion=1.20.1`, mod 1.2.0). GitLab branches: `1.19.2/forge`, `1.20.1`, `1.21.1`, `master`, `mc1.21.1/ponders`, `mc1.21.1/thermometer` | small | Active, slow |
| aesefficio/BellsAndWhistlesMod | `1.20.1-forge` | e06fb2f | 2026-06-26 | GPL-3.0 | Forge and NeoForge on MC 1.20.1 (also `1.21-forge`, `mc1.20.1/fabric`) | 7 MB | Active, but Modrinth page last updated 2025-03-13 `[doc]` |
| PssbleTrngle/CreateJetpack | `main/neoforge/1.21.x` | 0b1228f | 2026-08-11 | repo `LICENSE.md` (GitHub reports none) | NeoForge 1.21.1, Kotlin. Separate Fabric and Forge 1.20.x branches | 5 MB | Maintained |
| Cannoneers-of-Create/CreateBigCannons (CBC) | `create-v6-1.21.1` | 46a5d2a | 2026-08-22 | Mixed: CC BY-NC-SA 4.0 and MIT files in repo | NeoForge only (`enabled_platforms=neoforge`), MC 1.21.1, mod 5.11.7 | 23 MB | Active |
| Creators-of-Aeronautics/Simulated-Project | `main` | 8d482a5 | 2026-10-02 | Repo `LICENSE.md` (assets all rights reserved, code separate) | NeoForge 1.21.1, mod 1.3.2, needs Sable 2.x | 34 MB | Very active, 918 stars |
| ryanhcode/sable (physics library under Simulated) | `main` | not cloned | 2026-10-05 | Polyform Shield 1.0.0 | Fabric and NeoForge, MC 1.21.1 `[doc]` | 55 MB | Active |

Other repos used: `chaevsfe/createaddition`, branch `fabric-26.2`, commit c7aae67 (2026-10-08), an unofficial Fabric port of CCA for Minecraft 26.2 `[v]` via `gh api`.

## 2. Which add-ons are alive on 26.x

Short answer: none of the 12 repos has a 26.x branch from its own author. A parallel Fabric-only line exists.

- Official Create (Creators-of-Create/Create): the newest branch is `mc1.21.1/dev` `[v]` (branch list by `gh api`). The Modrinth page for `create` lists Forge and NeoForge up to 1.21.1, last update 2026-04-21 `[doc]`.
- **Create Fly** (Modrinth `create-fly`, source `ZurrTum/Create-Fly`, CC0-1.0): a from-scratch Fabric build of Create for Minecraft 26.2. Versions listed: 26.2-pre-2 to 26.2. Updated 2026-06-15. It needs no Fabric API and avoids Porting-Lib and Registrate-Refabricated `[doc]`. No NeoForge build exists on Modrinth for 26.x (search for "create" with NeoForge and 26.x returned no Create mod) `[doc]`.
- Add-ons on Create Fly (Modrinth, all updated 2026-10-08 except where noted `[doc]`):
  - Crafts & Additions: unofficial port, source `chaevsfe/createaddition`. Its `gradle.properties` pins `minecraft_version = 26.2`, `create_fabric_version = 26.2-rc-2-6.0.9-1`, `team_reborn_energy_version = 5.0.0` `[v]`.
  - Steam 'n' Rails: two ports ("unofficial port", and "Continued", 2026-09-11, 22,898 downloads).
  - Create Connected: two ports.
  - Diesel Generators, Bells & Whistles: one unofficial port each.
  - Enchantment Industry: the author's own "Fly" builds, 2.5.2-c, 2026-08-22, Fabric 26.1.2. They depend on Create Fly and `create-dragons-plus` `[doc]`.
- No 26.x port found for: Create New Age, Central Kitchen, Create Deco, Create Jetpack, Create Big Cannons, Simulated/Aeronautics (Modrinth `create-aeronautics`: NeoForge, 1.21.1 only) `[doc]`.

**Meaning for Nublar:** an optional Create link on 26.2 is possible only on Fabric, through Create Fly. A NeoForge 26.2 build has no Create to link to today. Rule: build every Create link as a separate optional layer behind a `ModList`/`FabricLoader.isModLoaded` check, and never put it in `common/`. Re-check the Create status when the P2 port finishes.

## 3. Per-mod findings

### 3.1 Create Crafts & Additions (CCA): electricity, wires, energy bridge

**In game.** The player builds an electric motor or alternator on the stress network. The alternator turns stress into FE. Copper, gold and electrum wires on spools join connectors in the air. Connector blocks sit on any block face and push or pull FE from the block they touch. The player right-clicks a connector with a spool, then right-clicks another connector. A sagging wire appears. Goggles show the flow in FE per tick. Barbed wire hurts mobs. Tesla coil, capacitor and modular accumulator (multiblock battery) round out the set `[doc]`.

**Mechanism: FE and stress bridge `[v]`.**
- The electric motor draws FE and gives stress capacity. `CommonConfig.java:108-112` sets `fe_at_max_rpm = 480` FE/t and `max_stress = 16384` SU, both "at 256 RPM". The motor's capacity is `MAX_STRESS/256` per RPM (`ElectricMotorBlockEntity.java:97-101`), and its draw is `FE_RPM * |rpm|/256` with a minimum (`:157-158`). The motor's energy buffer takes at most 5,000 FE per call (`CommonConfig.java:126-127`).
- The alternator reverses it: output `FE_RPM * |rpm|/256 * 0.75` (`AlternatorBlockEntity.java:134-136`, efficiency `CommonConfig.java:156-157`). It pushes into neighbours through cached capabilities (`:102-121`). The alternator's stress impact equals the motor's capacity, so one alternator loads one stress network by a known number.
- The bridge exposes `IEnergyStorage` (`Capabilities.EnergyStorage.BLOCK`) on each block and finds neighbours with `BlockCapabilityCache` (`AbstractConnectorBlockEntity.java:393-400`, `AlternatorBlockEntity.java:46-56`). It holds no list of known blocks.

**Mechanism: wire network `[v]`.**
- Rules at connect time are in `IWireNode.connect` (`IWireNode.java:564-594`): both ends must exist; node index must be valid (`COUNT`); the wire length must not pass the shorter of both ends' limits (`LONG`); a pair may hold one wire only (`EXISTS`); two large connectors reject copper and festive wire (`REQUIRES_HIGH_CURRENT`). Lengths: small connector 16 blocks, large 32 (config range 0 to 256, `CommonConfig.java:180-195`). A small connector has 4 wire nodes, a large one 6 (`SmallConnectorBlockEntity.java:53`, `LargeConnectorBlockEntity.java:53`).
- Wire types carry a transfer cap in FE/t: copper 256, gold 1,024, electrum 8,196, festive 256 (`WireType.java:9-12`). I could not find in the connector code where this cap is applied to the flow; the connector caps come from config (`small_connector_max_input/output = 1000`, large 5000, `CommonConfig.java:175-191`). Treat the wire cap as display-level `[inf]`.
- There is no wire health. A wire is removed when an end is broken or the pair moves too far apart (Sable check every 10 ticks, `AbstractConnectorBlockEntity.java:273-279`). A broken end drops the wire as an item (`:323-350`).
- The whole network is one saved graph per dimension: `WireGraph extends SavedData` (`WireGraph.java:44-67`). Vertices and inbound edges live in `Long2ObjectOpenHashMap` keyed by `BlockPos.asLong()`. The class comment says energy flows "however many unloaded chunks the wires cross, and no chunk is ever loaded to find out" (`:36-44`, 1.7.2 changelog: "Reworked Connectors should now function across unloaded chunks" `[doc]`). A `WireGraphVerifier` checks stored wires against disk with at most 16 async chunk reads in flight (`WireGraphVerifier.java:12-13`, `:25-40`).
- Networks rebuild only when something changes. `dirtyPorts` is a set; `rebuildNetworks()` runs a breadth-first walk from the touched ports and returns early when the set is empty (`WireGraph.java:296-335`, `:382`). Each tick: `verifier.tick()`, prune dangling wires, rebuild if dirty, then `network.tick(id)` per network (`:418-433`).
- Energy moves in a two-buffer swap per network: `inBuff` and `outBuff` swap each tick (`EnergyNetwork.java:38-49`). Producers `push`, consumers `pull`. A consumer sees only last tick's production. Capacity is `min(members * (outDemand + 2*inDemand + 10), 80000)` (`:34-36`). One network object, no per-node energy storage on the wires.
- Each connector block entity ticks cheaply: it asks for its network only when the cached one is invalid (`IWireNode.java:321-332`), then does one `external.getCapability()` and one `receiveEnergy` or `extractEnergy` pair (`AbstractConnectorBlockEntity.java:285-308`). Mode `None` returns before the transfer (`:265`).

**Mechanism: wire renderer `[v]`.** Details are in section 4.

**Lesson for Nublar.**
- B14: copy the shape "one network object per connected run, rebuilt on change, ticked once per tick, members never tick their own energy". This matches the earlier survey's remedy S7/B15.1 (cache fence power per network). CCA proves the shape at CCA's scale.
- B14/B15: store the run graph in `SavedData` keyed by `long` positions, so a fence run that spans unloaded chunks of a big park keeps its power state. Nublar's fence posts are small block entities, so this applies even though Nublar has no air-spanning wire. [inf] Effort L, only if parks cross unloaded chunks in practice.
- B14: the motor/alternator numbers show a clean bridge rule: pick one reference RPM (256), one FE rate at it (480 FE/t), one stress at it (16,384 SU), then scale linearly. If Nublar ever offers a Create-power machine, use the same linear rule.
- B15: the connect-result enum with player-readable failure reasons (`LONG`, `EXISTS`, `COUNT`, `OBSTRUCTED`) gives clear feedback at the spool. Nublar's fence spool should show the same kind of message (too long, already joined, no free node). Effort S.
- B15: CCA has no wire health, so it gives no precedent for B15's wire health. Keep Nublar's own design.

**Compatibility duty.**
- If Nublar offers FE, publish `IEnergyStorage` on NeoForge (block capability) and Team Reborn Energy on Fabric. The CCA 26.2 Fabric port uses Team Reborn Energy 5.0.0 `[v]` (`gradle.properties` at `chaevsfe/createaddition@c7aae67`), so a Nublar generator that exposes Team Reborn Energy on Fabric can feed a CCA connector with no extra code. Balm is the abstraction the plan already names (B14).
- Do not tick-push FE into neighbours every tick from many block entities. Expose a capability and let the neighbour pull. CCA's alternator pushes each tick, one cached capability per face; that is the ceiling Nublar should stay under.

**Platform overlap.** FE/energy transfer API: NeoForge capability and Team Reborn Energy already cover it. Do not copy CCA's `InternalEnergyStorage`. Saved data, capability cache and block-entity ticking are vanilla/NeoForge.

**Verdict.** Adapt (network shape, saved graph, connect-result messages). Compat (FE capability for CCA connectors, Fabric via Team Reborn Energy).

### 3.2 Create New Age (CNA): electricity, magnets, nuclear

**In game.** The player builds a generator coil on a rotating shaft, rings it with magnet blocks, and the coil makes electricity. Stronger magnets give more power and more stress load. The player runs wire between electrical connectors, stores power in batteries, drives motors from power, and "energises" items in an energiser. The advanced path is a nuclear reactor: fuel rods heat a multiblock, heat vents cool it, overheating explodes it, and radiation poisons nearby players. A Geiger counter ticks. Street lights and lamp posts use the power `[doc]` (`changelog.md`, 1.2.0). CC:Tweaked peripherals exist for motor, energiser and brushes `[v]` (`compat/computercraft/peripherals/`).

**Mechanism `[v]` (branch `1.20.1`, 9ff732f).**
- Generator coil (`content/electricity/generation/coil/GeneratorCoilBlockEntity.java:31-100`): a fixed ring of 12 positions two blocks out from the shaft (`magnetPositions`). Each magnet adds to the coil's stress; the energy per tick is `(stress - plainStress) * |speed| * suToEnergy`. Magnet strength comes from an `IMagneticBlock` interface or from a data tag `create_new_age:magnet/force_<n>` parsed with `Integer.parseInt` (`:62-80`). Stress is recomputed in `lazyTick` every 20 ticks and applied only when it changed (`:88-104`). This is data-driven and cheap.
- Wire network (`content/electricity/network/`): each `ElectricalNetwork` holds a node list, a consumer map and a pulled-source map, rebuilt when a node joins. `NetworkTicker` keeps a static `Map<Level, List<ElectricalNetwork>>` (`NetworkTicker.java:11-39`).
- Delivery uses a path search: for each consumer, `findConductiblePath` runs a breadth-first search with a `List.contains` visited check, limited by `maxPathfindingDepth` (`NetworkPathManager.java:12-40`). Each wire type has a conductivity (copper 1,024, overcharged iron 2,048, gold 4,096, diamond 8,192; `WireType.java:10-13`). A path's conductivity limits what it can carry, and the context decreases it as energy flows (`ElectricalNetwork.java:116-144`). This models wire-by-wire throughput.
- Insertion: `insert` splits the request evenly over consumers, then loops while any consumer accepts (`ElectricalNetwork.java:85-114`). It calls `be.setChanged()` and `chunkSource.blockChanged(pos)` after each successful insert (`:129-135`), which marks chunks dirty and sends block updates every tick of power flow. This is an expensive pattern `[inf]`.
- Pulled sources run inside a transaction (open, test-extract, abort, insert, extract, commit) from the author's own `esl` energy library (`ElectricalNetwork.java:150-163`, `build.gradle:67`).
- Reactor: a rod holds `heat` and `fuel`. Every tick: adds heat while fuel burns; every 40 ticks it averages heat with 6 neighbours and creates radiation (`ReactorRodBlockEntity.java:30-60`). Overheat above `16000 * overheatingMultiplier` triggers `explode(...)` through `HeatBlockEntity.handleOverheat` (`:33-39`, max heat 26,000). The rod block entity loads and saves `heat` and `fuel`. Reactor heat uses a shared `HeatBlockEntity` interface across heater, pipe, pump, Stirling engine and rods, so one heat model feeds all machines (`content/heat/`).

**Lesson for Nublar.**
- B14: the magnet ring is a good template for a "build quality sets output" mechanic that needs no code per block. Nublar's large generator could read a block tag for upgrade blocks around it. Effort S, lazy-tick (20 ticks) recompute. Low priority; the lead has settled generator fuel and tier as config values.
- B14: CNA shows what to avoid. The path search with `ArrayList.contains` is O(n²) on network size, and `setChanged` plus `blockChanged` per insert pushes chunk updates every tick `[inf]`. Nublar's lossless pylon lines (B14 decision) need none of this. CNA's per-wire conductivity would add a balance dial the lead did not ask for.
- B14/tone: CNA's reactor and radiation poisoning conflict with Nublar's tone (a dinosaur park) and scope. A generator that can explode is not in the design. Not relevant.
- B12: the shared `HeatBlockEntity` interface is a small example of one trait across many machines. Nublar's four machines share the B12 framework in the same way.

**Compatibility duty.** None needed unless Nublar offers an energy capability. CNA publishes its own storage through `esl` and Create's stress; its connectors read the block behind them through `EnergyStorage.findForBlock` (`ElectricalNetwork.java:61-76`). If Nublar machines expose FE, a CNA connector can feed them.

**Platform overlap.** `esl` overlaps NeoForge capabilities and Team Reborn Energy. Do not adopt.

**Verdict.** Avoid (path-search network, nuclear). Adapt only the data-tag magnet idea if Nublar wants generator upgrades. Not relevant otherwise.

### 3.3 Steam 'n' Rails (SnR): track types, signals, couplers, conductors

**In game.** The player lays Create train track. This add-on adds monorail, narrow-gauge and wide-gauge track, and "phantom" track that has no collision. It adds track switches (andesite switch: hand, arrow or redstone), buffer stops, semaphore signals, and track couplers that join or split trains at a point. Conductors are golem-like workers who drive the train: the player sets a conductor in the driver seat, and the train runs from a schedule or from redstone links (forward, back, left, right, jump, sneak). A conductor whistle and a remote lens are included. A shadow realm stores trains outside the world with no ticking `[doc]` (`changelog.md` 1.7.0).

**Mechanism `[v]` (branch `1.20/dev`, 7441960c).**
- Track types are blocks in `content/custom_tracks/` (`monorail/`, `narrow_gauge/`, `wide_gauge/`, `phantom/`, `generic_crossing/`, `casing/`). They extend Create's track block. Signals, track graph, edge points and trains belong to Create core. SnR adds to them.
- Semaphore: `SemaphoreBlockEntity` reads `SignalBlockEntity.getState()` through a `WeakReference` cache and drives the arm with a chaser (`SemaphoreBlockEntity.java:51-62`, `:89-135`). It adds no new signal logic. Distant signals override the shown state (`distant_signals/IOverridableSignal.java`).
- Coupler: `TrackCouplerBlockEntity` is a `SmartBlockEntity` with two `TrackTargetingBehaviour` edge points on the track graph. A lazy tick checks whether a carriage wheel sits on the point (`TrackCouplerBlockEntity.java:75-89`, `:240-282`, `:354-373`). `TrainUtils.splitTrain` and `combineTrains` do the work (`coupling/TrainUtils.java:66`, `:231`). Changelog: "Couplers validate placement less frequently, improving performance" and a config option "to disable train collision" `[doc]`.
- Conductor: `ConductorEntity extends AbstractGolem`, with a fake player profile and six redstone-link `FrequencyListener`s (forward, backward, left, right, jump, sneak; `ConductorEntity.java:55-110`). It "possesses" a fake player to use Create's controls. The note in the file says the item-handler capability is registered per loader in common events (`:46` comment).
- Shadow realm: a command-driven store that keeps trains out of the tick loop (`/snr shadow_realm banish|restore|kill`, changelog 1.7.0 `[doc]`).

**Lesson for Nublar (B22).**
- The Ford Explorer follows a player-laid track with stops and runs driverless tours. SnR + Create already solve a harder version of the same problem, but only with Create's train graph and carriages. The pieces that carry over without Create:
  - Stops as an "edge point" the vehicle reads (SnR coupler, Create station). Nublar's `TourStopBlock` is the same idea. `[inf]`
  - A per-vehicle seat that acts as the driver for scripted runs (SnR conductor). Nublar needs only a scripted driver, not a golem and fake player.
  - Semaphore-style visible state at the stop (arm up or down) is a cheap feedback block. A stop sign that shows "tour waiting" would use the same read-only pattern: one cached reference, state read in `tick`.
- The shadow realm is a model for B25: park-wide dormant mode. A tour vehicle outside any player's range could be parked as data (position on track, stop index, passengers) and rehydrated on approach. This is a design idea for after Beta `[inf]`.
- Track gauge as an add-on block family shows how to ship several variants through one base class with a state-generator. Nublar needs one track in Beta.

**Compatibility duty.** If a park builder runs Create trains on a park map, Nublar fences and tour tracks must not use the Create track block ids and should avoid the same tags. SnR's casing whitelist is a block tag (`railways:track_casing_whitelist`, 1.7.0 `[doc]`), so tags are the integration point. Nublar's own track block should not extend Create's track block unless Create is a hard dependency; it is not.

**Platform overlap.** Entity pathing along a spline is not in vanilla. Minecart rails give a cheap baseline: vanilla rails could carry a Nublar vehicle as an entity, but the design wants a free-form player-laid track `[inf]`. Do not adopt SnR's train graph.

**Verdict.** Not relevant as code (needs Create's graph). Adapt the stop/semaphore feedback idea. Compat only if the lead later wants Create trains in the park, via block tags.

### 3.4 Create Connected

**In game.** Extra gearboxes and clutches (overstress clutch, centrifugal clutch, freewheel, brake, shear pin), a **kinetic battery** that stores stress and releases it later, a **kinetic bridge** that passes stress capacity between two networks while keeping them separate, an item silo (vertical vault), a fluid vessel (horizontal tank), inventory access ports and bridges, a dashboard that shows data to a seated player, and many copycat blocks `[doc]` (wiki `New-Features.md`).

**Mechanism `[v]` (branch `main`, be43db7, version 1.3.3).**
- Kinetic battery stores charge in the **block item** with a data component: `CCDataComponents.KINETIC_BATTERY_CHARGE` is written in `collectImplicitComponents` and read in `applyImplicitComponents` (`KineticBatteryBlockEntity.java:232-238`). The player carries charge in an item and places it. Discharge RPM and minimum draw are server configs (`:82-83`, `:172`).
- Inventory access ports register an `IItemHandler` block capability and cache the wrapped handler with a dirty flag (`InventoryAccessPortBlockEntity.java:14-50`). The 1.3.3 changelog lists "Performance issues with inventory access ports / inventory bridges (#294)" as fixed `[doc]`. I did not find the diff in the shallow clone.
- Feature toggle: `config/FeatureToggle.java`, `CFeatures.java`, `CFeatureCategories.java` and a `FeatureEnabledCondition` (datagen recipes). Disabling a feature hides the item, removes the recipes, and leaves existing blocks alone. A config sync makes the server override clients `[doc]` (wiki `Configs.md`).
- Wildcard frequency item: two wildcards in one redstone-link frequency are off by default "due to the potential for lag and griefing" `[doc]`.

**Lesson for Nublar.**
- B14: **battery that keeps its charge when picked up.** A Nublar battery or a "charged power cell" can store its charge in a data component on the item, so the stored energy survives break-and-place. NeoForge 26.2 and Fabric both have data components in vanilla 1.20.5+, so use the platform. Effort S.
- B12/B25: the feature toggle pattern gives pack authors a per-feature on/off switch that keeps old blocks and syncs from the server. The lead decided on a TOML plus datapack config split; a feature toggle could be a TOML list of disabled feature ids plus a recipe condition. Effort M. Optional.
- B20: the wildcard note is a design warning: an "any item" filter slot on a Nublar feeder can cause lag and abuse. Keep feeder filters to a tag or a species diet.
- B12: access ports show how a small block expands a machine's reachable surface. Nublar's "tabs to switch between linked machines" covers the same need in the UI instead of with blocks.

**Compatibility duty.** Machines that expose item and fluid handlers work with ports, bridges and Create's chutes at no cost. Nublar machines must expose a normal `ItemHandler`/`FluidHandler` capability per side and not rely on GUI-only access.

**Platform overlap.** Data components, item capabilities and config are platform features. Fabric has Transfer API instead of NeoForge capabilities `[inf]`; Balm hides some of this.

**Verdict.** Adapt (item-stored charge). Compat (standard handlers). Adopt-as-idea for the feature toggle.

### 3.5 Create Deco

**In game.** Decorative blocks only: metal bars, mesh (chain-link) fences, catwalks with railings and stairs, shipping containers, sheet metal, windows, doors, supports and wedges, cage lamps, coin stacks, placards, decals, hulls, facades, ladders `[v]` (`api/*.java` list). Mesh fences are tagged as fences, a wrench toggles the top post.

**Mechanism `[v]` (branch `1.21-neo`, 996780b).** `MeshFences.build` registers a `MeshFenceBlock extends FenceBlock` with `BlockTags.FENCES` and pickaxe tags, sound `CHAIN`, a translucent layer, and a recipe of 16 fences from plates and string (`api/MeshFences.java`). `MeshFenceBlock` adds one boolean property `UP` and a wrench handler that flips it (`blocks/MeshFenceBlock.java`). Of the block classes listed, only the placard extends a Create block entity (`DyedPlacardBlock.java:5-16`). All other blocks have no block entity.

**Lesson for Nublar (B15).** The visible-only fence variant costs no block entity and no ticking. Nublar's "three voltage signs" (decorative blocks) and any non-electric park fence (decorative perimeter, visitor-side fence) should follow this pattern: blockstate only, vanilla fence tag, no ticks. This also means vanilla pathfinding and `FenceAwareNodeEvaluator` see them as fences with no extra logic. Effort S per variant.

**Compatibility duty.** Nublar's pathing treats `BlockTags.FENCES` blocks as barriers `[inf]` (check `FenceAwareNodeEvaluator`). Decorative fences from Deco will then block dinosaurs, which is probably wanted, and they will not shock. Decide whether a non-electric fence should hold a dinosaur.

**Platform overlap.** Fence blocks, tags, state properties: all vanilla.

**Verdict.** Adopt (pattern: no-BE decorative variants). Not relevant otherwise.

### 3.6 Create Diesel Generators (CDG)

**In game.** Diesel, gasoline, ethanol, biodiesel and plant-oil fuels burn in engines that produce stress. Pumpjacks drill oil from chunks. A distillation tower splits crude oil. A bulk fermenter makes ethanol and biodiesel. Canisters carry fuel. A chemical turret fires fluids `[doc]` (README, source tree).

**Mechanism `[v]` (branch `1.21.1`, bed7a1c).**
- Fuel types are datapack JSON: `data/createdieselgenerators/createdieselgenerators/fuel_type/*.json` with a fluid tag, `sound_pitch`, and per-engine `speed`, `strength`, `burn_rate` (`diesel.json`, `biodiesel.json`, ...). A KubeJS builder also exists (`compat/kubejs/FuelTypeBuilder.java`).
- Oil per chunk: `world/OilChunksSavedData extends SavedData` stores `Map<ChunkPos,Integer>` overrides; absent chunks compute a base amount from the world seed and biomes (`OilChunksSavedData.java:20-50`, `:66-70`). Pumping writes a new value; the thresholds make a chunk infinite above a config limit (`:60-63`). The save uses a boxed `HashMap<ChunkPos,Integer>`, which is fine at this scale.
- Optional integrations are isolated packages: `compat/computercraft/`, `compat/jei/`, `compat/kubejs/`, `compat/strut_your_stuff/`, `compat/EveryCompatCompat.java`, plus a `CDGMixinPlugin.java`.

**Lesson for Nublar.**
- B21/B02: the oil-chunk store is the same shape as fossil dig sites: a deterministic base value from seed and biome plus a `SavedData` map of what the player changed. A dig site that a paleontologist or player has worked would sit in the overrides map; untouched chunks cost nothing. Effort M. This also keeps dig sites out of chunk NBT.
- B01/B11: the fuel-type datapack JSON, with a fluid tag and per-tier numbers, matches the lead's rule "datapack JSON for per-species data, TOML for global balance". Generator fuel in Nublar (furnace rate, large generator slower) is a config value, so keep it TOML. No change.
- B22: Nublar's vehicles use no fuel (decision D07 is open on this; the brief says no fuel). CDG's fuel system has no use for the jeep. Not relevant.

**Compatibility duty.** None.

**Platform overlap.** Saved data, tags and datapack registries are platform features.

**Verdict.** Adapt (seed plus overrides SavedData for dig sites). Not relevant otherwise.

### 3.7 Create Enchantment Industry (CEI)

**In game.** Experience becomes a fluid. A hatch collects experience from killed mobs, a grindstone drain turns items into experience, a blaze enchanter spends experience and an item to enchant it, a blaze forger handles other transfers, and a printer copies books and other items using fluid ink. A lantern carries experience as a portable fluid container `[doc]`.

**Mechanism `[v]` (branch `1.21.1/6.0.0-dev`, 7cb2e9f).**
- **Printer** (`common/fluids/printer/`): `PrinterBlockEntity` is a belt processing block with a `SmartFluidTankBehaviour` and a countdown `processingTicks` (`PrinterBlockEntity.java:66-86`, `:194-197`). What it prints is chosen by a **registry of behaviours**: `PrintingBehaviour.Provider.create(level, tank, stack)` returns an optional result; the first provider (sorted by priority at registry bake) that claims the item wins; the default is a recipe-based one (`PrintingBehaviourRegistry.java`, whole file). The registry is a NeoForge custom registry that add-ons use (`sync(false)`, `onBake` sorts providers).
- Behaviours implement `getRequiredItemCount`, `getRequiredFluidAmount`, `getResult` and `onFinished` (`PrintingBehaviour.java`). Written book, enchanted book, banner pattern, name tag and package address are separate implementations.
- Experience as a fluid has a fluid type, a drop context, a hatch behaviour and a "fuel" entry (`common/fluids/experience/`). Mounted storage types register the lantern so contraptions can carry it (`CEIMountedStorageTypes.java`).
- Arm interaction points let Create's mechanical arm use the enchanter and forger (`BlazeEnchanterArmInteractionPoint.java`, `CEIArmInterationPoints.java`).
- `CEIDataMaps.java` registers NeoForge data maps for enchantment rules. `EnchantmentProcessingRules` are data-driven.

**Lesson for Nublar (B12, B06-B11 machines).**
- The printer pattern is the closest match to the egg printer: one machine, many "templates", each a small strategy object with item count, fluid amount, result, and finish callback. Nublar's egg printer and the incubator can use a **template provider list** instead of one big switch: an `EggTemplate` strategy per species family. Effort M, B12. Avoid a global custom registry unless other mods are expected to add templates; a datapack recipe type covers the common case and is simpler. `[inf]`
- B12: machine processing is a countdown stored in the block entity and reduced in `tick`. It is saved with the block entity and not synced each tick. Nublar's earlier survey already asks for this (S8/S9).
- B12: "experience as a fluid" is a pattern for "DNA as a fluid" (a sequencer output or a gene-editor feed). If the lead wants plumbing for DNA, a registered fluid with a tag lets Create pipes and tanks move it with no extra code. The lead has not asked for it; no change.
- Mechanical arm points exist for every CEI machine so Create automation works. This is the template for the optional compat: for each Nublar machine, expose slot handlers; add arm points only if needed.

**Compatibility duty.** Machines must expose `ItemHandler` and, if fluids exist, `FluidHandler` capabilities per face and never hide state in the GUI. Update the sided capability when a block changes face.

**Platform overlap.** NeoForge custom registry, data maps, fluid types and capabilities exist already. Fabric has Fluid Transfer API. Balm covers some. Do not copy CEI's registry boilerplate.

**Verdict.** Adapt (strategy list for printer templates). Compat (standard handlers).

### 3.8 Create Central Kitchen (CCK): cooking integration

**In game.** Create's belts, saws, deployers, mixers, fans, spouts and mechanical arms start to cook Farmer's Delight and 12 sibling "Delight" mods. A mechanical saw replaces the cutting board. A deployer applies the tool recipes. A spout fills and empties kegs. A packager unpacks recipe ingredients into a pot. A Blaze Burner wears a chef's hat `[doc]`.

**Mechanism `[v]` (branch `1.21.1/6.0.0-dev`, df9cc5c).**
- One integration per partner mod: an enum `ModIntegration` lists 13 partner mod ids and tests `ModList.get().isLoaded(id)` (`integration/ModIntegration.java`). Each partner has its own source set (`src/integration/<mod>/`) with its own mixin JSON (for example `create_central_kitchen.brewinandchewin.mixins.json`) and its own GameTest class.
- Recipes convert at load time. `CCKRecipesConfig` has one boolean per conversion (cutting board to sawing, to deploying; keg pouring to filling or emptying; mortar to compacting; juicer to compacting; melting pot to mixing), all default on (`config/CCKRecipesConfig.java`, 9 booleans, whole file).
- Arm access to a block that has no inventory: `StatefulBlockArmInteractionPoint` makes a block-state change into an atomic "plan". Insert and extract each call `planInsertion`/`planExtraction` on the live state; `simulate` returns the plan's result without touching the world; a real call applies the next state and plays the sound (`common/mechanicalarm/StatefulBlockArmInteractionPoint.java`).
- Packager unpacking reads recipes from a `RecipeManager`-keyed cache: `Cache<RecipeManager, Map<RecipeType, List<Candidate>>>` with weak keys, invalidated on reload, on tag update and on server stop (`common/packager/UnpackingRecipeCache.java`). Candidates with more than 9 ingredients or none are dropped at load.
- GameTests: `CCKGameTests` registers each integration's test class only if the partner mod is loaded, directly with `GameTestRegistry` so the partner's structure templates can be reused without enabling all of the partner's tests (`common/gametest/CCKGameTests.java`).

**Lesson for Nublar (feeders, B20; food, B12; tests, P2).**
- B20: a Nublar feeder holds food, and the plan already wants it fillable from outside the paddock. The arm-point idea transfers: if the feeder exposes an `ItemHandler`, a Create chute, funnel or arm fills it with no code. CCK needed arm points only for blocks that change state instead of holding items (a cake, a skillet). A feeder is an inventory, so skip arm points `[inf]`.
- B20: the plan/simulate/apply split in `StatefulBlockArmInteractionPoint` is a clean way to write any "player or machine inserts, state changes" action with a dry-run: use it for the live-bait feeder (put an animal in, release it later) so a hopper, a dispenser and a player share one code path. Effort S (a 20-line method triple).
- B12: the recipe cache keyed weakly by `RecipeManager` and cleared on reload and tag update is the right shape for any Nublar lookup built from recipes or tags (diet tags, fossil sets). The earlier survey asks for a feeder registry; this is the invalidation recipe for it. Effort S. Check NeoForge 26.2 event names `[inf]`.
- P2/tests: the `ModIntegration` enum plus a per-integration GameTest class loaded only when the mod is present is the right way to keep optional compat testable (acceptance already asks for GameTests on both loaders). Effort S.
- Conversion at load (CCK) is not needed for Nublar: Nublar's own machines have their own recipe types.

**Compatibility duty.** Nublar food items should carry standard food components and item tags (`c:foods`, `c:crops`) so CCK-style tools see them. Dinosaur diet tags (datapack) should name vanilla and common-tag items, not private ids, for the same reason.

**Platform overlap.** Mod-loaded checks, GameTest registration and recipe reload events exist in NeoForge. Fabric has `FabricLoader.isModLoaded` and its own GameTest API `[inf]`. The `common/` module needs a small platform shim.

**Verdict.** Adapt (plan/simulate/apply; weak-key reload cache; integration enum plus tests). Compat (standard item handlers on feeders).

### 3.9 Bells & Whistles

**In game.** Decoration for Create trains: train pilots (front plates) in several metals, headlights, metal steps and bogie steps, grab rails, panels, platforms `[doc]` (README feature list).

**Mechanism `[v]` (branch `1.20.1-forge`, e06fb2f).** The source has 7 block classes (`HeadlightBlock`, `MetalBogieStepsBlock`, `MetalGrabRailsBlock`, `MetalStepBlock`, `PanelBlock`, `PilotBlock`, `PlatformBlock`) plus registry and tab classes. No block entities, no entities, no renderers. All behaviour is block state. Modrinth lists Fabric, Forge, NeoForge, Quilt up to 1.21.1, last update 2025-03-13 `[doc]`. A third-party Fabric 26.2 port exists (section 2).

**Lesson for Nublar (B22/B24).** Vehicle skins and decoration on tour cars (stripes, lights, roof racks) should be blockstate-only or model-only, never a ticking block entity. The Explorer is an entity, so decoration belongs in its GeckoLib model, with textures swapped by variant. Nothing to copy.

**Compatibility duty.** None.

**Platform overlap.** All vanilla blocks.

**Verdict.** Not relevant.

### 3.10 Create Jetpack

**In game.** The player upgrades a copper backtank with brass into a jetpack. It burns the backtank's air to fly. A HUD hint shows controls `[doc]` (README).

**Mechanism `[v]` (branch `main/neoforge/1.21.x`, 0b1228f).** Kotlin source: `JetpackItem.kt`, `JetpackBlock.kt`, `ServerConfig.kt`, `SyncConfigMessage.kt`, `ControlsDisplay.kt`, `CopyComponentsMechanicalCraftingRecipe.kt`. The recipe class copies data components from the backtank item onto the jetpack, so air carries over. It needs Kotlin for Forge or Fabric Language Kotlin.

**Lesson for Nublar.** A `CopyComponents` recipe (output keeps the input's data) is a short pattern for upgrading a charged item without losing its charge. It applies to a battery upgrade. Effort S. Nublar has no flight or jetpack in the design (no mounts); nothing else applies.

**Compatibility duty.** None. A Kotlin dependency in a Java mod pack is a reason to avoid shipping with it.

**Platform overlap.** Data components and recipe types are vanilla in 1.20.5+.

**Verdict.** Not relevant.

### 3.11 Create Big Cannons (CBC): projectiles and munitions

B23 (guns) is blocked. This section is a note only; it makes no recommendation to start.

**In game.** The player builds multiblock cannons from cannon blocks, loads propellant, a projectile and a fuze, and fires with a kinetic mechanism. Shells can be solid shot, AP, HE, shrapnel, grapeshot, smoke, fluid shells and mortar rounds. Autocannons fire faster. Fuzes (impact, timed, proximity) set when a shell detonates `[doc]` (README).

**Mechanism `[v]` (branch `create-v6-1.21.1`, 46a5d2a, version 5.11.7).**
- A projectile is an entity (`AbstractBigCannonProjectile`). Its `tick` first checks that the chunk exists before it moves (`level().hasChunk(cx, cz)`, line 62-66). In flight it spawns 10 trail smoke particles per tick when the trail setting is not `NONE` (`:67-77`), and on the client plays a fly-by sound once per cooldown (`:78-97`). The tracer item is synced data (`:55-59`).
- Impact goes through a `ProjectileContext` that holds the hit-entity set, queued explosions (a `Map<BlockPos,Float>`), effect packets and a "grief state" (`munitions/ProjectileContext.java:17-50`). Block damage is gated by a server config `GriefState`.
- Properties are datapack-driven: `munitions/config/` has handlers for block properties, entity properties, item properties, fluid drag, dimension properties and propellant compatibilities (`BlockPropertiesTypeHandler`, `EntityPropertiesTypeHandler`, `FluidDragHandler`, `DimensionMunitionPropertiesHandler`, `BigCannonPropellantCompatibilityHandler`). Block armour is a separate data-driven table (`block_armor_properties/`).
- 4.x to 5.11 changelog lines: 5.11.4 "changes data storage for big cannon munition blocks ... Existing blocks will be broken" `[doc]`. That is a warning about changing block entity data layout after release.

**Lesson for a future gun (B23, note only).**
- Tranquilizer darts and a capture net behave like a small projectile with an impact effect and a data-driven "what happens to this entity" table. CBC's split (entity per projectile, context object per impact, datapack tables for targets) is a reference for scale, but CBC is built for heavy projectiles; a dart needs only vanilla `AbstractArrow`. `[inf]`
- Cheap guards worth remembering: skip tick when the chunk is not loaded; cap trail particles per tick; keep a server grief switch.
- Do not copy the munition block-entity data layout; CBC itself had to break it once.

**Compatibility duty.** If Nublar later adds guns, projectiles that hit dinosaur hitboxes (B27) go through `LivingEntity` hit checks; CBC projectiles do the same against the entity's boxes. Dinosaur part hitboxes must therefore also answer to ray and swept-box tests `[inf]`.

**Platform overlap.** Vanilla projectile entities, damage types and `Level.explode` cover darts and a stun effect.

**Verdict.** Not relevant now (B23 blocked). Note only.

### 3.12 Simulated Project (Simulated, Aeronautics, Offroad) and Sable

**In game.** The player assembles a block structure into a **physics body** ("sub-level") that moves, tilts and floats in the world: airships with propellers and hot-air burners (Aeronautics), vehicles with wheels (Offroad), and the core tools (Simulated): steering wheel, throttle lever, gimbal and altitude sensors, rope winches, torsion springs, honey glue, docking connectors, physics assembler, nameplate `[doc]` (README). The Sable physics library runs under it. Its README states the library is "an incredibly intrusive mod" that "makes extensive use of mixins" and is "prone to extensive compatibility issues" `[doc]`.

**Mechanism `[v]` (Simulated-Project `main`, 8d482a5; Sable not cloned).**
- Three modules (`simulated`, `aeronautics`, `offroad`), each a `common` plus `neoforge` pair, all NeoForge only (`settings.gradle.kts`). The mod range for Sable is `[2.0.0,3.0.0)` (`gradle.properties`).
- Ropes: server side `ServerLevelRopeManager` and `ServerRopeStrand` use Sable physics constraints (`ServerRopeStrand.java:6-18`, `:247-263`). Client side `ClientRopeStrand` holds points; `RopeStrandRenderer` draws each segment by scaling a cached partial model (`CachedBuffers.partialFacing`) and takes light with `LevelRenderer.getLightColor` per segment (`RopeStrandRenderer.java`, lines 20-90).
- **Network on demand.** `ServerRopeTrackingSystem` sends an update packet only while a strand `needsSync()`. When it settles it sends one "stop" packet and sets `networkingStopped = true`; it then skips the strand until it moves again (`ServerRopeTrackingSystem.java`, `sendTrackingData`). Players tracked are those near the holder block (`neededPlayers`).
- How a block entity must adapt: CCA, a separate mod, carries a `SABLE_ACTIVE` branch in its renderer and its connect rule. It transforms positions into the sub-level frame, measures wire length in local node offsets, and checks the break distance every 10 ticks (`WireNodeRenderer.java:88-91`, `:169-229`; `IWireNode.java:576-584`; `AbstractConnectorBlockEntity.java:273-279`).

**Lesson for Nublar.**
- B22/B25: the "send while moving, one stop packet, then silence" rule is the right model for any moving thing synced by hand: the tour vehicle, a moving gate, an animated feeder arm. The earlier survey's S10 asks for fewer synced fields; this adds a rule for the timing. Effort S.
- B22: a Sable-style physics body is not what Nublar's jeep needs. The design is a driven vehicle with simple movement and a track-following tour car. Physics bodies would be a heavy, intrusive dependency. Avoid as a dependency.
- P2: if a player runs Nublar blocks on a Sable sub-level, any block entity that stores or compares absolute world coordinates (fence connections store `BlockPos` of the other post) will misbehave, as CCA's Sable branches show. Store connection targets as offsets relative to the post, or store absolute positions and expect a mismatch. Do not add Sable support in Beta; do not block it either. Effort S to store relative offsets, and it also helps structure placement.

**Compatibility duty.** Keep block entity logic free of `level.getBlockEntity` calls at far absolute positions in `tick`, and keep render code reading per-end positions rather than assuming a shared origin. Sable ships `sable-companion` for light optional compat (version 1.6.0 in `gradle.properties`); Nublar needs none in Beta.

**Platform overlap.** None: vanilla has no physics bodies.

**Verdict.** Avoid (dependency). Adopt the "send while moving" rule. Note the relative-offset storage for future compat.

## 4. Wire renderer cost: Crafts & Additions in detail

**In game.** A park builder strings many wires. Each wire shows as a thin sagging ribbon. With a few hundred wires in view the frame rate falls if every wire costs a few hundred vertices per frame.

**What CCA does each frame `[v]` (`rendering/WireNodeRenderer.java`, `rendering/CARenderType.java`, commit 1c81fab).**

| Item | CCA behaviour | Cost note |
| --- | --- | --- |
| Draw path | A `BlockEntityRenderer` per connector. `render` loops its nodes and draws each wire with `MultiBufferSource.getBuffer(CARenderType.WIRE)` (`:80-128`, `:261-277`) | Immediate mode, no instancing, no chunk mesh |
| Geometry per wire | `wirePart` runs 24 steps (`:282-290`), each step calls `wireVert` twice, and `wireRender` calls `wirePart` twice (`:275-276`). Count: 2 parts x 24 steps x 4 vertices = 192 vertices per wire, 48 quads `[v]` (arithmetic from the loop bounds) | About 192 vertices and four 32-bit-colour attributes per vertex. For 900 wires fully visible: about 173,000 vertices per frame `[inf]` (estimate) |
| Drawn twice | In the non-Sable path each connector draws all of its connections (`:93-128`). Both ends of a wire hold the connection, so each wire is drawn by both. The one-end rule `shouldRenderConnection` exists only in the Sable path (`:231-240`) | About twice the needed vertices. CNA solves this with a position-order test in `shouldRenderConnection` (`ElectricalConnectorRenderer.java:46-57`) |
| Light | 4 `getBrightness` calls per wire per frame (2 block, 2 sky, `:271-274`), interpolated across the 24 steps | Small, but per frame and per wire. 26.2 fork reads light once during state extraction (see below) |
| Culling | `getRenderBoundingBox` is the box of all node positions plus 1 block of margin (`:45-70`). `shouldRender` measures distance to that box (`:72-77`). `shouldRenderOffScreen` returns true (`:40-43`) | Correct culling for long wires. The changelog line "Improved Connector wire rendering and culling" (1.7.2) points to this work `[doc]` |
| Render type | `RenderType.create("wire", POSITION_COLOR_LIGHTMAP, QUADS, 256, false, true, ...)` with the vanilla leash shader, no texture, no cull (`CARenderType.java:18`). The sixth argument `sortOnUpload` is `true` | In 1.21.1 a true value sorts quads on upload `[inf]`. The leash type in vanilla sets it false. For opaque wire, sorting is wasted work `[inf]` |
| Allocation | Few: `Vec3` returns from `getNodeOffset`, `PoseStack.pushPose` | Fewer than Nublar's today (C5: list and lambda per frame) |

**CNA's renderer, for comparison `[v]` (`ElectricalConnectorRenderer.java`, 9ff732f).** It builds a `new Wire(direction, distance, sections)` per wire per frame (`:78`), looks up both block entities with `level.getBlockEntity` per wire per frame (`:66-69`), reads two light levels per section (`:90-95`), and uses a textured wire render type per wire type. The section count is `ceil(distance * wireSectionsPerMeter)` from a client config (`:75`). That is more work per wire than CCA, but it has a client setting, which is useful.

**The 26.2 version of CCA's renderer `[v]` (`chaevsfe/createaddition@c7aae67`, `src/client/.../WireNodeRenderer.java`).**
- Rendering splits in two as Minecraft 26 requires: `createRenderState`, `extractRenderState(be, state, ...)` and `submit(state, poseStack, SubmitNodeCollector, camera)` (`:42-125`).
- `extractRenderState` builds a `List<WireRenderState>` per connector every frame (`:73-109`). Each `WireRenderState` stores floats and the four light values, read once.
- `submit` calls `queue.submitCustomGeometry(matrices, layer, wire)`. The wire's `render(Pose, VertexConsumer)` runs later and emits the strip (`:113-125`, `:253-330`).
- `getViewDistance` returns 256 (`:129-139`).
- Result: the P2 port needs this two-step shape for every Nublar block entity renderer. The `ArrayList` per extract is an allocation per connector per frame; a reusable list in the render state would avoid it `[inf]`.

**Known lag fixes in the CCA history `[v]`/`[doc]`.**
- 2022-05: "optimised block models ... not render unseen sides" (commit 30d29d9, `git log`).
- 2025-04: ported to the 1.21 `VertexConsumer` API (commit d07f923); "gave render type back" (5b3c414).
- 2026-03: "Make connector cache more robust" (d239617, PR #1122).
- 1.7.2: rewrote connectors to run over unloaded chunks, improved wire culling `[doc]`.
- I found no frame-rate measurements in CCA's repo or changelog.

**What this means for Nublar's fence renderer (B15, B25, P2).**
1. Cull by the wire boxes, not the post block: override `getRenderBoundingBox` and `shouldRender` as CCA does. Nublar's posts connect to nearby posts only, so a box of the post plus its neighbours is small. EntityCulling uses a block entity's render box for its occlusion test `[inf]`; a tight box helps it.
2. Draw each wire once. Use a total order on the two end positions (CNA's rule) or draw only for the lower-index end. This halves the vertices at no cost. Effort S.
3. Reduce the number of segments with distance: 24 steps per strip is too many for a 3-block gap between fence posts. Fence wires are short and nearly straight; 4 to 6 steps per strip suffice. A short wire's sag needs few points. Effort S `[inf]`.
4. Cache the vertex data or a baked mesh per connection and rebuild on a connection or power change. This is the earlier survey's C5 remedy. On 26.2 the cache sits in the render state; keep the state object per block entity and reuse it.
5. Set `sortOnUpload` false for the wire render type (or use the vanilla `leash`/`lines` type that already does).
6. Do not light each segment with its own `getBrightness`; read the two ends once at extract time and interpolate, as the 26.2 CCA fork does.
7. For very large parks, a client setting for wire detail and a max wire distance (CNA has the first; CCA has 256 view distance in the fork) gives low-end users a switch. Effort S.

## 5. Comparison with Nublar's electric fence design

| Question | Nublar plan (B14/B15, lead decisions) | Crafts & Additions | Create New Age |
| --- | --- | --- | --- |
| What is the wire | A line between two fence posts of one pole column. Block entity per post | A line between two connector block entities, anywhere within 16 or 32 blocks | Same as CCA |
| Wire health | Each wire has health; an animal hit removes its attack damage; spool repairs | None. A wire is whole or gone | None |
| Connection rules | Posts connect like fence posts; pylons reach `power.pylon_range` | Length limit, one wire per pair, node count 4 or 6, wire type vs connector size (`IWireNode.java:564-594`) | Max length by wire type; depth limit for path search |
| Power model | One store per pole column; complete runs share energy (`FencePowerService`); lossless pylon lines | One `EnergyNetwork` per connected set, two-buffer swap, 80,000 FE cap; no loss | Network with per-wire conductivity and path search |
| Persistence across chunks | Not stated | `SavedData` graph, async chunk-read verifier | Block entities only |
| Player feedback | Sparks, broken halves | Goggle tooltip with FE per tick, connect-result text | Goggle tooltip |
| Hurt on contact | Live wire hurts players and animals | Barbed wire only; the wire itself is harmless | Not applicable |
| Renderer | `RenderType.leash()` ribbon per wire, per block entity | Custom ribbon, 192 vertices per wire, drawn from both ends | Textured ribbon, per-section light |

Reading: CCA is the closest model for the power network and the persistence. Nublar's wire health, shock and animal logic have no precedent in these mods. The earlier survey says the same for the whole survey set.

## 6. Closing table

| Technique | Source | Nublar module | Verdict | Effort | Expected effect |
| --- | --- | --- | --- | --- | --- |
| One network object per connected run, rebuilt only when dirty, ticked once | CCA `WireGraph.java:296-335,418-433`, `EnergyNetwork.java:38-49` | B14, B15 | Adopt (pattern) | M | Posts stop ticking energy; supports S7 remedy |
| Run graph in `SavedData` with `long` position keys, async chunk verify (16 in flight) | CCA `WireGraph.java:44-67`, `WireGraphVerifier.java:12-40` | B14, B15 | Adapt, only if parks span unloaded chunks | L | Fence runs keep power state across unloaded chunks |
| Reference-RPM linear bridge (RPM/256 x FE and SU) | CCA `CommonConfig.java:108-112`, `AlternatorBlockEntity.java:134-136` | B14 compat | Compat (optional) | S | Clear balance if Create power ever feeds a machine |
| FE capability on machines and generator (NeoForge) / Team Reborn Energy (Fabric) | CCA `AbstractConnectorBlockEntity.java:393-400`; Fabric port `gradle.properties` | B14, P2 | Compat | S | CCA connectors feed Nublar machines |
| Connect-result enum with player-readable failure | CCA `WireConnectResult.java` | B15 | Adopt | S | Clear spool feedback |
| Wire drawn once (order the two ends) | CNA `ElectricalConnectorRenderer.java:46-57`; CCA gap `:93-128` | B15, B25 | Adopt | S | Halves wire vertices |
| Few segments per short wire; one light read per end | CCA `WireNodeRenderer.java:279-290`; 26.2 fork extract | B15, B25 | Adopt | S | Large cut in vertices per wire (estimate 4x fewer) |
| Render box and distance test from the wire span | CCA `WireNodeRenderer.java:40-77` | B15, B25 | Adopt | S | Correct culling, helps EntityCulling |
| `sortOnUpload` false on opaque wire type | CCA `CARenderType.java:18` | B15 | Adopt | S | Removes a per-frame sort `[inf]` |
| Extract-then-submit renderer with reused state object | CCA 26.2 fork `WireNodeRenderer.java` | P2, B15, B12 | Adopt | M | Required by 26.2; avoids per-frame list |
| Charge stored in item data component | Create Connected `KineticBatteryBlockEntity.java:232-238` | B14 | Adapt | S | Battery keeps charge when picked up |
| `CopyComponents` upgrade recipe | CreateJetpack `CopyComponentsMechanicalCraftingRecipe.kt` | B14, B13 | Adapt | S | Upgrades keep data |
| Feature toggle with recipe condition and server sync | Create Connected `config/FeatureToggle.java` | B12, B25 | Adapt, optional | M | Pack authors turn features off |
| Strategy list for printer templates | CEI `PrintingBehaviourRegistry.java` | B12 | Adapt (datapack recipe type first) | M | Egg printer templates without a switch |
| Plan, simulate, apply for state-changing inserts | CCK `StatefulBlockArmInteractionPoint.java` | B20 | Adapt | S | One code path for hopper, dispenser, player |
| Weak-key recipe cache cleared on reload, tag update, stop | CCK `UnpackingRecipeCache.java` | B12, B20 | Adopt | S | Safe cache for diet and fossil lookups |
| `ModIntegration` enum plus per-mod GameTest class | CCK `ModIntegration.java`, `CCKGameTests.java` | P2 | Adopt | S | Optional compat stays testable |
| Seed plus `SavedData` overrides per chunk | CDG `OilChunksSavedData.java` | B21 | Adapt | M | Dig sites cost nothing until worked |
| No-block-entity decorative variants (fence tag) | CreateDeco `MeshFenceBlock.java` | B15 | Adopt | S | Zero tick cost for decor fences and signs |
| Send while moving, one stop packet, then silence | Simulated `ServerRopeTrackingSystem.java` | B22, B25 | Adopt | S | Fewer packets for moving vehicles |
| Stop and semaphore feedback block reading cached state | SnR `SemaphoreBlockEntity.java` | B22 | Adapt | S | Visible tour-stop state |
| Hold dormant vehicles as data (shadow-realm idea) | SnR `changelog.md` 1.7.0 | B22, B25 | Adapt (post-Beta) | L | Idle tours cost no ticks |
| Path-search network with per-wire conductivity | CNA `NetworkPathManager.java`, `ElectricalNetwork.java` | B14 | Avoid | n/a | O(n^2) search; unwanted dial |
| `setChanged` plus `blockChanged` on every insert | CNA `ElectricalNetwork.java:129-135` | B14 | Avoid | n/a | Avoid per-tick chunk dirtying |
| Physics sub-levels (Sable) | Simulated, Sable | B22 | Avoid | n/a | Heavy, intrusive dependency |
| Munition data tables and projectile context | CBC `munitions/config/`, `ProjectileContext.java` | B23 | Not relevant now (B23 blocked) | n/a | Note only |

## 7. Platform overlap summary

| Need | Covered by | Notes |
| --- | --- | --- |
| Energy API (insert, extract, query) | NeoForge capability `Capabilities.EnergyStorage.BLOCK`; Fabric: Team Reborn Energy 5.0.0 on 26.2 (used by the CCA 26.2 port) | Balm bridges both per B14. Do not write a new API |
| Item and fluid handlers | NeoForge capabilities; Fabric Transfer API `[inf]` | Create belts, funnels, chutes, arms and packagers use these |
| Cached neighbour lookup | NeoForge `BlockCapabilityCache`; Fabric `BlockApiCache` `[inf]` | CCA uses the NeoForge one |
| Item-stored data | Vanilla data components | Battery charge, upgrade recipes |
| Saved world data | Vanilla `SavedData` | Wire graph, oil chunks |
| Mod-loaded checks and optional compat | Loader APIs | Keep out of `common/` |
| Block entity rendering on 26.2 | Vanilla `extractRenderState` and `submit` | Observed in the CCA 26.2 port |
| Create | Fabric only on 26.2 (Create Fly) | NeoForge 26.x: none found |

## 8. Open questions

1. **Compat scope on 26.2.** Create exists on 26.2 only as Create Fly (Fabric). Does the lead want any Create link in Beta, or only after P2, and Fabric only? The cheapest useful link is an FE capability on the generator and machines, which also serves Mekanism-style cables and needs no Create code.
2. **Fence run persistence.** Should a fence run keep power state when part of it sits in unloaded chunks? A big park can span many chunks while the player is at one end. CCA's saved graph solves it at the cost of one `SavedData` class and a verifier. The plan does not say.
3. **Non-electric fences.** Should a decorative fence block (Deco-style, no block entity) hold dinosaurs? It decides whether to tag it as a fence for pathing, and whether a "visitor side" perimeter without power is allowed.
4. **Wire detail setting.** Is a client setting for wire detail and maximum wire distance acceptable in the config split (TOML)? It is a low-cost safety valve for a few hundred wires.
5. **Spool feedback text.** Which failure reasons should the spool name (too long, already joined, no free node, blocked)? CCA names six.
