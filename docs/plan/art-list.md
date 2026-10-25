# Art list

Mirror of the shared page sub-tab "Art list" under "Beta and Release 1 project plan". Back to the [roadmap](../project-plan.md).

This page is the only place where asset status lives. The roadmap, the feature specs and the engineering backlog link here and do not repeat a status.

## How to read this page

- **Per thing:** each thing has a short "To make / to import / to fix" block, then tables by art type. One row is one asset.
- **Used for:** the game reason.
- **Feature:** the feature that needs the asset (F1.1 to F4.3, plus F2.0 for dinosaur behaviour, the same ids as the specs).
- **Where / reference:** the file or link.
- **MOD** = `common/src/main/resources/assets/projectnublar` in the mod.
- **REPO** = the asset repo `Dumb-Code/ProjectNublar-Assets`, branch master, folder `assets`.
- **Order:** rows in each table are sorted by status, work first.

Status words:
- **Missing**: no right asset anywhere.
- **Placeholder**: something stands in (the row says what). It counts as missing.
- **To check in game**: the asset is in the mod, but nobody has checked it in game or against the video. It counts as work to do, not as done.
- **Needs export**: a REPO Blockbench file holds it.
- **Needs conversion**: a REPO DumbCode file (.tbl, .dcm, .dca) holds it, or the clip names bones that the model does not have.
- **Ready to import**: the right asset is in REPO and works as is.
- **Shared, artist to confirm**: one file serves both genders, or one generic sound serves several machines or species. It is not counted as done or as missing.
- **Done**: the right asset is in the mod and somebody has checked it.
- **Missing, only if the lead approves** (and similar "only if" rows): a proposal or an open scope question. It is listed so that nothing is lost, and it is not counted until the lead decides.
- **No art needed**: code draws it or a vanilla asset serves. Not counted.
- **Not in Beta**: listed only. Not counted.

## Rules for all art

**No stand-ins**
- A copied file is a Placeholder.
- An empty clip (0 bones) is a Placeholder.
- A clip or sound of another species is a Placeholder.

**How the files were checked**
- Textures were checked as files and hashes, not opened as images.
- The machine block and GUI textures were also opened as images.

**Machine art**
- The current models and textures in the mod are the target art. They differ from the 2022 video.
- The video sets behaviour, timings and screen contents only.
- A machine part never counts as missing because it looks different from the video.
- A screen follows the video for its contents (slots, gauges, tabs, buttons) and the current GUI texture for its art.
- Code-driven machine motion is code work. Its art is the model part with the right pivot, which has its own row.
- If an animation needs a part that the current model lacks, the row says how the animation adapts to existing parts, or it asks the artist for a new part.
- If the cubes exist but sit inside a larger bone, a developer moves them into a bone of their own. No new art, no texture or UV change. Such a row is Done.

**Upgrade looks**
- No upgrade gets a new part.
- The Processor tanks and the Sequencer chip cubes use parts that exist.
- Every other upgrade gets a texture swap on an existing bone.

**Machine sounds**
- Each machine uses its own sound where one exists, and a generic lab sound for the rest.
- The artist also makes sounds of its own for each machine.
- Generators get sounds of their own.
- Style reference: the machinery sounds of Jurassic World Evolution 2 and 3.

**Dinosaur skins**
- The albino and melanistic morphs are tints of the normal layers. They need no textures of their own.
- The bioluminescence glow mask is one texture per species. All skin sets of that species use it.

**Dinosaur sounds**
- Source the dinosaur sounds from Jurassic World Evolution 3.
- Shared sounds can serve as placeholders until then, but they count as Missing.
- One shared eat sound for all carnivores is acceptable.
- Each species gets its own hatchling sounds, not a higher-pitched adult voice.
- Yawn, sneeze and snort get their own sounds per species.

**Dinosaur clips**
- An action that borrows the clip of another action needs its own clip. Until the artist makes it, the borrowed clip is a Placeholder and counts as missing.
- Every species needs the T. rex card extras: startled, shake body, scratch, speak.
- Young animals play with clips that exist (run, raptor pounce). Play needs no new art.

## Summary

| Thing | Done | To check in game | Ready to import | Needs export or conversion | Placeholder or missing | Shared, to confirm | Only if the lead approves (not counted) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Tyrannosaurus | 39 | 3 | 3 | 8 | 28 | 2 | 0 |
| Velociraptor | 31 | 2 | 31 | 24 | 33 | 1 | 1 |
| Dilophosaurus | 34 | 0 | 22 | 5 | 73 | 3 | 0 |
| Triceratops | 34 | 4 | 34 | 6 | 29 | 2 | 0 |
| Brachiosaurus | 25 | 2 | 27 | 2 | 37 | 15 | 0 |
| Gallimimus | 30 | 0 | 23 | 1 | 36 | 11 | 0 |
| Dinosaur effects | 0 | 0 | 0 | 0 | 32 | 0 | 0 |
| Dinosaur hover box | 0 | 0 | 0 | 0 | 2 | 0 | 0 |
| Eggs and hatching | 1 | 0 | 4 | 5 | 18 | 0 | 0 |
| Fossil Processor | 9 | 1 | 0 | 0 | 8 | 3 | 0 |
| Sequencer | 8 | 4 | 1 | 0 | 15 | 1 | 0 |
| Egg Printer | 8 | 1 | 0 | 0 | 12 | 2 | 0 |
| Incubator | 8 | 6 | 1 | 0 | 12 | 2 | 0 |
| Machine shared parts | 0 | 1 | 1 | 0 | 5 | 0 | 0 |
| Lab items | 12 | 0 | 2 | 0 | 6 | 0 | 0 |
| Fossils and amber | 2 | 0 | 15 | 0 | 17 | 0 | 0 |
| Lab power | 1 | 0 | 1 | 1 | 8 | 0 | 0 |
| Park power | 0 | 0 | 3 | 2 | 7 | 0 | 0 |
| Fences | 6 | 0 | 7 | 0 | 9 | 0 | 0 |
| Feeders | 3 | 0 | 0 | 10 | 20 | 0 | 0 |
| Digsites and villagers | 0 | 0 | 1 | 0 | 7 | 0 | 0 |
| Vehicles | 1 | 0 | 2 | 5 | 19 | 0 | 0 |
| **Total (without guns)** | **252** | **24** | **178** | **69** | **433** | **42** | **1** |
| Guns (blocked) | 0 | 0 | 2 | 11 | 3 | 0 | 0 |

- **Counted rows:** 998 (guns not included).
- **Work left:** 704 rows (433 to make, 69 to export or convert, 178 to import, 24 to check in game), plus 42 shared files for the artist to confirm.
- **Done:** 252.
- **Not counted:** 1 row that waits on the lead, 31 rows with no art needed, 38 rows not in Beta (6 of them Release 1 groups).
- **Guns:** blocked on D08. They are not in the total.
- **Per-milestone counts:** the roadmap's milestone table. They come from the Feature column of each row (F1.x is M1, F2.x is M2, F3.x is M3, F4.x is M4), with guns left out.

**Changed since last review:** Vehicle assembly uses existing groups, so the chassis and engine rows are gone and the wheel split is a Done developer row. Large Generator gets its own ignite, fuel-out and loop rows. Release 1 asset groups listed, not counted. Question 21 settled.

## Questions for the artist and lead

The Decisions register in the Engineering backlog holds the answers to all other questions.

- Q1. **T. rex sounds (artist, D91).**
  - All 37 REPO T. rex sounds have the same bytes as the distortus_rex folder. Which folder holds the real T. rex recordings?
  - Until the artist answers, the 7 T. rex sound rows are Placeholders.
  - Also pick mod or REPO hurt_1 (they differ).
- Q4. **Shared layers (artist).** Can both genders use one file for eyes, teeth, nostrils, claws or toes, mouth and eyelids? The rows say "Shared, artist to confirm".
- Q5. **Possible layer gaps (artist).**
  - T. rex eyelids.
  - Velociraptor Sorna female claws and mouth (inside "small details"?).
  - Triceratops JP female eye sockets, Sorna female osteoderms, Sorna male eye sockets.
  - Brachiosaurus JP3 back.
  - Gallimimus JW male belly.
  - Dilophosaurus JPTG nostrils.
- Q7. **Velociraptor crest and quill variants (artist).** Is each variant one extra texture layer on the base set, or does it also need a model part? The rows assume one layer.
- Q9. **Egg models (artist).**
  - Species map of the REPO egg models: Brachiosaurus `round`, Tyrannosaurus `tyrannosaurus`, Triceratops `tall_3`, Gallimimus `normal`, Velociraptor and Dilophosaurus `velociraptor`.
  - Natural eggs get the colours of their species on these models.
  - A viewer of the REPO files found the four points below. The artist answers each point:
    - `egg_normal.png` fits the `tall_3` UV exactly, and `tall_3` has no texture. Is `egg_normal.png` the `tall_3` texture? Which texture does `normal` use?
    - `normal` and `velociraptor` have the same cubes, so the Gallimimus and raptor eggs have one shape. Is that intended?
    - `tall` and `tall_2` share one texture that covers 87 % of their UV. Beta does not use these models.
    - The QR-code (printed) textures are 2048 px and render black on the models. Can the artist export them at the model's texture size?
- Q10. **Sorna and JP3 models (artist).**
  - The Velociraptor Sorna set uses its own JP3 model and the Sorna clip list (21 clips).
  - The REPO holds 16 of the 21 clips. The Sorna clips use the same bone names as the mod clips.
  - Can both models share one clip file?
  - Does the Sorna model also need the other Beta clips (pounce, startled, nesting)?
  - Same check for the Brachiosaurus JP3 model.
- Q11. **Clips that do not fit the model (artist).** Some clips name bones that the model lacks: T. rex attack 2 and injured, Velociraptor swim, bite, death, turns, Triceratops swim and call, Gallimimus flinch. Re-export or retarget.
- Q12. **Upgrade looks (artist).**
  - Every upgrade changes the look of the machine, with no new parts.
  - The processor tank tiers use their bones (`base_tanks`, `iron_tanks`, `gold_tanks`, `diamond_tanks`).
  - The sequencer chips use the 4 chip cubes, which a developer moves into bones of their own.
  - The incubator bulbs use emissive textures on the lamp cap.
  - Every other upgrade gets a texture swap on an existing bone: the processor chips, the sequencer tanks, the egg printer chips and Leveling Sensor, and the incubator containers and plant tanks.
  - Which bone takes each swap?
- Q13. **Incubator arm (artist).** The claw is the current two-finger claw. The arm links are 6.5 px: does the reach cover 9 eggs?
- Q14. **Shared sounds (artist).**
  - Machine sounds follow the "Machine sounds" rule at the top of this page.
  - The machine rows marked "Shared, artist to confirm" keep that status until the artist's own sounds replace them.
  - One shared flesh rip and crunch eat sound serves all carnivores.
- Q19. **Pylon head on a fence pole (artist).**
  - The pylon head sits on top of the fence pole block.
  - The player sets any height only for a pole used as a pylon. A fence post keeps 3 or 8 blocks.
  - Does the REPO pylon head (`pylon_pole_head.tbl`) fit on the low and the high fence post?
  - The REPO pylon pole is not used.
- Q20. **Vehicle dye tint (artist).** Dye tints the colours of a vehicle. Which parts of each Jeep and Explorer texture take the tint?

---

## Tyrannosaurus

The large carnivore. It hunts alone or in pairs, roars before it attacks and can break an unpowered fence.
Milestone: Dinosaurs.

**To make (28)**

- Skins, JP set (Rexy female, Buck male) (3): Female: eyelids (closed eyes); Male: eyelids (closed eyes); Bioluminescence glow mask.
- Animations (8): Call 2; Roar 3; Guard nest; Hatch; Yawn; Sneeze; Startled (extra); Limp (placeholder).
- Sounds (17):
  - Missing: Contact call; Nest call; Mating call; Hatchling sounds; Drink; Sleep (snore or slow breath); Footsteps (heavy); Yawn; Sneeze; Snort.
  - Placeholder: Roar (10 files); Growl (10 files); Attack snarl (3 files); Bite hit (3 files); Breath (5 files); Hurt (3 files); Death (3 files).

**To import (3)**

- Sounds (1): Eat (flesh rip, crunch).
- Sprites (2): Raw meat; Cooked meat.

**To fix (13)**

- Skins, JP set (Rexy female, Buck male) (2): Male: teeth (confirm shared), nostrils (confirm shared).
- Animations (11):
  - Check in game: Swim; Eat 2; Attack 1 (bite).
  - Export: Call; Roar 2; Sleep; Sit (tame stay); Stalk (extra); Snarl (extra).
  - Convert: Attack 2; Injured.

**Elsewhere for this species:**
- Dinosaur effects (Missing): Bloody mouth overlay; Bloody claw overlay; Snow cover overlay.
- Eggs and hatching (Ready to import): Printed egg, `tyrannosaurus` model; Natural egg.
- Fossils and amber (Missing): Special piece, Tyrannosaurus tooth; Tyrannosaurus tooth fossil item; Tyrannosaurus tooth fresh drop.

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Adult model (male and female use it) | The animal | Done | F2.1 | MOD/geo/entity/tyrannosaurus_rex.geo.json. REPO dinosaurs/tyrannosaurus/models/tyrannosaurus_adult.bbmodel |
| Baby model | Young animals scale from the adult | Not in Beta | F2.1 | None |

### Skins and textures: JP set (Rexy female, Buck male)

Mod folder: MOD/textures/entity/tyrannosaurus_rex/<gender>/. REPO: dinosaurs/tyrannosaurus/textures/_skins_/<gender>/.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Female: eyelids (closed eyes) | Sleep and rest. Other T. rex sets have eyelids | Missing | F2.2 | Possible gap. The REPO Big Eatie and Little Eatie sets have eyelid layers |
| Male: eyelids (closed eyes) | Sleep and rest | Missing | F2.2 | Possible gap, as for the female |
| Bioluminescence glow mask | Glow-in-the-dark patterns from the glow squid gene (Special Genes) | Missing | F2.7 | None. One mask per species, for all skin sets |
| Male: teeth | Fixed layer | Shared, artist to confirm | F2.2 | Same bytes as female/teeth.png in the mod and in REPO |
| Male: nostrils | Fixed layer | Shared, artist to confirm | F2.2 | Same bytes as female/nostrils.png in the mod and in REPO |
| Female: base | Body colour layer | Done | F2.2 | female/base.png |
| Female: belly | Colour layer | Done | F2.2 | female/belly.png (own edit, differs from REPO) |
| Female: back | Colour layer | Done | F2.2 | female/back.png (own edit) |
| Female: pattern | Colour layer | Done | F2.2 | female/pattern.png (own edit) |
| Female: mouth | Fixed layer | Done | F2.2 | female/mouth.png |
| Female: teeth | Fixed layer | Done | F2.2 | female/teeth.png |
| Female: eyes | Fixed layer | Done | F2.2 | female/eyes.png |
| Female: nostrils | Fixed layer | Done | F2.2 | female/nostrils.png |
| Female: claws | Fixed layer | Done | F2.2 | female/claws.png |
| Male: base | Colour layer | Done | F2.2 | male/base.png |
| Male: belly | Colour layer | Done | F2.2 | male/belly.png |
| Male: back | Colour layer | Done | F2.2 | male/back.png |
| Male: pattern | Colour layer | Done | F2.2 | male/pattern.png |
| Male: yellow | Colour layer, male only | Done | F2.2 | male/yellow.png |
| Male: feet | Colour layer, male only | Done | F2.2 | male/feet.png |
| Male: mouth | Fixed layer | Done | F2.2 | male/mouth.png |
| Male: eyes | Fixed layer | Done | F2.2 | male/eyes.png |
| Male: claws | Fixed layer | Done | F2.2 | male/claws.png |
| JW-look set | Skin versions 1.0 JP/TLW, 2.0 JP3, 3.0 JW/FK, 4.0 JWE | Not in Beta | F2.2 | The Beta JW sets are Brachiosaurus, Gallimimus and Dilophosaurus |
| Big Eatie set (17 layers) | Named T. rex set with no gender split | Not in Beta | F2.2 | REPO _skins_/old/big_eatie and working PSD |
| Little Eatie set (16 layers) | Named T. rex set with no gender split | Not in Beta | F2.2 | REPO _skins_/old/little_eatie and working PSD |

### Animations

- **Mod clip file:** MOD/animations/entity/tyrannosaurus_rex.animation.json. Most clips name 6 bones that the model does not have. The rows that name many more are "To check in game".
- **REPO clip file ("rex file"):** dinosaurs/tyrannosaurus/animations/blockbench/rex.animations.json.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Call 2 | Second call | Missing | F2.3 | None in any file |
| Roar 3 | Threat variety | Missing | F2.3 | None |
| Guard nest | Adult guards the nest and young | Missing | F2.3 | None |
| Hatch | Young comes out of the egg | Missing | F2.3 | None |
| Yawn | Yawn effect needs the motion | Missing | F2.3 | None |
| Sneeze | Sneeze effect needs the motion | Missing | F2.3 | None |
| Startled (extra) | Stand-or-flee reaction. T. rex card extra, wanted for every species | Missing | F2.3 | None in the mod or the rex file |
| Limp | Low-health walk. Replaces the walk below 30 % health, a config value | Placeholder | F2.3 | Stand-in: the Injured clip. Each species needs a new limp clip |
| Swim | Water | To check in game | F2.3 | `swim`: 14 of 35 bones not in the model |
| Eat 2 | Hunger, variety | To check in game | F2.3 | `eat2`: 13 bones only. Check that the body moves |
| Attack 1 (bite) | Attack | To check in game | F2.3 | `attack1`: 16 bones, 8 not in the model |
| Call | Warn or gather the pair | Needs export | F2.3 | Rex file `dramaticcall` (bones match). Old: tyrannosaurus_adult_calling.dca |
| Roar 2 | Threat variety | Needs export | F2.3 | Rex file `roar2` (bones match) |
| Sleep | Sleep need | Needs export | F2.3 | Rex file `sleeping` (bones match). Old: tyrannosaurus_adult_sleeping.dca |
| Sit (tame stay) | Taming: stay | Needs export | F2.3 | Rex file `resting/sit` (bones match) |
| Stalk (extra) | Slow hunting approach | Needs export | F2.3 | REPO dinosaurs/tyrannosaurus/animations/tyrannosaurus.animation.json `stalk` |
| Snarl (extra) | Short warning | Needs export | F2.3 | Rex file `snarl` (3 bones) |
| Attack 2 | Attack | Needs conversion | F2.3 | Rex file `attack2`: 17 of 34 bones match the model |
| Injured | Low health | Needs conversion | F2.3 | Rex file `injured`: 36 of 75 bones match. Old: tyrannosaurus_adult_injured.dca |
| Walk | Moving | Done | F2.3 | `move.walk` |
| Run | Chase and flee | Done | F2.3 | `run` |
| Drink | Thirst | Done | F2.3 | `drink` |
| Eat 1 | Hunger, carcass | Done | F2.3 | `eat1` |
| Roar 1 (threat display) | Warning before attack | Done | F2.3 | `roar` (7.5 s) |
| Rest (lie down) | Rest need | Done | F2.3 | `rest` |
| Rest idle | Lying idle | Done | F2.3 | `restidle` |
| Get up | Stand after rest or sleep | Done | F2.3 | `getup` |
| Idle | Standing idle | Done | F2.3 | `misc.idle` (8 bones, small sway) |
| Look left and right | Detection | Done | F2.3 | `lookleft`, `lookright` (head only) |
| Sniff air | Detection | Done | F2.3 | `sniffair` |
| Sniff ground | Detection and idle. Scent trails are not in Beta | Done | F2.3 | `sniffground` |
| Shake head | Idle | Done | F2.3 | `shakehead` |
| Flinch | Hit reaction | Done | F2.3 | `flinch` |
| Death | Death and carcass | Done | F2.3 | `dead` |
| Turn left and right | Turn in place | Done | F2.3 | `turn_left`, `turn_right` |
| Lay eggs (nesting) | Female lays eggs | Done | F2.3 | `nesting` |
| Jump and fall (extra) | Jump gene | Done | F2.3 | `jump/fall` |
| Shake body, scratch, speak (extra) | Idle variety | Done | F2.3 | `shakebody`, `scratch`, `speak` |
| Tranquilised | Guns are blocked | Not in Beta | F4.3 | Rex file `tranqed` |

### Sounds

- **Mod folder:** MOD/sounds/entity/tyrannosaurus_rex/.
- **REPO sounds are copies:** all 37 REPO T. rex sounds have the same bytes as REPO dinosaurs/distortus_rex/sounds/. A copied file counts as missing. These rows stay Placeholders until the artist names the real T. rex folder (Q1, D91).

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Contact call | Warn or gather the pair | Missing | F2.3 | No call file in REPO |
| Nest call | Adults at the nest | Missing | F2.3 | None |
| Mating call | Pair before mating | Missing | F2.3 | None |
| Hatchling sounds | Young animals have their own voice, not a higher-pitched adult voice | Missing | F2.3 | None |
| Drink | Thirst | Missing | F2.3 | None |
| Sleep (snore or slow breath) | Sleep | Missing | F2.3 | None |
| Footsteps (heavy) | Walking and running | Missing | F2.3 | None |
| Yawn | Yawn effect, own sound per species | Missing | F2.3 | None |
| Sneeze | Sneeze effect, own sound per species | Missing | F2.3 | None |
| Snort | Snort effect, own sound per species | Missing | F2.3 | None |
| Roar (10 files) | Threat display | Placeholder | F2.3 | vocals/roar/roar_1..10.ogg: byte copy of distortus_rex |
| Growl (10 files) | Warning before attack | Placeholder | F2.3 | vocals/growl/growl_1..10.ogg: byte copy of distortus_rex |
| Attack snarl (3 files) | Attack | Placeholder | F2.3 | attack/snarl/: byte copy of distortus_rex |
| Bite hit (3 files) | Bite lands | Placeholder | F2.3 | attack/bite/: byte copy of distortus_rex |
| Breath (5 files) | Idle ambient | Placeholder | F2.3 | ambient/breath_1..5.ogg: byte copy of distortus_rex |
| Hurt (3 files) | Hit reaction | Placeholder | F2.3 | hurt/: byte copy of distortus_rex. Mod hurt_1 (60 KB) differs from REPO hurt_1 (172 KB) |
| Death (3 files) | Death | Placeholder | F2.3 | death/: byte copy of distortus_rex |
| Eat (flesh rip, crunch) | Eating a carcass | Ready to import | F2.3 | REPO dinosaurs/_common/sounds/flesh_rip_1.ogg, flesh_crunch_medium_1, 2. One set for all carnivores |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Raw meat | Drop from a dead animal | Ready to import | F2.1 | REPO sprites/meat/tyrannosaurus/ |
| Cooked meat | Cooked drop | Ready to import | F2.1 | REPO sprites/meat/tyrannosaurus/ |
| Species icon (egg and DNA) | Egg and tube show the species | Done | F1.1 | MOD/textures/item/dino_overlay/ (T. rex file) |

