# Sound Pipeline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A local Python tool that turns JWE3, Prehistoric Kingdom and asset-repo sounds into reviewed, cleaned, clip-timed sound files and data for the Project Nublar mod, with T. rex, the Incubator and the Carnivore Feeder built end to end as the first slice.

**Architecture:** One Python package, `nublar_sounds`, with one module per stage (`pull`, `convert`, `map`, `craft`, `fit`, `video`, `review`, `stage`, `check`) and JSON files in `work\` as the contract between stages. Pure functions carry the proven spike logic (Wwise bank walking, FSB5 names, motion graph links, blend recipe, foot landing, nudge page). A local review server on 127.0.0.1 is the only place a decision is made, and `stage` copies only approved rows.

**Tech Stack:** Python 3.12, numpy, Pillow, pytest; ffmpeg and ffprobe on PATH; vgmstream `r2117` at `Sound Files\tools\vgmstream`; Cobra Tools at `Sound Files\tools\cobra-tools_src` run through its venv `Sound Files\tools\venv\Scripts\python.exe`; `tools/concept-render/dust_trail.py` from the Nublar repo for clip renders; a static HTML page with vanilla JS.

**Spec:** `docs/superpowers/specs/2026-11-04-sound-pipeline-design.md` (the design; every section). Also read `docs/superpowers/handoffs/2026-11-05-sound-pipeline-handoff.md`, sections "Do not redo", "Facts that replace older notes", "Lessons learned" and "How to reopen and rerun". Where they conflict, the design wins.

## Paths used in this plan

| Name | Path |
|---|---|
| `SF` | `C:\Users\kgumm\Documents\Minecraft Modding\Sound Files` |
| `TOOL` | `SF\tools\pipeline` (the tool, its own git repo) |
| `SPIKE` | `TOOL\spike` (throwaway reference scripts; never imported, never run by the tool) |
| `WORK` | `SF\work` |
| `STAGED` | `SF\staged` |
| `REPO` | `C:\Users\kgumm\Documents\Minecraft Modding\Project-Nublar` |
| `ASSETS` | `C:\Users\kgumm\Documents\Minecraft Modding\ProjectNublar-Assets\assets` |
| `JWE3_AUDIO` | `D:\SteamLibrary\steamapps\common\Jurassic World Evolution 3\Win64\ovldata\Content0\Audio` (copies of the needed banks are in `SF\JWE3 Sounds`) |
| `JWE3_DINOS` | `D:\SteamLibrary\steamapps\common\Jurassic World Evolution 3\Win64\ovldata\Content0\Dinosaurs\Land` |
| `PK` | `C:\Program Files (x86)\Steam\steamapps\common\Prehistoric Kingdom\Prehistoric Kingdom_Data\StreamingAssets` |
| `PY` | `TOOL\.venv\Scripts\python.exe` (made in Task 1) |

Commands in this plan run from `TOOL` unless they say otherwise. Every run of the tool is `PY -I -m nublar_sounds <stage> [options]`, and every test run is `PY -I -m pytest tests -q`. `-I` works because Task 1 installs the package into the venv; the package is never run from a script path.

## Global Constraints

Every task's requirements include these lines. Values are from the design unless a source is named.

- No rework (the lead, 2026-11-05). Act only on work that addresses an unfulfilled need, requirement, issue or gap. Before you start a step, a fix or a dispatch, name the unmet need and check that it is still open. If the need is already met, say so and skip the step. A finding counts only if it names a real unmet requirement or real breakage.
- Tool code lives in `TOOL` with its own local git repo. It is never pushed. Commits go only to that repo. Nothing is committed in `REPO` by this plan except Task 27, and that task commits nothing without the lead's word.
- Work data goes in `WORK`. Staging goes in `STAGED`. No decoded audio enters `REPO`. Nothing writes into the mod tree (`REPO\common\src\main\resources`). `REPO` is read only, except the files Task 27 lists.
- Any Python that reads extracted or downloaded files runs with `-I`. Scripts stay in `TOOL`; data stays in `WORK`, `SF\JWE3 Sounds`, `SF\*extracted` and the game installs.
- Never ask the lead to choose a sound from its name. A decision about a sound comes with the sound to play, and a clip-tied sound comes with its video.
- No row reaches `STAGED` without the lead's approval in `WORK\decisions.json`, saved by the review page. Audio stays on this PC. The review page is never published as an Artifact. The server binds 127.0.0.1 only and makes no outside request.
- Do not download a YouTube clip, use an audio-tagging model, or read `Main.ovl`. Each needs the lead's word. The `video` stage in this slice only runs the planted-sound control and loads the 10 saved result files in `SF\video_check`.
- Nobody but the lead judges a sound. An agent measures (length, LUFS, true peak, hit time, gap, match strength) and reports the numbers. Every success check that depends on the ear says "the lead judges".
- Commands over about a minute run in the background (`run_in_background`), and their output is read when they finish.
- When something falls outside this plan, the implementer stops and reports `NEEDS_CONTEXT` with the question. No improvising.
- Implementers run on Opus. Each task's reviewer is a fresh agent on Opus, never Sonnet.
- Sources allowed: JWE3 creature banks for the 6 Beta species and the shared banks, JWE3 park and UI banks, the 19 PK banks and the rest of the PK install, the asset repo, motion graphs from the JWE3 species archives. Nothing from any other game.
- Hazards in `SPIKE`: `build_blend.py` deletes `blend_*.ogg` at module level; `mux_clips.py` `main()` deletes everything in `audition\clips\`; `build_steps.py` `main()` deletes `step_*.ogg` and `layer_*.ogg`. No tool code imports or `exec`s a spike script. Logic is carried over by rewriting it.
- Art list rules: no hatchling sound made by pitching an adult; a sound of another species is at most a Placeholder.
- Cleaning (D349, D350): mono; true peak at or below -1 dBFS after the Ogg encode; no `"stream": true` under about 10 s; one loudness target per class. Start targets: roar -14, call -16, bite -16, footstep -22, breath -24, machine -22, fence -24 LUFS.
- Ids: `entity.<species>.<group>.<name>`, files `entity/<species>/<group>/<name>_<n>.ogg`; the T. rex folder is `tyrannosaurus_rex`; non-animal rows `block.<thing>.<name>`. The 7 existing T. rex ids stay as they are (`SoundInit` calls them frozen): `entity.tyrannosaurus_rex.vocals.roar`, `.vocals.growl`, `.ambient`, `.attack.snarl`, `.attack.bite`, `.hurt`, `.death`.
- No time-stretching. A slow pitch drop is allowed only inside a recipe the lead picked by ear.

## Questions for the lead

None open. Decisions the design left open are written into the tasks as rules: the Incubator is audio only (Task 8, Task 23), all JWE3 park banks are pulled in this slice (Task 23), the Carnivore Feeder is the slice's machine clip (Task 24), the hatch close sound fits `open` in reverse (the lead, 2026-11-05; Task 24), and the Java check reads the manifest from test resources (Task 27).

## Review Focus

Inputs the design implies but no task's tests would otherwise exercise, most likely to bite first. Each has its test pinned to the owning task.

1. An art list row whose asset text contains backticks, pipes inside backticks or a trailing space (`Sprint ` is a clip name with one): the parser must keep the text exactly and never split a row on a pipe inside backticks. Task 2.
2. A JWE3 event whose media sits in another bank (`pipe.py` missed this), and a switch container inside a switch container: `takes()` must still return audio. Task 4.
3. A clip with no `animation_length` (T. rex `misc.idle`, `restidle`, `turn_left`, `turn_right`): `fit` must use the GeckoLib rule and never crash or write a length of 0. Task 16.
4. The review server given a POST for a row id that does not exist, a body over 200 KB, or a GET path with `..`: it answers 400 or 404 and writes nothing. Task 13.
5. A second run with no changes: decisions stay, no row is marked "listen again", and `stage` writes byte-identical output. Task 22.

Also pinned: an Ogg whose true peak overshoots after encode is re-encoded lower until it passes (Task 11), and the chance-hit count is reported with every guess pass (Task 3).

## File structure

```
TOOL\
  pyproject.toml                 name nublar_sounds; deps numpy, Pillow; dev pytest
  config.toml                    every path in "Paths used in this plan", plus ports
  .gitignore                     .venv/, spike/, __pycache__/, *.pyc
  nublar_sounds\
    __init__.py
    __main__.py                  argparse: pull convert map craft fit video review stage check all
    config.py                    Paths: load config.toml, check that each folder exists
    cache.py                     source-hash cache: a stage step re-runs only when its inputs' hashes changed
    artlist.py                   pending rows from docs/plan/art-list.md
    fnv.py                       FNV-1 32-bit of the lowercased name
    registry.py                  wwisemetadatasoundbanks.wmetasb: event hash -> bank; name check; chance hits; engine-ending guesses
    cobra.py                     ovl extract through the Cobra venv, cached per archive
    wwise.py                     Bank: HIRC objects, event -> takes (wem ids), wem lookup across banks and loose folders
    vgm.py                       vgmstream: wem -> wav; .assets.bank stream -> wav; wav -> float32 mono 48 kHz
    fsb.py                       FSB5 stream names from .assets.bank bytes
    motiongraph.py               clip -> sound links from .motiongraph XML
    rowmap.py                    data files: species aliases, rows-to-names, rows-to-clips; row classification; validation
    candidates.py                map stage: candidates per sound, own-sound rules, ticked picks, marks
    audio.py                     trim, level, tone_match, join, droop, room, fades, loop crossfade, hit time, longest gap
    recipes.py                   death blend; layered machine sounds; footstep sets and sequences
    clean.py                     mono, loudness per class, Ogg encode, true-peak check after encode
    craft.py                     craft stage: variants per sound, aligned hits, provenance
    clips.py                     mod clip and model files, entity and block subjects; clip length rule; play mode; Catmull-Rom and per-key-mode sampling; bone matrices
    anchors.py                   foot landing, jaw, body drop, share
    fingerprint.py               canonical clip hash (shared vector with Java)
    fit.py                       fit stage: layout, edit, time, clean, manifest, change detection, render call
    render.py                    clip video through dust_trail.py, for an entity or a block model; mux with sound
    video.py                     video stage: matcher, planted control, saved results -> evidence
    review\server.py             127.0.0.1 server: static page, byte ranges, state, decisions, refit
    review\page.html             the review page (HTML, CSS, JS in one file)
    stage.py                     staged folder, fragments, keyframe files, status page, provenance, report
    check.py                     rule checks on STAGED and on sound keys
    report.py                    report.md writer (shared by stage and check)
  data\
    species.json                 alias table
    rows_to_names.json           row type -> sounds (event id, class, tokens per source, related sounds, clip words)
    rows_to_clips.json           species -> row type -> clips, play mode, start (key/code), anchor; top-level animation_choice table
    recipes.json                 death recipe values, lighter recipe values, footstep set values, machine layering
    foot_nudges.json             the lead's by-ear footstep offsets per clip and foot (ms); seeds decisions.json
    foot_branches.json           the 7 FootPlant events -> their branch wem lists (copy of the spike's)
    loudness.json                class -> LUFS target
    engine_endings.json          FootPlantFront, FootPlantRear, PredatedKilled, PredatedSurvive, Deathroll, DeathRoll, Footstep, Death, Hurt, Die
    fingerprint_vector.json      shared test vector (clip excerpt, model hash, expected hex)
  tests\
    fixtures\                    art list excerpt, motion graph excerpt, registry excerpt, clip and model copies (plus the 7 change cases), the spike's measured landings, tiny wavs
    conftest.py                  the `real` marker: skips real-data tests when a source is missing
    test_<module>.py             one per module
WORK\
  cache\  jwe3\<bank>\  pk\<bank>\  mg\<species>\  names\  rows.json  candidates.json
  craft\<row_id>\<candidate.dir>\<n>.ogg   craft\_unnamed\<bank>\<wem>.ogg   fit\<species>\<clip>\   renders\
  clip_fit_manifest.json  decisions.json  video\  report\
STAGED\                          as the design's "What the staging folder holds"
```

Row ids are `<thing>.<asset>` slugs: lowercase, spaces and punctuation to `_`, repeated `_` collapsed, a parenthesis group that ends in `files` dropped (it is a count, not a name), for example `tyrannosaurus.death` from "Death (3 files)", `tyrannosaurus.footsteps_heavy`, `tyrannosaurus.eat_flesh_rip_crunch`, `incubator.fault_no_power`, `incubator.heater_hum`. A sound id is the event id. A candidate id is `<source>:<name>` for named sounds (`jwe3:Trex_SadCall`, `pk:TheropodLarge_Footstep`, `repo:dinosaurs/_common/sounds/flesh_rip_1.ogg`), `blend:<recipe>` for a baked blend, `layer:<recipe>` for a layered sound, `edit:<name>:<take>:<part>` for a cut and `edit:<name>:<take>:<a>+<b>` for two joined parts, `unnamed:<bank>:<wem>` for an unnamed JWE3 sound. A PK candidate's `<name>` is the stream name minus its trailing `_NN` (`pk:Trex_GrowlCourtshipA` from `Trex_GrowlCourtshipA_01` to `_08`), and its takes are those streams; a stream name with no `_NN` is its own candidate.

---

# First slice: T. rex, the Incubator and the Carnivore Feeder

Order: the lead hears T. rex picks after Task 13 (milestone 1) and T. rex sounds on their clips after Task 21 (milestone 2). Tasks 1 to 7 are the data path, 8 to 13 map and craft, 14 to 15 the recipes the lead already judged, 16 to 22 clip fit and staging, 23 the Incubator and the park banks, 24 the Carnivore Feeder (the slice's machine clip, rendered with its sound), 25 video evidence, 26 the wrap. The Incubator and the feeder come after both milestones, so they delay neither.

The three things in the slice, from the art list: **Tyrannosaurus** (18 pending sound rows), **Incubator** (8 rows; its animations are code-driven, so every row is `clip: none` and audio only), **Feeders** (3 rows, all Missing: Hatch open and close; Food drop; Bait release). The feeder is the Carnivore Feeder, `carnivore_feeder_one`: the game's `CarnivoreFeederModel` uses that geo and animation file, the art list's Done rows name its clips (`open` 9 s hold, `idle` 1 s loop), and `feeder_one.animation.json`'s `idle` has no bones, so it would render nothing.

### Task 1: Tool repo, package and CLI skeleton

**Files:**
- Create: `TOOL\pyproject.toml`, `TOOL\config.toml`, `TOOL\.gitignore`, `TOOL\nublar_sounds\__init__.py`, `TOOL\nublar_sounds\__main__.py`, `TOOL\nublar_sounds\config.py`, `TOOL\nublar_sounds\cache.py`
- Test: `TOOL\tests\test_config.py`, `TOOL\tests\test_cache.py`, `TOOL\tests\conftest.py` (the `real` marker)

**Interfaces:**
- Produces: `config.Paths` (dataclass: `sound_files, tool, work, staged, repo, assets, jwe3_audio, jwe3_dinos, pk, vgmstream, cobra_src, cobra_python, review_port`, plus the derived `mod_assets = repo\common\src\main\resources\assets\projectnublar`; the test helper `paths_with(root)` is `dataclasses.replace(paths, mod_assets=root)` and changes no other field, and `root` is an assets root, as `tests\fixtures\changes\<case>` and the `tmp_repo_copy` fixture are; `tmp_repo_copy` is a full copy of `mod_assets`, textures included, so a render on it finds its files), `config.load(path: Path | None = None) -> Paths` (default `TOOL\config.toml`), `Paths.require(*names) -> None` (raises `MissingPathError(name, path)` for the first missing one).
- Produces: `cache.source_hash(*paths: Path) -> str` (sha256 of each file's bytes and size, in path order), `cache.stale(key: str, inputs_hash: str, store: Path) -> bool`, `cache.mark(key, inputs_hash, store)`. The store is `WORK\cache\<key>.json`.
- Produces: CLI `PY -I -m nublar_sounds <stage>` for the 9 stages plus `all`; each stage module has `cli(paths: Paths, args) -> int`, registered in `__main__.py`, and `cli` calls the stage's `run` with the parsed values (`craft.run(paths, rows)`, `fit.run(paths, rows)`, `stage.run(paths)`, `check.run(paths)`); a stage whose `run` takes `(paths, args)`, as `video.run` does, is registered as it is. Unbuilt stages print "not built yet" and return 2.
- Produces: the pytest marker `real`, declared in `pyproject.toml`; `tests\conftest.py` skips every `real` test when `JWE3_AUDIO` or `PK` is missing. Every test in this plan that reads a game file, the real art list, a real mod clip or a `WORK` output built from them carries `@pytest.mark.real`; the test code below does not repeat the marker.

- [ ] **Step 1: Make the venv and the repo**

Run, in `TOOL`: `py -3.12 -m venv .venv`, then `.venv\Scripts\python.exe -m pip install numpy Pillow pytest`, then `git init`. Confirm `SPIKE` is untracked after `.gitignore` lists `spike/`.

- [ ] **Step 2: Write the failing tests**

```python
def test_load_reads_every_path(tmp_path):
    cfg = tmp_path / "config.toml"; cfg.write_text(SAMPLE_TOML)   # every key, pointing at tmp_path subfolders
    paths = config.load(cfg)
    assert paths.work == tmp_path / "work" and paths.review_port == 8767
    assert paths.mod_assets == paths.repo / "common/src/main/resources/assets/projectnublar"

def test_require_names_the_missing_folder(tmp_path):
    with pytest.raises(config.MissingPathError) as e: config.load(cfg).require("vgmstream")
    assert e.value.name == "vgmstream"

def test_stale_until_marked(tmp_path):
    h = cache.source_hash(a_file)
    assert cache.stale("pull.TRex", h, tmp_path) is True
    cache.mark("pull.TRex", h, tmp_path)
    assert cache.stale("pull.TRex", h, tmp_path) is False
    assert cache.stale("pull.TRex", "other", tmp_path) is True
```

- [ ] **Step 3: Run the tests to see them fail**

Run: `PY -I -m pytest tests -q`. Expected: ImportError on `nublar_sounds`.

- [ ] **Step 4: Implement `pyproject.toml`, `config.py`, `cache.py`, `__main__.py`, and install the package**

`pip install -e .` into the venv, so `-I` finds it. `config.toml` holds the real paths from "Paths used in this plan"; `review_port = 8767` (8765 and 8766 are the audition pages).

- [ ] **Step 5: Run the tests and the CLI**

Run: `PY -I -m pytest tests -q` → all pass. Run: `PY -I -m nublar_sounds check` → prints "not built yet", exit 2. Run: `PY -I -m nublar_sounds --help` → lists the 9 stages and `all`.

- [ ] **Step 6: Commit** in `TOOL`: `git add -A && git commit -m "feat: package, config, cache and CLI skeleton"`.

### Task 2: Art list parser

**Files:**
- Create: `TOOL\nublar_sounds\artlist.py`
- Test: `TOOL\tests\test_artlist.py`, fixture `tests\fixtures\art_list_excerpt.md` (copy of the art list's "Tyrannosaurus ### Sounds", "Incubator ### Sounds" and "Feeders ### Sounds" tables plus one table of another art type)

**Interfaces:**
- Produces: `artlist.Row` (frozen dataclass: `id, thing, asset, used_for, status, feature, where, line`), `artlist.pending_rows(path: Path) -> list[Row]`: every row in a `### Sounds` table under a `## <Thing>` heading whose status is `Missing`, `Placeholder`, `Ready to import` or `Shared, artist to confirm`. Status text is matched on its first words, so `Missing, only if the lead approves` is not pending. `artlist.row_id(thing, asset) -> str`.
- Produces: `map` writes `WORK\rows.json` as `[Row]` with `counts` by status (Task 9 calls `pending_rows`).

- [ ] **Step 1: Write the failing tests**

```python
def test_counts_match_the_design():          # the real file
    rows = artlist.pending_rows(REPO / "docs/plan/art-list.md")
    by = Counter(r.status for r in rows)
    assert len(rows) == 161 and by["Missing"] == 98 and by["Placeholder"] == 10
    assert by["Ready to import"] == 45 and by["Shared, artist to confirm"] == 8

def test_trex_and_incubator_rows():
    rows = artlist.pending_rows(FIXTURE)
    assert [r.asset for r in rows if r.thing == "Tyrannosaurus"][:3] == ["Contact call", "Nest call", "Mating call"]
    assert {r.asset for r in rows if r.thing == "Incubator"} == {"Start", "Lid up and down", "Egg placed", "Done", "Fault: egg dies", "Arm move", "Heater hum", "Fault: no power"}
    assert artlist.row_id("Tyrannosaurus", "Footsteps (heavy)") == "tyrannosaurus.footsteps_heavy"
    assert artlist.row_id("Tyrannosaurus", "Death (3 files)") == "tyrannosaurus.death"
    assert artlist.row_id("Incubator", "Fault: no power") == "incubator.fault_no_power"
    assert [r.asset for r in rows if r.thing == "Feeders"] == ["Hatch open and close", "Food drop", "Bait release"]

def test_row_text_is_kept_exactly():         # Review Focus 1
    rows = artlist.pending_rows(FIXTURE_WITH_ODD_ROW)   # a row with `Sprint ` and a `|` inside backticks in Where
    assert rows[0].asset == "Run clip `Sprint `" and "|" in rows[0].where

def test_only_sound_tables_count():
    assert all(r.thing != "Tyrannosaurus" or not r.asset.startswith("Joint fix") for r in artlist.pending_rows(FIXTURE))
```

- [ ] **Step 2: Run the tests to see them fail.** Expected: `AttributeError: module has no attribute pending_rows`.

- [ ] **Step 3: Implement `artlist.py`.** Track `## ` and `### ` headings while reading lines; a table row is a line starting with `|` under a `### Sounds` heading; split cells on `|` outside backticks; strip one space each side of a cell only. The `## Summary` and `## Questions` sections have no `### Sounds` heading, so they are skipped.

- [ ] **Step 4: Run the tests** → pass. If the real-file count is not 161, stop and report `NEEDS_CONTEXT` with the counts found: the design's number is from 2026-11-04 and the art list may have moved.

- [ ] **Step 5: Commit**: `git add -A && git commit -m "feat: pending sound rows from the art list"`.

### Task 3: FNV, registry and engine-name guesses

**Files:**
- Create: `TOOL\nublar_sounds\fnv.py`, `TOOL\nublar_sounds\registry.py`, `TOOL\data\engine_endings.json`, `TOOL\data\species.json`
- Test: `TOOL\tests\test_fnv.py`, `TOOL\tests\test_registry.py`, fixture `tests\fixtures\registry_excerpt.wmetasb` (two `bnkmetanew` elements with a handful of `evententry` children, cut from the real file)

**Interfaces:**
- Produces: `fnv.fnv1_32(name: str) -> int` (lowercase, FNV-1, as `build_chains.fnv`).
- Produces: `registry.Registry` with `load(path) -> Registry`, `.bank_of(event_hash: int) -> int | None`, `.events_in(bank_name: str) -> set[int]`, `.registered(name: str) -> tuple[bool, str | None]` (tries `""`, `_start`, `_oc`, `_oc_start` endings as `build_blend.sound` does; returns the bank name when the bank's hash matches a known bank name), `.event_count == 15834`.
- Produces: `registry.guess_engine_names(species: str, aliases: dict, registry: Registry) -> GuessResult` with `.hits: dict[str, str]` (name -> bank), `.guesses: int`, `.expected_false: float` (= guesses × 15,834 ÷ 2^32). Guesses are `<prefix>_<ending>` for each JWE3 species prefix and group prefix in `species.json` and each ending in `engine_endings.json`.
- Produces: `data\species.json` with the design's alias table, exactly: per species `jwe3_prefixes`, `jwe3_group_bank`, `pk_prefix`, `pk_standin_groups` (ordered), `mod_name` (`tyrannosaurus_rex`...), `jwe3_archive` (`TyrannosaurusRex`...), `jwe3_variants` (T. rex: `TyrannosaurusRex_Male`, `TyrannosaurusRex_Juvenile`, `TyrannosaurusRex93`, `TyrannosaurusRexFeathered`, `TyrannosaurusRexJWE`, `TyrannosaurusRexScarred`). PK `Tyranno_` is listed under T. rex as `pk_unsure: ["Tyranno_"]`.

- [ ] **Step 1: Write the failing tests**

```python
def test_fnv_offset_and_case():
    assert fnv1_32("") == 2166136261 and fnv1_32("TRex_events") == fnv1_32("trex_events")

def test_registry_counts_the_real_file():
    r = registry.load(SF / "metadata_extracted/wwisemetadatasoundbanks.wmetasb")
    assert r.event_count == 15834 and len(r.events_in("TRex_events")) == 153

def test_known_names_register():
    r = registry.load(REAL)
    assert r.registered("Velociraptor_Die")[0] and r.registered("Gallimimus_DeathRoll")[0]
    assert r.registered("Trex_SadCall")[1] == "TRex_events"
    assert r.registered("NotARealName_xyz")[0] is False

def test_guess_pass_reports_chance_hits():
    g = registry.guess_engine_names("Tyrannosaurus", aliases, r)
    assert g.guesses > 0 and abs(g.expected_false - g.guesses * 15834 / 2**32) < 1e-9
    assert all(h in r_names for h in g.hits)
```

- [ ] **Step 2: Run the tests to see them fail.**

- [ ] **Step 3: Implement.** Bank names come from the file and folder names in `JWE3_AUDIO` and `SF\JWE3 Sounds` (`<name>_events`, `<name>_media`), hashed as `registry_probe.py` does.

- [ ] **Step 4: Run the tests** → pass. Print `g.hits` for T. rex in the test log; the reviewer checks that the hit list is reported with its `expected_false`.

- [ ] **Step 5: Commit**: `git add -A && git commit -m "feat: fnv, registry check and engine-name guesses"`.

### Task 4: JWE3 banks: extract, read, decode (`pull` and `convert` for JWE3)

**Files:**
- Create: `TOOL\nublar_sounds\cobra.py`, `TOOL\nublar_sounds\wwise.py`, `TOOL\nublar_sounds\vgm.py`, stage functions in `pull.py` and `convert.py`
- Modify: `TOOL\nublar_sounds\__main__.py` (registers `pull` and `convert`; flag `--species`)
- Test: `TOOL\tests\test_wwise.py`, `TOOL\tests\test_vgm.py` (real-data tests carry the `real` marker, Task 1)

**Interfaces:**
- Produces: `cobra.extract(ovl: Path, out: Path, paths: Paths) -> Path` runs `ovl_tool_cmd.py extract -g "Jurassic World Evolution 3" -o <out> <ovl>` with `paths.cobra_python`, `cwd=paths.cobra_src`, no `--type`; cached by the ovl's source hash.
- Produces: `wwise.Bank(name: str, store: BankStore)` carrying `build_chains.Bank` logic: `DIDX/DATA` media, `_bnk_s.aux` streams, HIRC objects, parent-child links by rank, `takes(event_name) -> list[int]` (tries `""`, `_start`, `_oc`, `_oc_start`), `wem_bytes(wem: int) -> bytes | None`. `wwise.BankStore(paths)` opens banks on demand from `SF\JWE3 Sounds` then `JWE3_AUDIO`, and `find_wem(wem, own_bank)` looks in the own bank, every open bank, then the loose folders `SF\Shared extracted\*\wem` and `SF\TRex extracted\wem*`.
- Produces: `wwise.foot_branch(event: str) -> list[int] | None` from a copy of `SPIKE\foot_branches.json` placed at `data\foot_branches.json` (the 7 FootPlant events and their branch wem lists).
- Produces: `vgm.decode_wem(wem_bytes, out_wav: Path, paths) -> Path`, `vgm.load_mono48(wav: Path) -> np.ndarray` (ffmpeg `-ac 1 -ar 48000 -f f32le`), `vgm.RATE = 48000`.
- Produces: `pull` for JWE3 extracts the species' banks, its group bank, `sharedCreatures`, `_landShared`, and the banks a death chain names, into `WORK\jwe3\<bank>\`. `convert` decodes every wem that any event of the species reaches into `WORK\jwe3\<bank>\wav\<wem>.wav`, cached.

- [ ] **Step 1: Write the failing tests**

```python
def test_trex_sad_call_has_takes_with_audio():
    store = wwise.BankStore(paths); bank = store.bank("TRex")
    takes = bank.takes("Trex_SadCall")
    assert takes and all(store.find_wem(w, bank) for w in takes)

def test_cross_bank_media_resolves():                     # Review Focus 2
    store = wwise.BankStore(paths)
    for name, bank in [("Fighting_BodyFall_Heavy", "_landShared"), ("dinosaurMedium_Body_RestFall_dly", "sharedCreatures"), ("TheropodLarge_drink_large_scoop_begin", "TheropodLarge_Shared")]:
        takes = store.bank(bank).takes(name)
        assert takes, name
        assert all(store.find_wem(w, store.bank(bank)) is not None for w in takes), name

def test_nested_switch_containers_reach_audio():          # Review Focus 2: this event's root is a switch on animal type, and its branches hold more switch containers (`switch_probe.py`)
    store = wwise.BankStore(paths); bank = store.bank("sharedCreatures")
    branch = set(bank.takes("TheropodLarge_FootPlantRear")) & set(wwise.foot_branch("TheropodLarge_FootPlantRear"))
    assert branch and all(store.find_wem(w, bank) is not None for w in branch)

def test_foot_branch_filters_the_footstep_system():
    assert len(wwise.foot_branch("TheropodLarge_FootPlantRear")) == 140 and wwise.foot_branch("Trex_SadCall") is None

def test_decode_gives_mono_48k():
    wav = vgm.decode_wem(store.find_wem(takes[0], bank), tmp_path / "a.wav", paths)
    x = vgm.load_mono48(wav); assert x.ndim == 1 and len(x) > 4800
```

- [ ] **Step 2: Run the tests to see them fail.**

- [ ] **Step 3: Implement `cobra.py`, `wwise.py`, `vgm.py`, `pull.run`, `convert.run`.** The already-extracted `SF\TRex extracted` and `SF\Shared extracted` are reused as loose wem folders, so T. rex needs no new extraction unless a bank is missing. Decoding all of a species' reachable wems takes minutes: run `convert` in the background.

- [ ] **Step 4: Run the tests** → pass. Run: `PY -I -m nublar_sounds pull --species Tyrannosaurus` then `convert --species Tyrannosaurus` (background). Expected: `WORK\jwe3\TRex\wav\*.wav` exists; a second `convert` run finishes in seconds (cache).

- [ ] **Step 5: Commit**: `git add -A && git commit -m "feat: JWE3 bank extract, read and decode"`.

### Task 5: Prehistoric Kingdom names and decode

**Files:**
- Create: `TOOL\nublar_sounds\fsb.py`; extend `pull.py`, `convert.py`, `vgm.py`, `__main__.py` (flag `--source`)
- Test: `TOOL\tests\test_fsb.py`

**Interfaces:**
- Produces: `fsb.stream_names(data: bytes) -> list[str]` (carry `fsb_names.stream_names`), `fsb.index_names(pk: Path, copies: Path) -> dict[str, list[str]]` bank file -> names, over `PK\*.assets.bank` with `SF\*.assets.bank` as fallback copies; skips `Music.assets.bank` and `VO_Characters*.assets.bank`.
- Produces: `vgm.decode_pk(bank: Path, index: int, name: str, out_wav: Path, paths) -> Path` runs `vgmstream-cli.exe -s <index+1> -o <out> <bank>` (lowercase `-s` selects one subsong; the spike's `-S` means "end subsong" and decodes 1 to N, confirmed from `vgmstream-cli -h`) and raises `NameMismatch` unless exactly one `stream name:` line is in stdout and its value equals `name`. Task 23 decodes PK streams through this function only.
- Produces: `pull`/`convert --source pk` write `WORK\pk\names.json` only. `craft` and `craft.edits_for` (Task 12) decode a stream on demand through `vgm.decode_pk`, into the cached path `WORK\pk\<bank>\<index>_<name>.wav`; no stage decodes PK audio ahead of time.

- [ ] **Step 1: Write the failing tests**

```python
def test_synthetic_fsb5_header_names():
    data = build_fsb5(["A_01", "B_02"])          # test helper writes a minimal FSB5 header + name table
    assert fsb.stream_names(data) == ["A_01", "B_02"]

def test_real_pk_names():
    names = fsb.index_names(paths.pk, paths.sound_files)
    flat = {n for ns in names.values() for n in ns}
    assert len(flat) == 9062 and "Trex_GrowlCourtshipA_08" in flat and "Amb_Incubation_Loop_01" in flat      # the design's 9,664 is all 33 banks; Music and VO_Characters are skipped
    assert not any(b.startswith("Music") for b in names)

def test_decode_checks_the_name():
    bank, idx, name = find("TheropodLarge_Footstep", names)
    x = vgm.load_mono48(vgm.decode_pk(bank, idx, name, tmp_path / "s.wav", paths))
    assert 0.05 < len(x) / vgm.RATE < 5                      # one stream, not the whole bank
    with pytest.raises(vgm.NameMismatch): vgm.decode_pk(bank, idx, "SomeOtherName", tmp_path / "t.wav", paths)
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** **Step 4: Run the tests** → pass. If the unique name count differs from 9,062 (the design's 9,664 without `Music` and `VO_Characters`), report the count found as `NEEDS_CONTEXT` (the design's number is a measurement, not a rule).

- [ ] **Step 5: Commit**: `git add -A && git commit -m "feat: PK stream names and decode by index"`.

### Task 6: Motion graphs, male and variant measurement

**Files:**
- Create: `TOOL\nublar_sounds\motiongraph.py`; extend `pull.py`, `__main__.py` (flag `--graphs`)
- Test: `TOOL\tests\test_motiongraph.py`, fixture `tests\fixtures\mg_excerpt.motiongraph` (two `data` elements cut from the T. rex female graph: `TyrannosaurusRex$Drink02` and `TyrannosaurusRex$Partial_Roar`)

**Interfaces:**
- Produces: `motiongraph.Link` (`clip, event, kind, at, curve: list[tuple[float, int]], graph`), `motiongraph.links_in(xml_path: Path) -> list[Link]` (carry `mg_links.links_in` and `streams`; `AudioEvent` and `AudioLoopingEvent` are sounds, `AudioRTPC` and `AudioBlend` are control values and are returned with `kind` so `map` can skip them), `motiongraph.names_in(xml_path) -> set[str]`.
- Produces: `pull --graphs <species>` extracts `JWE3_DINOS\<Archive>\<Gender>\<Archive>_<Gender>.ovl` for Female, Male, Juvenile and each variant archive in `species.json`, keeps only `*.motiongraph` in `WORK\mg\<species>\<graph>.motiongraph`, and copies the ones already in `SPIKE\mg\TRex` and `SF\trex_variants_mg` instead of re-extracting.
- Produces: `WORK\report\graphs_<species>.md`: per graph, clips with sound, links, audio names, names not in the female and juvenile graphs, and registered-event coverage (named of 153) with each graph added in turn.

- [ ] **Step 1: Write the failing tests**

```python
def test_links_from_excerpt():
    links = motiongraph.links_in(FIXTURE)
    assert ("TyrannosaurusRex$Drink02", "TheropodLarge_drink_large_scoop_begin") in {(l.clip, l.event) for l in links}
    assert ("TyrannosaurusRex$Partial_Roar", "Trex_HeroRoar_Pre") in {(l.clip, l.event) for l in links}
    assert all(0.0 <= p[0] <= 1.0 for l in links for p in l.curve)

def test_control_streams_are_marked():
    kinds = {l.kind for l in motiongraph.links_in(FIXTURE)}
    assert kinds <= {"AudioEvent", "AudioLoopingEvent", "AudioRTPC", "AudioBlend"}
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** Extraction of 7 archives takes minutes: background.

- [ ] **Step 4: Run the tests** → pass. Run: `PY -I -m nublar_sounds pull --graphs Tyrannosaurus` (background). Expected: 7 graph files in `WORK\mg\Tyrannosaurus\` and `WORK\report\graphs_Tyrannosaurus.md` with the coverage table. If an archive has no `.motiongraph` after extraction, record "no graph" in the report; do not guess.

- [ ] **Step 5: Commit**: `git add -A && git commit -m "feat: motion graph links and the T. rex graph measurement"`.

### Task 7: Names per species

**Files:**
- Create: `TOOL\nublar_sounds\names.py`
- Test: `TOOL\tests\test_names.py`

**Interfaces:**
- Produces: `names.build(species: str, paths, registry, aliases) -> SpeciesNames` and `WORK\names\<species>.json`: every JWE3 event name known for the species, each with `source` (`graph`, `engine_guess`, `registry_only`), `registered: bool`, `bank` (with `_events` and `_media` stripped, so `TRex_events` is stored as `TRex`, the name `BankStore.bank` and `WORK\jwe3\<bank>` use), `chance_hit_risk: float` (the guess pass's `expected_false`), `graphs: list[str]`; the unnamed registered events of the species' own bank as `unnamed_<hash>`; the PK names with the species' prefix and its stand-in groups.
- Produces: coverage numbers for `report.md`: named of registered per bank.

- [ ] **Step 1: Write the failing tests**

```python
def test_trex_names_cover_the_design_count():
    n = names.build("Tyrannosaurus", paths, reg, aliases)
    assert n.registered_named >= 130 and n.registered_total == 153      # female + juvenile gave 130; more graphs may add
    assert n.jwe3["TheropodLarge_FootPlantRear"].source == "engine_guess"
    assert n.jwe3["Trex_HeroRoar_Pre"].source == "graph"
    assert any(k.startswith("unnamed_") for k in n.jwe3)

def test_pk_names_split_by_prefix():
    assert "Trex_VoxYawn_03" in n.pk_own and "TheroLarge_DrinkSip_01" in n.pk_standin["TheroLarge_"]
    assert "Tyranno_SneezeA_01" in n.pk_unsure
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** **Step 4: Run the tests** → pass. **Step 5: Commit**: `git add -A && git commit -m "feat: species name sets with sources and coverage"`.

### Task 8: Row data files: rows to names, rows to clips

**Files:**
- Create: `TOOL\data\rows_to_names.json`, `TOOL\data\rows_to_clips.json`, `TOOL\data\loudness.json`, `TOOL\nublar_sounds\rowmap.py`
- Test: `TOOL\tests\test_rowmap.py`

**Interfaces:**
- Produces: `rowmap.classify(row: Row) -> RowType | None`: the row type by asset text (`rows_to_names.json` has `match` word lists per type, matched on the asset text before its first `(`, lowercased, so "Sleep (snore or slow breath)" is `sleep`, not `breath`; matching is on whole words, so `eat` does not match "Heater hum"; the first type in file order whose word matches wins; `Hurt 1` and `Hurt 2` both match `hurt`). `rowmap.sounds_for(row, subject, names: SpeciesNames | None = None) -> list[SoundSpec]` with `SoundSpec(event_id, cls, tokens: dict[source, list[str]], related: list[str], related_names: list[str], clip_words: list[str], no_voice: bool, baked: list[str])`; `related` holds row types (`breath`, `growl`, `snort`) and `related_names` the JWE3 and PK names of this species that those types' tokens match, filled from `names` (Task 7's `names.build`) and empty when `names` is None; `baked` holds the sound's baked candidate ids from `rows_to_names.json` (`entity.tyrannosaurus_rex.death`: `blend:trex_sc`; `block.incubator.start`: `layer:incubator_start`; `block.feeder.hatch_open`: `layer:feeder_hatch`; `block.feeder.hatch_close`: `layer:feeder_hatch_close`). `rowmap.clip_entry(subject, row_type, sound: str | None = None) -> ClipEntry | None` with `ClipEntry(clips: list[str], play: "LOOP"|"HOLD"|"FILE", start: "key"|"code", anchor: AnchorSpec | dict[part, AnchorSpec], jwe3_clip_words, reverse: bool = False)`; `clips == []` means `none`; a row type with one entry per sound (`feeder_hatch`, `footsteps`) takes the sound's short name, and with no name it gives the first entry. `rowmap.AnchorSpec(kind, ref, index=0, share=None, reverse=False)`: `index=-1` is the last moment, `share` holds a share anchor's value, and `clip_entry` copies `ClipEntry.reverse` onto each `AnchorSpec`; Task 17 imports it. `rowmap.subject_for(thing) -> SubjectRef | None` (`SubjectRef` is the named tuple `(kind, name)`; `clips.Subject`, Task 16, is the class it leads to) maps an art list thing to its clip subject: `Tyrannosaurus` → `("entity", "tyrannosaurus_rex")`, `Feeders` → `("block", "carnivore_feeder_one")`, `Incubator` → `None` (no clips). `rowmap.section_for(thing) -> str` maps an art list thing to its section key: a species to its `mod_name` in `species.json`, `Feeders` to `carnivore_feeder_one`, `Incubator` to `incubator`. `rows_to_clips.json` is keyed by subject name, each section with `kind: "entity" | "block" | "none"` (`incubator` has `kind: "none"`); the top-level `animation_choice` key (Task 16) is not a subject.
- Produces: `rowmap.validate(rows: list[Row]) -> list[str]` problems: a pending row with no row type, a dinosaur row type with no clip entry for that species (a missing entry is an error; `none` is explicit), a sound with no class. A row of a species or thing that has no section at all in `rows_to_clips.json` (every species but T. rex in this slice) is not an error: `rowmap.has_section(species_or_thing) -> bool` is false, `fit.run` (Task 19) skips the row and `report.md` lists it as `blocked: no row map entry`, and `check` (Task 22) lists the same rows.
- Produces: `loudness.json` = the start targets from Global Constraints.

`rows_to_names.json` start content (editable, My call in the design). Classes: roar, call, bite, footstep, breath, machine, fence; hurt and death join call, eat and drink join breath. Tokens shown as `jwe3 | pk`:

| Row type | Match words | Sounds (event id under `entity.<species>.`) | Class | Tokens | Related sounds for cuts |
|---|---|---|---|---|---|
| contact_call | contact call, call 1, call 2 | `vocals.contact_call` | call | `Social`, `Call`, `Bellow` \| `Vox`, `Call`, `Broadcast` | — |
| nest_call | nest call | `vocals.nest_call` | call | no name in any source: same-species `Call` candidates; the first-ranked starts ticked, proposed To check in game | — |
| mating_call | mating call | `vocals.mating_call` | call | `Courtship`, `Mating` \| `Courtship` | — |
| hatchling | hatchling | `hatchling.call` | call | `Juv` \| `Baby`. Only these tokens; an adult sound never fills this row | — |
| drink | drink | `ambient.drink` | breath | `Drink`, `Gulp` \| `Drink`, `Sip` | — |
| sleep | sleep | `ambient.sleep` | breath | `Sleep`, `Snore`, `Unconscious` \| `Sleep`, `Snore` | — |
| footsteps | footstep, footsteps | `step.walk`, `step.run` | footstep | `FootPlant`, `Footstep`, `Step` \| `Footstep`, `Step`; `no_voice: true` | — |
| yawn | yawn | `vocals.yawn` | call | `Yawn` \| `Yawn` | breath |
| sneeze | sneeze | `vocals.sneeze` | call | `Sneeze` \| `Sneeze` | snort only |
| snort | snort | `vocals.snort` | call | `Snort`, `Sniff` \| `Snort` | breath, growl |
| roar | roar | `vocals.roar` | roar | `Roar`, `HeroRoar` \| `Roar` | — |
| growl | growl | `vocals.growl` | call | `Growl` \| `Growl` | — |
| attack_snarl | snarl, attack | `attack.snarl` | call | `Attack`, `Snarl` \| `Attack` | — |
| bite | bite | `attack.bite` | bite | `Bite`, `Chomp`, `Jaws` \| `Bite`, `Chomp`; `no_voice: true` | — |
| breath | breath, idle ambient | `ambient` | breath | `Breath` \| `Breath` | — |
| hurt | hurt | `hurt` | call | `React`, `Pain`, `Hurt`, `Injured` \| `Hurt`, `Pain` | — |
| death | death | `death` | call | death chain (Task 14) first; `Die`, `Death`, `DeathRoll`, `SadCall`, `StandDie` \| `Death`, `Die` | — |
| eat | eat | shared `entity.carnivore.eat` | breath | `eat`, `Chomp`, `LipSmack` \| `Eat`, `Chew`; repo files on Ready rows | — |

Non-animal types (Incubator; event ids under `block.incubator.`; every game-file pick on a machine row is proposed `Placeholder`, because the art list says the artist makes machine sounds): `machine_start` (`Start`; PK `Amb_Incubation_Start`), `machine_lid` (`Lid up and down` → sounds `lid_up`, `lid_down`; PK `Gate_Open`, `Gate_Close`, `Lid`, `Hatch`), `machine_egg_placed` (`Egg placed`; PK `Egg`, `Place`, `Nest_`), `machine_done` (`Done`; PK `Notif_`), `machine_fault` (`Fault: egg dies` → `fault_egg`; `Fault: no power` → shared `block.machine.power_down`; PK `Notif_`, `Fail`, `Amb_Incubation_Stop`), `machine_arm` (`Arm move`; repo file), `machine_hum` (`Heater hum` → `hum`, loop; PK `Amb_Incubation_Loop`, repo `magnet/obj_magnet_hum_01.ogg`, `02`). All class `machine`.

Feeder types (thing `Feeders`; event ids under `block.feeder.`; class `machine`; all three rows are Missing, so a game-file pick is `Placeholder`): `feeder_hatch` (`Hatch open and close` → sounds `hatch_open`, `hatch_close`; PK `Gate_Open`, `Gate_Close`, `Hatch`, `Mat_Metal`), `feeder_food_drop` (`Food drop` → `food_drop`; PK `Unload_Meat`, `Meat`, `Drop`), `feeder_bait` (`Bait release` → `bait_release`; no name in any source: unnamed park candidates and Leave Missing).

`rows_to_clips.json` start content for `tyrannosaurus_rex` (play from `AnimationChoice`; `check` reports where the file's `loop` differs):

| Row type | Clips | Play | Start | Anchor | JWE3 clip words |
|---|---|---|---|---|---|
| contact_call | `speak` (2.875 s; T. rex has no `calling` clip yet, the art list says Call is to export) | FILE (loop true) | code | jaw 1 (`jawLower1`) | `Social`, `Call` |
| nest_call | `nesting` (2.5 s) | LOOP | code | jaw 1 | `Nest` |
| mating_call, hatchling, yawn, sneeze, snort, growl | none | — | code | — | — |
| drink | `drink` (9.125 s) | LOOP (DRINK) | key | jaw 1 | `Drink` |
| sleep | `restidle` (no length: unbounded, Task 16) | LOOP (REST) | key | share 0.0 | `Unconscious`, `Sleep` |
| footsteps | one entry per sound: `move.walk` (2.25 s) for `step.walk`, `run` (1.0 s) for `step.run` | LOOP | key | foot landing, `footLeft`, `footRight` | `FootPlant` (engine) |
| roar | `roar` (7.5417 s) | LOOP (ROAR) | code | jaw 1 | `Partial_Roar`, `Roar` |
| attack_snarl | `attack1` (1.0 s) | LOOP (ATTACK) | code | share 0.0 | `Attack` |
| bite | `attack1` | LOOP | code | jaw 1 | `Bite`, `Attack` |
| breath | `misc.idle` (no length: unbounded, Task 16) | FILE (IDLE) | code | share 0.0 | `Idle`, `Breath` |
| hurt | `flinch` (1.4167 s) | HOLD | code | share 0.0 | `React`, `Hurt` |
| death | `dead` (0.9583 s) | HOLD | code | body drop, bone `hips`; falls at the recipe shares when no drop is found | `JumpAttackDefendDeath` (death chain) |
| eat | `eat1` (4.21 s), `eat2` (3.875 s) | LOOP (EAT) | key | jaw 1 | `Eat` |

Every Incubator type: clips `none` (the Incubator's animations are code-driven: the art list's Animations table says "All code-driven", and no `incubator.animation.json` exists). Its rows are judged from audio alone on the review page, and the lid sound is trimmed to the 20-tick lift, 1.0 s (Task 23).

`rows_to_clips.json` section `carnivore_feeder_one` (`kind: block`; the clips are the two in `geckolib\animations\block\carnivore_feeder_one.animation.json`, and nothing else is planned for: no close clip exists, and the art list's rule is that code plays `open` in reverse until one does. That reverse is F3.3 work. Today's `DinosaurFeederBlockEntity` switches to `idle`, so the hatch snaps shut and the close sound cannot be judged in game until F3.3):

| Row type | Sound | Clips | Play | Start | Anchor | Notes |
|---|---|---|---|---|---|---|
| feeder_hatch | `hatch_open` | `open` (9 s, `hold_on_last_frame`) | HOLD | key | parts: `doors` = bone move start of `Right_door` (index 0); `platform` = bone move stop of `Food_Panel` (last, `index=-1`) | `Right_door` and `Left_door` slide apart (4 position keys), `Food_Panel` rises 14 px (5 position keys). A plain candidate has one part at the `doors` anchor; a layered candidate (door slide + platform thud) has both. |
| feeder_hatch | `hatch_close` | `open`, `reverse: true` | HOLD | code | parts, in reversed time: `platform` = bone move start of `Food_Panel` (index 0; the platform lands, 1.375 s); `doors` = bone move start of `Right_door` (index 0; the doors shut, 3.625 s) | The lead's pick, 2026-11-05. Reversed time 0 is the clip's last motion (4.375 s of `open`), not its end, because the 4.5 s hold after it would be a wait before the hatch closes. F3.3's reverse must start there, and it starts the sound with the reverse. A plain candidate has one part at `doors`; a layered candidate has both. No sound key: a key on `open` would play during the opening. When a close clip lands, the row map links `hatch_close` to it, and "clip arrived, no fit" brings the row back. |
| feeder_food_drop | `food_drop` | none | — | code | — | Each bite is the animal's action, not a feeder clip. |
| feeder_bait | `bait_release` | none | — | code | — | The Live-Bait Feeder has no model yet. |

`idle` (1 s loop, one static `Food_Panel` key) carries no sound and has a manifest entry with no sounds, so a change to it is still reported.

- [ ] **Step 1: Write the failing tests**

```python
def test_every_slice_row_classifies():
    rows = [r for r in artlist.pending_rows(ART) if r.thing in ("Tyrannosaurus", "Incubator", "Feeders")]
    assert len(rows) == 29 and all(rowmap.classify(r) for r in rows)
    assert [s.event_id for s in rowmap.sounds_for(row("Feeders", "Hatch open and close"), "carnivore_feeder_one")] == ["block.feeder.hatch_open", "block.feeder.hatch_close"]
    assert rowmap.subject_for("Feeders") == ("block", "carnivore_feeder_one") and rowmap.subject_for("Incubator") is None
    assert rowmap.section_for("Incubator") == "incubator" and rowmap.section_for("Tyrannosaurus") == "tyrannosaurus_rex" and rowmap.section_for("Feeders") == "carnivore_feeder_one"
    assert rowmap.classify(row("Incubator", "Heater hum")).name == "machine_hum"                       # whole words: `eat` is not in "Heater"
    assert rowmap.classify(row("Tyrannosaurus", "Footsteps (heavy)")).name == "footsteps"
    assert rowmap.classify(row("Tyrannosaurus", "Sleep (snore or slow breath)")).name == "sleep"      # the text after "(" does not count
    assert [s.event_id for s in rowmap.sounds_for(row("Tyrannosaurus", "Footsteps (heavy)"), "tyrannosaurus_rex")] == ["entity.tyrannosaurus_rex.step.walk", "entity.tyrannosaurus_rex.step.run"]
    assert rowmap.sounds_for(row("Tyrannosaurus", "Death"), "tyrannosaurus_rex")[0].event_id == "entity.tyrannosaurus_rex.death"

def test_clip_entries_and_none():
    assert rowmap.clip_entry("tyrannosaurus_rex", "death").clips == ["dead"]
    assert rowmap.clip_entry("tyrannosaurus_rex", "yawn").clips == []
    assert rowmap.clip_entry("incubator", "machine_hum").clips == []
    e = rowmap.clip_entry("carnivore_feeder_one", "feeder_hatch"); assert e.clips == ["open"] and e.play == "HOLD" and set(e.anchor) == {"doors", "platform"} and not e.reverse
    c = rowmap.clip_entry("carnivore_feeder_one", "feeder_hatch", "hatch_close"); assert c.clips == ["open"] and c.reverse and c.start == "code" and set(c.anchor) == {"doors", "platform"}
    assert all(a.reverse for a in c.anchor.values()) and not e.anchor["doors"].reverse and e.anchor["platform"].index == -1
    assert rowmap.clip_entry("tyrannosaurus_rex", "footsteps", "step.run").clips == ["run"] and rowmap.clip_entry("tyrannosaurus_rex", "footsteps", "step.walk").clips == ["move.walk"]
    assert rowmap.clip_entry("carnivore_feeder_one", "feeder_bait").clips == []

def test_missing_entry_is_an_error():
    problems = rowmap.validate(rows_with_a_trex_row_of_an_unmapped_type)   # a T. rex section exists, but not this row type
    assert any("tyrannosaurus_rex" in p and "no clip entry" in p for p in problems)

def test_species_without_a_section_is_blocked_not_an_error():
    assert rowmap.has_section("velociraptor") is False and rowmap.has_section("tyrannosaurus_rex") is True
    assert not any("velociraptor" in p for p in rowmap.validate(rows_with_a_velociraptor_row))

def test_hatchling_tokens_are_closed():
    s = rowmap.sounds_for(row("Tyrannosaurus", "Hatchling sounds"), "tyrannosaurus_rex")[0]
    assert set(s.tokens["jwe3"]) == {"Juv"} and set(s.tokens["pk"]) == {"Baby"}
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Write the three data files and `rowmap.py`.** **Step 4: Run the tests** → pass. **Step 5: Commit**: `git add -A && git commit -m "feat: row data files and classification"`.

### Task 9: `map` stage: candidates and ticked picks

**Files:**
- Create: `TOOL\nublar_sounds\candidates.py`, `TOOL\nublar_sounds\map.py`
- Modify: `TOOL\nublar_sounds\__main__.py` (registers `map`)
- Test: `TOOL\tests\test_candidates.py`

**Interfaces:**
- Consumes: `artlist.pending_rows`, `rowmap.*`, `names.build`, `motiongraph.Link` lists from `WORK\mg`, `registry`.
- Produces: `candidates.Candidate` (`id, dir, source, name, bank, stream, length_s | None, own: "own"|"stand_in"|"unknown", marks: list[str], rank: float, takes: list[str]`); marks from `{"two voices", "game use unknown", "guessed name", "not in registry", "unsure species", "other take cut", "pending_recipe"}`. `candidates.dir_name(candidate_id: str) -> str` replaces `:` and `/` with `__` and drops a final `.ogg` (`jwe3:Trex_SadCall` → `jwe3__Trex_SadCall`, `repo:dinosaurs/_common/sounds/flesh_rip_1.ogg` → `repo__dinosaurs___common__sounds__flesh_rip_1`), because `:` is not allowed in a Windows folder name and `rglob("*.ogg")` must find only files; `dir` is stored in `candidates.json` and every path under `WORK\craft` uses it, never the id.
- Produces: `candidates.Ctx` (dataclass: `paths, registry, names, links, store, loudness`, plus `edits_for`, which returns `[]` until Task 12 sets it to `craft.edits_for`; `.candidates(row_id, event_id)` reads a sound's candidates from `WORK\candidates.json`). This is the `ctx` that `candidates`, `craft`, `fit` and `render` take.
- Produces: `candidates.for_sound(spec: SoundSpec, row: Row, species, ctx) -> list[Candidate]` ranked, and `candidates.ticked(row, cands) -> list[str]` by the design's "Which pick starts ticked" table and "Rows with no direct recording" order.
- Produces: `map.run` writes `WORK\candidates.json`: `{row_id: {"row": Row, "sounds": {event_id: {"spec", "candidates": [...], "ticked": [...], "status_proposed": str}}}}` and `WORK\rows.json`. Every pending row appears, even with zero candidates (`ticked: []`, proposed `Missing`).

Ranking (builder's call, fixed here): a JWE3 candidate scores 3 when a graph link's clip name has a clip word and the sound name has a token, 2 for a token in the name only, 1 for a clip word only, plus `log1p(link count)`; PK scores 2 for a token in the name; repo files score 2 on Ready and Shared rows. For a row with no direct recording the ticked order is the design's: a related-sound edit (`edit:*` without `other take cut`, from Task 12's `craft.edits_for`), then a stand-in by the species' group order, then nothing (gap list). In this task `ctx.edits_for` returns `[]`; Task 12 sets it to `craft.edits_for` and re-runs this task's `test_ticked_source_follows_the_table`. `nest_call` has no name in any source, so its first-ranked same-species `Call` candidate starts ticked, proposed To check in game. Each id in `spec.baked` is emitted as a candidate, marked `pending_recipe` until `recipes.json` holds its recipe (Tasks 14, 23, 24); only a death blend starts ticked. Own-sound rules (`candidates.own`) as the design's "What counts as a species' own sound": graph-linked on this species → own; `no_voice` sound class → own; species-named → own; else `stand_in`, or `unknown` for an engine-named shared voice set on the row it was guessed for (`TheropodLarge_StandDie` on death).

- [ ] **Step 1: Write the failing tests**

```python
# The `ctx` and `mating_call_cands` fixtures run `map.run(paths)` on `tmp_work` first, because `ctx.candidates` reads `WORK\candidates.json`.
@pytest.mark.parametrize("status,sources,expect", [
    ("Missing", ["jwe3", "pk"], "jwe3"), ("Placeholder", ["jwe3", "pk"], "jwe3"), ("Missing", ["pk"], "pk"),
    ("Ready to import", ["jwe3", "repo"], "repo"), ("Shared, artist to confirm", ["jwe3", "repo"], "repo"),
    ("Missing", ["stand_in", "edit"], "edit")])                 # no direct recording: a same-species edit comes before a stand-in
def test_ticked_source_follows_the_table(status, sources, expect):
    cands = [fake_candidate(s) for s in sources]                # one candidate per source; "stand_in" is a PK candidate with own == "stand_in"
    got = candidates.ticked(row("Tyrannosaurus", "Yawn", status=status), cands)
    assert got and next(c for c in cands if c.id == got[0]).source == expect

def test_trex_death_starts_on_the_blend():
    cands = ctx.candidates("tyrannosaurus.death", "entity.tyrannosaurus_rex.death")
    assert candidates.ticked(row("Tyrannosaurus", "Death (3 files)"), cands)[0] == "blend:trex_sc"
    assert {"jwe3:Trex_SadCall_Short", "jwe3:Trex_SadCall"} <= {c.id for c in cands}
    assert not any("distortus" in c.id for c in cands)

def test_nest_call_ticks_a_same_species_call():
    event = "entity.tyrannosaurus_rex.vocals.nest_call"; cands = ctx.candidates("tyrannosaurus.nest_call", event)
    got = candidates.ticked(row("Tyrannosaurus", "Nest call"), cands)
    assert got and next(c for c in cands if c.id == got[0]).own == "own"
    assert json.load(open(WORK / "candidates.json"))["tyrannosaurus.nest_call"]["sounds"][event]["status_proposed"] == "To check in game"

def test_hatchling_never_takes_an_adult():
    cands = ctx.candidates("tyrannosaurus.hatchling_sounds", "entity.tyrannosaurus_rex.hatchling.call")
    assert all("Juv" in c.name or "Baby" in c.name for c in cands)
    assert all("Hatchery" not in c.name for c in cands)

def test_own_sound_rules():
    assert own("TheropodLarge_GrowlGulp", species="Tyrannosaurus") == "own"          # graph plays it on T. rex
    assert own("pk:C_Medium_Footstep_Grass", cls="footstep") == "own"                # no voice
    assert own("pk:Sauro_Sneeze", species="Brachiosaurus") == "stand_in"
    assert own("TheropodLarge_StandDie", species="Tyrannosaurus", row="death") == "unknown"

def test_pk_overlap_is_two_voices_and_unticked():
    c = find("pk:Trex_GrowlCourtshipA", mating_call_cands)
    assert "two voices" in c.marks and c.id not in ticked(mating_call_cands)

def test_every_pending_row_is_in_the_output(tmp_work):
    map.run(paths); out = json.load(open(WORK / "candidates.json")); assert len(out) == 161

def test_dir_name_is_a_safe_folder_name(tmp_work):
    map.run(paths); out = json.load(open(WORK / "candidates.json"))
    assert candidates.dir_name("jwe3:Trex_SadCall") == "jwe3__Trex_SadCall"
    assert candidates.dir_name("repo:dinosaurs/_common/sounds/flesh_rip_1.ogg") == "repo__dinosaurs___common__sounds__flesh_rip_1"
    assert all(c["dir"] == candidates.dir_name(c["id"]) for c in all_candidates(out)) and not any(":" in c["dir"] or "/" in c["dir"] for c in all_candidates(out))
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement `candidates.py` and `map.py`.** Rows of species other than T. rex get rows, zero or more candidates from names only, and no clip handling yet; that is enough for "every pending row appears". **Step 4: Run the tests** → pass. Run `PY -I -m nublar_sounds map` and read `WORK\candidates.json` for the 18 T. rex rows: report the ticked ids per row in the task log (names and counts, not judgements). **Step 5: Commit**: `git add -A && git commit -m "feat: map stage with ranked candidates and ticked picks"`.

### Task 10: Audio primitives

**Files:**
- Create: `TOOL\nublar_sounds\audio.py`
- Test: `TOOL\tests\test_audio.py`

**Interfaces:**
- Produces, all on float32 mono at `RATE = 48000`: `trim(x, floor_db=-40) -> x` (carry `build_blend.trim`: 10 ms power window, 10 ms fade in, 50 ms fade out), `level(x, target_db) -> x` (active-sample RMS as the spike), `tone_match(parts, bands=24, max_db=6, strength=0.7) -> parts`, `join(parts, gains_db, overlap_s=0.35) -> x` (equal-power overlaps), `droop(x, end_rate) -> x`, `room(x, wet_db, decay_s=0.7, seed=7) -> x`, `fade_out(x, share, power=1.5)`, `duck(x, at_s, db=-4, for_s=0.3)`, `loop_crossfade(x, overlap_s) -> x` (carry `build_steps.make_loop`), `hit_time(x) -> float` (time of the steepest rise of the 5 ms smoothed envelope), `longest_gap(x) -> float`, `low_shelf(x, db, hz=140)`, `resample_pitch(x, rate) -> x` (only for recipes the lead picked), `peak_db(x)`, `cuts(x, min_part_s=0.3, floor_db=-30, min_gap_s=0.15) -> list[tuple[float, float]]` (start and end of each part between silences at least `min_gap_s` long; the cut detection the design leaves to the builder).

- [ ] **Step 1: Write the failing tests**

```python
def test_trim_removes_leading_silence():
    x = concat(zeros(0.5 s), tone(0.3 s)); assert abs(len(trim(x)) / RATE - 0.3) < 0.015
def test_join_length_is_sum_minus_overlaps():
    out = join([tone(1.0), tone(1.0)], [0, 0]); assert abs(len(out) / RATE - (2.0 - 0.35)) < 0.002
def test_droop_shortens_and_keeps_shape(): assert 0.9 < len(droop(tone(1), 0.93)) / len(tone(1)) < 1.0
def test_loop_crossfade_has_no_seam():
    y = loop_crossfade(noise(2.0), 0.5); seam = abs(y[-1] - y[0]); assert seam < 0.2 * np.abs(y).max()
def test_hit_time_finds_the_rise():
    x = concat(zeros(0.11 s), click_then_tail); assert abs(hit_time(x) - 0.11) < 0.005
def test_level_hits_target(): assert abs(20 * log10(rms_active(level(noise(1), -20))) + 20) < 0.5
def test_cuts_find_the_parts():
    x = concat(tone(0.5), zeros(0.3), tone(0.6), zeros(0.05), tone(0.2)); parts = cuts(x)
    assert len(parts) == 2 and abs(parts[0][1] - 0.5) < 0.02 and abs(parts[1][0] - 0.8) < 0.02   # the 50 ms gap does not split
def test_tone_match_moves_bands_toward_the_mean():
    a, b = noise(1.0, seed=1), low_passed(noise(1.0, seed=2), 800)        # band_gap_db: mean absolute difference over the 24 bands, in dB
    x, y = tone_match([a, b], bands=24, max_db=6, strength=0.7); assert band_gap_db(x, y) < band_gap_db(a, b)
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** Carry the spike formulas unchanged where the test does not pin them. **Step 4: Run the tests** → pass. **Step 5: Commit**: `git add -A && git commit -m "feat: audio primitives"`.

### Task 11: Clean: mono, loudness, Ogg, true peak

**Files:**
- Create: `TOOL\nublar_sounds\clean.py`
- Test: `TOOL\tests\test_clean.py`

**Interfaces:**
- Produces: `clean.measure(path: Path) -> Measure(lufs: float | None, true_peak_db: float, seconds: float, channels: int, codec: str)` (ffmpeg `ebur128=peak=true` and ffprobe), `clean.render(x: np.ndarray, cls: str, out: Path, loudness: dict, loop: bool = False, trim: bool = True, pre_filter: str | None = None) -> Measure`: trim (unless `loop` or `trim=False`; `craft.align_hits` and `fit` pass `trim=False` after their own trim, so a lead-in or a padded start stays), mono, `pre_filter` (an ffmpeg filter string, run before the gain), gain to the class target, encode `libvorbis -b:a 64k -ac 1` with `-fflags +bitexact -flags +bitexact` (ffmpeg otherwise writes a run-dependent header, so two runs of the same input differ at byte 15 and `stage` could never be byte-identical), then measure the encoded file; while true peak > -1.0 dBFS, lower the gain by the overshoot + 0.1 dB and encode again, at most 4 passes; raises `CleanFailed` if still over. For clips under 0.4 s, where integrated loudness is undefined, the target applies to the active RMS in dBFS instead (builder's choice from the design). `clean.check(path, cls, loudness) -> list[str]` problems: not mono, not Vorbis, peak over -1.0, loudness off the target by more than 1.5 LU (loops and short clips excepted from the LUFS rule).

- [ ] **Step 1: Write the failing tests**

```python
def test_render_meets_the_rules(tmp_path):
    m = clean.render(noise_burst(2.0, peak=0.99), "call", tmp_path / "a.ogg", LOUDNESS)
    assert m.channels == 1 and m.codec == "vorbis" and m.true_peak_db <= -1.0 and abs(m.lufs - (-16)) < 1.0
def test_overshoot_is_re_encoded(tmp_path, monkeypatch):   # first measure forced to -0.3 dBFS
    assert clean.render(hot_signal, "roar", tmp_path / "r.ogg", LOUDNESS).true_peak_db <= -1.0
def test_short_clip_uses_rms(tmp_path):
    m = clean.render(click(0.2), "footstep", tmp_path / "s.ogg", LOUDNESS); assert m.true_peak_db <= -1.0 and m.seconds < 0.4
def test_check_flags_a_stereo_loud_file(tmp_path):
    problems = clean.check(write_stereo_loud_ogg(tmp_path), "call", LOUDNESS); assert any("mono" in p for p in problems) and any("peak" in p for p in problems)
def test_render_is_deterministic(tmp_path):
    x = noise_burst(2.0, seed=3)
    clean.render(x, "call", tmp_path / "a.ogg", LOUDNESS); clean.render(x, "call", tmp_path / "b.ogg", LOUDNESS)
    assert (tmp_path / "a.ogg").read_bytes() == (tmp_path / "b.ogg").read_bytes()
def test_trim_false_keeps_the_lead_in(tmp_path):
    m = clean.render(concat(zeros(0.5), tone(0.3)), "call", tmp_path / "l.ogg", LOUDNESS, trim=False); assert abs(m.seconds - 0.8) < 0.03
def test_pre_filter_changes_the_file(tmp_path):
    x = noise_burst(2.0, seed=3); clean.render(x, "call", tmp_path / "a.ogg", LOUDNESS)
    clean.render(x, "call", tmp_path / "f.ogg", LOUDNESS, pre_filter="acompressor=threshold=-20dB:ratio=2.5:attack=15:release=200")
    assert (tmp_path / "a.ogg").read_bytes() != (tmp_path / "f.ogg").read_bytes()
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** **Step 4: Run the tests** → pass. Record one encoded file's size per second in the test log (the design asks the builder to measure the size at about 64 kbps). **Step 5: Commit**: `git add -A && git commit -m "feat: clean to the sound rules"`.

### Task 12: `craft` for plain rows

**Files:**
- Create: `TOOL\nublar_sounds\craft.py`
- Modify: `TOOL\nublar_sounds\__main__.py` (registers `craft`; flag `--thing`), `TOOL\nublar_sounds\candidates.py` (`Ctx.edits_for`)
- Test: `TOOL\tests\test_craft.py`

**Interfaces:**
- Consumes: `WORK\candidates.json`, `wwise.BankStore`, `vgm.*`, `clean.render`, `audio.hit_time`.
- Produces: for every candidate of every T. rex, Incubator and Feeders sound, except `unnamed:*` (Task 23), `WORK\craft\<row_id>\<candidate.dir>\<n>.ogg` (`dir` from Task 9's `candidates.dir_name`, never the id) for `n` in 1..min(5, takes), at least 3 when the source has 3 (fewer only when the source has fewer), cleaned to the sound's class, plus `WORK\craft\<row_id>\<candidate.dir>\meta.json` (`source, bank, stream or wem, takes used, hit_s per file, trimmed_start_s per file, lufs, true_peak, seconds`). A `repo` candidate copies the repo file through `clean.render`. Variant alignment: `craft.align_hits(files) -> list[trim_s]` pads or trims each start so every variant's `hit_time` equals the set's median hit; `meta.json` records `hit_s` and each file's `trimmed_start_s` (Task 19's footstep times need it). It calls `clean.render` with `trim=False` after its own trim.
- Produces: `craft.edits_for(spec: SoundSpec, species, ctx) -> list[Candidate]`: same-species edit candidates for a row with no direct recording. For each related sound in `spec.related` (row types from Task 8: snort from breath or growl, yawn from breath, sneeze from snort only; `spec.related_names` lists the matching JWE3 and PK names of this species) and each of its takes, `audio.cuts` gives the parts; each part at least 0.3 s long is a candidate `edit:<name>:<take>:<part>`; two parts joined with `audio.join` is `edit:<name>:<take>:<a>+<b>`. The same cuts from every other same-species take are candidates too, marked `other take cut`, and they never start ticked. `map` (Task 9) calls this so the candidates are on the page; `craft` renders them.
- Produces: `craft.run(paths, rows: list[str] | None)` cached per candidate on source hash; a changed source clears that candidate's folder and its approval in `decisions.json` (Task 13 defines the file; until then craft writes a `WORK\craft\invalidated.json` list that Task 13 consumes).

- [ ] **Step 1: Write the failing tests**

```python
def test_variants_per_candidate(tmp_work):
    craft.run(paths, rows=["tyrannosaurus.drink"])
    files = sorted((WORK / "craft/tyrannosaurus.drink").rglob("*.ogg")); assert 3 <= count_per_candidate(files).min() <= 5 or source_has_fewer
def test_hits_align(tmp_work):
    meta = json.load(open(WORK / "craft/tyrannosaurus.footsteps_heavy/pk__TheropodLarge_Footstep/meta.json"))   # candidates.dir_name("pk:TheropodLarge_Footstep")
    hits = [f["hit_s"] for f in meta["files"]]; assert max(hits) - min(hits) < 0.005
def test_every_file_passes_clean(tmp_work):
    assert not any(clean.check(p, cls_of(p), LOUDNESS) for p in (WORK / "craft").rglob("*.ogg"))
def test_repo_candidate_is_cleaned(tmp_work):
    m = clean.measure(next((WORK / "craft/tyrannosaurus.eat_flesh_rip_crunch").rglob("*.ogg"))); assert m.channels == 1
def test_yawn_gets_related_cuts_and_other_takes_unticked():
    cands = craft.edits_for(spec("yawn"), "tyrannosaurus_rex", ctx)
    assert any(c.id.startswith("edit:Trex_Breath") for c in cands)
    assert all("other take cut" in c.marks for c in cands if not c.name.startswith(tuple(spec("yawn").related_names)))
    assert all(0.3 <= c.length_s for c in cands)
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement**, and set `Ctx.edits_for` to `craft.edits_for`. Baked candidates (`blend:*`, `layer:*`) are skipped here with a `pending_recipe` note; Tasks 14, 15, 23 and 24 fill them. `edit:*` candidates are rendered here. **Step 4: Run the tests** → pass. Run `PY -I -m nublar_sounds craft --thing Tyrannosaurus` in the background. Expected: a folder per T. rex candidate with 3 to 5 Ogg files. **Step 5: Commit**: `git add -A && git commit -m "feat: craft stage for plain rows"`.

### Task 13: Review server and page, version 1 (audio rows) — milestone 1

**Files:**
- Create: `TOOL\nublar_sounds\review\server.py`, `TOOL\nublar_sounds\review\page.html`, `TOOL\nublar_sounds\review\decisions.py`, `TOOL\nublar_sounds\review\__init__.py`
- Modify: `TOOL\nublar_sounds\__main__.py` (registers `review`)
- Test: `TOOL\tests\test_review_server.py` (uses `http.client` against a server started in a thread on port 0)

**Interfaces:**
- Produces: `decisions.Decisions` over `WORK\decisions.json`: `{"rows": {row_id: {"ticked": {event_id: [candidate_id]}, "approved": bool, "leave_missing": bool, "own_marked": [candidate_id], "offsets_ms": {event_id: {part: int}}, "approved_fingerprints": {clip: hex}, "approved_sources": {candidate_id: source_hash}, "marks": [str], "at": iso}}}`; `load`, `save` (atomic: temp file then `os.replace`), `row(row_id) -> dict`, `set_offset(row_id, event_id, part, ms)`, `tick(row_id, event_id, candidate_ids)`, `invalidate(row_id, reason)` (clears `approved`, appends `reason` to `marks`), `apply_invalidations(WORK\craft\invalidated.json)`.
- Produces: `server.serve(paths, port)`: `ThreadingHTTPServer` on `127.0.0.1` only; `GET /` → `page.html`; `GET /api/state` → JSON `{rows: {row_id: ...} in art list order (the page groups them by thing), candidates, decisions, manifest (empty until Task 19; then `Manifest.view()`), evidence (empty until Task 25)}`; `GET /files/<path>` serves only under `WORK` with byte ranges (carry `nudge_server.do_GET`), rejects `..`; `POST /api/decision` body `{row_id, ticked?, approved?, leave_missing?, own_marked?, offsets_ms?}` ≤ 200 KB, 404 for an unknown row, 400 for bad JSON, 409 for `approved: true` on a row with clips (v1), unless the body has `leave_missing: true` (a row left Missing has no video); `log_message` silent. The page has `<meta http-equiv="Content-Security-Policy" content="default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; media-src 'self' blob:">`, because the page's script and style are inline.
- Produces: `page.html`: rows grouped by thing then art list order; filter by status and "not yet approved"; each row shows art list text, status, proposed status, each sound with its ticked variants and all candidates (play button, source tag, name, length, marks); tick and untick; "Approve row", "Leave Missing"; "Mark as this species' own" on a `game use unknown` candidate. A row whose `rowmap.clip_entry(...).clips` is not empty has "Approve row" disabled with the text "approve after the clip video", and the server answers 409 to `approved: true` for it until Task 21 adds the built video; "Leave Missing" stays enabled, and ticking and offsets are still saved for such a row. Style and widget behaviour follow `SF\audition\nudge.html` (focus rings, `aria-pressed`, status line). Each click saves; the status line shows "Saved" or "Not saved".

- [ ] **Step 1: Write the failing tests**

```python
def test_state_lists_every_row(server): assert len(get("/api/state")["rows"]) == 161
def test_decision_round_trip(server):
    post("/api/decision", {"row_id": "tyrannosaurus.yawn", "ticked": {"entity.tyrannosaurus_rex.vocals.yawn": ["pk:Trex_VoxYawn"]}, "approved": True})
    saved = json.load(open(WORK / "decisions.json"))["rows"]["tyrannosaurus.yawn"]; assert saved["approved"] is True and saved["ticked"]["entity.tyrannosaurus_rex.vocals.yawn"] == ["pk:Trex_VoxYawn"]
def test_unknown_row_is_404_and_writes_nothing(server):      # Review Focus 4
    before = mtime(WORK / "decisions.json"); assert post_status("/api/decision", {"row_id": "nope"}) == 404; assert mtime(...) == before
def test_oversized_body_is_400_and_writes_nothing(server):      # Review Focus 4
    before = mtime(WORK / "decisions.json"); assert post_status("/api/decision", "x" * 200_001, raw=True) == 400; assert mtime(WORK / "decisions.json") == before
def test_path_traversal_is_refused(server): assert get_status("/files/../config.toml") in (400, 404)
def test_range_request(server): assert get_status("/files/craft/tyrannosaurus.drink/jwe3__TheropodLarge_drink_large_scoop_begin/1.ogg", headers={"Range": "bytes=0-99"}) == 206   # the folder is candidates.dir_name(id)
def test_binds_loopback_only(server): assert server.server_address[0] == "127.0.0.1"
def test_clip_rows_cannot_approve_in_v1(server):          # a clip-tied row is judged with its video (Task 21), never from audio alone
    assert post_status("/api/decision", {"row_id": "tyrannosaurus.drink", "approved": True}) == 409
    assert post_status("/api/decision", {"row_id": "tyrannosaurus.yawn", "approved": True}) == 200    # clip: none
def test_leave_missing_approves_a_clip_row(server):       # a row left Missing has no video to wait for
    assert post_status("/api/decision", {"row_id": "tyrannosaurus.drink", "approved": True, "leave_missing": True}) == 200
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement the three files.** **Step 4: Run the tests** → pass. Start `PY -I -m nublar_sounds review` in the background (Bash `run_in_background`; it runs until stopped), then open `http://127.0.0.1:8767/` with `preview_start` (`url`); confirm the T. rex rows list and play one candidate. Take a screenshot for the task report. `REPO\.claude\launch.json` is not changed: `REPO` stays read only.

- [ ] **Step 5: Hand to the lead (milestone 1).** The lead listens to the T. rex plain rows and approves, swaps or leaves them. The agent reports counts only: rows shown, candidates per row, lengths. No agent judges a sound.

- [ ] **Step 6: Commit**: `git add -A && git commit -m "feat: review server and page v1"`.

### Task 14: Death recipe: the T. rex `Sc` blend

**Files:**
- Create: `TOOL\nublar_sounds\recipes.py`, `TOOL\data\recipes.json`; extend `craft.py` for `blend:*` candidates, `__main__.py` (flag `--rows`)
- Test: `TOOL\tests\test_recipes.py`

**Interfaces:**
- Produces: `recipes.json` → `death.full` with the design's start values (trim, level -20 dB, tone match 24 bands / 6 dB / 70 %, overlap 0.35 s, part gains 0 to -10 dB, extra last-breath takes until the voice reaches the last fall, bed 16 dB under at -36 dB level, droop 1.0 → 0.93, fade last 35 %, duck -4 dB for 0.3 s with 20 ms lead, fall level -19 dB, room 0.7 s at -13 dB, compressor -20 dB 2.5:1 15 ms 200 ms), `death.light` with the lighter values (for the outline), and per species: T. rex voice `Trex_SadCall`, `Trex_BreathWheeze`; bed `Trex_BreathWheeze`; falls `Fighting_BodyFall_Heavy` and `dinosaurMedium_Body_RestFall_dly` at share 0.90; Triceratops and Brachiosaurus rows as the design's table (unused until the outline).
- Produces: `recipes.death_blend(species: str, take: int, clip_s: float, fall_times_s: list[float] | None, values: dict, loader: Callable[[str, int], np.ndarray], part_offsets_s: dict[str, float] | None = None) -> BlendResult(mix: np.ndarray, parts: list[PartInfo(name, start_s, end_s, kind: "voice"|"bed"|"fall")], ffmpeg_filters: str)`. `fall_times_s` override the shares when `fit` has a body-drop anchor (Task 19). `part_offsets_s` maps a part name to a shift in seconds: a voice part's join start moves by it (the overlap with its neighbours is kept, the following parts move with it), the bed's start moves by it, a fall's time moves by it. This is how a review-page slider on a blend part (design: "Baked sounds") changes the file. Part names are unique: the bed is `bed:<name>` (`bed:Trex_BreathWheeze`), and an extra take of a voice part is `<name>#2`, `<name>#3`.
- Produces: `craft` builds `blend:trex_sc` takes 1..3 with `clip_s = 0.9583` and the shares, through `clean.render(..., pre_filter=result.ffmpeg_filters)`, so the compressor runs before the loudness gain.

- [ ] **Step 1: Write the failing tests**

```python
def test_sc_reproduces_the_sample_measurably():
    r = recipes.death_blend("tyrannosaurus_rex", 0, 0.9583, None, VALUES["death"]["full"], loader)
    assert [p.name for p in r.parts if p.kind == "voice"][:2] == ["Trex_SadCall", "Trex_BreathWheeze"]
    assert {p.name for p in r.parts if p.kind == "fall"} == {"Fighting_BodyFall_Heavy", "dinosaurMedium_Body_RestFall_dly"}
    assert len({p.name for p in r.parts}) == len(r.parts) and "bed:Trex_BreathWheeze" in {p.name for p in r.parts if p.kind == "bed"}
    assert all(abs(p.start_s - 0.90 * 0.9583) < 0.01 for p in r.parts if p.kind == "fall")
    ref = vgm.load_mono48(SF / "audition/blend_s_c_0.ogg")
    assert abs(len(r.mix) / RATE - len(ref) / RATE) < 0.6          # within the room tail of the sample
    assert audio.longest_gap(r.mix) < audio.longest_gap(plain_chain)   # cohesion, as the design's "Gap" measure
def test_falls_follow_an_anchor():
    r = recipes.death_blend(..., fall_times_s=[0.70], ...); assert all(abs(p.start_s - 0.70) < 0.01 for p in r.parts if p.kind == "fall")
def test_part_offset_moves_that_part():
    a = recipes.death_blend("tyrannosaurus_rex", 0, 0.9583, None, V, loader)
    b = recipes.death_blend("tyrannosaurus_rex", 0, 0.9583, None, V, loader, part_offsets_s={"Trex_BreathWheeze": 0.15})
    assert abs(part(b, "Trex_BreathWheeze").start_s - part(a, "Trex_BreathWheeze").start_s - 0.15) < 0.002
    assert abs(part(b, "Trex_SadCall").start_s - part(a, "Trex_SadCall").start_s) < 0.002          # the part before it stays
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement**, carrying `build_blend.make` with treatment `c`. **Step 4: Run the tests** → pass. Run `craft --rows tyrannosaurus.death`; the page now plays `blend:trex_sc` beside `jwe3:Trex_SadCall`. The lead judges whether it is close to `Sc`; the agent reports length, gap and peak. **Step 5: Commit**: `git add -A && git commit -m "feat: death blend recipe, T. rex Sc"`.

### Task 15: T. rex footstep sets

**Files:**
- Create: `TOOL\data\foot_nudges.json` (copy of `SPIKE\foot_nudges.json`: the lead's by-ear offsets per clip and foot, in ms); extend `TOOL\data\recipes.json`, `recipes.py`, `craft.py` and `candidates.py`
- Test: `TOOL\tests\test_footsteps.py`

Step times are not a static copy: Task 19's `fit` measures each foot landing from the clip (`anchors.foot_landings`) and adds the lead's offset, so a changed clip keeps the offset. This task makes only the sound sets.

**Interfaces:**
- Produces: `recipes.json` → `footsteps` per species: T. rex `ticked: pk:TheropodLarge_Footstep` (plain, walk and run use the same set because PK has one), `also: jwe3:TheropodLarge_FootPlantRear pool, pitch 0.70, low_shelf +6 dB, mark "two voices"`; pool filter from `build_steps.pick_steps` (0.1 s < length ≤ 0.9 s, peak within 0.12 s, spectral centre under 3 kHz, 6 picked by centre - 0.2 × crest; `craft` writes the first 5 of them, the design's 3 to 5 variants); light species values recorded for the outline. `candidates.ticked` honours `footsteps.ticked`, so PK starts ticked on a footsteps sound (the design's footstep table).
- Produces: `recipes.step_tail(x, next_gap_s) -> x`: keep `max(next_gap + 0.08, 0.2)` s, 50 ms fade (carry `mux_clips.track`). `recipes.footstep_pool(event, store, values) -> list[(wem, wave)]` the filtered JWE3 pool.
- Produces: `craft` writes the two T. rex step sets with aligned hits (Task 12); `meta.json` carries each file's `hit_s` and `trimmed_start_s`, which Task 19 reads to move a step's time later by the trimmed amount.

- [ ] **Step 1: Write the failing tests**

```python
def test_tail_is_cut_before_the_next_step(): y = recipes.step_tail(tone(2.0), 0.493); assert abs(len(y) / RATE - 0.573) < 0.002
def test_pool_filter_drops_long_takes():
    pool = recipes.footstep_pool("TheropodLarge_FootPlantRear", store, V["footsteps"]); assert len(pool) == 6 and all(0.1 < len(w) / RATE <= 0.9 for _, w in pool)
def test_two_trex_sets_with_aligned_hits(tmp_work):
    craft.run(paths, rows=["tyrannosaurus.footsteps_heavy"])
    for d in ("pk__TheropodLarge_Footstep", "jwe3__TheropodLarge_FootPlantRear"):
        meta = json.load(open(WORK / f"craft/tyrannosaurus.footsteps_heavy/{d}/meta.json")); hits = [f["hit_s"] for f in meta["files"]]
        assert 3 <= len(hits) <= 5 and max(hits) - min(hits) < 0.005 and all("trimmed_start_s" in f for f in meta["files"])
    assert "two voices" in candidate("tyrannosaurus.footsteps_heavy", "jwe3:TheropodLarge_FootPlantRear")["marks"]
    map.run(paths); out = json.load(open(WORK / "candidates.json"))
    assert out["tyrannosaurus.footsteps_heavy"]["sounds"]["entity.tyrannosaurus_rex.step.walk"]["ticked"][0] == "pk:TheropodLarge_Footstep"
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** **Step 4: Run the tests** → pass. **Step 5: Commit**: `git add -A && git commit -m "feat: footstep sets with aligned hits"`.

### Task 16: Mod clips: lengths, play modes, sampling

**Files:**
- Create: `TOOL\nublar_sounds\clips.py`
- Modify: `TOOL\data\rows_to_clips.json` (adds the top-level `animation_choice` table)
- Test: `TOOL\tests\test_clips.py`, fixtures: copies of `tyrannosaurus_rex.animation.json` and `tyrannosaurus_rex.geo.json` under `tests\fixtures\mod\`

**Interfaces:**
- Produces: `clips.Subject(kind: "entity"|"block", name, root: Path | None = None)` (no root = `config.load().mod_assets`; `fit`, `render` and `stage` pass `paths.mod_assets`) reading `<root>\geckolib\{animations,models}\<kind>\<name>.*.json` (read only): `.kind`, `.name`, `.clips: dict[str, Clip]`, `Clip(name, length_s, length_source: "file"|"keys"|"unbounded", loop_in_file: bool|"hold_on_last_frame"|None, sound_keys: list[(time_s, effect)], bones: dict, raw: dict)`; `.model_sha256` (SHA-256 of the `.geo.json` file's raw bytes). `clips.Species(name, root=None) = Subject("entity", name, root)` is the short form used elsewhere in this plan; `clips.Block(name, root=None) = Subject("block", name, root)`. `clips.subjects(root) -> list[Subject]` lists every entity and block animation file that has a geo model of the same name.
- Produces: `clips.length_rule(clip_json) -> (seconds, source)`: `animation_length` when present (`file`); otherwise the GeckoLib 5.5.6 rule, `com.geckolib.loading.definition.animation.ActorAnimation.calculateUnknownAnimationLength` (the jar is in the Gradle cache at `~\.gradle\caches\modules-2\files-2.1\com.geckolib\geckolib-neoforge-26.2\5.5.6\`; decompile with the JDK's `javap -c -p` or read the same class in the GeckoLib GitHub source at tag 5.5.6; the implementer records the class and method name in the docstring): the largest bone length (`keys`), and when that is 0, `Double.MAX_VALUE` (`unbounded`, `length_s = inf`). All 4 T. rex clips with no stated length (`misc.idle`, `restidle`, `turn_left`, `turn_right`) have no key after time 0, so all 4 are unbounded. A share anchor on an unbounded clip sits at 0 and is marked `anchor unclear` (Task 17), a render uses 2 s (Task 20), and JSON files write `length_s: null`, because `Infinity` is not valid JSON. If the implementer finds another rule, use the real one and report it.
- Produces: `clips.play_mode(subject, clip) -> "LOOP"|"HOLD"|"FILE"|None` from a copy of `AnimationChoice`'s table in `rows_to_clips.json` (the top-level `animation_choice` key, which this task adds and which is not a subject: state → candidate names and loop type, copied from `REPO\common\...\animation\AnimationChoice.java`; a block subject has no state table, so its mode is the row map's `play`), and `clips.loop_differences(subject) -> list[str]` naming clips whose file `loop` differs from the state's mode (T. rex `eat1`: file none, state LOOP).
- Produces: `clips.sample(keys, t)` (Catmull-Rom on every key, carry `foot_contacts.sample`), `clips.sample_by_mode(keys, t)` (each key's `lerp_mode`; a key with no mode is linear; `foot_probe.lerp_sample` and `lerp_modes` hold the logic; keys here are `(time, vector, mode)`), `clips.bone_matrices(species, clip, t, by_mode: bool = False) -> dict[bone, 4x4]` (carry `foot_probe.curves` matrix code, with the length from `length_rule`, not `clip["animation_length"]`; `by_mode=True` samples through `sample_by_mode`), `clips.foot_height(species, clip, foot, steps=480) -> (times, heights)`; `species` is a `Subject` or a `str`.

- [ ] **Step 1: Write the failing tests**

```python
def test_trex_clip_table():
    s = clips.Species("tyrannosaurus_rex", FIXTURE_ROOT)
    assert len(s.clips) == 26 and s.clips["dead"].length_s == 0.9583 and s.clips["dead"].loop_in_file == "hold_on_last_frame"
    assert s.clips["dead"].raw["loop"] == "hold_on_last_frame"
    assert {c.name for c in s.clips.values() if c.length_source == "unbounded"} == {"misc.idle", "restidle", "turn_left", "turn_right"}   # Review Focus 3
    assert all(c.length_s == math.inf for c in s.clips.values() if c.length_source == "unbounded") and all(0 < c.length_s < 100 for c in s.clips.values() if c.length_source == "file")
def test_length_rule_follows_geckolib():
    assert clips.length_rule({"animation_length": 2.5, "bones": {}}) == (2.5, "file")
    assert clips.length_rule({"bones": {"a": {"position": {"0.0": [0, 0, 0], "1.25": [0, 1, 0]}}}}) == (1.25, "keys")
    assert clips.length_rule({"bones": {"a": {"position": {"0.0": [0, 0, 0]}}}}) == (math.inf, "unbounded") and clips.length_rule({"bones": {}}) == (math.inf, "unbounded")
def test_sample_by_mode_is_linear_without_a_mode():
    plain = [(0.0, [0, 0, 0], None), (1.0, [1, 0, 0], None), (2.0, [4, 0, 0], None)]; curved = [(t, v, "catmullrom") for t, v, _ in plain]
    assert clips.sample_by_mode(plain, 1.5)[0] == 2.5 and abs(clips.sample_by_mode(curved, 1.5)[0] - clips.sample([(t, v) for t, v, _ in plain], 1.5)[0]) < 1e-9
def test_eat1_loop_difference(): assert any("eat1" in d for d in clips.loop_differences(clips.Species("tyrannosaurus_rex")))
def test_block_subject_reads_the_feeder():
    f = clips.Block("carnivore_feeder_one")
    assert set(f.clips) == {"open", "idle"} and f.clips["open"].length_s == 9 and f.clips["open"].loop_in_file == "hold_on_last_frame" and f.clips["idle"].loop_in_file is True
    assert len(f.model_sha256) == 64 and {"Right_door", "Left_door", "Food_Panel"} <= set(f.clips["open"].bones)
    assert ("block", "carnivore_feeder_one") in {(s.kind, s.name) for s in clips.subjects(REAL)}
def test_brachiosaurus_sound_keys(): assert len(clips.Species("brachiosaurus", REAL).clips["move.walk"].sound_keys) == 4
def test_foot_height_cycles(): t, h = clips.foot_height("tyrannosaurus_rex", "move.walk", "footRight"); assert h.max() - h.min() > 0.5
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** **Step 4: Run the tests** → pass. **Step 5: Commit**: `git add -A && git commit -m "feat: mod clips, length rule, play modes, sampling"`.

### Task 17: Anchors

**Files:**
- Create: `TOOL\nublar_sounds\anchors.py`
- Test: `TOOL\tests\test_anchors.py`, fixture `tests\fixtures\foot_contacts_measured.json` (copy of `SPIKE\foot_contacts_v2.json`: the spike's measured landings, used only to check the measurement, never read by the tool)

**Interfaces:**
- Produces: `anchors.Anchor(kind: "foot"|"jaw"|"drop"|"share"|"move", ref: str, index: int, time_s: float, flags: list[str])`; `anchors.foot_landings(species, clip, foot) -> list[Anchor]` in two steps, as the spike did: first the touchdown from `SPIKE\foot_contacts.py` `measure` (the foot's lowest-corner height crosses 20 % of its lift, going down, 480 samples per cycle), then `settle_probe.py` from that time (the fastest drop within 0.12 of the clip before it, then the first moment the downward speed falls under 25 % of that peak and the height is within 25 % of the low above the minimum; 0.12 s cap → flag `capped`), on `clips.sample`, the spike's Catmull-Rom, so the times keep matching the spike's and the lead's nudges keep their meaning; `anchors.jaw_turns(species, clip, bone="jawLower1") -> list[Anchor]` (sign changes of the jaw rotation's x-rate, in order); `anchors.body_drops(species, clip, bone) -> list[Anchor]` (fastest fall of the bone's world position, then its stop, in order); `anchors.share(clip_length, share) -> Anchor`; `anchors.bone_moves(subject, clip, bone) -> list[Anchor]` (kind `move`: for a machine clip, the start and the stop of each continuous change of the bone's world position, sampled through `clips.sample_by_mode` at 480 steps, speed over 1 % of the bone's peak speed counts as moving; `index` counts starts and stops in order, `ref` is `<bone>:start` or `<bone>:stop`); `anchors.last_move_stop(subject, clip) -> float` (the last `stop` of any bone, also through `clips.sample_by_mode`); `anchors.measure(subject, clip, spec: rowmap.AnchorSpec) -> Anchor` with `flags=["anchor unclear"]` and a share fallback when the count or signal is missing (a machine clip uses the share of the clip unless its row map names a bone move); a share anchor on an unbounded clip sits at 0 with the flag `anchor unclear`; `AnchorSpec.reverse = True` gives `time_s = last_move_stop - measured time` with the flag `reversed`, so a clip change moves a reversed anchor too; `anchors.moved(a, b) -> bool` at 10 ms or more. All functions take a `clips.Subject`, entity or block, or a `str` (`clips.Species(str)`).

- [ ] **Step 1: Write the failing tests**

```python
def test_landings_match_the_spike_measurement():         # MEASURED = tests/fixtures/foot_contacts_measured.json
    for species, gaits in MEASURED.items():
        for gait, info in gaits.items():
            for foot, want in info["feet"].items():
                got = anchors.foot_landings(species, info["clip"], foot)[0].time_s
                assert abs(got - want) < 0.002, (species, gait, foot)
def test_capped_is_flagged():      # the design: many Brachiosaurus and Triceratops times stopped at the cap
    feet = ("footLeftFront", "footRightFront", "footLeftRear", "footRightRear")
    assert any("capped" in a.flags for f in feet for a in anchors.foot_landings("brachiosaurus", "move.walk", f))
def test_dead_has_a_body_drop(): d = anchors.body_drops("tyrannosaurus_rex", "dead", "hips"); assert d and 0.0 < d[0].time_s < 0.9583
def test_roar_jaw_turns_are_counted(): assert len(anchors.jaw_turns("tyrannosaurus_rex", "roar")) > 10
def test_missing_signal_falls_back_to_share():
    a = anchors.measure("tyrannosaurus_rex", "roar", AnchorSpec("jaw", "jawLower1", index=9999)); assert a.kind == "share" and "anchor unclear" in a.flags
def test_share_on_an_unbounded_clip_sits_at_zero():
    a = anchors.measure("tyrannosaurus_rex", "misc.idle", AnchorSpec("share", "", share=0.0)); assert a.kind == "share" and a.time_s == 0.0 and "anchor unclear" in a.flags
def test_moved_threshold(): assert anchors.moved(A(0.100), A(0.110)) and not anchors.moved(A(0.100), A(0.109))
def test_feeder_bone_moves():
    f = clips.Block("carnivore_feeder_one")
    doors = anchors.bone_moves(f, "open", "Right_door"); platform = anchors.bone_moves(f, "open", "Food_Panel")
    assert doors and doors[0].ref == "Right_door:start" and 0.0 <= doors[0].time_s < 9.0
    assert platform and platform[-1].ref == "Food_Panel:stop" and platform[-1].time_s > doors[0].time_s
    assert anchors.measure(f, "idle", AnchorSpec("move", "Food_Panel", index=0)).kind == "share"      # idle has one static key: no move
def test_feeder_reversed_times():      # the close plays `open` backward from its last motion, 4.375 s
    f = clips.Block("carnivore_feeder_one"); assert abs(anchors.last_move_stop(f, "open") - 4.375) < 0.03
    shut = anchors.measure(f, "open", AnchorSpec("move", "Right_door", index=0, reverse=True)); lands = anchors.measure(f, "open", AnchorSpec("move", "Food_Panel", index=0, reverse=True))
    assert abs(shut.time_s - 3.625) < 0.03 and abs(lands.time_s - 1.375) < 0.03 and "reversed" in shut.flags
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** **Step 4: Run the tests** → pass. **Step 5: Commit**: `git add -A && git commit -m "feat: anchors measured from the clips"`.

### Task 18: Fingerprint, manifest and change detection

**Files:**
- Create: `TOOL\nublar_sounds\fingerprint.py`, `TOOL\nublar_sounds\manifest.py`, `TOOL\data\fingerprint_vector.json`; extend `rowmap.py` (`rename_clip`)
- Test: `TOOL\tests\test_fingerprint.py`, `TOOL\tests\test_manifest.py`, fixtures: copies of the T. rex clip and model files, edited per case under `tests\fixtures\changes\<case>\` (each an assets root, the form `Paths.mod_assets` has, which also holds the unedited feeder clip and model, because `fit.run` compares every subject)

**Interfaces:**
- Produces: `fingerprint.canonical(clip_json: dict, model_sha256: str) -> bytes`: the clip object without `sound_effects`, plus `"model_sha256"` (the SHA-256 hex of the species' `.geo.json` file's raw bytes, as on disk), as JSON with sorted keys, no spaces, strings unchanged, UTF-8. Every number is the exact binary value rounded HALF_EVEN to 6 decimal places, written with 6 decimals: Python `str(Decimal(value).quantize(Decimal("0.000001"), rounding=ROUND_HALF_EVEN))`, and negative zero is written `0.000000`. Not `%.6f`: at a tie such as 0.0078125, Python's `%.6f` gives `0.007812` and Java's `String.format("%.6f")` gives `0.007813` (HALF_UP), so the two sides would disagree. Both sides therefore round the exact binary value HALF_EVEN (Java: `new BigDecimal(value).setScale(6, RoundingMode.HALF_EVEN).toPlainString()`). `fingerprint.of(clip_json, model_sha256) -> str` = SHA-256 hex of the canonical bytes. `fingerprint_vector.json` = `{"clip": <a 3-bone excerpt with loop, a vector key, a pre/post key, a catmullrom key, a sound key, and the values 0.0078125, -0.0 and -1e-9 among its numbers>, "model_sha256": "<64 hex>", "expected": "<64 hex>"}`; Task 27's Java test reads the same file.
- Produces: `manifest.Manifest` over `WORK\clip_fit_manifest.json`: per subject (`subjects: {name: {kind: "entity"|"block", model_sha256, clips: {...}}}`; the T. rex is `tyrannosaurus_rex`, the feeder `carnivore_feeder_one`) per clip `{fingerprint, length_s (null on an unbounded clip), rows: [row_id], file_keys: [(time_s, effect)] (the clip file's keys at the last fit), anchors: [Anchor], sounds: {event_id: {start, reverse, curve_hint, parts: {part: {anchor, offset_ms, time_s}}, files}}, expected_keys: [(time_s, effect, event_id)], decision: "approved"|"pending"|"clip changed, listen again"|"clip gone"|"no fit"|"clip arrived, no fit"}`, and `rows_without_clip: {row_id: "none"}`. `manifest.compare(subject: clips.Subject, m: Manifest) -> list[Change(subject, clip, kind, detail)]` with kinds exactly: `keys_edited`, `length_changed`, `model_changed`, `renamed` (old gone and a new name whose fingerprint minus the name equals the old one's → `likely_match`), `removed`, `added` (a row map clip with no entry, on a row the manifest already holds under `rows_without_clip` or another clip; a row never fitted is not `added`, so a first run marks nothing; `compare` puts the row map's row ids in `detail["rows"]`, and `apply` creates the entry with them), `sound_keys_differ`. `compare` checks `model_sha256` first: a changed hash is `model_changed` on each clip. Sound keys: when the clip file's keys equal `expected_keys`, `compare` sets `file_keys = expected_keys` and reports nothing; otherwise, when they differ from `file_keys`, it reports `sound_keys_differ`. So a `key` row stays approved until a developer imports its keys, because the tool never writes keys into the mod. `manifest.apply(m, changes, decisions)` marks each clip and invalidates the approvals of its `rows`; `manifest.load(path: Path | None = None) -> Manifest` (no path = `WORK\clip_fit_manifest.json`), `Manifest.save()`; `Manifest.__getitem__(subject)` returns that subject's `clips` (`m["rows_without_clip"]` returns that map), `.subjects` stays raw, and `Manifest.view()` is `{subject: clips, "rows_without_clip": ...}`, the form `/api/state` serves; `Manifest.decision(clip, subject=None) -> str` (the subject may be left out when one subject has the clip; `AmbiguousClip` otherwise). The decision text per kind: `keys_edited`, `length_changed`, `model_changed`, `sound_keys_differ` → `clip changed, listen again`; `removed` → `clip gone`; `renamed` → the old name `clip gone`, the new name `no fit`, with `detail = {"old", "new", "likely_match": (old, new)}` and nothing moved until the lead accepts (Task 21 `accept_rename`); `added` → `clip arrived, no fit`. `Manifest.accept_rename(subject, old, new, decisions)` moves the entry, its anchors, sounds and offsets to the new name, sets its decision to `clip changed, listen again` (the lead still listens), drops the old entry and rewrites the clip name in `data\rows_to_clips.json` (`rowmap.rename_clip(subject, old, new)` over `rowmap.DATA_DIR`, which a test points at a copy).

- [ ] **Step 1: Write the failing tests**

```python
def test_vector(): v = json.load(open(VECTOR)); assert fingerprint.of(v["clip"], v["model_sha256"]) == v["expected"]
def test_sound_keys_do_not_change_it():
    a = fingerprint.of(clip, M); clip2 = {**clip, "sound_effects": {"0.5": {"effect": "x"}}}; assert fingerprint.of(clip2, M) == a
def test_loop_changes_it(): assert fingerprint.of({**clip, "loop": False}, M) != fingerprint.of(clip, M)
def test_numbers_compare_at_six_decimals():
    a = fingerprint.of(set_key(clip, "hips", "rotation", "0.5", [1, 0, 0]), M)
    assert fingerprint.of(set_key(clip, "hips", "rotation", "0.5", [1.0000001, 0, 0]), M) == a      # 1e-7 is under 6 places
    assert fingerprint.of(set_key(clip, "hips", "rotation", "0.5", [1.00001, 0, 0]), M) != a
def test_canonical_number_form():
    assert fingerprint.number(0.0078125) == "0.007812" and fingerprint.number(-0.0) == "0.000000" and fingerprint.number(-1e-9) == "0.000000"
    assert fingerprint.number(1) == "1.000000" and fingerprint.number(0.0000015) == "0.000002"      # exact binary of 1.5e-6 is above the tie
    assert b'"loop":true' in fingerprint.canonical(clip, M) and b"sound_effects" not in fingerprint.canonical(clip, M)
def test_model_changes_it(): assert fingerprint.of(clip, "0" * 64) != fingerprint.of(clip, M)
DECISION = {"keys_edited": "clip changed, listen again", "length_changed": "clip changed, listen again", "model_changed": "clip changed, listen again",
            "sound_keys_differ": "clip changed, listen again", "removed": "clip gone", "renamed": ("clip gone", "no fit"), "added": "clip arrived, no fit"}
@pytest.mark.parametrize("case", list(DECISION))
def test_each_change_is_found_and_approval_removed(case):
    species = clips.Species("tyrannosaurus_rex", FIXTURES / "changes" / case); m = baseline_manifest_with_approvals()
    changes = manifest.compare(species, m); assert {c.kind for c in changes} == {case}
    manifest.apply(m, changes, decisions)
    if case == "renamed":
        old, new = changes[0].detail["old"], changes[0].detail["new"]
        assert m.decision(old) == "clip gone" and m.decision(new) == "no fit" and changes[0].detail["likely_match"] == (old, new)
    else:
        assert m.decision(changes[0].clip) == DECISION[case]
    assert decisions.row(m["tyrannosaurus_rex"][changes[0].clip]["rows"][0])["approved"] is False
# The "added" fixture overrides rows_to_clips.json so T. rex `yawn` maps to a new `yawn` clip that the fixture clip file carries and the baseline manifest has as `none`.
def test_unchanged_clip_keeps_approval(): assert manifest.compare(species, m) == [] and decisions.row(...)["approved"] is True   # Review Focus 5, part
def test_keys_not_yet_imported_are_no_change():            # the file still has its old keys, and the fit expects others
    m = baseline_manifest_with_approvals(); e = m["tyrannosaurus_rex"]["drink"]; e["expected_keys"] = [(1.0, "ambient_drink", EVENT)]; e["file_keys"] = []
    assert manifest.compare(species, m) == []
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement** (`fingerprint.number(value) -> str` is the one place the number form lives), then compute and write `expected` into the vector by running `fingerprint.of` once and pasting the hex (the vector is then fixed; a later change to the canonical form must fail this test on purpose). **Step 4: Run the tests** → pass. **Step 5: Commit**: `git add -A && git commit -m "feat: clip fingerprint, manifest and change detection"`.

### Task 19: `fit` stage

**Files:**
- Create: `TOOL\nublar_sounds\fit.py`; extend `recipes.json` with `fit` recipes per T. rex row type (the `hurt` entry has `tail: "clip_end"`), `__main__.py` (registers `fit`; flag `--thing`)
- Test: `TOOL\tests\test_fit.py`

**Interfaces:**
- Consumes: `candidates.json`, `craft` outputs (`meta.json` with `hit_s` and `trimmed_start_s`), `clips`, `anchors`, `manifest`, `motiongraph` links, `recipes`, `data\foot_nudges.json`, `decisions`.
- Produces: `fit.layout(species, clip, row_type, links) -> list[Placed(event, share, source: "graph"|"anchor")]`: the mapped JWE3 clip's sounds in curve order with their first curve x as the start share, or the row's sound at its anchor when no JWE3 clip matches.
- Produces: `fit.fit_row(row_id, paths, ctx) -> FitResult(sounds: dict[event_id, FittedSound(clip, start: "key"|"code", lead_in_s: float, parts: dict[part_name, Part(anchor: Anchor, offset_ms: int, time_s: float, file: Path | None)])])`; a plain sound has one part `main`; a blend has one part per voice, bed and fall. A sound on more than one clip (`entity.carnivore.eat` on `eat1` and `eat2`) has one `FittedSound` per clip, under the key `<event_id>@<clip>`; a sound on one clip keeps the key `event_id` (`step.walk` on `move.walk`, `step.run` on `run`, by the row map's entry per sound). Per sound and clip: time = anchor time + `offsets_ms` from decisions; `key` sounds get the time as an expected sound key `(time_s, effect, event_id)` with effect name `<group>_<name>` (the developer wires effect to event in F2.3); `code` sounds get the time as lead-in: the file is padded or trimmed at its start so the hit lands at the anchor when the game starts it at the clip's start; variants in a set share one hit; one-shot clips that do not hold, and any row whose `fit` recipe has `tail: "clip_end"` (`hurt`: `flinch` holds, and the design allows ringing on but does not require it; the lead can overrule by ear): trim or fade the file to end within 0.25 s of the clip end; looping clips: `loop_crossfade` check; death: the blend with `fall_times_s` from the body drop anchor (`hips`) or the recipe shares when `anchor unclear`; shared sets (`entity.carnivore.eat`) are only trimmed, never cut to one clip, and an index `WORK\fit\shared_index.json` links the file to its clips and rows. Then `clean.render(..., trim=False)` last, so the lead-in and the padded start stay. Writes `WORK\fit\<species>\<clip>\<event>\<n>.ogg`, `WORK\sound_keyframes.json` (every non-footstep approved-or-pending sound's keys per clip), updates the manifest entry: `decision: "pending"` on a new entry only, every other decision kept; `rows`, `file_keys`, `curve_hint` (the sound's first curve x from `fit.layout`, null with no graph link) and the subject's `model_sha256` are written here.
- Produces: footstep rows: `fit_row` measures each foot's landing with `anchors.foot_landings` (`move.walk`, `run`; feet from the row map), adds the lead's offset from `decisions.offsets_ms[event][foot]` (seeded on the first run from `data\foot_nudges.json` when the decision has no offset for that clip and foot), adds the median of the ticked set's `trimmed_start_s` from `craft`'s `meta.json` (each file has its own value, and one key plays any file of the set), and writes `WORK\footsteps\<species>.json`: per gait `{clip, length_s, steps: [{foot, landing_s, offset_ms, trimmed_start_s, time_s, hit_s, flags}]}` with `time_s = landing_s + offset_ms / 1000 + trimmed_start_s` wrapped into the clip length, plus the matching `expected_keys` in the manifest. It writes the step files `WORK\fit\<species>\<clip>\step.<gait>\<n>.ogg` through `recipes.step_tail`, with the clip's longest gap between steps (one key plays any file at any step), then `clean.render(..., trim=False)`. A changed walk clip therefore gets new landings with the same offsets.
- Produces: `fit.run(paths, rows: list[str] | None)`: first `manifest.compare` over every subject in the manifest and the row map, entity and block (mark changes, invalidate), then fit every pending row and every manifest row whose clip changed; a row whose clip is `clip gone` or `no fit` is skipped until the lead answers the rename pair (a `clip arrived, no fit` row is fitted); rows with `clip: none` get a manifest entry `none` and skip; a `kind: "none"` section (`incubator`, found through `rowmap.section_for`) gets no compare; until Task 24, rows of a `kind: "block"` section are skipped and listed as `blocked: not built yet`, `stage` leaves them out, and their clips are still compared; rows of a species with no row-map section (`rowmap.has_section` false) are skipped and listed as `blocked: no row map entry`. A clip whose mapped JWE3 clip words match several JWE3 clips takes the one with the most sound links and lists the others in the manifest as `alternatives`.

- [ ] **Step 1: Write the failing tests**

```python
def test_roar_layout_comes_from_the_graph():
    placed = fit.layout("tyrannosaurus_rex", "roar", "roar", LINKS); assert placed[0].source == "graph" and "Trex_HeroRoar_Pre" in [p.event for p in placed]
def test_curve_hint_is_written():
    fit.fit_row("tyrannosaurus.roar", paths, ctx); assert "curve_hint" in manifest.load()["tyrannosaurus_rex"]["roar"]["sounds"]["entity.tyrannosaurus_rex.vocals.roar"]
def test_death_falls_sit_on_the_body_drop():
    r = fit.fit_row("tyrannosaurus.death", paths, ctx); fall = r.sounds["entity.tyrannosaurus_rex.death"].parts["Fighting_BodyFall_Heavy"]
    assert fall.anchor.kind == "drop" and abs(fall.time_s - fall.anchor.time_s) < 0.001
def test_code_sound_gets_lead_in():
    r = fit.fit_row("tyrannosaurus.roar", paths, ctx); s = r.sounds["entity.tyrannosaurus_rex.vocals.roar"]; main = s.parts["main"]
    assert s.start == "code" and main.time_s > 0
    assert abs(audio.hit_time(vgm.load_mono48(main.file)) - main.time_s) < 0.02                 # the lead-in survives clean.render(trim=False)
def test_a_sound_on_two_clips_has_one_fit_per_clip():
    r = fit.fit_row("tyrannosaurus.eat_flesh_rip_crunch", paths, ctx); assert r.sounds["entity.carnivore.eat@eat1"].clip == "eat1" and r.sounds["entity.carnivore.eat@eat2"].clip == "eat2"
    r = fit.fit_row("tyrannosaurus.footsteps_heavy", paths, ctx); assert r.sounds["entity.tyrannosaurus_rex.step.walk"].clip == "move.walk" and r.sounds["entity.tyrannosaurus_rex.step.run"].clip == "run"
def test_rerun_keeps_a_decision(tmp_work):
    fit.run(paths, ["tyrannosaurus.yawn", "tyrannosaurus.drink"]); e = manifest.load()["tyrannosaurus_rex"]["drink"]; assert e["decision"] == "pending" and e["rows"] == ["tyrannosaurus.drink"]
    m = manifest.load(); m["tyrannosaurus_rex"]["drink"]["decision"] = "clip changed, listen again"; m.save()
    fit.fit_row("tyrannosaurus.drink", paths, ctx); assert manifest.load().decision("drink") == "clip changed, listen again"
def test_rows_of_a_gone_clip_are_skipped(tmp_repo_copy):
    rename_clip_in_file(tmp_repo_copy, "tyrannosaurus_rex", "drink", "drink_new"); fit.run(paths_with(tmp_repo_copy), None)
    assert manifest.load().decision("drink") == "clip gone" and manifest.load().decision("drink_new") == "no fit"
def test_key_sound_writes_expected_keys():
    r = fit.fit_row("tyrannosaurus.drink", paths, ctx); keys = json.load(open(WORK / "sound_keyframes.json"))["tyrannosaurus_rex"]["drink"]
    assert keys and all(0 <= k["time_s"] <= 9.125 for k in keys) and keys[0]["event_id"] == "entity.tyrannosaurus_rex.ambient.drink"
def test_offsets_apply():
    decisions.set_offset("tyrannosaurus.drink", EVENT, "main", 120); part = fit.fit_row("tyrannosaurus.drink", paths, ctx).sounds[EVENT].parts["main"]
    assert abs(part.time_s - (part.anchor.time_s + 0.120)) < 0.001
def test_changed_clip_copy_is_marked_and_refit(tmp_repo_copy):     # Done means: a changed copy of a T. rex clip
    shift_keys(tmp_repo_copy, "tyrannosaurus_rex", "drink", +0.2); fit.run(paths_with(tmp_repo_copy), None)
    m = manifest.load(); assert m["tyrannosaurus_rex"]["drink"]["decision"] == "clip changed, listen again" and decisions.row("tyrannosaurus.drink")["approved"] is False
    assert abs(m[...]["sounds"][event]["parts"]["main"]["time_s"] - old_time) >= 0.01   # its sounds moved
def test_none_rows_have_entries(): assert manifest.load()["rows_without_clip"]["tyrannosaurus.yawn"] == "none"
def test_footstep_times_are_landing_plus_offset():
    fit.fit_row("tyrannosaurus.footsteps_heavy", paths, ctx); walk = json.load(open(WORK / "footsteps/tyrannosaurus_rex.json"))["walk"]
    for s in walk["steps"]: assert abs(s["time_s"] - wrap(s["landing_s"] + s["offset_ms"] / 1000 + s["trimmed_start_s"], walk["length_s"])) < 1e-6
    assert {s["offset_ms"] for s in walk["steps"]} == {-115}                                         # seeded from foot_nudges.json
def test_footstep_times_reproduce_the_design_table():      # landing + nudge, before any trim, must give the design's "Step start times"
    steps = json.load(open(WORK / "footsteps/tyrannosaurus_rex.json"))
    assert sorted(round(s["landing_s"] + s["offset_ms"] / 1000, 3) for s in steps["walk"]["steps"]) == [0.794, 1.858]
    assert sorted(round(s["landing_s"] + s["offset_ms"] / 1000, 3) for s in steps["run"]["steps"]) == [0.097, 0.590]
    # If this fails by more than 1 ms, stop and report NEEDS_CONTEXT with both numbers: the table may carry a rounding from the spike's v3 file.
def test_blend_part_offset_rebuilds_the_file():
    before = fit.fit_row("tyrannosaurus.death", paths, ctx).sounds[DEATH].parts["Trex_BreathWheeze"]; before_bytes = before.file.read_bytes()   # fit writes to one fixed path
    decisions.set_offset("tyrannosaurus.death", DEATH, "Trex_BreathWheeze", 150); after = fit.fit_row("tyrannosaurus.death", paths, ctx).sounds[DEATH].parts["Trex_BreathWheeze"]
    assert abs(after.time_s - before.time_s - 0.150) < 0.002 and after.file.read_bytes() != before_bytes
def test_unbounded_clip_keeps_its_share_at_zero():
    main = fit.fit_row("tyrannosaurus.breath", paths, ctx).sounds[BREATH].parts["main"]            # share 0.0 on misc.idle, which has no finite length
    assert main.time_s == 0.0 and "anchor unclear" in main.anchor.flags and manifest.load()["tyrannosaurus_rex"]["misc.idle"]["length_s"] is None
def test_length_change_moves_share_anchors(tmp_repo_copy):    # a fixture clip with timed keys and no animation_length, on a share above 0
    add_clip(tmp_repo_copy, ("entity", "tyrannosaurus_rex"), "share_fixture", last_key_s=2.0); link_row_map("tyrannosaurus_rex", "breath", None, ["share_fixture"], anchor=AnchorSpec("share", "", share=0.5))
    p = paths_with(tmp_repo_copy); old = fit.fit_row("tyrannosaurus.breath", p, ctx).sounds[BREATH].parts["main"]; assert abs(old.time_s - 1.0) < 0.001
    extend_last_key(tmp_repo_copy, "tyrannosaurus_rex", "share_fixture", +1.0); fit.run(p, None)
    new = manifest.load()["tyrannosaurus_rex"]["share_fixture"]["sounds"][BREATH]["parts"]["main"]
    assert new["anchor"]["kind"] == "share" and abs(new["time_s"] - 1.5) < 0.001 and manifest.load().decision("share_fixture") == "clip changed, listen again"
def test_length_change_rebuilds_a_cut_tail(tmp_repo_copy):
    before = fit.fit_row("tyrannosaurus.hurt", paths, ctx).sounds[HURT].parts["main"].file; a = clean.measure(before).seconds
    set_length(tmp_repo_copy, "tyrannosaurus_rex", "flinch", 1.0); fit.run(paths_with(tmp_repo_copy), None)
    assert clean.measure(before).seconds < a                                                        # the one-shot ends within 0.25 s of the new, shorter clip end
def test_model_change_refits(tmp_repo_copy):
    touch_model(tmp_repo_copy, "tyrannosaurus_rex"); fit.run(paths_with(tmp_repo_copy), None)
    m = manifest.load(); assert all(m.decision(c) == "clip changed, listen again" for c in m["tyrannosaurus_rex"]) and m["tyrannosaurus_rex"]["dead"]["anchors"]
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** Run `fit --thing Tyrannosaurus` in the background (it renders no video yet). **Step 4: Run the tests** → pass. **Step 5: Commit**: `git add -A && git commit -m "feat: fit stage with anchors, offsets, manifest"`.

### Task 20: Clip renders and mux

**Files:**
- Create: `TOOL\nublar_sounds\render.py`
- Modify: `TOOL\nublar_sounds\fit.py` (`fit.run` calls `render.for_row`)
- Test: `TOOL\tests\test_render.py` (one 1 s clip at 320×180, supersampling 1)

**Interfaces:**
- Produces: `render.clip_video(subject: clips.Subject, clip, out_dir, paths, size=(1280, 720), ss=2, seconds=None, feet: dict[foot, time_s] | None = None, markers: list[(time_s, label)] = (), texture: Path | None = None) -> RenderInfo(mp4, json, length, cycles, count, seconds, fps=24)`: `render.compile_dust_trail(...)` loads `REPO\tools\concept-render\dust_trail.py` as source text and compiles it with these anchors swapped in, as `clip_video.py` does (`assert old in src` on each anchor; on a failed anchor stop and report `NEEDS_CONTEXT`, do not edit `dust_trail.py`): the assets root (`ASSETS = Path("common/src/main/resources/assets/projectnublar")` → `paths.mod_assets`, because the tool runs from `TOOL` and a changed copy must render its own clip), the clip name (`["animations"]["run"]`), the clip length (`CLIP_LEN = clip["animation_length"]` → the `clips.length_rule` value, and 2.0 on an unbounded clip, because `misc.idle` and `restidle` have no `animation_length`), frame size, supersampling, the dirt ground, and for a block subject also the geo path (`geckolib/models/entity/` → `geckolib/models/block/`), the animation path (`animations/entity/` → `animations/block/`), the texture line (`textures/entity/{SPECIES_NAME}/male/{SPECIES_NAME}.png` → the given `texture`), a `SPECIES` entry for the block (`scale=1.0, feet=[], puffs=0, life=1.0`; `puffs_at` reads `life`), `GROUND_Y = min(FOOT_MIN.values())` → `GROUND_Y = 0.0` and the two-line `SPEED = float(np.median(...))` statement (`dust_trail.py` lines 253-254) → `SPEED = 0.0` (with no feet both fail at import, so these are source replacements, not values set after `exec`), and a linear `sample` (before `p0 = np.array(keys[i - 1][1] if i > 0 else v0)`, insert `return np.array(v0) + (np.array(v1) - np.array(v0)) * s`), because the feeder's keys have no `lerp_mode` and the game plays them linear, as `anchors.bone_moves` measures them. An entity render stays on `dust_trail.py`'s Catmull-Rom, so the lead's nudges keep their meaning. When `clips.play_mode(subject, clip) == "HOLD"`, also swap `mats = bone_matrices(t % CLIP_LEN)` → `mats = bone_matrices(min(t, CLIP_LEN))`; a HOLD render calls `dt.render(moment)` with no `warm` (no dust to warm up, and `warm` would pin every frame to the last pose), and its marker strip uses `moment` unwrapped. Walk and run clips get the dust and the foot dots at the step times; every other clip is a non-gait render: `dt.TOUCH = []`, `puffs = 0`, the ground does not scroll (`SPEED = 0.0`, the same source replacement), no dust, and a marker strip along the bottom with one dot per sound that lights for 125 ms at each sound's time. A block render looks down more (`pitch` 30) and clips cubes below `y = 0` (an in-ground hatch's shaft is under the ground), and keeps the contact shadow off (`shadow_map` returns zeros), because a hatch flush with the ground casts none. Frames are cached per (fingerprint, size, feet, markers) so only new or changed clips render. `seconds` defaults to `max(clip length, 2 s)` rounded up to whole cycles for loops, one pass for holds, and 2 s on an unbounded clip; `count = round(seconds × fps)`, so a 0.9583 s `dead` renders 2 s with the last frame held. T. rex `misc.idle` moves only through Molang, and `vec` reads Molang as 0, so the breath row renders a still pose, and the lead judges breath on a still pose.
- Produces: `render.mux(mp4, tracks: list[(time_s, np.ndarray)], out_mp4, lufs) -> Path` (carry `mux_clips.mux` and the per-moment placement of `track`; no deletion of anything; both ffmpeg calls pass `-fflags +bitexact -flags +bitexact`, as `clean.render` does), and `render.for_row(row_id, ctx)` → for each clip of the row, `WORK\renders\<species>\<clip>\silent.mp4`, `<row_id>.mp4` with the row's ticked sounds built in, and the `.json` (`ctx.paths` gives the folders). A `key` sound goes in at its `time_s`; a `code` sound goes in at 0, because its file holds the lead-in.
- Produces: `fit.run(paths, rows, videos: bool = False)`: with `videos=True`, it calls `render.for_row` for each row that is new or changed in this run. `fit`'s `cli` passes `videos=True`, so `fit` and `all` render as the design says; the unit tests use the default and render nothing.

- [ ] **Step 1: Write the failing tests**

```python
def test_small_render_writes_video_and_json(tmp_path):
    info = render.clip_video(TREX, "dead", tmp_path, paths, size=(320, 180), ss=1, markers=[(0.86, "fall")])
    assert info.mp4.exists() and info.json.exists() and info.count == round(info.seconds * info.fps) and info.seconds >= 2.0
def test_non_gait_render_has_no_dust_or_ground_motion(tmp_path):
    info = render.clip_video(TREX, "dead", tmp_path, paths, size=(320, 180), ss=1); first, last = frame(info, 0), frame(info, info.count - 1)
    assert ground_rows_equal(first, last)        # the ground band under the model does not scroll in a non-gait render
    assert pixel_difference(model_region(frame(info, 24)), model_region(frame(info, info.count - 1))) < 0.001   # 1.0 s is past the end of `dead`: a HOLD clip holds, and does not replay
def test_block_render_shows_the_hatch_opening(tmp_path):
    info = render.clip_video(clips.Block("carnivore_feeder_one"), "open", tmp_path, paths, size=(320, 180), ss=1, texture=FEEDER_TEXTURE, markers=[(1.0, "doors")])
    first, last = frame(info, 0), frame(info, info.count - 1)
    assert info.length == 9 and pixel_difference(model_region(first), model_region(last)) > 0.02      # the doors slid and the platform rose
    assert ground_rows_equal(first, last) and no_pixels_below_ground_line(last)
def test_cache_skips_unchanged(tmp_path, monkeypatch):
    render.clip_video(TREX, "dead", tmp_path, paths, size=(320, 180), ss=1)                                # the first call fills the frame cache
    monkeypatch.setattr(render, "compile_dust_trail", explode)                                              # a second compile raises
    assert render.clip_video(TREX, "dead", tmp_path, paths, size=(320, 180), ss=1).mp4.exists()
def test_fit_renders_new_or_changed_rows_only(monkeypatch, tmp_work):
    calls = []; monkeypatch.setattr(render, "for_row", lambda row_id, ctx: calls.append(row_id))
    fit.run(paths, ["tyrannosaurus.drink"], videos=True); assert calls == ["tyrannosaurus.drink"]
    calls.clear(); fit.run(paths, ["tyrannosaurus.drink"], videos=True); assert calls == []
    fit.run(paths, ["tyrannosaurus.drink"]); assert calls == []
def test_mux_keeps_video_length(tmp_path): out = render.mux(info.mp4, [(0.86, click)], tmp_path / "m.mp4", -20); assert abs(ffprobe_duration(out) - ffprobe_duration(info.mp4)) < 0.05
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** The feeder's texture is the one `CarnivoreFeederRenderer` picks for a full feeder; start value `textures\block\feeder\meat\feeder_one\full.png` (the art list's Done row); if the renderer names another file, use that one. **Step 4: Run the tests** → pass. Run `fit --thing Tyrannosaurus` in the background: it now renders the T. rex clips with sounds (about 45 s of clip, several minutes). **Step 5: Commit**: `git add -A && git commit -m "feat: clip renders and mux, entity and block"`.

### Task 21: Review page version 2: clips, sliders, refit — milestone 2

**Files:**
- Modify: `TOOL\nublar_sounds\review\server.py`, `TOOL\nublar_sounds\review\page.html`, `TOOL\nublar_sounds\render.py` (`<row_id>.videos.json`)
- Test: extend `TOOL\tests\test_review_server.py`

**Interfaces:**
- Produces: `GET /api/state` now includes `manifest` and, per row, `render: {silent, built, json}` for the row's first clip and `extra_renders: [{clip, sound, silent, built, json}]` for each other video (T. rex footsteps: `run`, sound `step.run`; eat: `eat2`; Task 24 adds the reversed `open`). `render.for_row` writes `WORK\renders\<row_id>.videos.json`, a list of `{clip, sound, silent, built, json, ticks, offsets}`: the first entry is `render`, the rest are `extra_renders`, and `/api/state` and the current check read this file; `POST /api/refit` `{row_id}` runs `fit.fit_row` then `render.for_row` for that row in a worker thread and answers `{job: id}`; `GET /api/job/<id>` → `{done, error}`. `POST /api/decision` with `offsets_ms` or `ticked` stores them and the page calls refit: a candidate shows its clip fit once ticked, because over 1,000 candidates per sound cannot all be fitted, so a tick refits and the lead hears the candidate on the clip. A `ticked` or `offsets_ms` body that does not itself set `approved: true` clears `approved`, and each of the row's clips goes back from `approved` to `pending`, so a pick or a timing the lead did not approve on a built video cannot stage. `POST /api/decision` with `accept_rename: {old, new}` calls `Manifest.accept_rename` (Task 18) for a pair the manifest lists as `likely_match`, then refits the rows of the new clip; 400 for a pair the manifest does not list. The page shows the pair under the old row with "Same clip, renamed" and "Not the same" buttons. The v1 rule "409 on approve for a clip row" is replaced by "409 until every built video of the row is current" (current: each entry's `ticks` and `offsets` in `<row_id>.videos.json` equal the row's present ones; a body with `leave_missing: true` is exempt). `POST approved: true` on a clip row sets the manifest entry of each of its clips to `approved` once all rows on that clip are approved, so a row marked "clip changed, listen again" can reach staging again.
- Produces: `page.html` for a row with a clip: the silent video with live sounds scheduled from the manifest times plus offsets (a `code` sound's file holds its lead-in, so it starts at 0, and a live slider shifts it by the offset delta; carry `nudge.html` `tick`, `loadSteps`, `stopAll`, speed buttons 1×, 0.5×, 0.25×, dots per sound or blend part, `syncLog`), one slider per sound or blend part (-200 to +200 ms, step 5), "Rebuild and play the built video" (plays `<row_id>.mp4`, the final check), the "clip changed, listen again" mark with "anchors moved: yes/no" from the manifest, "Approve row" disabled until every built video has been generated for the current ticks and offsets. Each entry of `extra_renders` gets its own video and sliders. A sound with a graph link shows its first curve x (`fit.layout`, stored in the manifest as `curve_hint`) as a hint, labelled "curve position, not confirmed". Narration: every slider has `aria-label` and `aria-valuetext` as in `nudge.html`.

- [ ] **Step 1: Write the failing tests**

```python
def test_state_carries_render_and_manifest(server): s = get("/api/state"); r = s["rows"]["tyrannosaurus.death"]; assert r["render"]["built"].endswith(".mp4") and s["manifest"]["tyrannosaurus_rex"]["dead"]["fingerprint"]
def test_refit_job_updates_offsets_and_render(server):
    post("/api/decision", {"row_id": "tyrannosaurus.drink", "offsets_ms": {"entity.tyrannosaurus_rex.ambient.drink": {"main": 50}}})
    job = post("/api/refit", {"row_id": "tyrannosaurus.drink"})["job"]; wait(job)
    assert manifest.load()[...]["parts"]["main"]["offset_ms"] == 50 and mtime(built_mp4) > before
def test_two_clip_row_has_an_extra_render(server):
    r = get("/api/state")["rows"]["tyrannosaurus.footsteps_heavy"]; assert r["render"]["built"].endswith(".mp4")
    assert [(e["clip"], e["sound"]) for e in r["extra_renders"]] == [("run", "entity.tyrannosaurus_rex.step.run")] and r["extra_renders"][0]["built"].endswith(".mp4")
def test_approve_requires_a_current_build(server):
    row, event = "tyrannosaurus.drink", "entity.tyrannosaurus_rex.ambient.drink"
    post("/api/decision", {"row_id": row, "offsets_ms": {event: {"main": 40}}})                     # the offset moved and no rebuild ran: the build is stale
    assert post_status("/api/decision", {"row_id": row, "approved": True}) == 409
    wait(post("/api/refit", {"row_id": row})["job"]); assert post_status("/api/decision", {"row_id": row, "approved": True}) == 200
    post("/api/decision", {"row_id": row, "ticked": {event: [another_candidate(row, event)]}})      # a tick makes the build stale too, and removes the approval
    assert decisions.row(row)["approved"] is False
    assert post_status("/api/decision", {"row_id": row, "approved": True}) == 409
    assert post_status("/api/decision", {"row_id": row, "approved": True, "leave_missing": True}) == 200
def test_accept_rename_moves_the_entry_and_offsets(server_with_renamed_fixture):   # Task 18's "renamed" fixture: `drink` is now `drink_new`
    decisions.set_offset("tyrannosaurus.drink", EVENT, "main", 70); fit.run(paths, None)
    m = manifest.load(); assert m.decision("drink") == "clip gone" and m.decision("drink_new") == "no fit"
    assert m["tyrannosaurus_rex"]["drink"]["sounds"][EVENT]["parts"]["main"]["offset_ms"] == 70                 # unchanged before accepting
    post("/api/decision", {"row_id": "tyrannosaurus.drink", "accept_rename": {"old": "drink", "new": "drink_new"}})
    m = manifest.load(); assert "drink" not in m["tyrannosaurus_rex"] and m["tyrannosaurus_rex"]["drink_new"]["sounds"][EVENT]["parts"]["main"]["offset_ms"] == 70
    assert rowmap.clip_entry("tyrannosaurus_rex", "drink").clips == ["drink_new"]                                # rowmap.DATA_DIR points at a copy here
    assert m.decision("drink_new") == "clip changed, listen again"
    assert post_status("/api/decision", {"row_id": "tyrannosaurus.drink", "accept_rename": {"old": "roar", "new": "drink_new"}}) == 400
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** **Step 4: Run the tests** → pass; open the page, play the T. rex `dead` row at 0.25× and 1×, move a slider, rebuild. Screenshot for the report.

- [ ] **Step 5: Hand to the lead (milestone 2).** The lead judges the T. rex clip-tied rows (death on `dead`, drink, roar, footsteps on walk and run, eat, nest call, contact call, hurt, bite and snarl on `attack1`, breath, sleep). The agent reports the sync log's error statistics (ms) and nothing about how it sounds.

- [ ] **Step 6: Commit**: `git add -A && git commit -m "feat: review page v2 with clip videos, sliders and refit"`.

### Task 22: `stage`, `check` and `report`

**Files:**
- Create: `TOOL\nublar_sounds\stage.py`, `TOOL\nublar_sounds\check.py`, `TOOL\nublar_sounds\report.py`
- Modify: `TOOL\nublar_sounds\__main__.py` (registers `stage`, `check` and `all`)
- Test: `TOOL\tests\test_stage.py`, `TOOL\tests\test_check.py`

**Interfaces:**
- Produces: `stage.run(paths)` writes `STAGED\` as the design's list: `assets\projectnublar\sounds\entity\tyrannosaurus_rex\<group>\<name>_<n>.ogg` (the 7 frozen ids use the real folders in the table below) and `sounds\block\incubator\...`; `sounds.fragment.json` (entries for approved sounds; `"stream": true` only at 10 s or more; block and machine entries carry `"pitch"` and `"volume"` variants, dinosaur entries do not; subtitle key `subtitles.projectnublar.<event>` only for roar, call, threat, hurt and death classes; full replacement entries for the 7 T. rex ids); `en_us.fragment.json` (`"subtitles.projectnublar.<event>": "Tyrannosaurus roars"` form, for the subtitle classes only); `sound_events.txt` (new ids, not the 7 frozen ones); `footstep_keyframes.json` (from `WORK\footsteps\*.json`, approved rows only) and `sound_keyframes.json` (approved rows): each is the complete key list per clip, written to replace the clip's `sound_effects`, not to add to them (the Brachiosaurus clips already carry 4 keys each); `clip_fit_manifest.json` (a copy of `WORK`'s, decisions included); `art_list_status.md` (proposed status and Where text per pending row, by the design's status table, including the "D91 open" note on the 7 T. rex rows, and PK's "Prehistoric Kingdom: replace if JWE3 gets a match"); `provenance.json` (per file, with the sound's first curve x as `curve_hint`, a hint only); `report.md` (every pending row as staged, gap or blocked with its reason; rule check results; old files the new sets no longer use, such as `roar_6..10`; size per species and total, flag over 10 MB). A row is staged only when `approved` is true and its manifest entries are not marked changed, gone or no fit. A row with `leave_missing: true` stages nothing and is a gap in `report.md`. A shared file stays while another approved clip uses it.

The 7 frozen ids keep the folders and file stems of the mod's `sounds.json`, so each staged event is a complete set and `report.md` can list the old files it no longer uses:

| Frozen id, under `entity.tyrannosaurus_rex` | Folder, under `sounds\entity\tyrannosaurus_rex\` | File stem |
|---|---|---|
| `.vocals.roar` | `vocals\roar\` | `roar_<n>` |
| `.vocals.growl` | `vocals\growl\` | `growl_<n>` |
| `.ambient` | `ambient\` | `breath_<n>` |
| `.attack.snarl` | `attack\snarl\` | `attack_snarl_<n>` |
| `.attack.bite` | `attack\bite\` | `bite_<n>` |
| `.hurt` | `hurt\` | `hurt_<n>` |
| `.death` | `death\` | `death_<n>` |

- Produces: `check.run(paths) -> int` (0 pass, 1 problems) over `STAGED`: mono, Vorbis, true peak ≤ -1, no stream under 10 s, each fragment entry has its files and each file has an entry, ids unique, every JSON valid, every expected sound key present with no extras (compares `sound_keyframes.json` and `footstep_keyframes.json` with the `expected_keys` of the manifest clips whose decision is `approved`, leaving out the sounds of rows that `stage` did not stage (`leave_missing`, approval removed), over every subject, entity and block), every row in the row map has a manifest entry or `none`, rows of a species with no row-map section listed as `blocked: no row map entry`, and rows `blocked: not built yet` (Task 19) listed too (notes, not failures), `clips.loop_differences` reported as notes. `check` and `fit` both run `manifest.compare` first.

- [ ] **Step 1: Write the failing tests**

```python
def test_only_approved_rows_are_staged(tmp_work_with_decisions):
    stage.run(paths); staged = set(ids_in(STAGED / "sounds.fragment.json")); assert "entity.tyrannosaurus_rex.ambient.drink" in staged and "entity.tyrannosaurus_rex.vocals.yawn" not in staged
CASES = {"keys_edited": ("dead", "tyrannosaurus.death", "entity.tyrannosaurus_rex.death"), "length_changed": ("flinch", "tyrannosaurus.hurt", "entity.tyrannosaurus_rex.hurt"),
         "model_changed": ("dead", "tyrannosaurus.death", "entity.tyrannosaurus_rex.death"), "renamed": ("drink", "tyrannosaurus.drink", "entity.tyrannosaurus_rex.ambient.drink"),
         "removed": ("nesting", "tyrannosaurus.nest_call", "entity.tyrannosaurus_rex.vocals.nest_call"), "added": ("yawn", "tyrannosaurus.yawn", "entity.tyrannosaurus_rex.vocals.yawn"),
         "sound_keys_differ": ("drink", "tyrannosaurus.drink", "entity.tyrannosaurus_rex.ambient.drink")}
@pytest.mark.parametrize("case", list(CASES))               # Done means, over Task 18's 7 fixtures
def test_changed_clip_row_cannot_stage(case, tmp_work):
    clip, row_id, event = CASES[case]; approve_all(); paths2 = paths_with(FIXTURES / "changes" / case)
    fit.run(paths2, None); stage.run(paths2)
    assert event not in ids_in(STAGED / "sounds.fragment.json") and "blocked" in row_line(STAGED / "report.md", row_id)
    assert decisions.row(row_id)["approved"] is False
def test_changed_walk_clip_blocks_footsteps_and_moves_keys(tmp_work):
    approve_all(); before = json.load(open(WORK / "footsteps/tyrannosaurus_rex.json"))["walk"]["steps"]
    shift_keys(copy, "tyrannosaurus_rex", "move.walk", +0.2); fit.run(paths_with(copy), None); stage.run(paths_with(copy))
    after = json.load(open(WORK / "footsteps/tyrannosaurus_rex.json"))["walk"]["steps"]
    assert "entity.tyrannosaurus_rex.step.walk" not in ids_in(STAGED / "sounds.fragment.json") and "blocked" in row_line(STAGED / "report.md", "tyrannosaurus.footsteps_heavy")
    assert all(abs(a["landing_s"] - b["landing_s"]) >= 0.01 and a["offset_ms"] == b["offset_ms"] for a, b in zip(sorted(after, key=foot), sorted(before, key=foot)))
def test_changed_row_reapproved_reaches_staging(tmp_work, server_on):                  # change, refit, approve, stage
    approve_all(); shift_keys(copy, "tyrannosaurus_rex", "drink", +0.2); p = paths_with(copy); fit.run(p, None)
    assert manifest.load().decision("drink") == "clip changed, listen again"
    srv = server_on(p); wait(post(srv, "/api/refit", {"row_id": "tyrannosaurus.drink"})["job"])
    assert post_status(srv, "/api/decision", {"row_id": "tyrannosaurus.drink", "approved": True}) == 200 and manifest.load().decision("drink") == "approved"
    stage.run(p); assert "entity.tyrannosaurus_rex.ambient.drink" in ids_in(STAGED / "sounds.fragment.json")
def test_leave_missing_clip_row_stages_nothing(tmp_work):
    approve_all(); approve("tyrannosaurus.drink", leave_missing=True); stage.run(paths)
    assert "entity.tyrannosaurus_rex.ambient.drink" not in ids_in(STAGED / "sounds.fragment.json") and "gap" in row_line(STAGED / "report.md", "tyrannosaurus.drink")
    assert check.run(paths) == 0
def test_unmapped_species_rows_are_blocked_not_crashed():
    assert "blocked: no row map entry" in row_line(STAGED / "report.md", "velociraptor.death") and check.run(paths) == 0
def test_every_pending_row_in_report(): assert len(rows_in(STAGED / "report.md")) == 161
def test_fragment_rules():
    f = json.load(open(STAGED / "sounds.fragment.json"))
    assert all("stream" not in s or seconds_of(s) >= 10 for e in f.values() for s in e["sounds"])
    assert "pitch" not in f["entity.tyrannosaurus_rex.death"]["sounds"][0]                 # Task 23 asserts the block side on the hum
    assert f["entity.tyrannosaurus_rex.death"]["sounds"][0]["name"].startswith("projectnublar:entity/tyrannosaurus_rex/death/death_")   # a frozen id keeps its real folder
    assert f["entity.tyrannosaurus_rex.death"]["subtitle"] == "subtitles.projectnublar.entity.tyrannosaurus_rex.death" and "subtitle" not in f["entity.tyrannosaurus_rex.step.walk"]
def test_check_passes_on_staged_and_fails_on_a_planted_fault():
    assert check.run(paths) == 0; plant_stereo_file(STAGED); assert check.run(paths) == 1
def test_rerun_is_byte_identical(tmp_work):                 # Review Focus 5
    stage.run(paths); a = tree_hash(STAGED); fit.run(paths, None); stage.run(paths); assert tree_hash(STAGED) == a and no_row_marked_listen_again()
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement the three modules and wire `all` = pull, convert, map, craft, fit.** Each `all` runs `pull`/`convert --source pk` once; `--thing` maps a species thing to `pull --species`, `pull --graphs` and `convert --species`, and a non-animal thing to `pull --park` and `convert --park` (Task 23); `craft` and `fit` take the thing as given. **Step 4: Run the tests** → pass; run `stage` and `check` on the lead's approvals so far. **Step 5: Commit**: `git add -A && git commit -m "feat: stage, check and report"`.

### Task 23: Incubator rows and the park banks

**Files:**
- Modify: `TOOL\data\rows_to_names.json` (Incubator tokens, if Task 8 left any out), `TOOL\data\recipes.json` (`layer.incubator_start`: `Amb_Incubation_Start` crossfaded 0.5 s into the head of `Amb_Incubation_Loop`, 0.8 s fade, room 0.4 s at -16 dB, compressor as the generator sample in `build_steps.py`; `loop.hum`: `loop_crossfade` 0.5 s on the repo hum and on `Amb_Incubation_Loop`), `candidates.py` (unnamed park candidates), `craft.py` (`layer:*`, loops, lid trim to 1.0 s), `pull.py` and `convert.py` (park banks), `review\server.py` and `review\page.html` (paged candidates, audio on demand), `__main__.py` (flag `--park`)
- Test: `TOOL\tests\test_incubator.py`, `TOOL\tests\test_unnamed.py`

**Interfaces:**
- Consumes: Task 5 PK decode; `pull --park` and `convert --park`: every JWE3 park bank, `UI`, every `building*` bank, `_sharedBuildings`, `Vehicles`, `Weapons`, `Scenery`, `Zipline` and `Global` (names unknown; every registered event's first take is decoded to `WORK\jwe3\<bank>\wav\`, cached per bank by source hash, run in the background: thousands of files, tens of minutes; a second run finishes in seconds).
- Produces: `candidates.unnamed_park(ctx, max_len_s) -> list[Candidate]`: one candidate per decoded park event, `unnamed:<bank>:<wem>`, `name = "unnamed <bank> <wem>"`, `length_s` measured, `file = _unnamed\<bank>\<wem>.ogg` under `WORK\craft`, sorted by length, never ticked, no grouping and no sound-alike ranking. Every non-animal row (Incubator and Feeders) gets them after the named candidates, with `max_len_s` from the sound's class (machine 12 s; `hum` loops take any length).
- Produces: the page stays usable with thousands of candidates: `GET /api/state` carries named candidates in full and, per sound, `unnamed_count` only; `GET /api/candidates?row=<row_id>&sound=<event_id>&offset=<n>&limit=<m>` (`limit` ≤ 100) returns that page of unnamed candidates by length, each with its `file`; the page shows them collapsed under "N unnamed park sounds, by length" with "show 50 more"; every `<audio>` has `preload="none"` and gets its `src` on the first play, so no audio loads until the lead plays it.
- Produces: craft folders for every Incubator candidate at `WORK\craft\<row_id>\<candidate.dir>\` (`dir_name`, Task 9; `repo:machines/_common/sounds/magnet/obj_magnet_hum_01.ogg` → `repo__machines___common__sounds__magnet__obj_magnet_hum_01`), except `unnamed:*`: over 1,000 candidates per sound would mean tens of thousands of encodes, so each park event has one shared `WORK\craft\_unnamed\<bank>\<wem>.ogg`, cleaned once as class machine, and `craft` skips it per row. A ticked unnamed candidate is fitted from that file.
- Produces: Incubator status proposals: every game-file pick `Placeholder`; repo picks on Ready and Shared rows keep `Ready to import` and `Shared, artist to confirm`; layered picks `Placeholder`. Events: `block.incubator.start`, `.lid_up`, `.lid_down`, `.egg_placed`, `.done`, `.fault_egg`, `.arm`, `.hum`, and the shared `block.machine.power_down`.

- [ ] **Step 1: Write the failing tests**

```python
def test_incubator_rows_have_candidates_and_no_clip():
    c = candidates_json(); rows = [r for r in c if r.startswith("incubator.")]; assert len(rows) == 8
    assert all(manifest.load()["rows_without_clip"][r] == "none" for r in rows)
    assert ticked(c["incubator.heater_hum"], "block.incubator.hum")[0].startswith("repo:machines/_common/sounds/magnet/")
    assert "pk:Amb_Incubation_Loop" in ids(c["incubator.heater_hum"]) and "layer:incubator_start" in ids(c["incubator.start"])
    assert ticked(c["incubator.fault_no_power"], "block.machine.power_down")[0] == "repo:machines/_common/sounds/obj_computerpowerdown.ogg"
def test_unnamed_park_candidates_are_sorted_and_unticked():
    u = [x for x in c["incubator.egg_placed"]["sounds"]["block.incubator.egg_placed"]["candidates"] if x["source"] == "unnamed"]
    assert len(u) > 1000 and u == sorted(u, key=lambda x: x["length_s"]) and not any(x["id"] in ticked_ids for x in u)
    assert {x["bank"] for x in u} >= {"UI", "buildingGeneric", "_sharedBuildings", "Vehicles", "Weapons", "Scenery", "Zipline", "Global"}
def test_unnamed_candidates_share_one_cleaned_file():
    u = [x for x in c["incubator.egg_placed"]["sounds"]["block.incubator.egg_placed"]["candidates"] if x["source"] == "unnamed"][0]
    assert u["file"].startswith("_unnamed") and (WORK / "craft" / u["file"]).exists() and not list((WORK / "craft/incubator.egg_placed").glob("unnamed__*"))
def test_page_stays_usable_with_a_large_candidate_list(server_with_synthetic_unnamed):   # 5,000 unnamed candidates planted on one sound
    t0 = time(); state = get("/api/state"); assert time() - t0 < 2 and len(json.dumps(state)) < 2_000_000
    assert state["rows"]["incubator.egg_placed"]["sounds"]["block.incubator.egg_placed"]["unnamed_count"] == 5000
    page = get("/api/candidates?row=incubator.egg_placed&sound=block.incubator.egg_placed&offset=4000&limit=50")
    assert len(page) == 50 and page == sorted(page, key=lambda x: x["length_s"]) and get_status("/api/candidates?...&limit=1000") == 400
    html = get_text("/"); assert 'preload="none"' in html and "show 50 more" in html
def test_hum_loops_cleanly(): y = vgm.load_mono48(first_ogg("incubator.heater_hum", "repo:...hum_01")); assert abs(y[-1] - y[0]) < 0.2 * np.abs(y).max()
def test_lid_is_trimmed_to_the_lift(): assert all(clean.measure(p).seconds <= 1.05 for p in oggs("incubator.lid_up_and_down"))
def test_statuses(): s = status_md(); assert s["incubator.start"] == "Placeholder" and s["incubator.arm_move"] == "Ready to import"
def test_hum_entry_has_block_variants():
    approve("incubator.heater_hum"); stage.run(paths); assert "pitch" in json.load(open(STAGED / "sounds.fragment.json"))["block.incubator.hum"]["sounds"][0]
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** `pull --park` and `convert --park` run in the background; the agent reports the bank count, event count and decode time. **Step 4: Run the tests** → pass. Run `map`, `craft --thing Incubator`, `fit --thing Incubator`; the page shows the 8 rows, audio only. The lead judges, in particular whether the layered start beats the raw start and whether any unnamed park sound fits a row. **Step 5: Commit**: `git add -A && git commit -m "feat: Incubator rows, all park banks, paged unnamed candidates"`.

### Task 24: Carnivore Feeder: a machine clip with its sound

**Files:**
- Modify: `TOOL\data\recipes.json` (`layer.feeder_hatch`: a door-slide part from `Staff_Gate_Open_Mat_Metal` and a platform-thud part joined as one two-part sound, each part at its own anchor; `layer.feeder_hatch_close`: a door-slide part from `Staff_Gate_Close_Mat_Metal` and the same thud, the same join at the reversed anchors, platform first; no source has a thud name, so the thud starts from `Staff_Gate_Close_Mat_Metal`, and because the lead judges it by ear on the page, its source is one value in this file; `machine.one_shot`: trim to end within 0.25 s of the part's motion stop), `candidates.py` and `craft.py` (`layer:feeder_hatch`, `layer:feeder_hatch_close`), `fit.py` (block subjects, multi-part plain sounds, reversed clips; removes Task 19's `not built yet` skip), `render.py` (reversed video), `stage.py` (block keyframes), `check.py`, `report.py` (the `hatch_close` line)
- Test: `TOOL\tests\test_feeder.py`

The lead has heard T. rex and the Incubator before this task starts (milestones 1 and 2). This task adds the slice's machine clip: a block model rendered with its sound.

**Interfaces:**
- Consumes: Task 8's `carnivore_feeder_one` row map section, Task 16's `clips.Block`, Task 17's `anchors.bone_moves`, Task 18's manifest with `kind`, Task 20's block render, Task 21's `extra_renders`, Task 23's unnamed park candidates.
- Produces: `FittedSound` gains `reverse: bool`. `fit_row("feeders.hatch_open_and_close")`: `hatch_open` is fitted to `open` with parts `doors` (anchor `Right_door:start`, index 0) and `platform` (anchor `Food_Panel:stop`, last); a plain candidate places its one file at `doors`; the layered candidate `layer:feeder_hatch` bakes both parts into one file with each part at its anchor (so a slider on `platform` rebuilds the file, as a blend part does). A layered `key` candidate writes one key, at the `doors` time; its file starts at that moment, with `platform` placed at its time minus the `doors` time, and the mux places the file once, at that key. `start` is `key`, so `sound_keyframes.json` gets `carnivore_feeder_one.open` keys `(time_s, "feeder_hatch_open", "block.feeder.hatch_open")`, and the manifest's `expected_keys` match. `hatch_close` is fitted to `open` with `reverse: true` and start `code`: parts `platform` and `doors` at their reversed anchors (Task 17), a plain candidate's hit lands at `doors`, the layered candidate `layer:feeder_hatch_close` bakes both, and the file carries its time as lead-in. It gets no sound key, and the manifest lists it under `open` with `reverse: true`. `food_drop` and `bait_release` are `none` in `rows_without_clip`. `idle` gets a manifest entry with no sounds.
- Produces: `render.reversed_video(mp4, end_s, out_mp4) -> Path`: cuts `[0, end_s]` of the forward silent video, reverses it with ffmpeg's `reverse` filter and passes the bitexact flags. `dust_trail.py` does not render a second time. `end_s` is `anchors.last_move_stop`. It also writes `<out>.json` with `length = end_s`, because `extra_renders` carries `json`. `render.for_row` also writes `<row_id>.close.mp4` and appends to `<row_id>.videos.json` (Task 21) the entry `{clip: "open", sound: "block.feeder.hatch_close", silent, built, json, ticks, offsets}`, which `/api/state` serves under `extra_renders`. Task 21's page shows that second video with its own sliders, and "Approve row" stays disabled until both built videos are current.
- Produces: a line in `report.md`: `carnivore_feeder_one` `hatch_close` needs F3.3's reverse, from 4.375 s of `open`, with the sound started at the same moment; until then, the lead judges it on the render only. The same line says that F3.3 must check whether `hatch_open`'s sound key fires when `open` plays in reverse; this was not checked here.
- Produces: `render.for_row("feeders.hatch_open_and_close")` renders `open` as a block subject (Task 20) with the feeder texture and one marker per part, and muxes the ticked candidate; the review page shows it like any clip row, with a slider per part.
- Produces: `stage` writes the feeder's files under `assets\projectnublar\sounds\block\feeder\`, `sounds.fragment.json` entries with pitch and volume variants (block rule), no subtitle (class machine), proposed status `Placeholder` for every game-file pick; `check` runs its sound-key comparison over block subjects as well as entities.
- Produces: the "When a clip changes" path for a block: Task 18's `compare` runs on `clips.Block("carnivore_feeder_one")` like any subject, with the geo model's hash in the fingerprint.

- [ ] **Step 1: Write the failing tests**

```python
def test_hatch_open_fits_to_the_open_clip():
    s = fit.fit_row("feeders.hatch_open_and_close", paths, ctx).sounds["block.feeder.hatch_open"]
    assert s.clip == "open" and s.start == "key" and set(s.parts) == {"doors"}                        # the default tick is a plain PK file: one part
    decisions.tick("feeders.hatch_open_and_close", "block.feeder.hatch_open", ["layer:feeder_hatch"])
    s = fit.fit_row("feeders.hatch_open_and_close", paths, ctx).sounds["block.feeder.hatch_open"]; assert set(s.parts) == {"doors", "platform"}
    assert s.parts["doors"].anchor.ref == "Right_door:start" and s.parts["platform"].anchor.ref == "Food_Panel:stop" and s.parts["platform"].time_s > s.parts["doors"].time_s
    keys = json.load(open(WORK / "sound_keyframes.json"))["carnivore_feeder_one"]["open"]
    assert len(keys) == 1 and keys[0]["event_id"] == "block.feeder.hatch_open" and abs(keys[0]["time_s"] - s.parts["doors"].time_s) < 0.001      # one key plays the one baked file
def test_hatch_close_fits_to_the_open_clip_in_reverse():
    s = fit.fit_row("feeders.hatch_open_and_close", paths, ctx).sounds["block.feeder.hatch_close"]
    assert s.clip == "open" and s.reverse and s.start == "code" and set(s.parts) == {"doors"}         # the default tick is a plain PK file: one part
    assert abs(s.parts["doors"].time_s - 3.625) < 0.03 and abs(audio.hit_time(vgm.load_mono48(s.parts["doors"].file)) - s.parts["doors"].time_s) < 0.02   # the lead-in survives clean.render(trim=False)
    decisions.tick("feeders.hatch_open_and_close", "block.feeder.hatch_close", ["layer:feeder_hatch_close"])
    s = fit.fit_row("feeders.hatch_open_and_close", paths, ctx).sounds["block.feeder.hatch_close"]; assert set(s.parts) == {"doors", "platform"}
    assert abs(s.parts["platform"].time_s - 1.375) < 0.03 and abs(s.parts["doors"].time_s - 3.625) < 0.03
    keys = json.load(open(WORK / "sound_keyframes.json"))["carnivore_feeder_one"]["open"]; assert all(k["event_id"] != "block.feeder.hatch_close" for k in keys)
    assert manifest.load().subjects["carnivore_feeder_one"]["clips"]["open"]["sounds"]["block.feeder.hatch_close"]["reverse"] is True
def test_other_feeder_sounds_have_no_clip():
    m = manifest.load(); assert all(m["rows_without_clip"].get(r) == "none" for r in ("feeders.food_drop", "feeders.bait_release"))
    assert m.subjects["carnivore_feeder_one"]["clips"]["idle"]["sounds"] == {}
def test_feeder_render_and_built_video(server):
    r = get("/api/state")["rows"]["feeders.hatch_open_and_close"]; assert r["render"]["built"].endswith(".mp4") and r["render"]["json"]
    assert json.load(open(WORK / r["render"]["json"]))["length"] == 9
    close = r["extra_renders"][0]; assert close["sound"] == "block.feeder.hatch_close" and close["built"].endswith(".close.mp4")
def test_reversed_video_plays_the_motion_backward(tmp_path):
    info = render.clip_video(clips.Block("carnivore_feeder_one"), "open", tmp_path, paths, size=(320, 180), ss=1, texture=FEEDER_TEXTURE)
    out = render.reversed_video(info.mp4, 4.375, tmp_path / "close.mp4"); assert abs(ffprobe_duration(out) - 4.375) < 0.1 and json.load(open(out.with_suffix(".json")))["length"] == 4.375
    assert pixel_difference(model_region(first_frame(out)), model_region(frame_at(info, 4.375))) < 0.01 and pixel_difference(model_region(last_frame(out)), model_region(frame_at(info, 0.0))) < 0.01
def test_a_new_close_clip_brings_hatch_close_back(tmp_repo_copy):
    approve_all(); add_clip(tmp_repo_copy, ("block", "carnivore_feeder_one"), "close"); link_row_map("carnivore_feeder_one", "feeder_hatch", "hatch_close", ["close"]); fit.run(paths_with(tmp_repo_copy), None)
    assert manifest.load().decision("close") == "clip arrived, no fit"
def test_platform_slider_rebuilds_the_layered_file():
    decisions.tick("feeders.hatch_open_and_close", "block.feeder.hatch_open", ["layer:feeder_hatch"]); a = fit.fit_row(...).sounds[HATCH].parts["platform"]; a_bytes = a.file.read_bytes()   # fit writes to one fixed path
    decisions.set_offset("feeders.hatch_open_and_close", HATCH, "platform", 100); b = fit.fit_row(...).sounds[HATCH].parts["platform"]
    assert abs(b.time_s - a.time_s - 0.100) < 0.002 and b.file.read_bytes() != a_bytes
def test_feeder_clip_change_comes_back(tmp_repo_copy):
    approve_all(); shift_keys(tmp_repo_copy, ("block", "carnivore_feeder_one"), "open", +0.5); fit.run(paths_with(tmp_repo_copy), None); stage.run(paths_with(tmp_repo_copy))
    m = manifest.load(); assert m.decision("open") == "clip changed, listen again" and decisions.row("feeders.hatch_open_and_close")["approved"] is False
    assert not {"block.feeder.hatch_open", "block.feeder.hatch_close"} & ids_in(STAGED / "sounds.fragment.json")
def test_feeder_model_change_comes_back(tmp_repo_copy):
    approve_all(); touch_model(tmp_repo_copy, ("block", "carnivore_feeder_one")); fit.run(paths_with(tmp_repo_copy), None)
    assert manifest.load().decision("open") == "clip changed, listen again"
def test_feeder_staging_rules():
    approve("feeders.hatch_open_and_close"); stage.run(paths); f = json.load(open(STAGED / "sounds.fragment.json"))
    e = f["block.feeder.hatch_open"]; assert "pitch" in e["sounds"][0] and "subtitle" not in e and status_md()["feeders.hatch_open_and_close"] == "Placeholder"
    assert check.run(paths) == 0
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** Run `fit --thing Feeders` in the background: it renders `open` (9 s at 1280×720, one pass since the clip holds). **Step 4: Run the tests** → pass. The lead judges the hatch sounds on the page: the open sound against the opening doors and the rising platform, and the close sound against the reversed video; the agent reports anchor times and the sync log's error statistics. **Step 5: Commit**: `git add -A && git commit -m "feat: Carnivore Feeder hatch sound on its clip"`.

### Task 25: `video` stage, first-slice part: planted control and saved evidence

**Files:**
- Create: `TOOL\nublar_sounds\video.py`
- Modify: `TOOL\nublar_sounds\review\server.py` (reads `evidence.json` into `/api/state`), `TOOL\nublar_sounds\review\page.html` (shows the evidence on the candidate), `TOOL\nublar_sounds\candidates.py` (`own` reads `evidence.json`), `TOOL\nublar_sounds\__main__.py` (registers `video`)
- Test: `TOOL\tests\test_video.py`

**Interfaces:**
- Produces: `video.features(x16k: np.ndarray) -> np.ndarray` and `video.match(templates: dict[name, np.ndarray], mix: np.ndarray, pitch_steps=(0.9..1.1)) -> list[Match(time_s, len_s, z, ncc, name)]` carrying `SF\tools\scripts\match.py` (log-mel 48 bands at 16 kHz, NFFT 1024, hop 320, normalised cross-correlation, best template wins where matches overlap, chance level from reversed templates); `video.templates_for(species, ctx) -> dict` = the species' JWE3 sounds with no graph link, unnamed events included, first take each, as 16 kHz mono.
- Produces: `video.plant_control(templates, carrier: np.ndarray, plan: list[(name, time_s, pitch, db)]) -> np.ndarray` (carry `plant.py`), and the test below as the Done-means control.
- Produces: `video.load_saved(SF\video_check) -> list[Evidence(clip_label, time_s, strength_z, name, file)]` from the 10 `*_matches.json` files, attached in `WORK\video\evidence.json` to candidates by sound name (for example `Trex_HeroRoar_Start` → "heard in trex abilities clip at 0:14"); the page shows it on the candidate. A match never ticks a pick and never names a sound. `candidates.own` reads `evidence.json`: a shared set matched on a clip of that species counts as the species' own (the design's third rule).

- [ ] **Step 1: Write the failing tests**

```python
def test_planted_sounds_are_found():
    carrier = pink_noise(240 s at -30 dBFS); plan = [("Trex_SadCall", 40.0, 1.00, 0), ("Trex_BreathWheeze", 100.0, 1.06, -6), ("Trex_SadCall", 190.0, 0.95, -6)]
    mix = video.plant_control(T, carrier, plan); found = video.match(T, mix)
    for name, t, _, _ in plan: assert any(m.name == name and abs(m.time_s - t) < 0.15 for m in found)
def test_saved_results_load(): ev = video.load_saved(SF / "video_check"); assert len({e.clip_label for e in ev}) == 10 and any(e.name == "Trex_HeroRoar_Start" for e in ev)
def test_evidence_reaches_the_page(server): assert get("/api/state")["evidence"]["jwe3:Trex_HeroRoar_Start"]
def test_video_evidence_makes_a_shared_set_own(tmp_work):     # the design's third own-sound rule
    assert candidates.own("jwe3:TheropodLarge_StandDie", species="Tyrannosaurus", row="death", ctx=ctx) == "unknown"
    write_json(WORK / "video/evidence.json", {"jwe3:TheropodLarge_StandDie": [{"clip_label": "kills", "time_s": 49.0, "strength_z": 9.0}]})
    assert candidates.own("jwe3:TheropodLarge_StandDie", species="Tyrannosaurus", row="death", ctx=ctx) == "own"
def test_no_download_happens(monkeypatch):
    calls = []; real = subprocess.Popen                       # subprocess.run goes through Popen, so this records both

    class Spy(real):
        def __init__(self, cmd, *a, **k): calls.append(cmd); super().__init__(cmd, *a, **k)

    monkeypatch.setattr(subprocess, "Popen", Spy)
    assert video.run(paths, args(species="Tyrannosaurus")) == 0 and not any("yt-dlp" in str(c) for c in calls)
```

- [ ] **Step 2: Run the tests to see them fail.** **Step 3: Implement.** **Step 4: Run the tests** → pass. **Step 5: Commit**: `git add -A && git commit -m "feat: video stage, planted control and saved evidence"`.

### Task 26: First-slice wrap: `all`, the report, the measurement

**Files:**
- Modify: `TOOL\nublar_sounds\__main__.py`, `report.py`
- Test: `TOOL\tests\test_all.py`

- [ ] **Step 1: Write the failing test**

```python
def test_all_runs_and_reports():
    assert run_cli(["all", "--thing", "Tyrannosaurus", "--thing", "Incubator", "--thing", "Feeders"]) == 0
    assert run_cli(["stage"]) == 0 and run_cli(["check"]) == 0                    # only `stage` writes report.md
    r = (STAGED / "report.md").read_text(); assert "161" in r and "Male and variant graphs" in r and "graphs_Tyrannosaurus.md" in r and "carnivore_feeder_one" in r
    assert rows_in(r, thing="Feeders") == 3 and rows_in(r, thing="Incubator") == 8 and rows_in(r, thing="Tyrannosaurus") == 18
```

- [ ] **Step 2: Run `PY -I -m nublar_sounds all --thing Tyrannosaurus --thing Incubator --thing Feeders` in the background**, then `stage` and `check`. Expected: exit 0; `report.md` lists every pending row (161), the T. rex, Incubator and Feeders rows as staged, gap or blocked, the sizes, and a "Male and variant graphs" section copied from `WORK\report\graphs_Tyrannosaurus.md` (names added per graph, coverage of 153).

- [ ] **Step 3: Run the whole test suite** → pass. Run the tests again with the game drives unplugged in mind: every real-data test is skipped cleanly when `JWE3_AUDIO` or `PK` is missing (`pytest -q -m "not real"` passes on fixtures alone).

- [ ] **Step 4: Report to the lead**: rows staged, gap and blocked by count; the measurement table; what remains for the lead's ear. No judgement of any sound.

- [ ] **Step 5: Commit**: `git add -A && git commit -m "feat: all command and first-slice report"`.

---

# F2.3 task: the mod's warning check (Java)

This task belongs to F2.3 and needs the plan docs changed (`docs/plan/engineering-backlog.md` F2.3 and `docs/plan/feature-specs.md` F2.3 gain the clip-manifest warning). **Do not edit `docs/plan` in this task**: report the two places to the lead with the proposed line. The lead decides when it runs; it can run after Task 18, which fixes the fingerprint vector.

### Task 27: `ClipSoundManifestTest` in the mod

**Read first, and apply to every line written or reviewed:** `REPO\docs\java-best-practices.md`. If the checkout has no such file, read it with `git show m0.p2/port-26.2:docs/java-best-practices.md`. The implementer and the reviewer both read it and apply it.

**Files:**
- Create: `REPO\common\src\test\java\net\dumbcode\projectnublar\client\ClipSoundManifestTest.java`, `REPO\common\src\test\java\net\dumbcode\projectnublar\client\ClipFingerprint.java` (test-side helper, pure), `REPO\common\src\test\resources\projectnublar\sound\fingerprint_vector.json` (byte copy of `TOOL\data\fingerprint_vector.json`), `REPO\common\src\test\resources\projectnublar\sound\clip_fit_manifest.json` (byte copy of `STAGED\clip_fit_manifest.json` when one exists; otherwise a manifest with one T. rex clip, written by the tool for this test)
- Modify: `REPO\common\build.gradle` (the `test` block only, if Step 3 needs it)
- Test: the test is the deliverable. It follows `DinosaurAnimationFilesTest` for reading the animation files from the test class path, over both `/assets/projectnublar/geckolib/animations/entity` and `/assets/projectnublar/geckolib/animations/block`, each with its geo model from the matching `models` folder; the manifest's `subjects` carry `kind` so the test knows which folder a subject belongs to. The manifest and the vector live in test resources only, never in the jar.

**Interfaces:**
- Produces: `ClipFingerprint.of(JsonObject clip, String modelSha256) -> String`: the same canonical form as `fingerprint.canonical` (drop `sound_effects`, add `model_sha256`, sorted keys, no spaces, strings unchanged, UTF-8, SHA-256 hex). Every number is `new BigDecimal(value).setScale(6, RoundingMode.HALF_EVEN).toPlainString()` on the parsed `double` (the exact binary value, HALF_EVEN, as the Python side), and negative zero is written `0.000000`. `modelSha256` is the SHA-256 hex of the `.geo.json` file's raw bytes. `ClipFingerprint.sha256Hex(byte[])`.
- Produces: `ClipSoundManifestTest` with two tests: `fingerprintMatchesTheSharedVector` (asserts equality with `expected`, a hard failure: the two implementations must agree), and `everyClipMatchesTheImportedManifest`, which never fails the build: for every entity and block animation file, each clip with a manifest entry is compared (fingerprint; the file's sound keys against `expected_keys`, time within 0.001 s and the same effect), and each manifest clip missing from the file and each file clip missing from the manifest is listed. Each problem logs one line through the mod's logger, `Constants.LOG.warn("clip sounds: {} '{}': {}", species, clip, problem)` with `problem` one of `changed since its sound fit`, `missing sound key at <t> '<effect>'`, `extra sound key at <t> '<effect>'`, `no manifest entry`, `clip gone` (no `System.err`, per `docs/java-best-practices.md`), and the test passes. When the manifest resource is absent the test logs one `Constants.LOG.warn("clip sounds: no manifest imported")` and passes.

- [ ] **Step 1: Write the vector test first** and run `gradlew :common:test --tests "*ClipSoundManifestTest*"` from `REPO` to see it fail (class not found).

- [ ] **Step 2: Implement `ClipFingerprint`** and run until the vector test passes. If it cannot be made to agree, the difference is in number formatting or key order: fix the Java side; never change the vector or the Python form here (report to the tool side instead).

- [ ] **Step 3: Write `everyClipMatchesTheImportedManifest`** with a fixture manifest whose one T. rex entry has a wrong fingerprint on purpose, run it, and confirm one `clip sounds:` warn line in the Gradle output and a green test. Check `common/build.gradle`'s `test` block shows standard streams (the test logger writes to the console); if it does not, add `testLogging.showStandardStreams = true` to that block only, and say so in the report.

- [ ] **Step 4: Replace the fixture with the real manifest copy**, run the whole `:common:test`, and confirm all green.

- [ ] **Step 5: Reviewer** (fresh Opus agent, having read `docs/java-best-practices.md`) reviews the two Java files and the test resources.

- [ ] **Step 6: Report, do not commit.** Report the files, the Gradle output line, and the two `docs/plan` places that need the F2.3 line. The lead decides the commit and the plan edit.

---

# Outline after the first slice

The lead judges the first slice first. Each step below becomes bite-sized tasks only after that. Order by reuse: species that need nothing new in the stages come first.

1. **Velociraptor and Dilophosaurus.** Row map entries per species (`Attack2`, `calling`, the clips each file has); the lighter death recipe (`death.light`: at most two voices, pitch 1.0 → 0.85, fade last 60 %, room 0.7 s at -20 dB, plain gain, no compressor, no falls; forms `x` death-into-call at -3 dB and `y` call-into-breath at -8 dB; `Velociraptor_Die`/`Velociraptor_SickCall`/`Velociraptor_BreathSick_Out` and `Dilophosaurus_Die`/`Dilophosaurus_SadCall`/`TheropodSmall_BreathSick_Out`; both forms ticked; repo files as candidates); PK `TheroSmall_Footstep_Walk` and `_Run` lighter (115 % / -6 dB, 105 % / -4 dB, both 6 dB quieter) ticked, plain as candidates; toe bones for the Velociraptor anchors; male and variant graph measurement per species.
2. **Gallimimus.** No `dead` clip: death row `clip: none`, "clip arrived, no fit" when one lands; `Gallimimus_DeathRoll` and `DieScream_Selects` engine names; steps `TheroSmall` lighter 110 % / -6 dB ticked with walk times 40 ms earlier (0.862, 1.830 s) and `HerbSmall_Footstep` as a candidate, both played against the clip for the lead; `TheropodHerbivore_Shared` group bank; the D345 clip fixes change the fingerprint, so the rows come back by design.
3. **Triceratops and Brachiosaurus.** Blends `Tc` and `Bc` from `recipes.json`; `Bc` with fewer extra sighs to end under 10 s, the lead hears the shorter version; `Sprint ` clip name with its trailing space; four feet with front and rear FootPlant events, Cerapod and Sauropod pools (filter 0.9 s and 1.5 s); capped landings flagged as start values for the in-game check; the Brachiosaurus clips' 4 existing keys replaced by the keyframe file; 23 and 29 clips to render in the background.
4. **Eggs and hatching, generators, other machines.** Layered egg crack (two `Nest_Fracture` 0.14 s apart at 0 and -4 dB plus `Nest_Hatch` at 0.09 s and -9 dB, room 0.35 s, compressor) and generator start (crossfade 0.5 s into `DieselGenerator_Loop_Default`), reproducing the approved samples for the lead's ear; the remaining machine rows by the handoff's named PK candidates (the remaining feeder rows among them when their clips or models land); fence rows stay locked (Placeholder, no layering) behind one `config.toml` key, `unlock_layering = false` (the design's "Locked behind a setting").
5. **Unnamed sounds, grouping and ranking.** The park banks are already pulled and on the page by length (Task 23). This step adds grouping, so the lead hears one per group with the group openable, and sound-alike ranking of unnamed JWE3 sounds against named ones with the video matcher (untested for this use; measured first on the T. rex bank).
6. **Video stage, downloads.** The candidate list per species, written in this step (the first slice writes none), to the lead; each approved clip downloaded audio-only with `yt-dlp` into a new empty folder under `WORK\video\`, read only with `-I`, deleted after matching; JWE2 and JWE1 clips allowed; Species Viewer recordings supplied by the lead; the three extras (sandbox recordings, Demucs, the baby T. rex clip) each still need approval when reached.
7. **Hatchling rows and shared sets across species.** `Juv` names from the juvenile graphs per species; `entity.carnivore.eat` and other shared ids with their index; a Placeholder for every species-swap stand-in.
8. **Re-runs after the lead's playtest tuning.** Loudness targets changed in `loudness.json` and re-rendered; the art list re-read for new rows; D91's answer applied to the 7 T. rex rows.
