# Creature, dragon and boss mods: how they animate on top of GeckoLib

Research note for the GeckoLib creature animation survey. Written 2026-11-03. Scope: the animation side of five non-dinosaur mods with readable source: Mowzie's Mobs, Dragon Survival, Naturalist, Bosses of Mass Destruction (BOMD) and Critters and Companions (C&C). The note covers controller layout and state selection, procedural motion on top of clips, attack timing between server and client, turns and gait changes, keyframe events, and performance. Hitboxes and multipart are out of scope (see `research_notes/Multipart entity hitbox systems/mowzies_mobs.md`). The Mowzie dynamic chain and leg solver basics are already in that note and are only referenced here. The CHAOS IK audit (`research_notes/CHAOS branch IK audit/chaos_ik_audit.md`) is the Nublar-side IK reference.

Labels: **[v]** verified in source at the stated commit, with path and lines. **[inf]** inference from verified facts. **[doc]** from a README, comment or licence text. Clean room: no code is pasted. Mechanisms are described in words and numbered steps. Mowzie's Mobs is All Rights Reserved and Dragon Survival uses a restrictive custom licence, so their techniques may inform a design but nothing may be copied, including animation JSON values.

Nublar baseline used for comparison (commit `0838bd4a9ed4b5b95929a4c2b747b11c3523dea4`, 2026-10-25): two controllers (movement with speed-matched rate and hysteresis in `MovementGait`; main with a priority list in `DinoAnimator`), blend 4 to 5 ticks and 2 for reactions, breathing sway added in `DinosaurGeoModel.setCustomAnimations` through `IdleSway`, head look from `DefaultedEntityGeoModel`, one-shots from a synced byte (`DinoOneShots`). No tail or neck physics, no IK, no keyframe events.

Repository facts:

| Mod | Licence (SPDX) | Branch and commit read | Minecraft | Loaders | Animation library |
|---|---|---|---|---|---|
| Mowzie's Mobs | No SPDX. "All Rights Reserved unless otherwise explicitly stated" [v] License.txt L1 | `1.20`, `64d15248c4421fdf8555165134b306e8cfc0a6c6` (2026-04-22) | 1.20.1 | Forge | GeckoLib 4 (CurseMaven file 7553267) [v] build.gradle L110, plus a vendored LLibrary for older mobs |
| Dragon Survival | No SPDX. Custom "Dragon Survival License" v1.2, Sept 2026; addons and forks allowed, asset reuse needs permission [v] LICENSE.txt L1-30, L63 | `1.21.1`, `7623d8435bd5dad4a05f9fb55a964a1941f79194` (2026-11-01) | 1.21.1 [v] gradle.properties | NeoForge | GeckoLib 4.7.5.1, range [4.6,) [v] gradle.properties |
| Naturalist | MIT for code; All Rights Reserved for `*/src/main/resources/` [v] LICENSE L1-20 | `1.20.1-MultiLoader`, `82ef491e942b68114fc1ce98e2669b5b540f4e75` (2026-10-22), mod 2.0.9 | 1.20.1 [v] gradle.properties L8 | Forge and Fabric | None. This line uses vanilla `HierarchicalModel` and `AnimationState`, not GeckoLib [v] (no GeckoLib reference anywhere in the tree) |
| Bosses of Mass Destruction | LGPL-3.0 (text of LGPL v3; "only" or "or later" not stated) [v] LICENSE L1-3 | default branch, `2fbd0dc79bea498bcad755c4ad9969055dc452c7` (2024-10-13), mod 1.10.2 | 1.21.1 [v] gradle.properties | Fabric | GeckoLib 4.5.6 [v] gradle.properties, build.gradle L55. Kotlin |
| Critters and Companions | MIT for code; All Rights Reserved for `common/src/main/resources/assets/` [v] LICENSE.md L1-20 | `1.20.1/main`, `f99845def4850de2828b2d225f7c4c16131b6364` (2026-08-19) | 1.20.1 [v] gradle.properties | Forge and Fabric | GeckoLib 4.2.3 [v] gradle.properties |

Candidates dropped: Isle of Berk (`JCback/IsleofBerk`, master `854c1d1`, 2020) holds only a README and language files [v]. Born in Chaos (`FoxyFox909/Born-in-Chaos`, `b7dc5e3`, 2023-09-18) holds two Java files and no GeckoLib code; licence All Rights Reserved [v]. Creatures and Beasts and Untamed were not found with public source in the time available (gap).

