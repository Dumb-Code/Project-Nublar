# JurassiCraft 3.0.14: genetics, species data and the cloning chain

Scope: how JurassiCraft 3.0.14 (NeoForge 26.1.2, CAZ LLC, paid, proprietary) stores a genome, makes and spends DNA, runs the cloning machines, breeds animals, grows them and draws their colours. It also lists the full species roster and the data fields behind it. Machine internals (power, sync, block entities) belong to another note. This note covers what each step does to DNA and genome data. It maps each technique to Project Nublar modules B00 to B27 and P2.

This is a clean-room study. The note describes behaviour, state, steps, numbers and timings in my own words. It copies no code. Class names in the jar are obfuscated, so each class has a descriptive name that I inferred, followed by its path under `src/caz/jurassicraft/`. Packages on the brief's exclusion list, the `raw/jurassicraft/` directory and the `META-INF` licence files are not sources for this note.

Label key: `[v]` verified in the decompiled source or the data files (path and lines given). `[inf]` my inference. `[doc]` text the mod itself ships (its English lang provider, which also feeds JEI pages). Any number marked `[inf]` is an estimate.

Date of reading: 2026-10-11. The decompile and the raw jar contents sit in the session scratchpad (`jc/src`, `jc/raw`). Nothing was run. Two methods did not decompile (the colour-palette picker and the period-band setup). I read their bytecode with `javap` and describe only what I could confirm.

---

## What to take from this note (top findings)

1. **The genome is small.** A JurassiCraft animal carries five things: a 10-letter ACGT string, a DNA quality number (0 to 100), four trait multipliers (scale, damage, health, speed), four colour genes (one HSL colour per slot) and two parent ids (lineage). There are no named genes, alleles or dominance. The ACGT string does nothing in play except show on a tooltip. Target: B07, B18.
2. **DNA quality is a gate, not a penalty.** Sequencing a fossil gives a random quality of 5 to 100 in steps of 5. Combining two discs of the same species adds the numbers, capped at 100. The Synthesizer and the Hybridizer accept only quality 100. Nothing in the chain lowers quality, and nothing in the animal uses it except to pass it on. Target: B06, B07, B11.
3. **Cloning is an eight-machine chain with one recipe type.** One recipe type (`lab`) carries a machine id, an operation (carry, sequence, extract, combine, incubate, cultivate, hybridize) and a time in ticks. The data tables stay small and the machines stay generic. Target: B12, B13.
4. **Sex is a player control at hatch time.** Each Incubator slot and the Cultivator have a temperature slider (0 to 100, steps of 5). The slider value is the chance of a male. Bred young roll 50/50. Target: B07 (gender gene), B11.
5. **Breeding rolls the whole clutch at conception.** The female stores one finished offspring record per egg (own genome string, summed quality, blended traits, mixed colours) for 8 minutes. She then lays eggs or gives live birth. Target: B18.
6. **Traits blend, with a one-in-three re-roll.** Each child trait is a random value between the two parent values. In one case in three it is replaced by a fresh roll. Damage and health re-rolls follow the scale re-roll, and speed moves the other way. Target: B18 (heredity rule), B07 (variation).
7. **The Genome Editor changes colours only.** It has five buttons: natural variant, random variant, reset, apply and clone-colours-to-another-disc. It cannot change stats, sex, or species. Hybrids come from the Combinator in a second mode that needs exact parent species at quality 100. Target: B08, B09, backlog hybrids.
8. **Growth runs on a world clock that counts skipped time.** Age ticks once per 8 game ticks. A saved counter adds time lost to sleeping or `/time set`, so animals keep growing. Stages are 25 / 50 / 75 percent. Adults breed from 75 percent. A growth serum jumps to 100 percent. Target: B16.
9. **Species data is one JSON per species with strict validators.** 129 species files carry stats, sizes, growth, reproduction, social, habitat, sounds, abilities, bones, skeleton grid and colour families. The codec rejects bad clutch ranges, eye heights above body height, ragged skeletons and odd inventory sizes. Target: B00.
10. **The roster is 129 species.** 84 carnivores, 45 herbivores, 94 egg layers, 35 live-bearers, 28 marine, 8 hybrids. Growth to full size ranges from 4.75 to 75 Minecraft days (median 11.25). Full table in section 11. Target: B00, R02.

---

## 1. Sources read

All paths are under `src/caz/jurassicraft/` unless the row says `raw/`. Package names are obfuscated, and file names repeat across packages, so the path is the only safe identifier.

| Inferred name | Path | What it holds |
| --- | --- | --- |
| genome container ("Genetics" component) | `gyovxqy/aeygzxj.java:10-118` | genome string, quality, traits, lineage, colours; save and load keys |
| metabolism component | `gyovxqy/gjtxsudo.java` | energy and water pools (touched only to note the link to species stats) |
| reproduction component | `gyovxqy/kodkieb.java:16-258` | pregnancy, clutch list, delivery, cooldown, nest memory |
| ownership component | `gyovxqy/ummp.java` | owner id and order (imprint) |
| offspring record and conception | `aprffirmi/aeygzxj.java:19-91` | one finished animal-to-be; builds from parents or from DNA |
| laid egg entity | `aprffirmi/dnukfuguy.java:28-185` | stores an offspring record, counts down to hatch, can be picked up |
| pack size limit | `aprffirmi/gjtxsudo.java:7-35` | counts same-species animals within 16 blocks |
| DNA data record | `etemhnv/gjtxsudo.java:17-111` | species, quality, genome string, traits, colours; tooltip |
| DNA item helpers | `etemhnv/dbgouk.java:9-69` | read and copy DNA components on any item |
| trait record | `etemhnv/ummp.java:7-74` | four multipliers, roll and breed rules |
| colour gene set | `etemhnv/aeygzxj.java:11-180` | four slots, inherit and roll rules, tint conversion |
| HSL colour | `etemhnv/kodkieb.java:8-96` | hue, saturation, lightness; packing; RGB conversion |
| palette enum (46 named palettes) | `etemhnv/dnukfuguy.java:9-157` | HSL ranges, weights, role tags; weighted pick (bytecode) |
| colour slot enum | `etemhnv/vtclb.java:8-43` | base_body, primary_pattern, secondary_pattern, underside |
| lineage record | `etemhnv/jgdp.java:9-32` | mother id, father id |
| species definition | `ysjrlvxg/dbgouk.java:25-652` | the whole species record and its codec |
| diet, food, temperament, sleep, birth type, curve enums | `ysjrlvxg/vtclb.java`, `aeygzxj.java`, `kdhoqn.java`, `jgdp.java`, `dnukfuguy.java`, `ummp.java` | small enums |
| geologic period enum | `ysjrlvxg/omogwgxr.java:7-110` | 13 periods, fossil depth bands (bytecode for setup) |
| fossil species picker | `ysjrlvxg/gjtxsudo.java:10-56` | species at a block position |
| species list cache and random pick | `ysjrlvxg/onzkovhp.java:17-81` | random species by predicate, hybrid lookup (partly bytecode) |
| species registry access | `ysjrlvxg/kijk.java:21-119` | lookup, fallback species, warning logs |
| plant DNA | `cnxjsqwpw/gjtxsudo.java`, `ummp.java`, `dnukfuguy.java`, `aeygzxj.java` | plant species, quality, item helpers |
| sex and growth stage enums | `ctygd/rtwzimpls.java`, `ctygd/kodkieb.java` | male/female; infant, juvenile, adolescent, adult |
| growth clock | `ctygd/ummp.java:14-128` | saved count of skipped time |
| dinosaur entity (genetics use) | `ctygd/omogwgxr.java:362-1000` | age, size, stats, carcass drops |
| mating goal | `ztiurxyn/fgwme.java:36-215` | search, approach, mate |
| breeding wand | `bugratnc/gjtxsudo.java` | creative pairing tool |
| hatched egg item | `bugratnc/omogwgxr.java:51-95` | spawns an infant from DNA |
| storage disc item | `bugratnc/ysulhvcy.java` | three synthesis charges |
| spawn egg | `bugratnc/jgdp.java` | adult or hatchling, sex cycle |
| growth serum | `bugratnc/kdhoqn.java` | sets age to maximum |
| lab recipe | `vtrqoo/jgdp.java:9-159`, `vtrqoo/onzkovhp.java`, `vtrqoo/kodkieb.java` | machine ids, DNA operations |
| lab machine | `dnukfuguy/dbgouk.java:1-699` | what each machine does to DNA |
| generic recipe machine | `dnukfuguy/kdhoqn.java` | progress, status, output |
| genome editor block entity | `dnukfuguy/jgdp.java:1-194` | preview state, apply, clone |
| genome editor menu and screen | `snqzaxu/kodkieb.java:1-236`, `wgpfjieca/kodkieb.java:1-447` | the five buttons |
| slot layouts and nutrient values | `ufpurxw/hmocea.java`, `ufpurxw/onzkovhp.java` | which slot takes what; cultivator nutrients |
| render layer set | `stta/jgdp.java`, `stta/aeygzxj.java`, `stta/dbgouk.java`, `stta/gjtxsudo.java` | layer names, tints |
| config | `dnukfuguy.java` (root, config lines only) | creature and generation switches |
| English lang provider | `kfollsxb/kijk.java` | JEI text, tooltips |
| data files | `raw/data/jurassicraft/jurassicraft/species/*.json` (129), `.../plant/*.json` (49), `raw/data/jurassicraft/recipe/**`, `tags/item/*.json`, `data_maps/item/food_nutrients.json` | roster, plants, lab recipes |

Version note. JurassiCraft 3.0.14 targets Minecraft 26.1.2 on NeoForge. It uses 26.x names that Project Nublar meets in P2: `Identifier` (for resource ids), `ValueInput` and `ValueOutput` (save and load), `ItemStackTemplate`, `SavedDataType` with a codec, `hurtServer`, `EntitySpawnReason` and `ContainerData` menus `[v]` (for example `aprffirmi/dnukfuguy.java:159-176`, `ctygd/ummp.java:14-16`, `vtrqoo/vtclb.java`). The jar is therefore a worked example of the 26.x API for P2.

---

## 2. The genome data model

### 2.1 What a "genome" holds

**In game.** The player never sees "genes". A DNA item shows four lines: the species, "DNA Quality: n%", a "genetic code" of 10 letters, and an optional "Build" percent and "Coloration" row of coloured squares. A quality over 75 shows green, over 50 yellow, over 25 gold, and 25 or less red. An empty genome string shows as obfuscated "aaa" text. `[v]` `etemhnv/gjtxsudo.java:55-90`.

**Mechanism.** A DNA record has five fields:

1. Species (a registry key).
2. Quality, an integer clamped to 0 to 100. The default is 100.
3. Genome string. It must be exactly 10 letters from A, C, G, T. A bad string is replaced by an empty string on construction. `[v]` `etemhnv/gjtxsudo.java:26-29`.
4. Traits, optional. Four floats: scale, damage, health, speed. They are absent on fossil-derived DNA and are rolled later.
5. Colours. Four optional slots (section 2.4). Empty on fresh DNA.

The living animal holds the same five things plus lineage in one "Genetics" component. It saves them under the keys `Genetics`, `DNAQuality`, `GeneticAttributes`, `Lineage` (only if a parent is known) and `Colors` (only if any slot is set). `[v]` `gyovxqy/aeygzxj.java:97-117`. A new component starts with quality 100, an empty string and no traits. The string and the traits roll on first use. `[v]` `gyovxqy/aeygzxj.java:32-58`.

The genome string is built by picking one random letter ten times. Two animals' strings mix by taking each position from one parent at random (section 6.4). No rule reads the letters. The only readers I found are the tooltip and the crossover. `[inf]` from a search for the accessor in the entity, item and menu packages. A tool I did not trace (the creature inspector item) sends other fields, not the string.

**Platform overlap.** Vanilla data components and codecs cover the item side. The record uses `RecordCodecBuilder`, a 0-to-100 `Codec.intRange` and a network `StreamCodec` composite. Nublar's P2 plan already moves the genome to a `StreamCodec` component (B07). Nothing here needs a library.

**Verdict.** Adopt the shape "species + number + small fixed record, saved as components". Drop the ACGT string. It has no effect, and it costs a field on every item.

### 2.2 No alleles, no dominance

**In game.** Two parents of one species make a child that looks like a mix. The player cannot pick a trait to keep.

**Mechanism.** There is no gene list, no allele pair and no dominance rule `[v]` (the component in 2.1 is the full state). The only mixing rules are in section 6.4. Every trait is a continuous number or a colour. Every mix is "pick a random value between the parents" or "pick one parent's colour".

**Platform overlap.** None.

**Verdict.** Nublar's 26-gene plan (B07, `docs/ai-behaviour-genetics-reconciliation.md`) is a different and larger design. JurassiCraft is a reference for the small, visible traits only (scale, damage, health, speed, colour), not for the gene model.

### 2.3 The four traits

**In game.** Two raptors of the same species differ. One is a little bigger, tougher and slower. The tooltip shows "Build: 104%" for scale.

**Mechanism.** The record holds scale, damage, health and speed. `[v]` `etemhnv/ummp.java:7-74`.

- Fresh roll: scale is uniform in 0.8 to 1.2. Let f = (2 + scale) / 3. Damage is uniform in 0.8 to 1.2 times f. Health is the same. Speed is uniform in 0.8 to 1.2 divided by f.
- So a big animal hits harder and has more health, and it is slower. A small animal is the reverse.
- Reachable range after one roll: damage and health 0.747 to 1.28. Speed 0.75 to 1.286. `[inf]` from the formula.
- The animal applies the traits every time it refreshes its stats: max health = growth-interpolated species health times the health trait. Speed and attack work the same way. `[v]` `ctygd/omogwgxr.java:442-465`. For a raptor (adult health 35) the adult range is about 26 to 45 hit points. `[inf]`.
- The scale trait also multiplies the width, the height and the eye height of the hitbox. The size is clamped to 0.3 to 15 blocks. `[v]` `ctygd/omogwgxr.java:496-507`.
- Wild, spawn-egg and creative animals roll traits when first needed. Cloned animals keep traits only if the DNA carried them. Fossil DNA does not carry traits, so each clone rolls its own at hatch. `[v]` `aprffirmi/aeygzxj.java:45-48`.

