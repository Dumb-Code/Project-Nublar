# Park security, control and communication mods: SecurityCraft, CC: Tweaked, Simple Radio, Alternate Current

Researcher note for the Nublar optimization and technology survey. Topic: what the player sees and does with cameras, keycards, alarms, computers and radios, what each costs the server and the client, and what Project Nublar must do to stay light and compatible.

Labels: `[v]` verified in source (path and line range at the commit below), `[doc]` from README, wiki, config comments or store text, `[inf]` my inference. Any performance number without `[v]` is an estimate. Paths are relative to the repo root unless noted. No file in Project Nublar was edited except this note.

## 1. Repos read

| Repo | Branch read | Commit and date | Licence (repo fact) | Loaders | MC versions | Size | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Geforce132/SecurityCraft | `26.2` | `b88421b` 2026-09-20 | MIT | NeoForge only (all imports are `net.neoforged`, no Fabric source set) `[v]` | Branches 1.20.1, 1.21.1, 1.21.11, 26.1, 26.2, 26.3 `[v]`. Mod version 1.10.2.1, NeoForge 26.2.0.57 `[v gradle.properties]` | 616 Java files, about 66 MB repo | Active (pushed 2026-10-03). Older branches `eol/*` |
| cc-tweaked/CC-Tweaked | `mc-26.2` (default branch `mc-1.20.x`; `master` is a stale 2022 stub) | `3829165` 2026-08-25 | Per-file SPDX headers, mostly MPL-2.0; `LICENSES/` holds MPL-2.0, MIT, Apache-2.0, CC-BY and the CCPL text `[v]` | NeoForge and Fabric `[v]` (`projects/forge`, `projects/fabric`) | Branches 1.20.x, 1.20.y, 1.21.x, 1.21.y, 26.1, 26.2, 26.3 `[v]`. Targets MC 26.2, NeoForge 26.2.0.53-beta, Fabric API 0.156.0+26.2 `[v gradle/libs.versions.toml:8-17]` | 802 Java files, about 26 MB repo | Active (pushed 2026-10-06). Version 1.120.2 on the 1.20.x branch |
| CodinGlitch/SimpleRadio | `1.21.1` (default branch `1.20.1`) | `67e2a55` 2026-06-18 | GPL-3.0 | NeoForge, Forge and Fabric source sets in one repo `[v]` | `1.21.1` branch: MC 1.21 to 1.21.4 `[v gradle.properties]`. `1.20.1`: MC 1.20 to 1.20.1. **No 26.x branch exists** `[v gh branches]` | 232 Java files, about 3 MB | Active but not on 26.x |
| SpaceWalkerRS/alternate-current | `main` | `d93fd52` 2026-09-15 | MIT | Fabric on `main` (MC 26.3, mod 1.9.0); branches `26.2` and `neoforge-26.2` exist but I did not clone them `[v gh branches]` | Branches from 1.14 to 26.3 `[v]` | 21 Java files, about 1.3 MB | Active |

Also checked by API only, not cloned: Simple Voice Chat (henkelmax/simple-voice-chat) has branches `26.1`, `26.2`, `26.3`, `26.4` `[v gh branches]`. Simple Radio needs it `[v gradle.properties: required=lexiconfig,simple-voice-chat]`.

## 2. What matters most (read this first)

1. **A remote camera view is cheap if it is the player's own view, and expensive if it is a second render.** SecurityCraft has both. The "mount a camera" mode swaps the player's camera entity, so the client still renders one world view `[v]`. The "Frame" block mode (a live picture on a block) runs a second full `renderLevel` per feed, up to 30 times per second at 512 by 512 pixels by default `[v]`. Nublar should copy the first and skip the second in Beta.
2. **The real server cost of a camera is chunk loading, not rendering.** SecurityCraft force-loads a square of `(2r+1)^2` chunks around a mounted camera, per viewer `[v]`. At a 12-chunk radius that is 625 chunks. Nublar can avoid all of it: view only chunks that are already loaded, and show "no signal" otherwise `[inf]`.
3. **CC: Tweaked already reads Nublar machines for free on NeoForge.** Its generic peripherals wrap the NeoForge `EnergyHandler`, item and fluid capabilities `[v]`. If Nublar machines and fence networks expose the standard capabilities (B12, B14, B20), a player's computer can read power and feeder levels with zero Nublar code. On Fabric only the inventory generic exists `[v]`, so energy and fluid need a small Nublar source there. Dinosaur counts and fence state need a small custom source on both loaders.
4. **Alternate Current shows the cost rule for any redstone block.** A redstone dust change costs dozens of block updates and shape updates in vanilla `[doc]`. Nublar blocks must emit a redstone signal only when their output value changes, keep `getSignal` a cheap blockstate read, and never touch neighbours per tick. SecurityCraft's camera `getSignal` does a block-entity lookup per call `[v]`; that is the pattern to avoid.
5. **Simple Radio is not available for 26.2 and its voice layer is a separate mod.** For Beta, park announcements should be vanilla sounds from a speaker block. Voice radio is an optional compat story for later.
6. **Block entities that tick every tick are the common cost in all four repos.** SecurityCraft's motion light scans entities every 5 ticks per light `[v]`. CC: Tweaked avoids ticking idle block entities with a scheduler `[v]`. Nublar's fence and control blocks should follow CC: Tweaked.

## 3. SecurityCraft (Geforce132/SecurityCraft)

SecurityCraft is a base-protection mod: reinforced blocks, owner-locked doors, keypads and keycards, laser tripwires, sentries, cameras. Its tone is player-versus-player base defence. Nublar's tone is containment of animals. A few pieces fit; most do not.

### 3.1 Security cameras and the camera monitor (mounted view)

**In game:** the player places a camera on a wall or ceiling, takes a Camera Monitor item, right-clicks cameras to bind them (up to 30 per monitor `[v items/CameraMonitorItem.java:32]`), then uses the monitor to pick a camera. The player's view jumps to the camera. Movement keys turn the camera inside a limited arc, two keys zoom, Shift leaves `[v entity/camera/CameraController.java:43-52, 131-190]`. A camera can have a redstone module and night vision (config-gated `allowCameraNightVision`) `[v CameraController.java:192-203]`.

