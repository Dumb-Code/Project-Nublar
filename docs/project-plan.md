# Project Nublar: Beta and Release 1 project plan

Date: 2026-10-05. Status: draft for the project lead's review. Nothing in this plan is started.

> **For agentic workers:** this is the master plan. It defines phases, modules, scope and the order of work. Do not implement from this file alone. When you pick up a module, first write that module's detailed task plan (see "How to use this plan", step 3), then implement it with `superpowers:subagent-driven-development` or `superpowers:executing-plans`. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** ship a playable Beta of Project Nublar on Fabric and NeoForge for Minecraft 26.2, then a Release 1 that adds plants and four species, with every module built to the design the project lead confirmed on 2026-10-05.

**Architecture:** one MultiLoader project (`common`, `fabric`, `neoforge`). Game logic lives in `common`. Each loader module holds only registration glue, platform services, datagen and the client entrypoint. Energy and fluid storage go through Balm, which bridges to each loader's native API. Species data, fossil sets, diets and gene effects are datapack JSON. Global balance numbers are one TOML file.

**Tech stack:** Java 25, Minecraft 26.2, Fabric Loader and Fabric API, NeoForge, GeckoLib 5, SmartBrainLib 2, Architectury API, Common Networking, Forge Config API Port (for TOML on Fabric), Balm (energy and fluid storage only), Gradle 8 with ModDevGradle and fabric-loom, JUnit 5 for unit tests, the vanilla GameTest framework for in-game tests.

**Spec:** [docs/codebase-analysis.md](codebase-analysis.md). Section 11 is the design the project lead confirmed. Sections 2 to 6 describe the code as it is. Section 8 lists gaps and the art inventory. Read section 11 before every module. Where this plan and section 11 differ, section 11 wins and this plan has a bug.

---

## Global constraints

Every module's requirements include these lines.

- Loaders: Fabric and NeoForge. Both must be playable at the Beta release. No single-loader shortcut (section 11.17).
- Game version: Minecraft 26.2. Java 25. Re-check loader and library releases before the port starts, because releases move (section 11.17).
- Config split: TOML for global balance numbers (yields, times, chances, power). Datapack JSON for per-species data (stats, fossil sets, diets, gene effects). Tags for other mods' items. Where the split is unclear, pick the simpler format (section 11.17).
- Every number the project lead marked "tune in playtest" is a config value with the stated default. Never hard-code it.
- Block and item ids may change. No world-compat shims. Fix `carrnivore_feeder_one` and `iron_plant_tank_ugprade` (section 11.17).
- Stats: the Tyrannosaurus card values are approved. The interim values in section 11.16 are the working values for the other five species until real cards arrive. Do not invent other stats. State the basis of every number you add.
- Dinosaurs never spawn naturally. Cloning is the only source (section 11.9).
- Do not re-open decisions the lead marked "not decided" (guns, licence) unprompted. Build around them.
- Licence: do not change `LICENSE` or the `license` value in `gradle.properties` until the lead decides (section 11.17).
- Code style: Javadoc on every public type and every public method that is not an override. No `System.out` or `System.err`. No chat messages for debugging. No `Minecraft.getInstance()` in common or server code. All player-facing text through translation keys in `en_us.json`.
- Code quality standards set by the project lead on 2026-10-05. They apply to every module and every review:
  1. Keep the code as simple, well formatted, clean, documented and readable as possible.
  2. Keep the codebase as light as feasibly possible: optimized, efficient, performant, and following the basics of OOP. No speculative abstractions, no utility layer for one caller, no new dependency when the JDK or Minecraft already has it. Measure before optimizing, but never leave known per-tick waste.
  3. Review your own work as an adversary would, but do not add or remove code for the sake of it. Think carefully before every change.
  4. A human must be able to pick up wherever you left off: read, modify, extend, continue or rewrite the work without you. Javadoc, a short package note and tests that explain intent are how that happens.
- Every network packet from the client validates its payload on the server: menu open, slot range, value range.
- Datagen output in `common/src/generated/resources` is regenerated, never hand-edited, and regenerated in the same commit as the provider change.

## Cross-cutting risks

Inputs the design implies but no single module owns. Each line names the owning module, which must add the test.

