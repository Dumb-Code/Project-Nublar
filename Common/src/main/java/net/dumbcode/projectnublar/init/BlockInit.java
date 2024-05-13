package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.block.SequencerBlock;
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
    public static RegistryObject<Block> PROCESSOR = registerBlock("processor", () -> new ProcessorBlock(BlockBehaviour.Properties.of().noOcclusion()), block->()-> new GeoMultiBlockItem(block.get(),ItemInit.getItemProperties(),3,2, 2));
    public static RegistryObject<Block> SEQUENCER = registerBlock("sequencer", () -> new SequencerBlock(BlockBehaviour.Properties.of().noOcclusion()), block->()-> new GeoMultiBlockItem(block.get(),ItemInit.getItemProperties(),3,2, 2));



    public static RegistryObject<BlockEntityType<ProcessorBlockEntity>> PROCESSOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("processor", () -> BlockEntityType.Builder.of(ProcessorBlockEntity::new, PROCESSOR.get()).build(null));
    public static RegistryObject<BlockEntityType<SequencerBlockEntity>> SEQUENCER_BLOCK_ENTITY = BLOCK_ENTITIES.register("sequencer", () -> BlockEntityType.Builder.of(SequencerBlockEntity::new, SEQUENCER.get()).build(null));

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