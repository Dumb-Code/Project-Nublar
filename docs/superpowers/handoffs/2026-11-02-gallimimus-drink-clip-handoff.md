Suggested session title: **Gallimimus drink clip: in-game check of v19 (when the user asks)**

# Handoff: Gallimimus `drink` clip, v19 in the mod, to check in game (2026-11-02)

**v19 is in the mod**, committed together with this handoff update; not pushed.
- `scripts\insert_clip.py` inserted it into `gallimimus.animation.json` as `drink`: 34 bones, 2,339 keys, the file's key style, key times snapped to whole frames. Every other clip is byte-identical.
- `DinosaurAnimationFilesTest` passes, and the full `:common:test` is 485/485.
- Art list: Drink is "To check in game"; Gallimimus To make 35, To fix 13; totals 456 to make, 25 to check. Roadmap M2 art cell `192 · 12 · 186 · 276`.
- The shared plan doc was synced in the same turn as this commit: Art list body rev 315, roadmap body rev 156.
- **Do not launch the game until the user asks.** On 2026-11-02 they said "do not go to the game, let's assess after".
- The PK reference clip section is still in the session scratchpad; delete it after the in-game check.
- **v20 (built, not in the mod):** the user asked to check the dewlap between 3.75 and 4.24 s. In v19 it pumps in and out there: dewalp3/4 slides swell to 1.08/0.80 px and shrink back over frames 94-103.
  - Cause: `close_flap.py`'s moving-max smoothing pulled the rise frames' big needs (0.7-1.0 px, frames 101+) back into frames 93-99. Fix: only frames 27-97 feed the smoothing.
  - v20 = v19pre + fixed `close_flap.py` + `close_neck.py`: `blend\galli_drink20.blend`, `data\drink20_mcblend.json`. In the window the flap tip moves at most 0.14 px/frame against the head (v19 0.32). The other checks match v19.
  - Files: `preview\dewlap_375_424_v19_v20.mp4` (half speed, v19 left) and `preview\drink_v20_hd_head_{L,FL}.mp4`.
  - **v20 is now in the mod** in place of v19 (the user said "Yes, swap in v20"), committed with this note. `insert_clip.py --replace` removed the old `drink` clip (it must be the file's last clip) and inserted v20; every other clip is byte-identical. Tests 485/485.
  - No plan counts changed, so the shared plan doc needed no sync.
  - Another session was editing this checkout at the time: Java, game tests, README, structure files. Those changes were left out of this commit.

## v19 final

- v19 = v19pre + `close_flap.py` with `FLAP_WIN_START=27` + `close_neck.py`, giving `blend\galli_drink19.blend` and `data\drink19_mcblend.json`. Starting the flap pass at frame 9 opened a 0.51 px² flap/neck hole in the lean-in; starting it at 27 does not.
- **Back:** neck1 and neck2 stay under the back on the drinking frames (29-97): 0.00 px. v14 and v17 had neck1 up to 0.50 px above it.
- **Checks:**
  - Holes 0.04 px² (rest level). Poke-through: throat1 0.27, thigh 0.07 px.
  - Flap as before. Flicker: reversals only on the beats. Export matches Blender.
- **Seams** (side view, animation-made, beyond rest): mean 0.150 px² per frame, worst frame 0.27. v17 had 0.131 / 0.24 and v14 0.230 / 0.38.
  - neck1/neck2 is 0.17 (v17 0.09). neck2 now shifts down to stay under the back, and the down shift halves that seam less well.
  - dewalp4/neck5 improved to 0.02 (v17 0.09).
- **Shape:** neck1 bends 20 deg instead of 32, so the base arches a little less. Head 38/42/3.
- **Files:** `preview\drink_v19_hd_{head_L,head_FL,side}.mp4` and `preview\back_v17_v19.png` (frame 67 shoulder, v17 left, v19 right).

## v18/v19 (neck1 through the back) and where to stop

- **neck1 through the back:** the user, on v17: "neck1 is clipping through the back". When the neck bends down, neck1's top corners rise above the chest's top face; in v14 and v17 they sit 0.4 px above it.
  - `qa_clip.py` misses this. At rest neck1 already sits 1.1 px above the chest top, so the poke check takes that face as the attachment face and skips it.
  - New measure: `scripts\backlib.py` (`above_back`) and the probe `back_poke.py`.
  - Shifting neck1 down pushes throat1 out of the chest underside. Sliding neck1 forward out of the chest works (raptor style), but it needs 3+ px on v14's poses and moves the snout. `fix_back.py` is that pass; it is not used.
  - What works: `keyposes6.py` `BACK_W=0.015` puts neck1 and neck2 under the back at the key poses (`data\poses18.json`). The cost: neck1 bends 20 deg instead of 32, so the base arches a little less. The head angles stay at 38/42/3.
- **v18** (poses18, GRID with neck2/3): rejected. The neck3 slide shortened the neck, the snout lowest point was -0.09 (above the water), and the flap had holes in the lean-in.
- **v19pre** (poses18, `GRID=neck4,neck5,neck6,neck7,neck8,head SLIDE_PEN=0.15`): snout -0.21, holes 0.06, throat1 poke 0.27. It has no `close_flap.py` or `close_neck.py` pass yet. `close_neck.py` now shifts neck2 down only, with `BACK_MAX=0.0`.
- **The user asked for pushback on limits.** The model's tapered neck cubes with mid-height pivots and the throat flap set hard limits. Joint V-notches and the PK shape trade against each other, and neither can reach zero by animation alone. Recommendation: stop iterating; finish one candidate and check it in game. Model-side fixes (more cube overlap, pivots on cube edges) are the lead's call.

## v17 update (animation seams reduced)

The user chose "Fix animation seams". The head's own seams stay; they are the model's, and the lead owns the model.

- **Seam scan** (`crack_ids.py` on frames 0 and every 4th, views L and FL; compare with `scratchpad cmpscan.py`). Pairs where both cubes are head parts are skipped (model). Animation-made seam area beyond rest, side view: v14 mean 0.23 px² per frame (worst 0.38); v17 mean 0.13 (worst 0.24).
- **What worked:** `scripts\close_neck.py IN -- OUT` runs after the build.
  - Per keyed frame, it tries shifting neck2 and neck4 across the neck by 0, 0.25, 0.5 and 0.75 px. It keeps the shift with the smallest rendered seam at that joint (`crack_lib.py`, an importable version of `crack_ids.py`, scoring the L and FL views).
  - It smooths the shifts over ±2 keys and fades them with the flap envelope.
  - Results: neck1/neck2 0.20 → 0.09 px², neck3/neck4 0.10 → 0.03. The neck tapers there (neck1 5 px, neck2 4 px, neck4 3 px), so the step on the outer side of a bend opens; a 0.5 px shift halves it, in either direction. Along-the-neck slides do not help.
  - v17 = `close_flap.py` output v14 + `close_neck.py` = `blend\galli_drink17.blend`, `data\drink17_mcblend.json`.
- **Tried and rejected:**
  - v15, build-time 2D slides (`SLIDE2D=1`): the fitted along-slide pushed neck2 3.6 px through the chest, and the snout stopped reaching the water.
  - Seam-weighted pose solve (`SEAM_W`): no seam gain, and a steeper head (44 deg in the water, 12 on the lift).
  - v16, softer bends (all joints capped at 28 deg): neck seams unchanged (neck2's bend is not the main cause), and flap-neck seams got worse.
  - The flap seam term `SEAM_K=1` in `build6.py` made the lean-in flap seams worse; keep it at 0. `seam2.py` (raster seam measure) stays for probes; its windows do not always match the renders.
- **v17 checks:**
  - Holes 0.06 px² (v14 0.04). Poke-through: throat1 0.26 px only; no neck2-chest poke.
  - Flap as in v14. The shifted neck cubes have no reversals; their acceleration is 0.31 px/frame² at most.
- **Remaining animation seams:** neck5/6, neck7/8 and head/neck8 at 0.03-0.05 px². Flap against neck: dewalp4/neck5 at about 0.09. dewalp3/jawLower5 at 0.14, front-left view only, around frame 99.
- **Render note:** `crack_lib`/`crack_ids` change the scene's render settings: view transform Standard, flat light, no specular. `close_neck.py` now restores them before it saves. Before that fix, v17 was patched back by hand to AgX, studio light and specular on.
- **Files:** `preview\drink_v17_hd_{head_L,head_FL,side}.mp4`, `preview\neckseams_v14_v17.png` (frame 67, neck base, v14 left, v17 right).

## v14 update (snout seams explained, flap slit closed)

The user, on the v13 1080p head view: "why are there so many seams? the snout cubes should not move like that".

- **The snout cubes do not move.** `scripts\head_rigid.py`: every head part (snout1-4, jawUpper1-2, jawLower1-6, cheeks, HeadLower) moves 0.000 px and 0.00 deg against the head in every frame. The export has no keys for them; it has 34 bones, and the only head-part bones are dewalp2-4.
  - `scripts\head_match.py` renders frame 51, and the rest pose moved as one piece so its head sits on frame 51's head. The two renders differ in 32 of 1.6 million pixels, all edge anti-aliasing. `preview\head_rest_matched_vs_clip.png`.
  - The snout and jaw seams are in the model's rest shape (angled cubes: jawUpper1 17, snout1 -42, snout2 -32 deg). Closing them needs a model edit, which is the lead's art.
- **Flap slit:** a thin white slit between dewalp3 and dewalp4, where the tapered flap tilts into the neck. It is too thin for the area-based checks to flag.
  - `scripts\gap2d.py` measures the uncovered side-view gap at a joint (rotations here are all about x, so each cube is a rectangle in side view). It is reliable for the flap joints. On neck joints in transition frames it also counts empty space outside the outline, so do not use it there.
  - `scripts\close_flap.py IN -- OUT` runs after the build:
    - Per keyed frame, it finds the smallest slide of dewalp3 into dewalp2 and of dewalp4 into dewalp3 that closes the gap to the rest level.
    - It smooths the slides (moving max over ±2 keys, then average over ±1) and keys them on frames 9-105 only. In the rise (107-117) the throat folds, and these slides pulled the flap off the neck there: a 0.43 px² hole in a first try.
  - v14 = v13 + `close_flap.py`, giving `blend\galli_drink14.blend` and `data\drink14_mcblend.json`.
- **v14 checks:**
  - Holes 0.04 px² (rest level). No flap slit on frames 21-97.
  - Poke-through: throat1 0.26 px only.
  - Flicker: reversals only on the beats; dewalp3 at 97 is the rise start.
  - Export matches Blender.
- **Still there:** the V-notches at the bent neck joints (neck1/2, neck3/4, neck5/6, neck7/8). They come from the bends; softening the bends is the user's call.
- **Files:** `preview\drink_v14_hd_{head_L,head_FL,side}.mp4` and `preview\flapslit_v13_v14.png` (v13 top, v14 bottom). `scripts\crack_ids.py` now also counts enclosed holes.

## v13 update (throat flap blended)

The user on v12, at the lift: "the throat flap bottom is not blended at all".

- **Cause:** at rest the neck curves, so the flap's bottom edge meets the neck at an angle, and dewalp4 tucks into neck5. On the straight drinking neck, the flap solve kept each flap end at its rest distance from the nearest throat cube. dewalp3 and dewalp4 then lay parallel under neck7/neck8, a cube deep, and ended in a square step. The v12 crack fix (`FLAP_SEAM_W`) also stopped dewalp4 from tilting up, because tilting opened the dewalp3>dewalp4 joint.
- **Fix** (`build6.py`, on by default, `TAPER=0` turns it off):
  - Taper target: the far-end corners of dewalp3 may stick out of the throat by 0.5 px at most (`TAPER3`), dewalp4 by 0 px (`TAPER4`). The rest-depth target no longer applies to those ends.
  - dewalp3 and dewalp4 get a solved seam slide (0-1.5 px, toward the parent), so the flap joints stay closed while the flap tilts up. The slides fade with the flap at the loop ends.
  - Same poses as v12 (`data\poses12.json`); build output `blend\galli_drink13.blend`.
- **v13 checks** (every 2 frames):
  - Holes 0.06 px² (v12 0.08). Flap-cube joints 0.07 px at most.
  - Poke-through: throat1 0.26 px only.
  - The `qa_clip.py` FLAP number for dewalp4 is now 1.04 px. It measures any change from the rest depth, and dewalp4 now sits deeper in the neck on purpose.
  - Flicker: reversals only on the beats. Flap acceleration 0.89 px/frame² peak at frame 19, as the taper starts.
  - In the rise (frames 107-111) the head comes up last, so the throat folds and the flap tucks into it. The protrusion check reads below zero there, as it did in v12. It looks right in close-ups.
  - Export (`data\drink13_mcblend.json`) matches Blender within 0.2 deg.
- **Files:** `preview\drink_v13_side_front.mp4`, `preview\flap_v12_v13.png` (rows lift, water; v12 left, v13 right). New scripts: `flap_view.py` (textured and tinted throat close-ups, frame 0 = rest), `flap_hosts.py`, `flap_protrude.py`.
- **The user has not approved v13.** Insert it with next steps 2.1-2.7, using `data\drink13_mcblend.json`.
- **Seam review of v13** (the user: "still some splitting at the seams at the top of the neck/throat", "so many seam splits on the head as well"):
  - `scripts\crack_ids.py` renders each cube in its own flat colour and names the cubes on both sides of every outline crack. `CI_PROBE=bone:slides` tests extra slides at one joint.
  - **The rest pose already has these seams:** throat1/neck1 0.22 px², jawLower6/dewalp1, dewalp1/dewalp2, and the snout and jaw pieces (snout2-4, jawUpper1-2, jawLower3-4). The clip never moves the head pieces against each other, so the head seams are the model's own. Closing them means editing `gallimimus.geo.json`, which is the lead's art.
  - **The animation adds:** neck1/neck2 (two wedges of about 0.08 px² each), neck3/neck4 (about 0.1), neck5/neck6 and neck7/neck8 (0.03-0.05). They are hairline V-notches, about 0.04 px wide, where cubes of different sizes meet at a bend. Extra slides do not close them: neck2 halves at best, neck4 does not change, and closing neck8 opens others.
  - `look.py` now renders with cavity shading off by default (`CAVITY=1` turns it on). Cavity drew light lines on every cube edge, which made seams look worse than in game. `preview\drink_v13_nocavity.mp4`, `preview\head_rest_vs_v13.png` (left rest, right v13 lift; top side, bottom front-left).
  - Open choice for the user: check v13 in game, or soften the neck bends (fewer and smaller notches, less PK shape).
  - `scripts\render_hd.py OUT VIEW [W H] [AA]` renders 1080p views: `side`, `front`, `head_L`, `head_FL`. The head views use a steady camera that follows the head. Output: `preview\drink_v13_hd_{side,front,head_L,head_FL}.mp4`.

## v12 update (seams and misaligned cubes)

The user marked two spots on v9: "fix the seams and cubes not aligning here" (the lower neck behind the head, at the lift) and "another seam here" (a neck bend).

- **Causes found:**
  1. At neck1>neck2 and neck2>neck3, the joining-face corners stuck out of the next cube by 1.1-2.7 px. v9 bent those mid-height pivots 38-41 degrees. The old seam check only looked at the 4 corners nearest the pivot, so it missed this. On the same full-face measure, v7 had 1.2 px and v8 1.9 px.
  2. Behind the head, the model's rest S-curve kink at neck7/neck8 made a zig-zag on a lowered, straight neck: neck6 tipped up, then neck7 and neck8 tipped down.
- **Fixes:**
  - `keyposes6.py` now also has a joint-face overhang cost (`FACE_W`, threshold `FACE_T` 0.6 px), and a separate head-joint limit (`HEADLIM`). It can read slide constants from a build (`SLIDE_C`, JSON, px per tan(bend/2)).
  - v12 settings: `MID=40,40,35 ANG_W=2 SHAPE_W=4 BASE=45,45,45 POKE_W=0.02 FACE_W=0.15 TOP=30 HEADLIM=10`. Result: `data\poses11_h10.json`, copied to `data\poses12.json`.
  - neck7 and neck8 may now bend 30 to straighten the top of the neck. The head joint stays at 10: at 18-30 it opened a see-through gap between HeadLower and neck8.
  - `build6.py` settings `GRID=neck2,neck3,neck4,neck5,neck6,neck7,neck8,head SLIDE_PEN=0.15`:
    - The slide grid search uses the full-face measure at those joints. `SLIDE_PEN` keeps it from sliding neck3/neck4 by 2.5 px; slides that big shortened the neck, and the snout missed the water.
    - The flap solve has a new term (`FLAP_SEAM_W` 0.1 px) that keeps dewalp2>dewalp3 and dewalp3>dewalp4 joined. Without it, v11 opened a 0.23 px crack there.
- **v12 result:**
  - Head bone angle: water 38, dip 42, lift 3.
  - Neck segments, water: 52, 60, 65, 57, 40, 29, 11, 36. Lift: 46, 53, 53, 34, 7, -6, -24, 0.
  - The neck base still arches, a little less than v9. The lift neck is a smooth diagonal into a level head.
  - The body stands taller (drop 0.12-0.2).
- **v12 checks** (every 2 frames):
  - Holes 0.08 px² (v9 0.07). Outline notches 0.86.
  - Worst joint-face overhang 0.86 px (v9 2.68, v7 1.22). Flap-cube joints 0.14 px at most (rest level).
  - Poke-through: throat1 0.26 px only. Flap 0.52 px (v9 0.46).
  - Flicker: reversals only on the beats. Head acceleration 0.64 peak.
  - Export (`data\drink12_mcblend.json`) matches Blender within 0.2 deg.
- **Files:**
  - `blend\galli_drink12.blend` and `preview\drink_v12_side_front.mp4`.
  - `preview\seams_v9_v12.png`: rows lift, water; v9 left, v12 right.
  - `preview\compare_pk_v9_v12.png`.
  - New scripts: `closeups_neck.py` (high-res textured neck views), `probe_faces.py` and `probe_flapfaces.py` (full-face overhang per joint), `probe_slide2d.py`.
- **The user has not approved v12.** Insert it with next steps 2.1-2.7, using `data\drink12_mcblend.json`.

## v9 update (PK neck arch)

The user circled the neck base on v8 (neck1-neck3, where the neck leaves the chest): "wouldn't changing the rotation here on these cubes help with the shape?" In v8 those cubes made one straight ramp at about 40 degrees.

- **PK neck profile** (traced on a grid, degrees below forward, model segments neck1>2 to neck8>head): water 47, 71, 78, 59, 38, 32, 29, 27; lift 41, 62, 64, 34, 11, 1, -5, -7. The base arches out of the shoulders, the middle is near vertical, the bottom curves forward.
- **v9:** `scripts\keyposes6.py` adds that profile as a cost term. Env vars used: `TOP=12 MID=40,40,35 ANG_W=2 SHAPE_W=4 BASE=45,45,45 POKE_W=0.02 POSES_OUT=poses9.json POSES_BLEND=galli_poses9.blend`. Then `build6.py` with `DRINK_BLEND=galli_drink9.blend -- data\poses9.json`.
  - Water segments 48, 69, 77, 51, 18, 13, 9, 45: the base and the middle match PK. The lower neck is flatter than PK, because the model has a built-in kink at neck8/head, and the flap joints may bend only 12.
  - neck2 41 and neck3 38 (v8: 13 and 32). The body crouches less (drop 0.29, v8 0.5), so it stands taller, as in PK.
- **Trade-off:** the head is steeper than in v8. Bone angle: water 47 (v8 40), lift 14 (v8 3).
- **v9 checks** (every 2 frames):
  - Holes 0.07 px² max (v8 0.06). Outline notches 0.72 (v8 0.91).
  - Poke-through: throat1 0.26, thighs 0.06 px (v8 0.27).
  - Flap 0.46 px. Fingers 0.30 blocks off the ground.
  - Flicker: reversals only on the beats (frames 55, 59, 97). Head acceleration 0.62 peak. Export matches Blender within 0.2 deg.
  - Close-ups at the water, the lift and the rise show no holes.
- **Files:** `blend\galli_drink9.blend`, `data\poses9.json`, `data\drink9_mcblend.json`, `preview\drink_v9_side_front.mp4`, `preview\compare_pk_v8_v9.png` (rows PK, v8, v9), `preview\neckbase_v8_v9.png`, `scripts\color_neck.py` (a side render with each neck cube in its own colour).
- **The user has not picked v8 or v9.** Insert the chosen one with next steps 2.1-2.7.

## v8 update (later on 2026-11-02)

The user rejected v7: "I still don't like how the head is always pointed down unlike the PK ref". v8 fixes the head angle. Everything below about v7 still applies to v8, except what this section changes.

- **Measured from the PK ref** (2:07-2:13 re-downloaded with approval, background animal): in the water the head points about 25-35 degrees down; on the swallow lift it is about level.
- **v8 result** (head bone angle, rest = 7): water 40, dip 43, lift 3. v7 was 75, 80, 36.
- **How:** the nose-up counter-bend sits in neck4-neck6 (limits 40, 40, 35), below the throat flap. neck7, neck8 and head may bend 12 (v7: 6). A probe showed nose-up bends at neck4-neck6 open no seams. v7's solver could not reach the angles because it moved one joint at a time.
- **Files** (v7 files are kept):
  - `scripts\keyposes5.py`: key poses with paired moves (bend one joint, unbend another). Settings by env vars. v8 used `TOP=12 MID=40,40,35 ANG_W=2 POSES_OUT=poses8_c.json POSES_BLEND=galli_poses8_c.blend`, run on `galli_mcblend.blend` with `-- data`.
  - `scripts\build6.py`: the v7 build with three fixes. Run `DRINK_BLEND=galli_drink8c.blend` on `galli_mcblend.blend` with `-- data\poses8_c.json`.
    1. Seam slides at neck4-neck7 use a grid search, not golden section. The gap falls with the slide, then jumps once another corner pokes out.
    2. At those joints the gap measure checks the whole joining face. The four corners nearest the pivot miss the underside of a nose-up bend on the 3x3x3 cubes.
    3. The flap is solved again against the final seam slides, starting from the previous frame only. v7 solved it against the formula slides, and in v8 that left the flap end 1.1 px off the throat in the rise.
  - `blend\galli_drink8c.blend` (action `drink`), `data\poses8_c.json`, `data\drink8_mcblend.json` (mcblend export, matches Blender within 0.2 deg), `preview\drink_v8_side_front.mp4`, `preview\compare_pk_v7_v8.png`.
  - New helpers: `head_vis.py`, `keyposes_common.py`, `probe_gaps.py`, `probe_n6.py`, `where_joint.py` (maps a hole in a `holes.py` side view to a neck joint), `flicker_at.py` (frames of sharp reversals), `flap_dump.py`, `cmp_keys2.py` (`cmp_keys.py` for a named export file).
- **v8 checks** (every 2 frames, v7 at the same frames in brackets):
  - Holes 0.06 px² max (0.04): hairline cracks under neck5-neck7, about 0.04 px wide. Outline notches 0.91 px² (0.62), from the sharper S-curve.
  - Poke-through: the same as v7 (thighs 0.27, throat1 0.26 px). Flap 0.46 px (0.46).
  - Flicker: the snout reverses only on the dip, swallow and rise beats (frames 55, 59, 75, 97). No flap wobble. Head acceleration 0.59 px/frame² peak.
  - Close-ups (side, both front angles, flap tinted red) at the water, the lift and the rise show no holes.
- **The user has not approved v8.** Next steps 2.1-2.7 below apply with the v8 files: the export is done (`data\drink8_mcblend.json`).
- **JWE3 references** (the user asked; metadata search only, nothing downloaded): JWE3 has Archaeornithomimus, Gallimimus, Ornithomimus, Struthiomimus and Deinocheirus. No video confirms an ornithomimid drinking. Best leads:
  - `kDly3cXP01w`: SuperSim, a Gallimimus herd drinking. Drinking is confirmed, but it is **JWE2** footage.
  - `m7wed3W41zM`: Krowasaurus, JWE3, chapter "Watering Hole in the Desert" from 24:26.
  - `cupDTmVrumM`: National Jurassic, all 95 species; Gallimimus from 1:26:38, Struthiomimus from 3:27:39.
  - `6ixMDvdqr2A`: Dracorex (not an ornithomimid), chapter "Drinking Water" 3:23-4:04. It proves JWE3 has drink clips.
  Each download needs the user's approval first.
- The PK clip section is in this session's scratchpad only; delete it when the clip is done.

## Where things are

- **Branch:** `m0.p2/port-26.2`. Local only, nothing pushed.
  - `832009b` added a first drink clip; `1a17c7a` reverts it. The mod has **no** Gallimimus `drink` clip now, and the Art list Drink row says Missing again.
  - `7e1052a` (another session) records mcblend as the Blender clip route (D321). Keep it.
- **Current clip:** v7, in `Documents\Minecraft Modding\Animation Work\gallimimus_drink\` (outside the repo):
  - `blend\galli_drink6.blend`: the action `drink` (v7). `blend\galli_mcblend.blend`: the clean mcblend import of `gallimimus.geo.json`, the base for every build. `blend\galli_poses6.blend`: the three key poses.
  - `data\poses6.json`: the solved key poses. `data\drink7_mcblend.json`: v7 exported with mcblend (`animation.drink`, 34 bones, 5.0 s, loop). **Not yet inserted or tested in GeckoLib.**
  - `preview\drink_v7_side_front.mp4`: side and front render of v7.
  - `scripts\`: the pipeline (see below).
- **The user has not approved v7.** The last message asked "Does v7 look right to put into the mod?" Start there.
- **Not seen in game.** Never claim it works in game until the user checks it in `runClient`.

## What the user asked for, and every correction so far

The original ask: make one missing clip from the Art list, GeckoLib-ready and high quality, using a Blender-to-Blockbench tool, with references from JWE3, Prehistoric Kingdom, Prehistoric Planet and Netflix's The Dinosaurs. The user made an exception to the agent-art rule (D330) for this clip.

Each row below is a correction the user made. A new version must keep all of them.

| # | User said | What it means for the clip |
| --- | --- | --- |
| 1 | "use mcblend instead of sparkle" | Build on the mcblend rig, export with mcblend. Sparkle is not used. |
| 2 | v1: "cubes disconnect", "does not mimic the references", "incredibly stiff" | Joints must stay closed; motion must follow the reference with overlap, not pose-to-pose. |
| 3 | JWE3 reference "submerged in water covering the entire neck" is bad | Use the Prehistoric Kingdom Update 15 cinematic (`vYgj8kvGNQU`), 2:07.7-2:12.3, the **smaller background Gallimimus** (clean side view, dry bank, whole neck visible). |
| 4 | v4: neck shape wrong; throat cubes go into the neck or vanish; arms static, fingers clip the ground; head "nibbles" | Neck shape traced from the reference; throat flap must stay on the throat; arms fold and move; fingers off the ground; each sip lifts the head to swallow. |
| 5 | "improve the leg spacing and pose, it should not be so static" | Staggered stance: the left foot steps forward on the way down and back on the way up; knees flex with the crouch; weight shifts. |
| 6 | v5: "seams and literal holes in the head and neck", "arms pretty damn wonky", "why can't you reference how the velociraptor drinking was done" | Follow the hand-made `velociraptor.animation.json` `drink` (below). |
| 7 | v6: neck flap disconnected at the bottom; neck clipping through the body; pubis clipping through the top | Nothing may poke through the body; the flap must hug the throat. |
| 8 | "it's almost going behind the shoulders to drink" | The snout drinks in front of the chest (now about 1.1 blocks in front). |
| 9 | "why is the head lifting up so high for every swallow? that's a ton of movement" | Modest swallow lift: snout about 0.55 blocks above the water, body stays crouched. |
| 10 | "why does it keep jerking back and forth?" | No frame-to-frame flicker on any bone (fixed in v7). |

## How the raptor drink is built (copy this approach)

From `velociraptor.animation.json` `drink` (hand-made, 3.625 s):

- Big bends on few joints: hips 32.5, neck1 up to 50-60, smaller mid-neck bends, head -11 to -32.
- **Position keys hide seams:** neck1 moves about 4 px *out* of the shoulders as it bends (position z -4.2); the head slides 1.6-3 px along the neck.
- The shoulders (chest) barely move (sine of about 5 degrees). The tilt is on the hips.
- Throat pieces under the jaw rotate 13-22 degrees to follow the throat.
- Arms: forearm -24 to -48, bicep +19 then -7, fingers -24 to +26, all local rotations.
- Legs: thigh *position* y -3.1 instead of big thigh rotations.

## v7 design (what `build5.py` does)

- **Key poses** (`keyposes4.py`, solved against measured constraints):
  - WATER: snout 0.13 below the bank top, 1.1 blocks in front of the chest, head pointing about 75 degrees down.
  - DIP: WATER with the snout 0.17 below.
  - LIFT: snout 0.55 above the bank, head about 35 degrees down.
  - Body tilt on the hips (32); chest 4 degrees at most.
  - Bend limits: neck1 45, neck2 35, neck3 32, neck4 30, neck5 15, neck6 10, neck7 6, neck8 6, head 6. LIFT may use neck4 35, neck5 25, neck6 14.
  - Keeping head, neck7 and neck8 at 6 degrees or less keeps the throat flap on the throat. Bigger bends there folded the throat and lifted the flap off.
- **Timeline (5.0 s loop):**
  1. Lean in (0.25-1.30), then dip.
  2. Lift and swallow (around 2.05).
  3. Down, dip, lift and swallow again (around 3.65).
  4. Rise back to idle (4.00-4.76).
  Overlap: the base leads going down, the head leads going back down to the water, and the tail trails.
- **Per frame:**
  - Legs: IK keeps the feet planted; the left foot steps 0.28 blocks.
  - Belly: `belly1` counter-rotates against the chest, so the belly pieces don't swing through the hips.
  - Throat flap: `dewalp2-4` are solved to stay flush with the throat, with a smoothness term. `dewalp1` (on the jaw) never moves: moving it opened holes in the head. The flap fades to exactly rest at the loop ends.
  - Seam slides (position keys): neck1 slides out of the chest, by `k1 * h * tan(bend/2)`. The other neck joints slide into their parent, by `C[joint] * tan(bend/2)`. `C` is fitted once per joint from per-frame gap solves (75th percentile).
    - Never solve slides independently per frame: that flipped neck8's slide between 0 and 1 px every two frames, and that was the head jerk.
- **Jaw:** never opened. The flap hangs from the jaw, so opening it pulls the flap away and opens holes.

## Checks (all must pass before showing the user)

Run from `Animation Work\gallimimus_drink\scripts\`. Blender: `"C:\Program Files\Blender Foundation\Blender 5.2\blender.exe" -b <blend> --python <script> -- <args>`.

| Check | Script | v7 result | Rest pose |
| --- | --- | --- | --- |
| Holes and seam cracks in the head and neck, 5 views, silhouette render | `holes.py <out> <frames> drink neck` (also `arms`) | holes 0.04, cracks 0.60 px² max | 0.04 / 0.44 |
| Poke-through (a part's corner beyond the body face, with the line back to its joint through the body) | `qa_clip.py drink 2` | throat1 0.27 px, thighs 0.21 px | 0 |
| Flap off the throat | `qa_clip.py` (FLAP line) | 0.46 px max | 0 |
| Flicker (direction reversals per bone) | `flicker.py` | at most 2 per bone, all intended beats | n/a |
| Head acceleration | `jitter.py` | 0.53 px/frame² peak, in the rise to idle | n/a |
| Feet planted, snout depth, finger clearance | printed by `build5.py` | 0.000 px slip, snout -0.21, fingers 0.17 blocks | n/a |
| Export matches Blender | `export5.py`, then `cmp_keys.py` | v5 matched within 0.3 deg; rerun for v7 | n/a |

Visual QA the user relies on: head close-ups from the side and both front angles (`closeups.py`, `closeups_head.py`), the flap tinted red and fingers blue (`tint_frames.py`), hips from above (`closeups_body.py`; its "from below" views only show the ground, so ignore them), full side and front renders (`render2.py`).

Lessons:
- Every check that passed on single frames still missed the jerk. Always run `flicker.py`.
- Always look at close-ups from the user's angles before sending anything.

## Known open points in v7

- On the swallow lift the head still points about 35 degrees down. A more level head needs bigger bends on the thin top neck cubes, which reopened seams and the flap gap.
- Faint pale lines show where some neck cubes meet in front views, about the size of the rest pose's own cube edges.
- `build5.py` reports a 1.33 px neck7-neck8 corner opening at t=0.42 s, but the silhouette check shows no hole there.
- The keyframes are written every 2 frames as catmull-rom. In-game interpolation may differ slightly from the Blender preview.
- The Drink task (`Dinosaur.java:871`, 100 ticks) starts within about 2.2 blocks of the water and does not turn the animal to face it. The snout reaches about 2.1 blocks in front of the origin, so some drinks may dip beside the water until neck-reach work exists.

## Next steps (in order)

1. Ask the user to review v7 (`preview\drink_v7_side_front.mp4`). Fix what they find; rerun every check and the close-ups.
2. On approval:
   1. Export: `blender -b blend\galli_drink6.blend --python scripts\export5.py -- data\drinkN.json`.
   2. Rename `animation.drink` to `drink`. Reformat the keys to the file's style (`{"post": {"vector": [...]}, "lerp_mode": "catmullrom"}`), then insert the clip as text into `common/src/main/resources/assets/projectnublar/geckolib/animations/entity/gallimimus.animation.json`. Leave every other byte of the file unchanged. `scripts\bb2geckolib.py` (the older Sparkle-era converter) has the text-insert logic to reuse (`--insert`); its input reader expects a .bbmodel, so adapt it.
   3. Run `JAVA_HOME="/c/Program Files/Java/jdk-26.0.1" ./gradlew --offline :common:test --tests "net.dumbcode.projectnublar.client.DinosaurAnimationFilesTest"`, then the full `:common:test` (485 tests).
   4. Art list (`docs/plan/art-list.md`, Gallimimus): Drink row to "To check in game"; To make 36 to 35; To fix 12 to 13 (add "Check in game: Drink"); summary row and totals (456 to make, 25 to check); changelog line. Roadmap M2 art cell `192 · 11 · 186 · 277` to `192 · 12 · 186 · 276`.
   5. Sync the shared plan tabs (rule `sync-shared-plan-doc`). The doc is `acaba6d1-767a-422d-8dee-50c410217bb9`: Art list body `21232809-e729`, roadmap body `be030061-c438`.
   6. Commit locally (no push). Run `graphify update .`.
   7. Launch the headed client (`runClient`) for the user to check it in game.
3. Delete any reference videos once the clip is done (user rule: delete after use).

## Tools installed this session

- Blender 5.2.3 LTS (`C:\Program Files\Blender Foundation\Blender 5.2`), with these add-ons:
  - mcblend 13.0.0 (`%APPDATA%\Blender Foundation\Blender\5.2\scripts\addons\mcblend`).
  - Sparkle 1.3.1 (extension `mcbedrockparticles`): installed, no longer used.
  - cobra-tools (junction `addons\cobra_tools`): installed, plus the `bitarray` module.
- cobra-tools 2026.09.28 reads JWE3 animation names and lengths only. Every compressed keyframe block fails, and its maintainers say JWE3 is not supported (OpenNaja/cobra-tools #430, #440). Do not retry it.

## References (deleted after this session; re-download needs per-clip approval)

All 480p, no sound, with `yt-dlp -f "bv*[height<=480][ext=mp4]" -o "%(id)s.%(ext)s" -- <id>`:

- `vYgj8kvGNQU`, Benny T, "Gallimimus Nesting! - Prehistoric Kingdom Update 15 Cinematic". **The main reference:** 127.7-132.3 s, the background animal. Crop `crop=300:170:40:175` on the 854x480 frame.
- `_7eGEVsPJJU`, BAKEMONODA, "GALLIMIMUS 10 MIN, Prehistoric Kingdom": distant drinks at about 3:30 and 7:20.
- `MToIgmOVcsE`, `oqOStuDijIQ`: ostriches drinking (scoop and lift, deep U-shaped neck).
- `wsP9EwcirHU`, Throneful, JWE3 Gallimimus gameplay: the user rejected this as a reference (the animal is submerged).