**Platform overlap.** Attribute modifiers or `setBaseValue` do the application. JurassiCraft sets base values directly and heals up if max health grows. A modifier with a stable id is cleaner.

**Verdict.** Adapt. Keep "derived stats come from the species number times a trait multiplier". Keep the scale-to-stats link (big is strong and slow) as an optional rule in `GenomeEffects`. Apply modifiers, not base values.

### 2.4 Colour genes

**In game.** A species has up to four paintable slots: body, primary pattern, secondary pattern and underside. Each slot holds one colour. The colour shows as a tint on a grey texture layer. A species with colour families lists which slots it supports. In the Genome Editor the player rolls or resets those colours.

**Mechanism.**

- A colour is hue 0 to 359, saturation 0 to 100, lightness 0 to 100. It packs into one int as hue shifted 16, saturation shifted 8, lightness. An int below zero means "no colour". `[v]` `etemhnv/kodkieb.java:8-96`.
- The colour set is four ints, one per slot. `-1` means "slot unset". It saves as a map from slot name to packed int. `[v]` `etemhnv/aeygzxj.java:11-60`.
- There are 46 named palettes (brown_earth, tan_ochre, olive, slate_gray, rust, amber, crimson, steel_blue, grass and so on). Each has a hue range, a saturation range, a lightness range, a weight (default 10) and role tags (base, pattern, underside, markings, display). `[v]` `etemhnv/dnukfuguy.java:10-55`. Examples: brown_earth is hue 18 to 48, saturation 12 to 26, lightness 25 to 42, weight 60. Charcoal is hue 190 to 240, saturation 2 to 9, lightness 13 to 24. Amber is hue 20 to 48, saturation 62 to 88, lightness 14 to 36.
- A species lists, per slot, which palettes it may use (`color_families`, about 6 per slot). A "natural" roll picks one palette from that list by weight, then picks a hue, saturation and lightness uniformly inside its ranges. `[v]` bytecode of the weighted pick and of the per-slot roll (`etemhnv/dnukfuguy.java`, `etemhnv/aeygzxj.java:97-123`).
- A "random" roll ignores the palettes: hue 0 to 359, saturation 20 to 35, lightness 26 to 38. Colours are muted by design. `[v]` `etemhnv/aeygzxj.java:107-109`.
- The role tags are not used by the roll. They look like authoring hints. `[inf]`.
- 129 species use these slot sets: 64 have body, primary and underside; 37 have body only; 25 have all four; 2 have body and underside; 1 has body and secondary. `[v]` roster scan.
- Colours are the only genome part that wild and lab animals can lack. A new fossil disc has no colours. The animal then uses the default skin. Colours exist only after the player edits a disc (section 5) or a parent had them. `[inf]` from `etemhnv/gjtxsudo.java:31-33` and the absence of a roll at spawn in `ctygd/omogwgxr.java:203-205`.

**Platform overlap.** HSL to RGB is plain arithmetic. Nothing in Minecraft or NeoForge helps.

**Verdict.** Adapt for B07 and B09: curated HSL palette families per species, weights, and a muted random roll. The weights and the muted ranges give good-looking animals with no art work. Nublar already plans per-layer colours with a colour wheel. Add "natural roll from species families" as the default roll.

### 2.5 Lineage

**In game.** An animal born at the park remembers its mother and father. A clone remembers neither. A parent will not mate again while its own young, under half grown, stand within 16 blocks, unless the species allows it.

**Mechanism.** Lineage is two optional ids: mother and father. A child gets them at conception. `[v]` `etemhnv/jgdp.java:9-32`, `aprffirmi/aeygzxj.java:37`. The mating check uses them: it looks for living, non-carcass animals within 16 blocks, with growth at 0.5 or less, whose lineage names this animal. If any exist and the species flag `breed_around_offspring` is false, the animal does not look for a mate. `[v]` `ztiurxyn/fgwme.java:186-188`. Lineage is not read for inbreeding, and picking up a laid egg drops it. `[v]` `aprffirmi/dnukfuguy.java:127-141`.

**Platform overlap.** UUID codecs in vanilla.

**Verdict.** Adopt the "own young nearby blocks breeding" gate as an optional species flag in B18. It stops a paddock from filling with babies. Cost: one box query per eligible animal per mate search (section 6.2).

### 2.6 Where the state lives

| State | Where | Notes |
| --- | --- | --- |
| Species, quality, string, traits, colours | one DNA item component, and a species-key component on every bone, tissue and meat item | `etemhnv/dbgouk.java:13-67` |
| Animal genome | entity saved data (`Genetics`, `DNAQuality`, `GeneticAttributes`, `Lineage`, `Colors`) | `gyovxqy/aeygzxj.java:97-117` |
| Colours for rendering | four synched int fields on the entity, one per slot | `ctygd/omogwgxr.java:319-345` |
| Scale trait for rendering | one synched float | `ctygd/omogwgxr.java:303-317` |
| Sex | one synched boolean | `ctygd/omogwgxr.java:347-358` |
| Age | one synched long plus a flag | `ctygd/omogwgxr.java:362-385` |

The traits other than scale do not sync. The server alone applies them to attributes. `[inf]`.

---

## 3. DNA quality: how it is made and lost

### 3.1 Sources and their quality

| Source | Quality of the DNA made | Species | Path |
| --- | --- | --- | --- |
| Soft tissue in the Sequencer | random: (1 to 20) times 5, uniform | the tissue's species | `dnukfuguy/dbgouk.java` (action "sequence") and `etemhnv/gjtxsudo.java:35-37` |
| Mosquito or aphid in amber in the Extractor | same random roll | a random non-hybrid, non-marine species (mosquito, animal) or a random prehistoric plant (aphid) | `dnukfuguy/dbgouk.java` (action "extract_amber") |
| Sea lamprey in the Extractor | same random roll | a random non-hybrid marine species | action "extract_marine" |
| Dinosaur meat in the Extractor | the quality of the dead animal, unchanged | that animal's species, and its whole record (string, traits, colours) | action "carry" |
| Wild, spawn-egg or creative animal | 100 | the animal's own | `bugratnc/jgdp.java` builds with quality 100 |
| Bred young | sum of both parents' quality, capped at 100 | the mother's species | `aprffirmi/aeygzxj.java:37` |

Soft tissue comes from grinding bones, fossil eggs or plant fossils. Each grind gives one result, chosen by weight. A bone gives bone meal, flint or soft tissue at 3 : 2 : 1, so soft tissue comes one time in six. A fossil egg gives soft tissue, bone meal or flint at 1 : 1 : 1, so one time in three. A plant fossil gives bone meal 2, flint 1 and plant soft tissue 1, so one time in four. `[v]` `recipe/grinding/**` (weights); the pick is a weighted draw of one result `[inf]` from the JEI text "one time in six" `[doc]` `kfollsxb/kijk.java:188`. A tissue item holds only the species, so its DNA has no string, no traits and no colours. `[v]` `etemhnv/dbgouk.java:13-38`.

The random quality is uniform over 20 values. One in 20 is 100. `[inf]`.

### 3.2 Building quality 100

The Combinator joins two discs of the same species and adds their quality, capped at 100. It keeps the first disc's string, traits and colours when set, and falls back to the second disc's values when the first has none. `[v]` `etemhnv/gjtxsudo.java:43-45`; recipe flag `same_species`. For plants the rule is the same without strings or colours. `[v]` `cnxjsqwpw/gjtxsudo.java:33-35`.

How many sequenced discs does one species need? Each disc is a draw from 5 to 100 in steps of 5. The player needs a total of 100. The expected number of base discs is about 2.53. Half of the cases need exactly two. One case in twenty needs only one. `[inf]` computed from the distribution: P(1 disc) 0.05, P(2) 0.52, P(3) 0.31, P(4) 0.10, P(5 or more) 0.02.

### 3.3 What spends or loses quality

No machine reduces quality. The numbers only go up, to a cap of 100.

- The Synthesizer slot and the Hybridizer accept only discs at 100. `[v]` `ufpurxw/hmocea.java` (slot filter for the synthesizer: a disc with full-quality DNA) and `vtrqoo/jgdp.java:63-77`.
- A disc has three synthesis charges. A disc that sits alone in the Synthesizer slot comes back with one charge fewer, and is gone when the third is spent. If the slot holds a stack of discs, one whole disc leaves the stack and nothing comes back. The tooltip reads "Syntheses left: n of 3" and the item bar shows it. `[v]` `bugratnc/ysulhvcy.java`, `dnukfuguy/dbgouk.java:513-540`; text `[doc]` `kfollsxb/kijk.java:93`. Combining or sequencing writes into a fresh empty disc, so charges are per disc.
- Hybridizing consumes one 100-quality disc of each parent species. The output keeps the first parent disc's string, quality and traits, but its colours reset to empty. `[v]` `dnukfuguy/dbgouk.java` (hybridize branch) and `etemhnv/gjtxsudo.java:47-49`.
- In the world, the animal's own quality changes only at birth. A quality-100 animal that breeds with a quality-100 animal makes quality-100 young. Two clones of quality 40 and 35 make young of 75.
- Quality has no in-game effect on the animal: no health penalty, no deformity, no death. I found no reader other than the drop code and the breeding code. `[inf]`.

**Platform overlap.** None.

**Verdict.** Adapt. Nublar's drive already adds a percent per species and caps it at 100 (B06). JurassiCraft confirms the model. Two ideas to take: (a) a fixed set of steps (5 percent) in the random roll, so numbers read cleanly; (b) a charge count on the data carrier as a balance lever. Defer (b). Drop "quality passes to the young as a sum", because Nublar's heredity rule is different (section 6.4).

---

## 4. The cloning chain, step by step

### 4.1 Overview

The chain uses eight machine types: Extractor, Sequencer, Synthesizer, Combinator (which also runs as the Hybridizer), Embryonic Machine, Embryo Calcification Machine, Incubator and Cultivator. The Genome Editor is a ninth block that edits colours in place (section 5). `[v]` `vtrqoo/kodkieb.java:8-48`, `kfollsxb/kijk.java:128-137`.

```text
Fossil / bone / egg --Grinder--> soft tissue ------------------+
Amber (mosquito or aphid) -----------------Extractor-----+     |
Sea lamprey -------------------------------Extractor-----+     |
Dinosaur meat (butchered carcass) ---------Extractor-----+     v
                                                    storage disc (DNA)  <--Sequencer--
                                                         |
                    two discs, same species --Combinator--> disc, quality summed
                                                         |
          (quality 100)  disc + empty tube + nucleotides --Synthesizer--> DNA tube
                                                         |
          DNA tube + petri dish + empty syringe --Embryonic Machine--> embryo syringe
                                                         |
        egg-layers: syringe + vanilla egg --Calcification--> dinosaur egg --Incubator--> hatched egg
        live-bearers: syringe --Cultivator (water + food)--> hatched egg
                                                         |
                                      right-click a block --> infant animal
```

