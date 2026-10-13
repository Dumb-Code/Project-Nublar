# Project Nublar Beta plan: engineering backlog

Mirror of the shared page sub-tab "Engineering backlog" under "Beta and Release 1 project plan". Back to the [roadmap](../project-plan.md).

Date: 2026-10-12 (rebuilt by milestone). Status: draft for the lead's review. Nothing in this backlog is started. No work package is merged.

> **For agentic workers:** this file is the master backlog. It sets milestones, work packages, scope and order. Do not implement from this file alone. When you take a work package, first write its detailed task plan ("How to use this plan", step 3). Then implement it with `superpowers:subagent-driven-development` or `superpowers:executing-plans`. Steps use checkbox (`- [ ]`) syntax.

## Status on 2026-10-13

- **Nothing is started.** No milestone or work package is merged.
- **The lead committed the herd work.** The herd and movement redesign is on 1.20.1, on branch `kash's-slop`. The lead committed it on 2026-10-11 in commits 4e90174 and 582fbb0. This resolves D37, so nothing blocks M0.P0. The worktree `.claude/worktrees/herd-leader-sample` keeps 65 uncommitted files as a frozen backup. Never edit it.
  - Herd steps P1 to P13 are done and reviewed. 29 herd classes are in `J/entity/ai/behaviour/social/` [entity survey fact 49].
  - Step P12c (cliff edges) is written but not built, tested or reviewed. Playtest 6 ran. The lead has not reported its results.
  - Handoff: `docs/superpowers/handoffs/2026-10-10-herd-p13-p12c-handoff.md`. Ledger: `.superpowers/sdd/2026-10-08-herd-movement-redesign/progress.md`.
- **Inputs folded in.** Entity survey, tech survey, hitbox design, herd spec, JC3 survey, reconciliation (61 of RC1 to RC66), and on 2026-10-12 the design requirements catalogue, the 2022 cloning video parity reference, the original machine animation spec, the Art list and the skin layer audit.
- **Survey items are recommendations.** They are plan scope, not lead decisions. A "Dnn default" applies until the lead answers that register row.
- **Spec.** `docs/codebase-analysis.md` section 11 is the design that the lead confirmed. If this plan and section 11 differ, section 11 wins and this plan has a bug.
- **Lead decisions of 2026-10-12** (all binding):
  - Cloning is 1:1 with the 2022 Cloning Guide video (https://youtu.be/W9cMQUB4EdM) in behaviour: screen contents, flow, timings and what each machine does. Where `_Cloning Process.docx` ("the spec") or a section 11 decision differs from the video, the spec or the decision wins.
  - Machine looks are not part of parity [lead 2026-10-12]: the machine models and textures changed since 2022. The current models and textures in the mod are the target (`geo/block/*.geo.json`, `textures/block/*.png`, `textures/gui/*.png`). Animations drive the bones of the current models. A screen matches the video in its contents (slots, gauges, tabs, buttons, tooltips) and uses the current GUI texture for its art.
  - The Egg Printer and the Incubator animate in code. The original 1.12.2 and 1.16 behaviour is the reference. Do not copy its bugs: client-only state and frame-rate timing.
  - No stand-ins. An asset counts only when it is the right one for that thing. A copied file counts as missing. Work-in-progress code shows the current state only. It never proves that a requirement is met.
  - Animations per species are the 2020 board's clip list for that species plus every clip that a Beta behaviour needs.
  - All Dino VFX are in Beta. The Beta skin sets are complete with every layer for both genders.
  - Animals feed together. Every feeder is shared by all animals of its diet. Animals take feeding spots around its edge, and the feeder's size and their body widths set how many eat at once. The rest wait and take turns: dominance first within a group, then waiting time (D57, all species). A herd gathers around one tree or plant patch, and each animal claims a different block in its reach. Carcasses use the same spot model. Owner: F2.0, with F2.4, F2.5 and F3.3.
  - Beta has a contentment score: one 0 to 100 readout of needs, stress, health and safety, with effects of its own (D109). Stress stays the gate for breeding, taming and aggression. Readouts: a hover box for every player (band and worst need), visible cues, and exact numbers on the Debug Stick. This answers D18. Owner: F2.0.
  - The plan states how genes change behaviour: spec F2.0 "How genes change behaviour". The lead accepted most D112 effect sizes in the M2 pass. Owner: F2.0 with F1.5.
  - Herbivores graze when content or idle, and each idle graze adds a little hunger. By default the block stays, so a content herd does not strip its pasture. A hungry animal still eats and removes the block. Owner: F2.0 with F2.4.
  - M1 open-items pass: the lead answered most M1 open items. The register rows D05, D41, D78, D80 to D85 and D93 hold the answers, and D113 and later add the rest. Owners: F1.1 to F1.12.
  - M2 open-items pass: the lead answered most M2 open items and three M1 follow-ups (D140 in part, D145, D148). The register rows hold the answers, and D149 and later add the decisions that had no row. Owners: F2.0 to F2.7 and M0.B00. Reversals:
    - Beta has no scent trails. A predator that loses sight of its target searches the last seen spot.
    - The territory radius becomes a personal-space radius plus "young provoke always". Beta has no home territory (D166, 2026-10-13).
    - The Brachiosaurus defends with a tail whip and a stomp. It does not flee.
    - T. rex, Velociraptor and Dilophosaurus are cathemeral, with their own windows. The Gallimimus is an omnivore.
    - Every Velociraptor hatches with hyper intelligence. A hyper-intelligent animal climbs out at a dead fence section, as a lead-approved exception to fence parity.
  - M3 open-items pass: the lead answered most M3 open items. The register rows D21 to D24, D26, D27, D35, D78 (gap 1) and D86 hold the answers. D183 and later add the rest. Owners: F1.11 and F3.1 to F3.4. Reversals:
    - The voltage sign is the parity empty-hand click. It has no item and no recipe.
    - The fence gets a hum loop, a shock zap and a wire snap, as additions to parity.
    - The grid remembers power while a generator's chunk is not loaded (D21).
    - Beta has no comparator output and no power meter block (D27).
    - A live fence stops a vehicle and shocks the driver once (D26).
    - Every feeder is an in-ground hatch that opens on a timer. Beta has two carnivore feeders, four herbivore feeders and the Live-Bait Feeder.
  - M4 and follow-up open-items pass: the lead answered most M4 items (F4.1, F4.2) and most follow-ups of the earlier passes. The register rows hold the answers. D204 and later add the decisions that had no row. Owners: F1.1 to F4.2 and M0.B00. Reversals:
    - Lost sleep gives no stress (D105). The hover box shows icons only (D149).
    - A strong jump can clear the low-security fence (D47). Breeding has two more gates (D72).
    - A Dilophosaurus group hunts together (D54). Hungry packs journey (D56). The hurt-place memory lasts 1 in-game day (D61).
    - Carnivores stand or retreat by threat and group size (D13). A fast vehicle hurts what it hits (D75).
    - The Wire Spool shows 10 stages, not 12 (D184). A sneak-click removes a voltage sign (D216).
    - Only the breeding switch ships (D29). Feeders 3 and 4 ship (D217). Every feeder screen gets timer buttons (D194).
  - Final open-items pass and draft reviews (2026-10-13): the lead answered the last open items and approved Claude's seven drafts with changes. The drafts are the recipes, the power numbers, the trades, the fossil generation, the clutches, the skin sets by biome and the cathemeral herbivore windows. D219 and later add the decisions that had no row. Owners: F1.1 to F4.2 and M0.B00. Reversals:
    - Loneliness is part of stress, with no bar of its own (D52). This replaces "the social need is a need of its own" [s11.10].
    - The female builds a nest from vanilla blocks of her surroundings (D152). Eggs count as young (D219).
    - Dye tints a vehicle, and a new Wrench item changes its variation (D204). Vehicle damage, wrecks and repair wait for a later release (D206).
    - A herd member detours only at an urgent need (D36). The need cooldown is 5 minutes (D160).
    - The pylon head sits on top of a fence pole (D240). An overload trips the generator (D242).
  - Last open items (Z-8, 2026-10-13): the lead answered every open row except D08 (guns) and D14 (animation speed). D91 waits on the artist. Owners: F1.1, F1.2, F1.4, F1.12, F2.0, F2.2, F2.5, F2.7, F3.2, F4.1 and F4.2. Reversals:
    - The player assembles each vehicle from parts, like a lab machine (D250). This replaces "one Jeep item and one Explorer item".
    - The four lab machines alone cost about 45 iron value and 4 gold ingots. The whole first setup costs about 60.
    - The cocoa allergy quirk is not in Beta (D112).
    - Beta has no Identification Tag. An amber tube stays Unidentified until the Sequencer sequences it (D83).
- **Master copy.** The "Engineering backlog" tab of the shared Codebase Analysis page is the master copy. This file mirrors it. The overview tab holds Beta at a glance, the gameplay loop, the player journey and the species roster. The Art list tab tracks every art asset.

**In game:** a playable Beta of Project Nublar runs on Fabric and NeoForge for Minecraft 26.2. Release 1 follows and adds plants and four species.

**Architecture:** one MultiLoader project with the modules `common`, `fabric` and `neoforge`. Game logic lives in `common`. Each loader module holds only registration glue, platform services, datagen and the client entrypoint. Balm carries energy and fluid storage. Species data, fossil sets, diets and gene effects are datapack JSON. Global balance numbers are one TOML file.

**Tech stack:** Java 25, Minecraft 26.2, Fabric Loader and Fabric API, NeoForge, GeckoLib 5, SmartBrainLib 2, Architectury API, Common Networking, Forge Config API Port, Balm (storage only), Gradle 8 with ModDevGradle and fabric-loom, JUnit 5, the vanilla GameTest framework.

**Paths:** `J/` is `common/src/main/java/net/dumbcode/projectnublar/`. `R/` is `common/src/main/resources/`. `F/` is the Forge module, `neoforge` after M0. `MOD` is `R/assets/projectnublar`. `REPO` is the asset repository `Dumb-Code/ProjectNublar-Assets`. Line numbers refer to commit 4244f62 and go stale after M0.

**Source tags:**

| Tag | Source |
| --- | --- |
| [s11.6] | `docs/codebase-analysis.md` section 11.6 (other numbers alike) |
| [lead 2026-10-12] | a lead decision of that date |
| [entity survey n12], [entity survey S13], [entity survey B11.1] | Reference mod survey: new item, specification, item id |
| [tech survey n13], [tech survey d5], [tech survey T4] | Optimization and tech mod survey: new item, defect, specification. O, OS, OW, ON, OC and OM ids as in the survey |
| [JC3 J16], [JC3 B12.26] | JurassiCraft 3 survey: specification, item id |
| [recon RC43] | AI, behaviour and genetics reconciliation |
| [herd spec d9] | herd movement redesign spec, decision 9 |
| [hitbox design] | dinosaur hitboxes and movement design |
| [F29] | a row of the 1.12.2 fence parity table (F3.1) |
| [video 4:12] | 2022 Cloning Guide video, at m:ss |
| [spec] | `_Cloning Process.docx` |
| [design doc] | `Project_ Nublar (2).docx` |
| [commission] | `Mod Commission.docx` |
| [card] | a species card (`Tyrannosaurus.docx`, `Velociraptor.docx`) |
| [orig 1.16] | the original mod's behaviour, from the clean-room spec `original-machine-animations.md` |
| [2020 board] | Trello board HuHwtk2g, "project-nublar" |
| [Dino VFX] | `Dino VFX.txt` |
| [GH#23] | GitHub issue 23 on Dumb-Code/Project-Nublar |
| [inf] | an inference, not a source fact |

Survey tags keep each survey's own item ids, for example [entity survey B12.3] or [JC3 B12.26]. These ids are not old module codes. Each work package names its old module code once, in its "Old module" line.

---

## Global constraints

Every work package includes these rules.

- **Loaders.** Fabric and NeoForge. Both must be playable at the Beta release. No single-loader shortcut [s11.17].
- **Version.** Minecraft 26.2 and Java 25. Check loader and library releases again before the port starts [s11.17].
- **Config split** [s11.17]:
  - TOML for global balance numbers (yields, times, chances, power).
  - Datapack JSON for per-species data (stats, fossil sets, diets, gene effects).
  - Tags for the items of other mods.
  - If the split is not clear, pick the simpler format.
- Every number that the lead marked "tune in playtest" is a config value with the stated default. Never hard-code it.
- Block and item ids can change. No world-compat shims. Apply the D251 renames, and fix the block entity id `carrnivore_feeder_one` [s11.17].
- **Stats.** The Tyrannosaurus card values are approved. The interim values of [s11.16] are the working values for the other five species. Do not invent stats. State the basis of every number.
- **Numbers.** Do not invent a number that no source gives. Write "not set" and add a register row.
- Dinosaurs never spawn naturally. Cloning is the only source [s11.9].
- Do not open again, unprompted, the decisions that the lead marked "not decided" (guns, licence). Do not change `LICENSE` or the `license` value until the lead decides.
- **No stand-ins** [lead 2026-10-12]. An asset counts only when it is the right one for that thing. A Tyrannosaurus roar is not a Velociraptor sound. A clip from another species, or a byte copy of another file, counts as missing. A work package that ships a placeholder is not done.
- **Work-in-progress code proves nothing** [lead 2026-10-12]. Code on any branch shows the current state only. A requirement is met only when its acceptance test passes in the work package that owns it.
- **Cloning parity** [lead 2026-10-12]. Machine screen contents, flow, timings and machine behaviour match the 2022 video. The spec or a section 11 decision wins where it differs. Each machine work package cites the video times that it matches. Machine looks are not part of parity [lead 2026-10-12]: the machine models and textures changed since 2022. The current models and textures in the mod are the target (`geo/block/*.geo.json`, `textures/block/*.png`, `textures/gui/*.png`). Animations drive the bones of the current models. A screen matches the video in its contents (slots, gauges, tabs, buttons, tooltips) and uses the current GUI texture for its art. Where an animation needs a part that the current model lacks, the package adapts it to existing bones or asks the artist (Art list questions 12, 13, 17, 18).
- **Machine animation** [lead 2026-10-12]. Animation state that players see the same is server-owned and synced. Timers use game time. Nothing counts frames or partial ticks.
- **Art.** Art status lives only in the Art list. This backlog holds no art counts and no asset status. Each work package lists "Art: see Art list → <thing>". A package is not done until each of its Art list rows is "Done" (no Placeholder, no To check). A "Shared, to confirm" row counts as not done until the artist confirms it.
- **Code style:**
  - Javadoc on every public type and every public method that is not an override.
  - No `System.out` or `System.err`. No chat messages for debugging.
  - No `Minecraft.getInstance()` in common or server code.
  - All player-facing text goes through translation keys in `en_us.json`.
- **Code quality** (lead, 2026-10-05). They apply to every work package and every review:
  1. Keep the code as simple, well formatted, clean, documented and readable as possible.
  2. Keep the codebase as light as feasibly possible: optimized, efficient, performant, and in line with the basics of OOP. No speculative abstractions. No utility layer for one caller. No new dependency when the JDK or Minecraft has the function. Measure before you optimize, but never leave known per-tick waste.
  3. Review your own work as an adversary does. Do not add or remove code only for the sake of it.
  4. A human must be able to continue from where you stopped. Javadoc, a short package note and tests that explain intent make this possible.
- **Libraries first** [lead 2026-10-06, 2026-10-10]. If SmartBrainLib 2, GeckoLib 5 or Balm gives a hook, call the library. Do not copy library code. Do not write a second version of a library feature. Examples:
  - SmartBrainLib 2 ticks the brain. Remove the manual brain tick.
  - Gate sensors with `onlyScanIf` and set a scan rate per entity. No always-on block scan.
  - Use the built-in sensors and behaviours for nearby entities, players, hurt-by, attack targets, flee, follow and breeding.
  - Use GeckoLib 5 triggered animations, additive controllers and bone updaters. GeckoLib 5 applies the scale attribute.
  - Write Nublar code only where no hook exists (stamina chases, dominance, nests, herd formation, the incubator arm solver). Build it on the library's base classes. Use the library's extension points. Do not fork a library.
- **Three rules for the mod's own code** [tech survey, section 12.2]:
  1. Still things use static geometry. Fence posts, wires and idle machines draw from the chunk mesh or a block model, not from a block-entity renderer every frame.
  2. One network object runs each power grid. Posts, pylons and conduits do not tick.
  3. Work happens only on change. A block update or a packet goes out only when a value changes.
- Timers use game time, not tick counts. A server that skips ticks of far entities must not slow needs, cooldowns or herd travel [tech survey rank 3].
- Follow the compatibility duties per optimization mod in `docs/codebase-analysis.md` section 12.2 from the first port build. A compat item ships only when the other mod has a 26.2 build for that loader.
- Every client packet validates on the server: menu open, slot range, value range.
- Payloads stay under the vanilla limits: 32,767 bytes client to server, 1 MiB server to client [tech survey ON2].
- Datagen output in `common/src/generated/resources` is regenerated, never edited by hand, in the same commit as the provider change.

## Cross-cutting risks

No single work package owns these inputs. The owner adds the test.

1. **A dinosaur from `/summon` or a spawn egg has zero behaviour data.** It starves or divides by zero. Owner: F2.1. Test: summon each species. Stats load from species data. A species with no record uses the M0 fallback record and logs one line [JC3 J1].
2. **A dedicated server crashes because common code loads client classes.** Causes: `Minecraft.getInstance()` in `IncubatorMenu`, `distmarker` imports in five common files, the Forge entry point loads `ClientLevel` (crash seen 2026-10-10 [hitbox design]), and Forge `@OnlyIn` in common classes breaks the Fabric build. M0.P1 fixes the Fabric build. M0.P2 owns the full cleanup. Test: a dedicated server runs the full machine chain in a GameTest.
3. **A player loses items when a block breaks.** The sequencer drops 3 of 9 slots. The feeder saves nothing. The printer forgets its sensor and chip. Owners: F1.10 (the master drop rule), each machine package (F1.3, F1.4, F1.7, F1.8) and F3.3. Test: fill every slot, break the block, count the drops.
4. **Worldgen floods a chunk with fossils.** A vein attempt per chunk per stone, and a rarity test that always passes. Owner: F1.1. Test: a unit test on the rarity math, and a fossil count over 100 chunks against the config target.
5. **The server never checks a genome edit.** `UpdateEditInfoPacket` has no range, menu or content check (GH#49). Owner: F1.5. Test: a packet with 300 % and a packet with a locked gene are both rejected.
6. **Block-scanning sensors cost about 22 ms per tick at 100 dinosaurs, with aligned spikes near 440 ms** (estimate). Fix: a feeder registry per level, a water sensor that runs only while thirsty, `onlyScanIf` gates, a start offset per sensor [entity survey rank 2]. Owner: F2.4. Test: 20 animals in one paddock keep the server tick under 50 ms.
7. **Tick skipping breaks tick-count timers.** Far animals then recover stamina and lose needs too slowly [tech survey d10, rank 3]. Owners: F2.4 (needs, cooldowns), F2.5 (herd queues, stall thresholds), Beta hardening (test matrix). Test: skip ticks of a far dinosaur. Its needs and cooldowns follow game time.
8. **Colour layers cost most of each frame.** 20 Gallimimus draw 23 model passes each and run at 26 to 28 fps [entity survey C1]. Owner: F2.2. Test: 20 Gallimimus on screen reach at least 120 fps.
9. **Client-only animation state.** The original incubator arm kept its state in each client's renderer and counted frames, so players saw different eggs turn at different rates [orig 1.16]. Owners: F1.7, F1.8. Test: two clients see the same egg turn at the same game tick, at 30 fps and at 144 fps.
10. **Stand-in assets hide missing art.** Today the T. rex roar plays for every species, and some files are byte copies of other files (the Art list names them). Owners: F2.2, F2.3, every work package with an Art line. Test: a unit test fails when two species register the same sound file or clip, or when two texture files are byte-equal across genders or species. Exception: a layer that species data marks `shared_by_genders` after the artist confirms it.

---

## How to use this plan

This section is for a human developer and an AI agent. Follow it in sequence.

### 1. Pick work by milestone, then by work package

Milestones run in this order: M0, M1, M2, M3, M4, Beta hardening, Release 1. Each milestone ends with a playtest gate. The next milestone starts when the lead accepts the gate. Inside a milestone, take work packages. A work package starts when every item in its `Depends on` is merged. Inside a milestone, the work packages finish together: the gate tests them as one.

### 2. Check the gate of the work package

Do not start a work package until each dependency is merged into the integration branch. If a dependency is only partly merged, tell the lead and wait, or pick another work package.

### 3. Write the detailed task plan first

This file gives scope, not steps. Before any code:

- [ ] Read the work package and the section 11 sub-sections that it cites.
- [ ] Read the survey specifications that it cites (entity survey S1 to S27, tech survey T1 to T15, JC3 J1 to J23). They are clean-room specifications. Build each from its description only.
- [ ] For a machine: watch the video at the cited times. Read `original-machine-animations.md` for its animation.
- [ ] Read the Art list rows named in the Art line.
- [ ] Read the current code for every path under `Touches`. Fix wrong paths. Remember that the code proves nothing (global constraints).
- [ ] Write `docs/superpowers/plans/YYYY-MM-DD-<wp-id>-<name>.md` in the `superpowers:writing-plans` format: file list, interfaces, and bite-sized TDD tasks with real test code and real implementation code. No placeholders.
- [ ] Put every config key with its default in that plan and in `docs/config-reference.md` (M0.P1 creates it).
- [ ] Get a review of the task plan from the lead or a reviewer before implementation. An agent sends it to the lead and waits.

### 4. Implement on a branch

- [ ] Branch name: `<wp-id>/<short-name>`, for example `f1.1/fossil-worldgen`, from the integration branch.
- [ ] One commit per task, with a conventional prefix (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`).
- [ ] Write the test first for every piece of pure logic (math, data records, NBT round trips, rules, solvers, timelines).
- [ ] Run `./gradlew build` for all modules before every push. A compile failure on either loader blocks the push.
- [ ] Run both clients and the dedicated server for every work package that touches blocks, entities, screens or packets. Record what you did in the PR.

### 5. Definition of done

A work package is done when all of these are true:

- Every `In scope` item is implemented, or the PR names the cut item and the reason.
- Every `Acceptance` item passes. The PR lists each one with "pass" or "fail" and the evidence.
- It compiles and runs on Fabric and NeoForge. The feature was exercised in both clients.
- Unit tests for its pure logic pass in `./gradlew :common:test`.
- No debug output, no TODO without an issue number, no dead code added.
- Config keys are in `docs/config-reference.md` with defaults and the section 11 reference.
- `en_us.json` has every new key. Datagen ran again if a provider changed.
- Every Art list row in its Art line is "Done" (no Placeholder, no To check).
- Every row in its "Recipe rows" line ships with the ingredients of the recipe table (D78).
- For a machine: the PR shows each screen and animation next to the video frame at the cited time. It lists each difference and the spec or section 11 rule that causes it.
- The PR links this plan and the work package id. It names the open decisions that it worked around.

### 6. When you find a design gap

- Do not infer the answer from silence or from an unselected option.
- Build the version with the smallest gameplay commitment. Put any value behind a config key.
- Write the question, an in-game example and your choice into the PR under "Decisions for the lead". An agent also asks the lead through its question tool, with multi-select on.
- Add the question to the decisions register.

### 7. Reviewer checklist

A reviewer (a human, or a fresh agent that did not write the code) checks:

- [ ] The task plan was followed, or the PR explains each change.
- [ ] Both loaders build, and the PR shows runs on both.
- [ ] Every "tune in playtest" number is a config value.
- [ ] Packets validate on the server.
- [ ] No common-code reference to loader or client-only classes.
- [ ] Tests exist and fail without the implementation. Spot-check: revert one line and run the tests.
- [ ] Translation keys, datagen and config docs are current.
- [ ] No asset is a stand-in: no other species' clip or sound, no byte copy.
- [ ] Shared animation state is server-owned and uses game time.
- [ ] Nothing outside the work package changed without a reason in the PR.
- [ ] The four code quality standards hold.

---

## Milestones at a glance

| Milestone | Goal | Work packages | Exit gate (playtest) |
| --- | --- | --- | --- |
| M0. Port and foundation | One codebase on 26.2, Fabric and NeoForge, with config and species data. No new gameplay | M0.P0, M0.P1, M0.P2, M0.B00 | Everything that worked on 1.20.1 works on 26.2 on both loaders and a dedicated server |
| M1. Fossil to egg | A survival player goes from fossil to a hatched baby. All four machines are complete | F1.1 to F1.12 | A fresh survival world reaches a hatched baby on each loader. Each machine matches the video |
| M2. Dinosaurs | Six species live, grow, eat, drink, rest, socialise and breed with their own art | F2.0 to F2.7 | A paddock of each species runs a full growth cycle with own clips, sounds, skins and effects |
| M3. Park | Fences, power grid, feeders and their recipes | F3.1 to F3.4 | A powered park of fenced paddocks with feeders holds its animals |
| M4. World and vehicles | Digsites, villagers, illager sites, maps, vehicles | F4.1, F4.2, F4.3 (blocked) | A map leads to a digsite. A Ford Explorer runs a tour |
| Beta hardening | Performance, cleanup, balance, release | BH.1 (cleanup and performance), BH.2 (release) | Outside testers play the full loop. The Beta jars ship |
| Release 1 | Plants and wood, four species, their fossils | R01, R02, R03 | Each R package passes its acceptance |

---

## M0. Port and foundation

**Goal:** one codebase on Minecraft 26.2, Fabric and NeoForge, with the herd work, config and species data. No new gameplay.

**Exit gate (playtest):** on the client and the dedicated server of each loader, a tester runs the 1.20.1 machine chain from fossil to hatched T. rex, powers and repairs a fence, and watches a Gallimimus herd form and travel. A server owner changes a species value in a data pack and a balance value in the TOML, and both apply.

Work packages in order: M0.P0, M0.P1, M0.P2, M0.B00. M0.P2 blocks every later work package [lead 2026-10-05].

### M0.P0: Branch integration

**Old module:** P0 · **Section:** 2.6 · **Size:** L · **Depends on:** nothing. The lead committed the herd work on 2026-10-11, which resolves D37.

**In game:** no new gameplay. One 1.20.1 build holds the machine chain, fences, fossils and the herd behaviour. It builds on Forge and Fabric.

**Touches:** the whole tree. `J/init/*` becomes `J/registry/*`. `J/entity/ai/**` becomes `J/entity/dinosaur/ai/**`. `J/api/*` splits. The `nublar-gui` module appears.

**In scope:**

- Order: `1.20.1-ai`, then `rft/adversing`, then a port of the fossil design. Do not merge `fossils-1.20.1`.
- The herd work on `kash's-slop` is in commits 4e90174 and 582fbb0, which the lead made on 2026-10-11 (D37 resolved). It holds 29 herd classes plus changes to `FenceAwareNodeEvaluator`, `Dinosaur`, the renderers and the Gallimimus config [entity survey fact 49]. Never take files from the frozen worktree [herd handoff].
- [ ] Create `integration/1.20.1` from `1.20.1-ai` (4244f62). Remove `hs_err_pid20864.log` in the first commit.
- [ ] Bring in the committed herd work from `kash's-slop` (4e90174, 582fbb0).
- [ ] Merge `rft/adversing` with `git merge -X find-renames=30%`. Resolve the 13 shared files. Move `NearestFeederSensor`, `EatFromMeatFeeder` and `WalkToNearestFeeder` to `entity/dinosaur/ai/{sensor,task}`.
- [ ] Fix the two Javadoc blocks with a raw `->` (`ProjectNublar.java`, `api/fossil/FossilCollection.java`) so doclint passes.
- [ ] Check the swapped previous and next arguments at the two fence connection sites from commit 59d9518. Write a unit test on `Connection` ordering before the fix.
- [ ] Build both loaders. Fix compile errors before any behaviour work.
- [ ] Forge client: build a 4-post fence and power it. Break a wire and repair it with the spool. Break a post. Record the result.
- [ ] Port the fossil design from `fossils-1.20.1` by copy, not by merge:
  - `FossilBlock` with the `Quality` block state, `FossilBase` and `FossilFeature`.
  - The `fossil_filter` and `fossil_height` placement modifiers, the overlay textures and the datagen providers.
  - Keep all six species registered. Keep `BehaviourDataReloadListener` and the diet lookup.
  - Fix the quality roll (missing `break`) and the rarity test (`nextDouble() < 1.0 / chance`), with unit tests.
  - Use the worldgen `RandomSource`, not `new Random()`. Remove the `System.err.println`.
- [ ] Regenerate `common/src/generated/resources`. Do not merge the fossils branch's generated files.
- [ ] Forge client: a new world has fossil blocks, a mined one drops a fossil item, and the Fossil Processor accepts it.
- [ ] Forge client: a Gallimimus herd forms, follows the leader and travels as on `kash's-slop`.
- [ ] Open a PR to the default branch. After the merge, ask the lead to make the integration branch the default.

**Out of scope:** Fabric parity (M0.P2). M0.P0 must not make Fabric worse.

**Produces:** the integration branch.

**Config (defaults):** none.

**Art:** none.

**Acceptance:**

- `./gradlew build` passes for `common`, `forge`, `fabric` and `nublar-gui`.
- Forge client: a fossil is mined, processed, sequenced, synthesised, printed, incubated and hatched.
- Forge client: a fence powers, hurts, breaks and is repaired.
- Forge client: a Gallimimus herd behaves as it did on `kash's-slop`.
- The Fabric client at least starts to the title screen.
- No `hs_err_*`, `forge/dataRun/**`, `G/.cache/**` or `.ai/` file is in the tree.

### M0.P1: Repository hygiene and CI

**Old module:** P1 · **Section:** 3.5, 8.4 · **Size:** S · **Depends on:** M0.P0

**In game:** no new gameplay. Each change builds on both loaders in CI. A new contributor builds and runs the mod from the README alone.

**Touches:** repo root, `.gitignore`, `README.md`, metadata files, Gradle files, `.github/`, GameTest classes.

**In scope:**

- [ ] Remove `shapes/*.txt`, `J/mixin/ExampleMixin.java` (if rft did not), the `com.example` service files and the empty `.ai/mcp/mcp.json`. Add `hs_err_*.log`, `*/dataRun/` and `*/.cache/` to `.gitignore`.
- [ ] Replace `README.md`: what the mod is, the Beta scope in one paragraph with a link to section 11, how to build and run each loader, how to contribute (links to this plan).
- [ ] Fix the metadata: `mod_author`, `description`, `credits`, `pack_format`, placeholder URLs and the missing logo in `fabric.mod.json`, and dependency declarations with the right sides (GeckoLib, SmartBrainLib, Architectury, energy library, Common Networking).
- [ ] Remove `toml4j` from `common/build.gradle` and `fabric/build.gradle` [tech survey n6, d19].
- [ ] Remove `@OnlyIn` from common classes so CI builds both loaders [herd handoff, hitbox design]. M0.P2 owns the full client-class cleanup.
- [ ] Add a `common` unit test source set: JUnit 5 on `./gradlew :common:test`. Add real JUnit 5 dependencies (Gradle has none today [entity survey fact 50]). Move the 371 existing unit tests in. Remove `tools/junit-standin/run.sh` when all 371 run through Gradle. Add first new tests: `Quality` ordering and the fossil rarity math. The set is pure Java. Tests that need Minecraft are GameTests.
- [ ] Move the GameTests from the Forge module into `common`, with one small registration shim per loader [hitbox design, entity survey n30, S24].
- [ ] Add `.github/workflows/build.yml`: on every PR and push, install the toolchain, run `./gradlew build :common:test`, upload the two jars, cache Gradle.
- [ ] Add `.github/PULL_REQUEST_TEMPLATE.md` with the Definition of done.
- [ ] Add `docs/config-reference.md` with the header `Key | Default | Type | Section 11 ref | Added by`.
- [ ] Do not touch `LICENSE` or `license=`.

**Out of scope:** gameplay.

**Produces:** CI, the unit test set, common GameTests, `docs/config-reference.md`.

**Config (defaults):** none.

**Art:** none.

**Acceptance:**

- CI is green on the integration branch for both loaders.
- `git status` is clean after a build.
- The README answers "how do I run this" without the Gradle files.
- `./gradlew :common:test` runs the 371 moved tests and they pass. `tools/junit-standin/run.sh` is gone.
- No build file declares `toml4j`.
- The GameTests compile from `common` and run on each loader through its shim.

### M0.P2: Port to Minecraft 26.2

**Old module:** P2 · **Section:** 11.17 · **Size:** XL · **Depends on:** M0.P1 · **Blocks:** every later work package

**In game:** no new gameplay. Everything that worked on 1.20.1 works on 26.2 on Fabric and NeoForge, and on a dedicated server: the machine chain, fences, the six species and the Gallimimus herd.

Target chosen by the lead: 26.2, Fabric and NeoForge, Java 25, unobfuscated (no Parchment).

**Touches:** the build, every module, `J/**` for API moves.

**Before you start, check on that day** the current stable versions, and write them with dates and URLs into the task plan:

- NeoForge for 26.2. Pin the `26.2.x` branch, because the default branch is now `26.3.x` [hitbox design].
- Fabric Loader, Fabric API, GeckoLib, SmartBrainLib, Architectury API, Common Networking, Forge Config API Port, ModDevGradle, fabric-loom, the MultiLoader Template branch for 26.2.
- On 2026-10-10 these 26.2 builds existed: Balm 26.2.0.9, GeckoLib 5.5.6, SmartBrainLib 2.0.3, Architectury, Common Network, Forge Config API Port, Team Reborn Energy 5.0.0 [tech survey, "26.2 availability"].

**In scope:**

- [ ] Branch `m0.p2/port-26.2` from the integration branch.
- [ ] Replace the build with the MultiLoader Template for 26.2 (`common`, `fabric`, `neoforge`, `buildSrc`). Move `forge` into `neoforge`. Keep `nublar-gui` as a source set. Set `java_version=25` and `minecraft_version=26.2`. Remove Parchment.
- [ ] Replace Botarium with Balm, for energy and fluid storage only [lead 2026-10-06]:
  - Balm `common` gives `EnergyStorage`, `FluidTank`, `BalmEnergyStorageProvider`, `BalmFluidTankProvider`.
  - On NeoForge, Balm 26.2.0.9 bridges to the 26.2 `EnergyHandler` and `ResourceHandler` capabilities, not to `IEnergyStorage` or `IFluidHandler` [tech survey n1].
  - On Fabric, Balm bridges energy to Team Reborn Energy only when it is installed. Balm does not bundle it. Fluids go to the Fabric Transfer API [tech survey fact 5].
  - Record these facts in the task plan. Do not write an in-house adapter.
  - Keep Architectury, Common Networking and Forge Config API Port for everything else.
  - The 8 users: `Connection`, `BlockEntityElectricFencePole`, `DinosaurFeederBlockEntity`, `EggPrinterBlockEntity`, `GeneratorBlockEntity`, `IncubatorBlockEntity`, `ProcessorBlockEntity`, `SequencerBlockEntity`.
- [ ] Check Common Storage Lib (Terrarium) again on the start day. On 2026-10-10 its newest branch was `1.21.x`. If it has a stable 26.2 release, compare it with Balm (API fit, cable compat, maintenance) in the task plan. Also check whether Architectury merged `feature/fluid_transfer_ext`.
- [ ] Replace every Forge-only API in `common`: the five `distmarker` imports and remaining `@OnlyIn`, the `ClientLevel` load in the Forge entry point (risk 2), the model data in `BlockEntityElectricFence*Mixin`, `GeoMultiBlockItemMixin`, and `ForgeConfigSpec` (use Forge Config API Port's spec class on both loaders, or Night Config).
- [ ] Give Fabric what section 3.4 lists as missing: client entrypoint, reload listeners, death message hook, spool repair handler, the `fossil` and `test_tube` model loaders, fence model data, dependency declarations. Register reload listeners in `common` through a platform service.
- [ ] Move `IncubatorMenu`'s `Minecraft.getInstance()` call to the client screen.
- [ ] Upgrade GeckoLib 4.7 to GeckoLib 5 [lead 2026-10-06]:
  - Imports move from `software.bernie.geckolib` to `com.geckolib` (31 files).
  - `AnimationState`, `CoreGeoBone`, `Color`, `EntityModelData` and `RenderProvider` no longer exist. Rewrite predicates, bone code, render layers and item renderer providers.
  - Move head look and sway to bone updaters, which run after the controllers [entity survey n2].
  - Use `setAnimationSpeed` (the speed handler is gone). Remove manual entity scaling, because GeckoLib 5 applies the scale attribute [entity survey n3].
  - Keep the current look. Look changes belong to F2.2.
- [ ] Upgrade SmartBrainLib 1.15 to SmartBrainLib 2 [lead 2026-10-06]:
  - `BrainUtils` is gone (30 files use it [entity survey fact 45]). `BrainActivityGroup` is gone, and `ActivityBuilder` replaces it. `SBLLoader` is gone.
  - Seven classes moved package: `ExtendedBehaviour`, `DelayedBehaviour`, `OneRandomBehaviour`, `FirstApplicableBehaviour`, `PredicateSensor`, `SquareRadius`, `SmartBrainProvider`.
  - Remove the manual brain tick, so a dinosaur thinks once per tick [entity survey n1]. Flip the `PredicateSensor` argument order in the three custom sensors.
  - Keep brain behaviour as it is. Brain changes belong to M2.
- [ ] Port the herd code [hitbox design]:
  - Rewrite `FenceAwareNodeEvaluator` for `PathType` and `PathfindingContext`. Keep the evaluator override on both loaders (Fabric's `LandPathTypeRegistry` has no mob parameter).
  - Use the step height attribute in place of `setMaxUpStep(1.0F)`.
  - Keep `BodyFit` the one herd block reader.
- [ ] Move every block-entity render box to its renderer, because 26.x keeps it there [tech survey n2, d18]. NeoForge: `getRenderBoundingBox` on the renderer. Fabric: per renderer, pick the EntityCulling interface or `shouldRenderOffScreen`, and record it.
- [ ] Port `neighborChanged` to the 26.x `Orientation` signature. Remove the empty override in `ElectricFenceBlock` [tech survey n3, d6].
- [ ] Store genome and DNA drive data as data components with a `StreamCodec`. Give `DinoData` an `equals` [tech survey n4, d5].
- [ ] Add a `"version"` integer to the root of every custom NBT compound [tech survey n5].
- [ ] Record in the task plan the 26.x API shapes from a shipping 26.1.2 mod: `Identifier`, `ValueInput` and `ValueOutput`, `SavedDataType`, `hurtServer`, `EntitySpawnReason`, `PathType`, `InterpolationHandler`, extract-and-submit rendering, `ResourceHandler`, `Orientation`. Check each on 26.2 [JC3 P2.1].
- [ ] The client stores the recipe map when recipes arrive, so recipe viewers work. NeoForge: `RecipesReceivedEvent`. The task plan finds the Fabric equivalent [JC3 P2.2] [inf].
- [ ] Port datagen to run on NeoForge with the same output. Add a Fabric datagen entrypoint only if needed.
- [ ] Update CI for Java 25 and the new module names.
- [ ] Update `docs/codebase-analysis.md` sections 3.1 and 3.4. Keep its STE style.

**Out of scope:** any gameplay change.

**Produces:** the 26.2 build, Balm storage on eight block entities, the platform service layer.

**Config (defaults):** none.

**Art:** none.

**Acceptance:**

- `./gradlew build` passes for `common`, `fabric` and `neoforge`.
- On the client and dedicated server of each loader: the machine chain from fossil to hatched T. rex works, fences power and repair, the generator powers machines, and a GeckoLib dinosaur renders and animates.
- GameTest `machine_chain_smoke` runs on both loaders in CI. It places a creative generator, the four machines and a creative fossil, and asserts that an egg appears.
- A third-party energy cable connects to the generator on each loader. The PR names the mod. On Fabric the test installs Team Reborn Energy.
- Each species renders with its layers and plays walk, idle and attack on both loaders.
- The brain does what it did before: a T. rex drinks, eats dropped meat and hunts a cow (GameTest or recorded manual check).
- A Gallimimus herd forms, follows its leader and travels on both loaders.
- `grep -r "software.bernie.geckolib" common neoforge fabric`, `grep -r "net.minecraftforge" common/src` and `grep -r "botarium" .` return nothing.
- `grep -rl "net.blay09" common/src` lists only the eight storage block entities and the platform service.
- After login, the client recipe map holds the Nublar machine recipes on both loaders [JC3 P2.2].

### M0.B00: Config foundation and species data

**Old module:** B00 · **Section:** 11.17, 4.6, 6.4 · **Size:** M · **Depends on:** M0.P2

**In game:** no new gameplay. A server owner changes a balance number in one TOML file or a species value in a data pack. The change applies on both loaders. `/reload` applies species changes. A server owner can disable a costly system. It then costs nothing per tick, and old worlds still load [tech survey T11]. Today every dinosaur's stamina maximum is 0 because of a key mismatch [entity survey fact 51]. This package removes both keys.

**Touches:** `J/data/*ReloadListener`, `J/api/DinoBehaviourData`, `R/data/projectnublar/config/dinosaurs/*.json`, `R/data/projectnublar/diet/`, new `J/config/`.

**In scope:**

- Three TOML configs: common, server, client. `config/projectnublar-common.toml` holds every global balance number unless a package names the server or client file. Sections: `features`, `fossils`, `processor`, `sequencer`, `editing`, `incubation`, `power`, `fences`, `needs`, `breeding`, `vehicles`, `ai`, `detection`, `vanilla`, `worldgen`.
- The server config holds scan intervals, size caps and fast-path switches (BH.1). The client config holds the hover-box switch (F1.10) and the low-effects switches (BH.1, F2.3).
- Forge Config API Port loads TOML on Fabric. NeoForge's config loads it on NeoForge. `J/config/NublarConfig` wraps both with typed getters.
- A server owner disables a system with one switch [tech survey n7, T11]. `NublarFeatures` reads `features` at start-up. Switch list: D29 answered, only `features.breeding` [lead 2026-10-12]. For a disabled feature:
  - Its blocks and items stay registered so saves load. They do not show in creative tabs.
  - Its tick paths return at their first line. Its event handlers and sensors are not registered.
  - Its recipes, loot additions and advancements do not load. One data-load condition type on both loaders reads a switch.
  - The server's values win over the client's.
- Scan intervals and size caps live in the server config from day one [tech survey n8].
- Id redirects for renamed species and genes, so old saves load [tech survey n8].
- The join sync sends the client only a projection of species data: size, name, variants [tech survey n8].
- `J/api/dinosaur/SpeciesData` loads from `data/projectnublar/species/<species>.json` through one reload listener on both loaders. Fields:
  - id, clade
  - stats: `max_health`, `health_regen_ticks_per_half_heart`, `attack_damage`, `defense`, `speed`, `energy`, `hunger`, `thirst`
  - `group_size_min`/`max`, `egg_clutch_min`/`max`, `hatch_success` (nest), `lab_hatch_success` (lab egg, D41), `growth_days`
  - `base_height_blocks`, `height_range_min`/`max`, `gender_size_weights`, `incubation_minutes` (30 for every species in Beta, D130)
  - `diet` tag id, `prey_preferences` (clade to weight)
  - `fossil_set` id, `amber_weight`, `biomes` (biome tag to percent), `period`, optional `depth_min`/`max`. No depth pair takes a default band from `period` [JC3 J2].
  - `egg_model` (the asset repo egg model of the species, F1.9, D82) [lead 2026-10-12]
  - `variants` (F2.2), `stress_threshold` (F2.4, default 50, no gender split, D13, D88), `flee_or_stand` (F2.6), `personal_space_radius` (F2.6, replaces `territory_radius`. Start values by body size: T. rex 10, Brachiosaurus 10, Triceratops 8, Dilophosaurus 6, Velociraptor 6, Gallimimus 5 blocks, D165)
  - `activity_window` (F2.4): diurnal, nocturnal or cathemeral. It replaces the `nocturnal` flag. T. rex, Velociraptor and Dilophosaurus cathemeral, the three herbivores diurnal (D97) [recon RC1].
  - `sleep_windows` (F2.0): the two cathemeral windows per species in ticks (D98). T. rex 16,000 to 21,000 and 7,000 to 9,000. Velociraptor 20,000 to 23,000 and 6,000 to 8,500. Dilophosaurus 2,000 to 5,000 and 8,000 to 10,000
  - `shifted_sleep_windows` (F2.0): the cathemeral windows of a herbivore that the nocturnal gene shifts (D182). Triceratops 18,000 to 22,000 and 5,000 to 7,000. Brachiosaurus 19,000 to 22,000 and 9,000 to 10,000. Gallimimus 17,000 to 21,000 and 3,500 to 5,000
  - `call_interval_ticks` (F2.0, D101), `growth_days` values: T. rex 45, Velociraptor 29, Dilophosaurus 31, Gallimimus 31, Triceratops 47, Brachiosaurus 57 [lead 2026-10-12]
  - `young_size_ratios`: hatchling 0.08, infant 0.32 of adult height, for every species [lead 2026-10-12]
  - `follow_range`, `flee_list`, `reach_band`, `idle_weights`, `view_cone_degrees` [recon RC2]
  - `dominance_weights`: age 0.5, size 0.3, aggression gene 0.2. Today constants in `Dominance.java` [recon RC3].
  - `intelligence_rating` from the design doc byte values. F2.6 reads it for the detection chance [recon RC4]. Beta has no scent.
  - `default_special_gene`: hyper intelligence for the Velociraptor (D43), poison for the Dilophosaurus (D181 answered). Empty for the others [recon RC5].
  - `risk_behaviours`: stress threshold −20, activity window flipped, group-averse (maximum group 1), aggression +30 % [recon RC6].
  - a tracking range in chunks. Proposal: 8 for Gallimimus, Velociraptor, Dilophosaurus. 10 for Tyrannosaurus, Triceratops. 12 for Brachiosaurus. All use 10 today [tech survey ON9, n29].
  - `attack_reach` in blocks, for the hit tick [entity survey S27].
- An unknown field or a value that breaks a J1 rule fails loudly at load. The log names the file, the field, the value and the rule [JC3 J1].
- Flee or stand: Gallimimus flees, Triceratops stands, Brachiosaurus defends (stands, with tail whip and stomp, D50). The three carnivores stand or retreat by threat and group size (D13 answered, F2.6).
- `SpeciesData` replaces `config/dinosaurs/*_config.json` and `DinoBehaviourData`. Remove `default_stamina` and `default_energy_capacity`.
- A load error names the bad file. Today the message prints `{}` because of string concatenation with SLF4J placeholders (`BehaviourDataReloadListener.java:107,112`) [tech survey d8].
- Fill the six species files. Tyrannosaurus from its card [s7.8]. The others from [s11.16]. A `"_basis"` field names the source of each block.
- Clutch and nest hatch rate per species (D79 answered) [lead 2026-10-13]. T. rex: 12 to 24 at 0.06. Velociraptor: 8 to 13 at 0.25. Dilophosaurus: 12 to 24 at 0.16. Gallimimus: 15 to 30 at 0.15. Triceratops: 15 to 25 at 0.11. Brachiosaurus: 15 to 30 at 0.06. Each `_basis` names the clutch draft. Depth and biome per species: the F1.1 table (D138). Hunger and thirst: Velociraptor 27 and 23, Dilophosaurus 27 and 23, Gallimimus 40 and 35, Triceratops 67 and 58, Brachiosaurus 160 and 140 [lead 2026-10-12]. The height targets and the 11.16 stats stay interim values (D11, D12).
- `docs/config-reference.md` lists every key, plus for server owners: tracking ranges, the server broadcast property, and Nublar ids for Immersive Optimization, EntityViewDistance and Chloride [tech survey n9].
- Value rules after the codec decode [JC3 J1]: each `_min` ≤ its `_max`, eye height ≤ body height, sizes 0.01 to 15 blocks, no negative stat, speeds inside a species-wide range, `hatch_success` 0 to 1. A broken file logs one line, and the game leaves that species out.
- A summoned dinosaur with no species record loads with one fallback record. The log names the species once [JC3 J1].
- The package note keeps a table of species keys and the package that reads each. A key with no reader carries "planned: <wp-id>" [JC3 J1].
- A species with only a `period` gets a fitting depth. A largest-remainder split divides the fossil column into one band per period, by period length, at least 1 layer each, youngest on top [JC3 J2]. Build the map once at reload, immutable [tech survey OW1]. Column top and bottom are config. A species with its own depth pair keeps it.
- A server owner disables fossil ore or amber, and new chunks have none. One placement modifier type with one field (the switch name) returns no positions when false. It goes first in every Nublar placed feature [JC3 J3]. `docs/config-reference.md` says the switch applies to new chunks only.

**Out of scope:** any gameplay change. Values move unchanged, except where [s11.16] gives other working values.

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `features.breeding` | true | D29 answered: the only feature switch [lead 2026-10-12] |

**Produces:** `NublarConfig` getters, `NublarFeatures` and the data-load condition, `SpeciesData`, `SpeciesRegistry.get(EntityType)` and the client projection, the id redirect table, the reload-listener platform service.

**Art:** none.

**Acceptance:**

- `/reload` after a species JSON edit changes a summoned animal's max health on both loaders.
- A TOML edit and a restart change a processor run time on both loaders.
- Unit tests: `SpeciesData` codec round trip, every shipped species file parses, a key typo fails.
- `grep -r "DinoBehaviourData" common/src` returns nothing.
- A recipe with the data-load condition does not load while its switch is false, on both loaders.
- A save with an old species id loads through a redirect.
- A broken species file logs a message that names the file.
- Unit test: `group_size_min` above `group_size_max` fails, and the log names file, field, value and rule [JC3 J1].
- Unit test: every codec field has a row in the species key table [JC3 J1].
- A summoned dinosaur with no record loads with the fallback record and one log line, on both loaders [JC3 J1].
- Unit test: the depth band split gives each period at least 1 layer and uses every layer [JC3 J2].
- With the fossil ore switch false, 100 new chunks hold no fossil ore. An older chunk keeps its ore [JC3 J3].

---

## M1. Fossil to egg

**Goal:** a survival player finds fossils and amber, extracts and edits DNA, synthesises, prints and incubates an egg, and hatches a baby. All four machines are complete: building, screens, upgrades, power draw, animations, sounds, recipes and art.

**Exit gate (playtest):**

- On each loader, a tester starts a fresh survival world and reaches a hatched baby with no creative item and no trade. The tester records the time.
- Every machine is built from parts and runs on one Basic Generator.
- A reviewer plays the 2022 video next to the game. Each screen's contents, flow step, timing and machine behaviour matches at the cited times, except where the spec or section 11 differs. The reviewer does not compare machine looks: the current models and textures are the target [lead 2026-10-12]. The reviewer lists each difference with its rule.
- The reviewer records the machine time per dinosaur at base tier (target about 50 minutes [s11.7]) and the fossils needed for 50 % on an iron filter (target 25 to 30).
- Every Art list row for the four machines, the lab items, fossils and amber, and eggs is "Done" (no Placeholder, no To check).
- Each machine shows the upgrade looks that the lead approved (D85).

**One package per machine.** Each machine package delivers its whole machine: part list and building steps, screen, upgrades and their looks, power draw, animations, sounds, recipe rows, art and acceptance. The packages are F1.3 (Fossil Processor), F1.4 to F1.6 (Sequencer: sequencing, editing, synthesis), F1.7 (Egg Printer) and F1.8 (Incubator). F1.10 holds only the shared code that every machine needs first. No machine package merges with a placeholder.

**Order inside M1** (from the `Depends on` lines): F1.10 first, then F1.11, F1.1, F1.3, F1.4, then F1.2 and F1.5, then F1.6, F1.7, F1.9, F1.8, and F1.12 last.

**M1 uses the M0 dinosaur.** A few M1 items need an M2 package: the growth-step hook (F2.1), the per-animal layer render (F2.2), and the risk behaviours that F2.4 to F2.6 read. Each such item says "(lands with F2.x)". Its acceptance runs in that M2 package.

### F1.1: Fossils and amber

**Old modules:** B01, B02, B03 · **Section:** 11.1, 11.2, 2.4, 4.1, 4.2, 7.10, 8.5 · **Size:** XL · **Depends on:** M0

**In game:** fossil ore generates in four forms in stone, sandstone and deepslate. Biome and depth decide the species. Each block shows its quality before the player mines it. The overlay takes its tint from the host stone, and its tint strength, opacity and rotation change from block to block. A mined fossil is an item that knows its species, piece, quality and biome, and shows its DNA percent. Amber ore drops amber that shows no species. The tube from an amber also hides its species until an Identification Tag reveals it. A fossil pouch collects fossils by itself. Every DNA tube shows its percent and its DNA state.

**Touches:** `J/worldgen/` (from M0.P0), `J/block/FossilBlock`, `J/api/fossil/`, `J/item/FossilItem` (new), `J/api/DNAData`, `J/api/FossilCollection` (remove), `J/api/loot/functions/*`, `J/block/AmberBlock`, `J/item/AmberItem`, new `J/item/FossilPouchItem`, the test tube item, biome modifiers in both loader modules, `F/datagen/ModWorldGenProvider` (now `neoforge`), loot providers, item textures.

**In scope, worldgen:**

- Four forms, each a configured feature: `single_ore`, `small_cluster`, `skeleton_cluster`, `digsite_deposit`. F4.1's structure places the last one.
- One cluster is one species. A skeleton cluster is one animal. Piece presence follows the piece weights of the fossil set. The card's "quantity max" is the usual maximum. `fossils.over_max_chance` (default 0.05) lets a piece exceed it.
- Rarity formula (GH#1): ore rarity × period rarity × species share × piece share × quality share. One pure class, `FossilRarity`. Its tests reproduce the [s11.1] example: a pristine skull is about 1 in 200 T. rex ores.
- Biome percentages scale the amount per biome group. A species never generates in an unlisted biome. Biome groups are biome tags under `data/projectnublar/tags/worldgen/biome/`.
- Depth per species comes from `SpeciesData` (D138 answered) [lead 2026-10-13]: T. rex y 40 to 75, Triceratops 55 to 75, Gallimimus 50 to 72, Velociraptor 45 to 70, Brachiosaurus 35 to 55, Dilophosaurus −20 to 30. The Dilophosaurus is the only Beta fossil in deepslate. Biome percentages per species and the biome group lists: spec F1.1 table.
- Species share: an equal share for each species valid at the place [lead 2026-10-13]. Period rarity: Cretaceous 2.95, Jurassic 2.8. The Dilophosaurus uses the Jurassic rarity, 2.8 (D254 answered) [lead 2026-10-13].
- Every species has a pelvis piece. The T. rex pelvis has its own sprite. The other species share one generic pelvis set (D233) [lead 2026-10-13].
- Quality: a cluster rolls a base quality. Each block rolls near it with `fossils.quality_spread`: 70 % base, 15 % one tier up, 15 % one tier down, clamped. Quality is a block-state property.
- Each quality has its own overlay texture. Fix the shared fragmented and poor art.
- Host stones: stone, sandstone, deepslate. Deepslate features target deepslate depths.
- The block stores its biome group as a block-state property, so the drop carries it.
- Block layout (D31 default): one block per host stone and species. Piece, quality and biome group are properties. About 4,600 states in total [tech survey O17]. Today 910 blocks exist for two species, and six species would make 3,000 to 3,600 (`FossilCollection.java:35-52`) [tech survey d17].
- Overlay looks from `Fossil Ore Aesthetics.docx`: tint from the host stone with a random strength of 0 to 90 %, and random rotation. Each overlay has one of four opacity levels: 15, 43, 72 and 100 % (D31 answered). Build the levels as weighted model variants. The four levels and random rotation are in Beta (D126) [lead 2026-10-12].
- Draw the overlay as a baked model with randomness from the position seed. No block entity and no block-state property for the look [tech survey O17].
- A unit test counts the states per fossil block and the fossil block total [tech survey n10].
- Worldgen is thread safe with C2ME [tech survey n11]: build lookup maps once on the main thread as immutable maps, published with one assignment. Use only the generator's `RandomSource`. Place blocks only inside the given region, with no drops and no entity spawns. No lazy cache in a feature config. Keep features as data with codecs.
- Placement reads the immutable maps, not the public static `HashMap`s in `FossilCollection.java:20-22` and `ProjectNublar.java:15` [tech survey d16].
- Fabric `BiomeModifications` and NeoForge biome modifiers both place the features.
- One fossil pocket holds one species, stable across reloads. Seed a random source from the world seed and the cell `(x >> 3, y >> 3, z >> 3)`. Pick from the species valid at that depth and biome group, weighted by `FossilRarity`. No block entity stores the pick [JC3 J2].
- A cell with no valid species tries the other layers of the same cell, then D02 (nearest depth band) [JC3 J2].

**In scope, items and drops:**

- One `FossilItem` with components: species, piece, quality, biome group. Tooltips show all four. Quality ladder: fragmented, poor, common, pristine. The lead confirmed these names [lead 2026-10-12].
- The T. rex fossil set has a neck piece. A T. rex skeleton cluster can hold a neck [lead 2026-10-12].
- The fossil tooltip shows its DNA yield in percent. The tube that the Processor makes from it shows the same value times the filter efficiency (GH#33).
- Drops come from loot tables. Remove the `getDrops` override. Fortune gives a chance of one tier up (`fossils.fortune_tier_up_chance`, +7.5 % per level, D125) [lead 2026-10-13]. Silk Touch drops the ore block.
- DNA yield per quality in TOML: 5, 7.5, 10 and 30 %. Special pieces (skull, tooth, claw) multiply it by `fossils.special_piece_yield_multiplier`, 1.5 (D01 answered).
- Today every fossil gives 0 %, because the yield lookup in `DNAData.java:46-52` is in comments. Fix it here, with F1.5 [recon RC14].
- Item art: generic bone sprites per piece, shared across species. Species sprites for the skull, the tooth and the claw. The Art list rows give the file for each.
- Translation keys for every piece and quality.
- The tag `projectnublar:special_pieces` marks special pieces for trades (F4.1). No recipe uses special pieces (D78) [lead 2026-10-12].
- Remove the static maps of `FossilCollection`, the `COLLECTIONS.put` return bug and the empty-list `.get()` in `FossilItemFunction`.

**In scope, amber:**

- Amber ore in the three host stones, placed by the fossil feature set with its own rarity. The block stores depth band and biome group.
- The amber item carries only depth band and biome group. Its tooltip says "Unknown species".
- The Fossil Processor accepts amber. Fix the `dna_percentage` tag check (with F1.3).
- At run end, the Processor rolls the species from `amber_weight` of every species valid at that depth and biome, and the yield uniformly from 0 to `fossils.amber_max_yield` (default 80). Amber has no quality tiers (with F1.3).
- The amber tube stores the rolled species but stays Unidentified, and its tooltip hides the species. Only sequencing reveals it: the Sequencer names the species on the drive entry (F1.4) [lead 2026-10-13, Z-8]. Beta has no Identification Tag. This replaces the tag rule of 2026-10-12 (D83) and "the processor shows the species on the DNA tube" [s11.2].
- No valid species: the run gives the nearest-depth-band species (D02 answered).

**In scope, fossil pouch (new) [lead 2026-10-12]:**

- A fossil pouch item holds 27 slots [design doc, P4 #103]. Store the contents in the vanilla container contents component. No block entity, no tick.
- While the pouch is in any slot of the player's inventory, a picked-up fossil goes into the pouch first. It merges with equal stacks (same species, piece and quality). A full pouch passes the item to the normal inventory (D84).
- Amber also goes into the pouch (D145 answered).
- Manual insert: the player can put fossils into the pouch by hand, with a slot click or a drag. The pouch refuses every item that is not a fossil (D120).
- Take-out: the player opens the pouch with use and takes items out. Hoppers and the Fossil Processor can pull fossils out of the pouch (D84).
- The pickup hook is a loader event or a small mixin per loader. The task plan picks it. The hook returns at its first line when the player has no pouch.

**In scope, DNA labels (new) [lead 2026-10-12]:**

- Every test tube with DNA shows its DNA percent in its tooltip, with decimals [spec "Processor", s11.4]. The icon has no percent label (D234) [lead 2026-10-13].
- DNA items carry a DNA state: Unidentified, Identified or Full (just synthesised) [design doc "Items"]. The tooltip shows the state.
- State rules (D83, answered): only an amber tube has the Unidentified state, and its tooltip hides the species. A fossil tube and a DNA syringe show their species and have the Identified state [inf: the only state left that is not Full]. An amber tube stays Unidentified. Its DNA becomes Identified only on the drive, when the Sequencer sequences it (F1.4) [lead 2026-10-13, Z-8]. Synthesis output (F1.6) is Full. The state does not change what a machine accepts.

**Out of scope:** terracotta host, trace and generic fossils, the generic "unidentified" fossil piece, rock strata by age [s11.1, s11.2, s11.13]. Nest-site sediment, nest fossils, trackways and petrified logs [JC3 B01.2, B01.3]. Skeleton exhibits. A jacket step (D74) [JC3 B02.1]. The drill extractor. Amber that drops a plant sample (R01) [JC3 B03.1]. One species per amber deposit.

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `fossils.over_max_chance` | 0.05 | [s11.1] |
| `fossils.quality_spread` | 70 / 15 / 15 % | [s11.1] |
| `fossils.yield.<quality>` | 5, 7.5, 10, 30 % | T. rex card [s11.1] |
| `fossils.special_piece_yield_multiplier` | 1.5 | D01 answered |
| `fossils.amber_max_yield` | 80 | [s11.2] |
| `fossils.fortune_tier_up_chance` | 0.075 per level | [s11.1], D125 [lead 2026-10-13] |
| `fossils.species_share` | equal for every valid species | [lead 2026-10-13] |

**Produces:** `FossilBlock` with `QUALITY`, `BIOME_GROUP` and (D31) `PIECE`. `FossilSet` records in `data/projectnublar/fossil_sets/`. `FossilRarity`. `FossilItem` accessors `species()`, `piece()`, `quality()`, `biomeGroup()`. `DNAData` gains `biomeGroup`, `source` (FOSSIL, AMBER, SYRINGE, REMAINS, EGG) and `state` (UNIDENTIFIED, IDENTIFIED, FULL). `AmberItem`. `SpeciesSelector.rollAmber(depth, biomeGroup, random)`. `FossilPouchItem`.

**Recipe rows:** fossil pouch. This package adds the row from the approved recipe table (D78).

**Art:** see Art list → Fossils and amber, Lab items (test tube states, fossil pouch). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- 100 chunks in a listed biome at the right depth give fossil blocks within 20 % of the config target. An unlisted biome gives zero (GameTest or a debug count removed before merge).
- A debug count over a badlands column finds Dilophosaurus only from y −20 to 30, and no Beta fossil from y 31 to 34.
- Unit tests: rarity shares, over-max chance, quality spread, piece selection never outside the set.
- Unit test: states per fossil block and the block total match the layout (about 4,600 under D31).
- A 200-chunk run with C2ME on Fabric has no crash and no region warning [tech survey n11].
- Visual check on both loaders: tint, opacity and rotation change per block, and the overlay shows the quality.
- Unit tests: the same seed and cell give the same species. The cell pick never returns an invalid species [JC3 J2].
- GameTest: over 200 breaks per quality, with no enchantment, Fortune III and Silk Touch, the drops match the expected distribution.
- Unit test: yield per quality and for a special piece.
- A Processor run on a pristine T. rex skull gives the configured yield × filter efficiency, and the tube shows that percent, on both loaders (with F1.3).
- Unit test: the amber roll distribution with two species. GameTest on both loaders: the Processor processes amber. The tube hides its species. After the Sequencer sequences it, the drive entry names a species valid for its biome (with F1.3, F1.4).
- A player with a pouch in any inventory slot mines 40 fossils. They go into the pouch and merge. A click puts a fossil in and refuses any other item. A hopper under the pouch pulls fossils out. A full pouch sends the rest to the inventory.
- Each DNA item shows its state. A tube from a fossil shows its species. A tube from amber shows "Unidentified" and no species. The species shows only on the drive after sequencing. An embryo syringe from synthesis shows "Full" (with F1.6).

### F1.2: DNA from animals

**Old module:** B04 (syringe on vanilla animals) · **Section:** 11.2, 11.7 · **Size:** S · **Depends on:** F1.1, F1.4

**In game:** the player draws DNA from vanilla animals with a syringe. Each draw deals 1 HP, and many draws can kill the animal. The filled syringe goes straight into the Sequencer, and the empty syringe comes back [video 3:49-4:27]. A syringe on a chicken, parrot or turtle in love mode gives an Embryo Syringe. Its tooltip names the donor [video 11:22, s11.7, lead 2026-10-13].

**Touches:** `J/item/SyringeItem`, the Sequencer input filter.

**In scope:**

- Syringe on a vanilla mob: a random yield of 0 to 100 %, a small damage hit (`sequencer.syringe_damage`, default 1.0), no cooldown. Draws can kill the animal (D128). Example: 10 draws kill a 10 HP cow.
- The filled syringe tooltip reads like the video: "DNA Filled Syringe", "Contains 64 Red Parrot DNA" [video 4:07]. Variants (parrot colour, cat breed) stay apart.
- A syringe on a mob with no donor row gives a syringe with no usable DNA, and the tooltip says the animal is not a donor. Allay, strider, tadpole, happy ghast and sulfur cube are in this group (D64, answered) [recon RC64]. A trader llama uses the llama row.
- A syringe on a thing that is not alive gives nothing (GH#35).
- A syringe on a chicken, parrot or turtle in love mode gives an Embryo Syringe. F1.6 takes it [s11.7]. The turtle is new (D127). One item serves all donors. Its tooltip names the donor, for example "Embryo Syringe (Turtle)" (D146 answered) [lead 2026-10-13]. An embryo draw has no effect on the animal: it stays in love mode and takes no damage (D144 answered).
- A syringe draw has no sound of its own. It plays an existing vanilla sound (D228) [lead 2026-10-13].

**Out of scope:** syringes on dinosaurs, remains, egg samples, raw meat (F2.7). Sea lamprey ore as a marine source [JC3 B04.2]. The Identification Tag: the lead removed it from Beta on 2026-10-13 (Z-8). Sequencing identifies amber DNA (F1.1, F1.4).

**Config (defaults):** `sequencer.syringe_damage` 1.0.

**Produces:** filled syringes with `DNAData.source = SYRINGE`, the Embryo Syringe (bird or turtle).

**Recipe rows:** syringe. This package adds the row from the approved recipe table (D78). Beta has no Identification Tag and no Identified tube recipe (Z-8).

**Art:** see Art list → Lab items (syringe states). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- A filled syringe from a cow enters the Sequencer, adds its DNA, and the empty syringe returns to the leftover slot, on both loaders.
- Unit test: the yield range is 0 to 100 %.
- A syringe on an allay gives no usable DNA, and the tooltip says so [recon RC64].
- A syringe on a minecart does nothing (GH#35).
- A syringe on a chicken or a turtle in love mode gives an embryo syringe. A chicken not in love mode gives DNA.
- 10 draws on a 10 HP cow kill it.
- A turtle embryo gives an Embryo Syringe whose tooltip says "Embryo Syringe (Turtle)".

### F1.3: Fossil Processor

**Old module:** B05, plus the Processor parts of B12, its recipe rows of B13 and its power draw of B14 · **Section:** 11.2, 11.4, 11.8, 4.2, 7.4 · **Size:** L · **Depends on:** F1.1, F1.10, F1.11

**In game:** the player builds the Fossil Processor from four parts and runs it as the video shows [video 0:23-2:16], with the section 11.4 changes. It takes one fossil or amber, water, an empty test tube and a filter. It gives one DNA tube plus by-products in a 3×3 grid. Tank upgrades change its model. It draws power only while it runs. Hoppers and pipes run it with no player. It makes its own sounds when it starts, runs, finishes and loses power. This package delivers the whole machine.

**Touches:** `J/block/ProcessorBlock`, `J/block/entity/ProcessorBlockEntity`, `J/menutypes/ProcessorMenu`, the Processor screen, `J/item/FilterItem`, `J/client/renderer/ProcessorRenderer`, `geo/block/processor.geo.json`, the Processor part items, Balm fluid tank, loader item storage, sound events.

**In scope, parts and building [video 0:23-0:38]:**

- Four parts, in this order: base, body, lid, tanks. The lid and the tanks can face either way.
- The tanks show as the tank bones. The base, the drum and the lid cap are cubes of the root bone `FossilProcessor` today. Each build step shows its placed part, so they need bones of their own (bone split below).
- The F1.10 master and part code places each part. The model grows part by part. Only placed parts show.
- The full machine looks like the current model `geo/block/processor.geo.json` with `textures/block/processor.png` [lead 2026-10-12]. Bones: `FossilProcessor`, `water_port`, `power_port`, `base_tanks`, `iron_tanks`, `gold_tanks`, `diamond_tanks`.
- A creative-only item places a full Processor (F1.10 rule).

**In scope, bone split (developer, no new art):**

- In `processor.geo.json` and its .bbmodel, move the base, drum and lid cap cubes out of `FossilProcessor` into bones of their own. Keep each cube's position, size and UV. The texture does not change.
- Reason: each build step shows its placed part [video 0:23-0:34]. GeckoLib shows and hides bones, not single cubes.

**In scope, screen and flow (parity) [video 1:07-2:05]:**

- A left column holds, top to bottom: the filter slot (empty: a grey mesh disc), the bucket slot (empty: a grey bucket), and the water gauge (a slim tube with tick marks and animated water that rises on fill and falls while the machine runs).
- The fossil input slot sits at the top centre. A down-arrow under it fills as progress runs. Hover shows time left and percent, for example "3m 53s (3%)".
- The 3×3 output grid sits under the arrow. The empty test tube slot sits in the right column.
- The gauge tooltip shows "500mB / 2000mB" style text.
- A worn filter shows a durability bar.
- A status bar runs down the left outside edge, as on every machine screen [video 1:07].
- Screen art: the current `textures/gui/processor.png` [lead 2026-10-12].
- The finished tube tooltip names the species and the DNA percent with decimals [s11.4 over video 1:23].
- The Processor tab icon for the F1.10 tab host: a bone, in an active and an inactive look [spec].

**In scope, numbers:**

- One run always gives one tube. Results keep decimals. `processor.rounding` sets NONE, HALF or WHOLE. Default NONE [s11.4].
- Run time: 4 minutes by default. Chips: iron 3, gold 2, diamond 1 minute [spec, video 2:00-2:11]. The design doc's "25 min per stack" is overridden by the video and the spec.
- Tanks: 2 buckets by default, iron 3, gold 4, diamond 8 [spec]. The video shows 4,000 mB for its second upgrade. The spec wins for diamond. Beta has no netherite Processor tank (D143 answered).
- Water per run: `processor.water_per_run_mb`, a config value. The task plan proposes the start value, and playtests set it (D124) [lead 2026-10-12].
- Filters: iron 25 %, gold 50 %, diamond 100 % efficiency. A worn filter falls to 25 % of its base [spec]. Fix NaN for `dev_filter`. Durability: iron 150, gold 250, diamond 500 runs, as in the original, in config (D124) [orig 1.16, lead 2026-10-12]. Today the mod gives 100 to all three.
- By-products: sand, gravel, iron nugget, gold nugget, rarely diamond. Weights in TOML [s11.4]: about 70 % sand or gravel, 25 % nuggets, 1 % diamond (about 1 diamond in 100 runs, D220) [lead 2026-10-13].
- One input at a time. No mixed queue, no parallel upgrade [s11.4].
- Inputs: fossil, amber (F1.1), remains and raw meat (F2.7) as `ProcessorInput` sources.

**In scope, automation:**

- Hopper and pipe support on both loaders. Item storage per side (input, filter, tube, output). Balm fluid tank provider. Water from pipes and buckets.

**In scope, upgrades and their looks (D85):**

- Tank tiers change the model [video 1:40-2:10]. The renderer shows one tank bone per tier: `base_tanks` (default), `iron_tanks`, `gold_tanks`, `diamond_tanks`. These bones exist in the current model.
- Chips are the 3 shared chips of F1.10 (D113). Chip tiers change the look [s11.8] by a texture swap on an existing bone. No new part (D85, answered). The artist proposes the bone, and the lead approves it.
- Filters sit in a slot. They are not model upgrades [inf].

**In scope, power draw:**

- The Processor draws `power.machine_draw.processor.<chip>` only while a run makes progress. It draws zero while idle [s11.8].
- Draw [lead 2026-10-13]: 20 FE per tick at base, 28 with an iron chip, 44 gold, 92 diamond (the F1.10 run-cost rule). Filters cost no extra power (D141).
- With no power, the run pauses and keeps its progress.

**In scope, animation (D95, answered):**

- No animation. The video and the original show none (lead 2026-10-12) [video 0:36-0:50, 1:45-2:15, orig 1.16]. No part of the model moves.
- The fault effect (smoke, dark model) and the idle static model use the F1.10 code. This package supplies the Processor's dark model and idle-pose model.

**In scope, sounds [lead 2026-10-12]:**

- Sound events per state: start, working loop, finished, power-down. The loop plays only while the machine is active. Each event has its own file. Sources name a processor "Active" and "Finished" sound [2020 board].
- Sound assets (D129): each event uses the Processor's own asset where one exists, and a generic lab sound for the rest. The artist also makes sounds of its own for each machine.

**In scope, faults:** the Processor raises NO_POWER, NO_INPUT (no fossil, water or tube), WRONG_FILTER (no filter or a spent filter) and OUTPUT_FULL in the F1.10 fault mask. Each shows its reason in the warning tab.

**Recipe rows:** Fossil Processor base, body, lid and tanks (assembly part). Iron, gold and diamond filters. Test tube (3 per craft). This package adds the rows from the approved recipe table (D78). Chips and tank upgrades are shared items of F1.10.

**Out of scope:** a mixed queue, parallel upgrades, a refined-processing upgrade [design doc, s11.4]. Dust or water particles.

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `processor.rounding` | NONE | [s11.4] |
| `processor.time_minutes.<chip>` | 4, 3, 2, 1 | [spec, video 2:00] |
| `processor.tank_buckets.<tier>` | 2, 3, 4, 8 | [spec] |
| `processor.filter_efficiency.<tier>` | 0.25, 0.5, 1.0 | [spec] |
| `processor.filter_durability.<tier>` | 150, 250, 500 runs | [orig 1.16], D124 |
| `processor.water_per_run_mb` | set in the task plan, tuned in playtest | D124 |
| `processor.byproduct_weight.<item>` | sand and gravel 70, nuggets 25, diamond 1 | [s11.4], D220 |
| `power.machine_draw.processor.<chip>` | 20, 28, 44, 92 FE per tick | [s11.8], D141, F1.11 power numbers |

**Produces:** `ProcessorBlock` with its part list, `ProcessorBlockEntity` side handlers, `ProcessorInput` (fossil, amber, remains).

**Art:** see Art list → Fossil Processor, Lab items (filters, test tube, Processor part sprites), Screens (GUI) (Processor screen, tab icon). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- A survival player builds the Processor from its four parts in the video's order, on both loaders. A part out of order is refused.
- Breaking any part of a full Processor drops every slot once and returns the other parts as items (GameTest).
- The screen has every content item of the video at 1:07-2:05, with the current GUI texture (screenshots in the PR).
- A hopper chain feeds fossils and pulls tubes for 10 runs with no player, on both loaders.
- Unit test on the rounding modes. The by-product weights add up to the expected sum.
- A base-tier run takes 4 minutes. With each chip, 3, 2 and 1 minute.
- Each tank tier shows its model and holds 2, 3, 4 or 8 buckets. Each chip tier shows its texture swap (D85).
- A diamond filter gives four times the DNA of an iron filter. A worn filter gives a quarter of its base. An iron filter lasts 150 runs, gold 250, diamond 500.
- The energy meter falls only while a run makes progress. A gold chip draws more than an iron chip.
- With no power, the run pauses, the warning tab says "No power", and the machine smokes and goes dark. With no filter, the warning tab names the filter.
- No part of the model moves while it runs (D95).
- After the bone split, each build step shows only its placed part, and the full machine looks the same as before. The texture is unchanged.
- Each state plays its own sound event. The loop stops when work stops.
- Each recipe row crafts in survival.

### F1.4: Sequencer: sequencing and drives

**Old module:** B06, plus the Sequencer parts of B12, its recipe rows of B13 and its sequencing draw of B14 · **Section:** 11.5, 11.8, 4.3, 7.2 · **Size:** L · **Depends on:** F1.1, F1.10, F1.11

**In game:** the player builds the Sequencer from four parts and can dye it. The Sequence tab matches the video [video 2:45-4:50, 7:48-10:30]. The player puts in a drive and DNA. A cyan double helix grows as each sample runs. The list shows each species and variant with its percent. The Isolated view shows each gene and the animals still needed. The Sequencer door swings open while a player has the screen open, and the monitor glows while it works. The empty tube or syringe of each sample comes back. It draws power only while it sequences. A drive with 100 species syncs about 2.4 KB instead of 15 to 24 KB [tech survey n12] (estimate). This package delivers the Sequencer block, its frame screen and the Sequence tab. F1.5 adds the Edit tab and F1.6 the Synthesis tab.

**Touches:** `J/block/SequencerBlock`, `J/item/DiskStorageItem`, `J/api/DiskStorage`, `J/block/entity/SequencerBlockEntity`, `J/client/screen/SequencerScreen`, `J/client/renderer/SequencerRenderer`, `geo/block/sequencer.geo.json` (chip bones, split by the developer in F1.6), the Sequencer part items, sound events.

**In scope, parts and building [video 2:17-2:33]:**

- Four parts: base, door, computer, monitor. The door and the computer go in either order. The monitor comes after the computer.
- The F1.10 master and part code places each part. Only placed parts show.
- The full machine looks like the current model `geo/block/sequencer.geo.json` with `textures/block/sequencer.png` [lead 2026-10-12]. Bones: `Sequencer` (base), `computer`, `monitor_primary`, `mainBody`, `Door`. Behind the door the model has a tube tray, 3 system racks and 4 chip cubes.
- A creative-only item places a full Sequencer (F1.10 rule).

**In scope, screen frame (all Sequencer tabs) [video 2:45]:**

- Screen art: the current `textures/gui/sequencer.png` (frame and tab shapes) and `center_pieces.png` (ring arcs and "PN" logo) [lead 2026-10-12]. The "PN" logo sits in the middle with ring arcs that turn slowly [video 2:45].
- Three angled tab buttons at the top: "Sequence", "Edit", "Synthesis". The chosen tab is brighter.
- The status bar on the left edge.
- White Minecraft font. Positive values green, negative red, zero white.
- Clicking a slot opens a small popup with a title and one slot: "Insert Hard Drive", "Insert Genetic Material", "Collect Leftover Item". The main screen dims behind it [video 2:48-3:05, 5:45].
- The Sequencer tab icon for the F1.10 tab host: DNA, in an active and an inactive look [spec].

**In scope, Sequence tab [video 2:45-4:50]:**

- A "Regular | Isolated" toggle under the tab bar.
- A drive slot at the top left.
- A list box of "Name: N%" rows that scrolls. Hover or select shows a turning model of the animal and a card with its name, time period and diet. The card shows real species data. For a vanilla animal it shows the time period "Modern" and the real diet of the animal (D221) [lead 2026-10-13]. Each vanilla animal has one data entry for this. Remove the "Cretaceous/Apple" placeholder.
- An input slot at the bottom left, a double-helix track across the bottom, and a leftover slot at the bottom right. While sequencing, a cyan helix grows along the track. Hover shows time and percent ("9s (13%)").
- Each sample adds its DNA percent to the species entry [video 3:16-3:25, s11.5]. An Unidentified amber tube adds its DNA to the entry of its stored species, and the entry shows that species. Sequencing is the only step that identifies amber DNA (D83) [lead 2026-10-13, Z-8].
- The run uses up the sample's DNA. The empty tube or syringe goes to the leftover slot, and the player can use it again (D121) [lead 2026-10-12]. Neither the video nor the design doc gives this rule.

**In scope, Isolated view [video 7:48-10:30, s7.2]:**

- A left list of genes with the percent covered. A right list of the animals that the selected gene needs, each with its percent.
- Colour isolation needs a sample of each of the 15 tropical-fish colours except black [s11.6]. The drive records each colour. `geneCompletion(colour)` = colours sampled ÷ 15.

**In scope, drives:**

- Drive data: a map of species-or-variant id to percent (capped at 100), plus the biome-group share per species. No capacity limit. HDD: 10 s per sample. SSD: 5 s [spec, video 2:54].
- Drive data is a compact data component with a `StreamCodec`, not NBT [tech survey n12].
- Size target (D32 answered): test at 1,000 entries. Players have no entry limit.
- Variants (parrot colours, cat breeds, other vanilla variants) keep separate entries.
- A second tube of the same species must not reset progress (GH#34).
- A broken Sequencer drops all 9 slots. Fix `getContainerSize`.
- Fix the NaN from `DiskStorageItem.getGeneCompletion` for genes with no donor (gender).

**In scope, upgrades and their looks:**

- Dye [spec, s11.8, video 2:35-2:44]: right-click with a dye recolours every section of the current texture that is black today, as the spec says. The colour saves and syncs.
- Drives are inputs, not upgrades. The Sequencer's chips and tanks act on synthesis, so F1.6 owns them.

**In scope, power draw:**

- The Sequencer draws `power.machine_draw.sequencer.sequence` only while a sample runs. It draws zero while idle [s11.8]. Values: 20 FE per tick with a Hard Drive, 42 with an SSD [lead 2026-10-13].
- With no power, the sample pauses and keeps its progress.

**In scope, animation:**

- Door [orig 1.16, lead 2026-10-12]: the `Door` bone turns 90° about Y on its hinge. The pivot [23.5, 13.5, -2] already sits on a vertical door edge (asset repo `Sequencer.bbmodel`: origin x -7.5, door x -8 to 8). The open door shows the racks and chips inside.
- The door opens while any player has the Sequencer screen open and closes otherwise. The block entity syncs one "screen open" flag on change.
- Open and close each take 10 ticks, linear. A flip in the middle of a move starts from the current angle [orig 1.16].
- The renderer reads the synced flag and game time. It counts no frames.
- Screen: the helix grows with progress. The frame rings turn slowly, one turn per 20 s (D132) [video 2:45-3:10, lead 2026-10-12].
- Monitor glow (D132) [lead 2026-10-12]: a lit texture on bone `monitor_primary` shows while the Sequencer works. The original had no glow.
- The fault effect and the idle static model use the F1.10 code. This package supplies the Sequencer's dark model and idle-pose model.

**In scope, sounds [lead 2026-10-12]:**

- Sound events per state: start, working loop (computer sound), finished, power-down, door open, door close. Each event has its own file.
- Sound assets (D129): each event uses the Sequencer's own asset where one exists (the working loop), and a generic lab sound for the rest. The artist also makes sounds of its own for each machine.

**In scope, faults:** the Sequencer raises NO_POWER and NO_INPUT (no drive or no sample) in the F1.10 fault mask while it sequences.

**Recipe rows:** Sequencer base, door, computer and monitor. Hard drive (HDD). SSD. This package adds the rows to the recipe provider when the recipe table gives their ingredients (D78).

**Out of scope:** a DNA carrier with 3 synthesis charges [JC3 B06.2]. The DNA server block [s11.13]. The Ender SSD [spec "in the future"]: not in Beta (D142 answered).

**Produces:** `SequencerBlock` with its part list. `DriveData` with `percent(id)`, `biomeShare(species)`, `geneCompletion(gene)`. The "screen open" sync flag, reused by F1.7 and F1.8. The Sequencer screen frame, reused by F1.5 and F1.6.

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `sequencer.drive_seconds.<drive>` | 10 (HDD), 5 (SSD) | [spec, video 2:54] |
| `power.machine_draw.sequencer.sequence` | 20 (Hard Drive), 42 (SSD) FE per tick | [s11.8], D141, F1.11 power numbers |

**Art:** see Art list → Sequencer, Lab items (drives, Sequencer part sprites), Screens (GUI) (Sequencer frame, Sequence tab, tab icon). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- A survival player builds the Sequencer from its four parts in the video's order, on both loaders. The monitor is refused before the computer.
- A broken Sequencer drops every item and returns the other parts as items, on both loaders.
- The Sequence tab and the Isolated view match the video at 2:45-4:50 and 7:48-10:30 (screenshots in the PR).
- Sequencing two parrot colours gives two entries.
- The biome share changes after a tube from a desert fossil.
- Unit tests on addition and the cap.
- A drive with 1,000 entries round-trips and syncs under the payload limit (D32).
- Unit test: 3 tropical-fish colours give a colour completion of 3 ÷ 15. Black adds nothing.
- An Unidentified amber tube, sequenced, adds its DNA to the drive entry of its species, and the entry names the species. A sequenced tube adds its DNA, and the empty tube comes back in the leftover slot. The player can fill that tube again in the Processor (D121).
- The door opens in 10 ticks when a player opens the screen, and two clients see the same angle at the same tick.
- The monitor shows its lit texture while a sample runs and its plain texture when idle. The rings make one turn per 20 s (D132).
- Dye turns every black section to the dye colour. The colour stays after a reload.
- The energy meter falls only while a sample runs. With no power, the warning tab says "No power".
- Each state plays its own sound event.
- Each recipe row crafts in survival.

### F1.5: Sequencer: gene editing

**Old modules:** B07, B08 · **Section:** 11.6, 4.3, 4.6, 7.2, 7.5, GH#49 · **Size:** XL · **Depends on:** F1.4

**In game:** the Edit tab matches the 2022 layout [video 4:50-10:25], with the section 11.6 thresholds and controls. A Basic view mixes donors and shows effects. An Advanced view edits isolated genes and colour layers. A risk panel shows the chance of failure before synthesis. The server checks every edit and saves it in the genome. An edit changes the animal in all four gene groups. An edited animal gets its new stats once, with no per-tick stat work [tech survey n13].

**Touches:** `J/api/DinoData`, `J/api/GeneData`, `J/api/Genes`, `J/registry/GeneInit`, `J/network/c2s/UpdateEditInfoPacket`, `J/client/widget/GeneButton`, `J/client/widget/ColorWheelWidget`, `F/datagen/GeneDataProvider` (now `neoforge`), `J/client/screen/SequencerScreen`, `nublar-gui` widgets, `J/menutypes/SequencerMenu`, new `J/api/gene/GenomeEffects`, `GenomeRisk`, `EditRules`.

**In scope, rules:**

- Thresholds in TOML: clone at 50, edit at 70, gender sure at 80 with a 30 % random result below. Donor slots open at 50, 75 and 100, maximum 3 [s11.6]. The video's 3, 5 and 9 rows are overridden.
- From 50 % the player can move the donor sliders. Gene sliders unlock at 70 % (D133) [lead 2026-10-12].
- Attack and defense edits need 90 % (D115) [design doc, lead 2026-10-12].
- Clone-count limit (D116) [design doc, lead 2026-10-12]: each 3 clones hatched add one edit slot. The hard drive stores the count, so a shared drive shares its unlocks. A new drive starts with 2 edit slots. The count rises the next time the drive is in a Sequencer (D140 answered).
- `DinoData` saves `layerColors`, the base species, the rolled variation (after hatch) and the overrides. `toNBT`, `fromNBT` and `copy` carry them. Today layer colours are never saved and never reach the client [entity survey fact 48]. The genome sync carries them.
- The server checks `UpdateEditInfoPacket`, rejects and logs a bad packet. Checks [recon RC12]: the menu is open, the percentages add up to 100, the base is at 50 % or more for a clone and 70 % or more for an edit, 90 % or more for an attack or defense edit, the edit count fits the drive's edit slots, the donor count fits the slots, only an isolated gene takes an override inside its range, a gender choice below 80 % keeps the 30 % random rule, and a special-gene choice uses one slot at most (D51).
- Gene registry additions: `thirst`, `energy`, `egg_clutch`, `gestation_time`. Keep `immunity`, which acts on Poison.
- Gene definitions stay a Java registry in Beta. Data-pack gene editing comes after Beta (D30 answered).
- `GenomeEffects` maps every gene to an attribute or a `SpeciesData` modifier. All four groups do something visible:
  - core stats: health, regen, strength, defense, speed, size
  - needs: stomach, eat rate, thirst, energy
  - behaviour: aggression, intelligence, herd/pack size, tamability, nocturnal
  - breeding and survival: fertility, clutch, gestation, immunity, underwater, heat, jump
- Behaviour genes feed the memories that F2.4 to F2.6 read (lands with those packages). The F2.0 `BehaviourGenes` reader maps each gene to its behaviour parameter (spec F2.0 "How genes change behaviour", D112).
- Effective value = species base × (1 + gene % ÷ 100) × growth fraction × clone variation [recon RC13].
- `GenomeEffects.apply` is the one rebuild function for derived stats [tech survey n13]. It runs at spawn, on a genome change, on a data pack reload and on a growth step. It has a "data not loaded" guard and a version stamp. The growth-step call and the size scaling of the growth end point land with F2.1.
- The `jump` gene raises jump height. A strong edit can clear the 3-block low-security post. The cap stays below the 8-block high-security post (D47 answered, reversed). The `heat_resistance` gene cuts fire and lava damage by its percent [recon RC25].
- Risk model: a pure `GenomeRisk` returns three values from donor share and edit count with TOML coefficients: incubation failure chance, a stat trade-off list, an abnormal-behaviour chance. The curve is D225 [lead 2026-10-13]. Here s = donor share ÷ 50 %, and the donor share is the total share of all donors [inf]:
  - incubation failure = 25 % × s² + 2 % per edit
  - abnormal behaviour = 10 % × s² + 1 % per edit
  - trade-off strength = min(100 %, 20 % + 60 % × s² + 5 % per edit). It is the share of a stat gain that its paired stat loses.
  - Example: a 40 % bee donor with no edits gives 16 %, 6 % and 58 %.
- Stat trade-offs: five pairs in data, with the D225 trade-off strength, growing with donor share and edit count (D45 answered) [recon RC10]: speed up lowers health, strength up raises hunger, size up lowers speed and raises hunger, defense up lowers speed, health up lowers regen.
- At hatch, the abnormal-behaviour chance picks one entry from the species `risk_behaviours`. Every risk behaviour keeps the warning before an attack. The Debug Stick names it (D44 answered) [recon RC11]. The list: stresses easily (threshold −20), flipped day and night, loner (maximum group 1), more aggressive (+30 %).
- Per-clone variation: stats within `editing.variation_percent`, size with the gender weights, layer colours, albino or melanistic at `editing.morph_chance` (0.05). It rolls at hatch, not at synthesis [lead 2026-10-06]. An egg carries no rolled values. F1.9 calls the roll.
- Gene data: regenerate it from the reconciliation's donor catalogue. `Vanilla Animal DNA Effects.txt` wins where sources differ. Unsigned values count as plus (D66). Fix 15 values, add 16 missing effects, fill 4 empty entries. Delete the allay entry. Villager DNA randomises every gene per use [recon RC62].
  - Add frog, camel, sniffer, armadillo, nautilus and villager [s11.6]. Wolf variants use the wolf entry (D65).
- Mob variants share the base animal's gene effects and keep their own palette (D65 answered). Add the eight wolf variant palettes [recon RC63].
- Green parrot DNA gives a raptor a green colour shift. No skin has the name "Delta" (D67 answered). The cosmetic extras of the older design doc stay: a greener skin from turtle DNA, a higher albino chance from rabbit DNA. Stat numbers come only from the new table (D68 answered).
- Horse and frog jump effects skip Tyrannosaurus, Triceratops and Brachiosaurus [recon RC65].
- Fix `GeneButton`, which writes 0 into `advancedGenes` on select.
- One gene scale: percent deltas, as the DNA Effects table writes them. A neutral donor adds 0. Fix the turtle, rabbit, spider, cat and chicken entries, which store multipliers [recon RC7].
- Donor effect = table value × min(1, share ÷ 0.5) [recon RC8].
- Per-clone colour roll at hatch picks a palette by weight, then a hue, saturation and lightness inside its ranges [JC3 J21]. A palette has hue, saturation and lightness ranges and a weight (default 10). Each species lists about 6 palettes per colour layer, as a species key ("planned: F1.5"). A morph replaces the roll.
- An outline layer may carry an HSL offset from another layer [JC3 J21].
- A random roll never gives neon: hue 0 to 359, saturation 20 to 35, lightness 26 to 38 [JC3 J21].

**In scope, screen (with parity):**

- Basic view [video 4:50-7:00, s11.6]:
  - A base row and donor rows. To pick a donor, the player clicks an empty donor row, then clicks an animal in the DNA list on the right. The player drags the slider of the row to set its share. The total stays 100 % (D224) [lead 2026-10-13].
  - A turning preview of the animal with a player for scale. The client makes the preview with vanilla `EntityType.create`, with no server entity [JC3 B08.2].
  - A list of sequenced animals.
  - An effect panel in two columns. Each effect has a title ("Speed +3%") in green, red or white and a colour bar. A large edit names the paddock effect, for example "Size +40 %: needs the high fence" [recon RC15].
  - Each donor colour bar has two swatches. A click puts that colour on the primary or the secondary layer. By default the first colour is primary and the second secondary. The swatch swap stays (D133) [video 5:38-5:56, lead 2026-10-12].
- Advanced view [video 6:35-10:25, s11.6]:
  - A grid of gene bars. Dull bars cannot be edited. Purple bars are isolated and editable. The selected bar is red. Hover shows the gene name.
  - Number genes: a box with the percent and a slider limited to the gene's range ("Immunity +40%"). The range grows with the DNA % (D223) [lead 2026-10-13]. It is ±10 % of the species base at 70 %, and grows in a line to ±30 % at 100 %. Example: ±17 % at 80 %.
  - Choice genes (gender, skin variant, special gene) from a list. Gender is a button that cycles Male, Female, Random, with a "Reset Gene" button.
  - Colour: a colour wheel with a brightness strip and a tinted backdrop, plus saturation, opacity and tint strength per layer. Primary and secondary mode is the default colour mode. Fixed layers (claws, teeth, nostrils, eyes, mouth) are not listed.
- Risk panel: incubation failure chance, stat trade-offs and abnormal-behaviour chance from `GenomeRisk`. It shows the chance only, never the behaviour [recon RC16]. Position: a strip under the effect panel in both views, plus a one-line summary on the Synthesis tab (D133) [lead 2026-10-12].
- The skin variant list unlocks at 100 %. Below that it shows the biome-decided variant, read-only.
- Widgets live in `nublar-gui`. Menus have no `Minecraft.getInstance()`.
- Build the colour-wheel texture once and share it. Today each screen allocates a new 400 × 400 texture (640 KB) in `ColorWheelWidget.java:45,110` [tech survey d15].
- The player tries colours on the preview and commits them with Apply. A badge shows "Unapplied changes" until then [JC3 J22]. Button presses use the vanilla menu button route, checked on the server. Natural and random rolls use the server random. Reset returns the species default. Only Apply changes the genome, through the checked edit packet.
- A player copies colours between items of the same species. A copy between species or from an item with no colours fails with a reason [JC3 J22].

**In scope, power, animation and sounds:**

- Power draw: none. Editing runs no process, and a machine that does no work uses no power [s11.8].
- Animation: screen only. The preview turns, and the F1.4 frame rings turn. No model motion.
- Sounds: none of its own. Every button, Apply included, uses the vanilla click.

**Recipe rows:** none. The Edit tab adds no item.

**Out of scope:** the machine tabs (F1.10), disease risk, hybrids, special-gene effects (F2.7), skin rendering (F2.2). Gene JSON (D30). Size-linked stats [JC3 B07.4]. Mutation in the Sequencer. The design doc's 90 % gate for attack and defense edits is now in scope (D115).

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `editing.clone_threshold` | 50 | [s11.6] |
| `editing.edit_threshold` | 70 | [s11.6] |
| `editing.gender_sure_threshold` | 80 | [s11.6] |
| `editing.gender_random_chance` | 0.30 | [s11.6] |
| `editing.donor_slot_thresholds` | 50, 75, 100 | [s11.6] |
| `editing.attack_defense_threshold` | 90 | [design doc], D115 |
| `editing.clones_per_edit_slot` | 3 | [design doc], D116 |
| `editing.variation_percent` | set in the task plan | [s11.6] |
| `editing.morph_chance` | 0.05 | [s11.6] |
| `editing.slider_range` | ±10 % at 70 % DNA, ±30 % at 100 % DNA, linear | D223 |
| `editing.risk.failure` | 25 % × s² + 2 % per edit | D225 |
| `editing.risk.abnormal` | 10 % × s² + 1 % per edit | D225 |
| `editing.risk.tradeoff` | 20 % + 60 % × s² + 5 % per edit, at most 100 % | D225. The pairs: D45 answered |

**Produces:** complete `DinoData`, `GenomeEffects.apply(Dinosaur)`, `GenomeRisk.evaluate(DinoData)`, `EditRules`.

**Art:** see Art list → Screens (GUI) (Edit tab, colour wheel background and blip, value bar side and middle, risk panel). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- Unit tests: donor slot count per percent, the gender rule, risk rises and never falls, `DinoData` NBT round trip with colours and base species, a second rebuild with an unchanged stamp changes nothing.
- Unit test and GameTest: the server rejects out-of-range, non-isolated and forged edits, a sure gender below 80 % and a second special gene [recon RC12]. It also rejects an attack or defense edit below 90 % and an edit beyond the drive's edit slots (D115, D116).
- Unit test: a drive with 3 hatched clones has one more edit slot than a drive with 2. A second player who uses the same drive gets the same edit slots (D116).
- At 60 % the player moves a donor slider, and no gene slider moves (D133).
- Unit test: a 40 % bee donor with no edits gives 16 % failure, 6 % abnormal behaviour and 58 % trade-off (D225). At 80 % DNA a number gene slider stops at ±17 % (D223).
- In game on both loaders: a +30 % speed edit changes the movement speed attribute.
- A herd size edit changes the group memory, and a fertility edit changes the clutch roll (lands with F2.5).
- A GameTest per gene group: an edit changes the value that the gene's reader uses (behaviour group lands with F2.4 to F2.6) [recon RC13].
- Unit tests: a turtle donor raises health, a neutral donor adds 0, a donor at 25 % gives half its value [recon RC7, RC8]. Horse or frog DNA adds no jump to the three large species [recon RC65].
- An animal with the maximum jump edit does not clear a low-security fence [recon RC25].
- Unit tests: 1,000 natural rolls stay inside the palette ranges. A morph replaces the palette result [JC3 J21].
- The Edit tab matches the video at 4:50-10:25 except the section 11.6 changes (screenshots in the PR).
- Every control round-trips to the server and back after the screen opens again.
- A reviewer edits speed, gender and a layer colour. The hatched animal shows all three (colour lands with F2.2).
- Opening the Edit tab again allocates no new colour-wheel texture.
- A size edit that needs the high fence names it in the effect text [recon RC15].
- An unapplied colour roll leaves the genome unchanged after the screen closes. A colour copy between species fails with its reason [JC3 J22].

### F1.6: Sequencer: synthesis

**Old module:** B11 (synthesis), plus the synthesis draw of B14 · **Section:** 11.7, 11.8, 4.4, 7.4 · **Size:** M · **Depends on:** F1.2, F1.5

**In game:** the Synthesis tab matches the video [video 7:00-7:45]. The player fills water, sugar, bone matter and plant matter, puts in the bird-embryo syringe and presses Begin. An embryo picture fills with cyan. The output is a dinosaur-embryo syringe that carries the genome [s11.7]. Chips make it faster and tanks hold more, and each upgrade shows on the model (D85). It draws power only while it runs. This package delivers the whole synthesis part of the Sequencer.

**Touches:** `J/block/entity/SequencerBlockEntity` (synthesis), `J/client/screen/SequencerScreen` (Synthesis tab), `J/client/renderer/SequencerRenderer` (upgrade looks), new `J/item/EmbryoSyringeItem`, sound events.

**In scope, screen and flow [video 7:00-7:45]:**

- The F1.4 frame. Four corner groups (Water top left, Sugar bottom left, Bone Matter top right, Plant Matter bottom right), each with a slot, a label and a bar that is bright cyan when full.
- A centre picture of an embryo in a DNA helix fills with cyan from the bottom as the run goes.
- A "Begin" button that reads "Cancel" while running. Hover shows time and percent ("9m 59s (0%)").
- At the bottom: the input slot, an arrow that fills, the output slot. Slot popups ("Insert Water", "Insert Bone Matter", "Insert Syringe").
- The risk summary of F1.5 shows before the player presses Begin [s11.6].

**In scope, function and numbers:**

- Synthesis takes a bird-embryo syringe and gives a dinosaur-embryo syringe that carries `DinoData` [s11.7]. The video's empty test tube input and test tube output are overridden.
- The output's DNA state is Full (F1.1).
- Ingredients [video 7:08-7:20]: water, sugar, bone matter and plant matter.
- Bone matter: bone meal, or a bone block that counts as 9 bone meal (D134) [lead 2026-10-12].
- Plant matter: the item tag `projectnublar:plant_matter`, a list in config. It holds leaves, saplings, wheat, kelp, grass and flowers, and the same items of other mods (D134) [commission, lead 2026-10-12].
- Use per run (D93, answered): the task plan reads the amounts from the video at 7:20-7:35 and ships them as config defaults. Playtests set the final values.
- Cancel gives back the bird-embryo syringe and the ingredients that the run did not use. A cancelled run uses no DNA from the drive (D134) [lead 2026-10-12].
- Time: 10 minutes by default. Chips: iron 7, gold 4 [spec, video 11:00-11:11], diamond 2 (D139 answered) [lead 2026-10-13]. This replaces the spec's "no diamond chip".
- Tanks: default 1 bucket and 16 matter, iron 1.5 and 24, gold 2 and 32, diamond 2.5 and 40, netherite 3 and 48. Fill steps of 500 mB [spec, video 10:41-10:57].
- A player cannot start a run that will fail. Synthesis accepts only a genome at or above the clone threshold of `EditRules`. Use a vanilla slot filter [JC3 B11.11].
- Parse `DNAData` on a slot change, not twice per tick [entity survey S9].

**In scope, upgrades and their looks (D85):**

- Chips (iron, gold, diamond) and tanks (iron to netherite) are the shared upgrade items of F1.10 (D113). This package sets their effect on synthesis.
- Every upgrade changes the look [s11.8], with no new part (D85, answered). Chips: the current model has 4 chip cubes (`computer_chip01` to `04` in the asset repo) inside `mainBody`, behind the door. A developer moves them into bones of their own (bone split below). Proposal, the lead decides: more chips show at higher tiers. The original added a computer part per chip tier [orig 1.16]. Tanks: a texture swap on an existing bone. The artist proposes the bone, and the lead approves it.

**In scope, bone split (developer, no new art):**

- In `sequencer.geo.json` and its .bbmodel, move the 4 chip cubes (`computer_chip01` to `04`) out of `mainBody` into 4 child bones of their own. Keep each cube's position, size and UV. The texture does not change.
- Reason: GeckoLib shows and hides bones, not single cubes. Each chip tier shows its chips this way.

**In scope, power draw:**

- Synthesis draws `power.machine_draw.sequencer.synthesis.<chip>` only while a run makes progress. It draws zero while idle [s11.8]. Values: 20 FE per tick at base, 30 iron, 55 gold, 115 diamond (the F1.10 run-cost rule) [lead 2026-10-13]. The power numbers assume one Sequencer job at a time, sequencing or synthesis (power draft check E2). If both can run at once, the peak is 115 + 42 FE per tick.
- With no power, the run pauses and keeps its progress.

**In scope, animation:**

- Screen only: the embryo fills, the arrow fills, the frame rings turn (F1.4) [video 7:19-7:30].
- No model motion [orig 1.16]. The upgrade looks are bone visibility changes.

**In scope, sounds [lead 2026-10-12]:**

- Sound events: synthesis start, synthesis loop, synthesis done, power-down. Each has its own file. Begin and Cancel use the vanilla button click.
- Sound assets (D129): each event uses the Sequencer's own asset where one exists, and a generic lab sound for the rest. The artist also makes sounds of its own for each machine.

**In scope, faults:** synthesis raises NO_POWER, NO_INPUT (a missing ingredient or syringe) and OUTPUT_FULL in the F1.10 fault mask.

**Recipe rows:** none of its own. Chips and tanks are shared items of F1.10. The Embryo Syringe comes from F1.2. The dinosaur-embryo syringe is a machine output with no recipe.

**Out of scope:** a sped-up synthesis animation on the model (an idea only in the spec). The video's artificial egg and crafting step [s11.7].

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `sequencer.synthesis_minutes.<chip>` | 10, 7, 4, 2 | [spec, video 11:00], D113, D139 |
| `sequencer.ingredients_per_run.<ingredient>` | read from the video at 7:20-7:35 in the task plan | D93 |
| `sequencer.tank_buckets.<tier>` | 1, 1.5, 2, 2.5, 3 | [spec] |
| `sequencer.tank_matter.<tier>` | 16, 24, 32, 40, 48 | [spec] |
| `power.machine_draw.sequencer.synthesis.<chip>` | 20, 30, 55, 115 FE per tick | [s11.8], D141, F1.11 power numbers |

**Produces:** `EmbryoSyringeItem` with `DinoData`.

**Art:** see Art list → Sequencer (synthesis tab, chip and tank looks, synthesis sounds), Lab items (bird and dinosaur embryo syringes), Screens (GUI) (Synthesis tab). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- The Synthesis tab matches the video at 7:00-7:45 except the section 11.7 changes (screenshots in the PR).
- A base-tier run takes 10 minutes, 7 with an iron chip, 4 with a gold chip and 2 with a diamond chip (D139).
- A bone block adds 9 bone meal. Wheat, a sapling and a modded leaf block each count as plant matter.
- Cancel at 57 % returns the bird-embryo syringe and the unused ingredients. The drive keeps its DNA.
- Each tank tier holds its buckets and matter.
- Each chip and tank tier shows its approved look (D85).
- After the bone split, each of the 4 chips shows and hides on its own. The texture is unchanged.
- A genome below the clone threshold cannot start, on both loaders [JC3 B11.11].
- The output syringe carries the edited genome and shows "Full".
- The energy meter falls only while a run makes progress. A gold chip draws more than an iron chip. With no power, the warning tab says "No power".
- Each state plays its own sound event.

### F1.7: Egg Printer

**Old module:** B11 (printer), plus the Egg Printer parts of B12, its recipe rows of B13 and its power draw of B14 · **Section:** 11.7, 11.8, 4.4, 7.4 · **Size:** XL · **Depends on:** F1.6, F1.10, F1.11

**In game:** the player builds the Egg Printer from a base and three parts. It matches the video in behaviour [video 11:12-12:33] and prints the egg in code [commission, lead 2026-10-12]. The needle sweeps in a raster over the platform, the platform lowers as the egg grows, and the egg grows from the bottom up. The lid opens while a player has the screen open or an egg waits. The output is the unincubated egg [s11.7]. An idle printer shows a ghost egg outline. A failed print breaks the egg with an effect and gives a cracked artificial egg, the bone meal and the empty syringe. It draws power only while it prints. This package delivers the whole machine.

**Touches:** `J/block/EggPrinterBlock`, `J/block/entity/EggPrinterBlockEntity`, the printer screen, `J/recipe/UnincubatedEggRecipe` (remove), new printer renderer, `geo/block/egg_printer.geo.json` and its .bbmodel (bone split by the developer, no art change), the Egg Printer part items, sound events.

**In scope, parts and building [video 11:12-11:33]:**

- The base first. Then the needle, the platform and the body, in any order. These are the video names, not the design doc's Chassis, Lid, Printer Frame and Dome (D114).
- Parts in the current model: platform = bone `Panel`. The base, the body, the cross-rail and the needle are cubes inside bone `Body` today. The developer splits them into bones (bone split below).
- The F1.10 master and part code places each part. Only placed parts show.
- The full machine looks like the current model `geo/block/egg_printer.geo.json` with `textures/block/egg_printer.png` [lead 2026-10-12]. Bones: `Printer` (root), `Panel` (platform, 16 by 14 px, y 2.5-3.5), `Body`, `Egg` (y 3.5-13.5), `Door` (glass lid).
- A creative-only item places a full Egg Printer (F1.10 rule).

**In scope, bone split (developer, no new art):**

- In `egg_printer.geo.json` and its .bbmodel, move cubes out of `Body` into new bones. The cross-rail cube (a plane at y 15.5, x 0 to 16) goes into bone `rail`. The needle cube (x 7.5-8.5, y 13.5-15.5, under the middle of the cross-rail) goes into bone `needle`, a child of `rail`. The base cubes go into bone `base`. The side-rail planes (x -2.5 and x 18.5, y 16-18, z -5 to 6) stay in `Body`.
- Keep each cube's position, size and UV. The texture does not change. No artist work is needed.
- Reason: GeckoLib animates bones, not single cubes. The raster moves the needle and the rail apart, and the base is its own build step.

**In scope, screen [video 11:38-12:33]:**

- Screen art: the current `textures/gui/egg_printer.png`, `bonemeal_bar.png` and `egg.png` [lead 2026-10-12]. The status bar on the left.
- Left column: a slim bone-meal gauge that fills as bone meal goes in, the bone meal slot, the syringe slot.
- Centre: a large egg picture with progress lines.
- Right column: the egg output and the empty syringe output.
- Hover shows time and percent ("10m 0s (0%)").
- The Egg Printer tab icon for the F1.10 tab host: an egg, in an active and an inactive look [spec].

**In scope, function:**

- The printer takes the dinosaur-embryo syringe plus bone meal and gives the unincubated egg and the empty syringe [s11.7, video 12:16-12:17]. Remove `UnincubatedEggRecipe` and the crafting step.
- Without the Leveling Sensor, a print fails at a 10 % chance. The sensor removes the chance [spec, video 11:55-12:03].
- A failed print (D118) [lead 2026-10-12]: the embryo dies. The egg on the platform plays an egg-break effect. The output gets a cracked artificial egg, and the player gets the bone meal and the empty syringe back. The artificial egg item stays in the game only as this failed-print result.
- Time: 10 minutes by default. Chips: iron 8, gold 6, diamond 4 [spec, video 12:11-12:20].
- The printer saves its sensor and chip.
- Use bone meal and refill on events, not one item per tick with a block update [entity survey S9].
- Bone meal per egg: 16, so a stack of 64 prints 4 eggs (D222) [lead 2026-10-13]. Today's code uses the same value.
- Delete the uncracked artificial egg sprite `artificial_egg.png` (D227) [lead 2026-10-13]. The cracked artificial egg stays as the failed print. Today the unincubated and incubated egg sprites are byte copies of it, so they need their own art first (Art list).

**In scope, upgrades and their looks (D85):**

- Chips (shared F1.10 items, D113) set the print time. The Leveling Sensor removes the break chance.
- Every upgrade changes the look [s11.8]. The chips and the sensor each get a texture swap on an existing bone. No new part (D85, answered). The artist proposes the bones, and the lead approves them.

**In scope, power draw:**

- The printer draws `power.machine_draw.printer.<chip>` only while a print makes progress. It draws zero while idle [s11.8]. Values: 20 FE per tick at base, 26 iron, 37 gold, 58 diamond (the F1.10 run-cost rule). The Leveling Sensor costs no extra power (D141) [lead 2026-10-13].
- With no power, the print pauses and keeps its progress. The needle stops.

**In scope, animation (new) [orig 1.16, video 11:44-11:55]:**

- Bones [current model, lead 2026-10-12]: platform = `Panel` (pivot [8, 0.5, 0]). Lid = `Door` (pivot [8, 16.62, 7.17], the back top edge, turns about X). Egg = `Egg` (pivot [8, 0.5, 0]). Cross-rail = `rail` and needle = `needle` (child of `rail`), after the bone split above. No new part is needed.
- Platform travel: the original lowered the platform one egg length (6.5 px) over the print. In the current model `Panel` has only 0.5 px of room below it (the `Body` floor top is at y 2, the `Panel` underside at y 2.5). So `Panel` lowers by progress × 0.5 px, at most 0.5 px.
- Three states, each blended linearly over 10 ticks. A flip in the middle of a blend starts from the current value:

  | State | Active when | Active target | Idle target |
  | --- | --- | --- | --- |
  | Platform (`Panel`) | a job runs, or the output holds an egg or a cracked artificial egg | lowers by progress × 0.5 px (the travel the model has) | at rest (0 px) |
  | Needle raster (`needle` along X on the cross-rail, `rail` along Z on the side rails) | the job runs with power and is not finished | X: triangle wave ±2 px, one back-and-forth per 20 ticks. Z: triangle wave ±2 px, one back-and-forth per 4 ticks | X 0, Z 0 |
  | Lid (`Door`) | a player has the screen open, or the output holds an egg | 60° about X | 0° |

- The egg grows from the bottom up with progress. A clip plane or a scaled and masked `Egg` bone cuts it. The cut face is solid cream (`0xFFF7F1DD`). The egg sits on `Panel` and moves down with it. With an egg in the output, progress shows as 1.
- The printed egg is the species' egg model with the printed texture (D82, D135) [lead 2026-10-12]. It lands with F1.9. The original always showed one round egg.
- With no job and no output, the platform shows a ghost egg outline (D135) [lead 2026-10-12].
- A failed print plays the egg-break effect on the platform egg (D118).
- The server syncs "printing", the job start game time, total time and "output present" on change only. The client computes progress from game time plus the partial tick. It counts no frames.
- Body lamps (D237) [lead 2026-10-13]: the lamps of the current texture glow while the printer prints. The green lamp is lit while it prints. Proposal: the red lamp is lit while a fault stops it. The emissive texture is an Art list row.
- The fault effect and the idle static model use the F1.10 code. This package supplies the printer's dark model and idle-pose model.

**In scope, sounds [lead 2026-10-12]:** sound events for start, printing loop, finished, broken egg, lid open, lid close and power-down. Each event has its own file. Sound assets (D129): each event uses the printer's own asset where one exists, and a generic lab sound for the rest. The artist also makes sounds of its own for each machine.

**In scope, faults:** the printer raises NO_POWER, NO_INPUT (no syringe or no bone meal) and OUTPUT_FULL in the F1.10 fault mask.

**Recipe rows:** Egg Printer base, needle, platform and body. Leveling Sensor. This package adds the rows to the recipe provider when the recipe table gives their ingredients (D78). Chips are shared items of F1.10.

**Out of scope:** dyeing the printer (original only, not in section 11 or the spec). The video's artificial egg output and crafting step. The artificial egg stays only as the failed-print result (D118).

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `printer.minutes.<chip>` | 10, 8, 6, 4 | [spec, video 12:11] |
| `printer.break_chance` | 0.10 | [spec] |
| `printer.bone_meal_per_egg` | 16 | D222 |
| `power.machine_draw.printer.<chip>` | 20, 26, 37, 58 FE per tick | [s11.8], D141, F1.11 power numbers |

**Produces:** `EggPrinterBlock` with its part list. `UnincubatedEggItem` with `DinoData`. The printer's synced animation state.

**Art:** see Art list → Egg Printer, Eggs and hatching (printed textures), Lab items (Leveling Sensor, Egg Printer part sprites), Screens (GUI) (printer screen, tab icon). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- A survival player builds the Egg Printer from the base and its three parts, on both loaders. A part before the base is refused.
- A broken printer drops its sensor, chip and slots once and returns the other parts as items.
- The screen and the print animation match the video at 11:38-12:33 except the section 11.7 changes (screenshots and a clip in the PR).
- A base-tier print takes 10 minutes. With chips, 8, 6 and 4.
- Over 200 prints with no sensor, about 10 % fail. With the sensor, none fail.
- A failed print plays the egg-break effect. The output holds a cracked artificial egg, and the bone meal and the empty syringe come back. No embryo comes out (D118).
- An idle printer with an empty output shows the ghost egg outline. During a print the platform shows the species' egg model with the printed texture (D135).
- Each chip tier and the sensor show their approved texture swaps (D85).
- Two clients see the same platform height, needle position and egg height at the same game tick, at 30 fps and at 144 fps.
- Unit tests: the triangle waves, the 10-tick blend and the platform travel against progress.
- After the bone split, the needle and the rail move independently: the needle along X, the rail along Z. The base shows alone at the base build step. The texture is unchanged.
- The energy meter falls only while a print makes progress. With no power, the needle stops, the warning tab says "No power", and the machine smokes and goes dark.
- Each state plays its own sound event.
- Each recipe row crafts in survival.

### F1.8: Incubator

**Old module:** B11 (incubator), plus the Incubator parts of B12, its recipe rows of B13 and its power draw of B14 · **Section:** 11.7, 11.8, 4.4, 7.4 · **Size:** XL · **Depends on:** F1.7, F1.9, F1.10, F1.11

**In game:** the player builds the Incubator from five parts. It matches the video [video 12:21-15:05]. The player places each egg on a leaf bed in the screen, and the egg appears at that spot in the nest. Each egg gets a turn every 5 minutes [design doc, spec, lead 2026-10-12]. The lid opens, the robotic arm reaches the egg wherever it lies, turns it and folds back. The built-in lamp is the base bulb. The lamp glows, and each bulb upgrade makes the light brighter: level 9, 11, 13 and 15 (GH#23, D80). Eggs wobble now and then while they incubate. The Incubator holds 3, 6 or 9 eggs. It needs power and plant matter. Every species incubates for 30 minutes at the base lamp. This package delivers the whole machine.

**Touches:** `J/block/entity/IncubatorBlockEntity`, `J/block/IncubatorBlock`, `J/menutypes/IncubatorMenu`, the incubator screen, `J/registry/ItemInit` (container upgrades), `J/network/c2s/UpdateIncubator*Packet`, the incubator renderer (now inline in `ClientRegistrationHolder.java:71-93`), new `IncubatorArm` solver and timeline classes, the Incubator part items, sound events.

**In scope, parts and building [video 12:34-12:54]:**

- Five parts, in this order: base, nest, lid, arm base, arm. These are the parts of the current model (`body`, `nest`, `cover`, `arm_base`, `RoboticHand1` with its chain) and the mod's part items. No lift step: the current model has no lift part, and the lid lifts without one.
- The F1.10 master and part code places each part. Only placed parts show (GH#41). The arm sits on the left side, facing the player (GH#29).
- The full machine looks like the current model `geo/block/incubator.geo.json` with `textures/block/incubator.png` [lead 2026-10-12]. The claw has two fingers. The lid (`cover`) is a glass dome with a lamp cap and two small tabs on one edge. The glass stays translucent with shaders (GH#45).
- The hitbox matches the model (GH#30, GH#44, GH#51). Breaking one block does not chain-break neighbours (GH#31). No z-fighting on top (GH#32).
- A creative-only item places a full Incubator (F1.10 rule).

**In scope, function:**

- Capacity 3, 6 or 9 eggs [s11.7, video 13:53-14:00]. Fix the `ContainerUpgradeItem` registration (the spec flags the 6-egg tier "NEEDS FIXING").
- It needs power and plant matter. It stalls without either one. The built-in lamp is the base bulb, so it runs with no bulb item (D131) [lead 2026-10-12]. This replaces "the incubator needs a bulb" [s11.7].
- An egg that stays stalled for `incubation.stall_death_minutes` (default 5) dies (D131) [s11.7, lead 2026-10-12].
- Plant matter: 100, 150, 200 by tank tier [spec, video 14:55]. The plant matter tag `projectnublar:plant_matter` of F1.6 sets the items [commission].
- Plant matter drains per machine, not per egg, at `incubation.plant_matter_per_minute`. The task plan proposes the start value, and playtests set it (D122) [lead 2026-10-12]. GH#20 asks for about one stack per run.
- Time: 30 minutes for every species in Beta, scaled by the bulb (D130) [video, lead 2026-10-12]. This sets aside "incubation time is different for each species" [s11.7] for Beta. Bulb scale: default 1, warm 25/30, warmer 20/30, hot 15/30 (30, 25, 20, 15 minutes) [spec, video 14:07-14:36].
- Incubation failure comes from `GenomeRisk`. Roll it at completion, apart from hatch success.
- Progress syncs as a fraction, not as ticks, so the bar never wraps past 32,767 ticks [JC3 B12.26] [inf].
- `UpdateIncubatorSlotPacket` checks index and position. Remove `UpdateIncubatorPacket` or use it.
- Use plant matter and refill on events, not one item per tick with a block update [entity survey S9].
- The time left stops at 0 (GH#16).

**In scope, screen [video 12:50-13:50]:**

- Screen art: the current `textures/gui/incubator.png` [lead 2026-10-12]. It already holds the bed pane, the plant icon and the plant bar.
- A bed pane over the player inventory, with the status bar.
- Egg position in the pane = egg position on the nest. While the player holds an egg, the pane turns green where it can go and red when no more fit. A placed egg can be dragged to a new spot.
- A plant matter icon (a sprout outline when empty) with its number and a bar at the bottom left. Inserting plant matter plays a small icon and bar animation.
- Egg tooltip: species, percent, time left, then the F1.9 tooltip lines.
- The Incubator tab icon for the F1.10 tab host: a nest, in an active and an inactive look [spec].

**In scope, upgrades and their looks (D85):**

- Bulbs (warm, warmer, hot) set the time scale, change the lamp glow [video 14:07-14:36] and raise the light level (D80). Adapted to the current model: the lamp cap is the top cube of `cover`. An emissive texture per tier (base lamp and three bulbs) lights it. No new part. The original swapped one lamp model per tier [orig 1.16].
- Container upgrades (small for 6 eggs, large for 9) and plant tanks (iron, gold) change the capacity. Every upgrade changes the look [s11.8]. Each gets a texture swap on an existing bone. No new part (D85, answered). The artist proposes the bones, and the lead approves them.

**In scope, power draw:**

- The Incubator draws `power.machine_draw.incubator` only while it incubates. It draws zero with no egg [s11.8]. Values: 20 FE per tick with the base lamp, 25 warm, 33 warmer, 46 hot (the F1.10 run-cost rule) [lead 2026-10-13].
- Containers and plant tanks cost no extra power (D141).
- No water-bottle input (D226) [lead 2026-10-13].
- With no power, incubation stalls, and the stall timer of each egg runs.

**In scope, light (new):**

- A powered, working Incubator gives block light. Each bulb tier raises the level (GH#23) [lead 2026-10-12]. Levels (D80, answered): 9 with the base lamp, 11 warm, 13 warmer, 15 hot, only while it incubates. A block-state property carries the level and changes only on a state change.

**In scope, the arm (new) [lead 2026-10-12, spec, orig 1.16]:**

- **Bones** [current model, lead 2026-10-12]. Base yaw = `RoboticHand1` (pivot [1.5, 19.04, -0.04]). Shoulder = `RoboticHand2` ([1.5, 21.5, 2]). Elbow = `RoboticHand3` ([1.5, 28, 2.01]). Wrist, hand aim and egg roll = `RoboticHand4` ([1.5, 28, 8.53]). Claw fingers = `RoboticHand5` and `RoboticHand6` ([2.99, 28, 10.5] and [0.01, 28, 10.5], a mirror pair). The upper and lower links are 6.5 px each. The hand is about 5 px from the wrist pivot to the finger tips. `arm_base` stays still. The original had a wrist helper and a hand as two parts. Here `RoboticHand4` takes both roles. Pivots are the file values of `incubator.geo.json`.
- **Egg pose.** Each egg stores a nest position (from the screen click) and a pickup direction. The server chooses the direction once, at placement: from the egg toward a point at arm height minus a random 0.2 to 1.05 blocks, turned by a random yaw. It accepts the direction if the point 0.25 blocks along it lies inside the arm's reach. The reach comes from the rewrite model's link lengths (about 6.5 px each), not the original's 8.75/16 and 7/16 blocks. After a capped number of tries with no fit, the egg is not placed and the pane shows red. Poses save and sync.
- **Any egg in range.** The arm reaches any stored pose. There are no preset positions [spec].
- **Eggs in the nest.** Each egg draws at its pose, tilted along its pickup direction, with its species' egg model and printed texture (F1.9).
- **Trigger.** Each egg keeps the game time of its last turn. An egg is eligible when it sits in a slot, its incubation has power and runs, and `incubation.arm_turn_interval_ticks` (default 6,000 ticks, 5 minutes [design doc]) have passed since its own last turn. So each egg turns every 5 minutes, and the arm visits every egg (D131) [lead 2026-10-12]. The arm picks one eligible egg with the server random. It searches when idle for more than 75 ticks, or right after a turn. The original's 1,200 counted frames is a bug and is not copied.
- **Timeline** [orig 1.16], ticks from the pick:

  | Ticks | Phase | What moves |
  | --- | --- | --- |
  | 0-20 | Lid lift | `cover` lifts straight up 12.5 px with the sine ease, while the base turns. It is fully up before the reach starts at tick 35 |
  | 0-20 | Turn | Base yaw (`RoboticHand1`) moves to the egg's yaw |
  | 20-35 | Settle | Damped shake on base yaw (`RoboticHand1`): (π/30) × sin(8π i) × max(0.5 − i, 0), i = 0 to 1 over 15 ticks, peak about 3° |
  | 35-55 | Reach | Shoulder (`RoboticHand2`), elbow (`RoboticHand3`) and wrist (`RoboticHand4`) move to the solved angles. Hand roll (`RoboticHand4`) goes to +90° or −90° |
  | 55-65 | Grip | Nothing moves |
  | 65-115 | Spin | Hand roll (`RoboticHand4`) turns 180° more (3.6° per tick). The egg turns 180° about its long axis |
  | 115-125 | Release | Nothing moves |

  - Chain: if another egg is eligible at tick 125, the timer jumps to 35 and the arm moves straight there in 20 ticks. The roll direction flips for each egg in the chain.
  - Return: joints go to rest in ticks 0-20, base yaw goes to rest in 20-40, then the settle shake plays. The lid stays up for 30 ticks after the last turn ends, then lowers in 20 ticks [orig 1.16].
  - An egg removed during a turn makes the arm return from its current pose.
- **Solver.** A closed-form two-link solver (law of cosines) from the arm pivot. Base yaw from the horizontal direction to the hand target (egg position + pickup direction × hand length). The hand points down, turns to the approach yaw, and takes its pitch from the pickup direction. Clamp the reach so the solver never returns NaN. About 30 lines, exact, no iteration [orig 1.16]. The CHAOS FABRIK solver is not used.
- **Interpolation.** Linear in angle, the short way round, from a snapshot of the current pose. Progress clamps to 0 and 1. The arm keeps even-speed moves for parity, not eased moves (D131) [lead 2026-10-12].
- **State.** The server owns the active egg, the chain step, the phase start game time, each egg's spin angle and last-turn time. It saves them. It sends one event at each pick (egg index, start game time, chain flag). Clients compute the pose from game time plus the partial tick. Two players see the same pose.
- **Lid.** The lid (`cover` bone) lifts straight up by 12.5 px and lowers the same way [orig 1.16, video 13:38-13:46, lead 2026-10-12]. The bone moves by translation, so no hinge is needed. It is up while a player has the screen open, while an egg is active, and for 30 ticks after the arm last worked. Lift and lowering each take 20 ticks with a sine ease: height = (sin(π(x − 0.5)) × 0.5 + 0.5) × 12.5 px, with x = lid ticks ÷ 20. The lid is fully up before the arm reaches. The model has two small tabs on one edge of the lid. They do not change the lift.
- **Claw (D96 answered) [lead 2026-10-13].** The claw opens while the arm reaches, closes in the grip pause and opens at release. The two fingers `RoboticHand5` and `RoboticHand6` turn about Y in opposite directions. The original claw did not move [orig 1.16]. No arm light gradient.
- The fault effect and the idle static model use the F1.10 code. This package supplies the Incubator's dark model and idle-pose model.

**In scope, sounds [lead 2026-10-12]:** sound events for start, heater hum loop, egg placed, egg done, power-down, arm move (hydraulic), lid open and lid close. Each event has its own file. Sound assets (D129): each event uses the Incubator's own asset where one exists (the arm move), and a generic lab sound for the rest. The artist also makes sounds of its own for each machine.

**In scope, faults:** the Incubator raises NO_POWER, NO_PLANT_MATTER and NO_EGG in the F1.10 fault mask. The screen shows "No power" or "No plant matter". No NO_BULB: the built-in lamp is the base bulb (D131).

**Recipe rows:** Incubator base, nest, lid, arm base and arm. Warm, warmer and hot bulbs. Small and large container upgrades. Iron and gold plant tanks. This package adds the rows to the recipe provider when the recipe table gives their ingredients (D78).

**Out of scope:** a bedding block that speeds a natural hatch (deferred). Light that warms an egg outside an Incubator (dropped). A failed run that spawns a hostile mob (dropped) [entity survey B11.7 to B11.9]. A temperature slider for the male chance (D73) [JC3 B11.13]. Live-bearers [JC3 B11.10].

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `incubation.capacity.<tier>` | 3, 6, 9 | [s11.7, spec] |
| `incubation.plant_matter.<tier>` | 100, 150, 200 | [spec] |
| `incubation.bulb_scale.<tier>` | 1, 25/30, 20/30, 15/30 | [spec] |
| `incubation.stall_death_minutes` | 5 | [s11.7], D131 |
| `incubation.minutes` | 30, every species | [video], D130 |
| `incubation.plant_matter_per_minute` | set in the task plan, per machine, tuned in playtest | D122 |
| `incubation.arm_turn_interval_ticks` | 6,000 | [design doc] |
| `incubation.light_level.<tier>` | 9, 11, 13, 15 | D80 |
| `power.machine_draw.incubator` | 20, 25, 33, 46 FE per tick (base lamp, warm, warmer, hot) | [s11.8], D141, F1.11 power numbers |

**Produces:** `IncubatorBlock` with its part list. `IncubatedEggItem` with `DinoData`. The arm solver and timeline (pure classes with tests).

**Art:** see Art list → Incubator, Eggs and hatching, Lab items (bulbs, container upgrades, plant tanks, Incubator part sprites), Screens (GUI) (incubator screen, tab icon). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- A survival player builds the Incubator from its five parts in the video's order, on both loaders. A part out of order is refused.
- Breaking any part drops every egg and slot once and returns the other parts as items. Breaking one Incubator leaves its neighbour intact.
- The screen, egg placement and arm turn match the video at 12:50-13:50 and 13:38-13:46 (screenshots and a clip in the PR).
- GameTest on both loaders: a creative setup prints and incubates three eggs, one per capacity tier.
- An egg stalled for 5 minutes dies. An egg stalled for 4 minutes lives.
- An Incubator with no bulb item runs at the 30-minute base time.
- Plant matter falls at the same rate with 1 egg and with 9 eggs (D122).
- With the interval set to 100 ticks for the test, each egg turns once per interval, and the arm visits all 9 eggs. At 30 fps and at 144 fps the turn starts at the same game tick.
- Two clients watch one Incubator. They see the same egg turned at the same tick, with the same pose.
- Nine eggs at nine different nest spots are each reached with no NaN and no clipping through the nest.
- A world reload keeps each egg's last-turn time and spin angle.
- An egg taken out during a turn makes the arm return at once.
- Unit tests: the two-link solver against known poses with the 6.5 px links, the timeline phase at each tick, the lid ease curve.
- While it incubates, the light level is 9 with the base lamp, 11 warm, 13 warmer and 15 hot (D80). Each bulb, container and plant tank tier shows its approved look (D85).
- Two clients see the claw open and close at the same tick (D96).
- An incubation longer than 32,767 ticks shows a bar that rises with no wrap [JC3 B12.26].
- The energy meter falls only while an egg incubates. With no plant matter, the warning tab shows NO_PLANT_MATTER. With no power, the machine smokes and goes dark.
- Each state plays its own sound event.
- Each recipe row crafts in survival.

### F1.9: Eggs and hatching

**Old module:** B11 (eggs, hatch) · **Section:** 11.7 · **Size:** L · **Depends on:** F1.7

**In game:** each species has its egg model from the asset repo, with a natural texture and a printed texture. Eggs wobble now and then in the Incubator. The player places an incubated egg on the ground. The egg is an entity that dinosaurs can push. It starts to shake at once and cracks in 3 stages. 60 s after placing, a baby steps out with shell bits, a crack sound and a hatch sound.

**Touches:** `J/item/IncubatedEggItem`, `UnincubatedEggItem`, a new placed-egg entity (D05), egg models and textures, sound events, particles.

**In scope, egg models (new) [lead 2026-10-12]:**

- One egg model per species, from the asset repo egg folders (D82, answered). Brachiosaurus: `round`. Tyrannosaurus: `tyrannosaurus`. Triceratops: `tall_3`. Gallimimus: `normal`. Velociraptor and Dilophosaurus: `velociraptor`. `SpeciesData.egg_model` names the model.
- Natural texture: one per species (laid in a nest, F2.5) [card "Textures"]. A natural (bred) egg has the colours of its own species on the shared model (D82).
- Printed texture: one per egg model, 5 in total, plus a species tint in code (D236) [lead 2026-10-13].
- The artist questions on these models (shared cubes, missing `tall_3` texture, black QR textures) are Art list question 9. F1.9 is not done until the artist answers them.
- The unincubated and incubated egg items have their own sprites.
- The printer, the nest in the Incubator and the placed egg all use the same model registry.

**In scope, wobble while incubating (new) [design doc, lead 2026-10-12]:**

- An egg in the Incubator rocks gently side to side, about 4°, at a random moment every 30 to 90 s (D81, answered). The egg in the claw does not rock.
- Wobble times come from a seeded random of the egg's id and the game time. Every client sees the same wobble with no packet.
- The interval and the angle are config values.
- The original has no egg wobble [orig 1.16]. This is new design.

**In scope, tooltip [lead 2026-10-06]:**

- The unincubated and incubated egg show species, donor mix, the player's edits, the incubation-failure chance from `GenomeRisk` and the chosen gender. Below 80 % the gender line says "may be random". No rolled traits. Translation keys. Cache the parsed genome so a hovered tooltip does not parse every frame.

**In scope, the placed egg and hatch:**

- The player places the incubated egg. There is no wait: the shake starts at once, and the baby comes out 60 s after placing (D123) [lead 2026-10-12].
- Shake: 60 s in 3 stages of 20 s. The egg rocks every 4 s, then every 2 s with a first crack, then every second with a second crack and shell chips. Each stage rocks harder (D81) [lead 2026-10-12].
- The placed egg is an entity, not a block (D05, answered) [lead 2026-10-12]:
  - The entity stores `hatchGameTime` and a copy of the `DinoData`. It reads its crack stage from game time, with no per-tick counter.
  - Dinosaurs can push it.
  - On chunk load, an egg hatches at once if its hatch time is in the past.
  - This replaces the plan default "a block with a block entity that players bump into and dinosaurs walk through" [entity survey B11.1, B11.2, S1].
- A player who breaks a placed egg gets an egg item with its `DinoData` [JC3 B11.15].
- Space check: before the hatch, test free space for the baby box. If blocked, retry after 100 ticks (5 s) and show an action-bar message to players within 8 blocks [entity survey B11.3, S1].
- Hatch beat: the baby appears with shell particles, a crack sound and a hatch sound [entity survey B11.4, S1].
- Hatch success (D41, answered) [lead 2026-10-12]: a lab egg uses `SpeciesData.lab_hatch_success`, the lab rate of its species. It is separate from the nest rate `SpeciesData.hatch_success`, which F2.5 uses (D79 values: T. rex 0.06). Both are config values, and playtests tune them. Start values (D147 answered): 0.95 for small species (Velociraptor, Dilophosaurus, Gallimimus), 0.75 for large species (T. rex, Triceratops, Brachiosaurus). A failure leaves a broken egg item and remains.
- The hatch rolls the per-clone variation and the risk behaviour (F1.5) at the same moment and stores both on the animal [recon RC21]. The behaviour effects land with F2.4 to F2.6.
- A hatch also adds one to the clone count of the drive that made the genome (D116). The count rises the next time that drive is in a Sequencer (D140 answered).
- A hatchling gets no owner. It stays wild until a player tames it (D17 answered) [entity survey O3].
- The hatched animal is a baby. Today `IncubatedEggItem` spawns an adult and never calls `setBaby`. M1 sets the vanilla baby state. The growth fraction 0 lands with F2.1.

**Out of scope:** natural nests that speed a hatch (F2.5 deferred items). Owner on a hatch without sneaking (D17) [JC3 B11.12]. False eggs [s11.13].

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `incubation.hatch_seconds` | 60 | D123 |
| `incubation.crack_stages` | 3 stages of 20 s | D81 |
| `incubation.egg_wobble_interval` | 30 to 90 s | D81 |
| `incubation.egg_wobble_degrees` | 4 | D81 |
| `SpeciesData.lab_hatch_success` | 0.95 small species, 0.75 large species | D41, D147 answered |

**Produces:** the egg model registry per species, the placed-egg entity, `EggVisual` (model, texture, wobble) for F1.7, F1.8 and F2.5.

**Recipe rows:** none. Eggs are machine outputs.

**Art:** see Art list → Eggs and hatching (egg models, natural and printed textures, item sprites, crack stages, hatch particle, crack and hatch sounds). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- Each species shows its egg model with the printed texture in the printer and the Incubator, and the natural texture in a nest (F2.5).
- Two clients see the same egg wobble at the same tick in one Incubator. The egg in the claw does not wobble.
- A placed egg starts to shake at once and hatches 60 s after placing.
- A dinosaur walks into a placed egg and pushes it.
- A placed egg hatches a baby whose genome matches the edit.
- Two eggs printed from one genome hatch animals with different rolled values.
- The egg tooltip shows the failure chance and no rolled trait.
- An egg in a space too small for the baby does not hatch. It shows the message and hatches after the space clears [entity survey B11.3].
- An egg hatches on chunk load if its time passed while unloaded [entity survey S1].
- Unit tests on the hatch roll and the failure roll. A lab egg uses the lab rate, and a nest egg uses the card rate (D41).
- A high-risk genome hatches animals with a risk behaviour at the configured chance. The Debug Stick names it [recon RC21].
- A player breaks a placed egg and places it again. The hatched animal shows the edits [JC3 B11.15].

### F1.10: Shared machine framework

**Old module:** B12 (only the code that every machine shares. Each machine's parts, upgrade looks and acceptance moved to F1.3, F1.4, F1.6, F1.7 and F1.8) · **Section:** 11.8, 7.1, 7.4 · **Size:** L · **Depends on:** M0. It lands first inside M1.

**In game:** no machine of its own. Every lab machine uses this code: the player builds it from parts, opens one screen from any part, switches between linked machines with tabs and stays in the screen [spec "Additional Things", s11.8]. A machine with a problem says why, puffs smoke and goes dark. A broken machine drops every item. A player who looks at a machine sees its state near the crosshair. Each machine package fills in its own parts, looks, faults, sounds and art.

**Touches:** `J/block/api/multiblock/*`, `J/block/api/MultiEntityBlock`, a new `MachineBlockEntity` base, `MachinePartItem`, the shared chip and tank upgrade items, the `nublar-gui` tab widget, the machine screen base, a machine sound helper, the hover-box overlay, the machine command.

**In scope, multiblock master and part placement:**

- Part-by-part assembly with right-click. Each machine package gives its part list and order. The framework checks the order, places the part and grows the model. Only placed parts show. A part shows as a bone of the current model. Where a part's cubes share a bone today (Processor, Egg Printer), the machine package splits them into bones first, with no art change.
- One master block entity per machine. Use on any part opens the master's screen. Breaking any part drops the master's slots once, and the other parts become part items. A part whose master is missing returns to its unassembled state [entity survey B12.3, S2].
- Parts have no block entity. A part index in the block state gives a fixed offset to the master. Capabilities register on the block and return the master's handler. Parts cost no ticks and no saves [tech survey B12.14].
- A creative-only item places a fully assembled machine. Each machine package registers its own.
- Part names follow the video (Base, Needle, Platform, Body on the Egg Printer), not the design doc names (D114) [lead 2026-10-12].
- Parts of a broken machine can build another machine (GH#12).
- The shape follows the placed parts. Breaking one machine never breaks a neighbour. Fewer break particles than today (GH#42).
- Fix the static shape map. A public static `HashMap` filled by `computeIfAbsent` in `getShape` can throw or hang (`MultiEntityBlock.java:35,83`) [tech survey d3].

**In scope, shared upgrade items and the upgrade hook:**

- Chips (iron, gold, diamond) and tank upgrades (iron, gold, diamond, netherite) are shared items. The player installs them by right-click [video 1:36-2:15]. Each machine package sets what a tier does on its machine and how it looks (D85). The 3 chips fit the Processor, the Sequencer and the Egg Printer (D113) [lead 2026-10-12].
- Replacing an upgrade gives the old one back to the player (D136) [lead 2026-10-12]. Example: a gold chip over an iron chip returns the iron chip.
- The upgrade hook changes the model through GeckoLib bone visibility, the block state or a texture swap. It uses bones of the current models only, with no new part (D85, answered). The Processor tanks and the Sequencer chips use parts. Every other upgrade swaps the texture of an existing bone. Each machine package fills it.
- At most 3 upgrades per machine, as the design doc says (D117) [lead 2026-10-12]. No machine has more than 3 upgrade slots today, so the cap limits nothing. No upgrade blocks another. This replaces "no limit on the number of upgrades" [s11.8].
- Run-cost rule (D141 answered) [lead 2026-10-13]: only chips and bulbs cost extra power. A faster tier draws base × speed × run-cost factor, rounded to whole FE per tick. Speed = base time ÷ tier time. The factor is 1.05 (iron chip, warm bulb, SSD), 1.10 (gold, warmer) or 1.15 (diamond, hot). So each run costs 5, 10 or 15 % more energy. Filters, the Leveling Sensor, containers and tanks cost no extra power. The chip times of the spec stay the defaults. This replaces the tech survey curve [tech survey B12.12].

**In scope, power hook:**

- `MachineBlockEntity.powerDraw()` returns the draw for the current tier. The machine extracts it each working tick from its energy storage. No power, no progress. An idle machine draws zero [s11.8]. Each machine package sets its own draw values. F1.11 supplies the generator and the network.

**In scope, tab host and screen base:**

- Cross-machine tabs: a screen shows a tab for every machine on the same conduit or generator network (F1.11). A tab switch keeps the screen open (D136) [spec, s11.8, lead 2026-10-12]. Each machine package supplies its tab icon in an active and an inactive look.
- Tab position: proposal, the lead decides. A row of tabs along the top-left outside edge, in chain order, like vanilla creative tabs.
- The green status bar on the left of every machine screen [video 1:07, 2:53, 11:38, 12:50]. Its meaning is not stated. Proposal, the lead decides: stored energy, with the red tick showing "running".
- Every machine screen darkens the world behind it, as vanilla screens do (GH#22), and survives a window resize (GH#24).
- Shift-click works in every machine screen (GH#18, GH#19, GH#21). A removed tube leaves the screen (GH#39).
- One layout record per machine drives the menu, the screen, the item handler and any recipe viewer [JC3 J12]. The record holds a name, a screen size (default 176 × 166), a slot list and the player inventory position. A slot entry holds index, x, y, role, accept test, outside-insert and outside-extract flags and a side mask. Roles: input, output, fluid, storage, environment, result (output and result are extract only). At construction, entry i must have index i. A saved inventory with another slot count resizes and logs one warning. A debug command prints a layout.

**In scope, fault mask and feedback:**

- Machine issues: the screen shows a warning tab with the reason, for example "No power" or "No plant matter" [tech survey B12.8, T7]. One `int` issue mask with NO_POWER, NO_INPUT, OUTPUT_FULL, WRONG_FILTER, NO_PLANT_MATTER, NO_EGG and SPECIES_MISSING. No NO_BULB: the Incubator lamp is the base bulb (D131). Compute it only on a slot change, a power event or a tier change. Send it in one `ContainerData` slot while a menu is open. Each machine package lists the bits that it raises.
- Fault effect: a machine that stops for power puffs smoke (3 puffs over 20 ticks) and shows its dark model. It puffs once when it starts again [tech survey B12.9, T7]. Each machine package supplies its dark model.
- Hover box [tech survey B12.10, T6]: a player who looks at a machine, generator or feeder for 5 ticks sees state, progress and fault ("Working 42 %"). It reads synced fields only. A network summary for generators comes from a 20-tick server cache. The box is always on. A client switch hides it and sets its offset (D27 answered).
- Machine sound helper [lead 2026-10-12]: each machine registers its own sound events per state (start, working loop, finished, power-down, plus its own extras). The server plays one-shot events on state edges. The client plays the loop while the synced ACTIVE state is on. No sound packet per tick. Each machine package lists its events.

**In scope, sync rules:**

- Send progress through `ContainerData` while a menu is open. Send block updates only on a slot or state change. ACTIVE flips only on the working edge. Write save data and sync data apart. Coalesce world-visible changes with a short delay. The active look stops 60 ticks after work stops. Today each active machine sends about 20 full-NBT packets per second [entity survey B12.1, tech survey B12.11].
- Shared animation state (F1.4 door, F1.7 print, F1.8 arm and lid) syncs a start game time and flags, never a per-tick angle. The client computes the pose from game time plus the partial tick.
- Cache the recipe lookup per input item. Clear it on `/reload` [entity survey B12.2].
- No comparator output on machines and feeders (D27, D28 answered) [lead 2026-10-12]. The control room in Beta is the fence switch only.
- An empty machine costs a few slot reads per tick. With no input, the tick returns before the recipe check [JC3 B12.25].
- A recipe time is at most 32,767 ticks (about 27 min), because `ContainerData` sends 16-bit values. The recipe schema states the cap [JC3 B12.26].

**In scope, rendering:**

- The renderer builds its hidden-bone set on a state change and uses the normal frustum test [entity survey B12.5].
- The master has one render box over the whole machine, so a tall machine stays visible at the screen edge [tech survey B12.15, rank 4].
- An idle lab draws like stone. Idle machines show a static block model from the idle-pose export. The GeckoLib renderer draws only while active or animating. A block state `ANIMATED` flips only on the work edges [tech survey B12.13, T5]. Today every machine uses `ENTITYBLOCK_ANIMATED` [tech survey d14]. Each machine package supplies its idle-pose model.
- Far machines cost no draw. The GeckoLib machine renderer has a view distance of 48 blocks (start value) [JC3 B12.32].

**In scope, automation and test tools:**

- Pipes and cables of other mods work with no compat code. Each machine shows items, energy and fluids through the standard capabilities with per-side rules. Machines accept outside energy (D24 answered) [tech survey rank 11, O10].
- A tester runs a full chain in seconds [JC3 J13]: `/projectnublar machine turbo on|off` (operator level 2) makes every job finish in 20 ticks, kept in memory only. `/projectnublar machine rig <machine>` fills a machine with test input. `/projectnublar machine kit` gives a test kit.
- A GameTest-only test machine with two parts proves the framework before any machine package uses it.

**Recipe rows:** iron, gold and diamond computer chips. Iron, gold, diamond and netherite tank upgrades. This package adds the rows to the recipe provider when the recipe table gives their ingredients (D78).

**Out of scope:** factory lanes, a six-face side screen, upgrade cards, a kept-contents cache after a break, a walled multi-block, a power meter block (D27) [tech survey B12.22 to B12.24, O13]. One recipe type with a machine id for all machines. The task plan picks the recipe shape [JC3 B12.29]. A list of upgrades that block each other (D117). Each machine's own parts, looks, faults, sounds and art (F1.3, F1.4, F1.6, F1.7, F1.8).

**Produces:** a `MachineBlockEntity` base with `powerDraw()`, `isIdle()`, `linkedMachines()`, the issue mask, the upgrade hook and the state sound hooks. `MachinePartItem`. The shared chip and tank upgrade items. The hover-box info interface (machines and generators here, feeders in F3.3, dinosaurs in F2.0 [lead 2026-10-12]). The layout record. The tab host.

**Art:** see Art list → Lab items (chip and tank upgrade sprites), Screens (GUI) (tab frame, warning tab icon). Not done until those rows are Done (no Placeholder, no To check). Each machine's own art (part sprites, dark model, idle-pose model, upgrade looks, tab icon) belongs to its machine package.

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `power.run_cost_factor` | 1.05, 1.10, 1.15 (tiers 1, 2, 3) | D141 |
| `power.machine_base_draw_fe_per_tick` | 20 | F1.11 power numbers |
| client `hover_box.enabled` | true | D27 answered |
| client `hover_box.offset` | set in the task plan | D27 answered |
| machine renderer view distance | 48 blocks (start value) | [JC3 B12.32] |

**Acceptance:**

- GameTest with the two-part test machine on both loaders: parts place only in order, use on any part opens the master's screen, breaking any part drops the master's slots once and the other parts as items, and a part with no master returns to its unassembled state.
- A hopper or a third-party pipe on any part face reaches the master's inventory [tech survey B12.14].
- Tabs switch between two test machines on one conduit network and keep the screen open. A machine on another network shows no tab (D136).
- A gold chip over an iron chip gives the iron chip back (D136).
- A fault bit set on the test machine shows its reason in the warning tab. A power loss puffs smoke and shows the dark model [tech survey T7].
- The test machine draws power only while it works.
- An idle machine sends no block-entity packets and draws through the static model [tech survey B12.11, T5].
- A machine with empty input slots runs no recipe lookup (a GameTest counts them) [JC3 B12.25].
- Unit test: a recipe time above 32,767 ticks fails to load with one log line [JC3 B12.26].
- Unit test: in each layout record, entry i has index i, and the menu slot count equals the record [JC3 J12].
- With turbo on, `machine_chain_smoke` finishes each job in 20 ticks [JC3 J13].
- A machine plays a start event on the work edge and stops its loop on the stop edge. No sound packet goes out while state is steady.
- `machine_chain_smoke` still passes on both loaders after the four existing machines move onto the framework.

### F1.11: Lab power

**Old module:** B14 (Basic Generator, conduit, power network) · **Section:** 11.11, 4.5, 7.7 · **Size:** L · **Depends on:** M0, F1.10

**In game:** a Basic Generator burns furnace fuel. Its flame glows, smoke rises, and it gives light 13 while it burns. Power Conduits, thin cable blocks, carry the power to the machines. One Basic Generator runs all four machines at base tier and a starter fence. Other mods' cables connect. Each machine package sets its own draw inside the budget that this package writes.

**Touches:** `J/block/entity/GeneratorBlockEntity`, new `J/block/ConduitBlock`, new `PowerNetwork` and its level manager, generator model and screen, sound events.

**In scope:**

- Remove the simulate flag. Machines extract their draw each tick through the F1.10 power hook.
- The generator saves its energy.
- Basic Generator: burns any furnace fuel at furnace rate (coal 80 s), also with no load [s11.11]. Output is `power.basic_output_fe_per_tick`, 200 FE per tick. Its buffer holds 20,000 FE, 5 s of full output [lead 2026-10-13]. The Creative Generator stays.
- One constant "energy per fuel tick" sets generator output and burn time [tech survey B14.10].
- Generator screen: a fuel slot, a burning flame and an energy bar. The video and the spec give no layout.
- Power Conduit block, the mod's own thin cable block (D119) [lead 2026-10-12]. Its network also sets which machines show as tabs (F1.10). A network has no capacity limit. It carries all that its generators make (D243) [lead 2026-10-13].
- One power network object per connected group [tech survey B14.6 to B14.9, T1, rank 2]:
  - A placed member joins the network next to it. With several networks next to it, the largest adopts the others.
  - A removed member marks its network invalid. The next tick, a flood fill builds the new networks. Several changes in one tick make one rebuild.
  - A chunk full of members joins its networks in one batch per tick.
  - Each member block entity saves its own energy. Nothing saves the network.
  - Each tick, a level-wide manager visits each network once. A network with no change skips its sums.
  - A change of the `powered` flag raises a version counter and sends one event to the network's members.
  - A heartbeat every 60 ticks, offset by the network id, checks one random member against the world.
  - A flicker guard holds a network unpowered for 100 ticks after 4 flips in 100 ticks.
  - Store per-level networks in level attachments or `SavedData`. Never cache a `Level` in a static field [tech survey OM5, OM6].
  - Fence posts and pylons join the same network in F3.2.
  - Overload (D242) [lead 2026-10-13]: when the demand of a network is more than its supply, every generator on it trips. A tripped generator sends no energy. The hover box shows "Overload". A generator starts again when the player removes load or adds a generator, so that supply covers demand. Lead: "the player has to build another generator".
- Conduits run no tick [tech survey OS1].
- The generator sends block updates only on change. Today it updates every tick, also when idle [entity survey B14.1].
- Save data and sync data apart [tech survey rank 6].
- No comparator output and no power meter block (D27 answered) [lead 2026-10-12]. The hover box shows the generator state.
- Other mods' cables connect through Balm on each loader. On NeoForge, Balm bridges to `EnergyHandler` [tech survey facts 3, 4]. On Fabric, only with Team Reborn Energy installed [tech survey fact 5]. Generators give energy to outside cables. Machines accept it. Conduits, posts and pylons do not (D24 answered). No export cap: an outside cable can take the full output (D243) [lead 2026-10-13].
- Balance (D119 answered) [lead 2026-10-13]. One Basic Generator (200 FE per tick) runs the four base-tier machines (80) and 10 low-security posts (100). 20 FE per tick stay spare. Each post drains 10 FE per tick and holds 350 FE [F30]. A second paddock of 10 posts with the lab at work needs 280 FE per tick, so the generator trips. Machine draw per tier: F1.10 run-cost rule and the spec F1.11 table. Today the generator makes 0.2 FE per tick and one post drains 10 [entity survey fact 47]. F3.2 checks the fence share.
- Grid memory (D21 answered) [lead 2026-10-12]: while a generator's chunk is not loaded, its fence keeps its last power state. Each post saves its powered flag. When the chunk loads, the generator burns fuel for the game time that passed. A developer decides whether this catch-up burn makes energy [lead 2026-10-13]. No chunk tickets and no forced chunk loading.
- Fuel ends (D245) [lead 2026-10-13]: the generator runs on its buffer for about 5 s. Then the machines stop with "No power", and the fence lamps fade post by post over about 2 s (F3.2).
- Generator sounds: ignite, running loop and out of fuel. Each event has its own file. The generator gets sounds of its own, not the generic lab sounds (D129) [lead 2026-10-12].
- Generator look (D119) [lead 2026-10-12]: while it burns, the flame glows and a smoke puff rises every 2 s. It gives light 13 while it burns, like a lit furnace. The original had no generator animation.

**Recipe rows:** Basic Generator (id `basic_generator`, renamed from `coal_generator`). Power Conduit (16 per craft). This package adds the rows from the approved recipe table (D78).

**Out of scope:** rotational power, wire loss, path search, transformer limits, exploding generators, a battery block, a power meter block (D27) [tech survey B14.13, B14.14, B14.16 to B14.19]. Comparator output (D27). The Large Generator and pylons (F3.2). A generator thunderstorm failure (an archived 2020 idea). Machine draw values (each machine package).

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `power.basic_output_fe_per_tick` | 200 | D119, balance arithmetic |
| `power.basic_buffer_fe` | 20,000 (5 s of output) | D245 |
| `power.overload_trips` | true | D242 |

**Produces:** `GeneratorBlockEntity` (basic, creative), `ConduitBlock`, the `PowerNetwork` object and manager with the `powered` flag and its version counter [tech survey T1], the per-machine draw budget.

**Art:** see Art list → Power (Basic Generator model and texture, conduit block, item sprites, generator screen, sounds). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- GameTest on both loaders: one Basic Generator with coal runs the four base-tier machines and 10 posts (180 FE per tick). A second paddock of 10 posts with the lab at work trips it, and the hover box shows "Overload" (D242).
- Out of fuel: the machines keep running for about 5 s on the buffer, then stop with "No power" (D245).
- Energy survives a world reload.
- A third-party cable powers a machine.
- A generator gives no comparator signal (D27).
- A generator chunk unloads for 10 in-game minutes with 5 minutes of fuel left. Its fence stays powered meanwhile. After the load, the fuel is gone (D21).
- 60 idle conduits run no tick method [tech survey OS1].
- A GameTest places 20 conduits in one tick. The manager counts one rebuild.
- Each generator state plays its own sound event.
- A burning generator shows its flame glow and smoke and gives light 13. An empty generator shows neither and gives no light.
- Each recipe row crafts in survival.

### F1.12: Lab recipes and advancements

**Old module:** B13 (lab items) · **Section:** 11.7, 11.8, 8.1 · **Size:** S · **Depends on:** F1.1 to F1.11, the D78 recipe review

**In game:** the player crafts every lab block, part, upgrade and tool in survival from vanilla materials. No lab item comes only from loot or trades. Advancements mark each step of the chain.

**Touches:** datagen `ModRecipeProvider` (shared rules only), a new advancement provider, creative tabs in `J/registry/CreativeTabInit`.

**In scope:**

- The recipe table (D78 answered) [lead 2026-10-13]: the lead approved Claude's draft with changes. "Recipes and progression gates" holds every grid. Rules:
  - The four machines alone cost about 50 iron value, 4 gold ingots and 14 quartz slabs [lead 2026-10-13, D256]. Each machine has one gold ingot in its precision part. With the Basic Generator, the Hard Drive, the Iron Filter and a syringe, the first setup costs about 66.
  - No lab part uses a stone material. The parts are machines and computers smaller than a player. The quartz slabs make the first lab need a Nether trip (D256).
  - The player crafts each upgrade fresh. A higher tier never uses up the lower tier. Only the Netherite Tank Upgrade takes a Diamond Tank Upgrade.
  - Copper for wiring. Blaze powder in the Hot Bulb. Nether quartz in the quartz-slab parts, the SSD and the Leveling Sensor. Netherite only in the Sequencer tank.
  - No recipe uses special pieces. Beta has no emerald tank, no netherite Processor tank and no Ender SSD.
- Recipe book: the lab part recipes unlock when the player picks up the first fossil or amber [lead 2026-10-13].
- Id renames (D251) [lead 2026-10-13]: `coal_generator` to `basic_generator`, `iron_plant_tank_ugprade` to `iron_plant_tank_upgrade`, `carnivore_feeder_one` and `carnivore_feeder_two` to `small_carnivore_feeder` and `large_carnivore_feeder`. No world-compat shims (global constraint).
- New `*_base` part items start each machine in survival. The whole-machine items `processor`, `sequencer`, `egg_printer` and `incubator` become creative only.
- Each work package adds its own rows, as its "Recipe rows" line says. This package checks that every lab row shipped.
- Special pieces serve DNA and trades only. No recipe uses them (D78 answered). No item is trade-only or loot-only [s11.8].
- Each "Not crafted" item gets a creative-only mark or a source note.
- Advancements (D137, D148) [lead 2026-10-12]. For the chain: first fossil, first amber, first tube, first species at 50 % and first edit. Then first embryo, first egg, first hatch and first fully upgraded machine.
- Creative tabs ordered by chain stage.

**Out of scope:** park items (F3.4), lysine (F2.7), feeders (F3.3), vehicles (F4.2).

**Produces:** the advancements, the creative tab order, the recipe coverage test.

**Art:** see Art list → Lab items (advancement icons use the item sprites). Not done until those rows are Done (no Placeholder, no To check).

**Config (defaults):** none.

**Acceptance:**

- A survival playthrough from a fresh world reaches a hatched animal with no creative mode and no trades. A reviewer does it once per loader and records the time.
- Unit test: every registered lab item in the creative tabs has a recipe or a creative-only mark.
- A new player who picks up a first fossil sees the lab part recipes in the recipe book.
- Each advancement fires at its step of the chain.

---

## M2. Dinosaurs

**Goal:** six species live, grow, eat, drink, rest, socialise and breed. Each has its own models, skins, animations, sounds and effects.

**Exit gate (playtest):**

- One paddock per species, on each loader. Each animal grows from hatchling to adult (accelerated time), eats, drinks, sleeps and keeps its stress down.
- Every clip in the species' list (F2.3) plays in a fitting state. Every sound is the species' own. Every Dino VFX item shows.
- Every Beta skin set renders for both genders.
- A Gallimimus herd travels. A Velociraptor pack hunts. A calm pair lays a clutch. A dinosaur gives a threat display before it attacks a player.
- 20 Gallimimus on screen reach at least 120 fps. 20 animals in one paddock keep the server tick under 50 ms.

**Herds first** [lead 2026-10-10]. Herds must move and behave properly before hunger, thirst, attack and flee join herd play. Test on the Gallimimus only. F2.5 part A comes before F2.4.

**Order inside M2:** F2.1 part A, F2.5 part A, F2.0 part A, F2.2, F2.1 part B, F2.3, F2.4, F2.5 part B, F2.7, F2.6, F2.0 part B. F2.0 is listed first below because it sets the behaviour model that the other M2 packages fill.

### F2.0: Dinosaur behaviour model

**Old module:** none. New package, from the lead's question of 2026-10-12 ("where are the AI/Behaviors for the dinosaurs?"). It takes scope from B17 (F2.4), B24 and B09 (F2.3), B19 (F2.6) and B18 (F2.5, feeding order). See "Moved into F2.0" · **Section:** 11.6, 11.9, 11.10, 11.12, 11.16, 7.3 · **Size:** L (part A) + M (part B) + M (lead additions of 2026-10-12: feeding together, contentment, behaviour genes) · **Depends on:** part A: F2.1 part A, F2.5 part A, M0.B00 (species data). Feeding together: F2.4 (feeder registry, grazing search), F3.3 (feeder edges). Behaviour genes: F1.5 (`GenomeEffects`). Part B: F2.4, F2.5 part B, F2.6, F3.3.

**Specs:** feature spec F2.0 "Dinosaur behaviour", with its sections "Feeding together", "Contentment" and "How genes change behaviour". Primary sources first: the lead decisions of 2026-10-12, section 11.6, 11.9, 11.10, 11.12, 11.16 [s11], the design doc (sections "Basic Genes", "Stress: Basic comfort", "Social", "Group Size", "Domesticity", "Intelligence", "Aggression", "Diurnal/Nocturnal/Cathemeral", "Hunger/Stomach Capacity", "Eat Rate", "Energy", "Thirst", "Special Diet Genes", "Byte Values", "Dino vs Player"), Special Genes, Vanilla Animal DNA Effects [DNA Effects], Mod Commission ("Dinosaur behavior", "Needs", "Genes/Stats"), the species cards ("AI", "Idles", "Group Feeding"), the 2024 board AI card, the 2020 board creature and feeder cards, the herd decisions 1 to 33. The reconciliation, the entity survey, the JC3 survey and the AI comparison report are implementation help.

**Order inside M2** (proposal, lead decides): F2.1 part A, F2.5 part A, **F2.0 part A**, F2.2, F2.1 part B, F2.3, F2.4, F2.5 part B, F2.7, F2.6, **F2.0 part B**. Part A gives the other packages one brain skeleton to fill. Part B proves the whole model with the healthy-state test. The herds-first rule holds [lead 2026-10-10]: every new behaviour is tested on the Gallimimus first.

**In game:** each dinosaur does one thing at a time and the most urgent thing wins. A fed, watered, grouped animal has a daily rhythm: it grazes or patrols by day, sleeps in its window, calls to its mates, and fills spare time with its own idle acts. Animals share food: a herd gathers at one feeder or one tree, each animal takes its own feeding spot or its own block, and the others wait their turn. A content herbivore grazes when idle, gains a little hunger and leaves the block in place. A keeper sees state in the body and voice: content animals sit, sniff and call softly. Hungry, thirsty, lonely or stressed animals pace, call more and sit less. Every player who looks at a dinosaur sees its contentment band and its worst need. Genes change all of this: a wolf-DNA raptor needs a bigger pack and tames sooner. Neglect ends in aggression, health loss and death, never in a fence attack.

**Touches:** `J/entity/dinosaur/Dinosaur` (brain activity registration), `J/entity/dinosaur/animation/AnimationChoice`, `DinoOneShots`, `OneShotAction`, `StartleWatch`, `J/entity/ai/tasks/Rest`, `J/entity/ai/behaviour/social/JourneyPolicy`, `GrazeSchedule`, `J/item/DebugStick`, `J/util/DinoNeedsUtils` (band reads only), `SpeciesData` (M0.B00), the F2.4 feeder registry and grazing search, the F1.10 hover-box overlay, `J/api/gene/GenomeEffects` (reads only), new `J/entity/dinosaur/behaviour/` package (`NeedBands`, `IdlePool`, `AmbientCalls`, `HealthyState`, `FeedingSpots`, `FoodClaims`, `Contentment`, `BehaviourGenes`), species data files, `docs/config-reference.md`.

**Moved into F2.0** (each line leaves its old package. The old package keeps a one-line pointer here):

| Item | From | What stays in the old package |
| --- | --- | --- |
| Brain activities with priorities: CORE plus AVOID, FIGHT, HUNT, FEED, REST, HERD_TRAVEL, BREED, SOCIAL, IDLE, and "GameTests cover each switch" [recon RC28] | F2.4 "Brain activities" bullet | F2.4 fills FEED and REST. F2.5 fills HERD_TRAVEL, BREED, SOCIAL. F2.6 fills AVOID and the FIGHT warning |
| IDLE weighted pool of the species' own idle clips by `idle_weights`. A herbivore looks at a still player within 8 blocks [recon RC29] | F2.4 "IDLE plays a weighted pool" bullet | Nothing |
| "The stress band changes idle weights: a stressed animal sits less and calls more" [recon RC17] | F2.3 clips list | F2.3 keeps the clips themselves |
| "Idle acts (sniff, look, head shake, scratch, curious, ground peck, speak) join the F2.4 idle pool by `idle_weights`" | F2.3 limp bullet | F2.3 keeps limp and D92 |
| Sleep windows by `activity_window`: the per-species values and the cathemeral window | F2.4 "Sleep windows" bullet | F2.4 keeps the sleep mechanics: drowsy and rouse rolls, wake lock, sleep cooldown, threat box, no sleep while starving |
| Four temperaments table mapped to `flee_or_stand` (D13), diet and the aggression gene [JC3 J8, B19.16] | F2.6 "Four temperaments" bullet | F2.6 keeps flee, warning, detection and taming |
| Acceptance "When two activities can start, the higher priority runs" [recon RC28] | F2.4 acceptance | Nothing |
| "Feeding order at a carcass by dominance: the leader first, at most 3 at once, others wait within 6 blocks with idle acts (D57)", and its acceptance line | F2.5 part B | A pointer. F2.5 supplies dominance. The fixed 3 is gone: the spot model sets the count [lead 2026-10-12] |

Not moved (F2.0 links to them and does not repeat them): need decay, stress meter arithmetic, cooldowns, starvation, stamina chases, grazing and water search (F2.4). Herd travel, packs, leaders, breeding, nests (F2.5). Warning displays, flee, detection, personal space, taming (F2.6). Clips, sounds and effects themselves (F2.3).

**In scope, part A: the behaviour model**

- [ ] **Activity ladder.** Register the SmartBrainLib 2 activities in this priority order: CORE (always), AVOID, FIGHT, HUNT, FEED, REST, HERD_TRAVEL, BREED, SOCIAL, IDLE. Each activity has a start condition and a stop condition built on memories, no per-tick polling. Owner packages add their behaviours to the activity. A missing behaviour leaves the activity empty and the next rung runs. Map of the 2024 board AI card to activities:

  | AI card item | Activity | Beta | Owner of its behaviours |
  | --- | --- | --- | --- |
  | Hunting: Solo, Packs. Scavenging (cards) | HUNT, FEED (carcass) | In [s11.10] | F2.4, F2.5 part B |
  | Foraging, Eating, Drinking | FEED | In [s11.10] | F2.4 |
  | Herding: Herds, Flocking, Packs | HERD_TRAVEL, CORE (spacing, joining) | In [s11.10, herd d1-d33]. Flying flocks out | F2.5 part A |
  | Herding: Dominance/Group Leaders, Communication. Call (cards) | SOCIAL | In. Turf wars out [s11.10, s11.13] | F2.5 part B, F2.0 (ambient calls) |
  | Group Feeding (cards) | FEED | In for every species (D57 answered) | F2.5 part B, F2.0 |
  | Idles | IDLE | In [2020 board, cards] | F2.0 |
  | Sleeping, Resting | REST | In [s11.10] | F2.4, F2.0 (windows) |
  | Combat: Attacking, Defense, Threatening, Aggression | FIGHT | In [s11.10] | F2.6, F2.5 part B (guard young) |
  | Combat: Fleeing | AVOID | In [s11.10] | F2.6 |
  | Taming | CORE and IDLE start conditions | In [s11.10] | F2.6 |
  | Neutral | Temperament mapping | In [design doc "Stress: Basic comfort"] | F2.0 |
  | Movement: Walking, Running, Sprinting, Swimming, Jumping | CORE | In [s11.9, s11.10] | F2.1, F2.3, F2.5 part A |
  | Movement: Fully aquatic swimming, Flight | none | Out [s11.9] | none |
  | Socializing. Play (cards) | SOCIAL, IDLE | Socialising in. Loneliness is a stress cause, not a need of its own (D52). Play in: content young chase each other with existing clips, no damage (D107) | F2.0 |
  | Breeding, Mating, Nesting (MC, cards) | BREED | In [s11.10] | F2.5 part B |
  | Baby/Infant | CORE (follow parent) | In [s11.9] | F2.1 part A |

- [ ] **Rules across the ladder**, as start conditions, each with a GameTest. A follower on a journey leaves the line to eat or drink only at an urgent need (below 25 %, D36) [herd d8]. It rests on a roaming journey only at critical stamina, on an urgent trip when tired [herd d17]. REST does not start while hunger or thirst is urgent [JC3 J7, F2.4]. A tame "stay" blocks HERD_TRAVEL, SOCIAL and BREED only. The animal may walk to food and water, then it returns to its saved spot. It sleeps on its spot (D108 answered) [lead 2026-10-12]. A tame animal keeps its sleep window and calls. FOLLOW pauses while it sleeps (D108). AVOID and FIGHT ignore the REST schedule [comparison A2].
- [ ] **Need bands.** One pure function maps a need value to SATISFIED, WANTS, URGENT or HARMFUL from per-need lines in config. Hunger 0.75, 0.25, 0 [plan]. Thirst the same (D104 answered). Stamina 0.75, 0.33, 0 [herd d29, plan]. Company is not a need band of its own (D52 answered) [lead 2026-10-13]. Below `group_size_min` the "alone" stress cause runs. It counts as urgent after `needs.social_urgent_ticks` (72,000 ticks, D106). Young do not count toward the group size until they are adult (D246). A band change is an event: it writes or clears the F2.4 need memories once, never each tick. The band and the stress band reach the client only on change, inside the F2.1 debug-status bytes.
- [ ] **Stress bands.** CALM, UNEASY, STRESSED from the F2.4 meter. STRESSED at `stress_threshold` and above (default 50, one value per species, no gender split, D13 and D88 answered). UNEASY from `needs.stress_uneasy_margin` (20, D99 answered) below it. The F2.4 hysteresis applies to each edge.
- [ ] **Species behaviour profile in species data** (M0.B00 adds the record, F2.0 adds these keys, each with a `_basis` note):
  - `activity_window`: diurnal, nocturnal or cathemeral (D97 answered): Tyrannosaurus, Velociraptor and Dilophosaurus cathemeral. Triceratops, Brachiosaurus and Gallimimus diurnal. Today's config flags (three carnivores nocturnal) are code, not a source.
  - `sleep_windows`: two windows per cathemeral species, in ticks (D98 answered, config defaults, tuned in playtest). Tyrannosaurus 16,000 to 21,000 and 7,000 to 9,000. Velociraptor 20,000 to 23,000 and 6,000 to 8,500. Dilophosaurus 2,000 to 5,000 and 8,000 to 10,000.
  - `idle_pool`: the list of idle acts and clips the species has (table below), with weights.
  - `call_interval_ticks` (D101 answered). The lead set T. rex about 5 minutes, Velociraptor and Gallimimus about 1 minute, the others 2 to 3 minutes. Ticks: Tyrannosaurus 6,000, Velociraptor 1,200, Dilophosaurus 3,000, Triceratops 3,000, Brachiosaurus 3,000, Gallimimus 1,200. Babies use half the interval (D108).
  - `look_at_player`: true for herbivores, false for carnivores until F2.6 detection fires [recon RC29, comparison A4].
  - Temperament (moved from F2.6) [JC3 J8, B19.16]: computed from `flee_or_stand`, diet and the aggression gene. Gallimimus scared, Brachiosaurus neutral (it defends, D50 answered), Triceratops neutral, the three carnivores aggressive when hungry or by gene, else neutral. An aggression gene of +50 % or more makes a fed neutral species aggressive (D112). No new key unless the mapping needs one.

    | Temperament | Hunts | Fights back and alerts | Panics when hurt |
    | --- | --- | --- | --- |
    | aggressive | when hungry, or by the aggression gene | yes | no |
    | neutral | when hungry | yes | no |
    | passive | never | no | yes |
    | scared | never | no | yes |

- [ ] **Sleep windows.** REST switches by `SmartBrainSchedule.byDaytime` per `activity_window`: diurnal 16,000 to 24,000 [JC3 J7, plan], nocturnal 4,000 to 12,000 (D252). Cathemeral: the species' own `sleep_windows` (D98 answered). Each animal rolls a fixed offset of up to ±500 ticks at hatch and saves it. A herd or pack member uses its leader's offset, so the group sleeps together. A baby takes one extra nap each day and a longer night sleep (D108, values in the task plan). A diurnal herbivore that the nocturnal gene moves to cathemeral gets windows of its own, `shifted_sleep_windows` (D182 answered) [lead 2026-10-13]. Triceratops: 18,000 to 22,000 and 5,000 to 7,000. Brachiosaurus 19,000 to 22,000 and 9,000 to 10,000. Gallimimus 17,000 to 21,000 and 3,500 to 5,000. A shifted member keeps its own window and naps alone, also in a herd. The nocturnal window is 10:00 to 18:00, 4,000 to 12,000 (8 h, D252 answered) [lead 2026-10-13]. The F2.4 sleep behaviour reads the window from here.
- [ ] **Lost sleep** (D105 answered). A missed window caps the rest part of contentment at 50 until the next full sleep (reason "tired"). Stamina regenerates at half rate meanwhile. Lost sleep adds no stress. This reverses the M2 pass rule "each missed window adds one stress step" [lead 2026-10-12, follow-up pass].
- [ ] **Play** (D107 answered). Two or more content young of one species near each other may chase each other in IDLE with their run clip (Velociraptor: pounce). No damage. No new clip.
- [ ] **Idle pool per species.** IDLE runs one SBL `OneRandomBehaviour` over the species' `idle_pool`. Each act runs for its clip length or its duration, then the pool picks again. Rare acts have cooldowns. Acts with a missing clip drop out of the pool until the Art list row is Done. Never play another species' clip (lead 2026-10-12). Start weights (proposal, D101, basis [comparison "What an animal does when no need is urgent"], [reference mods]):

  | Act | Weight | Duration or cooldown | Notes |
  | --- | --- | --- | --- |
  | Stand with main idle and code sway | 6 | 40 to 120 ticks | herd d27 sway stays on |
  | One clip variant (each) | 1 | clip length | see species table |
  | Look at a mate, or a still player within 8 blocks | 2 | 45 to 90 ticks | head look, no clip |
  | Short wander | 2 | until arrival | groupless animals and leaders only. Herd members keep graze spots [herd d6] |
  | Sit briefly | 1 | 200 to 400 ticks, cooldown 3,600 | rest clip |
  | Greet a mate | 1 | clip length, cooldown 1,200 | speak clip, species that have it |
  | Sentry | leader only | clip length | herd d26 (keeps its 30 % per 200 to 400 ticks) |
  | Idle graze | 2 | clip length | herbivores only. A content or idle animal grazes a block in its reach, with a small hunger gain [lead 2026-10-12]. See "Idle grazing" |

  Idle clips per species (names from the 2020 board and the species cards. Status lives in the Art list):

  | Species | Idle clip variants in the pool |
  | --- | --- |
  | Tyrannosaurus | look left, look right, sniff air, sniff ground, shake head, shake body, scratch, speak, startled, yawn, sneeze |
  | Velociraptor | idle 2, look left, look right, look around, sniff air, sniff ground, shake head, shake body, scratch, speak, startled, sneeze, yawn |
  | Dilophosaurus | idle 2, curious, speak, look left, look right, sniff air, sniff ground, head shake, shake body, scratch, startled, call 2, yawn, sneeze |
  | Triceratops | idle 2, eat 2, drink 2, look left, look right, sniff air, sniff ground, head shake, shake body, scratch, startled, speak, yawn, sneeze |
  | Brachiosaurus | idle 2, look left, look right, low look left, low look right, sniff air, sniff ground, shake head, shake body, scratch, startled, speak, drink 2, yawn, sneeze |
  | Gallimimus | idle 2, ground peck, look left, look right, sniff air, sniff ground, head shake, shake body, scratch, speak, startled, yawn, sneeze |

- [ ] **Band shifts the pool.** Weights scale by the stress band (moved from F2.3): UNEASY sit × 0.5 and calls × 2, STRESSED sit × 0 and calls × 3 (D99 answered, config). A WANTS hunger band raises sniff acts × 2. A WANTS stamina band raises yawn × 2 (proposal, config).
- [ ] **Ambient calls.** One call scheduler per animal on game time, offset by entity id. At `call_interval_ticks` with a ± 50 % jitter, a calm animal plays its contact call clip and its own contact call sound (F2.3) and fires the F2.5 call game event. Social WANTS halves the interval. No ambient call while asleep, in AVOID or in FIGHT. A pack or herd staggers calls so that no two mates call on the same tick.
- [ ] **Debug Stick.** The F2.1 readout adds the stress band and the band of the most urgent need: for example "Velociraptor · FEED · thirsty (wants) · calm". It also shows the exact contentment score and each part (see "In scope, contentment"). Creative or operators only. Every player gets the hover box (D18 answered [lead 2026-10-12]).
- [ ] **Healthy-state predicate.** One pure function over the synced state. It returns true when every need sits in SATISFIED or WANTS (none URGENT or HARMFUL) and stress sits in CALM. It also needs company at or above `group_size_min`, no hurt in the last 2,400 ticks, and no threat memory. The D61 place memory (24,000 ticks) does not enter this test. The lead kept the 2,400 ticks for the safety part (D229) [lead 2026-10-13]. Used by the GameTests and shown as "healthy" on the Debug Stick. No gameplay reads it. The contentment score is a separate value with its own effects (D100 answered [lead 2026-10-12]).

**In scope, feeding together** [lead 2026-10-12] (spec F2.0 "Feeding together"):

- [ ] **Shared feeders, all kinds, all species.** A feeder never serves one animal only. `FeedingSpots` (pure) turns a source edge into spots. Each eater takes a stretch of open edge equal to its fit-box width [herd d5] plus `feeding.spot_gap` (0.5, D111 answered). The source takes eaters while free edge is left, so mixed sizes share one edge. An edge that faces a wall, a fence, deep water or a block the eater cannot stand on gives no spots. Each spot is a stand point from which the head touches the tray [s11.9, F2.4 head contact].
- [ ] **Spot claims on the feeder registry.** The F2.4 per-level feeder registry gains, per feeder: its open edges (F3.3 writes them on load and on a neighbour block change), a claim per spot (eater id, game time of expiry) and a wait queue (eater id, group id, dominance, join time). A claim lasts `feeding.claim_ticks`: it frees after 100 ticks (5 s) if the animal has not arrived (D111 answered) [lead 2026-10-13]. The claimer refreshes it every 20 ticks while it walks to the spot or eats. A claim ends when the eater is full, leaves, dies, unloads or stops the refresh. No per-tick scan: claims and the queue change only on these events.
- [ ] **Queue and turn order.** A hungry animal that finds every spot claimed joins the queue. It waits within `feeding.wait_radius` (6 blocks, D111 answered) outside the spots and their walk lanes, with idle acts. When a spot frees, the queue picks the next eater: within one group the higher dominance first (the leader first), then the longest wait. Between groups and species, the longest wait first (D111 answered). This is D57 for every species and every source [lead 2026-10-12]. An eater leaves its spot when full (hunger above the WANTS line). While others wait, it also leaves after `feeding.turn_limit_ticks` (600, D111 answered) and joins the queue again if it is still hungry. A queued animal that moves more than 8 blocks away, or is no longer hungry, leaves the queue.
- [ ] **No fights at a source.** A waiting animal never pushes an eater out and never targets a mate for food [s11.10, s11.13 turf wars out].
- [ ] **Natural food: block claims with expiry.** `FoodClaims` is one per-level map from block position to claimer id and expiry. The F2.4 grazing search skips a block that another animal claims. It also skips a block whose stand point lies inside a mate's spacing [herd d12, now extended to feeding targets, herd d24]. The first free block wins. The claim frees after `feeding.block_claim_ticks`, 200 ticks (10 s), if the animal has not arrived (D111 answered). The claimer refreshes it while it walks to the block or eats. Eating the block (sheep rule, `mobGriefing` on) clears the claim. Chunk unload and level unload clear their entries.
- [ ] **Idle grazing** [lead 2026-10-12]. A herbivore that is content or idle plays its graze behaviour (its eat clip) at a grass or plant block in its reach band. It finds the block with the F2.4 grazing search and claims it in `FoodClaims`, as a hungry eater does, so no two animals use one block. Each idle graze adds `needs.idle_graze_hunger` (proposal 0.01 of the hunger maximum, lead decides), up to the maximum. By default the block stays (`needs.idle_graze_removes_block` false), so a content herd does not strip its pasture. A hungry animal in FEED still eats and removes the block [s11.10]. Idle grazing is an IDLE act, so any higher rung stops it.
- [ ] **A herd gathers at one source.** Herd meals are hybrid, queue first (D111 answered) [lead 2026-10-13]. When a herd member finds a diet source in the herd area, it writes the source position into the group's food memory. The herd travels to that source together. Each member claims its own spot or block there, in its own reach band [s11.10 reach, F2.4 height band]. A Brachiosaurus takes the high leaves, a smaller animal the low plants. A member with no spot waits up to the 30 s turn limit. Then it may use other food inside the herd area. All regroup after the meal.
- [ ] **Omnivore.** The Gallimimus eats plants and small meat (dropped meat, eggs). It queues at herbivore and carnivore feeders alike [lead 2026-10-12].
- [ ] **Carcasses.** The F2.1 carcass uses the same spot model: its body box gives its edge. The carnivore rules stay: rank order, scavenging, and D62 for a starving animal. The fixed "at most 3" of the old D57 default goes [lead 2026-10-12].
- [ ] **Feeder animation.** A feeder opens when the first hungry animal claims a spot or joins its queue. It closes by the F3.3 rule when no claim and no queue is left.

**In scope, contentment** [lead 2026-10-12] (spec F2.0 "Contentment"):

- [ ] **`Contentment` (pure).** Inputs: the need values, stress, health, the group count near the animal, the threat and danger memories, and whether it slept in its last window. Output: seven parts (hunger, thirst, rest, company, stress, health, safety), each 0 to 100, the score, the band and up to two reason words. Formula, weights, cap (lowest part + 50) and bands as in the spec, all config (D110 answered). It reads state only, never the genome.
- [ ] **Update on change.** Recompute on a need band change, a stress band change, a health change of 5 % or more, a group size change, a threat or danger memory change, and the end of a sleep window. Never each tick. A band changes only past its line by 5 points.
- [ ] **Sync.** The band (2 bits) and the worst two reasons (one byte) join the F2.1 debug-status bytes. They go to every tracking client on change only. The exact score and parts go to a Debug Stick holder only.
- [ ] **Hover box for dinosaurs.** The F1.10 hover box gains a dinosaur provider. Any player who looks at a dinosaur for 5 ticks within `contentment.hover_range` (24 blocks, D110 answered) sees its hover box. The box shows icons only (D149 answered): the name, a coloured mood face for the band, and the icon of the worst need. Two need icons when both are below 70. No band words. Need icons: hunger, thirst, tired, lonely, stressed, hurt, threatened. Same client switch as machines (D27). The Debug Stick, creative mode and operators still get exact numbers.
- [ ] **Effects of its own** (D109 answered). Content: growth rate × 1.10 and natural regen × 1.25. Distressed: growth rate × 0.75 and natural regen × 0.5. Uneasy: none (D109 answered). Growth reads the factor through the F2.1 growth clock, so growth stays smooth. Each effect is a config value. A value of 1.0 switches it off.
- [ ] **Stress stays the gate.** Breeding (F2.5), taming (F2.6) and aggression (F2.4) read stress, never contentment [s11.10]. A GameTest checks this.

**In scope, behaviour gene readers** (spec F2.0 "How genes change behaviour"):

- [ ] **`BehaviourGenes`.** One reader turns the `GenomeEffects` values (F1.5) into behaviour parameters. Each owner package calls it. No behaviour code reads the raw genome. Map and owners:

  | Gene | Behaviour parameter | Owner | Basis |
  | --- | --- | --- | --- |
  | Aggression | Dominance input. Temperament line at +50 % (D112). Notice range and warning cooldown (D102) | F2.5 part B, F2.6, F2.0 | [s11.6, s11.10, design doc, DNA Effects] |
  | Intelligence | Search radius and retry factor 0.75 + 0.05 × I. Failed-target time × (1 + 0.05 × I). Detection + I points (all D112) | F2.4, F2.6 | [design doc "Intelligence", "Byte Values"] |
  | Herd size, pack size | `group_size_min` and `group_size_max` × (1 + gene %), rounded, at least 1 | F2.5 part A | [s11.6, s11.16, design doc, DNA Effects] |
  | Tamability | Trust threshold ÷ (1 + gene %), floor a quarter (D112) | F2.6 | [design doc "Domesticity", DNA Effects] |
  | Nocturnal | Window step at ±50 % (D49). Edge shift up to 2 hours below that (D112) | F2.0 sleep windows | [design doc, DNA Effects] |
  | Stomach capacity, eat rate, thirst, energy | Need maximum, drain, bite time, stamina maximum | F2.4 | [s11.6, design doc] |
  | Size | Dominance input, reach band, fit-box width (feeding spots, spacing) | F2.1, F2.4, F2.5, F2.0 | [s11.6, s11.9, s11.10] |
  | Speed, strength, defense | Chase speed, prey threat, retreat at a threat ratio of 2 (D112) | F2.4, F2.6 | [s11.10, design doc] |
  | Jump, underwater capacity, heat resistance | Step and jump height (cap D47), water path cost, fire path cost (D112) | F2.1, F2.5 part A | [s11.6, s11.10, DNA Effects] |
  | Special genes, diet genes, quirks | As F2.7 builds them, with the behaviour rows of the spec | F2.7 | [Special Genes, s11.6] |
  | Abnormal behaviour | One `risk_behaviours` entry at hatch (D44) | F1.5, F2.0 | [s11.6] |
  | Stress Tolerance (design doc name: Lower Threshold Stress) | Raises `stress_threshold` [lead 2026-10-12] | F2.4 | [design doc] |
  | Heredity | Midpoint plus variation. Choice genes from one parent. Quirks: 50 % per parent that has it [lead 2026-10-12]. Risk behaviour: from one parent, with a 50 % chance (D112) [lead 2026-10-13] | F2.5 part B | [s11.10] |

- [ ] Each mapping that has no primary value carries a `_basis` note that names D112 in the config reference.

**In scope, part B: proof**

- [ ] The healthy-state GameTest per species (acceptance below), run with a sped-up clock. Gallimimus first [lead 2026-10-10].
- [ ] Need-band transition GameTests per need (acceptance below).
- [ ] A playtest script for the lead: one paddock per species, watched for one in-game day, with the F2.0 per-species routine table as the checklist.
- [ ] The feeding-together, contentment and behaviour-gene GameTests (acceptance below). Gallimimus first [lead 2026-10-10].

**Out of scope:** contentment as a gate for breeding, taming or aggression (stress stays the gate [s11.10]). Contentment effects on disease (disease is not in Beta [s11.13]). Fights or pushing at a food source [s11.10, s11.13]. Fence attacks from needs or stress [s11.10, fence parity F27, F29]. Enclosure space and block comfort as stress inputs [s11.10]. Overcrowding stress (D106: a group above its maximum splits instead). Scent trails [lead 2026-10-12]. Look-around clips on the second controller (D14). Shelter search before sleep [F2.4 out of scope].

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `needs.band.hunger.wants`, `.urgent` | 0.75, 0.25 | [plan, entity survey B17.6] |
| `needs.band.thirst.wants`, `.urgent` | 0.75, 0.25 | D104 answered |
| `needs.band.stamina.wants`, `.urgent` | 0.75, 0.33 | [herd d29, plan] |
| `needs.social_urgent_ticks` | 72,000 (3 in-game days) | D106 answered |
| `needs.stress_uneasy_margin` | 20 | D99 answered |
| `behaviour.idle.weight.<act>` | 6, 1, 2, 2, 1, 1, 2 (idle graze) | proposal [comparison, reference mods] |
| `needs.idle_graze_hunger` | 0.01 of the hunger maximum per idle graze | proposal, lead decides [lead 2026-10-12: "marginally contribute to hunger"] |
| `needs.idle_graze_removes_block` | false | [lead 2026-10-12] |
| `behaviour.idle.sit_cooldown_ticks` | 3,600 | proposal [reference mods FAR] |
| `behaviour.idle.greet_cooldown_ticks` | 1,200 | proposal [reference mods MCA] |
| `behaviour.look_at_player_radius` | 8 | [recon RC29] |
| `behaviour.stress_idle_scale.uneasy`, `.stressed` | sit 0.5, call 2 and sit 0, call 3 | D99 answered |
| `behaviour.call_jitter` | 0.5 | proposal |
| Species data `sleep_windows` | per species, see In scope | D98 answered |
| `sleep.offset_max_ticks` | 500 | [lead 2026-10-12] |
| Species data `activity_window`, `call_interval_ticks`, `idle_pool`, `look_at_player` | see In scope | D97, D101 |
| `feeding.spot_gap` | 0.5 blocks | D111 answered |
| `feeding.claim_ticks` | 100 (5 s), refreshed every 20 | D111 answered |
| `feeding.turn_limit_ticks` | 600 (30 s), only while others wait | D111 answered |
| `feeding.wait_radius` | 6 blocks | D111 answered |
| `feeding.block_claim_ticks` | 200 (10 s) | D111 answered |
| `feeding.herd_wait_ticks` | 600 (30 s), then other food in the herd area | D111 answered |
| `contentment.weights` | hunger 20, thirst 20, rest 10, company 15, stress 15, health 10, safety 10 | D110 answered |
| `contentment.cap_above_lowest` | 50 | D110 answered (was 30) |
| `contentment.band.content`, `.uneasy` | 70, 40. Hysteresis 5 | D110 answered |
| `contentment.missed_sleep_rest_cap` | 50 | D105, D110 answered |
| `contentment.hover_range` | 24 blocks | D110 answered |
| `contentment.effect.content.growth`, `.regen` | 1.10, 1.25 | D109 answered |
| `contentment.effect.distressed.growth`, `.regen` | 0.75, 0.5 | D109 answered |
| `genes.intelligence.search_base`, `.search_per_point` | 0.75, 0.05 | D112 answered |
| `genes.intelligence.failed_target_per_point` | 0.05 | D112 answered |
| `genes.intelligence.detection_per_point` | 1 point | D112 answered |
| `genes.aggression.temperament_line` | +50 % | D112 answered |
| `genes.tamability.threshold_floor` | 0.25 | D112 answered |
| `genes.nocturnal.edge_shift_hours_max` | 2 | D112 answered |
| `genes.retreat_threat_ratio` | 2 | D112 answered |

**Produces:** the registered activity ladder with start and stop conditions, `NeedBands` (pure), the stress band edges, `IdlePool` with per-species data, `AmbientCalls`, `HealthyState` (pure), `FeedingSpots` (pure), `FoodClaims`, the spot claims and wait queue on the F2.4 feeder registry, `Contentment` (pure), the dinosaur hover-box provider, `BehaviourGenes`, the extended debug-status bytes.

**Art:** see Art list → each species (idle clips: idle 1, idle 2, yawn, sneeze, sit, curious, ground peck, sentry, startled, shake body, scratch, speak. Contact call sounds). Dinosaur hover box: 3 mood faces and 7 need icons (D149 answered). Shared with F2.3. Not done until those rows are Done (no Placeholder, no To check). A species whose idle clips are missing passes part A with a shorter pool. It does not pass part B.

**Acceptance, part A (both loaders):**

- Unit tests: `NeedBands` at 0.751, 0.749, 0.251, 0.249 and 0 for hunger. Stress band edges with hysteresis. `HealthyState` true and false cases.
- GameTest: when two activities can start, the higher rung runs, for each adjacent pair of rungs [recon RC28].
- GameTest: a follower on a roaming journey with hunger at 0.5 stays in line. At 0.2 it leaves for food [herd d8].
- GameTest: an animal with thirst at 0.2 does not sleep in its window [JC3 J7].
- A diurnal Gallimimus herd lies down after 16,000 and is up by 24,000, within the leader's offset. A Velociraptor pack naps together in both of its windows. A Dilophosaurus naps 2,000 to 5,000 and 8,000 to 10,000, within its offset.
- Over 2,400 ticks a calm animal plays at least 3 different idle acts, and plays no clip of another species (log check).
- With stress forced to STRESSED, sits fall to zero and calls rise, over 2,400 ticks, against the calm baseline.
- A calm Gallimimus calls within ± 50 % of its interval over one in-game day. A lone Velociraptor calls twice as often. No call plays while asleep.
- The Debug Stick shows the band, the activity and the exact contentment parts to an operator, and nothing to a survival player. A survival player sees the hover box only.
- The band byte goes out only on change: no band packet for 200 ticks with steady needs.
- Unit tests, `FeedingSpots`: a 5.25-block open edge gives 3 Gallimimus spots (1.25 wide, gap 0.5). A Gallimimus and a Triceratops share one edge by width. An edge against a wall gives none.
- Unit tests, `Contentment`: the spec example (fed, rested raptor, stress 40, thirst 30 %, no mates) gives 50, uneasy, reasons "lonely" and "thirsty". All parts at 100 give 100. The band does not change at 68 to 72 to 68 (hysteresis).
- Unit tests, `BehaviourGenes`: wolf DNA at full effect gives a Velociraptor pack minimum 5 and maximum 11 (3 and 6 × 1.75, rounded). Intelligence 7 gives a search factor of 1.10. Tamability +100 % halves the trust threshold. No reader takes the raw genome.

**Acceptance, part B (both loaders, sped-up clock):**

- **Healthy-state test.** A herd of 6 Gallimimus in a closed 48 × 48 paddock with grass, water, a stocked herbivore feeder and a vanilla wall fence (D35) runs for 3 in-game days (72,000 ticks). Every member stays CALM the whole time. No need enters URGENT. Each member sleeps once per night window and wakes. The herd makes at least one journey per day inside the fence (D40). Each member plays at least 3 different idle acts per 2,400 ticks. HealthyState is true for every member at every 1,200-tick sample.
- The same test for each species at its group minimum, with the right food: Tyrannosaurus 1 with a stocked carnivore feeder. Velociraptor 3 with a carnivore feeder. Dilophosaurus 1. Triceratops 4 with low plants. Brachiosaurus 3 with tall trees or a tall feeder in open ground. The M3 gate runs it again with a powered electric fence (F3.1).
- **Thirst transitions.** Remove the water from a healthy Triceratops herd. Each member passes WANTS (walks to the old water and searches), URGENT (runs, no sleep) and HARMFUL (loses health). Put water back: all return to SATISFIED and CALM within one in-game day. No member attacks the fence.
- **Hunger transitions.** Empty the feeder of a healthy T. rex. It searches, then runs to food below 25 %, then loses health at the F2.4 starvation rate and turns aggressive. It does not attack a live fence (F2.4 test, linked here, not repeated). With the fence unpowered, the F29 fence break still follows only its own 50 % rule.
- **Company transitions.** Remove 2 of 3 Velociraptors. The "alone" stress cause starts at once. The last raptor calls more, reaches STRESSED, and refuses to breed (F2.5) and to bond (F2.6). The hover box shows the lonely icon. Return the pack: it returns to CALM (D52).
- **Shifted herbivore.** A Triceratops with nocturnal +60 % in a herd of 4 sleeps 18,000 to 22,000 and 5,000 to 7,000, alone. The other three sleep 16,000 to 24,000 together (D182).
- **Stamina transitions.** A T. rex chases a sprinting player to zero stamina, stops, rests and does not hunt again during its cooldown (F2.4 test, linked).
- **Shared feeder.** Eight Gallimimus, hunger at 0.3, share one Herbivore Feeder with one open long side. At most 3 eat at once (the spot count). The others wait within 6 blocks with idle acts. The leader eats in the first round. All eight reach full hunger within 5 minutes (6,000 ticks, proposal: it assumes the F2.4 bite rate fills a Gallimimus in 30 s). No two animals claim one spot. No animal attacks another.
- **Shared carnivore feeder.** A Velociraptor pack of 4 and a stocked Carnivore Feeder: the leader eats first, the others follow by rank, then by wait time. A T. rex pair at one feeder eats side by side if the edge fits both, else the leader first.
- **Herd at one tree.** A herd of six Gallimimus around one oak, with grass and low leaves in their reach band, eats from six different blocks. No two claim one block at the same time. Their stand points keep the 2.2-block spacing [herd d24].
- **Idle grazing.** A content Gallimimus herd of 6 with hunger at 0.9 on a grass field idles by grazing. Each member grazes at least once per 2,400 ticks. Hunger rises slowly by `needs.idle_graze_hunger` per graze and never passes the maximum. No grass block is removed. No two members graze one block at the same time. Set hunger to 0.5: the members eat in FEED and remove the blocks they eat.
- **Reach heights at one tree.** Three Brachiosaurus at one tall tree eat only high leaves. A Triceratops beside them eats only low plants. No claim of one blocks the other.
- **Carcass.** Four raptors at one carcass: the spot count follows the carcass box, the leader eats first, and a starving raptor does not eat a mate (D62).
- **Contentment over a day.** In the healthy-state test every animal stays content. Remove the water: each animal's band falls to uneasy, then distressed, and the hover box names "thirsty". Put water back: content within one in-game day.
- **Contentment effects** (D109). A content calf and a distressed calf of one species, hatched on one tick: after 5 in-game days the content calf is larger. Turn the effects to 1.0 in config: both grow alike.
- **Stress stays the gate.** A calm but uneasy pair (thirsty, stress below the threshold) still breeds. A content animal above its stress threshold does not.
- **Lost sleep.** A raptor kept awake through one window shows the tired icon. Its rest part stays at 50 or lower until its next full sleep. Its stress does not rise from the lost sleep (D105).
- **Play.** Two content young Velociraptors chase each other with no damage, and an adult never joins (D107).
- **Tame stay.** A tame Triceratops on "stay" walks to food and water, then returns to its spot. It sleeps on its spot and makes no journey (D108).
- **Behaviour genes.** A Velociraptor with wolf DNA counts as lonely with 3 mates near it (minimum 5). An unedited one does not. A Velociraptor finds a new feeder 32 blocks away sooner than a Brachiosaurus (median of 20 runs). A Triceratops with +75 % nocturnal sleeps in daytime.
- 20 animals in one paddock keep the server tick under 50 ms with the idle pool, the call scheduler, feeding claims and contentment on (F2.4 budget).

**Register rows:** D97 to D108 are in the Decisions register under "D80 and later". D109 to D112 are under "D109 and later". The lead answered D18, D57 and D100 on 2026-10-12, and most of D97 to D112 in the M2 open-items pass. The follow-up pass answered D105 again, D106, D109 and D149. New rows: "D149 and later" and "D204 and later".

### F2.1: Species core

**Old modules:** B16 (part A), B27 (part B) · **Section:** 11.9, 11.16, 6.1, 6.4, GH#61, GH#63, GH#64 · **Size:** XL + XL · **Depends on:** M1. Part B also needs F2.2 and F2.5 part A.

**Specs:** part B follows `docs/dinosaur-hitboxes-and-movement-design.md` ("Hitboxes", "Anti-clipping and IK", "Cross-loader implementation").

**In game:** each species has its own stats. A dinosaur grows smoothly, and its hitbox and stats grow with it. A dead dinosaur leaves a carcass that players and scavengers eat. Babies follow a parent and do not hunt. Arrows and melee hits land on the visible head, neck, torso, legs and tail. A head hit hurts most. Necks and tails bend away from blocks. Feet sit on the ground.

**Touches:** `J/entity/dinosaur/Dinosaur`, `CarnivoreDinosaur`, `HerbivoreDinosaur`, the six species classes, `J/entity/dinosaur/PartEntity`, both `DinosaurPart` classes (removed), `J/registry/EntityInit`, `MemoryModuleTypeInit`, `AttributesInit`, `J/util/DinoNeedsUtils`, `FenceAwareNodeEvaluator`, `J/init/DataSerializerInit`, the species renderers, new `CarcassEntity`, `RemainsItem`, part, pose-state and bake classes, a Gradle bake task, both loader modules.

**In scope, part A: core:**

- Attributes come from `SpeciesData` at spawn and on `/reload`, not from the egg. The dinosaur caches its `SpeciesData` reference and writes attributes on change only [entity survey B16.2].
- Smooth growth: age is the growth clock minus the birth stamp, from 0 to `growth_days` × 24,000 ticks. No code counts age per tick [JC3 J4]. Size, hitbox and stats interpolate. F1.5 scales the end point by the `size` gene and the variation. No step thresholds. Fix the four flags read from one key.
- A hatched animal starts at growth fraction 0 (completes F1.9).
- Hitbox: `EntityDimensions` keep today's boxes, scaled through the 26.2 `scale` attribute. F2.5 part A replaces them with the movement box. The herd work sets the Gallimimus box to 1.2 × 2.6 [herd spec d20]. Re-test GH#64 (spinning on block edges) after F2.5's arrival fix.
- Culling box from the posed, scaled body, under 204,800 blocks³ for Sodium. Today a Brachiosaurus neck vanishes, because the box is `sized(2, 3)` (`EntityInit.java:36`) [tech survey n29, d11, GH#63].
- Apply the species tracking range (M0.B00). Today all six use 10 [tech survey ON9, d12].
- `FenceAwareNodeEvaluator` uses the mob-aware path check across the whole body width. Today a large animal can path through a 1-wide gap. The herd work's `BodyFit` probe already does this and ports in M0 [herd spec, Paths].
- Water path cost [lead 2026-10-06]: water deeper than the animal's height costs more, scaled by the underwater gene. Babies and small species go around deep water. Large ones wade. It is a cost, not a wall (GH#61).
- Debug status [lead 2026-10-06]: each animal syncs its most urgent need and current activity as two bytes on change [entity survey B16.6]. The Debug Stick shows them on the holder's action bar, in creative or for operators. It replaces the broadcast to all players.
- Sync only what the renderer needs. Keep the 12 need floats on the server [entity survey B16.6].
- Death: implement `tickDeath`. A carcass stays for `needs.carcass_ticks` (24,000, one in-game day [lead 2026-10-12]), then decays. It drops raw meat per species, fresh bones and tissue (remains for F2.7), and only the named special items: Velociraptor toe claw, Triceratops horn, T. rex tooth. No other species drops teeth [lead 2026-10-12].
- Carcass stages [entity survey B16.5, JC3 J5]:
  - `CarcassEntity` is a plain entity with no AI, sized from the dead animal's box. It stores species, growth fraction and a meat pool that starts at species hunger × growth fraction.
  - Stage count = `clamp(floor(√(width × height)) × 2, 1, 10)`. Each player use takes one stage after a 10-tick gap and drops 1 to 2 raw meat plus the Looting level.
  - A scavenger bite takes one share from the pool into the biter's hunger, with no item entity. Mates share the carcass by the F2.0 feeding spots: the carcass box gives the edge. Within a group the higher dominance eats first, then the longest wait (D57) [lead 2026-10-12].
  - The last stage leaves bones: each bone of the species fossil set drops at a 9-in-10 chance, flagged fresh, plus tissue. Then the carcass goes.
  - It decays when the pool is empty or the time ends. The renderer draws the species model in its own `dead` pose (F2.3). Drops spread over ticks (BH.1).
- Babies follow a parent, cannot hunt and need feeding. Stop `SoloHuntRoamBehaviour` for babies and herbivores.
  - A bred baby follows a parent through SBL `FollowParent`: search box 8 × 4 × 8, stop at 3 blocks, stop following at 16 [entity survey B16.3].
  - A lab hatchling searches 16 blocks for an adult of its species. With none, it stays within 8 blocks of its hatch point and counts as alone [recon RC26].
  - Growth bands: young ≤ 0.5, adult > 0.75. A non-adult has no hunt activity and no fear effect from its threat clip. It bites only and never targets a player [entity survey B16.4, S1].
- `MobCategory.CREATURE` with `persistenceRequired`, not `MONSTER` [entity survey B16.1, tech survey n29].
- Memory registry: unique ids for `HAS_GROUP` and `IS_GROUP_LEADER`. Remove the 15 unused types [entity survey fact 46]. Fix the trust attribute key.
- Move client-only effects from `Dinosaur.tick` to `aiStep` or server events, so off-screen animals play no footsteps or roars. Audit `Dinosaur.tick` [tech survey n30, rank 7].
- Prey tables from `SpeciesData.prey_preferences` by clade, players and villagers included. The `canTarget` overrides respect the parent result.
- An animal that drinks does not die of thirst. Fix the dehydration counter and the extra need drain on load (`DinoNeedsUtils.java:153-208`, `Dinosaur.java:783-868`) [recon RC27].
- A night skipped by sleep or `/time set` still grows the animals [JC3 J4]. The growth clock is game time plus `skippedTicks`, saved per level. Each server tick, a forward jump of the day time adds to `skippedTicks`. A backward jump adds nothing [inf]. A growth serum or gene moves the birth stamp. With growth off in config, a saved `frozenAge` replaces the clock.
- The birth stamp syncs once. A `skippedTicks` change sends one 8-byte packet to every client [JC3 J4].
- Every 8 ticks the server computes the growth fraction. A growth-step hook fires once when size changes by more than 0.01 blocks or a stat gets a new value. F1.5's `GenomeEffects.apply` runs from it [JC3 J4, B16.11].
- Voice and step scale with growth: voice volume 0.2 to 1.0, footstep volume 0.1 to 1.0, footstep pitch 2.5 to 1.0, with a small jitter [JC3 J19, B16.14]. A hatchling uses its own hatchling sounds, so its voice pitch does not scale [lead 2026-10-12].
- A species page template with a stat box: health, attack, hitbox, speed, groups, the fence height that body and jump need, and whether it breaks unpowered fences (Tyrannosaurus only) [entity survey B24.4, recon RC24].

**In scope, part B: section hitboxes and IK, in this order:**

- [ ] Remove the dead T. rex head-part code: both `DinosaurPart` classes and the writes to `DINOSAUR_HEAD_POS`.
- [ ] Synced pose state: animation id, start tick, loop flag. The server owns it. Spawn data carries it, so a late player sees a roar at the right point. The server sends a death clip again after a chunk reload [entity survey n32, S26].
- [ ] Section parts per the layout table: square footprints, two torso parts, one per leg, chained necks and tails. Sizes from the render-scaled posed model. No arm parts, no frill part. Counts: Gallimimus 12, T. rex 13, Triceratops 12, Brachiosaurus 17, Velociraptor 12, Dilophosaurus 11 [hitbox design].
- [ ] Placement uses the rest pose, yaw history and baked pose, on both sides, every tick and on load, also for `NoAI` animals. Parts never sit at 0, 0, 0.
- [ ] NeoForge `PartEntity` and a Fabric part class with the MoreHitboxes-design mixin set. IDs `parentId + i + 1` from a reserved block. Set part IDs before tracking starts. Never swap parts or IDs while tracked [hitbox design].
- [ ] Damage multipliers: head ×1.5, neck ×1.25, torso ×1.0, legs ×1.0, tail ×0.5. Parts filter environmental damage and forward the same `DamageSource`. The parent box is not pickable. Parts pass right-clicks to the parent.
- [ ] Each attack clip has one hit tick. The server keeps candidates inside `attack_reach` and inside half the arc from the body yaw. Each takes damage once. Parts receive hits only [entity survey n32, S27]. Start values: arc 90° per species [inf], turn toward target 30° per tick at most [Mowzie's Mobs].
- [ ] Explosions find parts. Sweeps and potions return the parent once. Range tests measure to the nearest part. Add the typed-query hook on both loaders [hitbox design].
- [ ] Chunk-section part buckets on both loaders, with a NeoForge `Level.getEntities` mixin. Fewer parts for far animals.
- [ ] A Gradle task bakes animation files into tables in the jar. Hash check on join. A golden test against recorded bone positions. A build failure when an animation is newer than its bake.
- [ ] Neck and tail bend on the server tick within joint limits, with a synced bend-side byte. A GeckoLib 5 updater adds the bend delta, lerped by partial tick [entity survey fact 35]. A playtest checks for double sway and 20 Hz steps [hitbox design test 19].
- [ ] Client leg and foot IK: once per tick, lerped per frame, full within 16 blocks and none past 32. Use the CHAOS branch IK code with its sources, credited as the Mowzie's Mobs terms require. The 2024 board's open IK items (angle constraints, model and entity integration, movement and idle cycles) are context.
- [ ] A missed part add or remove heals on the next query [JC3 J18]. A part moves between buckets only when its key changes. Before a query, the index compares its part count with the level's. A difference rebuilds all buckets.
- [ ] The index holds every part in the level, foreign parts too, so an Ender Dragon level keeps it on [JC3 J18].
- [ ] Pad the query box by the largest part reach, computed at reload, sideways and below [inf]. A padded box over 1,024 sections returns the full list [JC3 J18].
- [ ] A part leaves the index with its owner. Level unload drops the level entry. The index has the J13 switches.
- [ ] A clip request fails if the species lacks the clip, or if the current clip is exclusive and the new one is not. Two calls: "start if not playing" and "restart". A hold clip stays on its last frame until the server clears it [JC3 J19, rank 5].
- [ ] Each clip has events with a name, a tick and a side. The server runs server events from the start tick (the hit tick, the spit release). Clients run client events from the synced start tick. A loop fires its events once per loop. Use GeckoLib 5 triggered animations and keyframe events. Species data holds only the events that clip files lack [JC3 J19].
- [ ] Clips crossfade over 4 ticks through the GeckoLib transition length [JC3 J19].

**Out of scope:** a live tuning command for scale and eye height. Baby health on a square-root curve. A leader stat bonus [entity survey B16.7 to B16.9]. Hand-authored hitbox lists per pose, the fallback if the bake slips [JC3 B27.7].

**Config (defaults):** `needs.carcass_ticks` 24,000 [lead 2026-10-12]. Species data `growth_days` (T. rex 45, Velociraptor 29, Dilophosaurus 31, Gallimimus 31, Triceratops 47, Brachiosaurus 57) and `young_size_ratios` (hatchling 0.08, infant 0.32) [lead 2026-10-12, M0.B00].

**Produces:** `Dinosaur.getSpeciesData()`, `getGrowthFraction()`, `getEffectiveStat(Stat)`, the growth-step hook, the young and adult bands, `CarcassEntity`, `RemainsItem`, part entities per loader, `PoseState` with clip events, the bake tables and their loader.

**Art:** see Art list → each species (raw meat, special drops: raptor toe claw, Triceratops horn, T. rex tooth, dead pose, carcass stages), Fossils and amber (fresh bones). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance, part A:**

- Each summoned species has the stats of its data file.
- A T. rex reaches full size after 45 in-game days with a sped-up clock (GameTest).
- The hitbox grows with the model.
- An adult T. rex does not path through a 1-wide gap.
- A baby T. rex walks around a 3-deep pond when a land route exists.
- The Debug Stick shows need and activity to its holder only.
- A killed Triceratops leaves a carcass that drops a horn. A killed Velociraptor drops no teeth. A carcass decays after 24,000 ticks.
- A Velociraptor hatchling is 8 % of its adult height, and an infant 32 % (the T. rex ratios).
- Unit test: the stage count follows J5. A Brachiosaurus box of 3.68 × 8.87 gives 10 stages. A scavenger bite shrinks the pool.
- A Brachiosaurus with its box off screen and its neck on screen stays drawn [tech survey d11].
- A baby follows its parent and stops beyond 16 blocks.
- Unit tests on growth interpolation and stat scaling.
- A lab hatchling with no adult near stays within 8 blocks of its hatch point [recon RC26].
- A dinosaur that drinks lives past day 3. A reload drains no extra need [recon RC27].
- A GameTest moves the day time forward by 12,000 ticks. A baby's growth fraction rises by 12,000 ÷ (`growth_days` × 24,000) [JC3 J4].
- With no day-time jump, no age packet goes out for 200 ticks [JC3 J4].
- The last stage of a T. rex carcass drops fresh bones of its fossil set [JC3 J5].
- A hatchling plays its own hatchling sounds, not the adult voice at a higher pitch [lead 2026-10-12]. Footsteps of young animals still scale in pitch and volume [JC3 J19].

**Acceptance, part B (both loaders, GameTests in common code):**

- An arrow at a Brachiosaurus neck hits the neck part for ×1.25.
- A creeper next to a T. rex tail damages it. A sweep across a tail reaches the animal.
- The head part follows a calling Gallimimus's raised head.
- A Brachiosaurus tail bends the same way on server and client.
- Parts appear for a `NoAI` animal on the first tick.
- Melee and arrows on every part resolve on a dedicated server [hitbox design test 11].
- A tail in lava does not burn the dinosaur [hitbox design test 14].
- The bake golden test passes within 0.1 blocks. A client with a different bake hash cannot join [hitbox design tests 15, 16].
- A late player sees a roar at its elapsed time [entity survey S26].
- A bite does not hurt a mob behind the dinosaur [entity survey S27].
- 100 dinosaurs keep the part scan inside the spec's budget.
- A part removed with no index update is found on the next query [JC3 J18].
- With an Ender Dragon in the level, an arrow at a Brachiosaurus neck hits the neck part [JC3 J18].
- A look-around request during a T. rex roar fails, and the roar plays to its end [JC3 J19].
- A client that joins during a roar hears it at the synced tick, with no sound packet [JC3 J19].

### F2.2: Appearance

**Old module:** B09, plus the render items of B24 · **Section:** 11.6, 6.1, 6.3, 8.5, GH#60, GH#63 · **Size:** L · **Depends on:** F1.5, F2.1 part A

**In game:** each animal shows its own genome: layers, colours, gender texture, biome skin variant and morph. Every Beta skin set is complete for both genders. Each species renders at its target scale. 20 Gallimimus on screen run at 120 fps or more, against 26 to 28 fps today [entity survey n4]. Far herds animate at lower cost.

**Touches:** `J/client/renderer/DinosaurRenderer`, `J/client/renderer/dinosaurs/*Renderer`, `J/client/renderer/layer/DinoLayer`, `J/client/CommonClientClass` layer registration, `MOD/textures/entity/**`.

**In scope, layers and colour:**

- Each entity selects its own layers. Fix `DinosaurRenderer.createLayers`, which builds the layers once from the first entity. Layers read the entity's `DinoData`.
- Today the Velociraptor and Triceratops registries draw `base` only. Each species draws every layer of its set.
- Layer colours come from `DinoData.layerColors` with saturation, opacity and tint strength. Fixed layers are never tinted.
- Gender textures for all six species.
- Skin variants: a data list per species in `SpeciesData.variants` (id, biome group, texture set). The fossil biome decides the variant [s11.6]. Variant textures go by path in resource packs, one data line per variant [entity survey B24.5].
- Albino and melanistic morph rendering as tints of the normal layers. No morph textures [lead 2026-10-12].
- Glow layers come from a `_glowmask.png` through GeckoLib 5 `AutoGlowingGeoLayer` [entity survey B24.2]. One glow mask per species serves all its skin sets [lead 2026-10-12]. Name them without the `_e` suffix for ETF [tech survey].
- Render scale: `base_height_blocks` ÷ the measured model height, computed once per species. Targets (D11 answered, interim until real cards): Triceratops 3.0 blocks (scale 1.32), Brachiosaurus 12.5 (scale 1.61), others near their current size. Growth scale multiplies it. The female Dilophosaurus scales larger than the male (`gender_size_weights`) [lead 2026-10-12].
- Dilophosaurus layer cost: collapse the four frill variants into one selected layer. No second white pass when the tint is white (GH#60).
- Colour-layer bake [entity survey n4]: add `docs/superpowers/specs/2026-10-08-dino-layer-baking-design.md` to the scope. Each genome's layers bake into one texture, so each dinosaur draws one model pass. Today each Gallimimus draws 23 passes. The bake covers the design doc's layer-stitching idea. The lead ranks the bake next after herd work [lead 2026-10-09], and it reads only today's layer data, so it can start early [inf].
- Until the bake lands, cache the layer colour and index per entity. Remove about 40 per-frame `Stream` and `Color` allocations per dinosaur [entity survey n5].
- The bake's colour step lifts each tint channel by 255 ÷ 195 and clamps at 255 [JC3 J21].
- A long neck bends with no gap at the throat. For each `neckN` bone, a `throatN` bone that is not its child takes the same rotation [JC3 J23, B09.6].
- Overlay and glow passes that the bake does not cover get a bone mask [JC3 J20, rank 7]. The client reload listener scans each texture off the render thread. A bone draws if a cube UV box holds a texel with alpha above 0. A bone with clear cubes and a drawing child is carried. Apply the mask through GeckoLib 5 bone visibility. Until the scan is ready, or for a bad mask, the pass draws the whole model and logs once. A reload clears every mask.
- A coverage-audit switch (development property only) counts opaque overlay texels outside every bone UV box [JC3 J23, B24.7].

**In scope, skin sets in Beta [lead 2026-10-12]:**

Each set is complete with every layer for both genders. A layer shared by both genders (eyes, teeth, claws, nostrils) is "Shared, artist to confirm" in the Art list. The Art list holds the status of every layer of every set.

| Species | Set | Genders |
| --- | --- | --- |
| Tyrannosaurus | JP/TLW (Rexy female, Buck male) | both |
| Tyrannosaurus | JW look | Not in Beta [lead 2026-10-12] |
| Velociraptor | Nublar | both |
| Velociraptor | Nublar with Sorna crest | both |
| Velociraptor | Sorna (JP3) | both. Its own JP3 model `velociraptor_adult_JP3.bbmodel` and the 21-clip Sorna list (D90 answered) |
| Velociraptor | Sorna with Nublar crest | both |
| Velociraptor | Nublar with arm quills | male. Female: n/a (male-only variant) |
| Velociraptor | Sorna with arm quills | male. Female: n/a (male-only variant) |
| Velociraptor | with quills (card set) | male. Female: n/a (male-only variant). In Beta (D94 answered) |
| Velociraptor | JW look | Not in Beta [lead 2026-10-12] |
| Dilophosaurus | JP | both |
| Dilophosaurus | JPTG | both |
| Dilophosaurus | JWD | both. In Beta (D89 answered) |
| Triceratops | Nublar (JP) | both |
| Triceratops | Sorna (JP3) | both |
| Triceratops | JW look | Not in Beta [lead 2026-10-12] |
| Brachiosaurus | JP | both |
| Brachiosaurus | JP3 | both |
| Brachiosaurus | JW | both |
| Gallimimus | JP | both |
| Gallimimus | JW | both |

- JW sets in Beta: Brachiosaurus JW, Gallimimus JW and Dilophosaurus JWD [lead 2026-10-12]. T. rex, Velociraptor and Triceratops get no JW look in Beta [lead 2026-10-12].
- Dilophosaurus JPTG: one body for both genders. The male tint is fainter and less vibrant (asker's reading: than the female) [lead 2026-10-12].
- Biome group to skin set (D180 answered) [lead 2026-10-13]: the table in spec F2.2. The biome group with the largest share of the sequenced DNA wins. A tie gives the JP/Nublar base set. Amber counts by its biome group. Syringe and remains DNA does not count.
- Velociraptor rule (D248) [lead 2026-10-13]: a clone from Nublar biomes is Nublar with the Nublar crest. A clone from Sorna biomes is Sorna with the Sorna crest. A male Sorna clone rolls at random for "with quills" or "with arm quills". Mixed looks come only from Nublar × Sorna breeding, or from advanced gene editing after the player unlocks both sets. The mixed looks: Nublar with quills, Nublar with Sorna crest, Sorna with Nublar crest. Breeding (D249 answered) [lead 2026-10-13]: a Nublar × Sorna young takes each look trait from a random parent, one roll per trait. The traits are the base skin set, the crest and the quills.
- Not in Beta: the named raptors Charlie, Echo, Delta and Blue [lead 2026-10-12].
- Not in Beta: the T. rex "Big Eatie" and "Little Eatie" sets (D87 answered).

**In scope, render cost:**

- Far animals animate at lower cost with GeckoLib 5 extension points [entity survey n6]. Hold the last pose beyond 32 blocks. Skip frames by distance and size: skip = distance ÷ 20, divided by half the largest box side for bodies over 2 blocks. Two controllers at most. Expected: 50 to 75 % fewer keyframe evaluations at 32 to 96 blocks (estimate). Risk: this relies on an override of `applyAnimationControllers` on an internal GeckoLib interface. If unsupported, pause far controllers instead.
- Render types stay batchable and shader-safe [tech survey n14]: one `RenderType` per texture, cached. No core shader. No `sortOnUpload` on an opaque type.
- The culling box is F2.1's. F2.2 renders inside it.

**Out of scope:** a single-pass tint table with a core shader (D69 answered: textures baked on the CPU, which work with shader packs) [JC3 B09.1]. A packed pattern sheet [JC3 B09.2]. Pose replay for overlay passes [JC3 B09.4]. Hitboxes (F2.1).

**Produces:** `DinosaurRenderer` that reads per-entity data, `SkinVariant`, the layer bake cache.

**Art:** see Art list → each species (skin layers per set and gender, glow masks). Not done until those rows are Done (no Placeholder, no To check).

**Config (defaults):** none in TOML. Render scale and variants are species data (M0.B00).

**Acceptance:**

- Two T. rex of different genders and colours render differently, on both loaders.
- Each Beta skin set renders for both genders with every layer (male only for the quill sets). No layer file is a byte copy of the other gender's file, except layers marked `shared_by_genders` after the artist confirms them.
- A Velociraptor and a Triceratops draw every layer of their set.
- A Dilophosaurus herd of 10 keeps client FPS above a baseline recorded in the PR.
- A Triceratops measures about 3 blocks at full growth.
- 20 baked Gallimimus in view reach at least 120 fps. Record the layers-off figure (138 fps today) in the PR [entity survey n4].
- A dinosaur beyond 32 blocks holds its last pose [entity survey n6].
- A Brachiosaurus neck draws when its body leaves the screen edge [tech survey rank 4].
- In the glow pass, a dinosaur draws only its masked bones. A wrong mask draws the whole model and logs one line [JC3 J20].
- Unit test: the tint lift makes 195 into 255 and clamps higher values [JC3 J21].
- A Brachiosaurus neck bend shows no gap at the throat, if the model has throat bones [JC3 J23].
- The coverage audit reports no stray texels for each shipped overlay [JC3 J23].
- A Velociraptor cloned from desert fossils is Nublar with the Nublar crest. One cloned from badlands fossils is Sorna with the Sorna crest. Over 20 male Sorna clones, both quill sets appear, and no clone shows a mixed look (D248).
- A Gallimimus cloned mostly from badlands DNA shows the JW set. With equal DNA from two groups, it shows the JP base set (D180).

### F2.3: Animations, sounds and visual effects per species

**Old modules:** B24 (animation and sound items), B09 (clip wiring), B19 (threat display sound defect) · **Section:** 11.9, 11.10, 11.14 · **Size:** XL · **Depends on:** F2.1 part B, F2.2

**In game:** each species moves, eats, drinks, calls, sleeps, fights and dies with its own clips and its own sounds. No species borrows another's roar or clip. Dinosaurs leave fading footprints, kick up dust when they run, drip water after a drink, breathe fog in the cold, snore Zzz in their sleep, and make the screen shake when a big one roars nearby.

**Touches:** `MOD/animations/entity/*`, `MOD/sounds/entity/**`, `MOD/sounds.json`, `J/registry/SoundInit`, `ThreatDisplay`, the clip and event tables of F2.1, new particle types and client effect classes, overlay layers.

**In scope, clips [lead 2026-10-12]:**

- Each species has every clip of its 2020 board list, plus every clip that a Beta behaviour needs. Names are the board's. The task plan maps them to file clip names.

  | Species | 2020 board clips | Clips that Beta behaviours add |
  | --- | --- | --- |
  | Tyrannosaurus | Walking, Running, Drinking, Eating1, Eating2, Attack, Attack2, Calling, Calling2, Resting, Sleeping, Look L&R, Roaring1, Roaring2, Roaring3, Injured, SniffingAir, SniffingGround, ShakeHead, Limping, Death, Swimming | idle, rest idle, get up, dead pose, flinch, jump and fall, stalk, nesting, yawn, sneeze |
  | Velociraptor | Drink, Eat, Walk, Run, Attack, Attack2, Sleep, Sit, Jump, Idle1, Idle2, Call, Call2, Call3, Swim, Limp, Death, SniffAir, SniffGround, Look L&R | rest, rest idle, get up, dead pose, flinch, pounce, roar (warning), nesting, yawn, sneeze |
  | Dilophosaurus | Drink, Eat, Walk, Run, Attack1, Attack2 (Spitting), Call, Call2, Speak, Sleep, Sit, Jump, Idle1, Idle2, Swim, Limp, Death, SniffAir, SniffGround, Look L&R, Headshake, Curious | rest, rest idle, get up, dead pose, flinch, yawn, sneeze |
  | Triceratops | Drink, Eat, Walk, Run, Attack, Attack2, Call, Sleep, Sit, Jump, Idle1, Idle2, Call2, Swim, Limp, Death, SniffAir, SniffGround, Look L&R, Graze, HeadShake | rest, rest idle, get up, dead pose, flinch, roar (warning), ramwindup, ram, calling, sentry, startled, yawn, sneeze |
  | Brachiosaurus | Drink, Eat, Walk, Run, Attack, Call, Call2, Sleep, Sit, Jump, Idle1, Idle2, Swim, Limp, Death, SniffAir, SniffGround, Look L&R, HeadShake, Stand on hindlegs Eat | rest, rest idle, get up, dead pose, flinch, roar (warning), high eat, calling, sentry, startled, yawn, sneeze |
  | Gallimimus | Drink, Eat, Walk, Run, Attack, GroundPeck, Call, Call2, Sleep, Sit, Jump, Idle1, Idle2, Swim, Limp, Death, SniffAir, SniffGround, Look L&R, HeadShake, Sentry | rest, rest idle, get up, dead pose, flinch, calling (warning, journey start), startled, yawn, sneeze |

- Sources of the behaviour clips: rest and sleep (F2.4), get up [herd spec d27], dead pose (F2.1 carcass), flinch (hurt), warnings (F2.6, [recon RC38, RC39, RC19]), pounce and stalk (F2.5, [recon RC55, RC56]), calls (F2.5), herd clips calling, sentry and startled [herd spec d26], nesting (F2.5 breeding), jump and fall (jump gene), high eat (F2.4 grazing reach), yawn and sneeze (the Dino VFX idle acts) [Dino VFX]. Every species also gets the T. rex card extras: startled, shake body, scratch, speak [lead 2026-10-12].
- Own clips [lead 2026-10-12]: Brachiosaurus rest, sit and sleep, Triceratops sit, Dilophosaurus roar older and roar young. The existing clips play as placeholders meanwhile and count as missing.
- The Velociraptor threat screech gets its own clip and sounds, apart from the pack call [lead 2026-10-12].
- The Dilophosaurus warning plays its intimidate clip, converted from the asset repo. If the player stays, the spit follows [lead 2026-10-12].
- The Velociraptor Sorna model plays its own 21-clip Sorna list, with head shake (D90 answered).
- Play uses the run clip and the Velociraptor pounce. No play clip (D107).
- A clip from another species, a clip with 0 bones, or a byte copy counts as missing. The Art list names each such placeholder. Each one gets its own clip before this package is done.
- Herd decision 27 lets a get-up with no clip play plain idle. That rule applies in development only. Beta ships each species' get-up clip.
- REST plays the species' own `sleep` clip while the animal sleeps (F2.4) [recon RC17].
- Rename the Triceratops `Sprint ` and `injured` clips to `run` and `flinch` if they are the same motion.
- Wire `RUN_ANIM`. Read flinch lengths from the animation file.
- Walk starts at 0.05 and run at 0.2 blocks per tick (start values). F2.4's running state reads the same signal [JC3 J19].
- During a roar, the head-look and sway updaters scale by `1 − max(walk-block weight, action weight × suppression)`. Suppression starts at 0.75, and 0.8 for the T. rex [JC3 J19].
- Stress band idle weights: moved to F2.0 "Band shifts the pool". F2.3 keeps the clips.
- Limp is in Beta: the 2020 board lists it for every Beta species [2020 board]. Limp replaces walk below `behaviour.limp_health_fraction` (0.30, D92 answered). Until its own limp clip exists, the T. rex and the Triceratops play their injured clip. Each species needs its own limp clip. Idle acts (sniff, look, head shake, scratch, curious, ground peck, speak): moved to the F2.0 idle pool.
- Neck-reach IK waits for the eat and drink clips. The Gallimimus `eat` and `drink` clips come first in the art queue [recon RC42, hitbox design].
- Artist checklist: box UV, whole-number cubes, root bone pivot at 0, 0, 0, a named head bone, clip names [entity survey B24.1]. The Brachiosaurus has `head1` and `head2`, not `head`.
- An art mistake shows as one log line [JC3 J23]. Every bone lookup goes through a cache. A missing bone logs once with the model's bone list. A missing clip logs once per species with the expected path. In a development run, a species list of required bones fails the model build. A validate-on-load switch checks every clip against its model.

**In scope, sounds [lead 2026-10-12]:**

- Each species registers its own sound events from its own folder [design doc]. Events: roar or warning, each call (warn, gather, threaten, contact), nest call, mating call, hatchling sounds, hurt, death, idle breath, eat, drink, sleep, yawn, sneeze, snort, footsteps. Hatchlings, yawn, sneeze and snort get their own sounds per species [lead 2026-10-12]. One carnivore eat sound may serve all carnivores [lead 2026-10-12].
- Sourcing: the lead's direction is to source the dinosaur sounds from Jurassic World Evolution 3. Shared sounds may serve as placeholders meanwhile. They count as Missing on the Art list [lead 2026-10-12].
- Defect: `ThreatDisplay` plays the T. rex roar for every species. Each species plays its own warning sound [recon RC38]. The lead accepted the list (D230) [lead 2026-10-13]: T. rex roar, Velociraptor screech (apart from the pack call), Dilophosaurus hoot during the frill display, Triceratops snort (domination display), Brachiosaurus bellow, Gallimimus alarm screech.
- Nesting: only the T. rex and the Velociraptor play a nesting clip. The other four species play an idle clip while the female builds her nest [lead 2026-10-13]. The snort effect needs no clip. It plays over the idle or the sniff clip (D231).
- Footsteps play in time through GeckoLib 5 keyframe events [entity survey B24.3]. Every walk and run clip carries footstep keyframes (Art list rows). Wire every keyframe effect name to a sound event. Rename the Brachiosaurus `walk2` and `eatingtall2` effects to names that match their sounds.
- Gameplay sounds (roars, calls) start from server clip events, not only from keyframes, because Dynamic FPS skips frames [tech survey].
- The real T. rex recordings are D91. Until the artist names their folder, the T. rex sounds count as Placeholder, and this package is not done.

**In scope, Dino VFX [Dino VFX, lead 2026-10-12]:**

| Group | Effect | Trigger |
| --- | --- | --- |
| Mouth and body | Spit particles when roaring | roar clip event |
| Mouth and body | Drinking water drips | after a drink clip |
| Mouth and body | Blood drips when eating meat, a bloody mouth overlay, a bloody claw overlay after attacks (T. rex, Velociraptor, Dilophosaurus, D232) | eat clip on meat or a carcass, an attack hit |
| Mouth and body | Plant bits falling from the mouth | eat or graze clip on plants |
| Mouth and body | Foggy breath in cold biomes | idle breath in a cold biome |
| Mouth and body | Yawn, sneeze, snort | idle acts and sleep start |
| Mouth and body | Dilophosaurus spit: splash particles and a blob projectile with physics | the F2.6 spit |
| Movement | Slow-fading footprints, different by body size and by biped or quadruped | each footstep event on a solid top face |
| Movement | Dust trail when running | the running state |
| Movement | Air trails during attack clips (arm and tail swipes) | attack clip events |
| Movement | Screen shake when a large dinosaur walks close or roars, with a client switch to turn it off | footstep and roar events within range |
| Environment | Snow cover overlay on an animal that sits or sleeps in a snow biome | rest or sleep in snow |
| Environment | Splash rings and water ripples near large dinosaurs in water | movement in water |
| Sleep | Zzz particles and bubbles | the sleeping state |

- Species applicability follows the spec F2.3 matrix, which the lead accepted with two changes [lead 2026-10-12]. The Gallimimus (an omnivore) gets blood drips and a bloody mouth. Screen shake is for the T. rex, the Triceratops and the Brachiosaurus.
- Effects run on the client from synced state and clip events. No server packet per effect.
- Overlays (blood, snow) are render layers with F2.2 bone masks. They clear after a time.
- Footprints are client particles or decals. They never change blocks.
- Every effect follows the BH.1 low-effects switches: particles (all, reduced, none), screen shake, glow layers.
- Durations, ranges, sizes and fade times are config values, set in the task plan with a stated basis.
- Every row of Art list → Dinosaur effects is Beta [lead 2026-10-12].

**Out of scope:** the mud overlay ("later on" [Dino VFX]). Aquatic breach and dive effects (Release 1 and later). Animation speed that follows body size and look-around clips on a second controller (D14). The `tranqed` clip (F4.3). The crouch before a pounce (F2.5, deferred).

**Config (defaults):** effect durations, ranges and sizes, set in the task plan. `client.screen_shake` true (BH.1).

**Produces:** per-species clip and sound tables, the particle types and overlay layers, a test that no two species share a sound or clip file, apart from the shared carnivore eat sound.

**Art:** see Art list → each species (every clip and sound row), Dinosaur effects. Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- Each species plays every clip of its table in a fitting state (a scripted check per species in the PR, with a clip).
- A hurt animal below 30 % health limps with its own limp clip (D92).
- Each species plays its own nest, mating, contact and hatchling sounds, and its own yawn, sneeze and snort sounds.
- Unit test: no two species register the same sound file, apart from the shared carnivore eat sound. No clip file is a byte copy of another species' clip.
- A Gallimimus warns with its own calling clip and sound. A Velociraptor's warning plays a Velociraptor sound, not the T. rex roar [recon RC38].
- Footsteps play in time with walk and run for all six species.
- A loaded Velociraptor clip with a missing bone logs one line that lists the model's bones [JC3 J23].
- Each effect in the VFX table shows in its trigger, on both loaders.
- With particles off, no Nublar particle spawns. With screen shake off, the camera does not shake.
- 20 animals with all effects keep the client above the F2.2 baseline.

### F2.4: Needs

**Old module:** B17 · **Section:** 11.10, 6.2 · **Size:** XL · **Depends on:** F2.1 part A, F2.5 part A

**In game:** dinosaurs get hungry, thirsty, tired, lonely and stressed, and each need has results. Each species eats its own diet, and other mods' food counts through tags. A herbivore grazes what its head can reach. A carnivore picks its prey and scavenges carcasses. A tired predator stops its chase. A park of 100 dinosaurs keeps 20 ticks per second.

**Touches:** `J/util/DinoNeedsUtils`, `J/entity/dinosaur/Dinosaur` (running state, timers), `J/entity/dinosaur/ai/behavior/needs/*`, `J/entity/dinosaur/ai/sensor/*`, `NearestFeederSensor`, `WalkToNearestFeeder`, `J/entity/dinosaur/DinoOneShots`, `R/data/projectnublar/diet/` (becomes tags), `hunt_score_config.json` (remove).

**In scope, needs and stress:**

- Needs: hunger, thirst, stamina and sleep, stress. Loneliness is part of stress, with no bar of its own (D52 answered) [lead 2026-10-13]. Below the group minimum, the "alone" cause adds stress each step. The hover box shows the lonely icon when it is the main cause. Rates come from `SpeciesData` and genes. Fix: `IS_HUNGRY` never clears, the Drink distance differs from the walk distance (use one close-enough distance [entity survey feature 11]), `isSociallyDrained` reads stamina, `tickSocial` is empty, `IS_EXHAUSTED` is never set.
- Gene readers: `thirst`, `energy`, `stomach_capacity`, `eat_rate` and `nocturnal` set need rates, the stamina maximum and the activity window, through the F2.0 `BehaviourGenes` reader. `eat_rate` sets the bite time, so it sets how long an eater holds a feeding spot. The `nocturnal` gene moves the window one step at ±50 % (D49). A `scent` gene waits for D48 [recon RC33].
- Herd scope rule [lead 2026-10-10]: build the needs other than stamina into herd play only after F2.5 part A passes its playtests.
- Stamina needs a non-zero maximum. M0.B00 fixes the key [entity survey fact 51].
- Stress inputs in Beta: food and water, and company. A live-bait hunt lowers stress (F3.3). No enclosure or block comfort [s11.10].
- Stress meter: one saved integer per dinosaur [entity survey B17.5, S5, rank 9]. Every 20 ticks, offset by entity id, add the input deltas. Subtract a drift toward zero (stress ÷ drift divisor, rounded). Clamp from 0 to the maximum. Set `IS_STRESSED` at or above `stress_threshold`. Clear it below the threshold minus the hysteresis. A live-bait kill subtracts its relief once. Send the band (calm, uneasy, stressed) to the client on change only. `stress_threshold` is one value per species, default 50, with no gender split (D13, D88 answered). The Stress Tolerance gene raises it [lead 2026-10-12]. One stress step is 1,200 ticks [lead 2026-10-12]. Stress at 100 does no damage (D103).
- Results: aggression, health loss, death. A carnivore that kills a player gets hunger back. Unmet needs never drive fence attacks [F29].
- Starvation [lead 2026-10-06]: at zero hunger, lose `needs.starvation_damage` every `needs.starvation_interval_ticks`. A Velociraptor (35 HP) lasts about 2.3 minutes, a T. rex (155 HP) about 10. It turns aggressive and does not attack fences. Today `tickHunger` kills at the next tick.
- Two hunger thresholds: seek food below 75 %, run to food below 25 % [entity survey B17.6].
- Need cooldown [lead 2026-10-06]: a met need clears its memory and starts `needs.cooldown_seconds.<need>`, 5 minutes (D160 answered) [lead 2026-10-13]. An urgent need skips the cooldown. Build it as a memory with an expiry, registered with a codec. The sensor and behaviours start only while it is absent [entity survey B17.4, S4].
- Stamina-limited chases [lead 2026-10-06]: a chase ends when stamina reaches zero. `needs.hunt_cooldown_seconds` passes before the next hunt. Prey and players can escape. Build: SBL `InvalidateAttackTarget.invalidateIf(stamina <= 0)`, then `HUNT_COOLDOWN` with an expiry and rest [entity survey feature 3]. A new hunt needs the cooldown over and stamina above 0.33. A pack hunt needs the leader's cooldown over and half the members above 0.33. Each member that bites the kill gets hunger and the live-bait relief [recon RC52]. The cooldown counts down in one place [entity survey B17.13]. Playtest that every carnivore still feeds.
- Brain activities: moved to F2.0 "Activity ladder". F2.4 fills FEED and REST.
- Running state from the actual movement speed each tick (F2.3's gait signal), not from a flag. Today the chase flag never clears, so stamina drains double until a reload.
- IDLE pool: moved to F2.0 "Idle pool per species".
- Sleep [entity survey B17.10, S7]: `SmartBrainSchedule.byDaytime` switches to REST in the species window. Sleep starts when the sleep need is low, no danger memory exists and the animal stands on ground, not in water. A sleeper does not move, and its need recovers. Damage wakes it and sets `WAKE_LOCK` for 200 ticks. Each wake sets `SLEEP_COOLDOWN` for 6,000 to 8,000 ticks. In REST, each animal rolls drowsy at 1/30 and rouse at 1/240 per second, so a herd settles one at a time [recon RC30].
- A starving animal stays awake: no sleep while hunger or thirst is below 25 % [JC3 J7].
- An animal with a hunter near does not rest. Every 20 ticks, test the shared nearby-entity memory for a threat in a 10 × 5 × 10 box: an awake mob that targets it, a larger mob of another species, or a monster. A threat blocks sleep and wakes a sleeper [JC3 J7].
- Sleep windows by `activity_window`: moved to F2.0 "Sleep windows" (D97, D98). The sleep behaviour above reads the window from F2.0.

**In scope, food and water:**

- Carnivores: prey by clade from `SpeciesData`. Players and villagers are prey. Scavenge carcasses and dropped meat. The item sensor looks for dropped meat only while hungry, within 16 blocks [entity survey B17.9].
- Prey pick once per scan by one scored pass over the shared visible list. Cache the enclosure test per target for 100 ticks. Fix the block-entity test that is always false. Today each hungry carnivore makes about 1,200 lookups and 800 allocations per tick [entity survey B17.7, rank 6, S3].
- Pack threat = the sum of adult members within the leader's follow range, capped at 4× the leader's threat. Fill the group to-do in `DinoNeedsUtils.getThreatScore` and call `CarnivoreDinosaur.canHuntWithPack` (D58 answered) [recon RC46].
- A starving carnivore can take its own species at the card weight (T. rex 5 %), never its own group or family. A same-species carcass is food only while it starves (D62 answered) [recon RC60, JC3 B17.32].
- Herbivores graze blocks in a diet tag (grass, leaves, crops, flowers) and consume them. Reach from `SpeciesData` (Brachiosaurus high, Triceratops low).
- Grazing inside a height band [entity survey B17.8, S6]: band = species reach × growth fraction from the feet. Each scan samples 16 random columns within 12 blocks every 40 ticks while hungry, under 200 block reads. Read only inside the band. The first free block in the diet tag wins into `GRAZE_TARGET` with an expiry. Consume it as sheep do, only with `mobGriefing` on.
- Grazing together [lead 2026-10-12]: a herd gathers around one tree or plant patch. The search starts near the group's food position and skips blocks in the F2.0 `FoodClaims` map and stand points inside a mate's spacing [herd d12, extended to feeding targets]. A found block is claimed with an expiry (D111). No two animals target one block. Brachiosaurus takes the high leaves, smaller animals the low plants. F2.0 owns the claim map and the rule.
- Idle grazing [lead 2026-10-12]: a herbivore that is content or idle grazes a grass or plant block in its reach with its eat clip. The F2.0 idle pool starts it. This search finds the block and F2.0 `FoodClaims` claims it, so no two animals use one block. Each idle graze adds `needs.idle_graze_hunger` (proposal 0.01 of the hunger maximum, lead decides). By default it does not remove the block (`needs.idle_graze_removes_block` false), so a content herd does not strip its pasture. A hungry animal still eats and removes the block [s11.10].
- A grazer starts a graze move at 8 blocks from its spot and stops at 4 [recon RC34].
- Head contact [lead 2026-10-09, hitbox design]: eating and drinking count only when the head touches the food, water or feeder. The animal finds a stand point from which its mouth reaches. Each species has a reach envelope and a contact tolerance. Feeders follow the same rule. Neck-reach IK waits for the F2.3 eat and drink clips.
- Arrival: horizontal distance ≤ 1.75 + width ÷ 2, vertical ≤ body height + 1. Then head contact decides the bite [JC3 J6].
- Thirst [entity survey feature 11, B17.2]: below the seek threshold, set `IS_THIRSTY`. The water sensor runs only then. It scans 8 by 2 blocks every 60 ticks and grows by 8 blocks per failure, up to the follow range. A found shore block goes into `HAS_FOUND_WATER` for 6,000 ticks. The animal walks there, plays its drink clip and refills, then the cooldown starts. Seek below 75 %, run below 25 %. At zero thirst it loses 1 health each 40 ticks (D104 answered).
- A drinking animal faces the water. It turns at most 15° per tick, slides to the edge with a 0.35-block final approach, drinks under 20° of error, and gives up after 60 ticks [JC3 J6, B17.29].
- Diets are item and block tags per species under `data/projectnublar/tags/`, so other mods' food joins. Food value per item or tag lives in species data, with `c:` tags [entity survey B17.14].
- Food tiers by five tags [JC3 J6, rank 11]: ratios free 1, farmed 2.5, effort 5, premium 10, bale 45 (start values), scaled to species hunger. Untagged items count as farmed, untagged blocks as free. The lead chose these tiers over the GH #6 clade chunks [lead 2026-10-12].
- The Gallimimus is an omnivore: its diet tag holds plants and small meat (dropped meat, eggs), and it uses both feeder kinds [lead 2026-10-12].
- Lost sleep (D105 answered): a missed window halves stamina regeneration until the next full sleep. It adds no stress [lead 2026-10-12, follow-up pass]. F2.0 caps the rest part of contentment.
- Food source order: dropped items, carcasses, feeders, diet blocks in the grazing band. Water has its own source. The first source with a reachable target wins [JC3 J6, B17.21].

**In scope, cost:**

- Replace the three per-entity block scans with a shared chunk-section cache or a POI search, under `needs.sensor_budget_blocks` per tick. Today about 735,000 reads per tick at 100 dinosaurs, about 22 ms, spikes near 440 ms (estimates) [entity survey rank 2, S1]. Target: under 1,000 reads, under 0.1 ms, no aligned spike.
- Per-level feeder registry, owned here [entity survey B17.1, JC3 B20.17]: keyed by chunk, a query reads only chunks inside its radius, level unload clears it. F3.3 registers each feeder. Until then, the existing Carnivore Feeder joins on load and leaves on removal.
- Shared feeders [lead 2026-10-12]: each registry entry also holds the feeder's open edges, one claim per feeding spot (eater id and expiry) and a wait queue (eater id, group id, dominance, join time). The F2.0 `FeedingSpots` model sets the spots from the edge and the eaters' body widths. The F2.0 queue rule picks the next eater. Claims change on events only, never by a per-tick scan. A feeder never serves one animal only.
- Offset every sensor start by entity id, for example `scanRate(e -> base + (e.getId() & 15))` [entity survey B17.3].
- Path budget [entity survey B17.11, B17.12, S8]: a need walk asks for a path only while the per-level counter is under `ai.path_budget_per_tick`. Otherwise it queues for the next tick. Herd paths use the hitbox design's budget split.
- Danger memory: a spot where the animal took damage or a group mate died is a soft path cost within 6 blocks. It lasts 24,000 ticks, 1 in-game day (D61 answered, reversed from 2,400) [recon RC58].
- Timers use game time for need decay, cooldowns, grace periods and memory lifetimes. Clamp elapsed time, for example to 40 ticks. Today they count `tickCount` (`Dinosaur.java:581,723,760-765`, `DinoOneShots.java:70`) [tech survey n31, OS4, d10].
- Remove the 12 print calls in 6 files from `DinoNeedsUtils`, sensors and behaviours [tech survey d7, OS16].
- Need search backoff [JC3 J6, rank 1]: each failed search doubles the retry delay from 20 ticks (food) or 40 ticks (water), capped at 600 ticks, plus a random wait up to that delay.
- A failed-target ring of 64 entries, 600 ticks each. A failed path, a walk over 600 ticks or more than 2 re-paths adds the target. A new failure refreshes it. A throttled search (F2.5, J16) is not a failure [JC3 J6].
- A per-level change map stores the last change time per chunk: block place, block break, item entity join, feeder slot change. A search runs early when a chunk in its radius changed after the failure. Above 512 chunks, entries older than 1,200 ticks go. Level unload clears it [JC3 J6].
- Two more early triggers: the need falls below 25 %, or the animal moves more than 8 blocks from the failed position (at most 2 times per failure) [JC3 J6].
- A search runs at most 6 path tests. Before it reads a 16-block section, it asks the section palette for a diet-tag state. A section with none costs no reads [JC3 J6, B17.20].
- Build search timers on SBL memories with an expiry and gate sensors with `onlyScanIf` [JC3 J6].
- Predators [JC3 J8]: the prey pass tests reach for at most 6 candidates per scan, and a failed target costs no test. While hunting, retest reach every 40 ticks. A fail ends the hunt and adds the failed target. After any hunt, a 50 to 69 tick lockout precedes the next pick. An optional species ratio skips prey whose max health exceeds ratio × own max health (default off, JC3 uses 0.9).

**Contentment:** the needs feed the F2.0 contentment score and its hover box [lead 2026-10-12]. Stress stays the gate for aggression, breeding and taming.

**Out of scope:** a shelter search before sleep. Herd decision 28 rests members in their slots [JC3 B17.28]. Enclosure space and block comfort as stress inputs [s11.10]. Fence attacks [F29].

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `needs.starvation_damage` | 1 | [lead 2026-10-06] |
| `needs.starvation_interval_ticks` | 80 (4 s) | vanilla player rate [lead 2026-10-06] |
| `needs.cooldown_seconds.<need>` | 300 (6,000 ticks) after eating or drinking. An urgent need skips it | D160 answered |
| `needs.thirst_damage_interval_ticks` | 40 | D104 answered |
| `needs.stress_step_ticks` | 1,200 | [lead 2026-10-12] |
| `needs.stress_per_cause` | 2 per step | [lead 2026-10-12] |
| `needs.hunt_cooldown_seconds` | proposal 120 | Naturalist 2,400 ticks [entity survey feature 3] |
| `needs.sensor_budget_blocks` | set in the task plan | plan |
| `ai.path_budget_per_tick` | 4 | [entity survey S8] (estimate) |
| `needs.search_base_ticks.food` | 20 | [JC3 J6] (start value) |
| `needs.search_base_ticks.water` | 40 | [JC3 J6] |
| `needs.search_delay_cap_ticks` | 600 | [JC3 J6] |
| `needs.failed_target_ticks` | 600 | [JC3 J6] |
| `needs.search_path_tests` | 6 | [JC3 J6] |
| `needs.food_tier.<tier>` | 1, 2.5, 5, 10, 45 | [JC3 J6] |

Stress start values [entity survey S5], proposals for playtest: one step every 1,200 ticks. Each active cause adds +2 per step: hungry, thirsty, alone (below `group_size_min`, young not counted) [lead 2026-10-12, D52, D246]. Lost sleep is not a cause. Maximum 100. Fed, watered and with company −1. Drift divisor 12. Hysteresis 10. Live-bait relief −20.

**Produces:** `NeedsState` with getters per need. Memories `STRESS_LEVEL`, `IS_STARVING`, `IS_STRESSED`, one cooldown per need, `HUNT_COOLDOWN`, `SLEEP_COOLDOWN`, `WAKE_LOCK`, `GRAZE_TARGET`. The per-level feeder registry with feeding-spot claims and wait queues, and the change map.

**Art:** see Art list → each species (eat, drink, sleep, rest clips and eat and drink sounds). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance:**

- GameTest on both loaders: a starving T. rex loses health at the starvation rate, turns aggressive, and does not attack a live fence.
- Stamina drain returns to the walking rate after a chase. A T. rex that chases a sprinting player stops at zero stamina.
- An animal that ate does not walk to food again during its cooldown.
- A fed, watered pair has falling stress.
- A hungry, thirsty animal gains 4 stress per step. A raptor kept awake through a window gains none [lead 2026-10-12].
- A Brachiosaurus eats leaves at height 8, and a Triceratops does not.
- An animal eats only from a stand point where its head touches the food, water or feeder.
- A test skips a dinosaur's tick for 100 ticks. It loses the same hunger as one that ticks, within the 40-tick clamp [tech survey n31].
- With 20 thirsty dinosaurs, the water sensor stays under the budget [entity survey B17.2].
- 20 animals in one paddock keep the server tick under 50 ms.
- Unit tests on need decay and stress arithmetic.
- Herd members in REST fall asleep and wake on different ticks [recon RC30].
- A pack of three Velociraptors hunts a prey that one skips [recon RC46].
- A fed T. rex never targets its own species. A starving one never targets its own group [recon RC60].
- A hungry animal with no food in reach searches at gaps that double from 20 to 600 ticks plus the wait (logged) [JC3 J6].
- A diet item dropped inside the radius starts a search at the next scan [JC3 J6].
- An unreachable diet block gets no second path test for 600 ticks [JC3 J6].
- Unit test: a section with no diet-tag state costs no reads [JC3 J6].
- A raptor with dropped meat and a stocked feeder in range eats the meat first [JC3 J6].
- A starving dinosaur does not sleep in its window. A dinosaur with a monster in its box does not sleep [JC3 J7].
- A predator whose prey hides makes at most 6 reach tests per scan [JC3 J8].
- A level with 50 feeders answers a query with reads of the feeders inside the radius only [JC3 B20.17].
- A herd of six Gallimimus around one oak eats from six different blocks. None overlap [lead 2026-10-12] (F2.0 test, linked).
- Eight Gallimimus share one Herbivore Feeder. At most the spot count eats at once, and all reach full hunger within the D111 time (F2.0 test, linked).
- A content Gallimimus herd idles by grazing. Hunger rises slowly. No grass block is removed [lead 2026-10-12] (F2.0 test, linked).

### F2.5: Groups: herds, packs, leaders, breeding, nests

**Old modules:** B26 (part A), B18 (part B) · **Section:** 11.9, 11.10 · **Size:** XL + L · **Depends on:** part A: F2.1 part A. Part B: F2.4 and part A.

**Specs:** part A follows `docs/dinosaur-hitboxes-and-movement-design.md` ("Movement", the planning half of "Anti-clipping and IK", its decisions register) and `docs/superpowers/specs/2026-10-08-herd-movement-redesign.md`.

**In game:** large and long dinosaurs walk through gaps, corridors and forests without spinning, wedging or clustering. A herd grazes over a wide area. Every 2 to 5 minutes the leader starts a journey, and the herd follows as a wedge that turns into single file in corridors. Each herd and pack has one leader. A rival challenges the leader with a display, and no animal gets hurt. Dinosaurs call to warn, gather and threaten. A Velociraptor pack picks one prey, surrounds it and attacks together. A calm pair with a nest lays eggs. Adults guard the nest and the young.

**Boundary between the parts** [herd spec d26, d32]: part A owns group formation, joining, herd travel, spacing and the herd clips (calling at a journey start, sentry, startled). Part B owns leaders, dominance contests, calls, pack hunting and breeding. Part B reads part A's groups and does not change how a herd moves. The herd fight chase stays in part A and is deferred.

**Touches:** `FenceAwareNavigation`, `FenceAwareNodeEvaluator`, `J/entity/ai/behaviour/social/*` (`HerdMoveControl`, `HerdSpacing`, `HerdBodies`, `BodyFit`, `ClearanceBox`, `TravelOrder`, `LeaderTrail`, `JourneyPolicy`, `LeaderJourney`, `ContextSteering`, `FallbackBudget`, `GroundSupport`, `GroupManager`, `DinoGroup`, `Dominance`, `ChallengeGroupLeader`, `JoinGroup`, `LargeCarnivoreFight`), `Dinosaur` (growth, dimensions, turning, mating), `SetMateFromNearbyDinosaurs`, new `NestBlock`, species config.

**Starting point.** Herd steps P1 to P13 are done and reviewed: trail following, context steering, journeys, grazing, spacing, stuck and re-path, clips per state, smoothing, journey pauses. P12c (cliff edges, journey target within 8 blocks of height, new `GroundSupport`) is not built, tested or reviewed. Playtest 6 results are pending. The code enters M0 when the lead commits it (D37) and ports with M0.P2.

**Herd design already decided** [herd spec]. A later decision overrides an earlier one.

- Travel shape: a wedge in open ground, 1 to 3 abreast then single file where narrow, at most 3 abreast [d1, d10].
- Slot spacing scales with body size: personal space + 2 in open ground, + 1 in tight ground (4.2 and 3.2 for the Gallimimus, personal space 2.2). Decision 9's 5 and 4 are superseded. A debug-stick toggle sets tight spacing everywhere. After the 0.80 movement box lands, recompute `w + 0.5` and the fit box [d9].
- Waiting: on a roaming journey the leader waits at most 200 ticks for followers whose slot is on the trail, as a slow walk at 0.6, never a stop. Urgent trips never wait [d2, d21, d30].
- Journeys: every 2 to 5 min, 156 to 314 blocks, in loaded legs of at most 24 blocks, to a target at most 8 blocks above or below [d3, d25, d33].
- Journey start: the leader needs 75 % of its maximum stamina (150 for the Gallimimus). No roaming journey from sunset (day time 12,000) to morning. Walking drains 25 % less stamina [d28, d29].
- Journey pauses: a leader rest pauses a journey, which then continues to the same target. In daytime, for a rest up to 400 ticks, members keep their slots and tired ones lie in them. At night or for a longer rest, members may spread out and lie down. The wedge re-forms when the leader is up. Night pauses a journey until morning [d28].
- Herd area: 6 × the graze-square area (radius × √6). Gallimimus: 14.4 blocks for 5 animals, 27.4 for 20 [d25].
- Grazing: a member keeps a spot for 30 to 90 s, then moves a short way at a 50 % chance. Members avoid neighbours' spots while grazing only. A member outside the area returns [d6, d12, d13].
- Speeds: graze walk 0.6, leader graze move 0.8, journey 0.8, regroup and catch-up 1.2. Ground speed is quadratic in the speed setting through `GroundSpeed`. A follower ramps with the gap to its slot [d15, d19].
- Spacing: hard minimum 2.2 in open ground, 1.7 (`w + 0.5`) in tight ground and beside a resting body [d24].
- Fit box: the larger of hitbox and visible model width and height, times growth scale, margin 0 per side (Gallimimus 1.25 wide). Model length is not in it [d5, d11, d24].
- Hitbox: Gallimimus 1.2 × 2.6. Others unchanged. A member outside the area walks to its spot at 1.2 [d20]. Part A replaces the box with the 0.80 × 2.39 movement box [hitbox design].
- Paths: box-aware nodes. Members pass through mates on fallback paths only. Vanilla pushing keeps bodies apart [d21, d22].
- Blocked by blocks is stuck and asks for a fallback path. Blocked by members is waiting [d14].
- An unplanned walk out of the herd area is an urgent journey. A leader dragged out starts one [d16, d23].
- On a journey, a follower leaves the line to eat or drink only at a critical need. On a roaming journey it rests only at critical stamina. On an urgent trip it rests when tired [d8, d17].
- Clips: every clip of a species plays in a fitting state. Calling at a roaming journey start. Sentry when the leader stands watch. Startled when a threat comes within 6 blocks [d26].
- Smoothing: an idle sway in code for all species. Drop limit 3 blocks. A sitting follower stands still. No idle variants while travelling. Softer easing by default. No slow-down near walls [d27, d30, d33]. A get-up with no clip plays idle in development only. Beta ships each get-up clip (F2.3).
- Logs and leash: one log line per journey end (reason, distance left, length). A leashed leader makes no graze moves. A walk that ends from a rest is not an arrival [d31].
- GameTest time limits follow the real ground speeds, not decision 18's 1,200 and 1,500 ticks.

**Deferred** [lead 2026-10-10]: the leader's fight chase stops being an urgent trip once combat exists [d32]. The leader's water and food detour on a journey [d30].

**Need detours** (D36 answered) [lead 2026-10-13]. A herd member steps off a journey to drink or eat only at the urgent band (below 25 %), then rejoins. This matches herd decision 8. Fight chases stay the other exception to decision 16.

**In scope, part A: movement and herd travel, in this order:**

- [ ] Read 26.2 `PathNavigation.followThePath`, `Path.getEntityPosAtNode` and `WalkNodeEvaluator` from the toolchain sources. The arrival-test defect is a 1.20.1 fact only [hitbox design step 1].
- [ ] A Gallimimus walks through leaves and does not freeze in a canopy. One per-species block filter (pass, break, solid) for every block reader: `Entity.collide` (mixin), `BodyFit`, the node evaluator, the sweep shortcut, IK. Herd and IK code read block shapes directly, never through `noCollision`.
- [ ] A wide dinosaur reaches a node and walks on: an arrival test at the aim point, a box-sweep shortcut and a symmetric footprint.
- [ ] A dinosaur fits any gap that its torso and legs fit. The movement box comes from the render-scaled torso and legs. Growth runs through the `scale` attribute, a full `getDimensions` override and the Fossils growth guard. Split the clearance box by job: torso width for paths, whole model for spacing.
- [ ] In tight ground a dinosaur stops, turns, then walks. In open ground it slows while it turns. A hard turn gate, a soft gate and the anti-orbit guard.
- [ ] A stuck ladder that changes the path. Its last rung snaps to a clear spot, also with a player near.
- [ ] Leaves pass or break per species config. Only the main box breaks leaves, above a growth cut-off. Large species break logs only when charging. With `mobGriefing` off, breakers pass leaves and logs stay solid. Vanilla and decorative fences: D35 (F3.1).
- [ ] Capsule herd spacing with a circle broad phase. The in-line pitch by the chest rule. Tight spacing in corridors and big herds. The trail grows with the single-file column. Animals past the trail tail follow the animal ahead.
- [ ] Corner clearance as a soft path cost. A spine danger term in follower steering.
- [ ] An adult Brachiosaurus (movement box 3.68 × 8.87) stays in open ground. At most one large-species path per herd per tick. Its node volume is 144 cell reads, about 5 ms per path (estimate) [hitbox design].
- [ ] `FallbackBudget` tolerates gaps between asks. Stall thresholds above 20 ticks, or scaled by elapsed time (`FallbackBudget.java:30`) [tech survey n38, d9].
- [ ] Less garbage per herd tick: one roster, primitive returns, one `CollisionContext` per probe batch, an index map for the gather [entity survey n31, B26.2].
- [ ] View cones for separation and cohesion. Cohesion ignores mates behind [entity survey n31, B26.1].
- [ ] Inside a fence, journey targets come from the reachable area, found by a bounded fill of 6,000 blocks. Open ground keeps 156 to 314 blocks. A fenced herd picks targets inside its paddock only (D40 answered) [recon RC43, tech survey T12].
- [ ] When herd tests end, the Gallimimus `group_size` returns from 20 to 6 to 12 [s11.16]. Until the playtests pass, restore the `hasGroup` guard in `SetHunting` [recon RC44].
- [ ] A hungry carnivore group journeys to find prey. Otherwise the leader roams its home area near its spot, and the pack follows in the wedge (D56 answered, reversed) [lead 2026-10-12, recon RC50]. Beta has no home territory (D166 answered) [lead 2026-10-13]. The roam radius is a config value in the task plan.
- [ ] Before each path search, set the node multiplier to `min(1, cap ÷ (max(follow range, 16) × 16))`, cap from species data, start 200. Check that a 24-block leg still finds its path per species. Measure the Brachiosaurus path cost again [JC3 J16, rank 3].
- [ ] A second new search in the same tick after a failure returns no path and sets "throttled". A re-path to the same target stays allowed [JC3 J16, B26.7].
- [ ] A per-search column cache wraps the `BodyFit` probe: path types over the body height once per footprint column, cleared at search start and end. A J13 compare switch checks it [JC3 J16].
- [ ] Step height `max(1, ceil(height ÷ 2) ÷ 2)`: Gallimimus 1.0, Brachiosaurus 2.5 [inf]. A non-full block with a non-pathfindable collision shape in the step zone sets 0 for that move [JC3 J16].
- [ ] Stuck detection [JC3 J17]: every 2 ticks add 2 to a window counter, and 2 to a "trying" counter while a path is unfinished or the target is farther than width + 2. A move of 2 blocks resets both. At 60 counted ticks the animal is stuck if trying ≥ a third of the window. A block by mates is waiting. Stuck starts the first ladder rung.
- [ ] A rung tests 16 directions up to 8 steps for the longest run where the body fits with ground within 2 blocks below, and walks there for 60 ticks if the run is longer than 1 [JC3 J17].

**In scope, part B: social life and breeding:**

- `GroupManager`, `DinoGroup`, `Dominance`, `ChallengeGroupLeader`, `JoinGroup` and `LargeCarnivoreFight` exist in the herd work and port with M0.
- Order [lead 2026-10-10]: part B items that need genes, stats, hunger, thirst or attack wait until the part A herd movement passes. Test on the Gallimimus only.
- Calls to warn, gather or threaten. Each is a sound event (F2.3) and a game event, so F2.6 listeners hear it with no new code [entity survey B18.7].
- A hurt member writes a panic timer of 100 to 199 ticks to members in an 8 × 4 × 8 box, once per event [entity survey n20].
- A startle on a flee species writes the herd alarm, so one startled animal moves the herd [recon RC35]. The alarm reaches herbivores of other species within 8 blocks: flee species run, standing species face the threat. Any herbivore that hears an alarm becomes alert and reacts by its own rule. Graze spots avoid other species. No mixed groups (D63 answered) [recon RC61].
- Dominance [lead 2026-10-06]: from size, age and the aggression gene. It picks the leader and settles contests with `ThreatDisplay` and no damage. The leader chooses where the group goes. Flow [entity survey feature 4, S10]: a challenge starts when a member passes the leader by the margin with its cooldown over. Both face each other and play their own display clip. Higher dominance wins, a tie goes to the leader, the loser gets a cooldown. Dominance comes from saved values, so a reload elects the same leader. Start values: weights from `dominance_weights`, cooldown 1,200 to 2,400 ticks, margin 10 % (estimate) [recon RC3].
- Velociraptor pack hunting [entity survey n20, n22, S9, feature 12]: when the leader's target changes, the manager pushes it once to each member inside follow range, skipping babies, members in a hunt cooldown and members that cannot attack it. Members clear a lost target through `InvalidateAttackTarget`. Slot angles from the index: leader 0°, then ±60°, ±120°, 180°, radius 6. All attack when every member reaches its slot, or after 60 ticks. Slots update on the 20-tick step (estimates).
- T. rex pair hunt: one chases, and the second circles to cut off the target from the far side (180°), with no wait. A Dilophosaurus group hunts together, on the pack hunt rules (D54 answered, reversed for the Dilophosaurus) [lead 2026-10-12, recon RC45].
- Hunt end: a member at 0 stamina leaves, takes its cooldown and rests. The pack hunt ends when the leader's chase ends, the target is lost, or under half the starting members still chase (D55 answered) [recon RC47].
- Feeding order: moved to F2.0 "In scope, feeding together" [lead 2026-10-12]. This package supplies the dominance value that the F2.0 queue reads. Within a group the higher dominance eats first, then the longest wait, at every feeder, carcass and plant source (D57).
- After a herd alarm, the leader may change target once per hunt, to a baby or a member outside its herd area within 8 blocks of the old target [recon RC49].
- A member never starts a pack hunt. Leadership changes only through contests [recon RC53].
- A member skips a slot that it cannot stand on or reach, takes the nearest reachable one, or follows the leader. In a small paddock the pack attacks in a line [recon RC54].
- Stalking: a hungry carnivore with a target beyond 8 blocks walks at 0.5 with its walk clip, or the T. rex with its stalk clip. At 8 blocks it sprints, or a Velociraptor pounces with its `pounce` clip. A flee species treats a stalker as a threat only within 12 blocks (D53, D59 answered) [recon RC55, RC56]. The walk clip at half speed serves until crouch art exists. The Art list holds a crouch-then-leap clip for later (D53).
- Ambush: a hungry Dilophosaurus or Velociraptor with no prey in sight hides in cover by a path for 600 to 1,200 ticks. It picks cover near water or a feeder. Beta has no scent trails [lead 2026-10-12]. Prey within 6 blocks makes it spring out (D60 answered) [recon RC57].
- `JoinGroup` filters the shared nearby-entity memory and runs no entity query of its own [entity survey n20].
- Entity queries use a typed query, so Lithium's fast path works [tech survey OS10].
- No log call in a per-tick or per-entity path. SLF4J with `{}` only [tech survey d7, OS16].
- Breeding: male and female within a group. Adults guard nests and young. Flow [entity survey B18.4]: a saved has-egg flag, a walk to the nest site, the nest build, the clutch, a cooldown for both. Nest eggs use the natural egg texture (F1.9).
- Nest (D152 answered, reversed) [lead 2026-10-13]: no nest model exists. The female builds a nest from vanilla blocks that she takes from her surroundings: leaves in a forest, sand in a desert, gravel by water. A block tag per biome group picks the material. She plays her nesting clip (T. rex, Velociraptor) or an idle clip, then lays `egg_clutch` egg entities in the nest.
  - The nest size follows the egg class. The nest blocks stay after the hatch, and the pair uses the nest again.
  - With `mobGriefing` off, or with no room, she lays the eggs on the ground [entity survey B18.5: a floor block from a tag].
  - A nest egg waits 3 in-game days (72,000 ticks, D247), then shakes 60 s and hatches at `hatch_success`. Lab eggs keep their own rules (F1.9).
- Eggs count as young (D219) [lead 2026-10-13]: parents that see a player near their eggs warn and attack, as for young.
- A nest egg with sniffer DNA hatches 15 % slower on moss [recon RC66].
- Fix the `isMate` UUID comparison and the `nextInt(1,2)` clutch. Give each young its own genome copy: today a young's gender change also changes the mother's.
- Guards search for a baby within 8 × 4 blocks first, then a player at half the follow range. Alerts skip babies [entity survey n21].
- Genes: `fertility` sets the breeding roll, `egg_clutch` the clutch, `gestation_time` the days to laying [recon RC36].
- Breeding gates [lead 2026-10-06, 2026-10-13]: a nest or a free laying spot in range, the fertility roll passed, and both animals under `stress_threshold`. Two more gates (D72 answered, reversed) [lead 2026-10-12]: both animals at full health, and no own young under half grown within 16 blocks. Neglected paddocks stop breeding.
- Breeding cap per area: none (D20 answered). The config value `breeding.cap_per_area` stays at 0.
- Gestation: 24,000 ticks from mating to laying [lead 2026-10-12]. The `gestation_time` gene scales it.
- A bred young is male or female at 50/50 [lead 2026-10-12]. Young do not count toward the group size until they are adult (D246) [lead 2026-10-13].
- Clutch size and nest hatch rate (D79 answered) [lead 2026-10-13]. T. rex: 12 to 24 at 6 %. Velociraptor: 8 to 13 at 25 %. Dilophosaurus: 12 to 24 at 16 %. Gallimimus: 15 to 30 at 15 %. Triceratops: 15 to 25 at 11 %. Brachiosaurus: 15 to 30 at 6 %. The lead raised each drafted rate by × 1.25. The T. rex card gives 5 %, and the lead accepted 6 % [lead 2026-10-13, Z-8]. A failed nest egg cracks open, drops tissue (a DNA source for the Fossil Processor) and vanishes (D255 answered) [lead 2026-10-13].
- Pack collapse [lead 2026-10-12]: a pack that loses its leader flees to safety first. Then the members with the highest and second-highest dominance contest the lead. A pack below its minimum flees big threats. A lone member looks for another pack in range, else it counts as alone.
- Above its maximum, a group splits into two groups, each with its own leader. Crowding gives no stress (D106 answered).
- Heredity [lead 2026-10-06], one pure function with tests: number genes take the parents' midpoint plus the per-clone variation. The midpoint runs when the egg is laid, and the egg stores it, so a syringe can sample it. The per-clone variation rolls at hatch (D71 answered). Colour layers, skin, special gene and morph each come from one parent, plus the normal `editing.morph_chance` roll. The default special gene comes from one parent [recon RC37]. A risk behaviour comes from one parent with a 50 % chance (D112) [lead 2026-10-13]. A Nublar × Sorna Velociraptor young rolls a random parent for each look trait: base skin set, crest and quills (D249) [lead 2026-10-13]. Every bred gene clamps to the edit limits. Gender is 50/50 (a default).
- Mate search after the five gates [JC3 J10]: the female polls every 40 to 79 ticks and rolls 1 in 20. She picks the nearest eligible same-species animal of the other sex within 16 blocks with no target, tests paths for at most 6 candidates, and approaches for at most 300 ticks. Mating starts when both boxes inflated by 3 blocks touch. Use SBL `BreedWithPartner`.
- Breeding Wand and Birthing Wand, creative only, with the creative-only recipe mark [JC3 J10, B18.15]. The Breeding Wand pairs two animals of one species and other sexes, skipping age, health and cooldown, never the species test. The Birthing Wand makes a female lay at once.
- A variance floor test runs heredity on a closed pair line for 10 generations, 50 young each. Each number gene's spread stays above half the founder spread [inf]. If it fails, the lead tunes `editing.variation_percent` [JC3 J10].
- Each young saves its parents' UUIDs [JC3 B18.17].
- Adults join a juvenile's fight within half a second [JC3 J9]: a young animal (≤ 0.5) that gets a target or mob damage writes the game time for its chunk into a per-level event map. An adult (> 0.75, awake, not sitting, no target) checks only when its chunk or a neighbour has a newer event, at most every 10 ticks. It searches 8 × 3 × 8 for a same-species young with a target and copies it, skipping creative players and its owner. With no event it retries after `10 << min(failures, 8)` ticks, capped at 200. Events older than 400 ticks go. Level unload clears the map.

**Out of scope:** turf wars and false eggs (leave `StartTurfWar` and `LargeCarnivoreFight` disconnected with a note, or remove them). The crouch before a pounce, until F2.3 has a crouch clip [entity survey B18.10]. The JC3 pounce math is a reference for it [JC3 B19.19]. A scriptable breeding pipeline [entity survey B18.11]. Every young rolled at mating (D71) [JC3 B18.12]. A cap of 4 within 16 blocks, evidence for D20 only [JC3 B18.14]. Leaves that cost 8 for a desperate animal [JC3 B26.12]. Mutation from sibling breeding.

**Produces:** `BlockFilter` per species, the shared spine definition, the fixed navigation, `GroupManager` (extended), `NestBlock`.

**Art:** see Art list → each species (calling, sentry, startled, nesting (T. rex and Velociraptor), pounce, stalk clips, call, nest, mating and hatchling sounds), Eggs and hatching (natural textures). Not done until those rows are Done (no Placeholder, no To check).

**Config (defaults):** `breeding.cap_per_area` 0, no cap (D20 answered). `breeding.gestation_ticks` 24,000 [lead 2026-10-12]. `breeding.nest_egg_wait_ticks` 72,000 (D247). `breeding.nest_needs_mob_griefing` true, else the eggs lie on the ground (D152) [lead 2026-10-13]. Herd values stay in species config, as the herd spec sets them.

**Acceptance, part A (GameTests on both loaders, in common code):**

- A dinosaur of whole-number width advances through every node.
- A Gallimimus walks under a 3-high canopy.
- A 20-animal herd passes a 2-wide corridor in single file and regroups.
- A growing animal next to a fence never clips into it.
- A stuck animal reaches the snap rung and continues.
- A far member with gaps of up to 20 ticks between asks still gets its fallback path [tech survey n38].
- The server tick stays inside the budget (estimates, profile in the PR): load A, 20 Gallimimus, 2 to 3 ms. Load B, 100 dinosaurs in five herds, 9 to 15 ms plus repath spikes. Herd AI for 20 animals under 1 ms on average, under 5 ms in any tick.
- A fenced Gallimimus herd makes journeys with no NO_PROGRESS end [recon RC43].
- A fed Velociraptor pack starts no journey and roams near its leader. A hungry pack with no prey in range starts a journey (D56) [recon RC50].
- Kill a pack leader: the pack flees to safety, then the two highest-dominance members contest, and one leads.
- 20 Gallimimus in one paddock (maximum 12) form two herds, each with a leader. Their stress does not rise from crowding.
- A Brachiosaurus search visits no more nodes than its cap [JC3 J16]. A boxed-in animal fails at most one new search per tick [JC3 J16]. With the compare switch on, the column cache matches `BodyFit` in the corridor test [JC3 J16].
- A Brachiosaurus climbs a 2-block bank and does not step onto a wall or fence [JC3 J16].
- An animal held for 60 ticks starts the ladder. A member waiting behind a mate does not [JC3 J17].

**Acceptance, part B (both loaders):**

- Six Gallimimus form one herd with one leader, the animal with the highest dominance.
- A contest does no damage, and a reload keeps the same leader.
- Three raptors converge on one prey and take different slots before they attack.
- A T. rex pair builds a nest from local blocks. Its eggs follow the clutch and hatch rates over 20 trials (accelerated time). The eggs hatch after 3 in-game days.
- In a desert the nest is sand. With `mobGriefing` off, the eggs lie on the ground and no block changes.
- A player near the eggs gets a warning, then an attack from a parent that sees the player (D219).
- A Velociraptor pack of 3 adults with 2 young counts as 3 for its group size (D246).
- Unit tests: a young's genome change leaves the mother's unchanged. Heredity midpoint, choice genes from one parent, clamp to edit limits.
- A stressed pair does not breed. A hurt pair does not breed. A pair with its own young under half grown within 16 blocks does not breed (D72).
- A laid egg holds the parents' mix. A syringe sample of the egg reads it before the hatch (D71).
- One startled Gallimimus makes its herd flee [recon RC35].
- A T. rex pair approaches one target from opposite sides [recon RC45]. Three Dilophosaurus in one group attack one prey together (D54).
- A hungry Dilophosaurus waits in cover by a path and springs out when prey comes within 6 blocks (D60).
- An alarm from a Gallimimus makes a near Triceratops alert. The Triceratops faces the threat and does not join the Gallimimus herd (D63).
- A pack stops when fewer than half its members still chase [recon RC47].
- At a carcass or a feeder, the leader eats first. The spot count sets how many eat at once (F2.0 test, linked) [lead 2026-10-12].
- A hungry Velociraptor walks at 0.5 toward a far target and pounces at 8 blocks [recon RC55].
- With the Breeding Wand, two adult T. rex mate within 300 ticks. The wand refuses two species. With the Birthing Wand, a female lays at once [JC3 J10].
- Unit test: the variance floor test passes over 10 generations [JC3 J10].
- A young keeps its parent UUIDs after a reload [JC3 B18.17].
- A mob hits a juvenile Velociraptor. An adult within 8 blocks attacks that mob within 10 ticks. With no event, no adult runs the guard search [JC3 J9].

### F2.6: The player: threat displays, detection, taming

**Old module:** B19 · **Section:** 11.10, 6.2 · **Size:** L · **Depends on:** F2.1, F2.4, F2.5, F2.7

**In game:** a dinosaur notices a player by sight or sound. Beta has no scent trails [lead 2026-10-12]. A sneaking player far away often stays unseen. Before it attacks a player, a dinosaur stops, faces the player and plays its own warning for its clip length (2 to 4 s). A player who retreats avoids the fight. A Gallimimus runs from threats. A Triceratops stands and charges. A Brachiosaurus roars, then whips its tail and stomps. A player near a baby provokes its parents at any distance they can see. A calm animal can be tamed and takes follow, stay and defend commands. A Dilophosaurus spits and blinds.

**Touches:** `ThreatDisplay`, `J/entity/dinosaur/Dinosaur` (taming), new `DetectionService`, `J/entity/dinosaur/ai/sensor/*`.

**In scope:**

- Threat display before an attack. Connect `ThreatDisplay` [entity survey S10, rank 10]:
  - Display and attack run as one track: display clip, one hit tick with the cone test (F2.1), recovery. One trigger per attack, no animation packet per tick.
  - A player target beyond the skip radius starts the display if the cooldown is over. The animal stops, faces the target, starts its clip by a server trigger and plays its own sound. A hunt of a mob (stalk, pounce, ambush, pack hunt) attacks with no display.
  - The display lasts the clip length. If the target leaves the personal space or follow range, it ends with no attack.
  - Inside the skip radius, or after damage, the display is skipped. The cooldown starts at its end, so each fight opens with one display at most.
  - Start values: skip radius 5 blocks (estimate), cooldown 200 to 400 ticks.
  - Warnings: `roar` for Tyrannosaurus, Velociraptor, Triceratops, Brachiosaurus. `calling` for Gallimimus. `spit` for Dilophosaurus (F2.7). Each with the species' own sound. Today all species play the T. rex roar [recon RC38, RC19].
  - The Triceratops plays `ramwindup`, then `ram` [recon RC39].
  - In a pack only the leader warns. Members hold their slots. If the player leaves during the warning, the whole pack drops the target [recon RC51].
- Flee or stand per species from data. The Brachiosaurus stands and defends with tail whip and stomp after a roar warning (D50 answered). The three carnivores stand or retreat by threat (D13 answered) [lead 2026-10-12]: each adds the summed threat of its pair or pack to its own. It stands unless the attacker's threat is more than 2 × that sum (D112).
- Personal space [lead 2026-10-12]: a circle around the animal of `personal_space_radius`, by body size. Start values (D165) [lead 2026-10-13]: T. rex 10, Brachiosaurus 10, Triceratops 8, Dilophosaurus 6, Velociraptor 6, Gallimimus 5 blocks. Playtests tune them. A standing animal warns a noticed player inside it, then attacks. A fleeing species flees at once when a noticed player enters it, with no warning (D218). It replaces `territory_radius` and `Mob.restrictTo` (removed). Beta has no home territory (D166 answered) [lead 2026-10-13]. Eggs count as young (D219).
- Young provoke always [lead 2026-10-12]: a player noticed near a baby provokes its parents at any distance they can see.
- Aggressive mood (D102 answered): notice range × 1.5 and warning cooldown halved. A standing species attacks a player outside its personal space after a warning. A fleeing species still flees.
- A standing animal retreats from an attacker with more than 2 × its threat (D112).
- Lost sight: a predator that loses sight of its target walks to the last seen spot and searches there [lead 2026-10-12].
  - A flee species uses an avoid memory [entity survey S11]: the visible sensor tests the flee list and writes `AVOID_TARGET` for 100 to 200 ticks. The animal walks to a point 16 blocks away, sprints inside 7 (speeds 1.25 and 1.4), and picks a new point on arrival or every 20 ticks.
  - A baby of a standing species retreats from a larger predator within 8 blocks [recon RC59].
- Taming of calm, low-stress animals. The owner is safe. Commands: follow, stay, defend. The bond breaks on hunger or stress.
  - Trust rule [lead 2026-10-12]: trust rises while the player is near a calm animal. Hand-feeding (use its food on it) gives a big boost. Trust rises only while stress is below 20. The lead drops the design doc's zero-stress rule.
  - Owner safety ends above the stress threshold [entity survey B19.10].
  - Commands are one synced value. Each owner use cycles it with an action-bar message. Follow starts at 7 blocks, stops at 2, teleports beyond 18 to a checked spot [entity survey B19.8].
  - Each command packet: the owner UUID equals the sender, the distance is inside follow range, one packet per player per tick [entity survey S12].
  - `PLAYER_REPUTATION`: one capped integer per player with a daily decay and a threshold. No code creates it today [entity survey n25]. The `tamability` gene scales the threshold [recon RC40].
  - In defend mode, the animal copies the owner's last attacker or victim as its target while following. Use the vanilla tamable pattern [JC3 B19.21].
- Detection by sight and sound only. Scent is not in Beta [lead 2026-10-12]. The chance is 25 to 85 % by distance and activity [design doc, s11.10]. Levels [lead 2026-10-12]: sneak 25 %, walk or stand 40 %, sprint, jump or hit 60 %, loud acts (weapon, block breaking, vehicle, noise) 85 %. The detection range stays apart from the personal space. Vision is not movement-based. Build each channel from vanilla pieces [entity survey rank 8]:
  - Sight: a 20-tick sensor over players in follow range. Sneak scales range by 0.8, floor 2 blocks. A species view cone and a cached line of sight [entity survey B19.1].
  - Sound: a `VibrationSystem` listener with a species event tag, a radius and a 40-tick cooldown [entity survey n23].
  - One roll per awareness episode, kept in a memory with an expiry [entity survey n24]. One roll every 200 ticks (10 s) for each player in range (D15 answered), `detection.episode_ticks`.
  - Every "is a player near" radius is 128 blocks or less [tech survey OS11].
- `DetectionService` builds three F2.7 gene effects: camouflage (a still animal is hard to see beyond a range), thermal detection (players through walls within a range), hyper intelligence (+15 % detection).
- One sight-range factor from the vision genes. Low-light vision applies at night only. General vision applies always [recon RC41].
- Readout [lead 2026-10-12, D18 answered]: every player who looks at a dinosaur sees its contentment band and worst need in the hover box (F2.0). Exact numbers stay on the Debug Stick, in creative or for operators (F2.1). Taming reads stress, not contentment [s11.10].
- Roar fear (D19 answered, on): a roar scares passive mobs and villagers near it for a few seconds. Villagers flee loose carnivores as they flee zombies. `vanilla.mobs_fear_dinosaurs` is on by default. Build: a tag, a config value and a tick event, never a goal replacement [entity survey B21.7].
- Four temperaments: moved to F2.0 "Species behaviour profile". F2.6 keeps flee, warning, detection and taming.
- Dilophosaurus spit [JC3 J11]: it needs a target inside 10 blocks and 20 ticks of line of sight in a row. The `spit` clip plays through the pose state. A server event at tick 20 of the 40-tick clip fires a projectile at 1.5 blocks per tick. The aim rises by 0.2 × horizontal distance. A hit does 4 damage and 600 ticks of blindness, plus Poison with the poison gene. The projectile ignores the shooter's species except the target. The next spit waits 40 ticks. A blind target gets a bite. F2.3 draws the splash and the blob.

**Out of scope:** a roar that frightens other mobs [entity survey B19.12]. Taming by stun and calming food [entity survey B19.13]. Movement-based vision [s11.10].

**Produces:** `DetectionService.chanceToDetect(animal, player)`, the personal-space check, the last-seen-spot search.

**Art:** see Art list → each species (warning clips and sounds, `ramwindup`, `ram`, `spit`), Dilophosaurus (spit particle). Not done until those rows are Done (no Placeholder, no To check).

**Config (defaults):** `detection.episode_ticks` 200 (D15 answered). Detection levels 0.25, 0.40, 0.60, 0.85 [lead 2026-10-12]. `taming.trust_max_stress` 20 [lead 2026-10-12]. Species data `personal_space_radius` (start values D165). `vanilla.mobs_fear_dinosaurs` true (D19 answered). No scent keys (D16 moot).

**Acceptance (both loaders):**

- A sneaking player at 30 blocks is detected less often than a sprinting player at 10 blocks (100 samples).
- A tamed raptor follows and defends. In defend mode it attacks the mob that hit its owner [JC3 B19.21].
- A Triceratops charges after `ramwindup`. A Gallimimus flees [recon RC39].
- A dinosaur plays its threat display before the first hit. A player who leaves the personal space during it takes no hit [entity survey S10].
- A player who walks near a baby Triceratops 20 blocks from its parents provokes them, if they can see the player.
- A Brachiosaurus that a player hits roars, then whips its tail. It does not flee.
- A lone Velociraptor retreats from a T. rex. A pack of four Velociraptors stands against it (D13).
- A Gallimimus runs as soon as a noticed player enters its personal space, with no warning (D218).
- A Velociraptor that loses sight of a player walks to the last seen spot and searches there.
- Hand-feeding raises trust more than standing near the animal. At stress 25, trust does not rise.
- A roar near villagers makes them run for a few seconds.
- Unit test: one roll per player per episode [entity survey n24].
- A command packet from a non-owner changes nothing [entity survey S12].
- Each species warns with its own clip and its own sound. A Gallimimus warns with `calling` [recon RC38].
- An animal with wolf DNA reaches the taming threshold sooner [recon RC40].
- A player who leaves during a pack leader's warning takes no hit from any member [recon RC51].
- A Dilophosaurus spits at a player 10 blocks away after 20 ticks of sight. The player is blind for 600 ticks and gets Poison, because every Dilophosaurus carries poison from hatch (D181). A blind target gets a bite [JC3 J11].
- A scared species never fights back. A neutral one does when hurt [JC3 J8].
- A still animal with camouflage is detected less often. An animal with thermal detection finds a player behind a wall within range.

### F2.7: Special genes and quirks, DNA from your own dinosaurs

**Old modules:** B10, B04 (syringe on dinosaurs, remains, egg sample, raw meat) · **Section:** 11.2, 11.6, 11.9, 7.6 · **Size:** L · **Depends on:** F1.3, F1.5, F2.1, F2.4

**In game:** all seven special genes and the extras exist and do something. A command gives a gene whose donor animal is not in the game. A glowing animal causes no chunk rebuilds [tech survey n15]. The player's own animals supply DNA: a syringe on a live dinosaur, remains from a carcass, a sample from an egg, raw meat from a butchered animal.

**Touches:** `J/registry/GeneInit`, new `J/api/gene/special/*`, `J/entity/dinosaur/ai/**`, a command class, the creative tab, `J/item/SyringeItem`, `J/block/entity/ProcessorBlockEntity`, `J/item/RemainsItem`, the lysine item.

**In scope, special genes:**

- Special genes are a choice gene. Slots: 1 at 50 %, 2 at 75 % and 3 at 100 % DNA (D51 answered, design doc):
  - poison: a bite applies Poison. The F2.6 spit reads it.
  - gender swapping: only in a one-gender group, with no animal of the other gender, one animal changes gender over `editing.gender_swap_days` (default 10). The skin stays [design doc "Special Genes", lead 2026-10-12].
  - lysine contingency [lead 2026-10-12]: a full lysine reserve lasts 3 in-game days. When it is empty, the animal falls into a coma and dies 1 in-game day later (config). Lysine-rich food (chicken, eggs) tops up the reserve. Beta has no Lysine Pills item (D170 answered).
  - camouflage: players cannot easily see the animal beyond a range while it stands still. A data list names compatible species. An ambusher with camouflage waits as long as one without it (D112 answered) [lead 2026-10-13].
  - bioluminescence: an emissive layer only, never a moving light source. Dynamic-light mods rebuild up to 8 sections per 0.1-block move [tech survey n15].
  - hyper intelligence: opens doors, takes items from chests, detects unpowered fences (the `FENCE_IS_LIVE` memory of F3.1), +15 % detection. After it notices an unpowered fence, it walks to the dead section and climbs out over the wire (F26). This is a lead-approved exception to fence parity (D46 answered). Every Velociraptor carries it from hatch (D43 answered).
  - thermal detection: detects players through walls within a range. A data list names compatible species [recon RC18].
- This package registers camouflage, thermal detection and hyper intelligence. F2.6 builds their detection effects.
- Compatible species (D42 answered): thermal detection and camouflage for T. rex, Velociraptor and Dilophosaurus, bioluminescence for all six [lead 2026-10-12]. Every Dilophosaurus carries poison from hatch (D181 answered). One glow mask per species serves all its skin sets.
- Stress Tolerance (the design doc's "Lower Threshold Stress", renamed by the lead): a basic number gene, not a special gene. It raises `stress_threshold`. Calm donors give it, for example cat and sheep [lead 2026-10-12].
- The Dilophosaurus warning plays its intimidate clip, converted from the asset repo. If the player stays, it spits with `spit` [lead 2026-10-12]. This replaces the shared `spit` clip of recon RC19.
- Donors: poison (cave spider, bee), gender swapping (vanilla frog), bioluminescence (glow squid), hyper intelligence (camel, parrot, wolf, fox, mule), thermal detection (salmon, squid, glow squid, frog).
- Cuttlefish, reed frog, tree frog and pit viper do not exist. `/projectnublar gene grant <player> <gene>` and a creative-tab item give their genes.
- Diet genes: grass eating and mushroom eating add blocks to the diet. Sniffer DNA adds torchflower seeds to the herbivore diet with +25 % regen while eating them [recon RC66].
- Quirks: clucking, sound imitation, item carrying and llama spit. Each quirk plays at a random moment every 2 to 5 minutes (config). A parent that has the quirk passes it with a 50 % chance [lead 2026-10-12].
- Llama quirks (D112 answered) [lead 2026-10-13, Z-8]:
  - Item carrying: a tame animal carries a few items for its owner, like a llama with a chest.
  - Spit: a small knockback with no damage.
  - The cocoa allergy quirk is not in Beta.
- Donor palette shifts move layer colours by a data-defined hue shift.
- The immunity gene reduces Poison duration.

**In scope, DNA from own dinosaurs:**

- Syringe on a live dinosaur: yield range `sequencer.dino_syringe_min/max` (25 to 50 %, D04 answered), a small damage hit, no cooldown.
- Remains (fresh bone, tissue) drop from carcasses (F2.1). The Processor processes them at `processor.remains_yield` (50 %, D04 answered).
- Egg sampling: a syringe on an unincubated or incubated egg gives DNA of the egg's genome. It does not harm the egg (D03 answered).
- Raw meat from a carcass carries a genome component with the species and the `DinoData`. Cooked meat, and meat from a burning carcass, carries the species only [JC3 J5]. The Processor accepts raw meat as a `REMAINS` source, yield under D04 [JC3 B04.1].

**Out of scope:** hybrids, disease. The scent gene (RC9, D48): moot, because Beta has no scent. Coprolites as a DNA source (section 11 is silent). Sea lamprey ore [JC3 B04.2].

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `editing.gender_swap_days` | 10 | [design doc] |
| `sequencer.dino_syringe_min/max` | 25, 50 % | D04 answered |
| `processor.remains_yield` | 50 % | D04 answered |
| `lysine.reserve_days`, `lysine.coma_days` | 3, 1 | [lead 2026-10-12] |
| `quirks.interval_ticks_min/max` | 2,400, 6,000 | [lead 2026-10-12] |

**Produces:** the seven special genes, the grant command, tubes from live syringes, remains, egg samples and raw meat with `DNAData.source` set.

**Recipe rows:** none. Beta has no Lysine Pills item (D170 answered).

**Art:** see Art list → each species (bioluminescence glow masks, quirk sounds where they are the species' own), Lab items (gene-grant item). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance (both loaders):**

- Each special gene has a GameTest or a scripted manual check in the PR. F2.6 tests the detection effects.
- The command grants a gene that a survival player cannot otherwise get.
- A bioluminescent animal that walks in view causes no chunk-section rebuilds [tech survey n15].
- Each living source makes DNA that the Sequencer accepts.
- Unit tests on the yield ranges.
- A tube from the raw meat of an edited animal carries its genome. A tube from cooked meat carries the species only [JC3 J5].

---

## M3. Park

**Goal:** the player keeps animals behind powered fences that play like the original 1.12.2 fence, runs a power grid with pylons, and feeds animals from outside the paddock.

**Exit gate (playtest):** on each loader, a tester builds three fenced paddocks (one low security, two high security) on a Large Generator with pylons. The tester stocks a Small Carnivore Feeder, a Low Herbivore Feeder and a Very Tall Herbivore Feeder by hopper from outside. The tester also loads a Live-Bait Feeder with a led animal. A T. rex, a Velociraptor pack, a Triceratops herd and a Brachiosaurus live there for three in-game days. The T. rex breaks out only when a section is cut. Every fence parity test passes.

**Order inside M3:** F3.2, F3.1, F3.3, F3.4.

### F3.1: Fences

**Old module:** B15 · **Section:** 11.11, 2.3, 5.1, 5.2 · **Size:** XL · **Depends on:** M2, F3.2

**In game:** the fence plays like the original 1.12.2 fence on both loaders. Strings of 6 or 8 wires follow any slope between posts. A player breaks one wire at a time, and its cut ends hang bent and spark. A live wire shocks players and animals through armor for ½ ♥ (1 HP). Lamps light post by post as power spreads. A Tyrannosaurus tears through an unpowered fence, and any mob can climb an unpowered wire. The Beta design adds behaviour the original did not have. A switch or a redstone signal cuts a section. A paddock of hundreds of posts draws like ordinary blocks. A live fence hums, a shock zaps, and a wire snaps when a Tyrannosaurus breaks it (D183).

**Touches:** `J/block/api/fence/*` (rft), `J/block/ElectricFencePostBlock`, `J/block/ElectricFenceBlock`, `J/block/api/BlockConnectableBase`, `J/block/api/Connection`, `J/block/api/MathUtils`, `client/renderer/ElectricWireRenderer`, `ElectricFenceRenderer`, the wire spool (becomes a block item), new Fence Remover and Creative Fence Remover, a `regenfence` subcommand, damage type `projectnublar:electric_fence`, a new switch block, fence sound events, `J/entity/dinosaur/ai/behavior/BreakFenceBehaviour`.

#### 1.12.2 parity baseline

**Rule** [lead 2026-10-10]: the fence is functionally 1:1 with the original 1.12.2 fence. Each row is in scope and has a test. Where an earlier Beta decision changed the original, the original wins. Additions that change no original behaviour stay. Do not copy the original bugs below.

**Sources:** 1.12.2 code at `Dumb-Code/ProjectNublar` `master` `e525ed7`, the wiki article "Fences", the 2019 fence demo video. Evidence: `research_notes/Fence parity 1.12.2/`. "ai" is today's branch, "rft" is `rft/adversing`.

| # | Original 1.12.2 behaviour (in game) | Rewrite today | Target |
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
| F18 | Right-click a wire with an empty hand or a non-spool item. A "1000 VOLT" sign hangs on that wire, tilted with the incline. A second click removes it. The sign saves with the world | Flag saves. Nothing renders it | Render it. The three voltage-sign arts from the asset repository are the sign looks. Each next empty-hand click on the wire shows the next look: 1, 2, 3, then 1 again. Sneak + click removes the sign. Empty-hand clicks on a post still flip the post (F7). No sign item (D78 gap 1, D216) |
| F19 | Break the first end post: its wires stay, unpowered, with bent ends. Break the second: the wires go, with no drops | Works. rft also drops spools for wire left with no post | Keep. rft's spool drops may stay (an improvement, no gameplay loss) |
| F20 | Fence Remover: right-click a wire. The whole line of that wire goes. Posts stay | Missing | Add |
| F21 | Creative Fence Remover: right-click a wire. The whole connected fence network goes, posts included | Missing | Add, creative tab only |
| F22 | The Wire Spool sprite shows how much of the stack the pending span uses, in 12 stages ("Item Animation" in the video) | Missing | Add with 10 stages from the 10 asset-repo sprites. A lead-approved deviation for art reasons (D184) |
| F23 | A post stores power and passes it to the posts at the other end of its wires, so one source powers a whole connected fence. A wire is live only if every block on its run is intact and one end post holds power | Same rule on both lines | Keep. F3.2's network object carries it (tech survey, spec T1), with the F30 numbers and the F31 spread |
| F24 | A powered post shows a lit lamp model on its top block and gives light 10 (low) or 15 (high). An unpowered post is dark | No lit model. No light (`getLightBlock` bug) | Fix both. Lamp model from the post art |
| F25 | Touching a live wire: ½ ♥ (1 HP) of electric damage that ignores armor. The entity flies back from the wire (velocity set, ×0.4 in the air, vertical ×0.3). 15 spark particles burst at the contact point. Dinosaurs take it too | Damage and knockback work. The type is THORNS, which is not electric. No sparks | Add the damage type `projectnublar:electric_fence` with the armor-bypass tag and a death message. Add the sparks |
| F26 | An unpowered wire acts as a ladder for every living entity: players, mobs and dinosaurs. A powered wire does not | Code in comments (ai), absent (rft) | Restore for every living entity (D38, answered 2026-10-10) |
| F27 | Mobs path around wires and posts. Wire blocks and posts are always blocked nodes | Path obstacle, but a dinosaur with every need at zero gets open nodes and walks up to them | Always blocked. Remove the open-node exception. Only the F29 Tyrannosaurus walks to a fence. Lead-approved exception (D46, 2026-10-12): a hyper-intelligent animal walks to a dead section and climbs out |
| F28 | `/projectnublar regenfence` (operator level 2) rebuilds the wire caches | Missing | Add, if the rewrite keeps client wire caches. Otherwise say so in the PR |
| F29 | Only a Tyrannosaurus breaks fences. Each check rolls 50 %. It looks within 10 blocks for a wire block with an unpowered wire, walks to it at its speed + 0.1, and within 1 block breaks it once. The middle block column of each run goes up to 6 blocks high, and the cells on each side up to 4 high, with drops. It ignores live wires | ai: `BreakFenceBehaviour` for a starving carnivore that wants prey outside the enclosure. rft: marks segments broken, then the cascade | Restore the original rule and numbers. Remove the starvation trigger |
| F30 | A post holds 350 FE and accepts 350 FE per transfer. It loses 10 FE per tick. A post with more than 300 FE shares up to 300 FE per tick with the posts at the far end of its runs, emptiest first. A post with no supply goes dark after 35 ticks (1.75 s) | Same numbers on both lines | Keep these numbers. F1.11 and F3.2 size the generator output to them |
| F31 | Power spreads post by post, so lamps light along a fence over several seconds (demo 0:09 to 0:17) | Same sharing code, no light | Keep. The network object stores energy per post and moves at most 300 FE per post per tick along the runs, so the spread stays visible (D39, answered 2026-10-10) |
| F32 | Breaking a wire or a post is instant (no hardness) | Not checked | Keep instant breaking |

**Decisions superseded by the parity rule** [lead 2026-10-10]:

| Earlier decision | Superseded by |
| --- | --- |
| High security hurts more than low security (section 11.11) | F25: ½ ♥ (1 HP) for both posts |
| Each wire has health, and an animal hit removes its attack damage (section 11.11) | F10, F29: a player breaks a wire at once, and only a Tyrannosaurus breaks a fence |
| Every animal tests an unpowered fence, and desperate animals break even a live fence (section 11.11) | F29: only a Tyrannosaurus, only unpowered wires. Other animals can climb an unpowered wire (F26) |
| A large animal can topple a post (section 11.11) | No post falls |
| A shock can deter a stressed animal from its escape (lead, 2026-10-06) | Removed. A shock does damage and knockback only |
| A starving animal attacks fences [lead 2026-10-06] (F2.4) | Removed. Starvation does damage only |
| Decision D38 default (players only) and D39 default (lamps at once) | F26 and F31 |

**Additions to parity.** Outside these additions, the fence stays 1:1 with the original. Each addition changes no original behaviour, or the lead approved it:

- Survival recipes. The 1.12.2 posts, spool and removers came from the creative tab only. F3.4 keeps survival recipes for posts, the Wire Spool and the Fence Remover, because [s11.8] asks for a recipe for every item. The Creative Fence Remover stays creative only. The lead can remove the recipes on request.
- The switch and the redstone cut [s11.11].
- Fence sounds: a hum loop, a shock zap and a wire snap (D183) [lead 2026-10-12]. A power-up hum and a power-down click (D238) [lead 2026-10-13].
- The hyper-intelligent climb-out at a dead section (D46), an exception to F27.
- The spool shows 10 stages, not 12 (D184), a deviation from F22 for art reasons [lead 2026-10-12].
- A sneak-click removes a voltage sign. The original removed it with a second click (D216) [lead 2026-10-12].

**Original bugs not copied:** spark spam from operator precedence (`pb || nb && rand < 0.02`), solid boxes on broken wires, the yaw computed again on every block-state query, and the global `fullBoundingBoxLadders` override (make the ladder rule per block).

**In scope:**

- Keep rft's connection dedupe, cascade breaks and renderer. F3.2's `PowerNetwork` replaces rft's `FencePowerService`. Port rft's Forge-only model data mixins to the platform layer.
- Shock damage is `fences.damage`, 1 HP for both posts [F25]. Shocks draw no energy (D23) [tech survey O9].
- Posts of 3 and 8 blocks exist. Jump checks use the live height.
- Animals and the wire: a live wire hurts players and animals [F25]. A Tyrannosaurus breaks unpowered fences by [F29], built as an SBL behaviour on the library's base classes. Any living entity climbs an unpowered wire [F26]. Delete `allNeedsAtZero` from `FenceAwareNodeEvaluator` and the starving-carnivore trigger from `BreakFenceBehaviour` [recon RC22]. Hyper-intelligent animals read the `FENCE_IS_LIVE` memory (F2.7). When it is false, they walk to the dead section and climb out over the wire [F26]. This is the one lead-approved exception to F27 (D46 answered).
- Switch block and redstone: a redstone signal cuts power to a section. The switch looks like a vanilla lever on a metal post plate (D186 answered). The switch has a hand toggle, redstone control and an inverted mode. It flips the network, never each post [tech survey B15.13]. A section switch changes one block state per post, and vanilla bundles the updates. No custom packet [tech survey ON5].
- Three voltage-sign looks from the asset art hang on wires. Empty-hand clicks on the wire cycle them 1, 2, 3, 1. Sneak + click removes the sign [F18, D78 gap 1, D216].
- Vanilla fences and walls hold dinosaurs with normal collision. Dinosaurs cannot break them (D35 answered) [lead 2026-10-12].
- A live fence stops a vehicle like a wall and shocks the driver once (D26 answered) [lead 2026-10-12]. F4.2 builds the vehicle side.
- Feeders can be filled from outside the paddock. A feeder in a post line keeps the wire run intact (with F3.3).
- Remove the forced `|| true` and the 171 commented-out lines in `BlockConnectableBase`. Remove the all-players chat message in `BreakFenceBehaviour`.
- Fence geometry in tiers [tech survey B15.7, T4, rank 1]:
  - Posts, insulators and signs draw from block models in blockstate JSON.
  - Each wire block draws only its own segment from an immutable `FenceVisual` record (connections, tier, broken flags, signs). Shared geometry code lives in `common`. NeoForge reads it from model data. Fabric reads it from `RenderDataBlockEntity` in `emitQuads`.
  - Model data changes only for slow state. A powered tint changes at most once per 20 ticks.
  - Sparks, arc flashes and a snapping wire are particles or a short effect while active.
  - Draw each wire once, from the lower end, with 4 to 6 segments for a short span [tech survey OC7].
  - Today every fence block uses `ENTITYBLOCK_ANIMATED`, so 300 posts submit 300 or more renders per frame [tech survey d14].
- A remaining block-entity renderer returns a box over its span and never sets `shouldRenderOffScreen`, so a wire stays visible while its span is on screen [tech survey d13, T4].
- Wire events: one event of about 20 bytes per connect, break, repair or sign change. One batch when a player starts to watch a chunk [tech survey B15.9, T2].
- One fence config block holds span and damage [tech survey B15.11].
- The spool names the failure: too long, already joined, no free connection, blocked [tech survey B15.12].
- A powered post top glows. Set light through `lightLevel` from the state. Today the post overrides `getLightBlock` and casts a shadow (`ElectricFencePostBlock.java:275-276`) [tech survey d1, B15.14]. Emissive lamp parts on the powered variant: `SecurityLightBulb`, `PoleLight1`, `PoleLight2` (high), `PoleLight` (low) [orig 1.16].
- The wire draws full-bright only while powered [orig 1.16].
- Fence sounds (D183) [lead 2026-10-12]: a hum loop near a live fence and a zap on each shock. A wire snap plays when a Tyrannosaurus breaks a wire [F29]. A power-up hum plays when a fence network gets power, and a power-down click when it loses power (D238) [lead 2026-10-13]. The task plan limits how many hum sources play at once. Each event has its own file.
- Remove `NORMAL_CACHE`, which keeps one entry per triangle and never clears (`MathUtils.java:121,133`) [tech survey d4].
- Remove the empty `ElectricFenceBlock.neighborChanged` override [tech survey d6, B15.15].
- Build wire collision boxes once per block entity and rebuild them on a connection change. The shock reads the network's cached powered flag. Today contact costs about 1,000 allocations per tick with 10 dinosaurs [entity survey B15.1, S6].
- The Tyrannosaurus fence scan runs only on its [F29] check. Today it reads 9,261 blocks per scan, always [entity survey B15.2].
- Wire renderers walk connections with no stream. Today a 60-post paddock allocates about 1,800 objects per frame [entity survey B15.4].
- Player guide only: escape alarms from vanilla sculk sensors, since dinosaurs emit step events [entity survey B15.5].
- Spark particles at cut ends and on contact use a Nublar spark particle with the original numbers: 8 at a break point, 30 on a shock, lifetime a quarter of the default, scale 0.25, gravity 0.75, lit [orig 1.16].
- A fence line that grows block by block causes one power rebuild per tick per network, not one per block [JC3 B15.22, tech survey T1]. JC3 floods its whole power net for every placed block, the negative precedent.

**Out of scope:** gates and the overhanging fence. Decorative fences (D78 gap 2 answered: none in Beta). The film's four fence kinds and wire spacings [design doc] (parity picks two heights, D185). Shock that follows the grid (T3, only after D23 changes). Forced chunk loading (D21). Reinforced blocks, a laser tripwire alarm, iron-tier block breaking [tech survey B15.18, B15.20, B15.21, entity survey B15.6].

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `fences.damage` | 1 HP, both posts | [F25] |
| `fences.max_span` | 15 blocks | [F4] |

**Produces:** the `FenceVisual` record, the `FENCE_IS_LIVE` memory for F2.7.

**Recipe rows:** low-security post, high-security post, Wire Spool (iron and copper, 4 per craft), Fence Remover, fence switch. The voltage sign has no item (gap 1 of D78). F3.4 adds the rows from the approved recipe table (D78, D202). The Creative Fence Remover stays creative only.

**Art:** see Art list → Fences (posts, lamp states, spool stages, removers, voltage signs, switch, sparks, sounds). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance (both loaders):**

- GameTest: a Tyrannosaurus breaks an unpowered fence within 10 blocks and ignores a live one. A Gallimimus never breaks a fence [F29].
- A redstone signal cuts a section, and a Tyrannosaurus near it breaks it.
- A dedicated server runs the paddock with no client class errors.
- A wire stays visible while its span is on screen and both posts are off screen [tech survey d13].
- A still paddock of 60 posts submits no block-entity renders per frame [tech survey T4].
- A player who walks up to a paddock sees every wire and every broken wire [tech survey T2].
- `grep -r "allNeedsAtZero" common/src` returns nothing. A starving Velociraptor with prey outside does not walk to the fence [recon RC22].
- A GameTest places 20 posts in one tick, and the manager counts one rebuild [JC3 B15.22].
- Parity tests:
  - Two Low Security posts 15 blocks apart on a slope get 6 wires and cost 3 spools. Two High Security posts get 8 wires and cost 6 [F1, F5].
  - At 16 blocks the player sees the too-long message. With too few spools, the not-enough message, and nothing is built [F4, F5].
  - A Low Security post does not link to a High Security post [F3].
  - An empty-hand right-click flips a post 180°. A post with two runs faces their bisector [F7].
  - Only the hovered wire shows an outline. A left-click removes only that wire [F9, F10].
  - Cut ends hang bent on both clients. A cut end on a powered run sparks [F11, F12].
  - Each of the four repair routes restores a wire and uses 1 spool with the place sound [F13 to F17].
  - An empty-hand click on a wire hangs a voltage sign. The next clicks show the second and third looks, then the first look again. A sneak-click removes the sign. An empty-hand click on a post flips the post. The sign stays after a reload [F18, D78 gap 1, D216].
  - A player who walks up to a live fence hears its hum. A shock plays the zap. A Tyrannosaurus that breaks a wire plays the snap (D183). Power on plays the power-up hum, and power off the power-down click (D238).
  - After the first end post breaks, its wires hang unpowered. After the second, they are gone [F19].
  - The Fence Remover removes one wire line and keeps the posts. The Creative Fence Remover removes the whole network [F20, F21].
  - The spool sprite steps through its 10 stages as the pending span grows [F22, D184].
  - A fence fed at one post powers the whole connected fence. One broken wire block makes that wire dead [F23].
  - A powered Low Security post shows its lit lamp and gives light 10. High Security gives 15. Unpowered is dark [F24].
  - A player in full diamond armor loses ½ ♥ at a live wire, flies back and sees sparks. The death message names the fence [F25].
  - A player and a Velociraptor climb an unpowered wire, and neither climbs a powered one [F26].
  - A post with no supply goes dark after 35 ticks. A fence fed at one post lights post by post [F30, F31].
  - Low and high security wires each do ½ ♥ [F25].
  - `/projectnublar regenfence` runs at operator level 2, or the PR says why it is not needed [F28].

### F3.2: Power grid

**Old module:** B14 (pylons, Large Generator, fence power) · **Section:** 11.11, 7.7 · **Size:** M · **Depends on:** M2

**In game:** a Large Generator burns fuel longer with more output. Its own screen shows the fuel slots, the flame, the energy bar and the line status. Its green and red lights blink while it runs. The player puts a pylon head on top of a fence pole and sets the height of the pole. The player links pylons to each other and to fence posts with the Wire Spool, up to 32 blocks apart. A pylon cable follows any slope, like a fence wire. A pylon also feeds the nearest post within 4 blocks, and it hums. Lines carry power with no loss. An overload trips the generator. Fences draw power post by post. When fuel ends, the fence runs on its post buffers, and the lamps go dark post by post within seconds (D22) [F30]. While a generator's chunk is not loaded, its fence keeps its last power state (D21).

**Touches:** `J/block/entity/GeneratorBlockEntity` (large), the Large Generator screen, new `J/block/PylonBlock`, the Wire Spool pylon link, `J/block/entity/BlockEntityElectricFencePole` draw path, `J/block/ElectricFencePostBlock` (ticker), the F1.11 `PowerNetwork`.

**In scope:**

- Large Generator (D188 answered) [lead 2026-10-13]: fuel lasts `power.large_fuel_multiplier` (2) times longer. Its output is `power.large_output_multiplier` (4) times the Basic Generator output: 800 FE per tick. Buffer 80,000 FE (5 s). It runs 80 posts alone, or about 48 beside a fully upgraded lab. Playtests tune them.
- Large Generator screen of its own (D187) [lead 2026-10-12]: fuel slots, a burning flame, an energy bar and the line status. The task plan defines the line status.
- Large Generator lights (D187): the green and red light textures are emissive and blink while it runs.
- Pylon design (D240) [lead 2026-10-13]: the pylon head sits on top of the fence pole block. One pole block serves as a fence pole or a pylon pole. The task plan checks the REPO pylon head model (`pylon_pole_head.tbl`) on the fence post. Pole height (D253 answered) [lead 2026-10-13]: the player sets any height only for a pole used as a pylon. A fence post keeps the parity heights, 3 blocks (low) and 8 blocks (high).
- Pylons (D189) [lead 2026-10-12]:
  - The player links pylon to pylon and pylon to post with the Wire Spool, up to `power.pylon_range` (32 blocks). One cable costs ceil(length ÷ 20) spools. The pole height does not count (D241) [lead 2026-10-13].
  - A pylon cable behaves like a fence wire: any direction, any slope (D240).
  - Lines carry power with no loss. The line between two pylons is drawn in code.
  - A pylon also feeds the nearest post within `power.pylon_feed_range` (4 blocks), with no link.
  - A hum loop plays near a pylon.
- Fence posts and pylons join the F1.11 power network. Fence draw per post goes through it with the [F30] numbers. Check that only one block of a 3-block or 8-block post drains 10 FE per tick. Else a high post drains up to 80 (power draft check E1).
- Fence posts and pylons run no tick. 300 idle posts cost 0 ticks [tech survey OS1, B14.2]. Today every post ticks on both sides every tick, because `getTicker` always returns the ticker (`ElectricFencePostBlock.java:300-305`) [tech survey d2].
- After fuel ends, the generator runs on its buffer for about 5 s (D245). Then each post runs on its own 350 FE. The lamps fade post by post over about 2 s (D22 answered) [tech survey O8, T1, lead 2026-10-13].
- Feed points (D244) [lead 2026-10-13]: keep the parity share. One feed point carries about 31 posts on a closed loop (300 FE per tick share, about 15 posts each way). A large fence needs more feed points: pylons or generators.
- Overload (D242): F1.11 trips the generators of a network whose demand is more than its supply. Posts then fade as after fuel ends.
- Grid memory (D21 answered) [lead 2026-10-12]: while a generator's chunk is not loaded, its fence keeps its last power state. When the chunk loads, the generator burns fuel for the time that passed. No chunk tickets and no forced chunk loading. F1.11 builds the generator side.
- No comparator output and no power meter block (D27 answered).
- Balance check: one Basic Generator runs the four machines plus 10 low-security posts (180 of 200 FE per tick). A second paddock trips it. One Large Generator runs 80 posts alone, or 72 beside a base lab (D188).
- Generator sounds for the Large Generator: running loop and out of fuel.

**Recipe rows:** Large Generator. Pylon head. F3.4 adds them from the approved recipe table (D78, D202).

**Out of scope:** backup generators [s11.11]. A battery block, a power meter block and comparator output (D27). Forced chunk loading (D21).

**Config (defaults):**

| Key | Default | Basis |
| --- | --- | --- |
| `power.large_fuel_multiplier` | 2 | D188 answered |
| `power.large_output_multiplier` | 4 | D188 answered |
| `power.large_buffer_fe` | 80,000 (5 s of output) | D245 |
| `power.pylon_cable_blocks_per_spool` | 20 | D241 |
| `power.pylon_range` | 32 blocks | D189 |
| `power.pylon_feed_range` | 4 blocks | D189 |
| `fences.post_drain_fe_per_tick` | 10 | [F30] |
| `fences.post_capacity_fe` | 350 | [F30] |
| `fences.post_share_fe_per_tick` | 300 | [F30, F31] |

**Produces:** `GeneratorBlockEntity` (large), `PylonBlock`, fence membership in `PowerNetwork`.

**Art:** see Art list → Power (Large Generator, pylons, power lines, item sprites, sounds). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance (both loaders):**

- GameTest: one Basic Generator with coal keeps four base-tier machines and a 10-post fence powered. With a second paddock added, it trips, and every lamp fades (D242).
- A pylon head on a fence pole links to a pylon 20 blocks away for 1 spool (D241). A link of 32 blocks costs 2 spools.
- One feed point on a 31-post loop lights every post. A 40-post loop with one feed point leaves its far posts dark (D244).
- A chunk in the middle of a 100-post line unloads and loads. The powered flag then matches the supply [tech survey B14.15].
- 60 idle posts run no tick method [tech survey OS1].
- After fuel ends, the lamps stay lit for about 5 s, then go dark post by post over about 2 s (D245).
- The generator chunk of a lit paddock unloads for 10 in-game minutes with 5 minutes of fuel left. The lamps stay lit. After the chunk loads, the fuel is gone, and the lamps go dark post by post (D21).
- Two pylons 32 blocks apart link with a spool and carry full power. At 33 blocks the link fails (D189).
- A pylon within 4 blocks of a post feeds the nearest post with no link.
- The Large Generator screen shows the fuel slots, the flame, the energy bar and the line status. Its lights blink only while it runs.
- No generator gives a comparator signal (D27).
- A line draws between two connected pylons on both clients and stays visible while its span is on screen.
- Each generator state plays its own sound event.
- Each recipe row crafts in survival.

### F3.3: Feeders

**Old module:** B20 · **Section:** 11.12, 5.3 · **Size:** L · **Depends on:** F3.1

**In game:** seven feeders work. They are the Small and Large Carnivore Feeders, the Low, Medium, Tall and Very Tall Herbivore Feeders, and the Live-Bait Feeder. Each is an in-ground hatch. Only the hatch is a block, and the renderer draws the shaft and the rising food. On the feeding timer, the doors slide apart and the food rises to the height of the feeder. An empty feeder closes. A hopper or a pipe keeps a feeder stocked from outside the paddock. The gauge stores the food, and the slots hold the overflow. A player leads a cow to the Live-Bait Feeder and right-clicks, and the feeder keeps the cow in its pen. At the set interval, the cow comes out on a chain, and a hungry T. rex hunts it.

**Touches:** `J/block/DinosaurFeederBlock`, `J/block/entity/DinosaurFeederBlockEntity`, `EatFromMeatFeeder`, `WalkToNearestFeeder`, `NearestFeederSensor`, new feeder classes, the live-bait pen and chain, feeder screens, the feeder renderer (shaft and lift), `MOD/animations/block/*feeder*`.

**In scope, which feeder is which** [lead 2026-10-12, D86, D190, D191, D197]:

| Model | Feeder | Note |
| --- | --- | --- |
| Feeder 1 | Small Carnivore Feeder | 3 × 3 hatch. The meat platform rises 14 px |
| Feeder 2 | Large Carnivore Feeder | 5.25 × 4.88 hatch. More feeding spots. Same behaviour as Feeder 1 |
| Feeder 5 | Low Herbivore Feeder | Triceratops, Gallimimus |
| Feeder 3 | Medium Herbivore Feeder | The potted tree rises 55 px (about 3.4 blocks). Any herbivore that reaches the food eats there (D217) |
| Feeder 4 | Tall Herbivore Feeder | Any herbivore that reaches the food eats there (D217) |
| Feeder 6 | Very Tall Herbivore Feeder | Brachiosaurus. The head-contact rule of F2.4 applies at its lift height |
| Feeder 1 with a pole | Live-Bait Feeder | New art (Art list). Code draws the chain (D235) |
| Fish Feeder | not in Beta | |

**In scope:**

- Fix the current carnivore feeder (Feeder 1): the slot switch has `case 3` and no `case 2`, so the third slot is skipped (`DinosaurFeederBlockEntity.java:44-46,138-140`) [tech survey d20]. Fix item stack aliasing, save the contents, make hunger rise on eating, fix the map key type, the range mismatch (eat range = walk stop distance + half the species width [entity survey n26]), the sensor cache expiry, and rename the id.
- Each feeder is a vanilla `Container` with item capabilities on both loaders. Sided handlers let hoppers, Create chutes and Refined Storage exporters fill it with no Nublar code [entity survey B20.1, tech survey B20.13, rank 11].
- In-ground hatch (D190, D192) [lead 2026-10-12]: y = 0 of each model is the ground. The block is the hatch only. Placement needs no digging and clears no block. The renderer draws the shaft and the lift below and above the block, with no blocks. The render box covers the lifted food, so a tall lift stays visible while the feeder is on screen. Feeding spots and head contact use the lifted food position.
- Gauge (D195) [lead 2026-10-12]: the gauge stores the food as points, 0 to 100 %. An input converts into points until the gauge is full. The 3 × 3 slots hold the inputs above 100 % as an overflow buffer. The slots refill the gauge as it drains [inf]. One feeder serves many dinosaurs, and each bite takes points [entity survey n26].
- Shared feeders [lead 2026-10-12]: every feeder serves every species of its diet. Each feeder writes its open edges to the F2.4 registry on load and on a neighbour block change. An edge against a wall, a fence or deep water has no spots. The F2.0 spot model sets how many eat at once from the edge length and the eaters' body widths. The others wait in the F2.0 queue: within a group the higher dominance first, then the longest wait (D57). Values: D111. Read each hatch edge from the model in the task plan. Feeder 1 is about 3 × 3 blocks, and Feeder 2 about 5.25 × 4.88.
- The block state holds a fill level 0 to 4 from the gauge, written on change [entity survey n26]. It drives the look only. No comparator output (D27, D28 answered).
- Food tags [entity survey B20.6]. Carnivore feeders accept the carnivore diet tag. Herbivore feeders accept four tags (D196) [lead 2026-10-12]:
  - leaves and saplings
  - crops and produce: wheat, carrots, potatoes, beetroot, melon, pumpkin, apples, berries
  - grass and ferns, also seagrass and kelp
  - hay bales. One bale gives the points of 9 wheat.
- The eater stores the feeder position and checks at use that the feeder still exists [entity survey B20.3].
- Each feeder registers in the F2.4 feeder registry on load and leaves on removal.
- A feeder that watches a neighbour inventory polls every 5 to 40 ticks, faster after a change, offset per feeder [tech survey n32, OS12, OS13].
- Diet and prey lookups resolve once per reload into arrays, cleared on reload and tag updates [tech survey n32].
- Stackable food and Nublar items carry deterministic components so they stack in storage grids [tech survey B20.14].
- Feeding timer (D193, D194 answered) [lead 2026-10-12]: every feeder screen has interval buttons of 2, 5, 10 and 20 min. The hatch opens when the timer fires and the gauge holds food. It stays open until all animals finish eating: it closes after 100 ticks with no bite. An empty feeder always closes.
- Live-Bait Feeder (D197 to D201) [lead 2026-10-12]:
  - Model: Feeder 1 with a pole (new art). Code draws the chain from the pole to the animal that is out, like a lead (D235) [lead 2026-10-13].
  - Loading: a player leads a vanilla animal on a lead to the feeder and right-clicks it. The animal leaves the world, and the pen count of its type rises by 1. The pen saves its counts.
  - Pen types: chicken, rabbit, pig, sheep, cow, horse, goat.
  - Release: the screen sets the interval: 2, 5, 10 or 20 min. At each interval, if no bait is out, the hatch opens and one animal comes out on the chain. No new bait comes out while the last one lives. The task plan picks which type comes out next.
  - The animal out carries the tag `projectnublar:bait` and is persistent, with no goal change [entity survey S13]. The chain holds it near the pole.
  - Hunt: a carnivore's prey pass ranks the chained bait first when the bait fits its size. A small carnivore hunts a chicken, a rabbit, a pig or a sheep. A large carnivore also hunts a cow, a horse or a goat. Size classes: Tyrannosaurus large, Velociraptor and Dilophosaurus small [inf].
  - A kill gives the F2.4 stress relief and counts as a hunt (hunt cooldown, stamina).
  - No release on hunger or on a redstone pulse. The release runs on the interval only (D200 answered).
- Screens (D195) [lead 2026-10-12], three in the lab GUI style (`processor.png`):
  - Herbivore, for all four heights: 3 × 3 food slots, a green gauge, the food %, "Eating: X of Y" and the timer buttons.
  - Carnivore, shared by small and large: 3 × 3 meat slots, a red gauge and the timer buttons.
  - Live-Bait: the pen count per animal type, release interval buttons (2, 5, 10, 20 min), the animal on the chain and the next release.
- Hopper and pipe input. Fill from outside the paddock (with F3.1).
- A hopper fill writes the F2.4 change map, so a hungry dinosaur reacts at the next scan [JC3 J6].
- The gauge drives the look. An empty gauge shows the empty look, and any food shows the full look. The artist can add in-between looks. The Live-Bait Feeder shows the animal on its chain and needs no fill look.

**In scope, feeder motion** (D193) [lead 2026-10-12]:

The original mod had no feeder [orig 1.16]. The clips are the ones in each model file.

| Feeder | Open | While open | Closed | Close |
| --- | --- | --- | --- | --- |
| Carnivore (Feeders 1 and 2) | "Give me some meat" (9 s) plays once when the feeding timer fires and the gauge holds food. The doors slide apart, and the meat platform rises | The clip holds its last frame, and "Idle" loops. Each bite plays the food-drop sound and a few food particles | Only the lid shows | The open clip plays in reverse after 100 ticks with no bite, or at once when the gauge is empty |
| Herbivore (Feeders 5, 3 and 4) | "Open Feeder" (8 s) plays once by the same rule. The food rises to the feeder's height | "idle(open)" loops. Each bite plays the food-drop sound | "idle" (no motion) | By the carnivore rule |
| Very Tall Herbivore (Feeder 6) | "Open Feeder" (8 s) plays once by the same rule | The clip holds its last frame | "idle" (no motion) | By the carnivore rule |
| Live-Bait (Feeder 1 with a pole) | The Feeder 1 open clip plays once when a release fires. One animal comes out on the chain | The clip holds its last frame until the animal is out | Only the lid and the pole show | The open clip plays in reverse when the animal is out (proposal) |

- Close method (D239) [lead 2026-10-13]: each feeder gets a close clip from the artist (an Art list row). Until it exists, the open clip plays backwards through GeckoLib 5.
- The server owns the state (closed, opening, open, closing) and its start game time. It syncs them on change only. Clients play the clips from game time, so every player sees the same motion.
- Sounds per feeder kind (proposal, the lead decides): hatch open, hatch close, and a food drop on each bite. A release sound plays when the Live-Bait Feeder puts out its bait. Each event has its own file.

**Out of scope:** comparator output (D27, D28). An "any item" filter slot [tech survey B20.15]. A POI type for feeders [tech survey B20.16]. An experience-orb magnet [entity survey B20.9]. Nutrient values in a NeoForge-only data map [JC3 B20.20]. The Fish Feeder. A "Dino Feed" item [s11.13]. Real shaft blocks or digging (D192). Release on hunger or redstone (D200 answered).

**Produces:** the seven feeder blocks, each in the F2.4 registry, with the hover-box interface of F1.10.

**Recipe rows:** Small Carnivore Feeder, Large Carnivore Feeder, Low, Medium, Tall and Very Tall Herbivore Feeders, Live-Bait Feeder. One recipe each. Each bigger feeder uses the feeder below it (D202). F3.4 adds them from the approved recipe table (D78, D202). New ids: `small_carnivore_feeder`, `large_carnivore_feeder` (D251).

**Art:** see Art list → Feeders (models, the Live-Bait pole, empty and full looks, open and close clips, three screens, sounds, item sprites). Not done until those rows are Done (no Placeholder, no To check).

**Config (defaults):** feeding timer intervals 2, 5, 10 and 20 min, picked on the screen. Close after `feeders.idle_close_ticks` 100 with no bite (D194 answered). Live-bait intervals 2, 5, 10 and 20 min, picked on the screen. Live-bait stress relief −20 (F2.4). Neighbour poll 5 to 40 ticks [tech survey n32].

**Acceptance (both loaders):**

- A hopper keeps a Small Carnivore Feeder stocked. When its timer fires, the hatch opens, the meat rises, and a T. rex eats from it with a rising hunger value.
- A Brachiosaurus eats from the Very Tall Herbivore Feeder at head height. A Triceratops eats from the Low Herbivore Feeder.
- Eight Gallimimus, hunger at 0.3, share one Low Herbivore Feeder with one open long side. At most 3 eat at once (spot count, proposal). The others wait within 6 blocks. All reach full hunger within 5 minutes (proposal). An unused spot claim frees after 5 s (D111). No two claim one spot [lead 2026-10-12].
- A Triceratops and two Gallimimus share one Low Herbivore Feeder by body width. A feeder against a wall gives no spot on that side.
- A Large Carnivore Feeder gives more feeding spots than a Small Carnivore Feeder.
- A feeder placed on flat grass needs no digging. Its shaft and lifted food draw with no blocks below, and the food stays visible while the hatch is on screen.
- Inputs fill the gauge to 100 %. Further inputs stay in the slots. As the gauge drains, the slots refill it.
- A hay bale fills the gauge like 9 wheat. Kelp and seagrass go into a herbivore feeder. A carnivore feeder refuses wheat.
- An open hatch closes when its gauge reaches zero. A feeder with an empty gauge never opens.
- With the interval at 5 min, a stocked feeder opens every 5 min. Three hungry Gallimimus eat. The hatch closes 5 s after the last bite (D194).
- A Triceratops eats from the Medium Herbivore Feeder when its head reaches the food (D217).
- A player leads a cow to the Live-Bait Feeder and right-clicks. The cow leaves the world, and the pen count for cows rises by 1. With the interval at 2 min, one cow comes out on the chain after 2 min. No second cow comes out while it lives.
- A hungry T. rex kills the chained cow. Its stress falls by the relief, and the hunt counts. A Velociraptor does not hunt a chained cow, but it hunts a chained pig.
- Each screen shows its slots, its gauge and its status. The Live-Bait screen shows the pen counts, the interval buttons, the chained animal and the next release.
- The third food slot takes food, and dinosaurs eat from it [tech survey d20].
- A comparator next to a feeder reads 0 at every fill level (D27, D28).
- A hopper fills an empty feeder, and a hungry dinosaur in range starts its search at the next scan [JC3 J6].
- An empty feeder shows its empty look. A filled feeder shows its full look.
- Two clients see the same hatch frame at the same tick. Each open, close, bite and release plays its own sound event.

### F3.4: Park recipes

**Old module:** B13 (park items) · **Section:** 11.8 · **Size:** S · **Depends on:** F3.1, F3.2, F3.3, D78

**In game:** the player crafts every park block and tool in survival from vanilla materials. Each feeder has its own recipe. Advancements mark the first paddock, the first feeder fill, the first live-bait hunt and the first Large Generator.

**Touches:** datagen `ModRecipeProvider`, the advancement provider, creative tabs.

**In scope:**

- Build the park rows of "Recipes and progression gates". The lead approved Claude's draft with changes (D78, D202) [lead 2026-10-13]. The rows are the "Recipe rows" of F3.1 (fence items), F3.2 (Large Generator, pylon head) and F3.3 (feeders).
- One recipe per feeder. The Large Carnivore Feeder uses the Small Carnivore Feeder, and each taller herbivore feeder uses the one below it (D202). Copper is the wiring metal: Wire Spool, Power Conduit, pylon head.
- Advancements (D203) [lead 2026-10-12]:
  - "First paddock": a closed, powered fence loop surrounds a dinosaur. The task plan defines the loop check.
  - First feeder fill, first live-bait hunt, first Large Generator.
- Survival recipes for posts, spool and Fence Remover are the parity exception of F3.1. The Creative Fence Remover stays creative only.

**Out of scope:** vehicle items (F4.2).

**Produces:** the park recipes.

**Art:** none beyond the item sprites of F3.1 to F3.3.

**Config (defaults):** none.

**Acceptance:**

- Unit test: every registered item in the creative tabs (lab and park) has a recipe or a creative-only mark.
- A survival player builds a powered, fenced paddock with a feeder from vanilla materials, on each loader.
- Each park advancement fires once in a survival test world. The triggers are a closed powered loop around a dinosaur, a feeder fill, a live-bait kill and a first Large Generator.

---

## M4. World and vehicles

**Goal:** the world teaches the player where to dig, and the park gets vehicles.

**Exit gate (playtest):** on each loader, a tester in a fresh world buys a map from a paleontologist, follows it to a digsite, clears a Paleo-Illager site, drives the Jeep Wrangler to a paddock, and rides a Ford Explorer tour of two stops.

### F4.1: Digsites, paleontologists, Paleo-Illager sites and maps

**Old module:** B21 · **Section:** 11.3, 8.6 · **Size:** XL · **Depends on:** M3, F1.1

**In game:** digsites have tents, chests with fossils, paleontologist villagers and an iron golem. A paleontologist trades fossils, maps, amber and tools for emeralds, and any lab machine is its workstation. Paleontologists give iron golems fossil pieces, and a golem drops its piece on death. A map leads to the nearest digsite within 1,000 blocks. Pillagers and vindicators guard Paleo-Illager sites. The sites fight back and give fossils and machines when cleared.

**Touches:** new `J/worldgen/structure/*`, structure NBT and jigsaw JSON under `data/projectnublar/worldgen/`, a villager profession and POI, the map item, an illager entity variant. The POI states of the four lab machines.

**In scope:**

- Digsite structures (jigsaw): a rich deposit from F1.1's `digsite_deposit`, tents, loot chests, paleontologists and an iron golem. Small to large. Larger is rarer. A builder of the team makes the structure files. The 2020 builds are not used (D213 answered).
  - Data only, so `/locate` works with no code [entity survey S14]. Start values: jigsaw depth 3 to 4, heightmap `WORLD_SURFACE_WG`, terrain adaptation `beard_thin`, a biome tag, spacing 42 chunks, separation 28, village exclusion 10. Optional pieces use pools with an empty fallback.
  - Paleontologists and golems stand at fixed spots through mob pools of one entity or a data marker [entity survey S15].
  - Each chest names its own loot table. Fossils join vanilla loot only through an inject pool. No vanilla file is replaced [entity survey n27, S15].
- Paleontologist profession. Any lab machine is its workstation: the Fossil Processor, the Sequencer, the Egg Printer or the Incubator (D209 answered). No new block. Trades for emeralds: maps, fossils, amber and special pieces bought and sold, tools and parts sold. It spawns in digsites and at a low rate in villages (lab house spawn weight 1).
  - A POI type for the assembled states of the four lab machines with one ticket, and the profession with that POI [entity survey S17]. Trades ship as data per level. Vanilla brains claim the job site. A villager that claims a player's machine changes no machine state [inf].
  - Trades (D215 answered) [lead 2026-10-13]: the table in spec F4.1. Vanilla format: 2 trades per level, picked at random from the list of the level (confirmed by the lead, Z-8). The Journeyman Identification Tag trade left with the tag, so that level keeps 7 trades (Z-8). Maps cost the vanilla map price (Digsite Map 14 emeralds and a compass, Species Digsite Map 21 and a compass). Other goods cost the draft price + 25 %, rounded up.
  - The paleontologist also sells chips, the Hard Drive, the Gold Filter and lab machine parts. A part trade rolls its part when the trade appears. So a player can skip the gold gate by trade.
  - A fossil that it sells is of a local species: a species valid for the fossil generation near the villager.
  - Hero of the Village gift: 1 to 3 empty test tubes or 1 empty syringe.
  - Paleontologists give iron golems fossil pieces, a joke of the 2020 board (D214 answered). About once per in-game day, a paleontologist within 4 blocks gives a golem one fragmented fossil of a local species. The golem holds it like a poppy and drops it on death. The lead accepts a possible fossil farm [lead 2026-10-13].
- Lab house: the team builder also makes the paleontologist lab house for villages, in Default, Spruce and Desert styles [2020 board, lead 2026-10-13].
  - At server start, a paleontologist house joins each village house pool at `worldgen.paleontologist_house_weight`. It spawns at the normal rate of a village house (lead, Z-8): the task plan reads the weight of a normal house in each pool. Weight 0 adds nothing [entity survey n27, S18].
- Paleo-Illager sites: a hostile variant, small and poor or large and rich. A mix of pillagers and vindicators guards each site (D211 answered). Clearing gives fossils, chests and machines. No neutrality item, no "ancient plight" [s11.3].
  - The Paleo-Illager extends the vanilla illager base with the vindicator goals and attributes [entity survey S16]. It spawns only from site pools and spawn overrides, never in raids.
  - It has its own skin on the vindicator model, by the artist (D211). A vindicator texture does not count.
  - A friendly digsite camp has an iron golem. An illager site has none (D212 answered).
- Maps: an explorer map leads to the nearest digsite and can name a species. Paleontologists are the main sellers. Cartographers and wandering traders also sell it (D210 answered, asker's reading). They also sell species maps, at a higher price [lead 2026-10-13]. A cartographer Master trade rolls a Digsite Map (28 emeralds and a compass) or a Species Digsite Map (42 and a compass). It has 4 uses. A wandering trader's rare list offers a Digsite Map for 35 emeralds or a Species Digsite Map for 53.
  - Use the vanilla `exploration_map` function and treasure-map trade, so async-locate mods cover Nublar maps. No own structure search [tech survey n33, OW6].
  - Digsite spacing keeps the median distance under 800 blocks [tech survey n33].
  - Guaranteed distance: 1,000 blocks (D33 answered) [lead 2026-10-13]. This replaces the 1,600 blocks of the tech survey [tech survey O19].
- Worldgen thread rules [tech survey B21.14, OW1 to OW5]: immutable maps built on the main thread, only the given `RandomSource`, blocks only inside the region with no drops or spawns, no lazy cache.
- A vanilla mob change uses a tag, a config value and a tick event, never a goal replacement [entity survey B21.7].
- `worldgen.chest_fossil_percent` (100, range 0 to 1,000) scales the fossil inject pools on both loaders [JC3 B21.21].

**Out of scope:** Paleo-Illagers in raids and patrols [entity survey B21.8]. Dialogue NPCs [entity survey B21.9]. A new workstation block (D209). A flat-site grid test [JC3 B21.17]. "One per N chunks" placement [JC3 B21.20]. A worldgen switch for digsites (D29: only the breeding switch ships). Paleobotanists (Release 1, D10). Design wishes that section 11.3 does not select (the lead assigns them): librarians selling maps, Nitwits at digsites, trailer houses, Hatchery and Paleobotany Lab structures. The lab house is in scope since 2026-10-13.

**Config (defaults):** `worldgen.paleontologist_house_weight`: the weight of a normal village house in the same pool [lead 2026-10-13, Z-8], `worldgen.chest_fossil_percent` 100. `maps.search_radius_blocks` 1,000 (D33). Trade prices ship as data (D215). `villagers.golem_fossil_interval_ticks` 24,000 [lead 2026-10-13].

**Produces:** the digsite structures, the profession, the Paleo-Illager, the digsite map.

**Recipe rows:** none. The lab machines serve as the workstation (D209).

**Art:** see Art list → Digsites and villagers (structures by the team builder, paleontologist skin, illager skin, map item and marker). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance (both loaders):**

- `/locate structure projectnublar:digsite` finds one within 1,000 blocks in a fresh world (D33).
- A bought map points to it. 20 map trades each lead to a digsite [JC3 B21.18].
- An illager site attacks and, once cleared, yields fossils. Its guards are pillagers and vindicators, and it has no iron golem.
- A paleontologist next to an assembled Sequencer takes it as its job site.
- A cartographer and a wandering trader can offer a digsite map and a species map, at a higher price than the paleontologist.
- A golem that holds a fossil drops it on death. A paleontologist sells only fossils of species that generate near its village.
- No digsite overlaps a village [entity survey S14].
- With the house weight 0, no village gets a paleontologist house [entity survey S18].
- With `worldgen.chest_fossil_percent` 0, 50 village chests hold no fossil [JC3 B21.21].

### F4.2: Vehicles

**Old module:** B22 · **Section:** 11.12 · **Size:** XL · **Depends on:** M3

**In game:** the player builds a vehicle part by part, like a lab machine. The parts go on in order: a chassis, 4 wheels one by one, an engine, a body and seats. The body decides the vehicle. A Jeep body gives a Jeep Wrangler. An Explorer body gives a Ford Explorer, which also takes a Tour Computer. Each placed part shows on the model. When the last part is on, the vehicle becomes a driveable entity. The player drives the Jeep around the park. Dye on a vehicle tints its colours, and a Wrench cycles its variation. A 1992 Ford Explorer follows a visible road-marker track that the player lays and stops at stop markers. It waits for anything on the track and sounds its horn [tech survey T13]. It runs driverless tours in a loop. At each stop it chimes and shows "Stop 2 of 4: Triceratops paddock", with the name that the player gave the stop (D208).

**Touches:** new `J/entity/vehicle/*`, the vehicle part items and the assembly state, `J/block/TrackBlock`, `J/block/TourStopBlock`, the stop-name screen, client renderers, the input packet.

**In scope, vehicle assembly (D250 answered) [lead 2026-10-13, Z-8]:**

- The player assembles each vehicle with right-clicks of part items, in a set order, like the F1.10 lab machines:
  1. Chassis. The player places it on the ground.
  2. Wheels, one at a time, 4 in total.
  3. Engine.
  4. Body: a Jeep body or an Explorer body. The body sets the vehicle type.
  5. Seats.
  6. Explorer only: the Tour Computer.
- Every part except the body serves both vehicles: chassis, wheel, engine, seats. The Tour Computer serves only the Explorer.
- A part placed out of order does nothing, and the player keeps the item.
- Each placed part shows on the model. Asset basis: the REPO models of each vehicle "without wheels" and the Explorer `Wheels.bbmodel`. The REPO models have no chassis, engine or Tour Computer group of their own (Art list question 21).
- The last part turns the assembly into the driveable vehicle entity. A new vehicle starts at variation 1 [inf].
- This replaces "one Jeep item and one Explorer item" (D204, 2026-10-12).

**In scope, vehicle base:**

- Seats, driver input, collision, a GeckoLib model. No fuel (D07 answered).
- Variations (D204 answered) [lead 2026-10-12]: the player builds each vehicle from parts (D250). A synced variation index picks the model: Jeep 1 roll bar, 2 soft top, 3 open. Explorer 1v roof rack, 2v, 3v, 4v. A Wrench (new item) used on an assembled vehicle moves it to the next index [lead 2026-10-13]. Dye tints the colours of the vehicle, and the vehicle uses up the dye. The artist marks the parts that take the tint (Art list question 20).
- Damage and repair (D206 answered, reversed) [lead 2026-10-13]: not in Beta. The lead tabled vehicle damage, wrecks and repair for a later release. Vehicles have no health.
- Hits (D75 answered, asker's reading) [lead 2026-10-12]: below `vehicles.hurt_speed`, a vehicle pushes mobs and players aside with no damage. Above it, the hit does damage scaled by speed.
- The driver's client runs physics. Others interpolate over the real update interval. The server clamps speed and distance per packet to the TOML top speed [entity survey n28, tech survey n35].
- The server reads forward, back, left, right and jump from the controlling passenger's vanilla input. Extra keys (horn, driverless start) travel in one checked packet of vehicle id and one byte, sent only on change. The sender must be the controlling passenger in seat 0 and not a spectator, one packet per tick at most. Dismount clears the input [entity survey S12, JC3 J14].
- One handling record per vehicle: top speed, acceleration time, turn rate, reverse at a third of top speed, a collision speed cap, half speed in tight turns [entity survey B22.3, tech survey B22.24].
- The Jeep and the Explorer share one class. Boat-style controls: A and D turn, the mouse looks [entity survey B22.4].
- Seats sit at model locators. A development command computes offsets [entity survey B22.5].
- The chassis item ray-traces to the hit face. It fails with a message if the box of the finished vehicle hits blocks [entity survey S19].
- Animation-only values stay on the client [entity survey B22.8].
- Vanilla tracking sends position. Extra state (wheel angle source, lights, horn) goes only on change, and a stopped vehicle sends one final state [tech survey T15].
- Tracking range 10 chunks. Update interval: the tech survey proposes 3 ticks [T15], the entity survey 1 tick [entity survey B22.8], and JC3 ships 1. The task plan picks one.
- Render distance grows while a vehicle carries passengers [entity survey n28]. The culling box covers the whole model [tech survey n35].
- Dinosaurs never board. `projectnublar:vehicle_may_autoboard` is empty [tech survey n35]. Do not override `canCollideWith`. Override `canBeCollidedWith` only on the tour vehicle [tech survey OS7].
- A vehicle stops at an electric fence like at a wall. A live fence also shocks the driver once (D26 answered) [lead 2026-10-12].
- A rider who leaves and joins again sits in the same seat: one synced integer per seat, a saved map from player UUID to seat [JC3 J14, B22.35].
- Headlights work with LambDynamicLights through an optional data file [tech survey B22.25].
- Code drives all motion (D205 answered) [lead 2026-10-12]: wheel spin from the speed and front-wheel steering on the wheel groups. Doors swing open when a player gets in or out, where the model has door parts. No keyframed clips.
- Sounds: engine loop, horn, door, crash, and the tour stop chime (D208).

**In scope, Jeep Wrangler:**

- Driven, 2 seats (D25 plan default) [tech survey O11].
- It climbs a one-block ledge with a step height of 1.0 to 1.25 (1.5 as the upper bound) [tech survey B22.23, JC3 J15].
- Handling [JC3 J15]: each tick multiply velocity and yaw rate by the drag (start 0.8). Gravity in the air. Push along the facing by top speed × (1 − drag), a third in reverse, and a small push while only steering. Steering changes yaw rate by a factor times the push, clamped to 3° per tick, inverted in reverse. The server clamp stays.
- The body pitches and rolls on the client from four edge probes in 0.25-block steps, each taking the highest collision top within 32 blocks below, run when moved or turned, at most every 20 ticks, offset by id. Each height moves at 0.15 blocks per tick [JC3 J15].

**In scope, Ford Explorer:**

- Driven by hand off the track. 4 seats. Players board at any time, also while it moves (D25 answered) [lead 2026-10-12].
- The track is a visible road-marker block (D207 answered) [lead 2026-10-12].
- Track follower [entity survey S20, feature 6]: reads the track block and its `RailShape` only when its block position changes, picks the exit by shape and direction, moves at a constant track speed (start 0.25 blocks per tick) with no physics, rises and falls on slopes, and turns at a capped rate. At a track end it stops and gives control to the driver.
- One backward press on a track reverses it once [JC3 J14, B22.38].
- Driverless mode is a state of the Explorer, started by the driver with a key on a track. It runs on the server with or without a driver [entity survey feature 7].
- The tour forces no chunks and pauses where chunks stop ticking [tech survey B22.26]. It implements `isAlwaysTicking` for Immersive Optimization [tech survey n35].
- Blocked track [tech survey n34, T13]: every 10 ticks, test a box 4 blocks ahead for entities in `projectnublar:tour_vehicle_stops_for` (D25 answered: anything on the track). Stop. After 60 ticks, sound the horn with a growing chance, at most 5 times. Start again when clear.
- Stops [tech survey n34, T13]: a marker can set its own dwell time over `vehicles.stop_seconds`. A powered marker holds the vehicle until the signal ends. At each stop the vehicle plays a chime, and its riders see "Stop 2 of 4" and the stop name (D208 answered). The player sets the name on the marker in a name field like the anvil's. The name saves with the marker. The redstone lamp of the marker lights while an Explorer stands at the stop (D250) [lead 2026-10-13].
- Loop check [tech survey n34, T10]: at a tour start, walk the track through each block's exits until it returns. A gap shows "Track broken at x, y, z". Cap the walk at 4,096 blocks (estimate) as config.
- No Tour Station in Beta (D25 answered) [lead 2026-10-13]. The player starts each tour by hand [tech survey O11, B22.14].
- `TrackBlock` extends `BaseRailBlock` and reuses `RailShape` and `AbstractMinecart.exits` [tech survey B22.17]. A tool-placed track uses flags that skip neighbour updates, then updates only around the two ends [tech survey T10].
- Correction 2026-10-10: driverless vehicles have precedents (Automobility's Autopilot, MTR trains).

**Recipe rows:** Wrench, chassis, wheel (4 per craft), engine, seats, Jeep body, Explorer body, Tour Computer, tour track (16 per craft), tour stop marker. The lead set them on 2026-10-13 (D250 answered). This package adds them to the recipe provider from "Recipes and progression gates".

**Out of scope:** gyrosphere, monorail, aircraft, boats, schedules beyond a loop, track junctions [entity survey feature 6]. A timed Tour Station (D25). Vehicle health, wrecks and repair (D206, a later release). Visible damage stages, self-healing dents, breakdown after 160 ticks in water [entity survey B22.10, JC3 B22.40, B22.46]. Abstract tour data [tech survey B22.28]. Per-box hitbox entities, custom collision, boost pads, drift, upgrades, physics bodies [tech survey B22.29, B22.30]. Part-building is in scope since 2026-10-13 (D250). Camera distance keys [JC3 B22.36]. Breaking leaves and vines [JC3 B22.43]. Fuel (D07).

**Config (defaults):** `vehicles.stop_seconds` (set in the task plan), the handling records, the loop walk cap 4,096 (estimate). `vehicles.hurt_speed` (set in the task plan, D75).

**Produces:** the vehicle entity, the vehicle part items and the assembly state, `TrackBlock`, `TourStopBlock`.

**Art:** see Art list → Vehicles (models and textures for every variation, seat locators, track and stop marker blocks, item sprites, vehicle sounds). The assembly stages need parts of their own (Art list question 21). Code drives the motion, so no clips (D205). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance (both loaders):**

- A player assembles a Jeep from a chassis, 4 wheels, an engine, a Jeep body and seats, in that order. Then the player drives it around a paddock. Each part shows on the model when the player places it.
- An Explorer needs the Tour Computer after its seats. Before that part, it is not a vehicle that a player can drive.
- A fifth wheel used on a chassis does nothing, and the player keeps the item.
- The Explorer completes a laid loop with two stops unattended and carries a passenger.
- It stops for a dinosaur on the track and starts when clear [tech survey T13].
- A tour start on a broken loop shows the broken position [tech survey T10].
- A chassis does not place where the finished vehicle would sit inside a wall [entity survey S19].
- An extra-key packet from a non-controlling player changes nothing [entity survey S12, JC3 J14].
- A parked vehicle sends no movement packets [tech survey T15]. A driven Jeep sends no Nublar movement packet [JC3 J14].
- A rider who leaves and joins again sits in the same seat. One backward press reverses the Explorer once [JC3 J14].
- On a 1-block slope the Jeep body pitches on the client with no step jump [JC3 J15].
- A Jeep driven into a live fence stops, and its driver takes one shock (D26).
- A Wrench used on a Jeep three times cycles it through all 3 variations and back. The variation stays after a reload (D204).
- Red dye on a Jeep tints its marked parts red, and the player loses one dye.
- Wheels turn with speed and the front wheels steer. Explorer doors swing open when a player gets in (D205).
- A slow Jeep pushes a cow aside with no damage. A fast Jeep hurts the cow (D75).
- At a named stop, riders hear the chime and see "Stop 2 of 4" and the name (D208). The lamp of the stop marker lights while the Explorer stands there, and goes off when it leaves (D250).
- A player boards a moving Explorer (D25).

### F4.3: Guns (blocked)

**Old module:** B23 · **Section:** 11.12, 11.14 · **Size:** unknown · **Depends on:** D08

**In game:** no change until the lead decides D08.

Do not start. Do not re-ask unprompted. When the lead decides, write this package's scope here, then the task plan. Known inputs:

- The design document's three kinds: tranquilizer, stun and capture, lethal.
- Art list → Guns lists the gun models and the ammo sprites (dart, taser cartridge, shotgun shell).
- A tranquilised state implies a `tranqed` clip per species (F2.3).
- A dart gun with a fixed sedation of 650 to 699 ticks [JC3 B23.5].
- Design direction for use only after D08 [entity survey feature 8], not scope: a dart is a server projectile. Tranquilizer points scale down by body mass. Above the species threshold, a `TRANQUILISED` timer stops movement and behaviours. Capture stores the dinosaur in an item through vanilla entity save. Ammunition sits in a server-written data component.

**Art:** see Art list → Guns.

---

## Beta hardening

**Goal:** the Beta runs fast and clean next to the common optimization mods, and ships.

**Exit gate (playtest):** two players who did not build the mod play the full survival loop on each loader with the optimization mods installed. The Beta jars ship from a tag.

### BH.1: Cleanup and performance pass

**Old module:** B25 · **Section:** 6.4, section 10 item 14 · **Size:** M · **Depends on:** M4

**In game:** no new gameplay. A park of 100 dinosaurs keeps 20 ticks per second. A paddock far from every player keeps needs, growth and breeding at the normal rate. The server log stays quiet. A player on a weak computer can turn off particles, screen shake and glow layers.

**Touches:** the whole `common` tree, brain sensors and behaviours (distance band), the client config, GameTest classes, CI.

**In scope:**

- Remove unused classes: `DinosaurPart` in the loader module, `IDinoPart`, `IMultiPartDino`, `DinoTexture`, and `EntityColorStore` if unused. Remove the `DebugStick` chat output. Check that no print call remains [tech survey d7].
- Profile a park of 30 animals, 4 machines and 60 posts on a dedicated server, with the tick time per system. Also load A (20 Gallimimus in one herd, 1 player, 300 other entities) and load B (100 dinosaurs in 5 herds, 10 large, 3 players, 600 other entities) [hitbox design]. Today's load B estimate: about 28 ms plus spikes near 440 ms. Target 9 to 15 ms plus repath spikes. Nobody has profiled 26.2. Use SmartBrainLib profiler sections and the herd timings [entity survey B25.2].
- One class holds every slow-work interval. Work is due when game time plus entity id, modulo the interval, is 0 [entity survey n29, S21]:

  | Job | Interval | Basis |
  | --- | --- | --- |
  | Needs and stress | 20 ticks | F2.4 |
  | Group join | 40 or 200 ticks | `JoinGroup` |
  | Grazing scan | 40 ticks | estimate |
  | Water scan | 60 ticks | estimate |
  | Fence sensor | 100 ticks | estimate |
  | Reputation decay | 24,000 ticks | vanilla gossip |

- Far dinosaurs think less often but walk at full speed with needs at the normal rate [tech survey n36, T9]. A 20-tick sensor sets a band from the nearest player: 0 within 64, 1 within 128, 2 beyond. A target, a threat memory or fence contact sets band 0. Sensors use `scanRate(e -> base + band × base + (id & 15))`. Idle behaviours use a `cooldownFor` that grows with the band (100 ticks at band 2, estimate). The entity survey proposes a simpler rule (one far distance of 128, factor 4, recheck every 100 ticks) [S25]. The task plan picks one.
- Every gameplay timer uses game time (risk 7) [tech survey OS4].
- Level unload clears every per-level map: group manager, feeder registry, power networks, the need-search change map, the young-target event map, the part index [entity survey n29, tech survey OM5, JC3]. No static field holds an `Entity`, `BlockEntity` or `Level` [tech survey OM6].
- No tick mixin on a vanilla base class and no global per-entity hook [entity survey B25.5]. Exceptions: the F2.5 `Entity.collide` block filter and the F2.1 part-query mixins, each returning at its first line for non-Nublar entities. Profile both.
- A carcass drop, a leaf-breaking charge or a fence cascade never freezes the server. Each level queues pending units and runs 8 per tick (estimate). Level unload clears the queue [entity survey S23].
- A client config with three low-effects switches: particles (all, reduced, none), screen shake, glow layers. It loads into static fields on load and change. Renderers read the fields [entity survey n29, S22]. F2.3's effects obey them.
- A unit test parses every shipped JSON file [entity survey n29].
- Every work package has GameTests in common code. CI runs the GameTest server of both loaders [entity survey n30, S24].
- Run the optimization-mod test matrix [tech survey n37]: Sodium, Iris, EntityCulling, ImmediatelyFast, Lithium, Immersive Optimization, C2ME, Alternate Current, Particle Core, and "Prefer Vulkan".
- Every custom NBT root has a `"version"` integer [tech survey OM7].
- Resolve every open rft `TODO(BUG)` or make it a GitHub issue.
- Each fast path has three switches [JC3 J13, rank 12]: an off switch in the server config, a compare switch that runs both paths and logs any difference once with a count, and a JVM property that breaks the fast path in a test. They apply to the part index, the column cache, the need-search sleep and the bone masks.
- Profile the JDK vertex-allocation claim once on 26.2 with 100 creatures. Report it upstream if true. The mod has no `Unsafe` vertex code [JC3 n30].

**Out of scope:** pathfinding on a thread pool. Measure first [entity survey B25.10]. One random block per second for slow world jobs [JC3 B25.12]. A presized visited-block set through a mixin, unless the Brachiosaurus test on 26.2 shows lag (D70 answered) [JC3 B25.14].

**Produces:** the interval class, the distance band, the low-effects config, the profile records.

**Art:** none.

**Config (defaults):** client `particles` all, `screen_shake` true, `glow_layers` true [entity survey S22]. Server fast-path off and compare switches, all off by default [JC3 J13].

**Acceptance:**

- `grep -rn "System.out\|System.err\|printStackTrace" common/src` returns nothing.
- The PR records the profile per system for the park, load A and load B.
- With Immersive Optimization installed and the player 200 blocks away, needs and stamina change at the normal rate.
- Every row of the optimization-mod matrix passes on both loaders.
- The JSON parse test passes.
- After a level unload, no per-level map holds an entry for that level.
- With each compare switch on, the park profile logs no difference. With the break property set, the compare switch logs one.
- The PR records the vertex-allocation result.

### BH.2: Balance playtest and release

**Old module:** P4 · **Size:** M · **Depends on:** BH.1

**In game:** no new gameplay. Two players who did not build the mod play the full survival loop on each loader. The Beta jars ship from a tag.

**In scope:**

- [ ] Two outside players playtest the balance on each loader. Change TOML defaults only. Record each change and its reason in `docs/config-reference.md`.
- [ ] Check the "about 50 minutes per dinosaur" and "about 25 fossils on an iron filter" targets. Report the values to the lead.
- [ ] Run the optimization-mod matrix again before release on the loaders that each mod supports.
- [ ] Optional: the CC: Tweaked park peripheral on the fence switch and a park tracker (D34 answered: after F3.1. Create and Ponder wait for 26.2 builds). It pushes events on change only and uses one summary call [tech survey O20, C.2].
- [ ] Licence (D09): the lead chooses LGPLv3 or CC0 and states the art terms. Then set `LICENSE`, `gradle.properties`, `mods.toml` and `fabric.mod.json`.
- [ ] Complete `en_us`. Invite community translations after Beta.
- [ ] Enable CurseForge and Modrinth publishing with real project ids. A release workflow builds and uploads both jars from a tag.
- [ ] Update `docs/codebase-analysis.md` sections 1 and 4, or replace it with a short architecture overview and archive it.
- [ ] Tag `v1.0.0-beta.1`.

**Acceptance:** the Beta jars for both loaders are on CurseForge and Modrinth from the tag, and every Art list row for Beta is "Done".

---

## Release 1

**Goal:** plants and wood sets, the petrified tree, and the four Release Plan species not in Beta, with their fossils and digsites [s11.12, s8.6].

**Exit gate (playtest):** each R package passes its acceptance on both loaders.

Everything else "not in Beta" has no release. See "Unassigned backlog".

### R01: Plants and wood sets

**Section:** 11.12, 8.5, 8.6 · **Size:** L · **Depends on:** Beta released

**In game:** five new plants grow in the world and on farms. Four new wood sets and a petrified tree give building blocks. Herbivores eat the new plants.

**In scope:**

- Five plants from the Release Plan and the board: Cycad, Serenna Veriformans, West Indian Lilac, Heliconia, Horsetail. Each gets growth, placement, worldgen in listed biomes and drops.
- Four wood sets (logs, planks, stairs, slabs, fences, doors, trapdoors, signs) and the petrified tree. The plan does not name the four sets.
- Herbivore diet tags extend to the mod's plants. Plant matter recipes use them.
- Paleobotanist trades. The paleobotanist ships in Release 1 with the plants (D10 answered).

**Art:** see Art list (Release 1 rows). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance (both loaders):** each plant generates and can be farmed. A Triceratops eats a Cycad.

### R02: Release 1 species

**Section:** 7.3 (Release Plan), 8.5 · **Size:** XL per species · **Depends on:** Beta released

**In game:** four new species hatch, live and behave like the Beta species. The Mosasaurus lives in water.

**In scope:** Compsognathus, Microceratus, Parasaurolophus, Mosasaurus. Each needs a stat card from the lead (or interim values with a basis), a species JSON, a fossil set, a model and its full clip list (F2.3 rule), complete skin sets per gender, its own sounds, diet, prey preferences and AI particulars. The Mosasaurus needs aquatic movement, an aquatic enclosure rule and a water feeder. Fences do not apply underwater. Treat it as its own XL package R02d and schedule it last.

**Art:** see Art list (Release 1 rows). Not done until those rows are Done (no Placeholder, no To check).

**Acceptance per species:** the M2 acceptance lists of F2.1 to F2.6 pass for the new species.

### R03: Release 1 systems

**In game:** each new species has fossils to find and digsites that hold them.

**In scope:** a fossil set and digsite entries for each new species (F1.1 and F4.1 data only). Gene data for any vanilla animal that Minecraft adds after 26.2.

### Unassigned backlog

Not in Beta per section 11.13, and not assigned to Release 1. The lead assigns each one.

- Pteranodon (the Release Plan says Release 2), the tablet, the DNA server, hybrids, false eggs, gates, the overhanging fence, backup generators, trace and generic fossils, terracotta host stone, the drill extractor, turf wars, movement-based vision, the "Dino Feed" item, the mod's own frog and goat and other non-dinosaur animals, aircraft, boats, gyrosphere, monorail, the enclosure marker and park signs, diseases and treatment, the skeletal builder and skeleton exhibits, JEI or similar recipe integration, a guide book, the tracking beacon, the Fish Feeder, coprolites, gene mutation and sibling-breeding mutation.

**Candidates from the tech survey (not decided)** [tech survey "Post-Beta"]: security cameras with "no signal" in unloaded areas (SEC.1, SEC.2), a CCTV wall with 2 to 5 s snapshots, keycards for lab doors (SEC.5, after gates), a timed Tour Station (D25), dormant tours as data, a battery block, a power meter block (D27), a storm that makes fences flicker, control-room redstone channels (D28), a ranger voice radio, Ponder tutorials or a guide book (D34), Create integration (D34) (C.4 to C.7).

**Candidates from the JC3 survey (not decided)** [JC3 "Post-Beta"]: bugs, plankton and krill for insect and filter feeders, rare ruins with ready-made DNA, park decoration sets (concrete, reinforced stone, glass, signs, murals), a live-bearer machine with four nutrient bars [JC3 B11.14], rail speed tiers by block [JC3 B22.37]. Existing backlog lines with a JC3 precedent: hybrids [JC3 B00.13], aircraft [JC3 B22.45], the tablet [JC3 B19.18], skeleton exhibits, JEI [JC3 B13.1].

---

## Recipes and progression gates

Every Beta item that a player crafts has an approved recipe. The lead approved Claude's recipe draft with changes on 2026-10-13 (D78, D202). The mod has 1 recipe today (`unincubated_egg`), and F1.7 removes it. The original mod had no survival recipe for any part, upgrade, generator or fence item. The "Work package" column names the package that lists the row in its "Recipe rows" line and ships it. F1.12 checks the lab rows, and F3.4 builds the park rows. Evidence: the recipe inventory of 2026-10-12, the recipe draft of 2026-10-13 and the lead's review (open-items batch Z-1).

**Rules [lead 2026-10-13]:**

- Every ingredient is a vanilla item. No ingredient comes only from loot or trades. Metals, gems and dusts use common tags (`#c:...`), so the metals of other mods work too.
- The four lab machines alone cost about 50 iron value, 4 gold ingots and 14 quartz slabs (D256). Each machine has one gold ingot in its precision part. The whole first setup costs about 66. "Iron value" counts the iron ingots in every part, vanilla parts included. Examples: a bucket is 3, an iron trapdoor 4, a piston 1.
- No lab part uses a stone material. The lab machines are machines and computers smaller than a player (D256).
- The player crafts each upgrade fresh. A higher tier never uses up the lower tier. One exception: the Netherite Tank Upgrade follows the vanilla netherite rule.
- Copper is the wiring metal. Nether materials: nether quartz (the quartz-slab lab parts, SSD, Leveling Sensor), blaze powder (Hot Bulb) and netherite (the Sequencer tank only, 300 % [spec]).
- No recipe uses a special piece. No voltage sign item, no decorative fences, no emerald tank, no netherite Processor tank, no Ender SSD.
- New `*_base` part items start each machine in survival. The whole-machine items become creative only.
- The recipe book unlocks the lab parts when the player picks up the first fossil or amber.
- 26.2 reads recipes from `data/projectnublar/recipe/` (singular). Write every recipe in the common recipe provider, so both loaders get the same files. After datagen, a recipe viewer checks for grid clashes with vanilla and common mods.

**Grid notation.** A grid lists its rows from top to bottom, split by " / ". A dot is an empty slot. A grid with fewer than 3 rows or columns fits anywhere in the 3 × 3 grid. "Out" is the count per craft, 1 when not stated. A row note can give a letter its own meaning in that row only.

| Letter | Ingredient | Letter | Ingredient |
| --- | --- | --- | --- |
| I | iron ingot (`#c:ingots/iron`) | Q | glass pane (`#c:glass_panes/colorless`) |
| N | iron nugget (`#c:nuggets/iron`) | S | smooth stone |
| O | gold ingot (`#c:ingots/gold`) | K | stick (`#c:rods/wooden`) |
| D | diamond (`#c:gems/diamond`) | W | any wool (`#minecraft:wool`) |
| C | copper ingot (`#c:ingots/copper`) | P | piston |
| R | redstone dust (`#c:dusts/redstone`) | T | iron trapdoor |
| L | glass (`#c:glass_blocks/colorless`) | Y | redstone torch |
| Z | iron chain (`minecraft:iron_chain` on 1.21.9 and later. Check the id on 26.2) | U | quartz slab (`minecraft:quartz_slab`) |
| A | any light block (an item tag): lantern, soul lantern, the copper lanterns, redstone lamp, glowstone, the froglights, sea lantern, shroomlight, the copper bulbs | | |

**Lab items (M1):**

| Item | Work package | Tier | Ingredients (approved 2026-10-13) | Status |
| --- | --- | --- | --- | --- |
| Fossil pouch | F1.1 | Surface | `.X.` / `HJH` / `HHH`. X = string, H = leather, J = chest | Approved |
| Syringe | F1.2 | Iron | `I` / `L` / `N` (one column) | Approved |
| Fossil Processor base (`processor_base`) | F1.3 | Iron and Nether (quartz) | `IBI` / `UUU`. B = bucket | Approved (lead 2026-10-13) |
| Fossil Processor body | F1.3 | Gold (1 ingot) | `.O.` / `IGI` / `IRI`. G = grindstone | Approved (lead 2026-10-13) |
| Fossil Processor lid | F1.3 | Iron | `.I.` / `III`. No glass pane and no nugget | Approved (lead 2026-10-13) |
| Fossil Processor tanks (assembly part) | F1.3 | Iron | `LIL` / `L.L` / `LIL` | Approved (lead 2026-10-13) |
| Iron filter | F1.3 | Iron | `.I.` / `IXI` / `.I.`. X = iron bars | Approved |
| Gold filter | F1.3 | Gold | `.O.` / `OXO` / `.O.`. X = iron bars | Approved |
| Diamond filter | F1.3 | Diamond | `.D.` / `OXO` / `.D.`. X = iron bars | Approved |
| Test tube | F1.3 | Surface | `L` / `L` / `L` (one column). Out 3 | Approved |
| Sequencer base (`sequencer_base`) | F1.4 | Iron depth (copper) | `IRI` / `CCC` | Approved (lead 2026-10-13) |
| Sequencer door | F1.4 | Iron | `ITI`. T = iron trapdoor, not an iron door | Approved (lead 2026-10-13) |
| Sequencer computer | F1.4 | Gold (1 ingot) | `.O.` / `RER` / `III`. E = redstone repeater | Approved (lead 2026-10-13) |
| Sequencer monitor | F1.4 | Iron depth (copper) | `CCC` / `CQC` / `IRI` | Approved (lead 2026-10-13) |
| Hard drive (HDD) | F1.4 | Iron | `III` / `RCR` / `III` | Approved |
| SSD | F1.4 | Gold and Nether | `ORO` / `RAR` / `ORO`. A = nether quartz (`#c:gems/quartz`) | Approved |
| Egg Printer base (`egg_printer_base`) | F1.7 | Iron and Nether (quartz) | `IXI` / `UUU`. X = dropper | Approved (lead 2026-10-13) |
| Egg Printer needle | F1.7 | Gold (1 ingot) | `.O.` / `.R.` / `.I.` | Approved (lead 2026-10-13) |
| Egg Printer platform | F1.7 | Iron and Nether (quartz) | `UUU` / `IPI` | Approved (lead 2026-10-13) |
| Egg Printer body | F1.7 | Iron and Nether (quartz) | `QQQ` / `U.U` / `IRI` | Approved (lead 2026-10-13) |
| Leveling Sensor | F1.7 | Gold and Nether | `.A.` / `CRC` / `.O.`. A = nether quartz | Approved |
| Incubator base (`incubator_base`) | F1.8 | Iron and Nether (quartz) | `IFI` / `UUU`. F = furnace | Approved (lead 2026-10-13) |
| Incubator nest | F1.8 | Iron | `I.I` / `III`. An empty bed (an iron tray), with no wool and no hay. It stays empty until the player fills it with plant matter | Approved (lead 2026-10-13) |
| Incubator lid | F1.8 | Iron | `LAL` / `I.I`. A = any light block | Approved (lead 2026-10-13) |
| Incubator arm base | F1.8 | Iron depth (copper) | `.C.` / `IRI` | Approved (lead 2026-10-13) |
| Incubator arm | F1.8 | Gold (1 ingot) | `I.I` / `.I.` / `ROR`. Ingots, not nuggets | Approved (lead 2026-10-13) |
| Warm bulb | F1.8 | Iron | `.L.` / `IRI` / `.I.` | Approved |
| Warmer bulb | F1.8 | Gold | `.L.` / `ORO` / `.I.` | Approved |
| Hot bulb | F1.8 | Nether (blaze) | `.L.` / `BRB` / `.O.`. B = blaze powder | Approved |
| Small container upgrade (6 eggs) | F1.8 | Iron | `IWI` / `WXW` / `IWI`. X = chest (`#c:chests/wooden`) | Approved |
| Large container upgrade (9 eggs) | F1.8 | Gold | `OWO` / `WXW` / `OWO`. X = chest | Approved |
| Iron plant tank (`iron_plant_tank_upgrade`) | F1.8 | Iron | `ILI` / `LXL` / `ILI`. X = composter | Approved |
| Gold plant tank | F1.8 | Gold | `OLO` / `LXL` / `OLO`. X = composter | Approved |
| Iron tank upgrade | F1.10 | Iron | `ILI` / `LBL` / `ILI`. B = bucket | Approved |
| Gold tank upgrade | F1.10 | Gold | `OLO` / `LBL` / `OLO`. B = bucket | Approved |
| Diamond tank upgrade | F1.10 | Diamond | `ODO` / `LBL` / `ODO`. B = bucket | Approved |
| Netherite tank upgrade | F1.10 | Nether (netherite) | Shapeless: Diamond Tank Upgrade + netherite ingot (`#c:ingots/netherite`). Sequencer only (300 %) [spec] | Approved |
| Iron computer chip | F1.10 | Iron | `RIR` / `ICI` / `RIR` | Approved |
| Gold computer chip | F1.10 | Gold | `ROR` / `OCO` / `ROR` | Approved |
| Diamond computer chip | F1.10 | Diamond | `RDR` / `OCO` / `RDR`. It fits the Processor, the Sequencer (synthesis) and the Egg Printer (D113) | Approved |
| Basic Generator (`basic_generator`, was `coal_generator`) | F1.11 | Iron | `III` / `CFC` / `IRI`. F = furnace | Approved |
| Power Conduit | F1.11 | Iron depth (copper) | `CCC` / `RRR` / `CCC`. Out 16 | Approved |

Machine cost [lead 2026-10-13, D256]: the four machines alone cost about 50 iron value, 4 gold ingots and quartz slabs. The approved grids give Fossil Processor 15, Sequencer 13, Egg Printer 8 and Incubator about 14, so about 50. The five quartz-slab parts take 14 slabs, about 28 nether quartz (Claude's count). So the first lab needs a Nether trip, and the lead accepted this gate. Add the Basic Generator (5), the Hard Drive (6), the Iron Filter (about 4.4) and a syringe (about 1.1). Then the whole first setup costs about 66. This replaces the Z-8 text: about 45 iron value, with grids re-costed by Claude.

**Dinosaur, park and world items (F2.7, F3.1 to F3.3, F4.1, F4.2):**

The lead set the ingredients of the M4 rows on 2026-10-13 (open-items batch Z-8, D250). Claude set the grid shapes. Each part except the body serves both vehicles.

| Item | Work package | Tier | Ingredients (approved 2026-10-13) | Status |
| --- | --- | --- | --- | --- |
| Low-security fence post | F3.1 | Iron | `Y` / `I` / `I` (one column) | Approved |
| High-security fence post | F3.1 | Iron | `.Y.` / `IRI` / `III` | Approved |
| Wire Spool | F3.1 | Iron depth (iron, copper) | `CIC` / `CKC` / `CIC`. Out 4. A fence span costs ceil(distance ÷ 20 × post height) spools. A pylon cable costs ceil(length ÷ 20) | Approved |
| Fence Remover | F3.1 | Iron | `I.I` / `.I.` / `K.K` | Approved |
| Fence switch | F3.1 | Iron | `.E.` / `IRI`. E = lever | Approved |
| Large Generator | F3.2 | Gold | `OOO` / `BXB` / `III`. B = Basic Generator, X = block of redstone | Approved |
| Pylon head | F3.2 | Iron depth (copper) | `CAC` / `.I.`. A = lightning rod. It sits on top of a fence pole (D240) | Approved |
| Small Carnivore Feeder (`small_carnivore_feeder`, was `carnivore_feeder_one`) | F3.3 | Iron | `ITI` / `IZI` / `IPI` | Approved |
| Large Carnivore Feeder (`large_carnivore_feeder`, was `carnivore_feeder_two`) | F3.3 | Iron | `TIT` / `IXI`. X = Small Carnivore Feeder | Approved |
| Low Herbivore Feeder (Feeder 5) | F3.3 | Iron | `ITI` / `IFI` / `IPI`. F = flower pot | Approved |
| Medium Herbivore Feeder (Feeder 3) | F3.3 | Iron | `IPI` / `IXI`. X = Low Herbivore Feeder | Approved |
| Tall Herbivore Feeder (Feeder 4) | F3.3 | Iron | `IPI` / `IXI`. X = Medium Herbivore Feeder | Approved |
| Very Tall Herbivore Feeder (Feeder 6) | F3.3 | Iron | `IPI` / `IXI`. X = Tall Herbivore Feeder | Approved |
| Live-Bait Feeder (Feeder 1 with a pole) | F3.3 | Iron | `IIZ` / `I..` / `IXG`. X = Small Carnivore Feeder, G = any fence gate (`#minecraft:fence_gates`) | Approved |
| Wrench (new item) | F4.2 | Iron depth (copper) | `.I.` / `.CI` / `I..`. Three iron and one copper, as the lead approved. It moves an assembled vehicle to its next variation (D204) | Approved (Z-8) |
| Vehicle chassis (both vehicles) | F4.2 | Iron | `.I.` / `IMI` / `.I.`. M = minecart | Approved (Z-8, grid shape approved 2026-10-13) |
| Vehicle wheel (both vehicles) | F4.2 | Iron (slime) | Shapeless: slime block + iron ingot. Out 4, one full set per craft | Approved (Z-8) |
| Vehicle engine (both vehicles) | F4.2 | Iron | `.P.` / `IFI` / `IRI`. P = piston, F = furnace | Approved (Z-8) |
| Vehicle seats (both vehicles) | F4.2 | Surface | `H.H` / `HHH`. H = leather | Approved (Z-8) |
| Jeep body | F4.2 | Iron | `IQI` / `ITI` / `I.I`. About 10 iron value and 1 glass pane | Approved (Z-8, grid shape approved 2026-10-13) |
| Explorer body | F4.2 | Iron | `QQQ` / `IBI` / `TIT`. B = block of iron (`#c:storage_blocks/iron`). About 20 iron value and 3 glass panes | Approved (Z-8, grid shape approved 2026-10-13) |
| Tour Computer (Explorer only) | F4.2 | Gold (1 ingot) | `RXR` / `.O.`. R = redstone, X = redstone comparator, O = gold ingot | Approved (Z-8) |
| Tour track | F4.2 | Iron | `ISI` / `IRI` / `ISI`. Out 16. A powered-rail style guide strip | Approved (Z-8, grid shape approved 2026-10-13) |
| Tour stop marker | F4.2 | Iron | Shapeless: tour track + redstone lamp. The lamp lights while an Explorer stands at the stop. An anvil-style name field names the stop (D208) | Approved (Z-8) |

**Progression gates [lead 2026-10-13]:**

| Stage | The player needs | It unlocks |
| --- | --- | --- |
| 1. Surface | Wood, stone, sand, leather, string | Fossil pouch, test tubes |
| 2. Iron, copper, redstone (mining to about y 15) | Iron, copper, redstone, glass, smooth stone | Syringe, Basic Generator, conduit, Hard Drive, Iron Filter and the iron upgrades. Every fence item, the pylon head, all seven feeders, and every machine part that has no gold and no quartz slab. The Wrench, the tour track, the stop marker, and every vehicle part except the Tour Computer. The wheel also needs a slime block |
| 3. Gold and the Nether (gold from deep mining, badlands or the Nether. Quartz from the Nether) | 4 gold ingots and 14 quartz slabs (about 28 nether quartz) | The four precision parts and the five quartz-slab parts, so the first full lab. Also the gold upgrades, the Large Container, the Large Generator and the Tour Computer |
| 4. Diamond | Diamonds and more gold | Diamond chip, diamond tank, diamond filter |
| 5. Nether fortress and ancient debris | Blaze powder, 1 netherite ingot, more nether quartz | SSD, Leveling Sensor, Hot Bulb, Netherite Tank Upgrade |

- The first lab needs a Nether trip for quartz. The lead accepted this gate on 2026-10-13 (D256).
- A player can start a park before the lab is complete, because every fence and feeder needs only stage 2.
- Trade route: the paleontologist sells chips, the Hard Drive, the Gold Filter and lab machine parts (F4.1). A player with emeralds can skip the gold and quartz gate. The no-trade route of F1.12 stays possible.

**Not crafted.** The voltage sign has no item: an empty-hand click hangs it (D78 gap 1). Beta has no lysine item (D170), no decorative fences (D78 gap 2) and no workstation block (D209). These items have no recipe by design: the four assembled-machine items, the Creative Generator and the Creative Fence Remover (creative only), the five `dev_*` items, fossils and amber (world drops), and machine outputs (DNA tube, the Embryo Syringe and the dinosaur-embryo syringe, eggs, the cracked artificial egg). F1.12 and F3.4 give each a creative-only mark or a source note. The assembled Jeep and Explorer have no recipe either. The player builds them from parts (F4.2).

**Gaps that blocked the recipe design (all closed).** The lead answered these in register row D78. The lead answered all nine gaps on 2026-10-12 and approved the recipe draft on 2026-10-13. The lead set the M4 rows on 2026-10-13 (Z-8, D250), so no recipe row stays open:

1. Voltage signs: answered. No sign item. An empty-hand click hangs the sign and cycles its three looks (F18).
2. Decorative fence variants: answered. None in Beta.
3. Plant matter: answered. Plant matter is an item tag list in config: leaves, saplings, wheat, kelp, grass and flowers, also of other mods (D134).
4. Chips: answered. The 3 shared chips fit the Processor, the Sequencer and the Egg Printer. Synthesis gets a diamond chip time (D113).
5. Tanks: answered. No emerald tank and no netherite Processor tank in Beta (D143). The Sequencer netherite tank of the spec stays.
6. Upgrade limit: answered. The design-doc cap of 3 per machine stays, and it limits nothing today (D117).
7. Part names: answered. The video names (Base, Needle, Platform, Body) stay (D114).
8. The artificial egg: answered. It stays only as the failed-print result, the cracked artificial egg (D118).
9. Filter durability: answered. Iron 150, gold 250 and diamond 500 runs, as in the original (D124). The mod gives 100 to all three today.

---

## Art

The Art list tab replaces the old asset matrix. It lists every Beta and Release 1 asset by thing, with its status. Art status lives only in the Art list. This backlog holds no art counts and no asset status. Each work package names its Art list sections in its "Art" line. A package is not done until those rows are Done (no Placeholder, no To check).

---

## Conflicts for the lead

These items need a lead decision before they can enter the plan. Each bullet gives both texts and the source. Until the lead decides, the plan text applies. The M2 open-items pass of 2026-10-12 resolved RC23 (D46) and made RC9 (D48) moot. The final pass of 2026-10-13 resolved RC32 (D52). No register row has the status "Conflict" now. The flags of the final pass are at the end of this section.

**Reconciliation items held back.** These items touch lead decisions in `docs/codebase-analysis.md` section 11. Section 11 wins, so the work package text does not contain them (reconciliation fold-in, section 3, C1 to C5).

- **RC23, hyper-intelligent animals and fences (F2.7, F3.1).** Reconciliation: an animal with hyper intelligence walks to an unpowered section and climbs over the wire. Plan: fence parity F27 blocks every wire node, and only the F29 Tyrannosaurus walks to a fence. Section 11.11 says only that such an animal notices the unpowered fence. Register row D46. Resolved 2026-10-12 by the lead: a hyper-intelligent animal that notices an unpowered fence walks to the dead section and climbs out. This is a lead-approved exception to fence parity F27. Other animals keep away from fences (F2.7, F3.1).
- **RC20, lab hatch rate (F1.9).** Reconciliation: a lab egg hatches at `incubation.lab_hatch_success` (0.9), less the edit risk. A nest egg keeps the card rate. Plan and section 11.7: the placed lab egg uses `SpeciesData.hatch_success` (Tyrannosaurus 5 %). At 5 %, one Tyrannosaurus costs about 17 hours of machine time, against the 50-minute target. Register row D41. Resolved 2026-10-12 by the lead: a lab egg uses a lab hatch rate per species. The species data holds it, apart from the nest (card) rate. Both are config values, and playtests tune them. A nest egg keeps the card rate (F1.9).
- **RC32, social need (F2.4).** Reconciliation: the social need is the company input of stress, with no separate bar. Plan and section 11.10: the social need is a need of its own. Register row D52. Resolved 2026-10-13 by the lead: loneliness is part of stress, with no bar of its own. Being below the group minimum adds stress over time. This follows the reconciliation and supersedes section 11.10 "the social need is a need of its own" (F2.0, F2.4).
- **RC9, scent gene (F1.5).** Reconciliation: add a `scent` gene for the food, water and scent-trail sensor ranges. Map the table's Hunger row to `stomach_capacity` and its Thirst row to `thirst`. Plan and section 11.6: the lead's gene groups have no `scent` gene. Register row D48. Moot since 2026-10-12: the lead dropped scent from Beta, so no scent gene is needed.
- **RC31, cosmetic graze (F2.4).** Reconciliation: a herbivore with an eat clip plays a graze at its herd spot and eats no block. Plan and sections 11.9 and 11.10: eating removes the block and needs head contact. Resolved 2026-10-12 by the lead: idle grazing is in, with a small hunger gain. A content or idle herbivore grazes a block in its reach and gains `needs.idle_graze_hunger`. By default the block stays. A hungry animal still eats and removes the block (F2.0, F2.4).

**JC3 survey against the reconciliation.**

- **Activity window values (M0.B00, F2.4).** Reconciliation RC1: `activity_window` is diurnal, nocturnal or cathemeral. JC3 survey J7: sleep windows for diurnal, nocturnal, and dawn and dusk (18,000 to 22,000 and 6,000 to 10,000, an inference). The plan uses the RC1 values. F2.0 proposes the JC3 windows as config. Register row D98. Resolved 2026-10-12 by the lead: each cathemeral species has its own two windows (F2.0). The old "dawn and dusk" label was wrong, because its values were midnight and noon.

**Follow-up pass of 2026-10-12.**

- **Lost sleep and stress (F2.0, F2.4).** The M2 pass said that each missed window adds one stress step. A later answer did not pick sleep as a stress input. Resolved 2026-10-12 by the lead: lost sleep gives no stress. It caps rest at 50 and halves stamina regeneration (D105).
- **D33, map search distance (F4.1).** The question to the lead named a default of 1,000 blocks. Register row D33 holds 1,600 blocks, the vanilla `/locate` radius. Resolved 2026-10-13 by the lead: 1,000 blocks.
- **D36 and herd decision 8 (F2.5).** D36 lets a thirsty or hungry member step off the line. Herd decision 8 lets a follower leave the line only at an urgent need. Resolved 2026-10-13 by the lead: a detour starts only at the urgent band, as herd decision 8 says.
- **D61 and the safety part (F2.0).** D61 now keeps the hurt-place memory for 1 in-game day. The contentment safety part and the healthy test used the same 2,400-tick memory. Resolved 2026-10-13 by the lead: the safety part keeps 2,400 ticks, apart from the D61 memory (D229).
- **D29 switches (M0.B00).** The lead first picked only the breeding switch, then confirmed it on a second ask. No conflict remains. The vehicle, herd-travel and worldgen switches are gone.

**JC3 flags** (JC3 fold-in, "Flags for the reviewer"):

- **Recipe time cap against incubation (F1.8, F1.10).** JC3 B12.26 caps a recipe job at 32,767 ticks (about 27 min), because `ContainerData` sends 16-bit values. Today the Incubator takes 18 s per percent, so 36,000 ticks for 100 % (section 4.4). The plan syncs Incubator progress as a fraction (F1.8). This is an inference, not a survey finding.
- **Park decoration sets (backlog).** The JC3 survey "Post-Beta" table puts them in "Release 1 (R03)". Plan R03 holds only fossil sets, digsite data and gene data. The plan puts the sets in the backlog as a JC3 candidate.
- **Growth steps (F2.1).** JC3 J4 step 5 refreshes stats when growth "passes a size or stat step". F2.1 says "No step thresholds". The plan uses the 0.01-block refresh threshold of JC3 B16.11, so growth stays smooth. This resolves the difference.

**Final pass of 2026-10-13: flags for the lead.** The lead resolved each flag on 2026-10-13 (open-items batch Z-8).

- **T. rex nest hatch rate (F2.5, D79).** The T. rex card gives 5 %. The lead's rule "raise each drafted rate by × 1.25" makes it 6 %. Resolved: the lead accepted 6 %.
- **Dilophosaurus period (F1.1, D254).** The lead calls the Dilophosaurus Triassic. Board #4 dates it 201.3 to 182.7 Ma, the earliest Jurassic. Resolved: the Dilophosaurus uses the Jurassic period rarity, 2.8, and stays at y −20 to 30.
- **Pole height and fence parity (F3.1, F3.2, D253).** The lead lets the player set the height of each pole. Fence parity keeps posts of 3 and 8 blocks. Resolved: only a pole used as a pylon takes any height. A fence post keeps 3 blocks (low) and 8 blocks (high).
- **Lab cost (F1.12, D78).** The plan read "about 45 iron and 4 gold" as the whole first lab. So the four machines cost about 28. Resolved: the four machines alone cost about 45 iron value and 4 gold ingots. The whole first setup costs about 60. "Recipes and progression gates" holds the re-costed grids.
- **Heredity of raptor looks (F2.2, F2.5, D249).** No rule set how a Nublar × Sorna young gets its crest and quills. Resolved: each trait (base skin set, crest, quills) comes from a random parent, one roll per trait.

---

## Decisions register

The implementation needs these decisions, and section 11 does not give them. D97 to D108 come from the F2.0 package. D109 to D112 follow them. D113 to D148 come from the M1 open-items pass of 2026-10-12. D149 and later come from the M2 open-items pass of 2026-10-12. D183 and later come from the M3 open-items pass of 2026-10-12. D204 and later come from the M4 and follow-up open-items pass of 2026-10-12. D219 and later come from the final open-items pass and the draft reviews of 2026-10-13. The lead answers. The work package's task plan records the answer. Do not start the dependent step until the lead answers. Ship a listed default as a config value, except in "Conflict" rows. The "Status" column splits the rows:

- "Answered" rows have a lead answer.
- "Blocked" rows stop their step until the lead answers.
- "Conflict" rows hold a proposal that section 11 overrides. Do not ship it. The plan text applies (see "Conflicts for the lead").
- "Lead decision pending" rows carry a default from this plan.
- "Survey default" rows carry a default from a survey or the reconciliation.

D15 to D35 come from the open items of the two surveys (O1 to O21). D69 to D75 come from the JC3 survey open items O22 to O28. Their defaults are survey recommendations, not lead decisions.

D40 to D68 come from the questions Q1 to Q29 of [ai-behaviour-genetics-reconciliation.md](../ai-behaviour-genetics-reconciliation.md). Their defaults are recommendations, not lead decisions. The lead answered D64 on 2026-10-10. D76 to D78 come from the overview sections and the recipe table. D79 comes from the outside review of 2026-10-12.

**Old module codes.** Earlier drafts of this register named old modules (B00 to B27, P0 to P4). The "Work package" column now gives the feature id. Each work package's "Old module" line maps the old code to its package. Survey ids such as B25.5 inside a survey tag stay as the survey wrote them.

### D01 to D39: plan and survey decisions

| Id | Work package | Question | Default if unanswered | Status |
| --- | --- | --- | --- | --- |
| D01 | F1.1 | How much more DNA does a special piece give than a bone of the same quality? | Answered 2026-10-12 (lead, follow-up pass): 1.5 × the DNA of a bone of the same quality, a config value. Special pieces serve DNA and trades only. No recipe uses them | Answered |
| D02 | F1.1 | What happens when no species is valid for an amber's or a fossil cell's depth and biome? | Answered 2026-10-12 (lead, follow-up pass): the nearest depth band. F1.1 first tries the other layers of the same cell (JC3 survey, J2) | Answered |
| D03 | F2.7 | Does sampling an egg with a syringe harm the egg? | Answered 2026-10-12 (lead): no. An egg sample does not harm the egg | Answered |
| D04 | F2.7 | What is the yield range for a syringe on a live dinosaur and on remains? | Answered 2026-10-12 (lead): a syringe draw from a live dinosaur gives 25 to 50 %. Remains (fresh bones, tissue) in the Fossil Processor give 50 % | Answered |
| D05 | F1.9 | Hatching: egg block or egg entity? | Answered 2026-10-12 (lead): an egg entity, not a block. Dinosaurs can push it (F1.9) | Answered |
| D06 | F2.1 | Multipart hitboxes in Beta, or one scaled box? | Answered 2026-10-09: section parts in Beta (F2.1 part B). See [dinosaur-hitboxes-and-movement-design.md](../dinosaur-hitboxes-and-movement-design.md). | Answered |
| D07 | F4.2 | Do vehicles use fuel? | Answered 2026-10-12 (lead, follow-up pass): no fuel. Damage and repair: D206 | Answered |
| D08 | F4.3 | Which kinds of gun, and which models ship? | Blocked | Blocked |
| D09 | BH.2 | Licence and art terms | Blocked. Do not change files. | Blocked |
| D10 | R01 | Does the paleobotanist villager ship in Release 1? | Answered 2026-10-12 (lead, follow-up pass): yes. It ships in Release 1 with the plants. This replaces the default "not shipped" | Answered |
| D11 | F2.2 | Approve the render-scale targets (Triceratops 3.0 blocks, Brachiosaurus 12.5) and the section 11.16 missing-stat draft | Answered 2026-10-12 (lead): use the height targets, Triceratops 3.0 blocks (scale 1.32) and Brachiosaurus 12.5 (scale 1.61). They are interim values until real cards exist | Answered |
| D12 | F2.1 | Real stat cards for five species | Answered 2026-10-12 (lead): the section 11.16 interim stats stay the working values | Answered |
| D13 | M0.B00, F2.6 | Flee or stand for Tyrannosaurus, Velociraptor, Dilophosaurus and Brachiosaurus? `stress_threshold` and `territory_radius` per species? | Answered 2026-10-12 (lead): one `stress_threshold` per species in the species data, default 50, no gender split. The Brachiosaurus defends with tail whip and stomp after a roar warning (D50). A personal-space radius replaces the territory radius (D165). Follow-up pass: the three carnivores stand or retreat by threat and group size. Each animal adds its pair's or pack's summed threat to its own. It stands unless the attacker's threat is more than 2 × (D112). This replaces "the carnivores always stand" | Answered |
| D14 | F2.3 | Animation speed that follows body size, and look-around clips on the second controller (review items ANI-6, ANI-2) | Not built. The lead answered "not decided" on 2026-10-06. | Blocked |
| D15 | F2.6 | How long does an awareness episode last before a dinosaur rolls again for the same player? | Answered 2026-10-12 (lead, follow-up pass): one notice roll every 10 s (200 ticks) for each player in range, `detection.episode_ticks` | Answered |
| D16 | F2.6 | Which sources drop scent points, at what spacing, and with what cap per chunk? | Moot 2026-10-12 (lead): scent is not in Beta. A predator that loses sight of its target searches the last seen spot. This reverses the decision of 2026-10-05 | Answered |
| D17 | F1.9 | Does a hatchling get an owner? | Answered 2026-10-12 (lead, follow-up pass): no owner. The hatchling stays wild until a player tames it | Answered |
| D18 | F2.6, F2.0 | Do owners who are not operators get a status readout of their dinosaur? | Answered 2026-10-12 (lead): yes. Every player who looks at a dinosaur sees its contentment band and its worst need in the hover box (F2.0). Visible cues show the state too. Exact numbers stay on the Debug Stick, in creative or for operators (F2.1) | Answered |
| D19 | F2.6 | Do vanilla villagers and livestock flee loose dinosaurs? | Answered 2026-10-12 (lead): on. A roar scares passive mobs and villagers near it for a few seconds. Villagers flee loose carnivores as they flee zombies. Config `vanilla.mobs_fear_dinosaurs`, on | Answered |
| D20 | F2.5 | Does breeding stop above a dinosaur count per area? | Answered 2026-10-12 (lead): no breeding cap | Answered |
| D21 | F1.11, F3.2 | What powers a fence when its generator's chunk is not loaded? | Answered 2026-10-12 (lead): the grid remembers power. While a generator's chunk is not loaded, its fence keeps its last power state. When the chunk loads, the generator burns fuel for the time that passed. No forced chunk loading. This replaces the survey default (only loaded generators count) | Answered |
| D22 | F3.2 | How long does a fence stay live after fuel ends? | Answered 2026-10-12 (lead): the survey default. A buffer equal to the sum of post buffers (350 FE each), as a config value. The fence fades over seconds | Answered |
| D23 | F3.1 | Does shock strength follow the grid's energy, and does a shock drain the grid (tech survey, spec T3)? | Answered 2026-10-12 (lead): no. The shock is a fixed ½ heart (1 HP) that ignores armour, as in parity (F25). A shock uses no energy | Answered |
| D24 | F1.11 | Which blocks accept energy from, or give energy to, other mods' cables? | Answered 2026-10-12 (lead): other mods' cables connect to generators and machines only. Generators give, and machines accept. Posts, pylons and conduits accept no outside cables | Answered |
| D25 | F4.2 | What stops the Explorer? How many seats? May a player board a moving Explorer? Is a timed Tour Station in Beta? | Answered in part 2026-10-12 (lead, M4 pass): stop markers and anything on the track stop the Explorer, and it honks. The Explorer seats 4. Players board at any time, also while it moves. Players can drive it by hand off the track. Jeep 2 seats stays the plan default. Answered 2026-10-13 (lead, final pass): no Tour Station in Beta. The player starts each tour by hand | Answered |
| D26 | F4.2, F3.1 | What happens when a vehicle meets an electric fence? | Answered 2026-10-12 (lead): a live fence stops a vehicle like a wall and shocks the driver once. This replaces the survey default (no shock) | Answered |
| D27 | F1.10, F1.11 | Is the hover box always on? Is a power meter block in Beta? | Answered 2026-10-12 (lead): the hover box is always on, with a client setting. No comparator output and no power meter block in Beta. This replaces "Generators give comparator output". Follow-up pass (asker's reading of D28): no comparator output on machines and feeders either | Answered |
| D28 | Backlog | What does the control room have in Beta? | Answered 2026-10-12 (lead, follow-up pass): the fence switch only. Alarms, cameras and the status board come after Beta. No comparator outputs (D27) | Answered |
| D29 | M0.B00 | Which systems may a server owner disable? | Answered 2026-10-12 (lead, follow-up pass): only a `breeding` switch, confirmed on a second ask. No vehicle, herd-travel or per-feature worldgen switches. This replaces the survey default | Answered |
| D30 | F1.5 | Are gene definitions datapack JSON in Beta? | Answered 2026-10-12 (lead, follow-up pass): gene effects in code (a Java registry) for Beta. Data-pack editing after Beta | Answered |
| D31 | F1.1 | Where do species and piece of a fossil live? How many opacity steps does the overlay have? | Answered 2026-10-12 (lead, follow-up pass): 4 fossil fade levels: 15, 43, 72 and 100 % opacity. This refines the M1 answer "random opacity 15 to 100 %". The block layout stays the survey default: one block per host stone and species, about 4,600 states | Answered |
| D32 | F1.4 | What is the largest DNA drive the design expects? | Answered 2026-10-12 (lead, follow-up pass): tests use drives with 1,000 entries. Players have no entry limit | Answered |
| D33 | F4.1 | Which distance does the game guarantee for a digsite? | Answered 2026-10-13 (lead, final pass): a map finds a digsite within 1,000 blocks. This replaces the survey default of 1,600 blocks (the vanilla `/locate` radius) [tech survey O19] | Answered |
| D34 | After M0.P2 | Does the roadmap include optional compat modules? | Answered 2026-10-12 (lead, follow-up pass): a ComputerCraft (CC: Tweaked) peripheral after F3.1. Create support and Ponder guides wait for 26.2 builds. The guide book stays in the backlog | Answered |
| D35 | F3.1, F2.5 | Do vanilla and decorative fences hold dinosaurs? | Answered 2026-10-12 (lead): vanilla fences and walls hold dinosaurs with normal collision. Dinosaurs cannot break them. Beta has no decorative fences (gap 2 of D78) | Answered |
| D36 | F2.5 | Which other unplanned trips are exceptions to herd decision 16, besides fight chases? | Answered 2026-10-12 (lead, follow-up pass): need detours. Fight chases are also excepted. Answered 2026-10-13 (lead, final pass): a member steps off a journey only at the urgent band (below 25 %), not at "wants". This matches herd decision 8 | Answered |
| D37 | M0.P0 | How does the uncommitted herd work on `kash's-slop` enter M0.P0, under the lead's no-commit rule? | Resolved: the lead committed the herd work on `kash's-slop` on 2026-10-11 (4e90174, 582fbb0). M0.P0 is not blocked. The worktree `herd-leader-sample` keeps 65 uncommitted files as a backup | Answered |
| D38 | F3.1 | Who can climb an unpowered wire? | Answered 2026-10-10 ("1:1"): every living entity, as in 1.12.2 (fence parity F26) | Answered |
| D39 | F3.1 | Do post lamps light post by post or at once? | Answered 2026-10-10 ("1:1"): post by post, as in 1.12.2 (fence parity F31) | Answered |

### D40 to D79: reconciliation, JC3 survey and overview questions

| Id | Work package | Question | Default if unanswered | Status |
| --- | --- | --- | --- | --- |
| D40 | F2.5 | What does a fenced herd do for journeys? (reconciliation, Q1) | Answered 2026-10-12 (lead, follow-up pass): a fenced herd picks journey targets inside its paddock only. A bounded fill finds the area (6,000 blocks, T12). Open world keeps 156-314 blocks | Answered |
| D41 | F1.9 | Does the card's hatch success (T. rex 5 %) apply to lab eggs? (reconciliation, Q2) | Answered 2026-10-12 (lead): no. A lab egg uses a lab hatch rate per species, in the species data, apart from the nest (card) rate. Config values, tuned in playtest. Start values: D147. RC20 resolved | Answered |
| D42 | F2.7 | Which Beta species can take camouflage and thermal detection? (reconciliation, Q3) | Answered in part 2026-10-12 (lead): thermal detection for T. rex, Velociraptor and Dilophosaurus. Bioluminescence for all six. Follow-up pass: camouflage for T. rex, Velociraptor and Dilophosaurus | Answered |
| D43 | M0.B00, F2.7 | Do Velociraptor and Dilophosaurus carry hyper intelligence and poison from hatch? (reconciliation, Q4) | Answered 2026-10-12 (lead): every Velociraptor carries hyper intelligence from hatch. Special Genes wins over the plan default. Every Dilophosaurus carries poison from hatch (D181) | Answered |
| D44 | F1.5 | What is "abnormal behaviour"? Does it reuse the F2.7 quirks? (reconciliation, Q5) | Answered 2026-10-12 (lead, follow-up pass): a separate list (M0.B00 `risk_behaviours`). A risky genome may roll one at hatch. The list: stresses easily (threshold −20), flipped day and night, loner (maximum group 1), more aggressive (+30 %, still warns) | Answered |
| D45 | F1.5 | Which stats trade off? (reconciliation, Q6) | Answered 2026-10-12 (lead, follow-up pass): all five pairs. Speed up lowers maximum health, strength up raises hunger, size up lowers speed and raises hunger, defense up lowers speed, health up lowers regeneration. They grow with donor share and edit count | Answered |
| D46 | F2.7, F3.1 | What does a hyper-intelligent animal do when it notices an unpowered section? (reconciliation, Q7) | Answered 2026-10-12 (lead): it walks to the dead section and climbs out over the wire (F26). This is a lead-approved exception to fence parity F27. Other animals keep away from fences. RC23 resolved | Answered |
| D47 | F1.5 | Can a jump edit clear a low fence? (reconciliation, Q8) | Answered 2026-10-12 (lead, follow-up pass): yes, reversed. A strong jump edit can clear the 3-block low-security fence. High fences still hold. The jump cap stays below the high-fence height | Answered |
| D48 | F1.5 | Add a `scent` gene for the table's scent effects? (reconciliation, Q9) | Moot 2026-10-12 (lead): scent is not in Beta, so Beta needs no `scent` gene. RC9 is moot | Answered |
| D49 | F1.5, F2.4 | Does the nocturnal gene shift the window? (reconciliation, Q10) | Yes, one step at plus or minus 50 %. The lead's own windows for shifted herbivores: D182. Answered 2026-10-12 (lead): one step at plus or minus 50 %, and smaller values shift the window edges by up to 2 hours | Answered |
| D50 | M0.B00, F2.6 | Flee or stand for Brachiosaurus (D13) (reconciliation, Q11) | Answered 2026-10-12 (lead): the Brachiosaurus defends. A roar warning (its bellow), then a tail whip and a stomp. It does not flee | Answered |
| D51 | F2.7 | Special-gene slots: one, or 1-3 by sequence percent? (reconciliation, Q12) | Answered 2026-10-12 (lead): 1 slot at 50 %, 2 at 75 %, 3 at 100 % DNA, as the design doc says | Answered |
| D52 | F2.4 | Is the social need the same as the company input? (reconciliation, Q13) | Answered 2026-10-13 (lead, final pass): no. Loneliness is part of stress, with no bar of its own. Below the group minimum, stress rises over time. The hover box shows "lonely" when it is the main cause. The company part of contentment reads the group size. "Alone" becomes urgent after 3 in-game days (D106). This supersedes section 11.10 "the social need is a need of its own" | Answered |
| D53 | F2.5 | Velociraptor leap with the existing `pounce` clip, without a crouch? (reconciliation, Q14) | Answered 2026-10-12 (lead, follow-up pass): both. Raptors leap now with the existing pounce clip. A crouch-then-leap clip goes on the Art list for later | Answered |
| D54 | F2.5 | Do T. rex pairs and Dilophosaurus groups hunt together? (reconciliation, Q15) | Answered 2026-10-12 (lead, follow-up pass): T. rex pairs hunt as a team: one chases, one circles to cut off. Dilophosaurus groups hunt together. This reverses the default "Dilophosaurus hunts alone" | Answered |
| D55 | F2.4, F2.5 | When does a pack give up? (reconciliation, Q16) | Answered 2026-10-12 (lead, follow-up pass): accepted. When the leader stops, the target is lost, or under half the members still chase. No flight from a lost hunt in Beta | Answered |
| D56 | F2.6, F2.5 | Do packs make herd journeys? (reconciliation, Q17) | Answered 2026-10-12 (lead, follow-up pass): only when hungry, to find prey. Otherwise they wander their home area. This replaces the default "no journeys" | Answered |
| D57 | F2.0, F2.1, F2.5, F3.3 | Does dominance set the feeding order? (reconciliation, Q18) | Answered 2026-10-12 (lead): yes, for every species and every source (feeders, carcasses, plants). Within a group the higher dominance eats first, then the longest wait. The fixed "at most 3" goes: the source's size and the eaters' body widths set how many eat at once (F2.0). Values: D111 | Answered |
| D58 | F2.4, F2.5 | Can a full pack take prey a lone hunter avoids? (reconciliation, Q19) | Answered 2026-10-12 (lead, follow-up pass): yes. Pack threat sums the adults, capped at 4x the leader | Answered |
| D59 | F2.5 | Build stalking now with the walk clip, or wait for crouch art? (reconciliation, Q20) | Answered 2026-10-12 (lead, follow-up pass): now, with the walk clip at half speed, until crouch art exists | Answered |
| D60 | F2.5 | Is ambushing in Beta, and for which species? (reconciliation, Q21) | Answered 2026-10-12 (lead, follow-up pass): yes. Hungry Velociraptor and Dilophosaurus hide in cover by paths and spring out | Answered |
| D61 | F2.4, F2.6 | Do animals remember where they were hurt? (reconciliation, Q22) | Answered 2026-10-12 (lead, follow-up pass): yes, a soft path cost for a full in-game day (24,000 ticks). This replaces 2,400 ticks. Answered 2026-10-13 (lead, final pass): the safety part of contentment is a separate rule. It recovers 2 minutes (2,400 ticks) after a hit (D229) | Answered |
| D62 | F2.4 | May a starving animal eat its own species? (reconciliation, Q23) | Answered 2026-10-12 (lead, follow-up pass): yes, at the card weight, never its own group or family | Answered |
| D63 | F2.5 | Do alarms cross species, with no mixed groups? (reconciliation, Q24) | Answered 2026-10-12 (lead, follow-up pass): yes. Any herbivore that hears an alarm becomes alert and reacts by its own rule. No mixed groups | Answered |
| D64 | F1.2, F1.5 | Which 26.2 animals with no table entry give DNA (allay, strider, tadpole, happy ghast, sulfur cube)? (reconciliation, Q25) | Answered 2026-10-10: none of them. They give no usable DNA | Answered |
| D65 | F1.5 | Do mob variants change genes, or colour only? (reconciliation, Q26) | Answered 2026-10-12 (lead, follow-up pass): colour only | Answered |
| D66 | F1.5 | Are the unsigned table values plus? (reconciliation, Q27) | Answered 2026-10-13 (lead, final pass): yes. An unsigned donor-table value is an increase (+) | Answered |
| D67 | F1.5, F2.2 | What does "green unlocks Delta" (parrot) mean? (reconciliation, Q28) | Answered 2026-10-12 (lead, follow-up pass): green parrot DNA gives raptors a green colour shift. No skin has the name "Delta" | Answered |
| D68 | F1.5 | Do effects that only the older design doc lists still apply? (reconciliation, Q29) | Answered 2026-10-12 (lead, follow-up pass): the cosmetic extras stay (turtle greener skin, rabbit raises the albino chance). Stat numbers come from the new table only. Mule transport capacity waits for item carrying (F2.7 quirks) | Answered |
| D69 | F2.2 | Rule OC6 forbids a core shader, and the bake spec composites colour layers on the CPU. JC3 draws four tints in one pass with one core shader and a 32 KB shared tint table. Does OC6 keep its place, or may F2.2 test one creature shader? | Answered 2026-10-12 (lead, follow-up pass): pre-baked layer textures (CPU composite), no core shader. It works with shader packs | Answered |
| D70 | BH.1, F2.5 | Entity survey rule B25.5 forbids tick mixins on vanilla base classes. JC3 presizes the visited-block set of a vanilla sweep method through a required mixin. Is a small mixin with no tick allowed on a vanilla hot path? | Answered 2026-10-12 (lead, follow-up pass): measure Brachiosaurus movement on 26.2 first. Patch vanilla code only if the tests show lag | Answered |
| D71 | F2.5 | When does a bred young get its genome: at mating, at laying or at hatch? | Answered 2026-10-12 (lead, follow-up pass): at laying. The egg stores the parents' mix, and a player can sample it. A small variation rolls at hatch | Answered |
| D72 | F2.5 | Do two more gates join the lead's three breeding gates (nest, fertility, stress)? The gates are full health, and no own young under half grown within 16 blocks. | Answered 2026-10-12 (lead, follow-up pass): yes, two extra gates: full health, and no own young under half grown within 16 blocks. This reverses the default "no extra gates" | Answered |
| D73 | F1.5, F1.8 | Does the Incubator get a sex lever at hatch time, like the JC3 temperature slider? | Answered 2026-10-12 (lead, follow-up pass): no lever. Gender comes only from the gene rule | Answered |
| D74 | F1.1 | Does a fossil need a jacket step? A bare fossil drops nothing until the player encases it. Then a machine washes it. | Answered 2026-10-12 (lead, follow-up pass): no jacket step. Fortune and Silk Touch as F1.1 plans | Answered |
| D75 | F4.2 | Does a moving vehicle hurt the mobs and players that it hits? | Answered 2026-10-12 (lead, M4 pass, asker's reading: the lead picked both options): a slow vehicle pushes mobs and players aside with no damage. A fast vehicle hurts what it hits, scaled by speed. The speed threshold is a config value | Answered |
| D76 | Overview | Which species should a new player clone first? No rule makes one easier to reach or to keep. | Answered 2026-10-12 (lead, follow-up pass): no first-species rule | Answered |
| D77 | Overview | What is the long-term goal after the park runs (stage 6)? Section 11 has no endgame, park rating or milestone. | Answered 2026-10-12 (lead, follow-up pass): no endgame in Beta. Beta is a sandbox | Answered |
| D78 | F1.12, F3.4, every package with recipe rows | Which vanilla materials gate each machine, part and upgrade? See "Recipes and progression gates", including its nine gaps. | Answered 2026-10-12 (lead): all nine gaps. Answered 2026-10-13 (lead, final pass): the lead approved Claude's recipe draft with changes. Lab a third cheaper (about 45 iron value and 4 gold). Upgrades crafted fresh. Feeders build on the one below. Copper for wiring. Blaze powder in the Hot Bulb. Nether quartz in the SSD and the Leveling Sensor. Netherite only in the Sequencer tank. Test tubes 3 and Identification Tag 1 per craft. The recipe book unlocks lab parts on the first fossil or amber. Grids: "Recipes and progression gates" | Answered |
| D79 | M0.B00, F1.9, F2.5 | Clutch and hatch success for five species. Section 11.16 sets no value for Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus and Gallimimus. | Answered 2026-10-13 (lead, final pass): clutch and nest hatch rate per species. T. rex: 12 to 24 at 6 %. Velociraptor: 8 to 13 at 25 %. Dilophosaurus: 12 to 24 at 16 %. Gallimimus: 15 to 30 at 15 %. Triceratops: 15 to 25 at 11 %. Brachiosaurus: 15 to 30 at 6 %. The lead raised each drafted rate by × 1.25. The T. rex card gives 5 %, and the lead accepted 6 % on 2026-10-13 (Z-8). Config values, tuned in playtest | Answered |

**Design alternatives.** The entity survey lists 16 design alternatives in its section "Gameplay loops, pacing and design alternatives". They are options for the lead. None is decided, and none changes a decision above.

### D80 and later: rebuild of 2026-10-12

These rows come from the rebuild of this plan by milestone, with the design requirements catalogue, the video parity reference, the original machine animation spec, the Art list and the skin layer audit. D95 and D96 come from the lead review of the drafts on 2026-10-12. The lead's 2026-10-12 decisions settled the processor run time: the video and the spec both give 4 minutes, so the design doc's 25 minutes per stack has no row.

| Id | Work package | Question | Default if unanswered | Status |
| --- | --- | --- | --- | --- |
| D80 | F1.8 | How much light does a working Incubator give at each bulb tier? GH#23 asks for light that rises with each upgrade and gives no numbers. | Answered 2026-10-12 (lead): 9 with the base lamp, 11 warm, 13 warmer, 15 hot, only while it incubates. Config values | Answered |
| D81 | F1.9 | How often and how far does an egg wobble while it incubates? No source gives numbers, and the original had no egg wobble. | Answered 2026-10-12 (lead): about 4° side to side at a random moment every 30 to 90 s. The egg in the claw does not rock. Before the hatch, a 60 s shake in 3 stages of 20 s. Config values | Answered |
| D82 | F1.9, F1.7 | Which of the five egg size classes does each species use? | Answered 2026-10-12 (lead), by asset repo model: Brachiosaurus `round`, Tyrannosaurus `tyrannosaurus`, Triceratops `tall_3`, Gallimimus `normal`, Velociraptor and Dilophosaurus `velociraptor`. A natural (bred) egg has the colours of its species on that model. The Egg Printer prints the model with the printed texture. Artist questions: Art list question 9 | Answered |
| D83 | F1.1, F1.2, F1.4 | What do the DNA states Unidentified, Identified and Full mean, and what does the Identification Tag do? The design doc lists the states. The 2020 board names the tag with no rules. | Answered 2026-10-12 (lead): only an amber tube starts Unidentified, with its species hidden. A fossil tube and a DNA syringe show their species. A tag and an Unidentified tube in a crafting grid give the Identified tube, and the crafting uses up the tag. Synthesis output is Full. This replaces "the processor shows the amber species on the tube" (section 11.2). Answered 2026-10-13 (lead, Z-8): the lead removed the tag from Beta. "The amber tube does not receive a species ID, the contained DNA only gets identified once it gets sequenced." An amber tube stays Unidentified. The Sequencer names the species on the drive entry. No tag item, no tag recipe and no Identified tube recipe | Answered |
| D84 | F1.1 | Fossil pouch: what does "combines fossils" mean, and how does the player take fossils out? | Answered 2026-10-12 (lead): the pouch merges equal fossils (same species, piece and quality). It works from any inventory slot. Use opens a 27-slot screen. Hoppers and the Fossil Processor can pull fossils out. Manual insert: D120 | Answered |
| D85 | F1.3, F1.4, F1.6, F1.7, F1.8 (hook in F1.10) | How does each upgrade change the look of its machine? Section 11.8 says that upgrades change the look. The current models are the target [lead 2026-10-12]. They have parts for the Processor tank tiers only. | Answered 2026-10-12 (lead): every upgrade changes the look, with no new part. Two upgrades use parts: the Processor tanks (`base_tanks` to `diamond_tanks`) and the Sequencer chips (the 4 chip cubes, F1.6). Incubator bulbs light the lamp cap (D80), and Sequencer dye stays. Every other upgrade gets a texture swap on an existing bone: Processor chips, Sequencer tanks, Egg Printer chips and Leveling Sensor, Incubator containers and plant tanks. The artist proposes each bone, and the lead approves it | Answered |
| D86 | F3.3 | Feeder 1 is the Carnivore Feeder. Does Feeder 2 also ship, as a second look of the Carnivore Feeder or as its own block? Which of Feeders 3 and 4 is the Live-Bait Feeder (Feeder 3 has cage panels), and what is the other one? Ask the artist. | Answered 2026-10-12 (lead): each feeder is a block of its own. Feeder 1 is the Small Carnivore Feeder. Feeder 2 is the Large Carnivore Feeder, with more feeding spots. Feeders 5, 3, 4 and 6 are the Low, Medium, Tall and Very Tall Herbivore Feeders. The Live-Bait Feeder is Feeder 1 with a pole and a chain (new art). The Fish Feeder is not in Beta | Answered |
| D87 | F2.2 | Are the T. rex "Big Eatie" and "Little Eatie" sets in Beta? Both sets are in the asset repo with no gender split (Art list → Tyrannosaurus). | Answered 2026-10-12 (lead): not in Beta | Answered |
| D88 | F2.4 | The design doc says males stress at 60 and females at 50, "keep stress levels above 50", and a tameable dinosaur has 0 stress. Does the threshold differ by gender, and which way does the scale run? | Answered 2026-10-12 (lead): one `stress_threshold` per species, default 50, no gender split. Higher stress is worse. The design doc's "keep above 50" is a typo. Taming uses the trust rule (D163) | Answered |
| D89 | F2.2 | Is the Dilophosaurus JWD set one of the Beta "JW sets"? | Answered 2026-10-12 (lead): yes, the JWD set is in Beta. Its art is Missing (Art list → Dilophosaurus) | Answered |
| D90 | F2.2, F2.3 | Does the Velociraptor Sorna set use its own JP3 model and the 2020 board's Sorna clip list (21 clips, adds Headshake), or the Nublar model? | Answered 2026-10-12 (lead): its own JP3 model (`velociraptor_adult_JP3.bbmodel`) and the 2020 board's Sorna clip list, 21 clips with Headshake | Answered |
| D91 | F2.3 | The 37 T. rex sounds in the asset repo are byte copies of the `distortus_rex` folder. Which folder holds the real T. rex recordings? Ask the artist. | The 37 files count as Placeholder until the artist names the real folder. F2.3 is not done until then. | Lead decision pending |
| D92 | F2.3 | When does an animal limp? Limp is in Beta: the 2020 board lists it for every Beta species. No Beta behaviour triggers it yet. | Answered 2026-10-12 (lead): limp replaces walk below 30 % health (config, tuned in playtest). The T. rex and the Triceratops use their injured clip until each species has its own limp clip | Answered |
| D93 | F1.6 | How much water, sugar, bone matter and plant matter does one synthesis use? The spec gives tank sizes only. The video shows Sugar 48 to 40 and Plant Matter 60 to 58 during one run. | Answered 2026-10-12 (lead): the F1.6 task plan reads the video at 7:20-7:35 and ships the values as config. Playtests tune them | Answered |
| D94 | F2.2 | The Velociraptor card lists two quill sets: "with quills" and "with arm quills". On 2026-10-12 the lead answered "crest and quills in Beta". Are both quill sets in Beta? | Answered 2026-10-12 (lead): yes. Both quill sets and the crest set are in Beta, each with every layer | Answered |
| D95 | F1.3 | Fossil Processor working animation: does the Processor animate while it runs? | No animation. The video and the original show none (lead 2026-10-12) | Answered |
| D96 | F1.8 | Incubator arm extras: do the two claw fingers (`RoboticHand5`, `RoboticHand6`) open and close, and do the arm parts on the lamp side light up brighter? The original claw did not move. Proposal: the claw opens while the arm reaches, closes in the grip pause and opens at release (F1.8). The current model has the fingers as separate bones, so no new part is needed. | Answered 2026-10-13 (lead, final pass): the claw opens while the arm reaches, closes on the egg and opens at release. The fingers `RoboticHand5` and `RoboticHand6` are separate bones, so no new art. No arm light gradient | Answered |
| D97 | F2.0, M0.B00 | Active hours per species? No source sets them | Answered 2026-10-12 (lead): Tyrannosaurus, Velociraptor and Dilophosaurus cathemeral, with windows per species (D98). Triceratops, Brachiosaurus and Gallimimus diurnal | Answered |
| D98 | F2.0, F2.4 | Cathemeral sleep window? | Answered 2026-10-12 (lead): two windows per cathemeral species, config defaults tuned in playtest. T. rex 16,000 to 21,000 and 7,000 to 9,000 (22:00 to 03:00, 13:00 to 15:00). Velociraptor 20,000 to 23,000 and 6,000 to 8,500. Dilophosaurus 2,000 to 5,000 and 8,000 to 10,000. Offsets: D150. The old "dawn and dusk" label was wrong: its values were midnight and noon | Answered |
| D99 | F2.0, F2.4 | Stress band edges: uneasy margin, and how the idle pool shifts by band | Answered 2026-10-12 (lead): uneasy from 20 below the threshold. Uneasy sits half as often and calls twice as often. Stressed never sits and calls three times as often. Tuned in playtest | Answered |
| D100 | F2.0 | A contentment or welfare score, as in the reference games? | Answered 2026-10-12 (lead): yes, adopted for Beta as a readout plus effects of its own. One 0 to 100 score from needs, stress, health and safety. Stress stays the gate. Which effects: D109. Formula and bands: D110 | Answered |
| D101 | F2.0 | Idle weights and ambient call rates per species | Answered 2026-10-12 (lead), call rates: T. rex about every 5 minutes. Velociraptor and Gallimimus about every minute. Triceratops, Brachiosaurus and Dilophosaurus every 2 to 3 minutes. Tuned in playtest. Idle weights stay as in Config and species data | Answered |
| D102 | F2.0, F2.6 | What does "aggressive" change? | Answered 2026-10-12 (lead): notice range +50 %, warning cooldown halved. A standing species attacks a player even outside its personal space, after a warning. A fleeing species still flees | Answered |
| D103 | F2.0, F2.4 | Does stress at 100 cause damage? | Answered 2026-10-12 (lead): no. Stress at 100 does no damage | Answered |
| D104 | F2.0, F2.4 | Thirst seek and run lines, and the damage rate at zero thirst | Answered 2026-10-12 (lead): seek at 75 %, run at 25 %. At zero thirst the animal loses 1 health each 40 ticks (2 s). Tuned in playtest | Answered |
| D105 | F2.0, F2.4 | What does lost sleep do? | Answered 2026-10-12 (lead): the rest part of contentment stays at 50 or lower until the next full sleep ("tired"). Stamina regenerates at half rate. Follow-up pass, reversed: lost sleep gives no stress. This replaces "each missed window adds one stress step" | Answered |
| D106 | F2.0, F2.5 | Overcrowding and how long "alone" takes to become urgent | Answered in part 2026-10-12 (lead): a herd above its maximum splits into two herds, each with its own leader. No crowding stress. Follow-up pass: "alone" becomes urgent after 3 in-game days (72,000 ticks) | Answered |
| D107 | F2.0, F2.5 | What is "Play" (species cards)? | Answered 2026-10-12 (lead): content young animals chase each other with existing clips (run, Velociraptor pounce). No damage. No new clip. No species has a play clip | Answered |
| D108 | F2.0, F2.6, F2.5 | Do tame animals and babies keep the species rhythm? | Answered 2026-10-12 (lead): a tame animal keeps its rhythm. It sleeps in its window and calls. Follow pauses while it sleeps. A tame "stay" blocks journeys, social moves and breeding only. Follow-up pass: it may walk to food and water, then it returns to its spot. It sleeps on its spot. Babies take one extra nap a day and a longer night sleep, and call twice as often | Answered |

### D109 and later: lead decisions of 2026-10-12 on feeding, contentment and genes

These rows come from three lead decisions of 2026-10-12: animals feed together, Beta has a contentment score, and the plan states how genes change behaviour. The lead set each rule. The rows ask only for the values that no primary source gives. The same decisions answered D18, D57 and D100.

| Id | Work package | Question | Default if unanswered | Status |
| --- | --- | --- | --- | --- |
| D109 | F2.0, F2.1 | Which effects of its own does contentment have? The lead adopted effects and did not choose them. | Answered 2026-10-12 (lead): content grows 10 % faster and heals 25 % faster. Distressed grows 25 % slower and heals at half rate. Tuned in playtest. Follow-up pass: the uneasy band has no effect | Answered |
| D110 | F2.0 | Contentment formula, bands, missed-sleep cap and hover range. | Answered 2026-10-12 (lead), after a worked example. All seven parts with weights: hunger 20, thirst 20, rest 10, company 15, stress 15, health 10, safety 10. Cap: the worst part + 50, not + 30. Bands 70 and 40, hysteresis 5. Hover range 24 blocks. Missed-sleep rest cap 50 (D105). Icons only (D149) | Answered |
| D111 | F2.0, F2.4, F2.5, F3.3 | Feeding spots and turns: the gap per eater, the turn limit, the wait ring, the claim times, the order between groups, the herd food position. | Answered 2026-10-12 (lead): gap 0.5 blocks per eater, turn limit 30 s while others wait, wait ring 6 blocks, longest wait first between groups and species. Answered 2026-10-13 (lead, final pass): a claim on a feeder spot frees after 5 s if the animal has not arrived. A claim on a plant block frees after 10 s. Herd meals are hybrid, queue first. The herd travels to one source, and each member claims its own spot. A member with no spot waits up to 30 s, then may use other food in the herd area. All regroup after | Answered |
| D112 | F2.0, F1.5, F2.4, F2.5, F2.6, F2.7 | Behaviour gene effect sizes that no primary source gives (spec F2.0 "How genes change behaviour"). | Answered in part 2026-10-12 (lead): intelligence search × (0.75 + 0.05 × I), retry ÷ the same factor, failed-target memory × (1 + 0.05 × I), detection + I points. Aggression +50 % or more makes a fed neutral species warn and attack. Tamability threshold ÷ (1 + gene %), floor 0.25. Nocturnal edge shift up to 2 hours below ±50 %. Retreat above 2 × threat. Heat resistance lowers the fire path cost. Lysine, quirks: D169, D171. 2026-10-13: a risk behaviour comes from one parent with a 50 % chance. Answered 2026-10-13 (lead, Z-8): a camouflaged ambusher waits as long as one without camouflage. Camouflage only makes it hard to see. Llama item carrying is in Beta: a tame animal carries a few items for its owner, like a llama with a chest. Llama spit is in Beta: a small knockback, no damage. The cocoa allergy is not in Beta. This replaces the proposals × 1.5 and "llama spit not in Beta" | Answered |

### D113 and later: M1 open-items pass of 2026-10-12

These rows come from the lead's answers to the M1 open items on 2026-10-12. D113 to D138 record answers that had no row. D139 to D148 hold the new questions that the answers opened. The same pass answered D05, D41, D78, D80 to D85 and D93. Where a row says "asker's reading", the asker interpreted a multiple-choice answer, and the lead did not word the rule.

| Id | Work package | Question | Default if unanswered | Status |
| --- | --- | --- | --- | --- |
| D113 | F1.3, F1.6, F1.7, F1.10, F1.12 | Does each machine take its own chip set? Does synthesis get a diamond chip tier? | Answered 2026-10-12 (lead): 3 shared chip items (iron, gold, diamond) fit the Processor, the Sequencer and the Egg Printer. Synthesis gets a diamond chip time of its own. The value: D139. This replaces the spec's "no diamond chip" for synthesis | Answered |
| D114 | F1.7, F1.10 | Part names: video names or design doc names (Chassis, Lid, Printer Frame, Dome)? | Answered 2026-10-12 (lead): the video names Base, Needle, Platform, Body | Answered |
| D115 | F1.5 | Does the design doc's 90 % gate for attack and defense edits stay? | Answered 2026-10-12 (lead): yes. Attack and defense edits need 90 % | Answered |
| D116 | F1.5, F1.9 | Does the design doc's clone-count limit on edits stay? | Answered 2026-10-12 (lead): yes. Each 3 clones hatched add one edit slot. The hard drive stores the count, so a shared drive shares its unlocks. Open parts: D140 | Answered |
| D117 | F1.10 | Upgrade cap and upgrade drawbacks. Section 11.8 sets no limit. The design doc says 2 to 3 per machine, and some upgrades block others. | Answered 2026-10-12 (lead): the design-doc cap of 3 per machine stays. No machine has more than 3 upgrade slots today, so the cap limits nothing. No upgrade blocks another. Some upgrades cost more power as their drawback (D141). This replaces "no limit" (section 11.8) | Answered |
| D118 | F1.7 | What happens on a failed print, and does the artificial egg item stay? | Answered 2026-10-12 (lead): the embryo dies. The platform egg plays an egg-break effect. The player gets a cracked artificial egg, the bone meal and the empty syringe. The artificial egg stays only as this failed-print result | Answered |
| D119 | F1.11 | Generator look, light and output, and the conduit look. | Answered 2026-10-12 (lead): flame glow and a smoke puff while it burns, light 13. The conduit is a thin cable block. Other mods' cables also work. Answered 2026-10-13 (lead, final pass): Basic Generator 200 FE per tick, buffer 20,000 FE (5 s). Machine base draw 20 FE per tick. Faster tiers: D141. Table in spec F1.11 | Answered |
| D120 | F1.1 | Fossil pouch: can the player put items in by hand? | Answered 2026-10-12 (lead): yes, fossils only. The pouch refuses every other item | Answered |
| D121 | F1.4 | Does the Sequencer use up the sample? | Answered 2026-10-12 (lead): the run uses up the DNA. The empty tube or syringe returns to the player for reuse. Neither the video nor the design doc gives this rule | Answered |
| D122 | F1.8 | How fast does the Incubator use plant matter? | Answered 2026-10-12 (lead): plant matter drains per machine, not per egg. A config rate, tuned in playtest | Answered |
| D123 | F1.9 | How long does a placed egg wait before it shakes and hatches? | Answered 2026-10-12 (lead): no wait. The shake starts at once, and the baby comes out 60 s after placing | Answered |
| D124 | F1.3 | Filter durability and water per run. | Answered 2026-10-12 (lead): iron 150, gold 250, diamond 500 runs, as in the original. Water per run is a config value, tuned in playtest | Answered |
| D125 | F1.1 | Fortune tier-up chance per level. | Answered 2026-10-12 (lead): a config band from +5 % to +10 % per level. Answered 2026-10-13 (lead, final pass): +7.5 % per level, so Fortune III gives +22.5 % | Answered |
| D126 | F1.1 | Are random ore opacity and random rotation in Beta? Are the tier names and the T. rex neck right? | Answered 2026-10-12 (lead): random opacity (15 to 100 %) and random rotation are in Beta. The names fragmented, poor, common, pristine stay. The T. rex has a neck piece | Answered |
| D127 | F1.2 | Does a turtle in love mode give an embryo? | Answered 2026-10-12 (lead): yes, as a chicken and a parrot do. Item: D146 | Answered |
| D128 | F1.2 | Can syringe draws kill an animal? | Answered 2026-10-12 (lead): yes. 1 HP per draw, no cooldown | Answered |
| D129 | F1.3 to F1.8, F1.11 | Machine sounds: generic lab sounds, or sounds of each machine's own? | Answered 2026-10-12 (lead, asker's reading): each machine uses its own asset where one exists and a generic lab sound for the rest. The artist also makes sounds of its own for each machine. Generators get their own sounds. Style reference for the artist: Jurassic World Evolution 2 and 3 machinery sounds | Answered |
| D130 | F1.8, F1.9 | Incubation time per species. | Answered 2026-10-12 (lead): 30 minutes for every species in Beta, tuned in playtest. This sets aside "different for each species" (section 11.7) for Beta | Answered |
| D131 | F1.8 | Incubator rules: bulb item, stall death, turn interval per egg, arm speed. | Answered 2026-10-12 (lead): the built-in lamp is the base bulb, and the machine runs with no bulb item. An egg stalled for 5 minutes dies (config). Each egg turns every 5 minutes, and the arm visits every egg. The arm keeps even-speed moves (parity) | Answered |
| D132 | F1.4 | Sequencer monitor glow and screen ring speed. | Answered 2026-10-12 (lead): the monitor glows (lit texture on `monitor_primary`) while it works. The rings make one turn per 20 s | Answered |
| D133 | F1.5 | Donor sliders below 70 %, the colour swatch swap, the risk panel position. | Answered 2026-10-12 (lead): donor sliders move from 50 %, gene sliders at 70 %. The video swatch swap stays. The risk panel is a strip under the effect panel, plus one line on the Synthesis tab | Answered |
| D134 | F1.6 | Synthesis cancel, bone block, plant matter items. | Answered 2026-10-12 (lead): cancel returns the bird-embryo syringe and the unused ingredients, and uses no drive DNA. A bone block counts as 9 bone meal. Plant matter is a config tag list: leaves, saplings, wheat, kelp, grass, flowers, also of other mods | Answered |
| D135 | F1.7 | What does the printer platform show while idle and while it prints? | Answered 2026-10-12 (lead): idle, a ghost egg outline. During a print, the species' egg model with the printed texture | Answered |
| D136 | F1.10 | Does replacing an upgrade give the old one back? Which machines link as tabs? | Answered 2026-10-12 (lead): yes, the old upgrade comes back. Tabs show every machine on one conduit or generator network | Answered |
| D137 | F1.12 | Extra advancements? | Answered 2026-10-12 (lead): yes. First fossil, first DNA, first egg, first hatch, first fully upgraded machine. Open part: D148 | Answered |
| D138 | F1.1 | Depth and biome per species for the five species other than T. rex. | Answered 2026-10-13 (lead, final pass): the depth and biome table of spec F1.1. Depth: T. rex y 40 to 75, Triceratops 55 to 75, Gallimimus 50 to 72. Velociraptor 45 to 70, Brachiosaurus 35 to 55. Dilophosaurus −20 to 30, the only Beta deepslate fossil. Nine biome groups. Equal species shares. The Dilophosaurus uses the Jurassic period rarity, 2.8 (D254) | Answered |
| D139 | F1.6 | The diamond chip time for synthesis. | Answered 2026-10-13 (lead, final pass): 2 minutes (iron 7, gold 4, diamond 2) | Answered |
| D140 | F1.5, F1.9 | Clone-count limit: how many edit slots does a drive have before its first 3 clones hatch? How does the drive learn of a hatch far from the Sequencer? | Answered in part 2026-10-12 (lead): the count counts hatches. Each egg remembers its drive. After a hatch, the count rises the next time that drive is in a Sequencer. Follow-up pass: a new drive starts with 2 edit slots, +1 per 3 clones hatched from it | Answered |
| D141 | F1.10 | Which upgrades cost more power as their drawback, and how much? | Answered 2026-10-13 (lead, final pass): only chips and bulbs cost extra power. A faster tier draws base × speed × run-cost factor. The factor is 1.05, 1.10 or 1.15 for tiers 1, 2 and 3, so each run costs 5, 10 or 15 % more energy. Filters, the Leveling Sensor, containers and tanks cost nothing extra | Answered |
| D142 | F1.4 | Is the Ender SSD in Beta? The spec says "in the future". | Answered 2026-10-12 (lead, follow-up pass): not in Beta. No extra tiers in Beta | Answered |
| D143 | F1.3 | Is a netherite tank in Beta for the Fossil Processor (spec: "possible"), and with what capacity? | Answered 2026-10-12 (lead, follow-up pass): not in Beta. No emerald tank either (D78) | Answered |
| D144 | F1.2 | Does an embryo draw end the animal's love mode? | Answered 2026-10-12 (lead, follow-up pass): no effect on the animal. It stays in love mode and takes no damage | Answered |
| D145 | F1.1 | Does amber go into the fossil pouch? | Answered 2026-10-12 (lead): yes. The pouch takes fossils and amber and refuses every other item | Answered |
| D146 | F1.2, F1.6 | Does a turtle embryo use the bird-embryo syringe item, or an item and a look of its own? | Answered 2026-10-13 (lead, final pass): one item, the "Embryo Syringe", for all donors. The tooltip names the donor, for example "Embryo Syringe (Turtle)" | Answered |
| D147 | F1.9, M0.B00 | Start values of the lab hatch rate per species. | Answered 2026-10-12 (lead, follow-up pass): by size. 95 % for small species (Velociraptor, Dilophosaurus, Gallimimus), 75 % for large species (T. rex, Triceratops, Brachiosaurus). Config values, tuned in playtest | Answered |
| D148 | F1.12 | Do the earlier advancement proposals also ship: first amber, first species at 50 %, first edit, first embryo? | Answered 2026-10-12 (lead): yes. First amber, first species at 50 % genome, first edit and first embryo also ship | Answered |

### D149 and later: M2 open-items pass of 2026-10-12

These rows come from the lead's answers to the M2 open items on 2026-10-12. Most record answers that had no row. These rows stayed open after the M2 pass: D149, D160, D166, D170, D181 and D182. The follow-up pass answered D149, D170 and D181, and the approach of D182. The same pass also changed older rows:

- D03, D04, D11 to D13, D16, D19, D20, D42, D43, D46, D48, D50, D51 and D79
- D87 to D90, D92, D94, D97 to D112, D140, D145 and D148.

Some answers live in existing rows: the contentment parts and cap (D110), the sleep windows (D98), limp (D92), special-gene slots (D51) and the Sorna JP3 model (D90). A Claude default or a Claude draft is a proposal for the lead to review, not a lead value.

| Id | Work package | Question | Default if unanswered | Status |
| --- | --- | --- | --- | --- |
| D149 | F2.0, F1.10 | Hover box content: text only, or text with band and need icons? Do exact numbers stay on the Debug Stick only? | Answered 2026-10-12 (lead, follow-up pass): icons only. The name, a coloured mood face for the band, and need icons for the worst need. No band words. Art: 3 band faces and 7 need icons (hunger, thirst, rest, lonely, stressed, hurt, threatened). This replaces the text mock-up. Exact numbers stay on the Debug Stick, in creative or for operators (asker's reading: the lead did not pick that option again) | Answered |
| D150 | F2.0, M0.B00 | Does each animal sleep at the exact window edge? | Answered 2026-10-12 (lead): each animal rolls a fixed offset of up to ±500 ticks (±30 in-game minutes) at hatch. A herd or pack uses its leader's offset, so the group sleeps together | Answered |
| D151 | F2.5 | What does a pack do when it loses its leader or falls below its minimum? | Answered 2026-10-12 (lead): it flees to safety first. Then the members with the highest and second-highest dominance contest the lead. A pack below its minimum flees big threats. A lone member looks for another pack in range, else it is lonely | Answered |
| D152 | F2.5 | One nest block for all species, or one per egg size class? | Answered 2026-10-13 (lead, final pass): reversed. No nest model exists. The female builds a nest from vanilla blocks of her surroundings (leaves in a forest, sand in a desert, gravel by water). She plays her nesting clip (T. rex, Velociraptor) or an idle clip and lays egg entities inside. With `mobGriefing` off or with no room, she lays on the ground. The nest blocks stay after the hatch, and the pair uses the nest again. The size follows the egg class. The breeding gate is a built nest or a free laying spot. This replaces "one nest block for all species" | Answered |
| D153 | F2.5 | Gestation time from mating to laying. | Answered 2026-10-12 (lead): 1 in-game day (24,000 ticks), tuned in playtest | Answered |
| D154 | F2.1, M0.B00 | Growth time to full size for five species. | Answered 2026-10-12 (lead): the draft stands. Velociraptor 29, Dilophosaurus 31, Gallimimus 31, Triceratops 47, Brachiosaurus 57 in-game days (T. rex 45, card). Neutral rate. Contentment changes it (D109). Tuned in playtest | Answered |
| D155 | F2.1, M0.B00 | Hatchling and infant sizes for five species. | Answered 2026-10-12 (lead): the T. rex ratios for every species. Hatchling 8 %, infant 32 % of adult height. Tuned in playtest | Answered |
| D156 | F2.1 | How long does a carcass last? | Answered 2026-10-12 (lead): 1 in-game day (24,000 ticks), then it decays | Answered |
| D157 | F2.1 | Which special pieces drop from a carcass? Whose teeth? | Answered 2026-10-12 (lead): only the named pieces. Velociraptor toe claw, Triceratops horn, T. rex tooth. No other species drops teeth | Answered |
| D158 | F2.1 | Hit-part layouts for five species. | Answered 2026-10-12 (lead), after a review of `docs/dinosaur-hitbox-layouts.html`: all five accepted. T. rex 13, Triceratops 12, Brachiosaurus 17, Velociraptor 12, Dilophosaurus 11 parts | Answered |
| D159 | F2.4 | Food value: five food tiers by tag, or the GH #6 clade chunks? | Answered 2026-10-12 (lead): five food tiers by tag (plan) | Answered |
| D160 | F2.4 | How long is the need cooldown after eating or drinking? | Answered 2026-10-13 (lead, final pass): 5 minutes (6,000 ticks) after eating or drinking, unless the need is urgent. This replaces the Claude default of 3 minutes | Answered |
| D161 | F2.4 | How long is one stress step? | Answered 2026-10-12 (lead): 1 minute (1,200 ticks), tuned in playtest. Follow-up pass: each active cause adds +2 stress per step. Lost sleep is not a cause | Answered |
| D162 | F2.6 | Which player activity sets which detection level? | Answered 2026-10-12 (lead): sneak 25 %, walk or stand 40 %, sprint, jump or hit 60 %, loud acts (weapon, block breaking, vehicle, noise) 85 %. Tuned in playtest | Answered |
| D163 | F2.6 | How does a player tame a dinosaur? | Answered 2026-10-12 (lead): a trust score rises near a calm animal. Hand-feeding (use its food on it) gives a big boost. Trust rises only while stress is below 20. The lead drops the design doc's zero-stress rule | Answered |
| D164 | F2.6 | Does a roar frighten other mobs? | Answered 2026-10-12 (lead): yes. A roar scares passive mobs and villagers near it for a few seconds. Villagers flee loose carnivores (D19) | Answered |
| D165 | F2.6, M0.B00 | What is the "territory" of section 11.10? | Answered 2026-10-12 (lead): a personal-space radius by body size. A standing animal warns a noticed player inside it, then attacks. Young provoke always. The detection range stays separate. Answered 2026-10-13 (lead, final pass): start values T. rex 10, Brachiosaurus 10, Triceratops 8, Dilophosaurus 6, Velociraptor 6, Gallimimus 5 blocks, config, tuned in playtest | Answered |
| D166 | F2.6, F2.5 | Does an animal also have a home territory, or its paddock as a territory? The lead did not pick either option. | Answered 2026-10-13 (lead, final pass): no home territory. Personal space and "young provoke always" stay. Eggs count as young (D219) | Answered |
| D167 | F2.4, F3.3 | Gallimimus diet: herbivore or omnivore? | Answered 2026-10-12 (lead): omnivore. Plants plus small meat (dropped meat, eggs). It uses the herbivore and the carnivore feeders, and it gets the blood effects (D174) | Answered |
| D168 | F2.7 | Gender swap trigger: only one gender, or 0 to 2 of the other gender? | Answered 2026-10-12 (lead): only in a one-gender group, with no animal of the other gender | Answered |
| D169 | F2.7 | Lysine reserve and coma. | Answered 2026-10-12 (lead): a full reserve lasts 3 in-game days. Coma when it is empty, death 1 in-game day later (config). Lysine-rich food (chicken, eggs) tops it up. Tuned in playtest | Answered |
| D170 | F2.7, F1.12 | Is a "Lysine Pills" item in Beta? | Answered 2026-10-12 (lead, follow-up pass): food only. No Lysine Pills item | Answered |
| D171 | F2.7, F2.5 | Quirk rates and heredity. | Answered 2026-10-12 (lead): each quirk plays at a random moment every 2 to 5 minutes (config). A parent that has the quirk passes it with a 50 % chance | Answered |
| D172 | F2.2, F2.7 | Bioluminescence: one glow mask per species or per skin set? | Answered 2026-10-12 (lead): one glow mask per species (6 textures), shared by all its skin sets | Answered |
| D173 | F1.5, F2.4 | Is the design doc's Lower Threshold Stress gene dropped? | Answered 2026-10-12 (lead): no. A basic number gene, not a special gene. It raises the species stress threshold. Calm donors give it, for example cat and sheep. Follow-up pass: its new name is "Stress Tolerance" | Answered |
| D174 | F2.3 | Species applicability of the Dino VFX matrix. | Answered 2026-10-12 (lead): accepted with two changes. The Gallimimus gets blood drips and a bloody mouth. Screen shake is on the T. rex, the Triceratops and the Brachiosaurus | Answered |
| D175 | F2.3 | Sound rules: shared carnivore eat sound, yawn, sneeze and snort sounds, hatchling voice, Velociraptor screech. | Answered 2026-10-12 (lead): the lead accepts one shared carnivore eat sound. Yawn, sneeze and snort get their own sounds per species. Hatchlings get their own sounds, not a pitched adult voice. The Velociraptor threat screech has its own clip and sounds, apart from the pack call. The lead sources the dinosaur sounds from Jurassic World Evolution 3. Shared placeholders may serve meanwhile, and they count as Missing | Answered |
| D176 | F2.3 | Clip reuse: Brachiosaurus rest, sit and sleep, Triceratops sit, Dilophosaurus roar older and young, the T. rex card extras. | Answered 2026-10-12 (lead): each needs its own clip. The existing clips stay as placeholders meanwhile. The T. rex extras (startled, shake body, scratch, speak) are for every species | Answered |
| D177 | F2.3, F2.6 | Which clip plays the Dilophosaurus warning? | Answered 2026-10-12 (lead): its intimidate clip, converted from the asset repo. If the player stays, it spits | Answered |
| D178 | F2.2 | Dilophosaurus JPTG layers per gender, and gender size. | Answered 2026-10-12 (lead): one shared JPTG body for both genders. The female scales larger than the male. The male JPTG tint is fainter and less vibrant (asker's reading: than the female, because the lead's note was cut off) | Answered |
| D179 | F2.2 | Albino and melanistic: tints or own textures? | Answered 2026-10-12 (lead): tints of the normal layers. No morph textures | Answered |
| D180 | F2.2 | Which biome group gives which skin set? | Answered 2026-10-13 (lead, final pass): the table of spec F2.2. The biome group with the largest share of the sequenced DNA wins, and a tie gives the base set. Amber counts by its biome group. Syringe and remains DNA does not count. The Velociraptor follows its own rule (D248) | Answered |
| D181 | F2.7, M0.B00 | Does the Dilophosaurus carry poison from hatch? The lead answered D43 for hyper intelligence only. | Answered 2026-10-12 (lead, follow-up pass): yes. Every Dilophosaurus carries poison from hatch, as every raptor carries hyper intelligence | Answered |
| D182 | F2.0, F1.5 | Each cathemeral species has its own windows. Which windows does a diurnal species get when the nocturnal gene moves it one step to cathemeral? | Answered 2026-10-12 (lead, follow-up pass): Claude drafts cathemeral windows per herbivore. Answered 2026-10-13 (lead, final pass): Triceratops 18,000 to 22,000 and 5,000 to 7,000. Brachiosaurus 19,000 to 22,000 and 9,000 to 10,000. Gallimimus 17,000 to 21,000 and 3,500 to 5,000. Config start values, tuned in playtest. A shifted member keeps its own window and naps alone, also in a herd | Answered |

### D183 and later: M3 open-items pass of 2026-10-12

These rows come from the lead's answers to the M3 open items on 2026-10-12. Most record answers that had no row. These rows stayed open after the M3 pass: D186, D188, D194 and D200. The follow-up pass answered D186, D194 and D200. The same pass answered D21 to D24, D26, D27, D35 and D86, and gap 1 of D78. Gap 2 of D78 stays open. A Claude draft is a proposal for the lead to review, not a lead value. Where a row says "asker's reading", the asker interpreted the answer, and the lead did not word the rule.

| Id | Work package | Question | Default if unanswered | Status |
| --- | --- | --- | --- | --- |
| D183 | F3.1 | Does the fence get sounds? The 1.12.2 fence had only the repair sound. | Answered 2026-10-12 (lead): yes, as an addition to parity. A hum loop near a live fence, a shock zap, and a wire snap when a T. rex breaks a wire | Answered |
| D184 | F3.1 | Spool icon (F22): does the artist draw 12 fill stages? | Answered 2026-10-12 (lead). Follow-up pass, reversed: the code shows the 10 stage sprites of the asset repo, not 12. A deviation from parity F22 for art reasons. This replaces "the artist draws 12 fill-stage sprites" | Answered |
| D185 | F3.1 | Do the design doc's four film fence kinds (Perimeter, Large Carnivore, Small Carnivore, Light Herbivore) ship? | Answered 2026-10-12 (lead): no. They stay out of Beta. Beta has the two parity posts only | Answered |
| D186 | F3.1 | What does the fence switch block look like? No art and no reference exist. | Answered 2026-10-12 (lead, follow-up pass): a lever like the vanilla lever on a metal post plate. A small new model (Art list) | Answered |
| D187 | F3.2 | Large Generator screen and motion. | Answered 2026-10-12 (lead): a screen of its own with fuel slots, a burning flame, an energy bar and the line status. The green and red lights blink while it runs. Playtests tune its values | Answered |
| D188 | F3.2 | Large Generator start values. | Answered 2026-10-13 (lead, final pass): accepted. Large Generator 800 FE per tick (4 × the Basic Generator), fuel lasts 2 × longer, buffer 80,000 FE. It runs 80 posts alone, or about 48 beside a fully upgraded lab. Other mods' cables may take the full output (no export cap) | Answered |
| D189 | F3.2 | Pylon range, how a pylon meets a fence, pylon sound. | Answered 2026-10-12 (lead): link range 32 blocks. The player links pylon to pylon and pylon to post with the Wire Spool. A pylon also feeds the nearest post within 4 blocks. A pylon hums | Answered |
| D190 | F3.3 | What are the feeder models, and how big are they? | Answered 2026-10-12 (lead), after clip playback: every feeder model is an in-ground hatch. y = 0 is the ground. A shut hatch shows only its lid. The first reading (heights 2.9 to 8.7 blocks) was mostly shaft below the ground. Feeder 1 is the small carnivore hatch (3 × 3). Feeder 2 is the large one (5.25 × 4.88), with more feeding spots | Answered |
| D191 | F3.3 | How do the four herbivore feeders differ? | Answered 2026-10-12 (lead): by the height of the food lift. Feeder 5 low (Triceratops, Gallimimus), Feeder 3 medium, Feeder 4 tall, Feeder 6 very tall (Brachiosaurus). Same behaviour. The lead named no species for medium and tall | Answered |
| D192 | F3.3 | Does a feeder need a dug shaft? | Answered 2026-10-12 (lead): no. The shaft is virtual. Only the hatch is a block. The renderer draws the rising food with no blocks below. No digging and no block clearing | Answered |
| D193 | F3.3 | When does a feeder open and close? | Answered 2026-10-12 (lead): the hatch opens on a timer (the feeding schedule) and closes after the feeding. An empty feeder always closes. This replaces the proposal "opens when a hungry dinosaur comes near" | Answered |
| D194 | F3.3 | Feeding timer interval and open time. | Answered 2026-10-12 (lead, follow-up pass): every feeder screen (herbivore and carnivore too) gets the 2, 5, 10 and 20 min interval buttons. Each opening lasts until all animals finish eating. The hatch closes after 5 s with no bite. An empty feeder always closes | Answered |
| D195 | F3.3 | Feeder screens and food storage. | Answered 2026-10-12 (lead): three screens in the lab GUI style. Herbivore (all four heights): 3 × 3 food slots, a green gauge, the food % and "Eating: X of Y". Carnivore (small and large): 3 × 3 meat slots and a red gauge. Live-Bait: pen count per type, interval buttons, the animal on the chain, the next release. The gauge stores the food up to 100 %. The slots hold the overflow | Answered |
| D196 | F3.3 | Which items does a herbivore feeder take? | Answered 2026-10-12 (lead): four groups. Leaves and saplings (tag). Crops and produce: wheat, carrots, potatoes, beetroot, melon, pumpkin, apples, berries. Grass and ferns, also seagrass and kelp. Hay bales: one counts as 9 wheat | Answered |
| D197 | F3.3 | Which model is the Live-Bait Feeder? No live-bait model exists. | Answered 2026-10-12 (lead): Feeder 1, adapted with a pole and a chain to the animal that is out. New art | Answered |
| D198 | F3.3 | How does live bait get into the feeder? | Answered 2026-10-12 (lead): the player leads the animal to the feeder on a lead and right-clicks. The feeder stores it in an internal pen, with a count per animal type | Answered |
| D199 | F3.3 | When does the Live-Bait Feeder release bait? | Answered 2026-10-12 (lead): at an interval set on its screen: 2, 5, 10 or 20 min. One bait is out at a time. No new bait comes out while the last one lives on the chain | Answered |
| D200 | F3.3 | Does the Live-Bait Feeder also release on hunger or on a redstone pulse? | Answered 2026-10-12 (lead, follow-up pass): no. Interval release only | Answered |
| D201 | F3.3, F2.4 | Which vanilla animals are bait? | Answered 2026-10-12 (lead): a small carnivore takes a chicken, a rabbit, a pig or a sheep. A large carnivore also takes a cow, a horse or a goat. The hunt lowers stress and counts as a hunt | Answered |
| D202 | F3.4 | Park recipes: one per feeder model? Who drafts the ingredients? | Answered 2026-10-12 (lead): one recipe per feeder. Claude drafts every park recipe. Answered 2026-10-13 (lead, final pass): the lead approved the draft with changes. The Large Carnivore Feeder and each taller herbivore feeder use the feeder below as an ingredient | Answered |
| D203 | F3.4 | Park advancements. | Answered 2026-10-12 (lead): "First paddock" (a closed, powered loop around a dinosaur), first feeder fill, first live-bait hunt, first Large Generator | Answered |

### D204 and later: M4 and follow-up open-items pass of 2026-10-12

These rows come from the lead's answers of 2026-10-12 to the M4 open items (F4.1, F4.2). They also come from the follow-up rounds on agent readings and survey defaults. They record answers that had no row. The same pass answered or changed many older rows:

- D01, D02, D07, D10, D13, D15, D17, D25 (in part), D27 to D32, D34 to D37, D40, D42 to D45, D47
- D53 to D56, D58 to D63, D65, D67 to D77, D78 (all gaps), D105, D106, D108 to D110, D140, D142 to D144, D147, D149
- D161, D170, D173, D181, D182 (approach), D184, D186, D194 and D200.

These rows stay open: D25 (Tour Station), D33, D52, D66, D111, D160, D166 and D188. The D215 trade list waits as a Claude draft. Where a row says "asker's reading", the asker interpreted the answer, and the lead did not word the rule. A Claude draft is a proposal for the lead to review, not a lead value.

| Id | Work package | Question | Default if unanswered | Status |
| --- | --- | --- | --- | --- |
| D204 | F4.2 | Which vehicle variations ship, and how does a player pick one? | Answered 2026-10-12 (lead): all variations ship, one item per vehicle. Answered 2026-10-13 (lead, final pass): reversed in part. Dye tints the colours of the vehicle, and the vehicle uses up the dye. A new Wrench item moves the vehicle to its next variation. This replaces "dye cycles the variation". Answered 2026-10-13 (lead, Z-8): the player assembles each vehicle from parts (D250). This replaces "one item per vehicle" | Answered |
| D205 | F4.2 | Wheel and door motion: code or keyframed clips? | Answered 2026-10-12 (lead): code. Wheels spin by speed, and the front wheels steer. Doors swing open when a player gets in or out, where the model has door parts. No vehicle has clips, and code needs none | Answered |
| D206 | F4.2 | Do vehicles take damage, and how are they repaired? | Answered 2026-10-13 (lead, final pass): reversed. The lead tabled vehicle damage, wrecks and repair for a later release. The hit rules (slow push, fast hurt, D75) stay | Answered |
| D207 | F4.2 | What does the tour track look like? Can players see it? | Answered 2026-10-12 (lead, after the top-down diagram): a visible road-marker block | Answered |
| D208 | F4.2 | What happens at a tour stop? | Answered 2026-10-12 (lead): a chime, the line "Stop 2 of 4" and a custom stop name for the riders. The player sets the name on the marker, in a field like the anvil's | Answered |
| D209 | F4.1 | Which block is the paleontologist workstation? | Answered 2026-10-12 (lead): any lab machine (Fossil Processor, Sequencer, Egg Printer, Incubator). No new block | Answered |
| D210 | F4.1 | Who sells digsite maps? | Answered 2026-10-12 (lead, asker's reading): paleontologists are the main sellers. Cartographers and wandering traders also sell digsite maps. Answered 2026-10-13 (lead, final pass): they also sell species maps, at a higher price than the paleontologist | Answered |
| D211 | F4.1 | Does the Paleo-Illager get its own skin, and which illagers guard a site? | Answered 2026-10-12 (lead): its own skin on the vindicator model, by the artist. The guards are a mix of pillagers and vindicators | Answered |
| D212 | F4.1 | Do iron golems stand at digsites? | Answered 2026-10-12 (lead): a friendly digsite camp has an iron golem. Paleo-Illager sites have none | Answered |
| D213 | F4.1 | Who makes the digsite structures? The 2020 board marks builds as finished, and no file exists. | Answered 2026-10-12 (lead): a builder of the team makes new structures (Art list). The 2020 builds are not used. Answered 2026-10-13 (lead, final pass): the builder also makes the paleontologist lab house | Answered |
| D214 | F4.1 | Do paleontologists give iron golems fossil pieces (2020 board)? | Answered 2026-10-12 (lead): yes, in Beta. Answered 2026-10-13 (lead, final pass): the golem holds the fossil and drops it on death. The lead accepts a possible fossil farm | Answered |
| D215 | F4.1 | Paleontologist trade list and prices. | Answered 2026-10-13 (lead, final pass): the trade table of spec F4.1. Maps at the vanilla price (Digsite Map 14 emeralds and a compass), other goods at the draft price + 25 %. The paleontologist also sells chips, the Hard Drive, the Gold Filter and lab machine parts. Sold fossils are local species only. Hero of the Village gift: test tubes or a syringe. Confirmed 2026-10-13 (lead, Z-8): 2 random trades per level, as for vanilla villagers. The lab house spawns at the normal rate of a village house. The Identification Tag trade left with the tag | Answered |
| D216 | F3.1 | Where does the voltage sign click go, how do its looks cycle, and how does a player remove it? | Answered 2026-10-12 (lead): an empty-hand click on a wire hangs the sign (F18). More empty-hand clicks on the wire cycle look 1, 2, 3, then 1. Sneak + click removes the sign. Post clicks still flip the post (F7). This replaces "the click after the third look removes the sign" | Answered |
| D217 | F3.3 | Do Feeders 3 and 4 (medium and tall) ship, and for which species? | Answered 2026-10-12 (lead): both ship in Beta. Any herbivore that reaches the food can eat from them | Answered |
| D218 | F2.6 | What does a fleeing species do when a player comes near? | Answered 2026-10-12 (lead): a fleeing species (for example the Gallimimus) flees when a noticed player enters its personal space. It gives no warning | Answered |

### D219 and later: final open-items pass and draft reviews of 2026-10-13

These rows come from the lead's last open-items answers and the reviews of Claude's seven drafts on 2026-10-13. Most record answers that had no row. The same pass answered or changed these older rows:

- D25, D33, D36, D52, D61, D66, D78, D79, D96, D111, D112 (in part)
- D119, D125, D138, D139, D141, D146, D152, D160, D165, D166
- D180, D182, D188, D202, D204, D206, D210 and D213 to D215.

The lead answered D249, D250, D252 to D255 and the rest of D112 later on 2026-10-13 (open-items batch Z-8). Only D08 (guns) and D14 (animation speed) wait for the lead, and D91 waits for the artist. Where a row says "asker's reading", the asker interpreted the answer.

| Id | Work package | Question | Default if unanswered | Status |
| --- | --- | --- | --- | --- |
| D219 | F2.5, F2.6 | Do parents defend their eggs? | Answered 2026-10-13 (lead): eggs count as young. Parents that see a player near their eggs warn and attack. No separate home territory | Answered |
| D220 | F1.3 | By-product weights of the Fossil Processor. | Answered 2026-10-13 (lead): about 70 % sand or gravel, 25 % nuggets, 1 % diamond (about 1 diamond in 100 runs). Config | Answered |
| D221 | F1.4 | What does the info card show for a vanilla animal? | Answered 2026-10-13 (lead): the time period "Modern" and the real diet of the animal, from one data entry per vanilla animal | Answered |
| D222 | F1.7 | Bone meal per egg. | Answered 2026-10-13 (lead): 16 per egg, so 4 eggs per stack (today's code value) | Answered |
| D223 | F1.5 | Range of an isolated number gene slider. | Answered 2026-10-13 (lead): ±10 % of the species base at 70 % DNA. The range grows in a line to ±30 % at 100 %, for example ±17 % at 80 % | Answered |
| D224 | F1.5 | What replaces the square donor toggles? | Answered 2026-10-13 (lead): click an empty donor row, then click an animal in the DNA list on the right. Drag the row's slider for its share | Answered |
| D225 | F1.5 | Risk curve: failure, abnormal behaviour and trade-off strength. | Answered 2026-10-13 (lead), Claude's curve accepted. With s = donor share ÷ 50 %: failure 25 % × s² + 2 % per edit. Abnormal 10 % × s² + 1 % per edit. Trade-off min(100 %, 20 % + 60 % × s² + 5 % per edit). Example: 40 % bee, no edits: 16 %, 6 %, 58 %. Config, tuned in playtest | Answered |
| D226 | F1.8 | Does the Incubator take a water bottle (design doc)? | Answered 2026-10-13 (lead): no. The plan drops the input | Answered |
| D227 | F1.7 | Does the uncracked artificial egg sprite stay in the mod? | Answered 2026-10-13 (lead): no. Delete `artificial_egg.png`. The cracked one stays as the failed print | Answered |
| D228 | F1.2 | Does the syringe draw get its own sound? | Answered 2026-10-13 (lead): no own sound. It plays an existing vanilla sound. The first answer was an asker's reading, and the lead confirmed it (Z-8) | Answered |
| D229 | F2.0 | How long does the safety part of contentment stay low after a hit? | Answered 2026-10-13 (lead): it recovers 2 minutes (2,400 ticks) after a hit. The D61 place memory still lasts 1 in-game day | Answered |
| D230 | F2.3, F2.6 | The warning sound of each species. | Answered 2026-10-13 (lead). T. rex roar, Velociraptor screech (apart from the pack call), Dilophosaurus hoot during the frill display. Triceratops snort (domination display), Brachiosaurus bellow, Gallimimus alarm screech | Answered |
| D231 | F2.3 | Does the snort effect need its own clip? | Answered 2026-10-13 (lead): no. It plays over the idle or the sniff clip | Answered |
| D232 | F2.3 | Bloody claw overlays for species other than the Velociraptor? | Answered 2026-10-13 (lead): all carnivores. T. rex and Dilophosaurus join the Velociraptor | Answered |
| D233 | F1.1 | Is the pelvis a fossil piece in Beta? | Answered 2026-10-13 (lead): yes, for every species. The T. rex pelvis has its own sprite in the asset repo. The artist draws one generic pelvis set (fresh, fossilized, fragmented) for the others | Answered |
| D234 | F1.1, F1.3 | Does the test tube icon show its DNA %? | Answered 2026-10-13 (lead): tooltip only. No label sprite | Answered |
| D235 | F3.3 | Does the artist model the live-bait chain? | Answered 2026-10-13 (lead): no. Code draws it from the pole to the animal, like a lead | Answered |
| D236 | F1.9 | Printed egg textures: per species or per model? | Answered 2026-10-13 (lead): one per egg model (5 textures), plus a species tint in code | Answered |
| D237 | F1.7 | Do the Egg Printer lamps glow? | Answered 2026-10-13 (lead): yes, while it prints | Answered |
| D238 | F3.1 | Do fence power-up and power-down sounds ship? | Answered 2026-10-13 (lead): yes. A power-up hum and a power-down click | Answered |
| D239 | F3.3 | Does each feeder get a close clip? | Answered 2026-10-13 (lead): yes, from the artist. Until then, code may play the open clip in reverse | Answered |
| D240 | F3.2 | Pylon design and pylon height. | Answered 2026-10-13 (lead): the pylon head sits on top of the fence pole block. One pole block serves as a fence pole or a pylon pole. The player sets the height of each pole. Pylon cables behave like fence wires (any direction, any slope). This replaces a separate pylon pole structure. Narrowed by D253: only a pole used as a pylon takes any height | Answered |
| D241 | F3.2 | Spool cost of a pylon cable. | Answered 2026-10-13 (lead): ceil(length ÷ 20) spools for one cable. The height does not count | Answered |
| D242 | F1.11, F3.2 | What happens when demand is more than supply? | Answered 2026-10-13 (lead): the generator trips and shuts off until the player removes load or adds a generator. "The player has to build another generator, this is a gameplay decision." This replaces the draft rule "fences first" | Answered |
| D243 | F1.11 | Conduit capacity and outside cables. | Answered 2026-10-13 (lead): no capacity limit inside a network. Other mods' cables may take the full output of a generator | Answered |
| D244 | F3.2 | Posts per feed point. | Answered 2026-10-13 (lead): keep the parity share, about 31 posts per feed point on a loop. Large fences need more feed points (pylons or generators) | Answered |
| D245 | F1.11, F3.2 | Generator buffer, fence fade and the catch-up burn. | Answered 2026-10-13 (lead): a buffer of 5 s of full output, then the fence lamps fade post by post over about 2 s. Whether the catch-up burn after a chunk load makes energy is a developer choice | Answered |
| D246 | F2.5, F2.0 | Do young count toward group size? | Answered 2026-10-13 (lead): no, not until they are adult | Answered |
| D247 | F2.5 | When does a natural (nest) egg hatch? | Answered 2026-10-13 (lead): 3 in-game days (72,000 ticks) after laying, then the 60 s shake. Lab eggs keep their rule (no wait) | Answered |
| D248 | F2.2 | Velociraptor looks from fossils. | Answered 2026-10-13 (lead): fossils from Nublar biomes give Nublar with the Nublar crest. Fossils from Sorna biomes give Sorna with the Sorna crest. Both quill sets belong to the Sorna genome: a male Sorna clone rolls at random for "with quills" or "with arm quills". Mixed looks come only from Nublar × Sorna breeding, or from advanced gene editing after the player unlocks both sets. This replaces the draft's biome mapping of raptor variants | Answered |
| D249 | F2.5, F2.2 | Heredity of raptor looks: when a Nublar raptor breeds with a Sorna raptor, how does the young get its crest and its quills? | Answered 2026-10-13 (lead, Z-8): each trait (base skin set, crest, quills) comes from a random parent, one roll per trait. This replaces the plan default (crest from one parent, quills rolled as for a Sorna clone) | Answered |
| D250 | F4.2, F3.4 | Recipes for the Jeep Wrangler, the Ford Explorer, the track block, the tour stop marker and the Wrench. | Answered 2026-10-13 (lead, Z-8). The player assembles each vehicle from parts like a lab machine. Order: chassis, 4 wheels one by one, engine, body, seats. The Explorer adds a Tour Computer. Every part except the body serves both vehicles. Recipes: Wrench (3 iron, 1 copper). Chassis (minecart, 4 iron). Wheel × 4 (slime block, iron). Engine (piston, furnace, 4 iron, redstone). Seats (5 leather). Jeep body (about 10 iron, 1 glass pane). Explorer body (about 20 iron with a block of iron, 3 glass panes). Tour Computer (comparator, 2 redstone, 1 gold). Tour track × 16 (6 iron, 1 redstone, 2 smooth stone). Stop marker (track, redstone lamp). Grids: "Recipes and progression gates" | Answered |
| D251 | F1.12, F3.4, M0.P2 | Item id renames. | Answered 2026-10-13 (lead): `coal_generator` to `basic_generator`, `iron_plant_tank_ugprade` to `iron_plant_tank_upgrade`, `carnivore_feeder_one` and `carnivore_feeder_two` to `small_carnivore_feeder` and `large_carnivore_feeder` | Answered |
| D252 | F2.0 | Nocturnal window length. The lead reviewed only the cathemeral part of the windows draft. | Answered 2026-10-13 (lead, Z-8): 10:00 to 18:00 (4,000 to 12,000 ticks, 8 h), the same total as a diurnal sleep. This replaces the plan window 06:00 to 18:00 | Answered |
| D253 | F3.1, F3.2 | Pole height and fence parity. Does a pole with fence wires keep the parity height (3 or 8 blocks)? Then only a pylon pole takes any height | Answered 2026-10-13 (lead, Z-8): only a pole used as a pylon takes any height. A fence post keeps 3 blocks (low) and 8 blocks (high), so fence parity holds | Answered |
| D254 | F1.1 | The lead calls the Dilophosaurus Triassic. Board #4 dates it 201.3 to 182.7 Ma, the earliest Jurassic. Which period rarity applies? | Answered 2026-10-13 (lead, Z-8): the Jurassic period rarity, 2.8. Depth stays y −20 to 30 | Answered |
| D255 | F2.5, F1.9 | What does a failed natural egg leave? Most nest eggs fail. | Answered 2026-10-13 (lead, Z-8): the egg cracks open, drops tissue (a DNA source for the Fossil Processor), then vanishes. No broken egg stays in the nest | Answered |
| D256 | F1.12, F1.3, F1.4, F1.7, F1.8 | Lab part recipes and the quartz gate. Z-8 left 11 machine-part grids re-costed by Claude, and the lead had not approved them. | Answered 2026-10-13 (lead): the 17 machine-part grids of "Recipes and progression gates". No lab part uses a stone material, because the parts are machines and computers smaller than a player. Quartz slabs replace smooth stone. The Incubator nest is an empty iron tray until the player adds plant matter, with no wool and no hay. The Processor lid has no nugget and no glass pane. The Sequencer door takes an iron trapdoor. The four machines cost about 50 iron value, 4 gold ingots and 14 quartz slabs. The first lab needs nether quartz, so a Nether trip, and the lead accepted this gate. This replaces the Z-8 text (about 45 iron value, grids re-costed by Claude) | Answered |

---

## Change log

- **2026-10-05:** first plan, against section 11.
- **2026-10-10:** entity survey, tech survey, hitbox design, herd spec and the 1.12.2 fence parity baseline folded in. Register D15 to D39.
- **2026-10-12 (morning):** JC3 survey and the reconciliation folded in. Register D40 to D79. Overview sections and the recipe table added.
- **2026-10-12 (rebuild):** regrouped by milestone. 27 modules became 4 M0 packages, 12 M1, 7 M2, 4 M3, 3 M4, 2 Beta hardening and 3 Release 1 packages. Machine work finishes together in M1. Cloning parity with the 2022 video, programmatic machine animations, machine sounds, Incubator light, DNA labels, the fossil pouch, egg models, egg wobble, the Dino VFX list, per-species clips and sounds with no stand-ins, and complete skin sets added. Art moved to the Art list tab. Register D80 to D94 added. Source citations shortened to tags.
- **2026-10-12 (lead review):** each machine is now one self-contained work package (F1.3, F1.4 to F1.6, F1.7, F1.8) with its parts, building, screen, upgrade looks, power draw, animation, sounds, recipe rows, art and acceptance. F1.10 became the shared machine framework and lands first in M1. Art counts and asset statuses left the backlog for the Art list. Defaults of D85, D86, D89, D91 and D92 follow the lead's scope. D95 (Processor animation) and D96 (Incubator claw) added. Feeder animation proposals added to F3.3. The register and the recipe table use work package ids.
- **2026-10-12 (machine looks):** lead rule: the machine models and textures changed since 2022, so the current models and textures in the mod are the art target. Video parity covers behaviour, timings and screen contents only. F1.3 to F1.10 now name the bones that each animation drives. Adapted: no lift post and no lift build step on the Incubator, the Incubator bulbs light the lamp cap, the Sequencer chips use the 4 chip cubes. New artist work: new parts for upgrade looks (D85). D85 and D96 updated.
- **2026-10-12 (lead corrections):** D95 answered: the Fossil Processor has no animation, as in the video and the original. The Incubator lid lifts straight up 12.5 px, as in the video and the original. It does not swing. The Egg Printer gets its original motion back: needle raster on the cross-rail and side rails, the lid opens, the egg grows from the bottom up, and `Panel` lowers 0.5 px (the room the model has). Bone splits of existing cubes (Processor build parts, Egg Printer base, cross-rail and needle, Sequencer chips) are developer tasks in F1.3, F1.6 and F1.7, with no art change. Art list questions 17 and 18 removed. Question 13 keeps only the arm reach.
- **2026-10-12 (lead: feeding, contentment, genes):** animals feed together. Feeders of every kind are shared by all species of their diet, with feeding spots by size and body width, a wait queue, and the D57 order for all species. A herd gathers around one tree or plant patch with block claims. Carcasses use the same spot model (F2.0, F2.1, F2.4, F2.5, F3.3). Beta has a contentment score with its own effects and three readouts, and stress stays the gate (F2.0, F2.4, F2.6, F1.10). The F2.0 spec states how each gene group changes behaviour, and F1.5 and F2.4 read genes through F2.0 `BehaviourGenes`. D18 and D57 answered. D109 to D112 added.
- **2026-10-12 (lead: idle grazing, F2.0 merged):** RC31 resolved: a content or idle herbivore grazes a block in its reach, with a small hunger gain (`needs.idle_graze_hunger`, proposal), and by default leaves the block (F2.0, F2.4). F2.0 joins M2 as its first package, with the items it takes from F2.3, F2.4 and F2.6. Register rows D97 to D108 added.
- **2026-10-12 (M1 open-items pass):** the lead answered most M1 open items. D05, D41, D78, D80 to D85 and D93 answered. RC20 resolved: lab eggs use a lab hatch rate per species, apart from the nest rate. Register rows D113 to D148 added: D113 to D138 hold answers, D139 to D148 hold the new questions. Work packages F1.1 to F1.12 follow the answers. Main changes:
  - F1.1: amber tubes hide their species until an Identification Tag reveals it. The pouch takes fossils by hand and lets hoppers pull them out. Random opacity and rotation are in Beta.
  - F1.2: draws can kill, and a turtle gives an embryo.
  - F1.3 to F1.8: shared chips, texture swaps on existing bones for the upgrade looks, and machine sounds per D129.
  - F1.4: the Sequencer returns an empty tube, and its monitor glows.
  - F1.5: the 90 % gate and the clone-count limit stay.
  - F1.6: synthesis gets a diamond chip time.
  - F1.7: a failed print gives a cracked artificial egg, the bone meal and the empty syringe, with a break effect. An idle printer shows a ghost egg.
  - F1.8: the lamp is the base bulb, light 9, 11, 13 and 15, 30 minutes for every species, each egg turned every 5 minutes, and a 5-minute stall death.
  - F1.9: the placed egg is an entity that hatches 60 s after placing.
  - F1.10: a cap of 3 upgrades, and tabs per network.
  - F1.11: generator glow, smoke, light 13 and its own sounds.
  - F1.12: Claude drafts the recipes, and the lead reviews them.
- **2026-10-12 (M2 open-items pass):** the lead answered most M2 open items and three M1 follow-ups. These rows are now answered or moot: D03, D04, D11 to D13, D16, D19, D20, D43, D46, D48, D50, D51 and D79. Also D87 to D90, D92, D94, D97 to D105, D107 to D110, D145 and D148. D42, D106, D111, D112 and D140 answered in part. D52 stays a conflict. RC23 resolved, RC9 moot, the D98 window conflict resolved. Register rows D149 to D182 added. Main changes:
  - Reversals: no scent trails (a predator searches the last seen spot), and personal space plus "young provoke always" for territory. The Brachiosaurus defends. Three carnivores are cathemeral, and the Gallimimus is an omnivore. Every Velociraptor has hyper intelligence from hatch. Hyper-intelligent animals climb out at dead fence sections (lead-approved exception to F27).
  - F2.0: sleep windows per species with offsets, contentment cap at the worst part + 50, effects, tame and baby rhythm, play, lost sleep, D111 values.
  - F2.1: growth times, young sizes, carcass time, named drops, five hit-part layouts.
  - F2.2 and F2.3: skin scope, morph tints, the Sorna JP3 model, own clips and sounds, limp below 30 % health, the VFX matrix.
  - F2.4 and F2.5: hunger and thirst values, thirst damage, stress step, food tiers, the need cooldown (Claude default), gestation, one nest block, pack collapse, splits.
  - F2.6 and F2.7: detection levels, trust rule, roar fear, gene slots, thermal detection, lysine, quirks, glow masks, the Lower Threshold Stress gene, yields.
  - M0.B00: species fields for personal space, sleep windows, growth and young sizes. F3.1 records the D46 exception.
- **2026-10-12 (M3 open-items pass):** the lead answered most M3 open items. D21 to D24, D26, D27, D35 and D86 answered, and gap 1 of D78. Register rows D183 to D203 added. D186, D188, D194, D200 and gap 2 of D78 stay open. Main changes:
  - Reversals: the voltage sign is the parity empty-hand click, with no item. The fence gets sounds beyond parity. The grid remembers power while a generator's chunk is not loaded (D21). No comparator output and no power meter (D27). A live fence shocks a vehicle's driver (D26).
  - F1.11: the D21 grid memory, and no generator comparator.
  - F3.1: hum, zap and snap sounds, 12 spool stages, the voltage sign cycle, and a list of the additions to parity.
  - F3.2: the Large Generator screen, blinking lights and drafted values, and the pylon range, links, feed and hum.
  - F3.3: seven in-ground hatch feeders on a timer, a virtual shaft, a gauge with overflow slots, three screens and herbivore food tags. The Live-Bait Feeder gets a pen, an interval release and bait by size.
  - F3.4: one recipe per feeder, Claude's park recipe draft, and four park advancements.
  - F4.2: the D26 shock on the driver.
- **2026-10-12 (M4 and follow-up open-items pass):** the lead answered most M4 items and the follow-up rounds on agent readings and survey defaults. D37: the lead committed the herd work on 2026-10-11, so nothing blocks M0. Register rows D204 to D218 added. Many older rows answered (see "D204 and later"). Open: D25 (Tour Station), D33, D52, D66, D111, D160, D166, D188. Main changes:
  - Reversals: lost sleep gives no stress (D105). The hover box shows icons only (D149). A strong jump clears a low fence (D47). Dilophosaurus groups hunt together (D54). Hungry packs journey (D56). The hurt-place memory lasts 1 in-game day (D61). Breeding has two more gates (D72). A fast vehicle hurts what it hits (D75). The spool shows 10 stages (D184). Only the breeding switch ships (D29).
  - M0: D37 resolved. M0.B00 keeps only `features.breeding`, and the Dilophosaurus carries poison from hatch.
  - F1.1 to F1.12: special pieces give 1.5 × DNA and go in no recipe. Four opacity levels. No jacket and no embryo-draw effect. No extra tiers. A new drive has 2 edit slots. Lab hatch start values. A gold gate for the first lab.
  - F2.0 to F2.7: the carnivore threat rule and flight in personal space. Tame stay, +2 stress per cause, and "alone" urgent after 3 days. Hover icons. Hunts, ambush, alarms and detours. Breeding gates and the genome at laying. Camouflage species, no Lysine Pills, and the Stress Tolerance gene.
  - F3.1 and F3.3: the voltage sign on the wire, with a sneak-click removal. 10 spool stages, the lever switch, and no decorative fences. Timer buttons on every feeder screen, Feeders 3 and 4, and no comparator.
  - F4.1: any lab machine as the workstation, map sellers, the Paleo-Illager skin and guards, golems, the team builder, the trade-list draft.
  - F4.2: dye variations, code motion and no fuel. Damage and iron repair, slow push and fast hurt. The road-marker track, the stop chime and names, and boarding at any time.
- **2026-10-13 (Final open-items pass and draft reviews):** the lead answered the last open items and reviewed Claude's seven drafts. The drafts: recipes, power, trades, fossil generation, clutches, skin sets by biome and the cathemeral herbivore windows. The lead approved each draft with changes. Register rows D219 to D255 added, and many older rows answered (see "D219 and later"). D52 resolved, so no reconciliation conflict stays open. Still open: D08 (guns), D14, D91, D112 (in part), D249, D250 and D252 to D255. Main changes:
  - Reversals: loneliness is part of stress (D52). The female builds a nest from local blocks (D152). Dye tints a vehicle, and a Wrench changes its variation (D204). Vehicle damage waits for a later release (D206). Detours start only at an urgent need (D36). The pylon head sits on a fence pole (D240). An overload trips the generator (D242).
  - Status and M0.B00: the final-pass bullet, personal-space radii, clutch values, shifted herbivore windows.
  - F1.1 to F1.12: the fossil generation table, Fortune +7.5 %, the pelvis and the Embryo Syringe. By-product weights, the vanilla card, slider range, donor pick and risk curve. Also synthesis diamond 2 min, 16 bone meal, printer lamps, the claw, the run-cost rule, the power numbers, the recipes and the id renames.
  - F2.0 to F2.6: the need cooldown, claims and herd meals, the safety part and shifted windows. Skin sets and raptor looks, warning sounds and bloody claws. The built nest, clutches and the 3-day nest eggs.
  - F3.1 to F3.4: fence power sounds, generator values, pylon design and cable cost, and feed points. The close clip, the code-drawn chain and the park recipes.
  - F4.1 and F4.2: the 1,000-block map, the trade table, the golem drop, the lab house, the Wrench, no Tour Station and no vehicle damage.
  - "Recipes and progression gates": every approved grid, the progression table, and the five open M4 rows (D250).
- **2026-10-13 (Last open items, Z-8):** the lead answered every row that the final pass left open, and two later points. D249, D250, D252 to D255 and the rest of D112 answered. D83 changed, and D79, D138, D204, D215, D228 and D240 updated. The five final-pass flags resolved. Only D08 (guns) and D14 (animation speed) wait for the lead, and D91 waits for the artist. Main changes:
  - Reversals: the player assembles each vehicle from parts (D250), which replaces "one Jeep item and one Explorer item". The four lab machines alone cost about 45 iron value. The cocoa allergy leaves Beta. The Identification Tag leaves Beta, and sequencing identifies amber DNA (D83).
  - F1.1, F1.2 and F1.4: no tag item, no tag recipe and no Identified tube recipe. An amber tube stays Unidentified, and the Sequencer names its species on the drive entry.
  - F1.12 and "Recipes and progression gates": 11 machine-part grids re-costed, so the four machines cost about 44 and the first setup about 61. Ten M4 rows: Wrench, chassis, wheel, engine, seats, two bodies, Tour Computer, tour track and stop marker.
  - F2.0, F2.2, F2.5 and F2.7: the nocturnal window 10:00 to 18:00, raptor looks per trait from a random parent, the T. rex nest rate 6 %, failed nest eggs that drop tissue, the camouflage wait, and llama item carrying and spit.
  - F1.1 and F3.2: Dilophosaurus Jurassic rarity 2.8. Only a pylon pole takes any height, and fence posts keep 3 and 8 blocks.
  - F4.1 and F4.2: 2 random trades per level and the normal house spawn rate confirmed. Vehicle assembly, the part recipes and the stop marker lamp.
- **2026-10-13 (lab part recipes):** the lead approved all 17 lab machine-part grids (D256). Main changes:
  - Reversals: quartz slabs replace smooth stone, and no lab part uses a stone material. The Incubator nest is an empty iron tray, with no wool and no hay.
  - The Processor lid has no nugget and no glass pane. The Sequencer door takes an iron trapdoor. The Incubator arm takes ingots, not nuggets.
  - The four machines cost about 50 iron value, 4 gold ingots and 14 quartz slabs. The first setup costs about 66. This replaces the Z-8 sum of about 45.
  - The first lab now needs nether quartz, so a Nether trip. The lead accepted this gate. Progression stage 3 needs gold and quartz.
  - New key letters: U (quartz slab) and A (any light block).
