package net.dumbcode.projectnublar.api.dinosaur;

import net.minecraft.nbt.CompoundTag;

public record DinoBehaviourData(
        String speciesID,
        String dietID,

        double maxHealth,
        double maxStamina,
        double attackDamage,
        double speedModifier,
        double sizeMultiplier,
        double intelligence,
        double immunity,
        double resistance,
        double healthRegen,
        double growthRate,
        double fertility,
        double gestationTime,
        double eggClutchSize,
        double visionQuality,

        double domesticity,
        double aggressionLevel,
        double trustThreshold,
        double trustMultiplier,
        double socialNeed,
        double socialDrain,
        int groupSize,

        double eatRate,
        double drinkRate,
        int starvationLimit,
        int dehydrationLimit,
        double staminaDrain,

        double happyThreshold,
        double uncomfortableThreshold,
        double rageThreshold,

        boolean canFormGroup,
        boolean isNocturnal
)
{

    public static DinoBehaviourData fromNBT(CompoundTag tag) {
        String pSpeciesID = tag.getStringOr("species_id", "");
        String pDietID = tag.getStringOr("diet_id", "");


        double pMaxHealth = tag.getDoubleOr("default_health", 0);
        double pEnergyCapacity= tag.getDoubleOr("default_energy_capacity", 0);
        double pAttack = tag.getDoubleOr("default_attack_damage", 0);
        double pSpeed= tag.getDoubleOr("default_speed", 0);
        double pSize= tag.getDoubleOr("default_size", 0);
        double pIntelligence= tag.getDoubleOr("default_intelligence", 0);
        double pImmunity= tag.getDoubleOr("default_immunity", 0);
        double pResistance= tag.getDoubleOr("default_resistance", 0);
        double pHealthRegen= tag.getDoubleOr("default_health_regen", 0);
        double pGrowthRate= tag.getDoubleOr("default_growth_rate", 0);
        double pFertility= tag.getDoubleOr("default_fertility", 0);
        double pGestationTime= tag.getDoubleOr("default_gestation_time", 0);
        double pClutchSize= tag.getDoubleOr("default_egg_clutch", 0);
        double pVisionQuality= tag.getDoubleOr("default_vision", 0);

        double pDomesticity= tag.getDoubleOr("default_domesticity", 0);
        double pAggressionScore= tag.getDoubleOr("default_aggression", 0);
        double pTamingScore= tag.getDoubleOr("default_tame_score", 0);
        double pTrustIncrease = tag.getDoubleOr("default_trust_increase", 0);
        double pSocial= tag.getDoubleOr("default_social", 0);
        double pSocialDrain= tag.getDoubleOr("default_social_drain", 0);
        int pGroupSize= tag.getIntOr("default_group_size", 0);


        double pEatRate= tag.getDoubleOr("default_eat_rate", 0);
        double pDehydrationRate = tag.getDoubleOr("default_dehydration_rate", 0);
        int pStarvationLimit = tag.getIntOr("default_starvation_limit", 0);
        int pDehydrationLimit = tag.getIntOr("default_dehydration_limit", 0);
        double pBaseExhaustionRate = tag.getDoubleOr("default_exhaustion_rate", 0);

        double pLowRisk = tag.getDoubleOr("low_risk_threshold", 0);
        double pMediumRisk = tag.getDoubleOr("medium_risk_threshold", 0);
        double pHighRisk = tag.getDoubleOr("high_risk_threshold", 0);

        boolean pPack = tag.getBooleanOr("can_form_group", false);
        boolean pNocturnal = tag.getBooleanOr("nocturnal", false);

        return new DinoBehaviourData(pSpeciesID,pDietID,pMaxHealth,pEnergyCapacity,pAttack,pSpeed,pSize,
                pIntelligence,pImmunity,pResistance,pHealthRegen,pGrowthRate,pFertility,pGestationTime,pClutchSize,pVisionQuality,pDomesticity,
                pAggressionScore,pTamingScore,pTrustIncrease,pSocial,pSocialDrain,pGroupSize,pEatRate,pDehydrationRate,pStarvationLimit,pDehydrationLimit,
                pBaseExhaustionRate,pLowRisk,pMediumRisk,pHighRisk,pPack,pNocturnal);
    }

    public CompoundTag toNBT(DinoBehaviourData behaviourData) {
        CompoundTag tag = new CompoundTag();
        tag.putString("species_id", behaviourData.speciesID);
        tag.putString("diet_id", behaviourData.dietID);

        tag.putDouble("default_health",behaviourData.maxHealth);
        tag.putDouble("default_stamina",behaviourData.maxStamina);
        tag.putDouble("default_attack_damage",behaviourData.attackDamage);
        tag.putDouble("default_speed",behaviourData.speedModifier);
        tag.putDouble("default_size",behaviourData.sizeMultiplier);
        tag.putDouble("default_intelligence",behaviourData.intelligence);
        tag.putDouble("default_immunity",behaviourData.immunity);
        tag.putDouble("default_resistance",behaviourData.resistance);
        tag.putDouble("default_health_regen",behaviourData.healthRegen);
        tag.putDouble("default_growth_rate",behaviourData.growthRate);
        tag.putDouble("default_fertility",behaviourData.fertility);
        tag.putDouble("default_gestation_time",behaviourData.gestationTime);
        tag.putDouble("default_egg_clutch",behaviourData.eggClutchSize);
        tag.putDouble("default_vision",behaviourData.visionQuality);


        tag.putDouble("default_domesticity",behaviourData.domesticity);
        tag.putDouble("default_aggression",behaviourData.aggressionLevel);
        tag.putDouble("default_tame_score",behaviourData.trustThreshold);
        tag.putDouble("default_trust_increase",behaviourData.trustMultiplier);
        tag.putDouble("default_social",behaviourData.socialNeed);
        tag.putDouble("default_social_drain",behaviourData.socialDrain);
        tag.putInt("default_group_size",behaviourData.groupSize);


        tag.putDouble("default_eat_rate", behaviourData.eatRate);
        tag.putDouble("default_dehydration_rate", behaviourData.drinkRate);
        tag.putInt("default_starvation_limit",behaviourData.starvationLimit);
        tag.putInt("default_dehydration_limit",behaviourData.dehydrationLimit);
        tag.putDouble("default_exhaustion_rate", behaviourData.staminaDrain);

        tag.putDouble("low_risk_threshold",behaviourData.happyThreshold);
        tag.putDouble("medium_risk_threshold",behaviourData.uncomfortableThreshold);
        tag.putDouble("high_risk_threshold",behaviourData.rageThreshold);

        tag.putBoolean("can_form_group", behaviourData.canFormGroup);
        tag.putBoolean("nocturnal", behaviourData.isNocturnal);

        return tag;
    }
}