**Mechanism (server):**
- `SecurityCameraBlock.mountCamera` spawns a `SecurityCamera` entity at the block, sets `serverPlayer.camera` directly, and sends a `SetCameraView` packet. A comment says vanilla `ServerPlayer#setCamera` is not used "because it also teleports the player" `[v blocks/SecurityCameraBlock.java:148-176, comment at 161]`.
- The view distance is the player's requested view distance clamped between 2 and the server view distance `[v SecurityCameraBlock.java:154]`.
- It then force-loads every chunk in a square of that radius through a NeoForge `TicketController` `[v SecurityCameraBlock.java:163-169; SecurityCraft.java:74, 141-143]`. On dismount `SecurityCamera.discardCamera` un-forces the same square `[v entity/camera/SecurityCamera.java:147-161]`.
- The camera entity is `noPhysics`, always ticking, and discards itself if its block is gone `[v SecurityCamera.java:42, 73-78, 184-186]`.
- Because chunk streaming follows the player's position in vanilla, SecurityCraft patches `ChunkMap` with mixins so chunks around the camera entity are sent to the viewer, block updates reach them, and they are not dropped `[v mixin/camera/ChunkMapMixin.java:48-118]`. Another mixin makes entities near the camera visible to the viewer: any entity within the view distance of the camera counts as tracked `[v mixin/camera/TrackedEntityMixin.java, whole file 52 lines]`.
- There are 15 camera mixins in total (`mixin/camera/`: Camera, ChunkMap, ClientChunkCache, CloudRenderer, FogRenderer, GameRenderer, Gui, LevelExtractor, LevelRenderer, LocalPlayer, Minecraft, PlayerList, SectionUpdateTracker, ServerPlayer, TrackedEntity) `[v]`. This is the price of a remote view on a vanilla server: every part of vanilla that assumes "the player sees where the player stands" needs a patch. The mod also carries a debug flag that reports other mods that reset the camera entity `[v ConfigHandler.java:56-58]`.

**Mechanism (client):** `CameraController` hides movement keys during the pre-tick so the player body does not walk, then replays them in the post-tick as camera turns. It sends a rotation packet each tick the head turns `[v CameraController.java:59-105]`.

**Cost estimates (estimates unless marked):**
- Server chunk tickets: `(2r+1)^2` chunks per mounted camera `[v formula]`. r = 8 gives 289, r = 12 gives 625, r = 16 gives 1,089. I did not verify which ticket level the controller grants; if it is an entity-ticking level, a camera far from the base wakes a distant herd and its AI `[inf]`.
- Client: one world render, as normal. The cost is extra chunk meshing and extra memory for the remote chunk area, bounded by the radius `[inf]`.
- Network: a burst of chunk packets at mount time, then entity packets for the camera's area `[inf]`.

**Lesson for Nublar:** B15 (fences), B14 (power), B25 (cleanup and performance), and the backlog item "security cameras".
- Adapt the idea: a camera block plus a monitor (item or block) that lets one player view one camera at a time.
- Do **not** force-load. If the camera chunk and its neighbours are loaded, show the view. If not, show a "no signal" screen. This costs 0 tickets, needs no `TicketController`, and matches the film's feel (a feed that drops). `[inf]`
- Clamp the view radius with a Nublar config (for example 6 to 10 chunks) instead of the player's full view distance. `[inf]`
- Power: a camera draws a small energy amount from B14 only while viewed, or while the paddock network is up. Without power the screen shows static. `[inf]`
- Keep the mixin surface small. The vanilla spectator path (`ServerPlayer#setCamera` plus `ClientboundSetCameraPacket`) already streams chunks to the viewer, because it moves the player. A Nublar variant could move the player body to the camera and park it safely, with the player invisible and invulnerable, and move it back on exit. That needs no `ChunkMap`, `TrackedEntity` or `PlayerList` mixins, but it exposes the body at the camera position to dinosaur targeting and loads chunks as the player really is there `[inf]`. This is a design decision for the lead (see open questions).

**Compatibility duty:**
- Dinosaurs use an entity tracking range. A camera view that shows a herd 100 blocks away needs those dinosaurs tracked for the viewer. SecurityCraft solves it with the `TrackedEntityMixin`. If Nublar uses the spectator route, vanilla tracking follows the body position and no patch is needed `[inf]`.
- Sodium, Iris and the like: the mounted view uses the normal renderer, so no special rule. The Frame feed code reaches into `LevelRenderer.visibleSections`, `SectionRenderDispatcher`, `GameRenderer.mainRenderTarget` `[v FrameFeedHandler.java:62-208]`. SecurityCraft ships a compat package for Sodium-family mods (`compat/ium`) `[v]`. A second render in Nublar would break with Sodium unless similar glue exists. Another reason to skip it.
- Entity Culling (EntityCulling) hides entities that the main camera cannot see. A remote camera view needs those entities visible. The mounted view replaces the camera entity, and EntityCulling reads the camera it is given, so it should follow `[inf]`. Test with EntityCulling installed.

**Platform overlap:** vanilla has the camera-entity packet and the spectate path. NeoForge has `TicketController` (forced chunk tickets). I found no Fabric API with the same ticket-controller shape `[inf]`; vanilla chunk tickets exist but I did not check their public access. No library supplies a "remote view with chunk streaming" feature.

**Verdict:** **Adapt** (mounted view only, no force-loading, small config radius, power draw). Effort M if the spectator route is chosen, L if SecurityCraft's mixin route is chosen.

### 3.2 Frame blocks with live camera feeds

**In game:** a Frame block, placed on a wall, shows a live picture from a linked camera, like a CCTV wall. Walking near it shows moving video.

