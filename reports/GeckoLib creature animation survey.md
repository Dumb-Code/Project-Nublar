# Fix the clip data first, then add tail lag and head look

Date: 2026-11-03. Status: research report for the project lead. It changes no code, asset or plan module. Four notes back it, in `research_notes/GeckoLib creature animation survey/`.

Project Nublar's dinosaur clips have data faults that cost more than any missing feature. 179 rotation values in 10 clips are between 10^6 and 10^10 degrees. Most of the T. rex file only holds the rest pose. Every clip is baked at 24 to 25 keys per second, and GeckoLib 4 scans that data from the start on every frame. Two code faults also show in game. The breathing sway skips the last one or two tail bones of every species and the Brachiosaurus head. No species turns its head, because all six models are built with head turning off. The surveyed mods agree on the next layer: a tail that lags the body's turn, a clamped look-at spread over the neck, footstep sounds cued from the clip, and per-clip speed and hit-timing data. Each is small and runs on GeckoLib 4 today. Additive layers, distance-driven walk cycles and exact sound markers should wait for the GeckoLib 5 port, where the library supports them. None of the 13 mods read has foot IK, turn-in-place clips, blinking or animation LOD, so in those areas Nublar would lead.

**Labels.** `[v]` means verified in source, at the commit the note names. `[m]` means measured on Nublar's files in this session. `[inf]` means inference. `[doc]` means a README, wiki or store page. No result here was tested in game.

## 1. What is wrong with Nublar's animations now

| # | Finding | Evidence | Effect in game |
|---|---|---|---|
| N1 | 179 rotation values between 10^6 and 10^10 degrees: Brachiosaurus drink, call, eat1, roar; Dilophosaurus eat1, head_shake; Gallimimus swim; Velociraptor dead, roar, scratch | `[m]`, rechecked in this session | Probably bone flicker or a snap on those frames `[inf]` |
| N2 | 64 % of channels never change and 58 % hold exactly the rest pose. 1,432 of 4,545 bone entries animate nothing; on the T. rex, 850 of 1,411 | `[m]` | No visual effect, but on GeckoLib 5 a listed bone belongs to that controller, so these entries would block other layers `[v]` |
| N3 | Baked density: 110,620 keys, sampled at 24 to 25 per second. The T. rex file is 5.6 MB; hand-keyed T. rex files in other mods are 73 to 181 KB | `[m]`; Fossils & Archeology Revival and Unusual Prehistory files `[m]` | GeckoLib 4 walks each keyframe list from the start for every bone and axis on every frame `[v]`, so CPU cost grows with file size |
| N4 | Bezier keys export as `"easing":"bezier"`, which neither GeckoLib 4 nor 5 supports, so they play linear | Blockbench GeckoLib plugin, GeckoLib EasingType `[v]` | Curves authored in Blockbench go flat in game |
| N5 | Catmull-rom differs from the Blockbench preview: GeckoLib 4 uses the wrong incoming tangent (median 0.75°, 110 channels over 3°). 484 segments have catmull on one end only, and 927 looping channels have no wrap at the seam | `[v]` `[m]` | Small wobble and seam pops. GeckoLib 5 fixes the tangent |
| N6 | `IdleSway` covers `tail1` to `tail3`, but every species has `tail4`, and Brachiosaurus also has `tail5`. It writes `head`, but the Brachiosaurus bone is `head1` | `[v]` `IdleSway.java`, geo files | Tail tips are rigid, and the Brachiosaurus head never sways |
| N7 | Every `DinosaurGeoModel` is built with the one-argument constructor, so head turning is off | `[v]` `ClientRegistrationHolder.java` L45-50 | Dinosaurs never look at players or targets |
| N8 | The Brachiosaurus walk, run and eatingtall2 clips have sound keyframes (`walk2`, `grabdeeznuts`), but no handler is registered | `[v]` | No footsteps, and GeckoLib 4 prints a line to stdout every walk cycle `[v]` |
| N9 | No bone is free of keyframes: clips key `root`, `hips`, `neck1` and the tails | `[m]` | Every procedural layer must add a delta after the controllers. It cannot own a bone |

## 2. What the surveyed mods do

