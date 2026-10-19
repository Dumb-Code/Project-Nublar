# Reference mods chart a lighter, richer Beta

Date: 2026-10-10. Status: research report for the project lead. It changes no code and no plan module.

Project Nublar can keep 20 ticks per second with 100 dinosaurs after the 26.2 port. Most of the gain comes from its own code, fixed with patterns that the surveyed mods prove. Three block-scanning sensors on every dinosaur cost about 22 ms per tick at 100 dinosaurs, with aligned spikes near 440 ms (estimate). A feeder registry, a remembered water source and gated, jittered SmartBrainLib 2 sensors cut that cost below 0.1 ms. Per-tick machine packets, fence line walks and colour layers are the next wastes. The colour layers use about 80 % of each frame. Each waste has a small, proven fix. For gameplay, the mods agree on vanilla idioms: wolf-style commands, turtle-style eggs, bee-style group retaliation and a 5-minute breeding cooldown. Nublar can adopt them with no conflict with vanilla Minecraft. No surveyed mod implements Nublar's signature systems: chance-based detection, scent trails, stamina-limited chases, wire-health fences, track tours or tranquilizers. Vanilla classes, such as the Warden's vibration listener and the visibility math of target selection, supply most of their building blocks. This report rates 135 techniques against modules B11 to B27 and keeps every lead decision. It ends with 27 clean-room specifications and six open items.

**Labels.** `[v]` means verified in source at the commit that the notes name. `[inf]` means inference. `[doc]` means a README, wiki, changelog or store page. Every cost figure without `[v]` is an estimate. Nobody profiled a 26.2 build.

**Names.** "The plan" is [project-plan.md](../docs/project-plan.md). "Section 11" is section 11 of [codebase-analysis.md](../docs/codebase-analysis.md). "The hitbox design" is [dinosaur-hitboxes-and-movement-design.md](../docs/dinosaur-hitboxes-and-movement-design.md). "The bake spec" is [2026-10-08-dino-layer-baking-design.md](../docs/superpowers/specs/2026-10-08-dino-layer-baking-design.md). "Repository facts" defines the short source names (for example "AC", "FAR", "SBL").

## Ten changes remove most of the cost and add the most play

The ten highest-value changes, in recommended order. Effort: S is under 100 lines, M is one or two classes with tests, L is a new subsystem.

| Rank | Change | Module | Effort | Expected effect | Precedent |
| --- | --- | --- | --- | --- | --- |
| 1 | Let SmartBrainLib 2 tick the brain. Delete the manual brain tick, and flip the predicate argument order in the three custom sensors | P2 | S | Prevents a double brain tick, which doubles sensor and behaviour cost and halves every `runFor` time `[inf]` | SBL 2.0.3 `MobMixin` ticks the brain after `customServerAiStep` `[v]` |
| 2 | Find feeders through a per-level registry. Find water with a small sensor that runs only while thirsty. Jitter every sensor start | B17, B20 | M | Block reads fall from about 735,000 to under 1,000 per tick at 100 dinosaurs. About 22 ms becomes under 0.1 ms. The aligned spike disappears | SBL `onlyScanIf` and POI sensor `[v]`, FAR feeder search `[v]`, AFT block-entity map walk `[v]` |
| 3 | Bake the colour layers of each genome into one texture | B09 | L | 20 Gallimimus on screen: 26 to 28 fps today `[v]`. Spec target: at least 120 fps `[doc]` | The bake spec `[doc]`, EMF single render type `[v]` |
| 4 | Send machine progress through `ContainerData` while a menu is open. Send block updates only on a state change | B12, B14 | S | About 100 full-NBT packets per second per player for 5 machines fall to near zero | FAR edge-only state flip `[v]`. AC furnace per-tick update is the anti-pattern `[v]` |
| 5 | Cache fence power per network and wire boxes per block entity. Tick posts only on change | B14, B15 | M | Hundreds of allocations per touching dinosaur per tick, and 60 post loops per tick, fall to none | AC block-position cache `[v]` |
| 6 | Pick prey once per sensor scan from the shared visible list. Cache the enclosure test per target | B17 | M | About 1,200 lookups and 800 allocations per hungry carnivore per tick become one list pass per scan `[inf]` | SBL `NearestVisibleEntityFilteredSensor` `[v]` |
| 7 | Hold the last pose beyond 32 blocks, and skip animation frames by distance and body size | B09 | M | 50 to 75 % fewer keyframe evaluations for herds seen at 32 to 96 blocks | EMF LOD `[v]`, AzureLib LOD `[v]` |
| 8 | Build detection from vanilla pieces: game-event hearing, visibility-scaled sight, a view cone, one roll per episode | B19 | M | Hearing costs nothing between events. Sight costs at most one cached ray per player per scan `[v]` | Warden and Allay listeners `[v]`, `TargetingConditions` `[v]`, Enderman cone `[v]` |
| 9 | Keep stress as one integer with a 20-tick step, species thresholds and one synced band | B17 | S | One value per dinosaur drives fence attacks, aggression and the breeding gate | Warden anger `[v]`, MCA mood `[v]`, FAR mood `[v]` |
| 10 | Run threat display and attack as one track: display clip, one hit tick with a cone test, recovery | B19, B27 | M | Players get a 2 to 4 s warning. The server sends one trigger per attack and no per-tick animation packets | Warden roar `[v]`, Polar Bear stance `[v]`, Mowzie ability sections `[v]`, Mutant Monsters hit tick `[v]` |

Items 1 to 6 remove server waste. Item 3 and item 7 recover the frame rate. Items 8 to 10 add the behaviour that section 11 asks for at almost no tick cost.

## Library hooks come before Nublar code

**Rule (decided by the project lead on 2026-10-10):** where SmartBrainLib 2, GeckoLib 5 or Balm gives a hook for a need, Nublar calls the library through its API. Nublar does not copy library code. It does not write a second version of a library feature. The project plan has this rule in its global constraints. Section 11.17 has the general rule.

Every recommendation in this report follows the rule. The clean-room specifications cover only behaviour that no dependency supplies.

**Needs that a library hook covers:**

| Need | Library hook | Module |
| --- | --- | --- |
| Brain tick | SBL 2 ticks the brain after `customServerAiStep`. Delete the manual tick `[v]` | P2 |
| Activities and priorities | `ActivityBuilder` (replaces `BrainActivityGroup`) `[v]` | P2, B17 |
| Sensor cost | `onlyScanIf` gate and a per-entity `scanRate` `[v]` | B17, B20 |
| Nearby animals and players | `NearbyLivingEntitySensor`, `NearbyPlayersSensor`, with memoised line of sight `[v]` | B17, B19 |
| Retaliation | `HurtBySensor` `[v]` | B19 |
| Dropped meat and tempt items | `NearestItemSensor`, `ItemTemptingSensor` `[v]` | B17, B19 |
| Prey and attack targets | `GenericAttackTargetSensor`, `SetAttackTarget`, `InvalidateAttackTarget` `[v]` | B17 |
| Follow, flee, wander | `FollowEntity`, `FollowParent`, `AvoidEntity`, `FleeTarget`, `Panic`, `SetRandomWalkTarget`, `MoveToWalkTarget` `[v]` | B16, B18, B19 |
| Breeding | `BreedWithPartner` `[v]` | B18 |
| Sleep window | `SmartBrainSchedule.byDaytime` `[v]` | B17 |
| Need cooldowns | Memories set with an expiry `[v]` | B17 |
| Warning and attack clips | GeckoLib 5 triggered animations `[v]` | B19, B27 |
| Head-only idle clips | GeckoLib 5 additive controllers `[v]` | B09 |
| Procedural sway and neck bend | GeckoLib 5 bone updaters, additive to the clip `[v]` | B09, B27 |
| Model growth | GeckoLib 5 applies the 26.2 `scale` attribute to the model `[v]` | B16 |
| Clip speed | `setAnimationSpeed` in the state handler `[v]` | B09 |
| Energy and fluids | Balm storage and its loader bridges `[doc]` | B12, B14 |

**Needs with no hook.** Nublar writes its own code only for these. The code extends the library's base classes, such as SBL `ExtendedBehaviour` and `ExtendedSensor`.

- stamina-limited chases (B17)
- scent trails (B19)
- dominance contests (B18)
- nests (B18)
- herd formation and spacing (B26)
- detection rolls (B19)

**Features a library lacks.** Nublar uses the library's extension points and does not fork the library. GeckoLib 5 has no culling or animation LOD, so B09 adds distance LOD through a GeckoLib override `[inf]`. If that override is not supported, the fallback pauses far controllers.

## Three server paths and one client path carry most of the cost

**In game:** a park of 100 dinosaurs, 4 machines, a generator and 60 fence posts keeps 20 ticks per second. A herd on screen keeps the frame rate.

The hot paths come from the local code baseline (`kash's-slop` at 0c97bde plus the uncommitted tree). Each row names its owner module. The hitbox design owns parts, bend and capsule costs. This plan does not change its numbers.

### Assumptions behind every number

| Assumption | Value | Source |
| --- | --- | --- |
| Block-state read | 30 ns | Hitbox design and local baseline `[doc]` |
| Small `noCollision` query | 3 µs | Hitbox design `[doc]` |
| Hash-map lookup | 20 ns | Local baseline `[inf]` |
| Small allocation | About 30 bytes | Local baseline `[inf]` |
| Tick budget | 50 ms at 20 ticks per second | Vanilla |
| Load A | 20 Gallimimus in one herd, 1 player | Hitbox design `[doc]` |
| Load B | 100 dinosaurs in 5 herds (10 large), 3 players, 600 other entities | Hitbox design `[doc]` |
| Park | 4 machines, 1 generator, 60 posts, 15-block wire runs | Plan B25 profile `[doc]` |
| Hungry carnivores at once | 12, each with 20 candidates at 20 blocks | Assumption `[inf]` |
| Thirsty dinosaurs at once | 30 | Assumption `[inf]` |
| Dinosaurs that touch wires | 10 | Assumption `[inf]` |

### Server hot paths (load B)

| Id | Hot path | Today (estimate) | Remedy | Precedent | After (estimate) | Owner |
| --- | --- | --- | --- | --- | --- | --- |
| S1 | Block-scanning sensors: water (radius 20), feeder (radius 20), fence (radius 10) | 147,103 reads per dinosaur per 20 ticks `[v]`. About 735,000 reads per tick, about 22 ms. Scans that align: 14.7 million reads in one tick, about 440 ms `[inf]` | Feeder registry. Water: radius 8 by 2, rate 60, only while thirsty, remembered source with expiry. Fence sensor: rate 100, only while starving with a target. Jittered starts | SBL `[v]`, FAR `[v]`, AFT `[v]` | Under 1,000 reads per tick, under 0.1 ms. No aligned spike | B17, B20 |
| S2 | Entity sensors and group join | 5 queries per tick over a 70-block cube (follow range 35) `[v]`, about 10 µs each `[inf]`. `JoinGroup` adds its own query every 40 or 200 ticks `[v]` | Species follow range from `SpeciesData`. One `NearbyLivingEntitySensor` feeds prey, herd, threat and breeding. `JoinGroup` filters that memory | SBL `[v]`, Cobblemon herds `[v]` | About 0.03 ms | B17, B18 |
| S3 | Carnivore target search | A block ray-march per candidate per tick: 1,200 lookups and 800 allocations per hungry carnivore `[inf]`. 12 carnivores: about 0.5 ms and 9,600 allocations per tick | One scored pass per sensor scan over the visible list. Enclosure flag cached per target for 100 ticks. Fix the block-entity test that is always false `[v]` | SBL `[v]` | Under 0.01 ms, no per-tick allocation | B17 |
| S4 | Brain start checks | About 1 to 2 µs per dinosaur per tick, 0.1 to 0.2 ms `[inf]`. The idle group tests up to 11 start conditions per tick `[v]`. A double tick after the port doubles this `[inf]` | No manual brain tick. `cooldownFor` on idle behaviours. Activities with priorities (plan B17) | SBL `[v]` | About 0.1 ms | P2, B17 |
| S5 | Need walks and journey paths | One A* path costs 1 to 2 ms (Gallimimus) and about 5 ms (Brachiosaurus) `[doc]`. Need walks and journey legs have no cap `[v]`. Ten requests in one tick cost 10 to 50 ms | Per-level path budget with a queue. Unreachable-target memory. Need cooldown (plan B17). Need ticks offset by entity id | Hitbox design budget `[doc]`, FAR avoid cache `[v]`, MCA back-off `[v]` | Spikes held to the budget (a config value) | B17, B26 |
| S6 | Fence contact (`entityInside`) | Per touching entity per tick: 16 box allocations, then about 15 line steps with 6 allocations and 1 block-entity lookup each `[v]` `[inf]`. 10 dinosaurs: about 1,000 allocations per tick | Boxes built once per block entity. One cached powered flag per network section | AC cache `[v]` | No allocation, under 0.005 ms | B15 |
| S7 | Fence post tick | Every post every tick: rotation recompute with a list allocation, a 10 FE drain, and a set, a list and a sort above 300 FE `[v]`. 60 posts: about 0.06 to 0.18 ms `[inf]` | Rotation on connection change. Energy through `FencePowerService` per network | Local code `[v]` | Under 0.01 ms | B14 |
| S8 | Machine sync | Processor, sequencer, egg printer, incubator and generator each send a full-NBT block-entity packet every tick while active `[v]`. About 20 packets per second each, 0.5 to 3 KB each `[inf]` | `ContainerData` while a menu is open. Block update on a slot or state change | FAR `[v]` | 0 to 2 packets per second per machine | B12, B14 |
| S9 | Machine work | The sequencer parses `DNAData` NBT twice per tick. The incubator and the printer use one item per tick, each with a block update `[v]` | Parse on a slot change. Refill on events | Local code `[v]` | Negligible | B06, B11 |
| S10 | Synced dinosaur data | 41 synced fields per dinosaur `[v]`. Full `DinoData` NBT on each change. 12 need floats and 16 animation booleans synced `[v]` | Sync genome visuals, the pose state (about 6 bytes, hitbox design) and two debug bytes. Keep needs on the server. `DINO_BEHAVIOUR` goes with plan B00 | Cobblemon byte flags `[v]`, MCA game-time age `[doc]` | Smaller join payload, fewer packets per change | B16, B27 |
| S11 | Herd layer | Under 1 ms (20) and about 5 ms (100) `[doc]`. Per follower per tick: `Roster`, `TrailPoint`, `Target`, `CollisionContext` and `AABB` allocations, and an O(n²) gather `[v]`. Tens of KB of garbage per tick at 100 `[inf]` | Reuse one roster. Return primitives. One `CollisionContext` per probe batch. An index map for the gather | Local code `[v]` | Same CPU, near-zero garbage | B26 |
| S12 | Parts, bend and capsules | Planned rows of the hitbox design `[doc]` | Owned by the hitbox design | Hitbox design | 9 to 15 ms total at load B, herd AI included `[doc]` | B26, B27 |

### Client hot paths

| Id | Hot path | Today | Remedy | Precedent | After (estimate) | Owner |
| --- | --- | --- | --- | --- | --- | --- |
| C1 | Colour layers | 23 model draws per Gallimimus per frame `[doc]`. 20 Gallimimus: about 370 draws, 26 to 28 fps against 138 fps with layers off `[v]`. About 40 `Stream` and `Color` allocations per dinosaur per frame `[v]` `[inf]` | Bake per genome key (the bake spec). Until then: no white pass for white tints (plan B09), cached layer colour and index | The bake spec `[doc]`, EMF `[v]` | One draw per dinosaur. At least 120 fps for 20 Gallimimus `[doc]` | B09 |
| C2 | Head-position writes | Six renderers write synced data every frame, with 3 to 4 allocations each `[v]` | Removed with the dead head-part code (plan B27) | Plan | None | B27 |
| C3 | Animation evaluation | About 370 small allocations and 300 to 900 vertices per model pass for 12 bones. 9 keyframe evaluations per animated bone per controller `[v]` `[inf]` | Hold-pose LOD beyond 32 blocks. Skip count = distance ÷ 20, divided by half the largest box side for bodies over 2 blocks. Two controllers at most | EMF `[v]`, AzureLib `[v]` | 50 to 75 % fewer evaluations for herds at 32 to 96 blocks | B09 |
| C4 | Culling | Fixed boxes (Brachiosaurus 2 by 3) `[v]` pop the model at the frustum edge | Culling box from the scaled size (plan B09). Union of parts (hitbox design) | AC inflated boxes `[v]` | Correct culling | B09, B27 |
| C5 | Fence renderers | About 900 wire block entities each allocate a list and a lambda per frame `[inf]` | Iterate connections directly. Cache vertex data per connection | Local code `[v]` | No per-frame allocation | B15 |
| C6 | Machine renderers | Rendered off screen. A string compare per bone per frame `[v]` | Normal frustum test with a render box. Hidden-bone set built on a state change | GeckoLib 5 `skipRender` `[v]` | Small | B12 |
| C7 | Leg IK | 1.1 ms at load A `[doc]` | Once per tick, none past 32 blocks (hitbox design) | Hitbox design | About 1 ms at load B `[doc]` | B27 |

### Budget before and after (load B, estimates)

| Subsystem | Today | After | Owner |
| --- | --- | --- | --- |
| Block-scanning sensors | About 22 ms, aligned spikes near 440 ms | Under 0.1 ms | B17, B20 |
| Entity sensors and group join | About 0.05 ms | About 0.03 ms | B17, B18 |
| Carnivore target search | About 0.5 ms and 9,600 allocations | Under 0.01 ms | B17 |
| Brain start checks | 0.1 to 0.2 ms (double if ticked twice) | About 0.1 ms | P2, B17 |
| Need-walk paths | Unbounded spikes | Held to a per-tick budget | B17, B26 |
| Fence contact and posts | 0.1 to 0.25 ms and over 1,000 allocations | Under 0.02 ms, no allocation | B14, B15 |
| Machine sync (5 machines) | About 100 full-NBT packets per second per player | 0 to 10 per second | B12, B14 |
| Herd AI, parts, bend, capsules | Herd AI about 5 ms | 9 to 15 ms in the hitbox design budget | B26, B27 |
| **Server total (without A* spikes)** | **About 28 ms plus 440 ms spikes** | **About 10 to 16 ms** | — |

Two results here are uncertain. First, the GeckoLib 5 LOD relies on an override of `applyAnimationControllers`, a default method of an internal interface `[inf]`. If that override is not supported, pausing far controllers is the fallback, and it saves less. Second, the SBL 2.0.3 changelog claims random sensor offsets `[doc]`, but nobody verified that they reach the SBL timer `[inf]`. Nublar adds its own offset through `scanRate(e -> base + (e.getId() & 15))`, which the API supports `[v]`.

## Gameplay alignment: 89 adopt, 11 adapt, 17 defer, 18 drop

**In game:** the player feeds, tames, breeds and contains dinosaurs with the same blocks and signals that run the rest of a Minecraft base. Dinosaurs behave like large, believable animals, and vanilla mobs keep their vanilla behaviour.

### Vanilla channels tie the park into one loop

| Channel | In game | Nublar use | Module |
| --- | --- | --- | --- |
| Comparator | A comparator reads a feeder or a machine | Refill and alarm circuits | B12, B20 |
| Hoppers and pipes | Items move with no player | Feeders and machines stay stocked | B05, B20 |
| Redstone switch | A signal cuts power to a fence section | Maintenance and planned shutdowns | B15 |
| Game events | A mob emits step, damage and eat events `[v]` | Sculk sensors raise an escape alarm. Dinosaurs hear players | B15, B19 |
| Tags | A data pack edits a list | Diets, prey, breakable leaves, nest floors | B17, B18 |
| Villager trades and maps | Professions and explorer maps | Paleontologist trades, digsite maps | B21 |
| Structures | `/locate` and jigsaw generation | Digsites and Paleo-Illager sites | B21 |

### Seven vanilla-feel rules from the surveyed mods

| Rule | Evidence | Module |
| --- | --- | --- |
| Use vanilla idioms: wolf-style commands, turtle-style eggs, bee-style group retaliation, a 5-minute breeding cooldown | AC code and wiki `[v]` `[doc]`, FAR `[v]`, FAF `[doc]` | B11, B18, B19 |
| Touch vanilla mobs only by addition (tags, events, goals added after construction). Never cancel their goal lists | Naturalist changelog `[v]`, RevampedWolf goal cancel `[v]`, skeleton-ai-fix `[v]` | B20, B21 |
| Add no tick mixin to a vanilla base class and no global per-entity hook | AC `EntityMixin` `[v]`, Citadel tick-rate gate `[v]` | B25 |
| Give every vanilla-touching behaviour a config switch | AM toggles `[v]`, FAF config `[v]`, Vanilla Refresh toggles `[v]` | B00 |
| Never overwrite a vanilla data file. Add through inject pools or runtime mutation | Illager Invasion `[v]`, Better Villages compatibility packs `[doc]` | B21 |
| Keep real animals provokable, not hostile, and give every creature a function | Alex's Mobs design rules `[doc]` | B16, B19 |
| Gate cosmetic work by player distance, never the simulation | Vanilla Refresh `[v]` | B25 |

### How the tables rate a technique

| Fit | Meaning |
| --- | --- |
| 3 | Section 11 or a plan module asks for it, or the technique is the cheapest way to build a decided item |
| 2 | Section 11 is silent. The technique supports a decided feature with a small gameplay effect |
| 1 | The technique needs a lead decision with a real gameplay effect |
| 0 | The technique conflicts with a lead decision or targets content outside Beta |

Verdicts: **Adopt** builds the technique nearly as the precedent does. **Adapt** keeps the idea and changes the form to agree with section 11. **Defer** waits for a decision, art or a measurement. **Drop** leaves it out of Beta. Effort counts code in addition to the module's existing scope. A row id such as B17.4 links to "Clean-room implementation specifications" and "New items for the plan".

### B11 Synthesis, egg printer, incubator and hatch

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B11.1 | Placed egg is a block with a block entity. It stores its placement time and computes stage times. Scheduled block ticks advance three crack stages. No ticker | Adopt | 3 | M | AC egg stages `[v]`. FAR living-entity egg as contrast `[v]` | Decision D05 default. Fixed time from species data. AC hatches by random tick, mean about 3.4 days with high variance `[inf]` |
| B11.2 | The crack stages and the wobble run over `incubation.wobble_seconds` after placement | Adopt | 3 | S | AC crack stages `[v]` | Section 11.7: eggs shake before they hatch |
| B11.3 | Hatch tests free space for the baby box. If blocked, it waits and shows a message | Adopt | 2 | S | FAR "This Egg Cannot Spawn Here" `[doc]` | A baby that hatches in a wall suffocates |
| B11.4 | Hatch beat: baby at growth 0, shell particles, crack and hatch sounds. Dinosaurs pass through eggs | Adopt | 3 | S | AC hatch routine and egg collision `[v]` | Plan item (hatchling starts growth at 0) |
| B11.5 | Hatchling owner: the egg stores the placer, or the nearest player imprints | Defer | 1 | S | IaF CE owner on egg `[v]`, AC imprint 10 blocks `[v]`, FAR imprint 16 blocks `[v]` | Section 11 has no imprint rule. Open item O3 |
| B11.6 | Progress falls at twice the rate while the machine cannot work | Drop | 0 | S | FAR machine base `[v]` | Section 11.7 decides stall and stall death |
| B11.7 | A failed run spawns a hostile mob | Drop | 0 | S | FAR culture vat (10 %) `[v]` | Section 11.6 risk is incubation failure. Breaks vanilla feel |
| B11.8 | A bedding block speeds a natural hatch | Defer | 1 | S | AC Fern Thatch, 2 times faster `[v]` | Natural nests are B18. Section 11 sets no bedding rule |
| B11.9 | Light keeps an egg warm outside an incubator | Drop | 0 | S | FAR egg light test `[v]` | The incubator is the only Beta path |

