package net.dumbcode.projectnublar.datagen;

import com.geckolib.renderer.internal.GeckolibItemSpecialRenderer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.registry.BlockInit;
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
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Optional;
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
 * tube, and {@code overrides} of the syringe. These three items get no definition here: their
 * definitions are the select and condition definitions of the later port tasks.
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

        // Models only: the definitions that pick between them are not generated here (see the class note).
        Item syringe = ItemInit.SYRINGE.get();
        flatModel(itemModels, ModelLocationUtils.getModelLocation(ItemInit.FOSSIL_ITEM.get()), "block/fossil_overlay/common/spine");
        flatModel(itemModels, ModelLocationUtils.getModelLocation(ItemInit.TEST_TUBE_ITEM.get()), "item/test_tube");
        flatModel(itemModels, ModelLocationUtils.getModelLocation(syringe), "item/syringe");
        flatModel(itemModels, ModelLocationUtils.getModelLocation(syringe, "_dna"), "item/syringe_blood");
        flatModel(itemModels, ModelLocationUtils.getModelLocation(syringe, "_embryo"), "item/syringe_embryo");
    }

    /**
     * Nothing is checked against the registry: like the 1.20.1 providers, this provider writes the
     * listed blocks only. The default (every block of the mod) would fail, because five blocks have
     * no block state at all: {@code electric_fence}, {@code low_security_electric_fence_post},
     * {@code high_security_electric_fence_post} (their block entity renderer draws them),
     * {@code coal_generator} and {@code creative_generator}. Task 19 of the 26.2 port reconsiders a
     * filtered set in place of the empty one.
     */
    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    /**
     * Nothing is checked or filled in against the registry. The default (every item of the mod)
     * would fail on the items without a definition here, and would give each block item without
     * one a definition pointing at {@code block/<id>}, which is wrong for the items with a
     * hand-written model or none (for example {@code carnivore_feeder_one}, {@code coal_generator}).
     * The fossil, test tube and syringe definitions come from later port tasks. Task 19 of the 26.2
     * port reconsiders a filtered set in place of the empty one.
     */
    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.empty();
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

    /** A flat item model ({@code item/generated}) with one texture, without an item definition. */
    private static void flatModel(ItemModelGenerators itemModels, Identifier model, String texture) {
        ModelTemplates.FLAT_ITEM.create(model, TextureMapping.layer0(new Material(Constants.modLoc(texture))),
                itemModels.modelOutput);
    }
}
