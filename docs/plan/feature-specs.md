# Feature specs

Mirror of the shared page sub-tab "Feature specs" under "Beta and Release 1 project plan". Back to the [roadmap](../project-plan.md).

Each feature in player terms: how it plays, what it looks and sounds like, its numbers and sources, and what is still open. Art status lives only in the Art list.

## How to read this page

**Parity rule.** Cloning is 1:1 with the 2022 Cloning Guide video (https://youtu.be/W9cMQUB4EdM). Where the spec (`_Cloning Process.docx`) or a lead decision (codebase analysis section 11, or the lead on 2026-10-12) differs, the spec or the decision wins. Each feature lists where it differs from the video.

**Machine looks.** The machine models and textures changed since the 2022 video (lead 2026-10-12). The video sets behaviour only: the flow, the timings, the screen contents and what each machine does. The look of each machine and its moving parts come from the current models and textures in the mod (`geo/block/*.geo.json`, `textures/block/*.png`, `textures/gui/*.png`). Each animation drives a bone that the current model has. Where an animation needs a part that the model lacks, the spec says how it adapts to existing parts, or it lists an open question for the artist. Where the cubes exist but share a bone, a developer moves them into a bone of their own, with no art change (lead 2026-10-12). A screen follows the video for its contents (slots, gauges, tabs, buttons, tooltips) and the current GUI texture for its art.

**Source tags.** (video m:ss). (spec). (design doc). (decision, section 11.x) = a lead decision in codebase analysis section 11. (lead 2026-10-12). (original 1.16) = clean-room behaviour of the original mod. (2020 board). (GH #n) = a GitHub issue. (Mod Commission). (plan) = a value already in the engineering plan. (plan default Dnn) = a plan default that the lead has not confirmed. In tables, the Source column drops the brackets.

**Not set and proposals.** "Not set" means that no source gives the value. "Proposal, lead decides" marks a value that this page suggests. Neither is a requirement until the lead confirms it.

**Motion.** Machine motion runs in code on game time, never on the frame rate. Every player sees the same motion.

**Units.** 20 ticks = 1 second. 1 px = 1/16 block.

**Art.** "Art needed" names the things in the Art list. Only the Art list gives art status.

**Today.** "Today" lines describe the current code only. Code never proves that a requirement is met.

**Source tags for M2 to M4.** (design doc) = Project_ Nublar (2).docx. (species card) = Tyrannosaurus.docx or Velociraptor.docx. (2020 board) = Trello board "project-nublar". (art board) = the GitHub asset task board. (Dino VFX) = Dino VFX.txt. (Special Genes) = Special Genes.txt. (donor table) = Vanilla Animal DNA Effects. (decision, section 11.x) = a lead decision in codebase analysis section 11. Section 11 wins over older design text. (lead 2026-10-12). (herd decision n) = the herd decisions of 2026-10-09 and 2026-10-10. (fence Fn) = row n of the 1.12.2 fence parity table in the plan. (original 1.16) = clean-room behaviour of the original mod. (plan) = a value that the engineering plan already sets. (plan default Dnn) = a default that the lead has not confirmed. In tables, the Source column drops the brackets.

## M1. Fossil to egg

M1 gives a survival player the whole lab chain: find fossils and amber, extract and edit DNA, synthesise an embryo, print and incubate an egg, and hatch a baby. All four machines are complete, with building, screens, upgrades, power, animations, sounds, recipes and art.

### F1.1 Fossils and amber

**In one line:** Fossil ore and amber hide in the right rock at the right depth, and each find carries a species, a quality and a DNA label.

**How it plays:** The player learns where each species lies by biome and depth, then digs. The ore shows the bone piece and its quality before the player breaks it. Each break drops one fossil. Amber drops with its species unknown. The tube from the Fossil Processor keeps it hidden. The species shows only when the Sequencer sequences the tube. A fossil pouch collects fossils by itself as the player mines.

**Sources:** 2022 video (0:40, 0:55-1:05), design doc, decision (section 11.1, 11.2), lead 2026-10-12.

**Differs from the video:**

- Terracotta is not a host stone in Beta. Deepslate is (decision, section 11.1). The video names stone, sandstone and terracotta (0:40).
- Amber is in Beta (decision, section 11.2). The video shows no amber.
- Quality tiers, DNA labels and the pouch are new. The video fossil has a name only (1:05).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Items | Fossil ore in stone, sandstone and deepslate. Four forms: single ore, small cluster, skeleton cluster, digsite deposit. One cluster holds one species. A skeleton cluster holds the bones of one animal | decision, section 11.1 |
| Items | The ore shows the bone piece and its quality as an overlay, before the player mines it | design doc (GH #1), decision, section 11.1 |
| Items | A fossil item knows its species, piece, quality and biome. Name style: "Common Tyrannosaurus Skull Fossil". Qualities: fragmented, poor, common, pristine. The lead confirmed these four names | design doc (GH #1), decision, section 11.1, lead 2026-10-12 |
| Items | Special pieces are the skull, the tooth and the claw. They have species art and give more DNA. Other bones use generic art. Special pieces serve DNA and trades only. No recipe uses them | decision, section 11.1, lead 2026-10-12 (D78) |
| Items | Every species has a pelvis piece. The T. rex pelvis has its own art. The other species share one generic pelvis look | lead 2026-10-13 |
| Items | The T. rex has a neck piece. A T. rex skeleton cluster can hold a neck | lead 2026-10-12 |
| Items | Amber ore and an amber item. The item shows "Unknown species". The tube that the processor makes from it also hides the species. Sequencing reveals the species on the drive entry (F1.4). Beta has no Identification Tag | decision, section 11.2, lead 2026-10-13 (Z-8) |
| Items | DNA labels. A test tube shows its DNA % in its tooltip only, not on the icon. DNA states: Unidentified, Identified, Full (just synthesised). Only an amber tube shows the Unidentified state, and it keeps that state. Its DNA becomes Identified only on the drive, when the Sequencer sequences it. A fossil tube and a DNA syringe show their species at once | spec, design doc, lead 2026-10-12, lead 2026-10-13 |
| Numbers | Rarity = ore rarity × period rarity × species share × piece share × quality share. A pristine T. rex skull is about 1 in 200 T. rex ores | design doc (GH #1), decision, section 11.1 |
| Numbers | Quality weight and DNA yield: fragmented 40 / 5%, poor 20 / 7.5%, common 15 / 10%, pristine 4 / 30%. All are config values | decision, section 11.1 (T. rex card) |
| Numbers | A special piece gives 1.5 × the DNA of a bone of the same quality. Config value | lead 2026-10-12 (D01) |
| Numbers | Depth band and biome groups per species: see the table below. A species never generates in a biome group that its row does not list | design doc, decision, section 11.1, lead 2026-10-13 |
| Numbers | No valid species for a depth and biome: the species of the nearest depth band applies. This rule covers amber and fossil ore | lead 2026-10-12 (D02) |
| Numbers | Species share in the rarity formula: an equal share for each species that is valid at that depth and biome | lead 2026-10-13 |
| Numbers | One fossil per break. Fortune gives a chance of one tier higher, never more items. Silk Touch drops the ore block. No jacket step: the broken ore drops the fossil at once | design doc (GH #1), decision, section 11.1, lead 2026-10-12 (D74) |
| Numbers | Fortune tier-up chance: +7.5 % per Fortune level, so Fortune III gives +22.5 %. A config value inside the lead's band of +5 % to +10 % | lead 2026-10-12, lead 2026-10-13 |
| Numbers | A skeleton cluster holds a piece above the card's "quantity max" in about 1 of 20 clusters | decision, section 11.1 |
| Numbers | Amber: random DNA yield from 0 to 80%, no quality. The species rolls at the end of processing, from the species valid at its depth and biome | decision, section 11.2 |
| Effects | The overlay takes the host stone tint at a random strength of 0 to 90%. It has one of four opacity levels (15, 43, 72 or 100%) and a random rotation per block. The four levels refine the earlier "random opacity 15 to 100%" | design doc (Fossil Ore Aesthetics), lead 2026-10-12 (D31) |
| Capacity | Fossil pouch: 27 slots. It picks up fossils by itself from any inventory slot. It merges equal fossils (same species, piece and quality) into one stack | design doc, lead 2026-10-12 |
| Capacity | The pouch also takes amber, by pickup and by hand. The player can put fossils and amber into the pouch by hand. The pouch refuses all other items. Hoppers and the Fossil Processor can pull fossils and amber out of the pouch | lead 2026-10-12 |
| Screen | Pouch screen: a 3 × 9 grid like a vanilla chest. The player takes fossils out and puts fossils in | proposal (layout), lead 2026-10-12 (put in) |
| Sound | Vanilla stone break and item pickup sounds. No source asks for more | none |

**Fossil generation per species (lead 2026-10-13).** A biome cell gives the share of ore of that species in the biome group, in percent. A dash means no ore. All values are config values, and playtests tune them.

| Species | Period and age | Depth | Desert | Badlands | Plains | Forest | Taiga | Hills | Swamp | River | Caves |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Tyrannosaurus | Late Cretaceous, 83.6 to 65 Ma | y 40 to 75 | 75 | 75 | 35 | 15 | 15 | 8 | – | – | – |
| Triceratops | Late Cretaceous, 68 to 65 Ma | y 55 to 75 | 75 | 75 | 35 | 15 | 15 | 8 | – | 15 | 8 |
| Gallimimus | Late Cretaceous, 73 to 68 Ma | y 50 to 72 | 75 | 75 | – | – | – | – | – | 35 | 8 |
| Velociraptor | Late Cretaceous, 75 to 71 Ma | y 45 to 70 | 75 | 75 | 35 | – | – | 15 | 8 | 15 | 8 |
| Brachiosaurus | Late Jurassic, 154 to 150 Ma | y 35 to 55 | 35 | 75 | 35 | 15 | – | 15 | 8 | 15 | 8 |
| Dilophosaurus | Early Jurassic, 201.3 to 182.7 Ma (board #4) | y −20 to 30 | 75 | 75 | – | – | – | – | – | 15 | 8 |

- Period rarity: Cretaceous 2.95, Jurassic 2.8. These are the example values of the periods table.
- The T. rex keeps its approved band, y 40 to 75 (2026-10-05). The other Cretaceous bands spread around it by age: an older species lies deeper.
- The Dilophosaurus is the only Beta fossil in deepslate. From y 30 to 35, no Beta fossil generates.
- Biome groups use vanilla biomes. Biome tags let modded biomes join a group:
  - Desert: desert. Badlands: badlands, eroded badlands, wooded badlands. Plains: plains, sunflower plains.
  - Forest: forest, flower forest, birch forest, old growth birch forest, dark forest, pale garden.
  - Taiga: taiga, old growth pine taiga, old growth spruce taiga.
  - Hills: windswept hills, windswept gravelly hills, windswept forest, meadow, cherry grove, stony peaks.
  - Swamp: swamp, mangrove swamp. River: river. Caves: dripstone caves, lush caves.
- No Beta fossils: jungle, savanna, snowy biomes, ocean and beach, stony shore, mushroom fields, deep dark.
- In the game: deserts and badlands hold the most ore. A player who digs below y 35 finds only the Dilophosaurus.

**Art needed:** see Art list → Fossils and amber (ore overlays, fossil and amber sprites, pelvis), Lab items (fossil pouch, DNA looks).

**Open questions:**

- Settled by the lead (2026-10-13, Z-8): the Dilophosaurus uses the Jurassic period rarity, 2.8, and stays at y −20 to 30 (D254). The lead first called it Triassic. Board #4 dates it to the earliest Jurassic.
- Settled by the lead (2026-10-13, Z-8): Beta has no Identification Tag. Sequencing identifies amber DNA. This supersedes the tag rule of 2026-10-12 below.
- Settled by the lead (2026-10-13): the depth and biome table above, equal species shares and Fortune +7.5 % per level. Also a pelvis piece for every species, and the DNA % in the tooltip only.
- Settled by the lead (2026-10-12): amber goes into the pouch too. The pouch takes fossils and amber and refuses every other item (D145).
- Superseded on 2026-10-13 (Z-8): "the Identification Tag moves an amber tube from Unidentified to Identified" (lead, 2026-10-12). Now sequencing identifies amber DNA. Before that, the tag rule replaced "the processor reveals the species on the output tube" (section 11.2, 2026-10-05).
- Settled by the lead (2026-10-12): the T. rex has a neck piece (contradiction 8).
- Settled by the lead (2026-10-12, follow-up pass):
  - A special piece gives 1.5 × the DNA of a bone of the same quality (D01). No recipe uses special pieces (D78).
  - With no valid species, the nearest depth band applies (D02).
  - The overlay has four opacity levels (D31). A fossil needs no jacket step (D74).

**Today:** No worldgen runs. Fossil blocks exist for two species, drops are broken, and fossils hold 0% DNA. No amber, pouch or DNA labels.

**Engineering:** backlog F1.1 (old modules B01, B02, B03).

---

### F1.2 DNA from animals

**In one line:** A syringe takes DNA or a bird embryo from a vanilla animal.

**How it plays:** The player right-clicks a vanilla animal with an empty syringe. The syringe fills with a random share of that animal's DNA, and the animal takes a small hit. Many draws can kill the animal. A chicken, a parrot or a turtle in love mode gives an Embryo Syringe instead. Its tooltip names the donor. The DNA syringe goes straight into the Sequencer, and the embryo syringe goes into synthesis.

**Sources:** 2022 video (3:49-4:27, 11:22-11:35), spec, decision (section 11.2, 11.5, 11.6, 11.7), lead 2026-10-12.

**Differs from the video:**

- The embryo syringe goes into synthesis, not into the Egg Printer (decision, section 11.7). The video puts it in the printer (11:38).
- A draw hurts the animal a little (decision, section 11.2). The video shows no damage.
- A turtle in love mode also gives an embryo (lead 2026-10-12). The video shows a chicken and a parrot only.
- Some mobs give no usable DNA (decision, section 11.6).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Inputs | An empty syringe, used on a living vanilla animal. Things that are not alive give nothing | spec, video 3:49, GH #35 |
| Outputs | DNA Filled Syringe with a random 0 to 100% of the animal's genome. Tooltip: "DNA Filled Syringe / Contains 64 Red Parrot DNA" | video 4:07, decision, section 11.2 |
| Outputs | Embryo Syringe from a chicken, a parrot or a turtle in love mode. It holds the animal's embryo. One item serves all three donors, and its tooltip names the donor, for example "Embryo Syringe (Turtle)". An embryo draw has no effect on the animal: it stays in love mode and takes no damage | video 11:22-11:35, design doc, decision, section 11.7, lead 2026-10-12 (turtle, D144), lead 2026-10-13 (D146) |
| Items | Variants keep their own entries: each parrot colour, each tropical fish colour, each cat breed. Colour isolation needs the 15 tropical fish colours other than black | decision, section 11.5, 11.6, spec |
| Items | The Embryo Syringe (from a bird or a turtle) and the dinosaur-embryo syringe (F1.6) are two items with their own looks | decision, section 11.7, lead 2026-10-12 (no stand-ins) |
| Items | No Identification Tag in Beta. An amber tube stays Unidentified until the Sequencer sequences it (F1.1, F1.4) | lead 2026-10-13 (Z-8), replaces the 2020 board tag |
| Numbers | Damage per draw: 1 HP by default. No cooldown. Draws can kill the animal. Example: 10 draws kill a 10 HP cow | decision, section 11.2, plan, lead 2026-10-12 |
| Numbers | Allay, strider, tadpole, happy ghast and sulfur cube give no usable DNA. The tube says that the animal is not a donor | decision, section 11.6 |
| Numbers | Villager DNA gives every gene a random value each time it is used | decision, section 11.6 |
| Animation | Vanilla item swing for the player. Vanilla hurt flash on the animal. No source asks for more | none |
| Sound | Syringe draw: no sound of its own. It plays an existing vanilla sound. Animal hurt: vanilla | lead 2026-10-13 (confirmed in Z-8) |
| Failure | A draw from a non-donor or an object gives no DNA | decision, section 11.6, GH #35 |

**Art needed:** see Art list → Lab items (syringe sprites).

**Open questions:**

- Settled by the lead (2026-10-13): one item, the "Embryo Syringe", serves every embryo donor. Its tooltip names the donor (D146). The syringe draw has no sound of its own (confirmed in Z-8).
- Settled by the lead (2026-10-13, Z-8): Beta has no Identification Tag. The tag item, its recipe and the Identified tube recipe leave the plan (D83).
- Settled by the lead (2026-10-12, follow-up pass): an embryo draw has no effect. The animal stays in love mode and takes no damage (D144).

**Today:** A syringe on a mob gives a random 0 to 100%, and the embryo draw works. No non-donor rule.

**Engineering:** backlog F1.2 (old module B04, vanilla animals).

---

### F1.3 Fossil Processor

**In one line:** The first machine. It turns a fossil or an amber into one DNA test tube, with sand and gravel as by-products.

**How it plays:** The player builds the processor from four parts. The player fills its tank with water, puts in an empty test tube and a filter, and feeds one fossil. After 4 minutes a tube comes out with the species and the DNA %. Better filters give more DNA, and better chips cut the time. Hoppers can run it with no player.

**Sources:** 2022 video (0:23-2:16), spec, decision (section 11.2, 11.4), lead 2026-10-12.

**Differs from the video:**

- The diamond tank holds 8 buckets, not 4 (spec). The video shows 4000 mB (1:55).
- One run gives one tube, and the result keeps decimals, for example 1.25% (decision, section 11.4). The video shows whole "genetic data" counts of 2 and 5 (1:19-1:40).
- By-products fill the 3 × 3 grid (decision, section 11.4). The video shows only a tube.
- It accepts amber, works with hoppers and pipes, and needs power (decision, section 11.2, 11.4, 11.8).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Building | Base, body, lid, tanks, in that order. The lid and the tanks can face either way | video 0:23-0:38 |
| Building | Look: the current model (`processor.geo.json`, texture `processor.png`). Each build step shows its placed part, so the base, drum and lid cap need bones of their own. Today their cubes sit in one root bone `FossilProcessor`. A developer moves them into their own bones, with no art change | lead 2026-10-12 |
| Screen | Art: the current `gui/processor.png`. A standard 9 × 4 player inventory. A status bar runs down the left outside edge (F1.10) | lead 2026-10-12, video 1:07 |
| Screen | Left column, top to bottom: filter slot (grey mesh-disc outline when empty), bucket slot (grey bucket outline), tall slim water gauge with tick marks and animated blue water | video 1:07-1:31 |
| Screen | Top centre: fossil slot. Under it a down arrow that fills white with progress. Under the arrow, left of centre: 3 × 3 output grid. Right column, level with the filter: empty test tube slot (grey tube outline) | video 1:07-1:26 |
| Screen | Tooltips: arrow "3m 53s (3%)". Gauge "500mB / 2000mB". The filter icon shows a green wear bar | video 1:20, 1:23, 1:41 |
| Inputs | One fossil or one amber at a time. An empty test tube. Water by bucket (the bucket comes back) or by pipe. A filter. Hoppers and pipes can feed all of them | video 0:57, design doc, decision, section 11.2, 11.4 |
| Outputs | One DNA tube per run. Extraction never fails. The tube names the species and shows the DNA % with decimals | decision, section 11.4, spec |
| Outputs | By-products in the 3 × 3 grid: about 70 % sand or gravel, 25 % iron or gold nuggets and 1 % diamond. That is about 1 diamond in 100 runs. Config values | design doc, decision, section 11.4 |
| Capacity | Water tank: 2 buckets. Iron tank 3, gold tank 4, diamond tank 8 | spec |
| Timing | 4 minutes per run. Iron chip 3 min, gold chip 2 min, diamond chip 1 min | video 2:00-2:11, spec |
| Numbers | DNA = fossil yield × filter efficiency. Example: a pristine fossil (30%) with a new iron filter gives 7.5% | decision, section 11.1, spec |
| Numbers | Water per run: a config value. The task plan proposes the start value, and playtests set it | lead 2026-10-12 |
| Upgrades | Filters: iron 25%, gold 50%, diamond 100% efficiency. A filter wears down to 25% of its base. Durability: iron 150, gold 250, diamond 500 runs, as config values | spec, video 1:01-1:06, original 1.16, lead 2026-10-12 |
| Upgrades | Tank tiers change the model. Each tier shows its own bone: `base_tanks` (default), `iron_tanks`, `gold_tanks`, `diamond_tanks` | video 0:36, 1:44, 1:55, spec, lead 2026-10-12 |
| Upgrades | Chips: the 3 shared chips (iron, gold, diamond, F1.10). Each chip tier changes the look by a texture swap on an existing bone. No new part (D85) | decision, section 11.8, lead 2026-10-12 |
| Power | Uses power only while it runs: 20 FE per tick at base tier. Iron chip 28, gold 44, diamond 92 FE per tick (F1.11) | decision, section 11.8, lead 2026-10-13 |
| Animation | None. The video and the original show no Processor animation. No part of the model moves (D95, answered) | video 0:36-0:50, 1:45-2:15, original 1.16, lead 2026-10-12 |
| Sound | Run loop and run finished | 2020 board (Sounds card: Active, Finished) |
| Sound | Start and fault sounds. Each sound uses the Processor's own asset where one exists, and a generic lab sound for the rest. The artist also makes sounds of its own for each machine | lead 2026-10-12 (asker's reading) |
| Failure | With no power, water, tube or filter, or with a full grid, it stops and shows the reason. Fault smoke and dark model (F1.10) | decision, section 11.8, plan |

**Art needed:** see Art list → Fossil Processor (models, tank looks, chip texture swap, sounds, screen), Lab items (machine part sprites, filters, chips, tank upgrades).

**Open questions:**

- Settled by the parity rule, confirm: 4 minutes per fossil (video and spec), not "25 min to process 1 stack" (design doc, contradiction 9). Section 11.4 also feeds one fossil at a time. Example: 16 fossils take 64 minutes at base tier.
- Settled by the spec over the video and the design doc: tanks 2, 3, 4, 8 buckets (contradiction 10).
- Settled by the lead (2026-10-12, follow-up pass): no netherite Processor tank in Beta (D143).
- Settled by the lead (2026-10-13): by-products are about 70 % sand or gravel, 25 % nuggets and 1 % diamond.
- Chip look: which existing bone takes the texture swap for each chip tier? The artist proposes it, and the lead approves it (D85).

**Today:** Runs on a 4-minute timer with a filter and a 2000 mB tank. It rejects amber, rounds the result, has no by-products or hopper support, draws no real power and does not animate.

**Engineering:** backlog F1.3 (old module B05).

---

### F1.4 Sequencer: sequencing and drives

**In one line:** The player reads DNA samples onto a hard drive and watches each species fill toward 100%.

**How it plays:** The player builds the Sequencer from four parts and can dye it. The player inserts a Hard Drive or an SSD, then feeds tubes or DNA syringes. Each sample adds its DNA % to that species on the drive. The empty tube or syringe comes back. The Isolated view shows which animals each gene still needs. The drive keeps the data and can move to another sequencer or another player.

**Sources:** 2022 video (2:16-3:30, 3:49-4:50, 7:48-8:50), spec, decision (section 11.5), original 1.16 (door), lead 2026-10-12.

**Differs from the video:**

- The info card shows the real time period and diet. The video shows a placeholder ("Diet: Apple"), and the narrator says it is wrong (3:11-3:20).
- Variants and the biome share are new (decision, section 11.5).
- Every black section takes dye (spec). The video tints only two parts (2:35-2:44).
- The door opens with the screen (original 1.16). The video never shows the model with the screen open.
- The run uses up a sample's DNA, and the empty tube or syringe comes back for reuse (lead 2026-10-12). Neither the video nor the design doc gives this rule.
- The monitor glows while the Sequencer works (lead 2026-10-12). The original had no glow.

| Aspect | Requirement | Source |
| --- | --- | --- |
| Building | Base, door, computer, monitor. The door and the computer go in either order. The monitor goes after the computer. The original 1.16 build had no monitor item (3 parts). The lead kept the monitor on 2026-10-13 | video 2:17-2:33, lead 2026-10-13 |
| Building | Look: the current model (`sequencer.geo.json`, texture `sequencer.png`). Bones `Sequencer` (base), `Door`, `computer`, `monitor_primary`, `mainBody`. Behind the door the model has a tube tray, 3 system racks and 4 chip cubes | lead 2026-10-12 |
| Building | Dye: right-click with any of the 16 vanilla dyes. Every black section takes the colour | spec, video 2:35-2:44 |
| Screen | Art: the current `gui/sequencer.png` (frame and tab shapes) and `gui/center_pieces.png` (ring arcs and "PN" logo). Contents for all three tabs: the "PN" logo with ring arcs in the middle. White Minecraft font | lead 2026-10-12, video 2:45 |
| Screen | Status bar on the left outside edge, with a tick at its top while a process runs. Three tabs at the top: Sequence, Edit, Synthesis. The chosen tab is brighter | video 2:45-3:00 |
| Screen | Sequence tab: a centred "Regular / Isolated" toggle under the tabs. Drive slot top left. Large list centre left with rows "Tyrannosaurus: 100%". The hovered row is lighter and a row is clickable | video 2:45-3:12 |
| Screen | Right side: a turning 3D model of the hovered animal. Below it, text: name, time period and diet | video 3:11-3:15 |
| Screen | Bottom row: input slot left (with count), a double-helix track, leftover slot right (with count). The helix fills from left to right with progress. Tooltip "9s (13%)" | video 3:00-3:10 |
| Screen | Clicking a slot opens a popup with a title and one slot: "Insert Hard Drive", "Insert Genetic Material", "Collect Leftover Item". The main screen dims | video 2:48-3:05, 5:45 |
| Screen | Isolated view: left list of genes with % ("Immunity: 50%"). Right list of the animals that the selected gene needs, with % ("Pig: 100%", "Turtle: 0%"). Both lists scroll | video 7:48-8:50 |
| Inputs | Drive: Hard Drive or SSD. Samples: genetic material tubes and DNA syringes. The run uses up the sample's DNA. The empty tube or syringe returns to the leftover slot, and the player can use it again | video 2:48-4:27, lead 2026-10-12 |
| Timing | Hard Drive 10 s per sample. SSD 5 s per sample | video 2:54-3:00, spec |
| Numbers | Each sample adds its DNA % to its species entry, up to 100%. A second sample never resets progress. The drive records each biome's share of a species. Variants keep their own entries | video 3:16-3:25, decision, section 11.5, GH #34 |
| Behaviour | An Unidentified amber tube adds its DNA to the entry of its hidden species. The drive entry names the species. Sequencing is the only step that identifies amber DNA | lead 2026-10-13 (Z-8, D83) |
| Numbers | Info card for a vanilla animal: the time period "Modern" and the real diet of the animal. Each vanilla animal has a data entry for its diet | lead 2026-10-13 |
| Capacity | No limit on a drive. The data lives on the drive item, so players can swap drives | spec, decision, section 11.5 |
| Animation | Door: bone `Door` swings 90 degrees about Y on its hinge (pivot [23.5, 13.5, -2], a vertical door edge) while any player has the screen open, and closes when the last player leaves. Each swing takes half a second. The open door shows the racks and chips inside | original 1.16, lead 2026-10-12 |
| Animation | Screen: the arc rings turn slowly, one turn per 20 s. The helix grows with progress | video 2:45-3:10, lead 2026-10-12 |
| Light | Monitor glow: a lit texture on bone `monitor_primary` while the Sequencer works | lead 2026-10-12 |
| Power | Uses power only while it sequences: 20 FE per tick with a Hard Drive, 42 with an SSD (F1.11) | decision, section 11.8, lead 2026-10-13 |
| Sound | Door open and close, run loop, sample done. Each sound uses the Sequencer's own asset where one exists (the run loop), and a generic lab sound for the rest. The artist also makes sounds of its own for each machine | proposal (events), lead 2026-10-12 (asker's reading) |
| Failure | With no power it stops and shows the reason (F1.10) | decision, section 11.8 |

**Art needed:** see Art list → Sequencer (models, dye mask, lit monitor, door animation, sounds, screen frame, Sequence tab, info card), Screens (GUI) (pop-up slot dialog, status bar), Lab items (Hard Drive, SSD).

**Open questions:**

- Settled by the lead (2026-10-13): the card of a vanilla animal shows "Modern" and its real diet.
- Settled by the lead (2026-10-13, Z-8): sequencing identifies amber DNA. Beta has no Identification Tag.
- Settled by the lead (2026-10-12, follow-up pass): the Ender SSD is not in Beta (D142). Tests use drives with 1,000 entries. Players have no entry limit (D32).

**Today:** Sequencing works and writes to the drive. Variants and the biome share are missing. Colour completion stays at 1/16. The card shows "Cretaceous / Apple". No dye. The door shows or hides but never opens.

**Engineering:** backlog F1.4 (old module B06).

---

### F1.5 Sequencer: gene editing

**In one line:** The player designs the dinosaur: base species, donor animals, gender, colours and isolated genes, and sees the risk before synthesis.

**How it plays:** At 50% the player can clone, pick donor animals and move their sliders. At 70% the player can edit genes, and at 80% the chosen gender is sure. Attack and defense edits need 90%. A new drive has 2 edit slots. Each 3 clones hatched from it add one more. A turning preview shows size and colour change at once. Isolated genes get a slider or a list in the Advanced view. A risk panel shows the chance of incubation failure and the stat trade-offs before the player commits.

**Sources:** 2022 video (4:50-7:10, 8:45-10:25), spec, decision (section 11.6), design doc (edit gates), lead 2026-10-12.

**Differs from the video:**

- Editing needs 70% (decision, section 11.6). The video edits at 50%.
- At most 3 donors, opening at 50, 75 and 100% (decision, section 11.6). The video shows 3, 5 and 9 rows at 50, 55 and 100% (4:53-5:44).
- Below 80%, gender has a 30% chance of a random result (decision, section 11.6). The video says it is always random (6:55).
- Colour has two modes, and each layer has saturation, opacity and tint strength (decision, section 11.6). The video has hue and lightness only (9:05).
- The risk panel is new (decision, section 11.6).
- Attack and defense edits need 90%, and clones hatched unlock more edit slots (design doc, lead 2026-10-12). The video has no such gates.
- The square donor toggles are a prototype, "not how it will be in the release" (spec). Beta uses a row click and an animal click (lead 2026-10-13).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Screen | Donor pick: the player clicks an empty donor row, then clicks an animal in the DNA list on the right. The player drags the slider of the row to set its share | lead 2026-10-13 |
| Screen | Basic view, left: stacked rows. Each row is a small square slot (blue when chosen) and a slider with the % in the middle. Row 1 is the base dinosaur. The rest are donor rows. Hover shows the donor name | video 4:50-5:44 |
| Screen | Basic view, centre: framed 3D preview that turns slowly, with a Steve model beside it for scale. Right: framed list of sequenced animals, chosen row solid blue | video 5:00-6:20 |
| Screen | Basic view, bottom: wide panel with two columns of effect rows. Title in green (plus), red (minus) or white (zero), with a colour bar under it. Two small donor-colour swatches sit on the bar. Click one to send it to the primary or the secondary layer. The swatch swap stays | video 5:30-5:56, lead 2026-10-12 |
| Screen | A button at the top left switches between Basic and Advanced | video 4:50, 6:35 |
| Screen | Advanced view, left: chromosome grid, 4 columns by about 22 rows of small bars. Dull cyan = locked. Purple = isolated, can edit. Red = selected. Hover shows the gene name | video 6:36-10:10 |
| Screen | Advanced view, right of the preview: control for the selected gene. Gender: a button that cycles Male, Female, Random, and a "Reset Gene" button. Number gene: title, a box with the %, a slider in a limited range. Choice gene (skin, special gene): a list | video 6:38-10:10, decision, section 11.6 |
| Screen | Colour: primary and secondary mode by default, and a mode for each layer. Each layer has a colour wheel with a brightness strip, plus saturation, opacity and tint strength. Claws, teeth, nostrils, eyes and mouth are not listed | video 9:05-9:30, decision, section 11.6 |
| Screen | Risk panel: chance of incubation failure, the stat trade-offs, chance of abnormal behaviour. It shows chances only, never the rolled result. Position: a strip under the effect panel in both views, and a one-line summary on the Synthesis tab | decision, section 11.6, plan, lead 2026-10-12 |
| Numbers | Clone at 50%. Edit at 70%. Gender sure at 80%, below that a 30% chance of random. Donor slots at 50, 75, 100%, maximum 3. Base and donors total 100%. All are config values | decision, section 11.6 |
| Numbers | From 50% the player can move the donor sliders. Gene sliders unlock at 70% | lead 2026-10-12 |
| Numbers | Attack and defense edits need 90% | design doc, lead 2026-10-12 |
| Numbers | Clone-count limit: a new drive starts with 2 edit slots. Each 3 clones hatched add one edit slot. The hard drive stores the count, so a shared drive shares its unlocks | design doc, lead 2026-10-12 (D140) |
| Numbers | The count counts hatches. Each egg remembers its drive. After a hatch, the drive count rises the next time that drive is in a Sequencer | lead 2026-10-12 |
| Numbers | The player picks one option in each gene category | design doc, decision, section 11.6 |
| Numbers | Gene values come from the "Vanilla Animal DNA Effects" table. A donor at 50% share gives its full table value. A smaller share scales it down in a line. An unsigned table value is an increase (D66) | decision, section 11.6, plan |
| Numbers | Gene slider range by DNA %: ±10 % of the species base at 70 %, growing in a line to ±30 % at 100 %. Example: ±17 % at 80 % | lead 2026-10-13 |
| Numbers | Risk curve, config values that playtests tune. Incubation failure = 25 % × (donor share ÷ 50 %)² + 2 % per edit. Abnormal behaviour = 10 % × (donor share ÷ 50 %)² + 1 % per edit | lead 2026-10-13 |
| Numbers | Trade-off strength is the share of a stat gain that its paired stat loses. It is 20 % + 60 % × (donor share ÷ 50 %)² + 5 % per edit, at most 100 %. Example: a 40 % bee donor with no edits gives 16 % failure, 6 % abnormal behaviour and 58 % trade-off | lead 2026-10-13 |
| Numbers | Stat trade-offs come in five pairs. Speed up lowers maximum health. Strength up raises hunger. Size up lowers speed and raises hunger. Defense up lowers speed. Health up lowers regeneration. They grow with donor share and edit count | reconciliation, lead 2026-10-12 (D45) |
| Numbers | Abnormal behaviour is a separate list. A risky genome can roll one entry at hatch. The entries: stresses easily (threshold −20), flipped day and night, loner (maximum group 1), more aggressive (+30 %, still warns) | reconciliation, lead 2026-10-12 (D44) |
| Numbers | A strong jump edit can clear the 3-block low-security fence. The jump cap stays below the 8-block high-security fence | lead 2026-10-12 (D47, reversed) |
| Numbers | Mob variants change colour only, never genes. Green parrot DNA gives a raptor a green colour shift. No skin has the name "Delta" | lead 2026-10-12 (D65, D67) |
| Numbers | The cosmetic extras of the older design doc stay: turtle DNA gives a greener skin, and rabbit DNA raises the albino chance. Stat numbers come only from the new donor table | lead 2026-10-12 (D68) |
| Effects | Gene effects live in code in Beta. Data-pack editing of genes comes after Beta | lead 2026-10-12 (D30) |
| Effects | Every gene group changes the animal: core stats, needs, behaviour, breeding and survival | decision, section 11.6 |
| Animation | The preview turns slowly and shows size and colour changes at once | video 5:00-6:30 |
| Sound | Vanilla button clicks. No source asks for more | none |
| Failure | Limits: no edit gives -100% size, sliders reach their maximum, the total never passes 100%. The game checks every edit | GH #49, GH #48, GH #27, plan |

**Art needed:** see Art list → Sequencer (Edit tab: base page, effect panel, chromosome grid, gender and reset buttons, colour wheel, number slider, risk panel, Basic and Advanced button).

**Open questions:**

- Settled by the lead (2026-10-13): the slider range by DNA % and the donor pick by row and animal click. Also the risk curve, and unsigned donor values as increases (D66).
- Settled by the lead (2026-10-12): the count counts hatches. The egg remembers its drive, and the count rises the next time that drive is in a Sequencer. A new drive starts with 2 edit slots (D140).
- Settled by the lead (2026-10-12, follow-up pass):
  - the five trade-off pairs (D45) and the abnormal-behaviour list (D44)
  - a strong jump can clear a low fence (D47). This reverses the earlier default.
  - colour-only mob variants (D65), the green raptor shift with no "Delta" skin (D67), and the cosmetic extras (D68)
  - gene effects in code for Beta (D30), and no Incubator sex lever (D73).
- Settled by the lead (2026-10-12): the 90% gate for attack and defense edits and the clone-count limit stay. This replaces the plan reading "section 11.6 uses the sequenced % only".

**Today:** The edit tab is a prototype. The game does not check edits and does not save layer colours. Only gender and size change the animal.

**Engineering:** backlog F1.5 (old modules B07, B08).

---

### F1.6 Sequencer: synthesis

**In one line:** The Sequencer joins the designed genome with a bird embryo and makes a dinosaur-embryo syringe.

**How it plays:** The player fills four tanks with water, sugar, bone matter and plant matter. The player inserts a bird-embryo syringe and presses Begin. A glowing embryo picture fills from the bottom over 10 minutes. The output is a syringe with a dinosaur embryo that carries the genome.

**Sources:** 2022 video (7:00-7:45, 10:33-11:11), spec, decision (section 11.6, 11.7), lead 2026-10-12.

**Differs from the video:**

- The input is a bird-embryo syringe, not an empty test tube (decision, section 11.7). The video uses a tube (7:13).
- The output is a dinosaur-embryo syringe, not a "Tyrannosaurus Test Tube" (spec, decision, section 11.7). See 7:30.
- The tooltip shows the donor mix, edits, failure chance and gender (decision, section 11.7). The video shows size, speed, colour hex codes and gender (7:34-7:42).
- It needs power (decision, section 11.8).
- A Diamond Chip also speeds up synthesis (lead 2026-10-12). The spec gives synthesis no diamond chip.

| Aspect | Requirement | Source |
| --- | --- | --- |
| Screen | Same frame as F1.4. Four corner groups: Water top left, Sugar bottom left, Bone Matter top right, Plant Matter bottom right. Each has a slot above its label and a long bar below it. A full bar is bright cyan, an empty bar is a dark track | video 7:00-7:20 |
| Screen | Centre: a large glowing outline of an embryo curled inside a DNA helix. It fills with bright cyan from the bottom up as the run goes | video 7:22-7:30 |
| Screen | Top centre: "Begin" button. It reads "Cancel" while running. Bottom: input slot, right arrow that fills cyan, output slot. Tooltip "9m 59s (0%)" | video 7:22-7:30 |
| Screen | Popups when a slot is clicked: "Insert Water", "Insert Bone Matter", and "Insert Syringe" in place of "Insert Test Tube" | video 7:03-7:13, decision, section 11.7 |
| Screen | The risk summary shows before the player presses Begin | decision, section 11.6 |
| Inputs | Water (bucket or pipe), sugar, bone matter, plant matter, a bird-embryo syringe. The genome comes from the Edit tab | video 7:03-7:20, Mod Commission, decision, section 11.7 |
| Inputs | Bone matter: bone meal, or a bone block that counts as 9 bone meal | lead 2026-10-12 |
| Inputs | Plant matter: an item tag list in config. Leaves, saplings, wheat, kelp, grass, flowers, and the same items of other mods | Mod Commission, lead 2026-10-12 |
| Inputs | The input slot accepts only DNA at or above the clone threshold, so a run cannot fail at the start | plan |
| Outputs | Syringe with a dinosaur embryo that carries the genome. Shift on the tooltip shows the breakdown: species, donor mix, edits, failure chance, gender ("may be random" below 80%) | spec, decision, section 11.7, video 7:30 |
| Capacity | Base tanks: 1 bucket of water, 16 of each matter. Iron 150% (1.5 / 24). Gold 200% (2 / 32). Diamond 250% (2.5 / 40). Netherite 300% (3 / 48). Water fills in half-bucket steps | spec, video 10:41-10:57, plan |
| Timing | 10 minutes. Iron chip 7 min. Gold chip 4 min. Diamond chip 2 min | spec, video 11:00-11:11, lead 2026-10-12, lead 2026-10-13 (D139) |
| Numbers | Ingredients used per run: the task plan reads them from the video at 7:20-7:35 as config defaults. Playtests set them (D93). The video bars fall during a run: Sugar 48 to 40, Plant Matter 60 to 58 | video 7:30-7:35, lead 2026-10-12 |
| Upgrades | Section 11.8 says every upgrade changes the look. Chips: the 3 shared chips (F1.10). Chip tiers use the 4 chip cubes in the current model (`computer_chip01` to `04`, inside `mainBody`). Proposal, lead decides: more chips show at higher tiers. A developer moves the chip cubes into bones of their own, with no art change. Tank tiers: a texture swap on an existing bone. No new part (D85) | spec, decision, section 11.8, lead 2026-10-12 |
| Animation | Screen only: the embryo fills, the arrow fills, the rings turn. No model motion | video 7:19-7:30, original 1.16 |
| Power | Uses power only while it runs: 20 FE per tick at base tier. Iron chip 30, gold 55, diamond 115 FE per tick (F1.11) | decision, section 11.8, lead 2026-10-13 |
| Sound | Synthesis loop and synthesis done. Begin and Cancel: vanilla click. Each sound uses the Sequencer's own asset where one exists, and a generic lab sound for the rest. The artist also makes sounds of its own for each machine | proposal (events), lead 2026-10-12 (asker's reading) |
| Failure | With no power or a missing ingredient it stops and shows the reason (F1.10) | decision, section 11.8, plan |
| Failure | Cancel gives back the bird-embryo syringe and the ingredients that the run did not use. A cancelled run uses no DNA from the drive | lead 2026-10-12 |

**Art needed:** see Art list → Sequencer (Synthesis tab, sounds, computer pieces per chip tier, tank texture swap), Lab items (syringe with bird embryo, syringe with dinosaur embryo, netherite tank upgrade).

**Open questions:**

- Settled by the lead (2026-10-13): the Diamond Chip time for synthesis is 2 minutes (D139).
- Settled by the lead (2026-10-13): the Sequencer chip look uses the 4 existing chip cubes on the rack shelves. More cubes show per tier, in the tier colour: no chip 1 dull cube, Iron 2 grey, Gold 3 gold, Diamond 4 blue. Tank look: the artist proposes which existing bone takes the texture swap (D85).
- Settled by the lead (2026-10-12): the 3 shared chips (iron, gold, diamond) fit the Processor, the Sequencer and the Egg Printer. This replaces the spec's "no diamond chip" for synthesis.
- Settled by section 11.7: the synthesis output is the embryo syringe, not a DNA tube (contradiction 17).

**Today:** Synthesis runs for 10 minutes with 500 mB of water and 8 each of bone meal, sugar and plant matter. It gives a test tube, and a crafting recipe makes the egg. Water tiers and the netherite tank do not match the spec.

**Engineering:** backlog F1.6 (old module B11).

---

### F1.7 Egg Printer

**In one line:** A 3D printer that builds an egg around the embryo, layer by layer, in front of the player.

**How it plays:** The player builds the printer from a base and three parts. The player loads bone meal and the dinosaur-embryo syringe. The needle sweeps back and forth over the platform, and the species' egg grows from the bottom up for 10 minutes. The unincubated egg and the empty syringe come out. An idle printer shows a ghost egg outline. One print in ten fails unless a Leveling Sensor is installed. The egg breaks with an effect, the embryo dies, and the player gets a cracked artificial egg, the bone meal and the empty syringe.

**Sources:** 2022 video (11:12-12:33), spec, decision (section 11.7), original 1.16 (animation), Mod Commission, lead 2026-10-12.

**Differs from the video:**

- The output is the unincubated egg. The crafting step that joined an artificial egg and a test tube (12:22-12:45) is removed (decision, section 11.7). The artificial egg stays only as the failed-print result, the cracked artificial egg (lead 2026-10-12).
- The syringe carries a dinosaur embryo from synthesis (decision, section 11.7). The video loads a bird-embryo syringe (11:38).
- The platform lowers only 0.5 px as the egg grows, because the current model has no more room below `Panel`. The original lowered it one egg length (6.5 px).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Building | Base first. Then needle, platform and body in any order. Parts in the current model: platform = bone `Panel`. The base, the body, the cross-rail and the needle are cubes inside bone `Body` today. A developer moves them into bones of their own, with no art change | video 11:12-11:33, lead 2026-10-12 |
| Building | Look: the current model (`egg_printer.geo.json`, texture `egg_printer.png`). Bones `Printer` (root), `Panel` (platform), `Body`, `Egg`, `Door` (glass lid) | lead 2026-10-12 |
| Screen | Art: the current `gui/egg_printer.png`, `bonemeal_bar.png` and `egg.png`. Status bar at the far left (F1.10) | lead 2026-10-12, video 11:38 |
| Screen | Left column, top to bottom: bone meal gauge, bone meal slot, syringe slot | video 11:35-11:40 |
| Screen | Centre: a large egg picture. Thin progress lines along its top and bottom edges grow with the print (low confidence) | video 11:38-12:00 |
| Screen | Right column: egg output slot on top, empty-syringe slot below. Tooltip "10m 0s (0%)" | video 11:40, 12:16-12:18 |
| Inputs | Bone meal and a dinosaur-embryo syringe | design doc, decision, section 11.7 |
| Outputs | Unincubated egg and the empty syringe | design doc, decision, section 11.7, spec |
| Outputs | Failed print: the embryo dies. The player gets a cracked artificial egg, the bone meal and the empty syringe | spec, lead 2026-10-12 |
| Timing | 10 minutes. Iron chip 8 min, gold 6 min, diamond 4 min | spec, video 12:11-12:20 |
| Numbers | 16 bone meal per egg, so a stack of 64 prints 4 eggs. Config value | lead 2026-10-13 |
| Upgrades | Leveling Sensor: the break chance goes from 10% to 0%. Chips (the 3 shared chips, F1.10) set the time. Section 11.8 says each upgrade changes the look. The chips and the sensor each get a texture swap on an existing bone. No new part (D85) | spec, video 12:03, decision, section 11.8, lead 2026-10-12 |
| Animation | Needle raster: while it prints, the needle moves along X on the cross-rail, and the cross-rail moves along Z on the side rails. Each moves 2 px each way as a triangle wave. X makes one back-and-forth per 20 ticks, Z one per 4 ticks (original numbers). Both rest in the centre when idle. A developer first moves the needle and cross-rail cubes out of `Body` into bones of their own, with no art change | original 1.16, video 11:46-11:55 |
| Animation | Platform: bone `Panel` lowers with progress while a job runs or an egg waits in the output. The original lowered it one egg length (6.5 px). The current model has 0.5 px of room below `Panel`, so it lowers 0.5 px at most | original 1.16, lead 2026-10-12 |
| Animation | Egg: the species' egg model (F1.9) with the printed texture builds from the bottom up on the platform as the print goes. The cut face is solid cream (#F7F1DD). At the end the egg rests on `Panel` | original 1.16, video 11:55-12:01, lead 2026-10-12 |
| Animation | Idle: with no job and no egg in the output, the platform shows a ghost egg outline | lead 2026-10-12 |
| Effects | Failed print: the egg on the platform plays an egg-break effect | lead 2026-10-12 |
| Animation | Lid: bone `Door` turns 60 degrees about X on its hinge (pivot [8, 16.62, 7.17], the back top edge) while any player has the screen open or an egg waits in the output | original 1.16 |
| Animation | Each part moves smoothly to its new position in half a second | original 1.16, Mod Commission, lead 2026-10-12 |
| Power | Uses power only while it prints: 20 FE per tick at base tier. Iron chip 26, gold 37, diamond 58 FE per tick. The Leveling Sensor costs no extra power (F1.11) | decision, section 11.8, lead 2026-10-13 |
| Light | The lamps of the current texture glow while it prints: the green lamp is lit while it prints. Proposal: the red lamp is lit while a fault stops it | lead 2026-10-13, proposal |
| Sound | Print loop, lid open and close, print done, egg breaks. Each sound uses the printer's own asset where one exists, and a generic lab sound for the rest. The artist also makes sounds of its own for each machine | proposal (events), lead 2026-10-12 (asker's reading) |
| Failure | 10% chance of a failed print without the sensor. With no power it stops and shows the reason | spec, video 11:55-12:03, decision, section 11.8 |

**Art needed:** see Art list → Egg Printer (models, texture swaps, ghost egg, animations, sounds, screen) and Lab items (machine part sprites, Leveling Sensor). Also Eggs and hatching (printed egg textures, egg-break effect, egg sprites).

**Open questions:**

- Which existing bones take the texture swaps for the chips and the Leveling Sensor (D85)? The artist proposes them, and the lead approves them.
- Settled by the lead (2026-10-13): 16 bone meal per egg. The lamps glow while it prints. The uncracked artificial egg sprite (`artificial_egg.png`) leaves the mod. The cracked one stays as the failed print.
- Settled by the lead (2026-10-12): a failed print kills the embryo. The player gets the bone meal and the empty syringe back, plus the cracked artificial egg.
- Settled by section 11.7: the printer takes the embryo syringe and bone meal, not "embryo, dna tube, and egg materials" (contradiction 18).

**Today:** Prints from an embryo syringe and 16 bone meal to an artificial egg, with a 10% crack chance. The sensor and the chip are not saved. Nothing in the model moves.

**Engineering:** backlog F1.7 (old module B11).

---

### F1.8 Incubator

**In one line:** A warm nest under a glass lid where a robot arm turns the eggs until they are ready to hatch.

**How it plays:** The player builds the incubator from its parts, adds leaves as plant matter and keeps it powered. Its built-in lamp is the base bulb. The player drags each egg to a spot on the bed in the screen. Every 5 minutes each egg gets a turn: the lid opens, the arm reaches the real egg, rolls it over and folds back. Bulbs make it faster and brighter. Without power or plant matter it stops, and an egg stalled for 5 minutes dies.

**Sources:** 2022 video (12:21-15:05), spec, decision (section 11.7), original 1.16 (arm and lid), design doc (5-minute turn), lead 2026-10-12.

**Differs from the video:**

- It needs power and plant matter, and a stalled egg dies (decision, section 11.7, lead 2026-10-12). The video shows none of this.
- Incubation can fail from genome risk (decision, section 11.6, 11.7). The video shows no failed incubation.
- The hot bulb takes 15 minutes (spec). The narrator says "50 minute", a speech error (14:28).
- Each egg turns every 5 minutes (lead 2026-10-12). The original used a timer of about 30 s that each player saw differently.
- The build has no lift part. The current model has no lift post, and the lid lifts without one (lead 2026-10-12). The video builds a lift (12:45).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Building | Base, nest, lid, arm base, arm. The arm sits on the left as the player faces it. Only placed parts show. No lift step: the current model has no lift part. The lid lifts without one. The original 1.16 build had a lift (6 parts). The lead kept 5 parts on 2026-10-13 | video 12:34-12:54, GH #29, GH #41, lead 2026-10-12, lead 2026-10-13 |
| Building | Look: the current model (`incubator.geo.json`, texture `incubator.png`). Bones `body`, `nest`, `cover` (glass dome, lamp cap, two small tabs on one edge), `arm_base`, and the arm chain `RoboticHand1` to `RoboticHand6` with a two-finger claw. The glass stays see-through with shaders | lead 2026-10-12, GH #45 |
| Building | The nest part is an empty bed: an iron tray with no wool and no hay. The bed stays empty until the player fills it with plant matter | lead 2026-10-13 (D256) |
| Screen | Art: the current `gui/incubator.png`. Status bar on the left (F1.10). Top: one large bed pane. Eggs show as small egg markers at their real bed spot. Player inventory below | lead 2026-10-12, video 12:50-13:16 |
| Screen | Bottom left of the pane: a plant icon (a sprout outline when empty) with the plant matter count and a long bar. Adding leaves plays a small icon and bar animation | video 12:55-13:00 |
| Screen | Holding an egg over the pane turns it bright green where the egg can go and red where it cannot. A placed egg can be dragged to a new spot. Egg tooltip: name, progress %, time left, "Press shift for DNA breakdown". Time left stops at 0 | video 13:00-13:32, GH #16 |
| Inputs | Unincubated eggs placed by the player. Plant matter: the plant matter tag list of F1.6. Power | video 12:55-13:32, Mod Commission, decision, section 11.7, lead 2026-10-12 |
| Inputs | The built-in lamp is the base bulb. The machine runs with no bulb item. A bulb item is an upgrade | lead 2026-10-12 |
| Outputs | Incubated eggs, taken from the bed | video 13:50 |
| Capacity | 3 eggs. Small container 6. Large container 9. Plant matter 100, iron tank 150, gold tank 200 | video 13:25-14:05, 14:47-15:02, spec, decision, section 11.7 |
| Numbers | Plant matter drains per machine, not per egg. The rate is a config value, and playtests set it | lead 2026-10-12 |
| Timing | Default 30 min (18 s per %). Warm bulb 25 min (15 s). Warmer 20 min (12 s). Hot 15 min (9 s). Every species takes 30 minutes at the base lamp in Beta. A config value, and playtests tune it | video 14:07-14:36, spec, lead 2026-10-12 |
| Upgrades | The bulb changes the lamp glow: an emissive texture per tier on the lamp cap of `cover`, with no new part. Section 11.8 says every upgrade changes the look. Containers and plant tanks each get a texture swap on an existing bone. No new part (D85) | spec, video 14:13-14:36, decision, section 11.8, lead 2026-10-12 |
| Power | Needs power to run: 20 FE per tick with the base lamp. Warm bulb 25, warmer 33, hot 46 FE per tick. Containers and plant tanks cost no extra power (F1.11) | decision, section 11.7, 11.8, lead 2026-10-13 |
| Light | Gives light while it incubates: level 9 with the base lamp, 11 warm, 13 warmer, 15 hot (D80) | GH #23, lead 2026-10-12 |
| Animation | Lid: bone `cover` lifts straight up 12.5 px while a player has the screen open, while the arm works, and for 1.5 s after. It is fully up before the arm reaches. Each lift and each lowering takes 1 s with a sine ease. The model has two small tabs on one edge of the lid. They do not change the lift | video 13:38-13:46, original 1.16, lead 2026-10-12 |
| Animation | When: each egg is due for a turn 5 minutes after its last turn, counted while it incubates. The arm visits every egg. When any egg is due, the lid opens and the arm picks one due egg at random. The game remembers each egg's last turn over a save and reload | design doc, lead 2026-10-12, original 1.16 |
| Animation | Reach: the arm reaches the real egg wherever it lies, with no preset positions. Eggs lie at random tilts that show where the claw grips | spec, Mod Commission, original 1.16, video 13:36-13:40 |
| Animation | Turn: `RoboticHand1` turns the arm to the egg about Y. `RoboticHand2` (shoulder, pivot [1.5, 21.5, 2]) and `RoboticHand3` (elbow, pivot [1.5, 28, 2.01]) reach as a two-link arm with 6.5 px links. `RoboticHand4` (wrist, pivot [1.5, 28, 8.53]) points the hand at the egg and rolls the egg over in about 2.5 s. Then the arm lets go. About 6 s for each egg. After the turn, the `RoboticHand1` turn gives a small settle shake | original 1.16, lead 2026-10-12 |
| Animation | Next egg: the arm moves straight to the next due egg and repeats. The roll direction flips each time. When no egg is due, the arm folds back home. If the player takes the egg away, the arm goes home at once | original 1.16 |
| Animation | Speed: every arm move runs at an even speed, as in the original. No ease in or out | original 1.16, lead 2026-10-12 |
| Animation | Claw: the two fingers `RoboticHand5` and `RoboticHand6` (pivots [2.99, 28, 10.5] and [0.01, 28, 10.5]) turn about Y in opposite directions. They open while the arm reaches, close to grip, and open at release. The original claw did not move | lead 2026-10-13 (D96) |
| Sound | Arm move, heater hum, lid up and down, egg placed, egg done. Each sound uses the Incubator's own asset where one exists (the arm move), and a generic lab sound for the rest. The artist also makes sounds of its own for each machine | proposal (events), lead 2026-10-12 (asker's reading) |
| Failure | Without power or plant matter, incubation stops. An egg stalled for 5 minutes dies (config value). Incubation failure rolls at 100% from the genome risk. The screen shows "No power" or "No plant matter" | decision, section 11.6, 11.7, plan, lead 2026-10-12 |

**Art needed:** see Art list → Incubator (models, texture swaps, animations, sounds, screen), Lab items (bulbs, container upgrades, plant tank upgrades, machine part sprites).

**Open questions:**

- Settled by the lead (2026-10-13): the nest part is an empty iron tray until the player adds plant matter (D256).
- Settled by the lead (2026-10-13): the Incubator keeps 3, 6 and 9 eggs. The spec note "NEEDS FIXING" on the 6-egg tier is set aside.
- Which existing bones take the texture swaps for the containers and the plant tanks (D85)? The artist proposes them, and the lead approves them.
- The arm links are 6.5 px each. Does the reach cover all 9 bed spots? If not, the bed spots move inward or the artist scales the arm.
- Settled by the lead (2026-10-13): the claw opens and closes (D96). The Incubator has no water-bottle input.
- Settled by the lead (2026-10-12): each egg turns every 5 minutes, and the arm visits every egg (contradiction 16).
- Settled by the lead (2026-10-12): the built-in lamp is the base bulb. This replaces "the incubator needs a bulb" (section 11.7, 2026-10-05).
- Settled by the lead (2026-10-12): every species incubates for 30 minutes in Beta. This sets aside "incubation time is different for each species" (section 11.7) for Beta.
- Settled by section 11.7: 3, 6 or 9 eggs, not the design doc's 8 or 10 (contradiction 15).

**Today:** Holds 3 eggs only, because the container upgrades are plain items. 18 s per % with bulbs at 15, 12 and 9 s. Plant matter 64, 128 and 192. No power draw. The arm, the lid and the eggs never move or show. The renderer only shows or hides `nest`, `cover`, `arm_base` and `RoboticHand1`.

**Engineering:** backlog F1.8 (old module B11).

---

### F1.9 Eggs and hatching

**In one line:** A real egg for each kind of dinosaur that rocks while it incubates, cracks on the ground, and hatches a baby that nobody has seen before.

**How it plays:** Each species has its egg model from the asset repo, with a printed or a natural texture. In the incubator each egg rocks now and then. The player places the incubated egg on the ground. It starts to shake at once, then shakes harder and cracks in stages. 60 s after placing, a baby steps out at the start of its growth. Dinosaurs can push a placed egg. The hatch rolls the clone's own variation and can fail.

**Sources:** decision (section 11.6, 11.7), lead 2026-10-12, design doc, 2022 video (13:00 tooltip, 15:02-15:55 spawn).

**Differs from the video:**

- The video spawns an adult at once on right-click (15:05-15:15). Beta places an egg that shakes, cracks and hatches a baby (decision, section 11.7, lead 2026-10-12).
- A hatch can fail (decision, section 11.7).
- In the video, each clone's look comes only from its edits (15:35-15:55). Beta adds a random roll at hatch (decision, section 11.6).
- Eggs rock while they incubate (lead 2026-10-12). The video and the original have no egg wobble.

| Aspect | Requirement | Source |
| --- | --- | --- |
| Items | Egg model per species, from the asset repo egg folders: Brachiosaurus `round`, Tyrannosaurus `tyrannosaurus`, Triceratops `tall_3`, Gallimimus `normal`, Velociraptor and Dilophosaurus `velociraptor` (D82) | lead 2026-10-12 |
| Items | Natural egg: one texture per species, in the colours of its species on the shared model. Printed egg (with a QR-code mark): one texture per egg model, 5 in total, plus a species tint in code | lead 2026-10-12, lead 2026-10-13, 2020 board, species cards |
| Items | The placed egg is an entity, not a block. Dinosaurs can push it (D05) | lead 2026-10-12 |
| Items | The unincubated egg and the incubated egg have their own sprites | design doc (Mod Commission item list) |
| Items | Tooltip: species, donor mix, the player's edits, chance of incubation failure, chosen gender ("may be random" below 80%). No rolled traits. In the incubator also progress % and time left | decision, section 11.7, video 13:00 |
| Animation | Wobble while incubating: each egg rocks gently side to side (about 4°) at a random moment every 30 to 90 s. The egg in the claw does not rock (D81) | lead 2026-10-12, design doc |
| Animation | Before hatch: the placed egg shakes, then cracks. 60 s in 3 stages of 20 s. The egg rocks every 4 s, then every 2 s with a first crack, then every second with a second crack and shell chips. Each stage rocks harder | decision, section 11.7, plan (3 crack stages), lead 2026-10-12 |
| Timing | 60 s from placing to hatching. The shake starts at once, with no wait before it | lead 2026-10-12 |
| Numbers | Hatch success is a separate roll from incubation failure. A lab egg uses the lab hatch rate of its species. The species data holds it, apart from the nest (card) rate. Config values, and playtests tune them (D41) | decision, section 11.7, lead 2026-10-12 |
| Numbers | Lab hatch start values by size: 95% for small species (Velociraptor, Dilophosaurus, Gallimimus), 75% for large species (T. rex, Triceratops, Brachiosaurus). Config values | lead 2026-10-12 (D147) |
| Numbers | A nest egg uses the nest hatch rate of its species (F2.5): T. rex 6 %, Velociraptor 25 %, Dilophosaurus 16 %, Gallimimus 15 %, Triceratops 11 %, Brachiosaurus 6 %. Config values | T. rex card, section 11.16, lead 2026-10-13 (D79) |
| Timing | A natural (nest) egg waits 3 in-game days (72,000 ticks) after laying. Then the 60 s shake starts. A lab egg has no wait | lead 2026-10-13 |
| Outputs | A baby at the start of growth. The hatch rolls the clone's variation: stats, size by gender weights, layer colours, albino or melanistic at about 5% | lead 2026-10-12, decision, section 11.6, 11.7 |
| Outputs | The hatchling has no owner. It stays wild until a player tames it | lead 2026-10-12 (D17) |
| Effects | Crack overlays per stage. Shell particles at the hatch | plan |
| Sound | Rock tap, crack per stage, hatch, broken egg. The 2020 board card "Egg" lists sounds and gives no names | 2020 board |
| Failure | A failed hatch leaves a broken egg and remains. A failed nest egg is different: it cracks open, drops tissue and vanishes (F2.5, D255). With no room for the baby, the egg waits, retries every 5 s and tells players within 8 blocks on the action bar | plan |
| Failure | A player who breaks a placed egg gets the egg item with its genome. An egg whose time passed while its chunk was unloaded hatches when the chunk loads | plan |

**Art needed:** see Art list → Eggs and hatching (egg models per species, natural and printed egg textures, crack stages, wobble and shake, sounds, shell particle, egg sprites), each species (Sprites: species icon). The nest uses vanilla blocks (F2.5).

**Open questions:**

- Settled by the lead (2026-10-13): the nest hatch rates (D79) and a 3-day wait for natural eggs. Also one printed texture per egg model, with a species tint.
- Egg models: the artist questions on the asset repo eggs are in the Art list (question 9). Example: the `normal` and `velociraptor` models have the same cubes, so the Gallimimus and raptor eggs have one shape.
- Settled by the lead (2026-10-12): lab eggs do not use the card's 5% (D41). The lab rate is separate from the nest rate.
- Settled by the lead (2026-10-12): the placed egg is an entity, not a block (D05). This replaces the plan default "a block that players bump into and dinosaurs walk through".
- Settled by the lead (2026-10-12): eggs wobble both while incubating and before the hatch (contradiction 48).
- Settled by the lead (2026-10-12, follow-up pass): lab hatch start values 95% for small species and 75% for large species (D147). A hatchling has no owner (D17).

**Today:** Right-clicking an incubated egg spawns an adult dinosaur at once. No egg model, no wobble, no hatch roll, and growth never starts.

**Engineering:** backlog F1.9 (old module B11).

---

### F1.10 Machine building, upgrades, linked tabs, faults

**In one line:** Every lab machine is built from parts, upgraded by hand, linked by screen tabs, and says clearly why it stopped.

**How it plays:** The player crafts each part and right-clicks it onto the base in order, and the model grows piece by piece. The player right-clicks upgrades onto a machine, and the machine looks different. With machines linked, the player moves between their screens with tabs and never closes the window. A stopped machine puffs smoke, goes dark and names the problem.

**Sources:** 2022 video (0:23-0:38, 1:36-2:15, 2:17-2:44, 11:12-11:33, 12:34-12:54, 13:53-14:36), design doc, spec, decision (section 11.8), lead 2026-10-12.

**Differs from the video:**

- Tabs between linked machines are new (spec, decision, section 11.8). The video opens each machine on its own.
- Power, faults and warnings are new (decision, section 11.8).
- Every part and upgrade has a survival recipe (decision, section 11.8). The video uses the creative inventory.

| Aspect | Requirement | Source |
| --- | --- | --- |
| Building | Each machine is built from crafted parts in a set order, by right-clicking each part onto the machine. Each placed part shows as a bone of the current model. Orders per machine are in F1.3, F1.4, F1.7 and F1.8 | design doc, video, decision, section 11.8, lead 2026-10-12 |
| Building | Part names follow the video: Base, Needle, Platform, Body (Egg Printer), not the design doc's Chassis, Lid, Printer Frame and Dome | video, lead 2026-10-12 |
| Building | A creative-only item places a whole machine. Parts can be reused to build another machine. Breaking any part drops every item once | decision, section 11.8, GH #12, plan |
| Building | The hitbox matches the model. Breaking one machine never breaks its neighbours. Break particles use the machine texture and are fewer than today | GH #30, #44, #51, GH #31, GH #15, GH #42 |
| Upgrades | Installed by right-click with the upgrade item. Replacing an upgrade gives the old one back. Example: a gold chip over an iron chip returns the iron chip | video 1:36-2:15, decision, section 11.8, lead 2026-10-12 |
| Upgrades | Every upgrade changes the look, with no new part. The Processor tanks and the Sequencer chips use parts of the current models. Every other upgrade gets a texture swap on an existing bone (D85) | decision, section 11.8, lead 2026-10-12 |
| Upgrades | Chips: 3 shared chip items (iron, gold, diamond). Each fits the Processor, the Sequencer and the Egg Printer | lead 2026-10-12 |
| Upgrades | At most 3 upgrades per machine, as the design doc says. No machine has more than 3 upgrade slots today, so the cap limits nothing. No upgrade blocks another | design doc, lead 2026-10-12 |
| Power | Only chips and bulbs cost extra power. Each run of a faster tier costs 5 % (iron chip, warm bulb, SSD), 10 % (gold, warmer) or 15 % (diamond, hot) more energy. Filters, the Leveling Sensor, containers and tanks cost no extra power | decision, section 11.8, lead 2026-10-13 (D141) |
| Screen | Tabs between linked machines. Linked machines are all machines on one conduit or generator network. The screen stays open on a switch. Icons: bone (Processor), DNA (Sequencer), egg (Egg Printer), nest (Incubator), each in an active and an inactive look | spec (tab strip image), decision, section 11.8, lead 2026-10-12 |
| Screen | Tab position not set. Proposal, lead decides: a row of tabs along the top-left outside edge, in chain order, like vanilla creative tabs | proposal |
| Screen | The green status bar on the left of every machine screen. Its meaning is not stated. Proposal, lead decides: stored energy, with the red tick showing "running" | video 1:07, 2:53, 11:38, 12:50 |
| Screen | The world behind darkens like vanilla screens. Screens survive a window resize. Shift-click works | GH #22, GH #24, GH #18, #19, #21 |
| Screen | Hover box: looking at a machine for a quarter second shows a small box near the crosshair, for example "Working 42 %" or the fault | plan |
| Failure | A stopped machine shows a warning tab with the reason: no power, no input, output full, wrong filter, no plant matter, no egg, species missing. "No bulb" is gone, because the Incubator's built-in lamp is the base bulb (F1.8) | plan, lead 2026-10-12 |
| Effects | Power loss: 3 smoke puffs over 1 s and a dark model. One puff when it starts again. The working look stops 3 s after work stops | plan |
| Power | A machine with no power stops. A machine that does no work uses no power | decision, section 11.8 |
| Sound | Part placed, upgrade installed, power lost, power back | proposal, lead decides |
| Sound | Tab switch: vanilla click | none |

**Art needed:** see Art list → Screens (GUI) (linked-machine tabs, pop-up slot dialog, status bar), Lab items (machine part sprites).

**Open questions:**

- Settled by the lead (2026-10-13): only chips and bulbs cost extra power, +5, +10 and +15 % energy per run (D141).
- Settled by the lead (2026-10-12): every machine on one conduit or generator network shows as a tab. Example: two labs on one generator show all eight machines.
- Settled by the lead (2026-10-12): every upgrade changes the look, also the Sequencer tanks (section 11.8 over the spec).
- Settled by the lead (2026-10-12): the design-doc cap of 3 upgrades per machine stays. This replaces "no limit on the number of upgrades" (section 11.8, 2026-10-05). The cap limits nothing today. No upgrade blocks another.

**Today:** Only the Incubator assembles part by part. No tabs, no warning tab, no fault smoke, and break drops are incomplete.

**Engineering:** backlog F1.10 (old module B12).

---

### F1.11 Lab power

**In one line:** A fuel-burning Basic Generator and a power conduit run the whole lab.

**How it plays:** The player builds a Basic Generator and feeds it coal or any other furnace fuel. Its flame glows, smoke rises and it lights the room while it burns. A thin conduit cable carries the power to the machines. Machines draw power only while they work, and faster chips draw more. When the fuel runs out, the machines stop and say "No power".

**Sources:** decision (section 11.8, 11.11), plan, lead 2026-10-12. The 2022 video shows no power.

**Differs from the video:**

- Power is new. The video machines run with no power source (decision, section 11.8).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Building | Basic Generator: one block. A power conduit: the mod's own thin cable block. Other mods' energy cables also work | decision, section 11.8, 11.11, lead 2026-10-12 |
| Inputs | Any furnace fuel, burned at the furnace rate. One coal lasts 80 s. It burns while it has fuel, also with no load | decision, section 11.11 |
| Outputs | Energy to conduits, machines and other mods' cables. The Basic Generator makes 200 FE per tick while it burns. Its buffer holds 20,000 FE, 5 s of full output. Other mods' cables can take its full output | decision, section 11.8, plan, lead 2026-10-13 |
| Numbers | One Basic Generator runs all four machines at base tier plus a first paddock of about 10 fence posts. A post draws 10 FE per tick | decision, section 11.11 |
| Numbers | Machine draw: 20 FE per tick at base tier, only while the machine works. A faster tier draws base × speed × run-cost factor (table below). Burn times are config values | decision, section 11.11, plan, lead 2026-10-13 |
| Power | Overload: when the demand on a network is more than its supply, the generator trips and stops. It stays off until the player removes load or adds a generator | lead 2026-10-13 |
| Power | A conduit network has no capacity limit. It carries all the energy that its generators make | lead 2026-10-13 |
| Power | Generators and machines keep their stored energy over a reload. Other mods' cables connect to generators and machines only. Posts, pylons and conduits accept no outside cables | plan, lead 2026-10-12 (D24) |
| Power | While a generator's chunk is not loaded, its fence keeps its last power state. When the chunk loads, the generator burns fuel for the time that passed. A developer decides whether this catch-up burn makes energy. No forced chunk loading | lead 2026-10-12 (D21), lead 2026-10-13 |
| Screen | Generator screen: fuel slot, burning flame, energy bar. Layout not set | proposal, lead decides |
| Animation | None in the original. The flame glows while it burns, and a smoke puff rises every 2 s | lead 2026-10-12, proposal (2 s) |
| Light | Light level 13 while it burns, like a lit furnace | lead 2026-10-12 |
| Effects | No comparator output and no power meter block in Beta. The hover box is always on, and a client setting hides it | lead 2026-10-12 (D27) |
| Sound | Run loop, ignite, fuel out. The generator gets sounds of its own | proposal (events), lead 2026-10-12 |
| Failure | Out of fuel: the generator runs on its buffer for about 5 s. Then the machines stop and show "No power", and the fence lamps fade post by post over about 2 s (F3.2) | decision, section 11.8, lead 2026-10-13 |

**Power numbers (lead 2026-10-13).** Config values, and playtests tune them.

| Value | Basic Generator | Large Generator (F3.2) |
| --- | --- | --- |
| Output while it burns | 200 FE per tick | 800 FE per tick (4 ×) |
| Buffer | 20,000 FE (5 s) | 80,000 FE (5 s) |
| Burn rate | Furnace rate: coal 80 s | Half the furnace rate: coal 160 s |
| Energy per coal | 320,000 FE | 2,560,000 FE |
| Coal per in-game day | 15 | 7.5 |
| Posts it runs with no other load | 20 | 80 |

Machine draw in FE per tick, with the run time in brackets. A faster tier draws base × speed × run-cost factor. The factor is 1.05, 1.10 or 1.15, so each run costs 5, 10 or 15 % more energy.

| Machine | Base | Tier 1 (iron chip, warm bulb, SSD) | Tier 2 (gold chip, warmer bulb) | Tier 3 (diamond chip, hot bulb) |
| --- | --- | --- | --- | --- |
| Fossil Processor | 20 (4 min) | 28 (3 min) | 44 (2 min) | 92 (1 min) |
| Sequencer: synthesis | 20 (10 min) | 30 (7 min) | 55 (4 min) | 115 (2 min) |
| Sequencer: sequencing | 20 (Hard Drive, 10 s) | 42 (SSD, 5 s) | – | – |
| Egg Printer | 20 (10 min) | 26 (8 min) | 37 (6 min) | 58 (4 min) |
| Incubator | 20 (30 min) | 25 (25 min) | 33 (20 min) | 46 (15 min) |

- Lab peaks with all four machines at work: 80 FE per tick at base, 169 at tier 2, 311 at tier 3.
- Starter park: 4 base machines (80) and 10 low-security posts (100) need 180 of the 200 FE per tick.
- A second paddock of 10 posts with the lab at work needs 280 FE per tick. The Basic Generator trips.
- One Large Generator runs about 48 posts beside a tier 3 lab, and 72 beside a base lab.

**Art needed:** see Art list → Lab power (Basic Generator, flame glow and smoke, generator sounds, conduit).

**Open questions:**

- Settled by the lead (2026-10-13): the generator output and the machine draw above (D119, D141). Also no conduit capacity limit, a trip on overload, and no cap on outside cables.
- Settled by the lead (2026-10-12): Claude drafts the generator output and the machine draw from the balance target above. The lead reviews the draft.
- Settled by the lead (2026-10-12): the conduit is a thin cable block, and its network sets which machines link as tabs (F1.10).
- Settled by the lead (2026-10-12, M3 pass): other mods' cables reach generators and machines only (D24). The grid remembers power while a generator's chunk is not loaded (D21). No comparator output and no power meter block (D27).

**Today:** A coal generator gives 0.2 FE per tick (4 FE each second) and loses its energy on reload. Machines only pretend to draw power, so nothing uses it. No conduit exists.

**Engineering:** backlog F1.11 (old module B14, machine power).

---

### F1.12 Lab recipes and advancements

**In one line:** Every lab item can be crafted in survival, and advancements guide the player from the first fossil to the first hatch.

**How it plays:** The player crafts every part, upgrade and tool from vanilla materials. Better tiers cost better metals. Advancements pop up at each step of the chain. A new player can reach a hatched baby with no creative mode and no trades.

**Sources:** decision (section 11.7, 11.8), spec (tier names), plan, lead 2026-10-12. The 2022 video uses the creative inventory (0:30, 1:05).

**Differs from the video:**

- All lab items have survival recipes (decision, section 11.8).
- The unincubated egg crafting recipe (12:22-12:45) is removed (decision, section 11.7).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Items | Every lab block, part, upgrade and tool has a recipe from vanilla materials. No part comes only from loot or trades | decision, section 11.8 |
| Items | Lab recipes: 17 machine parts (Processor 4, Sequencer 4, Egg Printer 4, Incubator 5), tank upgrades (iron, gold, diamond, netherite), 3 filters, 3 shared chips, Hard Drive, SSD, syringe, test tube, 3 bulbs, 2 container upgrades, 2 plant tanks, Leveling Sensor, Basic Generator, power conduit, fossil pouch | plan, lead 2026-10-12, lead 2026-10-13 (no Identification Tag) |
| Items | No recipe: assembled machines (creative only), fossils and amber (world drops), machine outputs (tubes, filled syringes, eggs, the cracked artificial egg) | plan, lead 2026-10-12 |
| Numbers | Tier names iron, gold, diamond, netherite set the metal of each upgrade tier. Ingredients: the lead approved Claude's draft with changes (D78). The lead approved the 17 machine-part grids (D256). The backlog section "Recipes and progression gates" holds every grid | spec, plan, lead 2026-10-13 |
| Items | No lab part uses a stone material. The parts are machines and computers smaller than a player. Quartz slabs take the place of smooth stone | lead 2026-10-13 (D256) |
| Numbers | The base machines need some gold and quartz slabs. So the first lab needs gold and a Nether trip for nether quartz. No recipe uses special pieces. Beta has no emerald tank, no netherite Processor tank and no Ender SSD | lead 2026-10-12 (D78, D142, D143), lead 2026-10-13 (D256) |
| Numbers | The four machines alone cost about 50 iron value, 4 gold ingots and 14 quartz slabs. Each machine has one gold ingot in its precision part. With the Basic Generator, the Hard Drive, the Iron Filter and a syringe, the first setup costs about 66 | lead 2026-10-13 (D256) |
| Numbers | The player crafts each upgrade fresh. A higher tier never uses up the lower tier. One exception: the Netherite Tank Upgrade is a Diamond Tank Upgrade plus a netherite ingot | lead 2026-10-13 |
| Numbers | Materials: copper for wiring (Sequencer Monitor, conduit, Wire Spool, pylon, chips). Blaze powder in the Hot Bulb. Nether quartz in the quartz-slab parts, the SSD and the Leveling Sensor. Netherite only in the Sequencer tank (300 %, spec) | lead 2026-10-13 |
| Numbers | Output per craft: 3 test tubes | lead 2026-10-13 |
| Items | New and fixed ids: `basic_generator` replaces `coal_generator`, and `iron_plant_tank_upgrade` replaces `iron_plant_tank_ugprade`. A `*_base` part item starts each machine in survival | lead 2026-10-13 |
| Effects | The recipe book unlocks the lab parts when the player picks up the first fossil or amber | lead 2026-10-13 |
| Numbers | The Paleontologist sells chips, the Hard Drive, the Gold Filter and lab machine parts (F4.1). So a player can skip the gold and quartz gate by trade. The no-trade route stays possible | lead 2026-10-13 |
| Effects | Advancements: first fossil, first amber, first DNA tube, first species at 50% genome, first edit, first embryo, first egg, first hatch, first fully upgraded machine | plan, lead 2026-10-12 |
| Screen | Creative tabs ordered by chain stage | plan |
| Numbers | A survival player reaches a hatched baby with no creative mode and no trades | plan, M1 goal |

**Art needed:** see Art list → Lab items. Advancement icons reuse these item sprites.

**Open questions:**

- Settled by the lead (2026-10-13, D256): the 17 machine-part grids. No lab part uses a stone material. Quartz slabs make the first lab need a Nether trip, and the lead accepted this gate. The four machines cost about 50 iron value, 4 gold ingots and 14 quartz slabs. This supersedes the Z-8 sum of about 45 and the grids that Claude re-costed.
- Settled by the lead (2026-10-13, Z-8): the four machines alone cost about 45 iron value and 4 gold ingots. The first setup costs about 60. This supersedes the plan reading "about 45 for the whole first lab, about 28 for the machines". Claude re-costed the machine-part grids to the lead's sums. Beta has no Identification Tag. D256 replaces this sum and these grids.
- Settled by the lead (2026-10-13): Claude's recipe draft, with the lead's changes (D78). The backlog section "Recipes and progression gates" holds the grids.
- Settled by the lead (2026-10-12, follow-up pass):
  - The base machines need some gold. The recipe draft follows this gate.
  - No recipe uses special pieces. They serve DNA and trades only.
  - No emerald tank and no other extra tier in Beta.
- Settled by the lead (2026-10-12): the earlier proposals are also in: first amber, first species at 50% genome, first edit, first embryo (D148).
- Settled by the lead (2026-10-12): the 3 shared chips fit the Processor, the Sequencer and the Egg Printer (F1.10).

**Today:** One recipe exists, the unincubated egg, and Beta removes it. No advancements.

**Engineering:** backlog F1.12 (old module B13, lab items).

## M2. Dinosaurs

M2 makes the six Beta species into living animals: Tyrannosaurus, Velociraptor, Brachiosaurus, Triceratops, Dilophosaurus and Gallimimus.

Each species hatches small, grows smoothly, eats, drinks, rests, sleeps and leaves a carcass when it dies. Each species looks, moves and sounds like itself. It has its own skins, clips, sounds and effects, and never borrows another species' assets. Animals form herds and packs, pick leaders, breed and guard nests. They warn a player before they attack, and a calm animal can be tamed.

The milestone ends with a playtest: a paddock of each species shows every need, group behaviour and threat response.

The features: F2.0 Dinosaur behaviour (the model that ties the others together), F2.1 Species core, F2.2 Appearance, F2.3 Animations, sounds and visual effects, F2.4 Needs, F2.5 Groups, F2.6 The player, F2.7 Special genes and quirks.

### F2.0 Dinosaur behaviour

**In one line:** One model for everything a dinosaur does, what a well-kept dinosaur looks like, and how a park keeper sees that an animal is content or in trouble.

**How it plays:** A dinosaur always does one thing at a time: it rests, eats, drinks, travels with its herd, calls to its mates, hunts, fights or flees. When two things compete, the more urgent one wins. A fleeing Gallimimus does not stop to graze. A well-kept animal has a daily rhythm. A Gallimimus herd grazes and travels by day and lies down together at night. A fed T. rex patrols, sniffs the air and roars now and then. Animals share their food. A herd gathers around one feeder or one tree. Each animal takes its own place and its own leaves, and the others wait their turn. A content herbivore grazes in its spare time. It gains a little food and leaves the grass in place. A keeper reads the paddock at a glance. Content animals sit, sniff, look around, graze and call softly. Hungry, thirsty, lonely or stressed animals pace to food and water, call more, sit less and turn aggressive. Any player who looks at a dinosaur sees how content it is and what it lacks most. If the park keeps failing them, they lose health and die. They never attack a fence. Only a T. rex breaks a fence, and only an unpowered one. A hyper-intelligent animal that notices a dead fence section walks there and climbs out.

**Why this section exists.** The lead asked where the dinosaur AI is, and what an ideal, needs-met dinosaur looks like (lead 2026-10-12). Behaviour rules sit today in F2.4 (needs), F2.5 (groups) and F2.6 (the player). No source defines a healthy dinosaur. F2.0 ties them together. It does not repeat their rules. It links to them by id. Four later lead decisions of 2026-10-12 also live here: animals feed together ("Feeding together"), content herbivores graze when idle ("Feeding together"), Beta has a contentment score ("Contentment"), and genes change behaviour ("How genes change behaviour"). The M2 open-items pass of 2026-10-12 then answered most open questions of F2.0 to F2.7. The follow-up pass of the same date answered more of them.

**Reversals of the M2 open-items pass (lead 2026-10-12).** These answers replace earlier text:

- Scent trails are not in Beta. This reverses the decision of 2026-10-05. A predator that loses sight of its target searches the last seen spot.
- The "territory" radius becomes a personal-space radius plus "young provoke always". Beta has no home territory (lead 2026-10-13, D166).
- The Brachiosaurus defends with a tail whip and a stomp. This replaces the flee default of D13.
- T. rex, Dilophosaurus and Velociraptor are cathemeral. Each species has its own two sleep windows.
- The Gallimimus is an omnivore. It uses both feeder kinds and gets the blood effects.
- Every Velociraptor hatches with hyper intelligence. This replaces plan default D43.
- A hyper-intelligent animal walks to a dead fence section and climbs out. This is a lead-approved exception to fence parity.
- The scent gene (RC9, D48) is moot, because Beta has no scent.
- The contentment cap is the worst part plus 50, not plus 30.

**Reversals of the follow-up pass (lead 2026-10-12).** These answers replace earlier text or plan defaults:

- Lost sleep gives no stress. This reverses the M2 pass rule "each missed window adds one stress step" (D105).
- The hover box shows icons only: a coloured mood face and need icons, with no band words (D149). This replaces the text mock-up.
- A carnivore stands or retreats by threat and group size, not always stands (D13).
- A Dilophosaurus group hunts together, not alone (D54).
- An animal avoids the place where it was hurt for 1 in-game day, not 2 minutes (D61).
- Packs journey when hungry, to find prey (D56). The plan default was "no journeys".
- A strong jump edit can clear the 3-block low-security fence (D47).
- Breeding has two more gates: full health, and no own young under half grown within 16 blocks (D72).

**Decisions of the final pass (lead 2026-10-13).** These answers replace earlier text or plan defaults:

- Loneliness is part of stress. It has no bar of its own (D52). This replaces "the social need is a need of its own" (section 11.10).
- Beta has no home territory (D166). Eggs count as young, so parents defend them.
- The female builds a nest from vanilla blocks of her surroundings. This replaces "one nest block for all species".
- A herd member steps off a journey only at an urgent need (D36). Herd meals are hybrid, queue first (D111).
- A herbivore that the nocturnal gene shifts keeps its own sleep windows, also in a herd. This is an exception to "the group sleeps together".
- The need cooldown is 5 minutes (D160). The safety part recovers 2 minutes after a hit.
- Young do not count toward the group size until they are adult.

**Sources, in order of weight:** the lead decisions of 2026-10-12 and section 11.6, 11.9, 11.10, 11.12 and 11.16 of the codebase analysis (decision). Then the primary design sources: the design doc (Project Nublar (2).docx, sections "Basic Genes", "Stress: Basic comfort", "Social", "Group Size", "Domesticity", "Intelligence", "Aggression", "Diurnal/Nocturnal/Cathemeral", "Hunger/Stomach Capacity", "Eat Rate", "Energy", "Thirst", "Special Diet Genes", "Special Genes", "Byte Values", "Dino vs Player", "Scent Markers", "Detection Levels"), Mod Commission ("Dinosaur behavior", "Needs", "Genes/Stats"), the species cards (Tyrannosaurus.docx, Velociraptor.docx: "AI", "DNA Modification Effects"), the 2024 board "AI" card, the 2020 board creature and feeder cards, the MVP board species cards, the art board animation cards, Dino VFX, Special Genes, Vanilla Animal DNA Effects, GH #6, #59, #61, #64. Then the herd decisions 1 to 33. Surveys, the reconciliation, the reference mods and the reference games are implementation help only. A value that only they supply is marked "proposal, lead decides" and cites them after it.

**Source tags in this section:** (lead 2026-10-12) = a lead decision of that date. (decision, section 11.x). (design doc, section name). (Mod Commission). (species card). (2024 board, AI card). (2020 board). (MVP board, card). (art board, card) = the GitHub asset task board. (GH #n). (Dino VFX). (Special Genes). (DNA Effects) = Vanilla Animal DNA Effects.txt. (herd decision n). (plan) = a value already in the engineering plan. (plan default Dnn) = a default the lead has not confirmed. (reconciliation). (reference mods) = the Entity AI and behaviour comparison report. (reference games) = the "Reference game AI & Genetic Design Proposals" tab (Jurassic World Evolution, Planet Zoo, Prehistoric Kingdom). (proposal, lead decides) = this page suggests it. In tables, the Source column drops the brackets.

**Time.** One in-game day is 24,000 ticks, 20 real minutes. Day time 0 is 06:00 on the in-game clock, 6,000 is noon, 12,000 is sunset (18:00), 18,000 is midnight.

#### What a dinosaur can be doing

The spine of this list is the 2024 board "AI" card. The Mod Commission "Behaviors" list repeats it and adds Breeding. The species cards add Scavenging, Play, Call, Group Feeding, Mating and Nesting. Each item maps to one activity in player words. "In" means Beta has it by section 11. "Out" means section 11 or the Beta species list leaves it out.

| AI card item | Activity (player words) | Beta | Starts when | Ends when | Source |
| --- | --- | --- | --- | --- | --- |
| Hunting: Solo | Hunting | In | A hungry adult carnivore with no hunt cooldown and stamina above a third picks prey by clade | Prey dies, prey is lost, or stamina reaches zero. A predator that loses sight of its prey first searches the last seen spot. A hunt cooldown follows | 2024 board, AI card. Species card. Decision, section 11.10. Plan (stamina line). Lead 2026-10-12 (search) |
| Hunting: Packs | Hunting with the pack | In (Velociraptor pack, T. rex pair) | The pack leader picks prey. Members take flank places | The leader's chase ends, prey is lost, or under half the members still chase | 2024 board, AI card. Species cards. Decision, section 11.10. Lead 2026-10-12 (D55) |
| Scavenging (species cards) | Feeding at a carcass or dropped meat | In. Mates share the carcass by feeding spots (see "Feeding together") | A hungry carnivore finds a carcass or meat within reach | Full, or the carcass is empty | Species cards. Decision, section 11.10. Lead 2026-10-12 |
| Foraging | Grazing and browsing | In (herbivores). The herd gathers at one tree or plant patch, and each animal claims a different block (see "Feeding together") | A hungry herbivore finds a free diet block in its height band: low plants for Triceratops, high leaves for Brachiosaurus | Full. The cooldown starts | 2024 board, AI card. Decision, section 11.10. Lead 2026-10-12. Herd decision 12 |
| Herding: Herds | Travelling with the herd | In | The leader starts a journey every 2 to 5 minutes, or a need pulls it outside the herd area | The leader arrives and the herd settles to graze | 2024 board, AI card. Herd decisions 3, 16, 25 |
| Herding: Flocking | Keeping together (spacing, wedge, single file) | In for herds and packs. Flying flocks out (Pteranodon not in Beta) | Always while in a group | Never | 2024 board, AI card. Herd decisions 1, 10, 24. Decision, section 11.9 |
| Herding: Packs | Pack life between hunts | In (Velociraptor 3 to 6) | Always while in a pack. A hungry pack journeys to find prey. Otherwise it roams its home area near its leader (D56). It defends no home territory (D166) | Never | 2024 board, AI card. Decision, section 11.10. Lead 2026-10-12 (D56), lead 2026-10-13 (D166) |
| Herding: Dominance/Group Leaders | Rank contest | In. No damage. Turf wars out. A pack that loses its leader flees to safety first. Then the two members with the highest dominance contest the lead | A member passes the leader in dominance (size, age, aggression gene), or the leader dies | One display each. Higher dominance leads | 2024 board, AI card. Decision, section 11.10, 11.13. Lead 2026-10-12 (pack collapse) |
| Herding: Communication | Calling (warn, gather, threaten) | In | A threat, a journey start, a contest, a lost mate | The call clip ends | 2024 board, AI card. Decision, section 11.10. Herd decision 26 |
| Call (species cards) | Contact call and ambient call | In as calls. Rates per species (D101). Babies call twice as often (D108) | See "A healthy dinosaur" | The clip ends | Species cards. Decision, section 11.10. Lead 2026-10-12 |
| Drinking | Drinking | In | Thirst falls below the seek line | Full. The cooldown starts | 2024 board, AI card. Decision, section 11.9 (head contact), 11.10 |
| Eating | Eating (feeder, item, block, carcass) | In. Feeders are shared by all animals of their diet | Hunger falls below 75 % | Full. The cooldown starts | 2024 board, AI card. Decision, section 11.10. Plan. Lead 2026-10-12 |
| Group Feeding (species cards) | Eating together and waiting its turn at a feeder, carcass or plant source | In for all species (lead 2026-10-12). The source's size and the eaters' body widths set how many eat at once. Within a group the higher dominance eats first, then the longest wait (D57, now for all species) | Mates reach one food source | Its turn ends or the source is empty | Species cards. Lead 2026-10-12. Decision, section 11.10 (dominance) |
| Idles | Idling (standing, looking, sniffing, scratching, short sits). Herbivores also graze (see "Feeding together", idle grazing) | In. Section 11 is silent. The clips are on the 2020 board and the species cards. Idle grazing: lead 2026-10-12 | Nothing above applies | Any other activity starts | 2024 board, AI card. Species cards ("Idles"). 2020 board. Lead 2026-10-12 |
| Sleeping | Sleeping | In | Inside its sleep window, sleep need low, on dry ground, no threat near, not starving | Window ends, damage, a threat in reach | 2024 board, AI card. Decision, section 11.10. Design doc, "Diurnal/Nocturnal/Cathemeral". Plan |
| Sleeping: Resting | Resting (lying down awake) | In | Tired, or a herd rest pause | Stamina recovers, or the leader gets up | 2024 board, AI card. Decision, section 11.10. Herd decision 28 |
| Combat: Attacking | Fighting | In | A target after a warning, or a hit on the animal | The target dies or leaves, or stamina reaches zero | 2024 board, AI card. Decision, section 11.10 |
| Combat: Defense | Fighting back, guarding young and nest | In. Eggs count as young: a parent that sees a player near its eggs warns and attacks (lead 2026-10-13) | The animal or its young is hurt or threatened | The threat leaves | 2024 board, AI card. Decision, section 11.10 |
| Combat: Threatening | Warning display | In. Every species warns before it attacks | A player or rival the animal will attack | The clip ends. Then it attacks, or stops if the target left | 2024 board, AI card. Decision, section 11.10 |
| Combat: Aggression | Aggressive mood | In | Unmet needs or high stress, a player in its personal space, a player near its young, the aggression gene | Needs met and stress back down | 2024 board, AI card. Decision, section 11.10. Design doc, "Stress: Basic comfort", "Dino vs Player". Lead 2026-10-12 (personal space) |
| Combat: Fleeing | Fleeing | In (Gallimimus. Babies. A pack below its minimum flees big threats. A pack that lost its leader flees to safety first. A standing animal retreats from an attacker with more than 2 × its threat. A carnivore adds its group's threat to its own, D13). The Brachiosaurus defends and does not flee | A threat comes too near. A fleeing species flees when a noticed player enters its personal space, with no warning (D218) | The threat is far, or the flee time ends | 2024 board, AI card. Decision, section 11.10. Lead 2026-10-12 (D13, D50, D112, D218, pack collapse) |
| Taming | Tame commands: follow, stay, defend | In | Trust reaches the taming line. Trust rises while a player is near a calm animal, and hand-feeding gives a big boost. Trust rises only while stress is below 20 | Hunger or stress breaks the bond | 2024 board, AI card. Decision, section 11.10. Lead 2026-10-12 (trust rule) |
| Neutral | Calm mood | In as the mood between passive and aggressive | Needs met, stress low | Stress rises, or a threat | 2024 board, AI card. Design doc, "Stress: Basic comfort" (passive, neutral, aggressive) |
| Movement: Walking, Running, Sprinting | Walking, running, sprinting | In | Every move | n/a | 2024 board, AI card. Decision, section 11.9, 11.10 |
| Movement: Swimming | Swimming and wading | In. Small animals go round deep water, large ones wade | Water deeper than its height on its path | Back on land | 2024 board, AI card. Decision, section 11.10. GH #61 |
| Movement: Fully aquatic swimming | None | Out. No aquatic Beta species | n/a | n/a | 2024 board, AI card. Decision, section 11.9 |
| Movement: Flight | None | Out. Pteranodon is not in Beta | n/a | n/a | 2024 board, AI card. Decision, section 11.9 |
| Movement: Jumping | Jumping and falling | In | A ledge, the jump gene | Landing | 2024 board, AI card. Decision, section 11.6 |
| Socializing | Socialising: contact calls, greeting a mate, keeping company | In as calls. Loneliness is a cause of stress, not a need of its own (D52) | Mates near, idle time | Another activity starts | 2024 board, AI card. Decision, section 11.10 |
| Play (species cards) | Play: content young animals chase each other | In. No damage. Existing clips only (run, Velociraptor pounce). No new clip | Two content young of one species are near each other in idle time (proposal, lead decides) | Another activity starts | Species cards. Lead 2026-10-12 (D107) |
| Breeding (Mod Commission), Mating (species cards) | Courting and mating | In | A calm male and female, a built nest or a free laying spot near, the fertility roll passes | Mating done. Both get a cooldown. Two more gates: full health, and no own young under half grown within 16 blocks (D72) | Mod Commission. Species cards. Decision, section 11.10. Lead 2026-10-12 (D72) |
| Nesting (species cards) | Nest building and egg laying | In. The female builds a nest from vanilla blocks of her surroundings, then lays her eggs in it (F2.5) | A mated female, 1 in-game day (24,000 ticks) after mating | The clutch is laid | Species cards. Decision, section 11.10. Design doc, "Gender". Lead 2026-10-12 (gestation), lead 2026-10-13 (nest) |
| Baby/Infant | Following a parent, guarded by adults | In | Hatch | Grown past the young band | 2024 board, AI card. Decision, section 11.9, 11.10 |

**Which activity wins.** When two activities can start, the higher rung runs (plan). The "always on" layer runs beside every rung: looking, moving, following a parent, joining a group, and the need and stress tick.

| Rung | Activity | Why it is here | Source |
| --- | --- | --- | --- |
| 1 | Fleeing | A flee species or a baby that does not run first dies first | Decision, section 11.10. Plan |
| 2 | Fighting (warning, attack, guarding young) | Damage and a threat to young beat every need | Decision, section 11.10 |
| 3 | Hunting | A hungry carnivore with stamina | Decision, section 11.10. Plan |
| 4 | Eating and drinking | Hunger and thirst | Decision, section 11.10 |
| 5 | Resting and sleeping | Stamina and the sleep window | Decision, section 11.10. Plan |
| 6 | Travelling with the herd | The leader's journey | Herd decisions 3, 25. Plan |
| 7 | Courting, mating, nesting | Only a calm animal breeds | Decision, section 11.10 |
| 8 | Socialising (calls, rank contests, greeting) | Group life when nothing is urgent | Decision, section 11.10 |
| 9 | Idling | Fills all other time | 2020 board. Species cards. Plan |

Rules that cut across the ladder:

- A follower on a journey leaves the line to eat or drink only when the need is urgent (herd decision 8). It rests on a roaming journey only at critical stamina, and on an urgent trip whenever tired (herd decision 17).
- A herd member may detour for a need. A member with an urgent thirst or hunger (below 25 %) steps off to drink or eat, then rejoins. Fight chases are the other exception to herd decision 16 (lead 2026-10-13, D36).
- An animal that is starving or very thirsty (below 25 %) does not sleep (plan). A threat in reach stops sleep and wakes a sleeper (plan).
- A tame "stay" blocks journeys, social moves and breeding only. The animal may walk to food and water, then it returns to its spot. It sleeps on its spot (lead 2026-10-12, D108).
- A tame animal keeps its species rhythm. It sleeps in its window and calls. Follow pauses while it sleeps (lead 2026-10-12, D108).
- A predator that loses sight of its target walks to the last seen spot and searches there. Beta has no scent trails (lead 2026-10-12).
- A herd member that is startled writes a herd alarm, so the herd flees as one (herd decision 26, plan). Alarm calls cross species: any herbivore that hears one becomes alert and reacts by its own rule. Groups never mix (lead 2026-10-12, D63).

**Diagram to draw (state ladder).** A later step draws this. Boxes and arrows:

- Box A, at the left and full height: "Always on: look, move, follow parent, join group, need and stress tick".
- Ladder boxes from top to bottom: 1 Fleeing, 2 Fighting, 3 Hunting, 4 Eating and drinking, 5 Resting and sleeping, 6 Travelling with the herd, 7 Courting and nesting, 8 Socialising, 9 Idling. A thick arrow down the right side reads "higher rung wins".
- Inside box 2, three small boxes in a row: "Warning display" → "Attack" → "Recovery". An arrow from "Warning display" to box 9 reads "target left the personal space: no attack".
- Inside box 5, three small boxes: "Rest" → "Sleep" → "Get up". An arrow from "Sleep" to box 1 reads "damage or threat wakes it".
- Arrow 9 → 4: "hunger below 75 %, or thirst below the seek line". Arrow 4 → 9: "full, cooldown starts".
- Arrow 9 → 3: "hungry carnivore, no hunt cooldown, stamina above a third". Arrow 3 → 4: "kill made: eat". Arrow 3 → 5: "stamina zero: chase ends, rest".
- Arrow 9 → 6: "leader starts a journey". Arrow 6 → 9: "arrived, herd grazes". Arrow 6 → 4: "need urgent: leave the line".
- Arrow 9 → 5: "tired, or sleep window". Arrow 5 → 9: "rested, window ends".
- Arrow 9 → 7: "calm pair, nest near, roll passed". Arrow 7 → 9: "eggs laid, cooldown".
- Arrow 9 → 8: "rival or mate near, call due". Arrow 8 → 9: "clip ends".
- Arrow from any box → 1: "threat near (flee species, baby)". Arrow from any box → 2: "hurt, young threatened, player in personal space".
- A side note on box 4: "starving: health falls, aggressive, never attacks a fence".
- Arrow 9 → 8 also covers play: "content young near each other: chase, no damage".

#### A healthy dinosaur

The lead asked what an ideal, managed, needs-met dinosaur looks like. This is the definition. Every part must hold at once.

| Part of "needs met" | Healthy when | Source |
| --- | --- | --- |
| Hunger | Never below 25 %. Back above 75 % after each meal, then a cooldown with no food search | Plan (75 % and 25 % lines). Decision, section 11.10 (cooldown) |
| Thirst | Never below 25 %. Back above 75 % after each drink | Lead 2026-10-12 (D104: seek at 75 %, run at 25 %) |
| Stamina | Above a third outside a chase. A herd leader is at 75 % or more when it starts a journey | Plan. Herd decision 29 |
| Sleep | Sleeps in each of its sleep windows each in-game day, and is not woken by damage or a threat | Decision, section 11.10. Plan. Lead 2026-10-12 (D97, D98) |
| Company | Its group has at least the species minimum, near it: T. rex 1, Velociraptor 3, Dilophosaurus 1, Gallimimus 6, Triceratops 4, Brachiosaurus 3. "Near" means inside the herd area, or within 16 blocks for a pack or pair | Design doc, "Group Size" ("minimum size ... to have low stress"). Decision, section 11.16. Herd decision 25. Proposal, lead decides (16 blocks, as the plan's hatchling search) |
| Stress | In the calm band all the time | Decision, section 11.10. Plan |
| Food reachable | A diet source it can reach with its head: plants in its height band, a feeder of its kind, or carcasses and meat. It reaches the source by a path its body fits. At a shared source it gets a feeding spot or a free block within its wait time (see "Feeding together") | Decision, section 11.9 (head contact), 11.10. Lead 2026-10-12 |
| Water reachable | Water whose surface its head reaches, from a stand point it can walk to | Decision, section 11.9 |
| Safe | No hunter or larger animal of another species targets it within 10 × 5 × 10 blocks. It was not hurt in the last 2 minutes | Plan (sleep threat box, 2,400 ticks). Lead 2026-10-13: the 2 minutes stay. The D61 place memory (1 in-game day) is a separate rule |
| Health | Full or regenerating, above the limp line (30 % health) | Lead 2026-10-12 (D92) |
| Contentment | In the content band (see "Contentment") | Lead 2026-10-12 (D110) |

**Stress bands.** The plan sends three stress bands to the client: calm, uneasy, stressed. The lead set the threshold on 2026-10-12 (D13, D88, D99). Each species has one threshold in its species data, default 50, with no gender split. Higher stress is worse. The design doc's "keep above 50" is a typo. Stressed is at the threshold and above, uneasy from 20 below it, calm under that. With a threshold of 50, calm is 0 to 29, uneasy 30 to 49 and stressed 50 to 100. Playtests tune the values.

**What "healthy" looks like over a day.** One table per species. Daytime 06:00 to 18:00, night 18:00 to 06:00. The lead set the active hours on 2026-10-12 (D97, D98):

- The three herbivores are diurnal. They sleep 22:00 to 06:00 (16,000 to 24,000 ticks, plan).
- T. rex, Velociraptor and Dilophosaurus are cathemeral. Each species has its own two windows, in its table below. They are config defaults, and playtests tune them.
- Each animal rolls a fixed offset of up to ±30 minutes (500 ticks) at hatch. A herd or pack uses its leader's offset, so the group sleeps together. A shifted herbivore is the exception (below).
- A herbivore that the nocturnal gene shifts one step gets cathemeral windows of its own species. It keeps its own windows and naps alone, also in a herd (lead 2026-10-13, D182).
- The nocturnal window is 10:00 to 18:00 (4,000 to 12,000 ticks, 8 h), the same total as a diurnal sleep (lead 2026-10-13, D252).

The old cathemeral default had the name "dawn and dusk", but its values were midnight and noon. The per-species windows replace it. Idle clips name only clips that exist or that the Art list already requires. Call rates come from the lead (D101), and playtests tune them.

**Windows of shifted herbivores (lead 2026-10-13, D182).** Config start values, and playtests tune them.

| Species | Night window | Day window | Total sleep |
| --- | --- | --- | --- |
| Triceratops | 00:00 to 04:00 (18,000 to 22,000) | 11:00 to 13:00 (5,000 to 7,000) | 6 h |
| Brachiosaurus | 01:00 to 04:00 (19,000 to 22,000) | 15:00 to 16:00 (9,000 to 10,000) | 4 h |
| Gallimimus | 23:00 to 03:00 (17,000 to 21,000) | 09:30 to 11:00 (3,500 to 5,000) | 5.5 h |

A shifted Triceratops grazes in the late evening and at dawn, and rests through the midday heat. The largest herbivore sleeps least, because it browses for most of the day.

##### Tyrannosaurus (healthy routine)

| Aspect | Healthy behaviour | Source |
| --- | --- | --- |
| Active hours | Cathemeral. Sleeps 22:00 to 03:00 and naps 13:00 to 15:00 (16,000 to 21,000 and 7,000 to 9,000 ticks). Config default, and playtests tune it | Lead 2026-10-12 (D97, D98) |
| Daytime | Patrols its paddock at a walk. Naps 13:00 to 15:00. Stops to sniff the air and the ground. Eats at a feeder or carcass when hunger falls below 75 %, then ignores food during the cooldown. Drinks once or twice a day. A pair shares one feeder or carcass. Both eat at once if the source has room for two bodies. If not, the leader eats first | Species card ("AI": solo, pair, scavenging, group feeding). Decision, section 11.10. Plan. Lead 2026-10-12 |
| Night | Lies down and sleeps 22:00 to 03:00 with Zzz and bubbles. Snow cover in snow biomes. Wakes with a yawn and a shake, and patrols until dawn | Dino VFX. Lead 2026-10-12 (D98) |
| Alone or in its group | Alone is normal: group minimum 1. A pair keeps within 16 blocks and hunts as a team: one chases, and one circles to cut off the prey | Species card (group 1 to 2). Decision, section 11.16. Lead 2026-10-12 (D54) |
| Idle behaviours | Idle, look left and right, sniff air, sniff ground, shake head, shake body, scratch, speak, startled, breathing, short sit (rest), yawn, sneeze | Species card ("Idles"). 2020 board. Art list (yawn and sneeze required) |
| Calls | Rare. A growl or call about every 5 minutes. A full roar only as a warning or contest | Design doc (group tendency 2 to 3). Decision, section 11.10. Lead 2026-10-12 (D101) |
| Pace | Slow, heavy walk (speed 0.50). Stalks at half pace with its stalk clip, sprints at 8 blocks. Screen shake near the player | Species card. Dino VFX. Lead 2026-10-12 (D59) |
| Player near | Rolls to notice the player by distance and activity. A noticed player inside its personal space (10 blocks) gets a roar warning, then an attack. A noticed player near its young provokes it at any distance it can see. A player who leaves during the roar is not hit | Decision, section 11.10. Design doc, "Detection Levels". Plan. Lead 2026-10-12 (personal space) |

##### Velociraptor (healthy routine)

| Aspect | Healthy behaviour | Source |
| --- | --- | --- |
| Active hours | Cathemeral. Two naps: 02:00 to 05:00 and 12:00 to 14:30 (20,000 to 23,000 and 6,000 to 8,500 ticks). Config default, and playtests tune it | Lead 2026-10-12 (D97, D98) |
| Daytime | The pack roams behind the leader in a wedge and naps together 12:00 to 14:30. Members sniff, look around and keep a few blocks apart. The pack shares one feeder or carcass. The leader eats first, then the others by rank and by wait time, as many at once as the source has spots | MVP board, Velociraptor card ("AI", "Group Feeding"). Decision, section 11.10. Lead 2026-10-12 (D57) |
| Night | Pack members lie down near each other and nap together 02:00 to 05:00, on the leader's offset. Zzz and bubbles | Dino VFX. Lead 2026-10-12 (D98, the group sleeps together) |
| Alone or in its group | Needs 3 or more. A lone raptor calls often, paces and gets stressed. It joins any raptor pack in range | Design doc, "Group Size". Decision, section 11.16. Plan |
| Idle behaviours | Idle 1, idle 2, look left and right, look around, sniff air, sniff ground, shake head, shake body, scratch, speak, sit, sneeze, yawn, startled | Species card ("Idles"). MVP board, Velociraptor card. 2020 board. Art list. Lead 2026-10-12 (T. rex card extras for every species) |
| Calls | Chatty. A soft contact call between mates about every minute. Screech only as a warning, with its own clip and sounds | Design doc (group tendency 6 to 9). Decision, section 11.10. Lead 2026-10-12 (D101, own screech) |
| Pace | Quick and light (speed 0.65). Creeps at half pace with its walk clip, then pounces at 8 blocks with its pounce clip. A hungry raptor also hides in cover by a path and springs out | Decision, section 11.16. MVP board ("Pounce"). Lead 2026-10-12 (D53, D59, D60) |
| Player near | The pack notices together. Only the leader screeches the warning. Members hold flank places. A player who leaves during the warning is not hunted. Personal space 6 blocks. Every raptor hatches with hyper intelligence: it opens doors, raids chests and climbs out at a dead fence section | Decision, section 11.10. Proposal, lead decides (reconciliation P7). Lead 2026-10-12 (personal space, D43, D46) |

##### Dilophosaurus (healthy routine)

| Aspect | Healthy behaviour | Source |
| --- | --- | --- |
| Active hours | Cathemeral. Two naps: 08:00 to 11:00 and 14:00 to 16:00 (2,000 to 5,000 and 8,000 to 10,000 ticks). Config default, and playtests tune it | Lead 2026-10-12 (D97, D98) |
| Daytime | Naps 08:00 to 11:00 and 14:00 to 16:00 in a quiet spot, often near water. Between the naps it drinks and waits still near water | Lead 2026-10-12 (D98) |
| Night | Walks its paddock, sniffs, waits still near water or a feeder. Eats at the feeder or a carcass. A hungry Dilophosaurus hides in cover by a path and springs out at prey | 2020 board ("Curious"). Lead 2026-10-12 (D60) |
| Alone or in its group | Alone is normal: group minimum 1. Up to 3 share a paddock and hunt together | Decision, section 11.16. Lead 2026-10-12 (D54) |
| Idle behaviours | Idle 1, idle 2, curious, speak, look left and right, sniff air, sniff ground, head shake, shake body, scratch, startled, call 2, sit, yawn, sneeze | 2020 board. Art list. Lead 2026-10-12 (T. rex card extras for every species) |
| Calls | Hoots and soft calls about every 2 to 3 minutes | Design doc (group tendency 3 to 5). Decision, section 11.10. Lead 2026-10-12 (D101) |
| Pace | Medium (speed 0.55). Stands still for long spells | Decision, section 11.16 |
| Player near | Personal space 6 blocks. Plays its intimidate display with the frill open and hoots as a warning. If the player stays, it spits at up to 10 blocks. The spit blinds. It bites a blinded player | Decision, section 11.10 ("a frill"). Dino VFX. Plan. Lead 2026-10-12 (intimidate, then spit) |

##### Triceratops (healthy routine)

| Aspect | Healthy behaviour | Source |
| --- | --- | --- |
| Active hours | Diurnal | Herd decision 28 (herds rest at night). Lead 2026-10-12 (D97) |
| Daytime | Grazes low plants in its herd area. Holds a spot for 30 to 90 s, then moves a short way. The herd gathers at one plant patch or one feeder, and each animal eats its own block or spot. Travels behind the leader every 2 to 5 minutes. Drinks with the herd | Decision, section 11.10. Herd decisions 3, 6, 12, 25. Lead 2026-10-12 |
| Night | No journeys from sunset. Members spread out, lie down one by one and sleep 22:00 to 06:00 | Herd decisions 28, 29. Plan |
| Alone or in its group | Needs 4 or more. Below that it calls, wanders to find a herd and gets stressed | Design doc, "Group Size". Decision, section 11.16 |
| Idle behaviours | Idle 1, idle 2, graze, eat 2, drink 2, look left and right, sniff air, sniff ground, head shake, shake body, scratch, startled, speak, sit (own clip), snort, yawn, sneeze. Idle grazing: a content Triceratops grazes a low plant or grass block in its reach with its graze clip. Each idle graze adds a little hunger and leaves the block in place | 2020 board. Art board, Triceratops Animations. Art list. Dino VFX. Lead 2026-10-12 (idle grazing) |
| Calls | A call or snort about every 2 to 3 minutes. Calls at a journey start | Design doc (group tendency 6 to 7). Herd decision 26. Lead 2026-10-12 (D101) |
| Pace | Slow, heavy (speed 0.35). Graze walk 0.6, journey 0.8, catch-up 1.2 of base | Decision, section 11.16. Herd decision 19 |
| Player near | Turns its head to a still player within 8 blocks. Its personal space is 8 blocks. A player who threatens it, its young or its herd, or who enters its personal space, gets a roar, a ram wind-up and a charge | Decision, section 11.10. Plan. Lead 2026-10-12 (personal space) |

##### Brachiosaurus (healthy routine)

| Aspect | Healthy behaviour | Source |
| --- | --- | --- |
| Active hours | Diurnal | Herd decision 28. Lead 2026-10-12 (D97) |
| Daytime | Browses high leaves or the tall feeder, rears on its hind legs to reach. The herd spreads around one tall tree, and each animal takes its own high leaf blocks. Stays in open ground. Travels slowly with its herd | 2020 board ("Stand on hindlegs Eat"). Decision, section 11.10. 2020 board, tall feeder note. Lead 2026-10-12 |
| Night | Rests in open ground and sleeps 22:00 to 06:00 | Herd decision 28. Plan |
| Alone or in its group | Needs 3 or more. A lone animal broadcasts calls and gets stressed | Design doc, "Group Size". Decision, section 11.16 |
| Idle behaviours | Idle 1, idle 2, look left and right, low look left and right, sniff air, sniff ground, shake head, shake body, scratch, startled, speak, drink 2, sit (own clip), yawn, sneeze. Idle grazing: a content Brachiosaurus browses a high leaf block in its reach with its eat clip. Each idle graze adds a little hunger and leaves the block in place | 2020 board. Art list. Lead 2026-10-12 (idle grazing) |
| Calls | Deep broadcast calls about every 2 to 3 minutes, which carry far | Design doc (group tendency 5 to 7). Decision, section 11.10. Lead 2026-10-12 (D101) |
| Pace | Very slow (speed 0.25). Ripples in water and screen shake nearby | Decision, section 11.16. Dino VFX |
| Player near | Mostly ignores players: aggression 1 to 2. Looks at a still player within 8 blocks. Personal space 10 blocks. Defends against a threat: a roar warning (its bellow), then a tail whip and a stomp. It does not flee | Design doc, "Byte Values". Plan. Lead 2026-10-12 (D13, D50) |

##### Gallimimus (healthy routine)

| Aspect | Healthy behaviour | Source |
| --- | --- | --- |
| Active hours | Diurnal | Herd decision 28. Lead 2026-10-12 (D97) |
| Daytime | The herd grazes spread over its herd area: 14.4 blocks across for 5 animals. Each keeps a spot 30 to 90 s. At a feeder or a plant patch the herd gathers, and each animal takes a different spot or block. The leader moves every 20 to 40 s and starts a journey every 2 to 5 minutes with a call. The leader stands sentry while the herd grazes. As an omnivore it also eats dropped meat and eggs that it finds, and it uses both feeder kinds | Herd decisions 6, 12, 25, 26. Herd spec. Lead 2026-10-12 (omnivore) |
| Night | No journeys from sunset. Members leave their slots, spread out and lie down. They settle one at a time | Herd decision 28. Plan. Reference mods (drowsy and rouse rolls) |
| Alone or in its group | Needs 6 or more. A small herd calls often and is easily startled (proposal, lead decides) | Design doc, "Group Size". Decision, section 11.16 |
| Idle behaviours | Idle 1, idle 2, ground peck, look left and right, sniff air, sniff ground, head shake, shake body, scratch, speak, startled, sit, sentry (leader), burp, yawn, sneeze. Idle grazing: a content Gallimimus pecks a grass or plant block in its reach with its ground peck clip. Each idle graze adds a little hunger and leaves the block in place | 2020 board. Art list. Lead 2026-10-12 (idle grazing) |
| Calls | Frequent chirps between mates about every minute, a call at each journey start | Design doc (group tendency 7 to 10). Herd decision 26. Lead 2026-10-12 (D101) |
| Pace | Fast and light (speed 0.75). Graze walk 0.6, journey 0.8, catch-up 1.2 | Decision, section 11.16. Herd decision 19 |
| Player near | Startled at 6 blocks. Flees a noticed player inside its personal space (5 blocks). One startled animal makes the whole herd run | Decision, section 11.10. Herd decision 26. Plan |

**Common to all six.** Babies take one extra nap each day and a longer night sleep, and they call twice as often (lead 2026-10-12, D108). Content young play: they chase each other with no damage (D107). A visitor who watches a healthy animal for 2 minutes sees at least 3 different acts. No two herd mates do the same act on the same tick (proposal, lead decides: reference mods). A content herbivore looks at a still player within 8 blocks (plan). A content carnivore looks only after it notices the player (proposal, lead decides: reference mods).

#### Feeding together

The lead decided on 2026-10-12 that animals share their food. Herbivores and carnivores gather around one food source. A park never needs one feeder per animal. The lead's words: "herbivores should be able to congregate around one tree". The 2020 board already names one herbivore feeder for several species (Triceratops, Parasaurolophus, Gallimimus) and one carnivore feeder for "all carnivore animals". The species cards list "Group Feeding" under socialising.

**Rules for every food source** (lead 2026-10-12):

| Source | Rule | Source of the rule |
| --- | --- | --- |
| Feeders (all kinds, all species) | A feeder is shared. Hungry animals of its diet gather around it. Each eater takes a feeding spot on an open side of the feeder: a stand point from which its head touches the tray (section 11.9). The feeder's size and the eaters' body widths set how many eat at once. The others wait nearby and take turns | Lead 2026-10-12. Decision, section 11.9, 11.12 |
| Turn order | Within a group, the higher dominance eats first, so the leader eats first. Then the longest wait goes first. This is D57, now for every species and every source. Between groups and between species, the longest wait goes first (D111) | Lead 2026-10-12. Decision, section 11.10 (dominance) |
| Claims | A claim on a feeder spot frees after 5 s (100 ticks) if the animal has not arrived. A claim on a plant block frees after 10 s (200 ticks) | Lead 2026-10-13 (D111) |
| Herd meals | Hybrid, queue first. The herd travels to one food source together. Each member claims its own spot there. A member with no spot waits up to the 30 s turn limit, then may use other food inside the herd area. All regroup after the meal | Lead 2026-10-13 (D111) |
| Turn end | The eater leaves its spot when it is full (hunger back above 75 %). If others wait, it also leaves after a turn limit, 30 s (D111). It then waits again if it is still hungry. The need cooldown starts when it is full | Lead 2026-10-12. Decision, section 11.10 (cooldown). Plan (75 %) |
| Waiting | A waiting animal stands within 6 blocks of the source, outside the feeding spots and the walk lanes to them, and plays idle acts (D111) | Lead 2026-10-12. The old D57 default (6 blocks) |
| Natural food (trees, plant patches) | A herd gathers around one tree or plant patch. Each animal claims a different leaf or plant block inside its reach band. No two animals target the same block. The animals spread around the source and keep herd spacing. Brachiosaurus takes the high leaves, smaller animals take the low plants | Lead 2026-10-12. Decision, section 11.10 (reach). Herd decisions 12, 24 |
| Herd decision 12, extended | Members avoid neighbours' spots while grazing (herd decision 12). Now they also avoid neighbours' feeding targets: a block that a mate claims, and a stand point inside a mate's spacing | Lead 2026-10-12. Herd decision 12 |
| Omnivore | The Gallimimus is an omnivore. It eats plants and small meat: dropped meat and eggs. It uses the herbivore feeders and the carnivore feeders, and it takes turns there like any eater | Lead 2026-10-12 |
| Carcasses | The carnivore rules stay: scavenging, rank order, and a starving animal that takes its own species only outside its group and family (D62). The carcass uses the same spot model as a feeder: its body box sets its edge | Lead 2026-10-12. Decision, section 11.9, 11.10 |
| No fights at the source | A waiting animal never pushes an eater off a spot and never attacks a mate for food. Turf wars are not in Beta | Decision, section 11.10, 11.13 |
| Idle grazing | A herbivore that is content or idle plays its graze behaviour (its eat clip) at a grass or plant block in its reach. Each idle graze adds a small hunger gain, `needs.idle_graze_hunger` (proposal 1 % of the hunger maximum, lead decides). By default an idle graze does not remove the block, so a content herd does not strip its pasture. A hungry animal still eats and removes the block (section 11.10). An idle grazer uses the same block claims as a hungry eater, so no two animals use one block | Lead 2026-10-12. Decision, section 11.10 |

**How many eat at once.** Each eater takes a stretch of the source's open edge. The stretch is its body width plus a gap of 0.5 blocks (lead 2026-10-12, D111). A spot claim lasts 5 s and a block claim 10 s until the animal arrives (lead 2026-10-13, D111). Body width is the herd fit box width (herd decision 5): the Gallimimus is 1.25 blocks wide, so it takes 1.75 blocks of edge. The source takes eaters while free edge is left. Mixed sizes share one edge in the same way. An edge that faces a wall, a fence or deep water has no spots.

| Example (D111 values) | Open edge | Eater | Eat at once |
| --- | --- | --- | --- |
| Low Herbivore Feeder in the fence line, one long side open | 5.25 blocks (Feeder 5 model width, from the model file, to check in the task plan) | Gallimimus, 1.75 blocks each | 3 |
| Same feeder, standing free, three sides open | 5.25 + 4.88 + 4.88 blocks (model bounds) | Gallimimus | 3 + 2 + 2 = 7 |
| Very Tall Herbivore Feeder, one long side open | 5.25 blocks (Feeder 6) | Adult Brachiosaurus, 3.68 wide (movement box, the fit box may be wider) + 0.5 | 1 |
| One oak, low leaves and grass around it | Not an edge: one block per animal | Gallimimus herd of 6 | 6, each on its own block |

**What the player sees.** Eight Gallimimus at one Low Herbivore Feeder: three eat side by side, five stand nearby, sniff and look around. When one eater is full, it walks off and the next one steps in. The leader ate first. At a tall tree, a Brachiosaurus herd stands around the crown, each animal with its head in a different part of the leaves. A fed Gallimimus herd grazes through the afternoon. Each animal pecks at its own grass block, its hunger rises slowly, and the grass stays.

Engineering: feeding spots and the wait queue live on the feeder registry (F2.4, F3.3). Block claims with an expiry live in the F2.4 grazing search. F2.0 owns the spot model and the order rule. F2.5 part B supplies dominance. See the backlog F2.0 "In scope, feeding together".

#### When needs fail

Each need has four bands: satisfied, wants, urgent, harmful. Section 11.10 sets the results of a need the park does not supply: the animal turns aggressive, then loses health and dies. It never attacks a fence. Only a T. rex breaks a fence, and only an unpowered one, at any need level, 50 % per check (decision, section 11.10, fence parity F29). A hyper-intelligent animal climbs out at a dead fence section (lead 2026-10-12, D46). One stress step is 1 minute, 1,200 ticks. Each active cause adds 2 stress per step (lead 2026-10-12). Playtests tune both. "Not set" values are open questions.

**Hunger**

| Band | Range | Behaviour change | Visible signal | Source |
| --- | --- | --- | --- | --- |
| Satisfied | 75 to 100 % | No food search. After a meal, a cooldown with no search: 5 minutes, 6,000 ticks (config, D160). An urgent need skips it | Normal idle pool. Plant bits or a bloody mouth fade after the meal | Decision, section 11.10. Plan. Dino VFX |
| Wants | 25 to 75 % | Seeks food. Source order: dropped meat or items, carcasses, feeders, diet blocks. A carnivore hunts if its hunt cooldown is over and its stamina is above a third. Stress +2 per step | Sniffs air and ground more. Walks to the feeder. A carnivore stalks | Plan. Design doc, "Hunger/Stomach Capacity" ("increase stress", "seek food") |
| Urgent | Below 25 % | Runs to food. Does not sleep. Leaves the herd line on a journey. A starving carnivore may take its own species at the card weight (T. rex 5 %), never its own group or family | Running gait toward food. More calls and growls (proposal, lead decides) | Plan. Herd decision 8. Lead 2026-10-12 (D62) |
| Harmful | 0 % | Loses 1 health each 4 s (config). Turns aggressive. Never attacks a fence. A Velociraptor lasts about 2.3 minutes, a T. rex about 10 | Hurt flash and its own hurt sound at each loss. Limp below 30 % health (D92) | Decision, section 11.10 |

**Thirst**

| Band | Range | Behaviour change | Visible signal | Source |
| --- | --- | --- | --- | --- |
| Satisfied | 75 to 100 % | No water search. Cooldown after a drink: 5 minutes, as for hunger (D160) | Water drips from the mouth after a drink | Decision, section 11.10. Dino VFX. Lead 2026-10-12 (D104) |
| Wants | 25 to 75 % | The water search starts and widens on each miss. The animal walks to a shore point and drinks with its head at the surface. Stress +2 per step | Walks to water. Drink clip and drink sound | Plan. Decision, section 11.9. Lead 2026-10-12 (D104) |
| Urgent | Below 25 % | Runs to water. Does not sleep. Leaves the herd line on a journey | Running gait toward water | Plan. Herd decision 8 |
| Harmful | 0 % | Energy falls. It loses 1 health each 2 s (40 ticks, config), so it dies faster than from hunger. Playtests tune it | Hurt flash and hurt sound. Limp below 30 % health | Design doc, "Thirst". Decision, section 11.10. Lead 2026-10-12 (D104) |

**Stamina and sleep**

| Band | Range | Behaviour change | Visible signal | Source |
| --- | --- | --- | --- | --- |
| Satisfied | 75 to 100 % stamina. Slept in its window | A herd leader may start a journey. A carnivore may hunt | Normal gait and idle | Herd decision 29. Plan |
| Wants | A third to 75 % | No new roaming journey for a leader. Rests at a herd pause | Lies down with its rest clip. Yawns | Herd decisions 28, 29. Dino VFX |
| Urgent | Below a third | No new hunt. A follower on a roaming journey rests only at critical stamina | Lies down, rest idle. Slow walk | Plan. Herd decision 17 |
| Harmful | 0 % | A chase ends at once. Hunt cooldown follows (proposal 120 s, plan). Prey and players escape | The predator stops, pants (breath sound) and lies down | Decision, section 11.10. Plan |

Sleep: an animal sleeps in its window when its sleep need is low, on dry ground, with no threat near. Damage wakes it, and it stays awake 10 s. After it wakes, it does not sleep again for 5 to 7 minutes (plan). Lost sleep (lead 2026-10-12, D105):

- Until its next full sleep, the rest part of contentment stays at 50 or lower, and the hover box shows the tired icon.
- Stamina regenerates at half rate.
- Lost sleep gives no stress. This reverses the M2 pass rule "each missed window adds one stress step" (lead 2026-10-12, follow-up pass).

**Company (part of stress)**

| Band | Range | Behaviour change | Visible signal | Source |
| --- | --- | --- | --- | --- |
| Satisfied | Group at or above the species minimum, near it | Normal group life | Soft contact calls, greeting a mate with its speak clip | Design doc, "Group Size", "Social". Decision, section 11.16 |
| Wants | Below the minimum for a short time | Calls to gather. Joins any same-species group in range. A hatchling looks for an adult within 16 blocks | More contact calls. Looks around | Decision, section 11.10. Plan |
| Urgent | Alone for 3 in-game days (72,000 ticks, D106) | Stress +2 per step | Calls often, paces, sits less | Plan. Design doc, "Dino vs Player" ("more erratic and aggro the longer they go without meeting their minimum social req") |
| Harmful | Stress reaches the stressed band | Stressed-band results (below) | See stress | Design doc, "Dino vs Player" |

T. rex and Dilophosaurus have a minimum of 1, so being alone never fails this need. A group above its maximum splits into two groups, each with its own leader. Crowding causes no stress (lead 2026-10-12, D106). Young do not count toward the group size until they are adult. Loneliness has no bar of its own (lead 2026-10-13, D52):

- Below the group minimum, stress rises over time. "Alone" becomes urgent after 3 in-game days.
- The hover box shows the "lonely" icon when loneliness is the main cause of stress.
- The company part of contentment reads the group size.

**Stress**

| Band | Range (threshold 50, default) | Behaviour change | Visible signal | Source |
| --- | --- | --- | --- | --- |
| Calm | 0 to 29 | Neutral mood. Can breed. Taming trust rises only below stress 20 | Full idle pool, sits and rests often, soft calls | Design doc, "Stress: Basic comfort". Decision, section 11.10. Lead 2026-10-12 (trust rule) |
| Uneasy | 30 to 49 | Can still breed. Taming trust does not rise | Sits half as often, calls twice as often, looks around and sniffs more | Plan (stress band changes idle weights). Decision, section 11.10. Lead 2026-10-12 (D99) |
| Stressed | 50 to 99 | Aggressive. No breeding. A tame bond ends and the owner is no longer safe | Growls and warning calls, three times as many calls, paces, never sits | Decision, section 11.10. Design doc, "Stress: Basic comfort". Plan. Lead 2026-10-12 (D99) |
| Harmful | 100 | Stress itself does no damage. Hunger and thirst cause the damage | As stressed | Decision, section 11.10. Lead 2026-10-12 (D103) |

Stress inputs in Beta: food and water, and company. A live-bait hunt lowers a carnivore's stress. Enclosure space and block comfort are not inputs (decision, section 11.10, 11.12). Each active cause adds 2 stress per step: hungry, thirsty, alone (lead 2026-10-12). Start values: fed and in company −1, live-bait kill −20, drift toward zero, maximum 100 (plan). One step is 1 minute, 1,200 ticks. Lost sleep is not an input. The threshold is one value per species, default 50 (lead 2026-10-12, D13, D88). Playtests tune them. The Stress Tolerance gene raises the threshold (see "How genes change behaviour").

#### Contentment

The lead adopted a contentment score for Beta on 2026-10-12: a readout, plus effects of its own. Stress stays as it is.

**What it is.** One score from 0 to 100 that sums up how the animal is doing now. Higher is better. It reads the animal's present state: its needs, stress, health and safety. It never reads the genome. Genes change it only through the state they change (see "How genes change behaviour"). So a player can always see why an animal is unhappy, for example "thirsty and lonely".

**Its parts.** Each part scores 0 to 100. The lead accepted all seven parts and their weights after a worked example (lead 2026-10-12, D110). Each part scales as this table shows.

| Part | Scores 100 when | Scores 0 when | Reason word below 70 | Source |
| --- | --- | --- | --- | --- |
| Hunger | Hunger at 75 % or more | Hunger at 0 | hungry | Lead 2026-10-12. Plan (75 % line). Scale: proposal |
| Thirst | Thirst at 75 % or more | Thirst at 0 | thirsty | Lead 2026-10-12. Scale: proposal (D104 lines) |
| Rest | Stamina at 75 % or more, and it slept in its last sleep window | Stamina at 0. A missed sleep window caps the part at 50 until the next full sleep | tired | Lead 2026-10-12 (D105). Herd decision 29 (75 %) |
| Company | Its group near it is at or above the species minimum | No mate near it. Between: mates near ÷ (minimum − 1) | lonely | Lead 2026-10-12. Design doc, "Group Size". Decision, section 11.16 |
| Stress | Stress 0 | Stress 100 | stressed | Lead 2026-10-12. Design doc, "Stress: Basic comfort" |
| Health | Full health | 0 health | hurt | Lead 2026-10-12 |
| Safety | No threat in its 10 × 5 × 10 box and not hurt in the last 2 minutes | A threat in its box. Hurt in the last 2 minutes scores 50 | in danger | Lead 2026-10-12. Plan (threat box, 2,400 ticks). The D61 place memory is apart from this line |

A species with a group minimum of 1 (T. rex, Dilophosaurus) always scores 100 for company.

**The score** (lead 2026-10-12, D110). The weighted mean of the parts: hunger 20, thirst 20, rest 10, company 15, stress 15, health 10, safety 10. The score never rises above the lowest part plus 50, so one failing need always shows.

Example: a fed, rested Velociraptor at full health with stress 40, thirst 30 % and no mates near it. Parts: hunger 100, thirst 40, rest 100, company 0, stress 60, health 100, safety 100. The weighted mean is 67. The lowest part is 0, so the score is 50: uneasy, "lonely and thirsty".

**Bands** (lead 2026-10-12, D110):

| Band | Score | What it means |
| --- | --- | --- |
| Content | 70 to 100 | Its needs are met. This is the healthy state |
| Uneasy | 40 to 69 | One or more needs slip. A keeper has time to act |
| Distressed | 0 to 39 | A need fails, or the animal is in danger or badly hurt |

A band changes only when the score passes the line by 5 points, so the readout does not flicker.

**Contentment and stress.** Stress stays the gate for breeding, taming and aggression (section 11.10). Contentment never gates them. The two can differ. A thirsty animal whose stress is still low is calm, so it can breed, but it is uneasy, so the keeper sees the problem early.

**Effects of its own.** The lead chose these effects on 2026-10-12 (D109). Playtests tune them. The uneasy band has no effect.

| Band | Effect | Example |
| --- | --- | --- |
| Content | Grows 10 % faster. Natural health regen 25 % faster | A content T. rex calf reaches full size in about 41 in-game days, not 45 |
| Uneasy | None (lead 2026-10-12, D109) | |
| Distressed | Grows 25 % slower. Natural health regen at half rate | A distressed T. rex calf takes about 60 in-game days |

Not proposed: an effect on breeding or taming (stress gates them, section 11.10), fence attacks (fence parity F27, F29), and disease (not in Beta, section 11.13).

**Readouts.** The lead chose all three on 2026-10-12. This answers D18: every player gets a readout.

| Readout | Who sees it | What it shows | Source |
| --- | --- | --- | --- |
| Hover box | Every player who looks at the animal for 5 ticks, within 24 blocks (lead 2026-10-12, D110) | Icons only (D149): the name, a coloured mood face for the band, and the icon of the worst need. Two need icons if both are below 70. No band words. Example: "Gallimimus", a yellow face and a water-drop icon. Need icons: hunger, thirst, tired, lonely, stressed, hurt, threatened. It is the same hover box as on machines and feeders, with the same client switch (D27) | Lead 2026-10-12. F1.10 hover box |
| Visible cues | Everyone | The body and voice signals in "What the player can see" | Lead 2026-10-12 |
| Exact numbers | Debug Stick holder in creative or an operator | The score, each part, the stress value, each need value and the current activity | Lead 2026-10-12. F2.1 debug status |

#### What the player can see

Three readouts show an animal's state (see "Contentment"): the hover box for every player, the visible cues below, and exact numbers on the Debug Stick in creative or for operators (lead 2026-10-12, D18 answered). The hover box names the problem. The animal's body and voice show it without a look at any box.

| State | All species | Source |
| --- | --- | --- |
| Content | Idle pool with sits and rests, grazing or feeding at a calm pace, soft calls, sleep at night with Zzz and bubbles | 2020 board. Dino VFX. Plan |
| Hungry | Sniffs air and ground, heads for food, carnivores stalk. After a meal: plant bits, blood drips, bloody mouth | Plan. Dino VFX |
| Thirsty | Walks to water, drinks, water drips | Dino VFX. Plan |
| Tired | Yawns, lies down, slow walk. A predator gives up its chase | Dino VFX. Decision, section 11.10 |
| Lonely | Calls to gather more often, looks around | Decision, section 11.10 |
| Stressed | Sits less, calls more, paces, warns sooner | Plan |
| Hurt or starving | Hurt flash and own hurt sound, flinch, limp below 30 % health | 2020 board (limp). Lead 2026-10-12 (D92) |

Per species, what tells a keeper content from stressed. Clips and sounds are the species' own (no stand-ins, lead 2026-10-12).

| Species | Content signs | Stressed signs | Source |
| --- | --- | --- | --- |
| Tyrannosaurus | Slow patrol, sniffing, scratch and shake body, breath sound, sleeps through the night | Growls, roars without a target, paces the fence line, never sits | Species card ("Idles"). Art list (growl). Proposal, lead decides |
| Velociraptor | Pack close together, soft calls, sits and sneezes, sleeps in turns | Screeches and hisses, lone raptor calling, restless look around, pacing | MVP board, Velociraptor card. Art list (screech, hiss). Proposal, lead decides |
| Dilophosaurus | Curious head tilts, speak, still waits by water | Hoots, half-open frill without a target (needs the intimidate clip), pacing | 2020 board ("Curious", "Speak"). Proposal, lead decides |
| Triceratops | Grazing, snorts, sits, herd spread out | Growls, head shakes, faces the keeper, ram wind-up sooner | Art list (snort, growl). Proposal, lead decides |
| Brachiosaurus | High browsing, slow look around, deep calls, rests in open ground | Bellows, stomps, keeps moving, does not lie down | Art list (bellow, stomp). Proposal, lead decides |
| Gallimimus | Ground pecking, burps, chirps, sentry by the leader, spread herd | Bunched herd, startles at small things, screech alarm calls, runs often | 2020 board ("Sentry"). Art list (burp, screech). Proposal, lead decides |

Effects that help a keeper (Dino VFX, all in Beta): drinking drips, plant bits, blood drips and bloody mouth, foggy breath, yawn, sneeze, snort, Zzz and bubbles, snow cover while sleeping in snow, footprints and dust trails.

#### Behaviour profile per species

Temperament ratings out of 10 come from the design doc ("Byte Values", codebase analysis section 7.3). Group sizes come from section 11.16 and the T. rex card.

| Species | Aggression | Intelligence | Group tendency | Metabolism | Diet and feeding style | Group size and kind |
| --- | --- | --- | --- | --- | --- | --- |
| Tyrannosaurus | 7 | 3 to 4 | 2 to 3 | 7 to 8 | Carnivore. Prey by clade on its card, players and villagers included. Scavenges carcasses and dropped meat. Carnivore feeder | 1 to 2, solo or pair |
| Velociraptor | 7 to 9 | 6 to 8 | 6 to 9 | 5 to 6 | Carnivore. Scavenges. Carnivore feeder, live bait | 3 to 6, pack |
| Dilophosaurus | 5 to 6 | 2 to 4 | 3 to 5 | 4 to 6 | Carnivore. Carnivore feeder, live bait | 1 to 3, loose group that hunts together (D54) |
| Triceratops | 5 to 7 | 1 to 2 | 6 to 7 | 5 to 7 | Herbivore. Grazes low plants. Herbivore feeder at ground height | 4 to 6, herd |
| Brachiosaurus | 1 to 2 | 0.5 to 1 | 5 to 7 | 8 to 10 | Herbivore. Browses high leaves, rears to reach. Very Tall Herbivore Feeder | 3 to 5, herd |
| Gallimimus | 1 to 3 | 2 to 3 | 7 to 10 | 7 to 8 | Omnivore (lead 2026-10-12). Pecks low plants, eats dropped meat and eggs. Herbivore and carnivore feeders | 6 to 12, herd |

Sources for the diet column: species card (T. rex prey table), decision (section 11.10), 2020 board ("GroundPeck", "Stand on hindlegs Eat"), feature F3.3 (feeder kinds).

| Species | Active hours | Response to a threat | Hunting style | Personal space | Signature behaviours |
| --- | --- | --- | --- | --- | --- |
| Tyrannosaurus | Cathemeral | Stands, or retreats from a much stronger threat (D13). Roar, then attack | Solo or pair. Stalks, then sprints. In a pair one chases, and one circles to cut off the prey (D54) | 10 blocks. Young and eggs provoke always | Roar with screen shake. Breaks an unpowered fence (F29). Stalk |
| Velociraptor | Cathemeral | Stands, or retreats from a much stronger threat (D13). Leader screeches | Pack: one target, flank places, attack together. Creep, then pounce. Hides in cover by paths when hungry (D60) | 6 blocks. Young and eggs provoke always | Pack flank and pounce. Hyper intelligence from hatch: doors, chests, climbs out at a dead fence section. Sneeze. Laugh call |
| Dilophosaurus | Cathemeral | Stands, or retreats from a much stronger threat (D13). Intimidate display and hoot, then spit if the player stays | With its group (D54). Hides in cover by paths and springs out (D60). Spit blinds and poisons (poison from hatch, D181) | 6 blocks. Young and eggs provoke always | Intimidate display. Spit blob. Curious head tilt |
| Triceratops | Diurnal | Stands and charges | None | 8 blocks. Herd area | Ram wind-up and charge. Low grazing. Snort |
| Brachiosaurus | Diurnal | Defends: roar warning (bellow), then tail whip and stomp. Does not flee | None | 10 blocks. Herd area, open ground only | High browsing on hind legs. Bellow. Ripples and screen shake |
| Gallimimus | Diurnal | Runs. The herd bolts as one | None. Eats dropped meat and eggs | 5 blocks. Startles at 6 blocks. Herd area | Wedge travel, sentry, startle, burp |

Sources: decision (section 11.10) for flee or stand, packs and warnings. Lead 2026-10-12 for active hours (D97), the Brachiosaurus defence (D50), personal space, the Dilophosaurus warning and the omnivore Gallimimus. The personal-space radii are config start values by body size, and playtests tune them (lead 2026-10-13). Herd decisions 1, 26 for wedge, sentry and startle. Dino VFX for the effects. Lead 2026-10-12 (D43) for hyper intelligence: Velociraptors carry it from hatch, as Special Genes says. Lead 2026-10-12 for the threat rule (D13), group hunts (D54), ambush (D60) and Dilophosaurus poison from hatch (D181).

**Genes that move a profile.** Genes move every column of these two tables. See "How genes change behaviour".

#### How genes change behaviour

The lead asked on 2026-10-12 whether the plan says how genes change behaviour. This section is the answer. Mod Commission sets the goal: "The genetic editing players engage in while cloning will impact Dinosaur AI as well". Section 11.6 makes it a rule: each gene group must change the animal in Beta. The groups are core stats, needs, behaviour, and breeding and survival.

**How a gene reaches behaviour.** A gene holds a percent change from the species base. The donor table (DNA Effects) supplies the percents, and it wins over the code (section 11.6). Each clone also varies a little, even with no edits (section 11.6), so two unedited raptors differ a little in behaviour. F1.5 turns the genome into the animal's values: base × (1 + gene %) (plan). The behaviour code reads those values only: need rates, group sizes, dominance, reach, the sleep window. It never reads the genome directly. Contentment reads the state that results (see "Contentment").

Rules for this section: a percent in the "Range or limit" column is a donor value from the DNA Effects table, unless it says otherwise. The lead accepted most D112 effect sizes on 2026-10-12, and playtests tune them. An effect size that is still open is marked "proposal, lead decides".

**Behaviour genes** (section 11.6: aggression, intelligence, herd size, pack size, tamability, nocturnal)

| Gene | What it changes in behaviour | Range or limit | Source |
| --- | --- | --- | --- |
| Aggression | How aggressive the animal is by default (design doc). It is one of the three dominance inputs, so a high gene leads and wins rank contests more often (dominance weight 0.2, plan default). It moves the temperament (lead 2026-10-12, D112): at +50 % or more a species that is neutral when fed warns and attacks by gene. An aggressive animal notices players farther away and warns sooner (D102). Players who want "sport hunting" raise it (design doc) | Species base (Byte Values, out of 10): T. rex 7, Velociraptor 7 to 9, Dilophosaurus 5 to 6, Triceratops 5 to 7, Gallimimus 1 to 3, Brachiosaurus 1 to 2. Hoglin +87 %, polar bear +45 %, axolotl +15 % | Decision, section 11.6, 11.10 (dominance). Design doc, "Aggression", "Byte Values", "Dino vs Player". DNA Effects. Plan default (dominance weights) |
| Intelligence | The design doc: how efficient and smart the animal is in what it does and what it puts first. A low value makes it more prone to inefficient decisions and less able to meet its needs. It also feeds the social and stress genes, and a group "guides" a low-intelligence animal. The lead accepted these effects on 2026-10-12 (D112), with intelligence I on the 0 to 10 scale after genes: (1) the food and water search radius is × (0.75 + 0.05 × I), and the retry delay is ÷ the same factor. (2) A failed target stays remembered × (1 + 0.05 × I), so the animal stops trying an unreachable feeder sooner. (3) The detection chance (F2.6) rises by I points | Species base (Byte Values): Brachiosaurus 0.5 to 1, Triceratops 1 to 2, Gallimimus 2 to 3, Dilophosaurus 2 to 4, T. rex 3 to 4, Velociraptor 6 to 8. Wolf +75 %, dolphin +75 %, cat +50 %, fox +30 %, sheep −25 %, chicken −50 %. Hyper intelligence adds 15 % (Special Genes) | Design doc, "Intelligence", "Social", "Byte Values". DNA Effects. Special Genes. Reconciliation G3 (detection, implementation help) |
| Herd size, pack size | The company minimum and maximum: how many mates the animal needs for low stress, and how big a group it joins. Herd size acts on herbivores, pack size on carnivores. A larger minimum makes a small group lonely sooner (company part of contentment) | Section 11.16 group size × (1 + gene %), rounded, at least 1 (rounding: proposal, lead decides). Wolf +75 % ("max pack size of carnivores"), sheep and cow +50 % ("herd size for herbivores"), goat +25 %, dolphin +20 %. Cat and ocelot lower it (design doc) | Decision, section 11.6, 11.16. Design doc, "Group Size", "Social", donor list. DNA Effects |
| Tamability (domesticity) | How tameable the animal is. It lowers the trust a player needs to tame it (F2.6). Trust still rises only while stress is below 20 (F2.6) | Lead 2026-10-12 (D112): trust threshold ÷ (1 + gene %), never below a quarter of the base. Wolf +100 %, cat +45 %, horse +40 %, parrot +30 %, mule +30 %, llama +25 %, axolotl +15 %, ocelot −35 %, hoglin −75 % | Decision, section 11.6, 11.10. Design doc, "Domesticity". DNA Effects |
| Stress Tolerance | Raises the species stress threshold, so the animal stays calm longer. It is a basic number gene, not a special gene. The design doc name was "Lower Threshold Stress" | Calm donors, for example cat and sheep. Values: not set (F1.5 task plan) | Design doc. Lead 2026-10-12 (rename) |
| Nocturnal | The sleep window. At +50 % or more the window moves one step toward nocturnal (diurnal to cathemeral to nocturnal). At −50 % or less it moves one step back (plan default D49). A smaller value moves the window edges by up to 2 hours, the bat's "mild reversal" (lead 2026-10-12, D112). A diurnal herbivore moved one step gets cathemeral windows of its own (table in "A healthy dinosaur", D182). A nocturnal or cathemeral animal sees better at night | One step at most (D49). Spider +75 %, bat +25 % | Decision, section 11.6. Design doc, "Diurnal/Nocturnal/Cathemeral", "Light Level", donor list. DNA Effects. Plan default D49 |

**Need genes** (section 11.6: stomach capacity, eat rate, thirst, energy). Each one changes how fast a need drains or fills, so it changes how often the animal seeks food, water or rest, and so its contentment.

| Gene | What it changes in behaviour | Range or limit | Source |
| --- | --- | --- | --- |
| Stomach capacity (hunger) | How much it eats and how fast hunger drains, so how often it walks to food and waits at a shared feeder. High hunger raises stress and lowers energy | T. rex hunger 80 (card). Panda +75 %, llama +30 %, camel −25 %. A larger size also raises hunger (design doc, "Size") | Decision, section 11.6. Design doc, "Hunger/Stomach Capacity", "Size". T. rex card. DNA Effects |
| Eat rate | How long a bite takes. A slow eater holds a feeding spot longer, so the queue at a shared feeder moves slower | Panda −10 % | Design doc, "Eat Rate". DNA Effects. Lead 2026-10-12 (shared feeders) |
| Thirst | How fast thirst drains, so how often it walks to water. Lack of water lowers energy and kills faster than hunger | T. rex thirst 70 (card). Camel −30 % | Decision, section 11.6. Design doc, "Thirst". T. rex card. DNA Effects |
| Energy (metabolism) | The stamina maximum: how long a chase lasts, when a herd leader may start a journey (75 % stamina), and how often the animal rests | Section 11.16 energy (Gallimimus 203, Brachiosaurus 244). Camel +35 %, sniffer −25 % ("decrease in metabolism for herbivores", design doc) | Decision, section 11.6, 11.10, 11.16. Design doc, "Energy", "Byte Values". Herd decision 29. DNA Effects |

**Stat genes that shape behaviour**

| Gene | What it changes in behaviour | Range or limit | Source |
| --- | --- | --- | --- |
| Size | Dominance (size is a dominance input). Reach: a bigger animal browses higher (reach band × growth, F2.4). Body width: fewer animals fit at one feeder, and herd spacing grows. A bigger size also costs more hunger and less speed | Inside the species range (design doc). T. rex adult 3.78 to 6 blocks (card). Hoglin +75 %, polar bear +50 %, rabbit −75 % | Decision, section 11.6, 11.9, 11.10. Design doc, "Size". T. rex card. DNA Effects |
| Speed | Chase and escape. A faster predator closes the gap before its stamina ends. Faster prey escapes. Herd walk speeds scale with it | Edit limits (section 11.6). Fox +70 %, rabbit +50 %, turtle −50 % | Decision, section 11.6, 11.10 (stamina chase). Design doc, "Speed". DNA Effects |
| Strength and defense (with health) | The fight or flee choice. The prey pick compares threat, so a stronger animal takes bigger prey and is a threat to more animals. Very high stress raises attack damage (design doc). A standing animal compares its threat with the attacker's. A carnivore adds the summed threat of its group (D13). It retreats when the attacker's threat is more than 2 × its own (lead 2026-10-12, D112) | Polar bear strength +50 %, defense +75 %. Panda defense −50 % | Decision, section 11.6, 11.10. Design doc, "Attack", "Defense". DNA Effects |
| Jump | It climbs ledges it could not climb before, so it reaches more ground and food. Only species that can jump: large dinosaurs are excluded (design doc). A strong jump edit can clear the 3-block low-security fence. It never clears the 8-block high-security fence (lead 2026-10-12, D47, reversed) | Frog +43 %, horse +30 %. Cap below 8 blocks (D47) | Decision, section 11.6. Design doc, donor list. DNA Effects |
| Underwater capacity | Deep water costs less on its path, so it wades or swims where others walk around (section 11.10 water rule) | Turtle +125 %, dolphin +75 %, nautilus +35 % | Decision, section 11.6, 11.10. Design doc, "Underwater Capacity". DNA Effects |
| Heat resistance | Where it goes: a lower path cost near fire, lava and magma, scaled by the gene (lead 2026-10-12, D112). It also takes less fire damage (F1.5) | Hoglin fire resistance +55 % | Decision, section 11.6. DNA Effects. Reconciliation RC25 (damage, implementation help) |
| Health regen | It recovers sooner after a fight, so its health part of contentment returns sooner | Axolotl +55 %, sniffer +43 % | Design doc, "Health Regen". DNA Effects |
| Vision | Sight range for detection (F2.6). Low-light vision counts at night only | Armadillo −35 %, frog low-light +30 % | Design doc, "Vision". DNA Effects |
| Fertility, egg clutch, gestation time | The breeding roll, the clutch size and the days to laying (F2.5) | Mule fertility −40 %, frog clutch +25 %, armadillo gestation +40 % | Decision, section 11.6, 11.10. DNA Effects |

**Special genes** (section 11.6: all seven are in Beta)

| Gene | What it changes in behaviour | Range or limit | Source |
| --- | --- | --- | --- |
| Hyper intelligence | Opens doors, takes items from chests, notices when an electric fence is off, +15 % to intelligence and to food and water detection. After it notices an unpowered fence, it walks to the dead section and climbs out. This is a lead-approved exception to fence parity (D46) | Special gene slots: 1 at 50 %, 2 at 75 %, 3 at 100 % DNA (D51). Every Velociraptor carries it from hatch (D43) | Special Genes. Decision, section 11.6, 11.11. Lead 2026-10-12 |
| Camouflage | Players find it hard to see while it stands still. That is its only effect: an ambusher with camouflage waits as long as one without it (lead 2026-10-13, D112) | T. rex, Velociraptor, Dilophosaurus (lead 2026-10-12, D42) | Special Genes. Decision, section 11.6 |
| Thermal detection | It senses players through walls within a range, so a hidden player still gets noticed and warned | T. rex, Velociraptor, Dilophosaurus (lead 2026-10-12, D42). Range: F2.6 task plan | Special Genes. Lead 2026-10-12 |
| Poison | Its bite, and the Dilophosaurus spit, poisons. Its choices do not change | Special Genes: found in the Dilophosaurus. Every Dilophosaurus carries it from hatch (lead 2026-10-12, D181) | Special Genes. Design doc |
| Gender swapping | Only in a one-gender group, with no animal of the other gender, it starts to change gender. It commits on day 8 if no animal of the other gender came, and finishes on day 10. Its skin stays | 10 in-game days | Special Genes. Design doc. Lead 2026-10-12 (one-gender groups only) |
| Lysine contingency | It needs lysine from food: lysine-rich plants, chicken or eggs. A carnivore eats lysine plants only when no other lysine is near. Without enough lysine it falls into a coma (sleep) and soon dies. A full reserve lasts 3 in-game days. When the reserve is empty, the animal falls into a coma and dies 1 in-game day later (config). Lysine-rich food (chicken, eggs) tops up the reserve. Playtests tune it. Beta has no Lysine Pills item (D170) | Any species | Special Genes. Design doc. Lead 2026-10-12 |
| Bioluminescence | None. Special Genes: "Purely Aesthetic with no functional AI changes" | All six species. One glow mask per species, shared by all its skin sets | Special Genes. Lead 2026-10-12 |

**Diet genes and quirks** (section 11.6: diet genes and quirks are in Beta)

| Gene | What it changes in behaviour | Range or limit | Source |
| --- | --- | --- | --- |
| Grass eating, mushroom eating | It adds grass or mushroom blocks to a herbivore's diet, so it grazes new blocks and finds food in more places | Goat, sheep, cow (grass). Mooshroom (mushrooms). Sniffer: torchflower seeds, +25 % regen while it eats them | Design doc, "Special Diet Genes". DNA Effects |
| Clucking | Now and then it clucks: at a random moment every 2 to 5 minutes (config, lead 2026-10-12) | Chicken, "small chance" | Design doc, donor list. DNA Effects |
| Sound imitation | Now and then it copies the sound of a mob near it, as a parrot does. It plays at a random moment every 2 to 5 minutes (config, lead 2026-10-12) | Parrot, "small chance" | Design doc, donor list. DNA Effects |
| Item carrying | A tame animal carries a few items for its owner, like a llama with a chest (lead 2026-10-13, D112) | Llama | Design doc, donor list. DNA Effects |
| Spit (llama) | It spits now and then, like a llama. The spit gives a small knockback and no damage (lead 2026-10-13, D112) | Llama, "small chance of spit ability (no venom)" | DNA Effects |

Not in Beta: the cocoa allergy quirk of the parrot (lead 2026-10-13, D112).

**Abnormal behaviour from heavy editing**

| Gene | What it changes in behaviour | Range or limit | Source |
| --- | --- | --- | --- |
| Abnormal behaviour (risk) | A genome with many donors or many edits can roll one abnormal behaviour at hatch (section 11.6). The lead accepted the list (D44): stresses easily (threshold −20), flipped day and night, loner (maximum group 1), more aggressive (+30 %). Every abnormal animal still warns before it attacks. The Debug Stick names it | The chance grows with donor share and edit count. At most one per animal | Decision, section 11.6. Design doc ("abnormal/unnatural behavior"). Lead 2026-10-12 (D44) |

**Heredity of behaviour genes**

| Gene | What it changes in behaviour | Range or limit | Source |
| --- | --- | --- | --- |
| Number genes (aggression, intelligence, herd and pack size, tamability, nocturnal, the need and stat genes) | A bred young gets the middle value of its parents plus the clone variation. So two calm, sociable parents breed a calm, sociable young. The egg stores the mix when it is laid, and a player can sample it. The small variation rolls at hatch (D71) | Inside the edit limits | Decision, section 11.10. Lead 2026-10-12 (D71) |
| Choice genes (special gene, morph, skin, colour layers) | Each comes from one parent, picked at random | One parent | Decision, section 11.10 |
| Quirks | Each parent that has the quirk passes it with a 50 % chance | 50 % per parent | Lead 2026-10-12 |
| Abnormal behaviour | A young can take the abnormal behaviour of one parent, with a 50 % chance | One parent, 50 % | Lead 2026-10-13 |

**Intelligence, resolved.** The old open line "intelligence: not set" is now the design doc definition plus the proposal in the "Intelligence" row above. Example: an unedited Velociraptor (intelligence 7) searches × 1.10 and an unedited Brachiosaurus (intelligence 1) × 0.80. So the raptor's search ring is about 40 % wider, and it finds a new feeder sooner. The lead accepted the numbers on 2026-10-12 (D112), and playtests tune them.

#### Open questions

The final pass of 2026-10-13 answered D36, D52, D111, D160, D166 and D182. It also set the personal-space radii, the safety part and the heredity of abnormal behaviour. The last pass of 2026-10-13 (Z-8) answered the rest:

- Settled by the lead (2026-10-13, Z-8): the rest of D112. A camouflaged ambusher waits as long as one without camouflage. Llama item carrying and llama spit are in Beta. The cocoa allergy is not in Beta.
- Settled by the lead (2026-10-13, Z-8): the nocturnal window is 10:00 to 18:00 (8 h) (D252). This replaces the plan window 06:00 to 18:00.

No question stays open for F2.0.

**Art needed:** see Art list → each species (idle clips: idle 1, idle 2, yawn, sneeze, sit, curious, ground peck, sentry, startled, shake body, scratch, speak, limp. Calls: contact, nest, mating, hatchling. Hurt sounds), Dinosaur effects, Dinosaur hover box. Shared with F2.3. The dinosaur hover box shows icons only: 3 mood faces and 7 need icons (lead 2026-10-12, D149).

**Today:** Behaviour runs as one list of tasks in the dinosaur. Three carnivores use a nocturnal flag and the herbivores a diurnal one in today's config. That is current code, not a design value. The idle pick only changes the standing clip. No species looks at a player, flees, or warns a player before an attack. Every species plays the T. rex roar. Stress and social do nothing. No contentment score and no hover box for dinosaurs exist.

**Engineering:** backlog F2.0 (new package). It takes the activity ladder and the idle pool from F2.4, the stress-band idle shift from F2.3 and the temperament mapping from F2.6. It adds feeding together (with F2.4, F2.5 and F3.3), contentment, and the behaviour gene readers (with F1.5).

### F2.1 Species core

**In one line:** Six species with their own stats, smooth growth, real-size bodies, hits that land where you aim, and a carcass when they die.

**How it plays:** A hatchling is small and weak. Over in-game days it grows smoothly to adult size, and its body, hitbox and stats grow with it. Young animals follow a parent and cannot hunt. An arrow in the head hurts more than an arrow in the tail. A dead dinosaur leaves a carcass that drops meat, bones and special pieces, and decays after one in-game day.

**Sources:** species card (Tyrannosaurus), design doc, decision (section 11.9, 11.16), hitbox decisions of 2026-10-09 and 2026-10-10 (decision, section 11.9).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | Dinosaurs never spawn in the world. Cloning is the only source | decision, section 11.9 |
| Behaviour | Growth is smooth, with no steps. Size, hitbox and stats grow with the animal | decision, section 11.9 |
| Behaviour | Young animals follow a parent, cannot hunt and need care | decision, section 11.9 |
| Behaviour | Each clone differs in stats, size (with gender weights), layer colours and rare morphs. The roll happens at hatch | decision, section 11.6 |
| Behaviour | The movement box covers torso and legs. An animal fits any gap that its body fits | decision, section 11.9 |
| Behaviour | Hit parts cover the visible body: one head, a neck chain, two torso parts, one part per leg, a tail chain. No arm parts and no frill part. Parts follow the real clips | decision, section 11.9 |
| Behaviour | Neck and tail bend away from blocks. Feet sit on the ground | decision, section 11.9 |
| Behaviour | A carcass stays for a time and then decays. It drops meat and bones and gives a DNA sample | decision, section 11.9 |
| Numbers | Tyrannosaurus: health 155, regen half a heart per 15 ticks, damage 16, defense 12, speed 0.50, energy 203, hunger 80, thirst 70, group 1 to 2 | species card, decision, section 11.9 |
| Numbers | Other five species: interim working values (table below). They stay the working values until real cards exist. Each real card replaces its column | decision, section 11.16, lead 2026-10-12 (D12) |
| Numbers | Tyrannosaurus size: hatchling 0.30 blocks, infant 1.20, adult 3.78 to 6, with gender weights on the card | species card |
| Numbers | Young sizes for every species use the T. rex ratios: hatchling 8 % and infant 32 % of adult height. Playtests tune them | lead 2026-10-12 |
| Numbers | Full size in in-game days at the neutral rate: Tyrannosaurus 45, Velociraptor 29, Dilophosaurus 31, Gallimimus 31, Triceratops 47, Brachiosaurus 57. Contentment changes the rate (F2.0). Playtests tune it | decision, section 11.9, lead 2026-10-12 |
| Numbers | Height, 1 metre = 1 block: Triceratops 3.0 blocks (scale 1.32), Brachiosaurus 12.5 (scale 1.61), Dilophosaurus about 3. The other three stay near their size today. Interim values until real cards exist | lead 2026-10-12 (D11), section 11.16 ranges: Triceratops 2.7-4.5, Brachiosaurus 12.4-15.85 |
| Numbers | Hit damage: head ×1.5, neck ×1.25, torso ×1.0, legs ×1.0, tail ×0.5 | decision, section 11.9 |
| Numbers | Hit parts: Gallimimus 12, T. rex 13, Triceratops 12, Brachiosaurus 17, Velociraptor 12, Dilophosaurus 11. The layouts are in `docs/dinosaur-hitbox-layouts.html` | decision, section 11.9, lead 2026-10-12 |
| Numbers | A carcass lasts 1 in-game day (24,000 ticks), then it decays | lead 2026-10-12 |
| Items | Raw and cooked meat per species, fresh bones, tissue. Special drops: only the named pieces, the Velociraptor toe claw, the Triceratops horn and the T. rex tooth. No other species drops teeth | decision, section 11.9, species cards, lead 2026-10-12 |
| Animation | Death clip and dead pose per species (see F2.3) | decision, section 11.9 |
| Models | One adult model per species. No baby model: the adult scales down | 2020 board ("Baby, do not implement"), decision, section 11.9 |

Interim stats (decision, section 11.16):

| Stat | Velociraptor | Dilophosaurus | Gallimimus | Triceratops | Brachiosaurus |
| --- | --- | --- | --- | --- | --- |
| Health | 35 | 40 | 40 | 140 | 300 |
| Damage | 8 | 6 | 3 | 13 | 12 |
| Defense | 4 | 3 | 2 | 14 | 10 |
| Speed | 0.65 | 0.55 | 0.75 | 0.35 | 0.25 |
| Energy | 149 | 135 | 203 | 162 | 244 |
| Group size | 3 to 6 | 1 to 3 | 6 to 12 | 4 to 6 | 3 to 5 |

**Art needed:** see Art list → each species (Sprites: raw and cooked meat), Fossils and amber (fresh bones, soft tissue, Tyrannosaurus tooth, Velociraptor toe claw, Triceratops horn).

**Open questions:**

- Settled by the lead (2026-10-12): the growth times, the height targets (D11), the young sizes and the carcass time. The lead also settled the special drops and the five hit-part layouts. The interim stats stay the working values until real cards exist (D12).
- No other open questions.

**Today:** The six species walk in the game, but their data holds old placeholder stats, death leaves no carcass, and each species has one plain box with no hit parts.

**Engineering:** backlog F2.1 (old modules B16, B27).

### F2.2 Appearance

**In one line:** Each animal shows its own genome: gender, skin set, layer colours and the rare albino or melanistic morph.

**How it plays:** Males and females look different. The biome that gave most of the fossils sets the skin set, and at 100% DNA the player picks any set. Each colour layer has its own tint, so no two clones look the same. About one clone in twenty is albino or melanistic.

**Sources:** decision (section 11.6), design doc (skin variants, layers), Mod Commission (layer list), species cards, lead 2026-10-12 (Beta skin sets).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | Male and female have different textures | decision, section 11.6 |
| Behaviour | The biome that gave most of the sequenced fossils sets the skin set. At 100%, the player picks any set | decision, section 11.6 |
| Behaviour | Each editable layer varies in colour, tint, opacity and strength. Claws, teeth, nostrils, eyes and mouth never change | decision, section 11.6, Mod Commission |
| Behaviour | Colour editing has a primary and secondary colour mode and a per-layer mode | decision, section 11.6 |
| Numbers | Albino and melanistic morphs at about 5% | decision, section 11.6, design doc |
| Skins | Albino and melanistic morphs are tints of the normal layers. They have no textures of their own | lead 2026-10-12 |
| Behaviour | Which biome group gives which skin set: the table below. The biome group with the largest share of the sequenced DNA wins. A tie gives the JP/Nublar base set | lead 2026-10-13 (D180) |
| Behaviour | Amber counts by its biome group. DNA from a syringe draw or from remains has no fossil biome, so it does not count | lead 2026-10-13 (D180) |
| Behaviour | Velociraptor from fossils: a raptor from Nublar biomes comes out Nublar, with the Nublar crest. A raptor from Sorna biomes comes out Sorna, with the Sorna crest | lead 2026-10-13 |
| Behaviour | Both quill sets belong to the Sorna genome. A male Sorna clone rolls at random for "with quills" or "with arm quills" | lead 2026-10-13 |
| Behaviour | Mixed raptor looks come only from Nublar × Sorna breeding, or from advanced gene editing after the player unlocks both skin sets. The mixed looks: Nublar with quills, Nublar with Sorna crest, Sorna with Nublar crest | lead 2026-10-13 |
| Behaviour | A Nublar × Sorna young takes each look trait from a random parent, one roll per trait: base skin set, crest, quills | lead 2026-10-13 (Z-8, D249) |
| Numbers | The female Dilophosaurus scales larger than the male | lead 2026-10-12 |
| Skins | Beta sets: JP/Nublar base sets, JP3/Sorna sets (Velociraptor, Triceratops, Brachiosaurus, Dilophosaurus JPTG), JW sets (Brachiosaurus, Gallimimus, Dilophosaurus JWD), Velociraptor crest and both quill variants. Each set is complete, with every layer, for both genders | lead 2026-10-12 (D89, D94) |
| Skins | Not in Beta: the named raptors Blue, Charlie, Delta and Echo, the T. rex Big Eatie and Little Eatie sets, and the JW looks for T. rex, Velociraptor and Triceratops | lead 2026-10-12 (D87) |
| Skins | Dilophosaurus JPTG: one body for both genders. The male tint is fainter and less vibrant than the female tint (the lead's note was cut off: "than the female" is a reading) | lead 2026-10-12 |
| Models | The Velociraptor Sorna set uses its own JP3 model (`velociraptor_adult_JP3.bbmodel`) and the Sorna clip list. The Brachiosaurus JP3 set uses its own JP3 model | species card, 2020 board, lead 2026-10-12 (D90) |
| Failure | A copied layer does not count. A male skin that copies the female skin is missing art | lead 2026-10-12 |

Skin sets in Beta. "Yes" means that the set needs every layer for that gender.

| Species | Set | Female | Male | Source |
| --- | --- | --- | --- | --- |
| Tyrannosaurus | JP (Rexy female, Buck male) | Yes | Yes | lead 2026-10-12 |
| Tyrannosaurus | JW look | No | No | Not in Beta (lead 2026-10-12) |
| Tyrannosaurus | Big Eatie, Little Eatie | No | No | Not in Beta (lead 2026-10-12, D87) |
| Velociraptor | Nublar | Yes | Yes | lead 2026-10-12 |
| Velociraptor | Nublar with Sorna crest | Yes | Yes | species card, lead 2026-10-12 |
| Velociraptor | Nublar with arm quills | n/a (male-only variant) | Yes | species card, lead 2026-10-12 |
| Velociraptor | Nublar with quills | n/a (male-only variant) | Yes | species card, lead 2026-10-12 (D94) |
| Velociraptor | Sorna (JP3) | Yes | Yes | lead 2026-10-12 |
| Velociraptor | Sorna with Nublar crest | Yes | Yes | species card, lead 2026-10-12 |
| Velociraptor | Sorna with arm quills | n/a (male-only variant) | Yes | species card, lead 2026-10-12 |
| Velociraptor | JW look | No | No | Not in Beta (lead 2026-10-12) |
| Dilophosaurus | JP | Yes | Yes | lead 2026-10-12 |
| Dilophosaurus | JPTG (its JP3 set) | Yes | Yes. One body for both genders, with a fainter male tint | lead 2026-10-12 |
| Dilophosaurus | JWD (its JW set) | Yes | Yes | lead 2026-10-12 (D89) |
| Triceratops | JP | Yes | Yes | lead 2026-10-12 |
| Triceratops | JP3 Sorna | Yes | Yes | lead 2026-10-12 |
| Triceratops | JW look | No | No | Not in Beta (lead 2026-10-12) |
| Brachiosaurus | JP | Yes | Yes | lead 2026-10-12 |
| Brachiosaurus | JP3 | Yes | Yes | lead 2026-10-12 |
| Brachiosaurus | JW | Yes | Yes | lead 2026-10-12 |
| Gallimimus | JP | Yes | Yes | lead 2026-10-12 |
| Gallimimus | JW | Yes | Yes | lead 2026-10-12 |

**Skin set by biome group (lead 2026-10-13, D180).** A dash means that the species has no ore in that group. Velociraptor cells give the base set only. Crest and quill looks follow the Velociraptor rules above.

| Species | Desert | Badlands | Plains | Forest | Taiga | Hills | Swamp | River | Caves |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Tyrannosaurus | JP | JP | JP | JP | JP | JP | – | – | – |
| Triceratops | JP | JP3 Sorna | JP | JP3 Sorna | JP3 Sorna | JP3 Sorna | – | JP | JP |
| Velociraptor | Nublar | Sorna | Nublar | – | – | Sorna | Nublar | Nublar | Sorna |
| Gallimimus | JP | JW | – | – | – | – | – | JP | JW |
| Brachiosaurus | JP3 | JP | JW | JP3 | – | JP3 | JW | JW | JP |
| Dilophosaurus | JP | JPTG | – | – | – | – | – | JWD | JP |

In the game: a player who digs in the obvious place (desert or badlands) gets a classic look. A player who wants a rare look digs in a minor biome on purpose. If the JWD art is late, the river group gives the JP set until the art arrives.

**Art needed:** see Art list → each species (Skins and textures: JP, JP3/Sorna and JW sets, crest and quill variants. Models: Velociraptor Sorna, Brachiosaurus JP3).

**Open questions:**

- Settled by the lead (2026-10-12): Big Eatie, Little Eatie and the JW looks for T. rex, Velociraptor and Triceratops are not in Beta. Both quill sets and the Dilophosaurus JWD set are in Beta. The Velociraptor Sorna set has its own JP3 model. Morphs are tints. The JPTG set is one body with a fainter male tint.
- Settled by the lead (2026-10-13, Z-8): heredity of raptor looks (D249). A Nublar × Sorna young takes each trait from a random parent, one roll per trait: the base skin set, the crest and the quills. Example: a Nublar mother and a Sorna father with quills can give a Nublar son with quills.
- Settled by the lead (2026-10-13): the skin set table by biome group (D180) and the Velociraptor rules for crests and quills.
- Shared layers (eyes, teeth, claws, nostrils): the artist confirms that one file is right for both genders.
- Settled by the lead (2026-10-12, follow-up pass):
  - Green parrot DNA gives a raptor a green colour shift. No skin has the name "Delta" (D67). Mob variants change colour only (D65).
  - Skin layers are textures baked on the CPU, with no core shader, so they work with shader packs (D69).

**Today:** The renderer builds every animal's layers from the first animal it sees, draws only the base layer for Velociraptor and Triceratops, and ships one skin set per species.

**Engineering:** backlog F2.2 (old module B09).

### F2.3 Animations, sounds and visual effects per species

**In one line:** Each species moves, calls and reacts with its own clips, its own sounds and the full set of dinosaur effects.

**How it plays:** A T. rex roars with its own roar, a Velociraptor screeches, a Dilophosaurus opens its frill and hoots. A drinking animal drips water, a feeding carnivore gets a bloody mouth, a herbivore drops plant bits. A large animal shakes the screen when it roars near you, leaves fading footprints and sends ripples across water. A sleeping animal shows Zzz and bubbles.

**Sources:** 2020 board (clip lists per species), species cards, Dino VFX, decision (section 11.9, 11.10), lead 2026-10-12 (no stand-ins, all VFX in Beta).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Animation | Each species has the 2020 board clip list plus every clip that a Beta behaviour needs. Clips are keyframed | lead 2026-10-12, Mod Commission |
| Animation | No stand-ins. A clip from another species, an empty clip or a copy counts as missing | lead 2026-10-12 |
| Animation | The Velociraptor Sorna model and the Brachiosaurus JP3 model carry their own clip sets. The Sorna set has 21 clips, with head shake | 2020 board (Sorna checklist), species card, lead 2026-10-12 (D90) |
| Animation | Limp is in Beta for every species. Limp replaces walk below 30 % health (config, playtests tune it). Each species needs its own limp clip. Until then the T. rex and the Triceratops use their injured clip | 2020 board, lead 2026-10-12 (D92) |
| Animation | These need their own clips: Brachiosaurus rest, sit and sleep, Triceratops sit, Dilophosaurus roar older and roar young. The existing clips stay as placeholders and count as missing | lead 2026-10-12 |
| Animation | Every species gets the T. rex card extras: startled, shake body, scratch, speak | lead 2026-10-12 |
| Animation | The Dilophosaurus warning is its intimidate clip, converted from the asset repo. If the player stays, it spits | lead 2026-10-12 |
| Animation | Play uses existing clips (run, Velociraptor pounce). Play needs no new clip | lead 2026-10-12 (D107) |
| Animation | Nesting: the T. rex and the Velociraptor play their nesting clip while the female builds her nest. The other four species play an idle clip | lead 2026-10-13 |
| Sound | Each species has its own full set. Design scale: about 30 sounds per species, 47 for the T. rex | project proposal, lead 2026-10-12 |
| Sound | A T. rex roar never serves another species | lead 2026-10-12 |
| Sound | Footsteps play in time with the walk and run clips. Young footsteps sound higher and quieter | plan |
| Sound | Hatchlings have their own sounds per species. A pitched adult voice does not count | lead 2026-10-12 |
| Sound | Yawn, sneeze and snort have their own sounds per species. The lead accepts one shared carnivore eat sound | lead 2026-10-12 |
| Sound | The Velociraptor threat screech has its own clip and its own sounds, apart from the pack call | lead 2026-10-12 |
| Sound | Sourcing: the lead's direction is to source the dinosaur sounds from Jurassic World Evolution 3. Shared sounds can serve as placeholders meanwhile. They count as Missing on the Art list | lead 2026-10-12 |
| Effects | Every Dino VFX item is in Beta (matrix below). Screen shake has a config switch | lead 2026-10-12, Dino VFX |
| Failure | A species that plays another species' clip or sound is a defect | lead 2026-10-12 |

Why each clip is needed:

| Clip | Beta behaviour that uses it | Source |
| --- | --- | --- |
| Walk, run, swim | Movement, chase, flight | 2020 board |
| Jump / fall | Ledges, jump gene | 2020 board, decision, section 11.6 |
| Turn left, turn right | Turn in place in tight ground | decision, section 11.10 |
| Idle 1, idle 2 | Idle acts | 2020 board |
| Rest, rest idle, get up | Rest and stamina | decision, section 11.10 |
| Sleep | Sleep need | 2020 board, decision, section 11.10 |
| Sit | Tame "stay" command | 2020 board, decision, section 11.10 |
| Drink | Thirst, head at the water | 2020 board, decision, section 11.9 |
| Eat (graze, high leaves, ground peck, carcass) | Hunger, head at the food | 2020 board, decision, section 11.10 |
| Sniff air, sniff ground | Detection and idle acts (Beta has no scent trails) | 2020 board, decision, section 11.10, lead 2026-10-12 |
| Look left, look right, shake head | Idle and detection | 2020 board |
| Call, call 2, call 3 | Warn, gather, threaten | 2020 board, decision, section 11.10 |
| Threat display (roar, screech, intimidate, bellow, alarm call) | Warning before an attack, rank contest | decision, section 11.10, lead 2026-10-12 |
| Attacks (bite, claws, horns, ram, spit, tail whip, stalk, pounce) | Combat and hunting | 2020 board, decision, section 11.10 |
| Flinch / injured | Hit reaction | species card |
| Limp | Low-health walk below 30 % health (D92) | 2020 board, lead 2026-10-12 |
| Death | Death and carcass pose | 2020 board, decision, section 11.9 |
| Nesting | Female builds a nest and lays eggs (T. rex and Velociraptor. The others use an idle clip) | decision, section 11.10, lead 2026-10-13 |
| Startled, sentry | Herd alarm and herd watch | 2020 board (sentry), herd decision 26 |
| Startled, shake body, scratch, speak | Idle acts on every species (the T. rex card extras) | species card, lead 2026-10-12 |
| Yawn, sneeze | The yawn and sneeze effects (inference: the effect needs the motion) | lead 2026-10-12 |

The species tables below list what each species needs. The Art list gives the status of each clip and sound.

#### Tyrannosaurus

| Clips | Which | Source |
| --- | --- | --- |
| 2020 board list | walk, run, swim, drink, eat 1, eat 2, attack (bite), attack 2, call 1, call 2, rest, sleep, look left and right, roar 1, roar 2, roar 3, injured, sniff air, sniff ground, shake head, limp, death | 2020 board |
| Added by a Beta behaviour | idle, jump/fall, turn left and right, rest idle, get up, sit, stalk, flinch, nesting, yawn, sneeze, startled, shake body, scratch, speak. Limp uses the injured clip until its own limp clip exists | decision, section 11.10, species card, lead 2026-10-12 |
| Blocked or not in Beta | tranquilised (blocked with guns, D08, see F4.3). Baby model clips (not in Beta) | species card, 2020 board ("Baby, do not implement") |

| Sounds | Used for | Source |
| --- | --- | --- |
| roar, growl, call (contact call) | Warning, threat display, gather | decision, section 11.10 |
| attack snarl, bite hit, hurt, death | Combat, hit reaction, death | decision, section 11.9, 11.10 |
| eat (flesh rip, crunch. The lead accepts one shared carnivore eat sound), drink, sleep | Needs | decision, section 11.10, lead 2026-10-12 |
| heavy footstep | Walk and run | plan |
| breath | Idle | project proposal |
| nest call, mating call, hatchling sounds (own set, not a pitched adult voice) | Breeding and young | decision, section 11.10, lead 2026-10-12 |
| yawn, sneeze, snort | Idle and the yawn, sneeze and snort effects | lead 2026-10-12 |

#### Velociraptor

| Clips | Which | Source |
| --- | --- | --- |
| 2020 board list (Nublar) | walk, run, swim, jump, drink, eat, attack (bite), attack 2 (claw strikes), call, call 2, call 3, idle 1, idle 2, sit, sleep, sniff air, sniff ground, look left and right, limp, death | 2020 board |
| 2020 board list (Sorna) | 21 clips: the Nublar list plus head shake, on its own JP3 model | 2020 board, lead 2026-10-12 (D90) |
| Added by a Beta behaviour | turn left and right, rest, rest idle, get up, screech threat display (its own clip, apart from the pack call), pounce, startled, shake body, scratch, speak, flinch, nesting, yawn, sneeze | decision, section 11.10, herd decision 26, lead 2026-10-12 |

| Sounds | Used for | Source |
| --- | --- | --- |
| call (contact call), hiss, growl, laugh | Gather, threat | decision, section 11.10 |
| threat screech (own sounds, apart from the pack call) | Warning, threat display | decision, section 11.10, lead 2026-10-12 |
| attack snarl, bite, pounce charge, hurt, death | Combat, hit reaction, death | decision, section 11.9, 11.10 |
| eat, drink, snore | Needs | decision, section 11.10 |
| footstep | Walk and run | plan |
| exhale | Idle | project proposal |
| nest call, mating call, hatchling sounds (own set, not a pitched adult voice) | Breeding and young | decision, section 11.10, lead 2026-10-12 |
| yawn, sneeze, snort | Idle and the yawn, sneeze and snort effects | lead 2026-10-12 |

#### Dilophosaurus

| Clips | Which | Source |
| --- | --- | --- |
| 2020 board list | walk, run, swim, jump, drink, eat, attack 1 (bite), attack 2 (spit), call, call 2, speak, curious, idle 1, idle 2, sit, sleep, sniff air, sniff ground, look left and right, shake head, limp, death | 2020 board |
| Added by a Beta behaviour | turn left and right, rest, rest idle, get up, intimidate threat display (converted from the asset repo), roar older, roar young, startled, shake body, scratch, flinch, yawn, sneeze | decision, section 11.10, art board, lead 2026-10-12 |

| Sounds | Used for | Source |
| --- | --- | --- |
| call, hoot (intimidate display), roar, grunt | Warning, threat display, gather | decision, section 11.10 |
| spit launch, spit hit, bite hit, hurt, death | Combat, hit reaction, death | decision, section 11.9, 11.10, Dino VFX |
| eat, drink, sleep | Needs | decision, section 11.10 |
| footstep | Walk and run | plan |
| nest call, mating call, hatchling sounds (own set, not a pitched adult voice) | Breeding and young | decision, section 11.10, lead 2026-10-12 |
| yawn, sneeze, snort | Idle and the yawn, sneeze and snort effects | lead 2026-10-12 |

#### Triceratops

| Clips | Which | Source |
| --- | --- | --- |
| 2020 board list | walk, run, swim, jump, drink, eat, graze, attack (horn attack 1), attack 2 (horn attack 2), call, call 2, idle 1, idle 2, sit, sleep, sniff air, sniff ground, look left and right, shake head, limp, death | 2020 board |
| Added by a Beta behaviour | turn left and right, rest, rest idle, get up, roar threat display, ram wind-up, ram, injured/flinch, yawn, sneeze, startled, shake body, scratch, speak. Sit needs its own clip, apart from rest. Limp uses the injured clip until its own limp clip exists | decision, section 11.10, lead 2026-10-12 |

| Sounds | Used for | Source |
| --- | --- | --- |
| call (contact call), social, small roar, domination display, growl, hiss | Warning, threat display, gather | decision, section 11.10 |
| pre-charge, attack, bite, hurt, death | Combat, hit reaction, death | decision, section 11.9, 11.10 |
| graze and chew, drink, sleep | Needs | decision, section 11.10 |
| heavy footstep | Walk and run | plan |
| exhale | Idle | project proposal |
| nest call, mating call, hatchling sounds (own set, not a pitched adult voice) | Breeding and young | decision, section 11.10, lead 2026-10-12 |
| yawn, sneeze, snort | Idle and the yawn, sneeze and snort effects | lead 2026-10-12 |

#### Brachiosaurus

| Clips | Which | Source |
| --- | --- | --- |
| 2020 board list | walk, run, swim, jump, drink, eat, stand on hind legs and eat, attack (neck attack, stomp), call, call 2, idle 1, idle 2, sit, sleep, sniff air, sniff ground, look left and right, shake head, limp, death | 2020 board |
| Added by a Beta behaviour | turn left and right, rest, rest idle, get up, bellow threat display, tail whip, flinch, yawn, sneeze, startled, shake body, scratch. Rest, sit and sleep each need their own clip | decision, section 11.10, lead 2026-10-12 |
| JP3 model | its own clip set for the JP3 skin set | species card, 2020 board |

| Sounds | Used for | Source |
| --- | --- | --- |
| call (contact call), broadcast, roar, bellow, growl | Warning, threat display, gather | decision, section 11.10 |
| attack snarl, stomp, tail whip, hurt, death | Combat, hit reaction, death | decision, section 11.9, 11.10 |
| leaf rip and chew, drink, sleep | Needs | decision, section 11.10 |
| heavy footstep | Walk and run | plan |
| nest call, mating call, hatchling sounds (own set, not a pitched adult voice) | Breeding and young | decision, section 11.10, lead 2026-10-12 |
| yawn, sneeze, snort | Idle and the yawn, sneeze and snort effects | lead 2026-10-12 |

#### Gallimimus

| Clips | Which | Source |
| --- | --- | --- |
| 2020 board list | walk, run, swim, jump, drink, eat, ground peck, attack (kick, bite), call (also the alarm call), call 2, idle 1, idle 2, sit, sleep, sniff air, sniff ground, look left and right, shake head, sentry, limp, death | 2020 board |
| Added by a Beta behaviour | turn left and right, rest, rest idle, get up, startled, shake body, scratch, speak, flinch, yawn, sneeze | decision, section 11.10, herd decision 26, lead 2026-10-12 |

| Sounds | Used for | Source |
| --- | --- | --- |
| broadcast (contact call), bellow, chirp, screech, growl | Warning, threat display, gather | decision, section 11.10 |
| bite, hurt, death | Combat, hit reaction, death | decision, section 11.9, 11.10 |
| eat and peck, meat eat (omnivore), drink, sleep | Needs | decision, section 11.10, lead 2026-10-12 |
| light footstep | Walk and run | plan |
| burp | Idle | project proposal |
| nest call, mating call, hatchling sounds (own set, not a pitched adult voice) | Breeding and young | decision, section 11.10, lead 2026-10-12 |
| yawn, sneeze, snort | Idle and the yawn, sneeze and snort effects | lead 2026-10-12 |

Art order: the Gallimimus drink and eat clips come first. Neck reach waits for them (decision, section 11.14).

#### Dinosaur effects (all in Beta)

Every effect is in Beta (lead 2026-10-12). The lead accepted this matrix on 2026-10-12 with two changes. The Gallimimus gets blood drips and a bloody mouth, because it is an omnivore. Screen shake stays on T. rex, Triceratops and Brachiosaurus. On 2026-10-13 the lead added bloody claws for the T. rex and the Dilophosaurus.

| Effect | T. rex | Raptor | Dilo | Trike | Brachio | Galli |
| --- | --- | --- | --- | --- | --- | --- |
| Spit particles when roaring | yes | yes (screech) | yes | yes | yes (bellow) | no |
| Drinking water drips | yes | yes | yes | yes | yes | yes |
| Blood drips when eating | yes | yes | yes | no | no | yes (meat) |
| Bloody mouth overlay (own texture per species) | yes | yes | yes | no | no | yes |
| Bloody claw overlay | yes | yes | yes | no | no | no |
| Plant bits from the mouth | no | no | no | yes | yes | yes |
| Foggy breath in cold biomes | yes | yes | yes | yes | yes | yes |
| Yawn effect | yes | yes | yes | yes | yes | yes |
| Sneeze effect | yes | yes | yes | yes | yes | yes |
| Snort effect | yes | yes | yes | yes | yes | yes |
| Fading footprints | large biped | small biped | small biped | quadruped | sauropod | small biped |
| Dust trail when running | yes | yes | yes | yes | yes | yes |
| Air trails on attack clips | bite | claws, pounce | bite | horns, ram | tail whip, neck | kick |
| Screen shake near a large walker or roar | yes | no | no | yes | yes | no |
| Snow cover overlay while sitting or sleeping in snow | yes | yes | yes | yes | yes | yes |
| Splash rings in water | yes | yes | yes | yes | yes | yes |
| Water ripples near large animals | yes | no | no | yes | yes | no |
| Sleep Zzz | yes | yes | yes | yes | yes | yes |
| Bubbles while sleeping | yes | yes | yes | yes | yes | yes |
| Spit blob (physics projectile) and splash | no | no | yes | no | no | no |

Dino VFX also lists a mud overlay ("later on") and effects for sea reptiles and pterosaurs. None of those is in Beta.

**Art needed:** see Art list → each species (Animations, Sounds), Dinosaur effects.

**Open questions:**

- T. rex sounds: the artist names the folder that holds the real T. rex recordings (D91). The lead sources new dinosaur sounds from Jurassic World Evolution 3.
- Settled by the lead (2026-10-13): the snort needs no clip. It plays over the idle or the sniff clip. Bloody claws for all three carnivores. Only the T. rex and the Velociraptor play a nesting clip.
- Animation speed that follows body size: the lead answered "not decided" (plan default D14: not built).
- Settled by the lead (2026-10-12, follow-up pass): raptors leap now with the existing pounce clip. A crouch-then-leap clip is on the Art list for later (D53). Stalking uses the walk clip at half speed until crouch art exists (D59).
- Settled by the lead (2026-10-12):
  - sounds: the shared carnivore eat sound, own yawn, sneeze, snort and hatchling sounds, a separate Velociraptor screech
  - clips: the intimidate warning, own clips for Brachiosaurus rest, sit and sleep, Triceratops sit and Dilophosaurus roar older and young
  - the T. rex extras for every species, the limp line (D92) and the effects matrix.

**Today:** Only the T. rex has sounds in the mod. Every species plays the T. rex roar as its warning, which is a defect. No dinosaur effect exists.

**Engineering:** backlog F2.3 (old modules B24 for dinosaur art, B27 for clip events and synced poses).

### F2.4 Needs: hunger, thirst, stamina, sleep, stress and company

**In one line:** Dinosaurs get hungry, thirsty, tired, sleepy, lonely and stressed, and a park that neglects them pays for it.

**How it plays:** A hungry carnivore hunts, scavenges a carcass or eats dropped meat. A herbivore grazes the plants its head can reach and removes the block. A herd gathers around one tree or plant patch, and each animal eats a different block. A content herbivore grazes when idle. It gains a little hunger and leaves the block in place. A thirsty animal walks to water and drinks with its head at the surface. A tired predator gives up the chase, so a player can escape. A starving or stressed animal turns aggressive, loses health and dies.

**Sources:** decision (section 11.10), decision (section 11.9, head contact), design doc (needs, stress, thirst), Mod Commission (needs list), species card, lead 2026-10-12 (group feeding, contentment).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | Needs in Beta: hunger, thirst, stamina and sleep, stress. Loneliness is part of stress, with no bar of its own (D52) | decision, section 11.10, lead 2026-10-13 |
| Behaviour | Below the group minimum, stress rises over time. The hover box shows the "lonely" icon when loneliness is the main cause. The company part of contentment reads the group size | lead 2026-10-13 (D52) |
| Behaviour | Stress inputs: food and water, and company. A live-bait hunt lowers a carnivore's stress. Enclosure space and block comfort are not inputs | decision, section 11.10, 11.12 |
| Behaviour | An unmet need makes the animal aggressive, then it loses health and dies. It never attacks a fence | decision, section 11.10 |
| Behaviour | After an animal meets a need, a cooldown starts. During it, the animal does not look for that need | decision, section 11.10 |
| Behaviour | Carnivores rank prey by clade, as on the T. rex card. Players and villagers are prey. Carnivores eat carcasses and dropped meat. A kill of a player lowers hunger | decision, section 11.10, species card |
| Behaviour | A chase ends when the predator's stamina is zero. A cooldown follows before the next hunt | decision, section 11.10 |
| Behaviour | Herbivores eat grass, leaves and plants as blocks. Brachiosaurus eats high leaves, Triceratops low plants. Diet lists accept other mods' plants and meat | decision, section 11.10 |
| Behaviour | Eating and drinking count only when the head touches the source | decision, section 11.9 |
| Behaviour | Grazing together: a herd gathers around one tree or plant patch. Each animal claims a different leaf or plant block inside its reach. No two animals target the same block. The animals spread around the source and avoid their mates' spots and targets (herd decision 12, extended). Brachiosaurus takes the high leaves, smaller animals the low plants | lead 2026-10-12, herd decision 12, decision, section 11.10 |
| Behaviour | Feeders and carcasses are shared by every animal of their diet. Animals take feeding spots and turns (F2.0 "Feeding together") | lead 2026-10-12 |
| Behaviour | Idle grazing: a herbivore that is content or idle plays its graze behaviour (its eat clip) at a grass or plant block in its reach. Each idle graze gives a small hunger gain. By default it does not remove the block, so a content herd does not strip its pasture. A hungry animal still eats and removes the block. Idle grazers use the same block claims as hungry eaters, so no two animals use one block (F2.0 "Feeding together") | lead 2026-10-12, decision, section 11.10 |
| Numbers | Spot gap 0.5 blocks, turn limit 30 s while others wait, wait ring 6 blocks. A claim frees after 5 s on a feeder spot and 10 s on a plant block, if the animal has not arrived (D111) | lead 2026-10-12, lead 2026-10-13 |
| Numbers | Idle graze hunger gain `needs.idle_graze_hunger`: 1 % of the hunger maximum per graze (proposal, lead decides) | proposal |
| Behaviour | Each species is diurnal, nocturnal or cathemeral. The nocturnal gene moves the window | design doc, decision, section 11.6 |
| Behaviour | T. rex, Velociraptor and Dilophosaurus are cathemeral, each with its own two sleep windows. The three herbivores are diurnal. Windows and offsets: F2.0 | lead 2026-10-12 (D97, D98) |
| Behaviour | The Gallimimus is an omnivore. It eats plants and small meat (dropped meat, eggs), and it uses the herbivore and the carnivore feeders | lead 2026-10-12 |
| Behaviour | Lost sleep: the rest part of contentment stays at 50 or lower until the next full sleep. Stamina regenerates at half rate. Lost sleep gives no stress | lead 2026-10-12 (D105, follow-up pass) |
| Behaviour | Stress at 100 does no damage. Hunger and thirst cause the damage | lead 2026-10-12 (D103) |
| Numbers | Starvation: 1 health each 4 s at zero hunger. A Velociraptor lasts about 2.3 minutes, a T. rex about 10. Config values | decision, section 11.10 |
| Numbers | Hunger and thirst: T. rex 80 and 70 (card), Velociraptor 27 and 23, Dilophosaurus 27 and 23, Gallimimus about 40 and 35, Triceratops 67 and 58, Brachiosaurus 160 and 140. Playtests tune them | species card, decision, section 11.16, lead 2026-10-12 |
| Numbers | Stress: each active cause adds 2 per step (hungry, thirsty, alone). Start values: maximum 100, fed and in company −1, live-bait kill −20. Playtests set them | lead 2026-10-12, plan |
| Numbers | "Alone" becomes urgent after 3 in-game days (72,000 ticks) below the group minimum. Then the lonely cause of stress counts as urgent | lead 2026-10-12 (D106), lead 2026-10-13 |
| Numbers | One stress step is 1 minute (1,200 ticks). Playtests tune it | lead 2026-10-12 |
| Numbers | Seek food below 75% hunger, run to food below 25% | plan |
| Numbers | Seek water below 75% thirst, run to water below 25%. At zero thirst the animal loses 1 health each 2 s (40 ticks). Config values | lead 2026-10-12 (D104) |
| Numbers | Food value: five food tiers by item tag. The clade chunks of GH #6 are not used | plan, lead 2026-10-12 |
| Numbers | Stress threshold: one value per species in the species data, default 50, no gender split. Higher stress is worse. The Stress Tolerance gene raises it | lead 2026-10-12 (D13, D88) |
| Numbers | Need cooldown: 5 minutes (6,000 ticks) after a meal or a drink, config. An urgent need skips it | lead 2026-10-13 (D160) |
| Animation | Eat, drink, rest, get up and sleep clips per species (F2.3) | decision, section 11.10 |
| Sound | Eat, drink and sleep sounds per species (F2.3) | decision, section 11.10 |
| Effects | Drinking drips, blood drips and bloody mouth, plant bits, Zzz and bubbles, snow cover while sleeping in snow | lead 2026-10-12 |
| Failure | Lack of water lowers energy and kills faster than hunger | design doc |

**Contentment:** the needs feed the contentment score and its hover box. See F2.0 "Contentment".

**Art needed:** see Art list → each species (eat, drink, rest and sleep clips and sounds), Dinosaur effects. Shared with F2.3.

**Open questions:**

- Settled by the lead (2026-10-13): loneliness is part of stress, with no bar of its own (D52). The need cooldown is 5 minutes (D160). Feeding claims free after 5 s and 10 s (D111).
- Settled by the lead (2026-10-12):
  - the stress threshold (D13, D88), the stress step and stress at 100 (D103)
  - hunger and thirst for five species, the food tiers, the thirst lines and damage (D104)
  - the sleep windows (D97, D98) and lost sleep (D105).
- Settled by the lead (2026-10-12, follow-up pass): each active cause adds 2 stress per step. Lost sleep gives no stress. "Alone" becomes urgent after 3 in-game days (D106).

**Today:** Hunger, thirst and stamina tick, with defects: the stamina maximum is 0 so every chase ends at once, hunger never clears, and an animal that drinks still dies of thirst on day 3. Stress and social do nothing.

**Engineering:** backlog F2.4 (old module B17).

### F2.5 Groups: herds, packs, leaders, breeding, nests

**In one line:** Herds travel together, packs hunt together, a leader rules each group, and calm pairs nest and raise young.

**How it plays:** A Gallimimus herd grazes over a wide area, then follows its leader on a journey as a wedge, or in single file through a corridor. A rival challenges the leader with a threat display, and nobody gets hurt. A Velociraptor pack picks one prey, spreads around it and strikes together. A calm, fed male and female mate. The female builds a nest from the blocks around her and lays a clutch. Both adults guard the eggs and the young.

**Sources:** decision (section 11.10), herd decisions 1 to 33 (lead 2026-10-09 and 2026-10-10), species card (pair hunting, group feeding, nesting), design doc (group size), lead 2026-10-12 (group feeding).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | Animals of one species form herds and packs. Groups never mix species | decision, section 11.10 |
| Behaviour | Each group has one leader. A dominance value from size, age and the aggression gene picks it. The leader picks where the group goes | decision, section 11.10 |
| Behaviour | A rank contest is a threat display with no damage. Turf wars are not in Beta | decision, section 11.10 |
| Behaviour | Animals call to warn, gather or threaten | decision, section 11.10 |
| Behaviour | Velociraptors hunt in packs. A T. rex hunts alone or as a pair. A T. rex pair hunts as a team: one chases, and one circles to cut off the prey. A Dilophosaurus group hunts together | decision, section 11.10, species card, lead 2026-10-12 (D54) |
| Behaviour | A pack adds its strength: the sum of its adults, capped at 4 × the leader. So a pack attacks prey that a lone hunter avoids | lead 2026-10-12 (D58) |
| Behaviour | A pack gives up when the leader stops, the target is lost, or under half the pack still chases | lead 2026-10-12 (D55) |
| Behaviour | A hungry pack journeys to find prey. Otherwise it roams its home area | lead 2026-10-12 (D56) |
| Behaviour | Hungry Velociraptors and Dilophosaurus hide in cover by paths and spring out at prey. Raptors leap with the existing pounce clip. Stalkers use the walk clip at half speed | lead 2026-10-12 (D53, D59, D60) |
| Behaviour | Alarm calls cross species: any herbivore that hears one becomes alert and reacts by its own rule | lead 2026-10-12 (D63) |
| Behaviour | A herd member may detour for a need. A member with an urgent thirst or hunger (below 25 %) steps off a journey to drink or eat, then rejoins | lead 2026-10-13 (D36) |
| Behaviour | Herd meals: hybrid, queue first. The herd travels to one food source together, and each member claims its own spot. A member with no spot waits up to 30 s, then may use other food in the herd area. All regroup after the meal | lead 2026-10-13 (D111) |
| Behaviour | Group feeding: group mates share one feeder, carcass or plant source. Within a group the higher dominance eats first, then the longest wait (D57, now for every species). The source's size and the eaters' body widths set how many eat at once. The others wait nearby. Never one feeder per animal | lead 2026-10-12, species card ("Group Feeding") |
| Behaviour | Herd travel: a wedge in open ground, at most 3 abreast, single file in corridors. Mates keep space from snout to tail. An adult Brachiosaurus stays in open ground | herd decisions 1 and 10, decision, section 11.10 |
| Behaviour | A male and a female mate. The female builds a nest from vanilla blocks of her surroundings. Examples: leaves in a forest, sand in a desert, gravel by water. She plays her nesting clip (T. rex, Velociraptor) or an idle clip. Then she lays egg entities in the nest | decision, section 11.10, lead 2026-10-13 |
| Behaviour | With mobGriefing off, or with no room for a nest, she lays the eggs on the ground. The nest blocks stay after the hatch, and the pair uses the nest again. The nest size follows the egg class | lead 2026-10-13 |
| Behaviour | Eggs count as young. Parents that see a player near their eggs warn and attack. Adults guard nests and young | decision, section 11.10, lead 2026-10-13 |
| Behaviour | A pair breeds only when a nest or a free laying spot is near, the fertility roll passes, and both are below their stress threshold. Two more gates: both have full health, and no own young under half grown is within 16 blocks | decision, section 11.10, lead 2026-10-12 (D72) |
| Behaviour | A pack that loses its leader flees to safety first. Then the members with the highest and the second-highest dominance contest the lead | lead 2026-10-12 |
| Behaviour | A pack below its minimum flees big threats. A lone member looks for another pack in range. If it finds none, it is lonely | lead 2026-10-12 |
| Behaviour | A group above its maximum splits into two groups, each with its own leader. Crowding causes no stress | lead 2026-10-12 (D106) |
| Behaviour | Play: content young animals chase each other with existing clips (run, Velociraptor pounce). Play does no damage | lead 2026-10-12 (D107) |
| Behaviour | A bred young is male or female at 50/50. Breeding has no cap per area | lead 2026-10-12 (D20) |
| Behaviour | A young animal gets the middle of its parents' number genes plus clone variation. Each colour layer, skin, special gene and morph comes from one parent at random. Bred genes stay inside the edit limits. The egg gets the mix when it is laid, so a player can sample it. The small variation rolls at hatch | decision, section 11.10, lead 2026-10-12 (D71) |
| Numbers | Group size: T. rex 1 to 2, Velociraptor 3 to 6, Dilophosaurus 1 to 3, Gallimimus 6 to 12, Triceratops 4 to 6, Brachiosaurus 3 to 5. Young do not count until they are adult | species card, decision, section 11.16, lead 2026-10-13 |
| Numbers | Journeys every 2 to 5 minutes, 156 to 314 blocks. The leader waits at most 10 s for stragglers. A fenced herd picks journey targets inside its paddock only | herd decisions 3, 21, 25, lead 2026-10-12 (D40) |
| Numbers | Clutch size and nest hatch rate per species: the table below. A nest egg hatches 3 in-game days (72,000 ticks) after laying, then shakes for 60 s | species card, decision, section 11.16, lead 2026-10-13 (D79) |
| Numbers | Gestation: 1 in-game day (24,000 ticks) from mating to laying. Playtests tune it | lead 2026-10-12 |
| Animation | Call at a journey start, sentry while the leader keeps watch, startled when a threat comes within 6 blocks. Nesting clip. Pounce (Velociraptor), stalk (T. rex) | herd decision 26, decision, section 11.10 |
| Sound | Contact call, alarm call, mating call, nest call and hatchling call per species (F2.3) | decision, section 11.10 |
| Models | No nest model. The nest is vanilla blocks from the surroundings | lead 2026-10-13 |
| Failure | A neglected paddock stops breeding. False eggs are not in Beta | decision, section 11.10 |
| Failure | Most nest eggs fail. A failed nest egg cracks open, drops tissue and vanishes. The player can take the tissue to the Fossil Processor for DNA | lead 2026-10-13 (Z-8, D255) |

**Natural clutches (lead 2026-10-13, D79).** Every clutch is large, and most eggs fail. The lead raised each drafted hatch rate by a quarter (× 1.25). Config values, and playtests tune them.

| Species | Clutch | Nest hatch | Babies per clutch (mean) | Clutches with no baby |
| --- | --- | --- | --- | --- |
| Tyrannosaurus | 12 to 24 | 6 % | 1.08 | 33 % |
| Velociraptor | 8 to 13 | 25 % | 2.63 | 5 % |
| Dilophosaurus | 12 to 24 | 16 % | 2.88 | 4 % |
| Gallimimus | 15 to 30 | 15 % | 3.38 | 3 % |
| Triceratops | 15 to 25 | 11 % | 2.20 | 10 % |
| Brachiosaurus | 15 to 30 | 6 % | 1.35 | 25 % |

- The T. rex card gives 5 %. The × 1.25 rule makes it 6 %, and the lead accepted 6 % (Z-8).
- In the game: one raptor clutch makes a minimum pack of 3. A Gallimimus pair reaches its minimum herd of 6 in 1 or 2 clutches.
- A natural clutch is worth about one to three lab eggs. Lab eggs keep their own rates (D147).

**Art needed:** see Art list → Eggs and hatching (natural egg textures), each species (Animations: nesting, sentry, startled. Sounds: calls).

**Open questions:**

- Settled by the lead (2026-10-13, Z-8): a failed nest egg cracks open, drops tissue (a DNA source for the Fossil Processor), then vanishes (D255). The T. rex nest rate is 6 %. A Nublar × Sorna raptor young takes each look trait from a random parent (D249).
- Settled by the lead (2026-10-13): the clutch table (D79), the built nest and eggs as young. Young count toward the group only as adults. Nest eggs wait 3 days. Detours start at the urgent band (D36), and herd meals are hybrid (D111).
- Settled by the lead (2026-10-12): gestation, pack collapse, no breeding cap (D20), and a 50/50 gender roll. The one nest block of that date gave way to the built nest (2026-10-13).
- Settled by the lead (2026-10-12, follow-up pass):
  - hunts: T. rex team hunts and Dilophosaurus group hunts (D54), pack strength (D58), giving up (D55), ambush, leap and stalk (D53, D59, D60)
  - travel: hungry packs journey (D56), fenced herds journey inside the paddock (D40), need detours (D36), cross-species alarms (D63)
  - breeding: two more gates (D72), and the genome set at laying (D71).

**Today:** Gallimimus herd travel exists on the kash's-slop branch. The lead committed it on 2026-10-11. Its last playtest is not reported. Leaders by dominance, packs and nests do not work. Breeding lays one egg that shares the mother's genome.

**Engineering:** backlog F2.5 (old modules B18, B26).

### F2.6 The player: threat displays, detection, taming

**In one line:** Dinosaurs notice you, warn you before they strike, run or stand by species, and a calm one can be tamed.

**How it plays:** An animal notices a player by sight or sound. A sneaking player far away often stays hidden. Before it attacks, a dinosaur stops, faces the player and gives its own warning: a T. rex roars, a Dilophosaurus spreads its frill in its intimidate display. A player who backs off avoids the fight. A Gallimimus runs, a Triceratops charges, and a Brachiosaurus swings its tail. A calm animal can be tamed, and then it follows, stays and defends its owner.

**Sources:** decision (section 11.10), design doc (detection levels, dinosaur against player), Special Genes (poison), Dino VFX (spit), lead 2026-10-12 (M2 open-items pass).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | Every animal gives a warning before it attacks, with its own clip and its own sound | decision, section 11.10, lead 2026-10-12 |
| Behaviour | Gallimimus runs. Triceratops attacks. Brachiosaurus defends: a roar warning (its bellow), then a tail whip and a stomp. This replaces the flee default | decision, section 11.10, lead 2026-10-12 (D50) |
| Behaviour | The three carnivores stand or retreat by threat and group size. Each animal adds the summed threat of its pair or pack to its own threat. It stands unless the attacker's threat is more than 2 × that sum | lead 2026-10-12 (D13, D112) |
| Behaviour | Personal space: each animal has a circle around it, sized by its body. A standing animal warns a noticed player inside it, then attacks. A fleeing species flees at once, with no warning. This personal space replaces the "territory" radius. Beta has no home territory | decision, section 11.10, lead 2026-10-12 (D218), lead 2026-10-13 (D166) |
| Behaviour | Young provoke always: a player noticed near a baby provokes its parents at any distance they can see. Eggs count as young | decision, section 11.10, lead 2026-10-12, lead 2026-10-13 |
| Behaviour | An aggressive animal notices players 50 % farther away, and its warning cooldown halves. A standing species attacks a player outside its personal space, after a warning. Fleeing species still flee | lead 2026-10-12 (D102) |
| Behaviour | Animals detect by sight and sound only. Scent trails are not in Beta, which reverses the decision of 2026-10-05. A predator that loses sight of its target searches the last seen spot. Vision that needs movement is not in Beta | decision, section 11.10, lead 2026-10-12 |
| Behaviour | A calm, low-stress animal can be tamed. It does not attack its owner. It follows, stays and defends. Hunger or stress ends the bond | decision, section 11.10 |
| Behaviour | Taming: a trust score rises while the player is near a calm animal. Hand-feeding (use its food on it) gives a big boost. Trust rises only while stress is below 20. The lead drops the design doc's zero-stress rule | lead 2026-10-12 |
| Behaviour | A roar scares passive mobs and villagers near it for a few seconds. Villagers flee loose carnivores as they flee zombies | lead 2026-10-12 (D19) |
| Behaviour | A Dilophosaurus spit blinds. With the poison gene it also poisons | Special Genes, Dino VFX, plan |
| Numbers | Detection chance 25% to 85% by distance and activity. Rain, snow and snowy biomes shrink the radius. The detection range stays apart from the personal space | decision, section 11.10, design doc, lead 2026-10-12 |
| Numbers | Detection levels: sneak 25%, walk or stand 40%, sprint, jump or hit 60%, loud acts (weapon, block breaking, vehicle, noise) 85%. Playtests tune them | design doc, lead 2026-10-12 |
| Numbers | Notice roll: every 10 s (200 ticks) for each player in range | lead 2026-10-12 (D15) |
| Numbers | The warning lasts as long as the species clip. Skip radius 5 blocks, cooldown 10 to 20 s (plan start values) | plan |
| Numbers | Spit: 4 damage, 30 s of blindness, range 10 blocks (plan start values) | plan |
| Numbers | Personal-space radius by body size. Config start values that playtests tune: T. rex 10, Brachiosaurus 10, Triceratops 8, Dilophosaurus 6, Velociraptor 6, Gallimimus 5 blocks | lead 2026-10-13 (D165) |
| Animation | Own warning clip: T. rex roar, Velociraptor screech, Dilophosaurus intimidate display (then spit if the player stays), Triceratops roar then ram wind-up and ram, Brachiosaurus bellow, Gallimimus alarm call. Sit for "stay" | decision, section 11.10, lead 2026-10-12 (clip choice) |
| Sound | Own warning sound: T. rex roar, Velociraptor screech (apart from the pack call), Dilophosaurus hoot during the frill display, Triceratops snort (domination display), Brachiosaurus bellow, Gallimimus alarm screech | decision, section 11.10, lead 2026-10-13 |
| Effects | Spit particles on a roar, screen shake on a large roar, air trails on attacks, Dilophosaurus spit blob and splash | Dino VFX, lead 2026-10-12 |
| Failure | A player who leaves the personal space during the warning takes no hit | plan, lead 2026-10-12 |

**Contentment:** every player who looks at a dinosaur sees its contentment band and its worst need in the hover box (lead 2026-10-12, D18 answered). Taming still reads stress, not contentment. See F2.0 "Contentment".

**Art needed:** see Art list → each species (Animations: warning clips, sit. Sounds: warning sounds), Dinosaur effects. Shared with F2.3.

**Open questions:**

- Settled by the lead (2026-10-13): no home territory (D166), eggs count as young, the personal-space radii (D165) and the warning sounds.
- Settled by the lead (2026-10-12):
  - Beta has no scent, so D16 is moot.
  - The Brachiosaurus defends (D13, D50).
  - The detection levels, the trust rule for taming, roar fear and villager flight (D19), and the warning clips.
- Settled by the lead (2026-10-12, follow-up pass): carnivores stand or retreat by threat and group size (D13). Fleeing species flee in personal space with no warning (D218). One notice roll every 10 s (D15).

**Today:** Every species plays the T. rex roar as its warning, which is a defect. Detection chance and taming do not work.

**Engineering:** backlog F2.6 (old module B19).

### F2.7 Special genes and quirks, DNA from your own dinosaurs

**In one line:** Seven special genes and donor quirks change what an animal can do, and the player's own dinosaurs supply fresh DNA.

**How it plays:** In the Edit tab, the player adds a special gene from a donor animal. Every Velociraptor hatches with hyper intelligence. It opens doors, raids chests, and climbs out where a fence section has no power. Every Dilophosaurus hatches with poison, so its bite and spit poison. A glow squid donor gives glowing patterns. A syringe draw from a live dinosaur, the bones and tissue of a carcass, or a sample from an egg give much more DNA than a fossil.

**Sources:** Special Genes, donor table, design doc (special genes, items), decision (section 11.2, 11.6).

| Gene | Donors | Effect | Compatible Beta species |
| --- | --- | --- | --- |
| Camouflage | Cuttlefish (by command until it exists) | Hard to see while still | T. rex, Velociraptor, Dilophosaurus (lead 2026-10-12, D42) |
| Gender swapping | Vanilla frog in Beta | Only in a one-gender group, with no animal of the other gender, one animal changes gender over 10 in-game days. It commits on day 8. The skin stays | All |
| Bioluminescence | Glow squid | Glow-in-the-dark patterns. Looks only | All six (lead 2026-10-12) |
| Lysine contingency | Soy or chicken. No pills item in Beta (D170) | A full lysine reserve lasts 3 in-game days. When it is empty: coma (sleep), then death 1 in-game day later. Lysine-rich food (chicken, eggs) tops it up. Carnivores eat lysine plants only with no other source | All |
| Poison | Cave spider, bee | Bite or spit poisons the victim | Dilophosaurus. Every Dilophosaurus carries it from hatch (lead 2026-10-12, D181) |
| Hyper intelligence 1.0 and 2.0 | Camel, parrot, wolf, fox, mule | Opens doors, steals from chests, detects unpowered fences and climbs out at a dead section, +15% intelligence and food and water detection | Velociraptor, Dilophosaurus, Gallimimus (1.0). Velociraptor (2.0). Every Velociraptor carries it from hatch (lead 2026-10-12, D43) |
| Thermal detection | Tree frog, squid, glow squid, salmon, pit viper | Senses players through walls | T. rex, Velociraptor, Dilophosaurus (lead 2026-10-12, D42) |

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | All seven special genes are in Beta. Donors that do not exist give their gene by command or in creative mode | decision, section 11.6 |
| Behaviour | Diet genes (grass, mushroom blocks), quirks and donor palette shifts are in Beta. Quirks: chicken clucking, parrot sound imitation, llama item carrying and llama spit. The cocoa allergy is not in Beta | decision, section 11.6, donor table, lead 2026-10-13 (D112) |
| Behaviour | Villager DNA gives every gene a random value each use | decision, section 11.6 |
| Behaviour | Special gene slots: 1 at 50%, 2 at 75%, 3 at 100% DNA | design doc, lead 2026-10-12 (D51) |
| Behaviour | A hyper-intelligent animal that notices an unpowered fence walks to the dead section and climbs out. This is a lead-approved exception to fence parity. Other animals keep away from fences | lead 2026-10-12 (D46) |
| Behaviour | Stress Tolerance is a basic number gene, not a special gene. It raises the species stress threshold. Calm donors give it, for example cat and sheep. The design doc name was "Lower Threshold Stress" | design doc, lead 2026-10-12 (rename) |
| Behaviour | Each quirk plays at a random moment every 2 to 5 minutes (config). A parent that has a quirk passes it with a 50% chance | lead 2026-10-12 |
| Behaviour | Own dinosaurs give DNA three ways: a syringe draw from a live dinosaur, remains (fresh bones, tissue) in the Fossil Processor, a sample from an egg. Fresh material gives much more DNA than a fossil | decision, section 11.2 |
| Numbers | Yield: a syringe draw from a live dinosaur gives 25 to 50%. Remains (fresh bones, tissue) in the Fossil Processor give 50%. An egg sample does not harm the egg | lead 2026-10-12 (D03, D04) |
| Numbers | Lysine: a full reserve lasts 3 in-game days. Coma when it is empty, death 1 in-game day later. Config values, tuned in playtest | lead 2026-10-12 |
| Numbers | Gender swap: 10 in-game days, decision on day 8 | Special Genes |
| Items | Lysine-rich food (chicken, eggs), soft tissue, fresh bones. No Lysine Pills item | design doc, species cards, lead 2026-10-12 (D170) |
| Skins | One bioluminescence glow mask per species (6 textures), shared by all its skin sets | Special Genes, lead 2026-10-12 |
| Effects | Bioluminescent glow, the camouflage look. Vanilla poison and blindness effects | Special Genes |
| Sound | Clucking plays a chicken cluck. Imitation plays the imitated mob's sound | donor table |
| Failure | Lysine shortfall: coma, then death | Special Genes |

**Art needed:** see Art list → each species (Skins: bioluminescence glow mask), Fossils and amber (soft tissue, fresh bones).

**Open questions:**

- Settled by the lead (2026-10-13, Z-8): the rest of D112. Camouflage changes only how easily players see the animal, not the ambush wait. A tame animal with llama DNA carries a few items for its owner. Llama spit gives a small knockback and no damage. The cocoa allergy leaves Beta.
- Settled by the lead (2026-10-12, follow-up pass): camouflage for T. rex, Velociraptor and Dilophosaurus (D42). No Lysine Pills item (D170). Every Dilophosaurus carries poison from hatch (D181). The gene "Lower Threshold Stress" has the new name "Stress Tolerance".
- Settled by the lead (2026-10-12):
  - genes: gene slots (D51), the gender swap trigger, thermal detection (D42), the Stress Tolerance gene
  - hyper intelligence from hatch (D43) and its fence climb (D46)
  - DNA: the egg sample (D03) and the yields (D04)
  - one glow mask per species, the lysine reserve, and the quirk rates and heredity.
- The scent gene (RC9, D48) is moot, because Beta has no scent.

**Today:** Special genes, quirks and remains have no code (section 11.15).

**Engineering:** backlog F2.7 (old modules B10, B04 for remains, live syringe and egg sample).

---

## M3. Park

M3 turns a working lab into a working park.

Players build electric fences that behave exactly like the original 1.12.2 fence: wires, lamps, shocks and repairs. A power grid of generators and pylons feeds the lab and long fence lines. Feeders keep carnivores and herbivores fed from outside the paddock, and a live-bait feeder lets a carnivore hunt. Every park block has a survival recipe from vanilla materials.

The milestone ends with a playtest of a powered, fed paddock.

**Lead answers of 2026-10-12 (M3 pass).** These rules change:

- The voltage sign is the parity empty-hand click. It has no item and no recipe (D78 gap 1).
- Fence sounds are an addition to parity: a hum loop, a shock zap and a wire snap (D183).
- The grid remembers power while a generator's chunk is not loaded (D21).
- Beta has no comparator output and no power meter block (D27).
- Every feeder is an in-ground hatch that opens on a timer. The first model reading was wrong (D190).

**Lead answers of 2026-10-12 (follow-up pass).** These rules change:

- A sneak-click removes a voltage sign. Empty-hand clicks on the wire cycle its three looks (D216).
- The Wire Spool shows 10 fill stages, not 12. This is a deviation from parity F22 for art reasons (D184).
- Every feeder screen gets the timer buttons. An opening lasts until all animals finish eating (D194).
- Beta has no decorative fences (D78 gap 2), and the fence switch is a lever on a post plate (D186).
- No machine or feeder gives a comparator output (D27, D28).

**Lead answers of 2026-10-13 (final pass).** These rules change:

- The pylon head sits on top of a fence pole block. One pole block serves a fence or a pylon (D240). Only a pole used as a pylon takes any height. A fence post keeps 3 or 8 blocks (D253, Z-8).
- A pylon cable behaves like a fence wire. It costs ceil(length ÷ 20) spools (D241).
- An overload trips the generator. The player builds another generator (D242).
- The fence gets power-up and power-down sounds (D238). Each feeder gets a close clip (D239). Code draws the live-bait chain (D235).
- The lead approved the park recipes with changes (D202).

### F3.1 Fences: 1:1 with the 1.12.2 fence

**In one line:** An electric fence that plays exactly like the original 1.12.2 fence, plus a switch to cut a section and fence sounds.

**How it plays:** The player places posts and strings wires between them with a wire spool. Power spreads post by post, and the lamps light one after another. A live wire shocks anything that touches it and throws it back. Without power, any animal can climb the wire, and a T. rex tears through it. A player cuts one wire at a time and repairs it with a spool. A live fence hums when the player comes near. A shock zaps, and a wire snaps when a T. rex tears it. An empty-hand click on a wire hangs a voltage sign, and more clicks change its look. A sneak-click removes it.

**Sources:** original 1.12.2 fence (lead 2026-10-10: 1:1 parity, fence rows F1 to F32 in the plan). Also the wiki "Fences", the 2019 fence demo video, decision (section 11.11) and lead 2026-10-12.

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | Two posts: low security is 3 blocks high with 6 wires, high security 8 blocks with 8 wires. Only posts of one type link | fence F1, F3 |
| Behaviour | Right-click post A with a spool, then post B. Wires string between them, and B becomes the next start. Wires follow any slope. A solid block on the line leaves a gap | fence F3, F6 |
| Numbers | Longest span 15 blocks. Cost: ceil(distance ÷ 20 × post height) spools. Free in creative. A message says when a span is too long or the spools are too few | fence F4, F5 |
| Behaviour | A post turns to face its wires. An empty-hand click flips it 180° | fence F7 |
| Behaviour | Wires are solid. The crosshair picks one wire and outlines it. A click breaks only that wire, at once | fence F8 to F10, F32 |
| Behaviour | A cut wire hangs bent up to 30°. A cut end on a powered run sparks, and the sparks glow | fence F11, F12 |
| Behaviour | Four repairs: click the top or bottom of a wire, click a wire end, use a spool on a face beside the gap, or place the spool as a block in the gap. Each uses 1 spool and plays the place sound at pitch 0.8 | fence F13 to F17 |
| Behaviour | An empty-hand click on a wire hangs a voltage sign. Each next empty-hand click on the wire shows the next look: 1, 2, 3, then 1 again (`voltage_warning1` to `3`). Sneak + click removes the sign. Empty-hand clicks on a post still flip the post (F7). No sign item and no recipe | fence F18, decision, section 11.11, lead 2026-10-12 (D78 gap 1, D216) |
| Behaviour | Break one end post: its wires hang unpowered. Break both: the wires go. The Fence Remover removes one wire line. The Creative Fence Remover removes a whole fence | fence F19 to F21 |
| Behaviour | The spool icon shows, in 10 stages, how much of the stack a pending span uses. It uses the 10 stage sprites of the asset repo. The original had 12 stages. This deviation from parity is for art reasons | fence F22, lead 2026-10-12 (D184) |
| Behaviour | One fed post powers the whole connected fence. One broken wire block kills that wire | fence F23 |
| Behaviour | A live wire deals ½ heart through armour, throws the entity back and bursts 15 sparks. A death message names the fence. Dinosaurs take it too. A shock uses no energy | fence F25, lead 2026-10-12 (D23) |
| Behaviour | An unpowered wire is a ladder for every living entity. Mobs always path around wires and posts | fence F26, F27 |
| Behaviour | Only a T. rex breaks a fence, and only an unpowered one: a 50% roll per check, within 10 blocks, once. It ignores live wires | fence F29, decision, section 11.11 |
| Behaviour | Additions to the original: a switch or a redstone signal cuts a section. Posts, spool and Fence Remover get survival recipes. The fence gets five sounds (see Sound). A hyper-intelligent animal climbs out at a dead section (D46) | decision, section 11.11, lead 2026-10-12 |
| Behaviour | Vanilla fences and walls hold dinosaurs with normal collision. Dinosaurs cannot break them | lead 2026-10-12 (D35) |
| Behaviour | A live fence stops a vehicle like a wall and shocks the driver once | lead 2026-10-12 (D26) |
| Numbers | A post holds 350 FE and loses 10 FE per tick. Above 300 FE it shares up to 300 FE per tick. A post with no supply goes dark after 1.75 s | fence F30, F31 |
| Animation | No keyframed clip. A powered post shows its lit lamp model and gives light 10 (low) or 15 (high). Each cut wire keeps one fixed bend | fence F24, F11, original 1.16 |
| Sound | A hum loop plays near a live fence. A live wire plays a shock zap. A wire snaps when a T. rex breaks it. A power-up hum plays when the fence gets power, and a power-down click when it loses power. A repair plays the place sound at pitch 0.8. The original fence had only the repair sound | lead 2026-10-12 (D183), lead 2026-10-13 (D238), original 1.16 |
| Effects | Spark particle: small (scale 0.25), short-lived, glowing, falls with gravity | original 1.16 |
| Models | Low and high posts with lit and unlit tops, wires, and a voltage sign hanging on a wire. The switch block is a lever like the vanilla lever on a metal post plate, a small new model | fence F18, F24, decision, section 11.11, lead 2026-10-12 (D186) |
| Items | Low post, high post, wire spool, Fence Remover, Creative Fence Remover (creative only), switch | fence F20, F21, decision, section 11.11 |
| Failure | Fuel ends: the lamps go dark post by post within seconds. Gates, the overhanging fence, decorative fences and the film's four fence kinds are not in Beta | fence F30, decision, section 11.11, lead 2026-10-12 (D185, D78 gap 2) |

**Art needed:** see Art list → Fences (posts, lamps, textures, wires, voltage signs, switch lever and post plate, sounds, sparks, 10 spool stages, item sprites).

**Open questions:**

- No open questions for the fence itself. Settled by the lead (2026-10-13, Z-8): a fence post keeps the parity heights, 3 blocks (low) and 8 blocks (high). Only a pole used as a pylon takes any height (D253).
- Settled by the lead (2026-10-13): fence power-up and power-down sounds (D238).
- Settled by the lead (2026-10-12, follow-up pass):
  - The voltage sign hangs on a wire. Empty-hand clicks cycle looks 1, 2, 3, 1. A sneak-click removes it. Post clicks still flip the post (D216).
  - The spool shows the 10 repo stages, not 12 (D184). This replaces "the artist draws 12 stages".
  - The switch is a lever on a metal post plate (D186). Beta has no decorative fences (D78 gap 2).
- Settled by the lead (2026-10-12): the voltage sign is the parity empty-hand click, with no item (D78 gap 1). The fence gets a hum, a zap and a snap (D183). The film fence kinds stay out of Beta (D185). Shocks use no energy (D23). Vanilla fences hold dinosaurs (D35). A live fence stops a vehicle and shocks the driver (D26).

**Today:** Posts, wires, three of the four repairs and the shock work. Lamps never light, cut wires are invisible, sparks never show, the shock counts as thorns damage, and every post does work every tick.

**Engineering:** backlog F3.1 (old module B15).

### F3.2 Power grid: Large Generator, pylons, fence power

**In one line:** A Large Generator and pylon lines carry power from the lab to every fence line of a large park.

**How it plays:** One basic generator runs the four lab machines and a first paddock of about 10 fence posts. As the park grows, its fences need more power than the machines, so the player builds a Large Generator. Its screen shows the fuel, the flame, the energy and the line status. Its green and red lights blink while it runs. The player puts a pylon head on top of a fence pole and sets the height of that pylon pole. The player links pylons to each other and to fence posts with the Wire Spool, up to 32 blocks apart. A pylon cable follows any slope, like a fence wire. A pylon also feeds the nearest post within 4 blocks, and it hums. When fuel ends, the generator runs on its buffer for about 5 s. Then the lamps go dark post by post over about 2 s. When the load is more than the supply, the generator trips.

**Sources:** decision (section 11.11), Mod Commission (power system), 2020 board and art board (generator and pylon models), fence F30 and F31, lead 2026-10-12.

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | Beta has a basic generator and a large generator. Backup generators are not in Beta | decision, section 11.11 |
| Behaviour | A generator burns any furnace fuel. It burns while it has fuel, also with no load | decision, section 11.11 |
| Behaviour | The large generator makes fuel last longer | decision, section 11.11 |
| Behaviour | Pylons carry power across long distances with no loss. The player links pylon to pylon and pylon to post with the Wire Spool. A pylon also feeds the nearest post within 4 blocks, with no link | decision, section 11.11, lead 2026-10-12 (D189) |
| Behaviour | The pylon head sits on top of a fence pole block. One pole block serves as a fence pole or a pylon pole. The player sets any height only for a pole used as a pylon. A fence post keeps 3 blocks (low) or 8 blocks (high), as fence parity says | lead 2026-10-13 (D240, D253) |
| Behaviour | A pylon cable behaves like a fence wire: it goes in any direction and follows any slope | lead 2026-10-13 (D240) |
| Behaviour | Overload: when the demand on a network is more than its supply, the generator trips and stops. It stays off until the player removes load or adds a generator | lead 2026-10-13 (D242) |
| Behaviour | Fences draw from the grid: each post uses 10 FE per tick and holds 350 FE | fence F30 |
| Behaviour | Other mods' cables connect to generators and machines only. Posts, pylons and conduits stay closed to them. An outside cable can take the full output of a generator | decision, section 11.8, lead 2026-10-12 (D24), lead 2026-10-13 |
| Behaviour | No comparator output and no power meter block in Beta. The hover box is always on, and a client setting hides it | lead 2026-10-12 (D27) |
| Numbers | Coal lasts 80 s in the basic generator. One basic generator runs the four base-tier machines plus about 10 posts | decision, section 11.11 |
| Numbers | Pylon link range 32 blocks. Pylon feed range to the nearest post 4 blocks | lead 2026-10-12 (D189) |
| Numbers | Large Generator: 800 FE per tick (4 × the Basic Generator), and fuel lasts 2 × longer. It runs 80 posts alone, or about 48 posts beside a fully upgraded lab. Playtests tune it | lead 2026-10-13 (D188) |
| Numbers | Pylon cable cost: ceil(length ÷ 20) spools for one cable. The pole height does not count | lead 2026-10-13 (D241) |
| Numbers | One feed point carries about 31 posts on a closed loop, by the parity share of 300 FE per tick. A large fence needs more feed points: pylons or generators | lead 2026-10-13 |
| Screen | The Large Generator has a screen of its own: fuel slots, a burning flame, an energy bar and the line status | lead 2026-10-12 (D187) |
| Animation | The green and red lights blink while the Large Generator runs. No other motion. The original generator had no model and no animation | lead 2026-10-12 (D187), original 1.16 |
| Sound | Generator sounds of its own (D129). A hum loop near a pylon | lead 2026-10-12 (D189) |
| Models | Large generator (base, green lights, red lights, details, high-voltage sign), pylon head on a fence pole, power line between pylons | 2020 board, art board, lead 2026-10-13 |
| Items | Large Generator, pylon head | decision, section 11.11, lead 2026-10-13 |
| Failure | Fuel ends: the generator runs on its buffer for about 5 s. Then each post runs on its own 350 FE, and the lamps fade post by post over about 2 s. While a generator's chunk is not loaded, its fence keeps its last power state. When the chunk loads, the generator burns fuel for the time that passed. A developer decides whether that burn makes energy. No forced chunk loading | fence F30, F31, lead 2026-10-12 (D21, D22), lead 2026-10-13 |

**Art needed:** see Art list → Power (Large Generator, pylons, power line).

**Open questions:**

- Settled by the lead (2026-10-13, Z-8): pole height and fence parity (D253). Only a pole used as a pylon takes any height. Fence posts keep 3 and 8 blocks, with 6 and 8 wires (F3.1). Example: a player puts a pylon head on a pole and raises it to 6 blocks.
- Settled by the lead (2026-10-13): the Large Generator values (D188), the pylon head on a fence pole (D240) and the cable cost (D241). Also the trip on overload (D242), about 31 posts per feed point, and the 5 s buffer.
- Settled by the lead (2026-10-12):
  - the Large Generator screen and its blinking lights (D187)
  - the pylon range, links, feed and hum (D189)
  - the grid memory (D21), the fade buffer (D22) and outside cables (D24)
  - no comparator and no power meter (D27).

**Today:** The mod has one coal generator with no model. It makes 0.2 FE per tick, so it cannot run even one fence post. No pylon or Large Generator exists.

**Engineering:** backlog F3.2 (old module B14, pylons, Large Generator and fence power).

### F3.3 Feeders: carnivore, herbivore, live bait

**In one line:** Seven in-ground feeder hatches keep a paddock fed from outside the fence. Two are for carnivores, four for herbivores and one for live bait.

**How it plays:** The player places a feeder on the ground like any block, with no digging. Only its hatch lid shows. The player fills it by hand, with a hopper or with a pipe, from outside the paddock. The player sets the feeding timer on the feeder screen: 2, 5, 10 or 20 minutes. On the timer, the hatch doors slide apart and the food rises out of the ground. Carnivores get meat on a platform. Herbivores get a potted plant on a lift, at the height of their reach. The lift is low for a Triceratops or a Gallimimus, and very tall for a Brachiosaurus. Hungry animals gather around the open hatch and eat side by side, and the rest wait their turn. The hatch stays open until all animals finish eating. After 5 s with no bite, the food sinks and the hatch closes. An empty feeder always closes. For live bait, the player leads a vanilla animal to the Live-Bait Feeder and right-clicks it. The feeder keeps the animal in its pen. At the set interval, one animal comes out on a chain from the feeder's pole, and a carnivore hunts it.

**Sources:** decision (section 11.12), 2020 board (feeder family), art board (feeder models), feeder .bbmodel files and their clips (lead 2026-10-12). Lead 2026-10-12: shared feeders, models, motion, screens, food, live bait.

**Changed on 2026-10-12:** the lead's clip review replaces the first model reading. Every feeder is an in-ground hatch, and the old model heights (2.9 to 8.7 blocks) were mostly shaft below the ground (D190).

Which model is which feeder (lead 2026-10-12, D86). Each feeder is a block of its own. In each model, y = 0 is the ground.

| Feeder | Model | For | Clips in the model |
| --- | --- | --- | --- |
| Small Carnivore Feeder | Feeder 1 (3 × 3 hatch) | All carnivores | "Give me some meat" (9 s, holds its last frame). The doors slide apart at 0.75 to 2 s. The meat platform rises 14 px at 3 to 4.4 s. "Idle" (1 s) |
| Large Carnivore Feeder | Feeder 2 (5.25 × 4.88 hatch). More feeding spots | All carnivores | "Give me some meat" (9 s). "Idle" (1 s) |
| Low Herbivore Feeder | Feeder 5 | Triceratops, Gallimimus | "Open Feeder" (8 s). "idle(open)" (1 s). "idle" (no motion) |
| Medium Herbivore Feeder | Feeder 3 | Any herbivore that reaches its food (D217) | "Open Feeder" (8 s, plays once). The doors slide apart in 1 s. The potted tree rises 55 px (about 3.4 blocks) at 0.5 to 5 s, with a bounce. "idle(open)" (1 s). "idle" (no motion) |
| Tall Herbivore Feeder | Feeder 4 | Any herbivore that reaches its food (D217) | The same clips as Feeder 3 |
| Very Tall Herbivore Feeder | Feeder 6 | Brachiosaurus | "Open Feeder" (8 s, lifts a much taller tree). "idle" and "idle2" (no motion) |
| Live-Bait Feeder | Feeder 1 with a pole (new art). Code draws the chain | All carnivores, bait by size | The Feeder 1 clips. The chain holds the animal that is out |
| Fish Feeder | Fish Feeder (a cage) | Sea reptiles | Not in Beta |

Feeder motion (lead 2026-10-12). The original mod had no feeder.

| Feeder | Motion | When it plays |
| --- | --- | --- |
| All feeders | Closed | Only the hatch lid shows. The food waits under the ground |
| Food feeders | Open | The feeding timer fires, and the feeder holds food. The doors slide apart and the food rises. The clip holds its last frame |
| Food feeders | Open loop | Loops while the hatch stays open ("Idle" or "idle(open)"). Feeder 6 has no loop and holds its open pose |
| Food feeders | Close | After 5 s with no bite, the close clip plays. Until the artist delivers it, code plays the open clip in reverse. An empty feeder always closes |
| Live-Bait Feeder | Release | At the set interval, the hatch opens and one animal comes out on the chain. No new animal comes out while the last one lives |
| All feeders | Fill look | The empty look at an empty gauge, the full look when the gauge holds food |

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | Seven feeders are in Beta: the Small and Large Carnivore Feeders, the Low, Medium, Tall and Very Tall Herbivore Feeders, and the Live-Bait Feeder. Each is a block of its own | decision, section 11.12, lead 2026-10-12 (D86) |
| Behaviour | Every feeder is an in-ground hatch. Only the hatch is a block. The renderer draws the shaft and the rising food with no blocks below. No digging and no block clearing | lead 2026-10-12 (D190, D192) |
| Behaviour | The hatch opens on a feeding timer. The player picks the interval on the screen: 2, 5, 10 or 20 min. An opening lasts until all animals finish eating. The hatch closes after 5 s with no bite. An empty feeder always closes | lead 2026-10-12 (D193, D194) |
| Behaviour | Feeders 3 and 4 ship in Beta. Any herbivore that reaches the food can eat from them | lead 2026-10-12 (D217) |
| Behaviour | The player fills a feeder by hand, hopper or pipe, and can do it from outside the paddock | decision, section 11.11, 11.12 |
| Behaviour | The gauge stores the food. Inputs convert into the gauge up to 100 %. The slots hold the inputs above 100 % as an overflow buffer | lead 2026-10-12 (D195) |
| Behaviour | Herbivore feeders take four food tags: leaves and saplings, crops and produce, grass and ferns, and hay bales. Crops and produce: wheat, carrots, potatoes, beetroot, melon, pumpkin, apples, berries. Grass and ferns include seagrass and kelp. A hay bale counts as 9 wheat. Other mods' food in the tags counts | lead 2026-10-12 (D196), decision, section 11.10 |
| Behaviour | Carnivore feeders take meat of the carnivore diet. Other mods' food counts | decision, section 11.10 |
| Behaviour | A dinosaur eats only when its head touches the food. Each herbivore feeder lifts its food to one height, for the reach of its species | decision, section 11.9, lead 2026-10-12 (D191) |
| Behaviour | Every feeder is shared by all animals of its diet, of every species. Animals gather around it and take feeding spots along its open sides. How many eat at once depends on the feeder's size and the animals' body widths. The rest wait nearby and take turns: within a group the higher dominance first, then the longest wait (D57). A paddock never needs one feeder per animal | lead 2026-10-12, 2020 board (one herbivore feeder for Triceratops, Parasaurolophus, Gallimimus) |
| Behaviour | Live bait loading: the player leads a vanilla animal to the Live-Bait Feeder on a lead and right-clicks the feeder. The feeder stores the animal in an internal pen and counts the animals of each type | lead 2026-10-12 (D198) |
| Behaviour | Live bait release: the player sets an interval on the screen: 2, 5, 10 or 20 min. At each interval, one animal comes out on the chain. No new animal comes out while the last one lives. No release on hunger or on a redstone signal | lead 2026-10-12 (D199, D200) |
| Behaviour | Bait by size: a small carnivore hunts a chicken, a rabbit, a pig or a sheep. A large carnivore also hunts a cow, a horse or a goat. The hunt lowers stress and counts as a hunt | lead 2026-10-12 (D201) |
| Behaviour | The block shows its fill look. No comparator output | plan, lead 2026-10-12 (D27, D28) |
| Numbers | Gauge 0 to 100 %. Overflow: the 3 × 3 slots. Live-bait stress relief −20 (start value) | lead 2026-10-12, plan |
| Numbers | Dinosaurs that eat at once: one per stretch of open feeder edge equal to the eater's body width plus 0.5 blocks. Example: 3 Gallimimus on one 5.25-block side. Turn limit 30 s while others wait. Wait within 6 blocks. A spot claim frees after 5 s if the animal has not arrived (D111) | lead 2026-10-12, lead 2026-10-13 |
| Numbers | Feeding timer: 2, 5, 10 or 20 min. The player picks it on the screen. Open time: until all animals finish eating. Then 5 s with no bite closes the hatch. Food per bite and gauge points per item: not set | lead 2026-10-12 (D194) |
| Screen | Every feeder screen has the timer interval buttons (2, 5, 10, 20 min). Herbivore screen, shared by all four heights: 3 × 3 food slots, a green gauge, the food % and "Eating: X of Y". Carnivore screen, shared by small and large: 3 × 3 meat slots and a red gauge. Live-Bait screen: the pen count per animal type, release interval buttons (2, 5, 10, 20 min), the animal on the chain and the next release. All three use the lab GUI style (`processor.png`) | lead 2026-10-12 (D194, D195) |
| Animation | See the feeder motion table above | lead 2026-10-12, feeder clips |
| Sound | Hatch open and close, food lift, bait release | proposal, lead decides |
| Models | Feeders 1 to 6, each with an empty and a full look and a close clip. The Live-Bait Feeder is Feeder 1 with a pole (new art). Code draws the chain from the pole to the animal, like a lead | art board, lead 2026-10-12 (D197), lead 2026-10-13 (D235, D239) |
| Items | One item per feeder: seven items | decision, section 11.12, lead 2026-10-12 |
| Failure | "Dino Feed" is not in Beta. With an empty feeder, herbivores eat vanilla plants | decision, section 11.12, 11.13 |

**Art needed:** see Art list → Feeders (models, the Live-Bait pole, empty and full looks, open and close clips, sounds, item sprites, three screens).

**Open questions:**

- Settled by the lead (2026-10-13): claims free after 5 s on a spot (D111). Code draws the live-bait chain (D235). Each feeder gets a close clip from the artist (D239).
- Settled by the lead (2026-10-12, follow-up pass):
  - The timer buttons (2, 5, 10, 20 min) are on every feeder screen. An opening lasts until all animals finish eating, and 5 s with no bite closes it (D194).
  - Live bait releases on the interval only (D200).
  - Feeders 3 and 4 ship. Any herbivore that reaches them eats there (D217).
  - No comparator output on feeders or machines (asker's reading of D27 and D28: the control room has only the fence switch).
- Settled by the lead (2026-10-12):
  - the feeder models and sizes (D86, D190), the herbivore lift heights (D191) and the virtual shaft (D192)
  - the timer motion (D193), the screens and the gauge (D195) and the herbivore food (D196)
  - the live-bait model, loading, release and bait (D197 to D199, D201).

**Today:** One carnivore feeder (Feeder 1) exists with its open clip and its full and empty looks. It skips its third slot, does not save its contents, and eating does not raise hunger. No herbivore or live-bait feeder exists, and no feeder has a screen.

**Engineering:** backlog F3.3 (old module B20).

### F3.4 Park recipes

**In one line:** The player crafts every fence, power and feeder block of the park in survival from vanilla materials.

**How it plays:** A survival player builds a whole park without creative mode or trades. Posts, spools, generators, pylons and feeders all come from the crafting table. Each feeder has its own recipe. A taller feeder uses the feeder below it as an ingredient, so it costs more. Only the Creative Fence Remover stays creative only. Advancements mark the first paddock, the first feeder fill, the first live-bait hunt and the first Large Generator.

**Sources:** decision (section 11.8, 11.11), fence F5 and F21, plan recipe table, lead 2026-10-12.

| Aspect | Requirement | Source |
| --- | --- | --- |
| Recipes | Each machine, part and upgrade has a recipe from vanilla materials. No part comes only from loot or trade | decision, section 11.8 |
| Recipes | Posts, wire spool and Fence Remover get survival recipes. The original 1.12.2 items came from the creative tab only. This is a lead-approved addition to fence parity | decision, section 11.11 |
| Items | Fence items: low post, high post, Wire Spool, Fence Remover, fence switch. No decorative fences in Beta (D78 gap 2). Power items: Large Generator, pylon head. Feeders: the seven feeders of F3.3 | decision, section 11.11, 11.12, lead 2026-10-12 |
| Items | No item: the voltage sign. An empty-hand click hangs it (D78 gap 1) | lead 2026-10-12 |
| Items | Creative only: Creative Fence Remover | fence F21 |
| Numbers | Spool use per span: ceil(distance ÷ 20 × post height). A pylon cable costs ceil(length ÷ 20) spools | fence F5, lead 2026-10-13 (D241) |
| Recipes | Ingredients: the lead approved Claude's draft with changes (D78, D202). The backlog section "Recipes and progression gates" holds every grid. One recipe per feeder | lead 2026-10-13 |
| Recipes | The Large Carnivore Feeder uses the Small Carnivore Feeder. Each taller herbivore feeder uses the feeder below it. So a bigger feeder always costs more | lead 2026-10-13 |
| Recipes | Copper is the wiring metal. The Wire Spool uses iron and copper and gives 4 per craft. The Power Conduit gives 16 per craft | lead 2026-10-13 |
| Items | New ids: `small_carnivore_feeder` and `large_carnivore_feeder` replace `carnivore_feeder_one` and `carnivore_feeder_two` | lead 2026-10-13 |
| Effects | Advancements: "First paddock" (a closed, powered fence loop around a dinosaur), first feeder fill, first live-bait hunt, first Large Generator | lead 2026-10-12 (D203) |

**Art needed:** see Art list → Fences, Power, Feeders (item sprites).

**Open questions:**

- Settled by the lead (2026-10-13): the park recipe draft, with the lead's changes (D202).
- Settled by the lead (2026-10-12, follow-up pass): Beta has no decorative fences (D78 gap 2).
- Settled by the lead (2026-10-12): no voltage sign item (D78 gap 1), one recipe per feeder (D202), and the four park advancements (D203).

**Today:** No park item has a recipe.

**Engineering:** backlog F3.4 (old module B13, park items).

---

## M4. World and vehicles

M4 fills the world beyond the park and the roads inside it.

Digsites, paleontologists and Paleo-Illager camps show the player where to dig and give fossils, maps and trades. A driven Jeep Wrangler and a self-driving Ford Explorer tour carry players through the park. Guns are in Beta, but the lead has not picked the kinds, so that feature is blocked.

The milestone ends with a playtest: find a digsite by map, clear an illager site and run a tour loop.

### F4.1 Digsites, paleontologists, Paleo-Illager sites, maps

**In one line:** Digsites with fossil deposits, paleontologist traders and loot, illager-held sites to clear, and maps that lead to them.

**How it plays:** The player finds a digsite: tents around a large exposed fossil deposit, with chests, paleontologist villagers and an iron golem. A paleontologist trades fossils, amber, special pieces, maps, tools and parts for emeralds. Any lab machine is its workstation, so a paleontologist can also work in the player's lab. Paleontologists give iron golems fossil pieces, and a golem drops its piece when it dies. A map leads to a digsite within 1,000 blocks and can name its species. Paleontologists sell most maps. Cartographers and wandering traders also sell them, at a higher price. Some sites belong to Paleo-Illagers. Pillagers and vindicators guard them and attack on sight. Clearing them gives the site's fossils, chests and machines.

**Sources:** decision (section 11.3), digsite and paleontologist design cards (programming board), Paleo-Illagers.txt, 2020 board (dig site, paleontologist lab), asset repo (`misc/paleontologist`), lead 2026-10-12 (M4 pass).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | A digsite has a large fossil deposit, paleontologist traders and loot chests | decision, section 11.3 |
| Behaviour | Sites range from abandoned and empty to large excavations with machines. Larger sites are rarer and hold more and bigger veins | design cards |
| Behaviour | Exposed veins show the fossils of the biome's formation. No broken or half-buried structures | design cards |
| Behaviour | Parts: sleeping tents, machine tents, quarries, trailer houses, storage boxes, exposed bones like the Velociraptor skeleton of the first film | 2020 board |
| Behaviour | A builder of the team makes new digsite structures. The 2020 builds that the board marks as finished are not used | lead 2026-10-12 (D213) |
| Behaviour | A friendly digsite camp has an iron golem. A Paleo-Illager site has no golem | design cards, lead 2026-10-12 (D212) |
| Behaviour | Paleontologist: a new villager profession. Any lab machine is its workstation: the Fossil Processor, the Sequencer, the Egg Printer or the Incubator. No new workstation block. It lives at digsites and, rarely, in villages | decision, section 11.3, design cards, lead 2026-10-12 (D209) |
| Behaviour | Trades for emeralds: maps, fossils, amber and special pieces (bought and sold), tools, chips, the Hard Drive, the Gold Filter and lab machine parts. The trade table is below | decision, section 11.3, design cards, lead 2026-10-13 (D215) |
| Behaviour | Paleontologists give iron golems fossil pieces, a joke from the 2020 board. About once per in-game day, a paleontologist near a golem gives it one fragmented fossil of a local species. The golem holds it like a poppy and drops it when it dies | 2020 board, lead 2026-10-12 (D214), lead 2026-10-13 |
| Behaviour | A paleontologist lab house appears in villages, in Default, Spruce and Desert styles. The team builder makes it | 2020 board, lead 2026-10-13 |
| Behaviour | Some digsites are Paleo-Illager sites. Illagers guard them and attack. The guards are a mix of pillagers and vindicators. A site is small and poor or large and rich. Clearing it gives fossils, chests and machines | decision, section 11.3, lead 2026-10-12 (D211) |
| Behaviour | Not in Beta: the item that keeps illagers neutral, and "ancient plight" | decision, section 11.3 |
| Behaviour | A map leads to a digsite and can name a species. Paleontologists are the main sellers. Cartographers and wandering traders also sell digsite maps and species maps, at a higher price | decision, section 11.3, design cards, lead 2026-10-12 (D210, asker's reading), lead 2026-10-13 |
| Numbers | A map finds a digsite within 1,000 blocks. Spacing 42 chunks, separation 28 (plan start values) | lead 2026-10-13 (D33), plan |
| Numbers | Hero of the Village gift of a paleontologist: 1 to 3 empty test tubes or 1 empty syringe | lead 2026-10-13 |
| Numbers | Trades per level: 2, picked at random from the list of the level, as for vanilla villagers. The lab house spawns at the normal rate of a village house | lead 2026-10-13 (confirmed in Z-8) |
| Screen | The vanilla trade screen | none |
| Models | Digsite pieces, the lab house and the Paleo-Illager camp, built by the team builder. No workstation model: the lab machines serve | decision, section 11.3, 2020 board, lead 2026-10-12 |
| Skins | Paleontologist villager: the texture exists in the asset repo (`misc/paleontologist`). Paleo-Illager: a skin of its own on the vindicator model, drawn by the artist | decision, section 11.3, lead 2026-10-12 (D211) |
| Items | Digsite map | decision, section 11.3 |
| Sound | Villager work sound at a lab machine: none named. Each machine keeps its own sounds (F1.3 to F1.8) | none |
| Failure | The paleobotanist is not in Beta. It ships in Release 1 with the plants | decision, section 11.13, lead 2026-10-12 (D10) |

**Paleontologist trades (lead 2026-10-13, D215).** Start values that playtests tune. Maps cost the vanilla map price. Other goods cost the draft price plus 25 %, rounded up. A fossil that a paleontologist sells is of a local species: a species whose fossils generate near the villager. A lab machine part trade rolls its part when the trade appears.

| Level | Trades (the player gives → the player gets) |
| --- | --- |
| Novice | 6 fragmented fossils → 1 emerald. 1 emerald → 3 empty test tubes. 2 emeralds → 3 empty syringes. 5 emeralds → 1 fossil pouch |
| Apprentice | 4 poor fossils → 1 emerald. 14 emeralds and a compass → 1 Digsite Map |
| Journeyman | 2 common fossils → 1 emerald. 1 amber → 2 emeralds. 5 emeralds → 1 common fossil. 4 emeralds → 1 Iron Filter. 5 emeralds → 1 Iron Computer Chip. 8 emeralds → 1 Hard Drive. 3 emeralds → 1 lab machine part, or 5 emeralds for a precision part |
| Expert | 1 pristine fossil → 2 emeralds. 21 emeralds and a compass → 1 Species Digsite Map |
| Master | 13 emeralds → 1 pristine fossil. 30 emeralds → 1 pristine special piece. 1 special piece → 5 emeralds. 13 emeralds → 1 amber. 8 emeralds → 1 Gold Computer Chip. 16 emeralds → 1 Diamond Computer Chip. 8 emeralds → 1 Gold Filter |

- Other sellers: the Master list of a cartographer gets one map trade. It sells a Digsite Map for 28 emeralds and a compass, or a Species Digsite Map for 42 emeralds and a compass. 4 uses.
- A wandering trader can offer a Digsite Map for 35 emeralds or a Species Digsite Map for 53 emeralds, from its rare list.
- In the game: a player with emeralds and no gold can buy the precision parts. So the player can skip the gold gate of the first lab.

**Art needed:** see Art list → Digsites and villagers.

**Open questions:**

- Settled by the lead (2026-10-13, Z-8): 2 random trades per level and the normal house spawn rate. The Identification Tag trade left with the tag, so the Journeyman list keeps 7 trades.
- Settled by the lead (2026-10-13): the trade table (D215), a map distance of 1,000 blocks (D33) and the golem drop. Also the Hero of the Village gift, species maps from other sellers, and the lab house by the team builder.
- Settled by the lead (2026-10-12, M4 pass):
  - workstation: any lab machine (D209)
  - map sellers: paleontologists first, also cartographers and wandering traders (D210)
  - Paleo-Illager skin and guards (D211), the iron golem at friendly camps (D212)
  - a team builder makes the structures (D213), and golems get fossil pieces (D214)
  - the paleobotanist ships in Release 1 (D10).

**Today:** Nothing exists. Digsites, the profession, illager sites and maps have no code (section 11.15).

**Engineering:** backlog F4.1 (old module B21).

### F4.2 Vehicles: Jeep Wrangler, Ford Explorer tours

**In one line:** The player drives a Jeep Wrangler, and a 1992 Ford Explorer runs park tours on a track that the player lays.

**How it plays:** The player builds a vehicle part by part, like a lab machine. The player puts down a chassis, adds 4 wheels one by one, then an engine, a body and seats. Each part shows on the model as it goes on. The body decides the vehicle: a Jeep body makes a Jeep, and an Explorer body makes a Ford Explorer, which also needs a Tour Computer. With the last part on, the vehicle is ready to drive. The player drives the Jeep around the park. Dye on a vehicle tints its colours, and the vehicle uses up the dye. A Wrench changes its variation: the Jeep has 3 and the Explorer 4. The wheels turn with the speed, the front wheels steer, and the doors swing open when a player gets in or out. A vehicle needs no fuel. At low speed a vehicle pushes mobs and players aside. At high speed it hurts what it hits. For a tour, the player lays a visible road-marker track and sets stops. The Ford Explorer follows the track and stops at each stop. It plays a chime, and its riders see "Stop 2 of 4" and the name that the player gave the stop. It stops and honks for anything on the track. It seats 4, and players can get in at any time, also while it moves. Off the track, the player drives the Explorer by hand.

**Sources:** decision (section 11.12), design doc (vehicle list), 2020 board (Jeep and Ford Explorer cards), asset repo (vehicle `.bbmodel` files), lead 2026-10-12 (M4 pass, tour diagram), lead 2026-10-13 (Z-8).

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | Two vehicles in Beta: the Jeep Wrangler (driven) and the 1992 Ford Explorer (tour). Gyrosphere, monorail, aircraft and boats are not in Beta. The asset repo also holds helicopters, an FMTV truck and a truck trailer. They are not in Beta | decision, section 11.12, 11.13, lead 2026-10-12 |
| Behaviour | The player assembles each vehicle from parts, like a lab machine. Order: chassis, 4 wheels placed one by one, engine, body, seats. The Explorer adds a Tour Computer last. This replaces "one Jeep item and one Explorer item" | lead 2026-10-13 (Z-8, D250) |
| Behaviour | Every part except the body serves both vehicles: chassis, wheel, engine, seats. The Jeep body and the Explorer body set the vehicle. Only the Explorer takes a Tour Computer | lead 2026-10-13 (Z-8, D250) |
| Behaviour | Each placed part shows on the model. The finished vehicle becomes the driveable vehicle | lead 2026-10-13 (Z-8) |
| Behaviour | Every variation ships. A Wrench used on an assembled vehicle moves it to its next variation. Jeep: 1 roll bar, 2 soft top, 3 open. Explorer: 1v roof rack, 2v, 3v, 4v | lead 2026-10-12, lead 2026-10-13 (D204) |
| Behaviour | Dye on a vehicle tints its colours. The vehicle uses up the dye | lead 2026-10-13 (D204) |
| Behaviour | No fuel | lead 2026-10-12 (D07) |
| Behaviour | Vehicle damage, wrecks and repair are not in Beta. The lead tabled them for a later release. The hit rules below stay | lead 2026-10-13 (D206) |
| Behaviour | Below a speed threshold, a vehicle pushes mobs and players aside with no damage. Above it, a vehicle hurts what it hits, more at a higher speed. The threshold is a config value | lead 2026-10-12 (D75, asker's reading: the lead picked both options) |
| Behaviour | A vehicle that meets an electric fence stops like at a wall. A live fence shocks the driver once | lead 2026-10-12 (D26) |
| Behaviour | The Explorer follows a track that the player puts in the road, stops at each stop, can tour with no driver, and drives by hand off the track | decision, section 11.12 |
| Behaviour | The track is a visible road-marker block | lead 2026-10-12 (D207) |
| Behaviour | The Explorer stops at stop markers. It also stops and honks for anything on the track | lead 2026-10-12 (D25) |
| Behaviour | At a stop the Explorer plays a chime. Its riders see "Stop 2 of 4" and the stop's name. The player names each stop marker in a name field like the anvil's | lead 2026-10-12 (D208) |
| Behaviour | The redstone lamp of a stop marker lights while the Explorer stands at the stop | lead 2026-10-13 (Z-8) |
| Behaviour | Players can board at any time, also while the Explorer moves | lead 2026-10-12 (D25) |
| Behaviour | No Tour Station in Beta. The player starts each tour by hand | lead 2026-10-13 (D25) |
| Numbers | Seats: Explorer 4 (lead, D25), Jeep 2 (plan default D25) | lead 2026-10-12, plan |
| Numbers | Track speed 0.25 blocks per tick (estimate). Stop time, top speeds and the hit speed threshold: not set (config, task plan) | plan |
| Animation | Code drives all motion. The wheels spin with the speed, and the front wheels steer. The doors swing open when a player gets in or out, where the model has door parts. No keyframed clips | lead 2026-10-12 (D205) |
| Sound | Engine idle and drive, horn, door, crash, stop chime | proposal (lead decides), lead 2026-10-12 (chime) |
| Models | Jeep: 3 variations (2.12 × 2.03 × 4.21 blocks), each with and without wheels. Explorer: 4 variations (about 3.32 × 2.7 to 3.1 × 6.13 blocks), with and without wheels, plus `Wheels.bbmodel`. The "without wheels" models and the wheels are the basis of the assembly stages. The REPO models have no chassis, engine or Tour Computer group of their own (Art list question 21). No vehicle has clips. Track block, stop marker | asset repo, lead 2026-10-12, lead 2026-10-13 (Z-8) |
| Items | Vehicle parts: chassis, wheel, engine, seats, Jeep body, Explorer body, Tour Computer. Tools and track: Wrench, tour track, tour stop marker | decision, section 11.12, lead 2026-10-13 (Z-8) |
| Recipes | Wrench: 3 iron on a diagonal and 1 copper. Chassis: a minecart and 4 iron. Wheel: a slime block and 1 iron, 4 wheels per craft. Engine: a piston, a furnace, 4 iron and redstone. Seats: 5 leather. Jeep body: about 10 iron and 1 glass pane. Explorer body: about 20 iron, with a block of iron, and 3 glass panes. Tour Computer: a comparator, 2 redstone and 1 gold. Tour track: 6 iron, 1 redstone and 2 smooth stone, 16 per craft, like a powered rail. Stop marker: a tour track and a redstone lamp. Grids: backlog "Recipes and progression gates" | lead 2026-10-13 (Z-8, D250) |
| Failure | A broken track loop stops the tour start and shows where the gap is | plan |

**Art needed:** see Art list → Vehicles.

**Open questions:**

- Settled by the lead (2026-10-13, Z-8): the player assembles vehicles from parts like the lab machines (D250). The lead also set the recipes of the parts, the Wrench, the track and the stop marker. Every part except the body serves both vehicles. The Wrench changes the variation of an assembled vehicle. Dye tints the colours. Vehicle damage stays out of Beta.
- Settled by the lead (2026-10-13): no Tour Station in Beta (D25). Dye tints the colours, and the vehicle uses up the dye. A new Wrench item changes the variation (D204). Vehicle damage, wrecks and repair wait for a later release (D206).
- Settled by the lead (2026-10-12, M4 pass):
  - variations (D204, by Wrench since 2026-10-13), code motion (D205), no fuel (D07)
  - slow push and fast hurt (D75). Damage and iron repair (D206) wait for a later release since 2026-10-13
  - the road-marker track (D207), the chime with "Stop 2 of 4" and a stop name (D208)
  - Explorer stops, 4 seats and boarding at any time (D25).

**Today:** Nothing exists. Vehicles and the tour track have no code (section 11.15).

**Engineering:** backlog F4.2 (old module B22).

### F4.3 Guns (blocked on decision D08)

**In one line:** Some guns are in Beta. The lead has not picked the kinds, so this feature is blocked.

**How it plays:** Not decided. The design doc names three kinds: tranquilizer, stun and capture, and lethal. Do not start until the lead decides D08.

**Sources:** decision (section 11.12, 11.14), Guns List, design doc.

| Aspect | Requirement | Source |
| --- | --- | --- |
| Behaviour | Not decided | decision, section 11.14 |
| Items | The Guns List names 15 finished guns. The lead picks which ship (D08) | Guns List |
| Models | One model for each gun that ships | Guns List |
| Items | Ammo for each gun kind that ships, for example a dart, a taser cartridge or a shotgun shell | proposal, lead decides |
| Animation | A tranquilised clip per species, if tranquilizers ship | species card |
| Sound | Shot and reload for each gun that ships. Dart and taser hits | proposal, lead decides |

**Art needed:** see Art list → Guns (blocked, list only), each species (Animations: tranquilised).

**Open questions:**

- D08: which kinds, and which models ship?
- Tranquilizer: does every species need a tranquilised clip?
- Capture: what happens to a captured dinosaur?

**Today:** Nothing exists.

**Engineering:** backlog F4.3 (old module B23).
