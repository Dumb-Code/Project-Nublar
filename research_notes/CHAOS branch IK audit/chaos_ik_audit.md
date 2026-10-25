# CHAOS branch IK audit

Audit date: 2026-10-09. Read only. No build, no game run.

- Branch: `ShadowTekStudio/Project-Nublar-Behaviour` `1.20.1-CHAOS`, head `483c598cd66441db5e872e08b18b6cfb14224c17` (2025-06-08).
- Merge base with the fork's `1.20.1` and the Nyfaria head: `b2340d1d31d4fd7648c3df5c2e7edf3d0fc6de91` (2024-09-23). 18 commits on top, all by HerrChaos, 2024-09-28 to 2025-06-08.
- GeckoLib on this branch: 4.4.4 (`gradle.properties`). Minecraft 1.20.1, Forge and Fabric.
- Method: full clone in the scratchpad. `git diff b2340d1d31 HEAD`. Every IK file read in full.
- Links below are pinned to commit `483c598cd66441db5e872e08b18b6cfb14224c17`.
- In tables, `P/` means `Common/src/main/java/net/dumbcode/projectnublar/`.

Labels: **[F]** = fact read from code or from git metadata. **[I]** = my inference. I did not run anything, so every runtime effect is [I].

---

## 0. Provenance and history

- [F] The IK package starts in commit `ca07b8f` ("added angel constraints.", 2024-09-22). It already had FABRIK, angle constraints, `Rope`, `ServerLimb`. Two more commits (`76384ca`, `b2340d1`) followed on 2024-09-23.
- [F] These three commits went into Dumb-Code `1.20.1`. Nyfaria reverted all three on the same day: `397c42a`, `c254e19`, `4718f1b` (seen in the local repo: `git log origin/1.20.1 -- .../entity/ik`). The local `1.20.1-ai` HEAD has no `entity/ik` files. `483c598` is not in the local repo.
- [F] Starting point at `b2340d1` (13 files): `IKAnimatable`, `IKLegComponent`, `IKModelComponent`, `Rope`, `Segment`, `AngleConstraintIKChain`, `EntityStretchingIKChain`, `IKChain`, `StretchingIKChain`, `ServerLimb`, `ArrayUtil`, `GeoModelUtil`, `MathUtil`. Legs only (3 segments, no foot). No tail. No Mowzie bones. Bones were written with GeckoLib setters: `GeoModelUtil.setBonePosFromWorldPos` → `bone.setModelPosition(...)` and `setAngles` → `bone.updateRotation(...)`.
- [F] Commit `3ccb7ad` (2024-10-01) "moved to accessors and used matrices for rotation and position in model rendering" replaced the setters with forced world matrices (Mowzie bones).
- [F] HerrChaos continued the same package (same class names, `sever_limbs`, `LECK`-style code) in `SpacePotatoee/MinecraftFoundFootage` (GPL-3.0, HerrChaos 58 commits). Its IK commits run to 2025-12-23. It adds `BendReachingIKChain`, `TargetReachingIKChain`, `IKWalkerComponent`. Commit `b7d22a7` (2025-11-08) "fixed legs jittering" changes only gait gating (`adjacentEndPointGrounded` in `ServerLimb`, stand-still counter in `IKLegComponent`). It does not change the render path.

### Commit log (CHAOS only)

| Commit | Date | Message (verbatim, shortened) |
|---|---|---|
| 6ba23c9 | 2024-09-28 | moved to a better debug renderer integration and some minor performance improvements |
| a899ddd | 2024-09-28 | added feet |
| 3ccb7ad | 2024-10-01 | moved to accessors and used matrices for rotation and position in model rendering |
| a5449b4 | 2024-10-01 | refactoring and clean up |
| f5ec586 | 2024-10-11 | feet lifted off the ground; tail target set from the dino's head; legs adapt to size gene; tail collides with walls (not fully finished); "lobotomized the dino" |
| e963095 | 2024-10-17 | fixed disappearing foot; foot world colliding; tiny perf increase; springs |
| 85a3ef5 | 2024-10-17 | code clean up |
| 10d20ac | 2024-11-24 | "i'll fix quantum entaglement now" |
| 878cc4b | 2025-01-29 | "idk why it is f... me" (geo + animation rework, 74k-line animation diff) |
| cdaa301, aaca400 | 2025-03-02 | "The legs are working good enough. I don't want to make them angles anymore." |
| 9544828 | 2025-03-19 | reworked foot raising; tail bends better |
| cc6f09c | 2025-03-19 | legs walk on discontinuous terrain |
| c190d09 | 2025-04-15 | fixed up tail world colliding; refactoring entity system |
| 94c6470 | 2025-05-18 | finished entity system (accessors); started better stepping |
| b179c11 | 2025-05-18 | stepping better; "the only thing holding the leg back is the angle constraints" |
| c626103 | 2025-06-08 | "forward angle constraints are kinda working. They just freak-out when they reach to far." |
| 483c598 | 2025-06-08 | "tried to reduce jittering a bit but it is still very much present." |

