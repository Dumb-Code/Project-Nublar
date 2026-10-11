# Project Nublar: codebase analysis

Date: 2026-10-04. This document covers all five branches of `Dumb-Code/Project-Nublar`. Section 11 has the date 2026-10-05.

**Method and limits.** This analysis read the code. It did not compile the code or run the code. Six read-only passes covered these areas:

- the build
- the machine chain
- the dinosaurs
- each unmerged branch
- the legacy history

This document uses three markers:

- `[v]`: The analysis checked this item a second time against the source.
- `[inf]`: This item is an inference about runtime behaviour. The code implies it but does not prove it.
- No marker: The analysis read this item one time at the cited location.

Line numbers refer to branch `1.20.1-ai` (tip 4244f62) unless the text names a different branch.

Path shorthand:

| Short | Full path |
| --- | --- |
| `J/` | `common/src/main/java/net/dumbcode/projectnublar/` |
| `R/` | `common/src/main/resources/` |
| `G/` | `common/src/generated/resources/` |
| `F/` | `forge/src/main/java/net/dumbcode/projectnublar/` |
| `FB/` | `fabric/src/main/java/net/dumbcode/projectnublar/` |

---

## 1. Summary

Project Nublar is a Jurassic Park mod for Minecraft 1.20.1. It is a rewrite of the earlier DumbCode mod (`Dumb-Code/ProjectNublar`, versions 1.12 and 1.16). The player does these steps:

1. Extract DNA.
2. Sequence the DNA and edit it.
3. Print an egg.
4. Incubate the egg.
5. Keep the dinosaurs behind electric fences.

State today:

- The machine chain from a DNA sample to a hatched dinosaur works on Forge.
- Six species have models, textures, animations and data. Only Tyrannosaurus is almost complete.
- The brain uses the AI for hunger, thirst, rest, growth and solo hunting.
- Code exists for the social need, mating, turf wars and taming. The brain cannot reach that code.
- A player cannot find fossils in survival mode. A comment disables the fossil DNA yield.
- Most edited genes do not reach the animal. The code never saves colours.
- Machines never drain power. Only one crafting recipe exists.
- The Fabric build has no client.
- The work is on three unmerged branches. All three start from one commit.

Section 11 gives the design that the project lead set for Beta. Section 11.15 lists the Beta systems that have no code.

The project plan that executes section 11 is a separate file, [project-plan.md](project-plan.md). On the shared page it is the tab "Beta and Release 1 project plan".

---

## 2. Branches

```
1.20.1 (GitHub default, tip 67bf1b5, 2025-02-06)
   |
1.20.1-moddevgradle (tip 55e1968, 2026-02-20)   <- fork point
   |-- fossils-1.20.1   (aa9d047, 2026-03-02, 5 commits)
   |-- 1.20.1-ai        (4244f62, 2026-06-01, 2 commits)   <- checked out
   `-- rft/adversing    (97023cf, 2026-06-15, 6 commits)
