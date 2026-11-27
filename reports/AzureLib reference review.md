# AzureLib is a reference for Nublar's code layers, not a library to adopt

Date: 2026-11-27. Status: research report for the project lead. It changes no code. Register row D403 holds what went into the plan.

AzureLib is an animation library that began as a fork of GeckoLib 4. From 23 October to 26 November 2026 it added bone hitboxes (v3.2.0), a pose-layer API with IK, look-at, joint limits, spring chains, sockets and blend spaces (v3.2.3), spring colliders (v3.2.4) and clip-set layer weights (v3.2.8). Nublar stays on GeckoLib 5. The lead said so on 2026-11-27: "I'm not proposing we move from geckolib, I just want to take what we can or use azurelib as a reference point to write the code for our own needs/plans."

Six of the nine code layers of F2.3 have a working AzureLib counterpart: head overlay, breathing sway, leg IK, head look, neck reach and tail lag. The footfall bounce has none. The two synced layers, body pose and the neck and tail bend, have none either, because AzureLib poses on the client only. Its hitboxes take their positions from players' clients, which does not fit the server-truth parts of F2.1.

**Labels.** `[v]` means verified in source at the commits under Sources. `[doc]` means AzureLib's docs or changelog. `[inf]` means inference. Nobody ran a benchmark. Every speed figure is the AzureLib author's own.

**Names.** "The plan" is [engineering-backlog.md](../docs/plan/engineering-backlog.md). "The design" is [dinosaur-hitboxes-and-movement-design.md](../docs/dinosaur-hitboxes-and-movement-design.md). New specifications are AZ1 to AZ10. Version numbers are AzureLib's 1.21.1 line.

## Ten changes go into the plan (D403)

| # | Change | Package | Spec |
| --- | --- | --- | --- |
| 1 | One layer runner: start from the clip pose, run the layers in order, write once. Each layer has a bone list, a weight and a fade, and restarts cleanly | F2.3 | AZ1 |
| 2 | Head overlay: add the clip's offset from rest, multiply scale, and test a bone that has a rest rotation | F2.3 | AZ2 |
| 3 | Head overlay sampler: continue from the last keyframe, no object per frame | F2.3 | AZ9 |
| 4 | Tail lag grows toward the tip, so the tail bends | F2.3 | AZ3 |
| 5 | Tail keep-out: the lagged tail stays out of the animal's own legs and the ground | F2.3 | AZ4 |
| 6 | Control bones: a layer weight returns through its fade when a clip ends early | F2.3 | AZ7 |
| 7 | A draw never steps layer state | F2.3 | AZ8 |
| 8 | Leg IK: the foot target keeps the clip's lift | F2.1 part B | AZ6 |
| 9 | Leg IK: reach and straight-leg guards as unit tests on the kept solver | F2.1 part B | AZ5 |
| 10 | Glow pass draws only lit quads, and only if the client profile asks for it | BH.1 | AZ10 |

## What stays out, and why

| AzureLib feature | Why Nublar leaves it |
| --- | --- |
| Hitbox sync | Clients report each box to the server: 10 floats per box per report, every 2 ticks by default `[v]`. With no player near, hits fall back to the vanilla box `[doc]`. A modified client can shift boxes inside set bounds `[doc]`. The hitbox mixins have no explosion hook `[v]`. F2.1 parts are placed by the server from baked poses and send no part packets |
| Rotated hit boxes | D06 and D158 chose square parts in chains. The rotated-box maths stays a reference for the bake |
| Spring step | AzureLib steps its springs with plain Euler in sub-steps of at most one tick `[v]`. Nublar's exact springs by half-life stay. Only the chain rule (AZ3) and the keep-out idea (AZ4) are taken |
| Ground raycast | One raycast per foot per frame `[v]`. The block-top sampler of D381 stays |
| Head look | It splits the turn evenly over the bones with one yaw and one pitch cap `[v]`. Nublar's weight curve and spring are more detailed |
| Blend spaces | They need several weighted controllers on one bone. F2.2 allows two controllers (D341), and D292 is still open |
| Weighted idle pools | Each client picks its own clip `[doc]`. In Nublar the server owns the pose state |
| Neck reach to an animated target bone | It needs a new bone and keys in every drink and eat clip. Neck reach by code needs no art |
| Clip-set weights field | Control bones (D348) do the same and work in stock Blockbench |
| Bone sockets | GeckoLib 5 gives bone positions (D339), and D343 already listens only when needed |
| LOD preset | F2.2 has its own distance rule. Bone-depth hiding is close to the cube skip of D299, which is pending |
| Engine speed work | It sits inside AzureLib's own animation engine. Only AZ9 and AZ10 carry over |
| Meshes, Simple API, translucency sorting, Better Combat and morph support | No Beta need found |
| Easing | AzureLib registers the same curves as GeckoLib's main branch, plus `bezier` and `bezier_after` `[v]` |

