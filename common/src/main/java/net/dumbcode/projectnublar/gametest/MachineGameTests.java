package net.dumbcode.projectnublar.gametest;

import net.blay09.mods.balm.platform.energy.BalmEnergyStorageProvider;
import net.blay09.mods.balm.platform.energy.EnergyStorage;
import net.blay09.mods.balm.platform.fluid.FluidTank;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.api.dinosaur.DiskStorage;
import net.dumbcode.projectnublar.api.fossil.FossilPieces;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.api.multiblock.MultiBlock;
import net.dumbcode.projectnublar.block.api.multiblock.MultiEntityBlock;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.block.entity.ProcessorBlockEntity;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.platform.Services;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.EntityInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * GameTests for the machines: the Sequencer's synthesis start ("Begin"), the energy that reaches
 * it from a creative generator, and the machine chain from a fossil to an incubated egg. The
 * Sequencer is a 2 x 2 x 2 multiblock, and only its core block (rows, columns and depth all 0) has
 * the block entity that stores energy; the first two tests put the generator against that core
 * block, and {@code generatoronpartfillseachmachine} checks that a generator or a cable reaches it
 * from any other block of a machine. Every test uses the herd arena template
 * ({@code HerdGameTests.ARENA}) and the {@code minecraft:default} environment.
 * {@code GameTestInit} registers the bodies, and {@code ModGameTestProvider} writes their test
 * instances. Run one test with {@code /test run projectnublar:<name>}, where the name is the
 * method name in lower case:
 * <ul>
 * <li>{@code sequencersynthstartswithgeneratoroncore}: generator against the core, all inputs filled;
 * synthesis must start.</li>
 * <li>{@code sequencercorereceivesenergy}: generator against the core, no inputs; the core must store more
 * than the 32 FE a synthesis tick needs.</li>
 * <li>{@code machinechainsmoke}: fossils through the Processor, Sequencer and Incubator to an
 * incubated egg.</li>
 * <li>{@code generatoronpartfillseachmachine}: a generator against a non-core block of each of the
 * four machines; each machine must fill up.</li>
 * </ul>
 * Every test logs its readings with the prefix {@code [machinetest]}.
 */
public final class MachineGameTests {

