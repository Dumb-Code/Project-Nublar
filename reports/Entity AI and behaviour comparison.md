# Entity AI and behaviour comparison

Date: 2026-10-10. Status: research report for the project lead. It changes no code and no plan module.

A park animal looks alive when a watcher sees about 12 distinct behaviours per species, and at least 5 run with no urgent need. The richest reference creatures show 11 to 14 behaviours and fill idle time with a weighted pool of short acts. Nublar's species show 4 or 5 complete behaviours today, plus 5 to 7 in part, and the brain has no idle pool. The best shape is one shared SmartBrainLib 2 brain with ten activities, driven by species data, with Nublar code only where no hook exists. Of 19 AI-side candidates, 5 are Keep, 5 are Consider and 9 are Adopt-in-part, all as options for the lead.

**Labels.** `[v]` means verified in source at the commit that a research note names, or in the local code. `[inf]` means inference. `[doc]` means a README, wiki, changelog or store page. Every count in this report is a static reading of code or notes. Nobody ran a 26.2 build.

**Names.** "The survey" is [Reference mod survey for Beta.md](Reference%20mod%20survey%20for%20Beta.md). A row id such as B19.6 and a spec id such as S11 point into the survey. "Candidate 15" points into the survey section "Where another design may play better". "The plan" is [project-plan.md](../docs/project-plan.md). "Section 11" and "section 6" are sections of [codebase-analysis.md](../docs/codebase-analysis.md). Short mod names follow the survey: FAR, IaF CE, AC, MCA, FAF, GV, SBL, Cobblemon, Naturalist, Mowzie. "SBL" means SmartBrainLib 2.0.3 unless the text says 1.15. "The herd layer" is the uncommitted code under `entity/ai/behaviour/social`.

## Behaviour repertoire and daily life

### What each reference creature does

**In game:** each row is a behaviour that a player can see. Each column is a mod. "Y" means the mod has the behaviour, with its key number. "No" means the notes searched and found none. "n.r." means the notes do not record it. "Part" means a narrow version exists.

Every cell is `[v]` from the research notes unless it is marked `[doc]`. Cosy-critters and boids share a column because both are flocking patterns, not full creatures.

| # | Behaviour | FAR | IaF CE | AC | Naturalist | FAF | RevampedWolf | Cobblemon | MCA | Mowzie | Mutant Monsters | GV | boids, cosy-critters |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| R1 | Sleep in a time window | Y. Diurnal, nocturnal or both. Max 6,000 t | Y. At night, at the roost | No. Nap 200-399 t after a stolen nugget | Y. Bear: day time 6,000-12,000 and 18,000-23,000 | Y. 6,000-12,000 t, then cooldown 6,000-8,000 t | No | Y. Light and time window. Drowsy 1/30, rouse 1/240 per second | Y. Beds by schedule | No. Dormant state instead | No | No | No |
| R2 | Rest or sit outside sleep | Y. 1/1,000 per tick after 3 min. Stands after 300 t | No. Sit on command only | Y. Relaxed nap. Tame sit | Y. Bear sits to eat, 1/40 per tick | No | No. Sit on command only | n.r. | n.r. | No | No | No | Y. Birds perch up to 400 t |
| R3 | Graze or browse blocks | Y. Block food in range 32, every 160-320 t | No | Y. Plants every 200-400 t, not need-driven. Browse by height band | Y. Deer eat grass. Bear harvests hives and berries | n.r. | No | Y. Eat-grass task | No | No | No | No | No |
| R4 | Eat dropped food | Y. 16-block search | Y. Below 100 hunger. List refresh every 4 t | Y. Raptor, 9-block cube | Y. Bear, 8 by 2 | Y. Pickup cooldown 1-10 s | Y. Meat in 8 by 4 by 8 when the 1,200 t timer ends | Y. Pick-up-item task | Y. Held food every 200 t | No | No | Y. Off-hand food when hurt | No |
| R5 | Drink | No | No | No | No | No | No | No | No | No | No | No | No |
| R6 | Hunt by target search | Y. 1/10 per tick while hungry. Size gate | Y. Chance (100 - hunger) % | Y. Raptor about 1 scan per 100 t. Tremorsaurus 32 blocks | Y. Prey tags. Lions at night only | Y. Mauler `[doc]` | Y. Interval 10, after the cooldown | Y. Some species hunt others `[doc]` | No | Y. After activation | Y. Vanilla target goal | Y. Monsters by config | No |
| R7 | Stalk, leap or pounce | Y. Velociraptor leap goal | No | Y. Raptor leap, 1/10 per tick at 3-7 blocks | Y. Tiger crouches beyond 12 blocks at 0.7 | Y. Mauler burrows and ambushes `[doc]` | Y. Leap at 0.5-3 blocks | No | No | n.r. | Y. Creeper jump, 0.9 % per tick | No | No |
| R8 | Hunt cooldown after a kill | No. Hunger gate | No | No | Y. 2,400 t | No | Part. Set to 1,200 t, never counts down | No | No | No | No | No | No |
| R9 | Follow a group leader | Y. Leader has followers. Re-path 25 t. Max 10 | No | Y. Elder chain, max 8. Wander biased to the head | Y. Lion leader by lowest UUID, scan each tick | No | No. Store page claims it `[doc]` | Y. Leader tiers. Follow 4-8 blocks | No | Y. Raptors lead packs `[doc]` | No | Y. Phalanx behind shield guards | Y. Flocking, no leader |
| R10 | Group alarm or shared revenge | Y. SCARED flock flees 25 blocks | No | Y. Grottoceratops alerts others | Y. Deer panic timer 100-199 t in 8 by 4 by 8 | n.r. | Y. Hurt-by alerts others | n.r. | Y. Hurt villager alerts within 32 | n.r. | No | Y. Help range 50 | No |
| R11 | Pack shares one target | No | No | Y. Elder pushes its target every tick | No | No | No | No | No | n.r. | No | No | No |
| R12 | Flee or avoid a threat | Y. Outmatched animals flee about 100 t | No. Dragons never flee | Y. Small raptor pack flees 100-199 t | Y. Deer avoid players at 8, predators at 10 | Y. Avoid memory 5-10 s, flee 16 blocks | Y. Avoids llamas | Y. AVOID activity | Y. Panic package, 6 blocks | No | No | Y. Runs to eat below 50 % health | Y. Birds leave when an entity is within 10 |
| R13 | Panic when hurt | Y. SCARED species only | No | No | Y. Big panic 15 by 4. Baby panic | n.r. | Y. Environmental damage | n.r. | Y. Guards below 25 % health | No | No | No | No |
| R14 | Warning before an attack | No | No | Y. Tremorsaurus roar 55 t, cooldown 200-399 t | Y. Rhino stomp 42 t, then charge | No | No | No | Y. Guard warnings scaled by hearts | Y. Frostmaw roar 76 t | No | No | No |
| R15 | Guard young or eggs | n.r. | No | Part. An egg crush provokes a 25-block cube | Y. Attack a player near babies, 8 by 4 by 8 | No | No | No | No | No | No | No | No |
| R16 | Babies follow an adult | Y. Babies join an adult leader | n.r. | No | Y. Distanced follow 48, 12, 16 | Y. 5-16 blocks at 1.25 | n.r. | Y. 5-16 blocks at 0.4 | n.r. | No | No | No | No |
| R17 | Breed and lay a clutch | Y. Mood above 50, population cap | Y. Egg at the female's feet | Y. Bury 100 t, clutch 1-3 | Y. Lay on a tagged block after 60 t | Y. Has-egg flag. Parents age 6,000 | Y. Vanilla breed | No. Branch only | Y. Procreation under a bed limit | No | No | No | No |
| R18 | Ambient calls and sounds | Y. Ambient interval 200 t | Y. Roar 1/500 per tick | Y. Idle roar 1/600 per tick | Y. Sleep sound, sniff sound | n.r. | n.r. | Y. Cries `[doc]` | Y. Sounds and particles by mood band | Y. Roar replaces 1 in 4 ambient sounds | n.r. | n.r. | No |
| R19 | Idle variety (clips or random acts) | n.r. | Y. Flight 1/1,500, wander 1/20 per tick | Y. Raptor head tilt. Dancing | Y. Sniff 5 % per tick on walks | n.r. | Y. Beg. Random look | Y. Quirks such as blink. Look around 45-90 t | Y. Interact with villagers and cats at 8 | No | Y. Enderman block frenzy 1/300 per tick | n.r. | Y. Perch and look, 1 % per tick |
| R20 | Look at or approach players | Y. Look goals at priority 25 | No | Y. Atlatitan looks at players within 30 | n.r. | n.r. | Y. Look at a player within 8 | Y. Pastured creatures approach the player | Y. Greet within 32, every 100 t | No | No | Y. Look within 8. Follow the hero | No |
| R21 | Hide or freeze | No | No | Part. Tame raptor hides below 45 % health | Y. Snail, tortoise, hedgehog within 5-6 blocks | Y. Barnacle hides 400-1,200 t | No | No | Y. Hide package in raids | No | No | No | No |
| R22 | Recognise a player | No. Mood is not per player | No | No | No | No | No | Y. Friendship 0-255 for owned creatures `[doc]` | Y. Hearts, fatigue, last-seen day | No | No | Y. Gossip thresholds 15 and -100 | No |
| R23 | Home, patrol or territory | No. Stay order only | Y. Home radius 40, return at night | Y. Roost home 20-39 blocks | No | Y. Tuff Golem home | No | Y. Pasture radius 32 | Y. Village patrol and home bed | Y. Naga home 50 blocks `[doc]` | No | Y. Patrol post, village stroll | No |
| R24 | Act on the world (forage, play, raid) | Y. Toys. Breaks blocks when hungry and in a bad mood | Y. Breaks blocks every 5 t at stage 3 or more | Y. Chest raid every 100-200 t. Tree pushing | Y. Harvests hives and berries | Y. Glare seeks berries and dark spots | No | Y. Combee gathers nectar | Y. Chores and gifts | No | Y. Enderman picks up blocks | No | No |
| R25 | Slow idle far from players | No | No | Y. Idle stroll stops at no-action time 100 | No | No | No | No. Despawn instead | No | Y. Dormant until a target is within 8 | No | No | No |
| | **Rows with "Y"** | **15** | **8** | **17** | **19** | **10** | **9** | **13** | **14** | **6** | **4** | **8** | **4** |

