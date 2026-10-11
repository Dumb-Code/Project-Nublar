# Vehicles, transport and physics mods: survey for Project Nublar (B22)

Researcher note for the optimization and tech mod survey. Topic: Automobility, Man of Many Planes (and the Immersive Aircraft base it needs), Minecraft Transit Railway (MTR), Valkyrien Skies 2 (VS2) and Clockwork. Date of reading: 2026-10-10.

Labels: `[v]` verified in source (path and line range at the commit shown), `[doc]` README, wiki, changelog or store page, `[inf]` my inference. A line range with `~` is approximate (plus or minus 5 lines) because I read that part through a search hit. Every performance number that is not `[v]` is an estimate.

Earlier work that this note extends and does not repeat: `docs/project-plan.md` B22 (lines 799 to 825), the report "Reference mod survey for Beta" rows B22.1 to B22.10, section 6 and 7 (track follower, driverless tours), and specs S19 (placement check) and S20 (track follower). Decision D07 stands: no fuel.

## 1. Repos read

| Repo | Branch and commit | Date | Licence (plain fact) | Loaders and MC | Size | Status |
| --- | --- | --- | --- | --- | --- | --- |
| FoundationGames/Automobility | `1.21-rewrite` `e43f78b` | 2025-04-24 | MIT-style text in `LICENSE` | Fabric and NeoForge, MC 1.21.1 (`gradle.properties`: `minecraft_version=1.21.1`) | 182 Java files, 12 918 lines in `common/` | Beta. README says only 1.21.1 is maintained. Last push 2025-04-24. No 26.x branch. |
| Luke100000/Man-of-Many-Planes | `1.21.1` `c1c5189` | 2026-10-10 | README: code CC0, assets CC BY-NC-SA 4.0. `fabric.mod.json` says GPL 3.0 | Fabric and NeoForge, MC 1.21.1 | 10 Java files, 391 lines | Active (push 2026-10-10). It is an addon, not a framework. |
| Luke100000/ImmersiveAircraft (base of the addon, read for the vehicle design) | `1.21.1` `02010e1` | 2026-10-07 | GPL 3 | Fabric and NeoForge, MC 1.21.1 | 13 370 Java lines | Active |
| ValkyrienSkies/Valkyrien-Skies-2 | `1.20.1/main` `a79995c` (mod_version 2.4.12) | 2026-07-22 | LGPL 3 | Fabric, Forge, Quilt on 1.20.1. A `1.21.1/main` branch exists. No 26.x branch (`gh api .../branches`). | 637 Java and Kotlin files. Physics core is a separate Maven artifact (`vs_core_version=1.1.0+2d6eb6ff55`, `krunch_version=1.0.0+6eb0d35353`, `gradle.properties:59-62`) | Active. Release 2.4.11 on 2026-04-10. |
| ValkyrienSkies/Clockwork | `1.20.1/main` `067c9b3` | 2026-08-12 | Apache 2.0 for code, ARR for assets (README) | Forge and Fabric on 1.20.1. No branch for 1.21 or 26.x. | Not measured | Open Beta, active |
| Minecraft-Transit-Railway/Minecraft-Transit-Railway | `master` `739dba4` (v4.0.5) | 2026-06-13 | MIT | Fabric and Forge, MC 1.16.5 to 1.20.4 (`gradle.properties: minecraftVersion=1.20.4`; wiki lists 1.16.5, 1.17.1, 1.18.2, 1.19.2, 1.19.4, 1.20.1, 1.20.4) | 370 Java files, about 37 000 lines. The simulation core is NOT in the repo. It is the artifact `org.mtr:Transport-Simulation-Core:0.0.1` (`build.gradle:22`). | Active (push 2026-07-16). No 1.21 or 26.x port in this repo. |

Platform sources also read: NeoForge `26.2.x` `3ce4390` (2026-10-10) patches, and Fabric API `6a8ed6b` (2026-09-18). Wiki pages: MTR wiki (`mtr:4.0.x`, `players_builders`, `server_admin`, `mtr:dashboards`), VS wiki (`Main_Page`, `Valkyrien_Skies`).

Version fact for the port: none of the five mods has a 26.x build. They are design references only. Nothing here can be a runtime dependency on 26.2 today `[v]`.

## 2. Summary of findings

1. **Correction to the earlier report.** The earlier report says no surveyed mod runs a vehicle with no driver. Automobility does: its "Autopilot" front attachment drives the car with no player aboard, steered by Autopilot Sign blocks (section 3.4). MTR trains are driverless by design. B22 driverless tours can copy two proven ideas: "stop for what is in the way" and "markers the vehicle reads, not markers that scan".
2. **Driver-client physics is the common pattern.** Automobility and Immersive Aircraft both let the driver's client move the vehicle and send state to the server. Both trust the client. Automobility checks only that the sender is the driver (`CommonPackets.java:70-79`). Immersive Aircraft even trusts a client-reported collision damage number (`VehicleEntity.java:779-797`). Nublar must add the speed clamp that B22.1 already plans.
3. **Both vehicle mods auto-board nearby mobs.** Automobility (`AutomobileEntity.java:~745-758`) and Immersive Aircraft (`VehicleEntity.java:~456-466`) call `startRiding` for mobs that touch the vehicle. A dinosaur next to a parked Jeep could climb in. Nublar must block this (section 3.1, duty 3).
4. **MTR keeps hundreds of vehicles cheap by not using entities.** Vehicles are plain data simulated in a separate core, on a background thread. The server sends only nearby vehicles to each player. The client moves them between updates. No chunk tickets are used `[v]`. This is the strongest cost lesson, but its architecture is too large for Nublar. The cheap parts to copy are the client request radius, client-side dead reckoning and per-car occlusion culling (section 5).
5. **Vanilla 26.2 already has the rail follower.** NeoForge 26.2 patches `BaseRailBlock`, `NewMinecartBehavior`, `OldMinecartBehavior` and `AbstractMinecart` around one hook, `getRailDirection` (`IBaseRailBlockExtension.java:50`). `AbstractMinecart.exits(RailShape)` gives the two exit vectors of a rail shape `[v]` (used in `NewMinecartBehavior.java.patch`). S20 can reuse it and skip writing a shape table. Fabric API 26.2 has no rail hook, so `TrackBlock` should extend `BaseRailBlock` and use the vanilla `RailShape` property, as the earlier report planned.
6. **VS2 and Clockwork are out of scope.** No 26.x port, and they solve a problem Nublar does not have (player-built rigid-body ships). Stay compatible by following four cheap rules (section 6).
7. **Mechanics that fit the tour:** stop-until-clear with an impatient horn, passive stop markers, a dwell time plus a spoken or written announcement at each stop, a depot-style "tour station" that sends a vehicle every N minutes up to a limit, seats (not standing passengers), and one horn. **Mechanics that do not fit:** modular frame, engine and wheel assembly, dash panel boost, drifting and turbo, off-road tiles, banner posts, weapons, upgrade slots and fuel.

## 3. Automobility

### 3.1 Vehicle entity, driving feel, networking

