# Herd Movement (Layered Design) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the follow-the-leader movement with a layered herd: the leader pathfinds, followers hold soft slots in a loose wedge while travelling, graze around a weighted anchor while resting, and steer around each other every tick, with vanilla pathfinding only as a budgeted fallback.

**Architecture:** Pure, unit-tested building blocks (`LeaderTrail`, `WedgeLayout`, `HerdArea`, `HerdSteering`, `FallbackBudget`, `TickTimer`) feed two game-facing parts: `FollowFormation` (a SmartBrainLib behaviour that picks each follower's target point per herd state) and `HerdMoveControl` (per-tick steering, step check, step aside). `DinoGroup` holds the herd state, trail, slot assignments, step-check cache and fallback budget; `HerdLeading` updates them from the leader's tick.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.2.30 through ModDevGradle, SmartBrainLib 1.15, GeckoLib 4.7.3, Architectury multiloader layout (`common/` + `forge/`).

**Spec:** `docs/superpowers/specs/2026-10-08-herd-movement-design.md`

## Global Constraints

- Work only in the worktree `C:\Users\kgumm\Documents\Minecraft Modding\Project-Nublar\.claude\worktrees\herd-leader-sample` (branch `herd-leader-sample`). Never edit the main checkout.
- **Do not commit.** The lead chose "no commits" on 2026-10-08. Every task ends with a `git status --short` check instead of a commit.
- All new herd classes go in package `net.dumbcode.projectnublar.entity.ai.behaviour.social`.
- `HerdSpacing.applies(Dinosaur)` stays the single switch for spacing exceptions; every spacing, step-aside and formation path must call it.
- Every distance, weight and timing is a named `static final` constant in the class that uses it (spec: "tuned in playtest").
- Performance budget (spec): herd AI for 20 animals under 1 ms per tick on average; no single tick over 5 ms from herd code; at most 2 fallback paths per herd per tick.
- No new dependencies. The project has no JUnit; unit tests run on the stand-in runner from Task 1.
- In-game checks: launch the headed client (`./gradlew :forge:runClient` from the worktree root) and let the lead test. Never run `runGameTestServer` (the mod also cannot load on a dedicated server today). GameTests are run by the lead with `/test runall` in a creative world.
- Code style: match the surrounding code; Javadoc on every class and on non-obvious methods; no per-tick allocations unless a neighbour is actually in range (existing rule in `HerdSpacing.crowding`).

## Review Focus

1. **A formation slot over water next to the leader's trail.** A follower must not walk into the water; its slot squeezes toward the trail. Pinned by the `herdSkirtsWater` GameTest in Task 16.
2. **The leader changes mid-travel** (death, unload, contest). Followers keep following the new leader and get new slots. Pinned by `leaderChangeMarksSlotsDirty` in Task 13.
3. **A member joins or leaves mid-travel.** Slots are reassigned and no follower reads a slot index beyond the herd size. Pinned by `membershipChangeMarksSlotsDirtyAndForgetsTheSlot` in Task 13, plus the index guard in Task 15.
4. **A herd of two** (one follower). The single follower walks directly behind the leader, not off to one side. Pinned by `singleFollowerIsDirectlyBehind` in Task 8.
5. **No valid grazing spot** (a herd on a small island or pillar). The follower stays put and does not request fallback paths in a loop. Pinned by `roomiestOfNoCandidatesIsMinusOne` in Task 9, plus the "nowhere to go" branch in Task 15.

---

## File map

| File | Responsibility | Task |
| --- | --- | --- |
| `tools/junit-standin/**` | JUnit stand-ins and a runner script for `common/src/test/java` | 1 |
| `common/src/test/java/.../social/*Test.java` | Unit tests | 1, 5, 7–11, 13 |
| `common/src/main/java/.../entity/dinosaur/Dinosaur.java` | Stamina flag; wiring; leader tick hook | 2, 15 |
| `common/src/main/java/.../init/EntityInit.java` | Client tracking range | 3 |
| `common/src/main/java/.../util/DebugFlags.java` | Client debug switch | 4 |
| `common/src/main/java/.../client/renderer/DinosaurRenderer.java` | Skip colour layers when the switch is on | 4 |
| `common/src/main/java/.../item/DebugStick.java` | Toggle on sneak-use; herd timing readout | 4, 5 |
| `.../social/TickTimer.java`, `.../social/HerdTimings.java` | Herd code timing | 5 |
| `.../social/LeaderTrail.java` | Leader trail ring buffer | 7 |
| `.../social/WedgeLayout.java` | Wedge slots and assignment | 8 |
| `.../social/HerdArea.java` | Grazing anchor, radius, roomiest spot | 9 |
| `.../social/HerdSteering.java`, `.../social/HerdSpacing.java` | Steering sum, turn limit; `away` | 10 |
| `.../social/FallbackBudget.java` | Fallback path budget | 11 |
| `.../social/StepCheck.java` | Simple passability check, open width | 12 |
| `.../social/HerdState.java`, `.../social/DinoGroup.java`, `.../social/HerdLeading.java` | Herd state, trail, slots in the group; leader tick | 13 |
| `.../social/HerdMoveControl.java` | Steering mode, step check, step aside | 14 |
| `.../social/FollowFormation.java` (new), `.../social/FollowGroupLeader.java` (deleted) | Follower targets per state | 15 |
| `tools/gametest-templates/make_templates.py`, `forge/src/main/resources/data/projectnublar/structures/*.nbt`, `forge/src/main/java/.../gametest/HerdGameTests.java` | GameTest arenas and tests | 16 |

`...` stands for `net/dumbcode/projectnublar`; `.../social` is `common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social`.

**Verification commands used throughout** (run from the worktree root in Git Bash):

```bash
./gradlew :common:compileJava
```

```bash
sh tools/junit-standin/run.sh
```

```bash
./gradlew :forge:compileJava
```

---

### Task 1: Unit-test runner in the worktree

The project has no JUnit. This task adds JUnit stand-ins (the annotation and the assertions the tests use) and a runner script, and moves the existing unit tests into `common/src/test/java`.

**Files:**
- Create: `tools/junit-standin/stub/org/junit/jupiter/api/Test.java`
- Create: `tools/junit-standin/stub/org/junit/jupiter/api/Assertions.java`
- Create: `tools/junit-standin/RunTests.java`
- Create: `tools/junit-standin/run.sh`
- Create: `common/src/test/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/DominanceTest.java` (copy)
- Create: `common/src/test/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/GroupManagerTest.java` (copy)
- Existing: `common/src/test/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/HerdSpacingTest.java`

**Interfaces:**
- Produces: `sh tools/junit-standin/run.sh` runs every `*Test.java` under `common/src/test/java` and exits non-zero on any failure. Assertions available: `assertTrue/False(boolean[, String])`, `assertNull/NotNull(Object[, String])`, `assertSame(Object, Object[, String])`, `assertEquals(Object, Object[, String])`, `assertEquals(int, int[, String])`, `assertEquals(long, long[, String])`, `assertEquals(float, float, float[, String])`, `assertEquals(double, double, double[, String])`.

- [ ] **Step 1: Create the annotation stand-in**

```java
package org.junit.jupiter.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Stand-in for JUnit 5's @Test: the project has no JUnit setup. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
}
```

- [ ] **Step 2: Create the assertions stand-in**

```java
package org.junit.jupiter.api;

import java.util.Objects;

/** Stand-in for the JUnit 5 assertions the unit tests use. Failures throw AssertionError. */
public final class Assertions {

    private Assertions() {
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }

    public static void assertTrue(boolean condition) { assertTrue(condition, "expected true"); }
    public static void assertTrue(boolean condition, String message) { if (!condition) fail(message); }
    public static void assertFalse(boolean condition) { assertFalse(condition, "expected false"); }
    public static void assertFalse(boolean condition, String message) { if (condition) fail(message); }
    public static void assertNull(Object value) { assertNull(value, "expected null"); }
    public static void assertNull(Object value, String message) { if (value != null) fail(message + " (was " + value + ")"); }
    public static void assertNotNull(Object value) { assertNotNull(value, "expected a value"); }
    public static void assertNotNull(Object value, String message) { if (value == null) fail(message); }
    public static void assertSame(Object expected, Object actual) { assertSame(expected, actual, "not the same object"); }
    public static void assertSame(Object expected, Object actual, String message) { if (expected != actual) fail(message + ": expected " + expected + ", got " + actual); }
    public static void assertEquals(Object expected, Object actual) { assertEquals(expected, actual, "not equal"); }
    public static void assertEquals(Object expected, Object actual, String message) { if (!Objects.equals(expected, actual)) fail(message + ": expected " + expected + ", got " + actual); }
    public static void assertEquals(int expected, int actual) { assertEquals(expected, actual, "not equal"); }
    public static void assertEquals(int expected, int actual, String message) { if (expected != actual) fail(message + ": expected " + expected + ", got " + actual); }
    public static void assertEquals(long expected, long actual) { assertEquals(expected, actual, "not equal"); }
    public static void assertEquals(long expected, long actual, String message) { if (expected != actual) fail(message + ": expected " + expected + ", got " + actual); }
    public static void assertEquals(float expected, float actual, float delta) { assertEquals(expected, actual, delta, "not equal"); }
    public static void assertEquals(float expected, float actual, float delta, String message) { if (Math.abs(expected - actual) > delta) fail(message + ": expected " + expected + ", got " + actual); }
    public static void assertEquals(double expected, double actual, double delta) { assertEquals(expected, actual, delta, "not equal"); }
    public static void assertEquals(double expected, double actual, double delta, String message) { if (Math.abs(expected - actual) > delta) fail(message + ": expected " + expected + ", got " + actual); }
}
```

- [ ] **Step 3: Create the runner**

```java
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Runs every @Test method of the given classes on a fresh instance, prints PASS/FAIL, exits with the failure count. */
public final class RunTests {

    public static void main(String[] classNames) throws Exception {
        int runs = 0;
        int failures = 0;
        for (String name : classNames) {
            Class<?> type = Class.forName(name);
            for (Method method : type.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(org.junit.jupiter.api.Test.class)) {
                    continue;
                }
                Constructor<?> constructor = type.getDeclaredConstructor();
                constructor.setAccessible(true);
                method.setAccessible(true);
                runs++;
                try {
                    method.invoke(constructor.newInstance());
                    System.out.println("PASS " + type.getSimpleName() + "." + method.getName());
                } catch (InvocationTargetException e) {
                    failures++;
                    System.out.println("FAIL " + type.getSimpleName() + "." + method.getName() + ": " + e.getCause());
                }
            }
        }
        System.out.println(runs + " run, " + failures + " failed");
        System.exit(failures);
    }
}
```

- [ ] **Step 4: Create the runner script**

```sh
#!/bin/sh
# Runs the unit tests in common/src/test/java against the JUnit stand-ins (the project has no JUnit).
# Use from the worktree root in Git Bash on Windows, after: ./gradlew :common:compileJava
set -e
ROOT="$(pwd)"
HERE="$ROOT/tools/junit-standin"
OUT="$ROOT/build/junit-standin"
CP="$ROOT/common/build/classes/java/main"

# Minecraft with the dev mappings the common module compiles against.
MC=$(ls ~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/*/minecraft-merged-*.jar | head -1)
CP="$CP;$(cygpath -w "$MC")"
# Libraries Minecraft classes need at class-load time.
for pattern in "guava-3*.jar" "fastutil-8*.jar" "commons-lang3-*.jar" "slf4j-api-2*.jar" "datafixerupper-6*.jar" "logging-1*.jar" "annotations-2*.jar" "joml-1*.jar"; do
  jar=$(find ~/.gradle/caches/modules-2 -name "$pattern" | grep -v sources | head -1)
  [ -n "$jar" ] && CP="$CP;$(cygpath -w "$jar")"
done
# Mod libraries, remapped by ModDevGradle. The verifier loads Dinosaur, which needs them.
for pattern in "geckolib-forge-1.20.1-*.jar" "SmartBrainLib-forge-1.20.1-*.jar"; do
  jar=$(find ~/.gradle/caches -path "*transforms*" -name "$pattern" | grep -v sources | head -1)
  [ -n "$jar" ] && CP="$CP;$(cygpath -w "$jar")"
done

rm -rf "$OUT"
TEST_SOURCES=$(find "$ROOT/common/src/test/java" -name "*Test.java")
TEST_CLASSES=$(cd "$ROOT/common/src/test/java" && find . -name "*Test.java" | sed 's|^\./||; s|\.java$||; s|/|.|g')
javac -cp "$CP" -d "$OUT" "$HERE"/stub/org/junit/jupiter/api/*.java "$HERE/RunTests.java" $TEST_SOURCES
java -cp "$OUT;$CP" RunTests $TEST_CLASSES 2>/dev/null
```

- [ ] **Step 5: Copy the two existing tests into the test tree**

```bash
cp "C:/Users/kgumm/Documents/Minecraft Modding/Project-Nublar/docs/samples/herd-leader/DominanceTest.java" common/src/test/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/
```

```bash
cp "C:/Users/kgumm/Documents/Minecraft Modding/Project-Nublar/docs/samples/herd-leader/GroupManagerTest.java" common/src/test/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/
```

- [ ] **Step 6: Run everything**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: `27 run, 0 failed` (8 `HerdSpacingTest`, 8 `DominanceTest`, 11 `GroupManagerTest`).

- [ ] **Step 7: Checkpoint (no commit)**

Run: `git status --short`
Expected: new `tools/junit-standin/` and the two copied tests, plus the earlier uncommitted work.

---

### Task 2: Stamina drains only on self-propelled movement (B17)

A shove from a herd mate gives velocity, and the current check counts velocity as walking. Use the navigation path or a wanted position in the move control instead.

**Files:**
- Modify: `common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java` (in `customServerAiStep`)

**Interfaces:**
- Consumes: `PathNavigation.isInProgress()`, `MoveControl.hasWanted()` (vanilla).

- [ ] **Step 1: Replace the movement test**

Replace:

```java
        // Stamina drains only while the animal moves. The server owns stamina, so it decides here;
        // the animation predicate runs on the client only and its writes never reached the server.
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        this.entityData.set(SHOULD_TICK_STAMINA, moving || this.isRunning() || this.isSwimming());
```

with:

```java
        // Stamina drains only while the animal moves under its own power: a navigation path, or a wanted
        // position in the move control (herd steering). Not velocity: a shove from a herd mate is velocity too.
        // The server owns stamina, so it decides here; the animation predicate runs on the client only.
        boolean selfPropelled = this.getNavigation().isInProgress() || this.getMoveControl().hasWanted();
        this.entityData.set(SHOULD_TICK_STAMINA, selfPropelled || this.isRunning() || this.isSwimming());
```

- [ ] **Step 2: Compile**

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Checkpoint (no commit)**

Run: `git status --short`. The in-game check is part of Task 17 (pushed animals keep their stamina).

---

### Task 3: Client tracking range (B16)

Dinosaurs use the default tracking range of 5 chunks (80 blocks), so they vanish at a distance. Vanilla animals use 10.

**Files:**
- Modify: `common/src/main/java/net/dumbcode/projectnublar/init/EntityInit.java:30-39`

- [ ] **Step 1: Add `.clientTrackingRange(10)` to the six dinosaur builders**

Replace each `.sized(...)` call for the six dinosaurs so the line reads, for example:

```java
    public static final DeferredSupplier<EntityType<GallimimusEntity>> GALLIMIMUS = registerEntity("gallimimus", ()-> EntityType.Builder.of(GallimimusEntity::new, MobCategory.MONSTER).sized(2,3).clientTrackingRange(10), Dinosaur::createAttributes);
```

Apply the same `.clientTrackingRange(10)` after `.sized(1,3)` (Tyrannosaurus), `.sized(0.6F,0.6F)` (Velociraptor), `.sized(1,1)` (Dilophosaurus), `.sized(2,3)` (Triceratops), `.sized(2,3)` (Brachiosaurus). Do not change `DINOSAUR_PART`.

- [ ] **Step 2: Compile**

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Checkpoint (no commit)**

Run: `git status --short`. The in-game check is in Task 17.

---

### Task 4: Frame-rate measurement toggle (B09, measurement only)

Sneak and use the debug stick in the air to switch the colour-layer passes off and on. The lead compares the F3 frame rate with the herd in view.

**Files:**
- Create: `common/src/main/java/net/dumbcode/projectnublar/util/DebugFlags.java`
- Modify: `common/src/main/java/net/dumbcode/projectnublar/client/renderer/DinosaurRenderer.java` (`applyRenderLayers`)
- Modify: `common/src/main/java/net/dumbcode/projectnublar/item/DebugStick.java`

**Interfaces:**
- Produces: `DebugFlags.skipDinoColourLayers` (public static volatile boolean).

- [ ] **Step 1: Create the flag holder**

```java
package net.dumbcode.projectnublar.util;

/**
 * Switches for measuring, toggled in game and never saved. They live in common code so items can flip
 * them without loading client classes.
 */
public final class DebugFlags {

    /** When true, {@code DinosaurRenderer} skips the colour-layer passes, to measure what they cost. */
    public static volatile boolean skipDinoColourLayers = false;

    private DebugFlags() {
    }
}
```

- [ ] **Step 2: Skip the layers when the flag is on**

In `DinosaurRenderer.applyRenderLayers`, insert at the top of the method:

```java
        if (DebugFlags.skipDinoColourLayers) {
            return;
        }
```

and add `import net.dumbcode.projectnublar.util.DebugFlags;`.

- [ ] **Step 3: Toggle on sneak-use in the air**

In `DebugStick`, add imports `net.dumbcode.projectnublar.util.DebugFlags`, `net.minecraft.world.InteractionResultHolder`, `net.minecraft.world.level.Level`, and this method:

```java
    /** Sneak and use in the air: switches the dinosaur colour layers off and on, to measure their frame-rate cost. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        if (level.isClientSide()) {
            DebugFlags.skipDinoColourLayers = !DebugFlags.skipDinoColourLayers;
            player.displayClientMessage(Component.literal("Dinosaur colour layers: "
                    + (DebugFlags.skipDinoColourLayers ? "off (measuring)" : "on")), true);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
```

- [ ] **Step 4: Compile**

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`. The measurement itself is in Task 6.

---

### Task 5: Herd timing (average and worst tick)

`/debug` profiles give averages only. The budget also caps the worst tick, so herd code times itself.

**Files:**
- Create: `.../social/TickTimer.java`
- Create: `.../social/HerdTimings.java`
- Test: `common/src/test/java/.../social/TickTimerTest.java`
- Modify: `.../social/HerdMoveControl.java` (time `tick`)
- Modify: `.../social/FollowGroupLeader.java` (time `start`, baseline only; deleted in Task 15)
- Modify: `common/src/main/java/net/dumbcode/projectnublar/item/DebugStick.java` (readout line)

**Interfaces:**
- Produces: `TickTimer` with `void add(long gameTime, long nanos)`, `double averageMillis()`, `double worstMillis()`, constant `WINDOW = 600`. `HerdTimings` with `static void record(long gameTime, long nanos)`, `public static double averageMillis()`, `public static double worstMillis()`.

- [ ] **Step 1: Write the failing test**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TickTimerTest {

    private static final double EPSILON = 1.0E-9D;
    private static final long MS = 1_000_000L;

    @Test
    void emptyTimerReportsZero() {
        TickTimer timer = new TickTimer();
        assertEquals(0D, timer.averageMillis(), EPSILON);
        assertEquals(0D, timer.worstMillis(), EPSILON);
    }

    @Test
    void timesInOneTickAddUpAndCountWhenTheTickEnds() {
        TickTimer timer = new TickTimer();
        timer.add(1, MS);
        timer.add(1, 2 * MS);
        assertEquals(0D, timer.averageMillis(), EPSILON, "tick 1 is still open");
        timer.add(2, 0);
        assertEquals(3D, timer.averageMillis(), EPSILON);
        assertEquals(3D, timer.worstMillis(), EPSILON);
    }

    @Test
    void averageAndWorstCoverFinishedTicks() {
        TickTimer timer = new TickTimer();
        timer.add(1, MS);
        timer.add(2, 2 * MS);
        timer.add(3, 3 * MS);
        timer.add(4, 0);
        assertEquals(2D, timer.averageMillis(), EPSILON);
        assertEquals(3D, timer.worstMillis(), EPSILON);
    }

    @Test
    void windowKeepsOnlyTheLatestTicks() {
        TickTimer timer = new TickTimer();
        timer.add(0, 100 * MS);
        for (long tick = 1; tick <= TickTimer.WINDOW + 1; tick++) {
            timer.add(tick, MS);
        }
        assertEquals(1D, timer.worstMillis(), EPSILON, "the 100 ms tick has left the window");
        assertEquals(1D, timer.averageMillis(), EPSILON);
    }
}
```

- [ ] **Step 2: Run it to see it fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac error `cannot find symbol: class TickTimer`.

- [ ] **Step 3: Implement `TickTimer`**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/**
 * Sums the time herd code spends in each game tick and keeps the last {@link #WINDOW} finished ticks,
 * for the performance budget: average and worst tick. Ticks in which herd code did not run are not
 * counted, which makes the average a little pessimistic. Server thread only.
 */
final class TickTimer {

    static final int WINDOW = 600;

    private final long[] totals = new long[WINDOW];
    private int next;
    private int filled;
    private long currentTick = Long.MIN_VALUE;
    private long currentTotal;

    void add(long gameTime, long nanos) {
        if (gameTime != this.currentTick) {
            closeTick();
            this.currentTick = gameTime;
        }
        this.currentTotal += nanos;
    }

    double averageMillis() {
        if (this.filled == 0) {
            return 0D;
        }
        long sum = 0L;
        for (int i = 0; i < this.filled; i++) {
            sum += this.totals[i];
        }
        return sum / (double) this.filled / 1.0E6D;
    }

    double worstMillis() {
        long worst = 0L;
        for (int i = 0; i < this.filled; i++) {
            worst = Math.max(worst, this.totals[i]);
        }
        return worst / 1.0E6D;
    }

    private void closeTick() {
        if (this.currentTick == Long.MIN_VALUE) {
            return;
        }
        this.totals[this.next] = this.currentTotal;
        this.next = (this.next + 1) % WINDOW;
        this.filled = Math.min(this.filled + 1, WINDOW);
        this.currentTotal = 0L;
    }
}
```

- [ ] **Step 4: Implement `HerdTimings`**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/** The one herd timer per server JVM, read by the debug stick. Server thread only. */
public final class HerdTimings {

    private static final TickTimer TIMER = new TickTimer();

    private HerdTimings() {
    }

    static void record(long gameTime, long nanos) {
        TIMER.add(gameTime, nanos);
    }

    public static double averageMillis() {
        return TIMER.averageMillis();
    }

    public static double worstMillis() {
        return TIMER.worstMillis();
    }
}
```

- [ ] **Step 5: Run the tests**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: `31 run, 0 failed`.

- [ ] **Step 6: Time the current herd code (the baseline)**

In `HerdMoveControl.tick`, wrap the herd part:

```java
    @Override
    public void tick() {
        long start = System.nanoTime();
        if (this.operation == Operation.MOVE_TO && HerdSpacing.applies(this.dinosaur)) {
            bendAwayFromHerd();
        }
        HerdTimings.record(this.mob.level().getGameTime(), System.nanoTime() - start);
        super.tick();
    }
```

In `FollowGroupLeader.start`, add `long startNanos = System.nanoTime();` as the first line, and before each `return` and at the end of the method add `HerdTimings.record(entity.level().getGameTime(), System.nanoTime() - startNanos);`. The simplest form is to rename the current body to `private void pickTarget(E entity)` and make `start` call it:

```java
    @Override
    protected void start(E entity) {
        long startNanos = System.nanoTime();
        pickTarget(entity);
        HerdTimings.record(entity.level().getGameTime(), System.nanoTime() - startNanos);
    }
```

- [ ] **Step 7: Add the readout line to the debug stick**

In `DebugStick.interactLivingEntity`, after the `addMovementDebug(...)` call, add:

```java
                lines.add(String.format("Herd AI: average %.3f ms/tick, worst %.3f ms (last %d ticks with herd work)",
                        HerdTimings.averageMillis(), HerdTimings.worstMillis(), 600));
```

and `import net.dumbcode.projectnublar.entity.ai.behaviour.social.HerdTimings;`.

- [ ] **Step 8: Compile**

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Checkpoint (no commit)**

Run: `git status --short`

---

### Task 6: Baseline measurement (the lead, in game)

Record today's cost before the new design replaces it.

- [ ] **Step 1: Launch the headed client**

Run (in the background): `./gradlew :forge:runClient`
Expected: the log line `Sound engine started`.

- [ ] **Step 2: The lead measures** (hand over these steps)

1. In a fresh creative world, hatch 20 Gallimimus from the creative-tab eggs close together. Wait until the debug stick shows `Group: 20 members` on one of them.
2. `/debug start`, let the herd move for 60 seconds over open ground and then through trees, then `/debug stop`.
3. Use the debug stick on any herd member and note the `Herd AI:` line.
4. With the herd in view, note the F3 frame rate. Sneak and use the debug stick in the air (colour layers off), note the frame rate again, then switch the layers back on.

- [ ] **Step 3: Record the results**

Read the newest `forge/run/debug/profile-results-*.txt`. Note the share of the `projectnublar:gallimimus` section under entity ticking, and the lead's `Herd AI` line and the two frame rates. Write them into a new section `## Baseline (Task 6)` at the end of this plan file.

---

### Task 7: `LeaderTrail`

**Files:**
- Create: `.../social/LeaderTrail.java`
- Test: `common/src/test/java/.../social/LeaderTrailTest.java`

**Interfaces:**
- Produces: `LeaderTrail` with constants `CAPACITY = 64`, `RECORD_INTERVAL = 5`, `MIN_STEP = 0.5`; `record TrailPoint(double x, double y, double z, double dirX, double dirZ, int entry)`; methods `int size()`, `void clear()`, `void record(double x, double y, double z)`, `@Nullable TrailPoint pointBehind(double distance)`, `float openWidth(int entry, boolean left)`, `void setOpenWidth(int entry, boolean left, float width)`.

- [ ] **Step 1: Write the failing test**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaderTrailTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void fewerThanTwoEntriesGiveNoPoint() {
        LeaderTrail trail = new LeaderTrail();
        assertNull(trail.pointBehind(1D));
        trail.record(0D, 64D, 0D);
        assertNull(trail.pointBehind(1D));
    }

    @Test
    void pointBehindInterpolatesAlongAStraightTrail() {
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x <= 10; x++) {
            trail.record(x, 64D, 0D);
        }
        LeaderTrail.TrailPoint point = trail.pointBehind(3.5D);
        assertNotNull(point);
        assertEquals(6.5D, point.x(), EPSILON);
        assertEquals(64D, point.y(), EPSILON);
        assertEquals(0D, point.z(), EPSILON);
        assertEquals(1D, point.dirX(), EPSILON);
        assertEquals(0D, point.dirZ(), EPSILON);
    }

    @Test
    void pointBehindFollowsCorners() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(4D, 64D, 0D);
        trail.record(4D, 64D, 4D);
        LeaderTrail.TrailPoint onLastLeg = trail.pointBehind(2D);
        assertEquals(4D, onLastLeg.x(), EPSILON);
        assertEquals(2D, onLastLeg.z(), EPSILON);
        assertEquals(1D, onLastLeg.dirZ(), EPSILON, "travel direction on the last leg is +z");
        LeaderTrail.TrailPoint onFirstLeg = trail.pointBehind(6D);
        assertEquals(2D, onFirstLeg.x(), EPSILON);
        assertEquals(0D, onFirstLeg.z(), EPSILON);
        assertEquals(1D, onFirstLeg.dirX(), EPSILON, "travel direction on the first leg is +x");
    }

    @Test
    void beyondTheTrailGivesTheOldestEntry() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(3D, 64D, 0D);
        LeaderTrail.TrailPoint point = trail.pointBehind(50D);
        assertEquals(0D, point.x(), EPSILON);
        assertEquals(1D, point.dirX(), EPSILON);
    }

    @Test
    void standingLeaderDoesNotFillTheTrail() {
        LeaderTrail trail = new LeaderTrail();
        for (int i = 0; i < 10; i++) {
            trail.record(5D, 64D, 5D + i * 0.01D);
        }
        assertEquals(1, trail.size());
    }

    @Test
    void ringBufferKeepsTheNewestEntries() {
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x < 100; x++) {
            trail.record(x, 64D, 0D);
        }
        assertEquals(LeaderTrail.CAPACITY, trail.size());
        assertEquals(100 - LeaderTrail.CAPACITY, trail.pointBehind(1000D).x(), EPSILON);
    }

    @Test
    void openWidthsStartUnknownAndAreKeptPerSide() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(1D, 64D, 0D);
        int entry = trail.pointBehind(0.5D).entry();
        assertTrue(Float.isNaN(trail.openWidth(entry, true)));
        trail.setOpenWidth(entry, true, 3F);
        assertEquals(3F, trail.openWidth(entry, true), 0F);
        assertTrue(Float.isNaN(trail.openWidth(entry, false)));
    }
}
```

- [ ] **Step 2: Run it to see it fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac error `cannot find symbol: class LeaderTrail`.

- [ ] **Step 3: Implement**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.jetbrains.annotations.Nullable;

/**
 * The points the herd leader actually walked, in a fixed ring buffer. Followers in travel steer to
 * points on this trail, so they inherit a route the leader has proven walkable.
 * <p>
 * A position closer than {@link #MIN_STEP} to the last recorded one is skipped, so a standing leader
 * does not fill the buffer. Each entry also caches the open width to its left and right, measured
 * lazily by {@link StepCheck#openWidth}.
 */
final class LeaderTrail {

    static final int CAPACITY = 64;
    /** The leader records every this many ticks. */
    static final int RECORD_INTERVAL = 5;
    static final double MIN_STEP = 0.5D;

    /** A point on the trail, the direction of travel there (unit, horizontal), and the entry it belongs to. */
    record TrailPoint(double x, double y, double z, double dirX, double dirZ, int entry) {
    }

    private final double[] xs = new double[CAPACITY];
    private final double[] ys = new double[CAPACITY];
    private final double[] zs = new double[CAPACITY];
    private final float[] openLeft = new float[CAPACITY];
    private final float[] openRight = new float[CAPACITY];
    private int newest = -1;
    private int size;

    int size() {
        return this.size;
    }

    void clear() {
        this.newest = -1;
        this.size = 0;
    }

    void record(double x, double y, double z) {
        if (this.size > 0) {
            double dx = x - this.xs[this.newest];
            double dz = z - this.zs[this.newest];
            if (dx * dx + dz * dz < MIN_STEP * MIN_STEP) {
                return;
            }
        }
        this.newest = (this.newest + 1) % CAPACITY;
        this.xs[this.newest] = x;
        this.ys[this.newest] = y;
        this.zs[this.newest] = z;
        this.openLeft[this.newest] = Float.NaN;
        this.openRight[this.newest] = Float.NaN;
        if (this.size < CAPACITY) {
            this.size++;
        }
    }

    /**
     * The point {@code distance} blocks behind the newest entry, measured along the trail. If the trail
     * is shorter, its oldest entry. {@code null} with fewer than 2 entries.
     */
    @Nullable TrailPoint pointBehind(double distance) {
        if (this.size < 2) {
            return null;
        }
        double remaining = distance;
        for (int back = 0; back < this.size - 1; back++) {
            int newer = entry(back);
            int older = entry(back + 1);
            double segmentX = this.xs[newer] - this.xs[older];
            double segmentZ = this.zs[newer] - this.zs[older];
            double length = Math.sqrt(segmentX * segmentX + segmentZ * segmentZ);
            if (remaining <= length) {
                double t = remaining / length;
                return new TrailPoint(this.xs[newer] - segmentX * t,
                        this.ys[newer] + (this.ys[older] - this.ys[newer]) * t,
                        this.zs[newer] - segmentZ * t,
                        segmentX / length, segmentZ / length, newer);
            }
            remaining -= length;
        }
        int oldest = entry(this.size - 1);
        int second = entry(this.size - 2);
        double segmentX = this.xs[second] - this.xs[oldest];
        double segmentZ = this.zs[second] - this.zs[oldest];
        double length = Math.sqrt(segmentX * segmentX + segmentZ * segmentZ);
        return new TrailPoint(this.xs[oldest], this.ys[oldest], this.zs[oldest],
                segmentX / length, segmentZ / length, oldest);
    }

    /** Cached open width beside an entry, or NaN if not measured yet. */
    float openWidth(int entry, boolean left) {
        return left ? this.openLeft[entry] : this.openRight[entry];
    }

    void setOpenWidth(int entry, boolean left, float width) {
        if (left) {
            this.openLeft[entry] = width;
        } else {
            this.openRight[entry] = width;
        }
    }

    /** Buffer index {@code back} entries older than the newest (0 is the newest). */
    private int entry(int back) {
        return Math.floorMod(this.newest - back, CAPACITY);
    }
}
```

