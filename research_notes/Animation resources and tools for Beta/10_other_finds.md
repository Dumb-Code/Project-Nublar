# Item 10: Other finds (Blockbench tools, other mods, recording)

Date: 2026-11-03

## Scope

- Blockbench plugins that help F2.3 clips.
- Blockbench built-in IK: does it bake to keyframes? Does the GeckoLib plugin export IK-driven rotations?
- Other Minecraft mods with a technique we can use in layers 2 to 7.
- Tools for the short PR clips.
- Public sources only. No game files read. No code copied.

## Labels

- `[v]` checked in source at a named commit, with path and lines.
- `[doc]` from a README, changelog, store page or release note (with link).
- `[inf]` my inference. Not run in Blockbench or in game.

## Bottom line

- Blockbench has IK built in (null objects) and a "Bake Inverse Kinematics" action since v4.9.0. [doc] [v]
- The GeckoLib plugin 4.1.3 does not touch IK. The IK output goes through Blockbench's own export. [v]
- If you export without baking, Blockbench samples the IK at 24 per second and writes plain rotation keys. It also replaces all other keys on that bone. Bake first. [v]
- The GeckoLib plugin 4.1.3 allows Blockbench 4.12.0 up to 5.0.0 only. The plugin list marks it deprecated. [v] [doc] Several good tools need Blockbench 5.1 or later.
- Best plugins for us on Blockbench 4.12: Animated Platforms (foot-slide check) and Animation Sliders (delta clips). [inf]
- No plugin in the registry checks foot slide or loop ends. [inf]
- Other mods give two small ideas: a phase-lagged sine along the neck/tail chain, and the `min(0, wave)` foot lift. Nothing new for IK.
- Recording: OBS Studio replay buffer, then ffmpeg (already installed on this PC). Flashback and ReplayMod exist for 26.2 but only for Fabric.

## A. Context: which Blockbench does the plugin allow?

- "GeckoLib 4.1.3" is the plugin `animation_utils`, titled "GeckoLib Animation Utils". [v] `plugins/animation_utils/animation_utils.js:6688` (embedded `package.json`: version 4.1.3)
  - Registry commit: `fa29c5a69ef599481925faac1038be4a79369382` of `JannisX11/blockbench-plugins`, 2026-11-03.
  - Allowed range: `min_version` 4.12.0, `max_version` 5.0.0. [v] same line
  - The plugin's own note says it keeps working on Blockbench 4 and below, and Blockbench 5 users should switch. [v] same line
  - The registry tags it "Deprecated". [doc] https://github.com/JannisX11/blockbench-plugins/blob/master/plugins.json
- The replacement is `geckolib` ("GeckoLib Models & Animations") 4.2.5, `min_version` 5.0.0. [doc] same registry
- I do not know which Blockbench version you run. [inf] If it is 4.12.x, every plugin below with `min_version` 5.x is out.
  - Checked later by the main session: the lead's PC runs Blockbench 5.2.2 (`Blockbench.exe` product version 5.2.2.0), with `animation_utils.js` 4.1.3 installed (`min_version` 4.12.0, `max_version` 5.0.0). The new `geckolib` plugin is not installed. [v] So the 4.1.3 plugin runs outside its declared range, and the "4.12" column below does not apply. Plugins with `min_version` 5.x can run on this Blockbench, but next to the new GeckoLib plugin, not the old one. [inf]
- Registry size: 151 plugins. I filtered by animation, IK, bake, onion, mirror, graph, curve, timeline, loop, foot, record and similar words. 39 hits. I read all descriptions and opened the source or `about.md` of 9. [v]
- I did not fetch https://www.blockbench.net/plugins. It lists the same registry. [inf]

How the 4.1.3 plugin touches animation export [v] (`animation_utils.js`):

- It patches `Animator.buildFile` and `Animator.loadFile`. Lines 7829-7830, 8145-8185.
- It patches `Keyframe.getLerp` and `Keyframe.compileBedrockKeyframe`. Lines 8215-8216.
- It handles bezier keys and the `geckolib_format_version` field. Lines 8145-8185.
- The bundle (8,867 lines) has no mention of IK, null objects or `sampleIK`. (grep)

