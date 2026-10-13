# Beta and Release 1 project plan

Mirror of the shared page tab "Beta and Release 1 project plan" (the master copy). Updated 2026-10-13.

The Beta is a survival Jurassic Park mod for Minecraft 26.2 on Fabric and NeoForge. The player finds fossils, clones and edits six dinosaur species, and keeps them in a powered, fenced, fed park. No milestone is started. The lead committed the herd work on `kash's-slop` on 2026-10-11, so nothing blocks M0. Art is the largest gap: 432 of the 996 Beta art assets are missing or are placeholders, and 270 more need importing, exporting, converting or checking in game.

## Roadmap

Order of work: M0 Port and foundation, then M1 Fossil to egg, M2 Dinosaurs, M3 Park, M4 World and vehicles, then Beta hardening and release, then Release 1. A gate follows each step: the port must build and run on both loaders, and each milestone ends with a playtest.

Work runs top to bottom. Each milestone finishes all of its features together, with their screens, animations, sounds, recipes and art. Then a playtest decides whether the next milestone starts.

## Milestones

| Milestone | When it ends, the player can | Features | Art: done · to check · to import or export · missing | Status |
| --- | --- | --- | --- | --- |
| M0 Port and foundation | Play the current mod on 26.2, on both loaders | Branch merge, CI, port to 26.2, config and species data | Not applicable | Not started |
| M1 Fossil to egg | Find fossils and amber, extract and edit DNA, print and incubate an egg, and hatch a baby | Fossils and amber, DNA from animals, Fossil Processor, Sequencer (sequencing, editing, synthesis), Egg Printer, Incubator, eggs and hatching, machine building, lab power, lab recipes | 50 · 12 · 31 · 92 | Not started |
| M2 Dinosaurs | Raise six species that eat, drink, rest, socialise, breed and react to the player | Behaviour model and contentment, species core, appearance, animations and sounds and effects, needs, herds and packs, the player, special genes | 192 · 11 · 186 · 277 | Not started |
| M3 Park | Hold dinosaurs in a powered, fenced paddock with feeders | Fences, power grid, feeders, park recipes | 9 · 0 · 22 · 33 | Not started |
| M4 World and vehicles | Find digsites and traders, build and drive the Jeep, run Ford Explorer tours | Digsites and maps, vehicles, guns (blocked) | 0 · 0 · 8 · 30 | Not started |
| Beta hardening | Play a fast, stable Beta on both loaders | Performance and cleanup, balance playtest, release | Not applicable | Not started |
| Release 1 | Plant a park, and clone four new species | Plants and wood sets, four species, their fossils | Not counted yet | Not started |

"To check" means the asset exists in the mod but nobody has checked it in game. The 996 include 43 shared assets: skin layers that both genders may share, and generic machine sounds. The artist confirms them. Not counted: 1 asset that waits on a lead decision, and the guns, which are blocked. The Art list recounted these totals after the last open items of 2026-10-13 (Z-8).

## Waiting on you

The full lists are in the Feature specs and the Decisions register. Nothing blocks M0. You answered the last open items on 2026-10-13 (batch Z-8), so no draft, reading or flag waits for you.

**Decisions that you parked**

1. Guns: which kinds and which models ship (D08). The guns stay blocked until you decide.
2. Animation speed that follows body size, and look-around clips on the second controller (D14). The plan does not build them.

**For the artist**

3. Which asset-repo folder holds the real T. rex sounds (D91)? Can both genders share the eye, teeth, nostril and claw layers (Art list question 4)?
4. Art list questions 9, 12, 13, 19, 20 and 21:
    - the egg model findings
    - the upgrade texture swaps
    - the reach of the Incubator arm
    - the pylon head on a fence pole
    - the parts of each vehicle that take the dye tint
    - the vehicle assembly stages: the REPO models have no chassis, engine or Tour Computer group.
5. New art from your answers is on the Art list. The raptor crouch-then-leap clip waits for later.
    - bloody claws for the T. rex and the Dilophosaurus, the generic pelvis set, the Wrench sprite
    - the Egg Printer lamp glow, a close clip for each feeder, the fence power-up and power-down sounds
    - 3 mood faces and 7 need icons, the lever switch, the Paleo-Illager skin, and the digsite and lab house structures from the team builder
    - the vehicle part sprites, the assembly-stage parts and the stop marker lamp.

## The other pages

- [Feature specs](plan/feature-specs.md): each feature in player terms. How it plays, screens, animations, sounds, numbers, sources and open questions.
- [Art list](plan/art-list.md): every art asset by species, machine and block, with its status. For artists.
- [Engineering backlog](plan/engineering-backlog.md): work packages per milestone, with scope and acceptance tests. For developers and agents. It ends with the recipe table, the conflicts and the Decisions register.
- [Codebase analysis](codebase-analysis.md): the codebase analysis and the lead's design decisions (section 11).

## Rules this plan follows

- **Cloning parity.** The cloning system works 1:1 like the 2022 Cloning Guide video: flow, timings and screen contents. The newer spec (`_Cloning Process.docx`) and the lead's decisions win where they differ. The machines look like the current models, not the 2022 ones.
- **Fence parity.** The fence is 1:1 with the original 1.12.2 fence.
- **No stand-ins.** An asset counts only when it is the right one. A T. rex roar does not count for any other species. A copied file counts as missing.
- **Sources over code.** Requirements come from the design docs, the videos, the boards and the lead's decisions. Today's code shows only the current state.
