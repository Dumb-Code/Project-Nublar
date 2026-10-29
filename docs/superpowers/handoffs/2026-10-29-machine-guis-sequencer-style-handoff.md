Suggested session title: **Machine GUIs: mock up the Processor, Egg Printer, Incubator and Generator screens in the Sequencer style**

# Handoff: apply the approved Sequencer screen style to the other machine screens (2026-10-29)

The lead approved a new look for the three Sequencer tabs on 2026-10-29 ("Feels right"). This handoff carries that look to the other lab machine screens. The work is design only: mock-ups, the lead's review, then plan text. No Java code changes.

## Where things are

- **Branch:** `m0.p2/port-26.2`. The Sequencer design is in commit `b558595` (local, not pushed). The shared plan page is synced to `b558595`.
- **The rules (source of truth):** `docs/plan/feature-specs.md`, F1.4 → "Screen style (all three tabs)" and "Screen motion (all three tabs)". F1.5 and F1.6 show how a tab applies them. F1.10 "Widget look" holds the machine-wide widget rules.
- **The lead's answers:** Decisions register D300 to D305 in `docs/plan/engineering-backlog.md`, with the lead's own words. D304 and D305 are still open.
- **The approved mock-up:** `docs/mockups/2026-10-29-sequencer-screens-approved.html`. It draws all five Sequencer views on the real textures and holds every reusable helper (see "Reuse the mock-up code").
- **Memory:** `gui-redesign-style` holds the lead's style rules and the history of what was rejected.

## The approved style in short

Read the F1.4 sections for the full rules. The points that decide a new screen:

- **Keep the current art.** The machine's own GUI texture, its frame and its parts stay. New parts sit on top of that art. A full Material Design 3 restyle was rejected: it "strips too much of the original design" and leans "too hard into the pixel art style".
- **Font:** the plain Minecraft font, mixed case, normal shadow. No styled font.
- **Shapes:** a button takes the frame's 45° tab trapezoid (fill `#163758`, light edge `#5398AC`). Chips, messages and small controls have 45° cut corners. Nothing has round or pill ends ("clash with the existing tab designs").
- **One glow:** only the thing the player must use next glows, as a soft slow pulse. Progress art may glow while it fills. Never dashed or moving borders ("need to go away").
- **Guides built in:** each empty slot shows a faint icon of its item. Only the slot needed next has a label. A status chip names the machine state. One short event message at a time.
- **Colours:** cyan progress and chosen item, amber needed input or low, green ready or done, red failure or loss, purple an isolated gene.
- **Lists that grow scroll,** with a thin bar, the mouse wheel and a bar drag.
- **Motion (M3):** markers slide with a slight spring, tab changes slide and cross-fade in 0.24 s, values move in about 0.15 s, hover highlight and click ripple.
- **Motion never delays content.** A click shows its result in the same frame. Text never fades. A ticking label ("Working 42%") changes in place, or it reads as flicker.

## Scope: the screens to do

Do one machine at a time. Get the lead's approval on each before the next. Recommended order:

| Order | Screen | Spec | Art in `textures/gui/` | Video | Notes |
| --- | --- | --- | --- | --- | --- |
| 1 | Fossil Processor | F1.3 "Screen" | `processor.png` (256 × 256) | 1:07 to 2:05 | Filter, bucket, water gauge, fossil slot, down arrow, 3 × 3 by-product grid, test tube slot. Tooltips "3m 53s (3%)", "500mB / 2000mB" |
| 2 | Egg Printer | F1.7 "Screen" | `egg_printer.png` (176 × 230), `bonemeal_bar.png`, `egg.png` | 11:35 to 12:18 | Bone meal gauge and slot, syringe slot, egg picture with progress lines, egg and empty-syringe outputs. A failed print gives a cracked egg |
| 3 | Incubator | F1.8 "Screen" | `incubator.png` (334 × 222) | 12:50 to 13:32 | Bed pane with egg markers at their real spots, drag to place (green or red), plant matter icon, count and bar, bed fill stages, egg tooltip |
| 4 | Basic Generator | F1.11 "Screen, look and sounds" | `coal_generator.png` (256 × 256) | none | Fuel slot, flame, energy bar. The layout is not set: a mock-up here is a proposal for the lead |
| 5 | Shared machine parts | F1.10 "Screen" | none yet | 1:07, 2:53, 11:38, 12:50 | Linked-machine tabs, pop-up slot dialog, warning tab, status bar, hover box. Do these when two machines exist in the new style |

Later milestones, not now: the Large Generator screen (F3.2), the three feeder screens (F3.3, "lab GUI style", `processor.png`). The digsite trade screen (F4.1) stays the vanilla trade screen.

Open the texture first and note what it already draws (slots, gauges, arrows). The mock-up draws over it and must not double its parts. Each new part must use art that exists, or go to the Art list as a new row.