Base URLs: MM = `https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/`; DS = `https://github.com/DragonSurvivalTeam/DragonSurvival/blob/7623d8435bd5dad4a05f9fb55a964a1941f79194/src/main/java/by/dragonsurvivalteam/dragonsurvival/`; NAT = `https://github.com/Starfish-Studios/Naturalist/blob/82ef491e942b68114fc1ce98e2669b5b540f4e75/common/src/main/java/com/crispytwig/naturalist/`; BOMD = `https://github.com/miyo6032/bosses-of-mass-destruction/blob/2fbd0dc79bea498bcad755c4ad9969055dc452c7/src/main/kotlin/net/barribob/`; CC = `https://github.com/bonsaistudi0s/CrittersAndCompanions/blob/f99845def4850de2828b2d225f7c4c16131b6364/common/src/main/java/io/github/bonsaistudi0s/crittersandcompanions/`. Paths below are relative to those bases.

## Key question 1: Controller layout and state selection

### Takeaway

Nobody uses many controllers for the base body. Dragon Survival is the richest: one main controller for locomotion and poses, separate controllers for bite and breath, several emote slots and several "continuous" slots that always play. The main lever against jitter is not hysteresis but per-transition blend lengths: each branch of the state selector sets its own `transitionLength` (2 ticks for snappy changes, 6 to 8 for flight changes). Mowzie switches the blend to 0 whenever an ability owns the controller, so attack clips start exactly on their tick. Naturalist (vanilla animation states) gets crossfades by fading a weight per state at a fixed rate per tick, with idle also faded by limb-swing amount, which removes the hard idle/walk flip.

### Cited Findings

- Dragon Survival registers one `main` controller (blend 2), N emote slots, a `bite` and a `breath` controller (blend 2 each) and N "continuous" slots used "for things that need to play always (wind blowing, breathing)". [v] — DS `common/entity/DragonEntity.java` L125-141
- The main predicate is a long if/else ladder: emote override, ability animation, magic-source sit, sleep, riding, flight (spin, dive, dive-alt, climb, soar, land, hover, fly), swim, land-end, climb, jump, fall, sneak, sprint, walk, dig, idle. Each branch sets its own blend length: 8 for spin, 7 for steep dive, 6 for powered flight, 4 for run and fast swim, 3 for walk, idle and sneak, 2 for landing, swim and climb, 5 for fall and sneak-walk. [v] — DS `common/entity/DragonEntity.java` L575-781
- If the controller had no animation before the predicate ran (fresh entity, dimension change), the blend is forced to 10 ticks to hide the T-pose-to-clip snap. The comment says "instantly transition", but the code sets 10. [v] — DS `common/entity/DragonEntity.java` L784-786
- A landing is a two-part chain: while the land clip is playing and the dragon touches ground, the predicate switches to a land-end clip and records that clip's length in a tick timer. Later ticks keep the land-end clip while the timer has time left, so the clip is not cut by the next state. [v] — DS `common/entity/DragonEntity.java` L717-728; `client/render/util/AnimationTickTimer.java` L11-50
- Short one-shots (bite, use item, eat) use the same latch: the first frame stores the clip's baked length in the timer, and the predicate keeps the clip while the timer has time left, even if the input flag has cleared. [v] — DS `common/entity/DragonEntity.java` L324-382
- Emotes carry a `blend` flag. A non-blending emote stops the main controller, so the emote fully owns the body. Blending emotes play on their own slot over the main clip. Emotes and abilities can also "lock" the neck and tail (see Q2). [v] — DS `common/entity/DragonEntity.java` L598-611, L801-811
- Mowzie GeckoLib mobs use a single controller with blend 5. When an ability is active, the predicate sets blend 0 and delegates to the ability; otherwise it loops idle. [v] — MM `server/entity/MowzieGeckoEntity.java` L27, L103-127
- Mowzie's controller subclass adds `playAnimation`, which forces a reset, sets the clip, polls it from the queue and computes the tick offset from the entity's own tick count at once. A code comment says mobs normally do not start a clip until they render, "even if the behavior starts off-screen", and this fixes it. [v] — MM `client/model/tools/geckolib/MowzieAnimationController.java` L20-47
- C&C's shared bug predicate is a short priority list: custom (death, hit) > fly > dance > sit > walk > idle. Blend is 4 ticks, except 0 into sit and 0 out of sit to idle. [v] — CC `common/entity/animation/BugAnimations.java` L35-81
- BOMD bosses have one controller with blend 5. The client sets the next clip only when a status byte arrives, and loops idle otherwise. [v] — BOMD `boss/mob/mobs/gauntlet/AnimationHolder.kt` L10-46
- Naturalist's state weight: each state has a factor that moves toward 1 or 0 by a fixed fraction per tick (0.5 default, 0.25 for poses such as sit and sleep, 1.0 for instant states such as bite). The state stops when the factor falls under 0.001. The model skips a state whose factor is at or under 0.05. [v] — NAT `world/entity/SmoothAnimationState.java` L6-48; `client/model/NaturalistEntityModel.java` L112-123
- Naturalist idle is also scaled by one minus 2.5 times the limb-swing amount, so idle fades out smoothly as the animal starts moving instead of switching at a speed threshold. [v] — NAT `client/model/NaturalistEntityModel.java` L132-143

