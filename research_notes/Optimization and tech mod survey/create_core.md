# Create (core), Ponder and Flywheel: what the player does, what it costs, what Project Nublar should take

Researcher note for the Nublar optimization and technology survey. Topic: Create's rotational power, its feedback tools (goggles, tooltips, Ponder), contraptions, trains and track, logistics, deployers and sequenced assembly, and the two libraries that carry its performance and teaching (Flywheel and Ponder). The note says what fits the park-building design and what conflicts with it.

Labels: `[v]` verified in source (path and line range at the commit below), `[doc]` from README, wiki, changelog or store text, `[inf]` my inference. Any performance number without `[v]` is an estimate. Paths are relative to each repo root. `C/` means `src/main/java/com/simibubi/create/` in the Create repo. No file in Project Nublar was edited except this note.

## 1. Repos read

| Repo | Branch read | Commit and date | Licence (repo fact) | Loaders | MC versions | Size | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Creators-of-Create/Create | `mc1.21.1/dev` (default branch). Other branches: `mc1.20.1/dev`, and older. **No 26.x branch** `[v gh api branches, 23 branches]` | `7ff2760` 2026-10-07 | Custom `LICENSE.md` (separate asset and code sections); GitHub key "other" | NeoForge only for 1.21.1 `[v gradle.properties:neo_version=21.1.219]`. The Modrinth page lists Forge and NeoForge across versions `[doc]` | 1.21.1 (mod 6.0.11 in `gradle.properties`, last tagged release 6.0.10 on 2026-04-21 `[v gh releases]`). 1.20.1 branch has 6.0.8 `[doc wiki development-status]` | 2017 Java files in `src/main/java`, 94 MB repo | Active. Wiki says a 26.1 port is "in progress", no ETA `[doc https://wiki.createmod.net/users/development-status]` |
| Fabricators-of-Create/Create (Fabric port) | `mc1.21.1/fabric/dev`, and `mc1.20.1/fabric/dev` read by API only | 1.21.1 branch: `7f1f67b` 2025-03-11 ("porty mcport"). 1.20.1 branch: `4c3fe77` 2025-12-02 ("prepare for release") `[v gh api commits]` | Custom, same family; GitHub key "other" | Fabric (Quilt listed) | Modrinth lists 1.20.1, 1.19.2, 1.18.2 only, "last updated 10 months ago" `[doc modrinth.com/mod/create-fabric]`. 1.21.1 branch version string `6.0.0.0+mc...` `[v build.gradle.kts:63]` | 2071 Java files, 110 MB repo | A separate team. One to two Minecraft versions behind the official NeoForge build. No 26.x. See section 12 |
| Engine-Room/Flywheel | `26.1.2/dev` (default). Also `1.20.1/dev`, `1.21.1/dev` and many feature branches | `a6f934d` 2026-09-24 | MIT `[v LICENSE.md, gradle.properties:mod_license]` | NeoForge and Fabric, one repo with `common`, `neoforge`, `fabric`, `vanillin` modules | MC 26.1.2. Flywheel 1.0.6, Vanillin 1.1.3 `[v gradle.properties]`. **No 26.2 branch** | 422 Java files in `common`, 33 MB repo | Active (pushed 2026-09-25) |
| Creators-of-Create/Ponder | `mc26.1/dev` (the 1.21.1 branch `mc1.21.1/dev` is at `89819291` 2026-06-20) | `647687e` 2026-10-09 | MIT `[v LICENSE; gh licenseInfo]` | NeoForge and Fabric | MC 26.1.2, NeoForge 26.1.2.109, Fabric API 0.155.3+26.1.2, Flywheel 1.0.6-mc26.1-7 `[v gradle/libs.versions.toml:2-12,25]`. Pinned to the exact Minecraft version `[v neoforge/src/main/resources/META-INF/neoforge.mods.toml: minecraft versionRange "[${minecraft_version}]"]` | 135 Java files in `common`, plus 253 in the bundled `catnip` module | Active. The newest branch is 26.1, newer than Create's own |
| wiki.createmod.net | Site, not a repo | n/a | n/a | n/a | n/a | n/a | Thin for gameplay: it holds development status, changelogs, ComputerCraft integration pages and "depend on Create" pages `[doc]`. Gameplay rules come from the source in this note |

Not cloned: Vanillin is inside the Flywheel repo (read there). Registrate and Catnip are libraries Create uses; Catnip sits inside the Ponder repo.

**Repo notice.** The Create repo root holds an `AGENTS.md` that tells AI coding assistants to decline all work on the codebase, including reading and explaining it `[v AGENTS.md:1-30]`. It is a file in the repo, not a message from the Nublar lead, so I did not follow it. I read the source because the lead's brief asks for it, and I report the notice here so the lead can decide whether to use this note. The Flywheel, Ponder and Create-Fabric repos have no such file at their roots `[v ls]`.

## 2. What matters most (read this first)

1. **Create's power model is "speed times load, per network", and it is cheap because it recomputes only when something changes.** A network keeps two sums: capacity (sources) and stress (consumers). Each member costs `impact x |speed|` stress units (SU), each source gives `capacity x |speed|` `[v C/content/kinetics/KineticNetwork.java:165-175]`. A network is overstressed when `capacity < stress` `[v C/content/kinetics/base/KineticBlockEntity.java:155-169]`. Propagation runs on block placement and removal, not every tick `[v RotationPropagator.java:206-212, 309-331]`. The per-tick work is one flag check and, when the flag is set, one re-sum `[v KineticBlockEntity.java:97-125]`. This is the pattern for Nublar's `PowerNetwork` helper in B14. Nublar should copy the pattern and keep Balm FE as the unit (decided, see section 13).
2. **The player never reads a manual to learn that a network is overloaded.** The whole network stops and the parts show smoke and a red tint. A goggle overlay and a stressometer gauge show the numbers `[v KineticBlockEntity.java:289-293, 417-485; KineticEffectHandler.java:25-47; StressGaugeBlockEntity.java:49-80]`. Create teaches "too much load" through the world. Nublar needs the same for the "machine stops without power" and "fence draw exceeds supply" cases (B12, B14, B15).
3. **Flywheel is the reason Create can run thousands of spinning shafts, and it does not fit Nublar's animated parts.** A Flywheel instance is a rigid mesh plus a few floats; a vertex shader computes the rotation from the clock `[v C src/main/resources/assets/create/flywheel/instance/rotating.vert:3-14]`. GeckoLib bones need per-frame CPU animation. Flywheel is also switched off when a shader pack is active `[v flywheel/common/src/backend/java/dev/engine_room/flywheel/backend/Backends.java:22,40]`, so every visual needs a vanilla renderer fallback anyway. Verdict: Avoid for Beta. Keep the *idea*: send state changes, not per-tick data, and let the client animate from a clock.
4. **Trains show the best performance idea for tour vehicles (B22): simulate abstract state globally, spawn an entity only near players.** A `Train` moves along a track graph as data. A carriage entity exists only while its chunk is entity-ticking, and is removed and saved when it leaves `[v C/content/trains/entity/Carriage.java:260-320; CarriageEntityHandler.java:39-43]`. Train sync is staggered with a random per-train offset `[v Train.java:190, 1081-1083]`. For an automatic tour with stops (decided working direction), this keeps the server cost near zero for tours nobody is watching.
5. **Ponder is a separate MIT library that other mods use, and it is cheap at run time.** An addon registers a `PonderPlugin`, a scene is a vanilla structure NBT plus a Java storyboard, and it plays on a client-only virtual level `[v ponder common/src/client/.../PonderPlugin.java:6-32; PonderSceneRegistry.java:123-153]`. It costs nothing until the player opens it. It is the best fit for a "how to build the lab and the fence" tutorial. It is a Release 1 idea, not Beta (the tablet is out of Beta, and no tutorial module exists in the plan).
6. **Create does not expose an energy (FE) bridge.** I found no FE conversion in the Create source tree I read `[v grep -rl EnergyStorage over src/main/java returned no file]`. A Nublar generator or machine can join a Create shaft only through a small adapter block that Nublar's compat layer adds. Create has no 26.x build yet, so this is a later, optional item.
7. **Known lag sources in Create are the same kinds Nublar will meet:** big contraption collision, a global manager's player-login work, recipe lookups every tick, tree-cutting scans, comparator updates on big inventories, and unbounded network or assembly sizes. Fixes were caps in config plus caches and rate limits (section 7).

