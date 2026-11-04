# GeckoLib 5 hooks for the F2.3 code layers

Research note for the animation resources report. Written 2026-11-03.

Scope: item 1 of the handoff. The parameter order of `BonePositionListener#accept`, and the other GeckoLib 5 hooks that the code layers use (`addBoneUpdater`, `adjustModelBonesForRender`, additive controllers).

Labels:

- `[v]`: checked in the GeckoLib bytecode with `javap -c -p -l` (JDK 26.0.1). The jars are `geckolib-neoforge-26.2-5.5.6.jar` and `geckolib-fabric-26.2-5.5.5.jar` in the Gradle cache. The Gradle cache has no GeckoLib 5 source jar, so the local variable table gives the names. Line numbers are the `LineNumberTable` lines of `RenderUtil.java` and `AnimationProcessor.java`.
- `[doc]`: from the GeckoLib wiki or the earlier survey note.
- `[inf]`: an inference.

The earlier survey note `research_notes/Reference mod survey for Beta/client_rendering_and_animation.md` (KQ3) already covers the render pass order. This note adds only new facts.

## Answer: the world position is the first parameter

`BonePositionListener#accept(Vec3, Vec3, Vec3)` gets `(worldPos, modelPos, localPos)`, in that order. [v]

`RenderUtil.providePositionsToListeners(PoseStack, RenderPassInfo, BonePositionListener[])` (source lines 137 to 149) does this:

1. It copies the current pose matrix (`bonePose`).
2. `localPose` is `bonePose` with the pre-render matrix taken out (`extractPoseFromRoot`: the inverse of the pre-render matrix, times the bone pose). This removes the camera transform.
3. `modelPose` is `bonePose` with the model render matrix taken out.
4. It reads `DataTickets.POSITION` from the render state. When it is present, `worldPose` is `localPose` with that position added to the translation column (`addPosToMatrix`). When it is absent, `worldPose` is null.
5. `localPos` is the origin of `localPose`, scaled by (1, 1, 1).
6. `modelPos` is the origin of `modelPose`, scaled by (-16, 16, 16), so it is in model pixels with x flipped.
7. `worldPos` is the origin of `worldPose`, scaled by (1, 1, 1), or null.
8. It calls `accept(worldPos, modelPos, localPos)` on every listener of the bone.

The local variable table names the three values `worldPos`, `modelPos` and `localPos`, and the call passes them in that order. [v]

The Fabric 5.5.5 jar has the same method, the same variable names and the same call order. [v]

### Facts that change the D339 design

- **World position is absolute.** It is the entity position plus the bone offset from the entity. It is not relative to the camera. [v] (steps 2 and 4)
- **World position can be null.** It is null when the render state has no `DataTickets.POSITION`. `GeoEntityRenderer`, `GeoReplacedEntityRenderer`, `GeoBlockRenderer` and `GeoArmorRenderer` set it, so for a dinosaur it is set. [v] A listener must still check for null. [inf]
- **The point is the bone pivot, not the cube tip.** GeckoLib calls the listeners in `prepMatrixForBoneAndUpdateListeners` after it translates to the pivot, rotates and scales, and before it translates back. The point is the origin of that matrix, which is the pivot. [v] So a "snout" effect starts at the snout bone's pivot. For a jaw bone, the pivot is the hinge, not the tip. [inf] Pick the bone whose pivot sits nearest the tip, or use a locator.
- **No model has a locator.** All six `.geo.json` files have 0 locators. [v] (Python count over `bones[].locators`.) `addLocatorPositionListener` exists [v], but it needs a locator added to the model. That is a model change, so it needs the lead. [inf]
- **The listener runs only when the animal draws.** The call is inside the bone render path. An animal that vanilla culls (out of view) draws no bones, so its listeners do not run. [v] (call site in `GeoBone`) A splash behind the player then does not spawn. [inf] This matches the D328 idea (skip what did not draw).
- **GeckoLib allocates for each bone that has a listener.** Per pass and per such bone: about 4 `Matrix4f`, 3 `Vector4f` and 3 `Vec3`, once for the bone, shared by all its listeners. [v] (`new` calls in steps 1 to 7) Our code cannot remove this. One listener object per animal (F2.2) avoids only our own garbage. [inf] Cost: with 4 bones per animal and 30 animals in range, about 1,200 small objects per frame. That is small next to the vertex work (about 370 objects per pass, KQ3), but it is not zero. [inf] So the F2.2 "no garbage per frame" rule needs an exception for GeckoLib's own allocation, or the listener must be added only on frames where an effect can start (in water, eating, running). [inf]
- **Listeners are per pass.** `addBonePositionListener` is a method of `RenderPassInfo`, which is new for each pass. The renderer must add the listener in each pass, for example in `adjustModelBonesForRender` or `preRenderPass`. [v] (`RenderPassInfo` fields) [inf] (where to add it)

