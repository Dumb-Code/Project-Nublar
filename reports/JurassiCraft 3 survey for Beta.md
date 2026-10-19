# JurassiCraft 3 confirms Nublar's plan and adds cheap rules for food search, growth and large-body paths

Date: 2026-10-11. Status: research report for the project lead. It changes no code and no plan module.

JurassiCraft 3.0.14 (JC3) is a shipping Jurassic Park mod on NeoForge 26.1.2, the closest platform to Nublar's 26.2 target. It has 129 species, a cloning chain, vehicles, fences and feeders. JC3 confirms many planned Nublar choices: a server-owned animation state, part hitboxes with a chunk-section index, edge-only machine sync, species as strict data and natural spawning off. Its best new lessons are about cost. A food or water search waits with exponential back-off, skips failed targets for 600 ticks and wakes early only when a nearby chunk changes. A world clock counts skipped night time, so animals grow with no per-tick age work. Large bodies get a path node cap, one failed search per tick and a column cache. JC3 has none of Nublar's signature systems: stamina chases, stress, dominance, scent, sound detection, herd travel, real power, wire health, gene editing beyond colour, driverless tours. Its one-pass colour shader breaks Nublar rule OC6, so it needs a lead decision. This report rates 147 techniques against modules B00 to B27 and P2, settles 28 disagreements between the five notes against the source, and keeps every lead decision. It gives 23 clean-room specifications (J1 to J23) and 7 open items (O22 to O28).

**Labels.** `[v]` means verified in the decompiled source or the data files, by a note or again for this report. `[inf]` means inference. `[doc]` means text that the mod ships: lang strings, config comments, metadata comments. Every cost figure without `[v]` is an estimate. Nobody ran JC3 or profiled a 26.2 build.

**Names.** "The plan" is [project-plan.md](../docs/project-plan.md). "Section 11" is section 11 of [codebase-analysis.md](../docs/codebase-analysis.md). "The entity survey" is [Reference mod survey for Beta.md](Reference%20mod%20survey%20for%20Beta.md). "The tech survey" is [Optimization and tech mod survey for Beta.md](Optimization%20and%20tech%20mod%20survey%20for%20Beta.md). Their row ids (B17.11, B22.8), specifications (S1 to S27, T1 to T15), lessons (OS1, OC6) and open items (O1 to O21) keep their meaning here. New row ids continue each module's numbering, for example B17.17. Modules with no earlier rows start at .1, for example B01.1. New specifications are J1 to J23. New open items are O22 to O28. "The bake spec" is [2026-10-08-dino-layer-baking-design.md](../docs/superpowers/specs/2026-10-08-dino-layer-baking-design.md). "The notes" are the five files in `research_notes/JurassiCraft 3 survey/`. This report cites them by short name and section:

- "AI note" is `ai_and_behaviour.md`
- "genetics note" is `genetics_species_and_cloning.md`
- "vehicles note" is `vehicles_and_tech.md`
- "performance note" is `performance_and_rendering.md`
- "gameplay note" is `gameplay_content_and_world.md`

**Library hooks still come first.** The lead's rule of 2026-10-10 applies to every row. Where SmartBrainLib 2 (SBL), GeckoLib 5 or Balm gives a hook, Nublar calls it. JC3 uses vanilla goals and vanilla keyframes, so most of its code has a library counterpart in Nublar. The specifications below describe only behaviour, numbers and order. They name the library hook to use where one exists.

## Twelve changes add cheap search, growth and path rules

The twelve highest-value changes from JC3, in recommended order. Effort: S is under 100 lines, M is one or two classes with tests, L is a new subsystem. The ten changes of the entity survey and the twelve of the tech survey stay in force. This list adds to them.

| Rank | Change | Module | Effort | In-game effect | Precedent |
| --- | --- | --- | --- | --- | --- |
| 1 | Need searches that sleep until something changes: retry delay doubles from 20 to at most 600 ticks, a 64-slot list skips failed targets for 600 ticks, a change in a nearby chunk wakes the search early, at most 6 path tests per search (J6) | B17, B20, B25 | M | A hungry Triceratops with no food in reach waits twice as long after each failed search, up to 600 ticks (30 s). It searches at once when a player drops food or places a block within its range. An unreachable bush costs no path test for 30 s | JC3 eat and drink goal base, failed-target list, dirty-chunk map `[v]` (AI note §7). Extends S8 and B17.11 |
| 2 | Growth clock that counts skipped time. The animal stores a birth stamp, not an age counter (J4) | B16 | S | A night skipped by sleeping, or `/time set`, still grows the animals. No age value goes to clients per tick | JC3 growth clock and birth stamp `[v]` (genetics note §8, performance note §3.3) |
| 3 | Path caps for large bodies: a per-search node cap from species data, one failed search per animal per tick, a per-search column cache for the body footprint (J16) | B26 | M | A Brachiosaurus search visits at most about 200 nodes, not 1,024. A boxed-in animal fails once per tick, not several times | JC3 ground navigation and node evaluator `[v]` (AI note §14, performance note §10.3) |
| 4 | Part index self-checks: a count check that rebuilds the buckets, foreign parts in the index, a verify switch and a break hook (J18) | B27, B25 | S | A missed part add or remove heals on the next entity query. An Ender Dragon in the same level does not turn the index off | JC3 part index `[v]` (performance note §1.1). Confirms the hitbox design |
| 5 | Pose state flags and event ticks: exclusive and hold flags per clip, server and client events at fixed ticks, a 4-tick crossfade, procedural sway that fades while an action plays (J19) | B27, B16, B19 | M | A roar cannot be cut off by a look-around. Each client starts the roar sound from the synced start tick, with no sound packet. A Dilophosaurus spit leaves the mouth at tick 20 of the clip | JC3 animation state machine `[v]` (AI note §16, performance note §6.5). Confirms the planned pose state, S26 and S27 |
| 6 | Driving from vanilla input: the server reads movement keys from the input that vanilla already sends. Only extra keys use a Nublar packet, sent when they change (J14) | B22 | S | The Jeep needs no drive packet of its own. A parked car sends nothing | JC3 vehicle control `[v]` (vehicles note §6). Confirms S12 and T15 |
| 7 | Overlay bone masks: a pass for an overlay or glow texture draws only the bones whose UV boxes hold pixels (J20) | B09 | S to M | The glowing eye pass of a dinosaur draws only its head bones, not the whole model | JC3 coverage scan `[v]`. A median JC3 creature submits about 6 passes, against 23 for Nublar's Gallimimus today (performance note §5.4) |
| 8 | Species and art guard rails: strict validators, one log line per missing species, clip or bone, and a rule that every species key has a reader and a test (J1, J23) | B00, B24, B25 | S | A broken pack or model gives one clear log line that names the file or bone. No species key exists that no code reads | JC3 ships six species keys that no code reads `[v]` (AI note §4, §19) |
| 9 | Carcass stages from body size, meat that carries the genome, scavengers that bite the carcass (J5) | B16, B04, B17 | S | A Velociraptor carcass gives 2 cuts, a T. rex 6 and a Brachiosaurus 10. Raw meat from a prized animal carries its genome to the lab. A hungry carnivore eats from a carcass | JC3 carcass code `[v]` (AI note §12, genetics note §4.3). First precedent for the entity survey's feature 9 |
| 10 | Machine idle early-out and a 16-bit time cap: an empty machine skips its recipe check, and no recipe runs longer than 32,767 ticks | B12 | S | An idle lab costs a few slot reads per machine per tick. A progress bar never wraps for a long run (27 min at most) | JC3 machine base and recipe codec `[v]` (vehicles note §12.2, §13) |
| 11 | Food values as tag tiers: five tiers from cheap leaves to compact bales, with defaults for untagged items and blocks (J6) | B17, B20 | S | Leaves fill a little, crops more, cycads and hay much more, and bales most. A pack author moves a food between tiers with a tag | JC3 feed tiers 300, 750, 1,500, 3,000 and 13,500 `[v]` (AI note §7) |
| 12 | A verifier for every fast path: an off switch, a compare switch and a break hook, plus a `turbo` command for machine tests (J13) | B25, B12 | S | A tester proves that each shortcut gives the same answer as the slow path. A machine test runs each job in 1 s | JC3 config and JVM switches `[v]` (performance note §11, vehicles note §20) |

Ranks 1, 3 and 4 remove server waste. Ranks 2, 5 and 6 remove packets. Rank 7 recovers client frame time after the bake. Ranks 8, 9, 11 and 12 add play or safety at almost no cost.

## JC3 confirms 16 Nublar decisions and contradicts 12

**In game:** nothing changes here. Each row compares a Nublar decision or plan item with what JC3 does. A lead decision always wins. A conflict that needs a new answer is an open item.

### Confirmed

| # | Nublar decision or plan item | JC3 evidence | Note |
| --- | --- | --- | --- |
| 1 | Dinosaurs never spawn naturally (global constraint) | `naturalSpawning` is off by default, "as in 1.12" `[v]` `[doc]` | Same choice |
| 2 | No fuel (decision D07) | No fuel item and no fuel field in any vehicle class `[v]` | Same choice |
| 3 | A synced pose state owned by the server: id, start tick, loop flag (B27) | Two synced fields, name and start tick. Late joiners see the clip at the right point `[v]` | JC3 adds exclusive and hold flags and events (J19) |
| 4 | Part ids as `parentId + i + 1` from a reserved block (B27) | Same rule `[v]` | Same choice |
| 5 | Part buckets per chunk section with a `Level.getEntities` mixin (B27) | One mixin wraps the same three `Level` queries `[v]` | JC3 adds self-checks (J18) |
| 6 | Parent box not pickable. Parts forward the same damage source (B27) | Same `[v]` | Nublar adds damage multipliers |
| 7 | Culling box from the body and parts (B16, tech survey rank 4) | Union of body box and part boxes `[v]` | Same choice |
| 8 | Progress through `ContainerData`, block updates only on a status edge (B12.1, B12.11) | Same. A running machine sends no packets `[v]` | Same choice |
| 9 | Per-level feeder registry (B17.1) | Feeders join a per-level map keyed by chunk `[v]` | JC3 still scans every feeder per query. See B20.17 |
| 10 | Diets as tags (B17) | Food and feed tiers are tags `[v]` | Same choice |
| 11 | Species as data with loud errors (B00) | Strict codec validators, one log line per missing species `[v]` | J1 |
| 12 | A hatchling starts at growth 0 (B11) | A hatched egg spawns an infant at age 0 with full needs `[v]` | Same choice |
| 13 | Variation rolls at hatch for lab clones (lead, 2026-10-06) | Fossil DNA carries no traits. Each clone rolls its traits at hatch `[v]` | Same choice |
| 14 | Tracking interval 1 for vehicles only (B22.8) | Vehicles: interval 1, range 12 chunks. Creatures: default interval, range 10 `[v]` | Same choice |
| 15 | No sleep through damage: a 200-tick wake lock (S7) | Damage sets a 200-tick sleep lockout `[v]` | Same value |
| 16 | Villager trades as 26.x registry data (S17, B21.5) | Trades, trade sets and trade tags as data per level `[v]` | A working 26.x example |

### Contradicted

| # | Nublar decision or plan item | JC3 | Result |
| --- | --- | --- | --- |
| 1 | No core shader (OC6). The bake spec composites layers on the CPU (B09) | One custom core shader draws four tints in one pass from a shared tint table `[v]` | Lead decision needed. Open item O22 |
| 2 | Starvation: 1 HP per 80 ticks at zero hunger. Seek food below 75 %, run below 25 % (lead, B17) | 5 % of max health per 40 ticks below 20 %. Ladder 80 / 75 / 70 / 30 / 20 % `[v]` | Lead rule stands. B17.25 Drop |
| 3 | A live wire does 1 HP to anything, both tiers (F25) | Low tier 1.0, medium tier 2.0 damage per contact tick `[v]` | Parity stands. B15.24 Drop |
| 4 | Any living entity climbs an unpowered wire, nobody climbs a live one (F26, D38) | Players and climbing species climb, powered or not `[v]` | Parity stands. B15.25 Drop |
| 5 | A shock does damage and knockback only (superseded deterrence decision, 2026-10-10) | A shocked creature runs from the fence and stops herding for 200 ticks `[v]` | Parity stands. B15.26 Drop |
| 6 | Real power from generators (B14, F23, F30) | No power system. Fences run on a redstone signal at a base `[v]` | Plan stands. B14.21, B15.23 Drop |
| 7 | Heredity: midpoint of the parents plus per-clone variation (lead, B18) | A random value between the parents, re-rolled one time in three `[v]` | Lead rule stands. JC3 shows why a variance test helps (B18.16) |
| 8 | Gender is a gene: sure at 80 %, 30 % random below (lead, B07) | A temperature slider per incubator slot sets the male chance `[v]` | Lead rule stands. Open item O26 asks only whether a lever is wanted |
| 9 | Track on vanilla `BaseRailBlock` and `RailShape` (B22.17) | Its own 20-shape rail with diagonals and an own auto-shape class `[v]` | Plan stands. B22.39 Drop |
| 10 | Jeep step height 1.0 to 1.25 (B22.23) | 1.5 on every vehicle `[v]` | Plan stands. Treat 1.5 as the upper bound |
| 11 | No tick mixin on a vanilla base class, no global per-entity hook (B25.5) | A required mixin presizes the visited-block set in a vanilla sweep method `[v]` | Lead decision needed. Open item O23 |
| 12 | No world-compat shims for renamed ids (global constraint) | A registry alias migrates an old block id `[v]` | Constraint stands. P2.4 Drop |

**New evidence for open defaults.** JC3 ships a breeding cap of 4 animals within 16 blocks as its default `[v]`. Nublar's decision D20 default is 0 (no cap). JC3 gives a hatchling an owner when the player hatches it without sneaking `[v]`. Nublar's D17 default is no owner. Neither finding changes a default. Both are evidence for the lead when D20 and D17 come up.

## The notes disagree in 28 places, and the source settles each

**In game:** nothing. Each row settles a fact before the rest of this report uses it. This report checked rows 1 to 24 again in the decompiled source or the jar contents. Rows 25 to 28 settle verdicts that differ between notes.

