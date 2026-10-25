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
| `./gradlew :forge:runGameTestServer` | A Forge server that runs the mod's GameTests, then stops |
| `./gradlew :fabric:runGametest` | A Fabric server that runs the mod's GameTests, then stops |

The Fabric client has no client entry point until M0.P2 (the port to Minecraft 26.2). It crashes when it enters a world.

The first `./gradlew :forge:runServer` writes `forge/run/eula.txt` and stops. To accept the [Minecraft EULA](https://aka.ms/MinecraftEULA), set `eula=true` in that file and run the command again.

In one measured run of each, the Forge GameTest server took about 11 minutes and the Fabric GameTest server took less than 1 minute. Both GameTest servers report failures at this time and end with `BUILD FAILED`. On Forge, some herd tests fail, and the number changes from run to run (8 or 9 of 13 in recent runs). On Fabric, all 13 herd tests fail, because Fabric does not register the mod's data reload listeners until M0.P2. The machine tests pass on both loaders. These failures are known. Do not try to fix them in your work package.

## Test

To run the unit tests:

```sh
./gradlew :common:test
```

To run the GameTests in the Forge client:

1. Start the client with `./gradlew :forge:runClient`.
2. Create a creative world.
3. Type `/test runall` to run all of the mod's GameTests.
4. Type `/test run <name>` to run one GameTest. The name is the method name in lower case, for example `/test run fivegallimimusformoneherdledbythestrongest`.

The Fabric client cannot run GameTests until M0.P2. On Fabric, use `./gradlew :fabric:runGametest`. The GameTest bodies are in `common/src/main/java/net/dumbcode/projectnublar/gametest/`.

## Contribute

1. Read [How to use this plan](docs/plan/engineering-backlog.md#how-to-use-this-plan) in the engineering backlog.
2. Write the task plan for your work package in [`docs/superpowers/plans/`](docs/superpowers/plans/). Get a review of it before you write code.
3. A new clone starts on the default branch. Check out `integration/1.20.1` first (`git checkout integration/1.20.1`). Make your branch from it. Name it `<wp-id>/<short-name>`, for example `f1.1/fossil-worldgen`.
4. Make one commit per task. Start each commit message with a conventional prefix, for example `feat:` or `fix:`.
5. Run `./gradlew build` before each push.
6. Open a pull request. The template lists the [Definition of done](docs/plan/engineering-backlog.md#5-definition-of-done).

## Licence

See [`LICENSE`](LICENSE).
