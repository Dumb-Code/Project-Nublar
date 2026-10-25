# Machine, power and storage mods: what Project Nublar can learn

This note covers seven repositories: Mekanism, Immersive Engineering (IE), Immersive Thunder, Powah, Refined Storage 2 (RS2), Common Storage Lib (CSL) and Immersive Machinery (IM). It extends `reports/Reference mod survey for Beta.md` and does not repeat it. That report already gives the Nublar hot paths S6 to S9 and C5 to C6, and the plan items B12.1 to B12.7, B14.1 to B14.5 and B15.1 to B15.6. Labels follow the brief: `[v]` verified in source (path and lines at the commit), `[doc]` from README, manual or docs, `[inf]` my inference. Every performance number that is not `[v]` is an estimate.

## Top findings

1. **Power: build one in-memory network object per connected group, merged and split on change, with no per-tick graph walk.** Mekanism, RS2 and IE all do this. Mekanism keeps the energy buffer on the network, not on the cables, and its cables run no per-tick work (`TileEntityMekanism` and `TileEntityTransmitter.tickServer`, see A and B1). Plan B14.3 already has the "one powered flag per section" idea. This note adds the data model that feeds it. Target: B14, B15.
2. **Do not persist the network graph. Keep the state on the block entities and rebuild the graph in memory.** RS2 wrote this down as a decision after Refined Storage 1 broke on moved blocks (`docs/architecture/decision/006-no-persistent-networks.md`) `[v]`. IE does the opposite: it stores every wire in level `SavedData` and ships a sanitizer and a validator to repair it (`GlobalWireNetwork.java:60`, `:463-537`; `IEServerConfig.java:50-51`) `[v]`. The rft fence data already lives on the posts. Keep it there. Target: B15.
3. **Wires can bake into chunk geometry on NeoForge, not on Fabric.** IE draws all wires through `AddSectionGeometryEvent` (`ConnectionRenderer.java:75-82`) `[v]`. Sodium on NeoForge supports that event. Sodium on Fabric returns an empty list for it (`FabricLevelRenderHooks.java:24-27`) `[v]`, and Fabric API has no equivalent `[v]`. Nublar needs one wire data model and two renderers. This is a real cross-loader gap (open question 1). Target: B15, C5, P2.
4. **IE wire shock is computed from the energy the grid can deliver, and it drains the grid.** `WireDamageHandler.java:50-95` `[v]`. A wire on an empty grid does no damage, and a shock costs generator energy. This gives outage gameplay for free. It needs a lead decision (open question 3). Target: B15.
5. **Sync only what changed, only to who looks, and debounce the rest.** Three mods show three tools: dirty-diffed menu data (Mekanism `MekanismContainer.java:571-590`, IE `GenericContainerData.java:67-76`), a 3-second deactivation delay on the `active` state (Mekanism `GeneralConfig.java:126-127`, `TileEntityMekanism.java:624-632`), and a coalescing `sync(delay)` (Powah `PowahBaseTickingBlockEntity.java:67-73`). All `[v]`. Together they replace Nublar's per-tick full-NBT packets (S8). Target: B12, B14, B25.
6. **Mekanism's upgrade formula gives Nublar a clean chip-tier model.** Speed upgrade n of max m: ticks = base x M^(-n/m); energy per tick = base x M^(2n/m) (`UpgradeUtils.java:85-87`, `:109-114`) `[v]`. With M = 10 (config default) and m = 8, a machine runs 10 times faster for 100 times the draw per tick, so 10 times the energy per product. Target: B12, B14.
7. **Show machine problems as a set of named issues, not one blocked bar.** Mekanism tracks `RecipeError` values, syncs them as booleans and shows a warning tab (`CachedRecipe.java:588-606`, `TileEntityFactory.java:709-767`, `GuiWarningTab.java`) `[v]`. Nublar's incubator already has four stall causes (power, bulb, bedding, eggs). Target: B12.
8. **Multi-block machines: the master ticks, parts do not.** IE gives dummy parts no ticker (`MultiblockPartBlock.java:96-107`) and splits "save" from "sync" data (`IMultiblockState.java`). Mekanism ticks one `MultiblockData` per structure (`TileEntityMultiblock.java:107-155`). Nublar can go one step further: a block-state part index and a master lookup, with no block entity on the parts at all (platform capability registration works without a block entity, `RegisterCapabilitiesEvent.registerBlock` and `registerForBlocks`) `[v]` `[inf]`. Target: B12.
9. **Polling devices should speed up on change and slow down when idle.** RS2's external storage runs at 40, 30, 20, 10 or 5 ticks per poll and moves up or down one step depending on whether the last poll found a change (`ExternalStorageWorkRate.java`, `AbstractExternalStorageBlockEntity.java:115-126`) `[v]`. Fits feeders that watch a hopper or a chest (B20). Target: B20, B25.
10. **Refined Storage needs no Nublar API.** It reads any block that exposes the platform item capability. DNA tubes and fossils are item stacks with components, so they are distinct resources already. The work for Nublar is to expose item handlers on machines and feeders, which the plan already asks for. Verdict: Compat, no code. Target: B05, B20.
11. **Availability limits compat.** Mekanism, IE and Powah ship on NeoForge only (no Fabric module in their builds) `[v]`. RS2 ships both. CSL has no 26.x branch. So energy compat with those mods on Fabric is not available, and on NeoForge it is the platform capability, not a mod API.
12. **Power outside loaded chunks is a real design gap.** If the generator chunk is not loaded, the generator does not burn fuel and the loaded fences go dark. Mekanism and IE both ship chunk-loading answers. Nublar has none yet (open question 2). Target: B14.

## Repositories read

| Repo | Branch @ commit (date) | Licence (repo fact) | Loaders and MC | Size | Maintenance |
| --- | --- | --- | --- | --- | --- |
| mekanism/Mekanism | `26.3` @ `feb1959` (2026-10-09) | MIT | NeoForge only. MC 26.3. Mod 10.8.0 (`gradle.properties`). Earlier targets named there: 1.21.1 and 26.1.2 | GitHub size 160 MB. 1,727 Java files in `src/main` | Active. Pushed 2026-10-09 |
| BluSunrize/ImmersiveEngineering | `1.21.1` @ `75a27f0` (2025-08-18) | GitHub reports NOASSERTION. File headers name a custom "Blu's License of Common Sense" | NeoForge 21.1.164. MC 1.21.1 | 112 MB. 947 Java files in `src/main`, plus `src/api` | Last push 2025-08-18. The clone has only the 1.21.1 branch. No 26.x branch seen |
| netcatgirl/ImmersiveThunder | `1.21.6` @ `62fbc81` (2025-06-30) | MIT | Fabric, Forge, NeoForge. MC 1.21.6 | 6.5 MB. 12 Java files | Last push 2025-06-30 |
| Technici4n/Powah | `26.1` @ `4274274` (2026-06-09) | LGPL-3.0 | NeoForge only. MC 26.1.2. Version 26.1.0-alpha | 6 MB. 289 Java files | Active. README says NeoForge since 1.20.2, Fabric and Forge only to 1.20.1 |
| refinedmods/refinedstorage2 | `develop` @ `cb95e08` (2026-10-04) | MIT | NeoForge (needs 26.1.2.74 or newer, `neoforge.mods.toml:20`) and Fabric. Release 3.2.1 on 2026-06-07 | 38 MB. About 1,640 Java files in 14 modules | Active |
| terrarium-earth/Common-Storage-Lib | `1.21.x` @ `69dd639` (2026-07-12) | MIT | Fabric and NeoForge. MC 1.21.1. Version 0.0.10 | 1.4 MB. 187 Java files | Branches stop at 1.21.x. The releases API returned none. No 26.x branch. Its README is a one-line changelog fragment |
| Luke100000/ImmersiveMachinery | `1.20.1` @ `7ed648f` (2026-09-09) | GPL-3.0 | Fabric and Forge. MC 1.20.1. Needs Immersive Aircraft (`MachineEntity extends EngineVehicle`, `MachineEntity.java:3,11`) | 1.6 MB. 44 Java files | Pushed 2026-10-10 |

Platform sources read for the "Platform overlap" lines (clones made by other researchers, read only): NeoForge `26.2.x` @ `3ce4390` (2026-10-10), Balm `26.3` @ `cd5d2c3` (2026-10-02), Fabric API @ `6a8ed6b` (2026-09-18), Sodium `dev` @ `8aa723c` (2026-09-30), Embeddium `21.4/neoforge` @ `aa6657d` (2025-02-01).

A version caution: Mekanism targets 26.3 and Powah targets 26.1.2. Nublar targets 26.2. The patterns carry over. Class names in the quoted files may differ on 26.2.

## Platform baseline: what 26.2 already gives Nublar

The brief says: if the platform covers it, use the platform. This table lists what the surveyed mods use, and where the platform already has it.

