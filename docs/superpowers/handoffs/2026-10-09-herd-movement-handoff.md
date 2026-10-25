# Handoff: herd movement and dinosaur rendering (2026-10-09)

For the next session or person picking up this work. Read this first, then the files it points to.

## Where the work lives

- Worktree: `C:\Users\kgumm\Documents\Minecraft Modding\Project-Nublar\.claude\worktrees\herd-leader-sample`, branch `herd-leader-sample` (from `1.20.1-ai` at 4244f62).
- **Nothing is committed.** The lead's rule is "no commits". Everything below exists only as uncommitted files in this worktree. Never create a fresh worktree for this work (it would start without these changes), never `git stash`, `git reset` or `git checkout --` here, and keep the worktree when a session closes.
- Code edits need the lead's explicit say-so. Design answers go into docs only.
- Process ledger (every decision, ruling and review result, in order): `.superpowers/sdd/2026-10-08-herd-movement/progress.md` (git-ignored). Investigation reports sit next to it (see "Reference files").

## Current status (end of 2026-10-08)

1. **First herd design (layered, 17-task plan): implemented, failed two playtests.** Followers cluster and shove, get stuck on trees, walls and narrow gaps, turn randomly, never show a wedge, and do not graze. Root cause: the follower movement model (seek a point plus weak separation, no obstacle avoidance) cannot handle trees or narrow spaces. Tuning does not fix it. Details: `movement-investigation.md`.
2. **Quick fixes after playtest 1:** kept: one turn rate (`Dinosaur.MAX_TURN_PER_TICK = 10`, used by steering), slow down within 3 blocks of the target, the stall rule counts "not moving" instead of "not getting closer", no fallback path within 4 blocks. Reverted: same-herd pushing off (members stacked into one spot).
3. **Redesign:** spec `docs/superpowers/specs/2026-10-08-herd-movement-redesign.md` (with a "Lead decisions" table, Q1-Q8 answered). Plan `docs/superpowers/plans/2026-10-08-herd-movement-redesign.md` (22 tasks, 7,230 lines, not started). **The lead is reading the plan before execution.** Herding is the top priority.
4. **Other fixes done and reviewed (in the worktree, not yet seen in game unless noted):**
   - GameTests: spawns and goals use `FLOOR_Y = 2` (the test framework places the template one block above the origin, so y = 1 was inside the floor). Each test's Javadoc has its run command.
   - Missing-animation log spam stopped: `util/DinoAnimationAvailability` checks once per species whether the model has the animation; one WARN per missing name. Gallimimus lacks dead, getup, eat1, drink, roar; Dilophosaurus and Velociraptor lack getup.
   - Gallimimus base pass uses `<gender>/base.png` (it drew the finished composite under the layers, which doubled the markings).
   - Eyelids layer draws only while `isResting()` (`DinoLayer.visibleWhen`).
   - Male Gallimimus has its own layer set (the lead's new art) and its own layer list, picked per animal by gender (gene value 2.0 = male). Layer textures are cached per type and gender; the duplicate white pass is skipped for never-tinted layers.
   - Earlier plan work (still in place): stand-in unit runner, stamina drains only on self-propelled movement, client tracking range 10, debug-stick colour-layer toggle and herd timer, herd timing.
5. **Unit tests:** 76 run, 0 failed. `:common:compileJava` and `:forge:compileJava` build.

## Next steps (in order)

1. **Done (2026-10-09): the lead answered plan gaps 1-8.** They are recorded in the spec as "Lead decisions on the plan review", numbers 9-16. Gaps 2-4 matched the plan. Gap 1 changed it: travel slots are personal space + 2 apart in open ground (5 for Gallimimus) and + 1 in tight ground (4), and sneak-using the debug stick on a dinosaur switches to 4 everywhere for comparison. The plan is updated: Tasks 8, 10 (side room measured to 5.5), 11, 14 and 21, with test counts +2 (final 147). The new Task 8 code passed when extracted from the plan (14 run, 0 failed). Then a Fable subagent reviewed the plan (2 Critical, 10 Important, 7 Minor) and fixed all 19 findings in the plan and spec. The lead decided rest on a journey (decision 17) and kept the GameTest limits at 1200 travel and 1500 corridor (decision 18). Final check: all plan code compiles against the real classes, 151 unit tests pass, and the per-task counts match. Known risk: `herdPassesCorridor` may need more than 1500 ticks (estimate 900-1100). Waiting for the lead's go-ahead to execute.
2. **Execute the redesign plan** after the lead approves it: subagent-driven (`superpowers:subagent-driven-development`), one implementer per task, a review after each, no commits. Task 22 (hitbox fixes) only with the lead's approval. Herding is broken between Task 1 (removes the old model) and Task 13. Launch the game for the lead after Task 13 (grazing) and Task 15 (travel), and for Task 21 (checklist).
3. **After herding:** texture baking (spec `docs/superpowers/specs/2026-10-08-dino-layer-baking-design.md`, "Lead decisions" table answered; needs a plan), and saving and syncing layer colours (separate task; see "Known issues").

## How to work here

- Unit tests: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh` (Git Bash, worktree root). 3 SLF4J lines on stderr are harmless.
- Build: `./gradlew :forge:compileJava`.
- Game: launch the **headed** client with `./gradlew :forge:runClient` and let the lead test. Never run `runGameTestServer`. The lead runs GameTests one at a time; give the exact command for each, for example `/test run herdgametests.herdkeepspersonalspace` (name = class and method, lowercased). Tell the lead to stay near the arena while a test runs. `runall` is not wanted.
- Per-task diffs without commits: `bash .superpowers/sdd/2026-10-08-herd-movement/snap.sh` prints a git tree id of the whole worktree (temporary index; no commit, no stash). `bash .../pkg.sh BASE_TREE HEAD_TREE OUTFILE` writes a review package. Record the tree ids in the ledger.
- Questions to the lead: always multi-select. If picks conflict, ask when each applies. A dismissed question means re-ask, not proceed.

## Known issues (not fixed)

- **Colour layers cost about 80% of the frame** (26-28 fps with 20 Gallimimus vs 138 fps with layers off). Fix = texture baking (above).
- Layer colours are never saved or synced, so every dinosaur renders untinted. When fixing: `layerColor` picks the colour by list position, not by `basicLayer`, which would put the wrong colours on the male list; the sequencer colour widget lists only the female layer names; tinted male layers still get the second pass. Male tint channels are proposed, not confirmed by the lead: speckle → body-darkness, green → peach, tail → tail-darkness, pattern → brown.
- Other species (T. rex, Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus) still alias their base pass to one gender's finished composite; T. rex, Dilophosaurus and Brachiosaurus very likely double their markings. Not changed (lead decision pending).
- Breeding: `Dinosaur.java:934` `random.nextInt(1,2)` always returns 1, so bred offspring are never male; line 931 gives the child the mother's `DinoData` object.
- The 9 Gallimimus deaths in playtest 2 were not investigated (lead's choice).
- From earlier testing (also in memory): the mod crashes on a dedicated server; the Fabric build fails; the debug-stick colour toggle failed in the baseline session but worked in playtest 1 (Task 17).
- The old male texture files that the male set no longer uses (body-darkness, brown, peach, tail-darkness, tail-stripes, gallimimus.png in `gallimimus/male/`) are still on disk; `SimpleGeoLayerContainer` is unused.

## Reference files

All under `.superpowers/sdd/2026-10-08-herd-movement/` unless a path is given:
- `progress.md`: the ledger (decisions, rulings, review results, tree ids).
- `movement-investigation.md`: why the first design failed (with arithmetic).
- `render-investigation.md`, `skin-investigation.md`, `animation-investigation.md`, `gametest-investigation.md`.
- Reports per change: `quickfix-report.md`, `renderfix-report.md`, `testfloor-report.md`, `animspam-report.md`, `markings-report.md`, `eyelids-report.md`, `malelayers-report.md`.
- Specs and plans: `docs/superpowers/specs/` and `docs/superpowers/plans/` (the 2026-10-08 herd plan has the Task 6 baseline at its end).
