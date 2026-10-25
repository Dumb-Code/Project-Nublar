# JurassiCraft 3.0.14: vehicles and technology (clean-room study for B12, B14, B15, B20, B22, P2)

Researcher note for the JurassiCraft 3 survey. Subject: JurassiCraft 3.0.14 for NeoForge 26.1.2 (CAZ LLC, paid, proprietary). Date of reading: 2026-10-11. Topic: vehicles (part 1) and technology (part 2).

This note is a clean-room study. It describes behaviour, state, steps, numbers and timings. It copies no code. A short identifier is quoted only to locate a fact.

Labels: `[v]` verified in decompiled source or in a data file (path and line range given), `[inf]` my inference or my own arithmetic from verified numbers, `[doc]` text inside the mod (config comments, lang keys). Every performance number that is not `[v]` is an estimate.

## 0. Scope, sources and limits

**Sources.** Decompiled source under `scratchpad/jc/src/caz/jurassicraft/` (the paths below drop this prefix). Raw jar data under `scratchpad/jc/raw/` (`data/jurassicraft/recipe`, `assets/jurassicraft`). The decompiler failed on some classes. I read their bytecode with `javap`. The bytecode has no source line numbers. I cite it as "bytecode, method X".

**Names are obfuscated.** Package names, class names and method names are random words. The same name is reused in many packages. Each class below has a name that I inferred, followed by its path.

**Not read, by rule.** The 14 packages on the skip list, `raw/jurassicraft/`, the `META-INF` licence files, and the licence calls in the two root classes. Two more files sit in packages that are not on the list. I stopped reading them after their first screen because they were out of scope: `vtrqoo/gjtxsudo.java` and `ufpurxw/omogwgxr.java`. This note does not describe them. The DNA machine class calls into them (section 12.5). I did not follow the calls.

**Decompile failures that matter.**

| Class | Path | What I did |
| --- | --- | --- |
| vehicle base entity | `rmeh/dnukfuguy.class` | Whole class failed. Read with `javap -c`. All vehicle base facts come from bytecode. |
| helicopter ground check | `rmeh/gjtxsudo.java` method `uzuj` | `javap`. It resets fall distance for every passenger each tick, server side. |
| tour rail connect check | `rmeh/dbgouk.java` method `aeygzxj(BlockPos)` | Not read. Rail-neighbour test, low value. |
| tyre mark renderer | `prulnm/ummp.class` | Not read. Only its input record is known (section 10.6). |
| machine menu base | `snqzaxu/onzkovhp.class` | Not read. Quick-move rules are unknown. |
| screen base | `dbgouk/aeygzxj.class` | Not read. |
| `dismount_riders` handler | `znpnvzxz/dnukfuguy.java` | `javap`. It stops dinosaurs that ride the player. It is not a vehicle packet. |

**Earlier work that this note extends and does not repeat.** `docs/project-plan.md` B12, B14, B15, B20 and B22. The vehicle survey in `research_notes/Optimization and tech mod survey/vehicles_and_transport.md` (Automobility, Immersive Aircraft, MTR). The machine survey `machines_power_and_storage.md` in the same folder. This is the first note that covers a Jurassic Park mod built on 26.x.

**Version fact.** JurassiCraft 3 targets NeoForge 26.1.2. It uses 26.x APIs that Nublar needs at P2: `ResourceHandler` and `ItemResource` for item storage, `InterpolationHandler` for entity smoothing, `ValueInput` and `ValueOutput` for save data, `RecipesReceivedEvent` for recipe sync, `GuiGraphicsExtractor` and `submit` render states. It is a working example of the target platform.

## 1. What to take from this note (top findings)

1. **The vehicle set is small and simple.** Three vehicles: Ford Explorer, Jeep Wrangler, helicopter. Each is one entity with 2 invisible hitbox parts (cars) or 1 (helicopter). No fuel. No upgrade parts. Speed is 3 fixed presets. This matches D07 (no fuel) and the plan's two-car scope (section 2).
2. **Car physics is one line of arithmetic.** Each tick: multiply velocity by 0.8, add a push along the facing, add gravity 0.15 when airborne. Top speed is 5, 10 or 20 blocks per second (slow, medium, fast preset). Steering is a yaw rate that grows with the push and clamps at 3 degrees per tick. There is no tyre grip model (section 3). Nublar can match this "arcade" feel in about 60 lines.
3. **Body tilt is client-visible only, from 4 edge probes.** Every 20 ticks (staggered by entity id) the car samples the ground height along its front, rear, left and right edges at 0.25-block steps. Four smoothers (0.15 block per tick) turn the heights into pitch and roll. This is the cheap tilt that the earlier vehicle note proposed (section 4).
4. **Steps are 1.5 blocks.** `maxUpStep` is 1.5 on every vehicle. The plan says 1.0 to 1.25 (section 4).
5. **Authority differs by vehicle.** The helicopter forces server authority (both authority hooks return false). The cars keep the vanilla default, so the driving player's client moves a car `[inf]`. Clients smooth any vehicle over 3 ticks. The entity type updates every 1 tick, tracks at 12 chunks (section 5).
6. **The control protocol reuses vanilla input.** The server reads movement keys from the player's last vanilla input. The custom `vehicle_control` packet carries only the 3 extra helicopter keys. The client sends it only when those 3 bits change. The server accepts it only from the controlling passenger (section 6).
7. **"Tour rails" are an own rail system with 20 shapes, 4 speed tiers and no stops.** Diagonal pieces and smooth corner pieces exist. The Explorer follows them at 1.25, 2.5 or 5 blocks per second per axis `[inf]`. There is no stop marker, no blocked-track check, no horn, no loop check. A rider is enough to start it. Nublar's B22 tour is richer (section 7).
8. **The helicopter uses a physical model.** Rotor thrust follows momentum theory. Engine speed ramps 0 to 300 over 12.7 s. Hover needs about 57 percent engine speed. Banking turns it. A 4-gauge HUD shows engine, horizon, altitude and compass. A landing with more than 30 degrees tilt destroys it (section 8). Aircraft are out of Beta scope in the plan. Keep this as a reference.
9. **The machine framework is data-first.** One abstract block entity holds a typed slot list. One layout record per machine gives slot coordinates and roles. The same layout drives the menu, the JEI category and the `/jcgui` preview. One generic recipe machine base handles progress, status and output (sections 11 and 12).
10. **Idle machines cost almost nothing on the network.** Block updates go out only when a status changes. Progress travels only through `ContainerData` to an open menu. No packet flows while a machine runs. The server still ticks every machine every tick. There is no idle sleep (section 14).
11. **There is no power system.** No FE, no generator, no conduit. Machines run free. Fences run on redstone: a powered base block electrifies connected bases within 8 blocks. This is the opposite of B14 (sections 15 and 17).
12. **Automation is item-only and the same on all six sides.** Each slot has a role. Hoppers insert only into input slots and extract only from output slots. There is no fluid or energy capability. Water is a bucket item in a slot (section 16).
13. **Recipe time is capped at 32767 ticks.** The cap matches the 16-bit values of `ContainerData`. B12 should adopt the cap and say why (section 12.2).
14. **Compat is cheap.** A Jade provider named `machine_status` shows one status line plus a percentage. A JEI plugin makes one category per machine from the same layout and adds recipe-transfer handlers (section 19).

## 2. Vehicle roster and registration

**In game.** The player crafts three vehicles. The Ford Explorer has 4 seats and 4 doors. The Jeep Wrangler has 4 seats and 2 doors. The helicopter has 5 seats. Right-click boards a vehicle. Sneak-right-click does not board. The player places a vehicle with the item. A vehicle item stacks to 1.

**Mechanism.**

- Entity types register in `wkjvl/cvrd.java:17-19, 38-41` `[v]`. Explorer and Jeep: size 3.0 wide, 2.5 high. Helicopter: size 5.0 wide, 3.5 high. All three: mob category MISC, client tracking range 12 chunks, update interval 1 tick.
- Vehicle items register in the same file (`:20-22, 43-47`). The helicopter item places the entity at the exact click point. The two car items place it at the centre of the block next to the clicked face. Both set yaw to the player's yaw. The item fails if the new entity box collides with blocks. It shows no message. It consumes one item unless the player has infinite materials (vehicle item, `rmeh/rtwzimpls.java:20-48`).
- The Explorer entity is `rmeh/aeygzxj.java`. The Jeep entity is `rmeh/kodkieb.java`. The helicopter base class is `rmeh/gjtxsudo.java`. The helicopter entity is `rmeh/kdhoqn.java`. All three extend the vanilla `VehicleEntity` through the base `rmeh/dnukfuguy.class`.
- Recipes (`raw/data/jurassicraft/recipe/*.json`) `[v]`: car chassis from 8 iron ingots, "unfinished car" parts, then Explorer (dyes plus glass pane) and Jeep (dyes plus 4 tyres plus unfinished car). The helicopter takes an `engine_system` item (cauldron, comparator, repeater, piston, lever, pressure plate, iron bars, iron ingot), iron blades, 4 tyres, a windscreen and an iron block.
- There is no horn. There is no fuel item. There is no fuel field `[v]` (no class in the vehicle packages holds one).