**Mechanism `[v entity/camera/FrameFeedHandler.java, CameraFeed.java, ConfigHandler.java]`:**
- Each feed owns a GPU `TextureTarget` (`CameraFeed.java:50`), sized by the client config `frame_feed_resolution`, default 512, range 1 to 16384 `[v ConfigHandler.java:77-79]`.
- Each capture swaps the camera to a temporary `Marker` entity at the camera position (`FrameFeedHandler.java:102, 126-134`), shrinks the window size to 100 by 100 (`:109-110`), redirects `mainRenderTarget` to the feed target (`:143`), runs `gameRenderer.extract` and `renderLevel` (`:147-149`), and then restores 25 pieces of player, camera and renderer state (`:176-207`).
- Throttle: the client only renders feeds whose frame block is inside the player's view frustum (`:123-124`, `CameraFeed.java:140-156`). A time-slice scheduler caps work: a default limit of 30 frames per second per feed (range 10 to 260), spread across the feeds `[v FrameFeedHandler.java:236-261; ConfigHandler.java:81-83]`.
- View distance: the minimum of the frame option, the client config (default 16, range 2 to 32), the server config `frame_feed_view_distance` (default 16, range 2 to 32), and the player's render distance `[v FrameFeedHandler.java:335-339; ConfigHandler.java:73-75, 241-243]`.
- Server side, a frame link force-loads the area around the camera, 16 chunks per tick through a queue, and a limit `frame_feed_forceloading_limit` defaults to -1 (unlimited) `[v SecurityCameraBlockEntity.java:104-120, 330-365; ConfigHandler.java:245-247]`. A comment says a default 32 by 32 chunk area is fully loaded in about 40 server ticks `[v SecurityCameraBlockEntity.java:106]`.
- A feed that throws an exception is closed and the player is told in chat `[v FrameFeedHandler.java:151-156]`.

**Cost estimates:** each active feed is roughly one extra world render per interval. At 30 fps with 5 feeds in view, that is up to 150 extra world renders per second `[inf]` (the time-slice scheduler lowers this when the main frame rate is low `[v :236-241]`). Server: with the default radius of 16, one feed asks for up to `33^2 = 1,089` forced chunks `[inf from v formula]`.

**Lesson for Nublar:** B25. A second render is the most expensive client feature in this whole survey. If Nublar ever wants a control-room screen wall, use (a) a mounted view only, or (b) a low-rate "snapshot" feed (one frame every 2 to 5 seconds, 128 by 128 pixels) `[inf]`. Neither is Beta work.

**Compatibility duty:** a second render breaks shaders and render-pipeline mods that cache per-frame state (Iris, Sodium). SecurityCraft has a hand-written fix for its sky renderer cache (`FrameFeedHandler.java:138`) and for the fog buffer (`CameraFeed.java:51, 192`) `[v]`.

**Platform overlap:** none. No loader API renders the world twice.

**Verdict:** **Avoid** for Beta. Revisit for a later release as a snapshot feed.

### 3.3 Keycards, keycard readers, keypads and codes

**In game:** the player holds a keycard of level 1 to 5, or a limited-use card, and right-clicks a reader. A matching card pulses a redstone signal for a set time and the door opens. A keypad asks for a code.

**Mechanism:**
- The card is an item with a data component `KEYCARD_DATA` that holds a level, a signature (an int), and a limited-use counter. The owner is a second component, `OWNER_DATA` `[v blockentities/KeycardReaderBlockEntity.java:177-212]`.
- The reader stores `acceptedLevels[]` and a `signature` and checks owner, signature, level, and uses left in order, returning a translatable error message for each failure `[v :47-101, 177-212]`.
- On success it flips `POWERED`, updates indirect neighbours and calls `level.scheduleTick(pos, block, signalLength)`. The scheduled tick resets it `[v :223-240]`. There is no ticking block entity for the pulse.
- A "Codebreaker" item defeats readers with a time window stored in a component (`:128`).

**Lesson for Nublar:** keycards fit a control room, a lab door or a feeder-room door. In the design, **gates are not in Beta** `[doc project-plan B15 "Gates and the overhanging fence are not in Beta"]`, and a keycard system has no owner module in the plan. When the lead decides, the pattern is: a data component on a vanilla item, a reader block with a `POWERED` state, a pulse through `scheduleTick`, and no ticking block entity. This is about 150 lines `[inf]`. No library is needed; data components are vanilla.

**Compatibility duty:** none for rendering. For redstone, emit the signal only when the state flips (see section 6).

**Platform overlap:** vanilla data components, `scheduleTick`, `BlockStateProperties.POWERED`. NeoForge and Fabric both support data components.

**Verdict:** **Adopt (pattern)** for the unassigned gate and door backlog. Not Beta.

### 3.4 Laser blocks

**In game:** two laser blocks within 5 blocks (config `laserBlockRange`, default 5 `[v ConfigHandler.java:148-150]`) draw an invisible tripwire field. A player or mob that crosses it triggers a redstone pulse, and with a harming module takes damage (config `laser_damage`, default 10 health points `[v ConfigHandler.java:200-202]`).

**Mechanism:** the emitter places `LaserField` blocks between the two ends when placed or when a neighbour changes `[v blocks/LaserBlock.java: `setLaser` from line 75, `neighborChanged` at line 160]`. The field is a block, and the trigger is `entityInside`, which fires only for entities overlapping the field block `[v blocks/LaserFieldBlock.java:73-118]`. The handler walks up to `laserBlockRange` blocks along the field to find the owning laser block, checks the allow-list, and toggles power. A 500 ms guard prevents rapid retoggling, using wall-clock time `[v :89-96]`. So there is no per-tick scan; cost scales with entities overlapping field blocks.

**Lesson for Nublar:** this is a good model for a **fence-break or escape tripwire**: event-driven, zero cost when nothing touches it. Two cautions: (1) `entityInside` fires for every overlapping entity every tick. A T. rex with a multi-part hitbox (B27) overlapping several field blocks calls it many times per tick `[inf]`, so Nublar's variant should debounce per entity. (2) The 500 ms guard uses `System.currentTimeMillis()`, which does not pause with the game tick or stop under lag; use `level.getGameTime()` `[inf]`.

**Platform overlap:** vanilla `entityInside`. Nothing else needed.

**Verdict:** **Adapt** only if the lead wants a tripwire block. Otherwise **Not relevant**: escape detection is better done from the dinosaur's side (it already knows when it leaves its enclosure in B17).

### 3.5 Alarms

**In game:** an alarm block plays a siren when powered, with a range up to 100 blocks (config `maxAlarmRange` `[v ConfigHandler.java:156-158]`), custom sound and pitch, and volume that falls off with distance.

