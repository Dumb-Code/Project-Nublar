package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.api.fossil.FossilPieces;
import net.dumbcode.projectnublar.block.EggPrinterBlock;
import net.dumbcode.projectnublar.block.ElectricFenceBlock;
import net.dumbcode.projectnublar.block.GeneratorBlock;
import net.dumbcode.projectnublar.block.HighSecurityElectricFencePostBlock;
import net.dumbcode.projectnublar.block.IncubatorBlock;
import net.dumbcode.projectnublar.block.LowSecurityElectricFencePostBlock;
import net.dumbcode.projectnublar.block.DinosaurFeederBlock;
import net.dumbcode.projectnublar.block.ProcessorBlock;
import net.dumbcode.projectnublar.block.SequencerBlock;
import net.dumbcode.projectnublar.block.api.fence.EnumConnectionType;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.block.entity.GeneratorBlockEntity;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.block.entity.ProcessorBlockEntity;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.item.GeoMultiBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Registers every mod block and block-entity type, plus the per-species fossil block collections.
 *
 * <p>The registered id strings are registry contracts and must never change. The
 * {@link FossilCollection} field must stay declared here and in this position: its creation runs
 * during class initialization and registers the fossil blocks, their items and the amber blocks
 * as a side effect.
 *
 * <p>On 26.2 a block's properties must carry its registry key before the block is built, and so
 * must its item's. {@link #registerBlock} is the only place here that builds block properties.
 */
