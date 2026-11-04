# Animation resources and tools for Beta: springs, a clip checker, two traps in the F2.3 plan and a walk clip defect

Date: 2026-11-03. Status: research report for the project lead. It changes no code, no clip, no model and no plan module. Plan changes below are proposals.

The research checked ten resources for the F2.3 animation work. Most of them confirm the plan. Three findings change it:

- GeckoLib additive mode drops the model rotation of almost every head, neck, jaw and tail bone. So the D333 overlay controller cannot use it as planned.
- The breathing sway moves the Brachiosaurus front feet after the leg IK planted them. The move is small today and grows with the D338 panting.
- The shipped Gallimimus walk skids each foot forward at touch-down. A game speed change cannot stop this slide. The leg IK may hide part of it.

**Labels.** `[v]` means checked in this research: in the GeckoLib bytecode, in the mod's files, on copies of the models, or in source at a named commit. `[doc]` means a paper, page, video, changelog or README. `[inf]` means inference. Nobody ran these in game.

**Calibration rule (lead, 2026-11-03).** Speeds and timings from science and from other games are reference points only. Minecraft scale, model size, player speed and feel decide the game values. The lead tunes them in game. This report uses science for relative facts: which species is faster, stride per hip height, duty factor, footfall order and posture.

**Names.** "The plan" is `docs/plan/engineering-backlog.md`, F2.3 unless the row says otherwise. The layer numbers are the F2.3 table: 1 clips, 2 leg IK, 3 neck and tail bend, 4 head look, 5 neck reach, 6 tail lag, 7 breathing sway. The notes are in `research_notes/Animation resources and tools for Beta/`:

| Note | Item |
| --- | --- |
| [01_geckolib5_hooks.md](../research_notes/Animation%20resources%20and%20tools%20for%20Beta/01_geckolib5_hooks.md) | 1. GeckoLib 5 hooks |
| [01b_geckolib_upstream.md](../research_notes/Animation%20resources%20and%20tools%20for%20Beta/01b_geckolib_upstream.md) | 1b. GeckoLib upstream |
| [02_blockbench_ik.md](../research_notes/Animation%20resources%20and%20tools%20for%20Beta/02_blockbench_ik.md) | 2. Blockbench IK |
| [03_clip_checker_proof.md](../research_notes/Animation%20resources%20and%20tools%20for%20Beta/03_clip_checker_proof.md) | 3. Clip checker |
| [04_springs.md](../research_notes/Animation%20resources%20and%20tools%20for%20Beta/04_springs.md) | 4. Springs |
| [05_procedural_animation_talks.md](../research_notes/Animation%20resources%20and%20tools%20for%20Beta/05_procedural_animation_talks.md) | 5. Procedural animation talks |
| [06_movement_science.md](../research_notes/Animation%20resources%20and%20tools%20for%20Beta/06_movement_science.md) | 6. Movement science |
| [07_science_led_game_diaries.md](../research_notes/Animation%20resources%20and%20tools%20for%20Beta/07_science_led_game_diaries.md) | 7. Game diaries |
| [08_live_animal_reference.md](../research_notes/Animation%20resources%20and%20tools%20for%20Beta/08_live_animal_reference.md) | 8. Live-animal reference |
| [09_mowzies_mobs.md](../research_notes/Animation%20resources%20and%20tools%20for%20Beta/09_mowzies_mobs.md) | 9. Mowzie's Mobs |
| [10_other_finds.md](../research_notes/Animation%20resources%20and%20tools%20for%20Beta/10_other_finds.md) | 10. Other tools |

## Twelve changes, ranked

Each row is a proposal. "Plan" says which plan line or lead decision it changes. Effort: S is under 100 lines, M is one or two classes with tests.

