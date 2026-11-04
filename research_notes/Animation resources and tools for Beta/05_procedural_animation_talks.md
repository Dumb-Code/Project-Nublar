# Procedural animation talks for the F2.3 code layers

Research note for the animation resources report. Written 2026-11-03.

Scope: item 5 of the handoff. David Rosen's GDC 2014 talk "An Indie Approach to Procedural Animation" (Overgrowth, Wolfire), and similar public talks and write-ups: Rain World, Ubisoft IK Rig, procedural locomotion, quadruped and dinosaur work. For each source: key techniques, which of our 7 layers each helps, and a verdict. Flags for foot slide and for "heavy animals read as heavy".

The 7 layers (from the brief): 1 clips, 2 leg IK, 3 neck and tail bend, 4 head look, 5 neck reach, 6 tail lag, 7 breathing sway.

Labels:

- `[doc]`: from a page, paper, talk, video or README. The link is next to the fact. For talks I read the free GDC video transcript (YouTube auto-captions, read with the `video_analyze` tool). I give the minute mark of the caption. Auto-captions have errors, so I paraphrase.
- `[v]`: checked in source code. Not used in this note. I read no source code for it.
- `[inf]`: my inference.

Public sources only. I read no game files and no game installs. I copy no code. "min N" means minute N of the talk video.

---

## 1. Flags: foot slide and weight

### 1.1 What would cut foot slide

| # | Lever | Where it comes from | Layer | Verdict |
|---|---|---|---|---|
| F1 | Play each gait clip at ground speed divided by the clip's authored stride speed. Gate walk and run (the gait clips that exist). Do not blend gaits whose feet are out of phase. | Rosen min 5 to 6. Frey min 6 to 7. | 1 | **use** |
| F2 | At a stop, freeze at a planted pose and plant the feet with IK at their pre-additive world position. Then play the keep-alive idle on bones above the legs only. | Frey min 15 to 22. | 1, 2, 7 | **use** |
| F3 | Order check: layer 7 runs after layer 2. If breathing sway rotates a bone that parents a leg, planted feet move with it. Keep sway on bones that do not parent a leg, or run IK after sway. | Frey min 19 to 22. [inf] for our rigs. | 2, 7 | **use** (check the model hierarchy) |
| F4 | No turning in place: cap the body turn rate by speed, so the animal always moves a little. | Frey min 11. HZD min 27 to 31. | none (AI and movement) | **new idea, not in the plan.** Lead call. It touches pathing. |
| F5 | Fade the layers out with distance. Do not hard-cut. | Skovbo Johansen thesis section 8.6. | 2, 4 to 6 | **use** |
| F6 | Scale the footstep length, not the clip speed, for size variants. | IK Rig min 14 to 15. | 1, 2 | **reference** (use if we have growth or size genes) |
| F7 | Predict the next foothold from velocity and turn rate. Smooth more as the foot nears landing. | Skovbo Johansen section 7.2. IK Rig min 27 to 30. | 2 | **reference** (compare with the CHAOS foot planner) |
| F8 | Lock the toe to the landing point during contact, blend with inertialization. Soft reach clamp. | Holden foot locking (see `04_springs.md` section 2.2). | 2 | **reference** |

### 1.2 What makes heavy animals read as heavy

| # | Lever | Where it comes from | Layer | Verdict |
|---|---|---|---|---|
| H1 | Stagger the lag: head and neck first, body later, tail last. HZD used 6 frames for the body and 12 for the tail. | HZD min 32 to 33. | 4, 6 | **use** as a tuning rule |
| H2 | Per-bone softness: a different spring setting for each bone. Heavy: long half-life, no overshoot. | Rosen min 12 to 13. | 6 | **use** |
| H3 | A landing or footfall spring on the body: a kick on contact, then settle. No landing clip. | Rosen min 8. | new (next to 6) | **new idea, not in the plan.** Lead call. It must run before leg IK. [inf] |
| H4 | Body height follows the legs that carry the weight, not all legs. | Skovbo Johansen section 7.3.6. Frontier (JWE). | 2 | **reference** (check the CHAOS planner) |
| H5 | Light and heavy rule sets, blend between the two by weight, tiredness, strength. | IK Rig min 12. | 7 and new | **reference** |
| H6 | The head leads with physical weight. | Frontier (JWE). | 4 | **use** (our spring + weight curve) |
| H7 | A pose that "leans into" a turn. | Rosen min 5. HZD min 29 to 30. Frey min 9 to 10. | new | **reference.** Turn lean waits until after Beta (D337, from the commit log in the brief). |

