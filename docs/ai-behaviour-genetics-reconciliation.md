# AI, behaviour and genetics reconciliation

Date: 2026-10-10. Status: recommendation for the project lead. It changes no code and no plan module. The lead can move the section "Plan changes" into [project-plan.md](project-plan.md) row by row.

**Answer.** The sources agree on most of the scope. They disagree in 20 places, and 22 Beta items have no definition in any source. This page settles each disagreement by the lead's decisions (section 11 of [codebase-analysis.md](codebase-analysis.md), with the fence parity rules F1-F32 of 2026-10-10). It gives each gap a default that ships as a config value. It also joins AI, behaviour and genetics into one loop. The genome sets the animal's numbers. The numbers set its needs and acts. The needs and acts set what the paddock must supply. The result:

- 26 genes, each with one visible effect, most with a paddock effect
- one brain: CORE plus nine activities
- 10 pack-hunting defaults (P1-P10)
- 5 hunting and avoidance defaults (H1-H5)
- a donor catalogue of all 35 table donors
- 66 plan changes in 14 modules
- 29 questions for the lead (one answered)

**Labels.** "Lead" means a decision in section 11, a fence parity row or an answered register row. "Default" means my proposal, which ships as config until the lead rules. `[v]` means checked in the working tree on 2026-10-10. `[inf]` means inference.

**Fence parity changes the paddock side.** Since 2026-10-10 the fence plays like the 1.12.2 original. A live wire does 1 HP and knockback to anything (F25). Only a Tyrannosaurus breaks a fence, and only an unpowered one (F29). Any living entity can climb an unpowered wire (F26). Wire health, post knockdown, tier damage and stress-driven fence attacks are gone. So genes no longer act on fences through strength. They act through height, jump and hyper intelligence, and power uptime becomes the main paddock skill.

## The loop the player plays

**In game:** the player sequences DNA, picks donors and edits genes. The hatched animal shows the genome in its look, its stats and its acts. Its needs ask the park for water, food, company and a fence that stays powered. A park that supplies them keeps the animal calm, and calm animals breed. The young inherit the parents' genome, so the player's choices carry into the next generation.

```text
  Sequencer (DNA %, donors, edits)
        │  GenomeEffects.apply (B07)
        ▼
  Animal numbers ── look: colours, skin, morph, size, gender (B09)
        │            feel: health, speed, strength, stamina (B16)
        │            mind: aggression, intelligence, group size, window, tamability (B17-B19)
        ▼
  Needs and acts ── hunger, thirst, stamina, company, stress (B17)
        │            herd, hunt, flee or stand, warn, sleep, idle (B17-B19, B26)
        ▼
  Paddock demand ── water in reach, food supply, company, powered fence of the right height, open ground (B15, B20)
        │
        ▼
  Stress (food and water, company; live bait lowers it) ── high: aggression, no breeding, no taming
        │                                                    low: taming, breeding (B18, B19)
        ▼
  Bred young: parents' midpoint + variation (B18) ──► next generation
```

The paddock is the player's answer to the genome. Calm animals pass the genome on, so a bad paddock also ends a line.

| Step | The player sees | Target |
| --- | --- | --- |
| Clone with no edits | Each animal differs: size, stats, colour of each layer. About 1 in 20 is albino or melanistic. Filler donors change the animal by their table values | Lead (11.6) |
| Edit genes | The edit shows in the animal on the first day after hatch, in each of the four gene groups | Lead (11.6) |
| Animal lives | 12 to 15 visible acts per species, at least 5 of them idle acts. No two herd mates do the same act on the same tick | Comparison report |
| Paddock fails | Stress rises. The animal turns aggressive and stops breeding. A fence without power lets animals climb out, and a T. rex breaks it | Lead (11.10, F26, F29) |
| Paddock works | Stress falls. The herd grazes, sleeps and wakes one by one, and pairs nest | Lead (11.10), comparison C12 |

## Sources and their weight

When two sources disagree, the higher row wins.

| Rank | Source | Date | What it holds |
| --- | --- | --- | --- |
| 1 | Section 11 of `codebase-analysis.md`, fence parity F1-F32, answered register rows (D06, D14, D38, D39) | 2026-10-05 to 10-10 | The lead's decisions. Parity wins over older 11.11 fence rules |
| 2 | Herd spec decisions 1-33 and playtest rulings | 2026-10-08 to 10-10 | The lead's herd decisions, Gallimimus only |
| 3 | [project-plan.md](project-plan.md) module scope and register defaults D13, D15-D37 | 2026-10-10 | Executable scope. Survey items are not lead decisions |
| 4 | [reference-recommendations-review.md](reference-recommendations-review.md) | 2026-10-06 | 32 items rated. The lead accepted 15 additions |
| 5 | `reports/Entity AI and behaviour comparison.md` | 2026-10-10 | Behaviour matrix, brain layout, candidates C1-C19, open items A1-A5 |
| 6 | The two survey reports | 2026-10-10 | Specs S1-S27 and T1-T15, open items O1-O21 |
| 7 | Design documents in `PN Feature Docs` | 2021-2026 | Gene catalogue, donor table, special genes, byte values |
| 8 | Code in the working tree on `kash's-slop` | 2026-10-10 | What runs today `[v]` |

## What the code does today

**In game:** a Gallimimus herd forms, picks a leader, grazes, travels in a wedge and calls. A T. rex hunts when hungry. Every species drinks, eats dropped food, rests and fights back. No species warns, flees, breeds, tames, grazes blocks or looks at a player. Of the 21 genes, only size, aggression, herd size, pack size, gender and colour change anything, and colour is lost on save.

| Area | State `[v]` | Evidence |
| --- | --- | --- |
| Herd layer | Works, tested in 6 playtests on the Gallimimus. The P12c cliff code exists but nobody has built it. Playtest 6 not reported | 32 Java files in `entity/ai/behaviour/social` |
| Hunger and thirst | Broken. `IS_HUNGRY` never clears. Starvation kills at once. A thirst counter kills on day 3 even when the animal drinks. Each load drains extra need | `DinoNeedsUtils.java:153-208`, `Dinosaur.java:783-868` |
| Stamina | Works. Run flag fixed by the herd work | `Dinosaur.java:206-214` |
| Social, stress | No code. `tickSocial` is empty | `DinoNeedsUtils.java:243` |
| Hunting | T. rex only. Six `canTarget` overrides drop the parent result | Five species classes and `HerbivoreDinosaur` |
| Feeder | The animal walks to it and gains nothing | `EatFromMeatFeeder.java:48-50` |
| Fence breaking | Not reachable. Fence nodes open only when all four needs are 0 | `FenceAwareNodeEvaluator`, `BreakFenceBehaviour` |
| Breeding, taming | Not reachable | `SetMateFromNearbyDinosaurs` not in the brain. `PLAYER_REPUTATION` never created |
| Growth | Hatch spawns an adult. Stages are steps. Hitbox does not grow | `IncubatedEggItem.java:39-47` |
| Genes | 15 of 21 genes have no reader. Donor data is a multiplier and the code reads a percent. Colours not saved | `DinoData.java:127-221`, `GeneDataProvider.java` |
| Edit checks | No server check of 50, 70 or 80 percent, of donor slots or of ranges | `SequencerBlockEntity.java:528-530`, `UpdateEditInfoPacket.java:23-26` |
| Fossil DNA | A fossil gives 0 % DNA (yield lookup disabled by a comment) | `DNAData.java:46-52` |
| `ThreatDisplay` | Plays the T. rex roar sound and `roar` clip for all species, and only in rank contests. Gallimimus and Dilophosaurus have no `roar` clip. Only the T. rex has sounds | `ThreatDisplay.java:39-40`, `sounds.json` |

