package net.dumbcode.projectnublar.api.fossil;

import dev.architectury.registry.registries.DeferredSupplier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * The fossil and amber block variants registered for one species, indexed as
 * stone block -> quality -> piece (fossils) and stone block -> amber block (amber).
 *
 * <p>The registered block id formulas {@code {qualityPrefix}{stone}_{entityPath}_{piece}_fossil}
 * and {@code {stone}_{entity}_amber} are frozen registry contracts.
 */
public record FossilCollection(
        Map<Block, Map<Quality, Map<FossilPiece, DeferredSupplier<Block>>>> fossilblocks,
        Map<Block, DeferredSupplier<Block>> amberBlocks) {

    /** All created collections, keyed by the entity type id string. */
    public static Map<String, FossilCollection> COLLECTIONS = new HashMap<>();

    // TODO(BUG): these maps are static, so every FossilCollection created by create() aliases the
    // very same two maps; entries from later species overwrite earlier ones per stone block.
    public static Map<Block, Map<Quality, Map<FossilPiece, DeferredSupplier<Block>>>> fullFossilMap =
            new HashMap<>();
    public static Map<Block, DeferredSupplier<Block>> fullAmberMap = new HashMap<>();

    /** The stone variants a fossil/amber block is generated for. */
    public static List<Block> stonelist = List.of(
            Blocks.STONE,
            Blocks.ANDESITE, Blocks.GRANITE, Blocks.DIORITE, Blocks.SANDSTONE, Blocks.DEEPSLATE,
            Blocks.TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.ORANGE_TERRACOTTA,
            Blocks.YELLOW_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.WHITE_TERRACOTTA,
            Blocks.LIGHT_GRAY_TERRACOTTA
    );

    /** Overload for this mod's own entities. */
    public static FossilCollection create(String fossilName) {
        return create(Constants.modLoc(fossilName));
    }

    /**
     * Registers every fossil and amber block for the given entity type.
     *
     * <p>TODO(BUG): the return value is {@code Map.put}'s result, i.e. the <em>previous</em>
     * collection registered under this key - {@code null} on first registration. Callers such as
     * {@code BlockInit} therefore hold {@code null} references.
     */
    public static FossilCollection create(ResourceLocation entityType) {
        for (Block stone : stonelist) {
            String stoneName = BuiltInRegistries.BLOCK.getKey(stone).getPath();
            Map<Quality, Map<FossilPiece, DeferredSupplier<Block>>> qualityMap = new HashMap<>();

            for (Quality quality : Quality.values()) {
                String qualityPrefix = quality == Quality.NONE ? "" : quality.getName().toLowerCase() + "_";

                Map<FossilPiece, DeferredSupplier<Block>> pieceMap = new HashMap<>();

                for (FossilPiece piece : FossilPieces.getPiecesByEntityType(entityType)) {
                    String blockId = qualityPrefix + stoneName + "_" + entityType.getPath() + "_" + piece.name().toLowerCase() + "_fossil";

                    pieceMap.put(piece, BlockInit.registerBlock(blockId,
                            () -> new FossilBlock(
                                    BlockBehaviour.Properties.copy(stone).noOcclusion(),
                                    entityType, piece, quality, stone)));
                }
                qualityMap.put(quality, pieceMap);
            }

            fullFossilMap.put(stone, qualityMap);
            fullAmberMap.put(stone, BlockInit.registerBlock(
                    stoneName + "_" + entityType.getPath() + "_amber",
                    () -> new AmberBlock(
                            BlockBehaviour.Properties.copy(stone).noOcclusion(), entityType, stone)));
        }
        return COLLECTIONS.put(entityType.toString(), new FossilCollection(fullFossilMap, fullAmberMap));
    }
}