**Mechanism:** a ticking block entity. While powered it counts a cooldown, then for every `ServerPlayer` in range it sends a custom packet `ToggleAlarmSound.on(pos, volume, seed)` `[v blockentities/AlarmBlockEntity.java:47-72]`. The player list is scanned with a predicate on every cycle (every `soundLength * 20` ticks). The client then plays and loops the sound itself.

**Lesson for Nublar:** an escape alarm is a strong gameplay fit (film park siren). Cheaper implementation: on a rising signal edge, call vanilla `level.playSound` or `ServerLevel.playSeededSound` with a looping sound-event cadence driven by `scheduleTick`, not a ticking block entity and not a custom packet. Vanilla sound attenuation does the distance falloff `[inf]`. One block, about 60 lines.

**Compatibility duty:** none.

**Platform overlap:** vanilla sounds, `scheduleTick`.

**Verdict:** **Adapt** (vanilla sound path). Backlog "park signs and alarms".

### 3.6 Motion-activated light and sensors

**In game:** a light that turns on when a living entity walks within 5 to 20 blocks.

**Mechanism:** a ticking block entity that every 5 ticks runs `level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(radius), filter)` and flips a `LIT` blockstate only if the value changed `[v blockentities/MotionActivatedLightBlockEntity.java:23-46]`.

**Cost estimate:** one entity-section query per light per 5 ticks. A park with 40 motion lights costs 8 queries per tick on average `[inf]`. Each query touches the entity sections inside the AABB; with a radius of 20 that is up to 3 by 3 by 3 sections `[inf]`. Cheap alone, not cheap in bulk, and it ignores whether any player is near the light.

**Lesson:** the "write state only on change" check is good (see section 6). The scan is a scaling anti-pattern. For Nublar, motion lighting is a decoration; it should only scan when a player is within a few chunks, or use the shared per-level registry that B17/B20 already plan for dinosaurs `[inf]`.

**Verdict:** **Avoid** the scan; **Adopt** the "set state only on change" habit.

### 3.7 Ownership, modules and the block-reinforcement layer

**In game:** every protected block stores its placer. Others cannot open, break or reuse it unless on the allow-list or the same team. Modules (Redstone, Allowlist, Denylist, Harming, Smart, Storage, Disguise, Speed `[v misc/ModuleType.java:15-23]`) are items slotted into a block to add behaviour. Reinforced blocks are stone, glass, doors and so on with high blast resistance, and only the owner can break them (config `allow_breaking_non_owned_blocks`, default false `[v ConfigHandler.java:258-260]`). A reinforcer tool converts a vanilla block to its reinforced twin through a map `VANILLA_TO_SECURITYCRAFT` `[v api/IReinforcedBlock.java]`.

**Mechanism:** `Owner` holds a name string and a UUID string `[v api/Owner.java:26-40]`. `IOwnable.isOwnedBy` compares owners and also supports an "incognito mask" disguise `[v api/IOwnable.java:94-96]`. Every protected block is a block entity. There are hundreds of "reinforced" block classes, one per vanilla block (`blocks/reinforced/` holds dozens of classes; the repo has 616 Java files).

**Conflict with Nublar:** the whole reinforcement layer is aimed at players griefing players. Nublar's containment design is the opposite: dinosaurs should be able to damage fences, wires lose health, large animals knock posts down (B15). A "cannot be broken" block layer would undermine that tone and costs one block entity per block, which is a heavy memory and save-size cost `[inf]`.

**Lesson for Nublar:** an owner UUID on control blocks (cameras, monitors, the fence switch) is useful on multiplayer servers. If wanted, store a UUID in a data component on the item and copy it into the block entity at placement. Do not store names as strings; store the UUID and resolve the name on demand `[inf]`.

**Platform overlap:** NeoForge and Fabric have no ownership API. FTB Teams and other mods fill that gap; SecurityCraft has a compat package for FTB Teams `[v compat/ftbteams]`.

**Verdict:** reinforced blocks: **Avoid**. Module slots: **Avoid** (scope). Owner UUID on control blocks: **Adapt**, optional.

### 3.8 Sentries

**In game:** a turret entity with modes (camouflage, aggressive, idle; target players, mobs, or both) that shoots bullets `[v entity/sentry/Sentry.java:747-753]`. A remote tool controls it.

**Mechanism:** a normal `Mob` with `registerGoals` (ranged attack goal and a nearest-target goal) `[v Sentry.java:126-128]`. 802 lines.

**Conflict:** gun turrets that shoot "mobs" would shoot dinosaurs. The lead's design leaves the gun system (tranquillizer, stun, lethal) undefined and says do not start it `[doc project-plan "Unassigned backlog" on guns]`.

**Verdict:** **Avoid** for now. If tranquillizer turrets come later, they belong to the guns module, not to security.

### 3.9 Block-entity tracker (a reusable pattern)

`misc/BlockEntityTracker` keeps a per-dimension collection of positions for block entities that need to be found by range or by condition, so other code does not scan the world `[v misc/BlockEntityTracker.java:25-60]`. SecurityCraft uses it for sonic systems, block-change detectors, rift stabilisers, redstone interfaces and frame-viewed cameras `[v :30-35]`. This is the same idea the earlier survey recommended for feeders (per-level registry). **Adopt (pattern)**; write Nublar's own small registry (B17/B20), do not import this class.

### 3.10 Fit table: SecurityCraft vs Nublar park security

| SecurityCraft feature | Fits Nublar? | Why | Verdict |
| --- | --- | --- | --- |
| Camera plus monitor, mounted view | Yes | Film-park control room; paddock watching | Adapt, no force-load |
| Frame live feed | Partly | Strong look, high client cost | Avoid for Beta |
| Keycard readers | Yes, later | Gate and door control; gates not in Beta | Adopt pattern, backlog |
| Keypad code | Maybe | Control-room door | Not assigned |
| Laser tripwire | Weak | Escape detection is better from the dinosaur side | Not relevant |
| Alarm | Yes | Escape and fence-down siren | Adapt with vanilla sounds |
| Motion light | Weak | Decoration; scan cost | Avoid scan |
| Ownership and modules | Optional | Multiplayer parks | Adapt (UUID only) |
| Reinforced blocks | No | Conflicts with dinosaurs breaking fences | Avoid |
| Sentries | No | Shoots mobs; gun module not defined | Avoid |
| Block-entity tracker | Yes | Per-level registry pattern | Adopt pattern |

