# Item 1b: GeckoLib upstream check (additive animations and bone base rotation)

Date: 2026-11-03

## Scope

- Check the GeckoLib repo (`bernie-g/geckolib`, branches `26.2` and `main`).
- Question: does an additive controller drop the bone's model (base) rotation? Is that still the code? Was it changed? Is there an issue or changelog entry?
- List the newest GeckoLib 5 release for Minecraft 26.2 on Modrinth and CurseForge.
- I read source at the named commits. I did not run GeckoLib.

## Labels

- `[v]` checked in source at a named commit, with path and lines.
- `[doc]` from a README, wiki, changelog, store page or release note (with link).
- `[inf]` my inference. Not run.

## Answer in short

- Yes. The additive rule is still in the newest code. The lines are identical on `26.2` and `main`. [v]
- Nobody has changed those lines since they first appeared on 2025-12-10. [v]
- I found no issue, no pull request, no changelog line and no wiki text about additive animations losing base rotation. [v]
- Related items: issue #659 (closed), issue #889 (open), commit `9925e89a20` (changed how base and snapshot rotation are summed at draw time).
- Newest GeckoLib 5 for MC 26.2: NeoForge and Forge 5.5.6 (2026-09-16). Fabric 5.5.5 (2026-09-06). Version 5.5.7 exists only for MC 26.3. [doc]
- Extra finding, not run: the same code looks wrong when an additive controller fades out. See section 6.

## 1. Repo state

- Branches: `main`, `26.2`, `26.1`, `1.21.11` and older. [v] (`gh api repos/bernie-g/geckolib/branches`)
- `main` = `7c96bef7e1658e4158453c17eb464f0fb7f92426`, 2026-09-27, "Add modrinth environment". [v]
  - `gradle/libs.versions.toml` line 3 says GeckoLib `5.5.7`. Line 6 says Minecraft `[26.3,)`. [v]
  - `changelog.md` has the heading "GeckoLib v5.5.7". [v]
- `26.2` = `1e09655d3b54c2db959d234266fb564b90aa9382`, 2026-09-16, "Update to 5.5.6". [v]
  - `gradle/libs.versions.toml` line 3: `5.5.6`. Line 6: Minecraft `[26.2,)`. [v]
- `26.1` = `d91c9b36c8e18aa0ad4528e821a358f2e37b0773`, 2026-06-27, "Update to 5.5.2". [v]
- GitHub has no release objects for this repo. Use the stores for builds. [v] (`gh api .../releases` returned an empty list)

Path shorthand: `GL` = `common/src/main/java/com/geckolib`. Line numbers are for `main` unless the text says `26.2`.

## 2. What the code does

### Additive rotation

