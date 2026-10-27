package net.dumbcode.projectnublar.datagen;

import com.geckolib.renderer.internal.GeckolibItemSpecialRenderer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.api.fossil.FossilPiece;
import net.dumbcode.projectnublar.api.fossil.FossilPieces;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.client.model.FossilTextureProperty;
import net.dumbcode.projectnublar.client.model.TestTubeOverlayProperty;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.dumbcode.projectnublar.registry.EntityInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.DelegatedModel;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Generates the block states, block models, item models and item definitions.
 *
 * <p>It writes the same models as the two 1.20.1 providers it replaces (block states and item
 * models), in the 26.2 formats. Each item model of an item also gets the item definition
 * ({@code items/<id>.json}) that 26.2 needs to use it. Three 1.20.1 model keys have no reader on
 * 26.2 and are left out: {@code render_type} of the fossil and amber blocks (26.2 picks the layer
 * of each quad from the transparency of its texture), {@code loader} of the fossil and the test
 * tube, and {@code overrides} of the syringe. In their place, the fossil, test tube and syringe
 * definitions pick a model from the data of the stack.
 */
public class ModModelProvider extends ModelProvider {

    /** The host stone texture of a fossil or amber block. */
    private static final TextureSlot STONE = TextureSlot.create("0");
    /** The fossil or amber overlay texture, drawn over the stone. */
    private static final TextureSlot OVERLAY = TextureSlot.create("1");
    /** A fossil or amber block: {@code block/fossil_base} with its stone and its overlay. */
    private static final ModelTemplate FOSSIL_BASE =
            new ModelTemplate(Optional.of(Constants.modLoc("block/fossil_base")), Optional.empty(), STONE, OVERLAY);
    /** A block that its block entity renderer draws: the model gives only the particle texture. */
    private static final ModelTemplate PARTICLE_ONLY =
            new ModelTemplate(Optional.empty(), Optional.empty(), TextureSlot.PARTICLE);

    /**
     * @param output the pack output of the data generator
     */
    public ModModelProvider(PackOutput output) {
        super(output, Constants.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        FossilCollection.COLLECTIONS.values().forEach(collection -> {
            collection.blocks().forEach(block -> fossilBlock(blockModels, (FossilBlock) block.get()));
            collection.amberBlocks().forEach((base, block) -> amberBlock(blockModels, base.getBlock(), block.get()));
        });
        particleOnlyBlock(blockModels, BlockInit.CARNIVORE_FEEDER_ONE.get(), "block/feeder/meat/feeder_one/empty");
        particleOnlyBlock(blockModels, BlockInit.PROCESSOR.get(), "block/processor");
        particleOnlyBlock(blockModels, BlockInit.SEQUENCER.get(), "block/sequencer");
        particleOnlyBlock(blockModels, BlockInit.EGG_PRINTER.get(), "block/egg_printer");
        particleOnlyBlock(blockModels, BlockInit.INCUBATOR.get(), "block/incubator");

        Stream.of(
                        ItemInit.IRON_FILTER,
                        ItemInit.IRON_COMPUTER_CHIP,
                        ItemInit.IRON_TANK_UPGRADE,
                        ItemInit.GOLD_FILTER,
                        ItemInit.GOLD_COMPUTER_CHIP,
                        ItemInit.GOLD_TANK_UPGRADE,
                        ItemInit.DIAMOND_FILTER,
                        ItemInit.DIAMOND_COMPUTER_CHIP,
                        ItemInit.DIAMOND_TANK_UPGRADE,
                        ItemInit.HARD_DRIVE,
                        ItemInit.SSD,
                        ItemInit.SEQUENCER_COMPUTER,
                        ItemInit.SEQUENCER_DOOR,
                        ItemInit.SEQUENCER_SCREEN,
                        ItemInit.CRACKED_ARTIFICIAL_EGG,
                        ItemInit.ARTIFICIAL_EGG,
                        ItemInit.SMALL_CONTAINER_UPGRADE,
                        ItemInit.LARGE_CONTAINER_UPGRADE,
                        ItemInit.WARM_BULB,
                        ItemInit.WARMER_BULB,
                        ItemInit.HOT_BULB,
                        ItemInit.INCUBATOR_ARM,
                        ItemInit.INCUBATOR_LID,
                        ItemInit.INCUBATOR_ARM_BASE,
                        ItemInit.INCUBATOR_NEST,
                        ItemInit.UNINCUBATED_EGG,
                        ItemInit.INCUBATED_EGG,
                        ItemInit.WIRE_SPOOL
                )
                .map(Supplier::get)
                .forEach(item -> itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM));
        Stream.of(
                        BlockInit.SEQUENCER,
                        BlockInit.EGG_PRINTER,
                        BlockInit.INCUBATOR,
                        BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST,
                        BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST
                )
                .map(block -> block.get().asItem())
                .forEach(item -> itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM));
        FossilCollection.COLLECTIONS.values().forEach(collection -> {
            collection.fossilItems().forEach((base, pieces) -> pieces.forEach((piece, items) -> {
                String blockId = FossilCollection.blockId(collection.entityType(), base, piece);
                items.forEach((quality, item) ->
                        parentItem(itemModels, item.get(), Constants.modLoc("block/" + blockId + "_" + quality.getName())));
            }));
            collection.amberBlocks().values().forEach(block ->
                    parentItem(itemModels, block.get().asItem(), ModelLocationUtils.getModelLocation(block.get())));
        });
        parentItem(itemModels, ItemInit.DEV_STICK.get(), Identifier.withDefaultNamespace("item/stick"));