### Inferences

- Per-transition blend lengths are cheap and fit Nublar's priority list. A stand-to-walk wants about 3 ticks, run-to-stand longer, flinch 2. Nublar uses one value for all main transitions (4 to 5) and 2 for reactions. [inf]
- Dragon Survival's tick-timer latch is the same job Nublar's server-cleared one-shot byte does, but done on the client. It keeps a one-shot alive for its baked length even if the flag flickers. [inf]

### Gaps

- None of the five uses explicit hysteresis like Nublar's `RunGait`. Dragon Survival relies on player input being stable. Mobs in Mowzie are ability driven, so the question does not arise.

## Key question 2: Procedural motion on top of clips

### Takeaway

Three patterns stand out. (1) Mowzie "controller bones": empty bones whose keyed X position (0 to 1) is read in code as a weight. Animators fade procedural layers (head look, idle noise, tail physics, walk/run blend) in and out from inside each clip. (2) Dragon Survival "handler bones": extra wrapper bones in the rig hierarchy that only Molang drives. Every clip carries Molang tracks on them that read smoothed yaw-change and vertical-speed values from code, so each clip decides how much tail swing and neck counter-turn it gets. (3) Naturalist's damped body chain and terrain leg solver: per-segment exponential follow of the body yaw with gains that fall toward the tail tip, plus a four-corner ground sample that pitches and rolls the body and lifts each leg. All write after the clip, inside the model's custom-animation hook, and all avoid accumulation by resetting each frame or by writing to bones no clip touches.

### Cited Findings

**Mowzie's Mobs**

- `MowzieGeoModel` resets every registered bone to its initial snapshot (rotation, position, scale) in `applyMolangQueries`, before GeckoLib applies animation. So code can add to bones each frame and never accumulate, even on bones no clip keys. [v] — MM `client/model/tools/geckolib/MowzieGeoModel.java` L24-44
- Controller bones: `getControllerValue(name)` returns the named bone's X position (0 if the model is not ready) and an inverted form returns one minus it. Sixteen such bones are read across the mod: armAim, bellyBounce, featherRaise, featherShake, gauntletProgress, handLeft/Right, idleAnim, locomotionAnim, maskPlacementSwitch, tailPhysicsBlend, torsoAim, transformTexture, walkRunSwitch, wallCling, whichForm. [v] — MM `client/model/tools/geckolib/MowzieGeoModel.java` L46-58; text search of `client/`
- Sculptor uses `torsoAimController` to split head look between head and chest: head gets look times (1 minus c), chest gets look times c. An attack clip can move aiming to the torso and back without code changes. [v] — MM `client/model/entity/ModelSculptor.java` L62-67
- Sculptor's procedural idle (eyebrows, cloth, legs, mouth scale, arms) is multiplied by `idleAnimController`. The arm part is also multiplied by an inverted "disable arms" controller, so a clip can silence just the arms. [v] — MM `client/model/entity/ModelSculptor.java` L250-299
- Umvuthi spreads the look over three bones (neck, neck2, head), each taking one third of the clamped yaw and pitch. Yaw is clamped to plus or minus 140 degrees. Its idle is a set of sine waves with small per-bone phase offsets (0 to 0.6) so motion ripples down the chain. Leg sway is scaled by one minus a "legs up" timer value. [v] — MM `client/model/entity/ModelUmvuthi.java` L39-110
- Elokosa's tail physics strength is set per frame from `tailPhysicsBlendController`: the chain's alpha is one minus that value, and the chain output is a lerp between the animated pose and the simulated pose by alpha. A clip can pin the tail to its keys (for an attack) and let it go loose after. [v] — MM `client/model/entity/ModelElokosa.java` L162-170; `client/model/tools/dynamics/GeckoDynamicChain.java` L271-290
- The dynamic chain is Verlet with gravity, damping, optional pull back to the animated pose (falling off with segment index squared), several sub-steps per frame and fixed segment lengths. If more than one tick passed since the last update (the entity was culled), it snaps to the animated pose instead of simulating, which stops a pop when the mob comes back on screen. It runs at render time from the renderer, after the normal render pass, and draws a duplicate "dynamic" bone set while hiding the originals. [v] — MM `client/model/tools/dynamics/GeckoDynamicChain.java` L122-170, L233-268; `client/render/entity/MowzieGeoEntityRenderer.java` L62-97. Mechanism already summarised in the multipart note.
- Mowzie's foot placement (LLibrary models only): per-leg ground height from a block sample; the body drops by the back average; the body tilts by the arctangent of back-minus-front over body length; front thighs and the neck counter-rotate by the same tilt; a leg on lower ground bends thigh and calf by the height difference. [v] — MM `client/model/tools/LegArticulator.java` L12-63

