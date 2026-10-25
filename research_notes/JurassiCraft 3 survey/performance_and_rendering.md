# JurassiCraft 3.0.14 (NeoForge 26.1.2): performance, multipart hitboxes, rendering and server cost

Scope: the optimisation, multipart-entity, rendering, animation and server-cost parts of JurassiCraft 3.0.14 for NeoForge 26.1.2 (CAZ LLC, paid, proprietary). Read for Project Nublar modules B09 (appearance and rendering), B16 (dinosaur core), B25 (performance pass), B26 (large-body movement), B27 (section hitboxes and IK) and P2 (the port to 26.2). Licensing, entitlement, integrity and anti-tamper code was not read. The packages azaxjn, bjkox, elxqepf, kdhoqn, kijk, qkqaptnz, zkyafrvu, pufpxsnoo, lpmpb, iuwzxpow, ohfgova, qjliai, wtii and lyhmwcjw, `raw/jurassicraft/` and the licence files were skipped. Only registration lines of the two root classes were read.

Labels: `[v]` verified in the decompiled source or in `javap` output. `[inf]` my inference. `[doc]` a comment in `neoforge.mods.toml`, a config comment, a shader comment or a log string inside the jar. Every performance number that is not `[v]` is an estimate.

Clean room: this note describes behaviour, state, steps, numbers and timings. It copies no code. Class names are obfuscated. Each class gets a descriptive name that I inferred, followed by its path under `scratchpad/jc/src/caz/jurassicraft/`. Where the decompiler failed ("Couldn't be decompiled"), I read the method with `javap`. Those cases say "javap". Two package names appear twice (for example `ctygd/kdhoqn` the class and `kdhoqn/` the skipped package). The skipped package was not opened.

Method note: static reading only. Nothing was built or run. Asset counts come from file listings of the jar contents. Texture memory figures come from PNG header sizes and assume RGBA8 with no mipmaps. Earlier Nublar surveys used the same labels. See `reports/Reference mod survey for Beta.md` and `reports/Optimization and tech mod survey for Beta.md`.

---

## 0. Sources read

| Item | Path (under `scratchpad/jc/`) | What it is |
| --- | --- | --- |
| Mixin configs | `raw/jurassicraft.mixins.json`, `raw/jurassicraft.client.mixins.json` | Two configs. The first is `required: true` with two mixins. The second is `required: false` with two client mixins |
| Mod metadata | `raw/META-INF/neoforge.mods.toml` | Design comments on the mixins. They cite B110, B146, B147 and L15 |
| Enum extension | `raw/META-INF/enumextensions.json`, `src/.../core/JCMobCategories.java` | The own `MobCategory` |
| Config spec | `src/.../dnukfuguy.java` (root) | All three config files (client, common, server) |
| Part spatial index | `src/.../ctygd/kdhoqn` (javap, no source) | Per-level section buckets for parts |
| Part base and part | `ctygd/dbgouk.java`, `ctygd/vtclb.java` | Abstract part, creature part |
| Creature base entity | `ctygd/omogwgxr.java` (1,893 lines) | One class for all 129 species. Land subclass `ctygd/gjtxsudo.java`, water subclass `ctygd/kijk.java` |
| Creature renderer | `stta/kdhoqn` (javap, no source), `stta/kijk.java` (render state) | Entity renderer and its state |
| Colour system | `stta/ummp.java`, `stta/omogwgxr.java`, `stta/gjtxsudo.java`, `stta/jgdp.java`, `stta/onzkovhp.java`, `stta/wkjvl.java`, `stta/dbgouk.java`, `raw/assets/jurassicraft/shaders/core/creature_color.{vsh,fsh}` | Pass pipeline, tint table, layer list, species render definition, texture set, overlay layer, shader |
| Models | `hmocea/vtclb.java`, `hmocea/dnukfuguy.java`, `fgwme/*.java`, `omogwgxr/omogwgxr.java`, `rtwzimpls/*.java` | Geometry JSON loader, Tabula model loader, machine model loader, creature models and overlay models |
| Animation | `aeygzxj/*.java`, `octtcpmd/*.java` | Keyframe wrapper, procedural layers, server animation state machine |
| Block-entity renderers | `omogwgxr/*.java`, `stta/hmocea.java`, `stta/wtii.java`, `stta/incf.java` | Machines, hologram, skeleton and skull displays |
| Particles | `rqkeevhve/*.java`, `raw/assets/jurassicraft/particles/` | Three particle types |
| Navigation | `incf/*.java` | Ground navigation, node evaluator, move control |
| AI goals | `ztiurxyn/*.java` (31 files) | Vanilla-style goals |
| Needs | `gyovxqy/*.java` | Energy, water, breeding timers |
| Registrations | `core/vtclb.java`, `axvg/*.java`, `jdbttmo/*.java`, `znpnvzxz/*.java` | Entity types, spawn, biome modifiers, features, packets |
| Data | `raw/data/jurassicraft/jurassicraft/species/*.json` (129 files) | Species definitions, incl. hitboxes |
| Assets | `raw/assets/jurassicraft/{textures,models,jurassicraft,neoforge,sounds,particles,shaders}` | Counted in section 12 |

Scratch files I made, outside the repo: `kdhoqn_ctygd.txt`, `ctygd_omogwgxr.txt`, `rt_vtclb.txt`, `rt_dbgouk.txt`, `stta_kdhoqn.txt` (javap output), plus small Python statistics scripts.

## What to take from this note (top findings)

1. **Part spatial index (B110).** One section-bucket map per level. Buckets are 16-block sections. A query pads the box by 4 blocks sideways and 12 blocks down, and falls back to the flat list above 1,024 sections. A count cross-check rebuilds the index when it disagrees with the flat list. A foreign part (for example an Ender Dragon part) switches the index off. Two dev switches exist: a verifier and a "break the index" system property that proves the verifier works. This is the design Nublar's hitbox spec already accepts. JC confirms it works at 129 species. See section 1.1 and 2.
2. **Hitboxes are data, per pose.** Each species file lists boxes for the default pose and for five other poses (carcass, sleep, drinking, eating, resting). Part count per creature is 3 to 20 (median 7, mean 8.0, total 1,038 over 129 species). Boxes blend between pose lists over a per-species tick count with a smoothstep curve. Only the head pivots, and only by yaw and pitch. Nublar plans 11 to 17 parts per species, so it will cost about twice as much per creature. See section 2.
3. **One shader pass for the four body colours.** A packed pattern sheet stores three pattern layers in one alpha channel with four exact levels (0, 85, 170, 255). A 4 x 2,048 per-frame "tint table" texture (32 KB) holds each visible creature's four colours. The vertex colour carries only the row number. The fragment shader reads the layer from the alpha, then reads one tint. No per-genome texture exists. The cost is a custom core shader, which Nublar rule OC6 forbids. See section 5.
4. **Overlay passes only draw bones that have pixels.** An async scan of each overlay PNG finds which bones' UV boxes hold opaque pixels. Other bones are hidden for that pass. Bones that only carry visible children skip their own draw. A median species has 5 overlay sheets per sex. A median creature therefore submits about 6 passes, against 23 for Nublar's Gallimimus today. See section 5.4.
5. **Pose replay.** The body pass computes the bone pose once and stores 9 floats per bone in the render state. Overlay passes replay it. A dev switch compares replay to a fresh evaluation. See section 6.4.
6. **No animation level of detail.** JC has no distance rule for animation or colour layers. I searched every render and animation package for distance and camera use and found none. B09's "hold the last pose beyond 32 blocks" has no JC precedent. See section 6.7.
7. **Animation is vanilla keyframes with server-owned state.** 1,780 JSON clips (26.6 MB). The server owns the current action clip (a string plus a start tick, two synced fields). The client crossfades over 4 ticks. This matches Nublar's B27 "synced pose state" plan. See section 6.
8. **Creature vertex-write fast path (B146) is a gamble.** Direct stores through `sun.misc.Unsafe` replace about 27 LWJGL write calls, but only while a creature model draws. The claim, from JC's own metadata, is that JDK 25 made the stock path allocate on every field write. Nublar runs on the same JDK. Measure on 26.2 before copying. `sun.misc.Unsafe` memory access is also on a removal path. See section 1.3.
9. **Server-side numbers worth copying.** Hunt scan runs on 1 tick in 10, checks at most 6 candidates, then waits 50 to 70 ticks. Pathfinding visits at most about 200 nodes per search, and a failed search blocks any second search in the same tick. Age is a birth timestamp plus a "growth clock" that skips world time jumps, so nothing about age is synced each tick. See section 10.
10. **Costs I would not copy.** Leaf-breaking scans every block in the body box on every server tick. The species registry (129 files, 764 KB) syncs to every client at login. Hitbox lists sit in that sync. Both are visible in section 10 and 11.

---

## 1. The four mixins

JC ships four mixins in two configs. The metadata comment explains the split. The first config is `required: true`. If a NeoForge update moves a target, the game stops at start instead of silently losing tick time. The second config is `required: false`. The game plays without it, only slower, so a moved target logs and boots. Mixin's `required` flag works per config file, which is why there are two files `[doc]` (`neoforge.mods.toml`, comments above each `[[mixins]]`).

L15 is a third mixin that existed briefly. It replaced a coloured creature's overlay coordinate to carry two pattern tints. The per-frame tint table replaced it. The tints no longer travel in the vertex. UV1 is vanilla's again, and nothing needs to be substituted `[doc]` (`neoforge.mods.toml`; `shaders/core/creature_color.vsh` comments).

### 1.1 Part spatial index (`LevelPartScanMixin`, ticket B110)

Files: mixin `mixin/LevelPartScanMixin.java:17-51`. Index class `ctygd/kdhoqn` (javap).