1. **A dinosaur from `/summon` or a spawn egg has zero behaviour data** and starves or divides by zero. Owner: B16. Test: summon each species and check that stats load from species data, not from the egg.
2. **A dedicated server crash from client classes in common code** (`Minecraft.getInstance()` in `IncubatorMenu`, `distmarker` imports in five common files). Owner: P2 (port). Test: run the dedicated server through the full machine chain with a GameTest.
3. **A player loses items on block break** (sequencer drops 3 of 9 slots, feeder saves nothing, printer forgets its sensor and chip). Owner: B12. Test: fill every slot, break the block, count drops.
4. **Worldgen that floods a chunk** (the fossils branch places a vein attempt per chunk per stone and its rarity test always passes). Owner: B01. Test: a unit test on the rarity math plus a count of fossil blocks over 100 generated chunks against the config target.
5. **A genome edit the server never checks** (`UpdateEditInfoPacket` has no range, menu or content check; issue #49). Owner: B07. Test: send a packet with 300% and with a locked gene, and assert the server rejects both.
6. **Three block-scanning sensors per dinosaur scan about 150k blocks** each cycle. Owner: B17. Test: a timing GameTest with 20 animals in one paddock keeps the server tick under 50 ms.

---

## How to use this plan

This section is written for a human developer and for an AI agent alike. Follow it in order.

### 1. Pick work in phase order

Phases run in sequence: P0, P1, P2, then Beta modules, then P4, then Release 1. Inside the Beta phase, follow the module order in "Build order" unless the lead says otherwise. Modules in the same build-order tier are independent and can run in parallel on separate branches.

### 2. Check the module's gate

Each module lists `Depends on`. Do not start a module until each dependency is merged into the integration branch. If a dependency is only partly merged, say so to the lead and wait, or pick a module whose dependencies are met.

### 3. Write the detailed task plan first

This file gives scope, not steps. Line numbers in the analysis refer to commit 4244f62 and will be wrong after P0 and P2. So, before any code:

- [ ] Read the module entry here and the section 11 sub-sections it cites.
- [ ] Read the current code for every path under `Touches`, and confirm or correct the paths.
- [ ] Write `docs/superpowers/plans/YYYY-MM-DD-<module-id>-<name>.md` in the `superpowers:writing-plans` format: file list, interfaces, and bite-sized TDD tasks with real test code and real implementation code. No placeholders.
- [ ] Put every config key the module adds, with its default, in that plan and in `docs/config-reference.md` (created by P1).
- [ ] Have the lead or a reviewer read the task plan before implementation. An agent sends it to the lead and waits.

### 4. Implement on a branch

- [ ] Branch name: `<module-id>/<short-name>`, for example `b01/fossil-worldgen`, from the integration branch.
- [ ] One commit per task. Conventional prefix (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`).
- [ ] Test first for every piece of pure logic (math, data records, NBT round trips, rules). Write the failing test, run it, see it fail, then implement.
- [ ] Run `./gradlew build` for all modules before every push. A compile failure on either loader blocks the push.
- [ ] Run both clients and the dedicated server for every module that touches blocks, entities, screens or packets. Record what you did in the PR.

### 5. Definition of done

A module is done when all of these are true:

- Every item under `In scope` is implemented or the PR says which item was cut and why.
- Every item under `Acceptance` passes, and the PR body lists each one with "pass" or "fail" and the evidence.
- The module compiles and runs on Fabric and NeoForge. The feature was exercised in both clients.
- Unit tests exist for the module's pure logic and pass in `./gradlew :common:test`.
- No debug output, no TODO without an issue number, no dead code added.
- Config keys are in `docs/config-reference.md` with defaults and the section 11 reference.
- `en_us.json` has every new key. Datagen was re-run if a provider changed.
- The PR links this plan and the module id, and names the open decisions it worked around.

### 6. When you hit a design gap

Section 11 does not cover everything. When an implementation choice changes gameplay and section 11 is silent:

- Do not infer the answer from silence or from an unselected option. Both were wrong six times out of eight in the design pass.
- Build the version with the smallest gameplay commitment, behind a config value where a value is involved.
- Write the question, a concrete in-game example, and the choice you made into the PR under "Decisions for the lead". An agent also asks the lead through its question tool, with multi-select enabled.
- Add the question to "Decisions register" at the end of this file.

### 7. Reviewer checklist

A reviewer (human, or a fresh agent that did not write the code) checks:

- [ ] The task plan was followed, or deviations are explained.
- [ ] Both loaders build and the PR shows evidence of running both.
- [ ] Every "tune in playtest" number is a config value.
- [ ] Packets validate on the server.
- [ ] No common-code reference to loader or client-only classes.
- [ ] Tests exist for the logic and fail without the implementation (spot-check by reverting one line).
- [ ] Translation keys, datagen and config docs updated.
- [ ] Nothing outside the module's scope changed without a reason in the PR.
- [ ] The four code quality standards hold: the simplest design that meets the acceptance list, no dead weight or per-tick waste, defects hunted as an outsider would, and a stranger could continue the work from the code and its docs alone.

---

## Phases

| Phase | Name | Exit condition |
| --- | --- | --- |
| P0 | Branch integration | `1.20.1-ai` plus `rft/adversing` merged, fossil design ported from `fossils-1.20.1`, builds on Forge and Fabric at 1.20.1, machine chain and fences work in game |
| P1 | Repository hygiene and CI | Clean tree, CI builds both loaders on every PR, unit test source set exists, README and metadata are real |
| P2 | Port to Minecraft 26.2 | Fabric and NeoForge modules build and run on 26.2, Botarium replaced, all 1.20.1 features work again on both loaders |
| B | Beta modules | Every Beta module done per its acceptance list |
| P4 | Beta hardening and release | Balance pass, performance pass, licence set, public Beta published |
| R | Release 1 modules | Every Release 1 module done |

---

## Phase P0: branch integration

Source: analysis section 2.6. The order is ai, then rft, then port the fossil design. Do not merge `fossils-1.20.1`.

**Touches:** whole tree. `J/init/*` becomes `J/registry/*`, `J/entity/ai/**` becomes `J/entity/dinosaur/ai/**`, `J/api/*` splits, `nublar-gui` module appears.

**Tasks:**

- [ ] Create the integration branch `integration/1.20.1` from `1.20.1-ai` (4244f62). Delete `hs_err_pid20864.log` in the first commit.
- [ ] Merge `rft/adversing` with `git merge -X find-renames=30%`. Resolve the 13 shared files. Move the feeder AI classes (`NearestFeederSensor`, `EatFromMeatFeeder`, `WalkToNearestFeeder`) to `entity/dinosaur/ai/{sensor,task}`.
- [ ] Fix the two Javadoc blocks with a raw `->` (`ProjectNublar.java`, `api/fossil/FossilCollection.java`) so doclint passes.
- [ ] Check the swapped previous/next arguments at the two fence connection construction sites from commit 59d9518. Write a unit test on `Connection` ordering before fixing.
- [ ] Build both loaders with `./gradlew build`. Fix compile errors before any behaviour work.
- [ ] Run the Forge client. Build a fence of 4 posts, power it, break a wire, repair it with the spool, break a post. Record the result.
- [ ] Port the fossil design from `fossils-1.20.1` by copying, not merging: `FossilBlock` with the `Quality` block state, `FossilBase`, `FossilFeature`, the `fossil_filter` and `fossil_height` placement modifiers, overlay textures, and the datagen providers. Keep all six species registered. Keep `BehaviourDataReloadListener` and the diet lookup. Fix the quality roll (missing `break`) and the rarity test (`nextDouble() < 1.0 / chance`) as you port them, with unit tests. Use the worldgen `RandomSource`, not `new Random()`. Remove the `System.err.println`.
- [ ] Regenerate `common/src/generated/resources`. Do not merge the fossils branch's generated files.
- [ ] Build both loaders again. Run the Forge client: a new world has fossil blocks, mining one drops a fossil item, the processor accepts it.
- [ ] Open a PR to the GitHub default branch. Ask the lead to change the default branch to the integration branch after merge.

**Acceptance:**

- `./gradlew build` passes for `common`, `forge`, `fabric` and `nublar-gui`.
- Forge client: fossil mined, processed, sequenced, synthesised, printed, incubated, hatched. Fence powers, hurts, breaks, repairs.
- Fabric client at least starts to the title screen. (Fabric parity is P2's job; P0 must not regress it below where it is.)
- No file from `hs_err_*`, `forge/dataRun/**`, `G/.cache/**` or `.ai/` is in the tree.

**Size:** L. **Depends on:** nothing.

---

## Phase P1: repository hygiene and CI

Source: analysis section 3.5 and 8.4.

- [ ] Remove `shapes/*.txt`, `J/mixin/ExampleMixin.java` (if rft did not), the `com.example` service files, the zero-byte `.ai/mcp/mcp.json`. Add `hs_err_*.log`, `*/dataRun/`, `*/.cache/` to `.gitignore`.
- [ ] Replace `README.md`: what the mod is, the Beta scope in one paragraph with a link to section 11, how to build, how to run each loader, how to contribute (link this plan and the "How to use this plan" section).
- [ ] Fix metadata: `mod_author`, `description`, `credits`, `pack_format`, placeholder URLs in `fabric.mod.json`, the missing logo, dependency declarations in `mods.toml` and `fabric.mod.json` (GeckoLib, SmartBrainLib, Architectury, energy library, Common Networking) with correct sides.
- [ ] Add a `common` unit test source set: JUnit 5 on `./gradlew :common:test`. First test: `Quality` ordering and the fossil rarity math from P0. The source set must compile without a running game (pure Java only). Put Minecraft-dependent tests in GameTests, not here.
- [ ] Add `.github/workflows/build.yml`: on every PR and push, set up the Java toolchain, run `./gradlew build :common:test`, upload the two jars as artifacts. Cache Gradle.
- [ ] Add `.github/PULL_REQUEST_TEMPLATE.md` with the "Definition of done" checklist from this plan.
- [ ] Add `docs/config-reference.md` with the table header `Key | Default | Type | Section 11 ref | Added by module` (B00 fills it).
- [ ] Do not touch `LICENSE` or `license=` (open decision).

**Acceptance:** CI green on the integration branch for both loaders. `git status` clean after a build. README answers "how do I run this" without reading Gradle files.

**Size:** S. **Depends on:** P0.

---

## Phase P2: port to Minecraft 26.2

Source: analysis section 11.17. Target chosen by the lead: 26.2, Fabric and NeoForge, Java 25, unobfuscated (no Parchment). The lead confirmed on 2026-10-05 that the port comes before every Beta module.

**Before starting, verify on that day:** current stable versions of NeoForge for 26.2, Fabric Loader, Fabric API, GeckoLib, SmartBrainLib, Architectury API, Common Networking, Forge Config API Port, ModDevGradle, fabric-loom, and the MultiLoader Template branch for 26.2. Write them into the port task plan with dates and URLs.

**Tasks:**

- [ ] Create branch `p2/port-26.2` from the integration branch.
- [ ] Replace the build: take the MultiLoader Template branch for 26.2 (`common`, `fabric`, `neoforge`, `buildSrc`). Move the `forge` module's content into `neoforge`. Keep `nublar-gui` as a source set the way rft set it up. Set `java_version=25`, `minecraft_version=26.2`, remove Parchment properties.
- [ ] Replace Botarium with Balm, for energy and fluid storage only (decided by the lead on 2026-10-06). Balm's `common` module gives `EnergyStorage`, `FluidTank`, `BalmEnergyStorageProvider` and `BalmFluidTankProvider`; its loader modules bridge them to NeoForge's `IEnergyStorage` and `IFluidHandler` capabilities and, on Fabric, to Team Reborn Energy and the Fabric Transfer API. That bridge is what lets other mods' cables connect on both loaders (section 11.8). Do not write an in-house adapter. Keep Architectury, Common Networking and Forge Config API Port for everything else; Balm's registry, event, network and config layers stay unused. The 8 users are `Connection`, `BlockEntityElectricFencePole`, `DinosaurFeederBlockEntity`, `EggPrinterBlockEntity`, `GeneratorBlockEntity`, `IncubatorBlockEntity`, `ProcessorBlockEntity`, `SequencerBlockEntity`. Balm has shipped 26.2 builds for Fabric and NeoForge since 2026-06-16 (Apache 2.0, Java 25).
- [ ] Re-check Common Storage Lib (Terrarium, Botarium's successor) on the day P2 starts. On 2026-10-06 it supported only 1.21 and 1.21.1 (version 0.0.10, July 2026) and had no 26.x branch. If it has a stable 26.2 release by then, compare it against Balm on API fit, cable-mod compatibility and maintenance record before the Botarium replacement begins, and record the comparison in the port task plan. Also check whether Architectury's unmerged `feature/fluid_transfer_ext` branch has landed.
- [ ] Replace every Forge-only API in `common`: the five `distmarker` imports, `BlockEntityElectricFence*Mixin` model data, `GeoMultiBlockItemMixin`, `ForgeConfigSpec` usage (use Forge Config API Port's spec class on both loaders, or Night Config directly, chosen in the port plan).
- [ ] Give Fabric everything section 3.4 lists as missing: client entrypoint, reload listeners, death message hook, spool repair handler, the `fossil` and `test_tube` model loaders, fence model data, dependency declarations. Register reload listeners in `common` through a platform service so both loaders share one list.
- [ ] Move `IncubatorMenu`'s `Minecraft.getInstance()` call to the client screen.
- [ ] Upgrade GeckoLib 4.7 to GeckoLib 5 (5.5.x on 2026-10-06; decided by the lead on 2026-10-06 to do this inside P2). Every GeckoLib class the mod imports moves from `software.bernie.geckolib` to `com.geckolib` (31 files). Five classes the mod uses no longer exist: `AnimationState`, `CoreGeoBone`, `Color`, `EntityModelData`, `RenderProvider`. Rewrite the animation predicates, the bone code, the render layers and the item renderer providers to the GeckoLib 5 API. Keep the current look and animations; changes to them belong to B09.
- [ ] Upgrade SmartBrainLib 1.15 to SmartBrainLib 2 (2.0.x on 2026-10-06; same decision). `BrainUtils` no longer exists (22 files use it). `BrainActivityGroup` no longer exists (the dinosaur brain in `Dinosaur.java` is built on it). `SBLLoader` no longer exists (it registers the sensors). Seven classes moved package: `ExtendedBehaviour`, `DelayedBehaviour`, `OneRandomBehaviour`, `FirstApplicableBehaviour`, `PredicateSensor`, `SquareRadius`, `SmartBrainProvider`. Keep the brain's behaviour as it is; brain changes belong to B16 to B19.
- [ ] Port datagen to run on NeoForge and emit the same output. Add a Fabric datagen entrypoint only if the NeoForge datagen cannot produce loader-neutral files; prefer one datagen.
- [ ] Update CI for Java 25 and the two new module names.
- [ ] Update `docs/codebase-analysis.md` section 3.1 (versions table) and section 3.4 (parity list) to the new state. Keep the STE style of that document.

**Acceptance:**

- `./gradlew build` passes for `common`, `fabric`, `neoforge`.
- On each loader's client and on each loader's dedicated server: the full machine chain from fossil to hatched T. rex works, fences power and repair, the generator powers machines, a GeckoLib dinosaur renders and animates.
- A GameTest `machine_chain_smoke` runs on both loaders in CI and passes. It places a creative generator, the four machines and a creative fossil, and asserts an egg appears.
- A third-party energy cable mod connects to the generator on each loader (one mod per loader, named in the PR).
- Each of the six species renders with its layers and plays its walk, idle and attack animations on both loaders.
- The brain does what it did before the port, on both loaders: a T. rex drinks, eats dropped meat and hunts a cow (a GameTest, or a manual check recorded in the PR).
- `grep -r "software.bernie.geckolib" common neoforge fabric` returns nothing.
- `grep -r "net.minecraftforge" common/src` returns nothing. `grep -r "botarium" .` returns nothing. Balm is used only from the eight storage block entities: `grep -rl "net.blay09" common/src` lists nothing outside them and the platform service.

**Size:** XL. **Depends on:** P1. **Blocks:** every Beta module.

---

## Beta modules

Each module below has the same fields. "Section" cites `docs/codebase-analysis.md`. "Touches" names current locations as of 4244f62 plus rft's moves; confirm them when you write the task plan. "Produces" names what later modules rely on. Names in "Produces" are proposals for the detailed plans to confirm; once a module merges, its names are fixed.

### Build order

| Tier | Modules | Why this tier |
| --- | --- | --- |
| 0 | B00 | Everything reads config and species data |
| 1 | B01, B05, B14, B16 | The survival loop: find fossil, process it, power it, animal has real stats |
| 2 | B02, B03, B06, B07, B11, B12, B15, B26 | Complete the lab and the paddock. Large animals move and herds travel. |
| 3 | B08, B09, B10, B13, B17, B20, B27 | Editing depth, appearance, needs, feeders, recipes, section hitboxes |
| 4 | B04, B18, B19, B21, B22, B24 | Social life, player interaction, world content, vehicles, art |
| 5 | B23 (blocked), B25 | Guns wait on a decision; cleanup last |

Tier 1 produces the first fully playable survival loop. Stop and playtest at the end of tier 1 and tier 2 before continuing. B24 (art) starts as soon as P2 lands, in parallel, because B09, B14, B20 and B22 wait on its exports.

---

### B00: Config foundation and species data

**Section:** 11.17, 4.6, 6.4. **Size:** M. **Depends on:** P2.

**Goal:** one way to read a global balance number and one way to read per-species data, on both loaders, with reload.

**Touches:** `J/data/*ReloadListener`, `J/api/DinoBehaviourData`, `R/data/projectnublar/config/dinosaurs/*.json`, `R/data/projectnublar/diet/`, new `J/config/`.

**In scope:**

- A TOML file `config/projectnublar-common.toml` with sections `fossils`, `processor`, `sequencer`, `editing`, `incubation`, `power`, `fences`, `needs`, `breeding`, `vehicles`. Every key the later modules list goes here. Loaded through Forge Config API Port on Fabric and NeoForge's config on NeoForge, behind `J/config/NublarConfig` with typed getters.
- A species data record `J/api/dinosaur/SpeciesData` loaded from `data/projectnublar/species/<species>.json` through one reload listener registered on both loaders. Fields: id, clade, stats (`max_health`, `health_regen_ticks_per_half_heart`, `attack_damage`, `defense`, `speed`, `energy`, `hunger`, `thirst`), `group_size_min`/`max`, `egg_clutch_min`/`max`, `hatch_success`, `growth_days`, `base_height_blocks`, `height_range_min`/`max`, `gender_size_weights`, `incubation_minutes`, `diet` tag id, `prey_preferences` (clade to weight), `fossil_set` id, `amber_weight`, `biomes` (biome tag to percentage), `depth_min`/`max`, `period`, `variants` (filled by B09), `stress_threshold` (B17), `flee_or_stand` (B19), `territory_radius` (B19). Unknown fields fail loudly at load. The lead named flee or stand only for Gallimimus (flees) and Triceratops (stands); the other four take the decision D13 default.
- Replace `R/data/projectnublar/config/dinosaurs/*_config.json` and `DinoBehaviourData` with `SpeciesData`. Fix the `default_stamina`/`default_energy_capacity` key mismatch by deleting both.
- Fill the six species files: Tyrannosaurus from its card (section 7.8), the other five from section 11.16's tables, with a `"_basis"` field naming the source of each block of values.
- Datapack validation: a unit test loads every shipped species file through the codec and asserts no field is missing.
- `docs/config-reference.md` filled for every key added.

**Out of scope:** any gameplay change. Values are moved, not changed, except where section 11.16 says the working values differ.

**Produces:** `NublarConfig` typed getters; `SpeciesData` and `SpeciesRegistry.get(EntityType)`; the reload-listener platform service.

**Acceptance:**

- `/reload` after editing a species JSON changes a summoned animal's max health on both loaders.
- Editing the TOML and restarting changes a processor run time on both loaders.
- Unit tests: codec round trip for `SpeciesData`; every shipped species file parses; a file with a typo in a key fails to parse.
- `grep -r "DinoBehaviourData" common/src` returns nothing.

---

### B01: Fossil ore worldgen

**Section:** 11.1, 2.4, 7.10. **Size:** L. **Depends on:** B00.

**Goal:** fossil ore generates in four forms with per-block quality, species chosen by biome and depth, and a visible, tinted, randomly rotated overlay.

**Touches:** `J/worldgen/` (ported in P0), `J/block/FossilBlock`, `J/api/fossil/`, biome modifiers in both loader modules, `F/datagen/ModWorldGenProvider` (now `neoforge`).

**In scope:**

- Four forms, each a configured feature: `single_ore`, `small_cluster`, `skeleton_cluster`, `digsite_deposit` (the last is placed by B21's structure, but the feature belongs here).
- One cluster is one species. A skeleton cluster is one animal: piece presence follows the piece weights of the species' fossil set; the card's "quantity max" is the usual maximum; `fossils.over_max_chance` (default 0.05) lets a piece exceed it.
- Rarity formula from issue #1: ore rarity x period rarity x species share x piece share x quality share, implemented in one pure class `FossilRarity` with unit tests that reproduce the section 11.1 example (a pristine skull is about 1 in 200 T. rex ores).
- Biome percentages scale the amount per biome group; a species never generates in an unlisted biome. Biome groups are biome tags under `data/projectnublar/tags/worldgen/biome/`.
- Depth per species from `SpeciesData`; T. rex default y 40 to 75.
- Quality: a cluster rolls a base quality; each block rolls near it (`fossils.quality_spread`, default 70% base, 15% one tier up, 15% one tier down, clamped). Quality is the block state. The overlay texture differs per quality (fix the shared fragmented/poor art).
- Host stones: stone, sandstone, deepslate. Deepslate features actually target deepslate depths.
- The block stores the biome group it generated in (block state) so the drop can carry it (B02).
- Overlay aesthetics from `Fossil Ore Aesthetics.docx`: tint from the host, random tint strength 0 to 90%, random opacity 15 to 100%, random rotation. Implement as a baked model with per-position randomness from the block position seed (no block entity needed for look).
- Fabric `BiomeModifications` and NeoForge biome modifiers both place the features.

**Out of scope:** terracotta host, trace and generic fossils, fossil pouch, drops and items (B02), amber (B03), digsite structures (B21).

**Produces:** `FossilBlock` with properties `QUALITY` and `BIOME_GROUP`; `FossilSet` record (pieces with weight and usual max) in `data/projectnublar/fossil_sets/`; `FossilRarity` math.

**Acceptance:**

- A 100-chunk sample in a listed biome at the right depth yields fossil blocks within 20% of the config target; an unlisted biome yields zero. Done with a GameTest or a debug command that counts and is removed before merge.
- Unit tests: rarity shares, over-max chance, quality spread distribution, skeleton piece selection never emits a piece outside the set.
- Visual check on both loaders: tint, opacity and rotation vary block to block; quality is readable on the overlay.

---

### B02: Fossil items, drops and DNA yield

**Section:** 11.1 (quality, DNA yield, pieces), 4.1, 8.5. **Size:** M. **Depends on:** B01.

**Goal:** mining a fossil ore gives a fossil item that knows its species, piece, quality and biome, and that the processor turns into DNA at the configured yield.

**Touches:** `J/item/FossilItem` (new), `J/api/DNAData`, `J/api/FossilCollection` (delete), `J/api/loot/functions/*`, loot providers, item textures.

**In scope:**

- One `FossilItem` with component data: species, piece, quality, biome group. Tooltips show all four. Quality ladder: fragmented, poor, common, pristine.
- Loot table based drops with Fortune (chance of one tier up, `fossils.fortune_tier_up_chance` per level) and Silk Touch (drops the ore block). Delete the `getDrops` override.
- DNA yield per quality in TOML: 5, 7.5, 10, 30 percent. Special pieces (skull, tooth, claw) multiply by `fossils.special_piece_yield_multiplier` (decision D01).
- Item art: generic bone sprites per piece shared across species; species-specific sprites for skull, tooth, claw from the asset repository (80 sprites exist). Fix the zero-byte `pelvis.png` and the Triceratops copies of T. rex files.
- Translation keys for every piece and quality.
- Special pieces are tagged `projectnublar:special_pieces` for B13 recipes and B21 trades.
- Remove `FossilCollection`'s static maps and the `COLLECTIONS.put` return bug; remove `FossilItemFunction`'s empty-list `.get()`.

**Out of scope:** skeleton exhibit assembly, trace fossils.

**Produces:** `FossilItem` data accessors (`species()`, `piece()`, `quality()`, `biomeGroup()`); `DNAData` gains `biomeGroup` and `source` (FOSSIL, AMBER, SYRINGE, REMAINS, EGG).

**Acceptance:**

- Mining each quality with no enchant, Fortune III and Silk Touch gives the expected drop distribution over 200 breaks (GameTest).
- Unit test: yield for each quality and for a special piece.
- Processor run on a pristine T. rex skull gives the configured yield x filter efficiency on both loaders.

---

### B03: Amber

**Section:** 11.2, 4.2. **Size:** M. **Depends on:** B01, B05.

**Goal:** amber ore generates, drops an amber item with no visible species, and the processor rolls the species at the end of the run.

**Touches:** `J/block/AmberBlock`, `J/item/AmberItem`, `J/block/entity/ProcessorBlockEntity`, `J/menutypes/ProcessorMenu`.

**In scope:**

- Amber ore block in the three host stones, placed by the B01 feature set with its own rarity, storing depth band and biome group.
- Amber item carries depth band and biome group only. Tooltip says "Unknown species".
- Processor accepts amber (fix the `dna_percentage` tag check). At run end it rolls the species from `amber_weight` of every species valid at that depth and biome, and the yield uniformly from 0 to `fossils.amber_max_yield` (default 80). No quality tiers.
- If no species is valid for that depth and biome, the run yields a tube of the species with the nearest depth band (decision D02).

**Out of scope:** drill extractor, one species per deposit.

**Produces:** `AmberItem`; `SpeciesSelector.rollAmber(depth, biomeGroup, random)`.

**Acceptance:** unit test on the roll distribution with two species; GameTest processes an amber on both loaders and the tube names a species valid for its biome.

---

### B04: Living DNA sources

**Section:** 11.2, 11.9. **Size:** S. **Depends on:** B05, B16.

**Goal:** the player's own animals supply DNA.

**Touches:** `J/item/SyringeItem`, `J/block/entity/ProcessorBlockEntity`, `J/item/RemainsItem` (from B16).

**In scope:**

- Syringe on a vanilla mob: random 0 to 100%, a small damage hit (`sequencer.syringe_damage`, default 1.0), no cooldown.
- Syringe on a live dinosaur: same, yield range `sequencer.dino_syringe_min/max` (decision D04).
- Remains items (fresh bone, tissue) drop from carcasses (B16) and process in the fossil processor at a high yield (`processor.remains_yield`, decision D04).
- Egg sampling: syringe on an unincubated or incubated egg gives DNA of the egg's genome (decision D03 on whether it harms the egg).

**Produces:** `DNAData.source` values SYRINGE, REMAINS, EGG used by B06.

**Acceptance:** each source produces a tube that the sequencer accepts, on both loaders; unit tests on the yield ranges.

---

### B05: Fossil processor completion

**Section:** 11.4, 4.2, 7.4. **Size:** M. **Depends on:** B00, B14.

**Goal:** the processor behaves per section 11.4 and is automation-friendly.

**Touches:** `J/block/entity/ProcessorBlockEntity`, `J/menutypes/ProcessorMenu`, `J/item/FilterItem`, Balm's fluid tank and the loader's item storage API.

**In scope:**

- One run always gives one tube. Results keep decimals; rounding is `processor.rounding` (NONE, HALF, WHOLE; default NONE).
- By-products: sand, gravel, iron nugget, gold nugget, rarely diamond, with weights in TOML, into the 3x3 output grid.
- Hopper and pipe support: item storage exposed per side (input, filter, tube, output) through Balm's fluid tank provider and each loader's item storage API on both loaders. Fluid input accepts water from pipes and buckets.
- Filter efficiency: fix NaN for `dev_filter`.
- Tank tiers 2, 3, 4, 8 buckets already match; keep.
- No mixed queue, no parallel upgrade.

**Produces:** `ProcessorBlockEntity` side handlers; `ProcessorInput` (fossil, amber, remains).

**Acceptance:** a hopper chain feeds fossils and pulls tubes unattended for 10 runs on both loaders; unit test on rounding modes; by-product weights sum check.

---

### B06: Sequencer and drives

**Section:** 11.5, 4.3. **Size:** M. **Depends on:** B00, B02.

**Goal:** sequencing accumulates per species and variant, tracks biome share, and the Isolated view works.

**Touches:** `J/item/DiskStorageItem`, `J/api/DiskStorage`, `J/block/entity/SequencerBlockEntity`, `J/client/screen/SequencerScreen`.

**In scope:**

- Drive data: map of species-or-variant id to percent (capped 100), plus per-species biome-group share. No capacity limit. HDD 10 s per sample, SSD 5 s.
- Variants: parrot colours, cat breeds, and others with vanilla variant data keep separate entries.
- Fix `getContainerSize` (9 slots) so a broken sequencer drops everything.
- Fix `DiskStorageItem.getGeneCompletion` NaN for genes with no donor (gender).
- Isolated view (section 7.2): per gene, the percent and the animals still needed.
- Remove the "Cretaceous/Apple" placeholder.

**Produces:** `DriveData` record with `percent(id)`, `biomeShare(species)`, `geneCompletion(gene)`.

**Acceptance:** sequencing two parrot colours gives two entries; biome share updates after a tube from a desert fossil; unit tests on addition and cap; break-and-drop count on both loaders.

---

### B07: Gene editing core

**Section:** 11.6 (thresholds, donors, risk, gene effects, gene data), 4.3, 4.6, 7.5, issue #49. **Size:** XL. **Depends on:** B00, B06, B16.

**Goal:** edits are validated on the server, saved in the genome, and change the animal in all four gene groups.

**Touches:** `J/api/DinoData`, `J/api/GeneData`, `J/api/Genes`, `J/registry/GeneInit`, `J/network/c2s/UpdateEditInfoPacket`, `J/client/widget/GeneButton`, `F/datagen/GeneDataProvider` (now `neoforge`), new `J/api/gene/GenomeEffects`, `GenomeRisk`, `EditRules`.

**In scope:**

- Thresholds in TOML: clone at 50, edit at 70, gender sure at 80 with 30% random below; donor slots at 50/75/100, max 3.
- `DinoData` saves `layerColors`, base species, the rolled variation (once the animal has hatched), and overrides in `toNBT`, `fromNBT`, `copy`. Round-trip unit test.
- Server validation of `UpdateEditInfoPacket`: menu open, percentages sum to 100, base at or above threshold, donor count within slots, each gene override within the gene's range and only if isolated. Reject and log otherwise.
- Gene registry additions: `thirst`, `energy`, `egg_clutch`, `gestation_time`. Keep `immunity` (acts on poison in Beta).
- Gene application: one `GenomeEffects` class maps every gene to an attribute or a `SpeciesData` modifier at spawn and on growth tick. All four groups must do something observable: core stats (health, regen, strength, defense, speed, size), needs (stomach, eat rate, thirst, energy), behaviour (aggression, intelligence, herd/pack size, tamability, nocturnal), breeding and survival (fertility, clutch, gestation, immunity, underwater, heat, jump). Behaviour genes feed the memories B17 to B19 read.
- Risk model: a pure `GenomeRisk` class returns incubation failure chance, stat trade-off list and abnormal-behaviour chance from donor share and edit count, with TOML coefficients. B08 displays it; B11 applies it.
- Per-clone variation: stats within `editing.variation_percent`, size with gender weights from `SpeciesData`, layer colours, albino/melanistic at `editing.morph_chance` (default 0.05). Variation rolls at hatch, not at synthesis (decided by the lead on 2026-10-06), so an egg item carries no rolled values and hatching stays a reveal. B11 calls the roll.
- Gene data: regenerate from `Vanilla Animal DNA Effects.txt` (section 7.5 lists every difference). Add frog, camel, sniffer, armadillo and wolf variants now that 26.2 has them. Villager DNA randomises every gene per use.
- Fix `GeneButton` writing 0 into `advancedGenes` on select.

**Out of scope:** the screen (B08), skins and colour rendering (B09), special genes (B10), disease.

**Produces:** `DinoData` complete; `GenomeEffects.apply(Dinosaur)`; `GenomeRisk.evaluate(DinoData)`; `EditRules` (thresholds, slots) used by B08 and B11.

**Acceptance:**

- Unit tests: donor slot count per percent, gender rule, validation rejects out-of-range and non-isolated edits, risk monotonic in donor share, `DinoData` NBT round trip including colours and base species.
- In game on both loaders: a +30% speed edit measurably changes the movement speed attribute; a herd size edit changes the group memory; a fertility edit changes the clutch roll.
- A forged packet is rejected (a GameTest that calls the handler directly with bad data).

---

### B08: Edit screen

**Section:** 11.6 (gene control, appearance controls), 7.2. **Size:** L. **Depends on:** B07, B09.

**Goal:** the sequencer's Edit tab matches the 2022 layout with a Basic and an Advanced view and shows risk before synthesis.

**Touches:** `J/client/screen/SequencerScreen`, `nublar-gui` widgets, `J/menutypes/SequencerMenu`.

**In scope:**

- Basic view: entity preview with a player for scale, donor sliders, sequenced-animal list, effect text ("Speed +3%").
- Advanced view: isolated gene controls (slider for numeric genes, list for choice genes: gender, skin variant, special gene), per-layer colour with saturation, opacity and tint strength; primary/secondary mode as the default colour mode; fixed layers (claws, teeth, nostrils, eyes, mouth) not listed.
- Risk panel: incubation failure chance, stat trade-offs, abnormal behaviour chance from `GenomeRisk`.
- Skin variant list unlocked at 100%; otherwise shows the biome-decided variant read-only.
- Widgets live in `nublar-gui`. No `Minecraft.getInstance()` in menus.

**Out of scope:** machine tabs (B12), disease risk.

**Acceptance:** every control round-trips to the server and back after reopening the screen; a reviewer can edit speed, gender and a layer colour and see the hatched animal reflect all three, on both loaders.

---

### B09: Appearance, skins and rendering fixes

**Section:** 11.6 (appearance), 6.1, 6.3, 8.5, issues #60 and #63. **Size:** L. **Depends on:** B07, B16, B24 (skins).

**Goal:** each animal renders its own genome: layers, colours, gender texture, biome skin variant, morphs, and the render scale targets.

**Touches:** `J/client/renderer/DinosaurRenderer`, `J/client/renderer/dinosaurs/*Renderer`, `J/client/renderer/layer/DinoLayer`, `J/client/CommonClientClass` layer registration, textures.

**In scope:**

- Per-entity layer selection: fix `DinosaurRenderer.createLayers` building layers once from the first entity. Layers read the entity's `DinoData`.
- Layer colours applied from `DinoData.layerColors` with saturation, opacity and tint strength; fixed layers never tinted.
- Gender textures for all six species. Import male skins for Gallimimus and Brachiosaurus from the asset repository; flag the Dilophosaurus male skin as missing art.
- Skin variants: data-driven list per species in `SpeciesData.variants` (id, biome group, texture set). Beta ships several variants for at least some species (Velociraptor Nublar/Sorna and Triceratops JP3 exist as art).
- Albino and melanistic morph rendering.
- Render scale: base height from `SpeciesData.base_height_blocks` divided by measured model height, computed once per species. Targets from section 11.16 (decision D11): Triceratops 3.0 blocks, Brachiosaurus 12.5, others near their current size. Smooth growth scale (B16) multiplies it.
- Dilophosaurus layer cost: collapse the four frill variants into one selected layer; stop double-drawing white passes when tint is white.
- Running animation wired (`RUN_ANIM`); flinch lengths per species from the animation file, not hard-coded.
- Render culling box follows scaled size (issue #63).

**Out of scope:** missing animation art (B24), hitboxes (B16).

**Produces:** `DinosaurRenderer` reading per-entity data; `SkinVariant` record.

**Acceptance:** two T. rex of different genders and colours stand side by side and render differently on both loaders; a Dilophosaurus herd of 10 keeps client FPS above a baseline recorded in the PR; Triceratops measures about 3 blocks at full growth.

---

### B10: Special genes, diet genes and quirks

**Section:** 11.6 (special genes and extras), 7.6. **Size:** L. **Depends on:** B07, B17.

**Goal:** all seven special genes and the extras exist and do something, reachable through commands where the donor animal does not exist.

**Touches:** `J/registry/GeneInit`, new `J/api/gene/special/*`, `J/entity/dinosaur/ai/**`, a new command class, creative tab.

**In scope:**

- Special genes as a choice gene with one slot: poison (bite or spit applies Poison), gender swapping (a single-gender group flips one animal over `editing.gender_swap_days`, default 10), lysine contingency (needs a lysine item or weakens and dies; item and recipe in B13), camouflage (hard to see for players beyond a range while still; compatible species list in data), bioluminescence (emissive layer), hyper intelligence (opens doors, takes from chests, detects unpowered fences, +15% detection), thermal detection (detects players through walls within a range).
- Donors: cave spider and bee for poison, vanilla frog for gender swapping, glow squid for bioluminescence, camel/parrot/wolf/fox/mule for hyper intelligence, salmon/squid/glow squid/frog for thermal detection. Cuttlefish, reed frog, tree frog and pit viper do not exist: `/projectnublar gene grant <player> <gene>` and a creative-tab item give those genes until the animals exist.
- Diet genes: grass eating and mushroom eating add blocks to the animal's diet.
- Quirks: clucking, sound imitation, cocoa allergy, item carrying.
- Donor palette shifts: a donor's palette nudges layer colours by a data-defined hue shift.
- Immunity gene reduces Poison duration.

**Out of scope:** hybrids, disease.

**Acceptance:** each special gene has a GameTest or a scripted manual check listed in the PR, on both loaders; the command grants a gene a survival player cannot otherwise reach.

---

### B11: Synthesis, egg printer, incubator and hatch

**Section:** 11.7, 4.4, 7.4. **Size:** L. **Depends on:** B07, B14.

**Goal:** the chain from genome to hatchling matches section 11.7.

**Touches:** `J/block/entity/SequencerBlockEntity` (synthesis), `J/block/entity/EggPrinterBlockEntity`, `J/block/entity/IncubatorBlockEntity`, `J/block/IncubatorBlock`, `J/item/IncubatedEggItem`, `J/recipe/UnincubatedEggRecipe` (delete), `J/registry/ItemInit` (container upgrades), `J/network/c2s/UpdateIncubator*Packet`.

**In scope:**

- Synthesis takes a bird-embryo syringe and outputs a dinosaur-embryo syringe carrying `DinoData`. Remove `UnincubatedEggRecipe`. Printer takes the embryo syringe plus bone meal and outputs the unincubated egg.
- Printer saves its sensor and chip. 10% crack without the leveling sensor stays.
- Incubator: capacity 3, 6, 9 (fix the `ContainerUpgradeItem` registration). Needs power, a bulb and plant matter; stalls without any one; an egg left stalled for `incubation.stall_death_minutes` dies. Plant matter capacities 100, 150, 200 per the spec. The player places each egg; the arm turns them.
- Incubation failure from `GenomeRisk` rolled at completion, separate from hatch success.
- Incubation time per species from `SpeciesData.incubation_minutes`, scaled by the bulb.
- Hatch: the player places the incubated egg; it wobbles over the last `incubation.wobble_seconds`; hatch success from `SpeciesData.hatch_success` (T. rex 5%); failure leaves a broken egg item and remains. Egg block versus entity is decision D05.
- Hatch rolls the per-clone variation (B07) and stores the result on the animal.
- Egg tooltip (decided by the lead on 2026-10-06): the unincubated and incubated egg show species, donor mix, the player's edits, the incubation-failure chance from `GenomeRisk`, and the chosen gender, marked "may be random" below 80%. No rolled traits. All lines through translation keys; cache the parsed genome so a hovered tooltip does not parse it every frame.
- A hatched dinosaur starts at the beginning of growth (B16's growth fraction 0). Today `IncubatedEggItem` spawns an adult and never calls `setBaby`, so growth never starts.
- Sequencer water tiers to the spec (1, 1.5, 2, 2.5, 3 buckets) with 500 mB fill steps.
- About 50 minutes machine time at base tier for one dinosaur: verify by timing and record in the PR.
- `UpdateIncubatorSlotPacket` validates index and position; remove or use `UpdateIncubatorPacket`.

**Produces:** `EmbryoSyringeItem`, `UnincubatedEggItem` and `IncubatedEggItem` carrying `DinoData`; the hatching egg block or entity.

**Acceptance:** GameTest on both loaders: creative setup prints and incubates three eggs, one per capacity tier; a stalled egg dies; a placed egg hatches a dinosaur whose genome matches the edit and whose growth fraction is 0; two eggs printed from one genome hatch animals with different rolled values; the egg tooltip shows the incubation-failure chance and no rolled trait; unit tests on hatch and failure rolls.

---

### B12: Machine framework

**Section:** 11.8, 7.1, 7.4. **Size:** L. **Depends on:** B00, B14.

**Goal:** all four machines are built from parts in order, upgrades show, machines link with tabs, and no machine loses items.

**Touches:** `J/block/{Processor,Sequencer,EggPrinter,Incubator}Block`, their block entities, `J/block/api/multiblock/*`, GeckoLib block models, `nublar-gui` tab widget.

**In scope:**

- Part-by-part assembly for processor (base, body, lid, tanks), sequencer (base, door, computer, monitor) and egg printer (base, then needle, platform, body in any order), matching the incubator's existing right-click assembly. Parts are items with recipes (B13).
- A creative-only item that places a fully assembled machine.
- Upgrade visuals: tank, chip and bulb tiers change the model (GeckoLib bone visibility or blockstate). Dye recolours the sequencer's black sections.
- Faster chips draw more power (`power.chip_multiplier` per tier). Idle machines draw zero. No power, no progress.
- Cross-machine tabs: a screen shows tabs for every machine linked by adjacency or by a `linked` list in the block entity; switching keeps the screen open. Tab icons exist in the asset repository.
- Item retention: every machine drops every slot on break; one GameTest per machine.

**Produces:** `MachineBlockEntity` base with `powerDraw()`, `isIdle()`, `linkedMachines()`; `MachinePartItem`.

**Acceptance:** build each machine from parts in survival on both loaders; break each full machine and recover all items; tabs switch between four linked machines; the energy meter falls only while a machine works.

---

### B13: Recipes and progression

**Section:** 11.8, 4 (one recipe exists), 8.1. **Size:** M. **Depends on:** B12, B02.

**Goal:** every block, part, upgrade and tool has a survival recipe from vanilla materials, with no loot-only or trade-only item.

**Touches:** datagen `ModRecipeProvider`, advancement provider (new), creative tabs in `J/registry/CreativeTabInit`.

**In scope:**

- Recipes for: machine parts, tanks (iron, gold, diamond, netherite), chips, bulbs, filters, HDD and SSD, syringes, test tubes, container upgrades, plant tanks, leveling sensor, generators (B14), conduit and pylons (B14), fence posts and spool (B15), feeders (B20), vehicles and track (B22), lysine item (B10), voltage signs (B15), switch block (B15).
- Special pieces as optional ingredients that unlock nothing exclusive (section 11.8: no part is trade-only or loot-only).
- Advancements for the first fossil, first tube, first egg, first hatch, first paddock.
- Creative tabs ordered by chain stage.

**Acceptance:** a survival playthrough from a fresh world reaches a hatched animal without creative or trading (a reviewer does this once per loader and records the time); unit test that every registered item in the mod's creative tabs has a recipe or is marked creative-only.

---

### B14: Power

**Section:** 11.11 (power), 4.5, 7.7. **Size:** L. **Depends on:** P2 (Balm energy storage), B00.

**Goal:** machines and fences draw real power; generation, transport and balance match the lead's starter-park target.

**Touches:** `J/block/entity/GeneratorBlockEntity`, the four machine block entities, new `J/block/ConduitBlock`, `J/block/PylonBlock`, `J/block/entity/BlockEntityElectricFencePole` draw path.

**In scope:**

- Remove the simulate flag: machines extract what they draw each tick (`power.machine_draw.<machine>.<tier>`).
- Generator saves energy. Basic generator burns any furnace fuel at furnace rate (coal 80 s), output `power.basic_output_fe_per_tick`. Large generator uses fuel `power.large_fuel_multiplier` times longer with higher output. Creative generator stays.
- Power conduit block (the mod's own cable) and pylons with lines: pylons connect like fence posts up to `power.pylon_range` blocks, lines carry power with no loss. Pylon and generator models exist in the asset repository and need conversion (B24). This module may ship with placeholder models if art is late, and must say so.
- Other mods' cables connect through Balm's energy bridge on each loader.
- Balance: one basic generator runs all four machines at base tier plus about 10 low-security posts. Write the arithmetic in the task plan and in `docs/config-reference.md`, and make it hold with the defaults.
- Fence draw per post from B15's `FencePowerService` (rft) using Balm's `EnergyStorage`.

**Produces:** `GeneratorBlockEntity` (basic, large, creative), `ConduitBlock`, `PylonBlock`, `PowerNetwork` helper that fences and machines query.

**Acceptance:** GameTest on both loaders: one basic generator with coal keeps four base-tier machines running and a 10-post fence powered, and runs short when a second paddock is added; energy survives a world reload; a third-party cable powers a machine.

---

### B15: Fences and containment

**Section:** 11.11 (containment, fence types, damage and access), 2.3, 5.1, 5.2. **Size:** L. **Depends on:** P2, B14, B16.

**Goal:** the rft fence rework plus wire health, post knockdown, security tiers, player damage, switching and signs, on both loaders.

**Touches:** `J/block/api/fence/*` (rft), `J/block/ElectricFencePostBlock`, `J/block/ElectricFenceBlock`, `J/block/api/BlockConnectableBase`, `J/block/api/Connection`, fence renderer, new switch block and sign blocks, `J/entity/dinosaur/ai/behavior/BreakFenceBehaviour`.

**In scope:**

- Keep rft's `FencePowerService`, connection dedupe, cascade breaks and renderer. Port its Forge-only model data mixins to the P2 platform layer.
- Wire health: each wire has `fences.wire_health.<tier>`; an animal hit removes its attack damage; a wire at zero breaks; the spool repairs one wire per use. High security has more health and deals more damage (`fences.damage.<tier>`).
- Post knockdown: an animal above `fences.knockdown_mass_threshold.<tier>` can knock a post down with a chance per hit; a fallen post drops its item and breaks its wires.
- Height: posts of 3 and 8 blocks already exist; jump checks use the live height.
- Live wire hurts players (already) and animals; animals avoid a live wire (memory `FENCE_IS_LIVE`), test an unpowered one, and starved or stressed animals attack a live one; hyper-intelligent animals detect unpowered sections (B10 reads the same memory).
- Live-wire deterrent (decided by the lead on 2026-10-06): each shock gives a stressed animal a `fences.deterrent_chance.<tier>` chance to give up its escape attempt; a starving animal never gives up. Default and basis set in the task plan; tune in playtest.
- Switch block and redstone: a fence section turns off on signal.
- Three voltage signs as decorative blocks from the asset art.
- Feeders fillable from outside the paddock: a feeder placed in a post line keeps the wire run intact (coordinate with B20).
- Remove the forced `|| true` and the 171 commented lines in `BlockConnectableBase`. Remove the all-players chat message in `BreakFenceBehaviour`.
- Gates and the overhanging fence are not in Beta.

**Produces:** `FencePowerService` (rft) extended with `wireHealth`, `damage`; memory module `NEAREST_WEAK_FENCE` for B17 and B19.

**Acceptance:** GameTest on both loaders: a T. rex breaks a low-security wire in fewer hits than a Gallimimus; over 100 trials a stressed animal sometimes gives up after a shock and a starving one never does; a redstone signal de-energises a section and the animal tests it; a spool repairs a broken wire; both clients render broken halves and sparks; a dedicated server runs the fence paddock with no client class errors.

---

### B16: Dinosaur core

**Section:** 11.9, 11.16, 6.1, 6.4, issues #61, #63, #64. **Size:** XL. **Depends on:** B00.

**Goal:** each species has its own stats, grows smoothly, scales hitbox and stats, dies into a carcass, and babies behave as babies.

**Touches:** `J/entity/dinosaur/Dinosaur`, `CarnivoreDinosaur`, `HerbivoreDinosaur`, the six species classes, `J/entity/dinosaur/PartEntity`, `J/registry/EntityInit`, `J/registry/MemoryModuleTypeInit`, `J/registry/AttributesInit`, `J/util/DinoNeedsUtils`, `J/entity/dinosaur/ai/util/FenceAwareNodeEvaluator`, new `CarcassEntity`, `RemainsItem`.

**In scope:**

- Attributes from `SpeciesData` at spawn and on `/reload`; no dependence on the egg to set behaviour data.
- Smooth growth: age in ticks from 0 to `growth_days` x 24000; size, hitbox and stats interpolate; the `size` gene and variation scale the end point; no step thresholds. Persist age correctly (fix the four flags read from one key).
- Hitbox: `EntityDimensions` from the render-scaled torso and legs, refreshed on growth through the 26.2 `scale` attribute. Section hitbox parts are module B27, and large-body movement is module B26 (decision D06, answered 2026-10-09). Re-test "spinning on block edges" (issue #64) after B26's arrival fix and record the result.
- Path check by body size: `FenceAwareNodeEvaluator` uses the mob-aware path check and looks for fence blocks across the whole body width. Today it calls the one-block check, so a large animal can path through a gap one block wide.
- Water path cost (decided by the lead on 2026-10-06): the same evaluator adds a cost to water deeper than the animal's current height, scaled by the underwater-capacity gene, so babies and small species go around deep water and large ones wade. It is a cost, not a wall: an animal with no other route still swims. Addresses the drowning in issue #61.
- Debug status (decided by the lead on 2026-10-06): each animal syncs its most urgent need and its current activity to the client when they change. The debug stick shows them to the holder only, on the action bar, in creative mode or for operators. It replaces the stick's broadcast to all players.
- Death: `tickDeath` implemented; a carcass stays for `needs.carcass_minutes`, then decays; drops meat (raw per species from the asset repository), fresh bones, tissue (remains for B04), and special drops (raptor toe claw, Triceratops horn, teeth; art is missing, use placeholders and flag in B24).
- Babies: follow a parent, cannot hunt, need feeding; stop `SoloHuntRoamBehaviour` for babies and herbivores.
- `MobCategory` review: keep MONSTER only if despawn rules need it; prefer CREATURE with `persistenceRequired`.
- Clean the memory module registry: unique ids for `HAS_GROUP` and `IS_GROUP_LEADER`; delete the 12 unused types; fix the trust attribute translation key.
- Prey tables from `SpeciesData.prey_preferences` by clade (T. rex card), players and villagers included; the `canTarget` overrides in Velociraptor and Dilophosaurus respect the parent result.

**Produces:** `Dinosaur.getSpeciesData()`, `Dinosaur.getGrowthFraction()`, `Dinosaur.getEffectiveStat(Stat)`; `CarcassEntity`; `RemainsItem`.

**Acceptance:** summon each species: stats match its data file; a T. rex reaches full size after 45 in-game days with `/time` acceleration (GameTest with a sped-up clock); the hitbox grows with the model; an adult T. rex does not path through a gap one block wide; a baby T. rex walks around a pond three blocks deep when a land route exists; the debug stick shows need and activity to its holder only; killing a Triceratops leaves a carcass that drops a horn; unit tests on growth interpolation and stat scaling.

---

### B17: Needs

**Section:** 11.10 (needs, stress, results, carnivores, herbivores), 6.2. **Size:** XL. **Depends on:** B16, B15, B20.

**Goal:** five needs with consequences, species diets through tags, grazing with reach, carnivore prey ranking and scavenging.

**Touches:** `J/util/DinoNeedsUtils`, `J/entity/dinosaur/Dinosaur` (running state), `J/entity/dinosaur/ai/behavior/needs/*`, `J/entity/dinosaur/ai/sensor/*`, `R/data/projectnublar/diet/` (becomes tags), `hunt_score_config.json` (delete).

**In scope:**

- Needs: hunger, thirst, stamina and sleep, social, stress. Rates from `SpeciesData` and genes. Fix `IS_HUNGRY` never clearing, the Drink distance mismatch, `isSociallyDrained` reading stamina, `tickSocial` being empty, and `IS_EXHAUSTED` never set.
- Stress inputs in Beta: food and water, company. A live-bait hunt lowers stress (B20). No enclosure or block comfort.
- Consequences: escape attempts on fences (uses B15 memories), aggression, health loss and death; a carnivore that kills a player regains hunger.
- Starvation (decided by the lead on 2026-10-06): at zero hunger the animal loses `needs.starvation_damage` health (default 1) every `needs.starvation_interval_ticks` (default 80, the vanilla player rate), so a larger animal lives longer. With the section 11.16 health values a Velociraptor (35) lasts about 2.3 minutes and a T. rex (155) about 10. Tune in playtest. While starving it attacks fences and turns aggressive. Today `tickHunger` kills it on the next hunger tick, so the fence attack almost never happens.
- Need cooldown (decided by the lead on 2026-10-06): a met need clears its memory (`IS_HUNGRY` and the others) and starts `needs.cooldown_seconds.<need>`, during which the animal does not seek that need again. This stops eat-drink-sleep loops and cuts path requests.
- Stamina-limited chases (decided by the lead on 2026-10-06): a chase ends when stamina reaches zero, then `needs.hunt_cooldown_seconds` passes before the next hunt. Prey and players can escape a tired predator. Playtest that every carnivore still feeds, since starvation is lethal.
- Brain activities (decided by the lead on 2026-10-06): group the behaviours into SmartBrainLib activities with priorities (at least core, idle, rest, hunt, fight; B18 adds social and breeding) instead of one ordered list. Build on the SmartBrainLib 2 structure from P2. GameTests cover each activity switch.
- Running state from speed: set "running" from the actual movement speed each tick (the same signal as B09's gait choice), not from a flag. Today the chase sets a flag that never clears, so stamina drains at double rate until the chunk reloads.
- Carnivores: prey ranking by clade from `SpeciesData`; scavenging of carcasses and dropped meat; players and villagers as prey.
- Herbivores: graze blocks in a diet tag (grass, leaves, crops, flowers), consuming the block; reach from `SpeciesData` (Brachiosaurus high, Triceratops low); herbivore feeder (B20).
- Diets as item tags and block tags per species under `data/projectnublar/tags/`, so other mods' plants and meat can be added.
- Sensor cost: replace the three per-entity block scans with a shared chunk-section cache or a POI search; budget under `needs.sensor_budget_blocks` per tick.
- Debug output removed from `DinoNeedsUtils`, sensors and behaviours.

**Produces:** `NeedsState` on the entity with getters per need; memories `STRESS_LEVEL`, `IS_STARVING`, `IS_STRESSED`.

**Acceptance:** GameTest on both loaders: a starving T. rex attacks a live fence before it dies; stamina drain returns to the walking rate after a chase ends; a T. rex chasing a sprinting player stops when its stamina reaches zero; an animal that has eaten does not walk to food again during its cooldown; a fed, watered pair has falling stress; a Brachiosaurus eats leaves at height 8 and a Triceratops does not; 20 animals in one paddock keep the server tick under 50 ms; unit tests on need decay and stress arithmetic.

---

### B18: Social life and breeding

**Section:** 11.10 (groups, breeding). **Size:** L. **Depends on:** B17.

**Goal:** herds and packs with leaders, calls, raptor pack hunting, and natural breeding with nests.

**Touches:** `J/entity/dinosaur/ai/behavior/social/*`, `J/entity/dinosaur/Dinosaur` (mating), `SetMateFromNearbyDinosaurs`, new `GroupManager`, `NestBlock`.

**In scope:**

- Group formation per species within `group_size`; a leader; rank contests without turf wars; calls (warn, gather, threaten) with sound events (B24 imports the sounds).
- Dominance (decided by the lead on 2026-10-06): a dominance value from size, age and the aggression gene picks the leader and settles rank contests. A contest plays `ThreatDisplay` and does no damage. The leader chooses where the group goes.
- Pack hunting for Velociraptor: pack members share a target and flank.
- Breeding: male and female within a group; the female builds a nest block and lays `egg_clutch` eggs; eggs hatch at `hatch_success`; adults guard nests and young. Fix the `isMate` UUID comparison and the `nextInt(1,2)` clutch. Give each young its own copy of the genome; today `Dinosaur.java` passes the mother's own `DinoData` object, so a change to the young's gender changes the mother's.
- Fertility and gestation genes feed the roll.
- Breeding gates (decided by the lead on 2026-10-06): a pair breeds only with a nest in range, the fertility roll passed, and both animals' stress under their species `stress_threshold`. Neglected paddocks stop breeding, which also limits entity growth.
- Heredity (decided by the lead on 2026-10-06): number genes take the midpoint of the parents plus the per-clone variation (B07). Colour layers, skin, special gene and morph each come from one parent at random, and the young also gets the normal `editing.morph_chance` roll. Every bred gene is clamped to the edit limits, so breeding cannot pass the caps that B07 enforces. Gender is a 50/50 roll (default; the lead did not decide it separately). The rule lives in one pure function with unit tests.
- Turf wars and false eggs are out of scope; leave `StartTurfWar` and `LargeCarnivoreFight` disconnected with a note, or delete them.

**Produces:** `GroupManager` (per-level map of group id to members); `NestBlock`.

**Acceptance:** six Gallimimus form one herd with one leader; three raptors converge on one prey; a T. rex pair produces a nest whose eggs follow the clutch and hatch rates over 20 trials (GameTest with accelerated time); a unit test shows that a change to a young's genome leaves the mother's unchanged; unit tests on the heredity rule (midpoint, choice genes from one parent, clamp to edit limits); a stressed pair does not breed; the leader is the animal with the highest dominance; both loaders.

---

### B19: Player interaction and detection

**Section:** 11.10 (the player, detection), 6.2 (taming stub). **Size:** L. **Depends on:** B17, B18.

**Goal:** threat displays, flee or stand, territory, taming, and the three detection channels.

**Touches:** `ThreatDisplay`, `J/entity/dinosaur/Dinosaur` (taming), new `DetectionService`, `ScentTrail` saved data, `J/entity/dinosaur/ai/sensor/*`.

**In scope:**

- Threat display before attack (`ThreatDisplay` connected; roar, frill per species).
- Flee or stand per species from data; territory radius; adults attack near young.
- Taming of calm, low-stress animals; owner safe; follow, stay, defend; the bond breaks on hunger or stress; `PLAYER_REPUTATION` written.
- Detection: sight and sound; scent points that creatures drop and that fade (`needs.scent_lifetime_seconds`); detection chance 25 to 85% by distance and player activity; no movement-based vision.
- Hyper intelligence and thermal detection (B10) plug into the same detection pipeline.

**Produces:** `DetectionService.chanceToDetect(animal, player)`; `ScentTrail` level data.

**Acceptance:** a sneaking player at 30 blocks is detected less often than a sprinting one at 10 (GameTest with 100 samples); a tamed raptor follows and defends; a Triceratops charges and a Gallimimus flees; both loaders.

---

### B20: Feeders

**Section:** 11.12, 5.3. **Size:** M. **Depends on:** B16, B14.

**Goal:** working carnivore, herbivore and live-bait feeders with screens and automation.

**Touches:** `J/block/DinosaurFeederBlock`, `J/block/entity/DinosaurFeederBlockEntity`, `EatFromMeatFeeder`, `WalkToNearestFeeder`, `NearestFeederSensor`, new herbivore and live-bait feeder classes, feeder screens.

**In scope:**

- Fix the carnivore feeder: slot bug, item stack aliasing, save contents, hunger actually increases on eating, map key type, range mismatch, sensor cache expiry, rename the id.
- Herbivore feeder accepting the herbivore diet tag.
- Live-bait feeder: the player leads a vanilla animal in; the feeder holds it; releases it when a hungry carnivore is near; the hunt lowers stress.
- Screens for all three; hopper and pipe input; fill from outside the paddock (B15 integration).
- Feeder models from the asset repository (need Blockbench export; B24).

**Acceptance:** a hopper keeps a carnivore feeder stocked and a T. rex eats from it with a rising hunger value; a cow in the live-bait feeder is released to a hungry raptor; both loaders.

---

### B21: Digsites, paleontologists, Paleo-Illagers and maps

**Section:** 11.3, 8.6 (digsite cards). **Size:** XL. **Depends on:** B01, B02, B03.

**Goal:** world content that teaches where to dig and gives a trade loop for fossils.

**Touches:** new `J/worldgen/structure/*`, structure NBT and jigsaw JSON under `data/projectnublar/worldgen/`, new villager profession and POI registration, map item, illager entity variant.

**In scope:**

- Digsite structures (jigsaw): a rich fossil deposit from B01's `digsite_deposit` feature, tents, loot chests, paleontologist villagers, iron golems. Sizes small to large; larger is rarer.
- Paleontologist villager profession with a workstation block; trades for emeralds: maps, fossils, amber and special pieces bought and sold, tools and parts sold. Spawns in digsites and at a low rate in villages.
- Paleo-Illager sites: a hostile variant guarded by illagers; small and poor or large and rich; clearing it gives fossils, chests and machines. No neutrality item, no "ancient plight".
- Maps: an explorer-map item that leads to the nearest digsite, optionally naming a species; sold by paleontologists, cartographers and wandering traders.
- The illager skin is missing art; use a retextured vindicator and flag in B24.

**Acceptance:** `/locate structure projectnublar:digsite` finds one in a fresh world within 2000 blocks; a map bought from a paleontologist points to it; an illager site attacks and, once cleared, yields fossils; both loaders.

---

### B22: Vehicles

**Section:** 11.12 (vehicles). **Size:** XL. **Depends on:** P2, B24 (models).

**Goal:** a player-driven Jeep Wrangler and a 1992 Ford Explorer tour vehicle with a track, stops and driverless tours.

**Touches:** new `J/entity/vehicle/*`, `J/block/TrackBlock`, `J/block/TourStopBlock`, client renderers, input packet.

**In scope:**

- Vehicle entity base: seats, driver input, collision, GeckoLib model. Fuel is decision D07.
- Jeep Wrangler: driven, two or more seats.
- Ford Explorer: driven by hand off track; follows a track block the player lays; stops at stop markers for `vehicles.stop_seconds`; runs driverless tours in a loop.
- Track block and stop marker block with recipes (B13).
- Models exist in the asset repository and need Blockbench export (B24).

**Out of scope:** gyrosphere, monorail, aircraft, boats, schedules beyond a loop with stops.

**Acceptance:** a player drives the Jeep around a paddock; the Explorer completes a laid loop with two stops unattended and carries a passenger; both loaders.

---

### B23: Guns (blocked)

**Section:** 11.12, 11.14. **Size:** unknown. **Depends on:** decision D08 (kinds of gun and which of the 11 models ship).

Do not start. Do not re-ask unprompted. When the lead decides, write the module entry here with scope, then the task plan. Known inputs: the design document's three kinds (tranquilizer, stun and capture, lethal); ammo sprites for a dart, a taser cartridge and a shotgun shell; 11 gun models needing export; a tranquilised state implies a `tranqed` animation on the T. rex card that the file lacks.

---

### B24: Art integration

**Section:** 8.5, 8.6, 6.1. **Size:** L (mostly coordination). **Depends on:** P2 and the asset repository. **Blocks:** B09, B14, B20, B22.

**Goal:** every Beta feature has its art in the mod, and gaps are visible to the art team.

**Touches:** `R/assets/projectnublar/**`, `J/registry/SoundInit`, `docs/art-status.md` (new).

**In scope:**

- Import from `ProjectNublar-Assets`: sounds for the five species with none; male skins for Gallimimus and Brachiosaurus; fossil and bone sprites; meat sprites; paleontologist skin; GUI tab textures; voltage signs.
- Blockbench exports to GeckoLib for: feeders (1 to 6, fish feeder), Jeep Wrangler, Ford Explorer, guns (when B23 unblocks).
- Conversions from `.dcm`/`.tbl`: basic and large generators, four pylons, egg models.
- Missing animations: Velociraptor `flinch`; Dilophosaurus `getup`; Gallimimus `drink`, `eat1`, `getup`, `dead`, `roar`; the card's `tranqed`, `injured`, `sleeping`, `sitting`. Rename Triceratops `Sprint ` and `injured` to `run` and `flinch` if they are the same.
- Missing sprites: raptor toe claw, Triceratops horn, teeth; illager skin; Dilophosaurus male skin.
- Keep a table `docs/art-status.md`: asset, Beta module, state (in mod, needs export, needs conversion, no art), owner.

**Acceptance:** `docs/art-status.md` has no "unknown" rows; every Beta module's placeholder list is empty or linked to an art task.

---

### B25: Cleanup and performance pass

**Section:** 6.4, section 10 item 14. **Size:** M. **Depends on:** all other Beta modules merged.

- Delete unused classes (`DinosaurPart` in the loader module, `IDinoPart`, `IMultiPartDino`, `DinoTexture`, `EntityColorStore` if B16 did not use them), unused memory types, `DebugStick` chat output, every debug print.
- Profile a park of 30 animals, 4 machines and 60 posts on a dedicated server; record tick time per system; fix anything over budget.
- Resolve every `TODO(BUG)` from rft that is still open, or convert it to a GitHub issue.

**Acceptance:** `grep -rn "System.out\|System.err\|printStackTrace" common/src` returns nothing; the profile is recorded in the PR.

---

### B26: Large-body movement and herd travel

**Section:** 11.9, 11.10. **Spec:** [docs/dinosaur-hitboxes-and-movement-design.md](dinosaur-hitboxes-and-movement-design.md), sections "Movement" and "Anti-clipping and IK" (planning half), plus its decisions register. **Size:** XL. **Depends on:** B00, B15, B16. **Added:** 2026-10-10, after the port (P2).

**Goal:** large and long dinosaurs walk through gaps, corridors and forests without spinning, wedging or clustering. Herds travel as a wedge and switch to single file in corridors.

**Starting point:** the herd-movement redesign in the herd-leader worktree (1.20.1, uncommitted). Port it with P2, then build the items below on 26.2.

**Touches:** `FenceAwareNavigation`, `FenceAwareNodeEvaluator`, `J/entity/ai/behaviour/social/*` (`HerdMoveControl`, `HerdSpacing`, `HerdBodies`, `BodyFit`, `ClearanceBox`, `TravelOrder`, `LeaderTrail`, `JourneyPolicy`), `Dinosaur` (growth, dimensions, turning), species config (`group_size`, leaf and log values, pitch values).

**In scope (in this order):**

- [ ] Read 26.2 `PathNavigation.followThePath`, `Path.getEntityPosAtNode` and `WalkNodeEvaluator` from the NeoForge 26.2 and Fabric 26.2 toolchain sources. The arrival-test defect is confirmed on 1.20.1 only.
- [ ] Per-species block filter (pass, break, solid). Every block reader uses it: `Entity.collide` (mixin), `BodyFit`, the node evaluator, the sweep shortcut and the IK. Herd and IK code read block shapes directly, never through `noCollision`.
- [ ] Arrival test at the aim point, the box-sweep shortcut, and a symmetric footprint.
- [ ] Movement box from the render-scaled torso and legs. Growth through the 26.2 `scale` attribute, a full `getDimensions` override and the Fossils growth guard. The clearance box is split by job: torso width for paths, whole model for herd spacing.
- [ ] Turn gates: a hard gate in tight ground, a soft gate in open ground, and the anti-orbit guard.
- [ ] Stuck ladder that changes the path. Its last rung snaps to a clear spot, also when a player is near.
- [ ] Trees: leaves pass or break per species config. Only the main box breaks leaves, above a growth cut-off. Large species break logs only when charging. With `mobGriefing` off, breakers pass through leaves and logs stay solid.
- [ ] Capsule herd spacing with a circle broad phase. In-line pitch by the chest rule. Today's tight spacing in corridors and for big herds. The trail grows with the herd's single-file column, and animals past the trail tail follow the animal ahead.
- [ ] Corner clearance as a soft path cost. A spine danger term in follower steering.
- [ ] Brachiosaurus: open ground only, at most one large-species path per herd per tick.

**Produces:** `BlockFilter` per species, the shared per-species spine definition, the corrected navigation.

**Acceptance:** GameTests on both loaders, registered in common code:
- a dinosaur of whole-number width advances through every node
- a Gallimimus walks under a 3-high canopy
- a 20-animal herd passes a 2-wide corridor in single file and regroups after it
- a growing animal next to a fence never clips into it
- a stuck animal reaches the snap rung and moves on

The server tick stays inside the spec's performance budget for 20 and 100 dinosaurs. Record the profile in the PR.

---

### B27: Section hitboxes and IK

**Section:** 11.9. **Spec:** [docs/dinosaur-hitboxes-and-movement-design.md](dinosaur-hitboxes-and-movement-design.md), sections "Hitboxes", "Anti-clipping and IK" and "Cross-loader implementation". **Size:** XL. **Depends on:** B26, B09. **Added:** 2026-10-10, after the port (P2).

**Goal:** arrows and melee hits land on the visible head, neck, torso, legs and tail of every species. Necks and tails bend away from blocks. Feet sit on the ground.

**Touches:** `Dinosaur`, `J/entity/dinosaur/PartEntity` and both `DinosaurPart` classes (removed first), the species renderers, `EntityInit`, new part, pose-state and bake classes, a Gradle bake task, NeoForge and Fabric loader modules.

**In scope (in this order):**

- [ ] Remove the dead T. rex head-part code: both `DinosaurPart` classes and the renderer writes to `DINOSAUR_HEAD_POS`.
- [ ] Synced pose state: animation id, start tick and loop flag, owned by the server.
- [ ] Section parts per the spec's layout table: square footprints, two torso parts, one part per leg, chained necks and tails. Gallimimus has 12 parts. Sizes come from the render-scaled posed model. No arm parts and no Dilophosaurus frill part.
- [ ] Placement: rest pose, yaw history, baked pose. Run it on both sides every tick and on load, including for `NoAI` animals.
- [ ] Identity: NeoForge `PartEntity`, and a Fabric part class with the mixin set. Aligned IDs `parentId + i + 1` from a reserved block.
- [ ] Damage: multipliers head ×1.5, neck ×1.25, torso ×1.0, legs ×1.0, tail ×0.5. Parts filter environmental damage and forward the same `DamageSource`. The parent box is not pickable, and parts pass right-clicks to the parent.
- [ ] Area damage: explosions find parts. Sweeps and potions return the parent once, and the range test measures to the nearest part.
- [ ] Part cost: chunk-section part buckets on both loaders, including a NeoForge `Level.getEntities` mixin, plus fewer parts for animals far from players.
- [ ] Bake step: a Gradle task bakes the animation files into tables inside the mod jar. Both sides load them directly. A hash check on join, a golden test against recorded bone positions, and a build failure when an animation is newer than its bake.
- [ ] Neck and tail bend on the server tick within joint limits, with a synced bend-side byte. The GeckoLib 5 updater adds the bend delta, lerped by partial tick.
- [ ] Client leg and foot IK: once per tick, lerped per frame, full within 16 blocks and none past 32. Use the CHAOS branch IK code with its sources. Credit is given as the Mowzie's Mobs terms require.

**Produces:** the part entities per loader, `PoseState`, the bake tables and their loader.

**Acceptance:**
- An arrow at a Brachiosaurus neck hits the neck part and damages the animal ×1.25.
- A creeper next to a T. rex tail damages the T. rex.
- A sweep across a tail reaches the animal.
- The head part follows a calling Gallimimus's raised head.
- A Brachiosaurus tail bends the same way on server and client.
- Parts appear for a `NoAI` animal on the first tick.
- All of these run on both loaders.
- 100 dinosaurs keep the part scan inside the spec's budget.

---

## Phase P4: Beta hardening and release

- [ ] Balance playtest of the full survival loop on each loader by two people who did not build it. Adjust TOML defaults only; record each change and the reason in `docs/config-reference.md`.
- [ ] Verify the "about 50 minutes per dinosaur" and "about 25 fossils on an iron filter" targets; report actuals to the lead.
- [ ] Licence (decision D09): the lead chooses LGPLv3 or CC0 and states the art terms. Then set `LICENSE`, `gradle.properties`, `mods.toml` and `fabric.mod.json`.
- [ ] Translations: `en_us` complete; invite community translations after Beta.
- [ ] Publishing: enable CurseForge and Modrinth in `gradle.properties` with real project ids; a release workflow builds and uploads both jars from a tag.
- [ ] Update `docs/codebase-analysis.md` sections 1 and 4 to the Beta state, or replace that document with a shorter architecture overview and archive the analysis.
- [ ] Tag `v1.0.0-beta.1`.

---

## Release 1 modules

Release 1 content the lead confirmed: plants and wood sets (sections 11.12 and 8.6), the petrified tree (section 8.6), and the species from the Release Plan that are not in Beta. Everything else that is "not in Beta" has no release assigned; see "Unassigned backlog".

### R01: Plants and wood sets

**Section:** 11.12, 8.5, 8.6. **Size:** L. **Depends on:** Beta released.

**In scope:**

- Five plants from the Release Plan and the board: cycad, Serenna veriformans, West Indian lilac, heliconia, horsetail. Growth, placement, worldgen in listed biomes, drops.
- Four wood sets (logs, planks, stairs, slabs, fences, doors, trapdoors, signs) and the petrified tree (no art yet; flag).
- Herbivore diet tags extended to the mod's plants; plant matter recipes from them.
- Paleobotanist trades (decision D10).

**Acceptance:** each plant generates and can be farmed; a Triceratops eats a cycad; both loaders.

### R02: Release 1 species

**Section:** 7.3 (Release Plan), 8.5. **Size:** XL per species. **Depends on:** Beta released, B16 to B19.

**In scope:** Compsognathus, Microceratus, Parasaurolophus, Mosasaurus. Each needs: a stat card from the lead or interim values with a stated basis, a species JSON, a fossil set, a model and animations (Parasaurolophus is the most complete in the asset repository; Compsognathus has a model and six animations; Microceratus and Mosasaurus have no listed model), skins, sounds, diet, prey preferences, AI particulars.

**Mosasaurus** needs aquatic movement, an aquatic enclosure rule (fences do not apply underwater) and a water feeder. Treat it as its own XL module `R02d` and schedule it last.

**Acceptance per species:** the Beta acceptance lists for B09, B16, B17 and B18 pass for the new species.

### R03: Release 1 systems

Systems implied by Release 1 content but not decided:

- A fossil set and digsite entries for each new species (B01 and B21 data only).
- Gene data for any vanilla animal added by Minecraft after 26.2.

### Unassigned backlog

Not in Beta per section 11.13, and not assigned to Release 1 by the lead. The lead assigns each one; this plan does not.

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

---

## Decisions register

Decisions the implementation needs that section 11 does not give. The lead answers; the module's task plan records the answer. Do not start the dependent step until answered, unless a default is listed, in which case ship the default as a config value.

| Id | Module | Question | Default if unanswered |
| --- | --- | --- | --- |
| D01 | B02 | How much more DNA does a special piece give than a bone of the same quality? | Multiplier 2.0 in TOML |
| D02 | B03 | What happens when no species is valid for an amber's depth and biome? | Nearest depth band |
| D03 | B04 | Does sampling an egg with a syringe harm the egg? | No |
| D04 | B04 | Yield range for a syringe on a live dinosaur and for remains? | 25 to 50% live, 50% remains |
| D05 | B11 | Hatching: egg block or egg entity? | Block with block entity |
| D06 | B16 | Multipart hitboxes in Beta, or one scaled box? | Answered 2026-10-09: section parts in Beta (module B27). See [docs/dinosaur-hitboxes-and-movement-design.md](dinosaur-hitboxes-and-movement-design.md). |
| D07 | B22 | Do vehicles use fuel? | No fuel |
| D08 | B23 | Kinds of gun and which models ship | Blocked |
| D09 | P4 | Licence and art terms | Blocked; do not change files |
| D10 | R01 | Does the paleobotanist villager ship in Release 1? | Not shipped |
| D11 | B09 | Approve the render-scale targets (Triceratops 3.0 blocks, Brachiosaurus 12.5) and the section 11.16 missing-stat draft | Use the targets; label as interim |
| D12 | B16 | Real stat cards for five species | Section 11.16 interim values |
| D13 | B00, B19 | Flee or stand for Tyrannosaurus, Velociraptor, Dilophosaurus and Brachiosaurus; `stress_threshold` and `territory_radius` per species | Stand for the three carnivores, flee for Brachiosaurus; one shared stress threshold and radius, set with their basis in the B00 task plan; all as data |
| D14 | B09 | Animation speed that follows body size, and look-around clips on the second controller (review items ANI-6, ANI-2) | Not built. The lead answered "not decided" on 2026-10-06 |

---

## Self-review against the spec

Checked on 2026-10-05 against section 11:

- 11.1 fossils: B01, B02. 11.2 amber and living sources: B03, B04. 11.3 digsites: B21. 11.4 processor: B05. 11.5 sequencer: B06. 11.6 gene editing: B07, B08, B09, B10. 11.7 synthesis to hatch: B11. 11.8 machine framework: B12, B13, B14 (conduit, other mods' cables). 11.9 dinosaurs: B16, B24. 11.10 needs and behaviour: B17, B18, B19. 11.11 power and fences: B14, B15. 11.12 feeders, vehicles, guns: B20, B22, B23. 11.13 not in Beta: unassigned backlog. 11.14 open: D08, D12. 11.15 no-code list: every line maps to a module above. 11.16 stats: B00. 11.17 config and platform: B00, P2.
- Gaps found and fixed in this pass: pylons had no owner (added to B14); voltage signs had no owner (B15); the egg-sampling source had no owner (B04); "feeders fillable from outside" needed both B15 and B20 (noted in both); the fence switch block had no recipe owner (B13).
- Known weakness: module sizes are judgement, not measurement. Re-estimate after the first tier-1 module lands.
