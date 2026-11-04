# 05 Polytone, Continuity, EMF: what they teach Project Nublar

Date of study: 2026-11-04. Labels: [v] read in source, [doc] from a page, [inf] my inference.
Clean room: mechanisms, key names, numbers only. No code copied.

## Scope

Pages read:
- Modrinth API records: api.modrinth.com/v2/project/polytone, /continuity, /entity-model-features [doc].
- Polytone wiki (GitHub): Home, Shared Concepts, Entity Modifiers, Particle Modifiers, Block Properties Modifiers, Models Improvements, Scripting Expressions, Custom Particle Types, Polytone Configs, Colormaps, Gui Modifiers, Custom Sound Events, Variant Textures [doc].
- Continuity README [v]; EMF FEATURES.md, CHANGELOG.MD, .github/emf_animation.txt [v].
- Issue trackers (gh search): EMF #451, #597, #360, #236; Polytone #411, #417, #109, #116, #210; Continuity #826, #810, #516, #823; GeckoLib searches (see Problems).

Repos cloned (shallow, read only, in the session scratchpad):
| Repo | Branch | Commit | Date |
|---|---|---|---|
| MehVahdJukaar/polytone | 26.2 | 545be37 (mod 26.2-6.9.1) | 2026-11-01 |
| MehVahdJukaar/polytone | 1.21.1 (default) | d5b7546 | 2026-10-28 |
| PepperCode1/Continuity | 26.2/dev | d15d442 "Require Minecraft ~26.2" | 2026-09-13 |
| Traben-0/Entity_Model_Features | default (mod 3.3.12) | c801331 | 2026-11-03 |

Not read: Nublar code (not needed; fit statements are [inf] from the brief). ETF source skipped, except where EMF needs it.
Not done: no build, no run, no in-game test.

---

## 1. Polytone

### Facts
| Item | Value |
|---|---|
| What | Client-side mod. Resource packs retune colours, particles, sounds, biomes, GUI, items, models, shaders through JSON "modifier" files [doc] |
| MC versions | 1.21.4 to 1.21.8, 26.1.2, 26.2, 26.3 [doc] |
| Loaders | Fabric, Forge, NeoForge [doc] |
| Dependencies | None required. Optional compat: Iris/Oculus, Sodium, Veil, EMF, ETF, Distant Horizons, season mods, Nautilus Studio editor [v CompatHandler.java] |
| Downloads | 19,153,014 (2,173 followers) [doc] |
| Last update | 2026-11-01 [doc] |
| Repo | github.com/MehVahdJukaar/polytone |
| Licence name | GPL-3.0-or-later [doc] |
| Env | Client only. Works on any server [doc] |