---

## 2. Sources

### 2.1 David Rosen, "Animation Bootcamp: An Indie Approach to Procedural Animation", GDC 2014

- What and where: a 26 minute talk by the Wolfire co-founder. Free video: https://www.youtube.com/watch?v=LNidsMesxSE (GDC channel). GDC Vault: https://www.gdcvault.com/play/1020583/Animation-Bootcamp-An-Indie-Approach . Wolfire post: https://www.wolfire.com/blog/2014/05/GDC-2014-Procedural-Animation-Video/ (4 May 2014, only links the video). [doc]
- Help for F2.3: layer 1 (speed-matched gait), layers 5 and 6 (spring between poses, per-bone softness), layer 4 (look-at). [inf]
- Use now: the ideas yes. There is no code in the talk. [doc]
- Cost and limits: made for one humanoid with 13 keyframes in total. [doc] min 10. Most of the talk is about a player character, not an AI animal. It has no quadruped work. It has no numbers. [doc]
- Verdict: **use** (a few ideas), **reference** for the rest.

Key techniques, in a few lines. [doc]

- Movement first. Controller input sets acceleration. The animation drapes on the physics and never changes it. (min 3 to 4)
- The gait is driven by distance travelled on the ground, like a measuring wheel. Two keyframes (a passing pose and a reach pose) are enough. The feet never float. (min 5)
- A slower and a faster gait blend in sync, and their stride lengths blend too. Bounce falls with speed. (min 6)
- Cubic interpolation between keyframes keeps speed continuous. Linear interpolation looks stiff. (min 7)
- A spring and damper blend between stand and crouch, with two numbers: stiffness and damping. The spring is separate from the poses, so it works for any pair of poses. (min 7 to 8)
- A landing is a downward kick into the crouch spring. There is no landing clip. (min 8)
- Look targets use IK, so the head and the torso can face different ways. (min 12)
- Each bone has a "softness" for secondary physics, set per animation. (min 12 to 13)
- Active ragdolls: pose matching, animation matching, flailing keyframes near surfaces. (min 14 to 17)
- Use IK only where needed. He prefers authored extremes with interpolation, or an animation layer on top. (min 25)

| Technique | Layer | Verdict | Why |
|---|---|---|---|
| Movement first, animation on top | all | **use** | Same rule as the plan: layers add deltas, none writes an absolute angle. |
| Gait driven by ground distance | 1 | **use** | Cuts foot slide (F1). One stride speed per gait clip. GeckoLib clip speed can be set per frame. [inf] |
| Slower and faster gait in sync | 1 | **reference** | Needs matched foot phase between clips. Frey's gating is simpler (2.2). |
| Lean into acceleration | new | **reference** | Close to D337 (turn lean after Beta). Scale by mass. |
| Spring between two poses | 5, 6 | **use** | Same family as the spring note. Tune by two numbers. |
| Landing as a kick into a spring | new | **reference, lead call** | Makes weight readable. Needs a root offset that leg IK then re-plants. [inf] |
| Look-at IK, head apart from torso | 4 | **use** | Confirms layer 4. |
| Per-bone softness | 6 | **use** | Put half-life and damping ratio per bone in the species data. |
| Cubic interpolation between keys | 1 | **drop** | An authoring and engine matter. |
| Two-bone IK by trigonometry | 2 | **drop** | We have the CHAOS solver. |
| Active ragdoll, delayed ragdoll | none | **drop** | Wrong scope. Costly for 100 animals. |
| Extract repeated animator work into curves | process | **reference** | Good habit, no code. |

### 2.2 Gwen Frey, "Animating Quadruped Characters in The Flame in The Flood", GDC 2016

- What and where: a 31 minute talk on a boar and a wolf in Unreal Engine 4. Free video: https://www.youtube.com/watch?v=Bs046-TWMhA . GDC Vault: https://gdcvault.com/play/1023209/Animating-Quadruped-Characters-in-The . [doc]
- Help for F2.3: layer 1 (gait and stop), layer 2 and layer 7 (order), the AI turn rule. This is the most useful source here for foot slide. [inf]
- Use now: yes. The ideas need no special tool. [inf]
- Cost and limits: a top-down camera hides some errors. The speaker could not afford four IK per animal and moved the hooves under the root instead. A rig change like that is not open to us. [doc] min 20 to 21, 25 to 26. The turn trick needed a programmer to change the path. [doc] min 11 and 28.
- Verdict: **use**.