Segments always have length at least `MIN_STEP` because `record` skips shorter steps, so the divisions are safe.

- [ ] **Step 4: Run the tests**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: `38 run, 0 failed`.

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`

---

### Task 8: `WedgeLayout`

**Files:**
- Create: `.../social/WedgeLayout.java`
- Test: `common/src/test/java/.../social/WedgeLayoutTest.java`

**Interfaces:**
- Produces: `WedgeLayout` with constants `ROW_SPACING = 1.0`, `LATERAL_SPACING = 1.3`, `MAX_JITTER = 0.5`; `record Slot(int row, double lateral)` (lateral in blocks, positive to the leader's left); `static Slot slot(int index, int followers, double personalSpace)`; `static double rowDistance(int row, double personalSpace)`; `static double jitter(UUID id, int salt)`; `static int[] assign(double[][] followerXz, double[][] slotXz)`.

- [ ] **Step 1: Write the failing test**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WedgeLayoutTest {

    private static final double EPSILON = 1.0E-9D;
    private static final double SPACE = 3D;

    @Test
    void rowsGrowByOneSlot() {
        assertEquals(1, WedgeLayout.slot(0, 5, SPACE).row());
        assertEquals(1, WedgeLayout.slot(1, 5, SPACE).row());
        assertEquals(2, WedgeLayout.slot(2, 5, SPACE).row());
        assertEquals(2, WedgeLayout.slot(4, 5, SPACE).row());
        assertEquals(3, WedgeLayout.slot(5, 9, SPACE).row());
    }

    @Test
    void fullRowsAreCentredOnTheTrail() {
        double step = WedgeLayout.LATERAL_SPACING * SPACE;
        assertEquals(-step / 2D, WedgeLayout.slot(0, 5, SPACE).lateral(), EPSILON);
        assertEquals(step / 2D, WedgeLayout.slot(1, 5, SPACE).lateral(), EPSILON);
        assertEquals(-step, WedgeLayout.slot(2, 5, SPACE).lateral(), EPSILON);
        assertEquals(0D, WedgeLayout.slot(3, 5, SPACE).lateral(), EPSILON);
        assertEquals(step, WedgeLayout.slot(4, 5, SPACE).lateral(), EPSILON);
    }

    @Test
    void singleFollowerIsDirectlyBehind() {
        WedgeLayout.Slot slot = WedgeLayout.slot(0, 1, SPACE);
        assertEquals(1, slot.row());
        assertEquals(0D, slot.lateral(), EPSILON);
    }

    @Test
    void partlyFilledLastRowIsCentred() {
        double step = WedgeLayout.LATERAL_SPACING * SPACE;
        assertEquals(-step / 2D, WedgeLayout.slot(2, 4, SPACE).lateral(), EPSILON);
        assertEquals(step / 2D, WedgeLayout.slot(3, 4, SPACE).lateral(), EPSILON);
    }

    @Test
    void rowDistanceIsRowTimesPersonalSpace() {
        assertEquals(6D, WedgeLayout.rowDistance(2, SPACE), EPSILON);
    }

    @Test
    void jitterIsStableAndBounded() {
        Random random = new Random(1);
        for (int i = 0; i < 1000; i++) {
            UUID id = new UUID(random.nextLong(), random.nextLong());
            double first = WedgeLayout.jitter(id, 0);
            assertEquals(first, WedgeLayout.jitter(id, 0), 0D);
            assertTrue(Math.abs(first) <= WedgeLayout.MAX_JITTER, "out of range: " + first);
        }
    }

    @Test
    void assignPairsEachFollowerWithItsNearestSlot() {
        double[][] followers = {{10D, 0D}, {0D, 0D}};
        double[][] slots = {{0D, 1D}, {10D, 1D}};
        int[] assignment = WedgeLayout.assign(followers, slots);
        assertEquals(1, assignment[0]);
        assertEquals(0, assignment[1]);
    }
}
```