**Dragon Survival**

- A per-player `DragonAnimationState` is ticked every client tick, "even if the dragon is off screen". Each tick it (1) pushes body-yaw change, head-yaw change and head-pitch change (each times 0.2) and negative vertical speed (times 10, reduced when the dragon pitches up or down) into 4-tick moving averages; (2) eases the stored value toward the average with a factor tuned to match their old 60 fps behaviour; (3) keeps a previous and current pose so the render frame can interpolate by partial tick. A "clear vertical velocity" flag zeroes the tail-lift history on water entry, so the tail does not jerk. [v] — DS `client/render/util/DragonAnimationState.java` L8-136; `client/render/ClientDragonRenderer.java` L344-352
- `DragonModel.applyMolangQueries` publishes the sampled values as Molang queries: head yaw, head pitch, body-yaw change, tail lift, head-yaw change, head-pitch change, plus gravity and growth. Head values read 0 when the neck is locked; tail values read 0 when the tail is locked. [v] — DS `client/models/DragonModel.java` L43-70
- The shipped animation file `dragon_center.json` (assets; restricted licence, read but not copied) has 104 clips. Body-yaw change appears in 63 clips, tail lift in 59, head yaw in 57, head pitch in 55. In the walk clip the Molang tracks sit on dedicated wrapper bones: five tail handler bones, four neck handler bones, a head handler, and separate yaw and pitch "head turn handler" bones per neck segment. The tail gain rises from tip to base and each value is clamped (about plus or minus 20 degrees for yaw). Neck handlers counter-turn against body-yaw change, with clamps shifted by the current head yaw. [v] — DS `src/main/resources/assets/dragonsurvival/animations/dragon_center.json` (parsed with a script; values summarised, not copied)
- Ability animations are data driven: a "compound" ability animation is a start clip then a looping clip, with its own blend length, layer, and neck and tail lock flags. [v] — DS `common/codecs/ability/animation/CompoundAbilityAnimation.java` L1-50

**Naturalist (vanilla model, not GeckoLib)**