Key techniques. [doc]

- Solve three things first: moving forward, turning, and moving to idle and back. (min 1)
- Moving forward: scale the clip play rate by ground speed. Switch from walk to trot to gallop with a quick blend. Do not blend gaits whose feet are out of phase. Foot slide at the switch is hidden because the animal is fast. (min 6 to 7)
- Foot slide is most visible at low speed, so give slow speeds more clips. (min 4, 14)
- Turning: take the angle between the travel direction and the facing. Split it over the spine joints. Scale the turn rate by speed. Never turn in place; always move forward a little. (min 9 to 11)
- Stopping: at speed zero the play rate is zero. Blend to a copy of that frame with the feet pulled to the ground. Lower the body a little. Then play an additive keep-alive idle on the torso, head and tail, not on the feet. (min 15 to 19)
- An additive on the hips makes planted feet swim. Fix: run IK on each foot to its world position from before the additive. (min 19 to 22)
- Slopes: tilt the pelvis to the ground normal, then plant the feet with IK. (min 30)

| Technique | Layer | Verdict | Why |
|---|---|---|---|
| Clip play rate = ground speed over authored speed; gate gaits | 1 | **use** | F1. Matches Rosen. |
| Test foot slide at low speed first | 1, 2 | **use** | Slide is most visible there. |
| Stop: freeze at a planted pose, pull feet down with IK | 1, 2 | **use** | F2. Do it with IK, not a new clip. We must not plan around clips that do not exist. |
| Keep-alive idle on bones above the legs only | 7 | **use** | F3. Our breathing sway is this idle. |
| IK feet to pre-additive world positions | 2 | **use** | F3. Check layer order. |
| Turn rate scaled by speed, no pivot | none (AI) | **new idea, lead call** | F4. Cuts slide and adds weight. Touches pathing. |
| Spine arc from heading minus facing | new | **reference** | Our layer 3 covers neck and tail only. Turn lean waits (D337). |
| Blend space on turn angle and speed | none | **drop** | Needs turn clips. |
| Slope: tilt pelvis, IK feet | 2 | **reference** | Compare with the CHAOS planner. |

### 2.3 Alexander Bereznyak, "IK Rig: Procedural Pose Animation", GDC 2016 (Ubisoft)