## B. Blockbench built-in IK, bake and export

Source: `JannisX11/blockbench` `master` = `ec0b2827104d0799c153bfb5f867579c0c44652f`, 2026-10-18, version 5.2.2 (`package.json` line 5). Also tag `v4.12.5` (2025-06-17).

### How IK works

- A null object has three properties: IK target, IK root (source) and IK pole. [v] `js/outliner/types/null_object.js:120, 145, 170` and a "lock target rotation" flag at line 196
- A FABRIK solver moves the chain to the target. [v] `js/animations/fabrik.ts:1-104`
- The solve runs in the animator. [v] `js/animations/timeline_animators.js:856-923`
- Since v5.2.1, IK is off when the null object has no keyframes. So IK can be on in one animation and off in another. [doc] v5.2.1 release notes
- IK poles need v5.2.0 or later. [doc] https://github.com/JannisX11/blockbench/releases/tag/v5.2.0

### Bake action

- Name: "Bake Inverse Kinematics". Description: bake the rotations applied by IK into the selected animation. [v] `lang/en.json:2273-2274`
- Added in v4.9.0 (2023-12-02): "Added the option to bake IK animations". [doc] https://github.com/JannisX11/blockbench/releases/tag/v4.9.0
- Menu: Animation menu. [v] `js/interface/menu_bar.ts:442`
- On `master`: `js/animations/animation.js:1060-1119`. Steps:
  1. Sample the IK at the animation's snapping rate (`sampleIK`, lines 165-204). [v]
  2. Remove the keyframes of the IK null objects. IK is then off for the baked chain. [v] lines 1070-1082
  3. For bones with a model rotation, convert the sample so it is relative to that rotation. [v] lines 1088-1098
  4. Round to 4 decimals. [v] lines 1099-1101
  5. Write a rotation keyframe at each sample where the value changes. Skip samples equal to both neighbours. [v] lines 1103-1112
- On `v4.12.5`: `js/animations/animation.js:1222-1252`. Steps 1, 4 and 5 only. Steps 2 and 3 are missing. [v]
  - So on 4.12.x the IK keyframes stay after baking. Delete the null object's keyframes by hand. Otherwise the export samples the IK again (see below). [inf]
  - Without step 3, a bone with a model rotation may bake to a wrong angle. [inf] Test on one bone first.
- Later fixes: v5.1.2 (bake failed when a node had no IK support) and v5.2.1 (bake with an animated pole). [doc] release notes
- "Optimize Animation" (added v4.10.0) cuts keyframe count. Use it after baking. [doc] v4.10.0 notes; action at `js/animations/animation.js:1405`; menu `menu_bar.ts:440`

### What the export writes

Blockbench's own animation export (the Bedrock compile step, which the GeckoLib plugin calls through) does this:

- `v4.12.5`: `js/animations/animation.js:152-282`. The IK part is lines 262-276. [v]
- `master`: `js/formats/bedrock/bedrock_animation.js:528-544`. [v]
- Steps:
  1. Write the stored keyframes of every bone. [v]
  2. Call `sampleIK`. It samples from time 0 to the clip length. [v] `animation.js:165-204` (master)
  3. Write each sample as a plain `[x, y, z]` array at its time code. [v]
  4. The sample rate is the setting `animation_sample_rate`. Default 24 per second. [v] `v4.12.5` `js/interface/settings.js:626`
- Sampling happens only if some IK null object has position keyframes. [v] `v4.12.5` `animation.js:288`
- The IK write makes a fresh entry for the bone. It replaces every channel written before for that bone (position, scale, other rotation keys). [v] `v4.12.5` `animation.js:267`; `master` `bedrock_animation.js:533`
- The IK samples skip `Keyframe.compileBedrockKeyframe`. So the GeckoLib plugin's easing wrapper does not apply to them. [inf]

### Answers to your questions