### B12 Machine framework

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B12.1 | Progress through `ContainerData` while a menu is open. Block updates only on a slot or state change. The ACTIVE state flips only on the working edge | Adopt | 3 | S | FAR edge-only flip `[v]`. AC furnace per-tick update is the anti-pattern `[v]` | Removes about 20 full-NBT packets per second per active machine (S8) |
| B12.2 | Recipe lookup cached per input item and cleared on `/reload` | Adopt | 2 | S | FAR memo map, never cleared `[v]` | One hash lookup per tick. FAR shows the missing reload clear |
| B12.3 | Every part of a multi-block machine forwards use, shape and container queries to one master block entity | Adapt | 3 | M | AC nuclear furnace `[v]` | Section 11.8 decides ordered assembly. One inventory and one drop list per machine |
| B12.4 | Comparator output from progress or contents, clamped to 15 | Adopt | 2 | S | FAR feeder signal (unclamped) `[v]`, AFT `[v]` | Redstone automation with vanilla rules |
| B12.5 | Machine renderer: hidden-bone set built on a state change, normal frustum test | Adopt | 2 | S | GeckoLib 5 `skipRender` `[v]`, local renderers `[v]` | Removes per-bone string compares and off-screen draws (C6) |
| B12.6 | Hologram preview of a species on a machine | Defer | 2 | M | AC hologram projector `[v]` | Useful for tabs. Not in section 11 |
| B12.7 | A full block update every tick while active | Drop | — | — | AC furnace `[v]` | Per-tick waste |

### B14 Power

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B14.1 | Generator sends block updates only on change | Adopt | 3 | S | Local generator `[v]`, FAR `[v]` | The generator updates every tick today, also when idle |
| B14.2 | Fence posts tick only on an energy or connection change. Rotation cached | Adopt | 2 | M | Local post tick `[v]`, AC block-position cache `[v]` | 60 posts allocate and loop every tick today (S7) |
| B14.3 | One cached powered flag per fence network section, set on power change | Adopt | 3 | M | rft `FencePowerService` (plan), AC cache `[v]` | B15 reads one flag instead of a line walk |
| B14.4 | Machines run without energy by default | Drop | 0 | S | FAR `machinesRequireEnergy` false `[v]` | Section 11.8 decides real power |
| B14.5 | An energy API from a creature mod | Drop | — | — | AC has none `[v]` | Balm stays the energy bridge |

### B15 Fences and containment

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B15.1 | Wire collision boxes built once per block entity and rebuilt on connection change. Shock and deterrent read the cached powered flag (B14.3) | Adopt | 3 | S | Local `entityInside` (8 boxes per connection per tick) `[v]` | Removes about 1,000 allocations per tick (S6) |
| B15.2 | Fence sensor scans only while starving with a target, rate 100 ticks | Adopt | 3 | S | SBL `onlyScanIf` `[v]` | 9,261 reads per scan today, always `[v]` |
| B15.3 | `UnreachableTargetSensor` and `ReactToUnreachableTarget` start a fence attack when prey stands behind a fence | Adopt | 2 | S | SBL `[v]` | Library behaviour, no new code path |
| B15.4 | Wire renderers iterate connections without a stream | Adopt | 2 | S | Local renderers `[v]` | About 1,800 allocations per frame for a 60-post paddock (C5) |
| B15.5 | Players build escape alarms with vanilla sculk sensors | Adopt | 2 | None | Vanilla game events, sensor radius 8 or 16 `[v]` | Dinosaurs already emit step events. Document it only |
| B15.6 | Iron-tier block breaking, with reinforced glass as the only safe wall | Drop | 0 | — | FAR `[doc]` | Wire health replaces it (section 11.11). Vanilla-feel conflict |

### B16 Dinosaur core

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B16.1 | `MobCategory.CREATURE` with required persistence | Adopt | 3 | S | FAR `[v]`, IaF CE `[v]` | The plan prefers it. Both mods confirm it for never-despawning animals |
| B16.2 | Attributes from `SpeciesData` applied on change only. The dinosaur caches its `SpeciesData` reference and refreshes it on reload | Adopt | 3 | S | Cobblemon attribute refresh `[v]`, FAR repeated lookups `[v]` | No per-tick attribute writes or string-keyed lookups |
| B16.3 | Babies follow a parent from the shared visible list: box 8 by 4 by 8, stop at 3, give up at 16 | Adopt | 3 | S | Vanilla `FollowParentGoal` `[v]`, SBL `FollowParent` `[v]` | Avoids the vanilla box query every 2 ticks |
| B16.4 | Baby differences: no hunting, no roar effect, bite only, no player target | Adapt | 3 | S | AC baby list `[v]`, vanilla Sniffer and Polar Bear `[v]` | Section 11.9: babies cannot hunt and need care |
| B16.5 | The carcass holds the decided drops. Each player use takes one stage. Scavenger bites remove meat. The timer still decays it | Adapt | 3 | M | IaF CE staged corpse `[v]` | The only carcass precedent. Keeps the decided drops and timer |
| B16.6 | Sync only what the renderer needs. Keep the 12 need floats on the server. Debug status as two bytes on change | Adopt | 3 | S | Cobblemon byte flags `[v]` | B16 decided the two-value debug status. Need floats sync today `[v]` |
| B16.7 | A live tuning command for scale and eye height | Defer | 2 | S | Cobblemon `/changescaleandsize` `[doc]` | Helps D11 tuning. Not required |
| B16.8 | Baby health from a square-root curve | Drop | 0 | S | Naturalist `[v]` | Smooth growth interpolates stats (section 11.9) |
| B16.9 | A stat bonus for the group leader | Drop | 0 | S | AC Elder `[v]` | Dominance decides leaders |

### B17 Needs

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B17.1 | Feeders found through a per-level position registry that each feeder joins on load and leaves on removal | Adopt | 3 | S | FAR block-entity map walk `[v]`, AFT `[v]`, SBL POI sensor `[v]` | The plan asks for a cache or POI search. A registry is the simplest |
| B17.2 | Water found by a small sensor (radius 8 by 2, rate 60, only while thirsty) and a remembered source with an expiry | Adopt | 3 | S | SBL `onlyScanIf`, `setForgettableMemory` `[v]` | About 24 reads per tick per thirsty dinosaur `[inf]` |
| B17.3 | Every sensor start offset by entity id | Adopt | 3 | S | SBL 2.0.3 offsets `[doc]`, vanilla `Sensor` random start `[v]` | Removes aligned scans (S1) |
| B17.4 | A met need clears its flag and sets a cooldown memory with an expiry | Adopt | 3 | S | SBL forgettable memory `[v]`, MCA expiring keys `[v]`, FAF cooldown memories `[v]` | Plan decided the cooldown. Fixes `IS_HUNGRY` |
| B17.5 | Stress as one integer: inputs on a 20-tick step, species threshold, drift back toward zero, one synced band | Adopt | 3 | S | Warden anger `[v]`, MCA mood `[v]`, FAR mood `[v]` | Section 11.10 inputs only. Cheap |
| B17.6 | Two hunger thresholds: seek food below 75 %, sprint to food below 25 % | Adapt | 2 | S | FAR `[v]` | Fits the starvation and chase rules |
| B17.7 | Prey picked by one scored pass over the shared visible list per scan. Enclosure test cached per target | Adopt | 3 | M | SBL `NearestVisibleEntityFilteredSensor` `[v]` | Replaces the per-tick ray-march (S3) |
| B17.8 | Grazing from a diet block tag, inside a height band per species and age, with sampled columns | Adopt | 3 | M | AC height bands `[v]` | Section 11.10 reach rule. Avoids AC ring scans of about 14,000 reads `[inf]` |
| B17.9 | Dropped meat found by the item sensor only while hungry, radius 16 | Adopt | 3 | S | SBL `NearestItemSensor` `[v]`, IaF CE item goal `[v]` | The sight test per item is the hidden cost `[inf]` |
| B17.10 | Sleep in a day or night window, a sleep cooldown memory, wake on damage with a wake lock | Adopt | 3 | M | SBL `byDaytime` `[v]`, FAF sleep cooldown `[v]`, Naturalist wake timer `[v]` | Section 11.10 stamina and sleep |
| B17.11 | Unreachable food targets in a short avoid list | Adopt | 2 | S | FAR avoid cache `[v]` | Stops repeat paths to blocked food |
| B17.12 | Per-level path budget with a queue for need walks | Adopt | 2 | S | Hitbox design budget `[doc]`, MCA back-off `[v]` | Need walks have no cap today (S5) |
| B17.13 | The hunt cooldown counts down in one place and has a GameTest | Adopt | 3 | S | RevampedWolf counter that never counts down `[v]`, Naturalist 2,400 ticks `[v]` | Plan decided the hunt cooldown |
| B17.14 | Food value per item or tag in species data, with `c:` tags | Adopt | 3 | S | FAR food data `[v]`, Cobblemon item values `[doc]` | Plan decided diets as tags |
| B17.15 | Hunt chance equals 100 minus hunger | Drop | 0 | S | IaF CE `[v]` | Section 11 gates hunts by hunger and stamina |
| B17.16 | Raiding chests for food | Drop | 0 | M | AC raptor `[v]` | Not in section 11 |

### B18 Social life and breeding

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B18.1 | The leader pushes its attack target to members once, on change | Adopt | 3 | S | AC Elder (every tick) `[v]`, vanilla Vex copies its owner's target `[v]` | Raptor pack hunting. One write per change |
| B18.2 | A hurt member writes a panic timer to its group | Adopt | 3 | S | Naturalist deer: 100 to 199 ticks in an 8 by 4 by 8 box `[v]` | B18 warning call. One write per event |
| B18.3 | `JoinGroup` filters the shared nearby-entity memory | Adopt | 3 | S | SBL `[v]`, Cobblemon herds `[v]` | Removes one entity query per dinosaur |
| B18.4 | Breeding: a saved has-egg flag, a walk to the nest, a lay animation, the clutch, a cooldown for both parents | Adopt | 3 | M | Naturalist lay goal `[v]`, vanilla parent age 6,000 `[v]` | Section 11.10 breeding with a nest |
| B18.5 | Nest placement by a floor block tag | Adopt | 2 | S | Cobblemon wild nests `[v]` | Nests fit the ground |
| B18.6 | Protect the young: look for a baby within 8 by 4 first, then a player at half the follow range. Alerts skip babies | Adopt | 3 | M | Vanilla Polar Bear `[v]`, Naturalist `[v]` | Section 11.10: adults guard young |
| B18.7 | Calls as sound events and game events | Adopt | 3 | S | Vanilla game events `[v]` | Calls reach B19 listeners with no new code |
| B18.8 | A dinosaur cap per area for breeding | Defer | 1 | S | MCA population below 0.75 of beds `[v]`, FAR `maxPopulation` `[v]` | Open item O6 |
| B18.9 | Flee when the pack is small or the leader dies | Defer | 1 | S | AC pack-size rule `[v]` | Section 11 is silent |
| B18.10 | Raptors stalk, crouch, then pounce | Defer | 2 | M | Vanilla fox `[v]`, Naturalist tiger `[v]` | Needs crouch and leap clips (B24) |
| B18.11 | A scriptable breeding rule pipeline | Drop | 0 | L | Cobblemon breeding branch `[v]` | The lead decided the midpoint rule. Speculative abstraction |

### B19 Player interaction and detection

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B19.1 | Sight: a 20-tick sensor over players in follow range. Range times visibility (sneak 0.8, floor 2 blocks), a species view cone, a cached line of sight | Adopt | 3 | M | Vanilla `TargetingConditions`, `Sensing`, Enderman cone `[v]` | All vanilla pieces |
| B19.2 | Sound: a `VibrationSystem` listener with a species event tag, a radius and a 40-tick cooldown | Adopt | 3 | M | Warden and Allay `[v]` | Costs nothing between events |
| B19.3 | One detection roll per awareness episode. The result sits in a memory with an expiry | Adopt | 3 | S | Warden memory expiries `[v]` | A 25 % roll per 20-tick scan reaches 76 % in 5 scans `[inf]` |
| B19.4 | Scent: a level store of points with expiry, a sensor with no line of sight, a walk at 0.7 speed | Adopt | 3 | M | Sniffer and Warden sniff `[v]` | No precedent. See "No reference mod implements twelve Beta features" |
| B19.5 | Threat display: a warning state and a sound tied to the attack cooldown, a species time, no display within 5 blocks | Adopt | 3 | M | Polar Bear `[v]`, Warden roar 84 ticks `[v]`, AC roar 55 ticks `[v]`, Naturalist rhino 42 ticks `[v]` | Section 11.10 |
| B19.6 | Flee species: threat test in the sensor, a walk-away target 16 blocks away, an avoid memory of 5 to 10 s, sprint within 7 blocks | Adopt | 3 | M | FAF avoid memory `[v]`, vanilla `AvoidEntityGoal` `[v]` | Decision D13 default. Avoids a box query every 2 ticks |
| B19.7 | Territory through `Mob.restrictTo(home, territory_radius)` | Adopt | 3 | S | Vanilla target test `[v]`, IaF CE home radius 40 `[v]` | No new code path |
| B19.8 | Commands follow, stay, defend: one synced value cycled by owner use, an action-bar message. Follow starts at 7, stops at 2, teleports beyond 18 to a checked spot | Adopt | 3 | M | FAR orders `[v]` | Section 11.10 |
| B19.9 | Reputation: one capped integer per player, daily decay, a threshold | Adopt | 3 | S | Vanilla gossip `[v]` | The `PLAYER_REPUTATION` map is never created today `[v]` |
| B19.10 | Owner safety ends above the stress threshold | Adapt | 3 | S | FAR angry-mood exception `[v]` | Section 11.10: stress can end the bond |
| B19.11 | Every remote-control packet checks ownership or passenger state and distance | Adopt | 3 | S | AC key packet `[v]`. AM eagle packet has no check `[v]` | Plan packet rule |
| B19.12 | A roar frightens other mobs | Defer | 1 | S | AC roar scare `[v]` | Section 11 is silent |
| B19.13 | Taming by stun, then a calming food | Drop | 0 | M | AC Tremorsaurus `[v]` | Section 11.10 tames calm animals. Guns stay blocked |
| B19.14 | Vanilla villagers and livestock flee loose dinosaurs | Defer | 1 | S | Illager Invasion villager hostile map `[v]`, FAR livestock fear `[v]`, IaF CE fear tag `[v]` | Open item O5 |
| B19.15 | A status readout for non-operator owners | Defer | 1 | S | TES own-implementation option `[inf]`, Vanilla Refresh health bar `[v]` | Open item O4 |

### B20 Feeders

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B20.1 | The feeder is a vanilla `Container` with item capabilities on both loaders | Adopt | 3 | S | AFT `[v]` | Plan asks for hopper and pipe input |
| B20.2 | Point pools per diet: items convert to points, each bite takes points | Adopt | 2 | M | FAR feeder: cap 10,000, one item per 5 ticks `[v]` | One feeder serves many dinosaurs |
| B20.3 | The eater stores the feeder position, then fetches the block entity and checks removal at use | Adopt | 3 | S | AFT goal re-check `[v]` | No stale feeders |
| B20.4 | Fill level as a block state 0 to 4, written on change | Adopt | 2 | S | AFT `[v]` | Client visuals with no extra sync |
| B20.5 | Comparator output, clamped to 15 | Adopt | 2 | S | FAR (unclamped) `[v]` | Refill circuits |
| B20.6 | Slots accept only the species diet tag | Adopt | 2 | S | AFT filters on the eater `[v]` | Nublar feeders are diet-specific |
| B20.7 | Eat range equals the walk stop distance plus half the species width | Adopt | 3 | S | AFT 2.0 blocks `[v]` | Plan names the range mismatch |
| B20.8 | The live-bait feeder holds a tagged, unedited vanilla animal and releases it to a hungry carnivore | Adapt | 3 | M | Vanilla Refresh tag discipline `[v]`, FAF lure `[doc]`, FAR laser lure `[v]` | No direct precedent |
| B20.9 | An experience-orb magnet with entity queries every tick | Drop | — | — | AFT `[v]` | Per-tick waste |

### B21 Digsites, paleontologists, Paleo-Illagers and maps

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B21.1 | Digsite as a jigsaw structure in data: depth 3 to 4, surface heightmap, `beard_thin`, a biome tag, spread 42 by 28, a 10-chunk village exclusion, empty fallbacks | Adopt | 3 | M | Illager Invasion `[v]` | `/locate` works with no code |
| B21.2 | People and golems placed by one-template mob pools or data markers | Adopt | 3 | S | Illager Invasion `[v]` | Exact spawn points per piece |
| B21.3 | Chest loot per structure part. Fossils join vanilla tables only through inject pools | Adopt | 3 | S | Illager Invasion `[v]` | No vanilla file overwritten |
| B21.4 | Paleo-Illager as the smallest illager subclass with the vindicator goal set and site spawn overrides | Adopt | 3 | M | Illager Invasion `[v]` | Vanilla raid rules and other mods keep working |
| B21.5 | Paleontologist with a real workstation POI and trade sets as data per level | Adopt | 3 | M | MCA professions `[v]` | 26.2 trades are registry keys `[v]` |
| B21.6 | A village house at a low rate, added to the vanilla pools at server start behind a config value | Adapt | 3 | M | Guard Villagers pool access `[v]`, Better Villages conflict class `[inf]` | Section 11.3: paleontologists spawn at a low rate in villages |
| B21.7 | Any vanilla-mob tweak uses a tag, a config value and a tick event, never a goal replacement | Adopt | 2 | S | skeleton-ai-fix `[v]`, Naturalist `[v]` | Rule for every vanilla touch |
| B21.8 | Paleo-Illagers in raids and patrols | Defer | 1 | S | Illager Invasion enum extension `[v]` | Section 11.3 is silent |
| B21.9 | Dialogue NPCs | Drop | 0 | L | Cobblemon, MCA `[v]` | A trade screen covers Beta |

### B22 Vehicles

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B22.1 | The driver's client runs vehicle physics. Other sides interpolate. The server clamps speed to the TOML top speed | Adopt | 3 | M | Cobblemon riding state `[v]`, vanilla rideable pattern | Plan packet rule. Cobblemon trusts the client `[v]` |
| B22.2 | Driver keys travel in one checked packet (the sender must be a passenger) | Adopt | 3 | S | AC key packet `[v]` | Plan packet rule |
| B22.3 | Jeep handling as TOML values: top speed, acceleration time, turn rate, reverse at a third, collision speed cap, half speed in tight turns | Adopt | 2 | S | Cobblemon land behaviour `[v]` | Proven feel |
| B22.4 | Boat-style controls: A and D turn, the mouse looks | Adapt | 2 | S | Cobblemon vehicle style `[doc]` | Matches vanilla boats |
| B22.5 | Seats at model locators. A development command computes the offsets | Adopt | 2 | S | Cobblemon seats `[v]` | No hand-tuned offsets |
| B22.6 | Placement item with a raycast and a hull collision check | Adopt | 3 | S | AC submarine item `[v]` | No vehicle stuck in a wall |
| B22.7 | Explorer: server-side movement along track blocks at constant speed, with stops | Adapt | 3 | L | AC mag-lev rail follower `[v]` | The only track-following code found |
| B22.8 | Animation-only values stay on the client. Tracking interval 1 only for vehicles | Adopt | 2 | S | AC per-tick synced fields as the anti-pattern `[v]` | Smooth vehicles, cheap herds |
| B22.9 | Longer render distance while a vehicle carries passengers | Adopt | 2 | S | Cobblemon `[v]` | A passenger never floats alone |
| B22.10 | Vehicle damage stages and repair | Defer | 1 | S | AC submarine `[v]` | Section 11.12 is silent |

### B23 Guns (blocked on decision D08)

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B23.1 | The server resolves each projectile | Defer | 0 | — | AC sea staff `[v]` | D08 |
| B23.2 | Ammunition and charge in a data component that only the server writes | Defer | 0 | — | AC raygun charge trusted from the client (anti-pattern) `[v]` | D08 |
| B23.3 | Tranquilised state: a timer that stops movement and behaviours | Defer | 0 | — | AC stun effect `[v]`, Illager Invasion stun 60 ticks `[v]` | D08 and the missing `tranqed` clip |
| B23.4 | Capture: the item stores the dinosaur's data and places it again later | Defer | 0 | — | IaF CE Dragon Horn `[v]` | D08 |

### B24 Art integration

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B24.1 | Artist checklist: box UV, whole-number cubes, root bone pivot at 0, 0, 0, a named head bone, clip names | Adopt | 2 | S | Cobblemon asset rules `[doc]` | Prevents broken imports |
| B24.2 | Glow layers from a `_glowmask.png` | Adopt | 2 | S | GeckoLib 5 `AutoGlowingGeoLayer` `[v]` | B10 bioluminescence with no code |
| B24.3 | Sound cues from animation keyframes | Adopt | 2 | S | GeckoLib 5 keyframe events `[v]`, Naturalist client cues `[v]` | Footsteps in time with clips |
| B24.4 | A species page with a stat box (health, attack, hitbox, speed, groups) | Adopt | 2 | S | Naturalist wiki `[doc]` | Documentation template |
| B24.5 | Variant textures by path in resource packs, one data line per variant | Adopt | 3 | S | GeckoLib resource loader `[v]`, Naturalist variant registry `[v]` | B09 skin variants |

### B25 Cleanup and performance pass

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B25.1 | An interval table for slow work, offset by entity id | Adopt | 3 | S | MCA intervals `[v]`, vanilla Darkness id offset `[v]` | Spreads the load |
| B25.2 | Profile with SBL profiler sections and logged herd timings | Adopt | 3 | S | SBL `[v]` | The plan asks for a profile |
| B25.3 | A client config for low effects: particles, screen shake, glow | Adopt | 2 | S | AC client switches `[v]` | Low-end players |
| B25.4 | Per-level maps cleared on level unload | Adopt | 3 | S | AM level leak, issue 2165 `[doc]` | Group manager, scent store, feeder registry |
| B25.5 | No tick mixin on a vanilla base class and no global per-entity hook | Adopt | 3 | — | AC `EntityMixin` `[v]`, Citadel tick gate `[v]` | Rule |
| B25.6 | Bursts spread over ticks, a few units per tick | Adopt | 2 | S | Mutant Monsters waves `[v]` | Leaf breaking, carcass drops, fence cascades |
| B25.7 | A unit test that parses every shipped JSON file | Adopt | 3 | S | Cobblemon `SpawnValidator` `[v]` | B00 acceptance |
| B25.8 | GameTests in common code with one registration class per loader | Adopt | 3 | M | FAF tests `[v]` | B26 and B27 acceptance need it |
| B25.9 | With no player within 128 blocks, idle journeys and graze moves run less often. Needs keep ticking | Adapt | 2 | M | Mowzie dormant bosses `[v]`, Vanilla Refresh distance gates `[v]` | Parks keep running without observers |
| B25.10 | Pathfinding on a thread pool | Defer | 2 | L | Citadel, AC 5 threads `[v]` | Measure first |