---

## 1. Architecture

### Classes in `P/entity/ik/` (30 files, 2,698 lines) [F]

| Class | Lines | Role |
|---|---|---|
| `components/IKAnimatable` | 46 | Interface on the entity. Holds the component list. Dispatches `tickComponentsClient`, `tickComponentsServer`, `getModelPositions`, `renderDebug`. |
| `components/IKModelComponent` | 17 | Interface for one component: `tickServer`, `tickClient(model)`, `getModelPositions(model)`, `renderDebug`. |
| `components/IKChainComponent` | 20 | Abstract. A list of chains (`limbs`) and `setLimb(index, base, entity)` = solve one chain. |
| `components/IKLegComponent` | 351 | Legs. Per tick: plans foot targets with ground raycasts. Per frame: solves each leg chain and writes the bones. Has `LegSetting` builder. |
| `components/IKTailComponent` | 232 | Tail. Per frame: computes a swept tail target, solves the chain, writes the bones. Inner class `TailStretchingIKChain`. `tickServer` is empty. |
| `components/debug_renderers/IKDebugRenderer` | 62 | Static helpers: debug line, box, cone. |
| `components/debug_renderers/IKChainDebugRenderer` | 32 | Draws chain joints. |
| `components/debug_renderers/IKTailDebugRenderer` | 31 | Draws tail base, stretch point, target. |
| `components/debug_renderers/LegDebugRenderer` | 241 | Draws legs, foot targets, constraint cones. Repeats the ground raycasts every frame. |
| `model/BoneAccessor` | 25 | Bone abstraction: `getPosition`, `getRotationVec`, `moveTo(to, facing, entity)`, `getChildren`. |
| `model/EntityAccessor` | 16 | Entity abstraction: position, old position, level, yRot, forward vector. |
| `model/EntityEntityAccessor` | 38 | `EntityAccessor` for a vanilla `Entity`. |
| `model/ModelAccessor` | 7 | `getBone(name)`. |
| `model/GeckoLib/GeoModelAccessor` | 38 | `ModelAccessor` over a GeckoLib `GeoModel`. Throws `BoneNotFoundException` when `isDev`. |
| `model/GeckoLib/MowzieGeoBone` | 249 | `GeoBone` subclass from Mowzie's Mobs, plus `BoneAccessor` methods. `moveTo` builds a world matrix. |
| `model/GeckoLib/MowzieModelFactory` | 74 | GeckoLib `BakedModelFactory` that bakes `MowzieGeoBone`s. Copy of Mowzie's Mobs. |
| `parts/Segment` | 73 | Joint: position, length, stretch direction, angle limit (`angleOffset`, `angleSize`, default 30°). Builder. |
| `parts/WorldCollidingSegment` | 88 | `Segment` whose `move()` sweeps a block raycast from the old to the new position and stops at the hit. |
| `parts/Rope` | 61 | Follow-the-leader rope. **Unused.** |
| `parts/Spring1d`, `parts/Spring3d` | 33, 54 | Damped springs with gravity. **Unused.** |
| `parts/ik_chains/IKChain` | 171 | FABRIK solver. |
| `parts/ik_chains/StretchingIKChain` | 33 | Before FABRIK, lays the chain straight towards a "stretch" point (`extendFully`). |
| `parts/ik_chains/AngleConstraintIKChain` | 189 | FABRIK with planar hinge limits on a "leg plane". |
| `parts/ik_chains/EntityLeg` | 49 | Leg plane and stretch point from entity yaw. |
| `parts/ik_chains/EntityLegWithFoot` | 65 | Adds a foot segment pitched down to the ground with a swept raycast. |
| `parts/sever_limbs/ServerLimb` | 199 | Tick-rate foot planner: target, old target, step arc (parabola), gait lock. Gait index helpers translated from Cymaera. |
| `util/ArrayUtil` | 20 | First, last, get-or-null. |
| `util/ChainCommonClass` | 19 | Logger, `isDev = true`, `shouldRenderDebugLegs`. |
| `util/MathUtil` | 165 | Vector helpers, axis-angle rotation (JOML `Quaterniond`), plane normals, law-of-cosines angle. |

### Touch points outside the package [F]

