# Handoff: playtest 4 fixes (paused 2026-10-10)

Read this first, then the ledger. Supersedes `2026-10-09-herd-redesign-execution-handoff.md`.

## Where things are

- Worktree `.claude/worktrees/herd-leader-sample`, branch `herd-leader-sample`. **No commits** (lead's rule). Never `git stash`/`reset`/`checkout --`; keep the worktree.
- Ledger: `.superpowers/sdd/2026-10-08-herd-movement-redesign/progress.md` (every decision, ruling, tree id). Snapshots: `bash .superpowers/sdd/2026-10-08-herd-movement-redesign/snap.sh`. Review packages: `pkg.sh BASE HEAD OUT common/src forge/src`.
- Build: `./gradlew :common:compileJava`, `sh tools/junit-standin/run.sh`, `./gradlew :forge:compileJava`. Game: `./gradlew :forge:runClient` (headed; the lead tests).

## Done since the last handoff (reviewed)

- **P9**: herd area 3x, then superseded by P11. A leashed leader does not start ROAM journeys.
- **P10**: all dinosaur animations are connected to action states (spec `p10-animations-spec.md`). Calling plays when a ROAM journey starts, sentry plays when the leader is on watch while the herd grazes, and startled plays when a threat comes within 6 blocks. The server reads animation lengths from the mod's JSON.
- **P11**: herd area 6x the original (radius x sqrt 6, 27.4 for 20), journeys 156-314 blocks with far targets walked in loaded legs (`playtest1-fixes.md` P11).
- Playtest 4 (P9 + P10) found: no rest animation, janky catch-up and turning, followers "in sentry", no visible idle, cliff falls. Three investigations: `playtest4-animation-investigation.md`, `playtest4-motion-investigation.md`, `playtest4-cliff-investigation.md`. Key art findings: Gallimimus `rest`/`restidle` are byte copies of `misc.idle`; the file has no sniffground/shakehead.

## In progress (P12, spec `p12-spec.md`)

1. **P12a** (animation smoothing and additive idle sway): implemented (300 run, 0 failed), reviewed **Needs fixes**. Fix round 1 is ruled in the ledger and NOT dispatched yet. It covers: resume-restart idle timer, per-species run authored speed, ground-speed outlier clamp, and a gait test through the switch. Resume the P12a implementer from `p12a-report.md` (a fresh agent with that report path and the findings), then a scoped re-review.
2. **P12b** (catch-up speed matching, eased turning, sit-down stand-still): the implementer was **stopped at the lead's request** after its builds and tests passed, before it wrote `p12b-report.md`. Its code is in the worktree (10 files, tree b09bd0b, base 26acb4e). It is **not reviewed**. Next: have an agent write the report from the diff (or re-run P12b), then review. To undo it instead, restore the paths changed between trees 26acb4e and b09bd0b from tree 26acb4e (list them with `git diff --stat 26acb4e b09bd0b -- common/src forge/src`).
3. **Lead order:** launch playtest 5 after P12a and P12b pass review. **P12c (cliff edges) runs after that playtest.**

## Lead decisions to keep in mind

- Idle blend: additive sway in code (Mowzie's Mobs precedent), all species. Max drop stays 3. A follower stands still while it sits down. Journeys stay with far targets. The first-leg probe at pick time is allowed (it is a controller ruling and the lead has been told).

## Open questions for the lead

- A leashed leader can still pick graze moves that pull against the lead (pre-existing).
- The earlier list in `2026-10-09-herd-redesign-execution-handoff.md` (fight chase as urgent trip, target arcs, walkers near walls, javap use, stale spec text).
