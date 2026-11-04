# 01 Shaders: what Recrafted 0.2.1 teaches Nublar

Labels: [v] read in a file (zip-relative path); [doc] web page; [inf] my inference.
Clean room: techniques only. No shader source copied.

## Scope

- 06_builtin_shaders: 38 folders on disk, 1436 files. pack.mcmeta lists 38 shader overlays. [v] pack.mcmeta
- Base layer: assets/recrafted/shaders (147 files, mostly include/*.glsl), assets/minecraft/shaders/core (10 files), assets/sodium (27 files), 48 config_entries, 12 global_expressions, 21 post_shaders, 15 post_targets, 2 viewpoints. [v]
- Read in full or in key parts: all post_effect chains (pass counts), all post_shaders (priority, condition), pack.mcmeta conditions, config_entries, light_bloom, colored_lighting (grid, marker), dynamic_lights, directional_shading, world_normal, terrain/entity/item/block core shaders, fog.glsl override, biome modifiers, viewpoints.
- Skimmed only (file lists, constants): precipitation, rain_puddles, procedural_water/snow/sand/ice, underwater, volumetric_lighting, reflections, far_shadow, lightning, armor_weathering.
- Not found: any web documentation of Polytone. One search returned nothing relevant. Polytone behaviour below is read from the pack's JSON only.
- Folders with polytone_condition "false" (off in the release): procedural_ice, sun_path_rotation, menu_capture, reflections_ultra (the last folder is absent from disk). [v] pack.mcmeta

## Mechanism (question 1)

The pack runs on three vanilla 26.2 systems plus Polytone.

1. Resource-pack overlays. pack.mcmeta lists each folder with a polytone_condition (config('recrafted:x') && ...). Polytone enables or disables the overlay at runtime. [v] pack.mcmeta
2. Vanilla core shader replacement. The pack ships replacement files for core/terrain, block, entity, item, particle, text, text_background, rendertype_leash, sky, stars, position_tex, lightmap, rendertype_clouds, rendertype_lightning, plus include/fog.glsl. [v] assets/minecraft/shaders/core, 06_builtin_shaders/*/assets/minecraft/shaders
3. Vanilla post_effect chains. Each module is a post_effect JSON (targets, passes, inputs, scale) with fragment shaders in recrafted:post/*. Passes read minecraft:main colour and depth (use_depth_buffer), named targets, and a bundled blue_noise texture. [v] 06_builtin_shaders/ambient_occlusion/assets/recrafted/post_effect/gtao.json
4. Polytone extras (the part vanilla lacks):
   - post_shaders/*.json attach a chain to the frame with a priority (1 to 100), an activation_condition expression, and expression_uniforms (uniform blocks computed from game state, for example PolyFogMode, PolyRain). [v] recrafted/polytone/post_shaders/*.json
   - post_targets/*.json declare shared render targets. atlas_h (3104x6144), atlas_x (2306x1024), atlas_r, atlas_s act as shared data textures between modules. [v] atlas_heights/.../atlas_h.json, light_bloom/.../atlas_x.json
   - shader_modifiers/core/*.json add uniforms to a named core shader (PolyHeldLight, PolyLightOff, PolyPlayerLight, PolyRain, PolyFogMode, PolyNoSun, PolyFolPlayer). [v] assets/minecraft/polytone/shader_modifiers/core/*.json
   - viewpoints/*.json render the world a second time from an extra camera into a depth target (ortho, own resolution, terrain layers, entity flags, update interval). Two exist: heightmap and near_shadow; colored_lighting adds colored_light_capture. [v] recrafted/polytone/viewpoints/*.json
   - global_expressions, biome_modifiers, dimension_modifiers, surface_map, biome_id_mappers, config_entries. [v]
5. Sodium. sodium_terrain overlay (needs Sodium) swaps in Sodium's own block_layer_opaque shaders. pack.mcmeta "sodium.ignored_shaders" lists terrain.vsh, terrain.fsh, fog.glsl so Sodium skips the vanilla-format terrain files. [v] pack.mcmeta; 06_builtin_shaders/sodium_terrain

How screen-space effects are obtained in vanilla 26.2 (answer): a pack may ship post_effect JSON, but vanilla runs only fixed chains (transparency, entity outline, and similar). Recrafted overrides minecraft:post_effect/transparency to an empty pass list and runs its own chains through Polytone priorities. [v] assets/minecraft/post_effect/transparency.json. Without Polytone, the chains would not run [inf].

Pass order (priority): data first (weather_field 9, world_normal 10, sky_lut 12, atlas heights 13-14, far_shadow 20, shadow_mask 25), then surface effects (shelter 44, snow 45, sand 46, puddles 47, translucent lighting 48, shade composite 50, colored_lighting 56-58, water 60, GTAO 70, TAA mask 71, water_body 72), then (underwater_surface 75, bloom_mask 77, transparency/reflections 80, fog 82, clouds 83, air_light/underwater 84, volumetric light 85, anti-aliasing 86, lightning 87, precipitation 88, motion blur 92, bloom 93), debug and watermark last (95-100). [v] polytone/post_shaders/*.json

Off-switch trick: the base layer has "_off" chains (shade_off, ground_off, colored_light_translucent_off). They keep the target chain valid when a module is off. [v] recrafted/polytone/post_shaders/*_off.json

Default state: all 47 boolean config entries default true, except ultra_reflections (false). [v] config_entries/*.json. Presets: classic, balanced, complete, custom. Each entry maps to a value per preset. [v] config_entries, lang en_us.json lines 4-12
- Classic ON: directional_shading, ambient_occlusion, anti_aliasing, recrafted_lightmap, biome_atmospheres, lightning. Everything else off.
- Balanced adds: shadows, reflections, colored_lighting, dynamic_lights, light_bloom, animated grass/ferns/leaves, foliage interaction.
- Complete: all on (ultra_reflections stays off).

## Module table

Cost labels are the pack's own performance_impact field. [v] config_entries. Pass counts from post_effect JSON.

| Module | In-game effect | Mechanism | Key files | Default | Cost |
|---|---|---|---|---|---|
| world_normal | Supplies surface normals to other modules | Reads face id from terrain alpha; falls back to depth derivatives | post/world_normal.fsh, world_normal_recon.glsl | on if AO, water or reflections on | 1 pass |
| directional_shading | Sun-aware face shading instead of vanilla fixed per-face values; also on entities and items | Replaces block/entity/item vertex shaders; shade factor from sun direction and normal; GUI keeps vanilla light | core/entity.vsh, item.vsh, include/directional.glsl | on | low |
| ambient_occlusion | Soft contact darkening | GTAO: 3 slices x 8 steps, half-res target, blue noise, composite pass | post/gtao.fsh | on | medium; 2 passes |
| anti_aliasing | Smooth edges | SMAA (3 passes) plus temporal pass and sharpen, mask pass | post/taa.fsh, smaa.fsh | on | medium; 5+1 passes |
| shadow_map | Sun and moon shadows from blocks, mobs, leaves | Near viewpoint: ortho 66 blocks, 1056 px, entities+block entities+particles drawn; mask, temporal filter, composite | viewpoints/near_shadow.json, post/shadow_mask.fsh | on | high |
| far_shadow | Shadows beyond near map | Heightmap-based march, 7 passes | post/far_shadow_*.fsh | on with shadows or VL | part of shadows |
| colored_lighting | Tinted block light | Voxel grid from marker models; 20 passes | see Emissive section | on | high |
| translucent_lighting | Stained glass tints light and shadow | Glass blockstates gain a marker model; 3-pass ray walk | post/tl_walk.fsh | on with CL or shadows | part of CL |
| dynamic_lights | Held light sources light the world | Held-item expression, uniform into core shaders | global_expressions, dynamic_light_core.glsl | on | low |
| light_bloom | Glow around lamps | See Emissive section | post/bloom_*.fsh | on if CL or shadows on | low; 4 passes at 0.4-0.5 scale |
| reflections | Translucent-surface reflections, off-screen | Heightmap-based, 768 px lamps data, 4 passes | post/reflect.fsh | on | high |
| procedural_water | Animated water, wakes, foam, caustics | 13-pass water chain, wake simulation, 322 submerged models | post/water_*.fsh, wake_*.fsh | on | medium |
| underwater | Underwater fog and marine snow | 2+2 passes | post/underwater*.fsh | on with water+fog | n/a |
| atmospheric_fog | Post-process fog by depth, per biome | fog.glsl override hands environment fog to a 5-pass post chain | include/fog.glsl, post/fog | on | medium |
| atmosphere | Custom sky colour model | sky/stars core shaders, sky LUT | core/sky.fsh | on | low |
| biome_atmospheres | Per-biome fog and sky colours | biome_modifiers by tag | polytone/biome_modifiers | on | low |
| volumetric_clouds | 3D clouds with shadows | 5 passes at half res, 4-8 view steps, 2 light steps | post/cloud_march.fsh | on | high |
| volumetric_lighting | Light shafts | 7 passes at half res, 40-80 steps, 160 block reach | include/vl_march.glsl | on | high |
| lightning | Sky flash and cloud light | 3 passes, marker-based | post/lightning_*.fsh | on | low |
| precipitation | 3D rain and snow | 15 passes at 0.07/0.26/0.52 scale, tile lists | post/precip_*.fsh | on | medium |
| rain_puddles | Puddles on ground | 6 passes, wetness simulation | post/puddles.fsh | on | low |
| shelter | Roof detection for rain | 4 passes from heightmap viewpoint | post/rain_shelter_*.fsh | with precipitation or puddles | n/a |
| procedural_snow/sand | Ground cover that builds up | Model markers + 3-pass simulation + ground probe | post/snow_sim.fsh, sand_sim.fsh | on | low |
| atlas_heights | Shared heightmap | Viewpoint from y=320, ortho 256/512/1024 blocks, 4096 px, update interval 200; min pyramid | viewpoints/heightmap.json | on if any dependent module | n/a |
| recrafted_lightmap | New lightmap curve with weather term | Replaces core/lightmap.fsh | lightmap.fsh | on | low |
| motion_blur | Camera/object blur | 5 passes, 1/16 scale tile-max | post/motion_blur*.fsh | off in Balanced | low |
| armor_weathering | Wet/sandy/snowy armour | Replaces entity.fsh and item.fsh; player global expressions | core/entity.fsh | Complete only | low |
| frosted_leaves | Frost on leaves | Blockstate model swaps + Sodium include | blockstates | on | low |
| sodium_terrain | Sodium terrain compat | Sodium block_layer_opaque shaders | 06_builtin_shaders/sodium_terrain | when Sodium loaded | n/a |
| hide_blob_shadow | Removes vanilla blob shadow | Empty shadow.png | textures/misc/shadow.png | with shadows or AO | none |
| menu_capture / sun_path_rotation / procedural_ice | Unreleased or debug | Condition "false" | | off | n/a |

## Modded-content impact (question 2)

GeckoLib entities with vanilla render types
- They use the vanilla `entity` core shader (entityCutoutNoCull, entityTranslucent, eyes). [inf from 26.2 naming: entity.vsh sets EMISSIVE, NO_OVERLAY, PER_FACE_LIGHTING defines]
- The pack replaces entity.vsh. It adds a world normal, a player-light lookup and directional shading. Vertex lighting changes with the sun. Lightmap sampling goes through the pack's lightmap function. [v] 06_builtin_shaders/directional_shading/.../entity.vsh vs assets/minecraft/shaders/core/entity.vsh
- Entity fog: the fog.glsl override moves environment fog to a depth post pass. [v] atmospheric_fog/.../fog.glsl
- Entities get shadows (near map draws entities) and AO from the post passes. [v] near_shadow.json "render_entities": true
- Entities do not bloom, do not write light, and are skipped by dynamic lights. See below.
- Effects with entity.fsh replaced (armor weathering) are gated by pack-specific tint codes. A modded model is not affected unless it uses those codes. [v] reflective_code.glsl: armour tint codes are colour 255,255,~251 or alpha 245-247. [inf] low collision risk but non-zero.
- An alpha channel convention: opaque entities write alpha 255; other codes mark entity classes (alpha 241-248, 251). [v] entity_shadow_code.glsl. Many post passes read this to tell entity from terrain. A mod render type that writes a different alpha (for example translucent 0.5) is treated as non-entity and may get terrain-like treatment [inf].

Mod's own core shader (Nublar incubator_bed)
- It gets none of the pack's per-shader uniforms. Polytone shader_modifiers name specific core shaders. A mod shader is not listed, so PolyHeldLight, PolyFogMode and the like are absent. [v] shader_modifiers file names; [inf] for mod shaders.
- If the mod shader imports minecraft:fog.glsl while atmospheric_fog is on, the override declares uniform block PolyFogMode. The mod shader JSON does not bind it. Expect a shader compile or link failure, or a fog value of zero [inf, untested]. A mod shader that does not import fog.glsl gets no fog treatment and may pop against the post fog [inf].
- It gets no directional shading, no dynamic light, no player light, no colored light, no wet effects. It keeps its own look.
- Post passes still see its pixels. AO, shadows (as receiver), bloom mask, fog and AA treat them by depth and alpha. If it writes alpha 255 it counts as an entity pixel. [inf]
- Nublar fact: incubator_bed in the repo is a #version 120 shader using gl_ModelViewProjectionMatrix and a screen-space cellular noise. It is a 1.20.1 asset and needs a #version 330 rewrite for 26.2 regardless of this pack. [v] common/src/main/resources/assets/projectnublar/shaders/core/incubator_bed.vsh/.fsh/.json

Modded blocks in terrain (chunk meshes)
- They render through the pack's terrain shader (or Sodium's replacement). They get face-id alpha, AO, shadows, directional shading, fog, wet/snow effects where marked. No change needed in the mod. [v] terrain.fsh writes face id alpha 32 + id*8 for non-water, non-tagged pixels.
- If the face normal is not axis-aligned or diagonal, normals fall back to depth derivatives. [v] world_normal_recon.glsl
- Vertex colour and lightmap UV (UV2) are used by the pack, so a mod block must keep standard light coordinates.

Block entities (machines, fence posts)
- Block entity renderers draw outside the chunk mesh. They are NOT in the colored-light capture (render_block_entities false) and are NOT in the heightmap viewpoint. They ARE in the near shadow map (true). [v] viewpoints
- A block-entity-rendered machine therefore casts shadow, but cannot emit colored light or bloom unless its block also has a marker model in the blockstate. [inf]

## Emissive and light colour conventions (questions 3 and 4)

Bloom (light_bloom)
- Bloom is not a per-pixel alpha or texture-suffix test. It is a voxel-driven mask. [v] bloom_src.fsh
- Steps: rebuild world position from depth; look up the colored-light grid cell (96x64x96 cells around the camera) at that position; if the cell holds an emitter code, compare pixel brightness with the brightness the cell should show; blend with a smoothstep (0.35 to 0.75; torches 0.5 to 0.65); weight by emitter strength capped at 0.2. [v] bloom_src.fsh constants
- Entity pixels are masked out first (alpha test with the entity code ranges). [v] bloom_mask.fsh, entity_shadow_code.glsl
- Therefore: dinosaur eyes, entity glow layers and block entity renderers never bloom. Only terrain blocks whose block state has a marker model bloom. Bloom needs colored_lighting or shadows on (activation condition). [v] post_shaders/light_bloom.json
- A pixel in a marked cell blooms only if it is brighter than the cell's expected lit colour. Emissive texels in the block texture therefore matter: bright texels bloom, dark texels do not. [inf]

Colour of a block light (colored_lighting)
- Mechanism: each emitter block's blockstate (381 files) gets an extra multipart entry: a tiny degenerate quad (about 0.016 blocks wide) in a marker model named colored_light/e_<level>_<tone> (or es_ for solid emitters). Example: beacon -> es_15_3. [v] colored_lighting/assets/minecraft/blockstates/beacon.json, recrafted/models/block/colored_light/*.json
- The vertex shader reads the quad's sub-block position as a lattice code (level 1-15 plus tint index 0-15). [v] colored_light_grid.glsl rc_clLatticeCode, rc_clEmitterCode
- A capture viewpoint renders only marker quads (everything else is moved off-screen) from above: ortho 100 blocks, near -36 to far 36, 768x768, terrain only, update every 4 ticks. The vertex shader rewrites each marker into one texel of a voxel atlas; the emitter code goes into the depth value. [v] colored_light_capture.json, colored_light_vertex.glsl
- Palette: 15 fixed tints with a strength each, held in shader constants (white, cyan, red, soul-blue, amber, green, pink, lilac, purple, lime-green, orange, white, cyan, yellow-green, mint). No data file, no texture colour sampling. [v] dynamic_light_palette.glsl
- Other marker kinds: glass (stained glass tint), leaf, plant species, "shape" codes (partial blocks) using the same lattice. [v] colored_light_grid.glsl
- Mod block recipe [inf]: add a multipart model whose element copies a marker quad position and level/tone. It must be in the mod's own namespace and geometry to avoid depending on the pack's model files. No custom RGB: the choice is one of the 15 tints. Without the pack the extra quad is invisible (0.016 blocks, one face) but costs a face.
- Light range and grid: 96x64x96 cells near, plus "far_voxel" columns for distance. [v] colored_light_grid.glsl, far_voxel post chain.

Dynamic lights
- Only the local player's main hand and off hand matter. A nested ternary maps item id suffix (endsWith 'lantern', 'torch', 'glowstone', and 25 others) to a light level 0-15; smoothed per tick (factor 0.7254). [v] dynamic_lights/.../global_expressions/*.json
- No entity light sources, no vehicle headlights, no other players. [v] expression reads p.mainHandItem(), p.offHandItem() only.
- Mod items whose id ends in one of those suffixes (for example "lantern") match by accident. Items with other names give nothing. [v]
- Light position follows the player eye (PolyLightOff = player pos + 0.75 eye height minus camera). Terrain, entities, items, particles, text all get the held-light uniform. [v] shader_modifiers/core/*.json

What a mod should follow
- Glowing entity parts: keep vanilla emissive layers (eyes render type or EMISSIVE). They show at full brightness. They will not bloom under this pack. [inf]
- Machine screens, fence wire: if the glow must bloom or tint, the block needs a marker model. If the glow is a GUI sprite (D305), the pack has no effect.
- Avoid alpha values 241-255 on translucent custom layers when the pixel should not look like an entity [inf, weak].

## Weather and biome (question 5)

- Atmosphere (fog end, fog colour, sky colour, cloud colour, sky light factor) is set by Polytone biome_modifiers that match biome TAGS (for example #minecraft:is_jungle at priority 20). A fallback modifier matches #minecraft:is_overworld at priority -100 ("modded_fallback"). [v] biome_atmospheres/.../jungle.json, modded_fallback.json
- A modded biome inside a vanilla tag (is_jungle) gets jungle values. A modded biome in no matching tag gets the overworld fallback. [v]
- The fog field is built from biome attribute VALUES (fog_end_distance, fog_start_distance, fog_color, sky_color) sampled by a surface map over render distance, so the post fog blends modded biomes by their attributes. [v] recrafted/polytone/surface_map.json
- Biome id mapper: a fixed table of 66 vanilla ids (jungle 24, sparse_jungle 25, bamboo_jungle 26 ...). Modded ids are not in it. Where the table is used is not settled. [v] biome_mapper.json; [open] consumer unknown
- Precipitation type uses the player's temperature value (p.temperature()) and sea level, not biome id. [v] weather_field.json. Modded biomes work. [inf]
- Rain depends on g.rain() and sheltering from a heightmap (shelter module). Modded dimensions: effects gate on g.dimensionType() == overworld; others use dimension_air chains. [v] post_shaders
- Jungle look values (useful for the island) [v] jungle.json: clear fog end = 26% of view distance, rain 18.2%; fog colour clear 0xE0EEEC, rain 0x8D9897, storm 0x252A2A; sky 0xA9BBDE; sky light factor 1.0 -> 0.55 -> 0.3 with rain. Cloud colour white -> 0x94989C -> 0x4C5056.
- Result: dense, short, pale-green-grey jungle haze and dark storms. Dinosaurs beyond about 26% of view distance disappear into haze. Plan sight lines for herds accordingly [inf].
- Volumetric clouds, lightning, clouds: dimension and rain driven, not biome driven. [v]

## Cost and options (question 7)

- The pack's own labels: high = shadows, colored_lighting, reflections, volumetric_lighting, volumetric_clouds, ultra_reflections; medium = ambient_occlusion, anti_aliasing, atmospheric_fog, precipitation, procedural_water, bushy_leaves, particle_physics; low = the rest. [v] config_entries
- Pass counts: colored_lighting 20, precipitation 15, procedural_water 13, far_shadow 7, volumetric_light 7, rain_puddles 6, AA 5+1, fog 5, clouds 5, shadow_mask 5, translucent_shade 5, motion_blur 5+1, GTAO 2. [v] post_effect JSON
- Resolution tricks [v]: GTAO half res; shadow_raw mask half res; bloom 0.4-0.5; clouds and volumetric half res; motion blur tile-max 1/16; precipitation 0.07-0.52; sky and heightmap updated rarely (update_interval 200 for heightmap, 4 for colour grid, 0.6 near shadow).
- Fixed memory: atlas_h 3104x6144, atlas_x 2306x1024, heightmap viewpoint 4096x4096 depth, near shadow 1056x1056, colour capture 768x768. If RGBA8, atlas_h alone is about 76 MB [inf].
- Step counts: GTAO 3x8; cloud view steps 4-8, light steps 2; volumetric 40-80 steps, 160 block max, near reach 32 at 1 step/block. [v]
- Quality control: three presets. Per-module toggles. Modules cascade: many modules depend on shadows/colored lighting for their activation, so disabling shadows also disables bloom unless colored_lighting stays on. [v] polytone_condition expressions
- Conditional work: precipitation, puddles, shelter, weather_field run only while raining or wet. [v] activation_condition
- Low-end story: Classic preset keeps six cheap modules; the rest is off. The heavy chains have no automatic quality scaling or frame-time governor that I found. [v/inf]
- Sodium: supported by an overlay and ignored_shaders. [v] Sodium's own terrain shaders are provided by the pack. The pack declares its Sodium shaders as pack-supplied includes (colored_light_vertex, directional_shading_vertex, procedural_water, and so on). [v] assets/sodium/shaders/include
- Iris: no Iris support found. The conflict-warning list has an IRIS message constant plus Distant Horizons, Voxy, Lumalight, Colorwheel and others. [v] compat_watermark_text.glsl (message ids only; message text is in a texture). The pack renders through Polytone and Sodium; Iris replaces the pipeline. Treat as incompatible [inf].
- OptiFine/CEM: entity animation files under optifine/cem and ETF/EMF are used for textures/models, not for shaders. Out of this slice.

## Nublar fit table

| Finding | Nublar module | Use | In-game effect for Nublar | Effort |
|---|---|---|---|---|
| GeckoLib entities on vanilla `entity` shader get sun-aware face shading, shadows, AO from the pack with no mod change | F2.2, F2.3 | Compat duty (keep vanilla render types) | Dinosaurs sit naturally in lighting under shader packs | S |
| Entity-pixel alpha code (opaque 255; 241-248; 251) is used by bloom, shadow, AA masks | F2.3, BH.1 | Compat duty (do not write odd alpha on opaque layers) | Dino overlay layers not mistaken for terrain | S |
| Mod core shader gets none of the pack's uniforms; fog.glsl import may break | F1.8 incubator, F1.10 | Compat duty (rewrite incubator_bed for 26.2; avoid importing fog.glsl; or use vanilla render type) | Incubator effect works when pack is on | M |
| incubator_bed is a #version 120 file | F1.8 | Reference (rewrite regardless) | Incubator renders on 26.2 | M |
| Bloom works only on terrain blocks with marker models; entities never bloom | F3.1, F1.3-1.8, F2.3 | Reference | Eyes and fence wire will not bloom under this pack; glow must be baked into textures/sprites | S |
| Colored light = marker quad in blockstate; 15 fixed tints | F3.1, F3.2, F4.2, R01 | Adopt pattern (optional compat models) | Fence posts, lamps and machine screens could tint nearby light | M |
| Dynamic light = player hands, item id suffix only | F4.2 | Not applicable (no vehicle support) | Headlights not lit by pack; Nublar needs its own light | L |
| Material identity through sub-block vertex positions in models | R01, F3.1 | Reference | A way to tag blocks without a data file | M |
| Block entity renderers appear in shadow map but not colour capture | F1.8, F1.10 | Reference | Machines cast shadows; do not emit tinted light | S |
| Jungle values: fog end 26% of view distance, pale haze | R01, island look | Reference | Dense jungle mood; dinosaur sightlines short | S |
| Biome modifiers by tag plus fallback #is_overworld | R01, island biomes | Compat duty (put island biomes into #is_jungle or a custom tag with a modifier) | Island biome gets jungle atmosphere | S |
| Config presets classic/balanced/complete with performance_impact labels | BH.1 | Adopt pattern | One low-effects switch plus labelled options | M |
| Off-chain "passthrough" chains keep pipelines valid | BH.1 | Reference | Safe toggles for a future Nublar post effect | S |
| Half-res and update_interval tricks (heightmap 200, colour 4, shadow 0.6) | BH.1 | Reference | Low-end plan for any Nublar VFX | S |
| Heavy modules rely on Polytone, Sodium, no Iris | BH.1 | Compat duty (document: Nublar must work with the pack off or with Iris) | Players without Polytone see vanilla look | S |
| Terrain face-id alpha, hide_blob_shadow | F2.3 | Reference | Blob shadow texture can be hidden; Nublar must not rely on vanilla blob shadow under this pack | S |

## Open questions

1. Where biome_mapper's 66-entry id table is read, and what happens for ids outside it.
2. Does a mod core shader that imports fog.glsl compile with the override? Needs a game test with Polytone and the pack.
3. Whether block entity render types that write alpha 255 are fully treated as entities by shadows/bloom masks (read only entity_shadow_code and bloom_mask).
4. Is there any entity glow path (eyes render type) in the pack? None found in bloom or colored light.
5. The text of the Iris message (stored in a texture, not read).
6. Polytone documentation: none found online, so keys such as priority and activation_condition are read from examples only.
7. Texture format of atlas targets (so memory estimate is a guess).
8. Whether TAA uses camera jitter. None found in the files searched.
9. Detail of precipitation, rain_puddles, lightning, underwater, volumetric_lighting and procedural_water logic: skimmed, not traced.

## Checked by the main session (2026-11-04)

- Bloom mask [v `06_builtin_shaders/light_bloom/assets/recrafted/shaders/post/bloom_mask.fsh`]: the pass writes 1 for entity pixels and 0 for the rest, so bloom leaves entities out.
- Nublar render types [v]: dinosaur layers use vanilla `RenderTypes.entityTranslucent` (`DinoLayer.java:57`), and `IncubatorRenderer.java:66` uses the same. So Nublar entities and machines get the pack's entity shading and shadows with no Nublar change.
- `incubator_bed` [v]: the shader is `#version 120` and no code uses it; `IncubatorScreen.java:25` marks it `TODO(DEAD)`. It is dead code, not a port task.

## Armour weathering, read in full by the main session (2026-11-04)

Files: `06_builtin_shaders/armor_weathering/` (20 files): Polytone global expressions per armour piece, `shaders/core/entity.fsh` and `item.fsh`, and the include `assets/recrafted/shaders/include/armor_weathering.glsl` (406 lines). Default on; the config gives it `performance_impact` low and the "complete" preset only. [v]

**What it does in game.** The local player's armour picks up sand (with a red-sand tint), snow and wetness, piece by piece, and loses it again. There is no dirt or mud coating. [v: `grep -i "dirt\|mud"` finds nothing in the module]

**State.** One value from 0 to 1 per coating and per piece (helmet, chest, legs, boots), updated every tick (`update_interval` 1) from the local player only (`p.*` calls). Other players and mobs get nothing. Two values share one float to save uniforms: sand + 2 × round(snow × 100). [v `armor_sand_*.json`, `armor_weathering.glsl` 4-13, 254-260]

**Build-up and loss** (per tick; time constants are 1/rate) [v; seconds are inf at 20 ticks/s]:

| Coating | Gain | Loss |
| --- | --- | --- |
| Sand, boots | 0.010 × (1 − value) while walking on sand: about 5 s to 63 % | 0.0003 × value when not gaining: about 2.8 min |
| Sand, chest and helmet | Touching sand blocks at body height | Same |
| Red tint | Moves toward 1 on red sand, 0 on sand, rate 0.012 | Holds |
| Snow, boots | 0.012 walking on snow (about 4 s), 0.020 inside powder snow, 0.003 × rain in snowfall | Melts 0.004 in warm places (about 12.5 s), 0.0002 in cold (about 4 min) |
| Wet | Toward 0.40 (boots) or 0.90 (chest) in rain; toward 1 when under water; puddles and melting snow too | Dries 0.001 to 0.003 × value (about 50 s at night, 17 s in full sun). Heat dries fast: on fire 0.25, next to lava, magma or sponge 0.08, lit campfire 0.03, lit furnace 0.015 |

- **Water washes by depth.** A waterline value (0 to 1 up the body, from the fluid blocks at feet, body and head) sets how far each piece is under water. Boots wash between 0 and 0.14, the chest between 0.40 and 0.78. Wash rate 0.06 + 0.10 × speed per tick, so a dip clears in under a second. [v `armor_waterline.json`, `armor_water_surface.json`, `armor_sand_*.json`]
- **Temperature with height.** Snow and rain use the biome temperature minus 0.00125 per block above y 80, so a high mountain in a warm biome still snows. [v]

**Look.** [v `armor_weathering.glsl`]
- The shader marks armour by a sentinel alpha (252 humanoid armour, 251 leggings; 246 and 245 for reflective ones) and picks the piece from the UV region of the vanilla armour texture layout.
- Coverage is a threshold on noise (value-noise fbm plus Worley cells), snapped to whole texture pixels (`PIXELATE` 1), so the coating stays pixel-art. Base coverage 0.30, scale 7 texels, with an edge term that fills dark (low-luminance) pixels first, so crevices fill before flat faces.
- Colour comes from fixed 6-step palettes (sand, red sand, snow), desaturated 0.38 toward the base, with a small hue shift. A fake height bump (relief 0.30) shades the deposit edge.
- Wet darkens the albedo and adds a wet film with rain beads and a sky-reflection finish.

**Nublar fit.** No code to take: it is a core shader on armour, for the local player only, and OC6 forbids a core shader. The ideas fit F2.3's snow cover overlay (Beta), the mud overlay (later) and the timed clearing of blood overlays:
- A coverage value per animal and body zone that rises while the trigger holds (gain × (1 − value)), fades slowly after, melts faster in warm places and washes off in water by depth.
- Overlay masks that fill crevices and the lower body first, in whole texture pixels, from a small fixed palette. Nublar can draw them as a few stepped mask textures per species (D330 agent work) instead of a shader.
- A wet look (darker skin after a swim or rain, drying in under a minute) is not in the plan.