**In game.** The player crafts frame, engine and wheels at an Auto Mechanic Table (the GUI crafts each part), puts the parts on an Automobile Assembler block until the car is complete, and removes it with a crowbar `[doc]` (README "Getting Started"). Driving keys: W accelerate, S brake and reverse, A and D steer, Space drift `[doc]` (README "Driving"). The car has real weight, grip, drifting, a turbo after a drift, and a visible suspension bounce. It climbs steps by wheel size, sticks to slopes, launches off a ramp, slows on off-road tiles and gains a boost on a dash panel.

**Mechanism.**

- Parts are data. `AutomobileEngine` is a record of `torque`, `speed`, `sound`, `model` loaded from a dynamic registry and synced with a stream codec (`automobile/AutomobileEngine.java:~15-50`). Frame has `weight`, `width`, `hitboxes`, `horn`, `model` (`AutomobileFrame.java:24-27`). There is no fuel field `[v]`. This matches D07.
- One entity type with a small tracking setup: `updateRate` 3 and `updateRange` 10, passed to `updateInterval(3)` and `clientTrackingRange(10)` (`AutomobilityEntities.java:21-24`, `fabric/.../FabricPlatform.java:124-125`). A second type, `HitboxEntity`, exists for the extra collision boxes of the frame, with the same 3 and 10 (`AutomobilityEntities.java:25-28`). `AutomobileEntity.verifyHitboxesFor` keeps one entity per hitbox box (`AutomobileEntity.java:~583-590`). So a big vehicle costs several entities `[v]`.
- Only three float values and three holders are in `SynchedEntityData` (`AutomobileEntity.java:105-111`): attachment yaw and two attachment animation floats, plus the three part holders. Movement state uses custom packets instead.
- **Driver client runs the physics.** `tick()` calls `move()` only when `isControlledByLocalInstance()` (`AutomobileEntity.java:722-724`). Other clients call `positionTrackingTick()`, which lerps toward the last tracked position over `updateInterval + 1` ticks (`:793-811`, `lerpTo` at `:1287-1293`).
- **Client to server, every 4 ticks.** `CLIENT_SYNC_INTERVAL = 4` (`:134`), sent from `tick()` (`:~778-786`) through `ClientPackets.sendServerboundAutomobileSyncPacket` (`ClientPackets.java:29-34`). The payload is the entity id plus `writeSyncStateData`: `boostTimer` int, `steering`, `wheelAngle` floats, `turboCharge` int, `engineSpeed`, `boostSpeed` floats, the input record, two booleans (`AutomobileEntity.java:216-228`). That is about 26 bytes of state plus the input plus a 4-byte id `[inf]` (field count verified, byte total is my sum).
- **Server accepts it with one check.** `isDriving(player)` then `readSyncStateData` and `markDirty` (`CommonPackets.java:70-79`). No speed clamp, no distance check `[v]`.
- **Server to other players.** `syncData()` loops over players whose chunk tracking view contains the car and skips the driver (`forPlayersTrackingMe(true, ...)`, `AutomobileEntity.java:600-611`, `813-815`). `markDirty()` is called whenever the wheel angle changes (`:~684`) and whenever `hSpeed > 0` in `movementTick` (`:~960`). So a moving car can send a state packet to each tracker up to once per tick `[inf]`. I did not measure it. This is the anti-pattern the earlier report B22.8 already names (state sync per tick). Nublar sends animation-only values (wheel angle, steering) from the client side instead.
- A client that gets the entity asks the server for the parts (`recreateFromPacket` then `requestSyncAutomobileComponentsPacket`, `AutomobileEntity.java:342-346`). The parts are not in the spawn packet.
- **Auto-board.** On the server, when the car has free seats it scans a box 0.2 blocks wider than itself every tick and calls `entity.startRiding(this)` for any `Mob` that is narrower than the car and not a `WaterAnimal` (`AutomobileEntity.java:~745-758`). Immersive Aircraft does the same with an `AutoEnterRules` list (`VehicleEntity.java:~456-466`) `[v]`.
- **Culling box.** `getBoundingBoxForCulling()` returns the union of the entity box and all hitbox entities, refreshed every client tick (`:627-637`, `:787`). This is the right habit for EntityCulling.

**Lesson for Nublar (B22).**

- Keep B22.1 and B22.2 as planned. Add a server check that Automobility lacks: clamp the received speed and the distance moved per packet to the TOML top speed. Vanilla also has `ServerboundMoveVehiclePacket` for boats; its server handler has a "moved too quickly" check `[inf, recalled from 1.20.1 source, not read in 26.2]`. Prefer the vanilla packet for position if the Jeep is a normal controlled `Entity`. Send only extra custom values (horn, light) in Nublar's own payload.
- Use `clientTrackingRange` and `updateInterval` on the `EntityType` builder, as Automobility does (3 ticks, 10 chunks). Vanilla builders have these methods on 1.21.1 `[v]`; confirm the names on 26.2 at the port.
- Use one entity per vehicle. Do not copy the hitbox-entity pattern. It multiplies entity count and tracker work for every vehicle, which fights B26 and B27 (movement and hitboxes). One bounding box with a union culling box is enough for a Jeep.
- Override `getBoundingBoxForCulling()` so that the whole model, including a roll bar or an open door, stays inside the box.

**Compatibility duty.**

1. EntityCulling and Sodium-style frustum culling use the culling box. A GeckoLib model larger than the hitbox pops out at the screen edge. Override the culling box.
2. Never let a dinosaur board a vehicle. Override `canAddPassenger` or the auto-board rule for the `projectnublar:dinosaur` tag, and do not copy the Mob auto-board loop.
3. Add a tag `projectnublar:vehicle_may_autoboard` (empty by default) so players can opt other mobs in. Nothing in the design asks for auto-boarding.

**Platform overlap.** Vanilla has entity tracking (`updateInterval`, `clientTrackingRange`), `ServerboundMoveVehiclePacket`, passenger attachments, `startRiding`, `Mob` AI. Fabric API and NeoForge payload registration replace Automobility's `Platform` wrapper (`Platform.java:77`, `CommonPackets.registerReceiver`). Nothing in Automobility needs to be copied as code.

**Verdict:** Adopt (driver-client pattern, tracking numbers, culling-box habit). Avoid (hitbox entities, per-tick dirty sync, no server clamp, Mob auto-board).

### 3.2 Modular building (frame, engine, wheels) and attachments

**In game.** The player picks a frame, an engine and wheels, so a car's top speed, acceleration, grip and handling come from the parts. Optional front and rear attachments add a chest, a banner post, a horn-like extra or the autopilot.

**Mechanism.** Stats come from the part records and a combined `AutomobileStats` (`AutomobileEntity.calculateAcceleration` and friends, `:~1296-1310`). Parts craft in `AutoMechanicTableScreen` (368 lines) and assemble in `AutomobileAssemblerBlockEntity` (360 lines). Attachment types are data (`RearAttachmentType.java`, 105 lines). Datapacks and resource packs add new parts `[doc]` (README "Supports Data Packs").

**Lesson for Nublar.** The Beta set is two named vehicles (Jeep Wrangler, 1992 Ford Explorer). A part-building system is a new subsystem (size L) that the plan does not need. Two small ideas carry over:

- Put handling numbers in one record loaded from data, not in Java constants. The earlier report B22.3 already puts them in TOML. A per-vehicle JSON in the datapack (`top_speed`, `acceleration_ticks`, `turn_rate`, `step_height`) would let the Jeep and the Explorer share one entity class. This costs about 100 lines. The design decision says global balance numbers go to TOML and per-species data to JSON. Vehicle stats are per-vehicle, so JSON fits, but the developers choose (the decision says "whatever is simpler").
- A single optional attachment slot (spotlight, spare tyre) is cheap but not requested.

**Compatibility duty.** None.

**Platform overlap.** Datapack registries and codecs are covered by the platform (dynamic registries, `StreamCodec`). No library needed.

**Verdict:** Not relevant (modular building). Adapt (one data record for handling).

### 3.3 Ramps, slopes, step-up, collision at speed

**In game.** The car rolls up half-slab slopes, tilts to follow them, sticks to a downhill slope and launches off the top of a ramp. Walls stop it. Small ledges are climbed by large wheels.

**Mechanism.**

- Step height is wheel based: `maxUpStep()` returns a value taken from the wheels (`AutomobileEntity.java:597-599`).
- The mod replaces vanilla collision for the car. `collide()` sweeps the box, then tries "move horizontally, then up by step height, then down" if blocked (`:1808-1840`). Entity colliders are collected from nearby entities (`:~1836`).
- A `Displacement` helper scans the ground under the car at 20 samples per block (`SCAN_STEPS_PER_BLOCK = 20`, `:1873-1975`) to compute pitch and roll with a quaternion that turns at most about 7 degrees per tick (`:1892-1899`). This runs on the client and on the server `[inf]`.
- Slope blocks implement `SpecialAutomobileColliderBlock` and return a `CollisionArea` (a ramp shape) to that scan (`block/SlopeBlock.java:~42-60`). They carry the tag `automobility:sticky_slopes` (`Automobility.java:49`). The car reads the tag one block below it (`AutomobileEntity.java:925-931`) and pushes the car down (`:952-960`). On leaving a slope with upward speed it keeps the y speed for a launch (`:1103-1118`).
- Off-road tiles cap speed by layer count (`:937-945`).
- Cost: one Y scan per car per tick, in a loop over `colliders` (a hash set built from nearby blocks) times 20 samples per block of scan distance `[v]`. For one to five cars this is small. For a tour with many vehicles it is not free `[inf]`.

**Lesson for Nublar.** The Jeep should drive over terrain without any special block. Vanilla gives step height (an attribute on 1.20.5 and later, default 0.6 `[inf, recalled]`; confirm on 26.2). Set the Jeep to about 1.0 to 1.25 so it climbs one-block ledges and vanilla 1-block terrain steps without a jump key. Do not copy the ground-scan tilt. A cheap alternative is to tilt the model on the client from the height of the front and rear wheels (two block height reads per tick), render only. That is size S and has no server cost `[inf]`.

Do not add custom ramp blocks for the Beta. The vanilla slab and stair shapes already work with vanilla collision. The earlier report's `TrackBlock` handles the track slopes through `RailShape`.

**Compatibility duty.** Collision replacement breaks mods that read `Entity.collide`. Keep vanilla `move()` for both vehicles.

**Platform overlap.** Vanilla `Entity.move` and step-height attribute cover it.

**Verdict:** Avoid (custom collision and ground scan). Adapt (step height value, client-only tilt).

### 3.4 Autopilot and signs: driverless travel (new for B22)

**In game.** The player crafts an Autopilot attachment and fits it to the front of a car. The car then drives itself along roads made of Autopilot Sign blocks. The sign shows an arrow. A powered sign (redstone) turns into a stop sign. The car stops for entities, honks when stuck and gets more impatient the longer it waits.

**Mechanism.**

- A sign block entity ticks on the server. Every 4 ticks (`BROADCAST_INTERVAL = 4`) it builds a box 32 by 4 by 32 blocks (`new AABB(-16, -2, -16, 16, 2, 16)`), moves it ahead of the sign, and calls `notifyHeadingCommand` on every autopilot car in the box (`block/entity/AutopilotSignBlockEntity.java:~14-37`).
- `Heading` is a record of `origin`, `dir`, `planeOrigin`, `limitPlane`, `stop` (`AutopilotSignBlock.java:215`). `stop` is the redstone `POWERED` state (`:184`). The car keeps the nearest heading and drops it after `MAX_HEADING_COMMAND_TIME = 6000` ticks (5 minutes) or when it is out of range (`AutopilotFrontAttachment.java:~19`, `:~190-235`).
- `provideAlternativeInputs` (`AutopilotFrontAttachment.java:~38-138`) fills the same `Input` record as a driver would. It aims steering at a point on the path line, uses a 4-block-long look-ahead box, and calls `getEntitiesOfClass` for other vehicles' hitboxes and for living entities in that box every tick. If an entity of a type in the `stopsFor` list (saved in NBT as ids) is ahead, or a player is holding a sign item toward the car, the car brakes. Other living entities make it honk.
- Driverless cars despawn rules still apply (`despawnTime`, `AutomobileEntity.java:~758-775`).

**Lesson for Nublar (B22.7, driverless tours).**

- **"Stops for X" as a tag list.** Nublar's Explorer should stop for dinosaurs and players ahead on the track. A tag `projectnublar:tour_vehicle_stops_for` (default: all dinosaurs, players, `minecraft:villager`) gives the lead a data-driven rule, like Automobility's saved `stopsFor`. The earlier design used a "blocked check every 10 ticks" `[inf]`. Keep that cadence. Automobility checks every tick, which is an avoidable cost.
- **A blocked vehicle sounds its horn, a slow growing "impatience".** This is cheap (a counter and a random roll) and gives the tour a clear in-game signal when a Triceratops stands on the track. Adapt it as a horn after a delay (for example 3 seconds) and stop repeating after a limit.
- **Markers are read by the vehicle, not scanned by the marker.** Automobility's sign block entity scans a 32 by 4 by 32 box every 4 ticks. With many signs this is a standing cost. The earlier design has the Explorer read the block under it when its block position changes (AC mag-lev follower pattern, S3). Keep that. It costs zero when no vehicle is near.
- **A time limit on a command** (5 minutes) prevents a car from following a stale marker forever. For a track loop this is not needed, because the track is the route.
- A powered `TourStopBlock` as a "hold" signal (red light) fits the park: the lead can hold the tour from a control room with redstone. The earlier plan has only a timer. A redstone hold is size S (one boolean property) and matches the security and monitoring modules `[inf]`.

**Compatibility duty.** None beyond the culling and auto-board rules.

**Platform overlap.** Vanilla redstone `hasNeighborSignal` and `BlockState` properties cover the hold. Vanilla `getEntitiesOfClass` or `getEntities` with an AABB covers the blocked check. Vanilla `Entity.getType().is(TagKey)` covers the stop-for list.

**Verdict:** Adapt (stop-for tag, impatient horn, redstone hold). Avoid (sign blocks that scan).

### 3.5 Dash panels, boost, drift, off-road

**In game.** Driving onto a dash panel gives a strong boost for about 2.5 seconds (`boost(0.45f, 50)`; 50 ticks). Holding Space while turning charges a turbo that releases on release (boost levels 0.38 for 38 ticks, 0.3 for 21, 0.23 for 9, `AutomobileEntity.java:1311-1335`). Off-road tiles (dirt, grass, sand, snow) slow the car by layer.

