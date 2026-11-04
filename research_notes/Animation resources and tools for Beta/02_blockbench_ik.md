# Blockbench IK (null objects) for planted feet

Research note for the animation resources report. Written 2026-11-03.

Scope: item 2 of the handoff. Can the artist or an agent use Blockbench IK (null objects) to plant the feet in walk and run clips? Does the GeckoLib plugin 4.1.3 export keep the result, or must a bake step come first?

Labels:

- `[v]`: checked in this session, on the lead's PC or in source. Source facts come from `10_other_finds.md` section B, which cites the Blockbench repository (`master` `ec0b282`, version 5.2.2, and tag `v4.12.5`) and the plugin registry (`fa29c5a`).
- `[doc]`: from release notes or the plugin registry.
- `[inf]`: an inference.

## What is on the lead's PC

- Blockbench 5.2.2. [v] (`Blockbench.exe` product version 5.2.2.0)
- The GeckoLib plugin installed is `animation_utils.js` version 4.1.3, "GeckoLib Animation Utils". It declares `min_version` 4.12.0 and `max_version` 5.0.0. [v] (local plugin file)
- The plugin registry marks 4.1.3 "Deprecated". Its replacement is the plugin `geckolib` ("GeckoLib Models & Animations") 4.2.5, for Blockbench 5.0 and later. [doc] The replacement is not installed. [v]
- The replacement's changelog (registry `plugins/geckolib/changelog.json`, last change `5f4735d`, 2026-04-19) lists fixes that touch the mod's clips: 4.2.1 "exporting empty quotes instead of keyframe values" and failed animation exports for some models; 4.2.3 importing animations with sound keyframes; 4.2.5 an `.animation.json` import that reset GeckoLib frame keys. It lists no change to the exported file format. [doc]
- So the artist's tool runs a deprecated plugin outside its declared range. [inf] The CLAUDE.md note "the plugin cannot open `post: {vector}` keys" may come from this. The 4.1.3 import code reads `pre` and `post` objects that hold a `vector`. [v] (`10_other_finds.md` F) Nobody has tested the new plugin on the mod's clips. [inf]
- No artist model in `ProjectNublar-Assets/assets/dinosaurs` has a null object or an IK target today. [v] (count over 28 current `.bbmodel` files; the review counted 31 with the `old` copies, also with none)

## Answers

1. **Can the artist or an agent use Blockbench IK to plant the feet?** Yes. A null object with an IK target and an IK root drives a bone chain with a FABRIK solver. IK poles came in 5.2.0. [v] [doc] In 5.2.1 and later, IK is off when the null object has no keyframes, so IK can be on in a walk and off in an idle. [doc]
2. **Does the export keep the result without a bake?** Partly. Blockbench's own export samples the IK at the `animation_sample_rate` setting (default 24 per second) and writes plain rotation keys. That write replaces every other key on the bone (position, scale and other rotation keys). [v] The GeckoLib plugin adds no IK code, so its easing does not apply to the samples. [v] [inf] So exporting live IK can erase hand keys on the same bones. [inf]
3. **Is there a bake step?** Yes. "Bake Inverse Kinematics" in the Animation menu, since 4.9.0. [v] [doc] On 5.2.2 (the lead's version) it samples the IK at the clip's snapping rate, removes the null object's keys (IK is then off for that chain), converts each sample to be relative to the bone's model rotation, and writes a key only where the value changes. [v] On 4.12.x two of those steps are missing; on 5.2.2 they are present. [v]
4. **Recommended flow:** bake, check the keys, run "Optimize Animation", then export. Check a bone with a model rotation, the loop end and the foot track after the bake. [inf] The clip checker (item 3) can check the foot track.

## Fit with the plan

- The clip kit uses mcblend in Blender for IK, and bakes only at key times with "Extra frames" (F2.3, line 3812). [v] Blockbench bake samples every frame at the snapping rate, so it needs no extra frames. [inf]
- Blockbench IK keeps the clip in the artist's own tool. That avoids the Blender round trip for walk and run fixes. [inf]
- The in-game leg IK (layer 2) still runs. A baked clip with planted feet gives it less to fix. [inf]
- Limit: the bake plants feet only for the clip's own speed. The game speed must match the stride (D292). [inf]

## Hands-on test (2026-11-03, the lead's yes)

The agent drove Blockbench 5.2.2 on the lead's PC. All files were copies in `build/anim-research/bbtest/` (ignored by git). No artist file changed (no `.bbmodel` in `ProjectNublar-Assets` is newer than the test). [v]

- **Old plugin.** At start, 4.1.3 shows its own warning that it supports only Blockbench 4.12.0 to 5.0.0. Its plugin page says "Plugin is outdated and does not work with this version of Blockbench". [v]
- **Plugin swap.** The agent uninstalled "GeckoLib Animation Utils" 4.1.3 and installed "GeckoLib Models & Animations" 4.2.5 from the plugin store. [v] The lead's Blockbench now has the new plugin.
- **Import of the mod's clips.** Animation > Import Animations read the mod's `gallimimus.animation.json` (with its `post: {vector}` keys) and listed all 16 clips. [v] So the new plugin opens the mod's clip files.
- **Round trip.** The agent exported the 10 clips that only the mod file has (`move.walk`, `attack2`, `calling`, `misc.idle`, `rest`, `restidle`, `sentry`, `flinch`, `startled`, `drink`: 4,636 keys). Every key time, value, curve type (`lerp_mode`), clip length and loop flag matches the original, and keyed bones that the model lacks stay in the file. [v] (script comparison) Clips with the same name in the artist file were left out, because the export list merges same-name clips.
- **IK bake.** The agent added one null object to a copy (target `footRight`, root `legUpperRight`, three position keys) on the shipped `move.walk`. "Bake Inverse Kinematics" shows in the Animation menu once a null object exists. [v] After the bake and an export:
  - the three chain bones (`legUpperRight`, `legMiddleRight`, `legLowerRight`) have 46 rotation keys each, at 24 per second; [v]
  - the null object and its keys are gone from the export; the root's own position keys stay; the target bone (`footRight`) keeps its own 15 keys; the left leg does not change; [v]
  - the baked loop does not close: `legLowerRight` starts at 62.6° and ends at 1.5°, and the first step is about 22°; [v]
  - the knee flips 10° to 30° between frames at several times. [v]
  - Probable cause: no IK pole and a test path for the null that does not loop. [inf] Rule for real use: set an IK pole (5.2.0 and later), key the null so that the loop closes, and run the clip checker on the baked clip. [inf]

## Not done

- A load of a baked clip in game.
- A real foot-plant fix with IK. That is the Gallimimus Art list row.

## Verdicts

- Blockbench IK with "Bake Inverse Kinematics" on 5.2.2: **use it** for new or fixed walk and run clips, with an IK pole and a closed null path, and check the baked clip. Tested on a copy.
- Export with live (unbaked) IK: **drop**. It overwrites other keys on the same bones.
- GeckoLib plugin 4.1.3 on Blockbench 5.2.2: **replaced** with the `geckolib` plugin 4.2.5 on 2026-11-03 (the lead's yes). The round trip of the mod's clips is exact.
- mcblend for IK: **keep** for clips that need Blender tools. Blockbench bake covers foot planting.
