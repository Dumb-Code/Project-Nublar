# Procedural motion techniques from creature mods

Scope: procedural animation tricks in creature mods that do not use GeckoLib, and in general game-dev sources, that could sit on top of Project Nublar's GeckoLib clips. The goal is dinosaur motion that reacts to turning, speed, slope and targets, instead of the fixed sway Nublar has now. Sources read: Citadel (Alex's Mobs and Alex's Caves helper library), LLibrary, Ice and Fire, a JurassiCraft 2 (1.12) mirror, Dumb-Code's DumbLibrary and its 1.12 Project Nublar, Fossils and Archeology Revival, and Alex's Caves dinosaur models. Written 2026-11-03. Read only. Nothing was built or run.

Labels: **[v]** = verified in source (repo, commit, path, lines). **[inf]** = my inference. **[doc]** = from an article or documentation page. No code is pasted. All math below is my own wording.

This note builds on two earlier notes and does not repeat them:
- `research_notes/CHAOS branch IK audit/chaos_ik_audit.md`: FABRIK legs and tail, the step planner, the jitter causes, and the GeckoLib 5 `BoneUpdater` facts.
- `research_notes/Reference mod survey for Beta/alexs_caves_dinosaurs.md`, plus `Multipart entity hitbox systems/citadel_alexs_caves.md` (yaw ring buffer for hitbox chains) and `Reference mod survey for Beta/large_mob_infrastructure_and_vehicles.md` (Citadel `LegSolver` per-leg ground height).

Path short forms: `CIT:` = `src/main/java/com/github/alexthe666/citadel/`, `IAF:` = `src/main/java/com/github/alexthe666/iceandfire/`, `LL:` = `src/main/java/net/ilexiconn/llibrary/`, `JC2:` = `src/main/java/org/jurassicraft/`, `DL:` = `src/main/java/net/dumbcode/dumblibrary/`, `PN12:` = `src/main/java/net/dumbcode/projectnublar/` (Dumb-Code 1.12 repo), `FA:` = `common/src/main/java/com/github/teamfossilsarcheology/fossil/`, `AC:` = `src/main/java/com/github/alexmodguy/alexscaves/`, `N:` = this repo, `common/src/main/java/net/dumbcode/projectnublar/`.

## Repository facts

| Repo | Licence | Branch and commit read | Minecraft | What it has for this topic |
|---|---|---|---|---|
| AlexModGuy/Citadel | No LICENSE file in the repo root [v: root listing]. Treat as all rights reserved. | `1.20`, `8c09dce8a1574d210de72eb0bf5874fe1dc6532e` (2026-01-16), mod 2.6.3 | 1.20.1 | `AdvancedEntityModel` wave helpers, `faceTarget`, `LegArticulator` |
| iLexiconn/LLibrary | LGPL-2.1 [v: LICENSE.md L1-4] | `1.12.2`, `6437a59e2af1edd7e07c9bb4591ea086bd8ad757` (2020-11-19) | 1.12.2 | The original `ChainBuffer` |
| AlexModGuy/Ice_and_Fire | LGPL-3.0 [v: LICENSE L1-2] | `1.20`, `0526aad94119b705a86207623481b087c4d052ca` (2025-09-05) | 1.20.1 | `ChainBuffer`, `IFChainBuffer`, `ReversedBuffer`, dragon pitch |
| Jorch72/JurassiCraft2 (mirror of the JurassiCraft team repo) | LGPL-2.1 [v: LICENSE.md L1-3] | `dev-1.12.2`, `c2631ddbb76e74ba26260ed0ae524b7efc351752` (2018-08-25) | 1.12.2 | Animation passes, tail buffer, biped leg articulation |
| Dumb-Code/DumbLibrary | LGPL-3.0 [v: LICENSE.md L1-2] | `master`, `791ada6afea65e284e4f9e534dd64b15a1815afe` (2022-01-01) | 1.12.2 | Animation layers with fade-out "ghosts", JSON procedural modules |
| Dumb-Code/ProjectNublar | LGPL-3.0 [v: LICENSE.md L1-2] | `master`, `e525ed7b69aaaa9de5234de9875f50d3822a25af` (2021-04-30) | 1.12.2 | Speed factor for walk amplitude |
| TeamFossilsArcheology/FossilsArcheologyRevival | MIT for code [v: LICENSE L1-5] | `1.20.1`, `b400cf2423dc245f3d18c8c60122835d4be5c6e2` (2026-06-07) | 1.20.1 | GeckoLib 4: additive controllers, a reserved `pitch_root` bone |
| AlexModGuy/AlexsCaves | No LICENSE file; `gradle.properties` says LGPL (see the earlier note) | `1.21.1`, `0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5` (2026-04-26) | 1.21.1 | Tail turn lag, flight pitch and roll, envelopes, two-leg body drop |
| Project Nublar (this repo) | n/a | `claude/minecraft-dinosaur-geckolib-research-mgtpmv`, `bf4ac64eda2d8c2eb37cffa651f02d33aee841ce` (2026-11-03) | 1.20.1, GeckoLib 4.7.3 [v: gradle.properties L47] | Target of the techniques |

Not read: L_Ender's Cataclysm. I found no public source repo for it (a search found only add-ons). The original `JurassiCraftTeam/JurassiCraft2` URL asked for credentials, so I read a public mirror. Alex's Mobs uses the same Citadel helpers, so it was not read separately.

