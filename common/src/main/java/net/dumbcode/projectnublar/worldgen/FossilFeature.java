package net.dumbcode.projectnublar.worldgen;

import com.mojang.serialization.Codec;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.util.FossilUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * A fossil vein. Its shape is the vanilla ore shape: a chain of spheres along a random line
 * through the origin.
 *
 * <p>Each vein holds one species and one quality. The species is picked by weight from the species
 * of the period at the vein's depth; the quality is picked by weight from the four qualities.
 * Each block of the vein then rolls its own piece from the species' piece weights, and becomes the
 * target block of that species and piece when the stone there can take it.
 */
public class FossilFeature extends Feature<FossilConfiguration> {

    public FossilFeature(Codec<FossilConfiguration> codec) {
        super(codec);
    }
    public boolean place(FeaturePlaceContext<FossilConfiguration> context) {
        RandomSource randomsource = context.random();
        BlockPos blockpos = context.origin();
        WorldGenLevel worldgenlevel = context.level();
        FossilConfiguration fossilConfiguration = context.config();

        float randomAngle = randomsource.nextFloat() * (float)Math.PI;
        float veinHalfLength = (float)fossilConfiguration.size / 8.0F;
        int boundingRadius = Mth.ceil(((float)fossilConfiguration.size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double startX = (double)blockpos.getX() + Math.sin((double)randomAngle) * (double)veinHalfLength;
        double endX = (double)blockpos.getX() - Math.sin((double)randomAngle) * (double)veinHalfLength;
        double StartZ = (double)blockpos.getZ() + Math.cos((double)randomAngle) * (double)veinHalfLength;
        double endZ = (double)blockpos.getZ() - Math.cos((double)randomAngle) * (double)veinHalfLength;

        double startY = (double)(blockpos.getY() + randomsource.nextInt(3) - 2);
        double endY = (double)(blockpos.getY() + randomsource.nextInt(3) - 2);

        int minX = blockpos.getX() - Mth.ceil(veinHalfLength) - boundingRadius;
        int minY = blockpos.getY() - 2 - boundingRadius;
        int minZ = blockpos.getZ() - Mth.ceil(veinHalfLength) - boundingRadius;

        int width = 2 * (Mth.ceil(veinHalfLength) + boundingRadius);
        int height = 2 * (2 + boundingRadius);
        // The species is chosen by the depth of the vein origin, which the height placement keeps inside a period band
        String period = FossilsConfig.getPeriod(blockpos.getY());

        for(int x = minX; x <= minX + width; ++x) {
            for(int z = minZ; z <= minZ + width; ++z) {
                if (minY <= worldgenlevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z)) {
                    return this.doPlace(worldgenlevel, randomsource, fossilConfiguration, period, startX, endX, StartZ, endZ, startY, endY, minX, minY, minZ, width, height);
                }
            }
        }

        return false;
    }
    protected boolean doPlace(WorldGenLevel level, RandomSource random, FossilConfiguration config, String period, double minX, double maxX, double minZ, double maxZ, double minY, double maxY, int x, int y, int z, int width, int height) {
        int i = 0;
        BitSet bitset = new BitSet(width * height * width);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int veinSize = config.size;

        //Generate the sphere chain
        // Each sphere stores: [centerX, centerY, centerZ, radius]
        double[] sphereData = new double[veinSize * 4];
        for(int step = 0; step < veinSize; ++step) {
            // Interpolation progress along the vein (0 → 1)
            float progress = (float)step / (float)veinSize;
            // Interpolate position along vein line
            double centerX = Mth.lerp((double)progress, minX, maxX);
            double centerY = Mth.lerp((double)progress, minY, maxY);
            double centerZ = Mth.lerp((double)progress, minZ, maxZ);
            // Random radius scaling
            double randomScale = random.nextDouble() * (double)veinSize / (double)16.0F;
            // Makes vein thicker in middle, thinner at ends
            double radius = ((double)(Mth.sin((float)Math.PI * progress) + 1.0F) * randomScale + (double)1.0F) / (double)2.0F;
            sphereData[step * 4 + 0] = centerX;
            sphereData[step * 4 + 1] = centerY;
            sphereData[step * 4 + 2] = centerZ;
            sphereData[step * 4 + 3] = radius;
        }

        //cull overlapping spheres
        for(int a = 0; a < veinSize - 1; ++a) {
            if (!(sphereData[a * 4 + 3] <= (double)0.0F)) {
                for(int b = a + 1; b < veinSize; ++b) {
                    if (!(sphereData[b * 4 + 3] <= (double)0.0F)) {
                        //Distance between centers
                        double dX = sphereData[a * 4 + 0] - sphereData[b * 4 + 0];
                        double dY = sphereData[a * 4 + 1] - sphereData[b * 4 + 1];
                        double dZ = sphereData[a * 4 + 2] - sphereData[b * 4 + 2];
                        double radiusDiff = sphereData[a * 4 + 3] - sphereData[b * 4 + 3];
                        // If one sphere fully contains the other
                        if (radiusDiff * radiusDiff > dX * dX + dY * dY + dZ * dZ) {
                            // Kill the smaller one
                            if (radiusDiff > (double)0.0F) {
                                sphereData[b * 4 + 3] = (double)-1.0F;
                            } else {
                                sphereData[a * 4 + 3] = (double)-1.0F;
                            }
                        }
                    }
                }
            }
        }

        // One quality and one species per vein
        Optional<Quality> quality = pickQuality(random);
        Optional<Map.Entry<ResourceLocation, FossilsConfig.SpeciesFossils>> species = pickSpecies(random, period);
        if (quality.isEmpty() || species.isEmpty() || !FossilCollection.COLLECTIONS.containsKey(species.get().getKey())) {
            return false;
        }
        LinkedHashMap<String, Integer> pieceWeights = FossilUtils.pieceWeights(species.get().getValue());
        if (pieceWeights.values().stream().mapToInt(Integer::intValue).sum() <= 0) {
            return false;
        }
        Map<String, FossilConfiguration.TargetBlockState> targetsByPiece = targetsByPiece(config, species.get().getKey());
        if (targetsByPiece.isEmpty()) {
            return false;
        }

        try (BulkSectionAccess bulksectionaccess = new BulkSectionAccess(level)) {
            for(int sphereIndex = 0; sphereIndex < veinSize; ++sphereIndex) {
                double radius = sphereData[sphereIndex * 4 + 3];
                if (!(radius < (double)0.0F)) {
                    double centerX = sphereData[sphereIndex * 4 + 0];
                    double centerY = sphereData[sphereIndex * 4 + 1];
                    double centerZ = sphereData[sphereIndex * 4 + 2];
                    int minSphereX = Math.max(Mth.floor(centerX - radius), x);
                    int minSphereY = Math.max(Mth.floor(centerY - radius), y);
                    int minSphereZ = Math.max(Mth.floor(centerZ - radius), z);
                    int maxSphereX = Math.max(Mth.floor(centerX + radius), minSphereX);
                    int maxSphereY = Math.max(Mth.floor(centerY + radius), minSphereY);
                    int maxSphereZ = Math.max(Mth.floor(centerZ + radius), minSphereZ);

                    for(int pX = minSphereX; pX <= maxSphereX; ++pX) {
                        double normalisedX = ((double)pX + (double)0.5F - centerX) / radius;
                        if (normalisedX * normalisedX < (double)1.0F) {
                            for(int pY = minSphereY; pY <= maxSphereY; ++pY) {
                                double normalisedY = ((double)pY + (double)0.5F - centerY) / radius;
                                if (normalisedX * normalisedX + normalisedY * normalisedY < (double)1.0F) {
                                    for(int pZ = minSphereZ; pZ <= maxSphereZ; ++pZ) {
                                        double normalisedZ = ((double)pZ + (double)0.5F - centerZ) / radius;
                                        if (normalisedX * normalisedX + normalisedY * normalisedY + normalisedZ * normalisedZ < (double)1.0F && !level.isOutsideBuildHeight(pY)) {
                                            int bitIndex = pX - x + (pY - y) * width + (pZ - z) * width * height;
                                            if (!bitset.get(bitIndex)) {
                                                bitset.set(bitIndex);
                                                blockpos$mutableblockpos.set(pX, pY, pZ);
                                                if (level.ensureCanWrite(blockpos$mutableblockpos)) {
                                                    LevelChunkSection levelchunksection = bulksectionaccess.getSection(blockpos$mutableblockpos);
                                                    if (levelchunksection != null) {
                                                        int localX = SectionPos.sectionRelative(pX);
                                                        int localY = SectionPos.sectionRelative(pY);
                                                        int localZ = SectionPos.sectionRelative(pZ);
                                                        BlockState blockstate = levelchunksection.getBlockState(localX, localY, localZ);
                                                        // One piece per block; no target for that piece places nothing here
                                                        FossilConfiguration.TargetBlockState target = targetsByPiece.get(pickPiece(random, pieceWeights));
                                                        if (target != null && canPlaceOre(blockstate, bulksectionaccess::getBlockState, random, config, target, blockpos$mutableblockpos)) {
                                                            levelchunksection.setBlockState(localX, localY, localZ, target.state.setValue(FossilBlock.QUALITY, quality.get()), false);
                                                            ++i;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return i > 0;
    }

    /**
     * Picks the vein's quality by the configured quality weights.
     *
     * @return empty when the weights add up to 0 or less, so a config value never crashes chunk generation
     */
    static Optional<Quality> pickQuality(RandomSource random) {
        List<Quality> qualities = List.of(Quality.values());
        ToIntFunction<Quality> weight = q -> FossilsConfig.getQuality(q.getName()).weight().get();
        int total = FossilRolls.totalWeight(qualities, weight);
        if (total <= 0) {
            return Optional.empty();
        }
        return Optional.of(FossilRolls.pickWeighted(random.nextInt(total), qualities, weight));
    }

    /**
     * Picks the vein's species by weight from the species configured for this period.
     *
     * @return empty when no species has this period or their weights add up to 0
     */
    static Optional<Map.Entry<ResourceLocation, FossilsConfig.SpeciesFossils>> pickSpecies(RandomSource random, String period) {
        List<Map.Entry<ResourceLocation, FossilsConfig.SpeciesFossils>> candidates = FossilsConfig.getFossils().entrySet().stream()
                .filter(entry -> entry.getValue().getPeriods().get().contains(period))
                .toList();
        ToIntFunction<Map.Entry<ResourceLocation, FossilsConfig.SpeciesFossils>> weight = entry -> entry.getValue().getWeight().get();
        int total = FossilRolls.totalWeight(candidates, weight);
        if (total <= 0) {
            return Optional.empty();
        }
        return Optional.of(FossilRolls.pickWeighted(random.nextInt(total), candidates, weight));
    }

    /** Picks one piece name by weight. */
    static String pickPiece(RandomSource random, LinkedHashMap<String, Integer> weights) {
        List<String> pieces = new ArrayList<>(weights.keySet());
        return FossilRolls.pickWeighted(random.nextInt(FossilRolls.totalWeight(pieces, weights::get)), pieces, weights::get);
    }

    /** The targets of this species, keyed by piece name. */
    private static Map<String, FossilConfiguration.TargetBlockState> targetsByPiece(FossilConfiguration config, ResourceLocation entityType) {
        Map<String, FossilConfiguration.TargetBlockState> targets = new HashMap<>();
        for (FossilConfiguration.TargetBlockState target : config.targetStates) {
            if (target.state.getBlock() instanceof FossilBlock block && block.getEntityType().equals(entityType)) {
                targets.put(block.getFossilPiece().name(), target);
            }
        }
        return targets;
    }

    public static boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> adjacentStateAccessor, RandomSource random, FossilConfiguration config, FossilConfiguration.TargetBlockState targetState, BlockPos.MutableBlockPos mutablePos) {
        if (!targetState.target.test(state, random)) {
            return false;
        } else if (shouldSkipAirCheck(random, config.discardChanceOnAirExposure)) {
            return true;
        } else {
            return !isAdjacentToAir(adjacentStateAccessor, mutablePos);
        }
    }
    protected static boolean shouldSkipAirCheck(RandomSource random, float chance) {
        if (chance <= 0.0F) {
            return true;
        } else if (chance >= 1.0F) {
            return false;
        } else {
            return random.nextFloat() >= chance;
        }
    }

}