- [ ] **Step 2: Run it to see it fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac error `cannot find symbol: class WedgeLayout`.

- [ ] **Step 3: Implement**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.Arrays;
import java.util.UUID;

/**
 * The loose wedge a travelling herd forms behind its leader. Row {@code r} (1 nearest the leader)
 * holds {@code r + 1} slots, centred on the trail; a partly filled last row is centred too. Each
 * animal also gets a fixed jitter from its UUID, so the wedge does not look drilled.
 */
final class WedgeLayout {

    /** Rows are this many personal spaces apart along the trail. */
    static final double ROW_SPACING = 1.0D;
    /** Slots in a row are this many personal spaces apart. */
    static final double LATERAL_SPACING = 1.3D;
    /** Largest per-animal offset, in blocks. */
    static final double MAX_JITTER = 0.5D;

    /** A slot: its row behind the leader (1 = nearest) and its offset from the trail, positive to the left. */
    record Slot(int row, double lateral) {
    }

    private WedgeLayout() {
    }

    /** The slot with this index (0-based) in a wedge for {@code followers} animals. */
    static Slot slot(int index, int followers, double personalSpace) {
        int row = 1;
        int firstInRow = 0;
        while (index >= firstInRow + row + 1) {
            firstInRow += row + 1;
            row++;
        }
        int inRow = Math.min(row + 1, followers - firstInRow);
        int position = index - firstInRow;
        return new Slot(row, (position - (inRow - 1) / 2D) * LATERAL_SPACING * personalSpace);
    }

    /** Distance along the trail from the leader to a row. */
    static double rowDistance(int row, double personalSpace) {
        return row * ROW_SPACING * personalSpace;
    }

    /** A fixed offset in [-MAX_JITTER, MAX_JITTER] for this animal; {@code salt} picks the axis. */
    static double jitter(UUID id, int salt) {
        long hash = id.getMostSignificantBits() * 31L + id.getLeastSignificantBits() + salt * 0x9E3779B97F4A7C15L;
        hash ^= hash >>> 33;
        hash *= 0xFF51AFD7ED558CCDL;
        hash ^= hash >>> 33;
        double unit = (hash >>> 11) * 0x1.0p-53;
        return (unit * 2D - 1D) * MAX_JITTER;
    }

    /**
     * Greedy nearest assignment: repeatedly pairs the closest free follower with the closest free slot.
     * Runs only when the herd state or membership changes.
     *
     * @return for each follower index, its slot index ({@code -1} if there were fewer slots)
     */
    static int[] assign(double[][] followerXz, double[][] slotXz) {
        int[] assignment = new int[followerXz.length];
        Arrays.fill(assignment, -1);
        boolean[] slotTaken = new boolean[slotXz.length];
        int rounds = Math.min(followerXz.length, slotXz.length);
        for (int round = 0; round < rounds; round++) {
            int bestFollower = -1;
            int bestSlot = -1;
            double best = Double.MAX_VALUE;
            for (int f = 0; f < followerXz.length; f++) {
                if (assignment[f] >= 0) {
                    continue;
                }
                for (int s = 0; s < slotXz.length; s++) {
                    if (slotTaken[s]) {
                        continue;
                    }
                    double dx = followerXz[f][0] - slotXz[s][0];
                    double dz = followerXz[f][1] - slotXz[s][1];
                    double distance = dx * dx + dz * dz;
                    if (distance < best) {
                        best = distance;
                        bestFollower = f;
                        bestSlot = s;
                    }
                }
            }
            assignment[bestFollower] = bestSlot;
            slotTaken[bestSlot] = true;
        }
        return assignment;
    }
}
```

- [ ] **Step 4: Run the tests**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: `45 run, 0 failed`.

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`

---

### Task 9: `HerdArea`

**Files:**
- Create: `.../social/HerdArea.java`
- Test: `common/src/test/java/.../social/HerdAreaTest.java`

**Interfaces:**
- Produces: `HerdArea` with `SPOT_CANDIDATES = 6`; `record Point(double x, double z)`; `static Point anchor(Point leader, List<Point> followers)`; `static double radius(int herdSize, double personalSpace, double bodyWidth)`; `static double roominess(Point point, List<Point> occupied)`; `static int roomiest(List<Point> candidates, List<Point> occupied)`.