## 4. CC: Tweaked (cc-tweaked/CC-Tweaked)

**In game:** the player builds a Computer block (or a turtle or pocket computer), writes Lua programs, and attaches other blocks as peripherals: monitors, speakers, modems, and any block that exposes the right interface. A "park computer" next to a monitor wall can show fence power, feeder levels and dinosaur counts, and ring alarms by redstone or sound.

### 4.1 How another mod exposes its blocks (the peripheral API)

**Mechanism `[v]`:**
- **Platform lookup, not a CC-specific registry.** On NeoForge a mod registers a block capability `PeripheralCapability.get()` (a `BlockCapability<IPeripheral, Direction>` with id `computercraft:peripheral`) `[v projects/forge-api/.../PeripheralCapability.java:16-26]`. On Fabric it is a `BlockApiLookup<IPeripheral, Direction>` `PeripheralLookup.get()` `[v projects/fabric-api/.../PeripheralLookup.java:16-26]`. Both classes say they are for registering only, never for querying.
- The NeoForge example registers one line: `event.registerBlockEntity(PeripheralCapability.get(), BlockEntityTypes.BREWING_STAND, (b, d) -> new BrewingStandPeripheral(b))` `[v projects/forge/src/examples/.../ForgeExampleMod.java:35-39]`.
- **Methods by annotation.** A peripheral class implements `IPeripheral`, or the simpler `GenericPeripheral`, and marks public final methods with `@LuaFunction`. `mainThread = true` queues the call to the server thread; without it the call runs on the computer's own thread `[v core-api/.../LuaFunction.java:40-64]`. The example `FurnacePeripheral` is 12 lines of body and adds `getBurnTime` to every furnace `[v projects/common/src/examples/.../FurnacePeripheral.java:17-28]`.
- **Adding methods to blocks that you do not own.** `GenericSource` lets a mod add Lua methods to any block entity type or capability without wrapping it `[v core-api/.../GenericSource.java:29]`. CC registers its own inventory, fluid and energy sources this way `[v forge/.../ComputerCraft.java:107-113; fabric/.../ComputerCraft.java:140-142]`.
- **Generic peripherals follow capabilities automatically.** `GenericPeripheralProvider` runs `methods.forEachMethod(blockEntity, ...)` and then each registered `ComponentLookup` against the block `[v common/.../peripheral/generic/GenericPeripheralProvider.java:40-56]`. On NeoForge the energy lookup is registered with `ForgeComputerCraftAPI.registerGenericCapability(Capabilities.Energy.BLOCK)` and the methods read `EnergyHandler.getAmountAsLong()` and `getCapacityAsLong()` `[v forge/.../ComputerCraft.java:113; forge/.../generic/methods/EnergyMethods.java:11-22]`.
- **Fabric gap.** On Fabric only `InventoryMethods` is registered `[v fabric/.../ComputerCraft.java:140-142]`. There are no fluid or energy generic methods on Fabric. Nublar needs its own `GenericSource` for energy on Fabric if it wants parity `[inf]`.
- **No polling for presence.** The computer keeps a `BlockCapabilityCache` per side with an invalidate callback `[v forge/.../PlatformHelperImpl.java:279-310]`. When the neighbour changes, the cache invalidates and CC re-checks. Nublar must invalidate capabilities when a block entity changes its exposed interface (NeoForge `invalidateCapabilities`) `[inf]`.
- **Push events.** A peripheral can hold the attached computers (`AttachedComputerSet`) and call `computer.queueEvent(name, args...)`, which wakes the program without polling `[v core-api/.../IComputerAccess.java:139]`. Simple Radio does exactly this for received radio signals: `computers.forEach(c -> c.queueEvent("receive_signal", data, power))` `[v SimpleRadio compat/cc/SocketPeripheral.java:19-60]`.

**Lesson for Nublar (B12, B14, B15, B20):** build the machines and the fence network on the platform's standard capabilities first (Balm energy, item and fluid access). That alone gives a CC computer: `getEnergy`, `getEnergyCapacity` on NeoForge, and `list`, `size`, `getItemDetail`, `pushItems` on both loaders for feeders and machines `[inf from v]`. No Nublar code is written for these. Then add one optional compat class with a `GenericSource` or `IPeripheral` that adds park-specific reads.

**What a park computer could read (and what it needs):**

| Reading | Source | Free through standard capability? | Nublar work |
| --- | --- | --- | --- |
| Machine and generator energy | Energy capability | NeoForge yes `[v]`; Fabric no `[v]` | Fabric: small source (S) |
| Feeder item and fluid levels | Item and fluid capability | Items: both loaders `[v]`. Fluids: NeoForge only `[v]` | None on NeoForge; Fabric fluids: small source |
| Fence network: powered or off, post count, broken wires | Nublar `FencePowerService` | No | A peripheral on the switch block or a post: `isPowered()`, `getBrokenWires()`, `getLoad()` (S) |
| Dinosaur counts, species, hunger, stress band | Nublar server state | No | A "park tracker" peripheral that returns a cached summary (M) |
| Gate and door state | Nublar | No | Optional |

**Push events worth sending:** `nublar_fence_down` (a section lost power), `nublar_wire_break`, `nublar_escape`, `nublar_hungry`. Send them only on a state change `[inf]`.

### 4.2 Cost of polling and of the main-thread budget

- Computers run Lua on their own thread (config `computer_threads`, default 1) `[v common/.../config/ConfigSpec.java:155-162]`.
- A `mainThread = true` call is queued, and a global budget decides how much runs per tick: default 10 ms for all computers and 5 ms for one computer, both described as an upper bound of the average `[v core/.../mainthread/MainThreadConfig.java:16-26; ConfigSpec.java:164-177]`. Over-budget calls wait for the next tick.
- So a player script that polls 500 fence posts each tick cannot freeze the server, but it can spend its own computer's 5 ms and starve other computers `[inf]`. The Nublar defence is shape, not code: one call that returns a network summary, never one call per post. Cache a per-tick snapshot inside the peripheral and return that; the snapshot must be built on the main thread and published as an immutable object `[inf]`.
- **Idle block entities do not tick.** CC's `TickScheduler` schedules a vanilla block tick through a lock-free queue only when something changed, and parks block entities whose chunk is not loaded until the chunk loads again `[v common/.../util/TickScheduler.java:22-67, 102-112]`. Monitors use the same path: `markChanged` schedules once through an atomic flag `[v peripheral/monitor/ServerMonitor.java:64-66]`.
- Monitor network cost is bounded: the default `monitor_bandwidth` is 1 MB per tick, a full monitor is about 25 KB, so about 40 full monitors can update in one tick `[doc ConfigSpec.java:298-308]`. The queue skips monitors with no player in the chunk `[v monitor/MonitorWatcher.java:41-60]`. Client render distance for monitors defaults to 64 blocks `[v ConfigSpec.java:366-370]`.