Sources: `fossils_revival_and_ice_and_fire.md` Q4, Q5, Q7, Q8, Q10, Q13, Q15. `alexs_caves_dinosaurs.md` KQ2 to KQ6. `creature_behaviour_mods.md` key questions 1 to 4. `cobblemon_architecture.md` Q4. `cobblemon_wiki.md` key question 4. `people_and_structures_mods.md` (MCA, GV). `large_mob_infrastructure_and_vehicles.md` key questions 1 and 3. `vanilla_plus_and_feeders.md` key question 1. `other_mod_wikis.md` key question 1.

**Reading.**

- **No reference mod has drinking.** Thirst is one of the twelve features with no precedent (survey feature 11).
- **The mod counts overstate a single creature.** A mod spreads its rows over many species. The table below counts the richest single creature.
- **Warnings are rare.** Only AC, Naturalist, MCA and Mowzie show a warning before an attack. Section 11.10 asks every Nublar species for one.
- **Idle variety comes from two layers.** The brain picks an act (walk, look, sit, sniff). The client picks a clip for that act. Mods with a rich idle (Cobblemon, Naturalist, MCA) have both layers.

| Richest single creature | Distinct behaviours | Basis |
|---|---|---|
| AC Vallumraptor | 14 | Join pack, wander toward the Elder, prey hunt, pack hunt of Grottoceratops, leap, shared target, small-pack flee, chest raid, eat item, nap, flee after theft, open doors, head tilt, tame hide `[v]` |
| Naturalist Black Bear | 12 | Sleep window, wake timer, sit to eat, harvest, item search, sniff on walks, prey hunt, hunt cooldown, dark-time player target, guard cubs, alert others, cub follow `[v]` `[doc]` |
| FAR Gallimimus | 11 | Flock build, flock wander, sleep, sit, block food, item food, SCARED or TERRITORIAL response by herd size, flock alarm, toys, breed, look `[v]` `[inf]` |
| MCA villager | 11 | Sleep, meet at the bell, interact, greet, chores, panic, hide, gifts, grieve, procreate, mood display `[v]` |
| Cobblemon wild herd member | 8 | Look around, wander, sleep with drowsy and rouse chances, follow herd leader, walk toward adult, defend itself, avoid, cry `[v]` `[doc]` |

### How a day strings together

**In game:** this table shows what each creature does through one game day, and what fills the time when no need pulls it.

| Mod and creature | Day | Night | When no need is urgent | What interrupts |
|---|---|---|---|---|
| FAR (any species) | Diurnal species wander (interval 120, 10 blocks), graze at 160-320 t, flock with the leader | Diurnal species sleep (1/200 per tick to start, max 6,000 t) | Wander, sit (1/1,000 per tick after 3 min), play with toys when mood is below 100 | Hunger below 75 % seeks food. Below 25 % sprints. Damage wakes |
| IaF CE dragon | Wander (1/20 per tick), fly (1/1,500 per tick), hunt by hunger | Return to the roost beyond 10 x width, then sleep | Wander, roar (1/500 per tick), flight | A player within 3 x the box size wakes it and it attacks |
| AC Vallumraptor | Wander toward the Elder, prey scan about once per 100 t, chest search every 100-200 t | No sleep. Same as day | Biased wander, head tilt, item pick-up | Elder's target, a Grottoceratops when the pack is small, a full hand |
| AC Grottoceratops | Graze plants every 200-400 t (heals 5), stroll at interval 45 | Same as day | Graze, stroll, look | Hurt-by alert from a herd mate (target clears after 600 t) |
| Naturalist bear | Forage hives and berries, sniff on walks, hunt prey tags | Sleeps in its clock window. Ambient sound changes | Sit and eat held food, sniff, stroll | Damage wakes it with a 200 t wake lock. A player near a cub |
| FAF Tuff Golem | Wander, hold an item | Sleeps at home 6,000-12,000 t | Hold and show its item | Sleep cooldown 6,000-8,000 t after it wakes |
| Cobblemon wild Pokémon | Wander (chance 120, 10 by 5 at 0.33), herd follow (4-8 blocks), eat grass | Sleeps in its light and time window. Drowsy 1/30 and rouse 1/240 per second stagger the herd | Look around 45-90 t, quirk clips, follow the leader | Hurt-by switches to FIGHT. A threat writes AVOID |
| MCA villager | Work, chores, meet at the bell | Sleep in a bed. Night owls swap the schedule | Interact with villagers and cats within 8, greet known players within 32 | Hostile within range starts PANIC or HIDE. A hit raises a bounty |
| GV guard | Village stroll or patrol post, look at players within 8 | Same as day | Stroll, look, eat food when hurt | Any mob that targets a villager within 50 |
| Mowzie Frostmaw | Dormant: no movement, heals 0.3 per tick | Same as day | Nothing (dormant) | A target within 8 starts a 108 t wake animation with a roar |

### Nublar's six species today

**In game:** this table sets each Nublar species beside the matrix rows. "D" means section 11 or the plan decides the behaviour. "C" means the local code (branch `kash's-slop`, uncommitted tree included) has it. "Part" means the code has a narrow or broken version. "n/a" means the behaviour does not fit the species.

