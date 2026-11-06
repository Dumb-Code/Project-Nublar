# Sound pipeline design: pull, convert, map and craft for the art list

Date: 2026-11-04. Status: approved by the lead on 2026-11-05.
Marks: **Lead** = the lead decided it. **My call** = the author chose it and the lead can overrule it. **Reading** = the author's reading of overlapping answers.

## Why

The art list has 161 pending sound rows: 98 Missing, 10 Placeholder, 45 Ready to import, 8 Shared. The art list says to source dinosaur sounds from Jurassic World Evolution 3 (JWE3). The earlier manual session (`Documents\Minecraft Modding\Sound Files\HANDOFF.md`) proved the extraction for T. rex only, and it stopped at the listening step. Its steps are manual, and one bank takes many commands.

This tool turns the pending list into sound files and data that a developer can drop into the mod. A developer then wires them during F2.3 and the machine features.

## What the lead does

1. Runs one command.
2. Optional, per species: approves YouTube clips or records the species in the JWE3 Species Viewer. The tool matches them against that species' sounds.
3. Opens one local page. Each pending row shows ticked picks, the other candidates and any video evidence. A row tied to a clip plays with the clip and has a timing slider per sound. The lead plays them, swaps picks and approves the row.
4. Runs one command. A staging folder then holds the approved rows, ready to implement.

## Goals

- Read pending rows from `docs/plan/art-list.md` on each run, so that new rows appear and Done rows drop out.
- Pull and decode from three sources: JWE3, Prehistoric Kingdom (PK) and the asset repo.
- Find candidates per row from motion graph links and names, and pick 3 to 5 variants automatically.
- Clean each pick to the mod's sound rules.
- Edit, blend, layer and time each clip's sounds to that clip, and bring them back for review when the clip changes (see "Clip fit" and "When a clip changes").
- Report every pending row in the result: staged, gap or blocked. No row goes missing silently.

## Non-goals

- No change to the mod tree, `SoundInit.java`, `sounds.json`, `en_us.json`, `docs/plan/*` or the shared plan tabs. The tool only proposes them.
- No git commit in the Project Nublar repo.
- No wiring in game. The tool writes sound keyframe data for a developer or the artist to add. Connecting effect names to sound events, and server clip events, stay F2.3 work.
- No music or character voice lines: PK `Music` and `VO_Characters` banks are skipped (**My call**).

## Anti-goals (a failure even if the tool runs)

- A hatchling sound made by pitching up an adult (art list rule).
- A sound from another species counted as final. It can only be a Placeholder (art list rule).
- A row reaching staging without the lead's approval (**Lead**).
- Audio leaving this PC. The review page stays local and is never published as an Artifact (**My call**).

## Sources, as measured on 2026-11-04

| Source | Where | How it opens | Names |
|---|---|---|---|
| JWE3 creature banks | Copies in `Sound Files\JWE3 Sounds`. Install: `D:\SteamLibrary\steamapps\common\Jurassic World Evolution 3\Win64\ovldata\Content0\Audio` | Cobra Tools, then `pipe.py` logic, then vgmstream | Hashed: FNV-1 32-bit of the lowercased name. Motion graphs give the real names |
| JWE3 park banks | Same install: `UI`, `building*`, `_sharedBuildings`, `Vehicles`, `Weapons`, `Scenery`, `Zipline`, `Global` | Same | Hashed, with no known word source. UI 0 of 1,201 and buildingGeneric 0 of 444 decoded. Motion graphs do not help here |
| PK banks | `C:\Program Files (x86)\Steam\steamapps\common\Prehistoric Kingdom\Prehistoric Kingdom_Data\StreamingAssets` (19 pairs also copied to `Sound Files`) | vgmstream reads `.assets.bank` directly. A test file decoded to 48 kHz mono | Readable in the FSB5 name table: 9,664 unique names |
| Asset repo | `Documents\Minecraft Modding\ProjectNublar-Assets\assets` | Ogg already | File names, as the art list rows give them |

JWE3 names, from female and juvenile motion graphs plus the engine name list, counted against the events the game registers for each species' own bank:

| Species | Registered events named | Young sound names | Footstep name found |
|---|---|---|---|
| Tyrannosaurus | 130 of 153 | 48 | `TheropodLarge_FootPlantRear` |
| Velociraptor | 134 of 182 | 89 | `TheropodSmall_FootPlantRear` |
| Dilophosaurus | 74 of 83 | 67 | `TheropodSmall_FootPlantRear` |
| Triceratops | 41 of 54 | 68 | `Cerapod_FootPlantFront`, `Cerapod_FootPlantRear` |
| Brachiosaurus | 32 of 39 | 32 | `Sauropod_FootPlantFront`, `Sauropod_FootPlantRear` |
| Gallimimus | 66 of 114 | 82 | None found yet |

Male and variant graphs are not measured yet. The first slice measures them.

JWE3 footsteps are one system in `sharedCreatures`. A `<Group>_FootPlantFront` or `<Group>_FootPlantRear` event sets a switch to the animal type, then plays one root object that also reaches other sounds. The sounds under each type's switch branch:
- Theropods: 140. The small, medium and large events reach the same 140 sounds, confirmed by comparing the lists.
- `Cerapod`: 72.
- `Sauropod`: 61.

These sounds are the footstep sounds that the earlier notes listed as "not linked to any event". Ground type within a branch is not decoded. The branch is not all footsteps: one sampled file runs 10.5 s, so a length filter is needed. The events also send a different number per animal, which probably sets size; this is not confirmed. Theropod footsteps may need per-species pitch or filter changes in `craft`, so that a T. rex does not step like a raptor.

