# Oriented collision/hit shapes and server-side animation in Minecraft mods (research notes, 2026-10-09)

Scope: what existing mods, libraries and official docs do for oriented (rotated) or compound shapes, and for server-side skeleton posing. The goal is to judge the Project Nublar candidate hybrid: torso-sized vanilla box plus a per-species procedural spine for spacing and section hit parts.

Method note: public sources only (GitHub raw source on branch heads, Microsoft Learn, minecraft.wiki, Modrinth/CurseForge pages). No jars were decompiled and no local game files were read. The WebFetch tool could not show commit SHAs for the GitHub files, so links are pinned to **branch names as read on 2026-10-09**, not SHAs. Re-pin to SHAs before you quote these as stable references.

## Q1. Is there any proven way to give a Minecraft mob an oriented or compound collision shape that pathfinding and vanilla physics accept? What breaks?

### Takeaway
No. None of the systems checked gives a *mob's own* movement collision an oriented or compound shape that vanilla pathfinding understands. Every system that uses OBBs either (a) uses them only for **hit tests** (Epic Fight, Better Combat, SentinelLib, DoxHitbox), or (b) rotates the **world/contraption into local space** and tests the entity's ordinary AABB against it (Create, Valkyrien Skies). The vanilla entity box stays an unrotated cuboid with one width value. The only proven fixes for big-mob movement are navigation-side: whole-box sweeps and node clearance checks (Mowzie's Mobs to AzureLib's `AzureNavigation`). This is the same family as the candidate hybrid.

### Cited Findings