**Changed since last review:** Sleep is in Beta, so both genders need eyelids. Each species needs its own limp clip, below 30 % health. Startled is wanted for every species. JW-look, Big Eatie and Little Eatie sets are not in Beta.

---

## Velociraptor

The pack hunter. It calls to its pack, screeches before it attacks and pounces on prey.
Milestone: Dinosaurs.

**To make (33)**

- Skins, Nublar set (1): Bioluminescence glow mask.
- Skins, crest and quill variants (7): Nublar female with Sorna crest: crest layer; Nublar male with Sorna crest: crest layer; Nublar male with quills: quill layer; Nublar male with arm quills: arm-quill layer; Sorna female with Nublar crest: crest layer; Sorna male with Nublar crest: crest layer; Sorna male with arm quills: arm-quill layer.
- Skins, Sorna set (JP3) (2): Female: claws, mouth.
- Animations (11): Call 2; Call 3; Idle 2; Limp; Injured or flinch; Guard nest; Hatch; Yawn; Shake body (extra); Idle 1 (placeholder); Threat display (screech) (placeholder).
- Animations: Sorna model (JP3) (5): Call 2; Call 3; Idle 1; Idle 2; Limp.
- Sounds (6): Footsteps; Yawn; Sneeze; Nest call; Mating call; Hatchling sounds.
- Sprites (1): Species icon (egg and DNA).

**To import (31)**

- Skins, Sorna set (JP3) (17):
  - Female: base, belly, stripes, eye sockets, small details, eyes, eyelids.
  - Male: base, belly, red, stripe, eye sockets, quills, claws, mouth, eyes, eyelids.
- Sounds (12): Call (2 files); Screech (4 files) and hiss; Growl; Attack snarl and bite (4 files); Pounce charge (3 files); Laugh (2 files); Hurt (2 files); Death (4 files); Exhale and snort (4 files); Snore; Drink (2 files); Eat (flesh rip, crunch).
- Sprites (2): Raw meat; Cooked meat.

**To fix (27)**

- Models (1): Sorna adult model (JP3 look) (export).
- Skins, Nublar set (1): Male: eyelids (confirm shared).
- Animations (9):
  - Check in game: Attack 2 (claw strikes); Scratch, speak (extra).
  - Export: Swim; Attack (bite); Death; Sniff ground; Turn left and right.
  - Convert: Sit (tame stay); Get up (wake up).
- Animations: Sorna model (JP3) (16):
  - Export: Walk; Run; Jump; Drink; Eat; Attack 2 (claw strikes); Call; Sleep; Sniff air; Sniff ground; Look left and right; Head shake.
  - Convert: Swim; Attack (bite); Death; Sit (tame stay).

**Waits on the lead, not counted:** Crouch-then-leap.

**Elsewhere for this species:**
- Dinosaur effects (Missing): Bloody mouth overlay; Bloody claw overlay; Snow cover overlay.
- Eggs and hatching (Ready to import): Printed egg, `velociraptor` model; Natural egg.
- Fossils and amber: Skull (Ready to import); Special piece, Velociraptor toe claw (Missing); Velociraptor toe claw fossil item (Missing); Velociraptor toe claw fresh drop (Missing).
- Guns (blocked, list only): Tranquilised clip for Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus, Gallimimus (Missing).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Sorna adult model (JP3 look) | The Sorna set uses its own JP3 model | Needs export | F2.2 | REPO models/velociraptor_adult_JP3.bbmodel (28 clips inside) |
| Nublar adult model (JP and TLW look) | The animal | Done | F2.1 | MOD/geo/entity/velociraptor.geo.json. REPO models/velociraptor_adult_jptlw.bbmodel |
| Infant model | Young animals scale from the adult | Not in Beta | F2.1 | REPO models/tbl/velociraptor_infant_novel.tbl |

### Skins and textures: Nublar set

Mod folder: MOD/textures/entity/velociraptor/<gender>/. Today the renderer draws only `base` (a code task, not art).

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Bioluminescence glow mask | Glow-in-the-dark patterns from the glow squid gene (Special Genes) | Missing | F2.7 | None. One mask per species, for all skin sets |
| Male: eyelids | Fixed layer | Shared, artist to confirm | F2.2 | Same bytes as female eyelids in the mod and in REPO |
| Female: base layer | Body colour layer | Done | F2.2 | female/base_layer.png |
| Female: belly | Colour layer | Done | F2.2 | female/belly.png |
| Female: back | Colour layer, female only | Done | F2.2 | female/back.png |
| Female: stripes | Colour layer | Done | F2.2 | female/stripes.png |
| Female: mouth | Fixed layer | Done | F2.2 | female/mouth.png |
| Female: claws | Fixed layer | Done | F2.2 | female/claws.png |
| Female: eyes | Fixed layer | Done | F2.2 | female/eyes.png |
| Female: eyelids | Fixed layer, sleep | Done | F2.2 | female/eyelids.png |
| Male: base layer | Body colour layer | Done | F2.2 | male/base_layer.png |
| Male: belly | Colour layer | Done | F2.2 | male/belly.png |
| Male: head | Colour layer, male only | Done | F2.2 | male/head.png |
| Male: stripes | Colour layer | Done | F2.2 | male/stripes.png |
| Male: mouth | Fixed layer | Done | F2.2 | male/mouth.png |
| Male: claws | Fixed layer | Done | F2.2 | male/claws.png |
| Male: eyes | Fixed layer | Done | F2.2 | male/eyes.png |
| Named raptors Blue, Charlie, Delta, Echo | Film characters | Not in Beta | F2.2 | REPO textures/_skins/raptor_squad |
| JW-look set | Skin versions 1.0 JP/TLW, 2.0 JP3, 3.0 JW/FK, 4.0 JWE | Not in Beta | F2.2 | The Beta JW sets are Brachiosaurus, Gallimimus and Dilophosaurus |

### Skins and textures: crest and quill variants

- **In Beta:** the crest variants and both quill variants. Reference: Velociraptor.docx.
- **One layer each:** each variant is the base set plus one layer (Q7).
- **Male only:** "with quills" and "with arm quills" have no female row.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Nublar female with Sorna crest: crest layer | Crest variant | Missing | F2.2 | No crest file in any raptor folder |
| Nublar male with Sorna crest: crest layer | Crest variant | Missing | F2.2 | None |
| Nublar male with quills: quill layer | Quill variant | Missing | F2.2 | The Sorna male quills layer is for the Sorna set only |
| Nublar male with arm quills: arm-quill layer | Arm-quill variant | Missing | F2.2 | None anywhere |
| Sorna female with Nublar crest: crest layer | Crest variant | Missing | F2.2 | None |
| Sorna male with Nublar crest: crest layer | Crest variant | Missing | F2.2 | None |
| Sorna male with arm quills: arm-quill layer | Arm-quill variant | Missing | F2.2 | None |

### Skins and textures: Sorna set (JP3)

REPO folder: dinosaurs/velociraptor/textures/_skins/sorna/<Female or Male>/. None is in the mod.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Female: claws | Fixed layer | Missing | F2.2 | Possible gap: may sit inside "small details". Artist to check |
| Female: mouth | Fixed layer | Missing | F2.2 | Possible gap: may sit inside "small details". Artist to check |
| Female: base | Body colour layer | Ready to import | F2.2 | Female/velociraptor_adult_female_sorna_1_base.png |
| Female: belly | Colour layer | Ready to import | F2.2 | …_2_belly.png |
| Female: stripes | Colour layer | Ready to import | F2.2 | …_3_stripes.png |
| Female: eye sockets | Colour layer | Ready to import | F2.2 | …_4_eye-sockets.png |
| Female: small details | Colour layer | Ready to import | F2.2 | …_5_small-details.png |
| Female: eyes | Fixed layer | Ready to import | F2.2 | …_6_eyes.png |
| Female: eyelids | Fixed layer | Ready to import | F2.2 | …_7_eyelids.png |
| Male: base | Body colour layer | Ready to import | F2.2 | Male/velociraptor_adult_male_sorna_1_base.png |
| Male: belly | Colour layer | Ready to import | F2.2 | …_2_belly.png |
| Male: red | Colour layer, male only | Ready to import | F2.2 | …_3_red.png |
| Male: stripe | Colour layer | Ready to import | F2.2 | …_4_stripe.png |
| Male: eye sockets | Colour layer | Ready to import | F2.2 | …_5_eye-sockets.png |
| Male: quills | Male only | Ready to import | F2.2 | …_6_quills.png |
| Male: claws | Fixed layer | Ready to import | F2.2 | …_7_claws.png |
| Male: mouth | Fixed layer | Ready to import | F2.2 | …_8_mouth.png |
| Male: eyes | Fixed layer | Ready to import | F2.2 | …_9_eyes.png |
| Male: eyelids | Fixed layer | Ready to import | F2.2 | …_10_eyelids.png |

### Animations

- **Mod clip file:** MOD/animations/entity/velociraptor.animation.json.
- **REPO Nublar Blockbench file ("jptlw file"):** models/velociraptor_adult_jptlw.bbmodel. Its clips fit the mod model.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Call 2 | Second call | Missing | F2.3 | None |
| Call 3 | Third call | Missing | F2.3 | None |
| Idle 2 | Standing idle | Missing | F2.3 | None |
| Limp | Low-health walk. Replaces the walk below 30 % health, a config value | Missing | F2.3 | None. Each species needs a new limp clip |
| Injured or flinch | Hit reaction | Missing | F2.3 | No flinch or injured clip in any raptor file |
| Guard nest | Adult guards nest and young | Missing | F2.3 | None |
| Hatch | Young comes out of the egg | Missing | F2.3 | None |
| Yawn | Yawn effect needs the motion | Missing | F2.3 | None |
| Shake body (extra) | Idle variety. T. rex card extra, wanted for every species | Missing | F2.3 | None. The mod `shakebody` is the head shake (see Head shake) |
| Idle 1 | Standing idle | Placeholder | F2.3 | Stand-in: `misc.idle` is a copy of the `startled` clip |
| Threat display (screech) | Warning before attack. Needs its own clip, apart from the pack call | Placeholder | F2.3 | Stand-in: `roar`, the pack call clip |
| Attack 2 (claw strikes) | Attack | To check in game | F2.3 | `left_claw_down`, `right_claw_down` (1 bone each) |
| Scratch, speak (extra) | Idle variety | To check in game | F2.3 | `scratch` (11 of 41 bones not in the model), `speak` |
| Swim | Water | Needs export | F2.3 | Mod `swim`: 16 of 36 bones not in the model. Use jptlw file `swim` |
| Attack (bite) | Attack | Needs export | F2.3 | Mod `attack1`: 14 of 28 bones not in the model. Use jptlw file `attack1` |
| Death | Death | Needs export | F2.3 | Mod `dead`: 17 of 48 bones not in the model. Use jptlw file `death` |
| Sniff ground | Detection and idle. Scent trails are not in Beta | Needs export | F2.3 | REPO JP3 file `sniffground` (bones match) |
| Turn left and right | Turn in place | Needs export | F2.3 | Mod clips: 10 of 23 bones not in the model. Use jptlw file `turn_left`, `turn_right` |
| Sit (tame stay) | Taming: stay | Needs conversion | F2.3 | REPO animations/nublar/velociraptor_adult_nublar_sitting.dca |
| Get up (wake up) | Stand after sleep | Needs conversion | F2.3 | REPO animations/nublar/velociraptor_adult_nublar_wakingUp.dca |
| Walk | Moving | Done | F2.3 | `move.walk` |
| Run | Chase, pack hunt, flee | Done | F2.3 | `run` |
| Drink | Thirst | Done | F2.3 | `drink` (also `drink2`) |
| Eat | Hunger | Done | F2.3 | `eat1` |
| Crouch-then-leap | A crouch before the leap. In Beta, raptors leap with the `pounce` clip | Missing, only if the lead schedules it | F2.3 | None |
| Pounce | Pack hunt pounce | Done | F2.3 | `pounce`. REPO JP3 file also has `pounce2` |
| Call | Pack call | Done | F2.3 | `roar` (2.75 s). It is also the stand-in for the threat screech |
| Sleep | Sleep need | Done | F2.3 | `sleep` |
| Rest and rest idle | Rest need | Done | F2.3 | `rest`, `restidle` |
| Jump | Jump gene | Done | F2.3 | `jump/fall` |
| Sniff air | Detection | Done | F2.3 | `sniffair` |
| Look left and right | Detection | Done | F2.3 | `lookleft`, `lookright`, `lookaround` |
| Head shake | Idle | Done | F2.3 | `shakebody` (same as REPO `shakehead`) |
| Lay eggs (nesting) | Female lays eggs | Done | F2.3 | `nesting` |
| Sneeze | Sneeze effect | Done | F2.3 | `sneeze` |
| Startled (extra) | Stand-or-flee reaction | Done | F2.3 | `startled` |

### Animations: Sorna model (JP3)

- **Clip list:** the Sorna set uses its own JP3 model and the Sorna clip list. The list is the Nublar list plus head shake, 21 clips.
- **REPO file ("JP3 file"):** models/velociraptor_adult_JP3.bbmodel, 28 clips. Old: animations/sorna/ (18 .dca).
- **Check done:** a file check compared each clip with the bones of the JP3 model. Nobody tested these clips in game.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Call 2 | Second call | Missing | F2.3 | None in the JP3 file |
| Call 3 | Third call | Missing | F2.3 | None |
| Idle 1 | Standing idle | Missing | F2.3 | None. The JP3 clip `animation` has a length of 0 |
| Idle 2 | Standing idle | Missing | F2.3 | None |
| Limp | Low-health walk. Replaces the walk below 30 % health, a config value | Missing | F2.3 | None |
| Walk | Moving | Needs export | F2.3 | JP3 file `walk` (bones match) |
| Run | Chase, pack hunt, flee | Needs export | F2.3 | JP3 file `run` (bones match) |
| Jump | Jump gene | Needs export | F2.3 | JP3 file `jump/fall` (bones match) |
| Drink | Thirst | Needs export | F2.3 | JP3 file `drink`, `drink2` (bones match) |
| Eat | Hunger | Needs export | F2.3 | JP3 file `eat` (bones match) |
| Attack 2 (claw strikes) | Attack | Needs export | F2.3 | JP3 file `left_claw_down`, `right_claw_down` (1 bone each) |
| Call | Pack call | Needs export | F2.3 | JP3 file `call` (bones match) |
| Sleep | Sleep need | Needs export | F2.3 | JP3 file `sleep`, `sleep2` (bones match) |
| Sniff air | Detection | Needs export | F2.3 | JP3 file `sniffair` (bones match) |
| Sniff ground | Detection and idle. Scent trails are not in Beta | Needs export | F2.3 | JP3 file `sniffground` (bones match) |
| Look left and right | Detection | Needs export | F2.3 | JP3 file `lookleft`, `lookright`, `lookaround` (bones match) |
| Head shake | Idle. Only the Sorna list has it | Needs export | F2.3 | JP3 file `shakehead` (bones match) |
| Swim | Water | Needs conversion | F2.3 | JP3 file `swim`: 16 of 36 bones not in the JP3 model |
| Attack (bite) | Attack | Needs conversion | F2.3 | JP3 file `attack1`: 14 of 28 bones not in the JP3 model |
| Death | Death | Needs conversion | F2.3 | JP3 file `death`: 17 of 48 bones not in the JP3 model |
| Sit (tame stay) | Taming: stay | Needs conversion | F2.3 | REPO animations/sorna/velociraptor_adult_sorna_sitting.dca. The JP3 file `rest`, `rest2` can possibly serve: artist to check |

### Sounds

- **Mod:** none of these is in the mod. Today the threat display plays the T. rex roar for every species.
- **REPO folder:** dinosaurs/velociraptor/sounds/ (30 files, all raptor-only).

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Footsteps | Walking and running | Missing | F2.3 | None |
| Yawn | Yawn effect, own sound per species | Missing | F2.3 | None |
| Sneeze | Sneeze effect, own sound per species | Missing | F2.3 | None |
| Nest call | Adults at the nest | Missing | F2.3 | None |
| Mating call | Pair before mating | Missing | F2.3 | None |
| Hatchling sounds | Young animals have their own voice, not a higher-pitched adult voice | Missing | F2.3 | None |
| Call (2 files) | Pack contact call | Ready to import | F2.3 | call_1, 2.ogg |
| Screech (4 files) and hiss | Threat display. Its own sounds, apart from the pack call | Ready to import | F2.3 | screech_1..4.ogg, hiss_1.ogg |
| Growl | Warning | Ready to import | F2.3 | growl_1.ogg |
| Attack snarl and bite (4 files) | Attack | Ready to import | F2.3 | attack_snarl_1.ogg, bite_1..3.ogg |
| Pounce charge (3 files) | Pounce | Ready to import | F2.3 | charge_breath_1, 2.ogg, charge_prepare_1.ogg |
| Laugh (2 files) | Pack social call | Ready to import | F2.3 | laugh_1, 2.ogg |
| Hurt (2 files) | Hit reaction | Ready to import | F2.3 | hurt_1, 2.ogg |
| Death (4 files) | Death | Ready to import | F2.3 | death_1..4.ogg |
| Exhale and snort (4 files) | Idle ambient and the snort effect | Ready to import | F2.3 | exhale_1, 2.ogg, snort_1, 2.ogg |
| Snore | Sleep | Ready to import | F2.3 | snore_1.ogg |
| Drink (2 files) | Thirst | Ready to import | F2.3 | swallow_water_1, 2.ogg |
| Eat (flesh rip, crunch) | Hunger | Ready to import | F2.3 | REPO dinosaurs/_common/sounds/. One set for all carnivores |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Species icon (egg and DNA) | Egg and tube show the species | Missing | F1.1 | None in the mod or REPO dna_icons |
| Raw meat | Drop | Ready to import | F2.1 | REPO sprites/meat/velociraptor/ |
| Cooked meat | Cooked drop | Ready to import | F2.1 | REPO sprites/meat/velociraptor/ |

**Changed since last review:** The Sorna set uses its own JP3 model and clip list. Crest and quill variants are in Beta. The threat screech needs its own clip and sounds. Raptors leap with `pounce`. Named raptors and JW-look set are not in Beta.

---
## Dilophosaurus

The frilled carnivore. It opens its frill and hoots before it attacks, and it spits.
Milestone: Dinosaurs.

**To make (73)**

- Skins, JP set (14): Bioluminescence glow mask; Male: base (placeholder), back (placeholder), belly (placeholder), pattern (placeholder), red (placeholder), white outline (placeholder), frills (placeholder), frills green (placeholder), frills white (placeholder), frills red (placeholder), eyes (placeholder), claws (placeholder), frill open look (placeholder).
- Skins, JPTG set (JP3/Sorna group) (1): JPTG: nostrils.
- Skins, JWD set (JW) (30): JWD female: base, back, belly, pattern, red, white outline, frills, frills green, frills white, frills red, mouth, teeth, eyes, nostrils, claws; JWD male: base, back, belly, pattern, red, white outline, frills, frills green, frills white, frills red, mouth, teeth, eyes, nostrils, claws.
- Animations (15): Get up; Jump; Idle 2; Limp; Curious; Turn left and right; Guard nest; Hatch; Yawn; Sneeze; Startled (extra); Shake body (extra); Sleep (placeholder); Rest idle (placeholder); Idle 1 (placeholder).
- Sounds (11): Bite; Spit launch and spit hit; Drink; Sleep breath; Footsteps; Yawn; Sneeze; Snort; Nest call; Mating call; Hatchling sounds.
- Effects (2): Spit projectile blob; Spit splash on hit.

**To import (22)**

- Skins, JPTG set (JP3/Sorna group) (11): JPTG: base, back, belly, pattern, white outline, frill, frill pattern, mouth, teeth, eyes, claws.
- Sounds (7): Call (13 files); Hoot (10 files); Roar (3 files); Grunt; Hurt (2 files); Death (2 files); Eat (flesh rip, crunch).
- Effects (1): Spit particle.
- Sprites (3): Raw meat; Cooked meat; Species icon (egg and DNA).

**To fix (8)**

- Skins, JP set (3): Male: mouth (confirm shared), teeth (confirm shared), nostrils (confirm shared).
- Animations (5): Threat display (intimidate, open frill) (convert); Fold frill (convert); Roar (older) (convert); Roar (young) (convert); Sit (rest) (convert).

**Elsewhere for this species:**

- Bloody mouth overlay: Dilophosaurus (Missing, Dinosaur effects)
- Bloody claw overlay: Dilophosaurus (Missing, Dinosaur effects)
- Snow cover overlay: Dilophosaurus (Missing, Dinosaur effects)
- Printed egg: `velociraptor` model (Ready to import, Eggs and hatching)
- Natural egg: Dilophosaurus (Missing, Eggs and hatching)
- Skull: Dilophosaurus (Ready to import, Fossils and amber)
- Tranquilised clip: Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus, Gallimimus (Missing, Guns (blocked, list only))

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Adult model | The animal | Done | F2.1 | MOD/geo/entity/dilophosaurus.geo.json. REPO models/Dilophosaurus.bbmodel |

### Skins and textures: JP set

Mod folder: MOD/textures/entity/dilophosaurus/<gender>/. The mod male folder is a copy of the female folder (17 of 17 files).

