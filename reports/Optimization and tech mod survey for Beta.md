# Big parks stay smooth when Nublar works only on change, and tech mods fit through standard capabilities

Date: 2026-10-10. Status: research report for the project lead. It changes no code and no plan module.

Optimization mods cannot make Nublar light. They remove vanilla costs, and Nublar's costs are its own. Lithium does not speed up SmartBrainLib 2 brains, and it cannot put a modded block entity to sleep. The block-entity bakers (EnhancedBlockEntities, BetterBlockEntities) ignore modded types. Immersive Optimization slows far entities by skipping whole ticks, and this breaks Nublar code that counts ticks. So three rules carry most of the gain. First, fence posts, wires and idle machines draw from the chunk mesh or a static block model, not from a block-entity renderer every frame. Second, one power network object per connected group does the power work, and posts and pylons never tick. Third, every timer uses game time, and every update goes out only when a value changes. The optimization mods add a short list of compatibility duties:

- exact culling boxes
- no client-only logic in `Entity.tick`
- no OpenGL calls
- light and path types from the block state only
- compact `StreamCodec` packets

 For gameplay, the tech mods fit Nublar best in feedback and in tours. Create shows a stopped network with smoke and a red tint. Mekanism names each machine fault. Immersive Engineering makes a shock drain the grid. Automobility and Minecraft Transit Railway show driverless vehicles that stop for obstacles and announce stops. The tech mods conflict with Nublar in layout puzzles, reinforced blocks, sentries and racing. On 26.2, Sodium, Iris, Lithium, EntityCulling, ImmediatelyFast, FerriteCore, CC: Tweaked, SecurityCraft and Balm 26.2.0.9 exist. Official Create, Embeddium, ModernFix, Refined Storage 2, Flywheel and Ponder do not. This report rates 167 techniques against modules B00 to B25 and the backlog. It gives 15 clean-room specifications (T1 to T15) and 15 open items (O7 to O21).

**Labels.** `[v]` means verified in source at the commit that the notes name, or checked again for this report. `[inf]` means inference. `[doc]` means a README, wiki, changelog or store page. Every cost figure without `[v]` is an estimate. Nobody profiled a 26.2 build.

**Names.** "The plan" is [project-plan.md](../docs/project-plan.md). "Section 11" is section 11 of [codebase-analysis.md](../docs/codebase-analysis.md). "The earlier survey" is [Reference mod survey for Beta.md](Reference%20mod%20survey%20for%20Beta.md). Its row ids (B12.1, S1, C5, O1) and its specifications S1 to S27 keep their meaning here. New row ids continue each module's numbering, for example B12.8. New specifications are T1 to T15. New open items are O7 to O21. "The notes" are the eleven files in `research_notes/Optimization and tech mod survey/`. "The baseline note" is `platform_and_library_baseline.md`. It is the referee for "use the platform" verdicts. "Repository facts" defines the short source names (for example "IE", "IO", "CCA").

## Twelve changes keep a big park light and add the most play

The twelve highest-value changes, in recommended order. Effort: S is under 100 lines, M is one or two classes with tests, L is a new subsystem. The earlier survey's ten changes stay in force. This list adds to them.

| Rank | Change | Module | Effort | In-game effect | Precedent |
| --- | --- | --- | --- | --- | --- |
| 1 | Draw fence posts and wire segments from block models in the chunk mesh. Keep a block-entity renderer only for sparks, arcs and a breaking wire | B15 | M to L | A paddock of 900 wire block entities costs no per-frame work. Today each one extracts render state and allocates every frame `[inf]` | IE wires in section geometry `[v]`, Sodium meshes all model blocks `[v]`, Chloride solid chests `[v]`, EBE and BBE idle models `[v]` |
| 2 | One power network object per connected group. Posts, pylons and conduits run no tick. Merge on connect, invalidate and flood again on break. Store energy on the block entities, not the graph | B14, B15 | L | 60 posts make 1 energy call per tick instead of 60 tick methods and 60 drains. Idle posts cost nothing `[inf]` | Mekanism network `[v]`, IE wire network `[v]`, RS2 no persisted network `[v]`, CCA wire graph `[v]`, Create kinetic network `[v]` |
| 3 | Use game time for every deadline, cooldown and need clock. Let per-tick queues tolerate gaps. Add a distance band inside SmartBrainLib 2 sensors and behaviours | B17, B25, B26 | M | Needs, cooldowns and herd travel stay correct when a server throttles far animals. Brain cost falls with distance, and movement stays at full speed | IO tick skip `[v]`, SBL `scanRate` and `cooldownFor` take functions `[v]` |
| 4 | Give every renderer an exact culling box: the posed, scaled body for dinosaurs, the whole machine, and the wire span. Never set `shouldRenderOffScreen` on repeated block entities | B09, B12, B15, B16 | S | A Brachiosaurus neck no longer vanishes at the screen edge. Animals behind a hill or inside a hangar cost no draw | Vanilla `getBoundingBoxForCulling` `[v]`, Sodium section culling `[v]`, EntityCulling `[v]` |
| 5 | Machine fault set and fault effects: one bitmask of named issues in the menu, smoke and a dark state in the world, a hover box with the reason | B12, B14 | M | The player sees "No power" or "No bedding" on an incubator without a wiki | Mekanism warning tab `[v]`, Create overstress smoke and tint `[v]`, Create goggle overlay `[v]` |
| 6 | Write save data and sync data separately. Coalesce world-visible changes with a short delay and turn the active look off after 60 ticks | B12, B14 | S | Machine and generator packets fall from full NBT per tick to a few small updates per state change | IE save and sync split `[v]`, Powah `sync(delay)` `[v]`, Mekanism turn-off delay `[v]` |
| 7 | Move client-only effects out of `Dinosaur.tick` into `aiStep` or server events. Guard cosmetic client work with window activity | B16, B25 | S | Footsteps, roars and dust keep playing for animals off screen. A background client spends nothing on effects | EntityCulling tick culling `[v]`, Dynamic FPS `[v]` |
| 8 | Send the genome and DNA drives as compact `StreamCodec` data. Give `DinoData` an `equals` | P2, B06, B16 | M | A drive with 100 species syncs about 2.4 KB instead of 15 to 24 KB. An unchanged genome is never sent again `[inf]` | PacketFixer limits `[v]`, vanilla `SynchedEntityData` `[inf]` |
| 9 | Keep block states small and functional. Fossil looks come from the position seed. Light and path types come from the state only. Fix the fence post light | B01, B12, B15 | S | A powered post top glows instead of casting shadow `[inf]`. The fossil set stays near 4,600 states instead of 1.5 million | FerriteCore `[v]`, ScalableLux `[v]`, Lithium path cache `[v]` |
| 10 | Tour rules from the transit mods: stop for a tag list of mobs, sound the horn after a delay, hold on a redstone signal, dwell and announce at each stop | B22 | M | The Explorer stops for a Triceratops on the road, honks, and tells riders "On your left, the T. rex paddock" | Automobility autopilot `[v]`, MTR dwell and announcer `[v]` `[doc]` |
| 11 | Expose items, energy and fluids through the standard capabilities on every machine, feeder and generator, with per-side rules | B12, B14, B20 | S | CC: Tweaked computers, Refined Storage, Mekanism cables and Create belts work with Nublar blocks and no Nublar compat code | CC: Tweaked generic peripherals `[v]`, RS2 external storage `[v]`, NeoForge capabilities `[v]` |
| 12 | Pool ambient sound by group and pitch. Spawn sparks and dust on events, never per tick | B12, B15, B16 | S | A 300-post fence hums with a few sound instances, not 300. Spark storms stay bounded | Create `SoundScapes` `[v]`, Particle Core `[v]` |

Ranks 1, 2 and 4 remove most of the fence cost on both sides. Ranks 3, 6, 7 and 8 make Nublar a good neighbour for the optimization mods. Ranks 5, 10, 11 and 12 add play at almost no tick cost.

## 26.2 availability decides which compat is possible

**In game:** a player can install only the mods that have a 26.2 build for the loader of the pack. A compat item for a mod with no 26.2 build cannot ship or be tested.

The table merges the notes with a second check on 2026-10-10. This report asked the Modrinth API for files that list game version 26.2 `[doc]`. It read GitHub branch lists with `gh api` `[v]`.

### Libraries and the platform

| Library | 26.2 status | Loaders | Effect for Nublar |
| --- | --- | --- | --- |
| NeoForge | `26.2.x` branch, tags `26.2.0` and `26.2.0-stable` `[v]` | NeoForge | Port target |
| Fabric API | Branch `26.2`, version 0.161.0 `[v]` | Fabric | Port target. It has no energy API `[v]` |
| Balm | Branch `26.2`, `version = 26.2.0.9`, last commit `ae5a2fd` on 2026-09-25 `[v]`. Modrinth file `26.2.0.9+fabric-26.2` `[doc]` | Fabric, NeoForge | Usable. See "Facts that changed or were corrected" #3 |
| Team Reborn Energy | 5.0.0, "26.1 or later" `[doc]` | Fabric | Optional. Balm bridges to it only when it is present `[v]` |
| GeckoLib 5 | 5.5.6 (2026-09-16) `[doc]` | Fabric, NeoForge, Forge | Usable |
| SmartBrainLib 2 | 2.0.3 (2026-10-04) `[doc]` | Fabric, NeoForge, Forge | Usable |
| Architectury API | Branch `26.2` `[v]`, files 21.1.x for 26.2 `[doc]` | Fabric, NeoForge | Usable (the plan keeps it) |
| Common Network | `26.2-1.1.0` (2026-06-16) `[doc]` | Fabric, NeoForge | Usable (the plan keeps it) |
| Forge Config API Port | 26.2.1, "bugfixes only" `[doc]` | Fabric, NeoForge | Usable (the plan keeps it) |
| Common Storage Lib | Newest branch `1.21.x`, version 0.0.10 `[v]` | Fabric, NeoForge | Not usable on 26.2 |
| Flywheel | Newest branch `26.1.2/dev` `[v]` | Fabric, NeoForge | Not on 26.2 |
| Ponder | Newest branches `mc26.1/dev` `[v]`. It pins one exact Minecraft version `[v]` | Fabric, NeoForge | Not on 26.2 |

### Optimization mods

| Mod | 26.2 status | Loaders on 26.2 | Effect for Nublar |
| --- | --- | --- | --- |
| Sodium | `mc26.2-0.9.2` (2026-09-11) `[doc]` | Fabric, NeoForge | Baseline renderer in every test |
| Iris | 1.11.4 for 26.2 (2026-09-13) `[doc]` | Fabric, NeoForge | Test shaders. Use the matching Sodium build `[doc]` |
| Embeddium | No 26.x file. Newest 1.21.4 `[doc]` | none | 1.20.1 build only |
| Sodium Extra | `mc26.2-0.9.4` `[doc]` | Fabric, NeoForge | Particle and animation toggles reach Nublar |
| Chloride | `1.8.1` for 26.2 (2026-07-20) `[doc]` | Fabric, NeoForge | Distance culling by `MobCategory` |
| Nvidium | `0.4.4-beta6-26.2` `[doc]` | Fabric | Terrain only |
| VulkanMod | No 26.2 file `[doc]` | none | Vanilla 26.2 has its own Vulkan backend `[v]` |
| ImmediatelyFast | 1.16.5 for 26.2 `[doc]` | Fabric, NeoForge | Batching by shared render type |
| Dynamic FPS | 3.11.9 (2026-06-16) `[doc]` | Fabric, NeoForge | Skipped frames when unfocused |
| Ixeris | 4.6.8 for 26.2 `[doc]` | Fabric, NeoForge | Threaded input. No GLFW calls in Nublar |
| GPUBooster | 1.21.1 only `[v]` | none | Not relevant |
| EntityCulling | 1.11.3 (2026-10-08) `[doc]` | Fabric, NeoForge | Occlusion and tick culling |
| MoreCulling | 1.8.1 for 26.2 `[doc]` | Fabric, NeoForge | Face culling on modded blocks by default |
| Entity Texture Features | Files for 26.1 to 26.3 `[doc]` | Fabric, NeoForge | Texture variants by id |
| Entity Model Features | 3.3.11 for 26.2 `[doc]` | Fabric, NeoForge | Not used by Nublar |
| EnhancedBlockEntities | Newest 1.21.4 `[doc]` | none | Pattern only |
| BetterBlockEntities | `1.3.8-beta.1+mc26.2` `[doc]` | Fabric, NeoForge | Vanilla types only |
| LambDynamicLights | 4.12.4 for 26.2 `[doc]` | Fabric, NeoForge | Optional light data files |
| SodiumDynamicLights | 1.21.5 only `[v]` | none | Use LambDynamicLights |
| BetterGrassify | `versions/` to 26.3 `[v]` | Fabric | Not relevant |
| CreateBetterFPS | Forge 1.20.1 only `[v]` | none | Pattern only |
| EntityViewDistance | 1.9.0 for 26.2 `[doc]` | Fabric | Per-type view distance |
| modelfix-multi | Newest branch 1.21.10 `[v]` | none | Asset practice only |
| Lithium | `mc26.2-0.25.3` (2026-07-29) `[doc]` | Fabric, NeoForge | Collision, path and hopper rules |
| AI-Improvements | 0.5.4 for 26.2 `[doc]` | NeoForge | Not relevant to SBL brains |
| Immersive Optimization (IO) | 0.2.0 for 26.2 `[doc]` | Fabric, NeoForge (Forge on older) | Far entities skip ticks. Tolerance duties |
| VMP | 0.2.0 beta 7 for 26.2 `[doc]` | Fabric | Player-near lookups up to 128 blocks |
| Rail Optimization | 1.19.4 only `[v]` | none | Pattern only |
| Async Locator | 1.20.2 only `[doc]` | none | Pattern only |
| FerriteCore | 9.0.0 for 26.1 and 26.2 `[doc]` | Fabric, NeoForge | Block-state rules |
| ModernFix | Newest file for 26.1.2. No 26.2 file `[doc]`. Branch `26.1` builds NeoForge only `[v]` | none on 26.2 | Test when a 26.2 file exists |
| C2ME | `0.4.2-alpha.0.56+26.2` `[doc]` | Fabric | Worldgen thread rules |
| ScalableLux | `0.3.0-alpha.0.16+26.2` `[doc]` | Fabric, NeoForge | Light from block state only |
| Krypton | 0.3.1 for 26.2 `[doc]` | Fabric | No Netty pipeline changes |
| PacketFixer | 3.3.6 for 26.2 `[doc]` | Fabric, NeoForge | Do not depend on it |
| memoryLeakFix | 1.20.4 only `[v]` | none | Pattern only |
| lazydfu | 1.19 only `[v]` | none | Not relevant |
| Stfu | Modrinth slug `shuttfup`, files to 26.3 `[doc]` | Fabric | Logging rules only |
| Particle Core | `0.3.3+fix1+26.2` (2026-10-10) `[doc]` | Fabric, NeoForge | Async particle ticking |

### Technology and content mods

