# 03 Sounds: what Recrafted v0.2.1 teaches Nublar

Labels: [v] read or measured (zip-relative path given); [doc] web page (URL given); [inf] my inference.
Measuring script: scratchpad\scripts\probe_ogg.py (ffprobe + ffmpeg ebur128). Raw data: scratchpad\scripts\pack_probe.json and nublar_probe.json.

## Scope

- Folders read [v]: 04_sounds/ambient_sounds (6 Polytone block-modifier JSON, no audio), 04_sounds/block_sounds (28 block-modifier JSON + 21 .ogg), 04_sounds/item_sounds (9 .ogg), 04_sounds/sound_replacements (7 sounds.json, no audio).
- Also read [v]: assets/recrafted/sounds.json (111 events), assets/recrafted/polytone/sound_events.csv (111 names), assets/recrafted/polytone/config_entries/{block,ambient,item,menu}_sounds.json, assets/minecraft/polytone/entity_modifiers/player.json, assets/minecraft/polytone/custom_particles/player_particle.json, pack.mcmeta overlay entries, en_us.json config strings.
- Every sounds.json in the pack: assets/recrafted/sounds.json plus the 7 in 04_sounds/sound_replacements. No other file in the pack mentions sound events [v, grep].
- Audio files [v, ffprobe]: 271 .ogg, 4,573 KB. Split: 42 ambient, 135 block, 17 step, 10 menu click, 10 item (random/), 27 recrafted item, 21 + 9 in the 04_sounds overlays minus overlap with the base tree (the overlays add paths such as block/sand, step/grass, step/cloth, step/snow, item/shield, random/bowhit).
- Not found: no biome ambient settings (ambient loop, mood, additions), no music, no biome sound modifier, no music disc. Polytone folder list has no biome-sound file [v: ls assets/*/polytone].

## What changes in game

Config switches [v: config_entries/*.json, en_us.json]: block_sounds, ambient_sounds, item_sounds, menu_sounds. Each is a true/false toggle, default true, section "sounds". The labels read "Recrafted" and "Vanilla".

1. Block sounds (break, place, hit, step, fall) [v: 04_sounds/block_sounds/.../polytone/block_modifiers/*.json].
   - New sound groups for 28 modifier files. Examples: stone ores (8 ores), cobblestone and mossy cobblestone, stone bricks and mossy stone bricks, cracked bricks (5 blocks), deepslate (1), deepslate-brick family (31 blocks), nether bricks (10), netherrack (3), sandstone, gravel, dirt, podzol (6), clay, mud, ice (ice, packed ice, blue ice, glowstone), magma, bone block, gilded blackstone, copper grate (metal grate), cake.
   - Leaves, moss, vines, flowers and grass-like plants (41 blocks) get one "leaves" group. The grass block gets podzol break, place, hit and fall, but leaves-style steps.
   - TNT gets the vanilla "wood" sound type (string form) [v: sounds_block_wood.json].
2. Vanilla events replaced through minecraft:sounds.json [v: sound_replacements/block/assets/minecraft/sounds.json]: block.stone.*, block.sand.*, block.wood.* (break, place, hit, fall, step), and step-only for bamboo, bamboo_wood, glass, gravel, metal, mud, sculk, snow, wool. 24 events.
3. Item events [v: sound_replacements/item/...]: entity.arrow.shoot (3 variants), entity.skeleton.shoot (event reference, pitch 1.2), entity.snow_golem.shoot (event reference, volume 0.5), entity.snowball.throw, entity.item.pickup. 5 events.
4. Menu event [v: sound_replacements/menu/...]: ui.button.click only. 1 event, 80 entries, 10 stereo files.
5. New sound events in the recrafted namespace [v: assets/recrafted/sounds.json, sound_events.csv]: 111 total.
   - 25 base block events (block.stone_ore, block.leaves, ...), 68 variant events named *.rc_hit, *.rc_step, *.rc_fall.
   - 9 ambient events: ambient.fire, foliage, wind, wind_storm, thunder, soft_rustle, heavy_rustle, grasshoppers, birds_and_insects.
   - 9 item events: item.bow.draw_1..4, item.hotbar.draw, item.hotbar.stow, item.shield.raise, item.shield.lower, item.sword.draw.
6. Ambience, fired by Polytone emitters, not by vanilla biome data [v: see Ambient system]: wind everywhere outdoors in the overworld, storm wind and thunder in rain, leaf and grass rustle, grasshoppers, fire and torch crackle, grass rustle under the player's feet.
7. Player-action sounds fired by a Polytone custom particle [v: player_particle.json]: bow draw in four stages (ticks 1, 6, 11, 16 of item use), hotbar draw and stow (on main-hand change), sword draw (on swapping to a sword above stone tier), shield raise and lower.

Not replaced [inf]: mobs, music, weather loops, cave ambience, water, doors, redstone. The pack gives no mob voices.

## Overlay combination mechanism

Evidence [v: pack.mcmeta]
- Three toggle overlays carry audio and block modifiers: 04_sounds/block_sounds, ambient_sounds, item_sounds. Each uses polytone_condition as an object: override true, require_config "recrafted:<name>". Format range 88 to 88.
- Seven more overlays under 04_sounds/sound_replacements use polytone_condition as an expression string. Example: config('recrafted:block_sounds') && !config('recrafted:item_sounds') && !config('recrafted:menu_sounds'). Each of the 7 non-empty on/off combinations of {block, item, menu} has exactly one folder. The all-off case has no folder.
- Each folder holds only assets/minecraft/sounds.json.
- Key sets are disjoint: block has 24 events, item 5, menu 1, no overlap [v: scripts\combos.py]. Each combination file holds exactly the union of its parts, with identical values (checked for all four combined files, 0 differences).
- Every event has "replace": true [v].
- The new recrafted:* events live in assets/recrafted/sounds.json in the base pack, with no condition. They always exist. Only the Polytone modifiers and the minecraft:* overrides depend on the toggles.

What this shows
- [inf] Within one pack, overlays do not add their sounds.json files together. For one resource path, only the winning overlay's file is visible. If vanilla merged them, three small files (block, item, menu) would be enough and no combination file would exist. The author wrote seven files with copied content, so he must have hit this.
- [inf] A "replace": true entry also wipes the vanilla entry for that event key. So each combination must carry the full set of overrides for its toggles.
- [inf] Cost: N independent toggles need 2^N - 1 copies of the override file. Three toggles need seven. Copies must be kept in sync by a script. A fourth toggle would need fifteen.
- Audio files and Polytone JSON do not have this problem: they sit at unique paths per overlay, so independent toggles work with one condition each.

How Nublar can make independent toggles [inf]
1. Put new events in our own namespace (projectnublar:...). One sounds.json, never shadowed. This is what Recrafted does for its 111 events.
2. Put the per-toggle choice into the block or entity code, not into a vanilla-key override. Pick the SoundType or event with a config check.
3. If a vanilla key must be replaced, use "type": "event" redirects. In Recrafted, the vanilla key block.stone.step stays one tiny file, and the real entries sit in the recrafted namespace. Only the small redirect list then needs the 2^N copies [v: rc_step events use name "recrafted:block.x", type "event"]. Recrafted does not go this far for vanilla keys: its vanilla overrides carry full 48-entry lists.
4. Nublar does not need this for its own sounds. It needs it only for a Nublar resource pack that wants to toggle vanilla keys.

## Ambient system

Core finding [v]: ambience is built from Polytone emitters. There is no use of vanilla biome ambient settings, mood, additions, or music.

A. Block emitters [v: 04_sounds/ambient_sounds/.../block_modifiers/sounds_ambience_*.json]
Key names: targets (block ids), sound_emitters[] with sound, chance, pitch, volume, source, biomes.

| File | Targets | Sound | Chance | Pitch | Volume | Source |
|---|---|---|---|---|---|---|
| fire | fire, campfire | recrafted:ambient.fire | 5 | 1.0 | 0.45 | not set |
| torch | torch, wall, soul, copper (6 ids) | ambient.fire | 2 | 1.0 | 0.2 | not set |
| foliage | short_grass, fern, bush | ambient.foliage | 0.0005 * SKY_LIGHT | 1 | 0.5 | ambient |
| foliage | same | ambient.grasshoppers | 0.0005 * SKY_LIGHT | 1 | 0.15 | ambient, biomes #minecraft:is_overworld |
| foliage | same | ambient.birds_and_insects | 0 | 1 | 0.075 | ambient, biomes #minecraft:is_overworld |
| tall foliage | large_fern, tall_grass, sunflower | same three emitters, same numbers | | | | |
| leaves | acacia, azalea, flowering azalea, cherry, jungle, mangrove | ambient.foliage | 0.0001 * SKY_LIGHT | 0.8 | 0.5 | ambient |
| leaves oak/birch | birch, dark oak, oak | ambient.foliage | 0.002 | 0.8 | 0.5 | ambient |

- birds_and_insects has chance 0 [v]: the event and five files exist, but nothing fires it. It is unfinished or reserved [inf, early access].
- "SKY_LIGHT" is a Block Expression variable, 0 to 15 [inf; the player emitter divides by 15 for the same value]. A grass block in full sun gives 0.0075 chance per tick sample. In a cave or under a roof it gives 0. That is how the pack keeps grass noise outdoors only.
- Jungle, acacia and other "tropical" leaves have sky-light-scaled chance. Oak and birch have a flat chance. So oak leaves rustle even in shade [inf].
- biomes accepts a tag ("#minecraft:is_overworld") [v; also in [doc] https://github.com/MehVahdJukaar/polytone/wiki/Block-Properties-Modifiers, which lists biomes, state_predicate, x/y/z, distance_delay]. The pack uses it only to keep grasshoppers out of the Nether and End. It uses no per-biome sound. Rainforest, desert and swamp have no distinct ambience.
- [doc] The wiki says block emitters run only for blocks inside the particle ticking range. [inf] That is vanilla's random display tick around the player (positions sampled each client tick). "chance" is a per-sample probability, not per second. Tuning values need a play test.
- "source" is not in the wiki's emitter key list [doc]. The pack uses the values ambient, weather, players [v]. [inf] They match Minecraft's SoundSource categories. That puts each sound under the matching volume slider (Ambient/Environment, Weather, Players). Fire and torch have no source, so they take the Polytone default [inf].

B. Global ambience from one player-bound particle [v: assets/minecraft/polytone/entity_modifiers/player.json; custom_particles/player_particle.json]
- player.json adds an emitter: spawn minecraft:player_particle with count 1. Chance is 1 for the first 100 ticks of the player entity or while a warm-up counter is below 1200, then once per 20 ticks. This re-spawns the carrier if it dies.
- player_particle.json: lifetime set to the largest unsigned 64-bit value (lives forever), "limit": 1, no physics. Its ticker copies the player's position each tick (dx = PLAYER_X - X, etc.). The carrier follows the player.
- It has 14 sound_emitters. Chance expressions can read config('recrafted:ambient_sounds'), global.dimensionType, global.rain, o.skyLight, player.itemUsed(), player.itemUseTicks(), player.walkAnimation(), player.at().block(), random.rand().
- Ambient entries:

| Sound | Source | Chance (per particle tick) | Volume | Gate |
|---|---|---|---|---|
| ambient.wind | ambient | 0.2 * skyLight / 15 | 0.2 | ambient_sounds on; overworld only |
| ambient.wind_storm | weather | 0.2 * skyLight / 15 | 0.35 | also global.rain >= 1 (full rain) |
| ambient.thunder | weather | 0.005 * global.rain * skyLight / 15 | 0.35 | overworld only |
| ambient.soft_rustle | players | 1 on each foot-fall in short grass, fern, bush; else 0 | 2 | block_sounds on |
| ambient.heavy_rustle | players | same for tall grass, large fern, sunflower | 1.5 | block_sounds on |

- Footfall detection [v]: wk = 0.6662 / pi; fire when floor(walkAnimation * wk) changes between ticks and speedSq >= 0.01. It reproduces vanilla's walk-animation phase, so the rustle lands with the leg swing.
- A global expression wind_speed [v: global_expressions/wind_speed.json] mixes two sine waves on global.time, plus a rain gain: (0.40 + 0.15 sin(t*0.01309) + 0.10 sin(t*0.004712)) * (1 + clamp(rain,0,1)). Periods about 480 ticks (24 s) and 1333 ticks (67 s). This drives wind in the foliage/shader animation. The wind sound chance does not read it [v: chance formula above]. So audio wind and visual wind are not linked [inf].
- [inf] Wind chance 0.2 per tick with clips about 2 s long and volume 0.2 stacks clips on top of each other. It builds a continuous bed from overlapping one-shots. That removes loop seams. Needs a play test to confirm. I could not measure it.
- Thunder: "ambient.thunder" is a custom event of 5 s stereo clips. It is not tied to lightning strikes. The vanilla thunder event is not replaced [v: no entity/lightning key in any sounds.json]. So this is extra distant thunder during rain [inf].
- Time of day: no clock gate. No night, dawn or dusk logic anywhere [v: grep]. Height: only through sky light. Near water: nothing. Moon phase, temperature, Y level: nothing.

Where each sound plays: wind and thunder anywhere in the overworld under open sky. Grass sounds on grass blocks near the player in sun. Leaf rustle near leaf blocks. Fire and torch crackle near those blocks. Rain gates storm wind and thunder only.

## Variation and sounds.json settings

[v: assets/recrafted/sounds.json and 04_sounds/sound_replacements/*/sounds.json]
- Keys used on entries: name, volume, pitch, type ("event"). Keys used on events: sounds, replace, subtitle. Counts of weight, stream, preload, attenuation_distance, category in all 8 files: 0.
- Variation method: the author lists every combination as a separate entry. A typical event has 6 files x 2 volumes x 4 pitches = 48 entries. Four-file events have 32. Volumes are 0.933 and 1 (a 7 % spread). Pitches are 0.9, 1.0, 1.1, 1.2.
- Vanilla picks one entry at random. Equal weights [inf]. So each pick gives a random file, volume and pitch together. This avoids repeats without code, at the cost of 48-line lists.
- Ambient events: pitches 0.9 to 1.2, volumes 0.2 and 0.5 (fire 0.2/0.5, wind 0.3/0.5, thunder 1.2/1.5). Foliage uses 8 files at fixed 0.5 volume and pitches 0.9, 1, 1.1.
- Hit, fall, step get per-event volume multipliers. The base block event (used for break and place) is at 0.933 to 1. The variants are redirect events (type "event") that call the base event with a multiplier: rc_step 4.5, rc_hit 4, rc_fall 2. The cake step is 2.25 [v]. [inf] These values are the inverse of vanilla's fixed factors (step plays at 0.15, hit at 0.25, fall at 0.5 of the block volume). So the pack rescales every kind of sound to about the same loudness. That is why one base event can serve all five slots. I did not confirm the vanilla factors from code in this task.
- Direct vanilla overrides use the same numbers, e.g. block.stone.step volume 4.2/4.5, hit 3.733/4, fall 1.867/2 [v].
- Wool: one source file, 8 entries of pitch 4.2 to 4.5 [v]. A pitch that high reads as a trick for a very low or slowed source. [inf]
- Other events use pure pitch spread: metal step 0.7 to 0.9, glass step 1.2 to 1.6, snow step 0.5 to 1.1.
- Player emitters add randomness through expressions: pitch "0.8 + random.rand() * 0.14" (hotbar, shield), "0.88 + random.rand() * 0.1" (sword) [v].
- Subtitles: only 10 of 111 events carry one: two keys subtitles.block.generic.footsteps and subtitles.item.bow.draw [v]. Most ambient and block events have none.
- stream, preload, attenuation_distance: not used. All clips are 0.1 to 5.1 s, so streaming is not needed [inf].
- Volume above 1: step, hit, fall and menu entries use 4.2 to 4.5. Whether the engine clamps the final gain at 1 and what it does to hearing range is an open question (see below).

