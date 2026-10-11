# Chunk and render pipeline replacements, and client frame-time mods: what Nublar must do

Scope: Sodium, Embeddium, Chloride, Sodium Extra, Nvidium, VulkanMod, ImmediatelyFast, GPUBooster, Ixeris, Dynamic FPS. Read for Project Nublar's port to Minecraft 26.2 on Fabric and NeoForge (plan module P2), with attention to the fence renderers (B15), machine renderers (B12), GeckoLib 5 dinosaurs and colour layers (B09, B16) and the cleanup pass (B25).

Labels: `[v]` verified in source (path and lines at the commit named in section 0), `[doc]` README, wiki, changelog or store page, `[inf]` my inference. Any performance number that is not `[v]` is an estimate. Hot-path ids C1 to C7 come from `reports/Reference mod survey for Beta.md` ("Client hot paths"). New ids R1 to R9 in this note extend that table.

Method note: all clones are shallow and read-only. Nothing was built or run. Extra reads outside the ten named repos, used only to check "does the platform already do this": GeckoLib `26.2` branch (cloned), NeoForge `26.2.x` and Fabric API `26.2` (read through `gh api`, not cloned), Modrinth project and version metadata (store pages).

---

## 0. Repos read

| Repo | Branch read | Commit | Commit date | Licence (repo fact) | Loaders | MC versions | Size (GitHub, KB) | Status |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| CaffeineMC/sodium | `dev` (26.3 line). Also `26.2/stable` | `8aa723c` (dev). `9c69d26` (26.2/stable, 2026-09-20, read by API only) | 2026-09-30 | `LICENSE.md` present. GitHub reports NOASSERTION. `neoforge.mods.toml:3` names Polyform-Shield-1.0.0 | Fabric, NeoForge (Quilt on Modrinth) | Modrinth lists up to 26.3. Latest 26.2 release `mc26.2-0.9.2` (2026-09-11). Branches for 1.20.1 through 26.3 | 10,669 | Active. Releases every few weeks. Stable branch per MC version |
| FiniteReality/embeddium (the former embeddedt repo) | `21.4/neoforge` (default) | `aa6657d` | 2025-02-01 | LGPL-3.0 | Forge, Fabric, NeoForge | Branches 1.16.5 to 1.21.4. Modrinth lists up to 1.21.4 (last upload 2025-01-24, `1.0.15+mc1.21.1`) | 11,182 | No 26.x branch and no 26.x upload `[doc]`. Not usable on 26.2. Relevant to today's 1.20.1 build only |
| SrRapero720/chloride | `26.2.x-neo` (also `1.21.1-neo` default) | `579043b` (26.2.x-neo). `0ed94c9` (1.21.1-neo) | 2026-07-19 | No SPDX licence. `gradle.properties:22` says All-Rights-Reserved | Fabric, NeoForge (Forge on older MC) | 1.20.1, 1.21.1, 1.21.5, 1.21.11, 26.1.2, 26.2 | 6,080 | Active. Last push 2026-07-23. Sodium addon, not stand-alone |
| FlashyReese/sodium-extra | `26.3/dev` (also `26.2/stable`) | `b0bbae5` (26.3/dev). `658cbab` (26.2/stable, 2026-09-16, API only) | 2026-10-03 | LGPL-3.0 (`LICENSE.txt`) | Fabric, NeoForge (Quilt) | `mc26.2-0.9.4` released 2026-09-16 | 4,807 | Active |
| MCRcortex/nvidium | `26.2` (default `dev` is stale, 2024-08-20, MC 1.21) | `bda9ff4` (26.2). `f2028b2` (dev) | 2026-09-16 | LGPL-3.0 (`LICENSE.txt`) | Fabric only (Quilt) | 26.1.1 to 26.3. `mod_version=0.4.4-beta6-26.2` | 836 | Active, beta. Needs an NVIDIA GTX 1600 or newer `[doc]` |
| xCollateral/VulkanMod | `dev` | `087ef24` | 2026-08-10 | LGPL-3.0 | Fabric only (Quilt) | Source on `dev` is 1.21.11. Modrinth lists 1.21.11, 26.1, 26.1.1, 26.1.2. No 26.2 | 9,122 | Maintained, behind current MC. Last Modrinth update 2026-06-14 |
| RaphiMC/ImmediatelyFast | `26.3` (a `26.2` branch exists, `475e3f1`, 2026-10-08) | `a756fba` | 2026-10-08 | LGPL-3.0 | Fabric, NeoForge (Forge, Quilt on older MC) | 26.1 to 26.3 on Modrinth. Release `v1.16.0` (2026-06-18) added 26.2 | 3,052 | Active. Version line per MC version |
| ITsMrToad/GPUBooster | `GPUBooster-1.21.1-fabric` | `5ed54b4` | 2025-12-06 | GPL-3.0 | Fabric only | 1.21.1. Other branches are `GpuTape-*` for old MC | 217 | Dormant. No 26.x |
| decce6/Ixeris | `master` | `bc46aae` | 2026-10-09 | LGPL-3.0. The API package is 0BSD (`API_DOCS.md`, `IxerisApi.java:2-3`) | Fabric, Forge, NeoForge (Quilt) | `versions/` holds 1.16.5 to 26.3 | 1,277 | Active. Changelog top entry 4.6.9 |
| juliand665/Dynamic-FPS | `main` | `2152ae3` | 2026-09-28 | MIT | Fabric, NeoForge (Forge, Quilt on older MC) | `gradle.properties` is 26.3.0. Modrinth lists 26.2 and 26.3. `mod_version=3.11.10` | 2,201 | Active |

Wikis read by clone: Embeddium (`For-Developers`, `Compatibility`, `Changes-from-upstream`, `Version-Status`, `Unsupported-mods`), Sodium (`Resource-Packs`, `Known-Issues`, `Configuration-File`, `CaffeineMC-Maven-&-Config-API`), Sodium Extra (`Features`, `Server-Policies`), VulkanMod (`Incompatible-mods`, `Home`).

---

## 1. Ground truth for 26.2 that changes the question

Seven facts, found while reading, change how to think about these mods. Each one has a source below.

1. **Vanilla 26.2 ships its own Vulkan backend.** Sodium reads `com.mojang.renderpearl.backend.vulkan.VulkanDevice` and picks a `VK_MULTIDRAW` or `VK_INDIRECT` draw path (`sodium/.../client/gpu/device/backend/DrawBackend.java:5,18-24`) `[v]`. Sodium's video page has a "Graphics API" option with Default, OpenGL and "Prefer Vulkan" (`SodiumConfigBuilder.java:308-324`) `[v]`. Chloride can swap GL and Vulkan at run time in 26.2 (`chloride/.../impl/GraphicsEngine.java:21,130-146`) `[v]`. Effect for Nublar: **no raw OpenGL call may exist in Nublar code.** A GL call is wrong on the Vulkan backend. Use the vanilla pipeline, buffer and render-pass classes only.
2. **Entities and block entities render in two steps.** First an extract step copies data to a render-state object. Then a submit step collects draw tasks. Sodium's `extractBlockEntities` fills `levelRenderState.blockEntityRenderStates` (`SodiumWorldRenderer.java:338-394`) `[v]`. GeckoLib 5 submits through `SubmitNodeCollector.submitCustomGeometry` (`GeoRenderer.java:187`) `[v]`. The platform note (`platform_and_library_baseline.md`, section 4.3) covers the extract step. This note covers what the mods do with it.
3. **Sodium draws a block entity only when its chunk section is visible.** Details in section 2.2. This is new compared with 1.20.1, where Embeddium used frustum tests.
4. **Terrain has three passes only**: solid, cutout, translucent (`DefaultTerrainRenderPasses.java:6-8`) `[v]`. A custom terrain render layer does not exist in the Sodium pipeline.
5. **Embeddium has no 26.x build.** The last Modrinth upload is 2025-01-24 for MC 1.21.4 `[doc]`. For the port, the only chunk renderer to support is Sodium (plus its addons). Embeddium matters only while the 1.20.1 build lives.
6. **VulkanMod has no 26.2 build** and its wiki says it is incompatible with Sodium, Embeddium, ImmediatelyFast, Create, CC: Tweaked and others (list dated 2024-09-16) `[doc]`. Its purpose (a Vulkan renderer) is now in vanilla.
7. **NeoForge gives a supported hook for static geometry in chunk sections**, `AddSectionGeometryEvent`, and Sodium implements it (`NeoForgeLevelRenderHooks.java:27-47`) `[v]`. On Fabric the equivalent is `FabricBlockStateModel#emitQuads` (FRAPI), which Sodium implements through its `frapi` module `[v]`. This is the "do not reinvent" answer for fence wires. Section 8 turns it into a design.