- File and lines: `GL/animation/AnimationProcessor.java:134-148` on `main`. On `26.2`: `133-147`. [v]
- Steps:
  1. Read the keyframe value for X, Y and Z at the current time. [v] lines 136-138
  2. If the controller is additive, add (snapshot rotation minus the bone's base rotation) to each value. [v] lines 140-145
  3. Write the sum into the snapshot. [v] line 147
- The base rotation is the rotation stored in the model file for that bone. [v] `GL/cache/model/GeoBone.java:33-35, 52-54, 93-105`

### The snapshot starts at zero

- A new `BoneSnapshot` has rotation 0, translation 0 and scale 1. [v] `GL/animation/state/BoneSnapshot.java:16-26, 36-38`
- One snapshot is made the first time a bone is asked for in a pass, and cleared when the pass ends.
  - `26.2`: `GL/renderer/base/RenderPassInfo.java:293-310` (creation at line 299). [v]
  - `main`: `GL/renderer/base/BoneSnapshots.java:29-55` (moved there by commit `a4b3fe3a2c`, 2026-09-21). [v]
  - Clear at the end of the pass: `GL/animation/state/BoneSnapshot.java:250`. [v]
- Nothing sets the snapshot rotation to the base rotation before the controllers run. [v] (`grep` for `setRotation` finds only `AnimationProcessor.java:147` and the setter itself)
- So the snapshot rotation is a change from the base pose, not an absolute angle. [inf]

### The draw adds the base back

- `RenderUtil.translateAndRotateMatrixForBone` starts from the base rotation, adds the snapshot rotation, then rotates in Z, Y, X order. [v] `GL/util/RenderUtil.java:66-80` (same lines on `26.2`)

### What follows

- Non-additive controller: snapshot = keyframe. Drawn angle = base + keyframe. [v/inf]
- Additive controller, bone not yet written this pass: snapshot = keyframe + 0 minus base. Drawn angle = base + keyframe − base = keyframe. The base rotation is gone. [inf, arithmetic from the lines above]
- Additive controller, after a non-additive one wrote value A: snapshot = A + keyframe − base. Drawn angle = A + keyframe. The base is still gone. [inf]

Worked example (my arithmetic):

| Step | Snapshot | Drawn angle (base 30 deg) |
|---|---|---|
| Main clip, keyframe 10 deg | 10 | 40 |
| Additive head clip, keyframe 5 deg | 10 + 5 − 30 = −15 | 15 |
| Expected | | 45 |

The error equals the base rotation (−30 deg). If the base rotation is 0, there is no error. [inf]

### The other channels do not subtract anything

- Additive translation adds the snapshot value. `AnimationProcessor.java:150-163`. [v]
- Additive scale multiplies by the snapshot value. `AnimationProcessor.java:119-132`. [v]
- The reset target for rotation returns the snapshot rotation, with no base term. `AnimationProcessor.java:250-254`. [v]
- So the `− base` term appears only in the additive rotation step. It looks like a leftover from a model where the snapshot held the absolute angle. [inf] The author's intent is unknown to me.

### Controller order and our updater

- Controllers apply in the order of the controller array. [v] `GL/renderer/base/GeoRendererInternals.java:147-158`
- The array follows the registration order of the controllers (fastutil array map). [inf]
- A non-additive controller overwrites the snapshot. So register the additive controller after the main one.
- The animation controllers run as the first bone updater. The model adjust step runs second. Updaters we add run later. [v] `26.2` `GL/renderer/base/RenderPassInfo.java:284-285`
- So our code can read and change the snapshots after the controllers have run.

## 3. History of those lines

- The function first appears in this form in commit `3e2276128b1f2b78e668b675eec926c5a375eabb`, 2025-12-10, Tslat, "Update for 1.21.11 Alpha 1 ALPHA". [v] (`git log -L134,148:...AnimationProcessor.java` on a blobless clone of `main`)
  - No later commit changed those lines, through the 2026-02-22 package move. [v]
  - `git log -S baseRotX` and `git log -S additiveAnimations` give the same first commit. [v]
- Commit `9925e89a20060a0b99cd46f2653783f375028549`, 2025-12-25, "Fix bone rotation being applied consecutively instead of additively". [v]
  - It changed `RenderUtil`. Before: rotate by the base, then rotate by the snapshot. After: add the angles, then rotate once.
  - It did not touch `AnimationProcessor`. [v]
- Both commits are on branches `1.21.11`, `26.1`, `26.2` and `main`. [v] (`git branch -r --contains`)
- Latest commits that touch `AnimationProcessor.java`: `15ff7cb461` (2026-09-21, keyframe helper) on `main`; `cec6c88f78` (2026-08-29) on both. [v]
- The only difference in that file between `26.2` and `main` is the keyframe lookup in `findAnimationPointValue` (lines 173-178 on `main`). [v] (diff)
- Other files I compared between `26.2` and `main`: `BoneSnapshot.java` is identical. `RenderUtil.java` differs only in imports and pose-stack calls for the 26.3 port (8 changed lines, none in `66-80`). `BoneSnapshots.java` and `RenderPassInfo.java` differ in how snapshots are looked up (commit `a4b3fe3a2c`), not in what they hold. [v]

## 4. Issues, changelog, docs

- No hit for "baseRot" or "additiveAnimations" in issues and pull requests. [v] (GitHub search API)
- Search for "additive" finds two issues:
  - #659 "Support Animation blending", closed 2026-01-01. The maintainer says it is done in 1.21.11+ with an additive controller option and bone snapshots. [doc] https://github.com/bernie-g/geckolib/issues/659
  - #889 "Proper Controller Crossfade Support", open since 2026-08-14, 0 comments. It proposes weight-based blending in place of raw additive accumulation, like Blockbench. [doc] https://github.com/bernie-g/geckolib/issues/889
- Commit search for "additive" finds only `9925e89a20`. [v]
- `changelog.md` on `main` lists only v5.5.7. It has no line on additive animations. [v] `changelog.md` (7 lines)
- `git log -G additive -- changelog.md` over `main` history returns nothing. [v]
- The 5.5.6 changelog (on `26.2`) covers Molang, pre/post keyframe easing and catmull-rom ends (#895). Nothing on additive. [v] `26.2` `changelog.md`
- The wiki repo (`bernie-g/geckolib.wiki`, commit `1f12b23d7a40273769454e0fb77a4224b5d2af6a`, 2026-10-20) has no page that mentions additive. [v] (grep)
- Javadoc on `AnimationController#additiveAnimations` says animations add onto the value the bones already have. [v] `GL/animation/AnimationController.java:240-246`
  - Read with the rule above, this matches "snapshot is a change from base". It does not explain the `− base` term. [inf]

## 5. Newest GeckoLib 5 for Minecraft 26.2

Modrinth project `geckolib` (API listing, checked 2026-11-03). [doc] https://modrinth.com/mod/geckolib

| Version | Loader | MC | Date | File |
|---|---|---|---|---|
| 5.5.6 | NeoForge | 26.2 | 2026-09-16 | `geckolib-neoforge-26.2-5.5.6.jar` |
| 5.5.6 | Forge | 26.2 | 2026-09-16 | `geckolib-forge-26.2-5.5.6.jar` |
| 5.5.5 | Fabric | 26.2 | 2026-09-06 | `geckolib-fabric-26.2-5.5.5.jar` |
| 5.5.7 | all three | 26.3 only | 2026-09-22 | `geckolib-<loader>-26.3-5.5.7.jar` |
| 5.5.6 | Fabric, NeoForge | 26.3 | 2026-09-16 | `geckolib-fabric-26.3-5.5.6.jar` |

- Modrinth lists no Fabric 5.5.6 file for 26.2. The `26.2` branch builds 5.5.6. [v] I do not know why the Fabric file is missing. [inf]
- CurseForge file list agrees: newest are 5.5.7 for 26.3, then 5.5.6 NeoForge and Forge for 26.2, then 5.5.5 Fabric for 26.2. [doc] https://www.curseforge.com/minecraft/mc-mods/geckolib/files (read through a page-fetch summary)
- This matches our jars: 5.5.5 Fabric and 5.5.6 NeoForge.
- Upgrading to 5.5.7 means moving to MC 26.3. Its changelog lists animation speed-ups and fixes (reverse animations, easing override, `relative_to`). Nothing on additive. [v] `main` `changelog.md`

## 6. Extra finding: fade-out of an additive controller (not run)

Read from `main` lines 224-237 (reset), 240-261 (reset target), 139-145 (additive step). On `26.2`: 222-235, 238-259, 139-144.

1. When a controller ends its clip and resets, the engine lerps from the clip value to a reset target. [v]
2. For an additive controller, the reset target is the current snapshot value. [v] lines 240-254
3. After that, the additive step runs on the result and adds (snapshot − base) again. [v] lines 140-145
4. At the end of the reset, rotation = snapshot + (snapshot − base) = 2 × snapshot − base. Drawn angle = base + that = 2 × snapshot. [inf]
5. With snapshot 0, the drawn angle ends at 0 degrees, not at the base angle. [inf]
6. When the controller stops and returns no state, the bone goes back to the base. That is a visible pop of the base angle. [inf]
7. Translation would double the snapshot. Scale would square it. [inf]

I did not run this. Test it before relying on it:

1. Make a one-bone model with a 30 degree base rotation on X.
2. Add an additive controller that plays a short clip on that bone.
3. Watch the bone while the clip ends. Print the snapshot rotation in a bone updater.

## 7. What this means for F2.3

- Additive head overlay clips are safe only on bones whose model rotation is 0. [inf]
- If a head bone has a base rotation, pick one:
  1. Move the model rotation to a parent group, so the head bone has rotation 0.
  2. Key the overlay clip with the base angle added. [inf] The `− base` term then cancels. This hurts clip authoring.
  3. In our bone updater, add the base back to bones that an additive controller wrote. [inf] This needs a list of those bones. It does not fix the fade-out case in section 6.
  4. Skip the additive controller. Write the head overlay from code, as a delta on the snapshot.
- Layers 2 to 7 write deltas into the snapshots. They add to the snapshot rotation. They do not add the base rotation. The draw adds it. [v] `RenderUtil.java:66-80`
- No upstream fix is planned that I can see. Issue #889 may change the blend model later. [doc]
- Report to upstream: not done. Needs a decision from the lead.

## Not checked

- Bytecode of the released jars (you already read 5.5.5 and 5.5.6).
- Discord or other chat channels.
- Whether `fastutil` array maps keep insertion order in the version GeckoLib ships. I rely on general knowledge.
- Any run of the code.

## Verdicts

- GeckoLib `26.2` branch (`1e09655`, 5.5.6): same additive rule. Use 5.5.6 for NeoForge. Work around the base-rotation drop.
- GeckoLib `main` (`7c96bef`, 5.5.7, MC 26.3): same rule. Reference only. No upgrade for 26.2.
- Commit `9925e89a20` (draw-time sum): reference. It confirms "snapshot is a delta".
- Issue #659: reference. It confirms additive controllers are the supported blend path.
- Issue #889: reference. Watch it. It may replace additive accumulation.
- Modrinth and CurseForge listings: use. NeoForge 5.5.6 and Fabric 5.5.5 are the newest for 26.2.
- Section 6 finding: needs a test before we trust it.
