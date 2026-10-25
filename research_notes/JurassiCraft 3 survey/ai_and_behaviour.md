# JurassiCraft 3.0.14: AI, behaviour and movement

**Scope:** how JurassiCraft 3.0.14 (NeoForge 26.1.2, CAZ LLC, paid, proprietary) decides what a dinosaur does and how it moves. The note covers the AI architecture, the goal list, needs, foraging, hunting, herds, sleep, breeding, death and carcasses, special moves, path finding for large bodies, multipart bodies, the animation state machine, and taming. It maps each technique to Project Nublar modules B00 to B27 and P2.

**Clean room.** The note describes behaviour, state, steps, numbers and timings in my own words. It copies no code. Class names in the jar are obfuscated, so each class has a descriptive name that I inferred, followed by its path under `src/caz/jurassicraft/`. File names repeat across packages, so the path is the only safe identifier. I skipped the excluded packages and classes (including those added by the coordinator on 2026-10-11), the `raw/jurassicraft/` directory and the `META-INF` licence files. I read none of the licensing, entitlement, integrity or anti-tamper code. I describe egg hatching by its timer only.

**Label key:** `[v]` verified in the decompiled source or the data files (path and lines given). `[inf]` my inference. `[doc]` text the mod ships (lang file, config comments). Perf ratings: LOW = constant work or interval-gated; MED = a bounded box or block scan per tick per animal; HIGH = unbounded or quadratic work per tick.

**Date of reading:** 2026-10-11. Nothing was run. Nine methods did not decompile (creature constructor, attack, herd start test, unstick destination search, part index, part update, animation definition, dimension-change push-out, sound range test). I read the first six with `javap` and describe only what I could confirm.

---

## 1. Sources read

| Area | Descriptive name (inferred) | Path |
|---|---|---|
| Creature base | dinosaur entity base | `ctygd/omogwgxr.java:80-1893` |
| Land creature | land dinosaur (goal list) | `ctygd/gjtxsudo.java:15-123` |
| Marine creature | aquatic dinosaur | `ctygd/kijk.java:19-119` |
| Enums | life stage; order; sex; death cause | `ctygd/kodkieb.java`, `dnukfuguy.java`, `rtwzimpls.java`, `aeygzxj.java` |
| Growth clock | world clock that counts skipped time | `ctygd/ummp.java:13-94` |
| Herd size helper | herd size (config or species) | `ctygd/jgdp.java` |
| Body parts | part entity and part index | `ctygd/vtclb.java`, `dbgouk.java`, `kdhoqn.java` (javap) |
| Components | metabolism; reproduction; ownership; genetics; inventory | `gyovxqy/gjtxsudo.java`, `kodkieb.java`, `ummp.java`, `aeygzxj.java`, `vtclb.java` |
| Goals | 28 goal classes and 3 helpers | `ztiurxyn/*.java` (31 files) |
| Food model | source registry, sources, tiers, search, blacklist, dirty-chunk tracker | `nkjvgug/dbgouk.java`, `kodkieb.java`, `gjtxsudo.java`, `dnukfuguy.java`, `vtclb.java`, `kdhoqn.java`, `jgdp.java`, `onzkovhp.java`, `aeygzxj.java` |
| Navigation | ground navigation, node evaluator, move control | `incf/aeygzxj.java`, `dnukfuguy.java`, `vtclb.java` |
| Water movement | water navigation, swim control, water-cell tests, look control | `dfdtwro/gjtxsudo.java`, `aeygzxj.java`, `dnukfuguy.java`, `vtclb.java` |
| Animation | state machine, event record, per-species sets, definition record | `octtcpmd/dnukfuguy.java`, `vtclb.java`, `kodkieb.java`, `ummp.class` (javap) |
| Species data | species record and enums | `ysjrlvxg/dbgouk.java:25-652`, `jgdp.java`, `vtclb.java`, `aeygzxj.java`, `kdhoqn.java`, `kodkieb.java` |
| Species JSON | 129 files | `raw/data/jurassicraft/jurassicraft/species/*.json` |
| Tags | feed tiers, food tags | `raw/data/jurassicraft/tags/{block,item}/feed/*.json`, `block/herbivore_food_candidates.json`, `block/filter_feeder_food.json`, `item/plant_food.json`, `item/bug_food.json` |
| Fence block | electric fence segment | `qkek/aeygzxj.java:45-266` |
| Feeder | feeder block entity, registry, food source | `dnukfuguy/ummp.java:26-289`, `ufpurxw/dbgouk.java`, `ufpurxw/jgdp.java` |
| Pack helper, egg | pack-size count; egg timer | `aprffirmi/gjtxsudo.java`, `aprffirmi/dnukfuguy.java` |
| Items and projectiles | breeding wand; PaleoPad; dart item; dart; venom | `bugratnc/gjtxsudo.java`, `fgwme.java`, `kodkieb.java`, `ysulhvcy/aeygzxj.java`, `dbgouk.java` |
| Packets | set order; open order screen | `znpnvzxz/onzkovhp.java`, `gjtxsudo.java` |
| Genetic traits | four trait multipliers | `etemhnv/ummp.java:7-74` |
| Config | server config class | `dnukfuguy.java:17-29` (root package, the config class) |
| Registration | entity types, food sources, spawn | `core/vtclb.java:164,171`, `core/JCMobCategories.java`, `Jurassicraft.java:27`, `axvg/onzkovhp.java`, `axvg/fgwme.java` |
| Mixins | part scan; visited-set size | `mixin/LevelPartScanMixin.java`, `mixin/BlockIntersectVisitSetSizeMixin.java` |

---

## 2. What to take from this note (top findings)

1. **JC3 uses plain vanilla goals. It has no brain, no memory modules and no own scheduler.** 28 goal classes, one land creature class and one aquatic class. [v] `ctygd/gjtxsudo.java:37-72`, `ctygd/kijk.java:42-46`. All the "intelligence" is gate tests, per-goal backoff timers and a few event-driven wake-ups. Target: B16, B17.
2. **One number drives both needs: adult health.** Energy pool = health squared x 7.5. Energy burn per tick = health x 75 / 24000. Water burns twice as fast. So time to "hungry" is 480 ticks per health point (Velociraptor 14 min, T. rex 32 min). [v] `gyovxqy/gjtxsudo.java:40-148`. Target: B17.
3. **The threshold ladder is 0.8 / 0.75 / 0.7 / 0.3 / 0.2.** Hungry below 0.8 starts eating and hunting. Below 0.3 the animal cannot sleep. Below 0.2 (food or water) it takes 5 percent of max health every 40 ticks. [v] `gyovxqy/gjtxsudo.java:82-148`. Target: B17.
4. **Food is a five-tier energy economy driven by tags.** Free plants give 300, farmed 750, effort 1500, premium 3000, bale 13500. Every meal heals the animal to full health. [v] `nkjvgug/gjtxsudo.java:11-77`, `ztiurxyn/ummp.java:42-57`. Target: B17, B20.
5. **Search cost is controlled by three tools: exponential backoff (20 up to 600 ticks), a 64-slot failed-target blacklist (600 ticks each) and dirty-chunk wake-ups.** A shell search with a chunk-section palette pre-filter and a test budget of 6 finds food. [v] `ztiurxyn/cvrd.java`, `nkjvgug/kdhoqn.java`, `nkjvgug/jgdp.java`, `nkjvgug/dnukfuguy.java`. Target: B17, B25.
6. **Hunting is gated, not planned.** A hungry (or aggressive-config) animal scans with a 10 percent chance per goal tick. It picks the nearest valid target within follow range, tests at most 6 for reach, and only attacks things with max health at most 0.9 of its own. There is no diet filter on targets (quirk, section 19). [v] `ztiurxyn/omogwgxr.java:12-168`. Target: B17, B19.
7. **Detection is sight only.** Follow range is 64 blocks in all 129 species files, and the target test uses vanilla combat conditions. I found no sound or scent sensor. Hurt is the only wake-up from sleep. [v] species JSON; `ztiurxyn/omogwgxr.java`. Target: B19.
8. **The herd goal is minimal.** It keeps up to N nearest same-species animals within 16 blocks, steps away when one is closer than 3 blocks, and walks to the centroid when farther than 8. N is a config value (default 4) that overrides the species value (1 to 50). There is no leader. [v] `ztiurxyn/kdhoqn.java` (javap). Nublar's B26 herd design is far ahead of this. Target: B26 (drop).
9. **Large-body path finding is cheap by design.** The search budget is a flat 200 nodes. A failed search blocks a second search in the same tick. A column cache replaces the per-block footprint test. Step height is `max(1, ceil(height/2)/2)`. Partial blocks in the step zone cancel step-up. A 60-tick progress window triggers an unstick move. [v] `incf/aeygzxj.java`, `incf/dnukfuguy.java`, `ctygd/omogwgxr.java:453,994-1051`, `ztiurxyn/ysulhvcy.java`. Target: B26, B25, B15.
10. **The part spatial index is a world-keyed map of 16-block cells with a safe fallback.** It wraps the three `Level` entity queries through one mixin. It rebuilds when the part count changes, falls back to the full scan when any foreign part exists or the query spans more than 1024 cells, and has a dev verify mode. [v] `ctygd/kdhoqn.java` (javap), `mixin/LevelPartScanMixin.java`. Target: B27, B25.
11. **Animation is a server-owned state: one name, one start tick, both synced.** Events (sounds, spit release) fire at set ticks on server or client. An exclusive animation cannot be replaced by a non-exclusive one. Hitbox sets for eat, drink and rest blend by animation time. [v] `octtcpmd/dnukfuguy.java`, `ctygd/omogwgxr.java:1409-1469,1776-1785`. Target: B27, B19.
12. **Six species data fields have no reader.** `attack_bias`, `flock_speed`, `random_flock`, `jump_height`, `attack_speed` and `defend_offspring` parse but nothing reads them. I searched all non-excluded source. [v] section 19. Do not trust the JSON as a behaviour spec.