---

## 2. Sodium

### 2.1 In game

Terrain, block entities, entities and particles draw faster and with less stutter. Chunk builds run on worker threads, so a block change does not freeze the frame. Players see no gameplay change. They see three side effects that matter to a mod author:

- A block entity (machine, fence post) stops drawing when its section is hidden behind terrain (a hill, a cave roof, an opaque building) `[v]`.
- A dinosaur whose box overlaps only hidden sections does not draw, so a dinosaur inside a closed hangar costs nothing from outside `[v]`.
- A resource pack or mod that replaces vanilla core shaders loses effect, by design `[doc]`.

### 2.2 Mechanism

**Terrain.** Sodium builds each 16-block section into one vertex buffer per pass, groups sections into regions, and draws a region with one multi-draw call (`DefaultChunkRenderer`, `RenderRegion`). Visibility comes from its own occlusion graph (`RenderSectionManager`). Chunk builds run on a thread pool (`SodiumOptions.java:46`, `chunkBuilderThreads = 0` means automatic) `[v]`. Rebuild tasks sort into three queues by urgency: zero-frame, one-frame and always-deferred (`RenderSectionManager.java:761-763`) `[v]`. A rebuild near the player is "important" and gets uploaded first. A rebuild far away is deferred. A section rebuild reads a copy of the world (`LevelSlice`), so the mesh code never touches live objects on a worker thread `[v]` (`client/world/LevelSlice.java`).

**Block entities.** While meshing a section, Sodium looks at every block that `hasBlockEntity()`. If the block entity exists, a render predicate list passes, and a renderer exists for it, Sodium records it for that section (`ChunkBuilderMeshingTask.java:142-149`) `[v]`. It records it in one of two lists:

- `culledBlockEntities`: the default. The block entity extracts render state only when its section is in the visible list (`SodiumWorldRenderer.java:338-367`) `[v]`.
- `globalBlockEntities`: used when `renderer.shouldRenderOffScreen()` returns true. These extract every frame regardless of section visibility (`BuiltSectionInfo.java:94-97`, `SodiumWorldRenderer.java:369-380`) `[v]`.

The vanilla dispatcher then applies a frustum test with the renderer's render box (platform note section 4.1) `[v]` there, not here. So a fence-wire block entity pays: one section-visibility check (free, list membership), one frustum box test, and, if both pass, one render-state allocation and one submit.

A mod can veto block entities per type with `BlockEntityRenderHandler.addRenderPredicate(type, predicate)`. The predicate runs **on the chunk-build thread** at bake time, and "all predicates must return true" (`api/blockentity/BlockEntityRenderHandler.java:18-27`, `ExtendedBlockEntityType.java:17-27`) `[v]`. The API is marked `@ApiStatus.Experimental`, available since 0.6.0.

**Entities.** `EntityRenderer#shouldRender` is wrapped. When the option `useEntityCulling` is on (default true, `SodiumOptions.java:50`), the entity must (a) be glowing or show a name, or (b) have a culling box that touches at least one visible section, or (c) have a box volume above 16×16×16×50 = 204,800 blocks³, in which case only the frustum test runs (`SodiumWorldRenderer.java:443-472`, `EntityRendererMixin.java:18-28`) `[v]`. The box comes from `EntityRenderer#getBoundingBoxForCulling`. Nublar controls that method (platform note section 4.2).

**Immediate-mode vertex paths.** Sodium replaces vanilla's `MemoryUtil` writes in `BufferBuilder` with its own unsafe memory writes for **every** `addVertex` call (`mixin/features/render/immediate/buffer_builder/intrinsics/BufferBuilderMixin.java:15-33`) `[v]`. It adds a fast path for `putBakedQuad` when the buffer format is the block or entity format (same file, lines 36-56) `[v]`. It rewrites `ModelPart.translateAndRotate` (`ModelPartMixin.java:7-40`) and the cuboid writer for vanilla `ModelPart` cubes (`immediate/model/EntityRenderer.java`, `CubeMixin.java`) `[v]`. It exposes `VertexBufferWriter` and `EntityVertex` as public API for mods that want to write whole quads at once (`common/src/api/.../vertex/`) `[v]`.

**Items.** Sodium 0.9.2 for 26.2 adds optimised item meshing and fewer buffer resizes (release notes) `[doc]`. Item models for DNA tubes, syringes and similar items need no special work.

**Translucent terrain.** Sodium sorts translucent quads itself (`translucent_sorting/`: `SortBehavior`, `bsp_tree`, `TranslucentGeometryCollector`) with modes off, static and dynamic (`SortBehavior.java:5-12`) `[v]`. Nublar has no translucent terrain block in the plan, so this costs nothing. Amber and tank glass would use the vanilla translucent pass.

**FRAPI and NeoForge model API.**

- Fabric: Sodium's `frapi` module implements the Fabric Rendering API (`frapi/src/.../SodiumRenderer.java`, `FRAPIEmitter.java`) `[v]`. A mod needs only `fabric-renderer-api-v1`. Fabric API 26.2's `FabricBlockStateModel#emitQuads(QuadEmitter, level, pos, state, random, cullTest)` is the method that makes position-aware geometry, and `createGeometryKey(...)` lets the renderer cache results per context `[v]` (Fabric API `26.2` branch, `fabric-renderer-api-v1/.../model/FabricBlockStateModel.java`). Block entity data reaches a model through `RenderDataBlockEntity#getRenderData()`, which must be "safe to use in a multithreaded environment" (`fabric-block-getter-api-v2/.../RenderDataBlockEntity.java`) `[v]`. Sodium reads it in `FabricLevelAccess.java:12-14` `[v]`.
- NeoForge: Sodium reads `ModelData` per section from `level.getModelDataManager().getAt(sectionPos)` (`NeoForgeModelAccess.java:34-45`) `[v]`. A block entity sets it with `getModelData()` and signals changes with `requestModelDataUpdate()` (`IBlockEntityExtension.java:66-94`, NeoForge `26.2.x`) `[v]`. Dynamic tints come from `IClientBlockExtensions#collectDynamicTintValues` (`NeoForgeDynamicColorProvider.java:30`) `[v]`.
- Sodium 0.9.2 also supports FRAPI **on NeoForge**, but only if `fabric_renderer_api_v1` is present on the NeoForge side (`neoforge.mods.toml:22,31-34`: `requiredMods = ["fabric_renderer_api_v1"]`) `[v]`. A NeoForge-only Nublar build should not assume FRAPI exists.

**NeoForge build.** `sodium-neoforge` exists for every 26.x stable branch. It needs NeoForge `[26.1.2.10-beta,)` (`neoforge.mods.toml`, dependency block) `[v]`. It ships as a wrapped mod behind a `GraphicsBootstrapper` service so that driver workarounds can run before the early window starts (`neoforge/.../service/SodiumWorkarounds.java`, wiki "CaffeineMC Maven") `[v]` `[doc]`. The Sodium README says Fabric is "more lightweight and stable (for the time being)" `[doc]`. Release 0.9.2 for 26.2 lists "Support FRAPI on NeoForge" and a new terrain buffer allocator `[doc]`.

**Sodium's 26.2 status in one line.** Stable release `mc26.2-0.9.2` (2026-09-11, Fabric and NeoForge). `dev` has moved to 26.3 (`mc26.3-0.9.3-alpha.1`, 2026-09-20). Sodium Extra, ImmediatelyFast, Dynamic FPS, Iris, Nvidium and Chloride all list 26.2. Iris 1.11.1 is **not** compatible with Sodium 0.9.1 for 26.2, and the release note says to fetch an Iris build that matches `[doc]`.

**Config API.** A mod can add its own page to Sodium's video screen with `ConfigEntryPoint.registerConfigLate(ConfigBuilder)` (`api/config/ConfigEntryPoint.java`, wiki "Config API") `[v]` `[doc]`. Sodium Extra, Chloride and Nvidium all use it.

### 2.3 Lesson for Nublar