**In game.** A park with many big dinosaurs keeps its tick time. Every moving entity asks the level "who is in this box?". Without the index, each ask walks every part hitbox in the dimension.

**Mechanism.**

- The mixin wraps one call: `Level.dragonParts()`. It wraps that call in three methods: the untyped `getEntities(Entity, AABB, Predicate)`, the typed `getEntities(EntityTypeTest, AABB, Predicate, List, int)`, and `hasEntities(EntityTypeTest, AABB, Predicate)`. Each wrapper calls the index with the level, the query box and the original call as a supplier `[v]`.
- The index keeps one bucket map per level. The key is the 16-block section of the part's position (feet). The value is a list of parts. The per-level map is a concurrent map, so the client level and the integrated server level do not clash `[v]` (`ctygd/kdhoqn` javap, fields and `computeIfAbsent`).
- A part joins a bucket when its owner moves it. Each part remembers the key it sits in. If the key is unchanged, the update costs one compare `[v]`. A part leaves on `onRemovedFromLevel` of its owner. The whole level entry goes on the level unload event `[v]`.
- A query pads the box by 4 blocks on X and Z, and by 12 blocks below the box minimum. It does not pad above. It then reads every section in the padded range and concatenates the lists into a fresh list `[v]`. The padding is wider than any part (the largest single box side in the species data is 4.45 blocks), so the result is a safe superset `[inf]`. Vanilla's own loop still runs the exact intersect test on the returned parts `[inf]`.
- If the padded range spans more than 1,024 sections, the index gives up and the caller gets the flat list `[v]`.
- Before it answers, the index compares its count (indexed plus foreign parts) with the size of the flat list. If they differ, it throws the buckets away and rebuilds them from the flat list `[v]`. This is the self-heal for a missed add or remove.
- If any part in the level is not a JC part (for example an Ender Dragon part), the index returns the flat list and stays off `[v]`.
- The feature has a config switch `partSpatialIndex`, default on `[v]` (`dnukfuguy.java:27`). A second switch, `partIndexVerify`, default off, makes every query also run the flat scan and log an error for any part the index missed. The log text tells the user to turn the index off and report it `[v]`. A JVM property `jurassicraft.breakPartIndex` makes the index return an empty list. It exists to prove the verifier fires `[inf]`.

**Platform overlap.** NeoForge keeps a per-level collection of all parts and loops it inside `Level.getEntities`. Nublar's hitbox spec already records this and already accepts a mixin on the same call (`docs/dinosaur-hitboxes-and-movement-design.md` lines 105, 314-320, 731). JC's mixin hits the same three methods.

**Verdict.** Adopt, with three improvements. (1) Keep the count cross-check and rebuild. (2) Index foreign parts too, so a Dragon in the level does not turn the index off. (3) Ship the verifier as a dev switch with a test hook, as JC does. Reason: JC shows the design works in a mod with 129 species and about 1,000 part objects per species set. `[v]` for design, `[inf]` for the benefit size.

### 1.2 Presized collision visited set (`BlockIntersectVisitSetSizeMixin`, ticket B147)

File: `mixin/BlockIntersectVisitSetSizeMixin.java:1-30`.

**In game.** A large dinosaur's movement and block-inside checks stop allocating and rehashing a hash set on every tick.

**Mechanism.** The mixin wraps the constructor call of a `LongOpenHashSet` inside `BlockGetter.forEachBlockIntersectedBetween(Vec3, Vec3, AABB, BlockStepVisitor)`. It computes the box volume in blocks: `(floor(maxX) - floor(minX) + 1)`, the same for Y and Z, multiplied. It clamps the volume to the range 1 to 8,192 and builds the set with that expected size `[v]`. Vanilla uses the default size. A 7 x 8 x 7 box (Brachiosaurus) holds about 500 blocks, so the default set rehashes several times per call `[inf]`. The call site in vanilla is the movement and "inside block" check for entities `[inf]`.

**Platform overlap.** None known. This is vanilla code. Check that the method and the `LongOpenHashSet` constructor still exist in 26.2 before relying on it.

**Verdict.** Adapt. Adopt only for B26 large bodies, and only after a profile shows rehash cost. The mixin is small and `required` here, so a target move fails fast. Nublar's own plan forbids tick mixins on vanilla base classes (B25), so this one needs a lead decision `[inf]`.

### 1.3 Vertex write fast path (`BufferBuilderFastWriteMixin`, ticket B146)

Files: `mixin/BufferBuilderFastWriteMixin.java:1-111`, `stta/stta.java:1-112`.

**In game.** The config option `fastVertexWrites` (client, default on) says: "Write creature vertices straight to memory instead of through the graphics library. Large parks draw several times faster with this on." Turning it off restores the stock path exactly `[doc]` (`dnukfuguy.java:10`).

**Why it exists.** The metadata says B146 "drives creature drawing back to the speed it had before JDK 25's LWJGL started allocating on every vertex field write" `[doc]`. I did not measure this. The claim is JC's.

**Mechanism.**

- The mixin redirects the LWJGL `MemoryUtil` write calls inside `BufferBuilder`: put-int (2 sites, in `putRgba` and `putPackedUv`), put-short (2 + 2 sites), put-float (11 sites in `addVertex`, `setUv`, `setLineWidth` and the long `addVertex`), put-byte (10 sites in `setColor`, `setNormal` and the long `addVertex`). Each redirect carries a `require` count that must match, so a changed target fails at start of that config `[v]`. About 27 call sites in all.
- Each redirected call asks "is this thread inside a creature draw scope?". If yes, it stores through `sun.misc.Unsafe` straight to the address. If no, it calls the stock LWJGL write `[v]`.
- At class load, the helper probes `Unsafe`. It allocates 8 bytes with LWJGL, writes an int and a float with `Unsafe`, reads them back with LWJGL, and compares. Only on a match does it enable the path. The log prints "on", "off (probe wrote bytes that did not read back)" or "off (Unsafe unavailable)". This probe is a good pattern `[v]` (`stta/stta.java:83-111`).
- A debug property `jurassicraft.vertexWriteCount` counts fast and stock writes with `LongAdder`s `[v]`.

**Platform overlap.** `BufferBuilder` is vanilla. ImmediatelyFast changes other buffer paths (see `research_notes/Optimization and tech mod survey/render_pipeline_mods.md`). `sun.misc.Unsafe` memory-access methods warn since JDK 24 and are planned for removal `[inf]` (JEP 498, from memory). A JDK upgrade can break this path. The probe and the stock fallback make that safe, but the speed-up would vanish.

**Verdict.** Defer. Measure on 26.2 first (a 100-creature scene, with and without a stock-path profile). If the allocation claim holds on the 26.2 JDK, report it upstream or to ImmediatelyFast before writing our own `Unsafe` code. Reason: it is a deep hack, needs a JVM argument-free `Unsafe`, and is `required:false` even in JC. Home: B25 (profile) and P2 (JDK).

### 1.4 Creature draw scope (`ModelFastWriteScopeMixin`)

File: `mixin/ModelFastWriteScopeMixin.java:1-37`.

**In game.** Nothing visible. It limits the fast path to creatures, so other mods' models keep the stock path.

**Mechanism.** The mixin wraps the call from `Model.renderToBuffer(PoseStack, VertexConsumer, int, int, int)` into `ModelPart.render`. It only acts when the model is one of JC's creature model classes (they implement a marker interface). It stores the current thread in a static field, calls the original, and restores the previous value in a `finally` block `[v]`. The redirects in 1.3 read that field `[v]`. The scope is global, not thread-local. Two threads cannot both be in scope `[v]` (`stta.java:16-37`).

**Platform overlap.** In 26.x much of the draw path uses `submitModel` and feature renderers. I did not check that `Model.renderToBuffer` is still on the hot path in 26.2 `[inf]`.

**Verdict.** Defer, with 1.3. Drop the global static if ever copied. Use a thread-local or a per-draw token.

---

## 2. The multipart entity system

**In game.** A dinosaur has 3 to 20 hitbox pieces that follow its body, head and tail. Arrows and melee hit the piece. A hit on any piece damages the animal and gives kill credit to the attacker. When the animal sleeps, drinks, eats, rests or dies, the pieces change shape and position. Players are nudged by the pieces, so a big animal shoves a player aside.

### 2.1 Classes

| Role | Class | Path |
| --- | --- | --- |
| Abstract part | Extends NeoForge `PartEntity`. Never saved. Always pickable. Equality is identity. Remembers its index bucket key | `ctygd/dbgouk.java:1-45` `[v]` |
| Creature part | Holds the owner, a name, a base width and height, and the current scaled dimensions. Forwards hurt, push and interact to the owner | `ctygd/vtclb.java:19-105` `[v]` |
| Owner | The creature base entity. Holds the part array, builds the parts and updates them each tick | `ctygd/omogwgxr.java:1623-1660` and the failed constructor (javap) |
| Index | Section buckets per level | `ctygd/kdhoqn` (javap) |
| Debug overlay | Draws "parts N" and the union box top in the F3 gizmo layer | `stta/qkek.java:130-139` `[v]` |

### 2.2 How parts are built

- The constructor reads the species definition. It builds one part per entry of the longest hitbox list among six lists (default, carcass, sleep, drinking, eating, resting). Parts named in the default list take their names. Extra parts get the name "part" plus a number and a size of 0.05 x 0.05 `[v]` (javap, constructor).
- All pose lists reuse the same part objects. A pose list with fewer boxes shrinks the spare parts to 0.05 `[v]` (`omogwgxr.java` pose method, the "else" branch with `0.05F`).
- The constructor reserves a block of ids: the parent id plus one id per part. `setId` on the owner sets each part to `parentId + i + 1` `[v]` (`omogwgxr.java:1644-1651`). This matches vanilla's rule for dragon parts (Nublar's plan "Align IDs as `parentId + i + 1` from a reserved block").
- `isMultipartEntity()` and `getParts()` return the array. The parent box is not pickable when parts exist (`isPickable` checks the part count) `[v]`.