- What and where: a 61 minute talk. Free video: https://www.youtube.com/watch?v=KLjTU0yKS00 . GDC Vault: https://www.gdcvault.com/play/1023279/IK-Rig-Procedural-Pose . [doc]
- Help for F2.3: layers 4, 6 (spring and constraint nodes, look-at), layer 2 (footstep scaling, foot prediction), layer 3 (look ahead). [inf]
- Use now: no. It is a full pipeline: map every rig to IK chains, then apply rules at run time. We have 6 hand-made rigs. [inf]
- Cost and limits: the examples are prototypes. The speaker says some are exaggerated on purpose. The tech had passed proof of concept and was in run-time use in a few projects. [doc] min 1 to 2. A Game Anim write-up (https://www.gameanim.com/2018/02/03/ik-rig-procedural-pose-animation/) calls the talk theoretical, with pre-rendered examples. [doc]
- Verdict: **reference**.

Key techniques. [doc]

- Convert animation to IK chains. Apply "rules" on top (crouch, aim, tired). Rules mix. (min 3 to 4, 11)
- Constraint nodes at run time: a spring and damper for flapping parts, an orientation constraint (a tail base follows the pelvis and thigh rotation), a look-at, a driver. (min 8 to 9)
- Weight, strength and tiredness change the rule. The artist makes a light and a heavy rule and blends between them. (min 12)
- Footstep scaling: scale the character without scaling the animation timing. Scale only the footstep length. He says retiming by more than 10 to 15% loses the sense of mass. No foot slide. (min 14 to 15)
- Look ahead with ray casts for soft avoidance and anticipation. (min 24 to 26, 33)
- "Nostradamus mode": store, per frame and per foot, the time since lift and the time to plant. Then bend the foot path to land on a chosen spot. (min 27 to 30)
- Quadrupeds: the same locomotion runs on dog, horse, elephant by scaling footsteps. (min 34 to 36)

| Technique | Layer | Verdict | Why |
|---|---|---|---|
| Spring and damper node for secondary motion | 6 | **use** | Confirms layer 6. |
| Tail base follows the pelvis rotation | 6 | **reference** | A cheap driver for the first tail bone. |
| Look-at at run time | 4 | **use** | Confirms layer 4. |
| Light and heavy rule, blend by weight or tiredness | 7 and new | **reference** | Fits the stamina rule below a third. Heavy flag H5. |
| Footstep scaling for size variants | 1, 2 | **reference** | F6. Use if size genes exist. |
| Foot path bent to hit a chosen foothold | 2 | **reference** | F7. Needs per-clip foot data (a baked table). |
| Ray cast ahead, soft avoidance | 3 | **use** | Confirms the bend-away-from-blocks layer. |
| Map any rig to a shared IK rig; motion DNA | none | **drop** | Pipeline cost, no gain for 6 species. |

### 2.4 Richard Oud, "Bringing Life to the Machines of Horizon Zero Dawn", GDC 2018 (Guerrilla)

- What and where: a 58 minute talk on the big machine animals. Free video: https://www.youtube.com/watch?v=50mIKB-NACU . GDC Vault: https://gdcvault.com/play/1025040/Animation-Bootcamp-Bringing-Life-to . Game Developer: https://www.gamedeveloper.com/art/video-breathing-life-into-the-machines-of-i-horizon-zero-dawn-i- . [doc]
- Help for F2.3: heavy animals (layers 4 and 6). Large machines are the closest published case to a T. rex or a Brachiosaurus. [inf]
- Use now: the lag rule yes. The turn cycles no (they need turn clips). [inf]
- Cost and limits: a console AAA team, authored in Maya and a node-based animation tool. No numbers in seconds, only frames. [doc] min 31 to 33
- Verdict: **use** (lag rule), **reference** (rest).

Key techniques. [doc]

- Aim: bulky, with weight, yet responsive. Weight against response was a long back-and-forth. (min 13 to 14)
- Turning: authored 90 and 180 degree turn cycles. A new AI mover trades forward speed for sideways speed when the path bends. Result: little foot slide when cornering. A second talk (Julian Berteling, AI Summit) covers the mover. (min 27 to 31)
- Staggered lag: on a direction change the head and neck play at once, the body plays 6 frames late, the tail 12 frames late. A little foot slide appears and recovers fast. A tail that flapped with the body looked light; the delay fixed it. (min 32 to 33)
- Break up the cycle: three variants of the walk (centre, left, right) blended by a smoothed random value that changes every 1.5 seconds. (min 33 to 34)

| Technique | Layer | Verdict | Why |
|---|---|---|---|
| Head and neck first, body later, tail last | 4, 6 | **use** | H1. In frames at the game's animation rate; at 30 fps that is 0.2 s and 0.4 s. I do not know the game's rate. [inf] Set tail half-life about double the body's. |
| 90 and 180 degree turn cycles with a mover | none | **reference** | F4. Needs turn clips. Do not plan for clips that do not exist. |
| Speed trade-off in the AI mover | none (AI) | **reference** | Same idea as Frey's turn rule. |
| Random blend of cycle variants | new | **reference** | Needs variant clips. A smoothed random lean (a critical spring on a random goal) is the clipless form. [inf] |
| Lean into the turn | new | **reference** | D337. |

### 2.5 Rune Skovbo Johansen, "Dynamic Walking with Semi-Procedural Animation", GDC 2009, and his thesis

- What and where: GDC 2009 slides https://www.gdcvault.com/play/966/Dynamic-Walking-with-Semi-Procedural . Thesis "Automated Semi-Procedural Animation for Character Locomotion", Aarhus University, 25 May 2009: https://runevision.com/thesis/rune_skovbo_johansen_thesis.pdf . Project page: https://runevision.com/tech/locomotion/ . I read the thesis text. [doc]
- Help for F2.3: layer 2 (foot planner, weight-carrying body height), LOD. The best written source for foot slide here. [inf]
- Use now: as a reference to audit the CHAOS planner. The full system (cycle analysis, blends, interpolation) is a locomotion engine, not a layer. [inf]
- Cost and limits: the thesis claims the methods prevent any form of footskate (section 7.6). Tested on 2 and 4 legs. A 3-segment quadruped leg needs a slower numeric IK solver. Dozens of characters ran at high frame rates on a 2009 laptop (section 8.6). It can be turned off per character without popping, so use it near the camera only (section 8.6). The system is Unity C# from 2009. [doc]
- Verdict: **reference**.

Key techniques. [doc]

- Offline: analyse each walk cycle for foot lift and impact times, stance time, and speed. (abstract, chapter 4)
- Runtime: blend cycles by speed and turn rate, then fix the legs with IK. (abstract, chapters 5 and 7)
- Footprint prediction: predict the time, place and angle of the next footfall from velocity and turn. The prediction updates until the foot lands. Smooth it more as the foot nears the ground. Two ray casts per foot (heel and toe). (section 7.2)
- Supporting ground height: a weighted average of the ground under the feet that carry the weight, with little weight for swinging feet. This gives "a good sense of weight". (abstract, section 7.3.6)
- Root height from the feet, not from the physics body. (section 7.4.2)
- Start and stop with no transition clips: each leg parks at its own stance time, one after another. (section 7.5)

| Technique | Layer | Verdict | Why |
|---|---|---|---|
| Footprint prediction with smoothing | 2 | **reference** | F7. Audit the CHAOS planner against it. |
| Supporting ground height | 2 | **reference** | H4. Check how the planner sets body height. |
| Root height from the feet | 2 | **reference** | Same. |
| Fade the system out far away without popping | 2, 4 to 6 | **use** | F5. |
| Legs park one by one to stop | 1, 2 | **drop** | Needs per-leg cycle time. A GeckoLib clip plays all bones together. [inf] Use Frey's freeze plus IK plant (F2). |
| Cycle analysis and blending engine | 1 | **drop** | A full locomotion system, wrong size for F2.3. |

### 2.6 Frontier Developments, Jurassic World Evolution animation write-up

- What and where: "Animation: Frontier explains how the studio brought its dinosaurs to life in Jurassic World Evolution", by Oliver Powell (Frontier animation programmer), 2 April 2019: https://mcvuk.com/?p=30444 . [doc]
- Help for F2.3: layer 4, layer 2. It is the only shipped dinosaur game write-up I found. [inf]
- Use now: as confirmation. It has no numbers and no code. [doc]
- Cost and limits: the article does not discuss foot slide, LOD or cost. [doc]
- Verdict: **reference**.

Key techniques. [doc]

- A head look system twists bones up the neck to point the head. The physics engine adds weight, so the head leads the move.
- Physics drives ragdoll on long tails that drag on the ground.
- A procedural foot planting system with IK. Feet lift in arcs. Body height and rotation shift with the weight each foot carries. Quadrupeds and bipeds use different modes. The pose is rebuilt each frame with little saved state, so terrain edits show at once.
- Hand-keyed locomotion sets blend for speed and turn. Additive layers add twitches, head shakes and roars.

| Technique | Layer | Verdict | Why |
|---|---|---|---|
| Head look twisting the neck, with weight | 4 | **use** | Same as our weight curve plus spring (H6). |
| Foot planting where body height follows the carrying feet | 2 | **reference** | H4. |
| Tail ragdoll on the ground | 3, 6 | **drop** | Physics cost for 100 animals. |
| Partial and additive layers for twitches and shakes | 1 | **use** | Already in the plan (additive overlay for head clips). |

### 2.7 Rain World (Jakobsson and Therrien), GDC 2016

- What and where: GDC Vault session page https://www.gdcvault.com/play/1023475/Animation-Bootcamp-Rainworld-Animation . Free 8 minute interview with Jakobsson at GDC 2016: https://www.youtube.com/watch?v=D2r7bfaJmYc . Later interview: https://unity.com/blog/exploring-procedural-design-rain-world (25 Sep 2025). [doc]
- Help for F2.3: tail lag only. The game is 2D. [inf]
- Use now: no.
- Cost and limits: **thin.** The Vault page names no techniques. The only YouTube copy of the talk that I could transcribe (a 55 minute Anifilm re-run, https://www.youtube.com/watch?v=-iXwvoFhPuU ) has unusable captions. I did not find slides. The facts below come from the interview and from Rosen's description of Rain World at his min 20. [doc]
- Verdict: **drop** (reference for the tail idea).

Key techniques. [doc]

- A creature is a set of points held at fixed distances that drag or push each other. A drawn puppet follows the points. (interview, min 2 to 3)
- Rosen's description: the animal tilts into its direction, limbs reach the nearest surface with IK, and the tail is always simulated, so every state change is smooth. (Rosen min 20)

| Technique | Layer | Verdict | Why |
|---|---|---|---|
| Tail always simulated | 6 | **reference** | Confirms layer 6 (a spring per tail bone). |
| Distance-constrained point chain | 3, 6 | **drop** | Rope physics. The CHAOS branch has a rope; the audit covers it. |
| 2D puppet drawn on points | none | **drop** | 2D. |

### 2.8 Smaller sources

| Source | What it is | Help | Verdict |
|---|---|---|---|
| Roche and Torres-Cros, "Fitting the World: A Biomechanical Approach to Foot IK", GDC 2016 (Ubisoft). Vault: https://www.gdcvault.com/play/1023316/Fitting-the-World-A-Biomechanical [doc] | A predictive foot IK for Assassin's Creed. Reactive foot IK reacts late and jerks the knees in fast moves. I read only the abstract. I did not see the talk. | Layer 2, F7. | **reference** (content not checked) |
| Hecker et al., "Real-time Motion Retargeting to Highly Varied User-Created Morphologies", SIGGRAPH 2008 (Spore). https://doi.org/10.1145/1360612.1360626 [doc] | Stores motion in a body-independent form and maps it to each creature at run time with an IK solver. I read only the abstract and summaries. | None. We know our 6 bodies. | **drop** |
| Beasts of Bermuda, "Tail Tracking", 3 Apr 2017. https://beastsofbermuda.com/blog/post/tail-tracking/ [doc] | An indie dinosaur game reuses a head-tracking clip on the tail. The bend sits near the tail base. A banking layer widens the range. | Layer 3, tail weights. | **reference** (idea: put more tail bend weight near the base) |

---

## 3. Layer map

What the sources say per layer. [inf]

| Layer | From the talks | Verdict |
|---|---|---|
| 1 Clips | Speed-matched play rate and gait gating (Rosen, Frey). Freeze and plant at a stop (Frey). | use |
| 2 Leg IK | Plant to pre-additive positions (Frey). Predict footholds (Skovbo Johansen, IK Rig). Weight-carrying body height (Skovbo Johansen, Frontier). Fade by distance (Skovbo Johansen). | use the first and last; reference the middle two |
| 3 Neck and tail bend | Ray cast ahead for soft avoidance (IK Rig). | use (confirms) |
| 4 Head look | Look-at apart from torso (Rosen). Head leads (HZD, Frontier). Head and neck first, body later (HZD). | use (confirms) |
| 5 Neck reach | Nothing specific in these sources. | none |
| 6 Tail lag | Per-bone softness (Rosen). Tail lags most (HZD). Spring node (IK Rig). Tail always simulated (Rain World). | use (confirms) |
| 7 Breathing sway | Keep-alive on bones above the legs only (Frey). Light and heavy rules (IK Rig). | use (with the order check F3) |
| New, not in the plan | No pivot turns (Frey, HZD). Landing spring (Rosen). Lean into turns (D337 waits). Footstep scaling for size variants (IK Rig). | lead call |

---

## 4. What I did not do

- I did not see slides for Rain World. I did not see the Roche and Torres-Cros talk or the Spore talk.
- The talk transcripts are auto-captions. The minute marks are minutes of the caption, not exact times.
- I read no code of this mod. Items marked "check the model hierarchy" or "check the planner" need a look at the rigs and at the CHAOS foot planner.
- I found no talk on dinosaur animation by a game studio. The Frontier write-up is the closest.

---

## Verdicts

- Rosen, GDC 2014 (Overgrowth): **use** the speed-matched gait, the spring between poses and the per-bone softness. **Reference** the landing spring and the lean. **Drop** ragdoll and the rest.
- Frey, GDC 2016 (quadrupeds): **use**. Gait gating, freeze and IK-plant at a stop, keep-alive idle above the legs, and the layer order check. New idea for the lead: no pivot turns.
- Bereznyak, IK Rig, GDC 2016: **reference**. Footstep scaling, light and heavy rules, foothold prediction.
- Oud, HZD machines, GDC 2018: **use** the staggered lag (head, then body, then tail). **Reference** the turn cycles.
- Skovbo Johansen, GDC 2009 and thesis: **reference**. Audit the CHAOS planner against the footprint prediction and the weight-carrying height. **Use** the fade by distance.
- Frontier, Jurassic World Evolution write-up (2019): **reference**. Confirms head look with weight and weight-aware foot planting.
- Rain World (Jakobsson, Therrien), GDC 2016: **drop**. Thin sources, 2D game. Keep only the "tail always simulated" idea.
- Roche and Torres-Cros, foot IK, GDC 2016: **reference**. Abstract only.
- Spore retargeting paper (Hecker et al., 2008): **drop**.
- Beasts of Bermuda tail tracking (2017): **reference**. Put more tail bend weight near the base.