| Vehicle | Class | Seats (x, y, z in blocks, door flag) | Wheel box (half-track, front z, rear z) | Hitbox parts (name, offset z, size w x h) | Step |
| --- | --- | --- | --- | --- | --- |
| Ford Explorer | `rmeh/aeygzxj.java:42-52` | 2 front at z 0.4, 2 rear at z -1.0; x +-0.563; y 0.45; all with doors | 1.0, +2.0, -2.2 | bonnet +1.75 (2.0 x 1.2); boot -1.85 (2.0 x 1.5) | 1.5 |
| Jeep Wrangler | `rmeh/kodkieb.java:12-24` | 2 front at z 0.0, x +-0.563, y 0.45, doors; 2 rear jump seats at z -2.2, x +-0.5, y 0.7, no doors | 1.2, +1.5, -2.65 | bonnet +1.4 (2.4 x 1.2); boot -2.1 (2.4 x 1.5) | 1.5 |
| Helicopter | `rmeh/kdhoqn.java:15-29` | pilot (0, -0.23, 1.2); 2 side seats (+-0.55, -0.34, 0.1); 2 rear seats (+-0.4, 0.25, -1.0) | 1.0, +2.0, -2.2 (unused by flight) | "rotor" at y +3.5 (10.0 x 0.5) | 1.5 |

The seat y value is shifted up by 0.6 when the rider is placed (`rmeh/rqkeevhve.java:38-42`). Seat 0 is the driver or pilot.

**Platform overlap.** Vanilla `VehicleEntity`, `EntityType.Builder` (`sized`, `clientTrackingRange`, `updateInterval`) and `Item.useOn` cover every registration step. Nothing here needs a library.

**Verdict.** Adapt. The roster is the same as the plan's B22 (Jeep, Explorer) plus one aircraft that the plan puts out of scope.

## 3. Car driving model

**In game.** The driver holds forward. The car speeds up fast, reaches a top speed and holds it. Left and right turn the car. Reverse is slower. The car does not slide sideways. The car turns while the driver steers even with no throttle, at a lower rate. Leaves and vines break under the car (config `vehiclesDestroyBlocks`, default on).

**Mechanism** (base class bytecode, `rmeh/dnukfuguy.class` methods `tick`, `ctygd`, `lyhmwcjw` `[v]`).

- Input flags: left 1, right 2, forward 4, backward 8, jump 16, descend 32. Only the first four matter for a car.
- Per tick, in this order, when this instance is authoritative: (1) multiply velocity by 0.8 on all three axes and the steering rate by 0.8; (2) if not weightless, subtract 0.15 from vertical velocity; (3) compute a push; (4) steer; (5) `move`.
- Push = (0.05 if a steer key is held with no forward or backward key) + (0.1 if forward, or -0.05 if backward). The push is multiplied by the speed preset modifier (slow 0.5, medium 1.0, fast 2.0). In water the push drops by 0.1, with a floor of 0. The push goes along the facing: x by -sin(yaw), z by cos(yaw).
- Steering: left subtracts 20 x push from the yaw rate. Right adds 20 x push. The rate clamps at +-3 degrees per tick. The yaw then adds the rate. Push is negative in reverse, so steering inverts in reverse.
- Top speeds `[inf]` (my arithmetic: push / (1 - 0.8)): forward 0.25 / 0.5 / 1.0 blocks per tick = 5 / 10 / 20 blocks per second for slow / medium / fast. Reverse is half of forward. Free-fall terminal speed is about 0.75 blocks per tick.
- The steering rate reaches its 3-degree cap in 2 to 4 ticks. The effective turn rate is 60 degrees per second at every preset once steering has built up `[inf]`.
- There is no lateral grip term. The 0.8 drag acts on the whole velocity, so a sideways push dies in a few ticks. The car feels "on rails" in turns.
- The speed preset is stored in the entity (`Speed` in save data, default medium). Nothing in the mod changes it at run time except a tour rail tier (section 7). The setter has no other caller in the compiled classes `[v]` (class search of `raw/`). A player cannot pick a preset on a car off the rails.
- Vehicle block damage (`vehiclesDestroyBlocks`): every 20 ticks (staggered by entity id), the car breaks all leaves in its box with no drop, and vines and cave vines with a drop (`rmeh/dnukfuguy.class` method `dfdtwro`).
- Roadkill: when the measured speed is above 0.1 blocks per tick, every living entity in the box inflated by 0.1 takes damage equal to the speed in blocks per tick (so about 0.5 at medium) on the server, each tick (method `dnukfuguy(ServerLevel)`). Living entity invulnerability frames limit this to about one hit per 10 ticks `[inf]`.

**Platform overlap.** Vanilla `Entity.move`, `Mth`, `ServerPlayer.getLastClientInput` cover everything. No library is involved.

**Verdict.** Adapt. Copy the structure (drag, push, capped yaw rate, three presets). Choose the numbers for the park scale. Add the server clamp from the plan (B22), which JurassiCraft 3 lacks for cars (section 5).

## 4. Suspension, wheels, terrain, collision and hitbox parts

**In game.** The body pitches and rolls on slopes. The front wheels steer. The steering wheel turns. Doors swing open when a player stands within 4 blocks of a free seat. The car climbs 1.5-block steps without a jump.

**Mechanism.**

- **Edge probes** (`rmeh/dnukfuguy.class`, methods `cvrd`, `dnukfuguy(double,boolean)`). Four probes: front edge, rear edge, right edge, left edge. Each probe walks along its edge in 0.25-block steps, starts at the entity's y, scans down at most 32 blocks, and takes the highest collision-shape top it finds. A free cell above a solid block counts as a clear stand. The probes run when the car has moved or turned, and then at most once per 20 ticks, with an offset of (entity id mod 20) `[v]`.
- Four smoothers (`rmeh/ummp.java`, step 0.15 blocks per tick) follow the four heights. A 5th smoother (step 0.1) follows the steering wheel angle (40 degrees full lock). The seat doors use one smoother each (step 0.1 radian per tick, open angle 60 degrees).
- The renderer turns the heights into pitch and roll with a law-of-cosines helper (`rmeh/hmocea.java`, `prulnm/dnukfuguy.java:55-72`). It rotates the model parts "wheel holder front" and "wheel holder back" by half the pitch. It rotates the whole body by pitch and roll.
- The probes run on both server and client `[inf]` (the tick calls them outside any side check). The server does not need the result. This wastes work on the server.
- **Wheel tracking.** Four wheel records (`rmeh/vtclb.java`) keep the current and previous world position of each wheel. The client uses them for tyre marks.
- **Wheel spin.** The client measures horizontal speed x 4, clamps to 1, and smooths at 0.4 per tick. The result adds to an accumulated spin angle (`qkek`, bytecode).
- **Step height** is 1.5 for every vehicle (`maxUpStep`, `[v]`). Vanilla `move` does the collision. There is no custom collision.
- **Hitbox parts.** Cars own 2 `PartEntity` children ("bonnet", "boot"). The helicopter owns 1 ("rotor"). Each part sits at a fixed offset along the vehicle's facing and is moved every tick (`rqkeevhve`, bytecode). The parts get entity ids right after the vehicle's id (the constructor reserves count + 1 ids). A part forwards damage, push and right-click to the vehicle (`rmeh/fgwme.java:30-47`). The vehicle's culling box is the union of its box and the part boxes (method `fgwme()`).
- Parts register with a per-level spatial index when the config switch `partSpatialIndex` is on (default on, `dnukfuguy.java:27` `[doc]`). The index stops entity queries from scanning every part in the dimension.

**Platform overlap.** Vanilla `PartEntity` (NeoForge), `Entity.maxUpStep`, `Block` collision shapes. On Fabric, `PartEntity` is not in Fabric API. A Fabric port needs its own part type or a vanilla multi-box approach.

**Verdict.**

- Edge-probe tilt: adapt, client only. Run it from the render state, not the tick. Keep the 0.15 block per tick smoothing.
- Step height: the plan says 1.0 to 1.25. JurassiCraft 3 uses 1.5 on a 3 x 2.5 box. Treat 1.5 as the upper bound.
- Hitbox parts: the earlier note said "one box with a union culling box". JurassiCraft 3 uses 2 parts per car. Parts give a clickable bonnet and boot. Do not adopt them for Beta. B26 and B27 already own multipart rules.