| # | Behaviour | Tyrannosaurus | Velociraptor | Dilophosaurus | Triceratops | Gallimimus | Brachiosaurus |
|---|---|---|---|---|---|---|---|
| R1 | Sleep in a time window | D, C part | D, C part | D, C part | D, C part | D, C part | D, C part |
| R2 | Rest when tired | D, C | D, C | D, C | D, C | D, C | D, C |
| R3 | Graze or browse blocks | n/a | n/a | n/a | D (low) | D | D (high) |
| R4 | Eat dropped food, carcass or feeder | D, C part | D, C part | D, C part | D, C part | D, C part | D, C part |
| R5 | Drink | D, C | D, C | D, C | D, C | D, C (no drink clip) | D, C |
| R6 | Hunt by target search | D, C | D, C part | D, C part | n/a | n/a | n/a |
| R7 | Stalk, leap or pounce | No | No (clip `pounce` exists) | No | No | No | No |
| R8 | Hunt cooldown | D | D | D | n/a | n/a | n/a |
| R9 | Follow a group leader | D, C | D, C | D, C | D, C | D, C | D, C |
| R10 | Group alarm (warn call) | D | D, C part (startled) | D | D | D, C part (startled) | D |
| R11 | Pack shares one target | No | D | No | No | No | No |
| R12 | Flee a threat | No (stands) | No (stands) | No (stands) | No (stands) | D | D (D13 default) |
| R14 | Warning before an attack | D, C part | D, C part | D, C part | D, C part | D, C part | D, C part |
| R15 | Guard young or eggs | D | D | D | D | D | D |
| R16 | Babies follow an adult | D, C part | D, C part | D, C part | D, C part | D, C part | D, C part |
| R17 | Breed and lay a clutch | D | D | D | D | D | D |
| R18 | Calls (warn, gather, threaten) | D | D | D, C part (gather) | D, C part (gather) | D, C part (gather) | D, C part (gather) |
| R19 | Idle clip variety | C (8 clips) | C (7 clips) | C (7 clips) | C (6 clips) | C (4 clips) | C (7 clips) |
| R20 | Look at or approach players | No | No | No | No | No | No |
| R22 | Recognise a player | D | D | D | D | D | D |
| R23 | Territory | D | D | D | No | No | No |
| R24 | Act on the world | D, C part (fences) | D, C part (fences) | D, C part (fences) | D, C part (fences, leaves) | D, C part (fences) | D, C part (fences, leaves) |
| N1 | Rank contest with a display | D, C | D, C | D, C | D, C | D, C | D, C |
| N2 | Leader journeys and graze moves | D, C | D, C | D, C | D, C | D, C | D, C |
| N3 | Sentry watch by the leader | No | No | No | No | C | No |
| N4 | Tame: follow, stay, defend | D | D | D | D | D | D |
| N5 | Detection by sight, sound, scent | D | D | D | D | D | D |
| N6 | Stamina-limited chase | D | D | D | n/a | n/a | n/a |
| N7 | Stress and social need | D (social stub) | D (social stub) | D (social stub) | D (social stub) | D (social stub) | D (social stub) |

Rows R13, R21 and R25 are "No" for all six species, so the table leaves them out.

Code evidence `[v]`: brain groups in `Dinosaur.java` L594-L696. Rest hours from the `nocturnal` flag in `Rest.java` L36-L49, `FollowFormation.java` L367-L371 and `JourneyPolicy.restHours`. One-shot call, sentry and startled in `DinoOneShots.java` L20-L33, `OneShotAction.java`, `StartleWatch.java` (radius 6, check every 10 t, cooldown 400 t). Clip names in `AnimationChoice.java` L20-L45 and the six files under `assets/projectnublar/animations/entity/`. Hunting gaps in section 6.2. Decisions in section 11.10, plan B16 to B19 and decision D13.

**Defects that change what a player sees** `[v]`:

- `ThreatDisplay.start` plays `TYRANNOSAUR_ROAR` for every species (`ThreatDisplay.java`, `start`). A Gallimimus rank contest sounds like a Tyrannosaurus. Only the Tyrannosaurus has sound events (`SoundInit.java` L15-L21).
- `ThreatDisplay` starts only while `IS_ROARING` exists, and only `ChallengeGroupLeader` writes it. So no species warns a player before an attack today.
- Velociraptor and Dilophosaurus `canTarget` call the parent and drop its result (section 6.2). They retaliate only.
- `Eat` reads only `NEAREST_VISIBLE_WANTED_ITEM` (`Eat.java` L6-L8). Herbivores never eat blocks. The herd's "graze" state holds a spot and idles (`GrazeSchedule.java` Javadoc).
- `DinosaurLookAtTarget` reads `LOOK_TARGET`, and no behaviour writes a player into it. No species looks at a player who stands near it.
- `IncubatedEggItem` spawns the animal without `setBaby` (section 6.4). So `FollowParent` never runs for a hatchling.
- The Tyrannosaurus config has `group_size` 5, and its card says 1 to 2 (section 7.8). The B00 species data replaces the value.

**Per species: decided role, code today, gaps and spare clips.**

| Species | Decided role (section 11, plan, D13) | Code today | Missing for Beta | Clips that no behaviour plays today |
|---|---|---|---|---|
| Tyrannosaurus | Carnivore, group 1 to 2. Stands. Territory. Warning roar. Prey by clade (card). Card AI: solo and pair hunting, scavenging, dominance, calls, play, mating, nesting | The only species that hunts. Roams 40 blocks. Melee. Breaks fences while starving. Drinks, eats items, rests. Joins herds. Contest roar. Breath ambient sound | Territory, warning toward players, stamina chase, prey ranking, scavenging, nest, guard young, taming, detection, calls (no call clip) | `nesting`, `turn_left`, `turn_right`, `jump/fall`, `speak` |
| Velociraptor | Pack hunter, group 3 to 6, shares a target and flanks. Stands (D13 default) | No real hunt (retaliates only). Herd layer as a herd. Startled one-shot. Contest roar | Pack hunt and flank (survey feature 12), stamina chase, warning, nest, guard young, taming | `pounce`, `sleep`, `sneeze`, `nesting`, `left_claw_down`, `right_claw_down`, `speak` |
| Dilophosaurus | Carnivore, group 1 to 3. Frill as its warning (section 11.10). Stands (D13 default) | Retaliates only. Herd. Gather call at journey start | Frill or spit display, hunt, stamina chase, nest, guard young | `spit`, `call2`, `speak`, `speak2`, `sleep` |
| Triceratops | Herd 4 to 6. Stands and charges. Eats low plants | Herd. Gather call. Drinks, eats items, rests | Low grazing, charge with wind-up, guard young, nest | `ram`, `ramwindup`, `injured`, `speak` |
| Gallimimus | Herd 6 to 12. Flees and runs | The most complete herd: calling, sentry, startled, travel wedge. Herd size 20 in config | Flee (AVOID), grazing, drink and eat clips (the file has none), nest | none spare. The file lacks `drink`, `eat1`, `getup`, `roar` |
| Brachiosaurus | Herd 3 to 5. Flees (D13 default). Browses high. Open ground only (B26) | Herd. Gather call. Drinks, rests | High browsing, flee, open-ground rule | `tailwhip`, `Stomp`, `Neck Attack2`, `eatingtall2`, `rest2`, `speak` |

Clip lists come from the six animation files `[v]`. "Spare" means no `AnimationChoice` state lists the name today.

### How many behaviours a species needs

**In game:** a visitor who watches a paddock for two minutes sees at least three different acts per animal. No two herd mates do the same act on the same tick.

| Species | Decided rows (D) | Code rows complete today | Code rows in part today | Proposed target | Of which idle acts (no need) |
|---|---|---|---|---|---|
| Tyrannosaurus | 16 | 5 | 5 | 13 | 5 |
| Velociraptor | 17 | 4 | 7 | 15 | 6 |
| Dilophosaurus | 16 | 4 | 7 | 13 | 6 |
| Triceratops | 14 | 4 | 6 | 13 | 5 |
| Gallimimus | 15 | 4 | 7 | 14 | 5 |
| Brachiosaurus | 15 | 4 | 6 | 12 | 5 |

Counts use matrix rows R1 to R25 only. Targets are `[inf]`. Basis: the richest reference creatures show 11 to 14 behaviours, and Nublar's decided rows already reach 14 to 17. A decided row is not always visible, for example detection, so the target counts visible acts only.

**Answer.** Each species needs 12 to 15 visible behaviours. At least 5 of them must be idle acts that run with no urgent need. Today each species has 4 or 5 complete behaviours and one idle source, the clip pick. Babies count as "part" because a hatchling spawns as an adult.