### 2.3 Per-tick placement

The owner updates parts once per tick on both sides, at the end of `tick()` (`omogwgxr.java:1570-1620`, the part call at the end) `[v]`. The steps (from `javap`, `gbxqxxv`):

1. Choose the active box list: carcass if dead, sleep if asleep, drinking or eating or resting only while the matching animation plays, otherwise default.
2. If the list changed from the last tick, start a blend. The blend length comes from the species `pose_blend_ticks` entry for that pose. A blend uses smoothstep (t x t x (3 - 2t)) between the old and the new box values `[v]`. The value arrays hold 9 doubles per part: offset (3), width, height, pivot (3) and a "has pivot" flag.
3. For each part, work out the offset from the body yaw. The head piece has a `pivot`. The head yaw (clamped to plus or minus 75 degrees from the body) and the head pitch (clamped to plus or minus 60 degrees) rotate the offset about that pivot `[v]`. Other parts only rotate with body yaw.
4. Call `snapTo(x, y, z, yaw, 0)` on the part, scale its size by the creature scale, and update the index bucket `[v]`.
5. Build a union box of the body box and all part boxes. The renderer uses it for culling (section 7) `[v]`.

Cost per creature per tick: part count times a few dozen floating point operations, plus one index update. Nothing is throttled by distance `[v]` (no distance read in this method). Real cost is `[inf]` small, but it scales with the plan's higher part counts.

### 2.4 Numbers per species

From the 129 species files (`raw/data/jurassicraft/jurassicraft/species/*.json`, my script):

| Value | Result |
| --- | --- |
| Species with hitbox data | 129 of 129 |
| Parts per creature (longest list) | min 3, median 7, mean 8.0, max 20 (Apatosaurus, Mamenchisaurus) |
| Total part objects over all species | 1,038 |
| Boxes in the default list | min 3, median 6, max 19 |
| Species with carcass / eating / resting / sleep / drinking lists | 129 / 128 / 101 / 100 / 99 |
| Sum of all boxes in all lists | 4,501 (max 103 for one species) |
| Species where a pose list has more boxes than the default | 20 or more (Allosaurus 8 vs 6, Apatosaurus 20 vs 19, Brachiosaurus 16 vs 15) |
| Species with a head `pivot` | 128 |
| Largest single box side | 4.45 blocks |
| Examples (parts) | Compsognathus 3, Velociraptor 7, Tyrannosaurus 13, Triceratops 6, Brachiosaurus 16, Spinosaurus 16, Indominus 15, Apatosaurus 20 |
| `pose_blend_ticks` examples | carcass 11 to 40 ticks, sleep 18 to 61, eating three values (for example 75, 85, 166), drinking three values (40, 50, 90) |

A pose with three blend values has (start, hold, end) semantics: ramp in until the first value, hold until the second, ramp out until the third `[v]` (`omogwgxr.java`, the "dnukfuguy(int[])" weight method).

### 2.5 What parts do

- Damage: the part calls the owner's `hurtServer` with the same damage source. No per-part multiplier exists `[v]` (`ctygd/vtclb.java:63-83`). The attacker's stat and damage indicator particles are added for player attacks over 2 damage.
- Interact: the part calls the owner's `interact`, then falls back to the held item's entity interaction `[v]`.
- Push: a part pushed by something knocks the owner back `[v]`. In the other direction, `pushEntities` on the owner shoves nearby entities away from each part (strength 0.05, scaled by 1 / distance, minimum distance 0.01) instead of using the parent box `[v]` (`omogwgxr.java:1057-1100`).
- Saving: parts are never saved. They are rebuilt from the species file `[v]`.
- Collision with blocks: none. Parts only receive hits.

### 2.6 Platform overlap and verdict

Vanilla and NeoForge already supply `PartEntity`, `isMultipartEntity` and the id convention. JC adds only data-driven placement, pose lists, blending, the spatial index and a parent-box rule.

Verdict: adopt the data shape (named boxes with offsets, one `pivot` on the head, extra pose lists with blend ticks) for B27, with adaptations. Nublar wants section parts with chained necks and tails, so use per-part damage multipliers and keep the JC index. Do not copy "unused parts shrink to 0.05": Nublar's plan says to use "distance-based part counts with the same objects and IDs", which is a better rule. See section 13.

---

## 3. Data-driven species, sync and tracking

**In game.** Every species is one JSON file. The server and the client both read it. A datapack can add a species.

### 3.1 Species definition

- One datapack registry holds the species. It is registered with both a storage codec and a network codec, so the whole registry syncs to every client at login `[v]` (`wkjvl/dbgouk.java:21-23`).
- 129 files total 763,519 bytes (about 764 KB, my count). T. rex is 7.6 KB. The largest (Apatosaurus) is 12 KB `[v]`.
- A species file holds: classification (diet, temperament, sleep time, period), attributes (baby and adult health, speed, strength, follow range), size (baby, adult and female-adult width and height, eye heights, scale for infant, adult and female adult, all hitbox lists, `pose_blend_ticks`), `inventory_size`, growth (`maximum_age`, curve), social (`max_herd_size`, `attack_bias`, imprintable, defend owner), reproduction (birth type, incubation time, clutch size, cooldown, egg colours), habitat (`spawn_chance`, biome tags), sounds (nine event ids), remains (bones, skeleton grid, poses) and `color_families` (named palettes for four body layers) `[v]`.
- Sound, remains and breeding data is not needed on a client. It still travels `[inf]`.

### 3.2 One entity class

All land species share one creature class. All water species share a second class that adds swimming. The entity types differ by id and size only `[v]` (`core/vtclb.java:140-175`). Counts: 101 land, 28 water, 129 species `[v]`. The creature base class reads its species from the registry key that matches its entity type id `[v]` (`ctygd/omogwgxr.java:112-125` constructor via javap).

### 3.3 Synced data and tracking

- Entity type: `clientTrackingRange(10)` (10 chunks, 160 blocks). Update interval is the default (3 ticks) because none is set. Land and water types use the same builder values `[v]` (`core/vtclb.java:164,171`).
- Synced data fields on the base class: sex (bool), age value (long), age mode (bool), sleep flag, carcass flag, death cause (byte), size variation (float), four colour ints, a bool, an animation name (string), an animation start tick (long), the owner UUID (a string), and the order state (a string) `[v]` (`omogwgxr.java:81-93`, 17 accessors).
- Age is a birth timestamp, not a counter. The creature recomputes growth from `now - birth` divided by 8, clamped. Every 8 ticks the server re-writes the stored value if its mode changed `[v]` (`omogwgxr.java:424-431`, `wgpfjieca`).
- A "growth clock" saved-data object counts game ticks, plus the ticks skipped when the world time jumps (sleeping through the night, `/time set`). Age = game time + skipped ticks. The server sends one `growth_clock` packet with the skipped total (a var-long) to clients `[v]` (`ctygd/ummp.java:13-93`, `znpnvzxz/vtclb.java:9-11`). So no age packet goes out per tick.
- Dimensions are derived from age, sex and a size-variation float. The creature refreshes its dimensions only when width or height changes by more than 0.01 `[v]` (`omogwgxr.java:433-470`). Attributes (health, speed, damage, follow range, step height) re-apply every 40 ticks, or at once when a dirty flag is set `[v]` (`omogwgxr.java:1593-1596`).
- Packets: four client-bound and five server-bound `[v]` (`znpnvzxz/aeygzxj.java:26-35`). The client-bound ones open screens (paleo pad, order screen, paddock sign) and send the growth clock. The server-bound ones set orders, place paddock signs, dismount riders and control vehicles. No per-creature position or animation packets exist beyond vanilla tracking and synced data `[inf]`.

**Platform overlap.** All of this is vanilla and NeoForge APIs (`SynchedEntityData`, `SavedData`, `PayloadRegistrar`, datapack registry sync).

**Verdict.** Adapt for B00 and B16.
- Adopt "age as a timestamp plus a skip-aware clock" for B16 growth. It removes a per-tick sync.
- Do not sync full species definitions to clients. Split a client part (hitboxes, scale, colour families, animation names) from a server part (breeding, remains, sounds). Reason: 764 KB at login is acceptable for one pack, but it grows with Release 1 species and with any addon.

---

## 4. Models and model loading

**In game.** Creatures, eggs, skeleton displays and machines come from JSON models that the mod converts at load time. No Blockbench-only format is needed at run time.

### 4.1 Creature geometry loader (`hmocea/`, "geometry JSON loader")

Files: `hmocea/vtclb.java:27-138`, `hmocea/dnukfuguy.java:1-84`. Registered in `JurassicraftClient.java:90-120` `[v]`.

- Source: `assets/<ns>/jurassicraft/geometry/entity/<name>.json`. It holds `texture_size`, and a tree of bones. Each bone has `name`, `pivot`, `rotation`, `mirror`, cubes (`origin`, `size`, `uv`, `inflate`, `mirror`) and children `[v]`.
- The loader builds a vanilla `MeshDefinition` and `LayerDefinition`. The result is a vanilla `ModelPart` tree. It records the bone names per model for later checks `[v]`.
- Fallback: if the resource manager does not have the file, it reads the jar classpath `[v]`.
- A config option `logModelLoading` logs bone counts per model `[v]` (`dnukfuguy.java:8`).
- Counts: 406 geometry files: 129 creatures (one per species), 48 eggs, 225 skeleton models, 4 skull displays. Per creature: 49 to 346 bones, median 108. Every bone holds exactly one cube. Total cubes in creature models: 15,840 `[v]` (my script over the JSON).
- Models are not per growth stage by default. A species can name a model per stage (infant to adult) or one model for all four. Growth uses the render scale `[v]` (`stta/onzkovhp.java:288-299`).

