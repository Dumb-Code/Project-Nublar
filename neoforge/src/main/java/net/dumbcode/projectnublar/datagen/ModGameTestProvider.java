package net.dumbcode.projectnublar.datagen;

import dev.architectury.registry.registries.RegistrySupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.gametest.GameTestInit;
import net.dumbcode.projectnublar.gametest.HerdGameTests;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestEnvironments;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Generates the vanilla GameTest data of the mod: one test instance per test function of
 * {@link GameTestInit} ({@code data/projectnublar/test_instance/<id>.json}) and the mod's test
 * environments ({@code data/projectnublar/test_environment/<name>.json}).
 *
 * <p>26.2 runs all tests of one environment at the same time, as one batch, with their templates
 * 5 blocks apart. A herd reaches about 31 blocks for new members, so herds of neighbouring arenas
 * would merge. Each herd test therefore has its own empty environment, named after the test, and
 * runs alone, as every test did on 1.20.1. The machine tests share {@code minecraft:default}. The
 * scale test has {@link #SCALE_SWITCH}, because it changes a config switch that other tests read.
 *
 * <p>The time limits are the 1.20.1 {@code timeoutTicks} values; the two new tests explain theirs
 * in their Javadoc. The codec leaves out fields at their default ({@code setup_ticks} 0,
 * {@code required} true), so the files show only the other values.
 *
 * <p>Eight herd tests are optional ({@code required} false): they run and report, but a failure
 * does not fail the GameTest run or {@code build}.
 * <ul>
 * <li>{@code stackedmembersseparate}: known to be flaky (handoff, ruling P14).</li>
 * <li>{@code grazingherdholdshardminimum}, {@code grazingmembersstayput},
 * {@code smallleadermoveskeepgraze}, {@code memberneverenterslowgap}, {@code herdskirtswater},
 * {@code herdtravelsalongtrail} and {@code herdcrossesforest}: known failures since 1.20.1, not a
 * port regression (the 1.20.1 README at commit {@code 0838bd4}: 8 or 9 of the 13 herd tests fail
 * on Forge); on 26.2 each fails in every run or in some runs (ruling P189). The herd fixes are a
 * later plan task.</li>
 * </ul>
 */
public class ModGameTestProvider extends DatapackBuiltinEntriesProvider {

    /** The empty environment of {@code scalefollowsgrowthstage}; its own batch, nothing else. */
    private static final ResourceKey<TestEnvironmentDefinition<?>> SCALE_SWITCH =
            environment("scale_switch");

    private static final int SETUP_TICKS = 0;

    private static final List<TestEntry> TESTS = List.of(
            herdTest("fivegallimimusformoneherdledbythestrongest", HerdGameTests.ARENA, 600),
            herdTest("twentygallimimusformoneherdoftwenty", HerdGameTests.ARENA, 1200),
            herdTest("strongernewcomerleadsatonce", HerdGameTests.ARENA, 600),
            herdTest("deadmemberleavesitsherd", HerdGameTests.ARENA, 400),
            optionalHerdTest("grazingherdholdshardminimum", HerdGameTests.TRACK, 3000),
            optionalHerdTest("grazingmembersstayput", HerdGameTests.TRACK, 3400),
            optionalHerdTest("smallleadermoveskeepgraze", HerdGameTests.TRACK, 2400),
            optionalHerdTest("stackedmembersseparate", HerdGameTests.ARENA, 600),
            optionalHerdTest("herdtravelsalongtrail", HerdGameTests.TRACK, 3200),
            optionalHerdTest("herdcrossesforest", HerdGameTests.FOREST, 4000),
            herdTest("herdpassescorridor", HerdGameTests.CORRIDOR, 3400),
            optionalHerdTest("memberneverenterslowgap", HerdGameTests.LOWGAP, 3500),
            optionalHerdTest("herdskirtswater", HerdGameTests.SHORE, 3400),
            new TestEntry("scalefollowsgrowthstage", HerdGameTests.ARENA, 100, SCALE_SWITCH, true),
            machineTest("sequencersynthstartswithgeneratoroncore", 200),
            machineTest("sequencersynthfailswithgeneratoronpart", 200),
            machineTest("sequencercorereceivesenergy", 200),
            machineTest("machinechainsmoke", 450));

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.TEST_ENVIRONMENT, ModGameTestProvider::environments)
            .add(Registries.TEST_INSTANCE, ModGameTestProvider::instances);

    public ModGameTestProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Constants.MODID));
    }

    /** A name of its own: the world gen provider has the inherited one, "Registries", already. */
    @Override
    public String getName() {
        return "Project Nublar GameTests";
    }

    /** Registers each mod environment the tests name as an empty {@code minecraft:all_of}. */
    private static void environments(BootstrapContext<TestEnvironmentDefinition<?>> context) {
        TESTS.stream()
                .map(TestEntry::environment)
                .filter(key -> key.identifier().getNamespace().equals(Constants.MODID))
                .distinct()
                .forEach(key -> context.register(key,
                        new TestEnvironmentDefinition.AllOf(List.of())));
    }

    /**
     * Registers one function test instance per entry.
     *
     * @throws IllegalStateException if the entries and the functions of {@link GameTestInit}
     *                               differ, so a test added in one place only stops the data run
     */
    private static void instances(BootstrapContext<GameTestInstance> context) {
        checkEveryFunctionHasAnEntry();
        HolderGetter<TestEnvironmentDefinition<?>> environments =
                context.lookup(Registries.TEST_ENVIRONMENT);
        for (TestEntry test : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                    ResourceKey.create(Registries.TEST_FUNCTION, Constants.modLoc(test.id()));
            TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                    environments.getOrThrow(test.environment()), Constants.modLoc(test.template()),
                    test.maxTicks(), SETUP_TICKS, test.required());
            ResourceKey<GameTestInstance> instance =
                    ResourceKey.create(Registries.TEST_INSTANCE, Constants.modLoc(test.id()));
            context.register(instance, new FunctionGameTestInstance(function, data));
        }
    }

    private static void checkEveryFunctionHasAnEntry() {
        Set<String> functions = new LinkedHashSet<>();
        for (RegistrySupplier<Consumer<GameTestHelper>> function : GameTestInit.TEST_FUNCTIONS) {
            functions.add(function.getId().getPath());
        }
        Set<String> entries = TESTS.stream()
                .map(TestEntry::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!functions.equals(entries)) {
            throw new IllegalStateException("GameTestInit registers " + functions
                    + " but ModGameTestProvider lists " + entries);
        }
    }

    /** A required herd test in an empty environment of its own, named after the test. */
    private static TestEntry herdTest(String id, String template, int maxTicks) {
        return new TestEntry(id, template, maxTicks, environment(id), true);
    }

    /**
     * An optional herd test in an empty environment of its own: it runs and reports, but its
     * failure does not fail the run (the class Javadoc lists why each one is optional).
     */
    private static TestEntry optionalHerdTest(String id, String template, int maxTicks) {
        return new TestEntry(id, template, maxTicks, environment(id), false);
    }

    /** A required machine test in the herd arena and {@code minecraft:default}. */
    private static TestEntry machineTest(String id, int maxTicks) {
        return new TestEntry(id, HerdGameTests.ARENA, maxTicks, GameTestEnvironments.DEFAULT_KEY,
                true);
    }

    private static ResourceKey<TestEnvironmentDefinition<?>> environment(String name) {
        return ResourceKey.create(Registries.TEST_ENVIRONMENT, Constants.modLoc(name));
    }

    /**
     * One test instance.
     *
     * @param id          the test function id and the instance id, the method name in lower case
     * @param template    the structure template under {@code data/projectnublar/structure/}
     * @param maxTicks    the time limit in ticks
     * @param environment the environment, which is also the batch the test runs in
     * @param required    whether a failure fails the GameTest run
     */
    private record TestEntry(String id, String template, int maxTicks,
                             ResourceKey<TestEnvironmentDefinition<?>> environment,
                             boolean required) {
    }
}