- Every model resets all parts to the rest pose at the start of each setup call and then applies clips and procedural offsets additively. [v] — NAT `client/model/NaturalistEntityModel.java` L64-79
- `BodyChain` per tick: (1) eases a "render yaw" toward the body yaw by a fixed fraction (1.0 for alligator, 0.2 for shark, 0.15 for whale); (2) each segment eases its yaw toward the previous segment (segment 0 toward body yaw, segment 1 toward render yaw) with its own gain; alligator gains are 0.35, 0.16, 0.12; (3) the same for pitch; (4) a roll target equal to the render-yaw change times a factor, clamped (shark: 4 per degree, max 25), eased at 0.1. The model reads each segment's offset from its parent, interpolated by partial tick and wrapped, and adds it to the tail bones. [v] — NAT `world/entity/BodyChain.java` L23-87; `world/entity/animal/alligator/Alligator.java` L84-91, L377-394; `client/model/animal/alligator/AlligatorModel.java` L111-131
- Alligator body pitch: on land the target pitch is 0 and the ease is 0.25 per tick; while swimming the target is the motion pitch, clamped, eased at 0.07. Roll from yaw change applies only in water. The model rotates the body by pitch and roll, rotates the neck back by a fraction of both, and lifts and counter-rotates the limbs. [v] — NAT `world/entity/animal/alligator/Alligator.java` L377-394; `client/model/animal/alligator/AlligatorModel.java` L111-131
- `TerrainLegSolver` (client tick only for elephant and giraffe): for each of four foot points, offset forward and sideways from the body yaw and scaled by the entity scale, it walks down block collision shapes to find the ground distance, eases the stored height toward it by 0.2 per tick, and clamps it to a range. `IKEntityModel` then (1) takes front, back, left and right averages; (2) computes pitch as the arctangent of front-minus-back over twice the forward offset, and roll the same way over the side offset at half strength; (3) lowers the body by the mean height; (4) applies pitch and roll to the body and the inverse to the head; (5) lowers each leg by its own height. [v] — NAT `world/entity/TerrainLegSolver.java` L24-108; `client/model/IKEntityModel.java` L20-76; `world/entity/animal/elephant/Elephant.java` L84, L547-553

**BOMD and C&C**

- BOMD writes absolute values (not additions) to code-only bones named "code_root" or "headBase". The Lich's body pitch is a neutral 30 degrees plus up to 15 degrees from its velocity direction (interpolated from a velocity history); the head pitch is the look pitch minus that body pitch; head yaw is head minus body yaw. [v] — BOMD `boss/mob/mobs/lich/LichCodeAnimations.kt` L9-29; `boss/mob/GeoModel.kt` L23-25
- C&C red panda keeps the head level in world space: a `head_axis_align` bone gets the inverse of the summed parent rotations (built from a quaternion chain), the clip's `head` bone is compensated by the same amount, and a separate `head_rotation` bone takes the look yaw and pitch. [v] — CC `client/model/geo/RedPandaModel.java` L26-53; `client/util/AnimationUtils.java` L16-41
- C&C otter clamps look yaw to plus or minus 35 degrees "to avoid jitter", applies pitch only in water, scales yaw by 0.6 while walking on land, and zeroes the root bone's rotation on land to stop pitch spikes from the clip. [v] — CC `client/model/geo/OtterModel.java` L16-51

### Inferences

- Controller bones are the best fit for Nublar's dense baked clips. An animator can key, inside a roar or a bite, "head look off" or "tail physics off" without touching Java. It costs one empty bone per channel and works in GeckoLib 4 and 5. [inf]
- Dragon Survival's handler-bone approach moves the tail and neck response into the clip file. It needs wrapper bones added to each rig, and Molang tracks in every clip (or in an additive clip on a separate controller). That is a larger asset job for Nublar's 45 to 160-bone rigs, but it lets each clip tune its own sway. [inf]
- Naturalist's `BodyChain` is the cheapest "tail reacts to turning" layer: three gains per species, no physics, one array per segment, deterministic, and it already interpolates by partial tick. It drops straight into Nublar's `setCustomAnimations` as an additive yaw and pitch on tail and neck bones. [inf]
- Splitting head look over neck segments (Umvuthi: equal thirds; Dragon Survival: separate yaw and pitch handlers per segment, larger share near the head) suits long-necked sauropods better than one head bone. [inf]
- Nublar's `IdleSway` relies on GeckoLib re-writing every bone each frame. Mowzie's explicit reset in `applyMolangQueries` is a stronger guarantee if Nublar starts adding to bones that no clip keys (new procedural bones). [inf]

### Gaps

- None of the five uses real multi-joint IK for legs. Mowzie and Naturalist lift and bend; they do not solve knee angles. No mod plants a foot to a world point during a step.
- No mod has procedural jaw or blink. Mowzie's Sculptor uses a hand open/closed controller bone that swaps hidden meshes, which could be reused for eyelids. [v] ModelSculptor L73-82

## Key question 3: Attack choreography and server/client sync

### Takeaway