| # | Topic | The notes say | Source check | Settled fact |
| --- | --- | --- | --- | --- |
| 1 | Fence power flood | Vehicles note: a flood on every neighbour update, "no cap and no debounce". Gameplay note: "a bounded flood fill (radius 8)" | A neighbour update on a base or pole floods connected same-tier bases inside a 16-block horizontal circle, one block per hop with a height step of −1 to +1. Bases within 8 blocks then update their poles and wires. A wire update runs one search from its own base, limited to 8 blocks. No tick and no block entity `[v]` | Bounded in area (16 blocks for the flood, 8 for power), event-driven, with no tick. It has no debounce: every neighbour update of a base or pole repeats the whole flood, so placing a fence line runs one flood per block |
| 2 | Fence power source | Vehicles note: a redstone signal at the base. Gameplay note: a pole that receives the signal | The source test reads `hasNeighborSignal` at the base position, and needs a same-tier pole directly above `[v]` | A redstone signal next to the base powers the fence. The pole above is a condition, not the receiver |
| 3 | `processing_time` unit | Gameplay note: ticks, inferred | The ticker runs every server tick, and each running tick adds 1 to progress. The codec range is 1 to 32,767, default 200 `[v]` | Ticks |
| 4 | Data file count | Brief: about 10,000 data JSON. Gameplay note: 1,874 data JSON and 5,320 model JSON | `data/` holds 1,874 JSON and 6 NBT files. `assets/` holds 8,419 JSON, of which 5,320 are model files and 1,780 are animation clips `[v]` | 1,874 data JSON. About 10,300 JSON in all, which explains the brief's estimate |
| 5 | Recipe count | Vehicles note: 478. Gameplay note: 577 plus 2 vanilla overrides | 577 recipe files under `data/jurassicraft/`, 2 under `data/minecraft/` `[v]` | 577 mod recipes and 2 vanilla overrides |
| 6 | Texture count | Performance note: 6,405 files. Gameplay note: 6,393 | 6,405 files: 6,393 PNG and 12 `.mcmeta` `[v]` | Both are right. 6,393 images |
| 7 | Placed features | Performance note: 15. Gameplay note: 16 | 16 placed features, 19 configured features, 4 structures, 4 structure sets `[v]` | 16 |
| 8 | Config file of creature switches | Performance note: `partSpatialIndex`, `maxHerdSize` and others in "Common". Gameplay note: Server | They sit in the spec that registers as the server config `[v]` | Server config. 41 options: 4 client, 1 common, 36 server |
| 9 | Fossil depth bands | Genetics note estimate: Quaternary 1 layer, Cretaceous 28. Gameplay note: Quaternary 2, Cretaceous 27 | The band setup gives every banded period a fractional remainder, the Quaternary included. The 4 leftover layers go to Devonian (0.97), Paleogene (0.97), Quaternary (0.90) and Triassic (0.69) `[v]` | Quaternary 2, Neogene 7, Paleogene 15, Cretaceous 27, Jurassic 19, Triassic 18, Permian 16, Devonian 21 |
| 10 | Carcass stage formula | AI and genetics notes: `floor(√(w×h)) × 2`. Gameplay note: `√(w×h) × 2`, cast before the clamp | The cast applies to the square root only `[v]` | `clamp(floor(√(width × height)) × 2, 1, 10)`: Velociraptor 2, T. rex 6, Brachiosaurus 10 (14 before the clamp) |
| 11 | Quality of DNA in raw meat | Genetics note: the animal's quality. Gameplay note: a random quality | The meat record copies the animal's quality, genome string, traits and colours `[v]` | The animal's own quality |
| 12 | Carcass decay | AI note: wild unowned carcasses decay. Performance note: naturally spawned unowned | Decay runs only for animals that spawned naturally, from chunk generation or from a spawner, and have no owner `[v]` | A lab-made carcass never decays by time. Only breaking removes it |
| 13 | Ways to own an animal | AI note: hand-feeding only, "no other way". Genetics and gameplay notes: hatching | The hatched egg sets the user as owner when the player does not sneak and the species is imprintable `[v]` | Two routes: hatch without sneaking, or a 10 % roll when hand-feeding an infant |
| 14 | Range of the set-order packet | AI note: 64 blocks. Vehicles note: 8 blocks | The check compares the squared distance with 64 `[v]` | 8 blocks |
| 15 | Feeder lookup | AI note: a query scans all feeders of the dimension. Vehicles note: a per-chunk map with a radius lookup | Feeders are stored per chunk, but each query streams every feeder in the level, filters by distance and sorts `[v]` | Chunk-keyed storage with a whole-level scan per query |
| 16 | Cultivator nutrients | Genetics note: four bars. Vehicles note: six levels | The nutrient record and enum have four entries: proximates, minerals, vitamins, lipids `[v]` | Four |
| 17 | Goal counts | AI note: 28 goal classes and 3 helpers. Performance note: 21 goals plus 4 target goals | The land creature adds 27 goal-selector entries (22 behaviours and 5 idle emotes) and 4 target goals. The goal package holds 31 classes `[v]` | 27 goals and 4 target goals on a land creature |
| 18 | Clip set of "one big carnivore" | Performance note: roaring 20, eating 52, attacking 22 for a big carnivore | That set has `prepare_leap` and `leap`, so it is the raptor-type set. The T. rex set has roaring 125, eating 166, attacking 25 `[v]` | The quoted set belongs to pouncing raptors |
| 19 | Hunt cooldown | AI note: 50 to 69 ticks. Performance note: 50 to 70 | Game time plus 50 plus a random value below 20 `[v]` | 50 to 69 ticks |
| 20 | Straight-line path fallback | AI note: for climbing species. Performance note: a "walk straight" species flag | The flag is the same species climb flag that the fence ladder reads `[v]` | Climbing species only |
| 21 | Tight final approach | AI note: a mode for eating and drinking. Performance note: tied to the straight-line flag | A separate mode that eat and drink goals switch on. Any new path switches it off `[v]` | Eat and drink only |
| 22 | Incubation time | Gameplay note: 3,600 ticks | The incubator uses the species `incubation_time` when the egg carries DNA, else the recipe time of 3,600 (genetics note §4.2) `[v]` | 700 to 11,200 ticks by species, median 3,800. Raptor 4,200 |
| 23 | PaleoPad age unit | Gameplay note: "days of 3,000 ticks" | Age counts one unit per 8 ticks. One day is 3,000 units (genetics note §8) `[v]` | One shown day is 24,000 game ticks |
| 24 | Mixin roles | Gameplay note: "2 vertex writers" | One client mixin redirects the vertex writes. The other only marks the creature draw scope `[v]` (performance note §1.4) | One writer and one scope marker |
| 25 | Verdict on the `Unsafe` vertex path | Genetics note: Drop. Performance note: Defer, measure | — | Drop. Nublar never builds an `Unsafe` path. B25 measures the JDK allocation claim once (new item 30) |
| 26 | Verdict on an own mob category | Performance note: Adopt | Nublar dinosaurs never spawn naturally, and persistent mobs do not count toward vanilla mob caps `[inf]` | Drop. B16 keeps `CREATURE` with persistence |
| 27 | Verdict on the shock-flee goal | AI note: Adapt | The lead removed shock deterrence on 2026-10-10 (B15 parity) | Drop |
| 28 | AI note question Q2 (climbers over fences) | Open in the AI note | The lead answered D38 on 2026-10-10: every living entity climbs an unpowered wire (F26) | Closed. No open item |

## Optimization: JC3 keeps cost flat with gates, caches and verifiers

**In game:** a park with many dinosaurs keeps its tick rate when nothing changes. Searches, paths and part queries cost little until a block, an item or an animal moves.

JC3 has no brain and no scheduler. All its savings come from small gates on vanilla goals (AI note §3). Each technique below cuts a cost that Nublar also has.

| Technique | What it saves | JC3 numbers | Nublar home |
| --- | --- | --- | --- |
| Exponential retry delay per need goal | Repeated searches with no food in reach | Base 20 (eat) or 40 (drink), doubled per failure, capped at 600, plus a random wait up to the delay `[v]` | B17, J6 |
| Failed-target list | Repeated path tests to an unreachable target | 64 slots per animal, 600 ticks each, refreshed on a new failure `[v]` | B17, B19, J6 |
| Dirty-chunk wake | A search that waits for its delay although food just appeared | A per-level map from chunk to the last change time. Block place, block break, an item drop and a machine slot change write it. Old entries go when the map passes 512 chunks `[v]` | B17, B20, J6 |
| Section palette pre-filter and shell search | Block reads in sections that hold no food | A section whose palette has no matching state costs nothing. At most 6 path tests per search `[v]` | B17 grazing, J6 |
| Path node cap | Long searches for big bodies | Node budget `min(1, 200 ÷ (max(follow range, 16) × 16))` of vanilla, so about 200 nodes at follow range 64 `[v]` | B26, J16 |
| One failed search per tick | Several failing searches by one animal in one tick | A second new search in the same tick returns "throttled" `[v]` | B26, J16 |
| Column cache | Repeated block reads for a wide footprint | Path types per footprint column, cached for one search, with a compare switch `[v]` | B26, J16 |
| Part index | A scan of every part hitbox in the dimension per entity query | 16-block section buckets, pad 4 sideways and 12 down, flat list above 1,024 sections, count check and rebuild `[v]` | B27, J18 |
| Birth stamp and growth clock | Per-tick age counting and age sync | Age = (clock − birth stamp) ÷ 8. One 8-byte packet when skipped time changes `[v]` | B16, J4 |
| Attribute and size refresh on change | Per-tick attribute writes | Writes only on a different value. Size refresh only above a 0.01-block change `[v]` | B16, B07.1 |
| Edge-only machine sync | Per-tick block updates | Progress through `ContainerData`. A block update only on a status change `[v]` | B12 (planned) |
| Overlay bone masks | Draw calls for bones with no overlay pixels | An async PNG scan per overlay texture `[v]` | B09, J20 |
| Pose replay | A second clip evaluation per overlay pass | 9 floats per bone stored once per frame, with a verify switch `[v]` | B09 (Defer, GeckoLib 5 may share the pose) |
| Single-pass tint table | One draw per colour layer | A 4 × 2,048 tint texture (32 KB) and one core shader `[v]` | B09 (Defer, O22) |
| Presized visited-block set | Hash-set rehashing in a large box sweep | Expected size = box volume, clamped to 1 to 8,192 `[v]` | B25, B26 (Defer, O23) |
| Verifier pattern | Silent bugs in fast paths | An off switch, a verify switch and a "break it" JVM property for the part index, the column cache and pose replay `[v]` | B25, J13 |

**What JC3 does not optimize.** It has no distance throttle on the creature tick and no animation level of detail `[v]` (performance note §6.7, §10.1). Every creature ticks fully at every distance. The tech survey's distance band (T9) and the entity survey's hold-pose LOD (rank 7) have no JC3 precedent. They stay Nublar's own work.

## Performance: JC3's costs show where Nublar must spend less

**In game:** JC3 runs 129 species with 3 to 20 part hitboxes each. A median creature submits about 6 draw passes. A full species set holds 1,780 clips and 47.2 MB of sounds.

### Server cost per creature

The creature tick does a fixed list of light steps (performance note §10.1). The table lists the steps that cost more than a few counters (AI note §18).

| Job | Cost | Throttle | Rating |
| --- | --- | --- | --- |
| Leaf breaking | Every block of the body box, about 80 reads for a T. rex and about 500 for a Brachiosaurus `[inf]` | Every tick while the switch and mob griefing are on | Avoid (B25.13) |
| Hunt scan | A box of 128 × 64 × 128 blocks, then at most 6 path tests | 1 goal tick in 10, hungry only | Fine with the budget (J8) |
| Food and water search | Shell search with the palette pre-filter, 6 tests | Back-off 20 to 600 ticks, dirty wake | Fine (J6) |
| Feeder lookup | Every feeder in the level, sorted | Per search | Avoid the whole-level scan (B20.17) |
| Path search | About 200 nodes, one failed search per tick | Per goal | Fine (J16) |
| Part update | Per part: a few dozen float operations, one bucket compare | Every tick, on both sides | Fine. Nublar has 11 to 17 parts, about twice JC3's median of 7 `[inf]` |
| Sleep threat test | A box of 10 × 5 × 10 | Cached for 20 ticks | Fine (J7) |
| Breathing sound | One sound per creature | Every 62 ticks | Avoid per-creature loops (B16.15) |

Nothing in JC3 spreads work by entity id or by player distance `[v]`. The entity survey's interval table (S21) and the tech survey's distance band (T9) stay in the plan.

### Client cost

| Item | JC3 value | Nublar comparison |
| --- | --- | --- |
| Draw passes per creature | Body pass plus overlays. Median 5 overlay sheets per sex, so about 6 passes. T. rex at most 7 `[v]` | Gallimimus 23 today. The bake brings it to 1, plus any glow layer |
| Bones per creature model | 49 to 346, median 108, one cube each `[v]` | One pass over 20 median creatures walks about 2,200 parts `[inf]` |
| Texture memory per species set | Median 34 MB, T. rex 85 MB for both sexes, Triceratops 101 MB per sex (RGBA8, no mips) `[inf]` | The bake cache caps at 96 MB for 24 genome keys |
| Animation clips | 1,780 files, 26.6 MB, median 15 per species `[v]` | Release 1 budget reference |
| Sounds | 2,491 files, 47.2 MB, 558 events `[v]` | Release 1 budget reference |
| Species data sync | 129 files, 764 KB, sent to every client at login `[v]` | B00.5 sends only a client projection |

### Asset and data counts

All counts come from the jar listing `[v]`.

| Kind | Count |
| --- | --- |
| Species / plants (data pack registries) | 129 / 49 |
| Entity types | 148 |
| Data JSON / NBT templates | 1,874 / 6 |
| Recipes / loot tables / advancements | 577 (plus 2 vanilla overrides) / 382 / 468 |
| Configured / placed features / structures | 19 / 16 / 4 |
| Model JSON / textures / blockstates | 5,320 / 6,405 files (6,393 PNG) / 371 |
| Lang keys / config options / mixins | 2,024 / 41 / 4 |

## AI: plain vanilla goals with gates beat a brain only on cost discipline

**In game:** a JC3 animal picks one job at a time: flee a shock, eat, drink, sleep, mate, hunt, follow its owner or wander. It never plans further than one target.

- **Architecture.** One creature class extends the vanilla pathfinder mob. A land class registers 27 goals and 4 target goals `[v]` (settled fact 17). There is no brain, no memory module and no sensor class `[v]`. Behaviour comes from one species record per type (AI note §3).
- **Goal order.** Float and shock flight first, then unstick and panic, sleep, needs, attacks, social goals, wander and idle emotes (AI note §5.1). This ladder is a good check for the order of Nublar's SBL activities (B17).
- **Needs.** Hunger and thirst only. The pool is `adult_health² × 7.5`. Burn is `adult_health × 75 ÷ 24,000` per tick, and water burns twice as fast `[v]`. A Velociraptor gets hungry after 16,800 ticks (14 min), a T. rex after 38,400 (32 min) (AI note §6). Below 20 % food or water the animal loses 5 % of its max health every 40 ticks `[v]`. Nublar keeps the lead's starvation rule and thresholds (B17.25).
- **Foraging.** Sources in priority order: dropped items 10, carcass 20, feeder 30, plant blocks 40, filter blocks 100 `[v]`. Search radius 16, drink radius 32 (AI note §7).
- **Hunting.** A hungry hunter scans on 1 goal tick in 10. It sorts candidates by distance, tests reach for at most 6, and attacks only prey with max health at most 0.9 of its own. It re-tests reach every 40 ticks. After a hunt it waits 50 to 69 ticks `[v]` (AI note §8, settled fact 19). There is no diet filter, so a starving neutral herbivore can attack (AI note §19, B17.31).
- **Detection.** Sight only, through vanilla targeting. Follow range is 64 in all 129 species files `[v]`. No hearing and no scent (AI note §8).
- **Path finding.** Node cap, one failed search per tick, column cache, a step height of `max(1, ceil(height ÷ 2) ÷ 2)`, step cancel on partial blocks, and a 60-tick progress test that triggers an unstick move `[v]` (AI note §14). These go to B26 (J16, J17).