The only male art is the older set REPO textures/old/male (13 layers, older numbering). Check that it fits the current UV map.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Bioluminescence glow mask | Glow-in-the-dark patterns from the glow squid gene (Special Genes) | Missing | F2.7 | None. One mask per species. All skin sets of the species use it |
| Male: base | Body colour layer | Placeholder | F2.2 | Stand-in: copy of female base. Start from REPO old/male/…male_JP_1_base.png |
| Male: back | Colour layer | Placeholder | F2.2 | Stand-in: female copy. REPO old/male …_2_back.png |
| Male: belly | Colour layer | Placeholder | F2.2 | Stand-in: female copy. REPO old/male …_3_belly.png |
| Male: pattern | Colour layer | Placeholder | F2.2 | Stand-in: female copy. REPO old/male …_5_pattern.png |
| Male: red | Colour layer | Placeholder | F2.2 | Stand-in: female copy. REPO old/male …_6_red.png |
| Male: white outline | Colour layer | Placeholder | F2.2 | Stand-in: female copy. REPO old/male …_7_whiteoutline.png |
| Male: frills | Frill layer | Placeholder | F2.2 | Stand-in: female copy. REPO old/male …_10_frill.png |
| Male: frills green | Frill colour | Placeholder | F2.2 | Stand-in: female copy. No male art anywhere |
| Male: frills white | Frill colour | Placeholder | F2.2 | Stand-in: female copy. No male art anywhere |
| Male: frills red | Frill colour | Placeholder | F2.2 | Stand-in: female copy. REPO old/male …_11_frillred.png |
| Male: eyes | Fixed layer | Placeholder | F2.2 | Stand-in: female copy. REPO old/male …_12_eyes.png is a male file |
| Male: claws | Fixed layer | Placeholder | F2.2 | Stand-in: female copy. REPO old/male …_9_claws.png |
| Male: frill open look | Threat display | Placeholder | F2.2 | Stand-in: copy of the female file |
| Male: mouth | Fixed layer | Shared, artist to confirm | F2.2 | Old male and old female mouths are the same bytes |
| Male: teeth | Fixed layer | Shared, artist to confirm | F2.2 | Old male and old female teeth are the same bytes |
| Male: nostrils | Fixed layer | Shared, artist to confirm | F2.2 | Female copy. No male nostrils in the older set |
| Female: base | Body colour layer | Done | F2.2 | female/base.png |
| Female: back | Colour layer | Done | F2.2 | female/back.png |
| Female: belly | Colour layer | Done | F2.2 | female/belly.png |
| Female: pattern | Colour layer | Done | F2.2 | female/pattern.png |
| Female: red | Colour layer | Done | F2.2 | female/red.png |
| Female: white outline | Colour layer | Done | F2.2 | female/whiteoutline.png |
| Female: frills | Frill layer | Done | F2.2 | female/frills.png |
| Female: frills green | Frill colour | Done | F2.2 | female/frillsgreen.png |
| Female: frills white | Frill colour | Done | F2.2 | female/frillswhite.png |
| Female: frills red | Frill colour | Done | F2.2 | female/frillsred.png |
| Female: mouth | Fixed layer | Done | F2.2 | female/mouth.png |
| Female: teeth | Fixed layer | Done | F2.2 | female/teeth.png |
| Female: eyes | Fixed layer | Done | F2.2 | female/eyes.png |
| Female: nostrils | Fixed layer | Done | F2.2 | female/nostrils.png |
| Female: claws | Fixed layer | Done | F2.2 | female/claws.png |
| Female: frill open look | Threat display | Done | F2.2 | dilophosaurus_frill_open.png (flattened file) |
| Damage overlays (cuts, scars) | Wound look | Not in Beta | F2.3 | REPO textures/_skins/damage |

### Skins and textures: JPTG set (JP3/Sorna group)

The JPTG set is in Beta. REPO folder: textures/_skins/jptg/ (11 layers).

- **One body for both genders.** The female scales larger than the male.
- **Male look.** The male uses the same layers with a fainter, less vibrant tint than the female (proposal, lead decides). So the male needs no layer files of its own.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| JPTG: nostrils | Fixed layer | Missing | F2.2 | Possible gap: the JP set has nostrils |
| JPTG: base | JPTG skin for both genders | Ready to import | F2.2 | REPO textures/_skins/jptg/dilophosaurus_adult_JPTG_1_base.png |
| JPTG: back | Colour layer | Ready to import | F2.2 | …_JPTG_N_back.png |
| JPTG: belly | Colour layer | Ready to import | F2.2 | …_belly.png |
| JPTG: pattern | Colour layer | Ready to import | F2.2 | …_pattern.png |
| JPTG: white outline | Colour layer | Ready to import | F2.2 | …_whiteoutline.png |
| JPTG: frill | Frill layer | Ready to import | F2.2 | …_frill.png |
| JPTG: frill pattern | Frill colour | Ready to import | F2.2 | …_frillpattern.png |
| JPTG: mouth | Fixed layer | Ready to import | F2.2 | …_mouth.png |
| JPTG: teeth | Fixed layer | Ready to import | F2.2 | …_teeth.png |
| JPTG: eyes | Fixed layer | Ready to import | F2.2 | …_eyes.png |
| JPTG: claws | Fixed layer | Ready to import | F2.2 | …_claws.png |
| JPTG male look | Fainter, less vibrant tint of the shared layers (proposal, lead decides) | No art needed | F2.2 | Code tints the shared layers. Code also sets the smaller male size |

### Skins and textures: JWD set (JW)

The Dilophosaurus JWD set is in Beta. REPO showcase/jwd has 2 preview images and no layers.

No layer list exists, so the rows copy the JP layer list. This is an assumption: the artist confirms the real list.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| JWD female: base | JW skin | Missing | F2.2 | REPO showcase/jwd previews only |
| JWD female: back | Colour layer | Missing | F2.2 | None |
| JWD female: belly | Colour layer | Missing | F2.2 | None |
| JWD female: pattern | Colour layer | Missing | F2.2 | None |
| JWD female: red | Colour layer | Missing | F2.2 | None |
| JWD female: white outline | Colour layer | Missing | F2.2 | None |
| JWD female: frills | Frill layer | Missing | F2.2 | None |
| JWD female: frills green | Frill colour | Missing | F2.2 | None |
| JWD female: frills white | Frill colour | Missing | F2.2 | None |
| JWD female: frills red | Frill colour | Missing | F2.2 | None |
| JWD female: mouth | Fixed layer | Missing | F2.2 | None |
| JWD female: teeth | Fixed layer | Missing | F2.2 | None |
| JWD female: eyes | Fixed layer | Missing | F2.2 | None |
| JWD female: nostrils | Fixed layer | Missing | F2.2 | None |
| JWD female: claws | Fixed layer | Missing | F2.2 | None |
| JWD male: base | JW skin | Missing | F2.2 | REPO showcase/jwd previews only |
| JWD male: back | Colour layer | Missing | F2.2 | None |
| JWD male: belly | Colour layer | Missing | F2.2 | None |
| JWD male: pattern | Colour layer | Missing | F2.2 | None |
| JWD male: red | Colour layer | Missing | F2.2 | None |
| JWD male: white outline | Colour layer | Missing | F2.2 | None |
| JWD male: frills | Frill layer | Missing | F2.2 | None |
| JWD male: frills green | Frill colour | Missing | F2.2 | None |
| JWD male: frills white | Frill colour | Missing | F2.2 | None |
| JWD male: frills red | Frill colour | Missing | F2.2 | None |
| JWD male: mouth | Fixed layer | Missing | F2.2 | None |
| JWD male: teeth | Fixed layer | Missing | F2.2 | None |
| JWD male: eyes | Fixed layer | Missing | F2.2 | None |
| JWD male: nostrils | Fixed layer | Missing | F2.2 | None |
| JWD male: claws | Fixed layer | Missing | F2.2 | None |

### Animations

Clip file: MOD/animations/entity/dilophosaurus.animation.json. The REPO model holds the same clips. Its `rest` and `sleep` are empty too.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Get up | Stand after rest | Missing | F2.3 | None |
| Jump | Jump gene | Missing | F2.3 | None |
| Idle 2 | Standing idle | Missing | F2.3 | None |
| Limp | Low-health walk. It replaces the walk below 30 % health, a config value | Missing | F2.3 | None. Each species needs a new limp clip |
| Curious | Head tilt toward something new | Missing | F2.3 | None |
| Turn left and right | Turn in place | Missing | F2.3 | None |
| Guard nest | Adult guards nest and young | Missing | F2.3 | None |
| Hatch | Young comes out of the egg | Missing | F2.3 | None |
| Yawn | Yawn effect needs the motion | Missing | F2.3 | None |
| Sneeze | Sneeze effect needs the motion | Missing | F2.3 | None |
| Startled (extra) | Stand-or-flee reaction. Wanted for every species | Missing | F2.3 | None |
| Shake body (extra) | Idle variety. Wanted for every species | Missing | F2.3 | None |
| Sleep | Sleep need | Placeholder | F2.3 | Stand-in: `sleep` is an empty clip (0 bones) |
| Rest idle | Lying idle | Placeholder | F2.3 | Stand-in: `restidle` is an empty clip |
| Idle 1 | Standing idle | Placeholder | F2.3 | Stand-in: `misc.idle` is an empty clip |
| Threat display (intimidate, open frill) | Warning before attack: the intimidate clip. If the player stays, the Dilophosaurus spits | Needs conversion | F2.3 | REPO dinosaurs/dilophosaurus/animations/intimidate.dca |
| Fold frill | Close the frill after the display | Needs conversion | F2.3 | REPO animations/fold_frills.dca |
| Roar (older) | Roar clip of its own | Needs conversion | F2.3 | REPO animations/roar_older.dca |
| Roar (young) | Roar clip of its own | Needs conversion | F2.3 | REPO animations/roar_young.dca |
| Sit (rest) | Rest need | Needs conversion | F2.3 | Mod `rest` is empty. REPO animations/old/dilophosaurus_adult_resting.dca (older rig: check fit) |
| Walk | Moving | Done | F2.3 | `move.walk` |
| Run | Chase and flee | Done | F2.3 | `run` |
| Swim | Water | Done | F2.3 | `swim` |
| Drink | Thirst | Done | F2.3 | `drink` |
| Eat | Hunger | Done | F2.3 | `eat1` |
| Attack 1 (bite) | Attack | Done | F2.3 | `attack1` (11 bones) |
| Attack 2 (spit) | Ranged attack | Done | F2.3 | `spit` |
| Call | Warn or gather | Done | F2.3 | `call` |
| Call 2 | Second call | Done | F2.3 | `call2` |
| Speak | Short vocal | Done | F2.3 | `speak`, `speak2` |
| Flinch | Hit reaction | Done | F2.3 | `flinch` |
| Death | Death | Done | F2.3 | `dead` |
| Sniff air | Detection | Done | F2.3 | `sniffair` |
| Sniff ground | Detection and idle. Scent trails are not in Beta | Done | F2.3 | `sniffground` |
| Look left and right | Detection | Done | F2.3 | `lookleft`, `lookright` (4 bones, head only) |
| Head shake | Idle | Done | F2.3 | `head_shake` |
| Scratch (extra) | Idle variety | Done | F2.3 | `scratch` |
| Lay eggs (nesting) | Female builds a nest and lays eggs. This species plays an idle clip | No art needed | F2.3 | Uses an idle clip |

### Sounds

None is in the mod. REPO folder: dinosaurs/dilophosaurus/sounds/ (31 files).

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Bite | Bite attack | Missing | F2.3 | None |
| Spit launch and spit hit | Ranged attack | Missing | F2.3 | None |
| Drink | Thirst | Missing | F2.3 | None |
| Sleep breath | Sleep | Missing | F2.3 | None |
| Footsteps | Walking and running | Missing | F2.3 | None |
| Yawn | Yawn effect, own sound per species | Missing | F2.3 | None |
| Sneeze | Sneeze effect, own sound per species | Missing | F2.3 | None |
| Snort | Snort effect, own sound per species | Missing | F2.3 | None |
| Nest call | Adults at the nest | Missing | F2.3 | None |
| Mating call | Pair before mating | Missing | F2.3 | None |
| Hatchling sounds | Young animals have their own voice, not a higher-pitched adult voice | Missing | F2.3 | None |
| Call (13 files) | Contact and warning calls | Ready to import | F2.3 | call_1..13.ogg (call_12 and call_13 are the same bytes) |
| Hoot (10 files) | Frill threat display | Ready to import | F2.3 | hoop_1..9, hoop_11.ogg |
| Roar (3 files) | Threat | Ready to import | F2.3 | roar_1..3.ogg |
| Grunt | Idle | Ready to import | F2.3 | grunt_1.ogg |
| Hurt (2 files) | Hit reaction | Ready to import | F2.3 | hurt_1, 2.ogg |
| Death (2 files) | Death | Ready to import | F2.3 | death_1, 2.ogg |
| Eat (flesh rip, crunch) | Hunger | Ready to import | F2.3 | REPO dinosaurs/_common/sounds/. One set for all carnivores |

### Effects

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Spit projectile blob | Physics-based projectile blob | Missing | F2.3 | None |
| Spit splash on hit | Splash particles | Missing | F2.3 | None |
| Spit particle | Spit attack | Ready to import | F2.3 | REPO dinosaurs/dilophosaurus/textures/spitparticles.png |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Raw meat | Drop | Ready to import | F2.1 | REPO sprites/meat/dilophosaurus/ |
| Cooked meat | Cooked drop | Ready to import | F2.1 | REPO sprites/meat/dilophosaurus/ |
| Species icon (egg and DNA) | Egg and tube show the species | Ready to import | F1.1 | REPO dna_icons (Dilophosaurus). Not in the mod |

---

## Triceratops

The horned herbivore. It grazes low plants, lives in herds and charges a threat instead of running.
Milestone: Dinosaurs.

**To make (29)**

- Skins, Nublar set (JP) (2): Female: eye sockets; Bioluminescence glow mask.
- Skins, Sorna set (JP3) and horns (2): Female: osteoderms; Male: eye sockets.
- Animations (15): Jump; Idle 2; Turn left and right; Guard nest; Hatch; Yawn; Sneeze; Startled (extra); Shake body (extra); Scratch (extra); Rest idle (placeholder); Get up (placeholder); Sleep (placeholder); Sit (placeholder); Limp (placeholder).
- Sounds (10): Eat (plant chew); Drink; Sleep breath; Footsteps (heavy); Nest call; Mating call; Yawn; Sneeze; Hatchling sounds; Hurt 1 (placeholder).

**To import (34)**

- Skins, Sorna set (JP3) and horns (24): Female: base, belly, mid-tone, dark green, purple, feet, toes, mouth, snout, eye sockets, nostrils, eyes, eyelids; Male: base, belly, mid-tone, dark green, feet, white, osteoderms, mouth, nostrils, eyelids; Horn colour variants (28 layers).
- Sounds (7): Call (3) and social (4); Small roar (2) and domination display (2); Growl (7), hiss, snort, exhale; Charge wind-up; Attack and bite; Hurt 2; Death (3 files).
- Sprites (3): Raw meat; Cooked meat; Species icon (egg and DNA).

**To fix (12)**

- Skins, Sorna set (JP3) and horns (2): Male: toes (confirm shared), eyes (confirm shared).
- Animations (10): Attack 1 (horn) (check in game); Attack 2 (check in game); Sniff air (check in game); Look left and right (check in game); Eat 2 (extra) (export); Drink 2 (extra) (export); Call 2 (export); Swim (convert); Call (convert); Idle 1 (convert).

**Elsewhere for this species:**

- Snow cover overlay: Triceratops (Missing, Dinosaur effects)
- Printed egg: `tall_3` model (Missing, Eggs and hatching)
- Natural egg: Triceratops (Missing, Eggs and hatching)
- Skull: Triceratops (Ready to import, Fossils and amber)
- Special piece: Triceratops horn (Missing, Fossils and amber)
- Triceratops horn: fossil item (Missing, Fossils and amber)
- Triceratops horn: fresh drop (Missing, Fossils and amber)
- Triceratops teeth: fresh drop (Missing, Fossils and amber)
- Tranquilised clip: Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus, Gallimimus (Missing, Guns (blocked, list only))

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Adult model | The animal | Done | F2.1 | MOD/geo/entity/triceratops.geo.json. REPO models/Triceratops_25_04.bbmodel |

### Skins and textures: Nublar set (JP)

Mod folder: MOD/textures/entity/triceratops/<gender>/. Today the renderer draws only `base` (a code task, not art).

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Female: eye sockets | Colour layer | Missing | F2.2 | Possible gap: the male and the Sorna female have one |
| Bioluminescence glow mask | Glow-in-the-dark patterns from the glow squid gene (Special Genes) | Missing | F2.7 | None. One mask per species. All skin sets of the species use it |
| Female: base | Body colour layer | Done | F2.2 | female/base.png (own edit) |
| Female: belly | Colour layer, female only | Done | F2.2 | female/belly.png |
| Female: osteoderms | Colour layer, female only | Done | F2.2 | female/osteoderms.png |
| Female: toes | Fixed layer | Done | F2.2 | female/toes.png |
| Female: mouth | Fixed layer | Done | F2.2 | female/mouth.png |
| Female: nostrils | Fixed layer | Done | F2.2 | female/nostrils.png |
| Female: eyes | Fixed layer | Done | F2.2 | female/eyes.png |
| Female: eyelids | Fixed layer | Done | F2.2 | female/eyelids.png |
| Male: base | Body colour layer | Done | F2.2 | male/base.png (own edit) |
| Male: mid-tone | Colour layer | Done | F2.2 | male/midtone.png |
| Male: darker colour | Colour layer | Done | F2.2 | male/darkercolor.png |
| Male: diamond pattern | Colour layer | Done | F2.2 | male/pattern.png |
| Male: head details | Colour layer | Done | F2.2 | male/head.png |
| Male: brown | Colour layer | Done | F2.2 | male/brown.png |
| Male: green spots | Colour layer | Done | F2.2 | male/spots.png |
| Male: toes | Fixed layer | Done | F2.2 | male/toes.png |
| Male: mouth | Fixed layer | Done | F2.2 | male/mouth.png |
| Male: eye sockets | Colour layer | Done | F2.2 | male/eyesockets.png |
| Male: nostrils | Fixed layer | Done | F2.2 | male/nostrils.png |
| Male: eyes | Fixed layer | Done | F2.2 | male/eyes.png |
| Male: eyelids | Fixed layer | Done | F2.2 | male/eyelids.png |
| JW-look set | Skin versions 1.0 JP/TLW, 2.0 JP3, 3.0 JW/FK, 4.0 JWE | Not in Beta | F2.2 | The Beta JW sets are Brachiosaurus, Gallimimus and Dilophosaurus |

### Skins and textures: Sorna set (JP3) and horns

REPO folders: textures/_skins/jp3_female/ and jp3_male/. None is in the mod.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Female: osteoderms | Colour layer | Missing | F2.2 | Possible gap: the Nublar female and the Sorna male have one |
| Male: eye sockets | Colour layer | Missing | F2.2 | Possible gap: the Sorna female has one |
| Female: base | Body colour layer | Ready to import | F2.2 | jp3_female/triceratops_adult_female_sorna_1_base.png |
| Female: belly | Colour layer | Ready to import | F2.2 | …_2_belly.png |
| Female: mid-tone | Colour layer | Ready to import | F2.2 | …_3_midtone.png |
| Female: dark green | Colour layer | Ready to import | F2.2 | …_4_dark-green.png |
| Female: purple | Colour layer, female only | Ready to import | F2.2 | …_5_purple.png |
| Female: feet | Colour layer | Ready to import | F2.2 | …_6_feet.png |
| Female: toes | Fixed layer | Ready to import | F2.2 | …_7_toes.png |
| Female: mouth | Fixed layer | Ready to import | F2.2 | …_8_mouth.png |
| Female: snout | Female only | Ready to import | F2.2 | …_9_snout.png |
| Female: eye sockets | Colour layer | Ready to import | F2.2 | …_10_eye-sockets.png |
| Female: nostrils | Fixed layer | Ready to import | F2.2 | …_11_nostrils.png |
| Female: eyes | Fixed layer | Ready to import | F2.2 | …_12_eyes.png |
| Female: eyelids | Fixed layer | Ready to import | F2.2 | …_13_eyeyelids.png (file name typo) |
| Male: base | Body colour layer | Ready to import | F2.2 | jp3_male/…male_sorna_1_base.png |
| Male: belly | Colour layer | Ready to import | F2.2 | …_2_belly.png |
| Male: mid-tone | Colour layer | Ready to import | F2.2 | …_3_mid-tone.png |
| Male: dark green | Colour layer | Ready to import | F2.2 | …_4_dark-green.png |
| Male: feet | Colour layer | Ready to import | F2.2 | …_5_feet.png |
| Male: white | Colour layer, male only | Ready to import | F2.2 | …_6_white.png |
| Male: osteoderms | Colour layer | Ready to import | F2.2 | …_7_osteoderms.png |
| Male: mouth | Fixed layer | Ready to import | F2.2 | …_8_mouth.png |
| Male: nostrils | Fixed layer | Ready to import | F2.2 | …_9_nostrils.png |
| Male: eyelids | Fixed layer | Ready to import | F2.2 | …_12_eyelids.png |
| Horn colour variants (28 layers) | Horn colour (editable layer) | Ready to import | F2.2 | REPO textures/_skins/horns (Black, Black/Yellow, Brown, Grey) |
| Male: toes | Fixed layer | Shared, artist to confirm | F2.2 | …_10_toes.png, same bytes as the female toes |
| Male: eyes | Fixed layer | Shared, artist to confirm | F2.2 | …_11_eyes.png, same bytes as the female eyes |

### Animations

Clip file: MOD/animations/entity/triceratops.animation.json. REPO file: models/Triceratops_25_04.bbmodel ("REPO model").

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Jump | Jump gene | Missing | F2.3 | None |
| Idle 2 | Standing idle | Missing | F2.3 | None |
| Turn left and right | Turn in place | Missing | F2.3 | None |
| Guard nest | Adult guards nest and young | Missing | F2.3 | None |
| Hatch | Young comes out of the egg | Missing | F2.3 | None |
| Yawn | Yawn effect needs the motion | Missing | F2.3 | None |
| Sneeze | Sneeze effect needs the motion | Missing | F2.3 | None |
| Startled (extra) | Stand-or-flee reaction. Wanted for every species | Missing | F2.3 | None |
| Shake body (extra) | Idle variety. Wanted for every species | Missing | F2.3 | None |
| Scratch (extra) | Idle variety. Wanted for every species | Missing | F2.3 | None |
| Rest idle | Lying idle | Placeholder | F2.3 | Stand-in: `restidle` is an empty clip |
| Get up | Stand after rest | Placeholder | F2.3 | Stand-in: `getup` is an empty clip |
| Sleep | Sleep need | Placeholder | F2.3 | Stand-in: the rest clip. No own sleep pose |
| Sit | Sit and tame stay. The rest clip cannot serve it | Placeholder | F2.3 | Stand-in: `rest` |
| Limp | Low-health walk. It replaces the walk below 30 % health, a config value | Placeholder | F2.3 | Stand-in: `injured`. Each species needs a new limp clip |
| Attack 1 (horn) | Attack | To check in game | F2.3 | `attack1` (2 bones) |
| Attack 2 | Attack | To check in game | F2.3 | `attack2` (3 bones) |
| Sniff air | Detection | To check in game | F2.3 | `sniffair` (3 bones) |
| Look left and right | Detection | To check in game | F2.3 | `lookleft`, `lookright` (2 bones) |
| Eat 2 (extra) | Hunger variety | Needs export | F2.3 | REPO model `eat2` (bones match) |
| Drink 2 (extra) | Thirst variety | Needs export | F2.3 | REPO model `drink2` (bones match) |
| Call 2 | Second call | Needs export | F2.3 | REPO model `call2` (5 bones: check after export) |
| Swim | Water | Needs conversion | F2.3 | `swim`: 18 of 34 bones not in the model (REPO model is the same) |
| Call | Herd call | Needs conversion | F2.3 | `call`: 21 of 27 bones not in the model (REPO model is the same) |
| Idle 1 | Standing idle | Needs conversion | F2.3 | Mod `misc.idle` is empty. REPO models/triceratops_blockbench.bbmodel `weirdidlething`: 8 of 17 bones match |
| Walk | Moving | Done | F2.3 | `move.walk` |
| Run | Charge and flee | Done | F2.3 | `Sprint ` (the name ends with a space) |
| Drink | Thirst | Done | F2.3 | `drink` (15 bones) |
| Graze low plants (eat) | Eats low plants | Done | F2.3 | `eat1` (12 bones) |
| Ram wind-up and ram | Charge attack: Triceratops attacks, does not flee | Done | F2.3 | `ramwindup`, `ram` |
| Threat display (roar) | Warning before attack | Done | F2.3 | `roar` |
| Rest (lie down) | Rest need | Done | F2.3 | `rest` (REPO `rest/sleep`) |
| Injured | Hit reaction | Done | F2.3 | `injured` |
| Death | Death | Done | F2.3 | `dead` |
| Sniff ground | Detection and idle. Scent trails are not in Beta | Done | F2.3 | `sniffground` |
| Head shake | Idle | Done | F2.3 | `headshake` (4 bones) |
| Speak (extra) | Short vocal | Done | F2.3 | `speak` |
| Lay eggs (nesting) | Female builds a nest and lays eggs. This species plays an idle clip | No art needed | F2.3 | Uses an idle clip |