public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Constants.MODID, Registries.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Constants.MODID, Registries.BLOCK_ENTITY_TYPE);

    public static FossilCollection TYRANNOSAURUS_REX_FOSSILS = FossilCollection.create(
            EntityInit.TYRANNOSAURUS_REX.getId(), "cretaceous", FossilPieces.TYRANNOSAURUS_PIECES);

    // Machines are multiblocks: the trailing ints are (rows, columns, depth) of the structure
    // and are mirrored in each block's GeoMultiBlockItem.
    public static DeferredSupplier<Block> PROCESSOR = registerBlock("processor",
            properties -> new ProcessorBlock(properties.noOcclusion(), 3, 2, 2),
            block -> () -> new GeoMultiBlockItem(block.get(),
                    ItemInit.getItemProperties("processor").useBlockDescriptionPrefix(), 3, 2, 2));
    public static DeferredSupplier<Block> SEQUENCER = registerBlock("sequencer",
            properties -> new SequencerBlock(properties.noOcclusion(), 2, 2, 2),
            block -> () -> new GeoMultiBlockItem(block.get(),
                    ItemInit.getItemProperties("sequencer").useBlockDescriptionPrefix(), 2, 2, 2));
    public static DeferredSupplier<Block> EGG_PRINTER = registerBlock("egg_printer",
            properties -> new EggPrinterBlock(properties.noOcclusion(), 1, 2, 1),
            block -> () -> new GeoMultiBlockItem(block.get(),
                    ItemInit.getItemProperties("egg_printer").useBlockDescriptionPrefix(), 1, 2, 1));
    public static DeferredSupplier<Block> INCUBATOR = registerBlock("incubator",
            properties -> new IncubatorBlock(properties.noOcclusion(), 2, 2, 1),
            block -> () -> new GeoMultiBlockItem(block.get(),
                    ItemInit.getItemProperties("incubator").useBlockDescriptionPrefix(), 2, 2, 1));

    public static DeferredSupplier<Block> ELECTRIC_FENCE = registerBlock("electric_fence",
            properties -> new ElectricFenceBlock(properties.noLootTable().noOcclusion()));
    public static DeferredSupplier<Block> LOW_SECURITY_ELECTRIC_FENCE_POST =
            registerBlock("low_security_electric_fence_post",
                    properties -> new LowSecurityElectricFencePostBlock(
                            properties.noOcclusion(), EnumConnectionType.LOW_SECURITY));
    public static DeferredSupplier<Block> HIGH_SECURITY_ELECTRIC_FENCE_POST =
            registerBlock("high_security_electric_fence_post",
                    properties -> new HighSecurityElectricFencePostBlock(
                            properties.noOcclusion(), EnumConnectionType.HIGH_SECURITY));

    // Generators: (maxEnergy, energyOutput, energyInput).
    public static DeferredSupplier<Block> COAL_GENERATOR = registerBlock("coal_generator",
            properties -> new GeneratorBlock(properties, 256, 16, 0));
    public static DeferredSupplier<Block> CREATIVE_GENERATOR = registerBlock("creative_generator",
            properties -> new GeneratorBlock(properties, 99999, 99999, 0));

    public static DeferredSupplier<BlockEntityType<ProcessorBlockEntity>> PROCESSOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("processor",
                    () -> new BlockEntityType<>(ProcessorBlockEntity::new, Set.of(PROCESSOR.get())));
    public static DeferredSupplier<BlockEntityType<SequencerBlockEntity>> SEQUENCER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sequencer",
                    () -> new BlockEntityType<>(SequencerBlockEntity::new, Set.of(SEQUENCER.get())));
    public static DeferredSupplier<BlockEntityType<EggPrinterBlockEntity>> EGG_PRINTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("egg_printer",
                    () -> new BlockEntityType<>(EggPrinterBlockEntity::new, Set.of(EGG_PRINTER.get())));
    public static DeferredSupplier<BlockEntityType<IncubatorBlockEntity>> INCUBATOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("incubator",
                    () -> new BlockEntityType<>(IncubatorBlockEntity::new, Set.of(INCUBATOR.get())));
    public static DeferredSupplier<BlockEntityType<BlockEntityElectricFence>> ELECTRIC_FENCE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("electric_fence",
                    () -> new BlockEntityType<>(BlockEntityElectricFence::new, Set.of(BlockInit.ELECTRIC_FENCE.get())));
    public static DeferredSupplier<BlockEntityType<BlockEntityElectricFencePole>> ELECTRIC_FENCE_POST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("electric_fence_pole",
                    () -> new BlockEntityType<>(BlockEntityElectricFencePole::new, Set.of(
                            BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get(),
                            BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get())));
    public static DeferredSupplier<BlockEntityType<GeneratorBlockEntity>> GENERATOR =
            BLOCK_ENTITIES.register("coal_generator",
                    () -> new BlockEntityType<>(GeneratorBlockEntity::new,
                            Set.of(COAL_GENERATOR.get(), CREATIVE_GENERATOR.get())));

    // Feeders.
    public static DeferredSupplier<Block> CARNIVORE_FEEDER_ONE = registerBlock("carnivore_feeder_one",
            properties -> new DinosaurFeederBlock(properties.noOcclusion(), "feeder/meat/feeder_one"));
    // public static DeferredSupplier<Block> CARNIVORE_FEEDER_TWO = registerBlock("carnivore_feeder_two", properties -> new DinosaurFeederBlock(properties.noOcclusion(), "feeder/meat/feeder_two"));
    // The id "carrnivore_feeder_one" (sic) is a frozen registry id.
    public static DeferredSupplier<BlockEntityType<DinosaurFeederBlockEntity>> FEEDER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("carrnivore_feeder_one",
                    () -> new BlockEntityType<>(DinosaurFeederBlockEntity::new, Set.of(CARNIVORE_FEEDER_ONE.get())));

    public static void loadClass() {
        BLOCKS.register();
        BLOCK_ENTITIES.register();
    }

    /** The registry key of the mod block {@code name}; block properties must carry it on 26.2. */
    public static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, Constants.modLoc(name));
    }

    /** Registers a block with a plain {@link BlockItem} under the same id. */
    public static DeferredSupplier<Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> block) {
        return registerBlock(name, block,
                b -> () -> new BlockItem(b.get(), ItemInit.getItemProperties(name).useBlockDescriptionPrefix()));
    }

    /**
     * Registers a block together with its item under the same id.
     *
     * @param block builds the block from fresh properties that already carry the block key
     * @param item builds the item; it must use {@code ItemInit.getItemProperties(name)} with
     *     {@code useBlockDescriptionPrefix()}, so the item has the block's name as in 1.20.1
     */
    public static DeferredSupplier<Block> registerBlock(
            String name,
            Function<BlockBehaviour.Properties, Block> block,
            Function<RegistrySupplier<Block>, Supplier<Item>> item) {
        RegistrySupplier<Block> reg =
                BLOCKS.register(name, () -> block.apply(BlockBehaviour.Properties.of().setId(blockKey(name))));
        ItemInit.ITEMS.register(name, () -> item.apply(reg).get());
        return reg;
    }
}