**Verdict.** Take the gates, budgets and back-off. Keep SBL activities, as the lead decided. Do not take the vanilla-goal architecture.

## Behaviour: JC3 has the outline of a park animal, not its depth

**In game:** JC3 animals drift together, avenge a hurt herd mate, guard a fighting youngster, sleep at night under cover, mate, lay eggs, die into carcasses and pounce, spit or glide by species.

- **Herds.** The herd goal keeps up to N same-species animals within 16 blocks. It steps away below 3 blocks and walks to the centroid beyond 8. N is a config value, default 4, that overrides every species value `[v]` (AI note §9). It has no leader, no formation and no journey. Nublar's B26 herd work is far ahead (B26.13 Drop).
- **Alarm and young.** A hurt animal alerts awake same-species animals in follow range. An adult copies the target of a young herd mate within an 8 × 3 × 8 box. A "young animal gained a target" event marks the chunk for 400 ticks, and adults check marks at most every 10 ticks `[v]` (AI note §9). This event wake fits Nublar's protect-the-young item (J9).
- **Sleep.** A schedule per species, not a need. An animal does not sleep while desperate (food or water below 30 %), during a 200-tick lockout after damage, or with a threat in a 10 × 5 × 10 box `[v]` (AI note §10). The crepuscular window is inverted by mistake: the animal sleeps only from 22,480 to 6,480 `[inf]`.
- **Breeding.** Adults at full health with no cooldown mate after a 1-in-20 roll every 40 to 79 ticks. Pregnancy lasts 9,600 ticks (8 min). The mother rolls the whole clutch at mating and lays or gives birth half-way through the clip `[v]` (AI note §11, genetics note §6).
- **Death.** A dead animal becomes a carcass with `clamp(floor(√(w × h)) × 2, 1, 10)` stages. Each hit by an entity drops 1 to 2 meat plus Looting. Meat eaters below 75 % energy bite carcasses `[v]` (settled facts 10 to 12).
- **Special moves.** Pounce (10 species), venom spit (Dilophosaurus), tree climb and glide (Microraptor), shoulder sit `[v]` (AI note §13). Nublar's Dilophosaurus needs the spit (J11). Pounce stays deferred with B18.10.
- **Animation and sound.** One server-owned clip with a start tick, exclusive and hold flags, tick events on server or client and a 4-tick crossfade `[v]` (AI note §16). Voice volume grows from 0.2 to 1.0 and pitch falls from 2.5 to 1.0 with growth `[v]`.
- **Ownership.** Two routes to an owner: hatching without sneaking, or a 10 % roll when hand-feeding an infant (settled fact 13). Orders: wander, follow, sit. The order packet checks the owner and an 8-block range `[v]`.
- **Unread data.** `attack_bias`, `flock_speed`, `random_flock`, `jump_height`, `attack_speed` and `defend_offspring` parse, but no code reads them `[v]` (AI note §19).

## Genetics: a small genome with good colour and breeding mechanics

**In game:** a JC3 animal carries a species, a DNA quality, four trait multipliers, four colour slots and its parents. The player can change only the colours.

- **Genome.** A 10-letter ACGT string that no rule reads, a quality from 0 to 100, scale, damage, health and speed multipliers, and four HSL colour slots `[v]` (genetics note §2). Nublar's 26-gene design is far larger. JC3 is a reference for visible traits and colour only.
- **Traits.** Scale rolls from 0.8 to 1.2. Damage and health scale with `(2 + scale) ÷ 3`, and speed with its inverse. So a big animal hits harder, has more health and walks slower `[v]` (genetics note §2.3).
- **Colour.** 46 named HSL palettes with weights. A species lists about 6 palettes per slot. A "natural" roll picks a palette by weight, then a colour inside its ranges. A "random" roll is muted: saturation 20 to 35, lightness 26 to 38. Tints lift each channel by 255 ÷ 195 `[v]` (genetics note §2.4, §9). This fits Nublar's per-clone colour variation (J21).
- **Quality.** Sequencing gives 5 to 100 in steps of 5. A combiner adds two samples of one species, capped at 100. Synthesis accepts only 100. Nothing lowers quality, and nothing in the animal reads it `[v]` (genetics note §3).
- **Inheritance.** Each child trait is a random value between the parents, re-rolled one time in three. Colours come from one parent per slot. Colour mutation exists as a hook that never fires `[v]` (genetics note §6.4).
- **Sex.** A temperature slider per incubator slot gives the male chance. Bred young roll 50/50 `[v]`.
- **Growth.** Age counts one unit per 8 ticks from a birth stamp and a skip-aware world clock. Stages at 25, 50 and 75 % `[v]` (genetics note §8).
- **Species data.** One JSON per species with strict validators, per-pose hitbox lists, abilities as data, remains and colour families `[v]` (genetics note §10).

## Tech: one generic machine base, no power, redstone fences

**In game:** each JC3 lab block opens a screen with slots, a progress arrow and a status line. Machines run free. A redstone signal electrifies a fence.

- **Machine base.** One abstract block entity with a typed slot list and one layout record per machine. The same layout drives the menu, the JEI category and a preview command `[v]` (vehicles note §11).
- **Recipes.** One `lab` recipe type carries a machine id and a DNA operation. Results have weights. `processing_time` is in ticks, 1 to 32,767 `[v]` (settled fact 3).
- **Sync and idle.** Progress travels only to an open menu. A block update goes out only when a status changes. The server still ticks every machine every tick and has no idle sleep `[v]` (vehicles note §13, §14).
- **Automation.** Item capabilities only, the same handler on all six sides, with role flags per slot. No fluid or energy capability `[v]` (vehicles note §16).
- **Power.** None. B14 has no counterpart `[v]` (vehicles note §15).
- **Fences.** Low and medium tiers. A redstone signal next to a base with a pole above powers connected bases (settled facts 1 and 2). Shock damage 1.0 or 2.0 per contact tick `[v]`.
- **Feeders.** One feeder serves one animal at a time and ejects one item per visit `[v]` (vehicles note §18). Nublar's point pools serve many animals (B20.2).
- **Tooling.** A Jade status line, JEI categories from the layouts, and `/jcmachine turbo` that makes every job take 1 s `[v]` (vehicles note §19, §20).

## Vehicles: a simple arcade car and an own rail follower

**In game:** the player drives a Jeep Wrangler or a Ford Explorer, flies a helicopter, and lays tour rails that carry the Explorer.

- **Roster.** Two cars (3.0 × 2.5 blocks) and one helicopter (5.0 × 3.5). No fuel, no upgrades `[v]` (vehicles note §2).
- **Car model.** Each tick: velocity × 0.8, a push along the facing, gravity 0.15 in the air. Top speed 5, 10 or 20 blocks per second for three presets. Steering is a yaw rate capped at 3° per tick `[v]` `[inf]` (vehicles note §3).
- **Tilt.** Four client-visible edge probes every 20 ticks, staggered by entity id, smoothed at 0.15 blocks per tick `[v]` (vehicles note §4).
- **Authority.** The helicopter is server-authoritative. The cars keep the vanilla default, so the driver's client moves them `[inf]`. JC3 adds no speed clamp for cars `[v]` (vehicles note §5).
- **Input.** The server reads movement keys from the player's last vanilla input. One small packet carries only three extra helicopter keys, sent on change, from the controlling passenger only `[v]` (vehicles note §6).
- **Rails.** 20 shapes, 4 speed tiers, a rider starts the Explorer, the backward key reverses once. No stop, no horn, no blocked-track check `[v]` (vehicles note §7).
- **Helicopter.** A momentum-theory rotor, a stabiliser, an attitude lock and a safe-landing rule `[v]` `[inf]` (vehicles note §8). Aircraft are out of Beta scope.

## Gameplay: a full fossil-to-park loop with thin guidance

**In game:** the player jackets a fossil, washes and grinds it, sequences and combines DNA to 100 %, makes an embryo, incubates an egg and hatches a baby. Villagers sell lab parts and bones. Rare ruins hold ready-made DNA.

- **Fossils.** A geologic column from Y 64 to −60 splits into 8 period bands by period length (settled fact 9). A species is picked per 8 × 8 × 8 cell from a seeded random source `[v]` (gameplay note §2).
- **Jacket step.** A bare fossil drops nothing. Plaster and Bandage encases it, and a Cleaning Station washes it `[v]` (gameplay note §3). Nublar plans Fortune and Silk Touch (open item O27).
- **Short cuts.** Amber, raw meat from a carcass and a sea lamprey give DNA with no fossil chain. The Geneticist sells the machines for 4 to 7 emeralds, and recipes exist too `[v]` (gameplay note §4, §8).
- **Villagers.** Two professions on existing machine workstations. Trades, trade sets and tags as 26.x data `[v]` (gameplay note §8).
- **Structures.** Very rare: Visitor Centre one per 448-chunk cell, raptor paddock one per 4,000 chunks by default. The Visitor Centre map searches only 100 units, so many maps may fail `[inf]` (gameplay note §9).
- **Guidance.** No guide book, no step advancements, no gameplay commands. Guidance comes from 13 JEI info pages, the PaleoPad, Jade and tooltips `[v]` (gameplay note §13 to §15).
- **Config.** 41 options. Every worldgen feature has a switch through one placement filter. Natural spawning is off `[v]` (gameplay note §16).

## Gameplay alignment: 29 adopt, 36 adapt, 39 defer, 43 drop

**In game:** the rows below decide which JC3 behaviour a Nublar player meets. Most adopted rows are invisible cost rules. Most dropped rows conflict with a lead decision.

### How the tables rate a technique

Verdicts: **Adopt** builds the technique nearly as JC3 does. **Adapt** keeps the idea and changes the form to fit section 11, the plan or a library hook. **Defer** waits for a decision, art, a later module or a measurement. **Drop** leaves it out of Beta. Effort counts code beyond the module's existing scope. Evidence cites the note section. A "J" number links to "Clean-room implementation specifications". Rows that use only a vanilla or library API need no specification.

### B00 Config and species data

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B00.9 | Strict species validators: eye height up to body height, minimum up to maximum for every range, sizes and stats in range (J1) | A bad species file fails at load with a line that names the file and the field | Genetics note §10.1 `[v]`. Two species have entry errors (`attack_speed` 185 and 0) | Adopt | S | Same goal as B00's loud failure. Ranges also catch value typos |
| B00.10 | Fallback species record and one log line per missing species (J1) | A summoned dinosaur with no data still loads with safe values, and the log names it once | Genetics note §10.1 `[v]` | Adopt | S | Answers cross-cutting risk 1 |
| B00.11 | Every species key has a reader and a test (J1) | A species file never promises a behaviour that the game lacks | Six unread keys, AI note §4, §19 `[v]` | Adopt | S | Cheap rule. Stops dead data |
| B00.12 | Default depth band from the species period, by a largest-remainder split of the fossil column (J2) | A new species with only a period still has a fitting fossil depth | Gameplay note §2.1, settled fact 9 `[v]` | Adapt | S | Default only. Per-species `depth_min` and `depth_max` stay, as the lead decided |
| B00.13 | Hybrid list in species data excludes the species from fossils and natural spawns | — | Genetics note §4.5 `[v]` | Defer | S | Hybrids are backlog |
| B00.14 | Full species files synced to every client at login | — | 764 KB for 129 species, performance note §3.1 `[v]` | Drop | — | B00.5 sends a client projection |
| B00.15 | Worldgen switches through one placement filter in every placed feature (J3) | A server owner turns off fossil ore or amber, and new chunks have none | 18 worldgen switches, gameplay note §16 `[v]` | Adopt | S | Extends the T11 `features` section to worldgen |
| B00.16 | An own mob category with its own cap | — | Cap 10, despawn distance 128, performance note §10.5 `[v]` | Drop | — | No natural spawning. Settled fact 26 |

### B01 Fossil ore worldgen

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B01.1 | Species chosen per 8 × 8 × 8 cell from a random source seeded by the world seed and the cell, with no block entity (J2) | One fossil pocket holds one species, and the same spot holds the same species after a reload | Gameplay note §2.1 `[v]` | Adapt | S | Fits "one cluster is one species" and D31. Weighted by B01's rarity, not uniform |
| B01.2 | Nest-site patch of sediment, nest fossils and trackways | — | Gameplay note §2.2 `[v]` | Defer | S | Not one of the four decided forms. Art needed |
| B01.3 | Buried trackways and petrified logs as decoration | — | Gameplay note §2.2 `[v]` | Defer | S | Same reason |
| B01.4 | Fossil look from a hash of the species name | — | Gameplay note §2.1 `[v]` | Drop | — | B01 draws looks from the position seed (OM1) |
| B01.5 | Synchronized weak-map cache of species lists in worldgen | — | Gameplay note §18 `[v]` | Drop | — | OW1 and OW4 forbid it |

### B02 Fossil items, B03 Amber, B04 Living DNA sources, B06 Sequencer

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B02.1 | Jacket step: a bare fossil drops nothing, Plaster and Bandage encases it, a machine washes it | A failed break says "too brittle" | Gameplay note §3 `[v]` | Defer | M | B02 plans Fortune and Silk Touch. Open item O27 |
| B03.1 | Amber drops an animal sample or a plant sample, 50/50 | — | Gameplay note §5 `[v]` | Defer | S | Plant DNA belongs to Release 1 (R01) |
| B03.2 | Amber species picked uniformly from all non-hybrid species | — | Genetics note §4.4 `[v]` | Drop | — | B03 rolls by `amber_weight`, depth and biome (lead) |
| B04.1 | Raw meat carries the dead animal's genome. Cooked meat carries only the species (J5) | A player who butchers a prized animal can clone it from raw meat | Settled fact 11 `[v]` | Adapt | S | A new B04 remains source. Yield stays under D04 |
| B04.2 | Sea lamprey from an ore as marine DNA | — | Gameplay note §4 `[v]` | Defer | S | No aquatic Beta species |
| B06.1 | Random sample quality in steps of 5, from 5 to 100 | — | Genetics note §3.1 `[v]` | Drop | — | Yield follows fossil quality (lead, section 11.1) |
| B06.2 | A DNA carrier with 3 synthesis charges | — | Genetics note §3.3 `[v]` | Defer | S | A balance lever the lead has not asked for |

