package net.dumbcode.projectnublar.gametest;

import dev.architectury.registry.registries.DeferredRegister;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.function.Consumer;

/**
 * Registers every GameTest body as a vanilla test function ({@code projectnublar:<id>}). A test
 * instance file ({@code data/projectnublar/test_instance/<id>.json}, written by the NeoForge
 * {@code ModGameTestProvider}) names the function, its template, its environment and its time
 * limit. The id is the method name in lower case, as on 1.20.1, so
 * {@code /test run projectnublar:<id>} runs one test on both loaders.
 *
 * <p>The functions are registered on every server, not only in a development run: the test instance
 * files ship in the mod's data, and every function they name must exist wherever they load.
 */
public final class GameTestInit {

    /** The mod's test functions, one per GameTest body. */
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Constants.MODID, Registries.TEST_FUNCTION);

    static {
        TEST_FUNCTIONS.register("fivegallimimusformoneherdledbythestrongest",
                () -> HerdGameTests::fiveGallimimusFormOneHerdLedByTheStrongest);
        TEST_FUNCTIONS.register("twentygallimimusformoneherdoftwenty",
                () -> HerdGameTests::twentyGallimimusFormOneHerdOfTwenty);
        TEST_FUNCTIONS.register("strongernewcomerleadsatonce",
                () -> HerdGameTests::strongerNewcomerLeadsAtOnce);
        TEST_FUNCTIONS.register("deadmemberleavesitsherd",
                () -> HerdGameTests::deadMemberLeavesItsHerd);
        TEST_FUNCTIONS.register("grazingherdholdshardminimum",
                () -> HerdGameTests::grazingHerdHoldsHardMinimum);
        TEST_FUNCTIONS.register("grazingmembersstayput",
                () -> HerdGameTests::grazingMembersStayPut);
        TEST_FUNCTIONS.register("smallleadermoveskeepgraze",
                () -> HerdGameTests::smallLeaderMovesKeepGraze);
        TEST_FUNCTIONS.register("stackedmembersseparate",
                () -> HerdGameTests::stackedMembersSeparate);
        TEST_FUNCTIONS.register("herdtravelsalongtrail",
                () -> HerdGameTests::herdTravelsAlongTrail);
        TEST_FUNCTIONS.register("herdcrossesforest",
                () -> HerdGameTests::herdCrossesForest);
        TEST_FUNCTIONS.register("herdpassescorridor",
                () -> HerdGameTests::herdPassesCorridor);
        TEST_FUNCTIONS.register("memberneverenterslowgap",
                () -> HerdGameTests::memberNeverEntersLowGap);
        TEST_FUNCTIONS.register("herdskirtswater",
                () -> HerdGameTests::herdSkirtsWater);
        TEST_FUNCTIONS.register("scalefollowsgrowthstage",
                () -> HerdGameTests::scaleFollowsGrowthStage);
        TEST_FUNCTIONS.register("sequencersynthstartswithgeneratoroncore",
                () -> MachineGameTests::sequencerSynthStartsWithGeneratorOnCore);
        TEST_FUNCTIONS.register("sequencersynthfailswithgeneratoronpart",
                () -> MachineGameTests::sequencerSynthFailsWithGeneratorOnPart);
        TEST_FUNCTIONS.register("sequencercorereceivesenergy",
                () -> MachineGameTests::sequencerCoreReceivesEnergy);
        TEST_FUNCTIONS.register("machinechainsmoke",
                () -> MachineGameTests::machineChainSmoke);
    }

    private GameTestInit() {
    }

    /** Hands the test functions to the loader's registry; {@code ProjectNublar.init()} calls it. */
    public static void loadClass() {
        TEST_FUNCTIONS.register();
    }
}
