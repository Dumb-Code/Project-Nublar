package net.dumbcode.projectnublar.gametest;

import net.dumbcode.projectnublar.Constants;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Registers the GameTests of {@link HerdGameTests} and {@link MachineGameTests} on Forge. This class only carries
 * the loader annotations: per test, one {@code @GameTest} line and a one-line call to the test body. Forge finds
 * the class by {@code @GameTestHolder}; with {@code @PrefixGameTestTemplate(false)} the template is
 * {@code projectnublar:<template>} and the test name is the method name in lower case.
 */
@GameTestHolder(Constants.MODID)
@PrefixGameTestTemplate(false)
public class ForgeGameTests {

    @GameTest(template = HerdGameTests.ARENA, batch = "fivegallimimusformoneherdledbythestrongest", timeoutTicks = 600)
    public void fiveGallimimusFormOneHerdLedByTheStrongest(GameTestHelper helper) { HerdGameTests.fiveGallimimusFormOneHerdLedByTheStrongest(helper); }

    @GameTest(template = HerdGameTests.ARENA, batch = "twentygallimimusformoneherdoftwenty", timeoutTicks = 1200)
    public void twentyGallimimusFormOneHerdOfTwenty(GameTestHelper helper) { HerdGameTests.twentyGallimimusFormOneHerdOfTwenty(helper); }

    @GameTest(template = HerdGameTests.ARENA, batch = "strongernewcomerleadsatonce", timeoutTicks = 600)
    public void strongerNewcomerLeadsAtOnce(GameTestHelper helper) { HerdGameTests.strongerNewcomerLeadsAtOnce(helper); }

    @GameTest(template = HerdGameTests.ARENA, batch = "deadmemberleavesitsherd", timeoutTicks = 400)
    public void deadMemberLeavesItsHerd(GameTestHelper helper) { HerdGameTests.deadMemberLeavesItsHerd(helper); }

    @GameTest(template = HerdGameTests.TRACK, batch = "grazing_hard_minimum", timeoutTicks = 3000)
    public void grazingHerdHoldsHardMinimum(GameTestHelper helper) { HerdGameTests.grazingHerdHoldsHardMinimum(helper); }

    @GameTest(template = HerdGameTests.TRACK, batch = "grazing_stay_put", timeoutTicks = 3400)
    public void grazingMembersStayPut(GameTestHelper helper) { HerdGameTests.grazingMembersStayPut(helper); }

    @GameTest(template = HerdGameTests.TRACK, batch = "small_leader_moves", timeoutTicks = 2400)
    public void smallLeaderMovesKeepGraze(GameTestHelper helper) { HerdGameTests.smallLeaderMovesKeepGraze(helper); }

    @GameTest(template = HerdGameTests.ARENA, batch = "stacked_members", timeoutTicks = 600)
    public void stackedMembersSeparate(GameTestHelper helper) { HerdGameTests.stackedMembersSeparate(helper); }

    @GameTest(template = HerdGameTests.TRACK, batch = "travel_trail", timeoutTicks = 3200)
    public void herdTravelsAlongTrail(GameTestHelper helper) { HerdGameTests.herdTravelsAlongTrail(helper); }

    @GameTest(template = HerdGameTests.FOREST, batch = "travel_forest", timeoutTicks = 4000)
    public void herdCrossesForest(GameTestHelper helper) { HerdGameTests.herdCrossesForest(helper); }

    @GameTest(template = HerdGameTests.CORRIDOR, batch = "travel_corridor", timeoutTicks = 3400)
    public void herdPassesCorridor(GameTestHelper helper) { HerdGameTests.herdPassesCorridor(helper); }

    @GameTest(template = HerdGameTests.LOWGAP, batch = "travel_lowgap", timeoutTicks = 3500)
    public void memberNeverEntersLowGap(GameTestHelper helper) { HerdGameTests.memberNeverEntersLowGap(helper); }

    @GameTest(template = HerdGameTests.SHORE, batch = "travel_shore", timeoutTicks = 3400)
    public void herdSkirtsWater(GameTestHelper helper) { HerdGameTests.herdSkirtsWater(helper); }

    @GameTest(template = HerdGameTests.ARENA, batch = "sequencersynthstartswithgeneratoroncore", timeoutTicks = 200)
    public void sequencerSynthStartsWithGeneratorOnCore(GameTestHelper helper) { MachineGameTests.sequencerSynthStartsWithGeneratorOnCore(helper); }

    @GameTest(template = HerdGameTests.ARENA, batch = "sequencersynthfailswithgeneratoronpart", timeoutTicks = 200)
    public void sequencerSynthFailsWithGeneratorOnPart(GameTestHelper helper) { MachineGameTests.sequencerSynthFailsWithGeneratorOnPart(helper); }

    @GameTest(template = HerdGameTests.ARENA, batch = "sequencercorereceivesenergy", timeoutTicks = 200)
    public void sequencerCoreReceivesEnergy(GameTestHelper helper) { MachineGameTests.sequencerCoreReceivesEnergy(helper); }
}
