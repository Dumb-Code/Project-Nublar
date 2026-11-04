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

## Not done

- The hands-on test in Blockbench (a copy with a null object, bake, export through the plugin, load in game). The handoff says to ask the lead before launching Blockbench. The live `blockbench` MCP also failed to connect on 2026-11-03.
- A test of the new `geckolib` plugin 4.2.5 on the mod's clip files.

## Verdicts

- Blockbench IK with "Bake Inverse Kinematics" on 5.2.2: **use it** for new or fixed walk and run clips, after one test on a copy.
- Export with live (unbaked) IK: **drop**. It overwrites other keys on the same bones.
- GeckoLib plugin 4.1.3 on Blockbench 5.2.2: **replace** with the `geckolib` plugin 4.2.5, after a test on copies (lead's call; it is the artist's tool).
- mcblend for IK: **keep** for clips that need Blender tools. Blockbench bake covers foot planting.
