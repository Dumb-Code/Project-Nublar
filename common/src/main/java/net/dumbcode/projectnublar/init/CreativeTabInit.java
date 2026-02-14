package net.dumbcode.projectnublar.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Constants.MODID, Registries.CREATIVE_MODE_TAB);
    public static final DeferredSupplier<CreativeModeTab> FOSSIL_ORES_TAB = CREATIVE_MODE_TABS.register(Constants.MODID + "_fossil_ores", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MODID + ".fossil_ores"))
            .icon(() -> {
                Block block = FossilCollection.COLLECTIONS.get("projectnublar:tyrannosaurus_rex").fossilblocks().get(Blocks.STONE).get(Quality.PRISTINE).get(FossilPieces.getPieceByName("rex_skull")).get();
                ItemStack stack = new ItemStack(block);
                return stack;
            })
            .displayItems(
                    (itemDisplayParameters, output) -> {
                        FossilCollection.COLLECTIONS.forEach((entity, fossilCollection) -> {
                            fossilCollection.fossilblocks().forEach((block, qualityMap) -> {
                                qualityMap.forEach((quality, stoneMap) -> {
                                    stoneMap.forEach((piece, blockDeferredSupplier) -> {

                                        ///TO-DO: Replace with datapack
                                     //   FossilsConfig.Fossil fossil = FossilsConfig.getFossils().get(entity);
                                    //    if (FossilsConfig.getSet(fossil.getPieces().get()).pieces.get().contains(piece.name()) || fossil.getSpecial_pieces().get().contains(piece.name())) {
                                       //     ItemStack stack = new ItemStack(blockDeferredSupplier.get());
                                          //  stack.getOrCreateTag().putString("quality", quality.getName());
                                           // output.accept(stack);
                                      //  }
                                    });
                                });
                            });
                         //   fossilCollection.amberBlocks().forEach((block, blockDeferredSupplier) -> output.accept(blockDeferredSupplier.get()));
                        });
                    })
            .build());
    public static final DeferredSupplier<CreativeModeTab> MACHINES_TAB = CREATIVE_MODE_TABS.register(Constants.MODID + "_machines", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MODID + ".machines"))
            .icon(() -> new ItemStack(BlockInit.PROCESSOR.get()))
            .displayItems(
                    (itemDisplayParameters, output) -> {
                        output.accept(BlockInit.PROCESSOR.get());
                        output.accept(BlockInit.SEQUENCER.get());
                        output.accept(ItemInit.SEQUENCER_COMPUTER.get());
                        output.accept(ItemInit.SEQUENCER_DOOR.get());
                        output.accept(ItemInit.SEQUENCER_SCREEN.get());
                        output.accept(BlockInit.EGG_PRINTER.get());
                        output.accept(BlockInit.INCUBATOR.get());
                        output.accept(ItemInit.INCUBATOR_ARM.get());
                        output.accept(ItemInit.INCUBATOR_ARM_BASE.get());
                        output.accept(ItemInit.INCUBATOR_LID.get());
                        output.accept(ItemInit.INCUBATOR_NEST.get());
                        output.accept(BlockInit.COAL_GENERATOR.get());
                        output.accept(BlockInit.CREATIVE_GENERATOR.get());
                        
                    })
            .build());
    public static final DeferredSupplier<CreativeModeTab> MISC_TAB = CREATIVE_MODE_TABS.register(Constants.MODID + "_misc", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MODID + ".misc"))
            .icon(() -> new ItemStack(ItemInit.IRON_FILTER.get()))
            .displayItems(
                    (itemDisplayParameters, output) -> {
                        output.accept(ItemInit.IRON_FILTER.get());
                        output.accept(ItemInit.GOLD_FILTER.get());
                        output.accept(ItemInit.DIAMOND_FILTER.get());
                        output.accept(ItemInit.IRON_TANK_UPGRADE.get());
                        output.accept(ItemInit.GOLD_TANK_UPGRADE.get());
                        output.accept(ItemInit.DIAMOND_TANK_UPGRADE.get());
                        output.accept(ItemInit.IRON_COMPUTER_CHIP.get());
                        output.accept(ItemInit.GOLD_COMPUTER_CHIP.get());
                        output.accept(ItemInit.DIAMOND_COMPUTER_CHIP.get());
                        output.accept(ItemInit.HARD_DRIVE.get());
                        output.accept(ItemInit.SSD.get());
                        output.accept(ItemInit.SMALL_CONTAINER_UPGRADE.get());
                        output.accept(ItemInit.LARGE_CONTAINER_UPGRADE.get());
                        output.accept(ItemInit.WARM_BULB.get());
                        output.accept(ItemInit.WARMER_BULB.get());
                        output.accept(ItemInit.HOT_BULB.get());
                        output.accept(ItemInit.IRON_PLANT_TANK.get());
                        output.accept(ItemInit.GOLD_PLANT_TANK.get());
                        output.accept(BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get());
                        output.accept(BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get());
                        output.accept(ItemInit.WIRE_SPOOL.get());

                    })
            .build());

    public static final DeferredSupplier<CreativeModeTab> DNA_TAB = CREATIVE_MODE_TABS.register(Constants.MODID + "_dna", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MODID + ".dna"))
            .icon(() -> new ItemStack(ItemInit.TEST_TUBE_ITEM.get()))
            .displayItems(
                    (itemDisplayParameters, output) -> {
                        output.accept(ItemInit.TEST_TUBE_ITEM.get());
                        EntityInit.ENTITIES.forEach(type ->  {
                            ItemStack stack = new ItemStack(ItemInit.TEST_TUBE_ITEM.get());
                            DNAData dnaData = new DNAData();
                            dnaData.setEntityType(type.get());
                            dnaData.setDnaPercentage(0.5);
                            stack.getOrCreateTag().put("DNAData", dnaData.saveToNBT(new CompoundTag()));
                            output.accept(stack);
                        });
                    })
            .build());

    public static final DeferredSupplier<CreativeModeTab> SYRINGE_TAB = CREATIVE_MODE_TABS.register(Constants.MODID + "_syringe", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MODID + ".syringe"))
            .icon(() -> new ItemStack(ItemInit.SYRINGE.get()))
            .displayItems(
                    (itemDisplayParameters, output) -> {
                        output.accept(ItemInit.SYRINGE.get());
                        for (ResourceLocation entry : BuiltInRegistries.ENTITY_TYPE.keySet()) {
                            if (entry.getPath().contains("parrot")) {
                                for (Parrot.Variant variant : Parrot.Variant.values()) {
                                    ItemStack stack = new ItemStack(ItemInit.SYRINGE.get());
                                    DNAData dnaData = new DNAData();
                                    dnaData.setEntityType(BuiltInRegistries.ENTITY_TYPE.get(entry));
                                    dnaData.setVariant(variant.getSerializedName());
                                    dnaData.setDnaPercentage(1.0);
                                    stack.getOrCreateTag().put("DNAData", dnaData.saveToNBT(new CompoundTag()));
                                    output.accept(stack);
                                }
                            } else if (entry.getPath().contains("cat")) {
                                BuiltInRegistries.CAT_VARIANT.keySet().forEach((catVariant) -> {
                                    ItemStack stack = new ItemStack(ItemInit.SYRINGE.get());
                                    DNAData dnaData = new DNAData();
                                    dnaData.setEntityType(BuiltInRegistries.ENTITY_TYPE.get(entry));
                                    dnaData.setVariant(catVariant.toString());
                                    dnaData.setDnaPercentage(0.5);
                                    stack.getOrCreateTag().put("DNAData", dnaData.saveToNBT(new CompoundTag()));
                                    output.accept(stack);
                                });
                            } else {
                                if(BuiltInRegistries.ENTITY_TYPE.get(entry).create(Minecraft.getInstance().level) instanceof LivingEntity) {
                                    ItemStack stack = new ItemStack(ItemInit.SYRINGE.get());
                                    DNAData dnaData = new DNAData();
                                    dnaData.setEntityType(BuiltInRegistries.ENTITY_TYPE.get(entry));
                                    dnaData.setDnaPercentage(0.5);
                                    stack.getOrCreateTag().put("DNAData", dnaData.saveToNBT(new CompoundTag()));
                                    output.accept(stack);
                                }
                            }
                        }
                    })
            .build());
    public static final DeferredSupplier<CreativeModeTab> EGG_TAB = CREATIVE_MODE_TABS.register(Constants.MODID + "_egg", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MODID + ".egg"))
            .icon(() -> new ItemStack(ItemInit.ARTIFICIAL_EGG.get()))
            .displayItems(
                    (itemDisplayParameters, output) -> {
                        output.accept(ItemInit.ARTIFICIAL_EGG.get());
                        ItemStack femaleTrexEggItem = new ItemStack(ItemInit.INCUBATED_EGG.get());
                        ItemStack maleTrexEggItem = new ItemStack(ItemInit.INCUBATED_EGG.get());

                        ItemStack triceratopsEggItem = new ItemStack(ItemInit.INCUBATED_EGG.get());

                        ItemStack femaleTrexEggItemCopy = new ItemStack(ItemInit.UNINCUBATED_EGG.get());
                        ItemStack maleTrexEggItemCopy = new ItemStack(ItemInit.UNINCUBATED_EGG.get());

                        ItemStack triceratopsEggItemCopy = new ItemStack(ItemInit.UNINCUBATED_EGG.get());

                        DinoData dnaData = new DinoData();

                        //Female Tyrannosaurus
                        dnaData.setBaseDino(EntityInit.TYRANNOSAURUS_REX.get());
                        dnaData.setGeneValue(GeneInit.GENDER.get(), 1.0D);
                        dnaData.setBasePercentage(100);
                        dnaData.toStack(femaleTrexEggItem);
                        dnaData.copy().toStack(femaleTrexEggItemCopy);
                        output.accept(femaleTrexEggItem);
                        output.accept(femaleTrexEggItemCopy);
                        dnaData = new DinoData();

                        //Male
                        dnaData.setBaseDino(EntityInit.TYRANNOSAURUS_REX.get());
                        dnaData.setGeneValue(GeneInit.GENDER.get(), 2.0D);
                        dnaData.setBasePercentage(100);
                        dnaData.toStack(maleTrexEggItem);
                        dnaData.copy().toStack(maleTrexEggItemCopy);
                        output.accept(maleTrexEggItem);
                        output.accept(maleTrexEggItemCopy);
                        dnaData = new DinoData();


                        dnaData.setBaseDino(EntityInit.TRICERATOPS.get());
                        dnaData.setBasePercentage(100);
                        dnaData.toStack(triceratopsEggItem);
                        dnaData.copy().toStack(triceratopsEggItemCopy);
                        output.accept(triceratopsEggItem);
                        output.accept(triceratopsEggItemCopy);
                    })
            .build());

    public static void loadClass() {
        CREATIVE_MODE_TABS.register();
    }
}