| Mod | 26.2 status | Loaders on 26.2 | Effect for Nublar |
| --- | --- | --- | --- |
| Create (official) | No 26.x branch. Newest `mc1.21.1/dev` `[v]`. Wiki: a 26.1 port is in progress `[doc]` | none | No NeoForge Create link is possible |
| Create Fly | `26.2-rc-2-6.0.9-1` (2026-06-14), a separate Fabric rewrite, repository `ZurrTum/Create-Fly` `[doc]` `[v]` | Fabric | The only Create on 26.2 |
| Create add-ons (CCA, SnR, Create Connected, CDG, Bells & Whistles) | No 26.x branch from the authors `[v]`. Unofficial ports on Create Fly `[doc]`. CCA port `chaevsfe/createaddition` pins 26.2 and Team Reborn Energy 5.0.0 `[v]` | Fabric (ports) | A Nublar generator that exposes Team Reborn Energy feeds a CCA connector on Fabric |
| Other Create add-ons (CNA, CCK, Create Deco, Create Jetpack, CBC, Simulated) | No 26.x build `[doc]` | none | Pattern only |
| Mekanism | Branches `26.1`, `26.2`, `26.3` `[v]`. No 26.x file on Modrinth `[doc]` | NeoForge | Energy through the platform capability |
| Immersive Engineering | Newest branch read 1.21.1 `[v]`. No 26.x file `[doc]` | none | Pattern only |
| Powah | Files for 26.1.2 `[doc]` | NeoForge (26.1.2) | Pattern only on 26.2 |
| Refined Storage 2 | Newest file for 26.1.2 `[doc]` | none on 26.2 | Item capability compat when it arrives |
| Immersive Thunder | 1.21.6 only `[v]` | none | Idea after Beta |
| Immersive Machinery | 1.20.1 only `[v]` | none | Pattern only |
| SecurityCraft | v1.10.2.1 for 26.2 `[doc]` `[v]` | NeoForge | Players can mix it with Nublar on NeoForge |
| CC: Tweaked | 1.120.2 for 26.2 `[doc]`, branch `mc-26.2` `[v]` | Fabric, NeoForge | Optional peripheral compat is possible now |
| Simple Radio | Newest branch 1.21.1 `[v]` | none | Not on 26.2 |
| Simple Voice Chat | Files for 26.2 `[doc]` | Fabric, NeoForge and others | Needed by Simple Radio only |
| Alternate Current | `neoforge-mc26.2-1.9.0` and a Fabric file `[doc]` | Fabric, NeoForge | Redstone order changes |
| Automobility | 1.21.1 only `[v]` | none | Pattern only |
| Immersive Aircraft, Man of Many Planes | 1.21.1 only `[v]` | none | Pattern only |
| Minecraft Transit Railway | GitHub `master` targets 1.20.4 `[v]`. Modrinth has a NeoForge 1.21.4 beta `[doc]` | none | Pattern only |
| Valkyrien Skies 2, Clockwork | 1.20.1 and 1.21.1 branches only `[v]` | none | Four passive compat rules |
| Quark (and Zeta) | 1.21.1 only `[v]` | none | Pattern only |
| Tinkers' Construct | 1.20.1 only `[v]` | none | Pattern only |
| Ad Astra | 1.21.1 only `[v]` | none | Pattern only |
| WalksyLib | Active on 26.2 `[v]` | Fabric, client only | Not relevant |
| Patchouli | Not checked for 26.2 | — | Check before the plan adds a guide book |

**What this means for compat:**

- Every optimization duty in this report applies on day one of the port, because the main optimization mods exist on 26.2.
- An optional CC: Tweaked peripheral can ship with Beta on both loaders.
- A Create link can only target Create Fly on Fabric. No NeoForge Create exists on 26.x. The Create compat items stay "Defer".
- Mekanism, SecurityCraft and AI-Improvements are NeoForge-only neighbours. Nublar needs no code for them beyond standard capabilities.
- Ponder, Flywheel, Refined Storage 2 and ModernFix need a 26.2 release before any test. Re-check them when P2 finishes.

## Optimization: Nublar's own work carries the gain

**In game:** a park with hundreds of dinosaurs and fence posts and a full lab keeps 20 ticks per second. The frame rate stays stable. The park stays smooth when the player adds Sodium, Lithium, EntityCulling and the other optimization mods.

The optimization mods do three kinds of work. Some remove vanilla costs (Lithium, FerriteCore, Krypton). Some change how the client draws (Sodium, ImmediatelyFast, EntityCulling). Some skip work that a player does not see (IO, Chloride, Dynamic FPS). None of them removes a cost that Nublar's own code makes. Nublar must therefore remove its own waste, and then follow the rules that keep each mod working.

### Nublar's own lessons by area

#### Server

| Id | Lesson | In game | Evidence | Module |
| --- | --- | --- | --- | --- |
| OS1 | Posts, pylons and conduits have no ticker. One network object per connected group does the energy work once per tick. Changes merge or split the group | 300 idle posts cost 0 ticks. A fence switch flips a section in one step | Mekanism cables tick only a join flag `[v]`. IE connectors have no tick method `[v]`. Lithium cannot sleep modded block entities `[v]` | B14, B15 |
| OS2 | Do not unregister machine tickers. Write the tick as: read cached state, return when idle, else advance progress | An idle lab costs a few comparisons per machine per tick | No surveyed machine mod sleeps its machines `[v]`. A missed wake-up hook leaves a dead machine `[inf]` | B12 |
| OS3 | Look up a recipe only when no recipe is active, on a slot change, and at most every 50 ticks. Cache tier numbers at the time the tier item changes | No recipe or config read per tick | Ad Astra recipe cadence `[v]`, Mekanism `recalculateUpgrades` `[v]` | B12 |
| OS4 | Use game time for need decay, cooldowns, grace periods, stall tests and memory lifetimes. Clamp the elapsed time (for example 40 ticks) | Far animals keep correct needs when a server throttles them | IO skips the whole `tickNonPassenger`, so `tickCount` stops `[v]` | B17, B25, B26 |
| OS5 | Per-tick request queues tolerate gaps. `FallbackBudget` drops a request after one game tick today | Herd fallback paths still work for far herds | `FallbackBudget.java:30` `[v]` | B26 |
| OS6 | A distance band per dinosaur (within 64 blocks, within 128, beyond) scales sensor rates and idle behaviour cooldowns. Hunts, threats and fence contact stay at full rate | A 400-animal park spends brain time near players. Movement and needs run at full speed everywhere | SBL `scanRate(ToIntFunction)` and `cooldownFor` `[v]`. IO idea `[v]` | B25.9 |
| OS7 | Do not override `canCollideWith` on dinosaurs or vehicles. Override `canBeCollidedWith` only on the tour vehicle. Override `isPushable` only with a written reason | Every other mob keeps Lithium's fast collision path near a herd | Lithium groups classes by these overrides `[v]` | B22, B26, B27 |
| OS8 | Path types come from the block state only. Custom node evaluators call `super` | Lithium's path cache stays valid. Open grassland sections stay cheap | Lithium `PathNodeCache` `[v]` | B15, B17 |
| OS9 | No random-ticking Nublar block unless it needs one | Lithium keeps skipping sections with no tickable block | Lithium random ticking `[doc]` | B01, R01 |
| OS10 | Ask for entities with a typed query (`EntityTypeTest` or class) | Sensor queries use Lithium's fast path | Lithium entity sections `[doc]` | B17, B18 |
| OS11 | Keep every "is a player near" radius at 128 blocks or less | VMP answers the lookup from its area map on Fabric | VMP `getClosestPlayer` `[v]` | B19, B25 |
| OS12 | Poll a neighbour inventory at a rate that moves between 5 and 40 ticks, faster after a change and slower after none | An idle feeder that watches a chest costs up to 8 times less `[inf]` | RS2 external storage rate `[v]` | B20 |
| OS13 | Spread periodic work with a per-object offset (`(gameTime + id) % rate`) | 300 posts never do their slow check in the same tick | Create train tick offset `[v]`. Ad Astra distributors align, an anti-pattern `[v]` | B12, B15, B20 |
| OS14 | Bound every area job by a block count and a period, and stagger it | An enclosure scan of 6,000 blocks costs about 1.1 ms once per 5 s, not every tick `[inf]` | Ad Astra flood fill `[v]` | Backlog |
| OS15 | Verify an assembled structure one block at a time, and queue a full scan only on change | A formed machine costs about one block read per 4 ticks `[inf]` | Tinkers smeltery `[v]` | B12 |
| OS16 | No log call in a per-tick, per-entity or per-render path. SLF4J with `{}` only | No log spam at 100 animals | 12 print statements in 6 files today `[v]` | B17, B18, B25 |

#### Client

| Id | Lesson | In game | Evidence | Module |
| --- | --- | --- | --- | --- |
| OC1 | Fence geometry in three tiers: static model (post, wire, insulators), slow state in model data (tier, broken half, health band), effects only as particles or a few active block entities | No per-frame cost for a still fence. Sparks only where something happens | Sodium meshes model blocks and section-culls block entities `[v]`. IE bakes wires `[v]` | B15 |
| OC2 | Machines show a static block model while idle. The GeckoLib renderer draws only while active or near | An idle lab draws like ordinary blocks | Chloride solid chests `[v]`, EBE and BBE `[v]` | B12, B24 |
| OC3 | One `RenderType` per texture, stored in a static field or a cache. Never one per frame or per entity | ImmediatelyFast merges draws across a herd | ImmediatelyFast group batching `[v]` | B09, B12 |
| OC4 | Bake the colour layers (the bake spec). If a layer stays unbaked, give it its own render type and order. Never rely on submit order inside one render type | Layers keep their order with ImmediatelyFast and Iris | ImmediatelyFast render-order fixes `[doc]`, GeckoLib re-renders the whole model per layer `[v]` | B09 |
| OC5 | No OpenGL and no GLFW calls. Use vanilla pipeline, buffer and input classes | Nublar works with the vanilla Vulkan backend and threaded input | Vanilla 26.2 Vulkan backend `[v]`, Ixeris `[v]` | P2, B22 |
| OC6 | No core shader. No `sortOnUpload` on an opaque render type | Iris, Sodium and ImmediatelyFast keep their features | Sodium wiki `[doc]`, ImmediatelyFast blacklist `[v]`, CCA wire type `[v]` | B09, B15 |
| OC7 | Draw each wire once (from the lower end), with 4 to 6 segments for a short span, and one light read per end | About 4 times fewer wire vertices than CCA's 192 per wire, drawn twice `[inf]` | CCA renderer `[v]`, CNA one-end rule `[v]` | B15 |
| OC8 | Bioluminescence is an emissive layer, not a moving light source | A glowing herd causes no chunk rebuilds | SodiumDynamicLights rebuilds up to 8 sections per 0.1-block move `[v]` | B10 |
| OC9 | Gameplay state shows in the model, tint or sound, never only in an animated texture or a light level | Players who turn animations or light updates off still see a live fence | Sodium Extra toggles `[v]`, Sodium animates visible sprites only `[v]` | B14, B15 |
| OC10 | Name every particle type with a translation key. Spawn on events with a short lifetime. A particle uses its own random and reads no entity | Players can thin Nublar particles. Async particle ticking keeps them | Sodium Extra, Particle Core `[v]` | B15, B16 |
| OC11 | Pool ambient sound per group and pitch bucket within 16 blocks of the camera | A long fence hums with a fixed number of sounds | Create `SoundScapes` `[v]` | B12, B15 |
| OC12 | Render a monitor or minimap texture at most 5 to 10 times per second, and not at all when the screen is closed | No per-frame framebuffer switches | ImmediatelyFast framebuffer and map atlas work `[v]` | Backlog |

#### Memory

| Id | Lesson | In game | Evidence | Module |
| --- | --- | --- | --- | --- |
| OM1 | Keep cosmetic randomness out of block states. Draw tint, opacity and rotation from the position seed and weighted model variants | The fossil set stays near 4,600 states. Design B (quantised looks in state) would make about 1.5 million `[inf]` | FerriteCore `FastMap` size is the product of value counts `[v]` | B01 |
| OM2 | Register few blocks. One block per host stone and species, with properties, beats one block per host, quality and piece | 910 fossil blocks exist today for two species. Six species would make about 3,000 to 3,600 `[v]` `[inf]` | ModernFix, FerriteCore | B01 |
| OM3 | Use only `IntegerProperty`, `BooleanProperty` and `EnumProperty` | No start-up crash with FerriteCore | FerriteCore value-order check `[v]` | All blocks |
| OM4 | Do not compare `VoxelShape` objects by identity and do not change a shape after creation | Shape sharing works | FerriteCore `[v]` | B12, B15 |
| OM5 | Per-level stores (power networks, feeder registry, track data) use level attachments or `SavedData`, or a `WeakHashMap` whose values never reach the level | No world leak after a disconnect | `GroupManager` already follows the rule `[v]` | B14, B15, B20, B22 |
| OM6 | Never cache an `Entity`, `BlockEntity` or `Level` in a static field. Cache a UUID or a `BlockPos` | No world leak | memoryLeakFix patterns `[doc]` | All |
| OM7 | Add a `"version"` integer to every custom NBT root | Later format changes can migrate old data | lazydfu note `[inf]` | P2, B25 |

#### Network

| Id | Lesson | In game | Evidence | Module |
| --- | --- | --- | --- | --- |
| ON1 | Payloads are records with a `StreamCodec`. Data on items is a data component with a codec. No `writeNbt` of the genome | Smaller join bursts and no 2 MiB NBT risk | Vanilla limits read from PacketFixer `[v]` | P2, B06, B16 |
| ON2 | Stay under the vanilla limits: 32,767 bytes from a client, 1 MiB from the server | No "Packet too big" kick without PacketFixer | PacketFixer `[v]` | All |
| ON3 | Do not sync what the client can compute or never reads (server-only genes, need floats) | Smaller spawn data for 300 tracked animals | Earlier survey S10 | B16 |
| ON4 | Wire changes travel as small events (about 20 bytes per connection) plus one batch when a player starts to watch a chunk. Health travels in 3 or 4 bands | A 100-wire paddock costs about 2 KB on first sight `[inf]` | IE `WireSyncManager` `[v]` | B15 |
| ON5 | A fence section switch changes one block state per post and lets vanilla bundle the updates per section | No custom "section off" packet | Vanilla section updates `[inf]` | B15 |
| ON6 | Driver controls go to the server only when they change. The server clamps speed and distance | No speed cheat, few packets | Automobility trusts the client `[v]`. Immersive Machinery sends on change `[v]` | B22 |
| ON7 | A moving object sends updates while it moves, one stop packet, then nothing | A parked vehicle costs no packets | Simulated rope tracking `[v]` | B22 |
| ON8 | Never touch the Netty pipeline | Krypton keeps its handlers | Krypton replaces handlers by name `[v]` | All |
| ON9 | Tracking range by body size: 8 chunks for Gallimimus, Velociraptor and Dilophosaurus, 10 for T. rex and Triceratops, 12 for Brachiosaurus. Vehicles 10 with interval 3 | Fewer movement packets for small animals. A Brachiosaurus shows from a hill | Vanilla values `[v]`. All six species use 10 today `[v]` | B16, B22 |

#### Worldgen

| Id | Lesson | In game | Evidence | Module |
| --- | --- | --- | --- | --- |
| OW1 | Build worldgen lookup maps once on the main thread as immutable maps, and publish them by one assignment | No data race when C2ME generates on many threads | C2ME thread fixes `[v]`. `FossilCollection` static `HashMap`s today `[v]` | B01, B03, B21 |
| OW2 | Use only the `RandomSource` that the generator passes in. No `level.getRandom()`, no `new Random()` | No crash with C2ME's checked random | C2ME `CheckedThreadLocalRandom` `[v]` | B01 |
| OW3 | Place blocks only inside the given region, with no drops and no entity spawns | No warnings and no chunk deadlocks | C2ME region checks `[v]` | B01, B03, B21 |
| OW4 | No lazy cache inside a feature config or a structure piece | Shared configs stay thread safe | C2ME fix of a vanilla lazy cache `[v]` | B01, B21 |
| OW5 | Configured and placed features as data with codecs | Data-pack worldgen is "fully supported" by C2ME `[doc]` | C2ME README | B01 |
| OW6 | Digsite maps and trades use the vanilla exploration map function and the vanilla treasure-map trade | Any async-locate mod covers Nublar maps. Nublar adds no structure search of its own | Async Locator hooks `[v]` | B21 |

### Compatibility checklist per optimization mod

Each row says what the mod changes, what Nublar must do, and the test. Nublar adds no dependency on any of these mods.