Because each bone is one cube, a median creature walks 108 `ModelPart`s per pass. A pass over 20 median creatures is about 2,200 part renders `[inf]`. This is a cost driver that bone masks (5.4) reduce for overlay passes.

### 4.2 Tabula-style model loader (`fgwme/`)

Files: `fgwme/aeygzxj.java` (codec for "TabulaCube" trees), `fgwme/dnukfuguy.java` (unbaked geometry that bakes quads), `fgwme/vtclb.java` (unbaked model), registered with `ModelEvent.RegisterLoaders` `[v]` (`JurassicraftClient.java:168-171`).

- It reads a Tabula-style cube tree (name, position, offset, rotation, scale, size, texture offset, mirror, children), turns each cube into 6 quads, bakes them with NeoForge's `QuadBakingVertexConsumer`, and applies the block-state or item transform. It supports an emissive light level and a "shade" flag, and writes quads through the standard `ExtendedUnbakedGeometry` path `[v]`.
- Use: custom block and item models (blocks and items that were built in Tabula). `models/` holds 5,320 JSON files: item 4,039, block 1,240, fence 22, machine 16, vehicle 3 `[v]`.

### 4.3 Machine model loader (`omogwgxr/omogwgxr.java`)

Machine block-entity models come from `models/machine/<name>.json` in the same Tabula codec. The loader turns them into `LayerDefinition`s and registers one layer per machine (and per state, such as petri dish or running) `[v]` (`omogwgxr/omogwgxr.java:23-60`, `omogwgxr/kdhoqn.java:12-66`).

### 4.4 Platform overlap and verdict

Vanilla's `LayerDefinition` and NeoForge's model loader events do the heavy work. JC's own loaders only translate formats.

Verdict: drop for the creature loader (Nublar uses GeckoLib 5 models, `plan P2` keeps them). Adapt one idea for B24: a load-time check that logs every missing bone, with the list of available bones (see 6.6). Reason: the check turns an art mistake into a clear log line.

---

## 5. Colour and pattern system

**In game.** Each animal carries four body colours: base body, underside, primary pattern, secondary pattern. Each is picked from the species' named palette. The animal shows them on its hide in one model pass. Eyes, teeth, claws, mouth and nostrils sit on top as overlay passes. Eyes glow. The eyelids replace the eyes for 4 ticks in every 100 ticks (a blink). A genome editor shows a translucent hologram of the animal at 40% alpha.

### 5.1 Layers and species render definition

- The layer list (`stta/jgdp.java:6-26`): underside, primary pattern, secondary pattern, pattern, pattern outline, stripes, markings, frill, quills, crest, fins, cheeks, tail scales, claws, mouth, nostrils, teeth, eyes (glow, shown while open), eyelids (shown while closed), male parts, female parts `[v]`. 21 entries. Texture name = `<species>_<sex>_<suffix>.png`.
- Each species has a render definition built by a builder (`stta/onzkovhp.java`): model per growth stage, a texture set per sex and stage (falls back to adult, and logs a debug line), eye texture, the base colour source, a list of colour layers and fixed overlay layers, optional extra render layers, a render scale (T. rex 2.0), and a "procedural suppression" value in 0 to 1 (default 0.75, T. rex 0.8) `[v]` (`vtclb/jgdp.java:583-590`). 129 render definitions exist `[v]`.
- Four colour layers are tinted: base body, underside, primary pattern, secondary pattern. Everything else is an untinted overlay, except that the T. rex "stripes" overlay uses the secondary pattern colour `[v]`.
- Colour data: four ints in synced data (default -1 = none). The species file gives four named palettes (T. rex: 11 base, 10 primary, 12 secondary, 6 underside names) `[v]`. That is 7,920 combinations for a T. rex `[inf]`.

### 5.2 The single-pass colour pipeline

Files: `stta/ummp.java:26-103` (pipelines and render types), `stta/omogwgxr.java:21-247` (tint table), `stta/gjtxsudo.java` (bit helpers), `shaders/core/creature_color.vsh` and `.fsh`, `stta/kdhoqn` (renderer, javap).

**Step by step.**

1. Offline, the art team packs the three pattern layers of a species into one sheet named `<species>_<sex>_packed.png`. Its RGB holds the owning layer's grey. Its alpha holds the layer id in four exact levels: 0 = none, 85 = underside, 170 = primary, 255 = secondary. A shader comment gives the rule: when two layers claim a texel, secondary wins over primary over underside, decided at pack time `[doc]` (`creature_color.fsh` header comments). The grey master `<species>_<sex>_body.png` holds the hide.
2. Each frame, before drawing, the renderer's extract step copies the creature's four colour ints into a new row of the tint table. The row index is the creature's slot this frame `[v]` (`stta/omogwgxr.java:36-58`).
3. The tint table is a dynamic texture, 4 texels wide and 2,048 rows tall. Size 4 x 2,048 x 4 bytes = 32,768 bytes. Column 0 = base, 1 = underside, 2 = primary, 3 = secondary. A texel's alpha says "this creature has a colour here" (255) or not (0) `[v]` (`stta/omogwgxr.java:21-28`, `stta/gjtxsudo.java`).
4. Once per frame, after all creatures extracted (on `SubmitCustomGeometryEvent`), the mod uploads the used rows (rows 0 to n-1) with one `writeToTexture` call. A `RenderFrameEvent.Pre` handler resets the row counter `[v]` (`stta/omogwgxr.java:60-77`).
5. The creature draws with the model tint set to a value that holds the row number: red = low byte, green = high byte, blue = 0, alpha = 255 `[v]` (`stta/gjtxsudo.java` helper and `creature_color.vsh` comments).
6. The vertex shader is vanilla's entity vertex shader with two changes. It does not treat the vertex colour as a colour. It passes the row to the fragment shader as a flat integer. It computes lighting against white, so the tint can join afterwards. The overlay coordinate (UV1) and the lightmap stay vanilla, so the hurt flash is vanilla's `[doc]` (`creature_color.vsh` comments).
7. The fragment shader samples the packed sheet and the grey master unconditionally (to keep mip selection defined). It reads the layer from the packed alpha (`int(a x 3 + 0.5)` maps 0/85/170/255 to 0/1/2/3 directly). It does one `texelFetch` into the tint table at (layer, row). If the creature has no colour for that layer, it falls back to the base column. An uncoloured base tints white. A non-zero layer draws the packed sheet's grey times the tint. Layer 0 draws the master times the base tint and cuts out at alpha 0.1. Then it applies vanilla's lighting, overlay and fog in the same order as vanilla `[v]` (`creature_color.fsh:131-194`).

**Fallbacks.**

- More than 2,048 coloured creatures in one frame: a warning logs once, and the rest draw "a pass per layer" for the rest of the session `[v]` (`stta/omogwgxr.java:36-58`).
- No packed sheet for a species: 6 of 129 species (Alligator Gar, Brachiosaurus, Indominus, Malusaurus, Plateosaurus, Triceratops) draw a pass per colour layer `[v]` (my file check; `stta/ummp.java:57-73` logs "no packed sheet, drawing a pass per layer").
- The JVM property `jurassicraft.singlePassColorOff` forces the per-layer path `[v]`.

**Translucent variant.** A second pipeline sets a shader define `HOLOGRAM_ALPHA` of 0.4 and uses translucent blending. The hologram and the "invisible but glowing" cases use it `[v]` (`stta/ummp.java:39-42`, `stta/kdhoqn` render type method).

**Draw state.** Both pipelines use the entity vertex format (quads), depth default, no culling, 4 samplers plus the tint table sampler, uniform buffers for transforms, projection, fog and lighting. Both register through `RegisterRenderPipelinesEvent` `[v]`. This is the only core shader JC ships `[v]` (`raw/assets/jurassicraft/shaders/` holds two files).

### 5.3 Cost, as the code states it

The shader header says the creature's hide and "all three of its pattern layers" draw "in the one pass the creature was already paying for". The mod's config comment says large parks draw "several times faster" with the vertex fast path (1.3), which is a separate switch `[doc]`. JC states no frame-rate figure for the colour pass. I have no measured number.

### 5.4 Overlay passes and bone masks

Files: `stta/dbgouk.java:1-48` (overlay layer), `rtwzimpls/ummp.java:19-50` (coverage scan), `rtwzimpls/dnukfuguy.java:15-64` (mask), `rtwzimpls/kodkieb.java:154-206` (overlay model).

1. Each overlay layer (eyes, eyelids, mouth, nostrils, teeth, claws, stripes, quills and so on) submits the same model again with its own texture. Eyes use a glow render type at full light (15,728,880). Others use cutout, or translucent if the creature is a hologram `[v]`.
2. An overlay model wraps the main model. It first runs the main model's pose (replay, see 6.4). It then hides bones.
3. Which bones? On the first draw of a texture, an async task (on the background executor) reads the overlay PNG. For each bone it checks the bone's cube UV boxes. If any pixel in a box has non-zero alpha, the bone "draws". If only a child draws, the bone is "carried": its own cubes are skipped but children draw. All others are hidden for that pass `[v]` (`rtwzimpls/ummp.java:31-107`).
4. Until the async task finishes (the call returns "not ready"), the pass "draws the whole model". If a bone name in the mask is missing from the baked model, or the mask would hide the model root, the pass also draws the whole model, with a warning or error in the log `[v]`.
5. The reload listener clears all of these caches on a resource reload `[v]` (`JurassicraftClient.java:75-83`).
6. A dev property `jurassicraft.overlayCoverageAudit` logs how many opaque texels fall outside every bone box (so the artist sees dead pixels) and how many bones draw `[v]`.
7. Sex parts: bones whose names end in "Male" or "Female" hide for the other sex. A log line lists the single-sex geometry `[v]` (`rtwzimpls/dbgouk` javap).

