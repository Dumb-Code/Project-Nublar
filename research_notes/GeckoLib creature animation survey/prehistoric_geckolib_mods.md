# Prehistoric GeckoLib mods: how other dinosaur mods drive, layer and author their animations

Research note for the GeckoLib creature animation survey. Written 2026-11-03. Scope: dinosaur and prehistoric creature mods that animate with GeckoLib (no AzureLib dinosaur mod was found). It covers controller layout, speed matching, procedural bones, events and layering, babies and special states, file authoring and player reception, compared with Project Nublar (GeckoLib 4.7.3 on 1.20.1, planned GeckoLib 5 on 26.2). Three open-source mods were read in depth. Six closed-source jars were checked for their animation assets only. The GeckoLib 4 and 5 sources were read where a finding depends on library behaviour.

Labels: **[v]** verified in source or in a shipped jar asset at the stated commit or version, with path and lines. **[inf]** inference from the verified facts. **[doc]** from a README, store page or listing. Code is never pasted (clean-room rule); mechanisms are described in words and numbered steps. Most art assets in these mods are All Rights Reserved even when the code is MIT, so nothing here should be copied, only re-implemented from the description.

Keyframe density metric used below: for each clip, the number of keyframes summed over all bones and channels (rotation, position, scale), divided by the clip length in seconds. The same script was run on Nublar's files, so the numbers are comparable to each other, not to other surveys.

## Repository facts

