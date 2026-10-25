package net.dumbcode.projectnublar.gametest;

import net.dumbcode.projectnublar.Constants;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Registers the GameTests of {@link HerdGameTests} and {@link MachineGameTests} on Fabric, through the
 * {@code fabric-gametest} entrypoint in {@code fabric.mod.json}. This class only carries the loader annotations:
 * per test, one {@code @GameTest} line and a one-line call to the test body. Fabric uses the template string as
 * given, so it holds the {@code projectnublar:} namespace, and names each test
 * {@code fabricgametests.<method name in lower case>}. Fabric makes an instance with the public no-argument
 * constructor before each test, so the class declares no constructor.
 */
public class FabricGameTests implements FabricGameTest {

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.ARENA, batch = "fivegallimimusformoneherdledbythestrongest", timeoutTicks = 600)
    public void fiveGallimimusFormOneHerdLedByTheStrongest(GameTestHelper helper) { HerdGameTests.fiveGallimimusFormOneHerdLedByTheStrongest(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.ARENA, batch = "twentygallimimusformoneherdoftwenty", timeoutTicks = 1200)
    public void twentyGallimimusFormOneHerdOfTwenty(GameTestHelper helper) { HerdGameTests.twentyGallimimusFormOneHerdOfTwenty(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.ARENA, batch = "strongernewcomerleadsatonce", timeoutTicks = 600)
    public void strongerNewcomerLeadsAtOnce(GameTestHelper helper) { HerdGameTests.strongerNewcomerLeadsAtOnce(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.ARENA, batch = "deadmemberleavesitsherd", timeoutTicks = 400)
    public void deadMemberLeavesItsHerd(GameTestHelper helper) { HerdGameTests.deadMemberLeavesItsHerd(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.TRACK, batch = "grazing_hard_minimum", timeoutTicks = 3000)
    public void grazingHerdHoldsHardMinimum(GameTestHelper helper) { HerdGameTests.grazingHerdHoldsHardMinimum(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.TRACK, batch = "grazing_stay_put", timeoutTicks = 3400)
    public void grazingMembersStayPut(GameTestHelper helper) { HerdGameTests.grazingMembersStayPut(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.TRACK, batch = "small_leader_moves", timeoutTicks = 2400)
    public void smallLeaderMovesKeepGraze(GameTestHelper helper) { HerdGameTests.smallLeaderMovesKeepGraze(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.ARENA, batch = "stacked_members", timeoutTicks = 600)
    public void stackedMembersSeparate(GameTestHelper helper) { HerdGameTests.stackedMembersSeparate(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.TRACK, batch = "travel_trail", timeoutTicks = 3200)
    public void herdTravelsAlongTrail(GameTestHelper helper) { HerdGameTests.herdTravelsAlongTrail(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.FOREST, batch = "travel_forest", timeoutTicks = 4000)
    public void herdCrossesForest(GameTestHelper helper) { HerdGameTests.herdCrossesForest(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.CORRIDOR, batch = "travel_corridor", timeoutTicks = 3400)
    public void herdPassesCorridor(GameTestHelper helper) { HerdGameTests.herdPassesCorridor(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.LOWGAP, batch = "travel_lowgap", timeoutTicks = 3500)
    public void memberNeverEntersLowGap(GameTestHelper helper) { HerdGameTests.memberNeverEntersLowGap(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.SHORE, batch = "travel_shore", timeoutTicks = 3400)
    public void herdSkirtsWater(GameTestHelper helper) { HerdGameTests.herdSkirtsWater(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.ARENA, batch = "sequencersynthstartswithgeneratoroncore", timeoutTicks = 200)
    public void sequencerSynthStartsWithGeneratorOnCore(GameTestHelper helper) { MachineGameTests.sequencerSynthStartsWithGeneratorOnCore(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.ARENA, batch = "sequencersynthfailswithgeneratoronpart", timeoutTicks = 200)
    public void sequencerSynthFailsWithGeneratorOnPart(GameTestHelper helper) { MachineGameTests.sequencerSynthFailsWithGeneratorOnPart(helper); }

    @GameTest(template = Constants.MODID + ":" + HerdGameTests.ARENA, batch = "sequencercorereceivesenergy", timeoutTicks = 200)
    public void sequencerCoreReceivesEnergy(GameTestHelper helper) { MachineGameTests.sequencerCoreReceivesEnergy(helper); }
}
