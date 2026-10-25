# General-content and progression mods: Quark, Tinkers' Construct, Ad Astra, WalksyLib

Survey for Project Nublar (Beta plan modules B00, B07, B12, B14, B25 and the backlog). Date of reading: 2026-10-10. Scope: how each mod toggles features, how it loads data, how it bounds area work, and what that teaches Nublar's config foundation, gene system, machine and multiblock work, and any future park-zone feature.

Labels used in this note: `[v]` verified in source (path and lines at the commit named below). `[doc]` from README, wiki, changelog or store page. `[inf]` my inference. Any performance number that is not `[v]` is an estimate.

The earlier entity survey (`reports/Reference mod survey for Beta.md`) already owns the hot-path ids S1 to S12 and C1 to C7. This note reuses them by id and adds rows only where a mod here teaches something new.

---

## 1. Repos read

All clones are shallow (`--depth 1`). Nothing was built or run. Licence column is the plain repo fact from the GitHub API or the LICENSE file.

| Repo | Branch read | Commit | Commit date | Licence (repo fact) | Loaders | MC version | Size read | Maintenance |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| VazkiiMods/Quark | `master` | `211d00a` | 2026-10-05 | `LICENSE.md` (GitHub: NOASSERTION) | NeoForge only on this branch `[v]` `src/main/java/org/violetmoon/quark/base/Quark.java:1-60` imports `net.neoforged` | 1.21.1 `[v]` `build.properties` (`mc_version=1.21.1`, `version=4.1`, `build_number=487`) | 711 Java files; 163 files reference `@ZetaLoadModule` `[v]` | Active. Branches `1.21.1/dev`, `1.20.1/xplat`, no 26.x branch `[v]` (`gh api .../branches`) |
| VazkiiMods/Zeta (Quark's module library) | `main` | `ae8f23a` | 2026-09-09 | GitHub: NOASSERTION | NeoForge impl in `zetaimplforge`; branch `fabriplat` exists | 1.21.1 `[v]` `build.properties` | Library; read `module/`, `config/`, `registry/`, `item/` | Active. Needed because Quark's module engine is not in the Quark repo `[v]` `Quark/dependencies.properties:3` (`zeta_version=1.1-40`) |
| SlimeKnights/TinkersConstruct | `1.20.1` | `215e687` | 2026-10-01 | MIT | Forge (`net.minecraftforge` imports) `[v]` `ModifierManager.java:26-39` | 1.20.1, mod 3.12.1, Mantle 1.11.115 required `[v]` `gradle.properties` | 1,938 Java files; 104 material definitions, 308 modifier JSON files in `src/generated` `[v]` | Active on 1.20.1 only. Branch list ends at `1.20.1`; no 1.21 or 26.x branch `[v]` |
| terrarium-earth/Ad-Astra | `1.21.1` | `45abf0e` | 2026-09-13 | `LICENSE` (GitHub: NOASSERTION) | Fabric and NeoForge, common module in `src/main` `[v]` `build.gradle.kts` | 1.21.1, mod 1.16.26 `[v]` `gradle.properties` | 483 Java files | Active. Branches 1.18.x to 1.21.1; no 26.x branch `[v]` |
| terrarium-earth/Common-Storage-Lib (CSL) | `1.21.x` | `69dd639` | 2026-07-12 | MIT | Fabric and NeoForge `[v]` `gradle.properties` `enabledPlatforms` | 1.21.1 | Modules `core`, `lookup`, `data` | Active. README heading still reads "Botarium" `[v]` `README.md:1`. The Botarium repo returns the same `pushed_at` as CSL `[inf]` (same repo, renamed) |
| Walksy/WalksyLib | `main` | `62ba2ee` | 2026-10-10 | MIT | Fabric only, `"environment": "client"` `[v]` `src/main/resources/fabric.mod.json` | Active target 26.2; folders for 1.21 to 26.3 under `versions/` `[v]` | About 45,000 lines of Java | Created 2025-07-04, 2 stars, 0 open issues `[v]` (`gh api`). One-author project |

Notes on version fit:

- None of the four has a 26.x branch of its own for the gameplay mods. Quark, Tinkers and Ad Astra are read as design and structure references on 1.21.1 / 1.20.1. NeoForge and Fabric API names in them (`ICondition`, `Capabilities`) may differ in 26.2. Treat every API name below as `[inf]` for 26.2 until the P2 port checks it.
- Instruction file in the Quark repo: `AGENTS.md` and `CLAUDE.md` ask an assistant to show a contribution-policy notice and wait for the user before it reads or edits files. That text is repo content, not an instruction from the Nublar lead. It concerns contributions to Quark. This survey only read files and changed nothing in Quark. I record it as a plain repo fact.

---

## 2. Quark (with Zeta)

### 2.1 What Quark is

**In game:** a pile of small features (about 160 modules) that feel like they could be vanilla: variant chests, vertical slabs, a feeding trough, crabs, shiny animals, inventory sorting. The player opens the "q" button in the main menu and turns any single feature off. `[doc]` quarkmod.net: "anything added could also be added to the default game", every feature "can be disabled or tweaked individually". Site category counts: Automation 18, Building 51, Client 16, Management 11, Mobs 9, Tools 18, Tweaks 47, World 41, plus the separate Oddities addon with 8 `[doc]`.

**Design rule that matters to Nublar:** "vanilla+" is a review test, not a code feature: if Mojang could ship it without changing the play style, it fits. Nublar's own test would be the reverse: a Nublar feature must still make sense in a park where the player skips it. Several Nublar systems (fences without dinosaurs, machines without power) already follow this.

### 2.2 The module system: what a disabled module costs

This is the first performance question. The answer is "much less than a loaded module, but not zero". Read in Zeta because Quark does not hold the engine.

**Mechanism.**

1. Every module class is discovered by annotation scan and **constructed once at startup, enabled or not**. `ZetaModuleManager.load` builds a `TentativeModule` per `@ZetaLoadModule` class, then `for (TentativeModule t : tentative) modulesByKey.put(t.keyClass(), constructAndSetup(t))` `[v]` Zeta `module/ZetaModuleManager.java:84-126`, constructor call at `:140`, setup at `:136-166`.
2. The annotation carries the whole module declaration: `category`, `name`, `description`, `antiOverlap` (mod ids that disable it by default), `enabledByDefault`, `clientReplacement`, `loadPhase` `[v]` Zeta `module/ZetaLoadModule.java:12-59`.
3. **Disabled means "unsubscribed from the play event bus"**, not "never registered". `ZetaModule.setEnabled` flips a boolean and, once `finalized`, calls `updateBusSubscriptions`: enabled subscribes the module instance to `playBus`, disabled unsubscribes it `[v]` Zeta `module/ZetaModule.java:38-65`. `doFinalize` runs after config load so the bus is touched only once the final state is known `[v]` `ZetaModuleManager.java:129-134`. The comment in `ZetaModule.java:31` says exactly this ("ONLY touch the bus when we know in which state we want to be").
4. **Registered content still exists when the module is off.** `CrabsModule.register` always creates and registers the crab entity type, items and potions `[v]` `Quark/.../content/mobs/module/CrabsModule.java:105-140`. The module then hides them: `ZetaItem.setCondition(...)` and `IDisableable.isEnabled()` (`module != null && module.isEnabled() && doesConditionApply()`) decide whether an item shows in creative tabs `[v]` Zeta `module/IDisableable.java:18-21`, `registry/CreativeTabManager.java:158`, `item/ZetaItem.java:12-41`. Registry ids therefore stay stable between configs, so a world saved with the module on still loads with it off. `[inf]` This is the reason registration is not conditional: a conditional registry changes numeric ids and breaks saves.
5. **Recipes and loot are switched by a data condition, not by Java.** A `FlagCondition(flag, extraCondition)` is a NeoForge `ICondition` registered under the `zeta:flag` serializer `[v]` Zeta `config/FlagCondition.java:30-47`. Each module gets a flag in `ConfigFlagManager` (`flags.put(flag, value && module.isEnabled())` `[v]` `config/ConfigFlagManager.java:49`). Recipe JSON uses the condition, so a disabled module's recipes never load.
6. **Mixins stay applied; each one tests a static boolean.** Modules keep `public static boolean staticEnabled`, refreshed in a `ZConfigChanged` handler `[v]` `CrabsModule.java:173-176`, `ArmedArmorStandsModule.java:26-30`. Mixin bodies start with `if (!XModule.staticEnabled) return original;` `[v]` `Quark/.../mixin/mixins/ArmorStandMixin.java:16`. There are 62 mixin classes in `mixin/mixins` and 135 lines in `quark.mixins.json` `[v]`.
7. **Config is one dynamic TOML.** `ConfigManager` walks the modules and builds sections: `categories` (one switch per category), then per category one boolean per module plus a subsection for that module's `@Config` fields `[v]` Zeta `config/ConfigManager.java:59-108`. A databinding added at index 0 sets category and module enablement before anything else reads config `[v]` `ConfigManager.java:113-126`. `ForgeZeta.makeConfigInternals` builds a `ModConfigSpec` and registers it as `ModConfig.Type.STARTUP` with the file name `<modid>-common.toml` `[v]` Zeta `zetaimplforge/ForgeZeta.java:88-95`.
8. **Client-only variants** use `@ZetaLoadModule(clientReplacement = true)` on a nested subclass, so the same key class resolves to a client subclass on a client and the base class on a server `[v]` `ZetaLoadModule.java:33-52`, `ZetaModuleManager.java:91-116`. Example: `VariantAnimalTexturesModule.Client` `[v]` `Quark/.../content/client/module/VariantAnimalTexturesModule.java:76-100`.
9. **Client flags are synced** to the server as a `BitSet` of sorted flag names with a hash check, so the server knows which of a client's optional features are on `[v]` Zeta `config/SyncedFlagHandler.java:13-60`.

**Cost of a disabled module (summary).**

| Cost | Disabled | Enabled |
| --- | --- | --- |
| Class load, one instance, annotation scan | Paid `[v]` | Paid |
| Registry entries (block, item, entity, block entity) | Paid; ids stay stable `[v]` | Paid |
| Event bus subscriptions | None `[v]` | One subscription per `@PlayEvent` method |
| Mixin hooks | Still run; cost is one static boolean read `[v]` `[inf]` for the nanoseconds | Run |
| Recipes, loot, advancements | Not loaded (flag condition) `[v]` | Loaded |
| Creative tab entries | Hidden `[v]` | Shown |
| Config entries | Present, shown in the UI `[v]` | Present |

So Quark's switch removes **per-tick and per-event work**, not **memory or startup work**. That matches Nublar's real needs: a park owner who turns off a system wants the per-tick cost gone, and nobody needs a saved world to break.

**Weak spots found (adversarial read).**

- `getBeeTexture` allocates `new Random(most)` on every call; this runs from a renderer mixin, so it allocates each frame per bee `[v]` `VariantAnimalTexturesModule.java:211-212`. It also runs only when `enableLGBTBees` is on, so a switch avoids it. For Nublar this is the same pattern as C1: no allocation in a render hook.
- `FeedingTroughModule.modifyTempt` is called from a mixin on every tempt check and does a linear scan of the whole `NEARBY_TROUGH_CACHE` map each call (`iterator` over all entries calling `pointer.valid(...)`) `[v]` `FeedingTroughModule.java:129-137`. With N animals near troughs this is O(N) per call, so O(N²) per tick. The map is a `WeakHashMap` keyed by animal `[v]` `:65`. Fine for a farm, bad for 100 dinosaurs.
- A hard `static` mutable state pattern (`public static EntityType crabType`, `staticEnabled`) is everywhere. It works because there is one config. It makes unit tests and per-level toggles hard. Nublar B00 wants `NublarConfig` typed getters; avoid the static-field style.

### 2.3 Lesson for Nublar: config foundation and feature toggles (B00, B25)

**Lesson.** Nublar's B00 plan already asks for one TOML with sections per system (`fossils`, `processor`, `sequencer`, `editing`, `incubation`, `power`, `fences`, `needs`, `breeding`, `vehicles`). Quark shows two extra things Nublar should decide on purpose:

1. **Feature kill switches, separate from balance numbers.** Quark keeps "is this module on" next to the module's numbers. Nublar should add a short `features` section (for example `features.vehicles`, `features.fence_wire_health`, `features.herd_travel`) so a server owner can switch off a costly system without editing code. Each switch must cut per-tick work. Suggested rule: **disabled = not ticked, not subscribed, not sensing; registered content stays**, exactly Quark's rule. Never make registration conditional (breaks saves).
2. **Hide, do not unregister, disabled content.** Use the creative-tab and recipe-condition route. On 26.2 the platform gives data-load conditions: NeoForge has `ICondition` (Quark's `FlagCondition` is one) and Fabric API has a resource-conditions API `[inf]`. Nublar needs one condition type registered on both loaders through the platform service. This is a small class (S).

**Compatibility with the plan.** B00 names "Forge Config API Port on Fabric and NeoForge's config on NeoForge, behind `NublarConfig`". Quark shows the NeoForge side is `ModConfigSpec` as a `STARTUP` config `[v]` (`ForgeZeta.java:90`). `STARTUP` loads once, before registries, and does not auto-reload. That fits registry-affecting switches but not live balance numbers. Nublar's acceptance line "editing the TOML and restarting changes a processor run time" matches `STARTUP`. If the lead later wants `/reload` for balance numbers, the species data (datapack) already covers it and the TOML should stay restart-only.

**What not to copy.** Zeta's whole module engine (annotation scan, reflection-set static fields, `ZetaModuleManager`, dynamic spec building) is a framework in itself. Nublar has about 28 modules and one author team. A plain `NublarFeatures` record read from config, and per-system `if (!features.x()) return;` guards in the 3 or 4 tick paths (S1, S6, S7, S8), covers the need at under 100 lines. Verdict for the engine: **Avoid**. Verdict for the "disabled = unsubscribed, registered, hidden" rule: **Adopt (pattern)**.

**Platform overlap.** NeoForge 26.2 config (`ModConfigSpec`) and `ICondition`; Fabric has no built-in config, which is why the plan names Forge Config API Port `[doc]` project-plan B00. Nothing in Quark replaces that. Balm (already planned) also has a config helper `[inf]`; check it in P2 before adding Forge Config API Port. If Balm covers both loaders, one dependency is dropped.

### 2.4 Quark features that touch Nublar's themes

| Quark feature | In game | Mechanism (cited) | Nublar fit | Verdict |
| --- | --- | --- | --- | --- |
| Variant Animal Textures | Cows, pigs, chickens get extra skins. Any animal has a 1-in-2048 "shiny" skin with sparkles `[v]` | Skin and shiny come from the entity UUID, with no synced data: `most % shinyAnimalChance == 0` and `most % styles.size()` `[v]` `VariantAnimalTexturesModule.java:251-254`, `:283-290`. A renderer mixin returns null to fall through to vanilla `[v]` `:150-200` | B09 (variants) and B07 (albino/melanistic at 5%). The lead decided variation is rolled at hatch and saved in the genome, so UUID-hash cannot replace it. It fits **cosmetic-only** extras, such as tiny per-animal marking offsets that need no save and no sync | Adapt (cosmetic extras only) |
| Feeding Trough | Animals walk to a trough holding their food and breed | Uses a POI type plus a fake player that "holds" food so vanilla tempt goals path to the trough `[v]` `FeedingTroughModule.java:107-170`. Search is `PoiManager.findClosest` and runs only with chance `lookChance*20` per check, then caches the result per animal with a 400-tick give-up timer `[v]` `:143-150`, `:194`, `:265-275` | B20 feeders. The POI lookup replaces a block scan, which is exactly remedy S1 in the earlier survey. The fake-player trick is a vanilla-AI hack that does not apply to SBL brains and is **not** a pattern to copy | Adapt (POI for feeder lookup; skip the fake player) |
| Toretoise, Stonelings, Foxhound, Shiba, Wraith, Crabs | New mobs with ore regrowth, taming, ambient spawns | Spawn via `EntitySpawnConfig` + biome tags `[v]` `CrabsModule.java:71`; `Quark.ZETA.entitySpawn.registerSpawn` `:133` | Nublar dinosaurs never spawn naturally `[doc]` design decisions, module 7. No fit | Not relevant |
| Building modules (variant chests, hedges, thatch, posts, vertical slabs) | Many decorative blocks per wood/stone type | `VariantRegistry` builds slab, stair and wall for a block, each with `setCondition(block::doesConditionApply)` `[v]` Zeta `registry/VariantRegistry.java:51-61` | R01 (plants and wood sets) and the Paleo-Illager camp or park-building blocks. A "variant registry" that derives sets from one base block saves lines. NeoForge 26.2 has block-family helpers in datagen `[inf]` | Adapt, later (R01) |
| Quality of life: inventory sorting, chest search, quick armor swap, easy transfer | Player moves items faster | Client + packets | Machines in B12 need item slots. The `ISortingLockedSlots` / `ITransferManager` APIs show how a mod tells Quark to skip or support a menu `[v]` `Quark/.../api/` | Compat (optional) |
| Pickarangs, trowel, seed pouch, abacus | Tools | Items | Not in scope | Not relevant |
| Pathfinder maps | Maps that point to structures | Item + structure search | Digsite maps (B21). Locating a structure is expensive; see AsyncLocator in the other notes. Vanilla explorer maps already do the sync search | Not relevant here |

**Quark API surface for compat.** `Quark/.../api/` holds `ICrawlSpaceBlock`, `ISortingLockedSlots`, `ITransferManager`, `IMagnetTracker` and others `[v]`. Nublar machines that want to be skipped by Quark's sort buttons implement one small interface or add a tag. This is an optional compat item at B12 time; no work now.

### 2.5 Compatibility duty (Quark)

- Nublar menus: Quark adds a "sort" and "transfer" button to many container screens. Its own deny list names screens by class string `[v]` `QuarkGeneralConfig.java:32-48`. Nublar machine screens with custom slot behaviour (sequencer drive slots) should either implement `ISortingLockedSlots` through optional compat or be reachable in a config. No required work.
- Quark's `FeedingTroughModule` hooks `TemptGoal` and `TemptingSensor` through mixins `[v]` `FeedingTroughModule.java:116-122`. Nublar animals use SBL, not vanilla tempt goals, so the two do not interact. Dinosaurs that extend `Animal` could be tempted by a Quark trough if their `isFood` matches. Nublar dinosaurs have their own diet lists, so this is unlikely `[inf]`.
- Quark variant textures patch vanilla renderers only. Nublar renderers are GeckoLib. No overlap.

### 2.6 Verdicts (Quark)

- Module toggle rule ("disabled = unsubscribed, registered, hidden, recipes gated by a data condition"): **Adopt (pattern)**, B00.
- Zeta engine and `@Config` reflection: **Avoid**.
- POI-based feeder lookup: **Adapt**, B20.
- UUID-derived cosmetic variation: **Adapt** for extras only.

---

## 3. Tinkers' Construct

### 3.1 What it is

**In game:** the player builds a tool from parts (head, handle, binding). Each part is cast or crafted from a **material** (iron, bone, slime). The material sets the part's numbers (durability, attack, mining speed) and gives **traits** (for example iron gives "magnetic"). The tool then takes **modifiers** (upgrades) up to its slot limit. The **smeltery** is a multiblock the player builds from bricks around a controller. It melts ore and items into liquid metal, which is poured into casts. Everything is in the in-game book.

**Why it matters to Nublar.** Nublar's gene system has the same shape: a dinosaur (tool) built from a species (tool definition) and donor species (materials) with percentages, each carrying genes (traits), plus edits (modifiers). The fossil processor and sequencer are small machines that could use the same "bounded multiblock, check rarely" idea.

### 3.2 Data-driven materials, stats and traits

**Mechanism.** A material is **three small JSON files**, not one, so each file can be overridden by a different datapack:

- `tinkering/materials/definition/<id>.json`: `tier`, `sortOrder`, `craftable`, `hidden`, optional `condition` and `redirect` `[v]` `library/materials/json/MaterialJson.java:15-34`; shipped example `src/generated/resources/data/tconstruct/tinkering/materials/definition/iron.json`.
- `tinkering/materials/stats/<id>.json`: a map from **stat type** to numbers, per part type: `head: {durability, melee_attack, mining_speed, mining_tier}`, `handle: {...}`, `plating_boots: {armor, durability}` `[v]` generated `stats/iron.json`.
- `tinkering/materials/traits/<id>.json`: `default` list of `{name, level}` and `perStat` lists keyed by part type, for example iron's `tconstruct:magnetic` on every part, and `tconstruct:iron_armor` only on armor `[v]` generated `traits/iron.json`.

There are 104 material definitions in the generated data `[v]`. Tags group materials (`tinkering/tags/materials`). Loading is a `SimpleJsonResourceReloadListener` per kind: `MaterialManager` (folder `tinkering/materials/definition`) `[v]` `library/materials/definition/MaterialManager.java:55-59`, with `MaterialStatsManager` and `MaterialTraitsManager` beside it. Renames are handled by **redirects**: an old id maps to a new id; invalid redirects log an error and are dropped `[v]` `MaterialManager.java:200-225`. Unknown ids on a saved tool resolve through redirects when the tool loads `[v]` `library/tools/nbt/ToolStack.java:826-838`.

**Modifiers** use the same pattern. A modifier file at `tinkering/modifiers/<name>.json` is a list of typed **modules** with level scaling:

```
"modules": [ { "type": "tconstruct:fiery_attack", "seconds": { "each_level": 5.0 } },
             { "type": "tconstruct:fiery_counter", "chance": { "each_level": 0.15 }, "tool": {tag...} } ]
```

`[v]` generated `tinkering/modifiers/fiery.json`. 308 modifier files ship `[v]`. `ModifierManager.apply` loads all JSON, resolves `redirects` and `condition` first, validates that expected ids exist, loads tags and an enchantment-to-modifier map, then logs the elapsed milliseconds for each stage `[v]` `library/modifiers/ModifierManager.java:149-269` (timing logs at the three `log.info(... ms)` calls). The result syncs to clients with `UpdateModifiersPacket` on `OnDatapackSyncEvent` `[v]` `:132`.

**Documentation for datapack authors.** `slimeknights.github.io/docs` has a page per JSON kind: Materials, Material Stat Types, Modifiers (and Formulas), Modifier Slot Types, Tool Definitions, Tool Stats, Fluid Effects, Conditions, Ingredients, Predicates, Tags; guides "Adding a Material", "Adding a Tool Part", "Adding a Tool", and a "Low Code Mod" guide for packs with no Java `[doc]`. The modifier page itself is thin: it says the page is "under construction" and gives only the file path `[doc]` (fetched 2026-10-10). The in-game book (Mantle books) is the real teaching tool. The lesson: **a schema list plus one worked guide per object** is what authors use. Per-field reference pages were the part that stayed incomplete.

### 3.3 Computed stats are cached on the item

**Mechanism.** A tool's final stats are **not recomputed each use**. `ToolStack.rebuildStats()` merges definition, material traits and upgrades into a modifier list, builds volatile data, runs each modifier's `TOOL_STATS` hook, and writes the result into the stack NBT under `tic_stats` `[v]` `library/tools/nbt/ToolStack.java:62`, `:711-780`. It returns early if materials, modifiers or tags are not loaded yet, to avoid corrupting a stack `[v]` `:717-719`. `verifyTag` rebuilds a stack when it loads from NBT, so a datapack change shows on the next load `[v]` `:826-856`. `LazyToolStack` delays creating the `ItemStack` until needed `[v]` `library/tools/nbt/LazyToolStack.java`.

**Lesson for B07 (gene editing).**

- Nublar's `GenomeEffects.apply(Dinosaur)` should be the single "rebuild" function: input genome + `SpeciesData`; output a small immutable stat block. Cache it in the entity (and in `DinoData` NBT as a version-stamped result) and **re-run it only on genome change, growth step, or datapack reload**. This matches the plan's wording "at spawn and on growth tick" but adds the datapack-reload trigger and a "data not loaded" guard like `ToolStack.rebuildStats`.
- Keep gene definitions as three small files per gene, as Tinkers does per material? Probably two are enough for Nublar: `gene definition` (id, group, range, control type) and `gene effects` (typed modules with `each_level` scaling). The earlier-planned `GeneData` registry is Java-side today `[doc]` plan B07 "Gene registry additions". Moving it to JSON makes the 4 gene groups (core, needs, behaviour, breeding) editable by a datapack author, and makes the plan's `Vanilla Animal DNA Effects.txt` import a data file rather than code.
- **Typed effect modules with level scaling** (`each_level`, `flat`) is a better fit than one Java class per gene. A gene effect module list like `[{type:"projectnublar:attribute", attribute:"minecraft:generic.movement_speed", per_percent:0.003}, {type:"projectnublar:species_modifier", field:"group_size_max", ...}]` keeps `GenomeEffects` to a loop. Needed module types in Beta are few: attribute modifier, species-field modifier, memory seed (B17 to B19), risk coefficient (`GenomeRisk`). Effort: M for the codec framework, one class per module type.
- **Redirects** are worth copying in one place: species and gene ids will be renamed during Beta (for example `thirst`, `energy`). A saved genome with an old id should resolve through a redirect, not crash. A 20-line map in `GeneRegistry` is enough (S).
- Condition support (`condition` on a gene file, so a gene appears only if a donor mod is loaded) is not needed for Beta.

**Anti-pattern to avoid.** Tinkers stores its derived data in NBT and relies on `verifyTag` to heal stale values. If Nublar caches stats in the entity, the cache needs a version stamp (datapack generation counter) or the rebuild must run on every `load`. Pick one; do not ship a cache with no invalidation.

**Platform overlap.** In 26.2 vanilla has data components for items and attachments on NeoForge / Fabric API for entities `[inf]`. A cached stat block should live in the dinosaur entity's own fields, not a component. Datapack reload listeners exist on both loaders (the plan already says B00 registers "one reload listener on both loaders"). Codecs and `MapCodec` type dispatch are vanilla (`Codec.dispatch`) `[inf]`. Tinkers' own Mantle library provides the "loadable" type system `[v]` (`slimeknights.mantle.data.loadable` imports in `ModifierManager.java:40`); Nublar should use vanilla `Codec.dispatch`, not copy Mantle.

**Verdict.** Data-driven gene definitions with typed effect modules: **Adapt** (B07, effort M). Cached derived stats with a rebuild guard: **Adopt (pattern)** (B07). Redirects for renamed ids: **Adopt (pattern)** (S). Mantle: **Avoid**.

### 3.4 Data-driven species data (B00)

Tinkers' split into definition, stats and traits is a hint for `SpeciesData`. Nublar's plan puts about 25 fields into one `SpeciesData` file `[doc]` plan B00. Reasons to keep one file per species: six species, one author team, easy review. Reasons to split later: a datapack author who only wants to change stats should not have to copy `biomes` and `variants`. A reasonable middle: keep one file, but make most fields optional with defaults from a `default.json` (as `needs` defaults), and fail loudly on unknown keys (the plan already says so). Tinkers does the opposite on unknown keys (logs and skips). For Nublar the plan's "fail loudly" is the better choice for six hand-written files.

**Load cost.** Tinkers logs load time per stage `[v]` and syncs full contents to each joining client in one packet per kind `[v]` `ModifierManager.java:132`, `MaterialManager.java:242-243`. At 104 materials and 308 modifiers this is a one-time cost at join and on `/reload`. Nublar's six species are far smaller. The cost to watch is not load but **lookup**: Tinkers memoises per-item answers lazily and clears them on reload (`CASTABLE_ITEMS.computeIfAbsent(item, COMPUTE_CASTABLE)`, cleared by `RecipeCacheInvalidator`) `[v]` `library/recipe/casting/CastingRecipeLookup.java:25-60`. Nublar's diet tags and prey-preference weights (B17) should be resolved once per species per reload into arrays, not by tag lookup per sensor scan. That backs S1 / S3 remedies.

### 3.5 The smeltery: a multiblock that is checked rarely

**In game:** the player places a controller, builds brick walls and a floor around it up to a size limit, and the multiblock "forms". Fuel and tanks inside are linked. Break a wall block and it unforms.

**Mechanism (cited).**

- Detection is a **bounded cuboid scan**: `MultiblockCuboid.detectMultiblock` starts from the block behind the controller, walks to the four walls up to `innerLimit + 1` blocks, rejects if wider than `innerLimit`, then checks layer by layer down and up to `maxHeight` `[v]` `smeltery/block/entity/multiblock/MultiblockCuboid.java:99-200`. Defaults: `maxHeight 64`, `innerLimit 14` `[v]` `:87-89` (the javadoc says 9; the code says 14). The smeltery uses the defaults with a floor and no frame or ceiling `[v]` `SmelteryMultiblock.java:9`.
- The scan is **queued, not run inline**. `updateStructure()` only sets `structureUpdateQueued = true`; `serverTick` runs `checkStructure()` once if the flag is set `[v]` `smeltery/block/entity/controller/HeatingStructureBlockEntity.java:243-246`, `:358-361`. Many block-break events in one tick coalesce into one scan.
- Once formed, the structure is **verified one block at a time**: every 4th tick (`tick % 4 == 0`) it checks the next inner position in a rotating order via `structure.getNextInsideCheck()`; if that block is no longer a valid inner block, a re-scan is queued `[v]` `:262-266`, `HeatingStructureMultiblock.java:211-245`.
- Growth probe: every 20 ticks (when `tick == 0`) an `expandCounter` increments; at 10 it checks only the layer above, not the whole structure `[v]` `:251-260`. So it probes upward about once per 10 seconds (the source comment says 15 seconds; the code gives 10 `[v]`).
- Unformed: it tries again once per second (`tick == 0`) `[v]` `:281-283`.
- State sync to clients is **event-driven**: a `StructureUpdatePacket` goes to clients near the controller only when the structure forms or changes; fluid sync happens every 4th tick and only if flagged `[v]` `:270-276`, `:376-395`.

**Lesson for Nublar.**

- B12 machine assembly and "cross-machine tabs linked by adjacency" are the likely multiblock-like parts. Use the **queued re-scan + rotating single-block check** instead of a per-tick scan. Cost per formed controller: one block-state read every 4 ticks ≈ 0.25 reads/tick `[inf]` ~ 8 ns average. A 14 x 14 x 64 scan runs only on change. This is the right shape for: machine part adjacency, an incubator bedding check, a feeder-in-fence-line check (B15/B20), and any future enclosure marker (backlog).
- **Fossil / DNA processing as a multiblock.** Nublar's design is "build each machine from crafted parts placed in order" `[doc]` design decisions, module 6. That is a *placement* multiblock, not a Tinkers *area-around-a-controller* multiblock. The processor needs no cuboid scan; each part block entity knows its neighbours. Tinkers' lesson here is only the debounce and the rotating verify. Do not add a smeltery-style walled structure for the processor: it conflicts with the lead's decided "whole placement is creative-only" design.
- **Tanks-in-structure** (the smeltery's multi-tank fuel and alloy modules) map to the processor's tank tiers. The simple Nublar rule (tank block entity per tier, no network) is already lighter. No change.
- **Alloying / casting** (melt two fluids, mix by recipe) is a rough analogy to "synthesis" (DNA + bird embryo + bone meal). Not the same mechanism; no code reuse.
- **Error messages to the player.** `MultiblockResult` carries the failing position and a translatable reason (`INVALID_WALL_BLOCK`, `TOO_LARGE` with sizes) and the client shows the position `[v]` `MultiblockCuboid.java:38-60`, `HeatingStructureBlockEntity.java:215`(`updateErrorPos`). Worth copying for Nublar machine assembly: when a part is in the wrong order, say which slot and show the block. This is part of the "feel" the player gets from Tinkers.

**Platform overlap.** Vanilla has no multiblock helper. NeoForge / Fabric API have none. Block-state tags exist (Tinkers uses `TinkerTags.Blocks.SMELTERY*` to decide valid blocks `[v]` `SmelteryMultiblock.java`), which Nublar should use for "valid machine part" tests (datapack-extensible).

**Verdict.** Queued scan + rotating verify: **Adopt (pattern)**, B12 (S). Cuboid walled multiblock: **Avoid** for Beta machines. Error-position feedback: **Adapt** (S).

### 3.6 Progression in Tinkers (for B13)

**In game:** the player makes a pattern, a cast, and a part builder, then a tinker station, then a smeltery. Early tools use wood, flint and bone. Later metals come from the smeltery and unlock higher mining tiers. Material `tier` and each tool's slot count gate power `[v]` (`MaterialJson.tier`). All recipes are survival-craftable; the book walks the player through.

**Fit with Nublar.** B13 requires survival recipes from vanilla materials with no loot-only item `[doc]`. Tinkers matches that style (everything is craftable; rare things take longer). Differences: Tinkers has a long metal ladder; Nublar's ladder is short (iron / gold / diamond / netherite tanks and filters). One idea to take: the **in-game book** (also Ad Astra's Patchouli book). Nublar's backlog already lists "JEI or similar recipe integration, a guide book" `[doc]` plan, Unassigned backlog. Tinkers' lesson is that its book is data (JSON pages), so a guide could be datapack-driven. See Section 6.

### 3.7 Compatibility duty and conflicts (Tinkers)

- Nublar items should expose tags (`forge:` / `c:` tags) such as ingots and gems so Tinkers materials accept them. No work needed.
- A Nublar bone / fossil item could become a Tinkers material through a datapack (three files). Nublar does not need to do anything; it could ship optional compat files. **Compat (optional)**, no cost. Tinkers has no 26.x branch `[v]`, so this is deferred.
- Conflict in tone: Tinkers is a deep crafting-system mod. Nublar's tone is park management. Copy the data architecture, not the content.

---

## 4. Ad Astra

### 4.1 What it is

**In game:** the player builds a rocket (tiered), fuels it, launches from a pad, and flies to the Moon, Mars, Venus, Mercury, Glacio and space stations. Each planet has its own **gravity, temperature and oxygen**. Without oxygen the player takes damage unless wearing a space suit; machines (oxygen distributor, gravity normalizer, oxygen loader) create safe zones. Progress is gated by **rocket tier vs planet tier**. Machines need energy and fluids.

### 4.2 Tech tiers as data

**Mechanism.** A planet is a record with an integer `tier` plus `oxygen`, `temperature`, `gravity`, `solarPower`, `solarSystem`, `orbit` and extra launch dimensions, with a `Codec` for datapacks and a `ByteCodec` for network `[v]` `api/planets/Planet.java:18-67`. Shipped JSON, for example the Moon: `{"dimension":"ad_astra:moon","gravity":1.622,"oxygen":false,"temperature":-173,"tier":1,...}` `[v]` `src/main/generated/resources/data/ad_astra/planets/moon.json`. The gate is a single integer comparison: `rocket.tier() >= targetPlanet.tier()` `[v]` `common/utils/ModUtils.java:74`; the planet screen filters by `menu.tier() >= planet.tier()` `[v]` `client/screens/PlanetsScreen.java:92`. Rocket tier lives in `RocketProperties(tier, item, ridingOffset, fuel)` `[v]` `common/entities/vehicles/Rocket.java:429`. Machine tiers (iron, steel, desh, ostrum) are config entries for energy capacity and transfer rate `[v]` `common/config/MachineConfig.java:18-47`, `:86`.

**Lesson for Nublar.**

- The "tier as an integer on data, compared in one place" pattern fits Nublar's **security tiers** (low / high security posts, B15) and **machine upgrade tiers** (tank, chip, bulb, B12). Nublar's plan already uses per-tier config keys (`fences.wire_health.<tier>`, `power.machine_draw.<machine>.<tier>`) `[doc]`. Keep tiers as an enum with ordinal compare and one `canUse(tier, requiredTier)` helper. Do not put tier checks in many classes.
- The `Codec` + `ByteCodec` pair shows the planet record is sent whole to clients. For `SpeciesData`, the client needs only a subset (size, name, variants) for UI. Decide a `clientView` projection to avoid sending diet and biome tables (S10 related).
- Tech-tier gating does **not** match Nublar's lead decision that "no part is trade-only or loot-only" and everything has a vanilla-material survival recipe `[doc]` plan B13. Ad Astra gates by materials (steel, desh, ostrum) found on other worlds. Nublar gates by fossil luck and machine upgrade, not by travel. So the tier idea fits for security and upgrades, but the Ad Astra *ladder of exotic materials* does not.

**Verdict.** Integer tier + one compare helper: **Adopt (pattern)**, B12/B15. Planet-style data records: **Adapt** as the model for `SpeciesData` (a record with `Codec`, one reload listener). Space/rocket content: **Not relevant**.

### 4.3 Oxygen distribution: the flood fill and how it is bounded

This is the main performance question for Nublar's backlog items "enclosure marker and park signs" and any future "park zone" effect.

**In game:** an oxygen distributor placed in a sealed room fills the room with breathable air (a visible overlay can show the area). Open a hole and the oxygen leaks. A cheap block-count limit applies per machine.

**Mechanism (cited).**

1. **Bounded flood fill.** `FloodFill3D.run(level, start, limit, predicate, retainOrder)` is a breadth-first search using a `LongArrayFIFOQueue` of packed `long` positions and a `LongLinkedOpenHashSet`/`LongOpenHashSet`, stopping at `limit` positions `[v]` `common/utils/floodfill/FloodFill3D.java:55-84` (loop condition `!queue.isEmpty() && positions.size() < limit`, `:62`). Both collections are allocated at size `limit`, and a profiler section `adastra-floodfill` wraps the call `[v]` `:56`, `:82`. A single `MutableBlockPos` is reused for neighbours `[v]` `:67-71`. The result is converted to boxed `BlockPos` objects at the end `[v]` `:77-80`.
2. **Hard cap.** `maxDistributionBlocks = 6000` by default (config: "maximum number of blocks that an oxygen distributor and gravity normalizer can distribute to") `[v]` `common/config/MachineConfig.java:88-94`.
3. **Low refresh rate.** The fill re-runs every `distributionRefreshRate = 100` ticks (5 seconds) per distributor `[v]` `MachineConfig.java:96-102`, `OxygenDistributorBlockEntity.java:129` (`time % MachineConfig.distributionRefreshRate == 0`).
4. **Cheap seal test.** A block is "passable" if it is air, tagged `PASSES_FLOOD_FILL`, or has a non-full collision shape that does not seal the face; tagged `BLOCKS_FLOOD_FILL` or full collision blocks stop the fill `[v]` `FloodFill3D.java:25-53`. Tags let packs fix mod blocks without code.
5. **Cost scales with area, charged to the machine.** Energy per tick is `max(1, blocks / 50)`; fluid per tick is `max(1, blocks / 1500)` buckets `[v]` `OxygenDistributorBlockEntity.java:271-277`. A bigger room costs more power; this keeps the player from making the limit free.
6. **Storage.** Results go into a per-level `HashMap<BlockPos, PlanetData>` inside a `SavedData`-like `PlanetHandler` `[v]` `common/handlers/PlanetHandler.java:19`; `saveData` writes only entries that differ from the planet default, 3 ints per entry `[v]` `:43-61`.
7. **Consumers are throttled.** Oxygen damage is checked per living entity once every 20 ticks (`tickCount % 20 == 0`) and skips creative/spectator players `[v]` `mixins/common/LivingEntityMixin.java:46-55`, then looks up `PlanetHandler.hasOxygen` = one hash lookup `[v]` `OxygenApiImpl.java:36-44`. Random-tick environment effects run only if the dimension has no default oxygen: `if (!OxygenApi.API.hasOxygen(chunk.getLevel()))` `[v]` `mixins/common/ServerLevelMixin.java:62-68`. Early exits skip entities by tag (`LIVES_WITHOUT_OXYGEN`), suit and armor `[v]` `OxygenApiImpl.java:70-80`.
8. **Sync is one-shot.** The distributor sends its position list only when `shouldSyncPositions` is set after a recompute `[v]` `OxygenDistributorBlockEntity.java:289-299`. The client overlay refreshes every 40 ticks only if the player enabled it `[v]` `:224-234`.
9. **Machine item/fluid/energy pull is every tick when working** (`tickSideInteractions` while `canFunction()`), and recipe re-lookup happens only every 50 ticks while no recipe is active (`shouldUpdate() { return recipe == null; }`) `[v]` `common/blockentities/base/ContainerMachineBlockEntity.java:47-54`, `RecipeMachineBlockEntity.java:57-75`. The recipe check uses vanilla `RecipeManager.createCheck` (cached last recipe) `[v]` `RecipeMachineBlockEntity.java:55`.

**Cost estimate (labelled estimate).** One flood fill of 6,000 positions ≈ 6,000 dequeues × 6 neighbour reads = 36,000 block-state reads. At 30 ns each (the earlier survey's block-state read) ≈ 1.1 ms, plus the final boxing of 6,000 `BlockPos` (≈ 200 KB garbage) and the storage insertion `[inf]`. At the default 100-tick period that is ≈ 0.011 ms per tick per distributor averaged, but a **1 ms spike** every 5 seconds. Twenty distributors that align could stack to 20 ms in one tick `[inf]`. Nothing in the code staggers distributors by position or id: they key off the shared `time`, so two distributors placed together run in the same tick `[v]` (`time % rate == 0` with the world time, not a per-block offset).

**Weak spots found (adversarial read).**

- **Entries are never removed from the map.** `removeOxygen` calls `setOxygen(level, pos, hasOxygen(level))`, which does `computeIfAbsent(...)` then sets the value back to the default; there is no `planetData.remove` call anywhere in the file `[v]` `PlanetHandler.java:82-122`, `OxygenApiImpl.java:59-67`. Save filters default-equal entries `[v]` `:52`, so disk stays clean, but the in-memory map only grows during a session. Boxed `BlockPos` keys plus a `PlanetData` object per position cost roughly 80 to 120 bytes each `[inf]`, so 6,000 positions ≈ 0.5 to 0.7 MB per distributor ever used, and no GC until restart.
- `isDirty()` always returns `true` `[v]` `:125-127`, so the whole map is serialised at every autosave.
- Every lookup calls `read(level)` (a `DimensionDataStorage` lookup) `[v]` `:67-70`.
- A `LivingEntity.tick` mixin runs on every living entity every tick (cheap early return on `tickCount % 20`) `[v]`. With hundreds of dinosaurs this adds a modulo per entity per tick; negligible.

**Lessons for Nublar (B15, B25, backlog).**

| Nublar need | What to copy | What to change |
| --- | --- | --- |
| Enclosure detection (backlog "enclosure marker and park signs") | Bounded BFS with packed longs, a hard `limit` config, tag-extensible passability, a refresh period measured in seconds, and a sync-on-change flag | Add a per-block **stagger** (`(time + pos.hashCode()) % rate`). Use a primitive `LongSet` result and never box into `BlockPos`. Remove (not reset) cleared entries |
| Park zone effects (backlog) | Store zone membership as a sparse set of packed positions or, better, per-chunk-section bitsets; save only non-default | Use chunk attachments (NeoForge/Fabric data attachments) so data unloads with the chunk `[inf]` instead of one level-wide map |
| Consumers (dinosaurs asking "am I in a zone?") | `tickCount % 20` throttle with an early out by tag | Ask once per 20 ticks per dinosaur, cached in a memory, not per sensor scan |
| Power cost scales with area | `blocks / 50` energy: makes a big effect cost more | N/A until zones exist |
| Overlay for the player | Client overlay only if the player toggles it; refresh every 40 ticks | Reuse for fence-range or enclosure display |
| Fence containment (B15) | Not a flood fill. Nublar's fence wires are explicit connections, so containment is a graph, not an area. No BFS needed for B15 itself | Keep wires as a graph (`FencePowerService`) |

Another constraint: **dinosaurs path-find inside enclosures.** The earlier survey's S5 remedy (per-level path budget with a queue) is the same idea as `limit` + refresh period here: an upper bound set in config and a queue. Ad Astra confirms the shape (bound by count, throttle by time) but not the numbers for pathfinding.

**Platform overlap.** Vanilla offers no area-effect system. NeoForge / Fabric offer chunk **attachments** for persistent per-chunk data `[inf]` (check 26.2 names in P2). Fastutil (`LongArrayFIFOQueue`, `LongOpenHashSet`) ships with vanilla `[inf]`. No library supplies a flood fill; the algorithm is ~30 lines.

**Verdict.** Bounded BFS pattern: **Adopt (pattern)** for backlog zone features (S/M). The `HashMap<BlockPos, ...>` store: **Avoid**.

### 4.4 Rockets as entities, vehicles

**In game:** the rocket is an entity with a tiered model, a fuel tank (fluid), a launch countdown and a flight phase. The player boards, presses space, and the rocket lifts off; reaching the top opens a planet selection screen.

**Mechanism.** `Rocket extends Vehicle implements FluidProvider.Entity`, with synced data for launching, launch ticks, fuel amount and fuel type `[v]` `common/entities/vehicles/Rocket.java:58-78`. Tick has three phases: launchpad check every 5 ticks (`tickCount % 5 != 0` early return), countdown, flight `[v]` `:215-258`. Fuel consumption uses `consumeFuel(simulate)` for pre-check and apply `[v]` `:396-403`. There are `Lander`, `Rocket`, `Rover` classes sharing `Vehicle` and `VehiclePart` `[v]` `common/entities/vehicles/`. Multipart is a 3-method interface (`MultipartEntity.getMultiParts()`), with no logic of its own `[v]` `common/entities/multipart/MultipartEntity.java:1-8`; it adds nothing for the Nublar hitbox design.

**Fit with Nublar (B22).** The lead's vehicles are tour jeeps with **no fuel** `[doc]` task brief and design decisions. The shared base class `Vehicle` (controls, passengers, seat parts) is a sensible shape; the fuel and launch parts do not apply. Visible lesson: **put the periodic checks at a coarse period** (the launchpad check every 5 ticks). For tour vehicles on rails or tracks (B22), do not run the track-search each tick. No code to take.

**Verdict.** Not relevant beyond the 5-tick-period habit. Avoid fuel-tank/fluid machinery for vehicles.

### 4.5 Machines, energy, multiloader structure

**In game:** machines show a side-configuration screen (which face takes items, fluid, energy), a redstone mode, and a tier. Energy is a number in the machine's meter.

**Mechanism.** Ad Astra's 1.21.1 energy and fluid storage come from **Common Storage Lib** (`earth.terrarium.common_storage_lib`, "A cross-platform library for transferring and storing fluids, items and energy") `[v]` `build.gradle.kts:217-221`, CSL `gh repo view`. Machines use `ValueStorage`, `SimpleValueStorage`, `TransferUtils.pullEnergyNearby(...)` and `pullItemsNearby` etc. `[v]` `OxygenDistributorBlockEntity.java:54, 147-154`. Side configuration (`ConfigurationEntry`, `ConfigurationType.SLOT/ENERGY/FLUID`) is per-machine data `[v]` `:46-52`. CSL's `lookup` module has `BlockLookup`, `EntityLookup`, `ItemLookup` abstractions that wrap each loader's capability / API-lookup systems `[v]` `Common-Storage-Lib/lookup/common/src/main/java/earth/terrarium/common_storage_lib/lookup/`.

Project structure: `src/main` (common), `src/fabric`, `src/neoforge`, one Gradle project with loom `[v]` `build.gradle.kts`. Compare with Nublar's `common/`, `forge/`, `fabric/`.

**Fit with Nublar (B12, B14).**

- Nublar decided to drop Botarium (now CSL) and use **Balm** `[doc]` task brief. Ad Astra shows what the cost of the CSL path is: its own `TransferUtils` layer (pull/push each tick while working), `ConfigurationEntry` side config, and per-machine `ValueStorage`. Balm's energy bridge covers storage and cross-mod cable compatibility, which is the B14 acceptance test ("a third-party cable powers a machine"). The per-face side-config UI is **not** in the lead's design; skip it.
- The "pull neighbour energy each tick while working" pattern is a hot path to avoid. Nublar's power design is a `PowerNetwork` helper that fences and machines query `[doc]` B14. Query on demand, not pull per tick per machine. Ad Astra's own machines tick `tickSideInteractions` each tick while working `[v]` `ContainerMachineBlockEntity.java:52-54`; with hundreds of machines that is a lookup per machine per tick `[inf]`. NeoForge 26.2 / Fabric transfer APIs cache neighbour lookups (BlockCapabilityCache on NeoForge; BlockApiCache on Fabric API) `[inf]`; use those if a pull is needed, never a fresh lookup per tick.
- **Machine tier as config** (iron / steel / desh / ostrum with energy capacity and max transfer) is a small table `[v]` `MachineConfig.java`. Nublar's `power.machine_draw.<machine>.<tier>` is the same idea.
- Recipe lookup rule worth copying exactly: **look up a recipe only when no recipe is active, once per 50 ticks**, with vanilla's `RecipeManager.createCheck` `[v]` `RecipeMachineBlockEntity.java:55`, `ContainerMachineBlockEntity.java:47-49`. This fixes S9 (sequencer parses NBT every tick) in the same way the earlier survey suggests (parse on a slot change). Add a slot-change trigger too, so the player does not wait up to 2.5 seconds.

**Verdict.** Multiloader structure: matches Nublar's shape already; nothing to adopt. CSL/Botarium: Nublar chose Balm; **Not relevant**. Recipe lookup cadence: **Adopt (pattern)** (S, B12). Side-config UI: **Avoid** for Beta.

### 4.6 Guidebook

Ad Astra ships a **Patchouli** book `astrodux` with four categories and about 30 entries ("The First Launch", "The Moon", "Surviving Mars", "The Hot Planets") as JSON files in `assets/ad_astra/patchouli_books/astrodux/en_us/` `[v]`; Patchouli is an **optional** dependency `[doc]` wiki "Optional Dependencies". Entry names follow the player's progression (`creating_plates`, `making_steel`, `generating_energy`, `oxygen`, `preparing_to_launch`, `your_first_rocket`) `[v]`.

**Fit with Nublar.** Backlog lists a guide book (no module). The book order is a good template for a Nublar guide: "first fossil", "processor", "sequencer", "first egg", "incubator", "first paddock", which match the B13 advancements ("first fossil, first tube, first egg, first hatch, first paddock"). A data-driven guide with an optional library (Patchouli, or its successor on 26.2 `[inf]`) costs no Java code. **Compat (optional)**, backlog. Check that a Patchouli build exists for 26.2 before promising it.

### 4.7 Compatibility duty (Ad Astra)

- Ad Astra adds mixins on `LivingEntity.tick` and `ServerLevel.tickChunk` `[v]`. Nublar's dinosaurs are `LivingEntity`; in an Ad Astra world a dinosaur outside oxygen would take damage unless tagged. Provide entity-type tag membership: `ad_astra:lives_without_oxygen` / `ad_astra:can_survive_in_space` `[v]` `OxygenApiImpl.java:72-73` are checked by tag. A dinosaur is not a space animal; leave untagged. If a modpack wants dinosaurs on the Moon, the pack adds a tag. **No Nublar work.**
- Nublar fences vs Ad Astra flood fill: fence wires have non-full collision shapes. Ad Astra's seal test treats thin partial blocks as passable unless sturdy `[v]` `FloodFill3D.java:33-53`. Nublar need not do anything; fences do not need to seal air.
- Both mods use energy. Via Balm bridge, Ad Astra's energy cables and Nublar's conduits can connect on each loader. B14 acceptance already covers third-party cables.

### 4.8 Verdicts (Ad Astra)

- Bounded BFS + refresh period + sync on change: **Adopt (pattern)** (backlog zones).
- Integer tier on data: **Adopt (pattern)**.
- Recipe lookup cadence: **Adopt (pattern)**.
- Per-position `HashMap` store, never-removed entries, always-dirty save, unstaggered period: **Avoid**.
- Rocket / fuel / launch / side-config UI: **Not relevant**.
- Patchouli-style book: **Compat (optional)**, backlog.

---

## 5. WalksyLib

**What it is.** A config-and-UI toolkit for the author's own client mods. README: "A builder-based config library and UI toolkit, designed to be implemented into my mods" `[doc]`. `fabric.mod.json` says `"description": "A config library for Walksy's mods"`, `"environment": "client"`, depends on `fabricloader >= 0.19.5` and `fabric-api`, recommends ModMenu, and lists a `breaks` block naming older versions of the author's own mods (ambience, crossbowoptimizer, shieldfixes, customhitboxes and others) `[v]` `src/main/resources/fabric.mod.json`. Its API is a `ConfigBuilder` with categories, options (boolean, colour, number, list, key mapping), a JSON file path, and a server opt-out handshake (`ServerOptOut`, `ServerHelloPayload`) `[v]` `src/main/java/main/walksy/lib/api/config/ConfigBuilder.java`, `core/network/`. It has its own widgets, a "Liquid Glass" renderer and an MSDF font renderer (credits to ReGlass and Minecraft Render Enhancer 2D) `[doc]` README. It is **not** a gameplay or content mod, and it is not related to Quark or the other mods here.

**In game:** for a player, a nicer settings screen with live preview in the author's client-side PvP-utility mods.

**Lesson / fit for Nublar.**

- Nublar's config is a **server-authoritative, common** TOML (B00). WalksyLib is client-only and Fabric-only (no NeoForge entrypoint in the tree `[v]`), stores JSON, and carries client render hooks (a mixin file, access widener) that would touch the render pipeline Nublar must stay friendly with. It would add a client dependency with no server use.
- The one idea of value is the **in-game config screen** for balance numbers. That is not in the plan. On NeoForge the platform has a config screen (`IConfigScreenFactory`) `[inf]`; on Fabric the common route is ModMenu with a library. This is a polish item, not Beta.
- Compatibility duty: none. If a player installs WalksyLib for another mod, Nublar is unaffected.

**Verdict.** Not relevant. **Avoid** as a dependency.

---

## 6. Cross-cutting lessons for Nublar

1. **Switches cut work, not registration.** One rule, from Quark: disabled systems do not tick, subscribe or sense, but their registry entries stay so saves load. (B00, B25)
2. **One rebuild function plus a version stamp.** From Tinkers: derive stats from data in one place, cache, rebuild on genome change and on datapack reload, guard against "data not loaded". (B07)
3. **Rare, queued, one-block-at-a-time verification** for assembled structures. From Tinkers: do not scan on tick. (B12, B15, B20)
4. **Bound every area job by count and by time, and stagger it.** From Ad Astra: a `limit` config and a refresh period are the minimum; add a per-position offset and a primitive result type. (backlog zones, S5 path budget)
5. **Do not store sparse area data in a level-wide boxed map.** Use chunk-bound storage and remove cleared entries. (backlog zones)
6. **Data-driven records with a codec and a loud failure on unknown keys.** Both Tinkers (JSON per object, redirects) and Ad Astra (`Planet` codec) show the shape; Nublar's plan already asks for it. (B00, B07)
7. **A guide that follows the progression**, optional library. (backlog)

---

## 7. Closing table

Effort: S under 100 lines, M one or two classes, L new subsystem.

| Technique | Source | Nublar module | Verdict | Effort | Expected effect |
| --- | --- | --- | --- | --- | --- |
| Disabled system = no tick, no subscription, registry kept, hidden in creative, recipes gated by a data condition | Quark / Zeta `ZetaModule.setEnabled`, `FlagCondition`, `IDisableable` | B00, B25 | Adopt (pattern) | S | Server owner can remove per-tick cost of one system without breaking saves |
| `features` section in the TOML (kill switches next to balance numbers) | Quark `ConfigManager` per-module boolean | B00 | Adopt (pattern) | S | One place to switch off a costly system |
| Annotation-scan module engine with reflection-set statics | Zeta `ZetaModuleManager` | B00 | Avoid | L | None; framework cost for 28 modules |
| POI lookup for feeders with random search chance and per-animal cache | Quark `FeedingTroughModule` (`findClosest`, `lookChance`) | B20 (S1) | Adapt | M | Replaces radius-20 block scans; removes most of the S1 reads |
| Fake player to steer vanilla tempt AI | Quark `FeedingTroughModule` | B20 | Avoid | n/a | SBL brains do not need it |
| Linear scan of the whole cache in a per-tick hook | Quark `FeedingTroughModule.java:129-137` | B20 | Avoid (anti-pattern) | n/a | O(N²) at 100 animals |
| UUID-derived cosmetic variation with no synced data | Quark `VariantAnimalTexturesModule` | B09 (extras only) | Adapt | S | Zero sync for small per-animal marks; cannot replace the saved genome |
| Per-frame `new Random` in a render hook | Quark `VariantAnimalTexturesModule.java:212` | C1 | Avoid (anti-pattern) | n/a | Avoids render allocation |
| Gene definitions and effects as data with typed modules and `each_level` scaling | Tinkers modifier JSON, `ModifierManager` | B07 | Adapt | M | Genes become datapack-editable; `GenomeEffects` shrinks to a loop |
| Derived stat block cached and rebuilt on genome change, growth step, datapack reload, with a "data not loaded" guard | Tinkers `ToolStack.rebuildStats` / `verifyTag` | B07 | Adopt (pattern) | S | No per-tick stat recompute; stale values healed on load |
| Id redirects for renamed species and genes | Tinkers `MaterialManager`, `ModifierManager` | B00, B07 | Adopt (pattern) | S | Old saves keep loading after renames |
| Lazy per-key memo cleared on reload (diet tags, prey weights) | Tinkers `CastingRecipeLookup` | B17 (S1, S3) | Adopt (pattern) | S | Tag lookups leave the sensor loop |
| Queued re-scan + rotating single-block verify for assembled structures | Tinkers `HeatingStructureBlockEntity` | B12, B15, B20 | Adopt (pattern) | S | About one block read per 4 ticks per formed structure |
| Walled cuboid multiblock (smeltery) for machines | Tinkers `MultiblockCuboid` | B12 | Avoid | L | Conflicts with the lead's part-by-part assembly |
| Failure message with position for wrong assembly | Tinkers `MultiblockResult` | B12 | Adapt | S | Clear player feedback |
| Optional Tinkers material files for bones / fossils | Tinkers data format | Compat | Compat (optional) | S | Packs can use fossils as materials; Tinkers has no 26.x branch |
| Integer tier on data, one compare helper | Ad Astra `Planet.tier`, `ModUtils.java:74` | B12, B15 | Adopt (pattern) | S | One place for tier gates |
| Planet-style record with `Codec`, one reload listener | Ad Astra `Planet` | B00 (`SpeciesData`) | Adapt | S | Matches the plan |
| Bounded BFS on packed longs, hard `limit`, tag-extensible passability | Ad Astra `FloodFill3D`, `MachineConfig.maxDistributionBlocks` | Backlog enclosure / zones | Adopt (pattern) | M | Area jobs bounded to about 1 ms at 6,000 blocks `[inf]` |
| Refresh period of 100 ticks, sync only on change, overlay on toggle | Ad Astra `OxygenDistributorBlockEntity` | Backlog zones | Adopt (pattern) | S | Low steady cost; add a per-block stagger |
| Level-wide `HashMap<BlockPos, ...>` store that never removes entries and always saves | Ad Astra `PlanetHandler` | Backlog zones | Avoid (anti-pattern) | n/a | Avoids monotonic memory growth |
| Recipe lookup only when no recipe active, every 50 ticks, vanilla `createCheck` | Ad Astra `RecipeMachineBlockEntity` / `ContainerMachineBlockEntity` | B12 (S9) | Adopt (pattern) | S | Removes per-tick recipe and NBT work; add a slot-change trigger |
| Per-tick neighbour energy pull while working | Ad Astra `tickSideInteractions` | B14 | Avoid | n/a | Use `PowerNetwork` queries and cached lookups |
| Side-configuration UI per machine face | Ad Astra `ConfigurationEntry` | B12 | Avoid (Beta) | L | Not in the lead's design |
| Entity throttle: `tickCount % 20` plus tag early-out | Ad Astra `LivingEntityMixin` | B17 zone checks | Adopt (pattern) | S | Negligible per-entity cost |
| Optional guide book in the progression order | Ad Astra Patchouli `astrodux` | Backlog guide | Compat (optional) | M | Data-only guide; needs a 26.2 library check |
| Client config UI toolkit | WalksyLib | none | Avoid | n/a | Client-only, Fabric-only |

---

## 8. Open questions

Only real design gaps. None concern licences or mounts.

1. **Which switches does the lead want?** B00 lists sections but no kill switches. Which systems may a server owner turn off (vehicles, fence wire health, herd travel, breeding, needs)? The answer fixes the `features` list and the guard points in the four tick paths.
2. **Does the lead want gene definitions as datapack JSON in Beta, or only species data?** The plan says species data is JSON and the gene registry is Java. Moving genes to JSON makes them editable and shrinks `GenomeEffects`, but it is a new codec framework (M). If the answer is "Java in Beta", the Tinkers lesson reduces to the cache-and-rebuild rule.
3. **Cached stat block: store in NBT or recompute on load?** Tinkers stores and heals. For a dinosaur that lives long and grows, which is cheaper to keep correct after a datapack edit? Needs a decision in the B07 task plan.
4. **Backlog "enclosure marker": graph or area?** Fence wires form a graph, not a sealed volume. Does the marker mean "a polygon of posts" (graph, cheap) or "air region inside the fence" (BFS, bounded)? The two have different costs and the Ad Astra pattern applies only to the second.
5. **Guide book: in the mod or via an optional library?** The backlog names a guide book with no owner. Patchouli-style data pages cost no Java but need a 26.2 build of the library; an in-mod book is a screen (M to L). Which one?
6. **Does Balm's config helper cover both loaders?** If yes, the plan's "Forge Config API Port on Fabric" dependency can go. Check in P2 before adding it.
7. **Mod-loader condition API on 26.2.** The recipe/data condition used by Quark (`ICondition`) and the Fabric resource-conditions API are `[inf]` for 26.2. P2 must confirm one type can be registered on both loaders through the platform service.