## Where the sources disagree

Each row names the losing claim and the rule that settles it. Six rows (X3, X11, X12, X13, X14, X20) need the lead and appear again under Questions.

| # | Topic | Disagreement | Resolution | Basis |
| --- | --- | --- | --- | --- |
| X1 | Gender pick | Design doc: sure at 75 %. Lead: sure at 80 % | 80 %, 30 % random below | Lead 11.6 |
| X2 | Edit threshold | Design doc: stats at 50 %, physical traits at 55 %, attack at 90 %. Lead: editing at 70 % | 70 % for every gene. Isolation still gates each gene. Drop the 90 % attack rule | Lead 11.6 |
| X3 | Special-gene slots | Design doc: 1, 2, 3 slots at 50, 75, 100 %. Plan B10: one slot | One slot in Beta. Ask the lead (Q12) | Plan B10, design doc |
| X4 | Stress inputs | Design doc: space, food and water, engagement, safety, with thresholds 60 male and 50 female. Lead: food and water, and company | Two inputs. One shared threshold (D13 default), stored as a per-species field for later tuning | Lead 11.10, D13 |
| X5 | "Lower Threshold Stress Level" gene | Design doc lists it as an eighth special gene | Not in Beta. The lead named seven | Lead 11.6 |
| X6 | Aggression direction | Design doc: a higher threshold means fewer random attacks. Code and dominance: a higher value means more aggressive | A higher gene value means more aggressive everywhere | Code, comparison |
| X7 | Gene value scale | Donor data stores multipliers (1.5). Code, sliders and tooltips read percent points. A 50 % donor at 1.5 gives +150 | One scale: percent deltas as the DNA Effects table writes them. A neutral donor adds 0 | Lead 11.6 (table wins) |
| X8 | Activity window | Plan B00 deletes `DinoBehaviourData` and its `nocturnal` flag. A `nocturnal` gene also exists. Comparison A1 asks for a field | Species field `activity_window` (diurnal, nocturnal, cathemeral). The nocturnal gene shifts it (G7) | Comparison A1, gene registry |
| X9 | Group size | Config has one `group_size` (Gallimimus 20 as a herd test value). Section 11.16 gives ranges. B00 has min and max | B00 `group_size_min` and `group_size_max` from 11.16. Gallimimus returns to 6-12 when herd tests end. Herd and pack size genes scale both | Lead 11.16, plan B00 |
| X10 | Brain activities | Plan B17: core, idle, rest, hunt, fight, then social and breeding in B18. Comparison: CORE plus nine, with avoid, feed and herd travel | The comparison layout. It contains the plan list | Plan B17, comparison |
| X11 | Herd journeys in a paddock | Lead: journeys of 156-314 blocks in open world. No source says what a fenced herd does. Playtest 6 logged two NO_PROGRESS ends | Inside a fence, journey targets come from the reachable area. Ask the lead (Q1) | Gap between herd spec and B15 |
| X12 | Lab hatch success | Lead: a separate hatch rate applies to every placed egg (11.7). T. rex card: 5 %. Lead: about 50 machine minutes per dinosaur | At 5 % a player prints about 20 eggs, about 17 hours of machine time, per live T. rex. Ask the lead (Q2) | Lead 11.7 and 11.16 conflict |
| X13 | Camouflage and thermal detection | Lead: all seven special genes in Beta. Design doc: camouflage fits hybrids and Carnotaurus, thermal fits hybrids, Spinoceratops and Mutadon. None is a Beta species | Extend B10's camouflage data list to thermal detection, with Beta defaults. Ask the lead (Q3) | Lead 11.6, design doc, plan B10 |
| X14 | Default special genes | Design doc: Velociraptor carries hyper intelligence. Dilophosaurus carries poison. Plan silent | Species data `default_special_gene`. Ask the lead (Q4) | Design doc |
| X15 | Fence breaking | Code: a starving carnivore with prey outside breaks fences, and fence nodes open when all needs are 0. Lead parity: only a T. rex breaks a fence, only an unpowered one, 50 % per check | Parity rule. Fence nodes always blocked. Delete `allNeedsAtZero` | Lead F27, F29 |
| X16 | Grouped carnivores hunting | The herd work took out the `hasGroup` guard in `SetHunting`, so grouped carnivores hunt now. Lead: attack joins herd play only after the B26 playtests | Restore the guard until the B26 playtests pass. B18 pack rules then govern grouped hunts | Herd scope rule, plan B17 |
| X17 | Immunity | Design doc: disease and poison. Lead: disease out of Beta | Immunity shortens Poison. Mooshroom "poison resistance +50" maps here | Lead 11.6, plan B10 |
| X18 | Gender-swap donor | Design doc: common reed frog. Lead: vanilla frog | Vanilla frog | Lead 11.6 |
| X19 | Unmet needs and fences | Section 11.10: an animal with unmet needs attacks fences to escape. Fence parity: only the T. rex rule (F29) | Parity wins. Unmet needs give aggression, health loss and death only. Plan B17 already says so | Lead F29, plan B17 |
| X20 | "Quirks" | Lead 11.6 and plan B10: quirks are clucking, sound imitation, cocoa allergy and item carrying. Edit risk also asks for "abnormal behaviour", which no source defines | Keep B10 quirks as donor extras. Name edit-risk outcomes `risk_behaviours` (G1). Ask the lead (Q5) | Lead 11.6, plan B10 |

## Gaps no source defines

Each gap gets a default. Each default is a config value or a data field, so playtest can tune it.