```

- History is linear up to 55e1968. Every commit on `1.20.1` is an ancestor of `1.20.1-moddevgradle` `[v]`.
- The three newest branches are separate. No one of them contains the commits of another `[v]`.
- The repository has 95 commits from 8 committers.
- No pull requests are open. 17 issues are open.

Commits for each committer:

| Committer | Commits |
| --- | --- |
| ShadowTek | 29 |
| Tfarcenim | 21 |
| Nyfaria | 16, plus 1 as Anthony Jones |
| Kashyap Gummaraju | 16, all merges |
| Adversing | 6 |
| HerrChaos | 4 |
| Brandon Davis | 2 |

### 2.1 History in phases

| Period | Who | What |
| --- | --- | --- |
| Apr 2024 to Jan 2025 | Nyfaria | Processor, sequencer, gene editing, egg printer, incubator, fences, generator |
| Sep 2024 | HerrChaos | An inverse-kinematics leg system (`entity/ik/**`). Five commits reverted it. No branch tip contains it. Commits ca07b8f, 76384ca and b2340d1 contain it |
| Feb to May 2025 | Tfarcenim | Build migration to the MultiLoader layout and ModDevGradle. First dinosaur AI |
| Jun to Aug 2025 | ShadowTek | Hunger, thirst, sleep, social (part 1), hunting, turf wars, growth stages |
| Feb 2026 | ShadowTek | "Fossil refactor part 1" (c836cc6 removed worldgen and `FossilsConfig`). Five more species. Rendering changes |
| Feb to Mar 2026 | ShadowTek | `fossils-1.20.1`: fossil rewrite |
| Jun 2026 | ShadowTek | `1.20.1-ai`: carnivore feeder |
| Jun 2026 | Adversing | `rft/adversing`: package refactor, `nublar-gui`, fence rework |

The migration removed no classes. Later commits removed these items:

- c836cc6 removed fossil worldgen.
- 582a025 put the Fabric client code in a comment.
- ca50b06 removed the interaction manager classes for T. rex, `PackEntity` and `IPackBehavior`.

### 2.2 `1.20.1-ai`

This branch changes 43 files against the fork point. It adds these items:

- `DinosaurFeederBlock` and `DinosaurFeederBlockEntity`
- `CarnivoreFeederModel` and `CarnivoreFeederRenderer`
- `NearestFeederSensor`, `EatFromMeatFeeder` and `WalkToNearestFeeder`
- a `feeder_meat` tag
- assets

The branch also commits `hs_err_pid20864.log` and increases the Gradle wrapper version. The feeder does not work at this time (section 5.3).

### 2.3 `rft/adversing`

This branch changes 208 files. About 90% of the change is mechanical.

| Commit | Content |
| --- | --- |
| e5ebaa9 | Moves only. See the move list below this table |
| cb6ae23 | Moves 16 widgets from `J/client/widget` to a new Gradle module, `nublar-gui` (`net.dumbcode.projectnublar.gui.widget`) |
| 865489d | A cleanup that keeps behaviour the same. Registry ids, NBT keys and packet ids stay the same. It adds 53 `TODO(BUG)` comments about existing defects |
| 59d9518, 2a4bf12, 97023cf | The fence rework (below). 59d9518 does not compile alone because it uses `FencePowerService`. 2a4bf12 adds that class |

Moves in e5ebaa9:

- `init/*` moves to `registry/*`.
- `entity/ai/**` moves to `entity/dinosaur/ai/**`. In that move, `behaviour` becomes `behavior`, `tasks` becomes `task` and `sensors` becomes `sensor`.
- `api/*` splits into `api/dinosaur`, `api/fossil`, `api/gene` and `api/util`.
- `block/api/*` splits into `fence`, `geometry`, `multiblock` and `sync`.

All real behaviour changes are in the fences:

| Area | Before | After |
| --- | --- | --- |
| Power | Each pole segment had a 350 FE buffer. Both sides drained it at 10 FE for each tick. The code only set `POWERED` to true | Each pole column has one store (`block/api/fence/FencePowerService`) on the server only. `POWERED` syncs in both directions. Complete wire runs share energy |
| Post placement and removal | A state change of any type ran placement and removal again | The code ignores state changes of the same block. A new link restores broken wires |
| Connections | Duplicates were possible | The branch adds `equals`, `hashCode` and a duplicate check. Wire segments with no anchor break in a cascade and drop spools |
| Collision | Each `entityInside` call built the boxes again | A cache holds the boxes. Broken connections have no boxes |
| Rendering | The renderer always drew the full wire | Broken wires disappear. Loose halves render |
| Dinosaur fence break | `destroyBlock` on the block in the middle of the run | The code marks specific segments as broken |

Risks on this branch:

- Two new Javadoc blocks contain a raw `->` (`ProjectNublar.java:32`, `api/fossil/FossilCollection.java:17`). Java 17 doclint may fail the build `[inf]`.
- Commit 59d9518 swaps the previous and next arguments at both connection construction sites. Old fences may load as a mirror image `[inf]`.
- Connection changes send a notice through `triggerModelUpdate()`. That method does nothing outside the Forge mixin, so Fabric sync may regress `[inf]`.
- The loaders include `nublar-gui` as source sets, the same mechanism as `common`.
- `nublar-gui` is not a standalone library. Its mod id constant is `projectnublar` and its textures stay in common.

Static checks on the tip found 0 project imports with no target, 0 package mismatches and no duplicate classes. The branch removed these unused items:

- `ExampleMixin`
- the forge `FluidWidget`
- the empty `BlockElectricFencePoleMixin`
- the empty `DamageSourceInit`
- the `com.example` service files

### 2.4 `fossils-1.20.1`

This branch changes 2743 files. Most of the changes remove generated JSON files.

The old design at the fork point had 910 fossil blocks and 26 amber blocks. That is 13 stones x 5 qualities x 7 pieces x 2 species. A player could not get any of them in play.

The new design:

- The `FossilBase` enum has stone, sandstone and deepslate (`J/api/fossil/FossilBase.java:9-11`).
- Each base and piece has one `FossilBlock`. The block has an `EnumProperty<Quality>` with 4 values (`J/block/FossilBlock.java:40`).
- T. rex has 9 pieces. The result is 27 blocks, 108 block models and 108 `FossilBlockItem` items.
- `api/Dinosaur` is a record. `init/DinosaurInit` is a static list that holds only `TYRANNOSAURUS_REX`, with hard-coded behaviour numbers `[v]`.
- A comment disables the `DeferredRegister` version of that list.
- `FossilsConfig` is a `ForgeConfigSpec` (TOML). It holds periods, quality weights, DNA yields, a `biped` set and one fossil entry.
- The quality weights are 40, 20, 15 and 4. The DNA yields are 5, 7.5, 10 and 30 `[v]`.
- `worldgen/FossilFeature` places ore veins. The placement modifiers are `fossil_filter` and `fossil_height`.
- Datagen writes three cretaceous features and the Forge biome modifiers.
- `FossilBlock.getDrops` has an override that does not use loot tables.
- A Forge block colour for each base supplies the tint.

Regressions on this branch:

- The branch registers only `TYRANNOSAURUS_REX_ENTITY` `[v]`. The other five entity types, renderers, layers and eggs are absent or are in comments.
- The branch renames `entity/dinosaur/Dinosaur` to `AbstractDinosaur`.
- The branch removes `BehaviourDataReloadListener`. A newly spawned T. rex likely has behaviour data of zero `[inf]`.
- A comment disables the diet lookup. As a result, `getDinoDiet()` is null and `isFood` returns false.
- `IncubatedEggItem` reads its entity type from `Dinosaurs.DINOSAUR_STORAGE`. No code fills that map. Hatching likely throws an exception `[inf]`.

Bugs in the new worldgen:

- The quality roll has no `break` (`J/worldgen/FossilFeature.java:208-215`) `[v]`. After the total decreases to zero, each later entry also matches. The last map entry always wins.
- The rarity test is `nextDouble() < 1.0 / chance` (`FossilRarityFilter.java:34`). The default `chance` is 0.4 or 0.5 (`FossilsConfig.java:86-98`) `[v]`. The right side is 2 or more, so the test always passes.
- The feature uses `new Random()` and not the worldgen random source.
- The feature prints with `System.err.println` for each block (`FossilFeature.java:168`).
- The default `biped` set lists `spine`. No registered piece has that name (`FossilsConfig.java:70`).
- A comment disables amber registration.
- The drop code ignores silk touch and fortune (TODO comments at `FossilBlock.java:63-64`).
- Fabric has no config registration, biome modifier, colours or models.

### 2.5 Merge outlook

The estimates use overlapping diff ranges and `git merge-tree`. The analysis did not complete a merge.

| Pair | Shared files | Conflict hunks | Nature |
| --- | --- | --- | --- |
| ai x rft | 13 | about 11 | Import and package moves. `BlockInit` and `CreativeTabInit` show as `modify/delete` conflicts unless the merge uses `-X find-renames=30%` |
| ai x fossils | 17 | 4 files with real conflicts | The feeder classes use `Dinosaur`. `EatFromMeatFeeder` calls a diet, and the fossils branch makes that diet null |
| rft x fossils | 75 | about 168 | 7 `modify/delete` conflicts. An add/add conflict on `api/fossil/Quality.java`. 35 rft files refer to the renamed class |

The two branches have opposite designs. The rft branch calls the old fossil block ids "frozen registry contracts" and improves the datapack listeners. The fossils branch removes both.

### 2.6 Recommended integration order

1. Fast-forward the base branch to `1.20.1-ai`.
2. Merge `rft/adversing`.
   - Move the three feeder classes to `entity/dinosaur/ai/{sensor,task}`.
   - Before the merge, build both loaders with Gradle and include Javadoc.
   - Test the fences in the game.
3. Select the config format: datapack JSON or TOML.
   - The build already declares `forgeconfigapiport` and no code uses it.
   - As a result, `ForgeConfigSpec` can serve both loaders.
4. Port the fossil design. Do not merge the fossils branch.
   - Copy the `FossilBlock` with the quality state and copy `FossilBase`.
   - Copy the feature, the placement modifiers, the overlay textures and the datagen.
   - Keep all six species registered.
   - Keep diet loading and behaviour loading.
   - Fix the quality roll and the rarity test.
   - Generate `G/` again. Do not merge it.
5. Change the default GitHub branch. It is `1.20.1` now.
6. Restore the intended licence.
7. Replace the README.

A different order is also possible: fossils, then ai, then rft. Use it if the fossils branch is complete first. The cost of rft x fossils is the same in both orders.

After this integration, the mod ports to a newer Minecraft version (section 11.17).

---

## 3. Build and architecture

### 3.1 Layout

The project uses the MultiLoader Template layout. The `common` module holds almost all code, resources and generated data. The `forge` and `fabric` modules compile the common sources again (`buildSrc/src/main/groovy/multiloader-loader.gradle:24-32`).

| Item | Version | Where |
| --- | --- | --- |
| Gradle | 8.14.x | `gradle/wrapper/gradle-wrapper.properties` |
| ModDevGradle legacyforge | 2.0.77 | `build.gradle:3` (common and forge) |
| fabric-loom | 1.9-SNAPSHOT | `build.gradle:2` |
| Java / Minecraft / Parchment | 17 / 1.20.1 / 2023.09.03 | `gradle.properties` |
| Forge | 47.2.30 | `gradle.properties` |
| Fabric loader / API | 0.16.9 / 0.92.1+1.20.1 | `gradle.properties` |
| GeckoLib | 4.7.3 | Models and animation |
| SmartBrainLib | 1.15 | Dinosaur AI |
| Architectury API | 9.2.14 | Registration |
| Botarium | 2.3.4 (hard-coded) | Energy and fluids. 8 common files import it `[v]` |
| Common Networking | 1.0.3 in common, 1.0.5 on loaders | The versions do not match `[v]` |
| Forge Config API Port | 8.0.0 | The build declares it. No code uses it |
| toml4j 0.7.2, MixinExtras 0.4.1 | | The build declares them. No code uses them |
| NyfsGuiLib (`ngl`) | 0.0.19 | Comments disable it in all three modules. `J/client/widget` holds a copy of its widgets |

The legacy branch `1.20.1` used these tools:

- Gradle 7.5.1, ForgeGradle 5.1 and Loom 1.0
- RegistrationUtils
- Forge 47.1.3, GeckoLib 4.4.4 and SBL 1.14

### 3.2 Connections between the parts

- **Entry.** `J/ProjectNublar.java:17-34` builds the custom `projectnublar:gene` registry. Then it calls `loadClass()` on each `*Init` class.
- **Type token.** Line 19 passes `new GeneInit[0]` as the type token `[v]`. This is possibly wrong `[inf]`.
- **Registration.** The `J/init/*Init.java` classes use the Architectury `DeferredRegister`. On the rft branch the package is `registry/`.
- **Platform layer.** `J/platform/Services.java` loads `IPlatformHelper` through `ServiceLoader`. The helper covers the sequencer menu type, the incubator menu type and `openMenu`.
- **SmartBrainLib sensors.** The `SBLLoader` service registers the sensors (`J/init/SensorTypesInit.java:20`).
- **Networking.** `J/network/NetworkInit.java:9-13` registers 3 packets `[v]`. All three go from client to server. No packet goes from server to client.
- **Packet names.** The packets are `UpdateEditInfoPacket`, `UpdateIncubatorSlotPacket` and `UpdateIncubatorPacket`.
- **Config.** The mod has no loader config. Datapack JSON is in `R/data/projectnublar/config/**` and `R/data/projectnublar/diet/**`.
- **Reload listeners.** Four reload listeners in `J/data` read that JSON. Only `F/event/CommonForgeEvents.java:25-30` registers them `[v]`.
- **Forge API in common classes.** Mixins inject it. Each loader has its own `GeoMultiBlockItemMixin`. The `BlockEntityElectricFence*Mixin` classes are Forge only and supply model data.
- **Datagen.** Datagen is Forge only. `F/datagen` has 11 providers and the output goes to `G/`.
- **Datagen gaps.** `ModWorldGenProvider.java:34-49` has empty bootstrap methods. A comment disables the body of `ModSoundProvider`.

### 3.3 Package guide (`1.20.1-ai`)

| Package in `J/` | Contents |
| --- | --- |
| `api` | `DNAData`, `DinoData`, `GeneData`, `Genes`, `DiskStorage`, `FossilPiece(s)`, `FossilCollection`, `Quality`, `DinoBehaviourData`, `DinoDietData` |
| `api/fossil`, `api/loot/functions` | Fossil config records. `FossilItemFunction` and `AmberItemFunction` |
| `block`, `block/entity` | Machines, fences, generator, feeder, fossil blocks and amber blocks |
| `block/api` | Fence connections and geometry, multiblock helpers, block entities that sync |
| `client/screen`, `client/widget` | Machine screens. 26 widgets |
| `client/renderer`, `client/model` | Renderers for dinosaurs, fences and the feeder. `DinoLayer` |
| `data` | `GeneDataReloadListener`, `BehaviourDataReloadListener`, `DietReloadListener`, `FossilConfigReloadListener` |
| `entity/dinosaur` | `Dinosaur`, `CarnivoreDinosaur`, `HerbivoreDinosaur`, six species, `PartEntity`, `DinosaurPart` |
| `entity/ai` | Behaviours, sensors, tasks, `FenceAwareNavigation` |
| `init` | 16 registry classes |
| `item`, `menutypes`, `recipe`, `network/c2s`, `util` | As the names say |

### 3.4 Loader parity

Fabric does not have these items:

- A client entrypoint. `FB/ProjectNublarClientFabric.java:17-33` is in a block comment. `fabric.mod.json:18-22` declares only `main` `[v]`.
- The reload listeners, the death message hook and the handler that restores a wire with a spool.
- The `fossil` and `test_tube` model loaders. `FB/client/FossilModel.java` is a stub that no code registers.
- The fence mixins that supply model data.
- Dependency declarations for GeckoLib, SmartBrainLib, Architectury and Botarium in `fabric.mod.json`.

Five common files import `net.minecraftforge.api.distmarker` `[v]`:

- `AbstractSliderButton`
- `EditBox`
- `BlockEntityElectricFence`
- `BlockEntityElectricFenceBase`
- `ElectricFenceBlock`

### 3.5 Repository hygiene

- **Licence.** `LICENSE` is LGPLv3 on `1.20.1`. Each later branch has the CC0 text of the template `[v]`. `gradle.properties` says `license=CC0-1.0`. GitHub reports LGPL-3.0.
- **README.** `README.md` is the MultiLoader Template text with no changes.
- **Tracked files that are not source** `[v]`:
  - `hs_err_pid553788.log`, a crash of the IDE runtime
  - `hs_err_pid20864.log`, a Gradle daemon that had no memory left
  - `forge/dataRun/**`, 5 files from a different computer
  - `G/.cache/**`, 11 files
  - `.ai/mcp/mcp.json`, 0 bytes
- The mod did not cause either crash log.
- **`shapes/*.txt`.** These files are Blockbench shape exports. `ModShapes.java` already contains the same data. No code refers to the folder.
- **Template remnants:**
  - `J/mixin/ExampleMixin.java`
  - two `META-INF/services/com.example.examplemod...` files `[v]`
  - the credits at `mods.toml:13`
  - placeholder URLs in `fabric.mod.json`
  - a logo file that does not exist
  - `pack_format` 8
- **Metadata.** `mod_author=Nyfaria`, `description=Dinosaurs`, version 0.0.23, `minecraft_version_range=[1.20.1, 1.22)`.
- **Dependencies.** `mods.toml` declares only SmartBrainLib, with `side="SERVER"`.
- **Tests.** The repository has no tests, no `.github` folder and no CI.

---

## 4. Gameplay loop (`1.20.1-ai`, Forge)

| Step | Status | Detail |
| --- | --- | --- |
| Fossil and amber worldgen | Stub | `J/init/FeatureInit.java` is empty. `F/datagen/ModWorldGenProvider.java:34-49` is empty |
| Fossil blocks and drops | Broken | See 4.1 |
| DNA from mobs | Working | `J/item/SyringeItem.java:36-38` gives a random 0 to 100% |
| Processor | Partial | See 4.2 |
| Sequencer: sequence | Working | `J/block/entity/SequencerBlockEntity.java:154-189` |
| Sequencer: edit | Partial | See 4.3 |
| Colour edit | Preview only | The NBT data does not contain `layerColors` `[v]` |
| Synthesis | Partial | It uses 500 water and 8 each of bone meal, sugar and plant matter. It takes 10 minutes (`:210-225`) |
| Egg printer | Partial | `J/block/entity/EggPrinterBlockEntity.java:93-117` |
| Unincubated egg | Working | `J/recipe/UnincubatedEggRecipe.java:87-100`. This is the only recipe `[v]` |
| Incubator | Partial | `J/block/entity/IncubatorBlockEntity.java:300-333` |
| Hatch | Working | `J/item/IncubatedEggItem.java:28-50` |

### 4.1 Fossils on this branch

- `J/api/FossilPieces.java:13-23` has 11 piece types: `ribcage`, `neck`, `foot`, `arm`, `leg`, `wing`, `tail`, `spine`, `leaf`, `rex_skull`, `triceratops_skull`.
- `J/api/Quality.java` has the qualities none, fragmented, poor, common and pristine.
- In `J/api/FossilCollection.java:20-21,47-50`, `fullFossilMap` and `fullAmberMap` are static `[v]`. All species share them, so the second species replaces the first.
- `create` returns `COLLECTIONS.put(...)` `[v]`. That value is the previous entry, which is null on the first call.
- Generated blockstates and models exist only for Triceratops. Loot tables cover both species.
- In `J/api/loot/functions/FossilItemFunction.java:53-60`, a comment disables the fortune weights. Then the code calls `.get()` on an empty list. This likely throws an exception `[inf]`.
- A comment disables the DNA yield at `J/api/DNAData.java:49`. Fossils contain 0% DNA.
- No code calls the four getters in `J/data/FossilConfigReloadListener.java:181-195`.
- `periods.json` has snake_case keys. The record fields are camelCase.
- Set names such as `ribs` and `leg_biped` do not match the piece registry.
- `AmberFossils` and `TraceFossils` are empty records.
- No `piece.projectnublar.*` translation exists. The `amber` item has no model and no texture.

### 4.2 Processor

Source: `J/block/entity/ProcessorBlockEntity.java:84-116`.

- The inputs are a fossil or amber, water, a filter and a blank tube.
- The tank holds 2000 mB. Each run uses 250 mB.
- A run takes 4 minutes by default (`:249-251`).
- The output tube contains the DNA percent x the filter efficiency (`:240-245`).
- The processor rejects amber `[v]`. `J/menutypes/ProcessorMenu.java:42,105` looks for the tag `dna_percentage`, but amber stores `DNAData`.
- `getFilterEfficiency` is NaN for `dev_filter`, which has no durability (`:242`) `[inf]`.

### 4.3 Sequencer

Sources: `J/client/screen/SequencerScreen.java` and `J/menutypes/SequencerMenu.java`. Menu buttons 99 to 102 change the tab.

- The edit tab builds `DinoData` on the client. It sends the full object when the player releases the mouse (`SequencerScreen.java:903-908`).
- `J/network/c2s/UpdateEditInfoPacket.java:20-28` has no range check, no menu check and no content check.
- A gene slider is available when `DiskStorageItem.getGeneCompletion >= requirement` (`SequencerScreen.java:434-441`).
- `J/item/DiskStorageItem.java:45` divides by `Genes.GENE_STORAGE.get(gene).size()`. Gender has no donor species. The result is NaN and the gene never becomes available `[inf]`.
- Colour gene completion is the number of different tropical fish colours divided by 16 (`:36-43`).
- `DNAData` never saves tropical fish colours, so colour gene completion stays at 1/16.
- `J/client/widget/GeneButton.java:46` writes 0 into `advancedGenes` when the player selects a gene `[v]`.
- `SequencerBlock.java:81` requires a synthesis time above 0. The diamond chip has -1, so a player cannot install it.
- This agrees with the Cloning Process spec. The spec gives the sequencer no diamond tier.
- `getContainerSize` is 3 (`SequencerBlockEntity.java:309`). A broken block drops only 3 of 9 slots `[inf]`.
- Placeholder text "Cretaceous/Apple" is at `SequencerScreen.java:788-794`.

### 4.4 Egg printer and incubator

- The printer uses an embryo syringe and 16 bone meal. A chicken or parrot in love mode supplies the embryo.
- 10% of eggs crack when the printer has no leveling sensor.
- `saveData` does not save the sensor or the chip (`:143-153`) `[v]`.
- The incubator needs the nest, lid, arm base and arm. The player installs each part with a right-click (`J/block/IncubatorBlock.java:99`).
- Incubation takes 18 s for each percent. The bulbs give 15, 12 and 9 s.
- `J/init/ItemInit.java:46-47` registers the container upgrades as plain `Item` objects.
- `IncubatorBlock.java:51` tests for `instanceof ContainerUpgradeItem` `[v]`. The capacity stays at 3.
- `J/menutypes/IncubatorMenu.java:46` calls `Minecraft.getInstance()` `[v]`. This is a risk on a dedicated server `[inf]`.
- `UpdateIncubatorSlotPacket.java:27-34` can throw an exception on a bad index or position.
- The mod registers `UpdateIncubatorPacket`. No code sends it.

### 4.5 Power

- Four machines call `internalExtract(calculateEnergyConsumption(), true)` `[v]`. The `true` means simulate, so machines never drain power.
- The four call sites are Processor `:93`, Sequencer `:212`, EggPrinter `:102` and Incubator `:311`.
- In `J/block/entity/GeneratorBlockEntity.java:76-90`, coal gives 4 FE one time each second `[v]`.
- The creative generator adds 999999 FE each tick.
- The generator does not save its stored energy (`:58-65`). `updateBlock()` runs each tick.
- In `J/block/entity/BlockEntityElectricFencePole.java:111,225`, a pole holds 350 FE `[v]`. A powered pole drains 10 FE each tick.
- One pole uses 200 FE each second. Coal supplies 4 FE each second.

### 4.6 Data model

- **`DNAData`** (`J/api/DNAData.java`) holds the entity type, percentage, variant, fossil piece, quality and embryo flag. The item tag is `DNAData`.
- **`DinoData`** (`J/api/DinoData.java`) holds these fields:
  - the base species
  - the species percentages
  - the `advancedGenes` overrides
  - 8 layer colours
  - the incubation progress
- The item tag is `DinoData`. `J/init/DataSerializerInit.java:12-27` syncs it.
- `layerColors` (`:31`) is absent from `toNBT` (`:162`), `fromNBT` (`:189`) and `copy` (`:216`) `[v]`.
- `copy()` copies only `advancedGenes`, `entityPercentages` and `basePercentage` `[v]`. It does not copy the base species.
- `getGeneValue` (`:127-141`) returns the override if one exists. If not, it sums the species gene data x (percentage x 2).
- **Genes.** `J/init/GeneInit.java:25-45` registers 21 genes `[v]`:
  - `aggression, defense, eat_rate, health, health_regen, heat_resistance, herd_size`
  - `pack_size, immunity, intelligence, jump, nocturnal, fertility, size`
  - `speed, stomach_capacity, strength, tamability, underwater_capacity, color, gender`
- **Gene data.** `F/datagen/GeneDataProvider.java` has 32 active entries for vanilla mobs `[v]`. Each value is a multiplier (1.3 = +30%).
- About 40 more entries, mostly hostile mobs, are in comments.
- The gene data follows the published Mob Genetics guide. It differs from the newer "Vanilla Animal DNA Effects" table (section 7.5).
- The six dinosaurs have no gene data.
- **Genes read at runtime** `[v]`: `GENDER` (`Dinosaur.java`), `SIZE` (six renderers) and `AGGRESSION` (`DinoNeedsUtils.java:44`). No code uses the aggression result.
- The other 18 genes are only in tooltips and in the editor.

---

## 5. Fences and feeder

### 5.1 Fences

- Low-security posts are 3 blocks high and have 2 wires. High-security posts are 8 blocks high and have 1 wire (`J/block/api/EnumConnectionType.java:8-9`).
- The wire spool links posts that are up to 15 blocks apart (`J/block/ElectricFencePostBlock.java:133-200`).
- A powered wire gives 1 THORNS damage and knockback (`J/block/api/BlockConnectableBase.java:75-111`).
- `BlockConnectableBase.java` has 171 lines of code in comments. Ladder climbing is one example.
- `ElectricFencePostBlock.java:224` has a forced `|| true` from commit 511c1d8.
- Only Forge can restore a wire with a right-click (`F/event/CommonForgeEvents.java:39-75`).

TODO comments in the fence code:

| Location | Text or topic |
| --- | --- |
| `BlockConnectableBase.java:90` | Damage |
| `BlockConnectableBase.java:94,437` | Particles and sound |
| `Connection.java:137` | `fix this` |
| `Connection.java:270,277` | Energy |
| `ElectricFencePostBlock.java:258` | `verify if our block?` |
| `BlockEntityElectricFence.java:114` | `Add more randomness` |

The `rft/adversing` branch rewrites this area (section 2.3).

### 5.2 Reference: the original fence design

These points are from the wiki article and the 2019 demo video:

- Wire runs between poles go in all directions and can incline.
- Each connection has 16 small collision boxes in each block.
- Player ray tracing rotates the ray into the axis of the wire. The rewrite ports this as `RotatedRayBox`.
- A player can break one wire in a block and leave the other wires.
- A broken wire bends at the break. Spark particles appear at the break point.
- Pole yaw is the angle between the first two connections of the pole. A right-click rotates the pole 180 degrees.
- The demo captions are Powering ("actual powering method is still WIP"), Unpowering, Replacement, Item Animation and High security fences.
- A powered pole shows a light.

### 5.3 Carnivore feeder

- `J/block/entity/DinosaurFeederBlockEntity.java` has 3 slots and no GUI. `createMenu` returns null (`:88-90`).
- A right-click changes the open state and the full texture (`:116-128`).
- `getItem` has `case 3` where `case 2` belongs (`:140`).
- `J/block/DinosaurFeederBlock.java:43-57` stores the `ItemStack` object of the player. Then it shrinks that same object.
- The block entity does not save its contents (`:92-97`).
- `J/entity/ai/tasks/EatFromMeatFeeder.java:48-50` calls `foodMap().containsKey(ItemStack)` on a `Map<String, Double>` `[v]`. The result is always false.
- The loop at `:55-62` removes items and never increases hunger `[v]`.
- The range test `<= 2` (`:41`) does not agree with the walk target of 3.
- `NearestFeederSensor.java:43,60` keeps the position in a cache with no time limit. It prints on each sensor tick (`:68`).
- The block entity id is `carrnivore_feeder_one` (`J/init/BlockInit.java:46`) `[v]`. The spelling is wrong.
- The item id `iron_plant_tank_ugprade` also has a spelling error (`J/init/ItemInit.java`) `[v]`.

---

## 6. Dinosaurs

### 6.1 Classes

- `J/entity/dinosaur/Dinosaur.java:86` is abstract. It extends `TamableAnimal` and implements `GeoEntity` and `SmartBrainOwner`.
- `CarnivoreDinosaur` has Tyrannosaurus, Velociraptor and Dilophosaurus.
- `HerbivoreDinosaur` has Brachiosaurus, Triceratops and Gallimimus. The Gallimimus class is in the `omnivore` package.
- All six register as `MobCategory.MONSTER` with placeholder hitboxes (`J/init/EntityInit.java:30-39`).
- The mod has no natural spawning. A dinosaur comes from an egg or from `/summon`.
- Only the egg and breeding set `DINO_BEHAVIOUR`. A dinosaur from a different source has behaviour data of zero.
- The behaviour JSON is the same for all six: 200 health, attack 5, speed 2.0 and aggression 80.

| Species | Animations missing | Layers registered (`J/client/CommonClientClass.java:42-128`) | Notes |
| --- | --- | --- | --- |
| Tyrannosaurus rex | none | 9, plus `feet` and `yellow` for males only | The only species with sound events (`SoundInit.java:15-21`). The repository has 37 `.ogg` files |
| Velociraptor | `getup`, `flinch` | `base` only. Comments disable the others at `:58-68` | |
| Dilophosaurus | `roar`, `getup` | 14, with no `base`. All four frill variants draw together | Issue #60 |
| Triceratops | `run`, `flinch` by those names (section 8.5) | `base` only. The comment at `:97` says "WHY NO WORK FOR TRICERATOPS?" | |
| Brachiosaurus | none | 6 | The bones are `head1` and `head2`, so the head position code does nothing |
| Gallimimus | `drink`, `eat1`, `getup`, `dead`, `roar` | 11 | Issue #59 |

Render scale constants:

| Species | Constant |
| --- | --- |
| Tyrannosaurus | +1.0 |
| Velociraptor | +0.6 |
| Dilophosaurus | +0.5 |
| Gallimimus | +1.0 |
| Triceratops | +2.0 |
| Brachiosaurus | +2.5 |

The growth render scale is 0.25, 0.5, 0.75 and 1.0. The age thresholds are -18000, -12000 and -6000 (`Dinosaur.java:888-914`). Hitboxes never scale.

### 6.2 Brain (`Dinosaur.java:482-586`) `[v]`

**Sensors:**

- water, radius 20
- feeder, radius 20
- living entities that are near
- food item
- fence blocks that are near, radius 10, only while the animal starves

**Core behaviours:** `DinosaurLookAtTarget`, `MoveToWalkTarget`, `SetHunting`, `SetWalkTargetToWaterSource`, `WalkToNearestFeeder`, `SetWalkTargetToFoodItem`, `FollowParent`.

**Idle behaviours:** a `FirstApplicableBehaviour` of `Drink`, `EatFromMeatFeeder`, `Eat`, `Rest`, `GettingUpFromRestBehaviour`, `SoloHuntingBehaviour` and `SoloHuntRoamBehaviour`. A random walk follows.

**Fight behaviours:** `InvalidateAttackTarget`, `SetWalkTargetToAttackTarget`, `BreakFenceBehaviour`, `AnimatableMeleeAttack`.

**Not in any brain:** `StartTurfWar`, `LargeCarnivoreFight`, `SetMateFromNearbyDinosaurs`, `IdleAnimationBehaviour` and `ThreatDisplay`. A comment disables `ThreatDisplay` at `:529`.

| Subsystem | Status | Evidence |
| --- | --- | --- |
| Thirst | Working | `Drink.java`. The walk target is "within 3" but `Drink.java:47` needs distSq <= 5. The animal may stall `[inf]` |
| Hunger, dropped food | Working with a flaw | `DinoNeedsUtils.java:185` sets `IS_HUNGRY`. No code clears it `[inf]` |
| Feeder | Broken | Section 5.3 |
| Stamina and sleep | Partial | The client animation predicate writes `SHOULD_TICK_STAMINA` (`Dinosaur.java:168-172`). No code sets `IS_EXHAUSTED` |
| Growth | Partial | `readAdditionalSaveData` reads all four age flags from `baby_age_boolean` (`:318-321`). A hatched dinosaur is an adult (see section 6.4) |
| Hunting | T. rex only | `VelociraptorEntity.java:15-21` and the Dilophosaurus override ignore the parent result `[v]`. Roaming applies to all species, not only carnivores (`:565`) |
| Fence breaking | Partial | `BreakFenceBehaviour.java:71` sends a message to all players |
| Aggression | Computed, not used | No retaliation behaviour exists |
| Social | Stub | `tickSocial()` is empty (`DinoNeedsUtils.java:245`). `isSociallyDrained` reads `STAMINA` (`:139`) `[v]` |
| Mating | The brain cannot reach it | `nextInt(1,2)` always gives 1 (`Dinosaur.java:877`). `isMate` compares UUIDs with `==` (`:1035`) |
| Turf wars | Not connected | `StartTurfWar.java:72` rolls 0 to 2. `LargeCarnivoreFight.java:100-110` handles 1 to 3 |
| Taming | Stub | No code sets `PLAYER_REPUTATION`. `Dinosaur.java:840` returns early |
| Multipart | Stub | Only T. rex declares a head (`TyrannosaurusRexEntity.java:30-33`). No code calls `resetParts()`. The send methods at `PartEntity.java:222-233` are empty |

### 6.3 Rendering

- `DinosaurRenderer.createLayers` builds the layers one time for each shared renderer. It uses the first entity that draws (`J/client/renderer/DinosaurRenderer.java:73-75,91-93`).
- As a result, layers that depend on gender are wrong for each other animal.
- Each layer draws a tinted pass and then a white pass (`:49-50`).
- `DefaultedEntityGeoModel` expects `textures/entity/<species>.png`. `ClientRegistrationHolder.menuScreens()` makes an alias for those files at runtime (`:58-63,103-108`).
- `RUN_ANIM` exists and no code uses it. The comment at `Dinosaur.java:166` says "TO-DO: Fix running animation".
- Flinch lengths are hard-coded: 34, 39 and 39. Each other species has 0.

### 6.4 Other entity bugs

- `DinoBehaviourData` writes `default_stamina` and reads `default_energy_capacity` (`:101,53`).
- `HAS_GROUP` and `IS_GROUP_LEADER` both use the memory id `is_pack_leader` (`J/init/MemoryModuleTypeInit.java:98,100`) `[v]`.
- 12 memory types appear only at registration. 7 memory types have readers and no writers.
- At `AttributesInit.java:25`, the trust attribute uses the hunger translation key.
- `tickDeath()` is empty (`Dinosaur.java:139`). Dead dinosaurs stay in the world.
- At `Dinosaur.java:974`, `getFoodProperties()` is null for wheat. This likely throws an exception `[inf]`.
- No code reads `R/data/projectnublar/config/hunt_score_config.json`. The values are hard-coded at `DinoNeedsUtils.java:101,107`.
- Each dinosaur has three sensors that scan blocks. Each scan covers about 70k, 70k and 9k blocks. This is a performance risk.
- `DebugStick` prints to the chat of all players.
- `IncubatedEggItem.java:39-44` spawns the dinosaur and does not call `setBaby`. A hatched dinosaur is an adult, so growth does not start.
- `FenceAwareNodeEvaluator.java:16` calls the path check for one block. That call skips the vanilla check of the body size. A large dinosaur can find a path through a gap of one block.
- `DinoNeedsUtils.java:160-169` kills a dinosaur on the next hunger tick after hunger reaches 0. The fence attack needs a starving dinosaur, so it almost never occurs.
- `Dinosaur.java:874-877` gives the young the `DinoData` object of the mother, not a copy. A change to the gender of the young also changes the mother.
- `Dinosaur.java:580` sets the run state when a chase starts. No code clears it. Stamina decreases at two times the rate until the chunk loads again.
- No code uses `F/entity/DinosaurPart.java`, `IDinoPart`, `IMultiPartDino`, `DinoTexture` or `EntityColorStore`.

Debug output locations:

- `DinoNeedsUtils.java:162-166,252`
- `WalkToNearestFeeder.java:63`
- `NearestFeederSensor.java:68`
- `SoloHuntingBehaviour.java:88`
- `LargeCarnivoreFight.java:47,101,105`
- `BehaviourDataReloadListener.java:107,112`

---

## 7. Reference behaviour

This section gives the target numbers. The sources are:

- the 2022 Cloning Guide video (original mod, 1.16)
- the Mob Genetics guide
- the design documents
- the feature documents

### 7.1 Cloning Guide (2022)

| Machine | Parts, in order | Defaults | Upgrades |
| --- | --- | --- | --- |
| Fossil processor | Base, body, lid, tanks | A run takes 4 minutes. The tank holds 2 buckets. The filter sets the yield and it wears. An iron filter gave 2 genetic data and a diamond filter gave 5 | Tanks: 3 buckets, then 4 with diamond. Chips: iron 3 min, gold 2 min, diamond 1 min |
| Sequencer | Base, door, computer, monitor. The monitor goes after the computer. Dye changes the computer colour | Hard drive 10 s for each sample, SSD 5 s. Synthesis takes 10 minutes | Tanks: 150, 200, 250 and 300% (iron, gold, diamond, netherite). Chips: iron 7 min, gold 4 min |
| Egg printer | Base first. Then needle, platform and body in any order | A print takes 10 minutes. 10% of eggs break | The leveling sensor prevents breaks. Chips: iron 8, gold 6, diamond 4 min |
| Incubator | Base and nest, lid, lift, arm base, arm | 3 eggs. About 30 minutes (18 s for each percent). 100 plant matter | Containers: 6 and 9 eggs. Bulbs: 15, 12 and 9 s for each percent. Tanks: 150 and 200 plant matter |

Rules that the video shows:

- Fossils were in stone, sandstone and terracotta.
- A syringe on a mob gives the DNA of that mob.
- An embryo needs a chicken or parrot in love mode.
- A player can use the base dinosaur only at 50% or more.
- More donor slots become available as the dinosaur percentage increases. The total must be 100%.
- Each dinosaur has a primary colour layer and a secondary colour layer. More donor DNA gives a larger colour change.
- A player can select the gender at 80%.
- To isolate a gene, sequence each animal that has the gene to 100%. Colour needs each tropical fish colour.
- An artificial egg and a DNA tube in a crafting grid give the unincubated egg.
- The egg position in the incubator screen sets the egg position on the bed. An arm turns the eggs.

The rewrite already agrees with the times for the processor, drives, synthesis, printer and incubator.

### 7.2 Screens in the video

- **Processor.** A water gauge (500 mB / 4000 mB with diamond tanks), a filter slot and a 3x3 output grid.
- **Sequencer tabs.** Sequence, Edit and Synthesis. The Sequence tab has a Regular view and an Isolated view.
- **Isolated view.** It lists genes with a percent, for example Immunity 50%. It lists the animals that the gene still needs, for example Pig 100% and Turtle 0%.
- **Edit tab.** It has an entity preview with a player for scale, donor sliders and a list of sequenced animals.
- **Effect text.** The Edit tab shows effects such as "Speed +3%" and "Size -5%" with colour samples.
- **Advanced view.** A Basic/Advanced switch opens a chromosome grid, "Gender: Random" and "Reset Gene".
- **Synthesis tab.** It has gauges for Water, Sugar, Bone Matter and Plant Matter. Each gauge has its own input slot.

### 7.3 Values from the design documents

- **Roadmap ("Release Plan").** Beta is Tyrannosaurus, Velociraptor, Brachiosaurus, Triceratops and Dilophosaurus.
- Release 1 adds Compsognathus, Gallimimus, Microceratus, Mosasaurus, Parasaurolophus and five plants.
- Pteranodon is in Release 2. The summary page of the design document lists it in Release 1.
- The plan continues through Releases 2 to 13, X and NVR.
- Issue #6 gives hunger and saturation values for each clade and size class.
- Issue #1 gives the fossil rarity formula: ore rarity x period rarity x species weight x piece weight x quality weight.

Species temperament from the design document, each out of 10:

| Species | Aggressiveness | Intelligence | Group tendency | Metabolism |
| --- | --- | --- | --- | --- |
| Tyrannosaurus rex | 7 | 3-4 | 2-3 | 7-8 |
| Velociraptor | 7-9 | 6-8 | 6-9 | 5-6 |
| Brachiosaurus | 1-2 | 0.5-1 | 5-7 | 8-10 |
| Triceratops | 5-7 | 1-2 | 6-7 | 5-7 |
| Gallimimus | 1-3 | 2-3 | 7-10 | 7-8 |
| Dilophosaurus | 5-6 | 2-4 | 3-5 | 4-6 |

### 7.4 Cloning Process spec against code

Source: `_Cloning Process.docx` in the feature document folder. The code values are from `J/init/ItemInit.java:20-59`, `J/item/TankItem.java` and the block entities `[v]`.

| Value | Spec | Code | Match |
| --- | --- | --- | --- |
| Filter efficiency | Iron 25%, gold 50%, diamond 100%. A worn filter decreases to 25% of its base | `FilterItem` 0.25, 0.5, 1.0. `ProcessorBlockEntity.java:240-245` multiplies by `0.75 x remaining + 0.25` | Yes |
| Processor tank | 2, 3, 4 and 8 buckets. Netherite is "possible" | 2000 mB by default (`:247`). Then 3000, 4000 and 8000 mB | Yes |
| Processor time | 4, 3, 2 and 1 minutes | Same | Yes |
| Storage device | Hard drive 10 s, SSD 5 s. The item stores the data. An "Ender SSD" may follow | `DiskStorageItem` 200 and 100 ticks. The data is in the NBT of the item | Yes |
| Synthesis time | 10, 7 and 4 minutes. No diamond tier | Same. The diamond chip is -1 | Yes |
| Sequencer tank, matter | 16, 24, 32, 40, 48 | 16, 24, 32, 40. No netherite tank | Yes, without netherite |
| Sequencer tank, water | 1, 1.5, 2, 2.5 and 3 buckets | 1000, 2000, 3000 and 4000 mB | No |
| Synthesis output | A syringe with a dinosaur embryo. The egg printer uses it | `TEST_TUBE_ITEM` (`SequencerBlockEntity.java:215`). A crafting recipe joins it with a blank artificial egg | No |
| Dyeable sequencer | A right-click with dye colours the black sections | `J/block` has no dye code | No |
| Egg printer time | 10, 8, 6 and 4 minutes | Same | Yes |
| Cracked eggs | 10%. 0% with the leveling sensor | Same. The code does not save the sensor | Yes |
| Incubator capacity | 3, 6 and 9 eggs. The spec says the 6-egg tier "NEEDS FIXING" | The capacity stays at 3 (section 4.4) | No |
| Incubation time | 18, 15, 12 and 9 s for each percent | `BulbItem` 300, 240 and 180 ticks | Yes |
| Incubator plant matter | 100, 150, 200 | 64 by default (`IncubatorBlockEntity.java:222`). `PlantTankItem` 128 and 192 | No |
| Incubator arm | The arm reaches each egg in range, with no preset positions | Not examined | |
| Cross-machine tabs | The player changes between linked machines and stays in the screen | No code | No |

The sequencer adds water 1000 mB at a time (`SequencerBlockEntity.java:190-191`). The half-bucket tiers of the spec need a change to that fill code.

The spec contains one image. It is a strip of tab icons (egg, bone, DNA, nest) in active and inactive states. The icons are for the cross-machine tabs.

### 7.5 Vanilla Animal DNA Effects against code

Source: `Vanilla Animal DNA Effects.txt`, 35 animals. Code: `F/datagen/GeneDataProvider.java` `[v]`.

**Entry errors.** In four values, a percentage seems to be in the place of a multiplier:

| Animal and gene | Code | Table |
| --- | --- | --- |
| Turtle health | 0.75 (`:235`) | +75% |
| Rabbit size | 0.75 (`:174`) | -75% |
| Spider nocturnal | 0.75 (`:201`) | +75% |
| Cat size | 0.6 (`:49`) | -60% |

**Wrong gene.** Chicken has size 0.5 (`:56`). The table says intelligence -50%.

**Different values:**

| Animal and gene | Code | Table |
| --- | --- | --- |
| Wolf tamability | 2.75 | +100% |
| Polar bear speed | 1.5 | +20% |
| Panda speed | 0.8 | +10% |
| Panda fertility | 0.7 | -25% |
| Bee health | 0.5 | -30% |
| Hoglin heat resistance | 1.5 | +55% |
| Hoglin tamability | 0.5 | -75% |
| Hoglin aggression | 1.85 | +87% |
| Mule fertility | 0.5 | -40% |
| Ocelot tamability | 0.7 | -35% |

**Values that the code does not have:**

- horse speed +15%
- dolphin intelligence +75% and group size +20%
- parrot domesticity +30%
- polar bear aggression +45%

**Empty entries.** Axolotl, goat, glow squid and mooshroom.

**No entry.** Frog, camel, sniffer, armadillo, nautilus and villager. The table says the villager "randomizes everything every time it's used".

**Not in Minecraft 1.20.1.** Armadillo, nautilus and the wolf coat variants in the table.

**Genes in the table that the registry does not have:**

- food detection
- water detection
- poison resistance
- gestation time
- vision (low light and movement-based)
- egg clutch
- energy
- thirst

**Names that differ:**

| Table name | Registry name |
| --- | --- |
| fire resistance | `heat_resistance` |
| domesticity | `tamability` |
| group size | `herd_size` or `pack_size` |
| hunger | `stomach_capacity` |

**Behaviour effects that have no gene:**

- grass eating (cow, sheep, goat)
- mushroom eating (mooshroom)
- poison bite (bee, cave spider)
- item carrying and spit (llama)
- sound imitation and cocoa allergy (parrot)
- clucking (chicken)

### 7.6 Other feature documents

| Document | Content | Code |
| --- | --- | --- |
| `## Special Genes.txt` | Seven special genes with donor animals. See the list below this table | None. Cuttlefish, reed frog, tree frog and pit viper are not vanilla animals |
| `Primary Sources of DNA.txt` | Fossil blocks, trace fossils, plant fossils, amber, frozen sources and skeletal structures. See the list below | Bone pieces and amber blocks only |
| `Dino VFX.txt` | About 25 effects. See the list below | The mod registers no custom particle types |
| `Idea False Eggs.txt` | Nest training and clutch control with empty printed eggs. Hatch and fertilisation success rates | None |
| `Paleo-Illagers.txt` | Hostile paleontologist illagers at digsites. An "ancient plight" effect that spawns fossilised raider skeletons | None |
| `Release Plan.txt` | Section 7.3 | |

Special genes:

| Gene | Donor | Effect |
| --- | --- | --- |
| Camouflage | Cuttlefish | Compatible with four species |
| Gender swapping | Common reed frog | A group test that takes 10 days |
| Bioluminescence | Glow squid | Cosmetic only |
| Lysine contingency | None named | The animal needs lysine or it dies |
| Poison | Cave spider, bee | A bite or spit gives poison |
| Hyper intelligence 1.0 and 2.0 | Camel, parrot, wolf, fox, mule | See below |
| Thermal detection | Tree frog, squid, glow squid, salmon, pit viper | The animal senses players through walls |

Hyper intelligence gives these abilities:

- The animal opens doors.
- The animal steals from chests.
- The animal detects electric fences that have no power.
- The animal gets +15% intelligence and detection.

Primary Sources of DNA:

- The fossil block has 3 qualities: good, average and poor. A loot table uses position, biome and depth.
- Bone pieces: skull, ribcage, foot, arm, leg, tail, spine.
- Trace fossils: shell, footprint.
- Plant fossils: wood, leaf, fruit.
- Secondary sources: amber, frozen flesh, frozen plants.
- Special source: skeletal structures.

Dino VFX examples:

- roar spit and drinking drips
- blood overlays and plant particles
- breath effects and air trails
- footprints that fade
- screen shake with a config switch
- dust trails and a snow overlay
- sleep effects
- a Dilophosaurus spit projectile
- splashes and water ripples

### 7.7 Park-system diagrams

Seven diagrams (`image.png` to `image7.png`) model the control systems of the film park. No code implements them. Fence power is the only related system.

| Diagram | Content | Possible mod feature |
| --- | --- | --- |
| Master control overview | Command layer, redundancy, integration bus, seven core systems | Top-level park manager |
| Command centre | Command engine, scheduler, event log, authentication with clearance levels 1 to 5, recovery | Tablet park manager, access levels |
| Power management | Geothermal main supply, auxiliary backup, emergency units. Three primary grids and an emergency grid. Load balancing. Failover and restart sequence | Generators, pylons, sectors, power loss |
| Security | 24 fence sectors, 10,000 V control, integrity monitoring, moat control, cameras, doors, intrusion alerts, lockdown | Fence sectors and breach alerts |
| Tour management | Vehicles that follow a track, scheduling, exhibit timing, safety stops | Tour vehicles |
| Communications | Radio, phones, intercom, emergency alerts | Notifications |
| Lab data model | See the list below | Fields for `DNAData`, `DinoData`, the incubator and specimen tracking |

The lab data model has these records:

- DNA sequence library: completeness ratio, source amber
- gene modification records: frog DNA markers
- incubator control: temperature, humidity, rotation
- embryo tracker: viability score
- specimen records: genetic purity, lysine dependency
- breeding control

### 7.8 Tyrannosaurus card against code

Source: `Tyrannosaurus.docx`, a draft card from the project board. The card dates from 2024-04-21 and its last edit was on 19 March.

Code sources `[v]`:

- `R/data/projectnublar/config/dinosaurs/tyrannosaurus_rex_config.json`
- `J/init/EntityInit.java`
- `R/assets/projectnublar/animations/entity/tyrannosaurus_rex.animation.json`
- `J/config/FossilsConfig.java` and `J/api/FossilPieces.java` on `fossils-1.20.1`

| Value | Card | Code |
| --- | --- | --- |
| Clade, genders | Tyrannosauridae. Male and female | No clade field. A gender gene |
| Health | 155 | `max_health` 200, the same for all six species |
| Health regen | 15 ticks for each half heart | `health_regen` 5.0 |
| Damage | 16 | `attack_damage` 5.0 |
| Defense | 12 | `resistance` 2.0 |
| Speed | 0.50 | `speed_multiplier` 2.0, a different unit |
| Metabolism/energy | 203 | `max_stamina` 300 |
| Hunger, thirst | 80, 70 | The JSON has no capacity field. `eat_rate_per_day` and `drink_rate_per_day` are 5.0 |
| Group size | 1 to 2 | `group_size` 5 |
| Egg clutch | 12 to 24 | `egg_clutch_size` 3.0. The breeding code always gives 1 (`Dinosaur.java:877`) |
| Hatch success | 5% | None |
| Growth | Maximum size by in-game day 45 | Stage thresholds at -18000, -12000 and -6000 ticks. The code ignores `growth_rate` |
| Height | Hatchling 0.30, infant 1.20, adult 3.7795 to 6 blocks. Gender changes the weights | `sized(1, 3)` at each age. The size gene sets the render scale |
| Variation | Each animal gets random stats and DNA | None |
| Fossil formation | Hell Creek | No field |
| Fossil biomes | Arid (no savanna) 75%, flatland 35%, woodland (no jungle) 15%, highland 8% | Fossils branch: each overworld biome. No code uses the config `biomes` list |
| Fossil period | Cretaceous: min_y 55, max_y 75, rarity_mod 2.95 | Fossils branch: min_y 40, max_y 56, rarity_mod 0.4, with a maximum of 1.0 |
| Fossil quality (weight / DNA yield) | Fragmented 40 / 5%, poor 20 / 7.5%, common 15 / 10%, pristine 4 / 30% | Fossils branch: the same values |
| Fossil set | Biped set and a special tooth piece. See the piece table below | Fossils branch: skull, hand, foot, pelvis, leg, arm, neck, ribcage, tail. No tooth and no maximum quantities |
| Amber | Weight 5%, DNA yield up to 80% | A comment disables it on the fossils branch. The processor rejects it on `1.20.1-ai` |
| Diet | Prey preferences by clade. See the prey table below | 8 vanilla meat items in `tyrannosaur_food_config.json`. The hunt score is hard-coded at `DinoNeedsUtils.java:101,107` |
| AI | See the AI list below | Section 6.2 |
| Animations | 32 names | 26 in the file. See the lists below |
| Items | 24 fossil items (4 qualities x 6 pieces). Drops of fresh bones, tooth and meat. Soft tissue, amber and egg | The fossil item only. No drops |

`R/.../periods/periods.json` on `1.20.1-ai` has the card values for the period: 55, 75 and 2.95. No code reads that file.

Fossil pieces on the card:

| Piece | Maximum quantity | Weight |
| --- | --- | --- |
| Skull | 1 | 25% |
| Neck | 1 | 35% |
| Arm | 2 | 40% |
| Rib cage | 1 | 35% |
| Tail | 1 | 45% |
| Leg | 2 | 45% |
| Tooth (special piece) | 8 | 25% |

Prey preferences on the card:

| Preference | Weight | Clades |
| --- | --- | --- |
| Highly preferred | 51% | Ceratopsia, Ornithomimosaurid, Ankylopollexia |
| Somewhat preferred | 38% | Hypsilophodontidae, Pachycephalosaurid |
| Neutral | 17% | Sauropodomorpha, Thyreophora, Pterosauromorpha, Compsognathidae |
| Avoided | 45% | Paraves, Tetanurae |
| Highly avoided | 80% | Spinosauridae, Allosauroidea, Abelisauroidea, Mosasauria |
| No-go | 95% | Tyrannosauridae |

The card also lists about 25 vanilla prey. Villagers and the player are in that list.

AI on the card:

- solo hunting, pair hunting and scavenging
- dominance and communication
- idle animations
- combat: attack, defense, threat, aggression, fleeing
- taming
- movement, with swimming and sprinting
- play, call and group feeding
- mating, nesting and infants

Animation names on the card that the file does not have: `drinking2`, `sitting`, `sleeping`, `roar 2`, `injured`, `death`, `bite`, `startled`, `tranqed`, `attack 2`.

Animation names in the file that the card does not have: `getup`, `idle`, `nesting`, `restidle`.

Asset owners on the card:

| Asset | Owner |
| --- | --- |
| Models | @kashgummaraju |
| Textures | @Mazeekeen |
| Eggs | @Brotatsun64 |
| Animations | @Elysisy |
| Items | @ItzFred |

The fossil quality values on the card are the values in the fossils branch. That ladder (fragmented, poor, common, pristine) is the working standard.

### 7.9 Velociraptor card against code

Source: `Velociraptor.docx`, a draft card from the project board. Its last edit was on 25 February. The card has no stats section.

| Item | Card | Code |
| --- | --- | --- |
| Models | Nublar, Sorna, egg | One `velociraptor.geo.json` |
| Textures | 11 layer sets. Nublar and Sorna, male and female, with crest, quill and arm-quill variants | `male/` and `female/` with 10 files each. The code registers only the `base` layer |
| Animations | 22 for each model | 29 in one file. See the notes below |
| Fossils | 28 items: skull, toe claw, rib cage, leg, arm, tail and neck in 4 qualities | None on any branch |
| Drops | Fresh bones, toe claw, meat | None |
| AI | Solo hunting, pack hunting, scavenging, dominance, communication, and the Tyrannosaurus list | It targets only its last attacker (`VelociraptorEntity.java:15-21`) |

Animation notes for Velociraptor:

- The file does not have sitting, sniffing ground, calling or injured.
- The file has `dead`, `attack1` and `shakebody` where the card says death, bite and shake head.

### 7.10 Fossil ore notes

**`Fossil Ore Aesthetics.docx`** is a draft card from 2024-04-29. It asks for three random properties for each ore block:

- The overlay tint agrees with the host block. The card marks this part "implemented". The tint strength is random from 0 to 90%.
- The overlay opacity is random from 15 to 100%. This makes fossils more difficult to see.
- The block rotation is random.

The card says opacity and rotation "require a custom baked model". On `fossils-1.20.1`, `F/event/ClientModEventsForge.java:41-54` returns one fixed colour for each host stone `[v]`:

| Host stone | Colour |
| --- | --- |
| Sandstone | `0xE5D38C` |
| Deepslate | `0x4F4F56` |
| Stone | white |

No code gives a random strength, a random opacity or a random rotation.

**`Fossil Block Generation.docx`** gives the reason for the fossils branch:

- One block for each stone x species x quality x part makes 4 JSON files for each block.
- The full fossil list would make thousands of files.
- The aim is custom block-state properties and "maybe just 4 json files total". The note says this method does not work at this time.
- The screenshot in the note lists files such as `common_brown_terracotta_tyrannosaurus_rex_wing_fossil.json`.
- That file shows that the old code registered pieces for species that cannot have them.
- The fossils branch puts quality in a block state. One species has 27 blocks and 108 models.
- The number still increases with each species, piece and stone.

**`Backend Refactor.docx`** has the same text as issue #65.

---

## 8. Gap analysis

### 8.1 Commission brief

| Item | Request | In code |
| --- | --- | --- |
| Loaders | Forge and Fabric | Forge only |
| Energy | FE and a Fabric equivalent | The machines connect to Botarium. They never drain energy |
| Entities | 7, with Pteranodon | 6 |
| Entity items | Fossil sprites, meat, raptor claw, horns, eggs | The fossil item and eggs |
| Cloning items | Syringes, eggs, bulbs, HDD, SSD, tube, parts, tanks, amber, tooth, fragmented bone, trilobite, feather, unidentified bone | All but the last five |
| Blocks | Fossil ore, amber ore, three wood sets | No wood |
| Plants | 5 | 0 |
| Machines | 4 cloning machines, two generators, three fence types, pylon | No large generator, no overhang fence, no pylon |
| Fossil hunting | Maps, digsites, traders | None |
| Behaviours | About 30 | Drink, eat, rest, solo hunt, melee, growth |
| Genes | 26 names | 21 |

The registry does not have these genes from the brief: thirst, energy, growth rate, morph, social, gestation time, egg clutch and vision.

### 8.2 Systems in the design document that have no code

- diseases and treatment
- stress
- scent markers and detection levels
- block comfort values
- special genes
- false eggs
- the DNA server block
- tablet apps
- Paleo-Illager digsites
- vehicles
- guns
- animals that are not dinosaurs

### 8.3 Original mod (`Dumb-Code/ProjectNublar`, branch `1.16`)

The rewrite ports these items:

- the four machines
- coal power and creative power
- fences and the wire spool
- the gene registry and the drives
- the AI for hunger, thirst and hunting

The rewrite does not have these items:

- the mood system
- egg laying and the egg entity
- dinosaur drops
- a working multipart system (designed: Beta modules B26 and B27, section 11.9)
- the drill extractor
- the skeletal builder
- pylons
- the tracking beacon and the tablet
- the gyrosphere
- plants (cycad, Serenna veriformans)
- digsite structures
- commands and spawn eggs
- the villager trade
- spark particles

The original had 38 packet classes. The rewrite has 3.

### 8.4 Not on any branch

- vehicles, the tablet and plants
- gates, commands and structures
- JEI or a similar integration
- spawn eggs and a guide book
- advancements other than the one recipe advancement
- translations other than `en_us`
- tests and CI

### 8.5 Asset repository

The analysis read the file tree of the private repository `Dumb-Code/ProjectNublar-Assets` on 2026-10-05. It used the GitHub API and did not clone the repository. The tree has 3,718 files.

Limits of this inventory:

- "In the mod" means a PNG or OGG file with the same bytes, or a model with the same animation names.
- The analysis did not open the `.dca`, `.dcm` and `.tbl` files. An item from those files is from the file name only.

**Formats:**

- The repository has no `.geo.json` file.
- A current model is a Blockbench `.bbmodel` file. A Blockbench export makes it ready for GeckoLib.
- An older model is a DumbCode Studio file (`.dcm` or `.tbl`). It needs a conversion.
- PNG files and OGG files are ready.

**The six species in the mod.** The repository has these items that the mod does not have:

| Species | In the repository and not in the mod |
| --- | --- |
| Tyrannosaurus | Two older skins. The animations `stalk` and `deadpose` |
| Velociraptor | A JP3 model. Sorna skins and four raptor squad skins. 30 sounds |
| Dilophosaurus | A second skin, damage overlays and a spit particle. 31 sounds |
| Triceratops | JP3 skins and 28 horn variants. The animations `drink2`, `eat2` and `call2`. 32 sounds |
| Brachiosaurus | A JP3 model. JP3 skins and a JW skin. 30 sounds |
| Gallimimus | A JP male skin and JW skins. 30 sounds |

- For Gallimimus, Brachiosaurus and Dilophosaurus, the mod uses one skin for the two genders `[v]`.
- The repository has male skins for Gallimimus and Brachiosaurus. It has no current male skin for Dilophosaurus.
- The Gallimimus model in the mod is newer than the model in the repository.

**The missing animations of section 6.1:**

- Triceratops: the mod file has `Sprint ` and `injured` `[v]`. These are possibly `run` and `flinch` with different names `[inf]`.
- Velociraptor `getup` and Dilophosaurus `roar`: only older `.dca` files exist, from the file names.
- No art exists for the other seven animations:
  - Velociraptor `flinch`
  - Dilophosaurus `getup`
  - Gallimimus `drink`, `eat1`, `getup`, `dead` and `roar`

**Other creatures:**

- Parasaurolophus is the most complete: a model, 8 skin sets, 24 animations and 31 sounds.
- Pteranodon has a model, textures and 30 sounds. It has no animations.
- Compsognathus, Allosaurus and Carnotaurus have a model, skins and 5 or 6 animations each.
- About 20 more species have a model or sounds only.

**Other art that the mod does not have:**

| Category | In the repository | State |
| --- | --- | --- |
| Fossil and bone sprites | 80 sprites, with skulls for 10 species | Ready |
| Fossil block textures | 45 textures for trace fossils and generic fossils. One amber ore | Ready |
| Meat | Raw meat and cooked meat for 11 species | Ready |
| Eggs | 7 egg models and 6 spawn egg sprites | The models need a conversion |
| Machines | Gestation tank, skeletal builder, park beacon | Need a conversion |
| Upgrade sprites | Drill tops, fans, a netherite tank and a medium storage | Ready |
| Generators and pylons | A basic generator, an advanced generator and 4 pylon models | Need a conversion |
| Fences | A large gate, a perimeter fence, voltage signs and paddock signs | The models need a conversion |
| Feeders | Feeder 1 to 6 and a fish feeder, with open and idle animations | Need a Blockbench export |
| Vehicles | Jeep Wrangler, Ford Explorer, gyrosphere, monorail, two helicopters, two trucks and 7 transport cages | Need a Blockbench export |
| Guns | 11 current models. Ammo sprites for a shotgun shell, a taser cartridge and a tranquilizer dart | The models need a Blockbench export |
| Plants and wood | Five wood sets and about 12 plants | Most are ready |
| Tablet | Sprites for the tablet and a tracking beacon. No screens | Ready |
| Villagers | One paleontologist skin. No illager skin | Ready |
| GUI | 37 textures, with 8 tab textures for the machine screens | Ready |

The repository has no item sprites for the raptor claw, the Triceratops horn or teeth.

**Defects in the mod that the inventory found** `[v]`:

- `R/assets/projectnublar/textures/block/fossil_overlay/common/pelvis.png` has 0 bytes. The repository has a Tyrannosaurus pelvis sprite.
- The three Triceratops skull overlays are copies of the Tyrannosaurus files.
- `item/dino_overlay/triceratops.png` is a copy of the Tyrannosaurus file. The repository has Triceratops sprites for the skull and the DNA icon.

**Result for the Beta scope (section 11):**

- Ready art: sounds for the five species with no sound, male skins for two species, fossil sprites, meat and the paleontologist skin.
- Art that needs a Blockbench export: feeders, vehicles and guns.
- Art that needs a conversion: generators, pylons, the gate and eggs.
- No art: seven animations, the special drops, illager skins and tablet screens.

The newest commits are from March and April 2026. They add Parasaurolophus skull sprites and new model versions for Tyrannosaurus, Compsognathus and Brachiosaurus.

### 8.6 Project boards

The project lead gave read access to the GitHub Projects boards on 2026-10-05. The analysis read the three boards of `Dumb-Code` on that date.

| Board | Cards | Content |
| --- | --- | --- |
| Project Nublar MVP (number 5) | 18 | Species cards and feature cards for the rewrite |
| Programming (number 4) | 8 | Older cards for digsites, paleontologist villagers and fossil ore |
| Project: Nublar Tasks (number 2) | 82 | The art task board. Each card has a milestone and a status for model, texture, animation and sound |

**Species cards on the MVP board:**

- The Tyrannosaurus card has the same text as `Tyrannosaurus.docx` (section 7.8).
- The Velociraptor card has no stats (section 7.9).
- The cards for Triceratops, Brachiosaurus, Dilophosaurus and Gallimimus have no text.
- As a result, Tyrannosaurus is the only species with stats. The boards have no more stats to export.

**Other cards on the MVP board:**

- "Digsites/Paleontologist Villager" says that a digsite is a custom type of village. It has a hole in the ground with exposed fossils.
- The same card says that the paleontologist villager spawns in normal villages and trades for items of the mod.
- "Cloning System & Power" asks for the function of the 2022 video. It links a Google document that the analysis did not read.
- The cards "Fences", "Plants" and "Genetics System" have no text.
- The board marks "Beta Fossils Technical Writeup" and "Fossil Block Generation" as done.

**Digsite design on the Programming board.** These cards are older than the rewrite. They add these details to section 11.3:

- Digsites go from empty sites to large excavations with tents, cloning machines and tools.
- A site has one or more exposed fossil veins of the species for that biome.
- Larger sites have more veins and are more rare.
- Cartographers, librarians and wandering traders trade maps to digsites.
- Paleontologists at a digsite trade maps to other digsites.
- Paleontologist, cartographer and nitwit villagers live at a digsite. Iron golems guard the site.
- Paleontologists also spawn at a low rate in normal villages, in a laboratory building or a camp.
- A paleobotanist villager has plant trades.

**Fossil data on the Programming board.** The card "Fossil Ore Generation" has formations, ages and biomes for the six Beta species:

| Species | Formations | Age in million years | Biomes |
| --- | --- | --- | --- |
| Tyrannosaurus | Hell Creek, Lance | 83.6 to 65 | Hills, forests, taiga, old growth taiga, rivers, badlands, plains, caves |
| Velociraptor | Cloverly, Bayan Mandahu, Flaming Cliffs, Djadochta | 75 to 71 | Desert, badlands, plains, hills, caves, swamps, rivers |
| Brachiosaurus | Tendaguru, Morrison | 154 to 150 | Desert, forest, badlands, plains, hills, caves, swamps, rivers |
| Triceratops | Hell Creek, Lance | 68 to 65 | Hills, forests, taiga, old growth taiga, rivers, badlands, plains, caves |
| Dilophosaurus | Kayenta, Wingate, Moenave, Navajo | 201.3 to 182.7 | Desert, badlands, caves, rivers |
| Gallimimus | Nemegt | 73 to 68 | Desert, badlands, caves, rivers |

- This list is the only source of fossil biomes for the five species with no stats card.
- For Tyrannosaurus, the list does not agree with the newer card (section 7.8).
- The same card asks for veins of 3 to 4 blocks and a fossil pouch that collects fossils.

**The art task board against section 11.** The board gives a Beta milestone to items that section 11 does not put in Beta:

- plants: four wood sets, cycad, Serenna veriformans, West Indian lilac, heliconia and horsetail. The board marks their art as complete.
- animals: frog and goat
- the skeletal builder and the skeletons of the six species
- park decorations: enclosure markers and park signs

The project lead answered these differences on 2026-10-05:

- Plants stay in Release 1. The complete art waits.
- The frogs of the mod are not in Beta. In Beta, the vanilla frog is the donor for the gender swapping gene.
- The goat of the mod is not in Beta. In Beta, the vanilla goat is live prey, as each vanilla animal is.
- The skeletal builder and the skeletons stay after Beta.
- The enclosure marker and the park signs stay after Beta.
- The three voltage signs for fences are in Beta.
- The petrified tree is in Release 1, with the plants. The board gives it a Beta milestone and it has no art.

One difference has the opposite direction. The board gives a Release 1 milestone to the card "Park Management Machines". That card has feeders, two generators, a pylon, a tracking beacon and three gates. Section 11 puts feeders, the large generator and pylons in Beta.

- The decisions of section 11 apply. Feeders, the large generator and pylons are in Beta.
- The live bait feeder is also in Beta. Its art is complete.
- The tracking beacon and the gates are not in Beta. The board and section 11 agree on these two items.

The board marks sound as complete for the six Beta species. The mod has sounds for Tyrannosaurus only (section 8.5).

---

## 9. Open GitHub issues

| Issue | Evidence in the code |
| --- | --- |
| #65 Backend Refactor | `fossils-1.20.1` and `rft/adversing` both address it. Their designs are not compatible |
| #64 Spinning on block edges | The analysis found no cause |
| #63 De-rendering | The render scale is up to 3.5x. The placeholder bounding box does not scale `[inf]` |
| #62 Datapack config | No code calls the getters. The keys in `periods.json` do not match. Only Forge has the listeners |
| #61 Hitboxes | The hitboxes are placeholders with a constant size. Multipart is a stub. Designed for Beta modules B26 and B27 (section 11.9) |
| #60 Dilophosaurus lag | 14 layers. Each layer draws two times |
| #59 Gallimimus | Five animations are missing |
| #49 Gene editing limits | The server has no range check |
| #41, #42, #43, #45, #48, #51, #23 | Not examined |

---

## 10. Pending work, in the suggested order

1. Integrate the three branches (section 2.6).
2. Clean the repository and add CI (section 3.5).
3. Add fossils for each Beta species.
   - Add worldgen, silk touch, fortune and DNA yield.
   - Add amber and piece names. Trace fossils are not in Beta (section 11.2).
4. Send edited genetics to the animal.
   - Save colours and the base species in `DinoData`.
   - Apply genes to attributes.
   - Add random variation for each animal.
5. Check `UpdateEditInfoPacket` and `UpdateIncubatorSlotPacket` on the server.
6. Drain real power.
   - Balance the generator against fence use and machine use.
   - Save the generator energy.
7. Add survival recipes for machines, parts, upgrades and tools.
8. Fix the upgrades and the data.
   - Fix the container upgrades.
   - Save the printer upgrades.
   - Change the sequencer water tiers to the Cloning Process spec (section 7.4).
   - Change the incubator plant matter capacities to the spec.
   - Update the gene data from the new table (section 7.5).
9. Complete the feeder. Fix the two ids with spelling errors before a world depends on them.
10. Give each species its own stats.
    - The Tyrannosaurus card has exact values (section 7.8).
    - The other species have only temperament ratings (section 7.3).
11. Scale hitboxes with growth and size. Complete the multipart system. The design is in [dinosaur-hitboxes-and-movement-design.md](dinosaur-hitboxes-and-movement-design.md) (Beta modules B26 and B27).
12. Connect the behaviours that exist.
    - Connect mating, `ThreatDisplay`, retaliation and the social need.
    - Turf wars are not in Beta (section 11.10).
    - Fix the `canTarget` overrides.
13. Fix rendering.
    - Select layers for each entity.
    - Add the missing animations.
    - Decrease the layer cost of Dilophosaurus.
    - Fix the running animation.
14. Remove debug output.
    - Remove debug prints and chat messages.
    - Decrease the sensor scans.
    - Remove the memory types that no code uses.
15. Give Fabric parity with Forge.
    - After the port, the loaders are Fabric and NeoForge (section 11.17).
16. Build the Beta scope that has no code.
    - Section 11.15 lists that scope.
    - Section 11.13 lists the scope that is not in Beta.

The `rft/adversing` branch has 53 `TODO(BUG)` comments. They give a list with more detail.

---

## 11. Design decisions for Beta

Date: 2026-10-05. The project lead answered questions about each feature module. This section records the answers.

Each item is an answer from the project lead, unless the item names a different source. "In Beta" means that the Beta release must have the item.

In a second pass on the same date, the project lead answered eight interpretations of the analysis. This section has those answers. No interpretation stays.

### 11.1 Fossils

**Occurrence:**

- Fossil ore occurs in four forms:
  - single ores
  - small clusters
  - skeleton clusters
  - digsite deposits
- One cluster holds one species.
- A skeleton cluster holds the bones of one animal.
- The host stones in Beta are stone, sandstone and deepslate. Terracotta is not in Beta.
- A fossil keeps the biome where it generated. That biome sets the skin variant later (section 11.6).
- A skeleton cluster usually holds only some of the bones. The piece weights set which bones are present.
- The "quantity max" value on the Tyrannosaurus card is the usual maximum of a piece in one skeleton cluster.
- That value is not a hard limit. A second skull or a third leg can occur in one cluster, but this is rare.
- About 1 of 20 skeleton clusters holds a piece above the usual maximum.
- That chance is a config value. Playtests set the final value.

**Quality:**

- The worldgen sets the quality of each fossil ore. The quality is a block state.
- The ore shows its quality to the player before the player mines it.
- A cluster has a base quality. Each block in the cluster gets its own quality near that base.
- Fortune gives a chance of a drop that is one tier higher.
- Issue #1 says that Silk Touch drops the ore block.

**Weights:**

- Each weight is relative. Issue #1 gives the formula:
  `rarity = ore rarity x period rarity x species share x piece share x quality share`
- A share is one weight divided by the total of the weights in its group.
- The period rarity multiplies the quantity of fossil ore at the depth of that period.
- Example from the Tyrannosaurus card: the skull share is 25 / 250 and the pristine share is 4 / 79.
- With those shares, a pristine skull is about 1 of 200 Tyrannosaurus ores.
- The biome percentages on the card are a separate factor. They are not shares.
- A biome percentage sets the quantity of ore of the species in that biome group.
- Example: a desert has much more Tyrannosaurus ore than a mountain, also when no other species competes there.
- A species does not generate in a biome that its card does not list.

**Depth:**

- The default depth for Tyrannosaurus fossils is y 40 to 75. This range joins the card range and the branch range.
- The depth range is a config value. Playtests set the final range.
- The card gives a period rarity of 2.95 for the Cretaceous. That value is a multiplier, as the formula says.
- The branch value of 0.4 is a probability. It does not agree with the formula.

**DNA yield:**

- The working default is the ladder of the Tyrannosaurus card: fragmented 5%, poor 7.5%, common 10%, pristine 30%.
- The yields must stay in the config. Playtests set the final values.
- With these yields and an iron filter, the 50% threshold needs about 25 to 30 fossils.
- With a diamond filter, the 50% threshold needs about 7 fossils.
- The project lead said that these quantities are "about right".

**Player effort:**

- The effort is in three areas:
  - knowledge of the biome and depth for each species
  - processing time and equipment
  - quality luck
- Fossil ore is not rare in the right biome and depth.
- The player hunts fossils during the full game.
- Machine upgrades increase the DNA from each fossil and decrease the processing time.

**Pieces:**

- The special pieces are the skull, the tooth and the claw.
- A special piece gives more DNA than a bone of the same quality.
- A special piece has art that identifies the species. Other bones use generic art.
- A special piece is also a crafting ingredient or a trade item.
- After Beta, the pieces assemble into a skeleton exhibit.

**State of `fossils-1.20.1` against this design.** The analysis read this code on 2026-10-05. It did not run the code.

- `FossilFeature.java:117-119` rolls one quality for each vein. Each block in the vein gets that quality.
- `[inf]` The roll at `:210-215` always selects `fragmented`. That entry is the last entry in the map order.
- The species selection always returns Tyrannosaurus (`:256-262`).
- The placed features have no count modifier and the rarity test always passes.
- As a result, each chunk gets one vein attempt for each host stone.
- The height range is y 40 to 56. The deepslate feature finds no deepslate there `[inf]`.
- `[inf]` About 63% of the vein positions become fossil blocks.
- The config set `biped` does not list `neck`. The neck block never generates.
- The processor rounds the result to a whole percent (`ProcessorBlockEntity.java:100`).
- A fragmented fossil gives 1% with a new iron filter. It gives 0% in the last 20% of the filter life `[inf]`.
- The qualities `fragmented` and `poor` use the same overlay art (`ModBlockStateProvider.java:58-67`).

The branch does not have these items of the design:

- single ores
- the usual maximum for each piece
- the base quality of a cluster
- the Fortune effect and the Silk Touch drop
- species selection and biome weights
- more DNA for special pieces
- the stored biome

In play on the branch, each chunk has fossils and processing time is the only limit. The design also puts effort in the knowledge of where to dig.

### 11.2 Amber, live animals and remains

- Amber does not show its species when it drops.
- The fossil processor shows the species on the DNA tube.
- Each amber has a random DNA yield of 80% or less. Amber has no quality tiers.
- In Beta, the fossil processor processes amber. The drill extractor of the design document is not in Beta.
- An amber keeps the depth and biome where it generated. It has no species until the processor runs.
- The fossil processor selects the species at the end of the run.
- The selection uses the amber weights. Only species that can occur at that depth and biome are candidates.
- Each amber gets its own selection. The project lead did not select one species for each amber deposit.
- A syringe draw from a vanilla animal gives a random 0 to 100% of its genome.
- Vanilla animal DNA is easy to get. This is the intended design.
- A syringe draw gives a small quantity of damage to the animal.
- The dinosaurs of the player supply DNA in three ways:
  - a syringe draw from a live dinosaur
  - remains (fresh bones and tissue) in the fossil processor
  - a sample from an egg
- Fresh material gives much more DNA than a fossil.
- Trace fossils and generic fossils are not in Beta.

### 11.3 Digsites, maps and traders

- A digsite in Beta has these parts:
  - a large fossil deposit
  - paleontologist traders
  - loot chests
- Some digsites are Paleo-Illager sites. Illagers guard these sites and attack the player.
- A Paleo-Illager site is small with few fossils, or large with many fossils.
- A player who kills the illagers gets the fossils, the chests and the machines of the site.
- The project lead did not select two parts of the Paleo-Illagers note for Beta:
  - the item that keeps the paleontologist illager neutral
  - the "ancient plight" effect
- A map leads to a digsite. A map can name a species.
- The paleontologist is a new villager profession with a workstation.
- The paleontologist trades for emeralds. The trades include these items:
  - maps
  - fossils, amber and special pieces, as purchases and as sales
  - tools and parts

### 11.4 Fossil processor

- One run always gives one DNA tube. Extraction does not fail.
- A run also gives by-products: sand, gravel, nuggets and, at a low rate, a diamond.
- At the start of the game, the player feeds one fossil at a time. This is the intended feel.
- The processor accepts items from hoppers and pipes.
- The design has no mixed input queue and no upgrade for parallel runs.
- The result keeps its decimals. A result of 1.25% stays 1.25%, and a run has no minimum.
- The rounding rule is a config value. Playtests set it.

### 11.5 Sequencer and storage

- Each sample adds its percent to the species entry. The maximum is 100%.
- Variants keep their own entries. Parrot colours and cat breeds are examples.
- The drive records the share of each biome in the DNA of a species.
- The data is on the drive item. A drive has no capacity limit.
- The SSD is faster than the hard drive. It has no other difference.
- The DNA server block is not in Beta.

### 11.6 Gene editing

**Thresholds:**

| Action | Rule |
| --- | --- |
| Clone | The base dinosaur has 50% or more |
| Edit | The base dinosaur has 70% or more |
| Gender | The selection is sure at 80% or more. Below 80%, the selection has a 30% chance of a random result |

**Donors:**

- The base dinosaur and the donors total 100%.
- The number of donor slots increases with the sequenced percent.
- The maximum is 3 donor species. The code permits 8.
- The first donor slot opens at 50%, the second at 75% and the third at 100%.
- These steps are config values. Playtests set the final steps.
- More donor DNA gives more risk.

**Gene control:**

- The donor mix sets the genes by default.
- Isolation gives direct control of one gene. Isolation needs 100% of each donor animal that has the gene.
- Colour editing needs each tropical fish colour but black. That is 15 colours.
- The player also selects one option in each gene category, as the design document says.
- The working direction for the Edit tab is a Basic view and an Advanced view.
- The isolated gene controls and the colour mode for each layer are in the Advanced view.
- The type of control depends on the gene. A gene with a number, such as speed or size, has a slider.
- A gene with a choice, such as gender, skin or a special gene, has a list.
- The Edit tab starts from the layout of the 2022 video (section 7.2).
- That layout has the entity preview with a player for scale, the donor sliders, the animal list and the effect text.
- Before synthesis, the screen shows the risk of the genome to the player.
- The risk on the screen includes the chance of incubation failure and the stat decreases.

**Risk.** A genome with many donors or many edits has these risks, all in Beta:

- Incubation can fail.
- An increase of one stat decreases a different stat.
- The animal can have abnormal behaviour.

Disease is not in Beta. The project lead moved the disease risk and the disease system to a later release.

**Variation.** Each clone is different, also with no edits. These properties vary:

- stats
- size, with gender weights as on the Tyrannosaurus card
- the colour of each layer
- rare morphs (albino and melanistic) at about 5%

The variation rolls when the egg hatches, not at synthesis. The project lead decided this on 2026-10-06. As a result, hatching shows the result to the player for the first time.

**Gene effects.** Each gene group must change the animal in Beta:

- core stats: health, health regen, strength, defense, speed, size
- needs: stomach capacity, eat rate, thirst, energy
- behaviour: aggression, intelligence, herd size, pack size, tamability, nocturnal
- breeding and survival: fertility, egg clutch, gestation time, immunity, underwater capacity, heat resistance, jump

The gene registry does not have thirst, energy, egg clutch or gestation time (section 8.1).

The immunity gene stays in Beta. The design document says that immunity is resistance to disease and poison. Poison is in Beta, so the gene can change poison resistance.

**Gene data:**

- The table "Vanilla Animal DNA Effects" is the source for the gene values (section 7.5).
- Where the code and the table differ, the table value replaces the code value.
- Frog, camel, sniffer, armadillo, nautilus and the wolf variants get entries after the port (section 11.17).
- Villager DNA gives each gene a random value each time a player uses it. This effect is in Beta.

**Appearance:**

- Colour editing has two modes: a mode with a primary and a secondary colour, and a mode for each layer.
- Each layer has controls for saturation, opacity and tint strength.
- The claws, teeth, nostrils, eyes and mouth are not editable.
- The biome that supplied most of the sequenced fossils sets the skin variant.
- At 100%, the player selects any variant.
- Male and female animals have different textures.
- Beta has more than one variant for some species.

**Special genes and extras:**

- All seven special genes are in Beta (section 7.6).
- Some donor animals do not exist in the game.
- A command or creative mode gives the genes of those animals until the animals exist.
- The vanilla frog is the donor for the gender swapping gene in Beta. The reed frog of the mod comes later.
- Diet genes, quirks and donor palette shifts are in Beta.
- Hybrids are not in Beta.

### 11.7 Synthesis, egg printer, incubator and hatch

- The bird embryo joins the DNA in synthesis.
- Synthesis takes a syringe with a bird embryo. It gives a syringe with a dinosaur embryo.
- The egg printer takes that syringe and bone meal. It gives the unincubated egg.
- The design removes the crafting recipe for the unincubated egg.
- The incubator holds 3, 6 or 9 eggs.
- The incubator needs power, a bulb and plant matter. Incubation stops if one of them is absent.
- The player puts each egg on the bed. The arm turns the eggs.
- An egg with no heat or no plant matter for too long dies.
- The player puts the incubated egg on the ground. It hatches there.
- Eggs shake before they hatch.
- A hatch success rate applies at the end. It is separate from incubation failure.
- The tooltip of an egg shows the species, the donor mix, the edits of the player and the chance of incubation failure.
- The tooltip also shows the selected gender. Below 80%, the tooltip says that the gender can be random.
- The tooltip does not show the rolled variation. The project lead decided this on 2026-10-06.
- The machine time for one dinosaur is about 50 minutes at the base tier.
- The project lead said that this time is "about right".
- Incubation time is different for each species. Playtests set the final times.

### 11.8 Machine framework

- The player builds each of the four machines from crafted parts in a set sequence.
- An item that places a fully assembled machine is for creative mode only.
- Upgrades change the look of the machine. Dye changes the colour of the sequencer.
- A faster chip uses more power.
- A machine with no power stops. A machine that does no work uses no power.
- The project lead did not select a limit on the number of upgrades.
- A player changes between linked machines with tabs and stays in the screen.
- The mod has its own power conduit. Energy cables of other mods must also work.
- Each machine, part and upgrade has a recipe from vanilla materials.
- No part comes only from loot or trade.

### 11.9 Dinosaurs

- The Beta species are Tyrannosaurus, Velociraptor, Brachiosaurus, Triceratops, Dilophosaurus and Gallimimus.
- Pteranodon is not in Beta.
- Dinosaurs do not spawn in the world. Cloning is the only source.
- Growth is slow. A Tyrannosaurus is full size after about 45 in-game days.
- Size increases smoothly, not in steps.
- The hitbox and the stats increase with the animal.
- Young animals follow a parent, cannot hunt and need care.
- A carcass stays for a time and then decays.
- A dead dinosaur drops meat and bones.
- The special drops are the raptor toe claw, the Triceratops horn and teeth.
- A carcass gives a DNA sample.
- The project lead approved the numbers on the Tyrannosaurus card (section 7.8).
- The project board has cards for the other species. Four cards have no text, and the Velociraptor card has no stats (section 8.6).
- The interim values of section 11.16 become the working values in the game data until each card arrives.
- Section 11.16 has interim values for the five other species. They are a proposal in this document only.
- Playtests set the final values.

**Hitboxes (decided 2026-10-09 and 2026-10-10).** The full design is in [dinosaur-hitboxes-and-movement-design.md](dinosaur-hitboxes-and-movement-design.md). It is the spec for Beta modules B26 and B27.

- The movement box covers the render-scaled torso and legs. It grows smoothly through the 26.2 `scale` attribute.
- Hit parts cover the visible body. Each part has a square footprint and sits on a per-species spine:
  - one head part
  - a chain of neck parts
  - two torso parts
  - one part per leg
  - a chain of tail parts
- A Gallimimus has 12 parts. The proposed layouts for all six species are in the design and in [dinosaur-hitbox-layouts.html](dinosaur-hitbox-layouts.html). The project lead reviewed the Gallimimus layout only.
- The parent box takes no hits. No species has arm parts, and the Dilophosaurus frill has no part.
- Damage multipliers: head ×1.5, neck ×1.25, torso ×1.0, legs ×1.0, tail ×0.5.
- Parts never block movement.
- An explosion near a part hurts the animal. A sweep or a splash potion reaches the animal when any part is in range.
- Parts follow the real animations. A build step bakes the animation files into tables inside the mod jar. A synced pose state tells both sides which animation plays.
- The neck and the tail bend away from blocks (server-side IK). The feet sit on the ground (client-side IK).
- Eating and drinking count only when the head touches the source. The animal must be able to reach it.


### 11.10 Needs and behaviour

**Needs in Beta:** hunger, thirst, stamina and sleep, the social need, and stress.

**Stress.** In Beta, two inputs set stress: food and water, and company. Enclosure space and block comfort are not inputs in Beta. A hunt of live bait also decreases the stress of a carnivore (section 11.12).

**Results of a need that the park does not supply:**

- The animal attacks fences to escape.
- The animal becomes aggressive.
- The animal loses health and dies.
- After an animal satisfies a need, a cooldown starts. During the cooldown, the animal does not look for that need again.
- When a carnivore kills a player, its hunger decreases.
- At zero hunger, the animal loses a fixed quantity of health at an interval, as a starving player does. The default is 1 health each 4 seconds.
- As a result, a large animal lives longer than a small animal. With the values of section 11.16, a Velociraptor lives about 2.3 minutes and a Tyrannosaurus about 10 minutes.
- The damage and the interval are config values. Playtests set them. The project lead decided this rule on 2026-10-06.

**Carnivores:**

- Each species ranks prey by clade, as on the Tyrannosaurus card.
- Players and villagers are prey.
- Carnivores eat carcasses and dropped meat.
- Velociraptors hunt in packs.
- A chase stops when the stamina of the predator is zero. A cooldown follows before the next hunt. As a result, prey and players can escape a tired predator.

**Herbivores:**

- Herbivores eat grass, leaves and plants as blocks. Eating removes the block.
- Reach is important. Brachiosaurus eats high leaves and Triceratops eats low plants.
- A herbivore feeder exists.
- Each species has its own diet list. The list accepts plants and meat of other mods.

**Groups:**

- Animals of one species form herds and packs.
- A group has a leader. Animals compete for rank.
- Animals call to warn, to collect the group or to threaten.
- Turf wars are not in Beta.
- A dominance value selects the leader. Size, age and the aggression gene set the value.
- In a contest for rank, the animals make threats, as `ThreatDisplay` does. The contest does no damage.
- `StartTurfWar` and `LargeCarnivoreFight` are code for a contest between two animals. No brain uses that code.

**Herd travel and large-body movement (decided 2026-10-09 and 2026-10-10).** The full design is in [dinosaur-hitboxes-and-movement-design.md](dinosaur-hitboxes-and-movement-design.md). The herd code specs are in `docs/superpowers/`.

- A wide animal reaches each path node and does not spin.
- In tight ground, an animal turns in place before it walks. In open ground, it slows while it turns.
- Herd mates keep space along the whole body, from snout to tail tip.
- The herd travels as a wedge. In a corridor, it walks in single file.
- In single file, a follower's chest stays behind the tail tip ahead. In a corridor, and for a herd that would outgrow its trail, the herd uses the tighter spacing of today's code. Each species has config values for this.
- The leader's trail grows with the herd. An animal past the end of the trail follows the animal ahead of it.
- A stuck animal tries cheap fixes first. The last fix moves it to a clear spot nearby, also when a player is near.
- Leaves:
  - Large species (Brachiosaurus, Triceratops and adult Tyrannosaurus) break leaves with the torso, above a growth stage set per species.
  - The other species pass through leaves.
  - Player-placed leaves break too.
- Large species break logs only when they charge.
- With `mobGriefing` off, every species passes through leaves, and logs stay solid.
- A tag lists the blocks that dinosaurs can break.
- An adult Brachiosaurus stays in open ground.


**Breeding:**

- A male and a female mate. The female makes a nest and lays eggs.
- Each species has a clutch size and a natural hatch rate.
- Adults guard nests and young animals.
- False eggs are not in Beta.
- A pair breeds only when these three conditions are true:
  - a nest is near
  - the fertility roll passes
  - the stress of each animal is below the threshold of its species
- A young animal gets the middle value of its parents for each number gene, plus the variation of a clone.
- Each colour layer, the skin, the special gene and the morph come from one parent. The program selects the parent at random.
- The bred genes stay inside the edit limits. The project lead decided these breeding rules on 2026-10-06.
- Animals avoid water that is deeper than their height. Babies and small species walk around deep water, and large species go through it.

**The player:**

- An animal gives a warning before it attacks. A roar and a frill are examples.
- Each species runs or stands. Gallimimus runs and Triceratops attacks.
- A carnivore attacks a player in its territory. An adult attacks a player near its young.
- A player can tame an animal that is calm and has low stress.
- A tame animal does not attack its owner. It follows, stays and defends the owner.
- Hunger or stress can end the bond.

**Detection:**

- Animals detect by sight and sound.
- Creatures leave scent points that fade. Predators follow the points.
- Distance and activity set a 25 to 85% chance that an animal detects a player.
- Vision that needs movement is not in Beta.

### 11.11 Power and fences

**Power:**

- Beta has a basic generator and a large generator.
- Pylons and lines carry power across long distances.
- A generator burns each furnace fuel.
- Backup generators are not in Beta.
- One basic generator runs the four machines at the base tier and a first paddock of about 10 fence posts.
- In a large park, the fence lines use more power than the machines. That is the reason for the large generator.
- The values in the code do not agree with this target (section 4.5).
- The basic generator burns fuel at the furnace rate. One piece of coal lasts 80 seconds.
- The large generator makes fuel last longer.
- The burn times are config values. Playtests set the final values.
- The project lead did not select a rule that burns fuel only when a machine or a fence uses power.

**Containment:**

- A live wire keeps animals away. Animals test a fence that has no power.
- A starved animal attacks a live fence also. An animal with high stress does the same.
- Intelligent animals notice when a fence has no power.
- Each electric shock can stop the escape attempt of a stressed animal. The chance is a config value. A starved animal does not stop. The project lead decided this on 2026-10-06.
- The design has no rule that matches a fence type to an animal size. Wire health gives that effect (see "Damage and access").

**Fence types:**

- A high-security fence is stronger than a low-security fence. It also gives more damage.
- Height stops tall animals and animals that jump.
- The overhanging fence is not in Beta.

**Damage and access:**

- Wires break one at a time. The wire spool restores a broken wire.
- A large animal can make a post fall.
- Each wire has health. A hit decreases that health by the attack damage of the animal.
- As a result, a strong animal breaks a wire faster than a weak animal.
- A high-security wire has more health than a low-security wire.
- A high-security post is more difficult to make fall than a low-security post.
- Wire health and post strength are config values. Playtests set them.
- A live wire also hurts players.
- A switch or a redstone signal stops the power to a fence section.
- A player can fill a feeder and stay out of the paddock.
- Gates are not in Beta.
- Beta has the three voltage signs for fences. Their art is in the asset repository (section 8.5).

### 11.12 Feeders, tablet, plants, vehicles and guns

- Beta has a carnivore feeder and a herbivore feeder. This answers the question about the feeder on `1.20.1-ai`.
- A player fills a feeder by hand. Hoppers and pipes also fill it.
- Each feeder has a screen.
- A live bait feeder is also in Beta. It uses vanilla animals as bait.
- The player brings a vanilla animal to the live bait feeder. The feeder holds the animal.
- The feeder releases the animal when a carnivore is hungry. The carnivore hunts the animal.
- A hunt of live bait decreases the stress of the carnivore.
- The "Dino Feed" item of the design document is not in Beta.
- The tablet is not in Beta.
- Plants are in Release 1. In Beta, herbivores eat vanilla plants.
- Two kinds of vehicle are in Beta: ground vehicles that the player drives, and tour vehicles.
- The Beta vehicles are the Jeep Wrangler and the 1992 Ford Explorer.
- The player drives the Jeep Wrangler. The Ford Explorer is the tour vehicle.
- The asset repository has models for the two vehicles (section 8.5).
- The project lead did not select the gyrosphere, the monorail, aircraft or boats.
- The Ford Explorer follows a track that the player puts in the road.
- The player can also drive the Ford Explorer by hand, off the track.
- The player sets stops on the track. The Ford Explorer stops at each one.
- The Ford Explorer can do the tour with no driver.
- Some guns are in Beta. The kinds of gun are not decided (section 11.14).

### 11.13 Not in Beta

- Pteranodon
- plants and wood sets
- the tablet and the DNA server
- hybrids and false eggs
- gates, the overhanging fence and backup generators
- trace fossils, generic fossils and terracotta host stone
- the drill extractor
- turf wars and vision that needs movement
- the "Dino Feed" item
- animals that are not dinosaurs
- aircraft and boats
- the enclosure marker and the park signs
- diseases and their treatment

### 11.14 Decisions that stay open

- **Stat cards.** The project board has no stats for five species (section 8.6). The interim values of section 11.16 are the working values.
- **Guns.** Some guns are in Beta. The project lead answered "not decided" on the kinds of gun.
  - The design document has three kinds: tranquilizer, stun and capture, and lethal.
  - The asset repository has models for 11 guns. It has ammo sprites for a dart, a taser cartridge and a shotgun shell.
  - On a second question with that inventory, the project lead answered "not decided" again.
- **Animation details.** On 2026-10-06 the project lead answered "not decided" for two items. The plan does not build them (decision D14).
  - animation speed that follows body size
  - look-around clips on the second animation controller
- **Neck reach IK.** It waits until the eating and drinking animations exist. The Gallimimus has neither ([dinosaur-hitboxes-and-movement-design.md](dinosaur-hitboxes-and-movement-design.md)).

### 11.15 Systems in Beta that have no code

- digsites, the paleontologist profession, Paleo-Illager sites and maps
- amber worldgen, remains, carcasses and drops
- processor by-products and hopper support
- assembly from parts for the processor, the sequencer and the egg printer
- the power conduit, pylons, the large generator and machine tabs
- recipes for machines, parts and upgrades
- gene effects, variation for each animal, edit risk, skin variants and special genes
- herds, packs, pack hunting, grazing, breeding and taming
- scent trails, detection levels and stress
- the herbivore feeder and the fence switch
- vehicles, the tour track and guns

This scope is larger than the code on each branch.

### 11.16 Interim stats for five species

The analysis drafted these values on 2026-10-05. The project lead first asked for a draft in this document only. Later on that date, the project lead selected the values as the working values in the game data. Each card replaces its column when the card arrives.

The project lead also asked for a draft of the stats that this table does not have.

| Stat | Velociraptor | Dilophosaurus | Gallimimus | Triceratops | Brachiosaurus | Tyrannosaurus card |
| --- | --- | --- | --- | --- | --- | --- |
| Health | 35 | 40 | 40 | 140 | 300 | 155 |
| Damage | 8 | 6 | 3 | 13 | 12 | 16 |
| Defense | 4 | 3 | 2 | 14 | 10 | 12 |
| Speed | 0.65 | 0.55 | 0.75 | 0.35 | 0.25 | 0.50 |
| Energy | 149 | 135 | 203 | 162 | 244 | 203 |
| Group size | 3 to 6 | 1 to 3 | 6 to 12 | 4 to 6 | 3 to 5 | 1 to 2 |

Basis of the values:

- The analysis set health, damage, defense and speed by comparison with the Tyrannosaurus card. No formula gives them.
- Health and defense follow body size. Triceratops has the highest defense because of its frill and horns.
- Damage follows body size and the aggressiveness rating of the design document (section 7.3).
- Speed is higher for small species. Gallimimus is the fastest.
- Energy is 203 x the metabolism rating / 7.5. The Tyrannosaurus card has 203 energy, and its metabolism rating is 7 to 8.
- Group size follows the group rating of the design document.
- Issue #6 gives a size class for three of the species: Velociraptor is medium, and Triceratops and Tyrannosaurus are large.

**Stats that the first table does not have.** The project lead asked for research and for growth time from body mass. The project lead also said that real values do not always give good gameplay. The values below are a draft for review. The project lead did not approve them.

Research data, from the Wikipedia articles of the species, read on 2026-10-05:

| Species | Body mass | Height | Growth data | Egg data |
| --- | --- | --- | --- | --- |
| Tyrannosaurus | 5.4 to 8.0 t for most modern estimates | 3.7 to 4 m at the hip | Fast growth from about 14 years. Maturity at 16 to 20 years. Oldest specimens about 30 years | One 2026 study gives 21 to 32 eggs |
| Triceratops | 6 to 10 t | 2.3 to 3.0 m tall | None in the article | None |
| Brachiosaurus | 28.3 to 46.9 t | 9.4 m, or 12 to 13 m | None | None |
| Velociraptor | 14 to 20 kg. The film animal is about 90 kg | 0.5 m at the hip. The film animal is about 1.7 m tall | None | None |
| Dilophosaurus | About 400 kg | 1.5 to 1.75 m at the hip | 30 to 35 kg for each year when young | None |
| Gallimimus | 400 to 490 kg | 1.9 m at the hip | None | None |

Limits of the research:

- The articles have growth data for two of the six species only.
- The articles have egg data for Tyrannosaurus only.
- The analysis found no source for health regen, hunger or thirst. The values for those three stats are arithmetic, not research.

Franchise data. The project lead asked for a range from the Jurassic World Evolution games and the film data. The values are from the paleo.gg databases for the two games, read on 2026-10-05:

| Species | Height in m, game 1 / game 2 | Mass in kg, game 1 / game 2 | Social group, game 1 / game 2 | Attack, game 1 / game 2 | Defence, game 1 / game 2 | Incubation, game 2 |
| --- | --- | --- | --- | --- | --- | --- |
| Tyrannosaurus | 5 / 5.2 | 18,000 / 8,400 | 1 / 1 to 2 | 109 / 109 | 30 / 30 | 10 min |
| Velociraptor | 2 / 1.7 | 150 / 226 | 2 to 6 / 2 to 3 | 60 / 45 | 16 / 18 | 3 min |
| Brachiosaurus | 6 / 12.4 | 60,000 / 56,000 | 1 to 5 / 1 to 2 | 39 / 77 | 0 / 90 | 5 min 15 s |
| Triceratops | 3 / 3.6 | 12,000 / 12,000 | 1 to 6 / 2 to 3 | 60 / 76 | 30 / 38 | 3 min 45 s |
| Dilophosaurus | 2 / 1.6 | 400 / 300 | 1 to 12 / 3 to 4 | 47 / 35 | 15 / 16 | 1 min |
| Gallimimus | 2 / 3 | 400 / 450 | 1 to 18 / 6 to 7 | 6 / 8 | 0 / 0 | 1 min 30 s |

- Game 2 gives an egg clutch for two species: Dilophosaurus 3 to 8 and Gallimimus 6 to 10.
- The analysis could not read Jurassic-Pedia or the wikis of the games. The sites refused the requests of the analysis.

Game 3. A first version of this section said that no database exists for the third game. That statement was wrong, because the analysis did not look. The paleo.gg database for game 3 has the same height, mass, attack, defence and social group as game 2. It adds these values:

| Species | Eggs for each batch | Appetite | Sedative resistance | Poison resistance |
| --- | --- | --- | --- | --- |
| Tyrannosaurus | 1 to 2 | 150 | 120 | 150 |
| Velociraptor | 2 to 5 | 50 | 60 | 155 |
| Brachiosaurus | 1 to 2 | 300 | 180 | 250 |
| Triceratops | 1 to 4 | 125 | 120 | 100 |
| Dilophosaurus | 3 to 6 | 50 | 60 | 155 |
| Gallimimus | 4 to 6 | 125 | 60 | 82 |

- The game 3 pages give no growth time for a juvenile.

Film data:

| Animal | Height | Length | Mass | Source |
| --- | --- | --- | --- | --- |
| Tyrannosaurus, 1993 animatronic | 6.1 m | 12 m | 4,100 kg | Wikipedia article "Rexy" |
| Velociraptor "Blue" | 1.7 m | 3.9 m | | Size chart of the 2018 film, from a search result |
| Brachiosaurus | 12.4 m | 21.5 m | | The same chart |
| Triceratops | 3.6 m | 8.9 m | | The same chart |
| Gallimimus | 3.0 m | 4.7 m | | The same chart |
| Dilophosaurus, 1993 animatronic | 1.2 m | 3 m | | A search result |

- The analysis did not read the size chart itself. The values are from a search result that quotes a fan page.
- The heights and lengths of games 2 and 3 are the same as the values of that chart.
- Other film data: the Wikipedia articles give about 1.7 m and 90 kg for the film Velociraptor.

Jurassic-Pedia. The project lead named this site as a franchise source. The analysis read the six species pages on 2026-10-05, in the browser of the project lead:

| Species | Height | Length | Mass | Other data |
| --- | --- | --- | --- | --- |
| Tyrannosaurus | 3.5 m at the hip, 5.2 m at the head. One animal of 5.5 m | 12.3 to 13 m. One animal of 13.5 m | 7 to 12 tons | |
| Velociraptor | 1.7 to 1.8 m with the head up | 2.4 to 4.6 m, 3.4 m on average | 68 to 226 kg | Nests of 10 to 13 eggs. One group laid 8 eggs |
| Brachiosaurus | 12.4 to 15.85 m | 21.5 m and more | 25,401 to 56,245 kg | |
| Triceratops | 2.7 to 3.6 m. A maximum of 4.5 m | 6.1 to 9.1 m. A maximum of 10.5 m | 5,443 to 9,979 kg | |
| Dilophosaurus | 1.8 m at the hip | 6 m | 454 kg | Many animals stay at 2.7 m long and 1.5 m tall |
| Gallimimus | 1.9 m at the hip, 3 m in total | 4.7 to 7.9 m | 204 to 454 kg, 440 kg on average | Speed of 48 to 56 km/h |

- The site quotes the lifespans of game 3. It gives no growth time.

Dilophosaurus. The project lead gave this instruction: the size data for Dilophosaurus is the real scientific data, and the film size does not apply.

- Real data from the Wikipedia article: about 7 m long and about 400 kg for the largest specimen.
- The first specimen was about 6 m long, 283 kg and 1.36 m at the hip.
- The article gives a hip height of 1.50 to 1.75 m for the species.

Draft values:

| Stat | Velociraptor | Dilophosaurus | Gallimimus | Triceratops | Brachiosaurus | Tyrannosaurus card | Basis |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Growth time, in days of the game | 29 | 31 | 31 | 47 | 57 | 45 | Body mass, with a compressed range |
| Height range, in blocks | 1.7 to 1.8 | About 3 | 3 | 2.7 to 4.5 | 12.4 to 15.85 | 5.2 to 6.1 | Franchise sources, 1 metre for 1 block. Dilophosaurus: real data |
| Mass range, in kg | 68 to 226 | 283 to 400 | 204 to 454 | 5,443 to 12,000 | 25,401 to 56,245 | 4,100 to about 11,000 | Franchise sources. Dilophosaurus: real data |
| Group size range | 2 to 3 | 3 to 4 | 6 to 7 | 2 to 3 | 1 to 2 | 1 to 2 | Games 2 and 3 |
| Appetite | 50 | 50 | 125 | 125 | 300 | 150 | Game 3 |
| Health regen, in ticks for each half heart | 66 | 58 | 58 | 17 | 8 | 15 | Arithmetic |
| Hunger | 27 | 27 | 67 | 67 | 160 | 80 | Appetite of game 3, on the scale of the card |
| Thirst | 23 | 23 | 58 | 58 | 140 | 70 | The same ratios as hunger |
| Egg clutch | 2 to 13 | 3 to 8 | 4 to 10 | 1 to 4 | 1 to 2 | 12 to 24 on the card, 1 to 2 in game 3 | Games 2 and 3, and Jurassic-Pedia |
| Hatch success | No data | No data | No data | No data | No data | 5% | No research data |

Rule for each stat:

- The project lead gave the source sequence: numbers from the Jurassic Park franchise are first, and real scientific data fills the gaps.
- The analysis could not read the franchise wiki on 2026-10-05, because the site refused the request. The only franchise numbers in this draft are the film Velociraptor of about 1.7 m and 90 kg.
- Growth time: 45 days x the eighth root of (body mass / 8.4 t). The Tyrannosaurus card gives 45 days, and 8.4 t is its mass in game 2.
- The fourth root is a general rule of biology for growth time against body mass. The analysis used it from general knowledge and did not read a source for it.
- The fourth root gives 15 to 69 days. The project lead asked for a compressed range, so the draft uses the eighth root, which keeps the sequence of the species.
- Growth time is a config value for each species. Playtests set the final values.
- The masses in the rule are the masses of game 2. For Dilophosaurus the rule uses the real mass of 400 kg.
- The franchise sources are the films, the three games and Jurassic-Pedia.
- The project lead gave this rule: where game 1 does not agree with all the other sources, the draft ignores game 1.
- That rule removed these game 1 values:
  - the heights of Velociraptor, Gallimimus, Brachiosaurus and Tyrannosaurus
  - the lengths of Gallimimus, Brachiosaurus and Tyrannosaurus
  - the masses of Brachiosaurus and Tyrannosaurus
  - the social group of each species
- The project lead gave this rule: the draft ignores length, because height sets the scale of a model. The source tables above keep the lengths that the sources give.
- The project lead gave this rule: a height is the height at the highest point of the body. A hip height is not a height for this document.
- Each value in the height row is a height at the highest point.
- For Dilophosaurus, the scientific sources that the analysis read give hip heights only. The one value for the full height is about 3 m, from the Wikipedia article.
- That value is weak. The article calls it the "assumed true height" in its section about the film.
- The project lead selected about 3 blocks as the working height for Dilophosaurus. Playtests set the final value.
- Height range, mass range and group size range: the lowest value and the highest value of the franchise sources.
- The franchise ranges replace the real values of the first draft. The research table above keeps the real values.
- Appetite: game 3 has a value for each species. It is the franchise source for the hunger row.
- Egg clutch: the Tyrannosaurus card gives 12 to 24 and game 3 gives 1 to 2. The two sources do not agree.
- The project lead selected the model of the card for each species: a large clutch with a low hatch rate.
- The Tyrannosaurus card values stay: a clutch of 12 to 24 and a hatch success of 5%.
- Clutch size and hatch success are config values for each species. Playtests set the final values.
- The game values in the egg clutch row are small clutches. They do not agree with the model of the card.
- No source gives a large clutch or a hatch rate for the other five species. Those values are not set.
- Health regen: each species gets full health in the same time as Tyrannosaurus. That time is about 116 seconds.
- Hunger: the appetite of game 3, on the scale of the card. The Tyrannosaurus keeps the card value of 80, and its appetite in game 3 is 150.
- Thirst: the same ratios as hunger, from the card value of 70. No source gives thirst.
- Hunger and thirst are config values for each species. Playtests set the final values.

Points for gameplay:

- With the mass rule, Gallimimus grows faster than Triceratops.
- Triceratops takes 47 days, two more than Tyrannosaurus, because its mass in game 2 is larger.
- Brachiosaurus takes 57 days. That is about 19 hours of play.
- The Tyrannosaurus card has a group size of 1 to 2 and a height of 3.78 to 6 blocks. The two games agree with the group size.
- The first interim table has a Brachiosaurus defense of 10, below Triceratops at 14. Game 2 has the opposite sequence: 90 against 38.
- The real Velociraptor of 17 kg has a height of 0.5 blocks. The film animal is the basis of the model.

**Model heights.** The analysis measured each geometry file with the bone rotations applied. A first measure on the same date did not apply the rotations, and its values were wrong.

| Species | Model height, in blocks | Render scale at commit 4244f62 | Height in the game at that commit | Real animal |
| --- | --- | --- | --- | --- |
| Tyrannosaurus | 4.0 | 1.0 | 4.0 | 3.7 to 4 m at the hip |
| Velociraptor | 3.5 | 0.6 | 2.1 | 0.5 m at the hip. The film animal is about 1.7 m tall |
| Dilophosaurus | 4.3 | 0.5 | 2.1 | 1.5 to 1.75 m at the hip |
| Gallimimus | 3.3 | 1.0 | 3.3 | 1.9 m at the hip |
| Triceratops | 2.3 | 2.0 | 4.6 | 2.3 to 3.0 m tall |
| Brachiosaurus | 7.8 | 2.5 | 19.4 | 9.4 m, or 12 to 13 m |

- The real sizes are from the Wikipedia articles of the species, read on 2026-10-05.
- The model height includes the highest part of the model. For Velociraptor that part is the quills, and for Dilophosaurus it is the frill.

**Render scale.** The project lead gave this rule: 1 metre is 1 block. The real height sets the base height of the model.

- Triceratops: the target base height is 3.0 blocks. That height needs a scale of 1.32. The code has 2.0 (`TriceratopsRenderer.java:22`).
- Brachiosaurus: the target base height is 12.5 blocks. That height needs a scale of 1.61. The code has 2.5 (`BrachiosaurusRenderer.java:22`).
- Wikipedia has two height estimates for Brachiosaurus. The analysis used the larger estimate because the model has a raised neck.
- These values are targets. The code does not have them.
- The other four species are near the real size or the film size.

**Game data.** The six species config files do not have the working values. The files are in `R/data/projectnublar/config/dinosaurs/`, and they have the values of commit 4244f62.

- The project lead gave this rule on 2026-10-05: the analysis does not edit code without explicit approval.
- The analysis made changes to the six config files and to two renderers on that date. The project lead told the analysis to revert them, and no change stays.
- The table below maps the working values to the config fields for the person who makes the change.

| Stat of the first table | Config field | Note |
| --- | --- | --- |
| Health | `max_health` | |
| Energy | `max_stamina` | |
| Damage | `attack_damage` | |
| Defense | `resistance` | Sets the armor attribute (`DinoNeedsUtils.java:212`) |
| Group size | `group_size` | One number. No code reads it |
| Speed | `speed_multiplier` | A comment disables the speed line (`DinoNeedsUtils.java:210`). The unit is different from the card |

- The Tyrannosaurus file has placeholder values also. Its card gives 155, 203, 16, 12 and a group size of 1 to 2.

### 11.17 Config, platform and repository

**Config format:**

- A TOML settings file in the config folder holds the global balance numbers. Issue #1 shows this format.
- Examples of those numbers are yields, times, chances and power values.
- Datapack JSON holds the data for each species: stats, fossil sets, diets and gene effects.
- Tags let other mods add items to the diet lists.
- Where the split is not clear, the developers select the simpler format.

**Loaders and game version:**

- The mod moves to a newer Minecraft version. The project lead gave the rule for the version.
- The rule: the latest version for which Fabric, Forge and NeoForge have current support.
- The mod ships on Fabric and NeoForge. NeoForge replaces Forge on the new version.
- Fabric and NeoForge must be playable at the Beta release.
- As a result, a feature is complete only when the parts for each loader exist and a test in each loader passes.
- The port occurs before the new Beta systems. The sequence is:
  1. Integrate the three branches (section 2.6).
  2. Port to the new version.
  3. Build the new Beta systems.
- The port target is Minecraft 26.2. The project lead selected it from the candidates below.

**Version candidates.** The analysis read the release data of the game, the loaders and the libraries on 2026-10-05:

| Version | Release date | Fabric | NeoForge | Forge |
| --- | --- | --- | --- | --- |
| 26.3 | 2026-09-15 | Stable | Beta builds only | No recommended build |
| 26.2 | 2026-06-16 | Stable | Stable | A recommended build |
| 26.1.2 | 2026-04-09 | Stable | Stable | A recommended build |

- If beta builds are current support, the rule selects 26.3. If they are not, the rule selects 26.2.
- The project lead selected 26.2.
- Five of the six libraries have releases for Fabric and NeoForge on these three versions.
- The five libraries are GeckoLib, SmartBrainLib, Architectury, Common Networking and Forge Config API Port.
- On 26.2, GeckoLib is at version 5 and SmartBrainLib is at version 2. Both are major upgrades from the 1.20.1 versions (4.7.3 and 1.15).
- GeckoLib 5 moves each class that the mod imports to a new package (31 files). Five of those classes do not exist in GeckoLib 5.
- SmartBrainLib 2 does not have `BrainUtils` (22 files use it), `BrainActivityGroup` or `SBLLoader`. On 2026-10-06 the project lead put both upgrades into phase P2 of the plan.
- Botarium has no release after Minecraft 1.20.4. Its newest release is from 2024-06-29.
- The mod uses Botarium for energy and fluids in 8 files (section 3.1). The port must replace it.
- On 2026-10-06 the project lead selected Balm as the replacement, for energy and fluid storage only. Balm has builds for Fabric and NeoForge on 26.2. Architectury, Common Networking and Forge Config API Port stay for the other functions.
- Common Storage Lib is the successor of Botarium from the same team. On 2026-10-06 it had builds for 1.21 and 1.21.1 only. The port plan compares it with Balm again on the day the port starts.
- The project lead gave this rule: do not write an internal solution when a library gives the same function.
- On 2026-10-10 the project lead confirmed the rule for the libraries that the mod already depends on:
  - Where SmartBrainLib 2, GeckoLib 5 or Balm gives a hook for a need, the mod calls the library through its API.
  - The mod does not copy library code. It does not write a second version of a library feature.
  - Examples: SmartBrainLib 2 ticks the brain, `onlyScanIf` gates sensors, and the built-in sensors and behaviours cover nearby entities, attack targets, flee, follow and breeding. GeckoLib 5 gives triggered animations, additive controllers, bone updaters and scaling from the scale attribute.
  - The mod has its own code only where no hook exists, for example stamina chases, scent trails, dominance, nests and herd formation. That code uses the base classes of the library.
  - Where a library does not have a feature, for example animation LOD in GeckoLib 5, the mod uses the extension points of the library. The mod does not fork the library.
  - The project plan has this rule in its global constraints. The source is `reports/Reference mod survey for Beta.md`.
- These versions need Java 25. The mod uses Java 17 at this time.
- The MultiLoader Template has a branch for each of these versions. Its modules are `common`, `fabric` and `neoforge`.
- From version 26.1, the game code has no obfuscation. Parchment mappings are not available and are not necessary.

**Saved worlds:**

- Only the team has worlds for the rewrite. They are test worlds, and the team can discard them.
- As a result, block ids and item ids can change.
- The two ids with spelling errors can get the right spelling (section 5.3).
- The `rft/adversing` branch does not need to keep the old fossil block ids.

**Licence:**

- The licence is not decided. LGPLv3 and CC0 are the two candidates.
- The project lead decides the licence before the first public release.
- No answer gives the terms for the art.

**Park-system diagrams:**

- The seven diagrams (section 7.7) are background reference. No Beta feature must agree with them.
- The diagrams are also targets for the time after Beta.
- The project lead did not select the tour diagram as the design for the Beta tour routes.

---

## 12. Reference mod surveys

Two surveys read other mods for patterns that Nublar can use. Each survey has a full report in `reports/` and a folder of research notes in `research_notes/`. This section gives the results that matter for the plan. The reports have the evidence, the clean-room specifications and the repository facts.

The surveys use the same labels as the reports:

- `[v]`: The survey checked the item in source at the commit that the notes name.
- `[doc]`: The item comes from a README, a wiki, a changelog or a store page.
- `[inf]`: The item is an inference.

A cost figure without `[v]` is an estimate. Nobody profiled a 26.2 build.

### 12.1 Entity and creature mods (2026-10-10)

Full report: [Reference mod survey for Beta](../reports/Reference%20mod%20survey%20for%20Beta.md). Notes: `research_notes/Reference mod survey for Beta/` (13 files).

The survey rated 135 techniques from creature mods against modules B11 to B27. The main results:

- Nublar's own code causes most of the cost. Three block-scanning sensors cost about 22 ms per tick at 100 dinosaurs, with aligned spikes near 440 ms (estimate). A feeder registry, a remembered water source and gated, jittered SmartBrainLib 2 sensors cut this cost below 0.1 ms.
- SmartBrainLib 2 ticks the brain itself. The manual brain tick must go, or every brain ticks two times.
- Per-tick machine packets, fence line walks and colour layers are the next costs. The colour layers use about 80 % of each frame with 20 Gallimimus. The layer bake fixes it.
- The mods agree on vanilla idioms: commands like a wolf, eggs like a turtle, group retaliation like a bee and a 5-minute breeding cooldown.
- No surveyed creature mod implements twelve Nublar features, for example chance-based detection, scent trails, stamina-limited chases and wire-health fences. Vanilla classes supply most of their parts.
- The survey gave 27 clean-room specifications (S1 to S27) and six open items (O1 to O6).
- On 2026-10-10 the project lead made one rule from the survey. Where SmartBrainLib 2, GeckoLib 5 or Balm gives a hook, the mod uses the hook (section 11.17).

### 12.2 Optimization and technology mods (2026-10-10)

Full report: [Optimization and tech mod survey for Beta](../reports/Optimization%20and%20tech%20mod%20survey%20for%20Beta.md). Notes: `research_notes/Optimization and tech mod survey/` (11 files).

The survey read about 90 repositories in two groups:

- Optimization mods: render, culling, server tick, memory, chunk, network and log mods (Sodium, Lithium, EntityCulling, ImmediatelyFast, FerriteCore, C2ME, Immersive Optimization and others).
- Technology mods: Create and its add-ons, Mekanism, Immersive Engineering, Powah and Refined Storage 2.
- Vehicle, transit and security mods: Automobility, MTR, SecurityCraft, CC: Tweaked and others.
- Content mods: Quark, Tinkers' Construct and Ad Astra.

It rated 167 techniques against modules B00 to B25 and the backlog. It gives 15 clean-room specifications (T1 to T15) and 15 open items (O7 to O21).

#### Main result

The optimization mods make vanilla cheaper. They do not remove the costs of a mod:

- Lithium does not speed up SmartBrainLib 2 brains `[v]`.
- Lithium cannot let a hopper sleep next to a modded container `[v]`.
- The block-entity bakers (EnhancedBlockEntities, BetterBlockEntities) skip modded block entities `[v]`.
- Immersive Optimization skips whole ticks of far entities. Code that counts ticks then runs slow `[v]`.

As a result, Nublar must remove its own costs. Three rules carry most of the gain:

1. Still things use static geometry. Fence posts, wires and idle machines draw from the chunk mesh or a block model, not from a block-entity renderer each frame.
2. One network object runs each power grid. Posts, pylons and conduits do not tick.
3. Work happens only on change. Timers use game time, not tick counts.

#### Top changes

| Rank | Change | Module | In-game effect |
| --- | --- | --- | --- |
| 1 | Fence posts and wire segments as block models in the chunk mesh. A block-entity renderer only for sparks and a breaking wire | B15 | 900 wire block entities cost no per-frame work `[inf]` |
| 2 | One power network object per connected group. Merge on connect. Flood again on break. Energy stays on the block entities | B14, B15 | 60 posts make one energy call per tick, not 60 tick methods |
| 3 | Game time for every deadline. Queues that accept gaps. A distance band inside the brain | B17, B25, B26 | Needs and herds stay correct when a server slows far animals |
| 4 | Exact culling boxes for dinosaurs, machines and wire spans | B09, B12, B15, B16 | A Brachiosaurus neck does not vanish at the screen edge |
| 5 | Named machine issues, fault smoke and a hover box | B12, B14 | The incubator says "No bedding" |
| 6 | Save data and sync data apart. Coalesced sync. Active look off after 60 ticks | B12, B14 | A few small packets per state change |
| 7 | Client-only effects in `aiStep` or server events, not in `tick` | B16, B25 | Off-screen animals still make sounds |
| 8 | Genome and drives as compact `StreamCodec` data. `DinoData` gets `equals` | P2, B06, B16 | A 100-species drive syncs about 2.4 KB `[inf]` |
| 9 | Small block states. Light and path types from the state only | B01, B12, B15 | The fossil set stays near 4,600 states |
| 10 | Tour rules: stop for a tag list, horn after a delay, redstone hold, dwell and announcements | B22 | The Explorer waits for a Triceratops and names each stop |
| 11 | Standard item, energy and fluid capabilities with per-side rules | B12, B14, B20 | CC: Tweaked, Refined Storage, Mekanism and Create work with no compat code |
| 12 | Pooled ambient sound. Particles on events only | B12, B15, B16 | A long fence hums with a few sound instances |

#### 26.2 availability

A compat item can ship only when the other mod has a 26.2 build. The survey checked Modrinth and GitHub on 2026-10-10.

| Group | On 26.2 | Not on 26.2 |
| --- | --- | --- |
| Libraries | Balm 26.2.0.9, GeckoLib 5.5.6, SmartBrainLib 2.0.3, Architectury, Common Network, Forge Config API Port, Team Reborn Energy 5.0.0 | Common Storage Lib (1.21.1), Flywheel (26.1.2), Ponder (26.1) |
| Optimization | Sodium, Iris, Sodium Extra, Lithium, EntityCulling, ImmediatelyFast, MoreCulling, FerriteCore, Immersive Optimization, Particle Core, LambDynamicLights. Fabric only: C2ME, Krypton, VMP, Nvidium | Embeddium, ModernFix (26.1.2), VulkanMod |
| Technology | CC: Tweaked (both loaders), SecurityCraft (NeoForge), Alternate Current, Create Fly (a separate Fabric rewrite of Create) | Official Create, Mekanism files (26.2 branch only), Immersive Engineering, Refined Storage 2 (26.1.2), Powah (26.1.2), Simple Radio, Automobility, MTR, Quark, Tinkers' Construct, Ad Astra |

Results for compat:

- Every optimization duty applies from the first port build.
- An optional CC: Tweaked peripheral can ship with Beta on both loaders.
- Create exists on 26.2 only as Create Fly on Fabric. The Create compat items wait.
- Ponder, Refined Storage 2 and ModernFix need a 26.2 release before a test.

#### Platform first

The lead's rule applies: where the platform or a library that the mod uses covers a need, the mod uses it. Balm stays the bridge for energy and fluid storage only. Architectury, Common Networking and Forge Config API Port stay for the other functions. All three have 26.2 builds.

| Need | What covers it | What Nublar writes |
| --- | --- | --- |
| Energy on machines and generators | Balm. It bridges to the NeoForge 26.2 `EnergyHandler` and, when Team Reborn Energy is present, to Fabric `[v]` | Rules for which blocks give and which accept |
| Fence and pylon network | Nothing `[v]` | The network object (T1) |
| Items in and out of machines | Vanilla `Container` and `WorldlyContainer`, wrapped by both loaders `[v]` | Rules for each face |
| Fluids | Balm `FluidTank`, or the loader fluid APIs | Nothing more |
| Item data | Vanilla data components with codecs | The component records |
| Level and chunk data | `SavedData` and data attachments | The data records |
| Packets | `CustomPacketPayload` with `StreamCodec` through Common Networking | Payloads and server checks |
| Render boxes | NeoForge `getRenderBoundingBox` on the renderer. Fabric has none. Vanilla `shouldRenderOffScreen` is the coarse fallback `[v]` | The box values |
| Static fence geometry | Block models. NeoForge model data and `AddSectionGeometryEvent`. Fabric `emitQuads` with `RenderDataBlockEntity`. Sodium supports all of them `[v]` | One shared `FenceVisual` record and two small loader adapters |
| Tracking range | Vanilla `EntityType.Builder` values `[v]` | Values per species and vehicle |
| Track following | Vanilla `BaseRailBlock`, `RailShape`, `AbstractMinecart.exits` `[v]` | The rail-follow mode of the Explorer |
| GameTests | Vanilla, NeoForge and Fabric test hooks `[v]` | Tests in `common` |

#### Rules for the mod's own code

Server:

- Posts, pylons and conduits have no ticker. Machines return early when idle. Recipe lookups run only on a slot change.
- Timers use game time. `FallbackBudget` and the tick-count timers in `Dinosaur` must change (section 12.2, "Defects").
- Dinosaurs and vehicles do not override `canCollideWith`. Only the tour vehicle overrides `canBeCollidedWith`. Path types come from the block state only.
- Each "is a player near" radius stays at 128 blocks or less.
- Periodic work has an offset per object, so many posts never check in the same tick.

Client:

- Fence geometry has three tiers: a static model, slow state in model data, and effects as particles.
- Idle machines show a static model. The GeckoLib renderer draws only while a machine works.
- All renderers of one texture use one shared render type. The mod has no OpenGL calls, no GLFW calls and no core shader.
- Bioluminescence is an emissive layer, not a moving light source.
- Each wire draws one time, from one end, with 4 to 6 segments.

Memory and network:

- Cosmetic randomness stays out of block states. The fossil look comes from the position seed.
- The fossil set has few blocks with properties, not one block for each host stone, quality and piece.
- Per-level stores use `SavedData` or attachments. No static field holds a world, an entity or a block entity.
- Payloads stay under the vanilla limits: 32,767 bytes from a client and 1 MiB from the server.
- Wire changes travel as small events. Wire health travels in 3 or 4 bands.
- Tracking range follows body size: 8 chunks for small species, 10 for T. rex and Triceratops, 12 for Brachiosaurus (proposal).

Worldgen:

- Lookup maps are immutable and built on the main thread.
- Features use only the `RandomSource` that the generator gives. They place blocks only inside their region and make no drops.
- Digsite maps use the vanilla exploration map function and trade, so any async-locate mod covers them.

#### Compatibility duties

| Mod | The mod must | The mod must not |
| --- | --- | --- |
| Sodium, Iris | Give exact render boxes. Keep model data immutable | Set `shouldRenderOffScreen` on repeated block entities. Call OpenGL. Ship a core shader |
| EntityCulling | Override `getBoundingBoxForCulling`. Put client effects in `aiStep` | Put client-only logic in `tick`. Mark a non-cube block as full and opaque |
| ImmediatelyFast | Share render types. Bake the colour layers | Rely on submit order inside one render type |
| Chloride | Keep game logic out of renderers | Rely on rendering for sounds or effects |
| Lithium | Follow the collision and path-type rules. Call `setChanged` only on change | Give a block a path type that depends on a block entity |
| Immersive Optimization | Use game time. Mark the tour vehicle as always ticking | Opt out the dinosaurs |
| FerriteCore | Use the vanilla property types | Reflect into block-state internals |
| C2ME, ScalableLux | Follow the worldgen rules. Emit light with `lightLevel` | Use `level.getRandom()` in a feature. Change light every tick |
| Krypton, PacketFixer | Use platform payloads | Touch the Netty pipeline. Depend on raised limits |
| Particle Core | Spawn particles on events with their own random | Read entities in a particle tick |
| Alternate Current | Keep `getSignal` a block-state read | Keep order-dependent static state |

#### Technology gameplay fit

| Module | What fits | What conflicts |
| --- | --- | --- |
| B12 Machines | Named issues and a warning tab (Mekanism). Smoke and a dark look on a fault (Create). A hover box. One chip-speed curve with one config value. Parts without block entities | Factory lanes, a face-by-face side screen, seven upgrade cards, a walled multiblock |
| B14 Power | A network object with merge and split (Mekanism, IE, RS2, Create). One "energy per fuel tick" constant. Generator comparator output. A chunk-reload GameTest | Rotational speed as a second unit (Create). Wire loss and path search (IE). Exploding generators |
| B15 Fences | Geometry tiers. Health bands. Wire events. A per-tier config table. Spool messages that name the failure. A switch that flips the network. Decorative fences with no block entity. Shock that follows the grid (IE), after a lead decision | Unbreakable reinforced blocks. Laser tripwires |
| B20 Feeders | Adaptive polling. One code path for player, hopper and dispenser bait. Diet arrays cached on reload. Free automation through item capabilities | An "any item" filter. A POI type |
| B21 Maps | Vanilla map function and trade. A spacing rule. Digsite changes stored as differences from a seed value | A Nublar structure search on the server thread |
| B22 Vehicles and tours | Stop for a tag list, horn after a delay, redstone hold, dwell and announcements (Automobility, MTR). No dinosaur boarding. A server speed clamp. A track on `BaseRailBlock` | Vehicles as data on a thread (MTR). Hitbox entities, drifting, boost pads, fuel, upgrades. Physics ships |
| Park security (backlog) | A camera view that shows "no signal" outside loaded chunks. An alarm from vanilla sounds. Keycards as data components, later | A live picture on a block. Forced chunks. Sentries. Reinforced blocks |
| B00 Config | A `features` section: a disabled feature does not tick, but its content stays registered. Id redirects. Caps in config | A reflection-based module engine |
| B07 Genes | One rebuild function for derived stats with a version stamp | — |
| Release 1 | Ponder scenes or a guide book in the order of progression (optional) | — |

Optional compat: a CC: Tweaked peripheral for fence state and a park summary can ship with Beta. A Create shaft-to-energy adapter, Create recipes and Ponder scenes wait for 26.2 builds.

#### Defects in the code

The researchers found these defects. Each row gives the effect in the game first.

| Effect in the game | Location |
| --- | --- |
| A powered fence post top makes a shadow instead of light `[inf]` | `J/block/ElectricFencePostBlock.java:275-276` (`getLightBlock`) |
| Every fence post ticks on both sides every tick | `J/block/ElectricFencePostBlock.java:300-305` |
| A rare crash or hang when two threads read a machine shape | `J/block/api/MultiEntityBlock.java:35,83` |
| Client memory grows while wires show | `J/block/api/MathUtils.java:121,133` |
| A genome change sends the whole genome as NBT, also when nothing changed | `J/init/DataSerializerInit.java:14-24`. `DinoData` has no `equals` |
| Hundreds of wire blocks do useless work on each neighbour change | `J/block/ElectricFenceBlock.java:88-91` |
| 100 animals near feeders print about 100 log lines per second | 12 print statements in 6 files, for example `NearestFeederSensor.java:68` |
| A far herd member never gets a fallback path when a server slows far animals | `FallbackBudget.java:30` |
| Far animals recover stamina and lose needs too slowly when a server slows them | Tick-count timers in `Dinosaur.java:581,723,760-765` |
| A Brachiosaurus neck vanishes at the screen edge | `EntityInit.java:36` (`sized(2, 3)`) |
| All six species are hostile mobs with one tracking range of 160 blocks | `EntityInit.java:30-39` |
| A wire vanishes when both posts are off screen | `ElectricWireRenderer.java:25-45` |
| 300 posts submit 300 or more renders each frame `[inf]` | `ENTITYBLOCK_ANIMATED` on fence and machine blocks |
| Each colour wheel allocates 640 KB of native memory | `ColorWheelWidget.java:45,110` |

One note claimed a leak in `FallbackBudget`. It is false: `forget(UUID)` exists, and `DinoGroup.java:205` calls it `[v]`.

#### Corrections to earlier records

- Driverless vehicles have precedents. The earlier survey said that none exists. Automobility's Autopilot and MTR trains run with no driver.
- Balm has a 26.2 branch, version 26.2.0.9 from 2026-09-25. One note saw only 26.3 because its clone had one branch.
- Balm bridges to the NeoForge 26.2 `EnergyHandler`, not to `IEnergyStorage`. NeoForge marks the old interface for removal. On Fabric, Balm exposes energy only when the player installs Team Reborn Energy.
- All six species set `clientTrackingRange(10)` and `MobCategory.MONSTER` in the code.

#### Open items O7 to O21

Each item has a default that ships if the lead does not answer.

| Id | Question | Default |
| --- | --- | --- |
| O7 | What powers a fence when its generator's chunk is not loaded? | Only loaded generators count. No chunk tickets |
| O8 | How long does a fence stay live after fuel ends? | A buffer of the post buffers, as a config value |
| O9 | Does a shock follow the grid's energy and drain the grid? | No. Fixed damage per tier |
| O10 | Which blocks accept energy from other mods' cables? | Generators give, machines accept, fences are closed |
| O11 | What stops the Explorer, how many seats, boarding while moving, a timed Tour Station? | Dinosaurs and players stop it. Jeep 2 seats, Explorer 4. Boarding at stops only. No Tour Station |
| O12 | What happens when a vehicle meets an electric fence? | It stops. No shock |
| O13 | Hover box always on, or with an item? A power meter block? | Always on. No meter block |
| O14 | What does the control room have in Beta? | Switch block, comparator outputs, alarm block |
| O15 | Which systems can a server owner turn off? | Vehicles, herd travel, fence wire health, breeding |
| O16 | Are gene definitions datapack JSON in Beta? | No. JSON after Beta |
| O17 | Where do species and piece of a fossil live? Opacity steps? | One block per host stone and species. 4 opacity steps |
| O18 | What is the largest DNA drive? | 150 entries. Test at 1,000 |
| O19 | What distance does the game guarantee for a digsite? | 1,600 blocks, the vanilla `/locate` radius |
| O20 | Does the roadmap include optional compat modules? | CC: Tweaked after B15. Create and Ponder wait for builds |
| O21 | Do vanilla and decorative fences hold dinosaurs? | Yes, as walls |

---

## 13. Open questions

### 13.1 Answered on 2026-10-05

| Question | Answer | Section |
| --- | --- | --- |
| Which fossil quality ladder applies? | Fragmented, poor, common and pristine, with yields of 5, 7.5, 10 and 30%. This ladder is the working default | 11.1 |
| Which editing thresholds apply? | Edit at 70%. The gender selection is sure at 80% | 11.6 |
| Which dinosaurs are in Beta? | The five dinosaurs of the design document and Gallimimus. Pteranodon is not in Beta | 11.9 |
| Does the carnivore feeder stay in the mod? | Yes. Beta also has a herbivore feeder | 11.12 |
| Which synthesis output does the design intend? | A syringe with a dinosaur embryo. The egg printer makes the unincubated egg | 11.7 |
| Do cards exist for the other species? | The cards exist on the project board. Four have no text and one has no stats | 8.6 |
| Config format: datapack JSON or TOML? | Both. TOML for the global balance numbers and datapack JSON for the data of each species | 11.17 |
| Is Fabric necessary for Beta? | Yes. Fabric and NeoForge must be playable at the Beta release | 11.17 |
| Is a port to 1.21.x planned before Beta or after Beta? | Before the new Beta systems. The target is Minecraft 26.2 | 11.17 |
| Does a saved world or a released build depend on the current block ids? | No. Only test worlds exist, and the team can discard them | 11.17 |
| Does the gene data follow the new table now, or after a port? | The new table replaces the code values. The animals that 1.20.1 does not have get entries after the port | 11.6 |
| Are the seven park-system diagrams design targets or background reference? | Background reference for Beta, and targets for the time after Beta | 11.17 |
| Which Tyrannosaurus fossil depth does the design intend? | The default is y 40 to 75, which joins the two ranges. Playtests set the final range | 11.1 |
| Which source applies where the art task board and section 11 differ? | Section 11 applies for plants, the frog, the goat, the skeletal builder, the petrified tree and the park machines | 8.6 |
| Are park decorations in Beta? | The enclosure marker and the park signs stay after Beta. The three voltage signs for fences are in Beta | 8.6 |
| Do the boards or the asset repository hold tasks or assets that change this list? | The analysis read the three GitHub boards and made an inventory of the asset repository. The Trello board is out of date | 8.5, 8.6 |

### 13.2 Open

1. Licence: LGPLv3 or CC0? On 2026-10-05 the project lead answered "still choosing". The decision comes before the first public release.
2. Section 11.14 lists the decisions that stay open after the design discussion.

---

## 14. Sources

- `Dumb-Code/Project-Nublar`, branches at 67bf1b5, 55e1968, aa9d047, 4244f62 and 97023cf.
- GitHub issues #1, #6 and #65, and the lists of open and closed issues.
- File lists of `Dumb-Code/ProjectNublar` for `1.16` and `master`.
- https://github.com/Dumb-Code/ProjectNublar/wiki/Fences
- https://youtu.be/W9cMQUB4EdM (Project Nublar Cloning Guide, 2022-02-19, 16:25): captions and 11 frames.
- https://www.youtube.com/watch?v=sm1PlzE5rlQ (Project Nublar Fence Demo, 2019-02-17, 1:07): 14 frames.
- https://www.dumbcode.net/guides/projectnublar/mob-genetics
- https://www.dumbcode.net/blog/state-of-dumbcode-2022
- https://www.dumbcode.net/mods/projectnublar
- The "Project: Nublar" design document, the "Mod Commission" brief and the "Project proposal". This analysis used only the asset counts from the proposal.
- The feature document folder `Minecraft Modding/PN Feature Docs/`. It is not in this repository. It also holds the three Word documents above.
  - First batch: `_Cloning Process.docx`, `Vanilla Animal DNA Effects.txt`, `## Special Genes.txt`, `Release Plan.txt`, `Primary Sources of DNA.txt`, `Dino VFX.txt`, `Idea False Eggs.txt`, `Paleo-Illagers.txt` and seven park-system diagrams (`image.png` to `image7.png`).
  - Second batch: `Tyrannosaurus.docx`, `Velociraptor.docx` and `Fossil Ore Aesthetics.docx` (draft cards from the project board), `Fossil Block Generation.docx` and `Backend Refactor.docx`.
- The design discussion with the project lead on 2026-10-05. Section 11 records the answers.
- The analysis could not read the ArtStation page, the Twitter account, the Trello board or the GitHub Projects board.