## 5. Authority, interpolation, tracking

**In game.** The helicopter moves smoothly for the pilot and for other players. The cars move smoothly when the driver steers.

**Mechanism.**

- The helicopter overrides both authority hooks and returns false (`rmeh/gjtxsudo.java:84-90` `[v]`). The server therefore simulates the helicopter. The pilot's client does not.
- The base car class does not override these hooks (method list of `rmeh/dnukfuguy.class`). Vanilla then treats a car with a player driver as client-authoritative `[inf]`. The driver's client runs `lyhmwcjw` and the vanilla vehicle-move packet carries the position. Vanilla's own validation applies. JurassiCraft 3 adds no clamp of its own for cars `[v]` (no speed or distance check exists in `znpnvzxz/` or in the car classes).
- Every vehicle uses an `InterpolationHandler` with 3 steps (`rmeh/dnukfuguy.class` constructor, `getInterpolation`) `[v]`. The entity type sends an update every 1 tick (`wkjvl/cvrd.java:41`). The interpolation length (3) is not tied to the update interval (1), so a lost packet shows as a small hitch.
- `shouldRenderAtSqrDistance` returns true for every distance (bytecode). A vehicle never culls by distance on the client. Tracking range (12 chunks) is the only limit.
- Passengers: five synced integer fields hold the entity id in each seat (-1 = empty). The client resolves seat to entity from these ids and checks that the entity rides this vehicle (`vtclb(int)`, bytecode). Seat data is therefore part of the vehicle's entity data, not derived from the passenger list order.
- Seat assignment: `addPassenger` (server) uses the seat remembered for this player's UUID if it is free; else the first free seat (0 first). `removePassenger` clears the seat and clears the input flags when the driver leaves. A `Seats` child in the save data stores seat to UUID so riders return to their seats after a reload.
- A synced byte holds input flags. The client writes it locally for prediction. The server writes it from the last vanilla input (section 6).

**Platform overlap.** Vanilla (26.x) `Entity.isLocalInstanceAuthoritative`, `InterpolationHandler`, `ServerboundMoveVehiclePacket`. Fabric and NeoForge share these. Nothing special.

**Verdict.** Adapt. The plan already says the driver's client runs physics and the server clamps. JurassiCraft 3 shows both modes in one mod. Pick one mode per vehicle and test it on a 100 ms link.

## 6. Control protocol and validation

**In game.** The pilot presses keys and the helicopter reacts at once. The pilot presses Ctrl+1 to Ctrl+5 to change seats while riding.

**Mechanism.**

- Registrar: `znpnvzxz/aeygzxj.java` registers 4 clientbound and 5 serverbound payloads in one mod-version channel.
- `vehicle_control` (`znpnvzxz/kdhoqn.java:6-41`): fields = vehicle entity id (varint) + 1 byte. Bits 6, 7 and 8 of the byte carry "descend", "yaw left" and "yaw right". The lower 5 bits are ignored.
- Server checks, in order `[v]`: sender is a real player and not a spectator (`znpnvzxz/jgdp.java:14-18`); entity id resolves to a vehicle; the sender is the controlling passenger (seat 0). Then the server builds the flags: lower 5 bits come from `ServerPlayer.getLastClientInput` (the vanilla input), bit 6 from the packet. For a helicopter it also sets the two yaw keys.
- The client sends `vehicle_control` only when the byte changes or when the vehicle id changes (`prulnm/jgdp.java:134-149`). It sends nothing when the player leaves the vehicle (state resets locally).
- Wire size `[inf]`: a varint id (1 to 3 bytes) + 1 byte = 2 to 4 bytes of payload. The custom-payload frame adds the identifier `jurassicraft:vehicle_control` (28 characters + length prefix). Estimate 33 to 36 bytes on the wire per send. Send rate: only on key edges, so a few per second at most.
- `vehicle_action` (`znpnvzxz/kijk.java:7-55`): varint vehicle id + enum (take seat, toggle autopilot, toggle attitude lock) + varint seat index. The server checks that the sender rides that vehicle. Take-seat: the server tries to move the sender into the seat. Toggles: the sender must be the controlling passenger. The index is range-checked inside the seat method (`rmeh/dnukfuguy.class`, method `dnukfuguy(Entity,int,boolean)`).
- Rate limit: none. A client can spam toggles. The toggles flip one bit, so spam only flickers the HUD text and the stabiliser `[inf]`.
- Distance check: none, and none is needed. The controlling-passenger check already ties the sender to the vehicle.
- Other serverbound payloads in the layer (not vehicle): `set_order` (dinosaur id + order, checks owner list and 8-block distance), `place_paddock_sign` (position, face, hand, sign id; checks 8 blocks, held item, `mayUseItemAt`, registry key), `dismount_riders` (unit payload, 0 bytes). Clientbound: `open_paleo_pad` (13 fields, mostly varints), `open_order_screen`, `open_paddock_sign`, `growth_clock` (1 long, 8 bytes).
- Key conflict: all vehicle keys use a conflict context that is active only while the player rides a vehicle (`vtclb/ummp.java:11-19`). Key bindings: descend = Left Ctrl, yaw = Left and Right arrow, autopilot = Ctrl+A, attitude lock = Ctrl+D, zoom in/out = Page Down / Page Up, seats = Ctrl+1 to Ctrl+5, dismount riders = C.

**Platform overlap.** NeoForge `RegisterPayloadHandlersEvent` and `PayloadRegistrar`. Fabric API has equivalent `PayloadTypeRegistry`. The vanilla input packet already covers WASD and jump on both.

**Verdict.** Adopt the pattern: vanilla input for keys, one small custom packet for extra keys, send on change only, check "sender is the controlling passenger". Add what is missing: a clamp on the cars' reported speed, and a cap of one packet per player per tick (the plan already requires both).

## 7. Tour rails and rail mode (Ford Explorer)

**In game.** The player lays "tour rail" blocks. They come in 4 tiers: plain, slow, medium, fast (red, orange, green item tint). The Explorer, with a rider, rolls onto the rail and follows it. Curves, slopes and 45-degree diagonals all work. The rail speed sets the Explorer's speed. At a rail end the Explorer leaves the rail and drives free. Pressing the backward key once turns the Explorer round on the rail. There is no stop, no announcement and no braking for dinosaurs.

**Mechanism.**

- **Block** (`rmeh/dbgouk.java`, 437 lines): non-collision block, strength 1.0, needs the correct tool, push reaction DESTROY, waterloggable. It needs a rigid support below (and next to it for an ascending piece). Shape box height 2/16 (flat) or 8/16 (ascending) `[v]`.
- **Shape property** (`rmeh/onzkovhp.java`): 20 values. Straight (2), ascending (4), corners (4), diagonal (2), and 8 "smooth transition" pieces (horizontal and vertical, each a straight-to-diagonal bend). Each shape stores a forward and a backward exit as a vector in half-block units (diagonals use +-0.5).
- **Auto-shape logic** (inner class in `rmeh/dbgouk.java:237-436`): like vanilla `RailState`. On place it looks at north, south, east and west neighbours, picks a straight or corner shape, then upgrades to ascending (if the next block up is also a rail) or to a smooth transition piece (if a diagonal fits). It updates the neighbours it changed. It does the same on removal.
- **Crafting** (`recipe/tour_rail.json`, `tour_rail_*_from_*.json` `[v]`): 4 iron bars, 3 rails and 2 redstone in a 3 x 3 give 16 tour rails. Four shapeless one-ingredient recipes cycle the tier: plain to slow to medium to fast to plain.
- **Config**: `tourRailsEnabled` (default on). Off, the rails stay placeable and carry nothing (`dnukfuguy.java:31` `[doc]`).
- **On-rail test** (`rmeh/aeygzxj.java:113-173`): each tick, the Explorer looks for a tour rail at its block position, one below, or two below if that rail is an ascending piece. If it finds none, it keeps the last rail when that rail still has a neighbouring rail in its two exits and the Explorer is within 1 block on x and z. Else it leaves rail mode.
- **Rail mode changes**: collision box shrinks to 0.75 x 0.25 blocks, pick radius grows to 2.25, the motion direction turns 90 degrees, the sound volume follows the speed tier only while a rider is aboard, and the car-physics branch is skipped (`:62-84`, `:86-111`).
- **Rail step** (`:235-328`, server or authoritative side only):
  1. If the velocity is zero, a rider starts the Explorer with a push of 0.02 blocks per tick along its look direction. Without a rider, it uses a saved "RailMotion" vector (saved in the entity data), else it stays still.
  2. On an ascending piece, the speed along the slope changes by 0.0078125 (1/128) per tick, like a vanilla minecart.
  3. It projects the velocity onto the rail direction (exit vectors), caps the speed at 2.0, and flips the sign once if the backward key was just pressed.
  4. It snaps the position onto the rail line, with the y at the rail block height (+1 on slopes).
  5. It turns the body toward the rail direction with a rate limit of (tier modifier x 20) degrees per tick: 10, 20 or 40.
  6. It moves by at most (tier modifier / 8) blocks per tick on each axis: 0.0625, 0.125 or 0.25 blocks per tick = 1.25, 2.5 or 5 blocks per second per axis `[inf]`. A diagonal run is up to 1.41 times faster.
  7. It multiplies the velocity by 0.9 with a rider or 0.75 without, then adds 0.06 blocks per tick along the motion. The push keeps the velocity above zero, so a moving empty Explorer keeps rolling.
  8. It then adds (y before the move - y after the move) x 0.05 to the speed, so it speeds up downhill and slows uphill.