---

## 3. Architecture

**In game.** An animal picks one job at a time: flee a shock, eat, drink, sleep, mate, hunt, follow its owner or wander. It plays a named animation for each job. It never plans ahead more than one target.

**Mechanism.**
- One abstract creature class extends the vanilla pathfinder mob. [v] `ctygd/omogwgxr.java:80`. A land subclass registers goals and the ground navigation. An aquatic subclass adds water goals, 300 air and its own swim physics. [v] `ctygd/gjtxsudo.java:20-23,37-72`, `ctygd/kijk.java:19-95`.
- Goals are vanilla `Goal` objects in the vanilla goal selector and target selector. I found no brain, no memory module and no sensor class. [v] grep over all non-excluded source.
- The registration hook calls one overridable method. The land class adds float, leave-water, unstick and wander there. The aquatic class replaces it with seek-water, water unstick and swim wander. [v] `ctygd/gjtxsudo.java:37-72`, `ctygd/kijk.java:42-46`.
- Five state components hang off the creature: metabolism, reproduction, ownership, genetics, inventory. Each has a name string and save/load methods. [v] `gyovxqy/*.java`.
- 16 synced fields: sex, age stamp, age-frozen flag, asleep, carcass, death cause, genetic scale, four colour genes, one eye flag, animation name, animation start tick, owner id, order. [v] `ctygd/omogwgxr.java:81-93,228-247`.
- Species behaviour comes from one data record read through the entity type id. A missing record uses fallback values and logs one error per species. [v] `ysjrlvxg/dbgouk.java:25-652`, `core/kodkieb.java:20-43`.
- The creature caches the record and refreshes attributes and dimensions only when a synced field changes or every 40 ticks. It writes an attribute only if the value differs. [v] `ctygd/omogwgxr.java:220-226,433-477,1595-1597`.
- Entity category: a custom `prehistoric` mob category (cap 10, despawn distance 128) for land animals and the vanilla water-creature category for marine ones. Natural spawn is off by default, Overworld only, and group size is `clamp(herd/2, 1, 6)`. [v] `core/JCMobCategories.java:7`, `axvg/onzkovhp.java`, `axvg/fgwme.java:39-41`, `dnukfuguy.java:17`.
- Goal evaluation frequency is vanilla's. A mob runs full goal start tests on every second tick, staggered by entity id, and ticks running goals on the other tick `[inf]`. Two goal bases ask for a tick every tick (unstick and seek-water). [v] `ztiurxyn/ysulhvcy.java:99`, `ztiurxyn/dnukfuguy.java`.

**Platform overlap.** Nublar uses a SmartBrainLib brain with memory modules and sensors (earlier surveys). Both reach the same behaviour breadth. JC3 proves that backoff timers and event wake-ups give good cost without a brain `[inf]`.

**Verdict.** Adapt the ideas (gates, backoff, blacklist, dirty wake). Do not adopt the vanilla-goal architecture.

---

## 4. Species data that drives behaviour

**In game.** A species file sets temperament, diet, sleep schedule and a few special moves. Most social and breeding numbers do nothing yet.

**Mechanism.** Values come from the 129 JSON files and the codec in `ysjrlvxg/dbgouk.java`.

| Field | Values in the 129 files | Read by | Status |
|---|---|---|---|
| `diet` | carnivore 84, herbivore 45 (insectivore exists in code, 0 uses) | food filter, tempt, feeders | [v] used |
| `temperament` | aggressive 29, neutral 65, passive 5, scared 30 | hunt gate, hurt-by, panic | [v] used |
| `sleep_time` | diurnal 113, no_sleep 10, nocturnal 3, crepuscular 3 | sleep gate | [v] used |
| `marine`, `movement_type` | marine 28; near_surface 26, deep_water 2; filter_feeder 3 | class choice, wander depth, hunt filter | [v] used |
| `adult_health` | 8 to 203, median 43 | needs pool and burn, max health, hunt ratio | [v] used |
| `baby_/adult_ speed, strength, health` | per species | attribute lerp by growth | [v] used |
| `follow_range` | 64 in all 129 | target range, path range | [v] used |
| `attack_speed` | 0.5 to 3.0, missing in 24 | nothing found | [v] unread |
| `max_herd_size` | 1 to 50 | herd goal, breeding cap, spawn group, but config default 4 overrides | [v] used, overridden |
| `can_climb` | true in 10 species | fence ladder, path fallback | [v] used |
| `jump_height`, `attack_bias`, `random_flock`, `flock_speed` | various | nothing found | [v] unread |
| `imprintable` | true 71, false 38, missing 20 (default false) | taming gate | [v] used |
| `defend_owner` | true 32, default false | defend-owner goal | [v] used |
| `birth_type` | egg 94, live birth 35 (28 marine) | laying vs birth animation | [v] used |
| `breed_cooldown` | 14,400 to 152,400 ticks, median 57,600 | breed gate | [v] used |
| `breed_around_offspring` | bool | mating gate | [v] used |
| `defend_offspring` | bool | nothing found | [v] unread |
| `min_clutch`, `max_clutch` | 1 to 18; validator rejects min above max | clutch roll | [v] used |
| `incubation_time` | 700 to 11,200 ticks, median 3,800 | egg data (set by egg creation) | [v] data |
| `abilities` | pounce 10, bite_effect 7, venom_spit 1, tree_climb 1, shoulder_sit 1 | special goals | [v] used |
| `growth.maximum_age` | 4.75 to 75 days, median 11.25 | stage gates | [v] used |

Stage gates from the age fraction: infant up to 0.25, juvenile up to 0.5, adolescent up to 0.75, adult above. [v] `ctygd/kodkieb.java:20-27`.

**Platform overlap.** Nublar's B00 already plans one `SpeciesData` file per species. The reader-less fields show a risk: the data file promises behaviour that no code delivers.

**Verdict.** Adopt the rule "every behaviour key has a reader and a test". Do not copy the unread keys.

---

## 5. Goal lists and priorities

Lower number = higher priority. Speed is the speed modifier the goal passes to navigation. `Perf` uses the key at the top.

### 5.1 Goal selector, land creature (`ctygd/gjtxsudo.java:37-72`)