- Does Blockbench have a bake action for IK? Yes. Since v4.9.0. [doc] [v]
- Does the GeckoLib plugin export IK-driven rotations or only stored keyframes? Both, through Blockbench: stored keyframes always, and IK samples at the sample rate if IK is live. The plugin adds no IK code. [v] [inf]
- Best practice: bake IK, check the keys, delete the IK null object keys (4.12.x: by hand), run "Optimize Animation", then export. [inf]
- Check: bone with model rotation, loop end, and foot slide after baking. [inf]

## C. Plugins

Compatibility column: "4.12" means it can run next to GeckoLib plugin 4.1.3 on Blockbench 4.12.x, going by `min_version`. [doc] registry.

| Plugin (version) | Min BB | What it does | With GeckoLib 4.1.3 | Verdict |
|---|---|---|---|---|
| Animated Platforms 1.0.0 | 4.8.0 | Scrolling ground plane. Speed in blocks per second. Presets saved per animation. [doc] `about.md` | 4.12: yes. View only; no export effect. [inf] | Use. Set the scroll speed to the clip's design speed. A planted foot must stand still on the texture. |
| Animation Sliders 0.5.0 | 4.8.0 | Sliders: tween, amplify, ease, retime, "Normalize Keyframes" (subtract the shown value from the selected keys to remove the base pose), ground-speed slider. [doc] `about.md` | 4.12: yes. Edits keys only. [inf] | Use for delta (overlay) clips. See the rotation issue in `01b_geckolib_upstream.md`. The ground-speed slider writes a Bedrock Molang field; GeckoLib use not checked. |
| Expand Bone Timeline 2.2.0 | 4.8.0 | Add or remove a bone and all child animators in the timeline. [doc] `about.md` | 4.12: yes. | Use for tails and necks. |
| Bakery 1.1.1 | 3.7.0 | Samples selected keys at (1 / snapping x rate) steps between the first and last key. Makes linear keys. Optional spherical interpolation for rotation. [v] `bakery.js:16-35, 52-58, 67-76, 106` | 4.12: yes. | Reference. Useful for smooth (catmull-rom) curves, which GeckoLib fixed in 5.5.6 (#895). [doc] Bezier has a plugin setting for baking already. [v] `animation_utils.js:8145-8170` |
| IK Tools 2.6.1 | 5.1.0 | Two-bone IK, FABRIK, spline IK, aim IK, IK/FK blend, bake current frame or a range. [doc] `about.md` | No. Needs BB 5.1+; 4.1.3 stops at 5.0. | Reference. Look again if we move to Blockbench 5 and the new GeckoLib plugin. |
| Easing Peasy 1.0.1 | 5.1.4 | 11 easing curves per key. [doc] | No (BB 5). May clash with the GeckoLib plugin's easing UI. [inf] | Drop. |
| Easings 1.0.0 | 4.8.0 | Easing for Bedrock Edition animations. [doc] | Unclear. Bedrock only. | Drop. |
| Bone View 1.0.0 | 4.12.6 | Show group pivots and links as bones. [doc] | 4.12.6 or later only. [inf] | Reference. Helps read tail and neck pivots. |
| Scene Recorder 2.1.1 | 5.0.0 | Record the viewport as GIF, MP4, MKV, MOV, WebM, WebP. [doc] `about.md` | No (BB 5). | Drop. We record in game. |
| Cameras 1.2.2 | 4.5.0 | Animated cameras in Blockbench. [doc] | Yes. | Drop. |
| Root Motion Extractor 1.1.0 | none | Bedrock root motion. [doc] | Bedrock only. | Drop. |
| Duplicate Bone Renamer 1.0.0 | none | Rename duplicate bones so they work for Bedrock and GeckoLib. [doc] | Yes. | Reference. Only if a model has duplicate names. |
| Batch Group Rename 1.3.0 | 4.8.0 | Batch rename with animation name sync. [doc] | Yes. | Reference. |
| GeckoLib Models & Animations 4.2.5 | 5.0.0 | The new GeckoLib plugin. [doc] | Replaces 4.1.3. | Reference. Needed if we move to BB 5. |
| AzureLib Animator 2.2.0, Animated Java 1.11.0, PAL Bend 0.4.0, CEM Template Loader 9.2.0, EMF Animation Addon 1.0.5 | various | Other formats or libraries. [doc] | Not for GeckoLib. | Drop. |

Not found: no registry plugin that checks foot slide, loop ends or pose seams. [inf] (keyword filter plus descriptions)

Pointer, not checked: this session lists headless tools named `bbmodel_validate_animations` and `bbmodel_sample_pose`. I did not read or run them. A loop-end check could compare the pose at time 0 and at clip length. [inf]

### Blockbench built-ins worth using

| Tool | Since | Use for | Works on 4.12 |
|---|---|---|---|
| Animation onion skin | v4.9.0 | Compare frames, check seams. [doc] release notes | Yes |
| Graph editor (with amplify tool in v4.7.0) | 4.x | Shape curves, spot a pop at the loop end. [doc] | Yes |
| Flip Animation | 4.4 or earlier | Copy left-leg keys to the right leg. [doc] v4.4.0 notes | Yes |
| Mirror Animating | v5.1.0 | Edit both sides at once. [doc] | No (BB 5) |
| Loop modes once / loop / hold | long-standing | Match GeckoLib loop types. [v] `js/animations/animation.js:26, 57` | Yes |
| Motion trail | long-standing | Foot path. A planted foot should trace a straight, ground-level line. [v] `js/animations/animation_mode.js:537` [inf] | Yes |
| Reverse keyframes | 4.x | [doc] `lang/en.json:2303` | Yes |
| Copy Animation Pose | v5.1.0 | Copy the current pose as keys. [doc] | No (BB 5) |
| Optimize Animation | v4.10.0 | Cut keys after bake. [doc] | Yes |

## D. Other mods

| Source | Technique for our layers | Verdict |
|---|---|---|
| Alex's Caves, Tremorsaurus | Phase-lagged sines; foot lift on the positive half wave; head look split over neck and head; leg solver. | Reference |
| Citadel (library under Alex's mods) | Same helpers; leg solver with range clamp. | Reference |
| Ice and Fire (Fabric port) | Tail lag by a stored yaw value that decays after a hold time. | Reference |
| Alex's Mobs | Uses the Citadel helpers. Not read. | Skip |
| Prehistoric Fauna (1.20.1) | Hard-coded pose lerps. | Drop |
| Fossils and Archeology Revival | Only a buggy port found. | Drop |
| L_Ender's Cataclysm, Born in Chaos | No public source found. | Skip |

### Alex's Caves, Tremorsaurus

- Repo `AlexModGuy/AlexsCaves`, `main` = `4718f4287c65b810aecb211789b45f54d405a94d`. File `src/main/java/com/github/alexmodguy/alexscaves/client/model/TremorsaurusModel.java` (463 lines). [v]
- It is not a GeckoLib model. It uses Citadel's `AdvancedEntityModel`. [v] (imports and base class)
- Techniques, in words:
  1. Reset to the default pose each frame, then add offsets. [v] lines 333-336
  2. Idle sway: neck, head and jaw get a slow sine (speed 0.1, size 0.03) with phase offsets 1, 2 and 3. Tail and tail tip get a swing with offsets −1 and −2. [v] lines 363-371
  3. Walk: each leg segment and the body get a sine of the walk phase times the walk amount, with its own phase. Tail bones get the walk wave at half the size and a phase that shifts per bone. [v] lines 376-395
  4. Foot lift: the foot height gets `min(0, wave)`. Only the lifting half of the wave moves the foot. It never pushes into the ground. [v] lines 396-397
  5. Head look: yaw and pitch split evenly over neck and head. [v] line 400
  6. Sit pose: a 0-to-1 progress value scales a target offset per bone. [v] lines 347-355
  7. Leg solver: two legs, forward offset −0.45, side offset ±0.75, range 1.0. Updated each tick. [v] `TremorsaurusEntity.java:69, 137`
  8. A rider seat is lowered by a smooth minimum of the two back-leg heights. [v] `TremorsaurusEntity.java:519-522`
- Maps to: layer 7 (idle phase lag along neck/head/jaw), layer 6 (phase-shifted tail), layer 2 (foot lift rule, pelvis drop).
- Cost: a few dozen float operations per frame. No allocation in the lines I read. [inf]
- Limit: driven by walk phase, not by turn rate. No spring.

### Citadel

- Repo `AlexModGuy/Citadel`, `master` = `e86ca4f446060713e1b600705e2a185a4571e4f6`. [v]
- `animation/LegSolver.java:18-106`: per-leg height; clamp to a range times scale (line 65); step 0.25 per tick up or down (lines 99-105); uses the closest point on the block collision shape (lines 85-98). [v]
  - Same design as Mowzie's `LegSolver`. Both credit an author named pau101 (Citadel spells it paul101). [v] Mowzie `LegSolver.java:3-5`; Citadel line 14
- `client/model/AdvancedEntityModel.java`: `faceTarget` (line 62), `chainSwing/chainWave/chainFlap` (82-116), `walk/swing/flap` (159-203), `progressPosition/Rotation` (240-262). [v]
- `chain*` helpers add a cosine to each bone in an array. The phase grows by a fixed step per bone index (step = root offset times pi over twice the bone count). The size is the swing amount times a degree. [v] lines 82-104, 123-129
- Verdict: reference. The phase-along-the-chain idea is the cheapest tail and neck wave.

### Ice and Fire

- Official source not found on GitHub. [inf]
- Fabric port `IAFEnvoy/IceAndFire-Fabric`, `HEAD` = `abbf3410107b722c5ecaabacc46717449c7c139c`, 2024-11-30. [v]
- File `src/main/java/com/iafenvoy/iceandfire/render/model/IFChainBuffer.java`. [v]
- Technique (tail lag by a decaying buffer), in steps:
  1. Each tick, if the body yaw changed, add the change (clamped) to a stored value. [v] lines 45-48
  2. When the stored value is above a step size, wait `bufferTime` ticks. [v] lines 49-56
  3. Then reduce the stored value by one step each tick until it reaches zero. [v] lines 49-56
  4. At render, lerp the previous and current stored value by partial tick. Spread it evenly over the chain. [inf] (the same class in Mowzie's repo does this at `ChainBuffer.java:135-139`)
- No spring, no overshoot. Fixed hold time, linear decay.
- Other files exist: `ModelDragonBase`, `DragonAnimationsLibrary`, `LegSolver`, `LightningTabulaDragonAnimator`. Not read.
- Maps to layer 6. Our damped spring covers the same job with overshoot and speed dependence.
- Verdict: reference.

### Prehistoric Fauna, Fossils, others

- Prehistoric Fauna: `superlord9362/Prehistoric-Fauna`, branch `1.20.1`, `2957329210aaed710855bc893d1e8bd49e21d4f0`, 2026-10-22. Vanilla `EntityModel` classes. Pose changes are `lerp` from one set of constants to another. The sitting breathing is a small `abs(sin)`. [v] `VelociraptorModel.java:84-130`. Drop.
- Fossils and Archeology Revival: the only repo is `BluSpring/FossilsArcheologyRevival`, MC 1.21.1, archived. Its description calls it a direct port of the 1.12.2 mod that is "very buggy". [doc] I did not read its animation code. Drop.
- Cataclysm and Born in Chaos: a GitHub search found only add-ons and compat packs. No source read. Skip.
- Alex's Mobs: public (`AlexModGuy/AlexsMobs`, branch `1.20`). Same Citadel helpers as Alex's Caves. Not read. [inf]

## E. Recording tools for short PR clips

Facts:

- GitHub accepts GIF up to 10 MB. It accepts MP4, MOV and WebM; the limit is 10 MB on a free plan and 100 MB on a paid plan, by repository owner. H.264 plays best. [doc] https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/attaching-files
- OBS Studio: latest release 33.0.1, 2026-10-29. [doc] https://github.com/obsproject/obs-studio/releases
- ShareX: latest release v21.0.0, 2026-07-03. [doc] https://github.com/ShareX/ShareX/releases
- ffmpeg 9.0.2 (Gyan full build, from WinGet) is already on this PC. [v] (`where ffmpeg`; `ffmpeg -version`)
- Flashback (Moulberry): newest 0.43.6, 2026-09-25, for 26.1 and 26.2 (the project also lists 26.3). Fabric only for every 26.x version. [doc] https://modrinth.com/mod/flashback
- ReplayMod: `26.2-2.6.28`, 2026-10-18. Fabric only for every 26.x version. [doc] https://modrinth.com/mod/replaymod
- Our tester status: NeoForge only until a Fabric client check. (project memory) So Flashback and ReplayMod do not fit yet.

Best low-effort setup [inf]:

1. In game: press F1 to hide the HUD. Use third person (F5) or a fixed spot.
2. OBS: one scene with a Window Capture (or Game Capture) of the Minecraft window.
3. OBS: turn on the Replay Buffer (about 30 s) and set a hotkey. After the animal does the move, press the key. No need to start and stop a recording.
4. Trim and shrink with ffmpeg. I ran these commands on a synthetic 1080p test clip. They run; sizes below are for the test pattern only.
   - MP4: `ffmpeg -ss 00:00:03 -i in.mkv -t 6 -vf "scale=-2:720,fps=30" -c:v libx264 -crf 24 -preset slow -pix_fmt yuv420p -an -movflags +faststart out.mp4`
   - GIF: `ffmpeg -ss 3 -t 5 -i in.mkv -vf "fps=15,scale=480:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=128[p];[b][p]paletteuse=dither=bayer:bayer_scale=3" out.gif`
   - WebM: `ffmpeg -ss 3 -i in.mkv -t 6 -vf "scale=-2:720,fps=30" -c:v libvpx-vp9 -crf 34 -b:v 0 -an out.webm`
5. Drag the file into the PR comment box. Keep it under 10 MB.

Notes:

- Do not use these clips to judge frame time. Recording adds encode load. [inf]
- OBS menu paths (replay buffer, capture type) are from memory and not checked. [inf]
- ShareX can also record a screen region to MP4 or GIF. [inf] OBS is better for a game window.
- Windows Game Bar (Win+Alt+R) is a zero-install fallback. [inf] Not tested.

## F. Side note: importing `post: {vector}` keys

- CLAUDE.md says Blockbench with GeckoLib plugin 4.1.3 cannot open `post: {vector}` keys.
- The 4.1.3 import function reads an object with a `vector` key, and `pre` and `post` keys that may each be a vector or an object holding one. [v] `animation_utils.js:7998-8013`
- So the failure may have another cause. Candidates: other keys next to `post` (such as easing or `lerp_mode`), or the Blockbench version. [inf] Not tested.

## Not checked

- Which Blockbench version you run.
- Any run of Blockbench, the plugins or the headless tools.
- The `blockbench.net/plugins` web page.
- OBS and ShareX UI paths.
- Animation code in Alex's Mobs, Cataclysm, Born in Chaos and Fossils.

## Verdicts

- Blockbench built-in IK and "Bake Inverse Kinematics" (v4.9.0+): use. Bake before export. Delete IK keys by hand on 4.12.x.
- Blockbench "Optimize Animation" (v4.10.0+): use after baking.
- GeckoLib plugin 4.1.3 (`animation_utils`): keep for BB 4.12.x. It does not change IK output. Plan the move to the new plugin.
- Animated Platforms: use (foot-slide check).
- Animation Sliders: use (delta clips; mind the rotation issue in 01b).
- Expand Bone Timeline: use.
- Bakery: reference.
- IK Tools: reference (needs Blockbench 5.1+).
- Easing Peasy, Easings, Scene Recorder, Cameras, Root Motion Extractor, AzureLib, Animated Java: drop.
- Bone View, Duplicate Bone Renamer, Batch Group Rename, GeckoLib Models & Animations 4.2.5: reference.
- Alex's Caves Tremorsaurus: reference (phase lag, foot lift rule).
- Citadel: reference (chain helpers, leg solver).
- Ice and Fire chain buffer: reference.
- Alex's Mobs: skip. Prehistoric Fauna: drop. Fossils and Archeology Revival: drop. Cataclysm, Born in Chaos: skip.
- OBS Studio with replay buffer plus ffmpeg: use (best low-effort setup).
- ShareX: reference. Windows Game Bar: reference.
- Flashback and ReplayMod: reference (Fabric only; wait for the Fabric client check).
