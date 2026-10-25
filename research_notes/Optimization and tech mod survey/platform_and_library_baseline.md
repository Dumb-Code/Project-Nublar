# Platform and library baseline: what exists before Nublar builds anything

Date: 2026-10-10. Status: research note for the project lead and for the other researchers in this survey. It changes no code and no plan module.

**Purpose.** Other notes judge optimisation mods and tech mods. This note gives the yardstick they use: what NeoForge, Fabric API, vanilla Minecraft, Balm, GeckoLib 5 and SmartBrainLib 2 already supply on the 26.2 line. If the platform covers a need, the verdict in every other note is "use the platform", not "copy the mod".

**Labels.** `[v]` verified in source at the named commit (path and lines). `[doc]` README, wiki, changelog, store page or docs site. `[inf]` inference. A performance number without `[v]` is an estimate. Vanilla Minecraft source is not in the clones. Statements about vanilla classes come from NeoForge patch files (which quote vanilla lines) or from other mods' source. They are marked.

**Names.** "The plan" is `docs/project-plan.md`. "The earlier survey" is `reports/Reference mod survey for Beta.md`. This note does not repeat it.

## 0. Repos read

All clones live in the shared scratch folder (`...\scratchpad\clones\`). Some were cloned first by other researchers; they were read only. Nothing was built or run.

| Repo | Branch | Commit | Commit date | Licence (repo fact) | Loaders | MC | Maintenance |
| --- | --- | --- | --- | --- | --- | --- | --- |
| neoforged/NeoForge | `26.2.x` | `3ce4390` | 2026-10-10 | LGPL-2.1 (file headers) | NeoForge | 26.2 (tags `26.2.0`, `26.2.0-stable`; `26.3.0` also exists) | active, Java 25 `[v]` `gradle.properties` |
| FabricMC/fabric-api | `26.2` | `6a8ed6b` | 2026-09-18 | Apache-2.0 `[inf]` from repo `LICENSE` | Fabric | 26.2, version `0.161.0` (`gradle.properties`) | active; branches `26.3`, `26.4` exist; newest release `0.162.2+26.4` (2026-10-06) `[v]` via `gh api` |
| TechReborn/Energy (Team Reborn Energy) | `master` | `fdc4144` | 2026-01-01 | MIT (GitHub) | Fabric | `5.0.0` is "26.1 or later" `[doc]` README | stable, quiet; Maven lists `5.0.0` as newest |
| TwelveIterationMods/Balm | `26.3` read; `26.2` branch exists, `version = 26.2.0.9` | `cd5d2c3` | 2026-10-02 | All Rights Reserved (file `gradle.properties`) | NeoForge, Fabric | 26.2, 26.3 | active, Java 25 |
| terrarium-earth/Common-Storage-Lib (renamed Botarium) | `1.21.x` | `69dd639` | 2026-07-12 | MIT | NeoForge, Fabric | 1.21.1 only (`gradle.properties`: `minecraftVersion=1.21.1`, `version=0.0.10`) | no 26.x branch `[v]` (branch list: 1.20.x, 1.20.1, 1.20.4, 1.21.x) |
| architectury/architectury-api | `26.2` | `050efc3` | 2026-10-09 | LGPL-3.0 | NeoForge, Fabric | 26.2, 26.3 branches | active |
| Fuzss/forgeconfigapiport-fabric (Forge Config API Port) | n/a | n/a | n/a | MPL-2.0 | Fabric, NeoForge | 26.3.1 primary, 26.2.1 "bugfixes only", 26.1.5 maintained `[doc]` README | active |
| bernie-g/geckolib | `main`; branches `26.1`, `26.2` | n/a | pushed 2026-09-27 | MIT | NeoForge, Fabric | 26.2 branch exists | active |
| Tslat/SmartBrainLib | `Main`; branches `26.1.2`, `26.2` | n/a | pushed 2026-10-05 | MPL-2.0 | NeoForge, Fabric | 26.2 branch exists | active |
| mekanism/Mekanism | `26.3` (clone), `26.2` branch exists | `feb1959` | 2026-10-09 | MIT | NeoForge | 26.3 | active |
| owmii/Powah (clone) | `26.1` | `4274274` | 2026-06-09 | LGPL-3.0 (GitHub, old branch) | NeoForge | 26.1.2 | active on 26.1 |
| mrh0/createaddition (Create Crafts and Additions) | `1.21.1` | `1c81fab` | 2026-10-01 | NOASSERTION | NeoForge | 1.21.1 only | active on 1.21.1; no 26.x branch read |
| Architectury, FCAP, GeckoLib, SBL | see above | | | | | | read by `gh api` branch lists only, not cloned |

**Newest versions.** The lead's target is 26.2. NeoForge 26.3.0 and Fabric API for 26.3 and 26.4 already exist. Everything in sections 1 to 7 is read on 26.2 unless stated. Mekanism and Balm were read on 26.3. The relevant APIs match (Balm 26.3 builds against the same `transfer` package as 26.2 `[inf]`).

**Single most important finding.** Between 1.20.1 and 26.2 NeoForge replaced every storage interface Nublar's Forge side would have used (`IEnergyStorage`, `IItemHandler`, `IFluidHandler`) with one transaction-based `ResourceHandler` / `EnergyHandler` family, and kept the old ones only as `@Deprecated(forRemoval = true)` shims `[v]`. Fabric never had a built-in energy API and still has none `[v]`. So the cross-loader energy question reduces to "Balm's thin interface, or two small loader adapters". It does not need Botarium or Common Storage Lib.

## 1. Energy

### 1.1 NeoForge 26.2

**In game.** Machines, cables and batteries from any NeoForge mod connect without extra work. Energy moves as whole numbers of "FE".

**Mechanism `[v]`.**
- The capability set is `Capabilities.Energy.BLOCK`, `.ENTITY`, `.ITEM` (`src/main/java/net/neoforged/neoforge/capabilities/Capabilities.java:21-24`). The block one is `BlockCapability<EnergyHandler, @Nullable Direction>`. The item one takes an `ItemAccess` context (a stack plus how to swap it).
- The old `Capabilities.EnergyStorage.BLOCK` (used by Create Crafts and Additions on 1.21.1, `AlternatorBlockEntity.java:56`) does not exist in `Capabilities.java` on 26.2. The file has only the nested classes `Energy`, `Fluid`, `Item`.
- `EnergyHandler` (`transfer/energy/EnergyHandler.java`) has `getAmountAsLong`, `getCapacityAsLong`, `int insert(int amount, TransactionContext)` and `int extract(int amount, TransactionContext)` (lines 39, 75, 110, 124). Per-call amounts are `int`. Stored amounts and capacity are `long`.
- Ready implementations: `SimpleEnergyHandler(capacity, maxInsert, maxExtract, energy)` with `ValueIOSerializable` (`SimpleEnergyHandler.java:23-83`), `ItemAccessEnergyHandler` (replaces `ComponentEnergyStorage`), `EmptyEnergyHandler`, `InfiniteEnergyHandler`, `LimitingEnergyHandler`, `VoidingEnergyHandler`, `EnergyHandlerUtil.isFull / getRedstoneSignalFromEnergyHandler` (`EnergyHandlerUtil.java:32,43`).
- Transactions: `transfer/transaction/Transaction`, `TransactionContext`, `SnapshotJournal` (same model as Fabric Transfer API). Writes must go through a journal so an outer transaction can abort.
- Legacy shims: `energy/IEnergyStorage.java:22` is `@Deprecated(since = "1.21.9", forRemoval = true)` and offers `IEnergyStorage.of(EnergyHandler)`. `EnergyStorage`, `EmptyEnergyStorage`, `ComponentEnergyStorage` carry the same deprecation `[v]`.
- Caching neighbour lookups: `BlockCapabilityCache.create(capability, serverLevel, pos, context, validCheck, invalidateListener)` (`BlockCapabilityCache.java:31-47`). The cache is invalidated by `Level#invalidateCapabilities(pos)`. Powah's cable uses six such caches (`CableBlockEntity.java:53,200`, `Powah` `26.1` `4274274`). Create Crafts and Additions (1.21.1) does the same with `EnumMap<Direction, BlockCapabilityCache<IEnergyStorage, Direction>>` (`AlternatorBlockEntity.java:46,56,153`).

**Lesson.** Neighbour push should use one `BlockCapabilityCache` per connected side or, for fences, a precomputed network object (section 8, item E3). A fresh `level.getCapability(...)` call per tick per wire is the anti-pattern.

### 1.2 Fabric 26.2

**In game.** Fabric has no shared power unit. Tech mods agree on Team Reborn Energy (TRE), so "E" from TechReborn, Modern Industrialization, Ad Astra and others connects only if all use TRE.

**Mechanism.**
- Fabric API 26.2 has no energy API `[v]`: a case-insensitive grep for "Energy" in the whole `fabric-api` 26.2 tree matches only `fabric-biome-api-v1` (unrelated `BiomeModificationContext`) and a transfer unit-test file. The transfer module contains item, fluid, storage, transaction and context packages only.
- Fabric Transfer API is still the variant-based design: `Storage<T>` (`storage/Storage.java:64`), `ItemStorage.SIDED` as `BlockApiLookup<Storage<ItemVariant>, @Nullable Direction>` (`item/ItemStorage.java:85`), `ItemStorage.ITEM` as `ItemApiLookup<Storage<ItemVariant>, ContainerItemContext>` (`:95`), plus a new `ContainerStorage extends SlottedStorage<ItemVariant>` that exposes a vanilla container (`item/ContainerStorage.java:50`). Vanilla containers, composters, shulker boxes and bundles are registered as fallbacks (`ItemStorage.java:103-165`).
- Fabric's lookup base is `fabric-api-lookup-api-v1` (`BlockApiLookup`, `BlockApiCache`, `EntityApiLookup`, `ItemApiLookup`). This is the Fabric twin of NeoForge's `BlockCapability` and `BlockCapabilityCache`.
- TRE: README conventions (push-based, "1 coal = 4000", "machines and wires should NOT pull") and `5.x` is the 26.1-or-later line `[doc]`. The API root is `team.reborn.energy.api.EnergyStorage` with a `SIDED` lookup (used by Balm at `RebornEnergy.java:19`). `SimpleEnergyStorage` is the stock implementation `[doc]`.

### 1.3 Balm

**In game.** Nothing visible. One block entity class works with FE on NeoForge and with TRE on Fabric.

**Mechanism `[v]` (Balm `26.3`, `cd5d2c3`).**
- `platform/energy/EnergyStorage.java:3-18` is a small int interface: `fill(max, simulate)`, `drain(max, simulate)`, `getEnergy`, `setEnergy`, `getCapacity`, `canDrain`, `canFill`. `DefaultEnergyStorage` is the stock class.
- A block entity opts in by implementing `BalmEnergyStorageProvider` (`getEnergyStorage()` and a sided default, `BalmEnergyStorageProvider.java:6-12`). A fallback provider in `CommonCapabilitiesImpl.java:42-47` registers it under `CommonCapabilities.ENERGY_STORAGE` (`CommonCapabilities.java:15-16`).
- NeoForge bridge: `NeoForgeEnergyStorage implements EnergyHandler` (`neoforge/.../NeoForgeEnergyStorage.java:8-47`). It applies `fill/drain` directly and keeps a `SnapshotJournal<Integer>` so an aborted outer transaction restores the old value. It does not honour a "simulate only" request inside the transaction; it relies on the journal for rollback. `NeoForgeBalm.java:139-146` wires the provider into `Capabilities.Energy.BLOCK` `[v]` (line numbers of the block-entity registration).
- Fabric bridge: `RebornEnergy` registers an `EnergyStorage.SIDED` fallback that wraps a `BalmEnergyStorageProvider` into a TRE storage (`fabric/.../RebornEnergy.java:19-37`). It loads only if the mod `team_reborn_energy` is present: `Balm.initializeIfLoaded("team_reborn_energy", ...)` at `FabricBalm.java:112`, and TRE is `compileOnly` in `fabric/dependencies.gradle:10`. Without TRE on Fabric, Balm exposes no energy to the outside. It does not bundle TRE.
- Balm fluids are simpler than either loader: `FluidTank` uses `Fluid` plus `int` amounts per slot, no data components, no variants (`platform/fluid/FluidTank.java:5-27`; provider pattern the same as energy, `CommonCapabilitiesImpl.java:32-41`). That is enough for a mud or water tank. It is not enough if a fluid ever needs components.
- Balm item access: `CommonCapabilities.CONTAINER` is a vanilla `Container` per side (`CommonCapabilities.java:12`). Both loader entry points expose it to hoppers and pipes: `NeoForgeBalm.java:84`, `FabricBalm.java:74`. There is no Balm equivalent of `ResourceHandler`/`Storage`; a block entity that is a `Container` is the Balm item-transfer story.

**Overlap verdict.** Balm covers energy exposure for a block entity on both loaders with one 18-line interface. It does not cover item components, fluid variants, transactions across several mods, or networks of wires.

### 1.4 How other mods bridge units

- **Mekanism (26.3 clone, MIT).** Registers one `MultiTypeCapability<EnergyHandler> ENERGY = new MultiTypeCapability<>(Energy.BLOCK, Energy.ITEM, Energy.ENTITY)` (`mekanism/common/capabilities/Capabilities.java:46`) and a proxy that forwards `insert/extract` to its own `IEnergyContainer` (`ProxyEnergyHandler.java`). Mekanism keeps its own display unit (Joules) in `UnitDisplayUtils` and a config for the FE ratio `[inf]` (not read in depth). Lesson: expose the platform handler, keep any custom display unit as a display-only conversion.
- **Powah (26.1).** Reads and writes `EnergyHandler` directly (`CableBlockEntity.java:16,53`, `BatteryItem.java:58` uses `ItemAccess.forStack(...).getCapability(Capabilities.Energy.ITEM)`). No wrapper library.
- **Create Crafts and Additions (1.21.1 only).** Uses `IEnergyStorage` and `BlockCapabilityCache` (`AlternatorBlockEntity.java:36-56,153`). No 26.x branch was read. It will have to move to `EnergyHandler`; its design (a power network of connectors) is the closest tech precedent to fence wire, and the other notes cover it.

### 1.5 Common Storage Lib / Botarium / Architectury

- Common Storage Lib is the renamed Botarium repository (`gh api` returns `full_name = terrarium-earth/Common-Storage-Lib` for both URLs). It has `energy`, `fluid`, `item`, `heat` packages and `storage/base/CommonStorage`, `ValueStorage` (`core/common/src/main/java/earth/terrarium/common_storage_lib/...`). Its newest branch is `1.21.x` with `minecraftVersion=1.21.1`, version `0.0.10`. No 26.x branch exists. **Verdict: Avoid** for the port; it cannot be a 26.2 dependency today. Nublar already carries Botarium `2.3.4` for 1.20.1 (section 9).
- Architectury API `26.2` has `registry/registries`, `networking/NetworkManager`, `fluid/FluidStack`, `event`, `hooks`; no energy or item-transfer package `[v]` (grep for "energy" in `common/src/main` finds only a biome `SpawnProperties` hit).
- Common Storage Lib and Botarium are not widely adopted on 26.x. No other cross-loader energy library with a 26.x branch was found. **Verdict: Balm for exposure, own code for the graph.**

## 2. Item and fluid transfer

**In game.** Hoppers, pipes and sorting systems move items into and out of Nublar machines (feeders, incubator, fossil processor, sequencer, egg printer). A Refined Storage cable or Create belt works on them without custom code.

### 2.1 NeoForge 26.2 `[v]`

- `Capabilities.Item.BLOCK` is `BlockCapability<ResourceHandler<ItemResource>, @Nullable Direction>` (`Capabilities.java:38`). Fluids use `ResourceHandler<FluidResource>` (`:30`). There is also `Item.ENTITY_AUTOMATION` (sided, for entities that expose automation access) and `Item.ENTITY` (void context) (`:43,48`).
- `ResourceHandler<T extends Resource>` (`transfer/ResourceHandler.java`) is index-based: `size()`, `getResource(i)`, `getAmountAsLong(i)`, `insert`, `extract`, with a `TransactionContext` on every mutation. `ResourceHandlerUtil` gives `move`, `moveStacking`, `insertStacking`, `extractFirst`, `isEmpty`, `isFull`, `getRedstoneSignalFromResourceHandler` (`ResourceHandlerUtil.java:36-369`).
- Ready slots: `ItemStacksResourceHandler`, `ItemStackResourceHandler`, `FluidStacksResourceHandler`, `ResourceHandlerSlot` (for menus), `BundleItemHandler`, `PlayerInventoryWrapper`, `VanillaContainerWrapper`, `WorldlyContainerWrapper`.
- Vanilla blocks: `CapabilityHooks.java:92-113` registers `WorldlyContainerWrapper` for blast furnace, brewing stand, furnace, smoker, shulker box and `VanillaContainerWrapper.of(container)` for barrel, chiseled bookshelf, dispenser, dropper, hopper, jukebox, crafter, decorated pot, shelf. Line 63: `event.setProxyable(Capabilities.Item.BLOCK)`. `VanillaInventoryCodeHooks.extractHook/insert` lets vanilla hoppers talk to handlers (`VanillaInventoryCodeHooks.java:32`).
- Legacy: `items/IItemHandler`, `ItemStackHandler`, `SlotItemHandler` still exist; the energy ones are deprecated since 1.21.9 `[v]`; item ones were not checked line by line `[inf]` same treatment.
- Capability caching: `BlockCapabilityCache` (see 1.1).

### 2.2 Fabric 26.2 `[v]`

- `Storage<ItemVariant>` / `Storage<FluidVariant>`, `SlottedStorage`, `Transaction`, `StorageUtil` (`fabric-transfer-api-v1/.../storage/`). `ItemStorage.SIDED`, `FluidStorage.SIDED`. Vanilla `Container`/`WorldlyContainer` are auto-wrapped as fallbacks (`ItemStorage.java:106-120`). `ContainerStorage` exposes a vanilla `Container` as slotted storage (`ContainerStorage.java:42-50`).
- Both loaders now use transactions with snapshot journals. Fabric calls the unit a "variant"; NeoForge calls it a `Resource`. The shapes are close `[inf]`.

### 2.3 Hoppers and vanilla

- Vanilla hoppers read a block entity that implements `Container` / `WorldlyContainer` directly (`WorldlyContainer.getSlotsForFace/canPlaceItemThroughFace/canTakeItemThroughFace`). Nublar's `SyncingContainerBlockEntity extends BaseContainerBlockEntity` (`.../block/api/SyncingContainerBlockEntity.java:14`) already does this `[v]`. Both loaders wrap it, so hopper and pipe compat comes free. If the feeder, incubator and processor keep being `Container`s, Nublar writes no capability code for items. **Verdict: use the platform.**
- Gap: a `Container` with 3 slots (the feeder uses `foodSlot1..3`, `DinosaurFeederBlockEntity.java:44-46`; slot switch at `:138-140` skips index 2 and uses `case 3`) needs a `WorldlyContainer` per-face table for automation to insert food only. That is Nublar code, a few lines.

### 2.4 Verdict table (section 2)

| Need | Platform supplies | Nublar writes |
| --- | --- | --- |
| Items in and out of machines | vanilla `Container` / `WorldlyContainer`, wrapped by both loaders `[v]` | per-face slot rules |
| Item-holding items (syringe, test tube stack) | data components (section 3) | none |
| Fluids in machines | NeoForge `ResourceHandler<FluidResource>`; Fabric `Storage<FluidVariant>`; Balm `FluidTank` | pick one: Balm for a simple tank |
| Energy | section 1 | graph for fences |
| Cross-mod item networks (Refined Storage, AE2) | capabilities / lookups only | nothing; they find `Container` |

## 3. Data components, attachments, networking, config, registries, data, GameTest

### 3.1 Item data: data components `[doc]` plus `[v]`

- NeoForge docs for 26.1 state that attachments are not supported on item stacks and have been superseded by vanilla data components (`docs.neoforged.net/docs/datastorage/attachments`, fetched 2026-10-10). Nublar's `getOrCreateTag().put("DNAData", ...)` pattern (list in section 9, items C1 to C3) has no 26.2 equivalent. It must become a `DataComponentType<DNAData>` with a `Codec` and a `StreamCodec`.
- NeoForge's own item energy uses a component-backed handler (`ItemAccessEnergyHandler`) `[v]`.

### 3.2 Attachments `[doc]`

- NeoForge targets: block entities, chunks, entities, levels (not stacks). Serialisation through `IAttachmentSerializer`, `ValueIOSerializable`, or a map codec. Sync via `AttachmentSyncHandler` or a `StreamCodec` (always sends the full attachment). Copy-on-death for entities only (docs, version 26.1).
- Fabric: `fabric-data-attachment-api-v1` with `AttachmentRegistry`, `AttachmentTarget`, `AttachmentSyncPredicate`, `GlobalAttachments`. The mixin list shows targets: block entity, chunk, entity, level, server (`fabric-data-attachment-api-v1/src/main/java/net/fabricmc/fabric/mixin/attachment/` `BlockEntityMixin`, `ChunkAccessMixin`, `EntityMixin`, `LevelMixin`, `MinecraftServerMixin`) `[v]`.
- Balm: `platform/attachment/BalmDataAttachmentTypeRegistrar` + `DataAttachmentLookup` wrap both `[v]` (directory listing).
- Use: per-dinosaur data that is not an entity field (genome record, park-membership flag), per-level registries (feeder index, fence networks). A level attachment or a `SavedData`-style store replaces the hand-written maps the earlier survey proposes.

### 3.3 Networking

- Both loaders: `CustomPacketPayload` + `StreamCodec`. NeoForge: `RegisterPayloadHandlersEvent` (`network/event/RegisterPayloadHandlersEvent.java`), `PacketDistributor`, config-phase tasks via `RegisterConfigurationTasksEvent`. Fabric: `PayloadTypeRegistry`, `ServerPlayNetworking`, `PlayerLookup`, `EntityTrackingEvents` (`fabric-networking-api-v1/.../networking/v1/`).
- Balm hides both: `BalmNetworking.registerClientboundPacket(Type, Class, StreamCodec<RegistryFriendlyByteBuf,T>, BiConsumer<Player,T>)` and `registerServerboundPacket` (`network/BalmNetworking.java:42,44`) `[v]`.
- Architectury `networking/NetworkManager` also exists on 26.2 `[v]` but Nublar is moving to Balm.
- Nublar today: the `commonnetwork` library from `mysticdrew:common-networking-common:1.0.3-1.20.1` (`common/build.gradle:27`; `NetworkInit.java:3,10-12`). This must go.

### 3.4 Config

- NeoForge: `ModConfigSpec` (`common/ModConfigSpec.java`). Create Crafts and Additions uses it directly (`CommonConfig.java:11,28-34`).
- Fabric: no config API in Fabric API. The ecosystem answer is Forge Config API Port (FCAP), which Nublar already depends on (`forge_config_api_port_version=8.0.0`; `common/build.gradle:22`; `fabric/build.gradle:16`). FCAP `26.2.1` is "bugfixes only" and `26.3.1` is primary `[doc]`.
- Balm: its own config schema (`platform/config/BalmConfig.java`, `schema/ConfigSchemaBuilder` and `Configured*` types, TOML files named `<namespace>-<path>.toml`, `getConfigFile` at `BalmConfig.java:37-40`) plus config screens `[v]`. "All-inclusive from networking to configs, no third party dependencies" `[doc]` README.
- Pick one. The lead has chosen Balm; then FCAP and toml4j can go (toml4j has no Java usages in `common/src`, `forge/src` or `fabric/src` `[v]` by grep; it is only declared in `common/build.gradle:23` and `fabric/build.gradle:18`).
- Server-synced config (park rules) is the only config type with a design cost; datapacks cover most tuning (3.5).

### 3.5 Registries and data-driven content

- NeoForge: `DeferredRegister`, `DeferredBlock`, `DeferredItem`, `DataPackRegistryEvent` (`registries/DataPackRegistryEvent.java`) for a datapack registry that is synced to clients. Fabric has the equivalent through the registry-sync and dynamic registry hooks `[inf]`.
- Balm: `BalmRegistrar#createReloadableDynamicRegistry` (CHANGELOG first lines; Balm `26.3`) `[doc]`. Not read in source.
- Architectury `DeferredRegister` is used in 12 Nublar files (`rg DeferredRegister common/src` returns 12 files). The lead replaced Botarium with Balm; Balm's registrar can replace Architectury's `DeferredRegister` too `[inf]`, which would remove Architectury completely.
- Nublar's species data uses `SimpleJsonResourceReloadListener` x4 (`BehaviourDataReloadListener.java:19`, `DietReloadListener.java:22`, `FossilConfigReloadListener.java:23`, `GeneDataReloadListener.java:18`). A reload listener is not synced to clients and is not a registry. A datapack registry with a `Codec` gives: validation at load, client sync, tag support, `/reload`. **Adapt**: move species behaviour, diets, genes into dynamic registries. This is the "platform covers it" answer to data-driven species.

### 3.6 GameTest

- Vanilla `net.minecraft.gametest.framework.GameTest*`; NeoForge adds `RegisterGameTestsEvent` (`event/RegisterGameTestsEvent.java`) and a test framework (`docs/NEOGAMETESTS.md`, `docs/TESTFRAMEWORK.md`, `tests/` module); Fabric has `fabric-gametest-api-v1` and `fabric-client-gametest-api-v1` `[v]`.
- Nublar has `forge/.../gametest/HerdGameTests.java` using Forge's `GameTestHolder`. The lead runs tests from `runClient`, not a headless server (project memory). Both 26.2 loaders keep a test hook. Nothing else is needed.

## 4. Rendering hooks that matter for performance

**In game.** Large parks with many fence posts, feeders and dinosaurs need the renderer to skip off-screen block entities and entities.

### 4.1 Block entity render bounds

- NeoForge 26.2: `IBlockEntityRendererExtension<T>.getRenderBoundingBox(T)` defaults to `new AABB(blockPos)`; `AABB.INFINITE` forces always-visible (`src/client/java/net/neoforged/neoforge/client/extensions/IBlockEntityRendererExtension.java:10-22`). It lives on the renderer, not on the block entity. The 1.20.1 method `BlockEntity#getRenderBoundingBox` (Nublar `ProcessorBlockEntity.java:324`, `inflate(4)`) is gone.
- The dispatcher culls before extracting render state. `BlockEntityRenderDispatcher.tryExtractRenderState(..., Frustum frustum)` returns `null` when `!frustum.isVisible(renderer.getRenderBoundingBox(blockEntity))` (`patches/net/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher.java.patch:26`) `[v]`. An off-screen fence post therefore costs one box test and no state extraction. A too-large box costs the opposite: always extracted.
- Fabric 26.2: Fabric API has no `getRenderBoundingBox` (grep over `fabric-api` finds nothing) `[v]`. EntityCulling carries a copy of NeoForge's interface "allowing mods to have the same functionality" on Fabric (`BlockEntityRenderFabricExtension.java:5-14`), and a mod implements it (the file cites a CC: Tweaked commit). Vanilla's escape hatch on both loaders is `BlockEntityRenderer.shouldRenderOffScreen()`, which EntityCulling checks (`BlockEntityRenderDispatcherMixin.java:62,107`).
- Rule for Nublar `[inf]`: a fence-wire renderer returns the exact segment box, or the box of the wire span for the block entity, not a block-sized box and not `INFINITE`. A pole block entity that draws wires to a far post must use a union box or the wire pops out of view at the screen edge. This is the same trade-off as `ProcessorBlockEntity.getRenderBoundingBox` today (`inflate(4)`).

### 4.2 Entity culling box

- In 26.x the culling box moved to the renderer: Sodium calls `EntityRenderer#getBoundingBoxForCulling(entity, partialTicks)` through an invoker `sodium$getBoundingBoxForCulling` (`sodium/.../mixin/core/render/world/EntityRendererAccessor.java:11-12`; used at `SodiumWorldRenderer.java:462`) `[v]` (Sodium `dev`, `8aa723c`, 2026-09-30). Sodium skips section checks for entities above a volume cap (`MAX_ENTITY_CHECK_VOLUME`, lines 463-468) and always draws glowing or name-tagged entities (line 457).
- Rule: a dinosaur's culling box should cover the posed body (tail, neck) tightly, and must be cheap to compute. A 12-block-long sauropod above the volume cap is only frustum-tested.

### 4.3 Render-state extraction

- 26.x renders through an extract step. `EntityRenderer#extractRenderState(entity, state, partialTicks)` fills a state object that the render step reads (`patches/net/minecraft/client/renderer/entity/EntityRenderer.java.patch:21`); block entities do the same with `BlockEntityRenderState` (dispatcher patch above) `[v]`.
- Fabric exposes the extract step for mods: `RenderStateDataKey`, `FabricRenderState` (`fabric-rendering-v1/src/client/java/net/fabricmc/fabric/api/client/rendering/v1/`), `EntityRendererRegistry`, `BlockEntityRendererRegistry`, `LivingEntityFeatureRenderEvents`, `FeatureRendererRegistry` `[v]` (directory listing). That is the place to keep per-entity data (genome colour, pose) for the render thread without reading the live entity.
- GeckoLib 5 builds its entity and block-entity renderers on this model; the earlier survey already treats it as a library hook.

### 4.4 Model loading and FRAPI

- NeoForge 26.2: model package `neoforge/client/model/` with `UnbakedModelLoader`, `DynamicBlockStateModel`, `DelegateBlockStateModel`, `CompositeUnbakedModel`, `ExtendedUnbakedGeometry`, `StandardModelParameters`, `block`, `item`, `standalone` subpackages `[v]` (directory listing). Nublar's 1.20.1 fossil and test-tube loaders (`forge/.../client/model/fossil/FossilModelLoader.java`, `testtube/TestTubeModelLoader.java`) have a 26.x equivalent but must be re-written.
- Fabric 26.2: FRAPI is alive and extended: `fabric-renderer-api-v1` (client source set) with `mesh/QuadEmitter`, `Mesh`, `MutableMesh`, `model/FabricBlockStateModel`, `render/AltModelBlockRenderer`, `render/submit/ExtendedBlockModelSubmit`, `ExtendedItemSubmit`, `Renderer`; `fabric-renderer-indigo` is the reference renderer; `fabric-model-loading-api-v1` handles loading `[v]` (file lists). The 1.20.1 `ItemOverridesMixin`/`StackSensitiveItemOverrides` pattern (`fabric/.../mixin/ItemOverridesMixin.java`) has no 26.x use.
- Sodium/Embeddium/Iris compat: see the Sodium note. The platform contract is FRAPI on Fabric and the NeoForge model API; a mod that stays on those loads in Sodium without work. A mod that mixes into `BlockEntityRenderDispatcher` or vanilla `ModelBakery` breaks.

## 5. Entity tick and tracking

- Vanilla `EntityType.Builder` carries `clientTrackingRange` (chunks) and `updateInterval` (ticks). `ChunkMap#addEntity` uses `type.clientTrackingRange() * 16` blocks and skips when it is 0 (`patches/net/minecraft/server/level/ChunkMap.java.patch:97`) `[v]`. NeoForge adds overridable `trackingRangeSupplier`, `updateIntervalSupplier`, `trackDeltasSupplier` to the constructor (`patches/net/minecraft/world/entity/EntityType.java.patch:8-9,19`) `[v]`.
- Nublar sets `clientTrackingRange(10)` on the first two species (`EntityInit.java:30,31`) and relies on the default for the rest `[v]`. 10 chunks = 160 blocks. The default for the entity category differs; each dinosaur inside the range costs a movement packet at `updateInterval`. A 100-dinosaur park pays that per watching player. Set range by body size and species, not one value `[inf]`.
- Other numbers (vanilla defaults per category, `updateInterval` 3 for mobs, random-tick rate 3 per section per tick, `getTicker` returning null removes block-entity ticking, scheduled ticks via `ScheduledTick`/`LevelTicks`) are vanilla behaviour known from earlier versions. They were **not re-read in 26.2 source** `[inf]`. The block-entity ticker rule is visible in Nublar: `ElectricFencePostBlock.getTicker` returns a ticker whenever `pClientType == pServerType` (`ElectricFencePostBlock.java:300-305`), so every fence post pole runs a tick on both logical sides.
- `ServerLevel` ticks entities through the entity tick list; `Entity#tick`, the brain through SBL 2 after `customServerAiStep` (earlier survey, library hooks table) `[doc]`.
- Entity capability access (`Item.ENTITY_AUTOMATION`) is not needed for dinosaurs.

## 6. Vehicles

- NeoForge 26.2 patches show the vanilla vehicle tree is `world/entity/vehicle/` with subpackages `boat/` (`AbstractBoat`) and `minecart/` (`AbstractMinecart`, `AbstractMinecartContainer`, `Minecart`, `MinecartFurnace`, `MinecartSpawner`, `MinecartCommandBlock`, `NewMinecartBehavior`, `OldMinecartBehavior`), plus `ContainerEntity` (patch list) `[v]`.
- "Minecart improvements": `FeatureFlags.MINECART_IMPROVEMENTS = builder.createVanilla("minecart_improvements")` is still a vanilla feature flag in 26.2 (`patches/net/minecraft/world/flag/FeatureFlags.java.patch:6`) `[v]`. So the smooth new behaviour (`NewMinecartBehavior`: sub-steps along rails, `calculateTrackSpeed`, `adjustToRails`, `calculateBoostTrackSpeed`, see `NewMinecartBehavior.java.patch`) is gated by a flag, not unconditional. Whether the flag is on by default in 26.2 was not checked `[inf]` open.
- `AbstractBoat` is a fully physical vehicle: water status, friction by block under the hull (`friction += blockState.getFriction(level, blockPos, this)`), and a loader hook `canBoatInFluid` (`AbstractBoat.java.patch` hunks). It is the vanilla template for a hull that reads ground friction and fluid. The Nublar tour vehicle (no fuel, road) can reuse the pattern: base class, seat handling, `hurt`/drop-as-item, controller input, interpolation. It cannot reuse the rail code (`NewMinecartBehavior`), because the tour vehicle does not ride rails unless the design says so.
- Verdict: vehicles are a Nublar-written entity (B22) on `Entity`/`VehicleEntity` `[inf]` (`VehicleEntity` is a vanilla base class for hurt and drop handling; not seen in the patches directory, not verified). Do not add a vehicle library for it.

## 7. Cross-loader compat libraries

| Library | 26.x status | What it adds beyond the platform | Verdict |
| --- | --- | --- | --- |
| Balm | `26.2` and `26.3` branches, active `[v]` | one-interface energy / fluid / container exposure, networking, config, events, registrar, attachments | **Adopt** (lead's choice) |
| Team Reborn Energy | `5.0.0` for 26.1+ `[doc]` | the Fabric energy unit | **Compat**: optional on Fabric; Balm already bridges it |
| Common Storage Lib / Botarium | 1.21.1 only `[v]` | storage helpers across loaders | **Avoid** |
| Architectury API | `26.2`, `26.3` branches `[v]` | registry, events, networking, hooks, fluid | **Avoid** if Balm replaces it; one cross-loader base library is enough |
| Forge Config API Port | `26.2.1` bugfix-only, `26.3.1` primary `[doc]` | Forge-style config on Fabric | **Avoid** if Balm config is chosen |
| mysticdrew Common Network | 1.20.1 artifact only in the build | packet layer | **Avoid**: dropped by the port |
| GeckoLib 5 / SBL 2 | `26.2` branches `[v]` (branch lists) | animation / brain | already adopted |

## 8. Recommendations for this survey (the "do not reinvent" rules)

Use these when you judge a mod.

- **E1. Power exposure.** Expose machine energy through `BalmEnergyStorageProvider`. Do not write per-loader energy adapters. If the lead later needs long amounts or transactions, add a `NeoForge EnergyHandler` capability next to Balm's; do not replace Balm's. TRE stays optional on Fabric.
- **E2. Unit.** Keep a single integer unit "FE" in game. If a display unit is wanted (Mekanism-style), convert in the UI only.
- **E3. Fence power graph.** No platform supplies a wire network. Nublar's per-tick pole-to-pole push (`BlockEntityElectricFencePole.java:122-140`) and a `level.getBlockEntity` lookup per connection per tick is its own code. Build one network object per connected group (platform covers only the exposure at the ends). This matches rank 5 of the earlier survey.
- **I1. Items.** Machines stay vanilla `Container`/`WorldlyContainer`. Hopper and pipe compat is free.
- **I2. Fluids.** A mud or water tank uses Balm `FluidTank`; use a loader `ResourceHandler<FluidResource>` / `Storage<FluidVariant>` only if a mod needs components.
- **D1. Item data.** DNA, dino and tube data become `DataComponentType`s with `Codec` + `StreamCodec`. No `CompoundTag` on stacks.
- **D2. Block entity save.** In 26.x the save and load API uses `ValueInput`/`ValueOutput` (seen in `SimpleEnergyHandler.serialize(ValueOutput)` and `CableBlockEntity.readEnergySides(ValueInput)`) `[v]`. The `saveData(CompoundTag)`/`loadData(CompoundTag)` pair in `SyncingBlockEntity` is replaced.
- **D3. Species data.** Dynamic (datapack) registries with codecs, not reload listeners.
- **N1. Packets.** `CustomPacketPayload` + `StreamCodec` registered through Balm.
- **R1. Render boxes.** Use `getRenderBoundingBox` on NeoForge; for Fabric implement the EntityCulling-style interface or shrink `shouldRenderOffScreen`; keep boxes exact.
- **R2. Extract state.** Put render-time inputs in the render state (Fabric `RenderStateDataKey`; NeoForge's state classes), not in lookups on the live entity.
- **T1. Tracking.** Set `clientTrackingRange` and `updateInterval` per species by size.
- **V1. Vehicles.** A normal entity class using `AbstractBoat` as a template for friction and fluid handling.

## 9. What Project Nublar uses today and what the 26.2 platform replaces

Read-only audit of `Project-Nublar` at branch `kash's-slop`. Paths are relative to `common/src/main/java/net/dumbcode/projectnublar/` unless stated. Build files are at the repo root.

**Dependencies (all 1.20.1 values):**
- `gradle.properties:11` `minecraft_version=1.20.1`, `:8` `java_version=17`, `:25` `fabric_version=0.92.1+1.20.1`, `:29` `forge_version=47.2.30`, `:46` `forge_config_api_port_version=8.0.0`, `:47` `geckolib_version=4.7.3`, `:49` `sbl_version=1.15`, `:50` `architectury_api_version=9.2.14`.
- `common/build.gradle:22` FCAP, `:23` toml4j, `:24` SBL, `:25` GeckoLib, `:27` `mysticdrew:common-networking-common:1.0.3-1.20.1`, `:28` `earth.terrarium.botarium:botarium-forge-1.20.1:2.3.4`, `:30` Architectury Forge.
- `fabric/build.gradle:16` FCAP, `:18` toml4j, `:19` SBL, `:20` GeckoLib, `:24` Botarium Fabric (included), `:25` Architectury Fabric.
- `forge/build.gradle:61` MixinExtras, `:63` GeckoLib, `:64` SBL, `:67` Botarium, `:68` Architectury.

**Replacement table.** "Platform" means NeoForge/Fabric/vanilla/Balm as covered above.

| ID | Piece | File:line | Replaced by after the port | Effort |
| --- | --- | --- | --- | --- |
| H1 | Botarium energy on machines | `block/entity/EggPrinterBlockEntity.java:3-5,28,81,139-140`; `IncubatorBlockEntity.java:3-4,32,45,311,361`; `SequencerBlockEntity.java:3-5,38,212,242`; `ProcessorBlockEntity.java:3-5,36,93,131`; `DinosaurFeederBlockEntity.java:3-4,29,32,107` | `BalmEnergyStorageProvider` + `DefaultEnergyStorage` (Balm) | M (one pattern x5) |
| H2 | Botarium generator and push | `block/entity/GeneratorBlockEntity.java:3-6,27,30,68-73,78-79,87` (`EnergyApi.distributeEnergyNearby`) | Balm storage + own neighbour push via capability cache; platform has no auto-push | M |
| H3 | Fence pole energy storage and pole-to-pole push | `block/entity/BlockEntityElectricFencePole.java:5-7,57,67,75,105,111,122-140,224-225` | Balm storage per pole for outside access; own network object for the graph | L (new subsystem; plan B14/B15) |
| H4 | Fence power test reads Botarium | `block/api/Connection.java:6,273` (`te instanceof BotariumEnergyBlock wbec && wbec.getEnergyStorage().getStoredEnergy() > 0`), with `//todo: energy` at line 270 and `//todo: return to false when energy` at line 277 | the network object from H3 | S |
| H5 | `Container`-based block entity base with NBT save and load | `block/api/SyncingContainerBlockEntity.java:14,20-37`; `SyncingBlockEntity.java:20-35` | 26.2 `saveAdditional(ValueOutput)`/`loadAdditional(ValueInput)` and `getUpdateTag(HolderLookup.Provider)` shape; vanilla `Container` stays | M |
| H6 | `getRenderBoundingBox` on a block entity | `block/entity/ProcessorBlockEntity.java:324-326` (`inflate(4)`) | `BlockEntityRenderer.getRenderBoundingBox` (NeoForge); Fabric needs the copied interface | S |
| H7 | Networking through mysticdrew `commonnetwork` | `network/NetworkInit.java:3,10-12`; `ProjectNublar.java:28`; `fabric/.../ProjectNublarClientFabric.java:29`; senders: `client/screen/IncubatorScreen.java:105,132`; `menutypes/SequencerMenu.java:219`; three packets in `network/c2s/` | `CustomPacketPayload` + `StreamCodec` via Balm `registerServerboundPacket` | M |
| H8 | Menu opening with extra buffer | `platform/services/IPlatformHelper.java:47-49`; `forge/.../ForgePlatformHelper.java:48-49` (`NetworkHooks.openScreen`); `fabric/.../FabricPlatformHelper.java:42-69` (`ExtendedScreenHandlerType`, `ExtendedScreenHandlerFactory`) | platform menu factories (`IContainerFactory` on NeoForge, Fabric menu API); delete the `IPlatformHelper` menu methods if Balm has a menu helper `[inf]` | M |
| H9 | Item data in raw NBT | `api/DNAData.java:105-106,195`; `api/DinoData.java:55,243`; `api/loot/functions/AmberItemFunction.java:49`; `FossilItemFunction.java:72`; `block/ElectricFencePostBlock.java:134`; `block/entity/ProcessorBlockEntity.java:97-103`; `SequencerBlockEntity.java:157-216`; `client/screen/SequencerScreen.java:154-157,198-201,781-796,855` | data components with codecs | L (touches 9 files; plan B09/B10 data model) |
| H10 | `DNAData`/`DinoData`/`DiskStorage` NBT serialisation | `api/DNAData.java:159-178`; `api/DinoData.java:162-201`; `api/DiskStorage.java:28-52` | component `Codec` replaces `toNBT/fromNBT` | M |
| H11 | Architectury registries | 12 init files, e.g. `init/EntityInit.java:3-4,30,47-60`; `init/BlockInit.java:3-4,46-55`; `ProjectNublar.java:3` (`RegistrarManager`); `api/FossilCollection.java:3` | Balm registrar or NeoForge `DeferredRegister` plus Fabric registry; or keep Architectury (it exists on 26.2) | M (mechanical) |
| H12 | Forge config via FCAP | `forge/.../ProjectNublarForge.java:32` (commented `registerConfig`); `forge/.../event/CommonModEvents.java:18-22` (empty `onConfigLoaded`); `common/build.gradle:22`, `fabric/build.gradle:16`; toml4j declared with no usage | Balm config, or `ModConfigSpec`; delete FCAP and toml4j | S |
| H13 | JSON reload listeners for species data | `data/BehaviourDataReloadListener.java:19`; `DietReloadListener.java:22`; `FossilConfigReloadListener.java:23`; `GeneDataReloadListener.java:18` | dynamic registries with codecs | M-L |
| H14 | Forge-only model loaders and `ModelData` | `forge/.../client/model/fossil/FossilModelLoader.java`; `testtube/TestTubeModelLoader.java`; `ProjectNublarModelData.java`; `forge/.../mixin/BlockEntityElectricFencePoleMixin.java:17-36`; Fabric twin `fabric/.../client/FossilModel.java`, `ItemOverridesMixin.java` | 26.x model API (NeoForge `UnbakedModelLoader` and friends; Fabric FRAPI) | L |
| H15 | Forge-only GameTest holder | `forge/.../gametest/HerdGameTests.java:18-36` | NeoForge `RegisterGameTestsEvent`; Fabric `fabric-gametest-api-v1` | S |
| H16 | Entity tracking range | `init/EntityInit.java:30,31` (`clientTrackingRange(10)` on two species; others default) | set per species; add `updateInterval` | S |
| H17 | Legacy fluid widget | `forge/.../event/FluidWidget.java:10-13` | Balm `FluidTank` display or a loader fluid render helper | S |
| H18 | Recipe network codecs | `recipe/UnincubatedEggRecipe.java:12,137,151` (`fromNetwork/toNetwork` with `FriendlyByteBuf`) | recipe `StreamCodec` / `MapCodec` serializer | S |

**What stays.** `SyncingContainerBlockEntity extends BaseContainerBlockEntity` (vanilla `Container`), `BlockEntityTicker` use (`ElectricFencePostBlock.java:300-305`, `DinosaurFeederBlock.java:120`, `EggPrinterBlock.java:77`), GeckoLib block entities, the fence connection graph, species data content, SBL brain.

## 10. Closing table

Effort: S under 100 lines, M one or two classes, L new subsystem.

| Technique | Source | Nublar module | Verdict | Effort | Expected effect |
| --- | --- | --- | --- | --- | --- |
| Expose machine energy through Balm provider | Balm `BalmEnergyStorageProvider` `[v]` | B12, B14 | Adopt | M | all machines connect to FE (NeoForge) and TRE (Fabric) without per-loader code |
| NeoForge `EnergyHandler` directly | NeoForge `Capabilities.Energy` `[v]` | B14 | Compat (only if Balm int amounts block a feature) | S | long amounts, transactions |
| Team Reborn Energy on Fabric | TRE `5.0.0` `[doc]` | B14 | Compat, optional | S | Fabric tech-mod power interop |
| Common Storage Lib / Botarium | repo `1.21.x` `[v]` | P2 | Avoid | none | no 26.x build |
| Fence power network object (not per-tick capability lookups) | own; precedent in the earlier survey | B14, B15 | Adopt (own) | M | removes hundreds of lookups per tick |
| `BlockCapabilityCache` for neighbours | NeoForge `BlockCapabilityCache.java:31-47` `[v]`; Powah `CableBlockEntity.java:53,200` `[v]` | B14 | Adopt | S | one cache per side instead of per-tick lookup |
| Machines as vanilla `Container` / `WorldlyContainer` | both loaders wrap them `[v]` | B12 | Adopt | S | hopper and pipe compat free |
| Balm `FluidTank` for tanks | Balm `FluidTank.java` `[v]` | B12, B20 | Adopt | S | simple fluid exposure |
| Data components instead of stack NBT | NeoForge docs, data components `[doc]` | B09, B10, B12 | Adopt (forced) | L | stack data validated, synced, smaller |
| `ValueInput`/`ValueOutput` block entity save | NeoForge sources `[v]` | B12 | Adopt (forced) | M | the 26.x save API |
| Attachments for entity/level data | NeoForge docs, Fabric API `[doc]`,`[v]` | B17, B20, B25 | Adopt | M | no custom maps for level or entity data |
| `CustomPacketPayload` + `StreamCodec` via Balm | Balm `BalmNetworking.java:42,44` `[v]` | P2 | Adopt | M | drops `commonnetwork` |
| Balm config schema | Balm `BalmConfig.java` `[v]` | P2, B25 | Adopt | S | drops FCAP and toml4j |
| Dynamic registries for species data | NeoForge `DataPackRegistryEvent` `[v]` | B09, B16 | Adapt | M-L | validated, synced species data |
| Exact `getRenderBoundingBox` | NeoForge `IBlockEntityRendererExtension.java:20` `[v]`; dispatcher patch `:26` `[v]` | B15, B25 | Adopt | S | off-screen posts skip extraction |
| Fabric render-bounds interface | EntityCulling `BlockEntityRenderFabricExtension.java` `[v]` | B15, B25 | Compat | S | same culling on Fabric |
| Render-state keys for per-entity render data | Fabric `RenderStateDataKey` `[v]` | B09, B25 | Adopt | M | no live-entity reads during render |
| Per-species tracking range and interval | `ChunkMap.java.patch:97`, `EntityType.java.patch:8-19` `[v]` | B25, B26 | Adopt | S | fewer movement packets in a big park |
| `AbstractBoat` as vehicle template | `AbstractBoat.java.patch` `[v]` | B22 | Adapt | M | friction and fluid handling reuse |
| Rail code (`NewMinecartBehavior`) | `NewMinecartBehavior.java.patch` `[v]` | B22 | Not relevant | none | tour vehicle does not use rails |
| Architectury registries | Architectury `26.2` `[v]` | P2 | Avoid if Balm registrar replaces them | M | one fewer cross-loader library |

## 11. Open questions (real design gaps)

1. **Energy unit and size.** Balm's `EnergyStorage` is `int`. The fence network may carry large sums (a creative generator inserts 999999 at once, `GeneratorBlockEntity.java:78`). Does the lead want long amounts or a per-network cap? If long, expose NeoForge `EnergyHandler` too.
2. **Which base library owns registration?** Balm's registrar was read only as a changelog line. Confirm Balm registers blocks, items, entities and menu types before Architectury is dropped (H11).
3. **Is `minecart_improvements` on by default in 26.2?** It is a vanilla feature flag (`FeatureFlags.java.patch:6`). This decides whether a rail tour vehicle (if ever wanted) gets the smooth behaviour. The tour vehicle design does not currently use rails.
4. **Fabric culling for fence block entities.** No Fabric API gives `getRenderBoundingBox`. Decide between the EntityCulling-style interface (cheap, only works when that mod is present) and `shouldRenderOffScreen` (vanilla, coarser). Measure with a 200-post run.
5. **Dedicated server data sync for species.** Dynamic registry vs reload listener changes what a client sees before joining. Needs a one-line decision from the lead.
6. **Target version drift.** NeoForge 26.3.0 and Fabric API for 26.3 and 26.4 already exist, and FCAP, Mekanism and Balm lead on 26.3. Port to 26.2 as decided, but note that libraries move on; Balm's 26.2 branch is `26.2.0.9`.