| Need | NeoForge 26.2 | Fabric 26.2 | Balm 26.3 | Use in Nublar |
| --- | --- | --- | --- | --- |
| Energy API | `net.neoforged.neoforge.transfer.energy.EnergyHandler`: long amounts, `insert` and `extract` with `TransactionContext` (`EnergyHandler.java`) `[v]`. Block capability `Capabilities.Energy.BLOCK`, sided (`Capabilities.java:22`) `[v]` | No energy module in Fabric API `[v]` (searched the clone). Team Reborn Energy is the convention | Own `EnergyStorage` with `int fill/drain(int, boolean simulate)` and `canFill/canDrain` (`EnergyStorage.java`). On Fabric it registers a Team Reborn fallback lookup (`RebornEnergy.java`). On NeoForge it wraps | Plan B14 already uses Balm. Note: Balm amounts are `int`. A 300-post park at 10 FE per tick is 3,000 FE per tick, which fits `int`. Check the capacity maths, not the unit |
| Item and fluid transfer | `transfer.item`, `transfer.fluid`, `ResourceHandler`, `Transaction` `[v]` | `fabric-transfer-api-v1`, `ItemStorage` `[v]` | Not needed for hopper and pipe support: hoppers use vanilla containers | Expose vanilla `WorldlyContainer`-style slots for hoppers, plus the loader capability for pipes |
| Neighbour capability cache | `BlockCapabilityCache` with invalidation listener `[v]` | `BlockApiCache` `[v]` | No wrapper seen | Mekanism and Powah both use it (`AcceptorCache.java:30-35`, `CableBlockEntity.java:53`). Use it for generators that push to neighbours. Do not look up per tick |
| Capability on a block without a block entity | `RegisterCapabilitiesEvent.registerBlock(cap, provider, blocks...)` `[v]` | `BlockApiLookup.registerForBlocks` `[v]` | `Balm.capabilities()` for block entities | Lets machine parts without a block entity forward to the master (finding 8) |
| Level-wide saved data | `SavedDataType` with a codec. RS2 uses it for disk contents (`StorageRepositoryImpl.java:26-31`) `[v]`. Data attachments on levels (Mekanism `MultiblockManager.get`, `:116-118`) `[v]` | Vanilla `SavedData` and Fabric attachments `[inf]` | Not needed | Only for caches that can be rebuilt. Not for the wire graph (finding 2) |
| Chunk watch and ticket events | `ChunkWatchEvent`, `ChunkTicketLevelUpdatedEvent` `[v]` | Fabric API chunk events `[inf]` | Check | IE sends wires on chunk watch. Mekanism removes cables on ticket-level change |
| Extra geometry in a chunk mesh | `AddSectionGeometryEvent`. Sodium NeoForge calls it (`NeoForgeLevelRenderHooks.java:27-46`) `[v]` | None. Sodium Fabric returns an empty list (`FabricLevelRenderHooks.java:24-27`). Fabric API offers `RenderDataBlockEntity` for block models (`RenderDataBlockEntity.java`) `[v]` | None | Two renderers needed (open question 1) |
| Container menu sync | Vanilla `ContainerData` and menu data slots. Each slot travels as a 16-bit value in vanilla `[inf]` (check on 26.2) | Same (vanilla) | None | Small ints: vanilla. Energy and long values: a typed dirty-diff like Mekanism's, or split the value |
| GameTests | `net.neoforged.testframework` with `@GameTest` (Mekanism `TransmitterNetworkTest.java:28-36`) `[v]` | Fabric API gametest modules `[v]` | None | Plan B25.8 already puts GameTests in common code. Mekanism's network tests are the pattern for B14 acceptance |

### Mod availability for Nublar's two loaders

| Mod | NeoForge | Fabric | Effect on compat |
| --- | --- | --- | --- |
| Mekanism | Yes (26.3) | No | NeoForge only. Energy interop through the platform capability |
| Immersive Engineering | Yes (1.21.1 only seen) | No | Same. No 26.x port seen |
| Powah | Yes (26.1.2) | No | Same |
| Refined Storage 2 | Yes | Yes | Both loaders. Item capability based |
| Common Storage Lib | 1.21.1 only | 1.21.1 only | No 26.x. Not usable. Balm stays the bridge |
| Immersive Machinery | No (Forge 1.20.1) | Yes (1.20.1) | Old. Pattern source only |
| Immersive Thunder | Yes (1.21.6) | Yes (1.21.6) | Sound only. Pattern source only |

---

## Part A: Machines

### A1. Mekanism factory tiers and upgrade cards

**In game.** A Mekanism machine (for example an Enrichment Chamber) makes one product at a time. The player can put it into a Factory block of tier Basic, Advanced, Elite or Ultimate, which runs 3, 5, 7 or 9 products in parallel with one shared energy buffer. The player can also fill the machine's upgrade slot with Speed, Energy, Filter, Chemical, Muffling, Anchor or Stone Generator cards.

**Mechanism.**
- Factory lanes by tier: `FactoryTier` BASIC 3, ADVANCED 5, ELITE 7, ULTIMATE 9 (`FactoryTier.java:17-20`) `[v]`. Base work time: 10 seconds, 200 ticks (`TileEntityFactory.java:85`) `[v]`.
- Upgrades are data-pack entries now. `speed.json` holds `color`, `description`, `display_name` and `max: 8`. `muffling.json` has `max: 1` (`src/datagen/generated/.../upgrade/`) `[v]`. The set is: anchor, chemical, energy, filter, muffling, speed, stone_generator `[v]`.
- Speed: `ticks = base x M^(-n/max)`, `UpgradeUtils.getTicksD` (`:85-87`). If the result drops below 1 tick, the machine does several operations per tick (`getOperationsPerTick`, `:95-101`). `M` is the config `maxUpgradeMultiplier`, default 10 (`GeneralConfig.java:133-134`) `[v]`.
- Energy per tick: `base x M^((2 x speed - energy) / max)` (`getEnergyPerTick`, `:109-114`) `[v]`.
- Worked numbers from the formulas (derived): with 8 speed cards, 200 ticks becomes 20 ticks. Draw per tick is 100 times higher, so energy per product is 10 times higher. With 8 speed and 8 energy cards, draw per tick is 10 times (100 / 10). Energy per product is back to 1 times. The energy card pays for the speed card.
- Installing an upgrade calls `recalculateUpgrades` once, which stores `ticksRequired` and `operationsPerTick` as fields (`TileEntityFactory.java:422-427`) `[v]`. The tick loop reads two ints. Nothing recomputes per tick.
- Tier swap keeps contents: `parseUpgradeData` copies energy, slots and progress into the new tier block (`TileEntityFactory.java:455-475`) `[v]`.

**Lesson for Nublar (B12, B14).**
- Nublar's chip tiers are the Speed upgrade. Use the same shape: `work_ticks = base x M^(-tier/maxTier)`, `draw_per_tick = base x M^(2 x tier/maxTier)`. Keep the draw curve convex so a fast chip finishes sooner but costs more per product. The lead said "faster chips cost more power". This formula states how much more, in one config value `M`.
- Store the computed numbers on the block entity when the tier item changes. Do not read config or item tags per tick. This is the same pattern as plan B12.2.
- Accumulate progress as `progress += speedFactor` per tick instead of lowering the tick count. Then a very fast chip never needs "several operations per tick" code, and the per-tick cost is constant.
- Do not copy the Factory block idea (parallel lanes). Nublar's lead decided: hand-feed one fossil at a time, no mixed queue, no parallel processing (design decisions, module 2). Verdict for the Factory: Avoid.
- Do not copy the 7 upgrade cards. Nublar already has four tier item families (filter, tank, chip, bulb). The lead did not select "limited to 2 or 3 upgrades, some clash" (module 6). The Mekanism rule "one card type, up to `max` copies" is an alternative to "one slot per tier family". Nublar's current design is simpler for the player: each family has one slot.

**Compatibility duty.** None for Mekanism mods (NeoForge only). If a Mekanism user places Nublar machines next to Mekanism cables, energy flows through the platform capability. Nothing else.

**Platform overlap.** Data-pack upgrade registry is Mekanism's own. The formula needs no library. Data-driven tier numbers should go in the TOML plus per-tier JSON the lead chose (module config: "both").

**Verdict.** Adapt (formula and cached fields). Avoid (Factory lanes, upgrade-card slots).

### A2. Mekanism side configuration

**In game.** In a machine's screen, a Configuration tab (`GuiConfigTypeTab`) lets the player set a data type per face. The types are None, Input, Output, Input/Output, Energy and Extra, each with its own colour. An eject toggle pushes outputs into the neighbouring inventory. Input colours limit which coloured pipe can insert (`TileComponentEjector.java:76`, `:366-378`) `[v]`.

**Mechanism.**
- `DataType` enum: NONE, INPUT, INPUT_1, INPUT_2, OUTPUT, OUTPUT_1, OUTPUT_2, INPUT_OUTPUT, ENERGY, EXTRA, each with a colour (`DataType.java:12-23`) `[v]`.
- `TileComponentEjector.tickServer` runs for each transmission type that has ejection on. Items eject only when `tickDelay == 0`, then the delay resets to `TICKS_PER_HALF_SECOND` (10 ticks) (`TileComponentEjector.java:138-155`, `:311`) `[v]`. Other types call `eject` every tick, but only for non-empty tanks (`:146-150`, `:176-185`) `[v]`.
- Neighbour handlers come from a cache: `AcceptorCache` builds one `BlockCapabilityCache` per side lazily (`AcceptorCache.java:22-35`) `[v]`.

**Lesson for Nublar (B12).**
- Nublar's machines have a single input and a single output per product class (fossil in, tube out, by-products out). A six-face matrix is more UI than the machines need. Plan B05 asks for hopper and pipe support. Fixed rules cover it: items enter at the top and sides; outputs leave at the bottom and the back. Verdict: Avoid for Beta.
- If players later ask for "stop the processor pushing sand into my hopper", add one auto-eject toggle, not a face matrix.
- Two cheap patterns are worth copying: eject items on a 10-tick delay, not every tick, and look up neighbours through a capability cache.

**Platform overlap.** Vanilla hoppers, `WorldlyContainer` (`getSlotsForFace`, `canPlaceItemThroughFace`, `canTakeItemThroughFace`) cover the item rules. Capability caches cover the neighbour lookups.

**Verdict.** Avoid (face matrix). Adopt (10-tick eject delay and cached neighbour lookup, if Nublar ever ejects).

### A3. How machine GUIs show progress, energy and errors

**In game.**
- Mekanism: a progress arrow, an energy gauge, side tabs (Configuration, Security, Energy, Redstone control, Sorting: `client/gui/element/tab/`), and a Warning tab that appears only when something is wrong. Its tooltip lists the issues. Slots, bars, gauges and progress arrows can register a warning check, so the faulty element is marked (`GuiBar`, `GuiGauge`, `GuiProgress`, `GuiSlot` call `trackWarning`) `[v]`.
- IE: an energy bar and a progress arrow per machine. Multiblocks expose a comparator signal for fill state (manual `redstone_probe.txt`, `[doc]`).

**Mechanism.**
- Errors are values. `RecipeError` constants: INPUT_DOESNT_PRODUCE_OUTPUT, NOT_ENOUGH_ENERGY, NOT_ENOUGH_ENERGY_REDUCED_RATE, NOT_ENOUGH_INPUT, NOT_ENOUGH_SECONDARY_INPUT, NOT_ENOUGH_LEFT_INPUT, NOT_ENOUGH_RIGHT_INPUT, NOT_ENOUGH_OUTPUT_SPACE (`CachedRecipe.java:588-606`) `[v]`. The cached recipe reports a set of errors only when the set changes (`setErrorsChanged`, `:206-215`) `[v]`.
- The factory keeps a `boolean[process][errorType]` array and syncs it as a container array (`ErrorTracker`, `TileEntityFactory.java:709-767`, tracked at `:447-453`) `[v]`.
- Each GUI element asks for a "warning check" supplier (`GuiElectricMachine.java:29-32`). `GuiWarningTab.tick()` only checks `hasWarning()` and rebuilds its tooltip only if the text list changed (`GuiWarningTab.java:24-52`) `[v]`.
- Menu sync: `MekanismContainer.broadcastChanges` walks all tracked values, asks each `isDirty()`, and sends one packet with only the changed ones (`MekanismContainer.java:571-590`) `[v]`. `SyncableLong.isDirty` compares to the last sent value (`SyncableLong.java:19-24`) `[v]`. IE does the same with `GenericContainerData.needsUpdate` (`GenericContainerData.java:67-76`) `[v]`.
- Cost with nobody watching: zero. The tracked values exist only while a menu is open.