### B07 Gene editing core and B08 Edit screen

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B07.4 | Size-linked stats: a bigger animal hits harder, has more health and is slower | — | Genetics note §2.3 `[v]` | Defer | S | The gene map gives each gene its own effect. Coupling is a design change |
| B07.5 | Colour roll from weighted HSL palette families per species and layer, plus a muted random roll (J21) | A cloned raptor gets natural colours from its species palette, never neon | Genetics note §2.4 `[v]` | Adapt | S | Feeds B07's per-clone colour variation |
| B07.6 | A 10-letter genome string with no effect | — | Genetics note §2.1 `[v]` | Drop | — | No gameplay reader |
| B07.7 | Traits written into attribute base values on refresh | — | Genetics note §2.3 `[v]` | Drop | — | `GenomeEffects` uses modifiers in one rebuild (B07.1) |
| B08.1 | Staged colour edit: preview, natural roll, random roll, reset, explicit Apply, an "unapplied" badge, copy colours to another item (J22) | The player tries colours on a live preview and commits them with Apply | Genetics note §5 `[v]` | Adapt | S | Fits the server-checked edit packet. Nublar also edits stats and genes |
| B08.2 | Client-only preview entity made from the entity type | The screen shows the animal with no server entity | Genetics note §5 `[v]` | Adopt | S | Vanilla `EntityType.create`. B08 already plans a preview |
| B08.3 | An editor limited to colours | — | Genetics note §5 `[v]` | Drop | — | B07 edits all four gene groups |

### B09 Appearance and rendering

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B09.1 | Single-pass tint table with a custom core shader | — | Performance note §5.2 `[v]` | Defer | M | Conflicts with OC6 and the bake spec. Open item O22 |
| B09.2 | Packed pattern sheet: three pattern layers in one image, layer id in four alpha levels | — | Performance note §5.2 `[v]` `[doc]` | Defer | S | Would change the bake input and the art pipeline. Revisit if bake decoding costs too much |
| B09.3 | Overlay bone masks from an async PNG scan (J20) | The glow pass draws only the bones that hold glow pixels | Performance note §5.4 `[v]` | Adopt | S to M | Shader-free. Works with the bake. Uses GeckoLib 5 bone visibility |
| B09.4 | Pose replay: compute the pose once, replay it for overlay passes | — | Performance note §6.4 `[v]` | Defer | S | Check first whether GeckoLib 5 shares one pose across layers |
| B09.5 | Walk and run chosen by real speed, and procedural sway faded while an action clip plays (J19) | A roaring T. rex stops swaying its tail and head | Performance note §6.3 `[v]` | Adapt | S | Plan B09 asks for gait from real speed. Uses GeckoLib 5 bone updaters |
| B09.6 | The throat bone follows its neck bone in another branch (J23) | A long neck bends without a gap at the throat | Performance note §6.6 `[v]` | Adopt | S | Small rule for the Brachiosaurus neck |
| B09.7 | Vertex writes through `sun.misc.Unsafe` | — | Performance note §1.3 `[v]` `[doc]` | Drop | — | Settled fact 25. A P2 risk. Measure the claim only (new item 30) |
| B09.8 | Tint lift: each tinted channel × 255 ÷ 195, and per-layer HSL offsets (J21) | Tinted hides stay bright on grey masks | Genetics note §9 `[v]` | Adapt | S | One factor in the bake's colour step |
| B09.9 | Display hologram that creates a real entity per block | — | Performance note §8 `[v]` | Drop | — | Hidden cost. No Nublar module needs it |

### B11 Synthesis, egg printer, incubator and hatch

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B11.10 | Live-bearers refused by the egg route with a message | — | Genetics note §4.6 `[v]` | Defer | S | No live-bearing Beta species |
| B11.11 | The synthesis input slot accepts only DNA at the threshold | A player cannot start a run that will fail | Genetics note §4.6 `[v]` | Adapt | S | Use the clone threshold of `EditRules`. Vanilla slot filter |
| B11.12 | Hatching without sneaking makes the player the owner | — | Settled fact 13 `[v]` | Defer | S | Decision D17 default: no owner |
| B11.13 | Temperature slider per incubator slot sets the male chance | — | Genetics note §4.7 `[v]` | Defer | S | Gender is a gene (lead). Open item O26 |
| B11.14 | Live-bearer machine with four nutrient bars fed by a food data map | — | Genetics note §4.8, settled fact 16 `[v]` | Defer | M | No live-bearers. The data map is NeoForge only |
| B11.15 | A placed egg that breaks or is picked up keeps its genome on the item | A player moves an egg without losing its edits | Genetics note §6.5 `[v]` | Adapt | S | The S1 egg block copies its `DinoData` to the drop. Vanilla loot component copy |

### B12 Machine framework and B13 Recipes and progression

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B12.25 | Early-out when no input slot holds items, before the recipe check | An empty machine costs a few slot reads per tick | Vehicles note §13 `[v]` | Adopt | S | Same as OS2 |
| B12.26 | Recipe time capped at 32,767 ticks, because `ContainerData` sends 16-bit values | A progress bar never wraps | Vehicles note §12.2 `[v]` `[inf]` | Adopt | S | State the cap in the B12 recipe schema |
| B12.27 | One layout record per machine drives the menu, the screen, the item handler and any recipe viewer (J12) | Slots on the screen and slots for hoppers always agree | Vehicles note §11 `[v]` | Adapt | M | Add a side mask for B12's per-side rules |
| B12.28 | Weighted results in recipe JSON for by-products | — | Vehicles note §12.1 `[v]` | Drop | — | B05 keeps by-product weights in the TOML (plan) |
| B12.29 | One recipe type with a machine id and an operation field for all lab machines | — | Genetics note §4.2 `[v]` | Defer | S | The B12 task plan chooses the recipe shape |
| B12.30 | Five-value status enum | — | Vehicles note §11 `[v]` | Drop | — | The B12.8 issue mask is richer |
| B12.31 | `turbo` makes each machine job take 1 s. `rig` and `kit` fill machines for tests (J13) | A tester runs a full chain in seconds | Vehicles note §20 `[v]` | Adopt | S | Operator-only debug command |
| B12.32 | Machine renderer view distance 48 blocks | Machines far away cost no draw | Performance note §7 `[v]` | Adopt | S | A start value from a shipping mod |
| B13.1 | JEI info pages and Jade lines for machines and species | — | Gameplay note §13 `[v]` | Defer | S | 26.2 builds of JEI and Jade are not checked. The B12.10 hover box covers Jade's role |
| B13.2 | Recipe unlocks as the only advancements | — | Gameplay note §14 `[v]` | Drop | — | B13 plans step advancements |
| B13.3 | Items with no way to get them in survival | — | Gameplay note §18 `[v]` | Drop | — | B13 rule: a recipe or a creative-only mark for every item |

### B14 Power and B15 Fences and containment

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B14.21 | Machines run with no power | — | Vehicles note §15 `[v]` | Drop | — | Real power (lead) |
| B15.22 | Fence power computed only on change, with no tick and no block entity, over a bounded area | A still fence costs no tick | Settled fact 1 `[v]` | Adapt | S | Confirms OS1. Keep T1's one rebuild per tick, because JC3 repeats its flood for every placed block |
| B15.23 | A redstone signal at a base powers the fence | — | Settled fact 2 `[v]` | Drop | — | F23 and F30: posts take power from generators. The switch block cuts power |
| B15.24 | Shock damage by tier, 1.0 or 2.0 | — | Vehicles note §17 `[v]` | Drop | — | F25: 1 HP for both posts |
| B15.25 | The wire is a ladder for players and climbing species, powered or not | — | Vehicles note §17 `[v]` | Drop | — | F26 and D38 |
| B15.26 | A shocked animal flees the fence and stops herding for 200 ticks | — | AI note §13 `[v]` | Drop | — | Shock deterrence was superseded on 2026-10-10. Settled fact 27 |
| B15.27 | A 1.5-high base box for non-climbing dinosaurs | — | Vehicles note §17 `[v]` | Drop | — | Nublar posts are full columns of wires (F1, F8) |

### B16 Dinosaur core

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B16.10 | Birth stamp plus a world clock that counts skipped time (J4) | Animals grow through a skipped night. No age packet per tick | Genetics note §8 `[v]` | Adopt | S | Rank 2. Fits "timers use game time" |
| B16.11 | Size refresh only above a 0.01-block change, attribute writes only on a new value | No per-tick attribute or size work | Performance note §3.3 `[v]` | Adapt | S | Inside the B07.1 rebuild. Skip JC3's 40-tick backstop |
| B16.12 | Carcass stages from body size, `clamp(floor(√(w × h)) × 2, 1, 10)`, bones 9 in 10 at the last stage (J5) | A raptor carcass gives 2 cuts, a big sauropod 10 | Settled fact 10 `[v]` | Adapt | S | Replaces the fixed 4 stages of entity survey feature 9 (a proposal, not a lead decision) |
| B16.13 | Only naturally spawned carcasses decay | — | Settled fact 12 `[v]` | Drop | — | Every carcass decays after `needs.carcass_minutes` (lead) |
| B16.14 | Voice by growth: volume 0.2 to 1.0, pitch 2.5 to 1.0 (J19) | A hatchling squeaks and an adult roars | Performance note §9 `[v]` | Adapt | S | Sound volume and pitch at play time |
| B16.15 | A breathing sound per creature every 62 ticks | — | Performance note §9 `[v]` | Drop | — | Per-creature loops. OC11 pools ambient sound |

### B17 Needs

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B17.17 | Retry delay per need search: base × 2^failures, capped at 600 ticks, plus a random wait (J6) | An animal with no food in reach searches less and less often | AI note §7 `[v]` | Adopt | S | Rank 1. Cap 600, not S8's 400 |
| B17.18 | Failed-target list: 64 slots, 600 ticks each (J6) | An unreachable bush is skipped for 30 s | AI note §7 `[v]` | Adopt | S | Concrete form of B17.11 |
| B17.19 | Dirty-chunk wake: a search retries early after a block change or an item drop near it (J6) | A hungry animal reacts at once to food that a player drops | Settled fact 15, vehicles note §18 `[v]` | Adopt | S | Event-driven, no scan |
| B17.20 | Section palette pre-filter before reading blocks, and at most 6 path tests (J6) | — | AI note §7 `[v]` | Adapt | S | Applies to S6's sampled columns |
| B17.21 | Food sources in priority order: dropped items, carcasses, feeders, diet blocks (J6) | A raptor eats dropped meat before it walks to a feeder | AI note §7 `[v]` | Adopt | S | One ordered list |
| B17.22 | Food value from five tag tiers with defaults for untagged items and blocks (J6) | Bales feed far more than leaves | AI note §7 `[v]` | Adapt | S | Ratios, scaled to Nublar's species hunger values (B17.14) |
| B17.23 | Every meal heals the animal to full health | — | AI note §7 `[v]` | Drop | — | Section 11 has no such rule. Health regen is a gene |
| B17.24 | Need pools from `adult_health²` | — | AI note §6 `[v]` | Drop | — | `SpeciesData` has its own hunger and thirst values |
| B17.25 | Threshold ladder 80 / 30 / 20 % and 5 % health per 40 ticks below 20 % | — | AI note §6 `[v]` | Drop | — | The lead decided starvation and the 75 / 25 % thresholds |
| B17.26 | No sleep while desperate, and a cached threat check before sleep (J7) | A starving animal stays awake. An animal with a hunter nearby does not lie down | AI note §10 `[v]` | Adapt | S | Extends S7. Desperate line = the plan's 25 % |
| B17.27 | Sleep windows per activity type in day ticks (J7) | Diurnal, nocturnal and dusk animals sleep at their own times | AI note §10 `[v]` | Adapt | S | Fits the reconciliation's `activity_window`. Fix the inverted dusk window |
| B17.28 | Shelter search: 32 random spots within 8 blocks with no sky and no fluid below | — | AI note §10 `[v]` | Defer | S | Herd decision 28 rests members in their slots |
| B17.29 | Drink approach: turn up to 15° per tick to face the water, drink within 20°, a 0.35-block final approach (J6) | A drinking animal faces the water, not the shore | AI note §7, settled fact 21 `[v]` | Adapt | S | Fits the head-contact rule (lead, 2026-10-09) |
| B17.30 | Hunt budget: at most 6 reach tests, re-test every 40 ticks, a 50 to 69-tick lockout after a hunt, an optional prey-health ratio (J8) | A pack of predators never stalls the server when prey hides | AI note §8 `[v]` | Adapt | S | Inside B17.7's one scored pass |
| B17.31 | No diet filter on hunt targets | — | AI note §19 `[v]` | Drop | — | Prey comes from `prey_preferences` (B16) |
| B17.32 | Meat eaters below the seek threshold bite carcasses (J5) | A hungry raptor feeds on a kill it did not make | AI note §12 `[v]` | Adapt | S | First precedent for entity survey feature 9 |

### B18 Social life and breeding

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B18.12 | Every young rolled at mating and stored on the mother | — | Genetics note §6.3 `[v]` | Defer | S | Timing for bred young is open. Open item O24 |
| B18.13 | Own young under half grown within 16 blocks block breeding | — | Genetics note §2.5 `[v]` | Defer | S | The lead set three gates. Open item O25 |
| B18.14 | Breeding stops at 4 animals within 16 blocks | — | Genetics note §6.2 `[v]` | Defer | S | Decision D20 default is 0. Evidence only |
| B18.15 | Creative Breeding Wand and Birthing Wand (J10) | A tester pairs two animals and ends a wait at once | Genetics note §6.3 `[v]` | Adopt | S | Short B18 GameTests. Creative-only marks |
| B18.16 | Variance floor test for a closed breeding line (J10) | A paddock bred for 10 generations still shows varied animals | Genetics note §6.4 `[v]` `[inf]` | Adopt | S | A test, not a rule change. The lead's midpoint rule stays |
| B18.17 | Mother and father ids saved on the young | Family stays known after a reload | Genetics note §2.5 `[v]` | Adopt | S | H4 (no cannibalism in the family) needs it. Vanilla UUID codec |
| B18.18 | A young animal's new target wakes nearby adults through a chunk mark (J9) | Adults join a fight that a juvenile starts within half a second | AI note §9 `[v]` | Adapt | S | Event wake for B18.6 |
| B18.19 | Mate search budget: poll every 40 to 79 ticks, 1-in-20 roll, nearest within 16 blocks, at most 6 path tests, approach at most 300 ticks (J10) | Pairs find each other without path spam | Genetics note §6.2 `[v]` | Adapt | S | Numbers for SBL `BreedWithPartner` after the lead's gates |