### What an animal does when no need is urgent

**In game:** a fed, watered, rested animal mixes standing, short walks, looks, sniffs, brief sits and calls. A herd member does these at its graze spot. A leader also keeps watch and moves the herd every few minutes.

The reference mods agree on one shape: a weighted random pick of short acts of 2 to 15 seconds each. Rare acts have a cooldown `[v]`. SBL ships that shape as `OneRandomBehaviour` (weighted) and `Idle` (`runFor`) `[v]`. The SBL example skeleton picks between a random walk and an idle of 30 to 60 ticks `[v]`.

| Idle act | Precedent | Proposed weight | Duration | Clips present per species |
|---|---|---|---|---|
| Stand with the main idle and the sway | SBL `Idle` 30-60 t `[v]`. Nublar main idle weight 6 `[v]` | 6 | 40-120 t | All six (`misc.idle`) |
| A clip variant as a brain act: sniff the ground, look left or right, shake, scratch | Naturalist sniff 5 % per tick `[v]`. Cobblemon quirks `[v]` | 1 per clip | One clip length | T 7, V 6, Di 6, Tr 5, G 3, B 6 variants |
| Look at a nearby player or herd mate | SBL `SetPlayerLookTarget`, `SetRandomLookTarget` `[v]`. Cobblemon 45-90 t `[v]`. RevampedWolf and GV 8 blocks `[v]` | 2 | 45-90 t | All six (head look, no clip needed) |
| Short wander (groupless animals and leaders only) | SBL `SetRandomWalkTarget` 10 by 7 `[v]`. Cobblemon chance 120, 10 by 5 at 0.33 `[v]`. FAR interval 120 `[v]` | 2 | Until arrival | All six (`move.walk`) |
| Sit down briefly | FAR 1/1,000 per tick after 3 min, stand after 300 t `[v]` | 1, cooldown 3,600 t | 200-400 t | All six (`rest`, `restidle`) |
| Cosmetic graze at the spot, with no block removed | AC periodic graze every 200-400 t `[v]` | 2 (herbivores) | One clip length | Tr `eat1`. B `eat1`, `eatingtall2`. G none |
| Ambient call | FAR interval 200 t `[v]`. AC idle roar 1/600 per tick `[v]`. IaF CE 1/500 per tick `[v]` | 1, cooldown 1,200 t | One clip length | Clips: Di, Tr, G, B. Sound: Tyrannosaurus only |
| Sentry watch (leader) | Nublar 30 % every 200-400 t `[v]` | Leader only | Clip length | G only (`sentry`) |
| Greet a herd mate | MCA interact at 8 blocks `[v]` | 1, cooldown 1,200 t | One clip length | T, V, Di, Tr, B (`speak`) |

Weights, durations and cooldowns are `[inf]` proposals. Look-around clips on the second controller stay unbuilt under decision D14. The idle pool uses only clips that exist on the main controller.

## Brain architecture

### How each mod organises its AI

**In game:** players never see the AI framework. They see its effects. These are how fast a creature reacts, whether a herd acts on one tick, and whether the server keeps 20 ticks per second.

| Mod | Framework | Species differences live in | Sensing and its schedule | Memory and expiry | Activity switching |
|---|---|---|---|---|---|
| FAR | Vanilla goals with priority constants: 0 immobile, 5 attack, 10 sleep, 15 needs, 20 wander, 25 look. Plain "systems" (mood, sleep, sit, order) ticked from `aiStep` | Java subclass per species, an enum entry and datapack `entity_info` JSON with AI enums (activity, attacking, response, taming, moving) | Goals poll: hunt 1/10 per tick while hungry. Block food every 160-320 t with an avoid cache | Fields in the systems, saved to NBT | Goal flags MOVE, LOOK, JUMP. Sleep disables the control flags |
| Naturalist | Vanilla goals | Hard-coded goal list per species. Small interfaces (`SleepingAnimal`, `HuntingAnimal`, `HidingAnimal`). Prey and food as tags | Goals query in `canUse`, many each tick (three `AvoidEntityGoal` per deer, lion leader scan) | Entity fields, NBT `HuntingCooldown` | Goal priority and flags |
| AC | Vanilla goals plus Citadel animations | Class per dinosaur. Goal "chance" gates | Target goals by chance (about 1 scan per 100 t). Ring scans every 100-400 t | Fields. Flee ticks synced each tick | Goal priority |
| IaF CE | Goals plus one consolidated `IafDragonLogic` tick | One dragon base, few type subclasses | Probability gates. Item list every 4 t. Nearest player each tick | Fields | Flags in the logic class |
| RevampedWolf | A mixin replaces the vanilla wolf goal list | One mob only | Goals | Interface fields | Goal priority |
| GV | About 20 goals and 9 target goals | One mob only | Entity queries inside `canUse` | Fields, gossip container | Goal priority |
| Mutant Monsters | Goals plus `AnimationGoal`, which cannot be interrupted while the animation tick is below its duration | One class per mutant. Attack choice in `doHurtTarget` | Vanilla target goal, interval 10 | One synced animation index and tick | The entity sets the animation, the goal follows |
| Mowzie | `Ability` capability: STARTUP, ACTIVE and RECOVERY sections. Older `AnimationAI` goals. Dormancy flag | One class per boss | Cached target distance once per tick | Ability state in NBT, so a reload resumes mid-ability | One non-background ability at a time |
| FAF | Vanilla brain. A static brain class per mob with CORE, IDLE and custom activities | Brain class per mob. Species sensors (for example `GlareSpecificSensor`) | Vanilla sensors, 20 t default | Avoid target with a 5-10 s expiry. Cooldowns as counted memories. Flags mirrored into boolean memories | `updateActivities` picks the first valid activity from an ordered list |
| MCA | Vanilla brain with 8 package sets chosen by state. `refreshBrain` rebuilds on a state change | Personalities and traits as data. Packages by role | 10 sensors. `GuardEnemiesSensor` skips non-guards. Interval table 20 to 12,000 t, villages offset by id | 41 memory types. Expiring string keys (`LongTermMemory`) | Schedules as `EnvironmentAttribute<Activity>` |
| Cobblemon | Vanilla brain. Behaviour configs from datapack JSON: about 58 task-config types with MoLang conditions | Species and form JSON (`behaviour.resting`, `behaviour.herd`), `baseAI` and `ai` config lists, an in-game Behaviour Editor | 13 sensors on staggered intervals from 10 to 300 t | 36 memory types. `HERD_LEADER` as a UUID string | Activities CORE, IDLE, FIGHT, AVOID, battling, sleeping, growing plant |
| SBL 2 (library) | `SmartBrain`. `ActivityBuilder` per activity. Built-in sensors and about 45 behaviours | Any: owner methods return lists. Behaviours take lambdas | `ExtendedSensor` with `scanRate` (fixed or per entity), `onlyScanIf`, random start offset | `setMemoryWithExpiry`. A memory without expiry stays until cleared | First activity in `getActivityActivationPriority` whose memories are met. Schedule-ignoring set. Optional `SmartBrainSchedule` |

Sources: survey notes as listed for the matrix, plus `ai_library_layer.md` KQ1 to KQ4.

### Sensor scheduling, memories and switching

**In game:** staggered sensors mean a herd notices a player over one second, one animal after another, not all on one tick.

