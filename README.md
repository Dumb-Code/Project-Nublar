# Project Nublar

Project Nublar is a Minecraft mod. The player clones dinosaurs from fossil DNA and keeps them in powered enclosures. This code is for Minecraft 1.20.1 on Forge and Fabric. Work on the Beta is in progress.

## Beta scope

In the Beta, the player finds fossils and amber, extracts and edits DNA, and hatches six dinosaur species. The player keeps the dinosaurs in a powered, fenced and fed park. The Beta release is for Minecraft 26.2 on Fabric and NeoForge. The design decisions are in [section 11 of the codebase analysis](docs/codebase-analysis.md#11-design-decisions-for-beta). The milestones are in [the project plan](docs/project-plan.md).

## Build

You need Java 17. On Windows, use `gradlew.bat` in place of `./gradlew`.

```sh
./gradlew build
```

This command builds the four modules. `common` holds the shared code and resources. `nublar-gui` holds the screen widgets. `forge` and `fabric` hold the code for each loader. The Forge jar goes to `forge/build/libs/`. The Fabric jar goes to `fabric/build/libs/`.

## Run

| Command | Starts |
| --- | --- |
| `./gradlew :forge:runClient` | The Forge client |
| `./gradlew :fabric:runClient` | The Fabric client |
| `./gradlew :forge:runServer` | The Forge dedicated server |
| `./gradlew :neoforge:runGameTestServer` | A NeoForge server that runs the mod's GameTests, then stops |
| `./gradlew :fabric:runGameTest` | A Fabric server that runs the mod's GameTests, then stops |

The Fabric client has no client entry point until M0.P2 (the port to Minecraft 26.2). It crashes when it enters a world.

The first `./gradlew :forge:runServer` writes `forge/run/eula.txt` and stops. To accept the [Minecraft EULA](https://aka.ms/MinecraftEULA), set `eula=true` in that file and run the command again.

`./gradlew build` also runs the Fabric GameTest server, because Fabric Loom makes `check` depend on `:fabric:runGameTest`. A failed required GameTest fails the build. For a quick local build without the GameTests, run `./gradlew build -x runGameTest`.

In one measured run of each, the NeoForge and the Fabric GameTest servers each took less than 2 minutes, and the tests in them less than 1 minute. Eight herd tests are optional: `grazingherdholdshardminimum`, `grazingmembersstayput`, `smallleadermoveskeepgraze`, `memberneverenterslowgap`, `herdskirtswater`, `herdtravelsalongtrail`, `herdcrossesforest` and `stackedmembersseparate`. They still run and report their result, but a failed optional test does not fail the GameTest server or `build`. The first seven are known to fail since Minecraft 1.20.1. On 26.2, `grazingherdholdshardminimum`, `smallleadermoveskeepgraze`, `memberneverenterslowgap` and `herdskirtswater` failed in every measured run, and `grazingmembersstayput`, `herdtravelsalongtrail` and `herdcrossesforest` failed in some runs. `stackedmembersseparate` is flaky. All the other GameTests are required, and they pass on both loaders. These failures are known. The herd fixes are a later plan task, so do not try to fix them in your work package.

## Test

To run the unit tests:

```sh
./gradlew :common:test
```

To run the GameTests in the NeoForge client:

1. Start the client with `./gradlew :neoforge:runClient`.
2. Create a creative world.
3. Type `/test run projectnublar:*` to run all of the mod's GameTests.
4. Type `/test run projectnublar:<name>` to run one GameTest. The name is the method name in lower case, for example `/test run projectnublar:fivegallimimusformoneherdledbythestrongest`.

The GameTest bodies are in `common/src/main/java/net/dumbcode/projectnublar/gametest/`. Each one is a vanilla test function (`GameTestInit`) with a test instance file in `common/src/generated/resources/data/projectnublar/test_instance/`, which `./gradlew :neoforge:runData` writes.

## Contribute

1. Read [How to use this plan](docs/plan/engineering-backlog.md#how-to-use-this-plan) in the engineering backlog.
2. Write the task plan for your work package in [`docs/superpowers/plans/`](docs/superpowers/plans/). Get a review of it before you write code.
3. A new clone starts on `integration/1.20.1`, the default branch. Make your branch from it. Name it `<wp-id>/<short-name>`, for example `f1.1/fossil-worldgen`.
4. Make one commit per task. Start each commit message with a conventional prefix, for example `feat:` or `fix:`.
5. Run `./gradlew build` before each push.
6. Open a pull request. The template lists the [Definition of done](docs/plan/engineering-backlog.md#5-definition-of-done).

## Licence

See [`LICENSE`](LICENSE).