## How the Sequencer work ran (repeat it)

1. **Read** the machine's spec "Screen" section, its video timestamps and its GUI texture. Measure the texture's own shapes with PIL (`python -I`), as the Sequencer tab trapezoids were measured: rows 5 to 14, 45° sides, top wider.
2. **Mock up** with `show_widget` on the real textures. Load them by URL from the pinned commit, never as hand-copied base64 (copies came out corrupt):
   `https://cdn.jsdelivr.net/gh/Dumb-Code/Project-Nublar@0838bd4a9ed4b5b95929a4c2b747b11c3523dea4/common/src/main/resources/assets/projectnublar/textures/gui/<file>.png`
   Animate the steps of the machine (no input, input in, running, done) as the Sequencer mock-up does, with a demo cursor.
3. **Check in the browser pane before showing it.** Serve the file with a local server through `.claude/launch.json`, for example `python -I -m http.server 8766 --bind 127.0.0.1 --directory <folder>`, then open `http://localhost:8766/<file>`. Freeze a state with `javascript_tool` (`paused=true; pt=<seconds>`) and zoom in to check. Remove the launch entry when done.
4. **Show it** inline and ask one short question. Change one thing per round.
5. **After approval, write it into the plan** the same way as `b558595`:
   - Feature specs: the machine's "Screen" section. Point to the F1.4 rules ("Same frame rules, screen style and screen motion as F1.4"), then list only what this screen adds. Add one line to its "Changed since last review".
   - Engineering backlog: the machine's screen tasks and acceptance checks, including a same-frame check and a no-flicker check. Its changelog line.
   - Decisions register: new rows from D306, with the lead's own words, under a new subsection.
   - Art list: new rows (Missing unless the art exists), and update the section's "To make" count, the Summary table, "Counted rows", "Work left" and the roadmap's M1 column and totals in `docs/project-plan.md`.
   - Run the STE linter on the added text: `python -I ~/.claude/skills/asd-ste100/scripts/ste-lint.py <file> < /dev/null`.
   - Commit, then sync the same edits into the shared plan tabs (rule `sync-shared-plan-doc`). Record the synced commit in the M0.P2 ledger (`.superpowers/sdd/2026-10-25-m0.p2-port-to-26.2/progress.md`).

## Reuse the mock-up code

`docs/mockups/2026-10-29-sequencer-screens-approved.html` is one HTML fragment. Copy its helpers into the new mock-up:

- `FG`, `tw`, `tx`: a Minecraft-like bitmap font with the shadow. Text runs about 6 px a character at GUI scale, wider than a browser font, so lay out with `tw`. The game draws the real font.
- `spr`, `ctw`: springs for values and colours. `EMPH`, `DEC`, `ACC`: the M3 easing curves.
- `trp` (tab trapezoid), `chp`, `cham`, `chs` (cut corners), `rf`, `gf` (fills and three-colour gradients).
- `glowK`, `glowP`: the soft pulse glow. `chip`, `toast`, `tip`: status chip, event message, tooltip.
- `slist`: a scrolling list with a bar, wheel and drag. `hov`, `hit`, `ripples`: hover highlight, click areas and ripples.
- `frame`, `bg`, `chrome`, `layer`, `setMode`: the page frame, the tab bar and the page transitions.

## Mistakes already made (avoid them)

- The Write tool turns `·` escapes into raw characters. Python's `http.server` sends no charset, so the page shows "???". Escape non-ASCII with a short script before you serve the file.
- A text fade that restarts on each new value flickers. Text never fades.
- A scripted pause between a click and its result reads as lag. Results show in the click frame.
- Pill buttons, dashed or moving borders, and a full flat M3 restyle were rejected.
- New parts covered texture lines and labels: on the Sequencer the frame has lines at y 18, 33 and 183 and a "PN" logo at the bottom right. Check the machine texture for the same kind of clash.
- The mock-up said "disc" for the Sequencer drive. Use the item names from the spec.

## Open items to keep in mind

- D304 (failed-egg count on a drive, what unlocks a skin variant) and D305 (glow as sprites, no shader) wait for the lead.
- Pop-ups or an inventory drawer for slots: still open. The specs keep the pop-ups. Raise it with the shared F1.10 parts, not before.
- The selected gene bar stays red in the spec. The last Sequencer mock-up drew a white outline.

## Rules from memory that apply

- Design talk changes docs only. No Java or game-data edits (`no-code-changes-in-design-discussion`).
- Short replies. One short question at a time, with visuals in `show_widget` (`short-replies-no-walls`, `askuserquestion-show-visuals`).
- Design only with assets that exist, and check the asset folders first (`design-for-existing-assets-only`, `implement-assets-that-exist`).
- Commit locally. Push only when the lead asks (`no-push-every-commit`).
