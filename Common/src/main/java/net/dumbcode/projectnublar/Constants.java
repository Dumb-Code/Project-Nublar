package net.dumbcode.projectnublar;

import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MODID = "projectnublar";
	public static final String MOD_NAME = "Project Nublar";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
	public static final ResourceKey<ConfiguredFeature<?,?>> FOSSIL = FeatureUtils.createKey(Constants.MODID + ":fossil");
	public static final ResourceKey<ConfiguredFeature<?,?>> AMBER = FeatureUtils.createKey(Constants.MODID + ":amber");
	public static final ResourceKey<PlacedFeature> FOSSIL_PLACED = PlacementUtils.createKey(Constants.MODID + ":fossil");
	public static final ResourceKey<PlacedFeature> AMBER_PLACED = PlacementUtils.createKey(Constants.MODID + ":amber");

}