- **No stop, no scan, no announcement.** The rail logic never reads an entity, a stop block, a signal or a clock. The only living-entity effect is the roadkill rule of section 3, which uses the 0.75 x 0.25 box in rail mode `[v]`.
- **Tier effect.** A tier rail replaces the Explorer's preset for the next step; the plain rail keeps the stored preset (default medium) (`rmeh/stta.java`, `rmeh/kijk.java`, `rmeh/aeygzxj.java:175-191`).

**Platform overlap.** The plan uses vanilla `BaseRailBlock` and `RailShape` (10 shapes, no diagonals). JurassiCraft 3 does not reuse them. It wrote an own 20-shape enum and an own auto-shape class (about 250 lines). Vanilla 26.2 minecart behaviours exist (`NewMinecartBehavior`), but JurassiCraft 3 does not use them either.

**Verdict.**

- Own rail shapes with diagonals: drop for Beta. The plan's vanilla `RailShape` plus `AbstractMinecart.exits` is shorter and keeps vanilla placement.
- Tier blocks that set the speed: adapt as a later option. Recipe cycling between tiers is a good player-facing idea (size S).
- Tour behaviour (stops, blocked-track check, horn, announcements, loop check): none exists here. B22 stays original work. Nothing to copy.
- "Backward key reverses once": cheap; adopt as a driver option on the track.
- A rider starts the vehicle; an empty vehicle keeps rolling: adopt "push when a rider is aboard".

## 8. Helicopter flight model

**In game.** The pilot sits at the front. Space raises the engine speed. At about 57 percent engine speed the helicopter lifts. Forward, back, left and right tilt it. The arrow keys turn it. Ctrl lowers engine speed. A stabiliser (Ctrl+A) holds hover and levels the helicopter. An attitude lock (Ctrl+D) limits tilt to 15 degrees. A landing with too much tilt destroys the helicopter.

**Mechanism** (`rmeh/gjtxsudo.java`, `rmeh/kdhoqn.java`; constants in `kdhoqn.java:12` `[v]`).

- **Constants**: power 3992 "horsepower" (x 735.5 = 2.94 MW), max engine speed 300 (rpm), mass 6838, rotor radius 5, drag factor 2.0, box height 3.5, width 5.0. The constructor derives torque = power / (300 x 0.10472).
- **Engine speed** is a synced float, 0 to 300. Throttle step per tick = 3992 x 0.00029481 = 1.177 `[inf]`. From 0 to 300: 255 ticks = 12.7 s. From 0 to hover: 7.3 s.
- **Thrust** (momentum theory) `[inf]`: T = cube root of (2 x 1.2 x A x (0.75 x P)^2), with A = pi x 25 = 78.5 and P the power at the current engine speed. Full thrust is 97 kN. Weight is 67 kN. Hover needs 172 rpm (57 percent). The hover engine speed shows on the HUD as a second needle.
- **Tilt split**: vertical thrust = T x (1 - |pitch|/90) x (1 - |roll|/90). Forward thrust = 5 x T x (pitch/90) x (1 - |pitch|/90) x (1 - |roll|/90)^2. Side thrust = T x (roll/90) x (1 - pitch/90). Drag on each axis = 0.5 x 1.2 x 2 x (drag factor x area) x speed^2, with speed in blocks per tick x 20. Drag cannot reverse a force.
- **Tick order** (authoritative side): inputs and state, base drag (velocity x 0.8, gravity skipped while flying), thrust and accelerations, `move`. Vertical acceleration adds ((net vertical force / mass) - 9.81) / 20 blocks per tick. The result is about 20 times larger than a real helicopter's, since force/mass is already in metres per second squared and is added to a blocks-per-tick velocity `[inf]`. The helicopter is arcade-fast. My simulation of the code constants gives: full-throttle climb about 19 blocks per second, top forward speed about 40 blocks per second at 15 degrees tilt (attitude lock), about 64 at 45 degrees. These are `[inf]`.
- **Controls** (per tick, only while flying): forward key adds power/2 to pitch (about 0.59 degrees), back key subtracts it. Left key adds power to roll, right key subtracts it. With the stabiliser on and the lock on, pitch and roll decay to zero when no key is held (snap to 0 below 1 degree). Tilt limit L: 15 degrees with the lock, 180 without. Within 3 blocks of the ground L scales with height/3 (times 15 with the lock, times 90 without). Roll clamps hard at L. Pitch is pushed back by power/2 per tick when it passes L.
- **Yaw**: arrow keys change a yaw rate by 0.1 degrees per tick when engine speed is above 10 and height above ground above 0.1 (stabiliser decays it by 0.05). The rate clamps at +-2 x power. Banking adds a turn rate (clamped +-3 degrees per tick).
- **Takeoff / landing**: Space sets the flying flag, switches gravity off and records the start position. Ground contact clears the flag. If |pitch| and |roll| are at most 30 degrees and the yaw rate is under 0.2, the lock flag turns on and pitch resets. Else damage jumps to 43 and the helicopter explodes (`:351-384`). A hard landing also adds floor(height fallen / 3) damage.
- **Rotor strike**: when the engine speed is at least 1 and the rotor part overlaps blocks, damage += (engine speed / max) x 2 per tick. Full speed in a wall kills in about 20 ticks (`:421-440`).
- **Water**: no flight. Engine speed drops to one eighth each tick.
- **Passengers**: the helicopter resets fall distance of every passenger each tick on the server (method `uzuj`).
- **Cost** (client): the downwash effect runs a 360-step loop per tick per helicopter within 6 blocks of the ground. Each step rolls a chance up to 20 percent (80 over water) times (engine/300)^3. The exhaust effect adds up to 5 particles per tick (`prulnm/vtclb.java:47-95`).

**Platform overlap.** Vanilla only. Fabric and NeoForge are the same.

**Verdict.** Defer. Aircraft are out of scope for Beta in B22. If an aircraft returns, the stabiliser, the attitude lock and the safe-landing rule are good player protection. The unit mismatch is a trap: write the model in blocks and ticks from the start.

## 9. Damage, repair, fall, water, destruction

**In game.** A vehicle has 40 health. Hits from players dent it, and the dent repairs itself after a pause. Fall damage hurts it. Deep water breaks it. A destroyed vehicle drops its item.

**Mechanism** (`rmeh/dnukfuguy.class`, methods `hurtServer`, `wtii`, `causeFallDamage`, `vtclb(ServerLevel)`, `destroy`) `[v]` except where marked.

- Health = 40 - damage. A tick with health below 0 destroys the vehicle (drops the item, plays nothing extra for cars; the helicopter adds an explosion particle and a sound at volume 4.0 and pitch 0.7).
- A hit from a rider is ignored. Two hits in the same game tick count only the excess over the larger one (a copy of living-entity rules).
- A hit from a player adds a "repair pending" value of 10 x amount `[inf]` and sets a 40-tick cooldown. After the cooldown, damage falls by 1 per tick until the pending value is gone. So slow hits heal. Fast hits break the vehicle.
- Fall damage: ceil((fall distance - 3) x factor) x 1.25. It destroys the vehicle if health drops below 0. The factor is not shown in my reading.
- Water: while in water, each living passenger takes 0.5 drowning damage per tick and the vehicle takes 0.25 damage per tick. The vehicle breaks after 160 ticks (8 s) `[inf]`.
- Save data: `Damage`, `RepairPending`, `RepairCooldown`, `Speed`, `Record`, `Suspension` (four smoothers), `Seats`.
- Record slot: right-click with a music disc while seated stores the disc in the vehicle and removes it from the hand (the item must have the jukebox-playable component). Destroy drops it. I found no code that plays it `[inf]`; playback may be in a file I did not read.

