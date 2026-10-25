package net.dumbcode.projectnublar.gametest;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.api.multiblock.MultiBlock;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;

/**
 * GameTests for the Sequencer's synthesis start ("Begin") and the energy that reaches it from a creative
 * generator. The Sequencer is a 2 x 2 x 2 multiblock and only its core block (rows, columns and depth all 0)
 * has the block entity that stores energy; these tests check whether a generator must touch that core block.
 * Each test is its own batch and uses the herd arena template ({@code HerdGameTests.ARENA}). The tests have no
 * loader code; the holders {@code ForgeGameTests} (Forge) and {@code FabricGameTests} (Fabric) register them.
 * Run a test with {@code /test run <name>} on Forge and {@code /test run fabricgametests.<name>} on Fabric,
 * where the name is the method name in lower case:
 * <ul>
 * <li>{@code sequencersynthstartswithgeneratoroncore}: generator against the core, all inputs filled;
 * synthesis must start.</li>
 * <li>{@code sequencersynthfailswithgeneratoronpart}: generator against a non-core part only, all inputs
 * filled; synthesis must NOT start (the test passes when it does not).</li>
 * <li>{@code sequencercorereceivesenergy}: generator against the core, no inputs; the core must store more
 * than the 32 FE a synthesis tick needs.</li>
 * </ul>
 * Every test logs its readings with the prefix {@code [machinetest]}.
 */
public final class MachineGameTests {

    /** First layer above the arena floor (see {@code HerdGameTests.FLOOR_Y}). */
    private static final int FLOOR_Y = 2;
    /** Core block of the Sequencer. With FACING north the parts extend west (columns), south (depth) and up (rows). */
    private static final BlockPos CORE = new BlockPos(10, FLOOR_Y, 10);
    /** East of the core: touches the core only. */
    private static final BlockPos GENERATOR_ON_CORE = CORE.east();
    /** East of part (rows 0, columns 0, depth 1) at CORE.south(): touches that part only, diagonal to the core. */
    private static final BlockPos GENERATOR_ON_PART = CORE.south().east();
    private static final int SETTLE_TICKS = 40;
    private static final int AFTER_TOGGLE_TICKS = 5;
    private static final int SEQUENCER_ROWS = 2;
    private static final int SEQUENCER_COLUMNS = 2;
    private static final int SEQUENCER_DEPTH = 2;
    private static final String[] LEVEL_FIELDS = {"waterLevel", "boneMatterLevel", "sugarLevel", "plantMatterLevel"};

    private MachineGameTests() {
    }