### B19 Player interaction and detection

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B19.16 | Temperament table: who hunts, who retaliates and alerts, who panics (J8) | A scared herbivore never fights back. A neutral one does | AI note §8 `[v]` | Adapt | S | Maps to flee or stand (D13) and the aggression gene |
| B19.17 | Taming by hand-feeding an infant, 10 % per feed | — | AI note §17 `[v]` | Drop | — | Taming uses reputation (B19.9) |
| B19.18 | Handheld status panel opened on an animal | — | AI note §17 `[v]` | Defer | M | Decision D18 default: no readout for non-operators |
| B19.19 | Pounce math: crouch, jump arc, landing hit, 100-tick cooldown | — | AI note §13 `[v]` | Defer | M | B18.10 waits for crouch and leap clips |
| B19.20 | Venom spit: range 10, 20 ticks of line of sight, fired at tick 20 of the clip (J11) | A Dilophosaurus opens its frill and blinds a player 10 blocks away | AI note §13 `[v]` | Adapt | M | The Dilophosaurus spit clip exists. Poison only with the B10 gene |
| B19.21 | Defend: copy the owner's last attacker or last victim while following | A tamed raptor joins its owner's fight | AI note §17 `[v]` | Adapt | S | The B19 defend command. Vanilla tamable pattern |

### B20 Feeders and B21 Digsites, villagers and maps

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B20.17 | A feeder query that scans every feeder in the level | — | Settled fact 15 `[v]` | Drop | — | Look up only the chunks inside the radius (new item 21) |
| B20.18 | One animal claims a feeder, which ejects one item per visit | — | AI note §7 `[v]` | Drop | — | B20.2 point pools serve many animals |
| B20.19 | Meat slots and plant slots in one feeder | — | Vehicles note §18 `[v]` | Drop | — | Nublar has three feeders by diet |
| B20.20 | Nutrient values per food in a data map | — | Genetics note §4.8 `[v]` | Defer | S | NeoForge-only API. Feed tiers by tag cover B20 |
| B21.15 | Trades, trade sets and trade tags as 26.x data | — | Gameplay note §8 `[v]` | Adopt | S | A working example for S17 |
| B21.16 | An existing machine as the villager workstation | — | Gameplay note §8 `[v]` | Defer | S | The B21 task plan decides the workstation |
| B21.17 | Flat-site test on a 3 × 3 column grid before a structure places | — | Gameplay note §9.1 `[v]` | Defer | S | S14 digsites are data only. Revisit if digsites land in water |
| B21.18 | Size the structure spacing below the map search radius | Every map trade finds its digsite | Gameplay note §8.3 `[inf]` | Adopt | S | JC3 maps may fail, because the cell is wider than the search `[inf]`. Confirms B21.11 |
| B21.19 | A villager spawned inside structure post-processing | — | Gameplay note §9.2 `[v]` | Drop | — | OW3 and S15 |
| B21.20 | Rarity as "one per N chunks" with a custom placement type | — | Gameplay note §9.1 `[v]` | Defer | M | Needs a placement type on both loaders |
| B21.21 | A percent option for fossils injected into vanilla chests | A server owner makes chest fossils rarer or turns them off | Gameplay note §16 `[v]` | Adapt | S | Through S15 inject pools on both loaders |

### B22 Vehicles and B23 Guns

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B22.31 | Movement keys from the vanilla last client input. One small packet for extra keys, on change, from the controlling passenger (J14) | The Jeep reacts at once, and a parked car sends nothing | Vehicles note §6 `[v]` | Adopt | S | Rank 6. Keep S12's one packet per tick |
| B22.32 | Arcade drive: drag, push along the facing, a capped yaw rate, speed presets (J15) | The Jeep speeds up fast, holds its top speed and turns smoothly | Vehicles note §3 `[v]` | Adapt | S | Numbers come from the B22.3 data record. Keep B22.1's server clamp |
| B22.33 | Client body tilt from four edge probes, smoothed (J15) | The Jeep pitches and rolls on slopes | Vehicles note §4 `[v]` | Adapt | S | Client only, as B22.23 says |
| B22.34 | Step height 1.5 | — | Vehicles note §4 `[v]` | Drop | — | B22.23 says 1.0 to 1.25. 1.5 is the upper bound |
| B22.35 | One synced passenger id per seat and a seat memory per player id (J14) | A rider returns to the same seat after a reload | Vehicles note §5 `[v]` | Adapt | S | Smaller for 2 and 4 seats (D25) |
| B22.36 | Camera distance keys with a clamp and a reset | — | Vehicles note §10.2 `[v]` | Defer | S | NeoForge camera event. Not in B22 scope |
| B22.37 | Rail speed tiers set by block, cycled by recipe | — | Vehicles note §7 `[v]` | Defer | S | After Beta |
| B22.38 | The backward key reverses the Explorer once on the track (J14) | A driver turns the Explorer round on a track | Vehicles note §7 `[v]` | Adapt | S | One edge-triggered flag in S20 |
| B22.39 | An own rail with 20 shapes and diagonals | — | Vehicles note §7 `[v]` | Drop | — | B22.17 reuses `RailShape` |
| B22.40 | A dent that repairs itself after a pause | — | Vehicles note §9 `[v]` | Defer | S | Vehicle damage is deferred (B22.10) |
| B22.41 | Hitbox parts per vehicle | — | Vehicles note §4 `[v]` | Drop | — | B22.29 |
| B22.42 | A looping engine sound kept at volume 0 per vehicle | — | Vehicles note §10.4 `[v]` | Drop | — | A sound instance per parked vehicle |
| B22.43 | Vehicles break leaves and vines in their box every 20 ticks | — | Vehicles note §3 `[v]` | Defer | S | Section 11.12 is silent |
| B22.44 | A moving vehicle hurts the mobs it hits, by its speed | — | Vehicles note §3 `[v]` | Defer | S | Open item O28 |
| B22.45 | Helicopter flight model, stabiliser, attitude lock, safe landing | — | Vehicles note §8 `[v]` `[inf]` | Defer | L | Aircraft are out of Beta scope |
| B22.46 | Vehicles break after 160 ticks in water and hurt passengers | — | Vehicles note §9 `[v]` `[inf]` | Defer | S | Vehicle damage is deferred |
| B23.5 | Dart gun with tranquilizer, euthanasia and potion darts. Fixed sedation 650 to 699 ticks | — | Gameplay note §10 `[v]` | Defer | M | D08 blocks guns. No body-mass scaling in JC3 |

### B24 Art integration and B25 Cleanup and performance pass

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B24.6 | Load checks: a missing clip or bone logs once with the list of what exists. A required-bones check per species (J23) | An art mistake shows as one log line, not a broken animal | Performance note §6.1, §6.6 `[v]` | Adopt | S | Short art loop |
| B24.7 | Overlay coverage audit: count opaque texels outside every bone box (J23) | An artist sees pixels that never draw | Performance note §5.4 `[v]` | Adopt | S | Dev property only |
| B25.11 | Every fast path has an off switch, a compare switch and a break hook (J13) | A tester proves each shortcut | Performance note §11 `[v]` | Adopt | S | Rank 12 |
| B25.12 | One random block per second for slow world jobs | — | Performance note §10.6 `[v]` | Defer | S | No spreading block in Beta (R01) |
| B25.13 | A leaf scan of the whole body box every tick | — | Performance note §10.4 `[v]` | Drop | — | S23 queue and B26 leaf rules |
| B25.14 | Presized visited-block set through a mixin on a vanilla sweep | — | Performance note §1.2 `[v]` | Defer | S | Conflicts with B25.5. Open item O23 |

### B26 Large-body movement and B27 Section hitboxes

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B26.6 | Node cap per search from species data (J16) | A Brachiosaurus path stays cheap | AI note §14 `[v]` | Adapt | S | Rank 3. Check that herd legs of 24 blocks still fit the cap |
| B26.7 | One failed search per animal per tick (J16) | A boxed-in animal never fires a burst of failing searches | AI note §14 `[v]` | Adopt | S | Throttled is not failed, so the failed-target list ignores it |
| B26.8 | Column cache for the body footprint per search, with a compare switch (J16) | — | Performance note §10.3 `[v]` | Adapt | M | Wraps `BodyFit`, the one herd block reader |
| B26.9 | Step height from body height and no step-up over partial blocks (J16) | A sauropod climbs a 2-block bank but never steps over a wall | AI note §14 `[v]` | Adapt | S | The 26.2 step-height attribute |
| B26.10 | A 60-tick progress test as the stuck trigger (J17) | A stuck animal starts its recovery after 3 s | AI note §14 `[v]` | Adapt | S | First rung of B26's stuck ladder. Herd mates count as waiting (herd decision 14) |
| B26.11 | A straight-line walk for climbing species when a path fails | — | Settled fact 20 `[v]` | Drop | — | Lets climbers bypass fences. Fence parity governs climbing |
| B26.12 | Leaves cost 8 when the animal is desperate, else they block | — | AI note §14 `[v]` | Defer | S | Needs join herd play later (herd scope rule). B26's block filter decides leaves |
| B26.13 | Herd goal with 3, 8 and 16-block spacing around a centroid | — | AI note §9 `[v]` | Drop | — | The B26 herd design replaces it |
| B27.6 | Part index self-checks: count check and rebuild, foreign parts indexed, verify switch, break hook (J18) | A missed part heals on the next query | Performance note §1.1 `[v]` | Adopt | S | Rank 4 |
| B27.7 | Hand-authored hitbox lists per pose, blended over set ticks | — | Performance note §2.4 `[v]` | Defer | M | B27 bakes part positions from the animations. Fallback only if the bake slips |
| B27.8 | Pose flags and event ticks: exclusive, hold, server and client events, 4-tick crossfade (J19) | A roar is never cut off. Sounds start with no packet | AI note §16 `[v]` | Adapt | M | Rank 5. GeckoLib 5 triggered animations and keyframe events first |
| B27.9 | Spare parts shrink to 0.05 blocks when a pose list is shorter | — | Performance note §2.2 `[v]` | Drop | — | B27 changes part counts by distance with the same objects and ids |
| B27.10 | Damage forwarded with no multiplier | — | Performance note §2.5 `[v]` | Drop | — | B27 sets head ×1.5 to tail ×0.5 |

### P2 Port to 26.2

| Id | Technique | In game | JC3 evidence | Verdict | Effort | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| P2.1 | 26.x API shapes from a shipping mod: `Identifier`, `ValueInput` and `ValueOutput`, `SavedDataType`, `hurtServer`, `EntitySpawnReason`, `PathType`, `InterpolationHandler`, extract-and-submit rendering, `ResourceHandler`, `Orientation` | No change | All five notes `[v]` | Adopt | S | Reference for the port task plan. Check each name again on 26.2 |
| P2.2 | Store the client recipe map on `RecipesReceivedEvent` | A recipe viewer sees Nublar recipes on 26.x | Vehicles note §19 `[v]` | Adopt | S | 26.x clients get no full recipe list. Fabric equivalent `[inf]` |
| P2.3 | A static copy of the registry access taken at a tags event | — | Genetics note §10.1 `[v]` | Drop | — | Breaks with single-player client and server mixes |
| P2.4 | Registry alias for a renamed block id | — | Gameplay note, port notes `[v]` | Drop | — | Global constraint: no world-compat shims |

## No JC3 precedent for 18 Nublar features

**In game:** these features set Nublar apart from JC3. JC3 has no code for them `[v]` (grep and reading across the five notes). Each stays Nublar's own work under its existing plan item or specification.

| # | Feature | Module | Nublar source of design |
| --- | --- | --- | --- |
| 1 | Stamina-limited chases | B17 | Lead decision, entity survey feature 3 |
| 2 | Stress meter and its results | B17 | S5 |
| 3 | Sight cone and distance-chance detection | B19 | Entity survey feature 1 |
| 4 | Hearing through game events | B19 | B19.2 |
| 5 | Scent trails | B19 | Entity survey feature 2 |
| 6 | Threat display before an attack | B19 | S10 |
| 7 | Taming through reputation | B19 | B19.9 |
| 8 | Leaders, dominance contests and calls | B18 | Entity survey feature 4 |
| 9 | Pack flanking | B18 | Entity survey feature 12 |
| 10 | Herd travel: wedge, journeys, trail following | B26 | Herd spec |
| 11 | Nest blocks | B18 | B18.4, B18.5 |
| 12 | Gene editing of stats and behaviour, special genes, edit risk | B07, B10 | Section 11.6 |
| 13 | Real power with a network object | B14 | T1 |
| 14 | 1.12.2 fence parity: spools, cut wires, repairs, voltage signs, removers | B15 | F1 to F32 |
| 15 | Live-bait feeder | B20 | S13 |
| 16 | Driverless tours with stops, horn and announcements | B22 | T13, entity survey feature 7 |
| 17 | Distance LOD for animation and brain work | B09, B25 | Entity survey rank 7, T9 |
| 18 | Machine assembly from parts, fault effects and a hover box | B12 | S2, T6, T7 |

## JC3 against the earlier surveys

**In game:** nothing. Each row says whether JC3 adds a precedent for, or contradicts, a ranked change or a specification of the earlier surveys.

| Earlier item | Recommendation | JC3 | Effect |
| --- | --- | --- | --- |
| Entity survey rank 2, B17.1 | Per-level feeder registry | A registry per level, but each query scans every feeder `[v]` | New precedent, with one refinement: query only the chunks in range |
| Entity survey rank 6, B17.7 | One scored prey pass per scan | A 1-in-10 gate and at most 6 reach tests `[v]` | New precedent for the test budget (J8) |
| Entity survey feature 9 | Carcasses that scavengers eat | Meat eaters bite carcasses and eat the dropped meat `[v]` | First precedent (J5) |
| Entity survey feature 11 | Thirst | A water pool that burns at twice the food rate, a shore search, drink to full `[v]` | First precedent (J6) |
| Entity survey feature 8 | Tranquilizer guns | Darts with a fixed 650 to 699-tick sleep `[v]` | Precedent, still blocked by D08 |
| Entity survey features 6 and 7 | Track-following and driverless tours | A rail follower started by a rider. An empty Explorer keeps rolling, but there is no tour logic `[v]` | Precedent for following only |
| S7 | Sleep window, 200-tick wake lock | Same 200 ticks. Adds a desperate gate and a threat box `[v]` | Confirms and extends (J7) |
| S8 | Path back-off 20 to 400 ticks | Back-off 20 to 600 plus a 64-slot failed-target list `[v]` | New precedent (J6) |
| S9 | Leader pushes its target to the pack | An adult copies a young's target through an event mark `[v]` | Different trigger. Both fit (J9) |
| S12 | Checked control packets, one per player per tick | Controlling-passenger and owner checks, an 8-block order range, no rate limit `[v]` | Confirms the checks. Keep S12's rate limit |
| S19 | Vehicle placement check | Placement fails when the box hits blocks, with no message `[v]` | Confirms. Keep S19's message |
| S20 | Track follower at 5 blocks per second | Rail speeds 1.25, 2.5 and 5 blocks per second per axis, turn rate 10 to 40° per tick `[v]` `[inf]` | Confirms the start speed. Shapes differ (B22.39) |
| S23 | Bursts spread over ticks | A full-box leaf scan every tick `[v]` | Negative precedent. Confirms the need |
| S26 | Pose state in spawn data | The start tick is synced data, so late joiners see the right frame `[v]` | Confirms |
| S27 | One hit tick per attack clip | Spit fires at a clip tick. Melee uses the vanilla attack timer `[inf]` | Partial |
| T1 | Network object, one rebuild per tick | No power. A fence flood on every neighbour update with no debounce `[v]` | Confirms "no post ticks". Negative precedent for batching |
| T5, OC2 | Idle machines as static models | Block-entity renderers every frame, view distance 48 `[v]` | No precedent |
| T6, T7 | Hover box and machine issues | A Jade line with status and percent. A five-value status enum `[v]` | Partial precedent |
| T11 | Feature switches | 41 options. A placement filter turns each worldgen feature off `[v]` | New precedent for worldgen (J3) |
| T15 | Vehicle sync, interval 3, stop packet | Interval 1 with a 3-step client lerp, no stop packet `[v]` | Partial. Supports B22.8's interval 1 |
| OC6 | No core shader | One core shader for creature colour `[v]` | Contradicts. Open item O22 |
| ON9 | Tracking range by body size | Range 10 for every species `[v]` | No precedent |
| B25.5 | No tick mixin on a vanilla base class | A required mixin in a vanilla sweep method `[v]` | Contradicts. Open item O23 |

