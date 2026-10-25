# Performance baseline

Measure every fps gate and tick-time gate of the [engineering backlog](plan/engineering-backlog.md) on the setup on this page.

## Reference machine

| Item | Value |
| --- | --- |
| CPU | not set |
| GPU | not set |
| RAM | not set |
| Java version and flags | not set |

Register row D284 in the [decisions register](plan/engineering-backlog.md#decisions-register) holds these values open. The lead gives them.

**Dev default, not the reference.** The dev runs use Java 17 (`java_version=17` in `gradle.properties`). `gradle.properties` also sets `org.gradle.jvmargs=-Xmx3G`. This flag sets the heap of the Gradle process. The run tasks in `forge/build.gradle` and `fabric/build.gradle` add no `-Xmx` flag.

## Fixed client settings

| Setting | Value |
| --- | --- |
| Render distance | 12 chunks |
| Graphics | Fancy |
| Shaders | None |
| Optimization mods | None |
| Fullscreen | Off |
| V-Sync | Off |
| Max framerate | Unlimited |

## Test world

| Item | Value |
| --- | --- |
| Game mode | Creative |
| World type | Superflat |
| Structures | The GameTest structures `herd_arena` and `herd_track`, from `common/src/main/resources/data/projectnublar/structures/` |
| Time | Locked at day 1000: `/time set 1000`, then `/gamerule doDaylightCycle false` |
| Weather | Clear: `/weather clear`, then `/gamerule doWeatherCycle false` |

To place a structure, use `/place template projectnublar:herd_arena` or `/place template projectnublar:herd_track`.

## How a gate is measured

| Value | Reading | Result |
| --- | --- | --- |
| fps | The fps value on the F3 screen. Take three readings, one minute apart. | The median of the three readings |
| Server tick | Run `/debug start`. After 60 seconds, run `/debug stop`. | The average over the 60 seconds |

Note: `/debug stop` gives the average in ticks per second, not in ms. In single player, the F3 line `Integrated server @ … ms` gives the tick time in ms.

## Gates that cite this file

The line numbers are for `docs/plan/engineering-backlog.md` on 2026-10-25. Edits to the backlog can move them.

**Paddock tick, 25 ms:**

| Line | Package | Gate |
| --- | --- | --- |
| 107 | Global constraints | "Server gates for one paddock use half the tick budget: under 25 ms." |
| 147 | Risk 6 (F2.4) | "20 animals in one paddock keep the server tick under 25 ms on the reference setup." |
| 2752 | M2 exit gate | "20 animals in one paddock keep the server tick under 25 ms." |
| 3167 | F2.0 | "20 animals in one paddock keep the server tick under 25 ms on the reference setup" |
| 3754 | F2.4 | "This package's own gate is one paddock under 25 ms." |
| 4047 | F2.4 | "20 animals in one paddock keep the server tick under 25 ms on the reference setup." |

**120 fps:**

| Line | Package | Gate |
| --- | --- | --- |
| 152 | Risk 8 (F2.2) | "20 Gallimimus on screen reach at least 120 fps on the reference setup." |
| 2752 | M2 exit gate | "On the reference setup: 20 Gallimimus on screen reach at least 120 fps" |
| 3419 | F2.2 | "20 Gallimimus on screen run at 120 fps or more." |
| 3426 | F2.2 | "20 Gallimimus on screen run at 120 fps or more." |
| 3546 | F2.2 | "20 baked Gallimimus in view reach at least 120 fps on the reference setup." |

**Other fps and tick-time gates:**

| Line | Package | Gate |
| --- | --- | --- |
| 3544 | F2.2 | "A Dilophosaurus herd of 10 keeps client FPS above a baseline recorded in the PR." |
| 3728 | F2.3 | "within 10 % of the F2.2 baseline frame time" |
| 3931 | F2.4 | Shared block scan: "under 1,000 reads, under 0.1 ms, no aligned spike" |
| 4421 to 4424 | F2.5 | Load A "2 to 3 ms". Load B "9 to 15 ms plus repath spikes". Herd AI "under 1 ms on average, under 5 ms in any tick". |
| 5832 | BH.1 | "A park of 100 dinosaurs keeps 20 ticks per second" |
| 5847 | BH.1 | Load B "Target: 9 to 15 ms plus repath spikes." |
| 5928 | BH.1 | "The four lab machines at work meet the machine gate of D282 on the reference setup." |
| 6715 | D282 (open) | Proposal: the lab machines "add less than 2 ms to the server tick", and the client keeps "at least 90 % of its frame rate" |