### Sounds

None is in the mod. REPO folder: dinosaurs/triceratops/sounds/ (32 files).

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Eat (plant chew) | Grazing | Missing | F2.3 | None. The shared flesh sounds do not fit a herbivore |
| Drink | Thirst | Missing | F2.3 | None |
| Sleep breath | Sleep | Missing | F2.3 | None |
| Footsteps (heavy) | Walking and running | Missing | F2.3 | None |
| Nest call | Adults at the nest | Missing | F2.3 | None |
| Mating call | Pair before mating | Missing | F2.3 | None |
| Yawn | Yawn effect, own sound per species | Missing | F2.3 | None |
| Sneeze | Sneeze effect, own sound per species | Missing | F2.3 | None |
| Hatchling sounds | Young animals have their own voice, not a higher-pitched adult voice | Missing | F2.3 | None |
| Hurt 1 | Hit reaction | Placeholder | F2.3 | Stand-in: hurt_1.ogg is a copy of death_1.ogg |
| Call (3) and social (4) | Herd contact | Ready to import | F2.3 | call_1..3.ogg, social_1..4.ogg |
| Small roar (2) and domination display (2) | Threat display and rank contest | Ready to import | F2.3 | small_roar.ogg, small_roar_1.ogg, domination_display_1, 2.ogg |
| Growl (7), hiss, snort, exhale | Warning, idle and the snort effect | Ready to import | F2.3 | growl_1..7.ogg, hiss_1, snort_1, exhale_1 |
| Charge wind-up | Ram attack | Ready to import | F2.3 | pre_charge.ogg |
| Attack and bite | Attack | Ready to import | F2.3 | attack_2.ogg, attack_1.ogg, bite_1.ogg. attack_1, attack_snarl_1 and attack_snarl_2 are one sound with 3 names |
| Hurt 2 | Hit reaction | Ready to import | F2.3 | hurt_2.ogg |
| Death (3 files) | Death | Ready to import | F2.3 | death_1..3.ogg |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Raw meat | Drop | Ready to import | F2.1 | REPO sprites/meat/triceratops/ |
| Cooked meat | Cooked drop | Ready to import | F2.1 | REPO sprites/meat/triceratops/ |
| Species icon (egg and DNA) | Egg and tube show the species | Ready to import | F1.1 | REPO dna_icons (Triceratops). The mod file is a copy of the T. rex icon |

---

## Brachiosaurus

The tall sauropod. It eats high leaves, stays in open ground and calls over long distances.
Milestone: Dinosaurs.

**To make (37)**

- Skins, JP and JP3 sets (6): JP3 female: back; JP3 male: back; Bioluminescence glow mask; JP male: base (placeholder), belly (placeholder), back (placeholder).
- Skins, JW set (4): JW female: base, belly, back, blue.
- Animations (17): Stand on hind legs to eat; Call 2; Jump; Limp; Turn left and right; Guard nest; Hatch; Yawn; Sneeze; Startled (extra); Shake body (extra); Scratch (extra); Rest idle (placeholder); Death (placeholder); Rest (lie down) (placeholder); Sit (placeholder); Sleep (placeholder).
- Sounds (10): Eat (leaf rip, chew); Footsteps and stomp; Tail whip; Sleep breath; Yawn; Sneeze; Snort; Nest call; Mating call; Hatchling sounds.

**To import (27)**

- Skins, JP and JP3 sets (11): JP3 female: base, red, belly, stripes, eyes, mouth, nostrils, toes; JP3 male: base, red, stripes.
- Skins, JW set (6): JW male: base, belly, back, blue, eyes, nostrils.
- Sounds (7): Call (3) and broadcast (6); Roar (5) and bellow (4); Growl (3); Attack snarl (3); Hurt (2); Death (2); Drink (2).
- Sprites (3): Raw meat; Cooked meat; Species icon (egg and DNA).

**To fix (19)**

- Models (1): Adult model, JP3 look (export).
- Skins, JP and JP3 sets (9): JP male: eyes (confirm shared), mouth (confirm shared), nostrils (confirm shared), toes (confirm shared); JP3 male: belly (confirm shared), eyes (confirm shared), mouth (confirm shared), nostrils (confirm shared), toes (confirm shared).
- Skins, JW set (6): JW male: mouth (confirm shared), toes (confirm shared); JW female: eyes (confirm shared), mouth (confirm shared), nostrils (confirm shared), toes (confirm shared).
- Animations (3): Swim (check in game); Eat (second clip) (check in game); JP3 model clip set (export).

**Elsewhere for this species:**

- Snow cover overlay: Brachiosaurus (Missing, Dinosaur effects)
- Printed egg: `round` model (Missing, Eggs and hatching)
- Natural egg: Brachiosaurus (Missing, Eggs and hatching)
- Skull: Brachiosaurus (Ready to import, Fossils and amber)
- Tranquilised clip: Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus, Gallimimus (Missing, Guns (blocked, list only))

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Adult model, JP3 look | JP3 set uses its own model | Needs export | F2.2 | REPO models/brachiosaurus_adult_JP3.bbmodel (24 clips inside) |
| Adult model, JP1 look | The animal | Done | F2.1 | MOD/geo/entity/brachiosaurus.geo.json. REPO models/brachiosaurus_bb.bbmodel |

### Skins and textures: JP and JP3 sets

Mod folder: MOD/textures/entity/brachiosaurus/<gender>/. The mod male folder is a copy of the female folder. REPO JP3 folders: textures/_skins/jp3_female/, jp3_male/.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| JP3 female: back | Colour layer | Missing | F2.2 | Possible gap: JP and JW sets have a back layer |
| JP3 male: back | Colour layer | Missing | F2.2 | Possible gap, as for the female |
| Bioluminescence glow mask | Glow-in-the-dark patterns from the glow squid gene (Special Genes) | Missing | F2.7 | None. One mask per species. All skin sets of the species use it |
| JP male: base | Body colour layer | Placeholder | F2.2 | Stand-in: female copy. Start from REPO old/Male/[PN] Brachiosaurus_jp1_adult_male_base.png |
| JP male: belly | Colour layer | Placeholder | F2.2 | Stand-in: female copy. REPO old/Male …male_belly.png |
| JP male: back | Colour layer | Placeholder | F2.2 | Stand-in: female copy. REPO old/Male …male_back.png |
| JP3 female: base | Body colour layer | Ready to import | F2.2 | jp3_female/brachiosaurus_adult_female_JP3_1_base.png |
| JP3 female: red | Colour layer | Ready to import | F2.2 | …_2_red.png |
| JP3 female: belly | Colour layer | Ready to import | F2.2 | …_3_belly.png |
| JP3 female: stripes | Colour layer | Ready to import | F2.2 | …_4_stripes.png |
| JP3 female: eyes | Fixed layer | Ready to import | F2.2 | …_5_eyes.png |
| JP3 female: mouth | Fixed layer | Ready to import | F2.2 | …_6_mouth.png |
| JP3 female: nostrils | Fixed layer | Ready to import | F2.2 | …_7_nostrils.png |
| JP3 female: toes | Fixed layer | Ready to import | F2.2 | …_8_toes.png |
| JP3 male: base | Body colour layer | Ready to import | F2.2 | jp3_male/…male_JP3_1_base.png |
| JP3 male: red | Colour layer | Ready to import | F2.2 | …_2_red.png |
| JP3 male: stripes | Colour layer | Ready to import | F2.2 | …_4_stripes.png |
| JP male: eyes | Fixed layer | Shared, artist to confirm | F2.2 | Female copy. No male eyes file anywhere |
| JP male: mouth | Fixed layer | Shared, artist to confirm | F2.2 | Female copy. The JW male mouth is the same bytes |
| JP male: nostrils | Fixed layer | Shared, artist to confirm | F2.2 | Female copy |
| JP male: toes | Fixed layer | Shared, artist to confirm | F2.2 | Female copy. The JW male toes are the same bytes |
| JP3 male: belly | Colour layer | Shared, artist to confirm | F2.2 | …_3_belly.png, same bytes as the female |
| JP3 male: eyes | Fixed layer | Shared, artist to confirm | F2.2 | Same bytes as the female |
| JP3 male: mouth | Fixed layer | Shared, artist to confirm | F2.2 | Same bytes as the female |
| JP3 male: nostrils | Fixed layer | Shared, artist to confirm | F2.2 | Same bytes as the female |
| JP3 male: toes | Fixed layer | Shared, artist to confirm | F2.2 | Same bytes as the female |
| JP female: base | Body colour layer | Done | F2.2 | female/base.png |
| JP female: belly | Colour layer | Done | F2.2 | female/belly.png |
| JP female: back | Colour layer | Done | F2.2 | female/back.png |
| JP female: eyes | Fixed layer | Done | F2.2 | female/eyes.png |
| JP female: mouth | Fixed layer | Done | F2.2 | female/mouth.png |
| JP female: nostrils | Fixed layer | Done | F2.2 | female/nostrils.png |
| JP female: toes | Fixed layer | Done | F2.2 | female/toes.png |

### Skins and textures: JW set

JW sets are in Beta. REPO folder: textures/_skins/jw_male/ (male only). No JW female art exists.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| JW female: base | JW skin | Missing | F2.2 | None |
| JW female: belly | Colour layer | Missing | F2.2 | None |
| JW female: back | Colour layer | Missing | F2.2 | None |
| JW female: blue | Colour layer | Missing | F2.2 | None |
| JW male: base | JW skin | Ready to import | F2.2 | jw_male/brachiosaurus_adult_male_JW_1_base.png |
| JW male: belly | Colour layer | Ready to import | F2.2 | …_2_belly.png |
| JW male: back | Colour layer | Ready to import | F2.2 | …_3_back.png |
| JW male: blue | Colour layer | Ready to import | F2.2 | …_4_blue.png |
| JW male: eyes | Fixed layer | Ready to import | F2.2 | …_5_eyes.png |
| JW male: nostrils | Fixed layer | Ready to import | F2.2 | …_7_nostrils.png |
| JW male: mouth | Fixed layer | Shared, artist to confirm | F2.2 | …_6_mouth.png, same bytes as the JP female mouth |
| JW male: toes | Fixed layer | Shared, artist to confirm | F2.2 | …_8_toes.png, same bytes as the JP female toes |
| JW female: eyes | Fixed layer | Shared, artist to confirm | F2.2 | Could use the JW male eyes |
| JW female: mouth | Fixed layer | Shared, artist to confirm | F2.2 | Could use the JW male mouth |
| JW female: nostrils | Fixed layer | Shared, artist to confirm | F2.2 | Could use the JW male nostrils |
| JW female: toes | Fixed layer | Shared, artist to confirm | F2.2 | Could use the JW male toes |

### Animations

Clip file: MOD/animations/entity/brachiosaurus.animation.json. REPO file: models/brachiosaurus_bb.bbmodel. The mod `rest` is the REPO `sleep` clip, and the mod `rest2` is the REPO `rest` clip.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Stand on hind legs to eat | Reach the highest leaves | Missing | F2.3 | No clip has this name. Artist to check if `eatingtall2` rears up |
| Call 2 | Second call | Missing | F2.3 | None |
| Jump | Jump gene | Missing | F2.3 | None |
| Limp | Low-health walk. It replaces the walk below 30 % health, a config value | Missing | F2.3 | None. Each species needs a new limp clip |
| Turn left and right | Large animal turns in place | Missing | F2.3 | None |
| Guard nest | Adult guards nest and young | Missing | F2.3 | None |
| Hatch | Young comes out of the egg | Missing | F2.3 | None |
| Yawn | Yawn effect needs the motion | Missing | F2.3 | None |
| Sneeze | Sneeze effect needs the motion | Missing | F2.3 | None |
| Startled (extra) | Stand-or-flee reaction. Wanted for every species | Missing | F2.3 | None |
| Shake body (extra) | Idle variety. Wanted for every species | Missing | F2.3 | None |
| Scratch (extra) | Idle variety. Wanted for every species | Missing | F2.3 | None |
| Rest idle | Lying idle | Placeholder | F2.3 | Stand-in: `restidle` is a copy of the sleep clip |
| Death | Death | Placeholder | F2.3 | Stand-in: `dead` is a copy of the sleep clip. No death clip anywhere |
| Rest (lie down) | Rest need. One lying pose cannot serve rest, sit and sleep | Placeholder | F2.3 | Stand-in: `rest2` (REPO `rest`) |
| Sit | Sit and tame stay. It needs its own clip | Placeholder | F2.3 | Stand-in: `rest2` (REPO `rest`) |
| Sleep | Sleep need. It needs its own clip | Placeholder | F2.3 | Stand-in: `rest` (8.25 s, REPO `sleep`). It ends in almost the same lying pose as `rest2` |
| Swim | Water | To check in game | F2.3 | `swim` has no length set: check that it loops |
| Eat (second clip) | Hunger variety | To check in game | F2.3 | `eat1` (REPO `eatingtall`, 12 bones) |
| JP3 model clip set | All clips for the JP3 model | Needs export | F2.3 | REPO models/brachiosaurus_adult_JP3.bbmodel (24 clips). Check the bone names match |
| Walk | Moving | Done | F2.3 | `move.walk` |
| Run | Flee | Done | F2.3 | `run` |
| Drink | Thirst | Done | F2.3 | `drink`, `Drinking2` |
| Eat high leaves | Eats high leaves | Done | F2.3 | `eatingtall2` |
| Attack (neck attack, stomp) | Attack | Done | F2.3 | `attack1`, `Neck Attack2`, `Stomp` |
| Tail whip | Defence against predators | Done | F2.3 | `tailwhip` |
| Call | Long-range call | Done | F2.3 | `call` (11 bones) |
| Threat display (roar) | Warning before attack | Done | F2.3 | `roar` |
| Get up | Stand after rest | Done | F2.3 | `getup` |
| Idle 1 | Standing idle | Done | F2.3 | `misc.idle` |
| Idle 2 | Standing idle | Done | F2.3 | `idle2` |
| Flinch | Hit reaction | Done | F2.3 | `flinch` |
| Sniff air | Detection | Done | F2.3 | `sniffair` |
| Sniff ground | Detection and idle. Scent trails are not in Beta | Done | F2.3 | `sniffground` |
| Look left and right | Detection | Done | F2.3 | `lookleft`, `lookright`, `lowlevellookleft`, `lowlevellookright` |
| Head shake | Idle | Done | F2.3 | `shakehead` |
| Speak (extra) | Short vocal | Done | F2.3 | `speak` |
| Lay eggs (nesting) | Female builds a nest and lays eggs. This species plays an idle clip | No art needed | F2.3 | Uses an idle clip |

### Sounds

None is in the mod. REPO folder: dinosaurs/brachiosaurus/sounds/ (30 files).

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Eat (leaf rip, chew) | Hunger | Missing | F2.3 | None |
| Footsteps and stomp | Walking, stomp attack | Missing | F2.3 | None |
| Tail whip | Defence attack | Missing | F2.3 | None |
| Sleep breath | Sleep | Missing | F2.3 | None |
| Yawn | Yawn effect, own sound per species | Missing | F2.3 | None |
| Sneeze | Sneeze effect, own sound per species | Missing | F2.3 | None |
| Snort | Snort effect, own sound per species | Missing | F2.3 | None |
| Nest call | Adults at the nest | Missing | F2.3 | None |
| Mating call | Pair before mating | Missing | F2.3 | None |
| Hatchling sounds | Young animals have their own voice, not a higher-pitched adult voice | Missing | F2.3 | None |
| Call (3) and broadcast (6) | Herd contact, long-range call | Ready to import | F2.3 | call_1..3.ogg, broadcast_1..6.ogg |
| Roar (5) and bellow (4) | Threat display | Ready to import | F2.3 | roar_1..5.ogg, bellow_1..4.ogg. roar_1 and call_1 have the same size: listen to check they differ |
| Growl (3) | Warning | Ready to import | F2.3 | growl_1..3.ogg |
| Attack snarl (3) | Attack | Ready to import | F2.3 | attack_snarl_1..3.ogg |
| Hurt (2) | Hit reaction | Ready to import | F2.3 | hurt_1, 2.ogg |
| Death (2) | Death | Ready to import | F2.3 | death_1, 2.ogg |
| Drink (2) | Thirst | Ready to import | F2.3 | swallow_water_1, 2.ogg |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Raw meat | Drop | Ready to import | F2.1 | REPO sprites/meat/brachiosaurus/ |
| Cooked meat | Cooked drop | Ready to import | F2.1 | REPO sprites/meat/brachiosaurus/ |
| Species icon (egg and DNA) | Egg and tube show the species | Ready to import | F1.1 | REPO dna_icons (Brachiosaurus JP and Sorna) |

---

## Gallimimus

The fast herd omnivore. It watches for danger and runs from threats.
Milestone: Dinosaurs.

**To make (36)**

- Skins, JP set (1): Bioluminescence glow mask.
- Skins, JW set (1): Male: belly.
- Animations (24): Drink; Eat; Ground peck (graze low); Call 2; Sleep; Get up; Jump; Idle 2; Limp; Death; Sniff ground; Head shake; Turn left and right; Guard nest; Hatch; Yawn; Sneeze; Shake body (extra); Scratch (extra); Speak (extra); Sit (rest) (placeholder); Rest idle (placeholder); Idle 1 (placeholder); Startled (placeholder).
- Sounds (9): Eat (peck); Footsteps (light); Sleep breath; Yawn; Sneeze; Snort; Nest call; Mating call; Hatchling sounds.
- Sprites (1): Species icon (egg and DNA).

**To import (23)**

- Skins, JW set (13): Female: base, darker colour, belly, stripes, claws, eyelids; Male: base, lighter colour, head light, body spots, white, leg spots, eyelids.
- Sounds (8): Broadcast (3), bellow (5), chirp (2); Screech (4); Growl (4); Bite; Burp (3); Hurt (3); Death (3); Drink (2).
- Sprites (2): Raw meat; Cooked meat.

**To fix (12)**

- Skins, JP set (4): Male: claws (confirm shared), mouth (confirm shared), nostrils (confirm shared), eyes (confirm shared).
- Skins, JW set (7): Female: mouth (confirm shared), nostrils (confirm shared), eyes (confirm shared); Male: claws (confirm shared), nostrils (confirm shared), mouth (confirm shared), eyes (confirm shared).
- Animations (1): Flinch (convert).

Elsewhere for this species: Bloody mouth overlay: Gallimimus (Missing, Dinosaur effects); Snow cover overlay: Gallimimus (Missing, Dinosaur effects); Printed egg: `normal` model (Missing, Eggs and hatching); Natural egg: Gallimimus (Missing, Eggs and hatching); Skull: Gallimimus (Ready to import, Fossils and amber); Tranquilised clip: Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus, Gallimimus (Missing, Guns (blocked, list only)).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Adult model | The animal | Done | F2.1 | MOD/geo/entity/gallimimus.geo.json (newer than REPO models/gallimimus_adult.bbmodel) |

### Skins and textures: JP set

Mod folder: MOD/textures/entity/gallimimus/<gender>/. The male folder also holds 5 unused copies of female layers.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Bioluminescence glow mask | Glow-in-the-dark patterns from the glow squid gene | Missing | F2.7 | None. One mask per species. All skin sets of the species use it |
| Male: claws | Fixed layer | Shared, artist to confirm | F2.2 | Female copy. REPO female claws and male toes are the same bytes |
| Male: mouth | Fixed layer | Shared, artist to confirm | F2.2 | Female copy. REPO mouths are the same bytes |
| Male: nostrils | Fixed layer | Shared, artist to confirm | F2.2 | Female copy. REPO files are the same bytes |
| Male: eyes | Fixed layer | Shared, artist to confirm | F2.2 | Female copy. REPO files are the same bytes |
| Female: base | Body colour layer | Done | F2.2 | female/base.png |
| Female: body darkness | Colour layer | Done | F2.2 | female/body-darkness.png |
| Female: peach | Colour layer | Done | F2.2 | female/peach.png |
| Female: tail darkness | Colour layer | Done | F2.2 | female/tail-darkness.png |
| Female: brown | Colour layer | Done | F2.2 | female/brown.png |
| Female: body stripes | Colour layer | Done | F2.2 | female/body-stripes.png |
| Female: tail stripes | Colour layer, female only | Done | F2.2 | female/tail-stripes.png |
| Female: belly | Colour layer | Done | F2.2 | female/belly.png |
| Female: claws | Fixed layer | Done | F2.2 | female/claws.png |
| Female: mouth | Fixed layer | Done | F2.2 | female/mouth.png |
| Female: nostrils | Fixed layer | Done | F2.2 | female/nostrils.png |
| Female: eyes | Fixed layer | Done | F2.2 | female/eyes.png |
| Female: eyelids | Rest and sleep | Done | F2.2 | female/eyelids.png |
| Male: base | Body colour layer | Done | F2.2 | male/base.png |
| Male: speckle (body spots) | Colour layer | Done | F2.2 | male/speckle.png |
| Male: green (cyan) | Colour layer | Done | F2.2 | male/green.png |
| Male: tail (light blue) | Colour layer | Done | F2.2 | male/tail.png |
| Male: pattern (dark blue) | Colour layer | Done | F2.2 | male/pattern.png |
| Male: body stripes | Colour layer | Done | F2.2 | male/body-stripes.png |
| Male: belly | Colour layer | Done | F2.2 | male/belly.png |
| Male: eyelids | Rest and sleep | Done | F2.2 | male/eyelids.png |