### B26 Large-body movement and herd travel

The hitbox design decides the movement work. These rows add only what the design does not cover.

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B26.1 | View cones for separation and cohesion as steering terms: ignore mates behind for cohesion | Adapt | 2 | S | boids cones 70, 100, 70 degrees `[v]`, cosy-critters `[v]` | Stops trailing dinosaurs being pulled back |
| B26.2 | Herd allocation fixes: one roster, primitive returns, one `CollisionContext` per probe batch, an index map for the gather | Adopt | 3 | S | Local herd code `[v]` | Tens of KB of garbage per tick at 100 (S11) |
| B26.3 | Unreachable destinations in a short memory | Adopt | 2 | S | FAR avoid cache `[v]` | Fewer repeat paths. Complements the stuck ladder |
| B26.4 | Velocity-driven flocking that cancels the AI | Drop | 0 | — | boids `[v]` | Fights navigation |
| B26.5 | Followers copy the leader's walk target at random | Drop | 0 | — | Cobblemon 1 in 60 per tick `[v]` | The lead decided formation travel |

### B27 Section hitboxes and IK

| Id | Technique | Verdict | Fit | Effort | Precedent | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| B27.1 | Spawn data carries the pose state, so a player who arrives late sees the clip at the right time | Adopt | 3 | S | Mutant Monsters spawn data `[v]`, Aquamirae clip start `[v]` | The decided pose state needs a join path |
| B27.2 | The client clip follows a GeckoLib 5 triggered animation that starts at the right offset | Adopt | 3 | S | GeckoLib 5 trigger offset `[v]` | No per-tick packets |
| B27.3 | Hits that a dinosaur deals: one hit tick per attack clip with a distance and cone test. Parts only receive hits | Adopt | 2 | S | Mutant Monsters `[v]`, Mowzie melee ability `[v]` | Clear split of roles |
| B27.4 | The neck and tail bend as an added bone-updater delta | Adopt | 3 | S | GeckoLib 5 updater order `[v]` | Answers hitbox risk R3 at code level |
| B27.5 | Citadel leg solver | Drop | 0 | — | Citadel `[v]` | The lead decided the CHAOS branch IK |

## No reference mod implements twelve Beta features

**In game:** these twelve features set Nublar apart from other creature mods. The survey found no complete precedent for any of them `[v]` `[doc]`. Each entry names the nearest piece and gives one design direction. Every direction uses only vanilla classes, SBL 2, GeckoLib 5 and code that Nublar already has. Values marked `[inf]` are starting proposals for playtest, not findings.

| # | Feature | Module | Nearest piece | Effort |
| --- | --- | --- | --- | --- |
| 1 | Sight-cone and distance-chance detection | B19 | Vanilla target selection, `Sensing`, Enderman look cone | M |
| 2 | Scent trails | B19 | Sniffer dig-spot list, Warden sniff, memories with expiry | M |
| 3 | Stamina-limited chases | B17 | SBL `InvalidateAttackTarget.invalidateIf`, Cobblemon ride stamina | S |
| 4 | Dominance contests | B18 | Local `Dominance` and `ChallengeGroupLeader`, Polar Bear warning stance | S |
| 5 | Electric fences with wire health | B15 | rft `FencePowerService`, AC Tesla Bulb | M |
| 6 | Track-following tour vehicle | B22 | AC mag-lev rail follower | L |
| 7 | Driverless tours | B22 | Vanilla minecart on rails | M |
| 8 | Tranquilizer and capture guns | B23 | AC stun effect, Illager Invasion stun, IaF CE Dragon Horn | Blocked by D08 |
| 9 | Carcasses that scavengers eat | B16 | IaF CE staged dragon corpse, Mutant Monsters death clip | M |
| 10 | Live-bait feeders | B20 | Vanilla leads, FAR laser lure, FAF Tuff Golem lure | M |
| 11 | Thirst | B17 | SBL gated block sensor and forgettable memory | S |
| 12 | Pack flanking | B18 | AC Elder target push, vanilla Vex, local formation slots | M |

### 1. Sight-cone and distance-chance detection (B19)

**Nearest piece.** Vanilla target selection multiplies the follow range by a visibility factor. Sneaking gives 0.8, and the range never falls below 2 blocks `[v]`. `Sensing` caches one line-of-sight result per target per tick `[v]`. The Enderman tests a narrow look cone `[v]`. Vanilla never rolls a random chance to detect `[v]`. The 25 to 85 % roll is a Nublar addition.

**Design direction.** `DetectionService.chanceToDetect(animal, player)` is one pure function with unit tests. It divides the distance by the player's visibility factor. Then it maps the result linearly from the near chance to the far chance over the species follow range. A sight sensor runs every 20 ticks over players inside the follow range. It tests the species view cone first, because a dot product costs almost nothing. Then it asks `Sensing` for the cached line of sight. A player who passes both tests for the first time in an episode gets one roll. A pass writes `DETECTED_PLAYER`. A fail writes `UNNOTICED_PLAYER`. Both memories expire after the episode length (open item O1). The sensor skips a player while either memory exists. The plan excludes movement-based vision, so a still player and a moving player with the same visibility factor get the same chance. Hyper intelligence and thermal detection (B10) change the inputs, not the pipeline.

| Value | Start | Basis |
| --- | --- | --- |
| Chance at distance 0 | 85 % | Plan B19 `[doc]` |
| Chance at follow range | 25 % | Plan B19 `[doc]` |
| Visibility factor: sneaking, walking, sprinting | 0.8, 1.0, 1.5 | Vanilla sneak factor `[v]`. Sprint factor `[inf]` |
| Minimum distance | 2 blocks | Vanilla range floor `[v]` |
| Sensor rate | 20 ticks | Vanilla `Sensor` default `[v]` |
| View cone half-angle | 60° predators, 120° prey, in `SpeciesData` | `[inf]` |
| Episode length | 200 ticks (10 s) | Open item O1 `[inf]` |
| Check: sneaking at 30 blocks, sprinting at 10 blocks, follow range 35 | 25 % and about 74 % | Arithmetic `[inf]`. Meets the plan acceptance test |

### 2. Scent trails (B19)

**Nearest piece.** Vanilla has no scent trail and no fading world data `[v]`. The Sniffer keeps a list of its last 20 dig spots `[v]`. The Warden sniff picks the nearest target in a radius, through walls `[v]`. The Warden walks to a remembered disturbance at 0.7 speed while the memory lasts `[v]`.

**Design direction.** `ScentTrail` is `SavedData` per level, keyed by chunk position. Each entry holds a position, the source UUID, the source kind and an expiry game time. A player drops a point after moving a set distance from the last point. A hurt mob drops points the same way until it heals (open item O2). Each chunk keeps a capped list and removes the oldest point first. A cleanup pass removes expired points from a few chunks per tick (B25.6). A scent sensor runs only for species with a scent sense, and only in the hunt or alert activity. It reads the chunks inside its range and needs no line of sight. It picks the newest point of the best target and writes `SCENT_TARGET` with a short expiry. A walk behaviour steps from older points to newer points, as the Warden steps to a disturbance. The store saves only points that have not expired. It clears on level unload (B25.4).

| Value | Start | Basis |
| --- | --- | --- |
| Point lifetime | `needs.scent_lifetime_seconds`, proposal 60 s | Plan key `[doc]`, value `[inf]` |
| Drop spacing | 2 blocks, at most one point per 20 ticks | `[inf]` |
| Points kept per source | 20 | Sniffer dig-spot list `[v]` |
| Points per chunk | 32 | `[inf]` |
| Sensor rate and range | 40 ticks, 32 blocks | `[inf]` |
| `SCENT_TARGET` expiry | 100 ticks | Warden disturbance memory `[v]` |
| Follow speed | 0.7 | Warden investigate speed `[v]` |

### 3. Stamina-limited chases (B17)

**Nearest piece.** SBL `InvalidateAttackTarget.invalidateIf` ends an attack on any condition `[v]`. Cobblemon drains stamina during a ridden sprint and allows the next sprint above a third of full stamina `[v]`. Naturalist uses a 2,400-tick hunt cooldown `[v]`. RevampedWolf sets a hunt cooldown that never counts down, so each wolf hunts once per world `[v]`.

**Design direction.** Stamina lives in `NeedsState`. The running state comes from the actual speed each tick (plan B17). While the dinosaur runs, stamina falls at the species run rate. At other times it recovers. The hunt activity adds `invalidateIf(stamina <= 0)`. When it fires, the predator drops its target, writes `HUNT_COOLDOWN` with an expiry of `needs.hunt_cooldown_seconds`, and switches to rest. A new hunt starts only after the cooldown and above the resume fraction. Prey escapes by keeping distance until the predator tires. Fix the stamina maximum of 0 first `[v]`, or every chase ends at once. The plan acceptance test and its playtest note stay as written.

| Value | Start | Basis |
| --- | --- | --- |
| Resume fraction | 0.33 of full stamina | Cobblemon sprint re-enable `[v]` |
| Hunt cooldown | `needs.hunt_cooldown_seconds`, proposal 120 s | Naturalist 2,400 ticks `[v]` |
| Run drain and recovery | Per species in `SpeciesData` | Plan B00 `[doc]` |

### 4. Dominance contests (B18)

**Nearest piece.** The local herd code computes dominance from age, size and aggression, and has `ChallengeGroupLeader` with a cooldown `[v]`. AC makes the first raptor of a natural group an Elder with a stat bonus and never elects again `[v]`. Cobblemon herds pick the leader with the highest tier `[v]`. The Polar Bear warning stance is the nearest display with no damage `[v]`.

**Design direction.** Keep the local `Dominance` value and the challenge behaviour. A challenge starts when a member's dominance passes the leader's value by the margin and the cooldown has ended. Both animals face each other and play `ThreatDisplay` (B19.5) for the species display time. No animal takes damage. The higher dominance wins, and a tie goes to the current leader. The loser gets a challenge cooldown memory. The leader then chooses where the group goes, as the plan decides. Dominance comes from saved values, so a world reload elects the same leader. Groups need no save.

| Value | Start | Basis |
| --- | --- | --- |
| Weights: age, size, aggression gene | 0.5, 0.3, 0.2 | Local `Dominance` `[v]` |
| Challenge cooldown | 1,200 to 2,400 ticks | Local `ChallengeGroupLeader` `[v]` |
| Challenge margin | 10 % of the leader's value | `[inf]` |

### 5. Electric fences with wire health (B15)

**Nearest piece.** The rft fence rework keeps `FencePowerService`, connection dedupe and cascade breaks (plan B15). The AC Tesla Bulb shocks living entities in a small cube from a powered block, with a visual-only lightning bolt of damage 1 `[v]`. No surveyed mod gives a fence wire its own health `[v]` `[doc]`. FAR uses block hardness tiers instead (B15.6, dropped).

**Design direction.** Each connection keeps its health in the post block entity that owns it after rft dedupe. A dinosaur hit from `BreakFenceBehaviour` subtracts the dinosaur's attack damage. At zero health the connection breaks. Both halves render broken, sparks play, and the section's cached powered flag updates (B14.3). The spool restores one connection to full health per use. The client receives only a damage band per connection, sent on change. Shock damage and the deterrent roll read the cached powered flag (B15.1). The spark effect is a client particle burst, not a lightning entity.

| Value | Start | Basis |
| --- | --- | --- |
| Wire health per tier | `fences.wire_health.<tier>` | Plan key `[doc]` |
| Shock damage per tier | `fences.damage.<tier>` | Plan key `[doc]` |
| Damage bands sent to the client | Full, damaged (under 50 %), broken | `[inf]` |

### 6. Track-following tour vehicle (B22)

**Nearest piece.** The AC mag-lev rail cancels the vanilla minecart tick on its rail and runs a copy of the vanilla rail follower `[v]`. It clamps speed to 2.0 along the rail axis `[v]`. It reads the rail again only when the cart's block position changes `[v]`. AC keeps vanilla `BaseRailBlock` rules instead of a new track system `[v]`. AC has no routes, stops or driverless travel `[v]`. Cobblemon has no track movement `[doc]`.

**Design direction.** `TrackBlock` is a flat block with the vanilla `RailShape` property, so straights, curves and slopes place like rails. The Explorer reads the track under it only when its block position changes. On a track, the server moves the Explorer along the track direction at a constant speed, with no physics. The body turns toward the next track block. Clients interpolate the synced position. A `TourStopBlock` under the track starts a stop timer of `vehicles.stop_seconds`. At a track end the Explorer stops. Off the track it is a normal driven vehicle (B22.1). Junctions stay out of scope, because the plan excludes schedules beyond a loop with stops.

| Value | Start | Basis |
| --- | --- | --- |
| Track speed | 0.25 blocks per tick (5 blocks per second) | `[inf]` |
| Stop time | `vehicles.stop_seconds` | Plan key `[doc]` |
| Track read | On a block-position change only | AC cache `[v]` |

### 7. Driverless tours (B22)

**Nearest piece.** No surveyed mod runs a vehicle with no driver `[v]` `[doc]`. The vanilla minecart on powered rails is the closest feel. It moves on the server with no passenger.

**Design direction.** Driverless mode is a state of the Explorer, not a second entity. The driver starts it with a key while the Explorer is on a track. Then the Explorer runs on the server with or without a driver, and passengers can ride. A solid block or an entity in front pauses the Explorer until the way is clear. The Explorer forces no chunks. When its chunk stops ticking, the tour pauses, as every vanilla entity does. The mode flag is one byte, synced on change.

| Value | Start | Basis |
| --- | --- | --- |
| Blocked check | Every 10 ticks | `[inf]` |
| Chunk forcing | None | Vanilla entity rules `[v]` |

### 8. Tranquilizer and capture guns (B23, blocked by D08)

**Nearest piece.** The AC Tremorsaurus stun effect zeroes movement, turns off goal flags and cancels attacks `[v]`. Illager Invasion stuns a Basher for 60 ticks `[v]`. The IaF CE Dragon Horn stores a whole dragon as entity data in an item and places it again later `[v]`. The AC raygun trusts the client for its charge, which is the pattern to avoid `[v]`.

**Design direction, for use only after D08.** The plan keeps B23 closed. If D08 includes a tranquilizer, a dart is a server projectile. A hit adds tranquilizer points, scaled down by body mass. Above the species threshold, a `TRANQUILISED` timer stops movement and behaviours and plays the `tranqed` clip (missing art, B24). The points decay over time. Capture stores the dinosaur through vanilla entity save in an item, and placement uses the free-space check of B11.3. Ammunition sits in a data component that only the server writes.

### 9. Carcasses that scavengers eat (B16)

**Nearest piece.** IaF CE keeps a dead dragon as a corpse. Each use with an empty hand advances a death stage and drops loot, and the last stage removes the body `[v]`. Mutant Monsters plays a death clip that the server owns and sends it again after a chunk reload `[v]`. No surveyed mod has a carcass that scavengers eat `[v]`.

**Design direction.** `CarcassEntity` (plan B16) is a plain entity with no AI, sized from the dead dinosaur's box. It stores the species, the growth fraction and a meat pool. Each player use takes one stage and drops one share of the decided drops. Each scavenger bite (B17.9) removes meat from the pool. The carcass decays when the pool is empty or `needs.carcass_minutes` ends. The renderer draws the species model in the `dead` pose where that clip exists. Drops spread over ticks (B25.6).

| Value | Start | Basis |
| --- | --- | --- |
| Use stages | 4 | `[inf]` |
| Meat pool | Species hunger value times growth fraction | `[inf]` |
| Lifetime | `needs.carcass_minutes` | Plan key `[doc]` |

### 10. Live-bait feeders (B20)

**Nearest piece.** No surveyed mod holds a live animal for a predator `[v]` `[doc]`. FAR carnivores chase a laser-pointer point that the player places `[v]`. The FAF Tuff Golem holds an item and attracts mobs as a player does `[doc]`. Vanilla Refresh tags foreign mobs and never edits them `[v]`.

**Design direction.** The live-bait feeder is a block with a pen above it. A player leads a vanilla animal into the pen and uses the feeder. The feeder tags the animal, makes it persistent and keeps it on a vanilla leash to the feeder. It does not edit the animal's goals. The feeder joins the feeder registry (B17.1). When a hungry carnivore comes within range, the feeder drops the leash and opens. The prey pass (B17.7) ranks tagged bait first. A kill of tagged bait lowers stress (plan B17).

| Value | Start | Basis |
| --- | --- | --- |
| Release range | 16 blocks | `[inf]` |
| Tag | `projectnublar:bait` | `[inf]` |

### 11. Thirst (B17)

**Nearest piece.** Vanilla mobs have no thirst `[v]`. FAR and IaF CE have no thirst `[v]`. SBL `NearbyBlocksSensor` with `onlyScanIf` and a forgettable memory covers the water search `[v]`.

**Design direction.** Thirst falls at the species rate. Below the seek threshold the dinosaur sets `IS_THIRSTY`. The water sensor (B17.2) runs only while that flag exists. It stores the found shore block in `HAS_FOUND_WATER` with an expiry. The dinosaur walks to the shore block, plays the drink clip and refills thirst. Then the need cooldown starts (B17.4). If a scan finds no water, the next scan grows the radius by one step, up to the follow range. A found source resets the radius. The plan's Drink distance fix uses the same close-enough distance as the walk.

| Value | Start | Basis |
| --- | --- | --- |
| Base scan | Radius 8 by 2, every 60 ticks | `[inf]`, about 24 reads per tick per thirsty dinosaur |
| Radius step on a failed scan | 8 blocks | `[inf]` |
| `HAS_FOUND_WATER` expiry | 6,000 ticks (5 min) | `[inf]` |

### 12. Pack flanking (B18)

**Nearest piece.** The AC Elder raptor pushes its target to every member every tick `[v]`. A vanilla Vex copies its owner's target `[v]`. The local herd code assigns formation slots for travel in `TravelOrder` `[v]`. No surveyed mod spreads pack members around prey `[v]`.

**Design direction.** The leader picks the target and writes it once to the members (B18.1). Each member gets a slot angle around the target from its index in the pack. The leader approaches straight, and the others take the side and rear slots. A member walks to the target position plus its slot offset at the flank radius. All members close in when every member reaches its slot or the timeout ends. Slots update on the 20-tick sensor step, not every tick.

| Value | Start | Basis |
| --- | --- | --- |
| Slot angles | 0° leader, then ±60°, ±120°, 180° | `[inf]` |
| Flank radius | 6 blocks | `[inf]` |
| Timeout | 60 ticks (3 s) | `[inf]` |

## The plan covers most fixes, and 32 items are new

**In game:** nothing here changes a decided feature. The new items make decided features cheaper or remove a defect that the plan does not name.

### What the plan already covers

| Module | The plan already asks for | The survey adds |
| --- | --- | --- |
| P2 | GeckoLib 5 and SBL 2 upgrades, `IncubatorMenu` client call moved | SBL ticks the brain itself `[v]`. GeckoLib 5 applies the scale attribute itself `[v]` |
| B09 | Layers per entity, no white pass for white tints, culling box from scaled size | The bake spec `[doc]`, EMF and AzureLib LOD `[v]` |
| B11 | Egg block (D05 default), wobble, growth 0 at hatch, packet validation | AC egg stages `[v]`, FAR free-space check `[doc]` |
| B12 | Assembly from parts, tabs, zero idle draw | FAR edge-only state flip `[v]` |
| B14 | Real power, `FencePowerService` draw | Update-on-change rules for the generator and posts `[v]` |
| B15 | Wire health, deterrent, switch, rft rework | Cached boxes and gated sensor `[v]`, SBL unreachable-target pair `[v]` |
| B16 | `CREATURE` with persistence, carcass, babies follow, debug status | FAR and IaF CE confirm `CREATURE` `[v]`, IaF CE corpse stages `[v]` |
| B17 | Sensor cost cap, need cooldown, stamina chases, activities, diets as tags, grazing reach | Registry and gated sensors `[v]`, AC height bands `[v]`, FAF sleep cooldown `[v]` |
| B18 | Dominance, pack hunting, nests, breeding gates, calls | Naturalist herd panic and lay goal `[v]`, Polar Bear protect-young `[v]` |
| B19 | Threat display, flee or stand, territory, taming, detection, `ScentTrail` | Warden and Allay listeners `[v]`, vanilla visibility math `[v]`, FAR commands `[v]` |
| B20 | Three feeders, hopper input, range fix, fill from outside | AFT container and goal re-check `[v]`, FAR point pools `[v]` |
| B21 | Jigsaw digsites, profession, illager variant, maps | Illager Invasion structure data `[v]`, MCA profession and trade data `[v]` |
| B22 | Jeep, Explorer, track, stops, driverless loop | Cobblemon driver-client physics `[v]`, AC key packet and placement `[v]` |
| B25 | Profile, dead-code removal, `TODO(BUG)` cleanup | Interval table, unload cleanup, JSON parse test |
| B26 | Block filter, arrival test, turn gates, stuck ladder, capsules | boids view cones `[v]`, herd allocation fixes `[v]` |
| B27 | Pose state, parts, bend, IK | Spawn data with pose `[v]`, GeckoLib 5 updater order `[v]` |

### New items for the plan

| # | Module | Addition | From |
| --- | --- | --- | --- |
| 1 | P2 | Let SBL 2 tick the brain. Delete the manual brain tick. Flip the `PredicateSensor` argument order in the three custom sensors | Rank 1 |
| 2 | P2 | Move procedural bone writes (head look, sway) to GeckoLib 5 bone updaters, which run after the controllers | GeckoLib 5 `[v]` |
| 3 | P2 | Use `setAnimationSpeed`, because GeckoLib 5 removed the speed handler. Remove manual entity scaling, because GeckoLib 5 applies the scale attribute | GeckoLib 5 `[v]` |
| 4 | B09 | Add the bake spec to the module scope | Rank 3, C1 |
| 5 | B09 | Until the bake lands, cache the layer colour and index per entity and remove per-frame `Stream` and `Color` allocations | C1 |
| 6 | B09 | Hold the last pose beyond 32 blocks and skip animation frames by distance and body size | Rank 7, C3 |
| 7 | B12 | `ContainerData` progress, block updates on change, edge-only ACTIVE flip | B12.1 |
| 8 | B12 | Recipe cache cleared on reload | B12.2 |
| 9 | B12 | One master block entity per multi-block machine | B12.3 |
| 10 | B12 | Comparator output clamped to 15 on machines and feeders | B12.4, B20.5 |
| 11 | B12 | Machine renderer: hidden-bone set built on change, normal frustum test | B12.5 |
| 12 | B14 | Generator and post updates on change only. One cached powered flag per network section | B14.1 to B14.3 |
| 13 | B15 | Cached wire boxes, gated fence sensor, wire renderers with no stream | B15.1, B15.2, B15.4 |
| 14 | B15 | Player guide: escape alarms from vanilla sculk sensors | B15.5 |
| 15 | B16 | Sync only render data. Keep the 12 need floats on the server | B16.6 |
| 16 | B16 | Cache the `SpeciesData` reference. Write attributes on change only | B16.2 |
| 17 | B17 | Feeder registry, water sensor that runs only while thirsty, sensor start offsets | B17.1 to B17.3 |
| 18 | B17 | Prey picked by one scored pass per scan, enclosure test cached per target | B17.7 |
| 19 | B17 | Per-level path budget, unreachable-food list | B17.11, B17.12 |
| 20 | B18 | Leader target push on change, herd panic timer, `JoinGroup` reads the shared memory | B18.1 to B18.3 |
| 21 | B18 | Protect-the-young search order: baby first, then players at half range | B18.6 |
| 22 | B18 | Flank slots around prey | Feature 12 above |
| 23 | B19 | Hearing through a `VibrationSystem` listener | B19.2 |
| 24 | B19 | One roll per awareness episode | B19.3 |
| 25 | B19 | Reputation as one capped integer per player with daily decay | B19.9 |
| 26 | B20 | Point pools per diet, fill block state, eat range from stop distance and width | B20.2, B20.4, B20.7 |
| 27 | B21 | Inject pools for vanilla loot. Village house added at server start behind a config value | B21.3, B21.6 |
| 28 | B22 | Server speed clamp, tracking interval 1 for vehicles only, longer render distance with passengers | B22.1, B22.8, B22.9 |
| 29 | B25 | Interval table with id offsets, per-level maps cleared on unload, JSON parse test, low-effects client config | B25.1, B25.3, B25.4, B25.7 |
| 30 | B25 | GameTests in common for every module (B26 already asks for it) | B25.8 |
| 31 | B26 | Herd allocation fixes and a view-cone cohesion term | B26.1, B26.2 |
| 32 | B27 | Spawn data carries the pose state. Dealt hits use one hit tick with a cone test | B27.1, B27.3 |

