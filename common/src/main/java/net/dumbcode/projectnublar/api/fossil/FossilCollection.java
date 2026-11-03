package net.dumbcode.projectnublar.api.fossil;

import dev.architectury.registry.registries.DeferredSupplier;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.item.fossil.FossilBlockItem;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * The fossil ores and amber blocks of one species.
 *
 * <p>There is one fossil block per host stone and piece; its quality is a block state. Each block
 * has four items, one per quality, that place it with that quality. There is one amber block per
 * host stone.
 *
 * <p>The id formulas of {@link #blockId}, {@link #itemId} and the amber blocks are registry
 * contracts.
 *
 * @param period the geological period the species generates in, as named in {@code FossilsConfig}
 */
public record FossilCollection(
        Identifier entityType,
        String period,
        List<FossilPiece> pieces,
        Map<FossilBase, Map<FossilPiece, DeferredSupplier<Block>>> fossilBlocks,
        Map<FossilBase, Map<FossilPiece, Map<Quality, DeferredSupplier<Item>>>> fossilItems,
        Map<FossilBase, DeferredSupplier<Block>> amberBlocks) {

    /** Every collection, keyed by entity type id, in creation order. */
    public static final Map<Identifier, FossilCollection> COLLECTIONS = new LinkedHashMap<>();

    /**
     * Registers the fossil blocks, their items and the amber blocks of a species, and records the
     * collection in {@link #COLLECTIONS}. Call it while {@link BlockInit} initialises.
     */
    public static FossilCollection create(Identifier entityType, String period, List<FossilPiece> pieces) {
        Map<FossilBase, Map<FossilPiece, DeferredSupplier<Block>>> fossilBlocks = new EnumMap<>(FossilBase.class);
        Map<FossilBase, Map<FossilPiece, Map<Quality, DeferredSupplier<Item>>>> fossilItems = new EnumMap<>(FossilBase.class);
        Map<FossilBase, DeferredSupplier<Block>> amberBlocks = new EnumMap<>(FossilBase.class);

        for (FossilBase base : FossilBase.values()) {
            Map<FossilPiece, DeferredSupplier<Block>> blocks = new LinkedHashMap<>();
            Map<FossilPiece, Map<Quality, DeferredSupplier<Item>>> items = new LinkedHashMap<>();
            for (FossilPiece piece : pieces) {
                String blockId = blockId(entityType, base, piece);
                DeferredSupplier<Block> block = BlockInit.BLOCKS.register(blockId, () -> new FossilBlock(
                        stoneCopy(base, blockId), entityType, piece, base));
                blocks.put(piece, block);

                // Common goes last: the last item registered for a block becomes block.asItem().
                // Every quality item shows the block's name, as a 1.20.1 BlockItem did.
                Map<Quality, DeferredSupplier<Item>> qualityItems = new EnumMap<>(Quality.class);
                for (Quality quality : List.of(Quality.FRAGMENTED, Quality.POOR, Quality.PRISTINE, Quality.COMMON)) {
                    String itemId = itemId(blockId, quality);
                    qualityItems.put(quality, ItemInit.ITEMS.register(itemId, () -> new FossilBlockItem(block.get(),
                            ItemInit.getItemProperties(itemId).overrideDescription(block.get().getDescriptionId()),
                            quality)));
                }
                items.put(piece, qualityItems);
            }
            fossilBlocks.put(base, blocks);
            fossilItems.put(base, items);

            String amberId = base.getName() + "_" + entityType.getPath() + "_amber";
            DeferredSupplier<Block> amber = BlockInit.BLOCKS.register(amberId,
                    () -> new AmberBlock(stoneCopy(base, amberId), entityType, base.getBlock()));
            ItemInit.ITEMS.register(amberId, () -> new BlockItem(amber.get(),
                    ItemInit.getItemProperties(amberId).useBlockDescriptionPrefix()));
            amberBlocks.put(base, amber);
        }

        FossilCollection collection =
                new FossilCollection(entityType, period, pieces, fossilBlocks, fossilItems, amberBlocks);
        COLLECTIONS.put(entityType, collection);
        return collection;
    }

    /**
     * The host stone's properties for the block {@code id}, without occlusion. {@code ofLegacyCopy}
     * is the 1.20.1 {@code copy}, so the block behaves as on 1.20.1. {@code ofFullCopy} also copies
     * the stone's jump factor, its redstone-conductor, valid-spawn, post-process, suffocating and
     * view-blocking tests, and its loot table and name rules.
     */
    private static BlockBehaviour.Properties stoneCopy(FossilBase base, String id) {
        return BlockBehaviour.Properties.ofLegacyCopy(base.getBlock()).noOcclusion().setId(BlockInit.blockKey(id));
    }

    /** Every fossil block of this collection. */
    public Stream<DeferredSupplier<Block>> blocks() {
        return fossilBlocks.values().stream().flatMap(blocks -> blocks.values().stream());
    }

    /** The item that places the fossil block of this stone and piece with this quality. */
    public DeferredSupplier<Item> item(FossilBase base, FossilPiece piece, Quality quality) {
        return fossilItems.get(base).get(piece).get(quality);
    }

    /** For example {@code stone_tyrannosaurus_rex_tyrannosaurus_skull}. */
    public static String blockId(Identifier entityType, FossilBase base, FossilPiece piece) {
        return base.getName() + "_" + entityType.getPath() + "_" + piece.name();
    }

    /** The Common item carries the block id; the others prefix it with their quality name. */
    public static String itemId(String blockId, Quality quality) {
        return quality == Quality.COMMON ? blockId : quality.getName() + "_" + blockId;
    }
}
