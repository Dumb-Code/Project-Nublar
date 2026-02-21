package net.dumbcode.projectnublar.worldgen;

import com.mojang.serialization.Codec;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.FossilPieces;
import net.dumbcode.projectnublar.api.fossil.FossilSets;
import net.dumbcode.projectnublar.api.fossil.Fossils;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.data.FossilConfigReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.jetbrains.annotations.Nullable;

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
        FossilConfiguration oreconfiguration = (FossilConfiguration)context.config();

        float f = randomsource.nextFloat() * (float)Math.PI;
        float f1 = (float)oreconfiguration.size / 8.0F;
        int i = Mth.ceil(((float)oreconfiguration.size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double d0 = (double)blockpos.getX() + Math.sin((double)f) * (double)f1;
        double d1 = (double)blockpos.getX() - Math.sin((double)f) * (double)f1;
        double d2 = (double)blockpos.getZ() + Math.cos((double)f) * (double)f1;
        double d3 = (double)blockpos.getZ() - Math.cos((double)f) * (double)f1;
        int j = 2;
        double d4 = (double)(blockpos.getY() + randomsource.nextInt(3) - 2);
        double d5 = (double)(blockpos.getY() + randomsource.nextInt(3) - 2);
        int k = blockpos.getX() - Mth.ceil(f1) - i;
        int l = blockpos.getY() - 2 - i;
        int i1 = blockpos.getZ() - Mth.ceil(f1) - i;
        int j1 = 2 * (Mth.ceil(f1) + i);
        int k1 = 2 * (2 + i);

        for(int l1 = k; l1 <= k + j1; ++l1) {
            for(int i2 = i1; i2 <= i1 + j1; ++i2) {
                if (l <= worldgenlevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, l1, i2)) {
                    return this.doPlace(worldgenlevel, randomsource, oreconfiguration, d0, d1, d2, d3, d4, d5, k, l, i1, j1, k1);
                }
            }
        }

        return false;
    }
    protected boolean doPlace(WorldGenLevel level, RandomSource random, FossilConfiguration config, double minX, double maxX, double minZ, double maxZ, double minY, double maxY, int x, int y, int z, int width, int height) {
        int i = 0;
        BitSet bitset = new BitSet(width * height * width);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int j = config.size;
        double[] adouble = new double[j * 4];


        int fossilCount = config.targetStates.size();

        Map<FossilConfiguration.TargetBlockState, Integer> weights = new HashMap<>();
        Map<FossilConfiguration.TargetBlockState, Integer> weightTracker = new HashMap<>();

        for(FossilConfiguration.TargetBlockState pState:  config.targetStates ) {
            FossilBlock block = (FossilBlock) pState.state.getBlock();
            FossilPiece piece = block.getFossilPiece();
            int weight = 1;

            if(piece.name().equals("leg_biped")){
                weight = 2;
            }
            if(piece.name().equals("leg_quadruped")){
                weight = 4;
            }
            if(piece.name().equals("foot")){
                weight = 2;
            }
            if(piece.name().equals("arm")){
                weight = 2;
            }

            weights.put(pState,weight);
            weightTracker.put(pState,0);
        }


        if (fossilCount == 0) {
            return false;
        }
        int fossilIndex = 0;

        boolean placedAny = false;

        for(int k = 0; k < j; ++k) {
            float f = (float)k / (float)j;
            double d0 = Mth.lerp((double)f, minX, maxX);
            double d1 = Mth.lerp((double)f, minY, maxY);
            double d2 = Mth.lerp((double)f, minZ, maxZ);
            double d3 = random.nextDouble() * (double)j / (double)16.0F;
            double d4 = ((double)(Mth.sin((float)Math.PI * f) + 1.0F) * d3 + (double)1.0F) / (double)2.0F;
            adouble[k * 4 + 0] = d0;
            adouble[k * 4 + 1] = d1;
            adouble[k * 4 + 2] = d2;
            adouble[k * 4 + 3] = d4;
        }

        for(int l3 = 0; l3 < j - 1; ++l3) {
            if (!(adouble[l3 * 4 + 3] <= (double)0.0F)) {
                for(int i4 = l3 + 1; i4 < j; ++i4) {
                    if (!(adouble[i4 * 4 + 3] <= (double)0.0F)) {
                        double d8 = adouble[l3 * 4 + 0] - adouble[i4 * 4 + 0];
                        double d10 = adouble[l3 * 4 + 1] - adouble[i4 * 4 + 1];
                        double d12 = adouble[l3 * 4 + 2] - adouble[i4 * 4 + 2];
                        double d14 = adouble[l3 * 4 + 3] - adouble[i4 * 4 + 3];
                        if (d14 * d14 > d8 * d8 + d10 * d10 + d12 * d12) {
                            if (d14 > (double)0.0F) {
                                adouble[i4 * 4 + 3] = (double)-1.0F;
                            } else {
                                adouble[l3 * 4 + 3] = (double)-1.0F;
                            }
                        }
                    }
                }
            }
        }

        try (BulkSectionAccess bulksectionaccess = new BulkSectionAccess(level)) {
            for(int j4 = 0; j4 < j; ++j4) {
                double d9 = adouble[j4 * 4 + 3];
                if (!(d9 < (double)0.0F)) {
                    double d11 = adouble[j4 * 4 + 0];
                    double d13 = adouble[j4 * 4 + 1];
                    double d15 = adouble[j4 * 4 + 2];
                    int k4 = Math.max(Mth.floor(d11 - d9), x);
                    int l = Math.max(Mth.floor(d13 - d9), y);
                    int i1 = Math.max(Mth.floor(d15 - d9), z);
                    int j1 = Math.max(Mth.floor(d11 + d9), k4);
                    int k1 = Math.max(Mth.floor(d13 + d9), l);
                    int l1 = Math.max(Mth.floor(d15 + d9), i1);

                    for(int i2 = k4; i2 <= j1; ++i2) {
                        double d5 = ((double)i2 + (double)0.5F - d11) / d9;
                        if (d5 * d5 < (double)1.0F) {
                            for(int j2 = l; j2 <= k1; ++j2) {
                                double d6 = ((double)j2 + (double)0.5F - d13) / d9;
                                if (d5 * d5 + d6 * d6 < (double)1.0F) {
                                    for(int k2 = i1; k2 <= l1; ++k2) {
                                        double d7 = ((double)k2 + (double)0.5F - d15) / d9;
                                        if (d5 * d5 + d6 * d6 + d7 * d7 < (double)1.0F && !level.isOutsideBuildHeight(j2)) {
                                            int l2 = i2 - x + (j2 - y) * width + (k2 - z) * width * height;
                                            if (!bitset.get(l2)) {
                                                bitset.set(l2);
                                                blockpos$mutableblockpos.set(i2, j2, k2);
                                                if (level.ensureCanWrite(blockpos$mutableblockpos)) {
                                                    LevelChunkSection levelchunksection = bulksectionaccess.getSection(blockpos$mutableblockpos);
                                                    if (levelchunksection != null) {
                                                        int i3 = SectionPos.sectionRelative(i2);
                                                        int j3 = SectionPos.sectionRelative(j2);
                                                        int k3 = SectionPos.sectionRelative(k2);
                                                        BlockPos blockPos = new BlockPos(i3,j3,k3);
                                                        BlockState blockstate = levelchunksection.getBlockState(i3, j3, k3);

                                                        FossilConfiguration.TargetBlockState predicateSource = config.targetStates.get(0);

                                                        if (predicateSource.target.test(blockstate  , random)) {
                                                            int attempts = weightTracker.get(config.targetStates.get(fossilIndex));
                                                            int weight = weights.get(config.targetStates.get(fossilIndex));

                                                            int p = 0;

                                                            while (attempts > weight) {
                                                                fossilIndex++;
                                                                if (fossilIndex >= fossilCount - 1) {
                                                                    p++;
                                                                    fossilIndex = 0;
                                                                }

                                                                attempts = weights.get(config.targetStates.get(fossilIndex));
                                                                weight = weights.get(config.targetStates.get(fossilIndex));
                                                                if(attempts < weight || p > 2) {
                                                                    break;
                                                                }
                                                            }
                                                            levelchunksection.setBlockState(i3, j3, k3, config.targetStates.get(fossilIndex).state, false);
                                                                attempts++;
                                                                weightTracker.remove(config.targetStates.get(fossilIndex));
                                                                weightTracker.put(config.targetStates.get(fossilIndex),attempts);

                                                                fossilIndex++;
                                                                if (fossilIndex >= fossilCount - 1) {
                                                                    fossilIndex = 0;
                                                                }

                                                                placedAny = true;

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

        return placedAny;
    }

}