| Pri | Goal (inferred) | Path | Speed | Gate | Throttle | Perf |
|---|---|---|---|---|---|---|
| 0 | float, but not under a suffocating block | `ztiurxyn/bugratnc.java` | - | in water | vanilla | LOW |
| 0 | leave deep water | `ztiurxyn/hmocea.java:13-105` | 1.2 | water deeper than jump threshold, land animal | search every 20 ticks, 12 wide, 4 tall, 6 path tests, 600-tick cap | LOW |
| 0 | flee electric shock | `ztiurxyn/kodkieb.java:14-136` | 1.4 | shocked within the last 2 ticks | 20 random tries, 6 path tests | LOW |
| 1 | unstick (land) | `ztiurxyn/kijk.java`, base `ysulhvcy.java` | 1.0 | no progress for 60 ticks | window test every 2 ticks | LOW, MED burst |
| 1 | panic | `ztiurxyn/dbgouk.java:5-31` | 1.5 | temperament scared or passive | vanilla | LOW |
| 1 | fall glide | `ztiurxyn/jgdp.java:9-51` | - | species has tree_climb, off ground over 4 ticks | every tick while falling | LOW |
| 1 | go to nest | `ztiurxyn/rqkeevhve.java:9-71` | 1.0 | delivering, or nest set and over 12 blocks away | test every 40 ticks | LOW |
| 2 | sleep | `ztiurxyn/qkek.java:14-163` | 1.0 | section 10 | threat scan cached 20 ticks | LOW |
| 2 | sit (order) | `ztiurxyn/yknu.java:8-35` | - | order is sit | trivial | LOW |
| 3 | tempt (infants) | `ztiurxyn/wtii.java:5-24` | 0.6 | infant, no target | vanilla tempt | LOW |
| 3 | drink | `ztiurxyn/gjtxsudo.java:11-165` | 1.0 | water below 0.7, not marine | backoff 40 to 600 | MED |
| 3 | eat | `ztiurxyn/ummp.java:7-58` | 1.0 | energy below 0.8 | backoff 20 to 600 | MED |
| 3 | tree climb | `ztiurxyn/wkjvl.java:18-372` | 1.0 | ability present | 40 ticks, or 1200 after a climb | MED |
| 4 | venom spit | `ztiurxyn/incf.java:11-100` | 1.0 | ability present, has target | path every 10 ticks | LOW |
| 4 | pounce | `ztiurxyn/wgpfjieca.java:19-220` | - | ability, target 5 to 6 blocks, cooldown over | per tick when a target exists | LOW |
| 4 | melee | `ztiurxyn/vtclb.java:6-51` | 1.2 | no spit ability, pounce not ready | vanilla | LOW |
| 5 | mate | `ztiurxyn/fgwme.java:11-248` | 1.0 | section 11 | 40 to 79 ticks, then 1 in 20 | MED |
| 5 | herd | `ztiurxyn/kdhoqn.java:10-51` | 0.9 | herd size above 1 | 40 to 79 ticks | LOW |
| 5 | follow owner | `ztiurxyn/onzkovhp.java:10-81` | 1.0 | order is follow | repath every 10 ticks | LOW |
| 6 | wander | `ztiurxyn/dfdtwro.java:10-34` | 0.8 | idle | interval 120; 10 wide, 7 tall | LOW |
| 7 | look at player (10 blocks), random look | vanilla | - | idle | vanilla | LOW |
| 8 | five idle emotes | `ztiurxyn/rtwzimpls.java:6-49` | - | no animation, no target | 300 to 1800 ticks | LOW |

Aquatic creature differences: no float, leave-water, unstick-land or wander. It adds seek-water (0), water unstick (1) and swim wander (6). [v] `ctygd/kijk.java:42-46`.

### 5.2 Target selector

| Pri | Goal | Path | What it does |
|---|---|---|---|
| 1 | hurt-by | `ztiurxyn/prulnm.java:7-38` | Retaliate. Not for scared or passive. Alerts same-species animals that are awake. |
| 1 | pack assist | `ztiurxyn/lyhmwcjw.java:14-120` | An adult copies the target of a nearby young same-species animal. |
| 1 | defend owner | `ztiurxyn/aeygzxj.java:11-78` | Copies the owner's last attacker or last victim when the order is follow. |
| 2 | hunt | `ztiurxyn/omogwgxr.java:12-168` | Section 8. |

**Platform overlap.** Vanilla goals with flags. Nublar's brain has its own activity and priority model `[inf]`.

**Verdict.** The ladder (float, shock, unstick, panic, sleep, needs, attack, social, wander, idle) is a good order to check B17 against. Adapt.

---

## 6. Needs: hunger and thirst only

**In game.** An animal gets hungry and thirsty on a long clock. Big animals last longer. A hungry animal eats or hunts. A starving animal loses health fast and cannot sleep.

**Mechanism.** [v] `gyovxqy/gjtxsudo.java:10-175`.
1. Pool size (both energy and water): `max(1, floor(adult_health^2 x 7.5))`. Examples: Velociraptor 9,187; T. rex 48,000; Brachiosaurus 168,750; Compsognathus 750.
2. Energy burn per tick: `adult_health x 75 / 24000 x creatureFoodRequirement` (config 0 to 1, default 1). Water burn per tick: twice the unscaled energy burn. Water is not scaled by the food config.
3. The creature burns the same amount as a baby and as an adult, because the formula reads the species adult health.
4. A marine animal refills water to full every tick while in water. A land animal drinks.
5. Both pools start full. Both save as `Energy` and `Water`.
6. Starvation: every 40 ticks, if energy is below 0.2 or water is below 0.2, the animal takes 5 percent of its max health as starve damage. From 0.2 to death takes about 800 ticks (40 s) if nothing is eaten. Death cause is thirst if water is low, else starvation. [v] `gyovxqy/gjtxsudo.java:121-150`, `ctygd/aeygzxj.java:75-77`.
7. Master switch: config `creatureMetabolism` (default on). Off, no test ever reports hungry.

**Threshold ladder** [v] `gyovxqy/gjtxsudo.java:82-119`:

| Test (energy unless noted) | Value | Used by |
|---|---|---|
| hungry | below 0.8 | eat goal, hunt gate, plant feeders |
| scavenge | below 0.75 | carcass source |
| feeder (meat) | below 0.7 | carnivore feeder source |
| thirsty (water) | below 0.7 | drink goal |
| desperate (either) | below 0.3 | sleep blocked, leaf path malus, leaf breaking |
| starving (energy) | below 0.2 | starve damage, eats owner, urgent eat |
| dehydrated (water) | below 0.2 | starve damage, urgent drink |
| can eat an amount | amount at most the room left, or energy below 0.6 | item and plant sources |

**Time table** (ticks; 1,200 ticks = 1 min; computed from the formulas):

| Species (adult health) | Hungry (0.8) | Starving (0.2) | Thirsty (0.7) | Dehydrated (0.2) |
|---|---|---|---|---|
| Compsognathus (10) | 4,800 (4 min) | 19,200 (16) | 3,600 (3) | 9,600 (8) |
| Gallimimus (30) | 14,400 (12) | 57,600 (48) | 10,800 (9) | 28,800 (24) |
| Velociraptor (35) | 16,800 (14) | 67,200 (56) | 12,600 (10.5) | 33,600 (28) |
| Triceratops (70) | 33,600 (28) | 134,400 (112) | 25,200 (21) | 67,200 (56) |
| T. rex (80) | 38,400 (32) | 153,600 (128) | 28,800 (24) | 76,800 (64) |
| Brachiosaurus (150) | 72,000 (60) | 288,000 (240) | 54,000 (45) | 144,000 (120) |

Rule: time to hungry = 480 x health ticks; time to starving = 1,920 x health; thirsty = 360 x health; dehydrated = 960 x health.

**What JC3 does not have.** No stamina, tiredness, stress, comfort, loneliness or fear meter. Sleep is a clock schedule (section 10). Fear is a flag set by the panic goal. Mood is only the set of discrete flags (asleep, carcass, order, panic, unstuck, pregnant). [v] grep for stress, mood, territory and leader found nothing outside unrelated text.

**Platform overlap.** None. All data is saved in the creature.

**Verdict.** Adapt the burn model and the ladder for B17. Keep the lead's starvation rule (1 HP per 80 ticks at zero) and the 75 / 25 percent thresholds unless the lead chooses otherwise. See open question Q1.

---

## 7. Foraging and drinking

**In game.** A hungry animal walks to the nearest food and eats one bite per visit, with an eat animation of 25 to 166 ticks. A thirsty animal walks to a shore, turns to face the water and drinks to full. Food on the ground, a carcass or a feeder counts before plants.

**Mechanism: sources.** [v] `nkjvgug/dbgouk.java:11-60`, `nkjvgug/kodkieb.java:20-185`, `Jurassicraft.java:27`.
- Eat sources are sorted by priority number: item entity 10, carcass 20, feeder 30, plant block 40, filter-feed block 100. The first source that finds a spot wins. Search radius is 16 blocks.
- Item source: item entities in a 16 x 8 x 16 box, nearest first, at most 6 untested, must match the animal's food type and fit the energy rule. Eaten from within 4 blocks, one item per bite.
- Carcass source: meat eaters with energy below 0.75. Nearest carcass in the same box. The bite is a normal attack on the carcass (section 12). It returns no energy. The meat drops become item food.
- Plant source: needs `plant` in the diet. Shell search over block states in the tag `herbivore_food_candidates`, vertical limit 4. Eating breaks the block without a drop and gives the tier energy.
- Filter source: filter feeders eat a plankton or krill swarm block, 8 radius, vertical 2.
- Feeder source: section 7.3.
- Drink source (separate list): radius 32, vertical 8. A valid spot is a dry, solid-floor cell next to water. One drink sets water to full. [v] `nkjvgug/kodkieb.java` water class.
- Diet food types: carnivore eats meat, fish, egg and insect; herbivore eats plant; insectivore eats insect. Carnivores may also eat insects. [v] `ysjrlvxg/vtclb.java:20-34`.

**Mechanism: energy tiers.** [v] `nkjvgug/gjtxsudo.java:11-77`, tags under `data/jurassicraft/tags/*/feed/`.

| Tier | Energy | Example members |
|---|---|---|
| free | 300 | leaves, saplings, grass, ferns, mushrooms, kelp (53 block entries, 74 item entries) |
| farmed | 750 | carrots, potatoes, melon, pumpkin, apples, raw and cooked vanilla meats |
| effort | 1,500 | 22 block entries: small ferns and cycads |
| premium | 3,000 | tree ferns, cycads, hay block, dinosaur meat and steak |
| bale | 13,500 | four paleo bales |