| Rank | Change | Plan | Effort | In-game effect | Basis |
| --- | --- | --- | --- | --- | --- |
| 1 | Play the head overlay clips through Nublar's own bone updater, which adds the clip value as a delta. It needs its own clip clock and its own fade in and out. Do not use GeckoLib additive mode on bones with a model rotation | D333 (the intent stays, the mechanism changes), layer 1 "Overlay controller", the F2.2 rule "three controllers at most" | M | A Triceratops `headshake` over its walk turns the head from its normal pose. Without this, the keyed bones lose their model rotation while the clip plays `[inf]` | GeckoLib 5.5.5 and 5.5.6 bytecode `[v]`, the same rule on the newest branch `[v]` |
| 2 | Run the breathing sway before the leg IK, or leave `hips` out of the Brachiosaurus sway | Layer order table | S | A resting Brachiosaurus keeps its front feet still on the ground, also when it pants | Front legs hang from `hips` in the Brachiosaurus only `[v]`. They move about 0.5 px at render scale 1 today `[v]` |
| 3 | Tail lag: an exact damped spring per tail bone, tuned by half-life and damping ratio. It steps on the tick and moves forward by the partial tick at render. Head first, body later, tail last | Layer 6 | M | The tail swings behind a turn and settles. A light tail overshoots a little. A heavy tail settles slowly, with no overshoot. No pop at a long frame | Holden springs `[doc]`, Horizon Zero Dawn talk `[doc]` |
| 4 | Head look: one critically damped spring on the total yaw and pitch, before the weight curve splits it. A double spring for heavy heads. The head leads the neck a little | Layer 4 | S | A T. rex turns its head slowly and smoothly. A Gallimimus snaps its head to a target and holds it | Holden `[doc]`, Saurian, Prehistoric Kingdom, Frontier `[doc]` |
| 5 | Breathing: keep the sine. A damper eases its rate and size, and a stored phase stops a jump | Layer 7, D338 | S | A tired animal starts to pant smoothly. The sway does not jump when stamina crosses a third | Holden `[doc]` |
| 6 | D327: use the exact spring formula for the screen springs | F1.4, D327 | S | A screen on a slow PC opens with no shake, even after a long first frame | Holden `[doc]`. The numbers assume a simple step `[inf]` |
| 7 | A clip checker that poses the mod's own model and shipped clips | Checks (D332) | M (1 to 2 days) | A skid, a sinking foot or a pop at the loop shows up before a tester sees the clip | Proof on the Gallimimus `[v]` |
| 8 | Gallimimus clip fixes as Art list rows. The `move.walk` touch-down skid and foot sink are artist work, unless the lead gives the row to an agent (D331). The `sentry` keys after its end are agent work, because they add no new motion | Art list | S each | A walking Gallimimus plants each foot with no forward slide. The `sentry` fix changes almost nothing that a player sees | Proof `[v]`, not seen in game |
| 9 | Effects at bone pivots: per species, pick the existing bone whose pivot is nearest the tip. Add the listener only on frames where an effect can start. Make an F2.2 exception for GeckoLib's own small objects | D339, F2.2 "no new object per frame" | S | The drink splash starts at the snout, not at the jaw hinge. A herd far from water costs nothing | Bytecode `[v]`, no model has a locator `[v]` |
| 10 | Leg IK guards: a bend clamp, a leg-stretch check and a stop rule (freeze the legs and pin the feet when the animal stops) | Layer 2, F2.1 part B | M | Legs never bend backward or stretch on a step. A stopping animal does not shuffle its feet | Our proposal `[inf]`, from the Prehistoric Kingdom IK polish lists and the Frey talk `[doc]` |
| 11 | Replace the GeckoLib Blockbench plugin 4.1.3 with `geckolib` 4.2.5, after a test on copies. Plant walk and run feet with Blockbench IK and "Bake Inverse Kinematics" | Clip tools | S | The artist fixes a sliding foot in Blockbench, without Blender | The lead's Blockbench is 5.2.2, and the plugin allows 5.0.0 at most `[v]`, Blockbench source `[v]` |
| 12 | Recording for PR clips: the OBS replay buffer, then ffmpeg to an MP4 under 10 MB | Acceptance checks | S | A tester saves the last 30 s with one key and posts it in the PR | GitHub limits `[doc]`, ffmpeg is on the lead's PC `[v]` |