**Lesson for Nublar (B12).**
- Define a small `MachineIssue` set: NO_POWER, NO_INPUT, OUTPUT_FULL, WRONG_FILTER (fossil tier too low for this filter), NO_BULB, NO_BEDDING, NO_EGG, SPECIES_MISSING. The machine computes the set only when a slot, the energy state or the tier changes. It syncs one bitmask through the menu. The GUI shows an icon tab with a tooltip list. This turns the lead's "no power, no progress; stalled incubator" rules into player-visible text and costs one int.
- One bitmask int is a vanilla `ContainerData` slot. No typed sync system is needed for it. Energy and progress also fit vanilla slots if split (progress: percent 0 to 100; energy: scaled to a 0 to 1000 bar). So "use the platform" holds here. Mekanism's typed system exists because its values are long and chemical stacks.
- Keep the energy gauge in the menu only. Nothing in the world shows a number, so nothing needs to sync while nobody looks.

**Compatibility duty.** None.

**Platform overlap.** Vanilla `ContainerData`/menu data slots, `AbstractContainerMenu.broadcastChanges`. No library needed.

**Verdict.** Adopt (issue set plus warning tab). Adapt (typed dirty-diff only if long values need it).

### A4. Multi-block machines

**In game.**
- IE: the player builds the shape from engineering blocks, then right-clicks with the Engineer's Hammer. The blocks turn into one machine with one model. Breaking any part reverts the pieces (`TemplateMultiblock.createStructure` and `disassemble`, `TemplateMultiblock.java:165`, `:266`) `[v]`.
- Mekanism: the player builds a hollow cuboid (boiler, tank, matrix). The structure forms when the shape is valid. The screen opens from any valve or controller.
- Powah: the player holds 36 Reactor blocks and places one block in a 3 by 4 area. The reactor then places its other parts itself, one part every 5 ticks, with break particles (`Builder.java`, delay 5) `[v]`. It demolishes itself if a spot is blocked.

**Mechanism.**
- IE master and dummy blocks (1.21 design). The master block entity holds the state object and runs the logic. Dummy block entities only hold their position in the shape. Dummies get no ticker: `getTicker` returns null for `MULTIBLOCKSLAVE` blocks (`MultiblockPartBlock.java:96-107`) `[v]`. The state interface separates `writeSaveNBT` from `writeSyncNBT` (`IMultiblockState.java`) `[v]`. The master caches its render box over the whole shape (`MultiblockBEHelperMaster.java`, `renderBox`) `[v]`. A sync request calls `ServerChunkCache.blockChanged(pos)`, which lets vanilla batch the update (`MultiblockContext.java:57-67`) `[v]`. After a chunk or capability change the master invalidates capabilities on every part (`invalidateAllCaps`) `[v]`. The shape comes from a vanilla `StructureTemplate` data file.
- Mekanism: one `MultiblockData` per structure. Only the master block entity runs its tick, and only when formed (`TileEntityMultiblock.java:107-155`) `[v]`. A structure that is not valid retries validation every 10 ticks, and a structure that changed runs one update on the tick after the change (`TileEntityMultiblock.java:109-111`, `Structure.java:136-144`) `[v]`. A `recheckStructure` flag forces an immediate re-check (`:113-116`) `[v]`. A cache keyed by UUID holds the contents when the structure breaks, so items survive a rebuild (`MultiblockManager.java`, `FormationProtocol.java:105-165`) `[v]`.

