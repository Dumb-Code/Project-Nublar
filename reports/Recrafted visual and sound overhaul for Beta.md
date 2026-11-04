# Recrafted visual and sound overhaul for Beta: what a 26.2 overhaul pack teaches Nublar

Date: 2026-11-04. Status: research report for the project lead. It changes no code, no asset and no plan module. The changes below are proposals.

Recrafted Early Access 0.2.1 (MR_CHOO) is a paid resource pack for Minecraft 26.2 (pack format 88, the same version as the mod). It has 4,448 files in 48 options, and 47 of them are on by default. It changes block, item and GUI textures, the font, the HUD, particles, sounds and nearly every world shader. It needs Polytone. It also uses Sodium, Continuity, Entity Model Features (EMF) and Entity Texture Features (ETF). This research read the whole pack and the source of Polytone, Continuity and EMF at their 26.2 branches.

The main results:

- **Nublar must carry its own look.** Under this pack, entities and block-entity renderers never bloom, get no coloured light and push no plants. No pack tool reaches a GeckoLib bone: EMF changes only vanilla `ModelPart` models, and Polytone entity emitters walk `ModelPart` trees. So every dinosaur effect stays Nublar's own work, as the plan says. The rule "no core shader" (OC6) holds: dinosaurs and machines use vanilla render types, so they already get the pack's shading and shadows.
- **One defect that has nothing to do with the pack.** All 37 Nublar sounds are streamed. Vanilla 26.2 plays at most 8 streamed sounds at once, music included. With several dinosaurs calling, sounds drop out.
- **Two problems for players of this pack.** The pack makes text 15 to 26 % wider, and it restyles the vanilla slider, button and tooltip sprites that Nublar screens still use.
- **Patterns worth copying:** options that show their cost, with presets; grey particle sprites tinted when they spawn; dust spawned by a chance that follows speed; slow updates for particles that rest; a loudness target for sounds.

**Labels.** `[v]` means checked in this research: in a pack file, in Nublar's files, in mod source at a named commit, in vanilla 26.2 bytecode (javap on the Loom-mapped jar), or measured with a script. `[doc]` means a web page, README or changelog. `[inf]` means inference. Nobody ran the pack or the mods in game.

**Clean room.** The notes describe techniques, keys and values. They copy no shader code, expression or asset from the pack.

| Note | Area |
| --- | --- |
| [01_shaders.md](../research_notes/Recrafted%20visual%20and%20sound%20overhaul/01_shaders.md) | 36 shader modules: bloom, coloured light, dynamic lights, shadows, weather, fog, cost |
| [02_vfx_particles.md](../research_notes/Recrafted%20visual%20and%20sound%20overhaul/02_vfx_particles.md) | Foliage motion and push, particles, particle physics, player combat clips |
| [03_sounds.md](../research_notes/Recrafted%20visual%20and%20sound%20overhaul/03_sounds.md) | Ambience, block sound types, variation, file facts, Nublar sound measurements |
| [04_textures_gui_entities.md](../research_notes/Recrafted%20visual%20and%20sound%20overhaul/04_textures_gui_entities.md) | GUI, HUD, font, connected and overlay textures, colormaps, entity models |
| [05_polytone_continuity_emf.md](../research_notes/Recrafted%20visual%20and%20sound%20overhaul/05_polytone_continuity_emf.md) | The three mods: facts, mechanisms, modded content, cost, verdicts |

## Twelve changes, ranked

Each row is a proposal. "Plan" names the plan part it changes. Effort: S is under 100 lines or one art rule, M is one or two classes with tests.