## Repository facts

Each row records what the notes recorded at the commit read. "Not recorded" means the notes did not record the fact.

### Repositories new in this survey

| Short name | Repository | Licence as recorded | Branch @ commit read | Minecraft | Loaders | 26.x status | Libraries |
| --- | --- | --- | --- | --- | --- | --- | --- |
| FAF | Faboslav/friends-and-foes | CC-BY-NC-ND-4.0 | master @184a7c3 (2026-10-09) v5.0.2. 1.20.1 @5cf4194 v3.0.9 | 1.20.1 to 26.3 | Fabric (all), Forge (1.20.1), NeoForge (1.21.1 and newer) | 26.1.2, 26.2 and 26.3 on master | Resourceful Lib, YACL (optional), ModMenu, MixinExtras, Stonecutter 0.9.5 |
| Naturalist | crispytwig/Naturalist | MIT for code. Resource folders All Rights Reserved. Modrinth lists Custom | 26.2-MultiLoader @26d1bf8 (2026-09-28). 1.20.1-Arch @147b8f8 (2026-08-12). 1.21.1-NeoForge @43028bca (earlier note) | 26.2 | Fabric, NeoForge (26.2). Fabric, Forge (1.20.1) | 26.2 branch | Fabric API 0.160.0, NeoForge 26.2.0.87, MixinExtras 0.5.3. No GeckoLib on 26.2. GeckoLib 4.7, MidnightLib, Cloth Config on 1.20.1 |
| AFT | Slexom/animal-feeding-trough | LICENSE file and `fabric.mod.json`: CC0-1.0. `neoforge.mods.toml`, CurseForge, Modrinth and README: MIT | 26.2 @ad13f16 (2026-07-11) v1.2.1. 1.20.1 @86392c7 v1.1.0 | 26.2 | Fabric, NeoForge (26.2). Fabric, Forge, Quilt (1.20.1) | 26.2 branch | Architectury Loom with no Architectury API (26.2), Fabric API 0.152.1, NeoForge 26.2.0.2-beta. Architectury API on 1.20.1 |
| TES | Tslat/TslatEntityStatus | MPL-2.0 | 26.2 @ace1893 v1.10.2. 1.20 @2e87225 | 26.2 | Fabric, Forge, NeoForge | 26.2 branch | Forge Config API Port |
| GV | seymourimadeit/guardvillagers | LICENSE file: resources All Rights Reserved. `gradle.properties`: code MIT, assets ARR. No MIT text file | 26.1.0 @9074a04 (2026-06-19). 1.20.1 @b067d53 (2026-08-30) | 26.1 (branch read) | NeoForge (26.1.0). Forge (1.20.1). Fabric port by a third party | Store pages list 26.2 and 26.3 `[doc]` | Not recorded |
| SBL | Tslat/SmartBrainLib | MPL-2.0 | 26.2 @437898f (2026-10-05) v2.0.3. 1.20 @b5c21fe v1.15 | 26.2 and newer | Fabric, Forge, NeoForge | 2.0.3 for 26.1.2, 26.2 and 26.3 | None. Two common mixins |
| Mutant Monsters | Fuzss/mutant-monsters | AGPL-3.0-or-later (`gradle.properties`, LICENSE.md). Assets ARR. Modrinth lists AGPL-3.0-only | 26.2.x @0404e1d (2026-09-14) v26.2.2. 1.20.1 @9e12427 v8.0.8. @b1f3fb60 (earlier note) | 1.18.2 to 26.3 branches | Fabric, NeoForge (26.2) | 26.2.x branch | Puzzles Lib, Fabric API, Forge Config API Port (Fabric), MixinExtras |
| MCA | Luke100000/minecraft-comes-alive | GPL-3.0 | 26.2 @2de0738 (2026-09-13). 1.20.1 @4d82455 (2026-07-24) | 26.2 | Fabric, NeoForge (26.2) | 26.2 branch. Wiki lists the port as in progress | MixinExtras. No Architectury on 26.2 |
| boids | Tomate0613/boids | MIT | 26.2 @dbfd724 v2.0.0+26.2. __1.20.1 @0f50dca v1.2.1 | 26.2, 26.3 | Fabric | 26.2 branch | Jankson, MixinExtras |
| BV | jtorleon-studios-team/better-villages | All Rights Reserved | main @d1ea77d (2026-09-08). Tag 26.2-4.0.0 @a56e017. No source in the repository | 1.16.5 to 26.2 (Modrinth) | Fabric, Forge, NeoForge (server side) | 26.2 release on Modrinth | Library Ferret (ARR, no source) |
| RevampedWolf | baguchi/RevampedWolf | LGPL-2.1-only | 26.2.x @9b8f0a6 (2026-06-17). 1.20.1 @aa351fd. 1.21.x @a783669 | 26.2 | NeoForge (26.2.x). Forge (1.20.1) | 26.2.x branch | NeoForge 26.2.0.0-beta, ModDevGradle |
| skeleton-ai-fix | Fuzss/skeleton-ai-fix | MPL-2.0 for code. Assets ARR | 26.2.x @7dac096 v26.2.0. 1.20.1 @f54d7fa | 26.2 | Fabric, NeoForge | 26.2.x branch | Puzzles Lib, Forge Config API Port (Fabric). No mixins |
| Vanilla Refresh | limesplatus/Vanilla-Refresh | No LICENSE file. `fabric.mod.json`: LicenseRef-Custom. Modrinth: Custom (no republishing, no modified copies) | main @1530a81 (2026-07-09) v1.4.31 | 1.18.x to 26.2 (Modrinth) | Data pack wrapped for Fabric, Quilt, Forge, NeoForge | 26.2 on Modrinth. Pack format 71 with a 26.1 overlay | None. 1,067 function files, no Java |
| cosy-critters | PigCart/cosy-critters | MIT | main @1403312 (2026-07-08) v0.3.3 | 1.20.1 to 26.2 | Fabric (all), Forge (1.20.1), NeoForge (1.21.1, 1.21.4) | 26.2 on Fabric | Fabric API, ModMenu. Client side only |
| Aquamirae | ObscuriaLithium/aquamirae | Obscuria Modding License (OML) v1.3 | 1.21.1 @a468d77 (2026-09-08) v7.2.7 | 1.21.1 | Fabric, NeoForge. Forge (1.20.1, legacy) | No 26.x branch. Newest Modrinth file 7.2.10 for 1.21.1 | GeckoLib 4.8, Fragmentum API 2.4.4, YACL 3.6.6, mclib, Lombok, MixinExtras |
| Illager Invasion | Fuzss/illager-invasion | No LICENSE file. `gradle.properties` and `metadata.json`: MIT | 26.2.x @fd20e59 (2026-09-17). 1.20.1 @ee2743e (2025-07-03) | 26.2 | Fabric, NeoForge | 26.2.x branch | Puzzles Lib, Forge Config API Port (Fabric), Multiloader Data Extensions |
| Cobblemon | gitlab.com/cable-mc/cobblemon | MPL-2.0 | main @2780f491 (2026-10-10) v1.8.2 snapshot. 1.21.1-breeding @5a84b4b4 (2026-10-05) | 1.21.1 | Fabric, NeoForge | No 26.2 branch. feature/version-bump targets 26.1.2 | architectury-loom (not Architectury API), MoLang 1.1.20, GraalJS, MongoDB driver, Kotlin 2.2.20, KotlinForForge |
| EMF | Traben-0/Entity_Model_Features | LGPL-3.0-or-later | master @622a0cba (2026-10-05) v3.3.11 | 1.20.1, 1.21.x, 26.1 to 26.3 | Fabric, Forge (to 1.21.11). Fabric, NeoForge (26.x) | 3.3.11 for 26.2 | ETF 7.2.2 (required), Iris API, MixinExtras |

### Repositories read again in this survey

| Short name | Repository | Licence as recorded | Branch @ commit read | Minecraft | Loaders | 26.x status | Libraries |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AC | AlexModGuy/AlexsCaves | No LICENSE file. `gradle.properties` `mod_license`: GNU LGPL, no version | 1.21.1 @0ab3c214 (2026-04-26) v2.0.2. 1.20.1 code on main | 1.21.1 | NeoForge 21.1.86 | No 26.x branch | Citadel 2.7.0, JEI (optional) |
| AM | AlexModGuy/AlexsMobs | No LICENSE file. `mod_license`: GNU LGPL, no version | 1.21 @d5ec3a1 (2026-02-04). 1.20 @09755dad (earlier note) | 1.21.1 | NeoForge 21.1.86 | Not recorded | Citadel 2.7.0, JEI |
| Citadel | AlexModGuy/Citadel | No LICENSE file. `gradle.properties`: GNU LGPL, no version | 1.21 @badebf6 (2026-07-18) v2.7.1 | 1.21.1 | NeoForge 21.1.203 | No 26.x branch | Pathfinding code from MineColonies, used with permission |
| Mowzie | BobMowzie/MowziesMobs-Public | License.txt: All Rights Reserved unless otherwise stated, custom terms (credit, no direct sale) | 1.20 @64d1524 (2026-04-22) v1.8.2 | 1.20.1 | Forge | Not recorded | GeckoLib 4 |
| AzureLib | AzureDoom/AzureLib | MIT, plus LICENSE_Geckolib and LICENSE_Configuration files | 26.2 @ba4020b (2026-09-15) v4.0.0 | 26.2 | Fabric, NeoForge | 4.0.0 for 26.2 | Own configuration library. No AzureNavigation on 26.2 |
| GeckoLib 5 | bernie-g/geckolib | MIT | 26.2 @1e09655 (2026-09-16) v5.5.6 | 26.2 and newer | Fabric, NeoForge, Forge | 5.5.6 (NeoForge, Forge), 5.5.5 (Fabric) | None |
| GeckoLib 4 | bernie-g/geckolib | MIT | 1.20.1 @7d6f32d v4.8.4 | 1.20.1 | Fabric, Forge | GeckoLib 5 replaces it | None |
| FAR | TeamFossilsArcheology/FossilsArcheologyRevival | Code MIT, assets ARR (`mod_license`) | 1.20.1 @b400cf2 (2026-06-07) v9.3.4.0 | 1.20.1 | Fabric 0.16.7, Forge 47.4.3 | No 1.21 or 26.x branch | Architectury API 9.2.14, GeckoLib 4.7.2, More Hitboxes 1.9.2, TerraBlender, Team Reborn Energy (Fabric), MidnightLib (Fabric) |
| IaF CE | IAFEnvoy/IceAndFire-CE | LGPL-3.0-or-later | 1.21.1 @55c5dec (2026-09-20) v2.1.3. master targets 26.1.2 (v2.2-beta.3) | 1.21.1 | NeoForge 21.1.248. Fabric and Forge on the 1.20.1 branch `[doc]` | master on 26.1.2. No 26.2 | Uranus, Jupiter, Integration, Curios, GeckoLib 4.7.7 (compile only). No Citadel |

### Repositories from the earlier hitbox research only

| Short name | Repository | Licence as recorded | Branch @ commit read | Minecraft | Loaders | 26.x status | Libraries |
| --- | --- | --- | --- | --- | --- | --- | --- |
| IaF | AlexModGuy/Ice_and_Fire | Not recorded | 1.20 @0526aad (2025-09-05) | 1.20.1 | Not recorded | Not recorded | Citadel |
| MoreHitboxes | DarkPred/MoreHitboxes | MIT | 1.21.1 @88899b3e (2026-03-31) v1.9.4-alpha. 1.20.1 @012edbb4 v1.9.3 | 1.20.1, 1.21.1 | Forge (1.20.1). FAR uses it on Fabric and Forge | Not recorded | GeckoLib (optional), MixinExtras |
| CustomHitboxLib | gawrmonster/CustomHitboxLib | MIT | main @17585f5 (2026-10-06) v1.3.9. neoforge-1.21.1 v2.0.9 | 1.20.1, 1.21.1 | Forge 47.4.10, NeoForge 21.1.235 | Not recorded | Not recorded |
| Countered | Coun7ered/countereds-accurate-hitboxes-template-1.21.1 | Repository and `fabric.mod.json`: CC0-1.0. Modrinth: MIT | master @711b0b5 (2026-09-01) v1.0.1 | 1.21.1 | Fabric | Published 2.0.x lists 26.1.2, 26.2 and 26.3, with no linked source | Fabric API. The published 2.0.x needs Architectury API |
| Original Nublar | Dumb-Code/ProjectNublar | Not recorded | master @e525ed7b (1.12.2). 1.16 @32f3a45c. AIDevelopment1.16 @5332f898. wp/1.20.1-port @30f1eda8 | 1.12.2, 1.16 | Forge | None | DumbLibrary |
| DumbLibrary | Dumb-Code/DumbLibrary | Not recorded | master @791ada6a (1.12.2). 1.16 @dc5fa004 | 1.12.2, 1.16 | Forge | None | Not recorded |
| Twilight Forest | TeamTwilight/twilightforest | Not recorded | latest @28117eab (2026-10-09) v4.9 | 26.1.2 | NeoForge | 26.1.2 | Not recorded |
| Cataclysm | lender544/new1.20.1 | CC-BY-NC-ND-4.0 (Modrinth) | @81993720 (2026-10-04) | 1.20.1 | Forge | Not recorded | Not recorded |
| Wyrmroost | Shannieann/Wyrmroost. Old: MWall541/Wyrmroost | ARR (current) | master @a1f9934b. Old @4fdad083 | 1.19 to 1.20 era (master) | Forge | Not recorded | Not recorded |
| Untamed Wilds | RayTrace082/untamedwilds | Not recorded | 1.18.2 @4891218a (2023-10-15) | 1.18.2 | Forge | Not recorded | Not recorded |
| Jurassic Reborn | Interactive-Modding/Jurassic-Reborn | JurassiCraft-derived code LGPL-2.1, the rest ARR (README) | master @4a9a21b6 (1.12.2). 1.21.1 @a6bfa8fe (2026-03-29) | 1.12.2, 1.21.1 | Not recorded | Not recorded | Not recorded |
| Prehistoric Fauna | superlord9362/Prehistoric-Fauna | ARR (Modrinth) | 1.20.1 @e00d5241 (2026-09-07) | 1.20.1 | Not recorded | Not recorded | Not recorded |
| BOMD | barribob/bosses-of-mass-destruction | Not recorded | @2fbd0dc7 (2024-10-13) v1.10.2 | 1.21.1 | Fabric | Not recorded | MultipartEntities |
| MultipartEntities | barribob/MultipartEntities | MIT | @895e77a8 | 1.16.5 | Fabric | Not recorded | Not recorded |
| Iron's Spells | iron431/irons-spells-n-spellbooks | Not recorded | 1.21 @e4056af9 | 1.21.x | NeoForge | Not recorded | Not recorded |
| MultiHitboxLib | DerToaster, read from ZigyTheBird/MultiHitBoxLib | AGPL-3.0-or-later (moddex) | Copy @9b06d5cf (2023-11-11) v1.2.0 | 1.20.1 | Forge | Not recorded | Not recorded |
| Critters and Companions | bonsaistudi0s/CrittersAndCompanions | Not recorded | @2ff10719 | 1.21.1 | Not recorded | Not recorded | Not recorded |

## Clean-room implementation specifications

**In game:** each specification describes a behaviour that the player sees. It gives enough detail to build the behaviour without the source code.

These 27 specifications cover every Adopt or Adapt row whose precedent repository has one of two conditions:

- It has no LICENSE file.
- Its licence is not MIT, MPL-2.0, LGPL, Apache-2.0 or CC0.

These rows need no specification:

- Rows that cite a repository only as a pattern to avoid.
- Defer and Drop rows.
- Vanilla classes that Nublar uses through the Minecraft API.
- SBL, GeckoLib 5 and Balm, which Nublar depends on.

Build each one from the description below. **Specify, do not copy.**

### S1. Egg stages, hatch and hatchling rules (B11.1, B11.2, B11.4, B16.4). Source: AC

**In game:** a placed egg cracks in visible stages and wobbles, then breaks open with shell particles and a crack sound. The baby walks out at the start of growth.

- **Inputs:** the incubated egg item with its `DinoData`, the species `SpeciesData`, `incubation.wobble_seconds`, the placement position.
- **State:** block entity fields `hatchGameTime` (long) and a `DinoData` copy. Block state `STAGE` 0 to 3.

1. On placement, copy the `DinoData` into the block entity. Set `hatchGameTime` to the current game time plus the wobble time.
2. Set `STAGE` to 1 and schedule a block tick at one third of the wobble time. Use no block-entity ticker.
3. On each scheduled tick, raise `STAGE` by one and schedule the next tick. The client plays the wobble clip from `STAGE` 1.
4. On chunk load, if the game time has passed `hatchGameTime`, hatch at once.
5. At hatch, test free space for the baby box (B11.3). If a block fills the space, schedule a retry and show an action-bar message to nearby players.
6. Roll `SpeciesData.hatch_success`. On a fail, drop a broken egg item and remains (plan B11).
7. On a pass, remove the block and spawn the baby at growth 0 with the stored genome and the per-clone roll (B07). Play the crack sound and shell particles.
8. Give egg blocks an empty collision shape for dinosaurs and a normal shape for players.
9. Hatchling rules while the dinosaur is not adult: no hunt activity, no fear effect from its display, bite clip only, no player as a target.

| Value | Start | Basis |
| --- | --- | --- |
| Stages | 3 | AC crack stages `[v]` |
| Blocked-hatch retry | 100 ticks | `[inf]` |
| Message radius | 8 blocks | `[inf]` |
| AC reference, contrast only | Random tick 1 in 20, three successes, mean about 3.4 days | AC `[v]` `[inf]` |

### S2. One master block entity per multi-block machine (B12.3). Source: AC

**In game:** any part of an assembled machine opens the same screen. Breaking any part drops one set of contents.

- **Inputs:** the part block positions and the master position from assembly (plan B12).
- **State:** each part stores the offset to its master. Only the master holds inventory, energy and progress, and only the master ticks.

1. Assembly sets each part to its assembled state and writes the master offset.
2. Use on a part resolves the master and opens the master's menu.
3. Item and energy capability queries on a part return the master's handler, with the side mapped to the part face.
4. Each part keeps its own shape for collision and selection.
5. Breaking any part makes the master drop every slot once. The other parts become unassembled part items (plan item retention).
6. A part whose master is missing returns to its unassembled state.

### S3. Block-position and version cache (B14.2, B14.3, B22.7, rank 5). Source: AC

**In game:** fences and tracks cost nothing while nothing changes.

- **Inputs:** the current block position of an entity, or the version counter of a fence network.
- **State:** the last key (a `long`) and the cached result.

1. Each tick, compare the current key with the last key.
2. If the keys are equal, use the cached result.
3. If the keys differ, compute the result again and store it with the new key.
4. A fence network raises its version counter on any connection, power or wire-health change.

### S4. Need cooldown as an expiring memory (B17.4). Sources: MCA, FAF

**In game:** a dinosaur that has eaten does not walk to food again for a while.

- **Inputs:** the met need, `needs.cooldown_seconds.<need>`.
- **State:** one cooldown memory per need, registered with a codec so that it saves.

1. When a need is met, erase its need flag.
2. Set the matching cooldown memory with an expiry of the configured seconds times 20.
3. The need's sensor and behaviours start only while the cooldown memory is absent.
4. The brain removes the memory at expiry. No other code counts the time.

### S5. Stress meter with drift toward zero (B17.5). Source: MCA

**In game:** a hungry, thirsty or lonely dinosaur grows stressed. It then attacks fences and stops breeding. A fed, watered pair calms down.

- **Inputs (plan B17, Beta):** hunger state, thirst state, company, a live-bait kill.
- **State:** `stress` (int, saved), `IS_STRESSED` memory, a synced band (calm, uneasy, stressed).

1. Every 20 ticks, offset by entity id, add the input deltas from the table.
2. Subtract the drift: stress divided by the drift divisor, rounded.
3. Clamp to 0 and the maximum.
4. Set `IS_STRESSED` at or above the species `stress_threshold`. Clear it below the threshold minus the hysteresis.
5. A live-bait kill subtracts its relief once.
6. Send the band to the client only when it changes.

| Value | Start | Basis |
| --- | --- | --- |
| Maximum | 100 | `[inf]` |
| Hungry, thirsty, alone (below `group_size_min`) per step | +2, +2, +1 | `[inf]` |
| Fed, watered and with company per step | −1 | `[inf]` |
| Drift divisor | 12 | MCA mood drift `[v]` |
| Hysteresis | 10 | `[inf]` |
| Live-bait relief | −20 | `[inf]` |

### S6. Grazing inside a height band (B17.8). Source: AC

**In game:** a Brachiosaurus eats leaves at height 8. A Triceratops eats grass and low plants and never reaches those leaves.

- **Inputs:** species reach band from `SpeciesData`, growth fraction, the diet block tag, `mobGriefing`.
- **State:** `GRAZE_TARGET` memory (block position, expiry).

1. A hungry herbivore with no feeder target computes its band: species reach times growth fraction, relative to its feet.
2. It samples a fixed number of random columns inside the scan radius. It never scans a full ring.
3. In each column it reads only the blocks inside the band. The first block in the diet tag wins.
4. It stores the block in `GRAZE_TARGET` with an expiry.
5. It walks until its head is within reach, plays the eat clip and consumes the block as vanilla sheep do.
6. It restores hunger by the diet value of the block.

| Value | Start | Basis |
| --- | --- | --- |
| Columns per scan | 16 | `[inf]` |
| Scan radius | 12 blocks | `[inf]` |
| Scan rate while hungry | 40 ticks | `[inf]` |
| Reads per scan | Under 200 | Arithmetic `[inf]`. AC ring scans read about 14,000 `[inf]` |

### S7. Sleep window, cooldown and wake lock (B17.10). Source: FAF

**In game:** dinosaurs sleep at their time of day, wake when hurt and stay awake for a short time after they wake.