| Mod | What it changes | Nublar must | Nublar must not | Test |
| --- | --- | --- | --- | --- |
| Sodium | Section-culls block entities and entities. Meshes model blocks on worker threads. Three terrain passes only | Give exact render boxes. Keep model data immutable and copy it inside `AddSectionGeometryEvent`. Keep the dinosaur culling box under 204,800 blocks³ | Set `shouldRenderOffScreen`, mix into render dispatchers, call OpenGL, rely on random order for visuals | Every client test, both loaders |
| Iris | Shader pipeline for terrain and entities | Use vanilla render types and emissive textures | Ship a core shader | One run with a shader pack per release |
| Embeddium | 1.20.1 only | Nothing for 26.2 | — | None after the port |
| Sodium Extra | Turns off animations, particle types, light updates | Show state in model or tint. Translate particle names | Show state only by an animated texture or light | One run with animations off |
| Chloride | Hides entities and block entities past 64 blocks (128 for `MONSTER`) and outside a 32-block vertical band | Keep logic out of renderers. Use stable ids | Rely on rendering for gameplay or sounds | One hilltop view |
| Nvidium | GPU terrain culling | Keep static fence geometry in the chunk mesh | Add a terrain render pass | None |
| ImmediatelyFast | Batches draws by render type and reorders them | Share render types. Bake layers | Create a render type per frame, rely on submit order | Herd of 20 with layers |
| Dynamic FPS | Skips frames when unfocused | Drive one-shot effects from events | Drive sounds from animation keyframes only | None |
| Ixeris | Moves input to its own thread | Use `KeyMapping` | Call GLFW | Drive the Jeep once |
| EntityCulling | Occlusion culling by rays. Skips `Entity.tick` on the client for culled entities. Block-entity frustum test | Override `getBoundingBoxForCulling`. Move client effects to `aiStep`. Give wire block entities a span box (NeoForge method, Fabric extension interface) | Put client-only logic in `tick()`. Mark a non-cube block as full and opaque | Brachiosaurus behind a hill, wire behind a wall |
| MoreCulling | Culls faces of modded blocks by default | Give each non-cube block an occlusion shape that matches its model | Opt out before a hole shows | Machine next to stone |
| ETF | Swaps texture ids by rules. Caches one entry per id for the session | Keep stable texture ids. Name glow layers without the `_e` suffix | Make a texture id per animal | One run with a `.properties` file |
| BBE, EBE | Bake vanilla block entities | Nothing | — | None |
| LambDynamicLights | Lights from entities, rebuilds chunk sections | Optional data files for vehicle lights only | Make dinosaurs light sources | None |
| EntityViewDistance | Per-type view distance | Document Nublar ids | Read the type's tracking range for gameplay | None |
| Lithium | Collision groups by override, path cache, hopper sleep for vanilla types, faster entity queries | Follow OS7 to OS10. Call `setChanged` only on change. Expose sided slots | Override collision methods without need. Give a block entity-dependent path types | Herd walk, hopper into a machine |
| IO | Skips whole ticks of far entities | Follow OS4 and OS5. Implement `isAlwaysTicking` on the tour vehicle | Opt out the dinosaurs | Park with the player 200 blocks away |
| VMP | Area maps for tracking and nearby players (Fabric) | Keep radii at 128 or less. Keep positions in `Entity.position()` | Keep a private position | Pose-change anti-clip test (VMP cancels one zero move) |
| AI-Improvements | Swaps an exact `LookControl` class | Nothing | — | None |
| FerriteCore | Shares state tables and shapes | Follow OM1, OM3 and OM4 | Reflect into state internals | Start-up with it installed |
| ModernFix | Lazy model loading, lazy entity renderers (NeoForge) | Register reload listeners through platform events. Let renderers build on first sight | Loop all states at start-up. Hold a world in a static | One run with dynamic resources on, when a 26.2 file exists |
| C2ME | Threaded worldgen and checked world random (Fabric) | Follow OW1 to OW5 | Use `level.getRandom()` in a feature | 200-chunk generation |
| ScalableLux | Threaded light from block state only | Emit light with `lightLevel`. Keep `useShapeForLightOcclusion` off on wires | Change light every tick. Read a block entity for light | Fence switch over 200 posts |
| Krypton | Replaces compression and framing (Linux) | Use platform payloads | Touch the Netty pipeline | Linux dedicated server once |
| PacketFixer | Lifts packet limits | Stay under vanilla limits | Depend on it | None |
| Particle Core | Culls particles, can tick them in parallel | Follow OC10 | Read entities from a particle tick | Spark storm with async on |
| Alternate Current | Changes redstone dust update order | Keep `getSignal` a state read. Write state only on change | Keep order-dependent static state | Fence switch GameTest with and without it |

### Budget additions from the new notes (estimates)

The earlier survey's budget for load B stays. These rows add the costs that the new notes measured or derived. All values are estimates unless marked `[v]`.

| Subsystem | Today | After | Basis | Owner |
| --- | --- | --- | --- | --- |
| Fence post ticks | Every post ticks on both logical sides, drains 10 FE and recomputes rotation `[v]` | 0 post ticks. One network energy call per tick per group | Local code `[v]`, Mekanism, IE | B14, B15 |
| Fence renderer, 300 posts in view | 300 or more GeckoLib block-entity draws per frame, plus wires drawn per post `[inf]` | Static model in the chunk mesh. Active block entities only for effects | EBE, BBE, IE, Sodium | B15 |
| Wire geometry per wire | CCA-style: 192 vertices drawn from both ends `[v]` for CCA. Nublar: 39 floats per connection per frame `[v]` | 0 per frame when baked. Static vertices rebuild only with their section | IE section geometry `[v]` | B15 |
| Fence switch on 200 posts | Up to 200 light updates if the post light toggles `[inf]` | One state change per post, light from `lightLevel`, bundled per section | ScalableLux, Alternate Current | B15 |
| Machines while idle | Full ticks with checks `[inf]` | A few comparisons per tick | Mekanism, RS2 | B12 |
| Generator and machine state packets | Full NBT per tick `[v]` | One coalesced update per state change. Active look off after 60 ticks | Powah, Mekanism | B12, B14 |
| Hopper next to a Nublar machine | Cannot sleep under Lithium `[v]` | Same. A small fixed cost per hopper | Lithium | B12, B20 |
| Far herds under IO | Brain and needs skip with the entity, timers based on ticks stretch `[v]` | Needs on game time. Brain rate by distance band | IO, SBL | B17, B25 |
| DNA drive sync | About 15 to 24 KB per drive `[inf]` | About 2.4 KB | PacketFixer limits | B06 |
| Genome at join, 300 animals | 300 to 600 KB in separate packets `[inf]` | Smaller without server-only genes | — | B16 |
| Brachiosaurus occlusion test | — | Worst case 2 to 3 ms on the EntityCulling worker thread, not the main thread `[inf]` | EntityCulling rays | B16 |
| Camera view (backlog) | SecurityCraft forces 625 chunks at radius 12 per viewer `[v]` | 0 forced chunks. "No signal" outside loaded chunks | SecurityCraft | Backlog |
| Live feed on a block (backlog) | One extra world render per feed per frame, up to 30 per second `[v]` | Not built in Beta | SecurityCraft | Backlog |
| Enclosure scan (backlog) | — | About 1.1 ms once per 5 s per marker, staggered | Ad Astra | Backlog |

## Platform first: what Nublar still writes itself

**In game:** nothing. This section decides where code comes from. The lead set the rule on 2026-10-10. Where the platform, vanilla or a library that Nublar already uses covers a need, Nublar uses it. Nublar does not copy a mod. The baseline note is the referee for each row.

The plan keeps Architectury, Common Networking and Forge Config API Port, and uses Balm for energy and fluid storage only (lead decision, 2026-10-06). The baseline note suggests Balm for networking, config and registration too. That suggestion does not change the decision. All three kept libraries have 26.2 files (see "26.2 availability").

| Need | Platform or library that covers it | What Nublar still writes |
| --- | --- | --- |
| Energy exposure on machines, generators, conduits | Balm `BalmEnergyStorageProvider` and `DefaultEnergyStorage`. Balm bridges to the NeoForge 26.2 `EnergyHandler` capability and, when Team Reborn Energy is present, to its Fabric lookup `[v]` | Per-side rules: which blocks accept and which give energy (open item O10) |
| Energy network for fences and pylons | Nothing. No loader or library has a wire network `[v]` | The network object (T1). This is Nublar's own subsystem |
| Neighbour energy lookups | NeoForge `BlockCapabilityCache`, Fabric `BlockApiCache` `[v]` | One cache per connected side, only where a block pushes |
| Item transfer for machines and feeders | Vanilla `Container` and `WorldlyContainer`, wrapped by both loaders for hoppers and pipes `[v]` | Per-face slot rules (insert-only inputs, extract-only outputs, no upgrade slots) |
| Fluid tanks | Balm `FluidTank`, or NeoForge `ResourceHandler<FluidResource>` and Fabric `Storage<FluidVariant>` if a fluid needs components `[v]` | Nothing beyond the tank size |
| Item data (DNA, genomes, drives, tubes, keycards, stored charge) | Vanilla data components with `Codec` and `StreamCodec` `[doc]` | The component records. No `CompoundTag` on stacks |
| Block-entity save and load | Vanilla 26.x `ValueInput` and `ValueOutput`, `getUpdateTag` `[v]` | The split between save data and sync data |
| Per-level and per-chunk data | Vanilla `SavedData`, NeoForge and Fabric data attachments, Balm attachments `[v]` `[doc]` | The data records only |
| Networking | `CustomPacketPayload` with `StreamCodec`. The plan keeps Common Networking, which has a 26.2 build `[doc]` | The payload records and server checks |
| Config | Forge Config API Port on both loaders (plan). NeoForge `ModConfigSpec` underneath `[v]` | The keys and the `features` switches (T11) |
| Species and gene data | Datapack reload listeners (plan). The baseline note proposes dynamic registries with codecs `[v]` | The codecs. A dynamic registry is optional and synced to clients |
| Data load conditions (recipes of a disabled feature) | NeoForge `ICondition`, Fabric resource conditions `[inf]` | One condition type on both loaders |
| Block-entity render box | NeoForge `IBlockEntityRendererExtension.getRenderBoundingBox` `[v]`. Fabric API has none. EntityCulling offers a Fabric interface. Vanilla `shouldRenderOffScreen` is the coarse fallback `[v]` | The box from the wire span or the machine shape |
| Entity culling box | Vanilla `EntityRenderer.getBoundingBoxForCulling` and `affectedByCulling` `[v]` | The posed, scaled body box, cached per tick |
| Static fence and wire geometry | Vanilla block models with `multipart`. NeoForge `AddSectionGeometryEvent` and model data. Fabric `FabricBlockStateModel#emitQuads` with `RenderDataBlockEntity` `[v]`. Sodium supports all three `[v]` | A shared immutable `FenceVisual` record and two thin loader adapters. Each wire block draws only its own segment, so no strip crosses a section border |
| Render-time data | Fabric `RenderStateDataKey`, the 26.x extract and submit design `[v]` | Fill render state in `extractRenderState` with no allocation |
| Tracking range and update interval | Vanilla `EntityType.Builder.clientTrackingRange` and `updateInterval`, NeoForge suppliers, the `entity-broadcast-range-percentage` server property `[v]` | Values per species and per vehicle |
| Vehicles | Vanilla passengers, `ServerboundMoveVehiclePacket` with its speed check, step-height attribute, `AbstractBoat` as a template `[v]` `[inf]` | The vehicle entity, seats, the speed clamp |
| Track following | Vanilla `BaseRailBlock`, `RailShape`, `AbstractMinecart.exits` `[v]`. NeoForge `getRailDirection` for switches `[v]` | The rail-follow mode of the Explorer (S20) and stop markers |
| Redstone pulses and alarms | Vanilla `scheduleTick`, `POWERED`, sound events with attenuation `[v]` | The blocks |
| GameTests | Vanilla framework, NeoForge `RegisterGameTestsEvent`, Fabric `fabric-gametest-api-v1` `[v]` | Tests in `common`, one registration class per loader |
| Optional mod checks | NeoForge `ModList`, Fabric `FabricLoader.isModLoaded` `[v]` | One flag per optional mod, checked at each call site |
| Computer access | CC: Tweaked generic peripherals read standard capabilities on NeoForge, inventories on Fabric `[v]` | Optional park peripheral, and Fabric energy and fluid sources (C.3) |

## Technology gameplay: the fit is feedback, tours and capabilities

**In game:** the player runs a lab, a power grid, fences, feeders and tours. The tech mods show three things. Players learn a system without a wiki. A grid fails in a visible way. A driverless vehicle feels safe.

### How the tables rate a technique

Verdicts: **Adopt** builds the technique nearly as the precedent does. **Adapt** keeps the idea and changes the form to agree with section 11. **Compat** is an optional integration that ships only when the other mod is present. **Defer** waits for a decision, art, a 26.2 build or a measurement. **Avoid** leaves it out. Effort counts code beyond the module's scope. Row ids continue the earlier survey's numbering. A "T" number links to "Clean-room implementation specifications".

### B12 Machine framework

| Id | Technique | In game | Source | Verdict | Effort |
| --- | --- | --- | --- | --- | --- |
| B12.8 | Named machine issues (no power, no input, output full, wrong filter, no bulb, no bedding, no egg) as one bitmask in `ContainerData`, shown as a warning tab with a tooltip list (T7) | The incubator screen says "No bedding" with an icon | Mekanism `RecipeError`, `GuiWarningTab` `[v]` | Adopt | M |
| B12.9 | Fault effect in the world: smoke puffs and a dark model when a machine stops for power, a short recovery puff when it starts (T7) | The player sees a dead machine from across the lab | Create overstress effect `[v]` | Adapt | S |
| B12.10 | Hover box near the crosshair with the machine state, read from synced fields (T6) | The player looks at a printer and reads "Working 42 %" | Create goggle overlay `[v]` | Adapt | M |
| B12.11 | Save data and sync data written separately. `sync(delay)` coalesces world-visible changes. The active look turns off 60 ticks after work stops | Fewer packets. No flicker for bursty machines | IE `IMultiblockState` `[v]`, Powah `[v]`, Mekanism `[v]` | Adopt | S |
| B12.12 | Chip speed curve: work ticks = base × M^(−n/m), draw = base × M^(2n/m), M a config value | A faster chip finishes sooner and costs more per product, by one known rule | Mekanism `UpgradeUtils` `[v]` | Adapt | S |
| B12.13 | Static block model while idle, GeckoLib renderer only while active (T5) | An idle lab draws like stone blocks | Chloride `[v]`, EBE, BBE `[v]` | Adapt | L (needs idle-pose exports, B24) |
| B12.14 | Parts without block entities: a part index in the block state, a fixed offset to the master, block-only capability registration that returns the master's handler | Pipes on any face reach the machine. Parts cost no ticks or saves | NeoForge `registerBlock`, Fabric `registerForBlocks` `[v]`, IE dummy parts `[v]` | Adapt | M |
| B12.15 | One render box over the whole machine on the master | A tall machine does not vanish at the screen edge | IE `renderBox` `[v]` | Adopt | S |
| B12.16 | Assembly failure message that names the wrong part and highlights its position | "Place the lid before the tanks" with the block outlined | Tinkers `MultiblockResult` `[v]` | Adapt | S |
| B12.17 | Rotating one-block check of an assembled machine every 4 ticks, full check only on change (S2 from the earlier survey stays the model) | No scan cost while a machine stands | Tinkers smeltery `[v]` | Adopt | S |
| B12.18 | Egg printer templates as small strategies (item count, fluid, result, finish), from a datapack recipe type | New egg kinds need data, not a new switch branch | CEI printing behaviours `[v]` | Adapt | M |
| B12.19 | Recipe lookup cadence (OS3) | No per-tick recipe or NBT work (fixes S9) | Ad Astra `[v]` | Adopt | S |
| B12.20 | Staged placement for the creative "assembled machine" item, one part per 5 ticks with particles | The creative machine builds itself in a second | Powah reactor builder `[v]` | Adapt | S |
| B12.21 | Comparator output from cached counts, updated only when the 0 to 15 value changes (extends B12.4) | Redstone alarms with vanilla parts | Alternate Current rules `[v]`, IE current transformer `[doc]` | Adopt | S |
| B12.22 | Factory lanes, six-face side configuration, seven upgrade cards | — | Mekanism, Ad Astra | Avoid | — |
| B12.23 | Kept-contents cache after a break | — | Mekanism `[v]` | Avoid (machines drop every slot) | — |
| B12.24 | Walled cuboid multiblock around a controller | — | Tinkers smeltery | Avoid (conflicts with part-by-part assembly) | — |

### B14 Power

