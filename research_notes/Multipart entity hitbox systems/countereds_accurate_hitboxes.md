# Countered's Accurate Hitboxes (template repo, 1.21.1)

Scope: the public GitHub repo `Coun7ered/countereds-accurate-hitboxes-template-1.21.1` (branch `master`, HEAD `711b0b5`, 2026-09-01), read from a shallow clone. Read only, nothing built or run. Line numbers refer to that commit.

Base URL for code links: `https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/` (shortened below to `GH/`).

Package root: `src/main/java/net/countered/counteredsaccuratehitboxes/` (shortened below to `pkg/`). The whole mod is 12 Java files and 744 lines.

The single most important finding: **the repo is mod version 1.0.1 (Fabric, 1.21.1). The mod that is published today (2.0.x, Fabric + NeoForge, 1.21.1 to 26.3, MIT on Modrinth) has no linked public source.** Everything below about mechanics describes the 1.0.1 code in the repo, not the current 2.x releases.

## Q1. What is the hitbox model?

### Takeaway
It is not a PartEntity / EnderDragonPart system and it does not use OBB-vs-OBB maths. Each frame, the **client renderer** turns every vanilla `ModelPart.Cuboid` of a living entity into 8 world-space corner points. It stores these as a list on the entity with a Fabric data attachment. A mixin then **replaces `ProjectileUtil.raycast`** with a ray-vs-triangle test: each box is split into 12 triangles and tested with Möller–Trumbore. Because the 8 corners come from the full model matrix, each box is effectively an oriented box (any rotation, scale and translation of the bone), but it is stored as a list of points, not as a centre, half-extents and rotation.

