package net.dumbcode.projectnublar.api;

import net.minecraft.nbt.CompoundTag;

public record DinoBehaviourData(
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
        double pMaxHealth = tag.getDouble("default_health");
        double pEnergyCapacity= tag.getDouble("default_energy_capacity");
        double pAttack = tag.getDouble("default_attack_damage");
        double pSpeed= tag.getDouble("default_speed");
        double pSize= tag.getDouble("default_size");
        double pIntelligence= tag.getDouble("default_intelligence");
        double pImmunity= tag.getDouble("default_immunity");
        double pResistance= tag.getDouble("default_resistance");
        double pHealthRegen= tag.getDouble("default_health_regen");
        double pGrowthRate= tag.getDouble("default_growth_rate");
        double pFertility= tag.getDouble("default_fertility");
        double pGestationTime= tag.getDouble("default_gestation_time");
        double pClutchSize= tag.getDouble("default_egg_clutch");
        double pVisionQuality= tag.getDouble("default_vision");

        double pDomesticity= tag.getDouble("default_domesticity");
        double pAggressionScore= tag.getDouble("default_aggression");
        double pTamingScore= tag.getDouble("default_tame_score");
        double pTrustIncrease = tag.getDouble("default_trust_increase");
        double pSocial= tag.getDouble("default_social");
        double pSocialDrain= tag.getDouble("default_social_drain");
        int pGroupSize= tag.getInt("default_group_size");


        double pEatRate= tag.getDouble("default_eat_rate");
        double pDehydrationRate = tag.getDouble("default_dehydration_rate");
        int pStarvationLimit = tag.getInt("default_starvation_limit");
        int pDehydrationLimit = tag.getInt("default_dehydration_limit");
        double pBaseExhaustionRate = tag.getDouble("default_exhaustion_rate");

        double pLowRisk = tag.getDouble("low_risk_threshold");
        double pMediumRisk = tag.getDouble("medium_risk_threshold");
        double pHighRisk = tag.getDouble("high_risk_threshold");

        boolean pPack = tag.getBoolean("can_form_group");
        boolean pNocturnal = tag.getBoolean("nocturnal");

        return new DinoBehaviourData(pMaxHealth,pEnergyCapacity,pAttack,pSpeed,pSize,
                pIntelligence,pImmunity,pResistance,pHealthRegen,pGrowthRate,pFertility,pGestationTime,pClutchSize,pVisionQuality,pDomesticity,
                pAggressionScore,pTamingScore,pTrustIncrease,pSocial,pSocialDrain,pGroupSize,pEatRate,pDehydrationRate,pStarvationLimit,pDehydrationLimit,
                pBaseExhaustionRate,pLowRisk,pMediumRisk,pHighRisk,pPack,pNocturnal);
    }

    public CompoundTag toNBT(DinoBehaviourData behaviourData) {
        CompoundTag tag = new CompoundTag();
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