| Rank | Change | Plan | Effort | In-game effect | Basis |
| --- | --- | --- | --- | --- | --- |
| 1 | Stream only long loops. Every clip under about 10 s plays from memory (no `"stream": true`) | F2.3 sounds; `sounds.json` | S | Ten dinosaurs can call at once and every call plays. Music does not cut out | Vanilla 26.2 streaming pool is `clamp(sqrt(channels), 2, 8)` `[v]`. Nublar streams 37 of 37 entries, and its clips run 1.1 to 6.3 s `[v]`. Recrafted streams none of its 271 files `[v]` |
| 2 | A loudness rule for every dinosaur sound: one target loudness per sound class, true peak at or below -1 dBFS, mono for every positional sound. Add it to the sound-extraction steps | F2.3 sounds, Art list sound rows | S | Roars do not crackle. A growl and a footstep sit at the levels the lead set | Nublar T. rex clips: median -11.5 LUFS, 26 of 37 with a true peak above 0 dBFS `[v]` measured. Recrafted: 241 of 271 files mono, the 30 stereo files are wind, thunder and clicks `[v]` |
| 3 | A small random pitch and volume spread on each play, set per sound event in code | F2.3 sounds | S | Two growls in a row never sound the same | Hurt, death and idle sounds already get vanilla's voice pitch (±0.2 at random, 1.5 for a baby) through `Dinosaur.getVoicePitch` `[v]`. The `ThreatDisplay` roar plays at pitch 1, and keyframe sounds have no handler yet `[v]`. Recrafted lists every file at several pitches, 32 to 48 entries per event `[v]`; code does the same with one entry per file `[inf]` |
| 4 | Subtitles: every Nublar sound event gets a translation key `subtitles.projectnublar.<event>` and a lang entry | F2.3 sounds, D313 options | S | A player with sound off sees "Tyrannosaurus roars" with its direction | 7 Nublar events have a subtitle, but as plain text such as "t.rex roar" `[v]`. The plan has no subtitle rule `[v]` |
| 5 | Screen text fits a font 25 % wider. Every label is measured with `font.width`. A label that does not fit ends in "…" and shows the full text in a tooltip. Test the 320 px window once with the Recrafted font | F1.4 to F1.8 screen style, 320 px rule | S | Machine screens stay readable with any font pack | Recrafted widens "Tyrannosaurus Rex" from 100 to 124 px and "Egg Printer" from 58 to 73 px, line height unchanged `[v]` measured on both sheets |
| 6 | Never carry meaning by hue alone on a screen. A purple isolated gene or a red fault also gets a shape or an icon | D313 contrast, F1.5 | S | A colour-blind player, or one with a pack that dulls colours, still tells genes and faults apart | The Recrafted text shader cuts GUI text chroma to 0.7 and darkens colours above lightness 0.65 `[v]` |
| 7 | Glow must read without bloom. Glow masks, machine lights and fence lights look lit with no post effect. Any halo is painted into the texture or sprite | F2.2 glow masks, F1.10 powered glow (D279), F3.1 lights | S (art rule) | Eyes, screens and live fences look lit with or without a shader pack | Recrafted bloom leaves entity pixels out and needs a marker model on a terrain block `[v]`. Its coloured light comes only from marker quads in block models, 15 fixed tints `[v]` |
| 8 | One "Effects quality" preset (Low, Medium, High) over the BH.1 low-effects switches, and a cost label beside each switch in the config screen | BH.1 low-effects config, D329 | S | A player on a weak PC picks Low once and gets fewer particles, short footprints, no shake | Recrafted gives each of 48 options a `performance_impact` (low, medium, high) and three presets `[v]`. Polytone documents the same scheme `[doc]` |
| 9 | Dust colour source: the colour that vanilla's own block particle would have. That is the average colour of the block model's particle sprite, times the block's `BlockTintSource.colorAsTerrainParticle`, as vanilla 26.2 `TerrainParticle` does `[v]` bytecode, then 30 % paler. Grey sprites are tinted when the particle spawns | F2.3 dust (D340: "the task plan picks the colour source") | S | Dust matches the ground in any resource pack. Grass gives brown dust, as vanilla sprint particles do (the vanilla grass block model's particle texture is `block/dirt` `[v]`) | Recrafted tints grey sprites at spawn from a colormap, cached on spawn `[v]`. Polytone colormaps change block colours, not map colours `[inf]` |
| 10 | Dust on a run clip with no footstep keyframes: a spawn chance per tick that rises with speed and has a cap, 1 to 2 puffs, thrown at a fraction of the body speed. Not a fixed spawn every tick | F2.3 dust fallback | S | A trotting animal raises a little dust, a sprinting one raises more, and a herd does not fill the screen | Recrafted foot particles: chance `clamp(speed × 1.6, 0, 0.25)`, count 1 to 2, velocity 0.5 × body `[v]` |
| 11 | Long-lived particles (footprints, plant bits that rest) update every 5th tick when 2 or more blocks from the player, and skip when out of view | F2.3 footprints, BH.1 | S | Fifty fading footprints cost almost nothing | Recrafted particle physics does this for leaves at rest `[v]` |
| 12 | Each animal starts its breathing and idle sway at a phase from its id | F2.3 layer 7 breathing | S | A herd at rest does not breathe in step | Recrafted animal models offset each mob's cycle by its id `[v]`. The plan stores one phase but no offset per animal `[v]` |

### Compat duties (no new feature)