| Lesson | Module | Detail |
| --- | --- | --- |
| Static fence geometry belongs in the chunk mesh, not in a block entity renderer | B15 | Section 8 gives the design. Both loaders have a supported hook, and Sodium implements both. |
| Block entity renderers must return exact render boxes and must not set `shouldRenderOffScreen()` | B12, B15 | `shouldRenderOffScreen()==true` moves the block entity into the "global" list that extracts every frame (`ChunkBuilderMeshingTask.java:147`, `BuiltSectionInfo.java:94-97`) `[v]`. A parkwide machine renderer that sets it defeats section culling. |
| Section visibility also culls block entities whose drawing extends past their section | B15, B12 | A wire that is drawn from a post in a hidden section into a visible one vanishes with the post's section `[inf]` from `[v]` list logic. Keep every wire inside the section of its owner, or bake it into the chunk mesh. |
| Culling box on the dinosaur renderer | B09, B16, B27 | Sodium reads `getBoundingBoxForCulling`. Return the posed, scaled body box. Keep the volume below 204,800 blocks³ to get occlusion culling. A Brachiosaurus at 12.5 blocks is far below that. |
| Do not use glowing or a name tag on every dinosaur | B16 | Both force "always visible" in Sodium's entity cull (`SodiumWorldRenderer.java:457-460`) `[v]`. A tagged dinosaur in a hangar draws through walls (still frustum-tested). Show the name only on look-at or on the tablet. |
| Keep block-entity render predicates cheap and thread-safe if used | B12 | They run on the chunk worker thread. Do not read live block-entity state there (`BlockEntityRenderHandler.java:18-27`). Better: do not use them. A render box and a frustum test are enough. |
| Translucent or additive effects stay on vanilla `RenderType`s | B09, B12, B15 | Terrain passes are fixed at three. Entities and block entities may use any vanilla `RenderType`; they are outside the chunk mesh. |
| Provide a Sodium video-page entry for Nublar client options | B25 | Optional. Use `ConfigEntryPoint` only if a client option needs a place (for example "dinosaur animation distance"). `[inf]` |

### 2.4 Compatibility duty

1. Do not mix into `BlockEntityRenderDispatcher`, `LevelRenderer`, `ModelBakery`, `ChunkSectionLayer` or Sodium classes. Embeddium's developer page says the same: use platform APIs first, an Embeddium API second, a patch last and only with a version lock (wiki `For-Developers`, "Approach 1/2/3") `[doc]`.
2. Hold all chunk-geometry code to thread safety. The NeoForge event docs state it: the event handler runs on the main thread, but the `AdditionalSectionRenderer` runs on the rebuild thread, so copy any needed data inside the handler (`AddSectionGeometryEvent.java:7-29`, NeoForge `26.2.x`) `[v]`. The Fabric `RenderDataBlockEntity` doc says the same.
3. Do not rely on vanilla's random sequence for visuals. Sodium Extra has a "Use Fast Random" toggle, and its wiki note says it "has block rotation changes" `[doc]`. GPUBooster replaces random generators too (`client/mixin/random/*`) `[v]`. Pick model variants from `pos.asLong()` hash values you control, not from `RandomSource` call order. `[inf]`
4. Do not write to `VertexConsumer` outside the vanilla `RenderType`/submit path. Do not hold a `BufferBuilder` across frames.
5. Do not call GL. See ground-truth fact 1.
6. Treat animated textures as cosmetic. Sodium animates only sprites in visible sections (`animateOnlyVisibleTextures`, `SodiumOptions.java:49`) `[v]`, and Sodium Extra can turn all animation off (`SodiumExtraConfig.java:238`) `[v]`. A fence "live" state must show in the model or tint, never only in an animated `.mcmeta` frame.

### 2.5 Platform overlap

This is the platform's job already: culling boxes (vanilla renderer), render state (vanilla), model API (NeoForge model API and FRAPI). Nublar adds nothing here. Verdict: **Adopt (pattern)**. Test with Sodium on Fabric and on NeoForge as the baseline renderer from the first port build.

---

## 3. Embeddium

### 3.1 In game

Same as Sodium 0.5.x for players on Forge 1.20.1. It is the only chunk renderer that fits today's `forge/` module (Minecraft 1.20.1).

### 3.2 Mechanism

Embeddium is "based on the last FOSS-licensed version of the Sodium codebase" (Sodium 0.5.8) with extra patches `[doc]` (README). The patch list on the wiki ("Changes from upstream", marked out of date, February 2024) includes: read Forge `ModelData` when meshing, call the Forge overloads of `BakedModel#getQuads` and `getRenderLayers`, support entities with infinite bounding boxes, frustum-cull block entities, flat lighting for quads with AO off, a full-precision vertex format, a naive translucency sort `[doc]`. Its API package on `20.1/forge` holds `BlockRendererRegistry`, `MeshAppender`, `ChunkMeshEvent`, `ChunkDataBuiltEvent`, `RenderSectionDistanceFilter` and an event bus `[v]` (tree listing of `org/embeddedt/embeddium/api`). The wiki gives an example of the platform doing the work: NeoForge PR 642 added a standard way to append geometry to chunk sections, and Immersive Engineering's wires render under Embeddium "without either mod needing to code specific integration" `[doc]`. That PR is today's `AddSectionGeometryEvent` (section 2.2).

Support table (wiki `Version-Status`): 20.1 and 20.6 "Mainline", 19.2, 18.2 and 16.5 "LTS"; any other version is end of life `[doc]` (the page itself may be stale; the branch list reaches 21.4).

Known problem list (wiki `Unsupported-mods`): Entity Culling and Sound Physics Remastered "unsafely access the client world on a background thread", causing missing entities and `ConcurrentModificationException` `[doc]`. The wiki gives Create blocks on 1.20.1 as a former compatibility case that needed the mod to update `[doc]`.

### 3.3 Lesson for Nublar

- P2 dependency: the move from 1.20.1 Forge to 26.2 changes the renderer from Embeddium to Sodium. The rft model-data mixins (`BlockEntityElectricFencePoleMixin`, `ProjectNublarModelData`, listed in the platform note row H14) are exactly the pattern the Embeddium wiki calls "Approach 3" (direct patch). They are replaced, not ported (module B15, platform note row H14). `[inf]`
- While 1.20.1 lives: test the Forge build with Embeddium `0.2.x`/`1.0.x` and Rubidium-style `ModelData` once, before the port starts, so the baseline is known. `[inf]`

### 3.4 Compatibility duty

For the 1.20.1 build only: give every connected-model block a `getModelData()` result that is immutable and complete; call `requestModelDataUpdate()` on change; do not read the block entity inside the model. The wiki says that the Forge `ModelData` map is read when the chunk-meshing task is created `[doc]`.

### 3.5 Platform overlap and verdict

NeoForge model data replaces Forge's (same name, `ModelData`, `net.neoforged.neoforge.model.data`) `[v]`. Verdict: **Not relevant to 26.2. Compat for 1.20.1 until the port lands.** No new work.

---

## 4. Chloride

### 4.1 In game

A Sodium addon. Players with it see far entities, far block entities and far leaves vanish sooner than in vanilla. Options: distance culling per category, an entity count cap, whitelist by id, "fast" solid chests and beds, True Darkness, zoom, fog and nametag toggles, and (26.2) a live switch between OpenGL and Vulkan `[doc]` (README).

### 4.2 Mechanism

- **Entity distance cull.** An injection at `EntityRenderDispatcher.shouldRender` returns false when the entity is outside a horizontal square-distance and a vertical band, unless its type is whitelisted (`mixins/impl/EntityDistanceCullingMixin.java:67-92`) `[v]`. The test is `|y - camY - 4| < maxHeight` and `dx²+dz² < maxDistanceSqr` on `entity.position()` (`impl/EntityCulling.java:45-60`) `[v]`. Defaults: horizontal 4096 (squared, so 64 blocks), vertical 32, for every non-monster entity; monsters 16384 (128 blocks) and 64 vertical (`ChlorideConfig.java:183-206`) `[v]`. The split uses `entity.getType().getCategory() == MobCategory.MONSTER` (`EntityDistanceCullingMixin.java:75`) `[v]`.
- **Block entity distance cull.** `BlockEntityRenderDispatcher.tryExtractRenderState` returns null beyond range, unless the block-entity type id is whitelisted (`EntityDistanceCullingMixin.java:132-150`). Same 64 and 32 defaults (`ChlorideConfig.java:178-185`) `[v]`.
- **Entity cap.** `LevelExtractor.extractVisibleEntities` is redirected to sort all entities by distance to the player and keep the nearest N (default 512; 512 or more means no cap) (`EntityDistanceCullingMixin.java:38-65`, `ChlorideConfig.java:191`) `[v]`.
- **Whitelist** syntax: `modid:entity` or `modid:all` (`EntityCulling.java:26-35`) `[v]`.
- **Fast chests and beds.** Two built-in resource packs (`custom_packs/solid_chests`, `solid_beds`) replace the animated block-entity model with a static block model in blockstate JSON. Chloride adds or removes the pack from the repository when the player flips the option (`impl/FastBlocks.java:14-30,33-64`) `[v]`. It disables the chest option when Enhanced Block Entities is installed (`FastBlocks.java:29`) `[v]`.
- **Engine hot swap** between GL and Vulkan (`impl/GraphicsEngine.java`) `[v]`: "incompatible with Iris" (`:137-143`).

