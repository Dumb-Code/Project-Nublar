# Handoff: P13 done, playtest 6 run, P12c unbuilt (paused 2026-10-10)

Read this first, then the ledger. Supersedes `2026-10-10-herd-playtest4-fixes-handoff.md`.

## Where things are

- **Main checkout**, branch `kash's-slop`. All herd work is here as uncommitted files (lead's no-commit rule). The old worktree `.claude/worktrees/herd-leader-sample` is a backup only: never edit it.
- Ledger: `.superpowers/sdd/2026-10-08-herd-movement-redesign/progress.md`. Snapshots: `bash .superpowers/sdd/2026-10-08-herd-movement-redesign/snap.sh`. Review packages: `pkg.sh BASE HEAD OUT common/src forge/src`.
- Build: `./gradlew :common:compileJava`, `sh tools/junit-standin/run.sh` (last green: 353 run, 0 failed, before P12c), `./gradlew :forge:compileJava`. Game: `./gradlew :forge:runClient` (headed; the lead tests). `forge/run` in the main checkout is new (default options, new worlds).
- Lead rules: test on the Gallimimus only. Herds must move and behave properly before genes, stats, hunger, thirst and attack/flee are added; mark features that depend on those as deferred.

## Done this session (all reviewed)

- P12a (animation smoothing, idle sway) + fix round; P12b (catch-up, eased turning, sit-down) + 2 fix rounds.
- Idle weights: misc.idle 6, variants 1 (lead: variants too frequent).
- Playtest 5 -> consolidated list and the lead's rulings: `playtest5-consolidated.md` ("Lead rulings"). Investigation: `playtest5-journey-investigation.md`.
- P13 (`p13-spec.md`): P13a journeys (rest pause, herd during the pause, night, 75% stamina start, slow-walk straggler wait 0.6, leashed leader no graze moves, journey-end logs) + fix round; P13b (no idle variants while travelling, plain idle get-up, walk drain x0.75, softer easing). P13c (fight chase) and P13a item 5 (leader water/food detour) were removed by the lead.
- Design spec updated (lead approved): `docs/superpowers/specs/2026-10-08-herd-movement-redesign.md`, diff `spec-update.diff`. Open: Q6 (other exceptions to decision 16).

## In progress

1. **Playtest 6** (P13a + P13b) ran; the client closed cleanly. **The lead has not reported results yet.** Log: `playtest6-client.log` in the ledger folder (journey ends: lines with `[herd] `; debug: `[herd debug]`). Ask the lead for observations; read the journey-end lines.
2. **P12c (cliff edges + journey target height limit 8)**: the code and tests are written (new `GroundSupport` + `GroundSupportTest`; `BodyFit`, `FenceAwareNodeEvaluator`, `HerdMoveControl`, `HerdSpacing`, `JourneyPolicy`, `LeaderJourney`, tests), base tree a49e01d. **Not built, not tested, no report, not reviewed.** The implementer reached READY_TO_BUILD; its four design choices are in the ledger (P12c READY_TO_BUILD line). Next: an implementer runs the RED plan, the builds and the suite, writes `p12c-report.md`; then a review. Do not build while a playtest client is running.

## Open questions for the lead

- Playtest 6 observations.
- Spec Q6: other exceptions to decision 16 besides fight chases.
- A leashed leader's graze moves were fixed (no graze moves); the earlier list in `2026-10-09-herd-redesign-execution-handoff.md` is otherwise answered in `playtest5-consolidated.md`.
