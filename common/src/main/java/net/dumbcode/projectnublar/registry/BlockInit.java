package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.block.LowSecurityElectricFencePostBlock;
import net.dumbcode.projectnublar.block.*;
import net.dumbcode.projectnublar.block.api.fence.EnumConnectionType;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.block.entity.GeneratorBlockEntity;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.block.entity.ProcessorBlockEntity;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.gui.widget.VanillaColorPickerWidget;
import net.dumbcode.projectnublar.item.GeoMultiBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Constants.MODID, Registries.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Constants.MODID, Registries.BLOCK_ENTITY_TYPE);




     public static FossilCollection TYRANNOSAURUS_REX_FOSSILS = FossilCollection.create(EntityInit.TYRANNOSAURUS_REX.getId());
     public static FossilCollection TRICERATOPS_FOSSILS = FossilCollection.create(EntityInit.TRICERATOPS.getId());

    public static DeferredSupplier<Block> PROCESSOR = registerBlock("processor", () -> new ProcessorBlock(BlockBehaviour.Properties.of().noOcclusion(),3,2, 2), block->()-> new GeoMultiBlockItem(block.get(),ItemInit.getItemProperties(),3,2, 2));
    public static DeferredSupplier<Block> SEQUENCER = registerBlock("sequencer", () -> new SequencerBlock(BlockBehaviour.Properties.of().noOcclusion(),2,2, 2), block->()-> new GeoMultiBlockItem(block.get(),ItemInit.getItemProperties(),2,2, 2));
    public static DeferredSupplier<Block> EGG_PRINTER = registerBlock("egg_printer", () -> new EggPrinterBlock(BlockBehaviour.Properties.of().noOcclusion(),1,2, 1), block->()-> new GeoMultiBlockItem(block.get(),ItemInit.getItemProperties(),1,2, 1));
    public static DeferredSupplier<Block> INCUBATOR = registerBlock("incubator", () -> new IncubatorBlock(BlockBehaviour.Properties.of().noOcclusion(),2,2, 1), block->()-> new GeoMultiBlockItem(block.get(),ItemInit.getItemProperties(),2,2, 1));
    public static DeferredSupplier<Block> ELECTRIC_FENCE = registerBlock("electric_fence", () -> new ElectricFenceBlock(BlockBehaviour.Properties.of().noLootTable().noOcclusion()));
   // public static DeferredSupplier<Block> ELECTRIC_FENCE;
    public static DeferredSupplier<Block> LOW_SECURITY_ELECTRIC_FENCE_POST = registerBlock("low_security_electric_fence_post", () -> new LowSecurityElectricFencePostBlock(BlockBehaviour.Properties.of().noOcclusion(), EnumConnectionType.LOW_SECURITY));
    public static DeferredSupplier<Block> HIGH_SECURITY_ELECTRIC_FENCE_POST = registerBlock("high_security_electric_fence_post", () -> new HighSecurityElectricFencePostBlock(BlockBehaviour.Properties.of().noOcclusion(), EnumConnectionType.HIGH_SECURITY));
    public static DeferredSupplier<Block> COAL_GENERATOR = registerBlock("coal_generator", ()-> new GeneratorBlock(BlockBehaviour.Properties.of(),256,16,0));
    public static DeferredSupplier<Block> CREATIVE_GENERATOR = registerBlock("creative_generator", ()-> new GeneratorBlock(BlockBehaviour.Properties.of(),99999,99999,0));


    public static DeferredSupplier<BlockEntityType<ProcessorBlockEntity>> PROCESSOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("processor", () -> BlockEntityType.Builder.of(ProcessorBlockEntity::new, PROCESSOR.get()).build(null));
    public static DeferredSupplier<BlockEntityType<SequencerBlockEntity>> SEQUENCER_BLOCK_ENTITY = BLOCK_ENTITIES.register("sequencer", () -> BlockEntityType.Builder.of(SequencerBlockEntity::new, SEQUENCER.get()).build(null));
    public static DeferredSupplier<BlockEntityType<EggPrinterBlockEntity>> EGG_PRINTER_BLOCK_ENTITY = BLOCK_ENTITIES.register("egg_printer", () -> BlockEntityType.Builder.of(EggPrinterBlockEntity::new, EGG_PRINTER.get()).build(null));
    public static DeferredSupplier<BlockEntityType<IncubatorBlockEntity>> INCUBATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("incubator", () -> BlockEntityType.Builder.of(IncubatorBlockEntity::new, INCUBATOR.get()).build(null));
    public static DeferredSupplier<BlockEntityType<BlockEntityElectricFence>> ELECTRIC_FENCE_BLOCK_ENTITY = BLOCK_ENTITIES.register("electric_fence", () -> BlockEntityType.Builder.of(BlockEntityElectricFence::new, BlockInit.ELECTRIC_FENCE.get()).build(null));
  // public static DeferredSupplier<BlockEntityType<? extends BlockEntityElectricFence>> ELECTRIC_FENCE_BLOCK_ENTITY;

    public static DeferredSupplier<BlockEntityType<BlockEntityElectricFencePole>> ELECTRIC_FENCE_POST_BLOCK_ENTITY = BLOCK_ENTITIES.register("electric_fence_pole", () -> BlockEntityType.Builder.of(BlockEntityElectricFencePole::new, BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get(),BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get()).build(null));
    public static DeferredSupplier<BlockEntityType<GeneratorBlockEntity>> GENERATOR = BLOCK_ENTITIES.register("coal_generator", ()-> BlockEntityType.Builder.of(GeneratorBlockEntity::new, COAL_GENERATOR.get(), CREATIVE_GENERATOR.get()).build(null));
    public static void loadClass() {
        BLOCKS.register();
        BLOCK_ENTITIES.register();}

    public static <T extends Block> DeferredSupplier<T> registerBlock(String name, Supplier<T> block) {
        return registerBlock(name, block, b -> () -> new BlockItem(b.get(), ItemInit.getItemProperties()));
    }

    public static <T extends Block> DeferredSupplier<T> registerBlock(String name, Supplier<T> block, Function<DeferredSupplier<T>, Supplier<? extends BlockItem>> item) {
        var reg = BLOCKS.register(name, block);
        ItemInit.ITEMS.register(name, () -> item.apply(reg).get());
        return reg;
    }
}