| File | Change |
|---|---|
| `P/entity/Dinosaur.java` | Implements `IKAnimatable<Dinosaur>`. `setUpLimbs()` in the constructor ([L69-104](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/Dinosaur.java#L69-L104)). `tick()` calls `tickComponentsServer` ([L176-183](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/Dinosaur.java#L176-L183)). `getSize()` = SIZE gene / 100 + 1 ([L191-193](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/Dinosaur.java#L191-L193)). |
| `P/client/model/DinosaurGeoModel.java` | `setCustomAnimations` calls `tickComponentsClient(animatable, new GeoModelAccessor(this))` ([L22-26](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/client/model/DinosaurGeoModel.java#L22-L26)). |
| `P/client/renderer/entity/DinosaurRenderer.java` | After `super.render`, calls `getModelPositions` ([L36-39](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/client/renderer/entity/DinosaurRenderer.java#L36-L39)). Overrides `renderRecursively` and `renderChildBones` ([L41-122](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/client/renderer/entity/DinosaurRenderer.java#L41-L122)). |
| `P/client/renderer/entity/layer/IKDebugRenderLayer.java` | Calls `renderDebug` when the debug flag is on. |
| `P/mixin/DinoDebugRendererMixin.java` | `KeyboardHandler.handleDebugKeys`: key 74 (J, so F3+J) toggles `shouldRenderDebugLegs`. |
| `P/CommonClass.java` | Registers `MowzieModelFactory` for the whole mod ID ([L25](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/CommonClass.java#L25)). |
| `P/init/EntityInit.java` | T. rex size 2×4 → 2×3 ([L24](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/init/EntityInit.java#L24)). |
| `Fabric/.../ik/command/TestCommand.java` | Dev-only `/step change|set <n>` changes `IKChain.MAXLECK`. |
| Fabric and Forge `ProjectNublar.java` | In dev: `isDev = true`, `shouldRenderDebugLegs = true`. Fabric also adds `MOVEMENT_SPEED 0.23`. |
| T. rex geo and animation | See section 5. |

### How components attach [F]

- `Dinosaur` keeps `public List<IKModelComponent<Dinosaur>> components`. The constructor calls `setUpLimbs()`, which calls `addComponent(...)` twice. Every `Dinosaur` (only the T. rex is registered on this branch) gets the same T. rex legs and tail. Chains are per entity instance.
- Three call sites drive the components:
  1. `Dinosaur.tick()` → `tickComponentsServer` (both logical sides, see section 4).
  2. `DinosaurGeoModel.setCustomAnimations` → `tickComponentsClient` (every render frame, after GeckoLib applies keyframes).
  3. `DinosaurRenderer.render` → `getModelPositions` (after the model is drawn; reads bone world positions for the next frame).

### Which body parts have chains [F]

| Part | Chain | Segments | Bones written |
|---|---|---|---|
| Left and right hind legs | `EntityLegWithFoot` (FABRIK + hinge limits) | 1.1, 1.4, 1.3 blocks + foot 0.5625 ([Dinosaur L80-95](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/Dinosaur.java#L80-L95)) | `segment{1..3}_leg{1,2}`, `foot_leg{1,2}`. Base from `base_leg{1,2}`. |
| Tail | `TailStretchingIKChain` (plain FABRIK, no limits) of `WorldCollidingSegment` | 1.4, 1.7, 1.3 ([Dinosaur L98-103](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/Dinosaur.java#L98-L103)) | `segment{1..3}_tail1`. Base from `tail1_base`. |
| Neck, head, arms | none | – | – |

- [F] The tail reads `head` and `center_of_mass` only to aim the tail: `centerDirection = center_of_mass − head` ([IKTailComponent L133](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKTailComponent.java#L133)).

---

## 2. Solvers

### FABRIK [F]

- `IKChain.solve` ([L31-46](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/IKChain.java#L31-L46)): up to `MAX_ITERATIONS = 20`. Each iteration: forward pass (`reachForwards`, end to base) then backward pass (`reachBackwards`, base to end). Stops when the end joint is within `TOLERANCE = 0.01` blocks of the target. If the target is out of reach, it does one iteration and stops.
- `moveSegment` = place a point at `length × scale` from the pull point along the line ([L143-146](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/IKChain.java#L143-L146)).
- Global pass cap: `static int LECKMEINEEIER` counts passes across **all** chains. `MAXLECK = MAX_ITERATIONS × 2 + 1 = 41`. Reset to 0 at the end of every `solve` ([L45-64](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/IKChain.java#L45-L64)). The dev `/step` command changes `MAXLECK` to step through passes. Static and mutable; safe only on one thread.
- `StretchingIKChain.solve` first calls `extendFully`: it lays the chain straight from the base towards a "stretch point" ([L23-32](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/StretchingIKChain.java#L23-L32), [IKChain L67-78](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/IKChain.java#L67-L78)). So each solve starts from a straight pose, not from last frame's pose. For legs the stretch point is forward along entity yaw ([EntityLeg L28-31](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/EntityLeg.java#L28-L31)). That biases the knee to bend the right way. For the tail it is `tail1_base` + its bone direction × max length ([IKTailComponent L189-196](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKTailComponent.java#L189-L196)).

### Angle constraints [F]

- `AngleConstraintIKChain` ([L43-176](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/AngleConstraintIKChain.java#L43-L176)) overrides both passes. For each joint it builds a reference direction (previous segment direction, or "down" for the root), rotated by `angleOffset` in the leg plane. If the angle between the reference and the new segment exceeds `angleSize`, it rotates the joint back by the excess around the leg-plane normal.
- The leg plane normal comes from base, end joint and stretch point, oriented towards a point 100 blocks to the entity's side ([L184-186](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/AngleConstraintIKChain.java#L184-L186), [EntityLeg L22-25](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/EntityLeg.java#L22-L25)). So limits are 2D hinges in one vertical plane per leg. No twist limit.
- The end joint has a "floor angle" limit (`floorAngle = 20`, `floorAngleSize = 90`) relative to the ground direction.
- The rotation sign is picked by `dot(cross-product normal, leg plane) > 0` ([L65, L94, L120, L146, L170](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/AngleConstraintIKChain.java#L65)). `483c598` added this sign test to reduce jitter.
- Angles come from `Math.acos(dot)` with no clamp to [−1, 1] ([L60, L89, L115, L141, L165](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/AngleConstraintIKChain.java#L60); also [MathUtil L101-109](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/util/MathUtil.java#L101-L109)). The foot has a NaN guard that logs "Foot has dropped to NaN" ([EntityLegWithFoot L51-55](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/EntityLegWithFoot.java#L51-L55)).
- The tail chain is a plain `IKChain` subclass. `Segment.hasAngleConstraints` defaults to true but plain `IKChain` never reads it. So the tail has **no joint limits**.

### Stretching [F]

- "Stretching" here does not change segment lengths. It is the pre-straightening above. `Segment.StretchDirection` (FORWARDS, BACKWARDS, DOWN, TARGET, NONE) is stored and never read.

### Foot [F]

- After the leg solve, the foot segment (a `WorldCollidingSegment`) is placed by angle in the leg plane, swept down 0.05 blocks with a raycast, then clamped to `[0, angleSize]` ([EntityLegWithFoot L27-56](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/EntityLegWithFoot.java#L27-L56)).

### Step planner (`ServerLimb`) [F]

- Each tick, for each leg: offset `baseOffset × scale`, rotated by yaw, plus `stepInFront` forward if the entity moved. Five ground raycasts (centre plus floor/ceil of x and z), from 3 above to 10 below. Pick the hit nearest to centre + 0.5 up ([IKLegComponent L128-194](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKLegComponent.java#L128-L194)).
- A new target is set when the planted foot is farther than `maxDistance × scale` (or `maxStandingStillDistance` after 40 still ticks) from the ground under the hip.
- Step motion: `pos = oldTarget + flat delta × t + parabola(t)`, `t += 0.3` per tick, so a step takes 4 ticks ([ServerLimb L39-74](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/sever_limbs/ServerLimb.java#L39-L74)). A leg steps only when the opposite leg is planted (`horizontal(i)`). If the target is more than `5 × scale × leg length` away, the foot snaps.

### Other solver types

- [F] No CCD, no Jacobian. `Rope` is follow-the-leader (one FABRIK-style pass). `Spring1d`/`Spring3d` are explicit-Euler damped springs. Rope and springs are never instantiated.

### Iterations per tick or frame [F + I]

- [F] Leg and tail chains solve in `tickClient`, called from `setCustomAnimations`. In GeckoLib for 1.20.1, `handleAnimations` uses `currentFrameTime = tickCount + partialTick` for entities, so it runs every rendered frame (checked on the GeckoLib `1.20.1` branch head, version 4.8.4; I did not check the 4.4.4 tag).
- [I] So per T. rex per frame: 3 chain solves, each up to 20 iterations (41 passes). Per tick: the step planner, on both sides.

---

## 3. World collision

### `WorldCollidingSegment` [F]

- `move(position)` → `move(position, true, 0)`: sets the new position, then `level.clip(ClipContext(oldPos + rising, newPos, Block.COLLIDER, Fluid.NONE, new Arrow(...)))`. The joint ends at the hit point, nudged 0.01 ([L41-63](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/WorldCollidingSegment.java#L41-L63)).
- So it is a **swept point test** of the joint only, against block collision shapes (`COLLIDER` uses each block's collision `VoxelShape`). No AABB test. No test of the segment line between two joints. The variant that tests the link line (`move(position, link)`, [L65-87](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/WorldCollidingSegment.java#L65-L87)) is used only by commented-out code ([IKTailComponent L198-230](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKTailComponent.java#L198-L230)).
- Each raycast allocates a new `Arrow` entity only to build the collision context.

### Tail [F]

- Tail target: `tailBasePosition + centerDirection × maxLength × dot`, where `dot` maps the base bone direction against the head-to-centre direction into [0, 1] ([IKTailComponent L85-89](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKTailComponent.java#L85-L89)).
- The target is swept from **last frame's target** to the new point, and stops at the first block hit ([L136-166](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKTailComponent.java#L136-L166)).
- Then FABRIK runs. Every joint move, including the base move and `extendFully`, is a swept move. A joint stops where its sweep meets a block.
- If the first joint ends more than √2 blocks from the bone base, `isReady = false`. Next frame `initializeTail` teleports every joint to entity position + 1 up ([L51-70](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKTailComponent.java#L51-L70), [L93-95](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKTailComponent.java#L93-L95)).

### Answers

- **Bend or stop?** [F] Each joint *stops* at contact. [I] FABRIK then pulls the next joints towards the stopped joint, so the chain shape changes, but segment lengths are no longer held: a stopped joint leaves a gap or overlap that the next pass cannot fix through the block. The bone is placed at joint *i* and pointed at joint *i+1* with no length scaling, so the mesh can gap or poke through. There is no joint-limit-aware bend, and no search for a free direction.
- [I] The result depends on the previous frame (swept from old positions). It is frame-rate dependent.
- **Does the body's movement change?** [F] No. Nothing in the IK package sets entity position, velocity or yaw. The entity box is the vanilla box (now 2×3). Legs and tail follow the body; the body ignores them.

---

## 4. Server versus client

- [F] `Dinosaur.tick()` calls `tickComponentsServer(this)` with no `isClientSide` check ([L176-183](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/Dinosaur.java#L176-L183)). So the "server" step planner runs on **both** logical sides, each with its own `ServerLimb` objects.
- [F] `IKTailComponent.tickServer` is empty ([L46-49](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKTailComponent.java#L46-L49)).
- [F] Every chain solve is in `tickClient`, which needs bone world positions (`base_leg*`, `tail1_base`, `center_of_mass`, `head`). These exist only after a GeckoLib render (`DinosaurRenderer.render` → `getModelPositions`).
- [F] No packet, no `EntityDataAccessor`, no NBT for IK state. Nothing is synced.
- [I] The server's `ServerLimb` results are never read. They cost 12 raycasts and 12 `Arrow` allocations per tick per T. rex for nothing.
- [I] Server and client would **not** produce the same chain positions:
  - the client entity position is interpolated from network updates, and `xOld` differs;
  - `hasMovedOverLastTick` and the stand-still counter differ;
  - the tail and foot sweeps depend on the frame history;
  - bone positions come from the rendered pose, which the server does not have.
- [I] It cannot supply server-side hitbox positions as written. The pure math (`IKChain`, `AngleConstraintIKChain`, `MathUtil`) has no client classes and could run on a server if its bases came from a procedural spine instead of bones. The `WorldCollidingSegment` sweep must be replaced by a deterministic test first.

---

## 5. GeckoLib integration

### How solved positions reach bones [F]

1. `CommonClass` registers `MowzieModelFactory` for the mod ID, so every Project Nublar GeckoLib model bakes `MowzieGeoBone`s.
2. In `tickClient`, for each chain segment, `bone.moveTo(jointWorld, nextJointWorld, entity)`.
3. `MowzieGeoBone.moveTo` ([L217-244](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/model/GeckoLib/MowzieGeoBone.java#L217-L244)): sets `forceMatrixTransform = true`; rotates both points about the entity position by `entity.getYRot() − 180` around Y; builds `translate(point)` × `rotateTo((0,−1,0) → direction)`; stores it with `setWorldSpaceMatrix`.
4. `DinosaurRenderer.renderRecursively` ([L46-59](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/client/renderer/entity/DinosaurRenderer.java#L46-L59)): for forced bones, multiplies the current pose by `translate(0,−0.01,0) × translate(−entity.getX/Y/Z) × worldSpaceMatrix`. It skips the bone's own position, rotation and keyframes.
5. `getModelPositions` reads world positions of the base bones after the draw ([L36-39](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/client/renderer/entity/DinosaurRenderer.java#L36-L39)). In the normal bone path the renderer stores `worldSpaceMatrix = localMatrix + this.animatable.position()` ([L93-100](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/client/renderer/entity/DinosaurRenderer.java#L93-L100)).
6. `getRotationVec` (bone "up" axis from the model-space matrix) gives the tail base direction ([MowzieGeoBone L210-215](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/model/GeckoLib/MowzieGeoBone.java#L210-L215)).

### Does it fight the animation controller? [F]

It avoids the fight by removing the animation, not by blending:

- The geo file moves the IK bones out of the hierarchy. `leg1`, `leg2`, `tail1` are new top-level bones with the segments as children. `base_leg1/2`, `center_of_mass`, `tail1_start`, `tail1_base` (rotated −90°) are new children of `hips`. The old `tail2`–`tail4` chain under `hips` is gone. The old `seg*_leg*`/`footLeft*` names became `segment*_leg*`/`foot_leg*`.
- The animation file drops every leg and foot track and every `tail2`–`tail4` track. `tail1` is renamed `tail1_start`. Example: `walk` went from 23 animated bones to 12; `drink` from 26 to 16; `roar` from 26 to 17.
- [I] Effect: walk-cycle leg poses and tail sway from the artists are gone. Actions (drink, roar, graze) can still move the neck and head, and can aim the tail only through `tail1_start`. For our layer 3 (offsets baked from animation files), this branch's T. rex animation file is no longer a source for legs or tail. Use the original file.

### Jitter causes [I unless marked]

Ranked by my estimate of size:

1. **Mixed interpolation in the forced-bone path.** [F] Mowzie's original renderer lerps the entity position with `partialTick` and rotates by the lerped body yaw ([Mowzie MowzieGeoEntityRenderer L103-118](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/client/render/entity/MowzieGeoEntityRenderer.java#L103-L118)). [F] The CHAOS copy uses `animatable.getX()` with no lerp, computes `lerpBodyRot` but never uses it, and un-rotates by the raw `getYRot()` in `moveTo` instead. The same no-lerp change is in the tracked world matrix ([L99](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/client/renderer/entity/DinosaurRenderer.java#L99) versus [Mowzie L158-161](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/client/render/entity/MowzieGeoEntityRenderer.java#L158-L161)). [I] The pose stack is at the interpolated position, so IK bones are offset by `(1 − partialTick) × velocity` and snap back each tick: a 20 Hz sawtooth while walking.
2. **Yaw mismatch.** [I] The pose stack is rotated by lerped `yBodyRot`; `moveTo` cancels with raw `yRot`. A mob's body yaw lags `yRot` during turns, so legs and tail swing about the entity origin while turning.
3. **One-frame lag.** [F] Bases are read after this frame's draw and used next frame. [I] Hips move with the animation, so hip and upper leg separate a little each frame.
4. **Tick-rate foot targets.** [F] `ServerLimb.pos` changes per tick (`t += 0.3`), with no partial-tick blend. [I] Feet step in 20 Hz jumps while chains re-solve every frame.
5. **Constraint flips.** [I] Near-straight joints make the cross-product normal unstable; the sign test can flip between frames; unclamped `acos` can give NaN. The commit "freak out when they reach too far" fits this.
6. **History-dependent tail.** [I] Sweeps from last frame's joints plus the reset-to-entity rule cause snaps near walls.

### Other bugs and risks [F unless marked]

- `ChainCommonClass.isDev = true` by default ([L7](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/util/ChainCommonClass.java#L7)), so production builds also throw `BoneNotFoundException` when a bone is missing ([GeoModelAccessor L23-26](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/model/GeckoLib/GeoModelAccessor.java#L23-L26)). [I] Any new species without these bone names would crash its renderer.
- In dev, `shouldRenderDebugLegs = true` by default and moves IK bones 200 blocks down ([IKLegComponent L89-92](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKLegComponent.java#L89-L92), [IKTailComponent L107-110](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKTailComponent.java#L107-L110)).
- Scale is inconsistent: `IKChain.moveSegment` and `extendFully` scale lengths; `AngleConstraintIKChain` passes use raw `segment.length`; `getMaxLength()` and `isTargetToFar` are unscaled. [I] Only wrong when the SIZE gene is not 0. [I] `scaleModelForRender` also scales the pose stack under the forced world matrix, which may misplace IK bones for non-default sizes; needs a test.
- `IKChain.setSegmentsTo` loops to `size() − 2` and skips the last segment ([L136-141](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/ik_chains/IKChain.java#L136-L141)). Unused.
- Commit "quantum entanglement" (`10d20ac`): [I] probably state shared between instances (GeckoLib 4 bones belong to one `GeoModel` per species). Not confirmed. At head, chain state is per entity; forced-matrix flags stay on the shared bones, which is harmless while every T. rex uses IK.
- Test code left in: `Dinosaur.tick()` makes the dinosaur path to the nearest player within 100 blocks who holds a bone ([L178-181](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/Dinosaur.java#L178-L181)). `registerGoals` is empty. `getTextureLocation` is hard-wired to the T. rex texture.

---

## 6. Provenance and licence

| Code | Source | Evidence | Credit in code |
|---|---|---|---|
| `MowzieModelFactory` | Mowzie's Mobs `client/model/tools/MowzieModelFactory.java` (branch 1.20, commit `64d1524`) | `diff -w`: only the package line and a javadoc differ. Verbatim copy. | Javadoc "Provided by Bob Mowzie", CurseForge link |
| `MowzieGeoBone` | Mowzie's Mobs `client/model/tools/geckolib/MowzieGeoBone.java` | Same fields, constructors and helpers; methods reordered; some `double` overloads and `setWorldPos` removed; child filter in the copy constructor removed. Added: `getPosition`, `getRotationVec`, `moveTo`, `getChildren`. | Comment: "only the interface is mine but the code in this class is entirely provided by Bob Mowzie" |
| `DinosaurRenderer.renderRecursively`, `renderChildBones` | Mowzie's Mobs `MowzieGeoEntityRenderer.renderRecursively` (L99-175) | Near line for line, with the position lerp and the body-yaw rotate removed | **None** |
| `ServerLimb` gait helpers ([L138-199](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/parts/sever_limbs/ServerLimb.java#L138-L199)) | Cymaera `minecraft-spider` `LegLookUp.kt` | Kotlin → Java, same function names and logic | Comment: "politely stolen from Cymaera, with their consent" |
| Everything else in `entity/ik` | HerrChaos | No foreign header; same code continued in `MinecraftFoundFootage` under his name | – |

Licences:

- [F] Mowzie's Mobs `License.txt` (branch 1.20): "All Rights Reserved unless otherwise explicitly stated". Custom terms: credit "Mowzie's Mobs" with a CurseForge link; no direct sale; **non-compete** (no Mowzie gameplay features on Java Edition); non-imitate; non-manipulate ("faithful to the source material"); the author can change the licence. GitHub reports `NOASSERTION`.
- [F] Cymaera `minecraft-spider`: GitHub reports no licence (`license: null`). By default that is all rights reserved. The consent claim has no public record that I found.
- [F] Project Nublar (`Dumb-Code/Project-Nublar`) and this fork: LGPL-3.0.
- [I] Not legal advice: all-rights-reserved code cannot be relicensed as LGPL-3.0. Shipping the Mowzie copies in an LGPL-3.0 mod needs Bob Mowzie's explicit permission; a comment claiming it was "provided" is not a public grant. The Cymaera piece is about 60 lines of trivial index arithmetic, easy to rewrite. Do not port any of these into the 26.2 code; in 26.2 they do not fit the GeckoLib API anyway (section 8).
- [I] The FoundFootage copy of the IK core is GPL-3.0. HerrChaos can relicense his own code; if we take ideas from that copy, ask him.

---

## 7. State and quality

### Works (per commit messages; not tested) [F messages, I status]

- Two-legged stepping with ground raycasts, step arcs, alternating gait, uneven terrain ("working good enough", "not broken").
- Foot pitched to the ground.
- Tail aimed from the head–centre line, swept against walls ("fixed up tail world colliding").
- Debug overlay (F3+J).

### Unfinished or broken [F]

- Forward angle constraints "freak out" at full reach (`c626103`).
- Jitter "still very much present" (`483c598`).
- No neck or head chain; no arms.
- Springs and rope unused. `StretchDirection` unused. `Segment.hasAngleConstraints` unused.
- No server use, no sync, no save.
- No TODO or FIXME comments in the IK code.

### Cost per T. rex [F counts from code, I for size]

| When | Work | Allocations |
|---|---|---|
| Per tick, server **and** client | 2 legs × 6 ground raycasts = 12 | 12 `Arrow` entities, ~40 `Vec3`, 2 `List.of` |
| Per frame, legs | 2 × (straighten + up to 20 FABRIK iterations with hinge limits; each limit step recomputes the leg plane) + 2 foot sweeps | [I] thousands of short-lived `Vec3` per leg in the worst case; 2 `Arrow` |
| Per frame, tail | 1 target sweep + 3 straighten sweeps + up to 20 × 7 joint sweeps = up to ~144 raycasts | up to ~144 `Arrow` |
| Per frame, glue | 2 `new GeoModelAccessor`, string-concatenated bone names, `Optional` per lookup, a stream for an unused average ([IKLegComponent L57-63](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/ik/components/IKLegComponent.java#L57-L63)) | small |
| Debug on | `LegDebugRenderer` repeats ~9+ raycasts per leg per frame | more `Arrow`s |

- [I] At 60 fps, worst case near 8,000+ raycasts and `Arrow` constructions per second per T. rex from the tail alone. An `Arrow` runs the full `Entity` constructor (synched data, etc.); this is likely costlier than the raycast. Needs a profiler run before any claim of a number.
- [I] For a herd of 20 this does not scale without changes: pass `null`/`CollisionContext.empty()` instead of an entity, cache, and cap iterations by distance.

---

## 8. Fit to the hitbox design

| Design layer | CHAOS equivalent | Verdict [I] |
|---|---|---|
| 1. Rest pose from geo file | Reads bone positions from the rendered model | Wrong source for server use. Our spine reads the geo file directly. |
| 2. Chain buffering from body-yaw history | None. The tail lags only through the swept target from last frame. `Rope` (unused) is a follow-the-leader chain. | Nothing to reuse. Alex's Caves ring buffer stays the reference. |
| 3. Offsets baked from animations | Removed the leg and tail tracks from the T. rex animation file | Negative: use the original animation file, not this branch's. |
| 4. Collision constraint, bend out of blocks within joint limits | `WorldCollidingSegment` + tail sweep | Concept only. It stops joints (no bend search), tests joint points (not segments), has no tail joint limits, and depends on frame history. Reuse the idea "test against `Block.COLLIDER` shapes"; write a deterministic per-joint rotate-until-clear within limits, on the server tick. |
| 5. Reach IK, mouth to food or water | `AngleConstraintIKChain` (FABRIK + planar hinge limits) | Closest fit. A neck chain with base at the shoulders and target at the food point in the sagittal plane matches its planar-hinge model. Fix first: unclamped `acos`, sign flips at full reach, unscaled lengths, straight-pose restart. Small enough to rewrite cleanly. |
| Hitbox placement | Joints are world-space `Vec3` lists; easy to turn into part boxes | Only if computed on the server from the procedural spine. Leg chains are not hit sections; ignore them. |

### Porting to GeckoLib 5 on 26.2 [F for API facts from the GeckoLib `26.2` branch, commit `1e09655`; I for consequences]

- [F] `GeoBone` is abstract with `final` pivot and base rotation fields, and no model-space or world-space matrices or setters ([GL262 GeoBone.java L22-55](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/cache/model/GeoBone.java#L22-L55)). Per-pass changes go through `BoneSnapshot`s set by a `BoneUpdater` ([GL262 RenderPassInfo.java L177-183, L259-268](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/renderer/base/RenderPassInfo.java#L177-L183)).
- [F] Positions come only from `addBonePositionListener(name, (worldPos, modelPos, localPos) -> …)`, called during render; the comment says this "is the only time they actually have a position of any kind" ([L186-198, L271-273](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/renderer/base/RenderPassInfo.java#L186-L198)). Positions only, no rotation.
- [F] Bone drawing is `GeoBone.positionAndRender` → `RenderUtil.prepMatrixForBoneAndUpdateListeners` ([GeoBone L127-139](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/cache/model/GeoBone.java#L127-L139)). No file named `BakedModelFactory` exists in the 26.2 source tree.

What breaks [I]:

1. `MowzieGeoBone` and `MowzieModelFactory`: no mutable bone subclass hook and no matrices to force. Gone.
2. `DinosaurRenderer.renderRecursively` override: replaced by render-state rendering. Gone.
3. Forced world matrices: must become local `BoneSnapshot` rotations in a `BoneUpdater`, which means converting world joint positions into parent-relative Euler angles. The `b2340d1` starting point (setters for position and angle) is closer to this than the head.
4. `getRotationVec` for the tail base: listeners give no rotation. Use two bones or a locator.
5. `setCustomAnimations(animatable, …)` with entity access: the 26.2 renderer works from an extracted render state. Entity data (yaw, IK state) must be copied into the state first.
6. One-frame lag stays: listeners report during render, updaters run before it. Only procedural bases (our spine) remove it.
7. Top-level IK bones in the geo file: keep the artists' hierarchy instead; drive existing bones with a `BoneUpdater` on top of keyframes. Nobody has tested whether 26.2 `BoneUpdater`s fight the animation controller (same gap as the design report).

---

## 9. Hitboxes, multipart, pathfinding on this branch [F]

- **Hitbox:** only the T. rex vanilla size changes from 2×4 to 2×3 ([EntityInit L24](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/init/EntityInit.java#L24)). No part entities, no multipart, no `PartEntity`, no `EnderDragonPart`.
- **Pathfinding:** `createNavigation` returns SmartBrainLib `SmoothGroundNavigation` ([Dinosaur L59-62](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/Dinosaur.java#L59-L62)). All goals removed ([L150-152](https://github.com/ShadowTekStudio/Project-Nublar-Behaviour/blob/483c598cd66441db5e872e08b18b6cfb14224c17/Common/src/main/java/net/dumbcode/projectnublar/entity/Dinosaur.java#L150-L152)). Test follow-the-bone-holder in `tick()`. Fabric adds `MOVEMENT_SPEED 0.23`.
- **Movement:** IK never moves the body.