## Bone updaters and `adjustModelBonesForRender`

- `BoneUpdater#run(RenderPassInfo, BoneSnapshots)`. [v]
- `BoneSnapshots#get(String)` returns `Optional<BoneSnapshot>`. `ifPresent(String, Consumer)` and `get(GeoBone)` are default methods. [v]
- The snapshot for a bone is the bone's `frameSnapshot` for this pass. If no earlier updater touched the bone, GeckoLib makes a new snapshot with zero rotation and translation and scale 1. [v] (`RenderPassInfo.lambda$compileBoneUpdates$1`, `BoneSnapshot.<init>`)
- **Snapshot rotation is a delta.** The renderer draws the bone with base rotation plus snapshot rotation (`RenderUtil.translateAndRotateMatrixForBone`, source lines 67 to 79). [v] So a code layer adds its angle with `setRotX(getRotX() + delta)`. It never writes an absolute angle. This is what the plan says ("No layer writes an absolute angle").
- `DinosaurRenderer.adjustModelBonesForRender` already does this for `IdleSway` (lines 173 to 191). [v] It is the pattern for layers 2 and 4 to 7.
- **The breathing sway moves the Brachiosaurus front feet.** `IdleSway` turns `hips`, `neck1`, `head` and `tail1` to `tail3`, with a `hips` pitch of 0.5° (`IdleSway.java` lines 19 and 21). [v] In `brachiosaurus.geo.json` the front legs hang from `hips` (Root, `hips`, `deltLeft`, `bicepLeft`, `forearmLeft`, `footLeftFront`). [v] In the other five species no swayed bone parents a leg: their legs hang from the root bone (`root`, `Root` or `bone`), and the Triceratops front legs (`deltLeft`, `deltRight`) hang from `Root`. [v] The front soles are about 62 px from the `hips` pivot, so the sway moves them about 0.54 px at render scale 1. [v] (computed from the pivots and cube origins) That is small today. It grows with the render scale and with the D338 panting sway. [inf] With the sway after the leg IK (layer 7 after layer 2), it moves feet that the IK planted. [inf]
- `BoneSnapshot` has no "additive" flag. Getters and setters for rotation, translation and scale, and `skipRender` and `skipChildrenRender`. [v]

## Additive controllers drop the bone's model rotation

This answers the plan line "Test additive mode first on a bone with a model rotation" (F2.3, overlay controller).

- `AnimationProcessor.setSnapshotRotation` (source lines 135 to 146): for an additive controller, the new snapshot rotation is the keyframe value plus (the snapshot rotation minus the bone's base rotation). For a normal controller, it is the keyframe value. [v]
- The renderer then adds the base rotation again (see above). [v]
- So for a bone with base rotation B, base clip value K1 and overlay value K2, the bone draws at B + (K2 + K1 − B) = K1 + K2. The model rotation B is lost. [inf, from the two [v] facts]
- If the base clip does not key the bone, it draws at K2 alone. B is lost in that case too. [inf]
- Fabric 5.5.5 has the same subtraction. [v]
- Almost every head, neck, jaw and tail bone has a model rotation. Bones with a non-zero rotation whose name has head, neck, jaw, tail, snout or mouth: Brachiosaurus 19, Dilophosaurus 26, Gallimimus 25, Triceratops 14, T. rex 22, Velociraptor 23. [v] (Python count over the six `.geo.json` files)
- Result: the D333 overlay controller on GeckoLib additive mode would snap these bones away from their model pose. [inf] The `ControllerHandOverTest` or a new unit test can show it without a game: one bone with a base rotation, one normal and one additive controller. [inf]
- Ways round it (for the lead or the task plan):
  1. Play the overlay clips through our own bone updater, which adds the overlay keyframe value as a delta. The keyframe evaluation is GeckoLib's, so this needs only a sampler. [inf]
  2. Add an updater after the controllers that adds B back for each bone the overlay keyed this pass. [inf]
  3. Report it upstream and wait for a fix. Item 1b checks the newest GeckoLib code.

## Verdicts

- `BonePositionListener` (world, model, local): **use it** for D339, with a null check, a pivot-bone map per species and the allocation note in F2.2.
- `addLocatorPositionListener`: **reference**. No model has a locator.
- `adjustModelBonesForRender` and `addBoneUpdater` with delta writes: **use them** for layers 2 and 4 to 7. Already proven by `IdleSway`.
- GeckoLib additive controllers: **do not use as they are** for bones with a model rotation. Proposal for the plan: one of the three ways round it.