**Lesson for Nublar (B12, B25):** this scheduler is the model for machines. A machine that has no work should not tick; a progress change should call `level.scheduleTick` once, as the earlier survey's "tick posts only on change" row also found. **Adopt (pattern)**.

### 4.3 Monitors and the control room

**In game:** a wall of monitors shows a program's output: text and coloured blocks, up to 32 by 32 monitor blocks `[v ConfigSpec.java:355-357]`. The player can touch the monitor to click.

**Fit:** a CC monitor wall is the cheapest way for a player to get a "fence status board" without Nublar writing a screen renderer. It only works for players who write Lua, so it is optional. A Nublar-made status block (for example a lit panel showing "section A: ON, section B: OFF" through blockstates) needs no mod and costs nothing `[inf]`.

### 4.4 Verdict for CC: Tweaked

- **Compat** (optional): ship one class set under a `compat/cctweaked` package, loaded only when CC is present. It registers (a) a peripheral for the fence switch block and the park tracker, (b) on Fabric a `GenericSource` for energy. Effort M. CC is `compileOnly`, never a hard dependency.
- **Adopt (pattern)**: the scheduler and "mark changed then schedule once" for every Nublar block entity.
- **Platform overlap:** none of CC's API is duplicated by NeoForge or Fabric; the platform supplies the capability and lookup objects that CC builds on.

**Compatibility duty (Nublar must do):**
1. Expose energy, items and fluids through the standard capability on every machine, feeder and generator, in the side direction CC queries `[v PlatformHelperImpl.java:299-300]`.
2. Invalidate capabilities when a block entity changes what it exposes.
3. Do not assume only the player's own machines are on the network; computers may read them from any adjacent side.
4. Never run world-reading Nublar code from a non-main-thread Lua call.

## 5. Simple Radio (CodinGlitch/SimpleRadio)

**In game:** players hold a walkie-talkie or transceiver and talk on a frequency using Simple Voice Chat; others on the same frequency hear them, with range and audio quality loss (FM and AM, static near the edge) `[doc README]`. Blocks: Transmitter (with antenna), Receiver, Speaker, Microphone, Radio (listens and plays), Radiosmithing Station `[doc README]`. Walkie-talkie reception power is 100 and FM transmission power is 500 by default `[v SimpleRadioServerConfig.java:87-125]`.

**Mechanism `[v]`:**
- It is a plugin of Simple Voice Chat (SVC); audio arrives as microphone packets and locational or entity audio channels `[v radio/RadioManager.java:488-520]`.
- Audio processing runs on one daemon thread named `RadioMicrophoneProcessThread` `[v radio/CommonRadioPlugin.java:28-37]`.
- A static registry of `Router` objects keyed by a short id; `serverTick` runs each second for housekeeping and ticks frequencies, listeners and speakers `[v RadioManager.java:78, 321-345]`. A source is queued with a time delay, which models wire travel time `[v :349-398]`.
- Optional CC: Tweaked compat is a 41-line class plus a 135-line `SocketPeripheral`, gated by `CompatCore.COMPUTER_CRAFT.isLoaded` at the call sites `[v compat/cc/CommonCCCompat.java; CompatCore.java:42, 100-107]`.

**Availability:** the newest branch is `1.21.1` (MC 1.21 to 1.21.4); there is no 26.x branch `[v]`. SVC itself has 26.1 to 26.4 branches `[v]`. Simple Radio also requires the "lexiconfig" library `[v gradle.properties]`.

**Fit for Nublar:**
- Rangers' radios and a control-room microphone suit the film, but they need a voice-chat mod on every client and server. Nublar should not depend on that.
- **Park announcements without voice:** a "PA speaker" block that plays a recorded sound event (fence breach, tour start, feeding time) to players within a set range, driven by a redstone edge or by the same park event the CC peripheral raises. That is vanilla: `level.playSound` or a packet-free server sound with attenuation. Effort S, needs sound assets (B24) `[inf]`.
- If the lead wants real-time voice, the supported route is: players install Simple Radio when it reaches 26.2; Nublar adds nothing, or at most exposes a redstone-to-frequency hook through Simple Radio's `api` module (it has a separate `api/` source set with `Frequencies`, `Listeners`, `Speakers`) `[v api/src/.../core/Frequencies.java]`.

**Lesson for Nublar:** (1) keep audio-style heavy work off the main thread, with one named daemon thread and a queue (B25); (2) the compat gate pattern: one `isLoaded` flag checked at every call site so the compat classes never load without the mod; (3) its tick model shows what to avoid: static maps keyed by block entity must be cleaned on removal `[v CompatCore.java:101]`.

**Compatibility duty:** none beyond keeping Nublar's speaker block from clashing in the tag/registry sense. If both mods have a "speaker", no conflict arises as ids are namespaced.

**Platform overlap:** vanilla sounds and `ClientboundSoundPacket` cover announcements. No loader API covers voice.

**Verdict:** **Not relevant** for Beta (not available, extra hard dependency). **Compat** later through the players' own install; **Adapt** the announcement idea with vanilla sounds.

## 6. Alternate Current (SpaceWalkerRS/alternate-current)

**In game:** redstone dust works as in vanilla but causes far less server lag; big redstone builds that cost 20 times more update time in vanilla run lighter `[doc README]`. The update order of dust networks becomes predictable instead of depending on position.

### 6.1 Mechanism `[v unless noted]`