README text says the entity culling avoids "rendering/ticking" `[doc]`. The code I read culls rendering only; I found no tick hook `[v]` (search of `mixins/impl`).

### 4.3 Lesson for Nublar

| Lesson | Module | Detail |
| --- | --- | --- |
| A park with a 64-block-wide fence line may be cut by a player's own settings | B15, B16 | At defaults, every dinosaur and every fence block entity farther than 64 blocks horizontally, or more than 32 blocks above or below the camera, does not draw. A hilltop view over a valley (more than 32 blocks of height) hides the whole herd. Nublar cannot prevent this. It can make the result harmless: dinosaur logic must never depend on being drawn (animation events, sound triggers, particle spawns on the render thread). |
| Dinosaur `MobCategory` decides the Chloride range | B16 | Carnivores registered as `MONSTER` get 128 blocks and 64 vertical. Herbivores in `CREATURE` get 64 and 32. A mixed paddock shows predators farther than prey. Choose the category deliberately. Plan decision needed. See open question 1. |
| Static replacement of animated block entities is an accepted pattern | B12 | "Fast chests" proves players trade the chest-lid animation for FPS. Machines with an idle pose can ship a baked static block model and switch to the GeckoLib block entity renderer only while active or near (section 8.6 and checklist R2). |
| Ship `chloride:` style whitelists? No. | B25 | Nublar should not edit Chloride config. It can document type ids (`projectnublar:*`) for modpack authors. |

### 4.4 Compatibility duty

1. Never rely on the render path for state: no gameplay, no server-visible effect, no network request, from a renderer or from `extractRenderState` (a culled entity or block entity runs neither). `[inf]`
2. Register entity and block-entity types with stable ids. Chloride caches the whitelist result per type (`chloride$checked`, `EntityDistanceCullingMixin.java:97-119`) `[v]`.
3. Offer players a way to see far herds that does not need Chloride config: the tablet or map shows positions (B19). `[inf]`

### 4.5 Platform overlap and verdict

Vanilla already has per-entity-type `clientTrackingRange` (set at registration) and `EntityRenderer#shouldRender`. Nublar sets its tracking range per species in B16 and leaves further culling to players. Verdict: **Compat (document only).** No code.

---

## 5. Sodium Extra

### 5.1 In game

An add-on page for Sodium's video screen. Toggles: animations (water, lava, fire, portal, block animations, sculk sensors), per-particle-type particles, sky, stars, sun and moon, weather, biome colours, fog distance, cloud height, beacon beam, enchanting-table book, piston animation, item-frame and painting rendering, light updates, toasts, FPS and coordinates display, instant sneak, a Panini projection and more `[doc]` (wiki `Features`). Gameplay-affecting fog controls stay off on multiplayer unless the server grants them through a resource-pack policy file read by the Greenlight mod `[doc]` (wiki `Server-Policies`).

### 5.2 Mechanism

- **Particle toggles are generic.** `ParticleEngine.createParticle` is hooked: if the particle type's registry id is disabled in options, the method returns null (`mixin/particle/MixinParticleEngine.java:16-22`) `[v]`. The options list is built from the registry, so a mod's particle types appear without code `[doc]`. Effect for Nublar: spark, dust and drip particle types from fences, machines and dinosaurs show up as toggles named by their translation keys.
- **Light-update toggle.** `LevelLightEngine.checkBlock` and `runLightUpdates` are cancelled when the option is off (`mixin/light_updates/MixinLevelLightEngine.java:14-24`) `[v]`. The wiki warns it is for large redstone machines.
- **Animation toggle.** One master flag stops all sprite animation (`client/config/SodiumExtraConfig.java:238`) `[v]`.
- **Static entity toggles** for item frames, armor stands and paintings (mixins under `render/entity`) `[v]`.
- **Greenlight policy** for fog: server resource pack file `assets/sodium-extra/client_features/v1/protected_gameplay_fog.json` `[doc]`.

### 5.3 Lesson for Nublar

| Lesson | Module | Detail |
| --- | --- | --- |
| Name particle types with real translation keys | B15, B12, B25 | `particle.projectnublar.<name>` appears in the player's list. A raw id looks unfinished. |
| `createParticle` can return null | B15 | Code that spawns fence sparks must not assume a particle exists after the call. Vanilla `addParticle` already tolerates null. `[inf]` |
| Do not carry gameplay information in animation or light | B15, B14 | Light updates and animations can be off. Signal "fence live", "power low" or "alarm" through blockstate, model data, sound and the tablet UI, not through a light level or an animated texture alone. |
| Server-granted client features exist as a pattern | B19 | If Nublar ever needs a client feature that gives an advantage (for example a motion-tracker HUD overlay), the Greenlight pattern lets a server allow or deny it. Not needed for Beta. `[inf]` |

### 5.4 Compatibility duty

Name all `ParticleType`s. Keep any Nublar-specific client option in Nublar's own config, plus optionally a page through Sodium's `ConfigEntryPoint` (section 2.2).

### 5.5 Platform overlap and verdict

Vanilla 26.2 has accessibility and particle options already (the `particles` option for All/Decreased/Minimal). Sodium Extra only adds per-type granularity. Verdict: **Compat (document).** No code.

---

## 6. Nvidium

### 6.1 In game

On NVIDIA GTX 1600 cards and newer, large render distances run much faster because the GPU culls and draws terrain with mesh shaders. Players see only terrain change. Not usable with an active Iris shader pack `[v]`.

### 6.2 Mechanism

Nvidium cancels Sodium's `renderLayer` for terrain and draws its own passes with NV mesh shader extensions and GPU-driven occlusion (`mixin/sodium/MixinSodiumWorldRenderer.java:53-56`; shaders under `assets/nvidium/shaders/occlusion/*`) `[v]`. It takes the output of Sodium's chunk build and repacks it into its own vertex format (`MixinChunkBuilderMeshingTask.java`, `sodiumCompat/SodiumResultCompatibility.java`) `[v]`. It leaves the CUTOUT pass to render with SOLID (`MixinSodiumWorldRenderer.java:55-58`) `[v]`. It switches itself off when Iris reports a shader pack in use (`sodiumCompat/IrisCheck.java:10`) `[v]`. It publishes a tiny API, `NvidiumAPI.hideSection(x, y, z)` and `showSection`, that forces a section hidden until unhidden (`api0/NvidiumAPI.java:22`) `[v]`. Entities and block entities stay on Sodium's and vanilla's paths.

### 6.3 Lesson for Nublar

Nublar's static geometry must be in the Sodium chunk mesh or it does not exist for Nvidium. Anything drawn by a custom terrain-time hook (for example a fence wire mesh drawn after `renderLayer`) is invisible to GPU culling and may not depth-sort correctly. The chunk-mesh route in section 8 is therefore also the Nvidium-safe route. `[inf]`

### 6.4 Compatibility duty and platform overlap

No code. Do not hook `renderLayer` or terrain draw calls. Do not add a render pass for terrain-adjacent geometry. Verdict: **Not relevant (no action beyond the chunk-mesh rule).**

---

## 7. VulkanMod

### 7.1 In game

Replaces the OpenGL renderer with a Vulkan one, on Fabric only. For Minecraft 26.1.2 and older there is a build on Modrinth. For 26.2 there is none `[doc]`.

### 7.2 Mechanism

Its README promises reduced CPU and GPU overhead, terrain and entity render optimisations, chunk building optimisations, Wayland support, and windowed fullscreen `[doc]`. The wiki says **"Any mods that use OpenGL calls are not supported and are guaranteed to be broken"**, lists Sodium as "will never be supported", and lists ImmediatelyFast, Embeddium, Iris, Create, CC: Tweaked, Entity Texture Features, More Culling and others as crashing or broken (list updated 2024-09-16) `[doc]` (`Incompatible-mods.md`). The `dev` branch I read builds for 1.21.11 (`gradle.properties`) `[v]`.

