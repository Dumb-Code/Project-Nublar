# Performance baseline

Check every fps gate and tick-time gate of the [engineering backlog](plan/engineering-backlog.md) with the setup on this page.

## Target player PCs

The mod must run well on the PCs that players use. A developer's own PC is never the target, because most developer PCs are faster than most players' PCs (lead, 2026-10-25, D284).

The targets are the two tiers that Mojang published for Java Edition on 2026-07-21 ([Minecraft: Java Edition system requirements](https://www.minecraft.net/en-us/article/minecraft-java-edition-system-requirements)):

| Tier | CPU | RAM | GPU | Vanilla target |
| --- | --- | --- | --- | --- |
| Minimum PC | 4 cores | 8 GB (12 GB with integrated graphics) | Vulkan 1.3, 2 GB VRAM | 1080p, 30 fps, Fast preset |
| Recommended PC | "A stronger modern processor" | 16 GB | 6 GB VRAM | 1080p, 60 fps, Fancy preset |

Every speed gate holds on both tiers. For example, 20 Gallimimus on screen keep at least 30 fps on the minimum PC with Fast graphics, and at least 60 fps on the recommended PC with Fancy graphics.

**Dev runs.** The dev runs use Java 17 (`java_version=17` in `gradle.properties`). `gradle.properties` also sets `org.gradle.jvmargs=-Xmx3G`. This flag sets the heap of the Gradle process. The run tasks in `forge/build.gradle` and `fabric/build.gradle` add no `-Xmx` flag.

## Fixed client settings

| Setting | Value |
| --- | --- |
| Window size | 1920 × 1080 |
| Graphics | Fast for a minimum-PC gate. Fancy for a recommended-PC gate. |
| Render distance | 12 chunks |
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
| Time | Locked at time of day 1000 (morning): `/time set 1000`, then `/gamerule doDaylightCycle false` |
| Weather | Clear: `/weather clear`, then `/gamerule doWeatherCycle false` |

To place a structure, use `/place template projectnublar:herd_arena` or `/place template projectnublar:herd_track`.

## How a gate is checked

A developer checks a gate on their own PC by comparing with vanilla (lead, 2026-10-25, D284):

1. Measure the test world with the content that the gate names, for example 20 Gallimimus or one paddock of 20 animals.
2. Measure the same world on the same PC with the same settings, without that content.
3. The gate passes when the difference stays inside the allowed difference.

The package that owns a gate states its allowed difference in its task plan. The lead reviews it there.

| Value | Reading | Result |
| --- | --- | --- |
| fps | The fps value on the F3 screen. Take three readings, one minute apart. | The median of the three readings |
| Server tick time (ms gates) | The F3 line `Integrated server @ N ms ticks`, in single player (lead, 2026-10-25, D285). On 1.20.1, the game sets this value on every tick to 80 % of the old value plus 20 % of the new tick time. M0.P2 must check this F3 line again on 26.2. Take three readings, one minute apart. | The median of the three readings. Use this row for the gates set in ms, except the gates named under the table. |
| Server tick rate (ticks-per-second gates) | Run `/debug start`. After 60 seconds, run `/debug stop`. | The ticks per second that `/debug stop` reports. Use this row only for the "20 ticks per second" gates. |

The single-player F3 reading cannot test these gates:

- Backlog line 3931: under 0.1 ms. The F3 line shows whole milliseconds.
- Backlog line 4424: herd AI under 1 ms on average. The F3 line shows whole milliseconds.
- Backlog line 4424: herd AI under 5 ms in any tick. The F3 value is an average, so it cannot show the peak of one tick.
- Backlog line 6715: the machine increment of 2 ms, which is too close to the 1 ms step of the F3 line.
- Backlog line 5928: the lab machines meet the machine gate of D282. That gate is the proposed 2 ms increment of line 6715.
- Backlog lines 4423 and 5847: Load B, 9 to 15 ms. Load B has 3 players (line 5846), and BH.1 profiles it on a dedicated server (line 5843).

The package that owns one of these gates sets its method in its task plan. The same rule applies to a gate on a dedicated server.

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

**Gallimimus on screen, 30 fps on the minimum PC and 60 fps on the recommended PC:**

| Line | Package | Gate |
| --- | --- | --- |
| 152 | Risk 8 (F2.2) | "20 Gallimimus on screen keep at least 30 fps on the minimum PC (Fast) and 60 fps on the recommended PC (Fancy)." |
| 2752 | M2 exit gate | "20 Gallimimus on screen keep at least 30 fps on the minimum PC (Fast) and 60 fps on the recommended PC (Fancy)" |
| 3419 | F2.2 | "20 Gallimimus on screen keep at least 30 fps on the minimum PC (Fast) and 60 fps on the recommended PC (Fancy)." |
| 3426 | F2.2 | "20 Gallimimus on screen keep at least 30 fps on the minimum PC (Fast) and 60 fps on the recommended PC (Fancy)." |
| 3546 | F2.2 | "20 baked Gallimimus in view keep at least 30 fps on the minimum PC (Fast) and 60 fps on the recommended PC (Fancy)." |

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