- It cancels vanilla's recursive `RedstoneWireBlock.updatePowerStrength`, `neighborChanged` and the add/remove hooks, and calls its own `WireHandler` per level `[v mixin/RedstoneWireBlockMixin.java:16-77; mixin/ServerLevelMixin.java:26-39]`.
- It builds the whole connected network once, finds wires that receive power from outside, and spreads power from there. Each wire reads outside power at most once and sets its state once `[v wire/WireHandler.java header comment]`.
- Vanilla dust sends 42 block updates and up to 22 shape updates per power change; 6 of the block updates go to the wire itself and 16 shape updates go to blocks diagonally above and below that need no news when only the power level changes. AC emits 24 block updates and 6 shape updates per change, and skips updates to wires inside the same network `[doc README; v WireHandler.java:1035-1085]`.
- Wire state is written through a trimmed `setBlockState` that skips lighting, heightmap and block-entity work but still marks the chunk for client sync and saving `[v wire/LevelHelper.java:14-56]`.
- **What it asks of other blocks.** To learn the outside power it calls only these four methods on neighbours: `isConductor`, `isSignalSource`, `getSignal`, and `getDirectSignal` `[v WireHandler.java:699-745]`. It then sends `neighborChanged` to the neighbours of the network once the final level is known `[v :1080-1085]`.
- It reads the vanilla 26.x `Orientation` data on `neighborChanged` and patches `ExperimentalRedstoneUtils.initialOrientation` so a missing front gets a fixed `WEST` instead of a random pick `[v mixin/ExperimentalRedstoneUtilsMixin.java:15-32]`. This is needed for deterministic order.
- Admin command `/alternatecurrent on|off`, update-order setting, profiler `[v command/AlternateCurrentCommand.java:42-89]`.

### 6.2 Redstone duties for Nublar blocks

Nublar has few redstone blocks: the fence switch ("a fence section turns off on signal", B15), comparator output on feeders and machines (B12, B20), and possible alarms and keycard readers. Rules, each `[inf]` unless marked:

| Rule | Why | Nublar block |
| --- | --- | --- |
| Make `getSignal` and `getDirectSignal` plain blockstate reads. No `getBlockEntity`, no cast, no loop | AC calls them once per neighbour per network update. SecurityCraft's camera does `((IModuleInventory) level.getBlockEntity(pos)).isModuleEnabled(...)` inside `getSignal` `[v SecurityCameraBlock.java:204-210]` | Switch, alarm, reader |
| Return `isSignalSource` true only for blocks that can emit | AC and vanilla skip other neighbours | Switch only |
| Change the powered blockstate only when the value changes; compare before `setBlock` | A no-op `setBlock` with update flags still sends neighbour and shape updates in vanilla. SecurityCraft's motion light does this check `[v MotionActivatedLightBlockEntity.java:42-43]` | All |
| Use flag `Block.UPDATE_CLIENTS` or skip neighbour updates when only a client visual changes | Avoids neighbour updates for cosmetic changes | Fence post glow, spark state |
| Do not override `neighborChanged` unless the block reacts. Delete empty overrides | Nublar's `ElectricFenceBlock.neighborChanged` reads a neighbour block state and discards it `[v common/.../block/ElectricFenceBlock.java:88-91]`. On 26.x the signature becomes `neighborChanged(state, level, pos, block, Orientation, boolean)` (SecurityCraft's `LaserBlock` shows it `[v blocks/LaserBlock.java:160]`) so it must be touched in the port anyway | Fence wire block (hundreds) |
| Fence sections react to a switch through the fence network, not through redstone per post | One redstone read at the switch, then `FencePowerService` flips the whole network. Never make each post read redstone | B15 |
| Comparator output: implement `hasAnalogOutputSignal` and `getAnalogOutputSignal` from cached counts; call `updateNeighbourForOutputSignal` only when the analog value changes | A comparator update per item move costs neighbour updates. Vanilla furnaces and hoppers do the same checks | Feeders, machines |
| Never schedule redstone-related ticks of delay 0 in a loop; use a delay of 2 or more ticks for pulses | Same-tick loops create update storms | Reader pulse, alarm |
| Pulse with `scheduleTick`, as SecurityCraft's keycard reader does `[v KeycardReaderBlockEntity.java:223-240]` | No block entity tick | Reader |
| Keep no state in a static map that depends on update order | AC changes the order | All |
| Put a GameTest that runs the switch with AC and with vanilla and compares final states | AC promises parity of final states, not intermediate order `[doc]` | B15 acceptance |

**Estimate:** with 10 posts a switch change costs one network flip and one redstone update. With 500 posts and a naive per-post redstone read it would be 500 reads per change plus neighbour updates to each post `[inf]`.

### 6.3 Compatibility duty and platform overlap

- AC replaces dust logic only. It does not touch other blocks' `getSignal` code, so Nublar blocks work as long as they follow the vanilla redstone contract. No Nublar mixin should target `RedstoneWireBlock`.
- Vanilla 26.x already contains the `Orientation` and `ExperimentalRedstoneUtils` classes, and AC patches them `[v AC mixin target]`; that they sit behind a feature flag is from memory `[inf]`. Nublar must write `neighborChanged` for the `Orientation` signature and not depend on the order in which neighbours are told `[inf]`.
- Fabric and NeoForge: AC has a `26.2` branch and a `neoforge-26.2` branch `[v gh branches]`. It runs on both.

**Verdict:** **Adopt (pattern)** as a redstone checklist in B15 and B12 acceptance. **Compat**: test with AC on in the fence GameTest.

## 7. Performance summary (estimates, with the earlier survey's labels)