**Mechanism.** `DashPanelBlock.onCollideWithDashPanel` reads the entity and calls `boost` for an `AutomobileEntity`, or applies a Speed 7 effect and a push for entities in the tag `dash_panel_boostables` (`DashPanelBlock.java:~108-128`). Panels connect left and right through block state (`updateShape`, `:~58-63`) and are redstone-disabled (`POWERED`).

**Lesson for Nublar.** These are racing mechanics. They clash with a Jurassic Park tour tone (a calm guided ride, an Explorer on a rail). Do not adopt boost pads, drift or turbo. Keep one reusable idea: a block that sets a speed cap for a vehicle that stands on it, by block tag. A "speed zone" tag on the track (for example a slower curve near a paddock) is simple and could use `RailShape` plus a stop-marker variant. The plan does not ask for it. Skip for Beta.

**Verdict:** Not relevant.

### 3.6 Rendering

**In game.** The car body, the engine, each wheel, flames and smoke draw as separate model parts, with wheel spin, steering angle and suspension bounce.

**Mechanism.** `AutomobileRenderer.java:30-120` pushes a pose, then gets a vertex buffer per component (`buffers.getBuffer(frameModel.renderType(frameTexture))`, the engine model, one per wheel in a loop over the wheel positions, an `eyes` render type for flame frames and an `entityTranslucent` type for smoke). This is immediate-mode `ModelPart` drawing, not baked. Models are cached by id in `AutomobileModels` (289 lines).

**Lesson for Nublar.** GeckoLib 5 draws the vehicle from one model with bones and one render type, which is lighter and works with batching mods. Animation values (wheel spin, steering) stay client side (B22.8). Avoid per-frame `getBuffer` calls with several render types per vehicle: each is a separate draw state and breaks batching in ImmediatelyFast and similar `[inf]`. Use one texture with an emissive layer for the headlights, if the Blockbench export allows it.

**Compatibility duty.** Keep the render type count low, and keep translucent smoke particles as vanilla particles, not model parts.

**Verdict:** Avoid the multi-buffer pattern. Adopt "wheel angle from distance, client side" (`wheelAngle += 300 * (hSpeed / wheelCircumference)`, `AutomobileEntity.java:~965`).

## 4. Man of Many Planes and Immersive Aircraft

**In game.** Man of Many Planes adds two plane items (Scarlet Biplane, Economy Plane) that place a plane. The player sits in it, and flies it with the Immersive Aircraft controls. Planes can be dyed, show wing trails and engine smoke. Everything else (engine, boiler fuel slots, inventory, upgrades, boost, weapons) comes from Immersive Aircraft `[v]`.

**Mechanism.**

- The addon is 391 lines. Each plane is an entity class that extends `AirplaneEntity` and adds trail and smoke particles on the client (`EconomyPlaneEntity.java`), an item through `DyeableAircraftItem`, and an entity type with `.sized(2.5f, 4.0f).clientTrackingRange(12).fireImmune()` (`ManOfManyPlanes.java:~22-37`). The renderer lists named model parts from a `.bbmodel` file and animates three of them (`EconomyPlaneEntityRenderer.java:~15-35`).
- `VehicleEntity` (995 lines, Immersive Aircraft) is the base:
  - Controlling client simulates. `if (isControlledByLocalInstance()) { updateVelocity(); ... move(MoverType.SELF, getDeltaMovement()); }` (`VehicleEntity.java:440-451`).
  - Other clients interpolate: `lerpTo` stores the target and sets `interpolationSteps = 10` (`:382-389`). Then `setPos` closes 1/steps of the gap per tick (`:562-579`). The step count is fixed at 10 and ignores the real update interval `[v]`.
  - Commands (dismount, boost) go in a small `CommandMessage` with a key and a velocity vector (`network/c2s/CommandMessage.java`). The server checks `e.getRootVehicle() instanceof VehicleEntity`.
  - Collision damage is computed on the client and sent as `CollisionMessage(float)` (`VehicleEntity.java:779-797`). The server applies it. A modified client can send any number `[v]`. Do not copy.
  - Fuel (`EngineVehicle.java`): the `BOILER` slots burn items. `TARGET_FUEL = 1000`, `LOW_FUEL = 900` ticks, a synced `LOW_ON_FUEL` boolean, action-bar messages for low and empty (`:42-70`, `:159-190`, `:224-260`). Config has `fuelConsumption`, `burnFuelInCreative`, `acceptVanillaFuel`, `fuelList` (`config/Config.java:67-146`).
  - Upgrades: items in `UPGRADE` slots add stat deltas through a `VehicleUpgrade` map of `VehicleStat` to float (`InventoryVehicleEntity.java:88-100`, `item/upgrade/VehicleUpgrade.java`). A `VehicleUpgradesMessage` syncs them.
  - Inventory: slots are described in a `VehicleInventoryDescription` (170 lines) that lists slot types (boiler, upgrade, cargo, weapon, booster) and positions for the GUI.
- The README names `InventoryVehicleEntity`, `VehicleStat`, `VehicleInventoryDescription` and `JsonConfig` as addon extension points `[doc]`.

**Lesson for Nublar (B22).**

- This is the lightest working example of "vehicle as a small addon over a base". The useful parts for Nublar are already in vanilla and Nublar's own planned code: seats, driver input, a controlled entity. Do not add Immersive Aircraft as a dependency. It is aircraft only, GPL 3 and 1.21.1 only.
- **No fuel** (D07) means the Jeep needs none of the boiler, fuel, low-fuel message or fuel config code. Keep a cargo slot only if the lead wants a glovebox. Neither the plan nor the design decisions mention one.
- **Upgrades** (speed, armour) are racing or survival progression. Skip. The design has no vehicle damage stages (B22.10 is deferred).
- Use the lerp lesson: use the entity type's real `updateInterval` for the interpolation length. Automobility does this (`updateInterval() + 1`, `AutomobileEntity.java:1292`). Immersive Aircraft does not.
- Use the dismount-command lesson: a vehicle-key packet should check that the sender is a passenger (S12). Both mods do.

**Compatibility duty.** Same as 3.1: no dinosaur auto-board, culling box, one entity.

**Platform overlap.** Vanilla passengers, `startRiding`, and the Fabric and NeoForge payload APIs. Immersive Aircraft's own `NetworkHandler` and `Registration` helpers exist to avoid Architectury; Nublar already has Balm and platform payloads.

**Verdict:** Not relevant (as a dependency). Adapt (the interpolation and command-packet patterns, the "small addon" structure for a future aircraft, which is out of scope).

## 5. Minecraft Transit Railway

### 5.1 Gameplay: how a route is built and run

**In game (from the 4.0 wiki and dashboard docs `[doc]`).**