Unlisted item = farmed (750). Unlisted block = free (300). A Velociraptor needs about 6 free blocks to go from 0.8 to full. A Brachiosaurus needs about 112. Each successful plant or feeder meal also heals the animal to full health. [v] `ztiurxyn/ummp.java:42-57`.

**Mechanism: the shared goal base.** [v] `ztiurxyn/cvrd.java:12-294`.
1. On start test, the goal finds a source. If none, it doubles its retry delay: base x 2^failures, capped at 600 ticks (eat base 20, drink base 40). The real wait is the delay plus a random amount up to the delay.
2. A failed search can retry early in three cases: the animal is urgent (energy or water below 0.2); the animal moved over 8 blocks from the failed spot (max 2 times); a player block change, placed block or dropped item happened within 32 blocks in the last 1,200 ticks. The dirty-chunk tracker holds at most 512 entries and prunes old ones. [v] `nkjvgug/jgdp.java:25-110`.
3. During the walk the goal re-targets if navigation ends early. After 600 ticks, or on repeated path failure (more than 2 re-paths), it gives up and blacklists the spot.
4. Arrival test: horizontal distance within `1.75 + width/2` (drink: `width/2 + max(1.75, 3 x width / 7)`), vertical difference at most body height + 1.
5. After the bite the goal holds still for the eat animation length, then ends. The next bite waits the 20-to-40-tick base delay.
6. Drink only: the animal turns up to 15 degrees per tick to face the water, slides toward the water edge in steps of `0.08 x max(1, width/2)` blocks, and drinks when the angle error is within 20 degrees. It may try up to 60 ticks.
7. The final approach uses a "tight" mode in the ground navigation: the last path node counts when the animal is within 0.35 blocks horizontally. [v] `incf/aeygzxj.java:47-109`.

**Mechanism: blacklist and prefilter.**
- The blacklist is a 64-slot ring per creature. Each entry lasts 600 ticks. A re-fail refreshes the entry. [v] `nkjvgug/kdhoqn.java:5-38`.
- The reach test (`nkjvgug/onzkovhp.java:17-204`): marine animals test water cells; fence climbers test only that no electrified fence lies on the straight line; all others ask the navigation for a path to a ring of cells around the target (radius `1.75 + width/2`) and require `canReach`. A fail adds the cell to the blacklist unless the navigation was only throttled.
- The shell search visits cells in growing shells. It checks a pre-built per-section "might contain" array before any cell. The array uses the chunk section palette test, so an air-only or food-free section costs nothing. It stops after 6 candidate path tests. [v] `nkjvgug/dnukfuguy.java`, `nkjvgug/vtclb.java`.

**7.3 Feeder.** [v] `dnukfuguy/ummp.java:26-289`, `ufpurxw/jgdp.java:12-88`, `ufpurxw/dbgouk.java:23-69`.
- One feeder block holds 18 slots: slots 0 to 8 take animal food (meat, fish, insect, egg, filter), slots 9 to 17 take plants.
- A feeder registry lists feeder positions per chunk. A query within 32 blocks scans all feeders of the dimension, filters by distance, and sorts. Cost is linear in feeder count `[inf]`. At most 6 are tested.
- An animal reserves a feeder (one animal at a time, 100-tick claim). The lid opens in 20 ticks, waits 20 ticks, and at 10 ticks before the end spits one matching item out of the front face with velocity 0.3. Then the feeder has a 20-tick cooldown. The animal picks the item up as an item entity within 2.5 blocks.
- The goal retries a feeder up to 100 times before giving up. Plant eaters use the feeder at energy below 0.8; meat eaters at energy below 0.7.

**Platform overlap.** Nublar B20 plans three feeder types and a feeder registry. Nublar plans to move diets to tags (B17). JC3's design shows both working together.

**Verdict.** Adopt: tag tiers, source registry by priority, backoff, blacklist, dirty wake, palette prefilter. Adapt: feeder claim and dispense. Drop: whole-dimension feeder scan (use a chunk-keyed index).

---

## 8. Hunting, prey choice, aggression to players, detection

**In game.** A hungry meat eater picks the nearest creature it can beat and chases it. It leaves players alone when they are creative, spectators or its owner. A passive or scared species never hunts. After a chase it rests 2.5 to 3.5 seconds before the next.

**Mechanism: start test.** [v] `ztiurxyn/omogwgxr.java:12-168`.
1. 90 percent of goal ticks stop here (random gate of 1 in 10).
2. Stop if asleep, carcass, order is sit, in post-hunt cooldown, or growth is 0.5 or below.
3. Stop if owned and not hungry (energy at or above 0.8).
4. Passive and scared never hunt. Neutral hunts only when hungry. If config `huntWhenHungry` is on (default), every species hunts only when hungry. Off, aggressive species hunt always.
5. Scan: living entities in a box of follow range (64) horizontally and half that vertically. Each must pass the combat targeting test and the selector (below). Sort by distance.
6. Walk the list. Skip blacklisted positions (free). For the first 6 others, run the reach test. Return the first that passes. Land and aquatic reach tests are in section 7.

**Target selector** (all must hold):
- Alive, not self.
- Not a creative or spectator player.
- Not the owner, unless the animal is starving (energy below 0.2).
- Not a creature of its own species; not a carcass.
- Land animals: the target must not be underwater. Marine animals: the target must be in water and in a water cell the predator's body fits. [v] `ztiurxyn/omogwgxr.java:134-168`.
- Target max health at most 0.9 x the hunter's current max health.

**Continue test.** A land hunter stops if it is swimming off the ground. Every 40 ticks it re-tests reach and stops if it fails (the target position is then blacklisted for 600 ticks). A marine hunter stops when the target leaves reachable water. On stop, a cooldown of 50 + random 20 ticks starts. [v] same file.

**Attack.** Vanilla melee at speed 1.2, "follow even if not seen" on. Bite effects from the species list apply after each hit (Coelophysis weakness 300 ticks amp 2 and nausea 440 ticks amp 1; Troodon weakness, nausea and poison). Species with a spit ability cannot melee. [v] `ztiurxyn/vtclb.java:6-51`, `ctygd/omogwgxr.java:724-726` (javap), species JSON. Attack animations last 12 to 39 ticks; the damage tick is the vanilla melee timer, not an animation event `[inf]`.

**Temperament matrix** [v]:

| Temperament | Hunts | Retaliates and alerts herd | Panics when hurt |
|---|---|---|---|
| aggressive | when hungry (always if config off) | yes | no |
| neutral | only when hungry | yes | no |
| passive | never | no | yes |
| scared | never | no | yes |

**Detection.**
- Sight only. Range is the follow-range attribute (64 in every file). The vanilla combat test adds line of sight and vanilla visibility scaling (sneaking, invisibility, head items) `[inf]`.
- No hearing, no scent, no noise event. The only "event" input is the target-change event of young animals (section 9). [v] grep.
- A hurt animal wakes. No other stimulus wakes a sleeper.

**Platform overlap.** Vanilla `TargetingConditions`. Nublar's B19 wants sight, sound and scent.

**Verdict.** Adapt the gate order, the 0.9 health rule, the 6-test budget, the cooldown and the matrix. Add a diet filter. JC3 gives no model for sound or scent detection.

---

## 9. Herds, packs, defence and fleeing

**In game.** Animals of one species drift together in small groups. When one is hurt, the group turns on the attacker. Adults come to the help of a fighting youngster. Scared herbivores run.

**Mechanism: herd goal.** [v] `ztiurxyn/kdhoqn.java:10-51` (start test from javap).
1. Every 40 to 79 ticks: skip if asleep, carcass, has a target, or shocked within the last 200 ticks.
2. Herd size must be above 1. Herd size is the config value (default 4) or, if config is 0, the species value. [v] `ctygd/jgdp.java`.
3. Take the nearest same-species animals within 16 blocks (alive, not carcass), up to the herd size.
4. If the nearest is closer than 3 blocks, walk 3 blocks directly away from it. Otherwise, if the animal is more than 8 blocks from the centroid, walk to the centroid. Speed 0.9.
5. There is no leader, no formation, no journey and no memory of earlier moves.