### 7.3 Lesson, duty, platform, verdict

- The vanilla Vulkan backend of 26.2 takes the place VulkanMod had (ground-truth fact 1). Players who want Vulkan on 26.2 pick "Prefer Vulkan" in the video options, with or without Sodium `[v]` (`SodiumConfigBuilder.java:308-324`).
- Duty: **no GL calls**, and no custom `GlStateManager` use. Stay on vanilla pipeline classes. That also keeps Nublar working on any future Vulkan-only mod. `[inf]`
- Verdict: **Not relevant as a mod. Adopt the rule "no GL".** Test the Nublar client once with "Prefer Vulkan" per release (B25 checklist item).

---

## 8. Fence wires, machine renderers and chunk geometry (the design question)

This section answers the brief's key question: what should Nublar draw in a block entity renderer, and what should be a baked model?

### 8.1 Where the cost sits today

From the earlier survey, C5: about 900 wire block entities, each allocating a list and a lambda per frame `[inf]`. Under Sodium the cost per visible wire block entity is: list walk, frustum test, render-state allocation (a new `BlockEntityRenderState` object each frame in the 26.x design `[v]` `SodiumWorldRenderer.java:395-398`), a submit task, and one or more vertex writes. That is an allocation-rate and draw-count problem, not a triangle problem. A long fence line also contains hundreds of block entities in view at the same time.

### 8.2 The three tiers of geometry

| Tier | What it draws | Where | Cost per frame | Rebuild trigger |
| --- | --- | --- | --- | --- |
| T0 | Posts, wire strands, insulators, signs, the normal state of each wire | Baked block model: blockstate JSON with `multipart` for connections, or a custom `BlockStateModel` | None. Lives in the section buffer. One multi-draw per region | Blockstate change: the section rebuilds on a worker thread |
| T1 | State that changes seldom and per-block: broken wire halves, wire health bands, "off" versus "live" tint, tier colour | Model data (NeoForge) or render data (Fabric) read by the T0 model during meshing | None per frame | `requestModelDataUpdate()` (NeoForge) or a section dirty call: one worker rebuild of the affected section |
| T2 | Short-lived effects: sparks, arc flash, smoke, a snapping wire | Particles, or a block entity renderer on only the few block entities that need it now (an active flag in extracted state) | Only while the effect runs | None |

### 8.3 What T0 and T1 use on each loader, with no new library

- **Static, state-driven fences (T0).** Use a normal blockstate with `multipart` entries keyed by the connection properties. This needs no Java rendering code, works in Sodium, Embeddium, Iris and Nvidium, and is the cheapest tier. The FRAPI and NeoForge APIs are not needed for it. `[inf]` from `[v]` that Sodium meshes all `RenderShape.MODEL` blocks (`ChunkBuilderMeshingTask.java:129-133`).
- **Geometry that blockstates cannot express (T0/T1).** Examples: wires that sag along a catenary by span length, strand counts that depend on the tier of the neighbour, posts that lean after a knock-down.
  - Fabric: implement `FabricBlockStateModel#emitQuads(...)` and `createGeometryKey(...)` on a custom block state model. Quads come from a pre-baked `Mesh` where possible; the API doc tells implementors to "rely on pre-baked meshes as much as possible and keep dynamic transformations to a minimum" `[v]`. Read the data from `getBlockEntityRenderData(pos)` (`RenderDataBlockEntity`), which must be immutable or atomic `[v]`.
  - NeoForge: either a `DynamicBlockStateModel`/`DelegateBlockStateModel` (listed in the platform note section 4.4) reading `ModelData`, or an `AddSectionGeometryEvent` handler. The event suits geometry that spans blocks (a wire run across several blocks as one strip). It fires on the main thread when a section is queued for rebuild, and the renderer lambda runs on the rebuild thread, so the handler must copy its data first. The docs also tell authors to add a renderer only when something will be drawn, which allows Sodium to skip empty sections (`AddSectionGeometryEvent.java:7-29`) `[v]`. Sodium passes a vertex consumer for any of the three chunk layers (`NeoForgeLevelRenderHooks.java:33-47`, `SectionRenderingContext#getOrCreateChunkBuffer`) `[v]`.
  - Both loaders: put the shared geometry code (a function from a small immutable `FenceVisual` record to quads) in `common/`, with two thin loader adapters. The record is the same data in `getModelData()` and `getRenderData()`.
- **What stays a block entity renderer.** Anything animated per frame: rotating machine parts, GeckoLib machines while active, the sequencer's moving arm, an incubator's egg wobble in its last seconds. Keep a render box (not `AABB.INFINITE`) and do not set `shouldRenderOffScreen()` (section 2.3).

### 8.4 Rebuild-cost rules for T0 and T1 (read before choosing blockstate properties)

Each blockstate or model-data change dirties one 16-block section and its neighbours when occlusion can change. The rebuild runs off the main thread, so it will not cause a tick stall. It still costs worker time and a buffer upload, and it can show as a flicker if the player stands next to it (the "important" queue uploads near-player work in the same frame, `RenderSectionManager.java:761-763`). `[v]` for the queues, `[inf]` for the visual effect. Therefore:

1. **Slow state only in the model.** Connection layout, tier, broken/intact wire, post knocked down: yes. These change a few times per hour of play.
2. **Fast state never in the model.** Live/unlive that flips each tick when load shedding oscillates, sparks, per-tick current: no. Use T2, or a tint with a cheap quantised state (two values, debounced to at most once per second). `[inf]`
3. **Batch dirty calls.** When a switch turns off a 300-post fence section, mark sections dirty in one pass after the state change, not once per post. Vanilla's `Level.sendBlockUpdated` already coalesces rebuilds per section. `[inf]`
4. **Wire health bands.** Use 3 to 4 bands (intact, damaged, nearly broken, broken) per wire for the model, not one value per hit. Health stays exact in the block entity. The model sees the band only. `[inf]`

### 8.5 Estimated effect (all `[inf]`, estimate)

| Case | Today (plan C5) | After T0/T1 | Basis |
| --- | --- | --- | --- |
| 900 wire block entities in view, per frame | 900 extract steps, 900 allocations of a list and a lambda, 900 submit tasks | 0 extract steps. Wire geometry sits in the section buffer | Chloride's solid chests do the same exchange for chests (`FastBlocks.java:14-64`) `[v]`. The size of the gain is my estimate: the survey's remedy for C5 is allocation removal, and this removes the work entirely |
| Rebuild work on switch-off of a section | None (renderer reads the live state) | One rebuild per dirty 16³ section, off-thread | `[inf]` |
| Memory | 900 render-state objects per frame, short-lived | About the quad count of the wire strands in section buffers, long-lived | `[inf]` |

I make no claim about fps. No test was run.

### 8.6 Machines (B12)