1. The player places rail nodes and connects them with a rail connector. A preview shows length and curve radius before the connection is made.
2. The player places platform blocks along the track. In the dashboard the player draws a station area around the platforms. Overlapping station areas become interchange stations.
3. In the dashboard the player creates a route: an ordered list of platforms, with a name and a colour. Routes of the same colour group on signs.
4. Each platform has a number and a **dwell time** (how long a train waits). The wiki does not state a default dwell time.
5. A depot area is drawn over sidings. Each siding has defaults: siding number 1, maximum vehicles 1, increase speed when delayed 25 %, reduce dwell time when delayed 100 %, wait at platform when early true, maximum speed 300 km/h `[doc]` (`mtr:dashboards`). Several sidings with the same number are picked at random.
6. Departures are set per Minecraft hour with sliders, or in real time as `HH:MM:SS` with a repeat form `+x*HH:MM:SS` `[doc]`.
7. The player clicks "Refresh Path" after any change to track or instructions. Without it trains do not run `[doc]` (tutorial summary, `/mtr generate` for migrations in `server_admin`).
8. Trains run with no driver unless a player takes the driver seat. A train that is late speeds up and shortens dwell time. A train that is early waits (per siding switch) `[doc]` (`players_builders`).
9. Passengers board when they walk into the door area while the door is open. They stand and walk inside the car. A HUD action-bar line cycles "this station", "next station" and "last station" every second (`VehicleExtension.java:~85-130`). A **train announcer block** shows or narrates a message when a train passes, with a cooldown (`block/BlockTrainAnnouncer.java:~93-107`, `ANNOUNCE_COOLDOWN_MILLIS`). Signals and sensors (`BlockTrainSensor*`) use redstone.

**Mechanism.**

- **Vehicles are not Minecraft entities.** A vehicle is a `VehicleExtension` (374 lines) on the client, built from a `VehicleUpdate` the server sends (`VehicleExtension.java:44-52`, `packet/PacketUpdateVehiclesLifts.java:~44-70`). The client keeps them in `MinecraftClientData.vehicles` (a set). `EntityRendering` is a dummy client entity added to hook the render pass (`InitClient.java:~410-420`).
- **The simulation is a separate core on a background thread.** `Init.java:184` constructs `new Main(path, serverPort, useThreadedSimulation, ...)`. If the config says not threaded, the server tick calls `main.manualTick()` (`Init.java:~233-241`). Messages come back through a queue and are processed once per world tick (`registerEndWorldTick` → `main.processMessagesS2C`, `Init.java:~252-258`). The wiki says: "MTR 4 no longer simulates train with the Minecraft Game, but instead runs the simulation in a background thread", with less impact on server tick rate and more CPU use `[doc]` (`server_admin`).
- **Watchdog.** If the core does not answer the time update, the mod stops the server with the log "Transport Simulation Core not responding; stopping Minecraft server!" (`Init.java:~199-213`). The time update runs every `SECONDS_PER_MC_HOUR * 10` server ticks (`Init.java:~226`).
- **Request radius.** About every 0.5 seconds, while the player moves, the client sends a `DataRequest` with its UUID, camera block position, and `renderDistance * 16` blocks, plus the ids it already has (`InitClient.java:~430-439`). So the server sends only vehicles inside the client's render distance `[inf, the filter is in the core and not in this repo]`.
- **Client dead reckoning.** Each frame the client runs `vehicle.simulate(millisElapsed)` for every known vehicle (`render/MainRenderer.java:~96`; `VehicleExtension.simulate`, `:63-66`). Between packets the vehicle moves along its rail path with its own speed. A new packet corrects it.
- **No chunk loading.** A search for `ticket`, `forceLoad` and `ChunkLoad` across `fabric/src/main/java/org/mtr` found only fare tickets, plus `Init.isChunkLoaded` used by two block-config packets (`Init.java:305-307`). So MTR does not force-load chunks. A vehicle runs in the core whether or not its chunks are loaded `[v]`. Its blocks (platforms, signs) still need a loaded chunk to render and act.
- **Per-car occlusion culling.** `RenderVehicles.java:~85-100` queues a task that calls `occlusionCullingInstance.isAABBVisible` on a box around each car and stores the result in `persistentVehicleData.rayTracing[carNumber]`. The car draws only if the result is true or the player rides it.
- **Riding.** The player is not a vanilla passenger. While riding: `setNoGravity(true)`, `setNoClip(true)`, and a vanilla "in teleportation state" flag (`Init.java:403-408`). The client moves the player to world coordinates itself, at the end of each client tick, "otherwise there will be a rubber banding animation" (`client/VehicleRidingMovement.java:377-378`). It then sends the position inside the car, the car number, and the driver keys (accelerate, brake, doors, ATO, door override) in one packet (`:~401`, `packet/PacketUpdateVehicleRidingEntities.java:~16-24`). The server only flips the three player flags (`Init.updateRidingEntity`).

**Lesson for Nublar (B22, B25).** MTR's setup solves "100 trains over a big map". Nublar needs "one to a few tours over a park". The right size of idea:

| MTR idea | Fit for a tour | Verdict |
| --- | --- | --- |
| Depot with a limit on vehicles and a departure interval | A **Tour Station** block: sends an Explorer every N minutes if fewer than `vehicles.max_tours` are running and a free vehicle exists. Plan today: the player lays track and presses a key. This automates "driverless tours in a loop" | Adapt (size M) |
| Platform dwell time | Already `vehicles.stop_seconds`. Per-stop override on the stop block (one int) | Adapt (size S) |
| Late/early speed-up | No. A loop with 1 to 3 vehicles has no timetable | Avoid |
| Announcer block | A **stop announcement**: when the Explorer reaches a stop marker, the passengers hear a sound and read an action-bar line, configured on the marker (translation key or text). It makes the stop feel like a tour ("On your left, the Triceratops paddock"). It uses vanilla `Component` and `playSound`. | Adapt (size S to M) |
| Next/last station HUD line | Shows "Stop 2 of 4: Paddock" on the action bar | Adapt (size S) |
| Redstone sensors and signals | The redstone hold on the stop marker (3.4) | Adapt |
| Vehicles as data, off-thread simulation | Too heavy. Needs a custom core, a data format, client replay and custom riding. Loses vanilla riding, vanilla collision with dinosaurs and fences, and every vanilla vehicle mod interaction. The park has one to a few vehicles, so entity cost is small | Avoid |
| Client request radius and client dead reckoning | Vanilla already sends entity movement to players inside `clientTrackingRange`. The client lerps. | Not needed (vanilla covers it) |
| Per-car occlusion culling | EntityCulling already culls entities. Do nothing | Not needed |
| Watchdog on a background thread | No thread, no watchdog | Not relevant |
| Standing passengers walking inside the car, noclip | Needs custom riding and an interior collision model. Seats are simpler (vanilla passengers, B22.5 seat locators) | Avoid |
| Refresh Path button | Nublar reads the `RailShape` at move time, so there is no path to rebuild. A `TourStation` could validate a closed loop once and warn the player ("track broken at x, y, z") | Adapt (size S) |

**Chunk loading along long routes.**

- MTR does not need loaded chunks to move a train, because trains are data. Nublar's Explorer is an entity, so it moves only while its chunk is entity-ticking. The earlier design says this: the tour pauses when the chunk stops ticking, and the Explorer forces no chunks `[v]` (report section 7).
- A rider keeps the surrounding chunks loaded because a player is there (vanilla). So a **tour with a passenger never leaves loaded chunks** unless the loop is longer than the simulation distance. An **empty driverless loop** with no player near pauses. That is correct and costs nothing. I recommend no ticket. A ticket per Explorer would keep a strip of chunks ticking for no player, which is the cost B25 wants to avoid `[inf]`.
- A side effect to test in the playtest: when a passenger rides a long loop, vanilla loads chunks ahead of the vehicle only to the player's view distance. A track whose far end is outside simulation distance from the Explorer's position at the loop point pauses at the border. Keep park tracks inside a 10-chunk radius, or tell players.

