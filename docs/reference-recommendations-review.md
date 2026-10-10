# Reference recommendations: viability review

Date: 2026-10-06. Status: analysis for the project lead. It changes no code and no plan module.

This file rates the 32 recommendations in the "Recommendations for Project Nublar" section of the shared page's tab "Reference game AI & Genetic Design Proposals". The research is from 2026-10-04. The lead's Beta decisions came one day later (section 11 of [codebase-analysis.md](codebase-analysis.md)). So this review checks each recommendation against those decisions, the code, and [project-plan.md](project-plan.md).

## Result

- 12 recommendations were viable and feasible as written. Section 11 or a plan module already asks for 8 of them.
- 11 were viable after a change of form, so that they agree with section 11.
- 2 needed a decision from the lead. On 2026-10-06 the lead chose the midpoint heredity rule and an egg tooltip without rolled traits. The review now drops GEN-1 and adopts GEN-4.
- 4 wait for art, a later species or a later release.
- 3 conflict with a decision of the lead. Drop them for Beta.
- After the answers, the verdicts are 13 adopt, 11 adapt, 4 defer and 4 drop.

The research mostly agrees with the plan. Its new value is a set of small additions to B09, B15, B16, B17 and B18 (see "New items for the plan").

Of the research's own "start with these five", two stand as written (edit risk, hitbox scaling). Comfort and idle layers need a change of form. Alleles need a decision.

A check of the code found five defects that neither the plan nor the analysis listed. Four of them block shortlist items. On 2026-10-06 the lead put each fix into its plan module. Each fix is small (see "Code facts that change feasibility").

## Shortlist

The ten most viable and feasible items, in recommended order. Viable means that the item agrees with section 11. Feasible means low effort, no missing art and a clear owner module.

| Rank | Item | Module | Effort | Needs first | Why it ranks here |
| --- | --- | --- | --- | --- | --- |
| 1 | GEN-3 Edit risk and server limits | B07, B08 | M | Nothing | Section 11 asks for edit risk before synthesis. Server limits close issue #49 (a size of -100%) |
| 2 | NAV-1 Hitbox from growth and the size gene | B16 | M | A path check that uses body size | Section 11 asks for it. It addresses issue #61 (tiny hitboxes drown and pass small gaps) and issue #63 (culling) |
| 3 | GEN-8 Smooth growth with one maturity value | B16 | M | A hatched animal spawns as a baby | Section 11 asks for smooth growth. NAV-1 reads the same value |
| 4 | AI-4 Clear a need after a meal, then a cooldown | B17 | S | Nothing | `IS_HUNGRY` never clears today. The cooldown also blocks the eat-drink-sleep loop that Planet Zoo had |
| 5 | GEN-6 Variation around the genome | B07, B18 | S | Bred young get a genome copy | Section 11 asks for variation in each clone. The same rule gives bred young a variation around the parents |
| 6 | AI-2 Stress threshold sends an animal to the fence | B17 | M | A starving window before death | Section 11: a starved or stressed animal attacks a live fence. Today that attack almost never occurs |
| 7 | ANI-1 and ANI-6 Gaits from speed, clip speed from body scale | B09 | S | Rename the Triceratops `Sprint ` clip | Five species have a `run` clip. Smooth growth makes foot slide visible. GeckoLib has a speed handler for each controller |
| 8 | AI-8 Herd leader and cohesion | B18 | M | Unique memory ids | Section 11 asks for groups with a leader. The memory types exist, but no code writes them |
| 9 | AI-1 Two status values on the debug stick | B16 | S | Nothing | It replaces the debug stick's broadcast to all players. Items 4, 6 and 8 are faster to test with it |
| 10 | ANI-3 One-shot clips from the behaviour | B09 | M | Nothing | B09 already moves flinch lengths to the animation file. GeckoLib triggerable animations do the rest |

## Fit against effort