| Id | Technique | In game | Source | Verdict | Effort |
| --- | --- | --- | --- | --- | --- |
| B14.6 | Network object per connected group with supply, draw and a dirty flag. Merge on connect, invalidate and flood again on break. Energy stored on the block entities (T1) | The grid answers "powered" for a whole section at once | Mekanism, IE, RS2, CCA, Create `[v]` | Adapt | L |
| B14.7 | Queued connector loading: a chunk full of posts joins networks in one batch per tick (T1) | No lag spike when a big park loads | IE `processQueuedLoads` `[v]` | Adapt | S |
| B14.8 | Flicker guard on a network that changes state too often | No oscillating outage loop | Create flicker score `[v]` | Adapt | S |
| B14.9 | Validation heartbeat every 60 ticks with a per-network offset | A network heals itself without scans | Create `kineticValidationFrequency` `[v]` | Adopt | S |
| B14.10 | One constant "energy per fuel tick", from which generator output and burn time follow | One number tunes the starter-park balance. It fixes the 0.2 FE against 10 FE mismatch | Powah `DefaultEnergies` `[v]` | Adapt | S |
| B14.11 | Generator comparator output (stored fraction), updated on a change of the 0 to 15 value | A redstone lamp warns of low fuel | IE current transformer `[doc]` | Adopt | S |
| B14.12 | Strided world checks (`ticks % 40`) for anything a generator reads from the world | Cheap checks | Powah solar `[v]` | Adopt | S |
| B14.13 | Battery or cell keeps its charge in an item data component | Picked-up storage keeps its energy | Create Connected battery `[v]` | Defer (no battery in Beta) | S |
| B14.14 | Power meter block or hover readout of supply and draw in FE per tick | The player reads the grid load | Create stressometer `[v]` | Defer (O13) | S to M |
| B14.15 | GameTest: unload and reload a chunk in the middle of a 100-post line, then check the powered flag | Power survives chunk loading | Mekanism network test `[v]` | Adopt | M |
| B14.16 | Rotational power as a second quantity (RPM) | — | Create | Avoid | — |
| B14.17 | Wire loss, path search, transformer limits | — | IE, CNA | Avoid (lossless lines decided) | — |
| B14.18 | Per-tick neighbour energy pull or cable walk on every push | — | Ad Astra, Powah, CNA | Avoid | — |
| B14.19 | Generator that explodes, radiation | — | CNA reactor | Avoid (tone) | — |
| B14.20 | Shared Create shaft to FE adapter (a "dynamo") in a separate optional module | Create players power a lab from a water wheel | Create `BlockStressValues`, CCA motor and alternator rule `[v]` | Compat, Defer (no NeoForge Create on 26.x) | M |

### B15 Fences and containment

| Id | Technique | In game | Source | Verdict | Effort |
| --- | --- | --- | --- | --- | --- |
| B15.7 | Fence geometry tiers: static model, slow state in model data, effects only as particles or active block entities (T4) | A still fence costs no frame time. Broken halves and cracks show from the model | IE, Sodium, Chloride, EBE `[v]` | Adopt | M to L |
| B15.8 | Wire health in 3 or 4 bands for the client. Exact health stays on the server | Cracks show as bands. The network does not flood per hit | IE wire sync `[v]`, Sodium rebuild rules `[v]` | Adopt | S |
| B15.9 | Wire events: about 20 bytes per connect, break or band change, plus one batch on chunk watch (T2) | Walk up to a paddock and every wire appears | IE `WireSyncManager` `[v]` | Adopt | M |
| B15.10 | Shock strength follows the energy the network can deliver, and each shock drains the network (T3) | A weak generator and a pushing T. rex brown out a fence | IE `WireDamageHandler` `[v]` | Adapt after O9 | M |
| B15.11 | Per-tier fence config block: span, damage radius, damage factor, wire health | All tier numbers in one table | IE wire config `[v]` | Adopt | S |
| B15.12 | Spool messages that name the failure: too long, already joined, no free connection, blocked | The player knows why a wire did not connect | CCA connect results `[v]` | Adopt | S |
| B15.13 | Switch behaviour: hand toggle, redstone control, an inverted mode. The switch flips the network, never each post | One redstone read per change for any fence size | IE circuit breaker `[doc]`, Alternate Current rules `[v]` | Adopt | S |
| B15.14 | Fence post light through `lightLevel` from the state, changed only when it changes | Powered post tops glow. Switching a section costs one light update per post at most | ScalableLux `[v]` | Adopt | S |
| B15.15 | Delete the empty `ElectricFenceBlock.neighborChanged` and port the signature with `Orientation` | Less work on hundreds of wire blocks | Local code `[v]`, Alternate Current `[v]` | Adopt | S |
| B15.16 | Decorative fence and sign variants with no block entity and the vanilla fence tag | Visitor-side fences and voltage signs cost no ticks | Create Deco mesh fence `[v]` | Adopt | S |
| B15.17 | Fence connection targets stored as offsets from the post | Structures with fences paste correctly | Simulated and CCA `[v]` | Adapt | S |
| B15.18 | Fence power state kept across unloaded chunks by a saved graph | — | CCA `WireGraph` `[v]`, IE | Avoid in Beta (see O7) | L |
| B15.19 | Fence GameTest with Alternate Current on and off | Redstone order changes do not break the switch | Alternate Current `[v]` | Compat | S |
| B15.20 | Reinforced unbreakable blocks | — | SecurityCraft | Avoid (animals must break fences) | — |
| B15.21 | Laser tripwire as an escape alarm | — | SecurityCraft `LaserFieldBlock` `[v]` | Avoid (dinosaur-side escape test is cheaper) | — |

### B20 Feeders