---

## KQ0. What does Nublar do today, and where can a procedural layer go?

### Takeaway
Nublar has one procedural layer. It is a fixed cosine sway with no per-animal state. It does not react to turning, speed, slope or targets. It touches only `tail1` to `tail3`, but every species has four or five tail bones. It writes `head`, which the Brachiosaurus does not have (its bone is `head1`). Nublar has no head look at all. Per-animal client state already has a home: `DinoAnimator`, which lives on the entity and gets a client tick.

### Cited Findings
- [v] `IdleSway` holds six bone names (`hips`, `neck1`, `head`, `tail1`, `tail2`, `tail3`) with fixed pitch and yaw amplitudes and phases. Angles are a cosine of animation time only. N:`entity/dinosaur/animation/IdleSway.java` L13-45.
- [v] `DinosaurGeoModel.setCustomAnimations` calls the parent, skips dead animals, then adds the sway to each bone's current rotation and calls `resetStateChanges` on it. N:`client/model/DinosaurGeoModel.java` L33-63.
- [v] All six species build `DinosaurGeoModel` with the one-argument constructor. N:`client/ClientRegistrationHolder.java` L45-49. [inf] In GeckoLib 4 that constructor sets "turns head" to false, so the head never follows the look direction. No other code in `N:` reads `netHeadYaw` or the head yaw for bones (grep). The renderer only reads the `head` bone pivot for a synced position. N:`client/renderer/DinosaurRenderer.java` L93-97.
- [v] `Dinosaur` holds one `DinoAnimator` per entity and calls `animator.clientTick()` only on the client. N:`entity/dinosaur/Dinosaur.java` L176, L747-748. `DinoAnimator.clientTick` already computes the horizontal move per tick and feeds `MovementGait`. N:`entity/dinosaur/animation/DinoAnimator.java` L78-87.
- [v] `MovementGait` keeps an exponential average of ground speed (weight 0.3 per tick) and sets the leg clip rate. It does not change the clip's amplitude. N:`entity/dinosaur/animation/MovementGait.java` L39-41, L83-118.
- [v] Chain lengths in the geo files (read with a read-only script):

| Species | Tail chain under `hips` | Neck chain | Head bone | Top bones |
|---|---|---|---|---|
| Brachiosaurus | `tail1`-`tail5` | `neck1`-`neck7` (under `body > shoulder1`) | `head1` | `Root > hips` |
| Dilophosaurus | `tail1`-`tail4` | `neck1`-`neck5` | `head` | `bone > hips` |
| Gallimimus | `tail1`-`tail4` | `neck1`-`neck8` | `head` | `root > hips` |
| Triceratops | `tail1`-`tail4` | `neck1`-`neck2` | `head` | `Root > hips` |
| T. rex | `tail1`-`tail4` | `neck1`-`neck4` | `head` | `root > hips` |
| Velociraptor | `tail1`-`tail4` | `neck1`-`neck5` | `head` | `bone > hips` |

  `tailLower` and `tailUnder` exist but are side bones, not part of the chain. Source: `common/src/main/resources/assets/projectnublar/geo/entity/*.geo.json`.
- [v] The clips animate the top bones in some species. Count of clips that key `root`: T. rex 14 of 26, Gallimimus 5 of 15. `Root`: Brachiosaurus 9 of 29, Triceratops 4 of 23. `bone`: T. rex 15, Dilophosaurus 8, Velociraptor 8. Nearly every clip keys `hips`, `neck1` and the tail bones. Source: `.../animations/entity/*.animation.json`.

### Inferences
- [inf] Every technique below must be additive. No Nublar bone is free of keyframes, so an absolute write would erase the artist's motion. The current pattern (read the post-controller value, add a delta, clear the changed flags) is correct and should be reused.
- [inf] A procedural root bone (the Fossils pattern, KQ6) would need a new empty parent bone above `root`/`Root`/`bone` in each geo file. This is an art-pipeline change, not a code change.
- [inf] Per-animal state should live in a small client-only object owned by `DinoAnimator`, created lazily on the first client tick. The shared `GeoModel` gets it through the `animatable` argument. Ice and Fire also creates its buffers only on the client [v: IAF:`entity/EntityDragonBase.java` L250-256].

### Gaps
- Not checked in game whether the four-bone tail clips already have enough sway that an extra delta looks doubled.

---

## KQ1. Tail and neck lag after a turn (rate-limited follower)

### Takeaway
This is the strongest single idea. Keep a "tail yaw" per animal that chases the body yaw at a capped speed. Draw the tail rotated by the difference between the two. When the animal turns, the tail swings out and then catches up. Alex's Caves does this for the Vallumraptor and the Subterranodon with one float and its previous value.