**Draw count.** The body pass counts as 1. Median species: 5 overlay sheets per sex (min 0, mean 4.5, max 10; counted over 129 species). Eyes and eyelids never draw together. So a median creature submits about 5 or 6 passes. The T. rex has 7 overlay files (claws, eyelids, eyes, mouth, nostrils, stripes, teeth), so 7 passes at most `[v]` (file counts, `stta/dbgouk.java`). Nublar's Gallimimus draws 23 passes today (`docs/project-plan.md` risk 8).

### 5.5 Textures

- 2,258 files in `textures/entity` (all entities). Per species 2 sexes x (body, packed, overlays) plus two skeleton skins. Per-species folder: min 0 to max 48 files, median 18 `[v]`.
- Body sheet sizes (male): 128x128 (31 species), 256x128 (27), 2048x1024 (24), 256x256 (15), 1024x1024 (13), others. So 26 species use 1024-class or 2048-class sheets `[v]`.
- Estimated GPU memory if one species' whole male set loads (RGBA8, no mips): min 2.2 MB, median 34 MB, max 134 MB. T. rex 42 MB per sex (85 MB both). Triceratops 101 MB per sex. Textures load on first use `[inf]`.

### 5.6 Comparison with Nublar's bake spec

| Point | JC | Nublar bake spec (`docs/superpowers/specs/2026-10-08-dino-layer-baking-design.md`) |
| --- | --- | --- |
| What combines the layers | A fragment shader, per frame | A CPU compositor, per genome key |
| Per-genome GPU memory | 0 (32 KB shared table) | 4 MB per key, 24 keys by default = 96 MB |
| Cost when a colour changes | One row write per frame | A bake (1 to 2 ms estimate) after a 10-frame settle wait |
| Layers supported | 3 tint layers in one packed sheet, plus base | Any layer list (11 for Gallimimus) |
| Core shader | Yes (custom pipeline) | No (violates nothing; keeps OC6) |
| Overlay layers | Separate passes with bone masks | None needed, all baked |
| Iris and Sodium | Unknown. A custom core shader may not run under a shader pack | Safe by design (OC6) |
| Fallbacks | Per-layer path for 6 species and above 2,048 creatures | Base only, or the layered path, until a bake is ready |

### 5.7 Platform overlap and verdict

Vanilla 26.x already has `RenderPipeline`, render-type setup, `DynamicTexture` and `writeToTexture`. The shader pipeline is documented and stable on 26.1 and 26.2 (Sodium and Chloride use it; see `render_pipeline_mods.md`). A custom core shader is the one part that could break under shader packs `[inf]`.

Verdicts:
- Packed sheet that stores up to four tint layers in a levelled alpha: adapt. The art-side idea (pack three pattern layers into one image offline) also helps the CPU bake, because the bake touches one source instead of four. Reason: it cuts bake input and removes layer-order ambiguity.
- Tint-table shader: defer. Compare against the bake in a short test: 20 Gallimimus, with Iris and a shader pack, on 26.2. The lead must decide whether OC6 ("no core shader") has an exception. Reason: JC proves single-pass without per-genome textures works, but it conflicts with a written Nublar rule and has no shader-pack test.
- Overlay bone masks: adopt (adapted to GeckoLib 5 bone visibility). Reason: it does not need a shader, it works with the bake, and it makes eye, tooth and claw passes cost only the bones that hold pixels. Home: B09.
- Hologram at fixed alpha: drop for now. Nublar has no hologram module.

---

## 6. Animation

**In game.** Creatures walk, run, swim, idle, look around, eat, drink, roar, call, sleep, rest, get hurt, lay eggs and die. A new action blends in over 4 ticks (0.2 s). Necks bend toward where the animal looks. Tails sway. Legs swing with the movement speed.

### 6.1 Clip format and loader

- Clips are JSON under `assets/jurassicraft/neoforge/animations/entity/<species>/<clip>.json`. They use NeoForge's JSON animation loader and the vanilla keyframe classes: `AnimationLoader.INSTANCE.getAnimationHolder`, `AnimationDefinition.bake(rootPart)` and `KeyframeAnimation.apply / applyWalk` `[v]` (`aeygzxj/ummp.java:1-50`). This is vanilla plus NeoForge, not GeckoLib.
- A clip file holds `length`, `loop` and a list of channels (bone, target, keyframes with timestamp, value, interpolation) `[v]`.
- Each model keeps its own cache of baked clips. A clip bakes on first use. It re-bakes if the loaded definition object changes (resource reload). A clip that cannot bind to the model (a bone is missing) logs once and is skipped `[v]` (`aeygzxj/ummp.java:29-51`).
- Missing clips log once per species with the expected file path `[v]`.

### 6.2 Counts

| Value | Result (my script over the clip files) |
| --- | --- |
| Clip files | 1,780 over 129 species |
| Clips per species | min 5, median 15, max 21 |
| Total size | 26.6 MB. Median clip 12 KB. Max 81 KB |
| Channels per clip | median 33, max 119 |
| Keyframes per clip | median 156, max 1,110. Total 347,354 |
| T. rex walking | 1.4 s loop, 42 channels over 28 bones, 378 keyframes |
| T. rex sleeping | 3.05 s, 57 channels over 43 bones, 456 keyframes |
| T. rex roaring | 6.25 s, 55 channels over 40 bones, 605 keyframes |
| Compsognathus walking | 0.4 s loop, 31 channels, 155 keyframes |
| Most common clips | dying 129, injured 129, eating 128, swimming 118, speak 117, calling 113, resting/running/walking 101, sleeping 100, attacking 95 |

### 6.3 Layers of motion

Each species lists an ordered chain of "layers". Every layer reads the render state and changes bone poses `[v]` (`aeygzxj/dnukfuguy.java`, `vtclb/jgdp.java:583-590`). The T. rex chain shows the pattern:

1. Gait: walk and run clips blend by real speed. Between 0.05 and 0.2 blocks per tick the weight moves from walk to run. The T. rex also sets a walk-clip speed scale of 1.1 and a strength factor of 1.8. A separate swim clip plays in water `[v]` (`aeygzxj/vtclb.java:257-298`, `vtclb/jgdp.java:583-590`). Speed comes from the position change since the last tick `[v]` (`stta/kdhoqn` extract method).
2. The server action clip, if one is playing (section 6.5).
3. Two helper layers in the species definition (idle motion). I did not decode them.
4. Tail wave over the tail chain (9 bones for the T. rex): a yaw sine wave at 0.06 radians per tick, a phase lag of 0.4 radians per bone, and a weight that grows toward the tip. Amplitude moves from 0.9 degrees (still) to 5 degrees (at 0.14 blocks per tick or faster) `[v]` (`aeygzxj/kodkieb.java:13-50`).
5. Leg swing: a pitch swing on each thigh bone, 2.4 degrees at walk speed, 180 degrees out of phase between the two legs `[v]` (`aeygzxj/kodkieb.java:52-60`).
6. Head tracking over the neck chain (two bones for the T. rex). It adds the head's yaw and pitch, clamped, and split over the chain by a weight curve that sums to 1 `[v]` (`aeygzxj/kodkieb.java:95-140`). I did not decode the clamp values.

**Procedural suppression.** Two weights reach the model each frame `[v]` (`stta/kdhoqn` extract method, `octtcpmd/gjtxsudo.java`). The walk weight is 1 minus the weight of any playing clip that is flagged as walk-blocking. The procedural weight (tail wave, leg swing, head tracking) is 1 minus the larger of that blocking weight and (the weight of any action clip times the species suppression value). The suppression value is 0.75 by default and 0.8 for the T. rex. So a roar fades the walk out fully and the tail and head partly.

Speed scaling: the walk clip is scaled by the ratio of current speed to adult speed, clamped to 0.05 to 1 `[v]` (`omogwgxr.java`, `updateWalkAnimation`). The plan's decision D14 (animation speed that follows body size) is "not decided"; JC does this only for the walk clip.

### 6.4 Pose replay (ticket B133)

Files: `rtwzimpls/vtclb.java:59-155`.

- After the body model evaluates its pose, it copies 9 floats per bone (x, y, z, rotation x, y, z, scale x, y, z) into a float array held in the render state. It also stores the bone count `[v]`.
- Any later model for the same creature (an overlay pass) replays the array instead of evaluating clips again. It checks the bone count matches, resets visibility, and applies the sex-part hiding `[v]`.
- A dev property `jurassicraft.poseReplayVerify` runs both ways and logs an error once per model if they differ (with the number of bones that disagree). Another property `jurassicraft.breakPoseReplay` zeroes the stored pose to prove the verifier works `[v]`. This mirrors the part-index verifier.
- A dev property `jurassicraft.validateAnimationsOnLoad` checks every clip against the model and logs problems (a check for missing animations, missing bones, and an animation timestamp limit of 0.051) `[v]` (`aeygzxj/aeygzxj.java:9-49`, partly javap-only).

### 6.5 Server-owned action animation

Files: `octtcpmd/dnukfuguy.java` (controller), `octtcpmd/aeygzxj.java` (a per-species clip set), `octtcpmd/kodkieb.java` (the sets), `octtcpmd/vtclb.java` (events), `octtcpmd/gjtxsudo.java` (state for the renderer).