The machine checks species type. A live-bearer's syringe in the Calcification Machine shows "Bears live young - use a Cultivator". An egg-layer's syringe in the Cultivator shows "Lays eggs - use an Embryo Calcification Machine". `[doc]` `kfollsxb/kijk.java:178-179`; `[v]` `dnukfuguy/dbgouk.java:513-517` (the check reads the species' birth type).

### 4.2 Machine table

All times are processing ticks (20 per second) read from the recipe JSON. `[v]` `raw/data/jurassicraft/recipe/{extraction,sequencing,combining,hybridizing,synthesis,embryo,incubation,cultivation}/*.json`.

| Machine | Input | Output | Ticks | What it does to the DNA |
| --- | --- | --- | --- | --- |
| Extractor | amber item, sea lamprey or dinosaur meat, plus an empty disc | disc | 500 | amber and lamprey: new DNA of a random species with random quality. Meat: copies the animal's whole record, quality included |
| Sequencer (3 lanes) | soft tissue or plant soft tissue, plus an empty disc | disc | 500 | species from the tissue, random quality. If the tissue already holds full DNA, copies it |
| Combinator, normal mode | two discs, same species | disc | 750 | adds quality (cap 100); keeps string, traits, colours |
| Combinator, hybrid mode | up to 8 discs at quality 100 | disc | 750 | output species is the hybrid whose parent list equals the input species list |
| Synthesizer | disc (quality 100, not consumed, loses 1 charge), empty test tube, DNA nucleotides | DNA tube | 750 | copies the record to the tube |
| Embryonic Machine | DNA tube, petri dish (agar), empty syringe | embryo syringe | 200 (default) | copies the record to the syringe |
| Embryo Calcification | embryo syringe, vanilla egg (any item in the `eggs` tag) | dinosaur egg | 200 (default) | copies the record; egg-layers only |
| Incubator (5 egg slots) | dinosaur egg, one peat moss per egg | hatched egg, in the same slot | species `incubation_time` (700 to 11,200; median 3,800) | copies the record; rolls sex from the slot's temperature |
| Cultivator | embryo syringe | hatched egg, in the same slot | 2,000 | copies the record; rolls sex from the temperature; live-bearers only |

Notes.

- The "default" 200 ticks comes from the base recipe codec when the JSON gives no time. `[v]` `vtrqoo/onzkovhp.java` (codec default 200).
- Incubator time. If the egg holds DNA, the machine looks up the species and uses its `reproduction.incubation_time`. Otherwise it uses the recipe time of 3,600. `[v]` `dnukfuguy/dbgouk.java:337-352`. The raptor is 4,200 ticks (3.5 minutes). The tyrannosaurus is 5,500. The brachiosaurus is 6,000. `[v]` species files.
- Incubator and Cultivator transform the item in the slot. The egg stays in its slot and changes into the hatched egg. The output slot and the input slot are the same index. `[v]` `ufpurxw/hmocea.java:40-65`, `dnukfuguy/dbgouk.java:236-242`. The recipe marks the egg "not consumed", and the machine writes the result into that slot. `[inf]`.
- Plant chain. Plant soft tissue to disc (Sequencer, plant subject), combine same plant, synthesize to a plant DNA tube (needs quality 100), then the Embryonic Machine with a plant-cell petri dish and an empty syringe makes a plant callus. The player right-clicks farmland with the callus to plant it. `[v]` recipes `embryo/plant_callus.json`, `combining/plant_storage_disc.json`; text `[doc]` `kfollsxb/kijk.java:198`. Plant cells come from any living plant or leaf block hit by an empty syringe, without harm. `[doc]` `kfollsxb/kijk.java:184`.
- Amber gives aphids and mosquitoes. A mosquito reads out animal DNA and an aphid reads out plant DNA. `[doc]` `kfollsxb/kijk.java:189`.
- Ingredients for the chain are cheap: a disc is one circuit and four iron nuggets, a test tube comes from two glass, a syringe from a tube and four iron nuggets, nucleotides come from smelting a water bottle, liquid agar from smelting Gracilaria seaweed. `[v]` recipe JSON.

**Platform overlap.** One custom `RecipeType` with a `MapCodec` and `StreamCodec` is the 26.x way. The recipe has `ingredients` (1 to 9, each with `count` and `consumed`), `results` (1 to 16, each with a `weight`), and `processing_time`. `[v]` `vtrqoo/onzkovhp.java`, `vtrqoo/dbgouk.java`, `vtrqoo/vtclb.java`. The machine finds the recipe with `RecipeManager.createCheck`. Nothing outside vanilla is needed.

**Verdict.** Adapt. One recipe type with three enum-like fields (machine, DNA operation, subject creature or plant) keeps the JSON small and lets one block entity class serve eight machines. Nublar's plan has fewer machines (Sequencer, Egg Printer, Incubator). Take the recipe shape, not the machine count.

### 4.3 Fossils and bones to soft tissue

**In game.** The player mines an encased fossil, washes it in a Cleaning Station and gets one bone of that animal. Bones, fossil eggs and plant fossils grind into soft tissue. Killing a live animal leaves a carcass. Breaking the carcass drops fresh bones (each with a nine-in-ten chance) and meat. `[doc]` `kfollsxb/kijk.java:187-188`.

**Mechanism.**

1. Which species a fossil block holds depends on its depth and a world-seed hash (section 10.4).
2. A dead animal becomes a carcass. The carcass takes (2 times the whole-number part of the square root of width times height) hits, at least 1 and at most 10 (a raptor 2, a tyrannosaurus 6, a brachiosaurus 10). `[v]` `ctygd/omogwgxr.java:837-850`.
3. Every hit on the carcass drops 1 or 2 meat plus the Looting level. If the animal burns, the meat is cooked steak with the species only. Raw meat carries the full record: species, quality, string, traits and colours. `[v]` `ctygd/omogwgxr.java:895-915`. This makes any live animal a source of full DNA (quality as the animal's).
4. The hit after the last one removes the carcass and drops bones. Each bone type of the species drops with a nine-in-ten chance, carrying species, quality and string (no traits, no colours), plus a "fresh" flag. `[v]` `ctygd/omogwgxr.java:948-960`.
5. Fossil-derived items carry only the species key. The Sequencer rolls fresh quality for them.

**Platform overlap.** `DataComponentType` per field; vanilla loot.

**Verdict.** Adapt for B02 and B04. Two ideas fit Nublar's B04 "living DNA sources": raw meat from a carcass carries the animal's genome, and cooked meat carries only the species. Both are cheap and readable.

### 4.4 The Extractor, the Sequencer and the disc

**In game.** The player puts amber, a lamprey or meat into the Extractor with an empty disc. After 25 seconds the disc holds DNA. The Sequencer does the same with soft tissue and runs three lanes at once.

**Mechanism.** The machine reads the recipe's DNA operation and builds the output record. "Carry" copies the input record onto the output item. "Sequence" copies a full record if the tissue has one, or builds a fresh one from the species with a random quality. "Extract amber" and "extract marine" pick a species by uniform random from a cached list (all non-hybrid species with the right sea flag) and build a fresh record. `[v]` `dnukfuguy/dbgouk.java:356-420` and `ysjrlvxg/onzkovhp.java:17-81`. Hybrids never appear: the list filter drops any species with a non-empty `hybrid_of`. `[v]` `ysjrlvxg/onzkovhp.java:74-76`.

Slot rules: Sequencer lane i takes tissue in slot 2i and a disc in slot 2i+1, and writes to slot 6+i. The Extractor takes the source in slot 0 and a disc in slot 1, and writes to four output slots. A "blank" disc has no creature or plant data. `[v]` `ufpurxw/hmocea.java:40-100`.

**Platform overlap.** `WorldlyContainer` side lists cover the slot rules for hoppers.

**Verdict.** Adopt the idea that amber and lamprey roll a random species from a cached filtered list. Nublar's B03 rolls the amber species in the Processor from weights. A uniform list is the simple baseline.

### 4.5 The Combinator and the Hybridizer

**In game.** The Combinator has a toggle. In normal mode it merges two discs of the same species. In hybrid mode it takes up to eight discs. If the discs' species list equals a hybrid's parent list, the output is a disc of the hybrid species.

**Mechanism.** `[v]` `vtrqoo/jgdp.java:63-77`, `dnukfuguy/dbgouk.java:425-445`, `ysjrlvxg/onzkovhp.java:40-54`.

1. The recipe matches only if every input disc that holds creature DNA is quality 100, and the list of those species equals some hybrid's `hybrid_of` list as a multiset (same size, same members, order does not matter).
2. The output record is the first valid disc's record with the species swapped to the hybrid. Colours reset.
3. After the run, the machine removes one item from each valid input disc stack. The recipe marks the disc "not consumed", and the machine removes them by hand. `[v]` `dnukfuguy/dbgouk.java:500-512`.
4. Hybrids need exact parents. Example lists: Indominus needs 8 species (tyrannosaurus, velociraptor, carnotaurus, giganotosaurus, therizinosaurus, majungasaurus, rugops, deinosuchus). Indoraptor needs 5. Spinoraptor needs 2 (velociraptor, spinosaurus). `[v]` species `hybrid_of` arrays.
5. A hybrid is a normal species afterwards. It breeds only with its own kind (the mating filter needs equal species ids) and it never spawns in the world. `[v]` `ztiurxyn/fgwme.java:171`, `dnukfuguy.java` config text on `naturalSpawning`.

**Platform overlap.** Plain data.

**Verdict.** Defer as a design reference. Nublar lists hybrids as out of scope for B10 and as backlog. When it comes, "parent list in species data, multiset match, all parents at full DNA" is a clean, data-only rule.

### 4.6 The Synthesizer, the Embryonic Machine and the Calcification Machine

**In game.** A full-quality disc plus an empty test tube plus nucleotides makes a DNA tube. A tube plus a petri dish plus an empty syringe makes an embryo syringe. For an egg-layer, the syringe plus any vanilla egg makes a dinosaur egg.

**Mechanism.** All three copy the record along. The Synthesizer refuses to run on a disc below 100. It also removes one synthesis charge from the disc afterwards. The Calcification Machine refuses a live-bearer's record. `[v]` `dnukfuguy/dbgouk.java:391-404, 474-499, 525-540`. The petri dish step needs agar, so the player needs Gracilaria seaweed from the sea floor. `[doc]` `kfollsxb/kijk.java:185`.

**Platform overlap.** None.

**Verdict.** Adapt for B11. Nublar already has synthesis, an Egg Printer and an Incubator. JurassiCraft adds two cheap check rules worth keeping: "live-bearers cannot go through the egg route" and "the synth input slot rejects unfinished DNA, so the player cannot waste a run".

### 4.7 The Incubator and sex

**In game.** The Incubator holds five eggs. Each slot has a Temperature slider from 0 to 100 percent in steps of 5. The GUI shows "Male: n%" and "Female: n%". The default is 50. Each egg also needs one peat moss.

**Mechanism.** When the run ends, the machine builds the hatched-egg item with the same DNA and a sex flag. The flag is male when a random number from 0 to 99 is below the slider value. So 50 means 50 percent male, 100 means always male, 0 means always female. `[v]` `dnukfuguy/dbgouk.java:84-87, 436-445`; text `[doc]` `kfollsxb/kijk.java:103-106`. The Cultivator has one slider with the same rule.

The hatched egg item shows "Sex: Male/Female". The item name reads "Hatched <species> Egg", or "Gestated <species>" for a live-bearer. `[v]` `bugratnc/omogwgxr.java:30-45`.

**Platform overlap.** A slider widget in the menu; `ContainerData` carries the numbers.

**Verdict.** Drop for Beta, with one note. Nublar decided that gender is a gene with "sure at 80, 30 percent random below". JurassiCraft shows the same player control through a machine setting. If the lead later wants a hatch-time lever, the slider is the cheapest form. Record as an open question.

### 4.8 The Cultivator

**In game.** The Cultivator is a tall, dye-coloured machine for live-bearers. The player fills a water tank with two buckets, and feeds it ordinary food. Four nutrient bars (proximates, minerals, vitamins, lipids) fill up. When water is full and every bar holds at least 1,500 of 3,000, the machine runs for 100 seconds and turns the syringe into a hatched egg.

**Mechanism.** `[v]` `dnukfuguy/dbgouk.java:130-256`, `ufpurxw/onzkovhp.java:18-120`, data `data_maps/item/food_nutrients.json`.

- Water: the machine takes a water bucket from slot 2 while its level is below 2, gives back an empty bucket in slot 3 (stack to 16), and raises the level by 1. It runs only at level 2.
- Food: each food item has four coefficients in a data map (for example cooked porkchop 0.58 / 0.39 / 0.49 / 0.47 for proximates, minerals, vitamins, lipids; milk bucket 0.18 / 0.26 / 0.22 / 0.60; apple 0.06 / 0.065 / 0.1 / 0.01). One item adds, to each nutrient, floor((base + random) times coefficient). The base and spread are 800 + 0 to 200 for proximates, 900 + 0 to 100 for minerals and vitamins, 980 + 0 to 100 for lipids. Each bar caps at 3,000.
- A run costs one water level and 1,500 from every bar.
- Minerals are the slowest bar for meat: a cooked porkchop adds about 370 minerals but about 520 proximates. About five cooked chops fill every bar to 1,500, and bread, carrots or potatoes help minerals. `[inf]`.
- The block comes in 16 dye colours, repaintable by right-click. `[doc]` `kfollsxb/kijk.java:199`.

**Platform overlap.** Item data maps (`DataMapType`) carry the nutrient table. That is a NeoForge feature.

**Verdict.** Defer. It is a good item sink and teaches the player to feed. Nublar's feeders (B20) and the Incubator's plant matter (B11) cover similar ground. The data-map table is a neat way to rate foods without code. Consider it for the B20 feed values.

### 4.9 The hatched egg and the first animal

**In game.** The player right-clicks a block with a hatched egg. An infant steps out. If the species is imprintable and the player did not sneak, the player becomes the owner and gets a gold "imprinted" message.

**Mechanism.** `[v]` `bugratnc/omogwgxr.java:51-95`, `aprffirmi/aeygzxj.java:45-48, 80-91`.

1. The item rebuilds an offspring record from the DNA: species, a random sex (replaced by the item's sex), string (kept if valid), quality, traits (kept, or rolled if absent), colours, empty lineage.
2. The record spawns the animal. It sets the age to 0, refreshes its stats, and fills energy and water to the maximum.
3. 71 of 129 species are imprintable. `[v]` roster scan.
4. The spawn egg (creative) makes an adult at full age, or a hatchling if the player sneaks. It uses quality 100 and a random string. `[v]` `bugratnc/jgdp.java`.

**Platform overlap.** `EntitySpawnReason.SPAWN_ITEM_USE` for the item path and `BREEDING` for bred young. Both exist on 26.x.

**Verdict.** Adopt the "hatched egg is a sexed, finished DNA item that spawns an infant" shape for B11. Nublar's lead decided that the hatchling gets no owner (D17 default). Keep that decision. Imprint is JurassiCraft's taming route and is a separate call (B19).

---

## 5. The Genome Editor

**In game.** The player puts a DNA disc in slot 1. A 3D preview of the animal appears and turns when dragged. Five buttons sit beside it:

1. Generate Natural Variant: rolls colours from the species' palette families.
2. Generate Random Variant: rolls muted random colours.
3. Reset Dinosaur Variant: clears the colours.
4. Apply to DNA: writes the previewed colours onto the disc.
5. Clone DNA: copies the colours already saved on the first disc onto a second disc of the same species in slot 2.

A label shows "Default appearance", "Saved variant", "Natural variant", "Random variant" or "Reset (unapplied)". A badge shows "Unapplied changes" until the player applies. A species without colour families shows "This species does not support colour variants yet". Clone shows why it is blocked: first slot empty, target empty, different species, or no saved colour on the first disc. `[doc]` `kfollsxb/kijk.java:141-160`.

**Mechanism.** `[v]` `snqzaxu/kodkieb.java:128-205`, `dnukfuguy/jgdp.java:1-194`, `wgpfjieca/kodkieb.java:97-180`.

- The editor is a block entity with two disc slots. It keeps a "preview" colour set, a preview state enum (default, committed, natural, random, reset) and three flags (unapplied, applied, cloned).
- A button press is a menu button click. The server checks that the menu is still valid and that a disc is present. It rolls colours with the server random, stores them as the preview, and sets the state. The client draws the preview animal from the synced ints.
- Apply copies the disc stack and replaces only its colours field. The species, quality, string and traits stay. The editor then clears the unapplied flag.
- Clone writes the first disc's saved colours onto the second disc. It needs the same species and a saved colour set on the first disc.
- The editor changes nothing else: not stats, not sex, not species. There is no time, no cost, and no risk shown.
- A config option draws a faint hologram of the animal above the block, using the colours of the disc in slot 1. `[v]` `dnukfuguy.java` config `genomeEditorHologram`.

**Platform overlap.** `AbstractContainerMenu.clickMenuButton` is the vanilla route for button presses, so the server checks come free. The preview animal is a client-only entity made from the species type. `[v]` `wgpfjieca/kodkieb.java:131-140`.

**Verdict.** Adapt for B08 and B09.

- Take: the staged preview with an explicit Apply, an "unapplied" flag, a "natural variant" roll from species families, and "clone colours to another disc". They fit Nublar's server-validated edit packet and its colour-wheel plan.
- Do not copy the limit to colours. Nublar's editor must change stats and genes as well (B07).
- The preview entity made with `EntityType.create` on the client is cheap. Reuse the pattern for the entity preview in B08.

---

## 6. Breeding and inheritance

### 6.1 Who can breed

**In game.** Two adult animals of the same species and opposite sex, left alone in a calm paddock, find each other and mate. A heart cloud shows. The female is pregnant for about 8 minutes. A Pregnancy Test item (right-click) says whether she carries young.

**Mechanism.** An animal is eligible when all of these hold. `[v]` `gyovxqy/kodkieb.java:60-66`.

- Breed cooldown is zero.
- She is not pregnant and not mid-delivery.
- It is alive, not a carcass, and not asleep.
- Its growth stage is ADULT (growth above 75 percent).
- Its health equals max health.

Nothing about food, water, mood, stress, light, a nest or a player item is checked. Pack size is the only environmental gate (section 6.2).

**Platform overlap.** A vanilla `Goal` with MOVE and LOOK flags.

**Verdict.** Adapt. The list is short and each term is cheap. Nublar's gate (nest in range, fertility roll, stress under the species threshold) is stricter and the lead decided it. Keep the lead's gate and add full health as an extra term if wanted.

### 6.2 Finding a mate

**Mechanism.** `[v]` `ztiurxyn/fgwme.java:36-110`, `171-185`.

1. The goal checks every 40 to 79 ticks. It runs only when the animal is eligible and has no attack target.
2. It stops at once if the pack is full (below).
3. It then passes a one-in-twenty roll. So an eligible animal tries about once a minute on average. `[inf]`.
4. It finds the nearest same-species, opposite-sex, eligible animal within 16 blocks that has no target. It tests path access to at most six of them. Marine animals skip the path test.
5. It walks toward the mate. Each 10 ticks it renews the path and shows a few hearts. It gives up after 300 ticks (15 seconds).
6. When the two bounding boxes, widened by 3 blocks, touch, mating starts and both show 12 hearts.
7. If both are still eligible, the female becomes pregnant. If not, the goal stores a one-line reason ("mother injured 12.5/35.0", "father asleep") for the debug display.

Pack size. The count is every living, non-carcass animal of the same species in a box 16 blocks around the animal. Breeding stops when the count reaches the limit. The limit is the config `maxHerdSize` (default 4), or the species `max_herd_size` when the config is 0. The config `packSizeLimitsBreeding` (default on) turns the check off. `[v]` `aprffirmi/gjtxsudo.java:7-35`, `dnukfuguy.java:22-23`. With the default config, 4 animals within 16 blocks stop all breeding. The Breeding Wand ignores the cap.

Cost. The mate search runs only for eligible, idle animals, every ~60 ticks. Each run is one box query with a class filter. The pack count is one more box query. `[inf]` cheap (about two queries per eligible animal per minute).

**Platform overlap.** `level.getEntitiesOfClass` with a class filter and a predicate. A typed query is also friendly to Lithium.

**Verdict.** Adapt for B18. The pack cap is a close analogue of Nublar's per-area breeding cap (decision D20, default 0). JurassiCraft ships a non-zero default (4). Note the two terms: a small default cap guards servers from runaway breeding, at the price of surprising players.

### 6.3 Pregnancy, clutch and birth

**In game.** After mating, the female shows no change for 8 minutes. Then she plays a "laying egg" or "giving birth" animation, and the young appear around her. Egg-layers drop egg entities. Live-bearers spawn infants. Both parents rest for the species cooldown, counted from the moment of mating (for example 1.4 Minecraft days for a raptor).

**Mechanism.** `[v]` `gyovxqy/kodkieb.java:100-227`.

1. At mating, the female rolls a clutch size uniformly from `min_clutch` to `max_clutch`. For each egg she builds a complete offspring record at that moment (section 6.4). She stores the list.
2. Pregnancy lasts 9,600 ticks (8 minutes). It resets if she stops being female or she dies.
3. When the timer ends, she plays the delivery clip. If the species has no such clip, she delivers at once. Delivery lasts half the clip's length.
4. The "nest" position is remembered for 6,000 ticks (5 minutes).
5. At delivery, each record spawns one egg or one infant within half a block of her.
6. Both parents get `breed_cooldown` ticks at the moment of mating, not at delivery. Species values run 14,400 ticks (0.6 days) for small fish to 152,400 (6.35 days) for the Ultimasaurus, with a median of about 2.4 days. `[v]` species scan.
7. The clutch list, timers and nest save to the world. A reload resumes the pregnancy. `[v]` `gyovxqy/kodkieb.java:229-257`.
8. The Birthing Wand (creative) ends the pregnancy at once. The Breeding Wand pairs two animals of opposite sex: right-click one, then the other. They walk together and mate on contact whatever their age, health or cooldown. `[doc]` `kfollsxb/kijk.java:191`; `[v]` `bugratnc/gjtxsudo.java`.

Clutch ranges. Species values run from 1 to 18 eggs. The raptor lays 1 to 7. The tyrannosaurus lays 2 to 4. The dodo lays 1. Only three species can lay 12 or more (alvarezsaurus, megapiranha, mononykus). `[v]` roster scan.

**Platform overlap.** A saved list of records needs a list codec for the offspring record. The record itself is one `RecordCodecBuilder` with the DNA fields.

**Verdict.** Adopt the "roll every young at conception, save the list on the mother" pattern for B18. It makes each egg independent (Nublar's acceptance item: two eggs from one genome differ) and it removes any hatch-time work. Nublar's plan says the lab-egg variation rolls at hatch. That rule is for lab clones. For bred young the pure heredity function of B18 can run at laying.

### 6.4 How a child's genome is built

**In game.** A child looks and fights like a blend of its parents, and one child in three has a stat that jumps away from both.

**Mechanism.** `[v]` `aprffirmi/aeygzxj.java:26-38`, `etemhnv/ummp.java:20-43`, `etemhnv/aeygzxj.java:115-154`. The record is built from the mother and the father:

| Part | Rule |
| --- | --- |
| Species | the mother's species |
| Sex | 50/50 |
| Genome string | each of the 10 positions comes from the mother or the father, one coin flip each |
| Quality | mother's plus father's, capped at 100 |
| Scale | a uniform random value between the two parents' scales. One time in three, it is replaced by a fresh roll in 0.8 to 1.2 |
| Damage | uniform between the parents' damage values. One time in three, replaced by a fresh roll (0.8 to 1.2) times f, where f = (2 + the child's scale) / 3 |
| Health | same rule as damage |
| Speed | uniform between the parents' speeds. One time in three, replaced by a fresh roll (0.8 to 1.2) divided by f |
| Colours | each of the four slots independently takes the mother's or the father's value. If only one parent has the slot, the child takes that one. If neither has it, the slot stays empty |
| Lineage | mother id, father id |
| Age | 0 (set at spawn) |

Mutation. The four trait re-rolls are the only mutation: each fires with probability one in three, independently. There is a colour mutation hook (a re-roll from the species palettes), but its trigger function always returns false, so colour mutation never fires in 3.0.14. `[v]` bytecode and decompile of `etemhnv/aeygzxj.java:146-148`. There is no rare mutation, no albino and no melanistic form.

Effect on variance. A "random value between two parents" shrinks the spread every generation (the average of two values is closer to the mean). The one-in-three re-roll puts the spread back. `[inf]` Without the re-roll, a closed line would converge to one identical animal.

Every parent that has no traits yet rolls them first. So a wild pair always breeds from real numbers. `[v]` `aprffirmi/aeygzxj.java:37` (calls the lazy getter).

**Platform overlap.** `RandomSource` only.

**Verdict.** Adapt for B18 and B07. Nublar's heredity rule (decided 2026-10-06) is the midpoint of the parents plus a per-clone variation, with choice genes taken from one parent. That rule also shrinks variance in a closed line, unless the per-clone variation is large enough. Keep the lead's rule. Add one safeguard to the B18 acceptance test: after 10 generations in a closed pair line, the spread of each number gene must stay above a floor. The JurassiCraft one-in-three re-roll is one way to meet it.

### 6.5 Eggs in the world

**In game.** A laid egg sits where the female dropped it. After 4 to 5 minutes an infant hatches. A player can sneak and right-click the egg to pick it up as a normal egg item. Any creature that hits the egg destroys it. Fire, lava, falling and other non-creature damage do nothing.

**Mechanism.** `[v]` `aprffirmi/dnukfuguy.java:28-185`.

- The egg is an entity. It stores one offspring record. Its hatch time is 5,000 plus 0 to 1,000 ticks, whatever the species `incubation_time`. The species value is used only in the Incubator.
- It has its own mild gravity (0.035 per tick) and drag (0.85), and no vanilla gravity. It is pushable, pickable and collidable.
- It saves `HatchTime` and `Offspring`.
- Picking it up makes a dinosaur-egg item that carries species, quality, string, traits and colours. It loses lineage and sex. The player can then incubate the item and choose sex by temperature.
- The hatching animal gets `kswtc`: it plays its own call sound once.
- The render distance is 32 blocks (1,024 squared).

**Platform overlap.** A plain `Entity` with `SynchedEntityData` and `ValueInput`/`ValueOutput` save methods. The egg uses `hurtServer`.

**Verdict.** Adapt for B11 and B18. Nublar's decision D05 picks between an egg block and an egg entity. This is an entity-with-record example. It costs a ticking entity per egg, 6,000 ticks at most, and eggs rarely pile up when the pack cap stands. A block with a stored record and a scheduled tick (the Nublar default) costs less. Take only the pick-up rule, which keeps the genome.

### 6.6 What breeding does not do

- No cross-species breeding. The species filter needs equal ids.
- No inbreeding penalty. Lineage is read only for the "own young nearby" gate.
- No effect from food, hunger, light, a nest block or a player item. The pack cap and the cooldown are the limits.
- No fertility or gestation stat. The numbers come from species data only.
- No gene or trait is lost. Quality and traits cannot fall below the parents' range, except by the re-roll.

---

## 7. Sex

**In game.** Every animal is male or female. In five species the female has her own adult size. Textures and body parts can differ by sex. Only a female gets pregnant. The tooltip and the Breeding Wand show sex.

**Mechanism.** `[v]` `ctygd/rtwzimpls.java`, `ysjrlvxg/dbgouk.java:68-82, 121-135`, `stta/jgdp.java`.

- Sex is one synched boolean. Bred young roll 50/50. Lab animals take the egg item's sex, which comes from the temperature slider. The creative spawn egg cycles random, male, female by sneak-right-click.
- Five species give the female her own adult model scale. She is larger in four: tyrannosaurus (2.4 versus 2.1 for the male), tarbosaurus (2.2 versus 1.9), teratophoneus (1.2 versus 0.9) and moa (0.95 versus 0.55). She is smaller in giganotosaurus (2.8 versus 3.0). Only tyrannosaurus and moa also give the female her own adult hitbox width and height (3.75 x 4.05 and 1.35 x 2.55). `[v]` roster scan, `ysjrlvxg/dbgouk.java:205-224, 226-236`.
- Textures are per sex. The layer set has `male_parts` and `female_parts` layers that draw only on that sex. `[v]` `stta/jgdp.java:20-21`.
- Eggs of one species carry a male colour pair and a female colour pair (a primary and a secondary int each) in the species file. `[v]` `reproduction.male_egg`, `reproduction.female_egg`. I did not trace where the egg render reads them. `[inf]` they tint the egg by sex.

**Platform overlap.** `EntityDimensions.scalable(...).withEyeHeight(...)` builds the sex-specific box.

**Verdict.** Adapt. A per-sex size and texture set is data, so it costs little. Nublar's B00 already has `gender_size_weights`. Take the idea of `female_adult` size as optional data.

---

## 8. Growth stages and timing

**In game.** A hatchling is small and weak. It grows through four stages. The raptor takes 11.25 Minecraft days (3.75 real hours) to full size. A Growth Serum brings an animal to adult size at once. If the world sleeps through the night or the player sets the time, animals keep growing.

**Mechanism.** `[v]` `ctygd/ummp.java:14-128`, `ctygd/omogwgxr.java:362-440`, `ctygd/kodkieb.java`, `ysjrlvxg/dbgouk.java:369-388`.

- Age is an integer. One age unit passes per 8 game ticks. The species field `growth.maximum_age` is in these units. A species-file helper turns days into units as days times 24,000 divided by 8, so one day is 3,000 units. `[v]` `ysjrlvxg/dbgouk.java:373-375`.
- With growth on, the animal does not count ticks. It stores a birth stamp: the world clock at birth minus 8 times its age. Age is (clock minus stamp) divided by 8. The clock is the game time plus a saved "skipped time" counter. `[v]` `ctygd/omogwgxr.java:362-385`.
- The skipped counter lives in the overworld's saved data (`growth_clock`). A hook adds the gap whenever the game time jumps (sleeping, `/time set`). Animals in unloaded chunks also keep ageing, because the clock moves while they are away. `[v]` `ctygd/ummp.java:94-127`. `[inf]` the hook fires from a level tick event I did not read.
- With growth off (config `creatureGrowth`), age is a plain frozen integer.
- Growth fraction = age divided by `maximum_age`, clamped 0 to 1. Stage thresholds: infant up to 0.25, juvenile up to 0.5, adolescent up to 0.75, adult above 0.75. `[v]` `ctygd/kodkieb.java`.
- Values between baby and adult (health, speed, strength, width, height, eye height, model scale) interpolate between the two species numbers using the growth fraction passed through a curve: linear, ease-in (square) or ease-out. All 129 species use linear. `[v]` `ysjrlvxg/ummp.java`, roster scan.
- Because the stages only gate behaviour, a "juvenile" is partway through the interpolation. At 0.75 an animal is "adult" and still grows to 1.0.
- A refresh runs on change. Age sets a "dirty" flag, and the next tick re-applies attributes, size and eye height once. There is no per-tick stat work. `[v]` `ctygd/omogwgxr.java:410-440`.
- Eating and metabolism link to species stats: the energy and water pools hold adult health squared times 7.5, and energy burns adult health times 75 divided by 24,000 per tick (water burns twice as fast). A raptor (adult health 35) holds about 9,190 and burns about 0.11 per tick, so it empties in about 3.5 Minecraft days. `[inf]`. A creature below 20 percent energy or water takes 5 percent max health every 2 seconds. `[v]` `gyovxqy/gjtxsudo.java:40-52, 121-154`. The config `creatureFoodRequirement` scales the burn.
- Growth Serum. A right-click sets age to the species maximum, hands back an empty syringe and plays a bottle sound. A config switch turns it off. It does nothing on carcasses or on full-grown animals. `[v]` `bugratnc/kdhoqn.java`. Recipe: golden carrot, empty syringe, water bucket and any cooked meat. `[v]` `recipe/growth_serum.json`.

Growth durations across the roster: shortest pistosaurus at 4.75 days. Median 11.25 days. The largest tylosaurus at 45.75, mosasaurus 42.5, ultimasaurus 43, indominus 42.25, and the leedsichthys at 75 days. `[v]` roster scan.

**Platform overlap.** `SavedData` with a `SavedDataType` and a codec is the 26.x pattern. Entity data accessors carry the stamp.

**Verdict.** Adopt for B16: a world clock that counts skipped time, a birth stamp instead of a per-tick counter, and a dirty flag for the stat refresh. This costs no per-tick work and survives sleeping and `/time set`. It also handles unloaded chunks. Adapt the unit: JurassiCraft's eighth-tick unit is arbitrary. Nublar's `growth_days` can be stored as game-time ticks directly.

---

## 9. How colour and pattern genes become looks

**In game.** A dinosaur is drawn as a stack of texture layers. The grey layers take the animal's colour genes as tints. Eyes glow. Male and female parts show on the right sex. Infant, juvenile, adolescent and adult use different models.

**Mechanism.** `[v]` `stta/jgdp.java:6-30`, `stta/aeygzxj.java:5-33`, `stta/dbgouk.java:15-60`, `etemhnv/aeygzxj.java:156-163`.

- The layer set is: underside, primary pattern, secondary pattern, pattern, pattern outline, stripes, markings, frill, quills, crest, fins, cheeks, tail scales, claws, mouth, nostrils, teeth, eyes, eyelids, male parts and female parts. The first three layers match colour slots one to one. The body layer uses the base-body slot.
- Each layer file is named by species, sex and suffix: `textures/entity/<species>/<species>_<sex>_<suffix>.png`.
- A layer can carry a tint spec: one colour slot plus an optional hue, saturation and lightness offset. The offset moves the slot colour (for example a darker outline from the body colour). If the slot is empty, a layer with a tint spec is skipped. A layer without one draws in its own colour.
- The tint is the slot's HSL colour turned to RGB, then each channel multiplied by 255 over 195 and clamped. This lifts the colour about 31 percent. The grey textures are multiplied by the tint, which darkens, so the lift keeps the animal bright. `[v]` `etemhnv/aeygzxj.java:156-163` (constant 195).
- Eyes draw emissive. A layer can be set to show always, only while the eyes are open, or only while they are closed, and for one sex only. `[v]` `stta/jgdp.java:18-21`, `stta/onzkovhp.java`.
- Per-stage models: infant, juvenile, adolescent and adult each have a model layer.
- The client render state holds the four colour ints, so every client draws the same tint. `[v]` `stta/kijk.java`.
- The mod has a fast-path that writes vertices by direct memory stores through `sun.misc.Unsafe`, on by default and switchable in the client config. `[v]` `stta/stta.java`, `dnukfuguy.java` config `fastVertexWrites`. It claims large parks draw several times faster.

**Platform overlap.** `RenderLayer` per layer, `RenderTypes.entityCutout`/`eyes`/`entityTranslucent` and `submitModel` are the 26.x submit-node path. Sodium and Embeddium rewrite parts of that path.

**Verdict.** Adapt the layer and tint model for B09: grey masks, slot tints, per-layer HSL offsets, and a brightness lift. Avoid the Unsafe vertex path (C1 and P2 risk: it depends on internals that change between versions and on a probe at class load).

---

## 10. Species data

### 10.1 The species file

**In game.** A server owner or pack author adds a species by dropping a JSON file at `data/<namespace>/jurassicraft/species/<name>.json`. A missing file gives a fallback "unknown" species and one error line per species. `[v]` `ysjrlvxg/kijk.java:25-26, 42-56, 102-117`.

**Mechanism.** The record has these blocks. `[v]` `ysjrlvxg/dbgouk.java:25-29` and the nested records.

| Block | Fields |
| --- | --- |
| `classification` | `diet` (carnivore, herbivore, insectivore), `temperament` (aggressive, neutral, passive, scared), `sleep_time` (diurnal, nocturnal, crepuscular, no_sleep), `period` (13 periods), `marine`, `flying`, `mammal`, `movement_type` (near_surface, deep_water), `filter_feeder` |
| `attributes` | `baby_health`, `adult_health`, `baby_speed`, `adult_speed`, `baby_strength`, `adult_strength`, `attack_speed` (default 1.0), `follow_range` (default 64) |
| `size` | `baby_width`, `baby_height`, `adult_width`, `adult_height`, `baby_eye_height`, `adult_eye_height`, `scale` {`infant`, `adult`, optional `female_adult`}, optional `female_adult` {width, height}, six hitbox lists (standing, carcass, sleep, drinking, eating, resting) of named boxes with offset, width, height and optional pivot, `pose_blend_ticks` |
| `inventory_size` | 0 to 54, a multiple of 9 (saddle bag) |
| `growth` | `maximum_age`, `curve` |
| `social` | `max_herd_size` (default 32), `flock_speed` (0.8), `random_flock` (true), `attack_bias` (200), `can_climb`, `jump_height`, `imprintable`, `defend_owner` |
| `reproduction` | `birth_type`, `incubation_time` (1 to 32,767; default 3,600), `min_clutch`, `max_clutch` (1 to 64), `breed_cooldown`, `breed_around_offspring`, `defend_offspring`, `male_egg`/`female_egg` colour pairs |
| `habitat` | `spawn_chance` (0 to 1,000), `biomes` (a list of biome tags) |
| `sounds` | `ambient`, `hurt`, `death`, `attack`, `call`, `roar`, `breathing` and more, each an optional sound id |
| `abilities` | `venom_spit`, `pounce`, `tree_climb`, `shoulder_sit`, `bite_effect` (list of effect, duration, amplifier) |
| `remains` | `bones` (names), `skeleton` (a grid of at most 5 by 5 bone names), `skeleton_poses` |
| `hybrid_of` | a list of species ids |
| `color_families` | slot name to a list of palette names |

Validators in the codec. `[v]` `ysjrlvxg/dbgouk.java`.

- Inventory size is 0 to 54 and a multiple of 9.
- Baby and adult eye height may not exceed the matching body height.
- Minimum clutch may not exceed maximum clutch.
- Skeleton rows must have equal length, at most 5 columns and 5 rows, and every named bone must appear in the species' bone list.
- Attributes may not be negative. Sizes run from 0.01 to 15. Scale from 0.001 to 16.
- A species whose `period` has no fossil band logs a warning at tag update (section 10.4).

Data-only parts that Nublar's B00 lacks: poses with their own hitbox sets and blend times; abilities as data blocks (a pounce has minimum and maximum range, a reach factor, a lift impulse, a cooldown; a venom spit has interval, range, damage, blindness ticks and amplifier, and velocity; a tree climb has search radius, maximum height, climb speed, interval, glide range and descent rate; a shoulder sit has rider count and offsets); and bite effects as lists.

**Platform overlap.** A dynamic registry (`ResourceKey` of a species registry) with a codec and a data-pack registry. This is the NeoForge/vanilla `DataPackRegistryEvent.NewRegistry` route and works on 26.x. The species list cache keeps a weak map from the registry object to filtered key lists. `[v]` `ysjrlvxg/onzkovhp.java:17-18, 56-68`. The mod also keeps a static copy of the `RegistryAccess` taken at `TagsUpdatedEvent`. `[v]` `ysjrlvxg/kijk.java:58-63`. A static copy is fragile (client/server and single-player mixes).

**Verdict.** Adopt for B00: strict codec validators, a loud error that names the file, a fallback species with one log line per missing species, and the per-pose hitbox idea (it feeds B27). Avoid the static registry copy. Pass the registry access in from the level. Add `female_adult` as optional.

### 10.2 What the roster looks like

Counts. `[v]` scan of 129 species files.

| Property | Count |
| --- | --- |
| Diet: carnivore / herbivore | 84 / 45 |
| Temperament: neutral / scared / aggressive / passive | 65 / 30 / 29 / 5 |
| Sleep: diurnal / no_sleep / nocturnal / crepuscular | 113 / 10 / 3 / 3 |
| Birth: egg_laying / live_birth | 94 / 35 |
| Marine (water-bound) | 28 (26 near-surface, 2 deep-water: coelacanth, leedsichthys) |
| Filter feeders | 3 (coelacanth, leedsichthys, mawsonia) |
| Flying or mammal flag set | 0 (the flags exist, no species uses them) |
| Imprintable | 71 |
| Hybrids (`hybrid_of`) | 8 |
| Sounds block present | 118 |
| Periods | cretaceous 62, jurassic 29, triassic 12, none 8, neogene 7, devonian 4, paleogene 3, permian 3, quaternary 1 |
| Inventory sizes | 9 slots 63, 27 slots 31, 54 slots 21, 36 slots 14 |
| Abilities | pounce 10, bite effects 7, venom spit 1 (dilophosaurus), tree climb and shoulder sit 1 (microraptor) |
| Spawn chance | 10 for 51 species, 7 for 26, 5 for 23, 8 for 10, 2 for 5, 1 for 3, 4 for 2, 15 for 8 (baryonyx, carnotaurus, giganotosaurus, majungasaurus, rugops, spinosaurus, suchosaurus, tyrannosaurus), 0 for stegospinus |

Ranges. Adult health runs from 8 (procompsognathus) to 203 (Ultimasaurus), with brachiosaurus at 150 and mosasaurus at 132. Adult strength runs from 0.28 (compsognathus) to 57 (Ultimasaurus), then 47 (mosasaurus and kaikaifilu). Adult movement speed runs from 0.1 (coelacanth) to 0.45 (therizinosaurus and several others). Growth runs 4.75 to 75 days. Incubation runs 700 to 11,200 ticks. Breed cooldown runs 0.6 to 6.35 days. Natural spawning is off by default. `[v]` `dnukfuguy.java:17`.

Data quirks. The alligator gar has `attack_speed` 185 and the dodo has 0. These look like entry errors. `[v]` species files.

Non-default time-of-day species: ceratosaurus, ornitholestes and troodon are nocturnal. Dilophosaurus, leaellynasaura and protoceratops are crepuscular. Wake and sleep times are fixed per type: diurnal wakes at tick 0 and sleeps at 16,000; nocturnal wakes at 12,000 and sleeps at 0; crepuscular wakes at 6,480 and sleeps at 22,480 (shown as -1,520 in the file). `[v]` `ysjrlvxg/jgdp.java:7-26` (hours to ticks as (hour - 6) times 1,000 plus minutes times 16). Ten species never sleep (all the aquatic ones that use `no_sleep`, among others).

### 10.3 Plants

49 plant files, each `{"block": id, optional "prehistoric", optional "tree"}`. 39 are prehistoric non-trees, 5 are prehistoric trees (araucaria, calamites, ginkgo, phoenix, psaronius), and 5 are modern (anthurium, heliconia, palmetto, west indian lilac, western sword fern). `[v]` `raw/data/jurassicraft/jurassicraft/plant/*.json`, `cnxjsqwpw/dnukfuguy.java`. Plant DNA is a species key plus a quality with the same rules as animal DNA, minus string, traits and colours. Aphid-in-amber extraction picks a random prehistoric plant. `[v]` `cnxjsqwpw/gjtxsudo.java`.

### 10.4 Fossil species by depth

**In game.** A fossil found near the surface is young. One found deep is old. The same block position always holds the same species in one world.

**Mechanism.** `[v]` `ysjrlvxg/omogwgxr.java:7-110`, `ysjrlvxg/gjtxsudo.java:17-55`, and `javap` of the band setup.

- 13 periods hold a start and end in million years and a "banded" flag. Banded: quaternary, neogene, paleogene, cretaceous, jurassic, triassic, permian, devonian. Unbanded: carboniferous, silurian, ordovician, cambrian, and `none`.
- The fossil column is 125 blocks (Y 64 down to -60). Each banded period gets a share of blocks proportional to its length in years, at least 1, stacked from the top in order. Rounding leftovers go to the periods with the largest remainders. My estimate of the shares: quaternary 1, neogene 7, paleogene 15, cretaceous 28, jurassic 19, triassic 18, permian 16, devonian 21. `[inf]` computed from the formula.
- At a position, the code takes the period for that Y, then picks a species of that period (non-hybrid) with a random source seeded from the world seed and the 8 by 8 by 8 cell of the position. So one cell holds one species for the whole world. If none match, it tries the other Y values in the same 8-high layer, and then any non-hybrid species. `[v]` `ysjrlvxg/gjtxsudo.java:26-55`.
- Species of unbanded periods can never be found as fossils. The mod logs a warning at tag update that names the period and the count. `[v]` `ysjrlvxg/kijk.java:65-77`.
- A helper maps a species name to a number 0 to 3 by hash. `[v]` `ysjrlvxg/gjtxsudo.java:48-50`. `[inf]` it picks one of four fossil looks; I did not trace the caller.

**Platform overlap.** A deterministic `RandomSource.create(long)` seeded from cell coordinates is plain Java.

**Verdict.** Adapt for B00 and B01. Nublar's `period`, `depth_min` and `depth_max` fields can be derived from period length by this formula, which removes hand-set depth numbers. Take the cell-seeded pick for deterministic fossils. Avoid per-species hand tuning of depth for the first pass.

---

## 11. Roster appendix: all 129 species

Source: one JSON file per species. `[v]` `raw/data/jurassicraft/jurassicraft/species/*.json`. Columns: diet (carn, herb), temperament (aggr, neut, pass, scar), period (Cret, Jur, Tri, Neog, Dev, Pal, Perm, Quat, "-" for none), sleep ("-" diurnal), Sea (surf = near-surface water animal, deep = deep water, "+filter" = filter feeder), health, speed and strength as baby/adult, adult hitbox width x height in blocks, grow-up in Minecraft days, birth type, incubation ticks, clutch range, breed cooldown in days, herd size from the file (a dash means the default), natural spawn chance, special ability. Hybrids: indominus, indoraptor, malusaurus, paradeinonychus, spinoraptor, stegospinus, tyrannops, ultimasaurus.

| Species | Diet | Temper. | Period | Sleep | Sea | HP baby/adult | Speed baby/adult | Strength baby/adult | Adult W x H | Grow-up (MC days) | Birth | Incub. (ticks) | Clutch | Cooldown (days) | Herd | Spawn | Ability |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| achillobator | carn | neut | Cret | - | - | 13/43 | 0.2/0.3 | 1/10 | 1.2x1.9 | 12.5 | egg | 4400 | 1-7 | 1.4 | 8 | 8 | pounce |
| albertosaurus | carn | neut | Cret | - | - | 5/67 | 0.25/0.45 | 3/18 | 2.5x3.4 | 15.0 | egg | 4500 | 2-4 | 3.0 | 6 | 10 | - |
| alligator_gar | carn | neut | Quat | none | surf | 4/10 | 0.15/0.25 | 0.5/4 | 1x1 | 10.0 | live | 2900 | 1-3 | 1.5 | 3 | 10 | - |
| allosaurus | carn | neut | Jur | - | - | 5/60 | 0.25/0.45 | 3/15 | 2.5x3.4 | 15.0 | egg | 4500 | 2-4 | 3.0 | 3 | 10 | - |
| alvarezsaurus | herb | scar | Cret | - | - | 3/17 | 0.25/0.32 | 1/5 | 1x0.9 | 6.8 | egg | 1600 | 2-12 | 4.0 | 7 | 5 | - |
| ankylosaurus | herb | neut | Cret | - | - | 10/90 | 0.3/0.45 | 2/16 | 2.5x2.8 | 13.0 | egg | 5200 | 2-6 | 4.3 | - | 10 | - |
| apatosaurus | herb | neut | Jur | - | - | 20/100 | 0.22/0.25 | 5/20 | 5x5.5 | 25.0 | egg | 8000 | 4-8 | 3.6 | 15 | 10 | - |
| archaeornithomimus | herb | scar | Cret | - | - | 5/30 | 0.2/0.3 | 1/5 | 1.2x2.25 | 8.8 | egg | 3400 | 2-6 | 1.0 | 50 | 7 | pounce |
| arsinoitherium | herb | neut | Pal | - | - | 10/70 | 0.3/0.35 | 2/10 | 1.55x1.7 | 11.2 | live | 4400 | 2-6 | 2.4 | - | 10 | - |
| atrociraptor | carn | neut | Cret | - | - | 13/43 | 0.2/0.3 | 1/11 | 1x1.4 | 14.2 | egg | 4400 | 1-7 | 1.4 | 4 | 8 | pounce |
| attenborosaurus | carn | neut | Jur | - | surf | 7/29 | 0.13/0.22 | 0.5/3 | 1x0.8 | 7.8 | live | 2256 | 1-7 | 2.2 | 7 | 10 | - |
| baryonyx | carn | aggr | Cret | - | - | 10/50 | 0.35/0.42 | 5/20 | 2.1x2.5 | 15.0 | egg | 5500 | 2-4 | 3.0 | 3 | 15 | - |
| brachiosaurus | herb | neut | Jur | - | - | 20/150 | 0.22/0.25 | 5/15 | 7x8 | 21.2 | egg | 6000 | 4-8 | 3.6 | 10 | 10 | - |
| carnotaurus | carn | aggr | Cret | - | - | 10/60 | 0.35/0.42 | 5/25 | 2.1x3.2 | 20.0 | egg | 7000 | 2-4 | 3.0 | 2 | 15 | - |
| ceratosaurus | carn | neut | Cret | noct | - | 5/60 | 0.25/0.45 | 3/15 | 2.5x2.5 | 15.0 | egg | 4500 | 2-4 | 3.0 | 3 | 10 | - |
| chilesaurus | herb | pass | Jur | - | - | 14/30 | 0.15/0.25 | 0.2/0.9 | 1x1.4 | 15.0 | egg | 3200 | 2-6 | 3.5 | 12 | 5 | - |
| cladoselache | carn | neut | Dev | none | surf | 6/20 | 0.18/0.3 | 0.3/0.35 | 1x0.4 | 7.5 | live | 700 | 1-3 | 2.0 | 6 | 10 | - |
| coelacanth | carn | pass | Dev | none | deep+filter | 4/10 | 0.06/0.1 | 0.5/3 | 1x1 | 25.0 | live | 2200 | 1-3 | 4.0 | 1 | 10 | - |
| coelophysis | carn | neut | Tri | - | - | 10/27 | 0.15/0.25 | 0.2/0.9 | 1x1.4 | 11.5 | egg | 2400 | 2-6 | 2.4 | 8 | 5 | bite_effect |
| coelurus | carn | neut | Jur | - | - | 10/16 | 0.17/0.15 | 0.4/0.6 | 0.5x0.9 | 10.8 | egg | 1600 | 2-6 | 2.4 | 5 | 5 | - |
| compsognathus | carn | neut | Tri | - | - | 4/10 | 0.15/0.23 | 0.1/0.28 | 0.3x0.5 | 8.8 | egg | 1800 | 2-6 | 2.4 | 25 | 5 | - |
| corythosaurus | herb | scar | Cret | - | - | 10/55 | 0.35/0.41 | 2/8 | 2.5x3.5 | 11.2 | egg | 3800 | 4-6 | 2.0 | - | 10 | - |
| cristatusaurus | carn | aggr | Cret | - | - | 10/50 | 0.35/0.42 | 5/20 | 2.1x2.5 | 15.0 | egg | 5500 | 2-4 | 3.0 | 5 | 8 | - |
| cryolophosaurus | carn | neut | Jur | - | - | 12/45 | 0.3/0.35 | 0.8/7 | 1.3x2.5 | 15.0 | egg | 2500 | 2-6 | 2.4 | 3 | 5 | - |
| cryptoclidus | carn | neut | Jur | - | surf | 5/22 | 0.13/0.22 | 0.5/2 | 1x0.8 | 6.2 | live | 2256 | 1-3 | 3.8 | 12 | 10 | - |
| deinonychus | carn | neut | Cret | - | - | 10/32 | 0.2/0.4 | 1/7 | 0.9x1.1 | 10.0 | egg | 4000 | 1-7 | 1.4 | 9 | 4 | pounce |
| deinosuchus | carn | aggr | Cret | - | - | 17/116 | 0.15/0.15 | 0.8/3.5 | 2x1.6 | 26.2 | egg | 7200 | 2-6 | 2.4 | 3 | 7 | - |
| dilophosaurus | carn | neut | Jur | crep | - | 10/30 | 0.35/0.4 | 2/6 | 1.2x1.9 | 10.0 | egg | 3500 | 2-4 | 1.2 | 10 | 7 | venom_spit |
| dodo | herb | scar | Neog | - | - | 5/18 | 0.27/0.3 | 1/2 | 0.6x0.8 | 10.0 | egg | 2000 | 1-1 | 2.5 | 30 | 7 | - |
| dracorex | herb | neut | Cret | - | - | 10/35 | 0.2/0.3 | 1/8 | 1x1.6 | 11.2 | egg | 2500 | 1-7 | 1.4 | 3 | 10 | - |
| dryosaurus | herb | scar | Jur | - | - | 8/16 | 0.22/0.32 | 1/2 | 1.2x1.5 | 10.0 | egg | 4000 | 2-6 | 2.4 | 20 | 7 | - |
| dunkleosteus | carn | aggr | Dev | none | surf | 16/60 | 0.09/0.15 | 1/3.8 | 1.2x2.2 | 9.2 | live | 4800 | 1-4 | 0.6 | 2 | 2 | - |
| edmontosaurus | herb | neut | Cret | - | - | 13/78 | 0.35/0.41 | 4/13 | 3.5x4.5 | 17.5 | egg | 4400 | 4-6 | 2.0 | 12 | 10 | - |
| elaphrosaurus | herb | neut | Jur | - | - | 5/30 | 0.2/0.3 | 1/5 | 1.2x2.25 | 8.8 | egg | 3400 | 2-6 | 3.0 | 50 | 7 | bite_effect |
| elasmosaurus | carn | scar | Cret | - | surf | 9/41 | 0.13/0.22 | 0.7/4 | 1.5x1.4 | 15.8 | live | 3587 | 1-4 | 2.8 | 8 | 10 | - |
| eretmorhipis | herb | scar | Tri | - | surf | 8/38 | 0.09/0.15 | 0.5/5 | 0.8x0.5 | 5.8 | live | 3200 | 1-3 | 4.0 | 2 | 10 | - |
| eusthenopteron | carn | neut | Dev | none | surf | 6/20 | 0.09/0.15 | 0.7/2.3 | 1.2x2.2 | 10.0 | live | 1200 | 1-2 | 1.6 | 2 | 10 | - |
| gallimimus | herb | scar | Cret | - | - | 5/30 | 0.2/0.3 | 1/5 | 1.2x2.25 | 8.8 | egg | 3400 | 2-6 | 1.0 | 50 | 7 | pounce |
| gasparinisaura | herb | scar | Cret | - | - | 8/15 | 0.22/0.32 | 0.6/1.7 | 0.6x0.8 | 10.2 | egg | 2800 | 2-6 | 3.1 | 16 | 7 | - |
| giganotosaurus | carn | aggr | Cret | - | - | 10/97 | 0.35/0.42 | 5/33 | 3.6x4.9 | 21.8 | egg | 7000 | 2-4 | 3.0 | 2 | 15 | - |
| guanlong | carn | neut | Jur | - | - | 8/15 | 0.15/0.2 | 0.3/0.5 | 1x1.3 | 9.0 | egg | 2200 | 2-6 | 2.4 | 9 | 5 | - |
| hadrosaurus | herb | scar | Cret | - | - | 10/50 | 0.3/0.38 | 2/8 | 2.5x3 | 12.5 | egg | 3800 | 4-6 | 2.0 | - | 10 | - |
| herrerasaurus | carn | neut | Tri | - | - | 8/30 | 0.3/0.35 | 0.8/7 | 1x2.4 | 11.2 | egg | 2500 | 2-6 | 2.4 | 6 | 5 | - |
| homalocephale | herb | scar | Cret | - | - | 7/17 | 0.22/0.32 | 0.8/1.8 | 0.7x0.8 | 8.8 | egg | 4200 | 2-6 | 2.4 | 25 | 7 | - |
| huayangosaurus | herb | neut | Jur | - | - | 15/60 | 0.15/0.22 | 3/12 | 1.8x1.6 | 12.5 | egg | 6000 | 2-6 | 2.4 | 10 | 10 | - |
| hypsilophodon | herb | scar | Cret | - | - | 8/13 | 0.22/0.32 | 1/1.9 | 0.8x0.8 | 9.5 | egg | 3500 | 2-6 | 2.4 | 23 | 7 | - |
| ichthyosaurus | carn | neut | Jur | - | surf | 8/43 | 0.21/0.35 | 0.5/7 | 1x1 | 12.5 | live | 4200 | 1-3 | 2.0 | 12 | 10 | - |
| iguanodon | herb | scar | Cret | - | - | 10/50 | 0.3/0.38 | 6/18 | 2.5x3 | 12.5 | egg | 3500 | 4-6 | 2.6 | - | 10 | - |
| indominus | carn | aggr | - | - | - | 18/103 | 0.2/0.3 | 3/17 | 3x6 | 42.2 | egg | 8600 | 1-2 | 4.3 | 2 | 8 | - |
| indoraptor | carn | aggr | - | - | - | 12/87 | 0.2/0.3 | 2/15 | 1.5x2 | 21.5 | egg | 6600 | 1-2 | 5.3 | 2 | 8 | - |
| inostrancevia | carn | neut | Perm | - | - | 8/45 | 0.25/0.38 | 3/12 | 1x0.9 | 22.5 | live | 7600 | 1-3 | 4.3 | 2 | 5 | - |
| kaikaifilu | carn | neut | Cret | - | surf | 10/93 | 0.2/0.33 | 0.9/47 | 4x5.6 | 24.5 | live | 2400 | 1-2 | 2.8 | 3 | 2 | - |
| kaprosuchus | carn | aggr | Cret | - | - | 17/92 | 0.22/0.45 | 0.8/3.5 | 2x1.8 | 26.2 | egg | 7200 | 2-6 | 2.4 | 3 | 7 | - |
| kelenken | carn | aggr | Neog | - | - | 9/52 | 0.3/0.35 | 2/12 | 1.2x1.9 | 11.5 | egg | 3900 | 1-5 | 2.5 | 5 | 7 | - |
| kronosaurus | carn | aggr | Cret | - | surf | 12/80 | 0.15/0.25 | 3/20 | 1.5x1.4 | 7.5 | live | 6000 | 1-3 | 4.3 | 3 | 10 | - |
| lambeosaurus | herb | scar | Cret | - | - | 10/55 | 0.35/0.41 | 2/8 | 2.5x3.5 | 11.2 | egg | 3800 | 4-6 | 2.0 | - | 1 | - |
| leaellynasaura | herb | scar | Cret | crep | - | 8/15 | 0.22/0.32 | 1/2 | 0.5x0.65 | 10.0 | egg | 3600 | 2-6 | 2.4 | 15 | 7 | - |
| leedsichthys | carn | pass | Jur | none | deep+filter | 15/120 | 0.12/0.2 | 12/40 | 3x5 | 75.0 | live | 6500 | 1-3 | 4.0 | 1 | 4 | - |
| leptictidium | herb | scar | Pal | - | - | 8/18 | 0.22/0.32 | 1/2 | 0.5x0.8 | 6.2 | live | 1200 | 1-3 | 2.4 | 10 | 2 | - |
| lesothosaurus | herb | scar | Jur | - | - | 8/13 | 0.22/0.32 | 1/1.9 | 0.87x1 | 9.5 | egg | 3900 | 2-6 | 2.4 | 17 | 7 | - |
| liopleurodon | carn | aggr | Jur | - | surf | 8/78 | 0.15/0.25 | 1/17 | 1.5x1.4 | 6.8 | live | 5900 | 1-2 | 3.5 | 3 | 10 | - |
| lycaenops | carn | neut | Perm | - | - | 6/36 | 0.25/0.38 | 2/10 | 1x0.9 | 18.5 | live | 6300 | 1-5 | 4.8 | 6 | 5 | - |
| lystrosaurus | herb | scar | Perm | - | - | 6/16 | 0.25/0.38 | 2/10 | 1x0.9 | 11.0 | live | 3300 | 1-5 | 4.8 | 6 | 5 | - |
| machairasaurus | carn | neut | Cret | - | - | 10/30 | 0.35/0.4 | 2/6 | 1x1.6 | 11.5 | egg | 3000 | 1-5 | 2.5 | 6 | 7 | bite_effect |
| maiasaura | herb | neut | Cret | - | - | 13/78 | 0.35/0.41 | 4/13 | 3x3.7 | 17.5 | egg | 4400 | 4-6 | 2.0 | 12 | 10 | - |
| majungasaurus | carn | aggr | Cret | - | - | 10/60 | 0.35/0.42 | 5/25 | 1.9x2.8 | 15.5 | egg | 6200 | 2-4 | 3.0 | 2 | 15 | - |
| malusaurus | carn | aggr | - | - | - | 18/103 | 0.2/0.3 | 3/17 | 3.4x7 | 39.0 | egg | 4400 | 1-3 | 4.9 | 2 | 8 | - |
| mamenchisaurus | herb | neut | Jur | - | - | 20/100 | 0.22/0.25 | 5/20 | 5x6.5 | 25.0 | egg | 9000 | 4-8 | 3.6 | 15 | 10 | - |
| mawsonia | carn | pass | Cret | none | surf+filter | 4/10 | 0.06/0.1 | 0.5/3 | 3x4 | 19.5 | live | 3200 | 1-4 | 2.5 | 1 | 8 | - |
| megalosaurus | carn | neut | Jur | - | - | 5/60 | 0.25/0.45 | 3/15 | 2.5x3.4 | 15.0 | egg | 4500 | 2-4 | 3.0 | 5 | 10 | - |
| megapiranha | carn | neut | Neog | none | surf | 12/23 | 0.18/0.3 | 0.7/1 | 1x1 | 6.5 | live | 1200 | 5-18 | 1.5 | 25 | 10 | - |
| metriacanthosaurus | carn | neut | Jur | - | - | 3/50 | 0.25/0.42 | 3/15 | 2.5x2.5 | 15.0 | egg | 4500 | 2-4 | 3.0 | 3 | 10 | - |
| microceratus | herb | scar | Cret | - | - | 7/17 | 0.22/0.32 | 0.8/1.8 | 0.6x0.6 | 8.8 | egg | 4200 | 2-6 | 2.4 | 22 | 7 | - |
| microraptor | carn | neut | Cret | - | - | 4/10 | 0.15/0.2 | 0.5/2 | 0.7x0.6 | 7.5 | egg | 2000 | 1-5 | 1.2 | 16 | 7 | pounce,tree_climb,shoulder_sit |
| mixosaurus | carn | scar | Tri | - | surf | 8/36 | 0.19/0.32 | 0.5/3 | 0.8x0.8 | 17.5 | live | 3200 | 1-3 | 2.0 | 12 | 10 | - |
| moa | herb | neut | Neog | - | - | 6/42 | 0.2/0.3 | 1/8 | 1.3x1.7 | 11.2 | egg | 3400 | 2-6 | 1.0 | 3 | 2 | - |
| monolophosaurus | carn | aggr | Jur | - | - | 12/43 | 0.3/0.32 | 0.8/6 | 1x1.8 | 14.5 | egg | 2500 | 2-6 | 2.4 | 4 | 5 | - |
| mononykus | herb | scar | Cret | - | - | 3/17 | 0.25/0.32 | 1/5 | 1x0.9 | 6.8 | egg | 1600 | 2-12 | 4.0 | 7 | 5 | - |
| moros | carn | neut | Cret | - | - | 3/23 | 0.25/0.32 | 2/14 | 1x0.9 | 8.0 | egg | 1900 | 1-3 | 2.8 | 2 | 5 | - |
| mosasaurus | carn | aggr | Cret | - | surf | 15/132 | 0.21/0.35 | 0.9/47 | 4x5.6 | 42.5 | live | 3200 | 1-3 | 2.0 | 2 | 1 | - |
| muraenosaurus | carn | neut | Jur | - | surf | 9/36 | 0.13/0.22 | 0.5/3 | 1.4x1.2 | 6.5 | live | 2900 | 1-5 | 3.1 | 4 | 10 | - |
| mussaurus | herb | scar | Tri | - | - | 2/15 | 0.25/0.32 | 1/2 | 1x0.9 | 10.0 | egg | 3600 | 2-8 | 2.4 | 25 | 5 | - |
| ophthalmosaurus | carn | neut | Jur | - | surf | 12/54 | 0.21/0.35 | 0.5/7 | 1x1 | 11.2 | live | 4200 | 1-4 | 3.0 | 12 | 10 | - |
| ornitholestes | carn | neut | Jur | noct | - | 10/14 | 0.17/0.15 | 0.4/0.6 | 0.5x0.9 | 10.0 | egg | 1500 | 2-6 | 2.4 | 10 | 5 | - |
| ornithomimus | herb | scar | Cret | - | - | 5/28 | 0.2/0.34 | 1/5 | 1x1.9 | 11.5 | egg | 3200 | 2-6 | 1.0 | 27 | 7 | pounce |
| othnielia | herb | scar | Jur | - | - | 6/12 | 0.22/0.32 | 1/1.9 | 0.8x0.8 | 7.2 | egg | 3500 | 2-6 | 2.4 | 28 | 7 | - |
| oviraptor | carn | neut | Cret | - | - | 12/36 | 0.35/0.4 | 5/12 | 0.9x0.9 | 13.2 | egg | 2800 | 1-5 | 2.5 | 7 | 7 | bite_effect |
| pachycephalosaurus | herb | neut | Cret | - | - | 10/35 | 0.2/0.3 | 1/8 | 1x2 | 11.2 | egg | 3500 | 1-7 | 1.4 | 8 | 10 | - |
| paradeinonychus | carn | aggr | - | - | - | 18/89 | 0.23/0.4 | 3/13 | 1.5x2 | 10.5 | egg | 4600 | 1-4 | 3.4 | 5 | 8 | - |
| parasaurolophus | herb | scar | Cret | - | - | 10/55 | 0.35/0.41 | 2/8 | 2.5x3.7 | 11.2 | egg | 3800 | 4-6 | 2.0 | 20 | 10 | - |
| pistosaurus | carn | neut | Tri | - | surf | 6/20 | 0.13/0.22 | 0.5/1 | 0.7x0.5 | 4.8 | live | 1800 | 1-7 | 2.2 | 4 | 10 | - |
| plateosaurus | herb | scar | Tri | - | - | 8/43 | 0.25/0.32 | 1/12 | 2x2.2 | 15.8 | egg | 5200 | 2-8 | 2.4 | 5 | 5 | - |
| plesiosaurus | carn | neut | Jur | - | surf | 6/27 | 0.13/0.22 | 0.5/3 | 1x0.8 | 6.5 | live | 2400 | 1-7 | 2.2 | 4 | 10 | - |
| postosuchus | carn | neut | Tri | - | - | 7/26 | 0.22/0.45 | 0.2/1 | 1x1 | 24.5 | egg | 7200 | 2-6 | 2.4 | 3 | 7 | - |
| proceratosaurus | carn | neut | Jur | - | - | 8/12 | 0.15/0.2 | 0.3/0.5 | 0.5x0.9 | 10.0 | egg | 2000 | 2-6 | 2.4 | 10 | 5 | - |
| procompsognathus | carn | neut | Tri | - | - | 4/8 | 0.15/0.2 | 0.1/0.3 | 0.3x0.6 | 7.5 | egg | 1800 | 2-6 | 2.4 | 20 | 5 | bite_effect |
| protoceratops | herb | scar | Cret | crep | - | 12/37 | 0.12/0.22 | 0.8/1.8 | 1x1 | 8.8 | egg | 4600 | 2-6 | 2.4 | 35 | 7 | - |
| rhizodus | carn | neut | Neog | none | surf | 12/40 | 0.09/0.15 | 0.7/2.3 | 1.2x2.2 | 10.0 | live | 1200 | 1-4 | 0.6 | 6 | 10 | - |
| rugops | carn | aggr | Cret | - | - | 7/46 | 0.35/0.42 | 4/19 | 1.2x2.2 | 15.5 | egg | 5800 | 2-4 | 4.0 | 5 | 15 | - |
| segisaurus | carn | neut | Jur | - | - | 6/10 | 0.2/0.25 | 0.2/0.4 | 0.4x0.8 | 10.0 | egg | 2000 | 2-6 | 2.4 | 15 | 5 | bite_effect |
| shantungosaurus | herb | neut | Cret | - | - | 15/89 | 0.35/0.41 | 4/18 | 3.5x4.5 | 14.5 | egg | 5400 | 4-6 | 2.0 | 10 | 10 | - |
| shastasaurus | carn | scar | Tri | - | surf | 8/36 | 0.17/0.28 | 0.5/3 | 2x0.8 | 17.5 | live | 3200 | 1-3 | 2.0 | 12 | 10 | - |
| spinoraptor | carn | aggr | - | - | - | 10/35 | 0.2/0.3 | 1/8 | 1x1.8 | 11.2 | egg | 5200 | 1-7 | 1.4 | 4 | 10 | pounce |
| spinosaurus | carn | aggr | Cret | - | - | 10/80 | 0.35/0.42 | 5/20 | 4x5.5 | 12.5 | egg | 7000 | 2-4 | 3.0 | 2 | 15 | - |
| staurikosaurus | carn | neut | Tri | - | - | 8/30 | 0.3/0.35 | 0.8/7 | 0.9x2 | 11.2 | egg | 2500 | 2-6 | 2.4 | 6 | 5 | - |
| stegosaurus | herb | neut | Jur | - | - | 15/90 | 0.15/0.22 | 3/12 | 2.5x3 | 12.5 | egg | 6000 | 2-6 | 2.4 | 10 | 10 | - |
| stegospinus | carn | aggr | - | - | - | 18/103 | 0.2/0.3 | 3/16 | 2.5x4 | 5.8 | egg | 11200 | 1-2 | 6.0 | 2 | 0 | - |
| struthiomimus | herb | scar | Cret | - | - | 5/28 | 0.2/0.3 | 1/4 | 1.2x2.25 | 8.8 | egg | 3400 | 1-4 | 2.2 | 50 | 7 | pounce |
| stygimoloch | herb | neut | Cret | - | - | 10/35 | 0.2/0.3 | 1/8 | 0.6x1.4 | 11.2 | egg | 3500 | 1-7 | 1.4 | 8 | 10 | - |
| styracosaurus | herb | neut | Cret | - | - | 9/65 | 0.3/0.33 | 2/9 | 2x2.2 | 16.8 | egg | 3800 | 2-6 | 2.4 | - | 10 | - |
| styxosaurus | carn | scar | Cret | - | surf | 8/38 | 0.13/0.22 | 0.5/3 | 1.5x1.4 | 14.8 | live | 3500 | 1-3 | 2.0 | 12 | 10 | - |
| suchomimus | carn | aggr | Cret | - | - | 10/70 | 0.35/0.42 | 5/18 | 4x5.5 | 17.5 | egg | 6000 | 2-4 | 3.5 | 2 | 8 | - |
| suchosaurus | carn | aggr | Cret | - | - | 10/50 | 0.35/0.42 | 5/20 | 2.1x2.5 | 15.0 | egg | 5500 | 2-4 | 3.0 | 3 | 15 | - |
| tarbosaurus | carn | neut | Cret | - | - | 5/67 | 0.25/0.45 | 3/18 | 2.5x3.4 | 15.0 | egg | 4500 | 2-4 | 3.0 | 6 | 10 | - |
| teratophoneus | carn | neut | Cret | - | - | 5/55 | 0.25/0.45 | 3/14 | 1.5x2 | 10.0 | egg | 3300 | 2-4 | 3.0 | 4 | 10 | - |
| therizinosaurus | carn | neut | Cret | - | - | 5/67 | 0.25/0.45 | 3/18 | 2.5x3.4 | 15.0 | egg | 4500 | 2-4 | 3.0 | 6 | 10 | - |
| thescelosaurus | herb | pass | Cret | - | - | 7/16 | 0.22/0.32 | 0.6/1.5 | 0.8x0.8 | 10.5 | egg | 4300 | 2-8 | 3.4 | 6 | 7 | - |
| titanis | carn | aggr | Neog | - | - | 8/48 | 0.3/0.35 | 2/12 | 1.2x1.9 | 11.5 | egg | 3800 | 1-5 | 2.5 | 5 | 7 | - |
| titanoboa | carn | aggr | Pal | - | - | 17/116 | 0.15/0.25 | 1/10.5 | 2x1.2 | 26.2 | live | 8200 | 1-6 | 4.3 | 2 | 7 | - |
| triceratops | herb | neut | Cret | - | - | 10/70 | 0.3/0.35 | 2/10 | 2.5x2.8 | 11.2 | egg | 4400 | 2-6 | 2.4 | 10 | 10 | - |
| troodon | carn | aggr | Cret | noct | - | 8/43 | 0.2/0.25 | 1/8 | 0.5x0.8 | 7.5 | egg | 5000 | 1-4 | 6.0 | 3 | 5 | bite_effect |
| tylosaurus | carn | aggr | Cret | - | surf | 11/112 | 0.2/0.33 | 0.8/43 | 4x5.6 | 45.8 | live | 3200 | 1-3 | 2.0 | 3 | 1 | - |
| tyrannops | carn | aggr | - | - | - | 6/55 | 0.25/0.38 | 3/15 | 1.5x1.8 | 22.5 | live | 8200 | 1-2 | 5.2 | 2 | 5 | - |
| tyrannosaurus | carn | neut | Cret | - | - | 10/80 | 0.35/0.42 | 5/30 | 3.5x3.8 | 15.0 | egg | 5500 | 2-4 | 3.0 | 3 | 15 | - |
| ultimasaurus | carn | aggr | - | - | - | 23/203 | 0.2/0.3 | 13/57 | 3.4x7 | 43.0 | egg | 9600 | 1-2 | 6.3 | 2 | 8 | - |
| utahraptor | carn | neut | Cret | - | - | 11/45 | 0.2/0.3 | 1/11 | 1.1x1.9 | 13.8 | egg | 4800 | 1-7 | 1.4 | 4 | 10 | - |
| velociraptor | carn | neut | Cret | - | - | 10/35 | 0.2/0.3 | 1/8 | 1x1.8 | 11.2 | egg | 4200 | 1-7 | 1.4 | 4 | 10 | pounce |
| xiphactinus | carn | neut | Neog | none | surf | 12/40 | 0.21/0.35 | 1/3.4 | 1.2x2.2 | 10.0 | live | 1200 | 1-4 | 0.6 | 6 | 10 | - |
| yaguarasaurus | carn | neut | Cret | - | surf | 13/53 | 0.18/0.3 | 0.9/12 | 3.6x2.6 | 5.8 | live | 3200 | 1-6 | 3.4 | 6 | 2 | - |

---

## 12. Project Nublar fit

The plan modules named here are in `docs/project-plan.md`. JurassiCraft's genome is smaller than Nublar's 26-gene design. The useful parts are the data shapes, the chain recipes, the inheritance numbers, the growth clock and the colour system.

| Technique | JurassiCraft source | Plan module | Verdict | Reason |
| --- | --- | --- | --- | --- |
| DNA record = species + capped quality + small fixed record, saved as data components | `etemhnv/gjtxsudo.java` | B06, B07 | Adopt (pattern) | Matches Nublar's drive model (percent per species, cap 100). One codec, one stream codec |
| 10-letter ACGT string | `gyovxqy/aeygzxj.java` | none | Drop | No gameplay reader; costs a field on every item |
| Random quality in steps of 5 at sequencing | `etemhnv/gjtxsudo.java:35-37` | B06 | Defer | Nublar's yield comes from fossil quality and filters (B02, B05). Keep as a fallback for amber if the lead wants a random yield |
| Combine = add quality, cap 100, same species only | `etemhnv/gjtxsudo.java:43-45` | B06 | Adopt (pattern) | Same rule as the drive addition; gives a clean unit test |
| Hard gate: synthesis accepts only full DNA | `ufpurxw/hmocea.java` slot filter | B06, B11 | Adapt | Nublar's gate is threshold-based (clone 50, edit 70). Use the slot-filter idea to stop wasted runs |
| Disc with 3 synthesis charges | `bugratnc/ysulhvcy.java` | B06 | Defer | A balance lever; the lead has not asked for a use limit |
| Raw meat carries the animal's genome, cooked meat only the species | `ctygd/omogwgxr.java:895-915` | B04, B16 | Adopt | Cheap living-DNA source that fits B04's remains items |
| Carcass hit count from body size; meat per hit, bones at the end | `ctygd/omogwgxr.java:837-960` | B16 | Adapt | Compare with the B16 carcass plan; the square-root rule scales work with size |
| Amber and lamprey roll a random species from a cached filtered list | `ysjrlvxg/onzkovhp.java` | B03 | Adapt | A uniform list is a baseline; Nublar's weights (`amber_weight`) refine it |
| One `lab` recipe type: machine id, DNA operation, subject, time, results with weights | `vtrqoo/jgdp.java` | B12, B13 | Adapt | Small JSON; one block-entity class per family. Take the shape, not the eight machines |
| Machine refuses the wrong route (live-bearer vs egg route) | `dnukfuguy/dbgouk.java:513-517` | B11 | Adopt | Clear player message; one lookup |
| Incubator slot temperature = male chance | `dnukfuguy/dbgouk.java:84-87` | B07, B11 | Defer | Nublar has a gender gene (sure at 80). Ask the lead if a hatch-time lever is wanted (open question 1) |
| Hatched egg = sexed, finished DNA item that spawns an infant at age 0 | `bugratnc/omogwgxr.java` | B11, B16 | Adopt | Matches B11 "hatch starts growth at 0" and fixes the adult-spawn defect |
| Imprint on hatch for imprintable species | `bugratnc/omogwgxr.java:51-95` | B19 | Defer | Nublar's D17 default says no owner at hatch; taming stays the bond |
| Cultivator with four nutrient bars and a food data map | `dnukfuguy/dbgouk.java:130-256` | B11, B20 | Defer | Good food-rating idea (`DataMapType`); no plan slot yet |
| Genome Editor: staged preview, apply, natural/random/reset, clone colours | `snqzaxu/kodkieb.java`, `dnukfuguy/jgdp.java` | B08, B09 | Adapt | Fits the server-validated edit packet. Keep the "unapplied" flag and the explicit apply |
| Colour genes: 4 slots, HSL, 46 weighted palette families, muted random roll | `etemhnv/*` | B07, B09 | Adapt | Gives good-looking default variation without art. Add "natural roll from species families" |
| Brightness lift (x255/195) and per-layer HSL offset on tinted grey layers | `etemhnv/aeygzxj.java:156-163`, `stta/aeygzxj.java` | B09 | Adopt (pattern) | Keeps tinted animals bright; cheap |
| Fast vertex writes through `sun.misc.Unsafe` | `stta/stta.java` | B09, P2 | Drop | Depends on JVM internals; a P2 risk |
| Trait record (scale, damage, health, speed) with scale-linked stats | `etemhnv/ummp.java` | B07 | Adapt | Keep "derived stats = species number x multiplier", applied once on change. Use attribute modifiers |
| Child traits: uniform between parents, 1/3 re-roll | `etemhnv/ummp.java:20-43` | B18 | Adapt | The lead chose midpoint + variation. Add a variance-floor test over generations |
| Every young rolled at conception, list saved on the mother | `gyovxqy/kodkieb.java:100-113` | B18 | Adopt | Each egg is independent; no hatch-time work |
| Breed gate: adult, full health, no cooldown, not asleep | `gyovxqy/kodkieb.java:60-66` | B18 | Adapt | Nublar's nest, fertility and stress gate stands; add full health if wanted |
| Own young under half grown nearby blocks breeding (species flag) | `ztiurxyn/fgwme.java:186-188` | B18 | Adopt | Cheap; stops baby floods |
| Pack-size cap within 16 blocks, config default 4, wand bypass | `aprffirmi/gjtxsudo.java` | B18 (D20) | Adapt | D20 default is 0 (no cap). Ship the cap as config; test the default |
| Mate search: 40-79 tick poll, 1 in 20, nearest within 16, max 6 path tests | `ztiurxyn/fgwme.java:36-110` | B18, S1 | Adopt (pattern) | Interval-gated and bounded |
| Breeding Wand and Birthing Wand (creative) | `bugratnc/gjtxsudo.java` | B18 | Adopt | Makes B18 GameTests and playtests short |
| Egg entity with stored record; pick-up keeps the genome | `aprffirmi/dnukfuguy.java` | B11 (D05) | Adapt | Take only the "pick-up keeps the genome" rule; an egg block is cheaper |
| Hybrid = exact multiset of full-quality parent species from species data | `ysjrlvxg/onzkovhp.java`, `vtrqoo/jgdp.java:63-77` | backlog hybrids | Defer | Out of scope for Beta; record the rule |
| World growth clock that counts skipped time, birth stamp, dirty flag | `ctygd/ummp.java`, `ctygd/omogwgxr.java:362-440` | B16 | Adopt | No per-tick growth work; survives sleep and `/time set` |
| Four stages at 25/50/75 percent; adult breeds from 75 | `ctygd/kodkieb.java` | B16, B18 | Adopt | Simple and matches the interpolation |
| Growth Serum sets age to maximum; config switch | `bugratnc/kdhoqn.java` | B13, B16 | Defer | A shortcut item; the lead has not asked |
| Species JSON with strict codec validators and a loud error | `ysjrlvxg/dbgouk.java` | B00 | Adopt | Same goal as B00's "unknown field fails loudly" |
| Fallback species and one error line per missing species | `ysjrlvxg/kijk.java:42-56, 102-117` | B00 | Adopt | Stops crashes from bad packs |
| Static copy of `RegistryAccess` from a tags event | `ysjrlvxg/kijk.java:58-63` | B00, P2 | Drop | Breaks with client/server mixes. Pass the level's registry access |
| Per-pose hitbox sets with blend ticks in species data | `ysjrlvxg/dbgouk.java:92-98` | B27 | Adapt | Data-only poses; compare with the B27 section hitbox plan |
| Abilities as data blocks (pounce, venom spit, tree climb, bite effects) | `ysjrlvxg/dbgouk.java:248-367` | B10, B19 | Defer | Useful shapes for later special acts |
| `remains`: bones + 5x5 skeleton grid + poses in species data | `ysjrlvxg/dbgouk.java:493-560` | B21 | Defer | Exhibit assembly is out of scope for Beta |
| Fossil depth bands from period length; cell-seeded species pick | `ysjrlvxg/omogwgxr.java`, `ysjrlvxg/gjtxsudo.java` | B00, B01 | Adapt | Derive `depth_min/max` from `period` instead of hand numbers; deterministic per 8-block cell |
| Config switches (`naturalSpawning`, `creatureGrowth`, `creatureMetabolism`, `packSizeLimitsBreeding`, `growthSerumEnabled`) | `dnukfuguy.java` (root) | B00 | Adopt (pattern) | Maps to the `features` and `breeding` sections |
| 26.x API names for save/load, saved data, recipes, menus | many | P2 | Adopt (as reference) | A shipping 26.1.2 NeoForge mod that uses them |

---

## 13. Open questions

Only real design gaps.

1. **Does the lead want a hatch-time sex lever?** JurassiCraft gives the player a 0 to 100 percent male slider per egg. Nublar's gender gene is "sure at 80, 30 percent random below". If a lever is wanted, it could replace the random part below 80.
2. **Which inheritance rule keeps variance alive?** The midpoint rule (B18) shrinks spread each generation. Per-clone variation (`editing.variation_percent`) may be enough, or not. Needs a number and a test in the B18 task plan.
3. **Should the breeding pack cap ship non-zero?** JurassiCraft ships 4 within 16 blocks. D20 says 0. A non-zero default protects servers but surprises players.
4. **Do bred young roll their genome at laying or at hatch?** The lead decided hatch for lab clones. For bred young, rolling at laying (JurassiCraft's way) lets a picked-up egg carry a finished genome. Hatch-time rolling keeps the reveal.
5. **Does Nublar want a "natural variant" roll in the editor?** JurassiCraft's editor is colour-only. A roll button from species palette families could ship in B08 as a small extra.

---

## 14. Gaps in this note

- The creature inspector item and the paleo pad screens were not traced. They may read genome fields I did not list.
- The weighted result pick of the recipe class and the period-band setup were read as bytecode or inferred from JEI text, not from decompiled source.
- Where the growth clock hook is called from was not traced.
- The role tags on the 46 palettes have no reader that I found.
- Natural spawning in the world, herd AI and the needs system are outside this note.