Examples of PK names: `Trex_GrowlCourtshipA_08`, `Trex_VoxYawn_03`, `Trex_BreathSleepIn_04`, `Tricera_GrowlBroadcastA_01`, `Galli_VoxYawnSleep_04`, `Dilo_Baby_VoxCall_02`, `C_Medium_Footstep_Grass_06`, `Sauro_Sneeze_01`, `TheroLarge_DrinkSip_01`, `Amb_Incubation_Loop_01`, `Destroy_Fence_Large_Metal_Break_Impact_01`.

## Permissions

The lead approved these on 2026-11-04 for this tool: JWE3 creature banks for the 6 Beta species and shared banks, JWE3 park and UI banks, the 19 PK banks and the rest of the PK install. Motion graph reads from the JWE3 species archives are part of JWE3 name decoding, as in the earlier session. Other games' files stay out. Each YouTube audio download needs the lead's approval.

## Motion graph links: the main method for dinosaur rows (Lead)

Each species' motion graph lists which sounds each animation clip plays. Examples:
- `TyrannosaurusRex$Drink02` plays `TheropodLarge_drink_large_scoop_begin`.
- `TyrannosaurusRex$Partial_Roar` plays `Trex_HeroRoar_Pre`.
- `Velociraptor$Partial_Unconscious` plays `Velociraptor_BreathSleep_In`.

**Reading the graph**
- Extract `Content0\Dinosaurs\Land\<Species>\<Gender>\<Species>_<Gender>.ovl` with `ovl_tool_cmd.py extract`, with no `--type` (`--type motiongraph` gave no graph file in the test). Keep only the `.motiongraph` file.
- The file is plain XML. A `data` element holds a clip and its streams: `mani` with `additional_data_streams`, `coordinated_anim` with `coordinated_anim_data_streams`, or `waiting_anim` with `waiting_anim_data_streams`.
- Each `datastreamresourcedata` holds `ds_name`, `type`, `location` and `curve`. `AudioEvent` and `AudioLoopingEvent` are sounds. `AudioRTPC` and `AudioBlend` are treated as control values (**My call**).
- Read female, male, juvenile and variant graphs, such as `Triceratops93` and `BrachiosaurusJWE` (**My call**).
- The first number in each `curve` point looks like a position within the clip, but this is not confirmed. The tool keeps it in `provenance.json` and shows it on the page as a hint only (**My call**).

**Using the links**
- Each row type lists clip words and sound name words. Examples: drink uses clip word `Drink`; roar uses `Partial_Roar`; hurt uses `React`.
- A candidate ranks highest when both its clip and its sound name fit the row, then by how often those clips play it. A clip name alone is not enough: `$EatToStand` plays a growl as well as eating sounds.
- Sounds in juvenile graphs fill hatchling rows only when their name has `Juv`.
- `Hatchery` names do not fill hatchling rows. `TyrannosaurusRex$HatcheryExit_01` is in the female graph and plays adult roar sounds.
- No clips named yawn, sneeze or mating were found in the female graphs tested. Those rows use Prehistoric Kingdom or edits unless a named sound such as `TRex_Yawn` fits.

**Banks to pull**
- Each species' own banks, its group bank (see the alias table), `sharedCreatures` and `_landShared`.
- `TheropodSmall_Shared`, `TheropodHerbivore_Shared`, `Cerapod_Shared` and `Sauropod_Shared` are new. Copies are in `Sound Files\JWE3 Sounds`.

## Engine names and the registry check

Graphs may leave out sounds that the engine starts. Community source: the JWE3 Audio Kit (ViraLCyclopes, GitHub) lists these name endings: `FootPlantFront`, `FootPlantRear`, `PredatedKilled`, `PredatedSurvive`, `Deathroll`, `DeathRoll`, `Footstep`, `Death`, `Hurt`. The tool tries them, plus `Die`, on species and group names. The kit is not installed or run; the tool only uses this list and the registry finding.

The registry is `Sound Files\metadata_extracted\wwisemetadatasoundbanks.wmetasb`. It is XML with hashes for 15,834 events in 212 non-empty banks, and no names. A bank's hash is the FNV-1 of its name, such as `TRex_events`.
- **Name check:** a guessed name counts only if its hash is in the registry. The match also shows which bank holds it. Example hits: `Velociraptor_Die`, `Dilophosaurus_Die`, `Gallimimus_DeathRoll`.
- **Chance hits:** each guess pass reports its expected false hits: guesses × 15,834 ÷ 2^32. In the test, 566,820 guesses had about 2 expected false hits out of 28. The page marks these names as guessed.
- **Coverage:** `report.md` counts named events against registered events.
- **Registered or not:** the kit says the game never plays an event that is missing from the registry. This is not verified. The page marks such events "not in registry", and it keeps them as candidates (**My call**).