Block entity renderers stay, because machine animation is per frame. Four rules: (1) exact render box (survey C6), (2) idle machines use a baked static model through blockstate and the block entity renderer submits nothing when idle (pattern from Chloride's solid chests, `FastBlocks.java`), (3) GeckoLib machines drop hidden bones through the library's `skipRender` (survey C6), (4) no `shouldRenderOffScreen()`.

---

## 9. Dinosaurs, GeckoLib 5 and colour layers

### 9.1 What GeckoLib 5 does (26.2 branch, `1e09655`, 2026-09-16, MIT)

- `GeoRenderer#submitRenderTasks` calls `renderTasks.submitCustomGeometry(poseStack, renderType, lambda)` and inside the lambda renders the whole `BakedGeoModel` into the `VertexConsumer` (`renderer/base/GeoRenderer.java:172-195`) `[v]`. The default render type is `RenderTypes.entityCutout(texture)` (`:68-74`) `[v]`.
- Each quad calls `pose.transform(new Vector4f(...))` per vertex and then `vertexConsumer.addVertex(x, y, z, color, u, v, overlay, light, nx, ny, nz)` (`cache/model/GeoQuad.java:22-29`) `[v]`. One allocation per vertex. One ARGB colour per call (`renderColor`), so there is no per-vertex tint.
- `TextureLayerGeoLayer` re-renders the **entire model** once per layer: `renderer.submitRenderTasks(info, renderTasks.order(1), renderType)` (`renderer/layer/builtin/TextureLayerGeoLayer.java:60-72`), and its own javadoc says to use `CustomBoneTextureGeoLayer` for per-bone textures instead `[v]`. This is the mechanism behind survey C1 (23 draws per Gallimimus).

### 9.2 What Sodium and the others do with that

| Path | Sodium effect | Evidence |
| --- | --- | --- |
| `VertexConsumer.addVertex` on a `BufferBuilder` | Faster memory writes through Sodium's redirected `MemoryUtil` calls. GeckoLib benefits without changes | `BufferBuilderMixin.java:15-33` `[v]` |
| Whole-quad fast encoder (`putBakedQuad`, `VertexBufferWriter.push`) | **Not used by GeckoLib**, because GeckoLib writes vertex by vertex. Vanilla `ModelPart` cubes and baked quads use it | `BufferBuilderMixin.java:36-56`, `EntityRenderer.java` (immediate/model) `[v]` |
| Entity culling by section | Applies (any `EntityRenderer`) | `EntityRendererMixin.java:18-28` `[v]` |
| ImmediatelyFast batching | Applies to draws that share a `RenderType` (section 10) | `MixinRenderTypeFeatureRenderer_Group.java:24-32` `[v]` |

The vertex-by-vertex path is the hidden cost. Vertex count times 11 values times the number of layers. For 20 Gallimimus with 23 passes (survey C1): the survey measured 26 to 28 fps against 138 fps with layers off `[v]` in its own tests. This note does not retest. `[doc]`

### 9.3 Lesson for Nublar

| Lesson | Module | Detail |
| --- | --- | --- |
| Layer baking is the correct fix and no mod removes the need | B09 | No renderer mod batches the 23 passes into one. They have different textures (different `RenderType` state), so nothing can merge them. The bake spec (one dynamic texture per genome key, one draw) stays the plan. `[inf]` |
| Draw order of layers is a correctness rule, not only a perf rule | B09 | ImmediatelyFast forces every feature-renderer group to be reorderable (`MixinRenderTypeFeatureRenderer_Group.java:27-30`) and has to un-force it for banner patterns, where a later layer paints over an earlier one on the **same texture sheet** (`compat/iris/MixinRenderTypeFeatureRenderer_Group.java:37-48`) `[v]`. Its release notes list repeated "render order" fixes: villager profession level layers (v1.12.5, v1.12.6, v1.8.7), player nametag order (v1.14.2), banner patterns with Iris (v1.16.5) `[doc]`. If Nublar keeps unbaked layers that rely on submit order, e.g. `renderTasks.order(1)`, another renderer mod can reorder them. Bake. If not baked, give each layer a different `RenderType` (own texture) and a non-zero `order`, never rely on submission order inside one `RenderType`. `[inf]` |
| Per-vertex tint does not exist in GeckoLib 5 | B09 | One ARGB per pass. Per-region colour comes from the colour layers (bake) or from `CustomBoneTextureGeoLayer` for per-bone textures. A per-vertex pattern belongs in the baked texture. |
| `new Vector4f` per vertex | B25 | Allocation per vertex per pass. A baked single pass cuts the count by the layer count (in the same ratio as the draws). It cannot be fixed on Nublar's side because it is library code. Open an upstream issue only if a profile shows it after baking. `[inf]` |
| Dynamic textures: upload cost | B09 | A genome-key bake uploads a texture once. Share the texture across all dinosaurs with the same key. Release on key eviction. Keep the pool small. Under Vulkan, upload is through the vanilla GPU texture class, not GL. `[inf]` |

### 9.4 Compatibility duty (dinosaurs)

1. `getBoundingBoxForCulling`: posed, scaled body box (section 2.3).
2. Entity render type: use vanilla `RenderTypes` and the GeckoLib default. A custom `RenderType` built from `RenderSetup` is allowed, but note how vanilla builds text types with `sortOnUpload()` (a per-upload sort) and ImmediatelyFast removes that for text (`MixinRenderTypes.java:26-37`) `[v]`. A Nublar translucent layer (for example a wet-skin gloss) should not enable `sortOnUpload` unless needed.
3. Never override a core shader. ImmediatelyFast keeps a blacklist of core shaders whose replacement by a resource pack disables its text atlas feature (`CoreShaderBlacklist.java:30-32` lists `core/text`; `resource_pack_conflict_handling/MixinMinecraft.java:66-100`) `[v]`. Sodium's wiki says core shader replacement "is not supported" and that a changed solid-block shader "severely" damages frame rate `[doc]`. Nublar ships no core shader. If a glow or heat-haze effect is wanted, use vanilla `RenderType`s plus emissive textures, which Iris and Sodium support through the normal path.
4. Per-frame work in `extractRenderState` must not allocate arrays or streams (survey C1, C3).

### 9.5 Platform overlap and verdict

All of this is library and vanilla territory: GeckoLib 5 owns the draw path, vanilla owns culling and batching, Sodium and ImmediatelyFast speed up the shared parts. Verdict: **Adopt (pattern)**: bake layers (B09), tight culling box (B09/B27), no core shader, no render-order reliance.

---

## 10. ImmediatelyFast

### 10.1 In game

Fewer draw calls for entities, block entities, particles, text, map items, GUI and HUD. Players see higher fps in busy scenes (signs, maps, many entities, big GUIs). The README gives its own test numbers from version 1.1.x on MC 1.19.3: 1000 cows 16 to 60 fps with no other mod, 400 chests 170 to 205 fps, 200 signs 70 to 125 fps `[doc]` (README, hardware Ryzen 5 1600 and GTX 1060). Those are old and set on a different render design; treat them as estimates.

### 10.2 Mechanism in the 26.x line

In 26.x vanilla already collects draw tasks per `RenderType` (the submit/feature renderer design), so ImmediatelyFast's old custom buffer class is gone. What remains in `26.3` (the 26.2 branch has the same layout `[inf]`) is a list of targeted fixes. From `immediatelyfast-common.mixins.json` and `ImmediatelyFastConfig.java:24-38` `[v]`:

| Option | What it changes | Source |
| --- | --- | --- |
| `enhanced_batching` | Forces `canReorder = true` in every `RenderTypeFeatureRenderer.Group`, so draws that share a `RenderType` merge even when vanilla would keep their order. Fixes `equals`/`hashCode` of `ScissorState` so the group cache works | `MixinRenderTypeFeatureRenderer_Group.java:24-32`, `MixinScissorState.java:26-63` |
| Iris compat for batching | Un-forces reorder for the banner sheet | `compat/iris/MixinRenderTypeFeatureRenderer_Group.java:24-48` |
| `skip_text_translucency_sorting` | Removes `sortOnUpload()` from three text render types | `MixinRenderTypes.java:26-37` |
| `fast_text_lookup` | Caches the last `RenderType`/`VertexConsumer` pair per glyph renderer | `MixinTextFeatureRenderer_GlyphRenderer.java:19-34` |
| `font_atlas_resizing` | Larger glyph atlas textures (default 1024) | `ImmediatelyFastConfig.java:25-26` |
| `map_atlas_generation` | One texture for all maps (default 2048) | `:27-28` |
| `avoid_redundant_framebuffer_switching` | Cancels the framebuffer unbind after each render pass; re-binds 0 before present | `avoid_redundant_framebuffer_switching/MixinGlCommandEncoder.java:30-40` |
| `batch_animated_item_updates` | One atlas clear for all animated GUI items | `batch_animated_item_updates/MixinGuiRenderer.java` |
| `experimental_sign_text_buffering` | Off by default | `ImmediatelyFastConfig.java:38` |
| Resource-pack conflict handling | Turns off font-atlas and map-atlas features when a pack replaces `core/text` | `CoreShaderBlacklist.java:30-32`; `resource_pack_conflict_handling/MixinMinecraft.java:66-100` |

Its API is only `ImmediatelyFast.java` (config and runtime config) `[v]` (`docs/API_DOCS.md`). There is no batching API for mods; batching is automatic per `RenderType`.

### 10.3 Which Nublar patterns break batching

Batching merges draws that share a `RenderType` and render-state. Patterns that defeat it, with `[inf]` unless marked:

1. **One `RenderType` instance per object.** Creating a `RenderType` (or an equivalent pipeline state) in a per-frame call, or per entity, makes every draw its own group. Keep render types in `static final` fields, or in a cache keyed by texture id (as vanilla's `entityCutout(texture)` does through its function cache). Never build one per frame.
2. **Unique texture per object where a shared one works.** A dynamic texture per dinosaur individual stops merging across the herd. Key the bake by genome (shared by every animal with the same key).
3. **Layer passes that depend on submit order inside one `RenderType`.** They break when `canReorder` is forced (section 9.3).
4. **Scissor and render-state that change within a frame for no reason.** The scissor state is part of the group key; IF had to fix its `equals` so groups hash correctly `[v]`. A GUI that toggles scissor per widget splits batches (machine screens, tablet).
5. **Per-frame text through `Font` with unique styles or with custom shaders.** Text uses the shared glyph batches. A core shader for text turns off the atlas optimisation `[v]` (`CoreShaderBlacklist.java`).
6. **Forcing a flush or a render-target switch mid-frame.** `avoid_redundant_framebuffer_switching` assumes passes can share a bound framebuffer. A mod that renders a custom view to its own target every frame (a security-camera monitor, B19 tablet minimap in 3D) forces switches. Render such a view at a low rate (for example 5 to 10 times per second, not every frame) and not at all when the screen is closed. `[inf]`
7. **Map-item style per-frame texture uploads.** `map_atlas_generation` exists because per-map textures were slow. If Nublar draws a monitor screen texture that updates each frame, update it at most a few times a second.

### 10.4 Lesson for Nublar

| Lesson | Module | Detail |
| --- | --- | --- |
| Render types are cache keys | B09, B12 | Static, shared `RenderType`s only. Check each custom one in B25. |
| Do not fight order | B09 | Bake layers. See section 9. |
| HUD and screens | B12, B19 | Machine screens and the tablet are GUI draws. IF batches vanilla GUI and text. Avoid per-frame scissor changes and avoid custom text shaders. |
| Monitoring screens must be rate-limited | B19 | A live camera or minimap in a block-entity screen is the one place Nublar could create heavy per-frame target switching. |

### 10.5 Compatibility duty and platform overlap

No code to write. Run the client with ImmediatelyFast on 26.2 as a standard test (it lists 26.2 on Modrinth). The Vulkan backend is supported by it since `v1.16.0` (2026-06-18) `[doc]`. The platform (vanilla batching in the submit design) does the heavy part now; IF adds the rest. Verdict: **Compat (test with it), Adopt the "shared render type" rule.**

---

## 11. GPUBooster

### 11.1 In game

Higher fps on GPUs with OpenGL 4.5 by using direct state access, a buffer pool, a depth renderbuffer, bindless textures, plus faster math and random `[doc]` (README).

### 11.2 Mechanism

Fabric 1.21.1 only (`gradle.properties`, branch `GPUBooster-1.21.1-fabric`). Mixins into `BufferBuilder`, `VertexBuffer`, framebuffers, `BufferRenderer`, `Window`, `MatrixStack.Entry`, `MathHelper`, and the random classes (`src/main/java/com/mr_toad/gpu_booster/client/mixin/{dsa,math,random,tgl}`) `[v]`. The README states RBO depth is "not compatible with shaderpacks" and that GL below 4.5 loses features `[doc]`. Last commit 2025-12-06 `[v]`.

### 11.3 Lesson, duty, verdict

It targets OpenGL internals that 26.x moved behind the new device/pipeline classes, and replaces random and math. No 26.x build exists. Lesson only: faster random and math replacements are one more reason **not to depend on vanilla random order** (section 2.4, item 3). Verdict: **Avoid as a dependency and as a model. Not relevant to 26.2.**

---

## 12. Ixeris

### 12.1 In game

Smoother camera movement on high-polling-rate mice, mostly on Windows. The README measures an 8000 Hz mouse at 12 fps without the mod and 121 fps with buffered raw input in a flat world `[doc]` (author's tables), and smaller gains at 500 to 2000 Hz.

### 12.2 Mechanism

Two changes: threaded event polling (the main thread polls events and the render runs on its own thread), and buffered raw input on Windows (`GetRawInputBuffer`) (`README.md:11-12`) `[doc]`. GLFW calls that need the main thread get routed to it. Frequently used GLFW state is cached so that calls from the render thread do not block. It rewrites the frame limiter (hybrid sleep and spin wait) `[doc]`. Callbacks set by `glfwSet*Callback` run on the render thread `[doc]`. The API package `me.decce.ixeris.api.IxerisApi` has `isOnMainThread`, `runOnMainThread`, `runLaterOnMainThread`, `runNowOnMainThread` and is 0BSD (`IxerisApi.java:1-5,82`) `[v]`.

### 12.3 Lesson and duty for Nublar

- **Do not call GLFW directly** from Nublar. Vehicle controls (Jeep, Explorer: B22) read vanilla `KeyMapping` and `Options`; the mouse look for an optional camera view uses the vanilla input classes. A direct `GLFW.glfwGetKey`, `glfwSetCursor` or clipboard call from the render thread works through Ixeris's routing, but a raw native call without GLFW "may fail" on the render thread (`API_DOCS.md`) `[v]`. `[inf]` for the Nublar-specific conclusion.
- Any native window feature (for example an alert flash for a fence breach when the window is in the background) goes through `IxerisApi#runOnMainThread` and is optional. For Beta, do not add one.
- Verdict: **Compat (no action, follow the rule).** It is the cleanest example of "the platform input API is the contract".

---

## 13. Dynamic FPS (unfocused window)

### 13.1 In game

When the Minecraft window loses focus, is hovered only, is minimised, or is idle, the client renders fewer frames, lowers volume, and pauses toasts. Defaults and each state are configurable `[doc]`. It also fixes a vanilla busy-wait and stops rendering the world behind the loading overlay `[doc]`.

### 13.2 Mechanism

- States: `FOCUSED`, `HOVERED`, `UNFOCUSED`, `INVISIBLE`, `UNPLUGGED` (battery), `ABANDONED` (idle) (`impl/PowerState.java:10-40`) `[v]`.
- It wraps `Minecraft.renderFrame`. If its check says "do not render this frame", it calls `pauseIfInactive()` and a frame-rate limiter and skips the original method (`mixin/MinecraftMixin.java:40-50`) `[v]`. Target 0 means render nothing (`DynamicFPSMod.java:341`) `[v]`. The mod's minimum render loop rate is 15 cycles per second, so the window stays responsive (`Constants.java:13`, README FAQ) `[v]` `[doc]`.
- It replaces the vanilla event-loop wait with a 0.5 ms park (`mixin/bugfix/BlockableEventLoopMixin.java:15-19`) `[v]`.
- It changes volume (`SoundEngineMixin`) and toast pausing (`ToastManagerMixin`) `[v]`.
- Mod metadata can opt out of its loading-overlay change: `dynamic_fps = {optimized_overlay = false}` under `[modproperties.<modid>]` (README, "Developer Info") `[doc]`.
- The `renderFrame` wrapper skips drawing. I found no hook that skips `Minecraft.tick` or the integrated server. Vanilla's own "pause on lost focus" decides that for singleplayer. [inf] from the code read: the wrapped method is `renderFrame`, and the client tick lives elsewhere in vanilla 26.x.

### 13.3 What Nublar should do when the window is unfocused

Rule: **no client-side work that only serves what the player sees.** Frames are skipped, but ticks still run in multiplayer and on a dedicated server's client.

| Item | Rule | Module |
| --- | --- | --- |
| Render-thread work | None to write. `extractRenderState` and renderers do not run in skipped frames `[v]`. They are the reason to keep per-frame cost low in focused play, not in the background | B09, B12 |
| Client tick work | Put every purely cosmetic client tick behind a cheap guard: if the window is not active, skip spark particles, ambient fence hum emission, dust, footstep effects, tablet map refresh. Use `Minecraft.getInstance().isWindowActive()` `[inf]` (the name exists in earlier versions; I found no use of it in the repos read. Verify it in 26.2 at port time) | B15, B12, B19 |
| Particles | Skip spawning when unfocused. A skipped frame still ticks live particles; spawning more adds work with no one to see it | B15 |
| Sounds | Dynamic FPS lowers volume but still plays. Do not start new long loops (generator hum) just because the state changed; keep sound start on server events | B14, B12 |
| Animation controllers | GeckoLib controllers tick in the render step on 26.x (`RenderPassInfo.boneUpdates`, section 9). With frames skipped, they do not advance. When the window returns, animation time jumps. Drive any one-shot sound or particle keyframe from a server event or a tick, not from an animation keyframe, so that a skipped frame does not lose the effect `[inf]` | B09 |
| Network | Nothing changes. The client keeps receiving packets. Packet handlers must be cheap (survey: machine sync) | B12, B14 |
| Hard rules | Never block the client thread on I/O or on a server round trip. Never poll in a loop waiting for focus | B25 |

### 13.4 Compatibility duty

None for correctness. For clarity: Dynamic FPS and VulkanMod conflicted in 2024 ("flickering with reduced fps", wiki list) `[doc]`; VulkanMod is not a 26.2 target. Verdict: **Compat. Adopt the guard pattern (small, S).**

---

## 14. Cross-cutting checklist for Nublar (port and Beta)

| Id | Check | Source of the rule | Module |
| --- | --- | --- | --- |
| R1 | No GL call in Nublar code. Pass once with "Prefer Vulkan" selected | Ground-truth fact 1, Sodium `DrawBackend`, VulkanMod wiki | P2, B25 |
| R2 | Machines: static baked model when idle, GeckoLib block entity renderer only while active or near | Chloride `FastBlocks` | B12 |
| R3 | Fence geometry in the chunk mesh (T0/T1), effects only in T2 | Sodium list logic, NeoForge event, FRAPI | B15 |
| R4 | No `shouldRenderOffScreen()`, no `AABB.INFINITE` for repeated block entities | `ChunkBuilderMeshingTask.java:147` | B12, B15 |
| R5 | Culling box on the dinosaur renderer, below 204,800 blocks³ | `SodiumWorldRenderer.java:443-472` | B09, B27 |
| R6 | No glowing and no always-on name tag as a default | `SodiumWorldRenderer.java:457-460` | B16 |
| R7 | Shared static `RenderType`s. No per-frame or per-entity render type | ImmediatelyFast group key | B09, B12 |
| R8 | Bake colour layers. If not baked, distinct render types and orders | ImmediatelyFast render-order fixes | B09 |
| R9 | No core shader. No `sortOnUpload` on new translucent types without need | Sodium wiki, `CoreShaderBlacklist` | B09, B25 |
| R10 | Cosmetic client ticks guarded by window activity. One-shot effects driven by events, not keyframes | Dynamic FPS | B15, B12, B19 |
| R11 | No GLFW call. Input through `KeyMapping` and vanilla input | Ixeris | B22 |
| R12 | Immutable model data / render data. Copy inside the event handler | NeoForge event docs, Fabric `RenderDataBlockEntity` | B15 |
| R13 | Particle types and options have translation keys | Sodium Extra particle list | B15, B25 |
| R14 | Gameplay state never shown only by animation, light level or render-only state | Sodium Extra toggles, Chloride culling | B14, B15 |

---

## 15. Closing table

Effort: S under 100 lines, M one or two classes, L new subsystem. Effects are estimates unless `[v]`.

| Technique | Source | Nublar module | Verdict | Effort | Expected effect |
| --- | --- | --- | --- | --- | --- |
| Static fence geometry as blockstate `multipart` models | Sodium chunk mesh (`ChunkBuilderMeshingTask.java:129-133`) | B15 | Adopt (pattern) | M (JSON plus a small blockstate generator) | Removes most of the C5 cost for fences. Estimate: from hundreds of block-entity extract steps to none for static wires |
| `FabricBlockStateModel#emitQuads` plus `createGeometryKey` for dynamic fence shape | Fabric API 26.2 `[v]`, Sodium frapi | B15 (Fabric) | Adopt | M | Same, for sag, tiers, broken halves |
| `AddSectionGeometryEvent` or `DynamicBlockStateModel` for multi-block wire strips | NeoForge 26.2.x `[v]`, Sodium `NeoForgeLevelRenderHooks` | B15 (NeoForge) | Adopt | M | Same, fewer quads for long runs |
| Shared `FenceVisual` record read by both loader adapters | `getModelData()` and `getRenderData()` `[v]` | B15 | Adopt | S | One code path, thread-safe data |
| Slow state only in model; fast effects as particles or a few block entities | Sodium rebuild queues `[v]`, inference | B15, B14 | Adopt | S | Avoids section rebuild storms on power flicker |
| Exact render boxes, no off-screen flag | `ChunkBuilderMeshingTask.java:147` | B12, B15 | Adopt | S | Keeps section culling working |
| Idle machines as baked static model | Chloride `FastBlocks` | B12 | Adapt | M | Machines cost nothing when idle and far. Estimate only |
| Tight dinosaur culling box under the volume cap | Sodium `isEntityVisible` | B09, B27 | Adopt | S | Occlusion culling for hangars and hills |
| Do not use glowing/name tags as default | Sodium `isEntityVisible` | B16 | Adopt | S | Keeps occlusion culling |
| Bake colour layers per genome key | Survey bake spec; ImmediatelyFast order fixes | B09 | Adopt | L (already planned) | One draw per dinosaur. Survey estimate: 120+ fps for 20 Gallimimus `[doc]` |
| Static shared `RenderType`s | ImmediatelyFast group batching | B09, B12 | Adopt | S | Draws merge per type |
| No core shader | Sodium wiki, ImmediatelyFast blacklist | B09, B25 | Adopt (avoid) | S | Keeps Iris, Sodium and IF features active |
| No GL calls; test "Prefer Vulkan" | Sodium, Chloride, VulkanMod wiki | P2, B25 | Adopt | S | Works on both vanilla backends |
| Dynamic-FPS style guard on cosmetic client ticks | Dynamic FPS | B15, B12, B19 | Adapt | S | Less background CPU in multiplayer |
| Rate-limit monitor and minimap textures | ImmediatelyFast map atlas, framebuffer switches | B19 | Adapt | S | Avoids per-frame target switches |
| No GLFW calls | Ixeris | B22 | Adopt (rule) | S | Works with threaded polling |
| Name particle types | Sodium Extra | B15 | Adopt | S | Clean options list |
| Sodium `ConfigEntryPoint` page for Nublar client options | Sodium API | B25 | Optional | S | Players find options in one place |
| Mixin into Sodium or render dispatchers | Embeddium wiki "Approach 3" | any | Avoid | n/a | Breaks on each update |
| Embeddium on 26.x | Embeddium wiki, Modrinth | P2 | Not relevant (no build) | n/a | Use Sodium. Keep Embeddium only for the 1.20.1 build |
| VulkanMod | VulkanMod wiki | P2 | Not relevant (vanilla Vulkan exists) | n/a | None |
| Nvidium | Nvidium source | B15 (chunk-mesh rule) | Not relevant (terrain only) | n/a | Chunk-mesh rule keeps Nublar compatible |
| GPUBooster | GPUBooster source | P2 | Avoid / Not relevant | n/a | None |
| Chloride distance culling | Chloride source | B16 | Compat (document ids) | S | Players can whitelist `projectnublar:*` if wanted |
| Ixeris, Dynamic FPS, ImmediatelyFast, Sodium Extra | Sources above | B25 | Compat (test) | S | Test matrix item |

---

## 16. Open questions (real design gaps)

1. **Which `MobCategory` do the dinosaurs use?** Chloride's default view range depends on it (monster 128 blocks, others 64, section 4.2). A carnivore as `MONSTER` also changes spawn rules, despawn and difficulty behaviour in vanilla. The lead should state the intended category per diet (B16). I recommend the answer is chosen for gameplay reasons first, and Chloride's range is accepted as a player-side setting.
2. **How much of the fence state must change a block model, and how often?** The proposal in 8.4 limits model changes to slow state (tier, intact or broken band, knocked-down post) and sends fast state to particles. The lead should confirm that "live or not live" may have a visible tint in the model at all, given that a load-shed oscillation could flip it often. If yes, add a debounce rule (B14, B15).
3. **Is a visible "damaged wire" band in the model acceptable, or must each hit show?** The plan says "both clients render broken halves and sparks" (B15 acceptance). Bands in the model give broken halves. They do not give per-hit feedback; that comes from T2. Confirm.
4. **Does the lead want idle machines to be baked static models (R2)?** It needs art exports from B24 for a static pose of each machine. This is a cost in B24, not in code.
5. **Which loader gets the first fence implementation?** The Fabric path (`emitQuads`) and the NeoForge path (`AddSectionGeometryEvent` or a delegate model) differ. A shared `FenceVisual` record is cheap; the loader adapters are not identical. The lead should pick which loader gates the B15 acceptance test first (P2 order).
6. **Should Nublar ship a Sodium Config API page at all?** It is optional and has value only if Nublar adds client options (animation distance, particle density). Not needed before B25.
7. **Window-active API name in 26.2.** `Minecraft#isWindowActive` is my assumption from earlier versions; no repo read here uses it. Verify at port time against the 26.2 mappings before relying on it in R10.
