# Memory, startup, chunk threading, networking and log-spam mods: what they teach Project Nublar

Date: 2026-10-10. Scope: ten repos, read from shallow clones (read only, nothing built or run). Project Nublar facts come from a read-only grep of the repo at branch `kash's-slop` (working tree, uncommitted changes included) and from `docs/project-plan.md`.

Labels: `[v]` verified in source (path and line at the commit), `[doc]` from README, wiki or changelog, `[inf]` my inference. Every performance number that is not `[v]` is an estimate. Clone root for all paths below: the scratchpad `clones\<repo>\`; paths are written relative to the repo root.

Two corrections to the task text, found while reading:

- **Stfu is not a log silencer.** It is a client quality-of-life mod (menus, chat, toasts, keybinds, load-time options). It silences exactly one log line [v]. See section 8.
- **`fzzyhmstrs/pc` is Particle Core**, a client-only Fabric mod for particle culling and particle tick work. See section 9.

---

## 1. Repos read

| Repo | Branch | Commit | Date | Licence (plain fact) | Loaders | MC version | Size | Status |
|---|---|---|---|---|---|---|---|---|
| malte0811/FerriteCore | `26.1` | `0cef1f2` | 2026-03-24 | MIT | Fabric, NeoForge | 26.1 (`gradle.properties:6`) | 37 Java files in `Common`, about 34 KB of Java | Active. Version 9.0.0 [v] |
| embeddedt/ModernFix | `26.1` (the default branch `1.20` is the 1.20.1 line) | `8a837c0` | 2026-10-08 | LGPL-3.0 (GitHub shows "other") | The 26.1 branch builds NeoForge only (`build.gradle.kts:19`, `modernfix-neoforge`) [v]. Older lines also build Fabric [doc] | 26.1.2 (`gradle.properties:8`) | 273 files | Very active. Continuous release: one release per commit [doc] (`Changelog.md`) |
| embeddedt/ModernFix wiki | n/a | n/a | n/a | n/a | n/a | n/a | 36 pages | Used for the dynamic resources FAQ and the patch lists |
| RelativityMC/C2ME-fabric | `dev/26.3.0` | `9ac50bd` | 2026-10-07 | MIT for most; one directory (`c2me-opts-accel-opencl/`) is All Rights Reserved (`LICENSE.md`) | Fabric only | Latest release plus latest snapshot only (`README.md`); this branch is 26.3 | 848 files, 25 modules | Very active (pushed 2026-10-10) |
| RelativityMC/ScalableLux | `ver/26.2.0` | `77c290c` | 2026-07-09 | LGPL-3.0 | Fabric only | 26.2 (`gradle.properties`) | 41 Java files | Active. Version `0.3.0-alpha.0`: alpha [v] |
| astei/krypton | `master` | `bc08466` | 2026-09-30 | LGPL-3.0 | Fabric only (`src/main/resources/fabric.mod.json`) | 26.3 | 24 Java files | Active, "best-effort" by author's own words [doc] |
| TonimatasDEV/PacketFixer | `master` | `ba04520` | 2026-09-20 | MIT | Fabric and NeoForge (`common`, `fabric`, `neoforge` modules) | 26.3 | 26 Java files | Active. Version 3.3.7 |
| fxmorin/memoryLeakFix | `dev` | `988f54c` | 2025-06-25 | LGPL-2.1 | Fabric and Forge (Architectury) | 1.20.4 (`gradle.properties`) | 29 Java files | Stale for 26.x. The wiki says only 2 of 7 fixes still apply on 1.20.4+ |
| astei/lazydfu | `master` | `a83ce2f` | 2022-06-07 | MIT | Fabric | 1.19 (`gradle.properties:5`) | small | Not archived; last push 2024-01-13. Obsolete for 26.x |
| ItsFelix5/Stfu | `multiversion` | `7a5515f` | 2026-06-16 | MIT | Fabric only (FAQ: "Can you port this to (Neo)Forge? No.") | 1.20.1, 1.21.1, 1.21.8 to 1.21.11, 26.1, 26.2 | 86 files | Active. Version 2.9.1 |
| fzzyhmstrs/pc (Particle Core) | `master` | `1151fe6` | 2026-07-07 | MIT | Fabric only, client only (`fabric.mod.json` `"environment": "client"`) | 26.2, needs Fabric Language Kotlin and Fzzy Config | 57 files (Java and Kotlin) | Active. Version 0.3.3 |

Wikis read: ModernFix (36 pages), Krypton (Home, FAQ), memoryLeakFix (Current-Fixes), Stfu (Features).

Platform coverage of the repos matters for Nublar. Nublar targets Fabric and NeoForge on 26.2. Only FerriteCore and PacketFixer cover both loaders; C2ME, ScalableLux, Krypton, Stfu and Particle Core are Fabric only; ModernFix on 26.1 is NeoForge only. So on each loader, Nublar must work with the mods that exist there, and must not need any of them [inf].

---

## 2. FerriteCore: block-state memory

**In game.** Less RAM, a faster world join, and no change in play. Packs with many multi-property blocks save the most. In its README, the author reports 600 MB saved from block-state neighbour tables alone in a large pack from 2020 [doc].

**Mechanism.**

- Each vanilla block state stores a table of its neighbour states (the result of `state.setValue(property, value)`). FerriteCore replaces the per-state tables with one `FastMap` per block, an array indexed by the mixed-radix number of the property values (`Common/src/main/java/malte0811/ferritecore/fastmap/FastMap.java:11-47`, `with()` at 55-62) [v]. Cost goes from O(states x sum of values per property) to O(states) [doc] (`summary.md` point 2).
- It also drops the per-state property array: `propertyValues` is set to `null` and values are read back from the `FastMap` (`mixin/fastmap/FastMapStateHolderMixin.java`, `ferritecore_setStateMap`) [v]. README point 3: about 170 MB in that pack [doc].
- It deduplicates the per-state cache. `BlockStateCacheImpl` shares collision-shape and face-sturdy arrays between states, and it rewrites the inside of a `VoxelShape` to point at a canonical copy (`impl/BlockStateCacheImpl.java:53-97`) [v]. README point 7: about 200 MB [doc].
- It replaces the per-chunk-section `ThreadingDetector` by one byte, but this is off by default: "very rare and very hard-to-reproduce crashes" (`mixin/config/FerriteConfig.java`, option `useSmallThreadingDetector`, opt-in) [v].
- On 26.1 it also shares empty data-component patch maps (`mixin/datacomponents/PatchedDataComponentMapMixin.java:12-33`) [v].
- **The model-side optimizations are gone on 26.x.** Changelog 8.0.0: "Since the model system has been rewritten entirely, all optimizations related to it have been removed for the time being" [doc] (`changelog/8.0.0.md`). The config now has six options only: neighbour lookup, property map, block-state cache dedup, data components, compact fast map, small threading detector [v] (`FerriteConfig.java`). Points 1, 4, 5, 6 and 8 of the README (predicate caching, multipart instance sharing, model-resource-location strings, quad dedup) do not exist on 26.x. Do not count on them.

### What this teaches Nublar about state counts

Block states are Nublar's cheapest-looking and most dangerous memory item. Every state costs: a `BlockState` object with a property array, a neighbour table (vanilla) or a slot in a `FastMap` (FerriteCore), a cache object with shape and sturdy arrays, a model lookup, and one entry in the global state id map [inf]. FerriteCore shrinks the first two, but the count still multiplies everything else.

**Nublar today (grep, read only):**

| Block family | Properties | States per block | Note |
|---|---|---|---|
| Machines (`MultiEntityBlock`: processor, sequencer, incubator, egg printer) | `ROWS` 0-2, `COLUMNS` 0-1, `DEPTH` 0-1, `FACING` 4 (`block/api/MultiBlock.java:11-14`, `MultiEntityBlock.java:72-74`) | 3 x 2 x 2 x 4 = 48 | 4 machines = 192 states. Small. |
| Low-security fence post | `powered` 2 x `index` 0-2 (`EnumConnectionType.LOW_SECURITY`, height 3) | 6 | Small. |
| High-security fence post | `powered` 2 x `index` 0-7 (height 8) | 16 | Small. |
| Fence wire (`ElectricFenceBlock`) | none in the state; connections live in a block entity (`BlockConnectableBase.java:76-80`, `:268-270`) | 1 | No state explosion. |
| Fossil blocks (`FossilCollection.create`, `api/FossilCollection.java:35-52`) | none; one block per host stone x quality x piece, per species | 1 per block | See the block-count line below. |

The fence design (hundreds of wire block entities, a small state count per post) is already the right shape. The vanilla fence and wall pattern (one boolean or enum per side) would multiply the count, and the author of ModernFix lists "modpacks that add many variations of wall blocks" as a memory problem big enough to need a patch (`common/mixin/perf/deduplicate_wall_shapes/WallBlockMixin.java:18-21`, javadoc) [v]. Nublar's choice to keep connections in the block entity avoids that. The price: shape, collision and light queries then depend on a block entity, which is a rule break for several optimization mods (see section 4 and the Lithium notes by the other researcher).

**Block count in the current fossil code.** `FossilCollection.create` registers one `FossilBlock` per (host stone x quality x piece) and one `AmberBlock` per host stone. The stone list has 13 entries; `Quality` has 5 values; T. rex has 7 pieces and Triceratops 7 [v]. So today: 13 x 5 x 7 x 2 = 910 fossil blocks and 26 amber blocks, for two species. At six species with 7 to 8 pieces each, the same scheme would give about 3,000 to 3,600 blocks [inf]. Each block also gets a block item, a blockstate JSON, a model JSON, a loot table and a language key. That is the startup and model-loading cost that ModernFix's dynamic resources try to reduce, and it is the part of the design where the plan already changes course: B01 wants three host stones and `QUALITY` and `BIOME_GROUP` as block-state properties (`docs/project-plan.md:287-292`, `:300`). The plan does not say where species and piece live. That is the open question for the count (section 12, question 1).

### The fossil ore question: how many states for random tint, opacity and rotation?

The fossil design says tint strength 0 to 90 percent, opacity 15 to 100 percent and random rotation per ore (`project-plan.md:292`, from `Fossil Ore Aesthetics.docx`). The plan already says "per-position randomness from the block position seed (no block entity needed for look)". The numbers show why that is the correct choice.

Count for one fossil block, with `QUALITY` (4 tiers, NONE excluded as in the plan's ladder at line 317) and `BIOME_GROUP` (assume 8 groups; the species cards list 5 to 8 biome families, `docs/codebase-analysis.md:1189-1193` [inf]):

| Design | Extra properties | States per block | x 3 host stones | x about 48 species-piece blocks (6 species x 8 pieces) |
|---|---|---|---|---|
| A. Plan: quality and biome group only; look from position seed | none | 4 x 8 = 32 | 96 | about 4,600 |
| B. Quantised look as properties: tint 10 steps, opacity 8 steps, rotation 4 | x 320 | 10,240 | 30,720 | about 1.5 million |
| C. Finer look: tint 10, opacity 8, rotation 16 | x 1,280 | 40,960 | 122,880 | about 5.9 million |

Vanilla has roughly 25,000 block states [inf, from memory; not verified here]. Design B alone would be about 60 times vanilla. FerriteCore's own formula shows the damage: the vanilla neighbour table costs O(states x sum of values per property); property count grows the sum, and the state count grows the first factor. FerriteCore cuts the neighbour table to O(states), but the `FastMap` array must hold the full product of value counts (`FastMap.java:22-37`: `factorUpTo` is the product, and a list of that length is allocated) [v], so a 1.5 million-state set still needs a 1.5 million-slot list per block even before the cache objects. The shape and sturdy cache per state, and the baked-model lookup per state, are not helped by FerriteCore at all on 26.x because the model dedup was removed [doc].

Rules this gives Nublar:

1. **Never put cosmetic randomness in block-state properties.** Quality (gameplay, in the drop) and biome group (gameplay, in the drop) are state. Tint strength, opacity and rotation are looks.
2. **Draw the look from the position seed, as the plan says.** On 26.x the weighted variants in a block-state definition already pick a random model per position (the seed comes from the block position), so rotation can be 4 weighted variants with `y` 0, 90, 180 and 270 in the blockstate JSON. This costs baked quads, not states [inf; the exact 26.x blockstate format should be checked by the model-rendering researcher].
3. **Tint** can come from the block tint index with a position-hash colour at render time: zero extra states, zero extra models. **Opacity** is the hard one: the terrain pipeline does not give per-block alpha through tint, so use a small fixed number (3 to 4) of overlay textures chosen by weighted variant [inf]. State the count in the plan: 4 qualities x 4 opacity variants x 4 rotations = 64 baked model variants per block, with 0 extra states.
4. **Do not use a block entity for the look.** A block entity per ore block would cost an object per ore, chunk-save data, and a client sync, for a visual-only value [inf].
5. **Keep property value sets small and ordered.** FerriteCore's `FastMap` constructor asserts that every `Property` returns its possible values in the same order as its internal index (`FastMap.java:20-24`, `Preconditions.checkState(getIndexUnsafe(prop, prop.getPossibleValues().get(i)) == i)`) [v]. A custom `Property` subclass with an inconsistent order crashes at startup when FerriteCore is installed. Use `IntegerProperty`, `BooleanProperty` and `EnumProperty` only.

**Compatibility duty (FerriteCore).**

- Do not read or write `StateHolder.propertyValues`, `StateHolder.neighbours` or `BlockStateBase.cache` by reflection or mixin. FerriteCore sets the first to `null` and removes the second (`FastMapStateHolderMixin`: `initializeNeighbors` now throws `UnsupportedOperationException`) [v].
- Do not compare `VoxelShape` objects by identity and do not mutate a shape after creation. FerriteCore rewrites shape internals to share them (`BlockStateCacheImpl.java:53-97`) [v].
- Do not call `setValue` with a value outside the property's range. The `FastMap` path throws `IllegalArgumentException` (`FastMapStateHolderMixin.setValueInternal`) [v]. Vanilla would too, but the message is different.
- `MultiEntityBlock.SHAPES` (`block/api/MultiEntityBlock.java:35`) is a `static HashMap<BlockState, VoxelShape>` filled by `computeIfAbsent` inside `getShape()` (`:83`). It is not FerriteCore-incompatible, but it is the same class of bug that ModernFix patches in other mods (section 3). See section 6.

**Platform overlap.** None for the memory work: vanilla 26.2 keeps the neighbour tables. NeoForge and Fabric API do not replace FerriteCore.

**Verdict: Adopt (pattern).** Keep state counts small; keep looks out of state. Nublar does not copy FerriteCore code. Effort: S (a rule in the B01 plan and a unit test that counts states). Target module: B01 (fossil ore), B12 (machine blocks), B15 (fences).

---

## 3. ModernFix: loading, memory, and mod-compat problems

**In game.** Faster launch and world join, lower memory. The wiki reports a modpack dropping from 1.7 GB to 945 MB with dynamic resources on and FerriteCore already installed: 55 percent [doc] (`Dynamic-Resources-FAQ.md`). That page marks its own data "TODO: collect more data", so treat it as one data point.

**Mechanism.**

- **Dynamic resources** (`mixin.perf.dynamic_resources`): block and item models are loaded when first needed instead of all at launch. On the 26.1 branch the implementation is a `LoadingCache<Identifier, Optional<RESULT>>` with `softValues().maximumSize(1000)` over the resource map (`dynresources/DynamicModelSystem.java:61`) [v]. It wraps the unbaked-model map, the block-state definition map and the full block-state set (`getAllBlockStates()` at `:102`, `createDynamicBlockStateLoadedModels` at `:124`) [v]. The code comment at `:104-108` says the iterator is written so that "mods iterating the maps are likely to work with the same block many times in a row, which hits our caches better" [v].
- **It is off by default**, in the default-settings table: `.put("mixin.perf.dynamic_resources", false)` (`core/config/ModernFixEarlyConfig.java:248`) [v]. The wiki says the reason: "dynamic resources, by nature of changing such a fundamental mechanic, may introduce compatibility issues with some mods that use nonstandard APIs to alter the vanilla model system... The most likely issues you will encounter... are a crash on startup or missing textures on some models" [doc].
- **The 26.1 branch is not complete.** `TODO.txt` line 1: "Reimplement dynamic_resources for items & Neo standalone models" [v]. Items and NeoForge standalone models are not dynamic yet.
- Other features that matter to Nublar, with their default state [v] from `ModernFixEarlyConfig.java:248-272` and the 26.1 mixin tree (`common/mixin/perf/`): `dynamic_entity_renderers` (off: builds entity models on first sight; "Some mods are not compatible... EntityRenderer crashes" [doc]), `faster_item_rendering` (off), `deduplicate_location` (off, load-time cost), `compact_entity_models`, `cache_blockstate_cache_arrays`, `deduplicate_wall_shapes`, `memoize_creative_tab_build`, `release_protochunks`, `datapack_reload_exceptions`, `encoder_cache_leak`, `suspend_integrated_server_during_load`, `dynamic_dfu`.
- **Stability levels.** New patches ship at a "Beta" level that needs opt-in; "GA" is the default (`Stability-Levels.md`) [doc].
- **Its compatibility list is code**: `disableIfModPresent("mixin.bugfix.paper_chunk_patches", "c2me")`, `"mixin.perf.nbt_memory_usage", "c2me"`, `"mixin.bugfix.chunk_deadlock", "c2me", "dimthread"`, `"mixin.perf.release_protochunks", "c2me", "moonrise"`, `"mixin.perf.dynamic_dfu", "litematica"`, `"mixin.bugfix.packet_leak", "memoryleakfix"` (`ModernFixEarlyConfig.java:302-328`) [v]. So ModernFix backs off in front of C2ME and memoryLeakFix. Nublar needs nothing here.

### What breaks dynamic loading and slows startup (patterns seen in the source)

| Pattern | Why it hurts | Evidence |
|---|---|---|
| Code that walks every block state's model at startup (for example a `ModelEvent` listener that loops `BuiltInRegistries.BLOCK` and asks for each state's model) | Defeats the lazy cache; every model loads at once | The iterator comment in `DynamicModelSystem.java:104-108` [v] |
| Custom model loaders that replace models by block-state or by model location through the old event APIs | "nonstandard APIs to alter the vanilla model system" [doc] | Dynamic Resources FAQ |
| Huge numbers of registered blocks and items (each one adds a block-state JSON, a model, a language key, a loot table) | Cost is linear in the count even without dynamic loading | Section 2: 910 fossil blocks for two species |
| Per-variant textures: one texture per quality x piece x species | Atlas stitching cost and atlas size | `TODO.txt` line 5: "faster_texture_stitching" is under review on 21.x+ [v]; the cost is stitching |
| Registering a reload listener from `FMLClientSetupEvent` or `FMLCommonSetupEvent` | Races the vanilla reload; random `ConcurrentModificationException` | `wiki/registerReloadListener-called-on-wrong-thread.md`, and a runtime guard that moves the call to the main thread and logs an error (`common/mixin/bugfix/concurrency/ReloadableResourceManagerMixin.java:31-40`) [v] |
| Doing world access or chunk loads inside an entity-join or chunk-promotion handler | Chunk-system deadlock | `common/mixin/bugfix/chunk_deadlock/ChunkMapLoadMixin.java:27-40` (javadoc: "bad EntityJoinLevelEvent implementations") [v] |
| Holding a `Level`, chunk or screen in a static field | Keeps the world alive after disconnect | ModernFix clears chunk storage and block-entity tickers at `Minecraft.disconnect` to limit the damage from "leaked client worlds" (`bugfix/world_leaks/MinecraftMixin.java:26-42`) [v] |
| A non-thread-safe static shape cache (`HashMap`) filled from `getShape` | `ConcurrentModificationException` when two threads ask for shapes | `bugfix/unsafe_modded_shape_caches/ShapeCacheRSMixin.java:14-30` (Refined Storage) and `ShapeCacheCyclicMixin.java` (Cyclic): ModernFix swaps their `CACHE` map for a `ConcurrentHashMap` [v]. The javadoc says it "seems more likely to happen with ModernFix installed due to the dynamic blockstate cache generation" [v]. |
| Reading datapack or config data from a reload listener and storing it in a static `HashMap` that other threads read during the reload | Torn reads | `[inf]` |
| Reflection or `Class.forName` on mod classes at startup | Each call is slow and breaks when a coremod renames; the wiki's launch-profiling page shows how to find it | `Profiling-game-launches.md` [doc] |

Documented ModernFix "mods known to cause deadlocks" list (`Mods-Causing-Deadlocks.md`) names three mods (Twigs, Et Cetera, Valhelsia Structures) [doc]. None is Nublar.

**Lesson for Nublar** (B25, P2, B01):

1. **Keep the registered block and item count low.** The ModernFix wiki's own advice for a minimal-memory client is dynamic resources plus FerriteCore plus Sodium [doc] (`Ultra‐Low-Memory-Footprint.md`). Dynamic loading does not remove the registration cost, only the model-load cost. For fossils, one block per host stone with properties beats 3,000 blocks (section 2).
2. **Never loop all states or all blocks of the mod's own namespace to set up models.** Register what you have by data-driven definition (blockstate JSON). This stays valid when dynamic loading is on.
3. **Do not register reload listeners from setup events.** Use the platform event (NeoForge `AddServerReloadListenersEvent` and the client reload-listener registration event; Fabric API's `ResourceManagerHelper.registerReloadListener`) [doc]. Nublar already has three data reload listeners (`BehaviourDataReloadListener`, `DietReloadListener`, `FossilConfigReloadListener`); P2 must port them to the platform events on both loaders.
4. **Replace static `Map` fields that reload listeners fill by an immutable swap.** Nublar's listeners already do this (`private static Map ... = Collections.emptyMap()` then assign a new map; `data/BehaviourDataReloadListener.java:23`, `:33`). Good pattern; keep it, and make the field `volatile` [inf].
5. **Do not rely on a model being baked at startup.** Fence post models use GeckoLib (`FencePostModel` keeps three `HashMap` caches of `ResourceLocation`, `client/model/FencePostModel.java:14-16`). That is resource-location math only, not model baking, so it is safe with dynamic resources. If B15 later bakes fence wire quads at startup, test with `mixin.perf.dynamic_resources=true`.

**Compatibility duty.** Test the client once with ModernFix on NeoForge 26.2 with `mixin.perf.dynamic_resources=true` and `mixin.perf.dynamic_entity_renderers=true`. The second one is the risk for Nublar: dinosaur renderers create layers in code (`DinosaurRenderer.createLayers` builds layers from the first entity, `docs/project-plan.md:491`), and GeckoLib renderers register through `EntityRenderersEvent`. A renderer that is built on first sight must not need state from startup [inf]. ModernFix has no Fabric build on 26.1, so this test applies to NeoForge only.

**Platform overlap.** Vanilla 26.x already has a rewritten model system (FerriteCore's changelog says so) [doc]. NeoForge has standalone models. ModernFix is an optional mod; nothing in the platform replaces it.

**Verdict: Compat (optional integration, test only).** Adopt the rules above as a Nublar checklist. Effort: S.

---

## 4. C2ME and ScalableLux: thread rules for world generation and light

**In game.** C2ME makes chunk generation, loading and saving use many CPU cores. With it, a new area generates faster and flying over new land causes fewer lag spikes. ScalableLux does the same for light updates. In the README of C2ME: "world generation datapacks that can run on vanilla Minecraft are fully supported. Custom world generators implemented in mods usually runs well, but *may* cause compatibility issues due to certain design assumption used by mod authors being broken for further speedups" [doc]. Both mods are Fabric only [v]. On NeoForge there is no direct counterpart in these repos. So Nublar worldgen must be thread-safe by rule on both loaders, because Fabric players will run C2ME and because vanilla itself already generates chunks on worker threads [inf].

### C2ME: what it fixes shows what vanilla and mods get wrong

All paths below are under `c2me-fixes-worldgen-threading-issues/src/main/java/com/ishland/c2me/fixes/worldgen/threading_issues/` unless noted. The module patches vanilla classes that are not thread safe. Each patch is a rule for mod authors.

| Vanilla bug fixed | What it shows | Evidence |
|---|---|---|
| `StructureTemplate` lists wrapped in `Collections.synchronizedList`, and `PalettedBlockInfoList.blockToInfos` in `synchronizedMap` | Templates are shared between generation threads; do not mutate a template after load | `mixin/threading/MixinStructure.java:50-56`; `MixinStructurePalettedBlockInfoList.java` [v] |
| `StructurePlacementData.processors` wrapped | Processors list is shared | `MixinStructurePlacementData.java` [v] |
| `StructureStart.references` replaced by an `AtomicInteger` | A plain `int` counter on shared start objects races | `mixin/threading/MixinStructureStart.java:40-60` [v] |
| `StructureLocator` caches replaced by synchronized maps, with an explicit mutex around iteration | Shared per-level caches need real locking | `MixinStructureChecker.java` [v] |
| `RandomizedIntBlockStateProvider` rewritten | The vanilla class caches a resolved `IntProperty` in a mutable field on first use. That is a lazy cache in a shared feature config; C2ME re-writes `get()` to use a local copy (`mixin/threading/MixinRandomizedIntBlockStateProvider.java:64-76`, with `this.property = property` at 74) [v] | |
| `LightStorage.updateLight` wrapped in `synchronized (this.queuedSections)` | Iterating a shared map while another thread adds to it | `mixin/threading/MixinLightStorage.java:47` [v] |
| `ServerLevel.getNextEntityId` only asks the chunk manager on the owner thread | Entity-id allocation from a worker thread | `mixin/threading/MixinServerWorld.java` [v] |
| Many structure-piece classes (strongholds, mansions, monuments, temples, fortress) patched | Each had static or shared mutable state in the piece generator (stone selectors, piece weights) | the 25 `Mixin*Generator*.java` files in `mixin/threading/` [v] |
| `Block.isShapeFullCube` replaced by a per-thread cache | A global shape-cache map is a contention point; use a `ThreadLocal` | `c2me-opts-worldgen-vanilla/.../tlcache/MixinBlock.java` [v] |

### C2ME: the detectors that crash a mod that breaks the rules

These are the part that matters most for a mod author, because they turn silent corruption into a loud crash or a logged error:

- **World random on the wrong thread.** C2ME replaces the `Level` random with `CheckedThreadLocalRandom`. If a thread other than the owner uses it, it logs the full explanation and throws (`common/CheckedThreadLocalRandom.java:44` for the flag, `:79-92` for the throw) unless `fixes.enforceSafeWorldRandomAccess` is false [v]. The config is on by default (`common/Config.java`). The exception message says: "ThreadLocalRandom accessed from a different thread" [v]. Rule: **inside a feature, use only the `RandomSource` the generator passes in. Never use `level.getRandom()`, `new Random()`, or a static random.**
- **Write or schedule outside the region.** `ChunkRegion` wrappers check `isValidForSetBlock(pos)` and, for a position outside the allowed area, log a warning and return a read-only tick scheduler instead of the real one (`mixin/threading_detections/readonly_protection/MixinChunkRegion.java:55-70`) [v]. Rule: **a feature may only touch blocks inside the region the generator gave it (3 x 3 chunks around the target for features; the center chunk only for earlier steps).**
- **Item drops during generation.** `breakBlock` with `drop = true` during generation is forced to `false` and an error is logged (`mixin/threading/MixinChunkRegion.java:56-62`) [v]. Rule: **a feature must never call `destroyBlock(pos, true)` or spawn drops.**
- **Async catchers.** `ServerChunkCache.tick` throws if called from a non-server thread (`c2me-fixes-general-threading-issues/.../asynccatchers/MixinServerChunkManager.java`) [v].

### C2ME: the rules this gives Nublar's worldgen (B01 fossil ore, B03 amber, B21 digsites)

1. **No shared mutable state in a feature, a feature config, a placement modifier, a structure or a processor.** Nublar today has `ProjectNublar.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP` as a `public static Map<String, Map<String, SimpleWeightedRandomList.Builder<String>>>` (`ProjectNublar.java:15`), and `FossilCollection.fullFossilMap`, `COLLECTIONS` and `fullAmberMap` as `public static HashMap` fields (`api/FossilCollection.java:20-22`) [v]. If any placement code reads these while a reload or a second world generates, it is a data race. Build them once, on the main thread, into immutable maps (`Map.copyOf`, `ImmutableMap`), and publish by one assignment to a `volatile` field [inf].
2. **No lazy caches inside a config.** The `RandomizedIntBlockStateProvider` case is exactly that mistake. If `FossilFeature` or a placement modifier caches a resolved `Block`, a weighted list or a `Quality` table in a field on first use, make it a `final` computed in the constructor, or a record. The plan's `FossilRarity` pure class and `FossilSet` record are the right shape.
3. **Use the generator's `RandomSource` only.** The plan already says so: "Use the worldgen `RandomSource`, not `new Random()`" (`project-plan.md:154`). Add one more rule: derive the per-ore tint, rotation and opacity from the block position and the world seed with a pure hash function, not from any random object. Then it is identical on server, client and every thread, and it needs no block entity (the plan, line 292).
4. **Only place blocks inside the given region, and only by `setBlock`.** No `level.getBlockEntity` reads on neighbour chunks that may not be loaded (a `getBlockEntity` call outside the region can force a chunk load and deadlock the chunk system; ModernFix patches this class of bug, section 3). No drops. No entity spawns in the carving or feature steps (use the structure's `afterPlace` or a mob spawner block for digsite loot).
5. **No per-block block entities from worldgen if the ore does not need one.** Amber and fossil ore carry gameplay data (quality, biome group, depth band) in block-state properties (plan lines 291 and 347). Keep that. A block entity created in a feature must be created by the normal `setBlock` path and filled by `BlockEntity.load` only, never by pushing data to a global map.
6. **Structure pieces (digsites, B21) are shared across threads.** Do not keep a mutable field on a piece that is written during `postProcess`. Read all inputs (species, depth band) from the piece's NBT at construction. The C2ME patches for vanilla pieces show what happens otherwise.
7. **Loot and chest contents** must be set with a loot table and a seed (`RandomizableContainer.setLootTable`), not by calling a random inside the feature. Same reason.
8. **Data pack friendliness.** C2ME says data-pack worldgen that runs in vanilla is "fully supported" [doc]. Nublar's features should therefore be registered as configured and placed features in `data/`, with codecs, so that nothing in Java code needs to be thread-aware beyond the feature class itself. B01's four forms (`single_ore`, `small_cluster`, `skeleton_cluster`, `digsite_deposit`) as configured features fit this.

**Check, as read-only grep in the repo.** The old `fossils-1.20.1` code (the thing B01 ports) uses `new Random()` and has a missing `break` in the quality roll, per the plan (`:154`). Fixing both is a thread-safety fix as well as a correctness fix.

### C2ME: other points

- The module list (`c2me-opts-*`, `c2me-rewrites-*`, `c2me-fixes-*`, `c2me-threading-lighting`) shows that C2ME also rewrites chunk I/O, the chunk serializer, the scheduler, and a "no tick view distance" mode [v] (module directory names). A Nublar feature must not assume vanilla chunk status order beyond the documented one.
- C2ME's autosave patches ("enhanced autosave") spread saving over idle time [v] (`c2me-opts-scheduling/.../idle_tasks/autosave/`). A park with many block entities saves with less spike; Nublar's block entities should therefore write their NBT in `saveAdditional` without side effects and without heavy computation [inf].
- C2ME requires Fabric API modules only for networking and lifecycle events (`c2me-base/.../ModStatuses.java`) [v].
- JDK 25 and Clang are build requirements for the native modules (`README.md`) [doc]. Not relevant at runtime for Nublar.

**Compatibility duty (C2ME).** Run one GameTest or a manual 200-chunk generation on Fabric 26.2 with C2ME installed, with `enforceSafeWorldRandomAccess` at its default (true). Any crash that names a Nublar class is a Nublar bug. Do the same without C2ME to compare ore counts (the plan's B01 acceptance already asks for a 100-chunk count).

**Platform overlap.** Vanilla already runs generation on worker threads; the thread rules above apply to vanilla with or without C2ME. NeoForge has no equivalent of C2ME in these repos.

**Verdict (C2ME): Adopt (rules as a checklist) and Compat (test on Fabric).** Effort: S for the checklist and one test run; M if B01's old code needs a small rewrite. Target: B01, B03, B21.

### ScalableLux: what the light engine reads from a block

**In game.** Light updates run on several threads. Placing a lamp, digging out a cave or generating a chunk causes less lag. It is a fork of Starlight with optional parallel updates (`README.md`) [doc]. The repo calls itself alpha (`0.3.0-alpha.0`) [v].

**Mechanism.** The engine reads only a small part of a block state:

- `blockState.getLightEmission()` for light sources (`src/main/java/ca/spottedleaf/starlight/common/light/BlockStarLightEngine.java:94`, `:239`) [v].
- `blockState.getLightDampening()` (opacity) for how much light is lost (`BlockStarLightEngine.java:136`, `SkyStarLightEngine.java:308`) [v].
- For blocks that can occlude and use their shape for light (`canOcclude & useShapeForLightOcclusion`), the face occlusion shape: `getFaceOcclusionShape(direction)` (`StarLightEngine.java:1192-1193`, `:1232`). The flag is computed once per state in `initCache` (`mixin/common/blockstate/BlockStateBaseMixin.java:43`) [v].
- It does not read block entities and does not take a `Level` argument for any of these. So **light data must be a pure function of the block state.**
- The engine keeps its own per-chunk light data and writes a "lit" flag in the chunk save (`TECHNICAL_DETAILS.md`, "Chunk Save Format") [doc]. A world saved with ScalableLux and then opened without it just relights the chunks [doc].
- Parallelism: the scheduler locks a square of chunks per task (`common/thread/SchedulingUtil.java`, `scheduleTask(ownerTag, task, x, z, radius)`) and uses `availableProcessors() / 3` threads by default (`common/config/Config.java`) [v]. `GlobalExecutors` is a shared pool; "externally managed" mode lets C2ME supply it (`SchedulingUtil.isExternallyManaged`, `c2me-threading-lighting/.../mixin/scalablelux/MixinSchedulingUtil.java`) [v].
- The technical document states that this is a "complete rewrite" and "any mod that relies on hooking directly into the light engine will be broken" [doc] (`TECHNICAL_DETAILS.md`, Mod Compatibility). It also says the comparison to vanilla in that file is only valid for versions 1.16 to 1.19 [doc].

**Lesson for Nublar** (B01, B12, B14, B15, B20).

1. **Block light must come from the block state, through the right method.** The fence post reads state only, which is correct, but it uses the wrong method. `ElectricFencePostBlock` overrides `getLightBlock(state, world, pos)` and returns the tier light level (10 or 15) when `powered` is true on the top index (`block/ElectricFencePostBlock.java:273-276`) [v]. `getLightBlock` is the light *dampening* (opacity), the value ScalableLux reads as `getLightDampening()` (`BlockStarLightEngine.java:136`) [v]. Emission is `BlockBehaviour.Properties.lightLevel(state -> ...)`, read as `getLightEmission()` (`:94`) [v]. So a powered post top blocks 10 to 15 levels of light instead of emitting it [inf, from the vanilla meaning of the two methods]. Verify in game; if confirmed, fix in B15 with a `lightLevel` function on the block properties. Machines and generators that glow when running must use a `LIT` or `RUNNING` boolean property with `lightLevel`, set from the block entity on state change only.
2. **Update the state only when the value changes.** Every `setBlock` that changes the light value triggers a light update in a 16-block radius or more (up to 15 blocks in each direction) [inf]. A machine that toggles `LIT` every tick would flood the engine. A fence section that toggles `powered` on 200 posts at once (a switch block, B15) is 200 light updates. Batch these with a single flag update and use `Block.UPDATE_CLIENTS` without neighbour updates where possible.
3. **Do not use opacity or light as a gameplay value that changes continuously.** The "random opacity 15 to 100 percent" of the fossil design is a texture alpha, not a light-dampening value. Keep it so: `lightBlock` must stay a constant of the block, or the light engine pays on every chunk load [inf].
4. **No mixin into the vanilla light engine, and no code that calls `LevelLightEngine` internals.** None exists in Nublar (grep: no `LightEngine`, `LayerLightSection` or `getLightEngine` in `common/src`) [v, negative grep].
5. **Block-entity-dependent shapes are fine for collision and rendering but not for light.** Fence wires use a block entity for their collision shape (`BlockConnectableBase.java:268-270`). The light engine only calls `getFaceOcclusionShape` when `useShapeForLightOcclusion` is set, so keep that flag off for wire blocks, or the engine will read a block-entity-derived shape on a worker thread [inf].

**Compatibility duty.** None beyond rules 1 to 5. Fabric players may run ScalableLux; no hook is needed.

**Platform overlap.** Vanilla 26.x has its own light engine that Starlight's technical note says is a copy of the Starlight design from 1.20 on [doc]. No NeoForge or Fabric API covers ScalableLux's threading.

**Verdict: Not relevant as code; Adopt (pattern) for rules 1 to 3.** Effort: S. Target: B12, B14, B15.

---

## 5. Krypton and PacketFixer: packet size, compression, and what Nublar sends

**In game.** Krypton gives smoother tick times on busy servers (less time in the network layer) and nothing on the client. PacketFixer removes the "packet too big", "NBT tag too big" and "payload too large" kick or disconnect for big modpacks. Neither changes play, but PacketFixer *hides* a bug that Nublar must not cause: it lets a mod send data that the game was built to refuse.

### Vanilla limits, read out of PacketFixer's patches

PacketFixer patches constants. Each patch is a verified vanilla limit at the 26.x commit [v] (`common/src/main/java/dev/tonimatas/packetfixer/mixins/`):

| Limit | Value | Where PacketFixer patches it |
|---|---|---|
| Clientbound custom payload (server to client), unknown payload | 1,048,576 bytes (1 MiB) | `ClientboundCustomPayloadPacketMixin.java:11,16`; `ClientboundCustomQueryPacketMixin.java:12` |
| Serverbound custom payload (client to server) | 32,767 bytes | `ServerboundCustomPayloadPacketMixin.java:11` (NeoForge uses a second constant: `neoforge/.../ServerboundCustomPayloadPacketNeoForgeMixin.java`, `lambda$static$2`) |
| Serverbound custom query payload | 1,048,576 | `ServerboundCustomQueryPacketMixin.java:14` |
| Default NBT quota (one `readNbt` tag tree) | 2,097,152 bytes (2 MiB) | `NbtAccounterMixin.java:22` |
| Chunk packet data | 2,097,152 | `ClientboundLevelChunkPacketDataMixin.java:11` |
| Decompressed packet maximum | 8,388,608 (8 MiB) | `CompressionDecoderMixin.java:11` |
| String length (`readUtf`, `writeUtf`, identifiers) | 32,767 | `FriendlyByteBufMixin.java:11,16,21` |
| Packet frame length field | 3 bytes of VarInt = 21 bits = 2,097,151 bytes | `Varint21FrameDecoderMixin.java:29` (constant 3); README error "Length wider than 21-bit" [doc] |
| Network read timeout | 30 seconds | `ConnectionMixin.java:10` and `ServerConnectionListenerMixin.java:10` (constant 30) |
| Keep-alive | 15,000 ms | `ServerCommonPacketListenerImplMixin.java:11,15` |

PacketFixer's config defaults to "all sizes unlimited": `allSizesUnlimited = true` (`util/Config.java:9`), then every getter returns `Integer.MAX_VALUE` or `Long.MAX_VALUE` [v]. On Fabric it also cancels Fabric API's own payload `assertSize` (`fabric/.../PayloadHelperMixin.java`, `@Inject ... cancellable`, `ci.cancel()`) [v], which tells us that Fabric API has its own size assertion for payloads. This is the Fabric API networking check; the Fabric docs page on networking does not state a limit [doc, checked 2026-10-10].

**Krypton.** Replaces the compression pipeline with its own encoder and decoder, using libdeflate through JNI on Linux x86_64 and aarch64 only (`mixin/shared/network/pipeline/compression/ConnectionMixin.java:23-45`, `mod/shared/network/compression/MinecraftCompressEncoder.java`) [v][doc]. It also replaces the frame prepender, consolidates flushes on the server, and sends chunk packets in a spiral [doc] (`wiki/Home.md`). Everything stays wire compatible with vanilla [doc]. It optimises *how* packets go out, not *what* goes out. It does not fix an oversized packet.

### What this teaches Nublar's network design (B06, B08, B12, B14, B15, B16, P2)

What Nublar sends today (grep, read only):

- Three client-to-server packets: `UpdateEditInfoPacket` (writes a whole `DinoData` as NBT plus a block position), `UpdateIncubatorPacket`, `UpdateIncubatorSlotPacket` (`network/c2s/`) [v]. No server-to-client custom payload exists. All sync goes through vanilla paths: menus, block-entity update tags, and entity data.
- `DataSerializerInit.DINO_DATA` is an `EntityDataSerializer<DinoData>` that writes `buf.writeNbt(dinoData.toNBT())` (`init/DataSerializerInit.java:14-24`) [v]. It is used by `Dinosaur.DINO_DATA` as synched entity data (`entity/dinosaur/Dinosaur.java:106`) [v]. So the whole genome travels in the entity spawn data and in every dirty update of that field.
- `DinoData.toNBT()` writes `entityPercentages` (keys `entity_0`, `entity_1`, ... each a compound with type, variant and percentage) and a `genes` compound with one double per gene name (`api/DinoData.java:162-187`) [v]. The `layerColors` list is not in `toNBT` [v]; the plan already lists it as a fix (`project-plan.md:438`).
- `DiskStorageItem` keeps one compound per DNA entry in the root tag of the item stack; `DiskStorage.save` writes `synthedEntities` as one double per species and `tropicalFishColors` as an int array (`item/DiskStorageItem.java:21-26`, `api/DiskStorage.java`) [v]. There is no cap.

**Size estimates (all `[inf]`; my arithmetic, not measured).**

| Payload | Estimate | Reasoning |
|---|---|---|
| `DinoData` NBT per dinosaur | about 1 to 2 KB | 30 to 40 gene entries x about 30 bytes (UTF key + double + tag header) + 1 to 4 species entries x about 70 bytes. The plan has 8 donor species at most in code, 3 in design. |
| Same, for 300 tracked dinosaurs on join | 300 to 600 KB in total, in separate entity packets | Each entity is its own `ClientboundAddEntity` + `SetEntityData`, so each packet is small. No 1 MiB limit risk, but burst bandwidth on join. |
| A DNA drive stack, 100 vanilla species + 6 dinosaurs, one entry each | about 100 x 150 bytes = about 15 KB | The sequencer design (module 3) says the drive "also tracks how much of a species came from each biome". |
| Same with per-biome-group detail (8 groups) | about 100 x 8 x 30 = about 24 KB per drive, plus species headers | Still far under 2 MiB. |
| Container sync (every slot change resends the full stack) | the size above x number of slot updates | Hopper-fed processors that change a drive stack once per second send 24 KB per second per viewer. Real, but small. |
| A pathological drive: 10,000 entries | about 1.5 MB | Hits the 1 MiB custom-payload limit if sent in one payload and nears the 2 MiB NBT quota. Only mods that add thousands of mobs get here. |

Design rules (B06 sequencer, B07 and B08 gene editing):

1. **Keep DNA data on the item compact.** Use a data component with a `Codec` that stores a list of `(species id, percent as a short or a float, biome counts as bytes)`. Do not store gene doubles for each gene that can be derived. Do not store species by string key per entry if a registry id (VarInt) works for the network codec. Store percent as a single float. With these, the 100-species case is about 100 x 24 = 2.4 KB for the network form [inf].
2. **Use `StreamCodec` and data components, not `writeNbt`.** On 26.x, item data is data components, whose network form is a `StreamCodec`. A component with its own `StreamCodec` writes compact binary and does not go through the 2 MiB NBT accounting at all; a component stored as a raw `CompoundTag` does [inf]. For entity data, use a `StreamCodec`-based `EntityDataSerializer`. The vanilla API is `EntityDataSerializer.forValueType(StreamCodec)` in recent versions [inf; check the 26.2 signature].
3. **Never send NBT you can avoid.** A server-to-client custom payload should be a record with a `StreamCodec.composite(...)` of VarInts, shorts and floats. The Fabric docs page for networking shows exactly this pattern for payloads (`PayloadTypeRegistry.clientboundPlay().register`, `StreamCodec.composite`) [doc]. NeoForge uses `RegisterPayloadHandlersEvent` for the same [doc]. Nublar's three c2s packets must move to this API in P2 on both loaders; Balm wraps it [inf, check Balm's networking wrapper].
4. **Cap every list that a client can send.** The client sends `UpdateEditInfoPacket` with a whole `DinoData`. The server must not trust it: limit the species count to the donor slot cap (3), the gene count to the registry size, and validate every range (`project-plan.md:453` already asks for "validation rejects out-of-range and non-isolated edits"). The serverbound limit of 32,767 bytes (NeoForge uses its own constant) is a safety net, not a design target [v]. Do not ask PacketFixer-style mods to lift it.
5. **Do not sync what you can recompute.** The debug status plan (one need and one activity per animal, synced "when they change") is correct (`project-plan.md:668-670` region, B16). Keep it. The five float needs (`HUNGER`, `THIRST`, `STAMINA`, `SOCIAL`, plus `AGGRESSION`, `VISION`, `FERTILITY`, `DOMESTICITY`, `TAMING_SCORE` in `Dinosaur.java:366-370`) are each a synched field. Stamina is written once per 20 ticks (`Dinosaur.java:761`) [v], so each dirty float is one packet per second per animal: with 300 animals, 300 small packets per second, only to players who track them. That is fine. Do not move those writes to every tick [inf].
6. **Give `DinoData` an `equals`.** `SynchedEntityData.set` marks a value dirty only if it is not equal to the old one [inf, vanilla behaviour]. `DinoData` has no `equals` or `hashCode` (grep of `api/DinoData.java`) [v]. Any `set(DINO_DATA, copy)` sends the whole genome even when nothing changed. Today it is set on load and at one other place (`Dinosaur.java:356`, `:410`), so the cost is small; with growth driving the `scale` attribute and genes changing over time (B16) it can become a per-tick resend. A record or an explicit `equals` fixes it. Effort S.
7. **Spawn packet size.** The genome goes in the entity spawn data. With a hundred dinosaurs near a player at login, the server sends a burst. Krypton's flush consolidation and spiral chunk order help the server here [doc]. Nublar can help by not sending fields the client never reads (gene doubles for server-only genes such as immunity).
8. **Machine sync (B12).** Menus sync slot contents and data slots (`ContainerData`, 16-bit values per slot in vanilla). Power and progress are small ints; send them through `ContainerData`, not custom payloads. A processor's progress bar costs 4 bytes per change. Do not send the whole block entity NBT on each tick via `getUpdatePacket`. Send an update only when a visible value changes (animation state, `LIT` is already a block-state update).
9. **Fence power state (B14, B15).** A section toggle changes `powered` on many posts. Each `setBlock` with client update is one `ClientboundBlockUpdate`; vanilla bundles them into a `ClientboundSectionBlocksUpdate` per chunk section [inf]. Prefer one block-state change per post over a custom "section on/off" payload, and let the per-chunk bundling handle it. For wire health (B15), do not sync every hit; sync when a wire breaks and let the client render the broken half from the block entity's saved state.

**Compression.** Compression threshold is a server setting (`network-compression-threshold`, default 256 [inf]). Nublar packets are small and benefit little. Krypton's libdeflate helps large chunk or NBT payloads; it works only on Linux x86_64 and aarch64 (`wiki/Home.md`) [doc], so a Windows client or server gets the Java `Deflater` path [doc]. Nublar must not assume either. Do not set a custom compression threshold in Nublar.

**Compatibility duty.**

- Register all payloads with fixed ids and codecs, through the platform API, so that Krypton's wire-compatible pipeline sees ordinary packets [inf].
- Never read from or write to `Connection`'s Netty pipeline, never add a channel handler. Krypton replaces `compress`, `decompress` and the frame prepender handlers by name (`ConnectionMixin.java:23-45`) [v]. A Nublar handler with the same name would break.
- Do not send a payload larger than 32,767 bytes from the client or 1 MiB from the server. If PacketFixer is installed it hides the error. If it is not installed, the player is kicked with "Packet too big" or "Payload may not be larger than..." [doc].
- Do not depend on PacketFixer being present.

**Platform overlap.** NeoForge and Fabric API both give payload registration with `StreamCodec` [doc]. Vanilla gives `StreamCodec`, data components, `ContainerData` and `SynchedEntityData`. There is nothing to build.

**Verdict.** Krypton: Not relevant to code, Compat (test only: a Linux dedicated server with Krypton, once). PacketFixer: Avoid (do not depend on it; do not need it). The size rules above: Adopt. Effort: S for the rules, M for the P2 port of the three c2s packets and the data serializer.

---

## 6. memoryLeakFix: leak patterns and the Nublar grep

**In game.** Less memory growth over a long play session, especially after leaving and re-entering worlds. On 26.x the repo is stale: it targets 1.20.4 (`gradle.properties`) and its wiki lists 7 fixes, "Only 2 affect 1.20.4+" [doc] (`Current-Fixes.md`). The remaining two:

- **Biome temperature cache** (`mixin/biomeTemperatureLeak/Biome_threadLocalMixin.java`): the vanilla `ThreadLocal` was an instance field, so one `ThreadLocal` and one map per biome per thread. The fix makes it static [v]. Wiki status: "No mojira ticket - Not Fixed" [doc] (as of 1.20.4).
- **Target entity leak** (`mixin/targetEntityLeak/Minecraft_targetClearMixin.java:37`): `Minecraft.crosshairPickEntity` and `hitResult` are set to `null` each frame before `runTick`. Reason: they keep the last entity alive, and with it the whole world, after disconnect. Wiki: MC-101260 "Not Fixed" [doc].
- The fixed-in-vanilla list: entity brain memories (MC-260605, fixed 1.20), `TagKey` interning (MC-248621, fixed 1.19), Drowned navigation (MC-202246, fixed 1.17), `TextureUtil` buffer (MC-226729, fixed 1.19.4), and the custom-payload buffer (MC-121884, fixed 1.20.2) [doc].

ModernFix covers the same problem space with different patches on 26.1: it clears client level data at disconnect (`bugfix/world_leaks/MinecraftMixin.java:26-42`) and the data-component encoder cache (`perf/encoder_cache_leak/MinecraftMixin.java:18-26`: the cache "retains strong references to registries") [v]. It disables its own packet-leak fix when memoryLeakFix is present (`ModernFixEarlyConfig.java:328`) [v].

### Leak patterns this teaches (all are static or long-lived references to a short-lived object)

1. A `static` field or a registry-level map that holds a `Level`, `ServerLevel`, `Entity`, `BlockEntity`, `Screen` or `Minecraft` object.
2. A weak map whose *value* refers to its *key* (`WeakHashMap<ServerLevel, Manager>` where `Manager` holds the level). The key never becomes weakly reachable.
3. A client cache keyed by entity id, UUID or `BlockPos` that is never cleared at disconnect or at entity removal.
4. A `DynamicTexture` or `NativeImage` registered with the `TextureManager` or created per screen open and not closed.
5. A reload listener or event listener that captures a world in a lambda and is never unregistered.
6. A `ThreadLocal` that holds a big object and is never removed.

### Grep of the Nublar repo (read only; `common/src/main/java` and `forge/src`)

| Item | Finding | Verdict |
|---|---|---|
| `entity/ai/behaviour/social/GroupManager.java:34` | `private static final Map<ServerLevel, GroupManager> BY_LEVEL = new WeakHashMap<>()`. The javadoc at `:30-33` states the rule: "A value must never reach its level (no level field, no entity), or the key stays strongly reachable and the map leaks." `DinoGroup` javadoc (`:22`) also says it holds no entity and no level. `FallbackBudget` holds `UUID` keys only. [v] | **Correct.** This is the pattern memoryLeakFix teaches. Keep the rule, and add a unit test that reflects over `GroupManager` and `DinoGroup` fields and fails if a field type is `Level`, `Entity` or a subclass. Effort S. |
| `GroupManager` UUID maps | `groupByMember` is a per-level `HashMap<UUID, DinoGroup>`; `DinoGroup.spots` and `FallbackBudget.lastRequest/lastGranted` are `HashMap<UUID, ...>`. They are cleared by `GroupManager.leave` on death or discard (javadoc `:56`). `FallbackBudget.lastRequest` and `lastGranted` are written per member per request. I did not find code that removes the entry when the member leaves (`removeIf` at `FallbackBudget.java:34` only prunes the queue, not the two maps) [v] | **Slow leak.** One entry per dinosaur that ever asked for a fallback path, per group, for the life of the server session. Small (two longs per UUID) but unbounded as dinosaurs die. Add `forget(UUID)` to `FallbackBudget` and call it from `DinoGroup.remove`. Effort S. |
| `block/api/MultiEntityBlock.java:35` | `public static Map<BlockState, VoxelShape> SHAPES = new HashMap<>()`, filled by `computeIfAbsent` inside `getShape` (`:83`). Bounded (at most 48 states x 4 machines = 192 entries) [v] | **Not a memory leak; a thread-safety bug.** `getShape` runs on the client thread and on the server thread at the same time in a single-player world, and on other threads in collision and pathing code in some mods. `HashMap.computeIfAbsent` under concurrent access can throw `ConcurrentModificationException` or loop (the exact bug ModernFix patches in Refined Storage and Cyclic, section 3). Also `public` and mutable. Compute shapes once in the constructor or in a per-state field, or use a `ConcurrentHashMap`. Better: build an `ImmutableMap<Integer, VoxelShape>` keyed by the property-index of the state in `registerDefaultState`. Effort S. |
| `block/api/MathUtils.java:121,133` | `private static final Map<TriVec, Vec3> NORMAL_CACHE = Maps.newHashMap()`, filled by `calculateNormal` called by `calculateNormalF`, which `RenderUtils.drawSpacedCube` calls three times per drawn cuboid (`block/api/RenderUtils.java:61-63`) [v]. The key is a record of three `Vec3` objects. | **Unbounded growth and an allocation per call.** One entry per distinct triangle ever drawn, never evicted, never cleared at disconnect. A cross product costs less than building the key (three `Vec3` plus the record plus a hash). Remove the cache; compute with plain floats. This is the fence wire render path (B15), which runs for every visible wire every frame. Effort S. |
| `client/DinoTexture.java:18` | `private static final Set<UUID> usedIds = new HashSet<>()`; grep finds no other use of `usedIds` and no registration of `DinoTexture` anywhere (`new DinoTexture`, `DinoTexture.create`: not called). `DinoTexture` holds a `ByteBuffer dataRef`. | **Dead code with a latent leak.** The plan's B25 already lists `DinoTexture` for deletion (`project-plan.md:852`). Delete it. |
| `client/widget/ColorWheelWidget.java:45,110` | `Minecraft.getInstance().getTextureManager().register(COLOR_WHEEL, makeColorWheel(currentValue))` creates a 400 x 400 `NativeImage` (640,000 bytes of native memory) per widget construction, via `new DynamicTexture(buffer)`. `register` with an existing id closes the old texture in vanilla [inf], so repeated opens do not leak if the id is fixed. | **Check.** If the screen is opened often and the brightness slider re-registers per change, each call allocates 640 KB native. Create one texture, and update its pixels in place (`DynamicTexture.getPixels()` then `upload()`), or draw the wheel with a shader-free quad gradient. Effort S. |
| `client/model/FencePostModel.java:14-16` | Three static `HashMap<ResourceLocation, ResourceLocation>` caches, keyed by block id. Bounded by the number of fence-post blocks. | Fine. They hold only identifiers. Called from the render thread. Use a `ConcurrentHashMap` if GeckoLib ever calls from a second thread. |
| `client/CommonClientClass.java:23-30` | `GENE_WIDGET`, `LAYER_REGISTRY`, `LAYER_REGISTRY_NEW`, `MALE_LAYER_REGISTRY`: static `HashMap`s filled at client setup, keyed by `EntityType` or string. | Fine for leaks. They are read on the render thread while client setup may run on the loading thread. Fill them before first render and treat them as read-only after (the `DinosaurRenderer.createLayers` rework in B-modules already touches this). |
| `data/*ReloadListener.java` static maps | Replaced by assignment of a new map on reload (`BehaviourDataReloadListener.java:23,33`). | Good pattern. Add `volatile`. |
| `api/FossilCollection.java:20-22`, `ProjectNublar.java:15`, `api/Genes.java:15`, `api/GeneData.java:13` | `public static` mutable `HashMap`/`Multimap` registries filled during class loading or datagen. They hold `Block`, `EntityType` and `Gene` objects only, no world objects. | No leak. A thread-safety risk for worldgen if read during generation (section 4). |
| `ThreadLocal` use | none in `common/src` or `forge/src` (grep) [v, negative grep] | Good. |
| `DinoAnimationLengths.java:61` | `synchronized (SECONDS)` around a map read/fill. | Good; the only synchronised static cache in the repo. |

**Lesson for Nublar** (B25, B16, B15, B19).

- Keep the `GroupManager` rule and make it a test. Apply the same rule to every new per-level store: power networks (B14), fence graphs (B15), feeder registries (B20), vehicle track data (B22). Key by `ServerLevel` in a `WeakHashMap`, or better attach the data to the level through the platform (NeoForge data attachments on the level, Fabric attachments, or vanilla `SavedData` via `DimensionDataStorage`) so the lifetime is tied to the level object [inf]. `SavedData` also gives persistence.
- Never cache an `Entity` or `BlockEntity` in a static field. Cache the `UUID` or `BlockPos` and look it up.
- Clear client caches on `ClientPlayerNetworkEvent.LoggingOut` (NeoForge) or `ClientPlayConnectionEvents.DISCONNECT` (Fabric API) [doc, names to be confirmed in P2].
- The vanilla `crosshairPickEntity` leak is vanilla's. Nublar's hitbox parts (B27) are `PartEntity` instances that point to their parent dinosaur (`entity/dinosaur/PartEntity.java` has a `PARENT_UUID` synched field); keep it as a UUID, not a reference, so part entities do not keep the parent alive [v: field is `Optional<UUID>`].

**Compatibility duty.** None. memoryLeakFix does not need Nublar changes. Nublar must not rely on it.

**Platform overlap.** NeoForge data attachments, Fabric data attachments and vanilla `SavedData` replace the need for hand-made per-level static maps for persistent state. Vanilla fixed most of the leaks on the list.

**Verdict: Not relevant as a dependency; Adopt (pattern) as a checklist. Effort: S for the grep fixes (four items), S for the test.**

---

## 7. lazydfu: status

**In game.** On old versions the game started about 58 seconds faster on a slow laptop: the README reports "~58 seconds spent initializing DFU" versus "498 milliseconds" with LazyDFU on an i5-8250U [doc]. It delays building DataFixerUpper conversion rules until a conversion is needed, so a lag spike can appear later.

**Mechanism and status.** The repo targets Minecraft 1.19 (`gradle.properties:5`), last commit 2022-06-07 (`a83ce2f`), last push 2024-01-13, not archived [v]. The README says it complements Cadmium [doc]. It has not been updated for any version after 1.19.

**26.x status (inference).**

- ModernFix's 26.1 tree has a `dynamic_dfu` mixin package, but it no longer delays DFU creation. `DataFixersMixin` only starts a `DFUBlaster` cleaner thread after `DataFixerBuilder.build()` returns (`common/mixin/perf/dynamic_dfu/DataFixersMixin.java`, `dfu/DFUBlaster.java:11-60`). The cleaner thread clears three DFU cache maps (`TAGGED_CHOICE_TYPE_CACHE`, `HMAP_APPLY_CACHE`, `REWRITE_CACHE`) 60 seconds after DFU last ran [v]. So the 26.1 version of the idea is a memory fix, not a startup fix. The `build()` call returns a `Result` object (vanilla's current DFU API), which suggests vanilla already defers the expensive rule optimisation [inf, not verified in decompiled vanilla].
- Nublar has no data fixers of its own (no `DataFixer`, `Schema` or `DataFixesInternals` in `common/src`, grep) [v, negative grep]. Its saved data (block entity NBT, `DinoData`, drive items) has no version number in the NBT. That is a P2 and Beta risk, not a DFU risk: when the format changes (the plan changes `DinoData` to add `layerColors`, base species, and variation), old saves read with defaults. Add a `"version"` int to every custom NBT root now (S), so a later `DataFixer` or a simple `if (version < 2)` step is possible [inf].

**Lesson for Nublar.** None for startup. One for data: a version tag.

**Compatibility duty.** None.

**Platform overlap.** Vanilla handles DFU timing. No Nublar action.

**Verdict: Not relevant.** The version-tag advice goes to B25 or P2 (effort S).

---

## 8. Stfu: what it silences, and what Nublar must not log

**In game.** Stfu removes annoyances: the world advice screen, the tutorial, the narrator "jump scare", the multiplayer warning, the chat-signing toast, telemetry, particle culling, and many load-time options (`README.md`, `wiki/Features.md`) [doc]. It adds a config screen through YACL and Mod Menu. It is a client mod, Fabric only.

**What it silences in the log.** One thing: `NoNarratorError` redirects `Logger.error(String, Throwable)` inside `Narrator.getNarrator` to a no-op, "Suppresses the huge error when Minecraft can't load the narrator" (`src/main/java/stfu/mixin/NoNarratorError.java`; `wiki/Features.md`) [v][doc]. That is the only log silencer in the list. Every other feature changes a setting or UI. Other log noise that mods cause, such as `System.out.println` in a tick path, is not touched by Stfu. The ModernFix wiki's `mixin.feature.log_stdout_in_log_files` (default true, `ModernFixEarlyConfig.java:269`) goes the other way: it *writes* stdout into the log files [v].

Other Stfu features with an effect on Nublar (all `[doc]`, `wiki/Features.md`): `rendering.ParticleCulling` ("stops rendering particles you can't see" - the same idea as Particle Core, section 9), `rendering.DisableTextureAnimation`, `ThreadPriorities`, `RemoveOverlay` (lets the player use the game while resources reload), `Tooltips` (wraps long lines), `chat.CommandLengthLimit` (removes the 256-character command limit). The `Tooltips` feature matters for the DNA drive tooltip (B06), which can be long; Nublar must not depend on it and must wrap its own text with `Font.split` [inf].

**What Nublar logs today (grep, read only).**

| Location | Statement | When it runs | Verdict |
|---|---|---|---|
| `entity/ai/sensors/NearestFeederSensor.java:68` | `System.out.println("Feeder found by dinosaur at :" + nearestFeeder)` | Every time the sensor runs and finds a feeder. SmartBrainLib sensors run on a timer (default 20 ticks) per dinosaur, so every dinosaur next to a feeder prints once per second [inf] | **Log spam.** With 100 dinosaurs near feeders: 100 lines per second, 6,000 per minute. Remove. |
| `entity/ai/tasks/WalkToNearestFeeder.java:63` | `System.out.println("Walk target set to feeder.")` | Each time the task starts | Spam during feeding. Remove. |
| `entity/ai/behaviour/needs/SoloHuntingBehaviour.java:88` | `System.out.println("Entity: " + entity + ", has found target: " + this.toTarget + ...)` | Each hunt start. Builds the entity `toString` (a long string) for each call | Spam + string cost. Remove. |
| `entity/ai/behaviour/social/LargeCarnivoreFight.java:47,101,105` | `System.err.println("turf war started")` etc. | Each fight event | Remove. |
| `util/DinoNeedsUtils.java:163-167`, `:250` | `System.err.println("Days without food: ...")`, `"Starvation time exceeds maximum of days!"`, `"Hunger reached zero"`, `System.err.println(foodItem)` | Starvation and eating | `:250` prints the food item each time an animal eats. Remove. |
| `data/BehaviourDataReloadListener.java:107,112` | `System.err.println("Failed to parse ... {} ..." + ...)` and `System.out.println("Finished ... {} ..." + ...)` | Once per reload | Wrong: the `{}` placeholders are not formatted (it uses `+`). Use the SLF4J logger. |

Total: 12 statements, 6 files (grep count). The plan already has this: B25 acceptance is `grep -rn "System.out\|System.err\|printStackTrace" common/src` returns nothing (`project-plan.md:857`). The sensor and walk-task lines are the ones that run at scale, so they should go in B17 and B18 (needs and feeding) rather than wait for B25 [inf].

**Rules for Nublar logging** (B25):

1. No log call in a per-tick, per-entity, per-block-entity or per-render path, at any level, except behind a debug flag that is off by default and that is checked before building the message.
2. Use SLF4J with `{}` placeholders so that the string is built only when the level is on. Never concatenate with `+` in a log call.
3. Log on *state change* (a dinosaur started to starve), not on *state* (a dinosaur is starving).
4. Rate-limit by a counter for repeated errors: log the first and a count every N ticks.
5. One info line per feature at startup is fine ("loaded 6 species"). Nothing per entity.
6. Do not print an `Entity` or `BlockEntity` in a log line in a hot path. `Entity.toString` builds a long string.
7. Debug output for players goes through the debug stick action bar to the holder only, as B16 plans (`project-plan.md:667-668` region), not through chat or the log.

**Compatibility duty.** None. Do not depend on Stfu to hide log noise, and do not add a log line that Stfu cannot hide.

**Platform overlap.** Vanilla SLF4J logging. Nothing to build.

**Verdict: Not relevant as a dependency. Adopt the logging rules. Effort: S.**

---

## 9. Particle Core (`fzzyhmstrs/pc`): client particle culling and async ticking

**What it is.** A client-only Fabric mod for particle performance, name "Particle Core" (`src/main/resources/fabric.mod.json`, `"environment": "client"`) [v]. It requires Fzzy Config and Fabric Language Kotlin and Minecraft 26.1 or newer [v]. README features [doc]: particle culling by view frustum, rendering optimizations (vertex transform and light lookups; vertex work "defer[s] to Sodium"), and by-type reduction or disabling (a tag `particle_core:excluded_particles`, a config map of particle id to spawn chance, vanilla `ALL`/`DECREASED`/`MINIMAL` tuning dials).

**In game.** A big fire, rain of sparks or many dinosaur footstep particles stays smooth; particles behind the player are not drawn.

**Mechanism.**

- **Culling.** A wrap on `Frustum.pointInFrustum` inside `QuadParticleGroup.extractRenderState`: a particle outside the frustum is skipped unless its class is on a blacklist (`mixins/ParticleGroupFrustumMixin.java`, `particle_core_isBlacklisted()`) [v]. The `Particle` class gets a new boolean field, `FrustumBlacklisted` (`mixins/ParticleFrustumBlacklistMixin.java`) [v]. A particle with an unusual render path can opt out.
- **Spawn control.** `ParticleEngine.createParticle` returns `null` if the type is in the tag, the global disable is on, or the chance roll fails (`mixins/ParticleEngineTypeMixin.java`) [v]. Nublar particles can be thinned by a player through a data tag without a Nublar change. Good.
- **Async ticking, an option.** `ParticleEngineAsyncMixin` ticks a particle group with `parallelStream()` when the group has more than 0.35 x `maxParticlesPerSheet` (default 16,384) particles (threshold at `:71-77`, the stream at `:111`) [v]. A particle whose tick throws a world-random or threading-detector error is added to an `unsafeParticles` set by class and is ticked on the main thread from then on (`:118-131`, `:153`). If more than two thirds of a group fail, the mod turns async ticking off and logs an error (`:159-160`, "disabling") [v].

**Lesson for Nublar** (B25, B22 vehicles, B15 fences, B16 dinosaurs).

1. **Particles must tick without world access that is not thread safe.** The detector message strings it matches are "Accessing LegacyRandomSource from multiple threads" and "ThreadLocalRandom accessed from a different thread" (the C2ME message, section 4) [v]. A Nublar particle that calls `level.getRandom()` in `tick()` is demoted to the main thread; that costs nothing but disables the speed-up for it. A particle that reads blocks (`level.getBlockState`) or entities is a data race when ticked async. Rule: particle `tick()` uses its own `RandomSource` (`this.random`, which `Particle` already has), reads no entity, and reads blocks only through the cached position the base class supplies [inf].
2. **Spark particles on live fence wires (B15) will be many.** A fence of 200 posts with sparks at each break is a particle storm. Use the vanilla `ELECTRIC_SPARK` or a lightweight custom type, spawn on events (shock, break), not per tick, and keep `lifetime` short [inf]. Players with Particle Core can thin them with the tag.
3. **Footstep and dust particles for large dinosaurs.** One burst per footfall event, count scaled by the animal's size, with a per-species config cap. No per-tick particle spawn per animal.
4. **Do not rely on culling by the mod.** Culling by view frustum is cheap for the renderer to do itself; the lesson is to give custom particles a correct bounding position (`setPos`), so the frustum test is valid.

**Compatibility duty.**

- Give custom particles a valid position and a short finite lifetime; do not override `getRenderType` with a custom type that bypasses the standard quad group (Particle Core only culls `QuadParticleGroup`), unless there is a reason. Use the vanilla particle sheets.
- Do not put Nublar particle types in the `particle_core:excluded_particles` tag. It is for the player.
- Test once with Particle Core and async ticking on, in a scene with many Nublar particles. A crash or a "disabling" log line from `ParticleEngineAsyncMixin` names the offender.

**Platform overlap.** Vanilla particles and the vanilla `/particle` command; Fabric API particle factory registration; NeoForge `RegisterParticleProvidersEvent`. Particle options per player are a vanilla setting. Nothing to build.

**Verdict: Compat (test only) and Adopt (the particle rules). Effort: S.**

---

## 10. Cross-cutting summary: what these ten mods say about Nublar's hot spots

The earlier survey (`reports/Reference mod survey for Beta.md`) found the entity paths. These mods add memory, loading, thread and network checks that touch other modules. Labels reuse that report's style.

| Cost or risk | Where in Nublar | Mod that reveals it | Action |
|---|---|---|---|
| Block-state count explosion if cosmetic randomness goes into state | B01 fossil ore (tint, opacity, rotation) | FerriteCore | Keep look from position seed; cap states at 32 per block (section 2) |
| About 3,000 registered fossil blocks in the old scheme | `FossilCollection.create` | ModernFix, FerriteCore | One block per host stone with properties; decide where species and piece live |
| Thread-unsafe statics read during worldgen | `FossilCollection`, `WEIGHTED_PERIOD_BIOME_FOSSIL_MAP` | C2ME | Immutable maps built on the main thread |
| `new Random()` and level random in features | old fossil feature | C2ME `CheckedThreadLocalRandom` | Use the generator's `RandomSource` |
| `HashMap.computeIfAbsent` inside `getShape` | `MultiEntityBlock.SHAPES` | ModernFix shape-cache patches | Precompute or `ConcurrentHashMap` |
| Block entity dependent collision shape | `BlockConnectableBase.getShape` | ScalableLux (light), C2ME (off-thread reads) | Keep `useShapeForLightOcclusion` off on wires; never read the BE from light or worldgen paths |
| Fence post glow uses `getLightBlock` (dampening) instead of `lightLevel` (emission) | `ElectricFencePostBlock.java:273-276` | ScalableLux (reads both separately) | Move to `lightLevel`; keep it a pure function of state; toggle only on change |
| `writeNbt` of the whole genome | `DataSerializerInit.DINO_DATA`, `UpdateEditInfoPacket` | PacketFixer limits | `StreamCodec`, compact encoding, server-side validation |
| `DinoData` has no `equals` | `Dinosaur.DINO_DATA` | Krypton/PacketFixer (network cost) | Add `equals` or make it a record |
| Unbounded static `HashMap` per drawn triangle | `MathUtils.NORMAL_CACHE`, `RenderUtils.drawSpacedCube` | memoryLeakFix patterns | Remove the cache |
| 12 `System.out`/`System.err` lines, 3 in per-second paths | sensors, tasks, needs | Stfu (shows what players silence) | Remove; log rules |
| UUID maps never pruned | `FallbackBudget.lastRequest/lastGranted` | memoryLeakFix patterns | `forget(UUID)` on leave |
| Missing version tag in custom NBT | `DinoData`, drives | lazydfu / DFU status | Add `"version"` ints |
| Reload listeners from setup events | P2 port of three listeners | ModernFix wiki | Use platform reload-listener events |

---

## 11. Closing table

| Technique | Source | Nublar module | Verdict | Effort | Expected effect |
|---|---|---|---|---|---|
| Keep cosmetic randomness out of block state; derive look from position seed | FerriteCore (state math), plan line 292 | B01 | Adopt | S | Avoids 1.5 to 5.9 million states in the fossil set (design B or C); keeps 32 states per block |
| Cap state count per block and unit-test it | FerriteCore `FastMap` formula | B01, B12, B15 | Adopt | S | Guard for future property additions |
| Collapse 3,000 fossil blocks into host-stone blocks with properties | FerriteCore + ModernFix | B01 | Adopt | M | Fewer registry entries, models, items, lang keys; faster startup |
| Use only `IntegerProperty`/`BooleanProperty`/`EnumProperty` | FerriteCore `FastMap.java:20-24` | all blocks | Adopt | S | Avoid startup crash with FerriteCore |
| No identity compare or mutation of `VoxelShape` | FerriteCore `BlockStateCacheImpl` | B15, B12 | Adopt | S | Compatibility with shape dedup |
| Test client with `dynamic_resources` and `dynamic_entity_renderers` on NeoForge | ModernFix | P2, B25 | Compat | S | Finds renderer or model setup that needs startup state |
| Register reload listeners through platform events | ModernFix wiki | P2 | Adopt | S | No race with vanilla reload |
| Immutable maps for worldgen data; no lazy caches in features | C2ME `MixinRandomizedIntBlockStateProvider` | B01, B03, B21 | Adopt | S | Thread safety under C2ME and vanilla |
| Only generator `RandomSource`; position hash for per-ore look | C2ME `CheckedThreadLocalRandom` | B01 | Adopt | S | No crash with `enforceSafeWorldRandomAccess` |
| Place only inside the region; no drops; no off-region block-entity reads | C2ME `readonly_protection`, `MixinChunkRegion` | B01, B03, B21 | Adopt | S | No warnings, no chunk deadlocks |
| Run a 200-chunk generation on Fabric 26.2 with C2ME | C2ME | B01 | Compat | S | Finds thread bugs early |
| Light from block state only via `lightLevel`; fix fence post `getLightBlock`; toggle `LIT` on change only | ScalableLux `BlockStarLightEngine:94,136` | B12, B14, B15, B20 | Adopt | S | Correct light; fewer light updates; works with the threaded light engine |
| `StreamCodec` payloads and data components; no `writeNbt` of the genome | PacketFixer limits, Fabric and NeoForge docs | P2, B06, B08, B16 | Adopt | M | Compact packets; no 2 MiB NBT or 1 MiB payload risk |
| Server-side validation and caps for client packets | PacketFixer limits (32,767 serverbound) | B08, B11 | Adopt | S | No oversized or hostile packet |
| `equals` on `DinoData` | vanilla `SynchedEntityData` behaviour [inf] | B16 | Adopt | S | No resend of unchanged genome |
| Compact DNA drive component (registry ids, float percents) | PacketFixer sizes, my estimates | B06 | Adopt | M | 100-species drive about 2.4 KB on the wire instead of about 15 to 24 KB |
| Never touch the Netty pipeline | Krypton `ConnectionMixin` | all | Adopt | S | Compatible with Krypton's replaced handlers |
| Linux server test with Krypton | Krypton | B25 | Compat | S | Confirms no pipeline conflict |
| Do not depend on PacketFixer | PacketFixer | all | Avoid | S | Keep packets inside vanilla limits |
| Per-level data keyed weakly or attached to the level; no world refs in values | memoryLeakFix, `GroupManager` | B14, B15, B19, B20, B22 | Adopt | S | No world leak after disconnect |
| Reflection test: group and manager classes hold no `Level`/`Entity` | memoryLeakFix pattern | B19 | Adopt | S | Guards the `GroupManager` rule |
| Remove `MathUtils.NORMAL_CACHE` | memoryLeakFix pattern | B15 | Adopt | S | No unbounded map; fewer allocations per wire cuboid per frame |
| Fix `MultiEntityBlock.SHAPES` | ModernFix shape-cache patches | B12 | Adopt | S | No race between client and server threads |
| `FallbackBudget.forget(UUID)` | memoryLeakFix pattern | B19 | Adopt | S | No slow map growth |
| Delete `DinoTexture`; fix `ColorWheelWidget` texture reuse | memoryLeakFix pattern | B25 | Adopt | S | Removes dead code and 640 KB native allocation per open |
| Add `"version"` to custom NBT | lazydfu / DFU status | P2, B25 | Adopt | S | Allows format migration later |
| Remove 12 `System.out`/`err` lines; logging rules | Stfu (shows player-side silencing), plan B25 | B17, B18, B25 | Adopt | S | No log spam at 100 animals |
| Particle rules: own `RandomSource`, no entity reads, event-based spawn | Particle Core async ticking | B15, B16 | Adopt | S | Allows async particle ticking to keep Nublar particles |
| Test with Particle Core async ticking on | Particle Core | B25 | Compat | S | Finds unsafe particle classes |
| Lazy DFU | lazydfu | n/a | Not relevant | n/a | n/a |
| Copy FerriteCore, ModernFix, C2ME or ScalableLux code | all | n/a | Avoid | n/a | Not needed; rules suffice |

---

## 12. Open questions (real design gaps only)

1. **Where do species and piece of a fossil live: in the block, in the state, or in a block entity?** The plan names `QUALITY` and `BIOME_GROUP` as properties but not species or piece (`project-plan.md:300`). The choice sets the block count: one block per (host x species x piece) is about 3 x 6 x 8 = 144 blocks at 32 states each (4,600 states); one block per host with `SPECIES` and `PIECE` properties is 3 blocks at 6 x 8 x 32 = 1,536 states each (4,600 total, same state count, far fewer registry entries). A block entity per ore adds an object per ore block in the world. Which one does the lead want? The drop (B02) needs both values.
2. **How many opacity variants are acceptable for the fossil overlay?** Tint can come from a tint index at no cost. Opacity 15 to 100 percent in a continuous range needs either a custom render path (which conflicts with Sodium and Iris model handling) or a fixed set of overlay textures. Is 3 or 4 steps good enough for "random opacity"?
3. **Is the genome (`DinoData`) allowed to shrink for the network?** Server-only genes (immunity, behaviour thresholds) need not reach the client. Does any client screen (the gene editing preview in B08) need all values, or only the base species, size and colours?
4. **Is there a target for the largest DNA drive?** Drives have "no capacity limit" (module 3). The design should state an expected maximum (for example 100 species) so that the wire format and the tooltip can be sized and tested. A drive with 10,000 entries would reach the 1 MiB payload limit.
5. **Do fence wires keep their collision shape in the block entity on 26.2?** Several tools assume shape is a function of state (light, shape caches, culling). If the lead keeps connections in the block entity, B15 must say which shape queries are allowed to touch it, and the light flag must stay off. Otherwise the connections need a small bounded state (for example 4 horizontal booleans plus tier), which is 16 to 32 states per post and would make the shape a pure function of state.
6. **Should Nublar publish a "no-world-reference" rule as a project test?** A single reflection test over `entity/ai/behaviour/social` is easy. The question is whether the lead wants it as a gate on all per-level stores (B14, B15, B20, B22), or only for the herd code that exists today.