- **Inputs:** day time, sleep need, damage events, danger memories.
- **State:** `SLEEP_COOLDOWN` and `WAKE_LOCK` memories with expiry.

1. `SmartBrainSchedule.byDaytime` switches to the rest activity in the species window.
2. Sleep starts in the window when the sleep need is low, no danger memory exists, the dinosaur stands on ground and is not in water.
3. While asleep, the dinosaur does not move, and the sleep need recovers.
4. Damage wakes it and sets `WAKE_LOCK`.
5. When it wakes for any reason, it sets `SLEEP_COOLDOWN` with a random expiry in the range.

| Value | Start | Basis |
| --- | --- | --- |
| Sleep cooldown | 6,000 to 8,000 ticks | FAF Tuff Golem `[v]` |
| Wake lock after damage | 200 ticks | Naturalist wake timer `[v]` |
| Window | Per species in `SpeciesData` | `[inf]` |

### S8. Path budget with back-off (B17.12). Source: MCA

**In game:** a park of many dinosaurs never stalls when many of them want food at once.

- **Inputs:** path requests from need walks.
- **State:** a per-level counter reset each tick, a queue, a retry time per dinosaur.

1. A need walk asks for a path only while the counter is under the budget. Otherwise it joins the queue.
2. Queued requests run first on the next tick.
3. A failed path doubles the retry delay, from the base to the cap, and stores the target in an unreachable memory.
4. Herd and journey paths use the budget split of the hitbox design.

| Value | Start | Basis |
| --- | --- | --- |
| Budget | `ai.path_budget_per_tick`, 4 | `[inf]` |
| Retry delay base and cap | 20 and 400 ticks | `[inf]`. FAR clears its avoid list after 400 ticks `[v]` |

### S9. Pack target push on change (B18.1). Source: AC

**In game:** when the leading raptor picks a prey, the whole pack turns on it at once.

- **Inputs:** a change of the leader's attack target.
- **State:** none beyond the members' attack-target memories.

1. When the leader's attack target changes, the group manager receives one event.
2. For each member inside the species follow range, set the member's attack target. Skip babies, members in a hunt cooldown and members that cannot attack the target.
3. Members clear a dead or lost target on their own through `InvalidateAttackTarget`.
4. Do no per-tick push.

### S10. Threat display before an attack (B19.5, rank 10). Sources: AC, Mowzie

**In game:** a dinosaur stops, faces the player and roars or shows its frill before it charges. A player who backs away can avoid the fight.

- **Inputs:** the target, its distance, the species display clip and its length, the display cooldown.
- **State:** a phase (display, attack, recovery) and a `WARNING` memory with expiry.

1. When the dinosaur gets a target beyond the skip radius and has no display cooldown, it enters the display phase.
2. In the display phase it stops, faces the target, starts the display clip by a server trigger (B27.2) and plays the sound.
3. The display lasts the species clip length.
4. If the target leaves the territory or the follow range during the display, the dinosaur ends the display and attacks nothing.
5. Otherwise the attack phase follows (S27), then recovery.
6. The display cooldown starts at the end of the display, so each fight opens with at most one display.
7. A target inside the skip radius, or damage taken, skips the display.

| Value | Start | Basis |
| --- | --- | --- |
| Display length | Species clip length | Warden 84 ticks `[v]`, AC 55 ticks `[v]`, Naturalist 42 ticks `[v]` |
| Skip radius | 5 blocks | `[inf]` |
| Display cooldown | 200 to 400 ticks | AC roar cooldown 200 to 399 ticks `[v]` |

### S11. Flee through an avoid memory (B19.6). Source: FAF

**In game:** a Gallimimus that sees a threat runs away, sprints when the threat comes close and calms down after a few seconds.

- **Inputs:** the shared visible-entity list, the species flee list.
- **State:** `AVOID_TARGET` memory with expiry.

1. The visible-entity sensor tests each entity against the flee list. A match writes `AVOID_TARGET` with an expiry.
2. While the memory exists, the flee behaviour picks a walk target away from the threat with vanilla random-position helpers.
3. It uses walk speed, and sprint speed inside the sprint radius.
4. It picks a new target when it arrives, or every 20 ticks while the threat stays.
5. The memory expiry ends the flight.

| Value | Start | Basis |
| --- | --- | --- |
| Flee distance | 16 blocks | FAF `[v]` |
| Walk and sprint speed | 1.25 and 1.4 | FAF `[v]` |
| Memory expiry | 100 to 200 ticks (5 to 10 s) | FAF `[v]` |
| Sprint radius | 7 blocks | Vanilla avoid distance `[v]` |

### S12. Validated key packet (B19.11, B22.2). Source: AC

**In game:** only the driver steers a vehicle, and only the owner commands a dinosaur.

- **Inputs:** a packet with the target entity id and a key bit field.
- **State:** the vehicle input state, cleared on dismount.

1. The server finds the entity in the sender's level. A missing entity drops the packet.
2. For vehicle keys, the sender must be a passenger of that entity, and drive keys need the driver seat.
3. For dinosaur commands, the owner UUID must equal the sender, and the distance must be inside the follow range.
4. The server accepts at most one packet per player per tick.
5. The server writes the keys to the input state. Dismount clears it.

### S13. Live-bait holding and release (B20.8). Sources: Vanilla Refresh, FAF

**In game:** a cow led into the live-bait feeder stays there until a hungry raptor comes near. Then the feeder opens and the raptor hunts it.

- **Inputs:** a leashed vanilla animal in the pen, the feeder registry, hungry carnivores in range.
- **State:** the held animal UUID in the feeder block entity.

1. The player uses the feeder while the animal is in the pen. The feeder tags the animal with `projectnublar:bait` and makes it persistent.
2. The feeder ties the animal to the feeder with a vanilla leash. It does not edit the animal's goals.
3. Every 40 ticks the feeder asks the registry for a hungry carnivore inside the release range.
4. On a match, the feeder drops the leash and sets its open state.
5. A carnivore's prey pass ranks tagged bait first. A kill of tagged bait gives the stress relief of S5.
6. If the animal leaves or dies, the feeder clears its state.

### S14. Digsite as a data-only jigsaw structure (B21.1). Source: Illager Invasion

**In game:** `/locate structure projectnublar:digsite` finds a digsite in a fresh world. Digsites never overlap villages.

1. A jigsaw structure file names the start pool, the depth, the start height on the surface heightmap, the terrain adaptation and the biome tag.
2. A structure set gives the spread and an exclusion zone around villages.
3. Optional pieces use pools with an empty fallback.
4. All of it is data under `data/projectnublar/worldgen/`. No code places the structure.

| Value | Start | Basis |
| --- | --- | --- |
| Jigsaw depth | 3 to 4 | Illager Invasion `[v]` |
| Heightmap | `WORLD_SURFACE_WG` | Illager Invasion `[v]` |
| Terrain adaptation | `beard_thin` | Illager Invasion `[v]` |
| Spacing and separation | 42 and 28 chunks | Illager Invasion `[v]` |
| Village exclusion | 10 chunks | Illager Invasion `[v]` |

### S15. People placement and loot injection (B21.2, B21.3). Source: Illager Invasion

**In game:** each digsite has paleontologists and golems at fixed spots and chests with fossils. Vanilla chests can also hold fossils.

1. Each piece template points to a mob pool with one template that holds one entity, or holds a data marker.
2. Marker handling spawns the paleontologist or golem at the marker.
3. Each chest in a template names its own loot table.
4. Fossils join vanilla loot tables only through an added pool at loot-table load, through the loader's loot modification event.
5. Replace no vanilla loot file.

### S16. Paleo-Illager as the smallest illager subclass (B21.4). Source: Illager Invasion

**In game:** Paleo-Illagers guard their sites and fight like vindicators. Vanilla raids and other illager mods keep working.

1. The class extends the vanilla illager base with the retextured vindicator model (plan B21).
2. Its goals follow the vindicator goal set through the vanilla API.
3. Its attributes start at the vindicator values.
4. It spawns only from site mob pools and the structure spawn overrides.
5. It joins no raid (B21.8 deferred).

### S17. Paleontologist profession (B21.5). Source: MCA

**In game:** a villager next to the paleontologist workstation takes the profession and trades fossils, maps, amber and tools for emeralds.

1. Register a point-of-interest type for the workstation block states with one ticket.
2. Register the profession with that point of interest and a work sound.
3. Ship the trades as data per level, which 26.2 supports through registry keys `[v]`.
4. Vanilla villager brains claim the job site. Nublar adds no job code.
5. Digsite mob pools spawn villagers with the profession set.

### S18. Village house added at server start (B21.6). Sources: GV, BV

**In game:** paleontologists appear in some villages at a low rate.

1. At server start, read the template pool registry.
2. For each vanilla village house pool, add the paleontologist house element with the configured weight.
3. Add the element to the element list and to the weighted list that the pool draws from.
4. A weight of 0 adds nothing.
5. Replace no vanilla pool file.

| Value | Start | Basis |
| --- | --- | --- |
| `worldgen.paleontologist_house_weight` | 1 | `[inf]` |

### S19. Vehicle placement check (B22.6). Source: AC

**In game:** the Jeep item places a Jeep only where it fits. It never places one inside a wall.

1. On use, ray-trace from the player's eyes to the hit block, within reach.
2. Build the vehicle box on the hit face, turned to the player's yaw.
3. If the box collides with blocks, fail with a message.
4. Otherwise spawn the vehicle, copy the item name and data, and use one item outside creative mode.

### S20. Track follower (B22.7). Source: AC

**In game:** the Explorer drives along a laid track at a steady speed, takes curves and slopes, and stops at stop markers.

- **Inputs:** the track block under the Explorer, its `RailShape`, stop markers.
- **State:** the last block position (S3), the travel direction, a stop timer.

1. When the block position changes, read the track block and its shape. Otherwise use the cached shape.
2. From the shape and the travel direction, pick the exit direction.
3. Move the Explorer along the exit direction at track speed. Raise or lower it on slopes.
4. Turn the body toward the exit at a capped rate.
5. On a stop marker, start the stop timer and hold still until it ends.
6. At a track end, stop and hand control to the driver.

### S21. Interval table with id offsets (B25.1). Source: MCA

**In game:** no tick has a spike from many dinosaurs doing slow work at the same time.

1. One class holds every slow-work interval.
2. Work is due when the game time plus the entity id, modulo the interval, equals 0.
3. Every slow job reads its interval from this class.

| Job | Interval | Basis |
| --- | --- | --- |
| Needs and stress | 20 ticks | Plan B17 `[doc]` |
| Group join | 40 or 200 ticks | Local `JoinGroup` `[v]` |
| Grazing scan | 40 ticks | S6 `[inf]` |
| Water scan | 60 ticks | B17.2 `[inf]` |
| Fence sensor | 100 ticks | B15.2 `[inf]` |
| Reputation decay | 24,000 ticks | Vanilla gossip daily decay `[v]` |

### S22. Low-effects client switches (B25.3). Source: AC

**In game:** a player on a weak computer turns off particles, screen shake and glow layers.

1. A client config file has three switches: particles (all, reduced, none), screen shake, glow layers.
2. The config loads into static fields on load and on change.
3. Renderers and effect code read the static fields. They never read the config file per frame.

### S23. Bursts spread over ticks (B25.6). Source: Mutant Monsters

**In game:** a carcass drop, a leaf-breaking charge or a fence cascade never freezes the server for a tick.

1. Each level keeps a queue of pending units: block breaks, item drops, connection breaks.
2. Each tick processes up to the unit cap.
3. Level unload clears the queue (B25.4).

| Value | Start | Basis |
| --- | --- | --- |
| Units per tick | 8 | `[inf]` |

### S24. GameTests in common code (B25.8). Source: FAF

**In game:** no change. Both loaders run the same automatic tests.

1. Test classes live in `common` and use only the vanilla GameTest API.
2. Each loader has one registration class that lists the common test classes.
3. Structure templates for the tests live in common resources.
4. CI runs the GameTest server task of both loaders.

### S25. Slower idle work far from players (B25.9). Sources: Mowzie, Vanilla Refresh

**In game:** a paddock far from every player keeps its needs, growth and breeding. Its idle wandering slows down.

1. Every 100 ticks, offset by entity id, each dinosaur finds the distance to the nearest player.
2. Beyond the far distance, it sets a far flag.
3. With the flag set, idle journeys and graze moves run at the interval times the far factor.
4. Needs, growth, stress and breeding gates tick at the normal rate.
5. Sounds and particles do not start while the flag is set.

| Value | Start | Basis |
| --- | --- | --- |
| Far distance | 128 blocks | `[inf]` |
| Far factor | 4 | `[inf]` |
| Recheck | 100 ticks | `[inf]` |

### S26. Pose state in spawn data (B27.1). Sources: Mutant Monsters, Aquamirae

**In game:** a player who arrives during a roar sees the roar at the right point, not from its start.

1. The spawn payload carries the pose state of the hitbox design: animation id, start tick and loop flag.
2. On spawn, the client starts the clip at the elapsed time: current game time minus the start tick.
3. The server sends a death clip again after a chunk reload.

### S27. Hits dealt at one tick with a cone test (B27.3). Sources: Mutant Monsters, Mowzie

**In game:** a bite or tail swipe hurts what is in front of the dinosaur at the moment the clip lands, and nothing behind it.

1. Each attack clip has one hit tick, from the species data or a keyframe event.
2. At the hit tick, on the server, take candidates from the shared visible list inside the reach plus half the candidate's width.
3. Keep a candidate only if both tests pass: distance inside reach, and the angle from the body yaw inside half the arc.
4. Damage each kept candidate once.
5. Parts only receive hits. They never deal hits.

| Value | Start | Basis |
| --- | --- | --- |
| Arc | 90°, per species | `[inf]` |
| Turn toward target before the hit | 30° per tick at most | Mowzie `[v]` |
| Reach | Species attack reach | Plan B00 `[doc]` |

## Facts that changed or were corrected

These facts differ from a brief, a wiki, a store page, an earlier note or the plan. Code wins over documentation where both exist.

| # | Topic | Earlier record | Finding | Label |
| --- | --- | --- | --- | --- |
| 1 | IaF CE repository | Iceandfire-Community-Edition/ice_and_fire_ce | That address is dead. The repository is IAFEnvoy/IceAndFire-CE | `[v]` |
| 2 | Mowzie repository | BobMowzie/MowziesMobs | Not found. The public repository is BobMowzie/MowziesMobs-Public | `[v]` |
| 3 | AC egg hatch time | Wiki: 8 to 10 days | Code: mean about 3.4 days, median 3.0, 95th percentile 7.2. On Fern Thatch, mean 1.7 days | `[v]` `[inf]` |
| 4 | AC egg trample alarm | Wiki: 32 by 16 | Code: a 25-block inflate, a cube 51 blocks wide | `[v]` |
| 5 | AC Tremorsaurus taming | Wiki: 3 to 8 feeds | Code: 4 to 9 feeds, expected 4.97 | `[v]` `[inf]` |
| 6 | AC roar scare | Wiki: 1.5 s | Code: about 65 ticks (3.25 s) | `[v]` `[inf]` |
| 7 | AC Subterranodon flee box | Wiki: 32 | Code: 7 | `[v]` |
| 8 | AC Vallumraptor health | Wiki: 24 | Code: 28. Elder 32 | `[v]` |
| 9 | AC raptor flee on Elder death | Wiki claim | No code for it. A pack under 4 members flees from a Grottoceratops | `[v]` |
| 10 | AC Grottoceratops grazing | Wiki: within 6 blocks | 6 is the vertical range. The horizontal range is 16 | `[v]` |
| 11 | AC clutch size | Wiki: 1 to 4 | Code: 1 to 3. 4 is the stack limit | `[v]` |
| 12 | AC Atlatitan tail whip | Wiki: 16 | Code: 8 | `[v]` |
| 13 | AC egg and baby guarding | Expected from the wiki | No guarding goal in any AC class | `[v]` |
| 14 | AC versions | Modrinth lists 1.20.1 only | A 1.21.1 branch exists on GitHub | `[doc]` `[v]` |
| 15 | FAR egg hatch | Wiki: 100 s | Code: 3,000 ticks of warmth (150 s) | `[doc]` `[v]` |
| 16 | FAR imprint radius | Wiki: 6 blocks | Code: 16 blocks | `[doc]` `[v]` |
| 17 | FAR feeder detection | Wiki: 6 blocks | Code: a walk of the block-entity maps of 25 chunks | `[doc]` `[v]` |
| 18 | FAR scarab gem drop | Scarab page: 0.01 % | Fossil page: 0.1 % | `[doc]` |
| 19 | RevampedWolf pack | Store pages: pack leader, howl, hunger hunting | No pack-leader, howl or follow code in any branch. Hunting depends on a cooldown that never counts down | `[v]` |
| 20 | Better Villages source | Expected in the repository | The repository holds a README, a licence file and workflows. It has no source | `[v]` |
| 21 | Cobblemon on 26.x | Expected 26.2 support | No 26.2 branch. feature/version-bump targets 26.1.2 | `[v]` |
| 22 | Naturalist renderer | GeckoLib mod | The 26.2 branch dropped GeckoLib | `[v]` |
| 23 | Naturalist licence | Earlier note: Custom (Modrinth) | Repository: MIT for code, resource folders ARR. Modrinth still lists Custom | `[v]` `[doc]` |
| 24 | SBL 2.0.3 | Not recorded before | Changelog: random sensor offsets, a one-tick fix, a half-radius fix in entity retrieval. The half-radius fix is in the code. The offsets were not traced to the timer | `[doc]` `[v]` `[inf]` |
| 25 | SBL wiki | Used as a reference | The wiki describes the API before 2.0 | `[doc]` |
| 26 | Citadel versions | Expected 26.x work | No 26.x branch | `[v]` |
| 27 | AzureLib LOD distances | Docs: 20 and 28 blocks | Code: 40 and 48 blocks. Until 4.0.10 (2026-10-09), the code computed animation LOD and then ignored it | `[doc]` `[v]` |
| 28 | AzureNavigation | Earlier hitbox research | Absent from the 26.2 branch | `[v]` |
| 29 | AFT licence and libraries | CurseForge: MIT, needs Architectury API | LICENSE file: CC0-1.0. NeoForge metadata: MIT. The 26.2 build has no Architectury API | `[v]` `[doc]` |
| 30 | Countered licence | Not recorded before | Repository: CC0-1.0. Modrinth: MIT | `[v]` `[doc]` |
| 31 | Mutant Monsters licence | Not recorded before | Repository: AGPL-3.0-or-later. Modrinth: AGPL-3.0-only | `[v]` `[doc]` |
| 32 | GeckoLib 5 scale | Local renderers scale by hand | GeckoLib 5 applies the scale attribute itself | `[v]` |
| 33 | GeckoLib 5 speed | `setAnimationSpeedHandler` | Removed. `setAnimationSpeed` replaces it | `[v]` |
| 34 | GeckoLib 5 LOD | Expected | GeckoLib 5 has no LOD and no distance culling | `[v]` |
| 35 | Hitbox risk R3 (bone updater order) | Open in the hitbox design | Bone updaters run after the controllers, so an added bend delta works | `[v]` |
| 36 | IaF CE Fabric | Docs: Fabric on 1.21.1 | Not confirmed. The 1.21.1 branch read is NeoForge only | `[doc]` `[v]` |
| 37 | Warden anger levels | Not recorded before | Calm 0 to 39, agitated 40, angry 80, cap 150 | `[v]` |
| 38 | Sniffer dig cooldown | 20 min | 9,600 ticks (8 min) | `[v]` |
| 39 | Warden listener radius | Wiki: 15 | Code: 16 | `[v]` |
| 40 | Baby speed | Expected a general boost | No general baby speed boost in 1.20.1 | `[v]` |
| 41 | Gossip `major_positive` maximum | Wiki: 20 | Code: 100 | `[doc]` `[v]` |
| 42 | Zombified Piglin anger time | Wiki: 20 to 55 s | Code: 20 to 39 s | `[doc]` `[v]` |
| 43 | Cobblemon pasture wander | Config page: 64 | 1.8.0 changelog: 32 | `[doc]` |
| 44 | Cobblemon PC boxes | Config page: 30 | 1.7.0 changelog: 40 | `[doc]` |
| 45 | Local `BrainUtils` users | Plan P2: 22 files | 30 files | `[v]` |
| 46 | Local unused memory types | Plan B16: 12 | 15 | `[v]` |
| 47 | Local energy balance | Not recorded before | Each post drains 10 FE per tick. The generator makes 0.2 FE per tick | `[v]` |
| 48 | Local layer colours | Not recorded before | Colours are never saved and never reach the client | `[v]` |
| 49 | Local herd code | Plan B26: in the herd-leader worktree | 29 herd classes are in the main checkout, uncommitted | `[v]` |
| 50 | Local tests | Not recorded before | 371 unit tests run through a stand-in. Gradle has no test dependencies | `[v]` |
| 51 | Local stamina | Not recorded before | The stamina maximum is 0 because of a config key mismatch | `[v]` |

## Open items for the lead

The lead answered items O1 to O6. Decisions register rows D15 to D20 in the plan hold the answers.

## Gameplay loops, pacing and design alternatives

Project Nublar has the slowest road to the first creature of the six loops. After the first hatch, it has the richest mid-game: gene editing, heredity, fences, tours and trade. Every rival with a creature bond gives that bond at hatch, and every rival with hidden state gives a readout, but Nublar gives neither. Of the 16 candidates below, 5 are Keep, 6 are Consider and 5 are Adopt-in-part. All are options for the project lead, and none changes a decision.

**Names in this section.** "Section 11" is section 11 of `docs/codebase-analysis.md`. "The plan" is `docs/project-plan.md`. A row id such as B19.8 is a row of the technique tables above. O3 to O6 are the open items above. A note name such as `fossils_revival_and_ice_and_fire.md` Q4 is a file in `research_notes/Reference mod survey for Beta/`. **R** in a table cell marks a reward moment for the player.

### Loops compared

**In game:** each column is the full path from the first find to a creature that the player shows, rides or trades. Each cell gives what the player does, the gate or cost, and the reward moment.