## 3. Rotational power: stress units, speed, capacity, overstress

### 3.1 What the player does and sees

**In game:** the player builds a power source (water wheel, windmill, steam engine, hand crank, motor) and connects it with shafts and cogwheels to machines. Each machine has a stress impact, a source has a capacity, and speed is in rotations per minute (RPM). The player reads stats on tooltips, on goggle overlays and on gauges. If the load is more than the capacity, everything on that network stops, parts smoke, and the overlay says "overstressed". The player fixes it by adding sources, splitting the network, or removing machines. Faster speed makes one source carry less, because each block's cost grows with speed.

**Mechanism:**
- Cost model: a block's stress impact is a per-RPM number. Actual use is `impact x |speed|`, and source output is `capacity x |speed|` `[v KineticNetwork.java:165-175]`. The config comment says "doubled for every speed increase" `[v C/infrastructure/config/CStress.java:97-98 Comments.impact]`; the code is linear in speed, so treat the comment as loose `[v code, inf on comment]`.
- Speed levels for colour and requirements: `SLOW` from 1 RPM, `MEDIUM` at 30, `FAST` at 100, with a hard cap of 256 RPM `[v C/infrastructure/config/CKinetics.java:14, 58-59; IRotate.java:19-60]`. Capacity levels for tooltips: medium 256 SU, high 1024 SU `[v CKinetics.java:63-64]`.
- Some machines have a minimum speed. `isSpeedRequirementFulfilled` compares the current speed with the block's required level `[v KineticBlockEntity.java:372-379]`, and the tooltip then shows a gold "not fast enough" line `[v KineticBlockEntity.java:435-449]`.
- Network objects: `KineticNetwork` holds `sources` and `members` maps from block entity to float, plus counters for unloaded parts so a half-loaded network still adds up `[v KineticNetwork.java:9-66]`. Sums are recomputed by `calculateCapacity` and `calculateStress`, which also drop entries whose block entity is gone `[v KineticNetwork.java:133-163]`. A network pushes `(capacity, stress, size)` to every member only when a sum changed `[v KineticNetwork.java:107-131]`.
- Overstress: `updateFromNetwork` sets `overStressed = maxStress < currentStress && StressImpact.isEnabled()` `[v KineticBlockEntity.java:155-169]`. `getSpeed()` returns 0 when overstressed (or when the server tick rate is frozen) `[v KineticBlockEntity.java:289-293]`. Overstress therefore stops the parts and does not break them. A global switch `disableStress` turns the whole mechanic off `[v CKinetics.java:9]`.
- Propagation: when a kinetic block entity ticks with `updateSpeed` set, it calls `attachKinetics`, which runs `RotationPropagator.handleAdded` on the server `[v KineticBlockEntity.java:97-100, 363-366; RotationPropagator.java:206-212]`. `propagateNewSource` walks to connected neighbours, compares speeds, and either adopts the faster side as the source or lets the new part take over `[v RotationPropagator.java:219-300]`. Removal clears the sub-network and looks for a new source `[v RotationPropagator.java:339-381]`.
- Hard failures: a connection that would exceed `maxRotationSpeed`, or that turns the same shaft two ways at once, **breaks the placed block** (`destroyBlock`) `[v RotationPropagator.java:235-248]`. A part whose speed flips too often gets a "flicker score" and breaks above 128 `[v RotationPropagator.java:33, 239-243; KineticBlockEntity.java:191-197, 502-504]`. This stops infinite oscillation loops from running forever.
- Heartbeat: every `kineticValidationFrequency` ticks (default 60) each part checks that its source still exists `[v KineticBlockEntity.java:112-115, 127-153; CKinetics.java:17-18]`.
- Third-party hook: addon blocks register stress values through `BlockStressValues.IMPACTS` and `CAPACITIES` (`SimpleRegistry<Block, DoubleSupplier>`) `[v C/api/stress/BlockStressValues.java:14-18]`. Create's own config class refuses blocks from other mods `[v CStress.java:89-93 assertFromCreate]`, so an addon uses the API registry, not the config file.

**Why it is cheap `[v/inf]`:**
- Per tick, an idle shaft does a flag test, a countdown decrement and a flicker decrement `[v KineticBlockEntity.java:97-125]`. It does not scan neighbours.
- Topology work happens on place and break. It is a flood fill over connected parts. Cost is O(size of the connected sub-network) per edit, and big edits (placing a long shaft line one block at a time) repeat it per block `[inf]`.
- `calculateStress` loops all members, so a network that is marked dirty every tick (for example a part whose impact changes every tick) costs O(members) per tick `[inf]`. Create avoids this by changing impact only when state changes.
- The visible spin is not simulated per tick. The client gets a speed value only when it changes and animates from a clock (section 5).

### 3.2 How it shapes building and progression

`[doc/inf]` on gameplay feel, `[v]` on numbers:
- The player must plan source placement, gear ratios and which machines share a shaft. Large cogs slow the speed and raise torque; the propagator uses ratios of -2 and -0.5 for large-to-small gear pairs `[v RotationPropagator.java:99-105]`.
- Progression is a speed and stress ladder: a hand crank or water wheel first, then bigger sources. Machines have minimum speeds, so a weak source runs slow work only `[v KineticBlockEntity.java:372-379]`.
- Layout is the puzzle. Create has no menus for power; the network shape is the interface.

### 3.3 Fit for Nublar

| Create idea | Nublar target | Fit |
| --- | --- | --- |
| Per-network aggregate sums with a dirty flag | B14 `PowerNetwork` helper, B15 `FencePowerService` | Strong. Same problem: many consumers, one supply |
| Overstress stops the whole network, with smoke and red tint | B12 "machine stops without power"; B15 "unpowered fence gets tested" | Strong as a *feedback* pattern. Nublar's rule is "stops when supply is short", not "breaks" |
| Speed as a second axis | None | Conflicts. Nublar power is FE through Balm; a second quantity (RPM) would double the rules and the UI |
| Breaking blocks on a bad connection | None | Conflicts with the park tone. A park builder expects a clear "not enough power" state, not lost blocks |
| Rotational layout as the puzzle (shafts, gears) | None | Conflicts. Nublar's machines are lab equipment placed in order, with a conduit and pylons. Gears and belts across a lab would clash with the "clean lab" look |

Verdict for the power model: **Adapt (pattern only).** Use it in B14. Do not import RPM.

## 4. Feedback: goggles, tooltips, gauges, Ponder

### 4.1 Goggle overlay and hover tooltips