**Pack and alarm.**
- Hurt-by: the vanilla alert-others behaviour with an empty class list. It reaches same-species animals within follow range (64 blocks, 10 tall) `[inf]`. The override accepts only animals of the same species that are awake, not carcass, and not scared or passive. [v] `ztiurxyn/prulnm.java`.
- Pack assist: an adult (growth above 0.75, not asleep, not sit, no target) looks in a box of 8 x 3 x 8 for a same-species animal with growth at most 0.5 and a target. It copies that target (unless it is a creative player). Retry delay is `10 << min(failures, 8)`, capped at 200 ticks. A "young animal gained a target" event marks the chunk for 400 ticks, and an adult then rechecks every 10 ticks if a mark is newer than its last check. [v] `ztiurxyn/lyhmwcjw.java:14-120`, `ztiurxyn/stta.java:22-88`. The `defend_offspring` key is not read.
- Defend owner: section 17.
- Flee: panic at speed 1.5 for scared and passive species, and a panic flag that blocks pounce. No flee-from-player goal and no flee-from-predator goal exist. [v] `ztiurxyn/dbgouk.java`, `ctygd/omogwgxr.java:637-639`.
- Herd size also caps breeding (section 11).
- Claimed "moving with the herd" status: any same-species animal alive and not carcass within 16 blocks while the animal moves faster than 0.03 blocks per tick. It is only a PaleoPad label. [v] `ctygd/omogwgxr.java:641-651`.