**Verdict.** Adapt. The plan defers damage stages (B22.10). The self-repairing dent is a cheap rule if the lead wants one.

## 10. Client: input, camera, HUD, sounds, particles, models

### 10.1 Input
`prulnm/jgdp.java` (client tick, post): reads the local input, builds the 5-bit flags (nothing while in water), reads the 3 extra helicopter keys, writes the flags to the entity for prediction, sends `vehicle_control` on change. Seat keys use `consumeClick` and send `vehicle_action` at most once per tick.

### 10.2 Camera
`prulnm/kodkieb.java`: third-person distance 4 to 24 blocks, step 1 per key press, start 4 (or 12 when config `helicopterZoomOut` is on). The distance resets when the player leaves the vehicle. The event applies to every vehicle.

### 10.3 Helicopter HUD
`prulnm/aeygzxj.java`: a layer above the hotbar with 4 round gauges, 64 pixels each: tachometer (two needles: current and hover target; 33 to 327 degrees sweep), artificial horizon (pitch fills, roll needle), altimeter (y and height above ground, 63 to 250 scale), compass. Plus two text lines for the stabiliser and the lock. The gauges draw at full size in first person and at half size in third person.

### 10.4 Sounds
`prulnm/kdhoqn.java`: every vehicle entity starts a looping "car_move" sound on join, at volume 0, and keeps it forever. Volume = (|wheel speed| + 0.001) / 2 for cars (max 0.5), engine/150 for the helicopter. A second loop, "helicopter_warning", plays only for the pilot, without attenuation, at volume 0.1 x (1 - health/40), when health is below 30 percent. Two `.ogg` files: 8.5 KB and 11.6 KB `[v]`.

### 10.5 Particles
Helicopter: downwash dust within 6 blocks of the ground (sand, soul sand, gravel add block particles; water adds splash), exhaust from two vents, and smoke plus fire below health 25 (fire chance 75 percent at 25, 100 percent at 20; bubbles and thin smoke underwater). Two own particle types: `helicopter_downwash`, `helicopter_exhaust` (`wkjvl/rqkeevhve.java:13-14`).

### 10.6 Models and render path
- Models are Tabula-style JSON (`assets/jurassicraft/models/vehicle/*.json`: 80 KB, 72 KB, 38 KB) loaded into vanilla `ModelPart` trees, not GeckoLib. The renderer finds bones by name: "wheel holder front", "wheel holder back", "steering wheel main", "door left main" and similar, "rotorbase_rotatehere", "tailrotor_rotatehere", "controlstick1", "gearL1", "gearR1", "gearFront1" (`prulnm/dnukfuguy.java:112-135`, `prulnm/gjtxsudo.java:70-80`).
- One texture and one render type per vehicle (cutout). The helicopter adds a second pass for position lights at full light level (15728880) when its blink phase is on.
- The render state holds yaw, pitch, roll, damage fraction, door angles, wheel angles and a list of tyre mark segments (`prulnm/onzkovhp.java`). The renderer builds the state on the extract step and draws on the submit step. This is the 26.x two-step render path.
- Tyre marks (`rmeh/omogwgxr.java`): each segment has a start, an end, a game time and a flag. A segment lives 200 ticks. Alpha = min(2 - 2 x (age/200)^2, 1) x 0.3. The client adds a segment per wheel when the wheel moved at least 0.05 blocks and the speed preset is not slow `[inf]`. The segments fade fully in 10 s.

**Platform overlap.** All vanilla 26.x client API plus NeoForge events (`CalculateDetachedCameraDistanceEvent`, `RegisterGuiLayersEvent`, `ClientTickEvent`).

**Verdict.**

- Keep the plan's GeckoLib models. Do not copy the Tabula loader.
- Camera zoom with a clamp and a reset: adopt (size S).
- Looping sound per vehicle at volume 0: avoid. Start the loop only while the engine runs.
- Tyre marks: not needed.

## 11. Machine framework

**In game.** Each lab block opens a screen. The screen shows slots, a progress arrow and a status line. The player can break a machine and get every item back. A spectator can open a machine screen with a right-click.

**Mechanism.**

- **Machine block** (`ufpurxw/fgwme.java`): abstract `BaseEntityBlock`, waterloggable. State: facing and waterlogged. It asks its block entity type for a ticker, but only on the server and only for the recipe-machine base. Right-click opens the menu through `MenuProvider` and writes the position (plus two extras for the lab machine) to the open packet. It exposes a comparator signal. Spectators can open screens through a right-click handler that forwards the click (`ufpurxw/cvrd.java:15-35`).
- **Block entity base** (`dnukfuguy/onzkovhp.java`): holds a layout, a slot handler and a wrapper handler for outside access. It saves under "Inventory". It drops all items on removal. Any slot change calls `setChanged`, updates the comparator and records a "chunk dirty" mark for the feeding code (section 18).
- **Recipe machine base** (`dnukfuguy/kdhoqn.java`): one or more parallel processes. Each process holds progress, total time, status and the active recipe id. Save data: `Progress`, `TotalTime`, `ActiveRecipe`, plus `Status` in the update tag. Hooks for subclasses: a pre-tick step (the cultivator absorbs water and nutrients; the cleaning station refills from a bucket), a can-run test (cleaning station: water above 0; cultivator: water at least 2 and every nutrient at least 1500), and a per-running-tick step (cleaning station drains 1 water). A flag per process can send the output straight to one slot.
- **Layout record** (`elygmogfk/dnukfuguy.java`): name, width (default 176), height (default 166), a slot list, an optional player-inventory block. A slot record has index, x, y, role. 9 roles: input, output, fluid, storage, environment, crafting, result, player, hotbar. Slot size 18 pixels. The player block draws 3 rows plus a hotbar.
- **15 layouts** (`elygmogfk/vtclb.java`) `[v]`: fossil grinder (6 in, 6 out), bug crate (6 in, 3 out), cleaning station (1 in, 1 fluid, 6 out), DNA extractor (1 in, 1 storage, 4 out), DNA sequencer (3 in, 3 storage, 3 out), DNA synthesizer (3 in, 4 out), DNA combinator (10 storage/in, 2 out), DNA hybridizer (8 storage, 1 out), embryonic machine (3 in, 4 out), embryo calcification machine (2 in, 1 out), incubator (5 egg slots, 1 environment), feeder (18 in), genome editor (2 storage, 300 x 250 screen), cultivator (2 in, 1 fluid, 1 out; 352 x 188 screen), skeleton assembly (25 crafting + 1 result; 176 x 201 screen).
- **Slot spec** (`lbte/aeygzxj.java`): index, role, accepted-item test, capacity (99), "outside insert" flag, "outside extract" flag. Output and result roles are extract-only. The slot handler (`lbte/vtclb.java`) checks that spec number i matches slot i at construction. It resizes a loaded inventory with a warning if the slot count changed.
- **Status enum** (`dnukfuguy/kijk.java`): IDLE, RUNNING, OUTPUT_BLOCKED, MISSING_RESOURCE, UNUSABLE_INPUT. The lang key is `gui.jurassicraft.status.<name>`.

**Platform overlap.** Vanilla `MenuProvider`, `BaseEntityBlock`, `ContainerData`. NeoForge 26.x `ItemStacksResourceHandler`. Nublar's plan uses Balm and a master-block multiblock for B12. JurassiCraft 3 has single-block machines (no parts).

**Verdict.**

- Slot spec plus layout record as data: adapt. It removes the screen-coordinate duplication and feeds JEI, Jade and a preview command (size M).
- A 5-value status enum: the plan's issue bitmask is richer. Keep the mask. Map the 5 values to mask bits for the Jade line.
- Single-block machines: not applicable. B12 uses assembled multiblocks.

## 12. Recipes, timing and the individual machines

### 12.1 Recipe format
`vtrqoo/onzkovhp.java` (base), `vtrqoo/jgdp.java` (lab recipe), `vtrqoo/dbgouk.java` (ingredient), `vtrqoo/vtclb.java` (result) `[v]`:

- 1 to 9 ingredients. Each has a count (default 1) and a "consumed" flag (default true).
- 1 to 16 results. Each has a weight (default 1) and an item template. A weighted pick chooses one at the end of the job.
- `processing_time`: 1 to 32767 ticks, default 200.
- Ingredients match any slot (back-tracking assignment). The recipe input is a snapshot list of the input slots' stacks (`vtrqoo/kijk.java`).
- Recipe types: grinding, cleaning, bug breeding, and one type per lab machine (8: extractor, sequencer, synthesizer, combinator, embryonic machine, embryo calcification, incubator, cultivator). The lab types share one serializer. A lab recipe has a `machine` field and a mode: carry, sequence, extract amber, extract marine, combine, incubate, cultivate or hybridize.
- The bug crate picks the recipe with the largest output count when several match (`dnukfuguy/dnukfuguy.java:35-47`).