## The plan covers most items, and 30 items are new

**In game:** nothing here changes a decided feature. The new items make decided features cheaper or easier to test.

### What the plan already covers

| Module | The plan already asks for | JC3 adds |
| --- | --- | --- |
| P2 | Port to 26.x APIs | A shipping 26.1.2 mod that uses them `[v]` |
| B00 | Species JSON with loud errors, a client projection, feature switches | Validators, a fallback species, a reader-for-every-key rule, worldgen switches |
| B01 | One cluster per species, position-seeded looks, thread-safe worldgen | A cell-seeded species pick and a period-derived depth default |
| B12 | Edge-only sync, idle early return, per-side capabilities | A 16-bit time cap, a layout record, a `turbo` command |
| B16 | Attributes on change, culling union box, carcass stages | A skip-aware growth clock, size-scaled carcass stages, voice by growth |
| B17 | Sensor gates, need cooldown, path budget, diets as tags, sleep window | Search back-off, a failed-target list, a dirty-chunk wake, feed tiers, sleep gates |
| B18 | Breeding gates, protect the young, heredity rule | Wands for tests, a variance floor test, an event wake for guards |
| B22 | Checked key packet, server clamp, client tilt | Vanilla input for movement keys, seat memory, arcade drive numbers |
| B26 | Box-aware nodes, stuck ladder, path budget per herd | A node cap, a failed-search throttle, a column cache, a progress test |
| B27 | Pose state, part index, part ids | Clip flags, events, index self-checks |

### New items for the plan

| # | Module | Addition | From |
| --- | --- | --- | --- |
| 1 | P2 | Record the JC3 26.x API shapes in the port task plan as a reference. Store the client recipe map on `RecipesReceivedEvent` | P2.1, P2.2 |
| 2 | B00 | Species validators, a fallback record with one log line per missing species, a key-reader table with a unit test | B00.9 to B00.11, J1 |
| 3 | B00 | A depth default from `period` for species with no `depth_min` and `depth_max` | B00.12, J2 |
| 4 | B00 | Worldgen switches through one placement filter in every placed feature | B00.15, J3 |
| 5 | B01 | Species pick per cell with a seeded random source | B01.1, J2 |
| 6 | B04 | Raw meat from a carcass carries the genome. Cooked meat carries the species only | B04.1, J5 |
| 7 | B07 | Natural colour roll from species palette families for per-clone colour variation | B07.5, J21 |
| 8 | B08 | Staged colour edit with preview, Apply and an "unapplied" badge | B08.1, J22 |
| 9 | B09 | Overlay bone masks for every pass that the bake does not cover | B09.3, J20 |
| 10 | B09 | Tint lift and HSL offsets in the bake's colour step. Gait by real speed. Sway faded during action clips | B09.5, B09.8, J19, J21 |
| 11 | B11 | Synthesis slot filter at the clone threshold. A broken or picked-up egg keeps its genome | B11.11, B11.15 |
| 12 | B12 | Input early-out before the recipe check. Recipe time cap 32,767 ticks. Renderer view distance 48 | B12.25, B12.26, B12.32 |
| 13 | B12 | One layout record per machine for menu, screen, item handler and recipe viewer | B12.27, J12 |
| 14 | B15 | State in B15 that fence power changes run once per tick per network, because JC3 repeats a flood per placed block | B15.22, T1 |
| 15 | B16 | Growth clock that counts skipped time, birth stamp, 0.01-block size threshold | B16.10, B16.11, J4 |
| 16 | B16 | Carcass stages from body size. Bones 9 in 10 at the last stage | B16.12, J5 |
| 17 | B16 | Voice volume and pitch by growth | B16.14, J19 |
| 18 | B17 | Need search with back-off, failed-target list, dirty-chunk wake, palette pre-filter, source order and feed tiers | B17.17 to B17.22, B17.29, J6 |
| 19 | B17 | Sleep gates: desperate, threat check, activity windows | B17.26, B17.27, J7 |
| 20 | B17 | Hunt budget and scavenging | B17.30, B17.32, J5, J8 |
| 21 | B17, B20 | The feeder registry answers a query from the chunks inside the radius only | B20.17 |
| 22 | B18 | Breeding and Birthing Wands, a variance floor test, parent ids on the young, mate search numbers | B18.15 to B18.17, B18.19, J10 |
| 23 | B18 | A young animal's new target wakes nearby adults | B18.18, J9 |
| 24 | B19 | Temperament table, Dilophosaurus spit, defend copies the owner's attacker or victim | B19.16, B19.20, B19.21, J8, J11 |
| 25 | B21 | Spacing below the map search radius. A percent option for chest fossils | B21.18, B21.21 |
| 26 | B22 | Vanilla input for movement keys, seats and seat memory, reverse once on the track, arcade drive and tilt numbers | B22.31 to B22.33, B22.35, B22.38, J14, J15 |
| 27 | B24 | Clip and bone load checks, throat rule, coverage audit | B09.6, B24.6, B24.7, J23 |
| 28 | B25, B12 | Off, compare and break switches for every fast path. `turbo`, `rig` and `kit` debug commands | B12.31, B25.11, J13 |
| 29 | B26, B27 | Node cap, failed-search throttle, column cache, step rules, progress test, part index self-checks, pose flags and events | B26.6 to B26.10, B27.6, B27.8, J16 to J19 |
| 30 | B25 | Profile the JDK vertex-allocation claim once on 26.2 with 100 creatures. Report it upstream if true. Write no `Unsafe` code | B09.7, settled fact 25 |

## Repository facts

Each row records what the notes recorded. JC3 has no public repository. The study read the distributed jar.

| Field | Value |
| --- | --- |
| Short name | JC3 |
| Mod | JurassiCraft, mod id `jurassicraft`, version 3.0.14 `[v]` |
| Author | CAZ LLC `[doc]` |
| Licence as recorded | Proprietary, All Rights Reserved. Studied clean-room, no code reused |
| Platform | NeoForge 26.1.2 (dependency from 26.1.2.94), Minecraft 26.1.2 `[v]` |
| Loaders | NeoForge only |
| 26.2 status | No 26.2 build recorded |
| Libraries | None required for content. Optional JEI and Jade plugins `[v]` |
| Code form | Obfuscated jar. Decompiled source, 581 files in 66 packages, in the session scratchpad. Methods that did not decompile were read with `javap` `[v]` |
| Scope rule | A skip list of packages, files and folders was not read. Only registration lines of the two root classes were read |
| Packages cited by the notes | `ctygd/` (creature), `ztiurxyn/` (goals), `gyovxqy/` (components), `nkjvgug/` (food model), `incf/` and `dfdtwro/` (navigation), `octtcpmd/` (animation state), `ysjrlvxg/` (species data), `etemhnv/` (DNA records), `aprffirmi/` (offspring and eggs), `dnukfuguy/` and `ufpurxw/` (block entities and blocks), `vtrqoo/` (recipes), `qkek/` (fences), `rmeh/` and `prulnm/` (vehicles), `znpnvzxz/` (packets), `stta/`, `rtwzimpls/` and `aeygzxj/` (rendering and animation), `axvg/`, `jdbttmo/`, `uzuj/` and `kswtc/` (worldgen), `mixin/` |
| Data checked for this report | Counts of `data/` and `assets/` files, the recipe codec, the machine tick, the fence power rule, the period band setup (`javap`), carcass code, the feeder registry, the dirty-chunk map, the set-order packet, the hatched egg, the config spec, goal registration and the clip sets |

## Clean-room implementation specifications

**In game:** each specification describes a behaviour that the player sees, or a cost rule that the player does not see. It gives enough detail to build the behaviour without the JC3 code.

These 23 specifications cover every Adopt or Adapt row whose only precedent is JC3, unless an earlier specification covers it. The clean-room rule applies: study JC3, then build Nublar's own solution with the same function. These rows need no specification:

- rows that use only a vanilla or library API (B08.2, B11.11, B11.15, B12.26, B12.32, B18.17, B19.21, P2.1, P2.2)
- rows that an earlier specification or lesson already covers (B12.25 by OS2, B15.22 by T1, B16.11 by B07.1, B21.15 by S17, B21.18 by B21.11, B21.21 by S15)
- Defer and Drop rows

Build each one from the description below. **Specify, do not copy.** Start values are proposals for playtest unless the table names a source. A JC3 value is a reference, not a Nublar decision.

### J1. Species data guard rails (B00.9, B00.10, B00.11)

**In game:** a pack author adds a species file with a typo. The game starts, logs one line that names the file, the field and the value, and leaves that species out. A summoned dinosaur whose species file is missing still loads with safe values.

- **Inputs:** the species JSON files, the `SpeciesData` codec.
- **State:** a set of species ids that already logged a missing-data line. A table of species keys and the module that reads each.

1. Decode each file with the codec. After decode, test each rule in the table below. A failed rule rejects the file with one error line: file, field, value, rule.
2. A dinosaur whose species has no loaded record uses one fallback record. Log one line per species id, the first time only.
3. The Debug Stick shows "species data missing" for such an animal (B16 debug status).
4. Keep the key table in the B00 package note. A unit test reads every codec field name and fails if a field has no row in the table.
5. A key with a row but no reader yet carries "planned: Bnn" in the table. The test allows it until that module merges.

| Rule | Basis |
| --- | --- |
| Every `_min` value is at most its `_max` value | JC3 clutch rule `[v]` |
| Eye height is at most body height | JC3 rule `[v]` |
| Sizes from 0.01 to 15 blocks, stats not negative | JC3 ranges `[v]` |
| Speed values inside a species-wide range | JC3 ships `attack_speed` 185 and 0 by mistake `[v]` |
| `hatch_success` from 0 to 1 | `[inf]` |

### J2. Fossil depth default and cell-seeded species (B00.12, B01.1)

**In game:** deep fossils are older than shallow ones. A new species with only a period still appears at a fitting depth. One pocket of fossil ore holds one species, and the same spot holds the same species after a reload.

- **Inputs:** each species' `period` and optional `depth_min` and `depth_max`, the fossil column top and bottom (config), the world seed, a block position.
- **State:** an immutable map from period to band, built once on the main thread at reload (OW1).

1. List the periods that species use. Sum their lengths in million years.
2. Give each period `floor(length × layers ÷ total)` layers, at least 1. Keep each fractional part.
3. While layers are left, give one more to the period with the largest fractional part, then mark that part as used. If the sum is too large, take one away from the smallest fractional part in the same way.
4. Stack the bands from the top, youngest period first.
5. A species with its own `depth_min` and `depth_max` keeps them. The band is its default only.
6. To pick a species at a position, seed a random source from the world seed and the cell `(x >> 3, y >> 3, z >> 3)`. Pick from the species valid at that depth and biome group, with B01's rarity weights.
7. If no species is valid at that Y, try the other layers of the same cell, then use decision D02 (nearest depth band).

| Value | Start | Basis |
| --- | --- | --- |
| Cell size | 8 × 8 × 8 blocks | JC3 `[v]` |
| JC3 reference column | Y 64 to −60 (125 layers): Quaternary 2, Neogene 7, Paleogene 15, Cretaceous 27, Jurassic 19, Triassic 18, Permian 16, Devonian 21 | Settled fact 9 `[v]` |

### J3. Worldgen switches through a placement filter (B00.15)

**In game:** a server owner turns off fossil ore or amber in the config. Chunks generated afterwards have none. Chunks already generated keep theirs.

- **Inputs:** a `worldgen` group in the `features` TOML section, one boolean per feature.
- **State:** none beyond the config values.

1. Register one placement modifier type with one field: the switch name.
2. The modifier returns no positions when the switch is false, and the input position otherwise.
3. Put the modifier first in every Nublar placed feature.
4. For structures, test in the B21 task plan whether the T11 data condition can remove a structure set on both loaders `[inf]`.
5. Document in `docs/config-reference.md` that a change applies to new chunks only.

| Value | Start | Basis |
| --- | --- | --- |
| Switches | One per placed feature, all on | JC3 has 18 worldgen switches `[v]` |

### J4. Growth clock that counts skipped time (B16.10)

**In game:** the player sleeps through the night. The baby T. rex has grown by a full night when the player wakes. A chunk that was unloaded for an hour shows animals an hour older. Clients receive no age updates while nothing changes.

- **Inputs:** the overworld game time and day time, growth config, `growth_days` per species.
- **State:** per level, saved: `skippedTicks` (long). Per animal, saved and synced once: `birthStamp` (long), optional `frozenAge` (long).

1. The clock is game time plus `skippedTicks`.
2. Each server tick, compare the day time with the last day time plus 1. When the daylight cycle runs and the day time jumped forward, add the jump to `skippedTicks`. A backward jump adds nothing `[inf]`.
3. When `skippedTicks` changes, send one packet with its new value to every client.
4. At hatch, set `birthStamp` to the clock. Age in ticks is the clock minus `birthStamp`, clamped from 0 to `growth_days × 24,000`.
5. Every 8 ticks, the server computes the growth fraction. When it passes a size or stat step, it runs the B07.1 rebuild once.
6. A growth serum or a gene effect moves `birthStamp`. No code counts age per tick.
7. With growth off in the config, store `frozenAge` and use it in place of the clock.

| Value | Start | Basis |
| --- | --- | --- |
| Growth check | Every 8 ticks | JC3 age unit `[v]` |
| Skipped-time packet | One long (8 bytes), on change only | JC3 `[v]` |

### J5. Carcass stages, raw meat and scavenging (B16.12, B04.1, B17.32)

**In game:** a dead Velociraptor gives 2 cuts of meat, a dead T. rex about 6 and a Brachiosaurus 10. The last cut leaves bones. Raw meat carries the dead animal's genome to the lab. A hungry raptor that finds a carcass eats from it.

- **Inputs:** the dead animal's box at death, its genome, its fossil set, `needs.carcass_minutes`.
- **State:** `CarcassEntity` fields (plan B16): species, growth fraction, meat pool, plus `stagesLeft` (int) and a copy of the genome.