Reading banks: `pipe.py` misses two cases that the test met. Switch containers can hold switch containers, and an event's sound can sit in another bank's media. The test scripts in `Sound Files\tools\pipeline\spike\` handle both (`build_chains.py`). The scripts are throwaway tests, kept as a reference for the builder.

## What counts as a species' own sound (Lead, Reading on the combination)

A sound counts as the species' own if any one of these rules is true. The lead picked the first two, and the third comes with the video stage:

- **The game plays it on that species.** The species' JWE3 motion graph links the shared set to a clip, voice or not. Examples on T. rex: `TheropodLarge_GrowlGulp`, `TheropodLarge_drink_large_scoop`. The `map` stage keeps these links per species.
- **It has no voice.** Footsteps, body falls and splashes from shared sets count, in JWE3 and in PK. Example: `C_Medium_Footstep_Grass_06`. The tokens for "no voice" are in the rows-to-names file.
- **Video confirms it.** A shared set matches a clip of that species. See the video stage below.

Graphs may leave out sounds that the engine starts, such as footsteps and death. So a missing graph link does not prove the game leaves a sound out. A shared voice set for such a row, such as `TheropodLarge_StandDie` on the T. rex death row, is marked "game use unknown" and not stand-in, unless one of the rules above makes it the species' own. A species-named engine sound such as `Velociraptor_Die` is the species' own by its name.

Any other shared or group sound is a stand-in. Example: Prehistoric Kingdom `Sauro_Sneeze_01` on a Brachiosaurus row, because it has a voice and is named for a group, not for the species.

## Which pick starts ticked (Lead, with one Reading)

| Row status | Ticked first |
|---|---|
| Missing or Placeholder, JWE3 own sound matches | JWE3 |
| Missing or Placeholder, no JWE3 match | PK, own sound |
| Ready to import | Asset repo file |
| Ready to import, Triceratops or Brachiosaurus death | Blended chain (**Lead**, see "Death rows") |
| Ready to import, Velociraptor or Dilophosaurus death | Lighter blends (**Lead**, see "Death rows") |
| Shared, artist to confirm | Asset repo file (**My call**) |

- **Reading:** the lead picked both "JWE3 when its name matches" and "Asset repo on its 42 Ready rows". The second option's text says JWE3 is ticked only on Missing and Placeholder rows, so Ready rows start on the repo file.
- **Lead:** PK sounds that overlap a JWE3 row can serve as more variants. The tool offers same-species PK matches as extra variants on a JWE3 row. The page marks a row with mixed sources as "two voices", and the lead judges it by ear. They are not ticked by default (**My call**).
- Every other candidate stays on the page, one click away.

## Rows with no direct recording (Lead)

All options appear as candidates. The order only decides which one starts ticked:

1. Same-species edit from a related sound: a part cut from a longer take at its silences, or two takes joined. Each row type lists its related sounds (**My call**, editable): snort from breath or growl; yawn from breath; sneeze from snort only.
2. Stand-in from a shared or group set. Group order per species is in the alias table below.
3. Leave Missing. The row goes on the gap list.

**Reading:** the lead picked both "related sounds only" and "any same-species take". So related cuts can start ticked. Cuts from any other take of that species appear on the page as candidates, and they never start ticked. Example: Triceratops sneeze starts on the stand-in `Cera_Sneeze` unless a `Tricera_` snort exists, and a cut from a Triceratops growl is one click away.

## Layering (Lead)

Layering builds a new sound from two or more recordings. The lead asked to hear samples first.
- **Open:** the dinosaur death rows, by the recipes below, and other dinosaur rows tied to a clip (see "Clip fit"). Egg and machine rows, because the lead judged the layered egg crack and generator start better than the raw sounds (2026-11-05). `build_steps.py` in the spike folder made those two samples.
- **Locked behind a setting:** every other row, including fences.

Layered non-animal rows stay Placeholder until the artist replaces them. Layered dinosaur rows are To check in game.

## Death rows: blended chains (Lead)

The lead heard plain chains of a JWE3 death clip's sounds and liked the sick, pain and fall sounds, but they played separately with no cohesion. The lead then picked the strongest of three blends for each species: `Sc` (T. rex, led by the sad call), `Tc` (Triceratops) and `Bc` (Brachiosaurus). Sample files are in `Sound Files\audition`, and `build_blend.py` in the spike folder made them.

| Species | Voice, in order | Falls, as a share of the mod's `dead` clip |
|---|---|---|
| T. rex | `Trex_SadCall`, `Trex_BreathWheeze` | `Fighting_BodyFall_Heavy` and `dinosaurMedium_Body_RestFall_dly` at 90% |
| Triceratops | `Triceratops_PainCryLong`, `Triceratops_SickGrunt`, `Cerapod_Hiss`, `Cerapod_BreathSick` | `dinosaurLarge_Body_RestFall_dly` at 63%, `Fighting_BodyFall_Medium` at 93% |
| Brachiosaurus | `Brachiosaurus_PainCryLong`, `Brachiosaurus_SickCall`, `Brachiosaurus_SickGrunt`, `Sauropod_Sigh` | `dinosaurLarge_Body_RestFall` at 34%, `Fighting_BodyFall_Heavy` at 69%, `Sauropod_HeadImpact_Large_dly` at 75%, `Fighting_BodyFall_Impact_Large_Single` at 99% |

The recipe, as the samples used it (start values; the builder keeps them in one data file):
1. Trim each voice part's silence and match its level.
2. Pull each voice part toward the average tone of all parts: 24 bands, at most 6 dB per band, at 70% strength.
3. Join the voice parts with equal-power overlaps of up to 0.35 s. Fade the parts from 0 dB down to -10 dB, first to last. On a long clip, add more takes of the last breath until the voice reaches the final fall.
4. Add the species' breath quietly under the whole voice, 16 dB below the voice parts.
5. Drop the voice's pitch slowly, from 100% to 93% across its length. Fade its last 35%.
6. Place each fall at its share of the mod's clip. Duck the voice by 4 dB for 0.3 s at each fall.
7. Put the whole sound through one short shared space (0.7 s decay, -13 dB) and one compressor (-20 dB, 2.5:1, attack 15 ms, release 200 ms).

Death row picks:
- **T. rex (Lead):** starts on `Sc`, with the plain sad call sets `Trex_SadCall_Short` and `Trex_SadCall` as extra variants. The Distortus Rex copies now in the mod are dropped from this row.
- **Triceratops and Brachiosaurus (Lead):** the lead compared the blends with the asset repo death files by ear and picked the blends. These two rows start on the blend, even though they are Ready to import. The repo files stay as candidates.
- **Brachiosaurus length:** the `Bc` samples run 10.5 to 12.3 s, past the art list's limit of about 10 s for a clip that plays from memory. The tool uses fewer extra sighs to end under 10 s (**My call**). The lead hears the shorter version on the review page.
- **Velociraptor and Dilophosaurus (Lead):** these rows use a lighter recipe, picked by ear on 2026-11-05. The lead rejected the full recipe above ("so much going on", "over the top"), the plain recordings and the asset repo files. Both picked forms start ticked as variants. The repo files stay as candidates. Samples are `Vx`, `Vy`, `Dx` and `Dy` in `Sound Files\audition`, and `build_dying_calls.py` in the spike folder made them.
  - **Forms:** the death sound overlapping into the call, and the call overlapping into a last breath. The call is `Velociraptor_SickCall` or `Dilophosaurus_SadCall`. The breath is `Velociraptor_BreathSick_Out` or `TheropodSmall_BreathSick_Out`.
  - **Recipe (start values):** trim and match each part's level. Tone match the two parts. Join them with the same overlap as above. In the first form the call is 3 dB under the death sound; in the second the breath is 8 dB under the call. Drop the pitch from 100% to 85%. Fade the last 60%. Add one space (0.7 s decay, -20 dB). Set the level with plain gain, because a loudness filter can lift the fading tail. No compressor and no falls.
- **Gallimimus:** the mod has no `dead` clip, so the row has no clip timing for falls.

JWE3's own death clips last 5.27 s (T. rex), 4.87 s (Triceratops) and 7.67 s (Brachiosaurus), read with Cobra Tools from the `.manis` files. The curve values look like shares of the clip, because the falls land near the end. This is not confirmed.

## Clip fit (Lead; details are My call)

Every sound tied to a clip is edited, blended, layered and timed to that clip, and the lead judges it with the clip playing. On 2026-11-05 the mod's 6 species files hold 146 clips (T. rex 26, Velociraptor 29, Dilophosaurus 23, Gallimimus 16, Triceratops 23, Brachiosaurus 29). Only 3 Brachiosaurus clips have sound keys.

**Rows to clips.** Art list sound rows name no clip, and JWE3 clip words do not match the mod's clips: the hurt clip here is `flinch`. So one data file maps each row type, per species, to its mod clips, play mode, JWE3 clip words and how the sound starts. A row with no clip says `none`. A missing entry is an error, so no row skips this stage by accident. Play modes start from `AnimationChoice`, because code overrides the clip file: T. rex `eat1` has no loop in the file but plays looped, and `check` reports when the two differ. `fit` runs on pending rows and on every row in the manifest, so an imported row still comes back when its clip changes.

**What the `fit` stage does for each clip**
1. **Start layout.** It takes the mapped JWE3 clip's sounds from the motion graph, in order, with their curve positions as start values. These positions are not confirmed as shares of the clip, so the lead's ear decides. A clip with no JWE3 match starts from the row's sound at its anchor.
2. **Edit.** It trims, cuts, joins, overlaps and layers the sounds to the mod's clip length. Each species and row type has its own recipe in one data file. No recipe carries over by default: the death recipe that suited the big species was "over the top" on the small ones.
3. **Time.** It stores each sound's time as an anchor plus an offset (see below), and it writes `sound_keyframes.json`.
4. **Clean.** The cleaning rules run last, on the fitted file.
5. **Render.** It renders the clip with its sound for the review page. The lead moves each sound with a slider, and the page saves the offset.

**Clip length.** The tool uses the clip's stated `animation_length`. 18 of the 146 clips have none. The builder confirms from GeckoLib's source what length those play at, and the tool uses the same rule.

**Anchors.** An anchor is a moment measured from the clip, so that it can be measured again when the clip changes:

| Anchor | Signal | Which one, if there are several | Used for |
|---|---|---|---|
| Foot landing | The end of the foot's fast drop, as `settle_probe.py` in the spike folder measures it. A time stopped by its 0.12 s cap is marked "capped", because it is not a measured landing | Per foot | Footsteps |
| Jaw | A change of direction in the jaw bone's rotation | Counted in order within the clip (T. rex `roar` has 166 jaw keys, so the data file stores the number) | Bites, roars, calls |
| Body drop | The body bone's fastest fall, then its stop | Counted in order. The data file names the bone, because T. rex keys both `root` and `hips` | Falls, in death and sit clips |
| Share of the clip | Clip length | None | Breath, and anything with no clear motion |

An offset is the lead's by-ear change, kept apart from the measured moment. If the number of moments changes, or a clip no longer has the signal, the tool does not guess. It uses the share of the clip and marks the sound "anchor unclear". An anchor counts as moved at 10 ms or more (**My call**). The builder can add anchors if a row needs one.

**How each sound starts.** Each sound in the manifest is `key` or `code`:
- **`key`:** a sound key in the clip starts it, at the stored time.
- **`code`:** the game starts it, and a key would play it twice. All 7 T. rex events work this way today: `ThreatDisplay` and entity hooks such as `getDeathSound`. F2.3 starts roars and calls from server clip events. The time goes into the file as lead-in, so the sound still lands on its moment.
- **Variants:** one key plays one file picked at random, so all variants in a set must hit at the same point. The tool trims or pads each file's start to line up their hits.

**Baked sounds.** A blend is one file, so a slider cannot move part of it by shifting the whole file. Each part of a blend has its own anchor or share. Moving a part's slider changes its offset and rebuilds the file. A fall uses the body drop anchor where the clip has one, or the recipe's share if not.

**Shared sounds.** A sound cut or baked to a clip belongs to that clip and has its own event. A set used by several clips or species is only trimmed, never cut to one clip: T. rex `eat1` and `eat2` differ in length, and `entity.carnivore.eat` serves several species. An index links each file to its clips and rows.

**Rules**
- **No time-stretching.** Sounds are trimmed or joined. A slow pitch drop is allowed only in a recipe the lead picked by ear, as in the death recipes.
- **Death rows:** length does not change ranking. The lead picked sounds that run past the clip for their dramatic effect, and the `dead` clip holds its last frame.
- **Other one-shot clips:** a sound should end near the clip's end. It can ring on only when the clip holds its last frame. Looping clips need sounds that repeat cleanly at the loop point.
- **Rows with no clip** get no fit stage. Gallimimus death is an example.
- **Layering is open** for dinosaur rows tied to a clip. Candidates still start from the row's picks, and a layered row is To check in game.

**Limits**
- The renders have no leg IK, and in game clips play at varying speed. The in-game check during F2.3 is the final one.
- Rendering took about 10 minutes for 12 clips of about 8 s each at 1280×720 in parallel. One run through all 146 clips (511 s) would take about 50 minutes at that rate, so the tool renders only clips that are new or changed.
- Rendering a machine's clips is not tested. The first slice's Carnivore Feeder finds out what it needs. The Incubator has no clips, so its rows are audio only.
- The feeder's hatch close sound is fitted to `open` in reverse (Lead, 2026-11-05), from the clip's last motion. The art list's rule is that code plays the reverse until a close clip exists, but that code is F3.3 work: today's hatch snaps shut. The lead judges this sound on the render until F3.3.

## When a clip changes (Lead; details are My call)

Any edit or remake of a clip or model sends its sounds back through `fit`. No sound stays approved against a clip that changed.

**How the change is found**
- `clip_fit_manifest.json` holds, per clip: a fingerprint, the measured anchors, the offsets, how each sound starts, the sound files, the expected sound keys (time, effect, event) and the lead's decision. A row with no clip has an entry with `clip: none`.
- The fingerprint hashes the whole clip, including its `loop` value and every key form, with sorted keys and fixed-decimal numbers, plus the species' model file. It leaves out the sound keys, so adding keys does not set it off. Sound keys are compared on their own, against the expected keys.
- The baseline lives in `Sound Files\work\`, and `staged\` holds only a copy. A blocked row therefore keeps its baseline.
- `fit` and `check` compare every clip against the manifest on each run.
- **A warning in the repo (Lead):** the artist does not run this tool, so a check in the mod's tests compares each clip and its sound keys with the manifest that a developer imports. It warns and names the clip when a clip changed with no new fit, when a key is missing, or when a clip has no entry. It does not fail the build, so a clip change never blocks other work. The lead clears a warning by running the tool, approving by ear and importing the new manifest. One shared test vector keeps the Python and Java fingerprints equal. This check is F2.3 work, and it enters the plan with the build plan.

**What happens next**

| Change | What the tool does |
|---|---|
| Keyframes edited, same length | Measures the anchors again, applies the stored offsets, and rebuilds baked sounds, because a fall may have moved |
| Length changed | Same, moves share anchors with the length, and rebuilds sounds cut to the clip, such as tails |
| Model changed | Measures the anchors again and applies the stored offsets |
| Clip renamed or remade under a new name | Marks the old name "clip gone" and the new name "no fit". It shows the pair as a likely match, and it moves nothing until the lead accepts |
| Clip removed | Marks its rows "clip gone" |
| Clip added, such as Gallimimus `dead` | A row with `clip: none` that the row map now links to this clip is marked "clip arrived, no fit". It goes through `fit` |
| Sound keys missing or different after a re-export | `check` reports them. Whether a Blockbench export keeps sound keys is not tested |

In every case the tool renders the clip again, removes the row's approval and marks it "clip changed, listen again". `report.md` lists it as blocked until the lead approves it, and staging keeps no file cut for that clip. A shared file stays while another approved clip still uses it. If no anchor moved and the length is the same, the page says so, and one click approves it again. A remake can change a motion's feel, not only its timing, so the stored offset is a start value and the lead's ear decides.

**Sound keys in the clips.** The tool does not write into the mod. It writes a file of each clip's keys for the developer or artist, and that file replaces the clip's existing keys rather than adding to them. The Brachiosaurus clips already have 4 keys each, so adding would give 8 steps. F2.3's footprints and dust hang off footstep events, so a lost key also loses those effects.

## Footsteps (Lead on source and matching; details are My call)

**Source (Lead, by ear).** These sets count as each species' own sound, because footsteps carry no voice:

| Species | Starts ticked | Also on the review page |
|---|---|---|
| Velociraptor, Dilophosaurus, Gallimimus | Prehistoric Kingdom `TheroSmall_Footstep_Walk`, `TheroSmall_Footstep_Run`, lighter version | Plain version. For Gallimimus, also `HerbSmall_Footstep` |
| T. rex | Prehistoric Kingdom `TheropodLarge_Footstep` | JWE3 pool, marked "two voices" |
| Triceratops | Prehistoric Kingdom `Cera_Footstep` | JWE3 pool, marked "two voices" |
| Brachiosaurus | Prehistoric Kingdom `Sauro_Footstep` | JWE3 pool, marked "two voices" |

The lighter version is a little higher in pitch, with less low end, and quieter. In the samples: pitch 115% and low end -6 dB for Velociraptor, 105% and -4 dB for Dilophosaurus, 110% and -6 dB for Gallimimus, all 6 dB quieter. On the big species, the lead keeps both sets to pick per row. The JWE3 pool starting second is the author's order (**My call**).

**Matching the animation.** The lead wants steps to match each clip's footstep rate. There is no fixed rate to match: `MovementGait` plays leg clips at 0.2 to 2.0 times normal speed, from the animal's ground speed, so the planted feet keep pace with the ground. So a step must start from a keyframe in the clip at the moment the foot lands, and it then follows the clip at any speed. F2.3 already plans this. Today only Brachiosaurus has sound keyframes, and `Dinosaur`'s handler plays nothing. Sounds are not retimed, and clips are not retimed for sound.

**Step start times (Lead, by ear).** Each time is when the step's sound file starts, at normal clip speed. The data file is `Sound Files\tools\pipeline\spike\foot_contacts_v3.json`.

| Species | Walk clip: step times (gaps), s | Run clip: step times (gaps), s |
|---|---|---|
| T. rex | 2.25 long: 0.794, 1.858 (1.06, 1.19) | 1.00 long: 0.097, 0.590 (0.49, 0.51) |
| Velociraptor | 0.96 long: 0.311, 0.601 (0.29, 0.67) | 0.71 long: 0.042, 0.407 (0.37, 0.34) |
| Dilophosaurus | 1.33 long: 0.267, 0.936 (0.67, 0.66) | 1.00 long: 0.286, 0.790 (0.50, 0.50) |
| Gallimimus | 1.88 long: 0.902, 1.870 (0.97, 0.91) | 0.79 long: 0.096, 0.589 (0.49, 0.30) |
| Triceratops | 3.00 long: 0.688, 1.556, 1.812, 2.263 (0.87, 0.26, 0.45, 1.43) | `Sprint ` 0.92 long: 0.115, 0.449, 0.812, 0.909 (0.33, 0.36, 0.10, 0.12) |
| Brachiosaurus | 4.04 long: 0.413, 1.271, 2.215, 3.461 (0.86, 0.94, 1.25, 0.99) | 2.79 long: 0.291, 1.698, 2.007, 2.437 (1.41, 0.31, 0.43, 0.65) |

How the times were set:
- **Velociraptor, Dilophosaurus, T. rex, Gallimimus walk (Lead):** the lead moved each clip's steps by ear on a slider page, then confirmed the rebuilt videos. Each clip's feet moved by one shared amount, from -150 to +140 ms against the measured foot landing.
- **Gallimimus run (Lead):** the measured landing, kept by ear.
- **Triceratops and Brachiosaurus (Lead):** hard to judge in the renders. The measured landings are start values, set by ear in game during F2.3. A measured landing is the end of the foot's fast drop, with a shift of at most 0.12 s from the first method. On these slow feet many times stopped at that cap, so they are marked "capped" and are not true landings.

Limits of these times:
- The renders have no leg IK, and in game the clips play at 0.2 to 2 times speed. The in-game check during F2.3 is the final one.
- The gaps come from how the feet move in the clips. The uneven beats on the Velociraptor walk, Gallimimus run and Triceratops sprint stay, unless the artist changes those clips.
- **The times belong to the sound sets the lead tuned them with:** each species' Prehistoric Kingdom set, plain and untrimmed, with `HerbSmall_Footstep` for Gallimimus. A file's hit can come well after its start: T. rex run steps rise most steeply about 0.11 s in. A different set, or a trimmed start, moves the hit against the foot.
- **Gallimimus on its picked set:** the `TheroSmall` lighter walk hits about 40 ms later than `HerbSmall`, so its walk steps start 40 ms earlier than the table: 0.862 and 1.830 s. Its run differs by about 4 ms, and the lighter versions on Velociraptor and Dilophosaurus by about 10 ms, so those times stay. The lead has not heard this Gallimimus change against the feet.

**What the tool does.** It stores each time as the measured foot landing plus the lead's offset (`foot_contacts_v2.json` and `foot_nudges.json` in the spike folder), so a changed clip keeps the lead's offset (see "When a clip changes"). It writes the times as `footstep_keyframes.json`, for a developer or the artist to add to the clips. When `craft` trims the start of a step file, it moves that step's time later by the trimmed amount, so the hit stays on the foot. Variants in a set are aligned to one hit time (see "Clip fit"), because one key plays a random file. It trims each step's tail to run only a little past the next step at normal speed, with a fade. Separate walk and run sets are used where the source has them.

## Unnamed sounds (Lead)

- **Grouping:** unnamed sounds are clustered, so the lead hears one per group. A wrong group can hide a good sound, so a group can be opened on the page.
- **Sound-alike ranking:** unnamed JWE3 sounds are compared with named ones using the video matcher, and the closest come first. It is untested for this use.
- **Building archives:** tried and closed. A capped read of 107 of 165 JWE3 building archives found 0 of 3,365 park names, because they hold mostly textures and models. `Main.ovl` was not checked.

## Proposed statuses

The tool only proposes a status. A status applies when a developer imports the file, because a staged file is not in the mod yet.

| Pick | Proposed status |
|---|---|
| JWE3 own sound | To check in game |
| PK own sound | To check in game, with the note "Prehistoric Kingdom: replace if JWE3 gets a match" |
| Same-species edit | To check in game |
| Stand-in | Placeholder |
| Shared voice set, game use unknown | Placeholder, with the note "game use unknown". It starts ticked at the stand-in step. If the lead marks it as the species' own on the page: To check in game (**My call**) |
| Asset repo, Ready to import row | Ready to import. To check in game after import |
| Asset repo, Shared row | Shared, artist to confirm |
| Non-animal row from game files, layered or not | Placeholder |
| Layered dinosaur row | To check in game |
| Leave Missing | Missing |

**Reading:** for PK, the lead picked both "To check in game, counts as real" and "Placeholder until JWE3 fills it". So a PK pick plays as the row's sound and gets an in-game check. If a later run finds a JWE3 match, the page marks the row so the lead can swap it.

## Species aliases (My call, editable)

| Species | JWE3 bank and name prefix | JWE3 group bank | PK prefix | PK stand-in groups, nearest first |
|---|---|---|---|---|
| Tyrannosaurus | `TRex`, `Trex_` | `TheropodLarge_Shared` | `Trex_` | `TheroLarge_`, `TheropodLarge_`, `C_` |
| Velociraptor | `Velociraptor` | `TheropodSmall_Shared` | `Velo_` | `TheroSmall_`, `C_` |
| Dilophosaurus | `Dilophosaurus` | `TheropodSmall_Shared` | `Dilo_` | `TheroSmall_`, `C_` |
| Triceratops | `Triceratops` | `Cerapod_Shared` | `Tricera_`, `Trike_` | `Cera_`, `C_` |
| Brachiosaurus | `Brachiosaurus` | `Sauropod_Shared` | `Brachio_` | `Sauro_`, `Sauropod_`, `C_` |
| Gallimimus | `Gallimimus` | `TheropodHerbivore_Shared` | `Galli_` | `OrnithoSmall_`, `HerbSmall_`, `C_` |

PK `Tyranno_` (78 files, including sneezes) may or may not be Tyrannosaurus. It is offered on T. rex rows as a stand-in, with its name shown, until the lead decides by ear.

## Rows to names (My call, editable)

One data file maps each row type to a sound class, clip words and name tokens per source. Rows with no useful graph link use the tokens alone. For example:

- mating call: `Courtship`, `Mating`
- hatchling: `Baby`, `Juv`. Only these tokens count: no adult sound may fill this row.
- drink: `Drink`; sleep: `Sleep`, `Snore`; footstep: `Footstep`, `Step`, `FootPlant`; sneeze: `Sneeze`; yawn: `Yawn`.

Nest call has no matching name in any source so far. It starts as a same-species call candidate, proposed To check in game.

Non-animal rows get tokens by hand: for example heater hum to `Incubation_Loop`; fence wire snap to `Fence`, `Break`. JWE3 park sounds have no names, so they appear on these rows as unnamed candidates, sorted by length, and they never start ticked. Non-animal rows filled from game files are Placeholder, because the art list says the artist makes machine sounds (**My call**).

Rows and sound events (**My call**):
- One row can hold several sounds. "Growl (7), hiss, snort, exhale" is four sounds. "Spit launch and spit hit" is two.
- Each sound has its own class, tokens, 3 to 5 variants and one event id. Subtitles follow the sound, not the row.
- Approve covers the whole row.
- Two rows can feed one event: Triceratops "Hurt 1" and "Hurt 2" both go to `entity.triceratops.hurt`.
- A set shared across species or machines gets one id of its own: `entity.carnivore.eat`, `block.machine.power_down`.

## Cleaning (art list rules D349, D350)

Every staged file:
- Trims silence and fades the ends.
- Is mono, because every pending row plays from a place in the world.
- Meets a loudness target for its class, with true peak at or below -1 dBFS, measured after the Ogg encode, because Vorbis can overshoot.
- Is Ogg Vorbis at about 64 kbps mono (**My call**; the builder measures the size).

Sound classes come from the art list: roar, call, bite, footstep, breath, machine, fence. Other rows join the nearest class: hurt and death with call, eat and drink with breath, and so on (**My call**).

Loudness targets are start values in one file. The lead tunes them by ear in playtest, and a re-run applies them. Start values (**My call**): roar -14, call -16, bite -16, footstep -22, breath -24, machine -22, fence -24 LUFS. For reference, today's T. rex files have a median of -11.5 LUFS, and one PK test file measured -22.8. Very short clips may need a different loudness measure; that is the builder's choice.

Long loops (hum, engine) need a clean loop point: the builder crossfades the ends.

## What the staging folder holds

`Sound Files\staged\`:
- `assets\projectnublar\sounds\...` : the Ogg files in the mod's layout.
- `sounds.fragment.json`: new entries. No `"stream": true` under about 10 s (D349). Block, machine and fence entries get pitch and volume variants in the file; dinosaur entries do not, because code varies them (D351). Subtitle keys follow D352.
- `en_us.fragment.json`: `subtitles.projectnublar.<event>` lines for roars, calls, threat sounds, hurt and death only (D352).
- `sound_events.txt`: new event ids for a developer to add to `SoundInit`. The 7 T. rex ids stay as they are, because `SoundInit` calls them frozen.
- `footstep_keyframes.json`: step times per walk and run clip.
- `sound_keyframes.json`: every other approved sound's time per clip, with its effect name and event id.
- `clip_fit_manifest.json`: per clip, the fingerprint, anchors, offsets, sound files and decision. A developer imports it with the sounds, for the repo warning.
- `art_list_status.md`: proposed status and "Where" text per row.
- `provenance.json`: per file, the source game, bank, stream or wem id, edits and loudness.
- `report.md`: staged, gap and blocked rows, rule checks, and size per species and in total.

New ids (**My call**): `entity.<species>.<group>.<name>` and files at `entity/<species>/<group>/<name>_<n>.ogg`, following the T. rex layout. Non-animal rows use `block.<thing>.<name>`. The T. rex folder name stays `tyrannosaurus_rex`.

The 7 existing T. rex events (**My call**):
- The ids stay. `sounds.fragment.json` carries full replacement entries for them, with no `"stream": true` and D352 subtitle keys.
- New files use the same folders and `<name>_<n>` names, so each staged event is a complete set.
- `report.md` lists old files that the new set no longer uses, such as `roar_6..10`, for a developer to delete.
- Proposed status: Placeholder, with the note "D91 open", because the plan keeps these rows Placeholder until the lead rules on D91.

Size: the earlier estimate was 5 to 8 MB per dinosaur with 3 to 5 variants. It is not measured yet. The report flags a species over 10 MB, and there is no hard cap (**My call**).

## Video evidence stage (Lead; details are My call)

This stage is the backup for sounds with no graph link: unnamed events and engine sounds with unknown game use. Graph links already show game use, including quiet sounds. A match shows that a sound plays in the real game, and when. The earlier session identified one unnamed sound this way: `_landShared` `unnamed_1892928313`, matched in a kills clip. The stage is optional, and it stays outside `all` because each clip needs the lead's approval.

**Clips**
- A candidate list goes to the lead, with URL, channel, length, audio size and the behaviour each clip shows.
- The lead approves each clip before download and sets its behaviour label. Chapter times can give several labels to one clip.
- `yt-dlp` downloads audio only, to a new empty folder under `work\`. The audio is deleted after matching, and only the small result file stays.
- JWE2 and JWE1 clips are allowed, as they were for T. rex, because a match proves the same recording is in that clip.
- The lead can supply a local recording from the JWE3 Species Viewer with music off. It needs no download and avoids the mix limit below. The tool does not delete it.
- The 10 saved T. rex result files in `Sound Files\video_check` are reused.
- The lead kept three extras for this stage: sandbox recordings with music and ambience off, music stripping (Demucs) before matching, and the unused New Baby T-Rex Showcase clip (1:10:10, about 61 MB). Each download still needs approval when it is reached.

**Matching**
- Reuse the `match.py` method: log-mel cross-correlation, pitch steps up to ±10%, and a chance level from reversed sounds.
- `match.py` reads a T. rex-only list today. The new stage takes one species' JWE3 sounds that have no graph link, including its unnamed events.
- Prehistoric Kingdom sounds are not matched, because their names are readable.

**What a match does**
- It shows on the review page as evidence: clip, time and match strength. Example: "heard in kills clip at 0:49".
- A shared set matched on that species' clip counts as the species' own.
- An unnamed sound with a match appears as an unticked candidate on the rows tied to the clip's behaviour label.
- It never ticks a pick by itself, and it never names a sound.

**What no match means**
Nothing. The matcher finds loud sounds and misses sounds 6 dB or more under the mix. On T. rex, 10 clips confirmed 25 of 153 sets. A row with no match stays as it was.

## The review page (My call on the form)

- A small local Python server on 127.0.0.1 serves one page, with no outside requests. A clip link opens in the browser only on a click. Each click saves to `decisions.json` on disk. The earlier `listen.html` kept tags in browser storage and needed manual exports, and no export was ever made.
- Rows are grouped as in the art list, with a filter by status and by "not yet approved".
- Each row shows: art list text, status, ticked variants, all candidates with play, source tag, name, length, video evidence, and the gap, "two voices" or "game use unknown" mark. An "Approve" button.
- Playback uses the cleaned files, so candidates compare at equal loudness.
- Each candidate shows its clip fit. Death rows also list a blended chain as a candidate.
- A row tied to a clip plays the clip's video with its sounds, at normal and slow speed, with a timing slider per sound or blend part, as `Sound Files\audition\nudge.html` does for footsteps. The final check plays a video with the sound built in, because live playback on a page can drift.
- A row whose clip changed shows "clip changed, listen again", and whether any anchor moved.
- No choice is ever asked from names alone. A decision about a sound always comes with the sound to play (**Lead**).
- A row with no ticked variant can be approved as "Leave Missing".
- A re-run keeps earlier decisions unless the source file or the clip's fingerprint changed. The page then marks the row for another look.

## Shape of the tool (My call)

- Lives in `Sound Files\tools\pipeline\`, beside the Cobra Tools venv and vgmstream. It has its own local git repo and is never pushed.
- Python 3.12 with numpy and Pillow, ffmpeg (on PATH, has libvorbis), vgmstream `r2117`, Cobra Tools at `Sound Files\tools\cobra-tools_src`, and `tools/concept-render/dust_trail.py` from the repo for clip renders.
- Commands: `pull`, `convert`, `map`, `craft`, `fit`, `video`, `review`, `stage`, `check`, plus `all` for the first five.
- Tools also include `yt-dlp` for approved clips. Downloaded audio is untrusted data: it goes in its own new folder, and any Python that reads it runs with `-I`.
- Paths live in one config file. Stages cache by source hash, so a re-run redoes only what changed. Already-extracted T. rex and shared banks are reused.
- Run any Python that reads extracted files with `-I`. Keep scripts apart from extracted data.
- `pipe.py` and `group3.py` hold working logic but have hardcoded folders. Reuse the logic, not the scripts as they are.
- Work data goes in `Sound Files\work\`. Decoded audio is not copied into the Nublar repo.

## Done means

- `check` passes on all staged files: mono, Vorbis, true peak at or below -1 dBFS, no stream under 10 s, each fragment entry has its file and each file has its entry, ids unique, valid JSON.
- Unit tests cover: art list parsing against the current row counts (161 pending), FNV hashing, FSB5 name reading, motion graph link reading, the registry check, pick order, hatchling and species rules.
- Every pending row appears in `report.md`.
- First slice, for the lead to judge before the rest: T. rex, the Incubator (audio only) and the Carnivore Feeder, end to end, with the review page and the clip fit stage. It also measures what male and variant graphs add.
- Unit tests also cover the fingerprint, with its shared test vector, and all 7 cases in the "When a clip changes" table, on copies of clips and models. Each case must come back marked, with its approval removed.
- `check` also passes on sound keys: each expected key is present, with no extras. Every row in the row map has an entry or `none`.
- In the first slice, a changed copy of a T. rex clip comes back on the review page with its sounds moved, and it cannot reach staging until approved.
- The T. rex death row is built with the death recipe, and it reproduces the `Sc` sample closely enough for the lead's ear.
- After the first slice (step 4 of the build plan's outline), the layered egg crack and generator start reproduce the samples the lead approved, closely enough for the lead's ear.
- The `video` stage finds a planted sound in a test mix (the earlier `plant.py` control) and loads the 10 saved T. rex result files. The review page shows that evidence in the first slice.

## Left to the builder

The order of work after the first slice, module layout, candidate ranking, cut detection, the variant choice inside a row, how to use unnamed JWE3 events on the page, and the loudness measure for very short clips.

## Known limits

- The tool maps by graph links, names and measurements. Nobody has listened to these picks. A name or clip can mislead, so the review page is the real check.
- JWE3 park names stay unsolved, so non-animal rows rely on Prehistoric Kingdom names and on listening.
- The lead judged the blends by ear on a page, not yet in game with the clips.
- Plan item D91 (the real T. rex recordings) is open. The tool only proposes statuses, so that item stays with the lead.
- The art list says machine and fence rows wait for the artist's own sounds. Game-file sounds on those rows are stand-ins.
- Video will probably confirm only a minority of sounds. It adds evidence, and it does not replace the lead's listening.

## Not decided

These options were offered on 2026-11-04. They are untested and not part of this design unless the lead adds them:

- A local audio-tagging model (CLAP) that searches unnamed park sounds by text, such as "electric buzz".
