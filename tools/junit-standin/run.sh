#!/bin/sh
# Runs the unit tests in common/src/test/java against the JUnit stand-ins (the project has no JUnit).
# Use from the worktree root in Git Bash on Windows, after: ./gradlew :common:compileJava
set -e
ROOT="$(pwd)"
OUT="$ROOT/build/junit-standin"
CP="$(cygpath -w "$ROOT/common/build/classes/java/main")"
CP="$CP;$(cygpath -w "$ROOT/nublar-gui/build/classes/java/main")"

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

# Paths below are relative to the worktree root: the root path contains a space, which would split unquoted words.
rm -rf "$OUT"
TEST_SOURCES=$(cd "$ROOT" && find common/src/test/java -name "*Test.java")
TEST_CLASSES=$(cd "$ROOT/common/src/test/java" && find . -name "*Test.java" | sed 's|^\./||; s|\.java$||; s|/|.|g')
cd "$ROOT"
javac -cp "$CP" -d "$(cygpath -w "$OUT")" tools/junit-standin/stub/org/junit/jupiter/api/*.java tools/junit-standin/RunTests.java $TEST_SOURCES
java -cp "$(cygpath -w "$OUT");$CP" RunTests $TEST_CLASSES