    /**
     * Generator against the core block, all synthesis inputs filled: Begin starts synthesis.
     */
    public static void sequencerSynthStartsWithGeneratorOnCore(GameTestHelper helper) {
        SequencerBlockEntity sequencer = placeSequencer(helper, GENERATOR_ON_CORE);
        fillSynthInputs(sequencer);
        String[] before = new String[1];
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            before[0] = readings(sequencer);
            Constants.LOG.info("[machinetest] onCore before toggle: {}", before[0]);
            sequencer.toggleSynth();
        });
        helper.runAfterDelay(SETTLE_TICKS + AFTER_TOGGLE_TICKS, () -> {
            String after = readings(sequencer);
            Constants.LOG.info("[machinetest] onCore {} ticks after toggle: {}", AFTER_TOGGLE_TICKS, after);
            helper.succeedIf(() -> helper.assertTrue(sequencer.isSynthesizing(),
                    "synthesis did not start with the generator on the core; before toggle: " + before[0]
                            + "; after: " + after));
        });
    }

    /**
     * Generator against a non-core part only, all synthesis inputs filled: Begin does not start synthesis.
     * The test passes when synthesis stays off.
     */
    public static void sequencerSynthFailsWithGeneratorOnPart(GameTestHelper helper) {
        SequencerBlockEntity sequencer = placeSequencer(helper, GENERATOR_ON_PART);
        fillSynthInputs(sequencer);
        String[] before = new String[1];
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            before[0] = readings(sequencer);
            Constants.LOG.info("[machinetest] onPart before toggle: {}", before[0]);
            sequencer.toggleSynth();
        });
        helper.runAfterDelay(SETTLE_TICKS + AFTER_TOGGLE_TICKS, () -> {
            String after = readings(sequencer);
            Constants.LOG.info("[machinetest] onPart {} ticks after toggle: {}", AFTER_TOGGLE_TICKS, after);
            helper.succeedIf(() -> helper.assertFalse(sequencer.isSynthesizing(),
                    "expected synthesis to stay off with the generator on a non-core part, but it started; before toggle: "
                            + before[0] + "; after: " + after));
        });
    }

    /**
     * Generator against the core block, no inputs: the core stores more than one synthesis tick of energy.
     */
    public static void sequencerCoreReceivesEnergy(GameTestHelper helper) {
        SequencerBlockEntity sequencer = placeSequencer(helper, GENERATOR_ON_CORE);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            String now = readings(sequencer);
            Constants.LOG.info("[machinetest] coreEnergy after {} ticks: {}", SETTLE_TICKS, now);
            long stored = sequencer.getEnergyStorage().getStoredEnergy();
            helper.succeedIf(() -> helper.assertTrue(stored > sequencer.calculateEnergyConsumption(),
                    "the core stored only " + stored + " FE; " + now));
        });
    }

    /**
     * Places the 8 Sequencer blocks exactly as {@code MultiBlockItem.placeBlock} does (same loop and offsets:
     * up by rows, counter-clockwise of FACING by columns, opposite FACING by depth), then a creative generator.
     */
    private static SequencerBlockEntity placeSequencer(GameTestHelper helper, BlockPos generatorPos) {
        Direction direction = Direction.NORTH;
        BlockState state = BlockInit.SEQUENCER.get().defaultBlockState().setValue(MultiBlock.FACING, direction);
        for (int i = 0; i < SEQUENCER_ROWS; i++) {
            for (int j = 0; j < SEQUENCER_COLUMNS; j++) {
                for (int k = 0; k < SEQUENCER_DEPTH; k++) {
                    BlockPos pos = CORE.relative(Direction.UP, i).relative(direction.getCounterClockWise(), j)
                            .relative(direction.getOpposite(), k);
                    helper.setBlock(pos, state.setValue(MultiBlock.DEPTH, k).setValue(MultiBlock.COLUMNS, j)
                            .setValue(MultiBlock.ROWS, i));
                }
            }
        }
        helper.setBlock(generatorPos, BlockInit.CREATIVE_GENERATOR.get().defaultBlockState());
        BlockEntity blockEntity = helper.getBlockEntity(CORE);
        if (!(blockEntity instanceof SequencerBlockEntity sequencer)) {
            helper.fail("no SequencerBlockEntity at the core, found " + blockEntity);
            throw new IllegalStateException("unreachable");
        }
        return sequencer;
    }

    private static void fillSynthInputs(SequencerBlockEntity sequencer) {
        sequencer.setItem(SequencerBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        sequencer.setItem(SequencerBlockEntity.SLOT_BONE_MATTER, new ItemStack(Items.BONE_MEAL, 16));
        sequencer.setItem(SequencerBlockEntity.SLOT_SUGAR, new ItemStack(Items.SUGAR, 16));
        sequencer.setItem(SequencerBlockEntity.SLOT_PLANT_MATTER, new ItemStack(Items.OAK_LEAVES, 16));
        sequencer.setItem(SequencerBlockEntity.SLOT_EMPTY_TUBE_INPUT, new ItemStack(ItemInit.TEST_TUBE_ITEM.get()));
    }

    private static String readings(SequencerBlockEntity sequencer) {
        StringBuilder text = new StringBuilder()
                .append("energy=").append(sequencer.getEnergyStorage().getStoredEnergy())
                .append(" consumption=").append(sequencer.calculateEnergyConsumption())
                .append(" canSynth=").append(sequencer.canSynth());
        for (String name : LEVEL_FIELDS) {
            text.append(' ').append(name).append('=').append(intField(sequencer, name));
        }
        text.append(" tubeSlot=").append(sequencer.getItem(SequencerBlockEntity.SLOT_EMPTY_TUBE_INPUT))
                .append(" isSynthesizing=").append(sequencer.isSynthesizing());
        return text.toString();
    }

    /** The resource levels have no getters; read them by reflection (test diagnostics only). */
    private static String intField(SequencerBlockEntity sequencer, String name) {
        try {
            Field field = SequencerBlockEntity.class.getDeclaredField(name);
            field.setAccessible(true);
            return Integer.toString(field.getInt(sequencer));
        } catch (ReflectiveOperationException e) {
            return "?(" + e.getClass().getSimpleName() + ")";
        }
    }
}