| Topic | Best precedent | Nublar today | Note |
|---|---|---|---|
| Scan interval | Cobblemon 10-300 t by job `[v]`. Vanilla 20 t `[v]` | All five sensors at the SBL 1.15 default, 20 t `[v]` | SBL 2 sets the rate per sensor and per entity |
| Start offset | Vanilla random first scan within 20 t `[v]`. SBL 2.0.3 calls `randomlyDelayStart` `[doc]` | No offset in SBL 1.15. Animals that spawn together scan together `[v]` | SBL `scanRate(e -> base + e.getId() % k)` also works `[inf]` |
| Gate | SBL `onlyScanIf`, evaluated once per period `[v]`. MCA role gate `[v]` | The fence predicate tests the block first, so the scan always runs `[v]` | Gate need sensors on the need |
| Shared list | SBL `NearbyLivingEntitySensor` with memoised line of sight `[v]` | Used. `JoinGroup` runs its own query every 40 or 200 t `[v]` | B18.3 |
| Expiring memory | FAF avoid memory 5-10 s `[v]`. MCA expiring keys `[v]`. SBL `setMemoryWithExpiry` `[v]` | One use (`ChallengeGroupLeader` look target). `IS_HUNGRY` never clears `[v]` | S4 |
| Activity choice | FAF and Cobblemon: ordered list of activities with memory conditions `[v]` | Three fixed groups. Idle is one `FirstApplicableBehaviour` of 11 children `[v]` | Plan B17 decides activities |
| Pre-emption | None in SBL: priority is iteration order. Exclusion comes from memory conditions `[v]` | `HerdNeeds` start conditions and `isIdle` gates `[v]` | Keep exclusion in memories |
| Brain tick rate | None in SBL. No tick-skip option `[v]` | Manual `tickBrain` in `customServerAiStep` `[v]` | SBL 2 ticks the brain itself. Delete the manual call (survey rank 1) |

### What each way of expressing species costs

| Approach | Used by | Cost to add a species | Per-tick cost shape | Who can change behaviour | Fit for six Beta species |
|---|---|---|---|---|---|
| Hard-coded goal list per species | Naturalist, AC | A new class and goal list | Goals poll in `canUse`. High when they query | Java developers | Poor. Six copies of similar lists |
| Base class plus species subclass plus data enums | FAR | Subclass, enum entry, JSON | As goals | Developers. Pack makers change enums | Fair. Enums limit what data can say |
| One shared brain plus species data | Nublar (intent), IaF CE in part | One JSON file | One brain tick. Sensors on intervals | Developers for logic. Pack makers for numbers | Good. Six species share 90 % of their logic |
| Behaviour configs in datapack JSON | Cobblemon | JSON only | Vanilla brain cost. Large load-time layer (about 45 type adapters) | Pack makers, with a Behaviour Editor | Heavy for six species (survey Q4 note) |
| Packages chosen by state, rebuilt on change | MCA | A package set | Rebuild cost on each state change | Developers | Fair for tamed or baby variants. Complex |
| Ability sections per attack | Mowzie | An ability class per attack | Counters only. 2 varints per ability event | Developers | Good for attacks only |

### Nublar's brain today

**In game:** a dinosaur walks, drinks, eats dropped items, rests, joins a herd and follows its leader. A Tyrannosaurus hunts when hungry. Every species fights back when hit. No species warns, flees or looks at players.

Code in `Dinosaur.java` L535-L696, read on 2026-10-10 `[v]`. The brain runs on SBL 1.15 with `BrainActivityGroup`.

| Group | Behaviours, in order | Gates and notes |
|---|---|---|
| Core (always) | `DinosaurLookAtTarget`, `MoveToWalkTarget`, `SetHunting`, `SetWalkTargetToWaterSource`, `WalkToNearestFeeder`, `SetWalkTargetToFoodItem`, `FollowParent`, `JoinGroup`, `ThreatDisplay(34)`, `FollowFormation` | Need walks gated by `HerdNeeds::mayLeaveForNeeds`. `FollowFormation` runs each tick in every activity |
| Idle (default) | One `FirstApplicableBehaviour`: `Drink(100)`, `EatFromMeatFeeder(20)`, `Eat(69)`, `Rest(69)`, `GettingUpFromRestBehaviour(69)`, `SoloHuntingBehaviour`, `SoloHuntRoamBehaviour(40)`, `ChallengeGroupLeader`, `LeaderJourney`, `SetRandomWalkTarget(10, 4)` | `Panic` is commented out. Random walk only for groupless idle animals. Up to 11 start checks per tick (fit note 1b) |
| Fight (on `ATTACK_TARGET`) | `InvalidateAttackTarget`, `SetWalkTargetToAttackTarget` (speed 1.5), `BreakFenceBehaviour(20)`, `AnimatableMeleeAttack(20)` | Invalidates creative players and dead targets only. No stamina end |
| Outside the brain | `HerdLeading.settleLeaderChange` and `HerdLeading.tick` from `customServerAiStep`. `HerdMoveControl` as the move control. `DinoOneShots.serverTick` | The herd layer runs partly outside activity switching |
| Not wired | `StartTurfWar`, `LargeCarnivoreFight`, `SetMateFromNearbyDinosaurs`, `IdleAnimationBehaviour` | Section 6.2. Turf wars are not in Beta |

| Sensor | Radius | Rate | Gate | Reads per scan |
|---|---|---|---|---|
| `NearestWaterSourceSensor` (Nublar) | 20 | 20 t | None | 68,921 blocks |
| `NearestFeederSensor` (Nublar) | 20 | 20 t | None | 68,921 blocks |
| `NearbyBlocksSensor` (fences) | 10 | 20 t | Predicate after the block test | 9,261 blocks |
| `NearestItemSensor` | 20 | 20 t | None | One sight test per item |
| `NearbyLivingEntitySensor` | `FOLLOW_RANGE` 35 | 20 t | None | One entity query, 70-block cube |

Sources: `ai_library_layer.md` KQ4, `project_nublar_fit.md` key question 1. About 147,000 block reads per animal per cycle `[v]`.

**The uncommitted herd layer.** 29 classes, about 7,000 lines, under `entity/ai/behaviour/social` `[v]`. It is the only Nublar subsystem with a per-tick budget.

| Part | Role in the brain | Built on |
|---|---|---|
| `GroupManager`, `DinoGroup` | Per-level groups. Leader by `Dominance`. GRAZE and TRAVEL states. Not saved: groups re-form after a restart | Plain Java, `WeakHashMap` per level |
| `Dominance` | Age 0.5, size gene 0.3, aggression gene 0.2. UUID tie-break | Pure |
| `JoinGroup` | Core behaviour. Queries radius 24 + 2 x width every 40 t (groupless) or 200 t (in a group) | SBL `ExtendedBehaviour` with `cooldownFor` |
| `ChallengeGroupLeader` | Idle behaviour. Cooldown 1,200-2,400 t. Writes `IS_ROARING` for `ThreatDisplay` | SBL `ExtendedBehaviour` |
| `LeaderJourney`, `HerdLeading`, `JourneyPolicy` | Graze moves every 400-800 t. Journeys every 2,400-6,000 t of 156-314 blocks. Rest hours by the `nocturnal` flag | SBL behaviour plus a static tick |
| `FollowFormation`, `TravelOrder`, `LeaderTrail` | Core behaviour each tick: hold a graze spot or steer along the trail in a wedge | SBL behaviour |
| `HerdMoveControl`, `ContextSteering`, `HerdBodies`, `BodyFit` | Move control with 12-heading steering, capped at 10 evaluations per herd per tick | Vanilla `MoveControl` |
| `HerdNeeds` | A travelling follower leaves the line only below the rage threshold | Start conditions |
| `DinoOneShots` | Call at journey start, sentry, startled | Server tick, one synced byte |

The movement half of this layer is module B26, and the hitbox design governs it. This report does not reopen it. The brain half (groups, contests, journeys, one-shots) is the input to the layout below.

### Recommended SmartBrainLib 2 activity layout

**In game:** a dinosaur switches cleanly between ten modes. It flees or fights at once. It eats or drinks when a need is urgent. It sleeps in its species window. It travels with its herd. In all other time it fills the day from an idle pool.

**Rule.** Each activity uses SBL built-ins where a hook exists. Nublar code appears only where the survey's "Needs with no hook" list or this report shows no hook. Nublar behaviours extend `ExtendedBehaviour`, `DelayedBehaviour` or `HeldBehaviour`, and Nublar sensors extend `ExtendedSensor` or `NearestVisibleEntityFilteredSensor`.

**Shared sensors** (one set for all six species):