### Cited Findings
- README: the mod "adds precise per-cuboid hitbox detection to entities using transformed `ModelPart` data", using "raycasting and rotated bounding boxes (OBBs)". Features: per-part detection and a debug view with particles or render layers — [README.md L1-L9](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/README.md)
- Storage: `HitboxAttachment.HITBOXES` is an `AttachmentType<List<List<Vector3f>>>`, created with `AttachmentRegistry.createDefaulted(id, ArrayList::new)`. It has no persistence codec and no sync configuration — [GH/src/main/java/.../util/HitboxAttachment.java L18-L21](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/util/HitboxAttachment.java)
- Box generation (client): `HitboxFeatureRenderer.getCuboidHitbox` takes the cuboid min/max (divided by 16), builds the 8 corners and transforms each with `matrices.peek().getPositionMatrix()` — [pkg/client/HitboxFeatureRenderer.java L123-L148](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/client/HitboxFeatureRenderer.java#L123-L148)
- The renderer adds the camera position to each corner, which turns camera-relative render coordinates into world coordinates (L85-L90). It walks child parts recursively (L93-L95) and then calls `entity.setAttached(HITBOXES, vertexCol)` (L49) — [HitboxFeatureRenderer.java L39-L99](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/client/HitboxFeatureRenderer.java#L39-L99)
- Hit test: `ModProjectileUtilMixin` uses `@Inject(method="raycast", at=@At("HEAD"), cancellable=true)` on `ProjectileUtil` and always cancels vanilla. For each candidate entity it reads the attachment. If the attachment is empty, it falls back to `getBoundingBox().expand(getTargetingMargin())` and `Box.raycast`, which copies the vanilla logic (L41-L69). Otherwise, for each 8-point list it sorts the corners around the centroid (L154-L173), builds 12 triangles (L174-L201), runs Möller–Trumbore with backface culling off and `EPSILON = 0.01` (L203-L253), and keeps the nearest hit by squared distance (L84-L97). It returns `new EntityHitResult(closestEntity, hitPos)` (L100-L105) — [pkg/mixin/server/ModProjectileUtilMixin.java](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/mixin/server/ModProjectileUtilMixin.java)
- 4-point quads are inflated to an 8-point slab 0.01 thick before testing (`inflateQuadToBox`, L72-L74, L108-L151) — same file.
- `Triangle` is a plain record of three `Vector3f` — [pkg/util/Triangle.java](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/util/Triangle.java)
- Mixin config: `server.ModProjectileUtilMixin` is in the common `mixins` list. The renderer mixin and the accessors are in the `client` list — [src/main/resources/countereds-accurate-hitboxes.mixins.json L5-L17](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/resources/countereds-accurate-hitboxes.mixins.json)
- No public API: there are no interfaces, events, registration methods or data formats for other mods. The mod is applied to every `LivingEntityRenderer` by a constructor-TAIL mixin that adds `HitboxFeatureRenderer` as a feature layer — [pkg/mixin/client/ModLivingEntityRenderer.java L20-L24](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/mixin/client/ModLivingEntityRenderer.java)

### Inferences
- In 1.0.1, a hit result tells you only which entity was hit and the hit point. It does not say which part was hit: the part identity is lost because the attachment is a flat `List<List<Vector3f>>` with no names. Per-part damage (for example head vs. tail) would need a new data shape, such as named boxes.
- The 4-vertex quad path looks unused in 1.21.1: `getCuboidHitbox` always returns 8 points. It is probably left over from an earlier version.
- The "template" in the repo name does not mean a reusable library or a GitHub template repo: `is_template` is false (GitHub API). It is an ordinary small mod.

### Gaps
- The 2.x mechanics (Modrinth says "Animated limbs no longer desync with their hitboxes" and "Dedicated servers no longer crash") cannot be checked, because no public 2.x source was found (see Q4).

## Q2. Are boxes tied to animation bones, and how does the server know the bone transforms?

### Takeaway
The boxes are tied to **vanilla `ModelPart` cuboids**, not to GeckoLib bones. They are captured on the **client during rendering**, after the model has been posed for that frame, so they follow vanilla keyframe and procedural animation, interpolated by tickDelta. **The server never knows the transforms.** The data is not synced and there is no server-side animation evaluation and no baked data. On the server the attachment stays empty, so server-side calls to `ProjectileUtil.raycast` fall back to the vanilla AABB.

### Cited Findings
- The feature renderer runs inside `LivingEntityRenderer` and finds model roots by reflection. It walks `getDeclaredFields()` of the model class and its superclasses, collects `ModelPart` fields, skips fields named `cloak` and `ear`, and keeps only parts that are not a child of another part. `SinglePartEntityModel.getPart()` is a shortcut — [pkg/client/ModStuckObjectsFeatureRenderer.java L27-L79](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/client/ModStuckObjectsFeatureRenderer.java)
- For each visible part, the code calls `modelPart.rotate(matrices)` (the pivot, rotation and scale that the animation set this frame) and then reads its cuboids through accessor mixins on `ModelPart.cuboids` / `children` — [HitboxFeatureRenderer.java L78-L96](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/client/HitboxFeatureRenderer.java#L78-L96); [pkg/mixin/accessors/ModelPartAccessor.java](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/mixin/accessors/ModelPartAccessor.java)
- Babies have hard-coded handling: hitboxes are generated only for zombie, zombie villager, piglin, zombified piglin, cow, pig and sheep babies (L42, L52-L61). Head offsets come from `AnimalModel.childHeadYOffset/ZOffset` through an accessor (L101-L121). The rabbit has a hard-coded 0.6 scale (L74-L77) — [HitboxFeatureRenderer.java](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/client/HitboxFeatureRenderer.java#L42-L121); [pkg/mixin/accessors/AnimalModelAccessor.java](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/mixin/accessors/AnimalModelAccessor.java)
- The attachment is registered with `createDefaulted` only, with no sync and no persistence (HitboxAttachment.java L18-L21, cited above). No network packets exist anywhere in the repo: the file list has no networking classes.
- An earlier README (removed in commit `711b0b5`) said a 1.20.1 backport was hard because, before 1.21, matrix-stack transforms "seem to include unexpected rotations (possibly related to camera-space)", which breaks world-space vertex reconstruction — [commit 711b0b5 diff](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/commit/711b0b561b5d7f0dd703a9a430bdb0c112e919a9)

### Inferences
- **GeckoLib entities are not covered by 1.0.1.** GeckoLib's `GeoEntityRenderer` does not extend `LivingEntityRenderer` and does not use `ModelPart`, so the feature layer is never added and those entities keep the AABB fallback. This matters for Project Nublar, whose dinosaurs are GeckoLib-rendered. (Inference from GeckoLib's known class hierarchy; not tested.)
- Feature layers draw only from the base `EntityModel` parts. Armor, saddles, held items and other feature-layer geometry are not part of the hitbox.
- Hitboxes exist only for entities that the client has rendered. An off-screen (frustum-culled) entity keeps its **last stored vertices**, which can be stale world positions. An entity that has never been rendered has an empty list and uses the AABB. Baby entities outside the allow-list return early without clearing the attachment.
- The baby and rabbit scale and translate steps run inside the recursive `createHitboxes`, after `matrices.push()`, for every part at every depth. So nested child parts appear to get the scale applied more than once (for example 0.5 × 0.5). This may explain why babies are limited to an allow-list. (Code-reading inference; not tested.)
- Any other place that runs living-entity feature renderers outside the world pass, for example the entity preview in the inventory or horse screens, overwrites the attachment with GUI-space coordinates plus the camera position until the next world frame. (Inference from the render hook choice.)
- The server-side copy of the mixin always cancels vanilla `raycast` and runs its own AABB fallback. The fallback copies the vanilla logic but is not byte-identical: its root-vehicle and contains-origin branches differ slightly (L48-L67). Treat it as "almost vanilla" on the server.

### Gaps
- Exact vanilla 1.21.1 callers of `ProjectileUtil.raycast(Entity, Vec3d, Vec3d, Box, Predicate, double)` were not checked against decompiled sources (by policy: public sources only). The best understanding is that its main caller is client crosshair picking (`GameRenderer.findCrosshairTarget`), and that server projectile hits use `ProjectileUtil.getEntityCollision`, which this mod does not touch.

## Q3. Do the hitboxes affect block collision, movement, pathfinding, pushing or projectiles?

### Takeaway
No. In 1.0.1 the boxes affect only **one raycast function**, and in practice only on the client: crosshair and entity picking for melee and use. Block collision, entity pushing, movement, pathfinding, `EntityDimensions` and the server-side AABB all stay vanilla. Server-side projectile hits are also unchanged, because the projectile entity-collision path is not mixed in.

### Cited Findings
- The only gameplay mixin is `@Mixin(ProjectileUtil.class)` on `raycast`. No other mixin targets `Entity`, `LivingEntity`, collision, navigation or `ProjectileEntity` — [mixins.json](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/resources/countereds-accurate-hitboxes.mixins.json); [ModProjectileUtilMixin.java L25-L31](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/mixin/server/ModProjectileUtilMixin.java#L25-L31)
- The candidate search still uses `world.getOtherEntities(entity, box, predicate)`, which is the vanilla AABB broad phase (L41) — same file.
- The competing mod "Accurate Hitboxes" (VortexCombat, 1.20.x, All Rights Reserved) states that attacks **and projectiles** must hit the model geometry, that it has "dedicated-server synchronization for model-derived hitboxes" and GeckoLib 4 support, and that the Ender Dragon is excluded because it uses its own multipart system. This shows the scope that 1.0.1 lacks — [Modrinth: accuratehitboxes](https://modrinth.com/mod/accuratehitboxes)

### Inferences
- Effect on gameplay: on a server, the accurate boxes can only **reduce** client hits. A ray that crosses the vanilla AABB but misses every cuboid picks nothing, or picks the entity behind. Boxes that stick out past the AABB, such as long necks or tails, are found only if the entity's AABB overlaps the ray's swept search box. The server then checks the attack packet against the vanilla AABB and its reach rules. It does not re-check the model, so a modified client could still hit anywhere in the AABB.
- Performance in 1.0.1:
  - (a) Every frame, for every rendered living entity, the code reflects over the model class fields (`getDeclaredFields` + `setAccessible`), allocates 8 `Vector3f` and a list per cuboid, and reads the camera once per cuboid.
  - (b) Each raycast, for each candidate entity, does per cuboid: a stream sort of 8 points, 12 `Triangle` objects, and Möller–Trumbore in float precision. There is no per-part broad phase (no AABB pre-check per cuboid).
  - Raycasts are rare (once per frame for the crosshair), so (a) is the larger cost. For a high-cuboid model (a dinosaur with 50 to 100+ cubes) this is still small but allocation-heavy. Float maths with world-space coordinates can lose precision far from the origin, because the vertices are absolute world floats.

### Gaps
- No benchmarks or profiling data were found in the repo, README or issues.

## Q4. License, supported versions, loaders, integration, maturity and known issues

### Takeaway
The GitHub repo is **CC0-1.0**: Fabric only, MC 1.21.1, Fabric API required, mod 1.0.1, 19 commits from 2025-03-28 to 2025-04-23, plus one README edit on 2026-09-01. It has 5 stars, 1 fork and 1 open issue. The published mod has moved on: **2.0.x for Fabric and NeoForge on 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3**, which needs Architectury API, is marked as needed on both client and server, and is **MIT on Modrinth with no source URL**. "Integration" means installing the mod. There is no API, so to use the idea in another mod you copy or adapt the code, which CC0 allows.

### Cited Findings
- Build: Fabric Loom 1.10-SNAPSHOT, Yarn 1.21.1+build.3, loader 0.16.10, Fabric API 0.115.3+1.21.1, Java 21, mod_version 1.0.1 — [build.gradle](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/build.gradle); [gradle.properties](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/gradle.properties)
- `fabric.mod.json`: license `CC0-1.0`, `environment: "*"`, depends on minecraft `~1.21.1` and `fabric-api`, single `main` entrypoint. The contact links are still the fabric-example-mod defaults — [fabric.mod.json](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/resources/fabric.mod.json)
- Repo LICENSE is the CC0 1.0 Universal text. GitHub reports CC0-1.0, created 2025-03-28, 5 stars, 1 fork (`spatulox-minecraft/countereds-accurate-hitboxes`, last push 2025-05-27), 1 open issue — [GitHub repo](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1) (GitHub REST API, queried 2026-10-09)
- Commit history: "convert all parts to hitboxes" (2025-04-05), "change getModel logic to reflection, allowing for retrieving all animal models" (2025-04-16), "fix hitbox flickering" (2025-04-14, 2025-04-19), "cleanup" (2025-04-22) — [commits](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/commits/master)
- The main entrypoint has an unused debug method that registers a client `KeyBinding` from the common initializer (commented out at L29) — [pkg/CounteredsAccurateHitboxes.java L23-L68](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/src/main/java/net/countered/counteredsaccuratehitboxes/CounteredsAccurateHitboxes.java)
- Issue #1 "1.21.11 compatibility" (opened 2026-01-19) is still open. One commenter called this mod the only option of its kind, and another pointed to the VortexCombat "Accurate Hitboxes" mod on CurseForge — [issue #1](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/issues/1)
- Modrinth project `countereds-accurate-hitboxes`:
  - Published 2025-04-14 and updated 2026-10-08. About 15.1k downloads and 105 followers.
  - License is listed as **MIT** and `source_url` is empty. Loaders: fabric and neoforge. Client-side and server-side are both "required".
  - Description: the mod uses "the actual 3D models of entities" and "even works for modded entities". There is a keybind to toggle the hitbox view.
  - Version history: 0.1.0 (2025-04-15), 1.0.0 ("Now works for ALL entities even those from other mods"), 1.0.1 Fabric (2025-04-19, "fix flickering"), and 1.0.1 NeoForge (2025-05-27, a "Sinytra release" that needs Sinytra Connector and Forgified Fabric API).
  - Then 2.0.0 (2026-09-03 to 09-28, for 26.1.2, 26.2 and 26.3), 2.0.1 (2026-10-08, "Dedicated servers no longer crash", which adds 1.21.1) and 2.0.2 (2026-10-08, "Animated limbs no longer desync with their hitboxes", for 1.21.11 and 26.3).
  - All 2.x versions need Architectury API (`lhGA9TYQ`), and the jar names change to `accurate_hitboxes-{fabric|neoforge}-2.0.x.jar`.
  - [Modrinth project](https://modrinth.com/mod/countereds-accurate-hitboxes) (Modrinth API v2 `/project` and `/project/.../version`, queried 2026-10-09)
- Issues for the published mod go to CurseForge (`legacy.curseforge.com/.../countereds-accurate-hitboxes/issues`), as the Modrinth `issues_url` shows — [Modrinth project](https://modrinth.com/mod/countereds-accurate-hitboxes)
- License conflict: the GitHub repo and `fabric.mod.json` say CC0-1.0, but Modrinth lists MIT — [LICENSE](https://github.com/Coun7ered/countereds-accurate-hitboxes-template-1.21.1/blob/711b0b561b5d7f0dd703a9a430bdb0c112e919a9/LICENSE); contradicted by [Modrinth project](https://modrinth.com/mod/countereds-accurate-hitboxes)

### Inferences
- The 1.0.1 repo code is CC0, so it can be reused freely. The 2.x code is not public, so only its behaviour (from changelogs) can be described.
- 2.x is now server-required and fixes a dedicated-server crash. This suggests 2.x moved some logic to the server, or at least loads common code there. Whether it syncs boxes from client to server, evaluates animations on the server, or does something else cannot be told from public sources.
- As a pattern for Project Nublar (Forge 1.20.1, GeckoLib dinosaurs), 1.0.1 shows the "virtual boxes from the client render pose, tested by a raycast mixin" approach. Its main limits are: client-only authority, vanilla `ModelPart` only (no GeckoLib), no part names, no projectile, collision or pathfinding effect, and an author who reports trouble with world-space reconstruction before 1.21.
- Maturity: it is a small, single-author hobby mod. Code activity in the repo stopped in April 2025, and the author's newer work is published without linked source. It is not a library and has no API stability.

### Gaps
- The CurseForge page and Discord were not read. CurseForge issue-tracker contents (known bugs for 2.x) are unknown.
- No public source for 2.x was found among Coun7ered's GitHub repos (the account lists only this repo plus unrelated terrain, road and camera mods). Its design is unknown.
