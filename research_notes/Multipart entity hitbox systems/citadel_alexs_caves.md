# Multipart entities in Citadel (1.21) and Alex's Caves (1.21.1)

Source snapshot: shallow clones read on 2026-10-09. Citadel branch `1.21` at commit `badebf6` (2026-07-18). Alex's Caves (AC) branch `1.21.1` at commit `0ab3c21` (2026-04-26). NeoForge patches read from the NeoForge `1.21.1` branch. Line numbers refer to those snapshots. Short names in links: `AC/` = `src/main/java/com/github/alexmodguy/alexscaves/`, `CIT/` = `src/main/java/com/github/alexthe666/citadel/`.

## Q1. Does Citadel provide a reusable multipart API? Is it built on PartEntity?

### Takeaway
No. Citadel 1.21 has no multipart or part-entity code. AC builds its own system directly on NeoForge's `net.neoforged.neoforge.entity.PartEntity` (the generic replacement for vanilla `EnderDragonPart`). Citadel supplies only nearby tools that AC uses together with parts: the `AdvancedPathNavigate` pathfinder, the `LegSolver` foot-height solver, `ICustomCollisions`, and the `AnimationHandler`/`AnimationMessage` animation sync.

### Cited Findings
- A full-text search of the Citadel 1.21 source for `PartEntity`, `multipart`, `IMultipart`, `EntityPart` and `getParts` finds nothing. The entity-side packages are `animation/`, `server/entity/collision/`, `server/entity/pathfinding/raycoms/` and `server/message/` (Animation, DanceJukebox, Properties, SyncClientTickRate, SyncePath, SyncPathReached messages). None of them is a part packet. — [Citadel 1.21 tree](https://github.com/AlexModGuy/Citadel/tree/1.21/src/main/java/com/github/alexthe666/citadel)
- Citadel 1.21 targets NeoForge (it uses the `net.neoforged.moddev` Gradle plugin). — [Citadel build.gradle](https://github.com/AlexModGuy/Citadel/blob/1.21/build.gradle)
- NeoForge `PartEntity<T>` is a very small abstract `Entity`. Its constructor calls `super(parent.getType(), parent.level())` and stores the parent. It has `getParent()`. `getAddEntityPacket` throws `UnsupportedOperationException`, so a part is never spawned to clients as an entity of its own. — [NeoForge PartEntity.java (1.21.1)](https://github.com/neoforged/NeoForge/blob/1.21.1/src/main/java/net/neoforged/neoforge/entity/PartEntity.java)
- An entity opts in with `isMultipartEntity()` and `getParts()` (default methods in `IEntityExtension`). EnderDragon is patched to use the same hooks. — [NeoForge IEntityExtension](https://github.com/neoforged/NeoForge/blob/1.21.1/src/main/java/net/neoforged/neoforge/common/extensions/IEntityExtension.java); [EnderDragon patch](https://github.com/neoforged/NeoForge/blob/1.21.1/patches/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java.patch)
- `ServerLevel` changes the vanilla `dragonParts` map to `Int2ObjectMap<PartEntity<?>>`. On tracking start and end it adds or removes `getParts()` for any `isMultipartEntity()` entity. It also skips direct ticking of any `PartEntity` and exposes `getPartEntities()`. — [NeoForge ServerLevel.java.patch](https://github.com/neoforged/NeoForge/blob/1.21.1/patches/net/minecraft/server/level/ServerLevel.java.patch) (patch lines ~7-8, ~55-57, ~224-247, ~261-263)
- `ClientLevel` keeps a matching `partEntities` map, filled in `onTrackingStart` and cleared in `onTrackingEnd`, and exposes `getPartEntities()`. — [NeoForge ClientLevel.java.patch](https://github.com/neoforged/NeoForge/blob/1.21.1/patches/net/minecraft/client/multiplayer/ClientLevel.java.patch) (patch lines ~15, ~116-144)
- `Level.getEntities(Entity, AABB, Predicate)` and the typed `getEntities(EntityTypeTest, AABB, …)` disable the vanilla EnderDragon-only branch (`if (false)`). They then loop over **all** `getPartEntities()` in the level and add each part whose bounding box intersects the query. This is how parts become visible to ray picks, AoE queries and so on. — [NeoForge Level.java.patch](https://github.com/neoforged/NeoForge/blob/1.21.1/patches/net/minecraft/world/level/Level.java.patch) (patch lines ~166-178, ~186-201)
- Citadel's `AdvancedPathNavigate` takes an explicit `width`/`height` (constructor lines 103-129). `tick()` writes `nodeEvaluator.entityWidth/Height/Depth = floor(width+1)` (lines 249-256). `AbstractPathJob` also honours an optional `ICustomSizeNavigator` (`getXZNavSize`, `getYNavSize`) at lines 459-462. — [AdvancedPathNavigate.java](https://github.com/AlexModGuy/Citadel/blob/1.21/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/AdvancedPathNavigate.java#L103-L129); [ICustomSizeNavigator.java](https://github.com/AlexModGuy/Citadel/blob/1.21/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/pathjobs/ICustomSizeNavigator.java)
- Citadel `LegSolver` computes per-leg ground heights from body yaw and scale, with no entity parts. — [LegSolver.java](https://github.com/AlexModGuy/Citadel/blob/1.21/src/main/java/com/github/alexthe666/citadel/animation/LegSolver.java)
- Citadel `AnimationHandler.sendAnimationMessage` sends `AnimationMessage(entityId, animationIndex)` with `PacketDistributor.sendToAllPlayers` (lines 23-28). — [AnimationHandler.java](https://github.com/AlexModGuy/Citadel/blob/1.21/src/main/java/com/github/alexthe666/citadel/animation/AnimationHandler.java#L23-L28)

### Inferences
- Anyone who copies "the Alex's mods multipart approach" on NeoForge 1.21.1 depends only on NeoForge's `PartEntity` hooks plus AC's own code. Citadel adds nothing for hitboxes.
- The animation packet goes to all players, not only to players tracking the entity. This is a small, separate scaling cost.

### Gaps
- I did not check older Citadel branches (1.16 to 1.20) for a historical multipart API. The question was limited to the 1.21 branch.

## Q2. Which AC creatures use parts, how many, and how are part positions computed?

### Takeaway
Seven AC entity classes use NeoForge parts. Atlatitan and Luxtructosaurus share 7 parts through `SauropodBaseEntity`. Tremorzilla has 5, Hullbreaker 5, Gossamer Worm 5, Magnetron 10, Corrodent 1 and the Quarry Smasher (an item-entity) 1. Tremorsaurus, Grottoceratops, Vallumraptor, Subterranodon and Relicheirus have no parts and use one vanilla box. Positions come from a **procedural chain**: each segment is a fixed local offset, rotated by a smoothed per-segment pitch/yaw and added to the previous segment's centre. Yaw lag comes from a 128-entry ring buffer of past body yaws. No bones, IK solver or model data is used on the server.

### Cited Findings
- **Part classes.** Two families exist:
  - Older per-creature subclasses extend `PartEntity` directly: `SauropodPartEntity`, `TremorzillaPartEntity`, `HullbreakerPartEntity` and `MagnetronPartEntity`.
  - A newer shared base, `ACMultipartEntity<T> extends PartEntity<T>`, is used by `GossamerWormPartEntity`, `CorrodentTailEntity` and `QuarrySmasherHeadEntity`.

  — [ACMultipartEntity.java L16-104](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/util/ACMultipartEntity.java#L16-L104); [SauropodPartEntity.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodPartEntity.java)
- **Sauropods (Atlatitan, Luxtructosaurus).** `SauropodBaseEntity` builds the parts in its constructor (lines 110-117). Each part takes `(parent, connectedTo, sizeXZ, sizeY)`:
  - neckPart1 (3×3) connects to the body.
  - neckPart2 (2×2) connects to neck1.
  - neckPart3 (2×1.5) connects to neck2.
  - headPart (2×2) connects to neck3.
  - tailPart1 (3×2) connects to the body.
  - tailPart2 (2.5×1.5) connects to tail1.
  - tailPart3 (2×1) connects to tail2.

  It returns them from `isMultipartEntity()` and `getParts()` (lines 405-413). — [SauropodBaseEntity.java L110-117](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L110-L117)
- **Sauropod `tickMultipart()` (lines 247-305).** It is called from `tick()` at line 166, on both sides:
  1. It pushes `yBodyRot` into a 128-float ring buffer (`yawBuffer`).
  2. It saves each part's old position.
  3. It moves `neckXRot/neckYRot/tailXRot/tailYRot` toward targets with `Mth.approachDegrees` at a "rotate speed" (normally 10°/tick, 30 during whips or roars).
  4. It splits the neck rotation into 4 steps and the tail rotation into 3 steps.
  5. It places each part with `setPosCenteredY(rotateOffsetVec(localOffset * scale, xStep, yBodyRot + yStep*k) + previousPartCentre)`.

  Example offsets: neck1 `(0, 2, 5)` from the body centre, neck2 `(0, 0, 2.5)` from neck1, tail1 `(0, -0.5, -3.5)`. The body centre is lowered by `getLegSolverBodyOffset()` (lines 375-382), which comes from the Citadel-style leg solver. Stomp and dance animations add extra neck Y/Z offsets (lines ~277-288). Finally, `xo/yo/zo` and `xOld/yOld/zOld` are overwritten with the pre-move positions so render interpolation works. — [SauropodBaseEntity.java L247-305](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L247-L305)
- **Animation targets are hard-coded per animation.** `getTargetNeckXRot`, `getTargetNeckYRot`, `getTargetTailXRot` and `getTargetTailYRot` return constants or sine waves keyed on the current Citadel `Animation` and its tick. Examples are ROAR (-140° neck pitch), STOMP (30°), SPEW_FLAMES, and LEFT/RIGHT_WHIP (±70° tail yaw). Otherwise the neck follows `yHeadRot - yBodyRot` plus the yaw buffer at lag 10, and the tail follows the yaw buffer at lag 20. `rotateOffsetVec` is `offset.xRot(-x°).yRot(-y°)`. Atlatitan overrides `getTargetNeckXRot` (AtlatitanEntity line 241). — [SauropodBaseEntity.java L434-L521](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L434-L521)
- **Tremorzilla.** It has 5 tail parts only (3×3, 2.5×2, 2.5×1.5, 2.5×1.5, 2×1) and no neck or head parts (lines 153-158). It uses the same buffered chain with tail rotation split in 5 steps. The rotate speed is 25 during tail-swipe animations, 20 when swimming and 5 otherwise. tail1's Y offset is -4 on land and 0 when swimming. — [TremorzillaEntity.java L153-158](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/TremorzillaEntity.java#L153-L158); [L666-697](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/TremorzillaEntity.java#L666-L697)
- **Hullbreaker.** It has 1 head (3×2) and 4 tail parts (2×2, 2×1.5, 2.5×1.5, 1.5×1) (lines 99-104).
  - The head is placed with `getYHeadRot()` and `fishPitch + getXRot()`.
  - Tail segments use yaw-buffer lags 2, 4, 6 and 8, plus a sideways X offset from `swimDegree(...)`. That is a cosine of `walkAnimation.position()` mixed with an idle sine, so the procedural swim wave is applied to the hitboxes.
  - `swimDegree` calls `AlexsCaves.PROXY.getPartialTicks()`.

  — [HullbreakerEntity.java L99-104](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/HullbreakerEntity.java#L99-L104); [L328-362](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/HullbreakerEntity.java#L328-L362)
- **Gossamer Worm and Corrodent.** These use a "trail transformations" ring buffer of `(pitch, fakeYRot)` pairs. Gossamer Worm has 5 parts sampled at lags 5, 10, 15, 20 and 25. Corrodent has 1 tail at lag 5. Each part is placed by `setToTransformation(offset, xRot, yRot)` relative to its `connectedTo` centre (Gossamer) or the parent centre (Corrodent). — [GossamerWormEntity.java L68-73, L149-182](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/GossamerWormEntity.java#L149-L182); [GossamerWormPartEntity.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/GossamerWormPartEntity.java); [CorrodentEntity.java L270-300](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/CorrodentEntity.java#L270-L300); [CorrodentTailEntity.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/CorrodentTailEntity.java)
- **Magnetron.** It has 10 parts: 5 `MagnetronJoint` values × left/right (SHOULDER, ELBOW, HAND, KNEE, FOOT, each with a base offset). Each part is a block-sized 0.9×0.9 box that carries a `BlockState`. `positionMultipart` lerps from the block position where the part was picked up toward `parent.position() + joint.getTargetPosition(...)` by `getFormProgress`. When the Magnetron is not "formed", each part copies the parent's position. — [MagnetronEntity.java L160-172, L380-424](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/MagnetronEntity.java#L380-L424); [MagnetronPartEntity.java L128-157](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/MagnetronPartEntity.java#L128-L157); [MagnetronJoint.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/util/MagnetronJoint.java)
- **Quarry Smasher.** It has 1 head part that moves a fraction (`fallSpeed`) of the way toward a target position each tick. This is a simple spring or lerp. — [QuarrySmasherEntity.java L261-280](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/item/QuarrySmasherEntity.java#L261-L280)
- **Not PartEntity: Gum Worm.** It uses a different design. `GumWormSegmentEntity extends Entity implements ICustomCollisions, …` is a full, separately tracked and synced entity. It links to its neighbours through synced `HEAD_ENTITY_ID`, `FRONT/BACK_ENTITY_UUID` and `FRONT/BACK_ENTITY_ID` fields and is registered as its own type (2×2). — [GumWormSegmentEntity.java L42-49](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/GumWormSegmentEntity.java#L42-L49); [ACEntityRegistry.java L98-99](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ACEntityRegistry.java#L98-L99)
- A search of the AC source for `PartEntity` and `multipart` matches none of the Tremorsaurus, Grottoceratops, Vallumraptor or Subterranodon entity files. Their registry boxes are Tremorsaurus 2.5×3.85, Grottoceratops 2.3×2.5, Vallumraptor 0.8×1.5 and Subterranodon 1.75×1.2. — [ACEntityRegistry.java L41-45](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ACEntityRegistry.java#L41-L45)

### Inferences
- The design is "procedural hitbox chain first". The server never reads model bones. Neck and tail poses are a separate, simplified kinematic model, controlled by per-animation target angles and turn-rate limits.
- The ring-buffered yaw gives a cheap follow-the-leader effect without IK: the tail swings after a turn because it samples older body yaws.

### Gaps
- I did not read every subclass override (for example, all of `LuxtructosaurusEntity`) to list each creature-specific target angle.

## Q3. Are parts synced to animations? How do they reach clients (interpolation, packets, IDs)? How is rendering done?

### Takeaway
Parts are **not networked**. Server and client each run `tickMultipart()` from the same synced inputs: position and body yaw from vanilla movement packets, head yaw, Citadel animation ID and tick, entity data such as SWIMMING and WALKING, and leg-solver heights. The client therefore rebuilds part boxes on its own. Rendering then works **in reverse** of bone-driven hitboxes: the model reads its neck and tail bone rotations from the part positions. Part entity IDs are not synchronised between server and client, which explains why AC uses its own client-to-server packet for hits and interactions.

### Cited Findings
- `SauropodBaseEntity.tick()` calls `AnimationHandler.INSTANCE.updateAnimations(this)` and `legSolver.update(...)`, then `tickMultipart()` (line 166) with no side check. On the client it also does its own `lerpTo`/`lSteps` position interpolation (lines ~167-180; lerpTo override at L415). — [SauropodBaseEntity.java L135-195](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L135-L195)
- Each `tickMultipart` saves positions before the move and writes them back into `xo/yo/zo/xOld/yOld/zOld` (Sauropod lines 258-261 and 297-304; the Tremorzilla, Hullbreaker, Gossamer, Corrodent and Magnetron versions do the same). The renderer can then use `Mth.lerp(partialTicks, part.xOld, part.getX())`, as `MagnetronRenderer` does at lines 71-74. — [MagnetronRenderer.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/client/render/entity/MagnetronRenderer.java)
- **Model driven by parts.** `SauropodBaseModel.positionNeckAndTail` calls `entity.neckPartN.calculateAnimationAngle(partialTicks, pitch)` and `tailPartN.calculateAnimationAngle(...)` and adds the results to the `neck`, `neck2`, `head`, `tail`, `tail2` and `tail3` bone rotations (lines 432-461). This is skipped when `straighten` (the sepia/book render) is set or `isFakeEntity()` is true. — [SauropodBaseModel.java L432-461](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/client/model/SauropodBaseModel.java#L432-L461)
- `calculateAnimationAngle` uses the interpolated positions of the part and of its `connectedTo` entity. When connected to the parent, it adjusts for leg-solver body offset (and +2·scale for neck1). It returns pitch from `atan2(dy, horizontal)` or yaw from `atan2(dz, dx)` plus the lerped parent body rotation. — [SauropodPartEntity.java L127-152](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodPartEntity.java#L127-L152); same pattern in [TremorzillaPartEntity.java L128-150](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/TremorzillaPartEntity.java#L128-L150) and [HullbreakerPartEntity.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/HullbreakerPartEntity.java)
- Server gameplay also reads part geometry:
  - The Atlatitan tail-whip AoE is centred on `tailPart2.position()` (AtlatitanEntity line 150), and head particles spawn at `headPart.position()` (lines 108-113).
  - Luxtructosaurus aims flames and tail effects from summed part `calculateAnimationAngle` values and part centres (lines 172-181).

  — [AtlatitanEntity.java L150](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/AtlatitanEntity.java#L150); [LuxtructosaurusEntity.java L172-181](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/LuxtructosaurusEntity.java#L172-L181)
- Part `getAddEntityPacket()` throws, and parts are not saved (`save` returns false, `shouldBeSaved` returns false). — [ACMultipartEntity.java L41-44, L82-84, L101-103](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/util/ACMultipartEntity.java#L41-L103)
- No AC entity overrides `setId` or `recreateFromPacket` to align part IDs. The only `recreateFromPacket` overrides are in the falling-block entities. Vanilla does this alignment only in `EnderDragon.recreateFromPacket`, and none of the NeoForge 1.21.1 patches that mention `getParts` (EnderDragon, ClientLevel, ServerLevel, IEntityExtension) assigns part IDs. — repo grep; [NeoForge code search for getParts](https://github.com/search?q=repo%3Aneoforged%2FNeoForge+getParts&type=code)
- **Hit and interact relay.** `MultipartEntityMessage(parentId, playerId, type)` is registered `playToServer`:
  - type 0 means interact and calls `parent.interact(player, player.getUsedItemHand())`.
  - type 1 means attack and calls `player.attack(parent)`, so weapon, enchantment and crit maths apply.

  The server checks `parent.isMultipartEntity()` and `player.distanceTo(parent) < 16`. — [MultipartEntityMessage.java L47-63](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/message/MultipartEntityMessage.java#L47-L63); [AlexsCaves.java L174-175](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/AlexsCaves.java)
- **Culling.** Renderers override `shouldRender` so the entity still renders when the main box is off-screen but any part's `getBoundingBoxForCulling()` (part box inflated by 2/0.5/2) is visible. Examples: AtlatitanRenderer L67-77, LuxtructosaurusRenderer L73-84, TremorzillaRenderer L211-222, HullbreakerRenderer L56-67. Sauropods also set `shouldRenderAtSqrDistance` to under 1024 blocks and inflate their own culling box by 2. — [AtlatitanRenderer.java L67-77](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/client/render/entity/AtlatitanRenderer.java#L67-L77); [SauropodBaseEntity.java L628-634](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L628-L634)

### Inferences
- Part IDs come from the global entity counter on each side. Parts are constructed in the parent constructor, so their IDs follow the parent's ID locally, but client IDs differ from server IDs. A vanilla `ServerboundInteractPacket` that targets a client part ID cannot resolve to the server part. AC's parent-ID relay packet works around this.
- Client and server part positions can drift slightly. Examples: `swimDegree` reads client partial ticks through the proxy, and the client has its own `lSteps` interpolation. No correction packet exists. This is acceptable because hit detection happens on the client and is relayed by parent ID.
- Hitboxes follow animation only to the extent that the per-animation target angles in `getTargetNeck/Tail*Rot` copy the keyframed motion. Bone keyframes in the model (for example legs and jaw) have no hitbox counterpart.

### Gaps
- I did not verify at runtime whether NeoForge 1.21.1 has another, non-patch mechanism that aligns part IDs (for example in `ClientboundAddEntityPacket` handling outside the files the code search returned). The conclusion is based on the code search and on AC's need for a relay packet.

## Q4. Block collision, entity collision and pushing, pathfinding, and main-hitbox sizing

### Takeaway
Parts are passive boxes moved by `setPos` each tick. They never call `move()`, do not collide with blocks, are not solid to other entities, do not push and are not pushed. Movement and pathfinding use only the parent's main box, which covers roughly the torso and legs. Neck and tail reach is covered only by parts. One exception is the Magnetron, where part geometry feeds back into the parent's hover height.

### Cited Findings
- Parts are placed only through `setPos` / `setPosCenteredY` (Sauropod lines 289-295, Tremorzilla 688-692, Hullbreaker 344-348, Gossamer `setToTransformation`). NeoForge `ServerLevel` does not tick `PartEntity` instances directly. — [SauropodPartEntity.java L158-160](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodPartEntity.java#L158-L160); [NeoForge ServerLevel.java.patch](https://github.com/neoforged/NeoForge/blob/1.21.1/patches/net/minecraft/server/level/ServerLevel.java.patch)
- `canBeCollidedWith()` and `isPickable()` on every part delegate to the parent. LivingEntity's `canBeCollidedWith` is false by default, so parts are not solid. Parts set `blocksBuilding = true`, which stops players from placing blocks inside part boxes. — [ACMultipartEntity.java L18-21, L47-58](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/util/ACMultipartEntity.java#L18-L58); [SauropodPartEntity.java L29-35, L66-77](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodPartEntity.java#L29-L77)
- Part dimensions scale with the parent: `size.scale(parent.getScale())` in the Sauropod and Tremorzilla parts, so babies get smaller parts. On a baby/adult change the parent calls `refreshDimensions()` on all parts (Sauropod lines 186-192). Hullbreaker, Gossamer, Corrodent and Magnetron parts use `EntityDimensions.fixed`. — [SauropodPartEntity.java L37-40](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodPartEntity.java#L37-L40); [SauropodBaseEntity.java L186-192](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L186-L192)
- Main boxes (registry `sized`):

  | Entity | Main box (width × height) |
  |---|---|
  | Atlatitan | 5.0 × 8.0 |
  | Luxtructosaurus | 6.0 × 8.5 |
  | Tremorzilla | 4.5 × 11 (swimming: 4.0 × 5.0 × scale, via `getDefaultDimensions`, L1302-1305) |
  | Hullbreaker | 4.65 × 4.5 |
  | Gossamer Worm | 1.15 × 0.5 |
  | Corrodent | 0.9 × 0.9 |
  | Magnetron | 0.8 × 2.0 |

  Tracking ranges: Atlatitan 11, Luxtructosaurus 12, Tremorzilla 11, Hullbreaker client range 20. — [ACEntityRegistry.java L34-86](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ACEntityRegistry.java#L34-L86); [TremorzillaEntity.java L1302-1305](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/TremorzillaEntity.java#L1302-L1305)
- Sauropods and Tremorzilla use `AdvancedPathNavigateNoTeleport`, a subclass of Citadel `AdvancedPathNavigate`. It passes `entity.getBbWidth()` and `getBbHeight()` (main box only) and overrides `canUpdatePath()` to always return true. Tremorzilla switches to `AllFluidsPathNavigator` in water. — [AdvancedPathNavigateNoTeleport.java L8-22](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/AdvancedPathNavigateNoTeleport.java#L8-L22); [TremorzillaEntity.java L170-185](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/TremorzillaEntity.java#L170-L185)
- Sauropod movement uses a custom `SauropodMoveHelper`. It moves forward only when the body is within 15° of the move direction and only during the "leg slam" phase of the walk cycle. Step height is 3.2 and max navigable distance to ground is 3. — [SauropodBaseEntity.java L539-541, L645-686](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L539-L686)
- Tremorzilla returns `isPushable() = false`. It overrides `push(Entity)` to nudge others by 0.05 per tick, and only pushes entities that are pushable or are `KaijuMob`. — [TremorzillaEntity.java L998-1028](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/TremorzillaEntity.java#L998-L1028)
- Sauropods are immune to `IN_WALL` (suffocation) damage. — [SauropodBaseEntity.java L624-626](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L624-L626)
- Magnetron feedback: `tickMultipart` finds the lowest part's `minY` and adjusts the parent's vertical `deltaMovement` so the body hovers the correct distance above ground. In this case the part layout drives movement. — [MagnetronEntity.java L380-L415](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/MagnetronEntity.java#L380-L415)

### Inferences
- Necks and tails pass through walls and terrain freely. The only gameplay effects of parts are click and ray targeting, AoE and projectile overlap through `Level.getEntities`, and block-placement blocking.
- Parts are absent from the main box, so A* path clearance considers only the torso width. Long necks and tails do not stop the creature from entering narrow spaces. Combined with no part block collision, this avoids stuck AI at the cost of visual clipping.
- `PartEntity.isPushable()` is not overridden (vanilla `Entity` returns false), and parts are not LivingEntities, so they do not run `pushEntities`. The result is no push in either direction.

### Gaps
- I did not trace vanilla `Entity`/`LivingEntity` default `isPushable`/`canBeCollidedWith` in decompiled form; the defaults come from general knowledge of the vanilla API, not from a source read here.

## Q5. Damage routing, isPickable, performance, and known issues

### Takeaway
Two damage-routing styles exist:
- **Newer `ACMultipartEntity` parts.** These forward server-side damage straight to `parent.hurt`.
- **Older per-creature parts (Sauropod, Tremorzilla, Hullbreaker, Magnetron).** These always return `false` from `hurt()`. They deal damage only when a **client** player's attack triggers the `MultipartEntityMessage` relay.

With the older style, purely server-side damage that lands on a part (projectiles, mob attacks, explosions hitting only a part) does nothing to the parent. Their projectile reduction factors (×0.33, ×0.35) are computed but never used. Per-tick cost is small vector maths. The main scaling cost is NeoForge's `Level.getEntities`, which loops over every part entity in the level for each query.

### Cited Findings
- **`ACMultipartEntity.hurt`.**
  - If the attacker is client-side, it sends `MultipartEntityMessage(parent, attacker, 1)` and returns true.
  - If the attacker is null or server-side, it returns `parent.hurt(source, amount)`.
  - `interact` both sends a type-0 message on the client and returns `parent.interact`.
  - `is(e)` returns true for the part or its parent.
  - Parts are `fireImmune`.

  — [ACMultipartEntity.java L23-80](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/util/ACMultipartEntity.java#L23-L80)
- **`SauropodPartEntity.hurt`.** It multiplies `amount` by 0.33 for projectiles. If the attacker is client-side and not allied, it sends the relay. It **always returns false**. `isInvulnerableTo` also uses the parent's invulnerability and ignores attacks from riders of the same vehicle. Tremorzilla parts are the same but use 0.35 and treat ACID as invulnerable. Hullbreaker parts use 0.35, and Magnetron parts follow the same send-only pattern. — [SauropodPartEntity.java L79-120](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodPartEntity.java#L79-L120); [TremorzillaPartEntity.java L62-98](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/TremorzillaPartEntity.java#L62-L98); [HullbreakerPartEntity.java](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/HullbreakerPartEntity.java); [MagnetronPartEntity.java L92-101](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/MagnetronPartEntity.java#L92-L101)
- Sauropod parent `hurt` applies its own projectile reduction (`getProjectileDamageReduction() = 0.75`) to projectiles that hit the main box. — [SauropodBaseEntity.java L617-622, L636-638](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L617-L638)
- `SauropodPartEntity.interact` plays `SoundEvents.ITEM_BREAK` and forwards to the parent, and also sends the relay on the client. Tremorzilla and Hullbreaker parts do the same. — [SauropodPartEntity.java L47-59](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodPartEntity.java#L47-L59)
- **Relay validation.** The relay handler checks only that the parent is multipart and within 16 blocks of the player (`distanceTo < 16`). It does not check line of sight or which part was hit. — [MultipartEntityMessage.java L47-63](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/message/MultipartEntityMessage.java#L47-L63)
- **Per-part query cost.** NeoForge `Level.getEntities` loops over `this.getPartEntities()` (all parts in the level) and tests AABB intersection for each part, on every call. — [NeoForge Level.java.patch](https://github.com/neoforged/NeoForge/blob/1.21.1/patches/net/minecraft/world/level/Level.java.patch)
- **Per-entity tick cost.** Each `tickMultipart` allocates a `Vec3[]` plus a few `Vec3` objects per part, does trig rotations, and keeps a 128-float ring buffer per entity (Sauropod, Tremorzilla, Hullbreaker). — [SauropodBaseEntity.java L247-305](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java#L247-L305)
- **Issue #866 (closed).** "Baby Tremorzillas will have their hitboxes turn massive when swimming in water." The current code scales `SWIMMING_SIZE` by `getScale()`, which matches a fix. — [AlexsCaves #866](https://github.com/AlexModGuy/AlexsCaves/issues/866); [TremorzillaEntity.java L1302-1305](https://github.com/AlexModGuy/AlexsCaves/blob/1.21.1/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/TremorzillaEntity.java#L1302-L1305)
- **Issue #831 (closed).** The reporter says the Tremorzilla heat ray cannot hit giant mobs (Atlatitan, Luxtructosaurus, Giant) "unless if the player aided TZ with their aim", and suspects that only certain spots of the hitbox can be hit. — [AlexsCaves #831](https://github.com/AlexModGuy/AlexsCaves/issues/831)
- **Issue #1267 (open).** Gum worm body parts get stuck and disconnect, leaving long ropes of segments. This concerns the separate-entity Gum Worm segments, not PartEntity. — [AlexsCaves #1267](https://github.com/AlexModGuy/AlexsCaves/issues/1267)
- **Issue #852 (closed).** "Dinosaurs have started to occasionally teleport when moving, causing them to clip into walls and die of suffocation." This is consistent with the `NoTeleport` navigator subclass and the `IN_WALL` immunity seen in the code, but the issue text does not mention parts. — [AlexsCaves #852](https://github.com/AlexModGuy/AlexsCaves/issues/852)
- Searches of the AC issue tracker for "multipart", "tail hitbox" and "can't hit" returned no results. — [AlexsCaves issues](https://github.com/AlexModGuy/AlexsCaves/issues)

### Inferences
- With the older part classes, melee by a client player on a part works through the relay. An arrow, trident, mob attack or Tremorzilla beam that intersects only a neck or tail part is likely to deal **no** damage, because server-side `part.hurt` returns false. The `amount *= 0.33F` line is dead code. This may be related to #831, but the beam code was not traced, so this is unconfirmed.
- The relay allows attacks on any multipart parent within 16 blocks, well beyond normal melee reach. A modified client could exploit it. A port should check reach against the specific part's box on the server.
- A client-side attack on a part may also send the vanilla attack packet with an unmatched part ID. The client-side `hurt` of the part runs and sends the relay. Invulnerability frames on the parent would absorb any duplicate hit.
- A port should prefer the newer `ACMultipartEntity` pattern: the server-side `parent.hurt` path covers projectiles and AoE. It should also align part IDs on the client (as vanilla `EnderDragon.recreateFromPacket` does) so that vanilla attack and interact packets resolve without a custom relay.
- Performance scales with (total part count in level) × (number of `getEntities` calls). That cost is paid by every AABB entity query in the world, not only queries near the creature. A few dozen parts are cheap. Hundreds of multipart mobs in one level would add measurable overhead.

### Gaps
- I did not trace the Tremorzilla beam or other AoE code paths (`hurtEntitiesAround` uses `getEntitiesOfClass(LivingEntity…)`, which excludes parts) to confirm which attacks can or cannot damage through parts.
- I found no profiler data or issue reports that quantify the server cost of AC multipart mobs.
- I did not verify in game whether vanilla attack packets with mismatched part IDs cause hits on wrong entities. This is code reasoning only.