| Stage | Nublar (decided) | FAR | IaF CE | AC (Primordial Caves) | Cobblemon | MCA (people side) |
| --- | --- | --- | --- | --- | --- | --- |
| 1. Find a source | Mine fossil ore in the species' biome and depth. Find amber. Draw DNA from a live animal with a syringe. *Gate:* knowledge of biome and depth, quality luck. **R:** the ore shows its quality before mining | Mine fossil blocks or sift sand and gravel. *Gate:* iron pickaxe, drop chances `[doc]`. **R:** rare Scarab Gem | No lab source. Get an egg from a stage 4 or 5 female dragon `[doc]`. *Gate:* a boss-scale fight | Find the Primordial Caves biome. Dinosaurs live there wild `[doc]`. *Gate:* travel and danger | Brush suspicious sand at a Fossil Dig Site, or catch a wild Pokémon. *Gate:* find a site. **R:** each site guarantees one fossil `[doc]` | Villagers already live in each village `[v]`. No source stage |
| 2. Extract | Fossil Processor: one fossil per run, one DNA tube per run, by-products `[doc]`. *Gate:* filter tier, water, power, run time. **R:** amber reveals its species on the tube | Analyzer: one item per run, weighted outputs `[v]`. *Gate:* DNA chance per bio-fossil. Meat gives full DNA `[doc]` | None | None | Fossil Analyzer takes the fossils `[doc]` | None |
| 3. Build the genome | Sequencer adds each sample to the species entry. Edit donors, genes and colours. *Gate:* clone, edit and gender thresholds, isolation needs 100 % of each donor `[doc]`. **R:** effect text and risk panel | None. One DNA item gives one species | None | None | None. Hidden stats roll at creation `[v]` | Genes roll per villager `[v]`. No player editing |
| 4. Make the egg | Synthesis with a bird embryo, then the Egg Printer `[doc]`. *Gate:* power, bone meal, crack chance without the leveling sensor `[v]` | Culture Vat with Bio-Goo `[v]`. *Gate:* failure chance that destroys the vat and spawns a mob `[v]` | The female lays the egg `[v]` | Feed two adults. The mother buries a clutch `[v]` | Restoration Tank with organic material `[doc]` | Marriage, then pregnancy `[v]` |
| 5. Incubate and hatch | Incubator with power, bulb and plant matter. Place the egg on the ground. It shakes and hatches `[doc]`. *Gate:* hatch success per species `[doc]`. **R:** per-clone variation shows for the first time at hatch | Egg entity hatches while warm and lit `[v]`. **R:** first-dinosaur music, once per player `[v]` | Egg hatches in fire, rain or freezing water `[v]` | Egg cracks by random tick in 3 visible stages `[v]`. Fern Thatch doubles the speed `[v]` | Claim the result with a Poké Ball or release it `[doc]`. **R:** alpha chance `[doc]` | Birth at the end of pregnancy `[v]` |
| 6. Bond | Tame a calm animal with low stress `[doc]`. No owner at hatch (O3 default) | Imprint at hatch on the nearest player `[v]`. Hand-feed, whip or gem for other species `[v]`. **R:** tame at hatch | Owner stored on the egg. Hatching is the only survival tame `[v]`. **R:** tame at hatch | Imprint at hatch for 3 of 7 species `[v]`. Stun-and-feed ritual for the Tremorsaurus `[v]`. **R:** a tame baby that sits | Catch the Pokémon. Friendship value `[doc]`. **R:** the catch | Talk and gifts raise hearts `[v]`. **R:** hearts particles at each gain `[v]` |
| 7. Contain | Electric fences with wire health, power and a switch `[doc]` | Reinforced Glass and iron-tier walls `[doc]` | Home position and wander radius `[v]` | None. Animals stay wild | Pasture Block: release and recall `[doc]` | Village bounds, homes and beds `[v]` |
| 8. Feed and care | Carnivore, herbivore and live-bait feeders. Hunger, thirst, stamina and sleep, social need, stress `[doc]` | Feeder point pools, hunger, mood `[v]` | Hunger, dropped food `[v]` | Timed grazing. No hunger need `[v]` | Fullness meter `[doc]` | Mood and gifts `[v]` |
| 9. Grow | Smooth growth over game days `[doc]` | Smooth growth with baby and teen labels `[v]` | 5 stages with smooth size inside each stage `[v]` | One vanilla baby step `[v]` | Levels. Baby scale rises over the first levels `[v]` | Child age (time not recorded) |
| 10. Breed | Nest, fertility roll, stress below the species threshold `[doc]`. Large clutch, low hatch success `[doc]` | Mood gate, population cap, parent cooldowns `[v]` | Stage gate, type food, cooldown `[v]` | Vanilla love mode and cooldown, clutch on bedding `[v]` | Breeding branch only, not released `[v]` | Village procreation chance under a bed limit `[v]` |
| 11. Use and show | Drive the Jeep Wrangler. Run Ford Explorer tours on a track with stops `[doc]`. **R:** a driverless tour | Ride 15 species with a whip `[doc]`. **R:** first ride | Ride and fly at stage 3, breathe fire `[v]`. **R:** first flight | Ride the Tremorsaurus, Atlatitan and Subterranodon `[v]`. **R:** roar meter attack | Ride with 8 styles, battle `[doc]`. **R:** levels and Pokédex entries | Guards defend the village, taxes `[v]` |
| 12. Trade | Paleontologist trades maps, fossils, amber and parts `[doc]` | Archaeologist villager `[doc]` | None found | None found | Nurse villager trades `[doc]` | Village economy, taxes, bounty hunters at low reputation `[v]` |
| 13. Read the animal | Debug stick for creative mode and operators only (plan B16) | Dinopedia: age, health, hunger, mood, order, foods `[doc]` | Dragon Horn tooltip: name, gender, stage, days `[v]` | None found | Pokédex scanner and name labels `[doc]` | Blueprint: villager list and village rules `[doc]` |

Rival sources are `fossils_revival_and_ice_and_fire.md` Q1 to Q4 and Q11 to Q13, and `alexs_caves_dinosaurs.md` KQ1 to KQ5. Other sources are `cobblemon_wiki.md` key questions 3 and 4, `people_and_structures_mods.md` (MCA) and `other_mod_wikis.md` key question 1. Nublar cells are from section 11.1 to 11.12 and plan B05 to B22.

**What Nublar can learn at each stage:**

| Stage | Lesson for Nublar | Link |
| --- | --- | --- |
| 1. Find a source | Cobblemon guarantees one fossil per dig site. A digsite that always holds enough DNA for one first clone removes the worst early luck | Candidate 13 |
| 2. Extract | The FAR Analyzer and the Nublar Fossil Processor need a similar number of fossils for a first clone. Run time per fossil is the difference. The processor is Nublar's first bottleneck `[inf]` | Candidates 13, 16 |
| 3. Build the genome | No rival has this stage. The risk panel and the effect text are the reward. Keep them visible before synthesis, as section 11.6 decides | Section 11.6 |
| 4. Make the egg | FAR makes failure dramatic but breaks vanilla feel. Nublar's printer crack is a quiet loss. Show its cause (no leveling sensor) in the screen | Candidate 9, B11.7 |
| 5. Incubate and hatch | AC shows 3 crack stages. FAR plays a sound once at the first hatch. A one-time beat for the first hatch supports the decided variation reveal | B11.2, B11.4, plan B13 advancements |
| 6. Bond | All three dinosaur mods bond at hatch. Nublar asks the player to tame later, with no visible progress | Candidates 1, 15, O3 |
| 7. Contain | Nublar's fences have no rival. FAR's iron walls are the anti-pattern (B15.6). Cobblemon caps creatures per chunk | O6 |
| 8. Feed and care | FAR stores food as points and seeks food at two hunger thresholds. AC shows that animals with few needs ask little of an absent player | B20.2, B17.6, candidate 11 |
| 9. Grow | Nublar's growth sits between FAR and IaF CE. IaF CE names stages so a long wait has milestones. FAR and IaF CE sell items that add one day of growth | Candidates 5, 12 |
| 10. Breed | FAR, AC and IaF CE use the vanilla cooldown of 6,000 ticks. Nublar has no cooldown value. Nublar's large clutch with low hatch success is unique | B18.4, candidate 14 |
| 11. Use and show | Riding is the late-game payoff in four rivals. Nublar's payoff is the tour. Tours must carry that weight in Beta | Candidate 10 |
| 12. Trade | MCA ties trades, greetings and bounty hunters to one reputation value. Nublar plans that value (B19.9) but uses it only for taming | Candidates 13, 15 |
| 13. Read the animal | Every rival with hidden state gives a readout. Without one, Nublar's stress and needs are invisible to survival players | Candidate 4, O4 |

### Progression and pacing

**In game:** this table shows how long each mod makes the player wait, what each step costs, and how often it fails. Times use 20 ticks = 1 s and one game day = 24,000 ticks = 20 min. "Not recorded" means that no note or Nublar document gives the value.

| Value | Nublar | FAR | IaF CE | AC | Cobblemon | MCA |
| --- | --- | --- | --- | --- | --- | --- |
| Fossils or attempts for the first creature | 25 to 30 fossils with an iron filter, about 7 with a diamond filter, for the 50 % clone threshold `[doc]` | About 30 fossil blocks for one Mesozoic DNA (22.5 % bio-fossil × 15 % DNA) `[inf]` from `[doc]`. A species bone gives 35 % that species `[v]` | One egg from a stage 4 or 5 female dragon `[doc]` | Tremorsaurus: 4 to 9 feeds, mean 4.97 `[v]`. Subterranodon: 1 in 3 per tail `[v]` | One fossil per dig site `[doc]`. Catch rate: not recorded | No source stage |
| Extraction time per item | 4 min per fossil, then 3, 2 and 1 min with chips `[v]` | 200 ticks (10 s) per item `[v]` | None | None | Not recorded | None |
| Genome step | Sequencer: hard drive 10 s, SSD 5 s per sample `[v]` | None | None | None | None | None |
| Egg creation | Synthesis 10 min, Egg Printer 10 min at the base tier `[v]` | Culture Vat 6,000 ticks (5 min) `[v]` | Laid by the female `[v]` | Lay animation 100 ticks `[v]` | Restoration Tank 12 min `[doc]` | Pregnancy length: not recorded |
| Incubation or hatch time | 18 s per percent (30 min), 15, 12 and 9 s with bulbs `[v]`. Species times: not recorded. Wobble time: not recorded | 3,000 ticks (2.5 min) `[v]`. Wiki says 100 s `[doc]` | 7,200 ticks (6 min) `[v]` | Mean 82,000 ticks (3.4 days, 68 min loaded), 95th percentile 7.2 days `[inf]`. Wiki says 8 to 10 days `[doc]` | Breeding hatch: not released. `eggCycles` about 20 for a starter `[doc]` | Not recorded |
| Machine time for one creature | About 50 min at the base tier `[doc]` | About 8 min `[inf]` | None | None | 12 min `[doc]` | None |
| Time to the first creature, base tier | About 155 to 175 min with an iron filter, about 80 min with a diamond filter, before travel and building `[inf]` | About 9 min of machine time after the fossils `[inf]` | 6 min after the egg. The fight: not recorded | About 68 min loaded by the egg route `[inf]`. A few minutes by taming `[inf]` | 12 min after the fossil `[doc]` | Not recorded |
| Failure chances | Printer crack 10 % without the sensor `[v]`. Gender random at 30 % below 80 % `[doc]`. Incubation failure from genome risk: coefficients not recorded | Culture Vat failure 10 %, destroys the vat and spawns a mob `[v]` | None found | Trample crush 1 in 100 per step `[v]` | None found | None found |
| Hatch success | Tyrannosaurus 5 % `[doc]`. Five species: not recorded | 100 % if warm `[v]` | 100 % under the type condition `[v]` | 100 % `[v]` | Not recorded | None |
| Expected eggs for one live Tyrannosaurus | About 20 `[inf]` from the 5 % hatch success | 1 | 1 | 1 | 1 | None |
| Growth to adult | Tyrannosaurus 45 days (15 h) `[doc]`. Draft for the others: 29 to 57 days, not approved `[doc]` | Tyrannosaurus 12 days (4 h) `[v]`. Velociraptor 6 days `[doc]` | Mature at stage 4, 75 days. Full size 125 days (about 42 h) `[v]` | 24,000 ticks (1 day) `[v]` | By level. Level time: not recorded | Not recorded |
| Growth steps | None. Smooth size `[doc]` | Labels at teen and adult days `[v]` | Stage every 25 days `[v]` | One step `[v]` | Baby scale 0.9 to 1.0 over the first levels `[v]` | Age groups baby to adult `[doc]` |
| Breeding gate | Nest near, fertility roll, stress under threshold `[doc]`. Threshold value: not recorded (D13) | Mood above 50, population cap per species `[v]` | Stage 4, type food `[v]` | Breeding food, tame for 2 species `[v]` | Compatibility roll 20 to 70 `[v]` | Population below 0.75 × beds `[v]` |
| Breeding cooldown | Not recorded | Male 6,000 to 12,000 ticks, female 24,000 to 36,000 ticks `[v]`. Wiki says 5 min `[doc]` | 6,000 ticks `[v]` | 6,000 ticks (5 min) `[v]` | 5 to 20 min by `eggCycles`, branch only `[v]` | 0.05 chance per minute `[v]` |
| Clutch | Tyrannosaurus 12 to 24 `[doc]`. Others: draft only `[doc]` | One egg per mating `[v]` | One egg `[v]` | 1 to 3 (raptor, Subterranodon), 1 for others `[v]` | Not recorded | One child `[inf]` |
| Taming effort | Calm and low stress `[doc]`. Effort: not recorded | Imprint radius 16 blocks `[v]` (wiki 6 `[doc]`). Feeding 10 % per feed, whip 1 in 5 per use `[v]` | Hatch only `[v]` | Imprint radius 10 blocks `[v]` | Friendship 0 to 255, base 50 `[doc]` | Gift value × 0.25 `[doc]`. Friend at 40 hearts `[v]` |
| Hunger | Tyrannosaurus hunger 80, thirst 70 `[doc]`. Decay rates: not recorded | −1 per 1,200 ticks. Tyrannosaurus hungry after about 37 min `[inf]` | −1 per 3,000 ticks, range 0 to 100 `[v]` | No hunger need `[v]` | Fullness values: not recorded | None |
| Starvation | 1 health per 80 ticks. Velociraptor dies in about 2.3 min, Tyrannosaurus in about 10 min `[doc]` | 1 health per 40 ticks, stops at half health by default `[v]` | None found `[inf]` | None | None found | None |
| Power | Required. One coal lasts 80 s in the basic generator `[doc]` | Off by default `[v]` | None | None | None | None |
| Materials per creature | Processor 250 mB water per run `[v]`. Synthesis 500 mB water and 8 each of bone meal, sugar and plant matter `[v]`. Printer bone meal `[doc]`. Incubator plant matter `[doc]` | One Bio-Goo per vat run `[doc]` | Fire, rain or ice at the egg `[v]` | Breeding food `[v]` | 64 organic points `[doc]` | Gifts `[doc]` |
| Population cap | None decided (O6) | `maxPopulation`, Tyrannosaurus 2 `[v]` | None found | None found | Pasture 16 per block, 4 per chunk `[doc]` | Bed count × 0.75 `[v]` |

Nublar values are from section 4, section 7.1, 7.4 and 7.8, section 11.1 to 11.11, section 11.16 and plan B11 and B13. Inferred times add the documented steps and ignore travel, building and power setup.

**Reading.**

- **Slower than every rival to the first creature.** Nublar asks for about as many fossils as FAR. Each Nublar fossil takes 24 times longer to process. The first hatch comes after hours, not minutes `[inf]`.
- **The first live Tyrannosaurus comes later still.** Plan B11 applies the species hatch success to the placed lab egg. At 5 %, a player prints about 20 eggs for one live animal `[inf]`. No rival fails a finished egg. Candidate 14 asks the lead to confirm this rule.
- **Small rewards exist before the first hatch.** The ore shows its quality. Amber reveals its species. Plan B13 adds advancements for the first fossil, tube, egg, hatch and paddock. These rewards are small next to a creature.
- **Growth is mid-range.** A 45-day Tyrannosaurus is slower than FAR and faster than IaF CE. IaF CE makes its long wait readable with named stages. FAR and IaF CE also sell an item that adds one day of growth.
- **Breeding has no cooldown value.** Three rivals use 6,000 ticks. B18.4 proposes the same.
- **Care is strict.** Nublar starvation is lethal. FAR deals starvation damage twice as fast but stops at half health by default. AC animals have no hunger at all.
- **Nublar's hooks after the first hatch are the strongest of the six.** The table below lists the hooks that each mod uses. Nublar's gap is a bond and a readout early, not content late.

| Mod | Mid-game hooks | Late-game hooks |
| --- | --- | --- |
| Nublar | Gene edits at 70 %, isolation at 100 %, 7 special genes, skin variants, 5 % morphs `[doc]`. Feeders and live bait | Heredity breeding, high-security fences, large generator, track tours with stops, digsites, Paleo-Illager sites, six species `[doc]` |
| FAR | More than 60 creatures `[doc]`, whip riding, mood and toys, Dinopedia | Golden eggs at 5 % after the all-eggs advancement `[v]`, the boss Anu `[doc]`, relic repair `[v]` |
| IaF CE | Stage 3 unlocks riding and breath `[v]`, commands, home position | Dragon armour (189 ingots for a full set) `[doc]`, scales by brushing, Dragon Horn storage `[v]` |
| AC | A taming ritual per species, Elder-led packs `[v]` | Riding with a roar meter, Atlatitan rides of 10 min per Serene Salad `[v]` |
| Cobblemon | Pokédex entries, levels, ride styles `[doc]` | Alphas, Aprijuice ride tuning, berry mutations, habitats `[doc]` |
| MCA | Hearts, gifts, marriage `[doc]` | Family tree, village rules, taxes every 7 days, guards, bounty hunters `[v]` |

### Where another design may play better

**In game:** each row compares a decided Nublar rule with a rival rule that may play better. The rows are options for the project lead. They do not change section 11 or the plan. Items on the plan's "Not in Beta" list appear only as post-Beta options.

| # | Candidate | Nublar today | Rival (mod) | Player gain | Cost | Recommendation |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | Owner at hatch | Taming only, no owner at hatch (section 11.10, O3) | Imprint on the nearest player (FAR, AC). Owner stored on the egg (IaF CE) | A bond at the hatch reveal | Weakens taming as a later goal. Lab carnivores become safe at once | Adopt-in-part |
| 2 | Stress model | Two inputs: food and water, company (section 11.10) | FAR mood: two periodic inputs, event changes, five behaviour bands | Clear levers and legible states | FAR's plant scan is costly. Enclosure comfort is out of Beta | Adopt-in-part |
| 3 | Park view | Free-roaming paddocks only. Enclosure marker and tablet not in Beta (section 11.13) | Cobblemon Pasture Block. MCA Blueprint roster | One screen lists every animal | Release and recall break fence containment. Backlog items | Keep (post-Beta option) |
| 4 | Readout for survival players | Debug stick for operators only (plan B16, O4) | FAR Dinopedia. Cobblemon Pokédex scanner | Stress and needs become visible | One item and one screen. The tablet is not in Beta | Consider |
| 5 | Growth stages | Smooth size, no steps (section 11.9) | IaF CE stages with smooth size inside each stage. FAR labels | Milestones in a 45-day wait | Small if labels only. Real size steps conflict with the hitbox design | Adopt-in-part |
| 6 | Feeder storage | Item slots in a feeder screen (section 11.12, plan B20) | FAR point pools per diet | Many animals eat from one feeder | One integer per pool. Already rated B20.2 | Adopt-in-part |
| 7 | Hatch timing | Fixed incubator timer, per species (section 11.7) | AC random-tick cracking with bedding bonus | Suspense | High variance, depends on chunk loading, devalues bulb upgrades | Keep |
| 8 | Command input | Follow, stay, defend cycled by owner use (section 11.10, B19.8) | FAR order item per species. FAR whip taming | Commands never clash with feeding | Item clutter. Whip taming conflicts with calm taming | Consider |
| 9 | Failure outcome | Incubation failure and hatch failure leave a broken egg and remains (section 11.6, plan B11) | FAR Culture Vat failure spawns a mob | Drama | Vanilla-feel conflict, machine loss (B11.7 Drop) | Keep |
| 10 | Riding dinosaurs | Not in Beta. Vehicles only (section 11.12) | Cobblemon ride styles, FAR whip riding, AC mounts | The strongest late hook in four rivals | Large scope: seats, controls, packets, ridden clips | Keep (post-Beta option) |
| 11 | Starvation floor | Lethal starvation, 1 health per 80 ticks (section 11.10) | FAR stops at half health unless a config enables death | Fewer losses for new or absent players | Weaker consequence. Conflicts with the fence-attack trigger if on | Consider |
| 12 | Growth item | No item changes growth (section 11.9) | FAR Chicken Essence, IaF CE Dragon Meal: +1 day each | A player lever on the longest wait | Cuts the decided slow growth. One item and recipe | Consider |
| 13 | Sure first clone | Fossil luck and processing time (section 11.1) | Cobblemon dig site with one guaranteed fossil | An earlier first creature | Loot design work. Must not make a part loot-only (section 11.8) | Consider |
| 14 | Lab hatch success | One species hatch success for every placed egg (section 11.7, plan B11) | Lab and natural eggs always hatch in FAR, AC, IaF CE | First Tyrannosaurus after 1 egg, not about 20 | Reads against the card model that the lead selected | Consider |
| 15 | Taming effort | One check: calm and low stress (section 11.10) | AC attempt counter, MCA hearts, Cobblemon friendship | Visible progress toward a tame | One more threshold on the planned reputation value | Adopt-in-part |
| 16 | One fossil per run | One fossil per run is the intended early feel (section 11.4) | FAR Analyzer with 9 input slots | Less early tending | Conflicts with the decided feel. Hoppers already help | Keep |

**1. Owner at hatch.** Section 11.10 lets a player tame only a calm animal with low stress, and O3 defaults to no owner. FAR imprints the nearest player within 16 blocks `[v]`. AC imprints within 10 blocks for 3 of 7 species `[v]`. IaF CE stores the placer on the egg `[v]` (`fossils_revival_and_ice_and_fire.md` Q2, Q12, `alexs_caves_dinosaurs.md` KQ1). The player gains a bond at the moment of the variation reveal. A full tame at hatch makes a lab Tyrannosaurus safe for its owner and removes taming as a goal. Option: store the placer on the egg, as IaF CE does, and give that player a reputation head start (candidate 15). The animal still tames only when calm. This also answers O3 without the "nearest player" doubt in a busy lab.

**2. Stress model.** Section 11.10 sets two stress inputs, and B17.5 keeps stress as one integer with drift. FAR mood has two periodic inputs (plants nearby, crowding) and event changes (eat +5, kill +25, hurt −5) `[v]`. Five named bands gate breeding, player attacks and loyalty `[v]` (`fossils_revival_and_ice_and_fire.md` Q4). Event changes give the player direct levers, such as hand-feeding or a live-bait hunt. Named bands make stress readable with candidate 4. FAR's plant scan reads up to 18,513 blocks per check `[v]`, and enclosure comfort is out of Beta. Option: add event changes and named bands to the decided inputs. Skip the plant input. A crowding side of the company input overlaps O6.

**3. Park view.** Section 11.13 puts the enclosure marker and the tablet outside Beta. Cobblemon's Pasture Block releases and recalls up to 16 creatures `[doc]`. MCA's Blueprint lists villagers and village rules `[doc]` (`cobblemon_wiki.md` key question 4, `other_mod_wikis.md` MCA). A roster screen saves walking a large park. Release and recall would bypass the fences, which are Nublar's signature system. Keep paddocks for Beta. A post-Beta tablet or enclosure marker can carry an MCA-style roster.

**4. Readout for survival players.** Plan B16 limits the debug status to creative mode and operators, and O4 defaults to no readout. The FAR Dinopedia shows age, health, hunger, mood, order and foods `[doc]`, and the Cobblemon Pokédex scans name, gender, level and owner `[doc]`. The FAR note states that mood is invisible without the Dinopedia (`fossils_revival_and_ice_and_fire.md` Q7). The same holds for Nublar stress. The two-byte status that B16.6 already syncs can feed an owner readout at small cost. The tablet and the guide book stay post-Beta. Consider a minimal readout under O4.