| # | Gap | Default | Module |
| --- | --- | --- | --- |
| G1 | What "abnormal behaviour" from edit risk is | At hatch, `GenomeRisk` rolls one entry from a `risk_behaviours` data list. Beta list: stress threshold -20, activity window flipped, group-averse (max group 1), aggression +30 %. Every risk behaviour keeps the warning before an attack (11.10). The debug stick names it | B07, B11 |
| G2 | Which stat falls when one rises | Five pairs in data, coefficient 0.5. Speed up lowers health. Strength up raises hunger. Size up lowers speed and raises hunger. Defense up lowers speed. Health up lowers regen. The trade-off grows with donor share and edit count, as 11.6 says for risk | B07 |
| G3 | What the intelligence gene does | It adds to the species intelligence rating. The rating adds to the detection chance (B19) and to how long the animal follows a scent trail. Noticing an unpowered fence stays with hyper intelligence (11.11, B10) | B17, B19 |
| G4 | What the tamability gene does | It scales the reputation threshold for taming (B19.9) | B19 |
| G5 | What the jump gene does | It raises jump height. The cap stays below the low-security fence height (3 blocks). Horse and frog effects skip large species, as the table says | B16 |
| G6 | What heat resistance does | It cuts fire and lava damage by its percent | B16 |
| G7 | How the nocturnal gene acts | At +50 % or more it moves the window one step: diurnal to cathemeral to nocturnal. At -50 % or less it moves one step back | B07, B17 |
| G8 | Scent genes in the donor table | One new gene `scent`. It scales food, water and scent-trail sensor range. Camel's water scent maps here. Hyper intelligence adds its +15 % on top | B07, B17, B19 |
| G9 | Vision effects (frog low-light vision, armadillo vision -35) | One sight-range factor. Low-light vision scales it at night only, general vision at all times. Movement-based vision stays out of Beta | B19 |
| G10 | Growth-rate gene | Not in Beta. The lead's four gene groups do not name it | None |
| G11 | Social need against company | Plan B17 defines "alone" (below `group_size_min`, stress +1 per step). Section 11 also lists a social need. Default: one mechanic, no separate bar (Q13) | B17 |
| G12 | What a lab hatchling follows | The nearest adult of its species within 16 blocks. With none, it stays within 8 blocks of the hatch point and counts as alone | B16 |
| G13 | Mixed-species paddocks | Carnivores rank all prey by clade, also other park animals. Herbivores form no mixed groups, but alarms cross species (H5). No stress input, because the lead named two | B17 |
| G14 | How donor share scales a donor effect | A donor at the 50 % maximum share gives its full table value. Below that it scales in a line (the code's `share x 2`) | B07 |
| G15 | Which fence a species needs | Fence parity holds every species while the wire is live. The species page states the fence height its body and jump need, and whether it breaks unpowered fences (T. rex only) | B15, B24 |
| G16 | What idle acts play | The comparison's idle pool with existing clips only (A3 default). Spare `sleep` clips play in REST for Velociraptor and Dilophosaurus | B17 |
| G17 | What the table's "Hunger" and "Thirst" rows mean | How fast the need falls per day. `stomach_capacity` carries the table's Hunger (the design lists "Hunger / Stomach capacity" as one gene). `eat_rate` is bite speed at food. `thirst` carries the table's Thirst | B07, B17 |
| G18 | Mobs in 26.2 with no table entry | Lead (Q25, 2026-10-10): allay, strider, tadpole, happy ghast and sulfur cube give no usable DNA. Default for the rest: trader llama uses the llama row, all other mobs give no usable DNA | B04, B07 |
| G19 | Mob variants (wolf, cow, pig, chicken, frog, fox, cat, horse, rabbit, parrot, mooshroom, axolotl) | Each variant keeps its own drive entry (lead 11.5) and its own colour palette. Its gene effects equal the base animal's (Q26) | B06, B07 |
| G20 | Table values with no sign (polar bear speed 20, horse speed 15, tamability 40, jump 30, dolphin group size 20) | Read as plus (Q27) | B07 |
| G21 | The table's "Group size" | It sets both `herd_size` and `pack_size`. The species reads the one that fits it. Today the code sets one per donor | B07 |
| G22 | Conditional sniffer effects | Sniffer DNA adds torchflower seeds to the herbivore diet, with +25 % regen while the animal eats them. A nest egg on moss hatches 15 % slower (the table writes "+15" for a decrease) | B10, B18 |

## Gene effect map

**In game:** every gene changes one thing the player can see or measure, and most change what the paddock must supply. "Status" is the code today.

**Rule.** Effective value = species base x (1 + gene % / 100) x growth fraction x clone variation. One function, `GenomeEffects.apply`, writes it at spawn, on a genome change, on a growth step and on reload (plan B07). A bred or edited value stays inside the edit limits. Villager DNA gives every gene a random value each time the player uses it (lead 11.6).

| Group | Gene | In-game effect | Paddock or care effect | Example donors (table) | Status |
| --- | --- | --- | --- | --- | --- |
| Core | `health` | Max health | Longer starvation window (1 HP per 80 ticks) | Turtle +75, Polar Bear +35 | Not read |
| Core | `health_regen` | Regen ticks per half heart | Recovers from fights and shocks faster | Axolotl +55, Sniffer +43 | Not read |
| Core | `strength` | Attack damage | Kills prey and live bait faster. No fence effect after parity | Polar Bear +50, Hoglin +50 | Not read |
| Core | `defense` | Armor | Survives fights longer. Shocks ignore armor (F25) | Polar Bear +75, Armadillo +55 | Not read |
| Core | `speed` | Movement speed | A fast raptor catches a player before its stamina ends | Fox +70, Rabbit +50 | Not read |
| Core | `size` | Model, hitbox and parts scale | A taller animal needs the taller fence. Larger reach band. Needs wider gaps (B26). More hunger | Hoglin +75, Rabbit -75 | Render and herd box only |
| Needs | `stomach_capacity` | Hunger fall per day (G17) | Feeder points used per day | Panda +75, Llama +30, Camel -25 | Not read |
| Needs | `eat_rate` | Bite speed at food (G17) | Time at the feeder. Animals per feeder | Panda -10 | Not read |
| Needs | `thirst` (new, B07) | Thirst fall per day | Water can sit farther from the graze area | Camel -30 | Missing |
| Needs | `energy` (new, B07) | Stamina max | Longer chases. Longer herd journeys | Camel +35, Sniffer -25 | Missing |
| Behaviour | `aggression` | Dominance weight 0.2. Warning length. Chance to attack a player in territory | Edited carnivores need wider buffers at viewing points | Hoglin +87, Polar Bear +45 | Dominance only |
| Behaviour | `intelligence` | Detection chance and scent-trail time (G3) | Notices keepers and visitors sooner | Wolf +75, Dolphin +75, Chicken -50 | Not read |
| Behaviour | `herd_size` | Herbivore group min and max | The player needs a bigger herd to avoid "alone" stress | Cow +50, Sheep +50 | Group capacity only |
| Behaviour | `pack_size` | Carnivore group min and max | More raptors per paddock. Bigger flanks | Wolf +75 | Group capacity only |
| Behaviour | `tamability` | Taming reputation threshold (G4) | Faster bond | Wolf +100, Hoglin -75 | Not read |
| Behaviour | `nocturnal` | Activity window shift (G7) | Animals awake at night. Sleep spots differ | Spider +75, Bat +25 | Not read |
| Behaviour | `scent` (new, G8) | Food, water and trail sensor range | Finds feeders and water at the paddock edge | Pig +37, Wolf +38 | Missing |
| Breeding | `fertility` | Breeding roll | Population growth rate | Panda -25, Mule -40 | Not read |
| Breeding | `egg_clutch` (new, B07) | Clutch size | Nest space and egg count | Frog +25 | Missing |
| Breeding | `gestation_time` (new, B07) | Days from mating to laying | Time between clutches | Armadillo +40 | Missing |
| Survival | `immunity` | Poison duration | Safer with poison carriers in one paddock | Turtle +125, Mooshroom +50 (poison) | Not read |
| Survival | `underwater_capacity` | Breath and the water path cost (B16) | Can cross deep water inside the paddock | Turtle +125, Dolphin +75 | Not read |
| Survival | `heat_resistance` | Fire and lava damage cut (G6) | Lava moats hold it less | Hoglin +55 | Not read |
| Survival | `jump` | Jump height, capped (G5) | Low fence height still holds (Q8) | Frog +43, Horse +30 | Not read |
| Look | `gender` | Texture set, size weights, breeding role | Pairs for breeding | None. Choice at 80 % | Works for creative eggs only |
| Look | `color` | Per-layer tint, primary and secondary modes | None | Tropical fish colours | Lost on save |
| Look | Skin variant (choice) | Biome-decided variant, free pick at 100 % | None | Fossil biome shares | Missing |
| Look | Morph (rolled) | Albino or melanistic, about 5 % | None | Rabbit (older design: albinism chance) | Missing |
| Special | Special gene (choice) | One of seven (B10) | Hyper intelligence finds unpowered fence sections. Poison raises keeper risk | Cave Spider, Bee, Glow Squid, Frog, Wolf, Parrot | Missing |

## Donor catalogue

**In game:** every vanilla animal the player samples gives the effects of its row, and only those. The catalogue compares three sources for each donor:

- the current design table, `Vanilla Animal DNA Effects.txt` (percent values). It wins where sources differ (lead 11.6).
- the older list "Overworld Animal DNA modifiers" in the main design doc `Project_ Nublar (2).docx`. It gives directions only (up or down), no numbers.
- the code, `GeneDataProvider.java`, converted from multipliers to percent (1.5 is +50). "Palette" means the colour sets the code stores for the donor.

**Count.** 35 donors in the table (the four code fish share one Fish row). 15 code values disagree with the table. 4 code entries are empty. 6 table donors have no code entry. 16 table effects have no code value. The older doc has no scent effects, which explains most of the missing code values. The code panda speed (-20) follows the older doc ("reduce speed"), not the table (+10).

| Donor | Current table (wins) | Older design doc | Code today `[v]` | Extras, quirks, colour | Special-gene donor | Status |
| --- | --- | --- | --- | --- | --- | --- |
| Bat | Size -25, nocturnal +25 | Smaller. Mild day and night reversal | Size -25, nocturnal +25. Palette 1 | None | No | Matches |
| Polar Bear | Strength +50, defense +75, speed 20 (unsigned, G20), size +50, health +35, aggression +45, scent +30 | Strength, size, health, defense up. Cold resistance | Strength +50, defense +75, speed +50, size +50, health +35. Palette 1 | None | No | Speed differs. Aggression and scent missing. Cold resistance only in the older doc |
| Panda | Strength +25, defense -50, eat rate -10, stomach (table "Hunger") +75, fertility -25, speed +10 | Strength and hunger up. Speed, defense, fertility down | Strength +25, defense -50, eat rate -10, stomach +75, fertility -30, speed -20. Palette 2 | None | No | Speed follows the older doc. Fertility differs |
| Turtle | Defense +50, health +75, underwater +125, speed -50, immunity +125 | Same directions. Skin shift to turtle green | Defense +50, health -25, underwater +125, speed -50, immunity +125. Palette 2 | Turtle green skin shift (older doc only, Q29) | No | Health has the wrong sign |
| Wolf and 8 variants | Group size +75 (G21), speed +25, intelligence +75, tamability +100, scent +38. Variants Ashen, Black, Pale, Rusty, Snowy, Spotted, Striped, Woods | Pack size up for carnivores. Intelligence, taming, speed up | Pack size +75, speed +25, intelligence +75, tamability +175. Palette 1. No variants | Variant palettes (G19) | Hyper intelligence | Tamability differs. Scent missing. Group size only on `pack_size` |
| Hoglin | Heat resistance +55, size +75, tamability -75, strength +50, defense +30, aggression +87 | Not listed | Heat +50, size +75, tamability -50, strength +50, defense +30, aggression +85. Palette 1 | None | No | Three values differ |
| Axolotl | Regen +55, tamability +15, aggression +15, size -35 | Not listed | Empty | None | No | Empty entry |
| Goat | Group size +25, intelligence +25, strength (table "Attack") +16, speed +10 | Not listed | Empty | Grass in the herbivore diet | No | Empty entry |
| Dolphin | Underwater +75, speed +35, intelligence +75, group size 20 (unsigned) | Underwater, speed up | Underwater +75, speed +35. Palette 1 | None | No | Intelligence and group size missing. Code follows the older doc |
| Fox | Speed +70, intelligence +30, scent +30 | Speed, intelligence up | Speed +70, intelligence +30. Palette 2 | Snow fox variant (G19) | Hyper intelligence | Scent missing |
| Sheep | Intelligence -25, group size +50 | Herd size up, intelligence down | Intelligence -25, herd size +50. Palette 2 | Grass in the herbivore diet | No | Matches. Diet gene missing |
| Cow | Group size +50 | Herd size up. Speed down | Herd size +50 | Grass in the herbivore diet. Cold and warm variants (G19) | No | Matches. Diet gene missing. Speed only in the older doc |
| Pig | Defense +20, immunity +25, scent +37 | Defense, immunity up | Defense +20, immunity +25. Palette 1 | Cold and warm variants (G19) | No | Scent missing |
| Rabbit | Speed +50, size -75 | Speed up, size down. More albinism | Speed +50, size -25. 7 coat palettes | Higher albino chance (older doc only, Q29) | No | Size differs |
| Chicken | Intelligence -50 | Intelligence down. Skin shift to white and red. Clucking | Size -50. Palette 2 (white, red) | Skin shift to white and red. Small chance of clucking. Cold and warm variants (G19) | No | Wrong gene: size, not intelligence |
| Parrot | Intelligence +25, tamability +30 | Intelligence, taming up | Intelligence +25. 5 palettes | Sound imitation, cocoa allergy. Palette to red, cyan, grey. Colour blue or green ("green unlocks Delta", Q28) | Hyper intelligence | Tamability missing |
| Llama | Defense +25, stomach +30, tamability +25 | Defense, hunger, taming up | Defense +25, stomach +30, tamability +25. Palette 1 | Item carrying. Small chance of a spit with no venom | No | Matches. Quirks missing |
| Horse | Speed 15, tamability 40, jump 30 (all unsigned), scent +33. Large species excluded | Speed, strength, jump, taming up | Jump +30, tamability +40. 7 coat palettes | Coat colours | No | Speed and scent missing. Strength only in the older doc |
| Mule | Speed -20, tamability +30, fertility -40, intelligence +20, scent +30 | Speed down. Transport capacity, taming, infertility up | Speed -20, tamability +30, fertility -50, intelligence +20. Palette 1 | Transport capacity (older doc only, Q29) | Hyper intelligence | Fertility differs. Scent missing |
| Donkey | Speed -20, intelligence +25, strength +25, scent +22 | Speed down, strength up | Speed -20, intelligence +25, strength +25. Palette 1 | None | No | Scent missing |
| Cat | Intelligence +50, tamability +45, size -60, scent +30 | Intelligence, taming up. Size and group size down | Intelligence +50, tamability +45, size -40 | Palette from a random cat skin | No | Size differs. Scent and palette missing. Group size only in the older doc |
| Ocelot | Speed +50, tamability -35, size -60, scent +32 | Speed up. Taming, size, group size down | Speed +50, tamability -30, size -60. Palette 2 | None | No | Tamability differs. Scent missing |
| Mooshroom | Immunity (table "Poison resistance") +50 | Poison resistance | No genes. 2 palettes (brown, red) | Mushrooms in the herbivore diet | No | Immunity missing |
| Fish | Underwater +30, size -50, health -50 | Underwater up, health down | Cod, salmon, pufferfish, tropical fish: underwater +30, size -50, health -50. Palette 2 each, tropical fish 16 | Tropical fish: the 15 colours that unlock colour editing (black excluded) | Salmon: thermal detection | Matches |
| Squid | Regen +25, underwater +50, health -30 | Regen, intelligence, underwater up | Regen +25, underwater +50, health -30. Palette 2 | None | Thermal detection | Matches. Intelligence only in the older doc |
| Glow Squid | Regen +30, underwater +50, health -30 | Not listed | Empty | None | Bioluminescence, thermal detection | Empty entry |
| Bee | Speed +30, size -50, health -30 | Speed up | Speed +30, size -50, health -50. Palette 2 | None | Poison (carnivores only) | Health differs |
| Spider | Nocturnal +75 | Day and night shift | Nocturnal -25. Palette 1 | None | No | Nocturnal has the wrong sign |
| Cave Spider | Size -50 | Size down | Size -50. Palette 1 | None | Poison (carnivores only) | Matches |
| Armadillo | Immunity +15, defense +55, gestation +40, vision -35 (G9) | Resilience, defense, gestation up. Vision, speed down | No entry | None | No | No entry. Speed only in the older doc |
| Sniffer | Energy -25, speed -30, regen +43 | Lower metabolism for herbivores. Speed down, healing up | No entry | Torchflower seeds in the herbivore diet give +25 % regen. Nest eggs on moss hatch 15 % slower (G22) | No | No entry |
| Frog | Jump +43 (large species excluded), egg clutch +25, low-light vision +30, underwater +15. Movement vision +55 is out of Beta | Same directions. Temperate, warm and cold variants | No entry | Temperate, warm and cold variants (G19) | Gender swapping, thermal detection | No entry |
| Camel | Thirst -30, stomach -25, energy +35, health +25, scent (water) +42 | Not listed | No entry, although the camel exists in 1.20.1 | None | Hyper intelligence | No entry |
| Nautilus | Underwater +35, regen +25, speed -5, scent +10 | Not listed (named only as park fauna) | No entry | None | No | No entry |
| Villager | Every gene random on each use (lead 11.6) | Not listed | Commented out | None | No | No entry |
| Allay | Not listed | Not listed | Empty entry | None | No | No usable DNA (lead 2026-10-10). Delete the entry |

**Special-gene donors.** The special-gene column follows `## Special Genes.txt` and plan B10. The older doc names fewer donors (thermal detection from a tree frog only, hyper intelligence as a "Velociraptor combination"). Its non-vanilla donors (cuttlefish, common reed frog, red-eyed tree frog, pit viper) are not in Beta. A command or creative item stands in until they exist, and the vanilla frog gives gender swapping (lead 11.6).

**Large species.** The table excludes "all large dinosaurs" from the horse and frog jump effects. In Beta that is Tyrannosaurus, Triceratops and Brachiosaurus.

**Mobs in 26.2 with no table entry** ([Minecraft Wiki, Mob](https://minecraft.wiki/w/Mob), read 2026-10-10):

| Group | Mobs | Rule |
| --- | --- | --- |
| Animals with no values | Allay, Strider, Tadpole, Happy Ghast, Sulfur Cube (26.2) | No usable DNA (lead, Q25, 2026-10-10) |
| Copies of a donor | Trader Llama | Default: uses the llama row |
| Undead mounts | Skeleton Horse, Zombie Horse, Camel Husk, Zombie Nautilus | Default: no usable DNA |
| Golems and traders | Iron Golem, Snow Golem, Copper Golem, Wandering Trader | Default: no usable DNA |
| Hostile mobs | About 40 entries commented out in `GeneDataProvider` | Default: no usable DNA in Beta |

## Behaviour scope per species

**In game:** each species has a role the player can learn in one visit. Genes move the numbers, not the role.

| Species | Role (lead and D13) | Beta acts beyond the shared set | Clips that exist for them `[v]` | Gene levers that show most | Paddock it needs |
| --- | --- | --- | --- | --- | --- |
| Tyrannosaurus | Carnivore, 1-2. Stands. Territory. Warning roar. Prey by clade | Solo and pair hunt, scavenge, territory patrol, guard nest, break an unpowered fence (F29) | `roar`, `speak`, `nesting`, 8 idles | Strength, size, aggression | Power on at all times: it breaks unpowered fences. A fence tall enough for its height. Room for a 40-block hunt roam |
| Velociraptor | Pack hunter, 3-6. Shares a target and flanks | Pack hunt with flank slots | `pounce`, `sleep`, `sneeze`, 7 idles | Pack size, speed, special gene | Power on at all times: any animal climbs an unpowered wire (F26) |
| Dilophosaurus | Carnivore, 1-3. Frill warning | Warning and poison spit share the `spit` clip, the only clip that opens the frill | `spit`, `call2`, `sleep`, 6 idles | Aggression, special gene | Keepers stay out of spit range |
| Triceratops | Herd 4-6. Stands and charges. Low grazer | Charge with wind-up, low grazing, guard young | `ram`, `ramwindup`, `eat1`, 5 idles | Defense, herd size, strength | Low plants or a low herbivore feeder |
| Gallimimus | Herd 6-12. Flees and runs | Flee as a herd, wedge travel, sentry | `calling`, `sentry`, `startled`, 4 idles. No `eat`, `drink` or `roar` | Herd size, speed, nocturnal | Large open area for journeys (Q1) |
| Brachiosaurus | Herd 3-5. Flees (D13 default). High browser. Open ground | High browsing, breaks leaves, stays out of forests | `eatingtall2`, `tailwhip`, `Stomp`, 7 idles | Size, stomach, herd size | Tall trees or a high feeder. Open ground |

Under fence parity a live fence holds every species. Power uptime and fence height are the paddock levers, not wire strength.

**Asset notes.** Brachiosaurus has defence clips (`tailwhip`, `Stomp`, `Neck Attack2`). They support "stands" as well as the D13 default "flees". Velociraptor `pounce` exists, so a leap needs no new clip. The plan defers the crouch, not the leap. The Gallimimus has no `eat` or `drink`, so its eating and drinking show the idle clip until B24. Only the T. rex has sounds, so other species' warnings and calls stay silent until B24 imports them.

## Brain layout

**In game:** a dinosaur flees or fights at once. It eats and drinks when a need is urgent and sleeps in its window. It travels with its herd and fills the rest of the day with idle acts.

This is the comparison report's layout, adopted as the B17 target. The order is SmartBrainLib 2 activity priority, where 1 wins. CORE runs beside the active one.

| Order | Activity | Starts when | Owner | Genes that act on it |
| --- | --- | --- | --- | --- |
| Always | CORE | Always: look, move, follow parent, join group, needs and stress step every 20 ticks | B16, B17, B26 | herd and pack size |
| 1 | AVOID | `AVOID_TARGET` or panic (flee species, babies) | B19 | speed, energy |
| 2 | FIGHT | `ATTACK_TARGET`. Warning, hit, recovery. Ends at 0 stamina | B19, B27 | strength, aggression, energy |
| 3 | HUNT | Hungry adult carnivore, no hunt cooldown | B17, B18 | scent, intelligence, pack size |
| 4 | FEED | Hungry or thirsty, cooldown absent, herd allows | B17, B20 | stomach, eat rate, thirst, scent |
| 5 | REST | Tired, or in the species window. Drowsy and rouse rolls stagger the herd | B17 | energy, nocturnal |
| 6 | HERD_TRAVEL | Group state TRAVEL | B26 | energy |
| 7 | BREED | In love, nest in range, both calm, fertility roll passed | B18 | fertility, egg clutch, gestation |
| 8 | SOCIAL | Rank contest or a pending call | B18 | aggression, size |
| 9 | IDLE | Nothing above applies. Weighted pool of short acts | B17 | none |

Guard young writes `ATTACK_TARGET`, so it runs in FIGHT. Taming commands are start conditions in CORE and IDLE. The T. rex fence check (F29) is a FIGHT or IDLE behaviour with its own 50 % roll. The herd scope rule holds: AVOID, FIGHT, HUNT and FEED join herd play only after the B26 playtests pass on the Gallimimus.

## Pack hunting

**In game:** a Velociraptor pack picks one prey, spreads around it and strikes together. A lone raptor leaves a Triceratops alone, but a full pack takes a young one that strays from its herd. After the kill, the leader eats first and the others wait their turn. A tired pack gives up. A player who leaves the territory during the warning walks away.

This section extends plan B18 (shared target, spec S9, flank slots). Each row is a default that ships as config. The herd scope rule holds: pack hunting starts only after the B26 playtests pass.

**Code today** `[v]`: `CarnivoreDinosaur.canHuntWithPack` exists and nothing calls it. `DinoNeedsUtils.getThreatScore` has a to-do to multiply threat by group size. A carnivore skips prey whose threat is higher than its own (x10 when starving).

| # | Dynamic | Default | Module |
| --- | --- | --- | --- |
| P1 | Which species hunt as a group | Velociraptor: full pack with flank slots (lead). Tyrannosaurus pair: the second animal takes the leader's target and comes in from the far side (slot 180°), with no wait for slots. Dilophosaurus: hunts alone. Group mates join only by retaliation (Q15) | B18 |
| P2 | Prey choice with more hunters | The prey test compares the target's threat with the pack threat. Pack threat is the sum of adult members within follow range of the leader, capped at 4x the leader's own. Clade preference still ranks the candidates. Young prey have smaller stats, so packs pick juveniles first with no extra rule (Q19) | B17, B18 |
| P3 | When the hunt ends | A member at 0 stamina leaves the hunt, gets its hunt cooldown and rests. The pack hunt ends when the leader's chase ends, the target is lost, or fewer than half the starting members still chase. Flight when small or leaderless stays deferred (B18.9) (Q16) | B17, B18 |
| P4 | Who eats first | Dominance order at the carcass. The leader eats first. At most 3 eat at once. Others wait within 6 blocks with idle acts. A member that finds no meat left stays hungry, so an oversized pack shows hunger and stress (Q18) | B16, B18 |
| P5 | Pack against a herd | When the target's herd bolts (herd alarm, RC35), the leader may switch target once per hunt. The new target is a baby or a member outside its herd area, within 8 blocks of the old target. Standing species defend: adults that guard young attack the nearest hunter (B18 guard rule) | B18, B19 |
| P6 | Pack movement between hunts | Packs use the herd layer for joining, leader and spacing. They make no 156-314 block journeys. The leader roams inside `territory_radius` and the pack follows in the travel wedge (Q17) | B19, B26 |
| P7 | Hunting the player | A player is prey (lead). Flank slots apply. Only the leader plays the warning, and members hold their slots during it. If the player leaves the territory or follow range during the warning, the whole pack drops the target. The pushed target lets members that did not detect the player join | B18, B19 |
| P8 | Shared cooldowns and relief | Each animal keeps its own hunt cooldown and stamina. A new pack hunt starts only when the leader's cooldown is over and at least half the members are above 0.33 stamina. Every member that bites the kill gets the hunger and the live-bait stress relief | B17, B18, B20 |
| P9 | Genes inside the pack | The pack size gene sets the pack size. Speed and energy set who keeps up. Aggression acts only through dominance: an edited aggressive member can win a rank contest and lead. A member never starts a pack hunt of its own | B07, B18 |
| P10 | Space for a flank | A slot that is not standable or not reachable drops out. The member takes the nearest free reachable slot, else follows the leader in. In a small or cluttered paddock the pack attacks in a line | B18, B26 |

**Assets.** Velociraptor has `attack1`, `pounce` (Q14) and `eat1`. It has no `call` clip, so pack gather and hunt calls play no clip until B24.

## Hunting and avoidance

**In game:** a raptor creeps toward a grazing herd and sprints only for the last few blocks. A Dilophosaurus waits still by the water until prey comes close. Animals keep away from the spot where they were hurt. A starving T. rex may turn on its own kind, but never on its own group. In a mixed paddock, one startled Gallimimus sets the Triceratops on edge too.

This section adds five behaviours that no source defines. Each row is a default that ships as config. The hunting rows wait for the B26 playtests, like pack hunting.

**Code today** `[v]`: `CarnivoreDinosaur.canTarget` refuses its own species in all cases, although its comment says "unless starving". `JoinGroup` forms groups of one species only. `StartleWatch` reacts at 6 blocks.

| # | Behaviour | Default | Assets | Module |
| --- | --- | --- | --- | --- |
| H1 | Stalking | A hungry carnivore with a target beyond 8 blocks walks at speed 0.5. While it stalks, a flee species counts it as a threat only within 12 blocks, not the full follow range. At 8 blocks it sprints, or a Velociraptor pounces (Q14). Prey startle at 6 blocks, so the sprint starts first (Q20) | Walk clip at low speed. The T. rex `stalk` clip in the asset repository needs an export | B18, B19, B24 |
| H2 | Ambushing | A hungry Dilophosaurus or Velociraptor with no prey in sight picks a wait spot within 4 blocks of water, a feeder or a scent trail. It stands still for 600-1,200 ticks. Prey within 6 blocks starts the attack. Camouflage (B10) hides the waiting animal from players beyond its range (Q21) | Idle and attack clips | B10, B18 |
| H3 | Danger areas | Vanilla path costs, fence nodes (F27), the deep-water cost and cliff edges stay. Added: the spot where the animal took damage, or where a group mate died, is a soft path cost within 6 blocks for 2,400 ticks. Babies of standing species back away from a larger predator within 8 blocks (comparison C16, babies only) (Q22) | None | B17, B19, B26 |
| H4 | Cannibalism | Never its own group or family. While starving, its own species is valid prey at the card weight (T. rex: 5 %, from "no-go 95 %"). A same-species carcass is food only while starving (Q23) | None | B17 |
| H5 | Mixed-species paddocks | No mixed groups in Beta. A herd alarm reaches herbivores of other species within 8 blocks: flee species bolt, standing species face the threat. Graze spots keep clear of other species' spots (Q24) | None | B18, B26 |

## Paddock and care

**In game:** the paddock is the player's answer to the genome. Each row is one thing the player must supply, the genes that change how much, and what happens when it is missing.

| Player supplies | Animal side | Genes that change the demand | When it is missing |
| --- | --- | --- | --- |
| Water the head can reach | Thirst, FEED, head-contact rule | thirst, scent, size (reach) | Thirst falls, stress +2 per step, death later |
| Food: feeder points, graze blocks in the reach band, live bait | Hunger, FEED, HUNT, grazing. A live-bait kill lowers carnivore stress | stomach, eat rate, size, diet genes | Stress +2 per step, aggression, starvation 1 HP per 80 ticks |
| Company | Group size min and max | herd size, pack size | "Alone": stress +1 per step |
| A fence that stays powered | Live wire: 1 HP and knockback (F25). Unpowered wire: any animal climbs it (F26), a T. rex breaks it (F29) | special gene (hyper intelligence), jump, size (height) | Animals climb out or a T. rex breaks through |
| Fence height | Height stops tall and jumping animals (11.11) | size, jump | The animal steps or jumps over |
| Open ground and space | Herd area (radius x sqrt 6) and journeys. Brachiosaurus stays out of forests | herd size, energy | Journeys end early. Not a stress input in Beta |
| Nest site | Floor block from a tag, in range of the pair | fertility, egg clutch | No breeding |
| A calm park | Stress under the threshold | aggression, risk behaviour from G1 | No breeding, no taming, aggression |

**Neglect has a cost the player can read.** The debug stick shows the urgent need and the activity (B16). Section 11 gives survival players no readout in Beta (D18). So the visible signs carry it: stress bands change the idle weights (fewer sits, more calls), and a stressed animal turns aggressive in view.

## Build order

The order keeps the lead's herds-first rule and the plan tiers. Nothing after step 0 starts before P2.

| Step | Name | Modules | What the player gets | Gate |
| --- | --- | --- | --- | --- |
| 0 | Herd finish (now, 1.20.1) | Herd work | P12c built and reviewed. Playtest 6 rulings | Lead commits the herd work (D37) |
| 1 | Port | P2 | Same behaviour on 26.2. SBL 2 ticks the brain once | P2 acceptance |
| 2 | Real animals | B00, B16 | Species data, smooth growth, carcasses, hatchlings as babies | Tier 1 playtest |
| 3 | Visible genome | B07, B11, B09 bake | Genes reach the animal. Variation at hatch. Colours saved | Tier 2 playtest |
| 4 | Herds in paddocks | B26, B15 | Herd travel, fences at 1.12.2 parity, journeys inside fences | Herd playtests pass |
| 5 | Needs and the day | B17, B20 | Nine activities, idle pool, sleep stagger, stress, feeders, grazing | 20 animals under 50 ms |
| 6 | Social and danger | B18, B19, B27 | Leaders, packs, nests, warnings, flee, taming, detection, section hits | Tier 4 playtest |
| 7 | Depth | B08, B10, B24 | Full Edit screen, special genes, missing clips and sounds | Beta hardening (P4) |

## Plan changes

The lead can paste each row into the named module's scope. "Source" names where the item comes from.

| Id | Module | Change | In game | Source |
| --- | --- | --- | --- | --- |
| RC1 | B00 | Add `activity_window` (diurnal, nocturnal, cathemeral). Interim: three carnivores nocturnal, three herbivores diurnal | Each species sleeps in its own part of the day | X8, A1 |
| RC2 | B00 | Add `follow_range`, `flee_list`, `reach_band`, `idle_weights`, `view_cone_degrees` | Species differ in how far they notice, what they flee and what they reach | Comparison |
| RC3 | B00 | Move dominance weights (age 0.5, size 0.3, aggression 0.2) into species data | Pack makers can change who leads | Comparison, `Dominance.java` |
| RC4 | B00 | Add `intelligence_rating` from the design byte values | Smart species notice keepers sooner | G3 |
| RC5 | B00 | Add `default_special_gene` (empty unless Q4 answers) | Raptors are smart from hatch | X14 |
| RC6 | B00 | Add a `risk_behaviours` list for edit risk | Risky genomes hatch odd animals | G1, X20 |
| RC7 | B07 | Use one gene scale: percent deltas from the DNA Effects table. Fix turtle, rabbit, spider, cat and chicken entries | A turtle donor makes a tougher animal, not a weaker one | X7 |
| RC8 | B07 | Donor effect = table value x min(1, share / 0.5) | Filler choice changes an unedited clone in a predictable way | G14 |
| RC9 | B07 | Add gene `scent`. Map the table's Hunger and Thirst rows per G17 | Some animals find food and water from farther | G8, G17 |
| RC10 | B07 | Trade-off pairs that grow with donor share and edit count | Pushing one stat costs another | G2 |
| RC11 | B07 | `GenomeRisk` abnormal chance picks a risk behaviour at hatch | Risk is real and visible | G1 |
| RC12 | B07 | Server checks: clone 50, edit 70, gender 80, donor slots, ranges, special-gene slot | The server rejects hacked or buggy edits | Lead 11.6, issue #49 |
| RC13 | B07 | Map every gene to its reader per the gene effect map, with a GameTest per gene group | Each edit shows in the animal | Lead 11.6 |
| RC14 | B07 | Fix fossil DNA yield of 0 % (commented lookup in `DNAData.java:46-52`) | Fossils give DNA | Code `[v]` |
| RC15 | B08 | Effect text shows the paddock effect of big edits ("Size +40 %: needs the high fence") | The player sees why the paddock must change | Loop |
| RC16 | B08 | Risk panel names the risk-behaviour chance, not the result | The screen shows the risk and keeps the roll hidden | Lead 2026-10-06 |
| RC17 | B09 | REST plays `sleep` where it exists. Stress band shifts idle clip weights | Sleeping raptors look asleep. Stressed animals look restless | G16 |
| RC18 | B10 | Extend the camouflage data list to thermal detection, with Beta defaults (Q3) | Camouflage and thermal fit Beta species | X13 |
| RC19 | B10 | Dilophosaurus warning and poison spit use the existing `spit` clip | The frill opens before a spit | Asset check |
| RC20 | B11 | Separate lab hatch rate from nest hatch rate (Q2) | A cloned egg usually hatches | X12 |
| RC21 | B11 | Risk-behaviour roll at hatch, beside the variation roll | One hatch moment reveals the animal | G1 |
| RC22 | B15 | State in B15 that F27 and F29 replace `allNeedsAtZero` and the starving-carnivore fence break | Only a T. rex breaks fences, and only unpowered ones | X15, X19 |
| RC23 | B10, B15 | A hyper-intelligent animal that notices an unpowered section walks to it and climbs out (Q7) | Smart animals find the gap the player left | Lead 11.11, F26 |
| RC24 | B24 | Species page states the fence height the body and jump need, and whether it breaks fences | Players pick the right fence | G15 |
| RC25 | B16 | Jump cap below low-security height. Heat resistance cuts fire damage | Genes change movement and hazards | G5, G6 |
| RC26 | B16 | Lab hatchling follows nearest adult, else stays near the hatch point | Babies stay near their kind | G12 |
| RC27 | B16 | Fix the dehydration counter and the per-load need drain | Animals that drink do not die | Code `[v]` |
| RC28 | B17 | Adopt the CORE-plus-nine activity layout | Clean switches between acts | X10 |
| RC29 | B17 | Weighted idle pool with existing clips, look at a still player within 8 blocks (herbivores) | Animals look busy and notice visitors | Comparison C10, C11, A3, A4 |
| RC30 | B17 | Drowsy 1/30 and rouse 1/240 per second inside REST | Herds settle and wake one by one | Comparison C12 |
| RC31 | B17 | Cosmetic graze at the herd spot for species with an eat clip, no block eaten | Herbivores look busy all day | Comparison C14, A5 |
| RC32 | B17 | Social need = company input of stress | One meter, one rule | G11 |
| RC33 | B17 | Gene readers for thirst, energy, stomach, eat rate, scent, nocturnal | Needs follow the genome | Gene map |
| RC34 | B17 | Graze move start and stop band 8 and 4 blocks | Grazers do not walk back on each small gap | Comparison C3 |
| RC35 | B18 | Startle on a flee species writes the herd alarm | One startled animal moves the herd | Comparison C18 |
| RC36 | B18 | Fertility, clutch and gestation genes feed the breeding roll | Bred lines differ in output | Gene map |
| RC37 | B18 | Default: heredity carries risk behaviours and default special genes like the lead's one-parent choice genes | Odd traits can run in a line | G1 (not in the lead's list) |
| RC38 | B19, B24 | The warning uses each species' call clip (`calling`, `call`, `call2`) until B24 adds `roar` clips and sounds | A Gallimimus does not warn like a T. rex | Code `[v]`, asset check |
| RC39 | B19 | Triceratops charge uses `ramwindup` then `ram` | A visible wind-up before the charge | Asset check |
| RC40 | B19 | Tamability gene scales the reputation threshold | Wolf DNA makes taming easier | G4 |
| RC41 | B19 | Vision factor from the vision genes (G9) | Some animals see well at night | G9 |
| RC42 | B24 | Gallimimus `eat` and `drink` first in the art queue | Unblocks grazing, drinking clips and neck reach | Comparison, 11.14 |
| RC43 | B26 | Journey targets inside a fenced reachable area (Q1) | Herds travel inside paddocks without stalls | X11 |
| RC44 | B26 | Gallimimus `group_size` returns to 6-12 when herd tests end. Restore the `SetHunting` group guard until the B26 playtests pass | Herd size matches the species. Herds stay calm until combat joins | X9, X16 |
| RC45 | B18 | Tyrannosaurus pair hunt (far-side slot 180°). Dilophosaurus hunts alone | A T. rex pair closes from two sides | P1 |
| RC46 | B17 | Pack threat in the prey test. Fill the `getThreatScore` group to-do and call `canHuntWithPack` | A full pack takes prey a lone raptor avoids | P2 |
| RC47 | B18 | Hunt end: a member drops out at 0 stamina. The pack stops when the leader stops, the target is lost, or under half still chase | A player can outlast a pack | P3 |
| RC48 | B16, B18 | Dominance feeding order at the carcass, at most 3 eaters | The leader eats first and the rest wait | P4 |
| RC49 | B18 | One target switch per hunt to a baby or straggler after a herd alarm | The pack cuts out the weak animal | P5 |
| RC50 | B26 | Carnivore groups make no journeys. The leader roams inside `territory_radius` | Packs patrol, they do not migrate into fences | P6 |
| RC51 | B19 | Only the leader warns. The pack drops a player who leaves the territory during the warning | One warning, one chance to back away | P7 |
| RC52 | B17 | Pack hunt start gate: leader cooldown over, half the members above 0.33 stamina. Relief for each member that bites | Packs rest between hunts. Every feeder calms down | P8 |
| RC53 | B18 | Members never start pack hunts. Leadership changes only through contests | An aggressive edit shows as a new leader, not chaos | P9 |
| RC54 | B18 | Slot fallback when a block or a mate fills a flank slot | Packs still attack in tight paddocks | P10 |
| RC55 | B18 | Stalk approach: walk at 0.5 to 8 blocks, then sprint or pounce. Flee range against a stalker is 12 blocks | Raptors creep up on a herd | H1 |
| RC56 | B24 | Export the T. rex `stalk` clip from the asset repository | A stalking T. rex looks low and slow | H1 |
| RC57 | B18 | Ambush wait for Dilophosaurus and Velociraptor near water, feeders or scent trails | Predators lie in wait at the water hole | H2 |
| RC58 | B17 | Danger memory: a damage spot or a mate's death spot is a soft path cost for 2,400 ticks | Animals keep away from where it hurt | H3 |
| RC59 | B19 | Babies of standing species back away from larger predators | Young Triceratops stay behind the adults | H3 |
| RC60 | B17 | Own species is prey only while starving, at card weight. Never own group or family | Cannibalism is rare and a sign of neglect | H4 |
| RC61 | B18 | Herd alarm reaches other herbivore species within 8 blocks. Graze spots avoid other species | A mixed paddock reacts as one | H5 |
| RC62 | B07 | Regenerate the donor data from the donor catalogue: 15 values, 16 missing effects, 4 empty entries, 6 new entries. Delete the allay entry | Every donor gives the effects the design lists | Donor catalogue |
| RC63 | B07 | Variants share the base animal's genes and keep their own palette. Add the wolf variant palettes | A Snowy wolf donor lightens a raptor, a Black one darkens it | G19 |
| RC64 | B04 | The syringe gives a tube with no usable DNA for mobs without a donor row, with a tooltip line | The player learns which animals are donors | G18 |
| RC65 | B07 | Large-species exclusion for the horse and frog jump effects (Tyrannosaurus, Triceratops, Brachiosaurus) | A big animal never gains jump from horse or frog DNA | Donor catalogue |
| RC66 | B10, B18 | Sniffer torchflower regen and moss hatching rule | Sniffer DNA ties a herbivore to a crop | G22 |

## Questions for the lead

The lead answered questions Q1 to Q29. Decisions register rows D40 to D68 in the plan hold the answers.

## Sources

- [codebase-analysis.md](codebase-analysis.md) sections 4.3, 4.6, 6, 7.3 to 7.9, 8.5, 11 and 12.
- [project-plan.md](project-plan.md) global constraints, B00 to B27 (B15 fence parity F1-F32), R01 to R03, backlog and register D01 to D39.
- [reference-recommendations-review.md](reference-recommendations-review.md).
- `reports/Entity AI and behaviour comparison.md`, `reports/Reference mod survey for Beta.md`, `reports/Optimization and tech mod survey for Beta.md`.
- [dinosaur-hitboxes-and-movement-design.md](dinosaur-hitboxes-and-movement-design.md) and the herd spec `docs/superpowers/specs/2026-10-08-herd-movement-redesign.md`.
- `PN Feature Docs`: `Project_ Nublar (2).docx`, `Mod Commission.docx`, `_Cloning Process.docx`, `Tyrannosaurus.docx`, `Velociraptor.docx`, `Vanilla Animal DNA Effects.txt`, `## Special Genes.txt`, `Primary Sources of DNA.txt`.
- Code on `kash's-slop`, read on 2026-10-10. Java: `Dinosaur.java`, `DinoNeedsUtils.java`, `DinoData.java`, `GeneInit.java`, `GeneDataProvider.java`, `SequencerBlockEntity.java`, `IncubatedEggItem.java`, `ThreatDisplay.java`, `entity/ai/**`, `entity/dinosaur/animation/**`. Data: `sounds.json`, the six animation files and the six species configs.