### 12.2 Why the cap is 32767
The cap equals the largest signed 16-bit value. `ContainerData` values travel as 16-bit shorts in the vanilla data-slot packet, so a larger time would wrap on the client `[inf]`. B12 should state this cap in its recipe schema.

### 12.3 Processing times in the shipped data `[v]` (`raw/data/jurassicraft/recipe/`, counts and times from a script over all non-vanilla recipe JSON)

| Machine / recipe group | Files | Ticks | Seconds |
| --- | --- | --- | --- |
| Fossil grinder (`grinding`, `grinding/bone`) | 10 + 48 | 200 | 10 |
| Cleaning station (`cleaning`) | 5 | 200 | 10 |
| Embryo calcification (`embryo`) | 3 | 200 | 10 |
| DNA extractor (`extraction`) | 4 | 500 | 25 |
| DNA sequencer (`sequencing`) | 2 | 500 | 25 |
| DNA synthesizer (`synthesis`) | 2 | 750 | 37.5 |
| DNA combinator (`combining`) | 2 | 750 | 37.5 |
| DNA hybridizer (`hybridizing`) | 1 | 750 | 37.5 |
| Cultivator (`cultivation`) | 1 | 2000 | 100 |
| Incubator (`incubation`, default; species can override) | 1 | 3600 | 180 |
| Bug crate (`bug_breeding`) | 31 | 400 to 3600 (16 at 400, 8 at 800, 2 at 1200, 3 at 2000, 2 at 3600) | 20 to 180 |

Other custom recipes: one `species_smelting` recipe (200 ticks, 5 XP) and one `potion_dart` recipe. The recipe folder holds 478 files in all; 350 are shaped crafting and 86 are shapeless.

### 12.4 Machines

- **Fossil grinder** (`dnukfuguy/kodkieb.java`): 6 input slots (a tag filter), 6 outputs. Some outputs get a random species written to them after the weighted pick `[inf]`. A spin animation helper (`dnukfuguy/fgwme.java`) accelerates while running (add 1.3, then x 1.1) and brakes when idle (subtract 0.020526, then x 0.9025), clamped to 22.5 degrees per tick. The client reads the interpolated angle.
- **Cleaning station** (`dnukfuguy/vtclb.java`): 1 input, 1 fluid slot, 6 outputs. A water bucket in the fluid slot becomes an empty bucket and fills a tank of 1600 ticks (80 s) of work. The tank drains 1 per running tick. Needs water to run. A client particle spray runs while it works.
- **Lab machine** (`dnukfuguy/dbgouk.java`): one block entity class for 8 machine kinds, chosen by the block. The incubator keeps 5 egg temperatures (0 to 100, steps of 5, default 50) and uses the species' own incubation time. The cultivator keeps a water level (0 to 2, one water bucket adds 1) and six nutrient levels (0 to 3000 each). One job needs water of at least 2 and every nutrient of at least 1500. A job takes 1500 of each nutrient and 1 water level at the end. The combinator has a hybridizer mode (a toggle that swaps slot sets).
- **Feeder** (`dnukfuguy/ummp.java`): see section 18.
- **Genome editor** (`dnukfuguy/jgdp.java`): 2 storage slots, a preview of a creature with body and pattern choices. Not a timed machine.
- **Skeleton assembly** (`dnukfuguy/omogwgxr.java`): a 5 x 5 crafting grid with a live result slot. The result recomputes on any grid change, with a re-entrancy guard.

### 12.5 One more fact
The DNA machine can return Idle because of a check from a file outside this study's scope. I did not read it (`dnukfuguy/dbgouk.java:~232-236`).

**Verdict.** Adapt the time table as a starting point for B12 and B13 balance. Adopt the weighted-output recipe shape (it fits bone grinding). Adopt the 32767 cap. Skip the bug-crate recipe list.

## 13. Ticking, idle behaviour and sync

**In game.** A running machine animates. A stopped machine goes still. Many machines in a lab do not lag.

**Mechanism.**

- **Ticker.** The block returns a ticker only on a `ServerLevel`. The ticker calls the recipe-machine tick (`ufpurxw/fgwme.java:~70-86`). There is no sleep state. Every machine runs its tick every game tick `[v]`.
- **Tick body** (`dnukfuguy/kdhoqn.java:222-260`): for each process, check whether any input slot has items. If none, reset the process (progress 0, total 0, no recipe, status IDLE). If some, ask the cached recipe check for a match (`RecipeManager.CachedCheck`). No match calls the "no recipe" hook (usually a reset). A match sets the total time, picks the output, checks that the output fits, and then either sets a blocked or missing status or adds 1 to progress. At progress = total, it delivers the output, consumes the flagged inputs and resets progress.
- **Allocations**: the recipe input is rebuilt as a list of stack copies on each tick that has input. This is a small per-tick allocation `[inf]`.
- **Sync**:
  - Progress and total travel in `ContainerData` (3 ints per process, plus extras) to an open menu only (`snqzaxu/kijk.java`). Vanilla sends only changed data slots.
  - The block update tag has the inventory and the status ints (`saveCustomOnly`). The block sends a block update only when a status changes, or when a lab slot changes in a state that alters the model (`dnukfuguy/onzkovhp.java:62-69, 100-107`; `dnukfuguy/dbgouk.java` hook `a_`).
  - Progress changes call `setChanged` only. They send no packet.
  - So a running machine sends 0 packets per second on average. A status change sends one full inventory tag.
- **Comparator**: output from how full the slots are (`ResourceHandlerUtil.getRedstoneSignalFromResourceHandler`, `dnukfuguy/onzkovhp.java:110-112`).
- **Render**: lab blocks draw with a block entity renderer (view distance 48 blocks) that picks a model variant from a state enum (`default`, `petri dish`, `test tubes`, `both`, `egg`, `hybridizer`) and swaps the texture while the machine runs (`omogwgxr/kijk.java:68-100`).
- **Spectators** can open machine screens (section 11).

**Platform overlap.** Vanilla `BlockEntityTicker`, `ContainerData`, `RecipeManager.CachedCheck`.

**Verdict.** Adopt the "update only on status edge" rule. B12 already asks for it and for the 60-tick active tail. Keep the `ACTIVE` blockstate flip from the plan. Add an input-empty early-out before the recipe check to cut the per-tick allocation.

## 14. Idle cost

The server ticks every machine every tick. An empty machine does a few slot reads and a reset. The cultivator also runs a pre-tick hook every tick that scans its slots. For 100 idle machines this is small, but it is not zero `[inf]`. The plan's B12 target ("idle machine costs no ticks") needs a sleep or a "nothing to do" flag that wakes on a slot change. JurassiCraft 3 does not have one.

## 15. Power: none

I searched the machine, fence, feeder, vehicle and handler packages for energy capabilities. There are none `[v]`. The only "energy" field is the food energy of an item in the feeding code (section 18). Machines have no draw, no tier and no fault from power. The fence uses redstone (section 17). B14 has no counterpart here. Nothing to copy.

## 16. Automation: capabilities and per-side rules

**In game.** A hopper under a machine pulls finished items. A hopper above a machine pushes ingredients into input slots. A chest with a hopper cannot put items into an output slot.

**Mechanism.**

- Six machine block entity types register an item capability (`wkjvl/fgwme.java:119-124` `[v]`): fossil grinder, cleaning station, bug crate, lab machine, feeder, genome editor. The provider ignores the side argument. Every side gets the same handler. There are no per-side rules.
- The outside handler (`lbte/dnukfuguy.java`) wraps the slot handler. Insert: the slot's "outside insert" flag must be on, a per-machine predicate must pass (the machine can switch slot groups, for example the combinator hybridizer mode), and the item test must pass. Insert tries each slot in order. Extract: the slot's "outside extract" flag must be on (outputs only). Extract tries each slot in order.
- Per-machine item filters: `ufpurxw/hmocea.java:45-130` maps each lab machine to input and output slot lists per process and to item tests (for example the sequencer: even inputs take sequencing items, odd inputs take storage discs; the incubator: 5 egg slots plus 1 environment slot).
- No fluid capability. The cleaning station and cultivator take water only as a water bucket item in the fluid slot. The fluid slot capacity is 1 item.
- The skeleton assembly block has no capability.