### Skins and textures: JW set

The JW skin is in Beta. REPO folders: textures/_skins/JW female/, JW male/. None is in the mod.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Male: belly | Colour layer | Missing | F2.2 | Possible gap: the female has one |
| Female: base | JW skin | Ready to import | F2.2 | JW female/gallimimus_adult_female_JW_1_base.png |
| Female: darker colour | Colour layer | Ready to import | F2.2 | …_2_darker-color.png |
| Female: belly | Colour layer | Ready to import | F2.2 | …_3_belly.png |
| Female: stripes | Colour layer | Ready to import | F2.2 | …_4_stripes.png |
| Female: claws | Fixed layer | Ready to import | F2.2 | …_5_claws.png (differs from the JP claws) |
| Female: eyelids | Rest and sleep | Ready to import | F2.2 | …_8_eyelids.png |
| Male: base | JW skin | Ready to import | F2.2 | JW male/gallimimus_adult_male_JW_1_base.png |
| Male: lighter colour | Colour layer | Ready to import | F2.2 | …_2_lighter-color.png |
| Male: head light | Colour layer | Ready to import | F2.2 | …_3_head-light.png |
| Male: body spots | Colour layer | Ready to import | F2.2 | …_4_body-spots.png |
| Male: white | Colour layer | Ready to import | F2.2 | …_5_white.png |
| Male: leg spots | Colour layer | Ready to import | F2.2 | …_6_leg-spots.png |
| Male: eyelids | Rest and sleep | Ready to import | F2.2 | …_11_eyelids.png |
| Female: mouth | Fixed layer | Shared, artist to confirm | F2.2 | …_6_mouth.png, same bytes as the JP mouth |
| Female: nostrils | Fixed layer | Shared, artist to confirm | F2.2 | …_6_nostrils.png, same bytes as the JP nostrils |
| Female: eyes | Fixed layer | Shared, artist to confirm | F2.2 | …_7_eyes.png, same bytes as the JP eyes |
| Male: claws | Fixed layer | Shared, artist to confirm | F2.2 | …_7_claws.png, same bytes as the JP claws |
| Male: nostrils | Fixed layer | Shared, artist to confirm | F2.2 | …_8_nostrils.png, same bytes as the JP nostrils |
| Male: mouth | Fixed layer | Shared, artist to confirm | F2.2 | …_9_mouth.png, same bytes as the JP mouth |
| Male: eyes | Fixed layer | Shared, artist to confirm | F2.2 | …_10_eyes.png, same bytes as the JP eyes |

### Animations

- Clip file: MOD/animations/entity/gallimimus.animation.json.
- The REPO model holds only 8 clips.
- Neck reach waits on the Drink and Eat clips, so they come first in the art queue.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Drink | Thirst. First in the art queue | Missing | F2.3 | None in the mod or REPO |
| Eat | Hunger. First in the art queue | Missing | F2.3 | None |
| Ground peck (graze low) | Omnivore feeding (GroundPeck) | Missing | F2.3 | None |
| Call 2 | Second call | Missing | F2.3 | None |
| Sleep | Sleep need | Missing | F2.3 | None |
| Get up | Stand after rest | Missing | F2.3 | None |
| Jump | Jump gene | Missing | F2.3 | None |
| Idle 2 | Standing idle | Missing | F2.3 | None |
| Limp | Low-health walk. It replaces the walk below 30 % health (config value) | Missing | F2.3 | None. Each species needs a new limp clip |
| Death | Death | Missing | F2.3 | None |
| Sniff ground | Detection and idle. No scent trails in Beta | Missing | F2.3 | None |
| Head shake | Idle | Missing | F2.3 | None |
| Turn left and right | Turn in place | Missing | F2.3 | None |
| Guard nest | Adult guards nest and young | Missing | F2.3 | None |
| Hatch | Young comes out of the egg | Missing | F2.3 | None |
| Yawn | Yawn effect needs the motion | Missing | F2.3 | None |
| Sneeze | Sneeze effect needs the motion | Missing | F2.3 | None |
| Shake body (extra) | Idle variety. Wanted for every species | Missing | F2.3 | None |
| Scratch (extra) | Idle variety. Wanted for every species | Missing | F2.3 | None |
| Speak (extra) | Short vocal. Wanted for every species | Missing | F2.3 | None |
| Sit (rest) | Rest need | Placeholder | F2.3 | Stand-in: `rest` is a copy of the static idle pose, not a lying pose |
| Rest idle | Lying idle | Placeholder | F2.3 | Stand-in: `restidle` is the same static pose |
| Idle 1 | Standing idle | Placeholder | F2.3 | Stand-in: `misc.idle` is one static pose (8 bones) |
| Startled | Reaction before running | Placeholder | F2.3 | Stand-in: `startled` is a copy of the Velociraptor startled clip |
| Flinch | Hit reaction | Needs conversion | F2.3 | `flinch`: 16 of 30 bones not in the model |
| Walk | Moving | Done | F2.3 | `move.walk` |
| Run (flee) | Gallimimus runs from threats | Done | F2.3 | `run` |
| Swim | Water | Done | F2.3 | `swim` |
| Attack (kick, bite) | Cornered defence | Done | F2.3 | `attack1`, `attack2` |
| Call | Herd call and alarm call | Done | F2.3 | `calling` |
| Sentry (upright watch) | Alert before fleeing | Done | F2.3 | `sentry` |
| Sniff air | Detection | Done | F2.3 | `sniffair` |
| Look left and right | Detection | Done | F2.3 | `lookleft`, `lookright` |
| Threat display | Gallimimus flees instead. The alarm call serves | Not in Beta | F2.3 | Not needed while the species always flees |
| Lay eggs (nesting) | Female builds a nest and lays eggs. This species plays an idle clip | No art needed | F2.3 | Uses an idle clip |

### Sounds

None is in the mod. REPO folder: dinosaurs/gallimimus/sounds/ (30 files).

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Eat (peck) | Hunger | Missing | F2.3 | None |
| Footsteps (light) | Walking and running | Missing | F2.3 | None |
| Sleep breath | Sleep | Missing | F2.3 | None |
| Yawn | Yawn effect, own sound per species | Missing | F2.3 | None |
| Sneeze | Sneeze effect, own sound per species | Missing | F2.3 | None |
| Snort | Snort effect, own sound per species | Missing | F2.3 | None |
| Nest call | Adults at the nest | Missing | F2.3 | None |
| Mating call | Pair before mating | Missing | F2.3 | None |
| Hatchling sounds | Young have their own voice, not a higher-pitched adult voice | Missing | F2.3 | None |
| Broadcast (3), bellow (5), chirp (2) | Herd contact calls | Ready to import | F2.3 | broadcast_1..3, bellow_1..5, chirp_1, 2.ogg |
| Screech (4) | Alarm and threat | Ready to import | F2.3 | screech_1..4.ogg |
| Growl (4) | Warning | Ready to import | F2.3 | growl_1..4.ogg |
| Bite | Attack | Ready to import | F2.3 | bite_1.ogg |
| Burp (3) | Idle | Ready to import | F2.3 | burp_1..3.ogg |
| Hurt (3) | Hit reaction | Ready to import | F2.3 | hurt_1..3.ogg |
| Death (3) | Death | Ready to import | F2.3 | death_1..3.ogg |
| Drink (2) | Thirst | Ready to import | F2.3 | drinking_1, 2.ogg |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Species icon (egg and DNA) | Egg and tube show the species | Missing | F1.1 | None in the mod or REPO dna_icons |
| Raw meat | Drop | Ready to import | F2.1 | REPO sprites/meat/gallimimus/ |
| Cooked meat | Cooked drop | Ready to import | F2.1 | REPO sprites/meat/gallimimus/ |

---

## Dinosaur effects

Shared visual effects for all six species. All of them are in Beta.
Milestone: Dinosaurs.

- **Omnivore meat effects:** the Gallimimus eats meat, so it gets blood drips and a bloody mouth.
- **Bloody claws:** all three carnivores get them (Tyrannosaurus, Velociraptor, Dilophosaurus).
- **Screen shake:** only the T. rex, Triceratops and Brachiosaurus cause it.
- **Today in the code:** the mod has no custom particle type.

**To make (32)**

- Effects: mouth and body (16): Roar spit particle; Drinking water drips; Blood drip particle; Bloody mouth overlay: Tyrannosaurus, Velociraptor, Dilophosaurus, Gallimimus; Bloody claw overlay: Tyrannosaurus, Velociraptor, Dilophosaurus; Plant bits particle; Foggy breath; Yawn effect; Sneeze effect; Snort effect; Camouflage look.
- Effects: movement (6): Footprint: small biped, large biped, quadruped, sauropod; Dust trail; Air trails.
- Effects: environment and sleep (10): Snow cover overlay: Tyrannosaurus, Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus, Gallimimus; Splash ring; Water ripples; Sleep Zzz; Sleep bubbles.

**To import (0)**

- Nothing.

**To fix (0)**

- Nothing.

### Effects: mouth and body

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Roar spit particle | Spit flies when a dinosaur roars | Missing | F2.3 | None |
| Drinking water drips | Water drips from the mouth after drinking | Missing | F2.3 | None |
| Blood drip particle | Blood drips while a carnivore or the Gallimimus eats meat | Missing | F2.3 | None |
| Bloody mouth overlay: Tyrannosaurus | Mouth turns red after eating meat | Missing | F2.3 | One overlay on the species UV map |
| Bloody mouth overlay: Velociraptor | Same | Missing | F2.3 | One overlay on the species UV map |
| Bloody mouth overlay: Dilophosaurus | Same | Missing | F2.3 | One overlay on the species UV map |
| Bloody mouth overlay: Gallimimus | The omnivore eats meat too | Missing | F2.3 | One overlay on the species UV map |
| Bloody claw overlay: Tyrannosaurus | Claws turn red after an attack | Missing | F2.3 | One overlay on the species UV map |
| Bloody claw overlay: Velociraptor | Same | Missing | F2.3 | One overlay on the species UV map |
| Bloody claw overlay: Dilophosaurus | Same | Missing | F2.3 | One overlay on the species UV map |
| Plant bits particle | Leaves fall from the mouth while a herbivore eats | Missing | F2.3 | None |
| Foggy breath | Breath shows in cold biomes | Missing | F2.3 | None |
| Yawn effect | Tired or waking animal | Missing | F2.3 | None |
| Sneeze effect | Sneeze puff | Missing | F2.3 | None |
| Snort effect | Snort puff from the nostrils | Missing | F2.3 | None |
| Camouflage look | Camouflage gene: hard to see while still | Missing | F2.7 | None. A texture or a code effect: artist and developer decide |

### Effects: movement

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Footprint: small biped | Slow-fading prints (Velociraptor, Dilophosaurus, Gallimimus) | Missing | F2.3 | Prints differ by size and by quadruped or biped |
| Footprint: large biped | Tyrannosaurus prints | Missing | F2.3 | None |
| Footprint: quadruped | Triceratops prints | Missing | F2.3 | None |
| Footprint: sauropod | Brachiosaurus prints | Missing | F2.3 | None |
| Dust trail | Dust behind a running animal | Missing | F2.3 | None |
| Air trails | Swipe trails on arm and tail attacks | Missing | F2.3 | None |
| Screen shake | A T. rex, Triceratops or Brachiosaurus walks close or roars. Config can turn it off | No art needed | F2.3 | Code only |

### Effects: environment and sleep

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Snow cover overlay: Tyrannosaurus | Snow on an animal that sits or sleeps in a snow biome | Missing | F2.3 | One overlay on the species UV map |
| Snow cover overlay: Velociraptor | Same | Missing | F2.3 | One overlay on the species UV map |
| Snow cover overlay: Dilophosaurus | Same | Missing | F2.3 | One overlay on the species UV map |
| Snow cover overlay: Triceratops | Same | Missing | F2.3 | One overlay on the species UV map |
| Snow cover overlay: Brachiosaurus | Same | Missing | F2.3 | One overlay on the species UV map |
| Snow cover overlay: Gallimimus | Same | Missing | F2.3 | One overlay on the species UV map |
| Splash ring | Large animal enters or moves in water | Missing | F2.3 | None |
| Water ripples | Ripples when a large dinosaur is near water | Missing | F2.3 | None |
| Sleep Zzz | Sleeping animal | Missing | F2.3 | None |
| Sleep bubbles | Sleeping animal | Missing | F2.3 | None |
| Mud overlay | Mud look, for a later release | Not in Beta | F2.3 | None |
| Wound overlays (cuts, scars) | Wound look | Not in Beta | F2.3 | REPO dinosaurs/_common/textures/damage_overlay_*.png |

**Changed since last review:** All effects are in Beta. The Gallimimus gets blood drips and a bloody mouth. Screen shake stays with the three large species. All three carnivores get bloody claws.

---

## Dinosaur hover box

The look-at box that every player sees on a dinosaur. It is the same hover box as on machines and feeders (F1.10).
Milestone: Dinosaurs.

- **Icons only:** the name, a coloured mood face for the contentment band, and the icon of the worst need.
- **No band words.**
- **Name:** code draws it.

**To make (2)**

- GUI (2): Mood faces (3): content, uneasy, distressed; Need icons (7): hunger, thirst, tired, lonely, stressed, hurt, threatened.

**To import (0)**

- Nothing.

**To fix (0)**

- Nothing.

### GUI

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Mood faces (3): content, uneasy, distressed | A coloured face for the contentment band. No band words | Missing | F2.0 | None |
| Need icons (7): hunger, thirst, tired, lonely, stressed, hurt, threatened | The icon of the worst need. Two icons when two needs are low | Missing | F2.0 | None |
| Hover box name text | The species name or the given name | No art needed | F2.0 | Code draws the text, as the F1.10 machine hover box. See F2.0 "Contentment" |

---

## Eggs and hatching

The egg the Egg Printer makes, the Incubator warms and the player places on the ground to hatch a baby. Also the natural egg a female lays in a nest.
Milestone: Fossil to egg (natural eggs: Dinosaurs, F2.5).

Egg model per species, from the REPO egg folders:

| Species | Egg model |
| --- | --- |
| Brachiosaurus | `round` |
| Tyrannosaurus | `tyrannosaurus` |
| Triceratops | `tall_3` |
| Gallimimus | `normal` |
| Velociraptor and Dilophosaurus | `velociraptor` |

- **Natural (bred) eggs:** they get the colours of their species on these models.
- **Printed eggs:** one texture per egg model. Code adds a species tint.
- **Both in Beta:** natural and printed egg textures.
- **Nest:** a female builds it from vanilla blocks, so it needs no art.
- **Placed egg:** it is an entity.

**To make (18)**

- Models (1): Broken placed egg (failed hatch).
- Skins (9): Printed egg: `tall_3` model, `round` model, `normal` model; Base egg texture for `tall_3` (Triceratops); Crack stages (2 or 3 overlays); Natural egg: Dilophosaurus, Triceratops, Brachiosaurus, Gallimimus.
- Sounds (4): Egg wobble (rock tap); Egg crack; Hatch; Egg breaks.
- Effects (2): Shell bits particle; Egg-break effect (failed print).
- Sprites (2): Unincubated egg item (placeholder); Incubated egg item (placeholder).

**To import (4)**

- Skins (4): Printed egg: `tyrannosaurus` model, `velociraptor` model; Natural egg: Tyrannosaurus, Velociraptor.

**To fix (5)**