- [ ] **Step 1: Write the failing test**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HerdAreaTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void leaderWeighsAsMuchAsAThirdOfTheFollowers() {
        List<HerdArea.Point> followers = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            followers.add(new HerdArea.Point(6D, 0D));
        }
        // Weight 2 for the leader at 0, six followers at 6: (0 * 2 + 36) / 8.
        assertEquals(4.5D, HerdArea.anchor(new HerdArea.Point(0D, 0D), followers).x(), EPSILON);
    }

    @Test
    void leaderWeighsAtLeastOneAnimal() {
        List<HerdArea.Point> followers = List.of(new HerdArea.Point(4D, 0D));
        assertEquals(2D, HerdArea.anchor(new HerdArea.Point(0D, 0D), followers).x(), EPSILON);
    }

    @Test
    void radiusGrowsWithTheSquareRootOfTheHerd() {
        assertEquals(3D * Math.sqrt(10D) + 1D, HerdArea.radius(20, 3D, 2D), EPSILON);
        assertEquals(3D * Math.sqrt(2.5D) + 1D, HerdArea.radius(5, 3D, 2D), EPSILON);
    }

    @Test
    void roomiestPicksTheCandidateFarthestFromEveryone() {
        List<HerdArea.Point> occupied = List.of(new HerdArea.Point(0D, 0D), new HerdArea.Point(10D, 0D));
        List<HerdArea.Point> candidates = List.of(new HerdArea.Point(1D, 0D), new HerdArea.Point(5D, 0D), new HerdArea.Point(9D, 0D));
        assertEquals(1, HerdArea.roomiest(candidates, occupied));
    }

    @Test
    void roomiestOfNoCandidatesIsMinusOne() {
        assertEquals(-1, HerdArea.roomiest(List.of(), List.of(new HerdArea.Point(0D, 0D))));
    }

    @Test
    void withNobodyElseTheFirstCandidateWins() {
        assertEquals(0, HerdArea.roomiest(List.of(new HerdArea.Point(1D, 1D), new HerdArea.Point(2D, 2D)), List.of()));
    }
}
```

- [ ] **Step 2: Run it to see it fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac error `cannot find symbol: class HerdArea`.

- [ ] **Step 3: Implement**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.List;

/**
 * The grazing area of a herd: a weighted anchor, a radius that grows with the herd, and the choice of
 * the roomiest free spot. Pure arithmetic on horizontal points.
 */
final class HerdArea {

    /** A grazing follower samples this many spots and takes the roomiest. */
    static final int SPOT_CANDIDATES = 6;

    /** A horizontal position. */
    record Point(double x, double z) {
    }

    private HerdArea() {
    }

    /**
     * The herd's centre of gravity. The leader weighs as much as a third of the followers combined, and
     * at least as much as one animal (spec: {@code w = n / 3}, at least 1).
     */
    static Point anchor(Point leader, List<Point> followers) {
        double weight = Math.max(1D, followers.size() / 3D);
        double x = leader.x() * weight;
        double z = leader.z() * weight;
        for (Point follower : followers) {
            x += follower.x();
            z += follower.z();
        }
        double total = weight + followers.size();
        return new Point(x / total, z / total);
    }

    /** Herd radius: room for {@code herdSize} bodies at this personal space, plus half a body. */
    static double radius(int herdSize, double personalSpace, double bodyWidth) {
        return personalSpace * Math.sqrt(herdSize / 2D) + bodyWidth / 2D;
    }

    /** Distance from the point to the nearest occupied point; {@link Double#MAX_VALUE} if none. */
    static double roominess(Point point, List<Point> occupied) {
        double nearest = Double.MAX_VALUE;
        for (Point other : occupied) {
            double dx = point.x() - other.x();
            double dz = point.z() - other.z();
            nearest = Math.min(nearest, dx * dx + dz * dz);
        }
        return nearest == Double.MAX_VALUE ? nearest : Math.sqrt(nearest);
    }

    /** Index of the roomiest candidate, the first on a tie, or {@code -1} if there is none. */
    static int roomiest(List<Point> candidates, List<Point> occupied) {
        int best = -1;
        double bestRoom = -1D;
        for (int i = 0; i < candidates.size(); i++) {
            double room = roominess(candidates.get(i), occupied);
            if (room > bestRoom) {
                bestRoom = room;
                best = i;
            }
        }
        return best;
    }
}
```

- [ ] **Step 4: Run the tests**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: `51 run, 0 failed`.

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`

---

### Task 10: `HerdSteering` and `HerdSpacing.away`

**Files:**
- Create: `.../social/HerdSteering.java`
- Modify: `.../social/HerdSpacing.java` (add `away`)
- Test: `common/src/test/java/.../social/HerdSteeringTest.java`
- Modify test: `common/src/test/java/.../social/HerdSpacingTest.java` (add `away` tests)

**Interfaces:**
- Consumes: `HerdSpacing.steer(double, double, List<Neighbour>)`, `HerdSpacing.Neighbour`.
- Produces: `HerdSteering` with `ALIGNMENT_WEIGHT = 0.3`, `MAX_TURN_DEGREES = 20F`; `static Vec3 direction(double seekX, double seekZ, List<HerdSpacing.Neighbour> neighbours, double headingX, double headingZ)`; `static float yawOf(double x, double z)`; `static Vec3 directionOf(float yaw)`; `static Vec3 limitTurn(float currentYaw, Vec3 desired, float maxDegrees)`. `HerdSpacing.away(List<Neighbour>)` returns a unit `Vec3`.

- [ ] **Step 1: Write the failing tests**

`HerdSteeringTest.java`:

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdSteeringTest {

    /** Mth.sin and Mth.cos use a lookup table, so angles match to about 1e-3. */
    private static final double TABLE_EPSILON = 1.0E-3D;
    private static final double EPSILON = 1.0E-9D;

    @Test
    void withoutHeadingItIsSeekPlusSeparation() {
        List<HerdSpacing.Neighbour> neighbours = List.of(new HerdSpacing.Neighbour(1D, 1D, 3D));
        Vec3 expected = HerdSpacing.steer(1D, 0D, neighbours);
        Vec3 actual = HerdSteering.direction(1D, 0D, neighbours, 0D, 0D);
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }

    @Test
    void headingPullsTheStepTowardTheHerdDirection() {
        Vec3 step = HerdSteering.direction(1D, 0D, List.of(), 0D, 1D);
        assertTrue(step.z > 0D, "should lean toward +z, was " + step);
        assertTrue(step.x > step.z, "seek should still dominate, was " + step);
        assertEquals(1D, step.length(), EPSILON);
    }

    @Test
    void zeroSeekGivesZero() {
        assertEquals(Vec3.ZERO, HerdSteering.direction(0D, 0D, List.of(), 0D, 1D));
    }

    @Test
    void yawAndDirectionRoundTrip() {
        Vec3 east = HerdSteering.directionOf(HerdSteering.yawOf(1D, 0D));
        assertEquals(1D, east.x, TABLE_EPSILON);
        assertEquals(0D, east.z, TABLE_EPSILON);
        Vec3 south = HerdSteering.directionOf(HerdSteering.yawOf(0D, 1D));
        assertEquals(0D, south.x, TABLE_EPSILON);
        assertEquals(1D, south.z, TABLE_EPSILON);
    }

    @Test
    void smallTurnsPassUnchanged() {
        Vec3 desired = HerdSteering.directionOf(10F);
        Vec3 limited = HerdSteering.limitTurn(0F, desired, 20F);
        assertEquals(desired.x, limited.x, EPSILON);
        assertEquals(desired.z, limited.z, EPSILON);
    }

    @Test
    void largeTurnsAreClampedToTheLimit() {
        // Facing +z (yaw 0), wanting +x (yaw -90): turn only 20 degrees toward it.
        Vec3 limited = HerdSteering.limitTurn(0F, new Vec3(1D, 0D, 0D), 20F);
        assertEquals(Math.sin(Math.toRadians(20D)), limited.x, TABLE_EPSILON);
        assertEquals(Math.cos(Math.toRadians(20D)), limited.z, TABLE_EPSILON);
    }
}
```

Add to `HerdSpacingTest.java`:

```java
    @Test
    void awayFromOneNeighbourIsStraightBack() {
        Vec3 away = HerdSpacing.away(List.of(new HerdSpacing.Neighbour(1D, 0D, 3D)));
        assertEquals(-1D, away.x, EPSILON);
        assertEquals(0D, away.z, EPSILON);
    }

    @Test
    void awayFromBalancedNeighboursIsSideways() {
        Vec3 away = HerdSpacing.away(List.of(new HerdSpacing.Neighbour(1D, 0D, 3D), new HerdSpacing.Neighbour(-1D, 0D, 3D)));
        assertEquals(0D, away.x, EPSILON);
        assertEquals(1D, Math.abs(away.z), EPSILON);
    }
```

- [ ] **Step 2: Run them to see them fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors for `HerdSteering` and `HerdSpacing.away`.