## File facts table

[v: ffprobe -show_streams, ffmpeg ebur128; scratchpad\scripts\summ.py] LUFS is integrated loudness. Very short clips fall under the meter's gate and show -70 (no value).

| Group | Files | Size KB | Channels | Sample rate | Bitrate kbps | Length s | LUFS (min / median / max) |
|---|---|---|---|---|---|---|---|
| assets/minecraft/sounds/block | 135 | 1,936 | mono 135 | 48000 | 100-434 | 0.11-2.16 (median 0.65) | -70 / -39.4 / -21.4 |
| assets/minecraft/sounds/step | 17 | 249 | mono 17 | 44100 x11, 48000 x6 | 125-270 | 0.31-1.39 | -70 / -27.9 / -22.1 |
| assets/minecraft/sounds/ambient (fire, foliage, random) | 5 + 16 + 9 | in 1,491 total | mono | 44100/48000 | 100-232 | 0.93-3.37 | foliage -51 to -49; fire -31 to -29; birds -31 to -21 |
| ambient wind, wind_storm, thunder, soft/heavy rustle | 8 + 4 + 8 | | stereo | 48000 | 84-172 | 1.0-5.1 | wind -35 to -32; storm -19 to -17; thunder -39 to -23; rustle -44 to -29 |
| menu_click | 10 | 98 | stereo 10 | 48000 | 151-242 | 0.30-0.62 | -37 median |
| random (item) | 10 | 135 | mono | 48000 | 89-200 | 0.43-1.47 | -41 to -31 |
| assets/recrafted/sounds/item | 27 | 300 | mono 27 | 44100 | 111-317 | 0.32-1.10 | -70 / -28 / -15 |
| 04_sounds/block_sounds (overlay audio) | 21 | 239 | mono | 44100 x15, 48000 x6 | 148-182 | 0.44-0.87 | -44 / -37 / -24 |
| 04_sounds/item_sounds (overlay audio) | 9 | 126 | mono | 44100 x4, 48000 x5 | 100-142 | 0.76-1.52 | -36 / -26 / -25 |
| Pack total | 271 | 4,573 | mono 241, stereo 30 | 44.1/48 kHz | 84-434 | 0.11-5.10 | |
| Project Nublar sounds/entity (T. rex) | 37 | 1,145 | mono 37 | 44100 x31, 22050 x6 | 47-194 | 1.06-6.33 (median 3.07) | -19.4 / -11.5 / -8.4 |