**Platform overlap.** NeoForge 26.x item transfer (`ResourceHandler<ItemResource>`). Fabric Transfer API has `Storage<ItemVariant>`. The plan (P2) uses Balm for energy storage; check that Balm wraps item handlers on 26.x.

**Verdict.** Adapt. The delegating wrapper with role flags is the right shape. B12 wants per-side rules (section "Pipes and cables"): add a side mask to the slot spec. Skip the water-bucket trick if B12 uses a real fluid tank.

## 17. Fences (low and medium security)

**In game.** Fences come in two tiers: low security and medium security. A fence is a base block, optional poles on top, and wire blocks. A redstone signal on a base electrifies the connected fence within 8 blocks. A live wire shocks any living thing that touches it. Players can climb a wire. A dinosaur that cannot climb sees the base as 1.5 blocks high.

**Mechanism** (`qkek/*`, 985 lines) `[v]`.

- **Tiers** (`qkek/kodkieb.java`): LOW shock 1.0, hardness base 2.0, pole 3.0, wire 3.5. MED shock 2.0, hardness base 3.0, pole 4.0, wire 4.5. The block codec takes the tier name. The tier is part of the block class, not of the block state.
- **Base block** (`qkek/dnukfuguy.java`): state = facing, north/east/south/west connection flags, "pole" (a pole sits above), "connections" count 0 to 4, waterlogged. It connects to a same-tier base on a neighbouring side. Collision depends on the entity: a dinosaur without climbing trait that has a non-air block above gets a 1.5-high box. Everyone else gets a full block.
- **Pole block** (`qkek/vtclb.java`): thin post (5 x 5 pixels wide, 16 high), four connection flags, `powered`. It copies power from the base column below.
- **Wire block** (`qkek/aeygzxj.java`): thin post with arms toward same-tier fence, `up` (down, or the side toward which the wire climbs), `powered`. The collision shape equals the visual shape for entities and is empty for non-entity queries. A wire is a ladder for any player and for dinosaurs whose species allows climbing. `entityInside`: on a powered wire, a living entity takes the tier's shock damage every tick (invulnerability frames limit it), a dinosaur gets an "electrocuted" reaction, and a sound plays every 10 ticks at volume 0.25.
- **Power rule** (`qkek/ummp.java`):
  - A base is a source when its column has a pole of the same tier and the base block has a redstone neighbour signal (`hasNeighborSignal`).
  - On any neighbour change of a base, pole or wire, the code floods all connected bases within 16 blocks (horizontal radius, 3 height steps each way per hop) around the changed base. For each base within 8 blocks, it updates the pole column above and the wires above it (a gap of up to 5 blocks is allowed in a column).
  - A wire is live when a breadth-first search from its base (limited to 8 blocks from the changed base, one block per hop with plus or minus 1 height) reaches a source base.
  - Results cache per base in one pass.
- **Cost** `[inf]`: each neighbour update of a fence part runs two breadth-first searches over up to about 33 x 33 blocks of fence. Placing a long fence line fires one flood per block. A big paddock will spike. There is no cap and no debounce.
- **Not found in this package**: wire spools, spans, cut wires, voltage signs, a fence remover, a power meter, a high-security tier. I did not search the rest of the mod for them.
- Recipes: `low_security_fence_{base,pole,wire}`, `med_security_fence_{base,pole,wire}`, 3 files each, base gives 6 per craft in the low tier (`recipe/low_security_fence_base.json`).

**Platform overlap.** Vanilla block states, `hasNeighborSignal`, ladder hook (NeoForge `IBlockExtension.isLadder`; Fabric has none).

**Verdict.**

- The B15 parity baseline stays. This fence is a different design (no spool, no span, redstone power). Do not copy it.
- Take from it: a tier record (shock, three hardness values), entity-aware collision (1.5-high base for non-climbers), and per-entity ladder rules for climbing species. All size S.
- Do not copy the unbounded flood on every neighbour update. B14's network object with one rebuild per tick is the safer design.

## 18. Feeders and feeding support

**In game.** A dinosaur walks to a feeder. The lid opens. One item pops out toward the dinosaur. The lid closes. The feeder holds 18 stacks of up to 99. The first 9 slots take meat, fish, insect, egg and filter-feeder food. The other 9 take plants.

**Mechanism.**

- **Feeder block entity** (`dnukfuguy/ummp.java`): state = open flag, lid animation counter 0 to 20, hold counter, reserving dinosaur UUID (reservation lasts 100 ticks), eject cooldown 20 ticks. A dinosaur calls `reserve(level, dino)`. It succeeds when no one has reserved the feeder, the lid is closed and a matching item exists. The feeder opens (plays a sound). When the lid is fully open the hold counter starts at 20. At 10 it ejects one item with a small velocity (0.3 x direction) and sets the cooldown. At 0 it closes. Reservation clears when the dinosaur dies, moves on or times out. The whole visit is about 60 ticks (3 s) `[v]`.
- **Block**: directional (6 facings, the clicked face), its own ticker (`ufpurxw/kodkieb.java`).
- **Registry** (`ufpurxw/dbgouk.java`): per-level map from chunk to feeder positions. Feeders join on load and leave on removal. Chunk unload drops the chunk's set. A lookup returns positions within a radius, sorted by distance, only for loaded chunks.
- **Food energy tiers** (`nkjvgug/gjtxsudo.java`) `[v]`: FREE 300, FARMED 750, EFFORT 1500, PREMIUM 3000, BALE 13500. Items and blocks map to a tier through tags. Default for an untagged item is 750; for an untagged block, 300.
- **Diet filters** (`nkjvgug/aeygzxj.java`): a diet enum (meat, fish, plant, insect, filter, egg) maps to item tags. Plant food includes blocks tagged as plant candidates.
- **Dirty-chunk food cache** (`nkjvgug/jgdp.java`): a per-dimension map from chunk to the last game time a block was placed or broken or an item entity joined. The entry lives 1200 ticks (60 s). When the map holds more than 512 chunks, it prunes old ones. A food search asks "did anything change within r chunks since time t?" and skips the search if not.
- **Ring search** (`nkjvgug/dnukfuguy.java`): an expanding cube search around the dinosaur with a cap on candidates tested and an optional section pre-filter that dilates by a margin. It returns the first hit or null when the cap is reached.

**Platform overlap.** Vanilla chunk events and block events. NeoForge `BlockEvent.EntityPlaceEvent`. Fabric has equivalent callbacks.

**Verdict.**

- Per-chunk feeder registry: adopt (B20 already owns one; the sorted radius lookup is the useful part).
- Dirty-chunk cache with a 60 s life: adopt for B17 and B20 search cost.
- Item ejection with a lid animation: nice visual, but the plan uses a pool of points per diet. Choose one in the B20 design.
- Food energy tiers as a five-step table from tags: adapt as the B20 "points" source. Numbers need balance work.

## 19. JEI and Jade

**In game.** Hovering a machine in Jade shows its status and percentage. JEI lists each machine's recipes and lets the player fill a machine's slots from the recipe screen.

**Mechanism.**

- **Jade** (`cvrd/aeygzxj.java`): plugin with two providers. `machine_status`: for any recipe machine, one line = status text, then the percentage of the busiest process. Non-idle only adds the percentage (`dnukfuguy/kdhoqn.java:~395-410`). `display_block_name`: replaces the object name with the stack's hover name for blocks whose picked item carries species data.
- **JEI** (`cvrd/gjtxsudo.java`, 166 lines): category per recipe machine from the layout (`cvrd/kodkieb.java`), custom categories for moss spreading (`cvrd/dbgouk.java`) and two others, recipe-transfer handlers for the fossil grinder (inputs 0 to 5), the cleaning station (input 0) and the skeleton assembly grid (slots 1 to 25), and a custom handler for the lab machines. Item subtypes (species aware). Info pages on many items (pregnancy test, moss, peat, cultivator and so on). Catalysts. Synthetic crafting recipes for potion darts built from the potion registry (`cvrd/kijk.java`).
- **Recipe sync** (`cvrd/dnukfuguy.java`): a client handler stores the `RecipeMap` on `RecipesReceivedEvent` so JEI can read recipes on 26.x. The event runs at the highest priority.
- Size: the Jade plugin is 66 lines. The JEI code is about 600 lines across 11 files (`cvrd/`).

**Platform overlap.** Jade and JEI both publish NeoForge and Fabric builds. On 26.x the recipe map is not a full list on the client, so mods store it on `RecipesReceivedEvent` (NeoForge) or an equivalent Fabric event `[inf]`.

**Verdict.** Adopt both. Use the status line pattern for Nublar's hover box (B12 already plans a hover box; Jade gives it for free). The recipe-map capture is a P2 fact to remember.