The shared page shows this grid as a drawing. Nine of the eleven shortlist items sit at fit 3 with small or medium effort. The other two (debug status, clip speed) are small items where section 11 is silent. The four items at fit 0 conflict with a decision or wait for a later release. The grid shows the ratings before the lead's answers of 2026-10-06. After them, GEN-1 is at fit 0 and GEN-4 is at fit 3.

| Fit | S | M | L |
| --- | --- | --- | --- |
| 3: section 11 or the plan asks for it | AI-4, GEN-5, GEN-6, NAV-5, ANI-1 | AI-2, AI-8, AI-9, GEN-3, GEN-8, NAV-1, ANI-3 | |
| 2: section 11 is silent, small effect | AI-1, AI-6, GEN-4, GEN-7, NAV-4, NAV-6, ANI-4, ANI-6, ANI-7, ANI-8 | AI-3, AI-5, AI-7, NAV-2, ANI-2 | |
| 1: needs a decision | | | GEN-1 |
| 0: conflicts or not in Beta | | NAV-3 | GEN-2, NAV-7, ANI-5 |

## Code facts that change feasibility

Five defects block or weaken items. The plan and the analysis did not list them. On 2026-10-06 the lead put each fix into the module in the Module column. The check read `1.20.1-ai` at 4244f62 and `rft/adversing`. The refactor branch changes none of these facts. Paths are under `common/src/main/java/net/dumbcode/projectnublar/`.

| Finding | Evidence | Blocks | Fix size | Module |
| --- | --- | --- | --- | --- |
| A hatched dinosaur spawns as an adult. Growth never starts | `item/IncubatedEggItem.java:39-44` spawns the entity and never calls `setBaby` | GEN-8, NAV-1, ANI-8 | Tiny | B11, B16 |
| The fence path check ignores body size, so a large animal can path through a gap one block wide | `entity/ai/FenceAwareNodeEvaluator.java:16` calls the one-block `getBlockPathType` and skips the vanilla size check | NAV-1 | Small | B16 |
| A starving dinosaur dies on the next hunger tick. Fence attacks need "starving", so they almost never occur | `util/DinoNeedsUtils.java:160-169` kills the animal at hunger 0 | AI-2, and the B17 acceptance test "a starving T. rex attacks a live fence" | Tiny | B17 |
| Bred young share the mother's genome object. A change to the young's gender changes the mother | `entity/dinosaur/Dinosaur.java:874-877` passes `mother.getDinoData()` without a copy | GEN-6 for bred young | Tiny | B18 |
| The run flag never clears after the first chase. Stamina then drains at twice the rate until the chunk or the world loads again | `entity/dinosaur/Dinosaur.java:580` | AI-7 | Tiny | B17 |

Most code claims of the research are true:

- `IS_HUNGRY` never clears.
- The second animation controller always returns `STOP`.
- No code reads `hunt_score_config.json`. The file also has a typo (`minecraft:raveger`) and no player entry.
- `HAS_GROUP` and `IS_GROUP_LEADER` share the id `is_pack_leader`.
- `UpdateEditInfoPacket` checks only the block entity type. A size of -100 gives a render scale of 0 (issue #49).
- `RUN_ANIM`, the turn clips and the Triceratops `injured` clip have no caller.
- `init/EntityInit.java:30-39` sets fixed hitboxes.
- The code has no stress value, no nest block and no colour morph.

Three claims were partly wrong:

- The brain has three groups (core, idle, fight), not one list. The idle group holds one `FirstApplicableBehaviour` with eight children.
- In practice only T. rex attacks fences. The other five species target only the last animal that hit them.
- The hard-coded flinch lengths do not agree with the clips: T. rex has 34 ticks against a clip of about 28, and Dilophosaurus has 39 against about 18.

The first version of this review listed a sixth defect: memories that are not in the brain's list. That defect does not exist. SmartBrainLib stores every memory that code writes, on its 1.20 and its 26.2 versions.

## How the review rates an item

Fit with the Beta design:

| Fit | Meaning |
| --- | --- |
| 3 | Section 11 asks for it, or a plan module already has it in scope |
| 2 | Section 11 is silent. The item supports a decided feature and has a small gameplay effect |
| 1 | The item needs a lead decision with a real gameplay effect |
| 0 | The item conflicts with a lead decision, or targets content outside Beta |

For an Adapt item, fit and effort apply to the changed form. Effort is the code in addition to the module's existing scope. S is under about 100 lines. M is one or two classes with tests. L is a new subsystem.

Verdicts:

- **Adopt:** build it as the research says, or nearly.
- **Adapt:** keep the idea and change the form to agree with section 11.
- **Ask:** the lead decides first.
- **Defer:** it fits, but it waits for art, a species or a release.
- **Drop:** not for Beta.

## All 32 recommendations

Ids follow the order of the four tables in the research tab: AI, genetics (GEN), pathfinding (NAV) and animation (ANI).

| Id | Recommendation | Verdict | Fit | Effort | Module | Reason |
| --- | --- | --- | --- | --- | --- | --- |
| AI-1 | Two status values (urgent need, activity) on the debug stick | Adopt | 2 | S | B16 | Replaces the stick's broadcast to all players. Every AI module is faster to test with it |
| AI-2 | Comfort value with a species threshold | Adapt | 3 | M | B17 | Section 11 calls it stress. Its inputs are food and water and company. Stamina is not an input |
| AI-3 | Four action tiers | Adapt | 2 | M | B17 | The brain already has core, idle and fight groups. Add SmartBrainLib activities with priorities when B17 adds the needs. No separate tier framework |
| AI-4 | A pause after a need is met | Adopt | 3 | S | B17 | Fixes `IS_HUNGRY`, which never clears. The cooldown blocks need loops |
| AI-5 | Dominance separate from combat | Adapt | 2 | M | B18, B19 | Rank contests use a dominance value and `ThreatDisplay`. Not `StartTurfWar`, because turf wars are out of Beta |
| AI-6 | Reaction profiles as data | Adapt | 2 | S | B16, B19 | Put only the fields that section 11 names in species data: prey by clade, flee or stand, territory radius. No rule engine |
| AI-7 | Hunts with stamina, a target score and a cooldown | Adapt | 2 | M | B17 | End a chase at zero stamina, then a cooldown. Clear the run flag first. Score prey from species data, because B17 deletes `hunt_score_config.json` |
| AI-8 | Herd leader and cohesion | Adopt | 3 | M | B18 | Section 11 asks for groups with a leader. No code writes the group memories today, and two share one id |
| AI-9 | Needs from a sampled area | Adapt | 3 | M | B17 | Space and block comfort are not Beta inputs. Keep the technique (slow sampling, a cache) for the sensor cost risk |
| GEN-1 | Two alleles for each additive gene | Drop | 0 | L | B07, B18 | On 2026-10-06 the lead chose the parents' midpoint (GEN-6). Alleles would change the genome format and the meaning of each edit slider |
| GEN-2 | Letter sequences for fertility and immunity | Drop | 0 | L | none | Inbreeding is not in the design. Disease is out of Beta. Immunity acts on poison only |
| GEN-3 | Edits change chances and cost viability | Adopt | 3 | M | B07, B08 | Section 11 asks for edit risk on the screen before synthesis. Server limits close issue #49 |
| GEN-4 | Traits visible on the egg | Adopt | 3 | S | B11 | On 2026-10-06 the lead chose edits, risk and the chosen gender, with no rolled traits. Variation rolls at hatch |
| GEN-5 | Recessive colour morphs at about 1% | Adapt | 3 | S | B07, B09 | Section 11 sets about 5% for each clone. A recessive rule needs alleles (GEN-1) |
| GEN-6 | Variation around the parent average | Adopt | 3 | S | B07, B18 | Section 11 asks for clone variation. For bred young, the parents' midpoint plus the same variation is the smallest heredity rule. The lead chose it on 2026-10-06 |
| GEN-7 | Breeding gates | Adapt | 2 | S | B18 | Gates: a nest, the fertility gene, low stress. Space is not a Beta input. A stress limit replaces the 66% welfare of Planet Zoo |
| GEN-8 | Continuous growth with a cap | Adopt | 3 | M | B16 | Section 11 asks for smooth growth. Size dimorphism comes from the gender size weights of the T. rex card, not from an earlier stop |
| NAV-1 | A body size that agrees with the model | Adopt | 3 | M | B16 | Section 11 asks for it. It addresses issues #61 and #63 |
| NAV-2 | A view of the reachable area | Defer | 2 | M | B15, B17 | A debug tool. Build it only if fence tests show escapes that nobody can explain |
| NAV-3 | Fence rating against animal rating | Drop | 0 | M | none | The lead did not select a fence tier against animal size. Wire health against attack damage replaces it |
| NAV-4 | Hits to break, with a chance to stop | Adapt | 2 | S | B15 | Wire health gives the hits. Keep a chance that a live wire stops a calm animal. A starved or stressed animal does not stop |
| NAV-5 | Escape when a route exists | Adapt | 3 | S | B10, B15 | Section 11: intelligent animals notice an unpowered fence. Use that memory. No route search to targets outside the fence |
| NAV-6 | Water rules by species and age | Adopt | 2 | S | B16 | A path cost for water deeper than the body height. It ties to the underwater capacity gene. Issue #61 reports drowning |
| NAV-7 | Altitude bands and depth bands | Defer | 0 | L | R02d | Pteranodon has no release. Mosasaurus is in Release 1 (R02d) |
| ANI-1 | Discrete gaits | Adopt | 3 | S | B09 | B09 already wires `RUN_ANIM`. Five species have a `run` clip. Triceratops has `Sprint ` with a trailing space |
| ANI-2 | Additive idle layers | Adapt | 2 | M | B09 | In GeckoLib 4, a later controller overrides an earlier one on a shared bone. Re-check this on GeckoLib 5. Play head-only clips on the second controller. Lead: not decided (D14) |
| ANI-3 | Animations from context, not from timers | Adopt | 3 | M | B09 | B09 already moves flinch lengths to the animation file. Behaviours trigger one-shot clips |
| ANI-4 | Turn animations | Defer | 2 | S | B24 | Only T. rex and Velociraptor have turn clips, with two different names |
| ANI-5 | Paired clips with an alignment step | Drop | 0 | L | none | Turf wars are out of Beta. `LargeCarnivoreFight` stays disconnected |
| ANI-6 | Animation speed that follows body scale | Adopt | 2 | S | B09 | Smooth growth and size variation make foot slide visible. Lead: not decided (D14) |
| ANI-7 | Mood in the pose | Defer | 2 | S | B24 | Only Triceratops has an `injured` clip. B24 asks if that clip is a flinch |
| ANI-8 | Juvenile sets only where the gait differs | Adopt | 2 | S | B16, B24 | A rule, not code. It agrees with smooth growth on one scaled rig |

The five cautions of the research all stand. Each one becomes a test or a rule:

- territory as a radius (B19)
- a navigation test after each block change near a fence (B15)
- behaviour kept separate from needs (AI-4)
- a combat load profile (B25)
- inheritance numbers as config values (B07, B18)

## New items for the plan

Seventeen additions were not in the plan. On 2026-10-06 the lead put fifteen of them into the plan: the five code-check items, the midpoint heredity rule and nine more. The two animation items (B09) are not decided, and the plan does not build them (decision D14).

| Module | Addition | From |
| --- | --- | --- |
| B00 | Species data fields: `stress_threshold`, `flee_or_stand`, `territory_radius` | AI-2, AI-6 |
| B09 | Clip speed from body scale on the walk and run controllers | ANI-6 |
| B09 | Head-only idle clips (look left, look right) on the second controller | ANI-2 |
| B11, B16 | A hatched dinosaur spawns as a baby | Code check |
| B15 | A config chance that a live wire stops a calm animal | NAV-4 |
| B16 | Urgent need and activity synced to the client and shown by the debug stick | AI-1 |
| B16 | A path cost for water deeper than the body height | NAV-6 |
| B16 | A fence path check that uses the body size | Code check |
| B17 | A cooldown after each need is met | AI-4 |
| B17 | A chase ends at zero stamina, then a hunt cooldown | AI-7 |
| B17 | Behaviours grouped into SmartBrainLib activities with priorities | AI-3 |
| B17 | A starving window before death, so that the fence attack can occur | Code check |
| B17 | The run flag clears when the chase ends | Code check |
| B18 | Bred young take the parents' midpoint plus clone variation (decided by the lead on 2026-10-06) | GEN-6 |
| B18 | Breeding gates: nest, fertility gene, stress under the threshold | GEN-7 |
| B18 | A dominance value for leader choice and rank contests | AI-5 |
| B18 | Bred young get a copy of the genome | Code check |

## Facts that changed since the research

The two main libraries move up one major version in the port, so every AI and animation item needs new code anchors.

- The research cites code at commit 4244f62 with GeckoLib 4.7.3 and SmartBrainLib 1.15. On Minecraft 26.2 the current releases are GeckoLib 5.5.x and SmartBrainLib 2.0.x (Modrinth, 2026-10-06). Every AI and animation item lands after the port, so its code anchors will move.
- The plan's tech stack line said "GeckoLib 4", and phase P2 had no task for either upgrade. On 2026-10-06 the lead put both upgrades into P2 as two task groups, and the plan now names GeckoLib 5 and SmartBrainLib 2. GeckoLib 5 moves every class that the mod imports (31 files) to a new package and deletes five of them. SmartBrainLib 2 deletes `BrainUtils` (used in 22 files) and `BrainActivityGroup`.
- The lead made these decisions after the research:
    - no fence tier against animal size
    - stress inputs of food and water and company only
    - a morph chance of about 5%
    - no turf wars and no disease in Beta
- The animation files show that `misc.idle` has no bones for Dilophosaurus and Triceratops. Those two species have no idle motion today.

## Answers from the lead (2026-10-06)

1. Heredity of bred young: the parents' midpoint plus clone variation. Colour layers, skin, special gene and morph come from one parent at random. Bred genes stay inside the edit limits.
2. Egg tooltip: the species, the donor mix, the edits, the incubation-failure chance and the chosen gender. No rolled traits, because variation rolls at hatch.
3. New items for the plan: nine of the eleven other items are in the plan. The two animation items are not decided (decision D14).

One new gap is in the plan's register as D13. Section 11 does not name flee or stand for four species. It also does not give the stress threshold or the territory radius of each species.

## Sources

- The research tab "Reference game AI & Genetic Design Proposals" on the shared page, read 2026-10-06.
- [codebase-analysis.md](codebase-analysis.md) section 6 (code) and section 11 (decisions).
- [project-plan.md](project-plan.md) modules B00 to B25.
- GitHub issues [#49](https://github.com/Dumb-Code/Project-Nublar/issues/49), [#61](https://github.com/Dumb-Code/Project-Nublar/issues/61), [#63](https://github.com/Dumb-Code/Project-Nublar/issues/63).
- [GeckoLib wiki: The Animation Controller (GeckoLib 4)](https://github.com/bernie-g/geckolib/wiki/The-Animation-Controller-(Geckolib4)): later controllers override earlier ones, and `setAnimationSpeedHandler` exists.
- Modrinth version lists for [GeckoLib](https://modrinth.com/mod/geckolib) and [SmartBrainLib](https://modrinth.com/mod/smartbrainlib), filtered to 26.2.
- The animation files under `common/src/main/resources/assets/projectnublar/animations/entity/`.