## Version facts

| Branch | Version | Last commit | Hitboxes, pose layers, IK, springs |
| --- | --- | --- | --- |
| 26.3 | 4.1.10 | 2026-11-26 | Yes |
| 1.21.1 (default) | 3.2.9 | 2026-11-26 | Yes |
| 1.20 (Minecraft 1.20.1) | 3.2.10 | 2026-11-26 | Yes |
| 1.18.2 | 3.3.9 | 2026-11-26 | Yes |
| 26.2 | 4.0.6 | 2026-10-16 | No |
| 26.1.2 | 4.0.14 | 2026-10-16 | No |

- The code was read on 1.21.1. Of the 21 files this report cites, 12 are identical on 26.3 apart from package lines, 8 differ by 2 to 16 lines, and the client hitbox hooks differ by 221 lines `[v]`.
- The 26.3 render path is no model for Nublar. AzureLib runs animation and vertex output inside `extractRenderState` and replays the vertices in `submit` `[v]`. GeckoLib 5 extracts data first and draws later.
- AzureLib has no test files and no CI workflow in its repository `[v]`. The posing code is four weeks old. So each change above needs a Nublar test.

## Clean-room implementation specifications

AzureLib is MIT, a permissive precedent, so the lead's rule for restricted repositories does not apply. This report gives specifications anyway. AzureLib writes its bones directly and undoes the writes on the next frame. GeckoLib 5 bones are read-only, so none of its code ports as it is.

Build each one from the description below. **Specify, do not copy.** A start value is given only where a source has one. "Not set" means that the task plan sets it.

### AZ1. Code-layer runner (F2.3). Source: AzureLib pose layer

**In game:** a code layer never drifts, never pops on or off, and never leaves a bone turned after it stops.

- **Inputs:** this frame's clip pose, the layers in the F2.3 table order, each layer's weight.
- **State:** per animal and layer: the weight, its fade, the last tick the layer ran, and the layer's own state.

1. Start each frame from the pose that the clips produced.
2. Run the layers in order. A layer reads the clip pose or the pose so far. It adds its change into a scratch pose, only for the bones in its bone list.
3. Scale every change by the layer's weight. A layer at weight 0 reads and writes no bone.
4. Write the summed changes once, through the bone updater.
5. Nothing that a layer added is part of the next frame's start pose.
6. A layer starts at rest on its first run, after the animal was not posed for a while, and after the layer was off.

| Value | Start | Basis |
| --- | --- | --- |
| Gap that restarts a layer | 10 ticks | AzureLib `AzPoseLayer` `[v]` |
| Fade length of a weight change | Not set | Task plan |

### AZ2. Additive overlay rule (F2.3). Source: AzureLib `AzTransformMath`

**In game:** a head clip over a walk keeps the head's normal tilt.

1. Rotation and position: add (clip value − rest value) × weight to the pose below.
2. Scale: multiply the pose below by 1 + (clip scale ÷ rest scale − 1) × weight.
3. The rest value is never added twice and never dropped.

### AZ3. Tail chain lag (F2.3). Source: AzureLib spring chain since v3.2.4

**In game:** the tail bends and whips. The tip trails more than the base.

1. The first tail bone follows the clip.
2. Each later bone's spring aims at where the clip puts that bone relative to the lagged bone before it, not relative to the body.
3. So the lag adds up toward the tip.

Before v3.2.4 every joint lagged by the same angle, and the chain swung as one stiff piece `[doc]`. Nublar keeps its exact spring per bone and its client-tick step.

### AZ4. Tail keep-out (F2.3). Source: AzureLib spring colliders and ground option

**In game:** a swinging tail never passes through the animal's own legs or into the ground under its feet.

- **Shapes:** one capsule per leg bone, fitted to that bone's cubes at the bake, and the ground plane at foot height.

1. After the lag is known, find the two ends of each tail segment in model space.
2. Test the whole segment against each capsule and the ground plane, not only its joints. AzureLib tests segments so that thin shapes hold `[v]`.
3. If a segment is inside a shape, scale the lag of that bone and of every bone after it toward the clip pose until the segment is clear.
4. Never change the clip pose. If the clip pose itself is inside a shape, leave it.