1. At death, set `stagesLeft` to `clamp(floor(√(width × height)) × 2, 1, 10)` from the box at death.
2. Each player use takes one stage after a 10-tick gap. It drops 1 to 2 raw meat plus the Looting level.
3. Raw meat carries a genome component with species, the animal's DNA quality and genome. A burning carcass drops cooked meat with the species only.
4. When `stagesLeft` reaches 0, drop each bone of the species fossil set with a 9-in-10 chance, flagged fresh, plus tissue (B04 remains). Then remove the carcass.
5. A carnivore below its seek threshold (75 %, B17.6) lists a carcass in its food sources after dropped items and before feeders (J6).
6. A scavenger bite takes one share from the meat pool and adds it to the biter's hunger directly. It spawns no item entity. The B16 meat pool rules stay.
7. The carcass decays after `needs.carcass_minutes` whoever made it (lead).

| Value | Start | Basis |
| --- | --- | --- |
| Stages | `clamp(floor(√(w × h)) × 2, 1, 10)` | JC3 `[v]`, settled fact 10 |
| Meat per stage | 1 to 2, plus Looting | JC3 `[v]` |
| Bone chance | 9 in 10 per bone type | JC3 `[v]` |
| Use gap | 10 ticks | JC3 hurt gap `[v]` |

### J6. Need search that sleeps until something changes (B17.17 to B17.22, B17.29)

**In game:** a hungry Triceratops with no food in reach stops searching often. It searches again at once when a player drops food or places a plant near it. It does not walk again and again to a bush it cannot reach. A drinking animal turns to face the water.

- **Inputs:** the need state, the species diet tags, reach and width, the feeder registry, the level change map.
- **State:** per animal and need: failure count, next search time (game time), failed position and time, early-retry count. Per animal: a ring of 64 failed targets (position, expiry). Per level: a map from chunk to the last change time.

1. Keep food sources in this order: dropped food items, carcasses (J5), feeders, diet blocks inside the grazing band (S6). Water has its own source.
2. The first source that finds a reachable target wins. Each search runs at most 6 path tests. A position on the failed-target list costs no test.
3. Before reading the blocks of a 16-block section, ask the section palette whether any state in the diet tag exists. Skip the section if none does.
4. When a search finds nothing, add 1 to the failure count. Set the next search time to now plus `base × 2^failures`, capped, plus a random value up to that delay.
5. Search early if any of these holds:
   - the need is urgent (below the 25 % run threshold, B17.6)
   - the animal moved more than 8 blocks from the failed position (at most 2 times)
   - the change map shows a change in a chunk inside the search radius after the failure time
6. Write the change map on a block placement, a block break, an item entity join and a feeder slot change. When it holds more than 512 chunks, remove entries older than 1,200 ticks. Clear it on level unload (B25.4).
7. A path that fails, a walk over 600 ticks, or more than 2 re-paths adds the target to the failed-target list for 600 ticks. A new failure refreshes the entry. A throttled search (J16) is not a failure.
8. Arrival: horizontal distance at most `1.75 + width ÷ 2`, vertical at most body height + 1. Then the head-contact rule decides the bite (lead, 2026-10-09).
9. For water: turn at most 15° per tick toward the water, slide to the edge, drink when the angle error is under 20°. Give up after 60 ticks.
10. Food value comes from the tier tag of the item or block. An untagged item counts as the farmed tier. An untagged block counts as the free tier.
11. Build the timers on SBL memories with an expiry. Gate the sensors with `onlyScanIf` (library rule).

| Value | Start | Basis |
| --- | --- | --- |
| Base delay | 20 ticks (food), 40 ticks (water) | JC3 `[v]` |
| Delay cap | 600 ticks (30 s) | JC3 `[v]` |
| Failed-target list | 64 slots, 600 ticks | JC3 `[v]` |
| Path tests per search | 6 | JC3 `[v]` |
| Change life, prune size | 1,200 ticks, 512 chunks | JC3 `[v]` |
| Final approach | 0.35 blocks | JC3 `[v]` |
| Tier ratios | free 1, farmed 2.5, effort 5, premium 10, bale 45 | JC3 300 / 750 / 1,500 / 3,000 / 13,500 `[v]`, ratios `[inf]` |

### J7. Sleep gates (B17.26, B17.27)

**In game:** a starving animal stays awake. An animal with a hunter nearby does not lie down. Night animals sleep by day, and day animals by night.

- **Inputs:** the species activity window, hunger and thirst, the shared nearby-entity memory.
- **State:** a cached threat result and its time. `WAKE_LOCK` and `SLEEP_COOLDOWN` from S7.

1. The species window comes from data: diurnal, nocturnal or dawn and dusk.
2. Sleep cannot start while hunger or thirst is below the 25 % run threshold.
3. Every 20 ticks, test the nearby-entity memory for a threat inside a box of 10 × 5 × 10 blocks. A threat is an awake mob that targets this animal, or a larger mob of another species, or a monster. Cache the result until the next test.
4. A threat blocks sleep and wakes a sleeper.
5. S7 steps 3 to 5 stay: damage wakes the animal and sets `WAKE_LOCK`, and each wake sets `SLEEP_COOLDOWN`.

| Value | Start | Basis |
| --- | --- | --- |
| Diurnal sleep | Day time 16,000 to 24,000 | JC3 `[v]` |
| Nocturnal sleep | Day time 0 to 12,000 | JC3 `[v]` |
| Dawn and dusk sleep | Day time 18,000 to 22,000 and 6,000 to 10,000 | `[inf]`. JC3's window is inverted |
| Threat box, cache | 10 × 5 × 10 blocks, 20 ticks | JC3 `[v]` |
| Wake lock | 200 ticks | JC3 `[v]`, S7 |

### J8. Hunt budget and temperament (B17.30, B19.16)

**In game:** a hungry raptor picks prey fast and never stalls the server when prey hides. A scared herbivore never fights back. A neutral one does when hurt.

- **Inputs:** the scored prey list from B17.7, the failed-target list (J6), the species temperament.
- **State:** a target re-test time, a post-hunt lockout time.

1. The prey pass sorts candidates by B17.7's score. It tests reach for at most 6 candidates per scan. Failed targets cost no test.
2. An optional species ratio skips prey whose max health is above the ratio times the hunter's max health. Default: off.
3. While hunting, re-test reach every 40 ticks. A failed test ends the hunt and adds the target to the failed-target list.
4. When any hunt ends, start a lockout of 50 to 69 ticks before the next prey pick. This is apart from the stamina hunt cooldown (B17).
5. Read the temperament table for retaliation, alerts and panic.

| Temperament | Hunts | Retaliates and alerts | Panics when hurt |
| --- | --- | --- | --- |
| aggressive | When hungry, or by aggression gene | Yes | No |
| neutral | When hungry | Yes | No |
| passive | Never | No | Yes |
| scared | Never | No | Yes |

| Value | Start | Basis |
| --- | --- | --- |
| Reach tests per scan | 6 | JC3 `[v]` |
| Re-test | 40 ticks | JC3 `[v]` |
| Lockout | 50 to 69 ticks | JC3 `[v]`, settled fact 19 |
| Prey health ratio | Off. JC3 uses 0.9 | JC3 `[v]` |

### J9. Guard-the-young wake (B18.18)

**In game:** a juvenile raptor gets into a fight. Within half a second the nearby adults of its species join it.

- **Inputs:** target changes and damage of animals with growth 0.5 or less.
- **State:** per level, a map from chunk to the game time of the last young-target event. Per adult: the time of its last check and a retry delay.

1. When a young animal gets an attack target or takes damage from a mob, write the game time for its chunk.
2. An adult (growth above 0.75, awake, not sitting, no target) checks only when its chunk or a neighbour chunk has an event newer than its last check. It checks at most every 10 ticks.
3. On a check, search a box of 8 × 3 × 8 blocks for a same-species young with a target. Copy that target. Skip creative players and the adult's owner.
4. With no event, the adult retries with a delay of `10 << min(failures, 8)` ticks, capped at 200.
5. B18.6's search order stays: a baby first, then a player at half the follow range.
6. Remove events older than 400 ticks. Clear the map on level unload.

| Value | Start | Basis |
| --- | --- | --- |
| Event life | 400 ticks | JC3 `[v]` |
| Check rate | 10 ticks at most | JC3 `[v]` |
| Search box | 8 × 3 × 8 blocks | JC3 `[v]` |
| Retry cap | 200 ticks | JC3 `[v]` |

### J10. Breeding search, test wands and variance floor (B18.15, B18.16, B18.19)

**In game:** calm pairs find each other without lag. A tester pairs two animals with a wand. A paddock bred for 10 generations still shows varied animals.

- **Inputs:** the lead's three breeding gates (nest, fertility, stress), the heredity function of B18.
- **State:** none beyond SBL's breeding memories.

1. After the lead's gates pass, poll for a mate every 40 to 79 ticks. Then roll 1 in 20.
2. Pick the nearest eligible same-species animal of the other sex within 16 blocks that has no target. Test paths for at most 6 candidates.
3. Approach for at most 300 ticks. Mating starts when both boxes, inflated by 3 blocks, touch.
4. Use SBL `BreedWithPartner` with these values. Write no own breeding behaviour (library rule).
5. Breeding Wand (creative only): use it on one animal, then on another of the same species and the other sex. They walk together and mate on contact. The wand skips age, health and cooldown, never the species test.
6. Birthing Wand (creative only): the female lays her clutch at once.
7. Variance floor test: run the heredity function on a closed pair line for 10 generations, 50 young per generation. For each number gene, the spread must stay above a floor. If the test fails, the lead tunes `editing.variation_percent`. The heredity rule does not change.

| Value | Start | Basis |
| --- | --- | --- |
| Poll, roll | 40 to 79 ticks, 1 in 20 | JC3 `[v]` |
| Search radius, path tests | 16 blocks, 6 | JC3 `[v]` |
| Approach limit | 300 ticks | JC3 `[v]` |
| Spread floor | Half the founder spread | `[inf]` |

### J11. Venom spit (B19.20)

**In game:** a Dilophosaurus faces its target, opens its frill and spits. The target goes blind. With the poison gene it is poisoned too.

- **Inputs:** an attack target, the `spit` clip, the B10 poison gene.
- **State:** the next spit time.

1. Start only with a target inside the range and 20 ticks of line of sight in a row.
2. Play the `spit` clip through the pose state (J19). A server event at the release tick fires a projectile.
3. Aim at the target, raised by 0.2 times the horizontal distance.
4. On a hit: damage, blindness, and Poison only with the poison gene (B10).
5. The projectile ignores animals of the shooter's species, except the target.
6. Wait for the interval before the next spit. A target that is already blind falls back to the bite. JC3 leaves such a target alone, which is a dead end.

| Value | Start | Basis |
| --- | --- | --- |
| Range, interval | 10 blocks, 40 ticks | JC3 `[v]` |
| Release tick, clip length | 20 of 40 ticks | JC3 `[v]` |
| Projectile speed | 1.5 blocks per tick | JC3 `[v]` |
| Damage, blindness | 4 (2 ♥), 600 ticks | JC3 `[v]` |

### J12. Machine layout record (B12.27)

**In game:** the slots on a machine screen, the slots that hoppers reach and the slots that a recipe viewer fills always agree.

- **Inputs:** one record per machine.
- **State:** none. The record is static data.

1. A layout record holds a name, a screen width and height, a slot list and the player inventory position.
2. A slot entry holds an index, x, y, a role, an accept test, an outside-insert flag, an outside-extract flag and a side mask.
3. Roles: input, output, fluid, storage, environment, result. Output and result are extract only.
4. The menu builds its slots from the record. The screen draws slot frames from it. The item handler reads the flags and the side mask.
5. At construction, check that entry i has index i. A loaded inventory with another slot count resizes and logs one warning.
6. A debug command prints a layout: size, slot count, progress regions.

| Value | Start | Basis |
| --- | --- | --- |
| Default screen | 176 × 166 pixels | JC3 and vanilla `[v]` |
| Slot size | 18 pixels | JC3 and vanilla `[v]` |

### J13. Fast-path switches and machine test commands (B25.11, B12.31)

**In game:** a tester turns off one shortcut and sees the same result. A machine test runs a full chain in seconds.

- **Inputs:** server config, JVM properties, operator commands.
- **State:** a `turbo` flag in memory, not saved.

1. Each fast path has three switches:
   - an off switch in the server config that selects the slow path
   - a compare switch that runs both paths and logs any difference once, with a count
   - a JVM property that breaks the fast path on purpose in a test, to prove that the compare switch fires
2. Apply the switches to the part index (J18), the column cache (J16), the need search sleep (J6) and the bone masks (J20).
3. `/projectnublar machine turbo on|off` (operator level 2) makes every machine job finish in 20 ticks. The flag is not saved.
4. `/projectnublar machine rig <machine>` fills a machine with test input. `kit` gives a test kit.

| Value | Start | Basis |
| --- | --- | --- |
| Turbo job time | 20 ticks (1 s) | JC3 `[v]` |
| Command permission | Level 2 | JC3 game master level `[v]` |

### J14. Vehicle input and seats (B22.31, B22.35, B22.38)

**In game:** the driver presses W and the Jeep moves at once. A rider who logs out returns to the same seat. On a track, one press of the backward key turns the Explorer round.

- **Inputs:** the driver's last client input (vanilla 26.x), extra keys, seat clicks.
- **State:** one synced int per seat (passenger entity id or −1). A saved map from player UUID to seat index. An input byte.

1. The server reads forward, back, left, right and jump from the controlling passenger's last vanilla input.
2. Extra keys (horn, driverless start) travel in one packet: vehicle id and one byte. The client sends it only when the byte or the vehicle changes.
3. The server accepts the packet only from a non-spectator who is the controlling passenger (seat 0), at most once per tick (S12).
4. When a player boards, use the seat that the map remembers for that player if it is free, else the first free seat. Save the map with the vehicle.
5. When the driver leaves, clear the input byte.
6. On a track, a rising edge of the backward key flips the travel direction once (S20).

| Value | Start | Basis |
| --- | --- | --- |
| Extra-key packet | 2 to 4 bytes of payload, about 33 to 36 bytes on the wire | JC3 `[inf]` |
| Seats | Jeep 2, Explorer 4 | Decision D25 default |

### J15. Arcade drive and body tilt (B22.32, B22.33)

**In game:** the Jeep speeds up fast, holds its top speed, turns smoothly and does not slide. Its body pitches and rolls on slopes.

- **Inputs:** the input byte (J14), the B22.3 handling record (top speed, acceleration time, turn rate).
- **State:** velocity, a yaw rate, four smoothed edge heights (client).