## 20. Dev tooling in the mod

- `/jcmachine` (`ufpurxw/rqkeevhve.java`): info, flood, feed, clear, rig (fossil grinder, bug crate, skeleton with species and fresh flag, bones, discs, kit) and `turbo on|off`. Turbo makes every job take 1 second and is not saved.
- `/jcgui` (`gjtxsudo/dnukfuguy.java`): list previewable screens, open a preview screen on the client, print a layout's size, slot count, progress regions, tanks, sliders and labels.
- `/jcmodel` (`gjtxsudo/vtclb.java`): bones, validate and animation state for the nearest creature within 32 blocks.
- Config switches for checks: `partIndexVerify` and `pathColumnVerify` compare a fast path against the vanilla scan and log differences (default off, `dnukfuguy.java`).

**Verdict.** Adopt `turbo` and `/jcgui` ideas. They shrink balance and screen-layout work.

## 21. Packet layer summary (`znpnvzxz/`)

| Name | Direction | Fields | Check on server | Rate |
| --- | --- | --- | --- | --- |
| `vehicle_control` | to server | varint id, byte | non-spectator, controlling passenger | on key edge |
| `vehicle_action` | to server | varint id, enum, varint seat | rides that vehicle; toggles need seat 0 | on key press |
| `set_order` | to server | varint id, order enum | owner, 8 blocks | on click |
| `place_paddock_sign` | to server | pos, face, hand, id | 8 blocks, held item, may-use, registry key | on click |
| `dismount_riders` | to server | none | stops riders on the sender | on key press |
| `open_paleo_pad` | to client | 13 fields | server decides | on item use |
| `open_order_screen` | to client | varint id, enum | server decides | on click |
| `open_paddock_sign` | to client | pos, face, hand | server decides | on click |
| `growth_clock` | to client | long | server decides | sent by the server, trigger not traced |

Total: 9 payload types. No batching. No rate limit on any serverbound payload. Machine data travels in vanilla block-entity updates and menu data packets, not in custom payloads.

## 22. Project Nublar fit

| Technique | Source here | Plan modules | Verdict | Reason |
| --- | --- | --- | --- | --- |
| Vanilla input for keys + one small packet for extra keys, sent on change, sender must be the controlling passenger | `znpnvzxz/kdhoqn.java`, `prulnm/jgdp.java` | B22 | Adopt | Matches the plan's one checked packet. Lowest traffic. |
| Add a speed and distance clamp for driver-reported motion | missing here (cars) | B22 | Adopt (own work) | JurassiCraft 3 has none. The plan already requires it. |
| Car model: drag 0.8, push per tick, yaw rate cap 3 degrees, 3 presets | `rmeh/dnukfuguy.class` | B22 | Adapt | Simple and stable. Retune numbers for the park. |
| Four edge probes, staggered, client-side tilt with smoothing | `rmeh/dnukfuguy.class` | B22 | Adapt | Same as the earlier note's client tilt. Move it to render time. |
| Step height 1.5 | `rmeh/dnukfuguy.class` | B22 | Adapt | Plan says 1.0 to 1.25. Use as the upper bound. |
| 3 interpolation steps, update interval 1, tracking 12 | `wkjvl/cvrd.java` | B22, T15 | Adapt | Plan: 3 ticks, range 10. Test both. |
| Seats as synced entity ids per seat + seat memory by UUID | `rmeh/dnukfuguy.class` | B22 | Adapt | Needed for seat keys and reload. Smaller if only 2 to 4 seats. |
| Door animation by nearest player within 4 blocks | `rmeh/dnukfuguy.class` | B22, B24 | Defer | Polish. |
| Hitbox parts (bonnet, boot, rotor) | `rmeh/fgwme.java` | B22, B26 | Drop for Beta | One box fits. Parts add entities and index work. |
| Self-repairing dent from player hits | `rmeh/dnukfuguy.class` | B22 | Defer | Plan defers damage. |
| Tour rail with 20 shapes and tiers | `rmeh/dbgouk.java` | B22 | Drop (shapes), Defer (tiers) | Plan uses vanilla `RailShape`. Tier recipes are a later idea. |
| Backward key reverses once on rail | `rmeh/aeygzxj.java` | B22 | Adopt | Tiny. Useful for a hand-driven track. |
| Tour stops, horn, blocked check, loop check, announcements | not present | B22 | None (own work) | Nothing to copy. |
| Helicopter flight model, stabiliser, attitude lock, 4-gauge HUD | `rmeh/gjtxsudo.java`, `prulnm/aeygzxj.java` | out of Beta | Defer | Aircraft are out of scope. Keep as a reference. |
| Camera distance keys with clamp and reset | `prulnm/kodkieb.java` | B22 | Adopt | Size S. |
| Looping engine sound at volume 0 per vehicle | `prulnm/kdhoqn.java` | B22 | Drop | Wastes a sound instance per parked vehicle. |
| Slot spec + layout record as data | `lbte/aeygzxj.java`, `elygmogfk/*` | B12 | Adapt | One source for menu, JEI, Jade and preview. |
| Weighted multi-result recipes with `processing_time` 1 to 32767 | `vtrqoo/*` | B12, B13 | Adopt | Fits grinder outputs. Cap matches 16-bit sync. |
| Status enum (5 values) | `dnukfuguy/kijk.java` | B12 | Adapt | Keep the plan's issue mask. Map for Jade. |
| Block update only on status edge; progress by menu data | `dnukfuguy/kdhoqn.java` | B12 | Adopt | Already planned. Verified working in 26.x. |
| Early-out when no input | `dnukfuguy/kdhoqn.java` | B12 | Adopt | Cheap. Add an "idle sleep" flag on top. |
| Item capability, one handler for all sides, role flags | `lbte/dnukfuguy.java`, `wkjvl/fgwme.java` | B12, B20 | Adapt | Add a side mask for per-side rules. |
| Water as a bucket item in a slot | `dnukfuguy/vtclb.java` | B12 | Drop | B12 wants real fluid capability. |
| No power at all | whole mod | B14 | None | Nothing to copy. |
| Redstone-driven fence power with flood fill | `qkek/ummp.java` | B15, B14 | Drop | Conflicts with B14 networks. Flood has no cap. |
| Tier record (shock, three hardness values) | `qkek/kodkieb.java` | B15 | Adopt | Size S. |
| Entity-aware collision and ladder rules | `qkek/dnukfuguy.java`, `qkek/aeygzxj.java` | B15 | Adapt | Fits dinosaur climbers. Check against 1.12.2 parity. |
| Per-chunk feeder registry | `ufpurxw/dbgouk.java` | B20 | Adopt | B20 already owns one. |
| Dirty-chunk food cache (60 s life, 512-chunk prune) | `nkjvgug/jgdp.java` | B17, B20 | Adopt | Cuts search cost. |
| Lid animation, reservation, one item per visit | `dnukfuguy/ummp.java` | B20 | Adapt | Visual; plan uses points. Decide in B20 design. |
| Food energy tiers 300/750/1500/3000/13500 | `nkjvgug/gjtxsudo.java` | B20 | Adapt | Starting scale for diet points. |
| Jade `machine_status` provider | `cvrd/aeygzxj.java` | B12 | Adopt | 66 lines. |
| JEI plugin from layouts, transfer handlers, info pages | `cvrd/gjtxsudo.java` | B12, B13 | Adopt | Size M. |
| Recipe map captured on `RecipesReceivedEvent` | `cvrd/dnukfuguy.java` | P2 | Adopt | Needed for JEI on 26.x. |
| `/jcmachine turbo` and `/jcgui` | `ufpurxw/rqkeevhve.java`, `gjtxsudo/dnukfuguy.java` | B12 | Adopt | Dev speed. |

## 23. Open questions (real gaps)

1. **Car authority.** I inferred client authority for cars from missing overrides. A run with two clients would confirm it. If cars are server-authoritative after all, the 3-step smoothing at update interval 1 shows more latency.
2. **Fall damage factor** and the exact **hit repair rule** use values that my bytecode reading lost (a loaded constant). Confirm in game before copying.
3. **Record slot.** No playback code was found. Ask whether the lead wants a car radio.
4. **Tyre marks** (`prulnm/ummp.class`) and the **machine menu base** (`snqzaxu/onzkovhp.class`) did not decompile. Quick-move rules and the mark draw path are unknown.
5. **Two files** (`vtrqoo/gjtxsudo.java`, `ufpurxw/omogwgxr.java`) stay unread by rule.
6. **Idle sleep.** B12 wants it. JurassiCraft 3 shows the baseline without it. Decide the wake rule (slot change, neighbour change, redstone) in the B12 task plan.