- The server picks the action clip by name (`roaring`, `calling`, `eating`, `drinking`, `attacking`, `sleeping`, `dying`, and so on, about 30 names). It writes two synced fields: the name and the start game tick `[v]` (`omogwgxr.java:1472-1483`).
- Each species has a clip set that lists names and lengths in ticks. For one big carnivore: walking 24, running 18, roaring 20, calling 56, head cocking 70, eating 52, drinking 90, attacking 22, laying egg 40, leap 12. Sleeping and resting are 10 ticks long and then hold; dying is 16. A herbivore set differs: sleeping 40, resting 20, dying 37 `[v]` (`octtcpmd/kodkieb.java:45-46`).
- Clips carry events at fixed ticks, tagged server or client. For example: roar sound at tick 2 and roar peak at tick 6 for a 20-tick roar. The server runs server events and the client runs client events from its own copy of the clock, so sounds are not sent as packets `[v]` (`octtcpmd/vtclb.java`, `omogwgxr.java:1428-1445`). The "spit release" event fires the spit projectile on the server `[v]`.
- A clip cannot interrupt a non-interruptible clip. Starting a new clip moves the old one to a fade-out slot `[v]` (`octtcpmd/dnukfuguy.java:123-142`).
- The renderer asks the controller for a state with two layers: the current clip with weight min(1, elapsed / 4 ticks), and the fading clip with weight 1 - elapsed since switch / 4, which ends when the weight reaches 0. Looping clips wrap their time. Others clamp `[v]` (`octtcpmd/dnukfuguy.java:234-259`, 4.0 as the fade length in ticks).
- Footsteps: the client, not the server, detects movement over a threshold and fires footstep events from the walking or running clip's tick table. Walk when squared speed is above 0.0025 and run when above 0.04 `[v]` (`omogwgxr.java:1534-1559`, constants 0.0025 and 0.04 at lines 121-122).
- A late-joining player sees the current clip at the right point because the start tick is in synced data `[v]`.

This matches the design Nublar already chose in B27: "a synced pose state: animation id, start tick and loop flag; the server owns it" (`docs/project-plan.md` B27).

### 6.6 Bone lookups and the throat rule

File: `aeygzxj/gjtxsudo.java:83-206` ("bone resolver").

- Every lookup goes through a cache of bone name to part. A missing bone logs once, with the list of bones the model does have `[v]`.
- A "chain" of bone names resolves to a list. For each `neckN` bone, the resolver looks for a bone named `throatN` that is not a child of `neckN`. If found, the layer applies the same rotation to the throat bone, so the throat follows the neck even when the model puts it in a separate branch `[v]` (`aeygzxj/gjtxsudo.java:165-181`). Brachiosaurus-class necks list 13 neck bones `[v]` (`vtclb/jgdp.java` fgwme definition).
- A "required bones" check throws at model build if a species misses a bone it needs `[v]`.

### 6.7 Level of detail

I searched `stta/`, `rtwzimpls/`, `aeygzxj/`, `octtcpmd/` and the creature renderer bytecode for distance and camera use. The only camera use is the name-tag rule. There is no animation skip, pose hold, controller limit, or layer cut by distance `[v]` (grep and `javap` of `stta/kdhoqn`). Server creatures also tick fully at all distances (section 10).

### 6.8 Platform overlap and verdict

NeoForge 26.x ships the JSON animation loader, and vanilla ships `KeyframeAnimation`. GeckoLib 5 offers controllers instead.

Verdicts:
- Server-owned (name, start tick) state, 4-tick crossfade, client-run sound events: adopt for B27 and B16. Reason: JC shows it carries 129 species and removes sound packets.
- Walk and run blend by speed between two thresholds, and suppression of procedural layers while an action plays: adapt for B09 (GeckoLib 5 controllers).
- Throat-follows-neck rule, missing-bone log with the available list, and a validate-on-load switch: adopt for B24 and B09 art checks.
- Distance LOD: no JC precedent. Keep B09's own plan. Reason: this is a gap in JC that Nublar can fill and test.
- Vanilla keyframes instead of GeckoLib 5: drop. Nublar already chose GeckoLib 5. Revisit only if GeckoLib 5 blocks LOD (B09 risk note).

---

## 7. Culling boxes and view distances

**In game.** A long-necked animal does not vanish at the screen edge, and a machine far away does not render.

- Creature culling box: the union of the body box and every part box, from the same per-tick update `[v]` (`omogwgxr.java:1635-1642`, `stta/kdhoqn` culling method returns it). This is the fix Nublar's plan assigns to B16 ("posed, scaled body, under 204,800 blocks³").
- Machine block-entity renderers: view distance 48 blocks `[v]` (`omogwgxr/kijk.java:114-116`). Cultivator render box expands 1 block upward `[v]` (`omogwgxr/dbgouk.java:357-361`).
- Skeleton display (`stta/hmocea.java`): view distance 128. Its box comes from the pose's model extents times the render scale, plus 1 block. With no pose it returns an infinite box. With no species it uses a 4-block radius box `[v]` (`stta/hmocea.java:120-166`).
- Skull display (`stta/wtii.java`): view distance 128 and a box of 1 block inflated by 2 `[v]`.
- Genome editor with hologram: box 1 x 2 x 1 at the block `[v]` (`omogwgxr/jgdp.java:206-209`).
- Entity tracking range: 10 chunks (section 3.3).

**Verdict.** Adopt the union-box rule for B16 (already in the plan). Machines: use 48 as the default for B12 renderers. Reason: a value chosen by a shipping mod is a sane first number.

---

## 8. Block-entity renderers

**In game.** Machines show moving parts and item displays. The genome editor floats a faint animal over its screen. Skeleton and skull displays show the fossil pose.

| Renderer | Path | What it does |
| --- | --- | --- |
| Machine renderer (generic) | `omogwgxr/kijk.java:25-168` | One renderer class for eight machine types. Model per state (default, petri dish, test tubes, egg, hybridizer). Blocks rotate by facing with two flip modes. Static `Model.Simple` baked once per layer, kept in maps `[v]` |
| Incubator, DNA machines, cultivator | `omogwgxr/dbgouk.java:37-390` | Adds item stacks inside the machine (5 slots for eggs, sequencer discs), water volume, rotating items. Item display states live in the render state (`ItemStackRenderState`) `[v]` |
| Cultivator water | `omogwgxr/vtclb.java:21-41` | A custom translucent pipeline for one render type: depth test less-or-equal, no depth write, sort on upload. It copies the vanilla translucent entity pipeline and changes only depth `[v]` |
| Genome editor hologram | `omogwgxr/jgdp.java:38-326` | Creates a real creature entity per block position and draws it with the entity dispatcher at full light and zero rotation. Keeps at most 16 cached entities, pruning those whose block entity is gone. Scales the animal so its largest side is at most 0.7 blocks and its height at most 0.6 `[v]` |
| Skeleton display | `stta/hmocea.java:335-470` | Draws the skeleton model for the pose, with eye overlays and part masks. Model layers are cached in maps keyed by layer |
| Skull display | `stta/wtii.java:351-418` | One skull model, rotated, scaled 2 |
| Item holder | `stta/incf.java:156-184` | Draws one item, bobbing |

All of these use the 26.x extract-then-submit steps (`extractRenderState` fills a state, `submit` queues geometry) `[v]`.

**Platform overlap.** This is vanilla 26.x block-entity rendering. `render_pipeline_mods.md` in the earlier survey covers Sodium's treatment of block entities.

**Verdict.** Adapt for B12: one machine renderer class that reads a registry of (model, texture, facing mode) records, built once. Drop the hologram trick (create a real entity for display) for Nublar until a module needs it. Reason: an entity per display block is a hidden cost. The cap of 16 shows JC knew.

---

## 9. Particles and sounds

- Particles: three types only. Two use an upward-drifting, fading quad (helicopter exhaust and downwash, lifetime 8 / (0.2 to 1.0) ticks, i.e. 8 to 40 ticks, translucent). One is a falling opaque drop with no physics, lifetime 1 / (0.2 to 1.0) ticks (1 to 5 ticks), used for washing `[v]` (`rqkeevhve/dnukfuguy.java:14-62`, `rqkeevhve/vtclb.java:88-119`). They register through `RegisterParticleProvidersEvent` `[v]`. They are light.
- Sounds: 2,491 files (47.2 MB), 558 sound events, 2,519 entries `[v]`. T. rex has 9 events; the "living" (ambient) event alone has 28 variants. Each creature plays a species "breath" sound every 62 ticks while alive `[v]` (`omogwgxr.java:1379-1387`). Ambient interval is 200 ticks `[v]`. Sound distance for footstep, roar and call events is 16 or 24 blocks times max(volume, 1), tested by squared distance on the client `[v]` (`omogwgxr.java:1428-1475`).
- Sound volume and pitch come from growth: volume 0.2 to 1.0 and pitch 2.5 to 1.0 from infant to adult (`omogwgxr.java:1394-1411`).

**Verdict.** Adapt the client-run, tick-tagged sound events (section 6.5) for B09 and B16. Reason: no sound packets.

---

## 10. Server cost

**In game.** A park keeps its tick rate if few creatures hunt at once. A creature cannot spawn freely until the lead turns natural spawning on.

### 10.1 What a creature does every tick (server)

From `tick()` and `customServerAiStep` `[v]` (`omogwgxr.java:1570-1620`, `ctygd/gjtxsudo.java:79-111`):

1. Vanilla `Mob` tick and AI (goal selectors, navigation, move control).
2. Freeze body and head rotation while asleep or dead.
3. Airborne counter for fall behaviour.
4. Every 8 ticks: age value check.
5. Poison and stun timers; speed-up and hurt-flash timers (counters only).
6. Needs: a fractional energy and water drain per tick (no loops, no scans). Starvation damage check every 40 ticks (5% max health) `[v]` (`gyovxqy/gjtxsudo.java:121-157`).
7. Breeding timer countdown and animation start `[v]` (`gyovxqy/kodkieb.java:157-200`).
8. Sleep and rest animation choice; breath sound every 62 ticks.
9. The animation controller advance and server events.
10. Dirty-flag or every-40-ticks refresh of attributes and dimensions.
11. If not in a wall: nudge out of a wall. In a wall check: `isInWall` is overridden to false, and a separate check runs `moveTowardsClosestSpace` for non-passengers `[v]`.
12. Leaf breaking (see 10.4).
13. Part update (section 2.3).