Rank 1 stops a visible error that the plan would build. Rank 2 stops a small error that D338 makes larger. Rank 8 is a clip defect that a game speed cannot fix. Ranks 3 to 6 make weight readable at almost no cost. Rank 7 makes most of the D332 quality bar checkable.

Three new ideas need a lead call. They are not in the ranked list:

- No turning in place: the body turn rate falls as speed falls. So a big animal always steps while it turns (Frey, Horizon Zero Dawn `[doc]`). It touches pathing.
- A footfall spring on the body: a small kick at each foot contact, then a settle. It must run before the leg IK `[inf]` (Rosen `[doc]`).
- Controller bones: a clip keys an empty bone from 0 to 1, and that value scales a code layer. An example is head look off during a roar (Mowzie's Mobs `[v]`). It needs a new bone in each model and new keys in every clip that uses it.

## 1. GeckoLib 5 hooks

**In game:** effects start at the right body part, and code layers add to the clip pose without fighting it.

- `BonePositionListener#accept` gets `(worldPos, modelPos, localPos)`, in that order, on Fabric 5.5.5 and NeoForge 5.5.6. `[v]`
- The world position is absolute: the entity position plus the bone offset. It is null when the render state has no position. A dinosaur always has one. `[v]`
- The point is the bone pivot, not the tip of the cube. None of the six models has a locator. `[v]`
- The listener runs only in the bone render path. So an animal out of view probably makes no splash. `[inf]`
- GeckoLib makes about 10 small objects per listened bone per pass. Nublar's code cannot remove them. `[v]`
- Bone updaters get a snapshot whose rotation is a delta on the model rotation. Adding to it is the right way to write a layer. `IdleSway` in `DinosaurRenderer` already does this. `[v]`
- **Trap.** An additive controller computes the clip value plus (snapshot minus model rotation), and the renderer adds the model rotation again `[v]`. So the model rotation drops out `[inf]`. Bones with a model rotation whose name has head, neck, jaw, tail, snout or mouth: Brachiosaurus 19, Dilophosaurus 26, Gallimimus 25, Triceratops 14, T. rex 22, Velociraptor 23. `[v]`
- The same rule is on GeckoLib `26.2` (5.5.6) and `main` (5.5.7, Minecraft 26.3). No issue or changelog line names it. `[v]` An additive controller that fades out may also end at twice its value. Not tested. `[inf]`

**Verdict:** use the listener and delta updaters. Do not use additive mode as it is (rank 1).

## 2. Blockbench IK

**In game:** a walk or run clip whose planted feet do not slide.

- The lead's Blockbench is 5.2.2. The GeckoLib plugin is 4.1.3, which allows Blockbench 4.12.0 to 5.0.0 `[v]`. The plugin registry marks it deprecated `[doc]`. Its replacement `geckolib` 4.2.5 is not installed `[v]`.
- Blockbench IK uses null objects with a FABRIK solver. "Bake Inverse Kinematics" turns the IK into rotation keys. On 5.2.2 it also removes the null object's keys and allows for the bone's model rotation. `[v]`
- Exporting live IK writes samples at 24 per second and replaces every other key on the bone. So bake first. `[v]`
- No artist model uses IK today. `[v]`
- The new plugin fixes empty keyframe values on export and a frame-key reset on import `[doc]`. It may also fix the `post: {vector}` import failure. Not tested. `[inf]`

**Verdict:** use the bake on 5.2.2 after one test on a copy. Replace the plugin after a test (rank 11). The hands-on test waits for the lead's yes to launch Blockbench.

## 3. Clip checker

**In game:** a clip that skids, sinks or pops at the loop is caught before a tester sees it.

- The headless tool has four gates: bones, key times, ground sink and detachment. They passed a test clip with three planted defects: a 60° neck bend, a 60° leg swing and a loop that ends 25° off. `[v]`
- Proof scripts on the tool's own pose code caught the loop end and the leg swing. They run 6 clips in about 16 s. `[v]` They are in `Documents\Minecraft Modding\Animation Work\clip_check_proof\`.
- Neither the gates nor the scripts caught the 60° neck bend. So the plan line "The headless Blockbench `detachment` check runs at the extreme frames" does not check seams. The bend-budget table must do it. `[v]` `[inf]`
- The artist's Gallimimus file and the mod differ. Their walk and run lengths differ, and the mod model has a `root` bone that six clips key. So the checker must pose the mod's model. `[v]`
- Defects in the shipped Gallimimus clips `[v]`, not seen in game:
  - `move.walk`: each foot lands while it still swings forward, then slides about 5 to 7.5 px forward on the ground. A planted foot that moves forward slides at any game speed. The leg IK may hide part of it. `[inf]`
  - `move.walk` and `run`: a foot sinks about 1 px into the ground. `flinch`: 0.7 px.
  - `sentry`: 11 bones have a key at 4.75 s in a 4.74 s clip.
  - `run` ends 0.8 px off its first pose on `footRight`.
- The scripts' oriented-box overlap test flags 1.2 to 2.8 px in normal clips. One render showed nothing wrong at a flagged arm. The largest flag (a leg cube into the belly at 2.83 px) has no render. The clip kit's Blender rule (a corner out through a face) is better and ports to the script. `[v]` `[inf]`
- Limits: the tool samples Bezier keys as linear and skips Molang keys. Its Catmull-Rom is Blockbench's, which may differ a little from GeckoLib's. Bun runs only from the npx cache today. `[doc]` `[inf]`
- One MCP pose call costs about 7,000 tokens, so the checker must run as a script. `[v]`
- Another way: the mod already evaluates clips with GeckoLib 5.5.6's own code for the strides (`MovementGait.strides`). A JUnit check next to `DinosaurAnimationFilesTest` could use that exact evaluation, with no Bun. `[v]` `[inf]`

**Verdict:** build the checker (rank 7) after the lead picks the way. File the Gallimimus fixes (rank 8).

## 4. Springs

**In game:** tails, heads and breathing move with weight and never shake on a slow PC.

- Holden's exact spring forms are stable for any time step. A fixed 50 ms tick step is a constant 2 × 2 matrix. It costs about 8 operations per bone, with no `exp` call. `[doc]` `[inf]`
- Tail lag: a second-order spring per bone, with half-life and damping ratio. Heavy species get a damping ratio of 1 or more and a long half-life. The lead sets the values in game. `[doc]` `[inf]`
- Head look: a critical spring on total yaw and pitch, per frame. A double spring for heavy heads. `[doc]`
- Breathing: a sine with a damped rate and size and a stored phase. `[doc]`
- D327: the exact form is the safe choice. The 50 ms cap with 8 ms steps works, but it drifts 3 to 9 % from the tuned motion. `[inf]` The screen code does not exist yet, so these numbers assume a simple step.

**Verdict:** use for layers 4, 6 and 7 (ranks 3 to 6).

## 5. Procedural animation talks

**In game:** a big animal looks heavy, and stops without shuffling.

- Rosen, Overgrowth (GDC 2014): the gait follows ground distance, and each bone has its own softness. Use. `[doc]`
- Frey, quadrupeds (GDC 2016): gate the gait by speed. Freeze and pin the feet at a stop. The idle moves only bones above the legs. Use. `[doc]`
- Oud, Horizon Zero Dawn machines (GDC 2018): the body follows the head 6 frames later, the tail 12 frames later. Use. `[doc]`
- Skovbo Johansen (GDC 2009): a reference to audit the CHAOS foot planner. `[doc]`
- IK Rig (Ubisoft), Frontier (Jurassic World Evolution), Rain World, Spore: reference or drop. `[doc]`

**Verdict:** confirms layers 2, 4 and 6. Adds rank 10 and the three lead calls.

## 6. Movement science

**In game:** each species moves like its body. The proposed speed order puts the sauropod slowest and the ostrich-like Gallimimus fastest. Per-species speeds stay open in the plan.

Relative facts. All values are reference points, not game values. The speed order is an inference from the note `[inf]`:

| Species | Gait | Stride per hip height | Feet on the ground | Proposed speed order |
| --- | --- | --- | --- | --- |
| Tyrannosaurus | Walk and fast walk. A run only in models at the bone-stress limit | 1.2 to 1.5 | Over half the cycle at a walk | Slow for its size |
| Velociraptor | Biped, two-toed print. Small and quick | no data | no data | Quick |
| Dilophosaurus | Biped. Trackmaker unproven | no data | no data | Middle |
| Gallimimus | Ostrich-like. A grounded run, then a run with a flight phase | no data | Over half at a grounded run | Fastest |
| Triceratops | Quadruped. Scientists dispute the forelimb posture (elbows out 25 to 32°, or upright) | no data | no data | Slow. Scientists dispute a fast gait |
| Brachiosaurus | Quadruped, walk only. Shoulder higher than hip | no data | Over three quarters | Slowest |

Sources: Hutchinson, Sellers, van Bijlert, Lallensack and others, 36 in all `[doc]`. The note gives the formulas that turn hip height into stride and cycle time. They give starting values only.

Other facts for the layers:

- The T. rex tail acts as a vertical spring at about 0.66 Hz. A tail-lag reference. `[doc]`
- The Velociraptor tail is stiff up and down and bends sideways. `[doc]`

**Verdict:** reference for clip timing and the speed order. Game speeds stay a lead call in game.

## 7. Game diaries

**In game:** the same lessons that other dinosaur games learned the hard way.

- Prehistoric Kingdom and Saurian tie speed to the clip stride. Saurian checked stride against trackways. `[doc]`
- Prehistoric Kingdom's walk slid without foot IK, and in 2024 the team still listed foot IK polish so that limbs break less `[doc]`. The bend clamp and the stretch check of rank 10 are our proposal `[inf]`.
- Frontier: quadruped legs must not fold too far, long necks keep the head level, and bipeds lean into slopes with little roll. Body height follows the foot that carries the weight. `[doc]`
- Prehistoric Kingdom's head look took months of work, tested on one species first. Bird-like species snap and hold (Saurian). `[doc]`
- Saurian found its first tails too stiff. So start tail lag large, with a speed term. `[doc]`
- Prehistoric Kingdom fixed a sauropod turn snap in 2026 with a bank (a lean into the turn) `[doc]`. Turn lean waits until after Beta (D337). Check the blends from idle to walk and from walk to run `[inf]`.
- No team publishes its stride or gait numbers. `[doc]`

**Verdict:** use Prehistoric Kingdom, Saurian, Frontier and Prehistoric Planet. Drop The Isle and Beasts of Bermuda.

## 8. Live-animal reference

**In game:** clip reference that an artist or agent can watch, per species.

- Elephant (Brachiosaurus, Triceratops): three feet down at a walk, two on average at speed. No flight phase. The back stays level in the first half of a stride and dips in the second half. `[doc]`
- Giraffe (Brachiosaurus neck): the neck nods twice per stride, about 10° at 1 m/s. The fit is weak, and the agent read only a summary of the source. The head goes down to drink. `[doc]`
- Ostrich (Gallimimus, raptors): the head stays at nearly one height while the body moves. Knee and ankle axes turn outward. `[doc]`
- Cassowary and Komodo dragon (T. rex, Dilophosaurus): the cassowary stays grounded. The Komodo tail waves at a walk and lifts at a run. `[doc]`
- Muybridge plates 732 to 735 (elephant), 772 and 773 (ostrich), public domain. `[doc]`
- The agent watched four short public videos. The note gives their links and times. `[doc]`

**Verdict:** use for clip reference and for the head-hold rule of layer 4.

## 9. Mowzie's Mobs

**In game:** ideas for code-driven bodies, not code.

- The public repo is `BobMowzie/MowziesMobs-Public` on GeckoLib 4.8.3, Minecraft 1.21.1. It has no GeckoLib 5 branch. `[v]`
- It has no IK. Its leg solver moves a leg height at a fixed rate. `[v]`
- Its tail physics rebuild bones from world positions. That needs GeckoLib 4 bone access and does not port. It makes about 1,000 small objects per 6-bone tail per frame. `[inf]`
- Three ideas are worth taking `[v]`:
  - controller bones (a clip-keyed 0-to-1 weight for a code layer)
  - a stiffness fall-off along a chain spring
  - locator bones for effects.

**Verdict:** reference. Take the three ideas, clean-room.

## 10. Other tools

**In game:** faster clip work and short clips of the result.

- The note rated plugins for Blockbench 4.12. The lead runs 5.2.2. Nobody tested the plugin picks on that version. `[v]` `[inf]`
- Plugins to use after a test `[doc]`:
  - Animated Platforms: a ground that scrolls at a set speed. A planted foot must stand still on it.
  - Animation Sliders: delta clips.
  - Expand Bone Timeline.
- IK Tools 2.6.1 (two-bone IK, FABRIK, bake a frame range) needs Blockbench 5.1 or later. It can run on 5.2.2, next to the new GeckoLib plugin. A candidate for rank 11. `[doc]` `[inf]`
- No plugin checks foot slide or loop ends. `[inf]`
- Built-in Blockbench tools: onion skin, graph editor, motion trail, Flip Animation, Optimize Animation. `[doc]`
- Other mods (reference) `[v]`:
  - Alex's Caves and Citadel: a phase-shifted sine along a chain, and a foot lift on only the lifting half of the wave.
  - Ice and Fire: a tail lag buffer that decays.
- Recording: the OBS replay buffer and ffmpeg. Flashback and ReplayMod have 26.2 builds, but for Fabric only, and testers use NeoForge. `[doc]`

## Open questions for the lead

1. Overlay clips (D333): pick one way. (a) Nublar's own delta bone updater (proposed). (b) Add the model rotation back after GeckoLib's additive step. It does not fix the fade-out case. (c) Move the model rotation of each keyed bone to a parent group in the model.
2. Brachiosaurus: breathing sway before the leg IK, or no `hips` sway?
3. D327: the exact spring formula (proposed), or the 50 ms cap with 8 ms steps?
4. D339 and F2.2: may GeckoLib's own small objects for bone positions count as an exception to "no new object per frame"?
5. May the agent launch Blockbench to test the IK bake and the new GeckoLib plugin on copies?
6. The clip checker: a Bun script in `tools/animation/` on the headless pose code, or a JUnit check on GeckoLib's own evaluation? Build it now?
7. File the Gallimimus clip fixes on the Art list (the skid and sink for the artist, the `sentry` keys for an agent)?
8. The three new ideas: no turning in place, a footfall spring and controller bones.

## Answers (lead, 2026-11-03)

1. Overlay clips: Nublar's own delta bone updater (D341).
2. Brachiosaurus: breathing sway before the leg IK, for every species (D342).
3. D327: the exact spring formula.
4. An F2.2 exception for GeckoLib's own objects, with the listener on only when an effect can start (D343).
5. The agent ran the Blockbench test. The GeckoLib plugin is now 4.2.5, the round trip of the mod's clips is exact, and the IK bake works (note 02).
6. The clip check lives in the mod's unit tests, on GeckoLib's own evaluation, and is built with F2.3 (D344).
7. The Gallimimus walk, run and sentry fixes go to an agent (D345).
8. No turning on the spot for every species (D346). A light footfall bounce for every species (D347). Control bones (D348).

## Not done

- Nothing ran in game. The clip defects come from the headless pose code.
- No hands-on Blockbench test. The live Blockbench MCP did not connect, and the handoff asks for the lead's yes first.
- The spring numbers for D327 assume a simple step, because the screen code does not exist yet.
- The agents read some science sources only as abstracts. The notes mark them.