    /** First layer above the arena floor (see {@code HerdGameTests.FLOOR_Y}): template y=0 is relative y=0. */
    private static final int FLOOR_Y = 1;
    /** Core block of the Sequencer. With FACING north the parts extend west (columns), south (depth) and up (rows). */
    private static final BlockPos CORE = new BlockPos(10, FLOOR_Y, 10);
    /** East of the core: touches the core only. */
    private static final BlockPos GENERATOR_ON_CORE = CORE.east();
    /** Core of the chain test's Processor (3 x 2 x 2), clear of the Sequencer at {@link #CORE}. */
    private static final BlockPos PROCESSOR_CORE = new BlockPos(5, FLOOR_Y, 4);
    /** Core of the chain test's Incubator (2 x 2 x 1), clear of the other two machines. */
    private static final BlockPos INCUBATOR_CORE = new BlockPos(17, FLOOR_Y, 4);
    private static final int SETTLE_TICKS = 40;
    private static final int AFTER_TOGGLE_TICKS = 5;
    private static final String[] LEVEL_FIELDS = {"waterLevel", "boneMatterLevel", "sugarLevel", "plantMatterLevel"};
    /**
     * Fossils the chain needs for a full genome. A PRISTINE fossil yields 30 % DNA
     * ({@code FossilsConfig} quality {@code pristine}, {@code dna_yield} 30). The diamond filter has
     * efficiency 1, scaled by its wear (0.75 x remaining durability + 0.25, rounded to 2 places), so the
     * tubes hold 0.30, 0.30, 0.30 and 0.29: 1.19, which the drive caps at 1. Three would give 0.90.
     */
    private static final int FOSSIL_COUNT = 4;
    /** The Sequencer screen's main DNA slider at its top, as a player sets it for one species. */
    private static final double FULL_GENOME_PERCENT = 100D;
    private static final int MATTER_STACK = 16;
    /** The Incubator's first egg slot (container slots 0 to 8 hold eggs, 9 holds plant matter). */
    private static final int EGG_SLOT = 0;
    private static final Identifier UNINCUBATED_EGG_RECIPE = Constants.modLoc("unincubated_egg");
    /** The four machines of {@link #generatorOnPartFillsEachMachine}, placed west to east. */
    private static final List<Supplier<Block>> MACHINES = List.of(BlockInit.PROCESSOR,
            BlockInit.SEQUENCER, BlockInit.EGG_PRINTER, BlockInit.INCUBATOR);
    /** Core of the first machine of {@link #generatorOnPartFillsEachMachine}. */
    private static final BlockPos FIRST_MACHINE_CORE = new BlockPos(4, FLOOR_Y, 4);
    /** East step between two cores: 2 machine columns and a gap, so no generator touches two. */
    private static final int MACHINE_SPACING = 6;

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
     * Generator against the core block, no inputs: the core stores more than one synthesis tick of energy.
     */
    public static void sequencerCoreReceivesEnergy(GameTestHelper helper) {
        SequencerBlockEntity sequencer = placeSequencer(helper, GENERATOR_ON_CORE);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            String now = readings(sequencer);
            Constants.LOG.info("[machinetest] coreEnergy after {} ticks: {}", SETTLE_TICKS, now);
            long stored = sequencer.getEnergyStorage().getEnergy();
            helper.succeedIf(() -> helper.assertTrue(stored > sequencer.calculateEnergyConsumption(),
                    "the core stored only " + stored + " FE; " + now));
        });
    }

    /**
     * A creative generator against one non-core block of each machine fills that machine's
     * energy. Only the core has the block entity that stores energy, so a generator, or a cable
     * from another mod, that touches any other block must reach the core through it.
     * <p>
     * Each generator touches the machine's far corner (top row, last column, last depth) from the
     * west, so the way to the core undoes all three offsets. Before the generators run, the test
     * also offers 1 FE at that face through the loader's energy lookup
     * ({@code IPlatformHelper.insertEnergy}, a simulation), the path a cable from another mod
     * takes: the machine must accept it. The test passes when every machine accepted that offer
     * and is full; a failure lists every machine that did not.
     * <p>
     * <b>Time limit</b> 200 ticks, as the other machine tests: a creative generator pushes 256 FE
     * a tick, so each 1000 FE store is full after 4 ticks.
     */
    public static void generatorOnPartFillsEachMachine(GameTestHelper helper) {
        List<EnergyStorage> storages = new ArrayList<>();
        int[] lookupAccepted = new int[MACHINES.size()];
        for (int i = 0; i < MACHINES.size(); i++) {
            Block block = MACHINES.get(i).get();
            BlockPos core = FIRST_MACHINE_CORE.east(i * MACHINE_SPACING);
            storages.add(energyOf(helper, placeMachine(helper, block, core, BlockEntity.class)));
            BlockPos corner = farCorner(block, core);
            lookupAccepted[i] = Services.PLATFORM.insertEnergy(helper.getLevel(),
                    helper.absolutePos(corner), Direction.WEST, 1, true);
            helper.setBlock(corner.west(), BlockInit.CREATIVE_GENERATOR.get().defaultBlockState());
        }
        helper.succeedWhen(() -> {
            StringBuilder failures = new StringBuilder();
            for (int i = 0; i < storages.size(); i++) {
                EnergyStorage storage = storages.get(i);
                if (lookupAccepted[i] != 1 || storage.getEnergy() != storage.getCapacity()) {
                    failures.append(MACHINES.get(i).get()).append(": the loader lookup accepted ")
                            .append(lookupAccepted[i]).append(" of 1 FE, the generator filled ")
                            .append(storage.getEnergy()).append(" of ")
                            .append(storage.getCapacity()).append(" FE; ");
                }
            }
            helper.assertTrue(failures.isEmpty(), "at a non-core block, " + failures);
        });
    }

    private static EnergyStorage energyOf(GameTestHelper helper, BlockEntity core) {
        if (!(core instanceof BalmEnergyStorageProvider provider)) {
            throw helper.assertionException(Component.literal(core + " stores no energy"));
        }
        return provider.getEnergyStorage();
    }

    /** The block of a north-facing machine farthest from its core (top row, last column, depth). */
    private static BlockPos farCorner(Block block, BlockPos core) {
        AABB footprint = ((MultiEntityBlock) block).getFootprint(core,
                block.defaultBlockState().setValue(MultiBlock.FACING, Direction.NORTH));
        return core.above((int) footprint.getYsize() - 1)
                .relative(Direction.WEST, (int) footprint.getXsize() - 1)
                .relative(Direction.SOUTH, (int) footprint.getZsize() - 1);
    }

    /**
     * Fossil to incubated egg (lead, ruling P185): the machine chain a player runs, driven by inserting
     * items wherever the code allows it. The test passes when the Incubator holds the incubated egg
     * that its own code makes.
     * <ol>
     * <li>Processor: {@link #FOSSIL_COUNT} PRISTINE T. rex fossils (built as a mined fossil is), as many
     * empty test tubes, a diamond filter, the dev computer chip, and water filled through
     * {@code getFluidTank()} to the 2000 mB capacity. One bucket (1000 mB) is not enough: each run
     * uses 250 mB, so the 4th run reaches exactly 0 mB on its last progress tick and its finish tick
     * needs more than 0.</li>
     * <li>Sequencer: the dev SSD and the dev chip; each DNA tube the Processor makes moves into its DNA
     * input when that slot is empty, as a hopper would move it, until the drive holds 100 %. The
     * synthesis inputs are the ones the other Sequencer tests use.</li>
     * <li>Incubator: plant matter, the dev bulb, and the unincubated egg in egg slot 0.</li>
     * </ol>
     * Each machine has a creative generator against its core block, as a player would power it; only
     * the Sequencer reads its energy today.
     * <p>
     * <b>Lead-approved shortcuts</b> (P185), where a player clicks in a screen or a crafting grid:
     * <ul>
     * <li>The species choice in the Sequencer screen: the test calls {@code setDinoData} with the
     * sequenced species at 100 %, as {@code UpdateEditInfoPacket} does on the server.</li>
     * <li>The Sequencer's Begin button: the test calls {@code toggleSynth()}, as the
     * {@code SequencerMenu} button does on the server.</li>
     * <li>The crafting step: the test asks the server's recipe manager for the crafting recipe that
     * matches an artificial egg and the DinoData tube, checks that it is
     * {@code projectnublar:unincubated_egg}, and calls its {@code assemble}.</li>
     * </ul>
     * <b>Items</b> (P185, P186): the dev chip, dev SSD and dev bulb, the fastest of each. The
     * Processor gets a diamond filter instead of the dev filter: the dev filter has no durability, so
     * {@code ProcessorBlockEntity.getFilterEfficiency} divides 0 by 0, and the NaN makes the finish
     * step throw. That bug goes to the lead separately.
     * <p>
     * <b>Time limit</b> (the {@code max_ticks} in {@code ModGameTestProvider}): 1.5 x the sum of the
     * fastest stage times, from the item and block entity constants:
     * <ul>
     * <li>Processor: 4 runs x (10 progress ticks of the dev chip + 1 finish tick) = 44;</li>
     * <li>sequencing: 4 tubes x 10 ticks (dev SSD) = 40;</li>
     * <li>synthesis: the dev chip's 10 ticks, done when the count passes 10: 11;</li>
     * <li>incubation: 0.01 per step, one step every 2 ticks (dev bulb); 100 steps reach 1 (the double
     * sum is 1.0000000000000007) and 1 more step makes the incubated egg: 101 x 2 = 202.</li>
     * </ul>
     * Sum 297; x 1.5 = 445.5, rounded up to 450.
     */
    public static void machineChainSmoke(GameTestHelper helper) {
        ProcessorBlockEntity processor = placeMachine(helper, BlockInit.PROCESSOR.get(), PROCESSOR_CORE,
                ProcessorBlockEntity.class);
        SequencerBlockEntity sequencer = placeMachine(helper, BlockInit.SEQUENCER.get(), CORE,
                SequencerBlockEntity.class);
        IncubatorBlockEntity incubator = placeMachine(helper, BlockInit.INCUBATOR.get(), INCUBATOR_CORE,
                IncubatorBlockEntity.class);
        for (BlockPos core : List.of(PROCESSOR_CORE, CORE, INCUBATOR_CORE)) {
            helper.setBlock(core.east(), BlockInit.CREATIVE_GENERATOR.get().defaultBlockState());
        }
        DNAData fossilDna = pristineFossilDna();
        loadProcessor(processor, fossilDna);
        sequencer.setItem(SequencerBlockEntity.SLOT_STORAGE, new ItemStack(ItemInit.DEV_SSD.get()));
        sequencer.setChip(new ItemStack(ItemInit.DEV_COMPUTER_CHIP.get()));
        fillSynthInputs(sequencer);
        incubator.setItem(IncubatorBlockEntity.SLOT_PLANT_MATTER,
                new ItemStack(Items.OAK_LEAVES, MATTER_STACK));
        incubator.setBulbStack(new ItemStack(ItemInit.DEV_BULB.get()));

        boolean[] synthesisStarted = {false};
        helper.onEachTick(() -> {
            moveDnaTube(processor, sequencer);
            DNAData genome = fullGenome(sequencer, fossilDna.getStorageName());
            if (!synthesisStarted[0] && genome != null && sequencer.canSynth()) {
                startSynthesis(sequencer, genome);
                synthesisStarted[0] = true;
            }
            incubateSynthesizedTube(helper, sequencer, incubator);
        });
        helper.succeedWhen(() -> helper.assertTrue(
                incubator.getItem(EGG_SLOT).is(ItemInit.INCUBATED_EGG.get()),
                "no incubated egg in the Incubator yet; " + chainReadings(processor, sequencer, incubator)));
    }

    /** Fossils for the Processor, with the DNA data a mined fossil gets ({@code FossilItemFunction}). */
    private static DNAData pristineFossilDna() {
        DNAData dna = new DNAData();
        dna.setEntityType(EntityInit.TYRANNOSAURUS_REX.get());
        dna.setQuality(Quality.PRISTINE);
        dna.setFossilPiece(FossilPieces.TYRANNOSAURUS_SKULL);
        return dna;
    }

    private static void loadProcessor(ProcessorBlockEntity processor, DNAData fossilDna) {
        processor.setItem(ProcessorBlockEntity.SLOT_FILTER, new ItemStack(ItemInit.DIAMOND_FILTER.get()));
        processor.setItem(ProcessorBlockEntity.SLOT_CHIP_UPGRADE,
                new ItemStack(ItemInit.DEV_COMPUTER_CHIP.get()));
        FluidTank tank = processor.getFluidTank();
        tank.fill(0, Fluids.WATER, tank.getCapacity(0), false);
        processor.setItem(ProcessorBlockEntity.SLOT_TEST_TUBE,
                new ItemStack(ItemInit.TEST_TUBE_ITEM.get(), FOSSIL_COUNT));
        ItemStack fossils = new ItemStack(ItemInit.FOSSIL_ITEM.get(), FOSSIL_COUNT);
        fossilDna.saveTo(fossils);
        processor.setItem(ProcessorBlockEntity.SLOT_INPUT, fossils);
    }

    /** Moves the first DNA tube stack from the Processor's output into the Sequencer's empty DNA input. */
    private static void moveDnaTube(ProcessorBlockEntity processor, SequencerBlockEntity sequencer) {
        if (!sequencer.getItem(SequencerBlockEntity.SLOT_DNA_INPUT).isEmpty()) {
            return;
        }
        int end = ProcessorBlockEntity.OUTPUT_SLOT_START + ProcessorBlockEntity.OUTPUT_SLOT_COUNT;
        for (int slot = ProcessorBlockEntity.OUTPUT_SLOT_START; slot < end; slot++) {
            if (!processor.getItem(slot).isEmpty()) {
                ItemStack tubes = processor.removeItem(slot, processor.getItem(slot).getCount());
                Constants.LOG.info("[machinetest] chain: {} moved to the sequencer, {}", tubes,
                        DNAData.fromStack(tubes).getFormattedDNA().getString());
                sequencer.setItem(SequencerBlockEntity.SLOT_DNA_INPUT, tubes);
                return;
            }
        }
    }

    /** The drive's sample of the sequenced species once it reaches 100 %; {@code null} before. */
    private static @Nullable DNAData fullGenome(SequencerBlockEntity sequencer, String storageName) {
        DiskStorage drive = DiskStorage.fromStack(sequencer.getItem(SequencerBlockEntity.SLOT_STORAGE));
        DNAData sample = drive.get(storageName);
        return sample != null && sample.getDnaPercentage() >= 1 ? sample : null;
    }

    /** The two lead-approved screen shortcuts, the species choice and Begin ({@link #machineChainSmoke}). */
    private static void startSynthesis(SequencerBlockEntity sequencer, DNAData genome) {
        DinoData choice = new DinoData();
        choice.setBaseDino(genome.getEntityType());
        choice.addEntity(genome.getEntityInfo(), FULL_GENOME_PERCENT);
        sequencer.setDinoData(choice);
        sequencer.toggleSynth();
        Constants.LOG.info("[machinetest] chain: synthesis started, isSynthesizing={}",
                sequencer.isSynthesizing());
    }

    /** Crafts the Sequencer's DinoData tube into an unincubated egg and puts it into {@link #EGG_SLOT}. */
    private static void incubateSynthesizedTube(GameTestHelper helper, SequencerBlockEntity sequencer,
                                                IncubatorBlockEntity incubator) {
        if (sequencer.getItem(SequencerBlockEntity.SLOT_DNA_TEST_TUBE_OUTPUT).isEmpty()
                || !incubator.getItem(EGG_SLOT).isEmpty()) {
            return;
        }
        ItemStack tube = sequencer.removeItem(SequencerBlockEntity.SLOT_DNA_TEST_TUBE_OUTPUT, 1);
        ItemStack egg = craftUnincubatedEgg(helper, tube);
        Constants.LOG.info("[machinetest] chain: crafted {} from {}", egg, tube);
        incubator.setItem(EGG_SLOT, egg);
    }

    /** The lead-approved crafting shortcut: the recipe manager's match, then its {@code assemble}. */
    private static ItemStack craftUnincubatedEgg(GameTestHelper helper, ItemStack tube) {
        CraftingInput grid = CraftingInput.of(2, 1,
                List.of(new ItemStack(ItemInit.ARTIFICIAL_EGG.get()), tube));
        ServerLevel level = helper.getLevel();
        RecipeHolder<CraftingRecipe> recipe = level.recipeAccess()
                .getRecipeFor(RecipeType.CRAFTING, grid, level)
                .filter(holder -> holder.id().identifier().equals(UNINCUBATED_EGG_RECIPE))
                .orElseThrow(() -> helper.assertionException(Component.literal(
                        "no " + UNINCUBATED_EGG_RECIPE + " recipe matches an artificial egg and " + tube)));
        return recipe.value().assemble(grid);
    }

    private static String chainReadings(ProcessorBlockEntity processor, SequencerBlockEntity sequencer,
                                        IncubatorBlockEntity incubator) {
        return "processor input=" + processor.getItem(ProcessorBlockEntity.SLOT_INPUT)
                + " sequencer dnaInput=" + sequencer.getItem(SequencerBlockEntity.SLOT_DNA_INPUT)
                + " drive=" + DiskStorage.fromStack(sequencer.getItem(SequencerBlockEntity.SLOT_STORAGE))
                        .keys()
                + " isSynthesizing=" + sequencer.isSynthesizing()
                + " tubeOutput=" + sequencer.getItem(SequencerBlockEntity.SLOT_DNA_TEST_TUBE_OUTPUT)
                + " incubatorEggSlot=" + incubator.getItem(EGG_SLOT);
    }

    /** Places the Sequencer at {@link #CORE}, then a creative generator at {@code generatorPos}. */
    private static SequencerBlockEntity placeSequencer(GameTestHelper helper, BlockPos generatorPos) {
        SequencerBlockEntity sequencer = placeMachine(helper, BlockInit.SEQUENCER.get(), CORE,
                SequencerBlockEntity.class);
        helper.setBlock(generatorPos, BlockInit.CREATIVE_GENERATOR.get().defaultBlockState());
        return sequencer;
    }

    /**
     * Places every block of a multiblock machine facing north exactly as {@code MultiBlockItem.placeBlock}
     * does (same loop and offsets: up by rows, counter-clockwise of FACING by columns, opposite FACING by
     * depth), and returns the block entity of its core. The sizes come from the block's own footprint.
     */
    private static <T extends BlockEntity> T placeMachine(GameTestHelper helper, Block block, BlockPos core,
                                                          Class<T> type) {
        if (!(block instanceof MultiEntityBlock machine)) {
            throw new IllegalArgumentException(block + " is not a multiblock machine");
        }
        Direction direction = Direction.NORTH;
        BlockState state = machine.defaultBlockState().setValue(MultiBlock.FACING, direction);
        AABB footprint = machine.getFootprint(core, state);
        for (int i = 0; i < (int) footprint.getYsize(); i++) {
            for (int j = 0; j < (int) footprint.getXsize(); j++) {
                for (int k = 0; k < (int) footprint.getZsize(); k++) {
                    BlockPos pos = core.relative(Direction.UP, i).relative(direction.getCounterClockWise(), j)
                            .relative(direction.getOpposite(), k);
                    helper.setBlock(pos, state.setValue(MultiBlock.DEPTH, k).setValue(MultiBlock.COLUMNS, j)
                            .setValue(MultiBlock.ROWS, i));
                }
            }
        }
        // The level look-up, not 26.2's getBlockEntity(pos, type): that one fails with its own message.
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(core));
        if (!type.isInstance(blockEntity)) {
            throw helper.assertionException(Component.literal("no " + type.getSimpleName()
                    + " at the core " + core + ", found " + blockEntity));
        }
        return type.cast(blockEntity);
    }

    private static void fillSynthInputs(SequencerBlockEntity sequencer) {
        sequencer.setItem(SequencerBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        sequencer.setItem(SequencerBlockEntity.SLOT_BONE_MATTER,
                new ItemStack(Items.BONE_MEAL, MATTER_STACK));
        sequencer.setItem(SequencerBlockEntity.SLOT_SUGAR, new ItemStack(Items.SUGAR, MATTER_STACK));
        sequencer.setItem(SequencerBlockEntity.SLOT_PLANT_MATTER,
                new ItemStack(Items.OAK_LEAVES, MATTER_STACK));
        sequencer.setItem(SequencerBlockEntity.SLOT_EMPTY_TUBE_INPUT, new ItemStack(ItemInit.TEST_TUBE_ITEM.get()));
    }

    private static String readings(SequencerBlockEntity sequencer) {
        StringBuilder text = new StringBuilder()
                .append("energy=").append(sequencer.getEnergyStorage().getEnergy())
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