The best pattern is Mowzie's ability sections: one packet at start, then both sides run the same section timeline (startup, active, recovery) tick by tick, and the server does damage at a section boundary. The clip is started from the same tick, anchored to the entity's tick count rather than the first rendered frame. Combos chain by a "jump to section" packet in recovery. BOMD and C&C are simpler: send one status byte or `triggerAnim` at wind-up start, then the server runs a delayed event and re-checks range before damage.

### Cited Findings

- An ability is a list of sections, each typed STARTUP, ACTIVE, RECOVERY or MISC, and each instant, fixed duration or open ended. Every tick the ability counts ticks in the section and moves on when the duration passes. [v] — MM `server/ability/AbilitySection.java` L3-35; `server/ability/Ability.java` L58-98
- The server sends a use-ability message to tracking players once at start, an interrupt message when it is cancelled, and a jump-to-section message for combos. There is no per-tick traffic. [v] — MM `server/ability/AbilityHandler.java` L82-133
- Sculptor's attack: startup 5 ticks, active 7, recovery 11. The clip plays at the start of startup and alternates hands (start, right, left). An attack sound plays on startup tick 4 with a one-in-three chance. The boulder is launched on the server when the active section begins. In recovery, if a target is still valid, the server jumps back to section 0 for the next swing; otherwise the matching "end" clip plays. [v] — MM `server/entity/sculptor/EntitySculptor.java` L1163-1275
- Hurt reactions respect the attack: an ability interrupts only when damage is at least a per-ability threshold, and the mob plays a hurt ability only if no ability is running or the running one can be interrupted. [v] — MM `server/ability/Ability.java` L224-234; `server/entity/MowzieGeckoEntity.java` L70
- BOMD: the move selector sends a one-byte entity status for the chosen attack, then runs the attack. The client maps the byte to a "start" clip followed by a looping hold clip. The punch schedules timed events: a sound at tick 12, a closed-fist hitbox at tick 7, acceleration from tick 16 for 15 ticks, contact checks from tick 16 to 56, a stop-animation status at 56 and an open-hand hitbox at 64. Events cancel together if the attack is cancelled. [v] — BOMD `boss/mob/mobs/gauntlet/GauntletMoveLogic.kt` L39-43; `boss/mob/mobs/gauntlet/AnimationHolder.kt` L18-43; `boss/mob/mobs/gauntlet/PunchAction.kt` L32-77; `maelstrom/general/event/TimedEvent.kt` L3-18
- C&C melee goal: on attack, it swings, calls `triggerAnim` once, sets a wind-up counter and stops navigation. When the counter ends, it hits only if the target is still in range and in sight, then starts a 20-tick cooldown. [v] — CC `common/entity/brain/goal/AnimatedDelayedMeleeAttackGoal.java` L109-163
- Naturalist latches the vanilla `swinging` flag into a fixed-length timer so a short swing still plays the full bite clip. Its shark bite is an "instant" state (weight 1 at once). [v] — NAT `world/entity/AnimationTimer.java` L1-18; `world/entity/animal/greatwhiteshark/GreatWhiteShark.java` L107, L331-339

### Inferences

- Nublar's one-shot byte already avoids per-tick packets. What is missing is (1) named phases with the damage tick at a phase boundary, rather than the server and the clip agreeing by convention, and (2) anchoring the clip time to the entity tick when it started, so a dinosaur that comes into view mid-attack shows the right frame. Mowzie's `playAnimation` does (2) with a tick-offset override. [inf]
- A recovery-phase "chain again" check gives combo bites (raptor double slash) with one extra packet. [inf]
- The C&C range re-check at the hit tick is a cheap fix for bites that land after the target has moved away. [inf]

### Gaps

- None of the mods uses GeckoLib custom-instruction keyframes to trigger server damage. Mowzie uses a sound keyframe handler only for client sounds (Q5).

## Key question 4: Turning, start/stop and speed-matched gaits

### Takeaway

No mod has turn-in-place or run-to-stop skid clips. Turning is handled procedurally: Naturalist eases a visual body yaw behind the real yaw and banks the body into the turn; Dragon Survival and Mowzie bend tail and neck from yaw change. Dragon Survival solves the gait-rate problem exactly: when it changes the clip speed, it shifts the tick offset so the current phase stays the same. Mowzie Elokosa blends four directional walk cycles by the dot product of motion with the body axes.

### Cited Findings

