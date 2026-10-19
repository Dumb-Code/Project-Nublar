# Handoff: Beta plan reviewed, implementation starts at M0.P0 (2026-10-19)

Read this first, then the engineering backlog. Supersedes `2026-10-10-herd-p13-p12c-handoff.md` for everything except the herd ledger pointers, which still apply inside M0.P0.

## Where things are

- **Main checkout**, branch `kash's-slop` (based on `1.20.1`). All work goes on this branch. The old worktree `.claude/worktrees/herd-leader-sample` is a frozen backup. Never edit it and never take files from it.
- **Plan state:** commit `ffdd3aa` holds the reviewed Beta plan. Nothing is implemented. No milestone or work package is started.
- **The plan pages:**
  - `docs/project-plan.md`: the roadmap, milestone table and "Waiting on you".
  - `docs/plan/engineering-backlog.md`: the master backlog. Global constraints, cross-cutting risks, "How to use this plan", one section per work package, the recipe table, the Decisions register (D01 to D273) and the change log.
  - `docs/plan/feature-specs.md`: each feature in player terms. The spec wins over the backlog where they differ.
  - `docs/plan/art-list.md`: the only home of art status. 995 counted rows, 431 missing or placeholder.
  - `docs/codebase-analysis.md`: the codebase analysis. Section 11 is the lead-confirmed design and wins over the plan. Section 12.2 holds the optimization-mod duties.
- **Herd ledger** (for M0.P0): `.superpowers/sdd/2026-10-08-herd-movement-redesign/progress.md`. The herd code is in commit `582fbb0`. Commit `4e90174` is docs only.
- **Build today (1.20.1):** `./gradlew :common:compileJava`, `sh tools/junit-standin/run.sh` (last green: 353 tests, before P12c), `./gradlew :forge:compileJava`. Game: `./gradlew :forge:runClient`, headed. The lead tests in the client. Never run a headless GameTest server for a user test.

## What the review settled on 2026-10-19

All answers are in the Decisions register, rows D258 to D273, and each spec states the current rule. The short list:

- **M1:** gate split into "100 % tube to hatched baby, about 50 minutes" and "first fossil to 100 % tube". No recipe tick cap: progress syncs as a per-mille fraction. Order: F1.10 and F1.1 first, then F1.11, F1.3 and F1.4, then F1.2 and F1.5, then F1.6 and F1.9, then F1.7 and F1.8, then F1.12. F1.10 tests tabs with a test network provider. F1.11 tests with GameTest-only consumers until F3.2 adds posts. The pouch takes amber by hand and empties into the Processor on use.
- **M2:** stress meter with no drift: +2 per active cause per 1,200-tick step, −5 per step with no cause, hysteresis 10. Only a lone adult gets the alone cause (D271). Need rates are config defaults (D258 to D261). Stress Tolerance is a registered gene. There is no vision gene. F2.5 part A owns herd-meal travel and the new herd water trip (D273). F2.4 owns the feeder spot queue. The sleep cooldown follows damage only, and a full sleep is 6,000 ticks (D272). No warning skip radius. The spec's per-species clip lists are the master.
- **M3 to Release 1:** pylon pole is a plain block stacked by hand (D266). Vehicle stages use existing model groups: body, 4 wheels, seats, Tour Computer (D267). R03 holds the Fish Feeder and the aquatic enclosure rule for the Mosasaurus (D264).
- **M0:** networking on vanilla custom payloads, one registration class per loader. No Architectury and no Common Networking (D263). M0.P0 builds and tests P12c, and the lead decides keep or revert (D269). M0.P1 writes `docs/config-reference.md` and `docs/performance-baseline.md`. Paddock tick gates are 25 ms on the reference setup.

## What is still open

- **Blocked by the lead's choice:** D08 (guns), D09 (licence), D14 (animation speed by body size). Do not open them again unprompted.
- **Waits on the artist:** D91 (real T. rex sounds, `hurt_1`) and Art list questions 4, 5, 7, 9 to 14, 19 and 20. The roadmap's "For the artist" lists them.
- **Playtest 6** results were never reported. M0.P0 asks the lead for them before the herd acceptance.

## How to start: M0.P0 Branch integration

Follow "How to use this plan" in the backlog, steps 1 to 7. In short:

1. Read M0.P0 in the backlog, section 2.6 of the codebase analysis, and the herd ledger.
2. Write the task plan first: `docs/superpowers/plans/2026-MM-DD-m0.p0-branch-integration.md` in the `superpowers:writing-plans` format. File list, interfaces, bite-sized TDD tasks with real test code. No placeholders.
3. Send the task plan to the lead and wait for the review.
4. Implement on branch `m0.p0/branch-integration` from `kash's-slop` with `superpowers:subagent-driven-development` or `superpowers:executing-plans`. One commit per task.
5. Acceptance is in the backlog. Both loaders build, the Forge client runs the machine chain, the fence and a Gallimimus herd, and the Fabric client reaches the title screen.

M0.P0 steps that are easy to miss:

- Create `integration/1.20.1` from `1.20.1-ai` (4244f62). Line numbers in the backlog refer to that commit and go stale after M0.
- Bring the herd work in from `582fbb0` only.
- Build P12c (`GroundSupport`, `BodyFit`, `FenceAwareNodeEvaluator` changes), run `GroundSupportTest` and the Forge `HerdGameTests`, record the results, and ask the lead to keep or revert. Until then the herd baseline is the P13 state.
- Merge `rft/adversing` with `git merge -X find-renames=30%` and resolve the 13 shared files.
- Port the fossil design from `fossils-1.20.1` by copy, not by merge. Fix the quality roll and the rarity test with unit tests.
- Do not merge `fossils-1.20.1` itself.

## Rules that hold for every package

- Spec over code. Work-in-progress code proves nothing. A requirement is met only when its acceptance test passes.
- No stand-ins. A clip, sound or texture from another species counts as missing. A package is not done until each of its Art list rows is Done.
- Every "tune in playtest" number is a config value. Never invent a number: write "not set" and add a register row.
- Game time for every timer. Server-owned animation state. Packets validate on the server.
- Libraries first: SmartBrainLib 2, GeckoLib 5 and Balm hooks before Nublar code.
- Code quality: simple, light, adversarial self-review, and a human can continue from where you stopped (Javadoc, package note, tests that explain intent).
- Design answers go in the docs, never in code or game data, unless the lead asks for code.
- When you find a design gap: build the smallest version behind a config key, write the question and your choice into the PR under "Decisions for the lead", and add a register row.

## Open questions for the lead

- Playtest 6 observations (herd P13a and P13b).
- The keep-or-revert decision on P12c, once M0.P0 has built and tested it.
- Review of the M0.P0 task plan before implementation starts.