- [ ] **Step 3: Implement `HerdSteering`**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The flocking step of a follower: seek toward its target plus separation ({@link HerdSpacing#steer}),
 * plus a light pull toward the herd's heading while travelling, then a turn limit so rotation does not
 * jitter on clients. Pure arithmetic.
 */
final class HerdSteering {

    /** Pull toward the herd heading, against seek (1). */
    static final double ALIGNMENT_WEIGHT = 0.3D;
    /** Largest change of facing per tick. */
    static final float MAX_TURN_DEGREES = 20F;

    private HerdSteering() {
    }

    /**
     * Seek plus separation, then alignment. A zero heading means no alignment.
     *
     * @return a unit direction with y = 0, or {@link Vec3#ZERO} if the seek direction is zero
     */
    static Vec3 direction(double seekX, double seekZ, List<HerdSpacing.Neighbour> neighbours, double headingX, double headingZ) {
        Vec3 step = HerdSpacing.steer(seekX, seekZ, neighbours);
        if (step.lengthSqr() < 1.0E-12D) {
            return Vec3.ZERO;
        }
        double headingLength = Math.sqrt(headingX * headingX + headingZ * headingZ);
        if (headingLength < 1.0E-6D) {
            return step;
        }
        double x = step.x + ALIGNMENT_WEIGHT * headingX / headingLength;
        double z = step.z + ALIGNMENT_WEIGHT * headingZ / headingLength;
        double length = Math.sqrt(x * x + z * z);
        return length < 1.0E-6D ? step : new Vec3(x / length, 0D, z / length);
    }

    /** Minecraft yaw, in degrees, of a horizontal direction; the same formula as {@code MoveControl}. */
    static float yawOf(double x, double z) {
        return (float) Math.toDegrees(Mth.atan2(z, x)) - 90F;
    }

    /** Unit direction for a Minecraft yaw in degrees. */
    static Vec3 directionOf(float yaw) {
        float radians = (float) Math.toRadians(yaw);
        return new Vec3(-Mth.sin(radians), 0D, Mth.cos(radians));
    }

    /** Turns from the current yaw toward the desired direction by at most {@code maxDegrees}. */
    static Vec3 limitTurn(float currentYaw, Vec3 desired, float maxDegrees) {
        if (desired.lengthSqr() < 1.0E-12D) {
            return Vec3.ZERO;
        }
        float delta = Mth.wrapDegrees(yawOf(desired.x, desired.z) - currentYaw);
        if (Math.abs(delta) <= maxDegrees) {
            return desired;
        }
        return directionOf(currentYaw + Math.copySign(maxDegrees, delta));
    }
}
```

- [ ] **Step 4: Add `away` to `HerdSpacing`**

```java
    /**
     * Unit direction straight away from crowding neighbours, closer ones counting more. If they cancel
     * out (crowded evenly on two sides), a sideways step from the first one.
     */
    static Vec3 away(List<Neighbour> neighbours) {
        double x = 0D;
        double z = 0D;
        for (Neighbour neighbour : neighbours) {
            double distance = Math.sqrt(neighbour.dx() * neighbour.dx() + neighbour.dz() * neighbour.dz());
            if (distance < 1.0E-6D || distance >= neighbour.space()) {
                continue;
            }
            double closeness = (neighbour.space() - distance) / neighbour.space();
            x -= neighbour.dx() / distance * closeness;
            z -= neighbour.dz() / distance * closeness;
        }
        double length = Math.sqrt(x * x + z * z);
        if (length > 1.0E-6D) {
            return new Vec3(x / length, 0D, z / length);
        }
        Neighbour first = neighbours.get(0);
        double firstLength = Math.max(1.0E-6D, Math.sqrt(first.dx() * first.dx() + first.dz() * first.dz()));
        return new Vec3(-first.dz() / firstLength, 0D, first.dx() / firstLength);
    }
```

Callers pass a non-empty list (`crowding` results only).

- [ ] **Step 5: Run the tests**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: `59 run, 0 failed`.

- [ ] **Step 6: Checkpoint (no commit)**

Run: `git status --short`

---

### Task 11: `FallbackBudget`

**Files:**
- Create: `.../social/FallbackBudget.java`
- Test: `common/src/test/java/.../social/FallbackBudgetTest.java`

**Interfaces:**
- Produces: `FallbackBudget` with `PER_TICK = 2`, `COOLDOWN_TICKS = 60`; `boolean tryAcquire(UUID member, long gameTime)`; `void forget(UUID member)`; `int mostGrantedInOneTick()`.

- [ ] **Step 1: Write the failing test**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FallbackBudgetTest {

    private static final UUID A = new UUID(0L, 1L);
    private static final UUID B = new UUID(0L, 2L);
    private static final UUID C = new UUID(0L, 3L);
    private static final UUID D = new UUID(0L, 4L);
    private static final UUID E = new UUID(0L, 5L);
    private static final UUID F = new UUID(0L, 6L);

    @Test
    void grantsAtMostTwoPerTick() {
        FallbackBudget budget = new FallbackBudget();
        assertTrue(budget.tryAcquire(A, 10));
        assertTrue(budget.tryAcquire(B, 10));
        assertFalse(budget.tryAcquire(C, 10));
        assertEquals(2, budget.mostGrantedInOneTick());
    }

    @Test
    void waitingMemberIsServedNextTick() {
        FallbackBudget budget = new FallbackBudget();
        budget.tryAcquire(A, 10);
        budget.tryAcquire(B, 10);
        budget.tryAcquire(C, 10);
        assertFalse(budget.tryAcquire(A, 11), "A is on cooldown");
        assertTrue(budget.tryAcquire(C, 11));
    }

    @Test
    void cooldownBlocksAnotherGrantFor60Ticks() {
        FallbackBudget budget = new FallbackBudget();
        assertTrue(budget.tryAcquire(A, 0));
        assertFalse(budget.tryAcquire(A, 59));
        assertTrue(budget.tryAcquire(A, 60));
    }

    @Test
    void memberThatStoppedAskingDoesNotBlockOthers() {
        FallbackBudget budget = new FallbackBudget();
        budget.tryAcquire(A, 10);
        budget.tryAcquire(B, 10);
        budget.tryAcquire(C, 10);
        // C never asks again; two ticks later D, E and F ask.
        assertTrue(budget.tryAcquire(D, 12));
        assertTrue(budget.tryAcquire(E, 12));
        assertFalse(budget.tryAcquire(F, 12));
    }

    @Test
    void forgottenMemberLosesItsCooldown() {
        FallbackBudget budget = new FallbackBudget();
        assertTrue(budget.tryAcquire(A, 0));
        budget.forget(A);
        assertTrue(budget.tryAcquire(A, 1));
    }
}
```

- [ ] **Step 2: Run it to see it fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac error `cannot find symbol: class FallbackBudget`.

- [ ] **Step 3: Implement**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Caps the fallback paths one herd starts: at most {@link #PER_TICK} per tick, first come first served,
 * and at most one per member every {@link #COOLDOWN_TICKS}. A member that wants a path asks every tick;
 * a member that stops asking drops out of the queue. Server thread only.
 */
final class FallbackBudget {

    static final int PER_TICK = 2;
    static final int COOLDOWN_TICKS = 60;

    private final ArrayDeque<UUID> queue = new ArrayDeque<>();
    private final Map<UUID, Long> lastRequest = new HashMap<>();
    private final Map<UUID, Long> lastGranted = new HashMap<>();
    private long tick = Long.MIN_VALUE;
    private int grantedThisTick;
    private int mostGrantedInOneTick;

    /** {@code true} if the member may start a fallback path now. */
    boolean tryAcquire(UUID member, long gameTime) {
        if (gameTime != this.tick) {
            this.tick = gameTime;
            this.grantedThisTick = 0;
            this.queue.removeIf(id -> gameTime - this.lastRequest.getOrDefault(id, Long.MIN_VALUE) > 1L);
        }
        Long granted = this.lastGranted.get(member);
        if (granted != null && gameTime - granted < COOLDOWN_TICKS) {
            return false;
        }
        this.lastRequest.put(member, gameTime);
        if (!this.queue.contains(member)) {
            this.queue.addLast(member);
        }
        int position = 0;
        for (UUID queued : this.queue) {
            if (queued.equals(member)) {
                break;
            }
            position++;
        }
        if (position >= PER_TICK - this.grantedThisTick) {
            return false;
        }
        this.queue.remove(member);
        this.lastGranted.put(member, gameTime);
        this.grantedThisTick++;
        this.mostGrantedInOneTick = Math.max(this.mostGrantedInOneTick, this.grantedThisTick);
        return true;
    }

    /** Drops a member that left the herd. */
    void forget(UUID member) {
        this.queue.remove(member);
        this.lastRequest.remove(member);
        this.lastGranted.remove(member);
    }

    /** The most grants in a single tick so far; never above {@link #PER_TICK}. For GameTests. */
    int mostGrantedInOneTick() {
        return this.mostGrantedInOneTick;
    }
}
```

- [ ] **Step 4: Run the tests**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: `64 run, 0 failed`.

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`

---

### Task 12: `StepCheck`

Reads blocks, so it has no unit test. The GameTests in Task 16 exercise it.

**Files:**
- Create: `.../social/StepCheck.java`

**Interfaces:**
- Consumes: `LeaderTrail.TrailPoint`.
- Produces: `StepCheck` with `MAX_DROP = 3`; `boolean canEnter(ServerLevel level, Mob mob, double x, double z)` (cached per tick); `static boolean canStand(ServerLevel level, Mob mob, double x, int feetY, double z)`; `static float openWidth(ServerLevel level, Mob mob, LeaderTrail.TrailPoint point, boolean left, int maxBlocks)`.

- [ ] **Step 1: Implement**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.dumbcode.projectnublar.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A simple passability check for herd steering: does the body fit, is there standable ground within a
 * safe drop, and is there no fluid or hazard. Much cheaper than path-node evaluation. One instance per
 * herd: {@link #canEnter} results are cached for the current tick, because followers cross the same
 * ground. Server thread only.
 */
final class StepCheck {

    /** Largest drop a herd member steps down, the vanilla safe fall for mobs. */
    static final int MAX_DROP = 3;

    private final Long2BooleanOpenHashMap cache = new Long2BooleanOpenHashMap();
    private long cacheTick = Long.MIN_VALUE;

    /** Can the mob's body stand with its centre at (x, z), at its feet height or one block up? Cached per tick. */
    boolean canEnter(ServerLevel level, Mob mob, double x, double z) {
        long now = level.getGameTime();
        if (now != this.cacheTick) {
            this.cache.clear();
            this.cacheTick = now;
        }
        int feetY = Mth.floor(mob.getY());
        long key = BlockPos.asLong(Mth.floor(x), feetY, Mth.floor(z)) * 31L + Mth.ceil(mob.getBbWidth() * 2F);
        if (this.cache.containsKey(key)) {
            return this.cache.get(key);
        }
        boolean result = canStand(level, mob, x, feetY, z);
        this.cache.put(key, result);
        return result;
    }

    /** The uncached check, at a given feet height (or one block up, for the step height of 1). */
    static boolean canStand(ServerLevel level, Mob mob, double x, int feetY, double z) {
        double half = mob.getBbWidth() / 2D;
        int minX = Mth.floor(x - half);
        int maxX = Mth.floor(x + half - 1.0E-3D);
        int minZ = Mth.floor(z - half);
        int maxZ = Mth.floor(z + half - 1.0E-3D);
        int height = Mth.ceil(mob.getBbHeight());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int base = feetY; base <= feetY + 1; base++) {
            if (bodyFits(level, pos, minX, maxX, minZ, maxZ, base, height)) {
                return hasGround(level, pos, Mth.floor(x), base, Mth.floor(z));
            }
        }
        return false;
    }

    /**
     * Clear standable columns beside a trail point, counted outward up to {@code maxBlocks}, on the left
     * or right of the travel direction. One block wide, the body's height.
     */
    static float openWidth(ServerLevel level, Mob mob, LeaderTrail.TrailPoint point, boolean left, int maxBlocks) {
        double sideX = left ? -point.dirZ() : point.dirZ();
        double sideZ = left ? point.dirX() : -point.dirX();
        int feetY = Mth.floor(point.y());
        int height = Mth.ceil(mob.getBbHeight());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int step = 1; step <= maxBlocks; step++) {
            int x = Mth.floor(point.x() + sideX * step);
            int z = Mth.floor(point.z() + sideZ * step);
            boolean clear = false;
            for (int base = feetY; base <= feetY + 1 && !clear; base++) {
                clear = bodyFits(level, pos, x, x, z, z, base, height) && hasGround(level, pos, x, base, z);
            }
            if (!clear) {
                return step - 1;
            }
        }
        return maxBlocks;
    }

    private static boolean bodyFits(ServerLevel level, BlockPos.MutableBlockPos pos, int minX, int maxX, int minZ, int maxZ, int base, int height) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = base; y < base + height; y++) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (isHazard(state) || !state.getCollisionShape(level, pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** Solid ground under the centre column within {@link #MAX_DROP}, with no fluid or hazard on the way. */
    private static boolean hasGround(ServerLevel level, BlockPos.MutableBlockPos pos, int x, int base, int z) {
        for (int drop = 1; drop <= MAX_DROP + 1; drop++) {
            BlockState state = level.getBlockState(pos.set(x, base - drop, z));
            if (isHazard(state)) {
                return false;
            }
            if (!state.getCollisionShape(level, pos).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHazard(BlockState state) {
        return !state.getFluidState().isEmpty()
                || state.is(BlockTags.FIRE) || state.is(Blocks.CACTUS) || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.POWDER_SNOW)
                || state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS) || state.is(BlockTags.FENCE_GATES)
                || state.is(BlockInit.ELECTRIC_FENCE.get())
                || state.is(BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get())
                || state.is(BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get());
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :common:compileJava`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Checkpoint (no commit)**

Run: `git status --short`

---

### Task 13: Herd state in `DinoGroup`, and `HerdLeading`

**Files:**
- Create: `.../social/HerdState.java`
- Create: `.../social/HerdLeading.java`
- Modify: `.../social/DinoGroup.java`
- Test: `common/src/test/java/.../social/DinoGroupTest.java`

**Interfaces:**
- Consumes: `LeaderTrail`, `StepCheck`, `FallbackBudget` (Tasks 7, 11, 12).
- Produces: `enum HerdState { TRAVEL, GRAZE }`. `DinoGroup` gains `GRAZE_AFTER_TICKS = 40`, `HerdState state()`, `void updateState(boolean leaderTravelling, long gameTime)`, `LeaderTrail trail()`, `StepCheck stepCheck()`, `FallbackBudget fallbacks()`, `int slotOf(UUID member)` (−1 if none), `boolean slotsDirty()`, `void markSlotsDirty()`, `void setSlots(Map<UUID, Integer> assignment)`, `public int mostFallbacksInOneTick()`. `HerdLeading` gains `TRAVEL_DISTANCE = 8` and `public static void tick(Dinosaur dinosaur)`.

- [ ] **Step 1: Write the failing test**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DinoGroupTest {

    private final Map<UUID, Float> present = new HashMap<>();
    private final DinoGroup.Roster roster = new DinoGroup.Roster() {
        @Override
        public boolean isPresent(UUID member) {
            return present.containsKey(member);
        }

        @Override
        public float dominance(UUID member) {
            return present.get(member);
        }
    };

    private UUID animal(long id, float dominance) {
        UUID uuid = new UUID(0L, id);
        this.present.put(uuid, dominance);
        return uuid;
    }

    @Test
    void newGroupGrazes() {
        assertSame(HerdState.GRAZE, new DinoGroup(5).state());
    }

    @Test
    void travelStartsAtOnceAndGrazingWaits() {
        DinoGroup group = new DinoGroup(5);
        group.updateState(true, 100L);
        assertSame(HerdState.TRAVEL, group.state());
        group.updateState(false, 100L + DinoGroup.GRAZE_AFTER_TICKS);
        assertSame(HerdState.TRAVEL, group.state(), "not yet: exactly the wait");
        group.updateState(false, 101L + DinoGroup.GRAZE_AFTER_TICKS);
        assertSame(HerdState.GRAZE, group.state());
    }

    @Test
    void enteringTravelMarksSlotsDirty() {
        DinoGroup group = new DinoGroup(5);
        group.setSlots(Map.of());
        assertFalse(group.slotsDirty());
        group.updateState(true, 1L);
        assertTrue(group.slotsDirty());
    }

    @Test
    void membershipChangeMarksSlotsDirtyAndForgetsTheSlot() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID follower = animal(2, 0.5F);
        group.add(leader, this.roster);
        group.add(follower, this.roster);
        group.setSlots(Map.of(follower, 0));
        assertEquals(0, group.slotOf(follower));
        group.remove(follower);
        assertTrue(group.slotsDirty());
        assertEquals(-1, group.slotOf(follower));
        group.setSlots(Map.of());
        group.add(animal(3, 0.4F), this.roster);
        assertTrue(group.slotsDirty());
    }

    @Test
    void leaderChangeMarksSlotsDirty() {
        DinoGroup group = new DinoGroup(5);
        UUID leader = animal(1, 0.9F);
        UUID next = animal(2, 0.8F);
        group.add(leader, this.roster);
        group.add(next, this.roster);
        group.setSlots(Map.of(next, 0));
        this.present.remove(leader);
        assertEquals(next, group.leader(this.roster));
        assertTrue(group.slotsDirty());
    }
}
```

- [ ] **Step 2: Run it to see it fail**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: javac errors for `HerdState` and the new `DinoGroup` methods.

- [ ] **Step 3: Create `HerdState`**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/** What a herd is doing as a whole. Regrouping is per follower, not a herd state. */
public enum HerdState {
    /** The leader walks to a destination; followers hold wedge slots along its trail. */
    TRAVEL,
    /** The leader stands; followers graze around the weighted anchor. */
    GRAZE
}
```

- [ ] **Step 4: Extend `DinoGroup`**

Add imports `java.util.HashMap` and `java.util.Map`. Add these fields after `private @Nullable UUID leader;`:

```java
    /** After this many ticks without travelling, a herd goes back to grazing. */
    static final int GRAZE_AFTER_TICKS = 40;

    private HerdState state = HerdState.GRAZE;
    private long lastTravelTick = Long.MIN_VALUE;
    private final LeaderTrail trail = new LeaderTrail();
    private final Map<UUID, Integer> slots = new HashMap<>();
    private boolean slotsDirty = true;
    private final StepCheck stepCheck = new StepCheck();
    private final FallbackBudget fallbacks = new FallbackBudget();
```

Add these methods:

```java
    HerdState state() {
        return this.state;
    }

    /** Called from the leader's tick: travel starts at once, grazing after {@link #GRAZE_AFTER_TICKS} without travel. */
    void updateState(boolean leaderTravelling, long gameTime) {
        if (leaderTravelling) {
            this.lastTravelTick = gameTime;
            if (this.state != HerdState.TRAVEL) {
                this.state = HerdState.TRAVEL;
                this.slotsDirty = true;
            }
        } else if (this.state == HerdState.TRAVEL && gameTime - this.lastTravelTick > GRAZE_AFTER_TICKS) {
            this.state = HerdState.GRAZE;
        }
    }

    LeaderTrail trail() {
        return this.trail;
    }

    StepCheck stepCheck() {
        return this.stepCheck;
    }

    FallbackBudget fallbacks() {
        return this.fallbacks;
    }

    /** The member's wedge slot index, or -1 if it has none. */
    int slotOf(UUID member) {
        return this.slots.getOrDefault(member, -1);
    }

    boolean slotsDirty() {
        return this.slotsDirty;
    }

    void markSlotsDirty() {
        this.slotsDirty = true;
    }

    void setSlots(Map<UUID, Integer> assignment) {
        this.slots.clear();
        this.slots.putAll(assignment);
        this.slotsDirty = false;
    }

    /** The most fallback paths this herd started in one tick. For GameTests. */
    public int mostFallbacksInOneTick() {
        return this.fallbacks.mostGrantedInOneTick();
    }
```

Mark slots dirty on every membership or leader change:
- In `add`, add `this.slotsDirty = true;` as the first line.
- In `remove`, add `this.slotsDirty = true; this.slots.remove(member); this.fallbacks.forget(member);` after `this.members.remove(member);`.
- In `add` and `challenge`, after each `this.leader = ...` assignment, add `this.slotsDirty = true;`.
- In `electLeader`, replace `this.leader = best;` with:

```java
        if (best == null ? this.leader != null : !best.equals(this.leader)) {
            this.slotsDirty = true;
        }
        this.leader = best;
```

- [ ] **Step 5: Create `HerdLeading`**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.world.level.pathfinder.Path;

/**
 * The leader's part of herd movement, called from every dinosaur's server tick: it returns at once for
 * non-leaders. Every {@link LeaderTrail#RECORD_INTERVAL} ticks the leader records its position on the
 * trail and reports whether it is travelling.
 */
public final class HerdLeading {

    /** The herd travels when the leader's path ends at least this far away. */
    static final double TRAVEL_DISTANCE = 8D;

    private HerdLeading() {
    }

    public static void tick(Dinosaur dinosaur) {
        if (dinosaur.tickCount % LeaderTrail.RECORD_INTERVAL != 0) {
            return;
        }
        DinoGroup group = GroupManager.groupOf(dinosaur);
        if (group == null || !group.isLeader(dinosaur.getUUID())) {
            return;
        }
        group.trail().record(dinosaur.getX(), dinosaur.getY(), dinosaur.getZ());
        Path path = dinosaur.getNavigation().getPath();
        boolean travelling = path != null && !path.isDone()
                && path.getTarget().distToCenterSqr(dinosaur.position()) > TRAVEL_DISTANCE * TRAVEL_DISTANCE;
        group.updateState(travelling, dinosaur.level().getGameTime());
    }
}
```

- [ ] **Step 6: Run the tests**

Run: `./gradlew :common:compileJava` then `sh tools/junit-standin/run.sh`
Expected: `69 run, 0 failed` (all earlier `GroupManagerTest` cases still pass).

- [ ] **Step 7: Checkpoint (no commit)**

Run: `git status --short`

---

### Task 14: `HerdMoveControl`: steering mode, step check, step aside

**Files:**
- Modify (replace the whole file): `.../social/HerdMoveControl.java`

**Interfaces:**
- Consumes: `HerdSteering`, `HerdSpacing.crowding/away/applies`, `DinoGroup.stepCheck()`, `HerdTimings.record`.
- Produces: `public void steerTo(double x, double y, double z, double speed, double headingX, double headingZ)`, `public void stopSteering()`, `public boolean isSteering()`, `public @Nullable Vec3 steeringTarget()`, `public int stepFailures()`; constants `ARRIVED = 1.0`, `STEP_ASIDE_AFTER_TICKS = 5`.

- [ ] **Step 1: Replace `HerdMoveControl`**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Vanilla movement plus the herd's per-tick layer:
 * <ul>
 *   <li><b>Steering mode</b> ({@link #steerTo}): a follower moves toward its formation or grazing target
 *       by seek, separation and alignment ({@link HerdSteering}), with a turn limit and a simple step
 *       check ({@link StepCheck}) whenever its body is about to enter a new block column. No path.</li>
 *   <li><b>Path mode</b>: navigation sets the wanted position each tick (fallback paths, the leader, needs);
 *       steps bend away from crowding herd members.</li>
 *   <li><b>Standing</b>: a herd member crowded for more than {@link #STEP_ASIDE_AFTER_TICKS} ticks takes
 *       one short step straight away.</li>
 * </ul>
 * Navigation always wins: a running path ends steering mode. All herd work is timed for the budget.
 */
public class HerdMoveControl extends MoveControl {

    /** Steering stops this close to its target. */
    static final double ARRIVED = 1.0D;
    /** A standing member steps aside after this many crowded ticks. */
    static final int STEP_ASIDE_AFTER_TICKS = 5;
    /** The wanted position sits at most this far ahead along the steered direction. */
    private static final double LOOK_AHEAD = 4.0D;
    private static final double STEP_ASIDE_DISTANCE = 1.5D;
    private static final double STEP_ASIDE_SPEED = 0.6D;

    private final Dinosaur dinosaur;
    private boolean steering;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double targetSpeed;
    private double headingX;
    private double headingZ;
    private long checkedColumn = Long.MIN_VALUE;
    private boolean columnClear = true;
    private int stepFailures;
    private int crowdedTicks;

    public HerdMoveControl(Dinosaur dinosaur) {
        super(dinosaur);
        this.dinosaur = dinosaur;
    }

    /** Steer toward a point until it is reached or {@link #stopSteering} is called. A zero heading means no alignment. */
    public void steerTo(double x, double y, double z, double speed, double headingX, double headingZ) {
        this.steering = true;
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.targetSpeed = speed;
        this.headingX = headingX;
        this.headingZ = headingZ;
    }

    public void stopSteering() {
        if (this.steering) {
            this.steering = false;
            this.operation = Operation.WAIT;
        }
    }

    public boolean isSteering() {
        return this.steering;
    }

    /** Where steering is heading, or {@code null}. Allocates; for occasional callers (spot picking). */
    public @Nullable Vec3 steeringTarget() {
        return this.steering ? new Vec3(this.targetX, this.targetY, this.targetZ) : null;
    }

    /** Consecutive failed step checks while steering. */
    public int stepFailures() {
        return this.stepFailures;
    }

    @Override
    public void tick() {
        ServerLevel level = (ServerLevel) this.mob.level();
        long start = System.nanoTime();
        level.getProfiler().push("herdSteering");
        if (this.steering && this.mob.getNavigation().isInProgress()) {
            this.steering = false;
        }
        if (this.steering) {
            steer(level);
        } else if (this.operation == Operation.MOVE_TO) {
            bendAwayFromHerd(level);
        } else {
            stepAsideIfCrowded(level);
        }
        level.getProfiler().pop();
        HerdTimings.record(level.getGameTime(), System.nanoTime() - start);
        super.tick();
    }

    private void steer(ServerLevel level) {
        DinoGroup group = GroupManager.groupOf(this.dinosaur);
        double dx = this.targetX - this.mob.getX();
        double dz = this.targetZ - this.mob.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (group == null || distance < ARRIVED) {
            this.steering = false;
            this.operation = Operation.WAIT;
            return;
        }
        List<HerdSpacing.Neighbour> crowding = HerdSpacing.applies(this.dinosaur)
                ? HerdSpacing.crowding(this.dinosaur, group, level) : List.of();
        Vec3 direction = HerdSteering.direction(dx, dz, crowding, this.headingX, this.headingZ);
        direction = HerdSteering.limitTurn(this.mob.getYRot(), direction, HerdSteering.MAX_TURN_DEGREES);

        double probe = this.mob.getBbWidth() / 2D + 0.5D;
        double probeX = this.mob.getX() + direction.x * probe;
        double probeZ = this.mob.getZ() + direction.z * probe;
        long column = BlockPos.asLong(Mth.floor(probeX), 0, Mth.floor(probeZ));
        if (column != this.checkedColumn) {
            this.checkedColumn = column;
            this.columnClear = group.stepCheck().canEnter(level, this.mob, probeX, probeZ);
            this.stepFailures = this.columnClear ? 0 : this.stepFailures + 1;
        }
        if (!this.columnClear) {
            // Drop separation and alignment: head straight for the target. FollowFormation has already
            // moved the target to a safe point (the trail's centre line) when step checks fail.
            direction = new Vec3(dx / distance, 0D, dz / distance);
        }
        double ahead = Math.min(distance, LOOK_AHEAD);
        this.setWantedPosition(this.mob.getX() + direction.x * ahead, this.targetY,
                this.mob.getZ() + direction.z * ahead, this.targetSpeed);
    }

    /** Path mode: bend the step that navigation set away from crowding herd members. */
    private void bendAwayFromHerd(ServerLevel level) {
        if (!HerdSpacing.applies(this.dinosaur)) {
            return;
        }
        DinoGroup group = GroupManager.groupOf(this.dinosaur);
        if (group == null) {
            return;
        }
        List<HerdSpacing.Neighbour> crowding = HerdSpacing.crowding(this.dinosaur, group, level);
        if (crowding.isEmpty()) {
            return;
        }
        double dx = this.wantedX - this.mob.getX();
        double dz = this.wantedZ - this.mob.getZ();
        Vec3 step = HerdSpacing.steer(dx, dz, crowding);
        double distance = Math.sqrt(dx * dx + dz * dz);
        this.wantedX = this.mob.getX() + step.x * distance;
        this.wantedZ = this.mob.getZ() + step.z * distance;
    }

    /** Standing: after a few crowded ticks, one short step straight away, if the ground there is safe. */
    private void stepAsideIfCrowded(ServerLevel level) {
        DinoGroup group = HerdSpacing.applies(this.dinosaur) ? GroupManager.groupOf(this.dinosaur) : null;
        if (group == null) {
            this.crowdedTicks = 0;
            return;
        }
        List<HerdSpacing.Neighbour> crowding = HerdSpacing.crowding(this.dinosaur, group, level);
        if (crowding.isEmpty()) {
            this.crowdedTicks = 0;
            return;
        }
        if (++this.crowdedTicks <= STEP_ASIDE_AFTER_TICKS) {
            return;
        }
        this.crowdedTicks = 0;
        Vec3 away = HerdSpacing.away(crowding);
        double x = this.mob.getX() + away.x * STEP_ASIDE_DISTANCE;
        double z = this.mob.getZ() + away.z * STEP_ASIDE_DISTANCE;
        if (group.stepCheck().canEnter(level, this.mob, x, z)) {
            this.setWantedPosition(x, this.mob.getY(), z, STEP_ASIDE_SPEED);
        }
    }
}
```

- [ ] **Step 2: Remove the baseline timing in `FollowGroupLeader`**

Steering is now timed in `HerdMoveControl`. `FollowGroupLeader` is deleted in Task 15, so leave its timing until then.

- [ ] **Step 3: Compile and run the tests**

Run: `./gradlew :forge:compileJava`, then `./gradlew :common:compileJava` and `sh tools/junit-standin/run.sh`
Expected: `BUILD SUCCESSFUL`; `69 run, 0 failed`.

- [ ] **Step 4: Checkpoint (no commit)**

Run: `git status --short`

---

### Task 15: `FollowFormation` and wiring

**Files:**
- Create: `.../social/FollowFormation.java`
- Delete: `.../social/FollowGroupLeader.java`
- Modify: `common/src/main/java/net/dumbcode/projectnublar/entity/dinosaur/Dinosaur.java` (core tasks; `customServerAiStep`)

**Interfaces:**
- Consumes: everything from Tasks 7–14; `GroupManager.groupOf/leaderOf/presentMembers`; `HerdSpacing.personalSpace`.
- Produces: `public class FollowFormation<E extends Dinosaur> extends ExtendedBehaviour<E>` with a no-argument constructor.

- [ ] **Step 1: Create `FollowFormation`**

```java
package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * How a follower moves, per herd state (spec: layered design). Each tick it picks a target point and
 * hands it to {@link HerdMoveControl} for steering:
 * <ul>
 *   <li><b>Travel</b>: its wedge slot on the leader's trail ({@link WedgeLayout}, {@link LeaderTrail}),
 *       squeezed toward the trail where the ground beside it is narrow.</li>
 *   <li><b>Graze</b>: every few seconds, the roomiest of a few free spots inside the herd area around
 *       the weighted anchor ({@link HerdArea}).</li>
 *   <li><b>Regroup or stuck</b>: a fallback vanilla path toward the target, within the herd's
 *       {@link FallbackBudget}. Setting the walk target ends this behaviour until the path is done.</li>
 * </ul>
 * Followers only: the leader moves by its own path. Each brain gets its own instance (task lists are
 * built per entity), so the fields below are per animal.
 */
public class FollowFormation<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));

    static final double TRAVEL_SPEED = 1.0D;
    static final double CATCH_UP_SPEED = 1.2D;
    static final double GRAZE_SPEED = 0.6D;
    static final int GRAZE_PAUSE_MIN_TICKS = 60;
    static final int GRAZE_PAUSE_SPREAD_TICKS = 100;
    /** A follower farther than this many herd radii from its target regroups by fallback path. */
    static final double REGROUP_FACTOR = 1.5D;
    /** Ticks without getting closer before a follower counts as stuck (only when not already near). */
    static final int STALL_TICKS = 40;
    static final double NEAR_ENOUGH = 2.5D;
    static final int MAX_STEP_FAILURES = 3;
    static final double FALLBACK_RANGE = 16D;
    static final int FALLBACK_CLOSE_ENOUGH = 2;
    static final int SIDE_SAMPLE_BLOCKS = 6;
    static final int MAX_HEIGHT_DIFFERENCE = 4;

    private @Nullable Vec3 grazeSpot;
    private long nextGrazeMove;
    private double bestDistance = Double.MAX_VALUE;
    private int stalledTicks;

    public FollowFormation() {
        noTimeout();
        cooldownFor(entity -> 10);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return canFollow(entity);
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return canFollow(entity) && !BrainUtils.hasMemory(entity, MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void start(E entity) {
        resetProgress();
    }

    @Override
    protected void stop(E entity) {
        HerdMoveControl control = control(entity);
        if (control != null) {
            control.stopSteering();
        }
        this.grazeSpot = null;
    }

    @Override
    protected void tick(E entity) {
        ServerLevel level = (ServerLevel) entity.level();
        long start = System.nanoTime();
        level.getProfiler().push("herdFormation");
        follow(level, entity);
        level.getProfiler().pop();
        HerdTimings.record(level.getGameTime(), System.nanoTime() - start);
    }

    private static boolean canFollow(Dinosaur dinosaur) {
        DinoGroup group = GroupManager.groupOf(dinosaur);
        return group != null && !group.isLeader(dinosaur.getUUID()) && dinosaur.isIdle() && HerdSpacing.applies(dinosaur);
    }

    private static @Nullable HerdMoveControl control(Dinosaur dinosaur) {
        return dinosaur.getMoveControl() instanceof HerdMoveControl control ? control : null;
    }

    private void follow(ServerLevel level, E entity) {
        DinoGroup group = GroupManager.groupOf(entity);
        HerdMoveControl control = control(entity);
        if (group == null || control == null) {
            return;
        }
        Dinosaur leader = GroupManager.leaderOf(group, level);
        if (leader == null || leader == entity) {
            control.stopSteering();
            return;
        }
        double space = HerdSpacing.personalSpace(entity.getBbWidth(), entity.getBbWidth());
        double radius = HerdArea.radius(group.size(), space, entity.getBbWidth());
        if (group.state() == HerdState.TRAVEL) {
            this.grazeSpot = null;
            travel(level, entity, group, leader, control, space, radius);
        } else {
            graze(level, entity, group, leader, control, space, radius);
        }
    }

    private void travel(ServerLevel level, E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control,
                        double space, double radius) {
        int followers = group.size() - 1;
        if (group.slotsDirty()) {
            assignSlots(level, group, leader, space);
        }
        int index = group.slotOf(entity.getUUID());
        if (index < 0 || index >= followers) {
            group.markSlotsDirty();
            control.stopSteering();
            return;
        }
        WedgeLayout.Slot slot = WedgeLayout.slot(index, followers, space);
        LeaderTrail.TrailPoint point = group.trail().pointBehind(
                WedgeLayout.rowDistance(slot.row(), space) + WedgeLayout.jitter(entity.getUUID(), 1));
        if (point == null) {
            control.stopSteering();
            return;
        }
        double lateral = squeeze(level, entity, group.trail(), point, slot.lateral() + WedgeLayout.jitter(entity.getUUID(), 0));
        double targetX = point.x() - point.dirZ() * lateral;
        double targetZ = point.z() + point.dirX() * lateral;
        if (control.stepFailures() > 0) {
            // Spec: a failed step check sends a travelling follower to the trail's centre line.
            targetX = point.x();
            targetZ = point.z();
        }
        double distance = horizontalDistance(entity, targetX, targetZ);
        double speed = distance > radius ? CATCH_UP_SPEED : TRAVEL_SPEED;
        if (needsFallback(control, distance, radius)) {
            requestFallback(level, entity, group, control, targetX, point.y(), targetZ, speed);
            return;
        }
        control.steerTo(targetX, point.y(), targetZ, speed, point.dirX(), point.dirZ());
    }

    /** Shrinks a lateral offset to the open width beside the trail, so the wedge files through narrow places. */
    private static double squeeze(ServerLevel level, Dinosaur entity, LeaderTrail trail, LeaderTrail.TrailPoint point, double lateral) {
        if (Math.abs(lateral) < 1.0E-3D) {
            return lateral;
        }
        boolean left = lateral > 0D;
        float open = trail.openWidth(point.entry(), left);
        if (Float.isNaN(open)) {
            open = StepCheck.openWidth(level, entity, point, left, SIDE_SAMPLE_BLOCKS);
            trail.setOpenWidth(point.entry(), left, open);
        }
        double usable = Math.max(0D, open - entity.getBbWidth() / 2D);
        return Math.copySign(Math.min(Math.abs(lateral), usable), lateral);
    }

    /**
     * Assigns wedge slots nearest-first. The wedge has a slot for every follower in the herd (the same
     * count {@link #travel} uses), so unloaded members leave gaps rather than shifting everyone. Keeps the
     * slots dirty while the trail is too short to place them.
     */
    private static void assignSlots(ServerLevel level, DinoGroup group, Dinosaur leader, double space) {
        List<Dinosaur> present = new ArrayList<>();
        for (Dinosaur member : GroupManager.presentMembers(group, level)) {
            if (member != leader) {
                present.add(member);
            }
        }
        int slotCount = group.size() - 1;
        double[][] slotXz = new double[slotCount][];
        for (int i = 0; i < slotCount; i++) {
            WedgeLayout.Slot slot = WedgeLayout.slot(i, slotCount, space);
            LeaderTrail.TrailPoint point = group.trail().pointBehind(WedgeLayout.rowDistance(slot.row(), space));
            if (point == null) {
                return;
            }
            slotXz[i] = new double[]{point.x() - point.dirZ() * slot.lateral(), point.z() + point.dirX() * slot.lateral()};
        }
        double[][] followerXz = new double[present.size()][];
        for (int i = 0; i < present.size(); i++) {
            followerXz[i] = new double[]{present.get(i).getX(), present.get(i).getZ()};
        }
        int[] assignment = WedgeLayout.assign(followerXz, slotXz);
        Map<UUID, Integer> slots = new HashMap<>();
        for (int i = 0; i < present.size(); i++) {
            slots.put(present.get(i).getUUID(), assignment[i]);
        }
        group.setSlots(slots);
    }

    private void graze(ServerLevel level, E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control,
                       double space, double radius) {
        long now = level.getGameTime();
        double fromLeader = entity.distanceTo(leader);
        boolean farFromHerd = fromLeader > REGROUP_FACTOR * radius;
        if (this.grazeSpot == null && (now >= this.nextGrazeMove || farFromHerd)) {
            this.grazeSpot = pickGrazeSpot(level, entity, group, leader, space, radius);
            resetProgress();
            if (this.grazeSpot == null) {
                // Nowhere free to go (for example, a small island): stay put and try again after a pause.
                this.nextGrazeMove = now + pause(entity.getRandom());
                control.stopSteering();
                return;
            }
        }
        if (this.grazeSpot == null) {
            return;
        }
        double distance = horizontalDistance(entity, this.grazeSpot.x, this.grazeSpot.z);
        if (distance <= HerdMoveControl.ARRIVED || control.stepFailures() > 0) {
            // Arrived, or a failed step check while grazing: spec says stay put.
            this.grazeSpot = null;
            this.nextGrazeMove = now + pause(entity.getRandom());
            control.stopSteering();
            return;
        }
        double speed = farFromHerd ? CATCH_UP_SPEED : GRAZE_SPEED;
        if (needsFallback(control, farFromHerd ? fromLeader : distance, radius)) {
            requestFallback(level, entity, group, control, this.grazeSpot.x, this.grazeSpot.y, this.grazeSpot.z, speed);
            return;
        }
        control.steerTo(this.grazeSpot.x, this.grazeSpot.y, this.grazeSpot.z, speed, 0D, 0D);
    }

    /** The roomiest of a few standable, free spots inside the herd area around the anchor, or {@code null}. */
    private static @Nullable Vec3 pickGrazeSpot(ServerLevel level, Dinosaur entity, DinoGroup group, Dinosaur leader,
                                                double space, double radius) {
        List<Dinosaur> members = GroupManager.presentMembers(group, level);
        List<HerdArea.Point> followers = new ArrayList<>();
        List<HerdArea.Point> occupied = new ArrayList<>();
        for (Dinosaur member : members) {
            if (member != leader) {
                followers.add(new HerdArea.Point(member.getX(), member.getZ()));
            }
            if (member != entity) {
                occupied.add(new HerdArea.Point(member.getX(), member.getZ()));
                Vec3 target = member.getMoveControl() instanceof HerdMoveControl control ? control.steeringTarget() : null;
                if (target != null) {
                    occupied.add(new HerdArea.Point(target.x, target.z));
                }
                WalkTarget walkTarget = BrainUtils.getMemory(member, MemoryModuleType.WALK_TARGET);
                if (walkTarget != null) {
                    Vec3 heading = walkTarget.getTarget().currentPosition();
                    occupied.add(new HerdArea.Point(heading.x, heading.z));
                }
            }
        }
        HerdArea.Point anchor = HerdArea.anchor(new HerdArea.Point(leader.getX(), leader.getZ()), followers);

        RandomSource random = entity.getRandom();
        List<HerdArea.Point> candidates = new ArrayList<>(HerdArea.SPOT_CANDIDATES);
        List<BlockPos> candidateBlocks = new ArrayList<>(HerdArea.SPOT_CANDIDATES);
        for (int attempt = 0; attempt < HerdArea.SPOT_CANDIDATES; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2D;
            double distance = radius * Math.sqrt(random.nextDouble());
            int x = Mth.floor(anchor.x() + Math.cos(angle) * distance);
            int z = Mth.floor(anchor.z() + Math.sin(angle) * distance);
            BlockPos spot = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            if (Math.abs(spot.getY() - leader.getBlockY()) > MAX_HEIGHT_DIFFERENCE
                    || !StepCheck.canStand(level, entity, x + 0.5D, spot.getY(), z + 0.5D)
                    || isTaken(x + 0.5D, z + 0.5D, occupied, space)) {
                continue;
            }
            candidates.add(new HerdArea.Point(x + 0.5D, z + 0.5D));
            candidateBlocks.add(spot);
        }
        int best = HerdArea.roomiest(candidates, occupied);
        if (best < 0) {
            return null;
        }
        BlockPos spot = candidateBlocks.get(best);
        return new Vec3(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D);
    }

    private static boolean isTaken(double x, double z, List<HerdArea.Point> occupied, double space) {
        for (HerdArea.Point point : occupied) {
            double dx = x - point.x();
            double dz = z - point.z();
            if (dx * dx + dz * dz < space * space) {
                return true;
            }
        }
        return false;
    }

    /** Progress monitor: far from the herd, stuck while not near the target, or repeated failed step checks. */
    private boolean needsFallback(HerdMoveControl control, double distance, double radius) {
        if (distance <= NEAR_ENOUGH) {
            resetProgress();
            return control.stepFailures() >= MAX_STEP_FAILURES;
        }
        if (distance < this.bestDistance - 0.1D) {
            this.bestDistance = distance;
            this.stalledTicks = 0;
        } else {
            this.stalledTicks++;
        }
        return distance > REGROUP_FACTOR * radius || this.stalledTicks > STALL_TICKS
                || control.stepFailures() >= MAX_STEP_FAILURES;
    }

    /** Within the herd's budget, hand over to vanilla pathfinding toward the target (at most FALLBACK_RANGE away). */
    private void requestFallback(ServerLevel level, E entity, DinoGroup group, HerdMoveControl control,
                                 double x, double y, double z, double speed) {
        if (!group.fallbacks().tryAcquire(entity.getUUID(), level.getGameTime())) {
            // Queued: keep steering meanwhile.
            control.steerTo(x, y, z, speed, 0D, 0D);
            return;
        }
        control.stopSteering();
        double dx = x - entity.getX();
        double dz = z - entity.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        double reach = Math.min(distance, FALLBACK_RANGE);
        double goalX = distance < 1.0E-6D ? x : entity.getX() + dx / distance * reach;
        double goalZ = distance < 1.0E-6D ? z : entity.getZ() + dz / distance * reach;
        BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET,
                new WalkTarget(BlockPos.containing(goalX, y, goalZ), (float) speed, FALLBACK_CLOSE_ENOUGH));
        resetProgress();
    }

    private void resetProgress() {
        this.bestDistance = Double.MAX_VALUE;
        this.stalledTicks = 0;
    }

    private static int pause(RandomSource random) {
        return GRAZE_PAUSE_MIN_TICKS + random.nextInt(GRAZE_PAUSE_SPREAD_TICKS);
    }

    private static double horizontalDistance(Dinosaur entity, double x, double z) {
        double dx = x - entity.getX();
        double dz = z - entity.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
```

- [ ] **Step 2: Delete `FollowGroupLeader`**

```bash
rm common/src/main/java/net/dumbcode/projectnublar/entity/ai/behaviour/social/FollowGroupLeader.java
```

Then remove the stale Javadoc references to it: in `HerdSpacing`, replace `{@link FollowGroupLeader} picks destinations` with `{@link FollowFormation} picks destinations`. Search for any other reference:

Run: `grep -rn "FollowGroupLeader" common/src forge/src`
Expected after the edits: only the `Dinosaur` wiring line fixed in Step 3.

- [ ] **Step 3: Wire it into `Dinosaur`**

In the imports, replace `import net.dumbcode.projectnublar.entity.ai.behaviour.social.FollowGroupLeader;` with:

```java
import net.dumbcode.projectnublar.entity.ai.behaviour.social.FollowFormation;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.HerdLeading;
```

In `getCoreTasks`, replace `new FollowGroupLeader<>().cohesionRadius(12)` with `new FollowFormation<>()` (keep `new ThreatDisplay<>(34)` before it).

In `customServerAiStep`, after the `GROUP_LEADER` block, add:

```java
        // The leader records its trail and reports whether the herd travels (no-op for non-leaders).
        HerdLeading.tick(this);
```

Steering sets the move control's wanted position inside the move control's own tick, after this method has run, so `hasWanted()` never sees it. Extend the Task 2 stamina check to count steering: replace

```java
        boolean selfPropelled = this.getNavigation().isInProgress() || this.getMoveControl().hasWanted();
```

with

```java
        boolean selfPropelled = this.getNavigation().isInProgress() || this.getMoveControl().hasWanted()
                || this.getMoveControl() instanceof HerdMoveControl herd && herd.isSteering();
```

(`HerdMoveControl` is already imported in `Dinosaur`.)

- [ ] **Step 4: Compile and run the tests**

Run: `./gradlew :forge:compileJava`, then `./gradlew :common:compileJava` and `sh tools/junit-standin/run.sh`
Expected: `BUILD SUCCESSFUL`; `69 run, 0 failed`.

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`
Expected: `FollowGroupLeader.java` shows as deleted (`D` or missing), `FollowFormation.java` new.

---

### Task 16: GameTest arenas and herd GameTests

**Files:**
- Create: `tools/gametest-templates/make_templates.py`
- Create: `forge/src/main/resources/data/projectnublar/structures/herd_track.nbt`, `herd_forest.nbt`, `herd_shore.nbt` (generated)
- Modify: `forge/src/main/java/net/dumbcode/projectnublar/gametest/HerdGameTests.java`

**Interfaces:**
- Consumes: `GroupManager.groupOf(Dinosaur)`, `DinoGroup.size()`, `DinoGroup.mostFallbacksInOneTick()`, `Dinosaur.isGroupLeader()`, the existing `hatch` helper in `HerdGameTests`.

- [ ] **Step 1: Write the template generator**

```python
"""Writes the herd GameTest arenas as structure NBT files (Minecraft 1.20.1, DataVersion 3465).

Usage, from the worktree root:  python tools/gametest-templates/make_templates.py
"""
import gzip
import os
import struct

OUT = "forge/src/main/resources/data/projectnublar/structures"
DATA_VERSION = 3465


def _name(text):
    data = text.encode("utf-8")
    return struct.pack(">H", len(data)) + data


def _int(name, value):
    return b"\x03" + _name(name) + struct.pack(">i", value)


def _string(name, value):
    return b"\x08" + _name(name) + _name(value)


def _int_list(name, values):
    return b"\x09" + _name(name) + b"\x03" + struct.pack(">i", len(values)) + b"".join(struct.pack(">i", v) for v in values)


def _compound_list(name, items):
    kind = b"\x0a" if items else b"\x00"
    return b"\x09" + _name(name) + kind + struct.pack(">i", len(items)) + b"".join(item + b"\x00" for item in items)


def write(name, size, blocks):
    """blocks: dict (x, y, z) -> block id. Positions not listed are left as they are (structure void)."""
    palette = sorted(set(blocks.values()))
    index = {block: i for i, block in enumerate(palette)}
    entries = [_int_list("pos", list(pos)) + _int("state", index[block]) for pos, block in sorted(blocks.items())]
    root = (b"\x0a" + _name("") + _int("DataVersion", DATA_VERSION) + _int_list("size", list(size))
            + _compound_list("palette", [_string("Name", block) for block in palette])
            + _compound_list("blocks", entries) + _compound_list("entities", []) + b"\x00")
    os.makedirs(OUT, exist_ok=True)
    with open(os.path.join(OUT, name + ".nbt"), "wb") as handle:
        handle.write(gzip.compress(root))


def walled_floor(size_x, size_y, size_z, wall_height):
    blocks = {}
    for x in range(size_x):
        for z in range(size_z):
            blocks[(x, 0, z)] = "minecraft:grass_block"
            if x in (0, size_x - 1) or z in (0, size_z - 1):
                for y in range(1, wall_height + 1):
                    blocks[(x, y, z)] = "minecraft:barrier"
    return blocks


def main():
    # Open track: 64 x 24, for travel and spacing tests.
    write("herd_track", (64, 6, 24), walled_floor(64, 6, 24, 5))

    # Forest: the same track with log pillars 6 high on a staggered grid, gaps of at least 4 blocks.
    forest = walled_floor(64, 8, 24, 7)
    for row, z in enumerate(range(3, 22, 5)):
        for x in range(24 + (2 if row % 2 else 0), 50, 5):
            for y in range(1, 7):
                forest[(x, y, z)] = "minecraft:oak_log"
    write("herd_forest", (64, 8, 24), forest)

    # Shore: the track with a 3-wide water channel at z 12-14 from x 24 to 60, level with the floor.
    shore = walled_floor(64, 6, 24, 5)
    for x in range(24, 61):
        for z in range(12, 15):
            shore[(x, 0, z)] = "minecraft:water"
    write("herd_shore", (64, 6, 24), shore)
    print("ok")


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Generate the arenas**

Run: `python tools/gametest-templates/make_templates.py`
Expected: `ok`, and three new `.nbt` files under `forge/src/main/resources/data/projectnublar/structures/`.

- [ ] **Step 3: Add the tests to `HerdGameTests`**

Add imports `net.minecraft.world.entity.ai.memory.MemoryModuleType`, `net.minecraft.world.entity.ai.memory.WalkTarget`, `net.tslat.smartbrainlib.util.BrainUtils`. Add these constants and methods to the class:

```java
    private static final String TRACK = "herd_track";
    private static final String FOREST = "herd_forest";
    private static final String SHORE = "herd_shore";
    /** Gallimimus: width 2, so personal space 2 + 1 = 3 (HerdSpacing). */
    private static final double PERSONAL_SPACE = 3D;
    /** HerdArea.radius(20, 3, 2) = 3 * sqrt(10) + 1. */
    private static final double HERD_RADIUS_20 = 3D * Math.sqrt(10D) + 1D;
    private static final BlockPos GOAL = new BlockPos(56, 1, 8);

    @GameTest(template = TRACK, timeoutTicks = 2400)
    public static void herdOfTwentyTravelsWithoutCrowding(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        int[] samples = new int[2];
        Dinosaur[] leader = new Dinosaur[1];
        helper.startSequence()
                .thenWaitUntil(() -> assertOneHerdOfTwenty(helper, animals))
                .thenExecute(() -> leader[0] = sendLeaderTo(helper, animals, GOAL))
                .thenWaitUntil(() -> {
                    samples[0]++;
                    if (minimumDistance(animals) > 2D) {
                        samples[1]++;
                    }
                    helper.assertTrue(leader[0].distanceToSqr(helper.absoluteVec(GOAL.getCenter())) < 16D, "the leader has not arrived");
                })
                .thenWaitUntil(() -> assertAllNearAnchor(helper, animals, leader[0], HERD_RADIUS_20))
                .thenExecute(() -> helper.assertTrue(samples[1] >= 0.95D * samples[0],
                        "bodies closer than 2 blocks in " + (samples[0] - samples[1]) + " of " + samples[0] + " ticks"))
                .thenSucceed();
    }

    @GameTest(template = FOREST, timeoutTicks = 3000)
    public static void herdCrossesForestWithinTheFallbackBudget(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        Dinosaur[] leader = new Dinosaur[1];
        helper.startSequence()
                .thenWaitUntil(() -> assertOneHerdOfTwenty(helper, animals))
                .thenExecute(() -> leader[0] = sendLeaderTo(helper, animals, GOAL))
                .thenWaitUntil(() -> {
                    helper.assertTrue(leader[0].distanceToSqr(helper.absoluteVec(GOAL.getCenter())) < 16D, "the leader has not arrived");
                    for (Dinosaur animal : animals) {
                        helper.assertTrue(animal.distanceTo(leader[0]) < 1.5D * HERD_RADIUS_20, "a member is still behind");
                    }
                })
                .thenExecute(() -> helper.assertTrue(GroupManager.groupOf(leader[0]).mostFallbacksInOneTick() <= 2,
                        "more than 2 fallback paths in one tick"))
                .thenSucceed();
    }

    @GameTest(template = SHORE, timeoutTicks = 2400)
    public static void herdSkirtsWater(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        Dinosaur[] leader = new Dinosaur[1];
        BlockPos goal = new BlockPos(56, 1, 6);
        helper.startSequence()
                .thenWaitUntil(() -> assertOneHerdOfTwenty(helper, animals))
                .thenExecute(() -> leader[0] = sendLeaderTo(helper, animals, goal))
                .thenWaitUntil(() -> {
                    for (Dinosaur animal : animals) {
                        if (animal.isInWater()) {
                            helper.fail("a herd member walked into the water");
                        }
                    }
                    helper.assertTrue(leader[0].distanceToSqr(helper.absoluteVec(goal.getCenter())) < 16D, "the leader has not arrived");
                })
                .thenSucceed();
    }

    @GameTest(template = TRACK, timeoutTicks = 2000)
    public static void herdKeepsPersonalSpace(GameTestHelper helper) {
        List<Dinosaur> animals = hatchTwenty(helper, 3);
        int[] crowded = new int[animals.size()];
        int[] ticks = new int[1];
        boolean[] formed = new boolean[1];
        helper.onEachTick(() -> {
            if (!formed[0]) {
                DinoGroup group = GroupManager.groupOf(animals.get(0));
                formed[0] = group != null && group.size() == 20;
                return;
            }
            for (int i = 0; i < animals.size(); i++) {
                boolean tooClose = false;
                for (int j = 0; j < animals.size() && !tooClose; j++) {
                    tooClose = i != j && animals.get(i).distanceTo(animals.get(j)) < PERSONAL_SPACE;
                }
                crowded[i] = tooClose ? crowded[i] + 1 : 0;
                if (crowded[i] > 40) {
                    helper.fail("a herd member stayed inside another's personal space for over 40 ticks");
                }
            }
            if (++ticks[0] >= 1200) {
                helper.succeed();
            }
        });
    }

    /** Twenty adult Gallimimus on a 5 x 4 grid at the start of a track. */
    private static List<Dinosaur> hatchTwenty(GameTestHelper helper, int firstZ) {
        List<Dinosaur> animals = new ArrayList<>();
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 5; column++) {
                animals.add(hatch(helper, EntityInit.GALLIMIMUS.get(), new BlockPos(3 + 4 * column, 1, firstZ + 5 * row), 0D));
            }
        }
        return animals;
    }

    private static void assertOneHerdOfTwenty(GameTestHelper helper, List<Dinosaur> animals) {
        DinoGroup group = GroupManager.groupOf(animals.get(0));
        helper.assertTrue(group != null && group.size() == 20, "no herd of 20 yet");
        for (Dinosaur animal : animals) {
            helper.assertTrue(GroupManager.groupOf(animal) == group, "an animal is in another herd or none");
        }
    }

    /** Gives the herd leader a walk target, which starts travel; returns the leader. */
    private static Dinosaur sendLeaderTo(GameTestHelper helper, List<Dinosaur> animals, BlockPos goal) {
        Dinosaur leader = animals.stream().filter(Dinosaur::isGroupLeader).findFirst().orElseThrow();
        BrainUtils.setMemory(leader, MemoryModuleType.WALK_TARGET, new WalkTarget(helper.absolutePos(goal), 0.8F, 2));
        return leader;
    }

    private static double minimumDistance(List<Dinosaur> animals) {
        double minimum = Double.MAX_VALUE;
        for (int i = 0; i < animals.size(); i++) {
            for (int j = i + 1; j < animals.size(); j++) {
                minimum = Math.min(minimum, animals.get(i).distanceTo(animals.get(j)));
            }
        }
        return minimum;
    }

    /** Every member within the radius of the weighted anchor (leader weighs a third of the followers; HerdArea.anchor). */
    private static void assertAllNearAnchor(GameTestHelper helper, List<Dinosaur> animals, Dinosaur leader, double radius) {
        int followers = animals.size() - 1;
        double weight = Math.max(1D, followers / 3D);
        double x = leader.getX() * weight;
        double z = leader.getZ() * weight;
        for (Dinosaur animal : animals) {
            if (animal != leader) {
                x += animal.getX();
                z += animal.getZ();
            }
        }
        x /= weight + followers;
        z /= weight + followers;
        for (Dinosaur animal : animals) {
            double dx = animal.getX() - x;
            double dz = animal.getZ() - z;
            helper.assertTrue(dx * dx + dz * dz <= radius * radius, "a member is outside the herd radius");
        }
    }
```

`BlockPos.getCenter()` and `GameTestHelper.absoluteVec(Vec3)` both exist in 1.20.1.

- [ ] **Step 4: Compile**

Run: `./gradlew :forge:compileJava`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Checkpoint (no commit)**

Run: `git status --short`. The lead runs these tests in Task 17.

---

### Task 17: In-game verification and the budget (the lead)

- [ ] **Step 1: Launch the headed client**

Run (in the background): `./gradlew :forge:runClient`
Expected: the log line `Sound engine started`.

- [ ] **Step 2: The lead checks** (hand over these steps)

1. **GameTests:** in a creative world, `/test runall`. All tests should pass: the four from before, plus `herdOfTwentyTravelsWithoutCrowding`, `herdCrossesForestWithinTheFallbackBudget`, `herdSkirtsWater` and `herdKeepsPersonalSpace`.
2. **Travel:** hatch 20 Gallimimus and watch the leader walk off. The herd should trail in a loose wedge, file through gaps between trees and open up again, without shoving.
3. **Grazing:** when the leader stops, the herd should spread over an area of about 10 blocks around it, without piling onto the leader.
4. **Stamina:** a member that gets bumped while standing keeps its stamina (debug stick, check twice a few seconds apart).
5. **Tracking range:** walk away from the herd. It should stay visible to about 160 blocks, like cows.
6. **Budget:** repeat the Task 6 measurement (`/debug start` and `/debug stop`, and the `Herd AI:` line on the debug stick).

- [ ] **Step 3: Record and compare**

Add a section `## After (Task 17)` at the end of this plan with the same numbers as the Task 6 baseline. Compare against the budget: average under 1 ms per tick, worst under 5 ms, at most 2 fallbacks per tick (the forest GameTest asserts the last).

- [ ] **Step 4: Checkpoint (no commit)**

Run: `git status --short`

## Baseline (Task 6)

Measured by the lead on 2026-10-08, herd of 20 Gallimimus, old follow-the-leader movement (Tasks 1-5 build).

| Measure | Value |
| --- | --- |
| Herd AI (debug stick) | average 0.051-0.060 ms/tick; worst 0.281-0.285 ms (one early reading 1.631 ms, during herd formation) |
| Server tick (F3) | about 13-17 ms; tick profiling 340.52 s over 6100 ticks = 17.91 TPS |
| Frame rate, herd in view, colour layers on | 26-28 fps (GPU 88%) |
| Frame rate, colour layers off | not recorded |
| `/debug` profile-results file | not written (none found under `forge/run` or the save); share of `projectnublar:gallimimus` not available |

Observations: followers get stuck in forests; one follower was 126.8 blocks from its leader. The leader showed "Path: 1/9 nodes, reaches target: false" with "Can't reach walk target since: 22 ticks" in trees.