- Models (5): Egg model: Brachiosaurus (convert), Tyrannosaurus (convert), Triceratops (convert), Gallimimus (convert), Velociraptor and Dilophosaurus (convert).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Broken placed egg (failed hatch) | A failed hatch leaves a broken egg and remains | Missing | F1.9 | None |
| Egg model: Brachiosaurus | Brachiosaurus egg | Needs conversion | F1.9 | REPO eggs/round/model/tbl/*.tbl. Pivot at the base for the wobble |
| Egg model: Tyrannosaurus | Tyrannosaurus egg | Needs conversion | F1.9 | REPO eggs/tyrannosaurus/model/tbl/egg_tyranosaurus.tbl |
| Egg model: Triceratops | Triceratops egg | Needs conversion | F1.9 | REPO eggs/tall_3/model/tbl/*.tbl. It has no texture of its own (question 9) |
| Egg model: Gallimimus | Gallimimus egg | Needs conversion | F1.9 | REPO eggs/normal/model/tbl/*.tbl. Its cubes are the same as the `velociraptor` model (question 9) |
| Egg model: Velociraptor and Dilophosaurus | Velociraptor and Dilophosaurus egg | Needs conversion | F1.9 | REPO eggs/velociraptor/model/tbl/egg_velociraptor.tbl |
| Baby model | Young scale from the adult | Not in Beta | F2.1 | None |
| Nest | Female builds a nest from vanilla blocks of her surroundings and lays eggs | No art needed | F2.5 | Vanilla blocks. No nest model exists |

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Printed egg: `tall_3` model | Egg from the printer (Triceratops). One texture per model, tinted per species in code | Missing | F1.9 | None |
| Printed egg: `round` model | Egg from the printer (Brachiosaurus) | Missing | F1.9 | None |
| Printed egg: `normal` model | Egg from the printer (Gallimimus) | Missing | F1.9 | None |
| Base egg texture for `tall_3` (Triceratops) | Triceratops egg model | Missing | F1.9 | REPO eggs/tall_3 has a model and no texture. `egg_normal.png` fits the `tall_3` UV exactly. Ask: is it the `tall_3` texture (question 9)? |
| Crack stages (2 or 3 overlays) | Egg cracks before it hatches | Missing | F1.9 | None |
| Natural egg: Dilophosaurus | Laid egg: Dilophosaurus colours on the `velociraptor` model | Missing | F2.5 | None |
| Natural egg: Triceratops | Laid egg: Triceratops colours on the `tall_3` model | Missing | F2.5 | None |
| Natural egg: Brachiosaurus | Laid egg: Brachiosaurus colours on the `round` model | Missing | F2.5 | None |
| Natural egg: Gallimimus | Laid egg: Gallimimus colours on the `normal` model | Missing | F2.5 | None |
| Printed egg: `tyrannosaurus` model | Egg from the printer (QR code look) | Ready to import | F1.9 | REPO eggs/tyrannosaurus/ QR code texture. It is 2048 px and renders black on the model (question 9) |
| Printed egg: `velociraptor` model | Egg from the printer (Velociraptor and Dilophosaurus, tinted per species in code) | Ready to import | F1.9 | REPO eggs/velociraptor/ QR code texture. It is 2048 px and renders black on the model (question 9) |
| Natural egg: Tyrannosaurus | Laid egg | Ready to import | F2.5 | REPO eggs/tyrannosaurus/ TyrannosaurusEgg-texturemap.png |
| Natural egg: Velociraptor | Laid egg | Ready to import | F2.5 | REPO eggs/velociraptor/ VelociraptorEgg-pn.png |

### Animations

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Egg wobble while incubating | About 4 degrees side to side every 30 to 90 s | No art needed | F1.9 | Code-driven. Needs the pivot at the egg base (egg model rows) |
| Egg shake before hatching | 60 s shake in 3 stages before the hatch | No art needed | F1.9 | Code-driven. Same pivot |

### Sounds

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Egg wobble (rock tap) | Egg moves in the incubator or on the ground | Missing | F1.9 | None |
| Egg crack | Crack stage | Missing | F1.9 | None |
| Hatch | Young comes out | Missing | F1.9 | None |
| Egg breaks | Failed hatch, broken placed egg or failed print | Missing | F1.9 | None |

### Effects

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Shell bits particle | Hatching | Missing | F1.9 | None |
| Egg-break effect (failed print) | The egg on the printer platform breaks on a failed print | Missing | F1.7 | None. It can share the shell bits particle: artist to say |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Unincubated egg item | Egg Printer output, egg before the incubator | Placeholder | F1.9 | Stand-in: same bytes as artificial_egg.png |
| Incubated egg item | Egg after the incubator | Placeholder | F1.9 | Stand-in: same bytes as artificial_egg.png |
| Cracked artificial egg item | Failed-print result: a print fails 10% of the time without the Leveling Sensor. The artificial egg exists only in this form | Done | F1.7 | MOD/textures/item/cracked_artificial_egg.png |

**Changed since last review:** Printed eggs use one texture per egg model with a species tint in code. Females build nests from vanilla blocks. The artificial egg exists only as the cracked failed-print item.

---

## Fossil Processor

The first lab machine. The player puts in a fossil, water, a filter and an empty test tube, and gets a test tube of genetic material.
Milestone: Fossil to egg.

### References

- **Real-world look:** vertical rotor crushers.
  - http://www.kefid.com/v3/uploads/130702/1-130F20ZI0O9.jpg
  - http://uxacademy.in/images/IMG_1855.jpg
  - http://50.6.0.74/wp-content/themes/salegold/images/products/b-series-deep-rotor-vsi-crusher.jpg
  - http://www.kefid-machinery.com/images/stone-crusher/d-vsi-crusher-01.jpg
  - http://www.kefid.com/v3/uploads/proseo/proli_Sta/Deep-Rotor-VSI-Crusher.jpg
  - http://www.wascrusher.com//wp-content/uploads/2014/03/deep-crusher2.jpg
  - http://processcrusher.com/images/pro-img/crushing/vsi5x-crusher.jpg
- **Video behaviour:** Cloning Guide https://youtu.be/W9cMQUB4EdM 0:23-2:16.

### Art target

- **Target:** the current model and texture in the mod, not the 2022 video look.
- **Model:** MOD/geo/block/processor.geo.json.
- **Bones:** `FossilProcessor` (root, 21 cubes), `water_port`, `power_port`, `base_tanks`, `iron_tanks`, `gold_tanks`, `diamond_tanks`.
- **No moving parts:** no part moves, and none will. The video and the original show no Processor animation.
- **Older state:** REPO machines/fossil_processor/models/Fossil_Processor.bbmodel has one group of 36 cubes and no tank bones.

**To make (8)**

- Skins (2): Chip texture swap per chip tier; Powered glow: emissive texture elements.
- Sounds (2): Start; Fault: no water or worn filter.
- Sprites (4): Processor base; Processor body; Processor lid; Processor tanks.

**To import (0)**

- Nothing.

**To fix (4)**

- Sounds (3): Working loop (confirm shared); Done (confirm shared); Fault: no power (confirm shared).
- GUI (1): Processor screen (check in game).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Machine model with water and power ports | The machine | Done | F1.3 | MOD/geo/block/processor.geo.json, bones `water_port`, `power_port` |
| Base, drum and lid cap as separate bones | Each placed part shows while the player builds | Done | F1.3 | Developer splits each into its own bone (no art change). See the notes below |
| Tanks: default | Default tank look | Done | F1.3 | Bone `base_tanks` |
| Tanks: iron tier | Tank upgrade changes the look | Done | F1.3 | Bone `iron_tanks` |
| Tanks: gold tier | Tank upgrade changes the look | Done | F1.3 | Bone `gold_tanks` |
| Tanks: diamond tier | Tank upgrade changes the look (8 buckets) | Done | F1.3 | Bone `diamond_tanks` |

Base, drum and lid cap:

- Each build step shows its placed part. Today these three parts share one bone.
- The cubes exist inside the root bone `FossilProcessor`: base y 0-12, drum y 12-41, lid cap y 41-44 (REPO bbmodel).

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Chip texture swap per chip tier | Every upgrade changes the look. No new part: a texture swap on an existing bone | Missing | F1.3 | None. The artist proposes the bone. Question 12 |
| Powered glow: emissive texture elements | Chosen texture elements light up and animate while the Processor has power. No part moves | Missing | F1.3 | None. The artist proposes the elements, and the lead approves them (D279) |
| Block texture | Machine look | Done | F1.3 | MOD/textures/block/processor.png |

### Animations

None. The video (0:36-2:15) and the original show no Processor animation.

### Sounds

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Start | Run begins | Missing | F1.3 | None |
| Fault: no water or worn filter | Machine cannot run | Missing | F1.3 | None |
| Working loop | Machine runs (DNA Extractor Active) | Shared, artist to confirm | F1.3 | REPO machines/_common/sounds/labmachine/obj_labmachine_run_b1.ogg, b2 (generic lab sound: does it suit a crusher?) |
| Done | Run finished | Shared, artist to confirm | F1.3 | REPO machines/_common/sounds/obj_light_circuit_on.ogg (generic) |
| Fault: no power | Machine stops with no power | Shared, artist to confirm | F1.3 | REPO machines/_common/sounds/obj_computerpowerdown.ogg (generic) |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Processor base | Build the processor | Missing | F1.3 | None |
| Processor body | Same | Missing | F1.3 | None |
| Processor lid | Same | Missing | F1.3 | None |
| Processor tanks | Same | Missing | F1.3 | None |
| Processor (whole machine) | Creative item | Done | F1.3 | MOD/textures/item/processor.png |

### GUI

Screen contents follow the video. Screen art is the current GUI texture in the mod.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Processor screen | See the list below | To check in game | F1.3 | MOD/textures/gui/processor.png holds every slot, the gauge and the arrow. Check in game that each one works as in the video |
| Water gauge fill | Animated water that rises and falls | Done | F1.3 | MOD/textures/gui/fluid_overlay.png |

Processor screen parts: filter slot, bucket slot, water gauge, fossil slot, progress arrow, 3x3 output, test tube slot.

**Changed since last review:** The art target is the current mod model and texture, not the 2022 video look. The Processor has no animation.

---

## Sequencer

The second lab machine. It reads DNA onto a drive, edits genes, and synthesises the embryo syringe.
Milestone: Fossil to egg.

### References

- **Real-world look:** Illumina HiSeq X, https://www.illumina.com/systems/sequencing-platforms/hiseq-x.html.
- **Video behaviour:** 2:16-11:12.

### Art target

- **Target:** the current model and texture in the mod.
- **Model:** MOD/geo/block/sequencer.geo.json.
- **Bones:** `Sequencer` (root), `computer`, `monitor_primary`, `mainBody`, `Door`.
- **REPO model:** machines/sequencer/Sequencer.bbmodel has the same groups, with the monitor still inside `computer`.
- **Behind the door:** inside `mainBody`, the model already has a tube tray with 16 tubes, 3 system racks and 4 chip cubes. REPO cube names: `system_rack1` to `3`, `computer_chip01` to `04`.

**To make (15)**

- Skins (3): Tank texture swap per tank tier; Dye mask (black parts take the dye colour); Lit monitor while working.
- Sounds (3): Start; Done; Door open and close.
- GUI (9): Edit tab: donor rows with sliders, effect panel, chromosome grid, gender and reset buttons, colour wheel with brightness strip, layer controls, number slider box, risk panel, Basic and Advanced button.

**To import (1)**

- Sounds (1): Working loop.

**To fix (5)**

- Sounds (1): Fault: no power (confirm shared).
- Sprites (1): Sequencer base (check in game).
- GUI (3): Screen frame with turning ring logo (check in game); Sequence tab (check in game); Synthesis tab (check in game).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Base, door, computer and monitor parts | Built part by part | Done | F1.4 | MOD/geo/block/sequencer.geo.json bones `Sequencer`, `Door`, `computer`, `monitor_primary`, `mainBody` |
| Door with hinge pivot | Door swings 90 degrees while the screen is open | Done | F1.4 | Bone `Door`, pivot [23.5, 13.5, -2]. The pivot sits on a vertical door edge (REPO bbmodel: origin x -7.5, door x -8 to 8) |
| Chip look per chip tier | Every upgrade changes the look. See the notes below | Done | F1.4 | Developer splits into its own bone (no art change). See the notes below |

Chip look per chip tier:

- The chip cubes are one of the two upgrade looks that use parts. The original added a computer piece per tier.
- The 4 chip cubes `computer_chip01` to `04` exist inside `mainBody`, behind the door.
- More chips show at higher tiers (proposal, lead decides).

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Tank texture swap per tank tier | Every upgrade changes the look. No new part: a texture swap on an existing bone | Missing | F1.6 | None. The artist proposes the bone. Question 12 |
| Dye mask (black parts take the dye colour) | Dye changes the colour | Missing | F1.4 | None. Every black section takes the dye. The mask follows the black sections of the current texture |
| Block texture | Machine look | Done | F1.4 | MOD/textures/block/sequencer.png |
| Lit monitor while working | The monitor glows while the Sequencer works. The original had no glow | Missing | F1.4 | None. It lights bone `monitor_primary`. D279 can add more powered elements |

### Animations

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Door open and close | 90 degrees, 10 ticks, while the screen is open | No art needed | F1.4 | Code-driven. Turns bone `Door` about Y at its pivot |

### Sounds

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Start | Sequencing or synthesis begins | Missing | F1.4 | None |
| Done | Sample or synthesis finished | Missing | F1.4 | None |
| Door open and close | Door swings with the screen | Missing | F1.4 | None |
| Working loop | Sequencing and synthesis run | Ready to import | F1.4 | REPO machines/sequencer/sounds/computersound_06.ogg (sequencer sound) |
| Fault: no power | Machine stops | Shared, artist to confirm | F1.4 | REPO machines/_common/sounds/obj_computerpowerdown.ogg (generic) |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Sequencer base | Build the sequencer | To check in game | F1.4 | MOD/textures/item/sequencer.png. Check that it shows the base |
| Sequencer door | Same | Done | F1.4 | MOD/textures/item/sequencer_door.png |
| Sequencer computer | Same | Done | F1.4 | MOD/textures/item/sequencer_computer.png |
| Sequencer monitor | Same | Done | F1.4 | MOD/textures/item/sequencer_monitor.png |

### GUI

Screen contents follow the video. Screen art is the current GUI texture in the mod.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Edit tab: donor rows with sliders | Preview with a player for scale, donor rows with sliders, animal list | Missing | F1.5 | The page uses the current frame (sequencer.png) and tab (edit_page.png). The rows and sliders have no art |
| Edit tab: effect panel | Effect rows with colour bars and two colour swatches | Missing | F1.5 | None |
| Edit tab: chromosome grid | Gene bars: cyan, purple for isolated, red for selected | Missing | F1.5 | None |
| Edit tab: gender and reset buttons | Gender cycles male, female, random | Missing | F1.5 | None |
| Edit tab: colour wheel with brightness strip | Colour genes | Missing | F1.5 | None |
| Edit tab: layer controls | Per-layer mode with saturation, opacity and tint strength | Missing | F1.5 | None. Not in the video |
| Edit tab: number slider box | Number genes such as Immunity | Missing | F1.5 | None |
| Edit tab: risk panel | Incubation failure chance and stat drops | Missing | F1.5 | None. Not in the video |
| Edit tab: Basic and Advanced button | Two views | Missing | F1.5 | None |
| Screen frame with turning ring logo | All tabs: frame, three tab shapes, "PN" logo with ring arcs that turn | To check in game | F1.4 | MOD/textures/gui/sequencer.png (frame, tabs) and center_pieces.png (5 ring arcs, PN logo). REPO guis/sequencer is another set. Check in game that the rings turn |
| Sequence tab | See the list below | To check in game | F1.4 | MOD/textures/gui/sequencer.png, sequencer_page.png. Check in game that each item from the video is there |
| Synthesis tab | Four ingredient bars, embryo picture that fills, Begin and Cancel button, arrow | To check in game | F1.6 | MOD/textures/gui/synth_page.png, center_pieces.png. Check in game that each item from the video is there |
| DNA helix that grows while sequencing | Progress | Done | F1.4 | MOD/textures/gui/dna_spiral.png |
| Info card beside the 3D model | Name, time period, diet | No art needed | F1.4 | Code draws the text on the current frame (MOD/textures/gui/sequencer.png) |

Sequence tab parts: drive slot, Regular and Isolated toggle, species list, Isolated gene and animal lists, input and leftover slots, DNA track.

**Changed since last review:** The monitor glows while the Sequencer works. Each upgrade changes the look with a texture swap, except the chip cubes.

---

## Egg Printer

The third lab machine. It takes the embryo syringe and bone meal and prints an egg.
Milestone: Fossil to egg.

### References

- **Real-world look:** MakerBot Replicator Mini.
  - https://pisces.bbystatic.com/image2/BestBuy_US/images/products/5661/5661223_sd.jpg
  - <https://images-na.ssl-images-amazon.com/images/I/51gXrytuclL._SX522_.jpg>
  - https://www.supplychimp.com/media/extendware/ewimageopt/media/inline/b4/6/makerbot-mp05925-replicator-mini-compact-3d-printer-957.jpg
  - https://img.grouponcdn.com/deal/35nYjBn2hc4fJwbwtJMT/vz-1666x1000/v1/c700x420.jpg
  - http://gadgetsmagazine.com.ph/wp-content/uploads/2016/04/Makerbot-Replicator-Mini.png
- **Video behaviour:** 11:12-12:21.
- **Motion:** programmatic. The original moved 3 parts in code: platform, needle and lid.

### Art target

- **Target:** the current model and texture in the mod.
- **Model:** MOD/geo/block/egg_printer.geo.json, the same groups as REPO machines/egg_printer/models/egg_printer.bbmodel.
- **Bones:**
  - `Printer` (root).
  - `Panel`: the platform plate, 16 by 14 px, y 2.5-3.5.
  - `Body` (22 cubes).
  - `Egg` (y 3.5-13.5).
  - `Door`: the glass lid, hinge on the back top edge.
- **Cubes inside `Body`:** the needle, the cross-rail and the two side rails have no bone of their own.
  - The cross-rail is a plane at y 15.5, from x 0 to 16.
  - The needle is a 1 by 2 px plane under its middle (x 7.5-8.5, y 13.5-15.5).
  - The side rails are planes at x -2.5 and x 18.5 (y 16-18, z -5 to 6).
- **Bone split:** a developer moves the needle and the cross-rail into bones of their own, with no art change.
- **Platform room:** `Panel` has only 0.5 px of room below it, so it can lower 0.5 px at most.

**To make (12)**

- Skins (4): Chip texture swap per chip tier; Leveling Sensor texture swap; Ghost egg outline (idle printer); Emissive texture for the red and green lamps and the 2 by 3 panel.
- Sounds (4): Start; Done; Fault: broken egg; Lid open and close.
- Sprites (4): Egg Printer base; Egg Printer needle; Egg Printer platform; Egg Printer body.

**To import (0)**

- Nothing.

**To fix (3)**

- Sounds (2): Printing loop (confirm shared); Fault: no power (confirm shared).
- Sprites (1): Egg Printer (whole machine) (check in game).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Base and body as separate bones | Built part by part, base first | Done | F1.7 | Developer splits into its own bone (no art change). The base is its own build step. Today `Body` holds the base and the case in one bone |
| Cross-rail as its own bone | Moves along Z on the side rails for the raster. Also shows the needle build step | Done | F1.7 | Developer splits into its own bone (no art change). The cube exists inside `Body` (plane at y 15.5, x 0-16). The side-rail planes stay in `Body` |
| Needle as its own bone (child of the cross-rail) | The needle moves along X on the cross-rail | Done | F1.7 | Developer splits into its own bone (no art change). The cube exists inside `Body` (1 by 2 px plane, x 7.5-8.5, y 13.5-15.5) |
| Platform | The egg rests on it. Build step | Done | F1.7 | Bone `Panel`, pivot [8, 0.5, 0] |
| Lid with hinge | Lid opens 60 degrees while the screen is open or an egg waits | Done | F1.7 | Bone `Door`, pivot [8, 16.62, 7.17] on the back top edge. It turns about X |
| Egg inside the printer | Egg builds from the bottom up | Done | F1.7 | Bone `Egg`, pivot [8, 0.5, 0] |

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Emissive texture for the red and green lamps and the 2 by 3 panel | The lamps glow while the printer prints | Missing | F1.7 | No emissive texture. The lamps and the panel are in the current texture. D279 can add more powered elements and animation |
| Chip texture swap per chip tier | Every upgrade changes the look. No new part: a texture swap on an existing bone. The original shows no change | Missing | F1.7 | None. The artist proposes the bone. Question 12 |
| Leveling Sensor texture swap | Same | Missing | F1.7 | None. The artist proposes the bone. Question 12 |
| Ghost egg outline (idle printer) | An idle printer shows a ghost egg outline on the platform | Missing | F1.7 | None. Code can also draw the egg model see-through: artist to say |
| Block texture | Machine look | Done | F1.7 | MOD/textures/block/egg_printer.png |

### Animations

All code-driven. Numbers are in backlog F1.7 "In scope: animation" and spec F1.7 "Animations". Each row drives a bone of the current model.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Platform lowers with progress | Print motion. The original platform sank one egg length (6.5 px) as the egg grew | No art needed | F1.7 | Code-driven. Moves bone `Panel` down with progress, 0.5 px at most (room below `Panel`) |
| Needle raster sweep | Print motion | No art needed | F1.7 | Code-driven. See the notes below |
| Lid opens and closes | Screen open or egg ready | No art needed | F1.7 | Code-driven. Turns bone `Door` about X at its pivot |
| Egg builds from the bottom up | Print progress. The egg is the species' egg model with the printed texture | No art needed | F1.7 | Code-driven. Shows bone `Egg` from its base (y 3.5) up to the build height |

Needle raster sweep:

- Code moves the needle bone along X on the cross-rail, 2 px each way.
- Code moves the cross-rail bone along Z on the side rails, 2 px each way.
- It needs the developer bone split in the Models table.

### Sounds

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Start | Print begins | Missing | F1.7 | None |
| Done | Egg ready | Missing | F1.7 | None |
| Fault: broken egg | 10% failed print | Missing | F1.7 | None |
| Lid open and close | Lid hinges open | Missing | F1.7 | None |
| Printing loop | Machine runs | Shared, artist to confirm | F1.7 | REPO machines/_common/sounds/mechanical/obj_mechanical_01.ogg, 06 (generic) |
| Fault: no power | Machine stops | Shared, artist to confirm | F1.7 | REPO machines/_common/sounds/obj_computerpowerdown.ogg (generic) |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Egg Printer base | Build the printer | Missing | F1.7 | None |
| Egg Printer needle | Same | Missing | F1.7 | None |
| Egg Printer platform | Same | Missing | F1.7 | None |
| Egg Printer body | Same | Missing | F1.7 | None |
| Egg Printer (whole machine) | Creative item | To check in game | F1.7 | MOD/textures/item/egg_printer.png (226 bytes: check quality) |

### GUI

Screen contents follow the video. Screen art is the current GUI texture in the mod.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Egg Printer screen | Bone meal gauge and slot, syringe slot, egg picture with progress lines, two output slots | Done | F1.7 | MOD/textures/gui/egg_printer.png, bonemeal_bar.png, egg.png |

**Changed since last review:** The printer lamps glow while it prints. An idle printer shows a ghost egg. The printed egg is the species' egg model with the printed texture.

---

## Incubator

The fourth lab machine. It warms 3, 6 or 9 eggs on a leaf bed while a robotic arm turns them.
Milestone: Fossil to egg.

### References

- **Real-world look:**
  - https://farm4.staticflickr.com/3856/14507585294_1db2f47045_b.jpg
  - http://www.julienslive.com/images/lot/1319/13197_0.jpg
  - http://www.julienslive.com/images/lot/1319/13194_0.jpg
  - http://www.julienslive.com/images/lot/1319/13198_4.jpg
- **Video behaviour:** 12:21-15:05.
- **Arm:** it reaches any egg in range, with no preset positions. It turns each egg every 5 minutes.

### Art target

- **Target:** the current model and texture in the mod.
- **Model:** MOD/geo/block/incubator.geo.json.
- **Bones:**
  - `body`, `nest`, `arm_base`.
  - `cover`: the lid. It has a glass dome, a lamp cap and two small tabs on one edge.
  - Arm chain: `RoboticHand1` (base turn) → `RoboticHand2` (shoulder) → `RoboticHand3` (elbow) → `RoboticHand4` (wrist and hand) → `RoboticHand5` and `RoboticHand6` (the two claw fingers).
- **Texture:** the mod texture is the same file as REPO machines/incubator/textures/incubator_02.png.
- **REPO models:** incubator_02.bbmodel has the same arm groups, with the nest and the arm base still inside `body`. incubator_01.bbmodel and incubator_01.png are an older variant.
- **No lift post:** the current model has no lift post and no separate lamp part. Code moves `cover` straight up.

**To make (12)**

- Skins (6): Container upgrade texture swap (6 and 9 eggs); Plant tank upgrade texture swap; Emissive lamp texture: base lamp, warm bulb, warmer bulb, hot bulb.
- Sounds (5): Start; Lid up and down; Egg placed; Done; Fault: egg dies.
- GUI (1): Bed tint: green and red.

**To import (1)**

- Sounds (1): Arm move.

**To fix (8)**

- Models (2): Arm links: base turn, shoulder, elbow, wrist (check in game); Nest bed holds 9 eggs (check in game).
- Sounds (2): Heater hum (confirm shared); Fault: no power (confirm shared).
- Sprites (1): Incubator base (check in game).
- GUI (3): Bed fills with plants (check in game); Empty plant slot sprout icon (check in game); Incubator screen (check in game).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Arm links: base turn, shoulder, elbow, wrist | Arm reaches each egg | To check in game | F1.8 | See the pivots below. Links are 6.5 px: check that the reach covers 9 eggs (question 13) |
| Nest bed holds 9 eggs | 3, 6 or 9 eggs at their real bed spots | To check in game | F1.8 | Bone `nest` and the mod bed shader (shaders/core/incubator_bed.*). Check that 9 eggs fit |
| Two-finger claw | Grip and turn the egg | Done | F1.8 | Bones `RoboticHand5` [2.99, 28, 10.5] and `RoboticHand6` [0.01, 28, 10.5], a mirror pair on `RoboticHand4` |
| Base, nest, lid, arm base, arm | Built part by part | Done | F1.8 | MOD/geo/block/incubator.geo.json bones `body`, `nest`, `cover`, `arm_base`, `RoboticHand1` |
| Lid (glass dome, lamp cap, two small tabs) | The lid lifts straight up while the screen is open or the arm works | Done | F1.8 | Bone `cover`. Code moves it by translation, so no hinge is needed. The two tabs do not change the lift |
| Lamp per bulb tier | Light gets brighter with each bulb upgrade: 9, 11, 13, 15 | No art needed | F1.8 | Adapted: the lamp cap is the top cube of `cover` (10 by 10 px). The 4 emissive textures show the tier. No new part |
| Lift post | No lift post: the lid lifts without one | No art needed | F1.8 | Not in the current model. Code moves `cover` straight up |

Arm link pivots:

- `RoboticHand1` [1.5, 19.04, -0.04]
- `RoboticHand2` [1.5, 21.5, 2]
- `RoboticHand3` [1.5, 28, 2.01]
- `RoboticHand4` [1.5, 28, 8.53]

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Container upgrade texture swap (6 and 9 eggs) | Every upgrade changes the look. No new part: a texture swap on an existing bone | Missing | F1.8 | None. The artist proposes the bone. Question 12 |
| Plant tank upgrade texture swap | Same | Missing | F1.8 | None. The artist proposes the bone. Question 12 |
| Emissive lamp texture: base lamp | Lamp glow per bulb tier on the lamp cap of `cover`. The built-in lamp is the base bulb. Light 9. Each tier glows in a colour of its own (D280) | Missing | F1.8 | None. The lamp changes with the bulb |
| Emissive lamp texture: warm bulb | Same. Light 11 | Missing | F1.8 | None |
| Emissive lamp texture: warmer bulb | Same. Light 13 | Missing | F1.8 | None |
| Emissive lamp texture: hot bulb | Same. Light 15 | Missing | F1.8 | None |
| Block texture | Machine look | Done | F1.8 | MOD/textures/block/incubator.png |

### Animations

All code-driven. Numbers are in backlog F1.8 "The arm" and spec F1.8 "Animations". Each row drives a bone of the current model.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Arm reach and egg turn | Turn each egg every 5 minutes | No art needed | F1.8 | Code-driven. See the notes below |
| Lid lifts and lowers | Up while the screen is open or the arm works. It is up before the arm reaches | No art needed | F1.8 | Code-driven. Moves `cover` straight up 12.5 px with a sine ease over 20 ticks, and lowers it the same way |
| Arm settle shake | Small shake after the arm turns | No art needed | F1.8 | Code-driven. A small shake of the `RoboticHand1` turn. `arm_base` stays still |
| Claw open and close | Grip the egg. Opens while the arm reaches, closes on the egg, opens at release | No art needed | F1.8 | Code-driven. Turns `RoboticHand5` and `RoboticHand6` about Y in opposite directions |

Arm reach and egg turn:

- `RoboticHand1` turns about Y.
- `RoboticHand2` and `RoboticHand3` make the two-link reach.
- `RoboticHand4` aims the hand and rolls the egg 180 degrees.

### Sounds

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Start | Heater turns on | Missing | F1.8 | None |
| Lid up and down | Lid lifts and lowers | Missing | F1.8 | None |
| Egg placed | Player puts an egg on the bed | Missing | F1.8 | None |
| Done | Egg ready | Missing | F1.8 | None |
| Fault: egg dies | No power or no plant matter for 5 minutes | Missing | F1.8 | None |
| Arm move | Arm reaches an egg | Ready to import | F1.8 | REPO machines/incubator/hydraulic_08.ogg (incubator sound) |
| Heater hum | Incubating | Shared, artist to confirm | F1.8 | REPO machines/_common/sounds/magnet/obj_magnet_hum_01.ogg, 02 (generic) |
| Fault: no power | Heater stops | Shared, artist to confirm | F1.8 | REPO machines/_common/sounds/obj_computerpowerdown.ogg (generic) |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Incubator base | Build the incubator | To check in game | F1.8 | MOD/textures/item/incubator.png. Check that it shows the base |
| Incubator nest | Same | Done | F1.8 | MOD/textures/item/incubator_nest.png |
| Incubator lid | Same | Done | F1.8 | MOD/textures/item/incubator_lid.png |
| Incubator arm base | Same | Done | F1.8 | MOD/textures/item/incubator_arm_base.png |
| Incubator arm | Same | Done | F1.8 | MOD/textures/item/incubator_arm.png |
| Incubator lift | No lift build step | No art needed | F1.8 | The current model has no lift part, so the build has no lift step. The video shows one (12:45) |

### GUI

Screen contents follow the video. Screen art is the current GUI texture in the mod.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Bed tint: green and red | Green where an egg can go, red where it cannot | Missing | F1.8 | None found |
| Bed fills with plants | The bed in the pane fills with plants as plant matter goes in, as in 1.12: bare wood, then dark green, then leaves | To check in game | F1.8 | MOD/shaders/core/incubator_bed.* draws this fill, but no code uses it: `IncubatorScreen` draws a plain texture. Check that it matches the 1.12 effect |
| Empty plant slot sprout icon | Plant slot when empty | To check in game | F1.8 | In MOD/textures/gui/incubator.png, bottom left of the bed pane beside the plant bar |
| Incubator screen | Leaf bed pane, egg markers, plant matter icon and bar | To check in game | F1.8 | MOD/textures/gui/incubator.png. Check in game that each item from the video is there |

**Changed since last review:** The lid lifts straight up with no lift post, and the build has no lift step. The lamp cap shows the bulb tier with 4 emissive textures. The claw opens and closes on each egg.

---
## Machine shared parts

Screen parts, sounds and looks that all four lab machines share. Each machine's own screen is listed under its machine.
Milestone: Fossil to egg.

**To make (5)**

- GUI (3): Pop-up slot dialog; Warning tab; Power symbol: powered and unpowered.
- Sounds (2): Part placed; Upgrade installed.

**To import (1)**

- GUI (1): Linked-machine tabs (4 machines, active and inactive).

**To fix (1)**

- GUI (1): Status bar on the left of each machine screen (check in game).

### GUI

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Pop-up slot dialog | "Insert Hard Drive", "Insert Genetic Material", "Collect Leftover Item" | Missing | F1.10 | None found in MOD/textures/gui |
| Warning tab | A stopped machine shows why: no power, no input, output full and more | Missing | F1.10 | None |
| Power symbol: powered and unpowered | Shows whether a machine has power, on its screen and on the status display above it | Missing | F1.10 | None. Layout: D278 |
| Status bar on the left of each machine screen | Green bar, red tick while running | To check in game | F1.10 | Not a separate file. Check whether each screen texture holds it |
| Linked-machine tabs (4 machines, active and inactive) | Change machines without leaving the screen | Ready to import | F1.10 | REPO guis/tabs/ (8 PNG). Not in the mod |

### Sounds

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Part placed | Player adds a part to a machine | Missing | F1.10 | None |
| Upgrade installed | Player right-clicks an upgrade onto a machine | Missing | F1.10 | None |

---

## Lab items

The small items of the cloning chain: syringes, test tubes, DNA labels, filters, chips, bulbs, drives, tanks and the fossil pouch. Machine part sprites are under each machine.
Milestone: Fossil to egg.

Not in Beta:

- No Identification Tag. Sequencing identifies amber DNA.
- No extra tiers: no netherite Processor tank, no emerald tank and no Ender SSD.
- No lysine item.
- No DNA label sprite on the tube. The DNA percent shows only in the tooltip.

**To make (6)**

- Sprites: syringes, tubes and labels (4): Syringe with dinosaur embryo; Test tube: Unidentified DNA look, Identified DNA look, Full DNA look.
- Sprites: upgrades and drives (1): Plant tank upgrades: iron, gold.
- Fossil pouch (1): Fossil pouch item.

**To import (2)**

- Sprites: upgrades and drives (2): Netherite tank upgrade; Leveling sensor.

**To fix (0)**

- Nothing.

### Sprites: syringes, tubes and labels

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Syringe with dinosaur embryo | Synthesis output | Missing | F1.6 | None in the mod. Today one embryo sprite serves both. Candidate: the 1.12 texture `Dumb-Code/ProjectNublar` branch `master`, src/main/resources/assets/projectnublar/textures/items/embryo_filled_syringe.png (D283) |
| Test tube: Unidentified DNA look | DNA state of an amber tube | Missing | F1.1 | None |
| Test tube: Identified DNA look | DNA state of a fossil tube | Missing | F1.1 | None |
| Test tube: Full DNA look | DNA state after synthesis | Missing | F1.1 | None |
| Lysine item | Lysine comes from food only: chicken and eggs | Not in Beta | F2.7 | None |
| Identification Tag item | Sequencing identifies amber DNA, so no tag | Not in Beta | F1.2 | None |
| Tag on a test tube (overlay) | Tagged tube look. No tag in Beta | Not in Beta | F1.2 | None |
| Tag on a syringe (overlay) | No tag in Beta | Not in Beta | F1.2 | None |
| Syringe, empty | Draw DNA from animals | Done | F1.2 | MOD/textures/item/syringe.png |
| Syringe with blood (DNA) | DNA from an animal | Done | F1.2 | MOD/textures/item/syringe_blood.png |
| Embryo Syringe (bird or turtle embryo) | Embryo from a chicken, parrot or turtle. Synthesis input. One item for all donors | Done | F1.2 | MOD/textures/item/syringe_embryo.png. The video shows this sprite as the bird embryo |
| Test tube, empty | Processor input | Done | F1.3 | MOD/textures/item/test_tube.png |
| Test tube with genetic material | Processor output | Done | F1.3 | MOD/textures/item/test_tube_full.png |

- **Amber tubes:** an amber tube always keeps the Unidentified look. Only the Sequencer drive names its species, so its DNA becomes Identified only on the drive.
- **Embryo Syringe tooltip:** the tooltip names the donor.

### Sounds

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Syringe draw | Syringe takes DNA or an embryo from an animal (F1.2). No sound of its own | No art needed | F1.2 | An existing vanilla sound plays. The animal hurt sound is vanilla |

### Sprites: upgrades and drives

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Plant tank upgrades: iron, gold | Incubator plant tank | Missing | F1.8 | Items exist in code, no sprite |
| Netherite tank upgrade | Sequencer tank 300% | Ready to import | F1.6 | REPO sprites/machine_upgrades/NetheriteTankUp |
| Leveling sensor | Removes the broken-egg chance | Ready to import | F1.7 | REPO sprites/machine_upgrades/LevelingSensor.png |
| Filters: iron, gold, diamond | Processor filter | Done | F1.3 | MOD/textures/item/iron_filter.png, gold_filter.png, diamond_filter.png |
| Computer chips: iron, gold, diamond | Faster machine | Done | F1.10 | MOD/textures/item/*_computer_chip.png |
| Bulbs: warm, warmer, hot | Incubator bulb | Done | F1.8 | MOD/textures/item/warm_bulb.png, warmer_bulb.png, hot_bulb.png |
| Hard drive | Sequencer drive | Done | F1.4 | MOD/textures/item/hard_drive.png |
| SSD | Faster drive | Done | F1.4 | MOD/textures/item/ssd.png |
| Tank upgrades: iron, gold, diamond | Processor and sequencer tanks | Done | F1.3 | MOD/textures/item/*_tank_upgrade.png |
| Container upgrades: small, large | Incubator 6 and 9 eggs | Done | F1.8 | MOD/textures/item/small_container_upgrade.png, large_container_upgrade.png |
| Drill extractor parts, tablet, tracking beacon | Not in Beta | Not in Beta | F1.12 | REPO sprites/machine_upgrades, tablet, tracking_beacon |

### Fossil pouch

The pouch has 27 slots.

- It picks up fossils by itself from any inventory slot and merges equal fossils.
- The player can also put fossils in by hand.
- Hoppers and the Fossil Processor can pull fossils out.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Fossil pouch item | Collect fossils | Missing | F1.1 | None |
| Fossil pouch screen (27 slots) | View the contents, take fossils out and put fossils in | No art needed | F1.1 | The vanilla chest layout (proposal, lead decides). If the lead wants its own look, this row becomes Missing |

**Changed since last review:** No Identification Tag or tag overlays. Amber tubes stay Unidentified until the Sequencer drive. No extra tank or SSD tiers. No lysine item. No DNA label sprite. Syringe draw uses a vanilla sound.

---

## Fossils and amber

Fossil ore in stone, sandstone and deepslate, amber ore, and the items they drop. The ore shows the bone piece and its quality before the player mines it. Fresh pieces from a dead dinosaur are here too.
Milestone: Fossil to egg (fresh drops and soft tissue: Dinosaurs, F2.1).

**To make (17)**

- Skins, ore overlays (7): Special piece: Tyrannosaurus tooth, Velociraptor toe claw, Triceratops horn; Generic pelvis; Generic foot (placeholder); Generic spine (placeholder); Quality looks: fragmented, poor, common, pristine (placeholder).
- Sprites: items (10): Fourth quality look for each piece; Generic pelvis item, 3 looks; Tyrannosaurus tooth: fossil item; Velociraptor toe claw: fossil item; Triceratops horn: fossil item; Tyrannosaurus tooth: fresh drop; Velociraptor toe claw: fresh drop; Triceratops horn: fresh drop; Triceratops teeth: fresh drop; Soft tissue.

**To import (15)**

- Skins, ore overlays (10): Generic arm; Generic leg; Generic ribcage; Generic tail; Generic neck; Skull: Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus, Gallimimus.
- Sprites: items (5): Generic fossil pieces, 3 looks each; Skull items, 6 species; Amber; Fresh bone items; Pelvis item: Tyrannosaurus.

**To fix (0)**

- Nothing.

### Skins and textures: ore overlays

Mod folder: MOD/textures/block/fossil_overlay/. The overlay sits on the vanilla stone texture.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Special piece: Tyrannosaurus tooth | Special piece | Missing | F1.1 | None |
| Special piece: Velociraptor toe claw | Special piece | Missing | F1.1 | None |
| Special piece: Triceratops horn | Special piece | Missing | F1.1 | None |
| Generic pelvis | Every species has a pelvis piece | Missing | F1.1 | Stand-in today: common/pelvis.png is 0 bytes |
| Generic foot | Every species has a foot piece | Placeholder | F1.1 | Stand-in: T. rex foot art. No generic foot in REPO _common |
| Generic spine | Every species has a spine piece | Placeholder | F1.1 | Stand-in: Microceratus spine art. No generic spine in REPO _common |
| Quality looks: fragmented, poor, common, pristine | Ore shows its quality | Placeholder | F1.1 | Stand-in: 2 looks for skulls only. Fragmented and poor share one |
| Generic arm | Other bones use generic art | Ready to import | F1.1 | REPO sprites/fossils_bones/_common (arm). Mod common/arm.png is T. rex art |
| Generic leg | Other bones use generic art | Ready to import | F1.1 | REPO _common (leg). Mod file is T. rex art |
| Generic ribcage | Other bones use generic art | Ready to import | F1.1 | REPO _common (ribcage). Mod file is Microceratus art |
| Generic tail | Other bones use generic art | Ready to import | F1.1 | REPO _common (tail). Mod file is Microceratus art |
| Generic neck | Every species has a neck piece | Ready to import | F1.1 | REPO _common (neck). Not in the mod |
| Skull: Velociraptor | Species skull piece | Ready to import | F1.1 | REPO sprites/fossils_bones/velociraptor/ |
| Skull: Dilophosaurus | Species skull piece | Ready to import | F1.1 | REPO sprites/fossils_bones/dilophosaurus/ |
| Skull: Triceratops | Species skull piece | Ready to import | F1.1 | REPO sprites/fossils_bones/triceratops/. The mod triceratops/ files are T. rex copies |
| Skull: Brachiosaurus | Species skull piece | Ready to import | F1.1 | REPO sprites/fossils_bones/brachiosaurus/ |
| Skull: Gallimimus | Species skull piece | Ready to import | F1.1 | REPO sprites/fossils_bones/gallimimus/ |
| Skull: Tyrannosaurus | Special piece with species art | Done | F1.1 | tyrannosaurus_rex/ (fresh, fragmented, rex_skull) |
| Amber ore | Amber | Done | F1.1 | amber/amber.png |
| Generic, trace and terracotta fossils | Not selected for Beta | Not in Beta | F1.1 | REPO blocks/fossils/textures/ |

### Sprites: items

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Fourth quality look for each piece | Beta has 4 qualities. REPO has 3 looks | Missing | F1.1 | Artist to map 3 looks to 4 qualities |
| Tyrannosaurus tooth: fossil item | Special piece item | Missing | F1.1 | None |
| Velociraptor toe claw: fossil item | Special piece item | Missing | F1.1 | None |
| Triceratops horn: fossil item | Special piece item | Missing | F1.1 | None |
| Tyrannosaurus tooth: fresh drop | Special drop | Missing | F2.1 | None |
| Velociraptor toe claw: fresh drop | Special drop | Missing | F2.1 | REPO _common has a sickle-claw leg, not a claw item |
| Triceratops horn: fresh drop | Special drop | Missing | F2.1 | None |
| Triceratops teeth: fresh drop | Special drop | Missing | F2.1 | None |
| Soft tissue | Remains give DNA | Missing | F2.1 | None |
| Generic pelvis item, 3 looks (fresh, fossilized, fragmented) | Pelvis item of every species except the T. rex | Missing | F1.1 | The artist draws one generic set |
| Generic fossil pieces, 3 looks each | Fossil items | Ready to import | F1.1 | REPO sprites/fossils_bones/_common (27 sprites: fresh, fragmented, fossilized) |
| Skull items, 6 species | Special piece items | Ready to import | F1.1 | REPO sprites/fossils_bones/<species>/ |
| Amber | Amber item | Ready to import | F1.1 | REPO sprites/amber/amber.png |
| Fresh bone items | Drop from a dead dinosaur | Ready to import | F2.1 | Fresh sprites in REPO sprites/fossils_bones/ |
| Pelvis item: Tyrannosaurus | T. rex pelvis item | Ready to import | F1.1 | REPO tyrannosaurus_pelvis.png |
| Skeleton exhibits | After Beta | Not in Beta | F1.1 | REPO assets/skeletons |

---

## Lab power

The Basic Generator and the power conduit that run the four lab machines and a first paddock.
Milestone: Fossil to egg.

**To make (8)**

- Models (1): Conduit block.
- Skins (2): Conduit texture; Basic Generator flame glow and smoke.
- Sounds (3): Generator start; Generator running loop; Generator stop or out of fuel.
- Sprites (2): Basic Generator item; Conduit item.

**To import (1)**

- Skins (1): Basic Generator texture.

**To fix (1)**

- Models (1): Basic Generator (convert).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Conduit block | Links machines for power and tabs | Missing | F1.11 | None. A thin cable block. Other mods' cables also work |
| Basic Generator | Burns furnace fuel | Needs conversion | F1.11 | REPO machines/basic_power_generator/models/tbl/power_generator_basic.tbl. The mod block has no model |

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Conduit texture | Conduit look | Missing | F1.11 | None |
| Basic Generator texture | Machine look | Ready to import | F1.11 | REPO machines/basic_power_generator/textures/basic_power_generator.png |
| Basic Generator flame glow and smoke | The flame glows while it burns. A smoke puff rises every 2 s. Light 13 | Missing | F1.11 | None. An emissive flame texture. The smoke can use the vanilla particle unless the artist makes a smoke texture |

### Sounds

Generators get sounds of their own.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Generator start | Fuel lit | Missing | F1.11 | None |
| Generator running loop | Running | Missing | F1.11 | None. REPO machines/_common/sounds/mechanical/ is generic |
| Generator stop or out of fuel | Stops | Missing | F1.11 | None. REPO obj_computerpowerdown.ogg is generic |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Basic Generator item | Inventory | Missing | F1.11 | None |
| Conduit item | Inventory | Missing | F1.11 | None |

### GUI

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Basic Generator screen | Fuel slot, flame, energy bar | Done | F1.11 | MOD/textures/gui/coal_generator.png |

**Changed since last review:** The Basic Generator gets a lit state: flame glow, smoke puff, light 13.

---

## Park power

The Large Generator and the pylons that carry power across a large park to the fences.
Milestone: Park.

- A pylon is a head on top of a fence pole.
- The player sets the height of a pylon pole.
- A fence post keeps its height of 3 or 8 blocks.

**To make (4)**

- Sounds (1): Pylon hum (placeholder).
- Sprites (2): Large Generator item; Pylon head item.
- GUI (1): Large Generator screen.

**To import (3)**

- Skins (3): Large Generator textures (base, small details, high-voltage sign); Large Generator green and red lights (glow); Pylon textures.

**To fix (3)**

- Models (2): Large Generator (convert); Pylon head (convert).
- Sounds (1): Large Generator running loop (confirm shared).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Large Generator | Park power | Needs conversion | F3.2 | REPO machines/advanced_power_generator/models/tbl/power_generator_advanced.tbl |
| Pylon head | Sits on top of a fence pole and carries power far | Needs conversion | F3.2 | REPO blocks/pylons/models/pylon_pole_head.tbl. Question 19 |
| Power line between pylons | Wire. A pylon cable behaves like a fence wire | No art needed | F3.2 | Drawn in code (ElectricWireRenderer). Acceptance is a code task in F3.2 |
| Pylon pole | The fence pole block serves as the pylon pole | No art needed | F3.2 | The fence post model serves. REPO pylon_pole.tbl is not used |
| Backup generator | Not selected for Beta | Not in Beta | F3.2 | None |

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Large Generator textures (base, small details, high-voltage sign) | Machine look | Ready to import | F3.2 | REPO machines/advanced_power_generator/textures/ |
| Large Generator green and red lights (glow) | The green and red lights blink while it runs | Ready to import | F3.2 | Same folder (green and red light PNGs). Use as emissive. Code makes them blink |
| Pylon textures | Pylon look | Ready to import | F3.2 | REPO blocks/pylons/textures/ (4 PNG) |

### Sounds

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Pylon hum | A hum loop near a pylon | Placeholder | F3.2 | Stand-in: the fence hum loop obj_electric_fence_lp.ogg |
| Large Generator running loop | Running | Placeholder | F3.2 | Stand-in: REPO machines/_common/sounds/mechanical/ (generic). Generators get sounds of their own (Machine sounds rule) |
| Large Generator ignite | Fuel starts to burn | Missing | F3.2 | Artist makes it |
| Large Generator fuel out | The last fuel burns out, the buffer runs down | Missing | F3.2 | Artist makes it |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Large Generator item | Inventory | Missing | F3.2 | None |
| Pylon head item | Inventory | Missing | F3.2 | None |

### GUI

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Large Generator screen | A screen of its own: fuel slots, flame, energy bar, line status | Missing | F3.2 | None |

**Changed since last review:** A pylon is a head on a fence pole. The REPO pylon pole is not used. Pylon cables behave like fence wires. Pylon hum added. Large Generator lights blink and it gets its own screen.

---

## Fences

The electric fences that keep dinosaurs in paddocks. 1:1 with the 1.12.2 fence: a low post 3 blocks high with 6 wires, a high post 8 blocks high with 8 wires. A switch cuts a section.
Milestone: Park.

**To make (9)**

- Models (2): Voltage sign hanging on a wire; Fence switch block.
- Skins (1): Fence switch texture.
- Sounds (4): Shock zap; Wire snap; Power-up hum; Power-down click.
- Effects (1): Spark particle (placeholder).
- Sprites (1): Fence switch item.

**To import (7)**

- Skins (2): Low post: lit and off; High post: lit and off.
- Sounds (1): Hum loop.
- Sprites (4): Wire spool fill stages 1 to 10 and empty; Fence remover (survival); Fence remover (creative); Voltage warning signs (3).

**To fix (0)**

- Nothing.

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Voltage sign hanging on a wire | Three signs hang on wires. Empty-hand clicks cycle them. A sneak-click removes the sign | Missing | F3.1 | None |
| Fence switch block | A switch or a redstone signal cuts a section. A vanilla lever on a metal post plate | Missing | F3.1 | None. A small new model |
| Low-security post | Fence | Done | F3.1 | MOD/geo/block/low_security_electric_fence_post.geo.json |
| High-security post | Fence | Done | F3.1 | MOD/geo/block/high_security_electric_fence_post.geo.json |
| Lamp parts on posts | Lamps light along the fence | Done | F3.1 | Bones `PoleLight` (low), `SecurityLightBulb`, `PoleLight1`, `PoleLight2` (high) |
| Decorative fence variants | Beta has no decorative fences | Not in Beta | F3.1 | None |
| Wire, straight, sagging and broken | Wire between posts. Cut ends hang bent | No art needed | F3.1 | Drawn in code. Today cut wires are invisible: a code task in F3.1 |
| Gates, perimeter and overhanging fence | Not selected for Beta | Not in Beta | F3.1 | REPO blocks/big_fence_gate, perimiter_fence |

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Fence switch texture | Lever and metal post plate | Missing | F3.1 | None |
| Low post: lit and off | Lamp on when powered | Ready to import | F3.1 | REPO electric_light_fence_lit.png, _unlit.png. The mod has one texture |
| High post: lit and off | Lamp on when powered | Ready to import | F3.1 | REPO high_security_fence_lit.png, _off.png. The mod has one texture |

### Sounds

The fence keeps the repair place sound of the 1.12.2 fence. It also gets the five sounds below.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Hum loop | Plays near a live fence | Ready to import | F3.1 | REPO machines/_common/sounds/obj_electric_fence_lp.ogg |
| Shock zap | Live wire shocks | Missing | F3.1 | None |
| Wire snap | A T. rex breaks a wire | Missing | F3.1 | None |
| Power-up hum | Plays when the fence gets power | Missing | F3.1 | None |
| Power-down click | Plays when the fence loses power | Missing | F3.1 | None |

### Effects

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Spark particle | Broken ends spark, shock sparks | Placeholder | F3.1 | Stand-in: vanilla electric spark. The original had its own spark sprite sheet |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Fence switch item | Inventory | Missing | F3.1 | None |
| Wire spool fill stages 1 to 10 and empty | The spool icon shows 10 fill stages | Ready to import | F3.1 | REPO sprites/cables/spool.png, Spool1.png, spool2..10.png, spoolempty.png |
| Fence remover (survival) | Survival recipe | Ready to import | F3.1 | REPO sprites/fence_removers/fence_remover.png |
| Fence remover (creative) | Creative tool | Ready to import | F3.1 | REPO sprites/fence_removers/creative_fence_remover.png |
| Voltage warning signs (3) | The three sign looks on wires. An empty-hand click cycles them. No sign item | Ready to import | F3.1 | REPO sprites/fence_signs/voltage_warning1..3.png |
| Low post item | Inventory | Done | F3.1 | MOD/textures/item/low_security_electric_fence_post.png |
| High post item | Inventory | Done | F3.1 | MOD/textures/item/high_security_electric_fence_post.png |
| Wire spool | Repair uses 1 spool | Done | F3.1 | MOD/textures/item/wire_spool.png |
| Paddock signs and enclosure markers | Not selected for Beta | Not in Beta | F3.1 | REPO blocks/paddock_sign |

**Changed since last review:** Five fence sounds added: hum, zap, snap, power-up hum, power-down click. Spool shows 10 stages. Signs hang on wires with no sign item. Switch looks like a lever on a post plate. No decorative fences.

---

## Feeders

In-ground feeder hatches that hold food. On a timer, the hatch opens and the food rises. Hungry dinosaurs eat from them. Each feeder fills by hand, hopper or pipe.
Milestone: Park.

#### Which model is which feeder

- Every feeder model is an in-ground hatch. In each model, y = 0 is the ground. Code draws the shaft below the hatch, with no blocks.
- Feeder 1: Small Carnivore Feeder.
- Feeder 2: Large Carnivore Feeder, with more feeding spots.
- Feeders 5, 3, 4 and 6: Low, Medium, Tall and Very Tall Herbivore Feeders, by the height of the food lift. Feeder 5 serves the Triceratops and the Gallimimus. Feeder 6 serves the Brachiosaurus.
- Feeders 3 and 4 ship in Beta.
- Live-Bait Feeder: Feeder 1 with a pole. New art. Code draws the chain.
- Fish Feeder: a cage. Not in Beta.

#### Close clip

Each feeder gets a close clip. No model has one yet. Until the artist makes it, code plays the open clip in reverse.

#### Screens

- Three screens in the lab GUI style of MOD/textures/gui/processor.png.
- The four herbivore feeders share one screen. The two carnivore feeders share one screen.
- Every screen has timer buttons for 2, 5, 10 and 20 min.

**To make (20)**

- Models (1): Live-Bait Feeder: model and texture.
- Animations (1): Close clip for Feeders 1 to 6.
- Skins (5): Feeder 2: empty and full; Feeder 3: empty and full; Feeder 4: empty and full; Feeder 5: empty and full; Feeder 6: empty and full.
- Sounds (3): Hatch open and close; Food drop; Bait release.
- Sprites (7): Feeder 2 (Large Carnivore Feeder) item; Feeder 3 (Medium Herbivore Feeder) item; Feeder 4 (Tall Herbivore Feeder) item; Feeder 5 (Low Herbivore Feeder) item; Feeder 6 (Very Tall Herbivore Feeder) item; Live-Bait Feeder item; Feeder 1 (Small Carnivore Feeder) item (placeholder).
- GUI (3): Herbivore feeder screen; Carnivore feeder screen; Live-bait feeder screen.

**To import (0)**

- Nothing.

**To fix (10)**

- Models (5): Feeder 2, Large Carnivore Feeder: model and texture (export); Feeder 3, Medium Herbivore Feeder: model and texture (export); Feeder 4, Tall Herbivore Feeder: model and texture (export); Feeder 5, Low Herbivore Feeder: model and texture (export); Feeder 6, Very Tall Herbivore Feeder: model and texture (export).
- Animations (5): Feeder 2: "Give me some meat" and "Idle" (export); Feeder 3: "Open Feeder" and "idle(open)" (export); Feeder 4: "Open Feeder" and "idle(open)" (export); Feeder 5: "Open Feeder" and "idle(open)" (export); Feeder 6: "Open Feeder" and "idle2" (export).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Live-Bait Feeder: model and texture | Feeder 1 with a pole. Code draws the chain to the animal that is out, like a lead | Missing | F3.3 | None. Adapt REPO Feeder 1/Feeder 1.bbmodel |
| Feeder 2, Large Carnivore Feeder: model and texture | Large carnivore hatch with more feeding spots | Needs export | F3.3 | REPO Feeder 2/ (bbmodel and PNG) |
| Feeder 3, Medium Herbivore Feeder: model and texture | Medium-height herbivore hatch | Needs export | F3.3 | REPO Feeder 3/ |
| Feeder 4, Tall Herbivore Feeder: model and texture | Tall herbivore hatch | Needs export | F3.3 | REPO Feeder 4/ |
| Feeder 5, Low Herbivore Feeder: model and texture | Low herbivore hatch for the Triceratops and the Gallimimus | Needs export | F3.3 | REPO Feeder 5/ |
| Feeder 6, Very Tall Herbivore Feeder: model and texture | Very tall hatch for the Brachiosaurus | Needs export | F3.3 | REPO Feeder 6/ |
| Feeder 1, Small Carnivore Feeder: model | Small carnivore hatch | Done | F3.3 | MOD/geo/block/carnivore_feeder_one.geo.json. REPO Feeder 1/Feeder 1.bbmodel |
| Fish Feeder | Aquatic species are Release 1 | Not in Beta | F3.3 | REPO Fish Feeder/ |

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Feeder 2: empty and full | Meat level | Missing | F3.3 | One texture in REPO |
| Feeder 3: empty and full | Plant level | Missing | F3.3 | Check REPO Feeder 3/ |
| Feeder 4: empty and full | Plant level | Missing | F3.3 | Check REPO Feeder 4/ |
| Feeder 5: empty and full | Plant level | Missing | F3.3 | One texture in REPO |
| Feeder 6: empty and full | Plant level | Missing | F3.3 | One texture in REPO |
| Feeder 1: empty and full | Meat level on the platform | Done | F3.3 | MOD/textures/block/feeder/meat/feeder_one/empty.png, full.png |

### Animations

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Close clip for Feeders 1 to 6 | The hatch closes after the feeding | Missing | F3.3 | None. Until it exists, code plays the open clip in reverse |
| Feeder 2: "Give me some meat" and "Idle" | The hatch opens and the meat rises | Needs export | F3.3 | REPO Feeder 2 bbmodel |
| Feeder 3: "Open Feeder" and "idle(open)" | The hatch opens and the potted tree rises 55 px | Needs export | F3.3 | REPO Feeder 3 bbmodel. Its closed "idle" has no motion |
| Feeder 4: "Open Feeder" and "idle(open)" | The hatch opens and the food rises | Needs export | F3.3 | REPO Feeder 4 bbmodel |
| Feeder 5: "Open Feeder" and "idle(open)" | The hatch opens and the food rises | Needs export | F3.3 | REPO Feeder 5 bbmodel |
| Feeder 6: "Open Feeder" and "idle2" | The hatch opens and a tall tree rises for the Brachiosaurus | Needs export | F3.3 | REPO Feeder 6 bbmodel |
| Feeder 1: open and idle | The doors slide apart and the meat platform rises 14 px. The Live-Bait Feeder uses the same clips | Done | F3.3 | MOD/animations/block/carnivore_feeder_one.animation.json `open`, `idle` |

### Sounds

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Hatch open and close | The feeder opens and closes | Missing | F3.3 | None |
| Food drop | Each bite | Missing | F3.3 | None |
| Bait release | One animal comes out on the chain | Missing | F3.3 | None |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Feeder 2 (Large Carnivore Feeder) item | Inventory | Missing | F3.3 | None |
| Feeder 3 (Medium Herbivore Feeder) item | Inventory | Missing | F3.3 | None |
| Feeder 4 (Tall Herbivore Feeder) item | Inventory | Missing | F3.3 | None |
| Feeder 5 (Low Herbivore Feeder) item | Inventory | Missing | F3.3 | None |
| Feeder 6 (Very Tall Herbivore Feeder) item | Inventory | Missing | F3.3 | None |
| Live-Bait Feeder item | Inventory | Missing | F3.3 | None |
| Feeder 1 (Small Carnivore Feeder) item | Inventory. One item per feeder | Placeholder | F3.3 | Stand-in: MOD/textures/item/carnivore_feeder_one.png is the empty block texture |

### GUI

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Herbivore feeder screen | Shared by the four herbivore feeders | Missing | F3.3 | None. Lab GUI style |
| Carnivore feeder screen | Shared by the two carnivore feeders | Missing | F3.3 | None. Lab GUI style |
| Live-bait feeder screen | Live-Bait Feeder | Missing | F3.3 | None. Lab GUI style |

- **Herbivore feeder screen:** 3 × 3 food slots, a green gauge, the food %, "Eating: X of Y" and the timer buttons.
- **Carnivore feeder screen:** 3 × 3 meat slots, a red gauge and the timer buttons.
- **Live-bait feeder screen:** pen count per animal type, interval buttons (2, 5, 10, 20 min), the animal on the chain and the next release.

**Changed since last review:** Every feeder is an in-ground hatch. Feeders 3 and 4 ship. Live-Bait Feeder is Feeder 1 with a pole and a code-drawn chain. Each feeder gets a close clip. Three shared screens with timer buttons.

---

## Digsites and villagers

Digsites with a large fossil deposit, paleontologist traders and loot. Hostile Paleo-Illagers hold some digsites. Maps lead to them.
Milestone: World and vehicles.

- **Structures:** a builder of the team makes the digsite structures and the paleontologist lab house. The 2020 builds are not used.
- **Workstation:** any lab machine is the paleontologist workstation, so no workstation block exists.
- **Paleo-Illager:** it gets its own skin on the vindicator model. Its guards are vanilla pillagers and vindicators.
- **Friendly camps:** a vanilla iron golem guards the camp. The golem holds its fossil like a poppy. The renderer draws the fossil item sprite, so it needs no new art.

**To make (7)**

- Models (4): Dig site buildings: exposed bones, quarries, sleeping tents, machine tents, trailers, storage boxes; Dig site sizes: empty, small, large with machines; Paleontologist lab building (default, spruce, desert); Paleo-Illager site.
- Skins (1): Paleo-Illager.
- Sprites (2): Digsite map item; Digsite map marker.

**To import (1)**

- Skins (1): Paleontologist villager.

**To fix (0)**

- Nothing.

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Paleontologist workstation block | No block: any lab machine is the workstation | Not in Beta | F4.1 | None |
| Dig site buildings: exposed bones, quarries, sleeping tents, machine tents, trailers, storage boxes | Digsite structures | Missing | F4.1 | A builder of the team makes them. The 2020 builds are not used |
| Dig site sizes: empty, small, large with machines | Size range | Missing | F4.1 | The team builder |
| Paleontologist lab building (default, spruce, desert) | Trader house in villages | Missing | F4.1 | No structure file. The team builder makes it |
| Paleo-Illager site | Illager camp around a deposit, guarded by pillagers and vindicators | Missing | F4.1 | The team builder |
| Petrified tree blocks and forest | Release 1 | Not in Beta | F4.1 | None |

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Workstation block texture | No block: any lab machine is the workstation | Not in Beta | F4.1 | None |
| Paleo-Illager | Hostile illager. Its own skin on the vindicator model | Missing | F4.1 | None. A retextured vindicator is a stand-in |
| Paleontologist villager | Trader | Ready to import | F4.1 | REPO misc/paleontologist/textures/Paleontologist.png (1.5 KB: check its quality) |
| Paleobotanist villager | Plants are Release 1 | Not in Beta | F4.1 | None |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Digsite map item | A map leads to a digsite | Missing | F4.1 | None |
| Digsite map marker | Icon on the map | Missing | F4.1 | None |
| Ancient plight effect icon | Not selected for Beta | Not in Beta | F4.1 | None |

**Changed since last review:** The team builder makes all digsite structures and the lab house. No workstation block. Paleo-Illager has its own skin. Friendly camps have an iron golem that holds a fossil.

---

## Vehicles

The Jeep Wrangler the player drives, and the 1992 Ford Explorer tour car that follows a track with stops.
Milestone: World and vehicles.

#### At a glance

- **Variations:** every variation ships. Dye tints the colours of a vehicle. A new Wrench item moves a vehicle to its next variation.
- **No clips:** code drives the wheels and the doors.
- **Tour track:** the track is a visible road-marker block. A chime plays at each stop. The stop marker lamp lights while an Explorer stands at the stop.
- **Assembly:** the player assembles each vehicle from parts, like a lab machine. There is no Jeep item and no Explorer item.

#### Assembly order

1. Body (sets the vehicle).
2. 4 wheels, one by one.
3. Seats.
4. For the Explorer only: Tour Computer.

- Wheels and seats serve both vehicles. There is no chassis part and no engine part.
- The body and each wheel show on the model. The Explorer seats show the `Salon` group. The Jeep seats and the Tour Computer have no model part.
- Asset basis: the REPO "without wheels" models and the Explorer `Wheels.bbmodel`. A developer splits each wheel into a group of its own. No new art.

#### Not in Beta

- Vehicle damage, wrecks and repair. They wait for a later release.

**To make (19)**

- Models (2): Tour track piece; Tour stop marker.
- Skins (3): Track piece and stop marker textures; Stop marker lamp: lit and unlit; Dye tint mask: Jeep and Explorer.
- Sounds (6): Engine idle; Engine drive loop; Horn; Door open and close; Crash hit; Tour stop chime.
- Sprites (8): Wheel item; Seats item; Jeep body item; Explorer body item; Tour Computer item; Tour track item; Stop marker item; Wrench item.

**To import (2)**

- Skins (2): Jeep textures (3); Ford Explorer textures (4).

**To fix (5)**

- Models (5): Jeep Wrangler (3 variations, wheels as separate groups) (export); Ford Explorer (4 variations, doors, seats, steering wheel, wheels) (export); Ford Explorer wheels (export); Jeep without wheels, 3 variations (export); Ford Explorer without wheels, 4 variations (export).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Tour track piece | Player lays the track: a visible road-marker block | Missing | F4.2 | None |
| Tour stop marker | Player sets stops. Crafted from a tour track and a redstone lamp | Missing | F4.2 | None |
| Wheel groups, one per wheel (both vehicles) | Each wheel shows when the player places it | Done | F4.2 | Developer bone split of the existing wheel cubes, no art change. REPO Explorer `wheels` groups plus `Wheels.bbmodel`, Jeep wheel groups |
| Jeep Wrangler (3 variations, wheels as separate groups) | Player vehicle. All 3 ship: roll bar, soft top, open. A Wrench cycles them | Needs export | F4.2 | REPO vehicles/wrangler_jeep/…/Jurassic Wrangles JY (1..3 Variation).bbmodel |
| Ford Explorer (4 variations, doors, seats, steering wheel, wheels) | Tour car. All 4 ship. A Wrench cycles them | Needs export | F4.2 | REPO vehicles/explorer_ford_XLT/…/Ford Explorer 1933 JP (1v..4v).bbmodel |
| Ford Explorer wheels | Tour car wheels. Each of the 4 wheels shows when the player places it | Needs export | F4.2 | REPO vehicles/explorer_ford_XLT/…/Wheels.bbmodel |
| Jeep without wheels (3 variations) | Assembly stage: the Jeep before its wheels show | Needs export | F4.2 | REPO vehicles/wrangler_jeep/Wrangles Jeep's (Without wheels)/…/Jurassic Wrangles JY (1..3 Variation).bbmodel |
| Ford Explorer without wheels (4 variations) | Assembly stage: the Explorer before its wheels show | Needs export | F4.2 | REPO vehicles/explorer_ford_XLT/Ford Explorer (Without wheels)/…/Ford Explorer 1933 JP (1v..4v).bbmodel |
| Gyrosphere, monorail, aircraft, boats, helicopters, FMTV truck and trailer | Not selected for Beta | Not in Beta | F4.2 | REPO vehicles/ |

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Track piece and stop marker textures | Track look | Missing | F4.2 | None |
| Stop marker lamp: lit and unlit | The lamp lights while an Explorer stands at the stop | Missing | F4.2 | None. A vanilla redstone lamp look can serve. The artist decides |
| Dye tint mask: Jeep and Explorer | Dye tints the colours of a vehicle | Missing | F4.2 | None. Ask: which parts take the tint (question 20) |
| Jeep textures (3) | Jeep look | Ready to import | F4.2 | One PNG in each REPO variation folder |
| Ford Explorer textures (4) | Tour car look | Ready to import | F4.2 | One PNG in each REPO variation folder |

### Animations

Code drives the wheel and door motion on the existing groups. No vehicle needs a clip.

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Jeep wheel spin and steering | Driving | No art needed | F4.2 | Code drives the wheel groups |
| Ford Explorer wheel spin and steering | Driving | No art needed | F4.2 | Code drives the wheel groups |
| Ford Explorer doors open and close | Get in and out | No art needed | F4.2 | Code swings the door groups |

### Sounds

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Engine idle | Vehicle on | Missing | F4.2 | None |
| Engine drive loop | Driving | Missing | F4.2 | None |
| Horn | Player horn | Missing | F4.2 | None |
| Door open and close | Get in and out | Missing | F4.2 | None |
| Crash hit | Collision | Missing | F4.2 | None |
| Tour stop chime | Plays at each tour stop, with "Stop 2 of 4" and the stop name | Missing | F4.2 | None |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Wheel item | Assembly part of both vehicles. 4 per vehicle, placed one by one | Missing | F4.2 | None |
| Seats item | Assembly part of both vehicles | Missing | F4.2 | None |
| Jeep body item | The body that makes the vehicle a Jeep | Missing | F4.2 | None |
| Explorer body item | The body that makes the vehicle a Ford Explorer | Missing | F4.2 | None |
| Tour Computer item | Last assembly part of the Explorer | Missing | F4.2 | None |
| Tour track item | Lay track. 16 per craft | Missing | F4.2 | None |
| Stop marker item | Set stops | Missing | F4.2 | None |
| Wrench item | A Wrench moves a vehicle to its next variation | Missing | F4.2 | None |

**Changed since last review:** Vehicles are assembled from parts. No Jeep or Explorer item. All variations ship, cycled by a Wrench. Dye tints vehicles. Stop marker lamp lights at the stop. No vehicle damage in Beta.

---

## Release 1 (listed only, not counted)

The things that Release 1 adds (backlog R01 to R03). Each row is a group of assets, not one asset. The rows get their own tables, with one row per asset, when Beta ships. Until then they count as "Not in Beta".

| Asset group | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Five plants: Cycad, Serenna Veriformans, West Indian Lilac, Heliconia, Horsetail (block models, textures, item sprites) | Plants grow in the world and on farms | Not in Beta | R01 | REPO plants/ (check) |
| Four wood sets and the petrified tree (logs, planks, stairs, slabs, fences, doors, trapdoors, signs) | Building blocks | Not in Beta | R01 | Names not set |
| Paleobotanist villager skin | Trades plants | Not in Beta | R01 | Missing |
| Compsognathus, Microceratus, Parasaurolophus, Mosasaurus: model, full clip list, complete skin sets per gender, own sounds, hatchling sounds, egg model and textures | Four new species | Not in Beta | R02 | REPO (check per species) |
| Fossil item sprites and ore overlays for the four species | Fossils to find | Not in Beta | R03 | Missing |
| Fish Feeder: model, hatch clip, textures, sounds, GUI | Feeds the Mosasaurus | Not in Beta | R03 | Missing |

---

## Guns (blocked, list only)

Some guns are in Beta, but the kinds are not decided (blocked on D08). Work is blocked. Not counted in the total.
Milestone: World and vehicles.

**To make (3)**

- Models (1): CODA Net Gun, SIG SG 552, 25mm Olin Flare Gun, M16A2.
- Animations (1): Tranquilised clip: Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus, Gallimimus.
- Sounds (1): Shot, reload, dart, taser.

**To import (2)**

- Skins (1): Shared gun textures (metal, details, reticles).
- Sprites (1): Ammo: dart, taser cartridge, shotgun shell.

**To fix (11)**

- Models (11): Ingen ACU Taser Rifle (export); Franchi SPAS-12 (export); GE M134 Minigun (export); DAN-Inject JM (tranquilizer) (export); DRD Tactical Paratus (export); Saab Bofors AT4 (export); Park Staff Electric Rod (export); LAR Grizzly Big Boar (export); Marlin Model 1895SBL (export); UTAS UTS-15 (export); Lindstradt Air Rifle (export).

### Models

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| CODA Net Gun, SIG SG 552, 25mm Olin Flare Gun, M16A2 | Guns List (marked finished) | Missing | F4.3 | No file in REPO |
| Ingen ACU Taser Rifle | Guns List (finished) | Needs export | F4.3 | REPO weapons/acutaserrifle |
| Franchi SPAS-12 | Guns List | Needs export | F4.3 | REPO weapons/spas12 |
| GE M134 Minigun | Guns List | Needs export | F4.3 | REPO weapons/m134minigun |
| DAN-Inject JM (tranquilizer) | Guns List | Needs export | F4.3 | REPO weapons/daninjectjm |
| DRD Tactical Paratus | Guns List | Needs export | F4.3 | REPO weapons/tactical paratus |
| Saab Bofors AT4 | Guns List | Needs export | F4.3 | REPO weapons/at4 |
| Park Staff Electric Rod | Guns List | Needs export | F4.3 | REPO weapons/electricrod |
| LAR Grizzly Big Boar | Guns List | Needs export | F4.3 | REPO weapons/grizzly |
| Marlin Model 1895SBL | Guns List | Needs export | F4.3 | REPO weapons/marlin1895 |
| UTAS UTS-15 | Guns List | Needs export | F4.3 | REPO weapons/utauts |
| Lindstradt Air Rifle | Guns List | Needs export | F4.3 | REPO weapons/old/lindstradt (old folder only) |

### Skins and textures

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Shared gun textures (metal, details, reticles) | Guns | Ready to import | F4.3 | REPO weapons/_common/textures/ (7 PNG) |

### Animations

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Tranquilised clip: Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus, Gallimimus | Tranquilizer guns, if they ship | Missing | F4.3 | Only the T. rex has one (REPO rex file `tranqed`, listed under Tyrannosaurus) |

### Sounds

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Shot, reload, dart, taser | Guns | Missing | F4.3 | None in REPO |

### Sprites

| Asset | Used for | Status | Feature | Where / reference |
| --- | --- | --- | --- | --- |
| Ammo: dart, taser cartridge, shotgun shell | Ammo | Ready to import | F4.3 | REPO sprites/guns_ammo_items/ |
