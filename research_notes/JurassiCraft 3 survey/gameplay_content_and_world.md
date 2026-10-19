# JurassiCraft 3.0.14 (NeoForge 26.1.2): gameplay loop, content and world

Scope: what a player sees and does in JurassiCraft 3 from the first fossil to a running park. This note covers world generation (fossils, amber, minerals, plants), the digsite and other structures, villagers and trades, the dart gun, park building blocks (fences, glass, concrete, signs, murals), plants, info screens, commands, config options and the full content inventory. It maps each technique to Project Nublar modules B01, B02, B03, B04, B13, B21, B23, B24 and the port (P2). Machines, genetics, species, AI and vehicles belong to the sibling notes in this folder. This note repeats them only where the progression needs a step.

Labels: **[v]** verified in the decompiled source or data files at the path and lines given. **[inf]** my inference. **[doc]** text from the mod's own lang file or JEI info pages (shipped in the jar). Numbers marked [v] come from data JSON or constants. Rows I computed by hand from [v] constants carry [inf].

Clean-room rules followed: no code is copied. Only behaviour, state, steps, numbers and timings are described. Class names in the jar are obfuscated and reused across packages. I give each class a descriptive name I chose, then the path. All source paths are relative to `scratchpad\jc\src\caz\jurassicraft\`. All data paths are relative to `scratchpad\jc\raw\data\jurassicraft\` unless they start with `assets`. Excluded: the packages in the skip list, the `raw/jurassicraft/` directory, the META-INF licence files, and the package `wetxj` (not described). In `Jurassicraft.java` I read only the registration lines. One further command class sits in an excluded package and is not described.

---

## 1. What was read

| Area | Package or path | What it holds | Size read |
| --- | --- | --- | --- |
| Item classes | `bugratnc/` | dart gun, darts, wands, plaster, syringe tools, bone items, signs, murals | 25 files, 1,637 lines |
| Registries | `wkjvl/` | items, blocks, machines, recipes, sounds, tags, professions, data components | 21 files, 2,157 lines |
| Block classes | `yknu/` | fossil blocks, peat, moss, glass, plants, display blocks | 29 files, 2,130 lines |
| Worldgen features | `jdbttmo/` | tree growers, fossil vein, nest site, trackway, petrified log | 12 files, 703 lines |
| Worldgen bootstrap, loot and biome modifiers | `axvg/` | placed features, structure sets, chest, fishing and bug loot, natural spawns | 15 files, 809 lines |
| Structures | `uzuj/` | fossil digsite, template structure, template piece | 8 files, 778 lines |
| Placement helpers | `kswtc/` | config toggle filter, paddock rarity placement | 5 files, 172 lines |
| Loot functions | `umnlo/` | random species, bone, plant, DNA | 6 files, 188 lines |
| Hanging entities and dart | `ysulhvcy/` | attraction sign, paddock sign, mural, dart projectile | 8 files, 764 lines |
| Fences | `qkek/` | low and medium security fence base, pole, wire, power rules | 6 files, 985 lines |
| Species and period data | `ysjrlvxg/` | species record, geologic periods, fossil depth selector | 12 files, 1,248 lines |
| Recipes | `vtrqoo/` (except `gjtxsudo.java`, skipped), `smsbgw/` | lab recipe type, grinding, cleaning, potion dart, species smelting | `vtrqoo/jgdp.java` head and `smsbgw/dnukfuguy.java`; the rest from recipe JSON |
| Plants | `cnxjsqwpw/` | plant data record, plant item helpers | 4 files, 179 lines |
| Screens and HUD | `wgpfjieca/`, `onzkovhp/`, `dbgouk/`, `ummp/` | PaleoPad, paddock sign picker, order screen, dart ammo HUD, GUI widgets | about 2,000 lines, read in part |
| Plugins | `cvrd/` | JEI recipe categories, Jade tooltips | 11 files, scanned |
| Trade keys | `bjmjifa/` | trade and trade-set keys | 3 files, 206 lines |
| Root config | `dnukfuguy.java` (root) | all config options | 141 lines |
| Data | `raw\data\jurassicraft\` | 1,874 JSON files, 6 NBT files | inventory, all structure/worldgen/trade/loot/recipe files in this note |
| Assets | `raw\assets\jurassicraft\lang\en_us.json` | 2,024 lang keys | read in full |
| Skeleton and creature hooks | `ufpurxw/lyhmwcjw.java`, `dnukfuguy/omogwgxr.java`, `ctygd/omogwgxr.java` (parts) | skeleton table matcher, carcass and sedation hooks | parts |

The class `ysjrlvxg/omogwgxr.java` has a static initialiser that the decompiler could not rebuild. I read it with `javap -c -p`. [v]

Count note: the task brief said about 10,000 data JSON files. The jar holds 1,874 under `data/` and 5,320 model JSON files under `assets/`. [v]

---

## What to take from this note (top findings)

1. **Fossils follow a geologic column, not biomes.** The 125 world layers from Y 64 down to Y -60 split into 8 period bands. Each band is as thick as the period is long, so Cretaceous (62 of 129 species) gets Y 14 to 40. A species is picked per 8 by 8 by 8 cell, so one cell holds bones of one animal type. The fossil block has no block entity. [v] (section 2)
2. **A bare fossil shatters.** Breaking a fossil block drops nothing. The player must "jacket" it first with Plaster and Bandage (gypsum powder, paper, wool), then mine the encased block and wash it in a Cleaning Station. [v] (section 3)
3. **DNA has a quality number.** Each sample carries a species and a quality of 5 to 100 in steps of 5. The synthesizer takes only quality 100. A combinator adds two samples of one species. [v] (section 4)
4. **Amber is two items.** An amber ore drops a Mosquito (animal DNA) half the time and an Aphid (plant DNA) otherwise. Silk Touch drops the ore. [v] (section 5)
5. **Villagers use the 26.x data-driven trade format.** Trades, trade sets and tags live in `villager_trade/`, `trade_set/` and `tags/villager_trade/`. The profession constructor takes a map of level to trade-set key. The Geneticist sells the DNA machines for 4 to 7 emeralds. The Paleontologist sells random-species bones. Workstations are the Fossil Grinder and the DNA Sequencer. [v] (section 8)
6. **Structures are very rare by design.** Visitor Centre: one per 448-chunk cell (jungle and savanna). Raptor Paddock default one per 4,000 chunks. Digsite and Geneticist House: one per 48-chunk cell in village biomes. A Cartographer (master level) sells a map to the Visitor Centre. [v] (section 9)
7. **The dart gun is simple.** It holds up to 8 darts of one kind, fires one per 15 ticks, and the dart applies its effect on hit. Tranquilizer sedation lasts 650 to 699 ticks. Euthanasia poisons for 200 ticks, then kills. Potion darts come from 8 tranquilizer darts around a lingering potion. [v] (section 10)
8. **Electric fences cost no ticks.** Power is a redstone signal on a pole. A bounded flood fill (radius 8) over base blocks sets a `powered` flag on wire blocks on neighbour updates only. Shock damage is 1 (low) or 2 (medium). [v] (section 11)
9. **No guide book, no custom advancements, no gameplay commands.** Guidance comes from JEI info pages (13), the PaleoPad (right-click an animal), Jade tooltips and item tooltips. The 468 advancements are all recipe unlocks. The only commands are developer tools (`/jcgui`, `/jcmodel`, `/jcmachine`). [v] (sections 13 to 15)
10. **Config is 41 options.** Every worldgen feature and structure has an on/off toggle that works through a custom placement filter. Natural dinosaur spawning is off by default. Loot injection into vanilla chests has a percentage option (default 100). [v] (section 16)

---

## 2. Fossil world generation: the geologic column

### 2.1 Fossil bands by depth

**In game.** The player digs down. Near the surface the bones belong to young animals such as the Alligator Gar. Deeper they belong to dinosaurs, then to Triassic and Permian animals, and at the bottom to Devonian fish. The player does not need a biome. A fossil block looks like stone (or deepslate) with bone. It has 4 texture variants. The variant number comes from a hash of the species name, so one species always shows the same variant. [v]

**Mechanism.**
1. A geologic-period enum lists 13 entries with start and end dates in millions of years: Quaternary, Neogene, Paleogene, Cretaceous, Jurassic, Triassic, Permian, Carboniferous, Devonian, Silurian, Ordovician, Cambrian, None. Eight are "banded": Quaternary to Permian, and Devonian. Carboniferous, Silurian, Ordovician and Cambrian have no band. (geologic period enum, `ysjrlvxg/omogwgxr.java:1-110`) [v]
2. A static initialiser splits the 125 layers (Y 64 down to Y -60) among the banded periods. Each period gets `duration / total duration * 125` layers, rounded down, with a minimum of 1. Leftover layers go to the largest fractional parts, first-come on ties. The youngest period takes the top. (`ysjrlvxg/omogwgxr.java`, static initialiser, read with javap) [v]
3. My hand calculation of the result gives these bands [inf]:

| Period | Y range | Layers | Species with that period in data |
| --- | --- | --- | --- |
| Quaternary | 63 to 64 | 2 | 1 (Alligator Gar) |
| Neogene | 56 to 62 | 7 | 7 (Dodo, Moa, Kelenken, Titanis, Megapiranha, Rhizodus, Xiphactinus) |
| Paleogene | 41 to 55 | 15 | 3 (Arsinoitherium, Leptictidium, Titanoboa) |
| Cretaceous | 14 to 40 | 27 | 62 |
| Jurassic | -5 to 13 | 19 | 29 |
| Triassic | -23 to -6 | 18 | 12 |
| Permian | -39 to -24 | 16 | 3 (Inostrancevia, Lycaenops, Lystrosaurus) |
| Devonian | -60 to -40 | 21 | 4 (Cladoselache, Coelacanth, Dunkleosteus, Eusthenopteron) |

4. The 8 hybrid species (Indominus, Indoraptor, Malusaurus, Paradeinonychus, Spinoraptor, Stegospinus, Tyrannops, Ultimasaurus) have period `none`. A species with `hybrid_of` is excluded from all fossil picks. (`ysjrlvxg/dbgouk.java:44-46` returns "is hybrid"; `data/jurassicraft/jurassicraft/species/*.json`) [v]
5. A start-up check logs a warning when a species belongs to a period with no band ("none of them can be found as a fossil at any depth"). (`ysjrlvxg/kijk.java`, tags-updated hook) [v]
6. The fossil species for a position is chosen like this. Find the band for the Y value. Seed a random source from the world seed and the position divided by 8 on all three axes (an 8 by 8 by 8 cell). Pick one species from the cached list of non-hybrid species whose period matches the band. If none exists at that exact Y, try the other 7 layers of the cell. Last, pick any non-hybrid species. (species-at-position selector `ysjrlvxg/gjtxsudo.java:1-56`; cached list `ysjrlvxg/onzkovhp.java:1-81`, a synchronized cache keyed by registry) [v]
7. The fossil block variant is `hash(species path) mod 4`. (`ysjrlvxg/gjtxsudo.java`, last two methods) [v]

**Platform overlap.** All of this uses vanilla `Feature`, `RuleTest` and registries. Nothing uses a loader-only API. The synchronized `WeakHashMap` cache is a pattern the Nublar plan forbids inside feature configs (B01: no lazy cache in a feature config). [inf]

**Verdict.** adapt (B01). The banding idea removes per-species depth tuning and avoids block-entity or per-species blocks. Nublar still needs biome groups and quality, so it keeps its own data. Use the largest-remainder split to give a default depth for any species that lists only a `period`.

### 2.2 Fossil ore features and rarity

**In game.** Fossils are common underground, like a vanilla ore. Plant fossils sit next to coal. Nest fossils appear in small flat patches of gravel and terracotta. Trackways are buried lines of footprints. Petrified logs are stone-embedded trunks.

**Mechanism.** All placed features are registered in the Bootstrap class (`axvg/dbgouk.java:1-102`) and in JSON (`worldgen/placed_feature/`, `worldgen/configured_feature/`). Each placed feature starts with a custom config-toggle filter (section 16). [v]

| Feature | Attempts per chunk | Size | Y range | Host and notes |
| --- | --- | --- | --- | --- |
| Fossil vein (`ore_fossil`) | 44 | up to 5 blocks, random walk in a 3 by 3 by 3 box, 15 tries | -60 to 64 | `base_stone_overworld` tag; sets rock to stone or deepslate; species from section 2.1 |
| Plant fossil (`ore_plant_fossil`) | 10 | 4 | -60 to 192 | replaces coal ore and deepslate coal ore first, then stone and deepslate; 50% discard when touching air |
| Amber (`ore_amber`) | 8 | 3 | 0 to 19 | stone and deepslate ore replaceables; all overworld biomes |
| Ice Shard (`ore_ice_shard`) | 8 | 3 | 0 to 63 | stone and deepslate ore replaceables |
| Gypsum (`ore_gypsum`) | 32 | 10 | -64 to 80 | stone and deepslate ore replaceables |
| Phaneritic stone (`ore_phaneritic`) | 12 | 16 | 0 to 97 | stone and deepslate ore replaceables |
| Petrified log | 1 | straight line of 3 to 9 | 0 to 50 | stone ore replaceables; one of 5 tree kinds |
| Nest fossil site | 1 per 100 chunks | flat square of 6 to 8 | 30 to 50 | `base_stone_overworld`; 80% of cells filled; 10% of filled cells are trackway, the rest sediment (gravel 6, white terracotta 2, light gray terracotta 2); then 1 to 2 nest fossils at random cells |
| Buried trackway | 1 per 20 chunks | 1 to 2 lines of 3 to 4 blocks, spread 10 | 30 to 50 | replaces plain `stone` only; 3 kinds (medium biped, small biped, raptor); random facing |

(fossil vein `jdbttmo/gjtxsudo.java:1-62`; nest site `jdbttmo/jgdp.java:1-91`; trackway `jdbttmo/vtclb.java:1-84`; petrified log `jdbttmo/dbgouk.java:1-66`; data files as listed) [v]

The ore, buried-decor and surface features join biomes through three biome modifiers: `add_ores` (6 ores into `is_overworld`, step underground ores), `add_buried_decor` (petrified log, nest site, trackway into `is_overworld`, step underground decoration). (`neoforge/biome_modifier/`, `axvg/vtclb.java:1-41`) [v]

**Platform overlap.** NeoForge biome modifiers. Fabric needs `BiomeModifications` calls. The plan already says both loaders place features (B01). [doc, plan]

**Verdict.** adapt (B01). Take the "one flat patch with sediment, nest and trackway" nest-site idea and the buried trackway as cheap decorative terrain. Keep Nublar's four forms.

### 2.3 Surface and water worldgen (plants, peat, moss)

| Feature | Rate | Biomes | Notes |
| --- | --- | --- | --- |
| Peat blob (common) | 0.5 per chunk (weighted 1:1 for 1 or 0) | swamp, mangrove swamp | vein of 5 that replaces dirt, grass block, coarse dirt at the surface; `moisture` 7 |
| Peat blob (uncommon) | 0.4 per chunk | all taiga biomes | same |
| Peat blob (rare) | 0.3 per chunk | all jungle biomes, dark forest, pale garden | same |
| Moss carpet | 1 in 8 chunks, 7 tries, spread +/-2 | taiga, jungle, forest, birch forest, old growth birch, flower forest, dark forest, pale garden, swamp, mangrove | needs a solid block below |
| West Indian lilac | 1 in 8 chunks | jungle, swamp, mangrove | double plant, bears poison berries |
| Heliconia | 1 in 8 chunks | jungle | no config toggle [v] |
| Gracilaria (seaweed) | 1 in 4 chunks, 24 tries, spread +/-4 | ocean | waterlogged, on the ocean floor |

(`worldgen/placed_feature/*.json`, `neoforge/biome_modifier/*.json`, `tags/worldgen/biome/*.json`) [v]

No tree is generated in the world. The five ancient trees (Araucaria, Calamites, Ginkgo, Phoenix, Psaronius) exist as configured features only for saplings. Trees come from growing saplings. (`axvg/omogwgxr.java`, tree growers; no tree in any biome modifier) [v]

**In game, peat.** Peat is a dirt-like block with a `moisture` level 0 to 7. A random tick sets it to 7 when rain falls on it or water lies within 4 blocks horizontally (box 9 by 2 by 9 starting at its own layer). Otherwise it loses 1 level per tick and reverts to dirt at 0, taking Peat Moss with it. A peat block in a "has peat" biome then tries 4 random spots within +/- the `peatGenerationSpeed` range (default 20): a dirt block with grass within 3 blocks above turns into peat. (peat block `yknu/lyhmwcjw.java:1-92`) [v]

**In game, moss.** Moss is a carpet that only stays on full blocks. A random tick, if the block below is Peat, rolls 4 in 8 to try one random spot within 3 blocks sideways (down to 3 below, up 8) and places a moss if empty and fewer than 8 moss already stand in a 5 by 7 by 5 box. (moss block `yknu/stta.java:1-76`) [v]

**Verdict.** defer. Peat and moss feed the incubator (section 4). Nublar's B11 decides the incubator inputs. If B11 wants a swamp-gated input, the moisture rule is cheap (random ticks only, no scans).

---

## 3. Digging up a fossil: jacket, mine, wash

**In game.**
1. The player right-clicks a fossil block with Plaster and Bandage. The block turns into an Encased Fossil Block. Plant fossil and nest fossil blocks turn into their encased forms.
2. The player mines the encased block (stone pickaxe needed) and carries it away whole.
3. If the player breaks a bare fossil, nothing drops and the action bar says "The fossil is too brittle, I should wrap this in plaster". [v]
4. A Cleaning Station washes the block. An encased fossil gives 1 bone of that animal. An encased nest gives fossilized eggs. An encased plant fossil gives plant fossil, twig fossil, coal, flint or cobblestone. [v]

**Mechanism.**
- Plaster and Bandage (`bugratnc/lyhmwcjw.java:1-66`) holds a map of block to block: `fossil_block` to `encased_fossil_block`, `plant_fossil_block` to `encased_plant_fossil_block`, 3 nest blocks to 3 encased nests. If the target has a block entity, the item reads the species for that position (section 2.1) and writes it into the encased block. It uses 1 item and plays a mud sound. (map built in `wkjvl/kdhoqn.java`, method near the end) [v]
- The encased fossil block entity stores one species key. Its loot table copies the `species` component to the dropped item. (`dnukfuguy/gjtxsudo.java:1-17`, `loot_table/blocks/encased_fossil_block.json`) [v]
- The bare `fossil_block` and `plant_fossil_block` loot tables have no pool: nothing drops. (`loot_table/blocks/fossil_block.json`) [v]
- Cleaning Station (`dnukfuguy/vtclb.java:1-214`): one Water Bucket loads 1,600 ticks (80 seconds) of washing and leaves an empty bucket. For an encased fossil it picks one bone type at random from the species' `remains.bones` list. For an encased nest it sets a stack of 1 to 7 fossilized eggs (nest recipe: egg stack weight 2, flint 1, bone meal 1). For an encased plant fossil it rolls plant fossil 8, twig fossil 6, coal 4, flint 3, cobblestone 3 (out of 24). (`recipe/cleaning/*.json`) [v]
- The jacketed block is the only way to get a species-tagged bone from a fossil. The JEI text says the same: "an encased fossil gives one of that animal's bones". [doc]

**Platform overlap.** Uses data components (`species`, `fresh`), `copy_components` loot function and `block_entity` source. Vanilla only. [v]

**Verdict.** defer the jacket step as a design question for the lead. Nublar B02 plans Fortune and Silk Touch instead. The jacket step adds one item and one machine step but teaches the player what to do (a friendly message on a failed break).

---

## 4. The progression loop: first fossil to first animal

This is the full chain a survival player follows. Machine internals are in the genetics note.

| Step | Player does | Item or machine | Recipe or rule |
| --- | --- | --- | --- |
| 1 | Mine gypsum ore | Gypsum Stone, gives 4 to 8 Gypsum Powder (Fortune adds) | `loot_table/blocks/gypsum_stone.json` |
| 2 | Craft plaster | Plaster and Bandage x9 | 4 paper, 4 gypsum powder, 1 wool (`recipe/plaster_and_bandage.json`) |
| 3 | Find and jacket fossil | Fossil Block to Encased Fossil Block | section 3 |
| 4 | Wash | Cleaning Station | 3 iron ingots, 2 redstone, glass pane, bucket, iron block (`recipe/cleaning_station.json`) |
| 5 | Grind bones | Fossil Grinder | blades, iron rod, piston, 2 lapis, 2 redstone, 2 iron ingots (`recipe/fossil_grinder.json`). A bone gives bone meal 3, flint 2, Soft Tissue 1 (of 6). Soft tissue carries the bone's species. Fossilized eggs give soft tissue of a random species 1 in 3. [v] |
| 6 | Sequence | DNA Sequencer + Storage Disc + Soft Tissue | 500 ticks. Result: disc with one DNA sample (species, quality 5 to 100 in steps of 5, a genetic code string). |
| 7 | Raise quality | DNA Combinator (2 discs of one species) | 750 ticks. Quality adds up, capped at 100 (`etemhnv/gjtxsudo.java` merge method). |
| 8 | Synthesize | DNA Synthesizer: disc (not consumed) + Empty Test Tube + DNA Nucleotides | 750 ticks. Gives a DNA tube only when the sample is at quality 100. Each disc holds 3 syntheses ("Syntheses left: n of 3"). |
| 9 | Make an embryo | Embryonic Machine: DNA + Petri Dish + Empty Syringe | gives a Syringe. |
| 10a | Egg layers | Embryo Calcification Machine: Syringe + a vanilla egg | gives a Dinosaur Egg. |
| 11a | Incubate | Incubator: Dinosaur Egg (kept) + Peat Moss | 3,600 ticks (3 minutes). Gives a Hatched Egg. |
| 10b | Live-bearing species | Cultivator: Syringe (kept) | 2,000 ticks. Needs water and four nutrient levels (lipids, minerals, proximates, vitamins) fed from food items through a data map. Gives a "Gestated X". |
| 12 | Hatch | right-click a block with the hatched egg or gestated item | places a baby. Not sneaking: "X has imprinted on you", the creature is yours. |
| 13 | Grow | wait, or Growth Serum | Growth Serum: golden carrot + Empty Syringe + water bucket + a cooked meat. Brings the animal to full age in one use. Config can turn it off. |

Source for the table: recipes `recipe/**` (section 7 inventory), lab recipe times in the JSON `processing_time` (ticks), `ufpurxw/fgwme.java`, `dnukfuguy/omogwgxr.java` and the lang text `gui.jurassicraft.wrong_machine.*`. The "ticks" reading of `processing_time` is [inf]; the sibling machine note should confirm it. [v] for item names and ratios.

**Shortcuts that skip steps.**
- Amber. Mosquito or Aphid plus a Storage Disc in a DNA Extractor (500 ticks) gives a disc with no fossil chain. [v]
- Dinosaur Meat. Meat plus a disc in the Extractor gives a DNA sample of that species. Raw meat comes from a carcass (section 6). [v]
- Sea Lamprey. An Ice Shard ore drops one 20% of the time. Lamprey plus a disc in the Extractor gives "marine" DNA. [v] (`loot_table/blocks/ice_shard.json`, `recipe/extraction/sea_lamprey.json`)
- Visitor Centre laboratory chest holds ready-made discs with random DNA and soft tissue (section 9).
- The Geneticist sells the machines for emeralds (section 8).

**Plants branch.**
1. Plant fossil, twig fossil and petrified logs grind into Plant Soft Tissue. A petrified log gives the tree it came from. A plant fossil or twig fossil gives a random plant. [v]
2. Plant Soft Tissue plus a disc in the Sequencer gives a plant DNA disc.
3. Plant DNA comes out of the Synthesizer in the same way.
4. The player right-clicks any living plant or leaf with an Empty Syringe. That gives Plant Cells (the plant is not hurt). Cells plus an Agar Petri Dish give Plant Cells in Petri Dish. [v]
5. Embryonic Machine: Plant DNA + Plant Cells in Petri Dish + Empty Syringe gives a Plant Callus.
6. The player right-clicks the top of Farmland with the callus. The farmland turns to dirt unless the plant is a crop. Prehistoric plants then stand in the world. The message "Maybe I should try planting this on tilled soil" shows if the player clicks plain dirt. [v]
7. Agar. Smelt Gracilaria into Liquid Agar. Liquid Agar plus 4 Petri Dishes gives 4 Agar Dishes. [v]

**Vanilla feel.** The chain is machine-heavy and not vanilla-like. The early game (find, jacket, wash, grind) is a vanilla-style ore loop with one extra tool.

**Verdict.** adapt (B13). The plan already wants a survival path from a fresh world to a hatched animal without trades or creative. The mod lets the player skip with amber, meat or trades, which fits the plan rule that no part is trade-only.

---

## 5. Amber, bugs and other DNA and food sources

**Amber.** Amber ore generates at Y 0 to 19. Mining drops one item. With Silk Touch it drops the ore. Otherwise a 50% roll gives an Amber Mosquito, else an Amber Aphid. Fortune applies the usual ore bonus. The Mosquito yields animal DNA and the Aphid plant DNA, because "the mosquito fed on an animal and the aphid on a plant". (`loot_table/blocks/amber_ore.json`; `jei.jurassicraft.info.amber`) [v][doc]

**Bugs.** There is no craft for the first bug. Breaking Hay Bales, Pumpkins, Melons and Cocoa Pods gives a Cockroach or a Mealworm Beetle (half of the time one drop, picked from the two). Grass Blocks give Crickets 1 in 12, Grass and Ferns 1 in 10. A global loot modifier rolls 50% first. (`axvg/aeygzxj.java:1-66`, `loot_modifiers/bug_loot.json`) [v] A Bug Farming Crate then breeds more of a bug the player already holds. The 31 `bug_breeding` recipes use 1,200 ticks and give 10 of the bug per feed item. (`recipe/bug_breeding/*.json`) [v]

**Plankton and krill.** A Fine Net used on still water has 1 in 20 chance per use to bring up Krill or Plankton and loses 1 durability (50 total). Either item placed on a water source becomes a swarm block that slowly seeds more of itself (random tick, up to 6 in a patch). Filter feeders (Coelacanth, Leedsichthys, Mawsonia) graze the swarms. (`bugratnc/kijk.java:1-50`, `yknu/wtii.java:1-57`, JEI info) [v]

**Fishing.** Any vanilla fishing catch has a 10% chance to add Gracilaria. (`axvg/kodkieb.java:1-33`) [v]

**Verdict.** adopt amber as written (B03): two items, a species-blind drop, species rolled later. Defer bugs and swarms to Release 1 (feeders, B20 and later).

---

## 6. Carcasses, meat, fresh bones, skeletons

**In game.** A dead animal stays as a carcass (a smaller hitbox). Each hit with any weapon cuts meat. When the body is used up, the last hit drops the bones, and each bone has a 90% chance. Fresh bones and fossil bones look alike but carry a `fresh` flag, and a Skeleton Assembly Table builds either a "Fresh" or a "Fossilized" skeleton block. [v]

**Mechanism.**
1. On death the creature sets a `dying` state, restores full health and sets a hit counter: `clamp(max(1, sqrt(width * height) * 2), 1, 10)` (cast to int before the clamp). With `allowCarcass` off the counter is 0 and the first hit finishes it. (`ctygd/omogwgxr.java:837-855`) [v]
2. Each hit by an entity that is not blocked by the damage-immunity window runs the meat drop: `1 + rand(2) + Looting` pieces of Raw X Meat (X Steak if the body burns). Each piece carries a DNA sample with a random quality. After the counter reaches 0 the next hit calls the normal death loot and removes the body. (`ctygd/omogwgxr.java:857-913`) [v]
3. The death loot drops each bone of the species' `remains.bones` list with 9 in 10 chance, tagged `fresh`. (`ctygd/omogwgxr.java:948-962`) [v]
4. The JEI page "bones" says: "clearing the carcass drops one of each of its bones, nine times in ten". [doc]
5. Fresh and fossil bones both grind (bone meal, flint or soft tissue). A Skeleton Assembly Table has a 5 by 5 grid. It finds the smallest box around all bones. It accepts a layout only if every bone is the same species and the same fresh state, and the layout equals the species' `remains.skeleton` grid (up to 5 by 5, validated at load). It outputs a Display Block item flagged `skeleton`. Mixed fresh and fossil bones give nothing. (`ufpurxw/lyhmwcjw.java:1-130`, `dnukfuguy/omogwgxr.java:1-95`, `ysjrlvxg/dbgouk.java:493-560`) [v]
6. Example: the Tyrannosaurus grid is 4 rows by 5 columns using neck vertebrae, skull, tail vertebrae, pelvis, ribcage, shoulder bone, tooth, leg bones twice, arm bones, foot bones twice. The 10 bone kinds are in `remains.bones`. (`species/tyrannosaurus.json`) [v]
7. A Display Block item has three modes: Action Figure (statue), Fresh Skeleton, Fossilized Skeleton. Sneak plus right-click cycles the pose (species `skeleton_poses`, such as idle, death, attacking) or the figure's sex. (`bugratnc/dbgouk.java:1-130`) [v]
8. Skulls of only the Tyrannosaurus and the Triceratops can be placed as a "Skull Display" on any face except down. Sneak plus right-click toggles standing or placed pose. (`bugratnc/aeygzxj.java:1-146`, allowlist `yknu/yknu.java:36`) [v]

**Vanilla feel.** Not vanilla. The multi-hit carcass is the most unusual idea here.

**Verdict.** adopt the carcass loop for B04 and B16 (remains items). The plan's B04 already lists remains from carcasses. Keep the config toggle. Treat the skeleton table as out of scope for Beta (the plan excludes skeleton exhibits from B02).

---

## 7. Content inventory

All counts from `assets` and `data` listings. [v]

| Kind | Count | Notes |
| --- | --- | --- |
| Block states files | 371 | 379 block lang keys (templates such as "%s Cultivator" counted once) |
| Block loot tables | 371 | one per block |
| Item model definitions | 537 | 495 item lang keys |
| Creative tabs | 6 | Blocks, Items, Fossils, Foods, Plants, Vehicles |
| Entity types | 148 | 129 species + 10 boats (5 woods x boat + chest boat) + 3 vehicles + dart + venom + dinosaur egg + 3 hanging entities (paddock sign, attraction sign, mural) |
| Species (data pack registry `jurassicraft:species`) | 129 | 62 Cretaceous, 29 Jurassic, 12 Triassic, 7 Neogene, 4 Devonian, 3 Paleogene, 3 Permian, 1 Quaternary, 8 hybrids; 84 carnivores, 45 herbivores |
| Plants (registry `jurassicraft:plant`) | 49 | 5 trees (`tree: true`), 5 flagged `prehistoric: false`, 39 plain |
| Recipes | 577 + 2 vanilla overrides | 350 shaped, 86 shapeless, 58 grinding, 31 bug breeding, 24 stonecutting, 16 lab, 5 smelting, 5 cleaning, 1 species smelting, 1 potion dart |
| Advancements | 468 | all recipe unlocks (466 mod, 2 vanilla override); no step advancements [v] |
| Loot tables | 371 block + 11 chest (digsite, geneticist house, 9 visitor centre rooms) | 3 global loot modifiers (chest, fishing, bug) + 4 loot functions |
| Structures | 4 types, 4 structure sets, 6 templates | digsite (3 templates), geneticist house, raptor paddock, visitor centre |
| Biome modifiers | 10 | 9 add-features + natural spawns |
| Configured / placed features | 19 / 16 | |
| Tag files | 151 | 18 block + 24 item + 13 biome + 4 trade tags (mod), 28 block + 19 item + 2 (vanilla extensions), 22 block + 21 item (common) |
| Villager professions | 2 | Paleontologist, Geneticist |
| Trades | 47 trade files | geneticist 16, paleontologist 30, cartographer 1 |
| Damage types | 2 | `shock`, `run_over` |
| Jukebox songs | 3 | 237.4, 88.2 and 102.2 seconds |
| Sound events | 558 | 2,491 ogg files |
| Textures / model JSON | 6,393 / 5,320 | |
| Lang keys | 2,024 | subtitles 558, items 495, blocks 379, entity 279, gui 99, config 54, plant 49 |
| Keybinds | 13 | 5 seat keys, helicopter x5, dismount riders, 2 zoom |
| Config options | 41 | 4 client, 1 common, 36 server |
| Mixins | 4 | 2 common, 2 client; all are performance-related (a part-scan and block-intersect change, and 2 vertex writers). |
| Data pack registries added | 2 | species, plant |

### Plain-name content list (from the lang file)

**Blocks (park and world).** Amber Ore, Ice Shard, Gypsum (Stone, Cobblestone, Bricks, each with Slab, Stairs, Wall), Reinforced Stone and Reinforced Bricks (same four forms), Phaneritic Stone and Polished Phaneritic Stone (four forms each), Park Concrete (four forms; the old name "concrete_wall" is aliased to it), Clear, Modern, Reinforced and Bright Glass in uncoloured form and in 16 dye colours with a pane each (136 glass blocks), Fossil Block, Encased Fossil Block, Encased Plant Fossil, Plant Fossil, three Nest Fossils and three Encased Nest Fossils, three Trackways (Medium Biped, Small Biped, Raptor), five Petrified Logs, Peat, Peat Moss Block, Moss, Tour Rail (four speeds), Low and Medium Security Fence (Base, Pole, Wire each), Display Block, Skull Display, Tool Pedestal. [v]

**Blocks (machines).** Fossil Grinder, Cleaning Station, Bug Farming Crate, DNA Extractor, DNA Sequencer, DNA Synthesizer, DNA Combinator, Embryonic Machine, Embryo Calcification Machine, Incubator, Feeder, Cultivator (16 dye colours, two-block tall), Genome Editor, Skeleton Assembly Table. [v]

**Wood sets.** Araucaria, Calamites, Ginkgo, Phoenix, Psaronius. Each has Log, Stripped Log, Wood, Stripped Wood, Planks, Leaves, Sapling, Stairs, Slab, Fence, Fence Gate, Door, Trapdoor, Pressure Plate, Button, Sign, Hanging Sign, Shelf, Boat and Chest Boat. [v] Phoenix also gives Phoenix Fruit and Phoenix Seeds.

**Plants.** 39 plain plants (ferns, cycads, flowers, bushes) and 8 aquatic plants and corals (Gracilaria, Enallhelia, Microsolena, Aulopora, Cladochonus, Lithostrotion, Stylophyllopsis, Hippurites Radiosus). Paleo Bales (Cycad, Cycadeoidea, Fern, Leaves, Other) are 3 by 3 crafts of plants used as feed. Crops: Ajuginucula Smithii, Wild Onion, Wild Potato, Rhamnus Salicifolius. [v]

**Items (progression).** Amber Mosquito, Amber Aphid, Plant Fossil, Twig Fossil, three Fossilized Eggs, Plaster and Bandage, Gypsum Powder, Sea Lamprey, Storage Disc, Soft Tissue, Plant Soft Tissue, DNA, Plant DNA, DNA Nucleotides, Syringe, Empty Syringe, Empty Test Tube, Petri Dish, Petri Dish With Agar, Plant Cells, Plant Cells in Petri Dish, Plant Callus, Liquid Agar, Dinosaur Egg, Hatched Egg, Spawn Egg, Growth Serum, Dinosaur Meat and Steak (per species), 48 bone types each in fossil and fresh form (listed in section 6 and the lang file), Basic Circuit, Advanced Circuit, Laser, Disc Drive, Keyboard, Computer Screen, DNA Analyzer, Iron Blades, Iron Rod. [v]

**Items (tools and weapons).** Dart Gun, Dart, Tranquilizer Dart, Euthanization Dart, Potion Dart (any potion), Fine Net, Pregnancy Test, PaleoPad, Breeding Wand, Birthing Wand (the two wands have no recipe), Paddock Sign, Mural, 19 Attraction Signs and 19 "with plants or coral" variants, Dinosaur Tracking Implant, Stamp Set, Plastic Lunch Box, three Music Discs. [v]

**Items (food).** Chilean Sea Bass (nutrition 10), Fun Fries (4), Oiled Potato Strips, Wild Potato and Cooked Wild Potato, Wild Onion, Phoenix Fruit, Rhamnus and West Indian Lilac berries (poison), Ajuginucula Smithii Leaves and Oil. [v]

**Items (vehicles).** Ford Explorer, Jeep Wrangler, Helicopter and their parts (Car Chassis, Engine System, Car Seats, Car Tire, Windshield, Unfinished Car). Sibling note. [v]

---

## 8. Villagers: professions and trades

### 8.1 Profession registration

**In game.** Two new villager jobs exist. A Paleontologist works at a Fossil Grinder. A Geneticist works at a DNA Sequencer. A vanilla villager with no job can take either job by claiming the workstation block. Cartographers gain one new trade at master level.

**Mechanism.** (`wkjvl/yknu.java:1-57`) [v]
1. Two POI types are registered, one per workstation block. Each POI uses every block state of the block, 1 ticket, search distance 1.
2. Two professions are registered. The constructor takes: display name key (`entity.jurassicraft.villager.<name>`), held and acquirable job-site predicates (the same POI), no requested items, no secondary POI, no work sound, and an `Int2ObjectMap` of level to trade-set key.
3. Geneticist trade sets: levels 1 and 2. Paleontologist trade sets: levels 1, 2 and 3. There are no level 4 or 5 trades for either job.
4. The vanilla tag `acquirable_job_site` is extended with both POIs. (`data/minecraft/tags/point_of_interest_type/acquirable_job_site.json`) [v]
5. A vanilla Cartographer at level 4 can get `cartographer/4/visitor_centre_map` through the vanilla tag `villager_trade/cartographer/level_4`. [v]

### 8.2 Trade data format

Each trade is one JSON file in `villager_trade/<profession>/<level>/<name>.json`. Fields: `wants` (id and a count provider), `additional_wants` (optional), `gives` (id), `given_item_modifiers` (loot-style functions that set count or species), `max_uses` (7 in all mod trades), `reputation_discount` (0.05; map trade 0.2), `xp` (2, 5 or 10). A trade set is `trade_set/<profession>/level_N.json` with `amount` (a number provider), a `trades` tag, and a `random_sequence`. Each villager level draws `amount` trades from the tagged list. (`villager_trade/**`, `trade_set/**`, `tags/villager_trade/**`) [v]

### 8.3 What each villager sells

| Villager | Level | Draws | Trades |
| --- | --- | --- | --- |
| Geneticist | 1 | 1 (10% chance of 2) from 9 | sells Petri Dish x8-12 (1-2 em), Liquid Agar x10-14 (1 em), Empty Test Tube x14-17 (1 em), Storage Disc x14-17 (3 em), Empty Syringe x10-14 (1 em), DNA Nucleotides x14-17 (1-2 em); buys DNA Nucleotides x14-17 for 1 em, Glass x10-19 for 1-2 em, Basic Circuit x5-9 for 1-3 em |
| Geneticist | 2 | 1 from 7 | sells DNA Extractor (6-7 em), DNA Sequencer (5-6 em), DNA Combinator (5-7 em), DNA Synthesizer (5-6 em), Embryonic Machine (4-7 em); buys Advanced Circuit x2-3 for 2-3 em, Advanced Circuit x10-14 for 1 em |
| Paleontologist | 1 and 2 | 1 each from 3 "common" | Iron Shovel (1-4 em), Plaster and Bandage x1-3 (1-2 em), Plant Fossil (1-4 em) |
| Paleontologist | 3 | 1 (10% chance of 2) from 27 | sells a bone of a random species for 1-6 em (24 bone types, for example Skull 1-6, Pelvis 1-4, Teeth 1-3); buys Skull for 1-4 em, Pelvis for 1-2, Teeth for 1-2 |
| Cartographer | 4 | the one added trade | Visitor Centre Map for 16-27 em + a compass, 7 uses, 20% reputation discount |

(em = emeralds. Data in `villager_trade/**`.) [v]

The map trade uses the vanilla `exploration_map` item function with the structure tag `on_visitor_centre_maps`, search radius 100 and a custom name key `filled_map.jurassicraft.visitor_centre`. A `filtered` function discards the trade when the result is not a filled map. (`villager_trade/cartographer/4/visitor_centre_map.json`, `tags/worldgen/structure/on_visitor_centre_maps.json`) [v] The radius value 100 is in the unit of the vanilla function. If that unit is chunks, the radius is smaller than the Visitor Centre cell of 448 chunks, so many maps fail and the trade is dropped. [inf]

**Platform overlap.** Entirely vanilla registries (26.x villager trade system). No loader API. [v]

**Verdict.** adopt (B21). Use this exact layout for paleontologist and map trades; the Nublar plan already says "26.2 trades are registry keys". Reuse an existing machine as the workstation (the Fossil Grinder) rather than a new block. Keep the sell price bands as a starting table: bones 1 to 6 emeralds.

---

## 9. Structures

### 9.1 Placement

| Structure | Spacing / separation (chunks) | Spread | Biomes | Notes |
| --- | --- | --- | --- | --- |
| Fossil digsite | 48 / 16 | linear | village biome tags (plains, desert, savanna, taiga, snowy) | buried; config toggle |
| Geneticist house | 48 / 16 | default | same village biomes | exclusion zone 4 chunks from the raptor paddock set |
| Raptor paddock | spacing = ceil(sqrt(N)), separation = spacing x 5/16, with N = `raptorPaddockRarity` (default 4,000, range 50 to 16,777,216); default 64 / 20 | linear | jungle, savanna | custom placement type `paddock_rarity` |
| Visitor centre | 448 / 320 | triangular | jungle, savanna | no rotation; 81 by 34 by 104 blocks |

(`worldgen/structure_set/*.json`, `worldgen/structure/*.json`, `kswtc/gjtxsudo.java:1-61`, `axvg/kijk.java:1-37`) [v]

The Visitor Centre cell is 448 by 448 chunks (7,168 blocks per side). Triangular spread over a 128-chunk window means about one per 200,000 chunks of area. [inf] A Cartographer map is the intended way to find it.

The template structure type requires a flat site: it samples a 3 by 3 grid of columns at 16-block spacing around the chunk corner and refuses to place when the world-surface height and ocean-floor height differ on any (water or leaves in the way). Templates place with `beard_thin`, Y offset -1, and random rotation and mirror (not the Visitor Centre). (`uzuj/dbgouk.java:1-81`) [v]

### 9.2 Fossil digsite

**In game.** A small pit with ladders, sand and sandstone. Logs and oak fences brace the walls. Torches stand on posts. About 11 to 25 fossil blocks sit in the sand or stone (half of the marker cells). A chest holds fossils and plaster. An old iron shovel or pickaxe lies on a Tool Pedestal. A Paleontologist stands in the middle. [v]

**Mechanism.**
- Three templates: 8 by 7 by 7, 8 by 10 by 8, 9 by 13 by 9 (blocks). The structure picks one at random, plus random rotation and mirror. It places the piece at surface height minus (template height - 2), so only 2 layers stand above ground. (`uzuj/vtclb.java:1-45`, `data/jurassicraft/structure/fossil_digsite_*.nbt` via `scratchpad\nbtinfo.py`) [v]
- Data markers in the NBT: Log, Log1, Log2 (oak logs with rotation fix), Fence, Torch, Fossile (50% a random block from the `digsite_fossils` tag, else sand), Fossile2 (50% fossil, else air), Chest (loot `chests/fossil_digsite`), Shovel (Tool Pedestal holding an iron shovel or iron pickaxe with 20 to 248 damage). The block-state fix sets `rock` from the block below and sets the fossil variant from the species at that position (section 2.1). (`uzuj/dnukfuguy.java:1-202`) [v]
- A Paleontologist villager spawns once during post-processing at the piece centre at the base plus 3. It gets a home radius of 10 and XP 1. A `Staffed` flag saves in the piece so it does not respawn. [v]
- The chest: 2 to 3 rolls of {Amber (Mosquito or Aphid), Plaster and Bandage x1-3}, 0 to 1 roll of a Fossilized Egg x1-3, and 6 to 9 rolls of a random bone of a random species, 1 to 2 each. (`loot_table/chests/fossil_digsite.json`) [v]

### 9.3 Geneticist house

**In game.** A small smooth-stone and plank house (8 by 6 by 14) in a village biome, with bookshelves, glass panes, a bed, a double chest, and a DNA machine. A Geneticist villager lives there. A mooshroom stands in the template. [v]

**Mechanism.** Data markers set the wood, stone, hay, doors and torches. Two marker spots hold machines: one holds a DNA Sequencer (decided by the parity of the template X plus Z), and the other holds a random one of {Extractor, Combinator, Synthesizer, Embryonic Machine} with 50% chance or air. A bed (red) goes at a fixed cell. The villager spawns at the first open floor cell near the centre. (`uzuj/jgdp.java:1-248`, `uzuj/aeygzxj.java:1-102`) [v]
The chest: 2 to 3 rolls of {Storage Disc x1-8, Test Tube x1-8, Petri Dish x1-5, Empty Syringe x1-7}, 1 to 2 rolls of {Liquid Agar x1-3, DNA Nucleotides x1-2, Plant Fossil x1-5}, 0 to 1 roll of {Amber, DNA Extractor, DNA Sequencer, Disc Drive}. (`loot_table/chests/geneticist_house.json`) [v]

### 9.4 Raptor paddock

A 31 by 16 by 22 enclosed pen made of Reinforced Stone, stone bricks, iron bars and Low Security Fence (38 bases, 36 poles, 45 wires), vines, jungle trees and 6 levers with redstone lamps. It holds one chest (vanilla village weaponsmith loot). It has no entities: no dinosaurs spawn in it. [v] (`structure/raptor_paddock.nbt`)

### 9.5 Visitor centre

An 81 by 34 by 104 jungle ruin (68,905 blocks): quartz, cyan terracotta, hay, reinforced stone, 305 redstone lamps, 273 levers, 11 Murals and 3 paintings, plus 9 loot rooms. [v] (`structure/visitor_centre.nbt`)

| Room marker | Markers placed | Loot highlights |
| --- | --- | --- |
| Laboratory | 6 | Storage Discs, Test Tubes, Liquid Agar, Syringes, Petri Dishes, Nucleotides up to 64 each; 3 to 4 rolls of a Storage Disc with random DNA, 0 to 3 Soft Tissue of random species, 0 to 3 plant soft tissue, amber |
| Control room | 3 | Basic Circuit up to 64, Advanced Circuit up to 32, Laser, Disc Drive, Computer Screen, Keyboard, redstone parts |
| Ground storage | 17 | Feeders (up to 4), Low Security Fence parts, car parts, seeds, wool, amber, signs |
| Garage | 3 | Tires, Seats, Engine System, Chassis, Unfinished Car, Iron Rods |
| Infirmary | 3 | Syringes up to 48, Test Tubes, Plaster and Bandage up to 24 |
| Kitchen | 11 | Crops, meat, Chilean Sea Bass, Fun Fries, ice, potions |
| Dining hall | 2 | Stamp Set, skulls of random bones, random-species Display Blocks (Action Figures) |
| Staff dorms, Dorm tower | 3 and 12 | Emeralds, gold nuggets, paper, books, iron and leather armour |
| Cryonics | 1 | no table (a marker with no effect) |

(`loot_table/chests/visitor_centre/*.json`, `uzuj/gjtxsudo.java:1-60`) [v]

**Platform overlap.** Custom `StructureType` and `TemplateStructurePiece` classes in code, with structure JSON that names the type. `/locate` works. NeoForge registers the types (`uzuj/kodkieb.java`, `uzuj/ummp.java`). Fabric would register the same types in its own entry. [inf]

**Verdict.** adapt (B21). The data-marker approach (a piece class turns markers into logs, torches, fossils, chests and a pedestal) is good for a hand-built digsite. It differs from the plan (jigsaw and mob pools). The mod spawns the villager inside `postProcess`. The plan forbids entity spawns in worldgen, so use the plan's data-marker mob pool instead. Take the flat-site check, the surface-minus-height burial, the species-from-position block fix and the chest contents as starting data. Do not copy the 6 templates; Nublar needs its own art.

---

## 10. Weapons: dart gun, tranquilizers and venom

**In game.**
- The Dart Gun holds one stack of up to 8 darts of one kind.
- Right-click with the gun empty loads up to 8 darts from the inventory (it scans the off-hand, then hotbar slot 8 down to 0, then the main inventory from 35 down to 9). The reload sound plays, or the "empty" sound if the player has no dart.
- Right-click when loaded fires one dart. A 15-tick cooldown follows.
- Sneak plus right-click returns the loaded darts to the inventory.
- A small HUD shows the loaded dart item and its count above the hotbar at the bottom-right whenever the gun is in either hand.
- The tooltip reads "Empty" or "Loaded: N x name". [v]

**Mechanism.** (`bugratnc/ummp.java:1-128` gun; `bugratnc/kodkieb.java:1-83` dart item; `ysulhvcy/aeygzxj.java:1-94` projectile; `onzkovhp/dnukfuguy.java:1-53` HUD) [v]
- The dart is a thrown projectile. The gun launches it at speed 2.5 with inaccuracy 0.5. A cloud particle trails it. A hit plays an arrow-hit sound and calls the dart item's hit effect. If the hit part belongs to a multi-part creature, the effect goes to the parent. The dart is then discarded.
- Recipes: Dart x8 = 2 iron ingots and a feather. Tranquilizer Dart = Dart + Spider Eye. Euthanization Dart = Dart + Pufferfish. Potion Dart x8 = 8 Tranquilizer Darts around a Lingering Potion in a 3 by 3. (`recipe/dart.json`, `recipe/tranquilizer_dart.json`, `recipe/euthanization_dart.json`, `smsbgw/dnukfuguy.java:1-52`) [v]
- Effects:
  - **Dart:** 1 generic damage on the creature. Tooltip "Pricks a dinosaur".
  - **Tranquilizer:** sets a sedation timer of `50 + 600 + rand(50)` ticks (650 to 699, about 33 to 35 seconds) and puts the creature to sleep. The timer ends the sleep. Tooltip "Sedates a dinosaur for about 35 seconds". (`ctygd/omogwgxr.java:671-704`)
  - **Euthanization:** 200 ticks of Poison; when the 200-tick timer ends the creature takes lethal "magic" damage and the cause of death is set to Euthanasia. Tooltip "Kills a dinosaur over 10 seconds".
  - **Potion Dart:** applies every effect of the potion it was crafted from, using the potion's colour for the item tint.
- Only creatures of this mod react. A Growth Serum, a Pregnancy Test and other tools use the same "interact with creature" hook. [v]

**Pregnancy Test:** right-click a creature. Male: "Male dinosaurs can't get pregnant" (item kept). Female: "is pregnant" or "is not pregnant" and the one-use item breaks. Recipe: pink wool, redstone dust, white wool. [v]

**Venom.** A `venom` entity (0.25 by 0.25, no save) is the Dilophosaurus spit projectile; the species JSON lists `venom_spit` under `abilities` for one species. A `death.attack.shock` and `death.attack.run_over` message exist for the fence and the vehicles. [v]

**Vanilla feel.** Close to a vanilla crossbow plus tipped arrows. The loaded-stack model is simple and readable.

**Platform overlap.** Uses a vanilla `ThrowableItemProjectile` and a data-component container. No loader API. [v]

**Verdict.** defer (B23 is blocked by decision D08). If the lead picks tranquilizer: the mod uses a fixed sedation time with no body-mass scaling, which is simpler than the plan's design direction (points scale down by body mass).

---

## 11. Park building blocks

### 11.1 Fences

**In game.** Two fence tiers exist: Low Security and Medium Security. Each has three parts. A Base sits on the ground. A Pole stands on top of a Base (or on another Pole). A Wire joins poles. A redstone signal on a Pole electrifies the fence. An entity inside a powered Wire block takes shock damage (1 heart-point per hit at low, 2 at medium) and the creature is stunned; a click-sound plays every 10 ticks. Players and climbing species can climb the wire (it acts as a ladder for the player and for species that have `can_climb`). Fence blocks need an iron tool to drop. [v]

**Mechanism.** (`qkek/kodkieb.java:1-52`, `qkek/dnukfuguy.java:1-156`, `qkek/vtclb.java:1-130`, `qkek/aeygzxj.java:1-266`, `qkek/ummp.java:1-335`) [v]
- Tier constants: Low shock 1, base hardness 2, pole 3, wire 3.5. Medium shock 2, base 3, pole 4, wire 4.5.
- A Base has 4 connection booleans, a `pole` flag and a `connections` count 0 to 4. Its collision box is a full block, except that a creature sees a 1.5-tall box when blocks above are non-air and the creature cannot climb.
- A Pole has 4 side connections and a `powered` flag. It connects to a Pole of the same tier, to a Wire of the same tier, or to a Wire one block below that points back at it.
- A Wire has 4 sides, an `up` direction (down or one of 4 sides), and a `powered` flag. Its shape is a thin post. It blocks creatures and players but not item entities.
- Power: when a Base, Pole or Wire gets a neighbour update, the code runs a bounded flood fill over Bases of the same tier. The fill moves one block sideways with a vertical step of -1, 0 or +1 and stays within 16 blocks of the first Base, and it marks powered the Wires within 8 blocks. A Wire is powered when any connected Base in range has a Pole directly above that receives a redstone signal. The column scans stop after 5 non-fence blocks (poles may have gaps up to 5), and column heights cap at 256 blocks. Only neighbour updates run it; no tick, no block entity. [v]

**Recipes.** Low Base x6 = 2 iron nuggets + 1 iron ingot + 3 stone + 1 redstone. Low Pole x2 = redstone over 2 iron ingots. Low Wire x16 = 6 iron ingots + 1 redstone. Medium parts are made from Low parts and iron ingots (Base x3, Pole x3, Wire x8). (`recipe/low_security_fence_*.json`, `recipe/med_security_fence_*.json`) [v]

**Verdict.** adapt (B15, B14). The "powered flag computed only on neighbour updates by a bounded BFS" pattern is cheap and fits the plan's B25 perf work. Whether Nublar powers fences by redstone or by generators is a B14 decision. This mod uses redstone only, with no energy network.

### 11.2 Concrete, stone, glass

Park Concrete x8 = 4 sand, 4 gravel, 1 clay ball in a ring. Hardness 1. Reinforced Stone x4 = 4 stone + 4 iron ingots in a ring (hardness 2, resistance 6); Reinforced Bricks (hardness 3, resistance 6). Gypsum Cobblestone is an ore drop (gypsum stone smelts from cobble), Gypsum Bricks x4 from 4 cobble. Phaneritic Stone generates in the world (hardness 1.0); Polished Phaneritic x4. All have stairs, slabs and walls (crafted or stonecut). Glass: Clear (smelt vanilla glass), Modern, Reinforced (hardness 10, resistance 14), Bright (light level 15). Each dyes into 16 colours (8 glass + 1 dye in a ring) and each has a pane (16 per 6 glass). The glass is `TransparentBlock`-based and the coloured glass blocks act as beacon beam colourants. (`wkjvl/vtclb.java:1-339`, `recipe/*glass*`, `recipe/park_concrete.json`) [v]

### 11.3 Signs, murals, paddock signs

- **Paddock Sign** (stack 16): 3 iron nuggets over 3 signs over 3 iron nuggets. Right-click a wall face opens a picker screen titled "Which animal is this paddock for?" with Previous and Next buttons over the loaded species. The placed entity stores the species id and shows its picture. The picker stays empty with the text "No species are loaded" if the registry is empty. [v] (`bugratnc/hmocea.java:1-39`, `wgpfjieca/kijk.java:1-144`, `ysulhvcy/jgdp.java:1-63`)
- **Attraction Sign** (stack 16): 3 iron ingots over 3 signs over 3 iron ingots. It is a hanging entity 8 blocks wide and 4 to 6 blocks tall. The player clicks at the top of a clear wall; if not, the action bar says "No room - needs a 8 x N wall, clicked at the top". There are 38 designs (19 themes, each plain or with plants/coral). Each sign crafts into the next in a loop, so the player picks a design by crafting. (`bugratnc/dnukfuguy.java:1-55`, `ysulhvcy/vtclb.java:1-99`, 37 `attraction_sign_*_from_*` recipes) [v]
- **Mural** (stack 16): 4 iron ingots, 4 white wool and a painting. It is a hanging entity picked at random from 11 designs (Creation Lab, Fossils, Hunt, Mosasaurus, Park Entrance, Riding, Sketch, Triceratops, Valley, Dilophosaurus, The Great Mosasaurus). The code tries each design at the clicked spot and nearby offsets. It places one that fits, or says "No room - needs a larger wall". Width and height come from the picture size divided by 16. (`bugratnc/rtwzimpls.java:1-48`, `ysulhvcy/ummp.java:1-130`, `ysulhvcy/kodkieb.java:1-83`) [v]
- All three extend one hanging-entity base with the vanilla painting sounds, no drop in creative, and a standard add-entity packet. (`ysulhvcy/gjtxsudo.java:1-53`) [v]

**Verdict.** defer (B24 and later). Signs and murals are art-driven. The data-driven type list with texture paths is a good model, but the 38 sign designs and 11 murals are this mod's art and cannot move to Nublar.

---

## 12. Plants and food

- **Ancient plants** (39 land plants): each is a block with a `plant` JSON entry. Types: single, double (two-block tall), crop. They tick randomly and can spread unless `plantSpreading` is off. [v]
- **Feed tiers** for herbivores (tags `feed/free`, `feed/farmed`, `feed/effort`, `feed/premium`, `feed/bale`): free (leaves, grass, seagrass, kelp, berries), farmed (crops and meat), effort (ferns and cycads the player plants), premium (rare plants, hay, Paleo Bale Leaves, dinosaur meat) and bales (compacted plants). A data map `food_nutrients` gives lipids, minerals, proximates and vitamins per food, used by the Cultivator and perhaps by feeders. (`tags/block/feed/*.json`, `tags/item/feed/*.json`, `data_maps/item/food_nutrients.json`) [v]
- **Compost.** A data map gives compost chances: ancient leaves 0.3, ancient plants 0.65, saplings 0.3, Paleo Bales 0.85. (`neoforge/data_maps/item/*.json`) [v]
- **Strip data map** maps each log to its stripped version for axes. [v]
- **Crops:** Ajuginucula Smithii (leaves edible, nutrition 1; 3 leaves + glass bottle = Oil), Wild Potato (raw nutrition 1; cooked 6; with Oil makes Oiled Potato Strips x4, smelted to Fun Fries nutrition 4), Wild Onion (nutrition 3), Rhamnus Salicifolius (berries nutrition 5 with Poison 1,400 ticks), West Indian Lilac berries (nutrition 1 with the same Poison). Chilean Sea Bass = cooked cod + onion + carrot + Ajuginucula leaves (nutrition 10). Phoenix Fruit (nutrition 4) returns a Phoenix Seed 50% of the time when eaten. (`wkjvl/kdhoqn.java`, `bugratnc/cvrd.java:1-30`, recipes) [v]

**Verdict.** defer (R01, Release 1). The plant system is not a Beta need. Reuse the feed-tier idea in B20 only.

---

## 13. Info screens and in-game guidance

| Surface | What the player sees | Source |
| --- | --- | --- |
| PaleoPad (iron ring around a Computer Screen) | Right-click any creature. A 256 by 192 screen opens with page 1: a rotating model, health, hunger, thirst and age bars with hover tooltips, growth stage and sex, owner head, pack size when the herd limit is reached, cause of death for corpses, and a row of status icons (Eats meat or plants, Active by day or night or dusk, Hungry, Thirsty, Pregnant, Scared, Imprinted, Poisoned, Badly hurt, Drowning, Moving with the herd, Ready to sleep, Wild). Page 2: the species' description text. Age is shown in days of 3,000 ticks. | `wgpfjieca/kdhoqn.java:1-355`, lang `gui.jurassicraft.paleo_pad.*`, `entity.*.desc` (131 entries) |
| Order screen | A small screen "Give an order": Follow, Sit, Wander, for imprinted creatures only ("This creature is not yours" otherwise). | `wgpfjieca/onzkovhp.java:1-52`, lang |
| Item tooltips | Species name; DNA quality in colour (green above 75, yellow above 50, gold above 25, red); genetic code (shown as scrambled text when empty); build percent; coloration swatches; "Sneak and right-click to change pose/sex". | `etemhnv/gjtxsudo.java:1-111`, `bugratnc/qkek.java:1-33` |
| JEI info pages | 13 pages: Amber, Bones, Bugs, Cultivator colours, Fossils, Liquid Agar, Moss, Peat, Plant Callus, Plant Cells, Pregnancy Test, Swarms, Wands. Plus recipe categories for lab machines, cleaning, grinding, bug breeding and moss spreading. | lang `jei.jurassicraft.*`, `cvrd/*.java` |
| Jade tooltips | Shows the species on fossil and display blocks; shows machine status on machines (Idle, Running, Out of nutrients, Out of water, Output full, Cannot use this input). | `cvrd/aeygzxj.java:1-60`, lang `config.jade.*` |
| Machine screens | One screen per machine, with progress arrows and water or nutrient gauges. | `wgpfjieca/*.java`, `dbgouk/*.java` |
| Genome Editor hologram | Optional faint hologram of the animal over the editor. | config `genomeEditorHologram` |
| Messages | Action-bar hints: "The fossil is too brittle...", "Maybe I should try planting this on tilled soil", "No room...", "This creature is too young to carry anything", "Applied / Cloned" in the editor. | lang `message.jurassicraft.*` |

**There is no guide book, no lectern book and no tablet that works without an animal.** The PaleoPad opens only on a creature. [v]

**Verdict.** adapt (B19). The PaleoPad page layout (bars, status icons, species text) is a solid species info screen. The page text in the lang file reads like encyclopedia text copied from public sources; Nublar writes its own. Add recipe-viewer info pages for B13; they cost almost nothing.

---

## 14. Advancements

All 468 advancements are recipe unlocks (the recipe book). There is no first-fossil, first-egg or first-hatch advancement. (`advancement/recipes/**`; no other files under `advancement/`) [v]

**Verdict.** drop for B13: the plan wants step advancements (first fossil, first tube, first egg, first hatch, first paddock). Write Nublar's own.

---

## 15. Commands and developer tools

| Command | Permission | Purpose |
| --- | --- | --- |
| `/jcgui list`, `/jcgui preview <machine>`, `/jcgui layout <machine>` | any | print or preview machine GUI layouts |
| `/jcmodel bones`, `/jcmodel validate`, `/jcmodel frame` | any | inspect the nearest creature (32 blocks): bones, model validation, animation frame |
| `/jcmachine info\|flood\|feed\|clear [pos]`, `/jcmachine rig fossil_grinder\|bug_crate`, `/jcmachine skeleton <species> [fresh] [pos]`, `/jcmachine bones <species> [fresh]`, `/jcmachine discs <species>`, `/jcmachine kit`, `/jcmachine turbo [on\|off]` | game masters | fill machines, hand out test bones, discs and a kit, and make every machine job take 1 second |

(`gjtxsudo/dnukfuguy.java:1-82`, `gjtxsudo/vtclb.java:1-142`, `ufpurxw/rqkeevhve.java:1-424`, registration lines in `Jurassicraft.java:38-44`) [v]

**Verdict.** adapt (B12, B25). A "turbo on/off" switch and "rig/kit" helpers make machine GameTests and manual runs short. Add them as a debug command with a permission check.

---

## 16. Config options

All options use NeoForge `ModConfigSpec`: client, common and server files. (`dnukfuguy.java:1-141`) [v]

| File | Option (default) |
| --- | --- |
| Client | `logModelLoading` false, `helicopterZoomOut` false, `fastVertexWrites` true, `genomeEditorHologram` true |
| Common | `logMissingSpecies` true |
| Server: creatures | `naturalSpawning` false (hybrids never spawn), `creatureGrowth` true, `creatureMetabolism` true, `creaturesBreakLeaves` (on), `creatureFoodRequirement` 1.0 (0 to 1), `maxHerdSize` 4 (0 = species value, 1 to 1,024), `packSizeLimitsBreeding` true, `huntWhenHungry` true, `allowCarcass` true, `growthSerumEnabled` true, `partSpatialIndex` true, `partIndexVerify` false, `pathColumnVerify` false |
| Server: world | `vehiclesDestroyBlocks` true, `tourRailsEnabled` true, `plantSpreading` true |
| Server: minerals | `amberGeneration`, `iceShardGeneration`, `gypsumGeneration`, `phaneriticGeneration`, `fossilGeneration`, `plantFossilGeneration`, `petrifiedTreeGeneration`, `nestFossilGeneration`, `trackwayGeneration` (all true) |
| Server: plants | `mossGeneration` true, `peatGeneration` true, `peatGenerationSpeed` 20 (0 to 64; 0 stops spread), `flowerGeneration` true, `gracilariaGeneration` true |
| Server: structures | `fossilDigsiteGeneration`, `visitorCentreGeneration`, `raptorPaddockGeneration`, `geneticistHouseGeneration` (all true), `raptorPaddockRarity` 4,000 (50 to 16,777,216) |
| Server: loot | `lootSpawnrate` 100 (0 to 1,000; "percentage of the 1.12 amount of fossils and records in vanilla chests") |

**Toggle mechanism.** A custom placement modifier `config_filter` takes the name of a toggle and returns false when the option is off. Structures use an optional `toggle` field. The paddock rarity uses a custom structure placement type `paddock_rarity` that reads the option each time it picks the chunk. Changes apply to chunks generated afterwards. (`kswtc/dnukfuguy.java`, `kswtc/ummp.java:1-43`, `kswtc/gjtxsudo.java`) [v]

**Chest loot.** A global loot modifier on six vanilla tables (village weaponsmith, nether bridge, simple dungeon, stronghold corridor, desert pyramid, abandoned mineshaft) runs `1 + rand(2)` rolls of a 14-slot table: plant fossil x1-3 (5), twig fossil x1-3 (5), amber (2), skull of a random species x1-2 (2). Counts scale with the option. It then adds `rand(max(1, floor(2 x rate) + 1))` music discs picked from the three discs (0 to 2 at the default). (`axvg/gjtxsudo.java:1-89`, `loot_modifiers/chest_loot.json`) [v]

**Natural spawns.** A custom biome modifier adds each species to the biome spawn lists by its `habitat.biomes` and `spawn_chance`, with a group size of `max(1, min(6, herdSize / 2))`, in a custom mob category `jurassicraft:prehistoric` (cap 10, friendly, not persistent, despawn range 128). Spawn placement is on ground for land animals and in water for water animals, in the Overworld only. (`axvg/fgwme.java:1-33`, `core/JCMobCategories.java`, `axvg/onzkovhp.java:1-51`) [v]

**Verdict.** adopt (B00). The toggle pattern gives per-feature on/off with no code in each feature. Take the "rate" option for loot injection. Keep natural spawning off by default as this mod does.

---

## 17. Vanilla feel versus not

| Feels vanilla | Does not feel vanilla |
| --- | --- |
| Ore veins, stone and deepslate hosts, Silk Touch and Fortune on amber and ice shard | A depth-ordered fossil column and a jacket step |
| Crafting-table recipes, stonecutter recipes, smelting for agar, nucleotides and steak | A 12-machine pipeline with its own recipe types |
| Five full wood sets with shelves, hanging signs and boats | Multi-hit carcasses and fresh versus fossil bones |
| Villager professions with data-driven trades | Hanging signs and murals that are 8 blocks wide |
| Loot-table injection and fishing/bug loot | Electric fences with flood-filled power |
| Standard glass, stone and slab/stair/wall sets | A PaleoPad screen, species registry, 129 species |

---

## 18. Findings about gaps in the mod (not copied, for Nublar to avoid)

1. Some items have no acquisition: Dinosaur Tracking Implant, Plastic Lunch Box and Stamp Set have no recipe, and only the Stamp Set appears in one loot table. Action Figures appear only in the dining hall chest or the creative tab. The Tool Pedestal has no recipe. The two wands are creative-only by design. (grep of `recipe/` and `loot_table/`) [v]
2. Visitor Centre maps may fail because the search radius is smaller than the structure cell (section 8). [inf]
3. The "Cryonics" marker in the Visitor Centre template has no table or block. [v]
4. The cached species lists in the fossil picker live in a synchronized `WeakHashMap` keyed by registry; they reset only when the registry object changes. A reload with the same registry object but changed data could return stale lists. [inf]

---

## Project Nublar fit

Verdicts: **adopt** (use as is), **adapt** (take the idea, change the data or code), **defer** (not for Beta), **drop** (do not use).

| Technique | Nublar module | Verdict | Reason |
| --- | --- | --- | --- |
| Geologic depth bands from `period`, largest-remainder split of the 125 layers | B01, B00 | adapt | gives a default depth for every species with no hand tuning; Nublar still adds biome groups and quality |
| Species chosen per 8-block cell from a seeded random source; no block entity on the fossil block | B01 | adapt | fits the plan's "no block entity" and thread-safety rules; replace the synchronized global cache with an immutable map built once on the main thread |
| Fossil block with 8 states (2 rocks x 4 variants) and species derived from position | B01 | adapt | keeps the block-state count tiny (plan target about 4,600); Nublar's quality and piece properties add states |
| Jacket the fossil first (Plaster and Bandage), bare break drops nothing | B02 | defer | adds a step and a new item; raise it with the lead as a design option; keep Fortune and Silk Touch as the plan says |
| One item per bone type with `species` and `fresh` components; tooltip shows species | B02 | adopt | matches the plan's single `FossilItem` with components |
| Amber ore drops Mosquito (animal) or Aphid (plant) 50/50; Silk Touch drops the ore; species rolled at extraction | B03 | adopt | very close to the plan; the plant/animal split is a cheap extra |
| DNA sample = species + quality 5-100 step 5; combine adds; synthesis needs 100; disc keeps 3 syntheses | B04, B05 to B07 | adapt | the plan has a yield percent; this shows a compatible merge rule and a charge counter |
| Carcass with N hits (`clamp(2 x sqrt(w x h), 1, 10)`), meat with DNA per hit, fresh bones 90% on the last hit, `allowCarcass` toggle | B04, B16 | adopt | matches B04 remains items; keeps a config switch |
| Sea Lamprey from Ice Shard as marine DNA | B04 | defer | useful for aquatic species; only after aquatic species exist |
| Meat as a DNA source in the Extractor | B04 | adopt | cheap loop for species the player already owns |
| Bugs from vanilla blocks via loot modifier; Bug Crate breeding | B20, R03 | defer | belongs to feeders and insectivores |
| Fine Net, plankton and krill swarms | R03 | defer | Release 1 |
| Villager trades, trade sets and tags in the 26.x data layout | B21 | adopt | the plan already says trades are registry keys; this is a working example (`villager_trade/`, `trade_set/`, tag per level) |
| Fossil Grinder as Paleontologist workstation, DNA Sequencer as Geneticist workstation | B21 | adapt | no new workstation block; Nublar's Fossil Processor can serve as the job site |
| Geneticist sells the machines for 4-7 emeralds | B13 | adapt | satisfies "no part is trade-only" only because the crafting recipe also exists; copy the rule, not the prices |
| Cartographer master trade with `exploration_map` and a structure tag | B21 | adopt | same pattern as the plan; size the digsite cell below the map radius |
| Custom `Structure` + `TemplateStructurePiece` with data markers for chests, torches, fossils and a pedestal | B21 | adapt | good for a hand-built digsite; the plan prefers jigsaw and mob pools; do not spawn entities in `postProcess` |
| Flat-site check by comparing world-surface and ocean-floor heights on a 3 x 3 grid | B21 | adopt | cheap and avoids water and tree sites |
| Structure placement from "one per N chunks" (spacing = ceil(sqrt N), separation = 5/16) | B21, B00 | adapt | lets a single config number set rarity; needs a custom placement type on both loaders |
| `config_filter` placement modifier and structure `toggle` field | B00 | adopt | per-feature switches without code in features; note "new chunks only" |
| Loot injection into six vanilla chest tables through a percent option | B13, B21 | adapt | the plan wants inject pools at load; a global loot modifier is NeoForge-only, so Fabric needs the loot-table-load event |
| Visitor Centre loot rooms with ready-made DNA discs and parts | B21, R03 | defer | a rare reward structure; not in the Beta list |
| Dart gun: 8-dart stack, 15-tick cooldown, speed 2.5; Tranquilizer 650-699 ticks; Euthanasia 200 ticks poison then kill; Potion Dart from a lingering potion | B23 | defer | blocked by decision D08; use as a data point only (no body-mass scaling here) |
| Electric fence power by redstone on a pole, bounded flood fill on neighbour updates, shock 1 or 2, climbable wire | B15, B14, B25 | adapt | no ticks and no block entities is the right cost profile; B14 decides the power source |
| Park Concrete, Reinforced Stone/Bricks, Gypsum and Phaneritic sets with four forms each; 4 glass kinds x 16 colours | B15, R03 | defer | decoration; Nublar needs its own art |
| Paddock Sign with species picker; Attraction Signs; Murals | B24, R03 | defer | art-driven; take only the data-driven type list idea |
| PaleoPad info screen (bars, status icons, species text page) | B19, B24 | adapt | solid layout; write Nublar's own text and art |
| JEI info pages and Jade tooltips for machines and species | B13 | adapt | low cost; keep optional |
| Recipe unlocks as the only advancements | B13 | drop | the plan wants step advancements; write them |
| Items with no way to obtain (Tracker, Lunch Box, Stamp Set, Tool Pedestal) | B13 | drop | breaks the plan rule that no item is loot-only or creative-only without a mark |
| Debug commands: `/jcmachine turbo`, `rig`, `kit`, `skeleton`, `bones`, `discs` | B12, B25 | adapt | shortens machine tests; guard by permission |
| Config: 41 options with a loot percent, a rarity number, natural spawn off by default | B00 | adopt | good defaults; keep spawn off |
| Species and plant as data-pack registries with `hybrid_of`; hybrids excluded from fossils and natural spawns | B00, B01 | adopt | the plan's SpeciesData is data driven; add a `hybrid` exclusion for fossils |

### Port notes for P2 (APIs seen in this 26.1.2 jar) [v]

These names appear in the source. The Nublar target is 26.2, so check each name again at port time. [inf]

- `Identifier` replaces `ResourceLocation`.
- Villagers: `VillagerProfession` takes a `Int2ObjectMap<ResourceKey<TradeSet>>`. Trades are `VillagerTrade` registry entries. `TradeSet` and `VillagerTrade` have bootstrap contexts and tag keys.
- Entity save and load use `ValueOutput` and `ValueInput`.
- GUI draws through `GuiGraphicsExtractor` and `extractRenderState`. HUD layers register with `RegisterGuiLayersEvent`.
- Damage: `hurtServer(ServerLevel, DamageSource, float)`.
- Game rules: `GameRules.ENTITY_DROPS` and `GameRules.MOB_DROPS` are read through `getGameRules().get(...)`.
- Items: `ItemStackTemplate`, `TooltipDisplay`, `Item.TooltipContext`, `getBarWidth`, and shelf blocks exist.
- Commands: `Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)`.
- Block: `updateShape` takes a `ScheduledTickAccess`; `neighborChanged` takes an `Orientation`.
- Registry alias: `ModifyRegistriesEvent` with `addAlias` migrates the old block id `concrete_wall` to `park_concrete`.
- Config: `ModConfigSpec` with `registerConfig(Type.CLIENT, COMMON, SERVER)`.
- Mob category: `EnumProxy<MobCategory>`.
- Data maps: `DataMapType` with `RegisterDataMapTypesEvent`.
- Spawn placement: `RegisterSpawnPlacementsEvent`.

### Gaps

- I did not read machine block-entity internals (processing loops, power, slots). The genetics note covers them.
- I did not read the creature AI, breeding, feeders or vehicles. Other notes cover them.
- Two block `getStateForPlacement` bodies in the fence code and the Petrified/Phoenix tree grower `place` body for one tree kind did not decompile. Behaviour above for those is from the call sites. [inf]
- `processing_time` units are inferred as ticks.
- The package `wetxj` was excluded and not described.
