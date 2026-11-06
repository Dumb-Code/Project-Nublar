**Suggested session title:** Sound pipeline Task 8: rows-to-names and rows-to-clips data

# Sound pipeline build: paused after Task 7

## The lead's rule for all work from here

> "ensure that the work you do does not attempt to conduct rework"
>
> "I'm really trying to make sure that you only act upon and execute work that addresses an unfulfilled need, requirement, issue, and gap"

What this means in practice:

- Before each step, fix or dispatch, name the unmet need and check that it is still open (the ledger, git log, the files, a measurement). If the need is already met, say so and skip the step.
- Put this rule in every subagent prompt. It is now in the plan's Global Constraints and in `implementer-common.md`.
- A reviewer's finding counts only if it names a real unmet requirement or real breakage. Polish and a second look with no new question do not count.
- A deferred minor below stays deferred until a later task needs it.

## Do not redo

| Done | Where it is recorded |
|---|---|
| Tasks 1 to 7: built, reviewed, complete. 224 tests pass (checked at pause) | `git log` in `TOOL`; ledger below |
| Pre-flight conflict scan and its 83 + 11 plan fixes | `preflight.md`, `preflight-fixes.md`, `preflight-rereview.md`. Do not scan again |
| The lead's page decisions | Applied to the plan (see "Decisions") |
| T. rex and shared bank decode; 6 species' name files; motion graphs; PK names | `SF\work` (623 MB), cached. Do not delete |
| Ear picks: footsteps, nudges, death rows, layering | The design. Never ask again |
| Design, approved and committed at `37e9542` | The design file |

The older handoff `2026-11-05-sound-pipeline-handoff.md` also lists what not to redo from before the design.

## Work done

| Task | Result | Last commit |
|---|---|---|
| 1 Repo, config, cache, CLI | Cache keys are URL-encoded after a review finding | `16ea987` |
| 2 Art list parser | 161 pending rows (98 / 10 / 45 / 8) | `0bf58c6` |
| 3 FNV and registry | 15,834 events; `TRex_events` 153; 18 guesses per species | `2cc273c` |
| 4 JWE3 banks | T. rex decodes to 2,072 wavs, 0 failures. A missing bank file is now an error | `b78df11` |
| 5 PK names and decode | 9,062 names over 31 banks; 90 of 90 subsong checks agree | `5070700` |
| 6 Motion graphs | 12 graphs read. No male or variant archive holds one | `f0126b8` |
| 7 Names per species | Every registered event of a species' banks has one key (122 added, 49 in own banks) | `70b6603` |

Today the lead also settled the plan page's 3 decisions, and the design's small errors are fixed.

## Work left

1. **Tasks 8 to 13**, then **stop for the lead to listen** (milestone 1, T. rex picks).
2. **Tasks 14 to 21**, then **stop for the lead to listen** (milestone 2, T. rex on clips).
3. **Tasks 22 to 26**: staging, Incubator and park banks, Carnivore Feeder, video evidence, wrap.
4. **Task 27**: only after the lead judges the first slice. No commit in `REPO` without the lead's word.
5. Final whole-branch review, on the most capable model, with the deferred minors below.
6. The plan's 8-step outline for the other species and machines.

## How to resume

Paths: `TOOL` = `Documents\Minecraft Modding\Sound Files\tools\pipeline` (its own local git repo, never pushed). `W` = `Project-Nublar\.superpowers\sdd\2026-11-05-sound-pipeline\` (git-ignored; `git clean -fdx` would destroy it).

1. Use `superpowers:subagent-driven-development`. Read `W\progress.md` first: a task with a `complete` line is done. Resume at **Task 8, base `70b6603`**.
2. Brief: from `Project-Nublar`, run `bash <skill>/scripts/task-brief docs/superpowers/plans/2026-11-05-sound-pipeline.md 8`. Refresh `W\global-constraints.md` first, because the no-rework line is new: extract the section from `## Global Constraints` to `## Questions for the lead`.
3. Implementer: Opus. The prompt gives one line of context, the brief path, `W\implementer-common.md`, the "Carry" facts below for that task, and the report path `W\task-N-report.md`.
4. Review package: **run from `TOOL`** and pass an output file: `bash <skill>/scripts/review-package <absolute plan path> BASE HEAD "W\review-tN.diff"`. Without the output file it writes into `TOOL`.
5. Reviewer and re-reviewer: fresh Opus, never Sonnet. Use `task-reviewer-prompt.md` and `re-review-prompt.md` from the skill folder.
6. Append each result to `W\progress.md`. Start the next implementer alongside a re-review only, never alongside a first review, because a fix and a new task would share one git repo.