- Dragon Survival's `setAnimationSpeed` helper: if the new speed differs, it (1) reads how far the clip has run since its tick offset; (2) moves the offset so that distance times old speed equals the new distance times new speed; (3) sets the new speed. The controller's tick offset is reached through a mixin accessor. [v] — DS `util/AnimationUtils.java` L17-28
- Speed is matched for walk, run, sneak, swim and climb: the rate is a base plus a clamped term from horizontal speed over a per-gait base speed, divided by a size factor (bigger dragons play slower), then clamped between config minimum and maximum. A comment says a minimum is needed because zero speed breaks the state machine. [v] — DS `common/entity/DragonEntity.java` L789-798
- Naturalist's movement rate is a base times limb-swing amount over a reference limb swing, clamped between a minimum (0.4 by default, 0.1 for swimmers) and 2.0. The reference comes from the movement-speed attribute. [v] — NAT `client/model/NaturalistEntityModel.java` L99-110
- Naturalist's shark and whale rotate the whole renderer by the eased render yaw from `BodyChain`, so the body sweeps through a turn instead of snapping, and roll banks it. The shark also uses that yaw for its attack cone and part placement. [v] — NAT `client/renderer/entity/GreatWhiteSharkRenderer.java` L25; `world/entity/animal/greatwhiteshark/GreatWhiteShark.java` L98-101, L351-352, L406-412
- Mowzie directional walk: each tick, motion is rotated into the body frame and four weights are taken as the positive parts of its dot products with forward, back, left and right. The model plays four code-driven walk cycles scaled by those weights, by the locomotion controller bone, and by a walk/run switch controller bone. [v] — MM `server/entity/MowzieGeckoEntity.java` L80-95; `client/model/entity/ModelElokosa.java` L144-160

### Inferences

- Nublar's `MovementGait` caps the rate change so the phase jump stays under an error bound. The Dragon Survival offset shift removes the phase jump completely, so Nublar could follow speed changes at once. It needs access to the controller's tick offset (an accessor mixin in GeckoLib 4; check the GeckoLib 5 controller API before the port). [inf]
- An eased visual yaw plus a bank roll is a quick win for big theropods turning on the spot, before any turn clip is authored. [inf]

### Gaps

- No evidence of stop or skid transitions, or of foot-phase-aware gait switching, in any of the five.

## Key question 5: Keyframe events, one-shots and layering

### Takeaway

GeckoLib sound keyframes are used lightly (Mowzie Sculptor). Naturalist's code-side "sound track" is a clean design that does not need GeckoLib events: a list of (time, sound, volume, pitch) cues per clip, fired when the clip time crosses a cue, with correct loop wrap. Layering is mostly separate controllers (Dragon Survival bite, breath, emotes, continuous) or code layers gated by controller bones.

### Cited Findings

- Mowzie Sculptor sets a sound keyframe handler on its controller that maps keyframe sound names ("make_gauntlet_effects", "make_gauntlet_piece", "clap1") to mod sounds with random pitch. [v] — MM `server/entity/sculptor/EntitySculptor.java` L768-781
- Naturalist `AnimationSoundPlayer`: per tick on the client, for each (state, track) pair, it (1) reads the clip time; (2) treats a restart or a backwards jump as "before the first cue"; (3) for a one-shot clip fires cues with previous time less than cue time at or before now; (4) for a looping clip compares how many whole loops have passed since the cue at the previous and current times, so cues fire once per loop even across wrap. It plays a local sound, so there is no packet. Rat walk has four step cues in a 0.5 s loop; run has two in 0.625 s. [v] — NAT `world/entity/AnimationSoundPlayer.java` L1-74; `world/entity/AnimationSoundTrack.java` L1-68; `world/entity/animal/rat/Rat.java` L99-112, L512-519
- C&C uses triggerable animations on the main controller (stag beetle hit) or on a dedicated controller that otherwise stops (leaf insect eat). [v] — CC `common/entity/StagBeetleEntity.java` L180-200; `common/entity/LeafInsectEntity.java` L188, L294-299
- Dragon Survival "continuous" controllers play always-on clips (wind, breathing) beside the main clip. [v] — DS `common/entity/DragonEntity.java` L136-141

### Inferences

- For Nublar footsteps, Naturalist's cue list keyed by clip time is better than GeckoLib sound keyframes when animation files are baked densely by an exporter: cues stay in Java, survive re-export, and can scale volume by size. GeckoLib keyframe events need the cue in the JSON. [inf]
- A dedicated always-on controller for breathing would let Nublar move `IdleSway` into a clip, or keep it in code; both are valid. [inf]

