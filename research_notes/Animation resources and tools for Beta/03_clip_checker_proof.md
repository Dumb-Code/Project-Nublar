# Clip checker proof: the headless Blockbench tools against the D332 quality bar

Research note for the animation resources report. Written 2026-11-03.

Scope: item 3 of the handoff. Can the `blockbench-headless` MCP (v1.10.1) check every clip against the D332 quality bar automatically? A small proof on copies of one species, the Gallimimus, and the cost of a full run. The full tool is not built.

Labels:

- `[v]`: measured in this session on the copies named below, with the tool or the proof scripts.
- `[doc]`: from the tool's own descriptions or its source.
- `[inf]`: an inference.

## Files used

All work ran on copies. No file in `ProjectNublar-Assets` or in the mod changed.

- `build/anim-research/gallimimus_copy.bbmodel`: a copy of `ProjectNublar-Assets/assets/dinosaurs/gallimimus/models/gallimimus_adult.bbmodel`, with a `root` group added (pivot 0, 32, 2, the parent of `hips` and both upper legs, as in the mod's `gallimimus.geo.json`). `build/` is ignored by git.
- `build/anim-research/gallimimus_selftest.bbmodel`: the copy plus one clip with three planted defects.
- `build/anim-research/gallimimus_shipped.bbmodel`: the copy with its clips replaced by the 16 clips of the mod's `gallimimus.animation.json`.
- Proof scripts (Bun, TypeScript, 239 lines): `Documents\Minecraft Modding\Animation Work\clip_check_proof\`. They import the headless tool's own pose and bounds code from the npx cache (`blockbench-mcp` 1.10.1), so they pose the model exactly as the tool does.

## Facts about the inputs

- **The artist file and the mod differ.** The artist `.bbmodel` walk is 1.33 s long. The mod's `move.walk` is 1.875 s. The run is 0.7 s against 0.79 s. [v] A check on the artist file does not check what ships. [inf]
- **The mod model has one extra bone.** `gallimimus.geo.json` has a `root` bone that the artist file lacks. Every other bone name matches. [v] Six shipped clips key `root` (`move.walk`, `run`, `attack2`, `sentry`, `sniffair`, `drink`). [v] Without `root`, the walk's foot sink reads 2.61 px. With it, 1.03 px. [v] So a checker must pose the mod's model, not the artist's.
- **Importer sign rule.** A GeckoLib clip goes into a `.bbmodel` with rotation x and y negated and position x negated. With this rule, the shipped `sniffair` and the artist `sniffair` give the same pose at six sample times (largest difference 0.00). Every other sign choice differs by 61° or more. [v] (`compare_signs.ts`)
- **Keyed bones the model lacks.** `misc.idle`, `rest`, `restidle`, `flinch` and `startled` key 18 bones that the Gallimimus model does not have (`bicepLeft`, `ankleRight`, `throat7` and others). [v] The plan's unit test already covers this ("every keyed bone exists in the model").
- **Molang keys are skipped.** The tool skips channels whose keys are Molang expressions. `misc.idle`, `rest` and `restidle` lose 6 channels each. [v] [doc]

## What the tool checks today

`bbmodel_validate_animations` has four gates: bones exist, key timing, ground sink, detachment. [doc] `bbmodel_validate` has a rest-pose interpenetration gate, on axis-aligned boxes. [doc]

| D332 rule | Tool gate | Result in the proof |
| --- | --- | --- |
| No hole or seam beyond rest | `detachment` (a group moves away from the parent it touches) | Missed a 60° `neck1` bend in the self-test. [v] The proof scripts also have no seam check. Needs the bend-budget table instead. [inf] The plan relies on this gate ("The headless Blockbench `detachment` check runs at the extreme frames", F2.3 Checks). |
| No part through the body | none during a clip | Not checked. |
| No flicker | none | Not checked. |
| Planted feet do not slide | `ground_sink` covers sinking only | Caught the walk sink (below). Does not see sliding. [v] |
| A loop ends on its first pose | none | Missed a 25° loop-end mismatch in the self-test. [v] |
| Keyed bones exist | `animated_bones_exist` | Works. [v] |
| No joint past its bend budget | none | Needs the table. |
| Same in Blender, Blockbench and game | none | Out of scope for the tool. |

The planted-defect clip (`selftest`: an upper leg swung 60°, `neck1` bent 60°, `tail1` ending 25° off its start) passed all four gates. [v] So the built-in gates alone cannot carry the quality bar.

## What the proof scripts add

The scripts sample each clip at 60 Hz with the tool's pose code. [v]

- **Loop end.** The largest channel difference between time 0 and the clip length. It caught the 25° self-test defect. [v] Shipped Gallimimus: `run` ends 0.81 px off on `footRight`, `attack2` 0.75 px; the others 0. [v]
- **Foot contact track.** For each foot, the lowest point of its cubes and that point's front-to-back position. From this the script finds the planted part of the cycle, the belt speed (how fast a planted foot moves back), forward motion while planted (a skid) and sideways drift. It caught the self-test leg swing as 8 px of sideways drift. [v]
- **Parts through the body.** An oriented-box overlap test (separating axes on the 8 world corners) over all 1,902 cube pairs that are apart at rest and do not share a parent. It caught the self-test leg crossing the other leg (4.4 px). [v] It also flags 1.2 to 2.8 px in normal clips, mostly an arm cube into the hip cube. [v] A render of the `run` frame at 0.35 s shows nothing wrong at the arm. [v] So the arm flags are probably hidden overlaps inside the body, not visible poke-through. [inf] The largest `run` flag, `legMiddleRight_cube` into `belly2_cube` (2.83 px at 0.38 s), has no render. It is unchecked. The kit's Blender `clipcheck.py` uses a better rule: a corner that comes out through a face, inside that face's outline, minus the face the part joins through at rest. [v] (script header) Port that rule, and the false flags go. [inf]

## Findings on the shipped Gallimimus clips

These are defects by the D332 bar, found by the proof. Nobody checked them in game.

- **The walk skids at each touch-down.** In `move.walk`, each foot reaches the ground while it still swings forward, then slides forward on the ground, within 1 px of the ground: the right foot from 0.75 to 1.0 s, the left foot from 1.69 to 1.875 s. [v] (`dump_track.ts`, 16 samples per second) The size depends on the point measured: about 7.5 px (right) and 7.8 px (left) at the front edge of the lowest cube, about 5.4 px and 5.1 px at its back edge. The proof checker's contact point gives 5.40 px for the right foot. [v] (review re-run) So the skid is about 5 to 7.5 px. A planted foot that moves forward slides in game at every body speed. [inf] This is a clip defect, so a game speed change (D292) cannot fix it. [inf] The leg IK (layer 2) may hide part of it. [inf]
- **The walk and run feet sink.** `move.walk`: `footRight` 1.03 px under the ground at 0.79 s. `run`: 1.17 px at 0.59 s. `flinch`: 0.71 px. [v] (`ground_sink`)
- **The planted speed is not constant.** During the walk stance, the left foot moves back at 8 to 33 px/s. A constant game speed leaves up to about 1 px of slide. [v] [inf]
- **`sentry` has keys after its end.** 11 bones have a key at 4.75 s in a 4.74 s clip. [v] (`keyframe_timing`)
- **Expected flags.** `swim` puts a foot 6.7 px under the ground, and `drink` puts the upper jaw 3.1 px under it. Both are correct for water. [inf] A checker needs a per-clip "ground does not apply" list. [inf]

The repo already has measured strides per species (`MovementGait.strides`, lines 109 to 131: "the foot contact point followed through one cycle of GeckoLib 5.5.6's own evaluation"). [v] The belt speed in the proof repeats that work. The new parts are the skid, sink, loop and poke-through checks. [inf]

## Cost of a full run

- **Time.** The proof scripts took 15.6 s for 6 shipped Gallimimus clips, and 18.4 s for 6 artist clips including the 7.9 s `sniffair` at 474 samples. [v] The pair test dominates: 66 cubes give 1,902 pairs. The other species have 91 to 112 bones, so about 2 to 3 times the pairs. [v] (bone counts) [inf] (scaling) Estimate for all six species and all clips: 5 to 10 minutes on the lead's PC. This is a developer tool, so player hardware does not apply. [inf] At 30 Hz the time halves. [inf]
- **Tokens.** One `bbmodel_sample_pose` call returns about 7,000 tokens for 70 bones. [v] A clip at 20 samples per second through the MCP alone would cost 100,000 tokens or more per clip. [inf] So the checker must run as a script, and only its summary goes to the agent. [inf]
- **Work to build the full tool.** [inf]
  1. Build the `.bbmodel` from the mod's `.geo.json` (about 100 lines), so the check poses what ships.
  2. The clip importer (done in the proof, 40 lines).
  3. Contact checks on the rest-pose ground: sink, skid, belt speed (rework of the proof: the proof's stance window uses the clip's own lowest point, which splits the touch-down from the stance).
  4. Poke-through with the face-outline rule from `clipcheck.py`.
  5. Flicker: a sign change of a bone's angular speed between the planned beats. It needs the beat list per clip.
  6. Bend budgets: read the per-species table when it exists.
  7. A per-clip exception list (swim, drink, lie-down).
  About one to two days of agent work. It belongs in `tools/animation/` with the clip kit (F2.3 Checks).
- **Limits.** Bun runs only from the npx cache path today. A pinned Bun install or a `package.json` in `tools/animation/` fixes that. [inf] The tool samples Bezier keys as linear and skips Molang. [doc] Its Catmull-Rom is Blockbench's. GeckoLib's own Catmull-Rom may differ slightly. [inf] The proof does not compare against a game frame.

## Verdicts

- `bbmodel_validate_animations`: **use it** in the checker for bones, key timing and ground sink. It is not enough alone.
- `bbmodel_sample_pose` through the MCP: **reference** only. Too many tokens per clip. Use the same code from a script.
- `bbmodel_validate` interpenetration (rest pose, axis-aligned boxes): **drop** for clip checks.
- The proof scripts: **use them** as the start of the `tools/animation/` checker, after the lead says yes (D332 checks).
- Shipped Gallimimus `move.walk`: the touch-down skid and sink are new defects for the Art list, for the lead to confirm in game.