| Mod | Licence (SPDX) | Branch / commit or version read | Minecraft | Loaders | GeckoLib |
|---|---|---|---|---|---|
| Fossils and Archeology: Revival (FA) | Code MIT; all other assets All Rights Reserved [v] LICENSE L1-29 | `1.20.1`, `b400cf2423dc245f3d18c8c60122835d4be5c6e2` (2026-06-07), mod 9.3.4.0 | 1.20.1 | Fabric, Forge (Architectury) | 4.7.2 [v] gradle.properties `gecko_lib_version` |
| Unusual Prehistory (UP, Peeko32213) | Code MIT; textures, geo, animations, music LicenseRef-All-Rights-Reserved [v] LICENSE L1-20 | `main-final`, `c49f49a5075fa66bb58668344c2c46e25ec68238` (2026-03-22) | 1.20.1 | Forge 47.1.3 | 4.7.1.2 [v] gradle.properties |
| Jurassic Revived (JRev) | CC-BY-ND-4.0, whole repo [v] LICENSE | `main`, `e043449a6491f3840b9afe0d1cdd216f21cde114` (2026-07-04) | 1.20.1 and 1.21.1 (Stonecutter) | Fabric, Forge (1.20.1), NeoForge (1.21.1) | 4.8.2 [v] versions/*/gradle.properties |
| Primal Frontier Legacy (PFL) | LicenseRef-All-Rights-Reserved on Modrinth; mods.toml says "Not specified" [doc][v] | jar 0.4.1-alpha (Modrinth `xkWLBh5U`), MCreator-generated | 1.20.1 | Forge | required, any version [v] mods.toml |
| Jurassic World Craft (JWC) | LicenseRef-All-Rights-Reserved on Modrinth; mods.toml AFL-3.0 (MCreator default) | jar 1.1.0 (`L599avEq`), MCreator | 1.20.1 | Forge | required [v] |
| Not Enough Dinosaurs (NED) | LicenseRef-All-Rights-Reserved | jar 1.3.0 (`SGWEXFA7`), MCreator | 1.20.1 | Forge | required [v] |
| Prehistoric Legacy, Jurassic Additions, Cenozoicraft | AFL-3.0 / MIT / MIT in mods.toml | jars 1.0.5, 0.2.0, 1.3.0, all MCreator | 1.20.1 | Forge | required [v] |
| GeckoLib 4 (reference) | MIT | `1.20.1`, `7d6f32db9168a24e14b327c0ad9f8f87dfc204de`, 4.8.4 | 1.20.1 | Fabric, Forge | n/a |
| GeckoLib 5 (reference) | MIT | `26.2`, `1e09655d3b54c2db959d234266fb564b90aa9382`, 5.5.6 | 26.2 | Fabric, NeoForge, Forge | n/a |

Checked and dropped (one line each):

- Unusual Prehistory 2 (platypushasnohat, `676f89bc`, NeoForge 1.21.1): uses the authors' own "Sinew" library, no GeckoLib [v] gradle.properties.
- Prehistoric Fauna (superlord9362, `1.20.1`, `29573292`): no GeckoLib dependency; vanilla-style Java models [v] build.gradle.
- Jurassic Reborn (Interactive-Modding, `1.20.1`, `5826778c`): GeckoLib 4.8.2 only for blocks (incubator, fences, rails); dinosaurs use its own `EntityAnimator` system [v]. Licence ARR.
- Jurassic World Reborn (gamma-02, 1.19.2): same split, GeckoLib only for blocks [v].
- JurassiCraft (JurassiCraftTeam, 1.12.2): LLibrary, no GeckoLib [v].
- Jurassic World Reborn II (DexxKnight1): a modpack, no code [v].
- Jurassic Saga: issue tracker only, source ARR and not public [v] README.
- Primal Frontier (Neo), Jurassic Returned, ShineaL's Prehistoric Expansion, NecroCrab "Prehistoric Dinosaurs": no GeckoLib in jar or repo [v].
- Paleocraft, Primal Paleo, Dinosaurs Expansion, Prehistoric Expansion: no GeckoLib build found; the Paleocraft repos are 2014-2017 [v] gh search.

Base URLs: FA = `https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2423dc245f3d18c8c60122835d4be5c6e2/common/src/main/java/com/github/teamfossilsarcheology/fossil/`; UP = `https://github.com/Peeko32213/Unusual-Prehistory/blob/c49f49a5075fa66bb58668344c2c46e25ec68238/src/main/java/com/peeko32213/unusualprehistory/`; JRev = `https://github.com/classic-mods-revived/Jurassic-Revived/blob/e043449a6491f3840b9afe0d1cdd216f21cde114/common/src/main/java/net/cmr/jurassicrevived/`; GL4 = `https://github.com/bernie-g/geckolib/blob/7d6f32db9168a24e14b327c0ad9f8f87dfc204de/`; GL5 = `https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/`.

## Key question 1: How many controllers, and what does each drive?

### Takeaway

Everyone uses two or three controllers. Base locomotion and idle sit on one. Attacks sit on a second. UP adds a third for "blend" gestures (roar, sniff, shake). The useful difference is how the layers avoid fighting. UP gives each joint a stack of separate bones, so each controller owns its own bones. FA patches GeckoLib so controllers add together instead of the last one winning. Nublar's two controllers share the same bones, so an action on `main` must replace the whole pose.

### Cited Findings

- FA registers two controllers per animal, "Movement/Idle" and "Attack", both with a 5-tick blend. It also registers every clip in the file as a triggerable animation on both. [v] FA `entity/prehistoric/base/Prehistoric.java` L1608-1622; `entity/animation/AnimationLogic.java` L31-33.
- FA's movement predicate checks states in this order: knocked out, sleeping, sitting, in water (swim, kept until back on ground), moving (walk or sprint), idle. Fliers, leapers, swimmers and fish have their own predicates. [v] FA `AnimationLogic.java` L210-248 (water), L325-387 (leaping), L389-424 (land), L457-516 (fish, flying).
- FA's attack controller plays only while a server-chosen attack animation is unfinished, then stops. During a heavy attack the movement controller returns STOP so it does not fight the attack. [v] FA `AnimationLogic.java` L187-193, L426-435.
- UP's T-Rex registers three controllers: "controller" (5-tick blend; swim, walk at fixed speeds, idle, knockout), "idleController" (0-tick blend; shake, sniff and roar gestures from synced booleans), and "attackController" (5-tick blend; bites, stomps, tail whip, charge, tackle, aggro, chosen by a synced integer state). [v] UP `common/entity/custom/prehistoric/TyrannosaurusEntity.java` L636-756.
- UP's T-Rex geometry stacks three bones on each major joint: a `_rotation` bone (code- or molang-driven), then an `_overlay` bone (gesture clips), then the visible bone (locomotion clips). Examples are body, tail1, tail2, neck and head. The neck also has a `neck_control` bone for head look. [v] UP asset `assets/unusualprehistory/geo/tyrannosaurus/tyrannosaurus_rex.geo.json` (hierarchy inspected, not copied).
- UP's "_blend" clips animate only `_overlay` bones. For example, `bite_blend1` touches 8 overlay bones, while `walk` touches 15 base bones. So a bite plays on top of a walk with no conflict. [v] UP asset `animations/tyrannosaurus/tyrannosaurus_rex.animation.json`.
- FA replaces GeckoLib 4's controller loop with its own. Steps: (1) each controller is processed as usual and fills its bone queues; (2) for each bone, the first controller's value is applied on top of the bone's rest pose; (3) each later controller's value is **added** to it (rotation and position) or **multiplied** into it (scale). The default GeckoLib pass is then cancelled for models that carry FA's `AdditiveAnimationModel` marker. [v] FA `compat/geckolib/AnimationProcessorOverride.java` L21-101; `mixin/AnimationProcessorMixin.java` L40-53; `client/model/PrehistoricGeoModel.java` L19.
- JRev uses a "MovementController" (swim, run, walk, idle) plus two trigger-only controllers, "attackController" and "mouthController". [v] JRev `entity/custom/TyrannosaurusRexEntity.java` L169-188.

### Inferences

- [inf] UP's bone stack is the cheapest way to get true layering on GeckoLib 4 or 5 with no mixins. It costs extra empty bones in the model (UP's rex has 36 bones, 19 of them cube-less pivots). Each pivot bone must sit at the same pivot point as its child.
- [inf] FA's additive mixin gives the same result without changing models. But it reaches into GeckoLib internals (`BoneAnimationQueue`, snapshots), so it would have to be rewritten for GeckoLib 5's new pipeline.
- [inf] With Nublar's current setup, a roar or bite while walking must either be a full-body clip or stop the legs. A third "gesture" controller on overlay bones would allow bite-while-walking and roar-while-walking.

### Gaps

- FA's leaping and flying predicates were skimmed, not traced end to end.
- No mod was found using GeckoLib 5's built-in additive or blend-weight features, because none of these mods has ported yet.

## Key question 2: How is walk or run speed matched to movement? What about turning in place and slope?

### Takeaway

FA has the best speed matching. Each walk or run clip has a "blocks per second" value in a data file. Playback speed is the animal's real speed divided by that value, then divided by the animal's scale. FA also fixes GeckoLib 4's phase jump when speed changes mid-clip. UP and JRev use fixed speeds per state. Nobody has a dedicated turn-in-place clip. Body pitch is handled only for swimmers and fliers, through a `pitch_root` bone.

### Cited Findings

- FA keeps a client data file per species holding `blocks_per_second` for walk and run clips. For example, the T-Rex has walk 0.5 and run 1.5. [v] FA `entity/animation/ClientAnimationInfoLoader.java` L44; asset `assets/fossil/animations/entity/data/tyrannosaurus.json`.
- FA's movement speed steps [v] FA `AnimationLogic.java` L284-323:
  1. Start from 1 divided by the entity scale, because the clips were authored at scale 1.
  2. Measure horizontal speed from the delta movement, divided by block friction times 0.91, times 20, to get blocks per second. For a ridden animal not controlled locally, use the position delta instead.
  3. Cap that at the animal's attribute speed (walk or sprint).
  4. Multiply by measured speed divided by the clip's `blocks_per_second`.
  5. If the result is at or below 0.1, switch to idle instead of crawling the cycle.
  6. If the speed drops, ease toward it by at most 0.05 per tick ("inertia").
  7. Above 2.75, or when sprinting, switch to the sprint clip and recompute against its own `blocks_per_second`.
- FA's `PausableAnimationController.setAnimationSpeed(speed, tick)` shifts the controller's tick offset whenever the speed changes. The current point in the clip therefore stays where it was. The comment explains that GeckoLib 4 otherwise rescales all elapsed time retroactively. [v] FA `entity/animation/PausableAnimationController.java` L53-71.
- GeckoLib 4 computes clip time as speed times (tick minus start offset). So any speed change moves the current frame. [v] GL4 `core/src/main/java/software/bernie/geckolib/core/animation/AnimationController.java` L700-708.
- GeckoLib 5 adds a per-frame delta times the previous speed to a running timeline time. So a speed change no longer jumps the phase. [v] GL5 `animation/AnimationController.java` L211-215, L435-441.
- UP's T-Rex uses walk at speed 1.0, walk at 2.0 when running, and 1.15 or 1.65 when ridden. It has a run clip, but the movement predicate never picks it. [v] UP `TyrannosaurusEntity.java` L649-690.
- UP's clip files use `query.ground_speed` (288 uses) and `query.is_on_ground` (1,760 uses) inside molang, to fade amplitudes. For example, roar_blend scales an offset by a clamped ground speed. [v] UP asset `tyrannosaurus_rex.animation.json`, `roar_blend`.
- PFL's walk, run and back-walk clips declare `anim_time_update` as `query.modified_distance_moved` divided by a constant (31 clips). This is the Bedrock way to tie a cycle to distance travelled. [v] PFL jar 0.4.1, `assets/*/animations/maip.animation.json`, `antlion.animation.json`, `para.animation.json`.
- GeckoLib 4 does not read `anim_time_update` at all. GeckoLib 5 parses `anim_time_update` and `blend_weight` into its animation definition, but no runtime code reads either field. [v] GL4 grep, no match; GL5 `loading/definition/animation/ActorAnimation.java` L55-56, no other reference.
- GeckoLib 5 exposes `query.limb_swing` and `query.limb_swing_amount` from the vanilla walk animation state, plus `query.ground_speed` and `query.yaw_speed`. [v] GL5 `loading/math/MolangQueries.java` L306, L312, L323-324.
- GeckoLib 4 Forge computes `query.yaw_speed` by passing an angle in as a partial tick, which looks wrong. The Fabric copy uses a plain yaw minus previous yaw. [v] GL4 `Forge/src/main/java/software/bernie/geckolib/model/GeoModel.java` L249; `Fabric/.../GeoModel.java` L249.
- FA sets the `pitch_root` bone's X rotation from the entity pitch for swimmers in water and fliers in flight. When a player rides a flier, it also sets Z from a banking yaw. [v] FA `client/model/PrehistoricGeoModel.java` L30-56.
- Nublar compares: its gait limits the per-tick speed change by a step that shrinks as the clip runs longer, which bounds the phase error. [v] local `common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/MovementGait.java` L105-117.

### Inferences

- [inf] FA's offset re-anchor would let Nublar drop the bounded-step workaround on GeckoLib 4 and follow speed exactly. On GeckoLib 5 neither is needed.
- [inf] Storing "blocks per cycle" per clip as data, not code, would let artists re-tune foot sliding without a rebuild. Nublar's cycle-ticks inputs are already close to this.
- [inf] For 26.2, `query.limb_swing` gives distance-driven cycles in molang. But `anim_time_update` would need a small custom controller, because GeckoLib 5 ignores it.
- [inf] Nobody does turn-in-place or slope pitch for land animals. This is open ground for Nublar.

### Gaps

- No mod measures stride length from the clip itself; everyone hand-enters the speed.

## Key question 3: Procedural bone work (head look, tail drag, neck, breathing, jaw, IK)

### Takeaway

Tail drag on turning is the common procedural extra. UP feeds a turn-driven tail yaw into a molang query, which drives `_rotation` bones from a tiny 0-length clip. JRev writes a smoothed, weighted tail sway straight into six tail bones in `setCustomAnimations`. UP also has a small follow-the-leader chain solver for long tails and necks. Head look is aimed at a control bone the clips never key. Nobody does blinking, foot IK or procedural breathing beyond sine molang in idle clips.

### Cited Findings

- UP tail and head lag steps [v] UP `common/entity/custom/base/PrehistoricEntity.java` L366-420, called at L131:
  1. Each tick, if the yaw changed by more than 1 degree, add or remove 10 degrees of tail yaw.
  2. Otherwise decay it toward 0 by 5 degrees per tick.
  3. Do the same for a head-look value, with a 0.5 degree threshold.
  4. Clamp both to plus or minus 60 degrees.
  5. Keep previous values so the render side can interpolate by partial tick.
- UP pushes that value into the molang query `query.tail_yaw`, plus a second one relative to body yaw, every frame. It also registers about 45 custom queries at startup: head yaw and pitch, rider look, flight roll and pitch, target distance, temperature and more. [v] UP `client/model/ExtendedMolangQueriesModel.java` L26-114; `UnusualPrehistory.java` L139-187.
- UP's T-Rex file has a clip named `rotation`, length 0, on five `_rotation` bones. It reads `query.tail_yaw` through a clamp, which spreads the bend down the tail. [v] UP asset `tyrannosaurus_rex.animation.json`, clip `animation.tyrannosaurus.rotation`.
- UP's `IKSolver` is a distance-constraint chain. Steps: (1) place body-front and body-back points from the entity position and yaw; (2) pull each node toward its parent at a fixed spacing; (3) return the first node toward centre; (4) read yaw and pitch per link. It is used for the Brachiosaurus, Ulughbegsaurus and Dunkleosteus. The model lerps the bones toward the solved angles at 0.01 per frame. [v] UP `common/entity/util/kinematics/IKSolver.java` L145-200; `BrachiosaurusEntity.java` L171, L532; `client/model/entity/prehistoric/aquatic/DunkleosteusModel.java` L74-95.
- UP's T-Rex model sets the `neck_control` bone's yaw and pitch from the head yaw and pitch. That bone is never keyed by a clip. [v] UP `client/model/entity/prehistoric/TyrannosaurusModel.java` L63-82.
- JRev tail sway steps [v] JRev `entity/custom/BrachiosaurusEntity.java` L182-185, L221-267:
  1. Read the turn as the body yaw delta only, so head look does not swing the tail.
  2. Apply a 0.6 degree dead zone.
  3. Divide by 15 degrees and clamp to plus or minus 1.
  4. Low-pass the result with factor 0.24 per tick.
  5. Snap tiny values to 0.
  6. Clamp to plus or minus 1.5.
- JRev's model applies that sway to six tail bones with falling weights (1.00, 0.78, 0.58, 0.42, 0.30, 0.22). It uses a max of 22 degrees times a gain of 1.35 on yaw, plus 40 percent of that as opposite roll. It overrides Y and Z but keeps the clip's X bend. It also undoes the previous frame's offset, because the model instance is shared between entities. [v] JRev `entity/client/BrachiosaurusModel.java` L48-110. 166 files in JRev reference the sway, so it is used across species [v].
- JRev clamps sauropod head look to plus or minus 10 degrees on `Neck1`. [v] JRev `BrachiosaurusModel.java` L104-109.
- FA turns off GeckoLib's built-in head turning: its model passes "false" to the defaulted model. Its clips have no procedural head look. [v] FA `client/model/PrehistoricGeoModel.java` L24-25.
- Breathing and idle motion in FA and UP are authored as sine molang in the clip files. Over 15,000 FA keyframes have the form "offset plus sine of anim time times a rate, times an amplitude". [v] FA asset files under `assets/fossil/animations/entity/`; UP has 14,485 `query.anim_time` uses.

### Inferences

- [inf] JRev's sway (body yaw delta, dead zone, low-pass, weighted chain, counter-roll) is the closest match to what Nublar needs for tail drag. It fits into the existing `setCustomAnimations` next to the breathing sway.
- [inf] JRev's "undo last frame's offset" step is a workaround for writing absolute values. Nublar should add the offset to the clip value each frame, or keep per-entity state, to avoid cross-entity bleed.
- [inf] UP's "control bone the clips never touch" pattern removes the fight between head look and baked head keys. That matters for Nublar's dense files, which key the head every frame.

### Gaps

- No mod found does foot IK on slopes, blinking or procedural jaw. FA ships a third-party hitbox library, "more_hitboxes", but it was not read.

## Key question 4: Triggers, keyframe events, transitions, loop modes and layering

### Takeaway

FA and UP both use sound and particle keyframes for eat crunches, roars and sniffs. FA keeps the attack hit timing in a server-side data file, so the server knows when the bite lands without the client geometry. FA also fixes a GeckoLib 4 transition gap: bones missing from the new clip now blend instead of snapping. Start, loop and end triplets with `hold_on_last_frame` are common for sit, sleep and knockout.

### Cited Findings

- FA routes particle keyframes named "eat" to spawn diet-specific item particles (seeds, berries, cod, beef). Sound keyframes named "eat" and "call" play eating and ambient sounds. [v] FA `Prehistoric.java` L1627-1650; e.g. the T-Rex `eat` clip fires at 0.75, 1.125 and 1.75 s [v] asset `tyrannosaurus.animation.json`.
- FA's server data file lists, per attack clip, the length and an `action_delay` (seconds to the hit) plus a `uses_attack_box` flag. It is loaded into ticks and read by the attack logic. [v] FA `entity/animation/ServerAnimationInfoLoader.java` L64-70; `AnimationLogic.java` L199-208; asset `data/fossil/animations/entity/tyrannosaurus.animation.json`.
- FA plays server-picked animations by sending the clip name to players within 30 blocks. The client queues it as the "next" animation, then forces a reset and a set transition length on the next frame. [v] FA `AnimationLogic.java` L72-102, L260-271.
- FA's replacement rules [v] FA `AnimationLogic.java` L136-158:
  1. A clip in the same category replaces a finished clip. A looping one replaces it only by chance (the category's chance).
  2. A different category replaces a loop that is not "keep active", or any finished clip.
  3. The transition used is the longer of the old and new category's lengths.
- FA's category defaults are a 5-tick blend and chance 1. Sleep is 20 ticks and 0.05, sit 20 and 0.2, knockout 15, beached 5. [v] FA `entity/animation/AnimationCategory.java` L12-29, L35-37.
- FA transition fix steps [v] FA `compat/geckolib/AnimationControllerOverride.java` L26-53; `mixin/AnimationControllerMixin.java` L44-50:
  1. On the first tick of a new clip, find the bones the old clip animated but the new one does not.
  2. Snapshot them.
  3. Queue a blend from that snapshot to rest over the controller's transition length.
- GeckoLib 4 otherwise returns un-keyed bones to rest over `getBoneResetTime()`, default 5 ticks, whatever the controller's transition length. [v] GL4 `core/.../animatable/GeoAnimatable.java` L47-49; `core/.../animation/AnimationProcessor.java` L131-200.
- UP uses start, loop and end clips: sit_start, sit, sit_end; knockout_start, knockout, knockout_end. 4 of its 23 T-Rex clips are `hold_on_last_frame`. Sound keyframes cue `tyrannosaurus_roar` at 1.25 s and `tyrannosaurus_sniff` at 0.25 s. [v] UP asset `tyrannosaurus_rex.animation.json`.
- UP drives attacks from a synced integer state (21 to 27) on the entity, read by the attack predicate. It does not use GeckoLib triggers. [v] UP `TyrannosaurusEntity.java` L712-756.
- JRev uses GeckoLib triggerable animations for attack and mouth. [v] JRev `TyrannosaurusRexEntity.java` L161, L184-188, L226.
- FA has a debug screen that pauses a controller and scrubs its tick with a slider, through `PausableAnimationController`. [v] FA `client/gui/debug/AnimationTab.java` L90-137; `PausableAnimationController.java` L15-43.
- Nublar compares: the Brachiosaurus file already holds sound keyframes. `move.walk` and `run` have four "walk2" footstep events each, and `eatingtall2` has one more. But no keyframe handler is registered anywhere in Nublar, so they never fire. [v] local `common/src/main/resources/assets/projectnublar/animations/entity/brachiosaurus.animation.json`; grep for `KeyframeHandler` in `common/` finds nothing.

### Inferences

- [inf] Wiring a sound keyframe handler is a small change that turns on footsteps for the Brachiosaurus at once. It lets the other five species get footsteps by adding events to their walk and run clips.
- [inf] A server-side `action_delay` per attack clip would let Nublar line up bite damage with the jaw closing. That is better than a fixed tick count.
- [inf] FA's transition fix matters most for Nublar when moving between clips that key different bone sets: for example, `eatingtall2` back to idle, or dead to anything. It is less urgent if every baked clip keys every bone.

### Gaps

- Custom instruction keyframes (the `timeline` field) were not used by any mod read.

## Key question 5: Babies and growth, sleep and death, swimming

### Takeaway

Babies are handled by renderer scale plus one of: a speed correction (FA), alternative baby clips chosen by name (FA, UP), or a bigger head bone (UP). Sleep, sit and knockout are looping clips with long blends (FA uses 15 to 20 ticks). Swim has its own clip; FA stays in it until the animal touches ground. Death clips are rare; most of these mods rely on vanilla's death tilt.

### Cited Findings

- FA scales the renderer by entity scale and divides walk playback speed by that scale. A small baby therefore cycles its legs faster. [v] FA `client/renderer/entity/PrehistoricGeoRenderer.java` L36; `AnimationLogic.java` L288-289.
- FA sorts clips into pools by name: names containing "_baby", "_water" or "_air" go to separate pools. A trailing "_N_W" gives a pick weight W. A "!" prefix keeps a clip out of automatic category mapping (e.g. `!run_baby`, `!attack_heavy`). At pick time the baby, water or air pool wins if it is not empty. [v] FA `entity/animation/AnimationHolder.java` L20-62; `AnimationCategory.java` L31-33.
- UP's T-Rex model scales the baby's head bone by 1.5 for juvenile proportions. [v] UP `TyrannosaurusModel.java` L68-79.
- UP ships separate baby animation files for some species (beelze_baby, megalania_baby, baby_mammoth, gigantopithicus_baby) and registers `query.is_baby`. [v] UP assets under `animations/`; `UnusualPrehistory.java` L143.
- JRev scales the T-Rex renderer by a synced animal scale. [v] JRev `entity/client/TyrannosaurusRexRenderer.java` L20; `BrachiosaurusEntity.java` L275.
- FA's swim clip is "keep active" until the animal touches ground, so it does not flicker at the shoreline. [v] FA `AnimationLogic.java` L405-410 (land predicate).
- FA's sleep is 20 ticks with chance 0.05, so a sleeping animal rarely swaps between sleep_1 and sleep_2. [v] FA `AnimationCategory.java` L19.

### Inferences

- [inf] The 1 divided by scale speed correction is worth copying for Nublar's growth stages, so baby steps do not slide.
- [inf] FA's long sleep and sit blends (20 ticks against Nublar's 4 to 5) are a cheap way to make lying down read as heavy.

### Gaps

- None of the three open mods has a full death clip system comparable to Nublar's `dead` priority; this was not searched further.

## Key question 6: How are the files authored?

### Takeaway

The hand-animated mods are light. FA and UP T-Rex files are 157 to 181 KB, with a median of 46 to 62 keyframes per second and 17 to 23 bones. They lean on sine molang for idle and breathing, plus catmull-rom interpolation. Nublar's baked files are 10 to 35 times bigger and 10 times denser. Only one closed MCreator mod, JWC, is baked at a similar density. Easing in Nublar's T-Rex and Triceratops files is per keyframe, like JWC's.

### Cited Findings

Per-file numbers [v], from the stats script on the files named:

| File | Size | Clips | Max bones per clip | Keyframes per s (median / max) | Notes |
|---|---|---|---|---|---|
| FA tyrannosaurus | 157 KB | 21 | 23 | 46 / 127 | about 730 sine-molang keys; 4 sound and 4 particle events |
| FA stegosaurus | 210 KB | 21 | 39 | 40 / 280 | 1,250 molang keys |
| UP tyrannosaurus_rex | 181 KB | 23 | 17 | 62 / 171 | 1,220 `catmullrom` lerp keys; idle is 5 keys over 8 s plus molang |
| JRev tyrannosaurus_rex | 73 KB | 7 | 30 | 47 / 177 | 1 sound event |
| PFL maip | 426 KB | 39 | 32 | 79 / 330 | mostly easeInOutSine; `anim_time_update` on 9 clips |
| JWC trex | 453 KB | 6 | 35 | 436 / 459 | baked, 114k linear-eased keys across mod |
| NED / Jurassic Additions / Prehistoric Legacy | 16 to 72 KB | 4 to 11 | 11 to 22 | 21 to 82 | MCreator, linear, almost no molang |
| **Nublar tyrannosaurus_rex** | **5,579 KB** | 26 | 91 | **639 / 1,329** | 18,001 eased keys, 548 molang |
| Nublar velociraptor | 1,888 KB | 29 | 112 | 148 / 804 | no easing |
| Nublar brachiosaurus | 1,407 KB | 29 | 37 | 45 / 896 | 9 unused sound events |

- Naming. FA uses `animation.<species>.<action>[_variant][_weight]` with "!" and "_baby" markers. UP uses `animation.<species>.<action>` with "_blend", "_start" and "_end" suffixes. JRev uses `anim.<species>.<action>`. [v] files above.
- FA's molang is almost all "offset plus sine of anim time" waves. UP adds `math.clamp` on `ground_speed`, `is_on_ground`, `distance_from_camera` and its custom `tail_yaw`. [v] molang scan of both mods' animation folders.
- UP registers extra molang functions: inverse, compare, select, or. [v] UP `UnusualPrehistory.java` L184-187.
- FA notes in a code comment that driving a rider's pitch through a molang variable was "very laggy", so it sets the bone in code. [v] FA `PrehistoricGeoModel.java` L45-47.

### Inferences

- [inf] Hand-keyed clips at 30 to 80 keys per second with sine molang for idle look smooth in play and are 30 times smaller. Nublar's baked density costs load time and memory: the T-Rex file alone is 5.5 MB, parsed per resource reload. It does not obviously buy visual quality over FA or UP.
- [inf] A bake-time decimation pass would likely cut Nublar's files by 70 to 90 percent with no visible change. The pass would drop keys that linear interpolation of their neighbours predicts within a small angle error. This needs a test.
- [inf] UP's per-clip "0 keys plus molang" idle and sleep show a cheaper path for new clips: a pose plus breathing molang.

### Gaps

- The visual quality of each mod's clips was not judged in-game; only file structure was measured.

## Key question 7: What do players say about animation quality?

### Takeaway

Little direct feedback was found. Store pages for Jurassic Reborn advertise "unique AI, animations, textures, and models" [doc], and Primal Frontier credits a dedicated animator [doc]. Searches of Reddit, Modrinth, Moddex and CurseForge found no threads about animation for FA, UP or JRev. The "Fossils Revived" resource pack restyles FA's creatures and lists Fresh Animations as related [doc], which hints at demand for better FA motion.

### Cited Findings

- Jurassic Reborn Modrinth description, animation claim. [doc] modrinth.com/mod/jurassic-reborn
- Primal Frontier Legacy credits "Animators: Deemo". [doc] Modrinth `xkWLBh5U`
- Unusual Prehistory is discontinued in favour of Unusual Prehistory 2, a full remake on its own library. [doc] moddex.gg/mod/unusual-prehistory; [v] UP2 gradle.properties

### Gaps

- Player sentiment on animations is effectively unmeasured. Discord channels, which most of these teams use, were not accessible.

## Techniques Nublar could adopt (ranked)

1. **Wire a sound and particle keyframe handler.** Source: FA (`Prehistoric.java` L1627-1650). Improves walk and run footsteps (the Brachiosaurus events already exist), eat crunches and roar timing. Effort S.
2. **Turn-driven tail drag.** Steps: body yaw delta, dead zone, low-pass, weighted chain plus counter-roll, added in `setCustomAnimations`. Source: JRev (`BrachiosaurusEntity.java` L228-257, `BrachiosaurusModel.java` L48-102); UP for the lag and decay variant. Improves walk, run, idle and turning for all six species. Effort S.
3. **Per-clip data file: blocks per second and attack `action_delay`.** Source: FA (`ClientAnimationInfoLoader`, `ServerAnimationInfoLoader`). Improves walk and run foot slide tuning, and lines bite damage up with the jaw. Effort S-M.
4. **Phase-preserving speed change on GeckoLib 4.** Re-anchor the tick offset when speed changes. Source: FA (`PausableAnimationController.java` L53-71). Improves walk and run, and lets the gait follow speed exactly. Not needed after the GeckoLib 5 port. Effort S.
5. **Blend un-keyed bones over the controller's transition length.** Source: FA (`AnimationControllerOverride`), or simply raise `getBoneResetTime` to match. Improves transitions between clips with different bone sets (eat, drink, dead). Effort S.
6. **Divide speed by growth scale for babies, and use longer blends for sleep and sit.** Source: FA (`AnimationLogic.java` L288-289; `AnimationCategory.java` L19-21). Improves baby walk, rest and sit. Effort S.
7. **Head look on a dedicated control bone** that no clip keys. Source: UP (`TyrannosaurusModel.java` L63-82). Improves head turning over dense baked head keys. Needs a model edit per species. Effort M.
8. **Overlay bone layer plus a third "gesture" controller,** for roar, bite and sniff while walking. Source: UP (`_overlay` and `_rotation` bone stacks, "_blend" clips). Improves roar and attack during movement. Effort M-L (model and clip rework). FA's additive mixin is the code-only alternative, but it would not survive GeckoLib 5.
9. **Bake-time keyframe decimation.** Source: inferred from FA and UP density versus Nublar. Improves file size and load time for all clips, the T-Rex most. Effort M.
10. **Pause and scrub debug screen for controllers.** Source: FA (`AnimationTab`, `PausableAnimationController`). Improves the clip review workflow. Effort M.
11. **Distance-driven cycles on 26.2,** through `query.limb_swing` in molang or a custom `anim_time_update` reader. Source: PFL files plus GL5 `MolangQueries.java` L323-324. Improves foot sync at any speed. Effort M, best done during the GeckoLib 5 port.