| Duty | Plan | Why |
| --- | --- | --- |
| Make the D310 slider and button sprites before the first public test jar. Until then, Recrafted players see a black vanilla slider on Nublar's blue panels | F1.10 widget look (D289, D310) | `AbstractSliderButton.java:27-30` uses the vanilla `widget/slider*` ids `[v]`. Recrafted restyles them `[v]` |
| Keep every particle type, sound event and block under a `projectnublar:` id, with vanilla-style particle JSON | F2.3, F1.1, F3.1 | Polytone packs then retune Nublar effects by id with no Nublar code `[doc]` |
| Add three rows to the BH.1 mod test matrix: Polytone, Continuity (Fabric; NeoForge only through Connector), EMF with ETF. One extra run with Recrafted if the lead owns it | BH.1 test matrix | Check: Nublar particles show, fossil blocks mesh with Continuity's default CTM pack and Sodium face culling on (open Continuity bug #826 `[doc]`), dinosaurs unchanged under EMF |
| Remove the dead `incubator_bed` shader | BH.1 cleanup | `#version 120`, no code uses it, `IncubatorScreen.java:25` marks it dead `[v]` |
| Nublar's own plants (R01) use their own block models. They never take a vanilla grass, fern or leaf model as parent | R01 | Recrafted replaces those vanilla models and their blockstates `[v]` |
| If Nublar adds island biomes later, put them in the vanilla tags (`#minecraft:is_jungle` and others) | F4.1, R01 | Recrafted picks fog and sky colours by biome tag `[v]`. A tagged modded biome gets the jungle look |

## 1. Shaders

- **Mechanism** `[v]`. Each module is a resource-pack overlay with a Polytone condition. The pack replaces vanilla core shaders and runs its own post-effect chains through Polytone. It empties the vanilla `transparency` chain and adds about 40 passes in a fixed order (data, surfaces, lighting, water, AO, fog, clouds, anti-aliasing, bloom). Extra camera renders give a heightmap, a near shadow map and a colour capture. Sodium gets its own terrain shaders.
- **Modded content** `[v]`. Nublar dinosaurs and machines draw with vanilla `entityTranslucent` (`DinoLayer.java:57`, `IncubatorRenderer.java:66`). So they get the pack's sun-aware shading and shadows with no change. A mod core shader would get none of the pack's uniforms, and the pack's `fog.glsl` declares a uniform block that a mod shader importing it may not bind `[inf]`. This supports OC6.
- **Bloom, light colour, dynamic light** `[v]`. Bloom finds glowing terrain through a 96 × 64 × 96 grid of marker blocks and masks entity pixels out. Coloured light is a tiny marker quad in a block model; its position encodes the level (1 to 15) and one of 15 fixed tints. Dynamic light reads only the local player's held items, by item id suffix. Vehicle headlights get nothing; the LambDynamicLights row in BH.1 stays.
- **Weather and biome** `[v]`. Fog, sky and rain looks come from Polytone biome modifiers matched by tag, with an overworld fallback. Jungle fog ends at 26 % of the view distance (18 % in rain).
- **Cost** `[v]`. AO, bloom, clouds and volumetrics run at half resolution. Slow data updates (heightmap every 200 ticks). Off options swap in pass-through chains. No automatic quality control. The pack lists Iris as a conflict.
- **Armour weathering** `[v]`. The local player's armour picks up sand (with a red-sand tint), snow and wetness piece by piece; there is no dirt or mud. Each piece keeps a value from 0 to 1 that rises while the player walks on or touches sand or snow (about 4 to 5 s to 63 %), fades slowly (sand about 2.8 min), melts fast in warm places (about 12.5 s), and washes off in water up to the waterline. Wetness dries in about 17 to 50 s, faster near fire. The shader fills dark crevices first, in whole texture pixels, from fixed 6-step palettes. Ideas for the F2.3 snow cover and later mud overlays; no code to take (note 01).
- **For Nublar:** compat only. Nublar must not copy any of it: the target is player hardware, and OC6 forbids a core shader.

## 2. Visual effects and particles

- **Foliage** `[v]`. Grass, ferns and leaves sway in the fragment shader by whole texel rows, so the pixel look stays. One shared wind value (speed, direction, rain boost, calmer in shade) drives plants and particles. Plants bend away from the player only: three packed numbers go to the shader, with a 1.1-block radius. Big mobs push nothing. A dinosaur version needs Nublar's own terrain shader data: after Beta, reference only.
- **Particles** `[v]`. Leaves, petals, embers and motes are small grey sprites (5 × 5, 2 × 2, 1 × 4 to 1 × 8 strips) tinted per biome at spawn. Size and alpha follow curves in the particle ticker. Cost control: low emit chance, a limit of 1 on emitter particles, slow updates for resting particles, skip when out of view.
- **Player combat clips** `[v]`. An EMF file drives a three-hit sword combo with damped springs. The step caps the frame time at 0.05 s. Fast repeated swings scale the motion down. D327 already picks an exact spring, which is better; the frame-time cap and the scale-down for repeated attacks are ideas for F2.6 bites.
- **Style.** The pack's sprites are smaller and flatter than the D340 dust sprites. The lead's campfire-smoke rule (D330) stays.