### How it works
- Every feature has a content manager that loads JSON from `assets/<ns>/polytone/<folder>/` at resource reload [v: content/*Manager classes; reloader/ContentManager.java].
- Folders seen: biome_modifiers, block_properties, colormaps, particle_modifiers, custom_particles, entity_modifiers, fluid_properties, item_modifiers, gui modifiers, dimension_effects, config_entries, global_expressions, sound_events.csv, variant_textures [doc/v].
- Target rule: file name = target id, or an explicit `targets` list. Entries may be an id, a `#tag`, or a regex such as `create:.*`. A long form has `required: false` [doc Shared Concepts].
- `priority` integer, default 0. Higher wins. Use negative for catch-all files [doc].
- Conditions [v ConditionUtils.java]: `polytone_ignore`, `version` (MC range), `require_config` (a pack config id), `require_mods` (string, list, or {mod, version range}). `polytone_condition` wraps these, or holds a boolean script expression, for example a mod-loader check [doc]. Pack overlays use the same field with an `override` flag.
- Pack configs [doc Polytone Configs]: a JSON file in `config_entries` makes a toggle in the resource-pack screen. Keys: `default_value`, `section`, `display_order`, `performance_impact`, `presets`. A pack-wide preset slider sets many toggles at once.
- Colormaps [doc]: a PNG plus two expressions (x_axis, y_axis, each clamped 0..1). Types: triangular, fixed, grid (OptiFine `color.properties` support), expression. Compound colormaps map tint indexes or biomes. Transparent pixel = fall back to the original colour.
- Scripting [doc + v]: own mini language "nexp" (library `hollowpoint:nexp`), compiled at pack load, so typos show as pack errors. Namespaces: g (global), o (object), p (player), c (camera), r (seeded random and noise), v (value). Neighbour queries are capped at 8 blocks per axis. Global expressions run every `update_interval` and store results. A sandbox package restricts what scripts see [v PolyExpType.java].
- Block expression models [doc Models Improvements]: model type `polytone:expression` picks a model per block position. Cases run in order, first true wins, weighted random lists allowed. Also arbitrary-angle cube rotation and `xoffset/yoffset/zoffset` in blockstates.
- Block modifier keys [doc]: sound_type, map_color, colormap, offset_type (none, xz, xyz, custom), particle_emitters, sound_emitters, block_set_type, render_type, can_occlude (experimental), tinted_breaking_particles, disable_particles, voxel_flags.
- Particle modifier keys [doc]: colormap or color or red/green/blue/alpha, size, life, speed. Applies only at particle creation. `filter` narrows break particles by block or item.
- Custom particles [doc]: texture list in `particles/` plus a type file in `polytone/custom_particles`. Types: custom, semi_custom (copy a vanilla particle), gpu. Keys: render_type (opaque, translucent, invisible, additive), initializer and ticker expressions, remove_condition, emitters, limit, exclusion_radius, colormap caching (per position or once at spawn).
- GPU particles [doc Home cheat sheet]: tens of thousands of weather or ambient particles without lag.
- Entity modifiers [doc + v EntityModifier.java]: fields `emitters`, `colored_light`, `targets`. An emitter spawns a particle from a named model bone (dot path), with chance (default 1), count, max_distance (default 32), velocity, colour, size. It can filter by base texture id, EMF variant, ETF variant. Requires MC 1.21.11+.
- Sound events [doc]: `sound_events.csv` registers client-side events; `sounds.json` entries accept `polytone_condition`.
- Animated textures [doc]: `.png.mcmeta` key `mode` = day_time, game_time, weather, screen_time, season, or an expression.
- GUI modifiers [doc]: move slots, shift labels, draw extra sprites and text, with a `condition` expression. Targets: menu_id, menu_class, screen_title, screen_class.
- Addon API [v api/PolytoneRenderApi.java]: registerShaderPatcher, registerShaderInputPatch, registerLevelRenderListener, registerContentManager, registerUniformBlock, registerSampler, registerConfig (bool and float). It is for render addons, not for colours or particles. Also VoxelVolumeApi / VoxelDataProvider.

### Modded content
- Yes. It targets any registry id, tag or regex, so `projectnublar:.*` works with no code from us [doc Shared Concepts; README says it works with any biome, particle, block].
- A file that names another mod's content needs `require_mods` or it logs an error when the mod is absent [doc].
- Block tint: a colormap needs a model tint index. A modded block model with no `tintindex` gets no tint; `-1` in the JSON means all indexes [doc Colormaps]. Nublar blocks are tinted only if their models declare tint indexes [inf].
- Entity emitters need a bone found in a vanilla `Model.root()` ModelPart tree [v EntityParticleEmitter.java: findBonePath on model.root()]. GeckoLib bones are not ModelParts, so bone emitters do not reach GeckoLib entities. Without a bone name, the emitter spawns at the entity position [v lines 109-151]. The "WithoutModel" path suggests a fallback for entities without a ModelPart model [inf].
- Source grep for "geckolib" finds nothing in Polytone [v]. No GeckoLib issue in Polytone tracker [doc search].
- Custom particles are client-only: only the local player sees them [doc]. Nublar server-driven particles still need Nublar's own particle types. A modifier can retune them (size, life, colour) at creation [doc].
- Menu id targeting of modded menus is not confirmed. Class and title targets work for modded screens [doc Gui Modifiers].

### Problems and cost
- Sodium: Polytone mixes into Sodium internals (BlockRenderer, mesh buffer builder) for block offset [v mixins/compat/SodiumBlockRendererMixin.java]. This is version-fragile [inf]. Open issues: certain colormaps wrong with Sodium (#116), crash with separate-transform models on Sodium Fabric (#109), a Sodium crash on 1.21.3 (#138) [doc].
- Iris: Polytone copies vanilla pipelines for its own render types under Iris [v IrisCompat.java]. Leashes were invisible or crashed with Iris/Polytone in 2026-08 (#367, #375, closed) [doc].
- Memory: issue #411 (2026-10-18, closed) measured Polytone at about 39% of all allocation in a heavy modded 26.2 pack. Cause: the older exp4j evaluator allocated per call; colormap expressions ran per vertex on Sodium meshing threads. The author's reply: packs should use the new expression types, which are faster [doc]. The 26.2 branch uses nexp, compiled once [v].
- Colormap bug on 26.2: a custom colormap on one tint index can overwrite `minecraft:grass_color` for others (#417, open, 2026-10-31) [doc].
- Cost controls in design: compile at load; colormap result caching (per pos or at spawn); particle values set only at creation; `performance_impact` flag on pack configs; neighbour-query cap; global expressions on an interval; unused block-variant textures marked deprecated for cost [doc].
- No data found on startup time. Unknown.

### Ideas for Nublar (reuse without the mod)
1. Data-driven modifier files per feature, target by id, tag or regex, with `priority`, so a pack retunes without code. Fits dinosaur particles (dust, footprints), fossil tint, sound pitch.
2. A small condition block (`require_mods`, `require_config`, `polytone_ignore`, MC version) on every optional data file. Nublar can adopt the key names so pack makers see familiar fields.
3. Pack toggles with a `performance_impact` label and a preset slider: a model for BH.1 options (low, mid, high presets).
4. Seeded position random (`r` namespace: same position gives same value). This is exactly what F1.1 needs for per-position tint, opacity and rotation of fossil ore.
5. Expression-driven block model choice per position (first true case, weighted list) as a data pattern for F1.1 variants.
6. Arbitrary-angle cube rotation and blockstate offsets as a way to get random rotation without more models [doc].
7. Compile scripts at load and report errors once, as pack errors. Cap neighbour queries.
8. Colormap with fallback colour, and cached sampling for particles.
9. Animated textures with `mode` (day_time, weather, expression): fits F3.1 fence glow and F1.3-F1.10 machine screens that pulse with power.

### Verdict
(b) Stay compatible, plus (d) learn patterns. Reason: GPL licence and 19M installs mean packs will target `projectnublar:*` with no help. A name and tint-index rule is cheap. (c) is possible later: an optional Nublar entity bone list needs ModelParts, so it does not apply to GeckoLib.

---

## 2. Continuity

### Facts
| Item | Value |
|---|---|
| What | Client-side. Lets OptiFine-format connected textures (CTM), emissive textures (blocks and item models only) and custom block layers work without OptiFine [README v] |
| MC versions | 1.21.6 to 1.21.8, 26.1, 26.1.1, 26.1.2, 26.2, 26.3 [doc] |
| Loaders | Fabric, Forge, NeoForge, Quilt in the listing. The code is a Fabric mod; NeoForge/Forge need Connector and Forgified Fabric API; no native NeoForge version is planned [doc/v README] |
| Dependencies | Fabric API for Fabric; Connector + Forgified Fabric API on NeoForge/Forge [doc] |
| Downloads | 79,025,031 (16,967 followers) [doc] |
| Last update | 2026-09-16 [doc]; 26.2/dev head d15d442 is 2026-09-13 [v] |
| Repo | github.com/PepperCode1/Continuity |
| Licence name | LGPL-3.0-only [doc] |
| Built-in packs | Default Connected Textures (glass, sandstone, bookshelves); Glass Pane Culling Fix [v README] |

### How it works
- It wraps every baked block state model after bake, using a model-loading plugin hook (modifyBlockModelAfterBake, last phase) [v client/resource/ModelWrappingHandler.java]. Two wrappers: CtmBlockStateModel and EmissiveBlockStateModel. Wrapping happens only if the loaded packs contain CTM or emissive data [v: futures wrapCtm / wrapEmissive].
- Quads go through a Fabric Rendering API `QuadEmitter` with a push/pop transform. Each quad passes through a list of "quad processors" [v client/model/CtmBlockStateModel.java].
- Cache: processor lists are cached per BlockState, then per sprite, in a reference hash map with a StampedLock and optimistic reads. Cache clears on reload [v client/model/QuadProcessors.java]. Only processors whose `matchBlocks`/`matchTiles` hit the pair run.
- Per-thread scratch objects (`ModelObjectsContainer`) avoid allocation on meshing threads [v].
- Random method uses a position seed from the block state (`getSeed(pos)`), so the pick is stable per position and does not disturb the shared random [v CtmBlockStateModel.java comments].
- The model asks the world for the block's "appearance" state first (for camo or facade blocks) [v].
- CTM methods are plug-ins: `CtmLoaderRegistry.registerLoader(method, loader)` [v api/client/CtmLoaderRegistry.java]. Built in: ctm, ctm_compact, horizontal, vertical, horizontal+vertical, vertical+horizontal, random, repeat, fixed, top, overlay family [v client/processor/simple, properties].
- Properties keys seen [v BaseCtmProperties.java]: matchBlocks, matchTiles, tiles, faces (TOP, BOTTOM, SIDES, ALL), biomes, heights / minHeight / maxHeight, name (block entity name), prioritize, resourceCondition (pack-presence check).
- Emissive: a `suffix.emissive` in `optifine/emissive.properties` names the emissive twin of each sprite; the loader stitches twin sprites into the atlas, and `EmissiveSpriteApi.getEmissiveSprite(sprite)` returns the twin [v EmissiveSuffixLoader.java, api/client/EmissiveSpriteApi.java]. The emissive transform copies the quad, sets full-bright, turns off diffuse shade and ambient occlusion [v EmissiveBlockStateModel.java].
- Public API [v api/client]: CtmLoaderRegistry, CtmLoader, CtmProperties, QuadProcessor, CachingPredicates, ProcessingDataKey(+Registry), ProcessingDataProvider, ContinuityFeatureStates, EmissiveSpriteApi.
- Config: two booleans, `connected_textures` and `emissive_textures`, in `continuity.json`; Sodium and Mod Menu screens [v ContinuityConfig.java].

### Modded content
- Works on modded blocks with no mod code. A pack names `namespace:id` in `matchBlocks` or the texture in `matchTiles`. The author confirmed this in tracker issue #516 [doc].
- The pack must target the block's sprite or id. Biomes O' Plenty podzol/grass connection has an open bug (#796) [doc].
- A modded mod can add new CTM methods through CtmLoaderRegistry [v]. Needs a compile dependency on the Continuity API [inf].
- Emissive covers block models and item models. Entities are not covered; entity glow comes from ETF [v README].
- GeckoLib: the block-model wrappers only see vanilla-style baked block state models. GeckoLib block entities render by their own renderer, so CTM/emissive does not touch them [inf]. GeckoLib models use their own texture glowing layer [inf; skills docs].
- Machines in Nublar that use normal block models (F1.3-F1.10 casings) can get CTM and emissive from packs [inf].

### Problems and cost
- Sodium: open bugs. ctm_compact culling wrong when Sodium block face culling is on, can crash, a Sodium bug (#826, 2026-10-18, Continuity 3.0.2, Sodium 0.9.2) [doc]. Kelp flicker with the default CTM pack (#810) [doc]. ArrayIndexOutOfBounds in Sodium LevelSlice via an overlay processor, possible negative-Y bit shift (#823) [doc].
- Iris/PBR: odd reflections with connected glass under PBR (#549) [doc].
- NeoForge: Connector route reported broken on some versions (#671, #712 open) [doc]. Treat NeoForge use as unproven for 26.2 [inf].
- Resource-pack format mismatch on 26.3 (#829) [doc].
- Cost controls: model wrapped only when packs need it; per-state and per-sprite processor cache; thread-local scratch objects; `createGeometryKey` returns null for CTM (no mesh sharing), which is a known cost [v comment: "would be nice to implement"].
- No data on memory or startup. Unknown.

### Ideas for Nublar
1. Wrap-after-bake with a transform on the quad emitter, only if data needs it: a pattern for F1.1 fossil ore (per-position tint, opacity, rotation) without extra blockstate files. [inf]
2. Position-seeded random that does not consume the shared RandomSource.
3. Cache per (state, sprite); clear on reload; thread-local scratch for chunk-meshing threads.
4. Emissive-by-suffix: a `_e` twin texture per sprite, found at atlas stitch. A cheap rule for machine lights (F1.3-F1.10) and fence glow (F3.1). Nublar can follow the same name rule so packs are consistent. [inf]
5. Quad copy with full-bright and no shade as the way to do glow on baked models.
6. `resourceCondition` idea: data files that say "only if pack X is present".
7. Appearance state: call the vanilla appearance hook so camouflaged blocks tint right.

### Verdict
(b) Stay compatible. Name block textures so `_e` and CTM rules work, keep block models normal baked models for machine casings, test one fossil block with the Default CTM pack and one emissive machine on Sodium. (d) Learn the pattern for F1.1. (c) Not needed: no Nublar code should call its API. Reason: 79M installs; a modded block works with no Nublar work; the open risks are Sodium bugs, not ours.

---

## 3. Entity Model Features (EMF)

### Facts
| Item | Value |
|---|---|
| What | Client-side. Lets OptiFine Custom Entity Models (.jem/.jpm, animation expressions, random models) work without OptiFine [doc] |
| MC versions | 1.21.8, 1.21.9, 24w46a, 26.1, 26.1.1, 26.1.2, 26.2, 26.3 [doc]; repo mod 3.3.12 [v] |
| Loaders | Fabric, Forge, NeoForge, Quilt [doc] |
| Dependencies | Entity Texture Features required (random model feature, config screen, texture variation). Entity Culling highly recommended [doc]. Repo pins ETF 7.2.2 [v gradle.properties] |
| Incompatible | OptiFine, OptiFabric, dorianpb CEM [v FEATURES.md] |
| Downloads | 104,360,894 (10,332 followers) [doc] |
| Last update | 2026-10-15 [doc]; repo head 2026-11-03 [v] |
| Repo | github.com/Traben-0/Entity_Model_Features |
| Licence name | LGPL-3.0-only [doc] |

### How it works
- It injects into `EntityModelSet.bakeLayer`: when a mod asks for a ModelLayer root, EMF swaps in its own model part tree if a .jem exists [v mixin/mixins/MixinEntityModelLoader.java; EMFManager.injectIntoModelRootGetter].
- File paths: `assets/<ns>/optifine/cem/<name>.jem` or `assets/<ns>/emf/cem/` [v EMFModel_ID.java; EMFConfig.java:320]. Fallback models can be named.
- Jem parts use the game's ModelPart names; EMF has an export tool that writes a ready .jem for any supported model to `emf/export/` [v FEATURES.md].
- Animations: expressions per part variable (tx, ty, tz, rx, ry, rz, sx, sy, sz, visible, visible_boxes), run each render. Entity variables `var.*` and `varb.*` keep state between frames [v emf_animation.txt].
- Variables seen [v emf_animation.txt, FEATURES.md]: limb_swing, limb_speed (OptiFine set), frame_time, frame_counter, plus EMF extras is_climbing, is_blocking, is_crawling, distance (to the player), fluid_depth(_up/_down), move_forward, move_strafing (direction of motion vs facing, -1..1).
- Functions seen: lerp; about 30 easing functions (easeinoutexpo, easeoutbounce, and so on); catmullrom, quadbezier, cubicbezier, hermite; keyframe() and keyframeloop() (a timer through listed key values); wrapdeg/wraprad/degdiff/raddiff for angle wrap [v FEATURES.md].
- Smoothing in packs is done with `var.x = lerp(frame_time*k, var.x, target)` style expressions that carry state per entity [v emf_animation.txt lines ~306-337: counters built from frame_time].
- Cost controls [v]:
  - "ASM maths": expressions compile to JVM bytecode (`asmMaths = true` by default; classes in models/animation/math/asm). Changelog 3.3.12 notes ASM speed fixes; 3.3.10 faster animation loading; 3.3.6 fixed method-too-large limit.
  - Animation LOD (`animationLODDistance = 20`): skip animation frames by distance, scaled by FOV, by low FPS (`retainDetailOnLowFps`), by entity size (`retainDetailOnLargerMobs`). Never for GUI, villagers, horses [v utils/EMFLODHandler.java].
  - Skip animation in the Iris shadow pass (`animationFrameSkipDuringIrisShadowPass = true`), except first-person local player [v].
  - `modelUpdateFrequency` setting [v config].
  - Layer-bake cap: after 64 creations of the same layer outside loading, EMF ignores that layer and logs that models must be baked once and reused [v EMFManager.java:259-265].
- Public API: EMFAnimationApi (api version 11 in changelog) [v]. Registers animation variables and functions (registerSingletonAnimationVariable, registerUniqueAnimationVariableFactory, registerCustomFunctionFromStaticMethod with optional ASM variant, registerAnimationFunction/BiFunction/TriFunction/MultiFunction), hooks (registerAnimationHook), pause and vanilla-model locks (pauseAllCustomAnimationsForEntity, lockEntityToVanillaModel, registerVanillaModelCondition), queries (isModelAnimatedByEMF, getCurrentEMFVariantOfModel), and manual triggers (animateModelForEntity / ForState) [v].

### Modded content
- EMF supports "all modded entities whose model factories appear through the entity model loader" and says mods that use other custom model systems are not likely supported [v FEATURES.md, quoted idea].
- So: a mod whose entity builds a `LayerDefinition`/`ModelPart` tree through the vanilla model set is supported, with an exportable .jem. [v]
- GeckoLib entities use `.geo.json` bones and GeckoLib renderers, not `bakeLayer` roots. EMF source has no "geckolib" string [v grep]. So EMF does not retexture or reanimate Project Nublar dinosaurs. [v + inf]
- EMF issues mention GeckoLib only as a crash or log cause in mixed packs (EMF #451, #236, #159), not as support [doc]. GeckoLib tracker has closed crash reports that list EMF in the mod list (#823, #749) [doc]. No fix claims "EMF supports GeckoLib". [inf]
- Block entities: EMF covers CEM for block entities too (OptiFine list) [v FEATURES.md]. A Nublar block entity rendered with a vanilla BlockEntityRenderer ModelPart (for example a simple machine) would be supported; a GeckoLib one would not [inf].
- Texture variants of any entity (random, emissive) come from ETF, which already works by texture id [prior survey, not re-read].
- Polytone entity emitters can filter by EMF variant [doc], so those two mods link, but only for ModelPart models.

### Problems and cost
- Open: entity vertex buffer grows without bound in raids on 26.3, FPS 60 to 3, then crash; time sits in `EMFCube.compile` -> `BufferBuilder.addVertex` (#597, 2026-10-10) [doc]. Cost is per cube, per frame, so EMF's custom cubes cost more than vanilla baked cubes. [inf]
- Open: mixed with Sodium or Iris, ghost visuals (#549) and invisible or broken models with ImmediatelyFast + Iris (#189) [doc].
- First-person arm flails with Create, Just Enough Guns and custom player animation packs (#360, 2026-11-03) [doc]. Mods that touch the player pose clash with EMF's pose reset. [inf]
- Known workarounds: EBE (enhanced block entities) setting; Physics Mod ragdolls partial support [v FEATURES.md].
- Per-frame cost scales with animation size; the author's LOD and ASM exist for this reason [v].
- No numbers for startup or memory found. Unknown.

### Ideas for Nublar (reuse without the mod)
1. Variables a dinosaur animator wants: `move_forward` and `move_strafing` as a unit-circle measure of motion vs facing, to blend walk and strafe/turn clips. The brief says Nublar uses GeckoLib clips, so these become blend weights for the controller [inf].
2. `distance` to the player and an animation LOD: skip animation work beyond a distance, scale by FOV, FPS and body size (big dinosaurs keep detail longer). Fits BH.1 low/mid hardware. Their defaults: 20 blocks base; exempt GUI [v].
3. `fluid_depth` up/down as a swim and wading variable.
4. Per-entity animation state with first-class frame_time: smoothing by lerp toward a target using frame time, not tick count, so motion looks the same at 30 and 144 FPS [v emf_animation.txt].
5. Easing set (ease in/out, back, elastic) and `keyframe()` timers for procedural tail and head follow-through (F2.3). [inf]
6. Compile expressions once; avoid per-frame parse. (If Nublar ever allows expression files.)
7. Model-bake discipline: bake the layer once and reuse; EMF stops serving models baked more than 64 times. Nublar must never rebuild a ModelPart or LayerDefinition each frame. [v]
8. Skip animation in the Iris shadow pass. For GeckoLib: avoid duplicate animation work for shadow-pass renders. [inf]
9. Export tool: one button writes a ready model file from the in-game model. Nublar could do the same for tweak-friendly debug dumps of bone names. [inf]
10. A small public API with `isAnimatedBy`, pause, and vanilla-lock calls is the right shape for any cross-mod hook.

### Verdict
(a) Ignore EMF as an integration target for dinosaurs, because it cannot reach GeckoLib entities. (b) Stay compatible in one narrow way: do not render Nublar dinosaurs with ModelPart models, and bake any ModelPart model once. (d) Learn animation variables, LOD and smoothing patterns for F2.3 and BH.1. Reason: EMF is a ModelPart system; the lead's animation path is GeckoLib 5.

---

## Nublar fit table

| Finding | Nublar module | Use | In-game effect for Nublar | Effort |
|---|---|---|---|---|
| Polytone targets any id/tag/regex, so `projectnublar:.*` modifiers work with no Nublar code [doc] | F1.1, F2.3, F3.1, R01 | Compat duty | Pack makers can recolour fossil ore, fern tint, fence sparks and tweak Nublar vanilla-type particles | S (keep ids stable, tint indexes declared) |
| Data-driven modifier + `priority` + `require_mods` conditions [doc/v] | F2.3, BH.1 | Adopt pattern | Particle size, life, colour and sound pitch per species in data, tunable without a new jar | M |
| Pack config toggles with `performance_impact` label and preset slider [doc] | BH.1 | Adopt pattern | One "Quality" preset that turns dust trails, footprints, glow on or off for low-end players | M |
| Seeded position random (same pos gives same value) [doc] | F1.1 | Adopt pattern | Each fossil ore block keeps its own tint, opacity and rotation after reload | S |
| Expression-picked block model per position, arbitrary-angle rotation [doc] | F1.1 | Reference | Fossil variants without dozens of blockstate files | M |
| Colormap with fallback + cached sampling for particles [doc] | R01, F2.3 | Reference | Plant tint and dust colour that follow biome without per-tick cost | S |
| Polytone entity emitters use ModelPart bones, not GeckoLib bones [v] | F2.3 | Not applicable | Packs cannot add bone particles to dinosaurs; Nublar must expose its own emitter points (GeckoLib bones/locators) | M (own data) |
| Polytone custom particles are client-only; modifiers act at creation [doc] | F2.3 | Compat duty | Nublar's server-sent particles stay visible to all; packs can retune size/colour | S |
| Polytone GUI modifiers target screens by class/title [doc] | F1.3-F1.10 | Compat duty | Packs can shift slots and add sprites on machine screens; Nublar screens should keep stable titles and slot indexes | S |
| Polytone mixes into Sodium and Iris internals; many open bugs [v/doc] | BH.1 | Reference | Nublar avoids touching Sodium internals; the test list names Sodium/Iris | S |
| Continuity CTM/emissive on modded blocks via `matchBlocks`/`matchTiles` [doc] | F1.1, F1.3-F1.10, F3.1 | Compat duty | Pack makers can add connected casings and glowing machine lights if Nublar uses normal block models and plain texture names | S |
| Emissive twin by suffix, quad copy with full-bright, no shade [v] | F1.3-F1.10, F3.1 | Adopt pattern | Machine lights and live-fence glow stay lit in the dark without lightmap tricks | M |
| Wrap-after-bake + quad transform + per-(state,sprite) cache + thread-local scratch [v] | F1.1 | Adopt pattern | Fossil ore variation costs almost nothing at chunk meshing; nothing leaks per-frame | M |
| Position-seeded random that does not consume shared RandomSource [v] | F1.1 | Adopt pattern | Stable look at same coordinates, no flicker on chunk rebuild | S |
| Continuity: Sodium culling bug with ctm_compact [doc] | F1.1, BH.1 | Compat duty | A test: fossil block + Default CTM pack + Sodium face culling on | S |
| EMF covers only vanilla-ModelPart models; no GeckoLib [v] | F2.2, F2.3 | Not applicable | EMF packs do not change dinosaur shapes or skins; skins stay Nublar's own texture ids | S (document) |
| EMF `animationLODDistance` scaled by FOV, FPS, body size [v] | F2.3, BH.1 | Adopt pattern | Distant dinosaurs animate less often; big ones keep detail; FPS holds on low/mid PCs | M |
| EMF frame_time smoothing + easing + move_forward/move_strafing [v] | F2.3 | Adopt pattern | Smooth turn and strafe blend; no stutter at different FPS | M |
| EMF 64-bake cap: models must be baked once [v] | F4.2, F1.3-F1.10 | Compat duty | Any ModelPart for vehicles or machine renderers baked once at registration; EMF keeps working | S |
| EMF raid bug: custom cubes rebuilt every frame [doc] | BH.1 | Reference | Warns that per-frame mesh building is the cost; GeckoLib caches baked geometry already | S |
| EMF requires ETF; ETF handles skins/emissive by texture id [doc] | F2.2 | Compat duty | Fixed skin texture ids keep ETF variants working (earlier survey) | S |
| EMF ASM-compiled expressions [v] | F2.3 | Not applicable | Nublar has no expression packs; GeckoLib/Molang is the existing path | n/a |
| Continuity wraps models for the block atlas only; no entity use [v] | F2.2 | Not applicable | Dinosaur skin layers/vertex tint untouched by Continuity | n/a |
| Vehicles (F4.2) not covered by any of the three if GeckoLib [inf] | F4.2 | Not applicable | No pack effect; keep ids stable | n/a |

## Open questions

1. Does Polytone's EntityModifier have a path for GeckoLib or non-ModelPart entities (the `WithoutModel` branch spawns at the entity position only)? Needs one in-game test with a GeckoLib entity; not run.
2. Does `menu_id` targeting work for modded menu types in Polytone 26.2? Wiki does not say.
3. Does Continuity's NeoForge route (Connector + Forgified Fabric API) work on 26.2 NeoForge? Open issues (#671, #712) say mixed; not tested. The Modrinth page lists NeoForge builds; native NeoForge is "not planned".
4. Does a GeckoLib 5 block entity (machine, fence post) ever pass through Continuity's quad wrapper? Likely not [inf]; needs a test.
5. Startup time, memory and per-frame numbers for all three mods: no public measurement found, except Polytone #411 (allocation share, pre-nexp).
6. Does any ETF/EMF feature touch GeckoLib texture ids? Earlier ETF survey said texture variants work by id; EMF side not tested.
7. EMF 26.2 download page shows updated 2026-10-15 but repo head and changelog show 3.3.12 on 2026-11-03; which build is current on Modrinth is not confirmed.
8. Continuity version for 26.2: repo head Sep 13; issue #826 cites 3.0.2 on 26.3. Exact Modrinth version number for 26.2 not read.