        // The Processor's item is drawn by GeckoLib's special item renderer; its base model
        // (display transforms) is the hand-written models/item/processor.json.
        Item processor = BlockInit.PROCESSOR.get().asItem();
        itemModels.itemModelOutput.accept(processor, ItemModelUtils.specialModel(
                ModelLocationUtils.getModelLocation(processor), new GeckolibItemSpecialRenderer.Unbaked<>()));

        // One item each, whose look comes from the data of the stack.
        fossilItem(itemModels);
        testTubeItem(itemModels);
        syringeItem(itemModels);

        // The Carnivore Feeder's item uses its hand-written model models/item/carnivore_feeder_one.json.
        Item feeder = BlockInit.CARNIVORE_FEEDER_ONE.get().asItem();
        itemModels.itemModelOutput.accept(feeder, ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(feeder)));
    }

    /**
     * Every block of the mod must get a block state here, so the run fails for a new block without
     * one. These blocks have no block state and are left out:
     * <ul>
     *   <li>{@code electric_fence}: invisible; its block entity renderer draws it.</li>
     *   <li>{@code low_security_electric_fence_post}: invisible; its block entity renderer draws it.</li>
     *   <li>{@code high_security_electric_fence_post}: invisible; its block entity renderer draws it.</li>
     *   <li>{@code coal_generator}: had no block state on 1.20.1 either, and the port adds no art.</li>
     *   <li>{@code creative_generator}: had no block state on 1.20.1 either, and the port adds no art.</li>
     * </ul>
     */
    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        Set<Block> withoutBlockState = Set.of(
                BlockInit.ELECTRIC_FENCE.get(),
                BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get(),
                BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get(),
                BlockInit.COAL_GENERATOR.get(),
                BlockInit.CREATIVE_GENERATOR.get());
        return super.getKnownBlocks().filter(block -> !withoutBlockState.contains(block.value()));
    }

    /**
     * Every item of the mod must get an item definition here, so the run fails for a new item
     * without one. Leaving an item out also stops the default definition ({@code block/<id>}) that
     * the run would write for a block item. These items have no item model, no texture and no
     * definition, as on 1.20.1, and are left out (the port adds no art):
     * <ul>
     *   <li>{@code amber}: no item model or texture.</li>
     *   <li>{@code dev_filter}: no item model or texture.</li>
     *   <li>{@code dev_computer_chip}: no item model or texture.</li>
     *   <li>{@code dev_ssd}: no item model or texture.</li>
     *   <li>{@code dev_bulb}: no item model or texture.</li>
     *   <li>{@code iron_plant_tank_ugprade}: no item model or texture.</li>
     *   <li>{@code gold_plant_tank_upgrade}: no item model or texture.</li>
     *   <li>{@code leveling_sensor}: no item model or texture.</li>
     *   <li>{@code electric_fence}: no item model; the block has no model either.</li>
     *   <li>{@code coal_generator}: no item model; the block has no model either.</li>
     *   <li>{@code creative_generator}: no item model; the block has no model either.</li>
     * </ul>
     */
    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        Set<Item> withoutDefinition = Set.of(
                ItemInit.AMBER_ITEM.get(),
                ItemInit.DEV_FILTER.get(),
                ItemInit.DEV_COMPUTER_CHIP.get(),
                ItemInit.DEV_SSD.get(),
                ItemInit.DEV_BULB.get(),
                ItemInit.IRON_PLANT_TANK.get(),
                ItemInit.GOLD_PLANT_TANK.get(),
                ItemInit.LEVELING_SENSOR.get(),
                BlockInit.ELECTRIC_FENCE.get().asItem(),
                BlockInit.COAL_GENERATOR.get().asItem(),
                BlockInit.CREATIVE_GENERATOR.get().asItem());
        return super.getKnownItems().filter(item -> !withoutDefinition.contains(item.value()));
    }

    /**
     * The fossil item: a {@code select} on {@link FossilTextureProperty} with one case per piece and
     * quality ({@code <piece>_<quality>}), so a new piece or quality gets its case here. Each case
     * is the flat model {@code item/fossil/<piece>_<quality>} with the piece's overlay texture for
     * that quality. The fallback is the case that the property gives a fossil without a piece or a
     * quality (Tyrannosaurus skull, common). The model {@code item/fossil} is written as 1.20.1 had
     * it; no case uses it.
     */
    private static void fossilItem(ItemModelGenerators itemModels) {
        Item fossil = ItemInit.FOSSIL_ITEM.get();
        flatModel(itemModels, ModelLocationUtils.getModelLocation(fossil), "block/fossil_overlay/common/spine");
        List<SelectItemModel.SwitchCase<String>> cases = new ArrayList<>();
        for (FossilPiece piece : FossilPieces.all()) {
            for (Quality quality : Quality.values()) {
                String value = piece.name() + "_" + quality.getName();
                Identifier model = flatModel(itemModels, ModelLocationUtils.getModelLocation(fossil, "/" + value),
                        piece.texturePath(quality));
                cases.add(ItemModelUtils.when(value, ItemModelUtils.plainModel(model)));
            }
        }
        String fallbackValue = FossilPieces.TYRANNOSAURUS_SKULL.name() + "_" + Quality.COMMON.getName();
        Identifier fallback = ModelLocationUtils.getModelLocation(fossil, "/" + fallbackValue);
        itemModels.itemModelOutput.accept(fossil,
                ItemModelUtils.select(new FossilTextureProperty(), ItemModelUtils.plainModel(fallback), cases));
    }

    /**
     * The test tube item: a {@code select} on {@link TestTubeOverlayProperty}. An empty tube
     * ({@link TestTubeOverlayProperty#NONE}) shows {@code item/test_tube}. The species listed here
     * are the ones with an overlay texture ({@code item/dino_overlay/<species>}); each shows the full
     * tube with its overlay ({@code item/test_tube/<species>}). Add a species here when its overlay
     * texture is added. Any other sample gets the fallback: the full tube without an overlay
     * ({@code item/test_tube/full}).
     */
    private static void testTubeItem(ItemModelGenerators itemModels) {
        Item testTube = ItemInit.TEST_TUBE_ITEM.get();
        Identifier empty = flatModel(itemModels, ModelLocationUtils.getModelLocation(testTube), "item/test_tube");
        Identifier full = flatModel(itemModels, ModelLocationUtils.getModelLocation(testTube, "/full"), "item/test_tube_full");
        List<SelectItemModel.SwitchCase<String>> cases = new ArrayList<>();
        cases.add(ItemModelUtils.when(TestTubeOverlayProperty.NONE, ItemModelUtils.plainModel(empty)));
        for (EntityType<?> species : List.of(EntityInit.TYRANNOSAURUS_REX.get(), EntityInit.TRICERATOPS.get())) {
            String value = EntityType.getKey(species).getPath();
            Identifier model = ModelTemplates.TWO_LAYERED_ITEM.create(
                    ModelLocationUtils.getModelLocation(testTube, "/" + value),
                    TextureMapping.layered(texture("item/test_tube_full"), texture("item/dino_overlay/" + value)),
                    itemModels.modelOutput);
            cases.add(ItemModelUtils.when(value, ItemModelUtils.plainModel(model)));
        }
        itemModels.itemModelOutput.accept(testTube,
                ItemModelUtils.select(new TestTubeOverlayProperty(), ItemModelUtils.plainModel(full), cases));
    }

    /**
     * The syringe item: a syringe with an embryo or with DNA data shows the filled syringe
     * ({@code item/syringe_embryo}); any other syringe shows {@code item/syringe}. The model
     * {@code item/syringe_dna} is written as 1.20.1 had it; no definition uses it.
     */
    private static void syringeItem(ItemModelGenerators itemModels) {
        Item syringe = ItemInit.SYRINGE.get();
        Identifier empty = flatModel(itemModels, ModelLocationUtils.getModelLocation(syringe), "item/syringe");
        flatModel(itemModels, ModelLocationUtils.getModelLocation(syringe, "_dna"), "item/syringe_blood");
        Identifier filled = flatModel(itemModels, ModelLocationUtils.getModelLocation(syringe, "_embryo"), "item/syringe_embryo");
        itemModels.itemModelOutput.accept(syringe, ItemModelUtils.conditional(
                ItemModelUtils.hasComponent(DataComponentInit.EMBRYO.get()),
                ItemModelUtils.plainModel(filled),
                ItemModelUtils.conditional(
                        ItemModelUtils.hasComponent(DataComponentInit.DNA_DATA.get()),
                        ItemModelUtils.plainModel(filled),
                        ItemModelUtils.plainModel(empty))));
    }

    /**
     * One model per quality, named {@code block/<block id>_<quality>}: the host stone with the
     * piece's overlay for that quality on top. The block state picks the model by quality.
     */
    private static void fossilBlock(BlockModelGenerators blockModels, FossilBlock block) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(
                PropertyDispatch.initial(FossilBlock.QUALITY).generate((Quality quality) -> {
                    TextureMapping textures = new TextureMapping()
                            .put(STONE, TextureMapping.getBlockTexture(block.getBase().getBlock()))
                            .put(OVERLAY, new Material(Constants.modLoc(block.getFossilPiece().texturePath(quality))));
                    return BlockModelGenerators.plainVariant(FOSSIL_BASE.createWithSuffix(
                            block, "_" + quality.getName(), textures, blockModels.modelOutput));
                })));
    }

    /** The host stone with the amber overlay on top. */
    private static void amberBlock(BlockModelGenerators blockModels, Block base, Block block) {
        TextureMapping textures = new TextureMapping()
                .put(STONE, TextureMapping.getBlockTexture(base))
                .put(OVERLAY, new Material(Constants.modLoc("block/fossil_overlay/amber/amber")));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block,
                BlockModelGenerators.plainVariant(FOSSIL_BASE.create(block, textures, blockModels.modelOutput))));
    }

    /** One block state for every state, pointing at a model with only a particle texture. */
    private static void particleOnlyBlock(BlockModelGenerators blockModels, Block block, String particleTexture) {
        TextureMapping textures = TextureMapping.particle(new Material(Constants.modLoc(particleTexture)));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block,
                BlockModelGenerators.plainVariant(PARTICLE_ONLY.create(block, textures, blockModels.modelOutput))));
    }

    /** The item model {@code item/<item id>} with only a parent, and the definition that uses it. */
    private static void parentItem(ItemModelGenerators itemModels, Item item, Identifier parent) {
        Identifier model = ModelLocationUtils.getModelLocation(item);
        itemModels.modelOutput.accept(model, new DelegatedModel(parent));
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    /** A flat item model ({@code item/generated}) with one texture of this mod, without an item definition. */
    private static Identifier flatModel(ItemModelGenerators itemModels, Identifier model, String texture) {
        return ModelTemplates.FLAT_ITEM.create(model, TextureMapping.layer0(texture(texture)), itemModels.modelOutput);
    }

    /** The texture of this mod at this path, for example {@code item/test_tube}. */
    private static Material texture(String path) {
        return new Material(Constants.modLoc(path));
    }
}