## 3. Sounds

- **Ambience** `[v]`. No vanilla biome ambience. Polytone sound emitters sit on grass, ferns, leaves, fire and torches, with a chance scaled by sky light, so the sounds play outdoors only. One hidden particle that follows the player carries 14 emitters: wind, storm wind, thunder, grass rustle on footfalls. Reference for R01 island ambience; Nublar would use a client tick listener.
- **Overlays and `sounds.json`** `[inf]`. The pack ships seven combination folders for three sound toggles. One `sounds.json` per path wins, so N toggles on vanilla events need 2^N - 1 files. Nublar avoids this by using its own events and toggles in code.
- **Block sound types** `[v]`. A Polytone block modifier sets five event ids (break, place, hit, step, fall). Step, hit and fall are redirect events with volume multipliers 4.5, 4 and 2, so one recording serves all five. Nublar blocks with vanilla sound types take the player's pack sounds, which is fine. Own sound types only where vanilla fits badly.
- **Files** `[v]`. 271 Vorbis files, 4.6 MB, 0.1 to 5.1 s long, 241 mono, median -37 LUFS. 10 of 111 events have subtitles.

## 4. GUI, font and textures

- **GUI** `[v]`. The pack restyles `widget/button*`, `widget/slider*` and `tooltip/*` with nine-slice sprites: near-black fill, white hover ring. It does not touch slot, container, edit box, scroll bar or checkbox sprites.
- **Font** `[v]`. It swaps only the bitmap sheets of `minecraft:default`. Letters are wider; line height stays 9. A GUI text shader also dulls colours. See changes 5 and 6.
- **Immersive HUD** `[v]`. It fades `minecraft:hud/*` sprites when the player is idle, with no Java. Nublar sprites are not faded. White text or quads in the hotbar strip may fade `[inf]`.
- **Connected and overlay textures** `[v]`. Glass uses Continuity `ctm_compact` with 5 tiles. Grass and dirt edges use `method=overlay` on stone. Stone uses mirrored blockstate variants against tiling. F1.1 already plans weighted model variants and a position-seeded baked model; Continuity's source confirms the method: wrap the baked model, transform quads, cache per state and sprite, and take the random from the position without using the shared random `[v]`.
- **Entity models** `[v]`. Sheep, cow, pig and chicken get EMF animation (adapted from Fresh Animations): id-based phase, frame-time smoothing, ear twitch, breathing, blink, look-at, head down to graze. Change 12 takes the id phase. The others are already in the F2.3 layers or need bones the models lack.

## 5. Polytone, Continuity and EMF

| Mod | 26.2 | Loaders | Acts on Nublar? | Verdict |
| --- | --- | --- | --- | --- |
| Polytone 26.2-6.9.1 (`545be37`) | Yes `[doc]` | Fabric, Forge, NeoForge | Yes, by id, tag or regex: blocks, particles, sounds, colormaps, screens. Not GeckoLib bones `[v]` | Compat duty and patterns (data modifiers, conditions, cost labels). No dependency |
| Continuity (`d15d442`) | Yes `[doc]` | Fabric; NeoForge only through Connector | Yes, on block models by `matchBlocks` `[doc]`. Not entities or GeckoLib block entities `[inf]` | Compat test. Pattern for F1.1 baked-model variation |
| EMF 3.3.12 (`c801331`) | Yes `[doc]` | Fabric, Forge, NeoForge; needs ETF | No: it hooks `EntityModelSet.bakeLayer`, `ModelPart` only, no GeckoLib code `[v]` | Ignore for dinosaurs. Patterns: animation detail by distance scaled by FOV and body size; bake models once (EMF stops after 64 bakes of one layer) |

## Open questions for the lead

1. Which of the twelve changes and six duties go into the plan? Each accepted item becomes a register row from D349.
2. Do you own Recrafted, and do you want one test run of Nublar with it at BH.1? The research did not run it.

## Not done

- No in-game run of the pack or the mods.
- Polytone's wiki pages for colormaps and particles returned 404. The colormap effect on `BlockColors` is inferred.
- The one-`sounds.json`-per-path rule is inferred from the pack's layout; a two-overlay test pack would prove it.
- What 26.2 does with a `sounds.json` volume above 1 (the pack uses up to 4.5) was not read in code.
- Precipitation, puddles, lightning, underwater and water shaders were read in outline only.