- Codec: Ogg Vorbis, all 308 files measured.
- Stereo list [v]: 20 ambient (thunder x4, wind x4, wind_storm x4, heavy_rustle x4, soft_rustle x4) and 10 menu clicks. Rest are mono. [inf, from the lead's rule] Stereo sounds play without position. Wind, thunder, rustle and menu clicks are meant to be non-positional bed or player sounds, so stereo is right for them. All other sounds, including fire, birds, grasshoppers and foliage, are mono and positional.
- The pack's clips are quiet (median -37 LUFS) and are lifted with volume multipliers up to 4.5. Nublar's T. rex clips are loud (median -11.5 LUFS) and 26 of 37 have true peaks above 0 dBFS (up to +2.4 on the bites) [v]. Peaks over 0 dBFS can clip after decoding [inf].
- Nublar: all 37 entries use "stream": true, one category "entity", no volume, pitch, weight or attenuation_distance [v: projectnublar/sounds.json].

## Block sound types

[v: block_modifiers/*.json]
- Mechanism: a Polytone block modifier file with "targets" (block ids) and "sound_type". The sound_type value is either a string (vanilla type name, "wood" for TNT) or an object with break_sound, place_sound, hit_sound, step_sound, fall_sound (all event ids).
- [doc] https://github.com/MehVahdJukaar/polytone/wiki/Block-Properties-Modifiers lists three more forms: "copy(minecraft:block)", a custom id "namespace:name", and optional pitch and volume in the object. Custom types can sit in assets/<ns>/polytone/custom_sound_types/<name>.json.
- Pack practice: break and place use the base event (recrafted:block.X). Hit, step, fall use the three loudness-matched redirect events (block.X.rc_hit/rc_step/rc_fall). Fall has no own file; it points to the same base event [v: rc_fall -> block.X, type event, volume 2].
- Mixed types are allowed: grass block uses podzol for break, place, hit, fall and leaves for step. Cake: wool break and place, own step, base cake for fall.
- Which route: Polytone sound_type for blocks that vanilla already has; plain sounds.json overrides for sound types shared by many blocks (block.stone.*, block.sand.*, block.wood.*). So both routes are in use.
- One event covers many blocks. 31 deepslate-brick-family blocks share one file and one sound type.
- Could Nublar do this for fossil ore, machines and fences? Yes.
  - Own blocks do not need Polytone. Nublar registers a SoundType in Java (NeoForge and Fabric both accept one in block properties) with five projectnublar:* events. The shape is the same as the Polytone object: five event ids. The loudness-matched trio (step, hit, fall as redirect events) is the pack's idea worth copying. [inf]
  - Polytone is a resource-pack-side tool; it needs the user to install Polytone. A mod cannot rely on it.
  - Fossil ore: ore break/place (rock plus bone crack), step as stone. Machine: metal, with one lighter step. Fence: metal grate sound (copper grate in the pack is a model).
  - A resource pack with Polytone could retarget Nublar blocks by id. Nothing in Nublar blocks that [inf]. Still, give Nublar blocks a stable SoundType name so packs can copy it.

## Nublar fit table

| Finding | Nublar module | Use | In-game effect for Nublar | Effort |
|---|---|---|---|---|
| Five-slot block sound set with loudness-matched step, hit, fall redirect events [v] | F1.1 fossil ore, amber blocks; F1.3-F1.10 machines; F3.1 fences | Adopt pattern | Breaking fossil rock sounds like bone cracking in stone. Walking on machine floors rings softly. One recording serves five actions. | S |
| Variation by listing file x volume x pitch combinations [v] | F2.3 vocals, bites; F1.x UI | Reference (code the spread instead) | Two roars never sound the same. Prefer a Java pitch/volume random per play to save 48-line lists. | S |
| Per-sound pitch and volume spread in the T. rex list (none today) [v: Nublar sounds.json] | F2.3 | Adopt pattern | Repeated T. rex growls differ in pitch by about 10 %. | S |
| Mono for positional, stereo for non-positional [v measured; inf rule] | F2.3 all species; F3.1 fence hum; R01 ambience | Adopt pattern | Dinosaur calls fade with distance and pan. Wind and menu clicks stay at the listener. | S |
| Short clips, no streaming [v: pack uses no stream; 0.1-5 s clips] | F2.3; BH.1 | Adopt pattern | Many dinosaurs calling at once do not use up the small pool of streaming channels. Nublar flags all 37 clips as stream today. Check clips over about 6 s only. [inf on pool size] | S |
| Pack loudness median -37 LUFS vs Nublar -11.5 LUFS, true peaks above 0 dBFS on 26 clips [v] | F2.3; sound-extraction project | Compat duty | Quieter beds under loud calls. Normalise every extracted clip to a target and keep true peak under -1 dBFS. | S |
| Polytone block emitters gated by SKY_LIGHT and biome tag [v] | R01 jungle island ambience | Reference | Outdoor-only insects, birds and leaf rustle in R01. For own blocks we need a Java client tick. A Polytone player could also carry them. | M |
| Rain gates storm wind and thunder via global.rain [v] | R01; F4.2 vehicles | Reference | Rain on the island adds storm wind and thunder. We would write this in a client tick listener. | M |
| Overlapping 1-2 s one-shots at high chance to build a wind bed [inf] | R01; F3.2 generators | Reference | A hum or breeze with no loop seam. Needs play test. For fence hum and generator hum use a real looping SoundInstance. | M |
| Footstep rustle keyed to walk animation phase [v] | F2.3 footsteps on animation keyframes; R01 | Adopt pattern | Same idea for dinosaur feet: fire the step on the keyframe, scale by body size. Fits our plan. | S |
| Custom sound events registered in own namespace (sounds.json always loaded, never shadowed) [v] | All sound modules | Adopt pattern | No clash with other packs. Nublar already does this. | S |
| One sounds.json per pack: overlays hide each other, so toggles need 2^N copies [inf] | Any future Nublar resource-pack options | Compat duty | If a Nublar pack wants three toggles that change vanilla keys, it needs seven files. Avoid; toggle in code. | S |
| Config toggles per sound group (block, ambient, item, menu) with default on [v] | BH.1 options | Adopt pattern | Players can switch Nublar ambience and dinosaur calls off separately. Tied to SoundSource sliders. | S |
| Use SoundSource categories (ambient, weather, players) so the mixer sliders work [v, inf] | F2.3 (now all "entity"); R01 | Adopt pattern | Nublar's ambience obeys the ambient slider, dino calls the hostile or neutral slider. | S |
| Subtitles on 10 of 111 events only [v] | F2.3, accessibility | Reference | Give every dinosaur vocal a subtitle; the pack does not set the bar. | S |
| Pack replaces vanilla block, bow, pickup, click sounds with Polytone/override [v] | Compat with Recrafted users | Compat duty | Nublar's own events are not touched. Nublar blocks using vanilla sound types (stone, wood, metal) will change if the player has Recrafted. | S |
| Machine screens: menu click replaced via ui.button.click [v] | F1.3-F1.10 UI clicks | Not applicable | Nublar buttons play the vanilla click. A custom click needs our own event; the pack's solution replaces the global click for all menus. | S |
| Hotbar/sword/shield/bow cues from a player-bound particle with item-use ticks [v] | F4.3 guns (blocked) | Reference | If guns return: a charge-up cue per use-tick stage, like the bow's four stages. In Java use the item's use-tick callbacks. | M |
| Vehicle engine loop, horn, doors | F4.2 | Not applicable | The pack has none. | - |

## Open questions

1. Does vanilla 26.2 really show only one sounds.json per overlaid pack path? The 7 combination files imply yes. I read no engine code. Quick check: load a test pack with two overlays that carry different sounds.json.
2. What does 26.2 do with entry volume above 1 (4.2 to 4.5 here)? Does the final gain clamp at 1? Does hearing range grow? [inf: older versions clamp gain and widen hearing range with volume]. It matters for Nublar bites and roars.
3. Wind chance 0.2 per tick: is the emitter checked every tick, or every N ticks? The wind bed claim needs a play test.
4. How often Polytone samples block emitters (chance 0.0005 * SKY_LIGHT). Needs a play test or Polytone source read. The wiki gives no frequency.
5. Does `limit: 1` plus the respawn rule in player.json hold in multiplayer and with other players? Not tested.
6. birds_and_insects: chance 0, unfinished or hidden by a later toggle? Early access.
7. Why does block.dripstone have volumes 4.2/4.5 and no rc_* variants, and no modifier uses it [v]? Looks unused in v0.2.1.
8. Which sound files of the pack are original recordings vs. converted vanilla? Not measured; no spectral comparison done.
9. Does Polytone support Minecraft 26.2 stable? The pack targets format 88; I did not check Polytone's release page.

## Checked by the main session (2026-11-04)

- Streaming pool [v]: vanilla 26.2 `com.mojang.blaze3d.audio.Library` (javap on the Loom-mapped 26.2 jar) sets the streaming pool to `clamp(sqrt(channel count), 2, 8)` and the static pool to `clamp(channel count - streaming, 8, 255)`. So at most 8 streamed sounds play at once, music and records included. The open question on the pool size is closed.
- Nublar `sounds.json` [v]: 37 of 37 entries have `"stream": true`. Seven events carry a `subtitle`, but each is plain text (for example "t.rex roar"), not a translation key. The file also has a `category` key, which vanilla `sounds.json` does not read.
- Correction [v]: the fit table says Nublar has no pitch spread. That is true for `sounds.json` only. `Dinosaur.getVoicePitch()` (`Dinosaur.java:1383`) returns vanilla's voice pitch, 1.0 or 1.5 for a stage-1 baby, each moved by `(rand - rand) × 0.2`, so ambient, hurt and death sounds already vary. `ThreatDisplay.java:41` plays the roar with volume 10 and pitch 1, so it does not vary. The GeckoLib sound keyframe handlers in `Dinosaur.java:290-292` are empty.