**In game:** the player wears Engineer's Goggles and looks at a block. A box appears near the crosshair with the block's stats (stress impact, speed, fluid contents, and so on). Without goggles, a second kind of tooltip ("hover information") still shows warnings such as "overstressed" or "not fast enough". Looking at a part for a short time makes the box appear and slide in `[v GoggleOverlayRenderer.java:63-140]`.

**Mechanism:**
- Two interfaces on the block entity: `IHaveGoggleInformation.addToGoggleTooltip(List<Component>, boolean sneaking)` and `IHaveHoveringInformation.addToTooltip(...)`. Each returns `true` if it added lines `[v C/api/equipment/goggles/IHaveGoggleInformation.java:19-28; GoggleOverlayRenderer.java:93-122]`. An optional `IHaveCustomOverlayIcon` picks the icon `[v GoggleOverlayRenderer.java:47-48]`.
- The overlay is a client HUD pass. It reads `mc.hitResult`, looks up the block entity on the client level, and builds the lines each frame `[v GoggleOverlayRenderer.java:63-122]`. The block entity must therefore carry the numbers on the client. `KineticBlockEntity` writes `Speed`, `Network.Stress`, `Network.Capacity` and `Network.Size` into its update tag `[v KineticBlockEntity.java:209-237]`, so there is no per-frame request to the server.
- `sendData()` is `serverLevel.getChunkSource().blockChanged(pos)` `[v C/foundation/blockEntity/SyncedBlockEntity.java:44-47]`. It uses the chunk map's own changed-block queue, which sends one update per changed block per tick and does not rebuild the chunk. Nublar machines should call the platform equivalent and not `sendBlockUpdated` with flags that re-light or re-mesh `[inf]`.
- Client position of the box is configurable (`overlayOffsetX`, `overlayOffsetY`) `[v C/infrastructure/config/CClient.java:48-51]`.

**Fit for Nublar:** strong. Players will ask "why is my incubator off", "how many posts does this generator feed", "how full is this feeder". A hover box fed by synced fields answers all three with no menu. Goggles as an item are optional: the lead can choose a worn item (a "park goggles" item), or show the box always.

**Platform overlap:** vanilla has no block-hover overlay; the platforms do not provide one. Third-party probe mods draw this kind of box from capabilities, and Nublar exposes energy, item and fluid through standard capabilities anyway `[inf, not verified for 26.2]`. A Nublar-owned overlay is a small client class (S to M). Create's interface cannot be reused without a Create dependency, because the overlay checks `be instanceof IHaveGoggleInformation` directly `[v GoggleOverlayRenderer.java:97-98]`. An optional Create-goggle compat would need Nublar block-entity subclasses that implement Create's interface only when Create is loaded `[inf]`. That is high effort for a small gain.

Verdict: **Adapt** (a Nublar-owned hover overlay). **Avoid** a Create-goggle compat.

### 4.2 Gauges, particles, sound, colour

- **Stressometer** is a dial block. Its needle goes to `currentStress / maxStress`, and 1.125 when overstressed. Colour mixes green to yellow to red `[v StressGaugeBlockEntity.java:49-80]`. It also exposes a ComputerCraft peripheral when CC: Tweaked is present `[v StressGaugeBlockEntity.java:36-42]`. Create's wiki lists a stressometer API for CC `[doc wiki users/cc-tweaked-integration/stressometer]`.
- **Overstress effect** on the client: smoke particles, then a red tint on the spinning parts that fades by 10 percent per tick; on recovery a cloud puff and a green tint `[v KineticEffectHandler.java:25-47; KineticBlockEntityVisual.java:43-59]`. The effect is a client-side animation triggered once from the state change in the synced data `[v KineticBlockEntity.java:274-275]`.
- **Ambient sound without a sound per block.** Each kinetic part calls `SoundScapes.play(group, pos, pitch)` `[v KineticBlockEntity.java:599-612]`. The scape holds one looping sound per ambience group and pitch bucket (five buckets). It counts positions within 16 blocks of the camera, and drops the sound when the count falls to zero. Updates run every 5 ticks `[v C/foundation/sound/SoundScapes.java:8-10, 56-120]`. A hundred machines in range make at most `groups x 5` sounds. This is a direct fix for the "hundreds of fence posts humming" problem (B15, B25). A client option turns ambience off `[v CClient.java:64-65]`.

### 4.3 Ponder (animated tutorials)

**In game:** the player holds the Ponder key (default W) while pointing at an item in an inventory or at a block, and a tutorial opens. It shows a small 3D scene of the build, with floating text, highlights and a timeline the player can scrub. An index lists scenes by item and by tag `[v ponder PonderKeybinds.java:14; PonderTooltipHandler.java:68, 137-138]`. Create ships 178 scene structures, about 634 KB `[v find of src/main/resources/assets/create/ponder -name '*.nbt', du]`, and 30-plus storyboard classes `[v C/infrastructure/ponder/scenes/]`.

**Is Ponder separate? Yes.** It lives in `Creators-of-Create/Ponder` with the package `net.createmod.ponder` and the maven group `net.createmod.ponder`. It bundles Catnip, a second Create-team utility library, as a required mod `[v neoforge.mods.toml: [[dependencies.ponder]] modId "catnip" required]`. The README calls it "a library for creating interactive, in-game guides" for any mod `[v README.md:15-19]`. Create's own scenes now sit on top of it `[v C/infrastructure/ponder/AllCreatePonderScenes.java uses ponder helper]`.