**Lesson for Nublar (B12, extends survey item 9 "one master block entity").**
The earlier survey proposed one master block entity per machine. Four further steps cut more cost.
1. **No block entity on parts.** The machines are small (the processor, sequencer and printer are a few blocks). Give each part a block-state property for its place in the shape (a small integer or enum, like the post's existing `getIndexProperty()`), and find the master by a fixed offset table. Register the item and energy capability on the part blocks with the platform call for block-only capabilities (`registerBlock` on NeoForge, `registerForBlocks` on Fabric), and have the provider return the master's handler. Parts then have no ticker, no save data and no sync data. This is the vanilla door and bed pattern, widened. `[inf]`
2. **Master saves and syncs two different things.** `saveAdditional` holds items, energy, progress, tier items. `getUpdateTag` holds only what the client draws: tier look, active flag, dye colour, a few flags. Mekanism writes a "reduced" tag for the client (`TileEntityTransmitter.writeReducedUpdatedTag`, `:115-118`) `[v]` and Powah splits `writeStorable` from `writeSync` (`FurnatorBlockEntity.java`) `[v]`. This removes the full-NBT packet (S8) at its root.
3. **Render box.** Cache one `AABB` over the whole machine on the master (IE `renderBox`). GeckoLib 5 and the survey's C6 need a correct box for frustum culling, and for EntityCulling-style block-entity culling to keep a tall machine visible.
4. **Ordered assembly stays the lead's choice.** IE's hammer, Mekanism's shape check and Powah's one-block placement are three other flows. The lead chose ordered part placement. For the creative-only "fully assembled" item (plan B12), Powah's staged build (one part per 5 ticks with particles) is a ready pattern. A direct `StructureTemplate` placement is the simple one. IE's `StructureTemplate` shape file is a data-driven option if the machines are given a template per tier.

**Mekanism's UUID cache** is the one idea from Mekanism that matters: contents survive a break and a rebuild. The lead's rule is "every machine drops every slot on break". Keep drops. A kept-contents cache is a different design (a machine that remembers its recipe after the player re-places a part). Not needed for Beta. Verdict: Avoid.

**Compatibility duty.** If a part block has no block entity, the Fabric `RenderDataBlockEntity` and NeoForge model-data paths do not apply to it. Draw the machine only from the master's GeckoLib renderer, as today.

**Platform overlap.** `StructureTemplate`, block-state properties, `getUpdateTag`, block-only capability registration are all vanilla or platform.

**Verdict.** Adapt (block-state parts, master tick, save/sync split). Compat none.

---

## Part B: Power networks and wires

### B1. Mekanism energy network

**In game.** The player places Universal Cables of four tiers between generators and machines. Cables connect to each other and to any block with an energy capability. The network has one shared buffer. It also keeps a fill scale that the client uses for visuals `[v]`.

**Mechanism (all `[v]`, Mekanism `26.3` @ `feb1959`).**
- One `EnergyNetwork` object per connected group. It owns a `VariableCapacityEnergyContainer` whose capacity is the sum of its cables (`EnergyNetwork.java:33-39`; `DynamicBufferedNetwork.updateCapacity`, `:130-145`).
- Cables run almost no per-tick code: `TileEntityTransmitter.tickServer` only handles a "just joined" flag and a "refresh connections" flag (`TileEntityTransmitter.java:98-112`).
- One static registry ticks the world. `TransmitterNetworkRegistry.onTick` runs four steps: `handleChangedChunks`, `removeInvalidTransmitters`, `assignOrphans`, `commitChanges`, then calls `onUpdate()` once per network (`TransmitterNetworkRegistry.java:126-137`). If nothing changed, the first four steps return at once. The work is O(networks), not O(cables).
- Per network per tick: compute a "fill scale" for visuals and flag a client update only if the scale changed (`DynamicBufferedNetwork.onUpdate`, `:36-43`; `EnergyNetwork.computeContentScale`, `:146-156`, which moves the scale 0.02 per tick so the glow fades). Then `tickEmit`: skip when the buffer is empty, else build the target list lazily from the acceptor cache and call `EnergyUtils.emit` once (`EnergyNetwork.java:102-129`).
- Merge: a new cable next to two networks makes one new network that adopts both (`EnergyNetwork(Collection)`, `adoptAllAndRegister`, `DynamicNetwork.java:167-177`); the energy buffers add (`EnergyNetwork.adoptTransmittersAndAcceptorsFrom`, `:56-69`).
- Split: removing a cable invalidates the whole network. Each valid cable takes its saved share of the buffer, becomes an "orphan", and the orphan flood-fill rebuilds networks next tick (`DynamicNetwork.invalidate`, `:116-137`; `OrphanPathFinder`, `TransmitterNetworkRegistry.java:223-300`; orphans live in a sorted tree map, `:316`). Several changes in one tick coalesce through sets (`:46-47`).
- Persistence: each cable stores a "share" of the buffer (`startNewSaveShare`) updated only when the network is dirty (`EnergyNetwork.updateSaveShares`, `:89-100`; `DynamicBufferedNetwork.validateSaveShares`, `:159-172` limits it to once per game tick). On load the shares sum back. No network graph is stored.
- Unloaded chunks: `ChunkTicketLevelUpdatedEvent` records chunks that enter or leave the loaded range. Once per tick the registry calls `chunkAccessibilityChange(loaded)` on each cable in a changed chunk (`TransmitterNetworkRegistry.java:139-157`, `DimData.onTicketLevelChange`, `:332-347`, `handleChangedChunks`, `:348-380`) `[v]`. I did not read what a cable does with that call. It very likely drops out of or rejoins its network `[inf]`.
- Tests: GameTests for networks, including "reloading intermediary chunks does not break a network" (`TransmitterNetworkTest.java:75-83`) `[v]`.

**Lesson for Nublar (B14, B15, B25).**
- The cost model to copy: *change cost* is O(size of the affected network), paid once. *Tick cost* is O(networks), not O(posts or pylons). Nublar's posts drain 10 FE each per tick today (S7). A network object that knows its total draw (sum of post draws, updated only when a post connects, disconnects, or powers on or off) draws once per tick from the generators. 60 posts then cost one `extract` call, not 60. At 300 posts the saving is about 300 `extract` calls per tick (estimate: under 0.1 ms; the real win is no per-post tick, no allocation).
- Use "shares" (or recompute from block entities) so the world file stores the energy of each block, not a graph. Nublar's posts and pylons store their own energy today.
- Keep `FencePowerService` as the owner. Give it the merge and split methods named in this section. Name the two rules "merge on connect" and "invalidate-and-reflood on break". They are independent of how pylons are drawn.
- Acceptance test (B14): copy Mekanism's chunk-reload test shape. Build a 100-post line across a chunk border, unload and reload the middle chunk, and assert the powered flag.

**Compatibility duty.** Mekanism cables adjacent to a Nublar block with the energy capability connect automatically on NeoForge. Nublar's capability must therefore report `canInsert`/`canExtract` correctly per side so a Mekanism cable cannot drain a fence post that should only accept power. Check `insert` returns 0 when the block is full.

**Platform overlap.** `BlockCapabilityCache`, energy capability and `Transaction` come from the platform (NeoForge 26.2) `[v]`. Mekanism's network code is its own. The merge and split rule is about 150 lines of algorithm. Balm gives the storage facade, not the network.

**Verdict.** Adapt (network object with merge and split, buffer on the network, cached acceptors). Avoid copying class structure (generic `DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>` with six type layers is too heavy for one network type).

### B2. IE wire network and energy transfer

**In game.** The player right-clicks a wire coil on one connector, then on a second. A wire with a catenary curve appears. Low, medium and high voltage wires carry different power and span different distances. Wires lose power with distance. A wire that carries too much power burns and drops. Transformers join different voltages. Relays join wires without feeding power. Breaker switches open or close a line.

**Mechanism (all `[v]`, IE `1.21.1` @ `75a27f0`).**
- Graph layers. `GlobalWireNetwork` (one per level, `SavedData`) owns many `LocalWireNetwork` objects, one per connected group (`GlobalWireNetwork.java:60`, `:73-74`). Adding a connection merges two local nets if they differ (`addConnection`, `:127-148`). Removing a connection removes it from its net and calls `splitNet` (`removeConnection`, `:170-195`; `splitNet`, `:214-220`).
- Connectors do not tick. `ImmersiveConnectableBlockEntity` has `onLoad`, `onChunkUnloaded`, `setRemoved` and no tick method (`ImmersiveConnectableBlockEntity.java:43-62`). One level-tick hook calls `GlobalWireNetwork.update` (`EventHandler.java:197`), which loops local nets (`GlobalWireNetwork.java:443-461`).
- Connector loads are queued and processed in one batch, and only when the chunk is safe (`processQueuedLoads`, `:338-376`) `[v]`.
- Energy is a "local handler" on each local net. Sources and sinks are collected once. Paths from each source to each sink come from Dijkstra over wire loss, cached per source (`EnergyTransferHandler.getPathsFromSource`, `:141-152`; `runDijkstraWithSource`, `:190-226`). Any change to connectors or connections calls `reset()` and clears all caches (`:47-82`, `:114-125`) `[v]`. The next tick rebuilds lazily.
- Each tick (`transferPower`, `:228-325`): for each source with available energy, sum what each reachable sink requests, limited by transformers on the path (`limits`), scale by the path's loss, split by fraction, and pass energy along the path. Wire loss is exponential: `availableFactor *= (1 - lossRate)` per hop (`:299`).
- Loss rate per connection is `lossRatio x length / maxLength` (`IEWireTypes.java:199-202`) `[v]`. Defaults: copper 16 blocks, 2,048 IF per tick, 1.25 percent; electrum 16 blocks, 8,192, 0.3 percent; high voltage 32 blocks, 32,768, 0.08 percent (`IEServerConfig.java:54-65`) `[v]`.
- Overload: each connection records energy carried in the tick. Next tick, a connection above its rate "burns": it is removed and 15 flame particles spawn along the curve (`burnOverloaded`, `:327-350`; `IEnergyWire.burn`, `:418-433`) `[v]`.
- Cost per tick: O(sources x sinks x path length) per local net, with the path lists cached between topology changes `[inf]`.
- Wires are data in the template system: `WireTemplateHelper` writes and reads connections inside structure NBT, so a pasted structure keeps its wires (`WireTemplateHelper.java:32-96`) `[v]`.

**Lesson for Nublar (B14, B15).**
- Nublar's lead decided: pylon lines carry power with no loss (plan B14). IE's path search, loss maths and transformer limits exist to serve loss. With loss = 0 the whole path layer is unnecessary: the local network reduces to a buffer plus sets of sources and sinks (Mekanism's model, B1). Verdict for the path layer: Avoid.
- Take from IE: (a) *no ticking connectors*: posts and pylons should run no per-tick code and should call into the network on change only; (b) *one level-tick hook* that updates all networks; (c) *queued connector loading* so a chunk full of posts does not re-merge networks 300 times; (d) *burn and removal with particles* is a precedent for "wire at zero health breaks" (plan B15): remove the connection in one call and spawn the effect from the server.
- Take from IE for tuning: *per-wire-type config blocks* with `maxLength`, `transferRate`, `lossRatio` (`IEServerConfig.java:104-135`). Nublar's `fences.*.<tier>` keys follow the same shape. Add `max_span` per tier. The range of pylons (`power.pylon_range`) is the same concept as `maxLength`.
- Do not store the graph in a level save as IE does. IE needs it because its connectors have "proxies" for unloaded chunks (`IICProxy`, `api/wires/proxy`), plus a sanitizer (`NetworkSanitizer.java`) and a validator. RS2 chose the other way for good reasons (finding 2).

**Compatibility duty.** None (IE is NeoForge only). If a player runs both mods, they are separate wire families. Nothing in Nublar should assume the IE API.

**Platform overlap.** None. IE's wire API is its own.

**Verdict.** Adapt (connection list per tier config, one-hook update, queued loads, burn effect). Avoid (loss, Dijkstra, transformer limits, central saved graph).

### B3. IE wire damage (shock) and wire collision

**In game.** A player or mob that touches a live copper, electrum or steel wire is shocked and knocked back. Insulated wires do no harm. The damage is higher on higher voltage and higher when the grid has more energy to give. Placing a solid block into a wire breaks the wire and drops the coil (config `blocksBreakWires`).

**Mechanism (all `[v]`).**
- Wires are indexed per block position on the server: `WireCollisionData.blockToWires` maps each block that the curve passes through to its `CollisionInfo` list (entry and exit points) (`WireCollisionData.java:30-72`; built by ray-tracing the catenary once when the connection is added).
- Contact test: a mixin on `BlockStateBase.entityInside` calls `WireCollisions.handleEntityCollision(pos, entity)` (`AbstractBlockStateMixin.java:25-29`; `WireCollisions.java:40-60`). The call returns at once for client, creative players or when the config is off. Otherwise it looks up the position's wire list (usually empty) and calls each local net's collision handlers.
- Why a mixin: wires cross air blocks. Air still gets `entityInside` through the state, so IE hooks the state method.
- Shock: `WireDamageHandler.onCollided` first checks the entity box inflated by the wire's damage radius (copper 0.05, electrum 0.1, steel 0.3 blocks) against the segment inside this block (`:50-71`). Then it asks the energy handler for sources that can reach this wire, sums available energy (after loss), caps at the wire's transfer rate, and asks the wire for damage (`:74-95`).
- Damage formula: `factor x energy / transferRate x 8`, factor 2, 5 and 15 for copper, electrum and steel (`IEWireTypes.java:243-253`). At full load that is 16, 40 and 120 health (8, 20 and 60 hearts) (derived). At 10 percent load it is a tenth of that.
- The shock consumes energy: the handler extracts `energy x damageDone / damagePossible` from the sources and adds it to the wire's carried total (`WireDamageHandler.java:78-95`), so repeated shocks drain the grid and can burn the wire.
- Placing a block: `WireCollisions.notifyBlockUpdate` checks the new block's position in `blockToWires` and removes crossed wires (`:63-105`).

**Lesson for Nublar (B15).**
- Nublar's fence wires are real fence blocks plus wire data, so `entityInside` is a method on a real block and no mixin is needed. The survey's S6 and B15.1 (build collision boxes once per block entity) cover the cost side.
- The design idea is new: *shock strength follows the network's delivered energy, and a shock draws energy.* Effects in play:
  - An empty or half-empty grid gives weak or no shocks. Animals test a fence, find it dead, and push (the lead's "unpowered fence gets tested").
  - A big animal that hangs on a live wire drains the generator. A park with a weak generator and a pushing T. rex can brown out its own fence. That is outage gameplay that comes from physics, not a script.
  - It also gives a use for the large generator and later for a battery block.
- This conflicts with one settled rule only if shock damage must be a fixed config number per tier (`fences.damage.<tier>`). The lead settled "high security hurts more". A scale factor from 0 to 1 (network charge or satisfaction) multiplies that number. See open question 3. I make no change until the lead decides.
- Voltage-style tiers: IE's three voltage categories (LV, MV, HV) with different damage radius, damage factor, span and transfer map directly to Nublar's low and high security fences. Nublar needs two rows, not three.
- Placing a solid block into a fence line: Nublar has real fence blocks on the line, so this is vanilla block placement.

**Compatibility duty.** If Nublar ever adds a mixin on `entityInside` it would conflict with IE's mixin on the same method only if both inject at HEAD with a cancel. Nublar has no need for a mixin here, so no duty arises.

**Platform overlap.** `Block.entityInside` is vanilla. Damage sources and knockback are vanilla.

**Verdict.** Adapt (energy-scaled shock, per-tier damage radius, shock drains grid) pending lead decision. Adopt (config shape).

### B4. IE wire rendering: hundreds of wires without block-entity renderers

**In game.** A world with several hundred wires renders them at no visible frame cost. Wires hang in smooth curves and are lit by the surrounding light.

**Mechanism (all `[v]`).**
- Curve: a catenary solved in closed form per connection and cached in the connection (`Connection.makeCatenaryData`, `:164-205`; `getCatenaryData`, `:244-250`). The slack factor for normal wires is 1.005, so the curve is nearly straight (`IEWireTypes.java`, `getSlack`). A wire draws as 16 segments (`Connection.RENDER_POINTS_PER_WIRE = 16`, `:24`).
- Client index: on the client, `sectionsToWires` maps each 16-block chunk section to the wire pieces that pass through it (`WireCollisionData.java:35`, `:46-56`).
- Geometry: `ConnectionRenderer.onSectionRender` handles `AddSectionGeometryEvent`. For a section that has wires it adds a renderer that writes quads into the section's `RenderType.solid()` chunk buffer (`:75-103`). The wire is rebuilt only when that section is rebuilt.
- Shape cache: segment geometry is cached by (radius, colour, catenary shape, index range) for 120 seconds after the last access (`SEGMENT_CACHE`, `:53-56`). Identical wires share vertices.
- Mesh size: each segment is two crossed ribbons, and each ribbon is written front and back, so 16 vertices per segment and 256 vertices per wire (`renderSectionForCache`, `:144-169`, and `renderBidirectionalQuad`) (derived from the code). 300 wires are about 77,000 static vertices in chunk meshes `[inf]`. The per-frame cost for wires is zero. A section rebuild redraws them.
- Light: sampled once per segment end (`getLight`, `:171`).
- Connection changes dirty only the sections they touch: `MessageWireSync` collects the sections along the curve and calls `setSectionDirty` for each (`MessageWireSync.java:68-71`).

**Lesson for Nublar (B15, C5, B25).**
- Today: a `GeoBlockRenderer.renderFinal` per post draws 39 floats per connection through `RenderUtils.drawSpacedCube` with `RenderType.leash()` every frame (`ElectricFenceRenderer.java:17-50`). That is a per-frame block-entity draw, per post, per connection. It also depends on the post's render box, so a wire may vanish when its post leaves the frustum.
- On NeoForge, the IE approach removes all of that: no per-frame wire cost, no culling pop, and Sodium supports it (finding 3).
- On Fabric there is no section geometry hook. Options: (1) keep a BER, but one *light* renderer for wires only with cached vertex arrays per connection and a render box covering both posts; (2) add the wire quads to the post's block model through the Fabric Rendering API, using `RenderDataBlockEntity` to pass connection data into the model. The second option puts wire quads in the post's section mesh, which is the same idea as IE, but a wire that crosses a section border is drawn only when the post's section is visible. This needs a test `[inf]`.
- A catenary closed form, cached per connection, would replace Nublar's stored 39-float array (`connection.getRenderData()`) and costs about 10 values per wire.
- Check that the per-post GeckoLib renderer does not also draw the wires. Splitting the "post model" renderer from the "wire" renderer is the minimum change, on both loaders.

**Compatibility duty.**
- Sodium and Embeddium: vanilla BER wire drawing works with them, but is the slow path. For the NeoForge chunk-mesh path, Sodium needs the NeoForge event (supported at `dev`).
- Iris: wires in `RenderType.solid()` chunk buffers follow the shader pack's terrain pipeline. A BER using `RenderType.leash()` goes through the entity pipeline, which some packs shade differently `[inf]`. Test with Iris.
- EntityCulling and block-entity culling mods: BER wires can be culled by the post's box. Chunk-mesh wires cannot.
- Do not use dynamic lights from wire quads in chunk meshes: a chunk mesh is rebuilt on any light change in its section.

**Platform overlap.** NeoForge: `AddSectionGeometryEvent` (platform). Fabric: none. Balm: none. Renderer choice per loader is the P2 job.

**Verdict.** Adapt (NeoForge: adopt IE approach; Fabric: BER with cached vertices or baked model quads). The data model (connection list, catenary cache, section index) is shared.

### B5. IE wire sync: wires travel as events, not as state

**In game.** Walk up to a wire network: every wire in view appears. Cut one, and only that wire disappears. Far wires are never sent.

**Mechanism (all `[v]`).**
- `WireSyncManager` sends `MessageWireSync(connection, ADD|REMOVE|UPDATE)` when a connection changes, to players who track either endpoint chunk (`WireSyncManager.java:75-87`, `:116-130`).
- When a player starts or stops watching a chunk, the manager sends the connections of that chunk. It keeps a set of watched chunks per player so that a wire is sent when its second endpoint chunk becomes visible (`onChunkWatch`, `:90-108`, `shouldSendConnection`, `:52-60`).
- The client rebuilds the catenary from the endpoints and the wire type, and marks the touched sections dirty. The message carries two points and a type, not curve data (`MessageWireSync.java`).

**Lesson for Nublar (B15).**
- Today the post sends `sendBlockUpdated` when its flipped flag or cached rotation changes (`BlockEntityElectricFencePole.java:88`, `:103`) `[v]`. I did not trace how the connection list itself reaches clients (it lives in the connectable base class) `[inf]`. The wire message pattern is the target for it: one small payload per connection change, plus one batch on chunk watch. Nublar can use the platform's chunk-watch event and a custom payload on both loaders. Cost: one payload per connect or break. Wire health changes (an animal hits a wire) should *not* send a message per hit; send the health band (for example 4 levels) only when it crosses a band, so cracks show but the network does not flood.
- Payload size estimate: two block positions (16 bytes), a type byte, a health band byte: about 20 bytes per connection `[inf]`. A paddock with 100 connections is about 2 KB on first sight.

**Compatibility duty.** Packet-size mods (Krypton, PacketFixer) change compression and packet limits, not content. Small payloads stay safe. A full-NBT update per post per change is the pattern those mods exist to survive.

**Platform overlap.** Custom payloads and chunk-watch events are in NeoForge. On Fabric, use Fabric networking and a chunk-tracking event `[inf]`. Balm may wrap payloads; check.

**Verdict.** Adopt (connection events plus batch on watch, health in bands).

### B6. IE gameplay parts that map to Nublar's fences and park tech

All text below is from the IE manual files in `src/main/resources/assets/immersiveengineering/manual/en_us/` `[doc]` unless marked.

| IE part | What the player does | Nublar mapping | Verdict |
| --- | --- | --- | --- |
| Wire coil (`basic_wiring.txt`) | Right-click connector A, then connector B. A wire breaks if a block obstructs it. Throughput is the lowest tier on the path | The fence spool and pylon line placement (B14, B15). The same two-click flow | Adopt (flow) |
| Connectors and relays | Connectors feed or take power. Relays are hubs that do neither | Posts are both. Pylons are relays | Not relevant (no new block) |
| Insulated wire | Prevents shock | None. A fence that does not shock is not a fence | Not relevant |
| Feedthrough insulator (`feedthrough_insulators.txt`) | A wire passes through a wall: two connectors join across a one-block wall | Plan B15: "a feeder placed in a post line keeps the wire run intact". Same idea: a block that carries the wire through itself | Adapt (concept) |
| Circuit breaker, manual and redstone (`circuit_breakers.txt`) | A switch opens a line. It emits a redstone signal when closed, inverted with a screwdriver. A redstone breaker opens on signal and takes HV | The plan's fence switch block (B15). Precedent for: hand toggle plus redstone control plus invert | Adopt (behaviour) |
| Current transformer (`current_transformer.txt`) | Shows average power on right-click. A comparator at its base outputs 0 to 15 by load fraction | A power meter on a pylon or generator for park monitoring. Comparator output pattern is plan B12.4 | Adopt (comparator output, post-Beta block) |
| Redstone wiring (`redstone_wiring.txt`, `RedstoneNetworkHandler.java`) | 16 colour channels over a wire. Each connector is input or output on one channel. The network value per channel is the maximum of all emitters (`fillWithMax`, `:139-148`). It recomputes only when flagged (`updateNextTick`, `:28`, `:39-42`, `:92`) `[v]` | One fence section per channel would let a control room switch paddocks. This is a post-Beta idea. In Beta the plan gives a switch block and plain redstone | Not relevant in Beta. Idea after Beta |
| Redstone probe (`redstone_probe.txt`) | Reads a comparator value and puts it on a channel, with a threshold | Same as above | Not relevant |
| Razor wire (`razor_wire.txt`) | A wire that slows and cuts entities. LV wire can electrify it | Gates and overhanging fences are not in Beta. A deterrent stack is a post-Beta idea | Not relevant |
| Lightning rod (`lightning_rod.txt`) | Steel fence poles on a rod catch lightning. Energy goes to the base and trickles to connected wires | A storm event that charges or damages fences fits the Jurassic Park tone (the storm during the power cut). Needs weather hooks, not in the plan | Not relevant in Beta. Idea after Beta |
| Diesel generator multiblock (`diesel_generator.txt`) | Built from named blocks, formed with the hammer. Fuel goes to bottom corners. Output splits across up to 3 plugs. It runs only if at least one output accepts energy. A redstone terminal turns it off | The "large generator" (B14). The rule "runs only when an output accepts energy" is the on-demand burn that the lead did not select ("burns only on demand" was not selected). Keep fuel burning at a fixed rate unless the lead says otherwise | Not relevant (rule) |
| Machine interface | Reads multiblock inventories and energy for automation | A park computer or tablet. The tablet is not in Beta | Not relevant in Beta |

**Platform overlap.** Comparators and redstone are vanilla. IE's colour channel wires are a mod feature.

### B7. Powah

**In game.** Seven tiers of each block: Starter, Basic, Hardened, Blazing, Niotic, Spirited, Nitro, plus Creative. Generators: Furnator (solid fuel), Magmator (lava), Solar Panel, Thermo Generator (hot block below, coolant), Reactor (multiblock, Uraninite). Also Energy Cells, Energy Cables, Ender Cells and Ender Gates (wireless), an Energizing Orb (crafting by energy), a Discharger, a Hopper and a Player Transmitter. The guide book lists every tier's capacity, output and maximum transfer (`guidebook/generators/*.md`) `[doc]`.

**Mechanism (all `[v]`, Powah `26.1` @ `4274274`).**
- Scaling table: tier multipliers 1, 4, 10, 40, 100, 400, 2000 for "active production" (base 20 FE per tick), so the Furnator makes 20, 80, 200, 800, 2,000, 8,000 and 40,000 FE per tick by tier. Passive generators (solar) use 1, 3, 5, 10, 20, 40, 100 (`DefaultEnergies.java:15-33`). Capacity and transfer are derived from production (`:45-80`).
- Energy per fuel tick is 30 FE (`DefaultEnergies.java:10`). Coal burns 1,600 ticks, so a coal item is worth 48,000 FE. At 20 FE per tick (Starter) that coal lasts 2,400 ticks, 120 seconds (derived). Higher tiers burn the same coal faster for the same energy (the guide says so, `furnator.md`).
- The Furnator loads the fuel into a counter, spends `min(room, output, remaining)` each tick, and requests a sync only when state changes (`FurnatorBlockEntity.java`, `postTick`).
- `postTick` returns the number of ticks until the next client sync, or -1 for "none". `sync(delay)` sets a countdown and keeps the shorter of old and new delay. When it reaches 0, one block update is sent (`PowahBaseTickingBlockEntity.java:29-73`). This coalesces any number of changes inside the delay window into one packet. Furnator uses delay 4 for fuel and burning changes and returns 10 while it pushes energy (`FurnatorBlockEntity.java:70`, `:83`, `:88`, `:91`). Solar returns 5 or -1 (`SolarBlockEntity.java:68`).
- Solar checks the sky only every 40 ticks (`ticks % 40L == 0L`, `SolarBlockEntity.java:54`), and a Lens of Ender skips the check.
- Cables hold no energy. A generator pushes into a cable, and that cable walks its whole net's cable list, rotating the start index each call, to hand energy to cables that have a side energy consumer (`CableBlockEntity.insertEnergy`, `:144-185`). The net is a lazy list, dropped to null on any neighbour change and rebuilt by a breadth-first search the next time it is needed (`CableNet.java`, `updateAdjacentCables`, `calculateNetwork`). A re-entrancy guard stops loops (`netInsertionGuard`).
- Reactor builds itself: placing one block queues the other 35 positions. One part is placed every 5 ticks, with particles. If a position is blocked, the reactor demolishes what it placed (`Builder.java`).

**Lesson for Nublar (B14).**
- Accessible power for a park: Powah's recipe for readability is *one name per tier, same machine, scaled numbers, and a guide page that prints live numbers from config*. Nublar has two generators (basic, large) plus a creative one. Use the same layout: one config table `power.generator.<tier>` with `output_fe_per_tick`, `capacity`, `fuel_multiplier`. Lead decided the basic generator burns coal at furnace rate (80 s) and the large generator "makes fuel last longer" (design decisions). Powah's rule is the reverse (higher tier burns faster for the same energy per fuel tick). Both are valid. The lead's rule is the one to build.
- Energy per fuel tick as one constant keeps the balance table honest: coal at a given energy per tick of fuel gives one number to tune. For the lead's "one basic generator runs four machines plus about 10 posts", set `energy_per_fuel_tick`, then derive output from the draw sum, not the other way round. The earlier survey notes that the local generator makes 0.2 FE per tick while a post draws 10 `[v]`. A single derived constant fixes that class of error.
- Take the `sync(delay)` coalescing wherever a block entity shows state in the world (generator burning flag, machine active flag). One packet per window, no matter how many changes.
- Take the solar idea of sampling slow things on a stride (`ticks % 40`). Use it for any generator or machine that checks the world (sky for a solar array, water for a coolant check). The survey's S25 pattern "slower idle work" covers it.
- Avoid Powah's cable walk. `insertEnergy` iterates the whole net per push per tick, so cost grows with cables x generators `[inf]`. Mekanism's buffer-on-network is better for a park with long lines.
- Powah's Reactor pattern (self-building multiblock) fits the "creative-only assembled machine" item: staged placement avoids a big one-tick change.
- Tier naming and 7 tiers: Avoid. Nublar's tiers are parts (filters, tanks, chips, bulbs), not generators.

**Compatibility duty.** Powah (NeoForge) cables connect to any block with the energy capability. A Nublar generator or machine that exposes it will join Powah cables for free. Same duty as B1: report insert/extract per side correctly.

**Platform overlap.** `EnergyHandler`, `Transaction`, `SnapshotJournal` (Powah uses a journal for rollback in `CableBlockEntity`, `startIndexJournal`) are NeoForge's transfer API. `BlockCapabilityCache` is used per side. Furnace fuel time comes from vanilla `getBurnTime(RecipeType.SMELTING, level.fuelValues())` `[v]`, so any modded fuel works, which matches the lead's "any furnace fuel".

**Verdict.** Adapt (tier table shape, energy-per-fuel-tick constant, `sync(delay)`, strided sampling). Avoid (cable walk, seven tiers). Compat (energy capability).

---

## Part C: Storage and logistics

### C1. Refined Storage 2

**In game.** The player builds a storage network: a Controller (needs outside power), Disk Drives holding storage disks of 1k to 64k items and fluid disks, a Grid screen that lists every stored resource with search, Importers and Exporters on neighbouring inventories, Constructors and Destructors that place and break blocks, an External Storage block that exposes any chest or machine as network storage, an Interface, a Detector, wireless access and autocrafting. Devices that touch connect on their own. Cables are optional and can be coloured. If the Controller runs out of energy, the network shuts down (`docs/pages/networking/controller.adoc`) `[doc]`. Upgrade cards fit per device: Speed (up to 4), Stack, Range, Fortune and others (`AbstractModInitializer.java:529-565`) `[v]`. The design page says RS avoids config toggles for behaviour, to keep modpacks consistent (`docs/pages/design-philosophy.adoc`) `[doc]`.

**Mechanism.**
- *No persistent network.* "Networks cannot be persisted... All persistent data should be stored on the corresponding block entity" (`docs/architecture/decision/006-no-persistent-networks.md`) `[v]`. The reason given: a persisted graph drifted from the real world with block-moving mods.
- *Merge and split on change.* `NetworkBuilderImpl.initialize` finds connections around a new node and merges the networks it touches into a pivot network. `remove` picks a pivot among the remaining nodes, finds which nodes became unreachable and builds new networks for them. `update` does both for a node whose connections changed. Deterministic ordering keeps results stable (`NetworkBuilderImpl.java:30-196`) `[v]`.
- *Nodes do work only when active.* `AbstractNetworkNode.doWork` returns at once when the node has no network or is inactive; otherwise it extracts the node's energy use from the network (`AbstractNetworkNode.java:36-41`) `[v]`. The block-entity ticker updates the "active" block-state flag and then calls `doWork` (`NetworkNodeBlockEntityTicker.java:35-41`) `[v]`. The network's energy component sums over providers on demand and has no tick of its own (`EnergyNetworkComponentImpl.java`) `[v]`.
- *Adaptive polling.* External Storage polls its neighbour at 40, 30, 20, 10 or 5 ticks. The start is 20. Each poll that finds a change steps one level faster; each poll that finds none steps one slower; a neighbour block update steps faster (`ExternalStorageWorkRate.java`; `AbstractExternalStorageBlockEntity.java:115-126`, `:128-130`) `[v]`.
- *Watchers receive deltas.* A Grid registers as a watcher on the root storage. The listener forwards each resource change with its amount. On a network merge or split all watchers are invalidated and replayed (`GridWatcherManagerImpl.java:23-60`; `GridWatcherRootStorageListener.java`) `[v]`. Nothing is sent while no Grid is open.
- *Item identity.* A stored item is `ItemResource(item, DataComponentPatch)`. Two stacks with different components are different resources (`ItemResource.java`) `[v]`. Fuzzy mode strips the components (`normalize`).
- *Disk contents* sit in level `SavedData` keyed by a UUID that the disk item holds. A removed disk leaves its data in the world until re-inserted (`StorageRepositoryImpl.java:26-80`, `docs/pages/storage/disk-drive.adoc`) `[v]` `[doc]`.
- *Debounce on load.* The Unreleased changelog notes a Detector that sent a wrong redstone pulse on chunk load now changes only after the value is stable for 1 second (`CHANGELOG.md`, Unreleased) `[doc]`.
- *Platform-facing API.* A mod adds storage to a network through the item capability on its block. RS also has factory interfaces for custom external storage (`ExternalStorageProviderFactory.create(level, pos, direction)`), grid strategies, importer and exporter strategies and an upgrade registry (`refinedstorage-common-api/.../api/`) `[v]`. Nublar needs none of these for Beta.

**Lesson for Nublar (B05, B12, B14, B20).**
- *As DNA logistics.* The processor makes tubes. The sequencer takes tubes. The printer, gene editor and incubator take drives, syringes and eggs. A player with a big base will want these moved by network. With RS2, the player puts an Exporter on a Nublar machine and an Importer on its output. This works if the machine exposes standard item slots by side. That is plan B05 ("hopper and pipe input") already. No Nublar code for RS2 is needed.
- *Compatibility duty (item identity).* RS keys on item plus components. For items that should stack and be found in a Grid, the components must be the same for the same thing. Fossils and tubes: store species, quality, biome as deterministic components. Do not add a random seed, a UUID, a timestamp or a progress value to a stackable item's components. Eggs and syringes with genome data are unique by design, so they will not stack, which is correct.
- *Compatibility duty (output slots).* RS and hoppers extract from output slots only. Mark input slots as insert-only and output slots as extract-only per side. A machine that lets a pipe pull the filter or the chip out breaks the player's machine.
- *Compatibility duty (change signal).* External Storage polls. A neighbour block update speeds it up. A machine that changes slots must call `setChanged()` and send a neighbour update the vanilla way (comparator update on a content change, plan B12.4), or RS will read it at its slow rate. Do not send block updates every tick for this (S8).
- *Adopt the adaptive poll* for any Nublar device that watches a neighbour inventory on its own: the feeder that watches a hopper or chest (B20), the live-bait feeder, a future container-watching sensor. Level 20 ticks at start, 5 to 40 as the range, step on change. This replaces a fixed poll and cuts idle cost by the ratio of slow to fast (up to 8 times for a feeder that sits idle) `[inf]`.
- *DNA drive on the item.* The lead decided the data lives on the drive item. RS puts disk data in `SavedData` because a disk can hold millions of counts and moves through inventories. A DNA drive holds a few dozen entries (species, percent, biome shares), a few hundred bytes `[inf]`. Keep it on the item. If a drive ever holds thousands of entries, RS's UUID plus level data is the fix. Verdict: Avoid copying; note the trigger.
- *Controller shut-down* is the closest model for "no power, machines stop": the network goes inactive as a unit, and the active flag on each device flips once. This matches the lead's power rule.
- *RS has no config toggles* by policy. Nublar's lead chose TOML plus data-pack JSON for balance numbers. These do not conflict. RS's rule is about behaviour switches, not tuning.

**Platform overlap.** Item and fluid handlers come from the platform on both loaders. `ItemResource` and the network graph are RS's own.

**Verdict.** Compat (item capability, no code). Adopt (adaptive poll). Avoid (copying the network graph code: Nublar's graph is smaller, see B1).

### C2. Common Storage Lib

**In game.** Nothing. It is a library from Terrarium (Botarium's successor).

**Mechanism `[v]`.** `ValueStorage` (long amounts) behind `EnergyApi`, `FluidApi`, `ItemApi`, each declared with `@Expect` and implemented per loader (`energy/EnergyApi.java`, `energy/EnergyApiActual.java` in `core/fabric` and `core/neoforge`). `UpdateManager<T>` offers `createSnapshot`, `readSnapshot` and `update`, for rollback (`storage/base/UpdateManager.java`). Item contexts wrap player slots (`context/`). Version 0.0.10, Minecraft 1.21.1, NeoForge 21.1.133. Branch list ends at `1.21.x`.

**Lesson for Nublar.** None for P2. The lead chose Balm to replace Botarium. CSL has no 26.x branch, so it cannot be used for the 26.2 target. NeoForge 26.2 already ships `EnergyHandler` and `Transaction` (platform baseline). The snapshot idea is the transaction idea: both loaders now give a way to simulate and roll back an insert, so machine code should not invent its own snapshot class.

**Verdict.** Avoid (library). Not relevant to Beta.

---

## Part D: Two small repositories

### D1. Immersive Thunder

**What it does.** It replaces the sound of lightning on the client. It adds three thunder sounds (close, medium, far) and picks one by the distance from the local player to the bolt: up to 90 blocks close, up to 140 medium, beyond that far (`ThunderSoundInterface.java:8-9`). The volumes are 5,000 and 10,000, so the sound is still audible after vanilla distance falloff (`:11-13`). The mod is one mixin that redirects the `playLocalSound` call in `LightningBolt.tick` (`ImmersiveThunderMixin.java:21-35`) `[v]`. The README says it sounds best with Sound Physics Remastered `[doc]`. There is no gameplay and no machine.

**Lesson for Nublar.** A Jurassic Park storm (the power-cut scene) needs sound by distance. That is a small client feature (a mixin or an event handler, plus sound files), and it is not in the plan. Cheap check: the mixin calls `Objects.requireNonNull(player)` on the local player inside the bolt tick (`:26`), which can throw when no local player exists `[inf]`. Nublar must not copy that line.

**Compatibility duty.** Two mods that redirect the same `playLocalSound` call in `LightningBolt.tick` conflict. Nublar should not add a mixin there. If Nublar wants storm sounds, use a custom sound event played from its own weather event.

**Platform overlap.** Vanilla sound events and `sounds.json`. Fabric API and NeoForge have no thunder-specific hook; a redirect is the usual way `[inf]`.

**Verdict.** Not relevant (Beta). Idea after Beta: storm sound and fence flicker.

### D2. Immersive Machinery

**What it does.** It adds four small rustic machines as *vehicle entities* built on Immersive Aircraft's `EngineVehicle`: the Bamboo Bee (a programmable item-courier drone), the Redstone Sheep (a crop harvester that returns to its spawn when full), the Tunnel Digger (a rideable drill) and the Copperfin (an underwater vehicle powered by a conduit). The item descriptions say so (`en_us.json`, `item.immersive_machinery.*.description`) `[v]`. Version 0.2.1. It depends on Immersive Aircraft.

**Mechanism `[v]`.**
- *Path planning for non-mob vehicles.* `PilotNavigator` creates a hidden `Bee` as the "pilot", runs the vanilla `PathFinder` with a flying or walking node evaluator, and drives the vehicle along the path (`PilotNavigator.java:46-62`, `:88-114`, `:116-157`). Paths are cached in a Guava cache of 256 entries for 5 minutes, keyed by (from, to) block positions (`:41-44`, `:92-105`). A path that ends too far from the target is dropped (`:78-81`). A vehicle that makes no progress for 100 ticks drops its path (`:151-157`).
- *Courier drone.* With no task it searches for one only every 60 ticks (`BambooBee.java:90-96`). It takes items from a `Container` block entity and drops them in another (`:100-135`). It reads the `Container` interface, not the item capability, so it works with vanilla containers only (`:104`, `:124`).
- *Tunnel Digger.* While drilling it adds `drillSpeed / 20` to a power pool each tick (capped at 1), then mines random blocks from the 3 by 4 by 3 drill box until the pool is spent. The cost of each block is its destroy speed (`TunnelDigger.java:66-101`). Controls go to the server only when they change (`:149-155`).

**Lesson for Nublar.**
- *Controls on change.* The Tunnel Digger sends its control state only on a change. This is the same rule as the survey's S12 and B22.2: one packet when the state changes, not one per tick.
- *Idle search cooldown.* A 60-tick cooldown on an empty search is the same idea as the survey's S5 and B17 need-cooldown.
- *Path cache by endpoints, 256 entries, 5 minutes.* A candidate for the driverless tour (B22) *only if* the tour does not follow a laid track. The lead decided the tour vehicle follows a player-laid track with stops, so a path search is not needed.
- *Work budget per tick.* The drill's "power pool spent on random blocks" is a way to cap block-breaking work per tick. It fits a post-Beta drill extractor for amber ("drill extractor later", design decisions module 1), not the Beta processor.

**Compatibility duty.** None.

**Platform overlap.** Vanilla `PathFinder`, `PathNavigation`. Immersive Aircraft supplies the vehicle base. Nublar's vehicles (B22) use their own base.

**Verdict.** Not relevant (Beta). Pattern source for B22 control packets.

---

## Part E: Cross-cutting

### E1. How the networks stay cheap: side by side

| Aspect | Mekanism | IE | RS2 | Powah | Nublar today `[v]` | Nublar target |
| --- | --- | --- | --- | --- | --- | --- |
| Network object | One per connected group; energy buffer on the network | `LocalWireNetwork` per group, in one `GlobalWireNetwork` | One `Network` per group; nodes hold state | None; lazy cable list | Per post, per block entity | One `PowerNetwork` per group (B14) |
| Does the cable or post tick? | Only a join or refresh flag | No tick method on connectors | Node `doWork` early-outs when inactive | Cable has no energy and no tick | Every post, every tick (S7) | No tick on posts. Events only |
| Per-tick network work | O(networks) plus emit to cached acceptors | O(sources x sinks x path), paths cached | O(nodes) cheap extract calls | O(generators x cables) per push `[inf]` | 10 FE drain per post per tick | O(networks) |
| Change cost | Invalidate network, flood-fill orphans next tick, coalesced in sets | `reset()` caches; merge or split local net | Pivot BFS on add and remove | Drop `net` to null on neighbours; BFS when needed | Recompute rotation and lists | Merge on connect; invalidate and reflood on break |
| Persisted | Buffer shares on cables | Whole graph in level `SavedData` | Nothing; data on block entities | Energy on block entities | Energy and connections on posts | Same as today (on posts) |
| Chunk unload | Ticket-level event notifies cables in the changed chunk | Proxy connectors keep the graph | Nodes leave on unload | `removeChunk` clears the cable net | Not handled | Open question 2 |
| Client sync | Network scale when it changes | One message per wire change, plus on chunk watch | Watchers get deltas | `sync(delay)` coalescing | Full block-entity NBT every tick (S8) | Events and small payloads |
| Rendering | Block models | Section geometry (baked) | Block models | Block models | BER per post (C5) | See B4 |
| Tests | GameTests incl. chunk reload | Unit tests for wire network and catenary (`src/test`) `[v]` | Unit tests per module (`refinedstorage-network-test`) `[v]` | None seen | None | GameTests in common (B25.8) |

### E2. Do the surveyed machines sleep?

No. None of the four mods removes idle machines from the ticker list. Mekanism machines run `onUpdateServer` every tick. RS2 nodes call `doWork`, which returns at once when inactive. Powah blocks tick and return -1 from `postTick` when nothing changed. IE multiblocks tick only the master, and a redstone control component can stop the logic `[v]` (`RedstoneControl.java`). The saving is that a tick with nothing to do is a few comparisons, and no packet leaves it.

Lesson: Nublar's rule "idle machines draw zero" (design decisions, module 6) is a power rule, not a tick rule. Write the tick as: read cached state; return if idle and nothing marked dirty; otherwise advance progress. Do not try to unregister block entity tickers. Re-enabling them needs reliable wake-up hooks (hopper insert, neighbour change, power return), and a missed hook leaves a dead machine `[inf]`. The survey's B25.9 (slower idle work far from players) is the right tool for far machines.

### E3. A debounce and sync toolbox for block-entity state

| Tool | Where it is used | Use it for in Nublar | Platform note |
| --- | --- | --- | --- |
| Menu data only while a menu is open, dirty-diffed | Mekanism `MekanismContainer.broadcastChanges`; IE `GenericContainerData` | Progress, energy, issue mask, tier look in the GUI | Vanilla `ContainerData` covers small ints |
| Blockstate or BE flag change on the working edge | Mekanism `setActive` (`TileEntityMekanism.java:1396-1415`) | Machine "on" look: lid, light, sound | A blockstate change re-meshes a chunk section. Prefer a BE flag for GeckoLib-only visuals |
| Turn-off delay | Mekanism `blockDeactivationDelay`, default 60 ticks (`GeneralConfig.java:126-127`; `TileEntityMekanism.java:624-632`) | A machine that works in short bursts (feeder, incubator turn) | None |
| Coalesced `sync(delay)` | Powah (`PowahBaseTickingBlockEntity.java:67-73`) | Generator burn flag, any value the player sees in the world | None |
| Save data different from sync data | IE `IMultiblockState`; Powah `writeStorable` and `writeSync`; Mekanism reduced update tag | Every Nublar machine block entity (S8 root fix) | Vanilla `getUpdateTag` vs `saveAdditional` |
| Batched block update | IE `ServerChunkCache.blockChanged(pos)` (`MultiblockContext.java:62-67`) | One update per change, merged by vanilla | Vanilla |
| Debounced output change | RS2 Detector 1 s (`CHANGELOG.md`) | Comparator output on a machine that flickers | None |

Mekanism's turn-off delay has a hidden benefit for Sodium and Embeddium: a flicker on a block-state property causes a chunk-section rebuild each time. A 60-tick delay removes most rebuilds for bursty machines `[inf]`.

### E4. Outage, brownout and the control room: what the mods suggest

**In game (target).** The generator runs out of fuel. The fence lights and sparks go out. Machine screens show "No power". Animals that tested the fence earlier now push on it. The player refuels and sees the lines come back, section by section.

Parts the surveyed mods supply:
- *One event for a power change.* Mekanism's network flags `needsUpdate` once (`DynamicBufferedNetwork.java:36-43`). RS2 flips the active flag per node once. Nublar's network should publish `powered(true|false)` once, and sections, machines and signs subscribe to it. The survey's B14.3 (one powered flag per section) is the receiving side.
- *Brownout.* Mekanism has `NOT_ENOUGH_ENERGY_REDUCED_RATE`, a state where a machine runs slower when energy is low (`CachedRecipe.java:224`, `:268`) `[v]`. The lead said "no power, no progress". Keep it binary. Mention only because the data exists if the lead wants a slow-down.
- *Shock follows the grid.* IE's shock uses delivered energy and drains it (B3). That is the strongest mod example of outage physics.
- *A meter.* IE's Current Transformer gives a right-click reading and a comparator signal 0 to 15 (`current_transformer.txt` `[doc]`). A comparator output on the generator (stored fraction) costs one `getAnalogOutputSignal` and needs a neighbour update only when the 0 to 15 value changes (plan B12.4 pattern). It lets a player build an alarm with vanilla redstone.
- *Storm.* IE's Lightning Rod and Immersive Thunder show how weather ties to power and sound. Not in the plan.
- *Control room.* IE's Machine Interface, Redstone Probe and 16-channel redstone wire are control-room tools. The lead removed the tablet from Beta. The cheapest Beta control room is vanilla: comparators on generators and machines, a switch block per paddock, redstone lamps. Computer access (CC:Tweaked peripherals) is the survey topic of another note. Mekanism's pattern for it is annotation-driven methods on the block entity (`@WrappingComputerMethod` on `TileEntityFactory`) `[v]`: no hard dependency, methods are found by reflection when the computer mod is present.

---

## Closing table

Effort: S under 100 lines, M one or two classes, L new subsystem.

| Technique | Source | Nublar module | Verdict | Effort | Expected effect |
| --- | --- | --- | --- | --- | --- |
| One in-memory power network per group; buffer and demand on the network; merge on connect, invalidate and reflood on break | Mekanism `DynamicNetwork`, `TransmitterNetworkRegistry`; RS2 `NetworkBuilderImpl` | B14, B15 | Adapt | L | Per-tick power cost O(networks), not O(posts). 60-post drain from 60 calls to 1 (estimate: 0.05 to 0.15 ms saved; no allocation) |
| Do not persist the graph; store data on block entities | RS2 ADR 006 | B14, B15 | Adopt | S | Fewer corrupt-state bugs; no sanitizer code |
| Posts, pylons and connectors run no tick | IE `ImmersiveConnectableBlockEntity`; Mekanism `TileEntityTransmitter` | B14, B15 | Adopt | S | Removes S7 |
| Queued connector loading, one batch per tick | IE `GlobalWireNetwork.processQueuedLoads` | B15 | Adapt | S | No merge storm on chunk load |
| Shock damage follows delivered energy; shock drains the grid | IE `WireDamageHandler` | B15 | Adapt (needs lead decision) | M | Outage gameplay from physics; a pushing animal can brown out a weak park |
| Per-tier wire config block: max span, rate, damage radius, damage factor | IE `IEServerConfig` wires | B14, B15 | Adopt | S | Tuning in one table per tier |
| Wire message per connection change; batch on chunk watch; health in bands | IE `WireSyncManager`, `MessageWireSync` | B15 | Adopt | M | About 20 bytes per connection event; no per-post update tag |
| Bake wires into chunk sections (NeoForge) | IE `ConnectionRenderer` with `AddSectionGeometryEvent`; Sodium NeoForge hook | B15, C5 | Adapt (NeoForge only) | L | Zero per-frame wire cost; about 256 static vertices per wire (derived) |
| Light wire BER with cached vertices, or block-model quads (Fabric) | Fabric API `RenderDataBlockEntity`; survey C5 | B15, P2 | Adapt | M | No per-frame allocation; wire visible when the post is culled needs a render box |
| Closed-form catenary cached per connection | IE `Connection.makeCatenaryData` | B15 | Adopt | S | Replaces stored 39-float arrays |
| Chip tier formula: ticks = base x M^(-n/m), draw = base x M^(2n/m) | Mekanism `UpgradeUtils` | B12, B14 | Adapt | S | One config value `M` states how much more a faster chip costs |
| Cache computed tier numbers on the block entity at change time | Mekanism `recalculateUpgrades` | B12 | Adopt | S | No config or tag read per tick |
| `MachineIssue` set, synced as one int, shown as a warning tab | Mekanism `RecipeError`, `ErrorTracker`, `GuiWarningTab` | B12 | Adopt | M | Player sees why a machine or incubator stalled |
| Menu data only while a menu is open | Mekanism, IE | B12 | Adopt | S | Already in the survey (B12.1). Confirmed by two more mods |
| Turn-off delay (default 60 ticks) on the active flag | Mekanism `blockDeactivationDelay` | B12 | Adopt | S | Fewer chunk-section rebuilds for bursty machines (estimate) |
| Coalesced `sync(delay)` for world-visible state | Powah | B12, B14 | Adopt | S | One packet per window |
| Save data and sync data written separately | IE `IMultiblockState`; Powah; Mekanism | B12, B14 | Adopt | S | Root fix of S8 |
| Block-state part index; master lookup; no block entity on parts; block-only capability registration | Vanilla doors; `registerBlock` / `registerForBlocks` | B12 | Adapt | M | No ticker, save or sync on parts |
| Dummy parts get no ticker | IE `MultiblockPartBlock.getTicker` | B12 | Adopt | S | Same as above if parts keep a block entity |
| Cached render box over the whole machine | IE `MultiblockBEHelperMaster.renderBox` | B12 | Adopt | S | Correct culling for tall machines |
| Staged self-building placement for the creative item | Powah `Builder` | B12 | Adapt | S | No big one-tick change |
| Adaptive poll rate (5 to 40 ticks, step on change) | RS2 `ExternalStorageWorkRate` | B20 | Adopt | S | Idle feeder poll up to 8 times cheaper (estimate) |
| Deterministic components on stackable items | RS2 `ItemResource` | B02, B04, B05 | Adopt (rule) | S | Fossils and tubes stack and search in a Grid |
| Item capability on machines, by side | RS2 External Storage; platform | B05, B20 | Compat | S | Network logistics with no extra code |
| Energy capability (per-side insert/extract rules) | Mekanism, Powah, IE; NeoForge `EnergyHandler`; Balm | B14 | Compat | S | Third-party cables on NeoForge power Nublar blocks |
| Energy per fuel tick as one constant | Powah `DefaultEnergies` | B14 | Adapt | S | One number to tune the balance rule |
| Strided world checks (`ticks % 40`) | Powah solar | B14, B25 | Adopt | S | Cheap sky or coolant checks |
| GameTest: chunk reload keeps the network | Mekanism `TransmitterNetworkTest` | B14, B25 | Adopt | M | Catches the unload gap |
| Controls sent on change only | Immersive Machinery `TunnelDigger` | B22 | Adopt | S | No per-tick control packets |
| Path cache by endpoints | Immersive Machinery `PilotNavigator` | B22 | Avoid (track-following tour) | M | None |
| Factory lanes, six-face side config, seven upgrade cards, seven tiers | Mekanism, Powah | B12 | Avoid | — | Out of design |
| Path/loss/transformer layer | IE `EnergyTransferHandler` | B14 | Avoid (lossless lines) | — | None |
| Cable walk on every push | Powah `CableBlockEntity` | B14 | Avoid | — | Would cost O(cables x generators) |
| Central saved wire graph with sanitizer | IE | B15 | Avoid | — | State drift |
| CSL | Terrarium | P2 | Avoid | — | No 26.x |
| Thunder sound bands | Immersive Thunder | — | Not relevant (idea after Beta) | S | Atmosphere |

---

## Open questions

These are real gaps in the design or the plan. None concerns licences or riding.

1. **Which wire renderer on each loader?** NeoForge can bake wires into chunk sections and Sodium supports it. Fabric cannot (Sodium Fabric returns no appenders; Fabric API has no hook). Options for Fabric are a light block-entity renderer with cached vertices, or wire quads in the post's block model via `RenderDataBlockEntity`. The second option has an unknown behaviour at section borders. Someone must prototype both on Fabric with Sodium and Iris before B15 fixes the renderer. Plan P2 should record that the wire renderer is loader-specific.
2. **What happens to power when the generator's chunk is not loaded?** A generator in an unloaded chunk burns no fuel, so loaded fences can go dark. Mekanism and IE have chunk-loader features. The plan has none. Choices: force-load the generator chunks (costly for big parks), keep the generator in the paddock area by rule, or compute power analytically from stored fuel and an "as of" game tick so an unloaded generator still answers "powered" correctly `[inf]`. The lead should pick one before B14.
3. **Should shock strength follow network charge, and should a shock drain the generator?** IE does both. It adds outage physics and uses the large generator. It changes the settled `fences.damage.<tier>` into a number multiplied by a 0 to 1 factor. The lead has not decided.
4. **Does the network have a buffer, and how long do fences stay live after fuel ends?** Backup generators are not in Beta, and no battery block is planned. Mekanism's network sums cable capacity as a buffer. Without any buffer, an outage is instant. With a small buffer per post or conduit (the post holds 350 FE today `[v]`), it fades over seconds. This changes how an outage feels. The lead's power-feel answer covered the starter park, not the outage.
5. **Is there a Balm build for 26.2?** The Balm clone has one branch, `26.3` (tag `v26.3.0.3`). NeoForge's clone is at `26.2.x`. Mekanism also targets 26.3. If the project stays on 26.2, a Balm 26.2 release must exist. If not, B14's energy bridge needs a plan (Balm's energy is `int` and its simulate flag; NeoForge 26 uses transactions). Check before P2 freezes dependencies.
6. **Do Nublar block entities expose the energy capability on the sides that third-party cables should use?** Pylon and conduit blocks are Nublar's own network. A post next to a Mekanism cable on NeoForge is a path that can drain power into or out of a fence. The plan should state which blocks accept external insertion and which only accept the Nublar network.
7. **How should the master find its parts after a world edit?** With block-state part indices and no block entity on parts (finding 8), a missing or replaced part is visible from the block states. A part-by-part assembly check should run on part placement and on neighbour change only. The plan does not say what a machine does if a part is replaced by a command or a structure paste.