Nothing is throttled by distance from a player. Nothing is spread across ticks by entity id. `removeWhenFarAway` returns true only for naturally spawned animals that no one owns `[v]` (`omogwgxr.java:208-210`). A naturally spawned unowned corpse removes itself: every 600 ticks it loses one "break step", then is discarded `[v]`.

### 10.2 AI gating (goals and sensors)

JC uses vanilla goal selectors (no Brain, no SmartBrainLib). 21 goals plus 4 target goals `[v]` (`ctygd/gjtxsudo.java:37-66`). Gates I found:

| Job | Gate | Source |
| --- | --- | --- |
| Hunt target pick | Runs on 1 tick in 10 (random). Skips while sitting, asleep, too young (growth <= 0.5), under a "just lost a target" cooldown (50 to 70 ticks), or not hungry (if `huntWhenHungry`) | `ztiurxyn/omogwgxr.java:25-60` |
| Hunt scan | One `getEntitiesOfClass` over a box of follow range x (follow range / 2) x follow range. Sorted by distance. At most 6 candidates get the reachability check. Candidate must have no more than 90% of the hunter's max health | `ztiurxyn/omogwgxr.java:95-113` |
| Target re-check | Every 40 ticks while hunting, a reachability check of the target | `ztiurxyn/omogwgxr.java:62-82` |
| "Is it safe to sleep?" | A cached answer, refreshed every 20 ticks: a scan of the box (10, 5, 10) around the sleeper for hostile larger creatures | `omogwgxr.java:522-570` |
| Herd goals | `getEntitiesOfClass` over a 16-block radius for same-species creatures (follow, count, breed limit) | `ztiurxyn/kdhoqn.java:47-50`, `fgwme.java:187-220` |
| Wander and graze goals | Random delays: for example 40 plus random 0 to 39 ticks, and scans of a few blocks | `ztiurxyn/cnxjsqwpw.java:35` |
| Block scans | 5 x 5 x 5 or 3 x 3 x 3 `betweenClosed` loops in some goals | `ztiurxyn/kodkieb.java:86,128` |

`maxHerdSize` (config 0 to 1,024, default 4) caps how many creatures of one species count within 16 blocks before breeding stops `[v]` (`dnukfuguy.java:22-23`).

### 10.3 Pathfinding caps (tickets B121, and the node cap)

Files: `incf/aeygzxj.java:16-165` (navigation), `incf/dnukfuguy.java:16-100` (node evaluator), `incf/vtclb.java:1-35` (move control).

- Visited-node budget: the search multiplies vanilla's node cap by `min(1, 200 / (max(follow range, 16) x 16))`. For follow range 64 the factor is 0.195, so the search visits about 200 nodes. For follow range 16 or less it stays 1.0 (256 nodes) `[v]` (`incf/aeygzxj.java:17,43-46`).
- One failed search per tick per creature: if a search ends with no path or an unreachable one, the navigation records the game time. A second search in the same tick returns "no path" at once and sets a "blocked" flag `[v]` (`incf/aeygzxj.java:48-69`).
- Direct-line fallback: when a path request fails or is blocked, and the species can "walk straight", the creature remembers the target and the move control steers straight to it. This ends when it is within the body width (min 1 block) or after 300 ticks `[v]` (`incf/aeygzxj.java:71-146`). The switch lives in the species data (`kijk().ummp()` flag) `[inf]`.
- Column cache (B121): the node evaluator caches the set of path types for each (x, y, z) column from the foot up to the body height, for the duration of one search. A body that is W wide and D deep reads W x D columns per node. Neighbour nodes share columns, so most reads hit the cache. The cache clears at `prepare` and `done` `[v]`. A dev switch `pathColumnVerify` compares every cached answer to vanilla's block-by-block answer and logs the node where they differ `[v]` (`incf/dnukfuguy.java:43-56`, `dnukfuguy.java:29`).
- Move control: when the creature is within 0.25 blocks (squared 0.0625) of the wanted point, it stops and jumps only if the wanted height is above the step limit, which removes small wobbling at the end of a path `[v]` (`incf/vtclb.java:109-134`).
- Last-node rule: with the "straight" flag set, the final waypoint check is a tight box (0.35 blocks sideways, 1 block vertical) instead of the body width `[v]`.

### 10.4 Per-tick block scan (a cost to avoid)

When `creaturesBreakLeaves` is on (default) and mob griefing is on, the land creature class loops over every block position in its body box every server tick and destroys leaves it overlaps. If it is blocked horizontally, it scans a box inflated by 0.2. It also sets the path cost for leaves to 8 (walkable but costly) or -1 (blocked) on the same tick `[v]` (`ctygd/gjtxsudo.java:79-123`). A Brachiosaurus box (about 7 x 8 x 7) holds roughly 500 block positions `[inf]`. The step-up check in `move()` also scans a slab of blocks each move `[v]` (`omogwgxr.java:1005-1055`).

### 10.5 Spawning and the own MobCategory

- `META-INF/enumextensions.json` adds a `MobCategory` named `jurassicraft:prehistoric`. It is built by a class-load-time proxy with values: max count 10, friendly true, persistent false, despawn distance 128 `[v]` (`core/JCMobCategories.java`, `enumextensions.json`). The metadata comment says this gives "land creatures a mob cap and a spawn schedule of their own rather than sharing vanilla's with the cows" `[doc]`.
- Water creatures use the vanilla water creature category `[v]` (`core/vtclb.java:171`).
- Natural spawning is off by default ("as in 1.12"; hybrids never spawn naturally). It takes effect on world load `[v]` (`dnukfuguy.java:17`).
- When on, a biome modifier adds a spawner entry for each species whose biome tags match. The group maximum is max(1, min(6, herd size / 2)) `[v]` (`axvg/fgwme.java:42-44`). The spawn weight is the species `spawn_chance` (min 1, most common 10, then 7 and 5; max 15) `[v]` (species files).
- Placement: on ground (land) or in water (water creatures), height map "motion blocking, no leaves", and only in the Overworld `[v]` (`axvg/onzkovhp.java:25-50`).
- Naturally spawned creatures remember their spawn reason, so they can despawn when far. Owned or lab-made creatures never despawn `[v]` (`omogwgxr.java:203-210`).

### 10.6 World generation and slow global ticks

- Features: 9 ore and fossil features and 7 vegetation features. Every placed feature starts with a custom `config_filter` placement modifier. It reads a boolean from the config (for example `fossil_generation`, `peat_generation`), so a server owner can switch a feature off without a datapack `[v]` (`data/.../worldgen/placed_feature/*.json`).
- Examples: fossil ore 44 attempts per chunk between Y -60 and 64; amber 8 per chunk between Y 0 and 19; trackway one in 20 chunks (rarity 20) between Y 30 and 50; nest fossil site one in 100; moss one in 8 with 7 attempts; petrified log 1 per chunk `[v]`.
- Tree features: custom code. Heights 4 to 19 blocks, with leaf rings built per level (`jdbttmo/ummp.java:12-80`) `[v]`.
- Structures: 4 (fossil digsite, visitor centre, raptor paddock, geneticist house) from 6 NBT templates (216 KB total). Spacing: digsite 48 / 16, visitor centre 448 / 320, geneticist house 48 / 16 with a 4-chunk exclusion from the paddock. The paddock uses a custom placement type with a config rarity: one per 4,000 chunks by default, range 50 to 16,777,216 `[v]` (`worldgen/structure_set/*.json`, `dnukfuguy.java:133`).
- Peat spread: once every 20 ticks per level, the mod picks one random player, then one random chunk within that player's view distance (if loaded), then one random column. It searches down from the height map for dirt, coarse dirt or podzol under grass, and changes one block `[v]` (`core/gjtxsudo.java:21-77`). That is one block per second per level at most. The `peatGenerationSpeed` setting adjusts the rate (0 to 64, default 20) `[v]`.

### 10.7 Platform overlap and verdicts (server)

| Technique | Overlap | Verdict |
| --- | --- | --- |
| Hunt scan gate: 1 in 10, 6-candidate check, 50 to 70 tick cooldown | Vanilla `TargetGoal` has similar gates | Adopt as the baseline numbers for B19 and B25 |
| Pathfinding cap of about 200 nodes, one failed search per tick, direct-line fallback up to 300 ticks | None | Adopt for B26 (large bodies need it) |
| Column cache for body footprint | Vanilla `WalkNodeEvaluator` has no such cache | Adopt for B26. Keep the vanilla comparison switch |
| Age by timestamp with a skip-aware clock | None | Adopt for B16 |
| Own `MobCategory` through enum extension | NeoForge supports it | Adopt for B16 (see open question 1) |
| Config-filter placement modifier | None | Adopt for B01 |
| One-block-per-second slow global tick | None | Adopt as the model for B25 slow jobs |
| Per-tick leaf-break scan | None | Drop. Use the B25 queue (8 per tick) |
| No distance throttle on creature tick | None | Drop. B25's band system is better |
| Species definitions all synced | NeoForge datapack registry sync | Adapt (split client and server parts) |

---

## 11. Config surface that affects performance

All of this is from the three config specs in `dnukfuguy.java:6-140`. Each option has a short player-facing comment `[v]`.