| Item | Cost | Basis | Nublar choice |
| --- | --- | --- | --- |
| Mounted camera, chunk tickets | `(2r+1)^2` per viewer: 289 at r=8, 625 at r=12, 1,089 at r=16 | `[v formula]` | No force-load: 0 tickets |
| Mounted camera, client | One normal world render plus extra chunk meshes | `[inf]` | Radius clamp |
| Frame feed, client | One extra `renderLevel` per feed per interval. Default 30 fps cap, 512 by 512 | `[v]` for settings, `[inf]` for total | Skip in Beta |
| Frame feed, server | Up to 1,089 forced chunks per feed at default radius 16 | `[v]` default, `[inf]` product | Skip in Beta |
| Motion light scan | One entity query per light per 5 ticks | `[v]` | Avoid |
| Alarm | One predicate scan of all players per cycle, one packet per player in range | `[v]` | Vanilla sound |
| Laser field | `entityInside` per overlapping entity per tick; up to 5 block reads each | `[v]` | Debounce if used |
| CC main-thread calls | Bounded to 10 ms global and 5 ms per computer per tick | `[v]` | Summary calls, cached snapshot |
| CC monitor updates | Up to 1 MB per tick, about 40 full monitors | `[doc]` | n/a |
| Redstone dust change (vanilla) | 42 block updates, up to 22 shape updates | `[doc]` | n/a |
| Redstone dust change (AC) | 24 block updates, 6 shape updates | `[doc]` | n/a |
| Nublar switch (planned) | 1 redstone read, 1 network flip | `[inf]` | Required design |

None of these figures were measured in game. The two largest unknowns are the real cost of a chunk ticket at ticket level "entity ticking" and the frame-time cost of a second 512 by 512 render; both need a profiler run before any decision relies on them.

## 8. Closing table

| Technique | Source | Nublar module | Verdict | Effort | Expected effect |
| --- | --- | --- | --- | --- | --- |
| Mounted camera view (camera entity, limited arc, zoom, exit key) | SecurityCraft `CameraController`, `SecurityCameraBlock.mountCamera` `[v]` | Backlog "security cameras" | Adapt | M | Control-room feel with one world render |
| View only loaded chunks; show "no signal" otherwise | My design `[inf]` | Security cameras | Adopt (own) | S | Removes up to 625 forced chunks per viewer at r=12 |
| Clamp camera radius by config | SecurityCraft clamp `[v]` | Security cameras | Adopt | S | Bounds chunk and mesh cost |
| Frame block live feed | SecurityCraft `FrameFeedHandler` `[v]` | Backlog | Avoid (Beta) | L | Avoids a second render per feed |
| Snapshot feed (one frame every 2 to 5 s) | `[inf]` | Backlog | Defer | L | Cheap CCTV wall if wanted |
| Keycard as data component plus `scheduleTick` pulse | SecurityCraft `KeycardReaderBlockEntity` `[v]` | Gates, doors backlog | Adopt pattern | S | No ticking block entity |
| Vanilla-sound alarm on rising edge | Replaces `AlarmBlockEntity` `[v]` | Park signs and alarms | Adapt | S | No scan, no custom packet |
| Entity-inside tripwire with per-entity debounce | SecurityCraft `LaserFieldBlock` `[v]` | Optional | Adapt | S | Event-driven escape signal |
| Per-level registry of watched block entities | SecurityCraft `BlockEntityTracker` `[v]` | B17, B20 | Adopt pattern | M | No world scans |
| Owner UUID on control blocks | SecurityCraft `Owner` `[v]` | Multiplayer parks | Adapt | S | Optional protection, small save cost |
| Reinforced-block layer | SecurityCraft `[v]` | none | Avoid | L | Avoids tone conflict and block-entity bloat |
| Sentries | SecurityCraft `[v]` | Guns backlog | Avoid | L | n/a |
| Standard capabilities on machines, feeders, generators | CC generic peripherals `[v]` | B12, B14, B20 | Adopt (platform) | S | Free CC read of power and items |
| Park peripheral (fence state, dinosaur counts) | CC `GenericSource`, `IPeripheral` `[v]` | B15, B17 | Compat | M | Optional computer control |
| Fabric energy/fluid generic source | CC Fabric gap `[v]` | B14, B20 | Compat | S | Parity with NeoForge |
| Push events on state change only | CC `queueEvent` `[v]` | B15 | Compat | S | No polling |
| Summary calls and cached snapshot for Lua | CC main-thread budget `[v]` | Compat | Adopt | S | Bounds per-computer cost |
| On-demand tick scheduling for idle block entities | CC `TickScheduler` `[v]` | B12, B15, B25 | Adopt pattern | M | Idle machines and posts cost 0 ticks |
| Optional-compat gate (one `isLoaded` flag, separate package) | Simple Radio `CompatCore` `[v]` | All compat | Adopt pattern | S | No class-load crash |
| PA speaker block with vanilla sounds | Replaces Simple Radio idea `[inf]` | Park signs | Adapt | S | Announcements with no voice mod |
| Voice radio | Simple Radio `[v]` | none | Not relevant now | n/a | Not on 26.x |
| Redstone checklist (cheap `getSignal`, change-only writes, analog output on change) | Alternate Current `[v]` | B12, B15, B20 | Adopt pattern | S | Avoids update storms |
| Delete empty `ElectricFenceBlock.neighborChanged` and port the signature | `[v local]` | P2, B15 | Adopt | S | Removes work on hundreds of blocks |
| Run fence GameTest with AC on | Alternate Current `[v]` | B15 | Compat | S | Catches order assumptions |

## 9. Open questions (real design gaps)

1. **Camera body or camera entity?** A mounted camera can use the SecurityCraft route (a separate entity plus about 15 mixins and a ticket controller) or the vanilla spectator route (the player body moves to the camera and returns). The second needs far less code but puts a body at the camera position. Which does the lead want? Related: should a player be able to view a camera while the paddock chunks are unloaded, or must the view say "no signal"?
2. **Do cameras need power from B14, and what happens when a fence section is off?** The plan has power draw for machines and fences but no entry for cameras or monitors.
3. **Should a control-room status board exist in Beta?** Options: nothing, a Nublar block with blockstate lamps, a block with synced text (menu data), or only the CC compat. The park-tech section of the design memory does not name it.
4. **Is multiplayer ownership wanted for control blocks?** If a server has many players, can another player turn off your fence switch or look through your cameras?
5. **Escape detection source.** Should the escape alarm come from the dinosaur's own enclosure test (B17) or from block tripwires? The first is cheaper and more accurate; the second is more visible to a builder.
6. **Does the lead want any voice radio?** If yes, the plan should say it is a player-installed optional mod, and name the day Simple Radio gets a 26.x port as a trigger, since no port exists today.
7. **Sound assets for alarms and announcements** are not listed in the asset inventory in the plan (B24). Who supplies them?