**Platform overlap.** Nublar's B26 and the herd redesign on `kash's-slop` (trail following, context steering, journeys, wedge travel) replace the herd goal entirely. B18 owns leaders and calls. JC3 has neither.

**Verdict.** Drop the herd goal. Adapt the alert-others and young-target wake-up for B18. Keep 3 / 8 / 16 as a sanity baseline for B26 spacing.

---

## 10. Sleep

**In game.** Diurnal animals sleep at night in a covered spot. A hurt animal wakes for 10 seconds. A hungry animal, or one with a bigger creature nearby, will not sleep.

**Mechanism.** [v] `ysjrlvxg/jgdp.java:6-58`, `ctygd/omogwgxr.java:522-583`, `ztiurxyn/qkek.java:14-163`.

Schedule (clock time in a 24,000-tick day):

| Type | Active window (cannot sleep) | Sleep window |
|---|---|---|
| diurnal | 0 to 16000 | 16000 to 24000 |
| nocturnal | 12000 to 24000 | 0 to 12000 |
| crepuscular | 6480 to 22480 | 22480 to 6480 |
| no_sleep | always | never |

Steps:
1. Can-sleep test: not desperate (energy and water at 0.3 or above); wake lockout is 0; not in the active window; no threat nearby.
2. Threat test, cached for 20 ticks: any living mob in a box of 10 x 5 x 10 that is awake, is targeting this animal, or has higher max health and is another creature or a monster.
3. Hurt: sets a 200-tick lockout unless a forced sleep is running.
4. Start: a land animal in water looks for a shore spot (rings up to radius 31). Other land animals try 32 random spots within 8 blocks that cannot see the sky and have no fluid below. A marine animal sleeps in place. Walk speed 1.0, 400 ticks maximum, then sleep where it stands.
5. Sleeping freezes rotation, zeroes horizontal motion, and uses the "sleeping" animation and `sleep_hitboxes`.
6. Continue while the can-sleep test holds, or a forced sleep is running.
7. Dart forced sleep: 50 + dart value + random 50 ticks (tranquilizer value 600). Damage does not wake it.

**Platform overlap.** Nublar's B17 has a sleep need. JC3's version is a schedule plus gates, not a need.

**Verdict.** Adapt the gates (threat, desperate, lockout, shelter search). Check the crepuscular window: it is active almost all day, which looks wrong (quirk, section 19).

---

## 11. Mating, pregnancy, laying, birth and growth gates

**In game.** An adult female with a healthy partner nearby mates, carries eggs for 8 minutes, then goes to a nest spot and lays them (or gives live birth). Eggs hatch in 4 to 5 minutes by default.

**Mechanism.** [v] `ztiurxyn/fgwme.java:11-248`, `gyovxqy/kodkieb.java:16-258`.
1. Eligible (both partners): breed cooldown 0, not pregnant, not delivering, alive, not carcass, not asleep, adult, health full.
2. Start test every 40 to 79 ticks: eligible, no target, no own young nearby (within 16 blocks, growth at most 0.5, lineage lists this animal as parent) unless `breed_around_offspring`, the pack is not full (same species within 16 blocks below the herd size; config `packSizeLimitsBreeding`), then a 1 in 20 roll. Expected wait about 1,200 ticks.
3. Partner: nearest opposite-sex same-species eligible animal with no target within 16 blocks. For path-based animals at most 6 reach tests.
4. Approach up to 300 ticks (hearts every 10 ticks). Done when bodies are within 3 blocks. If either goal is interrupted, it logs a reason (debug string).
5. Mating: the mother rolls the whole clutch now (min to max eggs, one offspring record each). Pregnancy lasts 9,600 ticks (8 min). Both partners get the species breed cooldown.
6. At term: the animal plays "laying_egg" or "giving_birth", sets a nest spot for 6,000 ticks, and delivers half-way through the animation. Eggs appear as egg entities at the mother; live births spawn the young directly.
7. While delivering, the animal stands still. While a nest is set and it is over 12 blocks away, it walks back.
8. The egg entity default hatch time is 5,000 to 6,000 ticks. The species `incubation_time` is data for the creation step. [v] `aprffirmi/dnukfuguy.java:7-134`.
9. The Breeding Wand pairs a male and a female directly (second click pairs them). It does not test species `[inf]`. A paired couple ignore eligibility and walk to each other. [v] `bugratnc/gjtxsudo.java`.
10. Growth gates for behaviour: hunt above 0.5; pack assist above 0.75; defend owner above 0.5; mating adult only; carry an inventory adult only; tempt and imprint infant only.

**Platform overlap.** Nublar's B18 plans nests and eggs; B11 covers hatching.

**Verdict.** Adapt the numbers and gates. The genetic part is in the sibling note on genetics.

---

## 12. Death, carcass and scavenging

**In game.** A dead animal falls into a carcass pose. Players and meat eaters hack at it for meat. An unclaimed wild carcass slowly rots away.

**Mechanism.** [v] `ctygd/omogwgxr.java:837-964,1610-1621`, `nkjvgug/kodkieb.java`.
1. On death (config `allowCarcass` on): record the death cause (20 causes, from damage type), set health to full, set the carcass flag, and start the "dying" animation (hold). Off, vanilla death.
2. Carcass hit points: `clamp(max(1, floor(sqrt(width x height)) x 2), 1, 10)`. Velociraptor 2, T. rex 6.
3. Each hit that has an attacker entity (and passes the 10-tick hurt gap) drops 1 to 2 (+ looting) meat items. Burning carcasses drop the burnt variant. When the hit points reach 0, the next hit drops full loot and removes the body. Void damage removes it at once.
4. A carcass is not pushable, not moved by knockback, and keeps its rotation. It uses `carcass_hitboxes`.
5. Wild unowned carcasses lose 1 hit point every 600 ticks and vanish at 0.
6. Meat eaters at energy below 0.75 walk to a carcass and attack it. Its drops become item food (priority 10).
7. Death causes include starvation, thirst, electrocution, run over, venom, euthanasia, fall, drowning and others. The PaleoPad shows the cause.

**Platform overlap.** Nublar B16 plans carcass and scavenging.

**Verdict.** Adopt. The hit-point formula and the bite-drop-eat loop are small and clear.

---

## 13. Special moves

**Pounce** (10 species; Velociraptor-style). [v] `ztiurxyn/wgpfjieca.java:19-220`, `ysjrlvxg/dbgouk.java:315-345`.
- Defaults: range 5 to 6 blocks, reach factor 6, lift 0.6, max extra lift 0.3, bite range 2, cooldown 100 ticks. Microraptor: range 3 to 5, lift 0.5, bite range 1.5.
- Start: target alive, on ground, panic off, cooldown over, distance in range, clear line from 1 block above the feet. Steps: crouch (look at target, "prepare_leap" animation 15 ticks, up to 60 ticks); spring (aim at the target position plus its recent motion times `width x reach factor`; vertical speed = lift + `clamp(0.1 x height difference, 0, max extra lift)`; flight time n = `max(1, 2v/0.08)` ticks; horizontal speed = `min(distance / ((1 - 0.91^n)/0.09), 1.5)` per tick); land (when falling, play "leap_land"); on touching ground or water, hit the target if its box touches the animal box inflated by the bite range; cooldown starts.
- Melee is suppressed while a pounce is possible. The 5 ornithomimid species have the key but are scared, so they never get a target.

**Venom spit** (Dilophosaurus). [v] `ztiurxyn/incf.java`, `ysulhvcy/dbgouk.java`.
- Interval 40 ticks, range 10, damage 4, blindness 600 ticks amplifier 3, projectile speed 1.5. The animal walks to within range, needs 20 straight ticks of line of sight, plays "spit" (40 ticks), and at tick 20 fires a projectile (aim raised by 0.2 x horizontal distance). It ignores creative targets. It skips targets that are blind and below 90 percent health. Venom ignores other animals of the shooter's species unless they are the target.

**Tree climb and glide** (Microraptor). [v] `ztiurxyn/wkjvl.java:18-372`, `ztiurxyn/jgdp.java`.
1. Pick a tree: 20 random cells within 7 blocks; the lowest log of a trunk reached by a heightmap probe (up to 6 blocks down); trunk at least 2 logs high; a free neighbour cell.
2. Walk there (speed 1.0). Within 2 blocks: "start_climbing" (15 ticks), then "climbing" (loop). The animal is moved straight up the trunk 0.3 blocks per tick, no gravity, to the top of the log or leaf column (max 14).
3. Choose a landing within 22 blocks (40 samples, at least half range away, leaves preferred). Launch, then glide: horizontal speed is `min(speed x 0.5, 0.25 x distance)`, descent per tick is clamped between 0.05 and 0.3, pitch follows the glide angle. It ends within 1.5 blocks, on ground, in water, or after 200 ticks.
4. Next attempt after 1,200 ticks (after a climb) or 40 ticks (after a miss).
5. Fall glide: any time it is off the ground for over 4 ticks, downward speed is capped at 0.05 per tick and fall damage is reset.

**Fence shock flee.** [v] `qkek/aeygzxj.java:45-266`, `ztiurxyn/kodkieb.java`.
- A powered fence segment hurts any living entity that is inside it, by a tier-defined damage, with a click sound every 10 ticks. For a creature it also sets a shock counter (max 2, lasts 2 ticks) and a "shocked recently" timer of 200 ticks.
- The flee goal runs on the shock counter. It chooses a free cell by up to 20 random tries (offsets up to 8 from a triangular spread), tests at most 6 for reach, and runs there at 1.4. If none works, it walks 4 down to 1 block directly away from the nearest fence within 2 blocks.
- The recent-shock timer blocks the herd goal for 200 ticks.

**Climbing fences.** [v] `qkek/aeygzxj.java:259-262`, `ctygd/omogwgxr.java:744-779`.
- The fence block counts as a ladder for species with `can_climb`, and for players. Such an animal that is airborne next to a fence sets its jump flag, so it climbs. The fence is never path-walkable, so path search cannot cross it.
- For climbers, if path search fails or is throttled, navigation sets a direct "beeline" target for up to 300 ticks (the move control pulls toward it). The animal then bumps the fence and climbs. [v] `incf/aeygzxj.java:22-109`.
- Climbers decide reach by a line test: a powered fence anywhere along the line from the animal to the target (checked in 0.5-block steps, from 1 below to 3 above) blocks the target. An unpowered fence does not block it.

**Platform overlap.** Nublar's B15 plans a fence parity baseline (shock rules, T. rex breaks unpowered fences). B19 plans a charge move.

**Verdict.** Adapt pounce maths and the fence flee. Defer spit, tree climb and shoulder sit (no Beta species). Ask the lead about climbers over fences (Q2).

---

## 14. Movement and path finding for large bodies

**In game.** Large animals use wide paths, do not step over partial blocks such as fences, walk through leaves when desperate, and free themselves when stuck.

**Mechanism: ground navigation.** [v] `incf/aeygzxj.java:16-165`.
1. Node budget: multiplier `min(1, 200 / (max(follow range, 16) x 16))` on the pathfinder. With follow range 64 the pathfinder gets about 200 visited nodes, not 1,024.
2. A new search in the same game tick as a failed search returns "throttled" (null) instead. A search that keeps the same target path is allowed. Water navigation uses a 40-tick pause after any failed search. [v] `dfdtwro/gjtxsudo.java:11-62`.
3. Climbers fall back to the beeline (section 13).
4. "Tight" final approach mode for eating and drinking (section 7).

**Node evaluator.** [v] `incf/dnukfuguy.java:16-101`.
- It replaces vanilla's footprint test. For each column of the footprint (width x depth) it computes the path types of the animal's height once and caches them by column start. The cache clears at the start and end of each search. Doors: closed wooden door passable only if the mob can open and pass doors; open door blocked if it cannot pass doors. Rails: unpassable unless the animal already stands on rail (keeps animals off tour tracks).
- A dev config compares every cached footprint against vanilla's answer and logs differences.

**Body and step rules.** [v] `ctygd/omogwgxr.java:453,994-1051`.
- Step height attribute = `max(1.0, ceil(height / 2) / 2)`. Dimensions: width and height from the species record scaled by growth and the genetic scale, clamped to 0.3 to 15.
- If a non-full, non-pathfindable partial block with a collision shape stands in the step zone of the move, step height becomes 0 for that move. This stops big animals stepping over fences and walls.
- The land move control only turns a near arrival (within 0.25 blocks) into a jump when the height difference is above step height. [v] `incf/vtclb.java`.

**Leaves.** [v] `ctygd/gjtxsudo.java:74-122`. Each tick, if config `creaturesBreakLeaves` and mob griefing are on, leaves overlapping the body box are destroyed (no drop). When horizontally blocked and (desperate or unstuck-flagged), leaves in the box inflated by 0.2 go too. Path malus for leaves is 8 when desperate, else -1 (blocked).

**Unstick.** [v] `ztiurxyn/ysulhvcy.java:11-139`, `ztiurxyn/kijk.java`.
1. Every goal tick (every 2 ticks) add 2 to a window counter and, if the animal is trying to move (path not done, or target farther than width + 2), add 2 to a "trying" counter.
2. If the animal moved 2 or more blocks, reset the window.
3. At 60 counted ticks: stuck if "trying" is at least a third of the window and the animal moved less than 2 blocks.
4. Stuck: look in 16 direction pairs, up to 8 steps each, for the longest run of cells where the body box has no collision and solid ground is within 2 below (water version: body box fits in water). Needs a run above 1. Then for 60 ticks, set the move control straight to that cell at speed 1.0 and set the unstuck flag (which enables leaf breaking).

**Swimming** (aquatic class). [v] `ctygd/kijk.java`, `dfdtwro/aeygzxj.java:9-90`, `ztiurxyn/dnukfuguy.java`, `ztiurxyn/cnxjsqwpw.java`.
- 300 air; out of water it loses 1 per tick, and at -20 it takes 2 damage and resets to 0 (every 20 ticks).
- Swim move: yaw turn limit 30 degrees per tick; speed eases 12.5 percent toward target; a sine bob (0.05 blocks) plus vertical aim; 0.9 drag; sinks 0.005 per tick when idle with no target; near the surface a small downward push keeps it in the water. The head turns up to 180 degrees.
- Stranded on land: the seek-water goal looks within 16 blocks (6 vertical, 64 candidate tests) for a water cell that fits the body, crawls at `0.15 x clamp(width/1.2, 0.6, 1) / speed`, and gives up after 200 ticks.
- Wander picks a swimmable cell within 10 x 6, then shifts up or down 6 cells (near-surface up, deep-water down) while water continues. Speed 0.3. Pause 40 to 80 ticks.

**Flying.** No species file sets `flying`, and I found no flying move control or flying navigation in the creature code. The only airborne move is the Microraptor glide. [v]

**Platform overlap.** Vanilla has a node-evaluator cache for the footprint of large mobs `[inf]`. B26 plans body-fit path checks (`BodyFit`, herd work) and a water cost (B16).

**Verdict.** Adopt: node budget, one failing search per tick, partial-block step cancel, step height formula, progress-window unstick. Adapt: column cache (with a verify flag), tight final approach, leaf rules (only if leaves are meant to be breakable).

---

## 15. Multipart bodies and the part index

**In game.** A dinosaur has a body plus parts (head, neck, body, tail sections). Arrows and hits land on the parts. A big park stays fast.

**Mechanism.**
- The creature holds an array of part entities from the species hitbox list (`hitboxes`, plus `carcass_`, `sleep_`, `drinking_`, `eating_`, `resting_hitboxes`). Part ids are the creature id plus index plus 1. [v] `ctygd/omogwgxr.java:1623-1652`.
- Each tick (if the animal has parts) it picks a hitbox list from the current animation: carcass, sleeping, drinking, eating or resting, else the main list. It blends between lists by animation time. For "drinking" with ticks [45, 70, 90]: ramp in over 45 ticks, hold to 70, ramp out to 90. For "eating" [22, 32, 52]. For sleep, rest and carcass: one number. [v] `ctygd/omogwgxr.java:1654-1801`, species JSON `pose_blend_ticks`.
- Parts copy name, offset, size and optional head pivot. The head part follows the look direction (yaw clamp 75 degrees, pitch clamp 60 degrees) `[inf from javap constants]`. Parts also scale with the creature.
- Part damage goes to the parent: `hurtServer` on the part calls the parent, awards the damage stat to a player attacker and spawns damage particles when above 2. Push goes to the parent as knockback. Interact forwards to the parent first. Parts are never saved. [v] `ctygd/vtclb.java:19-106`.
- Pushing: with parts, the creature's own push step checks each part box; a touching entity gets a 0.05 push away from that part. [v] `ctygd/omogwgxr.java:1057-1115`.
- Part index. [v] `ctygd/kdhoqn.java` (javap), `mixin/LevelPartScanMixin.java`.
  1. A mixin wraps the call to the level's part collection in three `Level` entity queries (get entities with a predicate, typed get entities, has entities). The wrapper calls the index.
  2. Index: one map per level of section cell (16 x 16 x 16) to a list of parts.
  3. On each query: if the config is off, return the full collection. If the number of JC parts plus foreign parts differs from the collection size, rebuild. If any foreign part exists (for example an Ender Dragon), return the full collection.
  4. The query box is widened by 4 blocks on X and Z and 12 blocks below. If it spans more than 1024 cells, return null (full scan).
  5. A part moves cells: it is removed from the old list and added to the new list when its position is set.
  6. A dev mode compares the answer with the full scan and logs misses. A system property turns the index into a deliberately broken one for tests.
- A second mixin presizes the visited-block set in the block-sweep method (size = box volume, capped at 8,192). It avoids rehash churn for big boxes. [v] `mixin/BlockIntersectVisitSetSizeMixin.java`.

**Platform overlap.** NeoForge `PartEntity` and the level's part collection are the base. B27 plans parts and a spatial structure (hitbox design doc).

**Verdict.** Adopt the index design for B27 and B25, including the foreign-part fallback and the verify mode. Adapt the animation-time blend for pose hitboxes.

---

## 16. State machine, animation sync and sound keys

**In game.** Every action has a named animation. Footsteps, roars and bites sound at the right frame. The client always agrees with the server on what the animal is doing.

**Mechanism.** [v] `octtcpmd/dnukfuguy.java:7-166`, `octtcpmd/ummp.class` (javap), `octtcpmd/kodkieb.java`, `ctygd/omogwgxr.java:1409-1564`.
- An animation definition: name, length in ticks, loop flag, hold flag (stays on the last frame until cleared), exclusive flag, and a list of events (name, tick, side = server or client). Events must fall within the length and are sorted. Five builder kinds: loop, one-shot, hold, loop-exclusive, hold-exclusive.
- Each species has an animation set (129 sets) built in code: walking, running, swimming, roaring, calling, speak, head_cocking, looking_left/right, eating, drinking, attacking, injured, laying_egg or giving_birth, pounce steps, climb steps, sleeping, resting, dying. Example Velociraptor timings: eating 52, drinking 90, attacking 22, leap 12, sleeping 10 (hold). T. rex: eating 166, roaring 125, attacking 25.
- The state holds the current animation and start tick, and the previous one for a 4-tick cross-fade. A request fails if the animation is unknown for the species, or the current one is exclusive and the new one is not. "Start if not playing" and "restart" are two calls. Clearing keeps the old one for the fade.
- The server writes two synced fields (name, start tick). The client rebuilds the state from them. The server and client both tick events: server events (for example spit release, tick 20 of the spit) on the server, client events (sounds) on the client. Loops fire events once per loop. [v]
- Sound keys: speak_sound, call_sound, roar_sound, attack_sound, spit_sound, footstep, drink_sound. Each maps to a slot in the species `sounds` record. Hearing range uses 16 or 24 blocks times max(volume, 1). Voice volume lerps 0.2 to 1.0 with growth, footsteps 0.1 to 1.0, pitch 2.5 down to 1.0 plus small jitter.
- Client footsteps: choose "walking" above 0.05 blocks per tick and "running" above 0.2.
- Ambient: interval 200 ticks, plays the "speak" animation if idle. Breathing: every 62 ticks at the creature's voice volume, not when asleep or carcass.
- Idle emotes (goal at priority 8): roaring and calling 600 to 1,800 ticks, head cocking 300 to 900, looking left or right 400 to 1,200.
- Server logic sets state from flags each tick: asleep -> "sleeping"; sit order and on ground -> "resting"; carcass or dying -> "dying"; clears sleeping/resting when the flag ends.

**Platform overlap.** Nublar's B27 plan has "a synced pose state: animation id, start tick, loop flag. The server owns it." JC3 is the same design, with exclusivity, hold and events added.

**Verdict.** Adopt as the base for B27. Add events and exclusivity.

---

## 17. Ownership, imprinting, commands, inventory, darts

**In game.** A player feeds a baby and it may imprint (10 percent per feeding). The owner opens a screen to say wander, follow or sit. Adults can carry a chest. Microraptors sit on shoulders. Darts put animals to sleep or put them down.

**Mechanism.** [v] `ctygd/omogwgxr.java:1117-1273`, `gyovxqy/ummp.java:12-84`, `znpnvzxz/onzkovhp.java:13-44`.
- Hand-feed: an infant, a food item the species accepts, 600-tick cooldown per animal. Energy rises by the item's tier energy, health to full, eating animation, sounds. If the animal is unowned, imprintable, and a 10 percent roll succeeds, the player becomes the owner (hearts, message). Owners are stored by id and name. No other way to own was found.
- Owner empty-hand use opens the order screen. A non-owner sees "not yours". The set-order packet checks the sender owns the animal and is within 64 blocks.
- Orders: wander, follow, sit. Follow: starts when the owner is farther than `width x 40` squared blocks (Velociraptor about 6 blocks, T. rex about 12), repaths every 10 ticks, stops within 2 blocks. No teleport. Sit: stops all movement and hunting and plays "resting".
- Defend owner (species key): while following, with growth above 0.5 and no target, copy the owner's last attacker or last victim (not other creatures that are carcasses, not the owner).
- Owned animals hunt only when hungry; they never despawn; an owned animal can eat its owner when starving.
- Inventory: owner, shift + empty hand, adult only, species `inventory_size` (9 to 54 in steps of 9) as a chest menu; valid within 64 blocks. Contents drop on death or carcass removal.
- Shoulder sit (Microraptor): owner shift + empty hand while the animal is sitting. Up to `max_riders` (default 2); riders alternate left and right shoulder (90 degrees from body yaw, 0.35 offset, height 1.38, sneaking 1.2). Drops off when the player glides with elytra.
- Darts: a plain dart does 1 damage; tranquilizer puts the animal to forced sleep (650 to 699 ticks); euthanasia gives poison and kills it after 200 ticks (cause "euthanasia"); potion darts pass their effects. [v] `bugratnc/kodkieb.java:18-83`, `ctygd/omogwgxr.java:671-701`.
- PaleoPad: use on an animal to open a panel with energy, water, owner, can-sleep, moving-with-herd, panic, pregnancy, pack count and size. Status labels: hungry, thirsty, ready to sleep, fleeing, moving with the herd, pregnant, poisoned, badly hurt, drowning, wild or imprinted. [v] `bugratnc/fgwme.java`, lang file.

**Platform overlap.** Nublar's B19 plans taming with follow, stay and defend commands.

**Verdict.** Adapt. The imprint rule and the server checks are small. Adopt the PaleoPad idea as a debug and player tool.

---

## 18. Tick cost and throttle summary

| Item | Cost | Throttle | Perf |
|---|---|---|---|
| Creature tick (server): metabolism, reproduction, timers, state-to-animation | a few integer and double steps | every tick | LOW |
| Attribute and dimension refresh | writes only on difference | on synced-data change or every 40 ticks | LOW |
| Leaf scan of the body box | block reads, about 80 for a T. rex | every tick when enabled | MED |
| Step-zone partial block check | thin slab per axis | each move call | LOW |
| Fence-ladder scan (climbers) | box inflated 0.1 | once per tick | LOW |
| Part update (multipart) | per part trig and box set | every tick | LOW |
| Hunt scan | box 128 x 64 x 128, then up to 6 path tests | 1 in 10 per goal tick, hungry only | MED |
| Hurt-by alert | one box scan of 128 x 20 x 128 | on hurt events only | LOW |
| Eat and drink search | shell search with palette pre-filter, 6 tests | backoff 20 to 600, dirty wake | MED |
| Feeder lookup | all feeders of the dimension | per search | LOW to MED |
| Mate search | box 16, 6 tests | 40 to 79 ticks, then 1 in 20 | LOW |
| Sleep threat scan | box 10 x 5 x 10 | cached 20 ticks | LOW |
| Pack assist | box 8 x 3 x 8 | backoff 10 to 200, event wake | LOW |
| Herd | box 16, sort | 40 to 79 ticks | LOW |
| Unstick | counter; burst scan 16 x 8 collision tests when stuck | every 2 ticks | LOW, MED burst |
| Path search | 200 nodes; one failing search per tick | per goal | MED |
| Animation events | list compare per tick | per tick | LOW |
| Breathing sound | one sound | every 62 ticks | LOW |
| Part index query | cell lookup; rebuild on count change | per entity query | LOW |

---

## 19. Quirks, gaps and bugs found

1. **No diet filter in the hunt target test.** A hungry neutral herbivore (Triceratops, Stegosaurus, Ankylosaurus, Apatosaurus, Brachiosaurus and 12 others) may also hunt. The eat goal (priority 3) beats the melee goal (priority 4) while food is in reach, so it mostly grazes. When no food is reachable, it can attack players and small animals `[inf]`.
2. **Spitter dead end.** Dilophosaurus cannot melee. The spit goal ignores blind targets below 90 percent health. Such a target is left alone `[inf]`.
3. **Six unread keys** (section 4).
4. **Pounce on scared herbivores** (5 ornithomimids) is dead data.
5. **Crepuscular window** is active from 6480 to 22480, so it sleeps only from 22480 to 6480. That is the reverse of dawn-and-dusk activity `[inf]`.
6. **Herd size config default 4 overrides every species** (1 to 50) unless set to 0.
7. **Shock counter lasts only 2 ticks.** The flee goal must catch it at once. A second shock inside the fence lengthens nothing. [v] `ctygd/omogwgxr.java:728-742`.
8. **Failed-path throttle is weak.** It only blocks a second search in the same tick. The real cost control is the goal backoff and the blacklist.
9. **Entity update interval:** creature types set `clientTrackingRange(10)` and no update interval (vanilla default). [v] `core/vtclb.java:164,171`.
10. **Not read:** sound-range method (did not decompile), client rendering, spawn-time group logic beyond the cap, offspring creation class and vehicles.

---

## 20. 26.x API shapes seen (reference for P2)

JC3 compiles against NeoForge 26.1.2 and uses these shapes. They help the P2 port.
- Server-level overloads: attack, hurt and AI step take a `ServerLevel`. Save and load use `ValueOutput` and `ValueInput`. Synced data uses a `Builder` in the define method.
- `Identifier` replaces `ResourceLocation`. `PathType` replaces `BlockPathTypes`. `EntitySpawnReason` replaces `MobSpawnType`.
- `Attributes.STEP_HEIGHT` and `Attributes.TEMPT_RANGE` exist. `EntityDimensions.scalable(...).withEyeHeight(...)`. `Level` gives a part collection and a default clock time. `GameRules` are read with a typed key.
- `SavedDataType` with a codec for saved data. `LivingChangeTargetEvent`, `RegisterSpawnPlacementsEvent` and an `EnumProxy` for a new `MobCategory`. Biome modifier types registered as codecs.
- Item use: `interactLivingEntity` on items; `mobInteract` on the entity returns `InteractionResult`.

---

## 21. Project Nublar fit

| Technique | JC3 source | Module | Verdict | Reason |
|---|---|---|---|---|
| Energy pool and burn from one species number; water at 2x | `gyovxqy/gjtxsudo.java` | B17, B00 | Adapt | Same single-key idea, but use explicit per-species keys. Health-squared pools make giants hold huge reserves. |
| Threshold ladder 0.8 / 0.75 / 0.7 / 0.3 / 0.2 | same | B17 | Adapt | Plan has 75 and 25 percent. Add the "no sleep when desperate" gate. |
| Starve damage 5 percent per 40 ticks at 0.2 | same | B17 | Defer | The lead decided 1 HP per 80 ticks at zero (2026-10-06). Use JC3 only as a comparison. |
| Five feed tiers by tag (300 to 13,500) | `nkjvgug/gjtxsudo.java`, tags | B17, B20 | Adopt | Fits "other mods' food counts through tags". Gives the feeder a value scale. |
| Source registry sorted by priority | `nkjvgug/dbgouk.java` | B17, B20 | Adopt | Small, extendable. Items, carcass, feeder, plants in one list. |
| Exponential retry delay and dirty-chunk wake | `ztiurxyn/cvrd.java`, `nkjvgug/jgdp.java`, `ztiurxyn/stta.java` | B17, B25 | Adopt | Cuts search cost to near zero when nothing changed. |
| 64-slot failed-target blacklist, 600 ticks | `nkjvgug/kdhoqn.java` | B17, B19, B26 | Adopt | Stops repeat path tests to unreachable food and prey. |
| Palette pre-filter and shell search with test budget 6 | `nkjvgug/dnukfuguy.java`, `vtclb.java` | B17, B25 | Adopt | Bounded block scans. |
| Hunt gate: 1 in 10, hungry, health ratio 0.9, cooldown 50 to 69 | `ztiurxyn/omogwgxr.java` | B17, B19 | Adapt | Add a diet filter and a prey ranking (plan wants prey choice). |
| Sight-only detection | same | B19 | Defer | Plan wants sight, sound and scent. JC3 has no model for the last two. |
| Temperament matrix | same | B19 | Adapt | Clear table of who hunts, retaliates and panics. |
| Herd goal (16 / 8 / 3) | `ztiurxyn/kdhoqn.java` | B26 | Drop | The herd redesign on `kash's-slop` is far ahead. |
| Alert-others and young-target defence | `prulnm.java`, `lyhmwcjw.java` | B18 | Adapt | Fits "adults guard the young". Keep the event wake-up. |
| Sleep schedule table, threat test, 200-tick hurt lockout, shelter search | `qkek.java`, `omogwgxr.java:522` | B17 | Adapt | Replace the crepuscular window. |
| Mating gates (cooldown, pack cap, offspring nearby) | `fgwme.java`, `aprffirmi/gjtxsudo.java` | B18 | Adapt | Same gates the plan names. Use JC3 numbers as a start. |
| 8-minute pregnancy, 6,000-tick nest, delivery half-way | `gyovxqy/kodkieb.java` | B18 | Adapt | Numbers for the nest flow. |
| Breeding wand pairing | `bugratnc/gjtxsudo.java` | B18 | Defer | Useful for tests and creative; not in the plan. |
| Carcass hit points and bite-drop-eat loop | `omogwgxr.java:837-964`, `nkjvgug/kodkieb.java` | B16, B17 | Adopt | Matches B16 carcass scope. |
| Shock flee, step cancel on partial blocks, fence as ladder for climbers | `qkek/aeygzxj.java`, `ztiurxyn/kodkieb.java`, `omogwgxr.java` | B15, B19 | Adapt | Fence parity rules decide. See Q2. |
| Node budget 200, one failing search per tick | `incf/aeygzxj.java` | B26, B25 | Adopt | Big, cheap win for large bodies. |
| Column-cache node evaluator with verify flag | `incf/dnukfuguy.java` | B26, B25 | Adapt | Pair with B26's `BodyFit` probe. |
| Progress-window unstick (60 ticks, 2 blocks) | `ysulhvcy.java` | B26 | Adapt | Compare with the herd stuck and re-path step. |
| Step height formula; leaf breaking and leaf malus | `omogwgxr.java:453`, `ctygd/gjtxsudo.java` | B16, B26 | Adapt | Leaf rules only if the lead wants breakable leaves. |
| Swim physics, 40-tick path pause, 300 air | `dfdtwro/*`, `ctygd/kijk.java` | B16 | Defer | Beta has no marine species. |
| Part index (16-cell map, count check, foreign fallback, verify) | `ctygd/kdhoqn.java`, `LevelPartScanMixin.java` | B27, B25 | Adopt | Needed once parts exist. |
| Visited-set presize mixin | `BlockIntersectVisitSetSizeMixin.java` | B25 | Adapt | Small win for big hitboxes; test against the active sweep method in 26.2. |
| Pose hitbox sets blended by animation time | `omogwgxr.java:1654-1801` | B27 | Adapt | Ties hitbox to the pose state. |
| Animation state (name, start tick), server events, exclusivity, 4-tick fade | `octtcpmd/*` | B27, B19 | Adopt | Same as the planned synced pose state. |
| Pounce maths | `wgpfjieca.java` | B19 | Adapt | For raptor-type attacks. |
| Imprint (10 percent, 600-tick feed gap), set-order packet with owner and 64-block check | `omogwgxr.java:1153`, `znpnvzxz/onzkovhp.java` | B19 | Adapt | Small, safe taming stub. |
| Feeder claim, lid, dispense | `dnukfuguy/ummp.java` | B20 | Adapt | Fits the three-feeder plan. |
| PaleoPad status panel | `bugratnc/fgwme.java` | B17, B19 | Adapt | Good debug and player tool. |
| Spit, bite effects, tree climb and glide, shoulder sit | various | later | Defer | No Beta species uses them. |
| Dirty-flag attribute refresh | `omogwgxr.java:433-477` | B16 | Adopt | The plan already says so. |
| 26.x API shapes | section 20 | P2 | Adapt | Reference only. |

---

## 22. Open questions

**Q1. Which need model does the lead want?** JC3 starves at 20 percent and blocks sleep at 30 percent. The plan starves at zero and uses 75 and 25 percent. Which ladder is the target?

**Q2. Should climbing species go over fences?** JC3 treats a fence as a ladder for species with `can_climb`. The B15 parity rule says the original 1.12.2 fence rules win. What did the original do for raptors?

**Q3. Should a hungry neutral herbivore fight?** JC3 allows it by omission. The plan says herbivores graze what their head can reach. A diet filter on targets decides this.

**Q4. How much of the JC3 data model do we mirror?** Six keys are unread there. B00 can drop them or give them readers.
