# Entity and block-entity rendering, culling and texture mods: what they teach Project Nublar

**Scope:** ten repos named by the lead, plus two repos that the first ten point to (the occlusion library under EntityCulling, and the upstream of SodiumDynamicLights). The note answers how each mod removes cost, what Nublar's own code must do, and what Nublar must do to stay compatible.

**Label key:** `[v]` verified in source (path and lines at the commit in the table). `[doc]` from README, wiki, issue thread or store text. `[inf]` my inference. Any performance number that is not `[v]` is an estimate.

**Clones:** `<SCRATCH>\clones\<repo>`, where SCRATCH is `C:\Users\kgumm\AppData\Local\Temp\claude\C--Users-kgumm-Documents-Minecraft-Modding-Project-Nublar\8b070e0a-e83f-4cf7-99bd-6a759f6bb05e\scratchpad`. Paths below are relative to each clone. Vanilla 26.2 facts come from the third-party mirror `Renekovski/26.2-mcp` at commit `51f3128` (the same mirror the hitbox design cites). The clones are read only. Nothing was built or run.

---

## 1. Repos read

| Repo | Branch | Commit | Date | Licence (fact) | Loaders | MC versions | Size | Maintenance |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| tr7zw/EntityCulling | `main` | `7e62c6e` | 2026-10-08 | tr7zw Protective License | Fabric, NeoForge (Forge on older) | `modern.json` lists 26.1, 26.2, 26.3 | 2,379 Java lines (plus a separate occlusion library) | Active, 339 stars |
| LogisticsCraft/OcclusionCulling (the ray library under it) | `master` | `7a346ea` | 2024-03-03 | MIT | library | none | 514-line core class | Stable, rarely changed |
| fxmorin/moreculling (+ wiki) | `master` | `83ec119` | 2026-10-06 | GPL-3.0 | Fabric, NeoForge | 26.3 (`gradle.properties`) | 139 Java files | Active, 188 stars |
| Traben-0/Entity_Texture_Features | `ETF-Main` | `ba6dfec` | 2026-10-04 | LGPL-3.0 | Fabric, NeoForge (Forge to 26.2) | `versions/` has 1.20.1 to 26.3 | 196 Java files, 21,678 lines | Active, 170 stars |
| FoundationGames/EnhancedBlockEntities | `1.21.4` (newest branch) | `b0b202a` | 2025-05-26 | LGPL-3.0 | Fabric only | 1.21.4 | small | **Stale for 26.x.** No branch after 1.21.4 |
| EdeenMC/betterblockentities (BBE) | `main` | `7ecbd75` | 2026-09-15 | LGPL-3.0 | Fabric; NeoForge module has 4 files | 26.3 (`BuildConfig.kt`) | 6,441 Java lines | Active, 85 stars. Needs Sodium `0.9.2+mc26.3` |
| MehVahdJukaar/modelfix-multi | `master` (config says 1.19.2) | `bcf9b71` | 2026-02-03 | GPL-3.0 | Fabric, Forge | newest branch is `1.21.10` | 5 Java files | **No 26.x branch** |
| txnimc/SodiumDynamicLights (SDL) | `main` | `64b442f` | 2025-04-13 | MIT | Fabric, NeoForge | 1.21.5 | 1,811 Java lines | **Stale for 26.x** |
| LambdAurora/LambDynamicLights (SDL's upstream) | `26.3` | `b9c8a46` | 2026-09-15 | "Lambda License" (own text) | Fabric, NeoForge | 26.3 | has an `api` module | Active |
| UltimatChamp/BetterGrassify | `main` | `87015db` | 2026-09-16 | Apache-2.0 | Fabric (NeoForge via Sinytra Connector, README) | `versions/` has 1.21.1 to 26.3 | about 1,000 lines | Active |
| MoePus/CreateBetterFPS | `main` | `5e6c217` | 2026-04-19 | MIT | Forge | 1.20.1 only (`gradle.properties`) | 1,033 Java lines | Maintained on 1.20.1, no port |
| Patbox/EntityViewDistance (EVD) | `main` | `30dda3e` | 2026-06-16 | LGPL-3.0 | Fabric only | 26.2 (`gradle.properties`) | 25 Java files | Active |

---

## 2. Summary: what matters most

1. **EntityCulling is safe for a 12.5-block Brachiosaurus and needs only a correct render box.** The default size limit is 50 blocks `[v]`. The box comes from vanilla `EntityRenderer.getBoundingBoxForCulling`, the same method that vanilla frustum culling uses `[v]`. Nublar's Brachiosaurus is registered `sized(2, 3)` today `[v]` `EntityInit.java:36`. If the renderer keeps the default, the neck and head vanish when the 2x3 box is hidden. One override of `getBoundingBoxForCulling` fixes vanilla, EntityCulling, and Sodium-side entity checks at once (plan issue #63, module B16).
2. **Client tick culling is the real gameplay risk.** A culled or off-screen entity gets `setOldPosAndRot`, `tickCount++` and `aiStep()` on the client, not `tick()` `[v]` `ClientWorldMixin.java:34-110`. Any client-only logic in a Nublar entity's `tick()` (footstep sound, shake, particles, animation flags) stops while the animal is not on screen. Put that logic in `aiStep`, in packets, or in the renderer.
3. **Fence wires are the same problem that broke Connectible Chains and CC:Tweaked monitors.** Both were culled when their anchor blocks or entities were hidden, because the culling box covered only the anchor `[doc]` EntityCulling issues #271 and #313. Nublar's `ElectricWireRenderer` draws long spans from the post block entity `[v]` `ElectricWireRenderer.java:25-45`. The block entity has no render box that covers the span. NeoForge has `getRenderBoundingBox`; Fabric has no equivalent, but EntityCulling mixes one in for mods `[v]`.
4. **Baked-mesh block entities (EBE, BBE) show the largest block-entity gain, and neither helps Nublar directly.** Both support only a fixed list of vanilla block-entity types `[v]`. The lesson is the pattern: draw the idle machine or fence post as a normal block model, and run the block-entity renderer only while something moves. Vanilla already allows this (`RenderShape.MODEL` plus a renderer). Nublar sets `ENTITYBLOCK_ANIMATED` on every fence and machine block `[v]`, so every fence block entity renders every frame.
5. **Do not make bioluminescence a light source.** One moving light entity schedules up to 8 section rebuilds each time it moves 0.1 block `[v]` SDL `EntityMixin.java:162-200`. A herd of 20 glowing animals would rebuild chunks constantly. Use an emissive layer (render only). Offer dynamic light as an optional compat data file.
6. **ETF works by texture identifier, so Nublar's static layer textures are ETF-friendly and runtime-composited textures are not.** Nublar already uses fixed paths per species and gender `[v]` `DinoLayer.java:46-80`. Keep it that way. Do not ship a second, competing variant system. Do not create a new texture identifier per animal.
7. **Tracking range:** Nublar registers `clientTrackingRange(10)` for every species `[v]`. That is 160 blocks, equal to vanilla's Giant (12 blocks tall) and the Ender Dragon `[v]`. Use 8 for small species, 10 for T. rex and Triceratops, 12 for Brachiosaurus. Details in section 10.

---

## 3. Vanilla 26.2 facts the rest of the note relies on

These are `[v]` from the `26.2-mcp` mirror at `51f3128`. They matter because they are platform behaviour: Nublar uses them directly and never copies a mod.

| Fact | Where |
| --- | --- |
| Entity render decision: `entity.shouldRender(cam)` first, then `affectedByCulling`, then the frustum test against `getBoundingBoxForCulling(entity).inflate(0.5)` | `client/renderer/entity/EntityRenderer.java:61-86` |
| `getBoundingBoxForCulling` returns `entity.getBoundingBox()` by default. `affectedByCulling` returns `true` by default. Both are `protected` and meant to be overridden | `EntityRenderer.java:88-94` |
| Render distance is `boundingBox.getSize() * 64 * viewScale` blocks, where `getSize()` is the mean edge length of the **hitbox** | `world/entity/Entity.java:1998-2014` |
| Server tracking range is `clientTrackingRange * 16` blocks. A player sees an entity when the **horizontal** distance (x and z only) is within `min(range scaled, playerViewDistance * 16)` | `server/level/ChunkMap.java:1142`, `1403-1406` |
| The dedicated server scales every range by `entity-broadcast-range-percentage` (default 100, allowed 10 to 1000) | `server/dedicated/DedicatedServer.java:792-794`, `DedicatedServerProperties.java:114` |
| `EntityType.Builder` defaults: tracking range 5 chunks, update interval 3 ticks | `world/entity/EntityType.java:1626-1627` |
| Vanilla values for big mobs: Giant (3.6 x 12.0) and Ender Dragon (16 x 8) both use range 10. Player uses 32 with interval 2 | `EntityType.java` (Giant near line 535, dragon line 428, player line 1176) |

---

## 4. EntityCulling (tr7zw)

**In game:** an animal behind a hill, or a machine inside a closed room, costs no draw call. The player sees nothing different, except when a mod's box is wrong, and then a thing blinks out while it is in plain view.

### Mechanism

| Step | What happens | Source |
| --- | --- | --- |
| 1. Main thread, every 5th client tick (`captureRate = 5`) | Copies entity data and block-entity data into lists for the worker. Block entities: the 17 x 17 chunks around the player | `EntityCullingModBase.java:130-137`, `158-183`; `Config.java:34` |
| 2. Worker thread `CullThread` | Loop sleeps 10 ms (`sleepDelay`). It runs a pass when the camera moved or a cull was requested. A request is set on each client tick and each world tick | `CullTask.java:53-76`; `EntityCullingModBase.java:65-67`, `146` |
| 3. Per target | Skips if whitelisted, glowing, forced visible, farther than `tracingDistance` (128), or larger than `hitboxLimit` (50 blocks on any axis). Otherwise it asks the ray library `isAABBVisible` | `CullTask.java:84-123` |
| 4. Visible target gets a 1,000 ms grace | `setCulled(false)` calls `setTimeout()`, which forces "visible" for one second. This hides pop-in | `CullableMixin.java:32-48` |
| 5. Render side | A culled entity is replaced by an empty `INTERACTION` render state with only a name tag. A culled block entity returns no render state. Both are cheap | `WorldRendererMixin.java:47-82`; `BlockEntityRenderDispatcherMixin.java:29-91` |
| 6. Occlusion | Ray test uses `state.isSolidRender()` per block, so only full opaque cubes block sight. A fence, a glass pane, a machine with a non-full model, never blocks | `Provider.java:22-35` |
| 7. Ray library | The target box is grown by 0.5 block. The library walks the voxels that face the camera, casts rays to up to 14 sample points per voxel, and returns on the first visible voxel. A byte cache (one per pass) skips voxels it already knows | `OcclusionCulling/.../OcclusionCullingInstance.java:50-140`, `18-93`; `cache/ArrayOcclusionCache.java:6-30` |

The ray library is a separate repo (`com.logisticscraft.occlusionculling`). It has no Minecraft code. EntityCulling feeds it blocks through the `DataProvider` interface `[v]`.

### How it treats a very large animal (Brachiosaurus)

| Question | Answer |
| --- | --- |
| Is a 12.5-block animal skipped as "too big"? | No. The limit is 50 blocks per axis `[v]` `Config.java:33`. Brachiosaurus is far under it |
| Which box does it use? | `EntityRenderer.getBoundingBoxForCulling(entity)` on 1.21.2 and later (26.3: with a `partialTicks` argument) `[v]` `EntityRendererMixin.java:76-117`; `NMSCullingHelper.java:33-55`. That is the vanilla method, so the renderer controls it |
| How does an animal opt out? | Override `affectedByCulling(entity)` to return `false` in the renderer. EntityCulling reads it as "ignores culling" for both rendering and tick culling `[v]` `EntityRendererMixin.java:29-85`; `ClientWorldMixin.java:46-52` |
| What happens if the box is too small? | The animal is culled while its neck or tail is on screen. The 1,000 ms grace only delays it. Same failure as Connectible Chains `[doc]` issue #271 |
| Cost of a big box | Cost grows with the surface voxels of the grown box. A 5 x 12.5 x 14 block box grows to about 6.5 x 14.5 x 15.5 voxels (about 1,460 voxels, about 420 facing the camera). `[inf]` Worst case (fully hidden, every ray tested to the end): about 2,000 rays of up to 128 steps, so about 2 to 3 ms on the **worker thread** for that one animal. A visible animal exits on its first visible voxel, usually under 0.05 ms. This is an estimate, not a measurement |
| Is the main thread affected? | Only the 5-tick data copy and the per-entity flag check. Both are small `[v]` |

### How it treats multipart entities (planned section hitboxes)

| Case | Result |
| --- | --- |
| Parts are hitbox-only entities with **no renderer** (the design in `docs/dinosaur-hitboxes-and-movement-design.md`) | Safe. `NMSCullingHelper.ignoresCulling` returns `true` when `getRenderer(entity)` is `null` `[v]` `NMSCullingHelper.java:17-31`. No part is ever culled or tick-skipped |
| The parent's culling box | Must cover all parts. The hitbox design already says so and caches the union once per tick `[doc]` design doc line 276. EntityCulling adds no extra rule |
| Parts that **render** as separate entities with their own small boxes | The Twilight Forest Naga torso bug: parts vanish when the culling mod uses each part's own box `[doc]` Twilight Forest issue #2579 (open; the report blames a different culling mod, RuOK, but the cause is the same) |
| Whether parts appear in EntityCulling's list | EntityCulling reads `client.level.entitiesForRendering()` `[v]` `EntityCullingModBase.java:160`. Whether NeoForge part entities appear there is `[inf]`: unknown. Not a problem for renderer-less parts |

### Does it skip client entity ticking?

Yes, and this is the risk for Nublar.

| Detail | Source |
| --- | --- |
| Config `tickCulling = true` by default | `Config.java:35` |
| A tick is skipped when `isCulled()` or `isOutOfCamera()` is true. `outOfCamera` becomes `true` the first time the entity is ticked, and the renderer resets it to `false` each frame the entity renders. So an entity that was not rendered since its last tick skips the next tick | `ClientWorldMixin.java:58-69`; `WorldRendererMixin.java:76-77` |
| What replaces the tick | `setOldPosAndRot()`, `++tickCount`, and for a `LivingEntity` `aiStep()` plus the `hurtTime` countdown. Warden heartbeat is special-cased | `ClientWorldMixin.java:90-110` |
| Never skipped | The player, the camera entity, passengers and vehicles, minecarts, entities whose renderer says `affectedByCulling == false`, and whitelisted types | `ClientWorldMixin.java:46-57` |
| Server | Untouched. README states "No impact on server-side simulation" `[doc]` | README "Entity Tick Optimization" |

**What this means for Nublar `[inf]`:** `Dinosaur.tick()` on the client does not run for off-screen animals. `aiStep()` still runs, and so do animation updates driven by render. Client work that must continue off screen (a roar sound at 40 blocks, a footstep thump, a herd dust particle) belongs in `aiStep()` or in a server packet, not in `tick()`. Check `Dinosaur.java` and `DinoAnimationUtils.java` for client-only state in `tick()` during B16/B25.

### Whitelist, config and API for mods

| Mechanism | Detail | Source |
| --- | --- | --- |
| User config lists | `entityWhitelist` (no culling and no tick culling), `tickCullingWhitelist` (no tick culling), `blockEntityWhitelist`. Lists hold registry ids. Unknown ids are ignored | `Config.java:13-84`; `EntityCullingModBase.java:70-95` |
| Default lists name other mods by id | Create, Botania, Cinematic Cataclysm, Quark and others. A mod can ask tr7zw to add its ids (issue #253 added a Voidscape entity) | `Config.java:13-84`; `[doc]` issue #253 |
| Code API | `EntityCullingModBase.instance.addDynamicEntityWhitelist(Function<Entity,Boolean>)` and `addDynamicBlockEntityWhitelist(...)`. These turn culling off per instance and per moment | `EntityCullingModBase.java:233-251`; used in `CullTask.java:97, 144` |
| Renderer hooks (no dependency) | Entity: override `affectedByCulling` and `getBoundingBoxForCulling` (vanilla). Block entity: NeoForge `IBlockEntityRendererExtension.getRenderBoundingBox` is honoured. On Fabric EntityCulling mixes `BlockEntityRenderFabricExtension.getRenderBoundingBox(T)` into every `BlockEntityRenderer` | `EntityCullingMod.java:52-67`; `BlockEntityRenderFabricExtension.java:1-16`; `mixin/BlockEntityRendererMixin.java:7-14` |
| Block entity opt-out | `shouldRenderOffScreen()` returning `true` skips both frustum and ray culling for that block entity | `BlockEntityRenderDispatcherMixin.java:62-64` |
| Block-entity frustum test | `blockEntityFrustumCulling` (default true) tests the block entity's box. A span that leaves the screen but whose anchor is off screen is dropped. tr7zw said he will "turn it off by default" `[doc]` issue #313 | `Config.java:89`; `BlockEntityRenderDispatcherMixin.java:65-75` |

There is **no published API artifact** and no tag-based opt-out. The two renderer hooks above need no dependency, so use them.

### Lesson for Nublar (modules B16, B15, B12, B20, B25)

1. **B16 render box:** override `getBoundingBoxForCulling` in `DinosaurRenderer` to return the union of the scaled model box (head to tail tip, including the growth scale). Today the render box follows the hitbox `sized(...)` values `[v]`. This is plan issue #63. Do it once and cache it per tick (the hitbox design already plans the union).
2. **B15 fence wires:** give the wire a real render bounding box that covers its span (NeoForge: renderer method; Fabric: the EntityCulling extension interface, which needs a soft dependency, see below). Without it, a 15-block wire disappears when both posts are hidden or off screen.
3. **B12/B20 machines and feeders:** their `ProcessorBlockEntity.getRenderBoundingBox()` exists but sits on the **block entity**, which is the 1.20.1 Forge location `[v]` `ProcessorBlockEntity.java:324`. In 26.x the method lives on the renderer. Port it during P2 for all renderers with parts outside the block (processor, sequencer, feeder arms).
4. **Occlusion rule:** any Nublar block with a non-full model must not report `isSolidRender` as `true`. A machine marked full-opaque with a cut-out model hides animals and items behind it from the ray test. Use `.noOcclusion()` on non-cube blocks `[inf]`.
5. **Cull-aware client code (B25):** list every client-only effect in `Dinosaur.tick()` and move each to `aiStep()` or a packet.

### Compatibility duty

| Duty | Reason |
| --- | --- |
| Override `getBoundingBoxForCulling` to match the model | Else neck and tail pop out |
| Keep `affectedByCulling` at its default (`true`) for dinosaurs | Culling saves most when 100 animals stand in a park. Only opt out a few effect entities |
| Parts: no renderer, no `ec$` state needed | Auto-ignored |
| Wire and arm block entities: render box plus `shouldRenderOffScreen` only where needed | See above |
| No client-only logic in `tick()` | Tick culling |
| Optional: call `addDynamicEntityWhitelist` for a "dinosaur being sedated or in a cutscene" if one appears | Not needed now |

### Platform overlap

Vanilla gives `getBoundingBoxForCulling` and `affectedByCulling`. NeoForge gives `getRenderBoundingBox`. Fabric API has none that I found (a code search of `FabricMC/fabric` for `getRenderBoundingBox` returned nothing; CC:Tweaked's author asked for one in issue #313 `[doc]`).

**Verdict: Compat.** Do not depend on it. Use the vanilla renderer hooks. Install it in every playtest.

---

## 5. MoreCulling (fxmorin) and its wiki

**In game:** fewer hidden block faces are drawn: bamboo, chorus, fences, end rods, item frames, paintings, leaves (optional modes), signs, rain. Wiki figures: bamboo scene about 136 to 210 fps, 7,350 item frames 35 to 55 fps, 330 paintings about 390 to 490 fps `[doc]` (author's single-machine screenshots, "results may vary drastically").

### Mechanism (block faces, the part that touches Nublar)

| Step | What happens | Source |
| --- | --- | --- |
| Hook into Sodium | A Sodium mixin cancels `shouldDrawSide` and calls MoreCulling's check when the block is allowed | `mixin/compat/BlockOcclusionCache_sodiumMixin.java:26-55` |
| The check | 1. `skipRendering` (vanilla). 2. Block's own `customShouldDrawFace` if it opts in. 3. If the neighbour can occlude, or **both** faces have quads and the neighbour face has no translucent texture: compare the two **culling shapes** face to face and skip the face if one covers the other | `utils/CullingUtils.java:43-103` |
| Culling shape source | 1. A model's `cullshapes` or `useModelShape` JSON. 2. Else the block's occlusion shape. 3. Full block shape: fast path | `mixin/models/cullshape/BlockStateBase_cullShapeMixin.java:39-87` |
| Translucency | Computed once per model at resource reload, cached as a boolean on the baked model. Translucent faces are never culled against (trapdoor x-ray protection) | wiki `BlockState-Culling.md` "Why does Mojang not also do this?" `[doc]`; `Block_drawSideMixin.java:31-39` |
| Mod scope | At game start, each block gets `canCull` from the config map keyed by **namespace**. `useOnModdedBlocksByDefault = true` | `Minecraft_loadBlocksMixin.java:30-48`; `MoreCullingConfig.java:51-54` |
| Opt-out for blocks | Block tag `moreculling:dont_cull`, config list `dontCull`, per-namespace switch, or the `MoreBlockCulling` interface | `MoreCulling.java:22-23`; `api/block/MoreBlockCulling.java` |

**Default is on for modded blocks.** The wiki states the assumption: "all blocks have the proper culling hitbox setup like vanilla" `[doc]` `BlockState-Culling.md`, section "Mod incompatibilities". A model that does not match its occlusion shape shows holes (x-ray).

### Effect on custom block models

| Nublar block | Risk |
| --- | --- |
| Fence post, wire, pylon, conduit, if they become real block models (the recommended direction in section 7) | Their model faces touch neighbours. Shape must match the model. Wires and posts are thin, so culling removes few faces, and the risk is low |
| Fossil ore with an overlay layer (two quads in one block) | Overlay quads are translucent or cut-out. MoreCulling will not cull against translucent textures `[doc]`, so the overlay stays. The base cube is a full block |
| Machines with partial models (processor, sequencer, incubator, generator) | **Main risk.** If the block reports a full shape but the model is smaller, neighbours cull their faces and a hole shows |
| Egg block (small, round) | Low. Not full, no occlusion |

### Lesson for Nublar (B12, B14, B15, B20 plus the egg block)

1. Give every non-cube block a correct `getOcclusionShape` (or `getCullingShape` in older mappings) that matches its model. This also helps vanilla and Sodium.
2. Add the `cullshapes` array to the few block models that are complex, for example a machine with a recessed front. A Nublar block model JSON stays valid in vanilla (unknown keys are ignored `[inf]`).
3. Do not set `canOcclude` on a block with holes.
4. Ship a `moreculling:dont_cull` tag entry only if a playtest shows a hole. Do not pre-emptively opt out (that costs the gain on hundreds of fence posts).

### Compatibility duty

Correct occlusion shapes. Nothing else. Do not implement `MoreBlockCulling` (a compile dependency for no gain).

### Platform overlap

Vanilla has `getOcclusionShape`, `noOcclusion()` and `skipRendering`. MoreCulling only widens the check. The other features (item frames, paintings, signs, leaves, rain, beacon) do not touch Nublar blocks. The leaves modes (`LeavesCullingMode`) change vanilla leaves only `[v]` `LeavesBlock_typesMixin.java:28-75`. If a Nublar leaf-like block ever exists, extend vanilla `LeavesBlock` (wiki advice) `[doc]`.

**Verdict: Compat (shape correctness only).**

---

## 6. Entity Texture Features (ETF, Traben-0)

**In game:** a resource pack or data can give one entity type many skins (random, by biome, by health, by name tag, by time of day, and more), plus glowing parts and blinking eyes, with no code in the entity's mod.

### How ETF picks a texture variant

| Step | What happens | Source |
| --- | --- | --- |
| Hook point | ETF changes the texture **identifier** that goes into the vanilla render-type factories (`entityCutoutNoCull`, `entityTranslucent`, `eyes`, `entitySolid` and others), but only while an ETF entity state is "mounted" | `mixin/mixins/MixinRenderLayer.java:17-50`; `utils/ETFUtils2.java:171-188` |
| State mount | Around each model submit it mounts the entity's state (26.2: `ModelFeatureRenderer.prepareModel`). Also marks layer ("feature") rendering | `mixin/mixins/submit/Mixin_ModelRenderer.java:17-92`; `entity/renderer/MixinLivingEntityRenderer.java:48-79` |
| Variant lookup | A `.properties` file sits beside the base texture, in one of three folders: `textures/…`, `optifine/random/entity/…`, `etf/random/entity/…` | `texture_handlers/ETFDirectory.java:16-20`; `ETFTextureVariator.java:21-29` |
| Rules | Each numbered rule lists properties (biome, height, health, name, NBT, weather, moon phase, baby, block above or below, light, speed, team, difficulty, mod loaded, time, many more) and the variant numbers it can give | property classes under `features/property_reading/properties/` (about 50 files) |
| Result caching | One suffix per entity UUID in an LRU of 500 per texture. A rule that can change is re-tested every `textureUpdateFrequency` ticks (default `Fast`); the delay is spread by `abs(uuid.hashCode()) % delay` | `ETFTextureVariator.java:119, 197-212, 214-231` |
| Variant files | `texture2.png`, `texture3.png` and so on, found through the same directories | `ETFTextureVariator.java:154-167` |

### Emissive layers

| Detail | Source |
| --- | --- |
| Suffix list read from `optifine/emissive.properties`, `textures/emissive.properties` or `etf/emissive.properties`. Default suffix `_e` | `ETFManager.java:60-92` |
| For a base texture `x.png` ETF looks for `x_e.png` in the **same or a lower** resource pack. If found, a second draw pass renders it. Blink files `x_blink_e.png` and `x_blink2_e.png` work the same way | `ETFTexture.java:234-262` |
| Two modes: `DULL` (default) and `BRIGHT`; per entity override in config | `ETFConfig.java:50-54, 84-90`; `ETFManager.java:117-126` |
| ETF API v12 for mods: `getCurrentETFVariantTextureOfEntity`, `getCurrentETFEmissiveTextureOfEntityOrNull`, `renderETFEmissiveModel`, `registerCustomRandomPropertyFactory(modId, factories)` (add a new property name to `.properties` files) | `ETFApi.java:83, 205-290, 311-380, 434-438` |

### GeckoLib coexistence

| Point | Status |
| --- | --- |
| ETF source mentions GeckoLib | No hits in the source (`grep -i geckolib` over `src/`) `[v]`. ETF has no GeckoLib-specific code |
| Works anyway? | ETF changes ids inside vanilla render-type factories. A GeckoLib 5 renderer that builds its render type through the vanilla factories and runs inside the vanilla submit pipeline should be changed too. This is `[inf]`. The only public statement is the author's 2022 answer to "Is this compatible with geckolib?": it "seems to run fine" and custom models are not guaranteed `[doc]` ETF issue #3 |
| What to test | Run `runClient` with ETF installed. Add `x.properties` beside `textures/entity/gallimimus/male/base.png`, a `base2.png`, and an `_e` file. Check that one animal changes skin. Do this once at B09 or B24 |

### Can Nublar's multi-layer skin and colour system coexist?

| Question | Answer |
| --- | --- |
| Does the layer system keep ETF-friendly identifiers? | Yes. `DinoLayer.getTextureLocation` builds `textures/entity/<species>/<gender>/<layer>.png`, cached per species and gender `[v]` `DinoLayer.java:46-80`. Tint comes from vertex colour, not a new texture |
| Unique id per animal? | Avoid. ETF stores one `ETFTexture` per identifier in a plain `HashMap` that is never trimmed `[v]` `ETFManager.java:44, 280-290`. Each new `.png` identifier also triggers up to three resource lookups (emissive, blink, enchant) `[v]` `ETFTexture.java:66-74, 234-262`. A path that does not end in `.png` skips them `[v]` `ETFTexture.java:68`. Per-animal DynamicTexture ids would grow this map for the whole session |
| Who picks the variant? | **Nublar must.** Skin variant comes from the sequenced fossil biome and the player's free pick at 100% (design decisions, module 4). It is a server-synced gene, not a client guess. ETF re-tests rules on the client, so ETF could disagree with the genome. Keep variant choice in Nublar's `DinoLayer` path |
| Can packs still add skins? | Yes, without ETF, if the variant ids are datapack or asset names (for example `textures/entity/<species>/<gender>/<variant>/<layer>.png`). That is the same extensibility with no new dependency |
| Use ETF properties for Nublar's own variants? | Possible but not recommended. It would need a custom ETF property such as `nublar_variant` registered through `ETFApi.registerCustomRandomPropertyFactory` and a compile dependency. Benefit: resource-pack authors could write a `.properties` file. Cost: two systems decide the skin |
| Bioluminescence | Do it in Nublar's layer system (a glow layer drawn full bright, gated by the gene). If a pack ships `base_e.png` and Nublar also draws a glow layer, an animal would glow twice. Choose one: name Nublar glow layers `glow.png` (not `_e`) |
| Colour, saturation, opacity per layer | ETF cannot express these (it swaps whole textures). It does not conflict as long as colour stays a vertex-colour tint |

### Cost

ETF adds a texture-id swap per submitted model and an LRU lookup per entity per frame (`ETFTextureVariator.getVariantOfInternal`). For 100 animals with 6 layers each that is 600 swaps per frame `[inf]`: a map hit each, microseconds in total. The risk is memory growth from unique ids, not CPU.

### Compatibility duty

| Duty | Reason |
| --- | --- |
| Stable texture identifiers (species, gender, layer, variant) | ETF caches per id |
| No per-animal runtime texture ids. If one is unavoidable, use a path that does not end in `.png` (ETF skips emissive lookups) `[inf]` | Unbounded cache and lookups |
| Build render types with vanilla factories where possible, so ETF's id swap and Iris/shader paths work | The swap lives in the factories |
| Do not use the file suffix `_e` for anything that is not an emissive map | ETF reads it |
| Block entities: ETF handles them too, with a UUID built from block state and position `[v]` `ETFApi.java:179-185`. Static baked block models (section 7) bypass ETF `[doc]` BBE README says ETF/EMF conflict with its baked path | Pack authors lose skins on baked machines |

### Platform overlap

Vanilla 26.2 resource packs already swap entity textures by path, but not by condition. Conditional variants are ETF's value. Nothing else covers it, and Nublar needs only its own genome rule.

**Verdict: Compat (do not build on it).** Record the test above as a B09/B24 acceptance step.

---

## 7. EnhancedBlockEntities (EBE) and BetterBlockEntities (BBE)

**In game:** 1,700 chests: 155% more frames (EBE README, author's screenshot) `[doc]`. BBE says "expect at least 3x" for ~29,000 chests `[doc]` (author's claim, not verified here). A chest, bed, sign, bell, shulker box or pot looks the same, gets smooth lighting, and draws from far away like a normal block. The lid animation still plays.

### Mechanism (shared idea)

A block entity renderer (BER) draws every visible block entity every frame. Both mods draw the **idle** state as part of the normal chunk mesh, and switch to the BER only while something moves.

| Part | EBE (1.21.4) | BBE (26.3) |
| --- | --- | --- |
| Make the block render as a model | A mixin on `getRenderType` returns `MODEL` for registered blocks `[v]` `mixin/AbstractBlockStateMixin.java:12-18` | A mixin into Sodium's block renderer emits chest, shulker, bell, pot, banner and statue geometry into the section mesh `[v]` `client/chunk/pipeline/BBEBlockRenderer.java:47-120` |
| Decide per block entity | `BlockEntityRenderCondition`: `NON_ZERO_STATE` means "draw the BER only while the animation state is above 0" `[v]` `client/render/BlockEntityRenderCondition.java:34-47` | `RenderingMode { IMMEDIATE, TERRAIN }`, and a manager with phases `IDLE`, `IMMEDIATE_ACTIVE`, `WAITING_TERRAIN` `[v]` `InstancedBlockEntityManager.java:19-24` |
| Trigger | The chest's `clientTick` mixin reads the lid animation and changes a model state `[v]` `mixin/ChestBlockEntityMixin.java:27-35` | `lidAnimateTick` mixin calls `manager.tick(open?, option)` `[v]` `…/ChestBlockEntityMixin.java:41-49` |
| Switch back to terrain | Rebuild the chunk section, then run a task after the rebuild `[v]` `mixin/BuiltChunkMixin.java`; `util/WorldUtil.java:22-29` | Request a Sodium section rebuild with a callback ("fence") that fires after upload `[v]` `client/chunk/section/SectionUpdateDispatcher.java:14-44` |
| Which block entities | Registry of vanilla types only `[v]` `EnhancedBlockEntityRegistry.java:15-23` | A fixed enum of 10 vanilla types (chests, signs, shulker, pot, banner, bell, copper golem statue) `[v]` `data/SupportedBlockEntityTypes.java:7-17`. Mixin code states "skip if the declared BlockEntityType is not supported (could be a modded block entity)" `[v]` `BBEBlockRenderer.java:76-80` |
| Mod hook | EBE: a `ebe_v1` entrypoint lets a mod force-off an EBE option `[doc]` README | BBE: `BBEApiEntryPoint.registerRenderers` registers an alternative renderer for one of the 10 supported types only `[v]` `registration/AltRendererRegistration.java:9-13` |
| Dependencies | Fabric API; Sodium 0.6+ `[doc]` | Sodium (exact version) `[doc]` README; `BuildConfig.kt:20` |

### What this means for Nublar (modules B12, B14, B15, B20)

Neither mod helps Nublar's block entities, because both ignore unknown types. Nublar's machines and feeders are all `ENTITYBLOCK_ANIMATED` GeckoLib block renderers `[v]` `MultiEntityBlock.java:92-93`, `DinosaurFeederBlock.java:105-106`, `ElectricFenceBlock.java:37-38`, `ElectricFencePostBlock.java:73-74`. This means a park of 300 fence posts and wires submits 300 or more block-entity renders every frame, even when nothing moves `[inf]` (GeckoLib bone setup plus a draw per block entity).

**The pattern, adapted with vanilla means only:**

| Idea | Nublar use |
| --- | --- |
| Static look as a normal block model, moving or per-state look as a BER | Fence posts and wire stubs: a block model per `index` state (the post already has an `index` property `[v]` `ElectricFenceRenderer.java:58-69`). The BER draws only the live spark, a broken half, or the free-angle wire |
| BER only while animating | Machines (processor, sequencer, incubator, egg printer): render shell and frame as a block model. Show a GeckoLib BER only while the machine runs (bones moving, screen, arm) |
| Vanilla supports the hybrid | A block can have `RenderShape.MODEL` and a BER at once (vanilla does it for some blocks). EBE needed a mixin only because vanilla chests are `ENTITYBLOCK_ANIMATED` `[inf]` |
| Switch costs a section rebuild | A state change that swaps model and BER is a blockstate change, which already rebuilds that section in vanilla. Do not change blockstate every tick |

**Wires between posts at free angles** cannot be a static block model (the connection data is a free vector set, `ElectricWireRenderer` passes 39 floats per connection `[v]` `ElectricWireRenderer.java:25-45`). Options that need no mod: (1) keep the BER for wires only; (2) render each wire **once per connection** (not once per post); (3) add a render box (section 4); (4) cache the wire mesh per connection as a vertex template instead of rebuilding 39 values per frame (the CreateBetterFPS idea, section 11). Choose in B15's task plan after measuring.

**Do not** hard-depend on BBE or EBE. They bind to Sodium internals and pin an exact Sodium build.

### Compatibility duty

| Duty | Reason |
| --- | --- |
| Do not add Nublar blocks to vanilla types BBE handles | They are vanilla only; nothing to do |
| Do not rely on the BER for blocks that other mods might bake | Not applicable to modded types |
| ETF/EMF conflict: BBE README says ETF/EMF do not work on its baked types | Nublar's own baked parts would also bypass ETF skins `[inf]`. Acceptable |

### Platform overlap

Vanilla: `RenderShape`, block models, BER. GeckoLib 5: `GeoBlockRenderer`, bone visibility (hide the parts that the block model already draws `[inf]`, check in B24).

**Verdict: Adapt (pattern, not code).** Effort L for the machines (needs exported shell models, ties to B24), M for fence posts. EBE: **Not relevant** (stale, Fabric 1.21.4). BBE: **Not relevant as a dependency**; read it as the 26.x reference for the pattern.

---

## 8. modelfix-multi (MehVahdJukaar)

**In game:** thin gaps ("seams") between touching faces of custom block and item models disappear. Fence posts made of several boxes, and 2D items, show fewer sparkling lines.

### Mechanism

The mod cancels vanilla's UV "shrink" (texture zoom) so every face uses 100% of its sprite area, and it rebuilds item-generator side quads so there are no tiny gaps `[doc]` README. In source: a mixin on `TextureAtlasSprite.uvShrinkRatio` replaces the ratio, and an `ItemModelGenerator` overwrite changes span creation `[v]` `common/.../mixins/TextureAtlasSpriteMixin.java:23-27`; `ItemModelMixin.java:21-35`.

### Status

| Fact | Source |
| --- | --- |
| `master` builds for 1.19.2; the newest version branch is `1.21.10` | `gradle.properties`; `gh api …/branches` |
| No 26.x branch | branch list |
| It targets Mojang bug MC-73186 | README |
| Status of MC-73186 in 26.2 | **Not verified.** The Mojira page did not load in this session. Check `mojira.dev/MC-73186` by hand before deciding |

### Lesson for Nublar (B12, B15, B20, B24)

Nublar's block models are built from small boxes (machine shells, feeders, posts). If a 26.2 seam shows between boxes of one model, the fix has two parts that need no mod: overlap the boxes by about 0.001 block in the Blockbench or JSON export, or share UV edges. Record this as a B24 export checklist item. Do not depend on the mod.

### Compatibility duty

None. Players may install it; it changes UVs globally and does not alter geometry.

### Platform overlap

None on the platform side. The fix is an asset practice.

**Verdict: Not relevant as a dependency (no 26.x port). Adopt the asset practice (S).**

---

## 9. SodiumDynamicLights and LambDynamicLights

**In game:** a held torch or a burning mob lights nearby blocks, smoothly and moving. The mod works only where a light source exists.

### Mechanism (SDL, 1.21.5; upstream has the same design)

| Step | What happens | Source |
| --- | --- | --- |
| Light value | Every client tick, each entity computes a luminance (0 to 15): burning = 15, plus a registered handler for its type | `mixin/lightsource/EntityMixin.java:81-95, 144-151` |
| Registration | Code: `DynamicLightHandlers.registerDynamicLightHandler(EntityType, DynamicLightHandler)`. Items: JSON files under `assets/.../dynamiclights/item/` | `dev/lambdaurora/.../DynamicLightHandlers.java:71-83`; resources dir |
| Where light shows | The lightmap lookup (`getLightmapCoordinates`) is wrapped and returns the larger of vanilla and dynamic. Range 7.75 blocks; falloff `1 - dist/7.75` | `SodiumDynamicLights.java:130-131, 398-399`; upstream `HOW_DOES_IT_WORK.md` "Lightmap coordinates method" `[doc]` |
| Cost | Sodium bakes light into the chunk mesh, so the mod **schedules a section rebuild** for the entity's section and 7 neighbours whenever the entity moves more than 0.1 block or its luminance changes. Delay modes: `REALTIME` 0 ms, `FAST` 250 ms, `SLOW` 500 ms | `EntityMixin.java:159-200`; `DynamicLightsMode.java:21-24` |
| Upstream (26.3) | Entity lights are **data**: `assets/<ns>/dynamiclights/entity/<name>.json` with `match` (entity predicate) and `luminance` (a number, or a typed provider such as `lambdynlights:glow_squid`). Mods may add typed providers through the API. The upstream adds a culling-aware rebuild scheduler | `…/entity/allay.json`, `blaze.json`; `api/.../EntityLuminance.java`; `engine/scheduler/*` |

### Effect for the bioluminescence gene (B10)

| Option | Cost |
| --- | --- |
| **A. Emissive glow layer** (recommended default) | Render only. No chunk rebuild. No dependency. Works with shaders (Iris) the same way vanilla glow squid eyes do `[inf]` |
| **B. Dynamic light via data** | One JSON per entity type is not enough, because the gene is per animal. A typed luminance provider in code (registered through the upstream API) can read the gene. Cost: up to 8 section rebuilds each time a lit animal moves 0.1 block `[v]`. For a herd of 20 lit animals walking, hundreds of rebuilds per second in REALTIME `[inf]`. The player's own mode setting limits it; Nublar cannot |
| **C. Real block light** (server-side light blocks following the animal) | Worst. Changes server light data and sends light packets. Reject |

Recommend A for Beta, and B only as an optional compat file set behind a soft mod check, limited to a small light level (4 to 6) so the lit radius stays small `[inf]`.

### Lesson for Nublar (B10, B25)

Any Nublar feature that makes light follow an entity (a lit vehicle headlight in B22, a glowing egg, a flare) must be render-only or a block that does not move. A moving vehicle with headlights as a dynamic source is acceptable for one vehicle, not for 20 dinosaurs.

### Compatibility duty

| Duty | Reason |
| --- | --- |
| Do not emit `Entity.isOnFire`-style max light for ordinary states | SDL treats a burning entity as 15 |
| Provide an emissive layer that uses vanilla-style full-bright (light coords `0xF000F0`) | Works with and without the mod. The fence wire renderer already does this `[v]` `ElectricWireRenderer.java:25-27` |
| Optional: data file `assets/projectnublar/dynamiclights/entity/*.json` for vehicles | Zero code. Silent if the mod is absent |

### Platform overlap

Vanilla has no dynamic light. NeoForge and Fabric API do not either. Sodium has no API for it.

**Verdict: Compat (optional, vehicles only). Avoid for dinosaurs.** SDL itself is stale (1.21.5); use the active upstream LambDynamicLights `26.3` as the reference.

---

## 10. EntityViewDistance (Patbox)

**In game:** a server owner (or player) sets a different view distance for each entity type. Item frames can stop drawing at 32 blocks. Animals can stay visible at 200.

### Mechanism

| Side | What happens | Source |
| --- | --- | --- |
| Server | A mixin on `ChunkMap.addEntity` replaces the tracking distance with the configured value; a second step rewrites the `range` of already-tracked entities when the config changes | `mixin/ChunkMapMixin.java:13-17`; `EvdUtils.java:114-127` |
| Client | A mixin on `Entity.shouldRender(DDD)` returns true only if the absolute x, y and z deltas are each under the configured distance (a cube, not a sphere) | `mixin/EntityMixin.java:147-157` |
| Storage | One int per `EntityType` (mixin field, default -1 = vanilla) | `mixin/EntityTypeMixin.java:26-39` |
| Config | `entityViewDistance` map id to blocks; `mode` = `server`, `client` or `both`. Maximum `32 * 16` blocks | `config/data/ConfigData.java`; `EvdUtils.java:83` |
| Compatibility | Works on vanilla clients and servers (server-only effect keeps vanilla protocol) `[doc]` README. Fabric only | README |

### What tracking range should Nublar register?

Facts that decide it (section 3): tracking range is `chunks * 16` blocks, **horizontal only**, and is capped by the player's view distance. The default of the builder is 5; vanilla uses 10 for mobs up to 12 blocks tall.

| Species | Proposed `clientTrackingRange` (chunks) | Blocks | Reason |
| --- | --- | --- | --- |
| Gallimimus, Velociraptor, Dilophosaurus (small) | 8 | 128 | Small body; hard to see beyond 100 blocks; vanilla animals such as cat and bee use 8 `[v]` |
| Tyrannosaurus, Triceratops | 10 | 160 | Same as the horse and Giant |
| Brachiosaurus | 12 | 192 | 12.5 blocks tall; players want to see it from a hill. Needs server view distance 12 or more to take effect |
| Eggs, vehicles | 8 and 10 | | Vehicles must follow the player; keep default for entities carrying passengers (`getEffectiveRange` takes the larger passenger range `[v]` `ChunkMap.java:1428-1438`) |

Notes:
- These are **estimates and proposals**. No Nublar measurement exists. They follow the vanilla scale. The lead decides the numbers.
- Current Nublar value is 10 for all six species `[v]` `EntityInit.java:30-39`. The change saves bandwidth for the three small species only; the saving per animal is the position packets for animals between 128 and 160 blocks away.
- `updateInterval` stays at the default 3 ticks. Parts and IK are computed on both sides from the entity state, so no extra position packets are needed (hitbox design).
- Client render distance is **separate**: `shouldRenderAtSqrDistance` uses `getBoundingBox().getSize() * 64 * viewScale` `[v]` `Entity.java:2006-2014`. For `sized(2,3)` that is about `(2+3+2)/3 * 64 = 149` blocks at default scale. When B16 grows the hitbox to the real size the render distance grows with it. For a Brachiosaurus of 12.5 tall the mean edge is far larger, so render distance is not the limit; tracking is.
- A server can scale every range with `entity-broadcast-range-percentage` (10 to 1000) without any mod `[v]`.
- Tracking range cannot come from the TOML config: the `EntityType` is built at registration, before config loads `[inf]`. Use constants in `EntityInit`.

### Lesson for Nublar (B25, B26)

Park servers with 100 animals can use EVD to trim far animals. Nublar should list its tracking numbers in `docs/config-reference.md` so server owners know the vanilla property and the EVD ids to use.

### Compatibility duty

Register every Nublar type with a `clientTrackingRange` that is large enough for the biggest model. Do not read `getType().clientTrackingRange()` for gameplay (EVD rewrites the live range, not the type value `[v]` `EvdUtils.java:125`).

### Platform overlap

Vanilla `EntityType.Builder.clientTrackingRange` and `updateInterval`, plus the `entity-broadcast-range-percentage` property, cover the default and the server-wide scale. EVD adds per-type override and a client video option.

**Verdict: Adopt (use the vanilla builder values). Compat for EVD (nothing to code).** Effort S.

---

## 11. CreateBetterFPS (MoePus)

**In game:** Create's spinning shafts, cogwheels and belts cost much less on the main thread when many are in view, with or without shaders.

### Mechanism

Create builds each moving part as a pre-baked mesh (`SuperByteBuffer`) and, each frame, copies it into the vertex buffer with a new matrix, light, tint and sprite shift. CreateBetterFPS replaces that class with `SodiumByteBuffer`, which writes the transformed vertices through Sodium's `VertexBufferWriter` in bulk (native memory scratch buffer, 64-byte aligned) and has separate vertex layouts for entity, terrain, Iris entity and Iris terrain `[v]` `mixin/SuperBufferFactoryMixin.java:13-19`; `mixin/SuperByteBufferBuilderMixin.java:35-49`; `renderer/SodiumByteBuffer.java:34, 95-103` and the `renderer/*Vertex.java` files. The README text is missing; the repo description is "standalone now.finally.." `[doc]`. It is Forge 1.20.1 only, with Create 6 as an optional dependency `[v]` `gradle.properties`; `mods.toml:65-68`.

### Lesson for Nublar (B12, B14, B15, B22)

| Create problem | Nublar version | Remedy that needs no mod |
| --- | --- | --- |
| Many moving mesh instances, each re-transformed on the CPU every frame | Fence wires (free-angle), generator rotors, vehicle wheels, a sequencer arm | Bake the mesh once and reuse it; move many static parts with one transform (matrix on the part), not per vertex; share one wire template per connection |
| Separate paths for Iris and non-Iris vertex formats | Any custom vertex writer | Use vanilla `VertexConsumer` calls inside the BER. Sodium and Iris both wrap that path (this is why the mod needs four vertex classes). Avoid writing raw vertex memory |
| Culling and visibility of moving parts | Same as section 4 | Render box |

Create's real fix for "many moving blocks" is Flywheel (instanced visuals). Nublar has few moving parts (a few per machine), so instancing is not worth a dependency `[inf]`.

### Compatibility duty

None for Nublar. If a Create compat for rotational power exists later (another researcher's topic), the CreateBetterFPS patch applies to Create's meshes, not Nublar's.

### Platform overlap

Sodium's `VertexBufferWriter` API is the platform piece. Vanilla `VertexConsumer` is the portable path.

**Verdict: Not relevant as a dependency. Adopt the idea (S): bake once, one matrix per part.**

---

## 12. BetterGrassify (UltimatChamp)

**In game:** the side faces of grass blocks next to a lower block show grass or snow in a connected way (OptiFine "better grass"). Fast and Fancy modes.

### Mechanism

A wrapper `BlockStateModel` (`WrapperBlockStateModel` from Fabric's renderer API on 26.x) pushes a quad transform. For each side quad of a full block with no block entity, it rewrites the sprite to the neighbour's top sprite when the rules say so `[v]` `model/BetterGrassifyBlockStateModel.java:46-120`. A Sodium mixin and a section-compiler mixin keep it working under Sodium. Config has `whitelistedBlocks`, `whitelistedTags`, `excludedBlocks`, `excludedTags` and `moreBlocks` `[v]` `config/BetterGrassifyConfig.java:25-58`.

### Lesson for Nublar

| Point | Detail |
| --- | --- |
| It skips any state with a block entity | `state.hasBlockEntity()` returns early `[v]` `BetterGrassifyBlockStateModel.java:90`. Nublar fences, posts and machines are never touched |
| The 26.x way to change quads per position | Wrap the model. Fabric API provides `WrapperBlockStateModel` and `QuadEmitter.pushTransform`. NeoForge has its own model API. Nublar does not need this now |
| Possible use | If a later feature wants fossil ore overlays that depend on neighbours (ore showing more or less of its base stone), the wrapper-model route is the pattern. Not planned |

### Compatibility duty

If Nublar adds a custom grass or dirt block (a "paddock turf" block), it joins the `moreBlocks` or tag whitelist only if the lead wants connected sides. Default: ignore.

### Platform overlap

Fabric Rendering API and NeoForge model APIs cover wrapper models. Vanilla covers nothing here.

**Verdict: Not relevant.**

---

## 13. Pulling it together: answers to the lead's questions

| Question | Answer |
| --- | --- |
| How does EntityCulling decide visibility? | Worker thread, voxel rays from the camera to up to 14 sample points per voxel of the target's grown box, opaque full cubes only block. Grace of 1 s on visible. 128-block limit, 50-block box limit, copy every 5 ticks (section 4) |
| Brachiosaurus at 12.5 blocks tall | Fine, if the render box matches the model. Add `getBoundingBoxForCulling` in `DinosaurRenderer` |
| Multipart section hitboxes | Parts without renderers are ignored by EntityCulling. The parent box must cover all parts. Parts that render themselves must not be culled by their own small boxes |
| Whitelist or API | Config lists by id, code functions for dynamic opt-out, and two renderer hooks that need no dependency |
| Does it skip client ticks? | Yes. `tick()` is replaced by `aiStep()` for culled or off-screen entities |
| EBE/BBE and Nublar BERs | Pattern only: idle = block model, active = BER. Neither works on modded types |
| ETF and Nublar skins | Stable ids, Nublar picks variants, ETF stays an optional pack-author tool. Test with ETF once |
| MoreCulling | Provide correct occlusion shapes. Default-on for modded blocks |
| SodiumDynamicLights | Do not use for the bioluminescence gene; use an emissive layer |
| EntityViewDistance | Register range per species: 8, 10, 12 chunks (proposal) |
| CreateBetterFPS | A Create-only patch. The idea (bake once) applies to wires and rotors |

### Anti-patterns that break these mods (collected)

| Anti-pattern | Breaks |
| --- | --- |
| Render box smaller than the drawn model (default hitbox for a scaled animal) | Vanilla frustum test, EntityCulling |
| A block entity that draws outside its own block with no render box | EntityCulling (Fabric and NeoForge), vanilla frustum |
| Full-opaque flag on a non-cube block | EntityCulling rays, MoreCulling face culling |
| Client-only logic in `tick()` | EntityCulling tick culling |
| A unique texture id per animal | ETF caches, GeckoLib texture memory |
| Moving light source per animal | LambDynamicLights/SDL chunk rebuilds |
| Many BERs that never go idle | Everything; no mod fixes modded BERs |
| Raw vertex memory writes | Sodium, Iris, ImmediatelyFast paths |
| Changing block state each tick to animate | Section rebuilds |

---

## 14. Closing table

Effort: S under 100 lines, M one or two classes, L new subsystem.

| Technique | Source | Nublar module | Verdict | Effort | Expected effect |
| --- | --- | --- | --- | --- | --- |
| Override `getBoundingBoxForCulling` to the scaled model box (cached per tick) | Vanilla `EntityRenderer`; EntityCulling uses it `[v]` | B16 (issue #63), B27 | Adopt (platform) | S | No pop-out of neck or tail; correct vanilla frustum test |
| Keep `affectedByCulling` true for dinosaurs; false only for effect entities | EntityCulling `[v]` | B16 | Adopt (platform) | S | Full culling gain for a 100-animal park |
| Render box for fence wires and arm-type block entities (NeoForge method; Fabric extension) | EntityCulling issues #271, #313 `[doc]` | B15, B12, B20 | Adopt, Compat on Fabric | S to M | Wires stay visible; fewer reports |
| Move client-only effects from `tick()` to `aiStep()` or packets | EntityCulling `ClientWorldMixin` `[v]` | B16, B25 | Adopt | S | Sounds and particles continue off screen |
| Correct `getOcclusionShape` on every non-cube block | MoreCulling, EntityCulling `[v]` | B12, B14, B15, B20 | Adopt | S | No holes, no x-ray, no hidden animals |
| Block model for idle look, BER for active parts | EBE and BBE pattern `[v]` | B12 (machines), B15 (posts) | Adapt | L machines, M posts | Fewer block-entity renders per frame; est. several-fold for 300 posts `[inf]` (BBE claims 3x on chests `[doc]`) |
| One wire render per connection, cached mesh, render box | CreateBetterFPS idea, EntityCulling issues | B15 | Adapt | M | Fewer draws and allocations per wire |
| Stable texture ids; Nublar picks variants; no per-animal ids | ETF `[v]` | B09, B24 | Adopt | S | Bounded ETF caches; consistent skins |
| ETF smoke test with a `.properties` + `_e` file | ETF | B09/B24 acceptance | Compat | S | Know if GeckoLib + ETF work |
| Emissive glow layer for bioluminescence, not a light source | SDL `[v]` | B10 | Adopt | M | No chunk rebuilds |
| Optional dynamic-light data for vehicles | LambDynamicLights data format `[v]` | B22 | Compat | S | Headlights where installed |
| `clientTrackingRange` per species: 8, 10, 12 chunks | Vanilla `EntityType`, EVD `[v]` | B16 / B25 | Adopt (proposal) | S | Less far-range traffic; Brachiosaurus seen from afar |
| Document the server property and EVD ids in `config-reference.md` | Vanilla property, EVD | B00 | Adopt | S | Server owners can tune |
| Overlap model boxes by 0.001 in exports | modelfix-multi README `[doc]` | B24 | Adopt (asset practice) | S | No seams |
| Hard dependency on any mod in this note | all | any | Avoid | | Keep all integration optional |

---

## 15. Open questions

1. **GeckoLib 5 and ETF.** Does ETF's id swap reach a GeckoLib 5 render type on 26.2? No source line proves it (section 6). Needs one run with both installed.
2. **Brachiosaurus tracking range.** Is 12 chunks (192 blocks) acceptable for network cost with 100 animals in a park, or should Brachiosaurus stay at 10? The lead chooses after the B25 profile.
3. **Fence post render path.** Should B15 split the post into a block model plus a small BER (spark, break state), or keep the GeckoLib block renderer and rely on render boxes only? Needs one measurement: frame time of 300 posts with each approach (no data exists today).
4. **Fabric render bounding box.** Fabric has no platform method. Accept a soft compile dependency on EntityCulling's `BlockEntityRenderFabricExtension`, or accept `shouldRenderOffScreen() == true` on wire and arm renderers (cost: no culling for them)? The interface only works if the renderer class implements it with a matching generic erasure, which a soft dependency cannot do without a compile-only stub `[inf]`.
5. **MC-73186 on 26.2.** Is the UV-shrink seam bug still present in 26.2? Check Mojira by hand; then decide whether the 0.001-block overlap rule is needed.
6. **Client `tick()` audit.** Which client-only effects does `Dinosaur` run in `tick()` today? A grep pass in B25 should list them before the culling mod is part of playtests.