### Gaps

- No mod triggers screen shake from a keyframe. BOMD does shake and particles from server timed events and client status handlers, not from animation data (not read in detail).

## Key question 6: Performance for big rigs

### Takeaway

Little explicit LOD in any of the five. The useful tactics are: skip evaluating a faded-out state (Naturalist, under 0.05 weight), reset a physics chain instead of simulating after a culled gap (Mowzie), keep cheap procedural state ticking off screen so it is right when the mob appears (Dragon Survival), and cache bone and part lookups (Naturalist).

### Cited Findings

- Naturalist caches part lookup by name and the full part array for resets. [v] — NAT `client/model/NaturalistEntityModel.java` L32-71
- Naturalist skips any clip whose weight is at or under 0.05. [v] — NAT `client/model/NaturalistEntityModel.java` L116-123
- Mowzie chain: snap after a gap longer than one tick; skip the physics step when paused or when the time has not changed (several render passes in one frame). [v] — MM `client/model/tools/dynamics/GeckoDynamicChain.java` L129-134, L256-258; `client/render/entity/MowzieGeoEntityRenderer.java` L48-55
- Dragon Survival ticks its motion history for every player each client tick, on or off screen. [v] — DS `client/render/ClientDragonRenderer.java` L344-352

### Inferences

- If Nublar adds a damped chain, tick it per client tick (as Naturalist and Dragon Survival do), not per frame, and snap after an off-screen gap. That keeps cost fixed per animal and avoids pops. [inf]

### Gaps

- No distance LOD for animation found. Bone counts of these rigs were not measured.

## Techniques Nublar could adopt (ranked)

| Rank | Technique | Source | Improves | Effort |
|---|---|---|---|---|
| 1 | Damped tail and neck follow from body yaw change: per-segment ease toward the parent with falling gains, plus pitch follow; added in `setCustomAnimations`, ticked per client tick, interpolated by partial tick | Naturalist `BodyChain` (MIT) | Every walk, run, turn and idle; tails stop looking rigid | S |
| 2 | Controller bones: empty bones whose keyed X value is read in code as a 0 to 1 weight for head look, sway, tail physics | Mowzie (idea only, ARR) | Roar, bite, drink, eat, rest clips can switch off head look or sway without Java | S (code) plus M (keying) |
| 3 | Phase-preserving speed change: shift the tick offset when changing animation speed | Dragon Survival (idea only) | `MovementGait` walk and run foot sync; removes the rate-change cap | S to M (needs controller accessor; recheck for GeckoLib 5) |
| 4 | Anchor one-shot clip time to the server start tick, so late-rendered animals show the right frame | Mowzie `playAnimation` (idea only) | Attack, roar, call one-shots seen when entering view | M |
| 5 | Named attack phases (wind-up, active, recovery) with damage at the active boundary, range re-check at the hit tick, optional combo jump in recovery | Mowzie sections, C&C goal (MIT), BOMD events (LGPL) | Bite and slash timing; combo attacks | M |
| 6 | Clip-time sound cues in code with loop-safe crossing | Naturalist `AnimationSoundPlayer` (MIT) | Footsteps on walk and run; bite snaps; no JSON edits | S |
| 7 | Eased visual body yaw plus bank roll in turns | Naturalist shark (MIT) | Turning in place and wide turns for large theropods and sauropods | S |
| 8 | Weight-faded idle by limb-swing amount and per-transition blend lengths | Naturalist, Dragon Survival | Stand-walk start and stop; fewer hard flips | S |
| 9 | Head look split over neck segments with clamps, world-level head option | Mowzie Umvuthi, C&C red panda (MIT) | Long necks (sauropods), grazing and drinking | S |
| 10 | Four-corner terrain sample for body pitch, roll and per-leg drop | Naturalist `TerrainLegSolver` (MIT) | Standing and walking on slopes and steps for quadrupeds | M |
| 11 | Molang handler bones driven by smoothed yaw-change and vertical-speed queries, tuned per clip | Dragon Survival (idea only; assets restricted) | Per-clip tail and neck response | L (rig and clip edits) |
| 12 | Verlet chain blended against the animated pose by a controller bone | Mowzie (idea only, ARR) | Loose tails, throat skin, feathers on death and fall | L |