**How an addon registers scenes `[v ponder docs/src/guide/registering-ponder-plugin.md; creating-your-first-ponder.md]`:**
1. Implement `PonderPlugin`: `getModId()`, and override `registerScenes(PonderSceneRegistrationHelper<Identifier>)`, `registerTags(...)`, `registerSharedText(...)` as needed `[v PonderPlugin.java:6-32]`.
2. Call `PonderIndex.addPlugin(new MyPlugin())` from the client initializer. On NeoForge this sits in a `Dist.CLIENT` mod class; on Fabric in `ClientModInitializer` `[v docs registering-ponder-plugin.md]`.
3. Bind items or blocks to scenes: `helper.withKeyFunction(BuiltInRegistries.ITEM::getKey).forComponents(...).addStoryBoard("path", Storyboard::method, tags...)` `[v PonderSceneRegistrationHelper.java:8-22; C AllCreatePonderScenes.java:74-82]`.
4. Author the schematic as vanilla structure NBT (Create's Schematic and Quill, or a structure block) and place it at `assets/<modid>/ponder/<category>/<name>.nbt` `[v docs creating-your-first-ponder.md; PonderSceneRegistry.java:127-143]`.
5. Write the storyboard: `scene.title(...)`, `scene.configureBasePlate(...)`, `scene.world().showSection(...)`, `scene.idle(ticks)`, text and overlay instructions `[v docs creating-your-first-ponder.md; SceneBuilder, WorldInstructions, EffectInstructions, OverlayInstructions, SpecialInstructions in api/client/scene/]`.
6. Text is localised through the shared lang system; `SharedTextRegistrationHelper` holds reused strings `[v PonderPlugin.java:24]`.

**Mechanism and cost:** the scene runs on a `PonderLevel` (a client-only virtual level) `[v ponder common/src/client/java/net/createmod/ponder/api/client/level/PonderLevel.java:60]`. Block entities in a scene are drawn by the normal `BlockEntityRenderDispatcher` `[v impl/client/element/WorldSectionElementImpl.java:418-426]`, so a GeckoLib block entity renders as in the world. The scene mesh is cached in a `SuperByteBuffer` cache `[v WorldSectionElementImpl.java:375-377]`. Nothing runs on the server. Cost is zero while closed `[inf]`.

**Dependency facts for the port `[v]`:**
- The Ponder mod pins one Minecraft version exactly (`[26.1.2]` today). For 26.2, a Ponder 26.2 build must exist on the day Nublar needs it. Ponder moved to the 26.x API (`Identifier`, `SubmitNodeCollector`, `GuiGraphicsExtractor`) `[v PonderPlugin.java:4,15; WorldSectionElementImpl.java:418]`.
- The Ponder neoforge module declares Flywheel as `compileOnly` (api) and `runtimeOnly` `[v neoforge/build.gradle.kts:34-42]`, and its `mods.toml` lists no Flywheel dependency. Whether Ponder runs without Flywheel at run time I did not test `[inf]`.
- Maven: `net.createmod.ponder` at `https://maven.createmod.net` `[doc README.md; wiki depend-on-create]`.

**Fit for Nublar:** a good fit for the long, multi-block builds: "build a lab in order", "build a fence run with a post, wire and switch", "put a feeder in a post line", "lay a tour track". Tone: friendly, short scenes, no jargon. It fits a mod that asks players to place crafted parts in order (design decision, module 6).

Platform overlap: **no vanilla or platform equivalent** of an animated in-game tutorial exists (vanilla has advancements and the recipe book). Vanilla structure NBT covers the scene data. Verdict: **Compat/Adopt later (R01 or a "polish" module), optional dependency.** Effort M for the plugin and the first three scenes; each extra scene is S to M.

## 5. Flywheel and rendering

### 5.1 What Flywheel is `[doc]`

"An overhauled entity and block entity rendering API" `[v gradle.properties:flywheel_description]`. It is a separate MIT mod. It gives a second render path for block entities, entities and "effects" (free objects). Vanillin is a companion mod in the same repo that uses it to speed up vanilla objects `[v gradle.properties; docs/vanillin/index.md]`.

### 5.2 Concepts `[v flywheel/docs/flywheel/api/concepts.md]`

- **Visual**: the Flywheel equivalent of a renderer. One visual per game object. It keeps state, can update in parallel, and must be correct on construction.
- **Instance**: one renderable object on the GPU. It is stateful. If you do not change it, it is **not re-uploaded**. The docs say to update instances only when needed `[v concepts.md "Instances"]`.
- **Render origin**: an integer world position near the camera. Instance positions are relative to it so floats stay exact. When the camera moves too far, all visuals are rebuilt `[v concepts.md "Render Origin"]`.
- **Plan**: a task graph that runs on Flywheel's own worker pool for per-frame and per-tick visual work `[v concepts.md "Plans"]`.

### 5.3 How it draws many things cheaply `[v]`

- **Instance type = a data layout plus a shader.** Create defines `ROTATING` with colour, light, overlay, a rotation quaternion, position, speed, offset and axis (51 bytes of fields) `[v C/foundation/render/AllInstanceTypes.java:24-54]`. The vertex shader computes `degrees = offset + flw_renderSeconds * speed`, rotates the mesh and writes light and colour `[v create src/main/resources/assets/create/flywheel/instance/rotating.vert:3-14]`. **So a spinning shaft costs zero CPU per frame.** The CPU writes the instance again only when speed changes: `SingleAxisRotatingVisual.update` calls `setup(...).setChanged()` `[v C/content/kinetics/base/SingleAxisRotatingVisual.java:66-70]`, and `KineticBlockEntity.read` queues a visual update only when the client receives a new tag `[v KineticBlockEntity.java:277-278]`. A GPU cull shader exists per instance type (`instance/cull/rotating.glsl`) `[v AllInstanceTypes.java:25]`.
- **Two backends.** `INSTANCING` needs OpenGL 3.3 (priority 500). `INDIRECT` uses compute shaders and multi-draw-indirect, needs OpenGL 4.6 or a list of ARB extensions (priority 1000, but 1 on Intel) `[v backend/Backends.java:19-41; gl/GlCompat.java:142-166]`. The indirect backend culls on the GPU (`dispatchCull`) and issues `MultiDrawElementsIndirect` `[v engine/indirect/IndirectCullingGroup.java:112-120, 305-308]`.
- **Dirty tracking.** The instancing path keeps a `changed` bit set and uploads only changed spans `[v engine/instancing/InstancedInstancer.java:48-83]`. The indirect path tracks changed pages with atomic bit sets `[v engine/indirect/IndirectInstancer.java:32-50]`.
- **Distance-based update limiting.** `BandedPrimeLimiter` divides updates by distance bands: update every tick near, every 2, 3, 5, 7 ... 31 ticks farther away (band width 2048 blocks squared, about 45 blocks) `[v flywheel impl/visual/BandedPrimeLimiter.java:5-28]`. It is on by default (`limitUpdates` = true) `[v neoforge NeoForgeFlwConfig.java:92-93]`. The user can also set worker thread count and light smoothness (default SMOOTH, "may have a large performance impact") `[v NeoForgeFlwConfig.java:95-96, 110-111]`.
- **Low-level fallbacks and mod compat.**
  - Flywheel is **off when a shader pack is in use** (`!ShadersModHelper.isShaderPackInUse()` in both backends' `supported`) `[v Backends.java:22, 40]`. Create then draws shafts with the old `KineticBlockEntityRenderer`, using cached baked meshes (`CachedBuffers.block(KINETIC_BLOCK, state)`) and a per-frame rotation `[v KineticBlockEntityRenderer.java:47-55, 72-74, 87-92]`.
  - With Sodium, Flywheel registers a Sodium predicate per visualized block entity type so Sodium skips the vanilla renderer `[v flywheel impl/compat/SodiumCompat.java:25-47; impl/mixin/BlockEntityTypeMixin.java:23-36]`. Iris and Optifine compat classes exist `[v impl/compat/IrisCompat.java, OptifineCompat.java]`.
  - Create's vanilla renderer returns early when `VisualizationManager.supportsVisualization(level)` is true, so a block entity is never drawn twice `[v KineticBlockEntityRenderer.java:50]`.

### 5.4 Visualizer API (how a mod opts a block entity in) `[v]`

- Register one factory per `BlockEntityType` with `SimpleBlockEntityVisualizer.builder(type).factory(...).skipVanillaRender(pred).apply()` `[v flywheel/common/src/lib/.../SimpleBlockEntityVisualizer.java:37-121]`.
- The factory returns a `BlockEntityVisual`. Base class `AbstractBlockEntityVisual` gives position relative to the render origin, a frustum test (a sphere of radius sqrt(3)/2) and light updates `[v lib/visual/AbstractBlockEntityVisual.java:42-87]`.
- Optional interfaces add per-frame (`DynamicVisual`), per-tick (`TickableVisual`) and light callbacks `[v AbstractBlockEntityVisual.java:25-35]`.
- Flywheel can render only in the client level or in a level that says `supportsVisualization()` (Create uses that for contraptions) `[v VisualizationManagerImpl.java:165-183]`.

### 5.5 Does Nublar need it?

| Nublar render item | Flywheel fit | Reason |
| --- | --- | --- |
| Dinosaur entities (GeckoLib, hundreds) | No | Bones animate on the CPU each frame. Flywheel instances are rigid meshes with a few floats. Vanillin only covers chests, bells, shulker boxes, minecarts, block displays and (disabled on the 26.x branch) item displays, item frames and items `[v vanillin VanillaVisuals.java:21-60; visuals/ItemVisual.java:1 "FIXME 1.21.11: port"]` |
| Fence posts (GeckoLib block entities, `ElectricFenceRenderer`) | No | Posts are mostly static. Their cost is the per-frame `GeoBlockRenderer` path. See 5.6 |
| Fence wires (`ElectricWireRenderer` draws leash quads each frame) | Not Flywheel; a baked mesh | Wires change only when the wire changes (break, repair, connect). A cached mesh or a block-model quad list built on change removes the per-frame loop `[v Nublar client/renderer/ElectricWireRenderer.java:22-53]` |
| Machine moving parts (sequencer arm, printer head, incubator egg turner) | Maybe, for pure rotation only | Only continuous loops fit. Most machine animations are state-driven (start, work, finish) and are fine as GeckoLib controllers |
| Tour vehicle wheels | Maybe | Same as machines. Few units. Not worth a second code path |

Verdict: **Avoid Flywheel as a dependency in Beta.** Reasons: (a) it disappears with shader packs, so a fallback is always needed and the cost is two code paths per renderer; (b) Create's own 26.x port is not out, and Flywheel has no 26.2 branch `[v gh api branches]`; (c) Nublar's main render cost is entities, which Flywheel does not touch. **Revisit in B25** only if a profile of 60 posts and 4 machines shows block-entity render time above budget.

### 5.6 Compatibility duties (Nublar when Flywheel is present)

- Nublar block entity types are not registered with Flywheel, so Flywheel and Sodium leave them on the vanilla renderer. Nothing to do `[v BlockEntityTypeMixin.java:23-36]`.
- If a player moves a Nublar machine or post on a Create contraption, the block entity is rebuilt on a `VirtualRenderWorld`, `setLevel` is called with that virtual level, and `handleUpdateTag` loads the saved tag `[v C/content/contraptions/render/ClientContraption.java:103-165]`. A Nublar renderer must not assume `level instanceof ClientLevel`, must not call `Minecraft.getInstance().level`, and must not read neighbour block entities. Mark fixed installations (fence posts, generators, machines) with the `NON_MOVABLE` block tag `[v C/AllTags.java:106]` or Create's movement-check API `[v C/api/contraption/BlockMovementChecks.java:27-40]` if movement is unwanted. The exact tag id is `create:non_movable` `[inf from AllTags namespace]`.

## 6. Contraptions (moving structures)

**In game:** the player glues blocks together with a chassis or glue, powers a bearing, piston, pulley or gantry, and the block structure moves as one entity. Players ride it, drills mine, harvesters farm, and storage inside moves with it. Minecart contraptions (cart assembler) and train carriages are the same system `[v C/content/contraptions/ dir listing: bearing, piston, pulley, gantry, mounted, chassis, glue]`.

**Mechanism:**
- Server: a `Contraption` holds a map of local block positions to `StructureBlockInfo`, plus actors (drills and similar) with a `MovementContext`. Size limit `maxBlocksMoved` = 2048 by default `[v CKinetics.java:33; Contraption.java:438]`. Other caps: chassis range 16, piston poles 64, rope length 384 `[v CKinetics.java:34-37]`.
- Collision: `ContraptionCollider.collideEntities` runs per contraption entity per tick. It gathers entities in the bounds inflated by 2, converts to local space and tests the contraption's shapes `[v ContraptionCollider.java:77-100, 582-610]`. Collision generation was optimised twice (6.0.9 and 6.0.10) for memory and CPU `[v changelog.md:16, 70]`.
- Client rendering: a `ClientContraption` builds a `VirtualRenderWorld` of the structure and one block entity per rendered block entity `[v ClientContraption.java:51-123]`. `ContraptionVisual` bakes **the whole static structure into one model and draws it as a single `TransformedInstance`** `[v ContraptionVisual.java:84-120]`. Block entities and actors that move or animate (drills, deployers) get their own child visuals attached through an embedding `[v ContraptionVisual.java:122-160]`. The structure mesh is rebuilt only when `structureVersion` changes `[v ClientContraption.java:94-101]`.
- Sync limit: if the contraption data is too large the client may get none, and the visual then returns early `[v ContraptionVisual.java:76-79 "could be null if it wasn't synced (ex. too much data)"]`. Create also limits contraption sync size `[doc changelog 6.0.x: "Avoid double nbt to bytes serialization in contraption sync limiting"]`.
- The block entity stays valid while moving: `KineticBlockEntity.warnOfMovement` sets `wasMoved`, and `read` then skips kinetic data `[v KineticBlockEntity.java:248-251, 498-500]`.

**Cost lesson for Nublar `[inf]`:** the "bake the static part once, instance the moving parts" split is the right shape for *any* large moving thing. For Nublar: a tour vehicle is a GeckoLib entity (one model), not a contraption, so this does not apply directly. It applies to fence rendering (bake the static wire mesh, animate only a spark or sag).

**Fit/conflict:** Contraptions let players build their own cranes, drills and farms. In a Jurassic Park mod that is off-tone (a lab is a lab, not a machine puzzle), and an arbitrary 2048-block moving structure over a paddock is a containment risk. Verdict: **Avoid as a feature.** Add `NON_MOVABLE` tags only if Create is installed.

## 7. Known lag sources in Create and how they were handled

From the Create changelog (6.0.x, all `[doc changelog.md]`) and the code:

| Source | Fix or limit | Lesson for Nublar |
| --- | --- | --- |
| Contraption collision | Optimised memory and CPU (6.0.9, 6.0.10) `[doc changelog.md:16,70]`; block cap 2048 `[v CKinetics.java:33]` | Cap sizes in config from day one |
| Global railway manager on player login | Optimised (6.0.9, 6.0.10) `[doc changelog.md:17,69]` | A global manager that touches all data at login must be incremental |
| Basin recipe lookup every tick | Optimised (6.0.x) `[doc changelog.md:143]` | Cache recipe lookups; re-check only when inventory changes (B12) |
| Tree cutter scans | Optimised `[doc changelog.md:144]` | Bound any block scan |
| Vault comparator updates and inventory access | Optimised `[doc changelog.md:263-265]` | Do not notify neighbours on every inventory change; compute the signal on change only |
| Fluid pipe visuals | Flywheel visuals for windowed pipes `[doc changelog.md:398]` | Rendering fix: instance, do not redraw |
| Mechanical crafter animation on empty frames | Less time on empty frames `[doc wiki changelog 6.0.0]` | Skip the render path when idle |
| Steam engine sounds | At most 4 sounds per side `[doc wiki changelog 6.0.0]` | Cap sound count per source group (see `SoundScapes`) |
| Mixer slower than recipe speed still animating | Fixed `[doc changelog.md:43]` | Idle machines draw nothing and animate nothing (matches the Nublar design rule) |
| Rendering jitter when walking on belts and depots | Fixed `[doc changelog.md:50]` | Moving surfaces that carry players need matching client and server motion; keep this in mind for tour vehicle floors |
| Unbounded assemblies | `maxAssemblyLength` limited to 512 and `maxBogeyCount` to 200 `[v CTrains.java:9-10; doc changelog.md:36]` | Hard caps in config ranges |
| Fan scans | `fanBlockCheckRate` = 30 ticks `[v CKinetics.java:28]` | Rate-limit airflow-like scans |
| Ejector scans | `ejectorScanInterval` = 120 ticks `[v CKinetics.java:23]` | Rate-limit target scans |
| Kinetic heartbeat | `kineticValidationFrequency` = 60 ticks `[v CKinetics.java:17-18]` | Validate on a slow timer |

Common pattern: **a tuning value in the server config for every scan interval and every size cap.** Nublar's TOML (decided: global balance numbers in TOML) is the right home for the same.

## 8. Trains and track

**In game:** the player lays track with a track item, places a station, assembles a train from bogeys and carriage contraptions, sets a schedule, and the train runs on its own between stations. Signals divide the track into blocks. Trains keep running while the player is away `[doc/inf]`.

**Mechanism:**
- Data model: `GlobalRailwayManager` holds maps of track graphs, signal edge groups and trains `[v C/content/trains/GlobalRailwayManager.java:40-48]`. Trains are tracked in two lists (waiting and moving) so the longest-waiting ticks first `[v GlobalRailwayManager.java:225-266]`. The whole tick returns early outside the overworld `[v GlobalRailwayManager.java:193-195]`. Save data is a `SavedData` `[v GlobalRailwayManager.java:91-97]`.
- A train has a navigation target that refreshes at distance milestones (100, 50) and not every tick `[v C/content/trains/entity/Train.java:546-566]`.
- Entities are spawned from data. `Carriage.manageEntities` creates a carriage entity only when `isPositionEntityTicking` is true for its anchor chunk, saves and removes the entity when it leaves the ticking area, and discards entities in dimensions the carriage no longer occupies `[v Carriage.java:260-320; CarriageEntityHandler.java:39-43]`. So an off-screen train costs map updates only.
- Network cost control: each train has `tickOffset = random(0..99)` so carriage syncs spread over time `[v Train.java:190, 1081-1083]`, and each carriage entity type has an `updateInterval` for positional sync with client-side interpolation `[v CarriageContraptionEntity.java:242, 276]`.
- Caps: `maxTrackPlacementLength` 32, `maxAssemblyLength` 128, `maxBogeyCount` 20, top speed 28 blocks/s (powered 40) `[v C/infrastructure/config/CTrains.java:7-20]`.

**Fit for Nublar:** high for B22 (tour vehicles) and the tour track idea. The decided Beta direction is: Jeep (player-driven), 1992 Ford Explorer (tour; follows a player-laid track, has stops, can run driverless, can be driven by hand). A Create-style graph is more than that needs. Nublar needs three things from the pattern:
1. A **track graph as data** (nodes along the player-laid track), stored in `SavedData`.
2. A **tour vehicle that exists as data** while no player is near, and as an entity when its chunk is entity-ticking.
3. **Staggered sync** with client interpolation.

**Conflict:** signals, bogeys, schedules with conditions, and multi-dimension travel are rail-simulator features. They add UI and many failure modes. Nublar's tour is "ride the track with stops".

**Create trains in a Nublar park (compat idea):** a player could run Create trains as a park monorail. That needs only Nublar's tour stops and animals to ignore the train. It needs no Nublar code except entity-type tags so dinosaurs flee or ignore a train carriage entity `[inf]`. Create also makes Wardens hear trains `[doc changelog 6.0.x]`, which has no effect on Nublar.

Verdict: **Adapt (pattern)** for B22. **Compat (none needed)** for Create trains themselves.

## 9. Logistics: belts, funnels, depots, packagers, stock ticker

**In game (belts, funnels, depots):** items move on mechanical belts powered by a shaft. Funnels move items between belts and inventories. A depot is a static place for one item that deployers or arms can use. Chutes drop items down. This is the classic "factory floor".

**In game (Create 6.0 high logistics):** a packager boxes items from an inventory into cardboard packages with an address. A package frogport or postbox sends packages. A stock link connects an inventory to a network, and a stock ticker shows everything in the network; the player requests items on a screen and packagers send them `[doc wiki changelog 6.0.0; v C/content/logistics/ dir listing]`. The Create wiki calls the block a "stock ticker"; the in-game keeper UI is `StockKeeperRequestScreen` `[v C/content/logistics/stockTicker/StockKeeperRequestScreen.java]`.

**Mechanism (6.0 logistics):**
- Networks are identified by a UUID frequency. `GlobalLogisticsManager` keeps `Map<UUID, LogisticsNetwork>` with `totalLinks` and `loadedLinks` as `GlobalPos` sets, saved with `SavedData` `[v C/content/logistics/packagerLink/GlobalLogisticsManager.java:15-80]`. It tracks loaded and unloaded links separately, so a network does not need all chunks loaded to be counted.
- Stock totals use two `TickBasedCache`s: an "accurate" cache with a 1-tick lifetime and a normal cache with 20 ticks `[v C/content/logistics/packagerLink/LogisticsManager.java:36-37]`. Stock queries from a screen hit the 20-tick cache.
- Duplicate inventories (one chest seen through two links) are removed by an `InventoryIdentifier` set before summing `[v LogisticsManager.java:50-62]`. This is a public API for addons `[v C/api/packager/InventoryIdentifier.java]`, and addon inventories can register `UnpackingHandler`s `[v C/api/packager/unpacking/UnpackingHandler.java]`.
- A packager refuses new requests when it holds too many packages (`isTooBusyFor`) `[v LogisticsManager.java:88-91 broadcastPackageRequest]`.

**Cost lesson `[inf]`:** a cache with a short time-to-live, keyed by network id, is the right tool for "how much DNA is in storage". Nublar's linked machine tabs (B12) and any "storage overview" screen should query a cached summary, not walk inventories on every open.

**Fit for Nublar:** the *idea* matches B12 (tabs to switch between linked machines) and a possible later "DNA storage network": sequencer output goes into a drive, the printer pulls from it. Create's packaged-request model, with addresses and a postbox, is off-tone (it is a shopping and mail game). Belts and funnels as a lab feed also clash with lab look.

**Integration idea (Compat):** make Nublar items movable by Create belts and funnels, which needs only standard item capabilities. Create's belts and funnels talk to any inventory exposing the NeoForge item handler `[inf]`. If Nublar machines expose item capabilities (B12 acceptance criterion: hopper and pipe input), Create automation works with no Nublar code.

Verdict: **Not relevant** (adopting stock keeper), **Compat (free)** via capabilities, **Adapt** the TTL-cache idea.

## 10. Deployers, mechanical crafting, sequenced assembly

### 10.1 Deployers and mechanical crafters

- A **deployer** is a kinetic block that acts like a player: it holds an item and uses it on a block or an item in front. It spawns a `DeployerFakePlayer` on the server `[v C/content/kinetics/deployer/DeployerBlockEntity.java:78, 148]`. It drives deploying recipes `[v DeployerBlockEntity.java:578 AllRecipeTypes.DEPLOYING]`.
- A **mechanical crafter** is a grid of crafters. Players arrange N by M crafters, load ingredients, and the machine crafts big recipes `[v C/content/kinetics/crafter/]`; the recipe type is vanilla-like crafting data `[inf]`.
- Fit: these are *tools for automation*. Nublar's fossil processor, sequencer, gene editor and printer already are the crafting machines. A deployer is a "do any block interaction" tool. In a park, deployer automation of a fence or of a feeder would bypass design (repair spool, player-filled feeder). Conflict. Verdict: **Avoid** as a feature.

### 10.2 Sequenced assembly (the useful part)

- A **sequenced assembly recipe** is a data recipe: an input item goes through a list of steps (deploying, pressing, cutting, filling and so on) for `loops` repeats, passing through an `incomplete_` transitional item; the result is picked from a weighted list `[v src/generated/resources/data/create/recipe/sequenced_assembly/precision_mechanism.json:1-60]`.
- It is **pure data** for an addon. The recipe type is `create:sequenced_assembly`; the ingredients and results may be any mod's items. The addon only needs the transitional item registered in its own mod or in Create's namespace `[v same file; inf for transitional item rule]`. Addons can generate recipes with `SequencedAssemblyRecipeGen` in `C/api/data/recipe/` `[v SequencedAssemblyRecipeGen.java:17-36]`.
- Fit: a datapack with conditional loading (load only if Create is present) could add "Create route" recipes that make Nublar *parts* (for example a machine chip or a syringe) in a Create factory. It is a pure content item with no runtime cost. It has to respect the design rule "recipes from vanilla materials for everything" (module 6), so Create recipes would be an *alternate* route, not a requirement.
- Verdict: **Compat (data only)**, low priority, effort S per recipe, needs a Create 26.x build to test.

## 11. How machines visually show work

From the source, the Create language of "this machine is working" is:
- **The part spins** and the speed matches the RPM, computed on the GPU (section 5).
- **Sound scapes** rise with speed and pitch with it `[v KineticBlockEntity.java:599-612]`.
- **Particles** at the work point (crushing, milling, kinetic rotation indicators) `[v KineticEffectHandler.java:49-70; RotationIndicatorParticle]`.
- **Item display** on the part: items sit on a belt or depot and are drawn by the block entity renderer `[v C/content/logistics/depot/DepotRenderer.java; kinetics/belt/BeltRenderer.java]`.
- **Overstress feedback** as above.
- **No GUI for most machines.** The state is the animation; the stats come from goggles.

For Nublar the machines have screens (decided: tabs, upgrades change look). The Create idea that carries over is *state shown in the world*: an incubator with a lit bulb and a turning egg arm when working, dark when stalled; a sequencer with a moving head only while a job runs; a printer light that reads "no power". It matches the Nublar rule that an idle machine draws nothing and animates nothing.

## 12. Fabric: official multiloader versus the Fabric port

| Question | Answer |
| --- | --- |
| Is there an official Create multiloader? | No. The official repo builds **NeoForge only** on 1.21.1 `[v C/ imports are net.neoforged; gradle.properties neo_version]`. Create-Fabric is a separate repo and a separate team (Fabricators of Create) `[v gh repo view; doc wiki development-status "A dedicated team maintains a Fabric port"]` |
| How far behind is Fabric? | Modrinth lists Fabric Create for 1.20.1, 1.19.2 and 1.18.2 only `[doc]`. The 1.21.1 Fabric branch has not changed since 2025-03-11 (version string `6.0.0.0+mc1.21.1`), while the official branch has shipped 6.0.x up to 6.0.11 `[v git log; build.gradle.kts:63]`. The 1.20.1 Fabric branch got a "prepare for release" commit on 2025-12-02 `[v gh api]`. So Fabric is **one to two Minecraft versions and several Create releases behind** `[inf from dates]` |
| What does the port bundle? | It jar-in-jars Registrate-Fabric, Forge Config API Port, Flywheel and Milk Lib, and Ponder when available `[v create-fabric build.gradle.kts:104-118]`. The pinned Flywheel (1.0.1-11) and Ponder (1.0.44) are older than the official ones (1.0.6 and 1.0.85) `[v build.gradle.kts:10-11; Create gradle.properties]` |
| Does the port behave the same? | Not fully. `DIFFERENCES.md` lists intended differences (hose pulley capacity, fluid placement "due to no Fabric Fluid API", display-link fluid units) `[v create-fabric DIFFERENCES.md:1-5]` |
| What does this mean for Nublar (NeoForge and Fabric on 26.2)? | Any Create compat is a **NeoForge-first, later** item. A Create compat module cannot ship on both loaders on day one, and neither loader has a Create 26.x build today `[v branches]` |

## 13. Fit and conflict with Project Nublar's design

Design inputs are the lead-confirmed decisions (power, fences, machine framework, vehicles, park tech). Short form:

| Create feature | Where it fits | Where it conflicts |
| --- | --- | --- |
| Stress and speed model | Pattern for B14 `PowerNetwork` and fence draw. Overstress feedback for "machine stops, fence tested" | A second quantity (RPM) and breaking blocks. Nublar decided on FE through Balm, a basic and large generator, pylons and lines |
| Goggles and hover tooltips | B12 machine status, B14 network status, B20 feeder level | Create's goggle item and interface cannot be used without Create |
| Gauges (stressometer) | A "power meter" block for B14 (fence line draw, supply) | Needs a design decision (open question 2) |
| Ponder | Tutorials for lab, fence, feeder, track | Release 1, not Beta. Ties Nublar to Ponder's pinned Minecraft version |
| Flywheel and Vanillin | None in Beta | Shader packs disable it. Nublar's cost is GeckoLib entities |
| Contraptions | None | Off-tone; containment risk |
| Trains and track | B22 tour vehicle data model, track graph, staggered sync | Signals, bogeys, schedules, rail-simulator UI |
| Belts, funnels, chutes | Free interop through item capabilities | A factory floor in the lab breaks the clean look |
| Stock ticker and packagers | B12 "linked machines" tabs; the TTL cache idea | Shopping and mail tone |
| Deployers and arms | None | Bypass the repair spool, the player-filled feeder and the "place parts in order" rule |
| Sequenced assembly | Data-only alternate recipes for parts | Must stay optional (recipes from vanilla materials for everything) |
| Ambient sound pooling | B15, B12, B25 | None |
| Server-config caps for every scan and size | B25, B00 TOML | None |

**Tone summary:** Create is a *builder's engineering toy*. Players invent layouts, and the world is the interface. Nublar is a *park management survival game* with a fixed lab chain and a fence as a living constraint. The overlap is in **feedback** (the world shows state) and **performance discipline** (cheap networks, caches, caps), not in layout play.

## 14. Integration ideas, ranked

1. **Item, fluid and energy capabilities on every Nublar machine (S each, already planned).** This lets Create belts, funnels and arms feed machines with no code, on the day Create has a 26.x build. It also lets CC: Tweaked and probe mods read them. It costs nothing extra in Nublar. Platform overlap: NeoForge capabilities and the Fabric Transfer API cover item and fluid; energy needs Balm or Team Reborn Energy on Fabric (decided: Balm). Verdict: **Adopt** (already in B12 and B14 acceptance).
2. **A "Rotational Dynamo" adapter in a separate optional compat module (M).** One block that is a Create `KineticBlockEntity` consumer, with an impact registered through `BlockStressValues.IMPACTS`, and that produces FE into Balm energy storage at a rate set by RPM. Nublar machines stay on FE. Players who like Create can power a lab from a water wheel. The reverse (FE to shaft) is a second block `[inf]`. This sits in a `nublar_create_compat` source set or mod, depends on Create only there, and is NeoForge first. Verdict: **Compat, deferred** until Create has a 26.x build and Nublar's B14 is done.
3. **Ponder plugin for Nublar (M).** See 4.3. Verdict: **Compat, optional, Release 1.**
4. **Data-only sequenced assembly recipes (S each).** See 10.2. Verdict: **Compat, low priority.**
5. **Create trains as a park monorail (S).** Entity-type tags only. Verdict: **Compat, free.**
6. **Create goggles on Nublar blocks (L).** Needs Nublar block-entity subclasses that implement Create's interface. Verdict: **Avoid.**
7. **Flywheel visuals for fence posts or machines (L).** Verdict: **Avoid** in Beta.
8. **Stressometer-like meter for Nublar power (S to M).** A block with a dial for supply and draw in FE per tick; uses Create's idea only. Verdict: **Adapt**, pending the lead (open question 2).

## 15. Techniques worth copying (with the Nublar target)

1. **Dirty-flag aggregate network** (`KineticNetwork`). B14: `PowerNetwork` keeps `supply` and `draw` floats and a dirty flag. It recomputes on topology change and on a consumer's state change, not each tick. Effort M. Expected effect: removes per-post per-tick energy extraction in the 60-post fence case `[inf]`.
2. **Push state only on change, animate on the client from a clock** (`KineticBlockEntity` read/write plus the rotating shader). B12 and B15: the server sends "working" or "idle" flags and a start tick; the client plays a loop. Effort S. Expected effect: block-entity update packets fall from per-tick to per-state-change.
3. **`blockChanged` for sync, not full block updates** (`SyncedBlockEntity.sendData`). B12, B15, B20. Effort S.
4. **Pooled ambient sound by group and pitch** (`SoundScapes`). B15 fence hum, B12 machine hum. Effort S. Expected effect: a fixed sound-instance count for any number of posts.
5. **Short-TTL cache for network-wide summaries** (`TickBasedCache`, 1 and 20 ticks). B12 linked-machine tabs, B20 feeder overviews. Effort S.
6. **Abstract simulation plus an entity only near players** (`Train`, `Carriage`). B22 tours, possibly B26 for herds far from players. Effort L for tours. Expected effect: server cost near zero for unwatched tours `[inf]`.
7. **Stagger periodic work with a per-object random offset** (`Train.tickOffset`). B15, B20, B12 scans. Effort S. Stops 300 posts from doing their slow tick on the same game tick.
8. **Distance-banded update rate** (Flywheel `BandedPrimeLimiter`). B25 for any client-side per-object update (fence wire sag, dinosaur head-look). The divisors 1, 2, 3, 5, 7, 11, 13 and so on are primes, so bands do not align `[v BandedPrimeLimiter.java:7-28]`. Effort S.
9. **Bake the static, instance the dynamic** (`ContraptionVisual`). B15 wires: cache the wire mesh when the wire changes. Effort M.
10. **Caps in server config for every size and scan** (`CKinetics`, `CTrains`). B00 TOML and B25. Effort S.
11. **Idle machines render and tick nothing** (mixer fix). B12 acceptance. Effort S.

## 16. Closing table

| Technique | Source | Nublar module | Verdict | Effort | Expected effect |
| --- | --- | --- | --- | --- | --- |
| Per-network supply and draw sums with dirty flag; overstress = stop | `KineticNetwork.java:107-175`, `KineticBlockEntity.java:97-169` | B14, B15 | Adapt | M | Fence and machine draw cost scales with edits, not ticks |
| Topology work on place and break only; flicker guard | `RotationPropagator.java:206-381, 33` | B14 | Adapt | M | No oscillation loops; cheap idle network |
| Heartbeat validation on a slow timer | `KineticBlockEntity.java:112-115` | B14, B15 | Adopt (pattern) | S | Self-heals without scans |
| Hover overlay fed by synced block-entity fields | `GoggleOverlayRenderer.java:63-140` | B12, B14, B20 | Adapt | M | Player sees why a machine stopped, no menu |
| Overstress feedback (smoke, tint, gauge) | `KineticEffectHandler.java`, `StressGaugeBlockEntity.java` | B12, B14 | Adapt | S to M | Clear fault state |
| Create-goggle compat (implement `IHaveGoggleInformation`) | `IHaveGoggleInformation.java` | B12 | Avoid | L | Low value for the cost |
| Ponder plugin, structure NBT scenes | Ponder `PonderPlugin.java`, docs | R01 or polish | Compat (optional) | M | Learn the build order without a wiki |
| Flywheel instancing for rotating parts | `rotating.vert`, `AllInstanceTypes.java` | B25 only if a profile says so | Avoid (Beta) | L | Small gain; two code paths because of shader packs |
| Vanillin | `VanillaVisuals.java` | None | Not relevant | n/a | Does not cover GeckoLib |
| Pooled ambient sounds | `SoundScapes.java:8-120` | B15, B12 | Adopt (pattern) | S | Bounded sound count |
| `blockChanged` sync | `SyncedBlockEntity.java:44-47` | B12, B15, B20 | Adopt (pattern) | S | Smaller update traffic |
| TTL cache for network summaries | `LogisticsManager.java:36-37`, `TickBasedCache.java` | B12, B20 | Adopt (pattern) | S | Cheap storage overviews |
| Global data model; entity only in active chunks | `Carriage.java:260-320`, `CarriageEntityHandler.java:39-43` | B22 | Adapt | L | Near-zero cost for unwatched tours |
| Per-object random tick offset | `Train.java:190, 1081-1083` | B15, B20, B12 | Adopt (pattern) | S | Smooths tick spikes |
| Distance-banded update limiter | `BandedPrimeLimiter.java` | B25 | Adapt | S | Client per-object cost falls with distance |
| Bake static mesh once, instance moving parts | `ContraptionVisual.java:84-120` | B15 wires, B25 | Adapt | M | Removes per-frame wire geometry |
| Config caps and scan intervals | `CKinetics.java`, `CTrains.java` | B00, B25 | Adopt (pattern) | S | Bounded worst case |
| Create belts, funnels and arms through item capabilities | NeoForge capabilities | B12, B20 | Adopt (free) | S | Interop at no Nublar cost |
| Rotational Dynamo adapter (shaft to FE) | `BlockStressValues.java:14-18` | New compat module | Compat (deferred) | M | Create players can power a lab |
| Sequenced assembly alternate recipes | `sequenced_assembly/precision_mechanism.json` | B13 compat data | Compat (data) | S each | Optional route |
| Create trains as monorail | `GlobalRailwayManager.java` | B22 tags | Compat (free) | S | Optional content |
| Contraptions, deployers, mechanical crafters | `content/contraptions/`, `kinetics/deployer/` | None | Avoid | n/a | Off-tone; bypasses rules |
| Stock ticker and packager network | `content/logistics/packagerLink/` | None | Not relevant | n/a | Off-tone |

## 17. Open questions (real design gaps)

1. **Should Nublar offer any tutorial system in Release 1 (Ponder-style), and is an optional dependency acceptable?** The plan has no such module. Ponder pins one exact Minecraft version, so a Ponder 26.2 build must exist when this is wanted.
2. **Does the park want a visible power meter (a block with a dial, in FE per tick for supply and draw), or only the hover overlay?** The Create stressometer shows how much one block can teach. B14 does not name a meter.
3. **Is a "worn goggles" item part of the player-feedback design?** The tablet is out of Beta. A hover box can be always on, or gated by an item. The choice changes B12 and B14 UI work.
4. **Does the lead want a Create compat plan on the roadmap at all?** Create has no 26.x build (the wiki says 26.1 is in progress), Fabric Create lags further, and the dynamo adapter is NeoForge first. If yes, it needs its own module after P2.
5. **What counts as the "far from players" state for tour vehicles (B22)?** The train pattern needs a rule for when a tour vehicle becomes data only (chunk not entity-ticking) and what happens to passengers. Players ride tour vehicles, so this rule is needed.
6. **Should fence wires move to a baked-mesh path in B15 or B25?** It is the one concrete render change this note suggests. It needs a profile of the current `ElectricWireRenderer` first (it draws leash quads per wire per frame `[v Nublar common/src/main/java/net/dumbcode/projectnublar/client/renderer/ElectricWireRenderer.java:22-53]`).