**Vanilla Java Edition (the constraint)**
- The Java bounding box "moves with the entity but maintains a static orientation without rotating". It is used for block/entity collision and also for hits and interactions — [minecraft.wiki Hitbox](https://minecraft.wiki/w/Hitbox)
- The Ender Dragon is the vanilla compound-shape precedent: nine boxes, and players can damage it only through the eight small green part boxes. These "move as it flies but don't rotate" — [minecraft.wiki Hitbox](https://minecraft.wiki/w/Hitbox)
- Recent hitbox history: 1.20.2 (23w33a) changed mob melee reach to use an outward-extended bounding box. 1.20.5 has no hitbox entries. 26.1 snapshot 2 changed the rabbit and baby-chicken boxes to match new models. No version adds rotated or multi-box collision for mobs — [minecraft.wiki Hitbox](https://minecraft.wiki/w/Hitbox)
- In 1.20.5, `EntityDimensions` became a record with `width`, `height`, `eyeHeight`, `attachments` (`EntityAttachments`) and `fixed`, plus `scaled(...)`, `withAttachments(...)`, `withEyeHeight(...)` and `getBoxAt(...)`. It has only one `width` field and no orientation field. `EntityAttachmentType` lists PASSENGER, VEHICLE, NAME_TAG and WARDEN_CHEST, so attachments are anchor points, not shapes — [Yarn 1.20.5 EntityDimensions javadoc](https://maven.fabricmc.net/docs/yarn-1.20.5+build.1/net/minecraft/entity/EntityDimensions.html); [Yarn 1.20.5 EntityAttachmentType](https://maven.fabricmc.net/docs/yarn-1.20.5+build.1/net/minecraft/entity/EntityAttachmentType.html)
- A 1.21.1 modder reports that a 3 x 2.5 hitbox mob "spins, walks backwards, and moves diagonally", that vanilla Ravagers show similar trouble, and that "a BoundingBox of 3 is too big for the vanilla pathfinding to work well". The thread has no public fix — [Forge forum #159517 (Sept–Oct 2025)](https://forums.minecraftforge.net/topic/159517-bad-pathfinding-for-big-mobs-1211/)

**Bedrock Edition (official docs)**
- `minecraft:collision_box` has only `width` ("Width and Depth of the collision box") and `height`, with values up to 100000000. It has no rotation or offset field (doc updated 2026-08-25) — [Microsoft Learn: minecraft:collision_box](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/entityreference/examples/entitycomponents/minecraftcomponent_collision_box)
- `minecraft:custom_hit_test` holds a list of `hitboxes`, each with `width`, `height` and `pivot`, used "for melee and ranged hits against the entity". The page does not say that these boxes rotate or that they affect collision (doc updated 2026-04-21) — [Microsoft Learn: minecraft:custom_hit_test](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/entityreference/examples/entitycomponents/minecraftcomponent_custom_hit_test)

**Epic Fight (hit shapes, server-authoritative)**
- Repo `Antikythera-Studios/epicfight`, branch 1.21.1 (about 1,042 commits). Code is GPL-3.0, assets are All Rights Reserved. There are no plans to backport newer features — [GitHub repo](https://github.com/Antikythera-Studios/epicfight)
- The collider package has `Collider`, `OBBCollider`, `MultiOBBCollider`, `LineCollider`, `MultiLineCollider`, `MultiCollider` and `PlaneCollider` — [collider dir @1.21.1](https://github.com/Antikythera-Studios/epicfight/tree/1.21.1/src/main/java/yesman/epicfight/api/collider)
- `OBBCollider` starts axis-aligned in model space. `transform(OpenMatrix4f)` rotates its corners and normals by the joint matrix. `isCollide(Entity)` wraps the target's vanilla AABB as an OBB and runs SAT on 6 face axes only. The 9 edge-cross axes are commented out "for better performance", so the test is partial — [OBBCollider.java @1.21.1](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/collider/OBBCollider.java)
- `Collider.updateAndSelectCollideEntity` samples **one** pose at `elapsedTime` with `armature.getBoundTransformFor(attackAnimation.getPoseByTime(entitypatch, elapsedTime, 1.0F), joint)`. It ignores `prevElapsedTime`, so there is no swept sub-stepping. Broadphase is `level().getEntities(entity, getHitboxAABB(), ...)`, then `isCollide` per entity — [Collider.java @1.21.1](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/collider/Collider.java)
- `AttackAnimation.tick` calls `attackTick` → `hurtCollidingEntities` only when `!entitypatch.isLogicalClient()`, so hit detection is **server-side**. The client only draws debug colliders — [AttackAnimation.java @1.21.1](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/animation/types/AttackAnimation.java)
- The Epic Fight docs say a mob's scale affects "both its visual model and physical armature (hitbox, animations, etc)" — [Epic Fight docs, Entities guide](https://epicfight-docs.readthedocs.io/Guides/Entities/page2/)
- Movement collision in Epic Fight still uses the vanilla box. I found no source that says Epic Fight changes mob block collision (see Gaps).

**Better Combat (hit shapes, client-authoritative)**
- The README describes "accurate weapon swing calculation" based on OBB and SAT, for Fabric and Forge. A NeoForge folder exists, and a `26.2` branch is visible — [GitHub ZsoltMolnarrr/BetterCombat](https://github.com/ZsoltMolnarrr/BetterCombat)
- `TargetFinder` is in `net.bettercombat.client.collision`. It builds an AABB broadphase (`player.getBoundingBox().expand(range*mult+1)`), then `new OrientedBoundingBox(origin, size, pitch, yaw)` pushed forward by size.z/2, then tests `obb.intersects(entity.getBoundingBox().expand(margin))` or a centre-point containment, then radial angle and wall raycast filters — [TargetFinder.java @1.20.1](https://raw.githubusercontent.com/ZsoltMolnarrr/BetterCombat/1.20.1/common/src/main/java/net/bettercombat/client/collision/TargetFinder.java)
- The server does **not** recompute hits. It loops over client-supplied `request.entityIds()` and calls `player.attack(entity)`. The range check is disabled by `if (true || player.squaredDistanceTo(entity) < ...)` — [ServerNetwork.java @1.20.1](https://raw.githubusercontent.com/ZsoltMolnarrr/BetterCombat/1.20.1/common/src/main/java/net/bettercombat/network/ServerNetwork.java)

**Create (rotated contraption vs entity)**
- `ContraptionCollider.collideEntities` transforms the entity into contraption-local space (anchor subtract, inverse yaw, rotation matrix) and builds `new OrientedBB(localBB)` with `obb.setRotation(rotationMatrix)`. It intersects that box with block shapes (`getSimplifiedEntityColliders()` or a block scan), sums separation vectors with step height, transforms the response back, and runs a copy of vanilla `Entity#collide`. Local players are collided on the client and a `ClientMotionPacket` is sent. Server-side players are skipped. Clipping "rescue" code exists (`saveClientPlayerFromClipping`), as do tuning constants (`129/128f`, `1.0E-7D`) — [ContraptionCollider.java @mc1.20.1/dev](https://raw.githubusercontent.com/Creators-of-Create/Create/mc1.20.1/dev/src/main/java/com/simibubi/create/content/contraptions/ContraptionCollider.java)

**Valkyrien Skies 2 (rotated ships vs entity)**
- `adjustEntityMovementForShipCollisions` inflates the entity box (0.5 for players, 0.1 for others, with the note "increases the cost of doing collision"). It turns the swept box into a ship-space polygon via `worldToShipMatrix`, takes its enclosing AABB to query ship voxels, turns those back into world-space convex polygons, and calls `EntityPolygonCollider.adjustEntityMovementForPolygonCollisions` — [EntityShipCollisionUtils.kt @main](https://raw.githubusercontent.com/ValkyrienSkies/Valkyrien-Skies-2/main/common/src/main/kotlin/org/valkyrienskies/mod/common/util/EntityShipCollisionUtils.kt)
- Sable (the engine behind Create: Aeronautics) has add-ons that do their own server-side sweeps of structure bounds against entities. I found no official Sable doc on entity collision — [Aeronautics Entity Damage (CurseForge)](https://www.curseforge.com/minecraft/mc-mods/aeronautics-entity-damage)

**Hitbox libraries**
- **SentinelLib** (OmbreMoon): OBB hitboxes that "rotate with entity movement", NeoForge 1.21.1, GPL-3.0-only, "still very much in the beta stage", source at github.com/OmbreMoon/SentinelLibNeo. The page describes combat use only and says nothing about block collision — [Modrinth SentinelLib](https://modrinth.com/mod/sentinellib)
- **More Hitboxes** (DarkPred): multipart sub-hitboxes with optional attachment to GeckoLib bones. "Movements triggered by GeckoLib are not synched to the server". Attack boxes "are limited to the clientside and will only work on players". "The default bounding box is still used for collision". MIT license. Fabric/Forge/NeoForge 1.19.3 to 1.21.1. Newest 1.9.4-alpha (Mar 31, 2026) — [CurseForge More Hitboxes](https://www.curseforge.com/minecraft/mc-mods/more-hitboxes)
- **CustomHitboxLib**: any number of sub-hitboxes per entity, set up by JSON datapack or API, damage forwarded to the parent. Forge 1.20.1 and NeoForge 1.21.1. No rotation is stated — [CurseForge CustomHitboxLib](https://www.curseforge.com/minecraft/mc-mods/customhitboxlib)
- **DoxHitbox**: per-part OBBs for humanoids only. The client resolves hits and the server consumes the result. The listing says "all projects were produced using AI" — [CurseForge DoxHitbox](https://www.curseforge.com/minecraft/mc-mods/doxhitbox)

**Big-mob navigation prior art**
- AzureLib's `AzureNavigation` extends `GroundPathNavigation` and swaps in `AzurePathFinder`, which "considers not only the center position of a mob but also its full bounding box". `followThePath`/`tryShortcut` check the full collision box before advancing, and `sweep` runs box collision tests along each step to stop entry into gaps smaller than the box. It credits Bob Mowzie and pau101 (`MowziesMobs/.../server/ai/MMPathNavigateGround.java`). No license or version is stated on the page. The edit link points to branch `1.21.1` of AzureDoom/AzureLib — [AzureNavigation 101 (moddedmc wiki)](https://moddedmc.wiki/en/project/azurelib/latest/docs/misc/azurenavigation_101)

**Pehkui**
- Pehkui has separate `hitbox_width`/`hitbox_height` scale types, apart from model scale. I found no source that ties Pehkui to a pathfinding fix. A third-party guide says scale also changes step height and reach — [CurseForge Pehkui](https://www.curseforge.com/minecraft/mc-mods/pehkui)

### Inferences
- The "rotate the world, not the entity" pattern (Create, VS2) works because the moving thing is a rigid block grid. The entity's own AABB stays vanilla. To apply it to a dinosaur you would rotate *terrain* into dinosaur-local space every tick, which is the same as writing a custom collision solver for that mob. Create's clipping rescue code and tuning constants, and its client-side-only player handling, show that such solvers need ongoing patching. Neither mod changes pathfinding.
- An oriented collision shape for the mob itself would break, at least: `PathNavigation`/`NodeEvaluator` sizing (built on one width), `Entity.collide`/step-up, entity pushing, suffocation/in-wall checks, spawn placement and the client/server movement sync. Every system above avoids this, which is strong negative evidence. (Inference from the absence of any example, not a cited test.)
- The only proven fix for big-mob movement is navigation-side box sweeping (Mowzie → AzureLib). That backs the candidate hybrid's torso box plus path re-centring, turn-in-place and corner clearance checks. The AzureLib/Mowzie code is the most direct prior art to study (check its license first).
- Bedrock's split of `collision_box` (one unrotated width) from `custom_hit_test` (several hit boxes) is the official design pattern. It matches the hybrid's split of "vanilla box for movement, section parts for hits".
- Better Combat shows the cost of client-authoritative hits: the server trusts client entity IDs, with the range check disabled. For dinosaur section hits, a server-side procedural spine avoids this trust problem.

### Gaps
- I could not open `MMPathNavigateGround.java` (GitHub 404 on `master`; the repo may be renamed, private or on another branch) or the AzureLib `AzureNavigation.java` source, so their algorithms are described only from AzureLib's doc page. Their licenses are unconfirmed.
- I did not open the body of `EntityPolygonCollider` (VS2) or `ContinuousOBBCollider` (Create), so their exact SAT/response math is unconfirmed.
- I found no source that says whether Bedrock `custom_hit_test` boxes rotate with body yaw.
- I found no Epic Fight source on mob *movement* collision. I assume it is vanilla, but this is not verified.
- I found no Mojang changelog that says anything about entity collision shape in 1.21.x or 26.x beyond the per-mob size tweaks listed above.
- Commit SHAs were not captured. All links are branch heads as of 2026-10-09.

## Q2. Is server-side posing affordable today (Epic Fight's cost, mob counts)? Would 20 herd animals plus predators be affordable?

### Takeaway
Epic Fight shows that server-side armature posing works in production. It is cheap because it is **lazy**: the server advances only animation time every tick and evaluates a joint pose only when an attack phase needs a collider transform. GeckoLib does not do this. Its animation is evaluated in the render path on the client, and GeckoLib 5 ties it even more tightly to per-frame render states. Server truth of the GeckoLib pose would need a separate server-side evaluator. The compute cost would be small, but the engineering and the risk of desync would be large.

### Cited Findings
- Epic Fight `ServerAnimator.tick()` only ticks the player clock, handles clip end/queue and calls the animation's own `tick`. `getPose(partialTicks)` delegates to `AnimationPlayer.getCurrentPose`. On a clip switch it computes one pose (`getPoseByTime(entitypatch, 0.0F, 0.0F)`) for blending — [ServerAnimator.java @1.21.1](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/animation/ServerAnimator.java)
- `AnimationPlayer.tick()` only advances `elapsedTime += A_TICK * playbackSpeed`. A pose is computed only when someone asks for one, and nothing is cached — [AnimationPlayer.java @1.21.1](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/animation/AnimationPlayer.java)
- Server-side pose evaluation happens in `Collider.updateAndSelectCollideEntity` (one pose per collider per tick during attack phases) — [Collider.java @1.21.1](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/collider/Collider.java); gated to the logical server in [AttackAnimation.java @1.21.1](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/animation/types/AttackAnimation.java)
- The community "Epic Fight FPS Optimizer" limits distant pose calculation (20 FPS beyond 12 blocks, vanilla render beyond 32). It is **client-side**, which suggests the felt cost of posing is on the render side, not the server — [CurseForge Epic Fight FPS Optimizer](https://www.curseforge.com/minecraft/mc-mods/epic-fight-fps-optimizer)
- GeckoLib 5 docs cover Minecraft 1.21.5 to 1.21.11 and 26.1 to 26.3. GeckoLib 4 docs cover 1.19.3 to 1.21.4 — [GeckoLib wiki](https://wiki.geckolib.com/)
- GeckoLib 5: "the rendered object is not accessible at the time of rendering". A `GeoRenderState` is extracted per object per render pass, the animatable is "forgotten immediately after this step", and rendering, "including animation handling and processing", uses the RenderState — [GeckoLib 5: Conceptual changes](https://wiki.geckolib.com/docs/geckolib5/updating/important/conceptual-changes); [RenderStates](https://wiki.geckolib.com/docs/geckolib5/updating/important/renderstates)
- GeckoLib 5 Bone Snapshots are made "when the RenderPassInfo renders a model for the first time in a render pass", and "at the end of the render pass, all BoneSnapshots are discarded entirely". `GeoBone`s are immutable — [Bone Snapshots (concepts)](https://wiki.geckolib.com/docs/geckolib5/concepts/geobones/bone-snapshots); [Bone Snapshots (updating)](https://wiki.geckolib.com/docs/geckolib5/updating/important/bone-snapshots)
- GeckoLib 4 lets the server call `triggerAnim` at any time. The wiki does not say that the server evaluates the animation — [GeckoLib4 Triggerable Animations](https://github.com/bernie-g/geckolib/wiki/Triggerable-Animations-(Geckolib4))
- Third-party confirmation: "Movements triggered by GeckoLib are not synched to the server" — [More Hitboxes](https://www.curseforge.com/minecraft/mc-mods/more-hitboxes)

### Inferences
- **Compute cost is not the barrier.** Rough estimate: 25 mobs x ~40 bones x 20 ticks/s = 20,000 bone evaluations/s. Each one is a keyframe lerp/slerp plus a 4x4 multiply, roughly 100–200 flops, so about 2–4 M flops/s. That is far below 1 ms of a 50 ms tick. Epic Fight's lazy pattern (pose only the joints that need it, only during attack phases) cuts this further. This is arithmetic, not a measured benchmark.
- **The real costs are structural.** (1) GeckoLib models and animations ship as client assets, so a dedicated server would need its own load path for bone hierarchy and keyframes. (2) Every controller transition, Molang query and procedural bone tweak (head look, tail sway) would need a deterministic server copy, or server and client poses drift. (3) On 1.21.4+/26.x with GeckoLib 5, bone snapshots exist only inside a client render pass and are discarded at its end. A "capture bones on the client and send them to the server" design is therefore client-authoritative (the Better Combat trust problem) and render-rate dependent. Do not pursue it for gameplay truth.
- **Is server pose truth needed?** For Nublar's goals (herd spacing, body clearance, which section was hit), a procedural spine that follows body yaw, head yaw and gait phase is enough server truth. It differs from the rendered pose only by animation detail (tail swish, neck bob). Epic Fight needs exact poses because weapon swing timing *is* the gameplay. Dinosaur bites could copy Epic Fight's narrow pattern instead: one server-side collider on a "jaw" anchor that the spine computes during the attack window, with no full skeleton.
- 20 herd animals plus predators would be affordable even with a full server skeleton in pure compute terms. The maintenance cost across GeckoLib 4 → 5 and two loaders is the deciding factor, and it weighs against it.

### Gaps
- I found no published tick-time benchmark (spark profile) for Epic Fight server-side mobs.
- `DynamicAnimation.getPoseByTime` (per-joint interpolation cost) was not opened.
- I did not check whether AzureLib exposes server-side bone data (search turned up nothing).
- The GeckoLib 5 docs pages do not say outright "animations never run on the server". The client-only conclusion rests on the render-pass lifecycle text plus the third-party More Hitboxes statement.

## Q3. What would an oriented-capsule herd-spacing or hit test cost compared with AABB tests?

### Takeaway
An AABB-AABB overlap is 6 comparisons. An OBB-OBB SAT test is about 100 operations on average (Gottschalk). A capsule-capsule test (closest distance between two segments, compared with the sum of radii) is in the same tens-of-flops range. For a herd of 20 to 30 animals, any of these costs microseconds per tick, provided an AABB broadphase is used first. All mature systems above do this.

### Cited Findings
- Two OBBs can be tested for overlap "in about 100 operations on average" with the separating-axis theorem — [Gottschalk, Lin, Manocha 1996 (OBBTree)](https://www.cs.cornell.edu/courses/cs667/2005sp/readings/gottschalk96.pdf)
- The full OBB SAT test has 15 candidate axes (3 + 3 face normals, 9 edge cross products), with an early exit on the first separating axis — [DEV: AABB vs OBB / SAT](https://dev.to/pratyush_mohanty_6b8f2749/the-math-behind-bounding-box-collision-detection-aabb-vs-obbseparate-axis-theorem-1gdn); some implementations exit after 6 axes or use 5 selected axes — [OBB intersect test based on effective constraint (PoS)](https://pos.sissa.it/264/010/pdf)
- Epic Fight drops the 9 edge-cross axes "for better performance" (a 6-axis partial SAT) — [OBBCollider.java @1.21.1](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/collider/OBBCollider.java)
- Better Combat and Epic Fight both run a vanilla AABB `getEntities` broadphase before the oriented narrow phase — [TargetFinder.java](https://raw.githubusercontent.com/ZsoltMolnarrr/BetterCombat/1.20.1/common/src/main/java/net/bettercombat/client/collision/TargetFinder.java); [Collider.java](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/collider/Collider.java)

### Inferences
- Pairwise herd spacing for N=25 gives 300 pairs/tick. At about 100 ops each that is 30,000 ops/tick (well under 0.1 ms). A uniform grid or the existing `getEntities` box query keeps it near-linear. Capsules (or 2–4-segment spine capsule chains) are better than OBBs for herd spacing. They are rotation-invariant and smooth (no edge cases at corners), they give a separation direction directly from the closest-point pair, and a long body maps naturally to "segment + radius".
- Section hit parts: if each section part is a small vanilla `PartEntity`/AABB placed along the spine (Ender Dragon style), vanilla projectile and melee code already works with no custom maths. That costs the extra entities' box updates, not SAT. An OBB/capsule hit test would need a custom raycast against the spine. That is cheap, but it bypasses vanilla's projectile/attack pipeline and needs mixins or events on both loaders.
- The one place where cost could matter is a capsule or OBB *terrain* clearance check, such as a body-length sweep against blocks at sharp path corners. Do it as a few sampled AABB `noCollision` queries along the spine (as AzureLib's `sweep` does with the full box), not as an exact OBB-vs-voxel test. Run it only at path corners or on repath, not every tick.

### Gaps
- No Minecraft-specific benchmark of capsule vs AABB entity tests was found. The figures above are general collision-detection literature plus arithmetic.
- No Minecraft library offering capsule maths was found (searches for OBB libraries found only SentinelLib, DoxHitbox, CustomHitboxLib and More Hitboxes).

## Q4. Is anything clearly better than the candidate hybrid?

### Takeaway
No. Nothing found is clearly better. The evidence points to the same architecture: a vanilla unrotated box for movement, smarter navigation that sweeps that box, and a separate hit layer (Bedrock `custom_hit_test`, Ender Dragon parts, Epic Fight colliders). Two refinements from prior art are worth adopting: (1) port or study Mowzie's/AzureLib's box-aware navigation instead of writing node clearance from scratch; (2) copy Epic Fight's *lazy, server-side* collider evaluation for bite/tail-swipe attacks, driven by the procedural spine instead of a full skeleton.

### Cited Findings
- Bedrock officially separates a single-width `collision_box` from a multi-box `custom_hit_test` — [collision_box](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/entityreference/examples/entitycomponents/minecraftcomponent_collision_box); [custom_hit_test](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/entityreference/examples/entitycomponents/minecraftcomponent_custom_hit_test)
- Box-aware ground navigation for wide mobs exists as reusable code (AzureLib `AzureNavigation`/`AzurePathFinder`, derived from Mowzie's Mobs) — [AzureNavigation 101](https://moddedmc.wiki/en/project/azurelib/latest/docs/misc/azurenavigation_101)
- Bone-attached hitbox libraries for GeckoLib are client-only and keep vanilla collision — [More Hitboxes](https://www.curseforge.com/minecraft/mc-mods/more-hitboxes)
- The only server-authoritative oriented hit system found (Epic Fight) evaluates poses on demand, not every tick — [ServerAnimator.java](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/animation/ServerAnimator.java); [Collider.java](https://raw.githubusercontent.com/Antikythera-Studios/epicfight/1.21.1/src/main/java/yesman/epicfight/api/collider/Collider.java)

### Inferences
- **Reuse ranking for Project Nublar** (port target: 26.2, NeoForge plus Fabric):
  - *AzureLib/Mowzie navigation*: copy the approach (whole-box sweep, shortcutting). Check the license before you copy code. Mowzie's Mobs and AzureLib licences were not confirmed in this pass.
  - *Epic Fight*: copy the approach only. GPL-3.0 code and an All-Rights-Reserved asset split, Forge/NeoForge only, its own armature format. It cannot be used as a library for a GeckoLib mob.
  - *Better Combat OBB class*: the maths is generic and could be studied. Its client-authoritative design should not be copied for mob hits.
  - *Create/VS2*: the approach (local-space transform plus vanilla-style `collide` copy) suits rigid block structures, not mobs. Do not adopt it for dinosaur bodies.
  - *SentinelLib*: NeoForge 1.21.1 only, beta, GPL-3.0-only, no Fabric. Not suitable as a dependency for a two-loader 26.2 port.
  - *More Hitboxes*: MIT, three loaders, but client-side attack boxes and a 1.21.1 ceiling. Its multipart registration pattern could be studied.
- **Candidate hybrid assessment:** the torso-sized vanilla box is the right call. A long or square box larger than the torso is what makes vanilla pathing fail (see the Forge forum report). Turn-in-place plus corner clearance sweeps cover the "long body in a corridor" case that a square box cannot express. The spine/capsule for herd spacing is cheap (Q3). Section `PartEntity`-style AABB chains placed along the spine give vanilla-compatible hits with server truth and no GeckoLib dependency. This also survives the GeckoLib 5 render-state change, because nothing reads bones at runtime.
- **One risk to flag:** with a torso-only box, the head and tail are not in vanilla collision, so they can clip into walls visually and do not block other entities. That is a deliberate trade-off (Bedrock and the Ender Dragon accept the same). Pair it with the spine clearance check at corners and the herd spacing capsule, so the visible clipping stays small.
- An alternative to consider: **width-only scaling of the vanilla box** (keep the vanilla box at roughly torso width, and let only the spine/parts grow with age). This avoids the growing-hitbox pathing failures the Forge thread reports. It is a design inference, not proven prior art.

### Gaps
- I found no mod that has shipped a long-bodied animated mob with *both* server-truth section hitboxes *and* box-aware navigation, so the full hybrid has no direct precedent to cite.
- Licences of Mowzie's Mobs and AzureLib (needed before any code copy) were not confirmed.
- I did not check whether NeoForge 26.x or Fabric 26.x provide a cross-loader multipart/`PartEntity` API (NeoForge has `PartEntity`; the Fabric equivalent was not checked).