| Sensor | Hook | Rate and gate | Writes | Notes |
|---|---|---|---|---|
| Nearby living entities | SBL `NearbyLivingEntitySensor`, radius from species `follow_range` (new field) | 20 t, start offset by entity id | `NEAREST_LIVING_ENTITIES`, `NEAREST_VISIBLE_LIVING_ENTITIES` | The one entity query. Feeds herd join, prey, threats, breeding |
| Players | SBL `NearbyPlayersSensor` | 20 t | `NEAREST_PLAYERS`, `NEAREST_VISIBLE_PLAYER` | O(players) |
| Hurt by | SBL `HurtBySensor` | 20 t | `HURT_BY`, `HURT_BY_ENTITY` | Retaliation and panic |
| Prey ranking | Nublar subclass of `NearestVisibleEntityFilteredSensor` | 20 t, `onlyScanIf` hungry carnivore | `NEAREST_ATTACKABLE` | B17.7. One scored pass over the shared list |
| Threat (flee species) | Nublar subclass of `NearestVisibleEntityFilteredSensor` | 20 t, `onlyScanIf` `flee_or_stand` is flee | `AVOID_TARGET` with expiry 100-200 t | S11 shape |
| Dropped food | SBL `NearestItemSensor`, radius 16 | 40 t, `onlyScanIf` hungry | `NEAREST_VISIBLE_WANTED_ITEM` | B17.9 |
| Water | SBL `NearbyBlocksSensor` (8, 2) | 60 t, `onlyScanIf` thirsty | `NEARBY_BLOCKS`, then a remembered source with expiry | B17.2 |
| Fence | SBL `NearbyBlocksSensor` (10) | 100 t, `onlyScanIf` starving with a target | `NEARBY_BLOCKS` | B15 memories |
| Unreachable target | SBL `UnreachableTargetSensor` | 20 t | `TARGET_UNREACHABLE` | Fence break trigger |
| Feeder | Nublar sensor over a per-level feeder registry | 40 t, `onlyScanIf` hungry | `HAS_FOUND_FEEDER` with expiry | B17.1. No block scan |
| Graze columns | Nublar sensor (S6) | 40 t, `onlyScanIf` hungry herbivore | `GRAZE_TARGET` with expiry | Under 200 reads per scan |
| Sight, sound, scent | Nublar detection sensor, vanilla vibration listener, scent sensor | 20 t, event, 40 t | `DETECTED_PLAYER`, `DISTURBANCE`, `SCENT_TARGET` | Survey features 1 and 2 |

**Activities.** "Order" is the place in `getActivityActivationPriority`, where 1 wins. Every activity uses `behaviourPriorityBase(50)`, as SBL 2 does for IDLE and FIGHT, because only CORE and one other activity run at a time.

| Order | Activity | Starts when | SBL built-ins | Nublar code (no hook) | Species data that drives it |
|---|---|---|---|---|---|
| Always | CORE | Always | `LookAtTarget`, `MoveToWalkTarget`, `FollowParent` (babies), `InvalidateMemory` | `JoinGroup` reading the shared list (B18.3). Needs, stamina and stress step every 20 t, offset by id | `group_size_min`/`max`, `growth_days` (baby gate) |
| 1 | AVOID (vanilla `Activity.AVOID`) | `AVOID_TARGET` or `IS_PANICKING` present. Schedule-ignoring | `AvoidEntity` (flee distance 16, sprint inside 7), `Panic` on `HURT_BY` | Group alarm write on start (B18.2) | `flee_or_stand`, flee list (new field) |
| 2 | FIGHT | `ATTACK_TARGET` present (SBL requires and clears it). Schedule-ignoring | `InvalidateAttackTarget` with `invalidateIf(stamina <= 0)`, `SetWalkTargetToAttackTarget`, `AnimatableMeleeAttack` or a section hit, `LeapAtTarget` (raptor pounce, if built), `ReactToUnreachableTarget` | Display, hit and recovery track (S10, candidate C8). Fence break. Flank slots (feature 12). Stamina end writes `HUNT_COOLDOWN` | `attack_damage`, `energy`, `territory_radius`, display clip length from the animation file |
| 3 | HUNT (`projectnublar:hunt`) | Hungry carnivore, adult, `HUNT_COOLDOWN` absent, no `ATTACK_TARGET` | `SetAttackTarget` from `NEAREST_ATTACKABLE`, `SetRandomWalkTarget` for roaming | Leader target push on change (S9). Scent follow at 0.7 (feature 2) | `diet`, `prey_preferences`, `needs.hunt_cooldown_seconds` (TOML) |
| 4 | FEED (`projectnublar:feed`) | `IS_HUNGRY` or `IS_THIRSTY` present, the matching cooldown absent, `HerdNeeds.mayLeaveForNeeds` | `SetWalkTargetToBlock` (water, graze, feeder), `FollowTemptation` (tame lure) | `Drink`, `Eat`, `Graze` as `DelayedBehaviour`. Cooldown memory on finish (S4) | `diet` tag, `hunger`, `thirst`, reach band (new field), `needs.cooldown_seconds.<need>` |
| 5 | REST (vanilla `Activity.REST`) | `IS_TIRED`, or the species rest window. `SLEEP_COOLDOWN` and `WAKE_LOCK` absent. `HerdNeeds.mayRest` | `SmartBrainSchedule.byDaytime` per species (see open item A2), `Idle` with `runFor` | Sleep hold and wake lock (S7). Drowsy and rouse chances (candidate C12) | activity window (new field, see A1), `energy` |
| 6 | HERD_TRAVEL (`projectnublar:herd_travel`) | Group state is TRAVEL, mirrored into a memory each tick | `FollowEntity` for an animal past the trail tail (candidate C5) | `FollowFormation` travel mode, leader legs from `HerdLeading` | `group_size_min`/`max`, B26 spacing values |
| 7 | BREED (`projectnublar:breed`) | In love, nest in range, both stresses below the threshold, fertility roll passed | `BreedWithPartner` with an overridden `spawnChildFromBreeding` | Nest build, lay walk, has-egg flag (B18.4) | `egg_clutch_min`/`max`, `hatch_success`, `stress_threshold` |
| 8 | SOCIAL (`projectnublar:social`) | `IS_ROARING` (contest) or a pending call | `LookAtTarget` | `ChallengeGroupLeader`, `ThreatDisplay`, calls as clip plus sound plus game event (B18.7) | Dominance weights (B00 moves them to species data), call clips |
| 9 (default) | IDLE | Nothing above applies | `OneRandomBehaviour` (weighted) of `Idle`, `SetRandomWalkTarget`, `SetPlayerLookTarget`, `SetRandomLookTarget` | `FollowFormation` graze mode, `LeaderJourney`, idle clip act, sentry, cosmetic graze | Idle weights (new field or TOML), animation file clips |

Guard young has no own activity. A sensor that sees a player near a baby writes `ATTACK_TARGET`, and FIGHT runs (B18.6). Taming commands follow, stay and defend sit in CORE and IDLE as start conditions on an owner memory (B19.8).

**Fields that the B00 field list lacks** `[v]`. The layout needs `follow_range`, an activity window, a flee list, a reach band and idle weights. Dominance weights are named in `Dominance.java` as moving to species data, but the B00 list does not show them.

## Where another AI design may play better

**In game:** each row compares a Nublar AI choice with a rival design that may play better or cost less. The rows are options for the project lead. They do not change section 11 or the plan. Items on the plan's "Not in Beta" list appear only in the post-Beta table.