**5. Growth stages.** Section 11.9 decides smooth size, and the hitbox design depends on it. IaF CE steps its stage every 25 days but keeps size smooth inside each stage `[v]`. FAR keeps size smooth and adds baby and teen labels as behaviour gates `[v]` (`fossils_revival_and_ice_and_fire.md` Q3, Q11). Named stages give milestones such as "can hunt" or "can breed" during a 45-day wait. Option: name stages on the growth fraction as labels and gates only. Size, hitbox and stats stay smooth, so the hitbox design does not change.

**6. Feeder storage.** Plan B20 fixes the slot code of the Carnivore Feeder. The FAR Feeder turns one item into food points every 5 ticks, up to 10,000 per pool. Each bite takes one point `[v]` (`fossils_revival_and_ice_and_fire.md` Q1). Many animals can eat from one feeder with no stack handling. The technique table already rates this B20.2 Adopt. Option: keep the slots as the input in the feeder screen and store food as points behind them.

**7. Hatch timing.** Section 11.7 decides a fixed incubator time per species, and B11.1 keeps it. AC eggs crack by random tick with a mean near 3.4 days and a long tail `[inf]` (`alexs_caves_dinosaurs.md` KQ1). Random time adds suspense but makes the decided 50-minute chain unpredictable. It also lowers the value of bulb upgrades and stops when the chunk unloads. Keep the fixed timer. The 3 visible crack stages are already in B11.2.

**8. Command input.** Section 11.10 decides follow, stay and defend, and B19.8 cycles them by owner use. FAR cycles orders with a species order item and tames with a whip that lowers mood `[v]`. AC asks for sneak-use on mounts `[v]`. A command item or sneak-use keeps a feeding click from changing the order. Whip taming lowers mood, so it conflicts with taming a calm animal. Consider one command item or sneak-use for B19.8. Reject whip taming.

**9. Failure outcome.** Section 11.6 decides incubation failure as the genome risk, and plan B11 leaves a broken egg and remains on a failed hatch. The FAR Culture Vat fails at 10 %, destroys itself and spawns a hostile mob `[v]`. The technique table rates this B11.7 Drop for vanilla feel. The useful part of FAR is that failure gives material back. Nublar's remains already process into DNA (plan B04). Keep the decided failure.

**10. Riding dinosaurs.** Section 11.12 decides vehicles and no riding. Cobblemon has 8 ride styles with stamina `[doc]`. FAR rides 15 species with a whip `[doc]`, and AC rides three species `[v]` (`cobblemon_architecture.md` Q3, `large_mob_infrastructure_and_vehicles.md` key question 5). Riding is the late payoff in four rivals. It needs seats, controls, a validated key packet and ridden clips. The Tyrannosaurus card lists no ridden clips (section 7.8). Keep it out of Beta. The B22 seats and key packet carry over to a post-Beta option.

**11. Starvation floor.** Section 11.10 makes starvation lethal at 1 health per 80 ticks. FAR deals damage at twice that rate but stops at half health unless `enableStarvation` is on. That switch is off by default `[v]` (`fossils_revival_and_ice_and_fire.md` Q4). A floor protects a new player and a player who leaves a park loaded. Section 11.10 uses starvation as a fence-attack trigger, and that trigger stays with a floor. Consider a server config floor that is off by default, so the decided rule stays the default.

**12. Growth item.** Section 11.9 decides slow growth, and a Tyrannosaurus takes 45 days. FAR Chicken Essence and IaF CE Dragon Meal each add one day of growth `[v]`, and FAR blocks the essence for apex species `[doc]`. An item gives the player a lever on the longest wait in the loop. It also cuts the slowness the lead chose. Consider a costly item with a config switch, and block it for the largest species as FAR does.

**13. Sure first clone.** Section 11.1 puts early effort in biome knowledge, processing time and quality luck. Each Cobblemon dig site guarantees one fossil `[doc]` (`cobblemon_wiki.md` fossil section). A digsite chest or a paleontologist trade with enough DNA for one small species would bring the first creature forward. Section 11.8 bans parts that come only from loot or trade, but DNA is not a part. Consider a guaranteed amount in the B21 digsite loot.

**14. Lab hatch success.** Section 11.7 applies a hatch success at the end. Plan B11 reads it from species data, with 5 % for the Tyrannosaurus. Section 11.16 selected the card model of a large clutch with a low hatch success. Applied to lab eggs, that model asks for about 20 printed eggs per live Tyrannosaurus `[inf]`. FAR, AC and IaF CE hatch every finished egg that meets its condition `[v]`. Consider confirming whether hatch success applies to nest eggs only, or adding a separate lab value. A roll at lay, which places only fertile nest eggs, keeps the card's expected young with fewer egg blocks `[inf]`.

**15. Taming effort.** Section 11.10 tames a calm animal with low stress, and B19.9 plans one reputation value per player. AC counts tame attempts and is sure on the ninth feed `[v]`. MCA raises hearts with each gift and limits spam with fatigue `[v]`. Cobblemon stores friendship from 0 to 255 `[doc]`. A counter shows progress and turns taming into a goal. Option: tame when the planned reputation passes a species threshold while the animal is calm. This needs no new state.

**16. One fossil per run.** Section 11.4 decides one fossil per run as the intended early feel, and plan B05 adds hopper input. The FAR Analyzer takes 9 items and runs each in 10 s `[v]`. Batch input reduces tending, but it conflicts with the decided feel. Hoppers already remove most tending. Keep the decided design. The pacing gap comes from run time, which chip upgrades already address.

## Post-Beta

Everything in this section is outside Beta. It lists the nearest precedent for each item in the plan's backlog or outside the plan. Nothing here is a Beta recommendation.

| Feature | Plan status | Nearest precedent | Note |
| --- | --- | --- | --- |
| Riding dinosaurs | Not in the plan | Cobblemon ride styles and seat locators `[v]` `[doc]`, FAR riding with a whip `[v]`, AC rideable Tremorsaurus `[v]` | B22 seats and the key packet carry over |
| Gyrosphere, monorail, aircraft, boats | Backlog | Cobblemon air and liquid ride styles `[doc]`, AC submarine `[v]` | The B22 vehicle base carries over |
| Enrichment toys | Not in Beta (no enclosure comfort, plan B17) | FAR toys: ball +15 mood, log +30, cooldown 600 to 1,200 ticks `[v]` | A block toy avoids the ball's physics cost `[inf]` |
| Enclosure marker and park signs | Backlog | None found | — |
| Tracking beacon | Backlog | IaF CE Summoning Crystal with saved positions `[v]` | — |
| Dinopedia or scanner | Not in the plan | FAR Dinopedia `[doc]`, Cobblemon Pokédex scanner `[doc]` | — |
| Security cameras | Not in the plan | AC Beholder `[v]` | — |
| Diseases and treatment | Backlog | None found | — |
| Pteranodon flocks | Backlog (Release 2) | AC Subterranodon flock chain: at most 5, side offsets by index `[v]` | boids view cones fit flight `[inf]` |
| Mod's own non-dinosaur animals | Backlog | Naturalist animals `[v]`, cosy-critters client-side ambient critters `[v]` | — |
| Dialogue NPCs | Not in Beta (B21.9) | Cobblemon dialogue files `[doc]`, MCA dialogue `[v]` | — |
| Status overlay through TES | Not in the plan | TES HUD element API `[v]` | Open item O4 comes first |
| Guide book, recipe viewer integration | Backlog | Cobblemon JEI API, compile only `[v]`, Naturalist optional Field Guide `[v]` | — |
| Gates and the overhanging fence | Backlog | None found | — |
| Turf wars | Backlog | None found | `StartTurfWar` stays disconnected (plan B18) |

## Conclusion

The surveyed mods prove one lesson again and again. The cost of a creature mod comes from searches that run every tick, packets that send everything, and render passes that repeat the model. Nublar has all three today, and a surveyed mod proves a small fix for each. Four changes remove most of the server and client cost before any new behaviour lands. They are the sensor registry, the machine packets, the fence caches and the colour bake. The hitbox design keeps its own budget, and this report leaves it unchanged.

For gameplay, the mods agree on a vanilla grammar. Commands work like a wolf, eggs like a turtle and retaliation like a bee, with a 5-minute breeding cooldown. Nublar fits that grammar with no conflict. Its signature systems have no precedent, but vanilla supplies their parts. These parts are the vibration listener, the visibility math of target selection, the Polar Bear warning stance and memories with expiry. The twelve design directions and the 27 specifications give each module owner a starting point. The six open items are the only new decisions for the lead, and each has a default.

## Sources

### Local documents and code

- [docs/project-plan.md](../docs/project-plan.md): modules B00 to B27, P2, decisions register D01 to D14, unassigned backlog.
- [docs/codebase-analysis.md](../docs/codebase-analysis.md), section 11 (11.1 to 11.17).
- [docs/dinosaur-hitboxes-and-movement-design.md](../docs/dinosaur-hitboxes-and-movement-design.md): decisions, performance budget (load A, load B), part buckets, bake step, IK.
- [docs/superpowers/specs/2026-10-08-dino-layer-baking-design.md](../docs/superpowers/specs/2026-10-08-dino-layer-baking-design.md): layer bake per genome key.
- [docs/reference-recommendations-review.md](../docs/reference-recommendations-review.md): verdict scale and table format.
- Local code at `kash's-slop` 0c97bde plus the uncommitted tree. Key paths (`J/` = `common/src/main/java/net/dumbcode/projectnublar/`): `J/entity/dinosaur/Dinosaur.java:594-616` (five sensors), `J/entity/ai/sensors/NearestWaterSourceSensor.java:45-55`, `J/util/LineUtils.java:17-41` (fence line walk), `J/entity/dinosaur/CarnivoreDinosaur.java:20-70`, `J/util/DinoNeedsUtils.java:78-99`, `J/menutypes/IncubatorMenu.java:46`, `J/item/DebugStick.java:73-109`. The full list is in the fit note below.

### Research notes (13 files)

All in `research_notes/Reference mod survey for Beta/`: `ai_library_layer.md`, `alexs_caves_dinosaurs.md`, `client_rendering_and_animation.md`, `cobblemon_architecture.md`, `cobblemon_wiki.md`, `creature_behaviour_mods.md`, `fossils_revival_and_ice_and_fire.md`, `large_mob_infrastructure_and_vehicles.md`, `other_mod_wikis.md`, `people_and_structures_mods.md`, `project_nublar_fit.md`, `vanilla_detection_and_stress_precedents.md`, `vanilla_plus_and_feeders.md`. Earlier repository facts come from `research_notes/Multipart entity hitbox systems/` and `research_notes/Multipart hitbox hybrid design/other_creature_mods.md`.

### Vanilla Minecraft

Vanilla `[v]` labels come from the vanilla detection note, which checked Minecraft 1.20.1 class structure and constants. The public Minecraft Wiki pages that the note cites:

- [Warden](https://minecraft.wiki/w/Warden), [Allay](https://minecraft.wiki/w/Allay), [Vibration](https://minecraft.wiki/w/Vibration), [Sculk Sensor](https://minecraft.wiki/w/Sculk_Sensor), [Calibrated Sculk Sensor](https://minecraft.wiki/w/Calibrated_Sculk_Sensor)
- [Sniffer](https://minecraft.wiki/w/Sniffer), [Enderman](https://minecraft.wiki/w/Enderman), [Polar Bear](https://minecraft.wiki/w/Polar_Bear), [Fox](https://minecraft.wiki/w/Fox), [Ocelot](https://minecraft.wiki/w/Ocelot), [Cat](https://minecraft.wiki/w/Cat)
- [Wolf](https://minecraft.wiki/w/Wolf), [Bee](https://minecraft.wiki/w/Bee), [Iron Golem](https://minecraft.wiki/w/Iron_Golem), [Zombified Piglin](https://minecraft.wiki/w/Zombified_Piglin), [Vex](https://minecraft.wiki/w/Vex), [Rabbit](https://minecraft.wiki/w/Rabbit)
- [Villager](https://minecraft.wiki/w/Villager), [Breeding](https://minecraft.wiki/w/Breeding), [Mob](https://minecraft.wiki/w/Mob), [Attribute](https://minecraft.wiki/w/Attribute), [Sneaking](https://minecraft.wiki/w/Sneaking)

### Alex's Caves dinosaur behaviour (paths at 0ab3c214)

The dinosaur note cites paths under `src/main/java/com/github/alexmodguy/alexscaves/server/` at commit 0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5:

- [`block/DinosaurEggBlock.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/block/DinosaurEggBlock.java)
- [`entity/living/VallumraptorEntity.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/VallumraptorEntity.java)
- [`entity/living/TremorsaurusEntity.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/TremorsaurusEntity.java)
- [`entity/living/GrottoceratopsEntity.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/GrottoceratopsEntity.java)
- [`entity/living/SubterranodonEntity.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SubterranodonEntity.java)
- [`entity/living/AtlatitanEntity.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/AtlatitanEntity.java)
- [`entity/living/DinosaurEntity.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/DinosaurEntity.java)
- [`entity/util/PackAnimal.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/util/PackAnimal.java)
- [`entity/ai/AnimalJoinPackGoal.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/ai/AnimalJoinPackGoal.java)
- [`event/CommonEvents.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/event/CommonEvents.java)
- [Alex's Caves wiki](https://alexscaves.wiki.gg/wiki/Tremorsaurus) pages for Tremorsaurus, Vallumraptor, Grottoceratops and Subterranodon `[doc]`

### Repository files at the commits read

Each link points at the file at the commit in "Repository facts". The notes give line ranges for each claim.

**SBL @437898f**

- [`LICENSE`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/LICENSE)
- [`ExtendedSensor.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/sensor/ExtendedSensor.java)
- [`MobMixin.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/mixin/common/MobMixin.java)
- [`SmartBrain.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/internal/SmartBrain.java)
- [`NearbyBlocksSensor.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/sensor/custom/NearbyBlocksSensor.java)
- [`NearestBabyBedSensor.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/sensor/vanilla/NearestBabyBedSensor.java)
- [`NearbyLivingEntitySensor.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/sensor/vanilla/NearbyLivingEntitySensor.java)
- [`NearestVisibleEntityFilteredSensor.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/sensor/base/NearestVisibleEntityFilteredSensor.java)
- [`NearestItemSensor.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/sensor/vanilla/NearestItemSensor.java)
- [`InvalidateAttackTarget.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/behaviour/custom/target/InvalidateAttackTarget.java)
- [`UnreachableTargetSensor.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/sensor/custom/UnreachableTargetSensor.java)
- [`SmartBrainSchedule.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/schedule/SmartBrainSchedule.java)
- [`FollowParent.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/behaviour/custom/move/FollowParent.java)
- [`PredicateSensor.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/sensor/base/PredicateSensor.java)
- [`ActivityBuilder.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/api/core/ActivityBuilder.java)
- [`BrainUtil.java`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/common/src/main/java/net/tslat/smartbrainlib/util/BrainUtil.java)
- [`CHANGELOG.md`](https://github.com/Tslat/SmartBrainLib/blob/437898f0900808cf0f163b0a36bc56e143264375/CHANGELOG.md)

**GeckoLib 5 @1e09655**

- [`LICENSE`](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/LICENSE)
- [`AnimationController.java`](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/animation/AnimationController.java)
- [`GeoRenderer.java`](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/renderer/base/GeoRenderer.java)
- [`GeoEntityRenderer.java`](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/renderer/GeoEntityRenderer.java)
- [`TextureLayerGeoLayer.java`](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/renderer/layer/builtin/TextureLayerGeoLayer.java)
- [`CustomBoneTextureGeoLayer.java`](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/renderer/layer/builtin/CustomBoneTextureGeoLayer.java)
- [`AutoGlowingGeoLayer.java`](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/renderer/layer/builtin/AutoGlowingGeoLayer.java)
- [`StatelessEntityPlayAnimPacket.java`](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/network/packet/entity/StatelessEntityPlayAnimPacket.java)
- [`AnimationPoint.java`](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/animation/state/AnimationPoint.java)
- [`DataTickets.java`](https://github.com/bernie-g/geckolib/blob/1e09655d3b54c2db959d234266fb564b90aa9382/common/src/main/java/com/geckolib/constant/DataTickets.java)

**EMF @622a0cba**

- [`EMFLODHandler.java`](https://github.com/Traben-0/Entity_Model_Features/blob/622a0cba8c2e3eeef896b5a9dbcb8d94600f3cd7/src/main/java/traben/entity_model_features/utils/EMFLODHandler.java)
- [`EMFAnimationPauseHandler.java`](https://github.com/Traben-0/Entity_Model_Features/blob/622a0cba8c2e3eeef896b5a9dbcb8d94600f3cd7/src/main/java/traben/entity_model_features/utils/EMFAnimationPauseHandler.java)
- [`EMFConfig.java`](https://github.com/Traben-0/Entity_Model_Features/blob/622a0cba8c2e3eeef896b5a9dbcb8d94600f3cd7/src/main/java/traben/entity_model_features/config/EMFConfig.java)
- [`README.md`](https://github.com/Traben-0/Entity_Model_Features/blob/622a0cba8c2e3eeef896b5a9dbcb8d94600f3cd7/README.md)

**AzureLib @ba4020b**

- [`LICENSE`](https://github.com/AzureDoom/AzureLib/blob/ba4020bf2ae4950bca71c46df55df6b55e70cda1/LICENSE)
- [`AzLodConfig.java`](https://github.com/AzureDoom/AzureLib/blob/ba4020bf2ae4950bca71c46df55df6b55e70cda1/common/src/main/java/mod/azure/azurelib/render/lod/AzLodConfig.java)
- [`AzLodManager.java`](https://github.com/AzureDoom/AzureLib/blob/ba4020bf2ae4950bca71c46df55df6b55e70cda1/common/src/main/java/mod/azure/azurelib/render/lod/AzLodManager.java)
- [`entity_smartbrainlib.mdx`](https://github.com/AzureDoom/AzureLib/blob/ba4020bf2ae4950bca71c46df55df6b55e70cda1/docs/azurelib/entities/entity_smartbrainlib.mdx)
- [`changelog.md`](https://github.com/AzureDoom/AzureLib/blob/ba4020bf2ae4950bca71c46df55df6b55e70cda1/changelog.md)

**Aquamirae @a468d77**

- [`AnimController.java`](https://github.com/ObscuriaLithium/aquamirae/blob/a468d77cc443a932d56a7aa34303f4e435eca3b6/common/src/main/java/dev/obscuria/aquamirae/common/entity/animation/AnimController.java)
- [`ClientboundAnimationPlayPayload.java`](https://github.com/ObscuriaLithium/aquamirae/blob/a468d77cc443a932d56a7aa34303f4e435eca3b6/common/src/main/java/dev/obscuria/aquamirae/network/ClientboundAnimationPlayPayload.java)
- [`build.gradle`](https://github.com/ObscuriaLithium/aquamirae/blob/a468d77cc443a932d56a7aa34303f4e435eca3b6/build.gradle)

**AC @0ab3c214**

- [`gradle.properties`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/gradle.properties)
- [`AbstractMinecartMixin.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/mixin/AbstractMinecartMixin.java)
- [`MagneticLevitationRailBlock.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/block/MagneticLevitationRailBlock.java)
- [`SubmarineEntity.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/item/SubmarineEntity.java)
- [`SubmarineItem.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/item/SubmarineItem.java)
- [`MountedEntityKeyMessage.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/message/MountedEntityKeyMessage.java)
- [`NuclearFurnaceBlockEntity.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/block/blockentity/NuclearFurnaceBlockEntity.java)
- [`NuclearFurnaceComponentBlock.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/block/NuclearFurnaceComponentBlock.java)
- [`TeslaBulbBlockEntity.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/block/blockentity/TeslaBulbBlockEntity.java)
- [`HologramProjectorBlockEntity.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/block/blockentity/HologramProjectorBlockEntity.java)
- [`RaygunItem.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/item/RaygunItem.java)
- [`EntityMixin.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/mixin/EntityMixin.java)
- [`ACClientConfig.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/client/config/ACClientConfig.java)
- [`SauropodBaseEntity.java`](https://github.com/AlexModGuy/AlexsCaves/blob/0ab3c214b72c846c2c1ea713a37e2b4b3a00a9f5/src/main/java/com/github/alexmodguy/alexscaves/server/entity/living/SauropodBaseEntity.java)

**AM @d5ec3a1**

- [`gradle.properties`](https://github.com/AlexModGuy/AlexsMobs/blob/d5ec3a16e2a1fba84610021f110b46b8b3e1db0e/gradle.properties)
- [`CreatureAITargetItems.java`](https://github.com/AlexModGuy/AlexsMobs/blob/d5ec3a16e2a1fba84610021f110b46b8b3e1db0e/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/CreatureAITargetItems.java)
- [`AnimalAIHerdPanic.java`](https://github.com/AlexModGuy/AlexsMobs/blob/d5ec3a16e2a1fba84610021f110b46b8b3e1db0e/src/main/java/com/github/alexthe666/alexsmobs/entity/ai/AnimalAIHerdPanic.java)
- [`MessageUpdateEagleControls.java`](https://github.com/AlexModGuy/AlexsMobs/blob/d5ec3a16e2a1fba84610021f110b46b8b3e1db0e/src/main/java/com/github/alexthe666/alexsmobs/message/MessageUpdateEagleControls.java)
- [`AMConfig.java`](https://github.com/AlexModGuy/AlexsMobs/blob/d5ec3a16e2a1fba84610021f110b46b8b3e1db0e/src/main/java/com/github/alexthe666/alexsmobs/config/AMConfig.java)

**Citadel @badebf6**

- [`gradle.properties`](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/gradle.properties)
- [`LevelMixin.java`](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/mixin/LevelMixin.java)
- [`TickRateTracker.java`](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/tick/TickRateTracker.java)
- [`LegSolver.java`](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/animation/LegSolver.java)
- [`AnimationMessage.java`](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/message/AnimationMessage.java)
- [`PropertiesMessage.java`](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/message/PropertiesMessage.java)
- [`Pathfinding.java`](https://github.com/AlexModGuy/Citadel/blob/badebf6505f56650fd770887329d82b1a88f3f79/src/main/java/com/github/alexthe666/citadel/server/entity/pathfinding/raycoms/Pathfinding.java)

**Mowzie @64d1524**

- [`License.txt`](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/License.txt)
- [`Ability.java`](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ability/Ability.java)
- [`AbilitySection.java`](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ability/AbilitySection.java)
- [`MeleeAttackAbility.java`](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ability/abilities/mob/MeleeAttackAbility.java)
- [`MessageUseAbility.java`](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/message/MessageUseAbility.java)
- [`AnimationAttackAI.java`](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/ai/animation/AnimationAttackAI.java)
- [`MowzieEntity.java`](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/entity/MowzieEntity.java)

**FAR @b400cf2**

- [`LICENSE`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/LICENSE)
- [`FeederBlockEntity.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/block/entity/FeederBlockEntity.java)
- [`EatFromFeederGoal.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/EatFromFeederGoal.java)
- [`CacheMoveToBlockGoal.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/CacheMoveToBlockGoal.java)
- [`DinosaurEgg.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/DinosaurEgg.java)
- [`MoodSystem.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/system/MoodSystem.java)
- [`SleepSystem.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/system/SleepSystem.java)
- [`OrderType.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/OrderType.java)
- [`DinoFollowOwnerGoal.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/DinoFollowOwnerGoal.java)
- [`CultureVatBlockEntity.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/block/entity/CultureVatBlockEntity.java)
- [`AnalyzerBlockEntity.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/block/entity/AnalyzerBlockEntity.java)
- [`Prehistoric.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/prehistoric/base/Prehistoric.java)
- [`PlayGoal.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/PlayGoal.java)
- [`AnimalFearGoal.java`](https://github.com/TeamFossilsArcheology/FossilsArcheologyRevival/blob/b400cf2/common/src/main/java/com/github/teamfossilsarcheology/fossil/entity/ai/AnimalFearGoal.java)

**IaF CE @55c5dec**

- [`LICENSE`](https://github.com/IAFEnvoy/IceAndFire-CE/blob/55c5dec/LICENSE)
- [`DragonBaseEntity.java`](https://github.com/IAFEnvoy/IceAndFire-CE/blob/55c5dec/src/main/java/com/iafenvoy/iceandfire/entity/DragonBaseEntity.java)
- [`IafDragonLogic.java`](https://github.com/IAFEnvoy/IceAndFire-CE/blob/55c5dec/src/main/java/com/iafenvoy/iceandfire/entity/util/dragon/IafDragonLogic.java)
- [`DragonHornItem.java`](https://github.com/IAFEnvoy/IceAndFire-CE/blob/55c5dec/src/main/java/com/iafenvoy/iceandfire/item/DragonHornItem.java)
- [`DragonEggEntity.java`](https://github.com/IAFEnvoy/IceAndFire-CE/blob/55c5dec/src/main/java/com/iafenvoy/iceandfire/entity/DragonEggEntity.java)
- [`DragonAITargetItemsGoal.java`](https://github.com/IAFEnvoy/IceAndFire-CE/blob/55c5dec/src/main/java/com/iafenvoy/iceandfire/entity/ai/DragonAITargetItemsGoal.java)
- [`HomePosition.java`](https://github.com/IAFEnvoy/IceAndFire-CE/blob/55c5dec/src/main/java/com/iafenvoy/iceandfire/entity/util/HomePosition.java)

**Naturalist @26d1bf8**

- [`LICENSE`](https://github.com/crispytwig/Naturalist/blob/26d1bf8/LICENSE)
- [`Deer.java`](https://github.com/crispytwig/Naturalist/blob/26d1bf8/common/src/main/java/com/crispytwig/naturalist/world/entity/animal/deer/Deer.java)
- [`BigPanicGoal.java`](https://github.com/crispytwig/Naturalist/blob/26d1bf8/common/src/main/java/com/crispytwig/naturalist/world/entity/ai/goal/BigPanicGoal.java)
- [`LayEggGoal.java`](https://github.com/crispytwig/Naturalist/blob/26d1bf8/common/src/main/java/com/crispytwig/naturalist/world/entity/ai/goal/LayEggGoal.java)
- [`EggLayingBreedGoal.java`](https://github.com/crispytwig/Naturalist/blob/26d1bf8/common/src/main/java/com/crispytwig/naturalist/world/entity/ai/goal/EggLayingBreedGoal.java)
- [`SleepGoal.java`](https://github.com/crispytwig/Naturalist/blob/26d1bf8/common/src/main/java/com/crispytwig/naturalist/world/entity/ai/goal/SleepGoal.java)
- [`HuntingAnimal.java`](https://github.com/crispytwig/Naturalist/blob/26d1bf8/common/src/main/java/com/crispytwig/naturalist/world/entity/HuntingAnimal.java)
- [`Tiger.java`](https://github.com/crispytwig/Naturalist/blob/26d1bf8/common/src/main/java/com/crispytwig/naturalist/world/entity/animal/tiger/Tiger.java)
- [`Rhino.java`](https://github.com/crispytwig/Naturalist/blob/26d1bf8/common/src/main/java/com/crispytwig/naturalist/world/entity/animal/rhino/Rhino.java)
- [`AttackPlayerNearBabiesGoal.java`](https://github.com/crispytwig/Naturalist/blob/26d1bf8/common/src/main/java/com/crispytwig/naturalist/world/entity/ai/goal/AttackPlayerNearBabiesGoal.java)
- [`CHANGELOG.md`](https://github.com/crispytwig/Naturalist/blob/26d1bf8/CHANGELOG.md)

**RevampedWolf @9b8f0a6**

- [`LICENSE`](https://github.com/baguchi/RevampedWolf/blob/9b8f0a6/LICENSE)
- [`WolfMixin.java`](https://github.com/baguchi/RevampedWolf/blob/9b8f0a6/src/main/java/baguchan/revampedwolf/mixin/WolfMixin.java)
- [`HuntTargetGoal.java`](https://github.com/baguchi/RevampedWolf/blob/9b8f0a6/src/main/java/baguchan/revampedwolf/entity/goal/HuntTargetGoal.java)
- [`MoveToMeatGoal.java`](https://github.com/baguchi/RevampedWolf/blob/9b8f0a6/src/main/java/baguchan/revampedwolf/entity/goal/MoveToMeatGoal.java)
- [`LeapAtTargetWolfGoal.java`](https://github.com/baguchi/RevampedWolf/blob/9b8f0a6/src/main/java/baguchan/revampedwolf/entity/goal/LeapAtTargetWolfGoal.java)

**FAF @184a7c3**

- [`LICENSE.txt`](https://github.com/Faboslav/friends-and-foes/blob/184a7c3/LICENSE.txt)
- [`TuffGolemBrain.java`](https://github.com/Faboslav/friends-and-foes/blob/184a7c3/common/src/main/java/com/faboslav/friendsandfoes/common/entity/ai/brain/TuffGolemBrain.java)
- [`TuffGolemSleepTask.java`](https://github.com/Faboslav/friends-and-foes/blob/184a7c3/common/src/main/java/com/faboslav/friendsandfoes/common/entity/ai/brain/task/tuffgolem/TuffGolemSleepTask.java)
- [`GlareBrain.java`](https://github.com/Faboslav/friends-and-foes/blob/184a7c3/common/src/main/java/com/faboslav/friendsandfoes/common/entity/ai/brain/GlareBrain.java)
- [`CrabBreedTask.java`](https://github.com/Faboslav/friends-and-foes/blob/184a7c3/common/src/main/java/com/faboslav/friendsandfoes/common/entity/ai/brain/task/crab/CrabBreedTask.java)
- [`VillagerHostilesSensorMixin.java`](https://github.com/Faboslav/friends-and-foes/blob/184a7c3/common/src/main/java/com/faboslav/friendsandfoes/common/mixin/VillagerHostilesSensorMixin.java)
- [`StructurePoolHelper.java`](https://github.com/Faboslav/friends-and-foes/blob/184a7c3/common/src/main/java/com/faboslav/friendsandfoes/common/util/StructurePoolHelper.java)
- [`FriendsAndFoesFabricGameTests.java`](https://github.com/Faboslav/friends-and-foes/blob/184a7c3/fabric/src/main/java/com/faboslav/friendsandfoes/fabric/tests/FriendsAndFoesFabricGameTests.java)

**Cobblemon @2780f491**

- [`LICENSE`](https://gitlab.com/cable-mc/cobblemon/-/blob/2780f491d6023936ea1d4b75fc4430f43fea272d/LICENSE)
- [`HorseBehaviour.kt`](https://gitlab.com/cable-mc/cobblemon/-/blob/2780f491d6023936ea1d4b75fc4430f43fea272d/common/src/main/kotlin/com/cobblemon/mod/common/api/riding/behaviour/types/land/HorseBehaviour.kt)
- [`DriverInputPacketHandler.kt`](https://gitlab.com/cable-mc/cobblemon/-/blob/2780f491d6023936ea1d4b75fc4430f43fea272d/common/src/main/kotlin/com/cobblemon/mod/common/net/serverhandling/riding/DriverInputPacketHandler.kt)
- [`HerdBehaviour.kt`](https://gitlab.com/cable-mc/cobblemon/-/blob/2780f491d6023936ea1d4b75fc4430f43fea272d/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/ai/HerdBehaviour.kt)
- [`FollowHerdLeaderTask.kt`](https://gitlab.com/cable-mc/cobblemon/-/blob/2780f491d6023936ea1d4b75fc4430f43fea272d/common/src/main/kotlin/com/cobblemon/mod/common/entity/ai/FollowHerdLeaderTask.kt)
- [`FindHerdLeaderTaskConfig.kt`](https://gitlab.com/cable-mc/cobblemon/-/blob/2780f491d6023936ea1d4b75fc4430f43fea272d/common/src/main/kotlin/com/cobblemon/mod/common/api/ai/config/task/FindHerdLeaderTaskConfig.kt)
- [`PokemonEntity.kt`](https://gitlab.com/cable-mc/cobblemon/-/blob/2780f491d6023936ea1d4b75fc4430f43fea272d/common/src/main/kotlin/com/cobblemon/mod/common/entity/pokemon/PokemonEntity.kt)
- [`JsonDataRegistry.kt`](https://gitlab.com/cable-mc/cobblemon/-/blob/2780f491d6023936ea1d4b75fc4430f43fea272d/common/src/main/kotlin/com/cobblemon/mod/common/api/data/JsonDataRegistry.kt)
- [`CobblemonBreeding.kt`](https://gitlab.com/cable-mc/cobblemon/-/blob/5a84b4b4b42a9eef77d0cc85dd9602f096681e53/common/src/main/kotlin/com/cobblemon/mod/common/api/breeding/CobblemonBreeding.kt)
- [`CHANGELOG-1.7.0.md`](https://gitlab.com/cable-mc/cobblemon/-/blob/2780f491d6023936ea1d4b75fc4430f43fea272d/changelogs/CHANGELOG-1.7.0.md)

**MCA @2de0738**

- [`LICENSE`](https://github.com/Luke100000/minecraft-comes-alive/blob/2de0738ecabf262865d1aba5f94fa0e49d02d07b/LICENSE)
- [`ProfessionsMCA.java`](https://github.com/Luke100000/minecraft-comes-alive/blob/2de0738ecabf262865d1aba5f94fa0e49d02d07b/common/src/main/java/net/conczin/mca/registry/ProfessionsMCA.java)
- [`MoodGroup.java`](https://github.com/Luke100000/minecraft-comes-alive/blob/2de0738ecabf262865d1aba5f94fa0e49d02d07b/common/src/main/java/net/conczin/mca/entity/ai/MoodGroup.java)
- [`Memories.java`](https://github.com/Luke100000/minecraft-comes-alive/blob/2de0738ecabf262865d1aba5f94fa0e49d02d07b/common/src/main/java/net/conczin/mca/entity/ai/Memories.java)
- [`VillagerBrain.java`](https://github.com/Luke100000/minecraft-comes-alive/blob/2de0738ecabf262865d1aba5f94fa0e49d02d07b/common/src/main/java/net/conczin/mca/entity/ai/brain/VillagerBrain.java)
- [`LazyFindPointOfInterestTask.java`](https://github.com/Luke100000/minecraft-comes-alive/blob/2de0738ecabf262865d1aba5f94fa0e49d02d07b/common/src/main/java/net/conczin/mca/entity/ai/brain/tasks/LazyFindPointOfInterestTask.java)
- [`VillageProcreationManager.java`](https://github.com/Luke100000/minecraft-comes-alive/blob/2de0738ecabf262865d1aba5f94fa0e49d02d07b/common/src/main/java/net/conczin/mca/server/world/data/villageComponents/VillageProcreationManager.java)
- [`SpawnQueue.java`](https://github.com/Luke100000/minecraft-comes-alive/blob/2de0738ecabf262865d1aba5f94fa0e49d02d07b/common/src/main/java/net/conczin/mca/server/SpawnQueue.java)

**GV @9074a04**

- [`LICENSE`](https://github.com/seymourimadeit/guardvillagers/blob/9074a04e3c95c2466059809250d847bcf54930e1/LICENSE)
- [`gradle.properties`](https://github.com/seymourimadeit/guardvillagers/blob/9074a04e3c95c2466059809250d847bcf54930e1/gradle.properties)
- [`SinglePoolElementMixin.java`](https://github.com/seymourimadeit/guardvillagers/blob/9074a04e3c95c2466059809250d847bcf54930e1/src/main/java/tallestegg/guardvillagers/mixins/SinglePoolElementMixin.java)
- [`Guard.java`](https://github.com/seymourimadeit/guardvillagers/blob/9074a04e3c95c2466059809250d847bcf54930e1/src/main/java/tallestegg/guardvillagers/common/entities/Guard.java)
- [`GuardConfig.java`](https://github.com/seymourimadeit/guardvillagers/blob/9074a04e3c95c2466059809250d847bcf54930e1/src/main/java/tallestegg/guardvillagers/configuration/GuardConfig.java)

**Illager Invasion @fd20e59**

- [`gradle.properties`](https://github.com/Fuzss/illager-invasion/blob/ee2743ef08ff1e613d61b09095625ca75f18ea35/gradle.properties)
- [`metadata.json`](https://github.com/Fuzss/illager-invasion/blob/fd20e595f8c1125e4dde15215f1d26f884546f1d/metadata.json)
- [`ModStructures.java`](https://github.com/Fuzss/illager-invasion/blob/fd20e595f8c1125e4dde15215f1d26f884546f1d/Common/src/main/java/fuzs/illagerinvasion/common/init/ModStructures.java)
- [`ModStructureSets.java`](https://github.com/Fuzss/illager-invasion/blob/fd20e595f8c1125e4dde15215f1d26f884546f1d/Common/src/main/java/fuzs/illagerinvasion/common/init/ModStructureSets.java)
- [`ModTemplatePools.java`](https://github.com/Fuzss/illager-invasion/blob/fd20e595f8c1125e4dde15215f1d26f884546f1d/Common/src/main/java/fuzs/illagerinvasion/common/init/ModTemplatePools.java)
- [`DataMarkerStructureProcessor.java`](https://github.com/Fuzss/illager-invasion/blob/fd20e595f8c1125e4dde15215f1d26f884546f1d/Common/src/main/java/fuzs/illagerinvasion/common/world/level/levelgen/structure/templatesystem/DataMarkerStructureProcessor.java)
- [`ModChestLootProvider.java`](https://github.com/Fuzss/illager-invasion/blob/fd20e595f8c1125e4dde15215f1d26f884546f1d/Common/src/main/java/fuzs/illagerinvasion/common/data/loot/ModChestLootProvider.java)
- [`Basher.java`](https://github.com/Fuzss/illager-invasion/blob/fd20e595f8c1125e4dde15215f1d26f884546f1d/Common/src/main/java/fuzs/illagerinvasion/common/world/entity/monster/Basher.java)
- [`VillagerGoalHandler.java`](https://github.com/Fuzss/illager-invasion/blob/fd20e595f8c1125e4dde15215f1d26f884546f1d/Common/src/main/java/fuzs/illagerinvasion/common/handler/VillagerGoalHandler.java)
- [`ModEnumConstants.java`](https://github.com/Fuzss/illager-invasion/blob/fd20e595f8c1125e4dde15215f1d26f884546f1d/Common/src/main/java/fuzs/illagerinvasion/common/init/ModEnumConstants.java)

**BV @d1ea77d**

- [`README.md`](https://github.com/jtorleon-studios-team/better-villages/blob/d1ea77dae5e6a562e4cda6376bab849fa126fc51/README.md)
- [`license.txt`](https://github.com/jtorleon-studios-team/better-villages/blob/d1ea77dae5e6a562e4cda6376bab849fa126fc51/license.txt)

**AFT @ad13f16**

- [`LICENSE`](https://github.com/Slexom/animal-feeding-trough/blob/ad13f16a8d5ff6c4af4b34d5c562b749ca12fb83/LICENSE)
- [`neoforge.mods.toml`](https://github.com/Slexom/animal-feeding-trough/blob/ad13f16a8d5ff6c4af4b34d5c562b749ca12fb83/neoforge/src/main/resources/META-INF/neoforge.mods.toml)
- [`FeedingTroughBlockEntity.java`](https://github.com/Slexom/animal-feeding-trough/blob/86392c70ade8a96ad5cab1aaa3dc9e8013f10a90/common/src/main/java/slexom/animal_feeding_trough/platform/common/block/entity/FeedingTroughBlockEntity.java)
- [`FeedingTroughSensor.java`](https://github.com/Slexom/animal-feeding-trough/blob/ad13f16a8d5ff6c4af4b34d5c562b749ca12fb83/common/src/main/java/slexom/animal_feeding_trough/platform/common/world/entity/ai/sensing/FeedingTroughSensor.java)
- [`SelfFeedGoal.java`](https://github.com/Slexom/animal-feeding-trough/blob/86392c70ade8a96ad5cab1aaa3dc9e8013f10a90/common/src/main/java/slexom/animal_feeding_trough/platform/common/goal/entity/ai/SelfFeedGoal.java)
- [`SelfFeed.java`](https://github.com/Slexom/animal-feeding-trough/blob/ad13f16a8d5ff6c4af4b34d5c562b749ca12fb83/common/src/main/java/slexom/animal_feeding_trough/platform/common/world/entity/ai/behavior/SelfFeed.java)
- [`AnimalEntityMixin.java`](https://github.com/Slexom/animal-feeding-trough/blob/86392c70ade8a96ad5cab1aaa3dc9e8013f10a90/common/src/main/java/slexom/animal_feeding_trough/platform/common/mixin/AnimalEntityMixin.java)

**Mutant Monsters @0404e1d**

- [`LICENSE-ASSETS.md`](https://github.com/Fuzss/mutant-monsters/blob/0404e1dc59bedef60c8835940dd141b7a4fedc10/LICENSE-ASSETS.md)
- [`EntityAnimation.java`](https://github.com/Fuzss/mutant-monsters/blob/0404e1dc59bedef60c8835940dd141b7a4fedc10/Common/src/main/java/fuzs/mutantmonsters/common/world/entity/animation/EntityAnimation.java)
- [`ClientboundAnimationMessage.java`](https://github.com/Fuzss/mutant-monsters/blob/0404e1dc59bedef60c8835940dd141b7a4fedc10/Common/src/main/java/fuzs/mutantmonsters/common/network/ClientboundAnimationMessage.java)
- [`AdditionalSpawnDataEntity.java`](https://github.com/Fuzss/mutant-monsters/blob/0404e1dc59bedef60c8835940dd141b7a4fedc10/Common/src/main/java/fuzs/mutantmonsters/common/world/entity/animation/AdditionalSpawnDataEntity.java)
- [`AnimationGoal.java`](https://github.com/Fuzss/mutant-monsters/blob/0404e1dc59bedef60c8835940dd141b7a4fedc10/Common/src/main/java/fuzs/mutantmonsters/common/world/entity/ai/goal/AnimationGoal.java)
- [`MutantMeleeAttackGoal.java`](https://github.com/Fuzss/mutant-monsters/blob/0404e1dc59bedef60c8835940dd141b7a4fedc10/Common/src/main/java/fuzs/mutantmonsters/common/world/entity/ai/goal/MutantMeleeAttackGoal.java)
- [`SeismicWave.java`](https://github.com/Fuzss/mutant-monsters/blob/0404e1dc59bedef60c8835940dd141b7a4fedc10/Common/src/main/java/fuzs/mutantmonsters/common/world/level/SeismicWave.java)

**Vanilla Refresh @1530a81**

- [`fabric.mod.json`](https://github.com/limesplatus/Vanilla-Refresh/blob/1530a8168059991897a12d31f44248f4fe5784a7/fabric.mod.json)
- [`tick.mcfunction`](https://github.com/limesplatus/Vanilla-Refresh/blob/1530a8168059991897a12d31f44248f4fe5784a7/data/vanilla_refresh/function/tick.mcfunction)
- [`2tick.mcfunction`](https://github.com/limesplatus/Vanilla-Refresh/blob/1530a8168059991897a12d31f44248f4fe5784a7/data/vanilla_refresh/function/other/clock/2tick.mcfunction)
- [`20tick.mcfunction`](https://github.com/limesplatus/Vanilla-Refresh/blob/1530a8168059991897a12d31f44248f4fe5784a7/data/vanilla_refresh/function/other/clock/20tick.mcfunction)
- [`silence.mcfunction`](https://github.com/limesplatus/Vanilla-Refresh/blob/1530a8168059991897a12d31f44248f4fe5784a7/data/vanilla_refresh/function/block/echo/silence.mcfunction)

**TES @ace1893**

- [`LICENSE`](https://github.com/Tslat/TslatEntityStatus/blob/ace1893e31ab77d58cc1a8805b3b2a72dcb8fce6/LICENSE)
- [`TESEntityState.java`](https://github.com/Tslat/TslatEntityStatus/blob/ace1893e31ab77d58cc1a8805b3b2a72dcb8fce6/Common/src/main/java/net/tslat/tslatentitystatus/core/state/TESEntityState.java)
- [`TESHUDActivation.java`](https://github.com/Tslat/TslatEntityStatus/blob/ace1893e31ab77d58cc1a8805b3b2a72dcb8fce6/Common/src/main/java/net/tslat/tslatentitystatus/api/client/constant/TESHUDActivation.java)
- [`TESConfig.java`](https://github.com/Tslat/TslatEntityStatus/blob/ace1893e31ab77d58cc1a8805b3b2a72dcb8fce6/Common/src/main/java/net/tslat/tslatentitystatus/api/TESConfig.java)
- [`SyncEffectsPacket.java`](https://github.com/Tslat/TslatEntityStatus/blob/ace1893e31ab77d58cc1a8805b3b2a72dcb8fce6/Common/src/main/java/net/tslat/tslatentitystatus/core/networking/packet/SyncEffectsPacket.java)

**boids @dbfd724**

- [`LICENSE`](https://github.com/Tomate0613/boids/blob/dbfd724d9155e29d952a4b90a91bceac851fa5a6/LICENSE)
- [`BoidGoal.java`](https://github.com/Tomate0613/boids/blob/0f50dca0d344dc2a1ef6287023e09728679338e7/src/main/java/dev/doublekekse/boids/goals/BoidGoal.java)
- [`NearbyMobTracker.java`](https://github.com/Tomate0613/boids/blob/dbfd724d9155e29d952a4b90a91bceac851fa5a6/src/main/java/dev/doublekekse/boids/NearbyMobTracker.java)
- [`BoidsConfig.java`](https://github.com/Tomate0613/boids/blob/0f50dca0d344dc2a1ef6287023e09728679338e7/src/main/java/dev/doublekekse/boids/config/BoidsConfig.java)

**skeleton-ai-fix @7dac096**

- [`LICENSE.md`](https://github.com/Fuzss/skeleton-ai-fix/blob/7dac096b81519b582d7410649e551050e62ec1d3/LICENSE.md)
- [`SkeletonAIFix.java`](https://github.com/Fuzss/skeleton-ai-fix/blob/7dac096b81519b582d7410649e551050e62ec1d3/Common/src/main/java/fuzs/skeletonaifix/common/SkeletonAIFix.java)
- [`ServerConfig.java`](https://github.com/Fuzss/skeleton-ai-fix/blob/7dac096b81519b582d7410649e551050e62ec1d3/Common/src/main/java/fuzs/skeletonaifix/common/config/ServerConfig.java)

**cosy-critters @1403312**

- [`LICENSE`](https://github.com/PigCart/cosy-critters/blob/1403312/LICENSE)
- [`CritterParticle.java`](https://github.com/PigCart/cosy-critters/blob/1403312/src/main/java/pigcart/cosycritters/particle/CritterParticle.java)
- [`ConfigData.java`](https://github.com/PigCart/cosy-critters/blob/1403312/src/main/java/pigcart/cosycritters/config/ConfigData.java)