| Id | Technique | In game | Source | Verdict | Effort |
| --- | --- | --- | --- | --- | --- |
| B20.10 | Adaptive poll for a feeder that watches a neighbour inventory (OS12) | An idle feeder costs almost nothing | RS2 `[v]` | Adopt | S |
| B20.11 | Plan, simulate and apply for the live-bait feeder, so a player, a dispenser and a hopper share one code path | Any input method loads bait the same way | CCK arm points `[v]` | Adapt | S |
| B20.12 | Diet and prey lookups resolved once per reload into arrays, in a cache cleared on reload and tag update | No tag lookup per sensor scan | CCK `UnpackingRecipeCache` `[v]`, Tinkers memo `[v]` | Adopt | S |
| B20.13 | Feeders expose sided item handlers. Create chutes, Refined Storage exporters and hoppers fill them with no Nublar code | Automation from any mod | RS2, CCK, Create `[v]` | Compat (free) | S |
| B20.14 | Deterministic components on stackable food and Nublar items | Items stack and show in storage grids | RS2 `ItemResource` `[v]` | Adopt | S |
| B20.15 | An "any item" filter slot | — | Create Connected wildcard warning `[doc]` | Avoid | — |
| B20.16 | POI type for feeders | — | Quark trough `[v]` | Avoid (the earlier survey's registry is simpler) | — |

### B21 Maps and locate

| Id | Technique | In game | Source | Verdict | Effort |
| --- | --- | --- | --- | --- | --- |
| B21.10 | Digsite maps through the vanilla `exploration_map` loot function and the vanilla treasure-map trade (OW6) | A map trade never freezes the server when an async-locate mod is installed | Async Locator hooks `[v]` | Adopt | S |
| B21.11 | Digsite spacing chosen so that a search ends in a few rings (open item O19) | Maps fill fast on a fresh world | Async Locator, vanilla `/locate` radius 100 chunks `[v]` | Adapt | S |
| B21.12 | Placeholder map with "Locating…", a shared worker, write-back on the main thread | — | Async Locator `[v]` | Defer (only if Nublar writes its own search) | M |
| B21.13 | Digsite base value from seed and biome, with a `SavedData` map of changes only | Untouched digsites cost nothing in saves | CDG oil chunks `[v]` | Adapt | M |
| B21.14 | Worldgen thread rules OW1 to OW5 | No crash with C2ME | C2ME `[v]` | Adopt | S |

### B22 Vehicles and tours

| Id | Technique | In game | Source | Verdict | Effort |
| --- | --- | --- | --- | --- | --- |
| B22.11 | Stop-for tag (`projectnublar:tour_vehicle_stops_for`), blocked check every 10 ticks, horn after 3 s with a growing chance, stop after a limit (T13) | The Explorer waits for a Triceratops and honks | Automobility autopilot `[v]` | Adapt | S |
| B22.12 | Redstone hold on the stop marker | The control room holds a tour with a lever | Automobility sign `POWERED` `[v]` | Adapt | S |
| B22.13 | Dwell time per stop (overrides `vehicles.stop_seconds`), an announcement (text and sound) at each stop, and a "Stop 2 of 4" action-bar line (T13) | The tour feels guided | MTR dwell, announcer, HUD `[doc]` `[v]` | Adapt | S to M |
| B22.14 | Tour Station that starts a driverless Explorer every N minutes up to a vehicle limit | Tours run with no key press | MTR depot `[doc]` | Defer (O11) | M |
| B22.15 | Loop validation message ("track broken at x, y, z") when a loop starts (T10) | A broken track gives a clear error | MTR "Refresh Path" `[doc]` | Adapt | S |
| B22.16 | Bulk track placement with `UPDATE_KNOWN_SHAPE`, neighbour updates only at the ends (T10) | A long track placed by a tool causes no update cascade | Rail Optimization `[v]` | Adapt | S |
| B22.17 | Track block extends `BaseRailBlock` and reuses `RailShape` and `AbstractMinecart.exits` for exit vectors (S20 step 2) | Curves and slopes place like rails on both loaders | Vanilla 26.2 `[v]` | Adopt | M |
| B22.18 | Dinosaurs never board a vehicle. A tag `projectnublar:vehicle_may_autoboard`, empty by default | No T. rex in the Jeep | Automobility and Immersive Aircraft auto-board loops `[v]` | Adopt | S |
| B22.19 | Driver controls on change only. Server clamps speed and distance per packet. Lerp over the real update interval | Smooth, cheat-safe driving | Automobility, Immersive Aircraft, Immersive Machinery `[v]` | Adopt | S |
| B22.20 | Send while moving, one stop packet, then silence (T15) | A parked vehicle costs no packets | Simulated `[v]` | Adopt | S |
| B22.21 | `isAlwaysTicking` on the tour vehicle | A tour keeps full speed under IO | IO 26.2 `[v]` | Compat | S |
| B22.22 | Union culling box over the whole model | No pop-out with EntityCulling | Automobility `[v]` | Adopt | S |
| B22.23 | Step height about 1.0 to 1.25 on the Jeep, client-only body tilt from the wheel heights | The Jeep climbs a one-block ledge and looks alive | Automobility (simplified) `[v]` | Adapt | S |
| B22.24 | Handling numbers in one data record per vehicle | Jeep and Explorer share one class | Automobility parts `[v]` | Adapt | S |
| B22.25 | Optional dynamic-light data file for headlights | Headlights light the road where LambDynamicLights is installed | LambDynamicLights data format `[v]` | Compat | S |
| B22.26 | No chunk tickets for tours. An empty loop pauses where chunks stop ticking | No standing chunk cost | MTR, Automobility `[v]` | Adopt | none |
| B22.27 | Vehicles as data on a background thread, standing passengers | — | MTR | Avoid | — |
| B22.28 | Abstract tour data with an entity only near players | — | Create trains, SnR shadow realm | Defer (after Beta) | L |
| B22.29 | Hitbox entities per vehicle box, custom collision and ground scan, boost pads, drift, fuel, upgrades, part-building | — | Automobility, Immersive Aircraft | Avoid | — |
| B22.30 | Physics bodies (ships) | — | VS2, Clockwork, Sable | Avoid. Four passive rules keep Nublar compatible | — |

### Park security and control (backlog items and the B15 switch)

These items have no Beta module. The plan's backlog names security cameras, the enclosure marker and park signs, and gates. The table sets the direction for the day the lead assigns them.

| Id | Technique | In game | Source | Verdict | Effort |
| --- | --- | --- | --- | --- | --- |
| SEC.1 | Mounted camera view: the player's view jumps to a camera, turns in a limited arc, zooms, and exits with Shift | A control-room monitor shows a paddock | SecurityCraft `[v]` | Defer (O14) | M |
| SEC.2 | No forced chunks: a camera in an unloaded area shows "no signal". View radius from a config value (6 to 10 chunks) | The feed drops like in the film. 0 tickets | Own design `[inf]` | Adopt with SEC.1 | S |
| SEC.3 | Live picture on a block (second world render) | — | SecurityCraft frame feed `[v]` | Avoid in Beta. A snapshot every 2 to 5 s later | — |
| SEC.4 | Escape alarm and PA speaker: a rising redstone edge starts a vanilla sound loop driven by scheduled ticks (T14) | A siren sounds across the park when a fence goes down | SecurityCraft alarm `[v]`, Simple Radio idea `[v]` | Adapt | S |
| SEC.5 | Keycard as a data component, reader with a `POWERED` pulse from `scheduleTick` | A lab door opens for a level-3 card | SecurityCraft keycard `[v]` | Defer (gates not in Beta) | S |
| SEC.6 | Owner UUID on control blocks | Other players cannot turn off your fence switch | SecurityCraft `Owner` `[v]` | Defer (O14) | S |
| SEC.7 | Status board as a block with lamp states per section | A wall panel shows section A on, section B off | Own design `[inf]` | Defer (O14) | S |
| SEC.8 | Enclosure detection by a bounded, staggered fill of packed positions (T12) | An enclosure marker shows the paddock area | Ad Astra flood fill `[v]` | Defer (backlog) | M |
| SEC.9 | Motion-light scans, sentries | — | SecurityCraft | Avoid | — |
| SEC.10 | Voice radio | — | Simple Radio (no 26.x) | Avoid in Beta | — |

### B00 Config and feature switches

| Id | Technique | In game | Source | Verdict | Effort |
| --- | --- | --- | --- | --- | --- |
| B00.1 | A `features` section in the TOML. A disabled feature does not tick, subscribe or sense. Its blocks and items stay registered and hidden, and its recipes do not load (T11) | A server owner turns off vehicles or herd travel and the cost goes away. Saves still load | Quark and Zeta `[v]`, Create Connected `[v]` | Adopt | S |
| B00.2 | One data-load condition type on both loaders for recipes of a disabled feature | Recipes vanish with the feature | Quark `FlagCondition` `[v]` | Adopt | S |
| B00.3 | Scan intervals and size caps as server config values from day one | Large servers can tune worst cases | Create `CKinetics`, `CTrains` `[v]` | Adopt | S |
| B00.4 | Id redirects for renamed species and genes | Old saves keep loading after a rename | Tinkers redirects `[v]` | Adopt | S |
| B00.5 | A client-facing projection of species data (size, name, variants) | Smaller sync at join | Ad Astra planet record `[v]` | Adapt | S |
| B00.6 | Integer tiers with one compare helper | One place decides tier gates | Ad Astra `[v]` | Adopt | S |
| B00.7 | Module engine with annotation scan and reflection | — | Zeta | Avoid | — |
| B00.8 | Document tracking ranges, the server broadcast property and Nublar ids for IO, EVD and Chloride in `docs/config-reference.md` | Server owners can tune | Vanilla, IO, EVD `[v]` | Adopt | S |

### B07 Genes as data

| Id | Technique | In game | Source | Verdict | Effort |
| --- | --- | --- | --- | --- | --- |
| B07.1 | One rebuild function for derived stats. It runs on a genome change, a growth step and a datapack reload, with a "data not loaded" guard and a version stamp | An edited animal gets its new stats once. No per-tick stat work | Tinkers `rebuildStats` `[v]` | Adopt | S |
| B07.2 | Gene definitions and effects as JSON with typed effect modules and per-percent scaling | A datapack author edits gene effects | Tinkers modifier modules `[v]` | Defer (O16) | M |
| B07.3 | Cosmetic-only extras (tiny marking offsets) from the entity UUID with no sync | Small per-animal differences at no cost | Quark variant textures `[v]` | Adapt | S |

### Release 1 tutorials and guides

| Id | Technique | In game | Source | Verdict | Effort |
| --- | --- | --- | --- | --- | --- |
| R.1 | Ponder scenes: hold a key over an item and watch the lab, fence or track being built | A player learns the build order in game | Ponder library (MIT) `[v]` | Compat, Defer (no 26.2 build, O20) | M |
| R.2 | Guide book in progression order (first fossil, processor, sequencer, egg, incubator, paddock) as data pages | A book follows the B13 advancements | Ad Astra Patchouli book `[v]` | Compat, Defer (O20) | M |
| R.3 | Machine and fence feedback in the world first (B12.9, B12.10) | Fewer questions before any book exists | Create | Adopt | — |

### Optional compat with tech mods

All rows are optional. Each sits behind one `isLoaded` flag in its own package, outside `common` game logic.

| Id | Integration | In game | 26.2 status | Verdict | Effort |
| --- | --- | --- | --- | --- | --- |
| C.1 | Standard capabilities on machines, feeders and generators | CC: Tweaked, Refined Storage, Create belts and Mekanism cables work | Platform | Adopt (no compat code) | S |
| C.2 | CC: Tweaked park peripheral on the fence switch (`isPowered`, `getBrokenWires`, `getLoad`) and a park tracker (cached dinosaur summary). Push events on change only. One summary call, never one call per post | A player builds a Lua status wall | CC: Tweaked 1.120.2 on both loaders `[doc]` | Compat | M |
| C.3 | CC: Tweaked Fabric energy and fluid generic source | The same Lua script works on Fabric | Same | Compat | S |
| C.4 | Create dynamo (shaft to FE) | Water wheels power a lab | Create Fly only (Fabric) | Defer | M |
| C.5 | Create sequenced-assembly recipes for Nublar parts, data only | An alternate factory route | Create Fly only | Defer | S each |
| C.6 | Create trains as a park monorail: tags so dinosaurs ignore or flee carriages | A Create train runs through the park | Create Fly only | Defer | S |
| C.7 | `create:non_movable` tag on fence posts, generators and machines | Contraptions cannot carry a live fence away | Create Fly only | Defer | S |
| C.8 | Create goggles on Nublar blocks | — | — | Avoid | L |
| C.9 | Refined Storage 2 | DNA items move through a storage network | 26.1.2 only | Compat (free through C.1) | none |
| C.10 | Team Reborn Energy on Fabric (through Balm) | CCA connectors and Fabric tech cables power Nublar | 5.0.0 `[doc]` | Compat | none |
| C.11 | Ponder plugin | See R.1 | No 26.2 | Defer | M |
| C.12 | Lithium `lithium:options` entry | Disables one Lithium group if a playtest finds a conflict | Lithium 26.2 | Compat (reserve) | S |
| C.13 | Quark sort-lock interface on the sequencer screen | Quark's sort button skips drive slots | Quark 1.21.1 only | Defer | S |
| C.14 | Tinkers material files for bones | — | Tinkers 1.20.1 only | Defer | S |

## Defects that the researchers found in Nublar's code

**In game:** each row starts with what a player sees or what a server owner measures. The cause follows. Paths are under `common/src/main/java/net/dumbcode/projectnublar/` (`J/`) on `kash's-slop` at 0c97bde plus the uncommitted tree. This report read the rows marked "re-checked" again `[v]`.

| # | In-game effect | Cause | Location | Owner |
| --- | --- | --- | --- | --- |
| 1 | A powered fence post top makes a shadow of 10 to 15 light levels instead of glowing `[inf]`. Verify in game | The post overrides `getLightBlock` (light dampening). Emission needs `lightLevel` on the block properties | `J/block/ElectricFencePostBlock.java:275-276` (re-checked) | B15 |
| 2 | Every fence post runs a tick on the client and on the server, every tick, also when nothing changes | `getTicker` returns the ticker whenever the server and client types match | `J/block/ElectricFencePostBlock.java:300-305` (re-checked) | B14, B15 |
| 3 | Rare `ConcurrentModificationException` or a hang in single player when two threads ask for a machine shape | A public static `HashMap` filled with `computeIfAbsent` inside `getShape` | `J/block/api/MultiEntityBlock.java:35,83` (re-checked) | B12 |
| 4 | Client memory grows while wires are on screen. Each drawn cuboid allocates a key | `NORMAL_CACHE` keeps one entry per distinct triangle and never clears | `J/block/api/MathUtils.java:121,133` (re-checked) | B15 |
| 5 | Each synced genome change sends the whole genome as NBT. A copy with equal values is sent again | `DINO_DATA` writes `buf.writeNbt(dinoData.toNBT())`. `DinoData` has no `equals` | `J/init/DataSerializerInit.java:14-24`, `J/api/DinoData.java` (re-checked: no `equals`) | B16, P2 |
| 6 | Hundreds of fence wire blocks do useless work on each neighbour change | An empty `neighborChanged` override reads a block state and discards it | `J/block/ElectricFenceBlock.java:88-91` (re-checked) | B15, P2 |
| 7 | 100 animals near feeders print about 100 log lines per second | `System.out.println` in the feeder sensor and 11 more print calls (12 in 6 files) | `J/entity/ai/sensors/NearestFeederSensor.java:68`, `J/entity/ai/tasks/WalkToNearestFeeder.java:63`, `J/util/DinoNeedsUtils.java:163-167,250`, and others (re-checked: 12) | B17, B18, B25 |
| 8 | Load errors print `{}` instead of the file name | String concatenation with SLF4J placeholders | `J/data/BehaviourDataReloadListener.java:107,112` | B00 |
| 9 | Under IO, a far herd member never gets a fallback path | A queued request expires one game tick after the last ask. A throttled member asks every 2nd to 20th tick | `J/entity/ai/behaviour/social/FallbackBudget.java:30` (re-checked) | B26 |
| 10 | Under IO, far animals recover stamina and lose needs at a fraction of the normal rate. Checks "every second" run every N seconds | Timers count `tickCount`, which stops while IO skips the entity | `J/entity/dinosaur/Dinosaur.java:581,723,760-765`, `DinoOneShots.java:70` | B17, B25 |
| 11 | A Brachiosaurus neck and head vanish when its small box leaves the screen or hides behind a hill | Culling uses the hitbox `sized(2, 3)` | `J/init/EntityInit.java:36` (re-checked) | B16 (issue #63) |
| 12 | All six species use the hostile mob category and one tracking range of 160 blocks | `MobCategory.MONSTER` and `clientTrackingRange(10)` on every species | `J/init/EntityInit.java:30-39` (re-checked) | B16 |
| 13 | A wire disappears while its span is on screen, when both posts are off screen | Wires draw from the post's block-entity renderer with no box over the span | `client/renderer/ElectricWireRenderer.java:25-45`, `ElectricFenceRenderer.java:17-50` | B15 |
| 14 | 300 posts submit 300 or more block-entity renders each frame, also when nothing moves `[inf]` | Every fence and machine block uses `ENTITYBLOCK_ANIMATED` | `J/block/api/MultiEntityBlock.java:92-93`, `DinosaurFeederBlock.java:105-106`, `ElectricFenceBlock.java:37-38`, `ElectricFencePostBlock.java:73-74` | B12, B15 |
| 15 | Each colour-wheel screen allocates a 400 by 400 texture (640 KB of native memory) | A new `DynamicTexture` per widget | `J/client/widget/ColorWheelWidget.java:45,110` | B08 |
| 16 | A data race is possible if worldgen reads fossil maps during a reload or with C2ME | Public static mutable `HashMap`s used by placement | `J/api/FossilCollection.java:20-22`, `J/ProjectNublar.java:15` | B01 |
| 17 | Start-up and memory grow with 910 fossil blocks for two species | One block per host stone, quality and piece | `J/api/FossilCollection.java:35-52` | B01 |
| 18 | The 26.x render box of the processor is lost at the port | `getRenderBoundingBox` sits on the block entity (the 1.20.1 Forge place). On 26.x it lives on the renderer | `J/block/entity/ProcessorBlockEntity.java:324-326` | P2 |
| 19 | No effect today. `toml4j` is declared and never used | Build files only | `common/build.gradle:23`, `fabric/build.gradle:18` | P1 |
| 20 | The feeder's third food slot is skipped | The slot switch uses `case 3` and no `case 2` (already in plan B20: "slot bug") | `J/block/entity/DinosaurFeederBlockEntity.java:44-46,138-140` | B20 |

One claim from the notes is false. `FallbackBudget` does forget a member that leaves: `forget(UUID)` exists at line 66 and `DinoGroup.java:205` calls it `[v]`. It is not a leak.

## The plan covers the direction, and 38 items are new

**In game:** nothing here changes a decided feature. The new items make decided features cheaper, add feedback, or remove a defect that the plan does not name.

### What the plan already covers

| Module | The plan already asks for | The new notes add |
| --- | --- | --- |
| P2 | Balm for energy and fluids, GeckoLib 5, SBL 2, reload listeners through a platform service | The 26.x render-state split, `ValueInput` and `ValueOutput`, data components, render boxes on renderers, Balm 26.2.0.9 exists `[v]` |
| B00 | One TOML, species JSON, `config-reference.md` | Feature switches, redirects, a client projection of species data |
| B01 | Position-seed looks, generator `RandomSource` | State-count math, block-count collapse, C2ME rules |
| B12 | Assembly, tabs, zero idle draw, item retention | Issue set, fault effects, hover box, save and sync split, part capabilities without block entities |
| B14 | `PowerNetwork` helper, Balm bridge, balance | Network merge and split, no post ticks, queued loads, energy-per-fuel constant, chunk-reload GameTest |
| B15 | Wire health, switch, signs, rft renderer | Geometry tiers, health bands, wire events, light fix, spool messages |
| B20 | Hopper input, three feeders, live bait | Adaptive poll, plan-simulate-apply bait loading, cached diet arrays |
| B21 | Maps to digsites, `/locate` within 2,000 blocks | Vanilla map classes, spacing rule, seed-plus-overrides storage |
| B22 | Jeep, Explorer, track, stops, driverless loop | Stop-for tag, horn, redstone hold, dwell and announcements, no auto-board, speed clamp |
| B25 | Profile, no prints, dead code | Optimization-mod test matrix, logging rules, game-time timers, client tick audit |

### New items for the plan

| # | Module | Addition | From |
| --- | --- | --- | --- |
| 1 | P2 | Record in the port task plan that Balm 26.2.0.9 bridges to NeoForge `EnergyHandler` (not `IEnergyStorage`) and to Team Reborn Energy only when it is present | Facts #3 to #5 |
| 2 | P2 | Move every block-entity render box to its renderer. Fabric: decide between the EntityCulling interface and `shouldRenderOffScreen` per renderer | Defect #18, platform table |
| 3 | P2 | Port `neighborChanged` to the `Orientation` signature and delete the empty override | B15.15 |
| 4 | P2 | Genome and drive data as components with `StreamCodec`. `DinoData` gets `equals` | Rank 8, defect #5 |
| 5 | P2 | Add a `"version"` integer to every custom NBT root | OM7 |
| 6 | P2 | Remove `toml4j` | Defect #19 |
| 7 | B00 | `features` section and one data-load condition type | B00.1, B00.2, T11 |
| 8 | B00 | Id redirects, scan intervals and caps in config, the client projection | B00.3 to B00.5 |
| 9 | B00 | `config-reference.md` lists tracking ranges, the broadcast property and Nublar ids for IO, EVD and Chloride | B00.8 |
| 10 | B01 | Unit test that counts block states per block and the registered fossil block total | OM1, OM2 |
| 11 | B01 | Immutable worldgen maps, region-only placement, a 200-chunk C2ME run on Fabric | OW1 to OW5 |
| 12 | B06 | Compact drive component and a size target (O18) | ON1 |
| 13 | B07 | One stat rebuild function with a version stamp | B07.1 |
| 14 | B09 | Shared static render types, no core shader, glow layer named without `_e` | OC3, OC6, ETF duty |
| 15 | B10 | Bioluminescence as an emissive layer only | OC8 |
| 16 | B12 | Issue set, fault effects and hover box | B12.8 to B12.10, T6, T7 |
| 17 | B12 | Save and sync split, coalesced sync, 60-tick turn-off delay | B12.11 |
| 18 | B12 | Chip curve with one config value M | B12.12 |
| 19 | B12 | Part blocks with no block entity and block-only capabilities. Render box over the whole machine | B12.14, B12.15 |
| 20 | B12 | Idle machines as static models (needs idle-pose exports in B24) | B12.13, T5 |
| 21 | B12 | Fix the static shape map | Defect #3 |
| 22 | B14 | Network object with merge, split, queued loads and a heartbeat. No post ticks | B14.6 to B14.9, T1 |
| 23 | B14 | Energy-per-fuel constant and generator comparator output | B14.10, B14.11 |
| 24 | B14 | Chunk-reload GameTest for a 100-post line | B14.15 |
| 25 | B15 | Fence geometry tiers with a shared `FenceVisual` record | B15.7, T4 |
| 26 | B15 | Health bands, wire events, per-tier config block, spool messages | B15.8, B15.9, B15.11, B15.12, T2 |
| 27 | B15 | Post light through `lightLevel`. Remove `NORMAL_CACHE` | Defects #1, #4 |
| 28 | B15 | Decorative fence and sign variants with no block entity | B15.16 |
| 29 | B16 | Culling box from the posed, scaled body. Tracking range by body size. `MobCategory.CREATURE` as the earlier survey's B16.1 says | Defects #11, #12, ON9 |
| 30 | B16 | Client-only effects in `aiStep` or server events. Audit `Dinosaur.tick` | Rank 7 |
| 31 | B17 | Game-time timers for needs, cooldowns and memories that matter for play | OS4, defect #10 |
| 32 | B20 | Adaptive poll, plan-simulate-apply bait loading, cached diet arrays | B20.10 to B20.12 |
| 33 | B21 | Vanilla exploration map function and trade, digsite spacing rule | B21.10, B21.11 |
| 34 | B22 | Stop-for tag, horn, redstone hold, dwell, announcement, loop check | B22.11 to B22.13, B22.15, T10, T13 |
| 35 | B22 | No dinosaur boarding, speed clamp, union culling box, `isAlwaysTicking` | B22.18 to B22.22 |
| 36 | B25 | Distance band inside SBL | OS6, T9 |
| 37 | B25 | Optimization-mod test matrix: Sodium, Iris, EntityCulling, ImmediatelyFast, Lithium, IO, C2ME, Alternate Current, Particle Core, "Prefer Vulkan" | Compatibility checklist |
| 38 | B26 | `FallbackBudget` tolerates gaps. Stall thresholds above 20 ticks or scaled by elapsed time | OS5, defect #9 |

## Clean-room implementation specifications

**In game:** each specification describes a behaviour that the player sees. It gives enough detail to build the behaviour without the source code.

These 15 specifications cover Adopt and Adapt rows with a restricted precedent. A restricted precedent limits reuse, or has a licence that is not MIT, MPL-2.0, LGPL, Apache-2.0 or CC0. On 2026-10-10 the lead set the rule for restricted repositories: study the repository, then build Nublar's own solution with the same function. The Create repository asks AI assistants not to work on its code. The lead told the survey to read it anyway and to write specifications, not copies. These rows need no specification:

- rows that cite a repository only as a pattern to avoid
- Defer and Avoid rows
- rules that the platform or a library already enforces (render boxes, `StreamCodec`, capabilities)
- rows whose precedents are all permissive (Mekanism, RS2, SecurityCraft, CC: Tweaked, Automobility, MTR, Tinkers)

T13 and T14 have permissive precedents. This report includes them because each one merges several designs into one Nublar behaviour.

Build each one from the description below. **Specify, do not copy.** Start values are proposals for playtest unless the table names a source.

### T1. Power network object (B14.6 to B14.9, rank 2). Sources: Create kinetic network, IE wire network

**In game:** a fence section and its generators act as one grid. When fuel runs out, the whole section goes dark at once. When the player places a post, it joins the grid with no lag.

- **Inputs:** connect and disconnect events from posts, pylons, conduits, generators and machines. Energy offered by generators. Draw per consumer (`fences.draw.<tier>`, `power.machine_draw.<machine>.<tier>`).
- **State:** per network:
  - a set of member positions (packed `long`)
  - a supply sum, a draw sum and a buffer amount
  - a `dirty` flag, a `powered` flag and a version counter
- **Saved state:** each member block entity saves its own stored energy. Nothing saves the network.

1. A placed member looks at its connected neighbours. With no network next to it, it creates a new network. With one, it joins it. With several, the largest network adopts the others and adds their buffers.
2. A removed member marks its network invalid. On the next tick, a flood fill from each remaining neighbour builds the new networks. Each member gives its stored energy to its new network.
3. Several changes in one tick go into one set and run as one rebuild in the next tick.
4. A member that loads with its chunk goes into a load queue. The queue joins all queued members once per tick.
5. Each tick, a level-wide manager visits each network once. A network that is not dirty and has no consumer that changed skips the sums.
6. A dirty network sums supply and draw again, then clears `dirty`.
7. The network takes `min(supply, draw)` from generators in one call each, and fills the buffer with any rest up to its capacity.
8. If supply plus buffer covers draw, `powered` stays true. If not, `powered` turns false. A change of `powered` raises the version counter and fires one event to the network's posts, machines and signs.
9. Every 60 ticks, offset by the network id, the manager checks one random member against the world. If the member is gone, the network becomes dirty.
10. A network whose `powered` value flips more than a set count in a time window holds `false` for a cool-down. This stops an oscillating outage.

| Value | Start | Basis |
| --- | --- | --- |
| Heartbeat | 60 ticks | Create `kineticValidationFrequency` `[v]` |
| Network buffer capacity | Sum of member buffers (a post holds 350 FE today) | Local code `[v]`, open item O8 |
| Flicker limit | 4 flips in 100 ticks, hold 100 ticks | `[inf]` |
| Load queue | All queued members once per tick | IE `[v]` |

### T2. Wire events and health bands (B15.8, B15.9). Source: IE wire sync

**In game:** walk up to a paddock and every wire appears. An animal hits a wire and cracks show. A cut wire disappears for every nearby player at once.

- **Inputs:** connect, break and repair of a wire. Wire health changes. A player starting or stopping to watch a chunk.
- **State:** per wire on the server: two end positions, tier, exact health. Per wire on the client: two end positions, tier, health band.

1. On connect, break or repair, send one event (`ADD`, `REMOVE` or `UPDATE`) to players who track either end chunk.
2. When a player starts to watch a chunk, send all wires with an end in that chunk in one batch.
3. Keep a per-player set of watched chunks. Send a wire when its second end chunk becomes visible.
4. Map exact health to a band: intact, damaged, nearly broken, broken. Send `UPDATE` only when the band changes.
5. The client rebuilds the wire visual from the two ends and the tier, then marks the touched sections dirty.

| Value | Start | Basis |
| --- | --- | --- |
| Payload per wire | About 20 bytes (two positions, tier byte, band byte) | `[inf]` |
| Bands | 4 (above 66 %, above 33 %, above 0 %, 0 %) | `[inf]` |

### T3. Shock that follows the grid (B15.10, after open item O9). Source: IE wire damage

**In game:** a fence on a full grid hits hard. A fence on a weak or empty grid hits weakly or not at all. A large animal that keeps pushing a live wire drains the generator, and the fence can brown out.

- **Inputs:** an entity inside a live wire block. The network's available energy this tick. The tier's damage and transfer rate.
- **State:** per network: energy used by shocks this tick.

1. On contact, test the entity box, inflated by the tier's damage radius, against the wire segment inside this block.
2. Read the energy the network can still deliver this tick, capped at the tier's transfer rate.
3. Damage = `fences.damage.<tier>` × (available ÷ transfer rate), clamped to 0 to 1 for the factor.
4. Take energy from the network: `available × damage done ÷ damage possible`.
5. Apply one shock per entity per 10 ticks (game time), so a body with many parts takes one shock, not one per part.

| Value | Start | Basis |
| --- | --- | --- |
| Damage radius | Low security 0.1 block, high security 0.3 block | IE electrum and steel `[v]` |
| Shock interval per entity | 10 ticks | `[inf]` |

### T4. Fence geometry tiers (B15.7, rank 1). Sources: IE section geometry, Sodium meshing

**In game:** a paddock of hundreds of posts and wires draws like ordinary blocks. Sparks and a snapping wire still show.

- **Inputs:** post block states (tier, height index, powered), the wire connections of each wire block, the health band (T2).
- **State:** an immutable `FenceVisual` record per wire block (connections, tier, band). Model data on NeoForge, render data on Fabric.

1. Draw posts, insulators and signs from block models in blockstate JSON.
2. Each wire block draws only its own wire segment from its `FenceVisual`. No strip crosses a section border.
3. On NeoForge, read `FenceVisual` from model data in a delegating block model. On Fabric, read it from `RenderDataBlockEntity` in `emitQuads`. Shared geometry code lives in `common`.
4. Change model data only for slow state: connection layout, tier, band, a fallen post. Request one model update per change.
5. Never put a fast state in the model. A powered tint changes at most once per second (debounce).
6. Sparks, arc flashes and a snapping wire are particles or a short block-entity effect while active.
7. A block-entity renderer that remains returns a box over its span and never sets `shouldRenderOffScreen`.

| Value | Start | Basis |
| --- | --- | --- |
| Powered tint debounce | 20 ticks | `[inf]` |
| Segments per short wire | 4 to 6 | CCA uses 24, too many for short spans `[v]` `[inf]` |

### T5. Idle machines as static models (B12.13). Sources: Chloride solid chests, EBE and BBE

**In game:** an idle lab looks the same as before, but costs no block-entity draw. When a machine starts, its moving parts appear and animate.

- **Inputs:** the machine's active flag and its near-player state.
- **State:** a block state property `ANIMATED` (boolean).

1. The block uses `RenderShape.MODEL` with a baked idle model per upgrade look.
2. When work starts, set `ANIMATED` true. The GeckoLib renderer draws and hides the bones that the static model already shows.
3. When work stops, wait for the turn-off delay (B12.11), then set `ANIMATED` false. The renderer submits nothing.
4. Change `ANIMATED` only on these edges, never per tick.

| Value | Start | Basis |
| --- | --- | --- |
| Turn-off delay | 60 ticks | Mekanism `blockDeactivationDelay` `[v]` |

### T6. Hover box for machines, generators and feeders (B12.10). Source: Create goggle overlay

**In game:** the player looks at a machine and a small box near the crosshair shows its state, progress and fault. Looking at a generator shows supply and draw.

- **Inputs:** the client's hit result. Synced fields on the block entity.
- **State:** none beyond synced fields: active, progress percent, issue mask, and for generators a supply and draw summary from a short cache.

1. Each tick, the client reads the block entity under the crosshair.
2. If it implements Nublar's info interface, it asks for lines. The block entity builds them from synced fields only. It never asks the server.
3. The box slides in after a short look and hides when the crosshair moves away.
4. A network summary (supply, draw, post count) comes from a server cache with a 20-tick life. The server sends it on menu open or when its value band changes.
5. A client config sets the box offset and can turn the box off.

| Value | Start | Basis |
| --- | --- | --- |
| Look delay before the box | 5 ticks | `[inf]` |
| Summary cache life | 20 ticks | Create logistics cache `[v]` |

### T7. Machine issues and fault effect (B12.8, B12.9). Sources: Create overstress feedback, Mekanism issues

**In game:** a machine with a problem shows a warning tab with the reason. In the world it puffs smoke and goes dark. When the problem clears, it puffs once and starts again.

- **Inputs:** slot changes, energy state, tier changes.
- **State:** an `int` issue mask. Bits: NO_POWER, NO_INPUT, OUTPUT_FULL, WRONG_FILTER, NO_BULB, NO_BEDDING, NO_EGG, SPECIES_MISSING.

1. Compute the mask only on a slot change, a power event (T1) or a tier change.
2. Put the mask in one `ContainerData` slot while a menu is open.
3. The screen shows a warning tab when the mask is not 0, with one line per bit.
4. When the mask changes from 0 to NO_POWER, sync one state change. The client plays a smoke puff and shows the dark look.
5. When it changes back to 0, the client plays one recovery puff.

| Value | Start | Basis |
| --- | --- | --- |
| Smoke puffs on fault | 3 over 20 ticks | `[inf]` |

### T8. Pooled ambient sound (OC11, rank 12). Source: Create SoundScapes

**In game:** a long live fence hums and a working lab whirrs. Many sources make one sound, not hundreds.

- **Inputs:** each client tick, sources call `play(group, pos, pitch)`.
- **State:** per group and pitch bucket: a count of positions within range and one looping sound instance.

1. Sort each source into a group (fence hum, machine whirr, generator) and one of five pitch buckets.
2. Count sources within 16 blocks of the camera per group and bucket.
3. Keep one loop per non-empty group and bucket. Set its volume from the count, capped.
4. Every 5 ticks, update counts and stop loops whose count is 0.
5. A client option turns ambient sound off.

| Value | Start | Basis |
| --- | --- | --- |
| Range | 16 blocks | Create `[v]` |
| Update period | 5 ticks | Create `[v]` |
| Pitch buckets | 5 | Create `[v]` |

### T9. Distance band inside the brain (OS6, B25.9). Source: Immersive Optimization

**In game:** dinosaurs far from every player think less often. They still walk at full speed and their needs still change at the normal rate.

- **Inputs:** distance to the nearest player, the dinosaur's target, threat and fence memories.
- **State:** a field `band` (0, 1 or 2).

1. A cheap sensor every 20 ticks sets `band` from `getNearestPlayer(x, y, z, 128, false)`: 0 within 64 blocks, 1 within 128, 2 beyond or none.
2. If the dinosaur has an attack target, a threat memory or a fence contact, set `band` to 0.
3. Sensors use `scanRate(e -> base + band × base + (id & 15))`.
4. Idle behaviours (wander, graze, look) use `cooldownFor` that grows with `band`.
5. Needs use game time (OS4), so they do not depend on `band`.

| Value | Start | Basis |
| --- | --- | --- |
| Band edges | 64 and 128 blocks | `[inf]`. 128 keeps VMP's fast path `[v]` |
| Idle cooldown at band 2 | 100 ticks | `[inf]` |

### T10. Track placement and loop check (B22.15, B22.16). Source: Rail Optimization

**In game:** a long tour track placed by a tool or a structure appears at once with no lag. When the player starts a tour on a broken loop, the game names the broken spot.

- **Inputs:** a list of track positions and shapes to place. The start position of a tour.
- **State:** none kept. The Explorer reads the rail shape under it on a block change (S20, S3).

1. Place all track blocks with flags that skip neighbour and shape updates.
2. After the last block, send neighbour updates around the two ends of the run only.
3. To start a tour, walk the track from the start using each block's exits until the walk returns to the start.
4. If a step finds no track, stop and show "Track broken at x, y, z" to the player.
5. Cap the walk at a config length.

| Value | Start | Basis |
| --- | --- | --- |
| Loop walk cap | 4,096 blocks | `[inf]` |

### T11. Feature switches (B00.1, B00.2). Sources: Quark and Zeta, Create Connected

**In game:** a server owner turns off a costly system in the config, for example vehicles. The vehicles vanish from creative tabs and recipes, and they cost nothing per tick. Old worlds still load.

- **Inputs:** a `features` section in the TOML.
- **State:** a `NublarFeatures` record read at start-up.

1. Every content registers always, whatever the switch says.
2. A disabled feature's tick paths return at their first line. Its event handlers are not registered. Its sensors are not added.
3. Its items do not show in creative tabs.
4. Its recipes, loot additions and advancements carry a data condition that reads the switch.
5. The server's switch values win over the client's.

| Value | Start | Basis |
| --- | --- | --- |
| Switches | `vehicles`, `herd_travel`, `fence_wire_health`, `breeding`, all true | Open item O15 |

### T12. Bounded enclosure fill (SEC.8, backlog). Source: Ad Astra flood fill

**In game:** an enclosure marker shows the area of a paddock. A gap in the fence shows as an area that leaks.

- **Inputs:** a start position, a block limit, a passability rule from tags.
- **State:** a primitive set of packed positions per marker, stored with the chunk or the marker.

1. Run a breadth-first fill from the start with packed `long` positions and one reused mutable position.
2. Stop at the block limit. A fill that reaches the limit reports "open".
3. Passable: air, blocks tagged passable, and non-full shapes that do not seal a face. Fence wires count as sealing for this test.
4. Run again every period, with an offset from the marker position.
5. Remove cleared entries. Never reset them to a default value in a level-wide map.
6. Sync the result to clients only after a change, and only when a player turns on the overlay.

| Value | Start | Basis |
| --- | --- | --- |
| Block limit | 6,000 | Ad Astra `maxDistributionBlocks` `[v]` |
| Period | 100 ticks | Ad Astra `[v]` |

### T13. Tour stops and blocked track (B22.11, B22.13). Sources: Automobility autopilot (MIT), MTR (MIT), own design for the merge

**In game:** the Explorer stops when a dinosaur or a player stands ahead, waits, and honks if the wait goes on. At each stop it waits, plays a chime and shows a line such as "Stop 2 of 4: Triceratops paddock".

- **Inputs:** the tag `projectnublar:tour_vehicle_stops_for`, stop markers with optional dwell and announcement, the redstone state of a marker.
- **State:** per vehicle: blocked ticks, horn count, current stop index.

1. Every 10 ticks, test a box 4 blocks ahead on the track for entities in the tag.
2. If blocked, stop. After 60 ticks, sound the horn with a growing chance. Stop sounding after 5 horns.
3. When the box is clear, start again.
4. At a stop marker, stop for its dwell time (or `vehicles.stop_seconds`). Play its sound and show its text to riders.
5. A powered marker holds the vehicle until the signal ends.

| Value | Start | Basis |
| --- | --- | --- |
| Blocked check | 10 ticks | Earlier design `[inf]`. Automobility checks every tick `[v]` |
| Look-ahead box | 4 blocks | Automobility `[v]` |
| Horn delay | 60 ticks | `[inf]` |

### T14. Alarm and PA speaker (SEC.4). Sources: SecurityCraft alarm (MIT), Simple Radio (GPL-3.0) idea

**In game:** a siren sounds across the park when a fence section loses power or a lever turns on. A PA speaker plays "Feeding time" or a tour chime.

- **Inputs:** a redstone signal, or a park event (section off) from T1.
- **State:** block state `POWERED`. A sound id and range per block.

1. On a rising edge, set `POWERED` and play the sound with vanilla attenuation at the block.
2. Schedule a block tick for the sound length. On the tick, play again while `POWERED` stays true.
3. On a falling edge, clear `POWERED` and stop scheduling.
4. No block-entity tick and no custom packet.

| Value | Start | Basis |
| --- | --- | --- |
| Range | 64 blocks | `[inf]`. SecurityCraft allows up to 100 `[v]` |

### T15. Movement sync with a stop packet (B22.20). Source: Simulated rope tracking

**In game:** a driven or touring vehicle moves smoothly for watchers. A parked vehicle sends nothing.

- **Inputs:** the vehicle's position and rotation each tick.
- **State:** a `moving` flag and the last sent state.

1. While the vehicle moves, let vanilla tracking send position at the type's update interval.
2. Send Nublar's extra state (wheel angle source, lights, horn) only when it changes.
3. When the vehicle stops, send one final state and set `moving` false.
4. While `moving` is false, send nothing until the vehicle moves again.

| Value | Start | Basis |
| --- | --- | --- |
| Update interval | 3 ticks | Automobility `[v]` |
| Tracking range | 10 chunks | Automobility `[v]` |

## Facts that changed or were corrected

These facts differ from the earlier survey, the plan, a note, a store page or a wiki. Code wins over documentation where both exist.

| # | Topic | Earlier record | Finding | Label |
| --- | --- | --- | --- | --- |
| 1 | Driverless vehicles | Earlier survey, feature 7: no surveyed mod runs a vehicle with no driver. Nearest piece: the vanilla minecart | Automobility's Autopilot attachment drives a car with no player, steered by sign blocks, and brakes for entities. MTR trains run with no driver by design | `[v]` `[doc]` |
| 2 | Track-following vehicles | Earlier survey B22.7: the AC mag-lev follower is the only track-following code found | MTR, Create trains and Automobility signs also follow a route. Vanilla 26.2 `AbstractMinecart.exits(RailShape)` gives exit vectors | `[v]` |
| 3 | Balm on 26.2 | Machines note: the Balm clone had only `26.3` | Balm has a `26.2` branch with `version = 26.2.0.9`, last commit `ae5a2fd` on 2026-09-25. The clone was single-branch. Modrinth lists `26.2.0.9+fabric-26.2` | `[v]` `[doc]` |
| 4 | Balm's NeoForge bridge | Plan P2: Balm bridges to `IEnergyStorage` and `IFluidHandler` | NeoForge 26.2 replaced those with `EnergyHandler` and `ResourceHandler` and marks the old ones deprecated for removal. Balm's `NeoForgeEnergyStorage` implements `EnergyHandler` | `[v]` |
| 5 | Balm on Fabric | Plan P2: Balm bridges to Team Reborn Energy | True only when Team Reborn Energy is installed. Balm does not bundle it. Without it, Fabric players' cables see no Nublar energy | `[v]` |
| 6 | Fabric energy API | Assumed by some notes | Fabric API 26.2 has no energy API. Team Reborn Energy is the convention | `[v]` |
| 7 | Tracking range in code | Baseline note: only the first two species set `clientTrackingRange(10)` | All six species set `clientTrackingRange(10)` | `[v]` re-checked |
| 8 | Mob category in code | Earlier survey B16.1 recommends `CREATURE` | All six species are `MobCategory.MONSTER` today | `[v]` re-checked |
| 9 | `FallbackBudget` leak | Memory note: entries are never removed | `forget(UUID)` exists and `DinoGroup.java:205` calls it | `[v]` re-checked |
| 10 | Create on 26.x | Create core note: no Create build on 26.x | Official Create has none. Create Fly, a separate Fabric rewrite (CC0-1.0), has a 26.2 build, and add-on ports target it | `[v]` `[doc]` |
| 11 | Embeddium | Expected to follow Sodium | No 26.x build. Newest file is for 1.21.4 | `[doc]` |
| 12 | Vulkan | VulkanMod is the Vulkan path | Vanilla 26.2 ships a Vulkan backend. Sodium offers "Prefer Vulkan". VulkanMod has no 26.2 build | `[v]` `[doc]` |
| 13 | ModernFix | Expected on 26.2 | Newest file is for 26.1.2. Branch `26.1` builds NeoForge only | `[v]` `[doc]` |
| 14 | FerriteCore model savings | README lists model deduplication | Version 8.0.0 removed all model optimizations on 26.x | `[doc]` |
| 15 | Lithium and AI | Expected to speed up mob AI | SBL 2 runs its own `tick`, so Lithium's brain caches do not apply to dinosaurs | `[v]` |
| 16 | Lithium hopper sleep | Expected for any container | Only for named vanilla types. A hopper next to a modded container cannot sleep | `[v]` |
| 17 | Chloride | README: culls rendering and ticking | Code culls rendering only | `[v]` `[doc]` |
| 18 | IO opt-out | 1.20.1 branch reads `noCulling` | The 26.2 branch reads `isAlwaysTicking()` | `[v]` |
| 19 | IO effect | Earlier survey B25.9: "needs keep ticking" without observers | IO skips the whole entity tick, so tick-counted needs slow down. Needs must use game time to keep the rule | `[v]` |
| 20 | Sodium chunk hooks | Assumed equal on both loaders | Sodium on NeoForge runs `AddSectionGeometryEvent`. Sodium on Fabric returns an empty list for that path. Fabric gets per-block geometry through FRAPI `emitQuads` | `[v]` |
| 21 | Iris with Sodium 0.9.1 | — | The release note says Iris 1.11.1 does not work with Sodium 0.9.1 on 26.2. Use matching builds | `[doc]` |
| 22 | "Stfu" | Brief: a log silencer | A client quality-of-life mod. It silences one narrator error line only | `[v]` |
| 23 | `fzzyhmstrs/pc` | Unknown | Particle Core, a particle culling mod. Modrinth lists Fabric, NeoForge and Quilt on 26.2, not Fabric only | `[v]` `[doc]` |
| 24 | Common Networking | Baseline note: the old artifact must go | Common Network has a 26.2 build (`26.2-1.1.0`). The plan keeps it | `[doc]` |
| 25 | Library roles | Baseline note: Balm can replace config, networking and registration | The lead decided on 2026-10-06: Balm for energy and fluids only. Architectury, Common Networking and Forge Config API Port stay. All have 26.2 builds | `[doc]` |
| 26 | MTR versions | GitHub `master` targets 1.20.4 | Modrinth also has a NeoForge 1.21.4 beta (4.1.0-beta.2) | `[v]` `[doc]` |
| 27 | Refined Storage 2 | Expected on 26.2 | Newest file is for 26.1.2 | `[doc]` |
| 28 | Flywheel and Ponder | Expected with Create | Newest branches are 26.1.2 and 26.1. Ponder pins one exact Minecraft version | `[v]` |
| 29 | SBL sensor offsets | Earlier survey: offsets not traced | The `SmartBrain` constructor calls `randomlyDelayStart` for each sensor. Whether this moves the scan time depends on vanilla code that was not read | `[v]` `[inf]` |
| 30 | Create stress rule | Config comment: impact doubles with each speed step | Code is linear in speed | `[v]` |
| 31 | CCA wire cap | Wire types list a transfer cap | No code applies the cap to flow `[inf]`. Connector limits come from config | `[v]` |
| 32 | Tinkers smeltery | Javadoc: inner limit 9. Comment: upward probe every 15 s | Code: inner limit 14, probe every 10 s | `[v]` |
| 33 | EntityCulling block-entity frustum test | Default on | The author said he will turn it off by default (issue #313) | `[doc]` |
| 34 | Ad Astra oxygen store | — | Entries are reset, never removed, and the store is always dirty | `[v]` |

## Open items for the lead

These 15 items are real design gaps. Each has a default that ships as a config value or a documented rule if the lead does not answer. They continue the earlier survey's O1 to O6. This list merges duplicates across notes. Questions that a module owner can settle are in "New items for the plan" instead.

| Id | Module | Question | Default if unanswered | Why it matters |
| --- | --- | --- | --- | --- |
| O7 | B14 | What powers a fence when its generator's chunk is not loaded? | The network counts only loaded generators. Fences with no loaded supply show unpowered. No chunk tickets. Docs tell players to keep generators near paddocks | A player at one end of a big park can lose a far fence |
| O8 | B14 | How long does a fence stay live after fuel ends? | A buffer equal to the sum of post buffers (350 FE each today), as a config value. The fence fades over seconds | An instant outage and a slow fade feel different |
| O9 | B15 | Does shock strength follow the grid's energy, and does a shock drain the grid (T3)? | No. Fixed `fences.damage.<tier>`. Shocks draw no energy | It turns weak grids into a real danger and gives the large generator a role |
| O10 | B14 | Which Nublar blocks accept energy from, or give energy to, other mods' cables? | Generators give. Machines accept. Posts, pylons and conduits are closed to outside cables | A Mekanism cable next to a post could drain a fence |
| O11 | B22 | What stops the Explorer, how many seats each vehicle has, may a player board a moving Explorer, and is a timed Tour Station in Beta? | Stops for all dinosaurs and players (tag). Jeep 2 seats, Explorer 4. Boarding only at a stop. No Tour Station: the driver starts the loop | It sets the tour's feel and its acceptance test |
| O12 | B22, B15 | What happens when a vehicle meets an electric fence? | Vanilla collision stops it. No shock to the vehicle or riders | The fence design does not say |
| O13 | B12, B14 | Is the hover box always on, or shown only with a worn item? Is a power meter block in Beta? | Always on, with a client switch. No meter block. Generators give comparator output | It decides B12 and B14 UI work |
| O14 | Backlog | What does the control room have in Beta: status board, alarms, cameras, ownership? Camera route: a camera entity with mixins, or the player's body moved to the camera? | Beta has the switch block, comparator outputs and the alarm block (T14). Cameras, status board and ownership wait. A later camera shows "no signal" outside loaded chunks | It decides whether security gets a module |
| O15 | B00 | Which systems may a server owner turn off? | `vehicles`, `herd_travel`, `fence_wire_health`, `breeding`, all on | It fixes the `features` list and the guard points |
| O16 | B07 | Are gene definitions datapack JSON in Beta? | No. Java registry in Beta with the rebuild function (B07.1). JSON after Beta | JSON needs a codec framework (M) |
| O17 | B01 | Where do species and piece of a fossil live, and how many opacity steps does the overlay have? | One block per host stone and species. Piece, quality and biome group as properties, about 4,600 states in total. Opacity in 4 steps by weighted model variants | It sets the block count and the state count |
| O18 | B06 | What is the largest DNA drive the design expects? | Design for 150 entries. Test at 1,000 | A drive of 10,000 entries reaches the 1 MiB payload limit |
| O19 | B21 | Which distance does the game guarantee for a digsite, for which tool (command, map, trade), and may a map show "Locating…"? | Vanilla `/locate` radius of 1,600 blocks (100 chunks) replaces the plan's 2,000. Spacing keeps the median below 800 blocks. Maps use the vanilla function with no wait screen | The plan's 2,000 blocks exceeds the vanilla command radius |
| O20 | Post-P2 | Does the roadmap include optional compat modules (CC: Tweaked peripheral, Create on Fabric, Ponder or a guide book)? | CC: Tweaked peripheral after B15. Create and Ponder only when a NeoForge 26.x build exists. Guide book stays in the backlog | Create and Ponder have no NeoForge 26.2 build |
| O21 | B15, B26 | Do vanilla and decorative fences hold dinosaurs? | Yes, as walls with vanilla collision. They are not in the breakable list unless a data pack adds them | Without an answer, a wooden fence may be the cheapest paddock |

## Post-Beta

Everything in this section is outside Beta. Nothing here is a Beta recommendation.

| Feature | Plan status | Nearest precedent | Note |
| --- | --- | --- | --- |
| Security cameras | Backlog | SecurityCraft mounted view `[v]` | SEC.1 and SEC.2. No forced chunks |
| CCTV wall | Not in the plan | SecurityCraft frame feed `[v]` | Snapshot every 2 to 5 s at 128 by 128, not a live render `[inf]` |
| Keycards and gates | Backlog | SecurityCraft keycard `[v]` | SEC.5 |
| Enclosure marker | Backlog | Ad Astra flood fill `[v]` | T12 |
| Tour Station on a timer | Not in Beta by default (O11) | MTR depot `[doc]` | B22.14 |
| Dormant tours as data | Not in the plan | Create trains, SnR shadow realm `[v]` `[doc]` | B22.28 |
| Gyrosphere, monorail, aircraft, boats | Backlog | Immersive Aircraft addon shape `[v]`, Create trains `[v]` | — |
| Battery block | Not in Beta (no backup generators) | Create Connected item-stored charge `[v]` | B14.13 |
| Power meter block | O13 | Create stressometer `[v]`, IE current transformer `[doc]` | B14.14 |
| Storm event with fence flicker | Not in the plan | IE lightning rod `[doc]`, Immersive Thunder `[v]` | — |
| Control-room redstone channels | Not in the plan | IE redstone wire `[v]` | — |
| Voice radio for rangers | Not in the plan | Simple Radio (no 26.x) `[v]` | Player-installed only |
| Ponder tutorials, guide book | Backlog | Ponder `[v]`, Ad Astra Patchouli book `[v]` | O20 |
| Create integration | Not in the plan | Create Fly on Fabric `[doc]` | C.4 to C.7 |
| Drill extractor for amber | Backlog | Immersive Machinery work budget `[v]` | — |

## Conclusion

The optimization mods teach one hard fact. They make vanilla cheaper, and they leave a mod's own costs alone. Lithium skips SBL brains. Lithium and the block-entity bakers skip modded block entities. IO even makes tick-counted logic wrong. So Nublar's lightness comes from three of its own rules:

1. Still things use static geometry.
2. One network object runs each grid.
3. Work happens only on change, with game-time timers.

The compatibility duties are short and cheap. They are exact culling boxes, no client logic in `tick`, no OpenGL, state-only light and path types, compact packets and shared render types.

The tech mods agree on how a park should feel. A machine shows its fault in the world and in its screen. A grid fails as one unit with visible signs. A driverless vehicle stops for what blocks it and tells riders where they are. Standard capabilities give Nublar CC: Tweaked, Refined Storage, Mekanism and Create automation with no compat code. The mods conflict with Nublar where they turn the lab into a layout puzzle, make blocks unbreakable, add turrets or race cars. The 26.2 table decides the rest: optional compat ships first for CC: Tweaked, and Create waits for a NeoForge build. The 15 specifications and 15 open items give each module owner a starting point. Each open item has a default.

## Repository facts

Each row records what the notes recorded at the commit read. Licence values are plain repository facts. "Not recorded" means the notes did not record the fact.

### Platform and libraries

| Short name | Repository | Licence as recorded | Branch @ commit read | Loaders | 26.x status |
| --- | --- | --- | --- | --- | --- |
| NeoForge | neoforged/NeoForge | LGPL-2.1 (file headers) | `26.2.x` @3ce4390 (2026-10-10) | NeoForge | 26.2 and 26.3 |
| Fabric API | FabricMC/fabric-api | Apache-2.0 | `26.2` @6a8ed6b (2026-09-18), version 0.161.0 | Fabric | 26.2 to 26.4 |
| TRE | TechReborn/Energy | MIT | `master` @fdc4144 (2026-01-01), 5.0.0 | Fabric | 26.1 or later |
| Balm | TwelveIterationMods/Balm | Repository: Apache-2.0. `gradle.properties` `license`: All Rights Reserved | `26.3` @cd5d2c3 (2026-10-02) read. `26.2` @ae5a2fd (2026-09-25), 26.2.0.9 | Fabric, NeoForge | 26.1 to 26.3 |
| CSL | terrarium-earth/Common-Storage-Lib | MIT | `1.21.x` @69dd639 (2026-07-12), 0.0.10 | Fabric, NeoForge | None |
| Architectury | architectury/architectury-api | LGPL-3.0 | `26.2` @050efc3 (2026-10-09) | Fabric, NeoForge | 26.2, 26.3 |
| FCAP | Fuzss/forgeconfigapiport-fabric | MPL-2.0 | Not cloned | Fabric, NeoForge | 26.2.1 bugfixes only, 26.3.1 primary |
| GeckoLib 5 | bernie-g/geckolib | MIT | `26.2` @1e09655 (2026-09-16) | Fabric, NeoForge, Forge | 26.1, 26.2 |
| SBL | Tslat/SmartBrainLib | MPL-2.0 | `Main` @deb0150 (2026-10-05), 2.0.3 | Fabric, NeoForge, Forge | 26.1.2, 26.2 |

### Optimization mods

| Short name | Repository | Licence as recorded | Branch @ commit read | Loaders | 26.x status |
| --- | --- | --- | --- | --- | --- |
| Sodium | CaffeineMC/sodium | `LICENSE.md`. `neoforge.mods.toml`: Polyform-Shield-1.0.0 | `dev` @8aa723c (2026-09-30). `26.2/stable` @9c69d26 | Fabric, NeoForge | 26.2 stable, 26.3 alpha |
| Embeddium | FiniteReality/embeddium | LGPL-3.0 | `21.4/neoforge` @aa6657d (2025-02-01) | Forge, Fabric, NeoForge | None |
| Chloride | SrRapero720/chloride | `gradle.properties`: All-Rights-Reserved | `26.2.x-neo` @579043b (2026-07-19) | Fabric, NeoForge | 26.2 |
| Sodium Extra | FlashyReese/sodium-extra | LGPL-3.0 | `26.3/dev` @b0bbae5 (2026-10-03). `26.2/stable` @658cbab | Fabric, NeoForge | 26.2, 26.3 |
| Nvidium | MCRcortex/nvidium | LGPL-3.0 | `26.2` @bda9ff4 (2026-09-16) | Fabric | 26.1.1 to 26.3 |
| VulkanMod | xCollateral/VulkanMod | LGPL-3.0 | `dev` @087ef24 (2026-08-10) | Fabric | To 26.1.2 |
| ImmediatelyFast | RaphiMC/ImmediatelyFast | LGPL-3.0 | `26.3` @a756fba (2026-10-08). `26.2` @475e3f1 | Fabric, NeoForge | 26.1 to 26.3 |
| GPUBooster | ITsMrToad/GPUBooster | GPL-3.0 | `GPUBooster-1.21.1-fabric` @5ed54b4 (2025-12-06) | Fabric | None |
| Ixeris | decce6/Ixeris | LGPL-3.0. API package 0BSD | `master` @bc46aae (2026-10-09) | Fabric, Forge, NeoForge | To 26.3 |
| Dynamic FPS | juliand665/Dynamic-FPS | MIT | `main` @2152ae3 (2026-09-28) | Fabric, NeoForge | 26.2, 26.3 |
| EntityCulling | tr7zw/EntityCulling | tr7zw Protective License | `main` @7e62c6e (2026-10-08) | Fabric, NeoForge | 26.1 to 26.3 |
| OcclusionCulling | LogisticsCraft/OcclusionCulling | MIT | `master` @7a346ea (2024-03-03) | Library | — |
| MoreCulling | fxmorin/moreculling | GPL-3.0 | `master` @83ec119 (2026-10-06) | Fabric, NeoForge | 26.3 |
| ETF | Traben-0/Entity_Texture_Features | LGPL-3.0 | `ETF-Main` @ba6dfec (2026-10-04) | Fabric, NeoForge | 26.1 to 26.3 |
| EBE | FoundationGames/EnhancedBlockEntities | LGPL-3.0 | `1.21.4` @b0b202a (2025-05-26) | Fabric | None |
| BBE | EdeenMC/betterblockentities | LGPL-3.0 | `main` @7ecbd75 (2026-09-15) | Fabric, NeoForge | 26.2, 26.3 |
| modelfix | MehVahdJukaar/modelfix-multi | GPL-3.0 | `master` @bcf9b71 (2026-02-03) | Fabric, Forge | None |
| SDL | txnimc/SodiumDynamicLights | MIT | `main` @64b442f (2025-04-13) | Fabric, NeoForge | None |
| LambDynamicLights | LambdAurora/LambDynamicLights | Lambda License | `26.3` @b9c8a46 (2026-09-15) | Fabric, NeoForge | 26.2, 26.3 |
| BetterGrassify | UltimatChamp/BetterGrassify | Apache-2.0 | `main` @87015db (2026-09-16) | Fabric | To 26.3 |
| CreateBetterFPS | MoePus/CreateBetterFPS | MIT | `main` @5e6c217 (2026-04-19) | Forge | None |
| EVD | Patbox/EntityViewDistance | LGPL-3.0 | `main` @30dda3e (2026-06-16) | Fabric | 26.2 |
| Lithium | CaffeineMC/lithium | LGPL-3.0 | `26.2` @59c9db1 (2026-09-15). `develop` @416f68d | Fabric, NeoForge | 26.1 to 26.3 |
| AI-Improvements | BuiltBrokenModding/AI-Improvements | MIT (code only) | `26.3` @a51cab7 (2026-10-03) | NeoForge, Forge | 26.1.1 to 26.3 |
| IO | Luke100000/ImmersiveOptimization | GPL-3.0 | `26.2` @559386e (2026-07-14). `1.20.1` @3aab8b1 | Fabric, Forge, NeoForge | 26.1.2, 26.2 |
| VMP | RelativityMC/VMP-fabric | MIT | `ver/26.1.1` @7e89476 (2026-04-01) | Fabric | 26.1 to 26.3 |
| Rail Optimization | fxmorin/RailOptimization | No licence file. README limits reuse | `master` @52ca530 (2023-01-06) | Fabric, Forge, Quilt | None |
| Async Locator | thebrightspark/AsyncLocator | MIT | `1.20.x` @c79885f (2023-09-07) | Fabric, Forge | None |
| FerriteCore | malte0811/FerriteCore | MIT | `26.1` @0cef1f2 (2026-03-24), 9.0.0 | Fabric, NeoForge | 26.1, 26.2 |
| ModernFix | embeddedt/ModernFix | LGPL-3.0 | `26.1` @8a837c0 (2026-10-08) | NeoForge (26.1) | 26.1 only |
| C2ME | RelativityMC/C2ME-fabric | MIT. One module All Rights Reserved | `dev/26.3.0` @9ac50bd (2026-10-07) | Fabric | 26.2, 26.3 |
| ScalableLux | RelativityMC/ScalableLux | LGPL-3.0 | `ver/26.2.0` @77c290c (2026-07-09) | Fabric (Modrinth also NeoForge) | 26.2 |
| Krypton | astei/krypton | LGPL-3.0 | `master` @bc08466 (2026-09-30) | Fabric | 26.2, 26.3 |
| PacketFixer | TonimatasDEV/PacketFixer | MIT | `master` @ba04520 (2026-09-20) | Fabric, NeoForge | 26.2, 26.3 |
| memoryLeakFix | fxmorin/memoryLeakFix | LGPL-2.1 | `dev` @988f54c (2025-06-25) | Fabric, Forge | None |
| lazydfu | astei/lazydfu | MIT | `master` @a83ce2f (2022-06-07) | Fabric | None |
| Stfu | ItsFelix5/Stfu | MIT | `multiversion` @7a5515f (2026-06-16) | Fabric | 26.1, 26.2 |
| Particle Core | fzzyhmstrs/pc | MIT | `master` @1151fe6 (2026-07-07) | Fabric (Modrinth also NeoForge) | 26.2 |

### Technology and content mods

| Short name | Repository | Licence as recorded | Branch @ commit read | Loaders | 26.x status |
| --- | --- | --- | --- | --- | --- |
| Create | Creators-of-Create/Create | Custom `LICENSE.md`. `AGENTS.md` asks AI assistants not to work on the code | `mc1.21.1/dev` @7ff2760 (2026-10-07) | NeoForge | None. 26.1 port in progress `[doc]` |
| Create-Fabric | Fabricators-of-Create/Create | Custom | `mc1.21.1/fabric/dev` @7f1f67b (2025-03-11) | Fabric | None |
| Create Fly | ZurrTum/Create-Fly | CC0-1.0 | `26.2` (not cloned, pushed 2026-09-29) | Fabric | 26.2 |
| Flywheel | Engine-Room/Flywheel | MIT | `26.1.2/dev` @a6f934d (2026-09-24) | Fabric, NeoForge | 26.1.2 |
| Ponder | Creators-of-Create/Ponder | MIT | `mc26.1/dev` @647687e (2026-10-09) | Fabric, NeoForge | 26.1.2 |
| SnR | Layers-of-Railways/Railway | LGPL-3.0 | `1.20/dev` @7441960c (2026-08-27) | Forge, Fabric, NeoForge | Ports on Create Fly |
| CCA | mrh0/createaddition | MIT | `1.21.1` @1c81fab (2026-10-01) | NeoForge | Port `chaevsfe/createaddition` `fabric-26.2` @c7aae67 |
| Create Connected | hlysine/create_connected | AGPL-3.0 | `main` @be43db7 (2026-09-04) | NeoForge | Ports on Create Fly |
| Create Deco | talrey/CreateDeco | CC0-1.0 | `1.21-neo` @996780b (2026-04-14) | NeoForge | None |
| CDG | george8188625/Create-Diesel-Generators | MIT | `1.21.1` @bed7a1c (2026-09-25) | NeoForge | Port on Create Fly |
| CEI | DragonsPlusMinecraft/CreateEnchantmentIndustry | LGPL-3.0 | `1.21.1/6.0.0-dev` @7cb2e9f (2026-10-09) | NeoForge | Fabric 26.1.2 builds |
| CCK | DragonsPlusMinecraft/CreateCentralKitchen | LGPL-3.0 | `1.21.1/6.0.0-dev` @df9cc5c (2026-10-09) | NeoForge | None |
| CNA | gitlab antarcticgardens/create-new-age | Code licence file. Assets all rights reserved | `1.20.1` @9ff732f (2026-06-08) | Forge, Fabric | None |
| Bells & Whistles | aesefficio/BellsAndWhistlesMod | GPL-3.0 | `1.20.1-forge` @e06fb2f (2026-06-26) | Forge, NeoForge | Port on Create Fly |
| Create Jetpack | PssbleTrngle/CreateJetpack | Repository `LICENSE.md` | `main/neoforge/1.21.x` @0b1228f (2026-08-11) | NeoForge | None |
| CBC | Cannoneers-of-Create/CreateBigCannons | Mixed: CC BY-NC-SA 4.0 and MIT | `create-v6-1.21.1` @46a5d2a (2026-08-22) | NeoForge | None |
| Simulated | Creators-of-Aeronautics/Simulated-Project | Repository `LICENSE.md`, assets all rights reserved | `main` @8d482a5 (2026-10-02) | NeoForge | None |
| Sable | ryanhcode/sable | Polyform Shield 1.0.0 | Not cloned | Fabric, NeoForge | None |
| Mekanism | mekanism/Mekanism | MIT | `26.3` @feb1959 (2026-10-09) | NeoForge | 26.1 to 26.3 branches |
| IE | BluSunrize/ImmersiveEngineering | Custom ("Blu's License of Common Sense") | `1.21.1` @75a27f0 (2025-08-18) | NeoForge | None seen |
| Immersive Thunder | netcatgirl/ImmersiveThunder | MIT | `1.21.6` @62fbc81 (2025-06-30) | Fabric, Forge, NeoForge | None |
| Powah | Technici4n/Powah | LGPL-3.0 | `26.1` @4274274 (2026-06-09) | NeoForge | 26.1.2 |
| RS2 | refinedmods/refinedstorage2 | MIT | `develop` @cb95e08 (2026-10-04) | Fabric, NeoForge | 26.1.2 |
| Immersive Machinery | Luke100000/ImmersiveMachinery | GPL-3.0 | `1.20.1` @7ed648f (2026-09-09) | Fabric, Forge | None |
| SecurityCraft | Geforce132/SecurityCraft | MIT | `26.2` @b88421b (2026-09-20) | NeoForge | 26.1 to 26.3 |
| CC: Tweaked | cc-tweaked/CC-Tweaked | Mostly MPL-2.0 (per-file SPDX) | `mc-26.2` @3829165 (2026-08-25) | Fabric, NeoForge | 26.1 to 26.3 |
| Simple Radio | CodinGlitch/SimpleRadio | GPL-3.0 | `1.21.1` @67e2a55 (2026-06-18) | Fabric, Forge, NeoForge | None |
| Simple Voice Chat | henkelmax/simple-voice-chat | Not recorded | Branch list only | Many | 26.1 to 26.4 |
| Alternate Current | SpaceWalkerRS/alternate-current | MIT | `main` @d93fd52 (2026-09-15) | Fabric. Branch `neoforge-26.2` | 26.2, 26.3 |
| Automobility | FoundationGames/Automobility | MIT-style `LICENSE` | `1.21-rewrite` @e43f78b (2025-04-24) | Fabric, NeoForge | None |
| Man of Many Planes | Luke100000/Man-of-Many-Planes | README: code CC0, assets CC BY-NC-SA 4.0. `fabric.mod.json`: GPL 3.0 | `1.21.1` @c1c5189 (2026-10-10) | Fabric, NeoForge | None |
| Immersive Aircraft | Luke100000/ImmersiveAircraft | GPL-3.0 | `1.21.1` @02010e1 (2026-10-07) | Fabric, NeoForge | None |
| MTR | Minecraft-Transit-Railway/Minecraft-Transit-Railway | MIT | `master` @739dba4 (2026-06-13), 4.0.5 | Fabric, Forge | None |
| VS2 | ValkyrienSkies/Valkyrien-Skies-2 | LGPL-3.0 | `1.20.1/main` @a79995c (2026-07-22) | Fabric, Forge | None |
| Clockwork | ValkyrienSkies/Clockwork | Apache-2.0 for code, assets ARR | `1.20.1/main` @067c9b3 (2026-08-12) | Forge, Fabric | None |
| Quark | VazkiiMods/Quark | `LICENSE.md` (GitHub: NOASSERTION). `AGENTS.md` asks for a contribution notice | `master` @211d00a (2026-10-05) | NeoForge | None |
| Zeta | VazkiiMods/Zeta | NOASSERTION | `main` @ae8f23a (2026-09-09) | NeoForge | None |
| Tinkers | SlimeKnights/TinkersConstruct | MIT | `1.20.1` @215e687 (2026-10-01) | Forge | None |
| Ad Astra | terrarium-earth/Ad-Astra | `LICENSE` (GitHub: NOASSERTION) | `1.21.1` @45abf0e (2026-09-13) | Fabric, NeoForge | None |
| WalksyLib | Walksy/WalksyLib | MIT | `main` @62ba2ee (2026-10-10) | Fabric (client) | 26.2 |

## Sources

### Local documents and code

- [docs/project-plan.md](../docs/project-plan.md): P2, B00 to B27, decisions D01 to D14, unassigned backlog.
- [docs/codebase-analysis.md](../docs/codebase-analysis.md), section 11.
- [Reference mod survey for Beta.md](Reference%20mod%20survey%20for%20Beta.md): row ids, specifications S1 to S27, open items O1 to O6, budget for load B.
- Local code at `kash's-slop` 0c97bde plus the uncommitted tree. Paths in "Defects that the researchers found".

### Research notes (11 files)

All in `research_notes/Optimization and tech mod survey/`:

- `platform_and_library_baseline.md`
- `render_pipeline_mods.md`
- `entity_and_block_entity_rendering_mods.md`
- `server_tick_and_ai_mods.md`
- `memory_chunk_and_network_mods.md`
- `create_core.md`
- `create_addons.md`
- `machines_power_and_storage.md`
- `vehicles_and_transport.md`
- `security_control_and_comms.md`
- `general_content_and_progression.md`

### Checks made for this report (2026-10-10)

- `gh api repos/TwelveIterationMods/Balm/branches` and `contents/gradle.properties?ref=26.2`: branch `26.2`, `version = 26.2.0.9`, commit `ae5a2fd`.
- `gh api` branch lists for `Creators-of-Create/Create`, `Engine-Room/Flywheel`, `Creators-of-Create/Ponder`, `mekanism/Mekanism`, `embeddedt/ModernFix`, `ZurrTum/Create-Fly`.
- Modrinth API `project/<slug>/version?game_versions=["26.2"]` for each surveyed mod, and `search` for Refined Storage, Stfu and Tinkers.
- Local re-reads: `J/init/EntityInit.java:30-39`, `J/block/ElectricFencePostBlock.java:275-276,300-305`, `J/block/api/MultiEntityBlock.java:35,83`, `J/block/api/MathUtils.java:121,133`, `J/block/ElectricFenceBlock.java:88-91`, `J/entity/ai/behaviour/social/FallbackBudget.java:30,66`, `J/entity/ai/behaviour/social/DinoGroup.java:205`, and a count of print statements in `common/src/main/java`.

### Public pages cited by the notes

- [Sodium](https://github.com/CaffeineMC/sodium), [Lithium](https://github.com/CaffeineMC/lithium), [EntityCulling](https://github.com/tr7zw/EntityCulling), [ImmediatelyFast](https://github.com/RaphiMC/ImmediatelyFast), [FerriteCore](https://github.com/malte0811/FerriteCore), [ModernFix wiki](https://github.com/embeddedt/ModernFix/wiki), [C2ME](https://github.com/RelativityMC/C2ME-fabric), [Immersive Optimization wiki](https://github.com/Luke100000/ImmersiveOptimization/wiki)
- [Create development status](https://wiki.createmod.net/users/development-status), [Ponder](https://github.com/Creators-of-Create/Ponder), [Flywheel](https://github.com/Engine-Room/Flywheel), [Create Fly on Modrinth](https://modrinth.com/mod/create-fly)
- [Mekanism](https://github.com/mekanism/Mekanism), [Immersive Engineering](https://github.com/BluSunrize/ImmersiveEngineering), [Refined Storage 2](https://github.com/refinedmods/refinedstorage2), [Powah](https://github.com/Technici4n/Powah)
- [Automobility](https://github.com/FoundationGames/Automobility), [MTR](https://github.com/Minecraft-Transit-Railway/Minecraft-Transit-Railway), [Valkyrien Skies 2](https://github.com/ValkyrienSkies/Valkyrien-Skies-2)
- [SecurityCraft](https://github.com/Geforce132/SecurityCraft), [CC: Tweaked](https://github.com/cc-tweaked/CC-Tweaked), [Alternate Current](https://github.com/SpaceWalkerRS/alternate-current), [Simple Radio](https://github.com/CodinGlitch/SimpleRadio)
- [Quark](https://quarkmod.net/), [Tinkers' Construct docs](https://slimeknights.github.io/docs/), [Ad Astra](https://github.com/terrarium-earth/Ad-Astra)
- NeoForge documentation on data attachments: <https://docs.neoforged.net/docs/datastorage/attachments>