### Cited Findings
- [v] Vallumraptor keeps `tailYaw` and `prevTailYaw`, both set to the yaw at spawn. AC:`server/entity/living/VallumraptorEntity.java` L99-106. Each tick it saves the previous value (L191) and moves `tailYaw` toward `yBodyRot` by at most 8 degrees, with angle wrap (L315). A getter lerps previous to current by partial tick (L359-360).
- [v] The model lerps body yaw by partial tick, takes the wrapped difference "tail yaw minus body yaw", and turns it into radians. AC:`client/model/VallumraptorModel.java` L469-470. It adds 0.8 of it to `tail` and 0.2 to `tailTip` (L585-586).
- [v] Subterranodon uses the same follower at 8 degrees per tick (AC:`server/entity/living/SubterranodonEntity.java` L81-82, L179, L405) and the same 0.8 / 0.2 split (AC:`client/model/SubterranodonModel.java` L155-156, L225-226).
- [v] The same lag idea drives Alex's Caves hitbox chains through a ring buffer of past yaws. That is already covered in `Multipart entity hitbox systems/citadel_alexs_caves.md`.

### Inferences
Math, in my words:
1. State per animal: `tailYaw`, `prevTailYaw` (degrees). Neck: `neckYaw`, `prevNeckYaw` if wanted.
2. Client tick: `prevTailYaw = tailYaw`. Then `d = wrapDegrees(bodyYaw - tailYaw)`. Then `tailYaw += clamp(d, -maxStep, +maxStep)`.
3. Render frame, with partial tick `p`: `t = lerp(p, prevTailYaw, tailYaw)`, `b = lerp(p, yBodyRotO, yBodyRot)`, `lag = wrapDegrees(t - b)`.
4. Clamp `lag` to a species limit, for example ±40 degrees.
5. Split `lag` over the chain with weights `w_i` that sum to 1. Each bone gets `rotY_i += -lag * w_i` (sign per GeckoLib's axis flip; the current sway already negates Bedrock angles).

Constants to start from:
- `maxStep`: about 8 degrees per tick for raptor-sized animals (the Alex's Caves value). Larger animals should lag more: try 4 to 5 for the T. rex and the Triceratops, and 3 for the Brachiosaurus.
- Weights for a four-bone tail: about 0.4, 0.3, 0.2, 0.1. Child bones inherit parent rotation, so the tip already moves the most. A bigger weight near the base keeps the curve smooth.
- Neck: run the same follower with the head yaw as the goal and a faster step (about 12). Spread it over the neck chain. This gives a neck that leads and a tail that trails.

Where it runs: the step runs on the client tick. The lerp and the bone write run per frame. Cost: two floats per chain per animal, one wrap and a clamp per tick, and four to eight bone adds per frame. No allocation.

GeckoLib fit: add after the controllers, like `IdleSway`. In 4.x, inside `setCustomAnimations`, reading the state from `animatable`. In 5.x, copy `lag` into the render state during extraction (a `DataTicket`), then add it in a `BoneUpdater`.

- [inf] The rate limiter gives a linear, slightly robotic catch-up. A critically damped spring (KQ8) on the same `tailYaw` gives an eased catch-up. An under-damped spring gives a whip with one overshoot. Both cost one more float.
- [inf] Lerping `prevTailYaw` to `tailYaw` without wrap is safe only while both stay near the body yaw. Store them unwrapped next to `yBodyRot`, as Alex's Caves does, or wrap the difference before the lerp.

### Gaps
- The best `maxStep` per species needs an in-game test.

---

## KQ2. The ChainBuffer family (accumulate, hold, decay)

### Takeaway
LLibrary's `ChainBuffer` was the JurassiCraft 2 tail swing and is still in Ice and Fire. It sums the body's turn into a "variation" angle, holds it for a few ticks, then walks it back to zero in fixed steps. The total is spread evenly over the chain. It works, but it is jerkier than KQ1 and the Ice and Fire copies have two bugs. Use KQ1 with a spring instead. Keep this section as history and as a warning.

### Cited Findings
- [v] LLibrary original. Each tick: save the previous variation. If the body yaw changed and the variation is under `maxAngle`, add "old body yaw minus new body yaw, divided by `divisor`". If the variation is above 0.7 × `angleDecrement` (or below the negative), count a timer. Once the timer passes `bufferTime`, subtract `angleDecrement` per tick toward zero. Snap to zero and reset the timer when it gets inside one step. The pitch version is the same on `rotationPitch`. LL:`client/model/tools/ChainBuffer.java` L43-69, L80-105.
- [v] Apply step: lerp previous to current variation by partial tick, convert to radians, divide by the number of boxes, and add the same amount to every box's Y (swing) or X (wave). LL:`.../ChainBuffer.java` L137-150.
- [v] JurassiCraft 2 dinosaurs used `maxAngle` 68, `bufferTime` 3, `angleDecrement` 7. JC2:`server/entity/DinosaurEntity.java` L164, L307, L1109-1111. The T. rex animator applies it to its seven tail boxes after all other procedural motion. JC2:`client/model/animation/entity/TyrannosaurusAnimator.java` L45, L72.
- [v] Ice and Fire dragons, on the client only: neck `turn_buffer` (50, 0, 4), `tail_buffer` (90, 20, 5). In the air: roll (55, 1, 2, divisor 0.5), tail pitch (90, 10, 1, 0.5), body pitch (80, 10, 1, 0.5). IAF:`entity/IafDragonLogic.java` L291-299. They are applied to the neck only while breathing fire, to the tail and body when not a passenger, and the flight buffers only while flying or hovering. IAF:`client/model/animator/DragonTabulaModelAnimator.java` L130-141.
- [v] Bug 1: Ice and Fire's `ChainBuffer` adds a second gate. It only adds the turn when the body yaw changed by less than 0.1 degree in that tick. IAF:`entity/util/ChainBuffer.java` L37. The LLibrary original has no such gate (LL L45).
- [v] Bug 2: Ice and Fire's wave apply reads the yaw variation, not the pitch variation. IAF:`entity/util/ChainBuffer.java` L145-150 and `client/model/IFChainBuffer.java` L255-260. LLibrary reads pitch there (LL L149-150).
- [v] `IFChainBuffer` replaces the equality test with a test that treats a difference of about 1.0 as "no change" (IAF:`client/model/IFChainBuffer.java` L31-34, L47). It also clamps each tick's add to ±`maxAngle` (L48). Its swing decay snaps to `angleDecrement` instead of zero (L53-55, L63-65), so the swing never fully returns.
- [v] `ReversedBuffer` is the LLibrary logic with the sign flipped at apply time. IAF:`entity/util/ReversedBuffer.java` L32-58, L126-131.

### Inferences
- [inf] Bug 1 means the Ice and Fire dragon tail almost never collects a turn: any real turn is more than 0.1 degree per tick. The dragon tail swing is close to dead in 1.20.
- [inf] Bug 3 in all copies: the yaw delta is not wrapped. A yaw that jumps across a 360 boundary (teleport, or a mob whose yaw was reset) adds a huge step in one tick, because the `maxAngle` test runs before the add.
- [inf] The hold-then-linear-decay shape gives a visible "stick, then tick back" motion. Even spreading also means the base bends as much as the tip, which looks stiff on long tails. KQ1 with weights fixes both.
- [inf] Cost is the same as KQ1: two floats and an int per axis.

### Gaps
- Not run, so the bug effects are read from the code only.

---

## KQ3. Travelling waves along a chain (chainSwing, chainWave, walk, bob)

### Takeaway
Citadel's chain helpers turn one cosine into a wave that runs down a chain, with a phase step per bone. Driven by walk distance, the same wave becomes a gait-locked tail or neck sway that grows with speed. Nublar's `IdleSway` hand-writes the phases for six bones. A chain helper would cover the real chain lengths (four or five tail bones, two to eight neck bones) with three numbers per chain.

### Cited Findings
- [v] Chain wave. Phase step per bone = `rootOffset × π / (2 × N)`, where N is the chain length. Bone `i` gets `cos(t × speed + i × step) × amount × degree` added to one axis: Y for swing, X for wave, Z for flap. A global "movement scale" multiplies speed and degree. CIT:`client/model/AdvancedEntityModel.java` L77-124.
- [v] Single-bone helpers. `walk`, `swing` and `flap` add `cos(t × speed + offset) × degree × amount + weight × amount` to X, Y or Z, with an optional sign flip. CIT:`client/model/AdvancedModelBox.java` L309-361. `weight × amount` is a speed-dependent constant lean.
- [v] Bob moves the pivot up and down: `sin(t × speed) × amount × degree − amount × degree`. The "bounce" form uses `−|sin(...)| × amount × degree`, which gives two dips per cycle (one per footfall). CIT:`client/model/AdvancedModelBox.java` L369-378.
- [v] Callers pass either walk distance and walk speed (`limbSwing`, `limbSwingAmount`) or age in ticks with amount 1. The Ice and Fire dragon stacks a walk-driven tail swing and wave with an idle-driven one. IAF:`client/model/animator/DragonTabulaModelAnimator.java` L103-114. The JC2 T. rex does the same on its tail, body and arms. JC2:`client/model/animation/entity/TyrannosaurusAnimator.java` L55-68.
- [v] The Tremorsaurus mixes walk-driven body swing, tail swing, leg walk and a "bounce" body bob, then subtracts the bob from the legs so the feet stay down. AC:`client/model/TremorsaurusModel.java` L363-401.
- [v] DumbLibrary planned JSON modules for the same helpers (`Bob`, `ChainSwing`, `Facing`) with `speed`, `degree`, `root_offset` and `divisor` fields. Their bodies are commented out. DL:`server/json/objects/animation/ChainSwing.java` L20, `Facing.java` L20, `Bob.java` L20-22.

### Inferences
Math for Nublar, in my words:
1. Inputs per frame: a phase `φ` and an amount `a` in [0, 1]. For idle, `φ` = animation seconds and `a` = 1. For walking, `φ` = the leg clip's phase, and `a` = the smoothed speed over the authored speed, clamped to [0, 1].
2. For chain bone `i` of `N`: `angle_i = A × a × cos(2π × f × φ + i × k)`, with `k` = the per-bone phase lag in radians. `k` around 0.5 to 0.8 gives a visible wave down a four-bone tail.
3. Add `angle_i` to the bone's yaw (sideways) or pitch (up-down).
4. Optional: scale `A` by `i / N` so the base moves less. This matches how a heavy tail swings.

Gait lock: the leg clip's phase is the only safe walk clock. The vanilla `walkAnimation.position` drifts from GeckoLib's clip time. Nublar can compute the phase as "controller animation tick × speed, modulo cycle length", from the `MovementGait` values. [inf]

Cost: one cosine per bone per frame. That is the same as `IdleSway` now. No per-animal state for idle. The walk version reads two values already in `MovementGait`.

GeckoLib fit: the walk and run clips already have tail motion. The wave should be small (a few degrees) and mostly fill the bones the clip leaves still, or add the side-to-side sway the clip lacks. Check per species which axes the clips key before choosing amplitudes.

- [inf] Replace `IdleSway`'s six fixed bones with per-species chain lists read from the model once. This fixes the `head1` miss on the Brachiosaurus and the untouched `tail4`/`tail5`.

### Gaps
- Which axes the Nublar walk clips already key on each tail bone was not tabulated.

---

## KQ4. Look-at spread over the neck, with clamps

### Takeaway
Citadel's `faceTarget` turns the head toward the look direction by sharing the angle over several bones. JC2 spreads the T. rex look over chest, two neck bones and the head. Nublar has no head look today. A clamped, smoothed and weighted version would make animals track players and prey.

### Cited Findings
- [v] `faceTarget(yaw, pitch, divisor, bones...)`: each bone gets `yaw / (divisor × N)` and `pitch / (divisor × N)` added, in radians. CIT:`client/model/AdvancedEntityModel.java` L57-65. With divisor 1, the full angle is reached at the last bone.
- [v] JC2 T. rex: divisor 1.5 over `chest`, `Neck1`, `Neck5`, `Head`, so the head reaches two-thirds of the look angle. JC2:`client/model/animation/entity/TyrannosaurusAnimator.java` L70. Tremorsaurus: divisor 1 over `neck` and `head`. AC:`client/model/TremorsaurusModel.java` L401.
- [v] Nublar already limits the head yaw on the entity side with `rotateIfNecessary` against the body. N:`entity/dinosaur/DinoBodyRotationControl.java` L32.

### Inferences
Math, in my words:
1. Inputs per frame: `yawTarget = wrapDegrees(lerp(p, yHeadRotO, yHeadRot) − lerp(p, yBodyRotO, yBodyRot))` and `pitchTarget = lerp(p, xRotO, xRot)`.
2. Clamp: yaw to about ±50 (±30 for the Triceratops, which has a two-bone neck and a heavy frill), pitch to about ±25.
3. Smooth per animal with a critically damped spring (KQ8) on the client tick, half-life about 3 ticks. The vanilla look target can jump when the target changes. The spring stops the snap.
4. Weights over the chain, sum 1, growing toward the head. For the Gallimimus eight-bone neck: small weights on `neck1`-`neck4`, more on `neck5`-`neck8` and the head. For the T. rex: `neck1` 0.15, `neck2` 0.2, `neck3` 0.2, `neck4` 0.2, `head` 0.25.
5. Multiply by a layer weight `L` in [0, 1] that fades the look out while a clip owns the head (eat, drink, roar, attack, sleep). See KQ5.
6. Add `yaw × w_i × L` to each bone's yaw and `pitch × w_i × L` to its pitch.

Cost: two floats plus velocity per axis per animal (four floats), one spring step per tick, and two adds per neck bone per frame.

GeckoLib fit: additive after controllers, as above. Do not use GeckoLib 4's built-in "turns head", which writes only the `head` bone and does not fade during clips. [inf]

### Gaps
- Head-yaw sync: `yHeadRot` on the client follows the server's look control. Whether it is smooth enough for Nublar's large animals was not checked.

---

## KQ5. Layer weights and blend-outs (envelopes, ghosts, pose easing)

### Takeaway
Procedural layers need a weight that rises and falls with the clips, or they fight them. Alex's Caves scales procedural motion by a sine envelope over a one-shot's ticks. DumbLibrary fades a removed animation's last pose over 7.5 ticks. JC2 eases pose-to-pose with a sine. For Nublar, a per-layer weight with a short ramp is the piece that makes KQ1 to KQ4 safe.

### Cited Findings
- [v] Alex's Caves envelope: take the animation tick plus partial tick, minus a start offset, clamped to [0, end]. The weight is `sin(π × t / end)` times an amplitude, then a smooth minimum with 1 (softness 0.1) so it plateaus. AC:`server/misc/ACMath.java` L47-60. The Tremorsaurus uses it to scale the roar head shake and the prey shake. AC:`client/model/TremorsaurusModel.java` L316-330.
- [v] Smooth minimum of `a` and `b` with softness `k`: `h = max(k − |a − b|, 0) / k`, result `min(a, b) − h² × k / 4`. AC:`server/misc/ACMath.java` L47-50.
- [v] DumbLibrary: when an animation is removed, each cube's pose at that moment is stored as a "ghost". Each frame the ghost adds `(ghostPose − defaultPose) × c` to the cube, with `c = 1 − (age since removal) / 7.5 ticks`. Ghosts with `c ≤ 0` are dropped. DL:`server/animation/objects/AnimationLayer.java` L56-77, L94-120.
- [v] JC2 animation passes: each pass moves the parts from the previous pose toward the next pose by a factor. With inertia on, the factor is `sin(π × (t/T − 0.5)) × 0.5 + 0.5`, an ease-in-out. On a pose change, the reached offset is kept, so an interrupted move starts from where it was. JC2:`client/model/animation/AnimationPass.java` L97-105, L173-191, L265-275.

### Inferences
- [inf] GeckoLib already blends clip to clip (Nublar uses 2 to 5 ticks, N:`DinoAnimator.java` L38-44). The gap is between clips and procedural layers. Give each layer (look, tail lag, sway, lean) a weight per animal. Each tick, move it toward 1 or 0 by `1 / rampTicks`. Lerp it by partial tick at render. Targets come from state that `DinoAnimator` already knows: eating, drinking, attacking, the one-shot action, resting, dead.
- [inf] Use a smoothstep on the ramp (`3x² − 2x³`) so the start and end are soft. Ramp 4 to 6 ticks.
- [inf] A DumbLibrary-style ghost is an inertialization: keep an offset at the switch and decay it. Holden's article does this with a spring decay instead of a straight line [doc]. GeckoLib's own transition does most of this for clips. It is not needed unless procedural layers are switched on and off hard.
- [inf] Cost: one float and its previous value per layer per animal.

### Gaps
- None for the math. The list of which clips own which bones per species needs a pass over the animation files.

---

## KQ6. Body pitch and roll from motion (slope, climb, turn lean, acceleration)

### Takeaway
Flying mods tilt the body from vertical speed and roll it into turns. The same math gives land dinosaurs a lean into turns, a nose-down on slopes and a small rock on start and stop. Fossils writes its flight pitch to a bone that no clip touches, so it can set it outright.

### Cited Findings
- [v] Ice and Fire dragon pitch (server, synced): while in the air and not hovering, add 10 × vertical delta per tick, then clamp to [−60, 40]. Above ±2 degrees, decay toward zero by `planeSpeed × |pitch| / 90`, where `planeSpeed = 6 × (|vx| + |vz|)`, plus 1 degree per tick. On the ground, divide by 1.5 per tick, then snap to 0 under 1 degree. IAF:`entity/EntityDragonBase.java` L2202-2240. The previous value is saved each tick (L1721). The renderer lerps it by partial tick and rotates the whole pose stack. IAF:`client/render/entity/RenderDragonBase.java` L48-52.
- [v] Subterranodon (client and server tick): flight pitch moves toward `−clamp(vy, −1, 1) × 57.3` degrees at up to 10 per tick. Roll changes by 10 per tick while the yaw changes by more than 1 degree in a tick. It decays by 5 per tick otherwise, clamped to ±60. AC:`server/entity/living/SubterranodonEntity.java` L77-80, L177-178, L310, L384-404. The model lerps both, scales roll by fly progress and pitch by fly progress minus hover progress, and adds them to the body bone. AC:`client/model/SubterranodonModel.java` L153-154, L223-224.
- [v] Fossils (GeckoLib 4): when swimming or flying, `setCustomAnimations` sets the `pitch_root` bone's X rotation to the lerped entity pitch. For a ridden flyer it also sets Z from a lerped roll. FA:`client/model/PrehistoricGeoModel.java` L30-52.

### Inferences
Land version for Nublar, in my words:
1. Inputs per client tick: body yaw change `dYaw = wrapDegrees(yBodyRot − yBodyRotO)`, smoothed ground speed `s` (from `MovementGait`), forward acceleration `acc = s − sPrev`, and slope `dy` (vertical move per tick while on ground).
2. Turn lean (roll): `rollGoal = clamp(−kRoll × dYaw × s / sRun, −8, 8)` degrees. A turning animal leans in, more when fast.
3. Slope pitch: `pitchGoal1 = clamp(−kSlope × atan2(dy, s), −15, 15)`, only when `s` is above the walk threshold.
4. Acceleration rock: `pitchGoal2 = clamp(kAcc × acc, −5, 5)`. Nose dips when stopping, rises when starting. Large animals should get a smaller value.
5. Smooth each goal with a critically damped spring on the client tick (half-life 3 to 5 ticks). Use an under-damped spring for the acceleration rock if a settle is wanted.
6. Render: lerp by partial tick and add to `hips` (pitch and roll) or to a new procedural root bone. Counter-rotate `neck1` by part of the pitch so the head stays level (the Citadel leg articulator does this too, KQ7).

Cost: six floats per animal, three spring steps per tick, two bone adds per frame.

GeckoLib fit: `hips` is keyed by nearly every clip, so add, do not set. A Fossils-style empty parent bone above `root` would let the lean be an absolute write and keep it apart from clip motion. That needs a geo edit in all six models. [inf]

- [inf] Do not sync these values. They come from motion the client already sees. Ice and Fire syncs dragon pitch because it also drives server-side flight logic.

### Gaps
- Client motion arrives in three-tick pieces (N:`MovementGait.java` L9-11). The acceleration term will be noisy without the speed average Nublar already keeps. Use the averaged speed, not the raw per-tick move.

---

## KQ7. Cheap ground fitting without full IK (leg articulator, smooth body drop)

### Takeaway
Before going to the CHAOS FABRIK system, there is a cheap step. Citadel's leg articulator, from JurassiCraft 1, reads one ground height per leg and turns it into a body drop, a body tilt, and a simple bend of thigh and calf. Alex's Caves drops a biped's body with a smooth minimum of the two legs. The per-leg height source (`LegSolver`) is in the earlier large-mob note.

### Cited Findings
- [v] Quadruped: if any leg height is above zero, take the back pair average and front pair average. Body length is the distance between the pairs' forward offsets. Tilt = `atan2(bodyLength × scale, backAvg − frontAvg) − π/2`. Drop the body by `16 / scale × backAvg` pixels. Add the tilt to the body. Subtract it from the lower body, front thighs and neck. CIT:`client/model/LegArticulator.java` L15-37.
- [v] Leg pair: the lower leg (smaller height) gets its thigh rotated back and calf forward by the height difference times per-joint factors. Its foot is rotated to stay flat. CIT:`client/model/LegArticulator.java` L40-52. Ice and Fire dragons use thigh, calf and foot factors 1.0, 0.5, 0.5 behind and −0.15, −0.15, 0 in front, only for wide dragons on the ground. IAF:`client/model/animator/DragonTabulaModelAnimator.java` L143-151.
- [v] JC2 biped: drop the waist by the average height. Factors 0.4 and 0.4 for the T. rex. The leg solver exists only on the client. JC2:`client/model/animation/entity/LegArticulator.java` L14-23; `TyrannosaurusAnimator.java` L50-53; `server/entity/DinosaurEntity.java` L210, L1101-1104.
- [v] Tremorsaurus: body drop = `(1 − smoothMin(1 − hLeft, 1 − hRight, 0.1)) × 0.8`, in blocks, times 16 for pixels. Each leg is then offset by its own height minus that drop. AC:`client/model/TremorsaurusModel.java` L414-421.

### Inferences
- [inf] For Nublar, the leg-height step runs on the client tick (one or two block reads per leg). The bone adds run per frame with the partial-tick lerp of each height. This avoids the 20 Hz foot jumps the CHAOS audit lists.
- [inf] The smooth minimum stops the body from popping when one foot crosses a block edge.
- [inf] Cost per animal: per tick, two (biped) or four (quadruped) small block reads. Per frame, about ten bone adds. State: one height and its previous value per leg.
- [inf] Bone map: bipeds `hips`, `thighLeft`/`thighRight`, `calfLeft`/`calfRight`, `footLeft`/`footRight`. Quadrupeds (Triceratops, Brachiosaurus) also have `forearm*` and `foot*Front`, and `body` for the tilt. The Velociraptor's right thigh is named `legUpperRight`, not `thighRight` (geo file), so a name table per species is needed.

### Gaps
- Leg forward offsets per species are not in the geo files as data. They would be read from the bone pivots.

---

## KQ8. The smoothing primitive: critically damped springs

### Takeaway
Every technique above needs a smoother. The mods use "approach by a fixed step" or "divide by 1.5 per tick". A critically damped spring is better. It eases in and out, never overshoots, has one tuning number (half-life), and can be stepped exactly for any time step.

### Cited Findings
- [doc] Exact critically damped spring. With half-life `h`, set `y = 2 ln 2 / h`. Toward goal `g` with time step `dt`: `j0 = x − g`, `j1 = v + j0 × y`, `e = exp(−y × dt)`. Then `x = e × (j0 + j1 × dt) + g` and `v = e × (v − j1 × y × dt)`. A cheap form of `exp(−u)` is `1 / (1 + u + 0.48u² + 0.235u³)`. Two springs in series, each with half the half-life, give an S-shaped response. The same decay on an offset gives inertialization. Daniel Holden, "Spring-It-On", https://theorangeduck.com/page/spring-roll-call
- [v] Nublar's speed average is a first-order exponential with weight 0.3 per tick. N:`MovementGait.java` L39-41, L84.

### Inferences
- [inf] Step springs on the client tick with `dt = 1`. Keep the previous `x` and lerp by partial tick at render. This matches how Nublar already treats tick state, keeps cost fixed per tick, and stops at pause. `e` is a constant per half-life, so it can be computed once per species.
- [inf] Under-damped variant, for whip or settle: a damped oscillator with damping ratio about 0.5. Use only for tail tips and the acceleration rock.
- [inf] Cost: two floats per channel, a few multiplies per tick.

### Gaps
- None.

---

## KQ9. Clip-level tools: speed-scaled amplitude and additive controllers

### Takeaway
Two older ideas work at the clip level. Dumb-Code's 1.12 Nublar and JC2 scaled the walk clip's amplitude by speed, so a slow walk has small steps. Fossils patched GeckoLib 4 so that controllers add to each other instead of the last one winning. Nublar already speed-matches the rate. Amplitude scaling is a small extra. The additive-controller mixin is powerful but ties Nublar to GeckoLib 4 internals.

### Cited Findings
- [v] Dumb-Code 1.12 Nublar: the walk "angle" factor is the interpolated speed × 11, clamped to at most 1. PN12:`server/animation/AnimationFactorHandler.java` L9-17.
- [v] JC2 movement pass: for walk, run, swim and climb, the degree is the limb swing amount (×4 in water), clamped to [0.5, 1] while moving. Standing uses speed 3 for the blend back. JC2:`client/model/animation/MovementAnimationPass.java` L19-38.
- [v] JC2 kept per-animal pass state in a weak map from entity to handler, per growth stage, inside the shared animator. JC2:`client/model/animation/EntityAnimator.java` L19-38.
- [v] Fossils: a mixin on GeckoLib's `AnimationProcessor` routes models that carry a marker interface to a replacement loop. FA:`mixin/AnimationProcessorMixin.java` L40-49. The loop runs all controllers, then for each bone adds each controller's value to the previous one. Only the first controller starts from the bone's initial pose. FA:`compat/geckolib/AnimationProcessorOverride.java` L21-90.

### Inferences
- [inf] Amplitude scaling on GeckoLib 4: after controllers, for leg bones only, set `rot = rest + (rot − rest) × k`, with `rest` from the bone's initial snapshot and `k = clamp(s / sWalk, 0.5, 1)`. This needs a list of leg bones per species. It must be skipped during the blend into idle, or it will fight the transition. Effort M.
- [inf] The Fossils mixin would let Nublar play a head-only or tail-only clip on top of the body clip. On GeckoLib 5 the same is native: the earlier rendering note says a second controller is additive there. Do not take the mixin for 4.x if the 26.2 port is close.
- [inf] JC2's weak map is the pattern to avoid. Nublar's `DinoAnimator` on the entity is simpler and is already in place.

### Gaps
- Not checked whether GeckoLib 4.7.3 has the same `AnimationProcessor` method Fossils targets. Fossils' GeckoLib version was not read.

---

## KQ10. How the layers sit on GeckoLib 4 and 5

### Takeaway
One order works for all layers: controllers first, then procedural deltas, each times its layer weight. In 4.x this is `setCustomAnimations`. In 5.x it is a `BoneUpdater`, with the per-animal values copied into the render state first.

### Cited Findings
- [v] GeckoLib 5 `BoneUpdater`s add to bone snapshots after the controllers, in a fixed order. Per-pass data comes from the render state. Covered in `Reference mod survey for Beta/client_rendering_and_animation.md` (Bone updaters section) and the CHAOS audit (Porting section).
- [v] Nublar 4.x write pattern: read the current rotation, add, clear changed flags. N:`client/model/DinosaurGeoModel.java` L46-62.

### Inferences
Frame order for Nublar (numbered):
1. Client tick (`DinoAnimator.clientTick`): update the gait (existing), the layer weights, tail and neck followers, the look spring, the lean springs, and the leg heights. Save each previous value first.
2. Frame, after controllers: compute the partial-tick lerp of each value.
3. Add, in this order: leg fitting (KQ7), body lean (KQ6), look (KQ4), tail and neck lag (KQ1), chain sway (KQ3, replacing `IdleSway`).
4. Clear changed flags on each written bone, as now.
5. 26.2 port: in the extraction stage, copy about 12 floats per animal into a `DataTicket`. Step 3 moves into one `BoneUpdater`. Step 4 is not needed.

- [inf] Distance cut: skip steps 1 to 3 past about 32 blocks, as the hitbox budget suggests. Below that, the whole stack is under 30 bone adds and about 10 trig calls per animal per frame.
- [inf] Keep the procedural state on the client object only. The server never needs it. Do not allocate it on the server: `DinoAnimator` is created on both sides today (N:`Dinosaur.java` L176), so the new state object should be created lazily inside `clientTick`.

### Gaps
- The order of steps 3a to 3e was chosen by reasoning, not tested.

---

## Techniques Nublar could adopt (ranked)

| Rank | Technique | Source | Bones and species it improves | Effort | Per-animal state |
|---|---|---|---|---|---|
| 1 | Tail and neck turn lag: rate-limited or spring follower of body yaw, weighted over the chain | Alex's Caves Vallumraptor, Subterranodon (KQ1) | `tail1`-`tail4/5` all species; neck chain for Gallimimus, Brachiosaurus, Dilophosaurus, Velociraptor | S | 2-4 floats per chain |
| 2 | Clamped, smoothed look-at spread over neck and head, faded during clips | Citadel `faceTarget`, JC2 T. rex (KQ4) | `neck1`-`neckN`, `head`/`head1`; all species, most for long necks | S-M | 4 floats + 1 layer weight |
| 3 | Layer weights with smoothstep ramps tied to `DinoAnimator` states | Alex's Caves envelope, DumbLibrary ghosts (KQ5) | Every procedural layer | S | 1-2 floats per layer |
| 4 | Critically damped spring helper (pure class, like `MovementGait`) | Holden (KQ8) | Used by 1, 2, 5 | S | 2 floats per channel |
| 5 | Chain-wave idle and gait-locked sway from chain lists; replaces `IdleSway` and fixes the `head1` and `tail4/5` misses | Citadel chain helpers (KQ3) | All tails; long necks; Brachiosaurus head | S | None for idle; walk uses gait phase |
| 6 | Body lean into turns, slope pitch, acceleration rock on `hips`, with neck counter-rotation | Ice and Fire pitch, Subterranodon roll (KQ6) | `hips`, `neck1`; most visible on T. rex, Triceratops, Brachiosaurus | M | 6 floats |
| 7 | Ground fitting: body drop and leg bend from per-leg height, smooth minimum for bipeds | Citadel / JurassiCraft 1 articulator, Tremorsaurus (KQ7) | `hips`/`body`, thighs, calves, feet; all species on uneven ground | M | 2 floats per leg |
| 8 | Speed-scaled walk amplitude for slow walks | Dumb-Code 1.12, JC2 (KQ9) | Leg bones; all species at herd-travel speed | M | None (reads `MovementGait`) |
| 9 | Additive controllers for partial-body clips | Fossils mixin (KQ9) | Head or tail one-shots over body clips | L on 4.x, S on 5.x | None |
| - | Do not port | `ChainBuffer` family (KQ2) | - | - | - |

Ranks 1 to 5 together are under 300 lines of pure, testable code, plus about 40 lines in `DinosaurGeoModel`. [inf]