| # | Candidate | Nublar today | Rival (mod) | Player gain | Cost (effort, performance, vanilla feel) | Recommendation |
|---|---|---|---|---|---|---|
| C1 | Shared brain or a brain per species | One brain in `Dinosaur`. Species classes override only `canTarget` `[v]` | Goal list per species (Naturalist, AC). Subclass plus data enums (FAR) | Per-species quirks are easy to write | Six near-copies of the same lists. More code to port | Keep |
| C2 | Behaviour in datapack config | Behaviour in Java. Numbers in species data (plan B00) | Cobblemon configs: about 58 task types, MoLang, an editor | Pack makers change AI with no code | A large load-time layer. MoLang runtime. Six species do not need it | Keep |
| C3 | Leader choice | Dominance value plus no-damage contests (section 11.10) | Cobblemon leader tiers with a 4-8 block follow band | Calm herds with no contest noise | Tiers lose the decided contest drama | Adopt-in-part |
| C4 | Leader lookup | `GroupManager` holds the group and leader `[v]` | Naturalist lion: 24-block scan on each idle tick | None | The scan is an anti-pattern at 100 animals | Keep |
| C5 | Simple follow cases | `FollowFormation` for all following (679 lines) `[v]` | SBL `FollowEntity`, `FollowParent` | Same feel with less Nublar code | Must route through `HerdMoveControl` | Adopt-in-part |
| C6 | Flee | No flee. `Panic` commented out `[v]` | FAF avoid memory from a sensor. Naturalist `AvoidEntityGoal` each tick | A Gallimimus runs as decided | Low with a memory gate. High with per-tick goals | Adopt-in-part |
| C7 | Spreading slow work | `tickCount % 20` in places. Sensors at 20 t with no offset `[v]` | MCA interval table with id offsets | No aligned spikes | Small. SBL `scanRate` and `cooldownFor` cover the brain | Adopt-in-part |
| C8 | Attack sequencing | `AnimatableMeleeAttack(20)` plus animation booleans `[v]` | Mowzie ability sections in a capability | A readable wind-up, hit and recovery | A capability system is a second framework | Adopt-in-part |
| C9 | Pack model | Groups with dominance. Pack hunting not coded (plan B18) | AC Elder chain: push target each tick, wander bias, small-pack flee | Packs that break when outmatched | Chain walks are O(n). No re-election | Consider |
| C10 | Idle pool | Clip pick only. Brain walks groupless animals only `[v]` | SBL `OneRandomBehaviour` pools (SBL example), Cobblemon look and wander | Animals vary what they do, not only how they stand | Small. Uses existing clips | Adopt-in-part |
| C11 | Curiosity toward players | No look at players `[v]` | MCA greet, Cobblemon approach, Naturalist hide | Animals notice the visitor | Look only is cheap. Approach needs a design | Consider |
| C12 | Sleep onset | Rest when `IS_TIRED`. Herd lies down in rest hours on one tick `[v]` | Cobblemon drowsy 1/30 and rouse 1/240 per second. FAR start 1/200 per tick | A herd falls asleep and wakes one by one | Two numbers. No new state | Adopt-in-part |
| C13 | Ambient calls | Gather call at journey start only. Sounds for the Tyrannosaurus only `[v]` | FAR 200 t ambient interval. AC idle roar 1/600 | The park sounds alive | Needs sound assets (B24) | Consider |
| C14 | Grazing when not hungry | Herd "graze" holds a spot and idles `[v]` | AC periodic graze every 200-400 t, not need-driven | Herbivores visibly graze all day | Block loss if real. None if cosmetic | Adopt-in-part |
| C15 | Engage range per target class | One `FOLLOW_RANGE` 35 for every target `[v]` | AC: prey 32, players 8 or 12 | Carnivores hunt far but engage players near | One more species field | Consider |
| C16 | Flee when outmatched | Flee or stand fixed per species (D13) | FAR: a hit animal flees when its damage is under 50-80 % of the attacker's | A Triceratops still stands, a young one backs off | A rule that may blur the decided species roles | Consider |
| C17 | Packages by state | One brain with start conditions for babies and tamed animals `[v]` | MCA rebuilds the brain when the state changes | Clear per-state lists | Rebuild cost and complexity | Keep |
| C18 | Startle to herd alarm | `StartleWatch` per animal every 10 t. No spread `[v]` | Naturalist deer timer written to the herd. MCA and GV event alerts | One startled animal moves the herd | One write per event | Adopt-in-part |
| C19 | Dormancy far from players | Full brain everywhere `[v]` | Mowzie dormant bosses. AC no-action time gate | No visible change near players | Small. S25 covers idle work | Keep |

**C1. Shared brain or a brain per species.** Nublar has one brain in `Dinosaur.java`, and each species class only overrides `canTarget` `[v]`. Naturalist and AC write a goal list per species, and FAR adds species goals in subclasses with JSON enums for the AI `[v]`. Per-species lists make quirks easy, but six Nublar species share almost all logic (needs, herds, contests, fights). Keep the shared brain. Put species differences in species data, and keep a small set of override hooks on the species class, for example a display clip choice. Fix the two `canTarget` overrides that drop the parent result.

**C2. Behaviour in datapack config.** Cobblemon builds each brain from JSON configs with about 58 task types, MoLang conditions and an in-game editor `[v]`. It needs about 45 Gson type adapters for that layer `[v]`. The survey's Cobblemon note already says to keep activities in Java for six species. Keep code for logic. Expose numbers and lists (weights, ranges, flee list) as species data, which gives pack makers most of the gain.

**C3. Leader choice.** Section 11.10 decides dominance and rank contests that play `ThreatDisplay`. Cobblemon picks the highest matching tier and follows the leader inside a 4 to 8 block band `[v]`. The band is the useful part. It stops a grazing follower from walking back to the leader on every small gap. Keep dominance and contests. Consider a start and stop band for graze-mode cohesion only, for example start at 8 and stop at 4 blocks. The travel wedge (B26) stays as designed.

**C4. Leader lookup.** Naturalist scans a 24-block box on every idle tick to find the lion leader `[v]`. Nublar's `GroupManager` keeps the leader in the group object and reads members through `HerdBodies` `[v]`. Keep the group manager. It is the better design and needs no change for this point.

**C5. Simple follow cases.** `FollowFormation` handles graze spots, the trail, stragglers and rest spread `[v]`. SBL `FollowEntity` follows the closest entity that passes a predicate, starts beyond 8 blocks and stops at 4, with teleport off by default `[v]`. The hitbox design says an animal past the end of the trail follows the animal ahead. `FollowEntity` can carry that case and a groupless baby's follow. It sets a walk target, so the herd move control must still drive the body `[inf]`. Adopt-in-part for the simple cases. Keep the formation code for travel.

**C6. Flee.** No species flees today, and `Panic` is commented out `[v]`. FAF writes an avoid target from a sensor with a 5 to 10 s expiry `[v]`. Naturalist runs three `AvoidEntityGoal` per deer, each a box query every 2 ticks while idle `[v]`. S11 rates the FAF shape. On the AI side, the library rule points to SBL `AvoidEntity` for the motion. Gate it on the sensor-written `AVOID_TARGET`, so the threat test runs once per scan. Use SBL `Panic` for damage.

**C7. Spreading slow work.** S21 rates the MCA interval table. For the brain, SBL already supplies the hook: `scanRate` with a per-entity function and `cooldownFor` on behaviours `[v]`. Use those for sensors and behaviours. Keep the S21 table only for work outside the brain (needs step, stress, group join).

**C8. Attack sequencing.** Nublar attacks through `AnimatableMeleeAttack(20)` and toggles synced animation booleans `[v]`. Mowzie runs each attack as STARTUP, ACTIVE and RECOVERY sections in a capability and sends 2 varints per ability event `[v]`. The sections are the useful part. A capability framework would duplicate what an SBL behaviour already holds (start, tick, stop, run time). Put a small pure section track inside one SBL behaviour and start the clip with a GeckoLib 5 trigger. S10 gives the display rules.

**C9. Pack model.** The plan decides pack hunting with a shared target and flanking. AC keeps a chain of up to 8 members, pushes the Elder's target to all every tick and biases follower wander toward the head `[v]`. Its small-pack flee (pack below 4 against a Grottoceratops) makes packs break when outmatched `[v]`. Nublar's group manager and S9 push-on-change already beat the chain. A morale flee is the only new idea. B18.9 rates it Defer, so this stays Consider.

**C10. Idle pool.** Nublar varies only the standing clip, with a main idle weight of 6 against 1 per variant `[v]`. The brain adds nothing for a grouped animal at its graze spot. SBL's example picks between a random walk and an idle of 30 to 60 ticks with `OneRandomBehaviour` `[v]`. Add a weighted pool in IDLE that turns clips into acts: stop and sniff, look at a mate, sit for a short time. The pool uses the table in "What an animal does when no need is urgent". It needs no new assets.

