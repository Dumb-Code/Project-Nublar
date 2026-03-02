package net.dumbcode.projectnublar.worldgen;

import com.mojang.serialization.Codec;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.init.DinosaurInit;
import net.dumbcode.projectnublar.util.FossilUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import java.util.*;
import java.util.function.Function;

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

        for(int x = minX; x <= minX + width; ++x) {
            for(int z = minZ; z <= minZ + width; ++z) {
                if (minY <= worldgenlevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z)) {
                    return this.doPlace(worldgenlevel, randomsource, fossilConfiguration, startX, endX, StartZ, endZ, startY, endY, minX, minY, minZ, width, height);
                }
            }
        }

        return false;
    }
    protected boolean doPlace(WorldGenLevel level, RandomSource random, FossilConfiguration config, double minX, double maxX, double minZ, double maxZ, double minY, double maxY, int x, int y, int z, int width, int height) {
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

        //get random weighted quality from config values
        String quality = getRandomWeightedQuality(new Random());
        String period = FossilsConfig.getPeriod(y);
        FossilsConfig.Fossil fossil = getRandomWeightedFossil(new Random(),period);


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
                                                        BlockPos blockPos = new BlockPos(localX,localY,localZ);
                                                        BlockState blockstate = levelchunksection.getBlockState(localX, localY, localZ);
                                                        Map<String,Integer> pieces = FossilUtils.getPiecesForDino(fossil);

                                                        //Get random weighted Piece
                                                        for (FossilConfiguration.TargetBlockState fossilConfiguration$targetblockstate : config.targetStates){
                                                            Objects.requireNonNull(bulksectionaccess);
                                                            if(canPlaceOre(blockstate,bulksectionaccess::getBlockState,random,config,fossilConfiguration$targetblockstate,blockpos$mutableblockpos)){
                                                                String piece = getRandomWeightedPiece(new Random(),pieces);
                                                                FossilBlock block = (FossilBlock) fossilConfiguration$targetblockstate.state.getBlock();
                                                                BlockState setQuality = block.defaultBlockState().setValue(FossilBlock.QUALITY_PROPERTY, Quality.byName(quality));

                                                                if(block.getFossilPiece().name().equals(piece)){
                                                                    System.err.println("Placing fossil block:" + block.getFossilPiece().name() + " at co-ords" + localX+ " " +localY+ " "+localZ);
                                                                    levelchunksection.setBlockState(localX, localY, localZ, setQuality, false);
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
            }
        }

        return i > 0;
    }

    public static String getRandomWeightedQuality(Random random) {
       List<String> qualities = List.of("fragmented","poor","common","pristine");
       int totalWeights = 0;
       Map<String,Integer> qualityWeights = new HashMap<>();

       for(String quality : qualities){
           qualityWeights.put(quality,FossilsConfig.getQuality(quality).weight().get());
           totalWeights += FossilsConfig.getQuality(quality).weight().get();
       }
       if (totalWeights <= 0) {
           throw new IllegalArgumentException("Total weight must be > 0");
       }
       int r = random.nextInt(totalWeights);

       String selectedQuality = qualities.get(2);

       for(Map.Entry<String,Integer> entry : qualityWeights.entrySet()){
           r -= entry.getValue();
           if(r <= 0){
               selectedQuality = entry.getKey();
           }
       }

       return selectedQuality;

    }
    public static String getRandomWeightedPiece(Random random,Map<String,Integer> pieceWeights) {
        int totalWeights = 0;

        for(int value : pieceWeights.values()){
            totalWeights += value;
        }
        int r = random.nextInt(totalWeights);

        for(Map.Entry<String,Integer> entry : pieceWeights.entrySet()){
            r -= entry.getValue();
            if(r <= 0){
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Total weight must be > 0");
    }



    public static FossilsConfig.Fossil getRandomWeightedFossil(Random rand, String period) {
        List<FossilsConfig.Fossil> fossils = new ArrayList<>();

        int totalWeights = 0;

        for(FossilsConfig.Fossil fossil : FossilsConfig.getFossils().values()){
            if(fossil.getPeriods().get().contains(period)) {
                totalWeights += fossil.getWeight().get();
                fossils.add(fossil);
            }
        }

        if(totalWeights <= 1) {
            totalWeights = 2;
        }
        int r = rand.nextInt(totalWeights);

        for(FossilsConfig.Fossil fossil : fossils){
            r-= fossil.getWeight().get();
            if(r < 0){
              //  return fossil;
            }
        }
        return FossilUtils.getFossilForDino(DinosaurInit.TYRANNOSAURUS_REX);
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