**Compatibility duty (MTR itself).** None. It has no 26.x port. If a server runs MTR and Nublar together, vanilla rails and `TrackBlock` do not interact with MTR rail nodes.

**Platform overlap.** Vanilla: `AbstractMinecart`, `BaseRailBlock`, `RailShape`, `Component`, sound, action bar, redstone. NeoForge adds `getRailDirection`. No library is needed.

**Verdict:** Adapt (route/stop/dwell/announcement design, depot as a Tour Station). Avoid (data-only vehicles and the core). Not relevant (fares, tickets, lifts, PIDS signs, resource pack tooling).

### 5.2 Why a data-only simulation is not worth it here

MTR needs it because a metro line has dozens of cars per train and hundreds of trains. A Nublar park has one to a few vehicles of one car each. The cost of an entity with a controlled-vehicle tick is a few hundred nanoseconds per tick `[inf, estimate]`, far below the dinosaur and fence budgets in the earlier report. The dominant risk for B22 is not vehicle count. It is the **tracking and sync traffic of a driven vehicle** and the **render cost of a large model**. Both are covered in sections 3.1 and 3.6.

## 6. Valkyrien Skies 2 and Clockwork (out of scope; stay compatible)

**In game.** VS2 turns a set of blocks into a "ship" that moves, rotates, floats and collides as a rigid body, with players standing on it and using its blocks. Players assemble a ship from placed blocks, usually through an addon such as Eureka (a ship helm), and sail or fly. The wiki says VS "is a library mod and physics engine", with ships as rigid bodies with weight, friction and elasticity, and that most mods work, with two typical failure causes: the other mod is confused by the shipyard, and mixin conflicts `[doc]`. Clockwork adds propellers, bearings, balloons, gas engines and pipes, a Create-style tech tree and its own creatures and biomes on top of VS2 and Create `[doc]` (README, `changelog.md` 0.5.7).

**Mechanism.**

- **Shipyard.** Ship blocks live in a "shipyard" area millions of blocks from spawn, and are projected into the world as a moving body `[doc]` (wiki, `Valkyrien_Skies`).
- **Physics core is a separate artifact.** `vs_core` plus `krunch` (`gradle.properties:59-62`), consumed by `common/build.gradle:17-24`. The repo holds the Minecraft glue: many mixins.
- **Glue lists what it must patch.** Folders under `common/.../mixin/feature/` include `entity_collision`, `entity_movement_packets`, `get_entities`, `mob_spawning`, `shipyard_entities`, `tick_ship_chunks`, `physics_entities`, `render_blockentity_distance_check`, `poi`, `world_border`, `render_pathfinding`. Mod compat mixins exist for Sodium, ETF, Flywheel, FTB Chunks, CC Tweaked, Create, Create Big Cannons, Immersive Portals, Moonlight, Sound Physics Remastered and others (`mixin/mod_compat/`).
- **What these mixins mean for a custom entity.**
  - `get_entities` rewrites `Level#getEntities` calls so shipyard boxes become world boxes (`mixin/feature/get_entities/FEATURE.md`).
  - `entity_movement_packets` replaces the client's player position packet with a position relative to the ship when the player stands on a ship (`MixinLocalPlayer.java:~25-60`).
  - `shipyard_entities` lets chosen entity types (minecarts, paintings, item frames) live in the ship (`shipyard_entities/FEATURE.md`).
  - `tick_ship_chunks` replaces the distance test in `ChunkMap` so chunks of a ship count as near a player (`MixinChunkMap.java:~20-45`).
- Branches and versions: 1.20.1 is the main branch of the repo I read. A 1.21.1 branch exists. No 26.x branch `[v]` (`gh api` branch list). Clockwork has only the 1.20.1 branch.

**Why out of scope for Nublar.**

1. Nublar's vehicles are two ground vehicles on terrain and on a laid track. Nothing in the design needs a block-built ship, buoyancy, aerodynamics or ship-to-ship collision.
2. VS2 is a framework with a separate native-style physics core and dozens of mixins. Both cost a lot for a park mod. It has no 26.x build `[v]`.
3. VS2's own wiki says it is a library "designed to be used with addons"; its player features are the addon's (Eureka, Clockwork).
4. A driven Jeep is not a rigid body. Vanilla `move()` with step height is enough.

**Should Nublar stay compatible?** Yes, cheaply, because a server might run VS2 in a later 26.x port and a player might build a ship in or near a park. Four rules, each size S, none needs VS code:

| # | Rule | Why |
| --- | --- | --- |
| 1 | Use `level.getEntities(...)`, `getEntitiesOfClass` and `ServerLevel` helpers for all "near this block" queries. Do not walk chunk sections by hand or keep your own entity grid. | VS2 patches the vanilla calls to convert ship boxes to world boxes (`get_entities`) |
| 2 | Key stored data (fence zones, power graphs, feeder links) by the `BlockPos` the vanilla call hands you, and never assume `BlockPos` is near world spawn or inside the world border. | Ship blocks have shipyard positions millions of blocks away |
| 3 | Use the standard distance helpers (`Entity.distanceToSqr`, `Level.getNearestPlayer`) for "player near" tests. Do not compute distance from raw `getX()` pairs when it decides whether a block entity ticks. | VS2 patches ticking distance so ship chunks tick (`tick_ship_chunks`) |
| 4 | Keep block entity tickers on the normal `BlockEntityTicker` path. Do not use a global static list of fence posts that skips ticker registration. | Ship block entities run through the same path |

Dinosaurs on ships: not supported and not needed. Dinosaur pathing around a ship is the ship author's problem. Do not add a patch.

**Compatibility duty (Sodium/Iris/ETF).** VS2 ships Sodium and ETF compat mixins (`mod_compat/sodium`, `mod_compat/etf`), which is evidence that a modded block entity renderer or texture variant can be affected by shipyard positions. For Nublar, avoid reading `getBlockPos()` for render culling decisions outside the vanilla frustum test.

**Verdict:** Not relevant as a feature. Compat (passive: the four rules). Do not add VS2 as a dependency.

### Clockwork specifics

Clockwork has no vehicle code that Nublar needs. Its relevant idea is that gas and heat run on a separate solver ("Kelvin", changelog 0.5.7 lists a new default solver `JacobiSeidel`), which is a power-grid style simulation, not a vehicle one. Note for B14 (power) readers: the pattern "block entities register in one solver that runs once per tick" matches the Nublar conduit graph choice in the earlier report. Nothing to add here. **Verdict:** Not relevant.

## 7. What vanilla 26.x already provides

