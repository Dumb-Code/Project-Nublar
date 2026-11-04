# Recrafted v0.2.1: textures, GUI, HUD, font, menu, CTM, colormaps, entity models

Labels: [v] read in a file (path relative to the zip root); [doc] web page; [inf] my inference.
Pack paths are relative to the zip root, e.g. 01_textures/recrafted_gui/assets/...

## Scope
- pack.mcmeta [v]: overlays gated by Polytone `polytone_condition`. Each 01_textures folder is one overlay, switched by `config('recrafted:<name>')`: blocks, colormaps, bushy_leaves, overlay_textures, items, entity, particle_textures, recrafted_font, connected_glass, recrafted_gui (also gates recrafted_menu), immersive_hud. immersive_hud_gui needs immersive_hud AND recrafted_gui.
- Each toggle has a config entry in assets/recrafted/polytone/config_entries/*.json (48 files). Keys: default_value, display_order, section, performance_impact (low/medium), tooltip_images (on/off preview, 164x84). [v]
- File counts [v]: block_textures 705 (357 block PNG, 230 block models, about 50 blockstates, torch/lantern item defs), bushy_leaves 84, colormaps 108, connected_glass 124, entity_textures 42, immersive_hud 12, immersive_hud_gui 1, item_textures 274, overlay_textures 434 (408 PNG + 26 .properties), recrafted_font 9, recrafted_gui 202, recrafted_menu 9.
- Read: GUI sprites + mcmeta, HUD shaders and expressions, font sheets, menu shader includes, CTM properties, colormap JSONs, about 16 block textures (viewed), sheep/cow/pig/chicken .jem (expression usage scanned by script, not reproduced).
- Not read: bushy_leaves, item_textures, shaders outside my slice.
- Web: the Polytone wiki URL returned 404, so Polytone key meanings are [inf] from the JSON files. One EMF search gave the [doc] below.

## GUI and HUD

### Replaced vanilla sprites (01_textures/recrafted_gui/assets/minecraft/textures/gui) [v]
Only 154 sprite PNGs, by folder: container/creative_inventory 30 (tabs, scroller), container/slot 5 (armour and shield ghost icons only), friends 1, hud 22, hud/heart 47, icon 6, pause_menu 3, recipe_book 29, tooltip 2, transferable_list 8, widget 7, world_list 4. Plus 3 creative-tab sheets, recipe_book.png (256x256), title logo, Mod Menu sprites.
NOT replaced: slot background, container backgrounds (inventory, furnace, generic_54), edit box (`widget/text_field`), scroll bar, checkbox, tab buttons, separators, list backgrounds. Most container screens stay vanilla.

### Widgets
- button, button_highlighted, button_disabled, slider, slider_highlighted: 200x20 PNG. mcmeta `gui.scaling`: nine_slice, width 200, height 20, border 2. [v] .../sprites/widget/*.png.mcmeta
- slider_handle, slider_handle_highlighted: 8x20, nine_slice border 1. [v]
- Look [v, pixels read]: fill black at alpha 199 (about 78%). 2 px border: outer ring grey 43/255, inner ring black. Highlighted: outer ring pure white. Disabled: fill alpha 166, outer ring grey 26. Slider reuses the button art. Handle is flat grey, white when highlighted.
- No separate focus sprite exists; focus uses the highlighted sprite [inf, vanilla model].
- Contrast: white text on near-black is very high contrast. The hover cue is a 1 px white ring, so it does not depend on colour.

### Tooltip [v]
background.png 100x100, nine_slice border 9. frame.png 100x100, nine_slice border 10, `stretch_inner: true`. Background is solid black with transparent corners (rounded look). Frame is a white 1 px line.

### HUD [v]
- hotbar.png 182x22: grey bevel frame, dark cells. hotbar_selection 24x24: gold square. hotbar_offhand_* 29x24.
- hearts, food, armour, air: 9x9 with black outline. Heart set has 47 files (full, half, blinking, hardcore, poisoned, withered, frozen, absorbing, vehicle).
- exp bar and jump bar 182x5: dark base, green progress with segment ticks. crosshair 15x15 white chevron.
- Polytone `overlay_modifiers` (hotbar.json, hotbar_selection.json): per-texture draw tweaks. Keys: texture (regex on sprite id), x_inc, y_inc, width_inc, height_inc, color. Example: selection frame drawn 1 px taller.
- `gui_modifiers` (assets/recrafted/polytone/gui_modifiers/*.json) re-lay out vanilla screens by class and widget message key. Keys: target_type=screen_class, target, widget_modifiers[{target_message_key or target_class_name, x_from_center, y_offset, width_increment, visible}], polytone_condition or require_mods. Used on TitleScreen (hides the Realms button, moves 3 buttons into a row 24 px up) and PauseScreen (two variants: Mod Menu present or absent).
- Creative tab icons: creative_tab_modifiers/*.json, key `icon` (an item id). splashes.txt is replaced.

### Immersive HUD (hides the HUD when idle) [v]
Mechanism, no Java:
1. A Polytone global expression (assets/recrafted/polytone/global_expressions/immersive_hud.json) runs every tick (`update_interval: 1`). It reads player state through `p.*` calls: held items, armour, health, hurt time, item use ticks, eye in water. It builds an item+health signature. If the signature changes, or the player is hurt or uses a non-tool item, an idle counter resets. Health at or below 30 percent, or head under water, pins the HUD visible.
2. After 30 idle ticks a dim value rises 26 per tick (max 255). It falls 64 per tick when activity returns. The whole state (signature, idle counter, current and previous dim) is packed into ONE float with base-256 and 65536 digits and read back next tick. [inf: a small state machine, because expressions only have a global float store.]
3. shader_modifiers (core/gui.json, position_tex_color.json) push that value into uniforms (`ImmersiveHud`, `ImmersiveCrosshair`) with a tick beat counter. The shader lerps between old and new dim by partial tick.
4. Shaders gui.vsh and position_tex_color.vsh (assets/minecraft/shaders/core) include immersive_hud.glsl.
   - Overlay modifiers tag HUD sprite draws with a marker vertex colour: RGB (255,254,255) for HUD sprites, (255,253,255) for the crosshair. The vertex shader sees the tag and multiplies alpha by visibility. The regex skips crosshair, effect_background, and the hotbar and selection sprites themselves.
   - Parts with no sprite tag (item durability bars, cooldown overlays, item count text, XP number) are found by GEOMETRY. The shader derives GUI size from the projection matrix and tests whether a vertex sits at the expected hotbar slot positions and colours.
   - The crosshair keeps minimum opacity 0.25 and brightens while aiming (bow, crossbow, trident, thrown items).
Effect on a mod [inf from shader code]: only sprites under `minecraft:hud/` are tagged, so Nublar HUD sprites in the Nublar namespace do not fade. The geometry tests act on any draw call in the hotbar strip: white or grey-63 text from 8.5 px above the hotbar row down, and white vertex-colour quads at item-corner positions. A Nublar element drawn over the hotbar could fade with it. The risk is small.

### Menu module (recrafted_menu) [v]
- Replaces the 6 panorama faces (panorama_0..5.png, each 2048x2048) and the title logo (minecraft.png 2048x512, edition.png 1024x128).
- The faces are NOT a normal panorama. They hold a baked scene as data pages: layer 0 colour 3840x2160, layer 1 depth 1920x1080, plus a "far sheet" (title_atlas.glsl: RC_PAGE 2048, RC_STAGE 3840x2160, camera tangents, depth scale 4096). The modified panorama.fsh reads pages from the cubemap, rebuilds the picture with depth, and draws live rain (title_rain.glsl, three particle shells, 180 s loop with 3 s cross-fade).
- No background-blur change found in my slice [v: grep for "blur" hit only lang files]. Title and pause screen button layouts are set by gui_modifiers (above).

## Impact on mod screens
Nublar facts [v, graphify + one grep]: nublar-gui has its own EditBox, AbstractSliderButton, NGLSlider, FilteredSelectionWidget and BorderedButton. nublar-gui/.../gui/widget/AbstractSliderButton.java lines 27-30 build the slider, highlighted, handle and handle-highlighted sprite ids with `Identifier.withDefaultNamespace(...)`. So Nublar sliders use the vanilla ids and Recrafted restyles them.

| Surface | Changed by Recrafted? | Effect inside a Nublar screen |
|---|---|---|
| Vanilla `widget/button*`, `widget/slider*` | Yes (nine-slice, black fill, white hover ring) | Nublar controls using these ids turn near-black with a grey ring. A dark "app" look fits. A bright or warm textured panel would clash. |
| `tooltip/background`, `tooltip/frame` | Yes | All tooltips change. Fine. |
| Slot background, container frame, inventory art | No | Nublar slots and panels keep their look. |
| EditBox | Nublar draws its own copy | Not affected, unless it uses a vanilla sprite id [inf]. |
| Font | Yes if recrafted_font is on | Text widths change. See Font. |
| Text colour | Yes: the text vertex shader desaturates GUI colours | Nublar ARGB accents look duller. |
| Hover/focus | Highlighted button sprite only | Narration unaffected. |

Advice [inf]:
1. Give every Nublar widget its own sprite ids (`projectnublar:widget/...`) with its own .mcmeta nine_slice. Recrafted cannot touch them. Cost S per widget family.
2. Use vanilla ids only where "match the player's pack" is the goal.
3. Never hardcode widths. Use `font.width(...)`, wrap, and clamp. Test with a 25 percent wider font.
4. Keep text on dark fills. The text shader lowers highlights above lightness 0.65 [v text_desaturate.glsl: RC_TEXT_KNEE 0.65, RC_TEXT_PULL 0.31, RC_TEXT_CHROMA 0.7], so pastel accents dim.

## Font
- No font definition JSON is replaced. The pack replaces BITMAP SHEETS only: textures/font/ascii.png (128x128, 8x8 cells), accented.png (176x900), nonlatin_european.png (144x536), asciillager.png (128x40), ascii_sga.png (128x128), plus unifont.zip and unifont_jp.zip (Unifont 17.0.01 hex files, 7.7 MB and 0.7 MB). [v] 01_textures/recrafted_font
- So `minecraft:default` keeps its provider list and only the pixels change. Vanilla measures advance from the rightmost opaque pixel, so wider drawing widens text automatically. [inf]
- Measured from ascii.png alpha [v, script scripts/fontw.py]: glyph rows 0-6/7, ascent unchanged, so line height stays 9. Letters are wide: A, H, M, N, U, V, W, X, Y are 8 px; B-F, J, L, O-R about 7; digits 6; i, l, I, t are 4. Edges are antialiased (8 pixels at alpha 100, 646 at 157, 2871 opaque).
- Width impact [v measured; vanilla values from my memory of the vanilla advance table, so approximate]: "Fossil Processor" 100 vs about 87; "Incubator" 61 vs 50; "Start sequencing" 105 vs 86; "Tyrannosaurus Rex" 124 vs 100. About 15 to 25 percent wider.
- Text shader changes (assets/minecraft/shaders/core/text.vsh, recrafted_font/.../text.fsh) [v]: GUI text colour goes through an OKLab-style desaturation. The shadow is detected when max RGB is 63/255 or less and drawn from a lightened, quartered colour.
- Result for Nublar: labels, buttons, tooltips, tab titles can be about 25 percent wider than vanilla. The 320 px narrow-window rule must be tested with a wide font. Use `font.width`, `font.split`, ellipsis, and tooltips for overflow.
- compat_font.png (128x128) belongs to a watermark post effect, not the GUI. [v]

## Connected and overlay textures

### Connected glass (01_textures/connected_glass) [v]
- 34 .properties, 85 PNG (17 sets x 5 tiles): glass plus 16 stained colours. Each set has glass.properties and glass_pane.properties.
- Keys used: `method=ctm_compact`, `matchTiles=<sprite>`, `matchBlocks=<block>`, `tiles=0-4`, `connect=block`. Panes add `faces=sides`. Tiles are 16x16.
- ctm_compact [inf, OptiFine format]: 5 tiles make all 47 neighbour states by quarter-tile composition.
- Pane models replaced: 4 templates (post, side, noside, and alt variants) using `#edge` and `#pane` textures. The alt models mirror UVs so art stays continuous on opposite sides.
- Config entry performance_impact: low.

### Overlay textures (01_textures/overlay_textures) [v]
- 26 .properties, 408 PNG = 24 sets x 17 tiles (`tiles=0-16`, the overlay layout).
- Sets: grass_block, podzol, dirt, coarse_dirt, gravel, clay, sand (blocks and tiles), red_sand (tiles), 16 concrete powders (288 files).
- All use `method=overlay`. Keys: `matchBlocks` (about 150 stone-like and log-like blocks: stones, ores, deepslate, nether blocks, logs, terracotta, mushroom and coral blocks), `connectBlocks` (the neighbour that bleeds), `faces=top` (grass and podzol), `tintIndex=0` with `tintBlock=grass_block` so grass edges take grass colour, and `matchTiles` for polished sandstone tiles.
- Effect: a soft sand, dirt, gravel or grass fringe drawn on the stone block next to it. Ores are in matchBlocks, so they get the fringe.
- Not used in this pack [v, scan of method= values]: random, repeat, horizontal, vertical, overlay_ctm, and the keys heights and biomes.
- Fit for Nublar [inf]:
  - Fossil in stone: the `random` method picks one of N tiles per position. It gives variety, not a continuous tint, opacity or rotation. The F1.1 plan (per-position tint strength, opacity, rotation) needs code, not CTM.
  - Mirror and rotation variants work in vanilla blockstates. Recrafted's stone.json lists two models in one variant (normal and `_mirrored`), which vanilla picks per position by seed. [v] block_textures/assets/minecraft/blockstates/stone.json. Nublar can ship 4 rotation or mirror variants per fossil block with no code. Cosmetic randomness stays out of block states in the sense of properties: this is a model variant, not a state property.
  - Incubator glass: ctm_compact needs `matchBlocks=projectnublar:<glass block>` plus 5 tiles. It works only with Continuity or OptiFine. Vanilla players see the base texture. Effort M.
  - Keep Nublar block ids stable. Do not reuse vanilla sprite names, so other packs' `matchTiles` rules do not catch Nublar blocks.
  - A pack overlay set bleeds onto Nublar blocks only if the pack lists them in matchBlocks. Recrafted does not.

## Colormaps (01_textures/colormaps) [v]
- Files: 7 leaf block modifiers (acacia, birch, dark_oak, jungle, mangrove, oak, spruce), kelp, vine, tall_seagrass; leaf item defs; 12 particle modifiers (rain, splash, bubbles, drips, fishing) that use the water colormap; flower models split into tinted and untinted PNGs (peony, lilac, rose bush, oxeye daisy; `semi_tinted_cross` model); dimension_modifiers/overworld.json sets `minecraft:visual/water_fog_color` to colormap `recrafted:water`.
- Colormap definitions (assets/recrafted/polytone/colormaps/*.json + same-name PNG): keys `x_axis` and `y_axis` (TEMPERATURE, DOWNFALL, or "0"), `triangular` (true = vanilla triangular lookup). PNG sizes: 256x256 (acacia, dark_oak, jungle, mangrove, oak) or 1x1 (birch, spruce, aquatic_foliage: one fixed colour).
- Block modifier JSON: `targets` (block ids) and `colormap` = a colormap id, or an object `{default: "foliage_color", biomes: {<biome id>: <colormap id>}}`. Oak and vine list about 65 biome entries, all pointing to the same colormap, so per-biome variation exists in the schema but is not used. `biome_id_mappers/biome_mapper.json` maps biome ids to integers.
- Colour choice: biome temperature and downfall (vanilla triangle lookup). No height or custom data. [v]
- Does a Polytone colormap change what a mod reads? [inf, not tested]
  - Polytone registers block colour providers for the target blocks, so `BlockColors.getColor(state, level, pos, tintIndex)` returns the Polytone colour for those leaves, vine and kelp. It does NOT change MapColor.
  - Grass is not in the colormap list, so grass tint stays vanilla. The grass_block_top texture is grey-white, to be tinted.
  - A dust trail that averages the baked sprite changes when a pack replaces the texture. Here stone, dirt, gravel and sand textures are all recoloured.
  - Recommended read for Nublar: take the `BlockColors` tint when `tintindex >= 0`, multiply by the sprite average, fall back to MapColor. This follows whichever pack the player runs.

## Block and item texture style [v, viewed at 8x]
- Resolution: 305 of 378 PNGs are 16x16. 44 are 64x64 (entity). The rest are strips (16x32, 16x64, 16x272 animated) and 32/48/96 entity sheets. No high-res block textures.
- Viewed: stone (horizontal strata, 3-4 grey tones), dirt (warm brown, grey pebbles), grass top (grey-white, to be tinted), grass side (dark soil band; tint overlay is separate), gravel (grey-brown speckle), oak log (vertical bark, 3-4 browns), log top (rings), oak leaves (high-contrast grey-white cluster for tint), glass (blue-grey, diagonal highlight, light border), sandstone (cream bands), cobblestone (rounded stones, soft edges), ice (pale blue, plain), bricks, obsidian (near-black, purple veins), terracotta, wool.
- Style: pixel art with dither, lower contrast and more muted than vanilla. Light from upper left (highlight top-left, shadow bottom-right) [inf]. Palette about 3 to 5 tones per material.
- No ore, water or lava textures are in the pack. Water and lava are shader-driven (procedural_water). Ores only receive overlay fringes.
- Models and states [v]: 230 block models (fences in 7 parts, pressure plates, flowers in 1-4 variants, torches with `_e` emissive twin textures). Emissive twins use the `_e` suffix (copper_torch_e, lantern_e, crying_obsidian_e).
- Modded blocks next to them [inf]: Nublar textures in vanilla style read a little more contrasty than Recrafted but not out of place. Rules a mod can follow: 16x; 3-5 tones per material; contrast on edges, not noise; no pure black outlines; `_e` emissive twins; a `tintindex` for any colourable part; mirrored model variants for large-area blocks.

## Entity models and animation (CEM)
Files [v]: 01_textures/entity_textures/assets/minecraft/optifine/cem/*.jem: chicken (+cold, warm), cow (+cold, warm), pig (+cold, warm), sheep, sheep_wool, sheep_wool_undercoat, squid, glow_squid. That is 14 models. `optifine/random/entity/sheep/sheep.properties` picks a second sheep texture by a name regex. The sheep file carries a credit submodel naming its Fresh Animations author.

### Structure (sheep) [v]
`models[]` hold `animations[]` blocks. Each block maps "part.channel" to an expression. Channels: tx ty tz (translate), rx ry rz (rotate), sx sy sz (scale), plus `visible`, `child_scale`, `hy`. Rig: body, head, head2 (a second head root for look-at), left and right ear, eyes with a sub-chain (eye white, pupil, pupil in/out/up/down scalers, eyelid and blink plate), 4 legs. Cow adds udder, tail, two horn sets (adult and baby, toggled by `visible`), back plants. Chicken adds wings and a second blink set. About 90-120 expression entries per animal in 6-7 blocks.

### Variables used [v, script scan]
EMF variables: limb_swing, limb_speed, age, frame_time, frame_counter, head_yaw, head_pitch, hurt_time, is_hurt, is_child, is_on_ground, is_in_water, is_riding, is_ridden, id (entity id), pos_x/y/z, player_pos_x/y/z, rot_y, time, rule_index. Functions: sin, cos, asin, clamp, between, if, min, max, abs, random, torad. Local `var.*` values: 13 to 27 per animal. No `varb`.

### Procedural ideas seen (names only)
1. Walk and run as sine on `limb_swing` scaled by `limb_speed`, with separate walk and run blend weights.
2. Smooth blend: `var.run` and `var.eatA` use `frame_counter` and `frame_time` to move toward a target over real time (frame-rate independent).
3. Head look: `head_yaw` and `head_pitch` clamped, driving head2.
4. Grazing: head pitch plus a timer drives a head-down eating motion.
5. Ear twitch: each ear on an `age` sine, different phase per ear (`var.Lear`, `var.Rear`).
6. Tail sway on sines (cow tail rx, ry).
7. Breathing: `age` sine on body (`var.b`).
8. Swim: separate sine set when `is_in_water`.
9. Blink: eyelid `sy` scale toggled by a time gate. Eyelids are separate cubes scaled, not texture swaps.
10. Look-at: pupils translate toward `player_pos` relative to `pos` and head yaw, so eyes follow the player. A five-bone pupil chain reshapes the pupil when it looks in, out, up or down.
11. Per-animal variety: `id` and `random` set a different phase or idle choice (`var.r`).
12. Child: `child_scale` and `is_child` change head size and gait.
13. Hurt: `is_hurt` and `hurt_time` drive a flinch.
14. Rider: `is_riding` and `is_ridden` change pig and chicken pose.

### EMF and GeckoLib
- EMF swaps in .jem models for vanilla ModelPart entity models. Modded entity support is beta. Files go under `optifine/cem/modded/<namespace>/<name>.jem`. Models whose part names differ from OptiFine's are "very unlikely to animate". [doc] https://modrinth.com/mod/entity-model-features/version/0.2.11 (changelog, seen as a search result, not opened).
- GeckoLib entities render their own baked geo model, not a ModelPart tree. I found no source saying EMF animates GeckoLib models, and I judge it does not. [inf]
- What carries over to Nublar's GeckoLib procedural layers [inf]:
  - Per-entity phase from the entity id (idle variety, breathing phase): Adopt, S.
  - Frame-time smoothing for blends (exponential smoothing with partial tick): Adopt, S.
  - Eye look-at with pupil bones and separate eyelid scale for blink: only if Nublar models have eye bones. Check the models and clips first. M.
  - Breathing as a small sine on chest scale or rotation: Adopt, S.
  - Tail and ear secondary motion: keep the plan's damped spring. Fresh Animations uses plain sines, which are simpler and not spring-correct.

## Nublar fit table

| Finding | Nublar module | Use | In-game effect for Nublar | Effort |
|---|---|---|---|---|
| Recrafted restyles vanilla `widget/slider*`, `button*`, `tooltip/*`; Nublar's AbstractSliderButton uses vanilla ids | F1.3-F1.8 screens | Compat duty | Sliders and tooltips turn black/grey for Recrafted players | S |
| Own sprite ids with own nine_slice mcmeta for all widgets | F1.3-F1.8 | Adopt pattern | Same look in every pack | S-M |
| Recrafted font is about 15-25 percent wider, same line height | F1.3-F1.8, 320 px rule | Compat duty | Labels may overflow; measure with `font.width`, wrap, ellipsis | S |
| Text shader desaturates GUI colours | Glow and accent colours (D305, D313) | Compat duty | Accent text looks duller; do not carry meaning by hue alone | S |
| Immersive HUD fades `minecraft:hud/` sprites and hotbar-strip text and quads | Any Nublar HUD near the hotbar | Compat duty | Avoid white text or quads in the hotbar strip | S |
| Config UI with per-feature performance_impact and on/off preview images | Options screen, BH.1 | Reference | Players see cost and preview before toggling | S |
| Overlay method (neighbour bleeds onto stone, tint from a block) | F1.1 fossils, F4.1 digsites | Reference | Soft digsite-dirt edge on stone; needs Continuity | M |
| Mirrored and rotated blockstate model variants | F1.1 | Adopt pattern | Varied fossil blocks without new art or code | S |
| ctm_compact glass (5 tiles) | F1.8 incubator glass, F3.1 | Adopt pattern (optional) | Connected glass for incubator walls | M |
| Colormaps change BlockColors for leaves, vine, kelp; MapColor unchanged | F2.3 dust trail | Adopt pattern: BlockColors tint x sprite average | Dust matches the player's pack | S |
| Menu panorama: baked scene + depth in cubemap faces, live rain shader | none | Not applicable | -- | -- |
| Procedural CEM ideas: id phase, frame-time smoothing, breathing, blink, look-at | F2.3 | Adopt pattern | More alive dinosaurs | S-M |
| EMF likely does not animate GeckoLib entities | F2.2, F2.3 | Not applicable | Nublar animation is not affected by EMF packs | -- |

## Open questions
1. Does Polytone's colour hook change `BlockColors.getColor` for modded callers? Does it reach modded blocks a pack lists? (Wiki 404; not tested in game.)
2. Exact vanilla advances in 26.2: my width comparison uses remembered values. Measure against the real vanilla ascii.png.
3. Do EMF or ETF touch GeckoLib entities (for example texture swaps through `optifine/random/entity` paths)? Not settled.
4. How the `p.*` expression calls behave in multiplayer. Not read.
5. Whether planned Nublar HUD elements sit in the hotbar strip.
6. Whether Nublar screen text colours are already low-chroma, to judge the desaturation impact.

## Checked by the main session (2026-11-04)

- Font widths [v]: measured with the vanilla bitmap rule (rightmost opaque column + 1, plus 1 px spacing, space 4) on the vanilla 26.2 `ascii.png` from the Loom client jar and on the Recrafted sheet. "Fossil Processor" 87 to 100 px (+15 %), "Incubator" 50 to 61 (+22 %), "Start sequencing" 86 to 105 (+22 %), "Tyrannosaurus Rex" 100 to 124 (+24 %), "Egg Printer" 58 to 73 (+26 %), "Velociraptor" 63 to 79 (+25 %). The note's remembered vanilla values were right.
- Defaults [v `assets/recrafted/polytone/config_entries/*.json`]: 47 of 48 options have `default_value: true`; only `ultra_reflections` is off. So the font, GUI, HUD and every shader module are on for a new player.
- Nublar slider [v]: `nublar-gui/.../widget/AbstractSliderButton.java:27-30` uses the vanilla `widget/slider*` ids. The plan (backlog F1.10, D289/D310) already moves Nublar widgets to their own nine-sliced sprites; until that art exists, Recrafted players see its black slider on Nublar panels.