## Facts each coming task needs

Implementers do not see the ledger. Put these in the dispatch.

**Task 8**
- `data\species.json`: `pk_prefix` is a list on every species. `pk_unsure` is on the T. rex row only, so read it with `.get`.
- `jwe3_variants` for 5 species was derived, and none holds a graph.

**Task 9**
- `rows.json` is `[Row]` with `counts` by status (plan line 195 area).
- `jwe3` is keyed per event hash: 11 groups of spellings merged. Look up by hash or normalize, or `n.jwe3[link.event]` raises KeyError.
- `graphs` is non-empty for 24 names no clip holds. The own-sound rule says "links to a clip", so use `Link` lists there.
- Form entries (`X_oc`) carry the base's `source` and `graphs`, but no `Link.event` equals them, so a link count needs the base name. Links repeat (T. rex 4,705 against 1,714 unique), so `log1p` counts repeats.
- `jwe3` holds names in banks outside the allowed sources (`Albertosaurus`). **Filter to allowed banks**, because `BankStore.bank` would extract them.
- `pk_own`, `pk_unsure` and `pk_standin[group]` are dicts of name to bank files.
- Keep links in `motiongraph.SOUND_KINDS` only.
- Ignore the wrong note about empty `takes`: `X` and `X_oc` are held as `X_start` and `X_oc_start`, and the walk finds them.

**Tasks 9, 12, 23**
- 2,758 PK names sit in more than one bank: candidates carry bank + index. `UI_Animals` repeats 2 names inside one bank. Use `fsb.bank_files(pk, copies)`.
- Handle both `NameMismatch` and `DecodeError`. They are separate classes, and a past-the-end index raises `DecodeError`.
- Open one fixed bank set per species before any `takes()` call. `takes()` depends on which banks are open (Review Focus 5).
- Cache keys are URL-encoded. Keys that differ only by case share one file on NTFS, so check the plan's keys when a stage picks them.

**Task 18 or 19**
- `paths_with` and `tmp_repo_copy` have no owner in the plan. The first task that uses them creates `tests\helpers.py`.

**Task 26**
- `report.md` states that male and variant graphs add 0 names.

**Species steps later**
- `Death (2)` rows slug to `brachiosaurus.death_2`, not `brachiosaurus.death`.
- `guns_blocked_list_only.shot_reload_dart_taser` carries its heading's words, so its id changes if the heading does.

## For the lead to decide or know

1. **Commit word:** the plan, plan page, design fixes and this handoff are uncommitted in `REPO`. The plan is untracked, so it has no history until committed.
2. **T. rex wet breath:** `Trex_Wet_Breathing_start` reaches 13 takes, and 5 are only in `TheropodSmall_Shared`. They are left out, so it has 8. Say if you want them.
3. **Renders and in-game motion:** walk clips have keys with no `lerp_mode`: T. rex `move.walk` 1,070 of 2,236, `run` 537 of 1,329, Brachiosaurus `move.walk` all 360. The renders you nudged against smooth those keys with Catmull-Rom. That stays, so your nudges keep their meaning. The in-game check in F2.3 stays final.
4. **Hatch close:** the art list says code plays `open` in reverse, but that is F3.3 work. Today the hatch snaps shut, so you judge the close sound on the render until F3.3. F3.3 must start its reverse at 4.375 s and start the sound with it.
5. **Design text is out of date:** lines 62 and 88 expect male and variant graphs, and none exist. The table says Velociraptor 134 and the measurement gives 135 with engine guesses (cause not found). Not edited.
6. **Proposed global rule, waiting for a yes:** "never ask to pick a sound from its name" through `claude-md-management:revise-claude-md`. The no-rework rule could go the same way.
7. **Listening still owed:** Gallimimus walk 40 ms shift against the feet (0.862, 1.830 s); Triceratops and Brachiosaurus timing in game (F2.3).

## Decisions

The lead, on the plan page, 2026-11-05: stop after Task 13 and Task 21. The hatch close sound fits the `open` clip in reverse. The Java warning test comes after the lead judges the first slice.

## Rulings made without asking

The lead can overrule any of these. All are in `W\progress.md`.

