# Project Nublar: Beta and Release 1 project plan

Date: 2026-10-05, updated 2026-10-10. Status: draft for the project lead's review. Nothing in this plan is started. No phase or module is merged. Herd work exists outside the plan, as the status block below says.

> **For agentic workers:** this file is the master plan. It defines phases, modules, scope and the order of work. Do not implement from this file alone. When you take a module, first write the detailed task plan for that module ("How to use this plan", step 3). Then implement it with `superpowers:subagent-driven-development` or `superpowers:executing-plans`. Steps use checkbox (`- [ ]`) syntax for tracking.

### Status on 2026-10-10

- **The plan is not started.** No phase or module is merged.
- **Herd work exists outside the plan.** The herd and movement redesign is on 1.20.1 as uncommitted files in the main checkout on branch `kash's-slop`. The lead's no-commit rule keeps it uncommitted. The worktree `.claude/worktrees/herd-leader-sample` is a frozen backup. Never edit it.
- **What the herd work holds.** Herd redesign steps P1 to P13 are done and reviewed. There are 29 herd classes in `J/entity/ai/behaviour/social/` (entity survey, fact 49). Step P12c (cliff edges) is written but not built, tested or reviewed. Playtest 6 ran. The lead has not reported its results. Handoff: `docs/superpowers/handoffs/2026-10-10-herd-p13-p12c-handoff.md`. Ledger: `.superpowers/sdd/2026-10-08-herd-movement-redesign/progress.md`.
- **Inputs folded in on 2026-10-10.** This update adds the findings of these documents:
  - [Reference mod survey for Beta](../reports/Reference%20mod%20survey%20for%20Beta.md) ("entity survey"): new items 1 to 32, specifications S1 to S27, open items O1 to O6.
  - [Optimization and tech mod survey for Beta](../reports/Optimization%20and%20tech%20mod%20survey%20for%20Beta.md) ("tech survey"): new items 1 to 38, specifications T1 to T15, open items O7 to O21 and 20 defects.
  - [Dinosaur hitboxes and movement design](dinosaur-hitboxes-and-movement-design.md) ("hitbox design").
  - [Herd movement redesign spec](superpowers/specs/2026-10-08-herd-movement-redesign.md) ("herd spec"), decisions 1 to 33.
  - The summary of both surveys in `docs/codebase-analysis.md` section 12.
- **Survey items are recommendations.** The lead asked for them in the plan. They are plan scope, not lead decisions. Each survey open item is a "decision Dnn default" in the "Decisions register". A default is not a decision.

**In game:** A playable Beta of Project Nublar runs on Fabric and NeoForge for Minecraft 26.2. A Release 1 follows and adds plants and four species. Every module follows the design that the project lead confirmed on 2026-10-05.

**Architecture:** one MultiLoader project with the modules `common`, `fabric` and `neoforge`. Game logic lives in `common`. Each loader module holds only these parts:

- registration glue
- platform services
- datagen
- the client entrypoint

Energy and fluid storage go through Balm, which bridges to the native API of each loader. Species data, fossil sets, diets and gene effects are datapack JSON. Global balance numbers are one TOML file.

**Tech stack:**

- Java 25, Minecraft 26.2
- Fabric Loader and Fabric API, NeoForge
- GeckoLib 5, SmartBrainLib 2, Architectury API, Common Networking
- Forge Config API Port (for TOML on Fabric)
- Balm (energy and fluid storage only)
- Gradle 8 with ModDevGradle and fabric-loom
- JUnit 5 for unit tests, the vanilla GameTest framework for in-game tests

**Spec:** [docs/codebase-analysis.md](codebase-analysis.md). Section 11 is the design that the project lead confirmed. Sections 2 to 6 describe the code as it is. Section 8 lists gaps and the art inventory. Section 12 summarises the two surveys. Read section 11 before every module. If this plan and section 11 differ, section 11 wins and this plan has a bug.

---

## Global constraints

The requirements of every module include these lines.

- Loaders: Fabric and NeoForge. Both must be playable at the Beta release. No single-loader shortcut (section 11.17).
- Game version: Minecraft 26.2. Java 25. Releases move, so check loader and library releases again before the port starts (section 11.17).
- Config split (section 11.17):
  - TOML for global balance numbers (yields, times, chances, power).
  - Datapack JSON for per-species data (stats, fossil sets, diets, gene effects).
  - Tags for the items of other mods.
  - If the split is not clear, pick the simpler format.
- Every number that the project lead marked "tune in playtest" is a config value with the stated default. Never hard-code it.
- Block and item ids can change. No world-compat shims. Fix `carrnivore_feeder_one` and `iron_plant_tank_ugprade` (section 11.17).
- Stats: the Tyrannosaurus card values are approved. The interim values in section 11.16 are the working values for the other five species until real cards arrive. Do not invent other stats. State the basis of every number you add.
- Dinosaurs never spawn naturally. Cloning is the only source (section 11.9).
- Do not open again, unprompted, the decisions that the lead marked "not decided" (guns, licence). Build around them.
- Licence: do not change `LICENSE` or the `license` value in `gradle.properties` until the lead decides (section 11.17).
- Code style:
  - Javadoc on every public type and on every public method that is not an override.
  - No `System.out` or `System.err`. No chat messages for debugging.
  - No `Minecraft.getInstance()` in common or server code.
  - All player-facing text goes through translation keys in `en_us.json`.
- Code quality standards set by the project lead on 2026-10-05. They apply to every module and every review:
  1. Keep the code as simple, well formatted, clean, documented and readable as possible.
  2. Keep the codebase as light as feasibly possible: optimized, efficient, performant, and in line with the basics of OOP. No speculative abstractions. No utility layer for one caller. No new dependency when the JDK or Minecraft already has the function. Measure before you optimize, but never leave known per-tick waste.
  3. Review your own work as an adversary does. Do not add or remove code only for the sake of it. Think carefully before every change.
  4. A human must be able to continue from the point where you stopped. That person must be able to read, change, extend, continue or rewrite the work without you. Javadoc, a short package note and tests that explain intent make this possible.
- Use the libraries that the mod already depends on. If SmartBrainLib 2, GeckoLib 5 or Balm gives a hook for a need, call the library through its API. Do not copy library code into Project Nublar. Do not write a second version of a library feature (section 11.17). Examples:
  - SmartBrainLib 2 ticks the brain. Remove the manual brain tick.
  - Gate sensors with `onlyScanIf` and set a scan rate for each entity. Do not write an always-on block scan.
  - Use the built-in sensors and behaviours for nearby entities, players, hurt-by, attack targets, flee, follow and breeding.
  - Use GeckoLib 5 triggered animations, additive controllers and bone updaters. GeckoLib 5 applies the scale attribute to the model.
  - Write Nublar code only where no hook exists, for example stamina chases, scent trails, dominance, nests and herd formation. Build that code on the base classes of the library. If a library lacks a feature, such as animation LOD in GeckoLib 5, use the extension points of the library. Do not fork the library.
  - Source: [Reference mod survey for Beta.md](../reports/Reference%20mod%20survey%20for%20Beta.md), decided by the project lead on 2026-10-10.
- No internal solution when a library gives the function (lead, 2026-10-06).
- Three rules for the code of the mod itself (tech survey, "Twelve changes" and section 12.2 "Rules for the mod's own code"):
  1. **Still things use static geometry.** Fence posts, wires and idle machines draw from the chunk mesh or a block model. They do not draw from a block-entity renderer every frame.
  2. **One network object runs each power grid.** Posts, pylons and conduits do not tick.
  3. **Work happens only on change.** A block update or a packet goes out only when a value changes.
- Timers use game time, not tick counts. A server that skips ticks of far entities must not slow needs, cooldowns or herd travel (tech survey, rank 3).
- Follow the compatibility duties for each optimization mod in `docs/codebase-analysis.md` section 12.2, "Compatibility duties". The duties apply from the first port build (tech survey, "26.2 availability").
- A compat item ships only when the other mod has a 26.2 build for that loader. The 26.2 availability table is in section 12.2 (tech survey, "26.2 availability").
- Every network packet from the client validates its payload on the server: menu open, slot range, value range.
- Payloads stay under the vanilla limits (tech survey, ON2):
  - 32,767 bytes from a client to the server
  - 1 MiB from the server to a client
- Datagen output in `common/src/generated/resources` is regenerated, never edited by hand. Regenerate it in the same commit as the provider change.

## Cross-cutting risks

The design implies these inputs, but no single module owns them. Each line names the owner module, which must add the test.

1. **A dinosaur from `/summon` or a spawn egg has zero behaviour data.** It starves or divides by zero. Owner: B16. Test: summon each species and check that stats load from species data, not from the egg.
2. **A dedicated server crashes because common code loads client classes.** Causes:
   - `Minecraft.getInstance()` in `IncubatorMenu`.
   - `distmarker` imports in five common files.
   - The Forge entry point loads the client-only `ClientLevel`. On 2026-10-10 the mod crashed on a dedicated server in testing (hitbox design, "Blockers in the worktree today").
   - Common classes use the Forge `@OnlyIn` annotation. Because of this, the Fabric build fails on unchanged code. P1 fixes the Fabric build so that CI can build both loaders. P2 owns the full cleanup of client classes.

   Owner: P2 (port). Test: run the dedicated server through the full machine chain with a GameTest.