| Need | Vanilla 26.2 / platform piece | Evidence | Use for |
| --- | --- | --- | --- |
| Follow a rail shape, curves and slopes | `BaseRailBlock`, `RailShape`, `AbstractMinecart.exits(RailShape)` (returns a pair of exit vectors) | NeoForge `NewMinecartBehavior.java.patch` context line `Pair<Vec3i, Vec3i> exits = AbstractMinecart.exits(shape);` | S20 step 2: pick the exit direction without writing a table. Check that `exits` is accessible (it is called from a class in the same package; if it is package-private use an access widener / access transformer) |
| Custom rail shape per cart | `IBaseRailBlockExtension.getRailDirection(state, level, pos, cart)` (NeoForge only) | `IBaseRailBlockExtension.java:50`, and the patches in `NewMinecartBehavior`, `OldMinecartBehavior`, `AbstractMinecart`, `BaseRailBlock`, `PoweredRailBlock` | Only if Nublar wants switches or one-way rails later. Fabric API has no equivalent (search of the Fabric API tree finds only `MinecartComparatorLogic` and tags) `[v]` |
| New smooth minecart movement | `NewMinecartBehavior` vs `OldMinecartBehavior`, behind the feature flag `minecart_improvements` | `FeatureFlags.java.patch:6` (`builder.createVanilla("minecart_improvements")`). `NewMinecartBehavior` has `lerpSteps` and `POS_ROT_LERP_TICKS` `[doc]` (mappings.dev). | A model for how vanilla interpolates a vehicle along a rail. I could not confirm whether 26.2 turns the flag on by default. Treat it as an experimental world feature. Do not depend on it. |
| Speed cap for rail vehicles | Gamerule `minecart_max_speed` | `[doc]` (a mod page quotes the rule; not seen in source) | Not needed. Nublar sets its own speed |
| Passengers and seats | `Entity` passenger system, `startRiding`, `getPassengerAttachmentPoint`, `EntityType.Builder` passenger attachments | `[inf]` recalled from 1.21.2 and later, not read in 26.2 | B22.5 seats. Verify the API at the port |
| Controlled vehicle movement | `ServerboundMoveVehiclePacket` (the packet boats use), the "moved too quickly" check in the server packet handler | `[inf]` recalled from 1.20.1 | B22.1 position path and a free sanity check |
| Tracking range and update interval | `EntityType.Builder.updateInterval(int)` and `clientTrackingRange(int)` | `[v]` in 1.21.1 use at `FabricPlatform.java:125` | B22.8 |
| Boats | `AbstractBoat` in `world.entity.vehicle.boat`, with NeoForge `canBoatInFluid` | `AbstractBoat.java.patch` | Out of scope (boats are excluded in B22). A boat-style A and D steering (B22.4) is a control model only |
| Step height | `Attributes.STEP_HEIGHT` (1.20.5+) | `[inf]` recalled | Jeep step height of about 1.0 to 1.25 |
| Network payloads, config | NeoForge and Fabric API payload registration, Balm | Existing stack | B22.2 key packet |

Do not use `AbstractMinecart` as a base for the Explorer. It cannot be hand-driven off the track, and the lead asked for both modes. Use a custom entity and borrow the rail math only.

Option worth one design decision (see open question 1): the Explorer on a track could be an `AbstractMinecart` subclass while a second, hand-driven variant is a separate class. The pros: vanilla curve, slope, interpolation and (on NeoForge) the `getRailDirection` hook for free. The cons: two entity types for one vehicle item, and switching from rail to hand driving at runtime would need an entity swap. I recommend the single custom entity with a rail-follow mode.

## 8. Which mechanics fit a Jurassic Park tour

| Mechanic | Source | Fit | Plan module | Note |
| --- | --- | --- | --- | --- |
| Stop-until-clear with a horn after a delay | Automobility autopilot (3.4) | High. A dinosaur on the road stops the tour and the horn sounds | B22 | Tag `projectnublar:tour_vehicle_stops_for`; check every 10 ticks |
| Passive stop markers read by the vehicle | Automobility (3.4) and AC mag-lev (earlier report) | High | B22 | Block-position cache (S3). No ticking marker |
| Redstone hold on a stop marker | Automobility sign `POWERED` = stop | High. The control room can hold a tour | B22, B21/security | One boolean |
| Dwell time per stop, global default | MTR platform dwell | High | B22 | `vehicles.stop_seconds`, optional per-stop int |
| Stop announcement text and sound | MTR announcer block | High. This is the "tour" part | B22 | Component plus sound on the marker |
| "Stop 2 of 4" action-bar line | MTR HUD cycle | Medium | B22 | Size S |
| Tour Station that sends vehicles on a timer up to a limit | MTR depot, siding "maximum vehicles" | High for driverless tours | B22 | Replaces a manual start key. Size M |
| Seats, not standing passengers | Vanilla (MTR stands) | High | B22.5 | |
| One horn key | Automobility | Medium | B22 | A sound, no gameplay effect |
| Step-up for ledges | Automobility | High for the Jeep | B22 | Step height attribute |
| Boost pads, drift, turbo, off-road tiles | Automobility | Low. Racing tone | none | Skip |
| Part-building (frame, engine, wheels), crowbar | Automobility | Low. Two fixed vehicles | none | Skip |
| Upgrades, inventory, boiler fuel | Immersive Aircraft | Low. D07 says no fuel | none | Skip |
| Rigid-body ships, buoyancy | VS2 | None | none | Skip |
| Timetables, delay catch-up, interchange, fares | MTR | None for a loop | none | Out of scope per plan |

## 9. Performance and networking: consolidated answers

| Question | Answer | Evidence |
| --- | --- | --- |
| Client prediction or server authority for a driven vehicle? | Both vehicle mods use client authority for the driver. The server relays to others. Automobility checks only the sender is the driver. Immersive Aircraft checks the sender is in the vehicle. Neither clamps speed. | `CommonPackets.java:70-79`, `VehicleEntity.java:440-451`, `CommandMessage.java` |
| Packet rate | Automobility: driver to server every 4 ticks (5 Hz) `[v]`, server to trackers on dirty (up to every tick while moving) `[inf]`. Vanilla entity tracking: every `updateInterval` ticks. MTR: client request every 0.5 s, server pushes vehicle updates `[v]` | `AutomobileEntity.java:134, 600-611, 813-815`; `InitClient.java:~430-439` |
| Interpolation | Lerp to the last tracked position over `updateInterval + 1` ticks (Automobility) or a fixed 10 steps (Immersive Aircraft). MTR dead-reckons along a rail path | `AutomobileEntity.java:793-811, 1287-1293`; `VehicleEntity.java:382-389, 562-579`; `MainRenderer.java:~96` |
| Rendering | Automobility: immediate-mode parts, several buffers per car. Immersive Aircraft / MoMP: runtime-loaded `.bbmodel` with a per-part handler. MTR: custom renderer, not an entity renderer, with per-car occlusion culling. Nublar: GeckoLib 5, one entity renderer. | `AutomobileRenderer.java:30-120`; `EconomyPlaneEntityRenderer.java`; `RenderVehicles.java:~85-100` |
| Terrain collision at speed | Automobility replaces collision with its own sweep, step and ground scan. Immersive Aircraft uses vanilla `move` and reports collision speed. Vanilla `move` sweeps per axis and does not tunnel at the speeds in the design (0.25 b/t track, a few b/t Jeep) `[inf]`. | `AutomobileEntity.java:1808-1840, 1873-1975`; `VehicleEntity.java:779-797` |
| Chunk loading along routes | MTR: none, trains are data. Automobility and Immersive Aircraft: none, the vehicle pauses in unticked chunks. Nublar: no tickets, riders keep chunks loaded. | grep of MTR source; entity classes |
| How MTR keeps many trains cheap | Off-thread simulation, no entities, no chunk ticking, per-player radius, client dead reckoning, occlusion culling, a watchdog | `Init.java:184, 233-241, 199-213`; `InitClient.java`; `RenderVehicles.java` |
| Entities per vehicle | Automobility: 1 plus one hitbox entity per box. Immersive Aircraft: 1. MTR: 0. | `AutomobileEntity.java:583-590` |