| Ruling | Cost if wrong |
|---|---|
| The reverse starts at `open`'s last motion (4.375 s), not at 9 s | F3.3 must match, or the slider moves it |
| Accepted the scan's and re-review's fixes, each checked against the plan and real files | Rework at a task review |
| A candidate shows its clip fit once ticked; a tick refits (spec reads slightly wider) | The lead ticks to hear a candidate on the clip |
| `hurt` gets `tail: clip_end` though `flinch` holds | A shorter hurt; overrule by ear |
| Feeder anchors and renders are linear; dinosaur landings and renders stay Catmull-Rom | See item 3 above |
| `hatch_open`'s key in a reversed clip: F3.3 must check it | One report line |
| Video candidate list leaves this slice, stays in outline step 6 | Written later |
| Status is a whole-cell match in the art list | None today |
| `test_only_sound_tables_count` uses `startswith`, because the plan's form could not fail | None |
| 18 guesses counted by hash, not 30 by string | A number differs from a literal count |
| `pull` extracts T. rex into `WORK`; `convert` decodes all events in pulled banks | Disk only (388 MB) |
| T. rex keeps its own bank list | See item 2 above |
| Small tested extras kept: `Registry.named_events`, `registered_forms`, `.archives.json`, `.ovl` hash cache | None |
| Every registered event gets one key; `X_oc` under its real name | More candidates to hear: of 122 forms, 117 reach the same sounds as their base, 2 differ (`Trex_SniffLarge_oc`, `Gallimimus_Juv_Cluck01_oc`), and 3 were not measured because their banks are outside the allowed sources |
| `_sound_names` moved to `motiongraph.py` | None |
| `paths_with` and `tmp_repo_copy` go in a new `tests\helpers.py` | One small file |

## Lessons learned

- **Check a plan against real files before building.** The scan found 83 problems the earlier review missed, many only visible by measuring: the wrong sampler moves the feeder's anchors by 0.8 s to 2.8 s, and `git commit -am` would have dropped every new file.
- **Plan numbers are claims.** T. rex has 2 graph files, not 7. Clip links alone give 125, not 130. The real test decides, and the implementer reports the gap.
- **Task reviewers found real bugs a passing suite did not:** cache keys on NTFS, an empty bank cached as success, 49 events the lead would never have heard.
- **Tests that cannot fail arrive from the plan too.** Scans and reviews caught several. Keep looking for them, and prove a fix by making the test fail.
- **A count that adds up can hide a gap.** `len(unnamed) == total - registered_named` passed and pinned the bug.
- **The ear beats measurement.** Foot timing "within a frame" was wrong by ear. A slider page settled it. Plain words and side-by-side options work; code names do not.

## Mistakes to avoid

- I wrote "the code plays `open` in reverse" on a decision page from the art list, without reading the code. Read the code before you state a fact to the lead, and say "planned" when it is planned.
- Do not start a task beside a first review. Fixes and new work share one repo.
- Do not call `set_session_title` without a `session_id`. It failed here, so this session's title is unchanged.
- Never ask the lead to pick a sound from its name. Build a listening page.
- After 2 trade-off rounds on art or animation, state the limit and recommend a stop.

## Standing limits

- No push. No deleted branches. Commits go to `TOOL` only; nothing in `REPO` without the lead's word.
- `REPO` is read only for the tool. Do not edit `docs/plan/*`, the shared tabs or the mod tree.
- No YouTube download without per-clip approval, no audio-tagging model, no `Main.ovl`, no JWE3 Audio Kit, and no other games. Audio stays on this PC and is never published.
- Never run or import a script in `TOOL\spike`. Some delete files at module level.
- Agents measure. Only the lead judges a sound.

## Stuff to fix, only if a later task needs it

Deferred minors, also for the final review:

- **Task 1:** `stale`'s KeyError and TypeError branches and the `real` skip have no committed test. Case-only key collisions (above).
- **Task 2:** an unknown status in a Sounds table drops silently (`artlist.py:7-10`). The section reset is not pinned. One test overlaps its stricter sibling.
- **Task 3:** `known_bank_names` raises a raw FileNotFoundError if `JWE3 Sounds` is missing. A hit in an unnamed bank stores the hash as text.
- **Task 4:** `convert`'s cache does not check that wavs exist. `pull` and `convert` do not call `paths.require`. The rank comment at `wwise.py:25-26` is false for layers in random containers (no event changes). `cobra.extract` accepts a bad non-empty folder, so `convert` then fails loudly on every run. `ExtractError` docstring.
- **Task 5:** the audio-skip test proves less than its name says. A truncated bank gives a poor error. `write_pk_names` has no cache (0.5 s).
- **Task 6:** `pull.py:145` swallows a failed clear. Stale graphs stay in `WORK\mg` (Task 7 reads the record, so names are safe). Copies are not checked against the install. `Link` is unhashable. No direct test of the fifth link kind.
- **Task 7:** `_write` repeats `pull._write_text`. `names.py` keeps the `min()` spelling where the registry keeps the later one. A record with all-empty `graphs` gives a silent guess-only result. An unregistered `X` with a registered `X_oc` in one graph is unguarded (0 cases; the real test would fail).