**C11. Curiosity toward players.** No species looks at a player today `[v]`. MCA greets known players within 32 blocks every 100 ticks `[v]`. Cobblemon pastured creatures approach their owner, and Naturalist small animals hide within 5 to 6 blocks `[v]`. A head turn toward a still player within 8 blocks costs one SBL `SetPlayerLookTarget` and no clip. An approach needs a design rule for safety near carnivores. Consider look only (open item A4).

**C12. Sleep onset.** Nublar rests on `IS_TIRED`, and in rest hours a whole herd lies down when its leader rests `[v]`. Cobblemon rolls a drowsy chance of 1/30 and a rouse chance of 1/240 once per second inside its light and time window `[v]`. FAR starts sleep with 1/200 per tick `[v]`. A per-second roll makes a herd settle and wake one animal at a time, which reads as natural. Add the two chances to the REST activity.

**C13. Ambient calls.** Section 11.10 decides warn, gather and threaten calls. Today only the journey-start gather call exists, and only the Tyrannosaurus has sounds `[v]`. FAR plays an ambient sound every 200 ticks and AC rolls an idle roar at 1/600 per tick `[v]`. A rare ambient call makes a park sound alive. It waits for sound assets in B24. Consider a call cooldown of about 1,200 ticks once sounds exist.

**C14. Grazing when not hungry.** Section 11.10 decides block grazing with reach, and S6 specifies the hungry case. AC grazes on a timer of 200 to 400 ticks whether or not the animal is hungry `[v]`. Nublar's herd "graze" state holds a spot and idles `[v]`. A cosmetic graze clip at the spot, with no block removed, makes herbivores look busy without block loss. Only hungry animals consume blocks. The Gallimimus file has no eat clip, so it needs B24 art first (open item A5).

**C15. Engage range per target class.** Every Nublar species has `FOLLOW_RANGE` 35 for all targets `[v]`. AC hunts dinosaur prey to 32 blocks but targets players only within 8 or 12 `[v]`. A short player engage radius lets a carnivore hunt widely without chasing every visitor at 35 blocks. Detection (B19) and territory (B19.7) already shape this. Consider one extra species field.

**C16. Flee when outmatched.** Decision D13 fixes flee or stand per species. In FAR, a hit animal at half health flees when its damage is under 80 % of the attacker's. At any health, the limit is 50 % `[v]`. It would let a young Triceratops back off from an adult Tyrannosaurus. It also blurs the decided roles. Consider it only for babies and young animals.

**C17. Packages by state.** MCA chooses one of 8 package sets by state and rebuilds the brain on a change `[v]`. Nublar keeps one brain and gates babies and tamed animals with start conditions `[v]`. SBL supports runtime edits through `BrainUtil.addBehaviour` and `removeBehaviour` `[v]`. A rebuild adds a second way to change behaviour. Keep one brain with conditions.

**C18. Startle to herd alarm.** `StartleWatch` lets each animal notice an approach by itself every 10 ticks `[v]`. Naturalist writes one panic timer to every deer in an 8 by 4 by 8 box when one is hurt `[v]`. MCA and GV alert on events, not by polling `[v]`. B18.2 rates the hurt-event write. On the AI side, a startle on a flee species can also trigger that write, so the herd bolts as one. Adopt-in-part.

**C19. Dormancy far from players.** Mowzie bosses stay dormant until a target is within 8 blocks `[v]`. AC idle strolls stop at a no-action time of 100 `[v]`. SBL has no brain tick-skip `[v]`. S25 already slows idle work beyond 128 blocks without stopping needs. Keep the S25 route. A full dormant state would freeze needs and breeding, which section 11 keeps running.

**Post-Beta options.** These items are on the plan's "Not in Beta" list or the backlog. Nothing here is a Beta recommendation.

| Feature | Plan status | Nearest AI precedent | Note |
|---|---|---|---|
| Turf wars between large carnivores | Not in Beta (section 11.13) | None found. Local `LargeCarnivoreFight` and `StartTurfWar` stay unwired | Would be a FIGHT variant with a contest start |
| Non-dinosaur ambient animals | Backlog | cosy-critters client particles with caps 50, 8, 16 `[v]`. Naturalist animals `[v]` | Client-only critters cost the server nothing |
| Disease behaviour (lethargy, isolation) | Backlog | None found | A REST bias and a group-leave rule |
| Movement-based vision | Not in Beta | None found | A detection input, not a brain change |
| Riding with a driverless fallback | Not in the plan | AC Tremorsaurus, Cobblemon ride styles `[v]` | The brain keeps running when no rider steers |

## Open items for the lead

These five items are real gaps that the plan and section 11 do not answer. Each has a default that ships as a config value or a data field if the lead does not answer.

| Id | Module | Question | Default if unanswered | Why it matters |
|---|---|---|---|---|
| A1 | B00, B17 | Which species data field holds the sleep and activity window? Plan B00 deletes `DinoBehaviourData` and its `nocturnal` flag, and the B00 field list has no replacement | Add `activity_window` (diurnal, nocturnal, cathemeral) to species data. Interim values from today's configs: Tyrannosaurus, Velociraptor and Dilophosaurus nocturnal, the others diurnal | `Rest`, `LeaderJourney` and `FollowFormation` read the flag today `[v]`. Without a field the herd sleeps at the wrong time after B00 |
| A2 | B17 | Does REST switch by `SmartBrainSchedule.byDaytime` or by a rest-window memory? | `byDaytime` built per species, with AVOID and FIGHT in the schedule-ignoring set. A GameTest checks that an urgent need still wins at night. Fall back to a memory if it does not | The library rule prefers the schedule. The notes do not show how a schedule ranks against a met activity `[inf]` |
| A3 | B17, B24 | Which existing clips may the idle pool play as brain acts? | All clips that `AnimationChoice` already lists for IDLE, plus `speak` for greetings. No new clips. D14 stays unbuilt | Idle variety depends on clips. Spare clips (`sneeze`, `spit`, `speak`) exist for some species only |
| A4 | B19 | Do calm animals turn their head toward a still player within 8 blocks? | Yes for herbivores, by SBL `SetPlayerLookTarget`. No approach. Carnivores look only after detection | Without it animals ignore visitors. An approach rule near carnivores needs care |
| A5 | B17 | Does a herbivore that is not hungry play a cosmetic graze at its herd spot? | Yes, with no block removed, for species with an eat clip (Triceratops, Brachiosaurus). The Gallimimus waits for an eat clip | Section 11.10 decides block eating when hungry. It is silent on idle grazing |

## Sources

**Research notes** in `research_notes/Reference mod survey for Beta/`: `ai_library_layer.md`, `creature_behaviour_mods.md`, `alexs_caves_dinosaurs.md`, `fossils_revival_and_ice_and_fire.md`, `cobblemon_architecture.md`, `people_and_structures_mods.md`, `large_mob_infrastructure_and_vehicles.md`, `vanilla_detection_and_stress_precedents.md`, `project_nublar_fit.md`. Skimmed: `vanilla_plus_and_feeders.md`, `cobblemon_wiki.md`, `other_mod_wikis.md`.

**Project documents:** `docs/codebase-analysis.md` sections 6, 7.8, 7.9, 11.9, 11.10, 11.13, 11.16. `docs/project-plan.md` global constraints, B00, B16 to B19, B26, backlog and decisions register. `reports/Reference mod survey for Beta.md`.

**Local code** (read only, 2026-10-10). Root: `common/src/main/java/net/dumbcode/projectnublar/entity/`. Files: `dinosaur/Dinosaur.java` L535-L696, the six species classes, `dinosaur/animation/` (`AnimationChoice`, `DinoOneShots`, `OneShotAction`, `StartleWatch`, `IdleSway`), `ai/tasks/` (`Rest`, `Eat`, `SetHunting`), `ai/behaviour/actions/` (`ThreatDisplay`, `DinosaurLookAtTarget`), `ai/behaviour/social/` (29 classes). Data: `assets/projectnublar/animations/entity/*.animation.json`, `data/projectnublar/config/dinosaurs/*_config.json`, `init/SoundInit.java`.