| Side | Option | Default | Effect |
| --- | --- | --- | --- |
| Client | `fastVertexWrites` | true | Section 1.3 |
| Client | `genomeEditorHologram` | true | Section 8 |
| Client | `logModelLoading` | false | Logs bone counts |
| Client | `helicopterZoomOut` | false | Camera |
| Common | `partSpatialIndex` | true | Section 1.1 |
| Common | `partIndexVerify` | false | Dev check |
| Common | `pathColumnVerify` | false | Dev check |
| Common | `maxHerdSize` | 4 (0 to 1,024; 0 = per species) | Herd cap |
| Common | `creaturesBreakLeaves` | true | Section 10.4 |
| Common | `naturalSpawning` | false | Section 10.5 |
| Common | `plantSpreading` | true | Slow tick |
| Common | `creatureGrowth`, `creatureMetabolism`, `creatureFoodRequirement` | true, true, 1.0 | Needs and growth |
| Common | Feature toggles (9 minerals, 5 plants, 4 structures), `raptorPaddockRarity` 4,000, `lootSpawnrate` 100 | true | World gen |

JVM properties (dev switches, not config): `jurassicraft.vertexWriteCount`, `jurassicraft.singlePassColorOff`, `jurassicraft.overlayCoverageAudit`, `jurassicraft.poseReplayVerify`, `jurassicraft.breakPoseReplay`, `jurassicraft.breakPartIndex`, `jurassicraft.validateAnimationsOnLoad` `[v]`. The pattern is consistent: each optimisation has an off switch, a verifier and a "break it" hook that proves the verifier works.

---

## 12. Asset counts

From file listings of `raw/` (my counts):

| Asset | Count | Notes |
| --- | --- | --- |
| Species | 129 | 101 land, 28 water |
| Species JSON | 129 files, 764 KB | Synced to clients |
| Creature geometry | 129 | 49 to 346 bones, median 108; 15,840 cubes total |
| Egg geometry | 48 | |
| Skeleton geometry | 225 | |
| Skull display geometry | 4 | |
| Animation clips | 1,780 | 26.6 MB; 5 to 21 per species |
| Entity textures | 2,258 files | 6 species lack a packed sheet |
| Item textures | 3,388 | |
| Block textures | 449 | |
| Other textures | gui 85, paddock 129, attraction sign 80, mural 15 | |
| Textures, total | 6,405 files, 41.2 MB on disk | |
| Models (JSON) | 5,320 files, 1.8 MB | item 4,039, block 1,240, fence 22, machine 16, vehicle 3 |
| Blockstates | 371 | |
| Item definitions | 537 | |
| Sounds | 2,491 files, 47.2 MB; 558 events | |
| Particles | 3 | |
| Shader files | 2 | One core shader (vertex and fragment) |
| Data: recipes / loot tables / advancements | 577 / 382 / 466 | Loaded on every server start |
| Data: villager trades | 47 | |
| Data: worldgen | 43 files | 19 configured features, 15 placed features, 4 structures, 4 structure sets |

Per species (for the T. rex): 1 geometry, 17 clips (attacking, calling, drinking, dying, eating, injured, laying egg, looking left and right, resting, roaring, running, sleeping, speak, swimming, walking), 21 texture files, 9 sound events, 13 default hitboxes plus 5 pose lists.

---

## 13. Project Nublar fit

Each row maps one technique to a plan module and gives a verdict (adopt / adapt / defer / drop) with a reason.

| Technique | Source section | Module | Verdict | Reason |
| --- | --- | --- | --- | --- |
| Part spatial index: section buckets, pad 4 sideways and 12 down, 1,024-section cap, count cross-check and rebuild | 1.1, 2 | B27 (and B25 for the test) | Adopt | Matches the accepted design in the hitbox spec. JC shows it works at 129 species. Add: index foreign parts, a verifier switch, and a break hook |
| Verifier switches plus a "break it" hook for every optimisation | 1.1, 6.4, 11 | B25 | Adopt | Proves each verifier fires. Costs nothing when off |
| Data-driven hitboxes per pose (named boxes, offsets, head pivot, blend ticks with start/hold/end) | 2.4 | B27, B00 | Adopt | Same data fits section parts. Use the JC file shape as the starting schema |
| Part counts 3 to 20 (median 7) | 2.4 | B27 | Note | Nublar plans 11 to 17. Expect about twice the JC cost per creature. Profile at load B (100 dinosaurs) |
| Parts shrink to 0.05 when a pose list is shorter | 2.2 | B27 | Drop | The plan's "distance-based part counts with the same objects and IDs" is better |
| Damage forwarded to the parent with no multiplier | 2.5 | B27 | Adapt | Add head x1.5, neck x1.25, tail x0.5 per the plan |
| Parent box not pickable; parts push players | 2.5 | B27 | Adopt | Same as the plan |
| Union box (body plus parts) as culling box | 7 | B16 | Adopt | Fixes the Brachiosaurus neck bug the plan lists |
| Presized visited set mixin | 1.2 | B26 | Adapt | Needs a profile first. B25 forbids tick mixins on vanilla bases, so ask the lead |
| Vertex write fast path with `Unsafe` | 1.3, 1.4 | B25, P2 | Defer | Measure on 26.2. `Unsafe` memory access is on a removal path. A bake lowers vertex count already |
| Packed alpha-coded pattern sheet | 5.2 | B09, B24 | Adapt | Packing three patterns offline helps the CPU bake too |
| Tint-table single-pass shader | 5.2, 5.6 | B09 | Defer | Needs a lead decision against OC6 (no core shader). Test with Iris on 26.2 first |
| Overlay bone masks from an async PNG scan | 5.4 | B09 | Adopt | Shader-free. Cuts overlay cost. Log missing bones |
| Pose replay: compute once, apply to overlay passes | 6.4 | B09 | Adapt | Check whether GeckoLib 5 already shares one pose across layers. If yes, drop |
| Vanilla keyframes with NeoForge JSON loader | 6.1 | B09 | Drop | Nublar keeps GeckoLib 5 |
| Server-owned (name, start tick) animation state, 4-tick crossfade | 6.5 | B27, B16 | Adopt | Same as the plan's synced pose state |
| Client-run tick-tagged sound events | 6.5, 9 | B16 | Adopt | No sound packets |
| Walk/run blend by real speed (thresholds 0.05 and 0.2 for T. rex) | 6.3 | B09 | Adapt | Fits the plan's "gait choice from actual speed" |
| Procedural suppression while an action plays | 6.3 | B09 | Adopt | One number per species (0.75 default) |
| Throat-follows-neck, missing-bone log with available list, validate on load | 6.6 | B09, B24 | Adopt | Turns art errors into log lines |
| No animation LOD in JC | 6.7 | B09 | Note | No precedent. B09's own distance rule stands and needs a test |
| Hologram that creates a real entity | 8 | B08 | Drop | Not needed now. Hidden cost |
| Machine renderers: view distance 48, one class, records per model | 7, 8 | B12 | Adapt | Reasonable first numbers |
| Age as timestamp plus skip-aware growth clock | 3.3 | B16 | Adopt | No per-tick age sync. Survives time jumps |
| Full species registry synced to clients (764 KB) | 3.1 | B00 | Adapt | Split client and server parts |
| Own `MobCategory` by enum extension (max 10, friendly, not persistent, despawn distance 128) | 10.5 | B16 | Adopt | Own cap and schedule. Check the supported enum route on 26.2 (P2) |
| Hunt scan gate 1 in 10, 6 candidates, 50 to 70 tick cooldown | 10.2 | B19, B25 | Adopt | Good baseline numbers |
| Path node cap (about 200), one failed search per tick, 300-tick straight-line fallback | 10.3 | B26 | Adopt | Large bodies make searches costly |
| Column cache for body-footprint path types | 10.3 | B26 | Adopt | Keep the vanilla comparison switch |
| Per-tick leaf-break scan of the whole body box | 10.4 | B25 | Drop | Use the 8-per-tick queue |
| No distance throttle for creature tick | 10.1 | B25 | Drop | B25's band system is better |
| `config_filter` placement modifier for world gen toggles | 10.6 | B01 | Adopt | Server owners switch features off without a datapack |
| One-block-per-second global slow tick (peat) | 10.6 | B25 | Adopt | Pattern for slow world jobs |
| Counts: 129 species, 1,780 clips, 6,405 textures, 2,491 sounds | 12 | Planning | Note | Scale reference for Release 1 asset budgets |

---

## 14. Open questions

1. **Is the vertex-write allocation claim true on 26.2?** The only source is JC's own metadata. A profile of 100 creatures on the 26.2 JDK answers it. Home: B25.
2. **Does OC6 (no core shader) allow one creature shader?** JC proves single-pass with a tint table works. It has no shader-pack test that I can see. The lead should decide whether to test it, and with which shader packs. Home: B09.
3. **Does GeckoLib 5 already evaluate animation once per creature across layers?** If yes, pose replay is not needed. Read the GeckoLib 5 render path at port time. Home: B09.
4. **What exactly does `forEachBlockIntersectedBetween` serve in 26.2?** I inferred the call site. Confirm it is on the creature hot path before adopting the presized set. Home: B26.
5. **Does the head-tracking chain in JC use fixed clamps per species?** I could not decode the T. rex values from the bytecode. Not needed unless B27 wants JC's numbers.
6. **What does JC's `isInWall` override cost?** It returns false, then a second check runs. I did not trace the reason. Low priority.
7. **Species client/server split.** If the lead keeps the full species file on the client, the 764 KB login cost stays. The lead should state a limit for Release 1 (R02).
8. **Part ids vs 26.2.** Nublar's notes say NeoForge 26.2 removed the Forge id patch. JC targets 26.1.2 and sets ids with the reserved block rule. I did not read 26.2 code here.