| Mod | Licence | What is worth learning |
|---|---|---|
| Fossils & Archeology Revival 1.20.1 | MIT code, assets reserved | Per-clip data: blocks per second for gaits, time-to-hit for attacks. Speed divided by growth scale. Sound and particle keyframes. Re-anchors the tick on a speed change |
| Unusual Prehistory 1.20.1 | MIT code, assets reserved | Three stacked bones per joint (code, gesture, walk), so a bite plays over a walk. Lag-and-decay tail yaw |
| Jurassic Revived | CC-BY-ND | Body yaw change, smoothed, spread down six tail bones with falling weights and a counter-roll |
| Naturalist | MIT code, assets reserved | Damped tail follow (gains 0.35, 0.16, 0.12). Eased visual body yaw with bank. Footstep cues in code. Four-corner terrain sample |
| Mowzie's Mobs | All Rights Reserved (ideas only) | Empty "controller bones" whose key value is a 0 to 1 weight for head look or tail physics. Attack sections: wind-up, active, recovery. Clip time anchored to the server start tick |
| Dragon Survival | Custom, restrictive (ideas only) | Smoothed yaw and vertical speed exposed as Molang queries. Per-transition blend lengths of 2 to 10 ticks |
| Alex's Caves (Citadel) | ARR / no licence file (ideas only) | Tail follower limited to 8° a tick, split 0.8 / 0.2. A sine envelope that weights each procedural layer |
| JurassiCraft 2, DumbLibrary | Read from public mirrors | Look-at spread over chest, neck and head with a divisor. A removed clip's last pose fades over 7.5 ticks |
| Bosses of Mass Destruction, Critters and Companions | LGPL-3.0, MIT | Timed, cancellable attack events. Range and sight re-checked on the hit tick |
| GeckoLib 5, AzureLib | MIT | GeckoLib 5: about 80 Molang queries, exact sound markers, `setAnimationTime`, additive controllers. AzureLib: controller weights and bone masks |

**Do not port** LLibrary's `ChainBuffer` as Ice and Fire 1.20 ships it. It only adds a turn when the yaw changed by less than 0.1° that tick. Its wave step reads yaw where it should read pitch. It never wraps the yaw change `[v]`.

## 3. Recommended order

Effort: S is hours, M is days, L is a week or more.

### Now, on GeckoLib 4: asset fixes (no Java)

| Rank | Change | Fixes | Effort |
|---|---|---|---|
| A1 | Set the 179 out-of-range keys to their likely value (0) and check each clip in Blockbench | N1 | S |
| A2 | Strip channels and bone entries that only hold the rest pose. This is exact and changes no pose | N2 | S |
| A3 | Offline keyframe reduction, catmull-aware: 40 % fewer keys at 0.1° error, 60 % at 0.5°. T. rex: 49 % and 70 %. Review by eye afterwards | N3 | S-M |
| A4 | Authoring rules: no bezier keys; catmull on both ends of a curved segment; a duplicate seam key on looping catmull clips | N4, N5 | S |

A small Python tool in `tools/` would cover A1 to A3 and re-run on every Blockbench export. It needs the lead's approval, because it rewrites asset files.

### Now, on GeckoLib 4: small code

| Rank | Change | Source | Fixes or improves | Effort |
|---|---|---|---|---|
| C1 | A sound keyframe handler on the movement controller, plus footstep markers in walk and run clips. On GeckoLib 4, markers fire early by the transition length (about 0.25 s); shift them or accept it | Fossils, Naturalist, GeckoLib `[v]` | N8; footsteps for all species | S |
| C2 | A critically damped spring helper: a pure class tested like `MovementGait`, stepped on the client tick and lerped by partial tick | Holden, "Spring-It-On" `[doc]` | Base for C3 to C5 | S |
| C3 | Tail and neck turn lag: the wrapped body yaw change drives a follower, spread over the chain with weights that fall toward the root | Alex's Caves, Naturalist, Jurassic Revived | Rigid tails on turns, all species | S |
| C4 | Replace `IdleSway` with a chain wave built from each species' real bone lists | Citadel chain helpers | N6 | S |
| C5 | Own look-at: clamped, spread over the neck bones with a weight that grows toward the head, spring-smoothed, faded out during eat, drink, roar and attack | Citadel `faceTarget`, JurassiCraft 2, Mowzie | N7; most visible on long necks | S-M |
| C6 | Layer weights: a 4 to 6 tick smoothstep for each procedural layer, driven by states `DinoAnimator` already tracks | Alex's Caves, DumbLibrary | Keeps C3 to C5 from fighting the clips | S |
| C7 | Per-clip data: gait blocks per second and attack time-to-hit, so the server hits when the jaw closes | Fossils & Archeology Revival | Foot slide; bite timing | S-M |
| C8 | Baby gait speed divided by growth scale; longer blends into sleep and sit | Fossils & Archeology Revival | Babies' feet slide; rest snaps | S |