Numbers I could not measure: MSPT or FPS cost of any of these mods. No profiling was run. Cost statements in this note are structural.

## 10. Anti-patterns to avoid (collected)

1. Trusting a client-reported collision damage or speed (Immersive Aircraft `CollisionMessage`; Automobility sync state with no clamp).
2. Auto-boarding any `Mob` that touches the vehicle (both vehicle mods). Dinosaurs would board.
3. One extra entity per hitbox box (Automobility). Breaks the B26 and B27 hitbox budget.
4. Per-tick `getEntitiesOfClass` for the blocked check (Automobility autopilot). Use every 10 ticks.
5. A marker block that scans a 32 by 4 by 32 box every 4 ticks for vehicles (Automobility sign). Use a vehicle-side cache.
6. A fixed interpolation step count that ignores `updateInterval` (Immersive Aircraft: 10).
7. Several render types and buffers per vehicle per frame (Automobility).
8. A culling box that excludes parts of the model (any vehicle with an open door or aerial).
9. A synchronous wait on a background thread with a hard stop of the server (MTR watchdog). Right for a core it controls, wrong for Nublar.

## 11. Closing table

| Technique | Source | Nublar module | Verdict | Effort | Expected effect |
| --- | --- | --- | --- | --- | --- |
| Driver client runs physics, server relays and clamps | Automobility, Immersive Aircraft | B22.1 | Adopt (add the clamp they lack) | M | Smooth driving, no cheat speed. Server cost stays tiny |
| Skip the driver when relaying state | Automobility `forPlayersTrackingMe(true, ...)` | B22.1 | Adopt | S | One fewer packet per tick per vehicle |
| `updateInterval` 3 and `clientTrackingRange` 10 as vehicle defaults, tune in playtest | Automobility `AutomobilityEntities.java:21-24` | B22.8 | Adopt (starting values) | S | Smooth lerp, bounded traffic |
| Lerp length from the entity type's real update interval | Automobility vs Immersive Aircraft | B22 | Adopt | S | No jitter when the interval changes |
| Dinosaur and mob auto-board block | Findings from both vehicle mods | B22 | Adopt (as a guard) | S | No dinosaur in a Jeep |
| Union culling box | Automobility `:627-637` | B22, B25 | Adopt | S | No pop-out with EntityCulling |
| Stop-for tag, blocked check every 10 ticks, delayed horn | Automobility autopilot | B22.7 | Adapt | S | Tour stops for dinosaurs, clear signal to riders |
| Redstone hold on `TourStopBlock` | Automobility sign `POWERED` | B22, security | Adapt | S | Control room can hold a tour |
| Passive stop markers read on block-position change | AC mag-lev (earlier), contrast with Automobility signs | B22.7 | Adopt | S | Zero cost with no vehicle near |
| Reuse `AbstractMinecart.exits(RailShape)` for the exit vector | NeoForge 26.2 patch context | B22.7 (S20) | Adopt (check visibility) | S | No custom shape table |
| `TrackBlock` extends `BaseRailBlock`, vanilla `RailShape` property | Vanilla; Fabric has no rail hook | B22.7 | Adopt | M | Placement, curves and slopes like rails, both loaders |
| NeoForge `getRailDirection` for switches | NeoForge `IBaseRailBlockExtension` | later | Compat (optional) | S | Not needed for Beta |
| Tour Station with a vehicle limit and departure interval | MTR depot and siding | B22 | Adapt | M | Driverless tours without a key press |
| Per-stop dwell override | MTR platform dwell | B22 | Adapt | S | Longer stop at the T. rex paddock |
| Stop announcement (text and sound) and "Stop n of m" HUD | MTR announcer, HUD cycle | B22 | Adapt | S to M | The tour feels guided |
| Loop validation message instead of "Refresh Path" | MTR Refresh Path | B22 | Adapt | S | Clear error for a broken track |
| Step height 1.0 to 1.25 on the Jeep | Automobility wheels, vanilla attribute | B22 | Adopt | S | Climbs ledges |
| Client-only body tilt from wheel heights | Automobility ground scan (simplified) | B22 | Adapt | S | Looks alive, no server cost |
| Handling numbers in one data record | Automobility parts | B22.3 | Adapt | S | Jeep and Explorer share a class |
| Data-only vehicles and an off-thread core | MTR | none | Avoid | L | Not worth it for one to a few vehicles |
| Standing passengers with noclip | MTR | none | Avoid | L | Seats are simpler |
| Hitbox entities | Automobility | none | Avoid | M | Would add entities |
| Modular parts, boost pads, drift, upgrades, fuel | Automobility, Immersive Aircraft | none | Avoid / Not relevant | L | Wrong tone or D07 |
| Rigid-body ships | VS2, Clockwork | none | Not relevant | L | Out of scope |
| Four passive VS2 compat rules (vanilla entity queries, `BlockPos` keys, standard distance helpers, normal tickers) | VS2 mixins | all modules | Compat | S | Works near ships if a 26.x port exists later |
| Chunk tickets for tours | none | B22 | Avoid | S | Avoids standing chunk cost |

## 12. Open questions (real design gaps)

1. **Rail-follow base class.** The Explorer is one vehicle that is hand-driven off the track and follows a track on it. Should it be one custom entity with a mode, or an `AbstractMinecart` subclass on track with a separate class off track? I recommend one custom entity. The lead should confirm, because vanilla minecart behaviour gives smoother curves and slope handling for free.
2. **End of track.** The plan says the Explorer stops at a track end. Should a dead-end instead reverse (a shuttle), or must every tour be a closed loop? A loop is simpler. Switches and junctions are out of scope.
3. **What blocks the tour.** Which entities stop the Explorer: all dinosaurs, only large ones, players, any living entity? A default tag with all dinosaurs and players is my proposal. The lead should say if a dinosaur on the track should block the tour or whether the Explorer may push or run over (damage) small dinosaurs.
4. **Is the Tour Station in Beta?** The plan says the Explorer runs "driverless tours in a loop" started by the driver with a key. A timer-based station (MTR depot idea) is extra scope. Decide before the module task list.
5. **Announcements.** Are stop announcements wanted (translation keys per stop, or free text typed by the player on the marker)? Free text needs a text-entry screen (size M).
6. **Seat count and passenger rules.** How many seats do the Jeep and the Explorer have? Can a player board a driverless Explorer that is in motion, or only at a stop? (Dinosaurs never board.)
7. **Vehicle against fences.** Should a vehicle break through an electric fence, be stopped by it, or take damage? The design decisions for fences do not say. This decides whether the vehicle needs a fence-contact rule.
8. **Chunk behaviour of long loops.** The park loop may run longer than the simulation distance from a rider. Should the Explorer wait at the border (vanilla behaviour) or should the design cap loop size? I recommend documenting the cap and not adding tickets.
