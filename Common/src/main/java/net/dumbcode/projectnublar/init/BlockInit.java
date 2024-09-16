package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.block.EggPrinterBlock;
import net.dumbcode.projectnublar.block.IncubatorBlock;
import net.dumbcode.projectnublar.block.SequencerBlock;
import net.dumbcode.projectnublar.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.block.entity.ProcessorBlockEntity;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.block.ProcessorBlock;
import net.dumbcode.projectnublar.item.GeoMultiBlockItem;
import net.dumbcode.projectnublar.registration.RegistrationProvider;
import net.dumbcode.projectnublar.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockInit {
    public static final RegistrationProvider<Block> BLOCKS = RegistrationProvider.get(Registries.BLOCK, Constants.MODID);
    public static final RegistrationProvider<BlockEntityType<?>> BLOCK_ENTITIES = RegistrationProvider.get(Registries.BLOCK_ENTITY_TYPE, Constants.MODID);

    public static FossilCollection FOSSIL = FossilCollection.create("tyrannosaurus_rex");
    public static RegistryObject<Block> PROCESSOR = registerBlock("processor", () -> new ProcessorBlock(BlockBehaviour.Properties.of().noOcclusion(),3,2, 2), block->()-> new GeoMultiBlockItem(block.get(),ItemInit.getItemProperties(),3,2, 2));
    public static RegistryObject<Block> SEQUENCER = registerBlock("sequencer", () -> new SequencerBlock(BlockBehaviour.Properties.of().noOcclusion(),2,2, 2), block->()-> new GeoMultiBlockItem(block.get(),ItemInit.getItemProperties(),2,2, 2));
    public static RegistryObject<Block> EGG_PRINTER = registerBlock("egg_printer", () -> new EggPrinterBlock(BlockBehaviour.Properties.of().noOcclusion(),1,2, 1), block->()-> new GeoMultiBlockItem(block.get(),ItemInit.getItemProperties(),1,2, 1));
    public static RegistryObject<Block> INCUBATOR = registerBlock("incubator", () -> new IncubatorBlock(BlockBehaviour.Properties.of().noOcclusion(),2,2, 1), block->()-> new GeoMultiBlockItem(block.get(),ItemInit.getItemProperties(),2,2, 1));



    public static RegistryObject<BlockEntityType<ProcessorBlockEntity>> PROCESSOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("processor", () -> BlockEntityType.Builder.of(ProcessorBlockEntity::new, PROCESSOR.get()).build(null));
    public static RegistryObject<BlockEntityType<SequencerBlockEntity>> SEQUENCER_BLOCK_ENTITY = BLOCK_ENTITIES.register("sequencer", () -> BlockEntityType.Builder.of(SequencerBlockEntity::new, SEQUENCER.get()).build(null));
    public static RegistryObject<BlockEntityType<EggPrinterBlockEntity>> EGG_PRINTER_BLOCK_ENTITY = BLOCK_ENTITIES.register("egg_printer", () -> BlockEntityType.Builder.of(EggPrinterBlockEntity::new, EGG_PRINTER.get()).build(null));
    public static RegistryObject<BlockEntityType<IncubatorBlockEntity>> INCUBATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("incubator", () -> BlockEntityType.Builder.of(IncubatorBlockEntity::new, INCUBATOR.get()).build(null));

    public static void loadClass() {}

    public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        return registerBlock(name, block, b -> () -> new BlockItem(b.get(), ItemInit.getItemProperties()));
    }

    public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, Function<RegistryObject<T>, Supplier<? extends BlockItem>> item) {
        var reg = BLOCKS.register(name, block);
        ItemInit.ITEMS.register(name, () -> item.apply(reg).get());
        return reg;
    }
}