The procedural note estimates C2 to C6 at under 300 lines of pure code plus about 40 lines in `DinosaurGeoModel` `[inf]`. Per-animal state lives in a client-only object owned by `DinoAnimator`, created lazily, because the GeoModel is shared by every animal of a species.

### Next: medium work

| Rank | Change | Source | Effort |
|---|---|---|---|
| M1 | Body lean into turns, nose-down on slopes, a small rock at start and stop, with the neck counter-rotated. Fed by the speed average in `MovementGait` | Ice and Fire, Subterranodon | M |
| M2 | Ground fitting before full IK: body pitch and drop from per-leg ground height, with a smooth minimum for bipeds | Citadel / JurassiCraft leg articulator, Naturalist | M |
| M3 | Attack sections (wind-up, active, recovery) started by one packet, with damage at the active boundary and a range check on the hit tick | Mowzie, Critters and Companions, BOMD | M |
| M4 | Split clips by body region so controllers do not overlap: legs in movement, neck and head in main, tail in a third | GeckoLib example, Unusual Prehistory | M, required on GeckoLib 5 |
| M5 | Re-author the worst baked clips by hand with sparse keys: T. rex roar, drink and rest; Dilophosaurus sniffground | Authoring note | L, before the port |

### Wait for the GeckoLib 5 port

| Change | Why wait |
|---|---|
| Distance-driven walk time (`query.limb_swing` or `setAnimationTime`) | GeckoLib 5 has seek and smooth speed changes. On GeckoLib 4, the current speed limiter in `MovementGait` is the right tool |
| Additive breathing and flinch layers | GeckoLib 4 has no additive mode. Test GeckoLib 5's rest-rotation subtraction first, because 81 of 91 T. rex bones have a rest rotation |
| Random start offset per animal | Built in on GeckoLib 5. On GeckoLib 4 it needs a controller subclass |
| Molang tail and neck follow per clip | GeckoLib 4 lacks the head and limb queries |
| Controller weights and bone masks | Neither GeckoLib version has them. Copy AzureLib's idea into a GeckoLib 5 bone updater |

**Porting traps found** `[v]`: `forceAnimationReset` is gone (use `reset()` or `setAnimationTime(0)`). Held animations now report finished. A zero transition snaps to rest on stop. A fresh controller seems to drift toward rest and then snap `[inf]`. `DinoAnimator.set()` depends on the first two.

## 4. Open items for the lead

1. Approve an asset tool that rewrites the six animation files (A1 to A3), or decide to fix them by hand in Blockbench.
2. Confirm the out-of-range keys should become 0. Each clip needs a visual check, because the right value may be a neighbouring key.
3. Choose between procedural deltas (C3 to C5, no model edits) and dedicated control bones (Unusual Prehistory, Mowzie), which need edits to all six geo files but stop the code fighting dense head keys.
4. Choose whether attack timing (C7, M3) waits for the planned threat display and attack track, rank 10 of the Beta survey.

## Sources

| Note | Covers |
|---|---|
| `prehistoric_geckolib_mods.md` | Fossils & Archeology Revival, Unusual Prehistory, Jurassic Revived; six closed-source jars measured; 10 candidates dropped |
| `creature_and_boss_animation_mods.md` | Mowzie's Mobs, Dragon Survival, Naturalist, Bosses of Mass Destruction, Critters and Companions |
| `geckolib_animation_features_and_authoring.md` | GeckoLib 4.8.4 and 5.5.6, Blockbench and its plugins, AzureLib; keyframe measurements |
| `procedural_motion_techniques.md` | Alex's Caves, Citadel, JurassiCraft 2, DumbLibrary, Ice and Fire, spring smoothing |