1. Each tick on the side that moves the vehicle, multiply velocity and yaw rate by the drag `d`.
2. Add gravity when the vehicle is in the air.
3. Push along the facing: `p = top speed × (1 − d)` forward, a third of that in reverse (B22.3). A small push applies while only steering.
4. Steering changes the yaw rate by a factor times the push, clamped to the turn rate. Steering inverts in reverse.
5. The server clamps speed and distance per update (B22.1).
6. Client only: four probes walk the front, rear, left and right edges in 0.25-block steps. Each takes the highest collision top within 32 blocks below.
7. Run the probes when the vehicle moved or turned, at most every 20 ticks, offset by entity id.
8. Smooth each height toward its probe value. Compute pitch from front and rear, and roll from left and right.

| Value | Start | Basis |
| --- | --- | --- |
| Drag per tick | 0.8 | JC3 `[v]` |
| Push forward, reverse, steer-only | 0.1, −0.05, 0.05 blocks per tick at the middle preset | JC3 `[v]` |
| Top speed | 0.5 blocks per tick (10 blocks per second) at the middle preset | JC3 `[inf]` |
| Yaw rate cap | 3° per tick | JC3 `[v]` |
| Height smoothing | 0.15 blocks per tick | JC3 `[v]` |

### J16. Path caps for large bodies (B26.6 to B26.9)

**In game:** a Brachiosaurus finds paths without a lag spike. A boxed-in animal never fires a burst of failing searches. A sauropod climbs a 2-block bank but does not step over a wall.

- **Inputs:** the species node cap and follow range, `BodyFit`, the body height.
- **State:** per animal: the game time of the last failed search and a "throttled" flag. Per search: a column cache.

1. Before each search, set the node multiplier to `min(1, cap ÷ (max(follow range, 16) × 16))`.
2. When a search returns no path or a path that does not reach, store the game time.
3. A new search in the same tick returns no path and sets "throttled". A re-path to the same target stays allowed.
4. For each footprint column, compute the path types over the body height once per search. Store them by column. Clear the cache at the start and the end of the search.
5. `BodyFit` stays the one block reader. The cache wraps its probe.
6. A compare switch (J13) checks each cached answer against a fresh `BodyFit` answer.
7. Set the step-height attribute to `max(1, ceil(height ÷ 2) ÷ 2)`.
8. If a non-full block with a collision shape that is not pathfindable stands in the step zone of a move, use step height 0 for that move.
9. The herd rule "one large-species path per herd per tick" stays.

| Value | Start | Basis |
| --- | --- | --- |
| Node cap | 200 | JC3 `[v]`. About 200 nodes at follow range 64, against 1,024 in vanilla |
| Nublar follow range 35 | About 200 nodes, against 560 in vanilla | `[inf]` |
| Step height | Gallimimus (2.39 high) 1.0, Brachiosaurus (8.87 high) 2.5 | Formula `[inf]` |

### J17. Progress test as the stuck trigger (B26.10)

**In game:** an animal that tries to move but goes nowhere for 3 s starts its recovery. A herd member that waits behind a mate does not.

- **Inputs:** the animal's position, its path state, the herd's waiting rule.
- **State:** a window counter, a "trying" counter, the window start position.

1. Every 2 ticks, add 2 to the window counter. If the animal has an unfinished path, or its target is farther than its width + 2, add 2 to the trying counter.
2. If the animal moved 2 blocks or more from the window start, reset both counters and the start position.
3. At 60 counted ticks, the animal is stuck if the trying counter is at least a third of the window and it moved under 2 blocks.
4. A block caused by herd mates counts as waiting, not stuck (herd decision 14).
5. Stuck starts the first rung of B26's stuck ladder. A further rung: test 16 directions, up to 8 steps each, for the longest run of cells where the body fits and ground lies within 2 blocks below. Walk there for 60 ticks if the run is longer than 1.

| Value | Start | Basis |
| --- | --- | --- |
| Window, distance | 60 ticks, 2 blocks | JC3 `[v]` |
| Directions, steps, walk | 16, 8, 60 ticks | JC3 `[v]` |

### J18. Part index self-checks (B27.6)

**In game:** arrows always hit the right part, even after a part was added or removed in an odd order. A level with an Ender Dragon keeps the index on.

- **Inputs:** the planned chunk-section part buckets (B27), every part in the level.
- **State:** per level: the buckets, a count of indexed parts. Per part: its current bucket key.

1. A part that moves compares its new bucket key with the stored key. If equal, do nothing more. Else move it between buckets.
2. Before answering a query, compare the indexed count with the level's part count. If they differ, rebuild all buckets from the level's list.
3. Index every part in the level, Nublar parts and foreign parts alike. JC3 turns its index off when a foreign part exists. Nublar does not.
4. Pad the query box by the largest part reach from species data, computed at reload. Pad sideways and below, as the part offsets require `[inf]`.
5. If the padded box spans more than 1,024 sections, return the full list.
6. Remove a part when its owner leaves the level. Drop the whole level entry on level unload.
7. Add the off, compare and break switches (J13).

| Value | Start | Basis |
| --- | --- | --- |
| Section size | 16 blocks | JC3 `[v]`, hitbox design |
| JC3 pads | 4 blocks sideways, 12 below (largest JC3 box side 4.45 blocks) | JC3 `[v]` |
| Full-list limit | 1,024 sections | JC3 `[v]` |

### J19. Pose flags, events and growth voice (B27.8, B09.5, B16.14)

**In game:** a roar plays to its end and a look-around cannot cut it. Every client hears the roar at the right moment. The tail and head sway less while the T. rex roars. A hatchling squeaks in a high voice.

- **Inputs:** the planned pose state (id, start tick, loop flag), per-clip data: length, exclusive flag, hold flag, events.
- **State:** the current pose and start tick, the previous pose and its switch tick.

1. A request fails if the clip is unknown for the species, or if the current clip is exclusive and the new one is not.
2. Two calls exist: "start if not playing" and "restart".
3. A hold clip stays on its last frame until the server clears it.
4. Each clip has events: name, tick, side. The server runs server events from the start tick (hit tick S27, spit release J11). Clients run client events from the synced start tick, so sounds need no packet. A looping clip fires its events once per loop.
5. Use GeckoLib 5 triggered animations and keyframe events where the clip file has them (library rule). Species data holds only events that the clip files lack.
6. Crossfade over 4 ticks through the GeckoLib transition length.
7. Bone updaters (head look, sway) scale their effect by `1 − max(walk-block weight, action weight × suppression)`.
8. Choose walk or run from real speed between two thresholds.
9. Scale voice volume, footstep volume and pitch by the growth fraction, with a small random jitter.

| Value | Start | Basis |
| --- | --- | --- |
| Crossfade | 4 ticks | JC3 `[v]` |
| Suppression | 0.75 (T. rex 0.8) | JC3 `[v]` |
| Walk, run thresholds | 0.05 and 0.2 blocks per tick | JC3 `[v]` |
| Voice volume, footstep volume | 0.2 to 1.0, 0.1 to 1.0 | JC3 `[v]` |
| Pitch | 2.5 (hatchling) to 1.0 (adult) | JC3 `[v]` |

### J20. Overlay bone masks (B09.3)

**In game:** the glowing eyes of a dinosaur cost only a few bones per frame, not a second full model.

- **Inputs:** each overlay or glow texture that the bake does not cover, the GeckoLib 5 model with its cube UV boxes.
- **State:** per texture and model: a mask of bones (draw, carried, hidden). A "not ready" state.

1. In the prepare stage of the client reload listener, scan each overlay texture off the render thread.
2. For each bone, test each cube's UV box for a texel with alpha above 0. A bone with such a texel draws. A bone whose own cubes are clear but whose child draws is carried: skip its cubes, draw its children. Every other bone is hidden.
3. Until the scan is ready, draw the whole model in that pass.
4. If the mask names a bone that the model lacks, or would hide the root bone, draw the whole model and log once.
5. Apply the mask through GeckoLib 5 bone visibility for that render layer (library rule).
6. Clear all masks on a resource reload.

| Value | Start | Basis |
| --- | --- | --- |
| JC3 passes | About 6 per median creature, T. rex 7 at most | Performance note §5.4 `[v]` |

### J21. Colour roll from species palettes and tint lift (B07.5, B09.8)

**In game:** two cloned raptors differ in colour, and both look natural for their species. Tinted hides stay bright.

- **Inputs:** palette definitions, each species' palette list per colour layer, the per-clone roll (B07), the morph roll.
- **State:** none beyond the rolled layer colours in `DinoData`.

1. A palette has a hue range, a saturation range, a lightness range and a weight.
2. Each species lists the allowed palettes per colour layer.
3. Natural roll: pick a palette by weight, then a hue, saturation and lightness inside its ranges.
4. Random roll: hue 0 to 359 with muted saturation and lightness.
5. Per-clone variation (B07) uses the natural roll at hatch. A morph (albino, melanistic) replaces the result.
6. A layer may carry an HSL offset from another layer's colour, for example a darker outline.
7. In the bake's colour step, multiply each tint channel by `255 ÷ 195` and clamp at 255.

| Value | Start | Basis |
| --- | --- | --- |
| Palette weight default | 10 | JC3 `[v]` |
| Palettes per layer | About 6 | JC3 `[v]` |
| Random roll | Saturation 20 to 35, lightness 26 to 38 | JC3 `[v]` |
| Tint lift | 255 ÷ 195, about +31 % | JC3 `[v]` |

### J22. Staged colour edit (B08.1)

**In game:** in the Sequencer's edit tab the player rolls colours, sees them on the preview animal, and commits them with Apply. A badge shows "Unapplied changes" until then.

- **Inputs:** button presses through the menu button route, the drive or tube in the menu.
- **State:** on the server menu: a preview colour set, a preview state (saved, natural, random, reset), an "unapplied" flag.

1. A button press reaches the server through the vanilla menu button route. The server checks that the menu is open and that an input is present.
2. Natural and random rolls use the server random (J21). Reset clears the preview to the species default.
3. The preview syncs to the client. The client draws it on the preview entity (B08.2).
4. Apply sends the colours through the checked edit packet of B07. Only then does the genome change. The "unapplied" flag clears.
5. Copy colours: write the saved colours of one item onto another of the same species. Refuse with a reason when the species differs or the source has no saved colours.

### J23. Art load checks (B24.6, B24.7, B09.6)

**In game:** an art mistake shows as one clear log line. A long neck bends with its throat.

- **Inputs:** GeckoLib 5 models and clips, species bone lists, overlay textures.
- **State:** a set of logged misses per model.

1. Every bone lookup goes through a cache. A missing bone logs once, with the list of bones the model has.
2. A missing clip logs once per species, with the expected file path.
3. A species list of required bones fails the model build in a development run when a bone is missing.
4. For each `neckN` bone, look for a `throatN` bone that is not its child. If found, apply the same rotation to the throat bone.
5. A validate-on-load switch checks every clip against its model for unknown bones.
6. A coverage-audit switch counts opaque overlay texels outside every bone UV box, so the artist sees pixels that never draw.

## Open items for the lead

The lead answered items O22 to O28. Decisions register rows D69 to D75 in the plan hold the answers.

## Post-Beta

Everything in this section is outside Beta. Nothing here is a Beta recommendation.

| Feature | Plan status | JC3 precedent | Note |
| --- | --- | --- | --- |
| Hybrids from exact parent lists | Backlog | A multiset match of parent species at full DNA `[v]` | B00.13 |
| Prehistoric plants, peat and moss | Release 1 (R01) | 49 plants, moisture-driven peat, plant DNA chain `[v]` | B03.1, B25.12 |
| Bugs, plankton and krill for insect and filter feeders | Not in the plan | Bug crate, swarm blocks `[v]` | — |
| Aircraft | Backlog | Helicopter with stabiliser and attitude lock `[v]` | B22.45 |
| Skeleton displays and assembly | Excluded from B02 | A 5 × 5 grid that matches species data `[v]` | — |
| Rare ruins with ready-made DNA | Not in the plan | Visitor Centre with 9 loot rooms `[v]` | — |
| Park decoration sets | Release 1 (R03) | Concrete, reinforced stone, 136 glass blocks, signs, murals `[v]` | Art-driven |
| Live-bearer machine | Not in the plan | Four nutrient bars, food data map `[v]` | B11.14 |
| Status tablet | D18 default: none | PaleoPad `[v]` | B19.18 |

## Conclusion

JC3 is the first surveyed mod that ships the same genre on the same platform generation. It confirms the plan's architecture: a server-owned pose state, part buckets per section, edge-only machine sync, data species with loud errors and no natural spawns. Its value for Nublar is a set of cost rules that keep a big park cheap: searches that sleep until something changes, a growth clock that needs no per-tick work, and path caps for big bodies. It also shows what not to copy: per-tick leaf scans, whole-level feeder scans, a fence flood with no debounce and data keys that no code reads. JC3 has none of Nublar's signature systems, so those stay Nublar's own work. Two written rules meet JC3 head-on: OC6 and B25.5. The lead decides them through O22 and O23. The 23 specifications and 7 open items give each module owner a start. Each open item has a default.

## Sources

### Local documents

- [docs/project-plan.md](../docs/project-plan.md): P2, B00 to B27, global constraints, decisions D01 to D39.
- [docs/codebase-analysis.md](../docs/codebase-analysis.md), sections 11 and 12.
- [docs/ai-behaviour-genetics-reconciliation.md](../docs/ai-behaviour-genetics-reconciliation.md): gene map, H and P defaults, RC rows.
- [Reference mod survey for Beta.md](Reference%20mod%20survey%20for%20Beta.md): row ids, S1 to S27, O1 to O6, twelve features with no precedent.
- [Optimization and tech mod survey for Beta.md](Optimization%20and%20tech%20mod%20survey%20for%20Beta.md): row ids, T1 to T15, O7 to O21, lessons OS, OC, OM, ON, OW.
- The bake spec: [2026-10-08-dino-layer-baking-design.md](../docs/superpowers/specs/2026-10-08-dino-layer-baking-design.md).

### Research notes (5 files)

All in `research_notes/JurassiCraft 3 survey/`:

- [ai_and_behaviour.md](../research_notes/JurassiCraft%203%20survey/ai_and_behaviour.md)
- [genetics_species_and_cloning.md](../research_notes/JurassiCraft%203%20survey/genetics_species_and_cloning.md)
- [vehicles_and_tech.md](../research_notes/JurassiCraft%203%20survey/vehicles_and_tech.md)
- [performance_and_rendering.md](../research_notes/JurassiCraft%203%20survey/performance_and_rendering.md)
- [gameplay_content_and_world.md](../research_notes/JurassiCraft%203%20survey/gameplay_content_and_world.md)

### Checks made for this report (2026-10-11)

- File counts of the jar's `data/` and `assets/` folders: JSON per folder, recipes, loot tables, advancements, worldgen files, textures.
- The decompiled fence power rule and its callers, the machine tick and the recipe codec, the carcass and meat code, the carcass decay rule, the feeder registry, the dirty-chunk map, the set-order packet, the hatched egg use, the config spec and its registration, goal registration, the clip sets, the hunt cooldown, the ground navigation.
- `javap` of the geologic period setup, to settle the band split.
