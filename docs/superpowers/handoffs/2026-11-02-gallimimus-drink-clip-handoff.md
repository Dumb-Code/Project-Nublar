Suggested session title: **Gallimimus drink clip: user review of v7, then export with mcblend into the mod**

# Handoff: Gallimimus `drink` clip, v7 built, not in the mod (2026-11-02)

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
