# Ice and Fire (1.20 branch) multipart entity / multi-hitbox system

Source snapshot: branch `1.20`, HEAD commit `0526aad94119b705a86207623481b087c4d052ca` (2025-09-05). All code links below are pinned to that commit, so line numbers stay valid. Path prefix for code: `src/main/java/com/github/alexthe666/iceandfire/`. Code was read from a shallow clone (read only, nothing built or run). Code is summarised, not copied.

Short names used for links below (all are full GitHub URLs in the text):
- `EntityMutlipartPart.java` (sic, the typo "Mutlipart" is in the real class name).

## Q1. Which classes implement parts? Forge PartEntity, vanilla EnderDragonPart, Citadel, or custom?

### Takeaway
The system is fully custom. Every part is a real, separately registered, server-spawned `Entity` (abstract base `EntityMutlipartPart extends Entity`), not a Forge `PartEntity` and not a vanilla `EnderDragonPart`. Citadel supplies only the animation side (`IAnimatedEntity`); it plays no part in hitboxes. There are four registered part entity types (dragon, "slow" chain part, hydra head, cyclops eye) and an empty marker interface `IMultipartEntity`.

### Cited Findings
- Base class `EntityMutlipartPart` is `public abstract class ... extends Entity` (L31). It holds four synched data fields: parent UUID (`OPTIONAL_UUID`), scale width, scale height and part yaw (L33-36). It also has plain (unsynched) fields `radius`, `angleYaw`, `offsetY`, `damageMultiplier` (L37-41). — [EntityMutlipartPart.java L31-41](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L31-L41)
- Main constructor: `(type, parent, radius, angleYaw, offsetY, sizeX, sizeY, damageMultiplier)`. It stores the parent UUID and the sizes in synched data, and converts `angleYaw` to radians after adding 90 degrees. — [EntityMutlipartPart.java L63-74](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L63-L74)
- Hitbox size comes from synched data: `getDimensions` returns non-fixed `EntityDimensions(scaleX, scaleY)`. — [EntityMutlipartPart.java L85-96](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L85-L96)
- Subclasses:
  - `EntityDragonPart`: only change is that `collideWithNearbyEntities()` is a no-op (no pushing). — [EntityDragonPart.java L30-32](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonPart.java#L30-L32)
  - `EntitySlowPart`: only change is `isSlowFollow()` returns true. Used by sea serpent and death worm. — [EntitySlowPart.java L30-33](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySlowPart.java#L30-L33)
  - `EntityHydraHead`: adds `headIndex` and a `neck` flag; overrides `hurt` and spawns blood particles for a severed head. — [EntityHydraHead.java](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydraHead.java)
  - `EntityCyclopsEye`: overrides `hurt` so that arrows hit the eye. — [EntityCyclopsEye.java](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityCyclopsEye.java)
- Four entity types are registered: `dragon_multipart`, `multipart` (slow), `hydra_multipart`, `cylcops_multipart` (sic). All are `MobCategory.MISC`, 0.5x0.5 default size, `fireImmune()`, with a custom client factory for the Forge spawn packet. No `clientTrackingRange`, `updateInterval` or `noSave()` is set on them. — [IafEntityRegistry.java L31-34](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/IafEntityRegistry.java#L31-L34)
- `IMultipartEntity` is an empty marker interface. — [IMultipartEntity.java](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/util/IMultipartEntity.java)
- It is implemented by `EntityDragonBase`, `EntityHydra` and `EntitySeaSerpent`. `EntityDragonBase` also implements Citadel's `IAnimatedEntity` and the mod's own `ICustomSizeNavigator`. — [EntityDragonBase.java L99](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L99); [EntityHydra.java L37](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydra.java#L37); [EntitySeaSerpent.java L61](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L61)
- Shared helper `EntityUtil.updatePart(part, parent)` (server only). If the part is not yet in the world and not removed, it checks for a UUID clash. On a clash it makes new random UUIDs until one is free, then calls `addFreshEntity(part)`. At the end it always calls `part.setParent(parent)`. — [EntityUtil.java L15-38](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/util/EntityUtil.java#L15-L38)
- A maintainer (TheBv) wrote that ideally the whole multipart system would be replaced with the default Forge/vanilla one. He said he tried this about a year earlier and might look at it again. It has not happened on 1.20 (the custom classes are still at HEAD). — [PR #5117 comments](https://github.com/AlexModGuy/Ice_and_Fire/pull/5117)

### Per-creature part sets (all values are multiplied by a scale factor)
- **Dragon** (`EntityDragonBase`): 10 `EntityDragonPart` fields (L223-232). `resetParts(scale)` (L339-371) builds them. Arguments are radius, angle, offsetY, width, height, damage multiplier:
  - head: 1.55, 0°, 0.6, 0.5x0.35, x1.5
  - neck: 0.85, 0°, 0.7, 0.5x0.2, x1
  - right wing upper: 1.0, 90°, 0.5, 0.85x0.3, x0.5
  - right wing lower: 1.4, 100°, 0.3, 0.85x0.2, x0.5
  - left wing upper and lower: same as right, at -90° and -100°
  - tail1 to tail4: radius -0.75 / -1.15 / -1.5 / -1.95, small boxes, x1 (tail4 is x1.5)

  — [EntityDragonBase.java L339-371](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L339-L371)
- **Sea serpent**: 9 `EntitySlowPart` segments. Segments 0-3 are made with radius -0.4·scale (box 0.45x0.4). Segments 4-8 are made with radius 0.5·scale (box 0.5x0.5). All use angle 180. At construction, each segment's parent is the previous segment, so the design is two chains: 0-3 from the body, and 4-8 starting again from the body. — [EntitySeaSerpent.java L212-225](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L212-L225)
- **Death worm**: 7 `EntitySlowPart` segments, all parented to the worm. Radius is (-0.8 - 0.8·i)·scale, box 0.7·scale cube, so they form a fixed line behind the worm. — [EntityDeathWorm.java L209-216](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDeathWorm.java#L209-L216)
- **Hydra**: up to 9 heads (`HEADS = 9`), with a head box and a neck box per head.
  - Head boxes: radius 3.2, size 0.75x1.75.
  - Neck boxes: radius 2.1, size 0.75x0.75.
  - Angles come from a hard-coded fan table `ROTATE[headCount-1][i]`, multiplied by 1.1. The table goes up to ±50° for 9 heads.

  — [EntityHydra.java L44-54](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydra.java#L44-L54); [EntityHydra.java L239-251](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydra.java#L239-L251)
- **Cyclops**: one `EntityCyclopsEye`: radius 0.2, offsetY 7.4, size 1.2x0.5. It is created lazily in `aiStep` and registered through `EntityUtil.updatePart`. — [EntityCyclops.java L321-326](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityCyclops.java#L321-L326)

### Inferences
- This is the classic pre-`PartEntity` pattern: separate tracked entities that find their parent by UUID each tick. Every cost and failure in Q3-Q6 follows from that choice (full entity per part, entity tracking, chunk save, UUID registry).

### Gaps
- I did not check older branches (1.16, 1.18, 1.19) for a Forge `PartEntity` attempt. The maintainer mentions an attempt about a year before Jan 2024, but I found no PR for it.

## Q2. How are part positions computed and rotated? Are they synced with animations? How does the client get positions?

### Takeaway
Positions are computed on the server only. Each part sits at a fixed offset from the parent: a polar offset (radius, angle) around the parent's body yaw `yBodyRot`, plus a fixed Y offset. Nothing reads the model animation, pitch, flight pose or neck pose (one exception: the hydra makes a small Y change from walk speed).

"Slow" parts use the parent's previous-tick position, so they trail by one tick. The client never computes part positions. It receives each part as an ordinary tracked entity through vanilla position packets. The rendered models (dragon, sea serpent) use their own client-side chain buffers and do not read the parts.

### Cited Findings
- Part `tick()`:
  - Does nothing for the first 10 ticks.
  - Calls `refreshDimensions()` every tick.
  - Only on the server and with a non-null parent: yaw = parent `yBodyRot` if the parent is a `LivingEntity`, else parent `getYRot()`.
  - Normal part: position = parent position + radius·(cos, sin)(yaw + angleYaw), Y = parent Y + offsetY, then `markHurt()`.
  - Slow part: same, but from parent `xo/yo/zo` (last tick). It also sets its own XRot toward the parent, limited to 5° per tick (a copy of `MoveControl.rotlerp`), sets YRot to the parent yaw, and stores it in synched `PART_YAW`.
  - Discards itself if the parent is removed, or if the parent is still null after 20 ticks.

  — [EntityMutlipartPart.java L131-195](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L131-L195)
- `getParent()` returns `serverLevel.getEntity(uuid)` only when the level is a `ServerLevel`. Otherwise it returns null, so on the client every part has a null parent. — [EntityMutlipartPart.java L203-215](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L203-L215)
- Spawning uses the Forge `NetworkHooks.getEntitySpawningPacket`. — [EntityMutlipartPart.java L227-230](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L227-L230)
- Hydra override: after the base tick, the hydra sets each head box Y to `hydra.Y + (1 - walkAnimation.speed()*0.5)`. This is the only animation-coupled change found. — [EntityHydra.java L253-269](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydra.java#L253-L269)
- The dragon calls `refreshDimensions()` then `updateParts()` at the start of `tick()` on both sides. `updateParts` returns early on the client. — [EntityDragonBase.java L1716-1720](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L1716-L1720)
- The dragon model is animated client-side with its own buffers (`IFChainBuffer` roll/pitch, `ReversedBuffer` turn, `ChainBuffer` tail, quadruped leg solver). These buffers are created only when `isClientSide`. The constructor also sets `noCulling = true` and calls `resetParts(1)`. — [EntityDragonBase.java L250-263](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L250-L263)
- The sea serpent visual tail uses a yaw/pitch history: `tailYaw[0] = yBodyRot`, and each later index copies the previous tick's value of the index before it. The renderer reads it through `getPieceYaw/getPiecePitch(index, partialTicks)`. — [EntitySeaSerpent.java L295-324](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L295-L324); [SeaSerpentTabulaModelAnimator.java L74-80](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/client/model/animator/SeaSerpentTabulaModelAnimator.java#L74-L80)
- No class under `client/` references `EntityMutlipartPart`, `EntityDragonPart` or the dragon part fields (grep of the snapshot). Rendering does not depend on the parts. — [client source tree](https://github.com/AlexModGuy/Ice_and_Fire/tree/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/client)
- Death worm special case: `setDeathWormScale` rebuilds segments on the server and sends `MessageDeathWormHitbox(id, scale)` to all clients. The client handler calls `initSegments(scale)`, which only creates local part objects that are never added to the client world. — [EntityDeathWorm.java L379-388](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDeathWorm.java#L379-L388); [MessageDeathWormHitbox.java](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/message/MessageDeathWormHitbox.java)

### Sea serpent chain: a likely regression
- Design intent: `resetParts` sets each segment's parent to the previous segment. Each `EntitySlowPart` follows its parent's previous-tick position and yaw. Together this gives a lagging snake chain. — [EntitySeaSerpent.java L212-225](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L212-L225)
- `onUpdateParts()` runs every tick and calls `EntityUtil.updatePart(segment, this)` for each segment. `updatePart` ends with `part.setParent(parent)`, which sets the parent of every segment back to the serpent itself. — [EntitySeaSerpent.java L227-231](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L227-L231); [EntityUtil.java L37](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/util/EntityUtil.java#L37)
- This per-tick `setParent` for the serpent was added by PR #5117 ("Attempt to fix MultiPart issues", merged into 1.20 on 2024-02-12). Before it, the serpent's `onUpdateParts` only called `addFreshEntity` and did not call `setParent`. — [PR #5117 diff](https://github.com/AlexModGuy/Ice_and_Fire/pull/5117/files)

### Inferences
- Since PR #5117, all front segments (0-3) of the sea serpent should sit on the same point (-0.4·scale from the serpent), and all back segments (4-8) on another point (0.5·scale). The chain is flattened. This comes from reading the code only; I did not test it in game.
- Parts sync only through vanilla entity tracking. Because the part types set no tracking values, they use the vanilla `EntityType.Builder` defaults (5-chunk client tracking range, update interval 3). The dragon types use `clientTrackingRange(10)`. On the client, part hitboxes are therefore interpolated, delayed copies of server positions. They can disappear before the dragon does at long range. They do not follow wing flaps, neck turns, flight pitch or the tail sway the model shows. (The defaults are vanilla knowledge and were not checked in this repo; dragon tracking is in [IafEntityRegistry.java L38-40](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/IafEntityRegistry.java#L38-L40).)
- `markHurt()` is called every server tick on every part (L149, L157). In vanilla, `hurtMarked` makes the server entity tracker send a velocity packet for that entity. This probably adds a small per-tick packet per part per watching player.

### Gaps
- I did not run the game, so the flattened serpent chain and the client lag are inferred from code, not seen.

## Q3. Block collision, movement, pushing, pathfinding for huge bodies; main hitbox vs model; growth with age

### Takeaway
Parts never call `move()`. They are teleported with `setPos` every tick, so they pass through blocks and never block or steer the parent. Only the main hitbox collides with terrain. The main hitbox is much smaller than the model: the dragon base box is 0.78x1.2, scaled by `min(renderSize*0.35, 7)`.

Big dragons handle terrain in two ways: they break blocks around their main box, and they use a Minecolonies-derived async A* pathfinder (`AdvancedPathNavigate`) that sweeps a volume sized from the main box (`ICustomSizeNavigator`). The pathfinder ignores wings and tail.

Non-dragon parts push nearby entities away from the parent. Dragon parts do not push.

### Cited Findings
- Pushing:
  - `collideWithNearbyEntities()` queries entities in the part box (expanded by 0.2) and calls `entity.push(parent)` on each one that is pushable, not the parent, not a rider and not another part. — [EntityMutlipartPart.java L232-253](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L232-L253)
  - On the slow-follow path it runs twice per tick (L152-154 and again at L159-161). — [EntityMutlipartPart.java L142-161](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L142-L161)
  - The dragon part overrides it as a no-op. — [EntityDragonPart.java L30-32](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonPart.java#L30-L32)
- The sea serpent overrides `pushEntities()` so its main body does not push its own parts. — [EntitySeaSerpent.java L159-163](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L159-L163)
- The sea serpent's touch check `isTouchingMob` tests the main box and every segment box, each expanded by 1. — [EntitySeaSerpent.java L622-632](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L622-L632)
- Parts ignore environmental damage: fall, drown, in-wall, falling block, lava and the fire tag. Water splash is turned off and `wasTouchingWater` is reset each tick. — [EntityMutlipartPart.java L58-61](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L58-L61); [EntityMutlipartPart.java L273-276](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L273-L276)
- Main hitbox and growth:
  - Dragon types are registered at 0.78x1.2. — [IafEntityRegistry.java L38-40](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/IafEntityRegistry.java#L38-L40)
  - `getDimensions` scales that box by `getScale() = min(renderSize*0.35, 7)`. — [EntityDragonBase.java L1795-1803](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L1795-L1803)
  - `renderSize` interpolates over 5 growth stages: 1-3, 3-7, 7-12.5, 12.5-20, 20-30, by age in days (25 days per stage, capped after day 125). — [EntityDragonBase.java L181-185](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L181-L185); [EntityDragonBase.java L1809-1820](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L1809-L1820)
- Parts scale on growth: `refreshDimensions()` runs every tick. When the scale value changes, it rebuilds all parts with `resetParts(renderSize/3)`, which discards the old part entities and creates new ones. — [EntityDragonBase.java L1695-1709](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L1695-L1709)
  - The sea serpent does the same, keyed on `getSeaSerpentScale()`. — [EntitySeaSerpent.java L256-264](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L256-L264)
- Step height grows with age: `max(1.2, 1.2 + (min(age,125)-25)*1.8/100)`. — [EntityDragonBase.java L1711-1714](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L1711-L1714)
- Dragon navigation:
  - `createNavigator` builds an `AdvancedPathNavigate` (movement type WALKING or FLYING). `switchNavigator(0/1/2)` swaps both the move control (ground, flight or player-flight helper) and the navigator. The ground navigator gets a stuck handler with 5 teleport steps. — [EntityDragonBase.java L451-490](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L451-L490)
  - The pathfinder code says it is used with permission from Raycoms of the Minecolonies project. — [AdvancedPathNavigate.java L1-4](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/pathfinding/raycoms/AdvancedPathNavigate.java#L1-L4)
  - Path jobs run on a `ThreadPoolExecutor`. — [AdvancedPathNavigate.java L244](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/pathfinding/raycoms/AdvancedPathNavigate.java#L244); [Pathfinding.java L37-39](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/pathfinding/raycoms/Pathfinding.java#L37-L39)
  - Config: "Dragon Pathfinding Threads" (default 3) and "Dragon Max Pathfinding Nodes" (default 5000). — [ServerConfig.java L324-325](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/config/ServerConfig.java#L324-L325)
- Size-aware path checks:
  - The `ICustomSizeNavigator` interface has `isSmallerThanBlock()`, `getXZNavSize()` and `getYNavSize()`. — [ICustomSizeNavigator.java](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/pathfinding/raycoms/pathjobs/ICustomSizeNavigator.java)
  - The dragon returns XZ = `max(1.4, bbWidth/2)` and Y = `ceil(bbHeight)`, and is never "smaller than a block". — [EntityDragonBase.java L2911-2924](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L2911-L2924)
  - `AbstractPathJob.setEntitySizes` reads these values. — [AbstractPathJob.java L453-464](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/pathfinding/raycoms/pathjobs/AbstractPathJob.java#L453-L464)
  - `isPassableBBFull` checks a cube of `(XZ+1)·(Y+1)·(XZ+1)` block positions. `isPassableBB` and `isPassableBBDown` check a sideways slice perpendicular to the move direction. — [AbstractPathJob.java L1340-1396](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/pathfinding/raycoms/pathjobs/AbstractPathJob.java#L1340-L1396)
- Block breaking:
  - `breakBlocks` runs from `customServerAiStep` on a cooldown (`dragonBreakBlockCooldown`, default 5 ticks). For stage 3+ dragons, with mob griefing and `canGrief`, it breaks every breakable block in the main box expanded by 1.
  - `isBlockExplicitlyPassable` lets the pathfinder treat breakable blocks at or above the dragon's feet as passable. The hardness limit is 2.0 or 5.0, depending on stage and griefing config.

  — [EntityDragonBase.java L1451-1497](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L1451-L1497); [ServerConfig.java L196](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/config/ServerConfig.java#L196)
  - When `dragonDigWhenStuck` is on, a stuck dragon force-breaks blocks. — [EntityDragonBase.java L1756-1759](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L1756-L1759)
- Other creatures have their own navigators: the sea serpent swaps between vanilla `GroundPathNavigation` and a custom `SeaSerpentPathNavigator` with a swimming move helper. The pathfinding package also has `PathNavigateCyclops`, `PathNavigateDeathWormLand/Sand` and `NodeProcessorDeathWorm`. — [EntitySeaSerpent.java L165-176](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L165-L176); [pathfinding/](https://github.com/AlexModGuy/Ice_and_Fire/tree/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/pathfinding)
- Config "Dragon Moved Wrongly Error Fix" is marked `// FIXME :: Unused`. — [ServerConfig.java L108](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/config/ServerConfig.java#L108)

### Inferences
- The main box caps at about 5.5x8.4 blocks (0.78·7 x 1.2·7). The rendered dragon is much larger at renderSize up to 30. Parts scaled by renderSize/3 (up to x10) fill the gap for hit detection only. Terrain interaction (collision, pathing, block breaking) uses only the main box, so wings and tail clip through terrain. Block breaking around the main box hides most of this for adult dragons.
- The pathfinder's volume sweep costs about XZ²·Y passability checks per node, so it gets expensive for adults. The thread pool and the node cap are the mitigations.

### Gaps
- I did not trace `IafDragonFlightManager` flight path logic or `PathingStuckHandler` teleport details.

## Q4. Damage routing, per-part multipliers, isPickable, interaction, riding

### Takeaway
Non-melee damage on a part goes to `parent.hurt(source, damage * damageMultiplier)`. Player melee does not use the multiplier: a Forge `AttackEntityEvent` handler cancels the hit on the part and runs `player.attack(parent)` instead. Parts are pickable, so the crosshair selects them. Right-click forwards to `parent.interact`. Hydra heads and the cyclops eye route damage to special handlers.

### Cited Findings
- Part `hurt`: returns `parent != null && parent.hurt(source, damage * damageMultiplier)`. On the client it would send `MessageMultipartInteract(parentId, damage*mult)`, but only when the parent is non-null, and on the client the parent is always null. `interact` forwards to `parent.interact` in the same way. — [EntityMutlipartPart.java L255-271](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L255-L271)
- `isPickable()` returns true. `is(entity)` returns true for the part itself or its parent, so vanilla "is this the same entity" checks treat a part as its parent. — [EntityMutlipartPart.java L217-225](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L217-L225)
- Melee path (`ServerEvents.onPlayerAttackMob`):
  - Cancels the event when the target is a part.
  - Calls `player.attack(parent)` inside a try/catch that logs "Exception thrown while interacting with entity."
  - For a hydra head, calls `triggerHeadFlags(headIndex)`.
  - On the client, sends `MessagePlayerHitMultipart(parentId, extra)` if the parent is non-null.

  — [ServerEvents.java L208-229](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/event/ServerEvents.java#L208-L229)
- Network messages: both handlers look up the entity by ID and act only within 100 blocks. `MessageMultipartInteract` calls `hurt(mobAttack(player), dmg)` or `interact`. `MessagePlayerHitMultipart` calls `player.attack(entity)` plus the hydra head flags. — [MessageMultipartInteract.java L40-68](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/message/MessageMultipartInteract.java#L40-L68); [MessagePlayerHitMultipart.java L44-72](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/message/MessagePlayerHitMultipart.java#L44-L72)
- Dragon multipliers (see Q1): head x1.5, tail tip x1.5, neck and body tail x1, wings x0.5. — [EntityDragonBase.java L339-371](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L339-L371)
- The dragon's own `hurt` ignores in-wall, falling block and cramming damage. It also ignores damage from its controlling rider and arrow damage while the dragon is itself a passenger. — [EntityDragonBase.java L1661-1692](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L1661-L1692)
- Hydra head `hurt`: calls `hydra.onHitHead(damage, headIndex)`, then `parent.hurt(source, damage)` without the multiplier. — [EntityHydraHead.java L49-58](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydraHead.java#L49-L58)
- `onHitHead(damage, headIndex)` and `triggerHeadFlags(index)` only store `lastHitHead = index` (L464-470). The hydra's own `hurt` then adds the damage to `headDamageTracker[lastHitHead]`. Past a threshold it severs that head (`setSeveredHead`). If the hit would kill a hydra that has more than one head, it sets the damage to 0 (L350-366). Head targeting is therefore stateful: "last head hit", not per-hit routing. — [EntityHydra.java L350-366](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydra.java#L350-L366); [EntityHydra.java L464-470](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydra.java#L464-L470)
- Cyclops eye `hurt`: arrow damage calls `cyclops.onHitEye` and returns true (the body takes no normal damage). Other damage passes to the parent with no multiplier. — [EntityCyclopsEye.java L26-35](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityCyclopsEye.java#L26-L35)
- Teams and ownership: `DragonUtils.onSameTeam` resolves a part to its parent's owner. — [DragonUtils.java L366-390](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/util/DragonUtils.java#L366-L390)
- Self-hit exclusions:
  - Dragon charges skip `EntityDragonPart`. — [EntityDragonCharge.java L154](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonCharge.java#L154)
  - Sea serpent bubbles skip all parts. — [EntitySeaSerpentBubbles.java L107](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpentBubbles.java#L107)
  - The hydra's targeting predicate excludes parts. — [EntityHydra.java L110](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydra.java#L110)
  - The sea serpent's `HurtByTargetGoal` ignores `EntityMutlipartPart`. — [EntitySeaSerpent.java L149](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L149)
- Riding: the part pusher skips entities that ride the parent (recursive `sharesRider`). — [EntityMutlipartPart.java L236-253](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L236-L253)
- Issue: dragon horns could not pick up a dragon. A reporter traced it to the "Dragon Part" being the targeted entity when the Entity Culling mod is installed. Turning on F3+B hitboxes, which makes `RenderNothing` render parts, worked around it. — [Issue #5196](https://github.com/AlexModGuy/Ice_and_Fire/issues/5196); [RenderNothing.java L24-29](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/client/render/entity/RenderNothing.java#L24-L29)

### Inferences
- Because `getParent()` is null on the client, the client-side network paths (`MessageMultipartInteract` in `hurt`/`interact`, and `MessagePlayerHitMultipart` in `ServerEvents`) look like dead code in 1.20. Hits still work because vanilla sends the attack/interact packet for the part's entity ID, and the server-side `AttackEntityEvent` or `interact` forwards it to the parent.
- On the client, a part's `interact` returns PASS. Item-on-entity logic that needs a `LivingEntity` target (for example the dragon horn) can fail when the crosshair picks a part. This fits the cause in #5196.
- Melee ignores part multipliers (no head bonus for swords), but projectiles and explosions get them. One explosion overlapping several parts calls `parent.hurt` several times. Vanilla invulnerability frames should limit this to the highest hit (vanilla behaviour, not checked here).

### Gaps
- I did not trace the `headDamageThreshold` value or `EntityCyclops.onHitEye` beyond their entry points.

## Q5. Rendering, lifecycle, persistence and performance

### Takeaway
Parts render with `RenderNothing`, so they are invisible unless F3+B hitbox display is on. Large parents set `noCulling = true` so their oversized models are not culled by the small main box.

Each part is a full entity: it is spawned, tracked and ticked, and since it does not opt out of saving, it is also saved to chunk NBT. On reload, a saved part has no parent (the parent UUID is synched data only) and discards itself after 20 ticks. The parent then spawns fresh parts. Every size change removes and re-spawns all parts.

### Cited Findings
- `RenderNothing` is registered for all four part types. Its `shouldRender` returns false unless `shouldRenderHitBoxes()` is on. — [IafClientSetup.java L109-112](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/client/IafClientSetup.java#L109-L112); [RenderNothing.java L24-29](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/client/render/entity/RenderNothing.java#L24-L29)
- `noCulling = true` is set on the dragon, the sea serpent and the death worm. — [EntityDragonBase.java L259](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L259); [EntitySeaSerpent.java L109](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L109); [EntityDeathWorm.java L104](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDeathWorm.java#L104)
- Part save methods are empty, and the part does not override `shouldBeSaved`. — [EntityMutlipartPart.java L48-56](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L48-L56)
- `remove()` always removes with `DISCARDED`, whatever reason it is given. — [EntityMutlipartPart.java L198-201](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L198-L201)
- `shouldContinuePersisting()` is `isAddedToWorld() || isRemoved()`. `updatePart` uses it to decide whether to call `addFreshEntity`. — [EntityMutlipartPart.java L278-280](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L278-L280)
- Parent removal also removes the parts:
  - Dragon `remove()` calls `removeParts()` (discards all 10). — [EntityDragonBase.java L563-567](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L563-L567); [EntityDragonBase.java L373-414](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDragonBase.java#L373-L414)
  - The sea serpent, hydra and cyclops do the same. — [EntitySeaSerpent.java L233-245](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntitySeaSerpent.java#L233-L245); [EntityHydra.java L289-301](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydra.java#L289-L301); [EntityCyclops.java L373-377](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityCyclops.java#L373-L377)
  - The death worm calls `kill()` then `remove(KILLED)`. — [EntityDeathWorm.java L224-230](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityDeathWorm.java#L224-L230)
- Hydra bug: the tick loop calls `EntityUtil.updatePart(headBoxes[HEADS + 1], this)` (constant `1`) instead of `HEADS + i`. Only neck box index 1 is ever added to the world or re-parented. — [EntityHydra.java L263-269](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityHydra.java#L263-L269)
  - The pre-PR code used `HEADS + i`, so PR #5117 introduced the typo. — [PR #5117 diff](https://github.com/AlexModGuy/Ice_and_Fire/pull/5117/files)
- Per-tick work per part (server):
  - one UUID lookup (`getParent`)
  - `refreshDimensions`
  - `setPos` and `markHurt`
  - for non-dragon parts, one or two AABB entity queries

  Per parent tick, `updatePart` runs for each part. — [EntityMutlipartPart.java L131-170](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java#L131-L170)
- Part counts: dragon 10, sea serpent 9, death worm 7, hydra up to 18 allocated (9 heads + 9 necks), cyclops 1. — sources in Q1.

### Inferences
- Ghost and orphan parts: parts are saved with the chunk but carry no parent link on disk. After a reload they stay as parentless entities for about 20 ticks, then remove themselves (L165-166). A part whose chunk unloads separately from its parent (for example a wing across a chunk border) is marked removed. `shouldContinuePersisting()` then returns true, so `updatePart` never re-adds it. That hitbox is lost until the next `resetParts` (a scale change). I inferred this from code; I did not test it.
- The "UUID of added entity already exists" spam (Q6) fits this lifecycle. An in-memory part object and a copy reloaded from chunk NBT can share a UUID. A different part object can then be re-added under a UUID the entity manager already knows. The UUID re-roll in `EntityUtil` only checks `serverLevel.getEntity(uuid)` (loaded entities). It may miss UUIDs that `PersistentEntitySectionManager` tracks but that are not currently loaded. That could explain why reports continue after PR #5117.
- Rebuilding all parts on every scale change (`resetParts` from `refreshDimensions`) causes entity churn: remove packets, spawn packets and new UUIDs. For dragons this happens whenever `getScale()` changes, which is each growth step.

### Gaps
- No issue gives measured TPS or MSPT numbers for multipart overhead. The performance notes above come from reading the code.

## Q6. Known problems in the issue tracker and fixes

### Takeaway
The main recurring multipart bug is the log flood "UUID of added entity already exists: EntityDragonPart". It is reported from 1.18 through 1.20.1 beta-5. Some reports say it bloats logs to hundreds of MB and drops TPS. PR #5117 (Jan-Feb 2024) tried to fix it by centralising part spawning in `EntityUtil.updatePart` with UUID re-rolling, but newer 1.20.1 reports are still open. The maintainers discussed replacing the system with Forge/vanilla parts but have not done it. Other reports: part entities break item interactions, and melee hits on dragons throw exceptions.

### Cited Findings
- #4945 (1.18.2, closed): dragon spams "UUID of added entity already exists" for EntityDragonPart and causes server lag. The maintainer asked whether a modified server (Magma) was used. — [Issue #4945](https://github.com/AlexModGuy/Ice_and_Fire/issues/4945)
- #4954 (1.18, closed): the same spam, thousands of lines, and the server crashed. The maintainer blamed the crash on Apotheosis and the UUID spam on non-Forge servers (Magma); the reporter confirmed Magma. A Dec 2024 commenter said it also happens on plain Forge servers. — [Issue #4954](https://github.com/AlexModGuy/Ice_and_Fire/issues/4954)
- #5089 (1.20.1 beta-2, open): a lightning dragon spams the log. A maintainer pointed to the DEUF (duplicate entity UUID fix) mod and called it apparently a vanilla bug. Workarounds were teleporting to the dragon and relogging, or killing the entity. — [Issue #5089](https://github.com/AlexModGuy/Ice_and_Fire/issues/5089)
- PR #5117 "Attempt to fix MultiPart issues": "might fix #5089", merged into `1.20` on 2024-02-12.
  - It added `EntityUtil.updatePart` (UUID clash re-roll + `addFreshEntity` + `setParent`).
  - It replaced the per-creature add logic for dragon, sea serpent, death worm, hydra and cyclops.
  - It removed `shouldNotExist()` and `canBreatheUnderwater()` from parts.
  - It made `getParent` use an `instanceof ServerLevel` check.
  - A companion PR #5118 targeted 1.19.2.

  — [PR #5117](https://github.com/AlexModGuy/Ice_and_Fire/pull/5117); [PR #5118](https://github.com/AlexModGuy/Ice_and_Fire/pull/5118)
- #5412 (1.20.1 beta-5, open, Nov 2024): two EntityDragonPart UUID warnings repeat several times per second. The log grew past 800,000 lines / 200 MB, and several dragons did it (spawned and tamed). — [Issue #5412](https://github.com/AlexModGuy/Ice_and_Fire/issues/5412)
- #5413 (1.20.1 beta-5, open): the same spam with crashes. The reporter says it had been broken for 11 months. A commenter used the Log Begone mod to stop the TPS drop and said it also happened in a 1.18.2 pack. — [Issue #5413](https://github.com/AlexModGuy/Ice_and_Fire/issues/5413)
- #5360 "Server console flooded" and #5337 are open 2024 reports that matched a UUID/dragon-part search. I did not read their bodies. — [Issue #5360](https://github.com/AlexModGuy/Ice_and_Fire/issues/5360)
- #5196 (1.20.1, open): dragon horns fail to pick up dragons because the targeted entity is a "Dragon Part". The reporter reproduced it with only Ice and Fire + Entity Culling 1.7.0, and turning on hitbox rendering worked around it. — [Issue #5196](https://github.com/AlexModGuy/Ice_and_Fire/issues/5196)
- #4520 (1.16.5, open): melee hits on dragons log "Exception thrown when interacting with entity" and sometimes crash during rendering. This is the same warning text that the multipart melee forwarder in `ServerEvents` logs. — [Issue #4520](https://github.com/AlexModGuy/Ice_and_Fire/issues/4520); [ServerEvents.java L213-220](https://github.com/AlexModGuy/Ice_and_Fire/blob/0526aad94119b705a86207623481b087c4d052ca/src/main/java/com/github/alexthe666/iceandfire/event/ServerEvents.java#L213-L220)
- #4862 (1.18.2, open): the Domestication Innovation Herding enchant crashes near dragons. The stack trace is in Ice and Fire's pathfinding thread factory (`Pathfinding$IafThreadFactory.newThread`), so it is pathfinding, not parts. — [Issue #4862](https://github.com/AlexModGuy/Ice_and_Fire/issues/4862)
- Git history: after PR #5117 (commit `1c8d43d2`, "generalize", 2024-01-23), `EntityMutlipartPart` changed only once more: `75763b24`, which added a source comment. No later multipart fix exists on 1.20 up to HEAD (Sep 2025). — [commits for EntityMutlipartPart.java on 1.20](https://github.com/AlexModGuy/Ice_and_Fire/commits/1.20/src/main/java/com/github/alexthe666/iceandfire/entity/EntityMutlipartPart.java)

### Inferences
- The UUID spam was never fully fixed on 1.20.1. The open beta-5 reports are dated after the Feb 2024 merge, though I did not confirm that beta-5 includes the PR #5117 code. Root cause is unconfirmed. The maintainers' own view moved toward replacing the system with Forge/vanilla `PartEntity`, which avoids separate UUIDs, saving and tracking for parts.
- Lessons for a new design:
  - Do not make parts saved, tracked, UUID-bearing world entities.
  - Do not re-parent chain segments each tick.
  - Compute part positions on both sides from synced parent state, instead of relying on per-part tracking packets.
  - Drive part poses from the same data the animation uses if hitboxes must match the model.

### Gaps
- I found no issue that directly reports the flattened sea serpent chain or the hydra `HEADS + 1` typo. Both come from code reading only.
- No wiki or design document on the multipart system was found. I did not search the CurseForge or Modrinth comment threads.