Step 3 is Nublar's own rule `[inf]`. AzureLib pushes particles out of the shape and works the speed out again. Scaling the lag back is simpler, and it ends at the clip pose, which the artist made.

| Value | Start | Basis |
| --- | --- | --- |
| Capsule size | The leg bone's cubes | Bake |
| Tail thickness | Not set | Task plan |

### AZ5. Leg solve guards (F2.1 part B). Source: AzureLib two-bone solver

**In game:** a knee never flips, and a leg at full stretch never vanishes or shakes.

1. Keep the hip-to-target distance between the difference and the sum of the two bone lengths, each by a small margin.
2. Keep every cosine between −1 and 1 before the arc cosine.
3. A leg that is perfectly straight has no bend plane. Use the species' knee side.
4. Otherwise the knee stays in the plane that the clip put it in.
5. Apply the joint limits after each solver step, not only at the end.

These are tests for the kept CHAOS solver, not a new solver.

| Value | Start | Basis |
| --- | --- | --- |
| Reach margin | 0.0001 model units | AzureLib `AzIkSolvers` `[v]` |

### AZ6. Foot height rule (F2.1 part B). Source: AzureLib ground target

**In game:** a foot that the walk clip lifts stays lifted when the animal crosses a step.

1. Take the foot's height from the clip, above the model's ground plane.
2. Sample the ground under that foot with the D381 sampler.
3. Target height = the sampled ground + the height from step 1.
4. With no ground in range, or an animal in the air, leave the leg as the clip has it.

### AZ7. Layer weight return (F2.3). Source: AzureLib clip-set pose weights, v3.2.8

**In game:** when a roar is cut short, head look eases back in.

1. While a clip keys a layer's weight, the key value wins.
2. When the clip ends or is interrupted, the weight returns to its rule value through the layer's fade.
3. The weight never jumps.

### AZ8. A draw never steps layer state (F2.3). Source: AzureLib v3.2.9 fix

**In game:** a dinosaur drawn in a screen, or drawn twice in one frame by another mod's preview, moves the same as one drawn once.

1. Layer state steps on the client tick only.
2. A draw evaluates the state forward by the partial tick and writes nothing back.
3. An animal that never ticks, such as the preview copy in the Sequencer screen, shows the clip pose with every layer at rest.

AzureLib stepped its springs on every draw and had to add a guard in v3.2.9 `[doc]`. A tick step avoids the fault.

### AZ9. Keyframe cursor (F2.3). Source: AzureLib keyframe channel, v3.1.16

**In game:** nothing to see. A head clip costs less per frame.

1. Keep, per bone and channel, the index of the keyframe used last.
2. The next sample starts at that index and steps forward.
3. On a loop or a jump, run a binary search over keyframe end times that were computed at load.
4. Make no object per sample.

### AZ10. Glow quads (BH.1). Source: AzureLib glow coverage, v3.1.17

**In game:** nothing to see. The glow pass costs less.

1. At load, mark the quads whose UVs cover a lit pixel of the glow mask.
2. The glow pass draws only those quads.
3. A mask with no lit pixel skips the pass.

Build it only if the BH.1 client profile shows the glow pass as a large part of the frame time.

## Sources

- AzureLib, [branch 1.21.1 at e15bc8e](https://github.com/AzureDoom/AzureLib/tree/e15bc8edf0ade114b3169ce295965b681aa9aa81): source, `docs/` and the git history of `changelog.md`. Branch 26.3 at 199a671 and branch 26.2 at cc6d1a9.
- Files read under `common/src/main/java/mod/azure/azurelib/common/`: `animation/pose/AzPoseLayer.java`, `AzTransformMath.java`, `AzPoseModifiers.java`, `animation/pose/ik/AzIkSolvers.java`, `AzIkTargets.java`, `animation/pose/secondary/AzSpringBone.java`, `animation/primitive/AzPoseWeights.java`, `animation/easing/AzEasingTypes.java`, `hitbox/AzHitboxSync.java`, `AzOrientedBox.java`, `network/packet/AzHitboxPosePacket.java`, `render/lod/AzLodConfig.java`.
- AzureLib docs under `docs/azurelib/`: `misc/hitboxes.mdx`, `misc/limb_health.mdx`, `misc/animating/procedural_animation.mdx`, `inverse_kinematics.mdx`, `secondary_motion.mdx`, `blend_spaces.mdx`, `misc/meshes.mdx`, `blockbench/rig_panel.mdx`.
- GeckoLib, main branch at 3d0ac0b: `EasingType.java`.
- Not read: the AzureLib Animator Blockbench plugin, which moved to its own repository on 2026-11-26.
