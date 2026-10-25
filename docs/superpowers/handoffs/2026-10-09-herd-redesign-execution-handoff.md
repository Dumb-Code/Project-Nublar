# Handoff: herd redesign execution and playtest fixes (2026-10-09, evening)

Read this first, then the ledger. Supersedes the "Next steps" of `2026-10-09-herd-movement-handoff.md`.

## Where things are

- Worktree `.claude/worktrees/herd-leader-sample`, branch `herd-leader-sample`. **No commits** (lead's rule). Never `git stash`/`reset`/`checkout --`; keep the worktree.
- Ledger (every task, review, ruling, tree id): `.superpowers/sdd/2026-10-08-herd-movement-redesign/progress.md`. Per-task diffs come from tree snapshots: `bash .superpowers/sdd/2026-10-08-herd-movement-redesign/snap.sh` (tree id) and `pkg.sh BASE HEAD OUT [paths]`.
- Fix specs after the plan: `.superpowers/sdd/2026-10-08-herd-movement-redesign/playtest1-fixes.md` (P1-P9). Investigations and reports sit next to it (`playtest1-*`, `playtest2-*`, `p*-report.md`).
- Build: `./gradlew :common:compileJava`, `sh tools/junit-standin/run.sh` (last known: 233 run, 0 failed), `./gradlew :forge:compileJava`. Game: `./gradlew :forge:runClient` (headed; the lead tests; never `runGameTestServer`).

## Done (all reviewed, uncommitted)

1. Plan Tasks 1-20 of `docs/superpowers/plans/2026-10-08-herd-movement-redesign.md`, plus a final whole-branch review and fix wave. Task 21 = lead playtests (below). Task 22 (hitboxes) done only for Gallimimus (lead: do NOT change other species).
2. Playtest 1 fixes: P1 speed model (vanilla ground speed is quadratic in the speed setting; old speeds restored 0.6/0.8/0.8/1.2; walk-animation floor), P2 waiting falls through to the stuck rule, P3 leash restriction cleared on unleash + journey retry + leader-change settle, P4 straggler rule (on-trail followers only) + test limits + low-gap arena (2.8-high trapdoor gap), P5 Gallimimus hitbox 1.2 x 2.6, P6 box-aware path nodes in `FenceAwareNodeEvaluator`.
3. Playtest 2 fixes: P7 debug visuals (stick in OFF hand, main hand empty, use on a dinosaur; legend in `p7-report.md`), P8a box SIDE_MARGIN 0 (box 1.25), held fallback paths re-plan after 20 ticks, P8b make-way for any walker (never the leader), tight-ground hard minimum 1.7, ghosting through mates on fallback paths, P8c dragged/leashed leader becomes an urgent journey (starts at R + 2).
4. Research report: `reports/Multipart entity hitbox systems.md` (notes in `research_notes/`). Answer: multipart parts fix hit accuracy, not pathing; MoreHitboxes 1.20.1 is the best fit if wanted. A task chip was offered to remove the dead T. rex head-part code.

## In progress when this was written

- **P9** (implementer running in the background, not reviewed): herd area 6x the area (radius x sqrt 6: 27.4 for 20), journeys 78-157 blocks, journey targets only in loaded/entity-ticking chunks, GameTests override their herd's area scale to 1.0. Next: snapshot, review (`p9-report.md`), fix round if needed, build, relaunch.
- **Playtest 3** client was launched before P9 (the lead was testing P7/P8). No results recorded yet.

## Open questions for the lead

1. A fight chase that leaves the herd area counts as an urgent trip (decision 16 wording): keep?
2. Spec "no two followers share a target arc" is impossible within one row; tests check distinct target points: OK?
3. Walkers no longer slow near walls (FREE_SLOW_DISTANCE 1): keep?
4. Is reading vanilla classes with `javap` from the Gradle-cached mapped jar acceptable (no-reverse-engineering rule)? Some P4/P6 agents did this.
5. Spec text is stale in places (hysteresis 0.15, box margin 0.25, speeds "0.3-0.7 b/t", slow to 0.3, area/journey sizes): update the spec with the lead.
6. The lead asked whether sitting/sleeping animations exist (not answered; the lookup was interrupted).

## Known risks to watch in game

- Worst-case tick when two failing path searches coincide (est. 3-10 ms with the box-aware nodes); next lever is a per-path column cache.
- Rare double side-step; walls met at full speed; leader can still stop at an obstacle it cannot path round (design item: lateral correction).