3. **A player loses items when a block breaks.** The sequencer drops 3 of 9 slots. The feeder saves nothing. The printer forgets its sensor and chip. Owner: B12. Test: fill every slot, break the block, count the drops.
4. **Worldgen floods a chunk with fossils.** The fossils branch places a vein attempt per chunk per stone, and its rarity test always passes. Owner: B01. Test: a unit test on the rarity math, plus a count of fossil blocks over 100 generated chunks against the config target.
5. **The server never checks a genome edit.** `UpdateEditInfoPacket` has no range check, no menu check and no content check (issue #49). Owner: B07. Test: send a packet with 300% and a packet with a locked gene, and assert that the server rejects both.
6. **Three block-scanning sensors per dinosaur scan about 150k blocks each cycle.** At 100 dinosaurs the cost is about 22 ms per tick. Aligned spikes reach about 440 ms (estimate). The fix (entity survey, rank 2):
   - a feeder registry per level
   - a small water sensor that runs only while the dinosaur is thirsty
   - `onlyScanIf` gates on each sensor
   - a start offset for each sensor, so scans do not align

   Owner: B17. Test: a timing GameTest with 20 animals in one paddock keeps the server tick under 50 ms.
7. **Tick skipping breaks tick-count timers.** Immersive Optimization and similar mods skip whole ticks of far entities. Then far animals recover stamina and lose needs at a fraction of the normal rate. Code that counts `tickCount` causes this (tech survey, defect #10 and rank 3). Owners: B17 (needs and cooldowns), B25 (test matrix), B26 (herd queues and stall thresholds). Test: skip ticks of a far dinosaur and check that its needs and cooldowns follow game time.
8. **Colour layers cost most of each frame.** Each Gallimimus draws its model 23 times per frame (11 colour layers, two passes). With 20 Gallimimus on screen the client runs at 26 to 28 fps (entity survey, C1). Owner: B09. Test: 20 Gallimimus on screen reach the bake spec target of at least 120 fps (entity survey, rank 3).

---

## How to use this plan

This section is for a human developer and for an AI agent. Follow it in sequence.

### 1. Pick work in phase order

Phases run in this sequence: P0, P1, P2, then the Beta modules, then P4, then Release 1. In the Beta phase, follow the module order in "Build order" unless the lead gives a different order. Modules in the same build-order tier are independent. They can run in parallel on separate branches.

### 2. Check the gate of the module

Each module lists `Depends on`. Do not start a module until each dependency is merged into the integration branch. If a dependency is only partly merged, tell the lead and wait. Or pick a module whose dependencies are met.

### 3. Write the detailed task plan first

This file gives scope, not steps. Line numbers in the analysis refer to commit 4244f62. They will be wrong after P0 and P2. Before any code:

- [ ] Read the module entry here and the section 11 sub-sections that it cites.
- [ ] Read the survey specifications that the module cites. The entity survey has S1 to S27. The tech survey has T1 to T15. They are clean-room specifications. Use them as inputs, not as code to copy.
- [ ] Read the current code for every path under `Touches`. Check the paths and fix the wrong ones.
- [ ] Write `docs/superpowers/plans/YYYY-MM-DD-<module-id>-<name>.md` in the `superpowers:writing-plans` format. Include a file list, interfaces, and bite-sized TDD tasks with real test code and real implementation code. No placeholders.
- [ ] Put every config key that the module adds, with its default, in that plan and in `docs/config-reference.md` (P1 creates it).
- [ ] Get a review of the task plan from the lead or a reviewer before implementation. An agent sends it to the lead and waits.

### 4. Implement on a branch

- [ ] Branch name: `<module-id>/<short-name>`, for example `b01/fossil-worldgen`, from the integration branch.
- [ ] One commit per task. Use a conventional prefix (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`).
- [ ] Write the test first for every piece of pure logic (math, data records, NBT round trips, rules). Write the failing test, run it, see it fail, then implement.
- [ ] Run `./gradlew build` for all modules before every push. A compile failure on either loader blocks the push.
- [ ] Run both clients and the dedicated server for every module that touches blocks, entities, screens or packets. Record what you did in the PR.

### 5. Definition of done

A module is done when all of these are true:

- Every item under `In scope` is implemented, or the PR names the item that was cut and the reason.
- Every item under `Acceptance` passes. The PR body lists each one with "pass" or "fail" and the evidence.
- The module compiles and runs on Fabric and NeoForge. The feature was exercised in both clients.
- Unit tests exist for the pure logic of the module and pass in `./gradlew :common:test`.
- No debug output, no TODO without an issue number, no dead code added.
- Config keys are in `docs/config-reference.md` with defaults and the section 11 reference.
- `en_us.json` has every new key. Datagen ran again if a provider changed.
- The PR links this plan and the module id. It names the open decisions that it worked around.

### 6. When you find a design gap

Section 11 does not cover everything. When an implementation choice changes gameplay and section 11 is silent:

- Do not infer the answer from silence or from an unselected option. Both were wrong six times out of eight in the design pass.
- Build the version with the smallest gameplay commitment. Put it behind a config value if a value is involved.
- Write the question, a concrete in-game example and your choice into the PR under "Decisions for the lead". An agent also asks the lead through its question tool, with multi-select enabled.
- Add the question to "Decisions register" at the end of this file.

### 7. Reviewer checklist

A reviewer (a human, or a fresh agent that did not write the code) checks these items:

- [ ] The task plan was followed, or the PR explains each deviation.
- [ ] Both loaders build, and the PR shows evidence of runs on both.
- [ ] Every "tune in playtest" number is a config value.
- [ ] Packets validate on the server.
- [ ] No common-code reference to loader classes or client-only classes.
- [ ] Tests exist for the logic and fail without the implementation. Spot-check this: revert one line and run the tests.
- [ ] Translation keys, datagen and config docs are up to date.
- [ ] Nothing outside the scope of the module changed without a reason in the PR.
- [ ] The four code quality standards hold:
  - the simplest design that meets the acceptance list
  - no dead weight or per-tick waste
  - defects hunted as an outsider does
  - a stranger can continue the work from the code and its docs alone

---

## Phases

| Phase | Name | Exit condition |
| --- | --- | --- |
| P0 | Branch integration | `1.20.1-ai` plus `rft/adversing` merged, herd work from `kash's-slop` included, fossil design ported from `fossils-1.20.1`, builds on Forge and Fabric at 1.20.1, machine chain and fences work in game |
| P1 | Repository hygiene and CI | Clean tree, CI builds both loaders on every PR, unit test source set runs through Gradle, GameTests in common, README and metadata are real |
| P2 | Port to Minecraft 26.2 | Fabric and NeoForge modules build and run on 26.2, Botarium replaced, all 1.20.1 features (herd work included) work again on both loaders |
| B | Beta modules | Every Beta module done per its acceptance list |
| P4 | Beta hardening and release | Balance pass, performance pass, licence set, public Beta published |
| R | Release 1 modules | Every Release 1 module done |

---

## Phase P0: branch integration

**Section:** 2.6 · **Size:** L · **Depends on:** nothing. Decision D37 default: P0 waits until the lead commits the herd work (see below).

**In game:** No new gameplay. One 1.20.1 build has the machine chain, fences, fossils and the herd behaviour together. It builds on Forge and Fabric.

The order is ai, then rft, then the port of the fossil design. Do not merge `fossils-1.20.1`.

**Touches:** the whole tree. `J/init/*` becomes `J/registry/*`. `J/entity/ai/**` becomes `J/entity/dinosaur/ai/**`. `J/api/*` splits. The `nublar-gui` module appears.

**Herd work:** the herd and movement work on `kash's-slop` must come into the integration branch. It is 1.20.1 code: 29 herd classes in `J/entity/ai/behaviour/social/`, plus changes to `FenceAwareNodeEvaluator`, `Dinosaur`, the renderers and the Gallimimus config (entity survey, fact 49). The files are uncommitted because of the lead's no-commit rule. Decision D37 default: P0 is blocked until the lead commits the herd work. P0 does not start without it (herd handoff). The herd code ports to 26.2 with the rest of the tree in P2.

**Tasks:**

- [ ] Create the integration branch `integration/1.20.1` from `1.20.1-ai` (4244f62). Remove `hs_err_pid20864.log` in the first commit.
- [ ] Bring in the herd work from `kash's-slop` after the lead commits it (decision D37 default). Never take files from the frozen worktree `.claude/worktrees/herd-leader-sample` (herd handoff).
- [ ] Merge `rft/adversing` with `git merge -X find-renames=30%`. Resolve the 13 shared files. Move the feeder AI classes (`NearestFeederSensor`, `EatFromMeatFeeder`, `WalkToNearestFeeder`) to `entity/dinosaur/ai/{sensor,task}`.
- [ ] Fix the two Javadoc blocks with a raw `->` (`ProjectNublar.java`, `api/fossil/FossilCollection.java`) so that doclint passes.
- [ ] Check the swapped previous and next arguments at the two fence connection construction sites from commit 59d9518. Write a unit test on `Connection` ordering before the fix.
- [ ] Build both loaders with `./gradlew build`. Fix compile errors before any behaviour work.
- [ ] Run the Forge client. Build a fence of 4 posts and power it. Break a wire and repair it with the spool. Break a post. Record the result.
- [ ] Port the fossil design from `fossils-1.20.1` by copy, not by merge:
  - `FossilBlock` with the `Quality` block state, `FossilBase` and `FossilFeature`.
  - The `fossil_filter` and `fossil_height` placement modifiers.
  - The overlay textures and the datagen providers.
  - Keep all six species registered. Keep `BehaviourDataReloadListener` and the diet lookup.
  - Fix the quality roll (missing `break`) and the rarity test (`nextDouble() < 1.0 / chance`) during the port, with unit tests.
  - Use the worldgen `RandomSource`, not `new Random()`. Remove the `System.err.println`.
- [ ] Regenerate `common/src/generated/resources`. Do not merge the generated files of the fossils branch.
- [ ] Build both loaders again. Run the Forge client: a new world has fossil blocks, mining one drops a fossil item, and the Fossil Processor accepts it.
- [ ] Run the Forge client with a herd of Gallimimus. Check that the herd forms, follows the leader and travels as it did on `kash's-slop` (decision D37 default).
- [ ] Open a PR to the GitHub default branch. After the merge, ask the lead to change the default branch to the integration branch.

**Acceptance:**

- `./gradlew build` passes for `common`, `forge`, `fabric` and `nublar-gui`.
- Forge client: a fossil is mined, processed, sequenced, synthesised, printed, incubated and hatched.
- Forge client: a fence powers, hurts, breaks and is repaired.
- Forge client: a Gallimimus herd behaves as it did on `kash's-slop` before the merge.
- The Fabric client at least starts to the title screen. Fabric parity is the job of P2. P0 must not make Fabric worse than it is.
- No file from `hs_err_*`, `forge/dataRun/**`, `G/.cache/**` or `.ai/` is in the tree.

---

## Phase P1: repository hygiene and CI

**Section:** 3.5, 8.4 · **Size:** S · **Depends on:** P0.

**In game:** No new gameplay. Each change builds on both loaders in CI. A new contributor can build and run the mod from the README alone.

**Tasks:**

- [ ] Remove `shapes/*.txt`, `J/mixin/ExampleMixin.java` (if rft did not), the `com.example` service files and the zero-byte `.ai/mcp/mcp.json`. Add `hs_err_*.log`, `*/dataRun/` and `*/.cache/` to `.gitignore`.
- [ ] Replace `README.md`. It says:
  - what the mod is
  - the Beta scope in one paragraph, with a link to section 11
  - how to build, and how to run each loader
  - how to contribute, with links to this plan and the "How to use this plan" section
- [ ] Fix the metadata:
  - `mod_author`, `description`, `credits`, `pack_format`
  - placeholder URLs in `fabric.mod.json`, and the missing logo
  - dependency declarations in `mods.toml` and `fabric.mod.json` (GeckoLib, SmartBrainLib, Architectury, energy library, Common Networking) with the right sides
- [ ] Remove `toml4j` from `common/build.gradle` and `fabric/build.gradle`. The build declares it and never uses it (tech survey, new item 6 and defect #19).
- [ ] Fix the Fabric build failure. Common classes use the Forge `@OnlyIn` annotation, so the Fabric build fails on unchanged code. Remove `@OnlyIn` from common classes so that CI can build both loaders (herd handoff, hitbox design "Blockers in the worktree today"). P2 owns the full cleanup of client classes (cross-cutting risk 2).
- [ ] Add a `common` unit test source set: JUnit 5 on `./gradlew :common:test`.
  - Add real JUnit 5 test dependencies to Gradle. Today Gradle has no test dependencies (entity survey, fact 50).
  - Move the 371 existing unit tests into the source set. Remove the stand-in runner `tools/junit-standin/run.sh` when all 371 tests run through Gradle (entity survey, fact 50).
  - Add these first new tests: `Quality` ordering and the fossil rarity math from P0.
  - The source set must compile without a running game (pure Java only). Put tests that need Minecraft in GameTests, not here.
- [ ] Move the GameTests out of the Forge module into `common`. Add one small registration shim per loader (hitbox design, "Tests" decision. Entity survey, new item 30 and spec S24).
- [ ] Add `.github/workflows/build.yml`. On every PR and push, it installs the Java toolchain, runs `./gradlew build :common:test`, and uploads the two jars as artifacts. Cache Gradle.
- [ ] Add `.github/PULL_REQUEST_TEMPLATE.md` with the "Definition of done" checklist from this plan.
- [ ] Add `docs/config-reference.md` with the table header `Key | Default | Type | Section 11 ref | Added by module`. B00 fills it.
- [ ] Do not touch `LICENSE` or `license=` (open decision).

**Acceptance:**

- CI is green on the integration branch for both loaders.
- `git status` is clean after a build.
- The README answers "how do I run this" without a look at the Gradle files.
- `./gradlew :common:test` runs the 371 moved unit tests and they pass. `tools/junit-standin/run.sh` is gone (entity survey, fact 50).
- No build file declares `toml4j` (tech survey, defect #19).
- The GameTests compile from `common` and run on each loader through its shim (hitbox design, "Tests" decision).

---

## Phase P2: port to Minecraft 26.2

**Section:** 11.17 · **Size:** XL · **Depends on:** P1 · **Blocks:** every Beta module.

**In game:** No new gameplay. Everything that worked on 1.20.1 works on 26.2 on Fabric and on NeoForge. This includes the machine chain, fences, the six species and the Gallimimus herd. The mod also runs on a dedicated server on both loaders.

Target chosen by the lead: 26.2, Fabric and NeoForge, Java 25, unobfuscated (no Parchment). On 2026-10-05 the lead confirmed that the port comes before every Beta module.

**Before you start, check on that day:** the current stable versions of these parts. Write them into the port task plan with dates and URLs.

- NeoForge for 26.2. Pin the `26.2.x` branch, because the NeoForge default branch is now `26.3.x` (hitbox design, "Cross-loader implementation").
- Fabric Loader, Fabric API
- GeckoLib, SmartBrainLib, Architectury API, Common Networking, Forge Config API Port
- ModDevGradle, fabric-loom, and the MultiLoader Template branch for 26.2

Check the 26.2 availability of every library again on that day. On 2026-10-10 the tech survey found these 26.2 builds (section 12.2, "26.2 availability"):

- Balm 26.2.0.9, GeckoLib 5.5.6, SmartBrainLib 2.0.3
- Architectury, Common Network, Forge Config API Port
- Team Reborn Energy 5.0.0

**Tasks:**

- [ ] Create the branch `p2/port-26.2` from the integration branch.
- [ ] Replace the build. Take the MultiLoader Template branch for 26.2 (`common`, `fabric`, `neoforge`, `buildSrc`). Move the content of the `forge` module into `neoforge`. Keep `nublar-gui` as a source set, the way rft set it. Set `java_version=25` and `minecraft_version=26.2`. Remove the Parchment properties.
- [ ] Replace Botarium with Balm, for energy and fluid storage only (decided by the lead on 2026-10-06). Changed 2026-10-10: the bridge targets were wrong in this bullet (tech survey, facts #4 and #5).
  - The `common` module of Balm gives `EnergyStorage`, `FluidTank`, `BalmEnergyStorageProvider` and `BalmFluidTankProvider`.
  - On NeoForge, Balm 26.2.0.9 bridges to the NeoForge 26.2 `EnergyHandler` and `ResourceHandler` capabilities. It does not bridge to `IEnergyStorage` or `IFluidHandler`. NeoForge marks those for removal (tech survey, new item 1).
  - On Fabric, Balm bridges to Team Reborn Energy only when Team Reborn Energy is installed. Balm does not bundle it. Without it, Fabric cables see no Nublar energy. Fluids go to the Fabric Transfer API (tech survey, fact #5).
  - Record these bridge facts in the port task plan (tech survey, new item 1).
  - This bridge lets the cables of other mods connect on both loaders (section 11.8). Do not write an in-house adapter.
  - Keep Architectury, Common Networking and Forge Config API Port for everything else. The registry, event, network and config layers of Balm stay unused.
  - The 8 users are `Connection`, `BlockEntityElectricFencePole`, `DinosaurFeederBlockEntity`, `EggPrinterBlockEntity`, `GeneratorBlockEntity`, `IncubatorBlockEntity`, `ProcessorBlockEntity` and `SequencerBlockEntity`.
  - The first Balm 26.2 builds for Fabric and NeoForge came on 2026-06-16 (Apache 2.0, Java 25).
- [ ] Check Common Storage Lib (Terrarium, the successor of Botarium) again on the day P2 starts. On 2026-10-06 it supported only 1.21 and 1.21.1 (version 0.0.10, July 2026) and had no 26.x branch. On 2026-10-10 its newest branch was still `1.21.x` (tech survey, "26.2 availability"). If it has a stable 26.2 release by then, compare it with Balm before the Botarium replacement starts. Compare API fit, cable-mod compatibility and maintenance record. Record the comparison in the port task plan. Also check if Architectury merged its branch `feature/fluid_transfer_ext`.
- [ ] Replace every Forge-only API in `common`:
  - the five `distmarker` imports and every `@OnlyIn` that P1 did not remove
  - the `ClientLevel` load in the Forge entry point, which crashes a dedicated server (cross-cutting risk 2)
  - the model data in `BlockEntityElectricFence*Mixin`
  - `GeoMultiBlockItemMixin`
  - `ForgeConfigSpec` usage. Use the spec class of Forge Config API Port on both loaders, or Night Config directly. The port plan chooses.
- [ ] Give Fabric everything that section 3.4 lists as missing:
  - client entrypoint, reload listeners, death message hook
  - spool repair handler
  - the `fossil` and `test_tube` model loaders
  - fence model data, dependency declarations
  - Register reload listeners in `common` through a platform service, so that both loaders share one list.
- [ ] Move the `Minecraft.getInstance()` call of `IncubatorMenu` to the client screen.
- [ ] Upgrade GeckoLib 4.7 to GeckoLib 5 (5.5.x on 2026-10-06. The lead decided on 2026-10-06 to do this inside P2).
  - Every GeckoLib class that the mod imports moves from `software.bernie.geckolib` to `com.geckolib` (31 files).
  - Five classes that the mod uses no longer exist: `AnimationState`, `CoreGeoBone`, `Color`, `EntityModelData`, `RenderProvider`.
  - Rewrite the animation predicates, the bone code, the render layers and the item renderer providers to the GeckoLib 5 API.
  - A dinosaur turns its head and sways as before. Move procedural bone writes (head look, sway) to GeckoLib 5 bone updaters, which run after the controllers (entity survey, new item 2).
  - Clips play at the same speed as before. Use `setAnimationSpeed`, because GeckoLib 5 removed `setAnimationSpeedHandler` (entity survey, new item 3).
  - A baby dinosaur grows at the same visual rate. Remove the manual entity scaling, because GeckoLib 5 applies the scale attribute itself (entity survey, new item 3).
  - Keep the current look and animations. Changes to them belong to B09.
- [ ] Upgrade SmartBrainLib 1.15 to SmartBrainLib 2 (2.0.x on 2026-10-06, same decision).
  - `BrainUtils` no longer exists. Changed 2026-10-10: 30 files use it, not 22 (entity survey, fact 45).
  - `BrainActivityGroup` no longer exists. The dinosaur brain in `Dinosaur.java` is built on it. `ActivityBuilder` replaces it (entity survey, "Library hooks").
  - `SBLLoader` no longer exists. It registers the sensors.
  - Seven classes moved package: `ExtendedBehaviour`, `DelayedBehaviour`, `OneRandomBehaviour`, `FirstApplicableBehaviour`, `PredicateSensor`, `SquareRadius`, `SmartBrainProvider`.
  - A dinosaur thinks once per tick, not twice. SmartBrainLib 2 ticks the brain after `customServerAiStep`. Remove the manual brain tick (entity survey, new item 1).
  - Flip the `PredicateSensor` argument order in the three custom sensors (entity survey, new item 1).
  - Keep the behaviour of the brain as it is. Brain changes belong to B16 to B19.
- [ ] Port the herd code (29 classes in `J/entity/ai/behaviour/social/`) with the rest of the tree (hitbox design, "Cross-loader implementation"):
  - Rewrite `FenceAwareNodeEvaluator` for `PathType` and `PathfindingContext`, which replace `BlockPathTypes`. Keep the evaluator override on both loaders, because the Fabric `LandPathTypeRegistry` has no mob parameter.
  - A dinosaur steps up one block as before. Use the step height attribute in place of `setMaxUpStep(1.0F)`.
  - Keep `BodyFit` the one herd block reader, so that the port changes one place.
- [ ] Machines render correctly at the screen edge on 26.x. Move every block-entity render box to its renderer, because 26.x keeps the box on the renderer (tech survey, new item 2 and defect #18).
  - NeoForge: `getRenderBoundingBox` on the renderer.
  - Fabric: choose, per renderer, between the EntityCulling interface and `shouldRenderOffScreen`. Record the choice in the port task plan.
- [ ] Port `neighborChanged` to the 26.x `Orientation` signature. Remove the empty override in `ElectricFenceBlock`, which reads a block state and discards it (tech survey, new item 3 and defect #6).
- [ ] A genome change sends less data, and an equal genome is not sent again. Store genome and DNA drive data as data components with a `StreamCodec`. Give `DinoData` an `equals` method (tech survey, new item 4 and defect #5).
- [ ] Add a `"version"` integer to the root of every custom NBT compound (tech survey, new item 5).
- [ ] Port datagen to run on NeoForge and emit the same output. Add a Fabric datagen entrypoint only if the NeoForge datagen cannot make loader-neutral files. Prefer one datagen.
- [ ] Update CI for Java 25 and the two new module names.
- [ ] Update `docs/codebase-analysis.md` section 3.1 (versions table) and section 3.4 (parity list) to the new state. Keep the STE style of that document.

**Acceptance:**

- `./gradlew build` passes for `common`, `fabric` and `neoforge`.
- On the client and the dedicated server of each loader:
  - The full machine chain from fossil to hatched T. rex works.
  - Fences power and repair. The generator powers machines.
  - A GeckoLib dinosaur renders and animates.
- A GameTest `machine_chain_smoke` runs on both loaders in CI and passes. It places a creative generator, the four machines and a creative fossil, and asserts that an egg appears.
- A third-party energy cable mod connects to the generator on each loader. The PR names one mod per loader. On Fabric, the test installs Team Reborn Energy (tech survey, fact #5).
- Each of the six species renders with its layers and plays its walk, idle and attack animations on both loaders.
- The brain does what it did before the port, on both loaders: a T. rex drinks, eats dropped meat and hunts a cow. Use a GameTest, or a manual check recorded in the PR.
- A Gallimimus herd forms, follows its leader and travels on both loaders as it did on 1.20.1.
- `grep -r "software.bernie.geckolib" common neoforge fabric` returns nothing.
- `grep -r "net.minecraftforge" common/src` returns nothing.
- `grep -r "botarium" .` returns nothing.
- Balm is used only from the eight storage block entities. `grep -rl "net.blay09" common/src` lists nothing outside them and the platform service.

---
## Beta modules

Each module below has the same fields, in this order:

- **Section · Size · Depends on:** "Section" cites `docs/codebase-analysis.md`.
- **In game:** what the player sees, hears, does or gets after the module merges.
- **Touches:** current locations as of 4244f62, plus rft's moves. Check them when you write the task plan.
- **In scope**, **Out of scope**, and **Config (defaults)** where a module adds 3 or more keys.
- **Produces:** what later modules rely on. These names are proposals, and the detailed plans check them. When a module merges, its names do not change.
- **Acceptance:** one test per bullet.

Items that cite the entity survey or the tech survey are recommendations from 2026-10-10. They are plan scope, not lead decisions. A "decision Dnn default" applies until the lead answers that register row.

### Build order

| Tier | Modules | Why this tier |
| --- | --- | --- |
| 0 | B00 | Everything reads config and species data |
| 1 | B01, B05, B14, B16 | The survival loop: find fossil, process it, power it, animal has real stats |
| 2 | B02, B03, B06, B07, B11, B12, B15, B26 | Complete the lab and the paddock. Large animals move and herds travel. |
| 3 | B08, B09, B10, B13, B17, B20, B27 | Editing depth, appearance, needs, feeders, recipes, section hitboxes |
| 4 | B04, B18, B19, B21, B22, B24 | Social life, player interaction, world content, vehicles, art |
| 5 | B23 (blocked), B25 | Guns wait on a decision. Cleanup comes last |

Tier 1 makes the first fully playable survival loop. Stop and playtest at the end of tier 1 and at the end of tier 2. Then continue.

B24 (art) starts as soon as P2 lands, in parallel, because B09, B14, B20 and B22 wait on its exports.

The 2026-10-10 survey findings change no tier. One note on B09: the lead ranks the colour-layer bake next after herd work (lead, 2026-10-09). Inference, not from the survey: the bake spec reads only today's layer data, so the bake needs no B07 or B16 output. The detailed plan can therefore schedule the bake part of B09 before tier 3. The rest of B09 stays in tier 3.

---

### B00: Config foundation and species data

**Section:** 11.17, 4.6, 6.4 · **Size:** M · **Depends on:** P2

**In game:** No new gameplay. A server owner changes a balance number in one TOML file, or a species value in a data pack. The change applies on both loaders. `/reload` applies species changes with no restart. A server owner can disable a costly system, for example vehicles. That system then costs nothing per tick, and old worlds still load (tech survey, spec T11). Today the stamina maximum of every dinosaur is 0 because of a config key mismatch (entity survey, fact 51). This module removes both keys, so the mismatch cannot return.

**Touches:** `J/data/*ReloadListener`, `J/api/DinoBehaviourData`, `R/data/projectnublar/config/dinosaurs/*.json`, `R/data/projectnublar/diet/`, new `J/config/`.

**In scope:**

- One TOML file, `config/projectnublar-common.toml`, holds every global balance number. Every key that a later module lists goes here. Sections:
  - `features` (new, see below)
  - `fossils`, `processor`, `sequencer`, `editing`, `incubation`
  - `power`, `fences`, `needs`, `breeding`, `vehicles`
- Forge Config API Port loads the TOML on Fabric. NeoForge's config loads it on NeoForge. `J/config/NublarConfig` wraps both with typed getters.
- A server owner disables a system with one switch (tech survey, new item 7, spec T11). A `NublarFeatures` record reads the `features` section at start-up. The switch list is the decision D29 default. For a disabled feature:
  - Its blocks and items stay registered, so saves still load. They do not show in creative tabs.
  - Its tick paths return at their first line. Its event handlers are not registered. Its sensors are not added.
  - Its recipes, loot additions and advancements do not load.
  - The server's switch values win over the client's.
- Recipes of a disabled feature vanish with it. Add one data-load condition type on both loaders that reads a switch (tech survey, new item 7).
- Large servers can tune worst cases. Put scan intervals and size caps in the server config from day one (tech survey, new item 8).
- Old saves keep loading after a species or gene rename. Add id redirects for renamed species and genes (tech survey, new item 8).
- The join sync is smaller. Send the client only a projection of species data: size, name and variants (tech survey, new item 8).
- A species data record `J/api/dinosaur/SpeciesData` loads from `data/projectnublar/species/<species>.json`. One reload listener, registered on both loaders, loads it. Fields:
  - id, clade
  - stats: `max_health`, `health_regen_ticks_per_half_heart`, `attack_damage`, `defense`, `speed`, `energy`, `hunger`, `thirst`
  - `group_size_min`/`max`, `egg_clutch_min`/`max`, `hatch_success`, `growth_days`
  - `base_height_blocks`, `height_range_min`/`max`, `gender_size_weights`, `incubation_minutes`
  - `diet` tag id, `prey_preferences` (clade to weight)
  - `fossil_set` id, `amber_weight`, `biomes` (biome tag to percentage), `depth_min`/`max`, `period`
  - `variants` (B09 fills it), `stress_threshold` (B17), `flee_or_stand` (B19), `territory_radius` (B19)
  - a tracking range in chunks. Proposal: 8 for Gallimimus, Velociraptor and Dilophosaurus, 10 for Tyrannosaurus and Triceratops, 12 for Brachiosaurus. All six species use 10 today. B16 applies the value (tech survey, ON9 and new item 29).
- An unknown field fails loudly at load.
- The lead named flee or stand only for Gallimimus (flees) and Triceratops (stands). The other four species take the decision D13 default.
- `SpeciesData` replaces `R/data/projectnublar/config/dinosaurs/*_config.json` and `DinoBehaviourData`. Remove `default_stamina` and `default_energy_capacity`. This fixes the key mismatch.
- A load error names the bad file. Today the message prints `{}` instead. Cause: string concatenation with SLF4J placeholders in `J/data/BehaviourDataReloadListener.java:107,112`. The new listener uses the placeholders correctly (tech survey, defect #8).
- Fill the six species files. Tyrannosaurus comes from its card (section 7.8). The other five come from section 11.16's tables. A `"_basis"` field names the source of each block of values.
- Data pack check: a unit test loads every shipped species file through the codec. The test fails if a field is missing.
- `docs/config-reference.md` lists every key added. It also lists these items for server owners (tech survey, new item 9):
  - the tracking ranges
  - the server broadcast property
  - Nublar ids for Immersive Optimization (IO), EntityViewDistance (EVD) and Chloride

**Out of scope:** any gameplay change. Values move with no change, except where section 11.16 says the working values differ.

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `features.vehicles` | true | Decision D29 default (tech survey, spec T11) |
| `features.herd_travel` | true | Decision D29 default (tech survey, spec T11) |
| `features.breeding` | true | Decision D29 default (tech survey, spec T11) |

**Produces:**

- `NublarConfig` typed getters
- `NublarFeatures` switches and the data-load condition type
- `SpeciesData`, `SpeciesRegistry.get(EntityType)` and the client projection of species data
- the id redirect table
- the reload-listener platform service

**Acceptance:**

- `/reload` after an edit to a species JSON changes the max health of a summoned animal, on both loaders.
- An edit to the TOML, then a restart, changes a processor run time, on both loaders.
- Unit test: codec round trip for `SpeciesData`.
- Unit test: every shipped species file parses.
- Unit test: a file with a typo in a key fails to parse.
- `grep -r "DinoBehaviourData" common/src` returns nothing.
- A recipe that carries the data-load condition does not load while its switch is false, on both loaders.
- A save that uses an old species id loads through a redirect.
- A broken species file gives a log message that names the file.

---

### B01: Fossil ore worldgen

**Section:** 11.1, 2.4, 7.10 · **Size:** L · **Depends on:** B00

**In game:** Fossil ore generates in four forms, in stone, sandstone and deepslate. Biome and depth decide the species. Each block has its own quality, and the overlay shows it. The overlay takes its tint from the host stone. Its tint strength, opacity and rotation change from block to block. Worldgen works with no errors when C2ME generates chunks on many threads (tech survey, new item 11).

**Touches:** `J/worldgen/` (ported in P0), `J/block/FossilBlock`, `J/api/fossil/`, biome modifiers in both loader modules, `F/datagen/ModWorldGenProvider` (now `neoforge`).

**In scope:**

- Four forms, each a configured feature: `single_ore`, `small_cluster`, `skeleton_cluster`, `digsite_deposit`. B21's structure places the last one, but the feature belongs here.
- One cluster is one species.
- A skeleton cluster is one animal. Piece presence follows the piece weights of the species' fossil set. The card's "quantity max" is the usual maximum. `fossils.over_max_chance` (default 0.05) lets a piece exceed it.
- Rarity formula from issue #1: ore rarity × period rarity × species share × piece share × quality share. One pure class, `FossilRarity`, holds it. Its unit tests reproduce the section 11.1 example: a pristine skull is about 1 in 200 T. rex ores.
- Biome percentages scale the amount per biome group. A species never generates in an unlisted biome. Biome groups are biome tags under `data/projectnublar/tags/worldgen/biome/`.
- Depth per species comes from `SpeciesData`. T. rex default: y 40 to 75.
- Quality: a cluster rolls a base quality. Each block rolls near it with `fossils.quality_spread`: default 70% base, 15% one tier up, 15% one tier down, clamped. Quality is a block-state property.
- Each quality has its own overlay texture. Fix the shared fragmented/poor art.
- Host stones: stone, sandstone, deepslate. Deepslate features must target deepslate depths.
- The block stores the biome group it generated in, as a block-state property, so the drop can carry it (B02).
- Block layout (decision D31 default): one block per host stone and species. Piece, quality and biome group are block-state properties. The fossil set then has about 4,600 states in total (tech survey, O17).
- Start-up time and memory stay low. Today 910 fossil blocks exist for two species: one block per host stone, quality and piece. Six species would make about 3,000 to 3,600 blocks. Cause: `J/api/FossilCollection.java:35-52`. The D31 layout fixes it (tech survey, defect #17).
- Overlay looks from `Fossil Ore Aesthetics.docx`:
  - tint from the host stone, with a random tint strength from 0 to 90%
  - random opacity from 15 to 100%. Decision D31 default: 4 opacity steps, as weighted model variants
  - random rotation
- Draw the overlay as a baked model with randomness from the block position seed. The look needs no block entity and no block-state property. Looks in the block state would make about 1.5 million states (tech survey, O17).
- A unit test counts the block states of each fossil block and the total of registered fossil blocks (tech survey, new item 10).
- Worldgen is thread safe with C2ME (tech survey, new item 11):
  - Build worldgen lookup maps once on the main thread, as immutable maps. Publish them with one assignment.
  - Use only the `RandomSource` that the generator passes in. Never call `level.getRandom()` or `new Random()`.
  - Place blocks only inside the given region, with no drops and no entity spawns.
  - Put no lazy cache inside a feature config.
  - Keep configured and placed features as data with codecs.
- No data race when worldgen reads fossil maps during a reload or under C2ME. Today placement reads public static mutable `HashMap`s in `J/api/FossilCollection.java:20-22` and `J/ProjectNublar.java:15`. Placement reads the new immutable maps instead. B02 removes the old maps (tech survey, defect #16).
- Fabric `BiomeModifications` and NeoForge biome modifiers both place the features.

**Out of scope:** terracotta host, trace and generic fossils, fossil pouch, drops and items (B02), amber (B03), digsite structures (B21).

**Produces:**

- `FossilBlock` with the properties `QUALITY` and `BIOME_GROUP`. Under the decision D31 default it also has `PIECE`, and one block exists per host stone and species.
- `FossilSet` record (pieces with weight and usual max) in `data/projectnublar/fossil_sets/`
- `FossilRarity` math

**Acceptance:**

- A 100-chunk sample in a listed biome at the right depth gives fossil blocks within 20% of the config target. An unlisted biome gives zero. Use a GameTest, or a debug command that counts and that you remove before merge.
- Unit tests: rarity shares, over-max chance, quality spread distribution.
- Unit test: skeleton piece selection never gives a piece outside the set.
- Unit test: the block-state count per fossil block and the fossil block total match the chosen layout (about 4,600 states under the D31 default).
- A 200-chunk generation run with C2ME on Fabric completes with no crash and no region warning (tech survey, new item 11).
- Visual check on both loaders: tint, opacity and rotation change from block to block, and the overlay shows the quality.

---

### B02: Fossil items, drops and DNA yield

**Section:** 11.1 (quality, DNA yield, pieces), 4.1, 8.5 · **Size:** M · **Depends on:** B01

**In game:** A player mines fossil ore and gets a fossil item. The item knows its species, piece, quality and biome. The Fossil Processor turns it into DNA at the configured yield.

**Touches:** `J/item/FossilItem` (new), `J/api/DNAData`, `J/api/FossilCollection` (remove), `J/api/loot/functions/*`, loot providers, item textures.

**In scope:**

- One `FossilItem` with component data: species, piece, quality, biome group. Tooltips show all four. Quality ladder: fragmented, poor, common, pristine.
- Drops come from loot tables. Remove the `getDrops` override.
  - Fortune gives a chance of one tier up: `fossils.fortune_tier_up_chance` per level.
  - Silk Touch drops the ore block.
- DNA yield per quality in TOML: 5, 7.5, 10 and 30 percent. Special pieces (skull, tooth, claw) multiply it by `fossils.special_piece_yield_multiplier` (decision D01).
- Item art:
  - generic bone sprites per piece, shared across species
  - species sprites for skull, tooth and claw from the asset repository (80 sprites exist)
  - fix the zero-byte `pelvis.png` and the Triceratops copies of T. rex files
- Translation keys for every piece and quality.
- The tag `projectnublar:special_pieces` marks special pieces for B13 recipes and B21 trades.
- Remove the static maps of `FossilCollection` and the `COLLECTIONS.put` return bug. Remove the empty-list `.get()` in `FossilItemFunction`.

**Out of scope:** skeleton exhibit assembly, trace fossils.

**Produces:**

- `FossilItem` data accessors: `species()`, `piece()`, `quality()`, `biomeGroup()`
- `DNAData` gains `biomeGroup` and `source` (FOSSIL, AMBER, SYRINGE, REMAINS, EGG)

**Acceptance:**

- GameTest: over 200 breaks of each quality, with no enchantment, with Fortune III and with Silk Touch, the drops match the expected distribution.
- Unit test: yield for each quality and for a special piece.
- A Fossil Processor run on a pristine T. rex skull gives the configured yield × filter efficiency, on both loaders.

---

### B03: Amber

**Section:** 11.2, 4.2 · **Size:** M · **Depends on:** B01, B05

**In game:** Amber ore generates and drops an amber item that shows no species. The Fossil Processor rolls the species at the end of the run.

**Touches:** `J/block/AmberBlock`, `J/item/AmberItem`, `J/block/entity/ProcessorBlockEntity`, `J/menutypes/ProcessorMenu`.

**In scope:**

- An amber ore block in the three host stones. The B01 feature set places it with its own rarity. The block stores the depth band and the biome group.
- The amber item carries only the depth band and the biome group. Its tooltip says "Unknown species".
- The Fossil Processor accepts amber. Fix the `dna_percentage` tag check.
- At run end, the Fossil Processor rolls the species from the `amber_weight` of every species valid at that depth and biome. It rolls the yield uniformly from 0 to `fossils.amber_max_yield` (default 80). Amber has no quality tiers.
- When no species is valid for that depth and biome, the run gives a tube of the nearest-depth-band species (decision D02).

**Out of scope:** drill extractor, one species per deposit.

**Produces:** `AmberItem`, `SpeciesSelector.rollAmber(depth, biomeGroup, random)`.

**Acceptance:**

- Unit test: the roll distribution with two species.
- GameTest on both loaders: the Fossil Processor processes an amber, and the tube names a species that is valid for its biome.

---

### B04: Living DNA sources

**Section:** 11.2, 11.9 · **Size:** S · **Depends on:** B05, B16

**In game:** The player's own animals supply DNA.

**Touches:** `J/item/SyringeItem`, `J/block/entity/ProcessorBlockEntity`, `J/item/RemainsItem` (from B16).

**In scope:**

- Syringe on a vanilla mob: a random yield from 0 to 100%, a small damage hit (`sequencer.syringe_damage`, default 1.0), no cooldown.
- Syringe on a live dinosaur: the same, with the yield range `sequencer.dino_syringe_min/max` (decision D04).
- Remains items (fresh bone, tissue) drop from carcasses (B16). The Fossil Processor processes them at a high yield (`processor.remains_yield`, decision D04).
- Egg sampling: a syringe on an unincubated or incubated egg gives DNA of the egg's genome. Decision D03 says whether this harms the egg.

**Produces:** the `DNAData.source` values SYRINGE, REMAINS and EGG, which B06 uses.

**Acceptance:**

- Each source makes a tube that the sequencer accepts, on both loaders.
- Unit tests on the yield ranges.

---

### B05: Fossil processor completion

**Section:** 11.4, 4.2, 7.4 · **Size:** M · **Depends on:** B00, B14

**In game:** The Fossil Processor behaves as section 11.4 says. Hoppers and pipes can run it with no player.

**Touches:** `J/block/entity/ProcessorBlockEntity`, `J/menutypes/ProcessorMenu`, `J/item/FilterItem`, Balm's fluid tank and the loader's item storage API.

**In scope:**

- One run always gives one tube. Results keep decimals. `processor.rounding` sets the rounding: NONE, HALF or WHOLE. Default NONE.
- By-products go into the 3×3 output grid: sand, gravel, iron nugget, gold nugget, rarely diamond. Their weights are in the TOML.
- Hopper and pipe support on both loaders. Expose item storage per side (input, filter, tube, output). Use Balm's fluid tank provider and each loader's item storage API. Fluid input accepts water from pipes and buckets.
- Filter efficiency: fix NaN for `dev_filter`.
- Tank tiers of 2, 3, 4 and 8 buckets already match. Keep them.
- No mixed queue, no parallel upgrade.

**Produces:** `ProcessorBlockEntity` side handlers, `ProcessorInput` (fossil, amber, remains).

**Acceptance:**

- A hopper chain feeds fossils and pulls tubes with no player for 10 runs, on both loaders.
- Unit test on the rounding modes.
- Check that the by-product weights add up to the expected sum.

---

### B06: Sequencer and drives

**Section:** 11.5, 4.3 · **Size:** M · **Depends on:** B00, B02

**In game:** Sequencing adds DNA per species and per variant, and it tracks the biome share. The Isolated view works. A drive with 100 species syncs about 2.4 KB instead of 15 to 24 KB (tech survey, new item 12, estimate).

**Touches:** `J/item/DiskStorageItem`, `J/api/DiskStorage`, `J/block/entity/SequencerBlockEntity`, `J/client/screen/SequencerScreen`.

**In scope:**

- Drive data: a map of species-or-variant id to percent (capped at 100), plus the biome-group share per species. No capacity limit. HDD: 10 s per sample. SSD: 5 s.
- Drive data is a compact data component with a `StreamCodec`, not NBT (tech survey, new item 12).
- Size target (decision D32 default): design for 150 entries and test at 1,000. A drive of 10,000 entries would reach the 1 MiB server-to-client payload limit.
- Variants: parrot colours, cat breeds and other mobs with vanilla variant data keep separate entries.
- A broken sequencer drops all 9 slots. Fix `getContainerSize`.
- Fix the NaN from `DiskStorageItem.getGeneCompletion` for genes with no donor (gender).
- Isolated view (section 7.2): per gene, the percent and the animals still needed.
- Remove the "Cretaceous/Apple" placeholder.

**Produces:** `DriveData` record with `percent(id)`, `biomeShare(species)`, `geneCompletion(gene)`.

**Acceptance:**

- Sequencing two parrot colours gives two entries.
- The biome share changes after a tube from a desert fossil.
- Unit tests on addition and on the cap.
- A broken sequencer drops the expected item count, on both loaders.
- A drive with 1,000 entries round-trips through the codec and syncs to the client under the payload limit (decision D32 default).

---

### B07: Gene editing core

**Section:** 11.6 (thresholds, donors, risk, gene effects, gene data), 4.3, 4.6, 7.5, issue #49 · **Size:** XL · **Depends on:** B00, B06, B16

**In game:** The server checks every edit and saves it in the genome. An edit changes the animal in all four gene groups. An edited animal gets its new stats once, and no stat work runs per tick (tech survey, new item 13).

**Touches:** `J/api/DinoData`, `J/api/GeneData`, `J/api/Genes`, `J/registry/GeneInit`, `J/network/c2s/UpdateEditInfoPacket`, `J/client/widget/GeneButton`, `F/datagen/GeneDataProvider` (now `neoforge`), new `J/api/gene/GenomeEffects`, `GenomeRisk`, `EditRules`.

**In scope:**

- Thresholds in TOML: clone at 50, edit at 70, gender sure at 80 with 30% random below. Donor slots at 50, 75 and 100, max 3.
- `DinoData` saves `layerColors`, the base species, the rolled variation (after hatch) and the overrides. `toNBT`, `fromNBT` and `copy` all carry them. Add a round-trip unit test.
- Today layer colours are never saved and never reach the client (entity survey, fact 48). The genome sync must carry them too. P2 moves the genome to a `StreamCodec` component.
- The server checks `UpdateEditInfoPacket` and rejects and logs a bad packet. Checks:
  - the menu is open
  - percentages add up to 100
  - the base is at or above the threshold
  - the donor count fits the slots
  - only an isolated gene takes an override, and the override is within the gene's range
- Gene registry additions: `thirst`, `energy`, `egg_clutch`, `gestation_time`. Keep `immunity`, which acts on Poison in Beta.
- Gene definitions stay a Java registry in Beta. Data pack JSON waits until after Beta (decision D30 default).
- Gene application: one `GenomeEffects` class maps every gene to an attribute or a `SpeciesData` modifier. All four groups must do something you can see:
  - core stats: health, regen, strength, defense, speed, size
  - needs: stomach, eat rate, thirst, energy
  - behaviour: aggression, intelligence, herd/pack size, tamability, nocturnal
  - breeding and survival: fertility, clutch, gestation, immunity, underwater, heat, jump
- Behaviour genes feed the memories that B17 to B19 read.
- `GenomeEffects.apply` is the one rebuild function for derived stats (tech survey, new item 13). It runs at spawn, on a genome change, on a growth step and on a data pack reload. It has a "data not loaded" guard and a version stamp.
- Risk model: a pure `GenomeRisk` class returns three values from donor share and edit count, with TOML coefficients. B08 shows them. B11 applies them.
  - incubation failure chance
  - stat trade-off list
  - abnormal-behaviour chance
- Per-clone variation:
  - stats within `editing.variation_percent`
  - size with the gender weights from `SpeciesData`
  - layer colours
  - albino or melanistic at `editing.morph_chance` (default 0.05)
- Variation rolls at hatch, not at synthesis (decided by the lead on 2026-10-06). An egg item carries no rolled values, so hatching stays a reveal. B11 calls the roll.
- Gene data: regenerate it from `Vanilla Animal DNA Effects.txt`. Section 7.5 lists every difference. Add frog, camel, sniffer, armadillo and wolf variants, because 26.2 has them. Villager DNA randomises every gene per use.
- Fix `GeneButton`, which writes 0 into `advancedGenes` on select.

**Out of scope:** the screen (B08), skins and colour rendering (B09), special genes (B10), disease, gene definitions as data pack JSON (decision D30 default).

**Produces:**

- `DinoData` complete
- `GenomeEffects.apply(Dinosaur)`, the one rebuild function
- `GenomeRisk.evaluate(DinoData)`
- `EditRules` (thresholds, slots), used by B08 and B11

**Acceptance:**

- Unit test: donor slot count per percent.
- Unit test: the gender rule.
- Unit test: the server check rejects out-of-range and non-isolated edits.
- Unit test: risk rises with donor share and never falls.
- Unit test: `DinoData` NBT round trip, colours and base species included.
- Unit test: a second rebuild with an unchanged version stamp changes nothing.
- In game on both loaders: a +30% speed edit changes the movement speed attribute by a measurable amount.
- In game on both loaders: a herd size edit changes the group memory.
- In game on both loaders: a fertility edit changes the clutch roll.
- A GameTest calls the handler directly with bad data, and the server rejects the forged packet.

---

### B08: Edit screen

**Section:** 11.6 (gene control, appearance controls), 7.2 · **Size:** L · **Depends on:** B07, B09

**In game:** The Edit tab of the sequencer matches the 2022 layout, with a Basic and an Advanced view. It shows the risk before synthesis.

**Touches:** `J/client/screen/SequencerScreen`, `nublar-gui` widgets, `J/menutypes/SequencerMenu`.

**In scope:**

- Basic view: entity preview with a player for scale, donor sliders, sequenced-animal list, effect text ("Speed +3%").
- Advanced view:
  - isolated gene controls: a slider for numeric genes, a list for choice genes (gender, skin variant, special gene)
  - per-layer colour with saturation, opacity and tint strength
  - primary/secondary mode as the default colour mode
  - the fixed layers (claws, teeth, nostrils, eyes, mouth) are not listed
- Risk panel: incubation failure chance, stat trade-offs and abnormal behaviour chance, from `GenomeRisk`.
- The skin variant list unlocks at 100%. Below that, it shows the biome-decided variant, read-only.
- Widgets live in `nublar-gui`. Menus contain no `Minecraft.getInstance()`.
- Each colour-wheel screen allocates a new 400 × 400 texture (640 KB of native memory). Cause: a new `DynamicTexture` per widget in `J/client/widget/ColorWheelWidget.java:45,110`. Build the texture once and share it (tech survey, defect #15).

**Out of scope:** machine tabs (B12), disease risk.

**Acceptance:**

- Every control round-trips to the server and back after the screen opens again.
- On both loaders, a reviewer edits speed, gender and a layer colour. The hatched animal shows all three.
- Opening the Edit tab again allocates no new colour-wheel texture.

---

### B09: Appearance, skins and rendering fixes

**Section:** 11.6 (appearance), 6.1, 6.3, 8.5, issues #60 and #63 · **Size:** L · **Depends on:** B07, B16, B24 (skins)

**In game:** Each animal shows its own genome: layers, colours, gender texture, biome skin variant and morphs. Each species renders at its target scale. Today 20 Gallimimus on screen run at 26 to 28 fps with colour layers, and at 138 fps with layers off. After the bake, the target is at least 120 fps (entity survey, new item 4). Far herds animate at lower cost. A Brachiosaurus neck no longer vanishes at the screen edge.

**Touches:** `J/client/renderer/DinosaurRenderer`, `J/client/renderer/dinosaurs/*Renderer`, `J/client/renderer/layer/DinoLayer`, `J/client/CommonClientClass` layer registration, textures.

**In scope:**

- Each entity selects its own layers. Fix `DinosaurRenderer.createLayers`, which builds the layers once from the first entity. Layers read the entity's `DinoData`.
- Layer colours come from `DinoData.layerColors`, with saturation, opacity and tint strength. Fixed layers are never tinted.
- Gender textures for all six species. Import the male skins for Gallimimus and Brachiosaurus from the asset repository. Flag the Dilophosaurus male skin as missing art.
- Skin variants: a data-driven list per species in `SpeciesData.variants` (id, biome group, texture set). Beta ships several variants for at least some species. Art exists for Velociraptor Nublar/Sorna and Triceratops JP3.
- Albino and melanistic morph rendering.
- Render scale: base height from `SpeciesData.base_height_blocks`, divided by the measured model height. Compute it once per species. Targets from section 11.16 (decision D11): Triceratops 3.0 blocks, Brachiosaurus 12.5, others near their current size. The smooth growth scale (B16) multiplies it.
- Dilophosaurus layer cost: collapse the four frill variants into one selected layer. Do not draw a second white pass when the tint is white.
- Colour-layer bake (entity survey, new item 4). Add `docs/superpowers/specs/2026-10-08-dino-layer-baking-design.md` to the module scope. Each genome's colour layers bake into one texture, so each dinosaur draws one model pass. Today each Gallimimus draws its model 23 times per frame: 11 layers, two passes each.
- Until the bake lands, cache the layer colour and the layer index per entity. Remove the per-frame `Stream` and `Color` allocations, about 40 per dinosaur per frame (entity survey, new item 5).
- Far animals cost less to animate (entity survey, new item 6). Use GeckoLib 5 extension points, because GeckoLib 5 has no LOD.
  - Hold the last pose beyond 32 blocks.
  - Skip animation frames by distance and body size. Skip count = distance ÷ 20. For bodies over 2 blocks, divide it by half the largest box side.
  - Use two animation controllers at most.
  - Expected effect: 50 to 75% fewer keyframe evaluations for herds at 32 to 96 blocks (estimate).
  - Risk: this relies on an override of `applyAnimationControllers`, a default method of an internal GeckoLib interface. If GeckoLib does not support the override, pause far controllers instead. That fallback saves less.
- Render types stay batchable and shader-safe (tech survey, new item 14):
  - One `RenderType` per texture, in a static field or a cache. Never make one per frame or per entity.
  - No core shader. No `sortOnUpload` on an opaque render type.
  - Name glow layers without the `_e` suffix, for Entity Texture Features (ETF).
- Wire the running animation (`RUN_ANIM`). Read flinch lengths per species from the animation file. Do not hard-code them.
- Culling box: B16 owns it (posed, scaled body, under 204,800 blocks³ for Sodium). B09 renders inside it. Changed 2026-10-10: the box moves from B09 to B16 (tech survey, defect #11 and rank 4).

**Out of scope:** missing animation art (B24), hitboxes (B16).

**Produces:**

- `DinosaurRenderer` that reads per-entity data
- `SkinVariant` record
- the layer bake cache from the bake spec

**Acceptance:**

- Two T. rex of different genders and colours stand side by side and render differently, on both loaders.
- A Dilophosaurus herd of 10 keeps client FPS above a baseline recorded in the PR.
- A Triceratops measures about 3 blocks at full growth.
- 20 Gallimimus in view, all baked, reach at least 120 fps. Record the layers-off figure (138 fps today) in the same PR (entity survey, new item 4).
- A dinosaur beyond 32 blocks holds its last pose (entity survey, new item 6).
- With the B16 culling box, the renderer draws a Brachiosaurus neck when its body leaves the screen edge (tech survey, rank 4).

---

### B10: Special genes, diet genes and quirks

**Section:** 11.6 (special genes and extras), 7.6 · **Size:** L · **Depends on:** B07, B17

**In game:** All seven special genes and the extras exist and do something. A command gives a gene when its donor animal does not exist in the game. A glowing animal causes no chunk rebuilds (tech survey, new item 15).

**Touches:** `J/registry/GeneInit`, new `J/api/gene/special/*`, `J/entity/dinosaur/ai/**`, a new command class, creative tab.

**In scope:**

- Special genes are a choice gene with one slot:
  - poison: a bite or spit applies Poison
  - gender swapping: in a single-gender group, one animal changes gender over `editing.gender_swap_days` (default 10)
  - lysine contingency: the animal needs a lysine item, or it weakens and dies. B13 adds the item and its recipe
  - camouflage: players cannot easily see the animal beyond a range while it stands still. A data list names the compatible species
  - bioluminescence: an emissive layer
  - hyper intelligence: opens doors, takes items from chests, detects unpowered fences, +15% detection
  - thermal detection: detects players through walls within a range
- Bioluminescence is an emissive layer only, never a moving light source. Dynamic-light mods rebuild up to 8 chunk sections for each 0.1-block move of a light source (tech survey, new item 15).
- Donors:
  - poison: cave spider, bee
  - gender swapping: vanilla frog
  - bioluminescence: glow squid
  - hyper intelligence: camel, parrot, wolf, fox, mule
  - thermal detection: salmon, squid, glow squid, frog
- Cuttlefish, reed frog, tree frog and pit viper do not exist. Until they exist, `/projectnublar gene grant <player> <gene>` and a creative-tab item give their genes.
- Diet genes: grass eating and mushroom eating add blocks to the animal's diet.
- Quirks: clucking, sound imitation, cocoa allergy, item carrying.
- Donor palette shifts: a donor's palette moves layer colours by a data-defined hue shift.
- The immunity gene reduces Poison duration.

**Out of scope:** hybrids, disease.

**Acceptance:**

- Each special gene has a GameTest or a scripted manual check listed in the PR, on both loaders.
- The command grants a gene that a survival player cannot otherwise get.
- A bioluminescent animal that walks in view causes no chunk-section rebuilds (tech survey, new item 15).

---
### B11: Synthesis, egg printer, incubator and hatch

**Section:** 11.7, 4.4, 7.4 · **Size:** L · **Depends on:** B07, B14

**In game:** The player turns a genome into a hatchling, as section 11.7 describes. The Sequencer makes a dinosaur embryo. The Egg Printer prints an egg. The Incubator holds and warms at most 9 eggs. A placed egg cracks in stages, wobbles, and a baby steps from the shell at the start of growth.

**Touches:** `J/block/entity/SequencerBlockEntity` (synthesis), `J/block/entity/EggPrinterBlockEntity`, `J/block/entity/IncubatorBlockEntity`, `J/block/IncubatorBlock`, `J/item/IncubatedEggItem`, `J/recipe/UnincubatedEggRecipe` (remove), `J/registry/ItemInit` (container upgrades), `J/network/c2s/UpdateIncubator*Packet`.

**In scope:**

- Synthesis takes a bird-embryo syringe and gives a dinosaur-embryo syringe that carries `DinoData`. Remove `UnincubatedEggRecipe`. The Egg Printer takes the embryo syringe plus Bone Meal and gives the unincubated egg.
- The Egg Printer saves its sensor and chip. The 10% crack chance without the Leveling Sensor stays.
- Incubator:
  - Capacity 3, 6 or 9 eggs. Fix the `ContainerUpgradeItem` registration.
  - It needs power, a bulb and plant matter. It stalls without any one of them.
  - An egg that stays stalled for `incubation.stall_death_minutes` dies.
  - Plant matter capacities are 100, 150 and 200, per the spec.
  - The player places each egg. The arm turns the eggs.
- Incubation failure comes from `GenomeRisk`. Roll it at completion, apart from hatch success.
- Incubation time per species comes from `SpeciesData.incubation_minutes`, scaled by the bulb.
- Hatch:
  - The player places the incubated egg. It wobbles over the last `incubation.wobble_seconds`.
  - Hatch success comes from `SpeciesData.hatch_success` (T. rex 5%).
  - A failure leaves a broken egg item and remains.
  - Egg block against egg entity is decision D05.
- If D05 keeps its default (egg block), build the placed egg as follows (entity survey, B11.1, B11.2, spec S1):
  - The egg is a block with a block entity. It stores `hatchGameTime` and a copy of the `DinoData`. It has no ticker.
  - Scheduled block ticks raise a `STAGE` property through 3 crack stages over the wobble time. The client plays the wobble clip from stage 1.
  - On chunk load, an egg hatches at once if the game time is past its `hatchGameTime`.
  - Dinosaurs pass through egg blocks. Players collide with them.
- A baby that hatches in a wall suffocates. Before the hatch, test free space for the baby box. If a block fills the space, retry after 100 ticks (5 s). Show an action-bar message to players within 8 blocks (entity survey, B11.3, spec S1).
- The hatch beat: the baby appears at growth 0 with shell particles, a crack sound and a hatch sound (entity survey, B11.4, spec S1). B16 owns the hatchling rules.
- The hatch rolls the per-clone variation (B07) and stores the result on the animal.
- A hatchling gets no owner. Taming stays the only bond (B19). This is the decision D17 default (entity survey, O3).
- Egg tooltip (decided by the lead on 2026-10-06):
  - The unincubated and the incubated egg show species, donor mix, the player's edits, the incubation-failure chance from `GenomeRisk` and the chosen gender.
  - The gender line shows "may be random" below 80%.
  - The tooltip shows no rolled traits.
  - All lines use translation keys.
  - Cache the parsed genome, so that a hovered tooltip does not parse it every frame.
- A hatched dinosaur starts at the beginning of growth (B16's growth fraction 0). Today `IncubatedEggItem` spawns an adult and never calls `setBaby`, so growth never starts.
- Sequencer water tiers follow the spec: 1, 1.5, 2, 2.5 and 3 buckets, with 500 mB fill steps.
- One dinosaur takes about 50 minutes of machine time at base tier. Check this by timing and record it in the PR.
- The Sequencer parses `DNAData` NBT twice per tick, and the Incubator and the Egg Printer each use one item per tick with a block update. Parse on a slot change. Refill on events (entity survey, hot path S9).
- `UpdateIncubatorSlotPacket` checks index and position. Remove `UpdateIncubatorPacket` or use it.

**Out of scope:** a bedding block that speeds a natural hatch (B18 nests, deferred). Light that keeps an egg warm outside an Incubator (dropped). A failed run that spawns a hostile mob (dropped) (entity survey, B11.7 to B11.9).

**Produces:** `EmbryoSyringeItem`, `UnincubatedEggItem` and `IncubatedEggItem`, each with `DinoData`. The hatching egg block or entity.

**Acceptance:**

- GameTest on both loaders: a creative setup prints and incubates three eggs, one per capacity tier.
- A stalled egg dies.
- A placed egg hatches a dinosaur whose genome matches the edit and whose growth fraction is 0.
- Two eggs printed from one genome hatch animals with different rolled values.
- The egg tooltip shows the incubation-failure chance and no rolled trait.
- An egg placed in a space too small for the baby does not hatch. It shows the message and hatches after the space is clear (entity survey, B11.3).
- An egg hatches when its chunk loads, if its hatch time passed while the chunk was not loaded (entity survey, spec S1).
- Unit tests on the hatch roll and the failure roll.

---

### B12: Machine framework

**Section:** 11.8, 7.1, 7.4 · **Size:** L · **Depends on:** B00, B14

**In game:** The player builds each of the four machines from parts, in order. Upgrades show on the model. Linked machines share screen tabs. A machine with a problem says why, smokes and goes dark. A broken machine drops every item.

**Touches:** `J/block/{Processor,Sequencer,EggPrinter,Incubator}Block`, their block entities, `J/block/api/multiblock/*`, `J/block/api/MultiEntityBlock`, GeckoLib block models, `nublar-gui` tab widget.

**In scope:**

- Part-by-part assembly, the same as the Incubator's existing right-click assembly:
  - Fossil Processor: base, body, lid, tanks.
  - Sequencer: base, door, computer, monitor.
  - Egg Printer: base, then needle, platform and body in any order.
  - Parts are items with recipes (B13).
- One master block entity per machine. Use on any part opens the master's screen. Breaking any part drops the master's slots once, and the other parts become part items. A part whose master is missing returns to its unassembled state (entity survey, B12.3, spec S2).
- Parts have no block entity. A part index in the block state gives a fixed offset to the master. Capabilities register on the block and return the master's handler. Parts cost no ticks and no saves (tech survey, B12.14).
- A creative-only item places a fully assembled machine.
- Upgrade visuals: tank, chip and bulb tiers change the model (GeckoLib bone visibility or blockstate). Dye recolours the Sequencer's black sections.
- Faster chips draw more power (`power.chip_multiplier` per tier). No power, no progress. Idle machines draw zero.
- Proposal (tech survey, B12.12): one curve sets the tiers, work ticks = base × M^(−n/m), draw = base × M^(2n/m), with M = `power.chip_multiplier`.
- Cross-machine tabs: a screen shows tabs for every machine linked by adjacency or by a `linked` list in the block entity. A tab switch keeps the screen open. Tab icons exist in the asset repository.
- Item retention: every machine drops every slot on break. One GameTest per machine.
- Machine issues: the screen shows a warning tab with the reason. An Incubator can show "No power" or "No bedding" (tech survey, B12.8, spec T7).
  - One `int` issue mask with the bits NO_POWER, NO_INPUT, OUTPUT_FULL, WRONG_FILTER, NO_BULB, NO_BEDDING, NO_EGG and SPECIES_MISSING.
  - Compute the mask only on a slot change, a power event or a tier change.
  - Send it in one `ContainerData` slot while a menu is open.
- Fault effect: a machine that stops for power puffs smoke (3 puffs over 20 ticks, 1 s) and shows a dark model. It puffs once when it starts again (tech survey, B12.9, spec T7).
- Hover box: a player who looks at a machine, generator or feeder sees a small box near the crosshair. It shows state, progress and fault, for example "Working 42 %" (tech survey, B12.10, spec T6).
  - The box reads synced fields only. It never asks the server.
  - It shows after a 5-tick look.
  - A network summary for generators (supply, draw, post count) comes from a server cache with a 20-tick life.
  - The box is always on. A client config switch hides it and sets its offset. This is the decision D27 default (tech survey, O13).
- Sync (entity survey, B12.1. Tech survey, B12.11):
  - Send progress through `ContainerData` while a menu is open.
  - Send block updates only on a slot or state change. The ACTIVE state flips only on the working edge.
  - Write save data and sync data apart. Coalesce world-visible changes with a short delay.
  - The active look stops 60 ticks (3 s) after work stops.
  - Today each active machine sends about 20 full-NBT packets per second.
- Cache the recipe lookup per input item. Clear the cache on `/reload` (entity survey, B12.2).
- Comparator output from progress or contents, clamped to 15, on machines and feeders. Update it only when the 0 to 15 value changes (entity survey, B12.4. Tech survey, B12.21).
- Rendering:
  - The machine renderer builds its hidden-bone set on a state change and uses the normal frustum test (entity survey, B12.5).
  - The master has one render box over the whole machine. A tall machine then stays visible at the screen edge (tech survey, B12.15, rank 4).
  - An idle lab draws like stone blocks. Idle machines show a static block model. The GeckoLib renderer draws only while active. A block state `ANIMATED` flips only on the work edges (tech survey, B12.13, spec T5).
  - The static idle model applies when the B24 idle-pose exports exist. Until then the GeckoLib renderer draws idle machines, and the PR says so. Today every machine block uses `ENTITYBLOCK_ANIMATED` (tech survey, defect #14).
- Pipes and cables from other mods work with no compat code. Each machine shows items, energy and fluids through the standard capabilities, with per-side rules. Machines accept outside energy (D24 default) (tech survey, rank 11, O10).
- Fix the static shape map. Two threads that ask for a machine shape can cause a `ConcurrentModificationException` or a hang in single player. The cause is a public static `HashMap` filled by `computeIfAbsent` in `getShape` (tech survey, defect #3, `J/block/api/MultiEntityBlock.java:35,83`).

**Out of scope:** factory lanes, a six-face side screen and upgrade cards. A kept-contents cache after a break. A walled multi-block around a controller. A power meter block (D27 default) (tech survey, B12.22 to B12.24, O13).

**Produces:** a `MachineBlockEntity` base with `powerDraw()`, `isIdle()`, `linkedMachines()` and the issue mask. `MachinePartItem`. The hover-box info interface. B12 implements it for machines and generators. B20 implements it for feeders.

**Acceptance:**

- Build each machine from parts in survival on both loaders.
- Break each full machine and get all items back.
- Tabs switch between four linked machines.
- The energy meter falls only while a machine works.
- An Incubator with no bulb shows the NO_BULB line in its warning tab. A machine with no power smokes and shows the dark model (tech survey, spec T7).
- An idle machine sends no block-entity packets. When the B24 idle-pose exports exist, it draws through the static model (tech survey, B12.11, spec T5).
- A hopper or a third-party pipe on any part face reaches the master's inventory (tech survey, B12.14).

---

### B13: Recipes and progression

**Section:** 11.8, 4 (one recipe exists), 8.1 · **Size:** M · **Depends on:** B12, B02

**In game:** The player crafts every block, part, upgrade and tool in survival from vanilla materials. No item comes only from loot or only from trades. Advancements mark each step of the chain.

**Touches:** datagen `ModRecipeProvider`, advancement provider (new), creative tabs in `J/registry/CreativeTabInit`.

**In scope:**

- Recipes for:
  - machine parts, tanks (iron, gold, diamond, netherite), chips, bulbs, filters, HDD and SSD
  - syringes, test tubes, container upgrades, plant tanks, Leveling Sensor
  - generators (B14), conduit and pylons (B14)
  - fence posts and spool (B15), voltage signs (B15), switch block (B15)
  - decorative fence variants (B15) (tech survey, B15.16)
  - feeders (B20), vehicles and track (B22), lysine item (B10).
- Special pieces are optional ingredients that unlock nothing exclusive. Section 11.8: no part is trade-only or loot-only.
- Advancements for the first fossil, first tube, first egg, first hatch and first paddock.
- Creative tabs ordered by chain stage.

**Acceptance:**

- A survival playthrough from a fresh world reaches a hatched animal without creative mode or trades. A reviewer does this once per loader and records the time.
- Unit test: every registered item in the mod's creative tabs has a recipe or a creative-only mark.

---

### B14: Power

**Section:** 11.11 (power), 4.5, 7.7 · **Size:** L · **Depends on:** P2 (Balm energy storage), B00

**In game:** Machines and fences use real power. A Generator burns fuel. Conduits and pylons carry the power. One Basic Generator runs a starter park, as the lead's target says. When fuel ends, a whole fence section goes dark at once. A redstone lamp on a generator can warn of low fuel.

**Touches:** `J/block/entity/GeneratorBlockEntity`, the four machine block entities, new `J/block/ConduitBlock`, `J/block/PylonBlock`, `J/block/entity/BlockEntityElectricFencePole` draw path, `J/block/ElectricFencePostBlock` (ticker).

**In scope:**

- Remove the simulate flag. Machines extract what they draw each tick (`power.machine_draw.<machine>.<tier>`).
- The generator saves its energy.
- Basic Generator: burns any furnace fuel at furnace rate (coal 80 s). Output is `power.basic_output_fe_per_tick`.
- Large Generator: uses fuel `power.large_fuel_multiplier` times longer, with higher output. The Creative Generator stays.
- One constant "energy per fuel tick" sets generator output and burn time (tech survey, B14.10).
- Power Conduit block (the mod's own cable) and pylons with lines:
  - Pylons connect like fence posts, up to `power.pylon_range` blocks.
  - Lines carry power with no loss.
  - Pylon and generator models exist in the asset repository and need conversion (B24).
  - This module may ship with placeholder models if art is late. It must say so.
- One power network object per connected group (tech survey, B14.6 to B14.9, spec T1, rank 2):
  - A placed member joins the network next to it. With several networks next to it, the largest adopts the others.
  - A removed member marks its network invalid. The next tick, a flood fill builds the new networks. Several changes in one tick make one rebuild.
  - A chunk full of posts joins its networks in one batch per tick, so a big park loads with no lag spike.
  - Each member block entity saves its own energy. Nothing saves the network.
  - Each tick, a level-wide manager visits each network once. A network with no change skips its sums.
  - A change of the `powered` flag raises a version counter and sends one event to the network's posts, machines and signs.
  - A heartbeat every 60 ticks (3 s), offset by the network id, checks one random member against the world.
  - A flicker guard holds a network unpowered for 100 ticks after 4 flips in 100 ticks. This stops an outage loop.
  - Store per-level networks in level attachments or `SavedData`. Never cache a `Level` in a static field (tech survey, OM5, OM6).
- Fence posts, pylons and conduits run no tick. 300 idle posts cost 0 ticks (tech survey, OS1, B14.2). Today every fence post ticks on the client and on the server, every tick, because `getTicker` always returns the ticker (tech survey, defect #2, `J/block/ElectricFencePostBlock.java:300-305`).
- The generator sends block updates only on change. Today it updates every tick, also when idle (entity survey, B14.1).
- Save data and sync data apart, as in B12 (tech survey, rank 6).
- Generator comparator output gives the stored fraction. Update it only when the 0 to 15 value changes (tech survey, B14.11, D27 default).
- Other mods' cables connect through Balm's energy bridge on each loader:
  - On NeoForge, Balm 26.2.0.9 bridges to `EnergyHandler`, not `IEnergyStorage` (tech survey, facts #3, #4).
  - On Fabric, the bridge works only when the player installs Team Reborn Energy. Balm does not bundle it (tech survey, fact #5).
  - Generators give energy to outside cables. Machines accept it. Posts, pylons and conduits accept no outside cables. This is the decision D24 default (tech survey, O10).
- Balance: one Basic Generator runs all four machines at base tier plus about 10 low-security posts. Each post drains 10 FE per tick and holds 350 FE (B15 fence parity F30), so size the generator output to that drain. Write the arithmetic in the task plan and in `docs/config-reference.md`. Make it hold with the defaults.
  - Today each post drains 10 FE per tick and the generator makes 0.2 FE per tick. One generator cannot run one post. The energy-per-fuel constant and the draw values must fix this (entity survey, fact 47).
- Fence draw per post comes from B15's `FencePowerService` (rft), through Balm's energy storage.
- A fence counts only loaded generators. A fence with no loaded supply shows unpowered. The mod adds no chunk tickets. The docs tell players to keep generators near paddocks. This is the decision D21 default (tech survey, O7).
- After fuel ends, a fence stays live on a buffer. The buffer equals the sum of post buffers (350 FE per post today), as a config value. The fence fades over seconds. This is the decision D22 default (tech survey, O8, spec T1).

**Out of scope:** rotational power, wire loss, path search and transformer limits. Exploding generators. A battery block and a power meter block (backlog, D27 default) (tech survey, B14.13, B14.14, B14.16 to B14.19).

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `power.machine_draw.<machine>.<tier>` | Set in the task plan | Balance arithmetic |
| `power.basic_output_fe_per_tick` | Set in the task plan | Balance arithmetic, energy-per-fuel constant |
| `power.large_fuel_multiplier` | Set in the task plan | Balance arithmetic |
| `power.pylon_range` | Set in the task plan | Fence post connection rule |
| `fences.draw.<tier>` | Set in the task plan | Tech survey, spec T1 inputs |

**Produces:** `GeneratorBlockEntity` (basic, large, creative), `ConduitBlock`, `PylonBlock`. The `PowerNetwork` object and its level-wide manager, which fences and machines query for the `powered` flag and its version counter (tech survey, spec T1).

**Acceptance:**

- GameTest on both loaders: one Basic Generator with coal keeps four base-tier machines running and a 10-post fence powered. When the test adds a second paddock, the generator cannot supply both.
- Energy survives a world reload.
- A third-party cable powers a machine.
- A chunk in the middle of a 100-post line unloads and loads again. Afterwards the powered flag matches the supply (tech survey, B14.15).
- A generator with a comparator gives a signal that falls as the fuel ends (tech survey, B14.11).
- 60 idle posts run no tick method (tech survey, OS1).

---

### B15: Fences and containment

**Section:** 11.11 (containment, fence types, damage and access), 2.3, 5.1, 5.2 · **Size:** XL (changed 2026-10-10 from L, for the parity baseline) · **Depends on:** P2, B14, B16

**In game:** The fence plays like the original 1.12.2 fence, on both loaders. Strings of 6 or 8 wires follow any slope between posts. A player breaks one wire at a time and its cut ends hang bent and spark. A live wire shocks players and animals through armor for ½ ♥ (1 HP). Lamps light post by post as power spreads. A Tyrannosaurus tears through an unpowered fence, and any mob can climb an unpowered wire. The Beta design adds only behaviour the original did not have:

- A switch or a redstone signal cuts a section.
- A paddock of hundreds of posts draws like ordinary blocks.

**Touches:** `J/block/api/fence/*` (rft), `J/block/ElectricFencePostBlock`, `J/block/ElectricFenceBlock`, `J/block/api/BlockConnectableBase`, `J/block/api/Connection`, `J/block/api/MathUtils`, fence renderer (`client/renderer/ElectricWireRenderer`, `ElectricFenceRenderer`), the wire spool item (becomes a block item), new Fence Remover and Creative Fence Remover items, a `regenfence` subcommand, a damage type `projectnublar:electric_fence`, new switch block, `J/entity/dinosaur/ai/behavior/BreakFenceBehaviour`.

#### 1.12.2 parity baseline

**Rule (lead, 2026-10-10):** B15 is functionally 1:1 with the original 1.12.2 fence. Each row below is in scope and has a test. Where an earlier Beta decision changed the original, the original wins (see "Decisions superseded by the parity rule"). Additions that change no original behaviour stay. Do not copy the original bugs in "Original bugs not copied".

**Sources:** 1.12.2 code at `Dumb-Code/ProjectNublar` `master` `e525ed7`, the wiki article "Fences", and the 2019 fence demo video. Evidence with file and line: `research_notes/Fence parity 1.12.2/` (`original_1.12.2_fences.md`, `demo_video_2019.md`, `rewrite_fences_ai_and_rft.md`). "ai" is today's branch. "rft" is `rft/adversing`.

| # | Original 1.12.2 behaviour (in game) | Rewrite today | B15 target |
| --- | --- | --- | --- |
| F1 | Two posts. The Low Security post is 3 blocks high with 2 wires per block (6 wires). The High Security post is 8 blocks high with 1 wire per block (8 wires) | Same on ai and rft | Keep |
| F2 | Placing a post needs every block of its column to be replaceable. Breaking any block of the column removes the whole column | Same | Keep |
| F3 | Right-click post A with a Wire Spool, then post B. The wires string between them, and B becomes the next start, so a player chains posts. Only posts of the same type link | Same | Keep |
| F4 | Maximum span 15 blocks (3D). A longer span shows "%sm is too long. The max distance is %sm" | Same rule. The lang key is missing (ai). rft shows literal English | Keep the rule. Add both lang strings |
| F5 | Cost: ceil(distance ÷ 20 × post height) spools, taken from the held stack, then the inventory. Free in creative. Too few shows "Not enough cable. Need a total of %s cable. (You have %s)", and nothing is built | Same formula. Lang key missing | Keep. Add the lang string |
| F6 | Wires run in any direction and incline. Each wire fills air or replaceable blocks on its line with wire blocks. A solid block on the line leaves a gap | Same | Keep |
| F7 | The pole turns to face its wires: the bisector of its first two runs. An empty-hand right-click on the post flips it 180° | Same. rft uses a different angle formula | Keep the original result. Test that rft's formula gives the same facing |
| F8 | Wires are solid to entities, with small boxes along each wire | Same, 8 boxes per wire per block (the original used 16) | Keep. The box count is free if wires stay solid with no gaps |
| F9 | The crosshair selects one wire at a time. Only that wire shows an outline | Ray trace works. The outline code is in comments (ai) or absent (rft) | Restore the single-wire outline |
| F10 | Left-click breaks only the selected wire. The other wires in that block stay. The block goes only when no live wire is left | Partial. Vanilla then removes the whole block (inference, both lines) | Fix |
| F11 | A cut wire hangs bent. Each stub turns up to 30° in a random yaw and pitch, stable per position | ai: invisible. rft: drawn, up to about 26° | Show on both clients, up to 30° |
| F12 | A cut end on a powered run throws spark particles at random intervals. The sparks glow in the dark | Dead code on both lines | Fix. Sparks show at a cut end while the stub still touches a powered post |
| F13 | Repair 1: right-click the top or bottom face of a wire. The nearest broken wire above or below comes back | Works (common code) | Keep |
| F14 | Repair 2: right-click a wire end. The next wire block comes back | Works (common code) | Keep |
| F15 | Repair 3: use a spool on a block face next to a broken wire | Forge only | Both loaders |
| F16 | Repair 4: place the spool as a block in a gap. It copies the wires that cross that cell | Missing. The spool is a plain item | Make the spool a block item. Restore |
| F17 | Each repair uses 1 spool in survival and plays the place sound at pitch ×0.8 | Uses 1 item. No sound | Add the sound |
| F18 | Right-click a wire with an empty hand or a non-spool item. A "1000 VOLT" sign hangs on that wire, tilted with the incline. A second click removes it. The sign saves with the world | Flag saves. Nothing renders it | Render it. The three voltage-sign arts from the asset repository are the sign variants. The task plan picks how a player selects a variant |
| F19 | Break the first end post: its wires stay, unpowered, with bent ends. Break the second: the wires go, with no drops | Works. rft also drops spools for wire left with no post | Keep. rft's spool drops may stay (an improvement, no gameplay loss) |
| F20 | Fence Remover: right-click a wire. The whole line of that wire goes. Posts stay | Missing | Add |
| F21 | Creative Fence Remover: right-click a wire. The whole connected fence network goes, posts included | Missing | Add, creative tab only |
| F22 | The Wire Spool sprite shows how much of the stack the pending span uses, in 12 stages ("Item Animation" in the video) | Missing | Add |
| F23 | A post stores power and passes it to the posts at the other end of its wires, so one source powers a whole connected fence. A wire is live only if every block on its run is intact and one end post holds power | Same rule on both lines | Keep. B14's network object carries it (tech survey, spec T1), with the F30 numbers and the F31 spread |
| F24 | A powered post shows a lit lamp model on its top block and gives light 10 (low) or 15 (high). An unpowered post is dark | No lit model. No light (`getLightBlock` bug) | Fix both. Lamp model from the post art |
| F25 | Touching a live wire: ½ ♥ (1 HP) of electric damage that ignores armor. The entity flies back from the wire (velocity set, ×0.4 in the air, vertical ×0.3). 15 spark particles burst at the contact point. Dinosaurs take it too | Damage and knockback work. The type is THORNS, which is not electric. No sparks | Add the damage type `projectnublar:electric_fence` with the armor-bypass tag and a death message. Add the sparks |
| F26 | An unpowered wire acts as a ladder for every living entity: players, mobs and dinosaurs. A powered wire does not | Code in comments (ai), absent (rft) | Restore for every living entity (D38, answered 2026-10-10) |
| F27 | Mobs path around wires and posts. Wire blocks and posts are always blocked nodes | Path obstacle, but a dinosaur with every need at zero gets open nodes and walks up to them | Always blocked. Remove the open-node exception. Only the F29 Tyrannosaurus walks to a fence |
| F28 | `/projectnublar regenfence` (operator level 2) rebuilds the wire caches | Missing | Add, if the rewrite keeps client wire caches. Otherwise say so in the PR |
| F29 | Only a Tyrannosaurus breaks fences. Each check rolls 50 %. It looks within 10 blocks for a wire block with an unpowered wire, walks to it at its speed + 0.1, and within 1 block breaks it once. The middle block column of each run goes up to 6 blocks high, and the cells on each side up to 4 high, with drops. It ignores live wires | ai: `BreakFenceBehaviour` for a starving carnivore that wants prey outside the enclosure. rft: marks segments broken, then the cascade | Restore the original rule and numbers. Remove the starvation trigger |
| F30 | A post holds 350 FE and accepts 350 FE per transfer. It loses 10 FE per tick. A post with more than 300 FE shares up to 300 FE per tick with the posts at the far end of its runs, emptiest first. A post with no supply goes dark after 35 ticks (1.75 s) | Same numbers on both lines | Keep these numbers. B14 sizes the generator output to them |
| F31 | Power spreads post by post, so lamps light along a fence over several seconds (demo 0:09 to 0:17) | Same sharing code, no light | Keep. The network object stores energy per post and moves at most 300 FE per post per tick along the runs, so the spread stays visible (D39, answered 2026-10-10) |
| F32 | Breaking a wire or a post is instant (no hardness) | Not checked | Keep instant breaking |

**Decisions superseded by the parity rule (lead, 2026-10-10).** These earlier Beta decisions changed the original. The original now wins:

| Earlier decision | Superseded by |
| --- | --- |
| High security hurts more than low security (section 11.11) | F25: ½ ♥ (1 HP) for both posts |
| Each wire has health, and an animal hit removes its attack damage (section 11.11) | F10, F29: a player breaks a wire at once, and only a Tyrannosaurus breaks a fence |
| Every animal tests an unpowered fence, and desperate animals break even a live fence (section 11.11) | F29: only a Tyrannosaurus, only unpowered wires. Other animals can climb an unpowered wire (F26) |
| A large animal can topple a post (section 11.11) | No post falls |
| A shock can deter a stressed animal from its escape (lead, 2026-10-06) | Removed. A shock does damage and knockback only |
| A starving animal attacks fences (lead, 2026-10-06, B17) | Removed. Starvation does damage only |
| Decision D38 default (players only) and D39 default (lamps at once) | F26 and F31 |

**One exception kept: survival recipes.** The 1.12.2 posts, spool and removers had no recipes and came from the creative tab only. B13 keeps survival recipes for posts, the Wire Spool and the Fence Remover, because section 11.8 asks for a survival recipe for every item. The Creative Fence Remover stays creative only. The lead can remove the recipes on request.

**Original bugs not copied:**

- spark spam from operator precedence (`pb || nb && rand < 0.02`)
- solid boxes on broken wires
- the yaw computed again on every block-state query
- the global `fullBoundingBoxLadders` override. Make the ladder rule per block instead.

**In scope:**

- Keep rft's `FencePowerService`, connection dedupe, cascade breaks and renderer. Port its Forge-only model data mixins to the P2 platform layer.
- Shock damage is `fences.damage`, default 1 HP for both posts (F25). Shocks draw no energy from the grid. This is the decision D23 default (tech survey, O9).
- Height: posts of 3 and 8 blocks already exist. Jump checks use the live height.
- Animals and the wire:
  - A live wire hurts players and animals (F25).
  - A Tyrannosaurus breaks unpowered fences by the F29 rule. Build it as an SBL behaviour on the library's base classes.
  - Any living entity climbs an unpowered wire (F26).
  - Hyper-intelligent animals detect unpowered sections (B10 reads the same `FENCE_IS_LIVE` memory).
- Switch block and redstone:
  - A redstone signal cuts power to a fence section.
  - The switch has a hand toggle, redstone control and an inverted mode. It flips the network, never each post (tech survey, B15.13).
  - A section switch changes one block state per post. Vanilla bundles the updates per section. No custom packet (tech survey, ON5).
- Three voltage signs from the asset art hang on wires, as F18 says. Changed 2026-10-10: they were decorative blocks in this plan, but the original hangs them on wires.
- Decorative fence and sign variants have no block entity and carry the vanilla fence tag. Visitor-side fences cost no ticks (tech survey, B15.16).
- Vanilla and decorative fences hold dinosaurs as walls with vanilla collision. They are not in the breakable list unless a data pack adds them. This is the decision D35 default (tech survey, O21).
- A vehicle that meets an electric fence stops by vanilla collision. The fence does not shock the vehicle or its riders. This is the decision D26 default (tech survey, O12).
- Feeders can be filled from outside the paddock. A feeder placed in a post line keeps the wire run intact (coordinate with B20).
- Remove the forced `|| true` and the 171 lines that comments disable in `BlockConnectableBase`. Remove the all-players chat message in `BreakFenceBehaviour`.
- Fence geometry in tiers (tech survey, B15.7, spec T4, rank 1):
  - Posts, insulators and signs draw from block models in blockstate JSON.
  - Each wire block draws only its own segment from an immutable `FenceVisual` record (connections, tier, broken flags, signs). Shared geometry code lives in `common`.
  - NeoForge reads `FenceVisual` from model data. Fabric reads it from `RenderDataBlockEntity` in `emitQuads`.
  - Model data changes only for slow state: connection layout, tier, band, a fallen post. A powered tint changes at most once per 20 ticks.
  - Sparks, arc flashes and a snapping wire are particles or a short block-entity effect while active.
  - Draw each wire once, from the lower end, with 4 to 6 segments for a short span (tech survey, OC7).
  - Today every fence block uses `ENTITYBLOCK_ANIMATED`, so 300 posts submit 300 or more block-entity renders each frame (tech survey, defect #14).
- A wire disappears while its span is on screen, when both posts are off screen. A block-entity renderer that remains returns a box over its span and never sets `shouldRenderOffScreen` (tech survey, defect #13, spec T4).
- Wire events: approach a paddock and every wire appears. Send one event of about 20 bytes per connect, break, repair or sign change. Send one batch when a player starts to watch a chunk (tech survey, B15.9, spec T2).
- One fence config block holds span and damage (tech survey, B15.11). The defaults are the F4 and F25 values.
- The spool names the failure: too long, already joined, no free connection, or blocked (tech survey, B15.12).
- A powered fence post top glows. Today it casts a shadow of 10 to 15 light levels, because the post overrides `getLightBlock`. Set the light through `lightLevel` from the state, changed only on a change (tech survey, defect #1, B15.14, `J/block/ElectricFencePostBlock.java:275-276`).
- Client memory grows while wires are on screen. Remove `NORMAL_CACHE`, which keeps one entry per triangle and never clears (tech survey, defect #4, `J/block/api/MathUtils.java:121,133`).
- Hundreds of wire blocks do useless work on each neighbour change. Remove the empty `ElectricFenceBlock.neighborChanged` override. P2 ports the `Orientation` signature (tech survey, defect #6, B15.15).
- Fence contact costs about 1,000 allocations per tick with 10 dinosaurs today. Build wire collision boxes once per block entity and rebuild them on a connection change. The shock reads the cached powered flag of the network (entity survey, B15.1, hot path S6).
- The Tyrannosaurus fence scan runs only on its F29 check, not every tick. Today the fence sensor reads 9,261 blocks per scan, always (entity survey, B15.2).
- Wire renderers walk connections with no stream. Today a 60-post paddock allocates about 1,800 objects per frame (entity survey, B15.4).
- Player guide: escape alarms from vanilla sculk sensors. Dinosaurs already emit step events. Document it only (entity survey, B15.5).
- Gates and the overhanging fence are not in Beta.

**Out of scope:** shock that follows the grid (tech survey, spec T3, only after D23 changes). A saved power graph across unloaded chunks (D21 default). Reinforced unbreakable blocks. A laser tripwire alarm. Iron-tier block breaking (tech survey, B15.18, B15.20, B15.21. Entity survey, B15.6).

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `fences.damage` | 1 HP (½ ♥), both posts | 1.12.2 parity F25 |
| `fences.max_span` | 15 blocks | 1.12.2 parity F4 |

**Produces:** `FencePowerService` (rft) with the F30 power numbers. The `FenceVisual` record. The `FENCE_IS_LIVE` memory for B10.

**Acceptance:**

- GameTest on both loaders: a Tyrannosaurus breaks an unpowered fence within 10 blocks and ignores a live one. A Gallimimus never breaks a fence (F29).
- A redstone signal cuts power to a section, and a Tyrannosaurus near it breaks it.
- A spool restores a broken wire.
- Both clients render broken halves and sparks.
- A dedicated server runs the fence paddock with no client class errors.
- A powered post top emits light (tech survey, defect #1).
- A wire stays visible while its span is on screen and both posts are off screen (tech survey, defect #13).
- A still paddock of 60 posts submits no block-entity renders per frame (tech survey, spec T4).
- A player who walks up to a paddock sees every wire and every broken wire (tech survey, spec T2).

Parity tests (1.12.2 baseline), on both loaders:

- Two Low Security posts 15 blocks apart on a slope get 6 wires and cost 3 spools in survival. Two High Security posts get 8 wires and cost 6 spools (F1, F5).
- At 16 blocks the player sees the too-long message. With too few spools the player sees the not-enough message, and nothing is built (F4, F5).
- A Low Security post does not link to a High Security post (F3).
- An empty-hand right-click flips a post 180°. A post with two runs faces their bisector (F7).
- Only the hovered wire shows an outline. A left-click removes only that wire, and the other wires in the block stay (F9, F10).
- The cut ends hang bent on both clients. A cut end on a powered run sparks (F11, F12).
- Each of the four repair routes restores a wire and uses 1 spool with the place sound (F13 to F17).
- An empty-hand right-click on a wire hangs a voltage sign. A second click removes it. The sign is still there after a reload (F18).
- After the first end post breaks, its wires hang unpowered. After the second breaks, the wires are gone (F19).
- The Fence Remover removes one wire line and keeps the posts. The Creative Fence Remover removes the whole network (F20, F21).
- The spool sprite changes stage as the pending span grows (F22).
- A fence fed at one post powers every post of the connected fence. One broken wire block makes that whole wire dead (F23).
- A powered Low Security post shows its lit lamp and gives light 10. A High Security post gives light 15. An unpowered post is dark (F24).
- A player in full diamond armor loses ½ ♥ (1 HP) at a live low-security wire, flies back from it and sees sparks. The death message names the fence (F25).
- A player and a Velociraptor climb an unpowered wire. Neither climbs a powered one (F26).
- A post with no supply goes dark after 35 ticks (1.75 s). A fence fed at one post lights post by post (F30, F31).
- A low-security wire and a high-security wire each do ½ ♥ (1 HP) (F25).
- `/projectnublar regenfence` runs at operator level 2, or the PR says why the rewrite does not need it (F28).

---

### B16: Dinosaur core

**Section:** 11.9, 11.16, 6.1, 6.4, issues #61, #63, #64 · **Size:** XL · **Depends on:** B00

**In game:** Each species has its own stats. A dinosaur grows smoothly, and its hitbox and stats grow with it. A Brachiosaurus neck stays visible at the screen edge. A dead dinosaur leaves a carcass that players and scavengers eat. Babies follow a parent and do not hunt.

**Touches:** `J/entity/dinosaur/Dinosaur`, `CarnivoreDinosaur`, `HerbivoreDinosaur`, the six species classes, `J/entity/dinosaur/PartEntity`, `J/registry/EntityInit`, `J/registry/MemoryModuleTypeInit`, `J/registry/AttributesInit`, `J/util/DinoNeedsUtils`, `J/entity/dinosaur/ai/util/FenceAwareNodeEvaluator`, `J/init/DataSerializerInit`, new `CarcassEntity`, `RemainsItem`.

**In scope:**

- Attributes come from `SpeciesData` at spawn and on `/reload`. Behaviour data does not depend on the egg.
  - The dinosaur caches its `SpeciesData` reference and refreshes it on reload. Write attributes on change only, never per tick (entity survey, B16.2).
- Smooth growth:
  - Age counts in ticks from 0 to `growth_days` × 24000.
  - Size, hitbox and stats interpolate. The `size` gene and the variation scale the end point. No step thresholds.
  - Persist age correctly. Fix the four flags read from one key.
- Hitbox:
  - `EntityDimensions` come from the render-scaled torso and legs, refreshed on growth through the 26.2 `scale` attribute.
  - Section hitbox parts are module B27. Large-body movement is module B26 (decision D06, answered 2026-10-09).
  - Re-test "spinning on block edges" (issue #64) after B26's arrival fix and record the result.
  - The herd work on `kash's-slop` sets the Gallimimus box to 1.2 × 2.6. Other species keep today's boxes (herd spec, decision 20). B26 replaces the Gallimimus box with the 0.80 × 2.39 movement box (hitbox design, layouts table). The layouts table gives the movement boxes of the other species.
- Culling box from the posed, scaled body. Today a Brachiosaurus neck and head vanish when its small box leaves the screen or hides behind a hill. The box is `sized(2, 3)` (tech survey, new item 29, defect #11, issue #63, `J/init/EntityInit.java:36`).
- Proposal (tech survey, ON9): tracking range by body size, 8 chunks for Gallimimus, Velociraptor and Dilophosaurus, 10 for T. rex and Triceratops, 12 for Brachiosaurus. Today all six species use `clientTrackingRange(10)` (160 blocks) (tech survey, new item 29, ON9, defect #12).
- Path check by body size: `FenceAwareNodeEvaluator` uses the mob-aware path check and checks for fence blocks across the whole body width. Today it calls the one-block check, so a large animal can path through a gap one block wide.
  - The herd work on `kash's-slop` already has a full-body path check: the evaluator asks `BodyFit` (`pathProbe`) whether the clearance box stands at a node. It is 1.20.1 code and ports in P2 (herd spec, Paths).
- Water path cost (decided by the lead on 2026-10-06):
  - The same evaluator adds a cost to water deeper than the animal's current height, scaled by the underwater-capacity gene.
  - Babies and small species go around deep water. Large ones wade.
  - It is a cost, not a wall. An animal with no other route still swims.
  - This addresses the drowning in issue #61.
- Debug status (decided by the lead on 2026-10-06):
  - Each animal syncs its most urgent need and its current activity to the client when they change, as two bytes (entity survey, B16.6).
  - The Debug Stick shows them to the holder only, on the action bar, in creative mode or for operators.
  - It replaces the stick's broadcast to all players.
- Sync only what the renderer needs. Keep the 12 need floats on the server (entity survey, B16.6). Today each synced genome change sends the whole genome as NBT. The game also sends a copy with equal values again. P2 gives `DinoData` an `equals` and a `StreamCodec` (tech survey, defect #5, `J/init/DataSerializerInit.java:14-24`).
- Death:
  - Implement `tickDeath`.
  - A carcass stays for `needs.carcass_minutes`, then decays.
  - It drops meat (raw per species, from the asset repository), fresh bones and tissue (remains for B04). It also drops special items: raptor toe claw, Triceratops horn, teeth. The special-drop art is missing. Use placeholders and flag them in B24.
- Carcass stages (entity survey, B16.5, feature 9):
  - `CarcassEntity` is a plain entity with no AI, sized from the dead dinosaur's box. It stores species, growth fraction and a meat pool.
  - The meat pool starts at the species hunger value times the growth fraction.
  - Each player use takes one of 4 stages and drops one share of the decided drops.
  - Each scavenger bite (B17) removes meat from the pool.
  - The carcass decays when the pool is empty or `needs.carcass_minutes` ends.
  - The renderer draws the species model in the `dead` pose where that clip exists. Drops spread over ticks (B25).
- Babies follow a parent, cannot hunt and need feeding. Stop `SoloHuntRoamBehaviour` for babies and herbivores.
  - Babies follow a parent through SBL `FollowParent` and the shared visible list. Search box 8 × 4 × 8. Stop at 3 blocks. Stop following at 16 (entity survey, B16.3).
  - A dinosaur that is not adult has no hunt activity and no fear effect from its threat clip. It uses the bite clip only and never targets a player (entity survey, B16.4, spec S1).
- Use `MobCategory.CREATURE` with `persistenceRequired`. Today all six species use `MobCategory.MONSTER`. Keep MONSTER only if a despawn rule needs it (entity survey, B16.1. Tech survey, new item 29).
- Clean the memory module registry:
  - Give `HAS_GROUP` and `IS_GROUP_LEADER` unique ids.
  - Remove the 15 unused types. Changed 2026-10-10: the count is 15, not 12 (entity survey, fact 46).
  - Fix the trust attribute translation key.
- Footsteps, roars and dust keep playing for animals off screen. Move client-only effects from `Dinosaur.tick` to `aiStep` or server events. Audit `Dinosaur.tick` for other client work (tech survey, new item 30, rank 7).
- Prey tables come from `SpeciesData.prey_preferences` by clade (T. rex card), players and villagers included. The `canTarget` overrides in Velociraptor and Dilophosaurus respect the parent result.

**Out of scope:** a live tuning command for scale and eye height (deferred). Baby health from a square-root curve. A stat bonus for the group leader (entity survey, B16.7 to B16.9).

**Produces:** `Dinosaur.getSpeciesData()`, `Dinosaur.getGrowthFraction()`, `Dinosaur.getEffectiveStat(Stat)`. `CarcassEntity`. `RemainsItem`.

**Acceptance:**

- Summon each species. Its stats match its data file.
- A T. rex reaches full size after 45 in-game days with `/time` acceleration (GameTest with a sped-up clock).
- The hitbox grows with the model.
- An adult T. rex does not path through a gap one block wide.
- A baby T. rex walks around a pond three blocks deep when a land route exists.
- The Debug Stick shows need and activity to its holder only.
- Killing a Triceratops leaves a carcass that drops a horn.
- A player takes the carcass in 4 uses. A scavenger bite shrinks the meat pool (entity survey, feature 9).
- A Brachiosaurus whose box is off screen but whose neck is on screen stays drawn (tech survey, defect #11).
- A baby follows its parent and stops following beyond 16 blocks (entity survey, B16.3).
- Unit tests on growth interpolation and stat scaling.

---

### B17: Needs

**Section:** 11.10 (needs, stress, results, carnivores, herbivores), 6.2 · **Size:** XL · **Depends on:** B16, B15, B20, B26

**In game:** Dinosaurs get hungry, thirsty, tired, lonely and stressed, and each need has results. Each species eats its own diet, and other mods' food counts through tags. A herbivore grazes what its head can reach. A carnivore picks its prey and scavenges carcasses. A tired predator stops its chase. A park of 100 dinosaurs keeps 20 ticks per second.

**Touches:** `J/util/DinoNeedsUtils`, `J/entity/dinosaur/Dinosaur` (running state, timers), `J/entity/dinosaur/ai/behavior/needs/*`, `J/entity/dinosaur/ai/sensor/*`, `J/entity/ai/sensors/NearestFeederSensor`, `J/entity/ai/tasks/WalkToNearestFeeder`, `J/entity/dinosaur/DinoOneShots`, `R/data/projectnublar/diet/` (becomes tags), `hunt_score_config.json` (remove).

**In scope:**

- Needs: hunger, thirst, stamina and sleep, social, stress. Rates come from `SpeciesData` and genes. Fix these defects:
  - `IS_HUNGRY` never clears.
  - The Drink distance does not match the walk distance. Use the same close-enough distance for both (entity survey, feature 11).
  - `isSociallyDrained` reads stamina.
  - `tickSocial` is empty.
  - `IS_EXHAUSTED` is never set.
- Herd scope rule (lead, 2026-10-10): herds must move and behave properly before hunger, thirst, attack and flee join herd play. Test on the Gallimimus only. Today stamina is the only need that drives herd behaviour. Build the other needs after B26's herd work passes its playtests.
- Stamina needs a non-zero maximum. Today the maximum is 0 because of a config key mismatch, so every chase ends at once. B00 fixes the key (entity survey, fact 51, feature 3).
- Stress inputs in Beta: food and water, company. A live-bait hunt lowers stress (B20). No enclosure or block comfort.
- Stress meter: one saved integer per dinosaur drives aggression and the breeding gate (entity survey, B17.5, spec S5, rank 9):
  - Every 20 ticks (1 s), offset by entity id, add the input deltas.
  - Subtract a drift toward zero: stress divided by the drift divisor, rounded.
  - Clamp from 0 to the maximum.
  - Set `IS_STRESSED` at or above the species `stress_threshold`. Clear it below the threshold minus the hysteresis.
  - A live-bait kill subtracts its relief once.
  - Send the band (calm, uneasy, stressed) to the client only when it changes.
- Results: aggression, health loss and death. A carnivore that kills a player gets hunger back. Changed 2026-10-10: unmet needs no longer drive fence attacks. B15 fence parity F29 lets only a Tyrannosaurus break unpowered fences, at any need level.
- Starvation (decided by the lead on 2026-10-06):
  - At zero hunger, the animal loses `needs.starvation_damage` health (default 1) every `needs.starvation_interval_ticks` (default 80 ticks, 4 s, the vanilla player rate). A larger animal lives longer.
  - With the section 11.16 health values, a Velociraptor (17.5 ♥, 35 HP) lasts about 2.3 minutes. A T. rex (77.5 ♥, 155 HP) lasts about 10. Tune in playtest.
  - While it starves, it turns aggressive. Changed 2026-10-10: it no longer attacks fences (B15 fence parity, F29).
  - Today `tickHunger` kills it on the next hunger tick.
- Two hunger thresholds: the animal seeks food below 75 % and runs to food below 25 % (entity survey, B17.6).
- Need cooldown (decided by the lead on 2026-10-06): a met need clears its memory (`IS_HUNGRY` and the others) and starts `needs.cooldown_seconds.<need>`. During the cooldown, the animal does not seek that need again. This stops eat-drink-sleep loops and cuts path requests.
  - Build the cooldown as a memory with an expiry, one per need, registered with a codec so that it saves. The need's sensor and behaviours start only while the memory is absent. The brain removes it at expiry. No other code counts the time (entity survey, B17.4, spec S4).
- Stamina-limited chases (decided by the lead on 2026-10-06):
  - A chase ends when stamina reaches zero. Then `needs.hunt_cooldown_seconds` passes before the next hunt.
  - Prey and players can escape a tired predator.
  - Playtest that every carnivore still feeds, because starvation is lethal.
  - Build: the hunt activity adds SBL `InvalidateAttackTarget.invalidateIf(stamina <= 0)`. The predator drops its target, sets `HUNT_COOLDOWN` with an expiry and switches to rest (entity survey, feature 3).
  - A new hunt starts only after the cooldown and above 0.33 of full stamina (entity survey, feature 3).
  - The hunt cooldown counts down in one place and has a GameTest (entity survey, B17.13).
- Brain activities (decided by the lead on 2026-10-06):
  - Group the behaviours into SmartBrainLib activities with priorities, not one ordered list.
  - At least core, idle, rest, hunt and fight. B18 adds social and breeding.
  - Build on the SmartBrainLib 2 structure from P2. GameTests cover each activity switch.
- Running state from speed: set "running" from the actual movement speed each tick (the same signal as B09's gait choice), not from a flag. Today the chase sets a flag that never clears, so stamina drains at double rate until the chunk reloads.
- Sleep (entity survey, B17.10, spec S7):
  - `SmartBrainSchedule.byDaytime` switches to the rest activity in the species window from `SpeciesData`.
  - Sleep starts in the window when the sleep need is low, no danger memory exists, and the dinosaur stands on ground, not in water.
  - A sleeping dinosaur does not move, and its sleep need recovers.
  - Damage wakes it and sets `WAKE_LOCK` for 200 ticks (10 s).
  - Each wake sets `SLEEP_COOLDOWN` for 6,000 to 8,000 ticks (5 to 6.7 min).
- Carnivores:
  - Prey ranking by clade from `SpeciesData`. Players and villagers are prey.
  - Scavenging of carcasses and dropped meat. The item sensor finds dropped meat only while hungry, within 16 blocks (entity survey, B17.9).
  - Pick prey once per sensor scan by one scored pass over the shared visible list. Cache the enclosure test per target for 100 ticks. Fix the block-entity test that is always false. Today each hungry carnivore makes about 1,200 lookups and 800 allocations per tick (entity survey, B17.7, rank 6, hot path S3).
- Herbivores: graze blocks in a diet tag (grass, leaves, crops, flowers) and consume the block. Reach comes from `SpeciesData` (Brachiosaurus high, Triceratops low). Herbivore feeder (B20).
- Grazing inside a height band (entity survey, B17.8, spec S6):
  - The band is the species reach times the growth fraction, relative to the feet.
  - Each scan samples 16 random columns within 12 blocks, every 40 ticks (2 s) while hungry. It never scans a full ring. Each scan reads under 200 blocks.
  - In each column, read only the blocks inside the band. The first block in the diet tag wins. Store it in `GRAZE_TARGET` with an expiry.
  - Consume the block as vanilla sheep do, and only when `mobGriefing` allows it.
- Head contact (lead, 2026-10-09. Hitbox design, IK decisions):
  - Eating and drinking count only when the head touches the food, the water or the feeder.
  - The animal must find a stand point from which its mouth reaches the source.
  - Each species needs a reach envelope and a contact tolerance. The same rule applies to feeders (B20).
  - Neck reach IK waits until eating and drinking animations exist. Gallimimus has no `eat` or `drink` animation today (hitbox design, open items. B24).
- Thirst (entity survey, feature 11, B17.2):
  - Below the seek threshold, the dinosaur sets `IS_THIRSTY`. The water sensor runs only while that flag exists.
  - The sensor scans a radius of 8 by 2 blocks every 60 ticks (3 s). A failed scan grows the radius by 8 blocks, up to the follow range. A found source resets it.
  - It stores the found shore block in `HAS_FOUND_WATER` for 6,000 ticks (5 min).
  - The dinosaur walks to the shore block, plays the drink clip and refills thirst. Then the need cooldown starts.
- Diets are item tags and block tags per species under `data/projectnublar/tags/`, so other mods' plants and meat can join them. Food value per item or tag lives in species data, with `c:` tags (entity survey, B17.14).
- Sensor cost: replace the three per-entity block scans with a shared chunk-section cache or a POI search. Budget under `needs.sensor_budget_blocks` per tick.
  - Today the block-scanning sensors read about 735,000 blocks per tick at 100 dinosaurs, about 22 ms. Aligned scans spike near 440 ms (estimates) (entity survey, rank 2, hot path S1).
  - Find feeders through the per-level feeder registry. B20 owns the registry. B17 reads it (entity survey, B17.1).
  - Offset every sensor start by entity id, for example `scanRate(e -> base + (e.getId() & 15))` (entity survey, B17.3).
  - Target after the fix: under 1,000 reads per tick, under 0.1 ms, with no aligned spike (entity survey, rank 2).
- Path budget (entity survey, B17.11, B17.12, spec S8):
  - A need walk asks for a path only while the per-level counter is under `ai.path_budget_per_tick`. Otherwise it joins a queue that runs first on the next tick.
  - A failed path doubles the retry delay, from 20 to 400 ticks (1 to 20 s), and stores the target in an unreachable-food list.
  - Herd and journey paths use the budget split of the hitbox design.
- Timers use game time (tech survey, new item 31, OS4):
  - Under Immersive Optimization, far animals recover stamina and lose needs at a fraction of the normal rate. The timers count `tickCount`, which stops while the mod skips the entity (tech survey, defect #10, `J/entity/dinosaur/Dinosaur.java:581,723,760-765`, `DinoOneShots.java:70`).
  - Use game time for need decay, cooldowns, grace periods and memory lifetimes that matter for play. Clamp the elapsed time, for example to 40 ticks.
- 100 animals near feeders print about 100 log lines per second. Remove debug output from `DinoNeedsUtils`, sensors and behaviours: 12 print calls in 6 files (tech survey, defect #7, OS16).

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `needs.starvation_damage` | 1 | Lead, 2026-10-06 |
| `needs.starvation_interval_ticks` | 80 ticks (4 s) | Vanilla player rate. Lead, 2026-10-06 |
| `needs.cooldown_seconds.<need>` | Set in the task plan | Lead, 2026-10-06 |
| `needs.hunt_cooldown_seconds` | Proposal 120 s | Naturalist 2,400 ticks (entity survey, feature 3) |
| `needs.sensor_budget_blocks` | Set in the task plan | Plan |
| `ai.path_budget_per_tick` | 4 | Entity survey, spec S8 (estimate) |

Stress start values (proposals for playtest, entity survey, spec S5):

| Value | Start |
| --- | --- |
| Maximum | 100 |
| Hungry, thirsty, alone (below `group_size_min`) per step | +2, +2, +1 |
| Fed, watered and with company per step | −1 |
| Drift divisor | 12 |
| Hysteresis | 10 |
| Live-bait relief | −20 |

**Produces:** `NeedsState` on the entity with getters per need. Memories `STRESS_LEVEL`, `IS_STARVING`, `IS_STRESSED`, one cooldown memory per need, `HUNT_COOLDOWN`, `SLEEP_COOLDOWN`, `WAKE_LOCK`, `GRAZE_TARGET`.

**Acceptance:**

- GameTest on both loaders: a starving T. rex loses health at the starvation rate, turns aggressive, and does not attack a live fence.
- Stamina drain returns to the walking rate after a chase ends.
- A T. rex that chases a sprinting player stops when its stamina reaches zero.
- An animal that ate does not walk to food again during its cooldown.
- A fed, watered pair has falling stress.
- A Brachiosaurus eats leaves at height 8, and a Triceratops does not.
- An animal eats only from a stand point where its head touches the food, water or feeder (lead, 2026-10-09).
- A test skips a dinosaur's entity tick for 100 ticks. The dinosaur loses the same hunger as one that ticks every tick, within the 40-tick clamp (tech survey, new item 31).
- With 20 thirsty dinosaurs, the water sensor stays under the `needs.sensor_budget_blocks` budget (entity survey, B17.2).
- 20 animals in one paddock keep the server tick under 50 ms.
- Unit tests on need decay and stress arithmetic.

---
### B18: Social life and breeding

**Section:** 11.10 (groups, breeding) · **Size:** L · **Depends on:** B17, B26

**In game:** Each herd and pack has one leader. A rival challenges the leader with a roar or a frill, and no animal gets hurt. Dinosaurs call to warn, gather and threaten. A Velociraptor pack selects one prey, surrounds it and attacks together. A calm pair with a nest in range lays eggs. The adults guard the nest and the young.

**Boundary with B26 (changed 2026-10-10):** B26 owns group formation, joining, herd travel, spacing and the herd animations (calling at a journey start, sentry, startled). B18 owns leaders, dominance contests, calls, pack hunting and breeding. B18 reads the groups that B26 builds and adds behaviour to them. B18 does not change how a herd moves (herd spec, decision 26). The herd fight chase stays in B26. The lead deferred it (herd spec, decision 32).

**Touches:** `J/entity/dinosaur/ai/behavior/social/*`, `J/entity/dinosaur/Dinosaur` (mating), `SetMateFromNearbyDinosaurs`, `GroupManager`, new `NestBlock`.

**In scope:**

- Changed 2026-10-10: `GroupManager` is not new. The herd work on `kash's-slop` (1.20.1, uncommitted) already has these classes in `J/entity/ai/behaviour/social/`: `GroupManager`, `DinoGroup`, `Dominance`, `ChallengeGroupLeader`, `JoinGroup` and `LargeCarnivoreFight`. They port with P2 (local code on `kash's-slop`).
- Order (lead, 2026-10-10): herds must move and behave properly before genes, stats, hunger, thirst and attack or flee arrive. Test on the Gallimimus only. B18 items that need these factors wait until the B26 herd movement passes.
- Changed 2026-10-10: the old item "group formation per species within `group_size`" moves to B26. B18 keeps one leader per group, rank contests without turf wars, and calls.
- A dinosaur calls to warn, gather or threaten. Each call is a sound event (B24 imports the sounds). Each call is also a game event, so B19 listeners hear it with no new code (entity survey, B18.7).
- A hurt member warns its group. It writes a panic timer of 100 to 199 ticks (5 to 10 s) to members in an 8 × 4 × 8 box. It writes once per event (entity survey, new item 20).
- Dominance (decided by the lead on 2026-10-06): a dominance value from size, age and the aggression gene picks the leader and settles rank contests. A contest plays `ThreatDisplay` and does no damage. The leader chooses where the group goes.
- Dominance contest flow (entity survey, feature 4):
  - A challenge starts when a member's dominance passes the leader's value by the margin and its cooldown is over.
  - Both animals face each other and play `ThreatDisplay` for the species display time (B19, entity survey, spec S10).
  - The higher dominance wins. A tie goes to the current leader. The loser gets a challenge cooldown memory.
  - Dominance comes from saved values, so a world reload elects the same leader. Groups need no save.
  - Start values: weights age 0.5, size 0.3, aggression gene 0.2 (local `Dominance`). Cooldown 1,200 to 2,400 ticks (60 to 120 s). Margin 10 % of the leader's value (estimate).
- Pack hunting for Velociraptor: pack members share a target and flank.
  - When the leader's attack target changes, the group manager pushes it once to each member inside the species follow range. Skip babies, members in a hunt cooldown and members that cannot attack the target. Do no push per tick (entity survey, new item 20, spec S9).
  - Members clear a dead or lost target through `InvalidateAttackTarget`.
  - Each member gets a slot angle around the prey from its index: leader 0°, then ±60°, ±120°, 180°. The flank radius is 6 blocks. All members attack together when every member reaches its slot, or after 60 ticks (3 s). Slots update on the 20-tick sensor step. All values are estimates (entity survey, new item 22, feature 12).
- `JoinGroup` filters the shared nearby-entity memory and runs no entity query of its own (entity survey, new item 20). `JoinGroup` is herd-work code, so coordinate the change with B26.
- Entity queries use a typed query (`EntityTypeTest` or a class), so Lithium's fast path works (tech survey, OS10).
- Add no log call in a per-tick or per-entity path. Use SLF4J with `{}` only. B17 removes the 12 existing print calls (tech survey, defect #7, OS16).
- Breeding: male and female within a group. The female builds a nest block and lays `egg_clutch` eggs. Eggs hatch at `hatch_success`. Adults guard nests and young.
  - Breeding flow (entity survey, B18.4):
    - The female keeps a saved has-egg flag.
    - She walks to the nest and plays a lay animation.
    - She lays the clutch.
    - Both parents get a cooldown.
  - The female builds the nest only on a floor block from a block tag (entity survey, B18.5).
  - Fix the `isMate` UUID comparison and the `nextInt(1,2)` clutch.
  - Give each young its own copy of the genome. Today `Dinosaur.java` passes the mother's own `DinoData` object. A change to the young's gender then changes the mother's.
- Adults protect the young. A guard searches for a baby within 8 × 4 blocks first, then for a player at half the follow range. Alerts skip babies (entity survey, new item 21).
- Fertility and gestation genes feed the roll.
- Breeding gates (decided by the lead on 2026-10-06): a pair breeds only when all three conditions are true. Neglected paddocks stop breeding, which also limits entity growth.
  - A nest is in range.
  - The fertility roll passed.
  - The stress of both animals is under their species `stress_threshold`.
- Breeding cap per area: decision D20 default 0 (no cap), as a config value (entity survey, O6).
- Heredity (decided by the lead on 2026-10-06):
  - Number genes take the midpoint of the parents plus the per-clone variation (B07).
  - Colour layers, skin, special gene and morph each come from one parent at random. The young also gets the normal `editing.morph_chance` roll.
  - The rule clamps every bred gene to the edit limits, so breeding cannot pass the caps that B07 enforces.
  - Gender is a 50/50 roll. This is a default: the lead did not decide it separately.
  - The rule lives in one pure function with unit tests.

**Out of scope:**

- Turf wars and false eggs. Leave `StartTurfWar` and `LargeCarnivoreFight` disconnected with a note, or remove them.
- A pack flees when it is small or when the leader dies. Deferred, because section 11 is silent (entity survey, B18.9).
- Raptors stalk, crouch, then pounce. Deferred until B24 has crouch and leap clips (entity survey, B18.10).
- A scriptable breeding rule pipeline. Dropped, because the lead decided the midpoint rule (entity survey, B18.11).

**Produces:** `GroupManager` (per-level map of group id to members, from the herd work, extended here), `NestBlock`.

**Acceptance:**

- Six Gallimimus form one herd (B26) with one leader.
- The leader is the animal with the highest dominance.
- A dominance contest does no damage, and a world reload keeps the same leader (entity survey, feature 4).
- Three raptors converge on one prey.
- Pack members take different flank slots before they attack together (entity survey, feature 12).
- A T. rex pair produces a nest whose eggs follow the clutch and hatch rates over 20 trials (GameTest with accelerated time).
- A unit test shows that a change to a young's genome leaves the mother's genome unchanged.
- Unit tests on the heredity rule: midpoint, choice genes from one parent, clamp to edit limits.
- A stressed pair does not breed.
- Both loaders.

---

### B19: Player interaction and detection

**Section:** 11.10 (the player, detection), 6.2 (taming stub) · **Size:** L · **Depends on:** B17, B18, B27

**In game:** A dinosaur notices a player by sight, sound or scent. A sneaking player far away often stays unseen. Before an attack, the dinosaur stops, faces the player and roars or shows its frill for 2 to 4 s. A player who retreats can avoid the fight. A Gallimimus runs from threats. A Triceratops stands and charges. A calm animal can be tamed and takes follow, stay and defend commands.

**Touches:** `ThreatDisplay`, `J/entity/dinosaur/Dinosaur` (taming), new `DetectionService`, `ScentTrail` saved data, `J/entity/dinosaur/ai/sensor/*`.

**In scope:**

- Threat display before attack. Connect `ThreatDisplay`, with a roar or a frill per species.
  - Threat display and attack run as one track: display clip, one hit tick with a cone test (B27, entity survey, spec S27), then recovery. The server sends one trigger per attack and no animation packet per tick (entity survey, rank 10).
  - A target beyond the skip radius starts the display phase if the display cooldown is over. The dinosaur stops, faces the target, starts the clip by a server trigger and plays the sound (entity survey, spec S10).
  - The display lasts the species clip length. If the target leaves the territory or the follow range, the display ends and no attack follows.
  - A target inside the skip radius, or damage taken, skips the display. The cooldown starts at the end of a display, so each fight opens with one display at most.
  - Start values: skip radius 5 blocks (estimate). Display cooldown 200 to 400 ticks (10 to 20 s).
- Flee or stand per species from data. Territory radius. Adults attack near young.
  - Territory uses `Mob.restrictTo(home, territory_radius)`, with no new code path (entity survey, B19.7).
  - A fleeing species uses an avoid memory (entity survey, spec S11). The visible-entity sensor tests each entity against the species flee list. A match writes `AVOID_TARGET` with an expiry of 100 to 200 ticks (5 to 10 s).
  - While the memory exists, the dinosaur walks to a point 16 blocks away from the threat. It sprints inside 7 blocks. Speeds are 1.25 walk and 1.4 sprint. It picks a new point on arrival, or every 20 ticks while the threat stays.
- Taming of calm, low-stress animals. The owner is safe. Commands are follow, stay and defend. The bond breaks on hunger or stress.
  - Owner safety ends above the stress threshold (entity survey, B19.10).
  - Commands are one synced value. Each owner use cycles it and shows an action-bar message. Follow starts at 7 blocks, stops at 2, and teleports beyond 18 to a checked spot (entity survey, B19.8).
  - Every command packet passes a check. The owner UUID must equal the sender, and the distance must be inside the follow range. The server accepts one packet per player per tick at most (entity survey, spec S12).
- Write `PLAYER_REPUTATION`. It is one capped integer per player with a daily decay and a threshold. Today no code creates the map (entity survey, new item 25).
- Detection channels are sight and sound, plus scent points that creatures drop and that fade (`needs.scent_lifetime_seconds`). The detection chance is 25 to 85 % by distance and player activity. Vision is not movement-based. Build each channel from vanilla pieces (entity survey, rank 8):
  - Sight: a 20-tick sensor over players in follow range. Visibility scales the range (sneak 0.8, floor 2 blocks). A species view cone and a cached line of sight follow (entity survey, B19.1).
  - Sound: a `VibrationSystem` listener with a species event tag, a radius and a 40-tick (2 s) cooldown. It costs nothing between events (entity survey, new item 23).
  - One detection roll per awareness episode. The result sits in a memory with an expiry. A 25 % roll per 20-tick scan would reach 76 % in 5 scans (entity survey, new item 24).
  - Episode length: decision D15 default 200 ticks (10 s), `detection.episode_ticks` (entity survey, O1).
  - Scent: a level store keeps points with an expiry. A sensor finds them with no line of sight. The dinosaur follows them at 0.7 speed (entity survey, B19.4).
  - Scent sources, spacing and cap: decision D16 default (entity survey, O2). Players and hurt mobs drop points 2 blocks apart, up to 32 points per chunk. All three are config values.
  - Keep every "is a player near" radius at 128 blocks or less (tech survey, OS11).
  - Clear the scent store on level unload (entity survey, B25.4).
- Hyper intelligence and thermal detection (B10) plug into the same detection pipeline.
- Status readout for owners who are not operators: decision D18 default no readout. The debug stick stays as decided in B16 (entity survey, O4).
- Vanilla villagers and livestock flee loose dinosaurs: decision D19 default off, `vanilla.mobs_fear_dinosaurs` (entity survey, O5). If a server turns it on, use a tag, a config value and a tick event, never a goal replacement (entity survey, B21.7).

**Out of scope:**

- A roar frightens other mobs. Deferred, because section 11 is silent (entity survey, B19.12).
- Taming by stun, then a calming food. Dropped, because section 11.10 tames calm animals and guns stay blocked (entity survey, B19.13).

**Produces:** `DetectionService.chanceToDetect(animal, player)`, `ScentTrail` level data.

**Acceptance:**

- A sneaking player at 30 blocks is detected less often than a sprinting player at 10 blocks (GameTest with 100 samples).
- A tamed raptor follows and defends.
- A Triceratops charges and a Gallimimus flees.
- A dinosaur plays its threat display before the first hit (entity survey, spec S10).
- A player who leaves the territory during a threat display takes no hit (entity survey, spec S10).
- Inside one awareness episode, a dinosaur rolls once per player (unit test) (entity survey, new item 24).
- A command packet from a player who is not the owner changes nothing (entity survey, spec S12).
- Both loaders.

---

### B20: Feeders

**Section:** 11.12, 5.3 · **Size:** M · **Depends on:** B16, B14, B12

**In game:** The Carnivore Feeder, the Herbivore Feeder and the Live-Bait Feeder work. Each has a screen. A hopper or a pipe keeps a feeder stocked from outside the paddock. The block shows its fill level, and a comparator reads it. A cow led into the Live-Bait Feeder waits on a leash until a hungry raptor comes near. Then the feeder opens and the raptor hunts.

**Touches:** `J/block/DinosaurFeederBlock`, `J/block/entity/DinosaurFeederBlockEntity`, `EatFromMeatFeeder`, `WalkToNearestFeeder`, `NearestFeederSensor`, new Herbivore Feeder and Live-Bait Feeder classes, feeder screens.

**In scope:**

- Fix the Carnivore Feeder:
  - The feeder skips the third food slot. The slot switch has `case 3` and no `case 2` (`DinosaurFeederBlockEntity.java:44-46,138-140`) (tech survey, defect #20). This is the "slot bug".
  - Item stack aliasing.
  - Save the contents.
  - Hunger actually increases on eating.
  - The map key type.
  - The range mismatch. The eat range equals the walk stop distance plus half the species width (entity survey, new item 26).
  - The sensor cache expiry.
  - Rename the id.
- The feeder is a vanilla `Container` with item capabilities on both loaders (entity survey, B20.1). Sided item handlers let hoppers, Create chutes and Refined Storage exporters fill it with no Nublar code (tech survey, B20.13, rank 11).
- One feeder serves many dinosaurs. Items convert to points in a pool per diet, and each bite takes points (entity survey, new item 26).
- The block shows its fill level as a block state 0 to 4, written on change (entity survey, new item 26).
- A comparator reads the fill level. Clamp the output to 15 (entity survey, new item 10).
- Slots accept only the species diet tag (entity survey, B20.6).
- The eater stores the feeder position. At use it gets the block entity again and checks that the feeder still exists (entity survey, B20.3).
- B20 owns the per-level feeder registry. Feeders join it on load and leave it on removal. Level unload clears the registry (entity survey, B17.1, B25.4).
- A feeder that watches a neighbour inventory polls at a rate between 5 and 40 ticks. The rate is faster after a change and slower after none (tech survey, new item 32, OS12). Spread the polls with a per-feeder offset (tech survey, OS13).
- Diet and prey lookups resolve once per reload into arrays. A cache holds them and clears on reload and on a tag update (tech survey, new item 32).
- Stackable food and Nublar items carry deterministic components, so they stack and show in storage grids (tech survey, B20.14).
- The Herbivore Feeder accepts the herbivore diet tag.
- Live-Bait Feeder: the player leads a vanilla animal in. The feeder holds it and releases it when a hungry carnivore is near. The hunt lowers stress (entity survey, spec S13):
  - The player uses the feeder while the animal is in the pen. The feeder tags the animal `projectnublar:bait` and makes it persistent.
  - The feeder ties the animal with a vanilla leash. It does not change the animal's goals.
  - Every 40 ticks (2 s) the feeder asks the registry for a hungry carnivore inside the release range of 16 blocks (estimate).
  - On a match, the feeder drops the leash and sets its open state.
  - A carnivore's prey pass ranks tagged bait first. A kill of tagged bait gives the stress relief of B17.
  - If the animal leaves or dies, the feeder clears its state.
  - A player, a dispenser and a hopper load bait through one plan, simulate and apply path (tech survey, new item 32).
- Screens for all three feeders. Hopper and pipe input. Fill from outside the paddock (B15 integration).
- Feeder models come from the asset repository. They need a Blockbench export (B24).

**Out of scope:**

- An "any item" filter slot (tech survey, B20.15).
- A point-of-interest type for feeders. The feeder registry is simpler (tech survey, B20.16).
- An experience-orb magnet with entity queries every tick (entity survey, B20.9).

**Produces:** the per-level feeder registry that B17 reads.

**Acceptance:**

- A hopper keeps a Carnivore Feeder stocked, and a T. rex eats from it with a rising hunger value.
- A cow in the Live-Bait Feeder is released to a hungry raptor.
- The third food slot takes food, and dinosaurs eat from it (tech survey, defect #20).
- The comparator output follows the fill level and never passes 15 (entity survey, new item 10).
- Both loaders.

---

### B21: Digsites, paleontologists, Paleo-Illagers and maps

**Section:** 11.3, 8.6 (digsite cards) · **Size:** XL · **Depends on:** B01, B02, B03

**In game:** The world teaches the player where to dig. Digsites have tents, chests with fossils, paleontologist villagers and iron golems. A paleontologist trades fossils, maps, amber and tools for emeralds. A digsite map leads to the nearest digsite. Paleo-Illager sites fight back and give fossils and machines when cleared.

**Touches:** new `J/worldgen/structure/*`, structure NBT and jigsaw JSON under `data/projectnublar/worldgen/`, new villager profession and POI registration, map item, illager entity variant.

**In scope:**

- Digsite structures (jigsaw): a rich fossil deposit from B01's `digsite_deposit` feature, tents, loot chests, paleontologist villagers and iron golems. Sizes go from small to large. Larger is rarer.
  - The digsite is data only under `data/projectnublar/worldgen/`. No code places it, so `/locate` works with no code (entity survey, spec S14).
  - Start values: jigsaw depth 3 to 4, heightmap `WORLD_SURFACE_WG`, terrain adaptation `beard_thin`, a biome tag. Spacing 42 chunks, separation 28 chunks. Village exclusion 10 chunks. Optional pieces use pools with an empty fallback (entity survey, spec S14).
  - Paleontologists and golems stand at fixed spots. Each piece points to a mob pool with one template that holds one entity, or holds a data marker (entity survey, spec S15).
  - Each chest in a template names its own loot table. Fossils join vanilla loot tables only through an inject pool at loot-table load. Replace no vanilla file (entity survey, new item 27, spec S15).
- Paleontologist villager profession with a workstation block. Trades for emeralds: maps, fossils, amber and special pieces bought and sold, tools and parts sold. It spawns in digsites and at a low rate in villages.
  - Register a point-of-interest type for the workstation block states with one ticket. Register the profession with that point of interest and a work sound (entity survey, spec S17).
  - Ship the trades as data per level. 26.2 trades are registry keys. Vanilla villager brains claim the job site. Nublar adds no job code (entity survey, spec S17).
  - At server start, add a paleontologist house to each vanilla village house pool with the configured weight. A weight of 0 adds nothing. Replace no vanilla pool file (entity survey, new item 27, spec S18).
  - `worldgen.paleontologist_house_weight` starts at 1 (estimate) (entity survey, spec S18).
- Paleo-Illager sites: a hostile variant guarded by illagers. A site is small and poor or large and rich. Clearing it gives fossils, chests and machines. No neutrality item, no "ancient plight".
  - The Paleo-Illager extends the vanilla illager base. It uses the vindicator goal set and starts at the vindicator attributes (entity survey, spec S16).
  - It spawns only from site mob pools and the structure spawn overrides. It joins no raid (entity survey, spec S16).
- Maps: an explorer-map item leads to the nearest digsite and can name a species. Paleontologists, cartographers and wandering traders sell it.
  - Maps and trades use the vanilla `exploration_map` loot function and the vanilla treasure-map trade. An async-locate mod then covers Nublar maps, and a map trade never freezes the server. Nublar writes no structure search of its own (tech survey, new item 33, OW6).
  - Choose the digsite spacing so that the median distance stays below 800 blocks. Maps then fill fast on a fresh world (tech survey, new item 33).
  - Guaranteed digsite distance: decision D33 default is the vanilla `/locate` radius of 1,600 blocks (100 chunks). Changed 2026-10-10: this replaces the plan's 2,000 blocks, which is more than the vanilla command radius. Maps use the vanilla function with no wait screen (tech survey, O19).
- Worldgen thread rules (tech survey, B21.14, OW1 to OW5):
  - Build lookup maps once on the main thread as immutable maps.
  - Use only the `RandomSource` that the generator gives.
  - Place blocks only inside the given region, with no drops and no entity spawns.
  - Keep no lazy cache inside a feature config or a structure piece.
- Any change to a vanilla mob uses a tag, a config value and a tick event, never a goal replacement (entity survey, B21.7).
- The illager skin has no art. Use a retextured vindicator and flag it in B24.

**Out of scope:**

- Paleo-Illagers in raids and patrols. Deferred, because section 11.3 is silent (entity survey, B21.8).
- Dialogue NPCs. A trade screen covers Beta (entity survey, B21.9).

**Acceptance:**

- `/locate structure projectnublar:digsite` finds one in a fresh world within 1,600 blocks. Changed 2026-10-10: was 2000 blocks (decision D33 default).
- A map bought from a paleontologist points to it.
- An illager site attacks and, once cleared, yields fossils.
- No digsite overlaps a village (entity survey, spec S14).
- With `worldgen.paleontologist_house_weight` at 0, no village gets a paleontologist house (entity survey, spec S18).
- Both loaders.

---

### B22: Vehicles

**Section:** 11.12 (vehicles) · **Size:** XL · **Depends on:** P2, B24 (models)

**In game:** The player drives a Jeep Wrangler around the park. A 1992 Ford Explorer follows a track that the player lays and stops at stop markers. At each stop it plays a chime and shows a line such as "Stop 2 of 4: Triceratops paddock". It waits for a Triceratops on the road and honks. It runs driverless tours in a loop.

**Touches:** new `J/entity/vehicle/*`, `J/block/TrackBlock`, `J/block/TourStopBlock`, client renderers, input packet.

**In scope:**

- Vehicle entity base: seats, driver input, collision, GeckoLib model. Fuel is decision D07.
  - The driver's client runs vehicle physics. Other sides interpolate over the real update interval. The server clamps speed and distance per packet to the top speed in the TOML (entity survey, new item 28, tech survey, new item 35).
  - Driver controls travel to the server only when they change (tech survey, new item 35).
  - Driver keys travel in one checked packet. The sender must be a passenger of that vehicle, and drive keys need the driver seat. The server accepts one packet per player per tick at most. Dismount clears the input state (entity survey, spec S12).
  - Handling numbers live in one data record per vehicle (entity survey, B22.3, tech survey B22.24):
    - top speed, acceleration time, turn rate
    - reverse at a third of top speed, collision speed cap, half speed in tight turns.
  - The Jeep Wrangler and the Ford Explorer share one class.
  - Controls work as in a boat: A and D turn, the mouse looks (entity survey, B22.4).
  - Seats sit at model locators. A development command computes the offsets (entity survey, B22.5).
  - The placement item ray-traces to the hit block and builds the vehicle box on the hit face. If the box hits blocks, it fails with a message. A vehicle never appears inside a wall (entity survey, spec S19).
  - Animation-only values stay on the client (entity survey, B22.8).
  - A moving vehicle lets vanilla tracking send its position. The vehicle sends Nublar's extra state (wheel angle source, lights, horn) only on change. A vehicle that stops sends one final state, then nothing (tech survey, spec T15).
  - Tracking range 10 chunks (tech survey, spec T15). Update interval: the tech survey proposes 3 ticks (spec T15), and the entity survey proposes 1 tick for vehicles only (B22.8). The task plan picks one.
  - Render distance grows while a vehicle carries passengers, so a passenger never floats alone (entity survey, new item 28).
  - The culling box covers the whole model, so the vehicle does not vanish with EntityCulling (tech survey, new item 35).
  - Dinosaurs never board a vehicle. The tag `projectnublar:vehicle_may_autoboard` is empty by default (tech survey, new item 35).
  - Do not override `canCollideWith` on vehicles. Override `canBeCollidedWith` only on the tour vehicle (tech survey, OS7).
  - A vehicle that meets an electric fence: decision D26 default is that vanilla collision stops it. The fence gives no shock to the vehicle or the riders (tech survey, O12).
- Jeep Wrangler: driven, two or more seats. Decision D25 default: 2 seats (tech survey, O11).
  - The Jeep Wrangler climbs a one-block ledge with a step height of 1.0 to 1.25. The body tilts on the client from the wheel heights (tech survey, B22.23).
- Ford Explorer: driven by hand off the track. It follows a track block that the player lays. It stops at stop markers for `vehicles.stop_seconds`. It runs driverless tours in a loop.
  - Decision D25 default: 4 seats. A player boards only at a stop (tech survey, O11).
  - Track follower (entity survey, spec S20, feature 6): the Explorer reads the track block and its `RailShape` only when its block position changes. From the shape and the travel direction it picks the exit. It moves at a constant track speed with no physics, and it rises or falls on slopes.
  - The body turns toward the exit at a capped rate. At a track end the Explorer stops and gives control to the driver.
  - Track speed starts at 0.25 blocks per tick (5 blocks per second, estimate) (entity survey, feature 6).
  - Driverless mode is a state of the Explorer, not a second entity. The driver starts it with a key while the Explorer is on a track. It then runs on the server with or without a driver, and passengers can ride (entity survey, feature 7).
  - The tour forces no chunks and takes no chunk tickets. Where chunks stop ticking, the tour pauses (tech survey, B22.26).
  - The tour vehicle implements `isAlwaysTicking`, so a tour keeps full speed under Immersive Optimization (tech survey, new item 35).
  - Blocked track (tech survey, new item 34, spec T13): every 10 ticks, test a box 4 blocks ahead on the track. The test finds entities in the tag `projectnublar:tour_vehicle_stops_for`. If an entity blocks the box, stop. After 60 ticks (3 s), sound the horn with a growing chance. Stop sounding after 5 horns. Start again when the box is clear.
  - Decision D25 default: the stop tag holds all dinosaurs and players (tech survey, O11).
  - Stops (tech survey, new item 34, spec T13): a stop marker can set its own dwell time, which overrides `vehicles.stop_seconds`. It can play an announcement sound and show a text to riders, with a "Stop 2 of 4" action-bar line.
  - A powered stop marker holds the vehicle until the signal ends. A lever in the control room can hold a tour (tech survey, new item 34).
  - Loop check (tech survey, new item 34, spec T10): at a tour start, walk the track through each block's exits. The walk ends when it returns to the start. A gap stops the walk and shows "Track broken at x, y, z" to the player. Cap the walk at 4,096 blocks (estimate) as a config value.
  - Changed 2026-10-10: decision D25 default is no Tour Station in Beta. The driver starts the loop (tech survey, O11, B22.14).
- Track block and stop marker block with recipes (B13).
  - `TrackBlock` extends `BaseRailBlock` and reuses `RailShape` and `AbstractMinecart.exits`. Curves and slopes then place like rails on both loaders (tech survey, B22.17).
  - A long track placed by a tool appears with no lag. Place all blocks with flags that skip neighbour and shape updates. Then send neighbour updates around the two ends only (tech survey, spec T10).
- Headlights light the road where LambDynamicLights is installed, through an optional data file (tech survey, B22.25).
- Models exist in the asset repository and need a Blockbench export (B24).
- Correction 2026-10-10: driverless vehicles have precedents. Automobility's Autopilot and MTR trains run with no driver. The entity survey said that no precedent exists (tech survey, facts).

**Out of scope:**

- Gyrosphere, monorail, aircraft, boats, schedules beyond a loop with stops. Track junctions stay out for the same reason (entity survey, feature 6).
- A Tour Station that starts tours on a timer (decision D25 default).
- Vehicle damage stages and a way to fix a damaged vehicle. Deferred, because section 11.12 is silent (entity survey, B22.10).
- Abstract tour data with an entity only near players. Deferred to after Beta (tech survey, B22.28).
- Hitbox entities per vehicle box, custom collision, boost pads, drift, upgrades, part-building and physics bodies (tech survey, B22.29, B22.30).

**Acceptance:**

- A player drives the Jeep Wrangler around a paddock.
- The Ford Explorer completes a laid loop with two stops unattended and carries a passenger.
- The Ford Explorer stops for a dinosaur on the track and starts again when the way is clear (tech survey, spec T13).
- A tour start on a broken loop shows the broken position (tech survey, spec T10).
- The Jeep item does not place a Jeep inside a wall (entity survey, spec S19).
- A drive packet from a player who is not a passenger changes nothing (entity survey, spec S12).
- A parked vehicle sends no movement packets (tech survey, spec T15).
- Both loaders.

---

### B23: Guns (blocked)

**Section:** 11.12, 11.14 · **Size:** unknown · **Depends on:** decision D08 (kinds of gun and which of the 11 models ship)

**In game:** No change until the lead decides D08.

Do not start. Do not re-ask unprompted. When the lead decides, write the module entry here with scope, then the task plan. Known inputs:

- The design document's three kinds: tranquilizer, stun and capture, lethal.
- Ammo sprites for a dart, a taser cartridge and a shotgun shell.
- 11 gun models that need an export.
- A tranquilised state implies a `tranqed` animation on the T. rex card that the file lacks.

The entity survey gives a design direction for use only after D08 (entity survey, feature 8). It is not scope. A dart is a server projectile. Tranquilizer points scale down by body mass. Above the species threshold, a `TRANQUILISED` timer stops movement and behaviours. Capture stores the dinosaur through vanilla entity save in an item. Ammunition sits in a data component that only the server writes.

---

### B24: Art integration

**Section:** 8.5, 8.6, 6.1 · **Size:** L (mostly coordination) · **Depends on:** P2 and the asset repository · **Blocks:** B09, B14, B20, B22

**In game:** Every Beta feature has its own models, textures, animations and sounds. No feature ships with placeholder art that nobody tracks.

**Touches:** `R/assets/projectnublar/**`, `J/registry/SoundInit`, `docs/art-status.md` (new).

**In scope:**

- Import from `ProjectNublar-Assets`:
  - Sounds for the five species with none.
  - Male skins for Gallimimus and Brachiosaurus.
  - Fossil and bone sprites. Meat sprites.
  - Paleontologist skin. GUI tab textures. Voltage signs.
- Blockbench exports to GeckoLib for: feeders (1 to 6, fish feeder), Jeep Wrangler, Ford Explorer, guns (when B23 unblocks).
- An idle-pose export for each machine. B12 draws an idle machine as a static block model. It uses the GeckoLib renderer only while the machine is active (tech survey, spec T5, OC2).
- Idle-pose exports for the four machines feed the B12 static idle models when they land (tech survey, spec T5).
- Conversions from `.dcm`/`.tbl`: basic and large generators, four pylons, egg models.
- Missing animations:
  - Velociraptor `flinch`.
  - Dilophosaurus `getup`.
  - Gallimimus `drink`, `eat1`, `getup`, `dead`, `roar`. The Gallimimus `drink` and `eat1` clips also unblock neck-reach IK, which waits for eat and drink animations (hitbox design).
  - The card's `tranqed`, `injured`, `sleeping`, `sitting`.
  - Rename Triceratops `Sprint ` and `injured` to `run` and `flinch` if they are the same.
- Missing sprites: raptor toe claw, Triceratops horn, teeth, illager skin, Dilophosaurus male skin.
- Artist checklist: box UV, whole-number cubes, root bone pivot at 0, 0, 0, a named head bone, clip names (entity survey, B24.1).
- Glow layers come from a `_glowmask.png` through GeckoLib 5 `AutoGlowingGeoLayer` (entity survey, B24.2). Name glow layers without the `_e` suffix, so ETF does not swap them (tech survey, compatibility checklist).
- Footsteps play in time with the clips through GeckoLib 5 keyframe events (entity survey, B24.3). Gameplay sounds such as roars and calls start from events, not only from keyframes, because Dynamic FPS skips frames (tech survey, compatibility checklist).
- Variant textures go by path in resource packs, with one data line per variant (entity survey, B24.5).
- A species page template has a stat box: health, attack, hitbox, speed, groups (entity survey, B24.4).
- Keep a table `docs/art-status.md` with these columns: asset, Beta module, state (in mod, needs export, needs conversion, no art), owner.

**Acceptance:**

- `docs/art-status.md` has no "unknown" rows.
- Every Beta module's placeholder list is empty or linked to an art task.
- Each machine has an idle-pose export or a linked art task (tech survey, spec T5).

---

### B25: Cleanup and performance pass

**Section:** 6.4, section 10 item 14 · **Size:** M · **Depends on:** all other Beta modules merged

**In game:** No new gameplay. A park of 100 dinosaurs keeps 20 ticks per second. A paddock far from every player keeps its needs, growth and breeding at the normal rate. The server log stays quiet. A player on a weak computer can disable particles, screen shake and glow layers.

**Touches:** the whole `common` tree (cleanup), brain sensors and behaviours (distance band), the client config (low effects), GameTest classes, CI.

**In scope:**

- Remove unused classes: `DinosaurPart` in the loader module, `IDinoPart`, `IMultiPartDino`, `DinoTexture`, and `EntityColorStore` if B16 did not use them.
- Remove unused memory types (15 today) (entity survey, fact 46), and the `DebugStick` chat output.
- Check that no print call remains. B17 removes the 12 calls (tech survey, defect #7).
- Profile a park of 30 animals, 4 machines and 60 posts on a dedicated server. Record the tick time per system. Fix anything over budget.
  - Also profile every budget row at load A and load B (hitbox design). Load A is 20 Gallimimus in one herd, 1 player and 300 other entities. Load B is 100 dinosaurs in 5 herds, 10 of them large species, 3 players and 600 other entities.
  - Budget at load B, all estimates (entity survey, budget): the server total is about 28 ms plus spikes near 440 ms today. After the survey fixes it is about 10 to 16 ms. Herd AI, parts, bend and capsules take 9 to 15 ms. Nobody profiled 26.2 yet.
  - Use SmartBrainLib profiler sections and the logged herd timings (entity survey, B25.2).
- One class holds every slow-work interval. Work is due when game time plus the entity id, modulo the interval, equals 0 (entity survey, new item 29, spec S21):

  | Job | Interval | Basis |
  | --- | --- | --- |
  | Needs and stress | 20 ticks (1 s) | Plan B17 |
  | Group join | 40 or 200 ticks | Local `JoinGroup` |
  | Grazing scan | 40 ticks (2 s) | Estimate |
  | Water scan | 60 ticks (3 s) | Estimate |
  | Fence sensor | 100 ticks (5 s) | Estimate |
  | Reputation decay | 24,000 ticks (1 game day) | Vanilla gossip |

- Dinosaurs far from every player think less often. They still walk at full speed, and their needs change at the normal rate (tech survey, new item 36, spec T9):
  - A sensor every 20 ticks sets a band from the nearest player: 0 within 64 blocks, 1 within 128, 2 beyond.
  - An attack target, a threat memory or a fence contact sets the band to 0.
  - Sensors use `scanRate(e -> base + band × base + (id & 15))`. Idle behaviours use a `cooldownFor` that grows with the band, 100 ticks at band 2 (estimate).
  - The entity survey proposes a simpler rule (entity survey, spec S25). It uses one far distance of 128 blocks, a far factor of 4 and a recheck every 100 ticks. The task plan picks one rule. Both keep needs, growth, stress and breeding gates at the normal rate.
- Check that every gameplay timer uses game time, not a tick count (risk 7, B17 owns the timers) (tech survey, OS4).
- Level unload clears every per-level map: group manager, scent store, feeder registry, power networks (entity survey, new item 29. Tech survey, OM5). No static field holds an `Entity`, `BlockEntity` or `Level` (tech survey, OM6).
- No tick mixin on a vanilla base class and no global per-entity hook (entity survey, B25.5).
- A carcass drop, a leaf-breaking charge or a fence cascade never freezes the server for a tick. Each level keeps a queue of pending units and processes 8 per tick (estimate). Level unload clears the queue (entity survey, spec S23).
- A client config has three low-effects switches: particles (all, reduced, none), screen shake, glow layers. The config loads into static fields on load and on change. Renderers read the fields, never the file (entity survey, new item 29, spec S22).
- A unit test parses every shipped JSON file (entity survey, new item 29).
- Every Beta module has GameTests in common code. P1 moves the existing GameTests to common with one registration class per loader. B25 checks that each module adds its tests there. CI runs the GameTest server task of both loaders (entity survey, new item 30, spec S24).
- Run the optimization-mod test matrix. Use the test from the compatibility checklist for each mod (tech survey, new item 37):
  - Sodium, Iris, EntityCulling, ImmediatelyFast.
  - Lithium, Immersive Optimization (IO), C2ME, Alternate Current, Particle Core.
  - The "Prefer Vulkan" option.
- Check that every custom NBT root has a `"version"` integer (tech survey, OM7).
- Resolve every `TODO(BUG)` from rft that is still open, or change it to a GitHub issue.

**Out of scope:** pathfinding on a thread pool. Measure first (entity survey, B25.10).

**Acceptance:**

- `grep -rn "System.out\|System.err\|printStackTrace" common/src` returns nothing.
- The profile is recorded in the PR, with the tick time per system for the park, load A and load B.
- With IO installed and the player 200 blocks away, needs and stamina change at the normal rate (tech survey, compatibility checklist).
- Every row of the optimization-mod test matrix passes on both loaders (tech survey, new item 37).
- The JSON parse test passes (entity survey, new item 29).
- After a level unload, no per-level map holds an entry for that level (entity survey, new item 29).

---
### B26: Large-body movement and herd travel

**Section:** 11.9, 11.10 · **Size:** XL · **Depends on:** B00, B15, B16 · **Spec:** [docs/dinosaur-hitboxes-and-movement-design.md](dinosaur-hitboxes-and-movement-design.md), sections "Movement" and "Anti-clipping and IK" (planning half), plus its decisions register · **Herd spec:** [docs/superpowers/specs/2026-10-08-herd-movement-redesign.md](superpowers/specs/2026-10-08-herd-movement-redesign.md) · **Added:** 2026-10-10, after the port (P2)

**In game:** Large and long dinosaurs walk through gaps, corridors and forests. They do not spin, wedge or cluster. A herd grazes over a wide area. Every 2 to 5 minutes the leader starts a journey. The herd follows the leader's route as a wedge and changes to single file in corridors.

**Starting point (changed 2026-10-10):** the herd-movement redesign is in the main checkout on branch `kash's-slop`. It is 1.20.1 code and stays uncommitted under the lead's no-commit rule. It is no longer only in the herd-leader worktree. The worktree `.claude/worktrees/herd-leader-sample` is a frozen backup. Never edit it. (entity survey, fact 49)

- The team finished and reviewed herd redesign steps P1 to P13. They cover trail following, context steering, journeys, grazing, spacing, stuck and re-path, animations per state, smoothing and journey pauses.
- 29 herd classes are in `J/entity/ai/behaviour/social/` (entity survey, fact 49).
- An implementer wrote P12c (cliff edges, journey target height limit of 8 blocks), with a new `GroundSupport` class. Nobody has built, tested or reviewed it.
- Playtest 6 ran. The lead has not reported its results yet.
- Handoff: [docs/superpowers/handoffs/2026-10-10-herd-p13-p12c-handoff.md](superpowers/handoffs/2026-10-10-herd-p13-p12c-handoff.md). Ledger: `.superpowers/sdd/2026-10-08-herd-movement-redesign/progress.md`.
- The herd work enters P0 only when the lead commits it (decision D37 default). It ports with P2. Then build the items below on 26.2.

**Lead scope rule (lead, 2026-10-10):** herds must move and behave properly before the mod adds genes, stats, hunger, thirst and attack or flee. Test on the Gallimimus only. The plan defers features that depend on those factors. Today only stamina drives herd behaviour.

**Herd design already decided.** The port keeps these lead decisions from the herd spec. A later decision overrides an earlier one.

- **Travel shape.** A wedge in open ground. It narrows to 1 to 3 abreast, then single file where the ground is narrow. At most 3 abreast. (herd spec, decisions 1 and 10)
- **Slot spacing.** Spacing scales with body size. Slots are personal space + 2 apart in open ground and personal space + 1 in tight ground. A debug-stick toggle sets the tight spacing everywhere. (herd spec, decision 9)
  - Decision 9 gives 5 and 4 blocks for the Gallimimus. The spec's "Travel" section uses 4.2 and 3.2 blocks (personal space 2.2). (herd spec, decision 9 and "Travel")
- **Waiting.** On a roaming journey, the leader waits at most 200 ticks (10 s) for stragglers. The wait is a slow walk at speed 0.6, never a stop. Only followers whose slot is on the trail count. Urgent trips never wait. (herd spec, decisions 2, 21 and 30)
- **Journeys.** Every 2 to 5 min, 156 to 314 blocks, toward a far target. The leader walks in loaded legs of at most 24 blocks. A journey target is at most 8 blocks above or below the herd. (herd spec, decisions 3, 25 and 33)
- **Journey start.** The leader needs at least 75% of its maximum stamina (150 for the Gallimimus). No roaming journey starts from sunset (day time 12000) until morning. Walking drains 25% less stamina. Running is unchanged. (herd spec, decisions 28 and 29)
- **Journey pauses.** A leader rest pauses a roaming journey. After the get-up, the journey continues to the same target. (herd spec, decision 28)
  - In daytime, for a rest of up to 400 ticks (20 s), members keep their slots. Tired members rest lying in their slots.
  - At night, or for a longer rest, members may leave their slots, move apart and rest lying. The wedge forms again when the leader is up.
  - A journey that night catches pauses until morning.
- **Herd area.** The herd area is 6 times the area of the graze squares (radius × √6). Gallimimus: 14.4 blocks for 5 animals, 27.4 for 20. (herd spec, decision 25)
- **Grazing.** A member keeps a spot for 30 to 90 s, then moves a short way with a 50% chance. Members avoid neighbours' spots while grazing only, not while travelling. A member outside the herd area returns to it. (herd spec, decisions 6, 12 and 13)
- **Speeds.** Graze walk 0.6, leader graze move 0.8, journey 0.8, regroup and catch-up 1.2. Ground speed is quadratic in the speed setting, through one helper (`GroundSpeed`). A follower's speed ramps with the gap to its slot. (herd spec, decisions 15 and 19)
- **Spacing.** Hard minimum 2.2 blocks in open ground. 1.7 blocks (`w + 0.5`) in tight ground and beside a resting body. (herd spec, decision 24)
- **Fit box.** The larger of the hitbox and the visible model's width and height, times the growth scale, plus a margin. The margin is 0 per side (Gallimimus box 1.25 wide). Model length is not in the box. (herd spec, decisions 5, 11 and 24)
- **Hitbox.** The Gallimimus hitbox is 1.2 × 2.6. Other species do not change in the herd work. A member outside the herd area walks to its graze spot at speed 1.2. (herd spec, decision 20)
  - B26 replaces this hitbox with the 0.80 × 2.39 movement box (hitbox design, layouts table).
- **Paths and ghosting.** Herd animals use box-aware path nodes. Members pass through herd mates on fallback paths only. Vanilla pushing still keeps bodies apart. (herd spec, decisions 21 and 22)
- **Stuck or waiting.** Blocked by blocks means stuck, and the member asks for a fallback path. Blocked by members means waiting, not stuck. (herd spec, decision 14)
- **Unplanned trips.** A walk target that the herd did not plan and that leaves the herd area counts as an urgent journey. A leader dragged beyond the herd area starts an urgent journey, so the herd follows its route. (herd spec, decisions 16 and 23)
- **Needs and rest on a journey.** A follower leaves the line to drink or eat only at a critical need. On a roaming journey, a follower starts a rest only at critical stamina. On an urgent trip, it rests whenever it is tired. (herd spec, decisions 8 and 17)
- **Animations.** Every animation of a species plays in a fitting state. Calling plays at a roaming journey start. Sentry plays when the leader stands on watch. Startled plays when a threat comes within 6 blocks. (herd spec, decision 26)
- **Smoothing.** An idle sway in code plays for all species. The drop limit stays 3 blocks. A follower stands still while it sits. No idle variants play while the herd travels. A get-up with no animation plays plain idle. Softer easing is the default. No slow-down near walls. (herd spec, decisions 27, 30 and 33)
- **Logs and leash.** The server writes one log line per journey end: the reason, the distance left and the journey length. A leashed leader makes no graze moves. A walk that ends from a rest is not an arrival. (herd spec, decision 31)
- **GameTest limits.** The time limits follow the real ground speeds. They replace decision 18's 1,200 and 1,500 ticks. (herd spec, "Testing")

**Deferred (lead, 2026-10-10):**

- Fight chase. When combat exists, a leader's fight chase is no longer an urgent trip. The herd moves a short way away, waits for the leader and flees if the threat comes toward it. Until then, decision 16 applies unchanged. (herd spec, decision 32)
- The leader's water and food detour on a journey. It waits until needs are part of herd testing. (herd spec, decision 30)
- Other exceptions to decision 16 are open (decision D36). Default: only fight chases are excepted. (herd spec, decision 16. Herd handoff, open question Q6)

**Touches:** `FenceAwareNavigation`, `FenceAwareNodeEvaluator`, `J/entity/ai/behaviour/social/*` (`HerdMoveControl`, `HerdSpacing`, `HerdBodies`, `BodyFit`, `ClearanceBox`, `TravelOrder`, `LeaderTrail`, `JourneyPolicy`, `LeaderJourney`, `ContextSteering`, `FallbackBudget`, `GroundSupport`), `Dinosaur` (growth, dimensions, turning), species config (`group_size`, leaf and log values, pitch values).

**In scope (in this order):**

- [ ] Read 26.2 `PathNavigation.followThePath`, `Path.getEntityPosAtNode` and `WalkNodeEvaluator` from the NeoForge 26.2 and Fabric 26.2 toolchain sources. The arrival-test defect is a fact on 1.20.1 only. (hitbox design, build step 1)
- [ ] A Gallimimus walks through leaves and does not freeze in a canopy. Build one per-species block filter (pass, break, solid). Every block reader uses it: `Entity.collide` (mixin), `BodyFit`, the node evaluator, the sweep shortcut and the IK. Herd and IK code read block shapes directly, never through `noCollision`.
- [ ] A wide dinosaur reaches a path node and walks on. It does not circle or spin. Add the arrival test at the aim point, the box-sweep shortcut and a symmetric footprint.
- [ ] A dinosaur fits any gap that its torso and legs fit. Build the movement box from the render-scaled torso and legs. Drive growth through the 26.2 `scale` attribute, a full `getDimensions` override and the Fossils growth guard. Split the clearance box by job: torso width for paths, whole model for herd spacing. The split replaces today's single `ClearanceBox` (hitbox design).
- [ ] In tight ground a dinosaur stops, turns, then walks. In open ground it slows while it turns. Add a hard turn gate in tight ground, a soft gate in open ground, and the anti-orbit guard.
- [ ] A stuck dinosaur tries cheap recoveries first. Add a stuck ladder that changes the path. Its last rung snaps to a clear spot, also when a player is near.
- [ ] Leaves never stop a dinosaur. Leaves pass or break per species config. Only the main box breaks leaves, above a growth cut-off. Large species break logs only when charging. With `mobGriefing` off, breakers pass through leaves and logs stay solid.
  - Vanilla and decorative fences hold dinosaurs as walls. They are not in the breakable list unless a data pack adds them (decision D35 default). (tech survey, O21)
- [ ] Herd mates keep a gap from snout to tail. Add capsule herd spacing with a circle broad phase. Set the in-line pitch by the chest rule. Use today's tight spacing in corridors and for big herds. The trail grows with the herd's single-file column. Animals past the trail tail follow the animal ahead.
- [ ] The leader prefers corners where its full length can turn. Add corner clearance as a soft path cost. Add a spine danger term in follower steering.
- [ ] An adult Brachiosaurus stays in open ground and does not enter forests. Allow at most one large-species path per herd per tick. (hitbox design)
  - Its movement box is 3.68 × 8.87 blocks. Every trunk stops it, because logs hold for calm animals.
  - Its node volume is 4 × 9 × 4 = 144 cell reads per node. One path costs about 5 ms (estimate).

**Also in scope (any order):**

- [ ] Far herd members still get a fallback path when an optimization mod skips ticks. Make `FallbackBudget` tolerate gaps between asks. Set stall thresholds above 20 ticks, or scale them by elapsed time. (tech survey, new item 38, defect #9)
  - Cause: a queued request expires one game tick after the last ask. A throttled member asks every 2nd to 20th tick (`J/entity/ai/behaviour/social/FallbackBudget.java:30`).
- [ ] The herd tick makes less garbage. Use one roster, primitive returns, one `CollisionContext` per probe batch and an index map for the gather. (entity survey, new item 31, B26.2)
- [ ] Trailing dinosaurs are not pulled back toward mates behind them. Add view cones for separation and cohesion as steering terms. Cohesion ignores mates behind. (entity survey, new item 31, B26.1)

**Produces:** `BlockFilter` per species, the shared per-species spine definition, the fixed navigation.

**Acceptance:** GameTests on both loaders, registered in common code:

- A dinosaur of whole-number width advances through every node.
- A Gallimimus walks under a 3-high canopy.
- A 20-animal herd passes a 2-wide corridor in single file and regroups after it.
- A growing animal next to a fence never clips into it.
- A stuck animal reaches the snap rung and continues.
- A far herd member with a gap of up to 20 ticks between asks still gets its fallback path. (tech survey, new item 38)
- The server tick stays inside the spec's performance budget. Record the profile in the PR. All budget values are estimates. (hitbox design, "Performance budget")
  - Load A, 20 Gallimimus in one herd: 2 to 3 ms total server.
  - Load B, 100 dinosaurs in five herds: 9 to 15 ms total server. Repath spikes come on top.
  - Herd AI, 20 animals: under 1 ms per tick on average, under 5 ms in any single tick. (herd spec, "Performance budget")

---

### B27: Section hitboxes and IK

**Section:** 11.9 · **Size:** XL · **Depends on:** B26, B09 · **Spec:** [docs/dinosaur-hitboxes-and-movement-design.md](dinosaur-hitboxes-and-movement-design.md), sections "Hitboxes", "Anti-clipping and IK" and "Cross-loader implementation" · **Added:** 2026-10-10, after the port (P2)

**In game:** Arrows and melee hits land on the visible head, neck, torso, legs and tail of every species. A head hit hurts most. The attacker keeps kill credit. Necks and tails bend away from blocks. Feet sit on the ground. A bite hurts only what is in front of the dinosaur.

**Touches:** `Dinosaur`, `J/entity/dinosaur/PartEntity` and both `DinosaurPart` classes (removed first), the species renderers, `EntityInit`, new part, pose-state and bake classes, a Gradle bake task, NeoForge and Fabric loader modules.

**In scope (in this order):**

- [ ] Remove the dead T. rex head-part code: both `DinosaurPart` classes and the renderer writes to `DINOSAUR_HEAD_POS`.
- [ ] A calling Gallimimus raises its head, and the head part rises with it. Add a synced pose state: animation id, start tick and loop flag. The server owns it.
  - A player who arrives during a roar sees the roar at the right point. The spawn data carries the pose state. On spawn, the client starts the clip at the elapsed time. The server sends a death clip again after a chunk reload. (entity survey, new item 32, spec S26)
- [ ] Hits land on every visible section. Add section parts per the spec's layout table: square footprints, two torso parts, one part per leg, chained necks and tails. Sizes come from the render-scaled posed model. No arm parts and no Dilophosaurus frill part.
  - Part counts: 11 to 17 per species. Gallimimus 12, T. rex 13, Triceratops 12, Brachiosaurus 17, Velociraptor 12, Dilophosaurus 11. (hitbox design, "Layouts for the six species")
- [ ] A `NoAI` exhibit can be hit, and parts never sit at 0, 0, 0. Placement uses the rest pose, yaw history and baked pose. Run it on both sides every tick and on load, including for `NoAI` animals.
- [ ] A hit on any part counts on NeoForge and on Fabric. Use NeoForge `PartEntity`, and a Fabric part class with the mixin set. Copy the Fabric mixin set from the MoreHitboxes Fabric design. Align IDs as `parentId + i + 1` from a reserved block. (hitbox design, "Parts per loader")
  - Port rules: set part IDs before the dinosaur starts tracking. Never swap part objects or IDs while the game tracks the dinosaur. (hitbox design, "Port rules")
- [ ] A head hit hurts more than a tail hit. A tail in lava does not burn the dinosaur. Set damage multipliers: head ×1.5, neck ×1.25, torso ×1.0, legs ×1.0, tail ×0.5. Parts filter environmental damage and forward the same `DamageSource`. The parent box is not pickable. Parts pass right-clicks to the parent.
  - A bite or tail swipe hurts what is in front at the moment the clip lands. Each attack clip has one hit tick. At that tick, the server keeps candidates inside the reach and inside half the arc from the body yaw. Each kept candidate takes damage once. Parts only receive hits. They never deal hits. (entity survey, new item 32, spec S27)

    | Value | Start | Basis |
    | --- | --- | --- |
    | Arc | 90°, per species | Inference (entity survey, spec S27) |
    | Turn toward the target before the hit | 30° per tick at most | Mowzie's Mobs (entity survey, spec S27) |
    | Reach | Species attack reach | Plan B00 (entity survey, spec S27) |

- [ ] A creeper, a sweep or a splash potion near any part reaches the dinosaur once. Explosions find parts. Sweeps and potions return the parent once. The range test measures to the nearest part.
  - Add the typed-query hook on both loaders. NeoForge's native typed loop uses `tryCast`, so a part fails a `LivingEntity` test without the hook. (hitbox design, "Parts per loader")
- [ ] The server keeps its tick time with many dinosaurs loaded. Add chunk-section part buckets on both loaders, including a NeoForge `Level.getEntities` mixin. Use fewer parts for animals far from players.
- [ ] Hit parts follow the real animations. A Gradle task bakes the animation files into tables inside the mod jar. Both sides load them directly. Add a hash check on join, a golden test against recorded bone positions, and a build failure when an animation is newer than its bake.
- [ ] Near a trunk, the tail bends away on the server and on every client, to the same side. Bend the neck and tail on the server tick within joint limits, with a synced bend-side byte. The GeckoLib 5 updater adds the bend delta, lerped by partial tick.
  - Changed 2026-10-10: the entity survey resolves risk R3. GeckoLib 5 bone updaters run after the controllers, so an added bend delta works. (entity survey, fact 35)
  - The hitbox design still lists R3 as untested. A playtest still checks for double sway and 20 Hz steps. (hitbox design, test 19)
- [ ] Feet sit on uneven ground and steps. Add client leg and foot IK: once per tick, lerped per frame, full within 16 blocks and none past 32. Use the CHAOS branch IK code with its sources. Give credit as the Mowzie's Mobs terms require.

**Produces:** the part entities per loader, `PoseState`, the bake tables and their loader.

**Acceptance:**

- An arrow at a Brachiosaurus neck hits the neck part and damages the animal ×1.25.
- A creeper next to a T. rex tail damages the T. rex.
- A sweep across a tail reaches the animal.
- The head part follows a calling Gallimimus's raised head.
- A Brachiosaurus tail bends the same way on server and client.
- Parts appear for a `NoAI` animal on the first tick.
- Melee and arrows on every part resolve on a dedicated server. (hitbox design, test 11)
- A tail in lava does not burn the dinosaur. (hitbox design, test 14)
- The bake golden test passes within 0.1 blocks. A client with a different bake hash cannot join. (hitbox design, tests 15 and 16)
- A player who arrives during a roar sees the clip at the elapsed time, not from its start. (entity survey, spec S26)
- A dinosaur's bite does not hurt a mob behind it. (entity survey, spec S27)
- All of these run on both loaders.
- 100 dinosaurs keep the part scan inside the spec's budget.

---

## Phase P4: Beta hardening and release

**In game:** No new gameplay. Two players who did not build the mod play the full survival loop on each loader. The mod runs next to the common optimization mods. The Beta jars ship from a tag.

- [ ] Two people who did not build the mod playtest the balance of the full survival loop on each loader. Change TOML defaults only. Record each change and its reason in `docs/config-reference.md`.
- [ ] Check the "about 50 minutes per dinosaur" and "about 25 fossils on an iron filter" targets. Report the actual values to the lead.
- [ ] Run the optimization-mod test matrix before release, on the loaders that each mod supports. (tech survey, new item 37)
  - Sodium, Iris, EntityCulling, ImmediatelyFast, Lithium
  - Immersive Optimization, C2ME, Alternate Current, Particle Core
  - "Prefer Vulkan"
- [ ] Optional: the CC: Tweaked park peripheral on the fence switch and a park tracker, after B15 (decision D34 default). A player builds a Lua status wall. The peripheral pushes events on change only and uses one summary call. (tech survey, O20, C.2)
- [ ] Licence (decision D09): the lead chooses LGPLv3 or CC0 and states the art terms. Then set `LICENSE`, `gradle.properties`, `mods.toml` and `fabric.mod.json`.
- [ ] Translations: complete `en_us`. Invite community translations after Beta.
- [ ] Publishing: enable CurseForge and Modrinth in `gradle.properties` with real project ids. A release workflow builds and uploads both jars from a tag.
- [ ] Update `docs/codebase-analysis.md` sections 1 and 4 to the Beta state. Alternatively, replace that document with a shorter architecture overview and archive the analysis.
- [ ] Tag `v1.0.0-beta.1`.

---

## Release 1 modules

Release 1 content that the lead decided:

- plants and wood sets (sections 11.12 and 8.6)
- the petrified tree (section 8.6)
- the species from the Release Plan that are not in Beta

Everything else that is "not in Beta" has no release. See "Unassigned backlog".

### R01: Plants and wood sets

**Section:** 11.12, 8.5, 8.6 · **Size:** L · **Depends on:** Beta released

**In game:** Five new plants grow in the world and on farms. Four new wood sets and a petrified tree give new building blocks. Herbivores eat the new plants.

**In scope:**

- Five plants from the Release Plan and the board: Cycad, Serenna Veriformans, West Indian Lilac, Heliconia, Horsetail. Each gets growth, placement, worldgen in listed biomes and drops.
- Four wood sets (logs, planks, stairs, slabs, fences, doors, trapdoors, signs) and the petrified tree. The petrified tree has no art yet. Flag it.
- Herbivore diet tags extend to the mod's plants. Plant matter recipes use them.
- Paleobotanist trades (decision D10).

**Acceptance:**

- Each plant generates in the world.
- Each plant can be farmed.
- A Triceratops eats a Cycad.
- All of these pass on both loaders.

### R02: Release 1 species

**Section:** 7.3 (Release Plan), 8.5 · **Size:** XL per species · **Depends on:** Beta released, B16 to B19

**In game:** Four new species hatch, live and behave like the Beta species. The Mosasaurus lives in water.

**In scope:** Compsognathus, Microceratus, Parasaurolophus, Mosasaurus. Each species needs:

- a stat card from the lead, or interim values with a stated basis
- a species JSON
- a fossil set
- a model and animations
- skins, sounds, diet, prey preferences and AI particulars

Asset status: Parasaurolophus is the most complete in the asset repository. Compsognathus has a model and six animations. Microceratus and Mosasaurus have no listed model.

**Mosasaurus** needs aquatic movement, an aquatic enclosure rule and a water feeder. Fences do not apply underwater. Treat it as its own XL module `R02d` and schedule it last.

**Acceptance per species:**

- The Beta acceptance lists for B09, B16, B17 and B18 pass for the new species.

### R03: Release 1 systems

**In game:** Each new species has fossils to find and digsites that hold them.

Systems that Release 1 content implies but nobody decided:

- A fossil set and digsite entries for each new species (B01 and B21 data only).
- Gene data for any vanilla animal that Minecraft adds after 26.2.

### Unassigned backlog

Not in Beta per section 11.13, and not assigned to Release 1 by the lead. The lead assigns each one. This plan does not.

- Pteranodon (the Release Plan says Release 2)
- the tablet and the DNA server
- hybrids and false eggs
- gates, the overhanging fence, backup generators
- trace fossils, generic fossils, terracotta host stone
- the drill extractor
- turf wars and movement-based vision
- the "Dino Feed" item
- the mod's own frog and goat and other non-dinosaur animals
- aircraft, boats, gyrosphere, monorail
- the enclosure marker and park signs
- diseases and treatment
- the skeletal builder and skeleton exhibits
- JEI or similar recipe integration, a guide book
- the tracking beacon

**Candidates from the tech survey (not decided).** These are recommendations for the day the lead assigns them. None is a lead decision. (tech survey, "Post-Beta")

- Security cameras. A camera in an unloaded area shows "no signal". No forced chunks. (tech survey, SEC.1, SEC.2)
- A CCTV wall that shows a snapshot every 2 to 5 s, not a live picture.
- Keycards. A lab door opens for a level-3 card. Keycards wait for gates. (tech survey, SEC.5)
- A Tour Station that starts tours on a timer (decision D25 default: no Tour Station in Beta).
- Dormant tours as data.
- A battery block.
- A power meter block (decision D27 default: no meter block in Beta).
- A storm event that makes fences flicker.
- Control-room redstone channels (decision D28 for the Beta control room).
- A voice radio for rangers, installed by the player only.
- Ponder tutorials or a guide book (decision D34 default: Ponder waits for a 26.2 build, the guide book stays in the backlog).
- Create integration (decision D34 default: waits for a 26.2 build). (tech survey, C.4 to C.7)
- The drill extractor for amber (already listed above).

---

## Decisions register

The implementation needs these decisions, and section 11 does not give them. The lead answers. The module's task plan records the answer. Do not start the dependent step until the lead answers. If a default is listed, ship the default as a config value.

D15 to D35 come from the open items of the two surveys (O1 to O21). Their defaults are survey recommendations, not lead decisions.

| Id | Module | Question | Default if unanswered |
| --- | --- | --- | --- |
| D01 | B02 | How much more DNA does a special piece give than a bone of the same quality? | Multiplier 2.0 in TOML |
| D02 | B03 | What happens when no species is valid for an amber's depth and biome? | Nearest depth band |
| D03 | B04 | Does sampling an egg with a syringe harm the egg? | No |
| D04 | B04 | What is the yield range for a syringe on a live dinosaur and on remains? | 25 to 50% live, 50% remains |
| D05 | B11 | Hatching: egg block or egg entity? | Block with block entity |
| D06 | B16 | Multipart hitboxes in Beta, or one scaled box? | Answered 2026-10-09: section parts in Beta (module B27). See [docs/dinosaur-hitboxes-and-movement-design.md](dinosaur-hitboxes-and-movement-design.md). |
| D07 | B22 | Do vehicles use fuel? | No fuel |
| D08 | B23 | Which kinds of gun, and which models ship? | Blocked |
| D09 | P4 | Licence and art terms | Blocked. Do not change files. |
| D10 | R01 | Does the paleobotanist villager ship in Release 1? | Not shipped |
| D11 | B09 | Approve the render-scale targets (Triceratops 3.0 blocks, Brachiosaurus 12.5) and the section 11.16 missing-stat draft | Use the targets. Label them as interim. |
| D12 | B16 | Real stat cards for five species | Section 11.16 interim values |
| D13 | B00, B19 | Flee or stand for Tyrannosaurus, Velociraptor, Dilophosaurus and Brachiosaurus? `stress_threshold` and `territory_radius` per species? | Stand for the three carnivores, flee for Brachiosaurus. One shared stress threshold and radius, set with their basis in the B00 task plan. All as data. |
| D14 | B09 | Animation speed that follows body size, and look-around clips on the second controller (review items ANI-6, ANI-2) | Not built. The lead answered "not decided" on 2026-10-06. |
| D15 | B19 | How long does an awareness episode last before a dinosaur rolls again for the same player? | 200 ticks (10 s), `detection.episode_ticks` (entity survey, O1) |
| D16 | B19 | Which sources drop scent points, at what spacing, and with what cap per chunk? | Players and hurt mobs, 2-block spacing, 32 points per chunk, all config (entity survey, O2) |
| D17 | B11 | Does a hatchling get an owner? | No owner. Taming stays the only bond. (entity survey, O3) |
| D18 | B19 | Do owners who are not operators get a status readout of their dinosaur? | No readout. The debug stick stays as decided in B16. (entity survey, O4) |
| D19 | B19 | Do vanilla villagers and livestock flee loose dinosaurs? | Off, `vanilla.mobs_fear_dinosaurs` (entity survey, O5) |
| D20 | B18 | Does breeding stop above a dinosaur count per area? | 0 (no cap), config (entity survey, O6) |
| D21 | B14 | What powers a fence when its generator's chunk is not loaded? | Only loaded generators count. Fences with no loaded supply show unpowered. No chunk tickets. (tech survey, O7) |
| D22 | B14 | How long does a fence stay live after fuel ends? | A buffer equal to the sum of post buffers (350 FE each today), as a config value. The fence fades over seconds. (tech survey, O8) |
| D23 | B15 | Does shock strength follow the grid's energy, and does a shock drain the grid (tech survey, spec T3)? | No. Damage stays at `fences.damage.<tier>`. Shocks draw no energy. (tech survey, O9) |
| D24 | B14 | Which blocks accept energy from, or give energy to, other mods' cables? | Generators give. Machines accept. Posts, pylons and conduits are closed to outside cables. (tech survey, O10) |
| D25 | B22 | What stops the Explorer? How many seats? May a player board a moving Explorer? Is a timed Tour Station in Beta? | Stops for all dinosaurs and players (tag). Jeep 2 seats, Explorer 4. Boarding at a stop only. No Tour Station: the driver starts the loop. (tech survey, O11) |
| D26 | B22, B15 | What happens when a vehicle meets an electric fence? | Vanilla collision stops it. No shock to the vehicle or riders. (tech survey, O12) |
| D27 | B12, B14 | Is the hover box always on? Is a power meter block in Beta? | Always on, with a client switch. No meter block. Generators give comparator output. (tech survey, O13) |
| D28 | Backlog | What does the control room have in Beta? | The switch block, comparator outputs and the alarm block (tech survey, spec T14). Cameras, status board and ownership wait. (tech survey, O14) Owner if accepted: B15 (tech survey, spec T14). |
| D29 | B00 | Which systems may a server owner disable? | `vehicles`, `herd_travel`, `breeding`, all on (tech survey, O15). Changed 2026-10-10: `fence_wire_health` removed, because fence parity removed wire health |
| D30 | B07 | Are gene definitions datapack JSON in Beta? | No. A Java registry with the rebuild function in Beta. JSON after Beta. (tech survey, O16) |
| D31 | B01 | Where do species and piece of a fossil live? How many opacity steps does the overlay have? | One block per host stone and species. Piece, quality and biome group as properties, about 4,600 states in total. 4 opacity steps. (tech survey, O17) |
| D32 | B06 | What is the largest DNA drive the design expects? | Design for 150 entries. Test at 1,000. (tech survey, O18) |
| D33 | B21 | Which distance does the game guarantee for a digsite? | Vanilla `/locate` radius of 1,600 blocks (100 chunks). It replaces the plan's 2,000. (tech survey, O19) |
| D34 | Post-P2 | Does the roadmap include optional compat modules? | CC: Tweaked peripheral after B15. Create and Ponder wait for 26.2 builds. The guide book stays in the backlog. (tech survey, O20) |
| D35 | B15, B26 | Do vanilla and decorative fences hold dinosaurs? | Yes, as walls with vanilla collision. Not breakable unless a data pack adds them. (tech survey, O21) |
| D36 | B26 | Which other unplanned trips are exceptions to herd decision 16, besides fight chases? | Only fight chases are excepted. (herd handoff, open question Q6) |
| D37 | P0 | How does the uncommitted herd work on `kash's-slop` enter P0, under the lead's no-commit rule? | Blocked until the lead commits it. P0 does not start without it. (herd handoff, 2026-10-10) |
| D38 | B15 | Who can climb an unpowered wire? | Answered 2026-10-10 ("1:1"): every living entity, as in 1.12.2 (fence parity F26) |
| D39 | B15 | Do post lamps light post by post or at once? | Answered 2026-10-10 ("1:1"): post by post, as in 1.12.2 (fence parity F31) |

**Design alternatives.** The entity survey lists 16 design alternatives in its section "Gameplay loops, pacing and design alternatives". They are options for the lead. None is decided, and none changes a decision above.

---

## Self-review against the spec

**Pass of 2026-10-05**, against section 11:

- 11.1 fossils: B01, B02. 11.2 amber and living sources: B03, B04. 11.3 digsites: B21. 11.4 processor: B05. 11.5 sequencer: B06.
- 11.6 gene editing: B07, B08, B09, B10. 11.7 synthesis to hatch: B11. 11.8 machine framework: B12, B13, B14 (conduit, other mods' cables).
- 11.9 dinosaurs: B16, B24. 11.10 needs and behaviour: B17, B18, B19. 11.11 power and fences: B14, B15. 11.12 feeders, vehicles, guns: B20, B22, B23.
- 11.13 not in Beta: unassigned backlog. 11.14 open: D08, D12. 11.15 no-code list: every line maps to a module above. 11.16 stats: B00. 11.17 config and platform: B00, P2.
- Gaps found and fixed in this pass:
  - Pylons had no owner. They went to B14.
  - Voltage signs had no owner. They went to B15.
  - The egg-sampling source had no owner. It went to B04.
  - "Feeders fillable from outside" needed both B15 and B20. Both modules note it.
  - The fence switch block had no recipe owner. It went to B13.
- Known weakness: module sizes are judgement, not measurement. Estimate them again after the first tier-1 module lands.

**Pass of 2026-10-10:**

- Inputs folded in:
  - the entity survey (`reports/Reference mod survey for Beta.md`)
  - the tech survey (`reports/Optimization and tech mod survey for Beta.md`)
  - the hitbox design (`docs/dinosaur-hitboxes-and-movement-design.md`)
  - the herd spec (decisions 1 to 33) and the herd handoff of 2026-10-10
- New scope reached the global constraints and risks, P0 to P2, B00 and B01. It also reached B06 to B27, P4, the backlog and this register.
- The other text got the STE-MC style only.
- The register grew from D14 to D37. D15 to D35 are survey defaults, not lead decisions.
- Facts that changed: `BrainUtils` has 30 users, not 22. B16 has 15 unused memory types, not 12. The digsite distance is 1,600 blocks, not 2,000 (D33 default).
- Known weakness: every survey cost and budget value is an estimate. Nobody profiled 26.2. Profile each budget row after its module lands.
- Known weakness: playtest 6 results and the P12c review are still pending. B26 may change after them.
